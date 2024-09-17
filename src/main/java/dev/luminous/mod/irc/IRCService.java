package dev.luminous.mod.irc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
import org.jetbrains.annotations.NotNull;

public class IRCService {

    public static ConnectionHandler connection = null;
    public static Thread handlerThread = null;
    public static boolean running = false;

    public static void start() {
        connect("irc.spartanb312.net", 7921);
    }

    @SuppressWarnings("ALL")
    public static void restart() {
        try {
            if (connection != null) connection.getChannel().close();
        } catch (Exception ignored) {
        }
        Thread handlerThread = IRCService.handlerThread;
        connect("irc.spartanb312.net", 7921);
        if (handlerThread != null) handlerThread.stop();
    }

    @SuppressWarnings("ALL")
    private static void connect(String ip, int port) {
        handlerThread = new Thread() {
            @Override
            public void run() {
                running = true;
                var group = new NioEventLoopGroup();
                try {
                    var bootstrap = new Bootstrap();
                    bootstrap.group(group)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<NioSocketChannel>() {
                                @Override
                                protected void initChannel(@NotNull NioSocketChannel ch) {
                                    var pipeline = ch.pipeline();
                                    ConnectionHandler connection = new ConnectionHandler(ch);
                                    IRCService.connection = connection;
                                    pipeline.addLast(connection);
                                    pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                                }
                            });
                    var channelFuture = bootstrap.connect(ip, port).sync();
                    channelFuture.channel().closeFuture().sync();
                    group.shutdownGracefully();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    group.shutdownGracefully();
                    IRCManager.clientMessage("IRC service stopped");
                    running = false;
                }
            }
        };
        handlerThread.start();
    }

}
