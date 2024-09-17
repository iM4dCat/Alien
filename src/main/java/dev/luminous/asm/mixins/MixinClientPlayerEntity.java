package dev.luminous.asm.mixins;

import com.mojang.authlib.GameProfile;
import dev.luminous.Alien;
import dev.luminous.api.events.Event;
import dev.luminous.api.events.impl.MoveEvent;
import dev.luminous.api.events.impl.MovementPacketsEvent;
import dev.luminous.api.events.impl.UpdateWalkingPlayerEvent;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import dev.luminous.mod.modules.impl.exploit.PacketControl;
import dev.luminous.mod.modules.impl.movement.NoSlow;
import dev.luminous.mod.modules.impl.movement.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

	public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "pushOutOfBlocks",
			at = @At("HEAD"),
			cancellable = true)
	private void onPushOutOfBlocksHook(double x, double d, CallbackInfo info) {
		if (Velocity.INSTANCE.isOn() && Velocity.INSTANCE.blockPush.getValue()) {
			info.cancel();
		}
	}

	@Redirect(method = "tickMovement",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"),
			require = 0)
	private boolean tickMovementHook(ClientPlayerEntity player) {
		if (NoSlow.INSTANCE.noSlow()) {
			return false;
		}
		return player.isUsingItem();
	}

	@Shadow
	public float prevNauseaIntensity;
	@Shadow
	public float nauseaIntensity;

	@Inject(at = {@At("HEAD")}, method = {"updateNausea"}, cancellable = true)
	private void updateNausea(CallbackInfo ci) {
		ci.cancel();
		this.prevNauseaIntensity = this.nauseaIntensity;
		float f = 0.0F;
		if (this.inNetherPortal) {
			if (!ClientSetting.INSTANCE.portalGui() && this.client.currentScreen != null && !this.client.currentScreen.shouldPause() && !(this.client.currentScreen instanceof DeathScreen)) {
				if (this.client.currentScreen instanceof HandledScreen) {
					this.closeHandledScreen();
				}

				this.client.setScreen(null);
			}

			if (this.nauseaIntensity == 0.0F) {
				this.client.getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_PORTAL_TRIGGER, this.random.nextFloat() * 0.4F + 0.8F, 0.25F));
			}

			f = 0.0125F;
			this.inNetherPortal = false;
		} else if (this.hasStatusEffect(StatusEffects.NAUSEA) && !this.getStatusEffect(StatusEffects.NAUSEA).isDurationBelow(60)) {
			f = 0.006666667F;
		} else if (this.nauseaIntensity > 0.0F) {
			f = -0.05F;
		}

		this.nauseaIntensity = MathHelper.clamp(this.nauseaIntensity + f, 0.0F, 1.0F);
		this.tickPortalCooldown();
	}

	@Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
	public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
		MoveEvent event = new MoveEvent(movement.x, movement.y, movement.z);
		Alien.EVENT_BUS.post(event);
		ci.cancel();
		if (!event.isCancelled()) {
			super.move(movementType, new Vec3d(event.getX(), event.getY(), event.getZ()));
		}
	}

	@Shadow
	private void sendSprintingPacket() {
	}

	@Shadow
	@Final
	private List<ClientPlayerTickable> tickables;
	@Shadow
	public Input input;
	@Shadow
	private boolean autoJumpEnabled;
	@Final
	@Shadow
	public ClientPlayNetworkHandler networkHandler;
	@Shadow
	private double lastX;
	@Shadow
	private double lastBaseY;
	@Shadow
	private double lastZ;
	@Shadow
	private float lastYaw;
	@Shadow
	private float lastPitch;
	@Shadow
	private boolean lastOnGround;
	@Shadow
	private boolean lastSneaking;
	@Final
	@Shadow
	protected MinecraftClient client;
	@Shadow
	private int ticksSinceLastPositionPacketSent;

	@Shadow
	private void sendMovementPackets() {
	}

	@Shadow
	protected boolean isCamera() {
		return false;
	}

	@Shadow
	public abstract float getPitch(float tickDelta);

	@Inject(method = "sendMovementPackets", at = {@At("HEAD")}, cancellable = true)
	private void sendMovementPacketsHook(CallbackInfo ci) {
		ci.cancel();
		try {
			UpdateWalkingPlayerEvent updateEvent = new UpdateWalkingPlayerEvent(Event.Stage.Pre);
			Alien.EVENT_BUS.post(updateEvent);
			this.sendSprintingPacket();
			boolean bl = this.isSneaking();
			if (bl != this.lastSneaking) {
				ClientCommandC2SPacket.Mode mode = bl ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
				this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode));
				this.lastSneaking = bl;
			}

			if (this.isCamera()) {
				double d = this.getX() - this.lastX;
				double e = this.getY() - this.lastBaseY;
				double f = this.getZ() - this.lastZ;

				float yaw = this.getYaw();
				float pitch = this.getPitch();
				MovementPacketsEvent movementPacketsEvent = new MovementPacketsEvent(yaw, pitch);
				Alien.EVENT_BUS.post(movementPacketsEvent);
				yaw = movementPacketsEvent.getYaw();
				pitch = movementPacketsEvent.getPitch();
				Alien.ROTATION.rotationYaw = yaw;
				Alien.ROTATION.rotationPitch = pitch;

				double g = yaw - Alien.ROTATION.lastYaw;//this.lastYaw;
				double h = pitch - Alien.ROTATION.lastPitch;//this.lastPitch;
				++this.ticksSinceLastPositionPacketSent;
				boolean bl2 = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20 || (PacketControl.INSTANCE.isOn() && PacketControl.INSTANCE.position.getValue() && PacketControl.INSTANCE.positionT.passed(PacketControl.INSTANCE.positionDelay.getValueInt()));
				boolean bl3 = (g != 0.0 || h != 0.0 || (PacketControl.INSTANCE.isOn() && PacketControl.INSTANCE.rotate.getValue() && PacketControl.INSTANCE.rotationT.passed(PacketControl.INSTANCE.rotationDelay.getValueInt())));
				if (PacketControl.INSTANCE.isOn() && PacketControl.INSTANCE.timerBypass.getValue()) {
					bl3 = PacketControl.INSTANCE.full;
				}
				if (this.hasVehicle()) {
					Vec3d vec3d = this.getVelocity();
					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(vec3d.x, -999.0, vec3d.z, yaw, pitch, this.isOnGround()));
					bl2 = false;
				} else if (bl2 && bl3) {
					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(this.getX(), this.getY(), this.getZ(), yaw, pitch, this.isOnGround()));
				} else if (bl2) {
					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.getX(), this.getY(), this.getZ(), this.isOnGround()));
				} else if (bl3) {
					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, this.isOnGround()));
				} else if (this.lastOnGround != this.isOnGround() || PacketControl.INSTANCE.isOn() && PacketControl.INSTANCE.onGround.getValue() && PacketControl.INSTANCE.groundT.passed(PacketControl.INSTANCE.groundDelay.getValueInt())) {
					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(this.isOnGround()));
				}

				if (bl2) {
					this.lastX = this.getX();
					this.lastBaseY = this.getY();
					this.lastZ = this.getZ();
					this.ticksSinceLastPositionPacketSent = 0;
				}

				if (bl3) {
					this.lastYaw = yaw;
					this.lastPitch = pitch;
				}

				this.lastOnGround = this.isOnGround();
				this.autoJumpEnabled = this.client.options.getAutoJump().getValue();
			}
			Alien.EVENT_BUS.post(new UpdateWalkingPlayerEvent(Event.Stage.Post));
		} catch (Exception e) {
			e.printStackTrace();
			if (ClientSetting.INSTANCE.debug.getValue())
				CommandManager.sendChatMessage("§4[!] [SendMovePackets] An error has occurred:\n" + e);
		}
	}

	@Inject(method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasVehicle()Z",
					shift = At.Shift.AFTER
			),
			cancellable = true)
	private void tickHook(CallbackInfo ci) {
		try {
			if (this.hasVehicle()) {
				UpdateWalkingPlayerEvent updateEvent = new UpdateWalkingPlayerEvent(Event.Stage.Pre);
				Alien.EVENT_BUS.post(updateEvent);
				float yaw = this.getYaw();
				float pitch = this.getPitch();
				MovementPacketsEvent movementPacketsEvent = new MovementPacketsEvent(yaw, pitch);
				Alien.EVENT_BUS.post(movementPacketsEvent);
				yaw = movementPacketsEvent.getYaw();
				pitch = movementPacketsEvent.getPitch();
				Alien.ROTATION.rotationYaw = yaw;
				Alien.ROTATION.rotationPitch = pitch;

				this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, this.isOnGround()));
				Alien.EVENT_BUS.post(new UpdateWalkingPlayerEvent(Event.Stage.Post));
				this.networkHandler.sendPacket(new PlayerInputC2SPacket(this.sidewaysSpeed, this.forwardSpeed, this.input.jumping, this.input.sneaking));
				Entity entity = this.getRootVehicle();
				if (entity != this && entity.isLogicalSideForUpdatingMovement()) {
					this.networkHandler.sendPacket(new VehicleMoveC2SPacket(entity));
					this.sendSprintingPacket();
				}
			} else {
				this.sendMovementPackets();
			}

			for (ClientPlayerTickable clientPlayerTickable : this.tickables) {
				clientPlayerTickable.tick();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (ClientSetting.INSTANCE.debug.getValue())
				CommandManager.sendChatMessage("§4[!] [UpdateWalkingPlayer] An error has occurred:\n" + e);
		}
		ci.cancel();
	}
}