package rmi.virtualstopwatch;

import stopwatch.StopwatchUIUpdater;
import stopwatch.VirtualStopwatch;
import stopwatch.Stopwatch;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class StopwatchDecoratorForServer implements VirtualStopwatch {


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
    public void remoteStartPressed(String clientName) throws RemoteException {
        sw.remoteStartPressed(clientName);
    }

    @Override
    public void remoteStopPressed(String clientName) throws RemoteException {
        sw.remoteStopPressed(clientName);
    }

    @Override
    public void remoteOnTimeUpdated(long time) throws RemoteException {

    }

    @Override
    public void setStopwatchUiUpdater(StopwatchUIUpdater stopwatchUIUpdater) throws RemoteException {

    }

}
