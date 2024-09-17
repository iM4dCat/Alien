package dev.luminous.mod.modules.impl.movement;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.ICustomGoalProcess;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.BaritoneModule;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import net.minecraft.util.math.Direction;

public class AutoWalk extends Module {
    public AutoWalk() {
        super("AutoWalk", Category.Movement);
        setChinese("自动前进");
        INSTANCE = this;
    }
    public enum Mode {
        Forward,
        Path
    }

    EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Forward));
    public static AutoWalk INSTANCE;
    boolean start = false;

    @Override
    public void onEnable() {
        start = false;
    }

    @Override
    public void onLogout() {
        disable();
    }

    @Override
    public void onUpdate() {
        if (mode.is(Mode.Forward)) {
            mc.options.forwardKey.setPressed(true);
        } else if (mode.is(Mode.Path)) {
            if (!start) {
                Direction direction = mc.player.getHorizontalFacing();
                var x = mc.player.getBlockX() + direction.getVector().getX() * 30000000;
                var z = mc.player.getBlockZ() + direction.getVector().getZ() * 30000000;

                BaritoneModule.cancelEverything();
                IBaritone primary = BaritoneAPI.getProvider().getPrimaryBaritone();
                if (primary != null) {
                    ICustomGoalProcess customGoalProcess = primary.getCustomGoalProcess();
                    if (customGoalProcess != null) {
                        customGoalProcess.setGoalAndPath(new GoalXZ(x, (int) z));
                    }
                }
                start = true;
            } else if (!BaritoneModule.isActive()) {
                disable();
            }
        }
    }

    @Override
    public void onDisable() {
        BaritoneModule.cancelEverything();
    }


    public boolean forward() {
        return isOn() && mode.is(Mode.Forward);
    }
}
