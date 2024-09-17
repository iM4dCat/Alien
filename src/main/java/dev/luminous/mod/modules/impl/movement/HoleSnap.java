package dev.luminous.mod.modules.impl.movement;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.eventbus.EventPriority;
import dev.luminous.api.events.impl.*;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.Alien;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.player.Freecam;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class HoleSnap extends Module {
    public static HoleSnap INSTANCE;
    public final BooleanSetting any = add(new BooleanSetting("AnyHole", true));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5, 1, 50));
    private final SliderSetting timeoutTicks = this.add(new SliderSetting("TimeOut", 40, 0, 100));
    public final SliderSetting timer = add(new SliderSetting("Timer", 1, 0.1, 8, 0.1));
    public final BooleanSetting up = add(new BooleanSetting("Up", true));
    public final BooleanSetting grim = add(new BooleanSetting("Grim", false));
    private final SliderSetting steps = add(new SliderSetting("Steps", 0.8, 0, 1, 0.01, grim::getValue));
   private final SliderSetting priority = add(new SliderSetting("Priority", 10,0 ,100, grim::getValue));
    public final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
    public final SliderSetting circleSize = add(new SliderSetting("CircleSize", 1f, 0.1f, 2.5f));
    public final BooleanSetting fade = add(new BooleanSetting("Fade", true));
    public final SliderSetting segments = add(new SliderSetting("Segments", 180, 0, 360));
    boolean resetMove = false;
    private BlockPos holePos;
    private int stuckTicks;
    private int enabledTicks;

    public HoleSnap() {
        super("HoleSnap", "HoleSnap", Category.Movement);
        setChinese("拉坑");
        INSTANCE = this;
    }

    boolean applyTimer = false;
    @EventHandler(priority = EventPriority.LOW + 1)
    public void onTimer(TimerEvent event) {
        if (applyTimer) event.set(timer.getValueFloat());
    }

    @Override
    public void onEnable() {
        applyTimer = false;
        if (nullCheck()) {
            disable();
            return;
        }
        resetMove = false;
        holePos = Alien.HOLE.getHole((float) range.getValue(), true, any.getValue(), up.getValue());
    }

    @Override
    public void onDisable() {
        this.holePos = null;
        this.stuckTicks = 0;
        this.enabledTicks = 0;
        if (nullCheck()) {
            return;
        }
        if (resetMove && !grim.getValue()) {
            MovementUtil.setMotionX(0);
            MovementUtil.setMotionZ(0);
        }
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.disable();
        }
    }
    Vec3d targetPos;

    @EventHandler(priority = -999)
    public void onKeyInput(KeyboardInputEvent e) {
        if (!grim.getValue()) {
            return;
        } else if (MoveFix.INSTANCE.isOff() || !MoveFix.INSTANCE.grim.getValue()) {
            CommandManager.sendChatMessage("§4HoleSnap require MovementFix.");
            disable();
            return;
        }
        if (mc.player.isRiding() || Freecam.INSTANCE.isOn())
            return;

        mc.player.input.movementSideways = 0;
        mc.player.input.movementForward = 1;
    }

    @Override
    public void onUpdate() {
        holePos = Alien.HOLE.getHole((float) range.getValue(), true, any.getValue(), up.getValue());
        if (holePos == null) {
            disable();
            return;
        }
        ++enabledTicks;
        if (enabledTicks > timeoutTicks.getValue() - 1) {
            disable();
            return;
        }
        applyTimer = true;
        if (!grim.getValue()) return;
        if (!mc.player.isAlive() || mc.player.isFallFlying()) {
            disable();
            return;
        }
        if (stuckTicks > 8) {
            disable();
            return;
        }
        if (holePos == null) {
            //CommandManager.sendChatMessageWidthId("§e[!] §fHoles?", hashCode());
            disable();
            return;
        }
        Vec3d playerPos = mc.player.getPos();
        targetPos = new Vec3d(holePos.getX() + 0.5, mc.player.getY(), holePos.getZ() + 0.5);
        if (Alien.HOLE.isDoubleHole(holePos)) {
            Direction facing = Alien.HOLE.is3Block(holePos);
            if (facing != null) {
                targetPos = targetPos.add(new Vec3d(facing.getVector().getX() * 0.5, facing.getVector().getY() * 0.5, facing.getVector().getZ() * 0.5));
            }
        }

        applyTimer = true;
        resetMove = true;
        float rotation = getRotationTo(playerPos, targetPos).x;
        float yawRad = rotation / 180.0f * 3.1415927f;
        double dist = playerPos.distanceTo(targetPos);
        double cappedSpeed = Math.min(0.2873, dist);
        double x = -(float) Math.sin(yawRad) * cappedSpeed;
        double z = (float) Math.cos(yawRad) * cappedSpeed;
        if (Math.abs(x) < 0.25 && Math.abs(z) < 0.25 && playerPos.y <= holePos.getY() + 0.8) {
            disable();
            return;
        }
        if (mc.player.horizontalCollision) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
        }
    }

    @EventHandler()
    public void onRotate(LookAtEvent event) {
        if (grim.getValue() && holePos != null) {
            targetPos = new Vec3d(holePos.getX() + 0.5, mc.player.getY(), holePos.getZ() + 0.5);
            if (Alien.HOLE.isDoubleHole(holePos)) {
                Direction facing = Alien.HOLE.is3Block(holePos);
                if (facing != null) {
                    targetPos = targetPos.add(new Vec3d(facing.getVector().getX() * 0.5, facing.getVector().getY() * 0.5, facing.getVector().getZ() * 0.5));
                }
            }

            event.setTarget(targetPos, steps.getValueFloat(), priority.getValueFloat());
        }
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (grim.getValue()) return;
        if (!mc.player.isAlive() || mc.player.isFallFlying()) {
            disable();
            return;
        }
        if (stuckTicks > 8) {
            disable();
            return;
        }
        if (holePos == null) {
            //CommandManager.sendChatMessageWidthId("§e[!] §fHoles?", hashCode());
            disable();
            return;
        }
        Vec3d playerPos = mc.player.getPos();
        targetPos = new Vec3d(holePos.getX() + 0.5, mc.player.getY(), holePos.getZ() + 0.5);
        if (Alien.HOLE.isDoubleHole(holePos)) {
            Direction facing = Alien.HOLE.is3Block(holePos);
            if (facing != null) {
                targetPos = targetPos.add(new Vec3d(facing.getVector().getX() * 0.5, facing.getVector().getY() * 0.5, facing.getVector().getZ() * 0.5));
            }
        }

        applyTimer = true;
        resetMove = true;
        float rotation = getRotationTo(playerPos, targetPos).x;
        float yawRad = rotation / 180.0f * 3.1415927f;
        double dist = playerPos.distanceTo(targetPos);
        double cappedSpeed = Math.min(0.2873, dist);
        double x = -(float) Math.sin(yawRad) * cappedSpeed;
        double z = (float) Math.cos(yawRad) * cappedSpeed;
        event.setX(x);
        event.setZ(z);
        if (Math.abs(x) < 0.1 && Math.abs(z) < 0.1 && playerPos.y <= holePos.getY() + 0.5) {
            disable();
        }
        if (mc.player.horizontalCollision) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
        }
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (targetPos == null || holePos == null) {
            return;
        }
        GL11.glEnable(GL11.GL_BLEND);
        Color color = this.color.getValue();

        Vec3d pos = new Vec3d(targetPos.x, holePos.getY(), targetPos.getZ());
        if (fade.getValue()) {
            double temp = 0.01;
            for (double i = 0; i < circleSize.getValue(); i += temp) {
                drawCircle(matrixStack, ColorUtil.injectAlpha(color, (int) Math.min(color.getAlpha() * 2 / (circleSize.getValue() / temp), 255)), i, pos, segments.getValueInt());
            }
        } else {
            drawCircle(matrixStack, color, circleSize.getValue(), pos, segments.getValueInt());
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
    }
    public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
        Vec3d vec3d = posTo.subtract(posFrom);
        return getRotationFromVec(vec3d);
    }

    public static void drawCircle(MatrixStack matrixStack, Color color, double circleSize, Vec3d pos, int segments) {
        Vec3d camPos = mc.getBlockEntityRenderDispatcher().camera.getPos();
        RenderSystem.disableDepthTest();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);

        for (double i = 0; i < 360; i += ((double) 360 / segments)) {
            double x = Math.sin(Math.toRadians(i)) * circleSize;
            double z = Math.cos(Math.toRadians(i)) * circleSize;
            Vec3d tempPos = new Vec3d(pos.x + x, pos.y, pos.z + z).add(-camPos.x, -camPos.y, -camPos.z);
            bufferBuilder.vertex(matrix, (float) tempPos.x, (float) tempPos.y, (float) tempPos.z).next();
        }

        tessellator.draw();
        RenderSystem.enableDepthTest();
    }

    private static Vec2f getRotationFromVec(Vec3d vec) {
        double d = vec.x;
        double d2 = vec.z;
        double xz = Math.hypot(d, d2);
        d2 = vec.z;
        double d3 = vec.x;
        double yaw = normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
        double pitch = normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
        return new Vec2f((float) yaw, (float) pitch);
    }

    private static double normalizeAngle(double angleIn) {
        double angle = angleIn;
        if ((angle %= 360.0) >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return angle;
    }
}