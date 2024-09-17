package dev.luminous.core.impl;

import com.google.common.base.Splitter;
import dev.luminous.Alien;
import dev.luminous.core.Manager;
import dev.luminous.mod.gui.clickgui.tabs.ClickGuiTab;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.HUD;
import dev.luminous.mod.modules.impl.client.ModuleList;
import dev.luminous.mod.modules.settings.Setting;
import dev.luminous.mod.modules.settings.impl.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ConfigManager extends Manager {
	public static File options = getFile("options.txt");
	private final Hashtable<String, String> settings = new Hashtable<>();

	public ConfigManager() {
		readSettings();
	}

	public static void resetModule() {
		for (Module module : Alien.MODULE.modules) {
			module.setState(false);
		}
	}
	public void loadSettings() {
		for (Module module : Alien.MODULE.modules) {
			for (Setting setting : module.getSettings()) {
				setting.loadSetting();
			}
			module.setState(Alien.CONFIG.getBoolean(module.getName() + "_state", module instanceof HUD || module instanceof ModuleList));
		}
	}
	public void saveSettings() {
		PrintWriter printwriter = null;
		try {
			printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(options), StandardCharsets.UTF_8));

			printwriter.println("prefix:" + Alien.PREFIX);

			for (ClickGuiTab tab : Alien.GUI.tabs) {
				printwriter.println(tab.getTitle() + "_x:" + tab.getX());
				printwriter.println(tab.getTitle() + "_y:" + tab.getY());
			}
			printwriter.println("armor_x:" + Alien.GUI.armorHud.getX());
			printwriter.println("armor_y:" + Alien.GUI.armorHud.getY());

			for (Module module : Alien.MODULE.modules) {
				for (Setting setting : module.getSettings()) {
					if (setting instanceof BooleanSetting bs) {
						printwriter.println(bs.getLine() + ":" + bs.getValue());
					}else if (setting instanceof SliderSetting ss) {
						printwriter.println(ss.getLine() + ":" + ss.getValue());
					} else if (setting instanceof BindSetting bs) {
						printwriter.println(bs.getLine() + ":" + bs.getKey());
						printwriter.println(bs.getLine() + "_hold" + ":" + bs.isHoldEnable());
					} else if (setting instanceof EnumSetting es) {
						printwriter.println(es.getLine() + ":" + es.getValue().name());
					} else if (setting instanceof ColorSetting cs) {
						printwriter.println(cs.getLine() + ":" + cs.getValue().getRGB());
						printwriter.println(cs.getLine() + "Rainbow:" + cs.isRainbow);
						if (cs.injectBoolean) {
							printwriter.println(cs.getLine() + "Boolean:" + cs.booleanValue);
						}
					} else if (setting instanceof StringSetting ss) {
						printwriter.println(ss.getLine() + ":" + ss.getValue());
					}
				}
				printwriter.println(module.getName() + "_state:" + module.isOn());
			}
		} catch (Exception exception) {
			System.out.println("[" + Alien.NAME + "] Failed to save settings");
		} finally {
			IOUtils.closeQuietly(printwriter);
		}
	}

	public void readSettings() {
		final Splitter COLON_SPLITTER = Splitter.on(':');
		try {
			if (!options.exists()) {
				return;
			}
			List<String> list = IOUtils.readLines(new FileInputStream(options), StandardCharsets.UTF_8);
			for (String s : list) {
				try {
					Iterator<String> iterator = COLON_SPLITTER.limit(2).split(s).iterator();
					settings.put(iterator.next(), iterator.next());
				} catch (Exception var10) {
					System.out.println("Skipping bad option: " + s);
				}
			}
			//KeyBinding.updateKeysByCode();
		} catch (Exception exception) {
			System.out.println("[" + Alien.NAME + "] Failed to load settings");
		}
	}

	public static boolean isInteger(final String str) {
		final Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}

	public static boolean isFloat(String str) {
		String pattern = "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";
		return str.matches(pattern);
	}
	public int getInt(String setting, int defaultValue) {
		String s = settings.get(setting);
		if(s == null || !isInteger(s)) return defaultValue;
		return Integer.parseInt(s);
	}

	public float getFloat(String setting, float defaultValue) {
		String s = settings.get(setting);
		if (s == null || !isFloat(s)) return defaultValue;
		return Float.parseFloat(s);
	}
	public boolean getBoolean(String setting) {
		String s = settings.get(setting);
		return Boolean.parseBoolean(s);
	}

	public boolean getBoolean(String setting, boolean defaultValue) {
		if (settings.get(setting) != null) {
			String s = settings.get(setting);
			return Boolean.parseBoolean(s);
		} else {
			return defaultValue;
		}
	}

	public String getString(String setting) {
		return settings.get(setting);
	}

	public String getString(String setting, String defaultValue) {
		if (settings.get(setting) == null) {
			return defaultValue;
		}
		return settings.get(setting);
	}
}