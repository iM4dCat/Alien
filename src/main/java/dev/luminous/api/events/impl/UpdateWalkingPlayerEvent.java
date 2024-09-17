package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;

public class UpdateWalkingPlayerEvent extends Event {
    public UpdateWalkingPlayerEvent(Stage stage) {
        super(stage);
    }
}
