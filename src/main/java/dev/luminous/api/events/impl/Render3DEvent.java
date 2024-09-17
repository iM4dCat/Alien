package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;
import dev.luminous.api.utils.render.Render3DUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

import java.awt.*;

public class Render3DEvent extends Event {

    private final float partialTicks;
    private final MatrixStack matrixStack;

    public Render3DEvent(MatrixStack matrixStack, float partialTicks) {
        super(Stage.Pre);
        this.partialTicks = partialTicks;
        this.matrixStack = matrixStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public void drawBox(Box box, Color color) {
        Render3DUtil.drawBox(matrixStack, box, color);
    }
}

