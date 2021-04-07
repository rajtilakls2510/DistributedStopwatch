package rmi.server;

import rmi.shared.Client;

public class ClientDecorator {

    private Client client;
    private String name;
    private String identifier;

    public ClientDecorator(Client client, String name, String identifier) {
        this.client = client;
        this.name = name;
        this.identifier = identifier;
    }

    public Client getClient() {
        return client;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }
}
