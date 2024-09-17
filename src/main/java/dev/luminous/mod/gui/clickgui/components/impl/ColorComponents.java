package dev.luminous.mod.gui.clickgui.components.impl;

import dev.luminous.api.utils.math.Animation;
import dev.luminous.mod.modules.impl.client.ClickGui;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.core.impl.GuiManager;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.api.utils.math.MathUtil;
import dev.luminous.api.utils.render.Render2DUtil;
import dev.luminous.api.utils.render.TextUtil;
import dev.luminous.mod.gui.clickgui.components.Component;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.gui.clickgui.tabs.ClickGuiTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
public class ColorComponents extends Component {
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;

    private boolean afocused;
    private boolean hfocused;
    private boolean sbfocused;

    private float spos, bpos, hpos, apos;

    private Color prevColor;

    private boolean firstInit;

    private final ColorSetting colorSetting;

    public ColorSetting getColorSetting() {
        return colorSetting;
    }

    public ColorComponents(ClickGuiTab parent, ColorSetting setting) {
        super();
        this.parent = parent;
        this.colorSetting = setting;
        prevColor = getColorSetting().getValue();
        updatePos();
        firstInit = true;
    }

    @Override
    public boolean isVisible() {
        if (colorSetting.visibility != null) {
            return colorSetting.visibility.getAsBoolean();
        }
        return true;
    }
    private void updatePos() {
        float[] hsb = Color.RGBtoHSB(getColorSetting().getValue().getRed(), getColorSetting().getValue().getGreen(), getColorSetting().getValue().getBlue(), null);
        hue = -1 + hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = getColorSetting().getValue().getAlpha();
    }

    private void setColor(Color color) {
        getColorSetting().setValue(color.getRGB());
        prevColor = color;
    }
    static int copyColor = -1;
    private final Timer clickTimer = new Timer();
    private double lastMouseX;
    private double lastMouseY;
    boolean clicked = false;
    boolean popped = false;
    boolean hover = false;
    public Animation animation3 = new Animation();
    double pickerHeight = 0;

    @Override
    public int getCurrentHeight() {
        return (int) (defaultHeight + pickerHeight);
    }

    @Override
    
    
    public void update(int offset, double mouseX, double mouseY) {
        int x = parent.getX();
        int y = (parent.getY() + offset) - 2;
        int width = parent.getWidth();
        double cx = x + 3;
        double cy = y + defaultHeight;
        double cw = width - 19;
        double ch = getHeight() - 17;
        hover = Render2DUtil.isHovered(mouseX, mouseY, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight - 1);
        boolean copyOrPaste = hover && InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT);
        if (copyOrPaste) {
            if (GuiManager.currentGrabbed == null && isVisible()) {
                if (ClickGuiScreen.clicked) {
                    ClickGuiScreen.clicked = false;
                    sound();
                    copyColor = colorSetting.getValue().getRGB();
                }
                if (ClickGuiScreen.rightClicked) {
                    ClickGuiScreen.rightClicked = false;
                    sound();
                    colorSetting.setValue(copyColor);
                }
            }
            return;
        }
        if (hover) {
            if (GuiManager.currentGrabbed == null && isVisible()) {
                if (ClickGuiScreen.rightClicked) {
                    ClickGuiScreen.rightClicked = false;
                    sound();
                    this.popped = !this.popped;
                }
            }
        }
        if (popped) {
            pickerHeight = animation3.get(45);
            setHeight(defaultHeight + 45);
        } else {
            pickerHeight = animation3.get(0);
            setHeight(defaultHeight);
        }
        if ((ClickGuiScreen.clicked || ClickGuiScreen.hoverClicked) && isVisible()) {
            if (!clicked) {
                if (Render2DUtil.isHovered(mouseX, mouseY, cx + cw + 9, cy, 4, ch)) {
                    afocused = true;
                    ClickGuiScreen.hoverClicked = true;
                    ClickGuiScreen.clicked = false;
                }
                if (Render2DUtil.isHovered(mouseX, mouseY, cx + cw + 4, cy, 4, ch)) {
                    hfocused = true;
                    ClickGuiScreen.hoverClicked = true;
                    ClickGuiScreen.clicked = false;
                    if (colorSetting.isRainbow) {
                        colorSetting.setRainbow(false);
                        lastMouseX = 0;
                        lastMouseY = 0;
                    } else {
                        if (!clickTimer.passedMs(400) && mouseX == lastMouseX && mouseY == lastMouseY) {
                            colorSetting.setRainbow(!colorSetting.isRainbow);
                        }
                        clickTimer.reset();
                        lastMouseX = mouseX;
                        lastMouseY = mouseY;
                    }
                }
                if (Render2DUtil.isHovered(mouseX, mouseY, cx, cy, cw, ch)) {
                    sbfocused = true;
                    ClickGuiScreen.hoverClicked = true;
                    ClickGuiScreen.clicked = false;
                }
                if (GuiManager.currentGrabbed == null && isVisible()) {
                    if (hover && getColorSetting().injectBoolean) {
                        getColorSetting().booleanValue = !getColorSetting().booleanValue;
                        sound();
                        ClickGuiScreen.clicked = false;
                    }
                }
            }
            clicked = true;
        } else {
            clicked = false;
            sbfocused = false;
            afocused = false;
            hfocused = false;
        }
        if (!popped) return;
        if (GuiManager.currentGrabbed == null && isVisible()) {
            Color value = Color.getHSBColor(hue, saturation, brightness);
            if (sbfocused) {
                saturation = (float) ((MathUtil.clamp((float) (mouseX - cx), 0f, (float) cw)) / cw);
                brightness = (float) ((ch - MathUtil.clamp((float) (mouseY - cy), 0, (float) ch)) / ch);
                value = Color.getHSBColor(hue, saturation, brightness);
                setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
            }

            if (hfocused) {
                hue = (float) -((ch - MathUtil.clamp((float) (mouseY - cy), 0, (float) ch)) / ch);
                value = Color.getHSBColor(hue, saturation, brightness);
                setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
            }

            if (afocused) {
                alpha = (int) (((ch - MathUtil.clamp((float) (mouseY - cy), 0, (float) ch)) / ch) * 255);
                setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
            }
        }
    }

    public double currentWidth = 0;

    @Override
    
    
    public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
        if (popped) {
            pickerHeight = animation3.get(45);
            setHeight(defaultHeight + 45);
        } else {
            pickerHeight = animation3.get(0);
            setHeight(defaultHeight);
        }
        int x = parent.getX();
        int y = parent.getY() + offset - 2;
        int width = parent.getWidth();
        MatrixStack matrixStack = drawContext.getMatrices();

        Render2DUtil.drawRect(matrixStack, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1), hover ? ClickGui.INSTANCE.settingHover.getValue() : ClickGui.INSTANCE.setting.getValue());

        boolean unShift = !hover || !InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT);
        if (colorSetting.injectBoolean) {
            currentWidth = animation.get(colorSetting.booleanValue ? (width - 2D) : 0D);
            switch (ClickGui.INSTANCE.uiType.getValue()) {
                case Old -> {
                    if (ClickGui.INSTANCE.mainEnd.booleanValue) {
                        Render2DUtil.drawRectHorizontal(matrixStack, (float) x + 1, (float) y + 1, (float) currentWidth, (float) defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1), hover ? ClickGui.INSTANCE.mainHover.getValue() : color, ClickGui.INSTANCE.mainEnd.getValue());
                    } else {
                        Render2DUtil.drawRect(matrixStack, (float) x + 1, (float) y + 1, (float) currentWidth, (float) defaultHeight - (ClickGui.INSTANCE.maxFill.getValue() ? 0 : 1), hover ? ClickGui.INSTANCE.mainHover.getValue() : color);
                    }
                    if (unShift) {
                        TextUtil.drawString(drawContext, colorSetting.getName(), x + 4, y + getTextOffsetY(), -1);
                    }
                }
                case New -> {
                    if (unShift) {
                        TextUtil.drawString(drawContext, colorSetting.getName(), x + 4, y + getTextOffsetY(), colorSetting.booleanValue ? ClickGui.INSTANCE.enableTextS.getValue() : ClickGui.INSTANCE.disableText.getValue());
                    }
                }
            }
        } else if (unShift) {
            TextUtil.drawString(drawContext, colorSetting.getName(), x + 4, y + getTextOffsetY(), -1);
        }
        if (!unShift) {
            TextUtil.drawString(drawContext, "§aL-Copy §cR-Paste", x + 4, y + getTextOffsetY(), -1);
        }
        Render2DUtil.drawRound(matrixStack, (float) (x + width - 16), (float) (y + getTextOffsetY()), 12, 8, 1, ColorUtil.injectAlpha(getColorSetting().getValue(), 255));

        if (pickerHeight <= 1) {
            return true;
        }
        double cy = y + defaultHeight + 1;
        double cw = width - 15;
        double ch = defaultHeight - 17 + pickerHeight;

        if (prevColor != getColorSetting().getValue()) {
            updatePos();
            prevColor = getColorSetting().getValue();
        }

        if (firstInit) {
            spos = (float) (((double) x + cw) - (cw - (cw * saturation)));
            bpos = (float) ((cy + (ch - (ch * brightness))));
            hpos = (float) ((cy + (ch - 3 + ((ch - 3) * hue))));
            apos = (float) ((cy + (ch - 3 - ((ch - 3) * (alpha / 255f)))));
            firstInit = false;
        }

        spos = (float) animate(spos, (float) (((double) x + cw) - (cw - (cw * saturation))), .6f);
        bpos = (float) animate(bpos, (float) (cy + (ch - (ch * brightness))), .6f);
        hpos = (float) animate(hpos, (float) (cy + (ch - 3 + ((ch - 3) * hue))), .6f);
        apos = (float) animate(apos, (float) (cy + (ch - 3 - ((ch - 3) * (alpha / 255f)))), .6f);

        Color colorA = Color.getHSBColor(hue, 0.0F, 1.0F), colorB = Color.getHSBColor(hue, 1.0F, 1.0F);
        Color colorC = new Color(0, 0, 0, 0), colorD = new Color(0, 0, 0);

        Render2DUtil.horizontalGradient(matrixStack, (float) (double) x + 2, (float) cy, (float) ((double) x + cw), (float) (cy + ch), colorA, colorB);
        Render2DUtil.verticalGradient(matrixStack, (float) ((double) x + 2), (float) cy, (float) ((double) x + cw), (float) (cy + ch), colorC, colorD);

        for (float i = 1f; i < ch - 2f; i += 1f) {
            float curHue = (float) (1f / (ch / i));
            Render2DUtil.drawRect(matrixStack, (float) ((double) x + cw + 4), (float) (cy + i), 4, 1, Color.getHSBColor(curHue, 1f, 1f));
        }

        Render2DUtil.verticalGradient(matrixStack, (float) ((double) x + cw + 9), (float) (cy + 0.8f), (float) ((double) x + cw + 12.5), (float) (cy + ch - 2), new Color(getColorSetting().getValue().getRed(), getColorSetting().getValue().getGreen(), getColorSetting().getValue().getBlue(), 255), new Color(0, 0, 0, 0));

        Render2DUtil.drawRect(matrixStack, (float) ((double) x + cw + 3), hpos + 0.5f, 5, 1, Color.WHITE);
        Render2DUtil.drawRect(matrixStack, (float) ((double) x + cw + 8), apos + 0.5f, 5, 1, Color.WHITE);
        Render2DUtil.drawRound(matrixStack, spos - 1.5f, bpos - 1.5f, 3, 3, 1.5f, new Color(-1));
        return true;
    }
}