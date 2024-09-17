package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;
import net.minecraft.entity.player.PlayerEntity;

public class DeathEvent extends Event {
    private final PlayerEntity player;

    public DeathEvent(PlayerEntity player) {
        super(Stage.Post);
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}