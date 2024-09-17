package dev.luminous.mod.commands.impl.irc;

import dev.luminous.mod.commands.Command;
import dev.luminous.mod.irc.IRCManager;
import dev.luminous.mod.irc.IRCService;
import dev.luminous.mod.modules.impl.client.IRC;

import java.util.List;

public class WhisperCommand extends Command {

    public WhisperCommand() {
        super("w", "[id or name] [message]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length < 2) {
            sendUsage();
            return;
        }
        String idOrName = parameters[0];
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (int index = 1; index < parameters.length; index++) {
            String s = parameters[index];
            if (first) {
                builder.append(s);
                first = false;
            } else builder.append(" ").append(s);
        }
        if (IRC.instance.isOn()) {
            IRCManager.whisper(IRCService.connection, idOrName, builder.toString());
        }
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}
