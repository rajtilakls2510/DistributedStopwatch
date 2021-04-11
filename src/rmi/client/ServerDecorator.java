package rmi.client;

import main.InstanceInfo;
import rmi.server.Server;

public class ServerDecorator {
    private Server server;
    private InstanceInfo instanceInfo;

    public ServerDecorator(Server server, InstanceInfo instanceInfo) {
        this.server = server;
        this.instanceInfo = instanceInfo;
    }

    public Server getServer() {
        return server;
    }


    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }
}
