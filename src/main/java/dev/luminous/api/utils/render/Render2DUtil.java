package dev.luminous.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.api.utils.Wrapper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.*;

public class Render2DUtil implements Wrapper {
    public static void horizontalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(startColor.getRGB()).next();
        bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(startColor.getRGB()).next();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(endColor.getRGB()).next();
        bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(endColor.getRGB()).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static void verticalGradient(MatrixStack matrices, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, left, top, 0.0F).color(startColor.getRGB()).next();
        bufferBuilder.vertex(matrix, left, bottom, 0.0F).color(endColor.getRGB()).next();
        bufferBuilder.vertex(matrix, right, bottom, 0.0F).color(endColor.getRGB()).next();
        bufferBuilder.vertex(matrix, right, top, 0.0F).color(startColor.getRGB()).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static void drawRectVertical(MatrixStack matrices, float x, float y, float width, float height, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(startColor.getRGB()).next();
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(endColor.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(endColor.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(startColor.getRGB()).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }
    public static void drawRectHorizontal(MatrixStack matrices, float x, float y, float width, float height, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(startColor.getRGB()).next();
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(startColor.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(endColor.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(endColor.getRGB()).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }
    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, int c) {
        drawRect(matrices, x, y, width, height, new Color(c, true));
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
        if (c.getAlpha() <= 5) return;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(c.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(c.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(c.getRGB()).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(c.getRGB()).next();
        Tessellator.getInstance().draw();
        endRender();
    }


    public static void drawRect(DrawContext drawContext, float x, float y, float width, float height, Color c) {
        drawRect(drawContext.getMatrices(), x, y, width, height, c);
        //drawContext.fill((int) x, (int) y, (int) (x + width), (int) (y + height), c.getRGB());
    }
    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;
    }

    public static void drawRound(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
        renderRoundedQuad(matrices, color, x, y, width + x, height + y, radius, 4);
    }

    public static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        renderRoundedQuadInternal(matrices.peek().getPositionMatrix(), c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f, fromX, fromY, toX, toY, radius, samples);
        endRender();
    }


    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        double[][] map = new double[][]{new double[]{toX - radius, toY - radius, radius}, new double[]{toX - radius, fromY + radius, radius}, new double[]{fromX + radius, fromY + radius, radius}, new double[]{fromX + radius, toY - radius, radius}};
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
            }
            float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.disableBlend();
    }
}
