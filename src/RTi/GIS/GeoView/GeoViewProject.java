// ----------------------------------------------------------------------------
// GeoViewProject - class to read, write, and manipulate GeoViewProject files.
// ----------------------------------------------------------------------------
// History:
//
// 2001-10-10	Steven A. Malers, RTi	Add support for SymbolStyle property,
//					to replace SymbolType.
// 2001-11-27	SAM, RTi		Add ProjectAtRead property.
// 2001-12-04	SAM, RTi		Update to use Swing.
// 2002-01-08	SAM, RTi		Change GeoViewCanvas to
//					GeoViewJComponent.
// 2002-04-04	SAM, RTi		Update to recognize InitialExtent and
//					MaximumExtent.
// 2002-07-23	SAM, RTi		Change GRSymbol "pointSymbol" methods
//					to "style".
// ----------------------------------------------------------------------------
// 2003-05-06	J. Thomas Sapienza, RTi	* IOUtil.getFileSeparator replaced with
//					  File.separator
//					* Made class up-to-date compared with
//					  non-Swing code
// 2004-10-19	JTS, RTi		The geoview display now is explicitly
//					refreshed after all layer views are 
//					added in order to make sure the legend
//					is drawn correctly on the map.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.io.File;
import java.lang.Runtime;
import java.util.List;

import RTi.GR.GRColor;
import RTi.GR.GRLegend;
import RTi.GR.GRSymbol;
import RTi.GR.GRText;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.StopWatch;

/**
The GeoViewProject class reads a GeoViewProject file and handles instantiation
of GeoLayerViews.  Methods are also available to add the data to a GeoViewPanel
display.  This class will evolve as resources allow to support all the proposed
GeoView properties for reading and writing.
*/
public class GeoViewProject
{

private PropList _proplist = null;

/**
Construct from a GeoView project file (.gvp).  The project file is read into a
PropList, which can be further processed with addToGeoView().
@param filename Name of gvp file to process.
@exception Exception if there is an error processing the file.
*/
public GeoViewProject ( String filename )
throws Exception
{	// Just read in as a simple PropList...
	_proplist = new PropList ( "GeoViewProject" );
	_proplist.setPersistentName ( filename );
	_proplist.readPersistent ();
}

/**
Look through the project and add any layers that are appropriate for the
GeoView.  This method is typically called when a user selects a GeoViewProject
file or the file is automatically selected during program startup.  The layers
are read into memory.  Later might need a way to share
layers already in memory between layer views but for now assume most layer views will not share data.
@param geoview GeoViewJComponent to add layers to.
@param ref_geoview Reference GeoViewJComponent to add layers to.
@param legend GeoViewLegendJPanel to add to.
*/
public void addToGeoView ( GeoViewJComponent geoview, GeoViewJComponent ref_geoview, GeoViewLegendJTree legend )
//				GeoViewLegendJPanel legend )
{	String routine = "GeoViewProject.addToGeoView";

	legend.emptyTree();
	
	// Display missing layers as empty layers with a red indicator.  This is helpful when
	// tracking down problems in the data.  If necessary can pass in as a parameter.
	boolean displayMissingLayers = true;

	// See if there is a global data directory defined...
	String globalDataHome = _proplist.getValue ( "GeoView.GeoDataHome" );
	if ( (globalDataHome == null) || globalDataHome.equals(".") || globalDataHome.equals("") ) {
		// No home is specified so get the directory from the proplist persistent name.
		// Note this will only work if the GeoViewProject was read from a file in the first place.
		// If not, then layers are probably being added through a GUI and the paths will be absolute
		// (until the issue of converting absolute paths to relative is attacked)...
		globalDataHome = ".";
		try {
			File f = new File ( _proplist.getPersistentName() );
			globalDataHome = f.getParent();
			if ( !IOUtil.fileExists(globalDataHome) ) {
				globalDataHome = ".";
			}
		}
		catch ( Exception e ) {
			// Ignore for now until we figure out how often it occurs.
		}
	}
	else {
		// Need to see if the global data home is a relative path.  If so then it needs to be appended
		// to the project file directory.
		File f2 = new File ( globalDataHome );
		if ( !globalDataHome.regionMatches(true,0,"http:",0,5) && !f2.isAbsolute() ) {
			// Relative path that needs to append on the home of the GVP file...
			File f = new File ( _proplist.getPersistentName() );
			// Reset the data home...
			globalDataHome = f.getParent() + File.separator + globalDataHome;
		}
	}
	Message.printStatus ( 2, routine, "Global data home is \"" + globalDataHome + "\"" );
	// Loop requesting GeoLayer information
	String geoLayerFile;
	PropList layerViewProps = null;
	GeoLayerView layerView = null;
	String propValue = null;
	// Global GeoView properties...
	propValue = _proplist.getValue ( "GeoView.Color" );
	if ( propValue != null ) {
		GRColor grc = GRColor.parseColor(propValue);
		geoview.setBackground ( grc );
		if ( ref_geoview != null ) {
			ref_geoview.setBackground ( grc );
		}
	}
	propValue = _proplist.getValue ( "GeoView.Projection" );
	if ( propValue != null ) {
		try {
			geoview.setProjection (GeoProjection.parseProjection(propValue));
			if ( ref_geoview != null ) {
				ref_geoview.setProjection (	GeoProjection.parseProjection(propValue));
			}
		}
		catch ( Exception e ) {
			// Unknown projection...
			geoview.setProjection ( new UnknownProjection() );
		}
	}
	int ivisible = 0;	// Count of visible layers (equal to i if no layers are skipped
	StopWatch timer = null;
	legend.setProjectNodeText(_proplist.getPersistentName());
	for ( int i = 1; ; i++ ) {
		// Get the layer source...
		geoLayerFile = _proplist.getValue ( "GeoLayerView " + i + ".GeoLayer" );
		if ( geoLayerFile == null ) {
			// This is used to break out of the read.  Once a break
			// in the layer view numbers occurs, assume the end of the list is encountered.
			break;
		}
		// Make sure the layer view is supposed to be included...
		propValue = _proplist.getValue ( "GeoLayerView " + i + ".SkipLayerView" );
		if ( (propValue != null) && propValue.equalsIgnoreCase("true") ) {
			Message.printStatus ( 2, routine, "Skipping GeoLayerView \"" + geoLayerFile +
			"\" because of SkipLayerView=true in project file" );
			continue;
		}
		// Prepend the global directory if necessary...
		if ( !IOUtil.fileReadable(geoLayerFile) &&
			IOUtil.fileReadable(globalDataHome + File.separator + geoLayerFile)) {
			geoLayerFile = globalDataHome + File.separator + geoLayerFile;
		}
		Message.printStatus ( 2, routine, "Path to layer file is \"" + geoLayerFile + "\"" );
		// Add the layer...
		timer = new StopWatch();
		timer.start();
		//_status_TextField.setText ( "Adding layer..." );
		// Set properties for the layer view...
		layerViewProps = new PropList ( "forGeoLayerView" );
		// Save the position so we can get to other properties later
		layerViewProps.set ( "Number", "" + i );
		layerViewProps.set ( "Label", "UsingGeoViewListener" );
		propValue = _proplist.getValue ( "GeoLayerView " + i + ".ReadAttributes" );
		if ( propValue != null ) {
			layerViewProps.set ( "ReadAttributes=" + propValue );
		}
		propValue = _proplist.getValue ( "GeoLayerView " + i + ".ReadAttributes" );
		if ( propValue != null ) {
			layerViewProps.set ( "Name=" + propValue );
		}
		try {
			// Increment the count (will therefore be 1 for the first layer)...
			++ivisible;
			// Read the layer and create a layer view.  The legend is initialized to default values
			// without checking the project and will be further initialized in setLayerViewProperties().
			try {
				layerView = new GeoLayerView ( geoLayerFile, layerViewProps, ivisible );
			}
			catch ( Exception e1 ) {
				Message.printWarning(3, routine, "Error reading layer (" + e1 + ")." );
				Message.printWarning(3, routine, e1 );
				if ( displayMissingLayers ) {
					// Create an empty layer view
					Message.printStatus(2, routine, "Creating an empty layer as a placeholder." );
					layerView = GeoViewUtil.newLayerView(geoLayerFile, layerViewProps, ivisible);
				}
				else {
					// Re-throw the original exception and handle below
					Message.printStatus ( 2, routine, "Layer file \"" + geoLayerFile + "\" could not be read." +
						"  Initializing blank layer as placeholder." );
					layerView = GeoViewUtil.newLayerView ( geoLayerFile, layerViewProps, ivisible );
					throw e1;
				}
			}
			// If we get to here, the layer was read so it is OK to leave "ivisible" as it was set above.
			// Set the view properties after reading the layer data (the index is used to look in the GVP file
			// so don't use "ivisible")...
			setLayerViewProperties ( layerView, i );
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, "Unexpected error adding layer for \"" + geoLayerFile +
				"\" (" + e + ")" );
			Message.printWarning ( 3, routine, e );
			if ( displayMissingLayers ) {
				// Create an empty layer view
				Message.printStatus ( 2, routine, "Layer file \"" + geoLayerFile + "\" could not be read." +
				"  Initializing blank layer as placeholder." );
				layerView = GeoViewUtil.newLayerView(geoLayerFile, layerViewProps, ivisible);
			}
			else {
				// The layer load was unsuccessful so decrement the count...
				--ivisible;
				// Go to next layer...
				continue;
			}
		}
	
		// Now add the layer view to the view...
		geoview.addLayerView ( layerView );
		// If a reference geoview, only add if layer has been tagged as a reference layer...
		propValue = _proplist.getValue ( "GeoLayerView " + i + ".ReferenceLayer" );
		if ( (ref_geoview != null) && (propValue != null) && propValue.equalsIgnoreCase("true") ) {
			ref_geoview.addLayerView ( layerView );
		}
		timer.stop();
		Message.printStatus ( 1, routine,"Reading \"" + geoLayerFile +
		"\" took " + StringUtil.formatString(timer.getSeconds(),"%.2f")+ " seconds." );
		Runtime runtime = Runtime.getRuntime();
		Message.printStatus ( 1, routine, "JVM Total memory = " + runtime.totalMemory() + " used memory = " +
		(runtime.totalMemory() - runtime.freeMemory()) + " free memory = " + runtime.freeMemory() );
		runtime = null;
		//_status_TextField.setText ( "Finished adding layer.  Ready.");
		// If we got to here the layer could be added so add to the legend...
		legend.addLayerView ( layerView, ivisible );
	}

	// refresh the geoview to ensure that the legend draws correctly
	// with all layers and with all proper limits
	geoview.redraw(true);
}

/**
Clean up for garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	_proplist = null;
	super.finalize();
}

/**
Determine the number of symbols for a GeoLayerView.  This is accomplished
by checking for a property "Symbol #.#.SymbolStyle".  If
"Symbol #.1.SymbolStyle" is null, then assume 1 symbol.
@param ilayer_view GeoLayerView index (starting at 1).
*/
public int getNumSymbolsForLayerView ( int ilayer_view )
{	String prop_value;
	int i = 0;
	for ( i = 1;; i++ ) {
		prop_value = _proplist.getValue ( "Symbol " + ilayer_view + "." + i + ".SymbolSize" );
		if ( prop_value == null ) {
			break;
		}
	}
	if ( i == 1 ) {
		return 1;
	}
	else {
		return (i - 1);
	}
}

/**
Return the property list associated with the GeoViewProject.
@return the property list associated with the GeoViewProject.
*/
public PropList getPropList ()
{	return _proplist;
}

/**
Set a layer view's properties by evaluating properties in the project file.
It is assumed that default legend information has been set at construction of the legend.
@param layer_view GeoLayerView to set properties for.
@param index GeoLayerView index in GVP file (starting with 1).
*/
private void setLayerViewProperties ( GeoLayerView layer_view, int index )
{	String routine = "GeoViewProject.setLayerViewProperties";
	// Get the layer shape type...
	int layer_type = layer_view.getLayer().getShapeType();
	// Get the layer...
	GeoLayer layer = layer_view.getLayer();
	// Get the legend for the layer view...
	GRLegend legend = layer_view.getLegend ();
	// Get the default symbol, used to initialize each symbol below...
	GRSymbol default_symbol = legend.getSymbol();
	// Determine how many symbols will be used...
	int nsymbols = getNumSymbolsForLayerView ( index );
	legend.setNumberOfSymbols ( nsymbols );	
	// Properties that apply to the entire layer...	
	// Get the projection for the layer...
	String prop_value = _proplist.getValue ( "GeoLayerView " + index + ".Projection" );
	if ( prop_value != null ) {
		try {
			layer.setProjection (GeoProjection.parseProjection(prop_value));
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine,
			"Unrecognized projection \"" + prop_value + "\" for GeoLayerView " + index );
			// Unknown projection...
			layer.setProjection ( new UnknownProjection() );
			Message.printWarning ( 2, routine, e );
		}
	}
	// Also set the application layer data if available...
	prop_value = _proplist.getValue ( "GeoLayerView " + index + ".AppLayerType" );
	if ( prop_value != null ) {
		if ( layer != null ) {
			layer.setAppLayerType ( prop_value );
			// Also set in the layer property (debating whether this
			// is a layer or layer view data item).
			layer_view.getPropList().set("AppLayerType="+prop_value);
		}
	}
	prop_value = _proplist.getValue ( "GeoLayerView " + index + ".AppJoinField" );
	if ( prop_value != null ) {
		if ( layer != null ) {
			layer_view.getPropList().set("AppJoinField="+prop_value);
		}
	}
	// Project the layer view's data if requested...
	prop_value = _proplist.getValue ( "GeoLayerView " + index + ".ProjectAtRead" );
	if ( prop_value == null ) {
		// Try the global property...
		prop_value = _proplist.getValue ( "GeoView.ProjectAtRead" );
	}
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("true") ) {
		try {
			prop_value = _proplist.getValue ( "GeoView.Projection");
			if ( prop_value != null ) {
				Message.printStatus ( 1, routine, "Projecting to common projection..." );
				layer.project( GeoProjection.parseProjection(prop_value) );
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, "Unable to parse projection \"" + prop_value + "\"" );
		}
	}
	// Set the name in the legend...
	prop_value = _proplist.getValue ( "GeoLayerView " + index + ".Name" );
	if ( prop_value != null ) {
		legend.setText ( prop_value );
	}
	// Now loop through the symbols for the layer...
	GRSymbol symbol = null;
	List tokens = null;
	for ( int isym = 0; isym < nsymbols; isym++ ) {
		// Get the symbol...
		symbol = new GRSymbol ( default_symbol.getType(),
			default_symbol.getStyle(),
			default_symbol.getColor(),
			default_symbol.getOutlineColor(),
			default_symbol.getSize() );
		// Transfer default symbol information...
		// Get the label information...
		// New style...
		prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".LabelField" );
		// Old style...
		if ( prop_value == null ) {
			prop_value = _proplist.getValue ( "GeoLayerView " + index + ".LabelField" );
		}
		if ( prop_value != null ) {
			symbol.setLabelField ( prop_value );
		}
		// New style...
		prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".LabelFormat" );
		// Old style...
		if ( prop_value == null ) {
			prop_value = _proplist.getValue ( "GeoLayerView " + index + ".LabelFormat" );
		}
		if ( prop_value != null ) {
			symbol.setLabelFormat ( prop_value );
		}
		// New style...
		prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".LabelPosition" );
		// Old style...
		if ( prop_value == null ) {
			prop_value = _proplist.getValue ( "GeoLayerView " + index + ".LabelPosition" );
		}
		if ( prop_value != null ) {
			try {
				symbol.setLabelPosition ( GRText.parseTextPosition(prop_value) );
			}
			catch ( Exception e ) {
				// Unknown position...
				symbol.setLabelPosition (GRText.RIGHT|GRText.CENTER_Y);
			}
		}
		// Symbol color is always a property.  Depending on the
		// classification, more than one color may be specified...
		// Determine the classification type for symbols...
		// New style...
		prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".SymbolClassification" );
		// Old style...
		if ( prop_value == null ) {
			prop_value = _proplist.getValue ( "GeoLayerView " + index + ".SymbolClassification" );
		}
		if ( prop_value != null ) {
			symbol.setClassificationType ( prop_value );
			//Message.printStatus ( 1, "",
			//"SAMX symbol classification " + index + " is " + symbol.getClassificationType() );
		}
		if ( symbol.getClassificationType() == GRSymbol.CLASSIFICATION_SINGLE ) {
			// Simple color for symbol (this is also the default)...
			// New style...
			prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".Color" );
			// Old style...
			if ( prop_value == null ) {
				prop_value = _proplist.getValue ( "GeoLayerView " + index + ".Color" );
			}
			if ( prop_value != null ) {
				symbol.setColor(GRColor.parseColor(prop_value));
			}
			// Size for symbol...
			// New style...
			prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".SymbolSize" );
			// Old style...
			if ( prop_value == null ) {
				prop_value = _proplist.getValue ( "GeoLayerView " + index+".SymbolSize" );
			}
			if ( prop_value != null ) {
				symbol.setSize(StringUtil.atod(prop_value));
			}
		}
		else if ( symbol.getClassificationType() == GRSymbol.CLASSIFICATION_SCALED_SYMBOL ) {
			// Color for symbol may have more than one value...
			// New style...
			prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".Color" );
			// Old style...
			if ( prop_value == null ) {
				prop_value = _proplist.getValue ( "GeoLayerView " + index + ".Color" );
			}
			if ( prop_value != null ) {
				// The color property can contain more than one color, separated by ";"...
				List v = StringUtil.breakStringList ( prop_value, ";", 0 );
				int vsize = v.size();
				if ( vsize == 1 ) {
					symbol.setColor(GRColor.parseColor(prop_value));
					symbol.setColor2(GRColor.parseColor(prop_value));
				}
				else if ( vsize == 2 ) {
					symbol.setColor(GRColor.parseColor((String)v.get(0) ));
					symbol.setColor2(GRColor.parseColor((String)v.get(1) ));
				}
			}
			// Size for symbol...
			// New style...
			prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".SymbolSize" );
			// Old style...
			if ( prop_value == null ) {
				prop_value = _proplist.getValue ("GeoLayerView " + index+".SymbolSize" );
			}
			if ( prop_value != null ) {
				// The size can be specified as a single value
				// or an x and y value, separated by a comma...
				List v = StringUtil.breakStringList(prop_value,",",0);
				int size = 0;
				if ( v != null ) {
					size = v.size();
				}
				if ( size == 1 ) {
					symbol.setSize( StringUtil.atod((String)v.get(0)));
				}
				else if ( size == 2 ) {
					symbol.setSizeX( StringUtil.atod((String)v.get(0)));
					symbol.setSizeY( StringUtil.atod((String)v.get(1)));
				}
			}
			// Symbol class field...
			// New style...
			prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) +".SymbolClassField" );
			// Old style...
			if ( prop_value == null ) {
				prop_value = _proplist.getValue ( "GeoLayerView " + index + ".SymbolClassField" );
			}
			if ( prop_value != null ) {
				symbol.setClassificationField ( prop_value );
			}
		}
		else if ( symbol.getClassificationType() == GRSymbol.CLASSIFICATION_CLASS_BREAKS ) {
			// Need to get the class breaks and colors...
			// The number of colors in the color table should match
			// the number of values in the class break.
			// New style...
			prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) +".SymbolClassField" );
			// Old style...
			if ( prop_value == null ) {
				prop_value = _proplist.getValue ( "GeoLayerView " + index + ".SymbolClassField" );
			}
			if ( prop_value != null ) {
				// Can only specify class breaks if we know which field will be examined from the data...
				symbol.setClassificationField ( prop_value );
				// Get the color table.  The number of colors for this governs the maximum number of breaks
				// (so they are consistent).  The number of colors and breaks should normally be the same.
				// New style...
				prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".ColorTable" );
				// Old style...
				if ( prop_value == null ) {
					prop_value = _proplist.getValue ( "GeoLayerView " + index + ".ColorTable" );
				}
				int num_classes = 0;
				if ( prop_value != null ) {
					List c = StringUtil.breakStringList ( prop_value, ";", StringUtil.DELIM_SKIP_BLANKS );
					num_classes = StringUtil.atoi( ((String)c.get(1)).trim());
					symbol.setColorTable ( ((String)c.get(0)).trim(),num_classes );
					c = null;
				}
				// Get the class breaks.  Examine the classification field to determine whether the
				// array will be double, int, or String...
				double d[] = new double[num_classes];
				// Values are initialized to 0.0 by default...
				// New style...
				prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".SymbolClassBreaks" );
				// Old style...
				if ( prop_value == null ) {
					prop_value = _proplist.getValue ( "GeoLayerView " + index + ".SymbolClassBreaks" );
				}
				if ( prop_value != null ) {
					List c = StringUtil.breakStringList ( prop_value,",", StringUtil.DELIM_SKIP_BLANKS );
					// For now always use double...
					for ( int i = 0; i < num_classes; i++ ){
						d[i] = StringUtil.atod(((String)c.get(i)).trim());
					}
					symbol.setClassificationData ( d, true);
					c = null;
				}
				d = null;
			}
		}
		else {
			// GRSymbol.CLASSIFICATION_UNIQUE
			// Need to do some work to search the data for unique values...
		}
		if ( (layer_type == GeoLayer.POINT) || (layer_type == GeoLayer.MULTIPOINT) ) {
			// Symbol type...
			// Old convention...
			prop_value = _proplist.getValue ( "GeoLayerView " + index + ".SymbolType" );
			if ( prop_value != null ) {
				Message.printWarning ( 2, routine, "The SymbolType GeoView project property is obsolete.  Use SymbolStyle." );
				try {	symbol.setStyle (
					GRSymbol.toInteger(prop_value) );
				}
				catch ( Exception e ) {
					symbol.setStyle ( GRSymbol.SYM_PLUS );
				}
			}
			// Newer convention (need to also support for other shape types)...
			// New style...
			prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".SymbolStyle" );
			// Old style...
			if ( prop_value == null ) {
				prop_value = _proplist.getValue ( "GeoLayerView " + index + ".SymbolStyle" );
			}
			if ( prop_value != null ) {
				try {
					symbol.setStyle ( GRSymbol.toInteger(prop_value) );
				}
				catch ( Exception e ) {
					symbol.setStyle ( GRSymbol.SYM_PLUS );
				}
			}
		}
		else if ( layer_type == GeoLayer.LINE ) {
		}
		else if ( (layer_type == GeoLayer.POLYGON) || (layer_type == GeoLayer.GRID) ) {
			// New style...
			prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".OutlineColor" );
			// Old style...
			if ( prop_value == null ) {
				prop_value = _proplist.getValue ( "GeoLayerView " + index + ".OutlineColor" );
			}
			if ( prop_value != null ) {
				symbol.setOutlineColor(GRColor.parseColor(prop_value));
			}
			// New style...
			prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".IgnoreDataOutside" );
			// Old style...
			if ( prop_value == null ) {
				prop_value = _proplist.getValue ( "GeoLayerView " + index + ".IgnoreDataOutside" );
			}
			if ( prop_value != null ) {
				layer_view.getPropList().set("IgnoreDataOutside="+prop_value);
			}
			// Get the symbol style.  If "Transparent", then the style should be specified:
			//
			// SymbolStyle = Transparent,127
			//
			// Newer convention (need to also support for other shape types)...
			// New style...
			prop_value = _proplist.getValue ( "Symbol " + index + "." + (isym + 1) + ".SymbolStyle" );
			// Old style...
			if ( prop_value == null ) {
				prop_value = _proplist.getValue ( "GeoLayerView " + index + ".SymbolStyle" );
			}
			// Default fill is FILL_SOLID but need to see if transparent...
			if ( (prop_value != null) && prop_value.regionMatches(true,0,"Transparent",0,11) ) {
				// Parse out...
				tokens = StringUtil.breakStringList (prop_value, ",", StringUtil.DELIM_SKIP_BLANKS );
				if ( tokens.size() > 1 ) {
					int transparency = StringUtil.atoi((String)tokens.get(1) );
					symbol.setTransparency ( transparency );
				}
			}
		}
		// Now resave the information...
		legend.setSymbol ( isym, symbol );
	}
	// Save the legend...
	layer_view.setLegend ( legend );
	// Clean up...
	symbol = null;
	layer = null;
	legend = null;
	prop_value = null;
	routine = null;
}

}