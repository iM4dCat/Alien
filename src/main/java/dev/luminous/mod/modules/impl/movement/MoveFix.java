package dev.luminous.mod.modules.impl.movement;

import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.JumpEvent;
import dev.luminous.api.events.impl.KeyboardInputEvent;
import dev.luminous.api.events.impl.TravelEvent;
import dev.luminous.api.events.impl.UpdateVelocityEvent;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.BaritoneModule;
import dev.luminous.mod.modules.impl.player.Freecam;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


public class MoveFix extends Module {
    public static MoveFix INSTANCE;
    public MoveFix() {
        super("MoveFix", Category.Movement);
        INSTANCE = this;
        setChinese("移动修复");
    }

    public EnumSetting<UpdateMode> updateMode = add(new EnumSetting<>("UpdateMode", UpdateMode.UpdateMouse));
    public final BooleanSetting grim =
            add(new BooleanSetting("Grim", true)).setParent();
    private final BooleanSetting travel =
            add(new BooleanSetting("Travel", false, grim::isOpen));

    public enum UpdateMode {
        MovementPacket,
        UpdateMouse,
        All
    }
    public static float fixRotation;
    public static float fixPitch;
    private float prevYaw;
    private float prevPitch;

    @EventHandler
    public void travel(TravelEvent e) {
        if (BaritoneModule.isActive()) return;
        if (!grim.getValue() || !travel.getValue()) return;
        if (mc.player.isRiding())
            return;

        if (e.isPre()) {
            prevYaw = mc.player.getYaw();
            prevPitch = mc.player.getPitch();
            mc.player.setYaw(fixRotation);
            mc.player.setPitch(fixPitch);
        } else {
            mc.player.setYaw(prevYaw);
            mc.player.setPitch(prevPitch);
        }
    }

    @EventHandler
    public void onJump(JumpEvent e) {
        if (BaritoneModule.isActive()) return;
        if (!grim.getValue()) return;
        if (mc.player.isRiding())
            return;

        if (e.isPre()) {
            prevYaw = mc.player.getYaw();
            prevPitch = mc.player.getPitch();
            mc.player.setYaw(fixRotation);
            mc.player.setPitch(fixPitch);
        } else {
            mc.player.setYaw(prevYaw);
            mc.player.setPitch(prevPitch);
        }
    }

    @EventHandler
    public void onPlayerMove(UpdateVelocityEvent event) {
        if (BaritoneModule.isActive()) return;
        if (!grim.getValue() || travel.getValue()) return;
        if (mc.player.isRiding())
            return;
        event.cancel();
        event.setVelocity(movementInputToVelocity(event.getMovementInput(), event.getSpeed(), fixRotation));
    }

    @EventHandler(priority = -999)
    public void onKeyInput(KeyboardInputEvent e) {
        if (BaritoneModule.isActive()) return;
        if (!grim.getValue()) return;
        if (HoleSnap.INSTANCE.isOn()) return;
        if (mc.player.isRiding() || Freecam.INSTANCE.isOn())
            return;

        float mF = mc.player.input.movementForward;
        float mS = mc.player.input.movementSideways;
        float delta = (mc.player.getYaw() - fixRotation) * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(delta);
        float sin = MathHelper.sin(delta);
        mc.player.input.movementSideways = Math.round(mS * cos - mF * sin);
        mc.player.input.movementForward = Math.round(mF * cos + mS * sin);
    }

    private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        } else {
            Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
            float f = MathHelper.sin(yaw * 0.017453292F);
            float g = MathHelper.cos(yaw * 0.017453292F);
            return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
        }
    }

}