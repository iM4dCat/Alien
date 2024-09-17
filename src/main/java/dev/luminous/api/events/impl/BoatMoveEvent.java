package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;
import net.minecraft.entity.vehicle.BoatEntity;

public class BoatMoveEvent extends Event {

    private final BoatEntity boat;

    public BoatMoveEvent(BoatEntity boat) {
        super(Stage.Pre);
        this.boat = boat;
    }

    public BoatEntity getBoat() {
        return boat;
    }
}

