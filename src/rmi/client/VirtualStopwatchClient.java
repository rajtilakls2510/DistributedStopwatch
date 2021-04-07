package rmi.client;

import rmi.shared.VirtualStopwatch;
import stopwatch.StopwatchUIUpdater;

public interface VirtualStopwatchClient extends VirtualStopwatch {
    void setStopwatchUiUpdater(StopwatchUIUpdater stopwatchUIUpdater);

    void remoteOnTimeUpdated(long time);
}
