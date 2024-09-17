package dev.luminous.api.utils.render;

import dev.luminous.api.utils.Wrapper;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.mod.gui.font.FontRenderers;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import dev.luminous.mod.modules.impl.client.ClickGui;
import dev.luminous.mod.modules.impl.client.FontSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class TextUtil implements Wrapper {
	public static boolean isCustomFont() {
		return ClickGui.INSTANCE.font.getValue() && FontRenderers.ui != null;
	}
	public static float getWidth(String s) {
		return isCustomFont() ? FontRenderers.ui.getWidth(s) : mc.textRenderer.getWidth(s);
	}
	public static float getHeight() {
		return (isCustomFont() ? FontRenderers.ui.getFontHeight() : mc.textRenderer.fontHeight);
	}
	public static void drawStringWithScale(DrawContext drawContext, String text, float x, float y, Color color, float scale) {
		drawStringWithScale(drawContext, text, x, y, color.getRGB(), scale);
	}
	public static void drawStringWithScale(DrawContext drawContext, String text, float x, float y, int color, float scale) {
		MatrixStack matrixStack = drawContext.getMatrices();
		if (scale != 1) {
			matrixStack.push();
			matrixStack.scale(scale, scale, 1.0f);
			if (scale > 1.0f) {
				matrixStack.translate(-x / scale, -y / scale, 0.0f);
			} else {
				matrixStack.translate((x / scale) / 2, (y / scale) / 2, 0.0f);
			}
		}

		drawString(drawContext, text, x, y, color);
		matrixStack.pop();
	}

	public static void drawStringScale(DrawContext drawContext, String text, float x, float y, int color, float scale) {
		MatrixStack matrixStack = drawContext.getMatrices();
		if (scale != 1) {
			matrixStack.push();
			matrixStack.scale(scale, scale, 1.0f);
			if (scale > 1.0f) {
				matrixStack.translate(-x / scale, -y / scale, 0.0f);
			} else {
				matrixStack.translate((x / scale) / 2, (y / scale) / 2, 0.0f);
			}
		}

		drawContext.drawText(mc.textRenderer, text, (int) x, (int) y, color, true);
		matrixStack.pop();
	}
	public static void drawString(DrawContext drawContext, String text, double x, double y, Color color) {
		drawString(drawContext, text, x, y, color.getRGB());
	}
	public static void drawString(DrawContext drawContext, String text, double x, double y, int color) {
		drawString(drawContext, text, x, y, color, isCustomFont());
	}

	public static void drawString(DrawContext drawContext, String text, double x, double y, int color, boolean customFont) {
		if (customFont) {
			//FontRenderers.Arial.drawString(drawContext.getMatrices(), text, (float) x + 1, (float) y + 3, new Color(0, 0, 0).getRGB());
			FontRenderers.ui.drawString(drawContext.getMatrices(), text, (int) x, (int) (y + FontSetting.INSTANCE.yOffset.getValueFloat() + 2), color);
		} else {
			drawContext.drawText(mc.textRenderer, text, (int) x, (int) y, color, true);
		}
	}

	public static int drawStringPulse(DrawContext drawContext, OrderedText text, double x, double y, Color startColor, Color endColor, double speed, int counter) {
		char[] stringToCharArray = ClientSetting.chatMessage.get(text).getString().toCharArray();
		int index = 0;
		boolean color = false;
		String s = null;
		for (char c : stringToCharArray) {
			if (c == '§') {
				color = true;
				continue;
			}
			if (color) {
				if (c == 'r') {
					s = null;
				} else {
					if (s == null) {
						s = "§" + c;
					} else {
						s = s + "§" + c;
					}
				}
				color = false;
				continue;
			}
			index++;
			if (s != null) {
				drawContext.drawTextWithShadow(mc.textRenderer, s + c, (int) x,
                        (int) y, ColorUtil.pulseColor(startColor, endColor, index, counter, speed).getRGB());
			} else {
				drawContext.drawTextWithShadow(mc.textRenderer, String.valueOf(c), (int) x,
						(int) y, ColorUtil.pulseColor(startColor, endColor, index, counter, speed).getRGB());
			}
			x += (float) mc.textRenderer.getWidth(String.valueOf(c));
		}
		return (int) x;
	}
	public static void drawStringPulse(DrawContext drawContext, String text, double x, double y, Color startColor, Color endColor, double speed, int counter, boolean customFont) {
		char[] stringToCharArray = text.toCharArray();
		int index = 0;
		boolean color = false;
		String s = null;
		for (char c : stringToCharArray) {
			if (c == '§') {
				color = true;
				continue;
			}
			if (color) {
				if (c == 'r') {
					s = null;
                } else {
					s = "§" + c;
                }
				color = false;
                continue;
            }
			index++;
			if (s != null) {
				drawString(drawContext, s + c, x,
						y, startColor.getRGB(), customFont);
			} else {
				drawString(drawContext, String.valueOf(c), x,
						y, ColorUtil.pulseColor(startColor, endColor, index, counter, speed).getRGB(), customFont);
			}
			x += customFont ? FontRenderers.ui.getWidth(String.valueOf(c)) : mc.textRenderer.getWidth(String.valueOf(c));
		}
	}
	public static final Matrix4f lastProjMat = new Matrix4f();
	public static final Matrix4f lastModMat = new Matrix4f();
	public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
	public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
		Camera camera = mc.getEntityRenderDispatcher().camera;
		int displayHeight = mc.getWindow().getHeight();
		int[] viewport = new int[4];
		GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		Vector3f target = new Vector3f();

		double deltaX = pos.x - camera.getPos().x;
		double deltaY = pos.y - camera.getPos().y;
		double deltaZ = pos.z - camera.getPos().z;

		Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(lastWorldSpaceMatrix);
		Matrix4f matrixProj = new Matrix4f(lastProjMat);
		Matrix4f matrixModel = new Matrix4f(lastModMat);
		matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
		return new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);
	}

	public static void drawText(DrawContext context, String text, Vec3d vector) {
		drawText(context, text, vector, -1);
	}

	public static void drawText(DrawContext context, String text, Vec3d vector, int color) {
		Vec3d preVec = vector;
		vector = worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
		if (vector.z > 0 && vector.z < 1) {
			double posX = vector.x;
			double posY = vector.y;
			double endPosX = Math.max(vector.x, vector.z);
			float scale = (float) Math.max(1 - EntityUtil.getEyesPos().distanceTo(preVec) * 0.025, 0);
			float diff = (float) (endPosX - posX) / 2;
			float textWidth = mc.textRenderer.getWidth(text) * scale;
			float tagX = (float) ((posX + diff - textWidth / 2) * 1);
			context.getMatrices().push();
			context.getMatrices().scale(scale, scale, scale);
			context.drawText(mc.textRenderer, text, (int) (tagX / scale), (int) ((posY - 11 + mc.textRenderer.fontHeight * 1.2) / scale), color, true);
			context.getMatrices().pop();
		}
	}
}
