package dev.luminous.mod.modules.impl.render;

import com.google.common.collect.Maps;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.DeathEvent;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.api.utils.render.Render3DUtil;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Map;
import java.util.UUID;

public class BlinkDetect extends Module {
    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
    private final BooleanSetting box = add(new BooleanSetting("Box", true));
    private final BooleanSetting outline = add(new BooleanSetting("Outline", true));
    private final BooleanSetting text = add(new BooleanSetting("Text", true));
    private final BooleanSetting message = add(new BooleanSetting("Message", true));

    private final Map<UUID, Data> playerCaches = Maps.newConcurrentMap();
    private final Map<UUID, Data> blinkCaches = Maps.newConcurrentMap();

    public BlinkDetect() {
        super("BlinkDetect", Category.Render);
        setChinese("瞬移检测");
    }

    @Override
    public void onEnable() {
        playerCaches.clear();
        blinkCaches.clear();
    }

    private final Timer timer = new Timer();

    @Override
    public void onLogin() {
        playerCaches.clear();
        blinkCaches.clear();
    }

    @EventHandler
    public void onDeath(DeathEvent event) {
        playerCaches.remove(event.getPlayer().getGameProfile().getId());
        blinkCaches.remove(event.getPlayer().getGameProfile().getId());
    }
    @Override
    public void onUpdate() {
        if (!timer.passedS(1)) return;
        timer.reset();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || player.equals(mc.player)) continue;
            Data playerCache = playerCaches.get(player.getGameProfile().getId());
            if (playerCache != null) {
                if (BlockUtil.distanceToXZ(playerCache.pos.x, playerCache.pos.z, player.getX(), player.getZ()) > 20) {
                    if (message.getValue()) CommandManager.sendChatMessage("§f" + player.getName().getString() + " §rTeleported to §f" + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ());
                    blinkCaches.put(player.getGameProfile().getId(), playerCache);
                }
            }
            playerCaches.put(player.getGameProfile().getId(), new Data(player, player.getPos()));
        }
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        for (Data data : blinkCaches.values()) {
            Render3DUtil.draw3DBox(matrixStack, getBoxAt(data.pos()), color.getValue(), outline.getValue(), box.getValue());
            if (text.getValue()) {
                Render3DUtil.drawText3D(data.player().getName().getString() + " Teleported", new Vec3d(data.pos.getX(), getBoxAt(data.pos).maxY + 0.5, data.pos.getZ()), ColorUtil.injectAlpha(color.getValue(), 255));
            }
        }
    }

    public Box getBoxAt(Vec3d vec3d) {
        double x = vec3d.getX();
        double y = vec3d.getY();
        double z = vec3d.getZ();
        float f = 0.6f / 2.0F;
        float g = 1.8f;
        return new Box(x - (double)f, y, z - (double)f, x + (double)f, y + (double)g, z + (double)f);
    }

    private record Data(PlayerEntity player, Vec3d pos) {
    }
}