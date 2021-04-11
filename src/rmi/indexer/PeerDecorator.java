package rmi.indexer;

import main.InstanceInfo;
import rmi.client.Client;

public class PeerDecorator {

    private Client client;
    private InstanceInfo instanceInfo;

    public PeerDecorator(Client client, InstanceInfo instanceInfo) {
        this.client = client;
        this.instanceInfo = instanceInfo;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }

    public void setInstanceInfo(InstanceInfo instanceInfo) {
        this.instanceInfo = instanceInfo;
    }
}
