package dev.luminous.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.EntitySpawnEvent;
import dev.luminous.api.utils.render.Render3DUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PearlPredict extends Module {
    public PearlPredict() {
        super("PearlPredict", Category.Render);
        setChinese("珍珠预测");
    }

    public Map<EnderPearlEntity, FakeEntity> map = new ConcurrentHashMap<>();
    @EventHandler
    public void onReceivePacket(EntitySpawnEvent event) {
        if (nullCheck()) return;
        if (event.getEntity() instanceof EnderPearlEntity pearl) {
            mc.world.getPlayers().stream().min(Comparator.comparingDouble((p) -> p.getPos().distanceTo(new Vec3d(pearl.getX(), pearl.getY(), pearl.getZ())))).ifPresent((player) -> {
                map.put(pearl, new FakeEntity(player.getPos(), player.getYaw(), player.getPitch(), player.getVelocity()));
            });
        }
    }

    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 255)));

    static MatrixStack matrixStack;

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        PearlPredict.matrixStack = matrixStack;
        if (nullCheck()) return;
        RenderSystem.disableDepthTest();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EnderPearlEntity pearl) {
                FakeEntity fakeentity = map.get(pearl);
                if (fakeentity != null) {
                    calcTrajectory(fakeentity.yaw, fakeentity.pitch, fakeentity.pos, fakeentity.velocity);
                }
            } else if (entity instanceof PlayerEntity ent && ent != mc.player) {
                if (ent.getMainHandStack().getItem() == Items.ENDER_PEARL || ent.getOffHandStack().getItem() == Items.ENDER_PEARL) {
                    double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta();
                    double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta();
                    double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta();
                    calcTrajectory(ent.getYaw(mc.getTickDelta()), ent.getPitch(mc.getTickDelta()), new Vec3d(x,y,z), ent.getVelocity());
                }
            }
        }
        RenderSystem.enableDepthTest();
    }

    private void calcTrajectory(float yaw, float pitch, Vec3d vec3d, Vec3d velocity) {
        double x = vec3d.x;
        double y = vec3d.y;
        double z = vec3d.z;
        y = y + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;

        //x = x - MathHelper.cos(yaw / 180.0f * 3.1415927f) * 0.16f;
        //z = z - MathHelper.sin(yaw / 180.0f * 3.1415927f) * 0.16f;

        final float maxDist = .4f;
        double motionX = -MathHelper.sin(yaw / 180.0f * 3.1415927f) * MathHelper.cos(pitch / 180.0f * 3.1415927f) * maxDist;
        double motionY = -MathHelper.sin(pitch / 180.0f * 3.141593f) * maxDist;
        double motionZ = MathHelper.cos(yaw / 180.0f * 3.1415927f) * MathHelper.cos(pitch / 180.0f * 3.1415927f) * maxDist;
        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;

        final float pow = 1.5f;

        motionX *= pow;
        motionY *= pow;
        motionZ *= pow;

        //motionX += velocity.x;
        motionY += velocity.y;
        //motionZ += velocity.z;


        Vec3d lastPos;
        for (int i = 0; i < 300; i++) {
            lastPos = new Vec3d(x, y, z);
            x += motionX;
            y += motionY;
            z += motionZ;
            if (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }
            motionY -= 0.03f;


            Vec3d pos = new Vec3d(x, y, z);

            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof ArrowEntity || ent.equals(mc.player)) continue;
                if (ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.3))) {
                    Render3DUtil.drawBox(matrixStack, ent.getBoundingBox(), color.getValue());
                    break;
                }
            }

            BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK) {
                Render3DUtil.drawBox(matrixStack, new Box(bhr.getBlockPos()), color.getValue());
                break;
            }

            if (y <= -65) break;
            if (motionX == 0 && motionY == 0 && motionZ == 0) continue;

            Render3DUtil.drawLine(lastPos, pos, color.getValue());
        }
    }
    
    private record FakeEntity(Vec3d pos, float yaw, float pitch, Vec3d velocity) {
        
    }
}