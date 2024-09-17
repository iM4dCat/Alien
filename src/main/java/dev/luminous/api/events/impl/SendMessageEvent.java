package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;

public class SendMessageEvent extends Event {

    public String message;
    public final String defaultMessage;

    public SendMessageEvent(String message) {
        super(Stage.Pre);
        this.defaultMessage = message;
        this.message = message;
    }
}