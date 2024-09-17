package dev.luminous.api.utils.math;

import dev.luminous.Alien;
import dev.luminous.core.impl.PlayerManager;
import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.api.utils.Wrapper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

import java.util.ArrayList;

public class ExplosionUtil implements Wrapper {
    public static float anchorDamage(BlockPos pos, PlayerEntity target, PlayerEntity predict){
        if (BlockUtil.getBlock(pos) == Blocks.RESPAWN_ANCHOR) {
            CombatUtil.modifyPos = pos;
            CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
            float damage = calculateDamage(pos.toCenterPos().getX(), pos.toCenterPos().getY(), pos.toCenterPos().getZ(), target, predict, 5);
            CombatUtil.modifyPos = null;
            return damage;
        } else {
            return calculateDamage(pos.toCenterPos().getX(), pos.toCenterPos().getY(), pos.toCenterPos().getZ(), target, predict, 5);
        }
    }
    public static float calculateDamage(double posX, double posY, double posZ, LivingEntity entity, Entity predict, float power) {
        if (entity instanceof PlayerEntity player && player.getAbilities().creativeMode) return 0;
        if (predict == null) predict = entity;
        float doubleExplosionSize = 2 * power;
        double distancedsize = MathHelper.sqrt((float) predict.squaredDistanceTo(posX, posY, posZ)) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = getExposure(vec3d, predict);
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (int) ((v * v + v) / 2.0 * 7.0 * (double) doubleExplosionSize + 1.0);
        double finald = getBlastReduction(entity, getDamageMultiplied(damage));
        return (float) finald;
    }
    public static float getDamageAfterAbsorb(float damage, float totalArmor, float toughnessAttribute)
    {
        float f = 2.0F + toughnessAttribute / 4.0F;
        float f1 = MathHelper.clamp(totalArmor - damage / f, totalArmor * 0.2F, 20.0F);
        return damage * (1.0F - f1 / 25.0F);
    }

    public static float getBlastReduction(LivingEntity entity, float damageI) {
        float damage = damageI;
        if (entity instanceof PlayerEntity player) {
            PlayerManager.EntityAttribute a = Alien.PLAYER.map.get(player);
            if (a == null) return 0;
            damage = getDamageAfterAbsorb(damage, a.armor(), (float) a.toughness());
            int k = getProtectionAmount(player.getArmorItems());
            float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
/*            if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
                int lvl = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1);
                damage *= (float) (1 - (lvl * 0.2));
            }*/
            if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
                damage -= damage / 4.0f;
            }
            return Math.max(damage, 0.0f);
        }
        damage = getDamageAfterAbsorb(damage, (float) entity.getArmor(), (float) entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
        return Math.max(damage, 0.0f);
    }

    public static float getExposure(Vec3d source, Entity entity) {
        Box box = entity.getBoundingBox();

        int miss = 0;
        int hit = 0;

        for(int k = 0; k <= 1; k += 1) {
            for(int l = 0; l <= 1; l += 1) {
                for(int m = 0; m <= 1; m += 1) {
                    double n = MathHelper.lerp(k, box.minX, box.maxX);
                    double o = MathHelper.lerp(l, box.minY, box.maxY);
                    double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                    Vec3d vec3d = new Vec3d(n, o, p);
                    if (raycast(vec3d, source) == HitResult.Type.MISS)
                        ++miss;
                    ++hit;
                }
            }
        }
        return (float)miss / (float)hit;
    }

    private static HitResult.Type raycast(Vec3d start, Vec3d end) {
        return BlockView.raycast(start, end, null, (_null, blockPos) -> {
            BlockState blockState = mc.world.getBlockState(blockPos);
            if (blockState.getBlock().getBlastResistance() < 600) return null;

            BlockHitResult hitResult = blockState.getCollisionShape(mc.world, blockPos).raycast(start, end, blockPos);
            return hitResult == null ? null : hitResult.getType();
        }, (_null) -> HitResult.Type.MISS);
    }
    public static int getProtectionAmount(Iterable<ItemStack> armorItems) {
        int value = 0;
        for (ItemStack itemStack : new ArrayList<>((DefaultedList<ItemStack>) armorItems)) {
            int level = EnchantmentHelper.getLevel(Enchantments.PROTECTION, itemStack);
            if (level == 0) {
                value += EnchantmentHelper.getLevel(Enchantments.BLAST_PROTECTION, itemStack) * 2;
            } else {
                value += level;
            }
        }
        return value;
    }

    public static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0f : (diff == 2 ? 1.0f : (diff == 1 ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(BlockPos pos, LivingEntity target) {
        return calculateDamage(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, target, target, 6);
    }
}
