package dev.luminous.mod.modules.impl.player.freelook;

import net.minecraft.client.MinecraftClient;

public class CameraState {
    public float lookYaw;
    public float lookPitch;
    public float transitionInitialYaw;
    public float transitionInitialPitch;
    public boolean doLock;
    public boolean doTransition;

    public float originalYaw() {
        return MinecraftClient.getInstance().getCameraEntity().getHeadYaw();
    }

    public float originalPitch() {
        return MinecraftClient.getInstance().getCameraEntity().getPitch();
    }
}
