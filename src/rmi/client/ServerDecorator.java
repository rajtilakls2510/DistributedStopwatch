package rmi.client;

import rmi.server.Server;

public class ServerDecorator {
    private Server server;
    private String name;
    private String identifier;

    public ServerDecorator(Server server, String name, String identifier) {
        this.server = server;
        this.name = name;
        this.identifier = identifier;
    }

    public Server getServer() {
        return server;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }
}
