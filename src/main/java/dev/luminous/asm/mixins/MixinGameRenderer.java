package dev.luminous.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.Alien;
import dev.luminous.api.utils.render.TextUtil;
import dev.luminous.api.utils.world.InteractUtil;
import dev.luminous.mod.modules.impl.player.Freecam;
import dev.luminous.mod.modules.impl.player.InteractTweaks;
import dev.luminous.mod.modules.impl.player.freelook.CameraState;
import dev.luminous.mod.modules.impl.player.freelook.FreeLook;
import dev.luminous.mod.modules.impl.render.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow
    @Final
    MinecraftClient client;
    @Shadow
    private float fovMultiplier;
    @Shadow
    private float lastFovMultiplier;
    @Shadow
    private boolean renderingPanorama;
    @Shadow
    private float zoom;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;
    @Shadow
    private float viewDistance;
    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (floatingItem.getItem() == Items.TOTEM_OF_UNDYING && NoRender.INSTANCE.isOn() && NoRender.INSTANCE.totem.getValue()) {
            info.cancel();
        }
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float applyCameraTransformationsMathHelperLerpProxy(float delta, float first, float second) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.nausea.getValue()) return 0;
        return MathHelper.lerp(delta, first, second);
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurtHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.hurtCam.getValue()) {
            ci.cancel();
        }
    }
    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    public void hookOutline(CallbackInfoReturnable<Boolean> cir) {
        if (HighLight.INSTANCE.isOn()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void render3dHook(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        TextUtil.lastProjMat.set(RenderSystem.getProjectionMatrix());
        TextUtil.lastModMat.set(RenderSystem.getModelViewMatrix());
        TextUtil.lastWorldSpaceMatrix.set(matrix.peek().getPositionMatrix());
        Alien.FPS.record();
        Alien.MODULE.render3D(matrix);
    }

    @Inject(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D", at = @At("HEAD"), cancellable = true)
    public void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> ci) {
        if (!this.renderingPanorama && (CustomFov.INSTANCE.isOn() || Zoom.INSTANCE.isOn())) {
            double d = 70.0;
            if (changingFov) {
                if (CustomFov.INSTANCE.isOn()) {
                    double fov = CustomFov.INSTANCE.fov.getValue();

                    if (Zoom.on) {
                        ci.setReturnValue(Math.min(Math.max(fov - Zoom.INSTANCE.currentFov, 1), 177));
                    } else {
                        ci.setReturnValue(fov);
                    }
                    return;
                }
                d = this.client.options.getFov().getValue();
                d *= MathHelper.lerp(tickDelta, this.lastFovMultiplier, this.fovMultiplier);
                if (Zoom.on) {
                    d = (Math.min(Math.max(d - Zoom.INSTANCE.currentFov, 1), 177));
                }
            } else {
                if (CustomFov.INSTANCE.isOn()) {
                    ci.setReturnValue(CustomFov.INSTANCE.itemFov.getValue());
                    return;
                }
            }

            if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity)camera.getFocusedEntity()).isDead()) {
                float f = Math.min((float)((LivingEntity)camera.getFocusedEntity()).deathTime + tickDelta, 20.0F);
                d /= (1.0F - 500.0F / (f + 500.0F)) * 2.0F + 1.0F;
            }

            CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
            if (cameraSubmersionType == CameraSubmersionType.LAVA || cameraSubmersionType == CameraSubmersionType.WATER) {
                d *= MathHelper.lerp(this.client.options.getFovEffectScale().getValue(), 1.0, 0.85714287F);
            }

            ci.setReturnValue(d);
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V", shift = At.Shift.AFTER))
    public void postRender3dHook(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        Alien.SHADER.renderShaders();
    }

    @Inject(method = "getBasicProjectionMatrix",at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if(AspectRatio.INSTANCE.isOn()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float)(fov * 0.01745329238474369), AspectRatio.INSTANCE.ratio.getValueFloat(), 0.05f, viewDistance * 4.0f));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }

    @Inject(method = "updateTargetedEntity", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityHook(float tickDelta, CallbackInfo ci) {
        ci.cancel();
        update(tickDelta);
    }

    @Unique
    public void update(float tickDelta) {
        Entity entity = this.client.getCameraEntity();
        if (entity != null) {
            if (this.client.world != null) {
                this.client.getProfiler().push("pick");
                this.client.targetedEntity = null;
                double d = this.client.interactionManager.getReachDistance();
                InteractTweaks.INSTANCE.isActive = InteractTweaks.INSTANCE.ghostHand();
                this.client.crosshairTarget = entity.raycast(d, tickDelta, false);
                InteractTweaks.INSTANCE.isActive = false;
                Vec3d vec3d = entity.getCameraPosVec(tickDelta);
                boolean bl = false;
                double e = d;
                if (this.client.interactionManager.hasExtendedReach()) {
                    e = 6.0;
                    d = e;
                } else {
                    if (d > 3.0) {
                        bl = true;
                    }
                }

                e *= e;
                if (this.client.crosshairTarget != null) {
                    e = this.client.crosshairTarget.getPos().squaredDistanceTo(vec3d);
                }

                Vec3d vec3d2 = entity.getRotationVec(1.0F);
                Vec3d vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
                Box box = entity.getBoundingBox().stretch(vec3d2.multiply(d)).expand(1.0, 1.0, 1.0);
                if (Freecam.INSTANCE.isOn()) {
                    client.crosshairTarget = InteractUtil.getRtxTarget(Freecam.INSTANCE.getFakeYaw(), Freecam.INSTANCE.getFakePitch(), Freecam.INSTANCE.getFakeX(), Freecam.INSTANCE.getFakeY(), Freecam.INSTANCE.getFakeZ());
                    client.getProfiler().pop();
                    return;
                }
                if (!InteractTweaks.INSTANCE.noEntityTrace()) {
                    EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, (entityx) -> !entityx.isSpectator() && entityx.canHit(), e);
                    if (entityHitResult != null) {
                        Entity entity2 = entityHitResult.getEntity();
                        Vec3d vec3d4 = entityHitResult.getPos();
                        double g = vec3d.squaredDistanceTo(vec3d4);
                        if (bl && g > 9.0) {
                            this.client.crosshairTarget = BlockHitResult.createMissed(vec3d4, Direction.getFacing(vec3d2.x, vec3d2.y, vec3d2.z), BlockPos.ofFloored(vec3d4));
                        } else if (g < e || this.client.crosshairTarget == null) {
                            this.client.crosshairTarget = entityHitResult;
                            if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrameEntity) {
                                this.client.targetedEntity = entity2;
                            }
                        }
                    }
                }

                this.client.getProfiler().pop();
            }
        }
    }

    @Unique
    private CameraState camera;

    @Unique
    private Entity cameraEntity;
    @Unique
    private float originalYaw;
    @Unique
    private float originalPitch;

    @Inject(method = "renderHand", at = @At("HEAD"))
    private void onRenderHandBegin(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        this.camera = FreeLook.INSTANCE.getCameraState();

        if (this.camera.doTransition || this.camera.doLock) {
            cameraEntity = MinecraftClient.getInstance().getCameraEntity();
            originalYaw = cameraEntity.getYaw();
            originalPitch = cameraEntity.getPitch();

            var pitch = this.camera.lookPitch;

            pitch -= MathHelper.abs(this.camera.lookYaw - this.camera.originalYaw());

            cameraEntity.setYaw(this.camera.lookYaw);
            cameraEntity.setPitch(pitch);
        }
    }

    @Inject(method = "renderHand", at = @At("RETURN"))
    private void onRenderHandEnd(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        if (this.camera.doTransition || this.camera.doLock) {
            cameraEntity.setYaw(originalYaw);
            cameraEntity.setPitch(originalPitch);
        }
    }
}
