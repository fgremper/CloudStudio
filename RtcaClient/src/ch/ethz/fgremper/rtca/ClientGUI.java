package ch.ethz.fgremper.rtca;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientGUI {

	private static final Logger log = LogManager.getLogger(ClientGUI.class);

    private static DefaultListModel logModel;
    private static JList logList;
    private static JScrollPane logScrollPane;
    private static JLabel trafficLabelGreen;
    private static JLabel trafficLabelYellow;
    private static JLabel trafficLabelRed;
    private static JLabel monitoringText;
    private static JLabel statusCaption;
    private static JLabel lastUpdateCaption;
    private static JLabel nextUpdateCaption;
    private static JLabel statusText;
    private static JLabel lastUpdateText;
    private static JProgressBar updateProgressBar;
    private static JButton forceUpdateButton;
    
    private static long lastUpdate = 0;
    
    private static int errorLevel = 0;
    
    private static boolean forceUpdate = false;
    
    public static void setForceUpdate(boolean b) {
    	forceUpdate = b;
    }
    
    public static boolean getForceUpdate() {
    	return forceUpdate;
    }
    
    public static void addLogMessage(String logString) {
    	// Add log message
    	logModel.addElement(logString);
    	
        // Scroll down
        JScrollBar vertical = logScrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    public static void setStatusGreen() {
    	if (trafficLabelGreen != null && trafficLabelYellow != null && trafficLabelRed != null && errorLevel == 0) {
	    	trafficLabelGreen.setVisible(true);
	    	trafficLabelYellow.setVisible(false);
	    	trafficLabelRed.setVisible(false);
	    	errorLevel = 0;
    	}
    }

    public static void setStatusYellow() {
    	if (trafficLabelGreen != null && trafficLabelYellow != null && trafficLabelRed != null && errorLevel <= 1) {
	    	trafficLabelGreen.setVisible(false);
	    	trafficLabelYellow.setVisible(true);
	    	trafficLabelRed.setVisible(false);
	    	errorLevel = 1;
    	}
    }

    public static void setStatusRed() {
    	if (trafficLabelGreen != null && trafficLabelYellow != null && trafficLabelRed != null && errorLevel <= 2) {
	    	trafficLabelGreen.setVisible(false);
	    	trafficLabelYellow.setVisible(false);
	    	trafficLabelRed.setVisible(true);
	    	errorLevel = 2;
    	}
    }
    
    public static void setLastUpdate() {
    	lastUpdate = System.currentTimeMillis();
    }

    public static void refreshLastUpdate() {
    	if (lastUpdate != 0) {
    		int elapsedSeconds = (int) ((System.currentTimeMillis() - lastUpdate) / 1000.0);
            lastUpdateText.setText(elapsedSeconds + " " + (elapsedSeconds == 1 ? "second" : "seconds") + " ago");
    	}
    }

    public static void setStatus(String s) {
        statusText.setText(s);
    }

    public static void setMonitoringText(String s) {
        monitoringText.setText(s);
    }
    
    public static void setTimeTillNextUpdate(int t) {
    	updateProgressBar.setValue(t);
    }
    
    public static void createGuiContents() {

        // Init log list and scroll pane
    	logModel = new DefaultListModel();
        logList = new JList(logModel);
        logList.setFont(new Font("Helvetica", Font.PLAIN, 12));
        logScrollPane = new JScrollPane(logList);
        logScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        logScrollPane.setBounds(6, 155, 588, 237);
        
        // Init traffic light images
        try {
	        BufferedImage trafficImageGreen = ImageIO.read(new File("TrafficGreen.png"));
	        trafficLabelGreen = new JLabel(new ImageIcon(trafficImageGreen));
	        trafficLabelGreen.setBounds(15, 15, 120, 120);
	        BufferedImage trafficImageYellow = ImageIO.read(new File("TrafficYellow.png"));
	        trafficLabelYellow = new JLabel(new ImageIcon(trafficImageYellow));
	        trafficLabelYellow.setBounds(15, 15, 120, 120);
	        BufferedImage trafficImageRed = ImageIO.read(new File("TrafficRed.png"));
	        trafficLabelRed = new JLabel(new ImageIcon(trafficImageRed));
	        trafficLabelRed.setBounds(15, 15, 120, 120);
	        setStatusGreen();
        }
        catch (Exception e) {
        	log.error("Couldn't load traffic light image.");
        }

        // Init monitoring text
        monitoringText = new JLabel("Initializing...");
        monitoringText.setFont(new Font("Helvetica", Font.PLAIN, 14));
        monitoringText.setBounds(160, 27, 400, 20);
        
        // Init status caption
        statusCaption = new JLabel("Status:");
        statusCaption.setFont(new Font("Helvetica", Font.BOLD, 14));
        statusCaption.setBounds(160, 58, 100, 20);
        
        // Init last update caption
        lastUpdateCaption = new JLabel("Last update:");
        lastUpdateCaption.setFont(new Font("Helvetica", Font.BOLD, 14));
        lastUpdateCaption.setBounds(160, 78, 100, 20);

        // Init next update caption
        nextUpdateCaption = new JLabel("Next update:");
        nextUpdateCaption.setFont(new Font("Helvetica", Font.BOLD, 14));
        nextUpdateCaption.setBounds(160, 109, 100, 20);
        
        // Init status text
        statusText = new JLabel("Initializing");
        statusText.setFont(new Font("Helvetica", Font.PLAIN, 14));
        statusText.setBounds(270, 58, 300, 20);
        
        // Init last update text
        lastUpdateText = new JLabel("Never");
        lastUpdateText.setFont(new Font("Helvetica", Font.PLAIN, 14));
        lastUpdateText.setBounds(270, 78, 300, 20);
        
        // Init update progress bar
        updateProgressBar = new JProgressBar(0, 10000);
        updateProgressBar.setValue(0);
        updateProgressBar.setBounds(270, 109, 190, 20);
        
        // Init force update button
        forceUpdateButton = new JButton("Force update");
        forceUpdateButton.setFont(new Font("Helvetica", Font.PLAIN, 12));
        forceUpdateButton.setBounds(470, 109, 100, 20);
        forceUpdateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				ClientGUI.setForceUpdate(true);
			}
    	});

    }
    
    public static void createAndShowGUI() {
    	
        // Create and set up the window (frame)
        JFrame frame = new JFrame("RTCA Client");
        frame.setSize(600, 420);
        frame.setResizable(false);
        Container pane = frame.getContentPane();
        pane.setLayout(null);
        
        // Add GUI contents
        pane.add(logScrollPane);
        pane.add(trafficLabelGreen);
        pane.add(trafficLabelYellow);
        pane.add(trafficLabelRed);
        pane.add(monitoringText);
        pane.add(statusCaption);
        pane.add(lastUpdateCaption);
        pane.add(nextUpdateCaption);
        pane.add(statusText);
        pane.add(lastUpdateText);
        pane.add(updateProgressBar);
        pane.add(forceUpdateButton);
        
        // Event listeners
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
               System.exit(0);
            }
        });
        
        // Display the window.
        frame.setVisible(true);
        
        // Always update the "last update" time
        new Thread() {
            public void run() {
            	while (true) {
					ClientGUI.refreshLastUpdate();
					try {
					    Thread.sleep(100);
					} catch (InterruptedException ex) {
					    Thread.currentThread().interrupt();
					}
            	}
            }
        }.start();
    }
}
