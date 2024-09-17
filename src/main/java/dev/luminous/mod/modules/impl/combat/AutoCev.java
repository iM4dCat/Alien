package dev.luminous.mod.modules.impl.combat;

import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import dev.luminous.mod.modules.impl.player.PacketMine;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoCev extends Module {
    public static AutoCev INSTANCE;
    public AutoCev() {
        super("AutoCev", Category.Combat);
        setChinese("自动炸头");
        INSTANCE = this;
    }
    private final SliderSetting targetRange =
            add(new SliderSetting("TargetRange", 5, 0, 8, .1));
    private final SliderSetting breakRange =
            add(new SliderSetting("BreakRange", 5, 0, 8, .1));
    private final SliderSetting delay =
            add(new SliderSetting("Delay", 100, 0, 500).setSuffix("ms"));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true));
    private final BooleanSetting ground =
            add(new BooleanSetting("Ground", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting top =
            add(new BooleanSetting("Top", false));
    private final BooleanSetting bevel =
            add(new BooleanSetting("Bevel", true));

    private PlayerEntity target = null;
    private final Timer timer = new Timer();
    public static boolean canPlaceCrystal(BlockPos pos) {
        return mc.world.isAir(pos)
                && !BlockUtil.hasEntityBlockCrystal(pos, false)
                && !BlockUtil.hasEntityBlockCrystal(pos.up(), false)
                && (!ClientSetting.INSTANCE.lowVersion.getValue() || mc.world.isAir(pos.up()));
    }

    @Override
    public void onUpdate() {
        if (ground.getValue() && !mc.player.isOnGround()) return;
        PacketMine.INSTANCE.crystal.setValue(true);
        if ((target = CombatUtil.getClosestEnemy(targetRange.getValue())) != null) {
            BlockPos targetPos = EntityUtil.getEntityPos(target);
            if (PacketMine.getBreakPos() != null) {
                for (Direction facing : Direction.values()) {
                    if (facing == Direction.DOWN) continue;
                    if (facing == Direction.UP) {
                        if (!top.getValue()) continue;
                    } else if (!bevel.getValue()) continue;
                    BlockPos pos = targetPos.up(1).offset(facing);
                    if (pos.up().toCenterPos().distanceTo(mc.player.getPos()) > breakRange.getValue()) continue;
                    if (PacketMine.getBreakPos().equals(targetPos.up(1).offset(facing))) {
                        if (canPlaceCrystal(targetPos.up(2).offset(facing))) {
                            if (mc.world.isAir(pos)) {
                                if (!BlockUtil.canPlace(pos)) continue;
                                if (!timer.passedMs(delay.getValue())) {
                                    return;
                                }
                                placeBlock(pos);
                                timer.reset();
                                return;
                            } else if (getBlock(pos) == Blocks.OBSIDIAN) {
                                PacketMine.INSTANCE.mine(pos);
                                timer.reset();
                                return;
                            }
                        } else if (BlockUtil.hasCrystal(targetPos.up(2).offset(facing))) {
                            if (mc.world.isAir(pos)) {
                                return;
                            } else if (getBlock(pos) == Blocks.OBSIDIAN) {
                                PacketMine.INSTANCE.mine(pos);
                                timer.reset();
                                return;
                            }
                        }
                    }
                }
            }
            for (Direction facing : Direction.values()) {
                if (facing == Direction.DOWN) continue;
                if (facing == Direction.UP) {
                    if (!top.getValue()) continue;
                } else if (!bevel.getValue()) continue;
                BlockPos pos = targetPos.up(1).offset(facing);
                if (pos.up().toCenterPos().distanceTo(mc.player.getPos()) > breakRange.getValue()) continue;
                if (canPlaceCrystal(targetPos.up(2).offset(facing))) {
                    if (mc.world.isAir(pos)) {
                        if (!BlockUtil.canPlace(pos)) continue;
                        if (!timer.passedMs(delay.getValue())) {
                            return;
                        }
                        placeBlock(pos);
                        timer.reset();
                        break;
                    } else if (getBlock(pos) == Blocks.OBSIDIAN) {
                        PacketMine.INSTANCE.mine(pos);
                        timer.reset();
                        break;
                    }
                } else if (BlockUtil.hasCrystal(targetPos.up(2).offset(facing))) {
                    if (mc.world.isAir(pos)) {
                        break;
                    } else if (getBlock(pos) == Blocks.OBSIDIAN) {
                        PacketMine.INSTANCE.mine(pos);
                        timer.reset();
                        break;
                    }
                }
            }
        }
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        } else {
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
    }

    private void placeBlock(BlockPos pos) {
        int block;
        if ((block = getBlock()) == -1) {
            return;
        }
        int oldSlot = mc.player.getInventory().selectedSlot;
        if (BlockUtil.canPlace(pos)) {
            Direction side;
            if ((side = BlockUtil.getPlaceSide(pos)) == null) {
                if (BlockUtil.airPlace()) {
                    doSwap(block);
                    BlockUtil.placedPos.add(pos);
                    BlockUtil.clickBlock(pos, Direction.DOWN, rotate.getValue());
                    if (inventory.getValue()) {
                        doSwap(block);
                        EntityUtil.syncInventory();
                    } else {
                        doSwap(oldSlot);
                    }
                }
                return;
            }
            doSwap(block);
            BlockUtil.placedPos.add(pos);
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate.getValue());
            if (inventory.getValue()) {
                doSwap(block);
                EntityUtil.syncInventory();
            } else {
                doSwap(oldSlot);
            }
        }
    }

    @Override
    public String getInfo() {
        if (target != null) {
            return target.getName().getString();
        }
        return null;
    }


    private Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }
}
