package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;

import java.awt.*;

public class TotemParticleEvent extends Event {
    public double velocityX, velocityY, velocityZ;
    public Color color;
    public TotemParticleEvent(double velocityX, double velocityY, double velocityZ) {
        super(Stage.Pre);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }
}
