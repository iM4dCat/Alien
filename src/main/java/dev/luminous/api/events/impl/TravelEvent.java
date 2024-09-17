package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;
import net.minecraft.entity.player.PlayerEntity;

public class TravelEvent extends Event {

    private final PlayerEntity entity;


    public TravelEvent(Stage stage, PlayerEntity entity) {
        super(stage);
        this.entity = entity;
    }

    public PlayerEntity getEntity() {
        return entity;
    }
}