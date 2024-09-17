package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class HeldItemRendererEvent extends Event {
    private final Hand hand;
    private final ItemStack item;
    private final float ep;
    private final MatrixStack stack;

    public HeldItemRendererEvent(Hand hand, ItemStack item, float equipProgress, MatrixStack stack) {
        super(Stage.Pre);
        this.hand = hand;
        this.item = item;
        this.ep = equipProgress;
        this.stack = stack;
    }

    public Hand getHand() {
        return hand;
    }

    public ItemStack getItem() {
        return item;
    }

    public float getEp() {
        return ep;
    }

    public MatrixStack getStack() {
        return stack;
    }
}