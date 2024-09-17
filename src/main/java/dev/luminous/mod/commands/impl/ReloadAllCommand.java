package dev.luminous.mod.commands.impl;

import dev.luminous.Alien;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.mod.commands.Command;

import java.util.List;

public class ReloadAllCommand extends Command {

	public ReloadAllCommand() {
		super("reloadall", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§fReloading..");
		Alien.unload();
        try {
            Alien.load();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
