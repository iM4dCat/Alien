package dev.luminous.asm.mixins;

import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.render.Ambience;
import dev.luminous.mod.modules.impl.render.XRay;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class MixinAbstractBlockState {
    @Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
    public void getLuminanceHook(CallbackInfoReturnable<Integer> cir) {
        if (Module.nullCheck()) return;
        if (XRay.INSTANCE.isOn()) {
            cir.setReturnValue(15);
        } else if (Ambience.INSTANCE.customLuminance.getValue()) {
            cir.setReturnValue(Ambience.INSTANCE.luminance.getValueInt());
        }
    }
}