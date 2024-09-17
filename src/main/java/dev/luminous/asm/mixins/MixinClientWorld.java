package dev.luminous.asm.mixins;

import dev.luminous.Alien;
import dev.luminous.api.events.impl.EntitySpawnEvent;
import dev.luminous.mod.modules.impl.render.Ambience;
import dev.luminous.mod.modules.impl.render.NoRender;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld extends World {
    protected MixinClientWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    public void onAddEntity(Entity entity, CallbackInfo ci) {
        EntitySpawnEvent event = new EntitySpawnEvent(entity);
        Alien.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> info) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.sky.booleanValue) {
            Color sky = Ambience.INSTANCE.sky.getValue();
            info.setReturnValue(new Vec3d(sky.getRed() / 255.0, sky.getGreen() / 255.0, sky.getBlue() / 255.0));
        }
    }
    @Inject(method = "getCloudsColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetCloudsColor(float tickDelta,
                                    CallbackInfoReturnable<Vec3d> cir) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.cloud.booleanValue) {
            Color sky = Ambience.INSTANCE.cloud.getValue();
            cir.setReturnValue(new Vec3d(sky.getRed() / 255.0, sky.getGreen() / 255.0, sky.getBlue() / 255.0));
        }
    }
    @Unique
    private final DimensionEffects overworld = new DimensionEffects.Overworld();

    @Inject(method = "getDimensionEffects", at = @At("HEAD"), cancellable = true)
    private void onGetSkyProperties(CallbackInfoReturnable<DimensionEffects> info) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.forceOverworld.getValue()) {
            info.setReturnValue(overworld);
        }

    }
    @Override
    public float getRainGradient(float delta) {
        return NoRender.INSTANCE.isOn() && NoRender.INSTANCE.weather.getValue() ? 0 : super.getRainGradient(delta);
    }

    @Override
    public float getThunderGradient(float delta) {
        return NoRender.INSTANCE.isOn() && NoRender.INSTANCE.weather.getValue() ? 0 : super.getThunderGradient(delta);
    }

}
