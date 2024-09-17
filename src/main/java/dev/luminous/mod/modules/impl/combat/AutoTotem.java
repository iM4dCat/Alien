package dev.luminous.mod.modules.impl.combat;

import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.UpdateWalkingPlayerEvent;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.gui.clickgui.ClickGuiScreen;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
	private final BooleanSetting mainHand =
			add(new BooleanSetting("MainHand",false));
	private final BooleanSetting crystal =
			add(new BooleanSetting("Crystal",false, () -> !mainHand.getValue()));
	private final BooleanSetting gapple =
			add(new BooleanSetting("Gapple",false, () -> !mainHand.getValue()));
	private final SliderSetting health =
			add(new SliderSetting("Health", 16.0f, 0.0f, 36.0f, 0.1));
	public AutoTotem() {
		super("AutoTotem", Category.Combat);
		setChinese("自动图腾");
	}
	int totems = 0;
	private final Timer timer = new Timer();

	@Override
	public String getInfo() {
		return String.valueOf(totems);
	}

	@EventHandler
	public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
		update();
	}

	@Override
	public void onUpdate() {
		update();
	}

	private void update() {
		if (nullCheck()) return;
		totems = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
		if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof InventoryScreen) && !(mc.currentScreen instanceof ClickGuiScreen) && !(mc.currentScreen instanceof GameMenuScreen)) {
			return;
		}
		if (!timer.passedMs(200)) {
			return;
		}
		if (gapple.getValue() && !mainHand.getValue() && mc.player.getMainHandStack().getItem() instanceof SwordItem && mc.options.useKey.isPressed()) {
			if (mc.player.getOffHandStack().getItem() != Items.ENCHANTED_GOLDEN_APPLE && mc.player.getOffHandStack().getItem() != Items.GOLDEN_APPLE) {
				int itemSlot = findItemInventorySlot(Items.ENCHANTED_GOLDEN_APPLE);
				if (itemSlot == -1) {
					itemSlot = findItemInventorySlot(Items.GOLDEN_APPLE);
				}
				if (itemSlot != -1) {
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
					EntityUtil.syncInventory();
					timer.reset();
				}
			}
			return;
		}
		if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.getValue()) {
			if (!mainHand.getValue() && crystal.getValue() && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
				int itemSlot = findItemInventorySlot(Items.END_CRYSTAL);
				if (itemSlot != -1) {
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
					EntityUtil.syncInventory();
					timer.reset();
				}
			}
			return;
		}
		if (mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING || mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
			return;
		}
		int itemSlot = findItemInventorySlot(Items.TOTEM_OF_UNDYING);
		if (itemSlot != -1) {
			if (mainHand.getValue()) {
				InventoryUtil.switchToSlot(0);
				if (mc.player.getInventory().getStack(0).getItem() != Items.TOTEM_OF_UNDYING) {
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
					EntityUtil.syncInventory();
				}
			} else {
				mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
				mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
				mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
				EntityUtil.syncInventory();
			}
			timer.reset();
		}
	}

	public static int findItemInventorySlot(Item item) {
		for (int i = 44; i >= 0; --i) {
			ItemStack stack = mc.player.getInventory().getStack(i);
			if (stack.getItem() == item) return i < 9 ? i + 36 : i;
		}
		return -1;
	}

}
