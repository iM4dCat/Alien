package dev.luminous.mod.commands.impl;

import dev.luminous.Alien;
import dev.luminous.core.Manager;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.core.impl.ConfigManager;
import dev.luminous.mod.commands.Command;

import java.util.List;

public class LoadCommand extends Command {

	public LoadCommand() {
		super("load", "[config]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		CommandManager.sendChatMessage("§fLoading..");
		ConfigManager.options = Manager.getFile(parameters[0] + ".cfg");
		Alien.CONFIG = new ConfigManager();
		Alien.PREFIX = Alien.CONFIG.getString("prefix", Alien.PREFIX);
		Alien.CONFIG.loadSettings();
        ConfigManager.options = Manager.getFile("options.txt");
		Alien.save();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
