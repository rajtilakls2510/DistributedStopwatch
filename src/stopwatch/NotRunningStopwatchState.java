package stopwatch;

public class NotRunningStopwatchState implements StopwatchState {
    Stopwatch stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public NotRunningStopwatchState(Stopwatch stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
        this.stopwatch = stopwatch;
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        this.name = name;
    }

    @Override
    public void handleStopwatch() {
        StopwatchEngine sw = new StopwatchEngine();
        stopwatch.setStopwatch(sw);
        sw.registerObserver(stopwatch);
        sw.start();
    }

    @Override
    public void handleUI() {
        stopwatchUIUpdater.onStart();
    }

    @Override
    public void changeState() {
        stopwatch.setPreviousState(this);
        stopwatch.setState(stopwatch.getRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
