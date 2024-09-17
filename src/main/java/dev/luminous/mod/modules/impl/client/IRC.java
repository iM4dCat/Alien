package dev.luminous.mod.modules.impl.client;

import dev.luminous.Alien;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.commands.impl.irc.OnlineCommand;
import dev.luminous.mod.irc.IRCManager;
import dev.luminous.mod.irc.IRCService;
import dev.luminous.mod.modules.Module;

import static dev.luminous.mod.irc.IRCDispatcher.BASIC_HEAD;
import static dev.luminous.mod.irc.IRCDispatcher.BASIC_KEEP_ALIVE;

public class IRC extends Module {

    public static IRC instance;
    private final Timer timer = new Timer();
    public static String playerName = "alien-anonymous";
    public static String appID = "115";

    public IRC() {
        super("IRC", Category.Client);
        instance = this;
        setChinese("在线聊天");
    }

    @Override
    public void onEnable() {
        if (IRCService.connection != null) {
            IRCManager.raw(IRCService.connection, "" + (char) BASIC_HEAD + (char) BASIC_KEEP_ALIVE);
        }
    }

    public static boolean sending = false;
    @Override
    public void onUpdate() {
        if (OnlineCommand.timer.passed(10000)) {
            OnlineCommand.timer.reset();
            sending = true;
            mc.getNetworkHandler().sendChatMessage(Alien.PREFIX + "online");
            sending = false;
        }
        if (timer.passed(10000)) {
            timer.reset();
            if (IRCService.connection != null) {
                if (mc.player != null) {
                    String name = mc.player.getName().getLiteralString();
                    if (name != null && !playerName.equals(name)) {
                        playerName = name;
                        IRCManager.name(IRCService.connection, IRC.playerName);
                    }
                }
                IRCManager.raw(IRCService.connection, "" + (char) BASIC_HEAD + (char) BASIC_KEEP_ALIVE);
            }
        }
    }

}
