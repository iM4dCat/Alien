package dev.luminous.mod.modules.impl.client;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.calc.IPathingControlManager;
import baritone.api.process.ICustomGoalProcess;
import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.TickEvent;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;

public class BaritoneModule extends Module {
    public static BaritoneModule INSTANCE;
    SliderSetting rangeConfig = add(new SliderSetting("Range", 4.0f, 1.0f, 5.0f));
    BooleanSetting placeConfig = add(new BooleanSetting("Place", true));
    BooleanSetting breakConfig = add(new BooleanSetting("Break", true));
    BooleanSetting sprintConfig = add(new BooleanSetting("Sprint", true));
    BooleanSetting inventoryConfig = add(new BooleanSetting("UseInventory", false));
    BooleanSetting vinesConfig = add(new BooleanSetting("Vines", true));
    BooleanSetting jump256Config = add(new BooleanSetting("JumpAt256", false));
    BooleanSetting waterBucketFallConfig = add(new BooleanSetting("WaterBucketFall", false));
    BooleanSetting parkourConfig = add(new BooleanSetting("Parkour", true));
    BooleanSetting parkourPlaceConfig = add(new BooleanSetting("ParkourPlace", false));
    BooleanSetting parkourAscendConfig = add(new BooleanSetting("ParkourAscend", true));
    BooleanSetting diagonalAscendConfig = add(new BooleanSetting("DiagonalAscend", false));
    BooleanSetting diagonalDescendConfig = add(new BooleanSetting("DiagonalDescend", false));
    BooleanSetting mineDownConfig = add(new BooleanSetting("MineDownward", true));
    BooleanSetting legitMineConfig = add(new BooleanSetting("LegitMine", false));
    BooleanSetting logOnArrivalConfig = add(new BooleanSetting("LogOnArrival", false));
    BooleanSetting freeLookConfig = add(new BooleanSetting("FreeLook", true));
    BooleanSetting antiCheatConfig = add(new BooleanSetting("AntiCheat", true));
    BooleanSetting strictLiquidConfig = add(new BooleanSetting("Strict-Liquid", false));
    BooleanSetting censorCoordsConfig = add(new BooleanSetting("CensorCoords", false));
    BooleanSetting censorCommandsConfig = add(new BooleanSetting("CensorCommands", false));
    BooleanSetting chatControl = add(new BooleanSetting("ChatControl", false));
    BooleanSetting debugConfig = add(new BooleanSetting("Debug", false));

    public BaritoneModule() {
        super("Baritone", Category.Client);
        Alien.EVENT_BUS.subscribe(this);
        INSTANCE = this;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.isPre()) {
            return;
        }
        BaritoneAPI.getSettings().blockReachDistance.value = rangeConfig.getValueFloat();
        BaritoneAPI.getSettings().allowPlace.value = placeConfig.getValue();
        BaritoneAPI.getSettings().allowBreak.value = breakConfig.getValue();
        BaritoneAPI.getSettings().allowSprint.value = sprintConfig.getValue();
        BaritoneAPI.getSettings().allowInventory.value = inventoryConfig.getValue();
        BaritoneAPI.getSettings().allowVines.value = vinesConfig.getValue();
        BaritoneAPI.getSettings().allowJumpAt256.value = jump256Config.getValue();
        BaritoneAPI.getSettings().allowWaterBucketFall.value = waterBucketFallConfig.getValue();
        BaritoneAPI.getSettings().allowParkour.value = parkourConfig.getValue();
        BaritoneAPI.getSettings().allowParkourAscend.value = parkourAscendConfig.getValue();
        BaritoneAPI.getSettings().allowParkourPlace.value = parkourPlaceConfig.getValue();
        BaritoneAPI.getSettings().allowDiagonalAscend.value = diagonalAscendConfig.getValue();
        BaritoneAPI.getSettings().allowDiagonalDescend.value = diagonalDescendConfig.getValue();
        BaritoneAPI.getSettings().allowDownward.value = mineDownConfig.getValue();
        BaritoneAPI.getSettings().legitMine.value = legitMineConfig.getValue();
        BaritoneAPI.getSettings().disconnectOnArrival.value = logOnArrivalConfig.getValue();
        BaritoneAPI.getSettings().freeLook.value = freeLookConfig.getValue();
        BaritoneAPI.getSettings().antiCheatCompatibility.value = antiCheatConfig.getValue();
        BaritoneAPI.getSettings().strictLiquidCheck.value = strictLiquidConfig.getValue();
        BaritoneAPI.getSettings().censorCoordinates.value = censorCoordsConfig.getValue();
        BaritoneAPI.getSettings().censorRanCommands.value = censorCommandsConfig.getValue();
        BaritoneAPI.getSettings().chatControl.value = chatControl.getValue();
        BaritoneAPI.getSettings().chatDebug.value = debugConfig.getValue();
    }

    public static boolean isPathing() {
        IBaritone primary = BaritoneAPI.getProvider().getPrimaryBaritone();
        return primary != null && primary.getPathingBehavior() != null && primary.getPathingBehavior().isPathing();
    }
    public static void cancelEverything() {
        IBaritone primary = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (primary != null && primary.getPathingBehavior() != null) {
            primary.getPathingBehavior().cancelEverything();
        }
    }
    public static boolean isActive() {
        IBaritone primary = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (primary != null) {
            ICustomGoalProcess customGoalProcess = primary.getCustomGoalProcess();
            if (customGoalProcess != null && customGoalProcess.isActive()) {
                return true;
            }

            IPathingControlManager controlManager = primary.getPathingControlManager();
            if (controlManager != null && controlManager.mostRecentInControl().isPresent()) {
                return controlManager.mostRecentInControl().get().isActive();
            }
        }
        return false;
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