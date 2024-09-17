package dev.luminous.mod.modules.impl.movement;

import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.eventbus.EventPriority;
import dev.luminous.api.events.impl.*;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.player.OffFirework;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static dev.luminous.api.utils.entity.MovementUtil.*;

public class ElytraFly extends Module {
    public static ElytraFly INSTANCE;
    public final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Control));
    private final BooleanSetting autoStop = add(new BooleanSetting("AutoStop", true));
    private final BooleanSetting sprint = add(new BooleanSetting("Sprint", true, () -> mode.is(Mode.Bounce)));
    public final BooleanSetting autoJump = add(new BooleanSetting("AutoJump", true, () -> mode.is(Mode.Bounce)));
    private final SliderSetting pitch =
            add(new SliderSetting("Pitch", 88, -90, 90, .1, () -> mode.is(Mode.Bounce)));
    private final BooleanSetting instantFly = add(new BooleanSetting("AutoStart", true, () -> !mode.is(Mode.Bounce)));
    private final BooleanSetting firework = add(new BooleanSetting("Firework", false, () -> !mode.is(Mode.Bounce)));
    private final SliderSetting delay =
            add(new SliderSetting("Delay", 1000, 0, 20000, 50, () -> !mode.is(Mode.Bounce)));
    private final SliderSetting timeout = add(new SliderSetting("Timeout", 0.5F, 0.1F, 1F, () -> !mode.is(Mode.Bounce)));
    public final SliderSetting upPitch = add(new SliderSetting("UpPitch", 0.0f, 0.0f, 90.0f, () -> mode.getValue() == Mode.Control));
    public final SliderSetting upFactor = add(new SliderSetting("UpFactor", 1.0f, 0.0f, 10.0f, () -> mode.getValue() == Mode.Control));
    public final SliderSetting downFactor = add(new SliderSetting("FallSpeed", 1.0f, 0.0f, 10.0f, () -> mode.getValue() == Mode.Control));
    public final SliderSetting speed = add(new SliderSetting("Speed", 1.0f, 0.1f, 10.0f, () -> mode.getValue() == Mode.Control));
    public final BooleanSetting speedLimit = add(new BooleanSetting("SpeedLimit", true, () -> mode.getValue() == Mode.Control));
    public final SliderSetting maxSpeed = add(new SliderSetting("MaxSpeed", 2.5f, 0.1f, 10.0f, () -> speedLimit.getValue() && mode.getValue() == Mode.Control));
    public final BooleanSetting noDrag = add(new BooleanSetting("NoDrag", false, () -> mode.getValue() == Mode.Control));
    private final SliderSetting sneakDownSpeed = add(new SliderSetting("DownSpeed", 1.0F, 0.1F, 10.0F, () -> mode.getValue() == Mode.Control));
    private final SliderSetting boost = add(new SliderSetting("Boost", 1F, 0.1F, 4F, () -> mode.getValue() == Mode.Boost));
    private final Timer instantFlyTimer = new Timer();
    private final Timer strictTimer = new Timer();
    public final Timer fireworkTimer = new Timer();
    private boolean hasElytra = false;
    boolean rubberbanded = false;

    public ElytraFly() {
        super("ElytraFly", Category.Movement);
        setChinese("鞘翅飞行");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            if (!mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
        hasElytra = false;
    }

    @Override
    public void onDisable() {
        rubberbanded = false;
        hasElytra = false;
        if (mc.player != null) {
            if (!mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
    }

    private void boost() {
        if (hasElytra) {
            if (!mc.player.isFallFlying()) {
                return;
            }
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            if (mc.options.forwardKey.isPressed()) {
                mc.player.addVelocity(-MathHelper.sin(yaw) * boost.getValueFloat() / 10, 0, MathHelper.cos(yaw) * boost.getValueFloat() / 10);
            }
        }
    }

    @Override
    public void onUpdate() {
        for (ItemStack is : mc.player.getArmorItems()) {
            if (is.getItem() instanceof ElytraItem) {
                hasElytra = true;
                break;
            } else {
                hasElytra = false;
            }
        }
        if (mode.is(Mode.Bounce)) {
            mc.player.jumpingCooldown = 0;
            return;
        } else {
            if (firework.getValue() && fireworkTimer.passed(delay.getValueInt()) && MovementUtil.isMoving() && !mc.player.isUsingItem() && mc.player.isFallFlying()) {
                OffFirework.INSTANCE.off();
                fireworkTimer.reset();
            }
        }
        if (!mc.player.isFallFlying()) {
            fireworkTimer.setMs(99999999);
            if (!mc.player.isOnGround() && instantFly.getValue() && mc.player.getVelocity().getY() < 0D) {
                if (!instantFlyTimer.passedMs((long) (1000 * timeout.getValue()))) return;
                instantFlyTimer.reset();
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                strictTimer.reset();
            }
        }
        if (mode.getValue() == Mode.Boost) {
            boost();
        }
    }

    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public final Vec3d getRotationVec(float tickDelta) {
        return this.getRotationVector(-upPitch.getValueFloat(), mc.player.getYaw(tickDelta));
    }

    @EventHandler
    private void onPlayerMove(MoveEvent event) {
        if (!(mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof ElytraItem)) return;
        if (mc.player.isFallFlying()) {
            int chunkX = (int) ((mc.player.getX()) / 16);
            int chunkZ = (int) ((mc.player.getZ()) / 16);
            if (autoStop.getValue()) {
                if (!mc.world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
                    event.setX(0);
                    event.setY(0);
                    event.setZ(0);
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (nullCheck()) return;
        if (mode.is(Mode.Bounce) && hasElytra) {
            if (autoJump.getValue()) mc.options.jumpKey.setPressed(true);
            if (event.isPost()) {
                if (!mc.player.isFallFlying())
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

                if (checkConditions(mc.player)) {
                    if (!sprint.getValue()) {
                        // Sprinting all the time (when not on ground) makes it rubberband on certain anticheats.
                        if (mc.player.isFallFlying()) mc.player.setSprinting(mc.player.isOnGround());
                        else mc.player.setSprinting(true);
                    }
                }
            } else {
                if (checkConditions(mc.player) && sprint.getValue()) mc.player.setSprinting(true);
            }
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (nullCheck()) return;
        if (mode.is(Mode.Bounce) && hasElytra && event.getPacket() instanceof ClientCommandC2SPacket && ((ClientCommandC2SPacket) event.getPacket()).getMode().equals(ClientCommandC2SPacket.Mode.START_FALL_FLYING) && !sprint.getValue()) {
            mc.player.setSprinting(true);
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (nullCheck()) return;
        if (mode.is(Mode.Bounce) && hasElytra && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            rubberbanded = true;
            mc.player.stopFallFlying();
        }
    }

    boolean prev;
    float prePitch;

    @EventHandler(priority = EventPriority.LOWEST)
    public void RotateEvent(RotateEvent event) {
        if (mode.is(Mode.Bounce) && hasElytra) {
            event.setPitch(pitch.getValueFloat());
        }
    }

    @EventHandler
    public void travel(TravelEvent event) {
        if (mode.is(Mode.Bounce) && hasElytra) {
            if (event.isPre()) {
                prev = true;
                prePitch = mc.player.getPitch();
                mc.player.setPitch(pitch.getValueFloat());
            } else {
                if (prev) {
                    prev = false;
                    mc.player.setPitch(prePitch);
                }
            }
        }
    }

    public static boolean recastElytra(ClientPlayerEntity player) {
        if (checkConditions(player) && ignoreGround(player)) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            return true;
        } else return false;
    }

    public static boolean checkConditions(ClientPlayerEntity player) {
        ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
        return (!player.getAbilities().flying && !player.hasVehicle() && !player.isClimbing() && itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable(itemStack));
    }

    private static boolean ignoreGround(ClientPlayerEntity player) {
        if (!player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable(itemStack)) {
                player.startFallFlying();
                return true;
            } else return false;
        } else return false;
    }

    @EventHandler
    public void onMove(TravelEvent event) {
        if (nullCheck() || !hasElytra || !mc.player.isFallFlying() || event.isPost()) return;
        if (mode.is(Mode.Freeze)) {
            if (!MovementUtil.isMoving()) {
                event.cancel();
                return;
            }
        }
        if (mode.getValue() != Mode.Control) return;
        if (firework.getValue()) {
            if (!(mc.options.sneakKey.isPressed() && mc.player.input.jumping)) {
                if (mc.options.sneakKey.isPressed()) {
                    setY(-sneakDownSpeed.getValue());
                }
                else if (mc.player.input.jumping) {
                    setY(upFactor.getValue());
                } else {
                    setY(-sneakDownSpeed.getValue());
                }
            } else {
                setY(0);
            }
            double[] dir = directionSpeedKey(speed.getValue());
            setX(dir[0]);
            setZ(dir[1]);
        } else {
            Vec3d lookVec = getRotationVec(mc.getTickDelta());
            double lookDist = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
            double motionDist = Math.sqrt(getX() * getX() + getZ() * getZ());
            if (mc.player.input.sneaking) {
                setY(-sneakDownSpeed.getValue());
            } else if (!mc.player.input.jumping) {
                setY(-0.00000000003D * downFactor.getValue());
            }
            if (mc.player.input.jumping) {
                if (motionDist > upFactor.getValue() / upFactor.getMaximum()) {
                    double rawUpSpeed = motionDist * 0.01325D;
                    setY(getY() + rawUpSpeed * 3.2D);
                    setX(getX() - lookVec.x * rawUpSpeed / lookDist);
                    setZ(getZ() - lookVec.z * rawUpSpeed / lookDist);
                } else {
                    double[] dir = directionSpeedKey(speed.getValue());
                    setX(dir[0]);
                    setZ(dir[1]);
                }
            }
            if (lookDist > 0.0D) {
                setX(getX() + (lookVec.x / lookDist * motionDist - getX()) * 0.1D);
                setZ(getZ() + (lookVec.z / lookDist * motionDist - getZ()) * 0.1D);
            }
            if (!mc.player.input.jumping) {
                double[] dir = directionSpeedKey(speed.getValue());
                setX(dir[0]);
                setZ(dir[1]);
            }
            if (!noDrag.getValue()) {
                setY(getY() * 0.9900000095367432D);
                setX(getX() * 0.9800000190734863D);
                setZ(getZ() * 0.9900000095367432D);
            }
            double finalDist = Math.sqrt(getX() * getX() + getZ() * getZ());
            if (speedLimit.getValue() && finalDist > maxSpeed.getValue()) {
                setX(getX() * maxSpeed.getValue() / finalDist);
                setZ(getZ() * maxSpeed.getValue() / finalDist);
            }
            event.cancel();
            mc.player.move(MovementType.SELF, mc.player.getVelocity());
        }
    }

    private double getX() {
        return getMotionX();
    }

    private void setX(double f) {
        setMotionX(f);
    }

    private double getY() {
        return getMotionY();
    }

    private void setY(double f) {
        setMotionY(f);
    }

    private double getZ() {
        return getMotionZ();
    }

    private void setZ(double f) {
        setMotionZ(f);
    }

    public enum Mode {
        Control, Boost, Bounce, Freeze, None
    }
}