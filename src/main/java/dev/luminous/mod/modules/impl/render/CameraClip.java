package dev.luminous.mod.modules.impl.render;

import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.api.utils.math.FadeUtils;
import dev.luminous.mod.modules.Module;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;

public class CameraClip extends Module {
    public static CameraClip INSTANCE;
    public CameraClip() {
        super("CameraClip", Category.Render);
        setChinese("摄像机穿墙");
        INSTANCE = this;
    }

    public final SliderSetting distance = add(new SliderSetting("Distance", 4f, 1f, 20f));
    public final SliderSetting animateTime = add(new SliderSetting("AnimationTime", 200, 0, 1000));
    private final BooleanSetting noFront = add(new BooleanSetting("NoFront", false));
    private final FadeUtils animation = new FadeUtils(300);
    boolean first = false;
    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT && noFront.getValue())
            mc.options.setPerspective(Perspective.FIRST_PERSON);
        animation.setLength(animateTime.getValueInt());
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            if (!first) {
                first = true;
                animation.reset();
            }
        } else {
            if (first) {
                first = false;
                animation.reset();
            }
        }
    }

    public double getDistance() {
        double quad = mc.options.getPerspective() == Perspective.FIRST_PERSON ? 1 - animation.easeOutQuad() : animation.easeOutQuad();
        return 1d + ((distance.getValue() - 1d) * quad);
    }
}
