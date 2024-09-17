package dev.luminous.asm.mixins;

import dev.luminous.mod.modules.impl.render.Ambience;
import net.minecraft.client.render.DimensionEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(DimensionEffects.class)
public class MixinDimensionEffects {

    @Inject(method = "getFogColorOverride", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetFogColorOverride(float skyAngle, float tickDelta,
                                         CallbackInfoReturnable<float[]> cir) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.dimensionColor.booleanValue) {
            Color color = Ambience.INSTANCE.dimensionColor.getValue();
            cir.setReturnValue(new float[]
                    {
                            (float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f,
                            (float) color.getBlue() / 255.0f, 1.0f
                    });
        }
    }
}
