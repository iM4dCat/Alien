package dev.luminous.asm.mixins;

import dev.luminous.Alien;
import dev.luminous.mod.modules.impl.player.InteractTweaks;
import dev.luminous.api.events.impl.ClickBlockEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

	@Shadow
	private ItemStack selectedStack;

	@ModifyVariable(method = "isCurrentlyBreaking", at = @At("STORE"))
	private ItemStack stack(ItemStack stack) {
		return InteractTweaks.INSTANCE.noReset() ? this.selectedStack : stack;
	}

	@ModifyConstant(method = "updateBlockBreakingProgress", constant = @Constant(intValue = 5))
	private int MiningCooldownFix(int value) {
		return InteractTweaks.INSTANCE.noDelay() ? 0 : value;
	}

	@Inject(method = "cancelBlockBreaking", at = @At("HEAD"), cancellable = true)
	private void hookCancelBlockBreaking(CallbackInfo callbackInfo) {
		if (InteractTweaks.INSTANCE.noAbort())
			callbackInfo.cancel();
	}
	@Inject(at = { @At("HEAD") }, method = { "getReachDistance()F" }, cancellable = true)
	private void onGetReachDistance(CallbackInfoReturnable<Float> ci) {
		if (InteractTweaks.INSTANCE.reach()) {
			ci.setReturnValue(InteractTweaks.INSTANCE.distance.getValueFloat());
		}
	}

	@Shadow
	private int lastSelectedSlot;
	@Final
	@Shadow
	private MinecraftClient client;
	@Final
	@Shadow
	private ClientPlayNetworkHandler networkHandler;

	@Inject(at = { @At("HEAD") }, method = { "syncSelectedSlot" }, cancellable = true)
	private void syncSlotHook(CallbackInfo ci) {
		ci.cancel();
		int i = this.client.player.getInventory().selectedSlot;
		if (i != Alien.SERVER.serverSideSlot) {
			this.lastSelectedSlot = i;
			this.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.lastSelectedSlot));
		}
	}
	@Inject(at = { @At("HEAD") }, method = { "hasExtendedReach()Z" }, cancellable = true)
	private void hasExtendedReach(CallbackInfoReturnable<Boolean> cir) {
		if (InteractTweaks.INSTANCE.reach())
			cir.setReturnValue(true);
	}

	@Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
	private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		ClickBlockEvent event = new ClickBlockEvent(pos, direction);
		Alien.EVENT_BUS.post(event);
		if (event.isCancelled()) {
			cir.setReturnValue(false);
		}
	}
}
