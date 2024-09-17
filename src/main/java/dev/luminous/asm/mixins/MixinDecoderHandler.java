package dev.luminous.asm.mixins;

import dev.luminous.mod.modules.impl.misc.AntiBookBan;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.handler.DecoderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DecoderHandler.class)
public class MixinDecoderHandler {

    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/" +
            "network/NetworkState;getId()Ljava/lang/String;", shift = At.Shift.AFTER), cancellable = true)
    private void hookDecode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> objects, CallbackInfo ci) {
        if (AntiBookBan.INSTANCE.isOn()) {
            ci.cancel();
        }
    }
}
