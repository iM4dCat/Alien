package dev.luminous.core.impl;

import dev.luminous.Alien;
import dev.luminous.core.Manager;
import net.minecraft.item.Items;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TradeManager extends Manager {
    public TradeManager() {
        read();
    }
    public final ArrayList<String> list = new ArrayList<>();
    public boolean inWhitelist(String name) {
        return list.contains(name);
    }
    public void remove(String name) {
        list.remove(name);
    }
    public void add(String name) {
        if (!list.contains(name)) {
            list.add(name);
        }
    }

    public void read() {
        try {
            File friendFile = getFile("trades.txt");
            if (!friendFile.exists()) {
                add(Items.ENCHANTED_BOOK.getTranslationKey());
                add(Items.DIAMOND_BLOCK.getTranslationKey());
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
            File friendFile = getFile("trades.txt");
            PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(friendFile), StandardCharsets.UTF_8));
            for (String str : list) {
                printwriter.println(str);
            }
            printwriter.close();
        } catch (Exception exception) {
            System.out.println("[" + Alien.NAME + "] Failed to save tradess");
        }
    }
}
