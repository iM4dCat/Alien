package dev.luminous.mod.gui.clickgui.particle;

import dev.luminous.api.utils.render.Render2DUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.Random;

public class Snow {
    private int x;
    private int y;
    private int fallingSpeed;
    private int size;

    public Snow(int x, int y, int fallingSpeed, int size) {
        this.x = x;
        this.y = y;
        this.fallingSpeed = fallingSpeed;
        this.size = size;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int _y) {
        this.y = _y;
    }

    public void drawSnow(DrawContext drawContext) {
        Render2DUtil.drawRect(drawContext.getMatrices(), this.getX(), this.getY(), this.size, this.size, -1714829883);
        this.setY(this.getY() + this.fallingSpeed);
        if (this.getY() > MinecraftClient.getInstance().getWindow().getScaledHeight() + 10 || this.getY() < -10) {
            this.setY(-10);
            Random rand = new Random();
            this.fallingSpeed = rand.nextInt(10) + 1;
            this.size = rand.nextInt(4) + 1;
        }
    }
}

