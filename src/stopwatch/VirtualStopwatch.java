package stopwatch;

import main.InstanceInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualStopwatch extends Remote {

    void startPauseResume() throws RemoteException;

    void stop() throws RemoteException;

    long getTime() throws RemoteException;

    void remoteStartPressed(InstanceInfo clientInfo) throws RemoteException;

    void remoteStopPressed(InstanceInfo clientInfo) throws RemoteException;

    void remoteOnTimeUpdated(long time) throws RemoteException;

    void setStopwatchUiUpdater(StopwatchUIUpdater stopwatchUIUpdater) throws RemoteException;

    InstanceInfo getInstanceInfo() throws RemoteException;

}
