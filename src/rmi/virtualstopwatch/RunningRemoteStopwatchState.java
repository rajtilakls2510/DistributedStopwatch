package rmi.virtualstopwatch;

import stopwatch.StopwatchUIUpdater;

public class RunningRemoteStopwatchState implements RemoteStopwatchState {

    /**
     * The RemoteStopwatch is in this state when the Start/Resume button is pressed
     */

    RemoteStopwatch stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public RunningRemoteStopwatchState(RemoteStopwatch stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
        this.stopwatch = stopwatch;
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        this.name = name;
    }

    @Override
    public void handleUI() {
        stopwatchUIUpdater.onPause();
    }

    @Override
    public void changeState() {
        stopwatch.setState(stopwatch.getPausedState());
    }

    @Override
    public String getName() {
        return name;
    }
}
