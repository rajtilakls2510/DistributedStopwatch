package stopwatch;

public class InitializerStopwatchState implements StopwatchState {

    /**
     * This state should be used for initializing the stopwatch and the UI
     */


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

        //  Discard the old stopwatch if present
        if (stopwatch.getStopwatchEngine() != null)
            stopwatch.getStopwatchEngine().stop();
        stopwatch.setStopwatchEngine(null);
    }

    @Override
    public void handleUI() {
        stopwatchUIUpdater.onStop();
    }

    @Override
    public void changeState() {
        stopwatch.setPreviousState(this);

        // Changes to NotRunningState
        stopwatch.setCurrentState(stopwatch.getNotRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
