package com.contrastsecurity.config;

import com.contrastsecurity.models.Server;

public class ServerComboBoxItem {
    private Server server;
    private String allServersName;

    public ServerComboBoxItem(Server server) {
        this.server = server;
    }

    public ServerComboBoxItem(String name) {
        this.allServersName = name;
    }

    @Override
    public String toString() {
        if (server == null) {
            return allServersName;
        } else {
            return server.getName();
        }
    }

    public Server getServer() {
        return server;
    }
}
