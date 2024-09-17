package dev.luminous.mod.commands.impl;

import dev.luminous.Alien;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.mod.commands.Command;
import dev.luminous.mod.modules.Module;

import java.util.ArrayList;
import java.util.List;

public class ToggleCommand extends Command {

	public ToggleCommand() {
		super("toggle", "[module]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		String moduleName = parameters[0];
		Module module = Alien.MODULE.getModuleByName(moduleName);
		if (module == null) {
			CommandManager.sendChatMessage("§cUnknown module");
			return;
		}
		module.toggle();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		if (count == 1) {
			String input = seperated.get(seperated.size() - 1).toLowerCase();
			List<String> correct = new ArrayList<>();
			for (Module x : Alien.MODULE.modules) {
				if (input.equalsIgnoreCase(Alien.PREFIX + "toggle") || x.getName().toLowerCase().startsWith(input)) {
					correct.add(x.getName());
				}
			}
			int numCmds = correct.size();
			String[] commands = new String[numCmds];

			int i = 0;
			for (String x : correct) {
				commands[i++] = x;
			}

			return commands;
		}
		return null;
	}
}
