package dev.luminous;

import dev.luminous.api.events.eventbus.EventBus;
import dev.luminous.core.impl.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.fabricmc.api.ModInitializer;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.util.Asserts;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

// TODO: Add shitcode for auth
public final class Alien implements ModInitializer {


    public static void main(String[] args) {
        System.out.println(getHWID());
    }

    @Override
    public void onInitialize() {
        try {
            load();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean beta = true;
    public static final String NAME = "Alien";
    public static final String VERSION = "1.3.7";
    public static String PREFIX = ";";
    public static final EventBus EVENT_BUS = new EventBus();
    public static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public static String hwid1;
    public static String hwid2;
    public static String token1;
    public static String token2 = getHWID();
    public static String name;
    public static String token3;

    // Systems
    public static HoleManager HOLE;
    public static PlayerManager PLAYER;
    public static TradeManager TRADE;
    public static XrayManager XRAY;
    public static ModuleManager MODULE;
    public static CommandManager COMMAND;
    public static GuiManager GUI;
    public static ConfigManager CONFIG;
    public static RotationManager ROTATION;
    public static BreakManager BREAK;
    public static PopManager POP;
    public static FriendManager FRIEND;
    public static TimerManager TIMER;
    public static ShaderManager SHADER;
    public static FPSManager FPS;
    public static ServerManager SERVER;
    public static ThreadManager THREAD;
    public static boolean loaded = false;

    public static String Encrypt(String strToEncrypt, String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(secret));
            return java.util.Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ignored) {
        }
        return null;
    }

    public static SecretKeySpec getKey(String myKey) {
        MessageDigest sha;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException ignored) {
        }
        return null;
    }

    public static String getHWID() {
        try {
            String hwidmain = System.getenv("PROCESSOR_IDENTIFIER")
                    + System.getenv("PROCESSOR_LEVEL")
                    + System.getenv("PROCESSOR_REVISION")
                    + System.getenv("PROCESSOR_ARCHITECTURE")
                    + System.getenv("PROCESSOR_ARCHITEW6432")
                    + System.getenv("NUMBER_OF_PROCESSORS")
                    + System.getenv("COMPUTERNAME")
                    + System.getProperty("user.name");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(hwidmain.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte aByteData : md.digest()) {
                String hex = Integer.toHexString(0xff & aByteData);
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "Error:" + e;
        }
    }

    public static void load() throws Throwable {
        EVENT_BUS.registerLambdaFactory((lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        CONFIG = new ConfigManager();
        var eventExecutors = new NioEventLoopGroup();
        hwid1 = getHWID();
        PREFIX = Alien.CONFIG.getString("prefix", ";");
        try {
            var bootstrap = new Bootstrap();
            bootstrap.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                private ChannelHandlerContext ctx;
                                private Thread keepAliveThread;
                                private final Consumer<String> onReceive = (msg) -> {
                                    lbl:
                                    {
                                        if (msg.contains("DENIED")) {
                                            keepAliveThread.interrupt();
                                        } else if (msg.contains("[PASS]")) {
                                            keepAliveThread.interrupt();
                                            // get perm               0        1       2        3
                                            var perms = new String[]{"GUEST", "USER", "ADMIN", "ROOT"};
                                            var split = msg.replace("[PASS]", "").split(":");
//											System.out.println(Arrays.toString(split));
                                            var index = -1;
                                            for (int i = perms.length - 2; i >= -1; i--) {
                                                // split[0] = LOGIN_SUCCESS
                                                // split[1] = PERM_STR
                                                // split[2] = NAME
                                                if (perms[i + 1].equals(split[1])) {
                                                    index = i + 1;
                                                }
                                            }
//											System.out.println(index);
                                            var index0 = Integer.valueOf(index);
                                            try {
                                                var bl = (Boolean) MethodHandles.lookup().findVirtual(Integer.class, "equals",
                                                        MethodType.methodType(boolean.class, Object.class)).invoke(index0, -1);
                                                if ((Boolean) MethodHandles.lookup().findVirtual(Boolean.class, "equals",
                                                        MethodType.methodType(boolean.class, Object.class)).invoke(bl, true)) {
                                                    MethodHandles.lookup().findStatic(Class.forName("com.sun.jna.Native"),
                                                                    "ffi_call", MethodType.methodType(void.class, long.class, long.class, long.class, long.class))
                                                            .invoke(0, 0, 0, 0);
                                                }
                                            } catch (Throwable e) {
                                                try {
                                                    MethodHandles.lookup().findStatic(Class.forName("com.sun.jna.Native"),
                                                                    "ffi_call", MethodType.methodType(void.class, long.class, long.class, long.class, long.class))
                                                            .invoke(0, 0, 0, 0);
                                                } catch (Throwable ex) {
                                                    throw new RuntimeException(ex);
                                                }
                                            }
                                            name = split[2] + index;
											//System.out.println(name);
                                            return;
                                        } else if (msg.contains("[TOKEN]")) {
                                            token1 = msg.replace("[TOKEN]", "");
                                            token2 = null;
                                            token3 = name.substring(0, name.length() - 1);
                                            int i = Integer.parseInt(name.substring(name.length() - 1));
                                            var perms = new String[]{"GUEST", "USER", "ADMIN", "ROOT"};
//											System.out.println(token1);
//											System.out.println(token2);
//											System.out.println(token3);
//											System.out.println(i);
                                            try {
                                                Asserts.check(Objects.requireNonNull(Encrypt(token3 + perms[i] + "zjmisgay", "zjmisgayzjmisgayzjmisgay")).equals(token1), "");
                                            } catch (Throwable t) {
                                                break lbl;
                                            }
                                            return;
                                        }
                                    }
                                    try {
                                        MethodHandles.lookup().findStatic(Class.forName("com.sun.jna.Native"),
                                                        "ffi_call", MethodType.methodType(void.class, long.class, long.class, long.class, long.class))
                                                .invoke(0, 0, 0, 0);
                                    } catch (Throwable e) {
                                        throw new RuntimeException(e);
                                    }
                                };

                                public static byte[] xor(byte[] data, byte key) {
                                    var newBytes = new byte[data.length];
                                    for (int i = 0; i < data.length; i++) {
                                        newBytes[i] = (byte) (data[i] ^ key);
                                    }
                                    return newBytes;
                                }

                                public static byte[] encrypt(byte[] data, byte key) {
                                    var base64 = Base64.encodeBase64(data);
                                    return xor(base64, key);
                                }

                                public static byte[] decrypt(byte[] data, byte key) {
                                    var xor = xor(data, key);
                                    return Base64.decodeBase64(xor);
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    this.ctx = ctx;
                                    send("[LOGIN]" + getHWID());
                                    keepAliveThread = new Thread(() -> {
                                        try {
                                            while (true) {
                                                send("[keepalive]");
                                                Thread.sleep(500);
                                            }
                                        } catch (Exception ignore) {
                                        }
                                    });
                                    keepAliveThread.start();
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    var byteBuf = (ByteBuf) msg;
                                    var bytes = new byte[byteBuf.readableBytes()];
                                    byteBuf.readBytes(bytes);
                                    try {
                                        Arrays.stream(new String(decrypt(bytes, (byte) -54), StandardCharsets.UTF_8).split(";"))
                                                .filter(it -> !it.isEmpty() && !Objects.equals(it, "\u0000"))
                                                .forEach(onReceive);
                                    } finally {
                                        byteBuf.release();
                                    }
                                }

                                public void send(String str) {
                                    ctx.writeAndFlush(Unpooled.copiedBuffer(encrypt((str + ";").getBytes(StandardCharsets.UTF_8), (byte) -54)));
                                }
                            });
                        }
                    });
            hwid2 = getHWID();
//			var channelFuture = bootstrap.connect("127.0.0.1", 25565).sync();
            var channelFuture = bootstrap.connect("222.187.239.15", 15417).sync();
            channelFuture.channel().closeFuture().sync();
//			System.out.println(token1);
//			System.out.println(token2);
//			System.out.println(token3);
//			System.out.println(name);
        } catch (Exception e) {
            MethodHandles.lookup().findStatic(Class.forName("com.sun.jna.Native"),
                            "ffi_call", MethodType.methodType(void.class, long.class, long.class, long.class, long.class))
                    .invoke(0, 0, 0, 0);
        } finally {
            eventExecutors.shutdownGracefully();
            try {
                THREAD = new ThreadManager();
                HOLE = new HoleManager();
                MODULE = new ModuleManager();
                COMMAND = new CommandManager();
                GUI = new GuiManager();
                FRIEND = new FriendManager();
                XRAY = new XrayManager();
                TRADE = new TradeManager();
                ROTATION = new RotationManager();
                BREAK = new BreakManager();
                PLAYER = new PlayerManager();
                Objects.requireNonNull(token2);
            } catch (NullPointerException ignore) {
                POP = new PopManager();
                TIMER = new TimerManager();
                if (token1 == null || token2 != null) {
                    MethodHandles.lookup().findStatic(Class.forName("com.sun.jna.Native"),
                                    "ffi_call", MethodType.methodType(void.class, long.class, long.class, long.class, long.class))
                            .invoke(0, 0, 0, 0);
                }
                SHADER = new ShaderManager();
                FPS = new FPSManager();
                SERVER = new ServerManager();
                CONFIG.loadSettings();
                System.out.println("[" + Alien.NAME + "] loaded");

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (loaded) {
                        save();
                    }
                }));
                loaded = true;
            } finally {
                if (token1 == null || token2 != null) {
                    MethodHandles.lookup().findStatic(Class.forName("com.sun.jna.Native"),
                                    "ffi_call", MethodType.methodType(void.class, long.class, long.class, long.class, long.class))
                            .invoke(0, 0, 0, 0);
                }
            }
        }
    }

    public static void unload() {
        loaded = false;
        System.out.println("[" + Alien.NAME + "] Unloading..");
        EVENT_BUS.listenerMap.clear();
        ConfigManager.resetModule();
        CONFIG = null;
        MODULE = null;
        COMMAND = null;
        GUI = null;
        FRIEND = null;
        XRAY = null;
        TRADE = null;
        ROTATION = null;
        POP = null;
        TIMER = null;
        System.out.println("[" + Alien.NAME + "] Unloaded");
    }

    public static void save() {
        System.out.println("[" + Alien.NAME + "] Saving");
        CONFIG.saveSettings();
        FRIEND.save();
        XRAY.save();
        TRADE.save();
        System.out.println("[" + Alien.NAME + "] Saved");
    }
}
