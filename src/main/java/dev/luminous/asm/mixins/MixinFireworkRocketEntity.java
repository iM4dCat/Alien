package dev.luminous.asm.mixins;

import dev.luminous.Alien;
import dev.luminous.api.events.impl.RemoveFireworkEvent;
import dev.luminous.api.utils.Wrapper;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(FireworkRocketEntity.class)
public class MixinFireworkRocketEntity implements Wrapper {

    @Shadow
    private int life;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/" +
            "FireworkRocketEntity;updateRotation()V", shift = At.Shift.AFTER), cancellable = true)
    private void hookTickPre(CallbackInfo ci) {
        FireworkRocketEntity rocketEntity = ((FireworkRocketEntity) (Object) this);
        RemoveFireworkEvent removeFireworkEvent = new RemoveFireworkEvent(rocketEntity);
        Alien.EVENT_BUS.post(removeFireworkEvent);
        if (removeFireworkEvent.isCancelled()) {
            ci.cancel();
            if (life == 0 && !rocketEntity.isSilent()) {
                mc.world.playSound(null, rocketEntity.getX(), rocketEntity.getY(), rocketEntity.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0f, 1.0f);
            }
            ++life;
            if (mc.world.isClient && life % 2 < 2) {
                mc.world.addParticle(ParticleTypes.FIREWORK, rocketEntity.getX(), rocketEntity.getY(), rocketEntity.getZ(), mc.world.random.nextGaussian() * 0.05, -rocketEntity.getVelocity().y * 0.5, mc.world.random.nextGaussian() * 0.05);
            }
        }
    }
}
