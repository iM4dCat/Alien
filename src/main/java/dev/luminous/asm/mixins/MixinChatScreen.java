package dev.luminous.asm.mixins;

import dev.luminous.mod.modules.impl.client.ClientSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
    @Unique private boolean wasOpenedLastFrame = false;
    @Unique private long lastOpenTime = 0;
    @Unique private float offsetY = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ClientSetting.INSTANCE.inputBoxAnim.getValue()) {
            offsetY = 0;
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (!wasOpenedLastFrame && !client.player.isSleeping()) {
                wasOpenedLastFrame = true;
                lastOpenTime = System.currentTimeMillis();
            }
        }

        float FADE_TIME = 170;
        float FADE_OFFSET = 8;
        float screenFactor = (float)client.getWindow().getHeight() / 1080;
        float timeSinceOpen = Math.min((float)(System.currentTimeMillis() - lastOpenTime), FADE_TIME);
        float alpha = 1 - (timeSinceOpen/FADE_TIME);

        float c1 = 1.70158f;
        float c3 = c1 + 1;
        float modifiedAlpha = c3 * alpha * alpha * alpha - c1 * alpha * alpha;

        offsetY = modifiedAlpha * FADE_OFFSET * screenFactor;

        context.getMatrices().translate(0, offsetY, 0);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderEnd(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().translate(0, -offsetY, 0);
    }
}