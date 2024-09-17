package dev.luminous.mod.modules.settings.impl;

import dev.luminous.Alien;
import dev.luminous.core.impl.ModuleManager;
import dev.luminous.mod.modules.settings.Setting;

import java.util.function.BooleanSupplier;

public class BooleanSetting extends Setting {
	public boolean parent = false;
	public boolean popped = false;
	public Runnable task = null;
	public boolean injectTask = false;
	private boolean value;
	public final boolean defaultValue;

	public BooleanSetting(String name, boolean defaultValue) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public BooleanSetting(String name, boolean defaultValue, BooleanSupplier visibilityIn) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public final boolean getValue() {
		return this.value;
	}
	
	public final void setValue(boolean value) {
		if (injectTask && value != this.value) {
			task.run();
		}
		this.value = value;
	}
	
	public final void toggleValue() {
		setValue(!value);
	}
	public final boolean isOpen() {
		if (parent) {
			return popped;
		} else {
			return true;
		}
	}
	@Override
	public void loadSetting() {
		this.value = Alien.CONFIG.getBoolean(this.getLine(), defaultValue);
	}

	public BooleanSetting setParent() {
		parent = true;
		return this;
	}

	public BooleanSetting injectTask(Runnable task) {
		this.task = task;
		injectTask = true;
		return this;
	}
}
