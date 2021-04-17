package stopwatch;

import java.util.ArrayList;
import java.util.Iterator;

public class StopwatchEngine implements Runnable, Observable {

    /**
     * StopwatchEngine is the main class which runs the stopwatch on a background thread.
     * This class is a part of the Observer Design Pattern. This class implements Observable interface which means that for clients to
     * get time updates for this stopwatch, they have to implement Observer interface.
     * <p>
     * This Stopwatch can only be Paused and Resumed.
     * <p>
     * On pressing start(), it will resume form the point it was paused.
     * Starts from zero if the object is newly created.
     * <p>
     * On pressing stop(), it will get paused.
     */

    private long offset, currentStart;
    private boolean isStopped;
    private Thread th;
    private ArrayList<Observer> observers;

    public StopwatchEngine() {
        offset = 0L;
        currentStart = System.currentTimeMillis();
        isStopped = true;
        observers = new ArrayList<>();
    }

    /**
     * Method to resume the stopwatch from the point it was paused
     */
    public void start() {
        if (isStopped) {
            th = new Thread(this);
            th.start();
            currentStart = System.currentTimeMillis() - offset;
        }

        isStopped = false;
    }

    /**
     * Method to pause the stopwatch
     */
    public void stop() {
        if (!isStopped) {
            th = null;
            offset = System.currentTimeMillis() - currentStart;
        }
        isStopped = true;
    }

    public long getTime() {
        if (!isStopped)
            return System.currentTimeMillis() - currentStart;
        else
            return offset;
    }

    /**
     * This method runs the background thread and notifies all Observers about its time every 100 ms.
     */

    @Override
    public void run() {
        while (!isStopped) {
            notifyObservers();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method registers and Observer
     *
     * @param o Observer
     */
    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    /**
     * This method notifies all observers that are registered
     */
    @Override
    public void notifyObservers() {
        Iterator<Observer> iter = observers.iterator();
        while (iter.hasNext()) {
            Observer o = iter.next();
            o.update(getTime());
        }
    }
}