package dev.luminous.mod.gui.clickgui.components.impl;

import dev.luminous.Alien;
import dev.luminous.api.utils.math.Animation;
import dev.luminous.mod.modules.impl.client.ClickGui;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.core.impl.GuiManager;
import dev.luminous.api.utils.render.Render2DUtil;
import dev.luminous.api.utils.render.TextUtil;
import dev.luminous.mod.gui.clickgui.components.Component;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.gui.clickgui.tabs.ClickGuiTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
public class EnumComponent extends Component {
	private final EnumSetting<?> setting;
	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.getAsBoolean();
		}
		return true;
	}
	public EnumComponent(ClickGuiTab parent, EnumSetting<?> enumSetting) {
		super();
		this.parent = parent;
		setting = enumSetting;
	}

	private boolean hover = false;

	
	
	public void update(int offset, double mouseX, double mouseY) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		if ((mouseX >= ((parentX + 2)) && mouseX <= (((parentX)) + parentWidth - 2)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + defaultHeight - 2))) {
			hover = true;
			if (GuiManager.currentGrabbed == null && isVisible()) {
				if (ClickGuiScreen.clicked) {
					ClickGuiScreen.clicked = false;
					setting.increaseEnum();
					sound();
				}
				if (ClickGuiScreen.rightClicked) {
					setting.popped = !setting.popped;
					ClickGuiScreen.rightClicked = false;
					sound();
				}
			}
		} else {
			hover = false;
		}

		if (GuiManager.currentGrabbed == null && isVisible() && ClickGuiScreen.clicked) {
			int cy = parentY + offset - 1 + (defaultHeight - 2) - 2;
			if (setting.popped) {
				for (Object o : setting.getValue().getDeclaringClass().getEnumConstants()) {
					if (mouseX >= parentX && mouseX <= parentX + parentWidth && mouseY >= TextUtil.getHeight() / 2 + cy && mouseY < TextUtil.getHeight() + TextUtil.getHeight() / 2 + cy) {
						setting.setEnumValue(String.valueOf(o));
						ClickGuiScreen.clicked = false;
						sound();
						break;
					}
					cy += (int) TextUtil.getHeight();
				}
			}
		}
		y = 0;
		if (setting.popped) {
			for (Object ignored : setting.getValue().getDeclaringClass().getEnumConstants()) {
				y += (int) TextUtil.getHeight();
			}
			setHeight(defaultHeight + y);
		} else {
			setHeight(defaultHeight);
		}
	}

	@Override
	public int getCurrentHeight() {
		return (int) (defaultHeight + popHeightAnimation.get(y));
	}
	int y = 0;
	public double currentY = 0;
	public Animation popHeightAnimation = new Animation();
	@Override
	
	
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		y = 0;
		if (setting.popped) {
			for (Object ignored : setting.getValue().getDeclaringClass().getEnumConstants()) {
				y += (int) TextUtil.getHeight();
			}
			setHeight(defaultHeight + y);
		} else {
			setHeight(defaultHeight);
		}
		int x = parent.getX();
		int y = parent.getY() + offset - 2;
		int width = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();

		if (ClickGui.INSTANCE.mainEnd.booleanValue) {
			Render2DUtil.drawRectHorizontal(matrixStack, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1), hover ? ClickGui.INSTANCE.mainHover.getValue() : Alien.GUI.getColor(), ClickGui.INSTANCE.mainEnd.getValue());
		} else {
			Render2DUtil.drawRect(matrixStack, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1), hover ? ClickGui.INSTANCE.mainHover.getValue() : Alien.GUI.getColor());
		}
		TextUtil.drawString(drawContext, setting.getName() + ": " + setting.getValue().name(), x + 4, y + getTextOffsetY(), -1);
		TextUtil.drawString(drawContext, setting.popped ? "-" : "+", x + width - 11, y + getTextOffsetY(), new Color(255, 255, 255).getRGB());


		if (setting.popped) {
			currentY = animation.get(1);
		} else {
			currentY = animation.get(0);
		}
		double cy = (parent.getY() + offset - 1 + (defaultHeight - 2)) - 2;
		if (currentY > 0.04) {
			for (Object o : setting.getValue().getDeclaringClass().getEnumConstants()) {

				String s = o.toString();

				TextUtil.drawString(drawContext, s, width / 2d - TextUtil.getWidth(s) / 2d + 2.0f + x, TextUtil.getHeight() / 2d + (cy), setting.getValue().name().equals(s) ? new Color(255, 255, 255, (int) (currentY * 255)).getRGB() : new Color(120, 120, 120, (int) (currentY * 255)).getRGB());
				cy += TextUtil.getHeight() * currentY;
			}
		}
		return true;
	}
}