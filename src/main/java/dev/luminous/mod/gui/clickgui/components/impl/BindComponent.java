package dev.luminous.mod.gui.clickgui.components.impl;

import dev.luminous.mod.modules.impl.client.ClickGui;
import dev.luminous.mod.modules.settings.impl.BindSetting;
import dev.luminous.core.impl.GuiManager;
import dev.luminous.api.utils.render.Render2DUtil;
import dev.luminous.api.utils.render.TextUtil;
import dev.luminous.mod.gui.clickgui.components.Component;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.gui.clickgui.tabs.ClickGuiTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class BindComponent extends Component {
	private final BindSetting bind;
	public BindComponent(ClickGuiTab parent, BindSetting bind) {
		super();
		this.bind = bind;
		this.parent = parent;
	}

	boolean hover = false;

	
	
	public void update(int offset, double mouseX, double mouseY) {
		if (GuiManager.currentGrabbed == null && isVisible()) {
			int parentX = parent.getX();
			int parentY = parent.getY();
			int parentWidth = parent.getWidth();
			if (GuiManager.currentGrabbed == null && isVisible() && (mouseX >= ((parentX + 1)) && mouseX <= (((parentX)) + parentWidth - 1)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + defaultHeight - 2))) {
				hover = true;
				if (ClickGuiScreen.clicked) {
					sound();
					ClickGuiScreen.clicked = false;
					if (bind.getName().equals("Key") && InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
						bind.setHoldEnable(!bind.isHoldEnable());
					} else {
						bind.setListening(!bind.isListening());
					}
				}
			} else {
				hover = false;
			}
		} else {
			hover = false;
		}
	}

	@Override
	

	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		if (back) {
			bind.setListening(false);
		}
		int parentX = this.parent.getX();
		int parentY = this.parent.getY();
		int y = parent.getY() + offset - 2;
		int width = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		String text;
		if (hover && bind.getName().equals("Key") && InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
			text = "Hold " + (bind.isHoldEnable() ? "§aOn" : "§cOff");
		} else {
			if (bind.isListening()) {
				text = bind.getName() + ": " + "Press Key..";
			} else {
				text = bind.getName() + ": " + bind.getBind();
			}
		}
		if (hover) Render2DUtil.drawRect(matrixStack, (float) parentX + 1, (float) y + 1, (float) width - 3, (float) defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1), ClickGui.INSTANCE.settingHover.getValue());
		TextUtil.drawString(drawContext, text, (float) (parentX + 4),
				(float) (parentY + getTextOffsetY() + offset) - 2, 0xFFFFFF);
		return true;
	}
}