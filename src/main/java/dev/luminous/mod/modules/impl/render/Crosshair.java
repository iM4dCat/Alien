package dev.luminous.mod.modules.impl.render;

import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.api.utils.render.Render2DUtil;
import dev.luminous.mod.modules.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class Crosshair extends Module {
	public static Crosshair INSTANCE;
	private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 255)));
	public final SliderSetting length =
			add(new SliderSetting("Length", 5, 0.0, 20.0, 0.1));
	public final SliderSetting thickness =
			add(new SliderSetting("Thickness", 2, 0.0, 20.0, 0.1));
	public final SliderSetting interval =
			add(new SliderSetting("Interval", 2, 0.0, 20.0, 0.1));
	public Crosshair() {
		super("Crosshair", Category.Render);
		setChinese("准星");
		INSTANCE = this;
	}

	public void draw(DrawContext context) {
		MatrixStack matrixStack = context.getMatrices();
		float centerX = mc.getWindow().getScaledWidth() / 2f;
		float centerY = mc.getWindow().getScaledHeight() / 2f;

		//Up
		Render2DUtil.drawRect(matrixStack, centerX - thickness.getValueFloat() / 2f, centerY - length.getValueFloat() - interval.getValueFloat(), thickness.getValueFloat(), length.getValueFloat(), color.getValue());

		//Down
		Render2DUtil.drawRect(matrixStack, centerX - thickness.getValueFloat() / 2f, centerY + interval.getValueFloat(), thickness.getValueFloat(), length.getValueFloat(), color.getValue());

		//Right
		Render2DUtil.drawRect(matrixStack, centerX + interval.getValueFloat(), centerY - thickness.getValueFloat() / 2f, length.getValueFloat(), thickness.getValueFloat(), color.getValue());

		//Left
		Render2DUtil.drawRect(matrixStack, centerX - interval.getValueFloat() - length.getValueFloat(), centerY - thickness.getValueFloat() / 2f, length.getValueFloat(), thickness.getValueFloat(), color.getValue());
	}
}
