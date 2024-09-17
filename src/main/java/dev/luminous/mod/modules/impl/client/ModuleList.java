package dev.luminous.mod.modules.impl.client;

import dev.luminous.Alien;
import dev.luminous.api.utils.math.AnimateUtil;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.api.utils.render.Render2DUtil;
import dev.luminous.mod.gui.font.FontRenderers;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleList extends Module {
    public ModuleList() {
        super("ModuleList", Category.Client);
        setChinese("功能列表");
        INSTANCE = this;
    }

    public static ModuleList INSTANCE;
    private final BooleanSetting font = add(new BooleanSetting("Font", true));
    private final SliderSetting height = add(new SliderSetting("Height", 0, -2, 10));
    private final SliderSetting textOffset = add(new SliderSetting("TextOffset", 2, -5, 10));
    private final SliderSetting xOffset = add(new SliderSetting("XOffset", 0, 0, 500));
    private final SliderSetting yOffset = add(new SliderSetting("YOffset", 25, 0, 300));
    public final EnumSetting<AnimateUtil.AnimMode> animMode = add(new EnumSetting<>("AnimMode", AnimateUtil.AnimMode.Mio));
    public final SliderSetting disableSpeed = add(new SliderSetting("DisableSpeed", 0.09, -0.2, 1, 0.01));
    public final SliderSetting enableSpeed = add(new SliderSetting("EnableSpeed", 0.12, 0.0, 1, 0.01));
    public final SliderSetting ySpeed = add(new SliderSetting("YSpeed", 0.44, 0.01, 1, 0.01));
    public final BooleanSetting lowerCase = add(new BooleanSetting("LowerCase", false));
    private final BooleanSetting forgeHax = add(new BooleanSetting("ForgeHax", false));
    private final BooleanSetting space = add(new BooleanSetting("Space", true));
    private final BooleanSetting down = add(new BooleanSetting("Down", false));
    private final BooleanSetting animY = add(new BooleanSetting("AnimY", false));
    private final BooleanSetting scissor = add(new BooleanSetting("Scissor", false));
    private final BooleanSetting onlyBind = add(new BooleanSetting("OnlyBind", false));
    private final EnumSetting<ColorMode> colorMode = add(new EnumSetting<>("ColorMode", ColorMode.Pulse));
    private final SliderSetting rainbowSpeed = add(new SliderSetting("RainbowSpeed", 4, 1, 10, 0.1, () -> colorMode.getValue() == ColorMode.Rainbow ));
    private final SliderSetting saturation = add(new SliderSetting("Saturation", 130.0f, 1.0f, 255.0f, () -> colorMode.getValue() == ColorMode.Rainbow ));
    private final SliderSetting pulseSpeed = add(new SliderSetting("PulseSpeed", 1, 0, 5, 0.1, () -> colorMode.getValue() == ColorMode.Pulse ));
    private final SliderSetting pulseCounter = add(new SliderSetting("Counter", 10, 1, 50, () -> colorMode.getValue() == ColorMode.Pulse));
    private final SliderSetting rainbowDelay = add(new SliderSetting("Delay", 350, 0, 1000, () -> colorMode.getValue() == ColorMode.Rainbow));
    private final ColorSetting color = add(new ColorSetting("Color", new Color(173, 0, 0, 255), () -> colorMode.getValue() != ColorMode.Rainbow));
    private final ColorSetting endColor = add(new ColorSetting("EndColor", new Color(255, 0, 0, 255), () -> colorMode.getValue() == ColorMode.Pulse));
    private final BooleanSetting rect = add(new BooleanSetting("Rect", true));
    private final BooleanSetting backGround = add(new BooleanSetting("BackGround", true).setParent());
    private final BooleanSetting bgSync = add(new BooleanSetting("Sync", false, backGround::isOpen));
    private final ColorSetting bgColor = add(new ColorSetting("BGColor", new Color(0, 0, 0, 100), backGround::isOpen));
    private final BooleanSetting preY = add(new BooleanSetting("PreY", false));
    private final BooleanSetting fold = add(new BooleanSetting("Fold", false).setParent());
    private final SliderSetting foldSpeed = add(new SliderSetting("FoldSpeed", 0.1, 0.01, 1, 0.01, fold::isOpen));
    private final BooleanSetting fade = add(new BooleanSetting("Fade", true).setParent());
    private final SliderSetting fadeSpeed = add(new SliderSetting("FadeSpeed", 0.05, 0.01, 1, 0.01, fade::isOpen));
    private List<Modules> modulesList = new java.util.ArrayList<>();

    private enum ColorMode {
        Custom,
        Pulse,
        Rainbow
    }

    boolean update;

    @Override
    public void onEnable() {
        modulesList.clear();
        for (Module module : Alien.MODULE.modules) {
            modulesList.add(new Modules(module));
        }
    }

    private boolean lastSpaceToggled;
    private boolean lastLowercaseToggled;
    public int counter = 20;
    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (space.getValue() != lastSpaceToggled || lowerCase.getValue() != lastLowercaseToggled) {
            for (Modules modules : modulesList) {
                modules.updateName();
            }
            lastLowercaseToggled = lowerCase.getValue();
            lastSpaceToggled = space.getValue();
        }
        for (Modules modules : modulesList) {
            modules.update();
        }
        if (update) {
            modulesList = modulesList.stream().sorted(Comparator.comparing(module -> -getStringWidth(module.name))).collect(Collectors.toList());
            update = false;
        }
        int startY = down.getValue() ? mc.getWindow().getScaledHeight() - yOffset.getValueInt() - getFontHeight() : yOffset.getValueInt();
        int lastY = startY;
        for (Modules modules : modulesList) {
            if (modules.module.isOn() && modules.module.drawnSetting.getValue() && !(onlyBind.getValue() && modules.module.getBind().getKey() == -1)) {
                modules.enable();
            } else {
                modules.disable();
            }

            if (modules.isEnabled) {
                if (fade.getValue()) {
                    modules.fade = animate(modules.fade, 1, fadeSpeed.getValue());
                } else {
                    modules.fade = 1;
                }
                modules.fold = 1;
                modules.x = animate(modules.x, getStringWidth(getSuffix(modules.name)), enableSpeed.getValue());
            } else {
                if (fade.getValue()) {
                    modules.fade = animate(modules.fade, 0.08, fadeSpeed.getValue());
                } else {
                    modules.fade = 1;
                }
                modules.fold = animate(modules.fold, -0.1, foldSpeed.getValue());
                modules.x = animate(modules.x, -1, disableSpeed.getValue());
                if (modules.x <= 0 || modules.fade <= 0.084 || fold.getValue() && modules.fold <= 0) {
                    modules.hide = true;
                    continue;
                }
            }
            if (modules.hide) {
                modules.updateName();
                modules.x = 0;
                modules.y = animY.getValue() ? startY : lastY;
                modules.nameUpdated = false;
                modules.hide = false;
            }

            if (modules.nameUpdated) {
                modules.nameUpdated = false;
                modules.y = animY.getValue() && !modules.isEnabled ? startY : lastY;
            } else {
                modules.y = animate(modules.y, animY.getValue() && !modules.isEnabled ? startY : lastY, ySpeed.getValue());
            }
            counter += 1;

            int textX = (int) (mc.getWindow().getScaledWidth() - modules.x - xOffset.getValue() - (rect.getValue() ? 2 : 0));

            if (fold.getValue()) {
                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0D, modules.y * (1 - modules.fold), 0D);
                drawContext.getMatrices().scale(1, (float) modules.fold, 1);
            }
            if (scissor.getValue()) {
                drawContext.enableScissor(0, 0, (mc.getWindow().getScaledWidth() - xOffset.getValueInt() - (rect.getValue() ? 2 : 0)), mc.getWindow().getScaledHeight());
            }
            if (backGround.getValue()) {
                Render2DUtil.drawRect(drawContext.getMatrices(), textX - 1,
                        (int) modules.y,
                        ((float) mc.getWindow().getScaledWidth() - xOffset.getValueInt() + 1) - textX + 1,
                        getFontHeight() + height.getValueInt(),
                        bgSync.getValue() ? ColorUtil.injectAlpha(getColor(counter), (int) (bgColor.getValue().getAlpha() * modules.fade)) : ColorUtil.injectAlpha(bgColor.getValue().getRGB(), (int) (bgColor.getValue().getAlpha() * modules.fade)));
            }
            if (font.getValue()) {
                FontRenderers.ui.drawString(drawContext.getMatrices() ,getSuffix(modules.name), textX, (int) (modules.y + 1 + textOffset.getValueInt()), ColorUtil.injectAlpha(getColor(counter), (int) (255 * modules.fade)));
            } else {
                drawContext.drawTextWithShadow(mc.textRenderer, getSuffix(modules.name), textX, (int) (modules.y + 1 + textOffset.getValueInt()), ColorUtil.injectAlpha(getColor(counter), (int) (255 * modules.fade)));
            }
            if (scissor.getValue()) drawContext.disableScissor();
            if (fold.getValue()) {
                drawContext.getMatrices().pop();
            }
            if (rect.getValue()) {
                Render2DUtil.drawRect(drawContext.getMatrices(), (float) mc.getWindow().getScaledWidth() - xOffset.getValueInt() - 1,
                        (int) modules.y,
                        1,
                        getFontHeight() + height.getValueInt(),
                        ColorUtil.injectAlpha(getColor(counter), (int) (255 * modules.fade)));
            }
            if (modules.isEnabled || !preY.getValue()) {
                if (down.getValue()) {
                    lastY -= (getFontHeight() + height.getValueInt());
                } else {
                    lastY += (getFontHeight() + height.getValueInt());
                }
            }
        }
    }

    public double animate(double current, double endPoint, double speed) {
        if (speed >= 1) return endPoint;
        if (speed == 0) return current;
        return AnimateUtil.animate(current, endPoint, speed, animMode.getValue());
    }
    private String getSuffix(String s) {
        if (forgeHax.getValue()) {
            return s + "§r<";
        }
        return s;
    }

    public int getColor(int counter) {
        if (colorMode.getValue() != ColorMode.Custom) {
            return rainbow(counter).getRGB();
        }
        return color.getValue().getRGB();
    }


    private Color rainbow(int delay) {
        if (colorMode.getValue() == ColorMode.Pulse) {
            return ColorUtil.pulseColor(color.getValue(), endColor.getValue(), delay, pulseCounter.getValueInt(), pulseSpeed.getValue());
        } else if (colorMode.getValue() == ColorMode.Rainbow) {
            double rainbowState = Math.ceil((System.currentTimeMillis() * rainbowSpeed.getValue() + delay * rainbowDelay.getValue()) / 20.0);
            return Color.getHSBColor((float) (rainbowState % 360.0 / 360), saturation.getValueFloat() / 255.0f, 1.0f);
        }
        return color.getValue();
    }

    private int getStringWidth(String text) {
        if (font.getValue()) {
            return (int) FontRenderers.ui.getWidth(text);
        }
        return mc.textRenderer.getWidth(text);
    }
    
    private int getFontHeight() {
        if (font.getValue()) {
            return (int) FontRenderers.ui.getFontHeight();
        }
        return mc.textRenderer.fontHeight;
    }
    public class Modules {
        public boolean isEnabled = false;
        public final Module module;
        public double x = 0;
        public double y = 0;
        public double fade = 0;
        public boolean hide = true;
        public double fold = 0;

        public Modules(Module module) {
            this.module = module;
        }

        public void enable() {
            if (isEnabled) return;
            isEnabled = true;
        }

        public void disable() {
            if (!isEnabled) return;
            isEnabled = false;
        }
        public String lastName = "";

        public String name = "";
        public boolean nameUpdated = false;
        public void updateName() {
            String name = module.getArrayName();

            this.lastName = name;
            if (space.getValue()) {
                name = module.getDisplayName().replaceAll("([a-z])([A-Z])", "$1 $2");
                if (name.startsWith(" ")) {
                    name = name.replaceFirst(" ", "");
                }
                name = name + module.getArrayInfo();
            }
            if (lowerCase.getValue()) {
                name = name.toLowerCase();
            }
            this.name = name;
            update = true;
        }

        public void update() {
            String name = module.getArrayName();

            if (!this.lastName.equals(name)) {
                this.lastName = name;
                if (space.getValue()) {
                    name = module.getDisplayName().replaceAll("([a-z])([A-Z])", "$1 $2");
                    if (name.startsWith(" ")) {
                        name = name.replaceFirst(" ", "");
                    }
                    name = name + module.getArrayInfo();
                }
                if (lowerCase.getValue()) {
                    name = name.toLowerCase();
                }
                this.name = name;
                update = true;
                nameUpdated = true;
            }
        }
    }
}
