package dev.luminous.core.impl;

import dev.luminous.api.utils.Wrapper;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.Alien;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class HoleManager implements Wrapper {
    public boolean isHole(BlockPos pos) {
        return isHole(pos, true, false, false);
    }

    public boolean isHole(BlockPos pos, boolean canStand, boolean checkTrap, boolean anyBlock) {
        int blockProgress = 0;
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || i == Direction.DOWN) continue;
            if (anyBlock && !mc.world.isAir(pos.offset(i)) || Alien.HOLE.isHard(pos.offset(i)))
                blockProgress++;
        }
        return (!checkTrap || (mc.world.isAir(pos)
                        && mc.world.isAir(pos.up())
                        && mc.world.isAir(pos.up(1))
                        && mc.world.isAir(pos.up(2))
                        && (mc.player.getBlockY() - 1 <= pos.getY() || mc.world.isAir(pos.up(3)))
                        && (mc.player.getBlockY() - 2 <= pos.getY() || mc.world.isAir(pos.up(4)))))
                        && blockProgress > 3
                        && (!canStand || mc.world.getBlockState(pos.add(0, -1, 0)).blocksMovement());
    }

    public BlockPos getHole(float range, boolean doubleHole, boolean any, boolean up) {
        BlockPos bestPos = null;
        double bestDistance = range + 1;
        for (BlockPos pos : BlockUtil.getSphere(range, mc.player.getPos())) {
            if (pos.getX() != mc.player.getBlockX() || pos.getZ() != mc.player.getBlockZ()) {
                if (!up && pos.getY() + 1 > mc.player.getY()) continue;
            }
            if (Alien.HOLE.isHole(pos, true, true, any) || doubleHole && isDoubleHole(pos)) {
                if (pos.getY() - mc.player.getBlockY() > 1) continue;
                double distance = MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                if (bestPos == null || distance < bestDistance) {
                    bestPos = pos;
                    bestDistance = distance;
                }
            }
        }
        return bestPos;
    }
    public boolean isDoubleHole(BlockPos pos) {
        Direction unHardFacing = is3Block(pos);
        if (unHardFacing != null) {
            pos = pos.offset(unHardFacing);
            unHardFacing = is3Block(pos);
            return unHardFacing != null;
        }
        return false;
    }
    public Direction is3Block(BlockPos pos) {
        if (!isHard(pos.down())) {
            return null;
        }
        if (!mc.world.isAir(pos) || !mc.world.isAir(pos.up()) || !mc.world.isAir(pos.up(2))) {
            return null;
        }
        int progress = 0;
        Direction unHardFacing = null;
        for (Direction facing : Direction.values()) {
            if (facing == Direction.UP || facing == Direction.DOWN) continue;
            if (isHard(pos.offset(facing))) {
                progress++;
                continue;
            }
            int progress2 = 0;
            for (Direction facing2 : Direction.values()) {
                if (facing2 == Direction.DOWN || facing2 == facing.getOpposite()) {
                    continue;
                }
                if (isHard(pos.offset(facing).offset(facing2))) {
                    progress2++;
                }
            }
            if (progress2 == 4) {
                progress++;
                continue;
            }
            unHardFacing = facing;
        }
        if (progress == 3) {
            return unHardFacing;
        }
        return null;
    }

    public boolean isHard(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.NETHERITE_BLOCK || block == Blocks.ENDER_CHEST || block == Blocks.BEDROCK;
    }
}
