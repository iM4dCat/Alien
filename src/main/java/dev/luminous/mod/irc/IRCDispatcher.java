package dev.luminous.mod.irc;

import io.netty.channel.ChannelHandlerContext;

public class IRCDispatcher {

    public static void dispatch(
            ChannelHandlerContext ctx,
            ConnectionHandler handler,
            String message
    ) {
        if (message.length() > 0) {
            char head = message.charAt(0);
            String remaining = message.substring(1);
            if (head == IRC_MESSAGE_S) {
                var data = remaining.replace("[]", "!split/").split("!split/");
                var from = data[0];
                var str = data[1];
                IRCManager.receiveMessage(from, str);
            } else if (head == IRC_ANNOUNCEMENT_S) {
                IRCManager.receiveAnnouncement(remaining);
            } else if (head == IRC_WHISPER_S) {
                var data = remaining.replace("[]", "!split/").split("!split/");
                var from = data[0];
                var str = data[1];
                IRCManager.receiveWhisper(from, str);
            } else if (head == IRC_SERVER_MSG) {
                IRCManager.receiveServerMsg(remaining);
            }
        }
    }

    // Head
    public static final int OFFSET = 'a';
    public static final int IRC_HEAD = OFFSET + 0x20;
    public static final int BASIC_HEAD = OFFSET;
    // Basic
    public static final int BASIC_KEEP_ALIVE = BASIC_HEAD + 0x1;
    // Client
    public static final int IRC_APP = IRC_HEAD + 0x100;
    public static final int IRC_CHANNEL = IRC_HEAD + 0x110;
    public static final int IRC_NAME = IRC_HEAD + 0x111;
    public static final int IRC_MESSAGE_C = IRC_HEAD + 0x120;
    public static final int IRC_ANNOUNCEMENT_C = IRC_HEAD + 0x121;
    public static final int IRC_WHISPER_C = IRC_HEAD + 0x130;
    public static final int IRC_COMMAND = IRC_HEAD + 0x140;
    // Server
    public static final int IRC_MESSAGE_S = IRC_HEAD + 0x200;
    public static final int IRC_ANNOUNCEMENT_S = IRC_HEAD + 0x201;
    public static final int IRC_WHISPER_S = IRC_HEAD + 0x202;
    public static final int IRC_SERVER_MSG = IRC_HEAD + 0x210;
}
