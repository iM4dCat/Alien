package dev.luminous.mod.modules.impl.movement;

import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import io.netty.util.internal.ConcurrentSet;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.MoveEvent;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.api.events.impl.UpdateWalkingPlayerEvent;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.mod.modules.Module;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketFly extends Module {
    public final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Factor));
    public final SliderSetting factor = add(new SliderSetting("Factor", 1.0f, 0.0f, 10.0f));
    public final EnumSetting<Phase> phase = add(new EnumSetting<>("Phase", Phase.Full));
    public final EnumSetting<Type> type = add(new EnumSetting<>("Type", Type.Up));
    public final BooleanSetting antiKick = add(new BooleanSetting("AntiKick", true));
    public final BooleanSetting noRotation = add(new BooleanSetting("NoRotation", false));
    public final BooleanSetting noMovePacket = add(new BooleanSetting("NoMovePacket", false));
    public final BooleanSetting bbOffset = add(new BooleanSetting("BB-Offset", false));
    public final SliderSetting invalidY = add(new SliderSetting("Invalid-Offset", 1337, 0, 1337));
    public final SliderSetting invalids = add(new SliderSetting("Invalids", 1, 0, 10));
    public final SliderSetting sendTeleport = add(new SliderSetting("Teleport", 1, 0, 10));
    public final SliderSetting concealY = add(new SliderSetting("C-Y", 0.0, -256.0, 256.0));
    public final SliderSetting conceal = add(new SliderSetting("C-Multiplier", 1.0, 0.0, 2.0));
    public final SliderSetting ySpeed = add(new SliderSetting("Y-Multiplier", 1.0, 0.0, 2.0));
    public final SliderSetting xzSpeed = add(new SliderSetting("X/Z-Multiplier", 1.0, 0.0, 2.0));
    public final BooleanSetting elytra = add(new BooleanSetting("Elytra", false));
    public final BooleanSetting xzJitter = add(new BooleanSetting("Jitter-XZ", false));
    public final BooleanSetting yJitter = add(new BooleanSetting("Jitter-Y", false));
    public final BooleanSetting zeroSpeed = add(new BooleanSetting("Zero-Speed", false));
    public final BooleanSetting zeroY = add(new BooleanSetting("Zero-Y", false));
    public final BooleanSetting zeroTeleport = add(new BooleanSetting("Zero-Teleport", true));
    public final SliderSetting zoomer = add(new SliderSetting("Zoomies", 3, 0, 10));

    public final Map<Integer, TimeVec> posLooks = new ConcurrentHashMap<>();
    public final Set<Packet<?>> playerPackets = new ConcurrentSet<>();
    public final AtomicInteger teleportID = new AtomicInteger();
    public Vec3d vecDelServer;
    public int packetCounter;
    public boolean zoomies;
    public float lastFactor;
    public int zoomTimer = 0;

    public PacketFly() {
        super("PacketFly", Category.Movement);
        setChinese("发包飞行");
    }


    @Override
    public void onLogin() {
        disable();
        clearValues();
    }

    @Override
    public void onUpdate() {
        posLooks.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().getTime() > TimeUnit.SECONDS.toMillis(30L));
    }

    @EventHandler
    public void invoke(UpdateWalkingPlayerEvent event) {
        if (event.isPre() && this.mode.getValue() != Mode.Compatibility) {
            MovementUtil.setMotionX(0);
            MovementUtil.setMotionY(0);
            MovementUtil.setMotionZ(0);

            if (this.mode.getValue() != Mode.Setback && this.teleportID.get() == 0) {
                if (this.checkPackets(6)) {
                    this.sendPackets(0.0, 0.0, 0.0, true);
                }

                return;
            }

            boolean isPhasing = this.isPlayerCollisionBoundingBoxEmpty();
            double ySpeed;

            if (mc.player.input.jumping && (isPhasing || !MovementUtil.isMoving())) {
                if (this.antiKick.getValue() && !isPhasing) {
                    ySpeed = this.checkPackets(this.mode.getValue() == Mode.Setback ? 10 : 20) ? -0.032 : 0.062;
                } else {
                    ySpeed = this.yJitter.getValue() && this.zoomies ? 0.061 : 0.062;
                }
            } else if (mc.player.input.sneaking) {
                ySpeed = this.yJitter.getValue() && this.zoomies ? -0.061 : -0.062;
            } else {
                ySpeed = !isPhasing ? (this.checkPackets(4) ? (this.antiKick.getValue() ? -0.04 : 0.0) : 0.0) : 0.0;
            }

            if (this.phase.getValue() == Phase.Full && isPhasing && MovementUtil.isMoving() && ySpeed != 0.0) {
                ySpeed /= 2.5;
            }

            double high = this.xzJitter.getValue() && this.zoomies ? 0.25 : 0.26;
            double low = this.xzJitter.getValue() && this.zoomies ? 0.030 : 0.031;

            double[] dirSpeed = MovementUtil.directionSpeed(this.phase.getValue() == Phase.Full && isPhasing ? low : high);

            if (this.mode.getValue() == Mode.Increment) {
                if (this.lastFactor >= this.factor.getValue()) {
                    this.lastFactor = 1.0f;
                } else if (++this.lastFactor > this.factor.getValue()) {
                    this.lastFactor = this.factor.getValueFloat();
                }
            } else {
                this.lastFactor = this.factor.getValueFloat();
            }

            for (int i = 1; i <= (this.mode.getValue() == Mode.Factor || this.mode.getValue() == Mode.Slow || this.mode.getValue() == Mode.Increment ? this.lastFactor : 1); i++) {
                double conceal = mc.player.getY() < this.concealY.getValue() && MovementUtil.isMoving() ? this.conceal.getValue() : 1.0;

                MovementUtil.setMotionX(dirSpeed[0] * i * conceal * this.xzSpeed.getValue());
                MovementUtil.setMotionY(ySpeed * i * this.ySpeed.getValue());
                MovementUtil.setMotionZ(dirSpeed[1] * i * conceal * this.xzSpeed.getValue());
                this.sendPackets(MovementUtil.getMotionX(), MovementUtil.getMotionY(), MovementUtil.getMotionZ(), this.mode.getValue() != Mode.Setback);
            }

            this.zoomTimer++;
            if (this.zoomTimer > this.zoomer.getValue()) {
                this.zoomies = !this.zoomies;
                this.zoomTimer = 0;
            }
        }
    }

    @EventHandler
    public void invoke(PacketEvent.Receive event) {
        if (nullCheck()) return;
        if (this.mode.getValue() == Mode.Compatibility) {
            return;
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {

            if (mc.player.isAlive() && this.mode.getValue() != Mode.Setback && this.mode.getValue() != Mode.Slow && !(mc.currentScreen instanceof DownloadingTerrainScreen)) {
                TimeVec vec = this.posLooks.remove(packet.getTeleportId());
                if (vec != null && vec.x == packet.getX() && vec.y == packet.getY() && vec.z == packet.getZ()) {
                    event.setCancelled(true);
                    return;
                }
            }

            this.teleportID.set(packet.getTeleportId());
        }
    }

    @EventHandler
    public void invoke(MoveEvent event) {
        if (this.phase.getValue() == Phase.Semi || this.isPlayerCollisionBoundingBoxEmpty()) {
            mc.player.noClip = true;
        }
        if (this.mode.getValue() != Mode.Compatibility && (this.mode.getValue() == Mode.Setback || this.teleportID.get() != 0)) {
            if (this.zeroSpeed.getValue()) {
                event.setX(0.0);
                event.setY(0.0);
                event.setZ(0.0);
            } else {
                event.setX(MovementUtil.getMotionX());
                event.setY(MovementUtil.getMotionY());
                event.setZ(MovementUtil.getMotionZ());
            }

            if (this.zeroY.getValue()) {
                event.setY(0.0);
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (mode.getValue() != Mode.Compatibility && !playerPackets.remove(event.getPacket())) {
                if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround && !noRotation.getValue()) return;
                if (!noMovePacket.getValue()) return;
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onEnable() {
        clearValues();
        if (mc.player == null) {
            this.disable();
        }

        // teleportID.set(Managers.POSITION.getTeleportID());
    }

    @Override
    public String getInfo() {
        return mode.getValue().toString();
    }

    public void clearValues() {
        lastFactor = 1.0f;
        packetCounter = 0;
        teleportID.set(0);
        playerPackets.clear();
        posLooks.clear();
        vecDelServer = null;
    }

    public boolean isPlayerCollisionBoundingBoxEmpty() {
        double o = bbOffset.getValue() ? -0.0625 : 0;
        return mc.world.canCollide(mc.player, mc.player.getBoundingBox().expand(o, o, o));
    }

    public boolean checkPackets(int amount) {
        if (++this.packetCounter >= amount) {
            this.packetCounter = 0;
            return true;
        }

        return false;
    }

    public void sendPackets(double x, double y, double z, boolean confirm) {
        Vec3d offset = new Vec3d(x, y, z);
        Vec3d vec = mc.player.getPos().add(offset);
        vecDelServer = vec;
        Vec3d oOB = type.getValue().createOutOfBounds(vec, invalidY.getValueInt());

        sendCPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, mc.player.isOnGround()));

        if (!mc.isInSingleplayer()) {
            for (int i = 0; i < invalids.getValue(); i++) {
                sendCPacket(new PlayerMoveC2SPacket.PositionAndOnGround(oOB.x, oOB.y, oOB.z, mc.player.isOnGround()));
                oOB = type.getValue().createOutOfBounds(oOB, invalidY.getValueInt());
            }
        }

        if (confirm && (zeroTeleport.getValue() || teleportID.get() != 0)) {
            for (int i = 0; i < sendTeleport.getValue(); i++) {
                sendConfirmTeleport(vec);
            }
        }

        if (elytra.getValue()) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    public void sendConfirmTeleport(Vec3d vec) {
        int id = teleportID.incrementAndGet();
        mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(id));
        posLooks.put(id, new TimeVec(vec));
    }

    public void sendCPacket(Packet<?> packet) {
        playerPackets.add(packet);
        mc.getNetworkHandler().sendPacket(packet);
    }

    public enum Mode {
        Setback, Fast, Factor, Slow, Increment, Compatibility
    }

    public enum Phase {
        Off, Semi, Full
    }

    public enum Type {
        Down() {
            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add(0, -invalid, 0);
            }
        }, Up() {
            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add(0, invalid, 0);
            }
        }, Preserve() {
            private final Random random = new Random();

            private int randomInt() {
                int result = random.nextInt(29000000);
                if (random.nextBoolean()) {
                    return result;
                }

                return -result;
            }

            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add(randomInt(), 0, randomInt());
            }
        }, Switch() {
            private final Random random = new Random();

            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                boolean down = random.nextBoolean();
                return down ? vec3d.add(0, -invalid, 0) : vec3d.add(0, invalid, 0);
            }
        }, X() {
            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add(invalid, 0, 0);
            }
        }, Z() {
            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add(0, 0, invalid);
            }
        }, XZ() {
            @Override
            public Vec3d createOutOfBounds(Vec3d vec3d, int invalid) {
                return vec3d.add(invalid, 0, invalid);
            }
        };

        public abstract Vec3d createOutOfBounds(Vec3d vec3d, int invalid);

    }

    public static class TimeVec extends Vec3d {
        private final long time;

        public TimeVec(Vec3d vec3d) {
            this(vec3d.x, vec3d.y, vec3d.z, System.currentTimeMillis());
        }

        public TimeVec(double xIn, double yIn, double zIn, long time) {
            super(xIn, yIn, zIn);
            this.time = time;
        }

        public long getTime() {
            return time;
        }

    }

}
