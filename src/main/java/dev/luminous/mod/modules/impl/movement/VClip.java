package dev.luminous.mod.modules.impl.movement;

import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class VClip extends Module {
    public VClip() {
        super("VClip", Category.Movement);
        setChinese("纵向穿墙");
    }

    final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Jump));

    public enum Mode {
        Glitch,
        Teleport,
        Jump
    }

    @Override
    public void onUpdate() {
        disable();
        switch (mode.getValue()) {
            case Teleport -> {
                mc.player.setPosition(mc.player.getX(), mc.player.getY() + 3, mc.player.getZ());
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
            }
            case Jump -> {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.4199999868869781, mc.player.getZ(), false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.7531999805212017, mc.player.getZ(), false));
                //mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.9999957640154541, mc.player.getZ(), false));
                mc.player.setPosition(mc.player.getX(), mc.player.getY() + 1, mc.player.getZ());
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
            }
            case Glitch -> {
                double posX = mc.player.getX();
                double posY = Math.round(mc.player.getY());
                double posZ = mc.player.getZ();
                boolean onGround = mc.player.isOnGround();

                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(posX,
                        posY,
                        posZ,
                        onGround));

                double halfY = 2 / 400.0;
                posY -= halfY;

                mc.player.setPosition(posX, posY, posZ);
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(posX,
                        posY,
                        posZ,
                        onGround));

                posY -= halfY * 300.0;
                mc.player.setPosition(posX, posY, posZ);
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(posX,
                        posY,
                        posZ,
                        onGround));
            }
        }
    }
}
