package dev.luminous.api.utils.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockPosX extends BlockPos {

    public BlockPosX(double x, double y, double z) {
        super(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    public BlockPosX(double x, double y, double z, boolean fix) {
        this(x, y + (fix ? 0.5 : 0), z);
    }

    public BlockPosX(Vec3d vec3d) {
        this(vec3d.x, vec3d.y, vec3d.z);
    }

    public BlockPosX(Vec3d vec3d, boolean fix) {
        this(vec3d.x, vec3d.y, vec3d.z, fix);
    }
}
