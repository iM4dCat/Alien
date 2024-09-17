package dev.luminous.mod.modules.impl.combat;

import dev.luminous.Alien;
import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.AntiCheat;
import dev.luminous.mod.modules.impl.exploit.Blink;
import dev.luminous.mod.modules.settings.Placement;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public class AutoTrap
        extends Module {
    final Timer timer = new Timer();
    public final SliderSetting delay =
            add(new SliderSetting("Delay", 100, 0, 500).setSuffix("ms"));
    private final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 4.0f, 1.0f, 6.0f).setSuffix("m"));
    private final SliderSetting blocksPer = add(new SliderSetting("BlocksPer", 1, 1, 8));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true));
    private final BooleanSetting autoDisable =
            add(new BooleanSetting("AutoDisable", true));
    private final SliderSetting range =
            add(new SliderSetting("Range", 5.0f, 1.0f, 8.0f).setSuffix("m"));
    private final EnumSetting<TargetMode> targetMod =
            add(new EnumSetting<>("TargetMode", TargetMode.Single));
    private final BooleanSetting checkMine =
            add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting helper =
            add(new BooleanSetting("Helper", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting extend =
            add(new BooleanSetting("Extend", true));
    private final BooleanSetting antiStep =
            add(new BooleanSetting("AntiStep", false));
    private final BooleanSetting onlyBreak =
            add(new BooleanSetting("OnlyBreak", false, () -> antiStep.getValue()));
    private final BooleanSetting head =
            add(new BooleanSetting("Head", true));
    private final BooleanSetting headExtend =
            add(new BooleanSetting("HeadExtend", true));

    private final EnumSetting<Mode> headMode = add(new EnumSetting<>("BlockForHead", Mode.Anchor));
    private final BooleanSetting chestUp =
            add(new BooleanSetting("ChestUp", true));
    private final BooleanSetting onlyBreaking =
            add(new BooleanSetting("OnlyBreaking", false, () -> chestUp.getValue()));
    private final BooleanSetting chest =
            add(new BooleanSetting("Chest", true));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("OnlyGround", false, () -> chest.getValue()));
    private final BooleanSetting legs =
            add(new BooleanSetting("Legs", false));
    private final BooleanSetting legAnchor =
            add(new BooleanSetting("LegAnchor", true));
    private final BooleanSetting down =
            add(new BooleanSetting("Down", false));
    private final BooleanSetting onlyHole =
            add(new BooleanSetting("OnlyHole", false));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("Break", true));
    private final BooleanSetting usingPause = add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting selfGround = add(new BooleanSetting("SelfGround", true));
    public PlayerEntity target;
    public static AutoTrap INSTANCE;

    public AutoTrap() {
        super("AutoTrap", Category.Combat);
        setChinese("自动困住");
        INSTANCE = this;
    }

    public enum TargetMode {
        Single, Multi
    }

    int progress = 0;
    private final ArrayList<BlockPos> trapList = new ArrayList<>();
    private final ArrayList<BlockPos> placeList = new ArrayList<>();

    @Override
    public void onUpdate() {
        trapList.clear();
        placeList.clear();
        progress = 0;
        if (selfGround.getValue() && !mc.player.isOnGround()) {
            target = null;
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) return;
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            target = null;
            return;
        }
        if (!timer.passedMs((long) delay.getValue())) {
            return;
        }
        if (targetMod.getValue() == TargetMode.Single) {
            target = CombatUtil.getClosestEnemy(range.getValue());
            if (target == null) {
                if (autoDisable.getValue()) disable();
                return;
            }
            trapTarget(target);
        } else if (targetMod.getValue() == TargetMode.Multi) {
            boolean found = false;
            for (PlayerEntity player : CombatUtil.getEnemies(range.getValue())) {
                found = true;
                target = player;
                trapTarget(target);
            }
            if (!found) {
                if (autoDisable.getValue()) disable();
                target = null;
            }
        }
    }

    private void trapTarget(PlayerEntity target) {
        if (onlyHole.getValue() && !Alien.HOLE.isHole(EntityUtil.getEntityPos(target))) return;
        doTrap(EntityUtil.getEntityPos(target, true));
    }

    private void doTrap(BlockPos pos) {
        if (pos == null) return;
        if (trapList.contains(pos)) return;
        trapList.add(pos);
        if (legs.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos offsetPos = pos.offset(i);
                tryPlaceBlock(offsetPos, legAnchor.getValue(), false, false);
                if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue()) && getHelper(offsetPos) != null)
                    tryPlaceObsidian(getHelper(offsetPos));
            }
        }
        if (headExtend.getValue()) {
            for (int x : new int[]{1, 0, -1}) {
                for (int z : new int[]{1, 0, -1}) {
                    BlockPos offsetPos = pos.add(z, 0, x);
                    if (checkEntity(new BlockPos(offsetPos))) tryPlaceBlock(offsetPos.up(2), headMode.getValue() == Mode.Anchor, headMode.getValue() == Mode.Concrete, headMode.getValue() == Mode.Web);
                }
            }
        }
        if (head.getValue()) {
            if (BlockUtil.clientCanPlace(pos.up(2), breakCrystal.getValue())) {
                if (BlockUtil.getPlaceSide(pos.up(2)) == null) {
                    boolean trapChest = helper.getValue();
                    if (getHelper(pos.up(2)) != null) {
                        tryPlaceObsidian(getHelper(pos.up(2)));
                        trapChest = false;
                    }
                    if (trapChest) {
                        for (Direction i : Direction.values()) {
                            if (i == Direction.DOWN || i == Direction.UP) continue;
                            BlockPos offsetPos = pos.offset(i).up();
                            if (BlockUtil.clientCanPlace(offsetPos.up(), breakCrystal.getValue())) {
                                if (BlockUtil.canPlace(offsetPos, placeRange.getValue(), breakCrystal.getValue())) {
                                    tryPlaceObsidian(offsetPos);
                                    trapChest = false;
                                    break;
                                }
                            }
                        }
                        if (trapChest) {
                            for (Direction i : Direction.values()) {
                                if (i == Direction.DOWN || i == Direction.UP) continue;
                                BlockPos offsetPos = pos.offset(i).up();
                                if (BlockUtil.clientCanPlace(offsetPos.up(), breakCrystal.getValue())) {
                                    if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue()) && getHelper(offsetPos) != null) {
                                        tryPlaceObsidian(getHelper(offsetPos));
                                        trapChest = false;
                                        break;
                                    }
                                }
                            }
                            if (trapChest) {
                                for (Direction i : Direction.values()) {
                                    if (i == Direction.DOWN || i == Direction.UP) continue;
                                    BlockPos offsetPos = pos.offset(i).up();
                                    if (BlockUtil.clientCanPlace(offsetPos.up(), breakCrystal.getValue())) {
                                        if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue()) && getHelper(offsetPos) != null) {
                                            if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down(), breakCrystal.getValue()) && getHelper(offsetPos.down()) != null) {
                                                tryPlaceObsidian(getHelper(offsetPos.down()));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                tryPlaceBlock(pos.up(2), headMode.getValue() == Mode.Anchor, headMode.getValue() == Mode.Concrete, headMode.getValue() == Mode.Web);
            }
        }
        if (antiStep.getValue() && (Alien.BREAK.isMining(pos.up(2)) || !onlyBreak.getValue())) {
            if (BlockUtil.getPlaceSide(pos.up(3)) == null && BlockUtil.clientCanPlace(pos.up(3), breakCrystal.getValue())) {
                if (getHelper(pos.up(3), Direction.DOWN) != null) {
                    tryPlaceObsidian(getHelper(pos.up(3)));
                }
            }
            tryPlaceObsidian(pos.up(3));
        }
        if (down.getValue()) {
            BlockPos offsetPos = pos.down();
            tryPlaceObsidian(offsetPos);
            if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue()) && getHelper(offsetPos) != null)
                tryPlaceObsidian(getHelper(offsetPos));
        }
        if (chestUp.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos offsetPos = pos.offset(i).up(2);
                if (!onlyBreaking.getValue() || Alien.BREAK.isMining(pos.up(2))) {
                    tryPlaceObsidian(offsetPos);
                    if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue())) {
                        if (getHelper(offsetPos) != null) {
                            tryPlaceObsidian(getHelper(offsetPos));
                        } else if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down(), breakCrystal.getValue()) && getHelper(offsetPos.down()) != null) {
                            tryPlaceObsidian(getHelper(offsetPos.down()));
                        }
                    }
                }
            }
        }
        if (chest.getValue() && (!onlyGround.getValue() || target.isOnGround())) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos offsetPos = pos.offset(i).up();
                tryPlaceObsidian(offsetPos);
                if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, breakCrystal.getValue())) {
                    if (getHelper(offsetPos) != null) {
                        tryPlaceObsidian(getHelper(offsetPos));
                    } else
                    if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down(), breakCrystal.getValue()) && getHelper(offsetPos.down()) != null) {
                        tryPlaceObsidian(getHelper(offsetPos.down()));
                    }
                }
            }
        }
        if (extend.getValue()) {
            for (int x : new int[]{1, 0, -1}) {
                for (int z : new int[]{1, 0, -1}) {
                    BlockPos offsetPos = pos.add(x, 0, z);
                    if (checkEntity(new BlockPos(offsetPos))) doTrap(offsetPos);
                }
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

    public BlockPos getHelper(BlockPos pos) {
        if (!helper.getValue()) return null;
        for (Direction i : Direction.values()) {
            if (checkMine.getValue() && Alien.BREAK.isMining(pos.offset(i))) continue;
            if (AntiCheat.INSTANCE.placement.getValue() == Placement.Strict && !BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite())) continue;
            if (BlockUtil.canPlace(pos.offset(i), placeRange.getValue(), breakCrystal.getValue())) return pos.offset(i);
        }
        return null;
    }

    public BlockPos getHelper(BlockPos pos, Direction ignore) {
        if (!helper.getValue()) return null;
        for (Direction i : Direction.values()) {
            if (i == ignore) continue;
            if (checkMine.getValue() && Alien.BREAK.isMining(pos.offset(i))) continue;
            if (!BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite())) continue;
            if (BlockUtil.canPlace(pos.offset(i), placeRange.getValue(), breakCrystal.getValue())) return pos.offset(i);
        }
        return null;
    }
    private boolean checkEntity(BlockPos pos) {
        if (mc.player.getBoundingBox().intersects(new Box(pos))) return false;
        for (Entity entity : BlockUtil.getEndCrystals(new Box(pos))) {
            if (entity.isAlive())
                return true;
        }
        return false;
    }

    private void tryPlaceBlock(BlockPos pos, boolean anchor, boolean sand, boolean web) {
        if (placeList.contains(pos)) return;
        if (Alien.BREAK.isMining(pos)) return;
        if (!BlockUtil.canPlace(pos, 6, breakCrystal.getValue())) return;
        if (!(progress < blocksPer.getValue())) return;
        if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) > placeRange.getValue())
            return;
        int old = mc.player.getInventory().selectedSlot;
        int block = sand ? getConcrete() : (web ? (getWeb() != -1 ? getWeb() : getBlock()) : (anchor && getAnchor() != -1 ? getAnchor() : getBlock()));
        if (block == -1) return;
        placeList.add(pos);
        CombatUtil.attackCrystal(pos, rotate.getValue(), usingPause.getValue());
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
        timer.reset();
        progress++;
    }
    private void tryPlaceObsidian(BlockPos pos) {
        if (pos == null) return;
        if (placeList.contains(pos)) return;
        if (Alien.BREAK.isMining(pos)) return;
        if (!BlockUtil.canPlace(pos, 6, breakCrystal.getValue())) return;
        if (!(progress < blocksPer.getValue())) return;
        if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) > placeRange.getValue())
            return;
        int old = mc.player.getInventory().selectedSlot;
        int block = getBlock();
        if (block == -1) return;
        BlockUtil.placedPos.add(pos);
        placeList.add(pos);
        CombatUtil.attackCrystal(pos, rotate.getValue(), usingPause.getValue());
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
        timer.reset();
        progress++;
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

    private int getConcrete() {
        if (inventory.getValue()) {
            return InventoryUtil.findClassInventorySlot(ConcretePowderBlock.class);
        } else {
            return InventoryUtil.findClass(ConcretePowderBlock.class);
        }
    }
    private int getWeb() {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.COBWEB);
        } else {
            return InventoryUtil.findBlock(Blocks.COBWEB);
        }
    }
    private int getAnchor() {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR);
        } else {
            return InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
        }
    }
    private enum Mode {
        Obsidian,
        Anchor,
        Web,
        Concrete
    }
}
