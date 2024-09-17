package dev.luminous.mod.modules.impl.misc;

import dev.luminous.mod.modules.impl.player.PacketMine;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.mod.modules.Module;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class ChestStealer extends Module {
    private final BooleanSetting autoDisable = add(new BooleanSetting("AutoDisable", true));
    private final SliderSetting disableTime =
            add(new SliderSetting("DisableTime", 500, 0, 1000));
    public final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    private final BooleanSetting place = add(new BooleanSetting("Place", true));
    private final BooleanSetting inventory = add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting preferOpen = add(new BooleanSetting("PerferOpen", true));
    private final BooleanSetting open = add(new BooleanSetting("Open", true));
    private final SliderSetting range = add(new SliderSetting("Range", 4.0f, 0.0f, 6f, .1));
    private final SliderSetting minRange = add(new SliderSetting("MinRange", 1.0f, 0.0f, 3f, .1));
    private final BooleanSetting mine = add(new BooleanSetting("Mine", true));
    private final BooleanSetting take = add(new BooleanSetting("Take", true));
    private final BooleanSetting smart = add(new BooleanSetting("Smart", true, take::getValue).setParent());
    private final SliderSetting crystal = add(new SliderSetting("Crystal", 256, 0, 512, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting exp = add(new SliderSetting("Exp", 256, 0, 512, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting totem = add(new SliderSetting("Totem", 6, 0, 36, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting gapple = add(new SliderSetting("Gapple", 128, 0, 512, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting obsidian = add(new SliderSetting("Obsidian", 64, 0, 512, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting web = add(new SliderSetting("Web", 64, 0, 512, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting glowstone = add(new SliderSetting("Glowstone", 256, 0, 512, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting anchor = add(new SliderSetting("Anchor", 256, 0, 512, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting pearl = add(new SliderSetting("Pearl", 16, 0, 64, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting piston = add(new SliderSetting("Piston", 128, 0, 512, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting redstone = add(new SliderSetting("RedStone", 128, 0, 512, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting bed = add(new SliderSetting("Bed", 256, 0, 512, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting speed = add(new SliderSetting("Speed", 1, 0, 8, () -> take.getValue() && smart.isOpen()));
    private final SliderSetting turtle = add(new SliderSetting("Turtle", 1, 0, 8, () -> take.getValue() && smart.isOpen()));
    final int[] stealCountList = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public ChestStealer() {
        super("ChestStealer", Category.Misc);
        setChinese("自动补给");
    }

    public int findShulker() {
        if (inventory.getValue()) return InventoryUtil.findClassInventorySlot(ShulkerBoxBlock.class);
        return InventoryUtil.findClass(ShulkerBoxBlock.class);
    }

    private final Timer timer = new Timer();
    BlockPos placePos = null;
    private final Timer disableTimer = new Timer();
    @Override
    public void onEnable() {
        openPos = null;
        disableTimer.reset();
        placePos = null;
        if (nullCheck()) {
            return;
        }
        int oldSlot = mc.player.getInventory().selectedSlot;
        if (!this.place.getValue()) {
            return;
        }
        double distance = 100;
        BlockPos bestPos = null;
        for (BlockPos pos : BlockUtil.getSphere((float) range.getValue())) {
            if (!mc.world.isAir(pos.up())) continue;
            if (preferOpen.getValue() && mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) return;
            if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos())) < minRange.getValue()) continue;
            if (!BlockUtil.clientCanPlace(pos, false)
                    || !BlockUtil.isStrictDirection(pos.offset(Direction.DOWN), Direction.UP)
                    || !BlockUtil.canClick(pos.offset(Direction.DOWN))
            ) continue;
            if (bestPos == null || MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos())) < distance) {
                distance = MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos()));
                bestPos = pos;
            }
        }
        if (bestPos != null) {
            if (this.findShulker() == -1) {
                CommandManager.sendChatMessage("§cNo shulkerbox found");
                return;
            }
            if (inventory.getValue()) {
                int slot = findShulker();
                InventoryUtil.inventorySwap(slot, oldSlot);
                placeBlock(bestPos);
                placePos = bestPos;
                InventoryUtil.inventorySwap(slot, oldSlot);
            } else {
                InventoryUtil.switchToSlot(this.findShulker());
                placeBlock(bestPos);
                placePos = bestPos;
                InventoryUtil.switchToSlot(oldSlot);
            }
            timer.reset();
        } else {
            CommandManager.sendChatMessage("§c[!] No place pos found");
        }
    }

    private void update() {
        this.stealCountList[0] = (int) (this.crystal.getValue() - InventoryUtil.getItemCount(Items.END_CRYSTAL));
        this.stealCountList[1] = (int) (this.exp.getValue() - InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE));
        this.stealCountList[2] = (int) (this.totem.getValue() - InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING));
        this.stealCountList[3] = (int) (this.gapple.getValue() - InventoryUtil.getItemCount(Items.ENCHANTED_GOLDEN_APPLE));
        this.stealCountList[4] = (int) (this.obsidian.getValue() - InventoryUtil.getItemCount(Blocks.OBSIDIAN.asItem()));
        this.stealCountList[5] = (int) (this.web.getValue() - InventoryUtil.getItemCount(Blocks.COBWEB.asItem()));
        this.stealCountList[6] = (int) (this.glowstone.getValue() - InventoryUtil.getItemCount(Blocks.GLOWSTONE.asItem()));
        this.stealCountList[7] = (int) (this.anchor.getValue() - InventoryUtil.getItemCount(Blocks.RESPAWN_ANCHOR.asItem()));
        this.stealCountList[8] = (int) (this.pearl.getValue() - InventoryUtil.getItemCount(Items.ENDER_PEARL));
        this.stealCountList[9] = (int) (this.piston.getValue() - InventoryUtil.getItemCount(Blocks.PISTON.asItem()) - InventoryUtil.getItemCount(Blocks.STICKY_PISTON.asItem()));
        this.stealCountList[10] = (int) (this.redstone.getValue() - InventoryUtil.getItemCount(Blocks.REDSTONE_BLOCK.asItem()));
        this.stealCountList[11] = (int) (this.bed.getValue() - InventoryUtil.getItemCount(BedBlock.class));
        this.stealCountList[12] = (int) (this.speed.getValue() - InventoryUtil.getPotionCount(StatusEffects.SPEED));
        this.stealCountList[13] = (int) (this.turtle.getValue() - InventoryUtil.getPotionCount(StatusEffects.RESISTANCE));
    }

    @Override
    public void onDisable() {
        opend = false;
        if (mine.getValue()) {
            if (placePos != null) {
                PacketMine.INSTANCE.mine(placePos);
            }
        }
    }
    BlockPos openPos;

    boolean opend = false;
    @Override
    public void onUpdate() {
        if (smart.getValue()) update();
        if (!(mc.currentScreen instanceof ShulkerBoxScreen)) {
            if (opend) {
                opend = false;
                if (autoDisable.getValue()) disable2();
                if (mine.getValue()) {
                    if (openPos != null) {
                        if (mc.world.getBlockState(openPos).getBlock() instanceof ShulkerBoxBlock) {
                            PacketMine.INSTANCE.mine(openPos);
                        } else {
                            openPos = null;
                        }
                    }
                }
                return;
            }
            if (open.getValue()) {
                if (placePos != null && MathHelper.sqrt((float) mc.player.squaredDistanceTo(placePos.toCenterPos())) <= range.getValue() && mc.world.isAir(placePos.up()) && (!timer.passedMs(500) || mc.world.getBlockState(placePos).getBlock() instanceof ShulkerBoxBlock)) {
                    if (mc.world.getBlockState(placePos).getBlock() instanceof ShulkerBoxBlock) {
                        openPos = placePos;
                        BlockUtil.clickBlock(placePos, BlockUtil.getClickSide(placePos), rotate.getValue());
                    }
                } else {
                    boolean found = false;
                    for (BlockPos pos : BlockUtil.getSphere((float) range.getValue())) {
                        if (!mc.world.isAir(pos.up())) continue;
                        if (mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
                            openPos = pos;
                            BlockUtil.clickBlock(pos, BlockUtil.getClickSide(pos), rotate.getValue());
                            found = true;
                            break;
                        }
                    }
                    if (!found && autoDisable.getValue()) this.disable2();
                }
            } else if (!this.take.getValue()) {
                if (autoDisable.getValue()) this.disable2();
            }
            return;
        }
        opend = true;
        if (!this.take.getValue()) {
            if (autoDisable.getValue()) this.disable2();
            return;
        }
        boolean take = false;
        if (mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler shulker) {
            for (Slot slot : shulker.slots) {
                if (slot.id < 27 && !slot.getStack().isEmpty() && (!smart.getValue() || needSteal(slot.getStack()))) {
                    mc.interactionManager.clickSlot(shulker.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                    take = true;
                }
            }
        }
        if (autoDisable.getValue() && !take) this.disable2();
    }

    private void disable2() {
        if (disableTimer.passedMs(disableTime.getValueInt()))
            disable();
    }
    private boolean needSteal(final ItemStack i) {
        if (i.getItem().equals(Items.END_CRYSTAL) && this.stealCountList[0] > 0) {
            stealCountList[0] = stealCountList[0] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.EXPERIENCE_BOTTLE) && this.stealCountList[1] > 0) {
            stealCountList[1] = stealCountList[1] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.TOTEM_OF_UNDYING) && this.stealCountList[2] > 0) {
            stealCountList[2] = stealCountList[2] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE) && this.stealCountList[3] > 0) {
            stealCountList[3] = stealCountList[3] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Blocks.OBSIDIAN.asItem()) && this.stealCountList[4] > 0) {
            stealCountList[4] = stealCountList[4] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Blocks.COBWEB.asItem()) && this.stealCountList[5] > 0) {
            stealCountList[5] = stealCountList[5] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Blocks.GLOWSTONE.asItem()) && this.stealCountList[6] > 0) {
            stealCountList[6] = stealCountList[6] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Blocks.RESPAWN_ANCHOR.asItem()) && this.stealCountList[7] > 0) {
            stealCountList[7] = stealCountList[7] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.ENDER_PEARL) && this.stealCountList[8] > 0) {
            stealCountList[8] = stealCountList[8] - i.getCount();
            return true;
        }
        if (i.getItem() instanceof BlockItem && ((BlockItem) i.getItem()).getBlock() instanceof PistonBlock) {
            if (this.stealCountList[9] > 0) {
                stealCountList[9] = stealCountList[9] - i.getCount();
                return true;
            }
        }
        if (i.getItem().equals(Blocks.REDSTONE_BLOCK.asItem()) && this.stealCountList[10] > 0) {
            stealCountList[10] = stealCountList[10] - i.getCount();
            return true;
        }
        if (i.getItem() instanceof BlockItem && ((BlockItem) i.getItem()).getBlock() instanceof BedBlock) {
            if (this.stealCountList[11] > 0) {
                stealCountList[11] = stealCountList[11] - i.getCount();
                return true;
            }
        }
        if (Item.getRawId(i.getItem()) == Item.getRawId(Items.SPLASH_POTION)) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(i);
            for (StatusEffectInstance effect : effects) {
                if (effect.getEffectType() == StatusEffects.SPEED) {
                    if (this.stealCountList[12] > 0) {
                        stealCountList[12] = stealCountList[12] - i.getCount();
                        return true;
                    }
                } else if (effect.getEffectType() == StatusEffects.RESISTANCE) {
                    if (this.stealCountList[13] > 0) {
                        stealCountList[13] = stealCountList[13] - i.getCount();
                        return true;
                    }
                }
            }
        }

        return false;
    }
    private void placeBlock(BlockPos pos) {
        BlockUtil.clickBlock(pos.offset(Direction.DOWN), Direction.UP, rotate.getValue());
    }
}