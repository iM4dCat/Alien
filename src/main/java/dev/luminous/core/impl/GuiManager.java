package dev.luminous.core.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.Alien;
import dev.luminous.api.utils.Wrapper;
import dev.luminous.api.utils.math.FadeUtils;
import dev.luminous.api.utils.render.Snow;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.gui.clickgui.components.impl.ModuleComponent;
import dev.luminous.mod.gui.clickgui.tabs.ClickGuiTab;
import dev.luminous.mod.gui.clickgui.tabs.Tab;
import dev.luminous.mod.gui.elements.ArmorHUD;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.ClickGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GuiManager implements Wrapper {

	public final ArrayList<ClickGuiTab> tabs = new ArrayList<>();
	public static final ClickGuiScreen clickGui = new ClickGuiScreen();
	public final ArmorHUD armorHud;
	public static Tab currentGrabbed = null;
	private int lastMouseX = 0;
	private int lastMouseY = 0;
	private int mouseX;
	private int mouseY;

	public GuiManager() {

		armorHud = new ArmorHUD();

		int xOffset = 30;
		for (Module.Category category : Module.Category.values()) {
			ClickGuiTab tab = new ClickGuiTab(category, xOffset, 50);
			for (Module module : Alien.MODULE.modules) {
				if (module.getCategory() == category) {
					ModuleComponent button = new ModuleComponent(tab, module);
					tab.addChild(button);
				}
			}
			tabs.add(tab);
			xOffset += tab.getWidth() + 5;
		}
	}
	
	public Color getColor() {
		return ClickGui.INSTANCE.color.getValue();
	}
	
	public void onUpdate() {
		if (isClickGuiOpen()) {
			for (ClickGuiTab tab : tabs) {
				tab.update(mouseX, mouseY);
			}
			armorHud.update(mouseX, mouseY);
		}
	}

	
	
	public void draw(int x, int y, DrawContext drawContext, float tickDelta) {
		MatrixStack matrixStack = drawContext.getMatrices();
		boolean mouseClicked = ClickGuiScreen.clicked;
		mouseX = x;
		mouseY = y;
		if (!mouseClicked) {
			currentGrabbed = null;
		}
		if (currentGrabbed != null) {
			currentGrabbed.moveWindow((lastMouseX - mouseX), (lastMouseY - mouseY));
		}
		this.lastMouseX = mouseX;
		this.lastMouseY = mouseY;
		RenderSystem.enableCull();
		matrixStack.push();
		//matrixStack.scale((float) ClickGui.size, (float) ClickGui.size, 1);
		armorHud.draw(drawContext, tickDelta, getColor());
		double quad = ClickGui.fade.ease(FadeUtils.Ease.In2);
		if (quad < 1) {
			switch (ClickGui.INSTANCE.mode.getValue()) {
				case Pull -> {
					quad = 1 - quad;
					matrixStack.translate(0, -100 * quad, 0);
				}
				case Scale -> matrixStack.scale((float) quad, (float) quad, 1);
			}
		}
		for (ClickGuiTab tab : tabs) {
			tab.draw(drawContext, tickDelta, getColor());
		}
		matrixStack.pop();
	}

	public boolean isClickGuiOpen() {
		return mc.currentScreen instanceof ClickGuiScreen;
	}

	public static final ArrayList<Snow> snows = new ArrayList<>(){
		{
			Random random = new Random();
			for (int i = 0; i < 100; ++i) {
				for (int y = 0; y < 3; ++y) {
					add(new Snow(25 * i, y * -50, random.nextInt(3) + 1, random.nextInt(2) + 1));
				}
			}
		}
	};
}
