package dev.luminous.asm.mixins;

import com.google.common.collect.Lists;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ChatMessages.class)
public class MixinChatMessages {
    @Final
    @Shadow
    private static OrderedText SPACES;

    @Shadow
    private static String getRenderedChatMessage(String message) {
        return "";
    }

    @Inject(method = "breakRenderedChatMessageLines", at = @At("HEAD"), cancellable = true)
    private static void breakRenderedChatMessageLinesHook(StringVisitable message, int width, TextRenderer textRenderer, CallbackInfoReturnable<List<OrderedText>> cir) {
        TextCollector textCollector = new TextCollector();
        message.visit((style, messagex) -> {
            textCollector.add(StringVisitable.styled(getRenderedChatMessage(messagex), style));
            return Optional.empty();
        }, Style.EMPTY);
        List<OrderedText> list = Lists.newArrayList();
        textRenderer.getTextHandler().wrapLines(textCollector.getCombined(), width, Style.EMPTY, (text, lastLineWrapped) -> {
            OrderedText orderedText = Language.getInstance().reorder(text);
            OrderedText o = lastLineWrapped ? OrderedText.concat(SPACES, orderedText) : orderedText;
            list.add(o);
            ClientSetting.chatMessage.put(o, message);
        });
        cir.setReturnValue(list.isEmpty() ? Lists.newArrayList(OrderedText.EMPTY) : list);
    }

}
