package dev.luminous.api.utils.math;

import dev.luminous.Alien;
import dev.luminous.api.utils.Wrapper;

public class AnimateUtil implements Wrapper {
    public static double animate(double current, double endPoint, double speed) {
        if (speed >= 1) return endPoint;
        if (speed == 0) return current;
        return thunder(current, endPoint, speed);
    }

    public static double animate(double current, double endPoint, double speed, AnimMode mode) {
        switch (mode) {
            case Mio -> {
                return mio(current, endPoint, speed);
            }
            case Thunder -> {
                return thunder(current, endPoint, speed);
            }
            case My -> {
                return my(current, endPoint, speed);
            }
            case Old-> {
                return old(current, endPoint, speed);
            }
            case Normal -> {
                return normal(current, endPoint, speed);
            }
        }
        return thunder(current, endPoint, speed);
    }
    public static double mio(double current, double endPoint, double speed) {
        if (Math.max(endPoint, current) - Math.min(endPoint, current) < 0.001) {
            return endPoint;
        }

        int negative = speed < 0 ? -1 : 1;
        if (negative == -1) {
            speed *= -1;
        }

        double diff = (endPoint - current);
        double factor = diff * mc.getTickDelta() / (1 / speed * (Math.min(240D, Alien.FPS.getFps()) / 240D));
        if (diff < 0 && factor < diff) {
            factor = diff;
        } else if (diff > 0 && factor >= diff) {
            factor = diff;
        }
        return current + factor * negative;
    }

    public static double old(double current, double endPoint, double speed) {
        if (Math.max(endPoint, current) - Math.min(endPoint, current) < 0.001) {
            return endPoint;
        }

        int negative = speed < 0 ? -1 : 1;
        if (negative == -1) {
            speed *= -1;
        }

        double diff = (endPoint - current);
        double factor = diff * speed;
        if (diff < 0 && factor < diff) {
            factor = diff;
        } else if (diff > 0 && factor >= diff) {
            factor = diff;
        }
        return current + factor * negative;
    }
    public static double my(double current, double endPoint, double speed) {
        if (Math.max(endPoint, current) - Math.min(endPoint, current) < 0.001) {
            return endPoint;
        }

        int negative = speed < 0 ? -1 : 1;
        if (negative == -1) {
            speed *= -1;
        }

        double diff = (endPoint - current);
        double factor = diff * mc.getTickDelta() * speed;
        if (diff < 0 && factor < diff) {
            factor = diff;
        } else if (diff > 0 && factor >= diff) {
            factor = diff;
        }
        return current + factor * negative;
    }

    public static double thunder(double current, double endPoint, double speed) {
        boolean shouldContinueAnimation = endPoint > current;

        double dif = Math.max(endPoint, current) - Math.min(endPoint, current);
        if (Math.abs(dif) <= 0.001) return endPoint;
        double factor = dif * speed;
        return current + (shouldContinueAnimation ? factor : -factor);
    }

    public static double normal(double current, double endPoint, double speed) {
        boolean shouldContinueAnimation = endPoint > current;
        speed = speed * 10;
        if (Math.abs(Math.max(endPoint, current) - Math.min(endPoint, current)) <= speed) return endPoint;
        return current + (shouldContinueAnimation ? speed : -speed);
    }

    public enum AnimMode {
        Thunder,
        Mio,
        My,
        Old,
        Normal
    }
}
