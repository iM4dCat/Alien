package dev.luminous.mod.modules.impl.movement;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.UpdateWalkingPlayerEvent;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.BaritoneModule;
import dev.luminous.mod.modules.impl.combat.SelfTrap;
import dev.luminous.mod.modules.impl.combat.Surround;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Step extends Module {
	private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Vanilla));
	private final SliderSetting height = add(new SliderSetting("Height", 1, 0.0, 5, 0.5));
	public final BooleanSetting onlyMoving =
			add(new BooleanSetting("OnlyMoving", true));
	public final BooleanSetting useTimer =
			add(new BooleanSetting("Timer", true, () -> mode.getValue() == Mode.OldNCP || mode.getValue() == Mode.NCP));
	public final BooleanSetting fast =
			add(new BooleanSetting("Fast", true, () -> mode.getValue() == Mode.NCP && useTimer.getValue()));
	public final BooleanSetting surroundPause =
			add(new BooleanSetting("SurroundPause", true));
	public final BooleanSetting inWebPause =
			add(new BooleanSetting("InWebPause", true));
	public final BooleanSetting inBlockPause =
			add(new BooleanSetting("InBlockPause", true));
	public final BooleanSetting sneakingPause =
			add(new BooleanSetting("SneakingPause", true));
	public final BooleanSetting pathingPause =
			add(new BooleanSetting("PathingPause", true));
	public Step() {
		super("Step", "Steps up blocks.", Category.Movement);
		setChinese("步行辅助");
	}

	@Override
	public void onDisable() {
		if (nullCheck()) return;
		mc.player.setStepHeight(0.6f);
		Alien.TIMER.reset();
	}

	@Override
	public String getInfo() {
		return mode.getValue().name();
	}

	boolean timer;
    @Override
    public void onUpdate() {
		if (pathingPause.getValue() && BaritoneModule.isActive() || sneakingPause.getValue() && mc.player.isSneaking() || inBlockPause.getValue() && Alien.PLAYER.insideBlock || mc.player.isInLava() || mc.player.isTouchingWater() || inWebPause.getValue() && Alien.PLAYER.isInWeb(mc.player) || !mc.player.isOnGround() || onlyMoving.getValue() && !MovementUtil.isMoving() || surroundPause.getValue() && (Surround.INSTANCE.isOn() || SelfTrap.INSTANCE.isOn())) {
			mc.player.setStepHeight(0.6f);
			return;
		}
		mc.player.setStepHeight(height.getValueFloat());
	}

	int packets = 0;

	@EventHandler
	public void onStep(UpdateWalkingPlayerEvent event) {
		if (event.isPost()) {
			packets--;
			return;
		}
		if (timer && packets <= 0) {
			Alien.TIMER.reset();
			timer = false;
		}
		boolean strict = mode.getValue() == Mode.NCP;
		if (mode.getValue().equals(Mode.OldNCP) || strict) {
			double stepHeight = mc.player.getY() - mc.player.prevY;
			if (stepHeight <= 0.75 || stepHeight > height.getValue()) {
				return;
			}

			double[] offsets = getOffset(stepHeight);
			if (offsets != null && offsets.length > 1) {
				if (useTimer.getValue()) {
					Alien.TIMER.set((float) getTimer(stepHeight));
					timer = true;
					packets = 2;
				}
				for (double offset : offsets) {
					mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.prevX, mc.player.prevY + offset, mc.player.prevZ, false));
				}
			}
		}
	}

	public double getTimer(double height) {
		if (height > 0.6 && height <= 1) {
			if (!fast.getValue() && mode.getValue() == Mode.NCP) {
				return 1d / 3d;
			}
			return 0.5;
		}
		double[] offsets = getOffset(height);
		if (offsets == null) {
			return 1;
		}
		return 1d / offsets.length;
	}
	public double[] getOffset(double height) {
		boolean strict = mode.getValue() == Mode.NCP;
		if (height == 0.75) {
			if (strict) {
				return new double[]{0.42, 0.753, 0.75};
			} else {
				return new double[]{0.42, 0.753};
			}
		} else if (height == 0.8125) {
			if (strict) {
				return new double[]{0.39, 0.7, 0.8125};
			} else {
				return new double[]{0.39, 0.7};
			}
		} else if (height == 0.875) {
			if (strict) {
				return new double[]{0.39, 0.7, 0.875};
			} else {
				return new double[]{0.39, 0.7};
			}
		} else if (height == 1) {
			if (strict) {
				return new double[]{0.42, 0.753, 1};
			} else {
				return new double[]{0.42, 0.753};
			}
		} else if (height == 1.5) {
			return new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
		} else if (height == 2) {
			return new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
		} else if (height == 2.5) {
			return new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
		}

		return null;
	}

	public enum Mode {
		Vanilla,
		OldNCP,
		NCP
	}
}
