package dev.luminous.mod.modules.impl.player;

import dev.luminous.mod.modules.Module;
import net.minecraft.block.AirBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class AutoTool extends Module {

    public AutoTool() {
        super("AutoTool", Category.Player);
        setChinese("自动工具");
    }

    @Override
    public void onUpdate() {
        if (!(mc.crosshairTarget instanceof BlockHitResult result)) return;
        BlockPos pos = result.getBlockPos();
        if (mc.world.isAir(pos))
            return;
        int tool = getTool(pos);
        if (tool != -1 && mc.options.attackKey.isPressed()) {
            mc.player.getInventory().selectedSlot = tool;
        }
    }


    public static int getTool(final BlockPos pos) {
        int index = -1;
        float CurrentFastest = 1.0f;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != ItemStack.EMPTY) {
                final float digSpeed = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
                final float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));

                if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) return -1;
                if (digSpeed + destroySpeed > CurrentFastest) {
                    CurrentFastest = digSpeed + destroySpeed;
                    index = i;
                }
            }
        }
        return index;
    }
}