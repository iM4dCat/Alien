package dev.luminous.mod.modules.impl.misc;

import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.player.PacketMine;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.util.math.BlockPos;

public class Nuker extends Module {
    private final SliderSetting range =
            add(new SliderSetting("Range", 4, 0, 8,.1));
    private final BooleanSetting down =
            add(new BooleanSetting("Down",false));
    public Nuker() {
        super("Nuker", Category.Misc);
        setChinese("范围挖掘");
    }

    @Override
    public void onUpdate() {
        if (PacketMine.getBreakPos() != null && !mc.world.isAir(PacketMine.getBreakPos())) {
            return;
        }
        BlockPos pos = getBlock();
        if (pos != null) {
           PacketMine.INSTANCE.mine(pos);
        }
    }

    private BlockPos getBlock() {
        BlockPos down = null;
        for (BlockPos pos : BlockUtil.getSphere(range.getValueFloat(), mc.player.getPos())) {
            if (mc.world.isAir(pos)) continue;
            if (PacketMine.godBlocks.contains(mc.world.getBlockState(pos).getBlock())) continue;
            if (BlockUtil.getClickSideStrict(pos) == null) continue;
            if (pos.getY() < mc.player.getY()) {
                if (down == null && this.down.getValue()) {
                    down = pos;
                }
                continue;
            }
            return pos;
        }
        return down;
    }
}