package dev.luminous.mod.modules.impl.player;

import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.movement.ElytraFly;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class OffFirework extends Module {
	public static OffFirework INSTANCE;

	public OffFirework() {
		super("OffFirework", Category.Player);
		setChinese("放烟花");
		INSTANCE = this;
	}

	public final BooleanSetting inventory =
			add(new BooleanSetting("InventorySwap", true));

	@Override
	public void onEnable() {
		if (nullCheck()) {
			disable();
			return;
		}
		off();
		disable();
	}
	public void off() {
		ElytraFly.INSTANCE.fireworkTimer.reset();
		int firework;
		if (mc.player.getMainHandStack().getItem() == Items.FIREWORK_ROCKET) {
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
		} else if (inventory.getValue() && (firework = InventoryUtil.findItemInventorySlot(Items.FIREWORK_ROCKET)) != -1) {
			InventoryUtil.inventorySwap(firework, mc.player.getInventory().selectedSlot);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
			InventoryUtil.inventorySwap(firework, mc.player.getInventory().selectedSlot);
			EntityUtil.syncInventory();
		} else if ((firework = InventoryUtil.findItem(Items.FIREWORK_ROCKET)) != -1) {
			int old = mc.player.getInventory().selectedSlot;
			InventoryUtil.switchToSlot(firework);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
			InventoryUtil.switchToSlot(old);
		}
	}
}

