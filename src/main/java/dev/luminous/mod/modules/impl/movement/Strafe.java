package dev.luminous.mod.modules.impl.movement;

import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.MoveEvent;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.Alien;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.BaritoneModule;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Objects;

import static dev.luminous.api.utils.entity.MovementUtil.directionSpeed;

public class Strafe
        extends Module {
    public static Strafe INSTANCE;
    private final BooleanSetting airStop =
            add(new BooleanSetting("AirStop", true));
    private final BooleanSetting slowCheck =
            add(new BooleanSetting("SlowCheck", true));

    public Strafe() {
        super("Strafe", "Modifies sprinting", Category.Movement);
        setChinese("灵活移动");
        INSTANCE = this;
    }

    @EventHandler
    public void onStrafe(MoveEvent event) {
        if (BaritoneModule.isActive()) return;
        if (mc.player.isSneaking() || HoleSnap.INSTANCE.isOn() || Speed.INSTANCE.isOn() || mc.player.isFallFlying() || Alien.PLAYER.insideBlock || mc.player.isInLava() || mc.player.isTouchingWater() || mc.player.getAbilities().flying)
            return;
        if (!MovementUtil.isMoving()) {
            if (airStop.getValue()) {
                MovementUtil.setMotionX(0);
                MovementUtil.setMotionZ(0);
            }
            return;
        }
        double[] dir = directionSpeed(getBaseMoveSpeed());
        event.setX(dir[0]);
        event.setZ(dir[1]);
    }

    public double getBaseMoveSpeed() {
        double n = 0.2873;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED) && (!this.slowCheck.getValue() || !mc.player.hasStatusEffect(StatusEffects.SLOWNESS))) {
            n *= 1.0 + 0.2 * (double) (Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.SPEED)).getAmplifier() + 1);
        }
        return n;
    }
}