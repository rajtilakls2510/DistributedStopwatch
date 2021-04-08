package rmi.indexer;

import rmi.shared.Client;
import rmi.shared.Server;

public class PeerDecorator {

    private Client client;
    private String ip;

    public PeerDecorator(Client client, String ip) {
        this.client = client;
        this.ip = ip;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
