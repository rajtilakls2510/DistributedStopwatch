package ui;

import main.ApplicationController;
import main.InstanceInfo;
import stopwatch.Stopwatch;
import stopwatch.VirtualStopwatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;

public class StopwatchView {

    /**
     * StopwatchView is the class responsible for displaying the GUI of this application.
     */

    ApplicationController context;
    public ListView<ListItem> listView;
    JFrame frame;
    JPanel panel1, panel2, panel3, panelIndexer;
    JLabel remoteStopwatch;
    JTextField indexerIpField;
    JScrollPane scrollPane;

    // Owner Stopwatch
    public ListItem ownerListStopwatchItem;

    public StopwatchView(ApplicationController context) {
        this.context = context;
        listView = new ListView<>();
        initializeJFrame();
        setUpPanels();
        setFrameVisible();
        System.out.println("Started Stopwatch!");
    }

    public void initializeJFrame() {
        // Initializing the JFrame
        frame = new JFrame("Distributed Stopwatch") {
            protected void processWindowEvent(WindowEvent e) {
                if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                    doCleanup();
                    super.processWindowEvent(e);
                } else {
                    super.processWindowEvent(e);
                }
            }
        };
        frame.setSize(600, 600);
        frame.setMinimumSize(new Dimension(400, 400));
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void setUpPanels() {

        panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.CENTER));
        ownerListStopwatchItem = new ListItem(ApplicationController.instanceInfo);
        ownerListStopwatchItem.setStopwatch(new Stopwatch(context));
        panel1.add(ownerListStopwatchItem.getPanel());

        panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        remoteStopwatch = new JLabel("Remote Stopwatches");
        panel2.add(remoteStopwatch);

        panelIndexer = new JPanel();
        panelIndexer.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel hintLabel = new JLabel("Index Server IP: ");
        indexerIpField = new JTextField(ApplicationController.indexServerIp, 14);
        JButton indexerSelectButton = new JButton("Select");
        indexerSelectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNewIndexServerIp();
            }
        });

        panelIndexer.add(hintLabel);
        panelIndexer.add(indexerIpField);
        panelIndexer.add(indexerSelectButton);

        panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(panel3);
        scrollPane.setPreferredSize(new Dimension(600, 600));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        frame.add(panel1);
        frame.add(panelIndexer);
        frame.add(panel2);
        frame.add(scrollPane);
    }

    public void setFrameVisible() {
        for (ListItem item : listView.items)
            panel3.add(item.getPanel());
        frame.setVisible(true);
    }

    /**
     * This method is responsible for registering to a new Index Server when the Select button is pressed with a new Index Server IP
     */
    public void handleNewIndexServerIp() {
        ApplicationController.indexServerIp = indexerIpField.getText();
        new Thread(new Runnable() {
            @Override
            public void run() {
                context.startClientWithIndexServer(ApplicationController.indexServerIp);

            }
        }).start();

    }

    /**
     * Method responsible for displaying the IP and ID of this application when they are ready
     *
     * @param instanceInfo
     */
    public void displayInstanceInfo(InstanceInfo instanceInfo) {
        ownerListStopwatchItem.instanceIdentifierDisplay.setText("Your ID: " + instanceInfo.getInstanceIdentifier());
        ownerListStopwatchItem.instanceIpDisplay.setText("Your IP: " + instanceInfo.getHostIP());
    }

    public Stopwatch getOwnerStopwatch() {
        return (Stopwatch) ownerListStopwatchItem.stopwatch;
    }

    /**
     * This method adds a new virtual stopwatch to the list when one is received from another instance
     *
     * @param virtualStopwatch
     * @param serverInfo
     */
    public void addRemoteStopwatch(VirtualStopwatch virtualStopwatch, InstanceInfo serverInfo) {
        ListItem newItem = new ListItem(serverInfo);
        newItem.setStopwatch(virtualStopwatch);
        listView.add(newItem);
        panel3.add(newItem.getPanel());
        panel3.revalidate();
        panel3.repaint();
        setFrameVisible();
    }

    /**
     * This method removes a virtual Stopwatch from the list
     *
     * @param serverInfo
     */
    public void removeRemoteVirtualStopwatch(InstanceInfo serverInfo) {

        ListItem virtualStopwatchListItem = getVirtualStopwatchListItemByIdentifier(serverInfo);
        if (virtualStopwatchListItem != null) {
            listView.remove(virtualStopwatchListItem);
            panel3.remove(virtualStopwatchListItem.getPanel());
            panel3.revalidate();
            panel3.repaint();

        }
        setFrameVisible();
    }

    /**
     * This method gets the list item using the instance information
     *
     * @param serverInfo
     * @return ListItem
     */
    ListItem getVirtualStopwatchListItemByIdentifier(InstanceInfo serverInfo) {
        for (ListItem item : listView.items) {
            if (item.getInstanceInfo().getInstanceIdentifier().equals(serverInfo.getInstanceIdentifier()))
                return item;
        }
        return null;
    }

    /**
     * When a new time is received for a stopwatch, it is displayed using the instance info
     *
     * @param time
     * @param serverInfo
     */
    public void notifyVirtualStopwatchTimeUpdated(long time, InstanceInfo serverInfo) {
        ListItem virtualStopwatchListItem = getVirtualStopwatchListItemByIdentifier(serverInfo);
        if (virtualStopwatchListItem != null) {
            try {
                virtualStopwatchListItem.stopwatch.remoteOnTimeUpdated(time);
            } catch (RemoteException e) {
            }
        }
    }

    /**
     * When the start button for remote stopwatch is pressed, this method is called to update the UI
     *
     * @param serverInfo
     */
    public void notifyVirtualStopwatchStartPressed(InstanceInfo serverInfo) {
        ListItem virtualStopwatchListItem = getVirtualStopwatchListItemByIdentifier(serverInfo);
        if (virtualStopwatchListItem != null) {
            try {
                virtualStopwatchListItem.stopwatch.remoteStartPressed(serverInfo);
            } catch (RemoteException e) {
            }
        }
    }

    /**
     * When the stop button for remote stopwatch is pressed, this method is called to update the UI
     *
     * @param serverInfo
     */
    public void notifyVirtualStopwatchStopPressed(InstanceInfo serverInfo) {
        ListItem virtualStopwatchListItem = getVirtualStopwatchListItemByIdentifier(serverInfo);
        if (virtualStopwatchListItem != null) {
            try {
                virtualStopwatchListItem.stopwatch.remoteStopPressed(serverInfo);
            } catch (RemoteException e) {
            }
        }
    }

    /**
     * This method is responsible for doing the clean up tasks when the user closes the application
     */
    private void doCleanup() {
        try {
            ownerListStopwatchItem.stopwatch.stop();
        } catch (RemoteException e) {
        }

        // Asks the application controller to stop the server and client and close itself
        context.cleanUp();
    }
}
