package dev.luminous.core.impl;

import dev.luminous.Alien;
import dev.luminous.core.Manager;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.render.XRay;
import net.minecraft.block.Blocks;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class XrayManager extends Manager {
    public XrayManager() {
        read();
    }
    public final ArrayList<String> list = new ArrayList<>();
    public boolean inWhitelist(String name) {
        return list.contains(name);
    }
    public void remove(String name) {
        if (list.remove(name)) {
            if (!Module.nullCheck() && XRay.INSTANCE.isOn()) {
                mc.worldRenderer.reload();
            }
        }
    }
    public void add(String name) {
        if (!list.contains(name)) {
            list.add(name);
            if (!Module.nullCheck() && XRay.INSTANCE.isOn()) {
                mc.worldRenderer.reload();
            }
        }
    }

    public void read() {
        try {
            File friendFile = getFile("xrays.txt");
            if (!friendFile.exists()) {
                add(Blocks.DIAMOND_ORE.getTranslationKey());
                add(Blocks.DEEPSLATE_DIAMOND_ORE.getTranslationKey());
                add(Blocks.GOLD_ORE.getTranslationKey());
                add(Blocks.NETHER_GOLD_ORE.getTranslationKey());
                add(Blocks.IRON_ORE.getTranslationKey());
                add(Blocks.DEEPSLATE_IRON_ORE.getTranslationKey());
                add(Blocks.REDSTONE_ORE.getTranslationKey());
                add(Blocks.EMERALD_ORE.getTranslationKey());
                add(Blocks.DEEPSLATE_EMERALD_ORE.getTranslationKey());
                add(Blocks.DEEPSLATE_REDSTONE_ORE.getTranslationKey());
                add(Blocks.COAL_ORE.getTranslationKey());
                add(Blocks.DEEPSLATE_COAL_ORE.getTranslationKey());
                add(Blocks.ANCIENT_DEBRIS.getTranslationKey());
                add(Blocks.NETHER_QUARTZ_ORE.getTranslationKey());
                add(Blocks.LAPIS_ORE.getTranslationKey());
                add(Blocks.DEEPSLATE_LAPIS_ORE.getTranslationKey());
                return;
            }
            List<String> list = IOUtils.readLines(new FileInputStream(friendFile), StandardCharsets.UTF_8);
            
            for (String s : list) {
                add(s);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    
    public void save() {
        try {
            File friendFile = getFile("xrays.txt");
            PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(friendFile), StandardCharsets.UTF_8));
            for (String str : list) {
                printwriter.println(str);
            }
            printwriter.close();
        } catch (Exception exception) {
            System.out.println("[" + Alien.NAME + "] Failed to save xrays");
        }
    }
}
