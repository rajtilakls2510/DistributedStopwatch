package stopwatch;

public interface StopwatchState {

    /**
     * StopwatchState is the common abstraction over all the states that can be there for a Stopwatch.
     * This class is the part of the State Pattern.
     */

    default void execute() {
        handleStopwatch();
        handleUI();
        changeState();
    }

    public void handleStopwatch();

    public void handleUI();

    public void changeState();

    String getName();

    interface StopwatchStateUser
    {

        /**
         * StopwatchStateUser is the interface which is used to provide some common functionality which the Individual StopwatchStates are going
         * to use when implementing the State Pattern. Therefore, every Stopwatch class that wants to use the state pattern of StopwatchState must
         * implement this nested interface.
         */

        public StopwatchState getCurrentState();

        public void setCurrentState(StopwatchState stopwatchState);

        public StopwatchState getPreviousState();

        public void setPreviousState(StopwatchState previousState);

        public String getPreviousStateName();

        public StopwatchEngine getStopwatchEngine();

        public void setStopwatchEngine(StopwatchEngine sw);
    }
}
