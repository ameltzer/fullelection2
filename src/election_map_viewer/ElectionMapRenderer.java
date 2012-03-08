package election_map_viewer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Iterator;
import javax.swing.JPanel;

import shp_framework.SHPMap;
import shp_framework.geometry.SHPPolygon;
import shp_framework.geometry.SHPShape;
import static election_map_viewer.ElectionMapDataModel.*;
/**
 * This class does all the map rendering inside of a panel (itself). 
 * It knows how to render the shapes as well as the map legend and
 * title. It also manages the viewport, which means the window on the 
 * map that we are currently viewing, which means it manages scale
 * and the viewport location. Rendering settings like fonts and strokes
 * should also be managed here.
 * 
 * @author Richard McKenna
 **/
public class ElectionMapRenderer extends JPanel
{
	// THE APP, WE NEED IT TO SHARE INFO 
	private ElectionMapDataModel dataModel;
	
	// VIEWPORT DATA IS USED FOR ZOOMING IN AND OUT
	// AND VIEWING ONLY A PORTION OF THE MAP
	private double viewportCenterX;
	private double viewportCenterY;
	private double scale;
	
	// THIS HELPS US TO PROVIDE PADDING AROUND THE MAP WE ZOOM TO
	public static final double SCALE_MAP_DOWN_FACTOR = 0.8;
	
	// AND THE STYLES WE'LL USE FOR RENDERING THE MAP LINES
	public static final BasicStroke DEFAULT_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
	public static final BasicStroke OUTLINED_POLYGON_STROKE = new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);

	// FONT SETTINGS FOR RENDERING
	public static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 36);
	
	// COLOR SETTINGS FOR RENDERING
	public static final Color DEFAULT_BACKGROUND_COLOR = new Color(100, 100, 255);
	public static final Color DEFAULT_BORDER_COLOR = Color.BLACK;
	public static final Color DEFAULT_HIGHLIGHT_COLOR = Color.CYAN;
	public static final Color TITLE_COLOR = new Color(0, 0, 80);
		
	/**
	 * This constructor sets up all the rendering settings and gets
	 * ready to render the maps as provided by the data model.
	 */
	public ElectionMapRenderer(ElectionMapDataModel initDataModel)
	{
		// WE'LL NEED THIS DURING RENDERING TO GET WHAT TO RENDER
		dataModel = initDataModel;
		
		// DEFAULT VIEWPORT STUFF, WHICH WILL BE CHANGED AS SOON
		// AS A MAP IS LOADED
		viewportCenterX = 0;
		viewportCenterY = 0;
		scale = 1;
		
		// SOME DEFAULT SETUP STUFF
		setBackground(DEFAULT_BACKGROUND_COLOR);
	}
	
	// ACCESSOR METHODS
	public double getViewportCenterX() { return viewportCenterX; }
	public double getViewportCenterY() { return viewportCenterY; }

	/*** RENDERING METHODS ***/

	/**
	 * Called by the Swing and AWT libraries every time our panel is 
	 * displayed and then when we repaint it. All needed rendering
	 * on the panel must be done here each time.
	 */
	public void paintComponent(Graphics g)
	{
		// CLEARS THE PANEL TO THE BACKGROUND COLOR (BLUE FOR THE OCEAN)
		super.paintComponent(g);
		renderLegend(g, dataModel.getCurrentMapAbbr(), new File(ElectionMapFileManager.USA_DBF));
		// ONLY RENDER IF THERE IS ACTUALLY A MAP TO DRAW
		if (dataModel.isMapLoaded())
		{
			// ONLY ZOOM TO A MAP ONCE
			if (!dataModel.isMapRendered())
			{
				// ZOOM
				zoomToMapBounds();
				
				// AND MAKE SURE WE DON'T ZOOM FOR THIS MAP AGAIN
				dataModel.setMapRendered(true);
			}

			// ONLY RENDER THE MAP IF THE USER HAS SELECTED IT
			renderMap(g);
		
			// NOW THE MAP TITLE
			renderTitle(g);
		}
	}	
	
	/**
	 * Renders the actual map, meaning polygon outlines and filling.
	 */
	public void renderMap(Graphics g)
	{
		// WE'LL USE Graphics2D METHODS
		Graphics2D g2 = (Graphics2D)g;
		SHPMap mapData = dataModel.getCurrentSHP();
		if (mapData == null)
			return;
		Iterator<SHPShape> shapesIt = mapData.shapesIterator();

		// RENDER ALL THE SHAPES
		while (shapesIt.hasNext())
		{
			// GET THE SHAPE
			SHPShape shape = shapesIt.next();
			
			// AND RENDER IT
			shape.render(g2, scale, viewportCenterX, viewportCenterY, getWidth(), getHeight());
			
			// ALWAYS SET THE COLOR BACK TO BLACK (THE DEFAULT)
			g2.setColor(Color.black);
		}

		SHPPolygon highlightedPolygon = dataModel.getHighlightedPolygon();
		// WE WILL OUTLINE THE PART THAT THE POLYGON IS OVER
		if (highlightedPolygon != null)
		{
			g2.setStroke(OUTLINED_POLYGON_STROKE);
			highlightedPolygon.render(g2, scale, viewportCenterX, viewportCenterY, getWidth(), getHeight());
		}
	}
	public void renderLegend(Graphics g, String key, File file){
		Graphics2D g2 = (Graphics2D)g;
		g2.drawRect(1000, 600, 260, 150);
		g2.setColor(new Color(248,248,255));
		g2.fillRect(1001, 601, 259, 149);
		g2.setColor(Color.BLACK);
		g2.setFont(new Font("Times New Roman", Font.BOLD, 20));
		g2.drawString(key, 1005, 625);
		Image flag = dataModel.getMiniFlags().get(key);
		g2.drawImage(flag, 1005, 630, null);
		g2.setFont(new Font("Times New Roman", Font.PLAIN, 10));
		try {
			for(int i=2; i<5; i++){
				String toRender= this.dataModel.determineVotes(g2,file, i);
				g2 = this.dataModel.graphicsString(i, g2);
				g2.drawString(toRender, 1005, 670 + ((i-1) *15));
			}
		} 
		catch (IOException e){
			e.printStackTrace();
		}
		g2.setColor(Color.BLACK);
		g2.drawLine(1005, 720, 1250, 720);
		try {
			g2.drawString(dataModel.totalVotes(file), 1005, 735);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	/** 
	 * Renders the title and flag of the current map according to
	 * the data model. These are rendered at the top of the map.
	 */
	public void renderTitle(Graphics g)
	{
		// RENDER THE USA OR STATE FLAG IN THE TOP LEFT
		Image flag = dataModel.getCurrentFlag();
		g.drawImage(flag, 10, 10, null);
		
		// GET THE TITLE OF THE MAP TO RENDER
		String title = dataModel.getCurrentMapName() + MAP_TITLE;
		
		// SWITCH RENDERING SETTINGS FOR THE TITLE
		g.setFont(TITLE_FONT);
		g.setColor(TITLE_COLOR);
		
		// AND MEASURE IT USING THE CURRENT RENDERING SETTINGS
		FontMetrics fm = g.getFontMetrics();
		int titleWidth = fm.stringWidth(title);
		int titleHeight = fm.getHeight();
		
		// NOW LET'S CENTER THE TITLE
		int canvasWidth = getWidth();
		int titleX = (canvasWidth/2) - (titleWidth/2);
		int titleY = titleHeight;
		
		// AND RENDER IT
		g.drawString(title, titleX, titleY);
	}
	
	// VIEWPORT SETUP FUNCTIONS
	
	/**
	 * This method makes sure the viewport has not gone off the map. If the
	 * latitude or longitude is illegal, it gets corrected.
	 */
	public void correctViewport()
	{
		double longWidth = 360/scale;
		double minLong = viewportCenterX - (longWidth/2);
		double maxLong = viewportCenterX + (longWidth/2);

		double latHeight = 180/scale;
		double minLat = viewportCenterY - (latHeight/2);
		double maxLat = viewportCenterY + (latHeight/2);
	
		// CORRECT VIEWPORT IF IT WENT OFF THE WESTERN EDGE
		double diff = minLong - (-180);
		if (diff < 0)
		{
			viewportCenterX -= diff;
		}
		// OR OFF THE EASTERN EDGE
		else
		{
			diff = 180 - maxLong;
			if (diff < 0)
			{
				viewportCenterX += diff;
			}
		}

		// OR OFF THE SOUTHERN EDGE
		diff = minLat - (-90);
		if (diff < 0)
		{
			viewportCenterY -= diff;			
		}
		// OR THE NORTHERN EDGE
		else
		{
			diff = 90 - maxLat;
			if (diff < 0)
			{
				viewportCenterY += diff;
			}
		}
	}	

	/**
	 * This method moves the viewport to the arguments location,
	 * correcting if necessary.
	 */
	public void setCenter(double longCenter, double latCenter)
	{
		viewportCenterX = longCenter;
		viewportCenterY = latCenter;
		correctViewport();
	}

	/**
	 * This method moves the viewport to the pixel location. Note that this 
	 * method takes pixel locations x and y and must convert them 
	 * into corresponding lat/lon values.
	 */
	public void setCenter(int x, int y)
	{
		double longWidth = 360/scale;
		double latHeight = 180/scale;
		double longPerPixel = longWidth/getWidth();
		double latPerPixel = latHeight/getHeight();
		
		double longDist = (x - (getWidth()/2)) * longPerPixel;
		double latDist = ((getHeight()/2) - y) * latPerPixel;
		
		double xLong = longDist + viewportCenterX;
		double yLong = latDist + viewportCenterY;
		
		viewportCenterX = xLong;
		viewportCenterY = yLong;
		correctViewport();
	}

	// ZOOM FUNCTIONS
	
	/**
	 * This method will zoom our viewport in on the region
	 * represented by the x,y arguments. Note that these are
	 * pixel coordinates, so we must convert to lat,long. Also
	 * note that we have to determine the proper scale.
	 */
	public void zoom(int x1, int y1, int x2, int y2)
	{
		// ONLY ZOOM IF THIS IS A VALID ZOOM RECTANGLE
		if ((x2 > x1) && (y2 > y1))
		{			
			double longWidth = 360/scale;
			double latHeight = 180/scale;
			double longPerPixel = longWidth/getWidth();
			double latPerPixel = latHeight/getHeight();
			
			double longDist1 = (x1 - (getWidth()/2)) * longPerPixel;
			double longDist2 = (x2 - (getWidth()/2)) * longPerPixel;
			double latDist1 = ((getHeight()/2) - y1) * latPerPixel;
			double latDist2 = ((getHeight()/2) - y2) * latPerPixel;
			
			double long1 = longDist1 + viewportCenterX;
			double long2 = longDist2 + viewportCenterX;
			double lat1 = latDist1 + viewportCenterY;
			double lat2 = latDist2 + viewportCenterY;
			
			viewportCenterX = ((long1 + long2)/2);
			viewportCenterY = ((lat1 + lat2)/2);

			// THE PROVIDED RECT WILL LIKELY NOT HAVE THE SAME ASPECT
			// RATIO OF THE VIEWPORT, SO LET'S FIGURE OUT 
			// THE PROPER SCALING FACTORS FOR X AND Y
			double scaleX = 360/(long2-long1);
			double scaleY = 180/(lat1-lat2);
			if (scaleX > scaleY)
				scale = scaleY;
			else
				scale = scaleX;
			
			// AND NOW LET'S ZOOM OUT SLIGHTLY TO
			// MAKE IT LOOK A LITTLE BETTER
			scale *= SCALE_MAP_DOWN_FACTOR;

			// CORRECT THE VIEWPORT IF NECESSARY, WE DON'T WANT TO SCALE OFF THE MAP
			correctViewport();
		}
	}

	/**
	 * This can be used to zoom to perfectly fit the bounding box of
	 * a shape, so the viewport moves right to a region.
	 */
	public void zoomToMapBounds()
	{
		SHPMap mapData = dataModel.getCurrentSHP();
		double[] mapBounds = mapData.getShapefileData().getMBR();
		int x1 = xCoordinateToPixel(mapBounds[0]);
		int y1 = yCoordinateToPixel(mapBounds[3]);
		int x2 = xCoordinateToPixel(mapBounds[2]);
		int y2 = yCoordinateToPixel(mapBounds[1]);
		zoom(x1, y1, x2, y2);
	}

	/*** CONVERSION FUNCTIONS ***/
	
	/**
	 * This calculates and returns the longitude value that
	 * corresponds to the xPixel argument.
	 */
	public double pixelToXCoordinate(int xPixel)
	{
		double longWidth = 360/scale;
		double minLong = viewportCenterX - (longWidth/2);
	
		// SCALE THE PIXEL
		double percentLong = ((double)xPixel)/getWidth();
		double xLong = minLong + (percentLong * longWidth);
		return xLong;
	}

	/**
	 * This calculates and returns the latitude value that
	 * corresponds to the yPixel argument.
	 */
	public double pixelToYCoordinate(int yPixel)
	{
		double latHeight = 180/scale;
		double minLat = viewportCenterY - (latHeight/2);
	
		// SCALE THE PIXEL
		double percentLat = ((double)yPixel)/getHeight();
		double yLat = minLat + (percentLat * latHeight);
		return yLat;
	}

	/**
	 * This calculates and returns the x pixel value that
	 * corresponds to the xCoord longitude argument.
	 */
	public int xCoordinateToPixel(double xCoord)
	{
		double longWidth = 360/scale;
		double minLong = viewportCenterX - (longWidth/2);
	
		// SCALE THE COORDINATE
		double percentX = (xCoord - minLong)/longWidth;
		int panelWidth = getWidth();
		double coord = panelWidth * percentX;
		return (int)Math.round(coord);
	}

	/**
	 * This calculates and returns the y pixel value that
	 * corresponds to the yCoord latitude argument.
	 */
	public int yCoordinateToPixel(double yCoord)
	{
		double latHeight = 180/scale;
		double minLat = viewportCenterY - (latHeight/2);
	
		// SCALE THE COORDINATE
		double percentY = (yCoord - minLat)/latHeight;
		yCoord = getHeight() * percentY;
		return getHeight() - ((int)yCoord);
	}
}