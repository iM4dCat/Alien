package dev.luminous.api.utils.entity;

import dev.luminous.asm.accessors.IVec3d;
import dev.luminous.mod.modules.impl.movement.HoleSnap;
import dev.luminous.api.utils.Wrapper;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class MovementUtil implements Wrapper {
    public static boolean isMoving() {
        return mc.player.input.movementForward != 0.0 || mc.player.input.movementSideways != 0.0 || HoleSnap.INSTANCE.isOn();
    }
    public static boolean isJumping() {
        return mc.player.input.jumping;
    }
    public static double getDistance2D() {
        double xDist = mc.player.getX() - mc.player.prevX;
        double zDist = mc.player.getZ() - mc.player.prevZ;
        return Math.sqrt(xDist * xDist + zDist * zDist);
    }

    public static double getJumpSpeed() {
        double defaultSpeed = 0.0;

        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            //noinspection ConstantConditions
            int amplifier = mc.player.getActiveStatusEffects().get(StatusEffects.JUMP_BOOST).getAmplifier();
            defaultSpeed += (amplifier + 1) * 0.1;
        }

        return defaultSpeed;
    }
    public static float getMoveForward() {
        return mc.player.input.movementForward;
    }

    public static float getMoveStrafe() {
        return mc.player.input.movementSideways;
    }

    private static final double diagonal = 1 / Math.sqrt(2);
    private static final Vec3d horizontalVelocity = new Vec3d(0, 0, 0);
    public static Vec3d getHorizontalVelocity(double bps) {
        float yaw = mc.player.getYaw();

        Vec3d forward = Vec3d.fromPolar(0, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);
        double velX = 0;
        double velZ = 0;

        boolean a = false;
        if (mc.player.input.pressingForward) {
            velX += forward.x / 20 * bps;
            velZ += forward.z / 20 * bps;
            a = true;
        }
        if (mc.player.input.pressingBack) {
            velX -= forward.x / 20 * bps;
            velZ -= forward.z / 20 * bps;
            a = true;
        }

        boolean b = false;
        if (mc.player.input.pressingRight) {
            velX += right.x / 20 * bps;
            velZ += right.z / 20 * bps;
            b = true;
        }
        if (mc.player.input.pressingLeft) {
            velX -= right.x / 20 * bps;
            velZ -= right.z / 20 * bps;
            b = true;
        }

        if (a && b) {
            velX *= diagonal;
            velZ *= diagonal;
        }

        ((IVec3d) horizontalVelocity).setX(velX);
        ((IVec3d) horizontalVelocity).setZ(velZ);
        return horizontalVelocity;
    }
    public static double[] directionSpeedKey(double speed) {
        float forward = (mc.options.forwardKey.isPressed() ? 1 : 0) + (mc.options.backKey.isPressed() ? -1 : 0);
        float side = (mc.options.leftKey.isPressed() ? 1 : 0) + (mc.options.rightKey.isPressed() ? -1 : 0);
        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getTickDelta();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[]{posX, posZ};
    }
    public static double[] directionSpeed(double speed) {
        float forward = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getTickDelta();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[]{posX, posZ};
    }
    public static double getMotionX() {
        return mc.player.getVelocity().x;
    }
    public static double getMotionY() {
        return mc.player.getVelocity().y;
    }
    public static double getMotionZ() {
        return mc.player.getVelocity().z;
    }
    public static void setMotionX(double x) {
        ((IVec3d) mc.player.getVelocity()).setX(x);
    }
    public static void setMotionY(double y) {
        ((IVec3d) mc.player.getVelocity()).setY(y);
    }
    public static void setMotionZ(double z) {
        ((IVec3d) mc.player.getVelocity()).setZ(z);
    }

    public static double getSpeed(boolean slowness) {
        double defaultSpeed = 0.2873;
        return getSpeed(slowness, defaultSpeed);
    }

    public static double getSpeed(boolean slowness, double defaultSpeed) {
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = mc.player.getActiveStatusEffects().get(StatusEffects.SPEED)
                    .getAmplifier();

            defaultSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }

        if (slowness && mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            int amplifier = mc.player.getActiveStatusEffects().get(StatusEffects.SLOWNESS)
                    .getAmplifier();

            defaultSpeed /= 1.0 + 0.2 * (amplifier + 1);
        }

        if (mc.player.isSneaking()) defaultSpeed /= 5;
        return defaultSpeed;
    }
}
