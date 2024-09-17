package dev.luminous.api.utils.math;

import dev.luminous.api.utils.Wrapper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil implements Wrapper {
    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }
    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        return Math.min(value, max);
    }
    public static double round(double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    public static double square(double input) {
        return input * input;
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }
    public static double random(double min, double max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static float rad(float angle) {
        return (float) (angle * Math.PI / 180);
    }

    public static double interpolate(double previous, double current, double delta) {
        return previous + (current - previous) * delta;
    }

    public static float interpolate(float previous, float current, float delta) {
        return previous + (current - previous) * delta;
    }

    public static Direction getFacingOrder(float yaw, float pitch) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.sin(f);
        float i = MathHelper.cos(f);
        float j = MathHelper.sin(g);
        float k = MathHelper.cos(g);
        boolean bl = j > 0.0F;
        boolean bl2 = h < 0.0F;
        boolean bl3 = k > 0.0F;
        float l = bl ? j : -j;
        float m = bl2 ? -h : h;
        float n = bl3 ? k : -k;
        float o = l * i;
        float p = n * i;
        Direction direction = bl ? Direction.EAST : Direction.WEST;
        Direction direction2 = bl2 ? Direction.UP : Direction.DOWN;
        Direction direction3 = bl3 ? Direction.SOUTH : Direction.NORTH;
        if (l > n) {
            if (m > o) {
                return direction2;
            } else {
                return direction;
            }
        } else if (m > p) {
            return direction2;
        } else {
            return direction3;
        }
    }
    
    public static Direction getDirectionFromEntityLiving(BlockPos pos, LivingEntity entity) {
        if (Math.abs(entity.getX() - ((double) pos.getX() + 0.5)) < 2.0 && Math.abs(entity.getZ() - ((double)pos.getZ() + 0.5)) < 2.0) {
            double d0 = entity.getY() + (double)entity.getEyeHeight(entity.getPose());
            if (d0 - (double)pos.getY() > 2.0) {
                return Direction.UP;
            }

            if ((double)pos.getY() - d0 > 0.0) {
                return Direction.DOWN;
            }
        }

        return entity.getHorizontalFacing().getOpposite();
    }

}
