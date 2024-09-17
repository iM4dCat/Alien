package dev.luminous.asm.mixins;

import dev.luminous.core.impl.GuiManager;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen {
    @Shadow public int width;
    @Shadow public int height;

    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    public void renderInGameBackgroundHook(DrawContext context, CallbackInfo ci) {
        ci.cancel();
        if (ClientSetting.INSTANCE.guiBackground.getValue()) {
            context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        }
        if (ClientSetting.INSTANCE.customBackground.booleanValue) {
            context.fillGradient(0, 0, this.width, this.height, ClientSetting.INSTANCE.customBackground.getValue().getRGB(), ClientSetting.INSTANCE.endColor.getValue().getRGB());
        }
        if (ClientSetting.INSTANCE.snow.booleanValue) {
            GuiManager.snows.forEach(snow -> snow.drawSnow(context, ClientSetting.INSTANCE.snow.getValue()));
        }
    }
}
