package dev.luminous.mod.irc;

import dev.luminous.core.impl.CommandManager;
import dev.luminous.mod.commands.impl.irc.OnlineCommand;
import dev.luminous.mod.modules.impl.client.IRC;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.luminous.mod.irc.IRCDispatcher.*;

public class IRCManager {

    public static List<String> onlineAlienUser = new CopyOnWriteArrayList<>();
    public static void resolve(String str) {
        onlineAlienUser.clear();
        String[] strings = str.split(",");
        for (String s : strings) {
            if (s.contains("§b[Alien+]") || s.contains("§4[Tester]")) {
                String regex = "<(.*?)>";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    onlineAlienUser.add(matcher.group(1));
                }
            }
        }
    }

    public static void receiveServerMsg(String str) {
        if (str.contains("users in server") && !OnlineCommand.onlineCommand) {
            return;
        }
        if (str.contains("§b[Alien+]") || str.contains("§4[Tester]")) {
            resolve(str);
            if (!OnlineCommand.onlineCommand) {
                return;
            }
            OnlineCommand.onlineCommand = false;
        }
        if (IRC.instance.isOn()) CommandManager.ircRaw("§f[§6HyperIRC§f]§f " + str);
    }

    public static void receiveMessage(String from, String str) {
        if (IRC.instance.isOn()) CommandManager.ircRaw("§f[§6HyperIRC§f]§f" + str);
    }

    public static void receiveAnnouncement(String str) {
        if (IRC.instance.isOn()) CommandManager.ircRaw("§f[§6HyperIRC§f]§f[§aAnnouncement§f]§f" + str);
    }

    public static void receiveWhisper(String from, String str) {
        if (IRC.instance.isOn()) CommandManager.ircRaw("§f[§6HyperIRC§f]§d " + str);
    }

    public static void clientMessage(String message) {
        if (IRC.instance.isOn()) CommandManager.ircRaw("§f[§6HyperIRC§f]§c " + message);
    }

    public static void message(ConnectionHandler handler, String message) {
        raw(handler, (char) IRC_MESSAGE_C + message);
    }

    public static void announcement(ConnectionHandler handler, String announcement) {
        raw(handler, (char) IRC_ANNOUNCEMENT_C + announcement);
    }

    public static void whisper(ConnectionHandler handler, String idOrName, String message) {
        raw(handler, (char) IRC_WHISPER_C + idOrName + (char) 91 + (char) 93 + message);
    }

    public static void app(ConnectionHandler handler, String appID) {
        raw(handler, (char) IRC_APP + appID);
    }

    public static void name(ConnectionHandler handler, String name) {
        raw(handler, (char) IRC_NAME + name);
    }

    public static void channel(ConnectionHandler handler, String channel) {
        raw(handler, (char) IRC_CHANNEL + channel);
    }

    public static void command(ConnectionHandler handler, String command) {
        raw(handler, (char) IRC_COMMAND + command);
    }

    public static void raw(ConnectionHandler handler, String message) {
        handler.sendMessage((char) IRC_HEAD + message);
    }

}
