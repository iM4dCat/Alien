package dev.luminous.mod.modules.settings.impl;

import dev.luminous.Alien;
import dev.luminous.core.impl.ModuleManager;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.modules.impl.client.Colors;
import dev.luminous.mod.modules.settings.Setting;

import java.awt.*;
import java.util.function.BooleanSupplier;

public class ColorSetting extends Setting {
	public boolean isRainbow = false;
	private Color value;
	private final Color defaultValue;
	public static final Timer timer = new Timer();
	private boolean defaultBooleanValue = false;
	public boolean injectBoolean = false;
	public boolean booleanValue = false;
	public final float effectSpeed = 4;
	public ColorSetting(String name, Color defaultValue) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
		this.value = defaultValue;
		this.defaultValue = defaultValue;
	}

	public ColorSetting(String name, Color defaultValue, BooleanSupplier visibilityIn) {
		super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
		this.value = defaultValue;
		this.defaultValue = defaultValue;
	}

	public ColorSetting(String name, int defaultValue) {
		this(name, new Color(defaultValue, true));
	}

	public ColorSetting(String name, int defaultValue, BooleanSupplier visibilityIn) {
		this(name, new Color(defaultValue, true), visibilityIn);
	}

	public final Color getValue() {
		if (isRainbow) {
			if (Colors.INSTANCE.clientColor.booleanValue) {
				Color preColor = Colors.INSTANCE.clientColor.getValue();
				setValue(new Color(preColor.getRed(), preColor.getGreen(), preColor.getBlue(), value.getAlpha()));
			} else {
				float[] HSB = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
				Color preColor = Color.getHSBColor(((float) timer.getPassedTimeMs() * 0.36f * effectSpeed / 20f) % 361 / 360, HSB[1], HSB[2]);
				setValue(new Color(preColor.getRed(), preColor.getGreen(), preColor.getBlue(), value.getAlpha()));
			}
		}
		return this.value;
	}
	
	public final void setValue(Color value) {
		this.value = value;
	}

	public final void setValue(int value) {
		this.value = new Color(value, true);
	}
	public final void setRainbow(boolean rainbow) {
		this.isRainbow = rainbow;
	}
	public ColorSetting injectBoolean(boolean value) {
		injectBoolean = true;
		defaultBooleanValue = value;
		booleanValue = value;
		return this;
	}
	@Override
	public void loadSetting() {
		this.value = new Color(Alien.CONFIG.getInt(this.getLine(), defaultValue.getRGB()), true);
		this.isRainbow = Alien.CONFIG.getBoolean(this.getLine() + "Rainbow");
		if (injectBoolean) {
			this.booleanValue = Alien.CONFIG.getBoolean(this.getLine() + "Boolean", defaultBooleanValue);
		}
	}
}
