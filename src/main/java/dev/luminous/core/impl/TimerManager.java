package dev.luminous.core.impl;

import dev.luminous.mod.modules.impl.player.TimerModule;

public class TimerManager {

    public float timer = 1f;

    public void set(float factor) {
        if (factor < 0.1f) factor = 0.1f;
        timer = factor;
    }

    public float lastTimer;
    public void reset() {
        timer = getDefault();
        lastTimer = timer;
    }

    public void tryReset() {
        if (lastTimer != getDefault()) {
            reset();
        }
    }

    public float get() {
        return timer;
    }

    public float getDefault() {
        return TimerModule.INSTANCE.isOn() ? (TimerModule.INSTANCE.boostKey.isPressed() ? TimerModule.INSTANCE.boost.getValueFloat() : TimerModule.INSTANCE.multiplier.getValueFloat()) : 1f;
    }
}

