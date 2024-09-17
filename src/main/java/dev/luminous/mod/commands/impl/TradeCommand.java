package dev.luminous.mod.commands.impl;

import dev.luminous.Alien;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.mod.commands.Command;

import java.util.ArrayList;
import java.util.List;

public class TradeCommand extends Command {

	public TradeCommand() {
		super("trade", "[name/reset/list] | [addItem/addBlock/addkey/removeItem/removeBlock/removekey] [name]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
        switch (parameters[0]) {
            case "reset" -> {
                Alien.TRADE.list.clear();
                CommandManager.sendChatMessage("§fItems list got reset");
                return;
            }
            case "list" -> {
                if (Alien.TRADE.list.isEmpty()) {
                    CommandManager.sendChatMessage("§fItems list is empty");
                    return;
                }

                for (String name : Alien.TRADE.list) {
                    CommandManager.sendChatMessage("§a" + name);
                }
                return;
            }
            case "addkey" -> {
                if (parameters.length == 2) {
                    Alien.TRADE.add(parameters[1]);
                    CommandManager.sendChatMessage("§f" + parameters[1] + (Alien.TRADE.inWhitelist(parameters[1]) ? " §ahas been added" : " §chas been removed"));
                    return;
                }
                sendUsage();
                return;
            }
            case "addItem" -> {
                if (parameters.length == 2) {
                    Alien.TRADE.add("item.minecraft." + parameters[1]);
                    CommandManager.sendChatMessage("§f" + parameters[1] + (Alien.TRADE.inWhitelist("item.minecraft." +parameters[1]) ? " §ahas been added" : " §chas been removed"));
                    return;
                }
                sendUsage();
                return;
            }
            case "removeItem" -> {
                if (parameters.length == 2) {
                    Alien.TRADE.remove("item.minecraft." + parameters[1]);
                    CommandManager.sendChatMessage("§f" + parameters[1] + (Alien.TRADE.inWhitelist("item.minecraft." +parameters[1]) ? " §ahas been added" : " §chas been removed"));
                    return;
                }
                sendUsage();
                return;
            }
            case "addBlock" -> {
                if (parameters.length == 2) {
                    Alien.TRADE.add("block.minecraft." + parameters[1]);
                    CommandManager.sendChatMessage("§f" + parameters[1] + (Alien.TRADE.inWhitelist("item.minecraft." +parameters[1]) ? " §ahas been added" : " §chas been removed"));
                    return;
                }
                sendUsage();
                return;
            }
            case "removeBlock" -> {
                if (parameters.length == 2) {
                    Alien.TRADE.remove("block.minecraft." + parameters[1]);
                    CommandManager.sendChatMessage("§f" + parameters[1] + (Alien.TRADE.inWhitelist("item.minecraft." +parameters[1]) ? " §ahas been added" : " §chas been removed"));
                    return;
                }
                sendUsage();
                return;
            }
            case "removekey" -> {
                if (parameters.length == 2) {
                    Alien.TRADE.remove(parameters[1]);
                    CommandManager.sendChatMessage("§f" + parameters[1] + (Alien.TRADE.inWhitelist(parameters[1]) ? " §ahas been added" : " §chas been removed"));
                    return;
                }
                sendUsage();
                return;
            }
        }

        if (parameters.length == 1) {
			CommandManager.sendChatMessage("§f" + parameters[0] + (Alien.TRADE.inWhitelist(parameters[0]) ? " §ais in whitelist" : " §cisn't in whitelist"));
			return;
		}

		sendUsage();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		if (count == 1) {
			String input = seperated.get(seperated.size() - 1).toLowerCase();
			List<String> correct = new ArrayList<>();
            List<String> list = List.of("addItem", "addBlock", "addkey", "removeItem", "removeBlock", "removekey", "list", "reset");
			for (String x : list) {
				if (input.equalsIgnoreCase(Alien.PREFIX + "trade") || x.toLowerCase().startsWith(input)) {
					correct.add(x);
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
