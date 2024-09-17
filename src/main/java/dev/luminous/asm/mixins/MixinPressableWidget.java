package dev.luminous.asm.mixins;

import dev.luminous.api.utils.math.Animation;
import dev.luminous.api.utils.math.Easing;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.api.utils.render.Render2DUtil;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PressableWidget.class)
public abstract class MixinPressableWidget extends ClickableWidget {
    public MixinPressableWidget(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Unique
    Animation animation = new Animation();
    @Unique
    double progress = 0;
    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (ClientSetting.INSTANCE.customButton.booleanValue) {
            ci.cancel();
            if (this.isSelected()) {
                progress = animation.get(1, (long) ClientSetting.INSTANCE.speed.getValue(), Easing.Linear);
            } else {
                progress = animation.get(0, (long) ClientSetting.INSTANCE.speed.getValue(), Easing.Linear);
            }
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            Render2DUtil.drawRect(context.getMatrices(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ColorUtil.fadeColor(ClientSetting.INSTANCE.customButton.getValue(), ClientSetting.INSTANCE.hover.getValue(), progress));
            int i = this.active ? 16777215 : 10526880;
            this.drawMessage(context, minecraftClient.textRenderer, i | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }

    @Shadow
    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
    }
}
