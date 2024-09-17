package dev.luminous.core.impl;

import dev.luminous.core.Manager;
import dev.luminous.Alien;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FriendManager extends Manager {
    public FriendManager() {
        read();
    }
    public final ArrayList<String> friendList = new ArrayList<>();
    public boolean isFriend(String name) {
        return friendList.contains(name);
    }
    public void removeFriend(String name) {
        friendList.remove(name);
    }
    public void addFriend(String name) {
        if (!friendList.contains(name)) {
            friendList.add(name);
        }
    }

    public void friend(PlayerEntity entity) {
        friend(entity.getGameProfile().getName());
    }

    public void friend(String name) {
        if (friendList.contains(name)) {
            friendList.remove(name);
        } else {
            friendList.add(name);
        }
    }

    public void read() {
        try {
            File friendFile = getFile("friends.txt");
            if (!friendFile.exists())
                return;
            List<String> list = IOUtils.readLines(new FileInputStream(friendFile), StandardCharsets.UTF_8);
            
            for (String s : list) {
                addFriend(s);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    
    public void save() {
        try {
            File friendFile = getFile("friends.txt");
            PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(friendFile), StandardCharsets.UTF_8));
            for (String str : friendList) {
                printwriter.println(str);
            }
            printwriter.close();
        } catch (Exception exception) {
            System.out.println("[" + Alien.NAME + "] Failed to save friends");
        }
    }
    

    public boolean isFriend(PlayerEntity entity) {
        return isFriend(entity.getGameProfile().getName());
    }
}
