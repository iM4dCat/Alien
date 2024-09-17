package dev.luminous.mod.modules.impl.player;

import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class PacketEat extends Module {
	public static PacketEat INSTANCE;
	private final BooleanSetting deSync =
			add(new BooleanSetting("DeSync", false));
	public PacketEat() {
		super("PacketEat", Category.Player);
		setChinese("发包进食");
		INSTANCE = this;
	}

    @Override
    public void onUpdate() {
		if (deSync.getValue() && mc.player.isUsingItem() && mc.player.getActiveItem().getItem().isFood()){
			Module.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
		}
	}

	@EventHandler
	public void onPacket(PacketEvent.Send event) {
		if (event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && mc.player.getActiveItem().getItem().isFood()) {
			event.cancel();
		}
	}
}