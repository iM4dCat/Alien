package dev.luminous.api.events.impl;

import dev.luminous.api.events.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ClickBlockEvent extends Event {
    private final BlockPos pos;
    private final Direction direction;

    public ClickBlockEvent(BlockPos pos, Direction direction) {
        super(Stage.Pre);
        this.pos = pos;
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public BlockPos getPos() {
        return pos;
    }
}

