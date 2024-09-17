package dev.luminous.asm.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import dev.luminous.asm.accessors.IChatHud;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import dev.luminous.api.interfaces.IChatHudHook;
import dev.luminous.api.interfaces.IChatHudLine;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.api.utils.math.FadeUtils;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.api.utils.render.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(ChatHud.class)
public abstract class MixinChatHud implements IChatHudHook {
    @Final
    @Shadow
    private List<ChatHudLine.Visible> visibleMessages;
    @Shadow
    @Final
    private List<ChatHudLine> messages;
    @Unique
    private int nextMessageId = 0;
    @Shadow
    public abstract void addMessage(Text message);

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(MinecraftClient client, CallbackInfo ci) {
        ((IChatHud) this).setMessages(new CopyOnWriteArrayList<>());
        ((IChatHud) this).setVisibleMessages(new CopyOnWriteArrayList<>());
    }
    @Override
    public void addMessage(Text message, int id) {
        nextMessageId = id;
        addMessage(message);
        nextMessageId = 0;
    }
    
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void onAddMessageAfterNewChatHudLineVisible(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo info) {
        ((IChatHudLine) (Object) visibleMessages.get(0)).setMessageId(nextMessageId);
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void onAddMessageAfterNewChatHudLine(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo info) {
        ((IChatHudLine) (Object) messages.get(0)).setMessageId(nextMessageId);
    }
    
    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V")
    private void onAddMessage(Text message, @Nullable MessageSignatureData signature, int ticks, @Nullable MessageIndicator indicator, boolean refresh, CallbackInfo info) {
       if (nextMessageId != 0) {
           visibleMessages.removeIf(msg -> msg == null || ((IChatHudLine) (Object) msg).getMessageId() == nextMessageId);
           messages.removeIf(msg -> msg == null || ((IChatHudLine) (Object) msg).getMessageId() == nextMessageId);
       }
    }

    @Redirect(method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
            at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 2, remap = false))
    public int chatLinesSize(List<ChatHudLine.Visible> list) {
        return ClientSetting.INSTANCE.isOn() && ClientSetting.INSTANCE.infiniteChat.getValue() ? -2147483647 : list.size();
    }

    @Redirect(method = {"render"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
    private int drawStringWithShadow(DrawContext drawContext, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
        if (ClientSetting.chatMessage.containsKey(text) && ClientSetting.chatMessage.get(text).getString().startsWith(CommandManager.syncCode)) {
            if (ClientSetting.INSTANCE.pulse.booleanValue) {
                return TextUtil.drawStringPulse(drawContext, text, x, y, ColorUtil.injectAlpha(ClientSetting.INSTANCE.color.getValue(), ((color >> 24) & 0xff)),ColorUtil.injectAlpha(ClientSetting.INSTANCE.pulse.getValue(), ((color >> 24) & 0xff)), ClientSetting.INSTANCE.pulseSpeed.getValue(), ClientSetting.INSTANCE.pulseCounter.getValueInt());
            }
            return drawContext.drawTextWithShadow(textRenderer, text, x, y, ColorUtil.injectAlpha(ClientSetting.INSTANCE.color.getValue(), ((color >> 24) & 0xff)).getRGB());
        }
        return drawContext.drawTextWithShadow(textRenderer, text, x, y, color);
    }
    @Unique
    private final HashMap<ChatHudLine.Visible, FadeUtils> map = new HashMap<>();
    @Unique
    private ChatHudLine.Visible last;

    @ModifyArg(method = {"render"}, at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0, remap = false))
    private int get(int i) {
        last = visibleMessages.get(i);
        if (last != null && !map.containsKey(last)) {
            map.put(last, new FadeUtils(ClientSetting.INSTANCE.animateTime.getValueInt()));
        }
        return i;
    }

    @Inject(method = {"render"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I", ordinal = 0, shift = At.Shift.BEFORE)})
    private void translate(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (map.containsKey(last)) {
            context.getMatrices().translate(ClientSetting.INSTANCE.animateOffset.getValue() * (1 - map.get(last).ease(ClientSetting.INSTANCE.ease.getValue())), 0.0, 0.0f);
        }
    }

    @Shadow private int scrolledLines;
    @Shadow private int getLineHeight() { return 0; }
    @Shadow public int getWidth() { return 0; }

    @Unique private final ArrayList<FadeUtils> messageTimestamps = new ArrayList<>();
    @Unique private float fadeTime = 150;

    @Unique private int chatLineIndex;
    @Unique private int chatDisplacementY = 0;

    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I"
    ))
    public void getChatLineIndex(CallbackInfo ci, @Local(ordinal = 13) int chatLineIndex) {
        // Capture which chat line is currently being rendered
        this.chatLineIndex = chatLineIndex;
    }

    @Unique
    private void calculateYOffset() {
        // Calculate current required offset to achieve slide in from bottom effect
        try {
            int lineHeight = this.getLineHeight();
            float maxDisplacement = (float)lineHeight;// * fadeOffsetYScale;
            double quad = messageTimestamps.get(chatLineIndex).ease(FadeUtils.Ease.In2);
            if (chatLineIndex == 0 && quad < 1 && this.scrolledLines == 0) {
                chatDisplacementY = (int)(maxDisplacement - quad *maxDisplacement);
            }
        } catch (Exception ignored) {}
    }

    @ModifyArg(method = "render", index = 1, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
            ordinal = 1
    ))
    private float applyYOffset(float y) {
        fadeTime = ClientSetting.INSTANCE.fadeTime.getValueFloat();
        if (ClientSetting.INSTANCE.yAnim.getValue()) {
            calculateYOffset();
            return y + chatDisplacementY;
        } else {
            return y;
        }
    }

    @ModifyVariable(method = "render", ordinal = 3, at = @At(
            value = "STORE"
    ))
    private double modifyOpacity(double originalOpacity) {
        double opacity = originalOpacity;
        if (ClientSetting.INSTANCE.fade.getValue()) {
            try {
                double quad = messageTimestamps.get(chatLineIndex).ease(ClientSetting.INSTANCE.ease.getValue());
                if (quad < 1 && this.scrolledLines == 0) {
                    opacity = opacity * (0.5 + MathHelper.clamp(quad, 0, 1) / 2);
                }
            } catch (Exception ignored) {
            }
        }
        return opacity;
    }

    @ModifyVariable(method = "render", at = @At(
            value = "STORE"
    ))
    private MessageIndicator removeMessageIndicator(MessageIndicator messageIndicator) {
        if (ClientSetting.INSTANCE.hideIndicator.getValue()) {
            return null;
        }
        return messageIndicator;
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("TAIL"))
    private void addMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        messageTimestamps.add(0, new FadeUtils((long) fadeTime));
        while (this.messageTimestamps.size() > this.visibleMessages.size()) {
            this.messageTimestamps.remove(this.messageTimestamps.size() - 1);
        }
    }
}
