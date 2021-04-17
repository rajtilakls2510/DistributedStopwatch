package stopwatch;

public interface Observer {

    /**
     * This interface should be implemented by all classes that want to observe StopwatchEngine
     * @param time
     */

    // This method is called by the Observable when there is a new time.
    public void update(long time);
}
