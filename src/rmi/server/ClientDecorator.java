package rmi.server;

import main.InstanceInfo;
import rmi.client.Client;

public class ClientDecorator {

    private Client client;
    private InstanceInfo instanceInfo;

    public ClientDecorator(Client client,  InstanceInfo instanceInfo) {
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
