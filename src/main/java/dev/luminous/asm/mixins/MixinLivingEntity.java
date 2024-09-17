package dev.luminous.asm.mixins;

import dev.luminous.Alien;
import dev.luminous.api.events.Event;
import dev.luminous.api.events.impl.SprintEvent;
import dev.luminous.mod.modules.impl.exploit.NoBadEffects;
import dev.luminous.mod.modules.impl.movement.ElytraFly;
import dev.luminous.mod.modules.impl.movement.Glide;
import dev.luminous.mod.modules.impl.render.ViewModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "tickMovement",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"),
            require = 0)
    private boolean tickMovementHook(LivingEntity instance, StatusEffect effect) {
        if (Glide.INSTANCE != null && Glide.INSTANCE.isOn() && Glide.INSTANCE.onlyFall.getValue())
            return false;
        return instance.hasStatusEffect(effect);
    }
    @Final
    @Shadow
    private static EntityAttributeModifier SPRINTING_SPEED_BOOST;

    @Shadow
    @Nullable
    public EntityAttributeInstance getAttributeInstance(EntityAttribute attribute) {
        return this.getAttributes().getCustomInstance(attribute);
    }

    @Shadow
    public AttributeContainer getAttributes() {
        return null;
    }

    @Shadow public abstract void remove(RemovalReason reason);

    @Inject(method = {"getHandSwingDuration"}, at = {@At("HEAD")}, cancellable = true)
    private void getArmSwingAnimationEnd(final CallbackInfoReturnable<Integer> info) {
        if (ViewModel.INSTANCE.isOn() && ViewModel.INSTANCE.slowAnimation.getValue())
            info.setReturnValue(ViewModel.INSTANCE.slowAnimationVal.getValueInt());
    }

    @Unique
    private boolean previousElytra = false;
    @Inject(method = "isFallFlying", at = @At("TAIL"), cancellable = true)
    public void recastOnLand(CallbackInfoReturnable<Boolean> cir) {
        boolean elytra = cir.getReturnValue();
        if (previousElytra && !elytra && ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.mode.is(ElytraFly.Mode.Bounce)) {
            cir.setReturnValue(ElytraFly.recastElytra(MinecraftClient.getInstance().player));
        }
        previousElytra = elytra;
    }

    @Redirect(method = "travel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"),
            require = 0)
    private boolean travelEffectHook(LivingEntity instance, StatusEffect effect) {
        if (NoBadEffects.INSTANCE.isOn()) {
            if (effect == StatusEffects.SLOW_FALLING && NoBadEffects.INSTANCE.slowFalling.getValue()) {
                return false;
            }
            if (effect == StatusEffects.LEVITATION && NoBadEffects.INSTANCE.levitation.getValue()) {
                return false;
            }
        }
        return instance.hasStatusEffect(effect);
    }
    @Inject(method = {"setSprinting"}, at = {@At("HEAD")}, cancellable = true)
    public void setSprintingHook(boolean sprinting, CallbackInfo ci) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            SprintEvent event = new SprintEvent(Event.Stage.Pre);
            Alien.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
                sprinting = event.isSprint();
                super.setSprinting(sprinting);
                EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                entityAttributeInstance.removeModifier(SPRINTING_SPEED_BOOST.getId());
                if (sprinting) {
                    entityAttributeInstance.addTemporaryModifier(SPRINTING_SPEED_BOOST);
                }
            }
        }
    }
}
