package rmi.virtualstopwatch;

import main.ApplicationController;
import main.InstanceInfo;
import stopwatch.StopwatchUIUpdater;
import stopwatch.VirtualStopwatch;

import java.rmi.RemoteException;

public class RemoteStopwatch implements VirtualStopwatch {

    /**
     * RemoteStopwatch is the class which decorates the Stopwatch object received from the server of another instance and helps the
     * users interact with the remote stopwatches.
     * <p>
     * To use this class, create an object using the constructor and then call the setStopwatchUiUpdater() method with the StopwatchUiUpdater object
     */

    VirtualStopwatch sw;

    // Possible states of the Stopwatch
    RemoteStopwatchState notRunningState;
    RemoteStopwatchState runningState;
    RemoteStopwatchState pausedState;
    RemoteStopwatchState stopPressedState;

    // Current state of the stopwatch
    RemoteStopwatchState currentState;

    // The UI updater which is used to update the UI when a button is pressed (or other interactions).
    StopwatchUIUpdater stopwatchUIUpdater;

    // This string is used to find out the previous state of the remote stopwatch and set current state accordingly
    String previousState;

    // Stores the instance info of the owner of the stopwatch
    InstanceInfo remoteOwnerInfo;

    public RemoteStopwatch(VirtualStopwatch stopwatch, String previousState, InstanceInfo ownerInfo) {
        this.remoteOwnerInfo = ownerInfo;
        sw = stopwatch;
        this.previousState = previousState;

    }

    /**
     * This method is used to set the StopwatchUiUpdater of this RemoteStopwatch.
     * <p>
     * After initializing the states, this method decodes the state received from the owner instance
     *
     * @param stopwatchUIUpdater
     * @throws RemoteException
     */
    @Override
    public void setStopwatchUiUpdater(StopwatchUIUpdater stopwatchUIUpdater) throws RemoteException {
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        notRunningState = new NotRunningRemoteStopwatchState(this, this.stopwatchUIUpdater, "NOT_RUNNING");
        runningState = new RunningRemoteStopwatchState(this, this.stopwatchUIUpdater, "RUNNING");
        pausedState = new PausedRemoteStopwatchState(this, this.stopwatchUIUpdater, "PAUSED");
        stopPressedState = new InitializerRemoteStopwatchState(this, this.stopwatchUIUpdater, "STOP_PRESSED");
        currentState = stopPressedState;

        currentState.execute();
        decodeState(previousState);
    }

    /**
     * This method decodes the state received in String format (The String is actually the name of the State)
     *
     * @param previousState
     */
    private void decodeState(String previousState) {
        if (previousState.equals(pausedState.getName()))
            currentState = pausedState;

        else if (previousState.equals(notRunningState.getName()))
            currentState = notRunningState;

        else if (previousState.equals(runningState.getName()))
            currentState = runningState;

        else
            currentState = stopPressedState;

        currentState.execute();
        stopwatchUIUpdater.onTimeUpdate(getTime());
    }

    // <--------------------------------------- Methods which are used to send the user interactions to the owner instance -------------------------->

    @Override
    public void startPauseResume() {
        ApplicationController.networkThreadPool.submit(
                new Runnable() {
                    @Override
                    public void run() {

                        try {
                            sw.remoteStartPressed(ApplicationController.instanceInfo);
                            currentState.execute();
                        } catch (RemoteException e) {
                        }
                    }
                });
    }

    @Override
    public void stop() {
        ApplicationController.networkThreadPool.submit(
                new Runnable() {
                    @Override
                    public void run() {

                        try {
                            sw.remoteStopPressed(ApplicationController.instanceInfo);
                            currentState = stopPressedState;
                            currentState.execute();
                        } catch (RemoteException e) {

                        }
                    }
                });
    }

    @Override
    public long getTime() {
        try {
            return sw.getTime();
        } catch (RemoteException e) {
        }
        return 0L;
    }

    // <------------------------------- These methods are called when the owner stopwatch has some UI updates and wants all the clients to maintain sync ------------------------------>

    @Override
    public void remoteStartPressed(InstanceInfo serverInfo) {
        currentState.execute();
    }

    @Override
    public void remoteStopPressed(InstanceInfo serverInfo) {
        currentState = stopPressedState;
        currentState.execute();
    }

    @Override
    public void remoteOnTimeUpdated(long time) throws RemoteException {
        stopwatchUIUpdater.onTimeUpdate(time);
    }

    @Override
    public InstanceInfo getInstanceInfo() throws RemoteException {
        return remoteOwnerInfo;
    }

    // <-------------------------------- State changer methods ------------------------------->

    public void setState(RemoteStopwatchState stopwatchState) {
        currentState = stopwatchState;
    }

    public RemoteStopwatchState getNotRunningState() {
        return notRunningState;
    }

    public RemoteStopwatchState getRunningState() {
        return runningState;
    }

    public RemoteStopwatchState getPausedState() {
        return pausedState;
    }

    public RemoteStopwatchState getCurrentState() {
        return currentState;
    }
}
