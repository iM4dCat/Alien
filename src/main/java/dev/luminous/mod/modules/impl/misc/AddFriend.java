package dev.luminous.mod.modules.impl.misc;

import dev.luminous.Alien;
import dev.luminous.mod.modules.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AddFriend extends Module {
	public static AddFriend INSTANCE;

	public AddFriend() {
		super("AddFriend", Category.Misc);
		setChinese("加好友");
		INSTANCE = this;
	}

	@Override
	public void onEnable() {
		if (nullCheck()) {
			disable();
			return;
		}
		HitResult target = mc.crosshairTarget;
		if (target instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof PlayerEntity player) {
			Alien.FRIEND.friend(player);
		}
		disable();
	}
}