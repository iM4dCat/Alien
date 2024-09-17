package dev.luminous.mod.modules.impl.movement;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.EntityVelocityUpdateEvent;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.asm.accessors.IEntityVelocityUpdateS2CPacket;
import dev.luminous.asm.accessors.IExplosionS2CPacket;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Direction;

public class Velocity extends Module {
	public static Velocity INSTANCE;
	private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Custom));
	private final SliderSetting horizontal = add(new SliderSetting("Horizontal", 0f, 0f, 100f, 1f, () -> mode.is(Mode.Custom)));
	private final SliderSetting vertical = add(new SliderSetting("Vertical", 0f, 0f, 100f, 1f, () -> mode.is(Mode.Custom)));
	public final BooleanSetting flagInWall = add(new BooleanSetting("FlagInWall", false, () -> mode.is(Mode.Grim) || mode.is(Mode.Wall)));
	public final BooleanSetting noExplosions = add(new BooleanSetting("NoExplosions", false));
	public final BooleanSetting pauseInLiquid = add(new BooleanSetting("PauseInLiquid", false));
	public final BooleanSetting waterPush = add(new BooleanSetting("NoWaterPush", false));
	public final BooleanSetting entityPush = add(new BooleanSetting("NoEntityPush", true));
	public final BooleanSetting blockPush = add(new BooleanSetting("NoBlockPush", true));
	public final BooleanSetting fishBob = add(new BooleanSetting("NoFishBob", true));

	public Velocity() {
		super("Velocity", Category.Movement);
		setChinese("反击退");
		INSTANCE = this;
	}

	@Override
	public String getInfo() {
		if (mode.is(Mode.Custom))
			return horizontal.getValueInt() + "%, " + vertical.getValueInt() + "%";
		return mode.getValue().name();
	}
	private final Timer lagBackTimer = new Timer();
	private boolean flag;

	@EventHandler
	public void onPacketEvent(PacketEvent.Receive event) {
		if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
			lagBackTimer.reset();
		}
	}
	@EventHandler
	public void onVelocity(EntityVelocityUpdateEvent event) {
		if (nullCheck()) return;
		if ((mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isInLava()) && pauseInLiquid.getValue())
			return;

		if (mode.is(Mode.Grim) || mode.is(Mode.Wall)) {
			if (!lagBackTimer.passed(100)) {
				return;
			}
			boolean insideBlock = Alien.PLAYER.insideBlock;
			if (mode.is(Mode.Wall) && !insideBlock) return;
			event.cancel();
			flag = true;
		}
	}
	@EventHandler
	public void onReceivePacket(PacketEvent.Receive event) {
		if (nullCheck()) return;
		if ((mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isInLava()) && pauseInLiquid.getValue())
			return;

		if (fishBob.getValue()) {
			if (event.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == 31 && packet.getEntity(mc.world) instanceof FishingBobberEntity fishHook) {
				if (fishHook.getHookedEntity() == mc.player) {
					event.setCancelled(true);
				}
			}
		}

		if (mode.is(Mode.Grim) || mode.is(Mode.Wall)) {
			if (!lagBackTimer.passed(100)) {
				return;
			}
			boolean insideBlock = Alien.PLAYER.insideBlock;
			if (mode.is(Mode.Wall) && !insideBlock) return;

            if (event.getPacket() instanceof ExplosionS2CPacket explosion) {
                ((IExplosionS2CPacket) explosion).setVelocityX(0);
                ((IExplosionS2CPacket) explosion).setVelocityY(0);
                ((IExplosionS2CPacket) explosion).setVelocityZ(0);
                flag = true;
            }
		} else {
			float h = horizontal.getValueFloat() / 100;
			float v = vertical.getValueFloat() / 100;
			if (event.getPacket() instanceof ExplosionS2CPacket) {
				IExplosionS2CPacket packet = event.getPacket();

				packet.setVelocityX(packet.getX() * h);
				packet.setVelocityY(packet.getY() * v);
				packet.setVelocityZ(packet.getZ() * h);

				if (noExplosions.getValue()) event.cancel();
				return;
			}

			if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
				if (packet.getId() == mc.player.getId()) {
					if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
						event.cancel();
					} else {
						((IEntityVelocityUpdateS2CPacket) packet).setX((int) (packet.getVelocityX() * h));
						((IEntityVelocityUpdateS2CPacket) packet).setY((int) (packet.getVelocityY() * v));
						((IEntityVelocityUpdateS2CPacket) packet).setZ((int) (packet.getVelocityZ() * h));
					}
				}
			}
		}
	}

	@Override
	public void onUpdate() {
		if (mc.player != null && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isInLava()) && pauseInLiquid.getValue())
			return;

		if (flag) {
			if (lagBackTimer.passed(100) && (flagInWall.getValue() || !Alien.PLAYER.insideBlock)) {
				Alien.ROTATION.snapBack();
				mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
						mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
				//mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, BlockPos.ofFloored(mc.player.getPos()), mc.player.getHorizontalFacing().getOpposite()));
			}
			flag = false;
		}
	}

	public enum Mode {
		Custom, Grim, Wall
	}
}