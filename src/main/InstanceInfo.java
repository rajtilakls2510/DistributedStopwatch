package main;

import java.io.Serializable;

public class InstanceInfo implements Serializable {


    private String instanceIdentifier;
    private String hostIP;
    private String serverName;
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
