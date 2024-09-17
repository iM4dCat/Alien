package dev.luminous.mod.modules.settings.impl;

import dev.luminous.Alien;
import dev.luminous.core.impl.ModuleManager;
import dev.luminous.mod.modules.settings.Setting;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SelectionManager;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

import static dev.luminous.api.utils.Wrapper.mc;
public class StringSetting extends Setting {
    private boolean isListening = false;
    private String text;
    private final String defaultValue;
    public StringSetting(String name, String text) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
        this.text = text;
        this.defaultValue = text;
    }

    public StringSetting(String name, String text, BooleanSupplier visibilityIn) {
        super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
        this.text = text;
        this.defaultValue = text;
    }
    @Override
    public void loadSetting() {
        setValue(Alien.CONFIG.getString(getLine(), defaultValue));
    }
    
    public String getValue() {
        return this.text;
    }

    public void setValue(String text) {
        this.text = text;
    }
    public void setListening(boolean set) {
        isListening = set;
        if (isListening) {
            current = this;
        }
    }

    public boolean isListening() {
        return isListening && current == this;
    }

    public void keyType(int keyCode) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_V -> {
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
                    setValue(getValue() + SelectionManager.getClipboard(mc));
                }
            }
            case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> setListening(false);
            case GLFW.GLFW_KEY_BACKSPACE -> setValue(removeLastChar(getValue()));
        }
    }

    public void charType(char c) {
        setValue(getValue() + c);
    }
    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && !str.isEmpty()) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

}
