package dev.luminous.mod.modules.impl.player;

import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class InventorySorter extends Module {
    public InventorySorter() {
        super("InventorySorter", Category.Player);
        setChinese("背包整理");
    }

    private final BooleanSetting stack = add(new BooleanSetting("Stack", true));
    private final BooleanSetting sort = add(new BooleanSetting("Sort", true));
    private final SliderSetting delay = add(new SliderSetting("Delay", 0.1, 0, 5, 0.01).setSuffix("s"));

    private final Timer timer = new Timer();


    @Override
    public void onUpdate() {
        if (!timer.passedS(delay.getValue())) return;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof InventoryScreen) && !(mc.currentScreen instanceof ClickGuiScreen) && !(mc.currentScreen instanceof GameMenuScreen)) {
            return;
        }
        if (stack.getValue()) {
            for (int slot1 = 9; slot1 < 36; ++slot1) {
                ItemStack stack = mc.player.getInventory().getStack(slot1);
                if (stack.isEmpty()) continue;
                if (!stack.isStackable()) continue;
                if (stack.getCount() == stack.getMaxCount()) continue;
                for (int slot2 = 35; slot2 >= 9; --slot2) {
                    if (slot1 == slot2) continue;
                    ItemStack stack2 = mc.player.getInventory().getStack(slot2);
                    if (stack2.getCount() == stack2.getMaxCount()) continue;
                    if (canMerge(stack, stack2)) {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot1, 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot2, 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot1, 0, SlotActionType.PICKUP, mc.player);
                        timer.reset();
                        return;
                    }
                }
            }
        }
        if (sort.getValue()) {
            for (int slot1 = 9; slot1 < 36; ++slot1) {
                int id = Item.getRawId(mc.player.getInventory().getStack(slot1).getItem());
                if (mc.player.getInventory().getStack(slot1).isEmpty()) {
                    id = 114514;
                }
                int minId = getMinId(slot1, id);

                if (minId < id) {
                    for (int slot2 = 35; slot2 > slot1; --slot2) {
                        ItemStack stack = mc.player.getInventory().getStack(slot2);
                        if (stack.isEmpty()) continue;
                        int itemID = Item.getRawId(stack.getItem());
//                        System.out.println("searchSlot:" + slot2 + " id:" + itemID);
                        if (itemID == minId) {
//                            System.out.println("targetSlot:" + slot2);
                            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot1, 0, SlotActionType.PICKUP, mc.player);
                            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot2, 0, SlotActionType.PICKUP, mc.player);
                            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot1, 0, SlotActionType.PICKUP, mc.player);
                            timer.reset();
                            return;
                        }
                    }
                }
            }
        }
    }

    private int getMinId(int slot, int currentId) {
        int id = currentId;
        for (int slot1 = slot + 1; slot1 < 36; ++slot1) {
            ItemStack stack = mc.player.getInventory().getStack(slot1);
            if (stack.isEmpty()) continue;
            int itemID = Item.getRawId(stack.getItem());
            if (itemID < id) {
                id = itemID;
            }
        }
//        System.out.println("inputSlot:" + slot + " currentId:" + currentId + " minId:" + id);
        return id;
    }
    private boolean canMerge(ItemStack source, ItemStack stack) {
        return source.getItem() == stack.getItem() && source.getName().equals(stack.getName());
    }
}
