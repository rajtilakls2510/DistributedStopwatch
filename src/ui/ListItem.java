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

    public VirtualStopwatch stopwatch;
    StopwatchUIUpdater uiUpdater;
    JPanel panel;
    JLabel instanceIpDisplay;
    JLabel instanceIdentifierDisplay;
    JLabel timerDisplay;
    JButton start;
    JButton stop;
    InstanceInfo instanceInfo;

    public ListItem(InstanceInfo instanceInfo) {
        this.instanceInfo = instanceInfo;
        instanceIpDisplay = new JLabel("IP: "+instanceInfo.getHostIP());
        instanceIdentifierDisplay = new JLabel("ID: "+instanceInfo.getInstanceIdentifier());
        timerDisplay = new JLabel();
        start = new JButton("Start");
        stop = new JButton("Stop");
        panel = new JPanel();

        setUpPanel();
        addButtonListeners();
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
//        panel.setPreferredSize(new Dimension(500, 50));

        Font font = timerDisplay.getFont();
        timerDisplay.setFont(new Font(font.getFontName(), font.getStyle(), 16));

        stop.setVisible(false);

        JPanel infoPanel = new JPanel();
//        infoPanel.setPreferredSize(new Dimension(50,50));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.add(instanceIpDisplay);
        infoPanel.add(instanceIdentifierDisplay);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(infoPanel)
                        .addComponent(timerDisplay)
                        .addComponent(start)
                        .addComponent(stop)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(infoPanel)
                        .addComponent(timerDisplay)
                        .addComponent(start)
                        .addComponent(stop)
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

    public InstanceInfo getInstanceInfo()
    {
        return instanceInfo;
    }

    private void handleStartPress() {
        try {
            stopwatch.startPauseResume();
        } catch (RemoteException e) {
        }

    }

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

