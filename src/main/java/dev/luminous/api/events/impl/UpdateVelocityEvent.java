package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;
import net.minecraft.util.math.Vec3d;

public class UpdateVelocityEvent extends Event {
    Vec3d movementInput;
    float speed;
    float yaw;
    Vec3d velocity;

    public UpdateVelocityEvent(Vec3d movementInput, float speed, float yaw, Vec3d velocity) {
        super(Stage.Pre);
        this.movementInput = movementInput;
        this.speed = speed;
        this.yaw = yaw;
        this.velocity = velocity;
    }

    public Vec3d getMovementInput() {
        return this.movementInput;
    }

    public float getSpeed() {
        return this.speed;
    }

    public Vec3d getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Vec3d velocity) {
        this.velocity = velocity;
    }
}