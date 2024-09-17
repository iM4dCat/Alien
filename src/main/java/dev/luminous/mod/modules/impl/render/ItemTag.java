package dev.luminous.mod.modules.impl.render;

import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.text.Text;

public class ItemTag extends Module {

    public ItemTag() {
        super("ItemTag", Category.Render);
        setChinese("物品标签");
    }
    public final BooleanSetting customName = add(new BooleanSetting("CustomName", false));
    public final BooleanSetting count = add(new BooleanSetting("Count", true));
    @Override
    public void onUpdate() {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ItemEntity itemEntity) {
                String s = count.getValue() ? " x" + itemEntity.getStack().getCount() : "";
                itemEntity.setCustomName(Text.of((customName.getValue() ? itemEntity.getStack().getName() : itemEntity.getStack().getItem().getName()).getString() + s));

                itemEntity.setCustomNameVisible(true);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.world == null) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ItemEntity itemEntity) {
                itemEntity.setCustomNameVisible(false);
            }
        }
    }
    /*    @EventHandler
    public void onReceivePacket(EntitySpawnEvent event) {
        if (nullCheck()) return;
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            itemEntity.setCustomName(itemEntity.getStack().getName());
            itemEntity.setCustomNameVisible(true);
        }
    }*/
}