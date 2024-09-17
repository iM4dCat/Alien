package dev.luminous.mod.modules.impl.client;

import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.Placement;
import dev.luminous.mod.modules.settings.SwingSide;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;

public class AntiCheat extends Module {
    public static AntiCheat INSTANCE;
    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));

    public final BooleanSetting multiPlace = add(new BooleanSetting("MultiPlace", true, () -> page.is(Page.General)));
    public final BooleanSetting packetPlace = add(new BooleanSetting("PacketPlace", true, () -> page.is(Page.General)));
    public final BooleanSetting attackRotate = add(new BooleanSetting("AttackRotation", false, () -> page.is(Page.General)));
    public final BooleanSetting invSwapBypass = add(new BooleanSetting("PickSwap", false, () -> page.is(Page.General)));
    public final SliderSetting boxSize = add(new SliderSetting("HitBoxSize", 0.6, 0, 1, 0.01, () -> page.is(Page.General)));
    public final SliderSetting attackDelay = add(new SliderSetting("BreakDelay", 0.2, 0, 1, 0.01, () -> page.is(Page.General)).setSuffix("s"));
    public final BooleanSetting noBadSlot = add(new BooleanSetting("NoBadSlot", false, () -> page.is(Page.General)));
    public final EnumSetting<Placement> placement = add(new EnumSetting<>("Placement", Placement.Vanilla, () -> page.is(Page.General)));
    public final BooleanSetting blockCheck = add(new BooleanSetting("BlockCheck", true, () -> page.is(Page.General)));
    public final BooleanSetting oldNCP = add(new BooleanSetting("OldNCP", false, () -> page.is(Page.General)));

    public final BooleanSetting grimRotation = add(new BooleanSetting("GrimRotation", false, () -> page.is(Page.Rotation)));
    public final BooleanSetting snapBack = add(new BooleanSetting("SnapBack", false, () -> page.is(Page.Rotation)));
    public final BooleanSetting look = add(new BooleanSetting("Look", true, () -> page.is(Page.Rotation)));
    public final SliderSetting rotateTime = add(new SliderSetting("LookTime", 0.5, 0, 1, 0.01, () -> page.is(Page.Rotation)));
    public final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("SwingType", SwingSide.All));
    public final BooleanSetting noSpamRotation = add(new BooleanSetting("SpamCheck", true, () -> page.is(Page.Rotation)).setParent());
    public final SliderSetting fov = add(new SliderSetting("Fov", 10, 0, 180, 0.1, () -> page.is(Page.Rotation) && noSpamRotation.isOpen()));
    public final SliderSetting steps = add(new SliderSetting("Steps", 0.6, 0, 1, 0.01, () -> page.is(Page.Rotation)));
    public final BooleanSetting forceSync = add(new BooleanSetting("ServerSide", false, () -> page.is(Page.Rotation)));

    public final BooleanSetting obsMode = add(new BooleanSetting("OBSServer", false, () -> page.is(Page.Misc)));
    public final BooleanSetting inventorySync = add(new BooleanSetting("InventorySync", false, () -> page.is(Page.Misc)));

    public enum Page {
        General,
        Rotation,
        Misc
    }
    public AntiCheat() {
        super("AntiCheat", Category.Client);
        setChinese("反作弊选项");
        INSTANCE = this;
    }

    public static double getOffset() {
        if (INSTANCE != null) return INSTANCE.boxSize.getValue() / 2;
        return 0.3;
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
