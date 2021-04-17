package ui;

import main.InstanceInfo;
import stopwatch.StopwatchUIUpdater;
import stopwatch.VirtualStopwatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class ListItem {

    /**
     * ListItem holds a Stopwatch and the GUI elements that are to be displayed on the JFrame.
     */

    // Stopwatch
    public VirtualStopwatch stopwatch;

    // The UI Updater
    StopwatchUIUpdater uiUpdater;

    // UI Widgets
    JPanel panel;
    JLabel instanceIpDisplay;
    JLabel instanceIdentifierDisplay;
    JLabel timerDisplay;
    JButton start;
    JButton stop;

    // Instance Info for the particular Stopwatch
    InstanceInfo instanceInfo;

    public ListItem(InstanceInfo instanceInfo) {

        // Initializing all objects
        this.instanceInfo = instanceInfo;
        instanceIpDisplay = new JLabel("IP: " + instanceInfo.getHostIP());
        instanceIdentifierDisplay = new JLabel("ID: " + instanceInfo.getInstanceIdentifier());
        timerDisplay = new JLabel();
        start = new JButton("Start");
        stop = new JButton("Stop");
        panel = new JPanel();

        setUpPanel();
        addButtonListeners();

        // Creating the UI Updater
        uiUpdater = new StopwatchUIUpdater() {
            @Override
            public void onStart() {
                start.setText("Pause");
                stop.setVisible(true);
            }

            @Override
            public void onPause() {
                start.setText("Resume");
                stop.setVisible(true);
            }

            @Override
            public void onResume() {
                start.setText("Pause");
                stop.setVisible(true);
            }

            @Override
            public void onStop() {
                timerDisplay.setText("Time: " + formatTime(0L));
                start.setText("Start");
                stop.setVisible(false);
            }

            @Override
            public void onTimeUpdate(long time) {
                timerDisplay.setText("Time: " + formatTime(time));
            }
        };

    }

    /**
     * This method sets the stopwatch for this list item along with its UI Updater.
     *
     * @param stopwatch
     */
    public void setStopwatch(VirtualStopwatch stopwatch) {
        this.stopwatch = stopwatch;
        try {
            stopwatch.setStopwatchUiUpdater(uiUpdater);
        } catch (RemoteException e) {
        }
    }

    private void setUpPanel() {
        GroupLayout layout = new GroupLayout(panel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        panel.setLayout(layout);

        Font font = timerDisplay.getFont();
        timerDisplay.setFont(new Font(font.getFontName(), font.getStyle(), 16));
        timerDisplay.setHorizontalAlignment(JLabel.CENTER);

        stop.setVisible(false);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup()
                                        .addComponent(instanceIpDisplay)
                                        .addComponent(instanceIdentifierDisplay)
                        )
                        .addComponent(timerDisplay, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(start)
                        .addComponent(stop)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup()
                                        .addComponent(instanceIpDisplay)
                                        .addComponent(timerDisplay)
                                        .addComponent(start)
                                        .addComponent(stop)
                        )
                        .addComponent(instanceIdentifierDisplay)
        );

    }

    private void addButtonListeners() {
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleStartPress();
            }
        });
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleStopPress();
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    public JLabel getTimerDisplay() {
        return timerDisplay;
    }

    public JButton getStartButton() {
        return start;
    }

    public JButton getStopButton() {
        return stop;
    }

    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }

    /**
     * This method handles the Start button pressed event
     */
    private void handleStartPress() {
        try {
            stopwatch.startPauseResume();
        } catch (RemoteException e) {
        }

    }

    /**
     * This method handles the Stop button pressed event
     */
    private void handleStopPress() {
        try {
            stopwatch.stop();
        } catch (RemoteException e) {
        }
    }

    String formatTime(long time) {
        int time1 = (int) time;
        int milis = 0, secs = 0, minutes = 0, hours = 0;
        String formattedTime = "";

        milis = time1 % 1000;
        time1 = time1 / 1000;
        secs = time1 % 60;
        time1 = time1 / 60;
        minutes = time1 % 60;
        time1 = time1 / 60;
        hours = time1 % 60;

        formattedTime = String.format("%02d", secs) + ":" + String.format("%03d", milis);
        if (minutes > 0)
            formattedTime = String.format("%02d", minutes) + ":" + formattedTime;
        if (hours > 0)
            formattedTime = String.format("%02d", hours) + ":" + formattedTime;
        return formattedTime;

    }
}

