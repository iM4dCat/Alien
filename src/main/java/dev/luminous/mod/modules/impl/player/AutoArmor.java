package dev.luminous.mod.modules.impl.player;

import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.movement.ElytraFly;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.Map;

public class AutoArmor extends Module {
	private final BooleanSetting noMove =
			add(new BooleanSetting("NoMove", false));
	private final SliderSetting delay =
			add(new SliderSetting("Delay", 3, 0, 10, 1));
	private final BooleanSetting autoElytra =
			add(new BooleanSetting("AutoElytra", true));
	private final BooleanSetting snowBug =
			add(new BooleanSetting("SnowBug", true));
	public static AutoArmor INSTANCE;

	public AutoArmor() {
		super("AutoArmor", Category.Player);
		setChinese("自动穿甲");
		INSTANCE = this;
	}

	private int tickDelay = 0;
	@Override
	public void onUpdate() {
		if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof InventoryScreen) && !(mc.currentScreen instanceof ClickGuiScreen)) {
			return;
		}

		if (mc.player.playerScreenHandler != mc.player.currentScreenHandler)
			return;

		if (MovementUtil.isMoving() && noMove.getValue()) return;

		if (tickDelay > 0) {
			tickDelay--;
			return;
		}

		tickDelay = delay.getValueInt();

		Map<EquipmentSlot, int[]> armorMap = new HashMap<>(4);
		armorMap.put(EquipmentSlot.FEET, new int[]{36, getProtection(mc.player.getInventory().getStack(36)), -1, -1});
		armorMap.put(EquipmentSlot.LEGS, new int[]{37, getProtection(mc.player.getInventory().getStack(37)), -1, -1});
		armorMap.put(EquipmentSlot.CHEST, new int[]{38, getProtection(mc.player.getInventory().getStack(38)), -1, -1});
		armorMap.put(EquipmentSlot.HEAD, new int[]{39, getProtection(mc.player.getInventory().getStack(39)), -1, -1});
		for (int s = 0; s < 36; s++) {
			if (!(mc.player.getInventory().getStack(s).getItem() instanceof ArmorItem) && mc.player.getInventory().getStack(s).getItem() != Items.ELYTRA)
				continue;
			int protection = getProtection(mc.player.getInventory().getStack(s));
			EquipmentSlot slot = (mc.player.getInventory().getStack(s).getItem() instanceof ElytraItem ? EquipmentSlot.CHEST : ((ArmorItem) mc.player.getInventory().getStack(s).getItem()).getSlotType());
			for (Map.Entry<EquipmentSlot, int[]> e : armorMap.entrySet()) {
				if (e.getKey() == EquipmentSlot.FEET) {
					if (mc.player.hurtTime > 1 && snowBug.getValue()) {
						if (!mc.player.getInventory().getStack(36).isEmpty() && mc.player.getInventory().getStack(36).getItem() == Items.LEATHER_BOOTS) {
							continue;
						}
						if (!mc.player.getInventory().getStack(s).isEmpty() && mc.player.getInventory().getStack(s).getItem() == Items.LEATHER_BOOTS) {
							e.getValue()[2] = s;
							continue;
						}
					}
				}
				if (autoElytra.getValue() && (ElytraFly.INSTANCE.isOn()) && e.getKey() == EquipmentSlot.CHEST) {
					if (!mc.player.getInventory().getStack(38).isEmpty() && mc.player.getInventory().getStack(38).getItem() instanceof ElytraItem && ElytraItem.isUsable(mc.player.getInventory().getStack(38))) {
						continue;
					}
					if (e.getValue()[2] != -1 && !mc.player.getInventory().getStack(e.getValue()[2]).isEmpty() && mc.player.getInventory().getStack(e.getValue()[2]).getItem() instanceof ElytraItem && ElytraItem.isUsable(mc.player.getInventory().getStack(e.getValue()[2]))) {
						continue;
					}
					if (!mc.player.getInventory().getStack(s).isEmpty() && mc.player.getInventory().getStack(s).getItem() instanceof ElytraItem && ElytraItem.isUsable(mc.player.getInventory().getStack(s))) {
						e.getValue()[2] = s;
					}
					continue;
				}
				if (protection > 0) {
					if (e.getKey() == slot) {
						if (protection > e.getValue()[1] && protection > e.getValue()[3]) {
							e.getValue()[2] = s;
							e.getValue()[3] = protection;
						}
					}
				}
			}
		}

		for (Map.Entry<EquipmentSlot, int[]> equipmentSlotEntry : armorMap.entrySet()) {
			if (equipmentSlotEntry.getValue()[2] != -1) {
				if (equipmentSlotEntry.getValue()[1] == -1 && equipmentSlotEntry.getValue()[2] < 9) {
/*					if (equipmentSlotEntry.getValue()[2] != mc.player.getInventory().selectedSlot) {
						mc.player.getInventory().selectedSlot = equipmentSlotEntry.getValue()[2];
						mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(equipmentSlotEntry.getValue()[2]));
					}*/
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36 + equipmentSlotEntry.getValue()[2], 1, SlotActionType.QUICK_MOVE, mc.player);
					EntityUtil.syncInventory();
				} else if (mc.player.playerScreenHandler == mc.player.currentScreenHandler) {
					int armorSlot = (equipmentSlotEntry.getValue()[0] - 34) + (39 - equipmentSlotEntry.getValue()[0]) * 2;
					int newArmorSlot = equipmentSlotEntry.getValue()[2] < 9 ? 36 + equipmentSlotEntry.getValue()[2] : equipmentSlotEntry.getValue()[2];
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newArmorSlot, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, armorSlot, 0, SlotActionType.PICKUP, mc.player);
					if (equipmentSlotEntry.getValue()[1] != -1)
						mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newArmorSlot, 0, SlotActionType.PICKUP, mc.player);
					EntityUtil.syncInventory();
				}
				return;
			}
		}
	}
	private int getProtection(ItemStack is) {
		if (is.getItem() instanceof ArmorItem || is.getItem() == Items.ELYTRA) {
			int prot = 0;

			if (is.getItem() instanceof ElytraItem) {
				if (!ElytraItem.isUsable(is))
					return 0;
				prot = 1;
			}
			if (is.hasEnchantments()) {
				for (Map.Entry<Enchantment, Integer> e: EnchantmentHelper.get(is).entrySet()) {
					if (e.getKey() instanceof ProtectionEnchantment)
						prot += e.getValue();
				}
			}
			return (is.getItem() instanceof ArmorItem ? ((ArmorItem) is.getItem()).getProtection() : 0) + prot;
		} else if (!is.isEmpty()) {
			return 0;
		}

		return -1;
	}
}