// ----------------------------------------------------------------------------
// GeoLayer - class to hold a geographic layer
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 27 Aug 1996	Steven A. Malers	Initial version.
//		Riverside Technology,
//		inc.
// 05 Mar 1997	Matthew J. Rutherford,	Added functions and members for use in
//		RTi			linked list, and getFile() routine.
// 18 Apr 1997	MJR, RTi		Added _entity_name, and _entity_type
//					data members also added the prototypes
//					for the functions used. Also added a
//					flag to draw attributes or not.
// 09 Jul 1997	Jay J. Fucetola		Converted to Java.
// 14 Jun 1999	SAM, RTi		Revisit code and clean up.
//					Change name from GeoCoverage to GeoLayer
//					to be more consistent with GIS
//					conventions.
// 28 Jun 1999	Catherine E.		Added attribute table.
//		Nutting-Lane, RTi
// 07 Jul 1999	SAM, RTi		Add _user_type for to allow a broad
//					classification of the layer to a user
//					type.  Add _props so that information
//					can be easily associated with the layer.
//					Add getShape and getRecord for index
//					lookups.
// 01 Sep 1999	SAM, RTi		Add reindex() to reset the index numbers
//					on shapes.
// 09 Nov 1999	CEN, RTi		Fixed computeLimits.  limits_found was
//					never set to true and once true, no 
//					comparison was made between temporary
//					limits and shape limits.
// 01 Aug 2001	SAM, RTi		Add removeAllAssociations() to set all
//					shape.associated_object values to null.
//					This is used when items in a layer have
//					been previously associated via a join
//					and now a new join is being done.
//					Change "user type" to "AppLayerType" to
//					be consistent with current
//					GeoViewProject conventions.
// 10 Aug 2001	SAM, RTi		Change so the class is not abstract.
//					This allows a general GeoLayer to be
//					created.
// 17 Sep 2001	SAM, RTi		Add the GRID layer type.  Change from
//					Table to DataTable.
// 25 Sep 2001	SAM, RTi		Add getShapeAttributeValue() to allow
//					lookup of an attribute.  This works
//					even if the attibutes were never read.
// 27 Sep 2001	SAM, RTi		Fill out the readLayer() method to
//					generically handle shapefile and Xmrg
//					files.  Add projection as a layer
//					property.
// 04 Oct 2001	SAM, RTi		Add deselectAllShapes().
// 					Add hasSelectedShapes() to help optimize
//					code when setting colors for display.
// 08 Oct 2001	SAM, RTi		Add getDataFormat().
// 2001-10-17	SAM, RTi		Add call to System.gc() in readLayer().
// 2001-11-26	SAM, RTi		Add ability to read NWSRFS geographic
//					data in NWSRFSLayer.  Add project()
//					method to be used to project after
//					data are read (called in
//					GeoViewProject).
// 2001-12-07	SAM, RTi		Add _selected_count to keep track of
//					how many shapes are selected.  This
//					is used by getNumSelected().
//					The _has_selected_shapes data and
//					hasSelectedShapes() have been
//					eliminated.  This has become necessary
//					because shape selections can now be
//					appended and reversed in the
//					GeoViewCanvas with the Ctrl-key.
//					Add constructor with just a PropList -
//					this allows a layer of shapes to be
//					dynamically created in memory.
// 2002-07-27	SAM, RTi		Add setAttributeTable().
// 2002-09-09	SAM, RTi		Add setShapesVisible().
// 2002-09-24	SAM, RTi		Add getAttributeMax() and
//					getAttributeMin().
// 2004-08-03	JTS, RTi		* Added setAttributeTableValue().
//					* Added getAttributeTableRowCount().
//					* Added getAttributeTableFieldCount().
// 2004-08-09	JTS, RTi		* Added getShapePrecisionValue().
//					* Added getShapeWidthValue().
// 2004-10-27	JTS, RTi		Implements Cloneable.
// 2005-04-27	JTS, RTi		Added all data members to finalize().
// 2006-06-15	SAM, RTi		Add setShapes().
// ----------------------------------------------------------------------------
// EndHeader

package RTi.GIS.GeoView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import RTi.GR.GRLimits;
import RTi.GR.GRShape;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Math.MathUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;

// TODO SAM 2010-12-23 Need to convert GeoLayer to an interface and have implementations of the interface

/**
This class holds a layer of geographically-referenced data, which can be point,
polygon, image, etc.  GeoLayers are associated with visible attributes using
a GeoLayerView.  The GeoLayer therefore contains mainly raw shape data as Vector
of GRShape and shape attributes as a DataTable.  The GeoView class displays the layer views.
This base class can be extended for the different layer formats (e.g., ESRI
shapefiles) and the input/output code for these formats should be in the
derived code.  The benefit of extending from GeoLayer is that layers can be
treated similarly by higher-level code.  An example is that a layer's attributes
can be retrieved using the getShapeAttributeValue() method.  This method may
take values from memory or may cause a binary file read (often the case due to
the large amount of data in GIS files).  The shape data are usually read into
memory for fast redraws and region queries.  In the case of grid layers,
attributes are stored in the GeoGrid object (rather than an attribute table)
and special care can be taken.  The overall class hierarchy is shown below:
<pre>
   GeoViewPanel (has GeoView, GeoLayerLegend, etc.) - high level GUI components

         GeoView (has Vector of GeoLayerView) - GR library Canvas device

               GeoLayerView (has a GeoLayer, GRLegend) - organize view of data

                       GRLegend (has a GRSymbol) - legend information

                       GeoLayer (Vector of GRShape, has DataTable) - raw data
                          |                            
                          |                            
                          |                              GRShape - shape data
                          |                                 |
                          |                               GRPoint--GRGrid--etc
                          |                                          |
                          |                                          |
                          |                                       GeoGrid 
                          |
                          |------------------GeoGridLayer (Shapes are GeoGrid)
                          |                       |
                          |                       |
                     ESRIShapefile          XmrgGridLayer
               (shapes are GRPoint,etc.)
</pre>
Common formats can also be added to the readLayer() method.
If constructing a GeoLayer in memory:
<ol>
<li>	create the GeoLayer</li>
<li>	call setShapeType() to set the proper shape type (1 shape type per GeoLayer)</li>
<li>	call getShapes() to get the shape Vector</li>
<li>	add the proper GRShape objects to the Vector</li>
<li>	use the GeoLayer as needed (call computeLimits() to recompute the
	limits of the data, if necessary - note for ESRIShapefiles it is
	possible for the overall layer to have valid limits but individual
	shapes to have invalid limits like zeros)</li>
</ol>
*/
public class GeoLayer
implements Cloneable 
{

// TODO SAM 2010-12-29 implement open standard types and map ESRI to them
/**
Define layer types.  Use ESRI shapefile types where there is overlap.
*/

/**
Unknown layer type (can occur in ESRI shapefile).
*/
public static final int UNKNOWN = 0;

/**
Point (site) layer, stored in GRPoint (can occur in ESRI shapefile).
*/
public static final int POINT = 1;

/**
Arc (polyline) layer, stored in GRPolylineList (can occur in ESRI shapefile).
*/
public static final int ARC = 3;
public static final int LINE = ARC;

/**
Polygon layer, stored in GRPolygonList (can occur in ESRI shapefile).
*/
public static final int POLYGON = 5;

/**
Multipoint layer, stored as GRPolypoint (can occur in ESRI shapefile).
*/
public static final int MULTIPOINT = 8;  // Or just use 1?

/**
Point with Z and M, as per shapefile type 11.
*/
public static final int POINT_ZM = 11;

/**
PolylineZM list.
*/
public static final int POLYLINE_ZM = 13;

/**
Big picture layer consisting of a layer with shape data and an additional
table with attributes for additional analysis.  This is a special layer for
displaying complex symbols at locations.  <b>This layer type will be phased out
at some point in favor of more generic symbology (do not port to C++).</b>
*/
public static final int BIG_PICTURE = 50;

/**
Grid data, stored as GRGrid (not ESRI grid).
*/
public static final int GRID = 51;

/**
Type of shapes in layer (e.g., POINT).
*/
private int __shapeType = UNKNOWN;

/**
List of shape data.  Note that the index in the shape is used to
cross-reference to the attribute table.  The index starts at 0, unlike the ESRI
shapefiles, where the record numbers start at 1.
*/
private List<GRShape> __shapes = new Vector<GRShape>();

/**
Overall limits of the layer (this can be reset using computeLimits() or may be
set in the I/O code for a specific layer file type).
*/
private GRLimits __limits;

/**
Name of file for layer (currently layers are not constructed from database
query - if so, the meaning of this data value may need to be modified).
*/
private String __fileName;

/**
Count of the number of selected shapes.  This should be updated whenever a
shapes _is_selected data member is modified.  The selected shapes may or may
not be visible.  This data member must be updated if the shape data is edited
(e.g., if shapes are selected are removed, update the selected count).
*/
private int __selectedCount;

/**
Application layer type for layer.  This is used to allow an application to
somewhat generically associate layers with functionality.  For example, an
AppLayerType property in the GeoViewProject file may be set to "Streamflow".  An
application can then know that when a user is interacting with streamflow data
that the Streamflow data layer should be highlighted in the view.
*/
private String __appLayerType;

/**
Data format (e.g., "ESRI Shapefile") - this is a descriptive label but is not compared for any logic.
*/
private String __dataFormat = "";

/**
Property list for layer properties.  This is not used much at this time (need
to document each recognized property).
*/
private PropList __props;

/**
Projection for the layer (see classes extended from GeoProjection).
*/
private GeoProjection __projection = null;

/**
Table to store attribute information (id, location, etc.).  This may be a
derived class like DbaseDataTable due to the special requirements of a layer file format.
*/
private DataTable __attributeTable;

/**
Construct a layer and initialize to defaults.
@param props Properties for the layer (currently none are recognized).
*/
public GeoLayer ( PropList props )
{	initialize ( null, props );
}

/**
Construct a layer and initialize to defaults (derived class should construct
from a file).  An empty PropList is created.
@param filename File that is being read.
*/
public GeoLayer ( String filename )
{	initialize ( filename, null );
}

/**
Construct a layer and initialize to defaults (derived class should construct from a file).
@param filename File that is being read.
@param props Properties for the layer (currently none are recognized).
*/
public GeoLayer ( String filename, PropList props )
{	initialize ( filename, props );
}

/**
Clones the object.
@return a clone of the object.
*/
public Object clone() {
	GeoLayer l = null;
	try {
		l = (GeoLayer)super.clone();
	}
	catch (Exception e) {
		return null;
	}

	if (__shapes != null) {
		int size = __shapes.size();
		GRShape shape = null;
		l.__shapes = new Vector<GRShape>(size);
		for (int i = 0; i < size; i++) {
			shape = __shapes.get(i);
			l.__shapes.add((GRShape)shape.clone());
		}
	}

	if (__limits != null) {
		l.__limits = (GRLimits)__limits.clone();
	}

	if (__props != null) {
		l.__props = new PropList(__props);
	}

	if (__projection != null) {
		l.__projection = (GeoProjection)__projection.clone();
	}

	if (__attributeTable != null) {
		l.__attributeTable = DataTable.duplicateDataTable(__attributeTable, true);
	}

	return l;
}

/**
Compute the spatial limits of the layer.  Use getLimits() to retrieve the limits.
@param include_invisible Indicate that invisible shapes should be considered in the limits computation.
@exception Exception if the limits cannot be computed (e.g., all null data, all missing, etc.).
*/
public void computeLimits ( boolean include_invisible )
throws Exception
{	// Loop through the shapes and get the overall limits...
	boolean limits_found = false;
	int size = __shapes.size();
	GRShape shape = null;
	double xmin = 0.0, xmax = 0.0, ymin = 0.0, ymax = 0.0;
	for ( int i = 0; i < size; i++ ) {
		shape = __shapes.get(i);
		if ( shape == null ) {
			continue;
		}
		if ( !shape.limits_found ) {
			// Don't try to compute the shapes limits.  Just skip...
			continue;
		}
		if ( !include_invisible && !shape.is_visible ) {
			// Don't want invisible shapes so skip...
			continue;
		}
		// Limits for the shape are found and shape is to be considered...
		if ( !limits_found ) {
			// Initialize...
			xmin = shape.xmin;
			ymin = shape.ymin;
			xmax = shape.xmax;
			ymax = shape.ymax;
			limits_found = true;
		}
		else {
			if ( shape.xmin < xmin ) {
				xmin = shape.xmin;
			}
			if ( shape.ymin < ymin ) {
				ymin = shape.ymin;
			}
			if ( shape.xmax > xmax ) {
				xmax = shape.xmax;
			}
			if ( shape.ymax > ymax ) {
				ymax = shape.ymax;
			}
		}
	}
	// Now return...
	if ( !limits_found ) {
		throw new Exception ( "Cannot find GeoLayer limits" );
	}
	__limits = new GRLimits ( xmin, ymin, xmax, ymax );
}

/**
Deselect all the shapes in a layer.  This is useful, for example, when all
shapes need to be deselected before a pending select operation.
*/
public void deselectAllShapes ()
{	GRShape shape = null;
	int size = __shapes.size();
	for ( int i = 0; i < size; i++ ) {
		shape = __shapes.get(i);
		if ( shape == null ) {
			continue;
		}
		if ( shape.is_selected ) {
			--__selectedCount;
		}
		shape.is_selected = false;
	}
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize ()
throws Throwable
{	__fileName = null;
	__shapes = null;
	__limits = null;
	__appLayerType = null;
	__dataFormat = null;
	__attributeTable = null;
	__props = null;
	__projection = null;
	super.finalize();
}

/**
Compute the maximum value for a numeric attribute.
@param field index of attribute table field to check (field must be a numeric field).
@param include_invisible If true all shapes will be considered.  If false, only
visible shapes will be considered.
@return the maximum value for a numeric attribute or zero if there is an error.
*/
public double getAttributeMax (	int field, boolean include_invisible )
{	int size = 0;
	if ( __shapes != null ) {
		size = __shapes.size();
	}
	if ( size == 0 ) {
		return 0.0;
	}
	GRShape shape = __shapes.get(0);
	double max = 0.0;
	try {
		max = StringUtil.atod (getShapeAttributeValue ( shape.index, field).toString() );
		for ( int i = 1; i < size; i++ ) {
			shape = __shapes.get(i);
			if ( shape == null ) {
				continue;
			}
			if ( !include_invisible && !shape.is_visible ) {
				// Don't want invisible shapes so skip...
				continue;
			}
			max = MathUtil.max ( max, StringUtil.atod ( getShapeAttributeValue ( shape.index, field).toString() ) );
		}
	}
	catch ( Exception e ) {
		return 0.0;
	}
	return max;
}

/**
Compute the minimum value for a numeric attribute.
@param field index of attribute table field to check (field must be a numeric field).
@param include_invisible If true all shapes will be considered.  If false, only
visible shapes will be considered.
@return the minimum value for a numeric attribute or zero if there is an error.
*/
public double getAttributeMin (	int field, boolean include_invisible ) 
{	int size = 0;
	if ( __shapes != null ) {
		size = __shapes.size();
	}
	if ( size == 0 ) {
		return 0.0;
	}
	GRShape shape = __shapes.get(0);
	double min = 0.0;
	try {
		min = StringUtil.atod (getShapeAttributeValue ( shape.index, field).toString() );
		for ( int i = 1; i < size; i++ ) {
			shape = __shapes.get(i);
			if ( shape == null ) {
				continue;
			}
			if ( !include_invisible && !shape.is_visible ) {
				// Don't want invisible shapes so skip...
				continue;
			}
			min = MathUtil.min ( min, StringUtil.atod (getShapeAttributeValue ( shape.index, field).toString() ) );
		}
	}
	catch ( Exception e ) {
		return 0.0;
	}
	return min;
}

/**
Return the application type set with setAppLayerType().
@return the application type for the layer.
*/
public String getAppLayerType ()
{	return __appLayerType;
}

/**
Return the attribute table associated with the shapes.  Depending on the parameters set during
the layer read/creation, this table may contain a header only or header and data records.
@return Layer attribute table.
*/
public DataTable getAttributeTable()
{	return __attributeTable;
}

/**
Returns the number of fields in the attribute table.  If there is no attribute table, 0 is returned.
@return the number of fields in the attribute table.
*/
public int getAttributeTableFieldCount() {
	if (__attributeTable == null) {
		return 0;
	}
	return __attributeTable.getNumberOfFields();
}

/**
Returns the number of rows in the attribute table.  If there is no attribute table, 0 is returned.
@return the number of rows in the attribute table.
*/
public int getAttributeTableRowCount() {
	if (__attributeTable == null) {
		return 0;
	}
	return __attributeTable.getNumberOfRecords();
}

/**
Return the layer data format.
@return the layer data format.
*/
public String getDataFormat()
{	return __dataFormat;
}

/**
Return the source file name for the layer.
@return the source file for the layer.
*/
public String getFileName()
{	return __fileName;
}

/**
Return the limits of the layer (in the original data units).
@return the limits of the layer.
*/
public GRLimits getLimits()
{	return __limits;
}

/**
Return the number of selected shapes.
@return the number of selected shapes.
*/
public int getNumSelected()
{	return __selectedCount;
}

/**
Return the projection for the data.  This projection can be compared to the
GeoView's projection to determine whether a conversion is necessary.
@return Projection used for the layer.
*/
public GeoProjection getProjection()
{	return __projection;
}

/**
Return a property for the layer.
@return the String value of a property for the layer.  This calls PropList.getValue().
*/
public String getPropValue ( String key )
{	return __props.getValue ( key );
}

/**
Return the shape at a specific index.
@return the shape for the layer, given the index (0-reference).
Return null if the index is out of bounds.
*/
public GRShape getShape ( int index )
{	if ( (index < 0) || (index > (__shapes.size() - 1)) ) {
		return null;
	}
	return __shapes.get((int)index);
}

/**
Return the data value for a shape.  This method should be overruled in derived
classes because the I/O for the attribute information is different for different
layer types and on-the-fly reads may be needed.  If a general GeoLayer is
used (e.g., for in-memory manipulation) then this code will be called and will
return the value if an attribute table is available.
@return a feature attribute value as an object.  The calling code should check
the attribute table's field data types to know how to cast the returned value.
@param index Database record for shape (zero-based).
@param field Attribute table field to use for data (zero-based index).
@exception Exception if an error occurs getting the value (e.g., error reading from the source file).
*/
public Object getShapeAttributeValue ( long index, int field )
throws Exception
{	if ( __attributeTable != null ) {
		return __attributeTable.getFieldValue( index, field );
	}
	else {
		return null;
	}
}

/**
Returns the precision for the given attribute table field.
@param index the database record
@param field the field for which to get the precision
@return the precision.
*/
public int getShapePrecisionValue ( long index, int field )
throws Exception {
	if ( __attributeTable != null ) {
		return __attributeTable.getFieldPrecision(field);
	}
	else {	
		return 0;
	}
}

/**
Returns the width for the given attribute table field.
@param index the database record
@param field the field for which to get the width  
@return the width.
*/
public int getShapeWidthValue ( long index, int field )
throws Exception {
	if ( __attributeTable != null ) {
		return __attributeTable.getFieldWidth(field);
	}
	else {	
		return 0;
	}
}

/**
Return the list of shapes used in the layer.  This list can be added to
externally when reading the shapes from a file.
@return the list of shapes used by this layer.
*/
public List<GRShape> getShapes ()
{	return __shapes;
}

/**
Return the shape type defined in this class (e.g., POINT).
@return the shape type.
*/
public int getShapeType ()
{	return __shapeType;
}

/**
Return a table record for a requested index.  This method should be defined in
derived classes, especially if on-the-fly data reads will occur.
@param index index (0-reference).
@return the table record for the layer, given the record index.
Return null if the index is out of bounds.
*/
public TableRecord getTableRecord ( int index )
{	if ( __attributeTable == null ) {
		return null;
	}
	if ( (index < 0) || (index > (__attributeTable.getNumberOfRecords()- 1)) ) {
		return null;
	}
	try {
		return __attributeTable.getRecord(index);
	}
	catch ( Exception e ) {
		// Not sure why this would happen...
		String routine = "GeoLayer.getTableRecord";
		Message.printWarning ( 10, routine, "Unable to get attribute table record [" + index + "]" );
		Message.printWarning ( 10, routine, e );
		return null;
	}
}

/**
Initialize data.
*/
private void initialize ( String filename, PropList props )
{	setFileName ( filename );
	__limits = null;
	// Always assign some shapes so we don't have to check for null all the time...
	__shapes = new Vector<GRShape>();
	__shapeType = UNKNOWN;
	__attributeTable = null;
	__appLayerType = "";
	if ( props == null ) {
		// Construct a PropList using the filename as the name...
		__props = new PropList ( filename );
	}
	else {
		// Use the properties that were passed in...
		__props = props;
	}
}

/**
Indicate whether the layer data source is available, for example that the filename exists and is the correct
format.  If the source does not exist, the layer is therefore empty and should typically be displayed, but may be
shown with a special indicator and have actions (like "Browse to connect to data").
*/
public boolean isSourceAvailable ()
{
	// Currently all layers are file based so check to see if the file exists
	// TODO SAM 2009-07-02 Need to make this more sophisticated to check for format, etc.
	File file = new File (getFileName());
	if ( file.exists() ) {
		return true;
	}
	return false;
}

/**
Project a layer, resulting in the raw data changing.  Note that if the data are
saved, the projection will be different and some configuration files may need
to be changed.  The projection is accomplished by calling
GeoProjection.projectShape() for each shape in the layer.  The overall limits are also changed.
@param projection to change to.
*/
public void project ( GeoProjection projection )
{	if ( !GeoProjection.needToProject(__projection, projection) ) {
		// No need to do anything...
		//Message.printStatus ( 1, "", "No need to project " +
		//_projection.getProjectionName() + " to " + projection.getProjectionName() );
		return;
	}
	// Loop through all the shapes and project them...
	int size = __shapes.size();
	for ( int i = 0; i < size; i++ ) {
		GeoProjection.projectShape ( __projection, projection, __shapes.get(i), true );
	}
	// Now reset the limits...
	try {
		computeLimits ( true );
	}
	catch ( Exception e ) {
		// Should not matter.
	}
	// Now set the projection to the requested...
	setProjection ( projection );
}

/**
Read a recognized layer type, returning a GeoLayer object (that can be cast to
the specific type if necessary).  This is a utility method to simplify reading
GIS data.  The file type is determined by calling each file's is*() method
(e.g., ESRIShapefile.isESRIShapefile()).
@param filename Name of layer file to read.
@param props Properties to use during reading.  Currently the only one
recognized is "ReadAttributes", which indicates whether shapefile attributes
should be read (if not, attributes will be read on the fly).  The properties
are passed directly to the layer type's read method (e.g., its constructor).
@return the GeoLayer read from the file, or null if an error.
@exception IOException if there is an error reading the layer.
*/
public static GeoLayer readLayer ( String filename, PropList props )
throws IOException
{	String routine = "GeoLayer.readLayer";
	if ( ESRIShapefile.isESRIShapefile(filename) ) {
		Message.printStatus(2, routine, "Reading ESRI shapefile \"" + filename + "\"..." );
		// Do this first because the filename for xmrg, etc. may match
		// the other criteria but still be a shapefile.
		PropList props2 = new PropList ( "ESRIShapefile" );
		props2.set ( "InputName", filename );
		String propValue = props.getValue("ReadAttributes");
		if ( propValue != null ) {
			props2.set ( "ReadAttributes", propValue );
		}
		GeoLayer layer = new ESRIShapefile ( props2 );
		return layer;
	}
	else if ( CsvPointLayer.isCsvPointFile(filename) ) {
		Message.printStatus(2, routine, "Reading CSV \"" + filename + "\"..." );
		// Read the entire layer with attributes...
		String propValue = props.getValue("Projection");
		GeoProjection projection = null;
		if ( propValue != null ) {
			try {
				projection = GeoProjection.parseProjection(propValue);
			}
			catch ( Exception e ) {
				throw new IOException ( "Error parsing projection \"" + propValue + "\" (" + e + ")." );
			}
		}
		return new CsvPointLayer ( filename, "X", "Y", projection );
	}
	else if ( XmrgGridLayer.isXmrg(filename) ) {
		Message.printStatus(2, routine, "Reading XMRG grid \"" + filename + "\"..." );
		// For now read the entire grid and then close the file...
		return new XmrgGridLayer ( filename, true, false );
		// Test reading on the fly (this worked)...
		//return new XmrgGridLayer ( filename, false, false );
	}
	else if ( NwsrfsLayer.isNwsrfsFile(filename) ) {
		Message.printStatus(2, routine, "Reading NWSRFS file \"" + filename + "\"..." );
		// Read the entire layer with attributes...
		return new NwsrfsLayer ( filename, true );
	}
	if ( IOUtil.fileReadable( filename) ) {
		throw new IOException ( "Unrecognized layer format for file \"" + filename + "\"" );
	}
	else {
		throw new IOException ( "File is not readable: \"" + filename + "\"" );
	}
}

/**
Refresh the layer.  This should normally be done periodically when editing
data layers. The following actions occur:
<ol>
<li>	The select count is reset to match the total of selected shapes in the shape list.</li>
<li>	The limits are recomputed.</li>
</ol>
This method may be updated in the future to help synchronize in-memory data with files (e.g., when editing).
*/
public void refresh ()
{	int size = __shapes.size();
	GRShape shape;
	__selectedCount = 0;
	for ( int i = 0; i < size; i++ ) {
		shape = __shapes.get(i);
		if ( shape.is_selected ) {
			++__selectedCount;
		}
	}
	shape = null;
	try {
		computeLimits ( true );
	}
	catch ( Exception e ) {
	}
}

/**
Re-index the data for the layer.  This is useful if the initial data has been
updated (shapes inserted or removed).  It is assumed that in such case, the
shape and table information have been modified consistently.  The re-indexing
operation loops through all shapes and resets the index in the shapes to be
sequential (they are not resorted, the indexes are reset).
*/
public void reindex ()
{	int size = __shapes.size();
	GRShape shape = null;
	for ( int i = 0; i < size; i++ ) {
		// Just set the index to the loop index...
		shape = __shapes.get(i);
		shape.index = i;
	}
}

/**
Set the shape.associated_object to null for all shapes in the layer.
The associated object is used to link a shape to in-memory data that are not
in the DataTable (e.g., a model data object).
*/
public void removeAllAssociations()
{	int size = 0;
	if ( __shapes != null ) {
		size = __shapes.size();
	}
	GRShape shape = null;
	for ( int i = 0; i < size; i++ ) {
		shape = __shapes.get(i);
		shape.associated_object = null;
	}
}

/**
Remove shapes that have a null associated object.  This is useful for filtering
geographic data to only that in an application's data.  The indexes of the
data are also reset.  This method should only be called when the DataTable
attribute table is in memory because records from the table are also removed to
ensure consistency with the shape vector.
@param hide_only If true, then the unassociated shapes will just be hidden.
This results in more shapes being in memory, but the shapes will be accessible
if needed (e.g., for searches to add a feature and turn visible).  This also
will allow attribute data to be read on the fly.
*/
public void removeUnassociatedShapes ( boolean hide_only )
{	int size = __shapes.size();
	GRShape shape = null;
	List<TableRecord> records = null;
	if ( __attributeTable != null ) {
		records = __attributeTable.getTableRecords();
	}
	for ( int i = 0; i < size; i++ ) {
		shape = __shapes.get(i);
		if ( shape.associated_object == null ) {
			if ( hide_only ) {
				// Just set to not visible...
				shape.is_visible = false;
			}
			else {
				// Actually remove the shape...
				__shapes.remove(i);
				try {
					if ( records != null ) {
						records.remove(i);
					}
				}
				catch ( Exception e ) {
					// Table not the same size??
					;
				}
				--size;
				--i;
			}
		}
	}
	if ( !hide_only ) {
		reindex();
	}
}

/**
Set the application layer type.  This information can then be used by an
application to turn on/off layers or skip layers during processing.  The type
can be set using the AppLayerType property in the GeoView project file.
An example is "Streamflow".
@param appLayerType Application layer type.
*/
public void setAppLayerType ( String appLayerType )
{	if ( appLayerType != null ) {
		__appLayerType = appLayerType;
	}
}

/**
Set the attribute table associated with the shapes.  This is most often called
when the attribute table is read first and then shapes are associated with the table.
@param attribute_table Attribute table for the layer.
*/
public void setAttributeTable ( DataTable attribute_table )
{	__attributeTable = attribute_table;
}

/**
Set the shapes associated with the layer.  This is most often called
when bulk manipulation of layers is occurring.
@param shapes Shape list for the layer.
*/
public void setShapes ( List<GRShape> shapes )
{	__shapes = shapes;
}

/**
Sets a value in the attribute table.
@param row the row to set the value (0-based).
@param column the column to set the value (0-based).
@param value the value to set.
*/
public void setAttributeTableValue(int row, int column, Object value) 
throws Exception {
	if (__attributeTable == null) {
		return;
	}
	__attributeTable.setFieldValue(row, column, value);
}

/**
Set the data format label.
*/
public void setDataFormat ( String dataFormat )
{
	__dataFormat = dataFormat;
}

/**
Set the file name for the layer.
@param fileName Name of layer input file.
*/
public void setFileName ( String fileName )
{	if ( fileName != null ) {
		__fileName = fileName;
	}
}

/**
Set the data limits for the layer.
@param limits data limits for layer
*/
public void setLimits ( GRLimits limits )
{	__limits = limits;
}

/**
Set the data limits for the layer.
@param x1 Left X value (usually the minimum X value).
@param y1 Bottom Y value (usually the minimum Y value).
@param x2 Right X value (usually the maximum X value).
@param y2 Top Y value (usually the maximum Y value).
*/
public void setLimits ( double x1, double y1, double x2, double y2 )
{	__limits = new GRLimits ( x1, y1, x2, y2 );
}

/**
Set the number of selected shapes.  This should be called by code that is
selecting and deselecting shapes in a layer.  This approach is use because it
is is a performance hit to loop through the shapes to determine the count.
If necessary, the refresh() method can be called.
@param selected_count The number of selected shapes.
*/
public void setNumSelected ( int selected_count )
{	__selectedCount = selected_count;
}

/**
Set the projection for the layer data.  This is typically done when reading the
layer from a project file because the project file indicates the projection
(not the data file itself).  For example, ESRI shapefiles do not contain a
projection and the projection must be specified in a project file.
@param projection Projection for the layer.
*/
public void setProjection ( GeoProjection projection )
{	__projection = projection;
}

/**
Set the String value of a property for the layer.  This calls PropList.setValue().
@param key Key (variable) for the property.
@param value Value for the property.
*/
public void setPropValue ( String key, String value )
{	__props.setValue ( key, value );
}

/**
Set all shapes visible or invisible.  This is useful, for example, when
(un)selected shapes need to be (in)visible.
@param is_visible If true, all shapes in the layer will be set to visible.  If
false, all shapes will be set to invisible.
@param do_selected If true, apply the change to selected shapes.
If false, do not change the visibility of selected shapes.
@param do_unselected If true, apply the change to unselected shapes.
If false, do not change the visibility of unselected shapes.
*/
public void setShapesVisible ( boolean is_visible, boolean do_selected, boolean do_unselected )
{	for ( GRShape shape: __shapes ) {
		if ( shape.is_selected && do_selected ) {
			shape.is_visible = is_visible;
		}
		else if ( !shape.is_selected && do_unselected ) {
			shape.is_visible = is_visible;
		}
	}
}

/**
Set the shape type (e.g., POINT).  The type is not currently checked for validity.
*/
public void setShapeType ( int shape_type )
{	__shapeType = shape_type;
}

/**
Save the layer as a shapefile.  If necessary this method should be defined in
derived classes so that specific data attributes, etc., can be handled.
If not defined in a derived class, it is expected that the shapes and attribute
table records can be saved to standard Shapefile formats.
All visible, selected shapes are written in the specified projection.
@param filename Name of file to write.
@param projection Projection to use for output data.
@exception IOException if there is an error writing the file.
*/
public void writeShapefile ( String filename, GeoProjection projection )
throws IOException
{	ESRIShapefile.write (  filename, __attributeTable, __shapes, true, true, __projection, projection );
}

/**
Save the layer as a shapefile.  If necessary this method should be defined in
derived classes so that specific data attributes, etc., can be handled.
If not defined in a derived class, it is expected that the shapes and attribute
table records can be saved to standard Shapefile formats.
@param filename Name of file to write.
@param visible_only If true, only visible shapes are written.  If false, all shapes are written.
@param selected_only If true, only selected shapes are written.  If false, all
shapes are written (contingent on the other flag).
@param projection Projection to use for output data.
@exception IOException if there is an error writing the file.
*/
public void writeShapefile ( String filename, boolean visible_only, boolean selected_only,
	GeoProjection projection )
throws IOException
{	ESRIShapefile.write (  filename, __attributeTable, __shapes, visible_only, selected_only, __projection, projection );
}

/**
Save the layer as a shapefile.  This method should be defined in derived classes
so that specific data attributes, etc., can be handled.  This method was
implemented to handle grid data output and may not be appropriate for all other
layer types.  If the design changes in the future, this method may be deprecated.
@param filename Name of file to write.
@param projection Projection to use for output data.
@param use_data_limits If true, then the following parameters are used.  This
is useful if a large grid is being processed down to a smaller size.
@param min_data_value Minimum data value to write (for a grid there is only one
data value per grid cell).
@param max_data_value Maximum data value to write (for a grid there is only one
data value per grid cell).
@exception IOException if there is an error writing the file.
*/
public void writeShapefile ( String filename, GeoProjection projection, boolean use_data_limits,
	double min_data_value, double max_data_value )
throws IOException
{	Message.printWarning ( 2, "GeoLayer.writeShapefile", "This method should be defined in the derived class." );
}

}