package dev.luminous.mod.modules.impl.render;

import dev.luminous.Alien;
import dev.luminous.core.impl.BreakManager;
import dev.luminous.api.utils.math.Easing;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.api.utils.render.Render3DUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.text.DecimalFormat;

public class BreakESP extends Module {
	public static BreakESP INSTANCE;
	private final ColorSetting box = add(new ColorSetting("Box", new Color(255, 255, 255, 255)).injectBoolean(true));
	private final ColorSetting fill = add(new ColorSetting("Fill", new Color(255, 255, 255, 100)).injectBoolean(true));
	public final SliderSetting animationTime = add(new SliderSetting("AnimationTime", 500, 0, 2000).setSuffix("ms"));
	public final SliderSetting breakTime = add(new SliderSetting("BreakTime", 2.5, 0, 5, 0.1).setSuffix("s"));
	private final EnumSetting<Easing> ease = add(new EnumSetting<>("Ease", Easing.CubicInOut));

	public BreakESP() {
		super("BreakESP", Category.Render);
		setChinese("挖掘显示");
		INSTANCE = this;
	}

	DecimalFormat df = new DecimalFormat("0.0");

	@Override
	public void onRender3D(MatrixStack matrixStack) {
		for (BreakManager.BreakData breakData : Alien.BREAK.breakMap.values()) {
			if (breakData == null || breakData.getEntity() == null) continue;
			double size = 0.5 * (1 - breakData.fade.ease(ease.getValue()));
			Box cbox = new Box(breakData.pos).shrink(size, size, size).shrink(-size, -size, -size);
			if (fill.booleanValue) {
				Render3DUtil.drawFill(matrixStack, cbox, fill.getValue());
			}
			if (box.booleanValue) {
				Render3DUtil.drawBox(matrixStack, cbox, box.getValue());
			}
			Render3DUtil.drawText3D(breakData.getEntity().getName().getString(), breakData.pos.toCenterPos().add(0, 0.15, 0), -1);
			double breakTime = this.breakTime.getValue() * 1000;
			Render3DUtil.drawText3D(Text.of(df.format(Math.min(1, breakData.timer.getPassedTimeMs() / breakTime) * 100)), breakData.pos.toCenterPos().add(0, -0.15, 0), 0, 0, 1, ColorUtil.fadeColor(new Color(255, 6, 6), new Color(0, 255, 12), breakData.timer.getPassedTimeMs() / breakTime));
			//mc.world.isAir(breakData.pos) ? "Broken" : "Breaking"
		}
	}
}
