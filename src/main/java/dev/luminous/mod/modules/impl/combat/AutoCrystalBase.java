package dev.luminous.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.LookAtEvent;
import dev.luminous.api.events.impl.UpdateWalkingPlayerEvent;
import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.ExplosionUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.Alien;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.AntiCheat;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import dev.luminous.mod.modules.impl.exploit.Blink;
import dev.luminous.mod.modules.impl.player.PacketMine;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import static dev.luminous.api.utils.world.BlockUtil.hasCrystal;


public class AutoCrystalBase extends Module {
    public static AutoCrystalBase INSTANCE;
    public static BlockPos crystalPos;

    private final Timer placeTimer = new Timer();
    private final Timer delayTimer = new Timer();
    private final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
    //General
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true, () -> page.getValue() == Page.General));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", false, () -> page.getValue() == Page.General));
    private final BooleanSetting breakOnlyHasCrystal = add(new BooleanSetting("OnlyHold", true, () -> page.getValue() == Page.General));
    private final BooleanSetting eatingPause = add(new BooleanSetting("EatingPause", true, () -> page.getValue() == Page.General));
    private final SliderSetting targetRange = add(new SliderSetting("TargetRange", 12.0, 0.0, 20.0, () -> page.getValue() == Page.General).setSuffix("m"));
    private final SliderSetting updateDelay = add(new SliderSetting("UpdateDelay", 50, 0, 1000, () -> page.getValue() == Page.General).setSuffix("ms"));
    private final SliderSetting wallRange = add(new SliderSetting("WallRange", 6.0, 0.0, 6.0, () -> page.getValue() == Page.General).setSuffix("m"));
    //Rotate
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true, () -> page.getValue() == Page.Rotate));
    private final BooleanSetting yawStep =
            add(new BooleanSetting("YawStep", false, () -> page.getValue() == Page.Rotate));
    private final SliderSetting steps =
            add(new SliderSetting("Steps", 0.05, 0, 1, 0.01, () -> page.getValue() == Page.Rotate));
    private final BooleanSetting checkFov =
            add(new BooleanSetting("OnlyLooking", true, () -> page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 5f, 0f, 30f, () -> checkFov.getValue() && page.getValue() == Page.Rotate));
    private final SliderSetting priority = add(new SliderSetting("Priority", 10,0 ,100, () ->page.getValue() == Page.Rotate));
    //Place
    private final BooleanSetting auto = add(new BooleanSetting("Auto", true, () -> page.getValue() == Page.Interact));
    private final SliderSetting autoMinDamage = add(new SliderSetting("AMin", 5.0, 0.0, 36.0, () -> page.getValue() == Page.Interact && auto.getValue()).setSuffix("dmg"));
    private final SliderSetting minDamage = add(new SliderSetting("Min", 5.0, 0.0, 36.0, () -> page.getValue() == Page.Interact).setSuffix("dmg"));
    private final SliderSetting maxSelf = add(new SliderSetting("Self", 12.0, 0.0, 36.0, () -> page.getValue() == Page.Interact).setSuffix("dmg"));

    private final SliderSetting range = add(new SliderSetting("Range", 5.0, 0.0, 6, () -> page.getValue() == Page.Interact).setSuffix("m"));
    private final SliderSetting placeRange = add(new SliderSetting("PlaceRange", 5.0, 0.0, 6, () -> page.getValue() == Page.Interact).setSuffix("m"));
    private final SliderSetting noSuicide = add(new SliderSetting("NoSuicide", 3.0, 0.0, 10.0, () -> page.getValue() == Page.Interact).setSuffix("dmg"));
    private final BooleanSetting smart = add(new BooleanSetting("Smart", true, () -> page.getValue() == Page.Interact));
    private final SliderSetting placeDelay = add(new SliderSetting("PlaceDelay", 300, 0, 1000, () -> page.getValue() == Page.Interact).setSuffix("ms"));

    //Calc
    private final BooleanSetting useThread = add(new BooleanSetting("UseThread", true, () -> page.getValue() == Page.Calc));
    private final BooleanSetting doCrystal = add(new BooleanSetting("CalcDoPlace", false, () -> page.getValue() == Page.Calc));
    private final BooleanSetting lite = add(new BooleanSetting("Lite", false, () -> page.getValue() == Page.Calc));
    private final SliderSetting predictTicks = add(new SliderSetting("Predict", 4, 0, 10, () -> page.getValue() == Page.Calc).setSuffix("ticks"));
    private final BooleanSetting terrainIgnore = add(new BooleanSetting("TerrainIgnore", true, () -> page.getValue() == Page.Calc));
    //Misc
    private final BooleanSetting forcePlace = add(new BooleanSetting("ForcePlace", true, () -> page.getValue() == Page.Misc).setParent());
    private final SliderSetting forceMaxHealth = add(new SliderSetting("LowerThan", 7, 0, 36, () -> page.getValue() == Page.Misc && forcePlace.isOpen()).setSuffix("health"));
    private final SliderSetting forceMin = add(new SliderSetting("ForceMin", 1.5, 0.0, 36.0, () -> page.getValue() == Page.Misc && forcePlace.isOpen()).setSuffix("dmg"));
    private final BooleanSetting armorBreaker = add(new BooleanSetting("ArmorBreaker", true, () -> page.getValue() == Page.Misc).setParent());
    private final SliderSetting maxDurable = add(new SliderSetting("MaxDurable", 8, 0, 100, () -> page.getValue() == Page.Misc && armorBreaker.isOpen()).setSuffix("%"));
    private final SliderSetting armorBreakerDamage = add(new SliderSetting("BreakerMin", 3.0, 0.0, 36.0, () -> page.getValue() == Page.Misc && armorBreaker.isOpen()).setSuffix("dmg"));
    private final SliderSetting hurtTime = add(new SliderSetting("HurtTime", 10, 0, 10, 1, () -> page.getValue() == Page.Misc));
    public PlayerEntity displayTarget;
    public float breakDamage, tempDamage, lastDamage;
    private BlockPos tempPos;

    public AutoCrystalBase() {
        super("AutoCrystalBase", Category.Combat);
        setChinese("自动水晶底座");
        INSTANCE = this;
    }

    public static boolean canSee(Vec3d from, Vec3d to) {
        HitResult result = mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    @Override
    public String getInfo() {
        if (displayTarget != null && lastDamage > 0)
            return displayTarget.getName().getString() + ", " + new DecimalFormat("0.0").format(lastDamage);
        return null;
    }

    @Override
    public void onDisable() {
        crystalPos = null;
        tempPos = null;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
    }

    @Override
    public void onThread() {
        if (useThread.getValue()) {
            updateCrystalPos();
        }
    }
    @EventHandler
    public void onRotate(LookAtEvent event) {
        if (directionVec != null && rotate.getValue() && yawStep.getValue()) {
            event.setTarget(directionVec, steps.getValueFloat(), priority.getValueFloat());
        }
    }

    @Override
    public void onUpdate() {
        if (!useThread.getValue()) {
            updateCrystalPos();
        }
        doInteract();
    }
    public Vec3d directionVec = null;
    private boolean faceVector(Vec3d directionVec) {
        if (!yawStep.getValue()) {
            Alien.ROTATION.lookAt(directionVec);
            return true;
        } else {
            this.directionVec = directionVec;
            if (Alien.ROTATION.inFov(directionVec, fov.getValueFloat())) {
                return true;
            }
        }
        return !checkFov.getValue();
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        if (!useThread.getValue()) updateCrystalPos();
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (!useThread.getValue()) updateCrystalPos();
    }

    private void doInteract() {
        directionVec = null;
        if (crystalPos != null) {
            doPlace(crystalPos);
        }
    }

    private void updateCrystalPos() {
        update();
        lastDamage = tempDamage;
        crystalPos = tempPos;
    }

    private void update() {
        if (nullCheck()) return;
        if (!delayTimer.passedMs((long) updateDelay.getValue())) return;
        if (eatingPause.getValue() && mc.player.isUsingItem()) {
            tempPos = null;
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            tempPos = null;
            return;
        }
        if (breakOnlyHasCrystal.getValue() && !mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !AutoCrystal.INSTANCE.findCrystal()) {
            tempPos = null;
            return;
        }
        delayTimer.reset();
        breakDamage = 0;
        tempPos = null;
        tempDamage = 0f;
        ArrayList<PlayerAndPredict> list = new ArrayList<>();
        for (PlayerEntity target : CombatUtil.getEnemies(targetRange.getValueFloat())) {
            if (target.hurtTime <= hurtTime.getValueInt()) {
                list.add(new PlayerAndPredict(target));
            }
        }
        PlayerAndPredict self = new PlayerAndPredict(mc.player);
        if (!list.isEmpty()) {
            for (BlockPos pos : BlockUtil.getSphere((float) range.getValue() + 1)) {
                CombatUtil.modifyPos = null;
                if (mc.player.getEyePos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > range.getValue()) {
                    continue;
                }
                if (!canPlaceCrystal(pos, true, false)) continue;
                CombatUtil.modifyPos = pos.down();
                CombatUtil.modifyBlockState = Blocks.OBSIDIAN.getDefaultState();
                if (behindWall(pos)) continue;
                if (!canTouch(pos.down())) continue;
                for (PlayerAndPredict pap : list) {
                    if (pos.down().getY() > pap.player.getBlockY()) continue;
                    if (lite.getValue() && liteCheck(pos.toCenterPos().add(0, -0.5, 0), pap.predict.getPos())) {
                        continue;
                    }
                    float damage = calculateDamage(pos, pap.player, pap.predict);
                    if (tempPos == null || damage > tempDamage) {
                        float selfDamage = calculateDamage(pos, self.player, self.predict);
                        if (selfDamage > maxSelf.getValue()) continue;
                        if (noSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - noSuicide.getValue())
                            continue;
                        if (damage < EntityUtil.getHealth(pap.player)) {
                            if (damage < getDamage(pap.player)) continue;
                            if (smart.getValue()) {
                                if (getDamage(pap.player) == forceMin.getValue()) {
                                    if (damage < selfDamage - 2.5) {
                                        continue;
                                    }
                                } else {
                                    if (damage < selfDamage) {
                                        continue;
                                    }
                                }
                            }
                        }
                        displayTarget = pap.player;
                        tempPos = pos.down();
                        tempDamage = damage;
                    }
                }
            }
            CombatUtil.modifyPos = null;
            if (tempPos != null) {
                if (!BlockUtil.canPlace(tempPos, placeRange.getValue())) {
                    tempPos = null;
                    tempDamage = 0;
                }
            }
        }
        if (doCrystal.getValue() && tempPos != null) {
            doPlace(tempPos);
        }
    }

    public boolean canPlaceCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        BlockPos boost2 = boost.up();

        return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN || BlockUtil.canPlace(obsPos, placeRange.getValue()))
                && BlockUtil.getClickSideStrict(obsPos) != null
                && noEntityBlockCrystal(boost, ignoreCrystal, ignoreItem)
                && noEntityBlockCrystal(boost2, ignoreCrystal, ignoreItem)
                && (mc.world.isAir(boost) || hasCrystal(boost) && getBlock(boost) == Blocks.FIRE)
                && (!ClientSetting.INSTANCE.lowVersion.getValue() || mc.world.isAir(boost2));
    }

    private boolean liteCheck(Vec3d from, Vec3d to) {
        return !canSee(from, to) && !canSee(from, to.add(0, 1.8, 0));
    }

    private boolean noEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : BlockUtil.getEntities(new Box(pos))) {
            if (!entity.isAlive() || ignoreItem && entity instanceof ItemEntity || entity instanceof ArmorStandEntity && AntiCheat.INSTANCE.obsMode.getValue())
                continue;
            if (entity instanceof EndCrystalEntity) {
                if (!ignoreCrystal) return false;
                if (mc.player.canSee(entity) || mc.player.getEyePos().distanceTo(entity.getPos()) <= wallRange.getValue()) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    private boolean behindWall(BlockPos pos) {
        Vec3d testVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 2 * 0.85, pos.getZ() + 0.5);
        HitResult result = mc.world.raycast(new RaycastContext(EntityUtil.getEyesPos(), testVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        if (result == null || result.getType() == HitResult.Type.MISS) return false;
        return mc.player.getEyePos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > wallRange.getValue();
    }

    private boolean canTouch(BlockPos pos) {
        Direction side = BlockUtil.getClickSideStrict(pos);
        return side != null && pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5)).distanceTo(mc.player.getEyePos()) <= range.getValue();
    }

    public float calculateDamage(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
        return calculateDamage(pos.down(), new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), player, predict);
    }

    public float calculateDamage(BlockPos obs, Vec3d pos, PlayerEntity player, PlayerEntity predict) {
        CombatUtil.modifyPos = obs;
        CombatUtil.modifyBlockState = Blocks.OBSIDIAN.getDefaultState();
        if (terrainIgnore.getValue()) {
            CombatUtil.terrainIgnore = true;
        }
        float damage = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6);
        CombatUtil.modifyPos = null;
        CombatUtil.terrainIgnore = false;
        return damage;
    }

    private double getDamage(PlayerEntity target) {
        if (forcePlace.getValue() && EntityUtil.getHealth(target) <= forceMaxHealth.getValue() && !PacketMine.INSTANCE.obsidian.isPressed() && !PistonCrystal.INSTANCE.isOn()) {
            return forceMin.getValue();
        }
        if (armorBreaker.getValue()) {
            DefaultedList<ItemStack> armors = target.getInventory().armor;
            for (ItemStack armor : armors) {
                if (armor.isEmpty()) continue;
                if (EntityUtil.getDamagePercent(armor) > maxDurable.getValue()) continue;
                return armorBreakerDamage.getValue();
            }
        }
        if (PistonCrystal.INSTANCE.isOn()) {
            return autoMinDamage.getValueFloat();
        }
        return minDamage.getValue();
    }


    private void doPlace(BlockPos pos) {
        if (!placeTimer.passedMs((long) placeDelay.getValue())) return;
        if (detectMining.getValue() && Alien.BREAK.isMining(pos)) return;
        int block = getBlock();
        if (block == -1) return;
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) return;
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        if (!BlockUtil.canPlace(pos, placeRange.getValue())) return;
        if (rotate.getValue()) {
            if (!faceVector(directionVec)) return;
        }
        int old = mc.player.getInventory().selectedSlot;
        doSwap(block);
        if (BlockUtil.airPlace()) {
            BlockUtil.placedPos.add(pos);
            BlockUtil.clickBlock(pos, Direction.DOWN, false, Hand.MAIN_HAND);
        } else {
            BlockUtil.placedPos.add(pos);
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), false, Hand.MAIN_HAND);
        }
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
        if (rotate.getValue() && !yawStep.getValue() && AntiCheat.INSTANCE.snapBack.getValue()) {
            Alien.ROTATION.snapBack();
        }
        placeTimer.reset();
    }
    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }
    public static Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }
    private int getBlock() {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        } else {
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
    }

    private enum Page {
        General, Interact, Misc, Rotate, Calc
    }

    private class PlayerAndPredict {
        final PlayerEntity player;
        final PlayerEntity predict;

        private PlayerAndPredict(PlayerEntity player) {
            this.player = player;
            if (predictTicks.getValueFloat() > 0) {
                predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
                    @Override
                    public boolean isSpectator() {
                        return false;
                    }

                    @Override
                    public boolean isCreative() {
                        return false;
                    }

                    @Override
                    public boolean isOnGround() {
                        return player.isOnGround();
                    }
                };
                predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, predictTicks.getValueInt(), true)));
                predict.setHealth(player.getHealth());
                predict.prevX = player.prevX;
                predict.prevZ = player.prevZ;
                predict.prevY = player.prevY;
                predict.setOnGround(player.isOnGround());
                predict.getInventory().clone(player.getInventory());
                predict.setPose(player.getPose());
                for (StatusEffectInstance se : new ArrayList<>(player.getStatusEffects())) {
                    predict.addStatusEffect(se);
                }
            } else {
                predict = player;
            }
        }
    }
}