package dev.luminous.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.api.utils.math.Easing;
import dev.luminous.api.utils.math.FadeUtils;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExplosionSpawn extends Module {
    public static ExplosionSpawn INSTANCE;
    public static final CopyOnWriteArrayList<Pos> spawnList = new CopyOnWriteArrayList<>();
    public final ColorSetting color =
            add(new ColorSetting("Color", new Color(0xDEC6D0FF, true)));
    public final SliderSetting size =
            add(new SliderSetting("MaxSize", 0.5, 0.1, 5.0, 0.01));
    public final SliderSetting minSize =
            add(new SliderSetting("MinSize", 0.10, 0.00, 1.00, 0.01));
    public final SliderSetting up =
            add(new SliderSetting("Up", 0.1, 0.00, 1.00, 0.01));
    public final SliderSetting height =
            add(new SliderSetting("Height", 0.5, -1.00, 1.00, 0.01));
    private final BooleanSetting extra =
            add(new BooleanSetting("More", true).setParent());
    private final SliderSetting extraCount = add(new SliderSetting("Counts", 5, 1, 10, () -> extra.isOpen()));
    private final SliderSetting distance = add(new SliderSetting("Distance", 10, 0, 50, () -> extra.isOpen()));
    private final SliderSetting delay = add(new SliderSetting("Delay", 300, 0, 1000));
    private final SliderSetting time = add(new SliderSetting("Time", 500, 0, 5000));
    private final SliderSetting animationTime = add(new SliderSetting("AnimationTime", 500, 0, 5000));
    private final SliderSetting fadeTime = add(new SliderSetting("FadeTime", 200, 0, 5000));
    public ExplosionSpawn() {
        super("ExplosionSpawn", Category.Render);
        setChinese("水晶生成指示");
        INSTANCE = this;
    }
    private final EnumSetting<Easing> ease = add(new EnumSetting<>("Ease", Easing.CubicInOut));
    private final Timer timer = new Timer();

    public void add(BlockPos pos) {
        if (timer.passedMs(delay.getValue())) {
            timer.reset();
            spawnList.add(new Pos(pos.toCenterPos(), animationTime.getValueInt(), time.getValueInt(), fadeTime.getValueInt()));
        }
    }
    @Override
    public void onEnable() {
        spawnList.clear();
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (!spawnList.isEmpty()) {
            RenderSystem.enableBlend();
            boolean canClear = true;
            for (Pos spawnPos : spawnList) {
                if (spawnPos.time.easeOutQuad() >= 1) continue;
                double quad = spawnPos.firstFade.ease(this.ease.getValue());
                Color color = this.color.getValue();
                color = ColorUtil.injectAlpha(color, (int) (color.getAlpha() * Math.abs(1 - spawnPos.fadeTime.easeOutQuad())));
                canClear = false;
                if (extra.getValue()) {
                    for (double i = 0f; i < extraCount.getValue() * 0.001 * distance.getValue(); i += (0.001 * distance.getValue())) {
                        drawCircle(matrixStack, color, (this.size.getValue() * quad + minSize.getValue() - i), new Vec3d(spawnPos.pos.getX(), (spawnPos.pos.getY() + 1 + 1 * quad * up.getValue() + height.getValue() - 1.5), spawnPos.pos.getZ()));
                    }
                } else {
                    drawCircle(matrixStack, color, (this.size.getValue() * quad + minSize.getValue()), new Vec3d(spawnPos.pos.getX(), (spawnPos.pos.getY() + 1 + 1 * quad * up.getValue() + height.getValue() - 1.5), spawnPos.pos.getZ()));
                }
            }
            if (canClear) spawnList.clear();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
        }
    }

    public static void drawCircle(MatrixStack matrixStack, Color color, double circleSize, Vec3d pos) {
        Vec3d camPos = mc.getBlockEntityRenderDispatcher().camera.getPos();
        RenderSystem.disableDepthTest();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION);

        for (double i = 0; i <= 360d + (360d / 80d); i += (360d / 80d)) {
            double x = Math.sin(Math.toRadians(i)) * circleSize;
            double z = Math.cos(Math.toRadians(i)) * circleSize;
            Vec3d tempPos = new Vec3d(pos.x + x, pos.y, pos.z + z).add(-camPos.x, -camPos.y, -camPos.z);
            bufferBuilder.vertex(matrix, (float) tempPos.x, (float) tempPos.y, (float) tempPos.z).next();
        }

        tessellator.draw();
        RenderSystem.enableDepthTest();
    }

    public static class Pos {
        public final Vec3d pos;
        public final FadeUtils firstFade;
        public final FadeUtils time;
        public final FadeUtils fadeTime;
        public Pos(Vec3d pos, int animTime, int time, int fadeTime) {
            this.firstFade = new FadeUtils(animTime);
            this.time = new FadeUtils(time);
            this.fadeTime = new FadeUtils(fadeTime);
            this.pos = pos;
        }
    }
}