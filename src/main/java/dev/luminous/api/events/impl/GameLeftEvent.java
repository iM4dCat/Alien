package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;

public class GameLeftEvent extends Event {
    public GameLeftEvent() {
        super(Stage.Post);
    }
}
