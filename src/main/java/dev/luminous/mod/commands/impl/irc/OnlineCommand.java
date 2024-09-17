package dev.luminous.mod.commands.impl.irc;

import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.commands.Command;
import dev.luminous.mod.irc.IRCManager;
import dev.luminous.mod.irc.IRCService;
import dev.luminous.mod.modules.impl.client.IRC;

import java.util.List;

public class OnlineCommand extends Command {

    public OnlineCommand() {
        super("online", "");
    }

    public static boolean onlineCommand = false;
    public static Timer timer = new Timer();
    @Override
    public void runCommand(String[] parameters) {
        if (IRC.instance.isOn()) {
            if (!IRC.sending) onlineCommand = true;
            timer.reset();
            IRCManager.command(IRCService.connection, "/online");
        }
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }

}
