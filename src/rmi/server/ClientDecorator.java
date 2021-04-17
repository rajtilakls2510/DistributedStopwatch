package rmi.server;

import main.InstanceInfo;
import rmi.client.Client;

public class ClientDecorator {

    /**
     * ClientDecorator is the decorator to hold the client object with additional information about it. This avoids making a
     * remote call to fetch the client information
     */

    private Client client;
    private InstanceInfo instanceInfo;

    public ClientDecorator(Client client, InstanceInfo instanceInfo) {
        this.client = client;
        this.instanceInfo = instanceInfo;
    }

    public Client getClient() {
        return client;
    }

    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }
}
