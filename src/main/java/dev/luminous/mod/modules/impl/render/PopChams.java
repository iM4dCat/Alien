package dev.luminous.mod.modules.impl.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.TotemEvent;
import dev.luminous.api.utils.math.MathUtil;
import dev.luminous.api.utils.math.AnimateUtil;
import dev.luminous.mod.modules.Module;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PopChams extends Module {
    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255)));
    private final SliderSetting alphaSpeed = add(new SliderSetting("AlphaSpeed", 0.2, 0, 1, 0.01));

    private final CopyOnWriteArrayList<Person> popList = new CopyOnWriteArrayList<>();
    public static PopChams INSTANCE;

    public PopChams() {
        super("PopChams", Category.Render);
        setChinese("爆图腾上色");
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        popList.forEach(person -> person.update(popList));
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 0, 1);

        popList.forEach(person -> {
            person.modelPlayer.leftPants.visible = false;
            person.modelPlayer.rightPants.visible = false;
            person.modelPlayer.leftSleeve.visible = false;
            person.modelPlayer.rightSleeve.visible = false;
            person.modelPlayer.jacket.visible = false;
            person.modelPlayer.hat.visible = false;
            renderEntity(matrixStack, person.player, person.modelPlayer, person.getAlpha());
        });

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }


    @EventHandler
    private void onTotemPop(TotemEvent e) {
        if (e.getPlayer().equals(mc.player) || mc.world == null) return;

        PlayerEntity entity = new PlayerEntity(mc.world, BlockPos.ORIGIN, e.getPlayer().bodyYaw, new GameProfile(e.getPlayer().getUuid(), e.getPlayer().getName().getString())) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };

        entity.copyPositionAndRotation(e.getPlayer());
        entity.bodyYaw = e.getPlayer().bodyYaw;
        entity.headYaw = e.getPlayer().headYaw;
        entity.handSwingProgress = e.getPlayer().handSwingProgress;
        entity.handSwingTicks = e.getPlayer().handSwingTicks;
        entity.setSneaking(e.getPlayer().isSneaking());
        entity.limbAnimator.setSpeed(e.getPlayer().limbAnimator.getSpeed());
        entity.limbAnimator.pos = e.getPlayer().limbAnimator.getPos();
        popList.add(new Person(entity));
    }

    private void renderEntity(MatrixStack matrices, LivingEntity entity, BipedEntityModel<PlayerEntity> modelBase, int alpha) {
        double x = entity.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = entity.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = entity.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

        matrices.push();
        matrices.translate((float) x, (float) y, (float) z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180 - entity.bodyYaw)));
        prepareScale(matrices);

        modelBase.animateModel((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), mc.getTickDelta());
        modelBase.setAngles((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), entity.age, entity.headYaw - entity.bodyYaw, entity.getPitch());

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        modelBase.render(matrices, buffer, 10, 0, color.getValue().getRed() / 255f, color.getValue().getGreen() / 255f, color.getValue().getBlue() / 255f, alpha / 255f);
        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    private static void prepareScale(MatrixStack matrixStack) {
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(1.6f, 1.8f, 1.6f);
        matrixStack.translate(0.0F, -1.501F, 0.0F);
    }

    private class Person {
        private final PlayerEntity player;
        private final PlayerEntityModel<PlayerEntity> modelPlayer;
        private int alpha;

        public Person(PlayerEntity player) {
            this.player = player;
            modelPlayer = new PlayerEntityModel<>(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
            modelPlayer.getHead().scale(new Vector3f(-0.3f, -0.3f, -0.3f));
            alpha = color.getValue().getAlpha();
        }

        public void update(CopyOnWriteArrayList<Person> arrayList) {
            if (alpha <= 0) {
                arrayList.remove(this);
                player.kill();
                player.remove(Entity.RemovalReason.KILLED);
                player.onRemoved();
                return;
            }
            alpha = (int) (AnimateUtil.animate(alpha, 0, alphaSpeed.getValue()) - 0.2);
        }

        public int getAlpha() {
            return (int) MathUtil.clamp(alpha, 0, 255);
        }
    }
}
