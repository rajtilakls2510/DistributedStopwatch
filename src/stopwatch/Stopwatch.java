package stopwatch;

import main.ApplicationController;
import main.InstanceInfo;

import java.rmi.RemoteException;

public class Stopwatch implements Observer, VirtualStopwatch, StopwatchState.StopwatchStateUser {

    /**
     * Stopwatch is the class which uses the StopwatchEngine to provide higher level abstractions to let users to communicate with the StopwatchEngine
     */

    StopwatchEngine sw;

    // Stores the possible states of the Stopwatch
    StopwatchState notRunningState;
    StopwatchState runningState;
    StopwatchState pausedState;
    StopwatchState stopPressedState;

    // Stores the current state and previous state
    StopwatchState currentState;
    StopwatchState previousState;

    // The UI updater which is used to update the UI when a button is pressed (or other interactions).
    StopwatchUIUpdater stopwatchUIUpdater;
    transient ApplicationController context;

    public Stopwatch(ApplicationController context) {
        this.context = context;

    }

    // <----------------------------- Interface methods -------------------->

    /**
     * This method is used to set the UI updater for the stopwatch
     *
     * @param stopwatchUIUpdater
     */

    @Override
    public void setStopwatchUiUpdater(StopwatchUIUpdater stopwatchUIUpdater) {
        this.stopwatchUIUpdater = stopwatchUIUpdater;

        // Initializing all the states
        notRunningState = new NotRunningStopwatchState(this, this.stopwatchUIUpdater, "NOT_RUNNING");
        runningState = new RunningStopwatchState(this, this.stopwatchUIUpdater, "RUNNING");
        pausedState = new PausedStopwatchState(this, this.stopwatchUIUpdater, "PAUSED");
        stopPressedState = new InitializerStopwatchState(this, this.stopwatchUIUpdater, "STOP_PRESSED");

        // Making the StopPressed State as the current and previous state
        previousState = stopPressedState;
        currentState = stopPressedState;

        currentState.execute();
    }

    public void startPauseResume() {

        // When the Start/Pause/Resume button is pressed, change the state of the UI
        currentState.execute();

        /* Notify the application controller that the state has changed which forwards the
         notification to the server and then it goes away to all remote clients
        */

        ApplicationController.networkThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                context.notifyServerStartPauseResumePressed();
            }
        });

    }

    public void stop() {

        // When the Stop button is pressed, change the state of the UI
        currentState = stopPressedState;
        currentState.execute();

        /* Notify the application controller that the state has changed which forwards the
         notification to the server and then it goes away to all remote clients
        */

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

        /* When the time updates are received from the StopwatchEngine, update the UI and notify the
         Application Controller to forward the updated time to all remote clients using the server
        */
        stopwatchUIUpdater.onTimeUpdate(time);
        ApplicationController.networkThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                context.notifyServerStopwatchTimeChange(time);
            }
        });
    }

    @Override
    public void remoteStartPressed(InstanceInfo clientInfo) {
        currentState.execute();
        context.notifyServerStartPauseResumePressed(clientInfo);
    }

    @Override
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

    // <-------------------------------- State update helper methods --------------------------->

    @Override
    public StopwatchState getCurrentState() {
        return currentState;
    }

    @Override
    public void setCurrentState(StopwatchState stopwatchState) {
        currentState = stopwatchState;
    }

    @Override
    public StopwatchState getPreviousState() {
        return previousState;
    }

    @Override
    public void setPreviousState(StopwatchState previousState) {
        this.previousState = previousState;
    }

    @Override
    public String getPreviousStateName() {
        return previousState.getName();
    }

    @Override
    public StopwatchEngine getStopwatchEngine() {
        return sw;
    }

    @Override
    public void setStopwatchEngine(StopwatchEngine sw) {
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

}
