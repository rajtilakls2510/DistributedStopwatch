package rmi.client;

import stopwatch.StopwatchUIUpdater;

public class NotRunningStopwatchState implements VirtualStopwatchState {
    VirtualStopwatchClientImpl stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public NotRunningStopwatchState(VirtualStopwatchClientImpl stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
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
        stopwatch.setState(stopwatch.getRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
