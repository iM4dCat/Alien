package dev.luminous.api.interfaces;

import net.minecraft.client.gl.Framebuffer;

public interface IShaderEffect {
    void addHook(String name, Framebuffer buffer);
}