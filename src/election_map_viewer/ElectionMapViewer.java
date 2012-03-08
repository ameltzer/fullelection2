package election_map_viewer;

import java.awt.*;
import javax.swing.*;

import election_map_viewer.events.ElectionMapKeyHandler;
import election_map_viewer.events.ElectionMapMouseOverShapeHandler;
import election_map_viewer.events.ElectionMapWindowHandler;

/**
 * This application allows the user to view national and individual
 * state election results for the 2008 US Presidential Elections.
 * 
 * @author Richard McKenna
 */
public class ElectionMapViewer extends JFrame
{
	// THIS CAN LOAD OUR DATA
	private ElectionMapFileManager fileManager;
	
	// HERE'S THE DATA
	private ElectionMapDataModel dataModel;
	
	// FOR RENDERING THE MAP
	private ElectionMapRenderer renderer;

	/**
	 * This constructor sets up the GUI, including loading the
	 * USA map. Note that the state maps are only loaded upon 
	 * user request.
	 */
	public ElectionMapViewer()
	{
		// INIT OUR APPLICATION
		initWindow();
		initData();
		layoutGUI();
		initHandlers();
		
		// AND THEN SETUP THE DATA MODEL
		dataModel.init(renderer, fileManager);
	}
	
	// ACCESSOR METHOD
	public ElectionMapDataModel getDataModel() 		{ return dataModel; 	}
	public ElectionMapFileManager getFileManager()	{ return fileManager; 	}
	public ElectionMapRenderer	getRenderer()		{ return renderer;		}
	
	/**
	 * Initializes our GUI's window.
	 */
	public void initWindow()
	{
		// GIVE THE WINDOW A TITLE FOR THE TITLE BAR
		setTitle("Election Map Viewer");
		
		// MAXIMIZE IT TO FIT THE SCREEN
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		// WE'LL HANDLE WINDOW CLOSING OURSELF, SO MAKE
		// SURE NOTHING IS DONE BY DEFAULT
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	/**
	 * Initializes the data manager and the file manager used by our app.
	 * Note that the data model is still not ready for use after this method,
	 * it will still need a constructed renderer.
	 */
	public void initData()
	{
		// AND LOAD THE DATA MANAGEMENT CLASS
		dataModel = new ElectionMapDataModel();		

		// SETUP THE CLASS THAT LOADS DBF, SHP, and IMAGE FILES
		fileManager = new ElectionMapFileManager(dataModel);
		fileManager.loadAppIcon(this);
		fileManager.loadAllFlags(this);
		fileManager.loadUSAMap(this);
	}

	/**
	 * We are only using a single panel to render everything.
	 */
	public void layoutGUI()
	{
		renderer = new ElectionMapRenderer(dataModel);
		add(renderer, BorderLayout.CENTER);
	}
	
	/**
	 * This initializes and registers all event handlers. Note that
	 * we are using a MouseListener to listen for mouse clicks, and
	 * a MouseMotionListener to listen for mouse motion.
	 */
	public void initHandlers()
	{
		// THIS WILL LISTEN FOR KEY PRESSES SO THE USER CAN 
		// EASILY MOVE THE VIEWPORT AROUND AND ZOOM IN AND OUT
		ElectionMapKeyHandler kh = new ElectionMapKeyHandler(dataModel);
		this.addKeyListener(kh);
		renderer.addKeyListener(kh);
		this.setFocusable(true);
		renderer.setFocusable(true);

		// THIS WILL LISTEN FOR MOUSE MOVEMENT TO IMPLEMENT POLYGON HIGHLIGHTING
		ElectionMapMouseOverShapeHandler mosh = new ElectionMapMouseOverShapeHandler(dataModel);
		renderer.addMouseMotionListener(mosh);
		
		// THIS WILL HANDLE MOUSE CLICKS ON THE WINDOW'S X
		ElectionMapWindowHandler emwh = new ElectionMapWindowHandler(this);
		addWindowListener(emwh);
	}
	
	/**
	 * This main method starts the application.
	 */
	public static void main(String[] args)
	{
		// SETUP THE WINDOW
		ElectionMapViewer frame = new ElectionMapViewer();
		
		// DISPLAY THE WINDOW
		frame.setVisible(true);
		// WE'LL WAIT UNTIL WE KNOW THE SIZE OF THE RENDERING
		// PANEL BEFORE WE ZOOM IN ON THE MAP
		while (frame.renderer.getWidth() <= 0)
		{
			try { Thread.sleep(10); }
			catch(InterruptedException ie) { ie.printStackTrace(); }
		}
		frame.renderer.repaint();
	}
}