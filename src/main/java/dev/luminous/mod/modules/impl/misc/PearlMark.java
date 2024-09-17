package dev.luminous.mod.modules.impl.misc;

import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.EntitySpawnEvent;
import dev.luminous.mod.modules.Module;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

public class PearlMark extends Module {

    public PearlMark() {
        super("PearlMark", Category.Misc);
        setChinese("珍珠标记");
    }

    @EventHandler
    public void onReceivePacket(EntitySpawnEvent event) {
        if (nullCheck()) return;
        if (event.getEntity() instanceof EnderPearlEntity pearl) {
            mc.world.getPlayers().stream().min(Comparator.comparingDouble((p) -> p.getPos().distanceTo(new Vec3d(pearl.getX(), pearl.getY(), pearl.getZ())))).ifPresent((player) -> {
                pearl.setCustomName(player.getName());
                pearl.setCustomNameVisible(true);
            });
        }
    }

}