package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;

public class MoveEvent extends Event {
    private double x;
    private double y;
    private double z;

    public MoveEvent(double x, double y, double z) {
        super(Stage.Pre);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
