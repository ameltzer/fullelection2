package election_map_viewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Iterator;
import java.util.TreeMap;

import dbf_framework.DBFFileIO;
import dbf_framework.DBFRecord;
import dbf_framework.DBFTable;

import shp_framework.SHPData;
import shp_framework.SHPMap;
import shp_framework.geometry.SHPPolygon;
import shp_framework.geometry.SHPShape;

import static election_map_viewer.ElectionMapRenderer.*;
/**
 * This class serves as the data manager for all our application's 
 * core data. In in addition to initializing data, it provides service
 * methods for event handlers to use in manipulating the maps.
 * 
 * @author Richard McKenna
 **/
public class ElectionMapDataModel 
{
	private DBFTable sections;
	// HERE'S THE MAP'S RENDERER, WHICH WE NEED TO NOTIFY WHENEVER
	// THERE ARE CHANGES TO DATA SO THAT IT REPAINTS ITSELF\
	private ElectionMapRenderer renderer;
	
	// HERE'S THE FILE MANAGER, WHICH WILL HELP WITH LOADING OF
	// STATE MAPS WHEN NEEDED
	private ElectionMapFileManager fileManager;

	// HERE'S WHERE THE USA MAP'S DATA IS STORED
	private SHPMap usaSHP;
	
	// HERE'S INFO ABOUT THE CURRENT MAP BEING RENDERED
	private String currentMapName;
	private String currentMapAbbr;
	
	// THIS IS FOR HIGHLIGHTING A PART OF A POLYGON (LIKE A COUNTY OR STATE)
	private SHPPolygon highlightedPolygon;
	
	// WE'LL RECYCLE THIS POLYGON SO WE DON'T HAVE TO
	// KEEP CONSTRUCTING IT FOR OUR TESTS
	private Polygon testPoly;
	
	// THIS WILL STORE ALL OF OUR STATE FLAGS
	private TreeMap<String, Image> flags;
	private TreeMap<String, Image> miniFlags;

	// USED FOR TITLES AND THE USA MAP
	public static final String MAP_TITLE = " 2008 Presidential Election Results";
	public static final String USA_MAP_NAME = "USA";
	public static final String USA_MAP_ABBR = "USA";
	
	// .gif IMAGES ARE THE MINI IMAGES
	public static final String MINI_FLAG_EXT = ".gif";
	
	// THIS HELPS US TO KNOW IF A MAP HAS BEEN
	// RENDERED AT LEAST ONCE OR NOT
	private boolean mapRendered;
	
	/**
	 * Note that the data model is not fully setup after this constructor. It
	 * still needs the renderer and the file manager, which should be loaded
	 * when ready via the init method.
	 **/
	public ElectionMapDataModel()
	{
		// AND INITIALIZE OUR DATA STRUCTURES
		testPoly = new Polygon();
		flags = new TreeMap<String, Image>();
		miniFlags = new TreeMap<String, Image>();
		
		// THE MAP HAS NOT YET BEEN RENDERED
		mapRendered = false;
		sections = new DBFTable();
	}

	// SIMPLE ACCESSOR METHODS
	public String 		getCurrentMapName() 		{ return currentMapName; 	}
	public String 		getCurrentMapAbbr() 		{ return currentMapAbbr; 	}
	public SHPPolygon	getHighlightedPolygon()		{ return highlightedPolygon;}
	public boolean 	isMapLoaded() 			{ return currentMapName != null; 		}
	public boolean	isMapRendered()			{ return mapRendered;					}
	public boolean 	isRegionHighlighted()	{ return highlightedPolygon != null; 	}
	public TreeMap<String, Image> getFlags()  { return flags;							}
	public TreeMap<String, Image> getMiniFlags() { return miniFlags;				}
	
	// MORE COMPLEX ACCESSOR METHODS
	
	/**
	 * For accessing the large flag of the map currently being rendered.
	 **/
	public Image getCurrentFlag()
	{
		return flags.get(currentMapAbbr);
	}	
	
	/**
	 * For accessing the SHPMap that corresponds to the the map that
	 * is currently being rendered.
	 **/
	public SHPMap getCurrentSHP()
	{
		// WE ONLY HAVE ONE FOR NOW, YOU MIGHT WANT TO CHANGE
		// HOW THIS WORKS SINCE YOU'LL HAVE OTHERS
		return usaSHP;
	}
	public String determineVotes(Graphics g, File file, int candidate) throws IOException{
		String votes = "";
		BigDecimal totalVotes = BigDecimal.ZERO;
		BigDecimal candidateVotes = BigDecimal.ZERO;
		String theCandidate="";
		Iterator<DBFRecord> iterator = (new DBFFileIO()).loadDBF(file).getTree().iterator();
		
		if(candidate == 2) theCandidate = "Barack Obama";
		else if(candidate ==3) theCandidate = "John McCain";
		else theCandidate = "Other";
		
		while(iterator.hasNext()){
			DBFRecord next = iterator.next();
			candidateVotes = candidateVotes.add(new BigDecimal((Long)next.getData(candidate)));
			for(int i=2; i<5; i++){
				totalVotes= totalVotes.add(new BigDecimal((Long)next.getData(i)));
			}
		}
		int percentage =candidateVotes.divide(totalVotes, new MathContext(2)).multiply(new BigDecimal(100)).intValue();
		votes= theCandidate +": " + candidateVotes.toString() +" Votes ("+ percentage+"%)";
		return votes;
	}
	public BigDecimal candidateVotes(int candidate, File file) throws IOException{
		BigDecimal candidateVotes = BigDecimal.ZERO;
		Iterator<DBFRecord> iterator = (new DBFFileIO()).loadDBF(file).getTree().iterator();
		
		while(iterator.hasNext()){
			DBFRecord next = iterator.next();
			candidateVotes = candidateVotes.add(new BigDecimal((Long)next.getData(candidate)));
		}
		return candidateVotes;
	}
	public BigDecimal[] sortArray(BigDecimal[] array){
		BigDecimal temp = new BigDecimal(-1);
		for(int i=0; i<array.length; i++){
			int k=0;
			for(int j=i; j<array.length; j++){
				if(temp.compareTo(array[j])<0){
					temp = array[j];
					k=j;
				}
			}
			array[k]=array[i];
			array[i]=temp;
		}
		return array;
	}
	public String totalVotes(File file) throws IOException{
		String votes ="";
		Iterator<DBFRecord> record= (new DBFFileIO()).loadDBF(file).getTree().iterator();
		BigDecimal totalVotes = BigDecimal.ZERO;
		while(record.hasNext()){
			DBFRecord next = record.next();
			for(int i=2; i<5; i++){
				totalVotes = totalVotes.add(new BigDecimal((Long)next.getData(i)));
			}
		}
		votes = "Total: " + totalVotes + "Votes (100%)";
		return votes;
	}
	public Graphics2D graphicsString(int candidate, Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		Color theColor = null;
		
		if(candidate==2) theColor = Color.BLUE;
		else if(candidate==3) theColor = Color.RED;
		else theColor = Color.GRAY;
		
		g2.setFont(new Font("Times New Roman",  Font.PLAIN, 14));
		g2.setColor(theColor);
		return g2;
	}
	// MUTATOR METHODS
	
	/**
	 * Called whenever a different territory is highlighted, this updates
	 * our data model so it knows what is highlighted for rendering
	 * purposes.
	 **/
	public void setHighlightedRegion(SHPPolygon poly)
	{
		if (highlightedPolygon != null)
			highlightedPolygon.setLineColor(DEFAULT_BORDER_COLOR);
		highlightedPolygon = poly;
		poly.setLineColor(DEFAULT_HIGHLIGHT_COLOR);		
	}
	
	/**
	 * Called to keep track of whether the map has been rendered
	 * at least once or not. The reason for this is so we don't
	 * keep doing our zoom to map function.
	 */
	public void setMapRendered(boolean initMapRendered)
	{
		mapRendered = initMapRendered;
	}
	
	// SERVICE METHODS - THESE METHODS PROVIDE ADDITIONAL DATA PROCESSING
	// SERVICES, IN PARTICULAR FOR THE EVENT HANDLERS.
	
	/**
	 * This method adds the flag argument to the proper data structure
	 * for storage. Note that this method properly filters the images
	 * into their proper container, with .png files going to flags, and
	 * .gif files going to miniFlags.
	 **/
	public void addFlag(String name, String flagAbbr, Image flag)
	{
		if (name.endsWith(MINI_FLAG_EXT))
			miniFlags.put(flagAbbr, flag);
		else
			flags.put(flagAbbr, flag);
	}

	/**
	 * Called in reponse to mouse motion, this method tests to see
	 * if the current mouse's x,y position overlaps any of the current
	 * map's polygons, and if it does, makes that the highlighted
	 * map region. Note that this method forces a renderer repaint.
	 **/
	public void highlightMapRegion(int x, int y)
	{
		boolean polySelected = selectPolygonAt(x, y);
		if (!polySelected)
		{
			// THE MOUSE ISN'T CURRENTLY OVER ANY SHAPES, SO
			// DON'T HIGHLIGHT ANY OF THEM
			resetHighlightedRegion();
		}
		
		// UPDATE THE VIEW
		renderer.repaint();		
	}
	
	/**
	 * This method completes the setup of this data model. We'll need the 
	 * renderer and fileManager to properly process event responses.
	 **/
	public void init(	ElectionMapRenderer initRenderer,
						ElectionMapFileManager initFileManager)
	{
		// SAVE THESE GUYS FOR LATER
		renderer = initRenderer;
		fileManager = initFileManager;
	}
	public void colorSections(SHPMap map, File file){
		String name = "";
		try {
			sections = (new DBFFileIO()).loadDBF(file);
			initShapeColors(map);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * For the provided map and table arguments, this method initializes
	 * all the shape regions in the map with the colors of the winner
	 * in the table's election results.
	 **/
	public void initShapeColors(SHPMap map)
	{
		// INITIALIZE THE COLORS
		Iterator<SHPShape> shapesIt = map.shapesIterator();
		for (int i=0; shapesIt.hasNext(); i++)
		{
			SHPShape shape = shapesIt.next();
			DBFRecord record = this.sections.getTree().get(i);
			if((Long)record.getData(2)>(Long)record.getData(3)){
				shape.setFillColor(Color.BLUE);
			}
			else
				shape.setFillColor(Color.RED);
		}
	}	

	
	/**
	 * This method sets the USA map, including the shp and
	 * dbf data. Note that it does not force a repaint.
	 **/
	public void initUSAMap(SHPMap initUSAshp)
	{
		currentMapName = USA_MAP_NAME;
		currentMapAbbr = USA_MAP_ABBR;
		usaSHP = initUSAshp;
	}
	
	/**
	 * This method tests to see if the (x,y) point is inside one of the parts (polygons)
	 * of the poly argument. If it is, true is returned, else false.
	 **/
	public boolean pointIsInPoly(ElectionMapRenderer renderer, SHPPolygon poly, int x, int y)
	{
		// GO THROUGH ALL THE PARTS (POLYGONS) OF THIS SHPPolygon. REMEMBER, IN
		// AN SHP FILE, A POLYGON IS MAKE UP OF OTHER PARTS, WHICH ARE EACH THEIR
		// OWN POLYGONS
		for (int i = 0; i < poly.getNumParts(); i++)
		{
			// CLEAR OUR RECYCLED POLYGON OBJECT
			testPoly.reset();
		
			// DETERMINE WHERE IN THE ARRAY WE'LL GET OUR POINTS FROM
			int partStart = poly.getParts()[i];
			int partEnd = poly.getNumPoints()-1;
			if (i < poly.getNumParts()-1)
			{
				partEnd = poly.getParts()[i+1]-1;
			}
		
			// NOW FILL OUR testPoly WITH PARTS
			for (int j = partStart; j <= partEnd; j++)
			{
				double lat = poly.getXPointsData()[j];
				double lon = poly.getYPointsData()[j];
				int pX = renderer.xCoordinateToPixel(lat);
				int pY = renderer.yCoordinateToPixel(lon);
				testPoly.addPoint(pX,pY);
			}
			// AND LET OUR testPoly DO THE TEST
			if (testPoly.contains(x, y))
				return true;
		}
		return false;
	}

	/**
	 * Undoes all map highlighting, this method should be called
	 * when the mouse is determined to not be overlapping any map
	 * regions.
	 **/
	public void resetHighlightedRegion()
	{
		highlightedPolygon = null;
	}	
	
	/**
	 * Used for testing to see if the x,y location is within the
	 * bounds of a map region's polygon. If it is, that region is
	 * highlighted and true is returned. If no map region is found
	 * to contain the point, all regions are unhighlighted and 
	 * false is returned. 
	 **/
	public boolean selectPolygonAt(int x, int y)
	{
		SHPMap currentMap = getCurrentSHP();
		SHPData mapData = currentMap.getShapefileData();	
		Iterator<SHPShape> polyIt = (mapData.getShapes().iterator());
		SHPPolygon poly;
		boolean polyFound = false;
		boolean stopSearching = false;
		
		// GO THROUGH ALL THE POLYGONS IN THE MAP
		while (polyIt.hasNext())
		{
			// TEST EACH ONE
			poly = (SHPPolygon)polyIt.next();

			if (!stopSearching)
			{
				// ONCE ONE IS FOUND TO BE TRUE, NONE OTHERS CAN
				polyFound = pointIsInPoly(renderer, poly, x, y);
			}

			if (polyFound)
			{
				// MARK THIS ONE FOR HIGHLIGHTING
				setHighlightedRegion(poly);
				return true;
			}				
			else
			{
				// ALL OTHERS WILL KEEP THE STANDARD BLACK
				poly.setLineColor(Color.black);
			}
		}
		return false;
	}	
	
	/**
	 * A helper method for switching the map in use to the original
	 * USA map.
	 **/
	private void switchToUSAMap()
	{
		currentMapName = USA_MAP_NAME;
		currentMapAbbr = USA_MAP_ABBR;
	}
}