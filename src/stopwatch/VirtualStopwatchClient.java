package stopwatch;

import stopwatch.VirtualStopwatch;
import stopwatch.StopwatchUIUpdater;

public interface VirtualStopwatchClient extends VirtualStopwatch {
    void setStopwatchUiUpdater(StopwatchUIUpdater stopwatchUIUpdater);

    void remoteOnTimeUpdated(long time);
}
