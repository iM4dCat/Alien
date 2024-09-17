package dev.luminous.api.utils.math;

import dev.luminous.mod.modules.impl.client.ClickGui;

public class Animation {
    public final FadeUtils fadeUtils = new FadeUtils(0);
    private boolean setup = false;
    public double from = 0;
    public double to = 0;

    public double get(double target) {
        long length = ClickGui.INSTANCE.animationTime.getValueInt();
        if (length == 0) return target;
        Easing ease = ClickGui.INSTANCE.ease.getValue();
/*        if (!setup) {
            setup = true;
            from = target;
            to = target;
            fadeUtils.setLength(length);
            fadeUtils.reset();
            return target;
        }*/
        if (target != to) {
            from = from + (to - from) * fadeUtils.ease(ease);
            to = target;
            fadeUtils.reset();
        }
        fadeUtils.setLength(length);
        return from + (to - from) * fadeUtils.ease(ease);
    }

    public double get(double target, long length, Easing ease) {
/*        if (!setup) {
            setup = true;
            from = target;
            to = target;
            fadeUtils.setLength(length);
            fadeUtils.reset();
            return target;
        }*/
        if (target != to) {
            from = from + (to - from) * fadeUtils.ease(ease);
            to = target;
            fadeUtils.reset();
        }
        fadeUtils.setLength(length);
        return from + (to - from) * fadeUtils.ease(ease);
    }
}
