package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;

public class RotateEvent extends Event {
    private float yaw;
    private float pitch;
    private boolean modified;
    public RotateEvent(float yaw, float pitch) {
        super(Stage.Pre);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        modified = true;
        setYawNoModify(yaw);
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        modified = true;
        setPitchNoModify(pitch);
    }

    public boolean isModified() {
        return modified;
    }
    public void setRotation(final float yaw, final float pitch) {
        this.setYaw(yaw);
        this.setPitch(pitch);
    }

    public void setYawNoModify(float yaw) {
        this.yaw = yaw;
    }

    public void setPitchNoModify(float pitch) {
        this.pitch = pitch;
    }
}
