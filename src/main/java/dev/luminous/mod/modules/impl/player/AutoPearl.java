package dev.luminous.mod.modules.impl.player;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.LookAtEvent;
import dev.luminous.mod.modules.impl.client.AntiCheat;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class AutoPearl extends Module {
	public static AutoPearl INSTANCE;

	public AutoPearl() {
		super("AutoPearl", Category.Player);
		setChinese("扔珍珠");
		INSTANCE = this;
	}

	public final BooleanSetting inventory =
			add(new BooleanSetting("InventorySwap", true));
	private final BooleanSetting rotation = add(new BooleanSetting("Rotation", false));
	private final SliderSetting steps = add(new SliderSetting("Steps", 0.05, 0, 1, 0.01, () -> rotation.getValue()));
	private final SliderSetting fov = add(new SliderSetting("Fov", 10, 0, 50, () -> rotation.getValue()));
	private final SliderSetting priority = add(new SliderSetting("Priority", 100,0 ,100, () -> rotation.getValue()));
	boolean shouldThrow = false;
	@Override
	public void onEnable() {
		if (nullCheck()) {
			disable();
			return;
		}
		if (rotation.getValue()) {
			return;
		}
		if (getBind().isHoldEnable()) {
			shouldThrow = true;
			return;
		}
		throwPearl(mc.player.getYaw(), mc.player.getPitch());
		disable();
	}

	@Override
	public void onUpdate() {
		if (rotation.getValue() && Alien.ROTATION.inFov(mc.player.getYaw(), mc.player.getPitch(), fov.getValueFloat())) {
			throwing = true;
			int pearl;

			if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
				sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
			} else if (inventory.getValue() && (pearl = InventoryUtil.findItemInventorySlot(Items.ENDER_PEARL)) != -1) {
				InventoryUtil.inventorySwap(pearl, mc.player.getInventory().selectedSlot);
				sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
				InventoryUtil.inventorySwap(pearl, mc.player.getInventory().selectedSlot);
				EntityUtil.syncInventory();
			} else if ((pearl = InventoryUtil.findItem(Items.ENDER_PEARL)) != -1) {
				int old = mc.player.getInventory().selectedSlot;
				InventoryUtil.switchToSlot(pearl);
				sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
				InventoryUtil.switchToSlot(old);
			}
			throwing = false;
			disable();
		}
	}

	@EventHandler()
	public void onRotate(LookAtEvent event) {
		if (rotation.getValue()) {
			event.setRotation(mc.player.getYaw(), mc.player.getPitch(), steps.getValueFloat(), priority.getValueFloat());
		}
	}

	public static boolean throwing = false;
	public void throwPearl(float yaw, float pitch) {
		throwing = true;
		int pearl;
		if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
			Alien.ROTATION.snapAt(yaw, pitch);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
			if (AntiCheat.INSTANCE.snapBack.getValue()) {
				Alien.ROTATION.snapBack();
			}
		} else if (inventory.getValue() && (pearl = InventoryUtil.findItemInventorySlot(Items.ENDER_PEARL)) != -1) {
			InventoryUtil.inventorySwap(pearl, mc.player.getInventory().selectedSlot);
			Alien.ROTATION.snapAt(yaw, pitch);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
			InventoryUtil.inventorySwap(pearl, mc.player.getInventory().selectedSlot);
			EntityUtil.syncInventory();
			if (AntiCheat.INSTANCE.snapBack.getValue()) {
				Alien.ROTATION.snapBack();
			}
		} else if ((pearl = InventoryUtil.findItem(Items.ENDER_PEARL)) != -1) {
			int old = mc.player.getInventory().selectedSlot;
			InventoryUtil.switchToSlot(pearl);
			Alien.ROTATION.snapAt(yaw, pitch);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
			InventoryUtil.switchToSlot(old);
			if (AntiCheat.INSTANCE.snapBack.getValue()) {
				Alien.ROTATION.snapBack();
			}
		}
		throwing = false;
	}

	@Override
	public void onDisable() {
		if (nullCheck()) {
			return;
		}
		if (shouldThrow && getBind().isHoldEnable()) {
			shouldThrow = false;
			throwPearl(mc.player.getYaw(), mc.player.getPitch());
		}
	}
}