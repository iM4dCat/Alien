package dev.luminous.mod.modules.impl.movement;

import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.KeyboardInputEvent;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.api.events.impl.UpdateWalkingPlayerEvent;
import dev.luminous.api.utils.entity.MovementUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NoSlow extends Module {
    public static NoSlow INSTANCE;
    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Vanilla));
    private final BooleanSetting soulSand = add(new BooleanSetting("SoulSand", true));
    private final BooleanSetting active = add(new BooleanSetting("Gui", true));
    private final EnumSetting<Bypass> clickBypass = add(new EnumSetting<>("Bypass", Bypass.None));
    private final BooleanSetting sneak = add(new BooleanSetting("Sneak", false));

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    private enum Bypass {
       None, StrictNCP, GrimSwap, MatrixNcp, Delay, StrictNCP2
    }

    public enum Mode {
        Vanilla,
        NCP,
        Grim,
        None
    }
    public NoSlow() {
        super("NoSlow", Category.Movement);
        setChinese("无减速");
        INSTANCE = this;
    }

    private final Queue<ClickSlotC2SPacket> storedClicks = new LinkedList<>();
    private final AtomicBoolean pause = new AtomicBoolean();
    @EventHandler
    public void onUpdate(UpdateWalkingPlayerEvent event) {
        if (event.isPost()) return;
        if (mc.player.isUsingItem() && !mc.player.isRiding() && !mc.player.isFallFlying()) {
            switch (mode.getValue()) {
                case NCP -> {
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                }
                case Grim -> {
                    if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 8 + 1));
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 7 + 2));
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                    } else {
                        sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id));
                    }
                }
            }
        }
        if (active.getValue()) {
            if (!(mc.currentScreen instanceof ChatScreen)) {
                for (KeyBinding k : new KeyBinding[]{mc.options.backKey, mc.options.leftKey, mc.options.rightKey}) {
                    k.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
                }
                mc.options.jumpKey.setPressed(ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.mode.is(ElytraFly.Mode.Bounce) && ElytraFly.INSTANCE.autoJump.getValue() || InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.jumpKey.getBoundKeyTranslationKey()).getCode()));
                mc.options.forwardKey.setPressed(AutoWalk.INSTANCE.forward() || InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.forwardKey.getBoundKeyTranslationKey()).getCode()));
                mc.options.sprintKey.setPressed(Sprint.INSTANCE.isOn() || InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sprintKey.getBoundKeyTranslationKey()).getCode()));

                if (sneak.getValue()) {
                    mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
                }
            }
        }
    }
    @EventHandler
    public void keyboard(KeyboardInputEvent event) {
        if (active.getValue()) {
            if (!(mc.currentScreen instanceof ChatScreen)) {
                for (KeyBinding k : new KeyBinding[]{mc.options.backKey, mc.options.leftKey, mc.options.rightKey}) {
                    k.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
                }
                mc.options.jumpKey.setPressed(ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.mode.is(ElytraFly.Mode.Bounce) && ElytraFly.INSTANCE.autoJump.getValue() || InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.jumpKey.getBoundKeyTranslationKey()).getCode()));
                mc.options.forwardKey.setPressed(AutoWalk.INSTANCE.forward() || InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.forwardKey.getBoundKeyTranslationKey()).getCode()));
                mc.options.sprintKey.setPressed(Sprint.INSTANCE.isOn() || InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sprintKey.getBoundKeyTranslationKey()).getCode()));

                if (sneak.getValue()) {
                    mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
                }
                mc.player.input.pressingForward = mc.options.forwardKey.isPressed();
                mc.player.input.pressingBack = mc.options.backKey.isPressed();
                mc.player.input.pressingLeft = mc.options.leftKey.isPressed();
                mc.player.input.pressingRight = mc.options.rightKey.isPressed();
                mc.player.input.movementForward = getMovementMultiplier(mc.player.input.pressingForward, mc.player.input.pressingBack);
                mc.player.input.movementSideways = getMovementMultiplier(mc.player.input.pressingLeft, mc.player.input.pressingRight);
                mc.player.input.jumping = mc.options.jumpKey.isPressed();
                mc.player.input.sneaking = mc.options.sneakKey.isPressed();
            }
        }
    }
    private static float getMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0F;
        } else {
            return positive ? 1.0F : -1.0F;
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (nullCheck() || !MovementUtil.isMoving() || !mc.options.jumpKey.isPressed() || pause.get())
            return;

        if (e.getPacket() instanceof ClickSlotC2SPacket click) {
            switch (clickBypass.getValue()) {
                case GrimSwap -> {
                    if (click.getActionType() != SlotActionType.PICKUP && click.getActionType() != SlotActionType.PICKUP_ALL)
                        mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
                }

                case StrictNCP -> {
                    if (mc.player.isOnGround() && !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, 0.0656, 0.0)).iterator().hasNext()) {
                        if (mc.player.isSprinting())
                            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0656, mc.player.getZ(), false));
                    }
                }

                case StrictNCP2 -> {
                    if (mc.player.isOnGround() && !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, 0.000000271875, 0.0)).iterator().hasNext()) {
                        if (mc.player.isSprinting())
                            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.000000271875, mc.player.getZ(), false));
                    }
                }

                case MatrixNcp -> {
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                    mc.options.forwardKey.setPressed(false);
                    mc.player.input.movementForward = 0;
                    mc.player.input.pressingForward = false;
                }

                case Delay -> {
                    storedClicks.add(click);
                    e.cancel();
                }
            }
        }

        if(e.getPacket() instanceof CloseHandledScreenC2SPacket) {
            if(clickBypass.is(Bypass.Delay)) {
                pause.set(true);
                while (!storedClicks.isEmpty())
                    mc.getNetworkHandler().sendPacket(storedClicks.poll());
                pause.set(false);
            }
        }
    }

    @EventHandler
    public void onPacketSendPost(PacketEvent.SendPost e) {
        if (e.getPacket() instanceof ClickSlotC2SPacket) {
            if (mc.player.isSprinting() && clickBypass.is(Bypass.StrictNCP))
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }
    public boolean noSlow() {
        return isOn() && mode.getValue() != Mode.None;
    }

    public boolean soulSand() {
        return isOn() && soulSand.getValue();
    }
}
