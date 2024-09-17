package dev.luminous.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;

import java.awt.*;

public class HighLight extends Module {
	public static HighLight INSTANCE;
	public HighLight() {
		super("HighLight", Category.Render);
		INSTANCE = this;
		setChinese("方块高亮");
	}
	private final BooleanSetting depth = add(new BooleanSetting("Depth", true));
	private final ColorSetting fill = add(new ColorSetting("Fill", new Color(255, 0, 0, 50)).injectBoolean(true));
	private final ColorSetting boxColor = add(new ColorSetting("Box", new Color(255, 0, 0, 100)).injectBoolean(true));

	@Override
	public void onRender3D(MatrixStack matrixStack) {
		if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK && mc.crosshairTarget instanceof BlockHitResult hitResult && (fill.booleanValue || boxColor.booleanValue
		)) {
			VoxelShape shape = mc.world.getBlockState(hitResult.getBlockPos()).getOutlineShape(mc.world, hitResult.getBlockPos());
			if (shape == null) return;
			if (shape.isEmpty()) return;
			Box box = shape.getBoundingBox().offset(hitResult.getBlockPos()).expand(0.001);

			box = box.offset(mc.gameRenderer.getCamera().getPos().negate());
			RenderSystem.enableBlend();
			if (!depth.getValue()) RenderSystem.disableDepthTest();

			Matrix4f matrix = matrixStack.peek().getPositionMatrix();
			Tessellator tessellator = RenderSystem.renderThreadTesselator();
			BufferBuilder bufferBuilder = tessellator.getBuffer();

			if (fill.booleanValue) {
				Color color = fill.getValue();
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

			if (depth.getValue()) RenderSystem.disableDepthTest();
			if (boxColor.booleanValue) {
				Color color = boxColor.getValue();
				RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
				RenderSystem.setShader(GameRenderer::getPositionProgram);
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
			RenderSystem.setShaderColor(1, 1, 1, 1);

			RenderSystem.enableDepthTest();
			RenderSystem.disableBlend();
		}
	}
}
