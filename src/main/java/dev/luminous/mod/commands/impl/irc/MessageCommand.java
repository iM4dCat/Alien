package dev.luminous.mod.commands.impl.irc;

import dev.luminous.mod.commands.Command;
import dev.luminous.mod.irc.IRCManager;
import dev.luminous.mod.irc.IRCService;
import dev.luminous.mod.modules.impl.client.HUD;
import dev.luminous.mod.modules.impl.client.IRC;

import java.util.Arrays;
import java.util.List;

public class MessageCommand extends Command {

    public MessageCommand() {
        super("irc", "[message]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            sendUsage();
            return;
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String s : parameters) {
            if (first) {
                builder.append(s);
                first = false;
            } else builder.append(" ").append(s);
        }
        if (IRC.instance.isOn()) {
            IRCManager.message(IRCService.connection, builder.toString());
        }
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }

}
