package ch.ethz.fgremper.rtca;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * RTCA client main class.
 * @author Fabian Gremper
 */
public class ClientMain {

	private static final Logger log = LogManager.getLogger(ClientMain.class);

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("RTCA Client");
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
               System.exit(0);
            }        
         });
        
        frame.setSize(400,400);
        
        DefaultListModel fruitsName = new DefaultListModel();

        fruitsName.addElement("Apple");
        fruitsName.addElement("Grapes");
        fruitsName.addElement("Mango");
        fruitsName.addElement("Peer");

        try {
	        BufferedImage myPicture = ImageIO.read(new File("traffic-light-all.png"));
	        JLabel picLabel = new JLabel(new ImageIcon(myPicture));
	        frame.add(picLabel);
        }
        catch (Exception e) {
        	// 
        }
        
        JList fruitList = new JList(fruitsName);
        
        JScrollPane fruitListScrollPane = new JScrollPane(fruitList); 

        frame.add(fruitListScrollPane);
        
        //Add the ubiquitous "Hello World" label.
        //JLabel label = new JLabel("Hello World");
        //frame.getContentPane().add(label);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
	/**
	 * Run the RTCA client.
	 * @param args Filename of config XML can be specified as first argument (default is "config.xml")
	 * @throws Exception
	 */
	public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
		/*
		
		ClientConfig config;
		String sessionId;

		// Starting
		log.info("RTCA client starting...");
		
		// Read the config XML
		try {
			log.info("Reading config...");
			String configFileName = "config.xml";
			if (args.length >= 1) configFileName = args[0];
			config = new ClientConfigReader(configFileName).getConfig();
		}
		catch (Exception e) {
			log.error("Error while reading config file: " + e.getMessage());
			return;
		}
		
		// Get our HTTP client
		HttpClient httpClient = new HttpClient();

		// Login and get a session ID
		log.info("Login and requesting session ID...");
		try {
			sessionId = httpClient.login(config.serverUrl, config.username, config.password);
		}
		catch (Exception e) {
			log.error("Error while requesting session ID: " + e.getMessage());
			return;
		}
		log.info("Retrieved session ID: " + sessionId);
		
		// Keep updating the RTCA server
		while (true) {

			// For all repositories we're going to read the local data and send some of it to the server
			for (RepositoryInfo repositoryInfo : config.repositoriesList) {	
				
				try {
					
					log.info("Reading and sending repository \"" + repositoryInfo.alias + "\" at " + repositoryInfo.localPath);
					
					// Read repository info
					RepositoryReader repositoryReader = new RepositoryReader(repositoryInfo.localPath);
					JSONObject updateObject = repositoryReader.getUpdateObject();
					
			        // Store user information
					updateObject.put("sessionId", sessionId);
					updateObject.put("repositoryAlias", repositoryInfo.alias);
					
					// Send it to to the server
					String jsonString = updateObject.toString();
					httpClient.sendGitState(config.serverUrl, jsonString);
				
				}
				catch (Exception e) {
					log.error("Error while reading/sending local git state for " + repositoryInfo.alias + ": " + e.getMessage());
				}
				
			}
			
			// If interval is 0, we only submit once, otherwise wait and repeat periodically
			if (config.resubmitInterval == 0) {
				break;
			}
			else {
				log.info("Waiting " + config.resubmitInterval + " seconds...");
				try {
				    Thread.sleep(config.resubmitInterval * 1000);
				} catch (InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			}
		}

		log.info("RTCA client stopping...");
		
		*/
		
	}

}
