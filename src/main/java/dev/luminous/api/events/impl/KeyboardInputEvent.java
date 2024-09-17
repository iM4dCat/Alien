package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;

public class KeyboardInputEvent extends Event {
    public KeyboardInputEvent() {
        super(Stage.Pre);
    }
}
