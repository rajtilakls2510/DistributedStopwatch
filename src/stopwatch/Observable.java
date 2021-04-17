package stopwatch;

public interface Observable {

    /**
     * This interface is implemented by the StopwatchEngine class and is the part of Observer Design Pattern
     * @param o
     */

    public void registerObserver(Observer o);

    public void notifyObservers();
}
