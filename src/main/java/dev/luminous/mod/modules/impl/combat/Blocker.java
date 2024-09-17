package dev.luminous.mod.modules.impl.combat;

import dev.luminous.Alien;
import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.world.BlockPosX;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.AntiCheat;
import dev.luminous.mod.modules.impl.exploit.Blink;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class Blocker extends Module {
    public static Blocker INSTANCE ;
    final Timer timer = new Timer();
    private final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
    private final SliderSetting delay =
            add(new SliderSetting("PlaceDelay", 50, 0, 500, () -> page.getValue() == Page.General));
    private final SliderSetting blocksPer =
            add(new SliderSetting("BlocksPer", 1, 1, 8, () -> page.getValue() == Page.General));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true, () -> page.getValue() == Page.General));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("Break", true, () -> page.getValue() == Page.General));
    private final BooleanSetting inventorySwap =
            add(new BooleanSetting("InventorySwap", true, () -> page.getValue() == Page.General));

    private final BooleanSetting bevelCev =
            add(new BooleanSetting("BevelCev", true, () -> page.getValue() == Page.Target));
    private final BooleanSetting burrow =
            add(new BooleanSetting("Burrow", true, () -> page.getValue() == Page.Target));
    private final BooleanSetting face =
            add(new BooleanSetting("Face", true, () -> page.getValue() == Page.Target));
    private final BooleanSetting feet =
            add(new BooleanSetting("Feet", true, () -> page.getValue() == Page.Target).setParent());
    private final BooleanSetting onlySurround =
            add(new BooleanSetting("OnlySurround", true, () -> page.getValue() == Page.Target && feet.isOpen()));

    private final BooleanSetting inAirPause =
            add(new BooleanSetting("InAirPause", false, () -> page.getValue() == Page.Check));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", true, () -> page.getValue() == Page.Check));
    private final BooleanSetting eatingPause = add(new BooleanSetting("EatingPause", true, () -> page.getValue() == Page.Check));

    private final List<BlockPos> placePos = new ArrayList<>();
    private int placeProgress = 0;

    public Blocker() {
        super("Blocker", Category.Combat);
        setChinese("水晶阻挡");
        INSTANCE = this;
    }

    private BlockPos playerBP;

    @Override
    public void onUpdate() {
        list.clear();
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) return;
        if (!timer.passedMs(delay.getValue())) return;
        if (eatingPause.getValue() && mc.player.isUsingItem()) return;
        placeProgress = 0;

        if (playerBP != null && !playerBP.equals(EntityUtil.getPlayerPos(true))) {
            placePos.clear();
        }
        playerBP = EntityUtil.getPlayerPos(true);
        double[] offset = new double[]{AntiCheat.getOffset(), -AntiCheat.getOffset(), 0};
        if (bevelCev.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN) continue;
                if (isBedrock(playerBP.offset(i).up())) continue;

                BlockPos blockerPos = playerBP.offset(i).up(2);
                if (crystalHere(blockerPos) && !placePos.contains(blockerPos)) {
                    placePos.add(blockerPos);
                }
            }
        }
        if (face.getValue()) {
            for (double x : offset) {
                for (double z : offset) {
                    for (Direction i : Direction.values()) {
                        BlockPos blockerPos = new BlockPosX(mc.player.getX() + x, mc.player.getY() + 0.5, mc.player.getZ() + z).offset(i).up();
                        if (crystalHere(blockerPos) && !placePos.contains(blockerPos)) {
                            placePos.add(blockerPos);
                        }
                    }
                }
            }
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN) continue;
                if (isBedrock(playerBP.offset(i).up())) continue;

                BlockPos blockerPos = playerBP.offset(i).up(2);
                if (crystalHere(blockerPos) && !placePos.contains(blockerPos)) {
                    placePos.add(blockerPos);
                }
            }
        }
        if (getObsidian() == -1) {
            return;
        }

        if (inAirPause.getValue() && !mc.player.isOnGround()) return;
        placePos.removeIf((pos) -> !BlockUtil.clientCanPlace(pos, true));
        if (burrow.getValue()) {
            for (double x : offset) {
                for (double z : offset) {
                    BlockPos surroundPos = new BlockPosX(mc.player.getX() + x, mc.player.getY(), mc.player.getZ() + z);
                    if (isBedrock(surroundPos)) continue;
                    if (Alien.BREAK.isMining(surroundPos)) {
                        for (Direction direction : Direction.values()) {
                            if (direction == Direction.DOWN || direction == Direction.UP) continue;
                            BlockPos defensePos = surroundPos.offset(direction);
                            if (detectMining.getValue() && Alien.BREAK.isMining(defensePos)) {
                                continue;
                            }
                            if (breakCrystal.getValue()) {
                                CombatUtil.attackCrystal(defensePos, rotate.getValue(), false);
                            }
                            if (BlockUtil.canPlace(defensePos, 6, breakCrystal.getValue())) {
                                tryPlaceObsidian(defensePos);
                            }
                        }
                    }
                }
            }
        }
        if (feet.getValue() && (!onlySurround.getValue() || Surround.INSTANCE.isOn())) {
            for (double x : offset) {
                for (double z : offset) {
                    for (Direction i : Direction.values()) {
                        BlockPos surroundPos = new BlockPosX(mc.player.getX() + x, mc.player.getY() + 0.5, mc.player.getZ() + z).offset(i);
                        if (isBedrock(surroundPos)) continue;
                        if (Alien.BREAK.isMining(surroundPos)) {
                            for (Direction direction : Direction.values()) {
                                if (direction == Direction.DOWN || direction == Direction.UP) continue;
                                BlockPos defensePos = surroundPos.offset(direction);
                                if (detectMining.getValue() && Alien.BREAK.isMining(defensePos)) {
                                    continue;
                                }
                                if (breakCrystal.getValue()) {
                                    CombatUtil.attackCrystal(defensePos, rotate.getValue(), false);
                                }
                                if (BlockUtil.canPlace(defensePos, 6, breakCrystal.getValue())) {
                                    tryPlaceObsidian(defensePos);
                                }
                            }
                            BlockPos defensePos = surroundPos.up();
                            if (detectMining.getValue() && Alien.BREAK.isMining(defensePos)) {
                                continue;
                            }
                            if (breakCrystal.getValue()) {
                                CombatUtil.attackCrystal(defensePos, rotate.getValue(), false);
                            }
                            if (BlockUtil.canPlace(defensePos, 6, breakCrystal.getValue())) {
                                tryPlaceObsidian(defensePos);
                            }
                        }
                    }
                }
            }
        }

        for (BlockPos defensePos : placePos) {
            if (breakCrystal.getValue() && crystalHere(defensePos)) {
                CombatUtil.attackCrystal(defensePos, rotate.getValue(), false);
            }
            if (BlockUtil.canPlace(defensePos, 6, breakCrystal.getValue())) {
                tryPlaceObsidian(defensePos);
            }
        }
    }

    private boolean crystalHere(BlockPos pos) {
        return BlockUtil.getEndCrystals(new Box(pos)).stream().anyMatch(entity -> entity.getBlockPos().equals(pos));
    }

    private boolean isBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
    }
    private final List<BlockPos> list = new ArrayList<>();
    private void tryPlaceObsidian(BlockPos pos) {
        if (list.contains(pos)) return;
        list.add(pos);
        if (!(placeProgress < blocksPer.getValue())) return;
        if (detectMining.getValue() && Alien.BREAK.isMining(pos)) {
            return;
        }
        int oldSlot = mc.player.getInventory().selectedSlot;
        int block;
        if ((block = getObsidian()) == -1) {
            return;
        }
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue());
        if (inventorySwap.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(oldSlot);
        }
        placeProgress++;
        timer.reset();
    }

    private void doSwap(int slot) {
        if (inventorySwap.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }
    private int getObsidian() {
        if (inventorySwap.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        } else {
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
    }

    public enum Page {
        General,
        Target,
        Check,
    }
}