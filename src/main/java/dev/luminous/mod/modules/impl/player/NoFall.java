package dev.luminous.mod.modules.impl.player;

import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.asm.accessors.IPlayerMoveC2SPacket;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.exploit.BowBomb;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
	private final SliderSetting distance =
			add(new SliderSetting("Distance", 3.0f, 0.0f, 8.0f, 0.1));
	public NoFall() {
		super("NoFall", Category.Player);
		setChinese("没有摔落伤害");
		this.setDescription("Prevents fall damage.");
	}

	@Override
	public String getInfo() {
		return "SpoofGround";
	}


	@EventHandler
	public void onPacketSend(PacketEvent.Send event) {
		if (nullCheck()) {
			return;
		}
		for (ItemStack is : mc.player.getArmorItems()) {
			if (is.getItem() == Items.ELYTRA) {
				return;
			}
		}
		if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
			if (mc.player.fallDistance >= (float) this.distance.getValue() && !BowBomb.send) {
				((IPlayerMoveC2SPacket) packet).setOnGround(true);
			}
		}
	}
}
