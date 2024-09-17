package dev.luminous.mod.commands.impl.irc;

import dev.luminous.mod.commands.Command;
import dev.luminous.mod.irc.IRCService;
import dev.luminous.mod.modules.impl.client.IRC;

import java.util.List;

public class IRCPingCommand extends Command {

    public IRCPingCommand() {
        super("ircping", "");
    }

    public static long lastTime = System.currentTimeMillis();

    @Override
    public void runCommand(String[] parameters) {
        if (IRC.instance.isOn()) {
            IRCService.connection.sendMessage("ab");
            lastTime = System.currentTimeMillis();
        }
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }

}
