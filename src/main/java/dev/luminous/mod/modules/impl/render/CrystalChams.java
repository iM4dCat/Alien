package dev.luminous.mod.modules.impl.render;

import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.mod.modules.Module;

import java.awt.*;

public class CrystalChams extends Module {
    public static CrystalChams INSTANCE;
    public CrystalChams() {
        super("CrystalChams", Category.Render);
        setChinese("末影水晶模型");
        INSTANCE = this;
    }

    public final ColorSetting core =
            add(new ColorSetting("Core", new Color(255, 255, 255, 255)).injectBoolean(true));
    public final ColorSetting outerFrame =
            add(new ColorSetting("OuterFrame", new Color(255, 255, 255, 255)).injectBoolean(true));
    public final ColorSetting innerFrame =
            add(new ColorSetting("InnerFrame", new Color(255, 255, 255, 255)).injectBoolean(true));
    public final BooleanSetting texture = add(new BooleanSetting("Texture", true));
    public final SliderSetting scale = add(new SliderSetting("Scale", 1, 0, 3f, 0.01));
    public final SliderSetting spinValue = add(new SliderSetting("SpinSpeed", 1f, 0, 3f, 0.01));
    public final SliderSetting bounceHeight = add(new SliderSetting("BounceHeight", 1, 0, 3f, 0.01));
    public final SliderSetting floatValue = add(new SliderSetting("BounceSpeed", 1f, 0, 3f, 0.01));
    public final SliderSetting floatOffset = add(new SliderSetting("YOffset", 0f, -1, 1f, 0.01));
}
