package rmi.client;

import main.InstanceInfo;
import rmi.server.Server;

public class ServerDecorator {

    /**
     * ServerDecorator is the decorator to hold the server object with additional information about it. This avoids making a
     * remote call to fetch the server information
     */

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
