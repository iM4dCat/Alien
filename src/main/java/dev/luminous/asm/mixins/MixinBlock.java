package dev.luminous.asm.mixins;

import dev.luminous.mod.modules.impl.movement.NoSlow;
import dev.luminous.mod.modules.impl.render.XRay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class MixinBlock implements ItemConvertible {
	@Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
	private static void shouldDrawSideHook(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
		if (XRay.INSTANCE.isOn())
			cir.setReturnValue(XRay.INSTANCE.isCheckableOre(state.getBlock()));
	}

	@Inject(method = "isTransparent", at = @At("HEAD"), cancellable = true)
	public void isTransparentHook(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (MinecraftClient.getInstance() == null) return;
		if (XRay.INSTANCE.isOn())
			cir.setReturnValue(!XRay.INSTANCE.isCheckableOre(state.getBlock()));
	}

	@Inject(at = { @At("HEAD") }, method = { "getVelocityMultiplier()F" }, cancellable = true)
	private void onGetVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
		if (NoSlow.INSTANCE.soulSand()) {
			if (cir.getReturnValueF() < 1.0f)
				cir.setReturnValue(1F);
		}
	}

}
