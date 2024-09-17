package dev.luminous.core.impl;

import dev.luminous.api.utils.Wrapper;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.world.BlockPosX;
import dev.luminous.mod.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerManager implements Wrapper {

    public Map<PlayerEntity, EntityAttribute> map = new ConcurrentHashMap<>();
    public CopyOnWriteArrayList<PlayerEntity> inWebPlayers = new CopyOnWriteArrayList<>();
    public boolean insideBlock = false;

    public void onUpdate() {
        if (Module.nullCheck()) return;
        inWebPlayers.clear();
        insideBlock = EntityUtil.isInsideBlock();
        for (PlayerEntity player : new ArrayList<>(mc.world.getPlayers())) {
            map.put(player, new EntityAttribute(player.getArmor(), player.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)));
            webUpdate(player);
        }
    }

    public boolean isInWeb(PlayerEntity player) {
        return inWebPlayers.contains(player);
    }
    private void webUpdate(PlayerEntity player) {
        for (float x : new float[]{0, 0.3F, -0.3f}) {
            for (float z : new float[]{0, 0.3F, -0.3f}) {
                for (int y : new int[]{-1, 0, 1, 2}) {
                    BlockPos pos = new BlockPosX(player.getX() + x, player.getY(), player.getZ() + z).up(y);
                    if (new Box(pos).intersects(player.getBoundingBox()) && mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        inWebPlayers.add(player);
                        return;
                    }
                }
            }
        }
    }

    public record EntityAttribute(int armor, double toughness) {
    }
}
