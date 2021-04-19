package rmi.remotestopwatch;

import stopwatch.StopwatchUIUpdater;

public class NotRunningRemoteStopwatchState implements RemoteStopwatchState {

    /**
     * The RemoteStopwatch is in this state when the stop button was pressed or not started at all
     */

    RemoteStopwatch stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public NotRunningRemoteStopwatchState(RemoteStopwatch stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
        this.stopwatch = stopwatch;
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        this.name = name;
    }

    @Override
    public void handleUI() {
        stopwatchUIUpdater.onStart();
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
