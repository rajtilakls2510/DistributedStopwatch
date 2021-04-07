package stopwatch;

public class InitializerStopwatchState implements StopwatchState {
    Stopwatch stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public InitializerStopwatchState(Stopwatch stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
        this.stopwatch = stopwatch;
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        this.name = name;
    }

    @Override
    public void handleStopwatch() {
        if (stopwatch.sw != null)
            stopwatch.sw.stop();
        stopwatch.sw = null;
    }

    @Override
    public void handleUI() {
        stopwatchUIUpdater.onStop();
    }

    @Override
    public void changeState() {
        stopwatch.setPreviousState(this);
        stopwatch.setState(stopwatch.getNotRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
