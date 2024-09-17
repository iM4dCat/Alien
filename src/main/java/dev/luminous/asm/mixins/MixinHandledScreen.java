package dev.luminous.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.luminous.api.utils.render.Render2DUtil;
import dev.luminous.asm.accessors.IScreen;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import dev.luminous.mod.modules.impl.misc.Tips;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Set;

import static dev.luminous.api.utils.Wrapper.mc;
@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {

    protected MixinHandledScreen(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        int i = this.x;
        int j = this.y;
        super.renderBackground(context, mouseX, mouseY, delta);
        RenderSystem.disableDepthTest();
        context.getMatrices().push();
        float size = (float) ClientSetting.inventoryFade.ease(ClientSetting.INSTANCE.animEase.getValue());
        context.getMatrices().translate((float) (i + x / 4) * (1 - size), (float) (j + y / 4) * (1 - size), 0.0F);
        context.getMatrices().scale(size, size, 1);
        this.drawBackground(context, delta, mouseX, mouseY);
        for(Drawable drawable : ((IScreen) this).getDrawables()) {
            drawable.render(context, mouseX, mouseY, delta);
        }
        context.getMatrices().translate((float) i, (float) j, 0.0F);
        this.focusedSlot = null;

        for (int k = 0; k < this.handler.slots.size(); ++k) {
            Slot slot = this.handler.slots.get(k);
            if (slot.isEnabled()) {
                this.drawSlot(context, slot);
            }

            if (this.isPointOverSlot(slot, mouseX, mouseY) && slot.isEnabled()) {
                this.focusedSlot = slot;
                int l = slot.x;
                int m = slot.y;
                if (this.focusedSlot.canBeHighlighted()) {
                    drawSlotHighlight(context, l, m, 0);
                }
            }
        }

        this.drawForeground(context, mouseX, mouseY);
        ItemStack itemStack = this.touchDragStack.isEmpty() ? this.handler.getCursorStack() : this.touchDragStack;
        if (!itemStack.isEmpty()) {
            int l = this.touchDragStack.isEmpty() ? 8 : 16;
            String string = null;
            if (!this.touchDragStack.isEmpty() && this.touchIsRightClickDrag) {
                itemStack = itemStack.copyWithCount(MathHelper.ceil((float) itemStack.getCount() / 2.0F));
            } else if (this.cursorDragging && this.cursorDragSlots.size() > 1) {
                itemStack = itemStack.copyWithCount(this.draggedStackRemainder);
                if (itemStack.isEmpty()) {
                    string = Formatting.YELLOW + "0";
                }
            }

            this.drawItem(context, itemStack, mouseX - i - 8, mouseY - j - l, string);
        }

        if (!this.touchDropReturningStack.isEmpty()) {
            float f = (float) (Util.getMeasuringTimeMs() - this.touchDropTime) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                this.touchDropReturningStack = ItemStack.EMPTY;
            }

            int l = this.touchDropOriginSlot.x - this.touchDropX;
            int m = this.touchDropOriginSlot.y - this.touchDropY;
            int o = this.touchDropX + (int) ((float) l * f);
            int p = this.touchDropY + (int) ((float) m * f);
            this.drawItem(context, this.touchDropReturningStack, o, p, null);
        }

        context.getMatrices().pop();
        RenderSystem.enableDepthTest();
        if (focusedSlot != null && !focusedSlot.getStack().isEmpty() && client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            if (hasItems(focusedSlot.getStack()) && Tips.INSTANCE.isOn() && Tips.INSTANCE.shulkerViewer.getValue()) {
                renderShulkerToolTip(context, mouseX, mouseY, focusedSlot.getStack());
            }
        }
    }

    @Unique
    public void renderShulkerToolTip(DrawContext context, int mouseX, int mouseY, ItemStack stack) {
        try {
            NbtCompound compoundTag = stack.getSubNbt("BlockEntityTag");
            DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
            Inventories.readNbt(compoundTag, itemStacks);
            draw(context, itemStacks, mouseX, mouseY);
        } catch (Exception ignore) {
        }
    }

    @Unique
    private void draw(DrawContext context, DefaultedList<ItemStack> itemStacks, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        mouseX += 8;
        mouseY -= 82;

        drawBackground(context, mouseX, mouseY);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        DiffuseLighting.enableGuiDepthLighting();
        int row = 0;
        int i = 0;
        for (ItemStack itemStack : itemStacks) {
            context.drawItem(itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
            context.drawItemInSlot(mc.textRenderer, itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
            i++;
            if (i >= 9) {
                i = 0;
                row++;
            }
        }
        DiffuseLighting.disableGuiDepthLighting();
        RenderSystem.enableDepthTest();
    }
    @Unique
    private void drawBackground(DrawContext context, int x, int y) {
       Render2DUtil.drawRect(context.getMatrices(), x, y, 176, 67, new Color(0, 0,0, 120));
    }
    @Unique
    private boolean hasItems(ItemStack itemStack) {
        NbtCompound compoundTag = itemStack.getSubNbt("BlockEntityTag");
        return compoundTag != null && compoundTag.contains("Items", 9);
    }
    @Shadow public abstract void renderBackground(DrawContext context, int mouseX, int mouseY, float delta);

    @Shadow
    protected abstract void drawBackground(DrawContext context, float delta, int mouseX, int mouseY);
    @Shadow
    private void drawItem(DrawContext context, ItemStack stack, int x, int y, String amountText) {
    }
    @Shadow
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }
    @Shadow
    public static void drawSlotHighlight(DrawContext context, int x, int y, int z) {
    }
    @Shadow
    private boolean isPointOverSlot(Slot slot, double pointX, double pointY) {
        return false;
    }
    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Shadow
    private int draggedStackRemainder;
    @Shadow
    protected void drawSlot(DrawContext context, Slot slot) {
    }
    @Final
    @Shadow
    protected T handler;
    @Shadow
    private ItemStack touchDragStack;
    @Shadow
    private int touchDropX;
    @Shadow
    private int touchDropY;
    @Shadow
    private long touchDropTime;
    @Shadow
    private ItemStack touchDropReturningStack;
    @Final
    @Shadow
    protected  Set<Slot> cursorDragSlots;
    @Shadow
    protected boolean cursorDragging;
    @Shadow
    @Nullable
    private Slot touchDropOriginSlot;
    @Shadow
    private boolean touchIsRightClickDrag;
}
