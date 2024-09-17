package dev.luminous.api.utils.combat;

import dev.luminous.api.utils.Wrapper;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.Alien;
import dev.luminous.mod.modules.impl.client.AntiCheat;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class CombatUtil implements Wrapper {
    public static boolean terrainIgnore = false;
    public static BlockPos modifyPos;
    public static BlockState modifyBlockState = Blocks.AIR.getDefaultState();
    public static final Timer breakTimer = new Timer();
    public static List<PlayerEntity> getEnemies(double range) {
        List<PlayerEntity> list = new ArrayList<>();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!isValid(player, range)) continue;
            list.add(player);
        }
        return list;
    }

    public static void attackCrystal(BlockPos pos, boolean rotate, boolean eatingPause) {
        for (EndCrystalEntity entity : BlockUtil.getEndCrystals(new Box(pos))) {
            attackCrystal(entity, rotate, eatingPause);
            break;
        }
    }

    public static void attackCrystal(Box box, boolean rotate, boolean eatingPause) {
        for (EndCrystalEntity entity : BlockUtil.getEndCrystals(box)) {
            attackCrystal(entity, rotate, eatingPause);
            break;
        }
    }

    public static void attackCrystal(Entity crystal, boolean rotate, boolean usingPause) {
        if (!CombatUtil.breakTimer.passedMs((long) (AntiCheat.INSTANCE.attackDelay.getValue() * 1000))) return;
        if (usingPause && mc.player.isUsingItem())
            return;
        if (crystal != null) {
            CombatUtil.breakTimer.reset();
            if (rotate && AntiCheat.INSTANCE.attackRotate.getValue()) Alien.ROTATION.lookAt(new Vec3d(crystal.getX(), crystal.getY() + 0.25, crystal.getZ()));
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
            mc.player.resetLastAttackedTicks();
            EntityUtil.swingHand(Hand.MAIN_HAND, AntiCheat.INSTANCE.swingMode.getValue());
            if (rotate && AntiCheat.INSTANCE.snapBack.getValue()) {
                Alien.ROTATION.snapBack();
            }
        }
    }
    public static boolean isValid(Entity entity, double range) {
        boolean invalid = entity == null || !entity.isAlive() || entity.equals(mc.player) || entity instanceof PlayerEntity player && Alien.FRIEND.isFriend(player) || mc.player.getPos().distanceTo(entity.getPos()) > range;

        return !invalid;
    }

    public static PlayerEntity getClosestEnemy(double distance) {
        PlayerEntity closest = null;

        for (PlayerEntity player : getEnemies(distance)) {
            if (closest == null) {
                closest = player;
                continue;
            }

            if (!(mc.player.squaredDistanceTo(player.getPos()) < mc.player.squaredDistanceTo(closest))) continue;

            closest = player;
        }
        return closest;
    }
    public static Vec3d getEntityPosVec(PlayerEntity entity, int ticks) {
        if (ticks <= 0) {
            return entity.getPos();
        }
        return entity.getPos().add(getMotionVec(entity, ticks, true));
    }

    public static Vec3d getMotionVec(Entity entity, float ticks, boolean collision) {
        double dX = entity.getX() - entity.prevX;
        double dZ = entity.getZ() - entity.prevZ;
        double entityMotionPosX = 0;
        double entityMotionPosZ = 0;
        if (collision) {
            for (double i = 1; i <= ticks; i = i + 0.5) {
                if (!mc.world.canCollide(entity, entity.getBoundingBox().offset(new Vec3d(dX * i, 0, dZ * i)))) {
                    entityMotionPosX = dX * i;
                    entityMotionPosZ = dZ * i;
                } else {
                    break;
                }
            }
        } else {
            entityMotionPosX = dX * ticks;
            entityMotionPosZ = dZ * ticks;
        }

        return new Vec3d(entityMotionPosX, 0, entityMotionPosZ);
    }
}
