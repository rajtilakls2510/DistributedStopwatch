package rmi.remotestopwatch;

import stopwatch.StopwatchUIUpdater;

public class PausedRemoteStopwatchState implements RemoteStopwatchState {

    /**
     * The RemoteStopwatch is in this state when the Pause button is pressed
     */

    RemoteStopwatch stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public PausedRemoteStopwatchState(RemoteStopwatch stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
        this.stopwatch = stopwatch;
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        this.name = name;
    }

    @Override
    public void handleUI() {
        stopwatchUIUpdater.onResume();
    }

    @Override
    public void changeState() {
        stopwatch.setCurrentState(stopwatch.getRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
