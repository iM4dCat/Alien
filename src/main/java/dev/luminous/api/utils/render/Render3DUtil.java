package dev.luminous.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.api.utils.Wrapper;
import dev.luminous.mod.gui.font.FontRenderers;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;

import static com.mojang.blaze3d.systems.RenderSystem.disableBlend;

public class Render3DUtil implements Wrapper {
    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

        return matrices;
    }

    public static void drawText3D(String text, Vec3d vec3d, Color color) {
        drawText3D(Text.of(text), vec3d.x, vec3d.y, vec3d.z, 0, 0, 1, color.getRGB());
    }

    public static void drawText3D(String text, Vec3d vec3d, int color) {
        drawText3D(Text.of(text), vec3d.x, vec3d.y, vec3d.z, 0, 0, 1, color);
    }
    public static void drawText3D(Text text, Vec3d vec3d, double offX, double offY, double scale, Color color) {
        drawText3D(text, vec3d.x, vec3d.y, vec3d.z, offX, offY, scale, color.getRGB());
    }
    public static void drawText3D(Text text, double x, double y, double z, double offX, double offY, double scale, int color) {
        RenderSystem.disableDepthTest();
        MatrixStack matrices = matrixFrom(x, y, z);

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        matrices.translate(offX, offY, 0);
        matrices.scale(-0.025f * (float) scale, -0.025f * (float) scale, 1);

        int halfWidth = mc.textRenderer.getWidth(text) / 2;

        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        matrices.push();
        matrices.translate(1, 1, 0);
        mc.textRenderer.draw(Text.of(text.getString().replaceAll("§[a-zA-Z0-9]", "")), -halfWidth, 0f, 0x202020, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xf000f0);
        immediate.draw();
        matrices.pop();

        mc.textRenderer.draw(text.copy(), -halfWidth, 0f, color, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xf000f0);
        immediate.draw();

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }
    public static void drawTextIn3D(String text, Vec3d pos, double offX, double offY, double textOffset, Color color) {
        MatrixStack matrices = new MatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        //RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(pos.getX() - camera.getPos().x, pos.getY() - camera.getPos().y, pos.getZ() - camera.getPos().z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.translate(offX, offY - 0.1, -0.01);
        matrices.scale(-0.025f, -0.025f, 0);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        FontRenderers.ui.drawCenteredString(matrices, text, textOffset, 0f, color.getRGB());
        immediate.draw();
        RenderSystem.enableCull();
        //RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawFill(MatrixStack matrixStack, Box bb, Color color) {
        draw3DBox(matrixStack, bb, color, false, true);
    }
    public static void drawBox(MatrixStack matrixStack, Box bb, Color color) {
        draw3DBox(matrixStack, bb, color, true, false);
    }

    public static void drawBox(MatrixStack matrixStack, Box bb, Color color, float lineWidth) {
        draw3DBox(matrixStack, bb, color, true, false, lineWidth);
    }

    public static void draw3DBox(MatrixStack matrixStack, Box box, Color color) {
        draw3DBox(matrixStack, box, color, true, true);
    }

    public static void draw3DBox(MatrixStack matrixStack, Box box, Color color, boolean outline, boolean fill) {
        draw3DBox(matrixStack, box, color, outline, fill, 1.5f);
    }
    public static void draw3DBox(MatrixStack matrixStack, Box box, Color color, boolean outline, boolean fill, float lineWidth) {
        box = box.offset(mc.gameRenderer.getCamera().getPos().negate());
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        if (outline) {
            RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            //GL11.glLineWidth(lineWidth);
            RenderSystem.lineWidth(lineWidth);
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

            tessellator.draw();
        }

        if (fill) {
            RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            RenderSystem.setShader(GameRenderer::getPositionProgram);

            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

            tessellator.draw();
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawFadeFill(MatrixStack stack, Box box, Color c, Color c1) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        //RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f posMatrix = stack.peek().getPositionMatrix();
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
        buffer.vertex(posMatrix, minX, minY, minZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, maxX, minY, minZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, maxX, minY, maxZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, minX, minY, maxZ).color(c.getRGB()).next();

        buffer.vertex(posMatrix, minX, minY, minZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, minY, minZ).color(c.getRGB()).next();

        buffer.vertex(posMatrix, maxX, minY, minZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, minY, maxZ).color(c.getRGB()).next();

        buffer.vertex(posMatrix, minX, minY, maxZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, maxX, minY, maxZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB()).next();

        buffer.vertex(posMatrix, minX, minY, minZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, minX, minY, maxZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB()).next();

        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB()).next();
        RenderSystem.disableCull();

        tessellator.draw();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    public static void drawLine(Vec3d start, Vec3d end, Color color) {
        drawLine(start.x, start.getY(), start.z, end.getX(), end.getY(), end.getZ(), color, 1);
    }
    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, Color color, float width) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        MatrixStack matrices = matrixFrom(x1, y1, z1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(width);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), color);
        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1f);
        disableBlend();
    }

    public static void vertexLine(MatrixStack matrices, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, Color lineColor) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();
        Vector3f normalVec = getNormal((float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2);
        buffer.vertex(model, (float) x1, (float) y1, (float) z1).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
        buffer.vertex(model, (float) x2, (float) y2, (float) z2).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
    }

    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }
}
