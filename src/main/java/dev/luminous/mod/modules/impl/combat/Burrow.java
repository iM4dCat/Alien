package dev.luminous.mod.modules.impl.combat;

import dev.luminous.Alien;
import dev.luminous.core.impl.CommandManager;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

import static dev.luminous.api.utils.world.BlockUtil.canReplace;


public class Burrow extends Module {
    public static Burrow INSTANCE;
    private final Timer timer = new Timer();
    private final Timer webTimer = new Timer();
    private final BooleanSetting disable = add(new BooleanSetting("Disable", true));
    private final SliderSetting delay = add(new SliderSetting("Delay", 500, 0, 1000, () -> !disable.getValue()));
    private final SliderSetting webTime = add(new SliderSetting("WebTime", 0, 0, 500));
    private final BooleanSetting enderChest = add(new BooleanSetting("EnderChest", true));
    private final BooleanSetting antiLag = add(new BooleanSetting("AntiLag", false));
    private final BooleanSetting detectMine = add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting headFill = add(new BooleanSetting("HeadFill", false));
    private final BooleanSetting usingPause = add(new BooleanSetting("UsingPause", false));
    private final BooleanSetting down = add(new BooleanSetting("Down", true));
    private final BooleanSetting noSelfPos = add(new BooleanSetting("NoSelfPos", false));
    private final BooleanSetting packetPlace =
            add(new BooleanSetting("PacketPlace", true));
    private final BooleanSetting sound =
            add(new BooleanSetting("Sound", true));
    private final SliderSetting blocksPer = add(new SliderSetting("BlocksPer", 4, 1, 4, 1));
    private final EnumSetting<RotateMode> rotate = add(new EnumSetting<>("RotateMode", RotateMode.Bypass));
    private final BooleanSetting breakCrystal = add(new BooleanSetting("Break", true));
    private final BooleanSetting wait = add(new BooleanSetting("Wait", true, () -> !disable.getValue()));
    private final BooleanSetting fakeMove = add(new BooleanSetting("FakeMove", true).setParent());
    private final BooleanSetting center = add(new BooleanSetting("AllowCenter", false, () -> fakeMove.isOpen()));
    private final BooleanSetting inventory = add(new BooleanSetting("InventorySwap", true));
    private final EnumSetting<LagBackMode> lagMode = add(new EnumSetting<>("LagMode", LagBackMode.TrollHack));
    private final EnumSetting<LagBackMode> aboveLagMode = add(new EnumSetting<>("MoveLagMode", LagBackMode.Smart));
    private final SliderSetting smartX = add(new SliderSetting("SmartXZ", 3, 0, 10, 0.1, () -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartUp = add(new SliderSetting("SmartUp", 3, 0, 10, 0.1, () -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartDown = add(new SliderSetting("SmartDown", 3, 0, 10, 0.1, () -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartDistance = add(new SliderSetting("SmartDistance", 2, 0, 10, 0.1, () -> lagMode.getValue() == LagBackMode.Smart || aboveLagMode.getValue() == LagBackMode.Smart));
    private int progress = 0;
    private final List<BlockPos> placePos = new ArrayList<>();

    public Burrow() {
        super("Burrow", Category.Combat);
        setChinese("卡黑曜石");
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (Alien.PLAYER.isInWeb(mc.player)) {
            webTimer.reset();
            return;
        }
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return;
        }
        if (!webTimer.passed(webTime.getValue())) {
            return;
        }
        if (!disable.getValue() && !timer.passed(delay.getValue())) {
            return;
        }
        if (!mc.player.isOnGround()) {
            return;
        }
        if (antiLag.getValue()) {
            if (!mc.world.getBlockState(EntityUtil.getPlayerPos(true).down()).blocksMovement()) return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) return;
        int oldSlot = mc.player.getInventory().selectedSlot;
        int block;
        if ((block = getBlock()) == -1) {
            CommandManager.sendChatMessageWidthId("§c§oObsidian" + (enderChest.getValue() ? "/EnderChest" : "") + "?", hashCode());
            disable();
            return;
        }
        progress = 0;
        placePos.clear();
        double offset = AntiCheat.getOffset();
        BlockPos pos1 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 0.5, mc.player.getZ() + offset);
        BlockPos pos2 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 0.5, mc.player.getZ() + offset);
        BlockPos pos3 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 0.5, mc.player.getZ() - offset);
        BlockPos pos4 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 0.5, mc.player.getZ() - offset);
        BlockPos pos5 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 1.5, mc.player.getZ() + offset);
        BlockPos pos6 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 1.5, mc.player.getZ() + offset);
        BlockPos pos7 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 1.5, mc.player.getZ() - offset);
        BlockPos pos8 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 1.5, mc.player.getZ() - offset);
        BlockPos pos9 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() - 1, mc.player.getZ() + offset);
        BlockPos pos10 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() - 1, mc.player.getZ() + offset);
        BlockPos pos11 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() - 1, mc.player.getZ() - offset);
        BlockPos pos12 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() - 1, mc.player.getZ() - offset);
        BlockPos playerPos = EntityUtil.getPlayerPos(true);
        boolean headFill = false;
        if (!canPlace(pos1) && !canPlace(pos2) && !canPlace(pos3) && !canPlace(pos4)) {
            boolean cantHeadFill = !this.headFill.getValue() || !canPlace(pos5) && !canPlace(pos6) && !canPlace(pos7) && !canPlace(pos8);
            boolean cantDown = !down.getValue() || !canPlace(pos9) && !canPlace(pos10) && !canPlace(pos11) && !canPlace(pos12);
            if (cantHeadFill) {
                if (cantDown) {
                    if (!wait.getValue() && disable.getValue()) {
                        disable();
                    }
                    return;
                }
            } else {
                headFill = true;
            }
        }
        boolean above = false;
        BlockPos headPos = EntityUtil.getPlayerPos(true).up(2);
        boolean rotate = this.rotate.getValue() == RotateMode.Normal;
        CombatUtil.attackCrystal(pos1, rotate, false);
        CombatUtil.attackCrystal(pos2, rotate, false);
        CombatUtil.attackCrystal(pos3, rotate, false);
        CombatUtil.attackCrystal(pos4, rotate, false);
        if (headFill || mc.player.isCrawling() || trapped(headPos) || trapped(headPos.add(1, 0, 0)) || trapped(headPos.add(-1, 0, 0)) || trapped(headPos.add(0, 0, 1)) || trapped(headPos.add(0, 0, -1)) || trapped(headPos.add(1, 0, -1)) || trapped(headPos.add(-1, 0, -1)) || trapped(headPos.add(1, 0, 1)) || trapped(headPos.add(-1, 0, 1))) {
            above = true;
            if (!fakeMove.getValue()) {
                if (!wait.getValue() && disable.getValue()) disable();
                return;
            }
            boolean moved = false;
            BlockPos offPos = playerPos;
            if (checkSelf(offPos) && !canReplace(offPos) && (!this.headFill.getValue() || !canReplace(offPos.up()))) {
                gotoPos(offPos);
            } else {
                for (final Direction facing : Direction.values()) {
                    if (facing == Direction.UP || facing == Direction.DOWN) continue;
                    offPos = playerPos.offset(facing);
                    if (checkSelf(offPos) && !canReplace(offPos) && (!this.headFill.getValue() || !canReplace(offPos.up()))) {
                        gotoPos(offPos);
                        moved = true;
                        break;
                    }
                }
                if (!moved) {
                    for (final Direction facing : Direction.values()) {
                        if (facing == Direction.UP || facing == Direction.DOWN) continue;
                        offPos = playerPos.offset(facing);
                        if (checkSelf(offPos)) {
                            gotoPos(offPos);
                            moved = true;
                            break;
                        }
                    }
                    if (!moved) {
                        if (!center.getValue()) {
                            return;
                        }
                        for (final Direction facing : Direction.values()) {
                            if (facing == Direction.UP || facing == Direction.DOWN) continue;
                            offPos = playerPos.offset(facing);
                            if (canMove(offPos)) {
                                gotoPos(offPos);
                                moved = true;
                                break;
                            }
                        }
                        if (!moved) {
                            if (!wait.getValue() && disable.getValue()) disable();
                            return;
                        }
                    }
                }
            }
        } else {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.4199999868869781, mc.player.getZ(), false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.7531999805212017, mc.player.getZ(), false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.9999957640154541, mc.player.getZ(), false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.1661092609382138, mc.player.getZ(), false));
        }
        timer.reset();
        doSwap(block);
        if (this.rotate.getValue() == RotateMode.Bypass) {
            Alien.ROTATION.snapAt(Alien.ROTATION.rotationYaw, 90);
        }
        placeBlock(playerPos, rotate);
        placeBlock(pos1, rotate);
        placeBlock(pos2, rotate);
        placeBlock(pos3, rotate);
        placeBlock(pos4, rotate);
        if (down.getValue()) {
            placeBlock(pos9, rotate);
            placeBlock(pos10, rotate);
            placeBlock(pos11, rotate);
            placeBlock(pos12, rotate);
        }
        if (this.headFill.getValue() && above) {
            placeBlock(pos5, rotate);
            placeBlock(pos6, rotate);
            placeBlock(pos7, rotate);
            placeBlock(pos8, rotate);
        }
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(oldSlot);
        }
        switch (above ? aboveLagMode.getValue() : lagMode.getValue()) {
            case Smart -> {
                ArrayList<BlockPos> list = new ArrayList<>();
                for (double x = mc.player.getPos().getX() - smartX.getValue(); x < mc.player.getPos().getX() + smartX.getValue(); ++x) {
                    for (double z = mc.player.getPos().getZ() - smartX.getValue(); z < mc.player.getPos().getZ() + smartX.getValue(); ++z) {
                        for (double y = mc.player.getPos().getY() - smartDown.getValue(); y < mc.player.getPos().getY() + smartUp.getValue(); ++y) {
                            list.add(new BlockPosX(x, y, z));
                        }
                    }
                }

                double distance = 0;
                BlockPos bestPos = null;
                for (BlockPos pos : list) {
                    if (!canMove(pos)) continue;
                    if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos().add(0, -0.5, 0))) < smartDistance.getValue()) continue;
                    if (bestPos == null || mc.player.squaredDistanceTo(pos.toCenterPos()) < distance) {
                        bestPos = pos;
                        distance = mc.player.squaredDistanceTo(pos.toCenterPos());
                    }
                }
                if (bestPos != null) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(bestPos.getX() + 0.5, bestPos.getY(), bestPos.getZ() + 0.5, false));
                }
            }
            case Invalid -> {
                for (int i = 0; i < 20; i++)
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1337, mc.player.getZ(), false));
            }
            case Fly -> {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.16610926093821, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.170005801788139, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.2426308013947485, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.3400880035762786, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.6400880035762786, mc.player.getZ(), false));
            }
            case Glide -> {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.0001, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.0405, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.0802, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.1027, mc.player.getZ(), false));
            }
            case TrollHack -> mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.3400880035762786, mc.player.getZ(), false));
            case Normal -> mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.9, mc.player.getZ(), false));
            case ToVoid -> mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), -70, mc.player.getZ(), false));
            case ToVoid2 -> mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), -7, mc.player.getZ(), false));
            case Rotation -> {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(-180, -90, false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(180, 90, false));
            }
        }
        if (disable.getValue()) disable();
    }


    private void placeBlock(BlockPos pos, boolean rotate) {
        if (canPlace(pos) && !placePos.contains(pos) && progress < blocksPer.getValueInt()) {
            placePos.add(pos);
            if (BlockUtil.airPlace()) {
                progress++;
                BlockUtil.placedPos.add(pos);
                if (sound.getValue()) mc.world.playSound(mc.player, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
                BlockUtil.clickBlock(pos, Direction.DOWN, rotate, packetPlace.getValue());
            }
            Direction side;
            if ((side = BlockUtil.getPlaceSide(pos)) == null) return;
            progress++;
            BlockUtil.placedPos.add(pos);
            if (sound.getValue()) mc.world.playSound(mc.player, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate, packetPlace.getValue());
        }
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private void gotoPos(BlockPos offPos) {
        //mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.2, mc.player.getZ(), false));
        if (rotate.getValue() == RotateMode.None) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(offPos.getX() + 0.5, mc.player.getY() + 0.1, offPos.getZ() + 0.5, false));
        } else {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(offPos.getX() + 0.5, mc.player.getY() + 0.1, offPos.getZ() + 0.5, Alien.ROTATION.rotationYaw, 90, false));
        }
    }

    private boolean canMove(BlockPos pos) {
        return mc.world.isAir(pos) && mc.world.isAir(pos.up());
    }

    private boolean canPlace(BlockPos pos) {
        if (noSelfPos.getValue() && pos.equals(EntityUtil.getPlayerPos(true))) {
            return false;
        }
        if (!BlockUtil.airPlace() && BlockUtil.getPlaceSide(pos) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if (detectMine.getValue() && Alien.BREAK.isMining(pos)) {
            return false;
        }
        return !hasEntity(pos);
    }

    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : BlockUtil.getEntities(new Box(pos))) {
            if (entity == mc.player) continue;
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof EndCrystalEntity && breakCrystal.getValue() || entity instanceof ArmorStandEntity && AntiCheat.INSTANCE.obsMode.getValue())
                continue;
            return true;
        }
        return false;
    }

    private boolean checkSelf(BlockPos pos) {
        return mc.player.getBoundingBox().intersects(new Box(pos));
    }

    private boolean trapped(BlockPos pos) {
        return (mc.world.canCollide(mc.player, new Box(pos)) || BlockUtil.getBlock(pos) == Blocks.COBWEB) && checkSelf(pos.down(2));
    }

    private int getBlock() {
        if (inventory.getValue()) {
            if (InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST);
        } else {
            if (InventoryUtil.findBlock(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlock(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlock(Blocks.ENDER_CHEST);
        }
    }

    private enum RotateMode {Bypass, Normal, None}

    private enum LagBackMode {
        Smart, Invalid, TrollHack, ToVoid, ToVoid2, Normal, Rotation, Fly, Glide
    }
}