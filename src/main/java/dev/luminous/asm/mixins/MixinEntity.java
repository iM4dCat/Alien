package dev.luminous.asm.mixins;

import dev.luminous.Alien;
import dev.luminous.api.events.impl.UpdateVelocityEvent;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.movement.Velocity;
import dev.luminous.mod.modules.impl.player.freelook.CameraState;
import dev.luminous.mod.modules.impl.player.freelook.FreeLook;
import dev.luminous.mod.modules.impl.render.NoRender;
import dev.luminous.mod.modules.impl.render.Shader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static dev.luminous.api.utils.Wrapper.mc;
@Mixin(Entity.class)
public abstract class MixinEntity {

	@Inject(at = {@At("HEAD")}, method = "isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", cancellable = true)
	private void onIsInvisibleCheck(PlayerEntity message, CallbackInfoReturnable<Boolean> cir) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.invisible.getValue()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "updateVelocity", at = {@At("HEAD")}, cancellable = true)
	public void updateVelocityHook(float speed, Vec3d movementInput, CallbackInfo ci) {
		if(Module.nullCheck()) return;
		if ((Object) this == mc.player) {
			UpdateVelocityEvent event = new UpdateVelocityEvent(movementInput, speed, mc.player.getYaw(), movementInputToVelocity(movementInput, speed, mc.player.getYaw()));
			Alien.EVENT_BUS.post(event);
			if (event.isCancelled()) {
				ci.cancel();
				mc.player.setVelocity(mc.player.getVelocity().add(event.getVelocity()));
			}
		}
	}

	@Shadow
	private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
		double d = movementInput.lengthSquared();
		if (d < 1.0E-7) {
			return Vec3d.ZERO;
		} else {
			Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply((double) speed);
			float f = MathHelper.sin(yaw * 0.017453292F);
			float g = MathHelper.cos(yaw * 0.017453292F);
			return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
		}
	}

	@Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
	void isGlowingHook(CallbackInfoReturnable<Boolean> cir) {
		if (Shader.INSTANCE.isOn()) {
			cir.setReturnValue(Shader.INSTANCE.shouldRender((Entity) (Object) this));
		}
	}

	@ModifyArgs(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
	private void pushAwayFromHook(Args args) {
		if ((Entity) (Object) this == MinecraftClient.getInstance().player) {
			if (Velocity.INSTANCE.isOn() && Velocity.INSTANCE.entityPush.getValue()) {
				args.set(0, 0d);
				args.set(1, 0d);
				args.set(2, 0d);
			}
		}
	}

	@Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true)
	void isOnFireHook(CallbackInfoReturnable<Boolean> cir) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.fireEntity.getValue()) {
			cir.setReturnValue(false);
		}
	}

	@Unique
	private CameraState camera;

	@Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
	private void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo callback) {
		if ((Entity) (Object) this instanceof ClientPlayerEntity) {
			camera = FreeLook.INSTANCE.getCameraState();

			if (camera.doLock) {
				applyTransformedAngle(cursorDeltaX, cursorDeltaY);
				callback.cancel();
			} else if (camera.doTransition) {
				applyTransformedAngle(cursorDeltaX, cursorDeltaY);
			}
		}
	}

	@Unique
	private void applyTransformedAngle(double cursorDeltaX, double cursorDeltaY) {
		var cursorDeltaMultiplier = 0.15f;
		var transformedCursorDeltaX = (float) cursorDeltaX * cursorDeltaMultiplier;
		var transformedCursorDeltaY = (float) cursorDeltaY * cursorDeltaMultiplier;

		var yaw = camera.lookYaw;
		var pitch = camera.lookPitch;

		yaw += transformedCursorDeltaX;
		pitch += transformedCursorDeltaY;
		pitch = MathHelper.clamp(pitch, -90, 90);

		camera.lookYaw = yaw;
		camera.lookPitch = pitch;
	}
}
