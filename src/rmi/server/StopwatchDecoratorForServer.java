package rmi.server;

import main.InstanceInfo;
import stopwatch.Stopwatch;
import stopwatch.StopwatchUIUpdater;
import stopwatch.VirtualStopwatch;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class StopwatchDecoratorForServer implements VirtualStopwatch {

    /**
     * StopwatchDecoratorForServer is the decorator which holds the owner stopwatch of this instance.
     * <p>
     * This class is used to prevent the overhead of sending the member variables present in the stopwatch class
     * to be transferred over the network. This decorator class is sent over the network instead of the Stopwatch class to
     * prevent the member variables from being transferred over the network.
     */

    transient Stopwatch sw;

    public StopwatchDecoratorForServer(Stopwatch sw) throws RemoteException {
        this.sw = sw;
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public void startPauseResume() throws RemoteException {
        sw.startPauseResume();
    }

    @Override
    public void stop() throws RemoteException {
        sw.stop();
    }

    @Override
    public long getTime() throws RemoteException {
        return sw.getTime();
    }

    @Override
    public void remoteStartPressed(InstanceInfo clientInfo) throws RemoteException {
        sw.remoteStartPressed(clientInfo);
    }

    @Override
    public void remoteStopPressed(InstanceInfo clientInfo) throws RemoteException {
        sw.remoteStopPressed(clientInfo);
    }

    @Override
    public void remoteOnTimeUpdated(long time) throws RemoteException {

    }

    @Override
    public void setStopwatchUiUpdater(StopwatchUIUpdater stopwatchUIUpdater) throws RemoteException {

    }

    @Override
    public InstanceInfo getInstanceInfo() throws RemoteException {
        return sw.getInstanceInfo();
    }
}
