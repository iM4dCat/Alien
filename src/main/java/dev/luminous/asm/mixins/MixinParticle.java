package dev.luminous.asm.mixins;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Particle.class)
public abstract class MixinParticle {

    @Shadow
    protected double velocityX;
    @Shadow
    protected double velocityY;
    @Shadow
    protected double velocityZ;
    @Shadow
    public abstract void setColor(float red, float green, float blue);

    @Shadow
    protected void setAlpha(float alpha) {
    }
}