package stopwatch;

public class NotRunningStopwatchState implements StopwatchState {

    /**
     * The Stopwatch is in this state when the stop button was pressed or not started at all
     */

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

        // Creates a new stopwatch engine
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

        // Changes to RunningState
        stopwatch.setState(stopwatch.getRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
