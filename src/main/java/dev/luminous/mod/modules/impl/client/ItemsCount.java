package dev.luminous.mod.modules.impl.client;

import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemsCount extends Module {
    public static ItemsCount INSTANCE;
    private final SliderSetting xOffset = add(new SliderSetting("X", 100, 0, 1500));
    private final SliderSetting yOffset = add(new SliderSetting("Y", 100, 0, 1000));
    private final SliderSetting offset = add(new SliderSetting("Offset", 18, 0, 30));
    public final BooleanSetting hideEmpty = add(new BooleanSetting("HideEmpty", true));
    public final BooleanSetting crystal = add(new BooleanSetting("Crystal", true));
    public final BooleanSetting xp = add(new BooleanSetting("XP", true));
    public final BooleanSetting pearl = add(new BooleanSetting("Pearl", true));
    public final BooleanSetting obsidian = add(new BooleanSetting("Obsidian", true));
    public final BooleanSetting gApple = add(new BooleanSetting("GApple", true));
    public final BooleanSetting totem = add(new BooleanSetting("Totem", true));
    public final BooleanSetting web = add(new BooleanSetting("Web", true));
    public final BooleanSetting anchor = add(new BooleanSetting("Anchor", true));
    public final BooleanSetting glowstone = add(new BooleanSetting("Glowstone", true));
    public final BooleanSetting piston = add(new BooleanSetting("Piston", true));
    public final BooleanSetting redstone = add(new BooleanSetting("RedStone", true));
    public final BooleanSetting enderChest = add(new BooleanSetting("EnderChest", true));

    public ItemsCount() {
        super("Items", Category.Client);
        setChinese("物品数量");
        INSTANCE = this;
    }
    int x, y;
    DrawContext drawContext;
    ItemStack crystalStack = new ItemStack(Items.END_CRYSTAL);
    ItemStack xpStack = new ItemStack(Items.EXPERIENCE_BOTTLE);
    ItemStack pearlStack = new ItemStack(Items.ENDER_PEARL);
    ItemStack obsidianStack = new ItemStack(Items.OBSIDIAN);
    ItemStack gappleStack = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
    ItemStack totemStack = new ItemStack(Items.TOTEM_OF_UNDYING);
    ItemStack webStack = new ItemStack(Items.COBWEB);
    ItemStack anchorStack = new ItemStack(Items.RESPAWN_ANCHOR);
    ItemStack glowstoneStack = new ItemStack(Items.GLOWSTONE);
    ItemStack pistonStack = new ItemStack(Items.PISTON);
    ItemStack redstoneStack = new ItemStack(Items.REDSTONE_BLOCK);
    ItemStack enderChestStack = new ItemStack(Items.ENDER_CHEST);
    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        this.drawContext = drawContext;
        x = this.xOffset.getValueInt() - offset.getValueInt();
        y = this.yOffset.getValueInt();
        if (crystal.getValue()) {
            crystalStack.setCount(getItemCount(Items.END_CRYSTAL));
            drawItem(crystalStack);
        }
        if (xp.getValue()) {
            xpStack.setCount(getItemCount(Items.EXPERIENCE_BOTTLE));
            drawItem(xpStack);
        }
        if (pearl.getValue()) {
            pearlStack.setCount(getItemCount(Items.ENDER_PEARL));
            drawItem(pearlStack);
        }
        if (obsidian.getValue()) {
            obsidianStack.setCount(getItemCount(Items.OBSIDIAN));
            drawItem(obsidianStack);
        }
        if (gApple.getValue()) {
            gappleStack.setCount(getItemCount(Items.ENCHANTED_GOLDEN_APPLE));
            drawItem(gappleStack);
        }
        if (totem.getValue()) {
            totemStack.setCount(getItemCount(Items.TOTEM_OF_UNDYING));
            drawItem(totemStack);
        }
        if (web.getValue()) {
            webStack.setCount(getItemCount(Items.COBWEB));
            drawItem(webStack);
        }
        if (anchor.getValue()) {
            anchorStack.setCount(getItemCount(Items.RESPAWN_ANCHOR));
            drawItem(anchorStack);
        }
        if (glowstone.getValue()) {
            glowstoneStack.setCount(getItemCount(Items.GLOWSTONE));
            drawItem(glowstoneStack);
        }
        if (piston.getValue()) {
            int pistonCount = InventoryUtil.getItemCount(PistonBlock.class);
            if (pistonCount > 0 || !hideEmpty.getValue()) {
                x += offset.getValueInt();
                pistonStack.setCount(Math.max(1, pistonCount));
                drawItem(pistonStack);
            }
        }
        if (redstone.getValue()) {
            redstoneStack.setCount(getItemCount(Items.REDSTONE_BLOCK));
            drawItem(redstoneStack);
        }
        if (enderChest.getValue()) {
            enderChestStack.setCount(getItemCount(Items.ENDER_CHEST));
            drawItem(enderChestStack);
        }
    }

    private int getItemCount(Item item) {
        int i = InventoryUtil.getItemCount(item);
        if (hideEmpty.getValue() && i == 0) {
            return 0;
        } else {
            x += offset.getValueInt();
            return Math.max(i, 1);
        }
    }
    private void drawItem(ItemStack itemStack) {
        drawContext.drawItem(itemStack, x, y);
        drawContext.drawItemInSlot(mc.textRenderer, itemStack, x, y);
    }
}
