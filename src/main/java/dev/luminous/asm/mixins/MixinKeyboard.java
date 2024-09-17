package dev.luminous.asm.mixins;

import dev.luminous.Alien;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.mod.modules.settings.impl.StringSetting;
import dev.luminous.api.utils.Wrapper;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Keyboard.class)
public class MixinKeyboard implements Wrapper {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (mc.currentScreen instanceof ClickGuiScreen && action == 1 && Alien.MODULE.setBind(key)) {
            return;
        }
        if (action == 1) {
            Alien.MODULE.onKeyPressed(key);
        }
        if (action == 0) {
            Alien.MODULE.onKeyReleased(key);
        }
    }

    @Shadow @Final private MinecraftClient client;
    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void onChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle()) {
            Element element = this.client.currentScreen;
            if (element != null && this.client.getOverlay() == null) {
                if (Character.charCount(codePoint) == 1) {
                    if (!Module.nullCheck() && Alien.GUI != null) {
                        if (Alien.GUI.isClickGuiOpen()) {
                            Alien.MODULE.modules.forEach(module -> module.getSettings().stream()
                                    .filter(setting -> setting instanceof StringSetting)
                                    .map(setting -> (StringSetting) setting)
                                    .filter(StringSetting::isListening)
                                    .forEach(setting -> setting.charType((char)codePoint)));
                            Alien.MODULE.modules.forEach(module -> module.getSettings().stream()
                                    .filter(setting -> setting instanceof SliderSetting)
                                    .map(setting -> (SliderSetting) setting)
                                    .filter(SliderSetting::isListening)
                                    .forEach(setting -> setting.charType((char)codePoint)));
                        }
                    }
                    Screen.wrapScreenError(() -> element.charTyped((char)codePoint, modifiers), "charTyped event handler", element.getClass().getCanonicalName());
                } else {
                    char[] var6 = Character.toChars(codePoint);

                    for (char c : var6) {
                        if (!Module.nullCheck() && Alien.GUI != null) {
                            if (Alien.GUI.isClickGuiOpen()) {
                                Alien.MODULE.modules.forEach(module -> module.getSettings().stream()
                                        .filter(setting -> setting instanceof StringSetting)
                                        .map(setting -> (StringSetting) setting)
                                        .filter(StringSetting::isListening)
                                        .forEach(setting -> setting.charType(c)));
                                Alien.MODULE.modules.forEach(module -> module.getSettings().stream()
                                        .filter(setting -> setting instanceof SliderSetting)
                                        .map(setting -> (SliderSetting) setting)
                                        .filter(SliderSetting::isListening)
                                        .forEach(setting -> setting.charType((char)codePoint)));
                            }
                        }
                        Screen.wrapScreenError(() -> element.charTyped(c, modifiers), "charTyped event handler", element.getClass().getCanonicalName());
                    }
                }
            }
        }
        ci.cancel();
    }
}
