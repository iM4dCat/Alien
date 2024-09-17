package dev.luminous.asm.mixins;

import dev.luminous.Alien;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import dev.luminous.mod.modules.impl.player.freelook.CameraState;
import dev.luminous.mod.modules.impl.player.freelook.FreeLook;
import dev.luminous.mod.modules.impl.player.freelook.ProjectionUtils;
import dev.luminous.mod.modules.impl.render.Crosshair;
import dev.luminous.mod.modules.impl.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public class MixinInGameHud {


	@Shadow @Final private MinecraftClient client;

	@Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
	private void onRenderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.portal.getValue()) ci.cancel();
	}

	@Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
	private void onRenderStatusEffectOverlay(DrawContext context, CallbackInfo ci) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.potionsIcon.getValue()) ci.cancel();
	}

	@Inject(at = {@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", remap = false, ordinal = 3)}, method = {"render(Lnet/minecraft/client/gui/DrawContext;F)V"})
	private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
		Alien.MODULE.render2D(context);
	}

	@Inject(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"), cancellable = true)
	private void onClear(CallbackInfo info) {
		if (ClientSetting.INSTANCE.isOn() && ClientSetting.INSTANCE.keepHistory.getValue()) {
			info.cancel();
		}
	}

    @ModifyArg(method="renderHotbar",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",ordinal = 1),index = 1)
	private int selectedSlotX(int x){
		if (ClientSetting.INSTANCE.hotbar()) {
            double hotbarX = ClientSetting.animation.get(x, ClientSetting.INSTANCE.hotbarTime.getValueInt(), ClientSetting.INSTANCE.animEase.getValue());
			return (int) hotbarX;
		}
		return(x);
	}

	@Unique
	private CameraState camera;

	@Unique
	private double offsetCrosshairX;
	@Unique
	private double offsetCrosshairY;

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void onRenderCrosshairBegin(DrawContext context, CallbackInfo ci) {
		if (Crosshair.INSTANCE.isOn()) {
			Crosshair.INSTANCE.draw(context);
			ci.cancel();
			return;
		}
		camera = FreeLook.INSTANCE.getCameraState();

		var shouldDrawCrosshair = false;

		if (camera.doTransition || camera.doLock) {
			var cameraEntity = MinecraftClient.getInstance().getCameraEntity();

			var distance = Integer.MAX_VALUE;
			var position = cameraEntity.getPos();

			var rotation = Vec3d.fromPolar(camera.originalPitch(), camera.originalYaw());

			var point = position.add(
					rotation.getX() * distance,
					rotation.getY() * distance,
					rotation.getZ() * distance
			);

			var projected = ProjectionUtils.worldToScreen(point);

			if (projected.getZ() < 0) {
				offsetCrosshairX = -projected.getX();
				offsetCrosshairY = -projected.getY();
				shouldDrawCrosshair = true;
			}

			shouldDrawCrosshair |= MinecraftClient.getInstance().inGameHud.getDebugHud().shouldShowDebugHud();

			if (!shouldDrawCrosshair)
				ci.cancel();
		}
	}

	@ModifyArgs(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
	private void modifyDrawTextureArgs(Args args) {
		if (camera.doTransition || camera.doLock) {
			args.set(1, args.<Integer>get(1) + (int) offsetCrosshairX);
			args.set(2, args.<Integer>get(2) + (int) offsetCrosshairY);
		}
	}
}