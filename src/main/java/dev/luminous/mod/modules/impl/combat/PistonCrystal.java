package dev.luminous.mod.modules.impl.combat;

import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.Alien;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.AntiCheat;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import dev.luminous.mod.modules.settings.Placement;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class PistonCrystal extends Module {
    public static PistonCrystal INSTANCE;
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", false));
    private final BooleanSetting pistonPacket =
            add(new BooleanSetting("PistonPacket", false));
    private final BooleanSetting noEating = add(new BooleanSetting("NoEating", true));
    private final BooleanSetting eatingBreak = add(new BooleanSetting("EatingBreak", false));
    private final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 5.0f, 1.0f, 8.0f));
    private final SliderSetting range =
            add(new SliderSetting("Range", 4.0f, 1.0f, 8.0f));
    private final BooleanSetting fire =
            add(new BooleanSetting("Fire", true));
    private final BooleanSetting switchPos =
            add(new BooleanSetting("Switch", false));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("SelfGround", true));
    private final BooleanSetting onlyStatic =
            add(new BooleanSetting("MovingPause", true));
    private final SliderSetting updateDelay =
            add(new SliderSetting("PlaceDelay", 100, 0, 500));
    private final SliderSetting posUpdateDelay =
            add(new SliderSetting("PosUpdateDelay", 500, 0, 1000));
    private final SliderSetting stageSetting =
            add(new SliderSetting("Stage", 4, 1, 10));
    private final SliderSetting pistonStage =
            add(new SliderSetting("PistonStage", 1, 1, 10));
    private final SliderSetting pistonMaxStage =
            add(new SliderSetting("PistonMaxStage", 1, 1, 10));
    private final SliderSetting powerStage =
            add(new SliderSetting("PowerStage", 3, 1, 10));
    private final SliderSetting powerMaxStage =
            add(new SliderSetting("PowerMaxStage", 3, 1, 10));
    private final SliderSetting crystalStage =
            add(new SliderSetting("CrystalStage", 4, 1, 10));
    private final SliderSetting crystalMaxStage =
            add(new SliderSetting("CrystalMaxStage", 4, 1, 10));
    private final SliderSetting fireStage =
            add(new SliderSetting("FireStage", 2, 1, 10));
    private final SliderSetting fireMaxStage =
            add(new SliderSetting("FireMaxStage", 2, 1, 10));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting debug =
            add(new BooleanSetting("Debug", false));
    private PlayerEntity target = null;

    public PistonCrystal() {
        super("PistonCrystal", Category.Combat);
        setChinese("活塞水晶");
        INSTANCE = this;
    }

    private final Timer timer = new Timer();
    private final Timer crystalTimer = new Timer();
    public BlockPos bestPos = null;
    public BlockPos bestOPos = null;
    public Direction bestFacing = null;
    public double distance = 100;
    public boolean getPos = false;
    private boolean isPiston = false;
    public int stage = 1;


    public void onTick() {
        if (pistonStage.getValue() > stageSetting.getValue()) {
            pistonStage.setValue(stageSetting.getValue());
        }
        if (fireStage.getValue() > stageSetting.getValue()) {
            fireStage.setValue(stageSetting.getValue());
        }
        if (powerStage.getValue() > stageSetting.getValue()) {
            powerStage.setValue(stageSetting.getValue());
        }
        if (crystalStage.getValue() > stageSetting.getValue()) {
            crystalStage.setValue(stageSetting.getValue());
        }

        if (pistonMaxStage.getValue() > stageSetting.getValue()) {
            pistonMaxStage.setValue(stageSetting.getValue());
        }
        if (fireMaxStage.getValue() > stageSetting.getValue()) {
            fireMaxStage.setValue(stageSetting.getValue());
        }
        if (powerMaxStage.getValue() > stageSetting.getValue()) {
            powerMaxStage.setValue(stageSetting.getValue());
        }
        if (crystalMaxStage.getValue() > stageSetting.getValue()) {
            crystalMaxStage.setValue(stageSetting.getValue());
        }

        if (crystalMaxStage.getValue() < crystalStage.getValue()) {
            crystalStage.setValue(crystalMaxStage.getValue());
        }
        if (powerMaxStage.getValue() < powerStage.getValue()) {
            powerStage.setValue(powerMaxStage.getValue());
        }
        if (pistonMaxStage.getValue() < pistonStage.getValue()) {
            pistonStage.setValue(pistonMaxStage.getValue());
        }
        if (fireMaxStage.getValue() < fireStage.getValue()) {
            fireStage.setValue(fireMaxStage.getValue());
        }
    }

    @Override
    public void onUpdate() {
        onTick();
        target = CombatUtil.getClosestEnemy(range.getValue());
        if (target == null) {
            return;
        }
        if (noEating.getValue() && mc.player.isUsingItem())
            return;
        if (check(onlyStatic.getValue(), !mc.player.isOnGround(), onlyGround.getValue())) return;
        BlockPos pos = EntityUtil.getEntityPos(target, true);
        if (!mc.player.isUsingItem() || eatingBreak.getValue()) {
            if (checkCrystal(pos.up(0))) {
                CombatUtil.attackCrystal(pos.up(0), rotate.getValue(), true);
            }
            if (checkCrystal(pos.up(1))) {
                CombatUtil.attackCrystal(pos.up(1), rotate.getValue(), true);
            }
            if (checkCrystal(pos.up(2))) {
                CombatUtil.attackCrystal(pos.up(2), rotate.getValue(), true);
            }
        }
        if (bestPos != null && mc.world.getBlockState(bestPos).getBlock() instanceof PistonBlock) {
            isPiston = true;
        } else if (isPiston) {
            isPiston = false;
            crystalTimer.reset();
            bestPos = null;
        }
        if (crystalTimer.passedMs(posUpdateDelay.getValueInt())) {
            stage = 0;
            distance = 100;
            getPos = false;
            getBestPos(pos.up(2));
            getBestPos(pos.up());
        }
        if (!timer.passedMs(updateDelay.getValueInt())) return;
        if (getPos && bestPos != null) {
            timer.reset();
            if (debug.getValue()) {
                CommandManager.sendChatMessage("[Debug] PistonPos:" + bestPos + " Facing:" + bestFacing + " CrystalPos:" + bestOPos.offset(bestFacing));
            }
            doPistonAura(bestPos, bestFacing, bestOPos);
        }
    }

    public boolean check(boolean onlyStatic, boolean onGround, boolean onlyGround) {
        if (MovementUtil.isMoving() && onlyStatic) return true;
        if (onGround && onlyGround) return true;
        if (findBlock(Blocks.REDSTONE_BLOCK) == -1) return true;
        if (findClass(PistonBlock.class) == -1) return true;
        return findItem(Items.END_CRYSTAL) == -1;
    }
    private boolean checkCrystal(BlockPos pos) {
        for (Entity entity : BlockUtil.getEntities(new Box(pos))) {
            if (entity instanceof EndCrystalEntity) {
                float damage = AutoCrystal.INSTANCE.calculateDamage(entity.getPos(), target, target);
                if (damage > 7) return true;
            }
        }
        return false;
    }

    private boolean checkCrystal2(BlockPos pos) {
        for (Entity entity : BlockUtil.getEntities(new Box(pos))) {
            if (entity instanceof EndCrystalEntity && EntityUtil.getEntityPos(entity).equals(pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getInfo() {
        if (target != null) return target.getName().getString();
        return null;
    }

    private void getBestPos(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (i == Direction.DOWN || i == Direction.UP) continue;
            getPos(pos, i);
        }
    }

    private void getPos(BlockPos pos, Direction i) {
        if (!BlockUtil.canPlaceCrystal(pos.offset(i)) && !checkCrystal2(pos.offset(i))) return;
        getPos(pos.offset(i, 3), i, pos);
        getPos(pos.offset(i, 3).up(), i, pos);
        int offsetX = pos.offset(i).getX() - pos.getX();
        int offsetZ = pos.offset(i).getZ() - pos.getZ();
        getPos(pos.offset(i, 3).add(offsetZ, 0, offsetX), i, pos);
        getPos(pos.offset(i, 3).add(-offsetZ, 0, -offsetX), i, pos);
        getPos(pos.offset(i, 3).add(offsetZ, 1, offsetX), i, pos);
        getPos(pos.offset(i, 3).add(-offsetZ, 1, -offsetX), i, pos);

        getPos(pos.offset(i, 2), i, pos);
        getPos(pos.offset(i, 2).up(), i, pos);

        getPos(pos.offset(i, 2).add(offsetZ, 0, offsetX), i, pos);
        getPos(pos.offset(i, 2).add(-offsetZ, 0, -offsetX), i, pos);
        getPos(pos.offset(i, 2).add(offsetZ, 1, offsetX), i, pos);
        getPos(pos.offset(i, 2).add(-offsetZ, 1, -offsetX), i, pos);
    }

    private void getPos(BlockPos pos, Direction facing, BlockPos oPos) {
        if (switchPos.getValue() && bestPos != null && bestPos.equals(pos) && mc.world.isAir(bestPos)) {
            return;
        }
        if (!BlockUtil.canPlace(pos, placeRange.getValue()) && !(getBlock(pos) instanceof PistonBlock)) return;
        if (findClass(PistonBlock.class) == -1) return;
        if (ClientSetting.INSTANCE.lowVersion.getValue() && !(getBlock(pos) instanceof PistonBlock) && (mc.player.getY() - pos.getY() <= -2.0 || mc.player.getY() - pos.getY() >= 3.0) && BlockUtil.distanceToXZ(pos.getX() + 0.5, pos.getZ() + 0.5) < 2.6) {
            return;
        }
        if (!mc.world.isAir(pos.offset(facing, -1)) || mc.world.getBlockState(pos.offset(facing, -1)).getBlock() == Blocks.FIRE || getBlock(pos.offset(facing.getOpposite())) == Blocks.MOVING_PISTON && !checkCrystal2(pos.offset(facing.getOpposite()))) {
            return;
        }
        if (!BlockUtil.canPlace(pos, placeRange.getValue()) && !isPiston(pos, facing)) {
            return;
        }
        if (!(MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) < distance || bestPos == null)) {
            return;
        }
        bestPos = pos;
        bestOPos = oPos;
        bestFacing = facing;
        distance = MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos()));
        getPos = true;
        crystalTimer.reset();
    }

    private void doPistonAura(BlockPos pos, Direction facing, BlockPos oPos) {
        if (stage >= stageSetting.getValue()) {
            stage = 0;
        }
        stage++;
        if (mc.world.isAir(pos)) {
            if (BlockUtil.canPlace(pos)) {
                if (stage >= pistonStage.getValue() && stage <= pistonMaxStage.getValue()) {
                    Direction side = BlockUtil.getPlaceSide(pos);
                    if (side == null) {
                        return;
                    }
                    int old = mc.player.getInventory().selectedSlot;
                    AutoPush.pistonFacing(facing);
                    int piston = findClass(PistonBlock.class);
                    doSwap(piston);
                    BlockUtil.placeBlock(pos, false, pistonPacket.getValue());
                    if (inventory.getValue()) {
                        doSwap(piston);
                        EntityUtil.syncInventory();
                    } else {
                        doSwap(old);
                    }
                    BlockPos neighbour = pos.offset(side);
                    Direction opposite = side.getOpposite();
                    if (rotate.getValue()) {
                        Alien.ROTATION.lookAt(neighbour, opposite);
                    }
                }
            } else {
                return;
            }
        }
        if (stage >= powerStage.getValue() && stage <= powerMaxStage.getValue()) {
            doRedStone(pos, facing, oPos.offset(facing));
        }
        if (stage >= crystalStage.getValue() && stage <= crystalMaxStage.getValue()) {
            placeCrystal(oPos, facing);
        }
        if (stage >= fireStage.getValue() && stage <= fireMaxStage.getValue()) {
            doFire(oPos, facing);
        }
    }

    private void placeCrystal(BlockPos pos, Direction facing) {
        if (!BlockUtil.canPlaceCrystal(pos.offset(facing))) return;
        int crystal = findItem(Items.END_CRYSTAL);
        if (crystal == -1) return;
        int old = mc.player.getInventory().selectedSlot;
        doSwap(crystal);
        BlockUtil.placeCrystal(pos.offset(facing), true);
        if (inventory.getValue()) {
            doSwap(crystal);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
    }

    private boolean isPiston(BlockPos pos, Direction facing) {
        if (!(mc.world.getBlockState(pos).getBlock() instanceof PistonBlock)) return false;
        if (mc.world.getBlockState(pos).get(FacingBlock.FACING).getOpposite() != facing) return false;
        return mc.world.isAir(pos.offset(facing, -1)) || getBlock(pos.offset(facing, -1)) == Blocks.FIRE || getBlock(pos.offset(facing.getOpposite())) == Blocks.MOVING_PISTON;
    }

    private void doFire(BlockPos pos, Direction facing) {
        if (!fire.getValue()) return;
        int fire = findItem(Items.FLINT_AND_STEEL);
        if (fire == -1) return;
        int old = mc.player.getInventory().selectedSlot;

        int[] xOffset = {0, facing.getOffsetZ(), -facing.getOffsetZ()};
        int[] yOffset = {0, 1};
        int[] zOffset = {0, facing.getOffsetX(), -facing.getOffsetX()};
        for (int x : xOffset) {
            for (int y : yOffset) {
                for (int z : zOffset) {
                    if (getBlock(pos.add(x, y, z)) == Blocks.FIRE) {
                        return;
                    }
                }
            }
        }
        for (int x : xOffset) {
            for (int y : yOffset) {
                for (int z : zOffset) {
                    if (canFire(pos.add(x, y, z))) {
                        doSwap(fire);
                        placeFire(pos.add(x, y, z));
                        if (inventory.getValue()) {
                            doSwap(fire);
                            EntityUtil.syncInventory();
                        } else {
                            doSwap(old);
                        }
                        return;
                    }
                }
            }
        }
    }

    public void placeFire(BlockPos pos) {
        BlockPos neighbour = pos.offset(Direction.DOWN);
        BlockUtil.clickBlock(neighbour, Direction.UP, this.rotate.getValue());
    }

    private static boolean canFire(BlockPos pos) {
        if (BlockUtil.canReplace(pos.down())) return false;
        if (!mc.world.isAir(pos)) return false;
        if (!BlockUtil.canClick(pos.offset(Direction.DOWN))) return false;
        return AntiCheat.INSTANCE.placement.getValue() != Placement.Strict || BlockUtil.isStrictDirection(pos.down(), Direction.UP);
    }

    private void doRedStone(BlockPos pos, Direction facing, BlockPos crystalPos) {
        if (!mc.world.isAir(pos.offset(facing, -1)) && getBlock(pos.offset(facing, -1)) != Blocks.FIRE && getBlock(pos.offset(facing.getOpposite())) != Blocks.MOVING_PISTON)
            return;
        for (Direction i : Direction.values()) {
            if (getBlock(pos.offset(i)) == Blocks.REDSTONE_BLOCK) return;
        }
        int power = findBlock(Blocks.REDSTONE_BLOCK);
        if (power == -1) return;
        int old = mc.player.getInventory().selectedSlot;
        Direction bestNeighboring = BlockUtil.getBestNeighboring(pos, facing);
        if (bestNeighboring != null && bestNeighboring != facing.getOpposite() && BlockUtil.canPlace(pos.offset(bestNeighboring), placeRange.getValue()) && !pos.offset(bestNeighboring).equals(crystalPos)) {
            doSwap(power);
            BlockUtil.placeBlock(pos.offset(bestNeighboring), rotate.getValue());
            if (inventory.getValue()) {
                doSwap(power);
                EntityUtil.syncInventory();
            } else {
                doSwap(old);
            }
            return;
        }
        for (Direction i : Direction.values()) {
            if (!BlockUtil.canPlace(pos.offset(i), placeRange.getValue()) || pos.offset(i).equals(crystalPos) || i == facing.getOpposite())
                continue;
            doSwap(power);
            BlockUtil.placeBlock(pos.offset(i), rotate.getValue());
            if (inventory.getValue()) {
                doSwap(power);
                EntityUtil.syncInventory();
            } else {
                doSwap(old);
            }
            return;
        }
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }
    public int findItem(Item itemIn) {
        if (inventory.getValue()) {
            return InventoryUtil.findItemInventorySlot(itemIn);
        } else {
            return InventoryUtil.findItem(itemIn);
        }
    }
    public int findBlock(Block blockIn) {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(blockIn);
        } else {
            return InventoryUtil.findBlock(blockIn);
        }
    }
    public int findClass(Class clazz) {
        if (inventory.getValue()) {
            return InventoryUtil.findClassInventorySlot(clazz);
        } else {
            return InventoryUtil.findClass(clazz);
        }
    }
    private Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }
}
