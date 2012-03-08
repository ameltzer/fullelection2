package election_map_viewer.events;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import election_map_viewer.ElectionMapFileManager;
import election_map_viewer.ElectionMapViewer;
/**
 * This event handler responds when the user presses the X on the frame.
 * Note that we are extending the Java API's WindowAdapter class, which is
 * just a simple class that implements the WindowListener interface and 
 * provides empty method definitions. The purpose of doing so is that 
 * we can extend it and only define the methods that we need to override,
 * leaving out the other WindowListener methods, rather than defining
 * our own empty ones.
 * 
 * @author McKilla Gorilla
 */
public class ElectionMapWindowHandler extends WindowAdapter
{
	// WE'LL NEED THIS FOR LATER
	private ElectionMapViewer view;

	/**
	 * Constructor stores the view for use later.
	 */
	public ElectionMapWindowHandler(ElectionMapViewer initView)
	{
		view = initView;
	}
	
	/**
	 * Called by AWT/Swing in response to the user clicking the X
	 * on the frame. Our response should be to verify a quit with
	 * the user and then actually quit the application.
	 */
	public void windowClosing(WindowEvent we)
	{
		// CURRENTLY THIS DOES NOTHING
	}
}
