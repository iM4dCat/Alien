package dev.luminous.mod.modules.impl.combat;

import dev.luminous.mod.modules.impl.client.AntiCheat;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.eventbus.EventPriority;
import dev.luminous.api.events.impl.RotateEvent;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.modules.Module;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;

import java.util.Map;


public class AutoEXP extends Module {


    public static AutoEXP INSTANCE;
    private final SliderSetting delay =
            add(new SliderSetting("Delay", 3, 0, 5));
    public final BooleanSetting down =
            add(new BooleanSetting("Down", true));
    public final BooleanSetting onlyBroken =
            add(new BooleanSetting("OnlyBroken", true));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("OnlyGround", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final Timer delayTimer = new Timer();

    public AutoEXP() {
        super("AutoEXP", Category.Combat);
        setChinese("自动经验瓶");
        INSTANCE = this;
    }

    private boolean throwing = false;

    @Override
    public void onDisable() {
        throwing = false;
    }
    boolean rotation = false;
    int exp = 0;
    @Override
    public void onUpdate() {
        throwing = checkThrow();
        if (rotation && isThrow() && delayTimer.passedMs(delay.getValueInt() * 20L) && (!onlyGround.getValue() || mc.player.isOnGround())) {
            exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE) - 1;
            throwExp();
        }
    }

    @Override
    public void onEnable() {
        rotation = !down.getValue();
        if (nullCheck()) {
            disable();
            return;
        }
        exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE);
    }

    @Override
    public String getInfo() {
        return String.valueOf(exp);
    }

    public void throwExp() {
        int oldSlot = mc.player.getInventory().selectedSlot;
        int newSlot;
        if (inventory.getValue() && (newSlot = InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE)) != -1) {
            InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            EntityUtil.swingHand(Hand.MAIN_HAND, AntiCheat.INSTANCE.swingMode.getValue());
            InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
            EntityUtil.syncInventory();
            delayTimer.reset();
        } else if ((newSlot = InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE)) != -1) {
            InventoryUtil.switchToSlot(newSlot);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            EntityUtil.swingHand(Hand.MAIN_HAND, AntiCheat.INSTANCE.swingMode.getValue());
            InventoryUtil.switchToSlot(oldSlot);
            delayTimer.reset();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void RotateEvent(RotateEvent event) {
        if (!down.getValue()) return;
        if (isThrow()) {
            event.setPitch(88);
            rotation = true;
        }
    }

    public boolean isThrow() {
        return throwing;
    }

    public boolean checkThrow() {
        if (isOff()) return false;
/*        if (mc.currentScreen instanceof ChatScreen) return false;*/
        if (mc.currentScreen != null) return false;
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return false;
        }
        if (InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE) == -1 && (!inventory.getValue() || InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE) == -1))
            return false;
        if (onlyBroken.getValue()) {
            DefaultedList<ItemStack> armors = mc.player.getInventory().armor;
            for (ItemStack armor : armors) {
                if (armor.isEmpty()) continue;
                if (EntityUtil.getDamagePercent(armor) >= 100) continue;
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(armor);
                for (Enchantment enchantment : enchantments.keySet()) {
                    if (enchantment == Enchantments.MENDING) {
                        return true;
                    }
                }
                return false;
            }
        } else {
            return true;
        }
        return false;
    }
}
