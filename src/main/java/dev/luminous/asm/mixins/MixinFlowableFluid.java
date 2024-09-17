package dev.luminous.asm.mixins;


import dev.luminous.mod.modules.impl.movement.Velocity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(FlowableFluid.class)
public class MixinFlowableFluid {

    @Redirect(method = "getVelocity", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
    private boolean getVelocity_hasNext(Iterator<Direction> var9) {
        if (Velocity.INSTANCE.isOn() && Velocity.INSTANCE.waterPush.getValue()) {
            return false;
        }
        return var9.hasNext();
    }

}