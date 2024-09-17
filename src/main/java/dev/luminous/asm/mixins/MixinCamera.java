package dev.luminous.asm.mixins;

import dev.luminous.mod.modules.impl.player.Freecam;
import dev.luminous.mod.modules.impl.player.freelook.FreeLook;
import dev.luminous.mod.modules.impl.render.CameraClip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow
    protected abstract double clipToSpace(double desiredCameraDistance);

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(DDD)V", ordinal = 0))
    private void modifyCameraDistance(Args args) {
        if (CameraClip.INSTANCE.isOn()) {
            args.set(0, -clipToSpace(CameraClip.INSTANCE.getDistance()));
        }
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(double desiredCameraDistance, CallbackInfoReturnable<Double> info) {
        if (CameraClip.INSTANCE.isOn()) {
            info.setReturnValue(CameraClip.INSTANCE.getDistance());
        }
    }

    @Shadow
    private boolean thirdPerson;


    @Inject(method = "update", at = @At("TAIL"))
    private void updateHook(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (Freecam.INSTANCE.isOn()) {
            this.thirdPerson = true;
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void setRotationHook(Args args) {
        if(Freecam.INSTANCE.isOn())
            args.setAll(Freecam.INSTANCE.getFakeYaw(), Freecam.INSTANCE.getFakePitch());
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void setPosHook(Args args) {
        if(Freecam.INSTANCE.isOn())
            args.setAll(Freecam.INSTANCE.getFakeX(), Freecam.INSTANCE.getFakeY(), Freecam.INSTANCE.getFakeZ());
    }

    @Unique
    private float lastUpdate;

    @Inject(method = "update", at = @At("HEAD"))
    private void onCameraUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        var camera = FreeLook.INSTANCE.getCameraState();

        if (camera.doLock) {
            camera.lookYaw = MathHelper.wrapDegrees(camera.lookYaw);
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void modifyRotationArgs(Args args) {
        var camera = FreeLook.INSTANCE.getCameraState();

        if (camera.doLock) {
            var yaw = camera.lookYaw;
            var pitch = camera.lookPitch;

            if (MinecraftClient.getInstance().options.getPerspective().isFrontView()) {
                yaw -= 180;
                pitch = -pitch;
            }

            args.set(0, yaw);
            args.set(1, pitch);
        } else if (camera.doTransition) {
            var delta = (getCurrentTime() - lastUpdate);

            var steps = 1.2f;
            var speed = 2f;
            var yawDiff = camera.lookYaw - camera.originalYaw();
            var pitchDiff = camera.lookPitch - camera.originalPitch();
            var yawStep = speed * (yawDiff * steps);
            var pitchStep = speed * (pitchDiff * steps);
            var yaw = MathHelper.stepTowards(camera.lookYaw, camera.originalYaw(), yawStep * delta);
            var pitch = MathHelper.stepTowards(camera.lookPitch, camera.originalPitch(), pitchStep * delta);

            camera.lookYaw = yaw;
            camera.lookPitch = pitch;

            args.set(0, yaw);
            args.set(1, pitch);

            camera.doTransition =
                    (int) camera.originalYaw() != (int) camera.lookYaw ||
                            (int) camera.originalPitch() != (int) camera.lookPitch;
        }

        lastUpdate = getCurrentTime();
    }

    @Unique
    private float getCurrentTime() {
        return (float) (System.nanoTime() * 0.00000001);
    }
}
