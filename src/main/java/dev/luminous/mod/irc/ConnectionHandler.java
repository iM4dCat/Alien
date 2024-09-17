package dev.luminous.mod.irc;

import dev.luminous.mod.commands.impl.irc.IRCPingCommand;
import dev.luminous.mod.modules.impl.client.IRC;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.jetbrains.annotations.NotNull;

public class ConnectionHandler extends ChannelInboundHandlerAdapter {

    private final NioSocketChannel channel;

    public ConnectionHandler(NioSocketChannel channel) {
        this.channel = channel;
    }

    public NioSocketChannel getChannel() {
        return channel;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        // Send APPID and name
        IRCManager.app(this, IRC.appID);
        IRCManager.name(this, IRC.playerName);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        String message = byteBuf.toString(CharsetUtil.UTF_8);
        String[] strBuffer = message.split("\n");
        for (String str : strBuffer) {
            // Dispatch
            if (str.length() > 1) {
                if (str.charAt(0) == (char) IRCDispatcher.IRC_HEAD) {
                    IRCDispatcher.dispatch(ctx, this, str.substring(1));
                } else if (str.charAt(0) == (char) IRCDispatcher.BASIC_HEAD) {
                    String ping = "IRC Server Ping: " + (System.currentTimeMillis() - IRCPingCommand.lastTime) / 2 + " ms";
                    IRCManager.clientMessage(ping);
                }
            }
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        // Reconnect
        if (IRC.instance.isOn()) {
            IRCManager.clientMessage("IRC service disconnected. Automatically reconnecting...");
            IRCService.restart();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Nothing here
    }

    public void sendMessage(String message) {
        if (IRCService.running) {
            try {
                channel.writeAndFlush(Unpooled.copiedBuffer(message + "\n", CharsetUtil.UTF_8));
            } catch (Exception exception) {
                exception.printStackTrace();
                if (IRC.instance.isOn()) {
                    IRCManager.clientMessage("IRC service disconnected. Automatically reconnecting...");
                    IRCService.restart();
                }
            }
        } else IRCService.restart();
    }

}
