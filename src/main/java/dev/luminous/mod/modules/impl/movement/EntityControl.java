package dev.luminous.mod.modules.impl.movement;

import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.BoatMoveEvent;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.api.events.impl.UpdateWalkingPlayerEvent;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.asm.accessors.IVec3d;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.ClickGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.util.math.Vec3d;

public class EntityControl extends Module {
    public static EntityControl INSTANCE;

    public EntityControl() {
        super("EntityControl", Category.Movement);
        setChinese("骑行控制");
        INSTANCE = this;
    }
    public final BooleanSetting yaw = add(new BooleanSetting("Yaw", true));
    public final BooleanSetting speedBoolean = add(new BooleanSetting("Speed", true));

    public final SliderSetting speed = add(new SliderSetting("HSpeed", 5.0, 0.1, 50.0));
    public final BooleanSetting fly = add(new BooleanSetting("Fly", true));
    private final SliderSetting verticalSpeed = add(new SliderSetting("VSpeed", 6.0, 0, 20.0));
    public final SliderSetting fallSpeed = add(new SliderSetting("FallSpeed", 0.1, 0, 50.0));
    private final BooleanSetting noSync = add(new BooleanSetting("NoSync", false));

    @EventHandler
    public void onBoat(BoatMoveEvent event) {
        if (nullCheck()) return;
        Entity boat = event.getBoat();
        if (boat == null) return;
        if (boat.getControllingPassenger() != mc.player) return;

        if (yaw.getValue()) boat.setYaw(mc.player.getYaw());

        // Horizontal movement
        Vec3d vel = MovementUtil.getHorizontalVelocity(speed.getValue());
        double velX = vel.getX();
        double velY;
        double velZ = vel.getZ();

        if (mc.currentScreen instanceof ChatScreen || mc.currentScreen != null && ClickGui.INSTANCE.isOff()) {
            velY = -fallSpeed.getValue() / 20;
        } else {
            boolean sprint = InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sprintKey.getBoundKeyTranslationKey()).getCode());
            boolean jump = InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.jumpKey.getBoundKeyTranslationKey()).getCode());
            // Vertical movement
            if (jump) {
                if (sprint) {
                    velY = -fallSpeed.getValue() / 20;
                } else {
                    velY = verticalSpeed.getValue() / 20;
                }
            } else if (sprint) {
                velY = -verticalSpeed.getValue() / 20;
            } else {
                velY = -fallSpeed.getValue() / 20;
            }
        }

        // Apply velocity
        if (speedBoolean.getValue()) ((IVec3d) boat.getVelocity()).setX(velX);
        if (fly.getValue()) ((IVec3d) boat.getVelocity()).setY(velY);
        if (speedBoolean.getValue()) ((IVec3d) boat.getVelocity()).setZ(velZ);
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        if (nullCheck()) return;
        Entity boat = mc.player.getVehicle();
        if (boat == null) return;
        //if (boat.getControllingPassenger() != mc.player) return;

        if (yaw.getValue()) boat.setYaw(mc.player.getYaw());

        // Horizontal movement
        Vec3d vel = MovementUtil.getHorizontalVelocity(speed.getValue());
        double velX = vel.getX();
        double velY;
        double velZ = vel.getZ();

        if (mc.currentScreen instanceof ChatScreen || mc.currentScreen != null && ClickGui.INSTANCE.isOff()) {
            velY = -fallSpeed.getValue() / 20;
        } else {
            boolean sprint = InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sprintKey.getBoundKeyTranslationKey()).getCode());
            boolean jump = InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.jumpKey.getBoundKeyTranslationKey()).getCode());
            // Vertical movement
            if (jump) {
                if (sprint) {
                    velY = -fallSpeed.getValue() / 20;
                } else {
                    velY = verticalSpeed.getValue() / 20;
                }
            } else if (sprint) {
                velY = -verticalSpeed.getValue() / 20;
            } else {
                velY = -fallSpeed.getValue() / 20;
            }
        }

        // Apply velocity
        if (speedBoolean.getValue()) ((IVec3d) boat.getVelocity()).setX(velX);
        if (fly.getValue()) ((IVec3d) boat.getVelocity()).setY(velY);
        if (speedBoolean.getValue()) ((IVec3d) boat.getVelocity()).setZ(velZ);
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof VehicleMoveS2CPacket && noSync.getValue()) {
            event.cancel();
        }
    }
}
