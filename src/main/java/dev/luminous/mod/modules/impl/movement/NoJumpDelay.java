package dev.luminous.mod.modules.impl.movement;

import dev.luminous.mod.modules.Module;

public final class NoJumpDelay
        extends Module {
    public static NoJumpDelay INSTANCE;
    public NoJumpDelay() {
        super("NoJumpDelay", Category.Movement);
        setChinese("无跳跃冷却");
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        mc.player.jumpingCooldown = 0;
    }
}
