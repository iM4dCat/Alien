package dev.luminous.mod.modules.impl.misc;

import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.HashMap;

public class AutoQueue extends Module {
    public static HashMap<String, String> asks = new HashMap<>(){
        {
            put("红石火把", "15");
            put("猪被闪电", "僵尸猪人");
            put("小箱子能", "27");
            put("开服年份", "2020");
            put("定位末地遗迹", "0");
            put("爬行者被闪电", "高压爬行者");
            put("大箱子能", "54");
            put("羊驼会主动", "不会");
            put("无限水", "3");
            put("挖掘速度最快", "金镐");
            put("凋灵死后", "下界之星");
            put("苦力怕的官方", "爬行者");
            put("南瓜的生长", "不需要");
            put("定位末地", "0");
        }
    };

    public AutoQueue() {
        super("AutoQueue", Category.Misc);
        setChinese("自动答题");
    }
    private final BooleanSetting queueCheck = add(new BooleanSetting("QueueCheck", true));

    public static boolean inQueue = false;
    @Override
    public void onUpdate() {
        if (nullCheck()) {
            inQueue = false;
            return;
        }
        inQueue = InventoryUtil.findItem(Items.COMPASS) != -1;
    }

    @Override
    public void onDisable() {
        inQueue = false;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (!inQueue && queueCheck.getValue()) return;
        if (e.getPacket() instanceof GameMessageS2CPacket packet) {
            for (String key : asks.keySet()) {
                if (packet.content().getString().contains(key)) {
                    String[] abc = new String[]{"A", "B", "C"};
                    for (String s : abc) {
                        if (packet.content().getString().contains(s + "." + asks.get(key))) {
                            mc.getNetworkHandler().sendChatMessage(s.toLowerCase());
                            return;
                        }
                    }
                }
            }
        }
    }
}
