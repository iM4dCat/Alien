package dev.luminous.mod.modules.impl.misc;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.RotateEvent;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.asm.accessors.ILivingEntity;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.BaritoneModule;
import dev.luminous.mod.modules.impl.combat.KillAura;
import dev.luminous.mod.modules.impl.player.PacketEat;
import dev.luminous.mod.modules.impl.player.PacketMine;
import dev.luminous.mod.modules.settings.SwingSide;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AutoDupe extends Module {
    public static AutoDupe INSTANCE;

    public AutoDupe() {
        super("AutoDupe", Category.Misc);
        setChinese("自动刷物资");
        INSTANCE = this;
    }
    public final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Xin));
    public final BooleanSetting healthCheck = add(new BooleanSetting("AutoEat", true));
    public final BooleanSetting rotate = add(new BooleanSetting("Rotate", true, () -> mode.is(Mode.NPlusOne)));
    public final BooleanSetting inventory = add(new BooleanSetting("Inventory", true, () -> mode.is(Mode.NPlusOne)));


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

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (inventory.getValue()) {
            return InventoryUtil.findClassInventorySlot(ShulkerBoxBlock.class);
        } else {
            return InventoryUtil.findClass(ShulkerBoxBlock.class);
        }
    }
    public enum Mode {
        Xin,
        NPlusOne,
        Turtle
    }
    public enum Stage {
        Open,
        Take,
        Summon,
        Tame,
        Kill
    }

    List<BlockPos> emptyBox = new ArrayList<>();
    Stage stage = Stage.Open;
    BlockPos boxPos;
    boolean closeToBox;
    Timer closeScreen = new Timer();
    Timer openTimeOut = new Timer();
    Timer putTimer = new Timer();
    boolean putIn;
    LlamaEntity llama;

    @Override
    public String getInfo() {
        if (mode.is(Mode.Xin))
            return "Stage:" + stage.name() + ", Riding:" + mc.player.hasVehicle();
        return mode.getValue().name();
    }

    @Override
    public void onEnable() {
        emptyBox.clear();
        stage = Stage.Summon;
        boxPos = null;
        closeToBox = false;
        llama = null;
        putIn = false;
    }

    @Override
    public void onUpdate() {
        if (!PacketEat.INSTANCE.isOn()) {
            CommandManager.sendChatMessage("§4AutoDupe require PacketEat, auto enabled.");
            PacketEat.INSTANCE.enable();
        }
        if (nullCheck()) {
            emptyBox.clear();
            stage = Stage.Summon;
            boxPos = null;
            closeToBox = false;
            llama = null;
            putIn = false;
            return;
        }
        if (healthCheck.getValue() && mc.player.getHealth() < 15 && mc.player.getAbsorptionAmount() <= 3) {
            int golden_apple = InventoryUtil.findItem(Items.ENCHANTED_GOLDEN_APPLE);
            if (golden_apple == -1) {
                golden_apple = InventoryUtil.findItem(Items.GOLDEN_APPLE);
            }
            if (golden_apple != -1) {
                if (mc.currentScreen != null) mc.currentScreen.close();
                InventoryUtil.switchToSlot(golden_apple);
                Module.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            }
            return;
        }
        switch (mode.getValue()) {
            case Turtle -> {
                if (BaritoneModule.isActive()) return;
                //BaritoneAPI.getProvider().
            }
            case NPlusOne -> {
                BlockPos placePos = PacketMine.getBreakPos();
                if (placePos != null && BlockUtil.canPlace(placePos)) {
                    placeBlock(placePos);
                }
            }
            case Xin -> {
                if (llama != null) {
                    if (llama.isDead() || llama.distanceTo(mc.player) > 20) {
                        llama = null;
                    }
                }
                int chestSlot = InventoryUtil.findBlockInventorySlot(Blocks.CHEST);
                int swordSlot = InventoryUtil.findClass(SwordItem.class);
                if (chestSlot == -1) {
                    emptyBox.clear();
                    stage = Stage.Open;
                    boxPos = null;
                    closeToBox = false;
                    llama = null;
                    putIn = false;
                    return;
                }

                int shulkers = 0;
                for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
                    if (entry.getValue().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                        shulkers++;
                    }
                }
                if (shulkers > 18) {
                    if (mc.currentScreen != null) mc.currentScreen.close();
                    //mc.setScreen(null);
                    for (int slot1 = 9; slot1 < 36; ++slot1) {
                        ItemStack stack = mc.player.getInventory().getStack(slot1);
                        if (stack.isEmpty()) continue;
                        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                            shulkers--;
                            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot1, 1, SlotActionType.THROW, mc.player);
                            if (shulkers <= 18) return;
                        }
                    }
                    return;
                }

                if (closeToBox && boxPos != null) {
                    closeTo(boxPos);
                }
                if (!openTimeOut.passed(100)) return;
                switch (stage) {
                    case Open -> {
                        if (!closeScreen.passed(250)) {
                            if (mc.currentScreen != null) mc.currentScreen.close();
                            return;
                        }
                        if (mc.player.hasVehicle()) {
                            mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(0, 0, false, true));
                            return;
                        }
                        if (boxPos == null || emptyBox.contains(boxPos)) {
                            for (BlockPos pos : BlockUtil.getSphere(10)) {
                                if (!emptyBox.contains(pos) && mc.world.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity && BlockUtil.getClickSideStrict(pos) != null) {
                                    closeToBox = false;
                                    boxPos = pos;
                                    break;
                                }
                            }
                        }
                        if (boxPos != null && !emptyBox.contains(boxPos)) {
                            if (mc.player.getEyePos().distanceTo(boxPos.toCenterPos()) < 4) {
                                if (openTimeOut.passedS(1)) {
                                    closeToBox = false;
                                    openTimeOut.reset();
                                    BlockUtil.clickBlock(boxPos, BlockUtil.getClickSide(boxPos), true);
                                    stage = Stage.Take;
                                }
                            } else {
                                closeToBox = true;
                            }
                        }
                    }
                    case Take -> {
                        if (mc.player.hasVehicle()) {
                            mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(0, 0, false, true));
                            return;
                        }
                        if (boxPos == null || emptyBox.contains(boxPos)) {
                            closeScreen.reset();
                            stage = Stage.Open;
                            return;
                        }
                        if (mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler shulker) {
                            boolean egg = false;
                            boolean hay = false;
                            for (Slot slot : shulker.slots) {
                                if (slot.id < 27 && !slot.getStack().isEmpty()) {
                                    if (slot.getStack().getItem() == Items.EGG) {
                                        egg = true;
                                    }
                                    if (slot.getStack().getItem() == Blocks.HAY_BLOCK.asItem()) {
                                        hay = true;
                                    }
                                }
                            }
                            if (egg && hay) {
                                int eggs = 0;
                                int hays = 0;
                                for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
                                    if (entry.getValue().getItem() == Items.EGG) {
                                        eggs++;
                                    }
                                    if (entry.getValue().getItem() == Blocks.HAY_BLOCK.asItem()) {
                                        hays++;
                                    }
                                }
                                for (Slot slot : shulker.slots) {
                                    if (!slot.getStack().isEmpty()) {
                                        if (slot.id < 27) {
                                            if (slot.getStack().getItem() == Items.EGG && eggs < 2) {
                                                eggs++;
                                                mc.interactionManager.clickSlot(shulker.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                                            }
                                            if (slot.getStack().getItem() == Blocks.HAY_BLOCK.asItem() && hays < 2) {
                                                hays++;
                                                mc.interactionManager.clickSlot(shulker.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                                            }
                                        } else {
                                            if (slot.getStack().getItem() == Items.LEATHER) {
                                                mc.interactionManager.clickSlot(shulker.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                                            }
                                        }
                                    }
                                }
                                if (hays < 1 || eggs < 1) {
                                    emptyBox.add(boxPos);
                                }
                                if (mc.currentScreen != null) mc.currentScreen.close();
                                //mc.setScreen(null);
                                stage = Stage.Summon;
                            } else {
                                closeScreen.reset();
                                emptyBox.add(boxPos);
                                stage = Stage.Open;
                            }
                        } else if (openTimeOut.passedS(1)) {
                            closeScreen.reset();
                            stage = Stage.Open;
                        }
                    }
                    case Summon -> {
                        int eggs = 0;
                        int hays = 0;
                        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
                            if (entry.getValue().getItem() == Items.EGG) {
                                eggs++;
                            }
                            if (entry.getValue().getItem() == Blocks.HAY_BLOCK.asItem()) {
                                hays++;
                            }
                        }
                        if (eggs <= 1 || hays <= 1) {
                            closeScreen.reset();
                            stage = Stage.Open;
                            return;
                        }
                        for (Entity entity : mc.world.getEntities()) {
                            if (entity instanceof LlamaEntity llamaEntity && mc.player.getEyePos().distanceTo(entity.getPos()) < 10 && entity.isAlive()) {
                                if (mc.player.getEyePos().distanceTo(entity.getPos()) < 5) {
                                    llama = llamaEntity;
                                    stage = Stage.Tame;
                                } else {
                                    closeTo(entity.getBlockPos());
                                }
                                return;
                            }
                        }
                        if (mc.currentScreen != null) mc.currentScreen.close();
                        //mc.setScreen(null);

                        int slot = InventoryUtil.findItemInventorySlot(Items.EGG);
                        InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
                        Alien.ROTATION.snapAt(Alien.ROTATION.lastYaw, 89);
                        sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
                        InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
                    }
                    case Tame -> {
                        if (llama == null || llama.isDead()) {
                            stage = Stage.Summon;
                            return;
                        }
                        int eggs = 0;
                        int hays = 0;
                        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
                            if (entry.getValue().getItem() == Items.EGG) {
                                eggs++;
                            }
                            if (entry.getValue().getItem() == Blocks.HAY_BLOCK.asItem()) {
                                hays++;
                            }
                        }
                        if (eggs <= 1 || hays <= 1) {
                            closeScreen.reset();
                            stage = Stage.Open;
                            return;
                        }

                        if (mc.player.hasVehicle()) {
                            if (llama.isTame()) {
                                if (llama.hasChest()) {
                                    int moves = 0;
                                    if (mc.player.currentScreenHandler instanceof HorseScreenHandler shulker) {
                                        if (putTimer.passed(250)) {
                                            if (!putIn) {
                                                for (Slot slot : shulker.slots) {
                                                    if (slot.getStack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                                                        mc.interactionManager.clickSlot(shulker.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                                                        moves++;
                                                        putTimer.reset();
                                                        if (moves >= 15) break;
                                                    }
                                                }
                                                putIn = true;
                                            } else {
                                                stage = Stage.Kill;
                                                mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(0, 0, false, true));
                                            }
                                        }
                                    } else {
                                        putIn = false;
                                        putTimer.reset();
                                        mc.player.openRidingInventory();
                                    }
                                } else {
                                    mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(0, 0, false, true));
                                }
                            }
                        } else {
                            if (mc.player.getEyePos().distanceTo(llama.getPos()) > 5) {
                                closeTo(llama.getBlockPos());
                                return;
                            }
                            if (llama.isBaby()) {
                                if (mc.currentScreen != null) mc.currentScreen.close();
                                //mc.setScreen(null);

                                int slot = InventoryUtil.findBlockInventorySlot(Blocks.HAY_BLOCK);
                                InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
                                Alien.ROTATION.lookAt(llama.getPos());
                                mc.interactionManager.interactEntity(mc.player, llama, Hand.MAIN_HAND);
                                InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
                            } else {
                                if (llama.isTame()) {
                                    if (llama.hasChest()) {
                                        for (int i = 0; i < 9; ++i) {
                                            if (mc.player.getInventory().getStack(i).isEmpty()) {
                                                InventoryUtil.switchToSlot(i);
                                                mc.interactionManager.interactEntity(mc.player, llama, Hand.MAIN_HAND);
                                                return;
                                            }
                                        }
                                        for (int i = 0; i < 9; ++i) {
                                            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock || mc.player.getInventory().getStack(i).getItem() == Items.LEATHER) {
                                                InventoryUtil.switchToSlot(i);
                                                mc.player.dropSelectedItem(true);
                                                mc.interactionManager.interactEntity(mc.player, llama, Hand.MAIN_HAND);
                                                return;
                                            }
                                        }
                                    } else {
                                        putTimer.reset();
                                        putIn = false;
                                        if (mc.currentScreen != null) mc.currentScreen.close();
                                        //mc.setScreen(null);

                                        InventoryUtil.inventorySwap(chestSlot, mc.player.getInventory().selectedSlot);
                                        Alien.ROTATION.lookAt(llama.getPos());
                                        mc.interactionManager.interactEntity(mc.player, llama, Hand.MAIN_HAND);
                                        InventoryUtil.inventorySwap(chestSlot, mc.player.getInventory().selectedSlot);
                                    }
                                } else {
                                    if (mc.currentScreen != null) mc.currentScreen.close();
                                    //mc.setScreen(null);

                                    for (int i = 0; i < 9; ++i) {
                                        if (mc.player.getInventory().getStack(i).isEmpty()) {
                                            InventoryUtil.switchToSlot(i);
                                            mc.interactionManager.interactEntity(mc.player, llama, Hand.MAIN_HAND);
                                            return;
                                        }
                                    }
                                    for (int i = 0; i < 9; ++i) {
                                        if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem blockItem) {
                                            if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
                                                InventoryUtil.switchToSlot(i);
                                                mc.player.dropSelectedItem(true);
                                                mc.interactionManager.interactEntity(mc.player, llama, Hand.MAIN_HAND);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    case Kill -> {
                        if (llama == null || llama.isDead()) {
                            llama = null;
                            stage = Stage.Summon;
                            return;
                        }
                        if (mc.currentScreen != null) mc.currentScreen.close();
                        if (mc.player.hasVehicle()) {
                            mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(0, 0, false, true));
                            return;
                        }
                        if (mc.player.getPos().distanceTo(llama.getPos()) > 1) {
                            closeTo(llama.getBlockPos());
                        }
                        if (mc.player.getPos().distanceTo(llama.getPos()) > 2) {
                            return;
                        }
                        InventoryUtil.switchToSlot(swordSlot);
                        if (check()) {
                            Alien.ROTATION.lookAt(llama.getEyePos());
                            mc.interactionManager.attackEntity(mc.player, llama);
                            EntityUtil.swingHand(Hand.MAIN_HAND, SwingSide.All);
                        }

                    }
                }
            }
        }
    }

    @EventHandler
    public void onRotate(RotateEvent event) {
        if (mode.is(Mode.Xin))
        event.setPitch(88);
    }

    private boolean check() {
        int at = ((ILivingEntity) mc.player).getLastAttackedTicks();
        return Math.max(at / KillAura.getAttackCooldownProgressPerTick(), 0.0F) >= 1.3;
    }

    private void closeTo(BlockPos pos) {
        double speed = 0.2873 / 1.5;
        float forward = 1f;
        float side = 0;
        float yaw = Alien.ROTATION.getRotation(pos.toCenterPos())[0];
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        MovementUtil.setMotionX(posX);
        MovementUtil.setMotionZ(posZ);
    }
}