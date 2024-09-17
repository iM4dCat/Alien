package dev.luminous.asm.mixins;

import dev.luminous.api.interfaces.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.Visible.class)
public class MixinChatHudLineVisible implements IChatHudLine {
    @Unique
    private int id = 0;
    @Override
    public int getMessageId() {
        return id;
    }

    @Override
    public void setMessageId(int id) {
        this.id = id;
    }
}
