package dev.luminous.mod.gui.font;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;



public class FontRenderers {
    public static FontAdapter ui;
    public static FontAdapter Calibri;
    public static @NotNull RendererFontAdapter createDefault(float size, String name) throws IOException, FontFormatException {
        return new RendererFontAdapter(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(FontRenderers.class.getClassLoader().getResourceAsStream("assets/minecraft/font/" + name + ".ttf"))).deriveFont(Font.PLAIN, size), size);
    }

    public static void createDefault(float size) {

        try {
            ui = createDefault(size, "font");
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        }
/*        Font font = new Font("Tahoma", Font.PLAIN, (int) size);
        ui = new RendererFontAdapter(font, size);*/
    }

    public static RendererFontAdapter create(String name, int style, float size) {
        return new RendererFontAdapter(new Font(name, style, (int) size), size);
    }
}
