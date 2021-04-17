package main;

import java.io.Serializable;

public class InstanceInfo implements Serializable {

    /**
     * Instance Info class is responsible for holding the information about the current instance.
     */


    // Identifier for a particular instance
    private String instanceIdentifier;

    // IP for the machine
    private String hostIP;

    // ServerName is the name used by remote clients to get the stub of this application's server from the RMI Registry
    private String serverName;

    // Similar to ServerName
    private String clientName;

    public InstanceInfo(String instanceIdentifier) {
        this.instanceIdentifier = instanceIdentifier;
        hostIP="";
        serverName = "SERVER:"+instanceIdentifier;
        clientName = "CLIENT:"+instanceIdentifier;
    }

    public InstanceInfo(String instanceIdentifier, String hostIP) {
        this.instanceIdentifier = instanceIdentifier;
        this.hostIP = hostIP;
        serverName = "SERVER:"+instanceIdentifier;
        clientName = "CLIENT:"+instanceIdentifier;
    }

    public String getInstanceIdentifier() {
        return instanceIdentifier;
    }

    public String getHostIP() {
        return hostIP;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    public String getServerName() {
        return serverName;
    }

    public String getClientName() {
        return clientName;
    }
}
