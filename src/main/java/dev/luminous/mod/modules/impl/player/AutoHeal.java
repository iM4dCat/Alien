package dev.luminous.mod.modules.impl.player;

import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.eventbus.EventPriority;
import dev.luminous.api.events.impl.RotateEvent;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.modules.Module;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.Map;


public class AutoHeal extends Module {


    public static AutoHeal INSTANCE;
    private final SliderSetting delay =
            add(new SliderSetting("Delay", 3, 0, 10));
    public final BooleanSetting down =
            add(new BooleanSetting("Down", true));
    private final BooleanSetting onlyDamaged =
            add(new BooleanSetting("OnlyDamaged", true));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("OnlyGround", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final Timer delayTimer = new Timer();

    public AutoHeal() {
        super("AutoHeal", Category.Player);
        setChinese("自动治疗");
        INSTANCE = this;
    }

    private boolean throwing = false;

    @Override
    public void onDisable() {
        throwing = false;
    }

    int exp = 0;
    @Override
    public void onUpdate() {
        throwing = checkThrow();
        if (isThrow() && delayTimer.passedMs(delay.getValueInt() * 20L) && (!onlyGround.getValue() || mc.player.isOnGround())) {
            exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE) - 1;
            throwExp();
        }
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        exp = getPotionCount();
    }

    @Override
    public String getInfo() {
        return String.valueOf(exp);
    }

    public void throwExp() {
        int oldSlot = mc.player.getInventory().selectedSlot;
        int newSlot;
        if (inventory.getValue() && (newSlot = findPotionInventorySlot()) != -1) {
            InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
            EntityUtil.syncInventory();
            delayTimer.reset();
        } else if ((newSlot = findPotion()) != -1) {
            InventoryUtil.switchToSlot(newSlot);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            InventoryUtil.switchToSlot(oldSlot);
            delayTimer.reset();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void RotateEvent(RotateEvent event) {
        if (!down.getValue()) return;
        if (isThrow()) event.setPitch(88);
    }

    public boolean isThrow() {
        return throwing;
    }

    public boolean checkThrow() {
        if (isOff()) return false;
        if (mc.currentScreen instanceof ChatScreen) return false;
        if (mc.currentScreen != null) return false;
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return false;
        }
        if (onlyDamaged.getValue() && mc.player.getHealth() >= 20) {
            return false;
        }
        return findPotion() != -1 || (inventory.getValue() && findPotionInventorySlot() != -1);
    }

    public static int getPotionCount() {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            ItemStack itemStack = entry.getValue();
            if (Item.getRawId(itemStack.getItem()) != Item.getRawId(Items.SPLASH_POTION)) continue;
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(itemStack);
            for (StatusEffectInstance effect : effects) {
                if (effect.getEffectType() == StatusEffects.INSTANT_HEALTH) {
                    count = count + entry.getValue().getCount();
                }
            }
        }
        return count;
    }
    public static int findPotionInventorySlot() {
        for (int i = 0; i < 45; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (Item.getRawId(itemStack.getItem()) != Item.getRawId(Items.SPLASH_POTION)) continue;
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(itemStack);
            for (StatusEffectInstance effect : effects) {
                if (effect.getEffectType() == StatusEffects.INSTANT_HEALTH) {
                    return i < 9 ? i + 36 : i;
                }
            }
        }
        return -1;
    }
    public static int findPotion() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = InventoryUtil.getStackInSlot(i);
            if (Item.getRawId(itemStack.getItem()) != Item.getRawId(Items.SPLASH_POTION)) continue;
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(itemStack);
            for (StatusEffectInstance effect : effects) {
                if (effect.getEffectType() == StatusEffects.INSTANT_HEALTH) {
                    return i;
                }
            }
        }
        return -1;
    }
}
