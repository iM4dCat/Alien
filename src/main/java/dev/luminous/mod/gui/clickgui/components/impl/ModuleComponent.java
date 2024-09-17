package dev.luminous.mod.gui.clickgui.components.impl;

import dev.luminous.core.impl.GuiManager;
import dev.luminous.api.utils.math.Animation;
import dev.luminous.api.utils.math.FadeUtils;
import dev.luminous.api.utils.render.Render2DUtil;
import dev.luminous.api.utils.render.TextUtil;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.gui.clickgui.components.Component;
import dev.luminous.mod.gui.clickgui.tabs.ClickGuiTab;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.ClickGui;
import dev.luminous.mod.modules.settings.Setting;
import dev.luminous.mod.modules.settings.impl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleComponent extends Component {

	private final Module module;
	private final ClickGuiTab parent;
	private boolean popped = false;

	private int expandedHeight = defaultHeight;

	private final List<Component> settingsList = new ArrayList<>();
	public List<Component> getSettingsList() {
		return settingsList;
	}
	public ModuleComponent(ClickGuiTab parent, Module module) {
		super();
		this.parent = parent;
		this.module = module;
		for (Setting setting : this.module.getSettings()) {
			Component c;
			if (setting.hide) {
				c = null;
			} else if (setting instanceof SliderSetting) {
				c = new SliderComponent(this.parent, (SliderSetting) setting);
			} else if (setting instanceof BooleanSetting) {
				c = new BooleanComponent(this.parent, (BooleanSetting) setting);
			} else if (setting instanceof BindSetting) {
				c = new BindComponent(this.parent, (BindSetting) setting);
			} else if (setting instanceof EnumSetting) {
				c = new EnumComponent(this.parent, (EnumSetting<?>) setting);
			} else if (setting instanceof ColorSetting) {
				c = new ColorComponents(this.parent, (ColorSetting) setting);
			} else if (setting instanceof StringSetting) {
				c = new StringComponent(this.parent, (StringSetting) setting);
			} else {
				c = null;
			}
			if (c != null)
				settingsList.add(c);
		}

		RecalculateExpandedHeight();
	}

	boolean hovered = false;

	
	
	public void update(int offset, double mouseX, double mouseY) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();

		if (this.popped) {
			int i = offset + defaultHeight + 1;
			for (Component children : this.settingsList) {
				children.update(i, mouseX, mouseY);
				i += children.getHeight();
			}
		}

		hovered = ((mouseX >= parentX && mouseX <= (parentX + parentWidth)) && (mouseY >= parentY + offset && mouseY <= (parentY + offset + defaultHeight - 1)));
		if (hovered && GuiManager.currentGrabbed == null) {
			if (ClickGuiScreen.clicked) {
				ClickGuiScreen.clicked = false;
				if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
					sound();
					module.drawnSetting.toggleValue();
				} else {
					sound();
					module.toggle();
				}
			}

			if (ClickGuiScreen.rightClicked) {
				ClickGuiScreen.rightClicked = false;
				this.popped = !this.popped;
				sound();
			}
		}
		RecalculateExpandedHeight();
		if (this.popped) {
			this.setHeight(expandedHeight);
		} else {
			this.setHeight(defaultHeight);
		}
	}

	public double currentWidth = 0;
	public Animation offsetAnimation = new Animation();
	public double currentPopHeight = 0;
	public Animation popHeightAnimation = new Animation();
	@Override
	
	
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		RecalculateExpandedHeight();
		String text = module.getDisplayName();
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		currentOffset = offsetAnimation.get(offset);
		boolean scissor = ClickGui.fade.ease(FadeUtils.Ease.Out) >= 1;
		if (scissor) {
			drawContext.enableScissor(parentX, (int) ((parentY + currentOffset + defaultHeight)), (parentX + parentWidth), mc.getWindow().getScaledHeight());
		}
		currentPopHeight = popHeightAnimation.get(popped ? (expandedHeight - defaultHeight) : 0);
		if (currentPopHeight > 0) {
			int i = (int) (currentOffset + defaultHeight + 1);
			if (scissor) {
				drawContext.enableScissor(parentX, (parentY + i - 1), (parentX + parentWidth), (int) ((parentY + currentOffset + defaultHeight + currentPopHeight)));
			}
			for (Component children : this.settingsList) {
				if (children.isVisible()) {
					children.draw(i, drawContext, partialTicks, color, !popped);
					i += children.getCurrentHeight();
				}
			}
			if (scissor) {
				drawContext.disableScissor();
			}
		}
		if (scissor) {
			drawContext.disableScissor();
		}
		currentWidth = animation.get(module.isOn() ? (parentWidth - 2D) : 0D);
		if (ClickGui.INSTANCE.activeBox.getValue()) {
			if (ClickGui.INSTANCE.mainEnd.booleanValue) {
				Render2DUtil.drawRectHorizontal(matrixStack, parentX + 1, (int) (parentY + currentOffset), (float) currentWidth, defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1), hovered ? ClickGui.INSTANCE.mainHover.getValue() : ClickGui.INSTANCE.color.getValue(), ClickGui.INSTANCE.mainEnd.getValue());
			} else {
				Render2DUtil.drawRect(matrixStack, parentX + 1, (int) (parentY + currentOffset), (float) currentWidth, defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1), hovered ? ClickGui.INSTANCE.mainHover.getValue() : ClickGui.INSTANCE.color.getValue());
			}
		}
		if (module.isOff() || !ClickGui.INSTANCE.activeBox.getValue())
			Render2DUtil.drawRect(matrixStack, parentX + 1, (int) (parentY + currentOffset), parentWidth - 2, defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1), hovered ? ClickGui.INSTANCE.moduleHover.getValue() : ClickGui.INSTANCE.module.getValue());
		if (hovered && InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
			TextUtil.drawString(drawContext, "Drawn " + (module.drawnSetting.getValue() ? "§aOn" : "§cOff"), (float) (parentX + 4), (float) (parentY + getTextOffsetY() + currentOffset) - 1, -1);
		} else {
			if (ClickGui.INSTANCE.center.getValue()) {
				TextUtil.drawString(drawContext, text, parentX + parentWidth / 2f - TextUtil.getWidth(text) / 2, (float) (parentY + getTextOffsetY() + currentOffset) - 1,
						module.isOn() ? ClickGui.INSTANCE.enableText.getValue().getRGB() : ClickGui.INSTANCE.disableText.getValue().getRGB());
			} else {
				TextUtil.drawString(drawContext, text, (float) (parentX + 4), (float) (parentY + getTextOffsetY() + currentOffset) - 1,
						module.isOn() ? ClickGui.INSTANCE.enableText.getValue().getRGB() : ClickGui.INSTANCE.disableText.getValue().getRGB());
			}
		}

		if (ClickGui.INSTANCE.bind.booleanValue) {
			if (module.getBind().getKey() != -1) {
				String bindText = "[" + module.getBind().getBind() + "]";
				TextUtil.drawStringWithScale(drawContext, bindText, (ClickGui.INSTANCE.center.getValue() ? (parentX + parentWidth / 2f - TextUtil.getWidth(text) / 2) : (parentX + 4)) + 1 + TextUtil.getWidth(text), (float) (parentY + getTextOffsetY() + currentOffset - TextUtil.getHeight() / 4), ClickGui.INSTANCE.bind.getValue(), 0.5f);
			}
		}
		if (ClickGui.INSTANCE.gear.booleanValue) {
			if (popped) {
				TextUtil.drawString(drawContext, "-", parentX + parentWidth - 11,
						parentY + getTextOffsetY() + currentOffset - 1, ClickGui.INSTANCE.gear.getValue().getRGB());
			} else {
				TextUtil.drawString(drawContext, "+", parentX + parentWidth - 11,
						parentY + getTextOffsetY() + currentOffset - 1, ClickGui.INSTANCE.gear.getValue().getRGB());
			}
		}
		return true;
	}

	public void RecalculateExpandedHeight() {
		int height = defaultHeight;
		for (Component children : this.settingsList) {
			if (children != null && children.isVisible()) {
				height += children.getHeight();
			}
		}
		expandedHeight = height;
	}
}
