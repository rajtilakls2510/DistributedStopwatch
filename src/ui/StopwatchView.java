package ui;

import main.ApplicationController;
import stopwatch.VirtualStopwatch;
import stopwatch.Stopwatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;

public class StopwatchView {

    ApplicationController context;
    public ListView<ListItem> listView;
    JFrame frame;
    JPanel panel1, panel2, panel3, panelIndexer;
    JLabel remoteStopwatch;
    JTextField indexerIpField;
    JScrollPane scrollPane;
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
        ownerListStopwatchItem = new ListItem("Your instance: ");
        ownerListStopwatchItem.setStopwatch(new Stopwatch(context));
        panel1.add(ownerListStopwatchItem.getPanel());

        panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        remoteStopwatch = new JLabel("Remote Stopwatches");
        panel2.add(remoteStopwatch);

        panelIndexer = new JPanel();
        panelIndexer.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel hintLabel = new JLabel("Index Server IP: ");
        indexerIpField = new JTextField(ApplicationController.indexServerIp,18);
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

    public void handleNewIndexServerIp()
    {
        ApplicationController.indexServerIp = indexerIpField.getText();
        new Thread(new Runnable() {
            @Override
            public void run() {
                context.startClientWithIndexServer(ApplicationController.indexServerIp);

            }
        }).start();

    }

    public void displayIP(String ip){
        ownerListStopwatchItem.instanceDisplay.setText("Your Instance: "+ip);
    }

    public Stopwatch getOwnerStopwatch() {
        return (Stopwatch) ownerListStopwatchItem.stopwatch;
    }

    public void addRemoteStopwatch(VirtualStopwatch virtualStopwatch, String serverIdentifier) {
        ListItem newItem = new ListItem(serverIdentifier);
        newItem.setStopwatch(virtualStopwatch);
        listView.add(newItem);
        panel3.add(newItem.getPanel());
        panel3.revalidate();
        panel3.repaint();
        setFrameVisible();
    }
    public void removeRemoteVirtualStopwatch(String serverIdentifier) {

        ListItem virtualStopwatchListItem = getVirtualStopwatchListItemByName(serverIdentifier);
        if (virtualStopwatchListItem != null) {
            listView.remove(virtualStopwatchListItem);
            panel3.remove(virtualStopwatchListItem.getPanel());
            panel3.revalidate();
            panel3.repaint();

        }
        setFrameVisible();
    }

    ListItem getVirtualStopwatchListItemByName(String name) {
        for (ListItem item : listView.items) {
            if (item.name.equals(name))
                return item;
        }
        return null;
    }

    public void notifyVirtualStopwatchTimeUpdated(long time, String serverIdentifier) {
        ListItem virtualStopwatchListItem = getVirtualStopwatchListItemByName(serverIdentifier);
        if (virtualStopwatchListItem != null) {
            try {
                virtualStopwatchListItem.stopwatch.remoteOnTimeUpdated(time);
            } catch (RemoteException e) {
            }
        }
    }

    public void notifyVirtualStopwatchStartPressed(String serverIdentifier) {
        ListItem virtualStopwatchListItem = getVirtualStopwatchListItemByName(serverIdentifier);
        if (virtualStopwatchListItem != null) {
            try {
                virtualStopwatchListItem.stopwatch.remoteStartPressed(serverIdentifier);
            } catch (RemoteException e) {
            }
        }
    }

    public void notifyVirtualStopwatchStopPressed(String serverIdentifier) {
        ListItem virtualStopwatchListItem = getVirtualStopwatchListItemByName(serverIdentifier);
        if (virtualStopwatchListItem != null) {
            try {
                virtualStopwatchListItem.stopwatch.remoteStopPressed(serverIdentifier);
            } catch (RemoteException e) {
            }
        }
    }

    private void doCleanup() {
        try {
            ownerListStopwatchItem.stopwatch.stop();
        } catch (RemoteException e) {
        }
        context.cleanUp();
    }
}
