package dev.luminous.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.mod.modules.impl.render.Ambience;
import dev.luminous.mod.modules.impl.render.NoRender;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {
    @Inject(method = "applyFog", at = @At("TAIL"))
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo info) {
        if (Ambience.INSTANCE.isOn()) {
            if (Ambience.INSTANCE.fog.booleanValue) {
                RenderSystem.setShaderFogColor(Ambience.INSTANCE.fog.getValue().getRed() / 255f, Ambience.INSTANCE.fog.getValue().getGreen() / 255f, Ambience.INSTANCE.fog.getValue().getBlue() / 255f, Ambience.INSTANCE.fog.getValue().getAlpha() / 255f);
            }
            if (Ambience.INSTANCE.fogDistance.getValue()) {
                RenderSystem.setShaderFogStart(Ambience.INSTANCE.fogStart.getValueFloat());
                RenderSystem.setShaderFogEnd(Ambience.INSTANCE.fogEnd.getValueFloat());
            }
        }
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.fog.getValue()) {
            if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN) {
                RenderSystem.setShaderFogStart(viewDistance * 4);
                RenderSystem.setShaderFogEnd(viewDistance * 4.25f);
            }
        }
    }

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private static void hookRender(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.dimensionColor.booleanValue) {
            Color color = Ambience.INSTANCE.dimensionColor.getValue();
            ci.cancel();
            RenderSystem.clearColor((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f,
                    (float) color.getBlue() / 255.0f, 0.0f);
        }
    }

    @Inject(method = "getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;", at = @At("HEAD"), cancellable = true)
    private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<Object> info) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.blindness.getValue()) info.setReturnValue(null);
    }
}
