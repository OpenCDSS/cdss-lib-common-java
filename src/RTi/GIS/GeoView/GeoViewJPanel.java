//-----------------------------------------------------------------------------
// GeoViewJPanel - plug-in map interface containing main map, legend, reference
//				map, control buttons, and status text fields
//-----------------------------------------------------------------------------
// History:
//
// 2001-06-xx	Steven A. Malers, RTi	Created class to allow placement of
//					map interface in any application.
// 2001-07-14	SAM, RTi		Implement panel in CDSS StateView
//					application as first test.  Add more
//					methods to open a project, remove all
//					from display, etc.
// 2001-08-01	SAM, RTi		Start enabling methods to let an
//					external application control the
//					display.
// 2001-08-06	SAM, RTi		Finalize a version that can be released.
//					Comment out the Info and Select Region
//					modes.  Add a Refresh button to force a
//					manual redraw.
// 2001-10-02	SAM, RTi		Add ability to save shapefiles from menu
//					(for use with grid).
//					Add selectAppFeatures() to allow
//					interaction with applications.
// 07 Oct 2001	SAM, RTi		Update selectAppFeatures() to take a
//					list of AppLayerType's to search.
// 2001-10-15	SAM, RTi		Change selectAppFeatures() to return an
//					integer indicating the number of matched
//					features.
// 2001-11-27	SAM, RTi		For feature information dialog, add
//					max/min for shapes other than point.
// 2001-12-04	SAM, RTi		Update to use Swing.
//					Add ability to save as shapefile.
// 2002-01-08	SAM, RTi		Change GeoViewCanvas to
//					GeoViewJComponent.
//-----------------------------------------------------------------------------
// 2003-05-06	J. Thomas Sapienza, RTi	Updated code to match the non-Swing
//					version.
// 2003-05-08	JTS, RTi		Further updated code from non-Swin
//					version
// 2003-05-09	JTS, RTi		* Hammered out resizing issues.
//					* Removed calls to setBackground and
//					  setFont.
// 2003-05-11	JTS, RTi		* Corrected private variable naming
//					  convention.
//					* Added popup menu for the main 
//					  geo view display
// 2003-05-15	JTS, RTi		Removed the lower button section and
//					replaced it with a toolbar for all
//					the important commands.  
// 2003-05-16	JTS, RTi		Replaced the FileDialog with a 
//					JFileChooser.
// 2003-05-19	JTS, RTi		* Removed checkState()
//					* Removed getDisplayFont()
//					* Cleaned up Revisits
// 2003-05-21	JTS, RTi		* JTree doesn't set a preferred size so
//					  that its scrollbars display correctly
//					* New constructor that allows passing in
//					  textfields to use as the display 
//					  and tracker text fields.
//					* Commented out the open project and
//					  add layer menu bar items
// 					* "Save as JPEG" is now "Save as Image"
//					* "Save as Shapefile" is now "Save as"
// 2003-05-22	JTS, RTi		* Layers are not selected/deselected 
//					  correctly when a gvp is opened.
//					* Added code to set the interaction
//					  modes and button states when layers
//					  are selected/deselected
// 2003-06-02	JTS, RTi		Corrected bug in openGVP that was
//					leaving up the hourglass when a bad
//					geo view project name was given.
// 2003-07-31	JTS, RTi		Toolbar icons are now read in from
//					the RTi_140 jar file, and if an error
//					occurs reading in the icon image, 
//					text buttons take their place.
// 2003-09-02	JTS, RTi		Removed the saveAsImage() method and 
//					replaced it with the new SaveImageGUI
//					class.
// 2003-09-23	JTS, RTI		If the icons cannot be loaded from
//					the jar file location, the panel now
//					tries to load them from RTi's hard-
//					coded location before simply displaying
//					text-only buttons.
// 2004-01-08	SAM, RTI		* Show filters for supported files in
//					  addition to shapefiles.
//					* Change the labels on filters from
//					  "Shapefiles" to "ESRI Shapefile".
//					* Use the JFileChooserFactory to work
//					  around Java bug.
// 2004-01-19	JTS, RTi		Now extends ComponentListener in order
//					to repond to resize events.
// 2004-03-15	SAM, RTi		* Change summary map to search all
//					  layers if no layer types are given.
//					* Keep a count of how many locations are
//					  not matched and print a level 1
//					  warning.
//					* When adding a layer - cancel did not
//					  set the cursor back to normal.  Fix.
// 2004-07-29	JTS, RTi		When returning from file choosers when
//					the user presses cancel, hourglass
//					cursors	are now turned off.  Previously
//					they were being left active.
// 2004-08-02	JTS, RTi		Added the new addSummaryLayerView()
//					method that provides information about
//					animated layers.
// 2004-08-05	JTS, RTi		* Converted addSummaryLayerView() to 
//					  now take an array of integers for 
//					  specifying which fields are data 
//					  fields, instead of taking a single 
//					  int value.
//					* Adding a summary layer view now
//					  brings up a dialog for the user to
//					  select the id field and data fields
//					  from.
// 2004-08-09	JTS, RTi		Adding a summary layer now takes an 
//					array of the identifier join fields.
// 2004-08-10	JTS, RTi		Added support for teacups to 
//					addSummaryLayerView().
// 2004-10-06	JTS, RTi		Added support for unsigned vertical bars
//					to addSummaryLayerView().
// 2004-10-28	JTS, RTi		* Added the ability to turn on anti
//					  aliasing in a GVP file.
//					* Added the ability to set an alternate
//					  zoom out percentage in the GVP file.
// 2004-11-11	JTS, RTi		"Zoom Out" changed to "Zoom to Full 
//					Extent"
// 2004-11-12	JTS, RTi		Overloaded addLayerView(GeoLayerView).
// 2005-04-27	JTS, RTi		Added all member variables to finalize()
// 2006-01-16	SAM, RTi		Add selectLayerFeatures() similar to
//					selectAppFeatures().  However, the new
//					method operates on a layer list, not
//					a list of AppLayerTypes.
// 2006-06-19	Scott Townsend, RTi	Added a property to the GVP files to set
// 					the precision of the Coordinates in the
// 					"Locator" rather than have it only hard
// 					codded.
// 2006-06-19	SAT, RTi		Added entries into the JPanel popup menu
// 					to allow for changing the coordinate system 
// 					on the fly. This involved adding to the popup
// 					menu, processing actions performed, and 
// 					modifying the displayed points. Note that
// 					the map is NOT going to be reprojected.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

package	RTi.GIS.GeoView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.File;

import java.net.URL;

import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import RTi.GR.GRColor;
import RTi.GR.GRJComponentDevice;
import RTi.GR.GRLegend;
import RTi.GR.GRLimits;
import RTi.GR.GRPoint;
import RTi.GR.GRScaledClassificationSymbol;
import RTi.GR.GRScaledTeacupSymbol;
import RTi.GR.GRShape;
import RTi.GR.GRSymbol;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ReportJFrame;
import RTi.Util.GUI.SaveImageGUI;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJToggleButton;

import RTi.Util.IO.PropList;

import RTi.Util.Math.MathUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;

import RTi.Util.Time.StopWatch;

/**
The GeoViewJPanel is a JPanel that manages other components.
It has the the following layout:
<pre>
// REVISIT
// may not be right anymore
+--------+ +----------------------------------------------------------------+
| Legend | |                                                                |
|(GeoViewLegendJPanel)                                                      |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                  Main canvas (GeoViewJComponent)               |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|        | |                                                                |
|Controls| |                                                                |
+--------+ +----------------------------------------------------------------+
+--------+ +----------------------------------------------------------------+
| Ref.   | |      Animate controls                                          |
| Map.   | +----------------------------------------------------------------+
|GeoView | +----------------------------------------------------------------+
|        | |      Standard controls                                         |
+--------+ +----------------------------------------------------------------+
+-------------------------------------++------------------------------------+
| Status                              || Locator                            |
+-------------------------------------++------------------------------------+
</pre>
This GeoViewJPanel is meant to be used for integrated map and as a stand-alone
tool when used by GeoViewJFrame.
TODO (JTS - 2006-05-23) Example of usage?
*/
public class GeoViewJPanel 
extends JPanel
implements ActionListener, ComponentListener, GeoViewListener, ItemListener {
	
/**
Home of graphics.
*/
private final String __resource_home = "/RTi/GIS/GeoView";

/**
Main map canvas area for drawing.
*/
private GeoViewJComponent __mainGeoView = null;
/**
Reference (overview) map canvas area for drawing.
*/
private GeoViewJComponent __refGeoView = null;
/**
GeoView project (map configuration file).
*/
private GeoViewProject __gvp = null;
/**
Map controls and tracker information area.
*/
private JPanel __allControlsJPanel = null;
/**
Parent Frame object, for window positioning, etc.
*/
private JFrame __parentJFrame = null;

private SimpleJButton __printJButton = null;
private SimpleJButton __saveAsImageJButton = null;

private SimpleJButton __refreshJButton = null;
private SimpleJButton __zoomOutJButton = null;
private JToggleButton __selectJButton = null;
private JToggleButton __zoomJButton = null;
private JToggleButton __infoJButton = null;

private JComboBox __modeJComboBox = null;

private JTextField __statusJTextField = null;
private JTextField __trackerJTextField = null;

// Buttons...

public final String ADD = "Add Layer";
public final String REMOVE = "Remove Layer";
public final String GEOVIEW_PROPERTIES = "Properties";
public final String PROPERTIES = "Properties ...";

public final String ANIMATE = "Animate";
public final String END = "End >>";
public final String NEXT = "Next >";
public final String PREVIOUS = "< Previous";
public final String PAUSE = "Pause";
public final String STOP = "<< Stop";

public final String MODE = "Mode:";
public final String REFRESH = "Refresh";
public final String PRINT = "Print";
public final String SAVE_AS = "Save As:";

// Choices...

public final String SAVE_AS_IMAGE = "Image";
public final String SAVE_AS_SHAPEFILE = "Save as";

public final String MODE_INFO = "Info";
public final String MODE_SELECT = "Select";
public final String MODE_SELECT_REGION = "Select Region";
public final String MODE_ZOOM = "Zoom";

// Menus...

public final String OPEN_GVP = "Open Project...";
public final String ADD_LAYER_TO_GEOVIEW = "Add Layer...";
public final String ADD_SUMMARY_LAYER_TO_GEOVIEW = "Add Summary Layer...";
public final String EXIT = "Exit";
public final String GEOVIEW_ZOOM = "Zoom Mode";
public final String GEOVIEW_ZOOM_OUT = "Zoom to Full Extent";
public String GEOVIEW_ZOOM_OUT_X = null;
public final String CHANGE_TO_HRAP = "Change to HRAP Projection";
public final String CHANGE_TO_GEOG = "Change to Geographic Projection";
public final String PRINT_GEOVIEW = "Print...";
public final String SAVE_AS_IMAGE_MENU = "Save As Image...";
public final String SAVE_AS_SHAPEFILE_MENU = "Save As ...";
public final String SELECT_GEOVIEW_ITEM = "Select Mode";
public final String SET_ATTRIBUTE_KEY = "Set Attribute Key...";

private String __gvpFile = "";

private List<String> __enabledAppLayerTypes = new Vector (5);

private PropList __displayProps = null;

private GeoViewLegendJTree __legendJTree = null;

private JToolBar __toolBar = null;

/**
Popup menu that holds the "Properties" item for the entire GeoView.
*/
private JPopupMenu __propertiesPopup = null;

/**
The menu item for zooming out by a given percent.
*/
private JMenuItem __zoomOutXMI = null;

/**
The percent by which to zoom out with the Zoom Out By ... menu item.
*/
private int __zoomOutAmount = 20;

/**
The default value for the precision of the coordinates in the "Locator".
*/
private int __locatorPrecision = 6;

/**
The menu item for changing to HRAP projection
*/
private JMenuItem __projHRAP = null;

/**
The menu item for changing to GEOG projection
*/
private JMenuItem __projGEOG = null;

/**
A boolean value specifying whether the shown X,Y coordinates on the map are HRAP or not. 
*/
private boolean __HRAPCoordinates = false;

/**
A boolean value specifying whether the shown X,Y coordinates on the map are Geographic (lat/lon) or not. 
*/
private boolean __GEOGCoordinates = false;

/**
Constructor.  This constructor differs from the other in that this one
creates a toolbar and adds it to this panel.  If using the GeoViewJPanel
within an application that has its own toolbar, that toolbar should
be passed to the other constructor and the toolbar buttons will be added to it.
@param parent Parent Frame.
@param display_props Properties used to control display of GeoViewJPanel (currently not used).
*/
public GeoViewJPanel (JFrame parent, PropList display_props) {
	this (parent, null, null);
}

/**
Constructor.
@param parent Parent Frame.
@param display_props Properties used to control display of GeoViewJPanel (currently not used).
@param toolbar the application tool bar to which to add this Panel's tool bar
(if null, a new tool bar will be declared and used for this panel).
*/
public GeoViewJPanel (JFrame parent, PropList display_props, JToolBar toolbar) {
	__parentJFrame = parent;

	setupGUI(toolbar, display_props, null, null);
}

/**
Constructor.
@param parent Parent Frame.
@param display_props Properties used to control display of GeoViewJPanel (currently not used).
@param toolbar the application tool bar to which to add this Panel's tool bar
(if null, a new tool bar will be declared and used for this panel).
@param field1 a textfield from the application to replace the left textfield in 
the display with.  If null, this will make its own textfield.
@param field2 a textfield from the application to replace the right textfield
in the display with.  If null, this will make its own textfield.
*/
public GeoViewJPanel (JFrame parent, PropList display_props, 
JToolBar toolbar, JTextField field1, JTextField field2) {
	__parentJFrame = parent;

	setupGUI(toolbar, display_props, field1, field2);
}

/**
Handle action events.
@param evt ActionEvent from menus and buttons.
*/
public void actionPerformed( ActionEvent evt )
{	String command = evt.getActionCommand();

	if (command.equals( ADD_LAYER_TO_GEOVIEW ) ||
		command.equals( ADD ) ){
		addLayerView ();
	}
	if (command.equals( ADD_SUMMARY_LAYER_TO_GEOVIEW ) ) {
		setStatus("Adding summary layer to geoview");
		JGUIUtil.setWaitCursor(__parentJFrame, true);
		// Called from the GeoViewFrame...
		// File dialog to select the delimited file...
		JFileChooser fc = JFileChooserFactory.createJFileChooser(JGUIUtil.getLastFileDialogDirectory() );
		fc.setDialogTitle("Select Summary Data File");
		SimpleFileFilter cff = new SimpleFileFilter("txt", "Comma-delimited Files");
		fc.addChoosableFileFilter(cff);
		fc.setFileFilter(cff);
		JGUIUtil.setWaitCursor(__parentJFrame, false);
		if ( fc.showOpenDialog(__parentJFrame) != JFileChooser.APPROVE_OPTION ) {
	    	setStatus("Summary layer add cancelled");
			return;
		}

		File file = fc.getSelectedFile();
		JGUIUtil.setWaitCursor(__parentJFrame, true);

		String fileName = file.getPath();
		JGUIUtil.setLastFileDialogDirectory(file.getParent());

		// Now add to the map...
		addSummaryMapLayer(fileName);
		setStatus("Summary layer add complete");
		JGUIUtil.setWaitCursor(__parentJFrame, false);
	}
	else if( command.equals( GEOVIEW_ZOOM ) || command.equals(MODE_ZOOM)){
		__zoomJButton.setSelected(true);
		__infoJButton.setSelected(false);
		__selectJButton.setSelected(false);
		__mainGeoView.setInteractionMode (GeoViewJComponent.INTERACTION_ZOOM );
	}
	else if( command.equals( GEOVIEW_ZOOM_OUT ) ){
		JGUIUtil.setWaitCursor(__parentJFrame, true);
		__mainGeoView.setWaitCursorAfterRepaint(false);
		__mainGeoView.zoomOut();
	}
	else if (GEOVIEW_ZOOM_OUT != null && command.equals(GEOVIEW_ZOOM_OUT_X)) {
		zoomOut(__zoomOutAmount);
	}	
	else if (command.equals(CHANGE_TO_HRAP)) {
		// Change coordinate systems to HRAP
		PropList pl = __gvp.getPropList();
		String origProj = (String)pl.getContents("GeoView.Projection");

		if(!origProj.equalsIgnoreCase("HRAP")) {
			// Set to display as HRAP projection
			pl.set("GeoView.Projection","HRAP");

			// Change popup in projection
			if (__projHRAP != null) {
				__propertiesPopup.remove(__projHRAP);
			}
			if (__projGEOG != null) {
				__propertiesPopup.remove(__projGEOG);
			}

			// Update popup menu
			__projGEOG = new JMenuItem(CHANGE_TO_GEOG);
			__projGEOG.addActionListener(this);
			__propertiesPopup.add(__projGEOG);

			// Set globals to tell to use HRAP coordinates
			__HRAPCoordinates = true;
			__GEOGCoordinates = false;
		}
	}	
	else if (command.equals(CHANGE_TO_GEOG)) { // Change coordinate systems to Geographic
		// Get new projection and old proplist values 
		PropList pl = __gvp.getPropList();
		String origProj = (String)pl.getContents("GeoView.Projection");

		if(!origProj.equalsIgnoreCase("Geographic")) {
			// Set to display as HRAP projection
			pl.set("GeoView.Projection","Geographic");

			// Change popup in projection
			if (__projHRAP != null) {
				__propertiesPopup.remove(__projHRAP);
			}
			if (__projGEOG != null) {
				__propertiesPopup.remove(__projGEOG);
			}

			// Update popup menu
			__projHRAP = new JMenuItem(CHANGE_TO_HRAP);
			__projHRAP.addActionListener(this);
			__propertiesPopup.add(__projHRAP);

			// Set globals to tell to use Geographic coordinates
			__GEOGCoordinates = true;
			__HRAPCoordinates = false;
		}
	}	
	else if (command.equals(MODE_INFO)) {
		__infoJButton.setSelected(true);
		__zoomJButton.setSelected(false);
		__selectJButton.setSelected(false);
			
		__mainGeoView.setInteractionMode ( GeoViewJComponent.INTERACTION_INFO );
	}
	else if( command.equals( OPEN_GVP ) ) {
		openGVP ();
	}
	else if( command.equals( PRINT_GEOVIEW ) ||
		command.equals ( PRINT ) ){
		printGeoView();
	}
	else if (command.equals(GEOVIEW_PROPERTIES)) {
		if (__mainGeoView == null || __gvp == null) {
			return;
		}
		new GeoViewPropertiesJFrame ( __parentJFrame, __mainGeoView, __gvp, this );
	}
	else if ( command.equals( REFRESH ) ) {
		refresh();
	}
	// This is currently triggered from the GeoViewLegendPanel...
	// else if ( command.equals( REMOVE ) ) {
	//	removeLayerView ( true );
	// }
	else if( command.equals( SAVE_AS_IMAGE ) || command.equals(SAVE_AS_IMAGE_MENU) ) {
		new SaveImageGUI(getGeoView().getImage(),__parentJFrame);	
	}
	else if (command.equals(SAVE_AS_SHAPEFILE) || command.equals(SAVE_AS_SHAPEFILE_MENU) ) {
		saveAs();
	}
	else if(command.equals( SELECT_GEOVIEW_ITEM ) || command.equals(MODE_SELECT)){
		__zoomJButton.setSelected(false);
		__infoJButton.setSelected(false);
		__selectJButton.setSelected(true);
		__mainGeoView.setInteractionMode (
		GeoViewJComponent.INTERACTION_SELECT );
	}
	else if( command.equals( SET_ATTRIBUTE_KEY ) ) {
		// Prompt for a simple response...
		// Old - may be phased out.
	}
}

/**
Add an annotation renderer.  Just chain to the map component.
@param renderer the renderer that will be called when it is time to draw the object
@param objectToRender the object to render (will be passed back to the renderer)
@param label label for the object, to list in the GeoViewJPanel
@param scrollToAnnotation if true, scroll to the annotation (without changing scale)
*/
public void addAnnotationRenderer ( GeoViewAnnotationRenderer renderer,
	Object objectToRender, String label, boolean scrollToAnnotation )
{	// Ad the annotation to the list and redraw if necessary, zooming to new annotation.
	__mainGeoView.addAnnotationRenderer ( renderer, objectToRender, label, scrollToAnnotation );
}

/**
Add a layer view using a file dialog to prompt for the layer file.
Currently only ESRI shapefiles can be interactively added.
*/
public void addLayerView ()
{	String rtn = "gvGUI.addLayerView";

	try {
		// Get the name of the shapefile to add and then add to the view...
	
		// Instantiate a file dialog object with no default...
		setStatus("Adding layer view");
		JGUIUtil.setWaitCursor(__parentJFrame, true);
	
		JFileChooser fc = JFileChooserFactory.createJFileChooser(JGUIUtil.getLastFileDialogDirectory() );
		// IWS remove default "All Files" filter since the code below
		// assumes if its not a xmrg, its a shapefile...
		FileFilter[] ff = fc.getChoosableFileFilters();
		if (ff.length > 0) {
			fc.removeChoosableFileFilter(ff[0]);
		}
		
		fc.setDialogTitle("Add Layer to Map");
		SimpleFileFilter shp_sff =new SimpleFileFilter("shp", "ESRI Shapefile");
		fc.addChoosableFileFilter(shp_sff);
		
		//IWS - delegate to a  SimpleFileFilter, and check for xmrg prefix
		FileFilter xmrgFilter = new FileFilter() {
			final SimpleFileFilter delegate = new SimpleFileFilter("xmrg", "NWS Gridded Precipitation File");
			
			public boolean accept(File f) {
				boolean accept = delegate.accept(f);
				if (! accept) {
					accept = f.getName().startsWith("xmrg");
				}
				return accept;
			}
			
			public String getDescription() {
				return delegate.getDescription();
			}
		};
		
		fc.addChoosableFileFilter(xmrgFilter);
		fc.setFileFilter(shp_sff);
		if ( fc.showOpenDialog(__parentJFrame) != JFileChooser.APPROVE_OPTION){
			setStatus("Layer view add cancelled");
			JGUIUtil.setWaitCursor(__parentJFrame, false);
			return;
		}
		
		JGUIUtil.setWaitCursor(__parentJFrame, true);
		File file = fc.getSelectedFile();
	
		// IWS fix class cast potential, was casting to SimpleFileFilter
		FileFilter res = (FileFilter)fc.getFileFilter();
		
		String filename = file.getPath();
		JGUIUtil.setLastFileDialogDirectory(file.getParent());
	
		if (res == xmrgFilter) {
			if (addXMRGLayerView(filename, "XMRG", false)) {
				setStatus("Layer view added to GeoView");
			}
			else {
				setStatus("Could not add layer to GeoView");
			}
		}
		else {
			addLayerView(filename);
			setStatus("Layer view added to GeoView");
		}
	}
	catch ( Exception e ) {
		Message.printWarning(1, rtn, "Unable to add layer to view");
		Message.printWarning(2, rtn, e);
	}
	JGUIUtil.setWaitCursor(__parentJFrame, false);
}

/**
Add a layer view given the file name for the layer.  The filename should refer a Shapefile or an XMRG file.
@param filename Name of spatial data layer file.
*/
public void addLayerView ( String filename )
{	String rtn = "GeoViewJPanel.addLayerView";

	try {
		StopWatch timer = new StopWatch();
		timer.start();
		setStatus ( "Adding layer..." );
		JGUIUtil.setWaitCursor ( this, true );
		PropList layer_view_props = new PropList ( "forGeoLayerView" );
		layer_view_props.set ( "Label", "UsingGeoViewListener" );
		// Size after add...
		int size = __mainGeoView.getNumLayerViews() + 1;
		GeoLayerView layer_view = new GeoLayerView ( filename, layer_view_props, size );
		layer_view_props = null;
	
		// Now add the layer view to the view...
		__mainGeoView.addLayerView ( layer_view );
		timer.stop();
		Message.printStatus ( 1, rtn, "Reading \"" + filename + "\" took " +
		StringUtil.formatString(timer.getSeconds(),"%.2f") + " seconds." );
		timer = null;
		setStatus ( "Finished adding layer.  Ready." );
		// Add to the legend...
		__legendJTree.addLayerView ( layer_view, (__mainGeoView.getNumLayerViews() - 1) );
		layer_view = null;
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, rtn, "Unable to add layer to view" );
		Message.printWarning(2, rtn, e);
	}
	JGUIUtil.setWaitCursor ( this, false );
}

/**
Add layer view given a layer view.  This method is usually called in cases where
a layer is dynamically created in memory, rather than read from a file.  The
layer view can be added to the main and/or reference view.  A layer view that is
added to the main GeoView must be added to the legend (and will automatically be
done when this method is called).  However, its visual appearance in the legend
can be disabled (e.g., when working with primitive shapes for user interaction).
Handle the behavior by setting GeoLayerViewproperties before calling this method.
@param layer_view GeoLayerView to add.
*/
public void addLayerView(GeoLayerView layer_view) {
	addLayerView(layer_view, true);
}

/**
Add layer view given a layer view.  This method is usually called in cases where
a layer is dynamically created in memory, rather than read from a file.  The
layer view can be added to the main and/or reference view.  A layer view that is
added to the main GeoView must be added to the legend (and will automatically be
done when this method is called).  However, its visual appearance in the legend
can be disabled (e.g., when working with primitive shapes for user interaction).
Handle the behavior by setting GeoLayerViewproperties before calling this method.
@param reset_limits true if the overall limits should be reset and used for
the redraw (use false if adding a layer and zoom has already been made).
Use true to zoom to see all layers.
@param layer_view GeoLayerView to add.
*/
public void addLayerView(GeoLayerView layer_view, boolean reset_limits) {
	boolean add_to_main = true;
	boolean add_to_ref = false;
	boolean show_in_legend = true; // false does not seem to work - maybe need a legend panel size 0;
	if ( add_to_main ) {
		__mainGeoView.addLayerView ( layer_view, reset_limits );
		// Need to handle "show_in_legend"!...
		if ( show_in_legend ) {
			__legendJTree.addLayerView ( layer_view, __legendJTree.getNumLegend() + 1);
		}
	}
	if ( add_to_ref ) {
		__refGeoView.addLayerView ( layer_view, reset_limits );
	}
}

/**
Create and display a summary layer view on the map.  A completely new layer is
created by pulling shapes from available layers.
@param attribute_table DataTable containing attributes to create a new layer.
@param layer_name Name for the layer, to be displayed in the legend.
@param identifier_field Field in the delimited file to be used for the 
identifiers (starting with 0).  Later need to get from the intermediate dialog.
@param first_data_field Field in the delimited file containing the first
numeric data field and after which all fields are assumed to be numeric (first column is field 0).
@param avail_app_layer_types Available application layer types to use when
matching the identifiers in the summary information with active data.
Currently all that are specified are used for searches but in the future a
dialog may be shown to allow the user to select layers to be considered during the matching process.
If null is passed, all layers that have AppJoinField defined are searched.
@param equalize_max If true, the maximum display values for each symbol will
be set equal.  If false, the maximum will be determined from the data used for the individual symbols.
@return the GeoLayerView created and added
@exception Exception if there is an error processing the summary layer data.
*/
public GeoLayerView addSummaryLayerView (DataTable attribute_table, String layer_name, int identifier_field,
	int first_data_field, List avail_app_layer_types, boolean equalize_max ) 
throws Exception {
	return addSummaryLayerView(attribute_table, layer_name, 
		identifier_field, first_data_field, avail_app_layer_types, 
		equalize_max, null, null);
}

/**
Used in the next method to not require creating lots of 1 element arrays.
*/
private static int[] __temp = new int[1];

/**
Create and display a summary layer view on the map.  A completely new layer is
created by pulling shapes from available layers.
@param attribute_table DataTable containing attributes to create a new layer.
@param layer_name Name for the layer, to be displayed in the legend.
@param identifier_field Field in the delimited file to be used for the 
identifiers (starting with 0).  Later need to get from the intermediate dialog.
@param first_data_field Field in the delimited file containing the first
numeric data field and after which all fields are assumed to be numeric (first column is field 0).
@param avail_app_layer_types Available application layer types to use when
matching the identifiers in the summary information with active data.
Currently all that are specified are used for searches but in the future a
dialog may be shown to allow the user to select layers to be considered during the matching process.
If null is passed, all layers that have AppJoinField defined are searched.
@param equalize_max If true, the maximum display values for each symbol will
be set equal.  If false, the maximum will be determined from the data used for the individual symbols.
@param animationFields an integer array of the fields in the attribute 
table that will be animated.  Null if not an animated layer.  This array
does not correspond with all the fields in the attribute table but will 
instead contain a series of values, such as:<p>
<ul>
<li>animationFields[0] = 12</li>
<li>animationFields[1] = 13</li>
<li>animationFields[2] = 15</li>
</ul>
This means that fields 12, 13 and 15 (base-0) in the table are animated fields.
@param animationMaxValues the maximum values for each of the animated fields, 
used for determining the size of the bars.  Null if not an animated layer.
This array is sized the same as the animationFields array, and for a value
in array position X, the maximum is the field maximum for the field stored in animationFields[X].
@return the summary layer view added
@exception Exception if there is an error processing the summary layer data.
*/
public GeoLayerView addSummaryLayerView ( DataTable attribute_table, String layer_name, int identifier_field,
	int first_data_field, List avail_app_layer_types, boolean equalize_max, int[] animationFields,
	double[] animationMaxValues)
throws Exception
{	
	__temp[0] = identifier_field;
	
	int fieldCount = attribute_table.getNumberOfFields();
	int size = fieldCount - first_data_field;
	int[] dataFields = new int[size];
	dataFields[0] = first_data_field;
	for (int i = 1; i < size; i++) {	
		dataFields[i] = dataFields[i - 1] + 1;
	}

	return addSummaryLayerView(attribute_table, layer_name, 
		__temp, dataFields, avail_app_layer_types, equalize_max, animationFields, animationMaxValues);
}

/**
Create and display a summary layer view on the map.  A completely new layer is
created by pulling shapes from available layers.
@param attribute_table DataTable containing attributes to create a new layer.
@param layer_name Name for the layer, to be displayed in the legend.
@param identifier_fields Fields in the delimited file to be used for the 
identifiers (starting with 0).  Cannot be null.
@param data_fields an integer array of the fields in the attribute table that
hold data that can be displayed on the map.  Cannot be null. 
This array does not correspond with all the fields in the attribute table 
but will instead contain a series of values, such as:<p>
<ul>
<li>data_fields[0] = 6</li>
<li>data_fields[1] = 8</li>
<li>data_fields[2] = 9</li>
</ul>
This means that fields 6, 8 and 9 (base-0) in the table are data fields.
@param avail_app_layer_types Available application layer types to use when
matching the identifiers in the summary information with active data.
Currently all that are specified are used for searches but in the future a
dialog may be shown to allow the user to select layers to be considered during the matching process.
If null is passed, all layers that have AppJoinField defined are searched.
@param equalize_max If true, the maximum display values for each symbol will
be set equal.  If false, the maximum will be determined from the data used for the individual symbols.
@param animationFields an integer array of the fields in the attribute 
table that will be animated.  Null if not an animated layer.  This array
does not correspond with all the fields in the attribute table but will 
instead contain a series of values, such as:<p>
<ul>
<li>animationFields[0] = 12</li>
<li>animationFields[1] = 13</li>
<li>animationFields[2] = 15</li>
</ul>
This means that fields 12, 13 and 15 (base-0) in the table are animated fields.
@param animationMaxValues the maximum values for each of the animated fields, 
used for determining the size of the bars.  Null if not an animated layer.
This array is sized the same as the animationFields array, and for a value
in array position X, the maximum is the field maximum for the field stored in animationFields[X].
@return the summary layer view added
@throws Exception if there is an error processing the summary layer data.
*/
public GeoLayerView addSummaryLayerView (DataTable attribute_table, String layer_name, int[] identifier_fields,
	int[] data_fields, List avail_app_layer_types, boolean equalize_max,
	int[] animationFields, double[] animationMaxValues)
throws Exception {	
	return addSummaryLayerView(attribute_table, 
		GRSymbol.SYM_VBARSIGNED, layer_name, 
		identifier_fields, data_fields, avail_app_layer_types,
		equalize_max, animationFields, animationMaxValues, null);
}

/**
Create and display a summary layer view on the map.  A completely new layer is
created by pulling shapes from available layers.
@param attributeTable DataTable containing attributes to create a new layer.
@param symbolType one of either GRSymbol.SYM_VBARSIGNED or 
GRSymbol.SYM_TEACUP, specifying the kind of symbol that should be used for managing the data
@param layerName Name for the layer, to be displayed in the legend.
@param identifierFields Fields in the delimited file to be used for the 
identifiers (starting with 0).  Cannot be null.
@param dataFields an integer array of the fields in the attribute table that
hold data that can be displayed on the map.  Cannot be null. 
This array does not correspond with all the fields in the attribute table 
but will instead contain a series of values, such as:<p>
<ul>
<li>dataFields[0] = 6</li>
<li>dataFields[1] = 8</li>
<li>dataFields[2] = 9</li>
</ul>
This means that fields 6, 8 and 9 (base-0) in the table are data fields.<p>
For teacup symbols, dataFields must be a 3-element array with the following:
<ol>
<li>MaxCapacityField - the first element should have the field that stores
the maximum capacity of the teacup.</li>
<li>MinCapacityField - the second element should have the field that stores
the minimum capacity of the teacup.</li>
<li>CurrentCapacity - the third element should have the field that stores 
the current capacity of the teacup.</li>
</ol>
@param avail_app_layer_types Available application layer types to use when
matching the identifiers in the summary information with active data.
Currently all that are specified are used for searches but in the future a
dialog may be shown to allow the user to select layers to be considered during the matching process.
If null is passed, all layers that have AppJoinField defined are searched.
@param equalize_max If true, the maximum display values for each symbol will
be set equal.  If false, the maximum will be determined from the data used for the individual symbols.
@param animationFields an integer array of the fields in the attribute 
table that will be animated.  Null if not an animated layer.  This array
does not correspond with all the fields in the attribute table but will 
instead contain a series of values, such as:<p>
<ul>
<li>animationFields[0] = 12</li>
<li>animationFields[1] = 13</li>
<li>animationFields[2] = 15</li>
</ul>
This means that fields 12, 13 and 15 (base-0) in the table are animated fields.
<p>
For teacup symbols, this must be a 3-element array like the following:
<ol>
<li>MaxCapacityField - the first element should have the field that stores
the maximum capacity of the teacup.</li>
<li>MinCapacityField - the second element should have the field that stores
the minimum capacity of the teacup.</li>
<li>CurrentCapacity - the third element should have the field that stores 
the current capacity of the teacup.</li>
</ol>
<p>
Typically, for teacups this array will be identical to the dataFields array, above.
@param animationMaxValues the maximum values for each of the animated fields, 
used for determining the size of the bars.  Null if not an animated layer.
This array is sized the same as the animationFields array, and for a value
in array position X, the maximum is the field maximum for the field stored in animationFields[X].<p>
If the symbol type is a teacup, this array should have only one value in in,
the maximum capacity of all the teacups being animated together.  
@return the summary layer view added
@throws Exception if there is an error processing the summary layer data.
@throws Exception if the specified symbol type is not one of the supported types.
@throws Exception if for teacup symbols the data fields and animation fields do not contain 3 values.
*/
public GeoLayerView addSummaryLayerView (DataTable attributeTable, int symbolType, String layerName,
	int[] identifierFields, int[] dataFields, List availAppLayerTypes, boolean equalizeMax,
	int[] animationFields, double[] animationMaxValues, PropList props)
throws Exception
{	
	String routine = "GeoViewPanel.addSummaryLayerView";

	int numAppLayerTypes = 0;
	if (availAppLayerTypes != null) {
		numAppLayerTypes = availAppLayerTypes.size();
	}
	
	List layerViews = getGeoView().getLayerViews();			
	int numLayerViews = layerViews.size();

	if (numAppLayerTypes == 0) {
		// Set to the full size so that everything is searched...
		numAppLayerTypes = numLayerViews;
	}

	List dataFieldsVector = new Vector();

	if (symbolType == GRSymbol.SYM_TEACUP) {
		if (dataFields.length != 3) {
			throw new Exception("Data fields must have 3 elements for teacup symbols, not: " + dataFields.length);
		}
	}

	for (int i = 0; i < dataFields.length; i++) {
		dataFieldsVector.add(attributeTable.getFieldName(dataFields[i]));
	}

	// Convert the field names to numbers (names may be duplicated so use
	// the field numbers that the user has chosen)...

	// There are a couple of ways to structure the loop.  Because we want
	// to make sure that each item in the attribute table has a shape, loop
	// on the attribute table records first.  If a corresponding item is
	// not found, add a "null" shape to keep things consistent.

	GeoLayer summaryLayer = new GeoLayer(new PropList("SummaryLayer"));
	summaryLayer.setShapeType(GeoLayer.POINT);
	List summaryShapes = summaryLayer.getShapes();
	summaryLayer.setAttributeTable(attributeTable);
	summaryLayer.setAppLayerType("Summary");
	GRLegend summaryLegend = null;
	
	GRSymbol symbol = null;

	int length = dataFields.length;

	int numPosColors = 1;
	int numNegColors = 1;
	GRColor[] posColors = null;
	GRColor[] negColors = null;
	if (props != null) {
		while (true) {
			if (props.getValue("PositiveBarColor."+numPosColors) == null) {
				numPosColors--;
				break;
			}
			numPosColors++;
		}
//		Message.printStatus(1, "", "NumPosColors: " + numPosColors);
		if (numPosColors > 0) {
			posColors = new GRColor[numPosColors];
			for (int i = 0; i < numPosColors; i++) {
//		Message.printStatus(1, "", "PositiveBarColor." + (i + 1) + " = "
//			+ props.getValue("PositiveBarColor." + (i + 1)));
				posColors[i] = GRColor.parseColor( props.getValue("PositiveBarColor." + (i + 1)));
			}
		}
	
		while (true) {
			if (props.getValue("NegativeBarColor."+numNegColors) == null) {
				numNegColors--;
				break;
			}
			numNegColors++;
		}
//		Message.printStatus(1, "", "NumNegColors: " + numNegColors);
		if (numNegColors > 0) {
			negColors = new GRColor[numNegColors];
			for (int i = 0; i < numNegColors; i++) {
//		Message.printStatus(1, "", "NegativeBarColor." + (i + 1) + " = "
//			+ props.getValue("NegativeBarColor." + (i + 1)));
				negColors[i] = GRColor.parseColor( props.getValue("NegativeBarColor." + (i + 1)));
			}
		}
	}
	else {
		numPosColors = 0;
		numNegColors = 0;
	}

	if (symbolType == GRSymbol.SYM_TEACUP) {
		summaryLegend = new GRLegend(1);
		summaryLegend.setText(layerName);
		symbol = new GRScaledTeacupSymbol(dataFields);
		symbol.setClassificationField((String)dataFieldsVector.get(2));
		symbol.setColor(GRColor.blue);
		symbol.setColor2(GRColor.red);
		String tsize = props.getValue("TeacupSize");
		String tmeas = props.getValue("TeacupSizeCap");
		String max = props.getValue("MaxCapacity");

		if (max == null) {
			throw new Exception ("No MaxCapacity property defined for teacup symbols.");
		}

		double size = 20;

		if (tsize != null) {
			size = (new Double(tsize)).doubleValue();
			if (tmeas == null) {
				tmeas = max;
			}
			
			double dmax = (new Double(max)).doubleValue();
			double temp = (new Double(tmeas)).doubleValue();
			double pct = temp / dmax;	
			size *= pct;
		}
		symbol.setSizeX(size);
		symbol.setSizeY(size);
			
		summaryLegend.setSymbol(0, symbol);
	}
	else if (symbolType == GRSymbol.SYM_VBARSIGNED) {
		summaryLegend = new GRLegend(dataFields.length);
		summaryLegend.setText(layerName);	
		for (int i = 0; i < length; i++) {
			symbol = new GRScaledClassificationSymbol();
			symbol.setStyle(GRSymbol.SYM_VBARSIGNED);
			symbol.setClassificationField((String)dataFieldsVector.get(i));
			symbol.setSizeX(4.0);
			symbol.setSizeY(40.0);
			if (posColors != null) {
				symbol.setColor(posColors[i % numPosColors]);
			}
			else {
				symbol.setColor(GRColor.blue);
			}
			if (negColors != null) {
				symbol.setColor2(negColors[i % numNegColors]);
			}
			else {
				symbol.setColor2(GRColor.red);
			}
			summaryLegend.setSymbol(i, symbol);
		}
	}
	else if (symbolType == GRSymbol.SYM_VBARUNSIGNED) {
		summaryLegend = new GRLegend(dataFields.length);
		summaryLegend.setText(layerName);	
		for (int i = 0; i < length; i++) {
			symbol = new GRScaledClassificationSymbol();
			symbol.setStyle(GRSymbol.SYM_VBARUNSIGNED);
			symbol.setClassificationField((String)dataFieldsVector.get(i));
			symbol.setSizeX(4.0);
			symbol.setSizeY(40.0);
			if (posColors != null) {
				symbol.setColor(posColors[i % numPosColors]);
			}
			else {
				symbol.setColor(GRColor.blue);
			}
			if (negColors != null) {
				symbol.setColor2(negColors[i % numNegColors]);
			}
			else {
				symbol.setColor2(GRColor.red);
			}
			summaryLegend.setSymbol(i, symbol);
		}
	}


	GeoLayerView summaryLayerView = new GeoLayerView( summaryLayer, summaryLegend, new PropList("SummaryView"));

	boolean isAnimated = false;
	if (animationFields == null) {
		isAnimated = false;
	}
	else {
		isAnimated = true;

		if (symbolType == GRSymbol.SYM_TEACUP) {
			if (animationFields.length != 3) {
				throw new Exception("Animation fields length must be 3, not: " + animationFields.length);
			}
		}
		
		summaryLayerView.setAnimationFields(animationFields);
		summaryLayerView.setAnimated(true);		
	}

	summaryLayerView.setName(layerName);
	
	////////////////////////////////////////////////////////////////
	// Variables for the match loop 

	boolean found; // Indicates whether a shape is found in the search
	boolean skipLayer = false;
	DataTable layerAttributeTable; // Attribute table from layer that is being searched				
	GeoLayer layer; // GeoLayer that is being searched
	GeoLayerView layerView = null; // GeoLayerView that is being searched
	GRShape shape = null; // Shape to match in an existing layer.
	int[] layerJoinFields; // Field in layer view that is the appJoinField
	int size = attributeTable.getNumberOfRecords(); // Number of layer views
	int is, ic, j, k; // Indices for loops
	int joinSize = 0;
	int missingCount = 0; // Count of locations that cannot be matched.
	int numShapes; // Number of shapes in a layer
	Object o = null;
	String[] layerIDs; // Identifier in layer to check
	String[] currentIDs = new String[identifierFields.length];
	String appJoinField; // The appJoinField property value for a layer that is being searched.	
	String appLayerType; // Layer type in a layer that is being searched
	String temp = null;
	List joinFieldsVector = null;
	List shapes = null;	// Shapes in a layer that is being searched

	for (int i = 0; i < size; i++) { // Loop on list of feature IDs to match
		found = false;

		// copy out all the identifier field values
		for (j = 0; j < identifierFields.length; j++) {
			currentIDs[j] = ((String)attributeTable.getFieldValue(i, identifierFields[j])).trim();
		}

		// Loop through the layers and search those that match the requested AppLayerTypes...
		for (j = 0; j < numLayerViews; j++) {
			skipLayer = true;
			layerView = (GeoLayerView)layerViews.get(j);
			layer = layerView.getLayer();
			appLayerType = layer.getAppLayerType();
			if ((availAppLayerTypes == null) || (availAppLayerTypes.size() == 0)) {
				// Always search the layer...
				skipLayer = false;
			}
			else {	
				// Figure out if layer should be searched...
				for (k = 0; k < numAppLayerTypes; k++) {
					if (appLayerType.equalsIgnoreCase((String)availAppLayerTypes.get(k))) {
						skipLayer = false;
					}
				}
			}

			if (skipLayer) {
				continue;
			}
			
			// If here have found a layer type that should be searched...
			shapes = layer.getShapes();
			numShapes = shapes.size();
			layerAttributeTable = layer.getAttributeTable();
			
			// Figure out the field in the layer that is
			// joined to the application.  This can only be
			// done if appJoinField is a property...
			appJoinField = layerView.getPropList().getValue("AppJoinField");
				
			if (appJoinField == null) {
				// The layer does not have join information so skip it...
				continue;
			}

			try {	
				joinFieldsVector = StringUtil.breakStringList(appJoinField, ",", 0);
				joinSize = joinFieldsVector.size();
				if (joinSize != identifierFields.length) {
					continue;
				}

				layerJoinFields = new int[joinSize];
				for (ic = 0; ic < joinSize; ic++) {
					layerJoinFields[ic] = layerAttributeTable.getFieldIndex((String)joinFieldsVector.get(ic));
				}
			}
			catch (Exception e) {
				Message.printWarning(2, routine, "Layer view \"" + layerView.getLegend().getText() + "\"");				
				Message.printWarning(2, routine, e);
				continue;
			}
			for (is = 0; is < numShapes; is++) {
				shape = (GRShape)shapes.get(is);
				// Get the identifier attribute from the layer...
				layerIDs = new String[identifierFields.length];
				for (ic = 0; ic < identifierFields.length; ic++) {
					o = layer.getShapeAttributeValue( shape.index, layerJoinFields[ic]);
					if (o instanceof Double) {
						layerIDs[ic] = StringUtil.formatString(o,"%" 
							+ layer.getShapeWidthValue(shape.index, layerJoinFields[ic]) + "."
							+ layer.getShapePrecisionValue(shape.index, layerJoinFields[ic]) + "f").trim();
					}
					else {
						layerIDs[ic] = o.toString().trim();
					}
				}
				
				// Compare to the identifiers from the attribute table...
				// This can generate many megabytes of output...
				found = true;
				for (ic = 0; ic < identifierFields.length; ic++) {
					//Message.printStatus(1, routine,
					//	"Comparing \"" + currentIDs[ic] + "\" to layer ID \"" + layerIDs[ic] + "\"");
			
					if (!currentIDs[ic].equalsIgnoreCase(layerIDs[ic])) {
						found = false;
						break;
					}
				}

				if (found) {
					// ugly, but want to break out of 2 for loops at once
					is = numShapes + 1;
					j = numLayerViews + 1;
				}
			}
		}

		if (found && (shape != null)) {
			// Add a copy of the found shape to the layer and set
			// the index to the record count.  For now assume the
			// shapes are points - later need to add clone() to GRShape classes.
			if (shape.type == GRShape.POINT) {
				temp = "";
				for (ic = 0; ic < identifierFields.length; ic++) {
					temp += currentIDs[ic];
				}
				Message.printStatus(2, routine, "Found location for \"" + temp
					+"\" in \"" + layerView.getLegend().getText() + "\"");
				//Message.printStatus(1, routine, "Data values are " 
				//	+ attributeTable.getFieldValue(i, 2) + " " + attributeTable.getFieldValue(i, 3));
					shape = new GRPoint( ((GRPoint)shape).x, ((GRPoint)shape).y);
			}
			else {	
				// Just add a null shape...
				shape = new GRShape();
				shape.is_visible = false;
			}
		}
		else {	
			temp = "";
			for (ic = 0; ic < identifierFields.length; ic++) {
				temp += currentIDs[ic];
			}

			// Add a new null shape...
			Message.printWarning(2, routine, "Did NOT find location for \"" + temp + "\"");
			shape = new GRShape();
			shape.is_visible = false;
			missingCount++;
		}
		shape.index = i;
		summaryShapes.add(shape);
	}

	// Figure out the limits of the data to be used with each symbol.  This
	// is done now to streamline rendering and allow the user to change the max values for appearance.

	boolean animated = false;
	double allMax = 0.0;
	double symbolMax = 0.0;
	int foundPos = -1;
	
	// determine the max values for all the fields
	
	if (symbolType == GRSymbol.SYM_TEACUP) {
		symbol = summaryLegend.getSymbol(0);
		if (isAnimated) {
			((GRScaledTeacupSymbol)symbol).setMaxCapacity(animationMaxValues[0]);
		}
		else {
			double d = summaryLayer.getAttributeMax(dataFields[0], true);
			((GRScaledTeacupSymbol)symbol).setMaxCapacity(d);
		}
	}
	else if (symbolType == GRSymbol.SYM_VBARSIGNED || symbolType == GRSymbol.SYM_VBARUNSIGNED) {
		for (int i = 0; i < length; i++) {
			animated = false;
			foundPos = -1;
			symbol = summaryLegend.getSymbol(i);
	
			// Check to see if the current field is an animation field
			if (isAnimated) {
				for (int m = 0; m < animationFields.length; m++) {
					if (animationFields[m]==dataFields[i]) {
						animated = true;
						foundPos = m;
					}
				}
			}
	
			// if the field is an animation field, use the 
			// previously-determined max value instead of computing one
			if (animated) {
				symbolMax = animationMaxValues[foundPos];
			}
			else {
				// for non-animated fields, use the original code
				symbolMax = MathUtil.max(summaryLayer.getAttributeMax((dataFields[i]), false),
				 	Math.abs(summaryLayer.getAttributeMin((dataFields[i]), false)));
			}
			if (equalizeMax) {
				allMax = MathUtil.max(symbolMax, allMax);
			}
	
			// Go ahead and set here and reset outside the loop if equalizeMax...
			((GRScaledClassificationSymbol)symbol).setClassificationDataMax(symbolMax);
			((GRScaledClassificationSymbol)symbol).setClassificationDataDisplayMax(symbolMax);
		}

		if (equalizeMax) {
			for (int i = 0; i < dataFields.length; i++) {
				symbol = summaryLegend.getSymbol(i);
				((GRScaledClassificationSymbol)symbol).setClassificationDataDisplayMax(allMax);
			}
		}
	}

	// Add the summary layer...

	summaryLayer.refresh();	// To compute layer limits.
	addLayerView(summaryLayerView);

	// Warn about missing data...

	if (missingCount > 0) {
		String plural1 = "were";
		String plural2 = "s";
		String plural3 = "are";
		
		if (missingCount == 1) {
			plural1 = "was";
			plural2 = "";
			plural3 = "is";
		}

		Message.printWarning(1, routine, "There " + plural1 + " " + missingCount +
			" location" + plural2 + " that could not be matched and " + plural3 + " not shown.", __parentJFrame);
	}

	return summaryLayerView;
}

/**
Create and display a summary layer on the map.
@param filename Delimited file containing attributes to create a new
layer.  Currently the first column must be identifiers that match the
AppJoinField information in available layers.  The second column is an optional
description, and the remaining fields are numerical data values.
@return the summary map layer added, or null if there was an error adding.
*/
public GeoLayerView addSummaryMapLayer ( String filename ) 
{	String rtn = "addSummaryMapLayer";
	String delimiter = ",";
	try {	
		/* SAMX need to figure out whether this is selected by the
		first dialog or not needed here...
		Vector avail_app_layer_types = new Vector ();
		avail_app_layer_types.addElement ( "Diversion" );
		avail_app_layer_types.addElement ( "DiversionWell" );
		avail_app_layer_types.addElement ( "InstreamFlow" );
		avail_app_layer_types.addElement ( "Reservoir" );
		avail_app_layer_types.addElement ( "Streamflow" );
		avail_app_layer_types.addElement ( "Well" );
		*/
		List avail_app_layer_types = new Vector();
		avail_app_layer_types.add ( "BaseLayer" );

		List tableFields = DataTable.parseDelimitedFileHeader ( filename, delimiter);

		List appLayers = getLayerViews(null);
		GeoViewSummaryFileJDialog d = new GeoViewSummaryFileJDialog(
			__parentJFrame, filename, tableFields, delimiter, appLayers);

		int[] idFields = d.getIDFields();
		if (idFields == null) {
			// the user cancelled
			return null;
		}
	
		int[] dataFields = d.getDataFields();

		List v = d.getAppLayerTypes();

		boolean equalizeMax = d.getEqualizeMax();

		String layerName = d.getLayerName();

		int index = -1;
		String s = null;
		Vector appLayerTypes = new Vector();
		for (int i = 0; i < v.size(); i++) {
			s = (String)v.get(i);
			index = s.indexOf(" - ");
			s = s.substring(0, index).trim();
			appLayerTypes.add(s);
		}

		// Default field type is string so set data fields to double...

		for (int i = 0; i < dataFields.length; i++) {
			((TableField)tableFields.get(dataFields[i])).setDataType(TableField.DATA_TYPE_DOUBLE);
		}

		// Now read the file and properly handle the field types...
	
		DataTable attribute_table = DataTable.parseDelimitedFile ( filename, delimiter, tableFields, 1 );
		return addSummaryLayerView ( attribute_table,
			layerName, idFields, dataFields, appLayerTypes, equalizeMax, null, null);
	}
	catch ( Exception e ) {
		Message.printWarning(1, rtn, "Unable to add summary layer.");
		Message.printWarning(2, rtn, e);
		return null;
	}
}

/**
Adds a XMRG file as a layer.
@param xmrgFile the file from which to rad the xmrg information.
@param xmrgName the name to give the xmrg layer.
@param daily whether the data in the xmrg is daily.
@return true if the layer was added successfully, false if not.
*/
public boolean addXMRGLayerView(String xmrgFile, String xmrgName, boolean daily)
throws Exception {
	String routine = "GeoViewJPanel.addXMRGLayerView";
	XmrgGridLayer gridLayer = null;
	try {
		gridLayer = new XmrgGridLayer(xmrgFile,true,false);
	}
	catch (Exception e ) {
		Message.printWarning(2, routine, e);
		Message.printWarning(1, routine, "An error occurred while "
			+ "reading the xmrg; it will not be added to the map display.");
		return false;
	}

	//set the Layer type so it can be removed from map.
	gridLayer.setAppLayerType("xmrg");

	//create an im-memory layer to add to the GeoView 
	GeoViewJComponent geoview = getGeoView();
	
	//get projection of the GeoViewCanvas
	GeoProjection geoViewProjection = geoview.getProjection();

	//make the shape file layer be of the same projection
	gridLayer.setProjection(geoViewProjection);

	//set up legend
	GRSymbol symbol = new GRSymbol();

	symbol.setClassificationField("VALUE"); // Grid attribute in shapefile.
	symbol.setClassificationType("ClassBreaks");
	// XMRG is always in MM
	double[] class_breaks = { 
		1.0, 
		2.0, 
		3.0, 
		4.0, 
		5.0,
		10.0, 
		20.0, 
		30.0, 
		40.0, 
		50.0,
		50 
	};
	symbol.setClassificationData(class_breaks, false);

	symbol.setColorTable("BlueToRed", 11);

	//xmrg name (without full path) = xmrgName
	GRLegend legend = new GRLegend(symbol, xmrgName);
	PropList props = new PropList("xmrg shape file");

	GeoLayerView xmrg_shape_LayerView = new GeoLayerView( gridLayer, legend, props);
	
	//add layer to map
	addLayerView(xmrg_shape_LayerView);
	return true;
}

/**
Does nothing.
*/
public void componentHidden(ComponentEvent event) {}

/**
Does nothing.
*/
public void componentMoved(ComponentEvent event) {}

/**
Sets the hourglass to not appear after the redraw if the component is resized.
TODO (JTS - 2004-11-01) this code was written to handle putting the hourglass up on the screen 
properly or not as things were redrawn.  It was never effective and often
buggy and I highly suggest we remove it entirely.
*/
public void componentResized(ComponentEvent event) {
	setWaitCursorAfterRepaint(false);
//	JGUIUtil.setWaitCursor(__parentJFrame, true);
}

/**
Does nothing.
*/
public void componentShown(ComponentEvent event) {}

/**
Disable the application layer types that are specified.  For example,
AppLayerTypes of "Streamflow" may be specified.  The layer types
are associated with the GeoLayer for the view.  All GeoLayerViews are checked
and views that show layers that are not in the enabled set are turned off.
Currently, layers and layer views are a one to one relationship.
@param enabled_types Vector of strings containing application layer types that should be enabled.
@param disabled_types Vector of strings containing application layer types that should be disabled.
Currently only false is supported.
*/
public void disableAppLayerTypes ( List enabled_types, List disabled_types )
{	int tsize = 0;
	if ( disabled_types != null ) {
		tsize = disabled_types.size();
	}
	Message.printStatus ( 1, "", "Number of types to disable is " + tsize );
	if ( tsize == 0 ) {
		return;
	}
	int size = __mainGeoView.getNumLayerViews();
	List layer_views = __mainGeoView.getLayerViews();
	GeoLayerView layer_view = null;
	GeoLayer layer = null;
	String layer_type = null;
	boolean did_something = false;
	for ( int i = 0; i < size; i++ ) {
		layer_view = (GeoLayerView)layer_views.get(i);
		if ( layer_view == null ) {
			continue;
		}
		layer = layer_view.getLayer();
		if ( layer == null ) {
			continue;
		}
		layer_type = layer.getAppLayerType();
		// Check the layer type against the types that are to be
		// enabled.  Disable if the layer matches the app type...
		Message.printStatus ( 1, "", "App layer type for " +
		layer.getFileName() + " is \"" + layer_type + "\"" );
		for ( int j = 0; j < tsize; j++ ) {
			Message.printStatus ( 1, "", "Checking disabled type \"" + (String)disabled_types.get(j) + "\"" );
			if (layer_type.equalsIgnoreCase((String)disabled_types.get(j)) ) {
				layer_view.isVisible ( false );
				did_something = true;
				break;
			}
		}
		if ( layer_view.isVisible() ) {
			Message.printStatus ( 1, "", "Layer view for \"" + layer.getFileName() + "\" is visible.");
		}
		else {
			Message.printStatus ( 1, "", "Layer view for \"" + layer.getFileName() + "\" is not visible.");
		}
	}
	layer_views = null;
	layer_view = null;
	layer = null;
	layer_type = null;
	if ( did_something ) {
		// Update the legend checkboxes...
		//__legendJTree.repaint();
		__legendJTree.invalidate();
		__legendJTree.repaint();
		// Force a redraw of the main map to make sure the layers are
		// shown in agreement with the legend.  If a normal repaint() is
		// called, the image is not updated because the size has not changed...
		__mainGeoView.redraw();
	}
}

/**
Enable the application layer types that are specified.  For example,
AppLayerTypes of "Streamflow" and "Baseline" may be specified.  The layer types
are associated with the GeoLayer for the view.  All GeoLayerViews are checked
and views that show layers that are not in the enabled set are turned off.
Currently, layers and layer views are a one to one relationship.
@param enabled_types Vector of strings containing application layer types that
should be enabled.  This is used by applications to force certain layers to
be displayed.  User interaction with GeoView may change the appearance of
GeoView in which case the AppLayerTypes that GeoView thinks are enabled may not
actually be reflected in the graphical interface.
@param append_types If true, the list of types to be enabled is added to the
existing enabled list.  If false, only the listed types are enabled.
Currently only false is supported.
*/
public void enableAppLayerTypes ( List enabled_types, boolean append_types )
{	int tsize = 0;
	if ( enabled_types != null ) {
		tsize = enabled_types.size();
	}
	Message.printStatus ( 1, "", "Number of types to enable is " + tsize );
	if ( tsize == 0 ) {
		return;
	}
	int size = __mainGeoView.getNumLayerViews();
	GeoLayerView layer_view = null;
	GeoLayer layer = null;
	String layer_type = null;
	// Careful of this, it seems to empty out the list totally because of the reference.
	//if ( !append_types ) {
	//	__enabledAppLayerTypes.removeAllElements();
	//}
	// For now always do this...
	__enabledAppLayerTypes = enabled_types;
	boolean did_something = false;
	List layerNodes = __legendJTree.getAllLayerNodes();
	size = layerNodes.size();
	GeoViewLegendJTree_Node node = null;	
	for ( int i = 0; i < size; i++ ) {
		node = (GeoViewLegendJTree_Node)layerNodes.get(i);
		layer_view = node.getLayerView();
		if (layer_view == null) {
			continue;
		}
		layer = layer_view.getLayer();
		if (layer == null) {
			continue;
		}
/*		
		layer_view = (GeoLayerView)layer_views.elementAt(i);
		if ( layer_view == null ) {
			continue;
		}
		layer = layer_view.getLayer();
		if ( layer == null ) {
			continue;
		}
*/		
		layer_type = layer.getAppLayerType();
		// Check the layer type against the types that are to be
		// enabled.  Enable if the layer has no app type or it matches the app type...
		Message.printStatus ( 1, "", "App layer type for " + layer.getFileName() + " is \"" + layer_type + "\"" );
		// Default to not visible...
		layer_view.isVisible ( false );
		node.setVisible(false);
		for ( int j = 0; j < tsize; j++ ) {
			Message.printStatus ( 1, "", "Checking enabled type \"" + (String)enabled_types.get(j) + "\"" );
			if (layer_type.equals("") || layer_type.equalsIgnoreCase((String)enabled_types.get(j)) ) {
				layer_view.isVisible ( true );
				node.setVisible(true);
				did_something = true;
				break;
			}
		}
		if ( layer_view.isVisible() ) {
			Message.printStatus ( 1, "", "Layer view for \"" + layer.getFileName() + "\" is visible.");
		}
		else {
			Message.printStatus ( 1, "", "Layer view for \"" + layer.getFileName() + "\" is not visible.");
		}
	}
	layer_view = null;
	layer = null;
	layer_type = null;
	if ( did_something ) {
		// Update the legend checkboxes...
		//__legendJTree.repaint();
		__legendJTree.invalidate();
		__legendJTree.repaint();
		// Force a redraw of the main map to make sure the layers are
		// shown in agreement with the legend.  If a normal repaint() is
		// called, the image is not updated because the size has not changed...
		__mainGeoView.redraw();
	}
}

/**
Clean up for garbage collection.
*/
protected void finalize()
throws Throwable
{	__mainGeoView = null;
	__refGeoView = null;
	__gvp = null;
	__legendJTree = null;
	__allControlsJPanel = null;
	__parentJFrame = null;

	__refreshJButton = null;
	__printJButton = null;
	__zoomOutJButton = null;

	__modeJComboBox = null;

	__statusJTextField = null;
	__trackerJTextField = null;

	__gvpFile = null;
	__enabledAppLayerTypes = null;
	__displayProps = null;

	__saveAsImageJButton = null;
	__selectJButton = null;
	__zoomJButton = null;
	__infoJButton = null;
	__toolBar = null;
	__propertiesPopup = null;
	__zoomOutXMI = null;

	super.finalize();
}

/**
Determine label for node.  For now always return null.
@param record GeoRecord used to determine label.
*/
public String geoViewGetLabel ( GeoRecord record )
{	return null;
}

/**
Handle GeoView info event.  This will show the information for the selected features.
@param devpt Device coordinates of selection.
@param datapt Data limits of selection.
@param selected Vector of GeoRecord selected from GeoView.
*/
public void geoViewInfo(GRShape devpt, GRShape datapt, List selected) {
	showFeatureInformation ( selected );
}

/**
Handle GeoView info event.  This will show the information for the selected features.
@param devlim Device limits (these are actual device limits in native
device coordinates - Y0 will be at top of window).
@param datalim Data limits.
@param selected Vector of GeoRecord selected from GeoView.
*/
public void geoViewInfo(GRLimits devlim, GRLimits datalim, List selected) {
	showFeatureInformation ( selected );
}

/**
Handle GeoView info event.  This will show the information for the selected features.
@param devpt Device coordinates of selection.
@param datapt Data limits of selection.
@param selected Vector of GeoRecord selected from GeoView.
*/
public void geoViewInfo(GRPoint devpt, GRPoint datapt, List selected) {
	showFeatureInformation ( selected );
}

/**
Handle mouse motion events.  Print the coordinates in a status JTextField.
If the global boolean value __HRAPCoordinates is true and the projection is
not HRAP then display coordinates in HRAP. Also if the global boolean value
__GEOGCoordinates is true (__HRAPCoordinates will be false) and the projection
is not Geographic then display coordinates in Geographic.
@param devpt mouse position in device coordinates.
@param datapt mouse position in data coordinates.
*/
public void geoViewMouseMotion(GRPoint devpt, GRPoint datapt) {
	// Determine whether or not to do a point conversion! First
	// check to see if a conversion is necessary. If so do it.
	if(__HRAPCoordinates == true) {
		HRAPProjection HRAPProj = new HRAPProjection();
		GRPoint newDatapt = (__mainGeoView.getProjection()).unProject(datapt,false);
		datapt = HRAPProj.project(newDatapt,false);
	}
	else if(__GEOGCoordinates == true) {
		(__mainGeoView.getProjection()).unProject(datapt,true);
	}

	// Set the precision string
	String precisionString = "%."+__locatorPrecision+"f";

	// Set the displayed output String
	String text = "X, Y:  " + StringUtil.formatString(datapt.x,precisionString) + "," +
			StringUtil.formatString(datapt.y,precisionString);
	__trackerJTextField.setText ( text );
	text = null;
}

/**
Handle select event; does nothing.
Handle GeoView select event.  Do nothing.
@param devpt Device coordinates.
@param datapt Data coordinates.
@param selected Vector of GeoRecord selected from GeoView.
*/
public void geoViewSelect(GRShape devpt, GRShape datapt, List selected, boolean append) {}

/**
Handle GeoView select event; does nothing.
@param devlim Device limits (these are actual device limits in native
device coordinates - Y0 will be at top of window).
@param datalim Data limits.
@param selected Vector of GeoRecord selected from GeoView.
*/
public void geoViewSelect(GRLimits devlim, GRLimits datalim, List selected, boolean append) {}

/**
Handle select event; does nothing.
Handle GeoView select event.  Do nothing.
@param devpt Device coordinates.
@param datapt Data coordinates.
@param selected Vector of GeoRecord selected from GeoView.
*/
public void geoViewSelect(GRPoint devpt, GRPoint datapt, List selected, boolean append) {}

/**
Handle GeoView zoom event; does nothing.
@param devlim Device limits (these are actual device limits in native
device coordinates - Y0 will be at top of window).
@param datalim Data limits.
*/
public void geoViewZoom(GRShape devlim, GRShape datalim) {}

/**
Handle GeoView zoom event; does nothing.
@param devlim Device limits (these are actual device limits in native
device coordinates - Y0 will be at top of window).
@param datalim Data limits.
*/
public void geoViewZoom(GRLimits devlim, GRLimits datalim) {}

/**
Return the reference to the main GeoView.
@return the main GeoView.
*/
public GeoViewJComponent getGeoView ()
{	return __mainGeoView;
}

/**
Returns a list of the app layer types of the app layers that are currently enabled.
@return a list of the app layer types of the app layers that are currently enabled.
*/
public List getEnabledAppLayerTypes() {
	List layerViews = getGeoView().getLayerViews();			
	GeoLayerView glv = null;
	GeoLayer gl = null;
	
	if (layerViews == null) {
		return null;
	}
	
	int size = layerViews.size();

	List v = new Vector();

	for (int i = 0; i < size; i++) {
		glv = (GeoLayerView)layerViews.get(i);
		if (glv.isVisible()) {
			gl = glv.getLayer();
			v.add(gl.getAppLayerType());
		}
	}

	if (size > 0) {
		return v;
	}
	
	return null;
}

/**
Return the GeoView project file name.  The name will be "" if it has not been set.
@return the GeoView project file name.
*/
public String getGVPFile()
{	return __gvpFile;
}

/**
Return a Vector of GeoLayerView that is being managed in the main canvas for the GeoViewPanel.
@return a Vector of GeoLayerView that is being managed in the main canvas
for the GeoViewPanel, or null if no layer views match the requested criteria.
@param app_layer_types A Vector of application layer types.  If null, all
layer views are returned.  Otherwise, only layer views matching the requested type are returned.
*/
public List getLayerViews ( List app_layer_types )
{	List layer_views = __mainGeoView.getLayerViews();
	if ( app_layer_types == null ) {
		return layer_views;
	}
	GeoLayerView layer_view = null;
	String app_layer_type = null;
	int numlayerviews = layer_views.size();
	List matching_layer_views = null;
	int n_app_layer_types = app_layer_types.size(), j;
	for ( int i = 0; i < numlayerviews; i++ ) {
		layer_view = (GeoLayerView)layer_views.get(i);
		app_layer_type= layer_view.getPropList().getValue ( "AppLayerType");
		if ( app_layer_type == null ) {
			continue;
		}
		for ( j = 0; j < n_app_layer_types; j++ ) {
			if (app_layer_type.equalsIgnoreCase( (String)app_layer_types.get(j)) ) {
				if ( matching_layer_views == null ) {
					matching_layer_views = new Vector();
				}
				matching_layer_views.add ( layer_view );
			}
		}
	}
	return matching_layer_views;
}

/**
Return the reference to the GeoViewLegendPanel.
@return the GeoViewLegendPanel.
@deprecated use getLegendJTree
*/
public GeoViewLegendJTree getLegendJPanel ()
{	return __legendJTree;
}

/**
Return the reference to the GeoViewLegendJTree.
@return the GeoViewLegendJTree.
*/
public GeoViewLegendJTree getLegendJTree() {
	return __legendJTree;
}

/**
Returns the parent JFrame in which this GeoViewJPanel can be found.
@return the parent JFrame in which this GeoViewJPanel can be found.
*/
public JFrame getParentJFrame() {
	return __parentJFrame;
}

/**
Determine whether any of the AppLayerType String match visible layers.
This can be used, for example, to turn on map features when supporting data are available.
@param app_layer_types Vector of AppLayerType String to check.
@return true if any of the specified AppLayerType match the AppLayerType for visible layer views.
*/
public boolean hasAppLayerType ( List app_layer_types )
{	List layer_views = __mainGeoView.getLayerViews();
	GeoLayerView layer_view = null;
	int size = 0;
	if ( app_layer_types != null ) {
		size = app_layer_types.size();
	}
	int isize = 0;
	if ( layer_views != null ) {
		isize = layer_views.size();
	}
	String prop_value = null;
	for ( int i = 0; i < isize; i++ ) {
		layer_view = (GeoLayerView)layer_views.get(i);
		if ( layer_view.isVisible() ) {
			prop_value = layer_view.getPropList().getValue("AppLayerType");
			if ( prop_value == null ) {
				continue;
			}
			for ( int j = 0; j < size; j++ ) {
				if (prop_value.equalsIgnoreCase((String)app_layer_types.get(j)) ){
					layer_view = null;
					layer_views = null;
					prop_value = null;
					return true;
				}
			}
		}
	}
	return false;
}

/**
Handle ItemEvents.
@param evt ItemEvent to handle.
*/
public void itemStateChanged(ItemEvent evt)  {
	Object o = evt.getItemSelectable();
	if ( o.equals(__modeJComboBox) ) {
		String item = (String)__modeJComboBox.getSelectedItem();
		if ( item.equals(MODE_INFO) ) {
			// For now use select and know here that the mode is info...
			__mainGeoView.setInteractionMode ( GeoViewJComponent.INTERACTION_INFO );
		}
		else if ( item.equals(MODE_SELECT) ) {
			__mainGeoView.setInteractionMode ( GeoViewJComponent.INTERACTION_SELECT );
		}
		else if ( item.equals(MODE_SELECT_REGION) ) {
			//__mainGeoView.setInteractionMode ( GeoViewJComponent.INTERACTION_SELECT_REGION );
		}
		else if ( item.equals(MODE_ZOOM) ) {
			__mainGeoView.setInteractionMode( GeoViewJComponent.INTERACTION_ZOOM);
		}
		item = null;
	}
	o = null;
}

/**
Open a GeoView Project (.gvp) file and load into the GeoView.  The user is prompted for a GVP file name.
*/
public void openGVP() {
	String gvp_file = "";
	try {
		setStatus("Opening GeoView Project");
		JGUIUtil.setWaitCursor(__parentJFrame, true);
		// Get the GVP file from the user...
	
		JFileChooser fc = JFileChooserFactory.createJFileChooser( JGUIUtil.getLastFileDialogDirectory() );
		fc.setDialogTitle("Open GeoView Project");
		SimpleFileFilter gff = new SimpleFileFilter("gvp", "GeoView Projects");
		fc.addChoosableFileFilter(gff);
		fc.setFileFilter(gff);
		if ( fc.showOpenDialog(__parentJFrame) != JFileChooser.APPROVE_OPTION) {
			JGUIUtil.setWaitCursor(__parentJFrame, false);
			setStatus("Open cancelled");
			return;
		}
		
		File file = fc.getSelectedFile();
		JGUIUtil.setWaitCursor(__parentJFrame, true);
	
		gvp_file = file.getPath();        
		JGUIUtil.setLastFileDialogDirectory(file.getParent());
		
		openGVP(gvp_file);
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, "GeoViewJPanel.openGVP",
		"Unable to open and display project file \"" + gvp_file + "\"." );
		Message.printWarning ( 2, "GeoViewJPanel.openGVP", e );
	}
	JGUIUtil.setWaitCursor(__parentJFrame, false);
}

/**
Open a GeoView Project (.gvp) file and load into the GeoView, clearing any previous display.
@param gvp_file GeoView project file.
@exception Exception if there is an error opening or processing the file.
*/
public void openGVP ( String gvp_file )
throws Exception
{	openGVP ( gvp_file, false );
}

/**
Open a GeoView Project (.gvp) file and load into the GeoView.
@param gvp_file GeoView project file.
@param append indicates whether the project should be appended to the existing
display.  <b>Currently this is always treated as false.</b>
@param displayMissingLayers if true, display missing layers with available symbology but
have zero-length list of shapes.
@exception Exception if there is an error opening or processing the file.
*/
public void openGVP ( String gvp_file, boolean append )
throws Exception
{	//if ( !append ) {
		// Remove the existing layer views from the display...
		removeAllLayerViews();
	//}

	// Strongly encourage the VM to garbage collection.  
	// This isn't efficient, speed-wise (garbage collection could
	// conceivably run 20 times), but it almost totally guarantees
	// that the memory will be reclaimed.
	/* Remove for now and see if it performs better
	for (int i = 0; i < 20; i++) {
		System.gc();
	}
	*/
	
	JGUIUtil.setWaitCursor(__parentJFrame, true);
	try {
		setStatus("Opening GeoView Project");
		Message.printStatus(1, "", "Opening GeoViewProject file: '" + gvp_file + "'");
		__gvp = new GeoViewProject ( gvp_file );
	
		// Read whether point symbol anti-aliasing should be done
		PropList AA = __gvp.getPropList();
		String aa = AA.getValue("GeoView.AntiAliased");
		if (aa != null && aa.equalsIgnoreCase("true") && __mainGeoView instanceof GRJComponentDevice) {
			__mainGeoView.setAntiAliased(true);
			__refGeoView.setAntiAliased(true);
		}
		else {
			__mainGeoView.setAntiAliased(false);
			__refGeoView.setAntiAliased(false);
		}
	
		// Get Coordinate Locator precision value	
		String X = AA.getValue("GeoView.CoordinatePrecision");
		if (X != null) {
			int x = StringUtil.atoi(X);
			if (x <=0) {}
			else {
				__locatorPrecision = x;
			}
		}
	
		// Read whether to allow zooming out by a small percentage
		if (__zoomOutXMI != null) {
			__propertiesPopup.remove(__zoomOutXMI);
		}
		X = AA.getValue("GeoView.ZoomOutPercent");
		if (X != null) {
			int x = StringUtil.atoi(X);
			if (x <=0 ) {
				// Don't process property
			}
			else {
				__zoomOutAmount = x;
				GEOVIEW_ZOOM_OUT_X = "Zoom out " + __zoomOutAmount + "%";
				if (__zoomOutXMI == null) {
					__propertiesPopup.addSeparator();
				}
				__zoomOutXMI = new JMenuItem(GEOVIEW_ZOOM_OUT_X);
				__zoomOutXMI.addActionListener(this);
				__propertiesPopup.add(__zoomOutXMI);
			}
		}
		
		// Read whether to change read in projection
		__propertiesPopup.addSeparator();
		if (__projHRAP != null) {
			__propertiesPopup.remove(__projHRAP);
		}
		if (__projGEOG != null) {
			__propertiesPopup.remove(__projGEOG);
		}
		X = AA.getValue("GeoView.Projection");
		if (X != null && X.equalsIgnoreCase("HRAP")) {
			__projGEOG = new JMenuItem(CHANGE_TO_GEOG);
			__projGEOG.addActionListener(this);
			__propertiesPopup.add(__projGEOG);
		}
		else if (X != null && X.equalsIgnoreCase("Geographic")) {
			__projHRAP = new JMenuItem(CHANGE_TO_HRAP);
			__projHRAP.addActionListener(this);
			__propertiesPopup.add(__projHRAP);
		}
		
		__mainGeoView.setProject ( __gvp );
		__refGeoView.setProject ( __gvp );	
		// Add the layer views in the GVP to the GeoView...
		__gvp.addToGeoView ( __mainGeoView, __refGeoView, __legendJTree );
		__gvpFile = gvp_file;
		// Check the list of enabled data types.  Typically at startup we
		// only want to display background layers.  As other windows are opened
		// the appropriate layers can be set visible.
		if ( __enabledAppLayerTypes.size() > 0 ) {
			enableAppLayerTypes ( __enabledAppLayerTypes, false );
		}
		// Force the legend panel to redraw since it does not seem to show its contents initially...
		__legendJTree.repaint();
	}
	catch (Exception e) {
		JGUIUtil.setWaitCursor(__parentJFrame, false);
		throw e;
	}
	JGUIUtil.setWaitCursor(__parentJFrame, false);
	setStatus("GeoView Project Opened");
}

/**
Print the current contents of the GeoView.  The user will be prompted to select
a printer and its properties.
*/
private void printGeoView() {	
	setStatus("Printing GeoView Map");
	JGUIUtil.setWaitCursor ( this, true );
	try {
		__mainGeoView.printView();
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, "GoeViewJPanel.printGeoView", "Error printing." );
	}
	JGUIUtil.setWaitCursor ( this, false );
	setStatus("GeoView Map Printed");
}

/**
Refreshes the display, redrawing all the maps.
*/
public void refresh() {
	JGUIUtil.setWaitCursor(__parentJFrame, true);
	__mainGeoView.setWaitCursorAfterRepaint(false);
	__mainGeoView.redraw();
	__refGeoView.redraw();
}

/**
Refreshes the main geo view after features are selected.
JTS thinks this is old and was used for some working development of some 
tools in late 2003 for doing image saving.
*/
public void refreshAfterSelection() {
	__mainGeoView.redraw();
}

/**
Remove all GeoLayerView from the active display.  This can be used when
disabling or refreshing a GeoViewJPanel with a new GeoView project.
*/
public void removeAllLayerViews ()
{	// Remove the layers views from the legend...
	__legendJTree.removeAllLayerViews();

	// Remove the layer views from the GeoView...
	__mainGeoView.deleteLayerViews();
	if ( __refGeoView != null ) {
		__refGeoView.deleteLayerViews();
	}

	// Now redraw the main interface so it is clear (it may actually not
	// be visible but at least the display will be consistent with the layer views)...

	__mainGeoView.redraw();
}

/**
Remove layer views that match an App Layer Type.
@param app_layer_types list of app layer types to remove.
*/
public void removeAppLayerViews ( List app_layer_types )
{	// First get a Vector of matching layer views...
	int size = 0;
	List app_layer_views = new Vector();
	if ( app_layer_types != null ) {
		size = app_layer_types.size();
	}
	List layer_views = __mainGeoView.getLayerViews();
	int vsize = 0;
	if ( layer_views != null ) {
		vsize = layer_views.size();
	}
	String app_layer_type = null;
	GeoLayerView layer_view = null;
	// Find layer views that have app layer types that match the requested type.
	for ( int i = 0; i < size; i++ ) {
		app_layer_type = (String)app_layer_types.get(i);
		for ( int j = 0; j < vsize; j++ ) {
			layer_view = (GeoLayerView)layer_views.get(j);
			if (app_layer_type.equalsIgnoreCase( layer_view.getLayer().getAppLayerType()) ) {
				app_layer_views.add ( layer_view );
			}
		}
	}
	// Now remove them...
	size = app_layer_views.size();
	for ( int i = 0; i < size; i++ ) {
		if ( i == (size - 1) ) {
			// Remove and redraw the view...
			removeLayerView ( (GeoLayerView)app_layer_views.get(i), true );
		}
		else {
			// Remove but do not redraw the view...
			removeLayerView ( (GeoLayerView)app_layer_views.get(i), false );
		}
	}
	app_layer_views = null;
	layer_views = null;
	app_layer_type = null;
	layer_view = null;
}

/**
Remove a GeoLayerView from the active display.  The GeoLayerView is removed from
the list maintained by the GeoView and the legend but is NOT removed from the
GeoView project (if added dynamically at run time, it was never a part of the project).
@param layer_view_to_remove GeoLayerView to remove.
@param redraw Indicates whether the map display should be redrawn.  For
performance reasons this should be specified as false if multiple layer views
are being removed - then redraw after all have been removed (or specify true for the last remove).
*/
public void removeLayerView ( GeoLayerView layer_view_to_remove, boolean redraw )
{	List layer_views = __mainGeoView.getLayerViews();
	int size = 0;
	if ( layer_views != null ) {
		size = layer_views.size();
	}
	GeoLayerView layer_view;
	for ( int i = 0; i < size; i++ ) {
		layer_view = (GeoLayerView)layer_views.get(i);
		// Check reference value...
		if ( layer_view == layer_view_to_remove ) {
			// Remove from the legend first...
			__legendJTree.removeLayerView ( layer_view_to_remove );
			// Remove from the GeoView...
			__mainGeoView.removeLayerView ( layer_view_to_remove, redraw );
			--size;
		}
	}
	layer_view = null;
}

// FIXME SAM 2009-07-01 Need to save projection file.
/**
Save the selected layer as a shapefile.  This is set up mostly for writing
grids in geographic projection.  Later needs to support writing of selected records.
*/
private void saveAs ()
{
	JGUIUtil.setWaitCursor(__parentJFrame, true);

	JFileChooser fc = JFileChooserFactory.createJFileChooser(JGUIUtil.getLastFileDialogDirectory() );
	fc.setDialogTitle("Save as ...");
	SimpleFileFilter sff = new SimpleFileFilter("shp", "ESRI Shapefile");
	fc.addChoosableFileFilter(sff);
	fc.setFileFilter(sff);
	if ( fc.showSaveDialog(__parentJFrame) != JFileChooser.APPROVE_OPTION) {
		JGUIUtil.setWaitCursor(__parentJFrame, false);
		return;
	}
	
	File file = fc.getSelectedFile();
	JGUIUtil.setWaitCursor ( __parentJFrame, true );
	String fileName = file.getPath();
	JGUIUtil.setLastFileDialogDirectory(file.getParent());
	try {
		// Write the first selected layer or if nothing is selected the first layer...
		List layer_views = __mainGeoView.getLayerViews();
		int pos = -1;
		GeoLayerView layer_view = null;
		GeoLayer layer = null;
		int isize = 0;
		if ( layer_views != null ) {
			isize = layer_views.size();
		}
		for ( int i = 0; i < isize; i++ ) {
			layer_view = (GeoLayerView)layer_views.get(i);
			if ( layer_view.isSelected() ) {
				pos = i;
				break;
			}
		}
		if ( (pos < 0) && (isize > 0) ) {
			pos = 0;
		}
		if ( pos < 0 ) {
			// Return without writing...
			JGUIUtil.setWaitCursor(__parentJFrame, false);
			return;
		}		
		// If here write the shapefile...
		layer_view = (GeoLayerView)layer_views.get(pos);
		layer = layer_view.getLayer();
		int shape_type = layer.getShapeType();		
		if ( shape_type == GRShape.GRID ) {
			String prop_value = layer_view.getPropList().getValue("IgnoreDataOutside");
			double min_to_draw = 0.0;
			double max_to_draw = 0.0;
			boolean use_data_limits = false;
			if ( prop_value != null ) {
				List v = StringUtil.breakStringList ( prop_value,",",0);
				prop_value = null;
				if ( (v != null) && (v.size() == 2) ) {
					use_data_limits = true;
					min_to_draw = StringUtil.atod((String)v.get(0));
					max_to_draw = StringUtil.atod((String)v.get(1));
					use_data_limits = true;
				}
				v = null;
				layer.writeShapefile ( fileName,
					getGeoView().getProjection(), use_data_limits, min_to_draw, max_to_draw );
			}
		}
		else {
			// No need to handle special issues...
			if ( layer.getNumSelected() > 0 ) {
				// Write only the selected shapes...
				layer.writeShapefile ( fileName, true, true, getGeoView().getProjection() );
			}
			else {
				// Write all the shapes...
				layer.writeShapefile ( fileName, true, false, getGeoView().getProjection() );
			}
		}
		layer_view = null;
		layer = null;
		layer_views = null;
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, "GeoViewJPanel.saveAs", "Error saving shapefile \"" + fileName + "\"");
		Message.printWarning ( 1, "GeoViewJPanel.saveAs", e );
	}
	JGUIUtil.setWaitCursor(__parentJFrame, false);
	fileName = null;
}

/**
Select features on the map.  The selections are NOT appended to previous selections.
@param app_layer_types If specified, this contains a list of AppLayerType
string properties for layers that should be searched.  Specifying this
information increases the speed of searches.
@param feature_ids The data attributes corresponding to the AppJoinField
property saved with a GeoLayerView.  One or more field values can be given, separated by commas.
@param zoom_to_selected Indicates whether the GeoView should zoom to the selected shapes.
@param zoom_buffer The percent (1.0 is 100%) to expand the visible area in
both directions for the selected shapes.  For example, specifying a value of
1.0 would result in a viewable area that is 50% bigger than selecte shapes on each edge.
@param zoom_buffer2 If the selected shapes result in a region that is a single
point, then zoom_buffer2 can be applied similar to zoom_buffer but using the
dimension of the main view as the reference region.
@return Vector of GeoRecord for the selected features, or null if nothing is selected.
*/
public List selectAppFeatures ( List app_layer_types, List feature_ids, boolean zoom_to_selected,
	double zoom_buffer, double zoom_buffer2)
{	return selectAppFeatures ( app_layer_types, feature_ids, zoom_to_selected, zoom_buffer, zoom_buffer2, false );
}

/**
Select features on the map based on a check of an attribute value (e.g., a
string identifier).  The AppLayerType data in the GeoView project is used to
identify suitable layers for the check.
@param app_layer_types If specified, this contains a list of AppLayerType
string properties for layers that should be searched.  Specifying this
information increases the speed of searches.
@param feature_ids The data attributes corresponding to the AppJoinField
property saved with a GeoLayerView.  One or more field values can be given, separated by commas.
@param zoom_to_selected Indicates whether the GeoView should zoom to the selected shapes.
@param zoom_buffer The percent (1.0 is 100%) to expand the visible area in
both directions for the selected shapes.  For example, specifying a value of
1.0 would result in a viewable area that is 50% bigger than selecte shapes on each edge.
@param zoom_buffer2 If the selected shapes result in a region that is a single
point, then zoom_buffer2 can be applied similar to zoom_buffer but using the
dimension of the main view as the reference region.
@param append Indicates whether the selections should be added to previous
selections.  <b>This feature is under development.</b>
@return list of GeoRecord for the selected features, or null if nothing is selected.
*/
public List selectAppFeatures (	List app_layer_types, List feature_ids, boolean zoom_to_selected,
					double zoom_buffer, double zoom_buffer2, boolean append )
{	String routine = "GeoViewPanel.selectAppFeatures";

	// First loop through all non-baseline layers and set shapes to not selected.

	if ( feature_ids == null ) {
		return null;
	}
	int nfeature = feature_ids.size();
	if ( nfeature == 0 ) {
		return null;
	}
	int napp_layer_types = 0;
	if ( app_layer_types != null ) {
		napp_layer_types = app_layer_types.size();
	}
	// Break the features_ids into a 2-D array of strings for examination
	// below.  It is assumed that the first feature_id has the correct number of fields...
	List georecords = null;
	List v = StringUtil.breakStringList ( (String)feature_ids.get(0), ",", 0 );
	if ( (v == null) || (v.size() == 0) ) {
		return null;
	}
	int nfeature_parts = v.size();
	String[][] feature_array = new String[nfeature][nfeature_parts];
	for ( int i = 0; i < nfeature; i++ ) {
		v = StringUtil.breakStringList ( (String)feature_ids.get(i), ",", 0 );
		for ( int j = 0; j < nfeature_parts; j++ ) {
			feature_array[i][j] = (String)v.get(j);
		}
	}
	v = null;
	
	List layer_views = __mainGeoView.getLayerViews();
	int numlayerviews = 0;
	if ( layer_views != null ) {
		numlayerviews = layer_views.size();
	}
	GeoLayerView layer_view = null;
	GeoLayer layer = null;
	String prop_value = null;
	for ( int i = 0; i < numlayerviews; i++ ) {
		layer_view = (GeoLayerView)layer_views.get(i);
		//prop_value = layer_view.getPropList().getValue ( "AppLayerType");
		//if ( prop_value.equalsIgnoreCase("BaseLayer") ) {
			//continue;
		//}
		layer = (GeoLayer)layer_view.getLayer();
		layer.deselectAllShapes();
	}

	// Now loop through all non-baseline layers and search for the features...

	List lv_records = null;	// Records selected in a layer view.
	String join_field; // Fields to join the application data to the spatial data
	List join_fields_Vector; // join_field parsed with ","

	for ( int i = 0; i < numlayerviews; i++ ) {	
		layer_view = (GeoLayerView)layer_views.get(i);
		layer = (GeoLayer)layer_view.getLayer();

		// See if the app layer type matches the types that should be searched...
		if ( napp_layer_types > 0 ) {
			boolean layer_type_matches = false;
			for ( int j = 0; j < napp_layer_types; j++ ) {
				if (layer.getAppLayerType().equalsIgnoreCase((String)app_layer_types.get(j)) ){
					layer_type_matches = true;
					break;
				}
			}
			if ( !layer_type_matches ) {
				continue;
			}
		}
		// Layers that are not visible don't need to be searched...
		if ( !layer_view.isVisible() ) {
//			System.out.println("   (not visible)");
			continue;
		}
		// Base layers cannot be searched...
		prop_value = layer_view.getPropList().getValue ("AppLayerType");
		if ((prop_value != null) && prop_value.equalsIgnoreCase("BaseLayer") ) {
//			System.out.println("   (base layer)");
			continue;
		}
		// Get the join field...
		join_field = layer_view.getPropList().getValue ("AppJoinField");
		if ( join_field == null ) {
			// The layer is not attached to any application data so return...
//			System.out.println("   (not attached to app data)");
			continue;
		}
		join_fields_Vector = StringUtil.breakStringList ( join_field, ",", 0 );
		if ( join_fields_Vector == null ) {
			// No need to process layer...
//			System.out.println("   (null join fields)");
			continue;
		}

		// Select shapes in the layer view...
		lv_records = layer_view.selectFeatures ( feature_array, join_field, append);
		// if not null, add to the main list...
		if ( lv_records != null ) {
			if ( georecords == null ) {
				georecords = lv_records;
			}
			else {
				// Transfer...
				int lv_size = lv_records.size();
				for ( int ilv = 0; ilv < lv_size; ilv++ ) {
					georecords.add ( lv_records.get(ilv) );
				}
			}
		}
	}

	int match_count = 0;
	if ( georecords != null ) {
		match_count = georecords.size();
	}
	Message.printStatus ( 1, routine, "Found " + match_count + " matches for " + nfeature + " features." );

	GRShape shape = null;
	if ( match_count > 0 ) {
		// Something matched so we need to update the view...
		if ( zoom_to_selected ) {
			// First determine the limits of the data that are returned...
			int size = georecords.size();
			GRLimits datalimits = null; 
			GeoRecord georecord;
			for ( int i = 0; i < size; i++ ) {
				// Have to check for zero because some shapes
				// don't have coordinates...  For now check only the max...
				georecord = (GeoRecord)georecords.get(i);
				shape = georecord.getShape();
				if ( zoom_to_selected && (shape.xmin != 0.0) ) {
					if ( datalimits == null) {
						datalimits = new GRLimits( shape.xmin, shape.ymin, shape.xmax, shape.ymax );
					}
					else {	
						datalimits = datalimits.max( shape.xmin, shape.ymin, shape.xmax, shape.ymax, true );
					}
				}
			}
			// Increase the limits...
			double xincrease = 0.0, yincrease = 0.0;
			if (datalimits.getMinX() == datalimits.getMaxX()) {
				xincrease = __mainGeoView.getDataLimits().getWidth() * zoom_buffer2;
			}
			else {	
				xincrease = datalimits.getWidth() * zoom_buffer;
			}
			if (datalimits.getMinY() == datalimits.getMaxY()) {
				yincrease = __mainGeoView.getDataLimits().getHeight() * zoom_buffer2;
			}
			else {	
				yincrease = datalimits.getHeight() *zoom_buffer;
			}
			datalimits.increase(xincrease, yincrease);
			Message.printStatus(1, routine, "2ooming to " + datalimits.toString());
			__mainGeoView.geoViewZoom(null, datalimits);
			__refGeoView.geoViewZoom(null, datalimits);
		}
		else {
			// Just force a redraw...
			__mainGeoView.redraw();
		}
	}

	return georecords;
}

/**
Select features on the map based on a check of an attribute value (e.g., a
string identifier).  A list of layers, and corresponding attribute to check must be provided.
@param layer_list If specified, this contains a list of layers or layer names
(must be consistent in the list).
@param attribute_list The list of attributes, one per layer, to match in the
searched layers.  For example, the "ID" column may be used for one layer and
the "Name" column may be used for an other layer.
@param feature_ids The data attributes to be matched in layer attributes.
Each layer can have a single attribute.
@param props Properties to control the select, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>Append</b></td>
<td>
Indicates whether the selections should be added to previous
selections.  <b>This feature is under development.</b>
</td>
<td>False</td>
</tr>

<tr>
<td><b>ZoomToSelected</b></td>
<td>
Indicates whether the GeoView should zoom to the selected shapes.  See
ZoomBuffer and ZoomBufferMain for properties to control the zoom.
</td>
<td>False (select but do not zoom)</td>
</tr>

<tr>
<td><b>ZoomBuffer</b></td>
<td>
The fraction (0 to 1.0) to expand the visible area in
both directions for the selected shapes.  For example, specifying a value of
1.0 would result in a viewable area that is increased by the size of the
selected area, .50 of the length in each coordinate direction.
</td>
<td>.05</td>
</tr>

<tr>
<td><b>ZoomBufferMain</b></td>
<td>
If the selected shapes result in a region that is a single point, then this
parameter dimension of the main view as the reference region.
The value indicates the fraction (0 to 1.0) of the main extents to center on the point.
</td>
<td>.05</td>
</tr>

</table>

@return Vector of GeoRecord for the selected features, or null if nothing is
selected.  At a minimum, the size of this Vector can be used by calling code
to determine wether the count of input items is less than the number matched.
@exception Exception if there is an error selecting features (e.g., properties are not valid).
*/
public List selectLayerFeatures ( List layer_list, List attribute_list, List feature_ids, PropList props )
throws Exception
{	String routine = "GeoViewPanel.selectLayerFeatures";

	// First loop through all non-baseline layers and set shapes to not selected.

	if ( feature_ids == null ) {
		return null;
	}

	// Translate properties into internal data to speed performance...

	if ( props == null ) {
		props = new PropList ( "select" );
	}
	boolean append = false;
	String prop_value = props.getValue ( "Append" );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("true") ) {
		append = true;
	}
	boolean zoom_to_selected = false;
	prop_value = props.getValue ( "ZoomToSelected" );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("true") ) {
		zoom_to_selected = true;
	}
	double zoom_buffer = .05;
	prop_value = props.getValue ( "ZoomBuffer" );
	if ( (prop_value != null) && StringUtil.isDouble(prop_value) ) {
		zoom_buffer = StringUtil.atod ( prop_value );
	}
	double zoom_buffer2 = .05;
	prop_value = props.getValue ( "ZoomBufferMain" );
	if ( (prop_value != null) && StringUtil.isDouble(prop_value) ) {
		zoom_buffer2 = StringUtil.atod ( prop_value );
	}

	int nfeature = feature_ids.size();
	if ( nfeature == 0 ) {
		return null;
	}
	int layer_list_size = 0;
	if ( layer_list != null ) {
		layer_list_size = layer_list.size();
	}
	/* TODO SAM 2006-01-16 Comment out for now - may support AppLayerType queries later.
	int napp_layer_types = 0;
	if ( app_layer_types != null ) {
		napp_layer_types = app_layer_types.size();
	}
	*/
	// TODO SAM 2006-01-16
	// This may not be needed but keep for now in case the AppType queries
	// are added to this method at some point.
	// Break the features_ids into a 2-D array of strings for examination
	// below.  It is assumed that the first feature_id has the correct number of fields...
	List georecords = null;
	List v = StringUtil.breakStringList ( (String)feature_ids.get(0), ",", 0 );
	if ( (v == null) || (v.size() == 0) ) {
		return null;
	}
	int nfeature_parts = v.size();
	String[][] feature_array = new String[nfeature][nfeature_parts];
	for ( int i = 0; i < nfeature; i++ ) {
		v = StringUtil.breakStringList ( (String)feature_ids.get(i), ",", 0 );
		for ( int j = 0; j < nfeature_parts; j++ ) {
			feature_array[i][j] = (String)v.get(j);
		}
	}
	v = null;
	
	List layer_views = __mainGeoView.getLayerViews();
	int numlayerviews = 0;
	if ( layer_views != null ) {
		numlayerviews = layer_views.size();
	}
	GeoLayerView layer_view = null;
	GeoLayer layer = null;
	for ( int i = 0; i < numlayerviews; i++ ) {
		layer_view = (GeoLayerView)layer_views.get(i);
		//prop_value = layer_view.getPropList().getValue ("AppLayerType");
		//if ( prop_value.equalsIgnoreCase("BaseLayer") ) {
			//continue;
		//}
		layer = (GeoLayer)layer_view.getLayer();
		layer.deselectAllShapes();
	}

	// Now loop through specified layers and search for the features...

	List lv_records = null;	// Records selected in a layer view.
	String join_field; // Fields to join the application data to the spatial data

	boolean layer_match = false; // Used to track layer matches.
	int layer_match_j = 0;
	Object o; // Used to get layer list object, which can be a layer name String or GeoLayer (supported later).
	String layer_name = null; // Layer name (as displayed), used to lookup layers requested for the search.
	for ( int i = 0; i < numlayerviews; i++ ) {	
		layer_view = (GeoLayerView)layer_views.get(i);
		layer = (GeoLayer)layer_view.getLayer();

		layer_match = false;
		for ( int j = 0; j < layer_list_size; j++ ) {
			o = layer_list.get(j);
			if ( o instanceof String ) {
				// Check the layer name...
				layer_name = (String)o;
				//Message.printStatus ( 2, routine,
				//"Checking layer view \"" +
				//layer_view.getLegend().getText() +
				//"\" against \"" + layer_name + "\"" );
				// TODO SAM 2006-01-16
				// The layer view name is actually stored with
				// legend information for the layer.  This needs to be reviewed.
				if ( layer_name.equalsIgnoreCase(layer_view.getLegend().getText()) ) {
					layer_match = true;
					layer_match_j = j;
					break;
				}
			}
			else {
				// TODO SAM 2006-01-16 Need to evaluate supporting GeoLayer and GeoLayerView here.
				continue;
			}
		}
		if ( !layer_match ) {
			continue;
		}

		// TODO SAM 2006-01-16 May take the following out but leave in for now in case
		// App type queries are supported in the future.
		// See if the app layer type matches the types that should be searched...
		/*
		if ( napp_layer_types > 0 ) {
			boolean layer_type_matches = false;
			for ( int j = 0; j < napp_layer_types; j++ ) {
				if (layer.getAppLayerType().equalsIgnoreCase((String)app_layer_types.elementAt(j)) ){
					layer_type_matches = true;
					break;
				}
			}
			if ( !layer_type_matches ) {
				continue;
			}
		}
		*/

		// Layers that are not visible don't need to be searched...
		if ( !layer_view.isVisible() ) {
//			System.out.println("   (not visible)");
			continue;
		}
		// Base layers cannot be searched...
		prop_value = layer_view.getPropList().getValue ("AppLayerType");
		if ((prop_value != null) &&
			prop_value.equalsIgnoreCase("BaseLayer") ) {
//			System.out.println("   (base layer)");
			continue;
		}
		/* TODO SAM 2006-01-16
			Leave for now in case AppLayerType query needs to be
			supported in the future.  For now use the passed-in attributes.
		// Get the join field...
		join_field = layer_view.getPropList().getValue ("AppJoinField");
		if ( join_field == null ) {
			// The layer is not attached to any application data so return...
//			System.out.println("   (not attached to app data)");
			continue;
		}
		join_fields_Vector = StringUtil.breakStringList ( join_field, ",", 0 );
		if ( join_fields_Vector == null ) {
			// No need to process layer...
//			System.out.println("   (null join fields)");
			continue;
		}
		*/
		join_field = (String)attribute_list.get(layer_match_j);
		Message.printStatus ( 2, routine, "Selecting from " + layer_view.getName() + " using join field " +
			join_field );

		// Select shapes in the layer view...
		lv_records = layer_view.selectFeatures ( feature_array, join_field, append );
		// if not null, add to the main list...
		if ( lv_records != null ) {
			if ( georecords == null ) {
				georecords = lv_records;
			}
			else {
				// Transfer...
				int lv_size = lv_records.size();
				for ( int ilv = 0; ilv < lv_size; ilv++ ) {
					georecords.add ( lv_records.get(ilv) );
				}
			}
		}
	}

	int match_count = 0;
	if ( georecords != null ) {
		match_count = georecords.size();
	}
	Message.printStatus ( 1, routine, "Found " + match_count + " matches for " + nfeature + " features." );

	GRShape shape = null;
	if ( match_count > 0 ) {
		// Something matched so we need to update the view...
		if ( zoom_to_selected ) {
			// First determine the limits of the data that are returned...
			int size = georecords.size();
			GRLimits datalimits = null; 
			GeoRecord georecord;
			for ( int i = 0; i < size; i++ ) {
				// Have to check for zero because some shapes
				// don't have coordinates...  For now check only the max...
				georecord = (GeoRecord)georecords.get(i);
				shape = georecord.getShape();
				if ( zoom_to_selected && (shape.xmin != 0.0) ) {
					if ( datalimits == null) {
						datalimits = new GRLimits(shape.xmin,shape.ymin,shape.xmax,shape.ymax );
					}
					else {	
						datalimits = datalimits.max(shape.xmin,shape.ymin,shape.xmax,shape.ymax,true );
					}
				}
			}
			// Increase the limits...
			double xincrease = 0.0, yincrease = 0.0;
			if (datalimits.getMinX() == datalimits.getMaxX()) {
				xincrease = __mainGeoView.getDataLimits().getWidth() * zoom_buffer2;
			}
			else {	
				xincrease = datalimits.getWidth() * zoom_buffer;
			}
			if (datalimits.getMinY() == datalimits.getMaxY()) {
				yincrease = __mainGeoView.getDataLimits().getHeight() * zoom_buffer2;
			}
			else {	
				yincrease = datalimits.getHeight() *zoom_buffer;
			}
			datalimits.increase(xincrease, yincrease);
			Message.printStatus(1, routine, "2ooming to " + datalimits.toString());
			__mainGeoView.geoViewZoom(null, datalimits);
			__refGeoView.geoViewZoom(null, datalimits);
		}
		else {
			// Just force a redraw...
			__mainGeoView.redraw();
		}
	}

	return georecords;
}

/**
Select features on the map based on an intersection with a shape.  Typically
this is called when a radius or box is used to select the features.
@param shape Shape to use for select (in projected units).
@param app_layer_types If specified, this contains a list of AppLayerType
string properties for layers that should be searched.  Specifying this
information increases the speed of searches.
@param zoom_to_selected Indicates whether the GeoView should zoom to the selected shapes.
@param zoom_buffer The percent (1.0 is 100%) to expand the visible area in
both directions for the selected shapes.  For example, specifying a value of
1.0 would result in a viewable area that is 50% bigger than selecte shapes on each edge.
@param zoom_buffer2 If the selected shapes result in a region that is a single
point, then zoom_buffer2 can be applied similar to zoom_buffer but using the
dimension of the main view as the reference region.
@param append Indicates whether the selections should be added to previous
selections.  <b>This feature is under development.</b>
@return A Vector of GeoRecord for the selected features, or null if nothing is selected.
*/
public List selectAppFeatures (	List app_layer_types, GRShape shape, boolean zoom_to_selected,
	double zoom_buffer, double zoom_buffer2, boolean append )
{	String routine = "GeoViewPanel.selectAppFeatures";
	// A general method is available in GeoViewCanvas...
	List georecords = __mainGeoView.selectGeoRecords (
		shape, app_layer_types, GeoViewJComponent.INTERACTION_SELECT, append );
	int match_count = 0;
	if ( georecords != null ) {
		match_count = georecords.size();
	}
	Message.printStatus ( 1, routine, "Found " + match_count + " matches for shape." );

	// Now do the zoom...

	if ( match_count > 0 ) {
		// Something matched so we need to update the view
		if ( zoom_to_selected ) {
			GRLimits datalimits = null;
			for ( int i = 0; i < match_count; i++ ) {
				if ( i == 0 ) {
					datalimits = new GRLimits (shape.xmin, shape.ymin,shape.xmax, shape.ymax );
				}
				else {
					datalimits = datalimits.max (shape.xmin, shape.ymin,shape.xmax, shape.ymax, true );
				}
			}
			// Increase the limits...
			double xincrease = 0.0, yincrease = 0.0;
			if ( datalimits.getMinX() == datalimits.getMaxX() ) {
				xincrease = __mainGeoView.getDataLimits().getWidth()*zoom_buffer2;
			}
			else {
				xincrease = datalimits.getWidth()*zoom_buffer;
			}
			if ( datalimits.getMinY() == datalimits.getMaxY() ) {
				yincrease = __mainGeoView.getDataLimits().getHeight()* zoom_buffer2;
			}
			else {
				yincrease = datalimits.getHeight()*zoom_buffer;
			}
			datalimits.increase ( xincrease, yincrease );
			Message.printStatus ( 1, routine, "Zooming to " + datalimits.toString() );
			__mainGeoView.geoViewZoom ( null, datalimits );
			__refGeoView.geoViewZoom ( null, datalimits );
		}
		else {
			// Just force a redraw...
			__mainGeoView.redraw();
		}
	}
	// Return the records...
	return georecords;
}

/**
Turns on or off the buttons in the toolbar based on whether anything is 
selected.  This is called by the legend JTree as layers are turned on and off.
@param selected if true then the buttons are enabled.  If they are not enabled.
*/
public void setButtonsEnabledByLayersSelected(boolean selected) {
	if (selected) {
		__infoJButton.setEnabled(true);
		__selectJButton.setEnabled(true);
		__zoomJButton.setEnabled(true);

		__mainGeoView.setInteractionMode(GeoViewJComponent.INTERACTION_NONE);

		if (__infoJButton.isSelected()) {
			__mainGeoView.setInteractionMode(GeoViewJComponent.INTERACTION_INFO);
		}
	
		if (__selectJButton.isSelected()) {
			__mainGeoView.setInteractionMode(GeoViewJComponent.INTERACTION_SELECT);
		}
	
		if (__zoomJButton.isSelected()) {
			__mainGeoView.setInteractionMode(GeoViewJComponent.INTERACTION_ZOOM);
		}		
	}
	else {
		int mode = __mainGeoView.getInteractionMode();
		if (mode != GeoViewJComponent.INTERACTION_ZOOM) {
			__mainGeoView.setInteractionMode(GeoViewJComponent.INTERACTION_NONE);	
		}
		__infoJButton.setEnabled(false);
		__selectJButton.setEnabled(false);
		__zoomJButton.setEnabled(true);
	}
}

/**
Sets data limits in the reference map.
@param limits the limits to set in the reference map.
*/
public void setReferenceMapDataLimits(GRLimits limits) {
	__refGeoView.setDataLimits(limits);
}

/**
Sets text in the status field.
@param text the text to show in the field.
*/
public void setStatus(String text) {
	__statusJTextField.setText(text);
}

/**
Sets whether the hourglass should reappear after the geo view is redrawn.
TODO (JTS - 2004-11-01) this method is always setting to false in main geo view!  What's going on here?
@param waitCursorOn if true then the wait cursor will reappear after drawing.
*/
public void setWaitCursorAfterRepaint(boolean waitCursorOn) {
	__mainGeoView.setWaitCursorAfterRepaint(false);
}

/**
Sets up the GUI.
@param toolBar the application tool bar to which to add this Panel's tool bar
(if null, a new tool bar will be declared and used for this panel).
@param display_props props for the display
@param field1 textfield to use for the left status textfield.  if null, one will be generated
@param field2 textfield to use for the right status textfield.  if null, one will be generated
*/
private void setupGUI(JToolBar toolBar, PropList display_props,JTextField field1, JTextField field2)
{	String routine = getClass().getName() + ".setupGUI";
	GridBagLayout gbl = new GridBagLayout();

	Insets insetsTLBR = new Insets ( 2, 2, 0, 0 );	 // space around canvas

	// Make sure there are always properties...

	if ( display_props == null ) {
		__displayProps = new PropList ( "GeoViewPanel Display Props" );
	}
	else {
		__displayProps = display_props;
	}

	// Set the font properties.
	String font_name = __displayProps.getValue ( "GeoView.FontName" );
	if ( font_name != null ) {
		// Get other font properties...
		String font_style =__displayProps.getValue("GeoView.FontStyle");
		if ( font_style == null ) {
			font_style = "PLAIN";
		}
		String font_size = __displayProps.getValue ("GeoView.FontSize");
		if ( font_size == null ) {
			font_size = "10";
		}
	}
	
	setLayout ( gbl );
	int y = 0;

	JPanel leftPane = new JPanel();
	leftPane.setLayout(gbl);
	leftPane.setPreferredSize(new Dimension(190, 550));
	JPanel rightPane = new JPanel();
	rightPane.setLayout(gbl);
	rightPane.setPreferredSize(new Dimension(400, 550));

	// legend is 2 wide, canvas 8.  All components have the same Y but
	// only legend, reference, and main canvas are resizable.

	__legendJTree = new GeoViewLegendJTree(this);

	//__legendJTree.setSize(100,200);
	//__legendJTree.setPreferredSize(new Dimension(190,300));
	//JGUIUtil.addComponent ( this, __legendJTree, 0, y, 2, 1, 0, 1,
	
	JGUIUtil.addComponent ( leftPane, 
		new JScrollPane(__legendJTree), 0, 0, 1, 1, 1, 1,
		insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.NORTH );

	++y;
	__refGeoView = new GeoViewJComponent ( __parentJFrame, display_props );
	__refGeoView.isReference( true );
	__refGeoView.setInteractionMode( GeoViewJComponent.INTERACTION_ZOOM );
	//__refGeoView.setSize ( 100, 100 );
	//__refGeoView.setSize ( 200, 75);
	__refGeoView.setMinimumSize(new Dimension(75, 100));
	__refGeoView.setPreferredSize ( new Dimension(75,100) );
	__refGeoView.setBackground ( Color.white );
	//JGUIUtil.addComponent ( this, __refGeoView, 0, y, 2, 1, 0, 0,
	JGUIUtil.addComponent ( leftPane, 
		__refGeoView
//		new JScrollPane(__refGeoView)
		, 0, 1, 1, 1, 0, 0,
		insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.SOUTH );
			//insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.SOUTH );
			//insetsTLBR, gbc.NONE, GridBagConstraints.SOUTH );

	// Each GeoView listens to the other to be able to react to zoom events.
	// This panel listens to each GeoView to be able to display the mouse coordinates.

	// Add the main map...

	__mainGeoView = new GeoViewJComponent ( __parentJFrame, display_props );
	__mainGeoView.addRemindedRepainter(__refGeoView);
	__mainGeoView.setInteractionMode ( GeoViewJComponent.INTERACTION_ZOOM );
	__mainGeoView.setBackground( Color.white );
	__mainGeoView.setPreferredSize ( new Dimension(300, 400) );
	__mainGeoView.setMinimumSize ( new Dimension(100, 100) );
	__mainGeoView.setLegendJTree(__legendJTree);

	//JGUIUtil.addComponent ( rightPane, __mainGeoView, 2, y, 8, 1, 1, 1,
	JGUIUtil.addComponent ( rightPane, 
		__mainGeoView,
		0, 0, 1, 1, 1, 1,
		insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.NORTH );

	// Add the reference map...

	__mainGeoView.addGeoViewListener(this);
	__mainGeoView.addGeoViewListener(__refGeoView);
	__refGeoView.addGeoViewListener(this);
	__refGeoView.addGeoViewListener(__mainGeoView);

	// add the popup menu

	JPopupMenu popup = new JPopupMenu();
	JMenuItem propertiesMI = new JMenuItem(GEOVIEW_PROPERTIES);
	propertiesMI.addActionListener(this);
	JMenuItem zoomOutMI = new JMenuItem(GEOVIEW_ZOOM_OUT);
	zoomOutMI.addActionListener(this);
	popup.add(zoomOutMI);
	popup.addSeparator();
	popup.add(propertiesMI);
	__mainGeoView.setPopupMenu(popup);

	__propertiesPopup = popup;

	__allControlsJPanel = new JPanel ();
	__allControlsJPanel.setLayout ( gbl );

	Insets none = new Insets(0, 0, 0, 0);

	boolean priorToolbar = false;
	if (toolBar == null) {
		__toolBar = new JToolBar("GeoView Control Buttons");
	}
	else {
		priorToolbar = true;
		__toolBar = toolBar;
	}

/*
	__openJButton = new SimpleJButton(new ImageIcon(iconLocation + "icon_openProject.gif"),
		OPEN_GVP, "Open GeoView Project", none, false, this);
	__toolBar.add(__openJButton);

	__addJButton = new SimpleJButton(new ImageIcon(iconLocation + "icon_addLayer.gif"),
		ADD, "Add Layer to GeoView", none, false, this);	
	__toolBar.add(__addJButton);
*/	

	URL url = null;
	String url_string = __resource_home + "/icon_print.gif";
	try {
		url = this.getClass().getResource( url_string );
		__printJButton = new SimpleJButton( new ImageIcon(url), PRINT, "Print Map", none, false, this);	
	}
	catch ( Exception e ) {
		// Not able to find graphic...
		Message.printWarning( 3, routine, e );
		url = null;
	}
	if (url == null) {
		// Add a button with only strings...
		Message.printWarning( 3, routine,
			"Unable to load graphic \"" + url_string + "\" - report to software support.");
		__printJButton = new SimpleJButton( "Print", PRINT, "Print Map", none, false, this);	
	}
	__toolBar.add(__printJButton);

	url_string = __resource_home + "/icon_saveAsImage.gif";
	try {
		url = this.getClass().getResource( url_string );
		__saveAsImageJButton = new SimpleJButton(
			new ImageIcon(url), SAVE_AS_IMAGE, "Save Map as Image", none, false, this);	
	}
	catch ( Exception e ) {
		url = null;
	}
	if ( url == null ) {
		// Add a button with only strings...
		Message.printWarning( 3, routine,
				"Unable to load graphic \"" + url_string + "\" - report to software support.");
		__saveAsImageJButton = new SimpleJButton(
			"Save", SAVE_AS_IMAGE, "Save Map as Image", none, false, this);
	}	
	__toolBar.add(__saveAsImageJButton);
	
	/*
	__saveAsJButton = new SimpleJButton(
		new ImageIcon(iconLocation + "icon_saveAs.gif"),
		SAVE_AS_SHAPEFILE, "Save Map as ...", none, false, this);	
	__toolBar.add(__saveAsJButton);
*/
/*	
	__toolBar.add(new JToolBar.Separator());

	__animateJButton = new SimpleJButton(
		new ImageIcon(iconLocation + "icon_play.gif"), ANIMATE, "Play animation", none, false, this);	
	__animateJButton.setEnabled(false);
	__toolBar.add ( __animateJButton );

	__pauseJButton = new SimpleJButton(
		new ImageIcon(iconLocation + "icon_pause.gif"), PAUSE, "Pause animation", none, false, this);		
	__pauseJButton.setEnabled(false);
	__toolBar.add ( __pauseJButton );

	__stopJButton = new SimpleJButton(
		new ImageIcon(iconLocation + "icon_stop.gif"), STOP, "Go to animation beginning", none, false, this);		
	__stopJButton.setEnabled(false);
	__toolBar.add ( __stopJButton );

	__previousJButton = new SimpleJButton(
		new ImageIcon(iconLocation + "icon_rewind.gif"), PREVIOUS, "Step backwards", none, false, this);		
	__previousJButton.setEnabled(false);
	__toolBar.add ( __previousJButton );

	__nextJButton = new SimpleJButton(
		new ImageIcon(iconLocation + "icon_fastforward.gif"), NEXT, "Step forwards", none, false, this);
	__nextJButton.setEnabled(false);
	__toolBar.add ( __nextJButton );

	__endJButton = new SimpleJButton(
		new ImageIcon(iconLocation + "icon_end.gif"), END, "Go to animation end", none, false, this);		
	__endJButton.setEnabled(false);
	__toolBar.add ( __endJButton );
*/
	__toolBar.addSeparator();

	url_string = __resource_home + "/icon_refresh.gif";
	try {
		url = this.getClass().getResource( url_string );
		__refreshJButton = new SimpleJButton( new ImageIcon(url), REFRESH, "Refresh Map", none, false, this);		
	}
	catch ( Exception e ) {
		url = null;
	}
	if ( url == null ) {
		// Add a button with only strings...
		Message.printWarning( 3, routine,
				"Unable to load graphic \"" + url_string + "\" - report to software support.");
		__refreshJButton = new SimpleJButton( "Refresh", REFRESH, "Refresh Map", none, false, this);
	}		
	__refreshJButton.setActionCommand(REFRESH);
	__toolBar.add(__refreshJButton);
	
	url_string = __resource_home + "/icon_zoomToFullExtents.gif";
	try {
		url = this.getClass().getResource( url_string );
		__zoomOutJButton = new SimpleJButton( new ImageIcon(url),
			GEOVIEW_ZOOM_OUT, "Zoom to full extent of Map", none, 
			false, 	this);
	}
	catch ( Exception e ) {
		url = null;
	}
	if ( url == null ) {
		// Add a button with only strings...
		Message.printWarning( 3, routine,
				"Unable to load graphic \"" + url_string + "\" - report to software support.");
		__zoomOutJButton = new SimpleJButton(
			"Zoom to Full Extent", GEOVIEW_ZOOM_OUT, "Zoom to full extent of Map", none, false, this);
	}
	__zoomOutJButton.setActionCommand(GEOVIEW_ZOOM_OUT);	
	__toolBar.add(__zoomOutJButton);	

	url_string = __resource_home + "/icon_zoomMode.gif";
	try {
		url = this.getClass().getResource( url_string );
		__zoomJButton = new SimpleJToggleButton(
			new ImageIcon(url), MODE_ZOOM, "Enter Zoom Mode", none, false, this, true);
	}
	catch ( Exception e ) {
		url = null;
	}
	if ( url == null ) {
		// Add a button with only strings...
		Message.printWarning( 3, routine,
				"Unable to load graphic \"" + url_string + "\" - report to software support.");
		__zoomJButton = new SimpleJToggleButton(
			"Zoom Mode", MODE_ZOOM, "Enter Zoom Mode", none, false, this, true);
	}	
	__zoomJButton.setActionCommand(MODE_ZOOM);
	__toolBar.add(__zoomJButton);	

	url_string = __resource_home + "/icon_infoMode.gif";
	try {
		url = this.getClass().getResource( url_string );
		__infoJButton = new SimpleJToggleButton(
			new ImageIcon(url), MODE_INFO, "Enter Info Mode", none, false, this, false);
	}
	catch ( Exception e ) {
		url = null;
	}
	if ( url == null ) {
		// Add a button with only strings...
		Message.printWarning( 3, routine,
				"Unable to load graphic \"" + url_string + "\" - report to software support.");
		__infoJButton = new SimpleJToggleButton(
			"Info Mode", MODE_INFO, "Enter Info Mode", none, false, this, false);
	}	
	__infoJButton.setActionCommand(MODE_INFO);
	__infoJButton.setEnabled(false);
	__toolBar.add(__infoJButton);	
	
	url_string = __resource_home + "/icon_selectMode.gif";
	try {
		url = this.getClass().getResource( url_string );
		__selectJButton = new SimpleJToggleButton(
			new ImageIcon(url), MODE_SELECT, "Enter Select Mode", none, false, this, false);
	}
	catch ( Exception e ) {
		url = null;
	}
	if ( url == null ) {
		// Add a button with only strings...
		Message.printWarning( 3, routine,
				"Unable to load graphic \"" + url_string + "\" - report to software support.");
		__selectJButton = new SimpleJToggleButton(
			"Select Mode", MODE_SELECT, "Enter Select Mode", none, false, this, false);
	}	
	__selectJButton.setActionCommand(MODE_SELECT);
	__selectJButton.setEnabled(false);
	__toolBar.add(__selectJButton);	

	if (field1 == null) {
		__statusJTextField = new JTextField ();
		__statusJTextField.setEditable ( false );
		JGUIUtil.addComponent ( __allControlsJPanel, __statusJTextField,
			0, 0, 1, 1, 1, 1,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
		__mainGeoView.setStatusTextField ( __statusJTextField );
	}
	else {
		__statusJTextField = field1;
	}

	if (field2 == null) {	
		__trackerJTextField = new JTextField ();
		__trackerJTextField.setEditable ( false );
		JGUIUtil.addComponent ( __allControlsJPanel, 
			__trackerJTextField,
			1, 0, 1, 1, 1, 1,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );		
	}
	else {
		__trackerJTextField = field2;
	}
   	JGUIUtil.addComponent ( rightPane, 
		__allControlsJPanel,
		0, 1, 1, 1, 1, 0,
   		insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.SOUTH );

	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		leftPane, rightPane);

	JPanel bigPanel = new JPanel();
	bigPanel.setLayout(new BorderLayout());
	bigPanel.add(splitPane, "Center");
	if (!priorToolbar) {
		bigPanel.add(__toolBar, "North");
	}
	JGUIUtil.addComponent(this, bigPanel,
		0, 0, 1, 1, 1, 1, 
		insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	// Check the EnabledAppLayerTypes property.  If not defined, then all
	// layers that are added are enabled by default.  If the property is
	// defined, then it can be used to limit the initial display to enabled layers.

	// The following sets the enabled application layer types.  For example,
	// at startup, an application may have only "BaseLayer" application types enabled.
	String prop_value = __displayProps.getValue ( "EnabledAppLayerTypes" );
	if ( prop_value != null ) {
		__enabledAppLayerTypes = StringUtil.breakStringList ( prop_value, ",", 0 );
		// These are saved and used each time a project is opened with openGVP().
		if ( __enabledAppLayerTypes != null ) {
			int size = __enabledAppLayerTypes.size();
			String message = null;
			if (size == 1) {
				message = "There is 1 layer type to be visible by default:";
			}
			else {
				message = "There are " + size + " layer types to be visible by default:";
			}
			Message.printStatus ( 1, "", message);
			size = __enabledAppLayerTypes.size();
			for ( int i = 0; i < size; i++ ) {
				Message.printStatus ( 1, "", (String)__enabledAppLayerTypes.get(i) );
			}
		}
	}
	addComponentListener(this);
}

/**
Show the information for selected features.
@param selected list of selected GeoRecord.
*/
private void showFeatureInformation ( List<? extends GeoRecord> selected )
{	GeoRecord record = null;
	GRShape shape = null;
	
	if ( selected == null ) {
		//Message.printStatus ( 1, routine, "No shapes selected" );
		return;
	}
	int size = selected.size();
	if ( size < 1 ) {
		return;
	}
	GeoLayer layer = null; // GeoLayer associated with shape
	GeoLayer layer_prev = null; // Previous value
	GeoLayerView layer_view = null; // GeoLayerView associated with shape

	DataTable table = null;
	int j = 0;
	List<String> strings = new Vector();
	int nfields = 0;
	String string = null;
	String name = null;		// Name of layer.
	Object o = null;
	strings.add ( "" );
	strings.add ( "Selected " + size + " features." );
	for ( int i = 0; i < size; i++ ) {
		record = selected.get(i);
		if ( record == null ) {
			break;
		}
		shape = record.getShape();
		if ( shape == null ) {
			break;
		}
		if ( (layer == null) || (layer_prev != layer) ) {
			// Now get the shape attributes and display in a report.
			// Need to somehow tell the GeoView which is the
			// selected layer so it will only process that layer in
			// the select.  For now, use the first layer.
			layer = record.getLayer();
			layer_view = record.getLayerView();
			// Now get the field information...
			table = layer.getAttributeTable();
			nfields = table.getNumberOfFields();
		}
		// Print the shape information with its attributes...
		if ( layer_view != null ) {
			name = layer_view.getLegend().getText();
		}
		if ( name == null ) {
			name = layer.getFileName();
		}
		strings.add ("");
		strings.add ( "" + (i+1) + ": " + name + ", layer feature [" + shape.index + "]" );
		strings.add ("");
		String field_formats[] = table.getFieldFormats();
		for ( j = 0; j < nfields; j++ ) {
			string = "[" + j + "] " + table.getFieldName(j) + ": ";
			try {
				o=layer.getShapeAttributeValue(shape.index, j);
				string += StringUtil.formatString(o,field_formats[j] );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, "", e );
				string += "";
			}
			strings.add ( string );
		}
		if ( shape.type == GRShape.POINT ) {
			strings.add ( "    X: " +
			StringUtil.formatString(((GRPoint)shape).x, "%13.6f"));
			strings.add ( "    Y: " +
			StringUtil.formatString(((GRPoint)shape).y, "%13.6f"));
		}
		else {
			strings.add ( "    XMIN: " + StringUtil.formatString(shape.xmin,"%13.6f"));
			strings.add ( "    YMIN: " + StringUtil.formatString(shape.ymin,"%13.6f"));
			strings.add ( "    XMAX: " + StringUtil.formatString(shape.xmax,"%13.6f"));
			strings.add ( "    YMAX: " + StringUtil.formatString(shape.ymax,"%13.6f"));
		}
		field_formats = null;
		layer_prev = layer;
	}
	// Do a custom GUI later.  For now, just use text...
	PropList reportProp = new PropList ("ReportJFrame.props");
	reportProp.set ( "HelpKey", "TSTool.ExportMenu" );
	reportProp.set ( "TotalWidth", "550" );
	reportProp.set ( "TotalHeight", "550" );
	reportProp.set ( "Title", "Information for Selected Features" );
	reportProp.set ( "DisplayFont", "Courier" );
	reportProp.set ( "DisplaySize", "11" );
	// reportProp.set ( "DisplayStyle", Font.PLAIN);
	reportProp.set ( "PrintFont", "Courier" );
	// reportProp.set ( "PrintFont", Font.PLAIN );
	reportProp.set ( "PrintSize", "7" );
	//reportProp.set ( "PageLength", "100" );
	// Make a very large number...
	reportProp.set ( "PageLength", "1000000" );
	// Now display (the user can save as a file, etc.).
	new ReportJFrame ( strings, reportProp );
}

/**
Show the layer view properties (and GeoView properties) in a tabbed panel.
@param layer_view GeoLayerView to show properties for.
*/
public void showLayerViewProperties ( GeoLayerView layer_view )
{	new GeoViewPropertiesJFrame ( __parentJFrame, layer_view, __mainGeoView, __gvp, this );
}

/**
Zooms out from the current position by the specified percent.
@param percent the percent to zoom out by.
*/
public void zoomOut(double percent) {
	GRLimits dataLimits = __mainGeoView.getDataLimits();

	double w = dataLimits.getWidth();
	double h = dataLimits.getHeight();

	double wdiff = w * .01 * percent;
	double hdiff = h * .01 * percent;

	dataLimits.setLeftX(dataLimits.getLeftX() - wdiff);
	dataLimits.setRightX(dataLimits.getRightX() + wdiff);
	dataLimits.setBottomY(dataLimits.getBottomY() - hdiff);
	dataLimits.setTopY(dataLimits.getTopY() + hdiff);

	__mainGeoView.setDataLimits(dataLimits);
	__refGeoView.setDataLimits(dataLimits);
	refresh();
}

}