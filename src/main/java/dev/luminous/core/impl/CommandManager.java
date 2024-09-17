package dev.luminous.core.impl;

import dev.luminous.Alien;
import dev.luminous.api.interfaces.IChatHudHook;
import dev.luminous.api.utils.Wrapper;
import dev.luminous.mod.commands.Command;
import dev.luminous.mod.commands.impl.*;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import net.minecraft.text.Text;

import java.util.HashMap;

public class CommandManager implements Wrapper {
    public static final String syncCode = "§)";
    private final HashMap<String, Command> commands = new HashMap<>();

    public CommandManager() {
        registerCommand(new AimCommand());
        registerCommand(new BindCommand());
        registerCommand(new BindsCommand());
        registerCommand(new ClipCommand());
        registerCommand(new FriendCommand());
        registerCommand(new XrayCommand());
        registerCommand(new GamemodeCommand());
        registerCommand(new LoadCommand());
        registerCommand(new PingCommand());
        registerCommand(new PrefixCommand());
        registerCommand(new RejoinCommand());
        registerCommand(new ReloadCommand());
        registerCommand(new ReloadAllCommand());
        registerCommand(new SaveCommand());
        registerCommand(new TeleportCommand());
        registerCommand(new TCommand());
        registerCommand(new ToggleCommand());
        registerCommand(new TradeCommand());
        registerCommand(new WatermarkCommand());
    }

    private void registerCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public Command getCommandBySyntax(String string) {
        return this.commands.get(string);
    }

    public HashMap<String, Command> getCommands() {
        return this.commands;
    }

    public void command(String[] commandIn) {

        // Get the command from the user's message. (Index 0 is Username)
        Command command = commands.get(commandIn[0].substring(Alien.PREFIX.length()).toLowerCase());

        // If the command does not exist, throw an error.
        if (command == null)
            sendChatMessage("§cInvalid Command");
        else {
            // Otherwise, create a new parameter list.
            String[] parameterList = new String[commandIn.length - 1];
            System.arraycopy(commandIn, 1, parameterList, 0, commandIn.length - 1);
            if (parameterList.length == 1 && parameterList[0].equals("help")) {
                command.sendUsage();
                return;
            }
            // Runs the command.
            command.runCommand(parameterList);
        }
    }
    public static void sendChatMessage(String message) {
        if (Module.nullCheck()) return;
        if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
            mc.inGameHud.getChatHud().addMessage(Text.of("§f[§b" + ClientSetting.INSTANCE.hackName.getValue() + "§f] " + message));
            return;
        }
        mc.inGameHud.getChatHud().addMessage(Text.of(syncCode + "§r" + ClientSetting.INSTANCE.hackName.getValue() + "§f " + message));
    }

    public static void sendChatMessageWidthId(String message, int id) {
        if (Module.nullCheck()) return;
        if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
            ((IChatHudHook) mc.inGameHud.getChatHud()).addMessage(Text.of("§f[§b" + ClientSetting.INSTANCE.hackName.getValue() + "§f] " + message), id);
            return;
        }
        ((IChatHudHook) mc.inGameHud.getChatHud()).addMessage(Text.of(syncCode + "§r" + ClientSetting.INSTANCE.hackName.getValue() + "§f " + message), id);
    }

    public static void sendChatMessageWidthIdNoSync(String message, int id) {
        if (Module.nullCheck()) return;
        ((IChatHudHook) mc.inGameHud.getChatHud()).addMessage(Text.of("§f" + message), id);
    }
}
