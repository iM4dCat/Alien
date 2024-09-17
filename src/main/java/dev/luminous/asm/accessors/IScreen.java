package dev.luminous.asm.accessors;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface IScreen {
    @Accessor("drawables")
    List<Drawable> getDrawables();
}