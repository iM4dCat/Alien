package dev.luminous.mod.modules.impl.player;

import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.modules.Module;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class Replenish extends Module {
    public Replenish() {
        super("Replenish", Category.Player);
        setChinese("物品栏补充");
    }

    private final SliderSetting delay = add(new SliderSetting("Delay", 2, 0, 5, 0.01).setSuffix("s"));
    private final SliderSetting min = add(new SliderSetting("Min", 50, 1, 64));
    private final SliderSetting forceDelay = add(new SliderSetting("ForceDelay", 0.2, 0, 4, 0.01).setSuffix("s"));
    private final SliderSetting forceMin = add(new SliderSetting("ForceMin", 16, 1, 64));
    private final Timer timer = new Timer();


    @Override
    public void onUpdate() {
/*        if (mc.currentScreen != null && !(mc.currentScreen instanceof ClickGuiScreen)) return;*/
        for (int i = 0; i < 9; ++i) {
            if (replenish(i)) {
                timer.reset();
                return;
            }
        }
    }

    private boolean replenish(int slot) {
        ItemStack stack = mc.player.getInventory().getStack(slot);

        if (stack.isEmpty()) return false;
        if (!stack.isStackable()) return false;
        if (stack.getCount() > min.getValue()) return false;
        if (stack.getCount() == stack.getMaxCount()) return false;

        for (int i = 9; i < 36; ++i) {
            ItemStack item = mc.player.getInventory().getStack(i);
            if (item.isEmpty() || !canMerge(stack, item)) continue;
            if (stack.getCount() > forceMin.getValueFloat()) {
                if (!timer.passedS(delay.getValue())) {
                    return false;
                }
            } else {
                if (!timer.passedS(forceDelay.getValue())) {
                    return false;
                }
            }
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            return true;
        }
        return false;
    }

    private boolean canMerge(ItemStack source, ItemStack stack) {
        return source.getItem() == stack.getItem() && source.getName().equals(stack.getName());
    }
}
