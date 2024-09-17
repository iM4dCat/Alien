package dev.luminous.asm.accessors;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChatHud.class)
public interface IChatHud {

    @Mutable
    @Accessor("visibleMessages")
    void setVisibleMessages(List<ChatHudLine.Visible> visibleMessages);
    @Mutable
    @Accessor("messages")
    void setMessages(List<ChatHudLine.Visible> messages);
}