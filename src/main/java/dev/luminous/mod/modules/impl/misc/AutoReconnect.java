package dev.luminous.mod.modules.impl.misc;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.ServerConnectBeginEvent;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.mod.modules.settings.impl.StringSetting;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class AutoReconnect extends Module {
    public final BooleanSetting rejoin = add(new BooleanSetting("Rejoin", true));
    public final SliderSetting delay =
            add(new SliderSetting("Delay", 5, 0, 20,.1).setSuffix("s"));
    public final BooleanSetting autoLogin = add(new BooleanSetting("AutoAuth", true));
    public final SliderSetting afterLoginTime =
            add(new SliderSetting("AfterLoginTime", 3, 0, 10,.1).setSuffix("s"));
    private final StringSetting password = add(new StringSetting("password", "123456"));
    public final BooleanSetting autoQueue = add(new BooleanSetting("AutoQueue", true));
    public final SliderSetting joinQueueDelay =
            add(new SliderSetting("JoinQueueDelay", 3, 0, 10,.1).setSuffix("s"));
    public Pair<ServerAddress, ServerInfo> lastServerConnection;

    public static AutoReconnect INSTANCE;
    public AutoReconnect() {
        super("AutoReconnect", Category.Misc);
        setChinese("自动重连");
        INSTANCE = this;
        Alien.EVENT_BUS.subscribe(new StaticListener());
    }
    private final Timer queueTimer = new Timer();
    private final Timer timer = new Timer();
    private boolean login = false;
    @Override
    public void onUpdate() {
        if (login && timer.passedS(afterLoginTime.getValue())) {
            mc.getNetworkHandler().sendChatCommand("login " + password.getValue());
            login = false;
        }
        if (autoQueue.getValue() && InventoryUtil.findItem(Items.COMPASS) != -1 && queueTimer.passedS(joinQueueDelay.getValue())) {
            InventoryUtil.switchToSlot(InventoryUtil.findItem(Items.COMPASS));
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            queueTimer.reset();
        }
     }

    @Override
    public void onLogin() {
        if (autoLogin.getValue()) {
            login = true;
            timer.reset();
        }
    }

    public boolean rejoin() {
        return isOn() && rejoin.getValue();
    }
    private class StaticListener {
        @EventHandler
        private void onGameJoined(ServerConnectBeginEvent event) {
            lastServerConnection = new ObjectObjectImmutablePair<>(event.address, event.info);
        }
    }
}