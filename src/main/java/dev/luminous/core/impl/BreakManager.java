package dev.luminous.core.impl;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.api.utils.Wrapper;
import dev.luminous.api.utils.math.FadeUtils;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.modules.impl.player.PacketMine;
import dev.luminous.mod.modules.impl.render.BreakESP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.concurrent.ConcurrentHashMap;

public class BreakManager implements Wrapper {
    public BreakManager() {
        Alien.EVENT_BUS.subscribe(this);
    }
    public final ConcurrentHashMap<Integer, BreakData> breakMap = new ConcurrentHashMap<>();

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof BlockBreakingProgressS2CPacket packet) {
            if (packet.getPos() == null) return;
            BreakData breakData = new BreakData(packet.getPos(), packet.getEntityId());
            if (breakMap.containsKey(packet.getEntityId()) && breakMap.get(packet.getEntityId()).pos.equals(packet.getPos())) {
                return;
            }
            if (breakData.getEntity() == null) {
                return;
            }
            if (MathHelper.sqrt((float) breakData.getEntity().getEyePos().squaredDistanceTo(packet.getPos().toCenterPos())) > 8) {
                return;
            }
            breakMap.put(packet.getEntityId(), breakData);
        }
    }

    public boolean isMining(BlockPos pos) {
        return isMining(pos, true);
    }
    public boolean isMining(BlockPos pos, boolean self) {
        if (self && PacketMine.getBreakPos() != null && PacketMine.getBreakPos().equals(pos)) {
            return true;
        }

        for (BreakData breakData : breakMap.values()) {
            if (breakData.getEntity() == null) {
                continue;
            }
            if (breakData.getEntity().getEyePos().distanceTo(pos.toCenterPos()) > 7) {
                continue;
            }
            if (breakData.pos.equals(pos)) {
                return true;
            }
        }

        return false;
    }
    public static class BreakData {
        public final BlockPos pos;
        public final int entityId;
        public final FadeUtils fade;
        public final Timer timer;
        public BreakData(BlockPos pos, int entityId) {
            this.pos = pos;
            this.entityId = entityId;
            this.fade = new FadeUtils((long) BreakESP.INSTANCE.animationTime.getValue());
            this.timer = new Timer();
        }

        public Entity getEntity() {
            if (mc.world == null) return null;
            Entity entity = mc.world.getEntityById(entityId);
            if (entity instanceof PlayerEntity) {
                return entity;
            }
            return null;
        }
    }
}
