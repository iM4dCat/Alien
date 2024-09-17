package dev.luminous.mod.gui.clickgui.tabs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public abstract class Tab {
	public static final int defaultHeight = 15;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected MinecraftClient mc = MinecraftClient.getInstance();
	
	public abstract void update(double mouseX, double mouseY) ;
	
	public abstract void draw(DrawContext drawContext, float partialTicks, Color color);
	
	public void moveWindow(int x, int y) {
		this.x = this.x - x;
		this.y = this.y - y;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}

}
