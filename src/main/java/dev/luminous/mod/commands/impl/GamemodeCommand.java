package dev.luminous.mod.commands.impl;

import dev.luminous.mod.commands.Command;
import net.minecraft.world.GameMode;

import java.util.List;

public class GamemodeCommand extends Command {

	public GamemodeCommand() {
		super("gamemode", "[gamemode]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		String moduleName = parameters[0];
		if (moduleName.equalsIgnoreCase("survival")) {
			mc.interactionManager.setGameMode(GameMode.SURVIVAL);
		} else if (moduleName.equalsIgnoreCase("creative")) {
			mc.interactionManager.setGameMode(GameMode.CREATIVE);
		} else if (moduleName.equalsIgnoreCase("adventure")) {
			mc.interactionManager.setGameMode(GameMode.ADVENTURE);
		} else if (moduleName.equalsIgnoreCase("spectator")) {
			mc.interactionManager.setGameMode(GameMode.SPECTATOR);
		}
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		if (count == 1) {
			return new String[]{"survival", "creative", "adventure", "spectator"};
		}
		return null;
	}
}
