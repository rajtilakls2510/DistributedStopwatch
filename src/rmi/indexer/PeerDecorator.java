package rmi.indexer;

import main.InstanceInfo;
import rmi.client.Client;

public class PeerDecorator {

    /**
     * PeerDecorator is the decorator to hold the peer object with additional information about it. This avoids making a
     * remote call to fetch the peer information
     */

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
