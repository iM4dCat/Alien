package dev.luminous.mod.modules.settings.impl;

import dev.luminous.Alien;
import dev.luminous.core.impl.ModuleManager;
import dev.luminous.mod.modules.settings.Setting;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.function.BooleanSupplier;

public class BindSetting extends Setting {
    private boolean isListening = false;
    private int key;
    private final int defaultKey;
    private boolean pressed = false;
    private boolean holdEnable = false;
    public boolean hold = false;
    public BindSetting(String name, int key) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
        defaultKey = key;
        this.key = key;
    }

    public BindSetting(String name, int key, BooleanSupplier visibilityIn) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
        defaultKey = key;
        this.key = key;
    }

    @Override
    public void loadSetting() {
        setKey(Alien.CONFIG.getInt(getLine(), defaultKey));
        setHoldEnable(Alien.CONFIG.getBoolean(getLine() + "_hold"));
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getBind() {
        if (key == -1) return "None";
        if (key < -1) {
            if (key == -2) {
                return "MouseLeft";
            }
            if (key == -3) {
                return "MouseRight";
            }
            if (key == -4) {
                return "MouseMiddle";
            }
            return "Mouse" + (Math.abs(key) - 4);
        }
        String kn = this.key > 0 ? GLFW.glfwGetKeyName(this.key, GLFW.glfwGetKeyScancode(this.key)) : "None";
        if (kn == null) {
            try {
                for (Field declaredField : GLFW.class.getDeclaredFields()) {
                    if (declaredField.getName().startsWith("GLFW_KEY_")) {
                        int a = (int) declaredField.get(null);
                        if (a == this.key) {
                            String nb = declaredField.getName().substring("GLFW_KEY_".length());
                            kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                        }
                    }
                }
            } catch (Exception ignored) {
                kn = "None";
            }
        }

        return kn.toUpperCase();
    }

    public void setListening(boolean set) {
        isListening = set;
    }

    public boolean isListening() {
        return isListening;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setHoldEnable(boolean holdEnable) {
        this.holdEnable = holdEnable;
    }

    public boolean isHoldEnable() {
        return holdEnable;
    }
}
