package dev.luminous.mod.commands.impl;

import dev.luminous.Alien;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.core.impl.ConfigManager;
import dev.luminous.mod.commands.Command;

import java.util.List;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super("reload", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§fReloading..");
		Alien.CONFIG = new ConfigManager();
		Alien.PREFIX = Alien.CONFIG.getString("prefix", Alien.PREFIX);
		Alien.CONFIG.loadSettings();
		Alien.XRAY.read();
		Alien.TRADE.read();
		Alien.FRIEND.read();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
