package dev.luminous.mod.gui.clickgui.components;

import dev.luminous.api.utils.Wrapper;
import dev.luminous.api.utils.math.AnimateUtil;
import dev.luminous.api.utils.math.Animation;
import dev.luminous.mod.gui.clickgui.tabs.ClickGuiTab;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.ClickGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.awt.*;

public abstract class Component implements Wrapper {
	public int defaultHeight = 16;
	protected ClickGuiTab parent;
	private int height = defaultHeight;

	public Animation animation = new Animation();
	public Component() {
	}

	public boolean isVisible() {
		return true;
	}
	
	public int getHeight()
	{
		if (!isVisible()) {
			return 0;
		}
		return height;
	}
	public int getCurrentHeight() {
		return getHeight();
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}
	
	public ClickGuiTab getParent()
	{
		return parent;
	}
	
	public void setParent(ClickGuiTab parent)
	{
		this.parent = parent;
	}

	public abstract void update(int offset, double mouseX, double mouseY);
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		return false;
	}
	public double currentOffset = 0;

	public double getTextOffsetY() {
		return (defaultHeight - Wrapper.mc.textRenderer.fontHeight) / 2D + (ClickGui.INSTANCE.maxFill.getValue() ? 2 : 1);
	}

	public static double animate(double current, double endPoint, double speed) {
		if (speed >= 1) return endPoint;
		if (speed == 0) return current;
		return AnimateUtil.thunder(current, endPoint, speed);
	}
	public static void sound() {
		if (ClickGui.INSTANCE.sound.getValue() && !Module.nullCheck()) {
			mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.BLOCKS, (float) 100f, 1.9f);
		}
	}
}
