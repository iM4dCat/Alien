package dev.luminous.mod.modules.impl.combat;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.LookAtEvent;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.api.events.impl.UpdateWalkingPlayerEvent;
import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.math.*;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.api.utils.render.JelloUtil;
import dev.luminous.api.utils.render.Render3DUtil;
import dev.luminous.asm.accessors.IEntity;
import dev.luminous.asm.accessors.ILivingEntity;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.SwingSide;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class KillAura extends Module {

    public static KillAura INSTANCE;
    public static Entity target;
    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));

    public final SliderSetting range =
            add(new SliderSetting("Range", 6.0f, 0.1f, 7.0f, () -> page.getValue() == Page.General));
    private final EnumSetting<Cooldown> cd = add(new EnumSetting<>("CooldownMode", Cooldown.Delay, () -> page.getValue() == Page.General));
    private final SliderSetting cooldown =
            add(new SliderSetting("Cooldown", 1.1f, 0f, 1.2f, 0.01, () -> page.getValue() == Page.General));
     private final SliderSetting wallRange =
            add(new SliderSetting("WallRange", 6.0f, 0.1f, 7.0f, () -> page.getValue() == Page.General));
    private final BooleanSetting whileEating =
            add(new BooleanSetting("WhileUsing", true, () -> page.getValue() == Page.General));
    private final BooleanSetting weaponOnly =
            add(new BooleanSetting("WeaponOnly", true, () -> page.getValue() == Page.General));
    private final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.All, () -> page.getValue() == Page.General));
    private final BooleanSetting onlyCritical =
            add(new BooleanSetting("OnlyCritical", false, () -> page.getValue() == Page.General));
    private final BooleanSetting onlyTick =
            add(new BooleanSetting("OnlyTick", false, () -> page.getValue() == Page.General));

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
    private final EnumSetting<TargetMode> targetMode =
            add(new EnumSetting<>("Filter", TargetMode.DISTANCE, () -> page.getValue() == Page.Target));
    public final BooleanSetting Players = add(new BooleanSetting("Players", true, () -> page.getValue() == Page.Target).setParent());
    public final BooleanSetting armorLow = add(new BooleanSetting("ArmorLow", true, () -> page.getValue() == Page.Target && Players.isOpen()));
    public final BooleanSetting Mobs = add(new BooleanSetting("Mobs", true, () -> page.getValue() == Page.Target));
    public final BooleanSetting Animals = add(new BooleanSetting("Animals", true, () -> page.getValue() == Page.Target));
    public final BooleanSetting Villagers = add(new BooleanSetting("Villagers", true, () -> page.getValue() == Page.Target));
    public final BooleanSetting Slimes = add(new BooleanSetting("Slimes", true, () -> page.getValue() == Page.Target));

    private final EnumSetting<TargetESP> mode = add(new EnumSetting<>("TargetESP", TargetESP.Box, () -> page.getValue() == Page.Render));
    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 50), () -> page.getValue() == Page.Render));
    private final ColorSetting hitColor = add(new ColorSetting("HitColor", new Color(255, 255, 255, 150), () -> page.getValue() == Page.Render));
    public final SliderSetting animationTime = add(new SliderSetting("AnimationTime", 200, 0, 2000, 1, () -> page.getValue() == Page.Render && mode.is(TargetESP.Box)));
    public final EnumSetting<Easing> ease = add(new EnumSetting<>("Ease", Easing.CubicInOut, () -> page.getValue() == Page.Render && mode.is(TargetESP.Box)));

    private final Animation animation = new Animation();
    public enum TargetESP {
        Box,
        Jello,
        None
    }
    public enum Cooldown {
        Vanilla,
        Delay
    }
    public Vec3d directionVec = null;
    private final Timer tick = new Timer();

    public KillAura() {
        super("KillAura", Category.Combat);
        setChinese("杀戮光环");
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (target != null) {
            doRender(matrixStack, mc.getTickDelta(), target, mode.getValue());
        }
    }
    public void doRender(MatrixStack matrixStack, float partialTicks, Entity entity, TargetESP mode) {
        switch (mode) {
            case Box -> Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))).expand(0, 0.1, 0), ColorUtil.fadeColor(color.getValue(), hitColor.getValue(), animation.get(0, animationTime.getValueInt(), ease.getValue())), false, true);
            case Jello -> JelloUtil.drawJello(matrixStack, entity, color.getValue());
        }
    }

    public static void doRender(MatrixStack matrixStack, float partialTicks, Entity entity, Color color, TargetESP mode) {
        switch (mode) {
            case Box -> Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))).expand(0, 0.1, 0), color, false, true);
            case Jello -> JelloUtil.drawJello(matrixStack, entity, color);
        }
    }

    @Override
    public String getInfo() {
        return target == null ? null : target.getName().getString();
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        if (!onlyTick.getValue())
            onUpdate();
    }
    @Override
    public void onUpdate() {
        if (weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(mc.player)) {
            target = null;
            return;
        }
        target = getTarget();
        if (target == null) {
            return;
        }
        doAura();
    }

    @EventHandler
    public void onRotate(LookAtEvent event) {
        if (target != null && rotate.getValue() && yawStep.getValue()) {
            directionVec = target.getEyePos();
            event.setTarget(directionVec, steps.getValueFloat(), priority.getValueFloat());
        }
    }
    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof HandSwingC2SPacket || packet instanceof PlayerInteractEntityC2SPacket && Criticals.getInteractType((PlayerInteractEntityC2SPacket) packet) == Criticals.InteractType.ATTACK) {
            tick.reset();
        }
    }
    private boolean check() {
        if (onlyCritical.getValue()) {
            if (!(Criticals.INSTANCE.isOn() || mc.player.fallDistance > 0)) {
                return false;
            }
        }
        int at = (int) (tick.getPassedTimeMs() / 50);
        if (cd.getValue() == Cooldown.Vanilla) {
            at = ((ILivingEntity) mc.player).getLastAttackedTicks();
        }
        at = (int) (at * Alien.SERVER.getTPSFactor());
        if (!(Math.max(at / getAttackCooldownProgressPerTick(), 0.0F) >= cooldown.getValue()))
            return false;
        return whileEating.getValue() || !mc.player.isUsingItem();
    }

    public static float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
    }

    private void doAura() {
        if (!check()) {
            return;
        }
        if (rotate.getValue()) {
            if (!faceVector(target.getEyePos())) return;
        }
        animation.to = 1;
        animation.from = 1;
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        mc.player.resetLastAttackedTicks();
        EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
        tick.reset();
    }

    public boolean faceVector(Vec3d directionVec) {
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
    
    private Entity getTarget() {
        Entity target = null;
        double distance = range.getValue();
        double maxHealth = 36.0;
        for (Entity entity : mc.world.getEntities()) {
            if (!isEnemy(entity)) continue;
            if (!mc.player.canSee(entity) && mc.player.distanceTo(entity) > wallRange.getValue()) {
                continue;
            }
            if (!CombatUtil.isValid(entity, range.getValue())) continue;

            if (target == null) {
                target = entity;
                distance = mc.player.distanceTo(entity);
                maxHealth = EntityUtil.getHealth(entity);
            } else {
                if (armorLow.getValue() && entity instanceof PlayerEntity && EntityUtil.isArmorLow((PlayerEntity) entity, 10)) {
                    target = entity;
                    break;
                }
                if (targetMode.getValue() == TargetMode.HEALTH && EntityUtil.getHealth(entity) < maxHealth) {
                    target = entity;
                    maxHealth = EntityUtil.getHealth(entity);
                    continue;
                }
                if (targetMode.getValue() == TargetMode.DISTANCE && mc.player.distanceTo(entity) < distance) {
                    target = entity;
                    distance = mc.player.distanceTo(entity);
                }
            }
        }
        return target;
    }
    private boolean isEnemy(Entity entity) {
        if (entity instanceof SlimeEntity && Slimes.getValue()) return true;
        if (entity instanceof PlayerEntity && Players.getValue()) return true;
        if (entity instanceof VillagerEntity && Villagers.getValue()) return true;
        if (!(entity instanceof VillagerEntity) && entity instanceof MobEntity && Mobs.getValue()) return true;
        return entity instanceof AnimalEntity && Animals.getValue();
    }

    private enum TargetMode {
        DISTANCE,
        HEALTH,
    }

    public enum Page {
        General,
        Rotate,
        Target,
        Render
    }
}