package dev.luminous.mod.modules.impl.movement;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.eventbus.EventPriority;
import dev.luminous.api.events.impl.RotateEvent;
import dev.luminous.api.events.impl.SprintEvent;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.BaritoneModule;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;

public class Sprint extends Module {

	public static Sprint INSTANCE;
	public final EnumSetting<Mode> mode =
			add(new EnumSetting<>("Mode", Mode.Rage));
	public Sprint() {
		super("Sprint", Category.Movement);
		setChinese("强制疾跑");
		this.setDescription("Permanently keeps player in sprinting mode.");
		INSTANCE = this;
	}

	@Override
	public String getInfo() {
		return mode.getValue().name();
	}

    @Override
    public void onUpdate() {
		if (BaritoneModule.isPathing()) return;
        if (mode.getValue() == Mode.PressKey) {
            mc.options.sprintKey.setPressed(true);
        } else {
			mc.player.setSprinting(shouldSprint());
		}
	}

	@EventHandler
	public void sprint(SprintEvent event) {
		if (BaritoneModule.isPathing()) return;
		event.cancel();
		event.setSprint(shouldSprint());
	}

	private boolean shouldSprint() {
		if ((mc.player.getHungerManager().getFoodLevel() > 6 || mc.player.isCreative())
				&& MovementUtil.isMoving()
				&& !mc.player.isSneaking()
				&& !mc.player.isRiding()
				&& !mc.player.isHoldingOntoLadder()
				&& !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
			switch (mode.getValue()) {
				case Grim -> {
					if (MoveFix.INSTANCE.isOn()) {
						return mc.player.input.movementForward == 1;
					} else {
						return HoleSnap.INSTANCE.isOn() || mc.options.forwardKey.isPressed() && MathHelper.angleBetween(mc.player.getYaw(), Alien.ROTATION.rotationYaw) < 40;
					}
				}
				case Rotation -> {
					if (MoveFix.INSTANCE.isOn()) {
						return mc.player.input.movementForward == 1;
					} else {
						return HoleSnap.INSTANCE.isOn() || MathHelper.angleBetween(getSprintYaw(mc.player.getYaw()), Alien.ROTATION.rotationYaw) < 40;
					}
				}
				case Rage -> {
					return true;
				}
			}
		}
		return false;
	}
	public static float getSprintYaw(float yaw) {
		if (mc.options.forwardKey.isPressed() && !mc.options.backKey.isPressed()) {
			if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed()) {
				yaw -= 45f;
			} else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed()) {
				yaw += 45f;
			}
			// Forward movement - no change to yaw
		} else if (mc.options.backKey.isPressed() && !mc.options.forwardKey.isPressed()) {
			yaw += 180f;
			if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed()) {
				yaw += 45f;
			} else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed()) {
				yaw -= 45f;
			}
		} else if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed()) {
			yaw -= 90f;
		} else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed()) {
			yaw += 90f;
		}
		return MathHelper.wrapDegrees(yaw);
	}
	@EventHandler(priority = EventPriority.LOW)
	public void rotate(RotateEvent event) {
		if ((mc.player.getHungerManager().getFoodLevel() > 6 || mc.player.isCreative())
				&& MovementUtil.isMoving()
				&& !mc.player.isFallFlying()
				&& !mc.player.isSneaking()
				&& !mc.player.isRiding()
				&& !mc.player.isTouchingWater()
				&& !mc.player.isInLava()
				&& !mc.player.isHoldingOntoLadder()
				&& !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
			if (mode.is(Mode.Rotation) && !event.isModified()) {
				event.setYaw(getSprintYaw(mc.player.getYaw()));
			}
		}
	}
	public enum Mode {
		PressKey,
		Rage,
		Grim,
		Rotation
	}
}
