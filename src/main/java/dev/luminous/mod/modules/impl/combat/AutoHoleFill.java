package dev.luminous.mod.modules.impl.combat;

import dev.luminous.Alien;
import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.exploit.Blink;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class AutoHoleFill extends Module {
    public static AutoHoleFill INSTANCE;
    private final Timer timer = new Timer();
    public final SliderSetting placeDelay =
            add(new SliderSetting("PlaceDelay", 50, 0, 500).setSuffix("ms"));
    private final SliderSetting blocksPer =
            add(new SliderSetting("BlocksPer", 1, 1, 8));
    private final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 5, 0, 8.).setSuffix("m"));
    private final SliderSetting enemyRange =
            add(new SliderSetting("EnemyRange", 6, 0, 8.).setSuffix("m"));
    private final SliderSetting holeRange =
            add(new SliderSetting("HoleRange", 2, 0, 8.).setSuffix("m"));
    private final SliderSetting selfRange =
            add(new SliderSetting("SelfRange", 2, 0, 8.).setSuffix("m"));
    private final SliderSetting predictTicks =
            add(new SliderSetting("Predict", 1, 1, 8).setSuffix("tick"));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true));
    private final BooleanSetting packetPlace =
            add(new BooleanSetting("PacketPlace", true));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("Break", true).setParent());
    private final BooleanSetting eatPause =
            add(new BooleanSetting("EatingPause", true, () -> breakCrystal.isOpen()));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    public final BooleanSetting inAirPause =
            add(new BooleanSetting("InAirPause", true));
    private final BooleanSetting web =
            add(new BooleanSetting("Web", true));

    int progress = 0;
    public AutoHoleFill() {
        super("AutoHoleFill", Category.Combat);
        setChinese("自动填坑");
        INSTANCE = this;
    }


    @Override
    public void onUpdate() {
        if (!timer.passedMs((long) placeDelay.getValue())) return;
        progress = 0;

        if (getBlock() == -1) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) return;
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return;
        }
        if (inAirPause.getValue() && !mc.player.isOnGround()) return;
        CombatUtil.getEnemies(enemyRange.getValue()).stream()
                .flatMap(enemy -> BlockUtil.getSphere(holeRange.getValueFloat(), CombatUtil.getEntityPosVec(enemy, predictTicks.getValueInt())).stream())
                .filter(pos -> pos.toCenterPos().distanceTo(mc.player.getPos()) > selfRange.getValue() && (Alien.HOLE.isHole(pos, true, true, false) || Alien.HOLE.isDoubleHole(pos)))
                .distinct()
                .forEach(this::tryPlaceBlock);
    }

    private void tryPlaceBlock(BlockPos pos) {
        if (pos == null) return;
        if (detectMining.getValue() && Alien.BREAK.isMining(pos)) return;
        if (!(progress < blocksPer.getValue())) return;
        int block = getBlock();
        if (block == -1) return;

        if (!BlockUtil.canPlace(pos, placeRange.getValue(), true)) return;
        if (breakCrystal.getValue()) {
            CombatUtil.attackCrystal(pos, rotate.getValue(), eatPause.getValue());
        } else if (BlockUtil.hasEntity(pos, false)) return;
        int old = mc.player.getInventory().selectedSlot;
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue(), packetPlace.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
        progress++;
        timer.reset();
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
            if (web.getValue() && InventoryUtil.findBlockInventorySlot(Blocks.COBWEB) != -1) {
                return InventoryUtil.findBlockInventorySlot(Blocks.COBWEB);
            }
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        } else {
            if (web.getValue() && InventoryUtil.findBlock(Blocks.COBWEB) != -1) {
                return InventoryUtil.findBlock(Blocks.COBWEB);
            }
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
    }
}
