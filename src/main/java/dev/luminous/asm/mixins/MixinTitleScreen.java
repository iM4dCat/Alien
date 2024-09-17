package dev.luminous.asm.mixins;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ias.screen.AccountScreen;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {


    public MixinTitleScreen(Text title) {
        super(title);
    }

    @Inject(method = "initWidgetsNormal", at = @At(
            target = "Lnet/minecraft/client/gui/screen/TitleScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;",
            value = "INVOKE", shift = At.Shift.AFTER, ordinal = 1), cancellable = true)
    public void hookInit(int y, int spacingY, CallbackInfo ci) {
        ci.cancel();
        final ButtonWidget widget = ButtonWidget.builder(Text.of("Account Manager"), (action) -> client.setScreen(new AccountScreen(this)))
                .dimensions(this.width / 2 + 2, y + spacingY * 2, 98, 20)
                .tooltip(Tooltip.of(Text.of("Allows you to switch your in-game account")))
                .build();
        widget.active = true;
        addDrawableChild(widget);
        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("menu.online"), button -> this.switchToRealms())
                        .dimensions(this.width / 2 - 100, y + spacingY * 2, 98, 20)
                        .build()
        ).active = true;
    }

    @Shadow
    private void switchToRealms() {
    }
}
