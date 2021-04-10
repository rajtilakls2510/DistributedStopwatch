package stopwatch;

import main.ApplicationController;

public class Stopwatch implements Observer, VirtualStopwatch {

    StopwatchCore sw;

    StopwatchState notRunningState;
    StopwatchState runningState;
    StopwatchState pausedState;
    StopwatchState stopPressedState;

    StopwatchState currentState;
    StopwatchState previousState;

    StopwatchUIUpdater stopwatchUIUpdater;
    transient ApplicationController context;


    public Stopwatch(ApplicationController context) {
        this.context = context;

    }

    @Override
    public void setStopwatchUiUpdater(StopwatchUIUpdater stopwatchUIUpdater) {
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        notRunningState = new NotRunningStopwatchState(this, this.stopwatchUIUpdater, "NOT_RUNNING");
        runningState = new RunningStopwatchState(this, this.stopwatchUIUpdater, "RUNNING");
        pausedState = new PausedStopwatchState(this, this.stopwatchUIUpdater, "PAUSED");
        stopPressedState = new InitializerStopwatchState(this, this.stopwatchUIUpdater, "STOP_PRESSED");
        previousState = stopPressedState;
        currentState = stopPressedState;

        currentState.execute();
    }


    public void startPauseResume() {
        currentState.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                context.notifyServerStartPauseResumePressed();
            }
        }).start();

    }

    public void stop() {

        currentState = stopPressedState;
        currentState.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                context.notifyServerStopPressed();
            }
        }).start();

    }

    @Override
    public long getTime() {
        if (sw != null)
            return sw.getTime();
        return 0L;
    }


    @Override
    public void update(long time) {

        stopwatchUIUpdater.onTimeUpdate(time);
        new Thread(new Runnable() {
            @Override
            public void run() {
                context.notifyServerStopwatchTimeChange(time);
            }
        }).start();
    }


    public void setState(StopwatchState stopwatchState) {
        currentState = stopwatchState;
    }

    public void setPreviousState(StopwatchState previousState) {
        this.previousState = previousState;
    }

    public void setStopwatch(StopwatchCore sw) {
        this.sw = sw;
    }

    public StopwatchState getNotRunningState() {
        return notRunningState;
    }

    public StopwatchState getRunningState() {
        return runningState;
    }

    public StopwatchState getPausedState() {
        return pausedState;
    }

    public StopwatchState getCurrentState() {
        return currentState;
    }

    public StopwatchState getPreviousState() {
        return previousState;
    }

    public String getPreviousStateName() {
        return previousState.getName();
    }



    public void remoteStartPressed(String clientIdentifier) {
        currentState.execute();
        context.notifyServerStartPauseResumePressed(clientIdentifier);
    }

    public void remoteStopPressed(String clientIdentifier) {

        currentState = stopPressedState;
        currentState.execute();
        context.notifyServerStopPressed(clientIdentifier);
    }

    @Override
    public void remoteOnTimeUpdated(long time) {

    }
}
