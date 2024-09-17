package dev.luminous.mod.gui.clickgui.components.impl;

import dev.luminous.mod.modules.impl.client.ClickGui;
import dev.luminous.mod.modules.settings.impl.StringSetting;
import dev.luminous.core.impl.GuiManager;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.render.Render2DUtil;
import dev.luminous.api.utils.render.TextUtil;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.gui.clickgui.components.Component;
import dev.luminous.mod.gui.clickgui.tabs.ClickGuiTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class StringComponent extends Component {
	private final StringSetting setting;

	public StringComponent(ClickGuiTab parent, StringSetting setting) {
		super();
		this.setting = setting;
		this.parent = parent;
	}
	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.getAsBoolean();
		}
		return true;
	}

	boolean hover = false;

	
	
	public void update(int offset, double mouseX, double mouseY) {
		if (GuiManager.currentGrabbed == null && isVisible()) {
			int parentX = parent.getX();
			int parentY = parent.getY();
			int parentWidth = parent.getWidth();
			if ((mouseX >= ((parentX + 1)) && mouseX <= (((parentX)) + parentWidth - 1)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + defaultHeight - 2))) {
				hover = true;
				if (ClickGuiScreen.clicked) {
					sound();
					ClickGuiScreen.clicked = false;
					setting.setListening(!setting.isListening());
				}
			} else {
				if(ClickGuiScreen.clicked && setting.isListening()) {
					sound();
					setting.setListening(false);
				}
				hover = false;
			}
		} else {
			if (setting.isListening()) {
				setting.setListening(false);
			}
			hover = false;
		}
	}

	private final Timer timer = new Timer();
	boolean b;

	@Override
	
	
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		if (timer.passed(1000)) {
			b = !b;
			timer.reset();
		}
		if (back) {
			setting.setListening(false);
		}
		int parentX = this.parent.getX();
		int parentY = this.parent.getY();
		int y = parent.getY() + offset - 2;
		int width = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		String text = setting.getValue();
		if (setting.isListening() && b) {
			text = text + "_";
		}
		String name = setting.isListening() ? "[E]" : setting.getName();
		if (hover)
			Render2DUtil.drawRect(matrixStack, (float) parentX + 1, (float) y + 1, (float) width - 3, (float) defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1), ClickGui.INSTANCE.settingHover.getValue());
		TextUtil.drawString(drawContext, text, parentX + 4 + TextUtil.getWidth(name) / 2,
				(float) (parentY + getTextOffsetY() + offset) - 2, 0xFFFFFF);
		TextUtil.drawStringWithScale(drawContext, name, (float) (parentX + 4),
				(float) (parentY + getTextOffsetY() + offset - 2), -1, 0.5f);
		return true;
	}
}