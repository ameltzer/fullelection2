package election_map_viewer.events;

import java.awt.event.*;

import election_map_viewer.ElectionMapDataModel;
/**
 * Used for managing the event handling for mouse-overs of states or 
 * counties that we can then hightlight.
 * 
 * @author Richard McKenna
 */
public class ElectionMapMouseOverShapeHandler implements MouseMotionListener
{
	// WE'LL NEED TO UPDATE THE DATA
	private ElectionMapDataModel dataModel;
	
	/**
	 * The constructor sets up everything for use.
	 */
	public ElectionMapMouseOverShapeHandler(ElectionMapDataModel initDataModel)
	{
		dataModel = initDataModel;
	}
	
	/**
	 * This method responds to mouse movement by testing to
	 * see if the mouse is currently over a shape. If it is,
	 * that shape is highlighted. Note that the data model
	 * provides this service, since it manages all the data.
	 */
	public void mouseMoved(MouseEvent me) 
	{
		// GET THE CURRENT MOUSE LOCATION
		int x = me.getX();
		int y = me.getY();
		dataModel.highlightMapRegion(x, y);
	}

	// WE WON'T USE THIS ONE
	public void mouseDragged(MouseEvent me) {}
}