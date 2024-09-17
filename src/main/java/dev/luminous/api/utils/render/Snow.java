package dev.luminous.api.utils.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.Random;

public class Snow {
    int x;
    int y;
    int fallingSpeed;
    int size;
    public Snow(int x, int y, int fallingSpeed, int size) {
        this.x = x;
        this.y = y;
        this.fallingSpeed = fallingSpeed;
        this.size = size;
    }

    public void drawSnow(DrawContext drawContext, Color color) {
        this.y = (this.y + this.fallingSpeed);
        Render2DUtil.drawRect(drawContext.getMatrices(), this.x, this.y, this.size, this.size, color.getRGB());
        if (this.y > MinecraftClient.getInstance().getWindow().getScaledHeight() + 10 || this.y < -10) {
            this.y = (-10);
            Random rand = new Random();
            this.fallingSpeed = rand.nextInt(10) + 1;
            this.size = rand.nextInt(4) + 1;
        }
    }
}