package dev.luminous.mod.modules.settings.impl;

import dev.luminous.Alien;
import dev.luminous.core.impl.ConfigManager;
import dev.luminous.core.impl.ModuleManager;
import dev.luminous.mod.modules.settings.Setting;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SelectionManager;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

import static dev.luminous.api.utils.Wrapper.mc;

public class SliderSetting extends Setting {
	private double value;
	private final double defaultValue;
	private final double minValue;
	private final double maxValue;
	private final double increment;
	private String suffix = "";
	public boolean isListening = false;
	public boolean update = false;
	public Runnable task = null;
	public boolean injectTask = false;
	public SliderSetting(String name, double value, double min, double max, double increment) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
		this.value = value;
		this.defaultValue = value;
		this.minValue = min;
		this.maxValue = max;
		this.increment = increment;
	}

	public SliderSetting(String name, double value, double min, double max) {
		this(name, value, min, max, 0.1);
	}

	public SliderSetting(String name, int value, int min, int max) {
		this(name, value, min, max, 1);
	}


	public SliderSetting(String name, double value, double min, double max, double increment, BooleanSupplier visibilityIn) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
		this.value = value;
		this.defaultValue = value;
		this.minValue = min;
		this.maxValue = max;
		this.increment = increment;
	}

	public SliderSetting(String name, double value, double min, double max, BooleanSupplier visibilityIn) {
		this(name, value, min, max, 0.1, visibilityIn);
	}

	public SliderSetting(String name, int value, int min, int max, BooleanSupplier visibilityIn) {
		this(name, value, min, max, 1, visibilityIn);
	}

	public final double getValue() {
		return this.value;
	}

	public final float getValueFloat() {
		return (float) this.value;
	}

	public final int getValueInt() {
		return (int) this.value;
	}

	public final void setValue(double value) {
		if (injectTask) {
			task.run();
		}
		this.value = Math.round(value / increment) * increment;
	}

	public final double getMinimum() {
		return this.minValue;
	}

	public final double getMaximum() {
		return this.maxValue;
	}

	public final double getIncrement() {
		return increment;
	}

	public final double getRange() {
		return this.maxValue - this.minValue;
	}
	public SliderSetting setSuffix(String suffix) {
		this.suffix = suffix;
		return this;
	}
	public String getSuffix() {
		return suffix;
	}
	@Override
	public void loadSetting() {
		setValue(Alien.CONFIG.getFloat(this.getLine(), (float) this.defaultValue));
	}

	public String temp;

	public void setListening(boolean set) {
		isListening = set;
		if (isListening) {
			temp = String.valueOf(getValueFloat());
			current = this;
		}
	}

	public boolean isListening() {
		return isListening && current == this;
	}

	public void keyType(int keyCode) {
		switch (keyCode) {
			case GLFW.GLFW_KEY_V -> {
				if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
					temp = temp + SelectionManager.getClipboard(mc);
				}
			}
			case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
				if (temp.isEmpty()) {
					setValue(defaultValue);
				} else if (ConfigManager.isFloat(temp)) {
					setValue(Float.parseFloat(temp));
					update = true;
				}
				setListening(false);
			}
			case GLFW.GLFW_KEY_BACKSPACE -> temp = removeLastChar(temp);
		}
	}
	public SliderSetting injectTask(Runnable task) {
		this.task = task;
		injectTask = true;
		return this;
	}
	public void charType(char c) {
		temp = temp + c;
	}
	public static String removeLastChar(String str) {
		if (!str.isEmpty()) {
			return str.substring(0, str.length() - 1);
		}
		return "";
	}
}
