package stopwatch;

import main.ApplicationController;
import main.InstanceInfo;

import java.rmi.RemoteException;

public class Stopwatch implements Observer, VirtualStopwatch {

    StopwatchEngine sw;

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
        ApplicationController.networkThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                context.notifyServerStartPauseResumePressed();
            }
        });

    }

    public void stop() {

        currentState = stopPressedState;
        currentState.execute();
        ApplicationController.networkThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                context.notifyServerStopPressed();
            }
        });

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
        ApplicationController.networkThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                context.notifyServerStopwatchTimeChange(time);
            }
        });
    }

    public void setState(StopwatchState stopwatchState) {
        currentState = stopwatchState;
    }

    public void setPreviousState(StopwatchState previousState) {
        this.previousState = previousState;
    }

    public void setStopwatch(StopwatchEngine sw) {
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

    public void remoteStartPressed(InstanceInfo clientInfo) {
        currentState.execute();
        context.notifyServerStartPauseResumePressed(clientInfo);
    }

    public void remoteStopPressed(InstanceInfo clientInfo) {

        currentState = stopPressedState;
        currentState.execute();
        context.notifyServerStopPressed(clientInfo);
    }

    @Override
    public void remoteOnTimeUpdated(long time) {

    }

    @Override
    public InstanceInfo getInstanceInfo() throws RemoteException {
        return ApplicationController.instanceInfo;
    }
}
