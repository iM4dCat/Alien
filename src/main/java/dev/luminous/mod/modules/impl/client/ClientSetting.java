package dev.luminous.mod.modules.impl.client;

import dev.luminous.api.utils.math.Animation;
import dev.luminous.api.utils.math.Easing;
import dev.luminous.api.utils.math.FadeUtils;
import dev.luminous.Alien;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.*;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;

import java.awt.*;
import java.util.HashMap;

public class ClientSetting extends Module {
    public static ClientSetting INSTANCE;
    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.Game));
    public final BooleanSetting lowVersion = add(new BooleanSetting("1.12", false, () -> page.is(Page.Game)));
    public final BooleanSetting crawl = add(new BooleanSetting("Crawl", true, () -> page.is(Page.Game)));
    public final BooleanSetting rotations = add(new BooleanSetting("ShowRotations", true, () -> page.is(Page.Game)));
    public final BooleanSetting titleFix = add(new BooleanSetting("TitleFix", true, () -> page.is(Page.Game)));
    private final BooleanSetting portalGui = add(new BooleanSetting("PortalGui", true, () -> page.is(Page.Game)));

    public final StringSetting windowTitle = add(new StringSetting("WindowTitle", Alien.NAME, () -> page.is(Page.Misc)));
    public final BooleanSetting titleOverride = add(new BooleanSetting("TitleOverride", true, () -> page.is(Page.Misc)));
    public final BooleanSetting debug = add(new BooleanSetting("DebugException", true, () -> page.is(Page.Misc)));
    public final BooleanSetting caughtException = add(new BooleanSetting("CaughtException", false, () -> page.is(Page.Misc)).setParent());
    public final BooleanSetting log = add(new BooleanSetting("Log", true, () -> page.is(Page.Misc) && caughtException.isOpen()));

    private final BooleanSetting inventoryAnim = add(new BooleanSetting("InventoryAnim", true, () -> page.is(Page.Gui)));
    private final SliderSetting inventoryTime = add(new SliderSetting("InvTime", 300, 0, 1000, () -> page.is(Page.Gui)));
    private final BooleanSetting hotbar = add(new BooleanSetting("HotbarAnim", true, () -> page.is(Page.Gui)));
    public final SliderSetting hotbarTime = add(new SliderSetting("HotbarTime", 300, 0, 1000, () -> page.is(Page.Gui)));
    public final EnumSetting<Easing> animEase = add(new EnumSetting<>("AnimEase", Easing.CubicInOut, () -> page.is(Page.Gui)));
    public final BooleanSetting guiBackground = add(new BooleanSetting("GuiBackground", true, () -> page.is(Page.Gui)).setParent());
    public final ColorSetting customBackground = add(new ColorSetting("CustomBackground", new Color(0, 0, 0, 36), () -> page.is(Page.Gui)).injectBoolean(false));
    public final ColorSetting endColor = add(new ColorSetting("End", new Color(255, 0, 0, 80), () -> page.is(Page.Gui) && customBackground.booleanValue));
    public final ColorSetting customButton = add(new ColorSetting("CustomButton", new Color(0, 0, 0, 100), () -> page.is(Page.Gui)).injectBoolean(false));
    public final ColorSetting hover = add(new ColorSetting("Hover", new Color(255, 255, 255, 100), () -> page.is(Page.Gui) && customButton.booleanValue));
    public final SliderSetting speed = add(new SliderSetting("Time", 100, 0, 500, 1, () -> page.is(Page.Gui) && customButton.booleanValue));
    public final ColorSetting snow = add(new ColorSetting("Snow", new Color(255, 255, 255, 70), () -> page.is(Page.Gui)).injectBoolean(false));

    public final StringSetting hackName = add(new StringSetting("Notification", "[Alien]", () -> page.getValue() == Page.Notification));
    public final ColorSetting color = add(new ColorSetting("Color", new Color(255, 38, 38), () -> page.getValue() == Page.Notification));
    public final ColorSetting pulse = add(new ColorSetting("Pulse", new Color(145, 0, 0), () -> page.getValue() == Page.Notification).injectBoolean(true));
    public final SliderSetting pulseSpeed = add(new SliderSetting("Speed", 1, 0, 5, 0.1, () -> page.getValue() == Page.Notification && pulse.booleanValue));
    public final SliderSetting pulseCounter = add(new SliderSetting("Counter", 10, 1, 50, () -> page.getValue() == Page.Notification && pulse.booleanValue));
    public final EnumSetting<Style> messageStyle = add(new EnumSetting<>("Style", Style.Mio, () -> page.getValue() == Page.Notification));
    public final BooleanSetting toggle = add(new BooleanSetting("ModuleToggle", true, () -> page.getValue() == Page.Notification).setParent());
    public final BooleanSetting onlyOne = add(new BooleanSetting("OnlyOne", false, () -> page.getValue() == Page.Notification && toggle.isOpen()));

    public final BooleanSetting keepHistory = add(new BooleanSetting("KeepHistory", true, () -> page.getValue() == Page.ChatHud));
    public final BooleanSetting infiniteChat = add(new BooleanSetting("InfiniteChat", true, () -> page.getValue() == Page.ChatHud));
    public final SliderSetting animateTime = add(new SliderSetting("AnimTime", 300, 0, 1000, () -> page.getValue() == Page.ChatHud));
    public final SliderSetting animateOffset = add(new SliderSetting("AnimOffset", -40, -200, 100, () -> page.getValue() == Page.ChatHud));
    public final EnumSetting<Easing> ease = add(new EnumSetting<>("Ease", Easing.CubicInOut, () -> page.getValue() == Page.ChatHud));
    public final BooleanSetting fade = add(new BooleanSetting("Fade", true, () -> page.getValue() == Page.ChatHud));
    public final BooleanSetting yAnim = add(new BooleanSetting("YAnim", false, () -> page.getValue() == Page.ChatHud));
    public final SliderSetting fadeTime = add(new SliderSetting("FadeTime", 300, 0, 1000, () -> page.getValue() == Page.ChatHud));
    public final BooleanSetting inputBoxAnim = add(new BooleanSetting("InputBoxAnim", true, () -> page.getValue() == Page.ChatHud));
    public final BooleanSetting hideIndicator = add(new BooleanSetting("HideIndicator", true, () -> page.getValue() == Page.ChatHud));

    public ClientSetting() {
        super("ClientSetting", Category.Client);
        setChinese("客户端设置");
        INSTANCE = this;
    }

    public static final FadeUtils inventoryFade = new FadeUtils(500);
    public static final Animation animation = new Animation();

    @Override
    public void onUpdate() {
        inventoryFade.setLength(inventoryTime.getValueInt());
        if (mc.currentScreen == null && inventoryAnim.getValue()) {
            inventoryFade.reset();
        }
    }

    public enum Page {
        Game,
        Gui,
        Misc,
        Notification,
        ChatHud
    }

    public enum Style {
        Mio,
        Debug,
        Lowercase,
        Normal,
        Future,
        Earth,
        Moon,
        Melon,
        Chinese,
        None
    }

    public static final HashMap<OrderedText, StringVisitable> chatMessage = new HashMap<>();

    public boolean portalGui() {
        return isOn() && portalGui.getValue();
    }

    public boolean hotbar() {
        return isOn() && hotbar.getValue();
    }

    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }
}