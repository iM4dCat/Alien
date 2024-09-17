package dev.luminous.asm.accessors;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface IExplosionS2CPacket {

    @Mutable
    @Accessor("playerVelocityX")
    void setVelocityX(float velocityX);

    @Mutable
    @Accessor("playerVelocityY")
    void setVelocityY(float velocityY);

    @Mutable
    @Accessor("playerVelocityZ")
    void setVelocityZ(float velocityZ);

    @Accessor("playerVelocityX")
    float getX();

    @Accessor("playerVelocityY")
    float getY();

    @Accessor("playerVelocityZ")
    float getZ();
}