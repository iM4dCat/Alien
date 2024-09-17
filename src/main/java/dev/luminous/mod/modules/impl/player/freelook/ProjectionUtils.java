package dev.luminous.mod.modules.impl.player.freelook;


import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class ProjectionUtils {
    public static Vec3d worldToScreen(Vec3d destination) {
        var client = MinecraftClient.getInstance();
        var renderer = client.gameRenderer;
        var camera = renderer.getCamera();
        var position = camera.getPos();
        var rotation = camera.getRotation();

        var calculation = rotation.conjugate().transform(position.subtract(destination).toVector3f());

        var fov = client.options.getFov().getValue();

        var half = client.getWindow().getScaledHeight() / 2;
        var scale = half / (calculation.z() * Math.tan(Math.toRadians(fov / 2)));

        return new Vec3d((calculation.x() * scale), (calculation.y() * scale), calculation.z());
    }
}
