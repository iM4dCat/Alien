package dev.luminous.asm.mixins;

import dev.luminous.mod.modules.impl.render.Ambience;
import dev.luminous.mod.modules.impl.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {


	@Final
	@Shadow
	private  NativeImageBackedTexture texture;
	@Final
	@Shadow
	private  NativeImage image;
	@Shadow
	private boolean dirty;
	@Shadow
	private float flickerIntensity;
	@Final
	@Shadow
	private  GameRenderer renderer;
	@Final
	@Shadow
	private  MinecraftClient client;

	@Inject(method = "update", at = @At("HEAD"), cancellable = true)
	public void updateHook(float delta, CallbackInfo ci) {
		if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.worldColor.booleanValue) {
			ci.cancel();
			if (this.dirty) {
				this.dirty = false;
				this.client.getProfiler().push("lightTex");
				ClientWorld clientWorld = this.client.world;
				if (clientWorld != null) {
					float f = clientWorld.getSkyBrightness(1.0F);
					float g;
					if (clientWorld.getLightningTicksLeft() > 0) {
						g = 1.0F;
					} else {
						g = f * 0.95F + 0.05F;
					}

					float h = this.client.options.getDarknessEffectScale().getValue().floatValue();
					float i = this.getDarknessFactor(delta) * h;
					float j = this.getDarkness(this.client.player, i, delta) * h;
					float k = this.client.player.getUnderwaterVisibility();
					float l;
                    l = 1;

                    Vector3f vector3f = new Vector3f(f, f, 1.0F).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
					float m = this.flickerIntensity + 1.5F;
					Vector3f vector3f2 = new Vector3f();

					for (int n = 0; n < 16; ++n) {
						for (int o = 0; o < 16; ++o) {
							float p = getBrightness(clientWorld.getDimension(), n) * g;
							float q = getBrightness(clientWorld.getDimension(), o) * m;
							float s = q * ((q * 0.6F + 0.4F) * 0.6F + 0.4F);
							float t = q * (q * q * 0.6F + 0.4F);
							vector3f2.set(q, s, t);
							boolean bl = clientWorld.getDimensionEffects().shouldBrightenLighting();
							if (bl) {
								vector3f2.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
								clamp(vector3f2);
							} else {
								Vector3f vector3f3 = new Vector3f(vector3f).mul(p);
								vector3f2.add(vector3f3);
								vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
								if (this.renderer.getSkyDarkness(delta) > 0.0F) {
									float u = this.renderer.getSkyDarkness(delta);
									Vector3f vector3f4 = new Vector3f(vector3f2).mul(0.7F, 0.6F, 0.6F);
									vector3f2.lerp(vector3f4, u);
								}
							}

                            {
                                float v = Math.max(vector3f2.x(), Math.max(vector3f2.y(), vector3f2.z()));
                                if (v < 1.0F) {
                                    this.image.setColor(o, n, new Color(Ambience.INSTANCE.worldColor.getValue().getBlue(), Ambience.INSTANCE.worldColor.getValue().getGreen(), Ambience.INSTANCE.worldColor.getValue().getRed(), Ambience.INSTANCE.worldColor.getValue().getAlpha()).getRGB());
                                    continue;
                                }
                            }

							if (!bl) {
								if (j > 0.0F) {
									vector3f2.add(-j, -j, -j);
								}

								clamp(vector3f2);
							}

							float v = this.client.options.getGamma().getValue().floatValue();
							Vector3f vector3f5 = new Vector3f(this.easeOutQuart(vector3f2.x), this.easeOutQuart(vector3f2.y), this.easeOutQuart(vector3f2.z));
							vector3f2.lerp(vector3f5, Math.max(0.0F, v - i));
							vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
							clamp(vector3f2);
							vector3f2.mul(255.0F);
							int x = (int) vector3f2.x();
							int y = (int) vector3f2.y();
							int z = (int) vector3f2.z();
							this.image.setColor(o, n, 0xFF000000 | z << 16 | y << 8 | x);
						}
					}

					this.texture.upload();
					this.client.getProfiler().pop();
				}
			}
		}
	}

	@Shadow
	private static void clamp(Vector3f vec) {
		vec.set(MathHelper.clamp(vec.x, 0.0F, 1.0F), MathHelper.clamp(vec.y, 0.0F, 1.0F), MathHelper.clamp(vec.z, 0.0F, 1.0F));
	}
	
	@Inject(method = "getDarknessFactor(F)F", at = @At("HEAD"), cancellable = true)
	private void getDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> info) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.darkness.getValue()) info.setReturnValue(0.0f);
	}

	@Shadow
	private float easeOutQuart(float x) {
		float f = 1.0F - x;
		return 1.0F - f * f * f * f;
	}

	@Shadow
	public static float getBrightness(DimensionType type, int lightLevel) {
		float f = (float)lightLevel / 15.0F;
		float g = f / (4.0F - 3.0F * f);
		return MathHelper.lerp(type.ambientLight(), g, 1.0F);
	}

	@Shadow
	private float getDarknessFactor(float delta) {
		if (this.client.player.hasStatusEffect(StatusEffects.DARKNESS)) {
			StatusEffectInstance statusEffectInstance = this.client.player.getStatusEffect(StatusEffects.DARKNESS);
			if (statusEffectInstance != null && statusEffectInstance.getFactorCalculationData().isPresent()) {
				return statusEffectInstance.getFactorCalculationData().get().lerp(this.client.player, delta);
			}
		}

		return 0.0F;
	}
	@Shadow
	private float getDarkness(LivingEntity entity, float factor, float delta) {
		float f = 0.45F * factor;
		return Math.max(0.0F, MathHelper.cos(((float)entity.age - delta) * (float) Math.PI * 0.025F) * f);
	}
}
