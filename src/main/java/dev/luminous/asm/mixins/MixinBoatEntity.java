package dev.luminous.asm.mixins;

import dev.luminous.Alien;
import dev.luminous.api.events.impl.BoatMoveEvent;
import dev.luminous.mod.modules.impl.movement.EntityControl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class MixinBoatEntity extends Entity {
    @Shadow
    private boolean pressingLeft;

    @Shadow
    private boolean pressingRight;

    public MixinBoatEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    private void onTickInvokeMove(CallbackInfo info) {
        BoatMoveEvent event = new BoatMoveEvent((BoatEntity) (Object) this);
        Alien.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Shadow
    private boolean pressingForward;
    @Shadow
    private boolean pressingBack;
    @Shadow
    private float yawVelocity;

    @Shadow
    public void setPaddleMovings(boolean leftMoving, boolean rightMoving) {
    }

    @Inject(at = { @At("HEAD") }, method = { "updatePaddles" }, cancellable = true)
    private void updatePaddlesHook(CallbackInfo info) {
        info.cancel();
        if (this.hasPassengers()) {
            float f = 0.0F;
            if (this.pressingLeft && !(EntityControl.INSTANCE.isOn() && EntityControl.INSTANCE.fly.getValue())) {
                --this.yawVelocity;
            }

            if (this.pressingRight && !(EntityControl.INSTANCE.isOn() && EntityControl.INSTANCE.fly.getValue())) {
                ++this.yawVelocity;
            }

            if (this.pressingRight != this.pressingLeft && !this.pressingForward && !this.pressingBack) {
                f += 0.005F;
            }

            this.setYaw(this.getYaw() + this.yawVelocity);
            if (this.pressingForward) {
                f += 0.04F;
            }

            if (this.pressingBack) {
                f -= 0.005F;
            }

            this.setVelocity(this.getVelocity().add((double) (MathHelper.sin(-this.getYaw() * 0.017453292F) * f), 0.0, (double) (MathHelper.cos(this.getYaw() * 0.017453292F) * f)));
            this.setPaddleMovings(this.pressingRight && !this.pressingLeft || this.pressingForward, this.pressingLeft && !this.pressingRight || this.pressingForward);
        }
    }
}
