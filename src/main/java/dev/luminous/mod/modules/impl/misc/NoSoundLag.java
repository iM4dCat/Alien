package dev.luminous.mod.modules.impl.misc;

import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.PlaySoundEvent;
import dev.luminous.mod.modules.Module;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;

public class NoSoundLag extends Module {
	public static NoSoundLag INSTANCE;
	public NoSoundLag() {
		super("NoSoundLag", Category.Misc);
		setChinese("去除声音");
		INSTANCE = this;
	}
	private final BooleanSetting equip =
			add(new BooleanSetting("ArmorEquip", true));
	private final BooleanSetting explode =
			add(new BooleanSetting("Explode", true));
	private final BooleanSetting attack =
			add(new BooleanSetting("Attack", true));
	static final ArrayList<SoundEvent> armor = new ArrayList<>();
	@EventHandler
	public void onPlaySound(PlaySoundEvent event){
		if (equip.getValue()) {
			for (SoundEvent se : armor) {
				if (event.sound.getId() == se.getId()) {
					event.cancel();
					return;
				}
			}
		}
		if (explode.getValue()) {
			if (event.sound.getId() == SoundEvents.ENTITY_GENERIC_EXPLODE.getId() || event.sound.getId() == SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE.getId()) {
				event.cancel();
				return;
			}
		}
		if (attack.getValue()) {
			if (event.sound.getId() == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK.getId() || event.sound.getId() == SoundEvents.ENTITY_PLAYER_ATTACK_STRONG.getId()) {
				event.cancel();
            }
		}
	}

	static {
		armor.add(SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE);
		armor.add(SoundEvents.ITEM_ARMOR_EQUIP_TURTLE);
		armor.add(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN);
		armor.add(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA);
		armor.add(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND);
		armor.add(SoundEvents.ITEM_ARMOR_EQUIP_GOLD);
		armor.add(SoundEvents.ITEM_ARMOR_EQUIP_IRON);
		armor.add(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER);
		armor.add(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC);
	}
}