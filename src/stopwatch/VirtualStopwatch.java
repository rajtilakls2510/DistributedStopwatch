package stopwatch;

import main.InstanceInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualStopwatch extends Remote {

    /**
     * Virtual Stopwatch is an interface which provides an abstraction over Stopwatch created by the current instance and remote instances
     *
     * @throws RemoteException
     */

    // <-------------------------- These methods are called by the instance that this stopwatch is being displayed in  --------------------->

    void startPauseResume() throws RemoteException;

    void stop() throws RemoteException;

    long getTime() throws RemoteException;

    // <------------------------- These methods are called by some remote instance ------------------------>

    void remoteStartPressed(InstanceInfo clientInfo) throws RemoteException;

    void remoteStopPressed(InstanceInfo clientInfo) throws RemoteException;

    void remoteOnTimeUpdated(long time) throws RemoteException;

    // <----------------------------- Some common methods ----------------------->

    void setStopwatchUiUpdater(StopwatchUIUpdater stopwatchUIUpdater) throws RemoteException;

    InstanceInfo getInstanceInfo() throws RemoteException;

}
