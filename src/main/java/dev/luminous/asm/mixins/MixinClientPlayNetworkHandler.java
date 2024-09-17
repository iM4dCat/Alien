package dev.luminous.asm.mixins;

import dev.luminous.Alien;
import dev.luminous.api.events.impl.EntityVelocityUpdateEvent;
import dev.luminous.api.events.impl.GameLeftEvent;
import dev.luminous.api.events.impl.SendMessageEvent;
import dev.luminous.mod.modules.impl.client.ServerApply;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler {

    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "onEnterReconfiguration", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onEnterReconfiguration(EnterReconfigurationS2CPacket packet, CallbackInfo info) {
        Alien.EVENT_BUS.post(new GameLeftEvent());
    }

    @Shadow
    private ClientWorld world;

    @Unique
    private boolean alien$worldNotNull;

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void onGameJoinHead(GameJoinS2CPacket packet, CallbackInfo info) {
        alien$worldNotNull = world != null;
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info) {
        if (alien$worldNotNull) {
            Alien.EVENT_BUS.post(new GameLeftEvent());
        }
    }

    @Shadow
    public abstract void sendChatMessage(String content);

    @Unique
    private boolean ignore;

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (ignore) return;
        if (message.startsWith(Alien.PREFIX)) {
            Alien.COMMAND.command(message.split(" "));
            ci.cancel();
        } else {
            SendMessageEvent event = new SendMessageEvent(message);
            Alien.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            } else if (!event.message.equals(event.defaultMessage)) {
                ignore = true;
                sendChatMessage(event.message);
                ignore = false;
                ci.cancel();
            }
        }
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    public void test(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet,(ClientPlayNetworkHandler) (Object) this, this.client);
        Entity entity = this.world.getEntityById(packet.getId());
        if (entity != null) {
            if (entity == MinecraftClient.getInstance().player) {
                EntityVelocityUpdateEvent event = new EntityVelocityUpdateEvent();
                Alien.EVENT_BUS.post(event);
                if (!event.isCancelled()) {
                    entity.setVelocityClient((double) packet.getVelocityX() / 8000.0, (double) packet.getVelocityY() / 8000.0, (double) packet.getVelocityZ() / 8000.0);
                }
            } else {
                entity.setVelocityClient((double) packet.getVelocityX() / 8000.0, (double) packet.getVelocityY() / 8000.0, (double) packet.getVelocityZ() / 8000.0);
            }
        }
        ci.cancel();
    }
    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"), cancellable = true)
    public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        ci.cancel();
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, this.client);
        PlayerEntity playerEntity = this.client.player;
        Vec3d vec3d = playerEntity.getVelocity();
        boolean bl = packet.getFlags().contains(PositionFlag.X);
        boolean bl2 = packet.getFlags().contains(PositionFlag.Y);
        boolean bl3 = packet.getFlags().contains(PositionFlag.Z);
        double d;
        double e;
        if (bl) {
            d = vec3d.getX();
            e = playerEntity.getX() + packet.getX();
            playerEntity.lastRenderX += packet.getX();
            playerEntity.prevX += packet.getX();
        } else {
            d = 0.0;
            e = packet.getX();
            playerEntity.lastRenderX = e;
            playerEntity.prevX = e;
        }

        double f;
        double g;
        if (bl2) {
            f = vec3d.getY();
            g = playerEntity.getY() + packet.getY();
            playerEntity.lastRenderY += packet.getY();
            playerEntity.prevY += packet.getY();
        } else {
            f = 0.0;
            g = packet.getY();
            playerEntity.lastRenderY = g;
            playerEntity.prevY = g;
        }

        double h;
        double i;
        if (bl3) {
            h = vec3d.getZ();
            i = playerEntity.getZ() + packet.getZ();
            playerEntity.lastRenderZ += packet.getZ();
            playerEntity.prevZ += packet.getZ();
        } else {
            h = 0.0;
            i = packet.getZ();
            playerEntity.lastRenderZ = i;
            playerEntity.prevZ = i;
        }

        playerEntity.setPosition(e, g, i);
        playerEntity.setVelocity(d, f, h);
        if (ServerApply.INSTANCE.rotate.getValue()) {
            float j = packet.getYaw();
            float k = packet.getPitch();
            if (packet.getFlags().contains(PositionFlag.X_ROT)) {
                playerEntity.setPitch(playerEntity.getPitch() + k);
                playerEntity.prevPitch += k;
            } else {
                playerEntity.setPitch(k);
                playerEntity.prevPitch = k;
            }

            if (packet.getFlags().contains(PositionFlag.Y_ROT)) {
                playerEntity.setYaw(playerEntity.getYaw() + j);
                playerEntity.prevYaw += j;
            } else {
                playerEntity.setYaw(j);
                playerEntity.prevYaw = j;
            }

            this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
            this.connection
                    .send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYaw(), playerEntity.getPitch(), false));
        } else {
            if (ServerApply.INSTANCE.applyYaw.getValue()) {
                float j = packet.getYaw();
                float k = packet.getPitch();
                if (packet.getFlags().contains(PositionFlag.X_ROT)) {
                    k = (Alien.ROTATION.lastYaw + k);
                }

                if (packet.getFlags().contains(PositionFlag.Y_ROT)) {
                    j = (Alien.ROTATION.lastPitch + j);
                }
                this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                this.connection
                        .send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), j, k, false));
            } else {
                this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                this.connection
                        .send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), Alien.ROTATION.rotationYaw, Alien.ROTATION.rotationPitch, false));
            }
        }
    }
}
