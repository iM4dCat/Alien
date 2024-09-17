package dev.luminous.api.events.impl;

import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ServerConnectBeginEvent {

    public ServerAddress address;
    public ServerInfo info;

    public ServerConnectBeginEvent(ServerAddress address, ServerInfo info) {
        this.address = address;
        this.info = info;
    }
}