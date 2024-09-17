package dev.luminous.mod.modules.impl.movement;

import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.KeyboardInputEvent;
import dev.luminous.api.events.impl.MoveEvent;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.SliderSetting;

import static dev.luminous.api.utils.entity.MovementUtil.*;

public class FastSwim extends Module {
    public static FastSwim INSTANCE;

    public FastSwim() {
        super("FastSwim", Category.Movement);
        setChinese("快速游泳");
        INSTANCE = this;
    }
    @EventHandler
    public void onKeyboardInput(KeyboardInputEvent event) {
        if (mc.player.isInFluid()) {
            mc.player.input.sneaking = false;
        }
    }

    public final SliderSetting speed = add(new SliderSetting("Speed", 0.2, 0, 1, 0.01));
    private final SliderSetting sneakDownSpeed = add(new SliderSetting("DownSpeed", 0.2, 0, 1, 0.01));
    private final SliderSetting upSpeed = add(new SliderSetting("UpSpeed", 0.2, 0, 1, 0.01));
    public final SliderSetting downFactor = add(new SliderSetting("DownFactor", 0, 0.0, 1, 0.000001));
    private MoveEvent event;
    @EventHandler
    public void onMove(MoveEvent event) {
        if (nullCheck()) return;
        if (mc.player.isInFluid()) {
            this.event = event;
            if (!(mc.options.sneakKey.isPressed() && mc.player.input.jumping)) {
                if (mc.options.sneakKey.isPressed()) {
                    setY(-sneakDownSpeed.getValue());
                } else if (mc.player.input.jumping) {
                    setY(upSpeed.getValue());
                } else {
                    setY(-downFactor.getValue());
                }
            } else {
                setY(0);
            }
            double[] dir = directionSpeed(speed.getValue());
            setX(dir[0]);
            setZ(dir[1]);
        }
    }

    private void setX(double f) {
        event.setX(f);
        setMotionX(f);
    }

    private void setY(double f) {
        event.setY(f);
        setMotionY(f);
    }

    private void setZ(double f) {
        event.setZ(f);
        setMotionZ(f);
    }
}
