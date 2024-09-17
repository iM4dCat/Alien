package dev.luminous.mod.modules.impl.player;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.eventbus.EventPriority;
import dev.luminous.api.events.impl.KeyboardInputEvent;
import dev.luminous.api.events.impl.RotateEvent;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.api.utils.math.MathUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.BaritoneModule;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;

public class Freecam extends Module {
    public static Freecam INSTANCE;
    private final SliderSetting speed = add(new SliderSetting("HSpeed", 1, 0.0, 3));
    private final SliderSetting hspeed = add(new SliderSetting("VSpeed", 0.42, 0.0, 3));
    final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true));
    private float fakeYaw;
    private float fakePitch;
    private float prevFakeYaw;
    private float prevFakePitch;
    private double fakeX;
    private double fakeY;
    private double fakeZ;
    private double prevFakeX;
    private double prevFakeY;
    private double prevFakeZ;

    public Freecam() {
        super("Freecam", Category.Player);
        setChinese("自由相机");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        mc.chunkCullingEnabled = false;

        preYaw = getYaw();
        prePitch = getPitch();

        fakePitch = getPitch();
        fakeYaw = getYaw();

        prevFakePitch = fakePitch;
        prevFakeYaw = fakeYaw;

        fakeX = mc.player.getX();
        fakeY = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
        fakeZ = mc.player.getZ();

        prevFakeX = fakeX;
        prevFakeY = fakeY;
        prevFakeZ = fakeZ;
    }


    @Override
    public void onDisable() {
        mc.chunkCullingEnabled = true;
    }

    @Override
    public void onUpdate() {
        if (rotate.getValue() && mc.crosshairTarget != null && mc.crosshairTarget.getPos() != null) {
            float[] angle = Alien.ROTATION.getRotation(mc.crosshairTarget.getPos());
            preYaw = angle[0];
            prePitch = angle[1];
        }
        if (BaritoneModule.isPathing()) {
            double[] motion = MovementUtil.directionSpeedKey(speed.getValue());

            prevFakeX = fakeX;
            prevFakeY = fakeY;
            prevFakeZ = fakeZ;

            fakeX += motion[0];
            fakeZ += motion[1];

            if (mc.options.jumpKey.isPressed())
                fakeY += hspeed.getValue();

            if (mc.options.sneakKey.isPressed())
                fakeY -= hspeed.getValue();
        }
    }

    private float preYaw;
    private float prePitch;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRotate(RotateEvent event) {
        if (BaritoneModule.isPathing()) return;
        if (event.isModified()) return;
        event.setYawNoModify(preYaw);
        event.setPitchNoModify(prePitch);
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        prevFakeYaw = fakeYaw;
        prevFakePitch = fakePitch;

        fakeYaw = getYaw();
        fakePitch = getPitch();
    }

    private float getYaw() {
        return mc.player.getYaw();
    }

    private float getPitch() {
        return mc.player.getPitch();
    }

    @EventHandler
    public void onKeyboardInput(KeyboardInputEvent event) {
        if (mc.player == null) return;

        double[] motion = MovementUtil.directionSpeedKey(speed.getValue());

        prevFakeX = fakeX;
        prevFakeY = fakeY;
        prevFakeZ = fakeZ;

        fakeX += motion[0];
        fakeZ += motion[1];

        if (mc.options.jumpKey.isPressed())
            fakeY += hspeed.getValue();

        if (mc.options.sneakKey.isPressed())
            fakeY -= hspeed.getValue();

        mc.player.input.movementForward = 0;
        mc.player.input.movementSideways = 0;
        mc.player.input.jumping = false;
        mc.player.input.sneaking = false;
    }

    public float getFakeYaw() {
        return (float) MathUtil.interpolate(prevFakeYaw, fakeYaw, mc.getTickDelta());
    }

    public float getFakePitch() {
        return (float) MathUtil.interpolate(prevFakePitch, fakePitch, mc.getTickDelta());
    }

    public double getFakeX() {
        return MathUtil.interpolate(prevFakeX, fakeX, mc.getTickDelta());
    }

    public double getFakeY() {
        return MathUtil.interpolate(prevFakeY, fakeY, mc.getTickDelta());
    }

    public double getFakeZ() {
        return MathUtil.interpolate(prevFakeZ, fakeZ, mc.getTickDelta());
    }
}
