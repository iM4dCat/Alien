package dev.luminous.mod.gui.clickgui.components.impl;

import dev.luminous.core.impl.GuiManager;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.render.Render2DUtil;
import dev.luminous.api.utils.render.TextUtil;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.gui.clickgui.components.Component;
import dev.luminous.mod.gui.clickgui.tabs.ClickGuiTab;
import dev.luminous.mod.modules.impl.client.ClickGui;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class SliderComponent extends Component {

	private final ClickGuiTab parent;
	private double currentSliderPosition;
	final SliderSetting setting;

	public SliderComponent(ClickGuiTab parent, SliderSetting setting) {
		super();
		this.parent = parent;
		this.setting = setting;
	}

	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.getAsBoolean();
		}
		return true;
	}

	private boolean clicked = false;
	private boolean hover = false;
	private boolean firstUpdate = true;

	@Override
	
	public void update(int offset, double mouseX, double mouseY) {
		if (firstUpdate || setting.update) {
			this.currentSliderPosition = (float) ((setting.getValue() - setting.getMinimum()) / setting.getRange());
			firstUpdate = false;
		}
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();

		if ((mouseX >= ((parentX)) && mouseX <= (((parentX)) + parentWidth - 2)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + defaultHeight - 2))) {
			hover = true;
			if (GuiManager.currentGrabbed == null && isVisible()) {
				if (ClickGuiScreen.clicked) {
					sound();
				}
				if (ClickGuiScreen.clicked || ClickGuiScreen.hoverClicked && clicked) {
					if (setting.isListening()) {
						setting.setListening(false);
						ClickGuiScreen.clicked = false;
					} else {
						clicked = true;
						ClickGuiScreen.hoverClicked = true;
						ClickGuiScreen.clicked = false;
						this.currentSliderPosition = (float) Math.min((mouseX - (parentX)) / (parentWidth - 4), 1f);
						this.currentSliderPosition = Math.max(0f, this.currentSliderPosition);
						this.setting.setValue((this.currentSliderPosition * this.setting.getRange()) + this.setting.getMinimum());
					}
				}
				if (ClickGuiScreen.rightClicked) {
					sound();
					setting.setListening(!setting.isListening());
					ClickGuiScreen.rightClicked = false;
				}
			}
		} else {
			clicked = false;
			hover = false;
		}
	}

	public double renderSliderPosition = 0;
	private final Timer timer = new Timer();
	boolean b;

	@Override
	
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		if (back) {
			setting.setListening(false);
		}
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		renderSliderPosition = animation.get(Math.floor((parentWidth - 2) * currentSliderPosition));
		float height = ClickGui.INSTANCE.uiType.getValue() == ClickGui.Type.New ? 1 : defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1);
		float y = ClickGui.INSTANCE.uiType.getValue() == ClickGui.Type.New ? (float) (parentY + offset + defaultHeight - 3) : (float) (parentY + offset - 1);
		if (ClickGui.INSTANCE.mainEnd.booleanValue) {
			Render2DUtil.drawRectHorizontal(matrixStack, parentX + 1, y, (int) this.renderSliderPosition, height, hover ? ClickGui.INSTANCE.mainHover.getValue() : color, ClickGui.INSTANCE.mainEnd.getValue());
		} else {
			Render2DUtil.drawRect(matrixStack, parentX + 1, y, (int) this.renderSliderPosition, height, hover ? ClickGui.INSTANCE.mainHover.getValue() : color);
		}
		if (this.setting == null) return true;
		if (setting.isListening()) {
			if (timer.passed(1000)) {
				b = !b;
				timer.reset();
			}
			TextUtil.drawString(drawContext, setting.temp + (b ? "_" : ""), parentX + 4,
					(float) (parentY + getTextOffsetY() + offset - 2), 0xFFFFFF);
		} else {
			String value;
			if (setting.getValueInt() == setting.getValue()) {
				value = String.valueOf(setting.getValueInt());
			} else {
				value = String.valueOf(this.setting.getValueFloat());
			}
			value = value + setting.getSuffix();
			TextUtil.drawString(drawContext, setting.getName(), (float) (parentX + 4),
					(float) (parentY + getTextOffsetY() + offset - 2), 0xFFFFFF);
			TextUtil.drawString(drawContext, value, parentX + parentWidth - TextUtil.getWidth(value) - 5,
					(float) (parentY + getTextOffsetY() + offset - 2), 0xFFFFFF);
		}
		return true;
	}
}

