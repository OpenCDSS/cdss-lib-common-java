//-----------------------------------------------------------------------------
// ESRIShapefile
//-----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// Description: ShapeFile reads and provides info about .shp/.shx files.
//-----------------------------------------------------------------------------
// History:
//
// 30 Oct 1998	Steven A. Malers, RTi	Re-implement code based on C version
//					and insight from Jay Fucetola's code.
// 14 Jun 1999	SAM, RTi		Update with new EndianDataInputStream
//					class - I/O is now simpler!
// 25 Jun 1999	SAM, RTi		Start adding generic features to
//					support specific use on projects,
//					primarily through PropList options.
// 01 Sep 1999	SAM, RTi		Fix problem where the record number
//					from the ESRI shape was being used for
//					GRShape indexes.  This was problematic
//					because ESRI records start at 1 and
//					a zero-reference index is preferred for
//					programming.  Change to use zero.
//					If a shapefile writer is developed,
//					write the record number as 1+ the
//					GRShape index.
// 07 Sep 1999	CEN, RTi		Added readDBFHeaderOnly.
// 19 Feb 2001	SAM, RTi		Change IO to IOUtil.
// 15 Aug 2001	SAM, RTi		Change so a .shp file can be read
//					without a .shx or .dbf file.  This is
//					useful for testing.  Add a few more
//					debug messages to help figure out how to
//					write the shapefiles.
// 17 Sep 2001	SAM, RTi		Deprecate the ESRIShapefile package and
//					move the single class from that package
//					to this GeoView code.  Change the file
//					I/O to use RandomAccessFile for Dbase
//					and allow writing of shapefiles.  Random
//					access allows data attributes to be read
//					asynchronously.  Add isESRIShapefile()
//					to allow GeoView package to support
//					multiple file types.
// 02 Oct 2001	SAM, RTi		Change so reading attributes is false
//					by default.  The data will be read as
//					necessary from the attribute file.
// 2001-10-11	SAM, RTi		Review Javadoc for port to C++.  Remove
//					features that are no longer needed, like
//					the "Select...Where" properties.  Now
//					attributes can be read on-the-fly and
//					an application can set shapes to
//					invisibible if necessary.  Remove
//					readDBFHeadersOnly().
// 2002-10-30	SAM, RTi		Fix bug in write() where unknown shape
//					types were causing an error.  Update to
//					skip unknown shape types.
// 2003-01-17	SAM, RTi		Handle writing shapefiles when zero
//					shapes are given.  In this case write
//					a file with zero length.
// 2006-01-23	SAM, RTi		If for point data the x coordinate is
//					very negative, reset the shape type to
//					UNKNOWN.
// 2007-02-17	SAM, RTi		Incorporate performance enhancements from Ian.
//					Clean up code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

package RTi.GIS.GeoView;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import RTi.GIS.GeoView.GeoLayer;
import RTi.GR.GRPoint;
import RTi.GR.GRPointZM;
import RTi.GR.GRPolygon;
import RTi.GR.GRPolygonList;
import RTi.GR.GRPolyline;
import RTi.GR.GRPolylineList;
import RTi.GR.GRPolylineZM;
import RTi.GR.GRPolylineZMList;
import RTi.GR.GRPolypoint;
import RTi.GR.GRShape;
import RTi.Util.IO.EndianDataInputStream;
import RTi.Util.IO.EndianRandomAccessFile;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DbaseDataTable;

/**
Class to store ESRI shapefile data, including shape and attribute data.  Most
of the data and layer attributes are stored in the GeoLayer base class, which is
a generic layer class.  This shapefile class mainly handles input and output
from the three files that comprise a "shapefile" (commonly all three files are
collectively called a shapefile):
<ol>
<li>	.shp - Shape file, containing shape coordinates but no attributes.</li>
<li>	.dbf - Dbase (IV) database file containing shape attributes.</li>
<li>	.shx - Index file containing offsets to shape positions in .shp.</li>
</ol>
These files are binary files and are described in the ESRI shapefile
specifications.  In the past, the attributes have been read into memory up front
to simplify programs and speed execution.  However, some shapefiles are very
large and it is impractical to read all data into memory.  With the
implementation of the EndianRandomAccessFile, it is now possible to <b>not</b>
read attribute data when the file is opened and only read it when necessary.
This may slow performance if a full map is viewed; however, when zoomed in,
performance is usually very fast and little memory is used.  A shapefile can
be read using the ESRIShapefile constructor or by relying on the
GeoLayer.readLayer() method.
*/
public class ESRIShapefile extends GeoLayer
{

/**
Shape names corresponding to shape numbers from shapefiles.  The indexes
correspond to the shape numbers in the shapefile specification.  Some values are
empty strings.
*/
public final static String [] SHAPE_NAMES = {
	"Null Shape", // 0
	"Point", // 1
	"",
	"Arc", // 3
	"",
	"Polygon", // 5
	"",
	"",
	"MultiPoint", // 8,
	"",
	"",
	"PointZM", // 11
	"",
	"PolylineZM" }; // 13

/**
Name of .shp file, with extension.
*/
private String _shp_file = "";

/**
Input stream to read .shp file (note -only DBF read code has been converted to use EndianRandomAccessFile
*/
private EndianDataInputStream _shp_stream = null;

/**
Name of .shx file, with extension.
*/
private String _shx_file = "";

/**
Input stream to read .shx file.
*/
private EndianDataInputStream _shx_stream = null;

/**
Name of .dbf file, with file extension.
*/
private String _dbf_file = "";

/**
File code (should be 9994).
*/
private int _file_code = 0;

/**
File length in bytes.
*/
private int _file_length  = 0;

/**
File version.
*/
private int _version = 0;

/**
Array to hold content length values from the shx file.
*/
private int [] _shx_content_length = null;

/**
Array to hold content offset values from the shx file.
*/
private int [] _shx_offset = null;

/**
Property list for layer (can the base class be used instead)?
*/
private PropList _props = null;

/**
Read the attribute data?  If false only the header will be
read and attributes will be read on the fly.
*/
private boolean _read_attributes = false;

/**
Construct a shapefile by reading its files, given the path to the shapefile.
The file name should include the .shp file extension.
@param path File or URL path to a shapefile, without the extension.
@exception IOException if there is an error reading the file(s).
*/
public ESRIShapefile ( String path )
throws IOException
{	super ( path );
	_props = new PropList ( "ESRIShapefile.default" );
	_props.set ( "InputName", path );
	setDataFormat ( "ESRI Shapefile" );
	initialize ();
	try {
		read ();
	}
	catch ( IOException e ) {
		throw e;
	}
}

/**
Constructor to read a shapefile, given a PropList.  
@param props containing information about how to read the file.  The
following options are recognized:
<p>

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr

<tr>
<td><b>InputName</b></td>
<td>File or URL path to a shapefile, with or without the .shp extension.</td>
<td>None.  Must be specified.</td>
</tr>

<tr>
<td><b>ReadAttributes</b></td>
<td>Indicates whether attributes should be read.</td>
<td>false (true will result in slower performance but complete access to attribute data).</td>
</tr>
</table>
*/
public ESRIShapefile ( PropList props )
throws IOException
{	// Need to set the path in the base class in the initialize method...
	super ( "" );
	setDataFormat ( "ESRI Shapefile" );
	if ( props == null ) {
		// Error since we are expecting an input name...
		throw new IOException ( "Null PropList in constructor" );
	}
	_props = props;
	initialize ();
	try {
		read ();
	}
	catch ( IOException e ) {
		throw e;
	}
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	_dbf_file = null;
	_shp_file = null;
	_shp_stream = null;
	_shx_content_length = null;
	_shx_file = null;
	_shx_offset = null;
	_shx_stream = null;
	_props = null;
	super.finalize();
}

/**
Return the data value for a shape.  The object will be a Double, or String.
Use the DataTable methods to get field formats for output.
@param index Database record for shape.
@param field Attribute table field to use for data.
@exception Exception if there is an error getting the value.
*/
public Object getShapeAttributeValue ( long index, int field )
throws Exception
{	// Get the data from the attribute table in the base class...
	DataTable attributeTable = getAttributeTable();
	return attributeTable.getFieldValue( index, field );	
}

/**
Initialize the object.  For now, just set the file names.  It is assumed that
_props has been set to a non-null PropList that contains the input name.
*/
private void initialize ()
{	// Get the input name to use to create the file names...
	String path = _props.getValue ( "InputName" );
	if ( path != null ) {
		// Base class...
		setFileName ( path );
		// Only add the extensions if the path does not have...
		if ( !path.endsWith(".shp") && !path.endsWith(".SHP") ) {
			_shp_file = path + ".shp";
			_shx_file = path + ".shx";
			_dbf_file = path + ".dbf";
		}
		else {
			// Path already includes the full shapefile name...
			_shp_file = path;
			_shx_file = path.substring(0,path.length()-4) + ".shx";
			_dbf_file = path.substring(0,path.length()-4) + ".dbf";
		}
	}

	// To read ... or not to read attributes
	String prop_value = _props.getValue ( "ReadAttributes" );
	if ( prop_value != null ) {
		if ( prop_value.equalsIgnoreCase("true") ) {
			_read_attributes = true;
		}
		else {
			_read_attributes = false;
		}
	}
}

/**
Determine whether the file is a shapefile.  Checks are made as follows:
<ol>
<li>	First, the file must have the .shp extension.</li>
<li>	If the file cannot be opened, return false.</li>
<li>	If the file can be opened and the first big-endian byte value is 9994, return true.</li>
<li>	Else, return false.</li>
</ol>
@return true if the file is an ESRIShapefile, false if not.
@param filename Name of file to check, with or without the .shp extension.
*/
public static boolean isESRIShapefile ( String filename )
{	String f2 = filename;
	if ( !filename.toUpperCase().endsWith(".SHP") ) {
		return false;
	}
	// Further check the binary value...
	EndianDataInputStream s = null;
	try {
		s = new EndianDataInputStream( IOUtil.getInputStream(f2));
		int file_code = s.readInt();
		s.close();
		if ( file_code == 9994 ) {
			return true;
		}
		else {
			return false;
		}
	}
	catch ( Exception e ) {
		if ( s != null ) {
			try {
				s.close();
			}
			catch ( Exception e2 )
			{}
		}
		return false;
	}
}

/**
Read the three shapefile files, as appropriate (only the .dbf header will be
read if reading attributes is false, which is the default).
@exception IOException if an error occurs.
*/
private void read ()
throws IOException
{	// Read the DBF file (do it first, for now)...

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "ESRIShapefile.read", "Reading layer attributes." );
	}
	if ( !IOUtil.isApplet() ) {
		readDBF ( _dbf_file, _read_attributes );
	}

	// Read the SHP file...

	readSHP();

	// Read the SHX file...

	try {
		readSHX();
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, "ESRIShapefile.read",
		"Unable to read index file \"" + _shx_file + "\".  Only shapes will be available." );
	}
}

/**
Read the .DBF file contents.  The header information is always read.  The
actual data attributes are optional (reading takes longer but access is faster.
@param dbf_file Name of database file to read.
@param read_attributes Indicates whether the attributes should be read.
@exception IOException if there is an error reading the file.
*/
private void readDBF ( String dbf_file, boolean read_attributes )
throws IOException
{	String message, routine = "ESRIShapefile.readDBF";
	int dl = 5;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Reading DBF file..." );
	}

	try {
		// Open the Dbase data and leave open during run...
		setAttributeTable ( new DbaseDataTable ( dbf_file, read_attributes, true ) );
	}
	catch ( Exception e ) {
		message = "Unable to read dbf file \"" + _dbf_file + "\"";
		Message.printWarning ( 3, routine, message );
		Message.printWarning ( 3, routine, e );
		throw new IOException ( message );
	}
}

/**
Read the shapes from the .shp file.
@exception if an error occurs.
*/
private void readSHP ()
throws IOException
{	String message = null;
	String routine = "ESRIShapefile.read";
	int dl = 50;
	byte [] buffer = null;
	byte [] buffer20 = new byte[20];

	// Open the .shp file...

	try {
		_shp_stream = new EndianDataInputStream(new BufferedInputStream(IOUtil.getInputStream(_shp_file)));
	}
//        try {	_shp_stream = new EndianDataInputStream(IOUtil.getInputStream(_shp_file));
//	}
	catch ( Exception e ) {
		message = "Unable to open shape file \"" + _shp_file + "\"";
		Message.printWarning ( 3, routine, message );
		throw new IOException ( message );
	}
	if ( _shp_stream == null ) {
		message = "Null file pointer for \"" + _shp_file + "\"";
		Message.printWarning ( 3, routine, message );
		throw new IOException ( message );
	}

	// Read the shapefile header, stored in the first 100 characters of the .shp file...

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Reading shp file..." );
	}

	// File code is big endian...

	_file_code = _shp_stream.readInt();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "File code is " + _file_code );
	}

	// Read the intervening unused bytes...

	_shp_stream.read ( buffer20 );

	// File length is big endian...

	_file_length = _shp_stream.readInt();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "File length is " + _file_length );
	}

	// Version is little endian...

	_version = _shp_stream.readLittleEndianInt ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "File version is " + _version );
	}

	// Shape type is little endian...

	int shapeType = _shp_stream.readLittleEndianInt ();
	setShapeType ( shapeType );
	if ( (shapeType != ARC) && (shapeType != MULTIPOINT) &&
		(shapeType != POINT ) && (shapeType != POINT_ZM) && (shapeType != POLYGON) &&
		(shapeType != POLYLINE_ZM)) {
		message = "Unknown shape type " + shapeType;
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
			"Shape type for file is " + shapeType + " (" + SHAPE_NAMES[shapeType] + ")." );
	}
	//Message.printStatus ( 2, routine,
	//	"Shape type for file is " + shapeType + " (" + SHAPE_NAMES[shapeType] + ")." );

	// Limits are little endian...

	double xmin = _shp_stream.readLittleEndianDouble ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Xmin is " + xmin );
	}
	double ymin = _shp_stream.readLittleEndianDouble ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Ymin is " + ymin );
	}
	double xmax = _shp_stream.readLittleEndianDouble ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Xmax is " + xmax );
	}
	double ymax = _shp_stream.readLittleEndianDouble ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Ymax is " + ymax );
	}
	// Extra information for certain shape types...
	// Initialize to zero and read only if shape type uses (otherwise old files may
	// have garbage if read)
	double zmin = 0.0, zmax = 0.0, mmin = 0.0, mmax = 0.0;
	int bytesExtraRead = 0;
	if ( (shapeType == POINT_ZM) || (shapeType == POLYLINE_ZM) ) {
		zmin = _shp_stream.readLittleEndianDouble ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Zmin is " + zmin );
		}
		zmax = _shp_stream.readLittleEndianDouble ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Zmax is " + zmax );
		}
		bytesExtraRead += 16;
	}
	if ( (shapeType == POINT_ZM) || (shapeType == POLYLINE_ZM) ) {
		mmin = _shp_stream.readLittleEndianDouble ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Mmin is " + mmin );
		}
		mmax = _shp_stream.readLittleEndianDouble ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Mmax is " + mmax );
		}
		bytesExtraRead += 16;
	}
	// TODO SAM 2010-12-29 Evaluate support for Z, M
	// Set in base class (for now only do X and Y)...
	setLimits ( xmin, ymin, xmax, ymax );

	// Read the remaining 32 bytes in the header...
	
	buffer = new byte[32 - bytesExtraRead];
	_shp_stream.read(buffer);

	// Read the data...

	int content_length = 0, i = 0, iend = 0, j = 0,
		npolygons = 0, npolylines = 0, npts = 0,
		num_points = 0, pos_array[] = null,
		recordNumber = 0, esriShapeType, total_npts = 0;
	double x = 0.0, y = 0.0, z = 0.0, m = 0.0;
	GRPoint point = null;
	GRPointZM pointzm = null;
	GRPolypoint polypoint = null;
	GRPolygon polygon = null;
	GRPolygonList polygonlist = null;
	GRPolyline polyline = null;
	GRPolylineZM polylinezm = null;
	GRPolylineList polylinelist = null;
	GRPolylineZMList polylinezmlist = null;
	List<GRShape> shapes = getShapes(); // From base class
	while ( true ) {
		try {
			// Record number is big-endian..
			recordNumber = _shp_stream.readInt();
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "ESRI record number " + recordNumber );
			}
			//Message.printStatus( 2, routine, "ESRI record number " + recordNumber );
			// Content length is big-endian..
			content_length = _shp_stream.readInt();
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Content length " + content_length );
			}
			//Message.printStatus ( 2, routine, "Content length " + content_length );
			// Shape type is little endian...
			esriShapeType = _shp_stream.readLittleEndianInt();
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Shape type is " + esriShapeType );
			}
			//Message.printStatus ( 2, routine, "Shape type is " + esriShapeType );
			// Documentation says that shape types can't be
			// mixed but we don't really care at this point so
			// don't check against the file shape type.
			if ( esriShapeType == UNKNOWN ) {
				// No geometry data...  Instantiate a shape of unknown type...
				shapes.add ( new GRShape(recordNumber - 1 ) );
				//Message.printStatus(2, routine, "Adding null shape " + shapes.size() );
				continue;
			}
			else if ( esriShapeType == ARC ) {
				// Read the box.
				xmin = _shp_stream.readLittleEndianDouble();
				ymin = _shp_stream.readLittleEndianDouble();
				xmax = _shp_stream.readLittleEndianDouble();
				ymax = _shp_stream.readLittleEndianDouble();
				// Read the number of polylines...
				npolylines = _shp_stream.readLittleEndianInt();
				// Allocate memory and set the limits...
				polylinelist = new GRPolylineList (	npolylines );
				polylinelist.index = recordNumber - 1;
				polylinelist.xmin = xmin;
				polylinelist.ymin = ymin;
				polylinelist.xmax = xmax;
				polylinelist.ymax = ymax;
				polylinelist.limits_found = true;
				// Read the total number of points...
				total_npts = _shp_stream.readLittleEndianInt();
				polylinelist.total_npts = total_npts;
				// Read the position index.  Reuse this array to optimize performance...
				if ( pos_array == null ) {
					// Create it...
					pos_array = new int[npolylines];
				}
				else if ( pos_array.length < npolylines ) {
					// Resize bigger...
					pos_array = new int[npolylines];
				}
				for ( i = 0; i < npolylines; i++ ) {
					pos_array[i] =
					_shp_stream.readLittleEndianInt();
				}
				// Loop through the polylines...
				iend = npolylines - 1;
				for ( i = 0; i < npolylines; i++ ) {
					// Figure out how many points in the polyline...
					if ( i == iend ) {
						// Last polyline
						npts = total_npts - pos_array[i];
					}
					else {
						// Not last...
						npts = pos_array[i + 1] - pos_array[i];
					}
					// Allocate the polyline and fill...
					polyline = new GRPolyline ( npts );
					polyline.index = recordNumber - 1;
					for ( j = 0; j < npts; j++ ) {
						x = _shp_stream.readLittleEndianDouble();
						y = _shp_stream.readLittleEndianDouble();
						polyline.setPoint ( j, new GRPoint(x,y) );
					}
					polylinelist.setPolyline ( i, polyline);
				}
				shapes.add ( polylinelist);
			}
			else if ( esriShapeType == MULTIPOINT ) {
				// Read the box.
				xmin = _shp_stream.readLittleEndianDouble();
				ymin = _shp_stream.readLittleEndianDouble();
				xmax = _shp_stream.readLittleEndianDouble();
				ymax = _shp_stream.readLittleEndianDouble();
				// Read the number of points...
				num_points = _shp_stream.readLittleEndianInt();
				// Save a GRPolypoint, using the record number as the attribute table index...
				polypoint = new GRPolypoint ( (recordNumber - 1), num_points );
				shapes.add ( polypoint);
				for ( i = 0; i < num_points; i++ ) {
					x =_shp_stream.readLittleEndianDouble();
					y =_shp_stream.readLittleEndianDouble();
					polypoint.setPoint ( i, x, y );
				}
			}
			else if ( (esriShapeType == POINT) || (esriShapeType == POINT_ZM) ) {
				x = _shp_stream.readLittleEndianDouble ();
				y = _shp_stream.readLittleEndianDouble ();
				if ( esriShapeType == POINT_ZM ) {
					z = _shp_stream.readLittleEndianDouble ();
					m = _shp_stream.readLittleEndianDouble ();
				}
				// Save a point, using the record number as the attribute table index...
				if ( esriShapeType == POINT ) {
					point = new GRPoint((recordNumber - 1),x,y);
				}
				else {
					point = new GRPointZM((recordNumber - 1),x,y,z,m);
				}
				point.xmin = x;
				point.ymin = y;
				point.xmax = x;
				point.ymax = y;
				point.limits_found = true;
				shapes.add ( point );
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Point x,y =" + x + "," + y);
				}
				// TODO SAM 2006-01-23 Some CDSS point data are having very large
				// negative values assigned, which results in
				// erroneous displays.  Change to UNKNOWN shape type here in these cases.
				if ( x < -1.0e50 ) {
					// Will reset in base class...
					point.type = UNKNOWN;
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,routine, "Resetting shape type to " + UNKNOWN );
					}
				}
			}
			else if ( esriShapeType == POLYGON ) {
				// Read the box.
				xmin = _shp_stream.readLittleEndianDouble();
				ymin = _shp_stream.readLittleEndianDouble();
				xmax = _shp_stream.readLittleEndianDouble();
				ymax = _shp_stream.readLittleEndianDouble();
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Bounding box is " + xmin + "," + ymin + " " + xmax + "," + ymax );
				}
				// Read the number of polygons...
				npolygons = _shp_stream.readLittleEndianInt();
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Number of polygons is " + npolygons );
				}
				// Allocate memory and set the limits...
				polygonlist = new GRPolygonList ( npolygons );
				polygonlist.index = recordNumber - 1;
				polygonlist.xmin = xmin;
				polygonlist.ymin = ymin;
				polygonlist.xmax = xmax;
				polygonlist.ymax = ymax;
				polygonlist.limits_found = true;
				// Read the total number of points...
				total_npts = _shp_stream.readLittleEndianInt();
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Total number of points is " + total_npts );
				}
				polygonlist.total_npts = total_npts;
				// Read the position index.  Reuse this array to optimize performance...
				if ( pos_array == null ) {
					// Create it...
					pos_array = new int[npolygons];
				}
				else if ( pos_array.length < npolygons ) {
					// Resize bigger...
					pos_array = new int[npolygons];
				}
				// Now read the positions...
				for ( i = 0; i < npolygons; i++ ) {
					pos_array[i] = _shp_stream.readLittleEndianInt();
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,routine, "Position[" + i + "] is " + pos_array[i] );
					}
				}
				// Loop through the polygons...
				iend = npolygons - 1;
				for ( i = 0; i < npolygons; i++ ) {
					// Figure out how many points in the polygon...
					if ( i == iend ) {
						// Last polygon...
						npts = total_npts - pos_array[i];
					}
					else {
						// Not last...
						npts = pos_array[i + 1] - pos_array[i];
					}
					// Allocate the polygon and fill...
					polygon = new GRPolygon ( npts );
					for ( j = 0; j < npts; j++ ) {
						x = _shp_stream.readLittleEndianDouble();
						y = _shp_stream.readLittleEndianDouble();
						polygon.setPoint ( j, new GRPoint(x,y) );
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, routine, "x,y = " + x + "," + y);
						}
					}
					polygonlist.setPolygon ( i, polygon);
				}
				shapes.add ( polygonlist);
			}
			else if ( esriShapeType == POLYLINE_ZM ) {
				// Read the bounding box.
				xmin = _shp_stream.readLittleEndianDouble();
				ymin = _shp_stream.readLittleEndianDouble();
				xmax = _shp_stream.readLittleEndianDouble();
				ymax = _shp_stream.readLittleEndianDouble();
				// Read the number of polylines...
				npolylines = _shp_stream.readLittleEndianInt();
				// Allocate memory and set the limits...
				polylinezmlist = new GRPolylineZMList (	npolylines );
				polylinezmlist.index = recordNumber - 1;
				polylinezmlist.xmin = xmin;
				polylinezmlist.ymin = ymin;
				polylinezmlist.xmax = xmax;
				polylinezmlist.ymax = ymax;
				polylinezmlist.limits_found = true;
				// Read the total number of points...
				total_npts = _shp_stream.readLittleEndianInt();
				polylinezmlist.total_npts = total_npts;
				// Read the position index.  Reuse this array to optimize performance...
				if ( pos_array == null ) {
					// Create it...
					pos_array = new int[npolylines];
				}
				else if ( pos_array.length < npolylines ) {
					// Resize bigger...
					pos_array = new int[npolylines];
				}
				for ( i = 0; i < npolylines; i++ ) {
					pos_array[i] = _shp_stream.readLittleEndianInt();
				}
				// Loop through the polylines...
				iend = npolylines - 1;
				for ( i = 0; i < npolylines; i++ ) {
					// Figure out how many points in the polyline...
					if ( i == iend ) {
						// Last polyline
						npts = total_npts - pos_array[i];
					}
					else {
						// Not last...
						npts = pos_array[i + 1] - pos_array[i];
					}
					// Allocate the polyline and fill...
					polylinezm = new GRPolylineZM ( npts );
					polylinezm.index = recordNumber - 1;
					for ( j = 0; j < npts; j++ ) {
						x = _shp_stream.readLittleEndianDouble();
						y = _shp_stream.readLittleEndianDouble();
						polylinezm.setPoint ( j, new GRPointZM(x,y,z,m) );
					}
					// Set the polyline int the list (Z and M are modified below)
					polylinezmlist.setPolyline ( i, polylinezm);
				}
				// Read the Z limits.
				zmin = _shp_stream.readLittleEndianDouble();
				zmax = _shp_stream.readLittleEndianDouble();
				// Read the z coordinates for all points...
				for ( i = 0; i < npolylines; i++ ) {
					polylinezm = polylinezmlist.getPolyline(i);
					npts = polylinezm.npts;
					for ( j = 0; j < npts; j++ ) {
						z = _shp_stream.readLittleEndianDouble();
						pointzm = polylinezm.getPoint(j);
						pointzm.z = z;
					}
				}
				// Read the measure limits.
				mmin = _shp_stream.readLittleEndianDouble();
				mmax = _shp_stream.readLittleEndianDouble();
				// Read the measure value for all points...
				for ( i = 0; i < npolylines; i++ ) {
					polylinezm = polylinezmlist.getPolyline(i);
					npts = polylinezm.npts;
					for ( j = 0; j < npts; j++ ) {
						m = _shp_stream.readLittleEndianDouble();
						pointzm = polylinezm.getPoint(j);
						pointzm.m = m;
					}
				}
				// Add the shape...
				shapes.add ( polylinezmlist);
				//Message.printStatus ( 2, routine, "Shape " + shapes.size() + " limits are " + xmin +","+ymin + " " +
				//	xmax + "," + ymax );
			}
		}
		catch ( EOFException e ) {
			// Done reading shapes
			break;
		}
		catch ( Exception e ) {
			// Unexpected exception
			Message.printWarning ( 3, routine, e );
			break;
		}
	}

	Message.printStatus ( 2, routine, "Read " + shapes.size() + " shapes from \"" + _shp_file + "\"." );
}

/**
Read the .SHX file contents.
@exception IOException if an error occurs.
*/
private void readSHX ()
throws IOException
{	String message, routine = "ESRIShapefile.readSHX";
	int dl = 75;
	byte buffer100[] = new byte[100];

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Reading SHX file..." );
	}

	try {
		_shx_stream = new EndianDataInputStream(new BufferedInputStream(IOUtil.getInputStream(_shx_file)));
	}
//	try {	_shx_stream = new EndianDataInputStream(IOUtil.getInputStream(_shx_file));
//	}
	catch ( Exception e ) {
		message = "Unable to open index file \"" + _shx_file + "\"";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
	if ( _shx_stream == null ) {
		message = "Null file pointer for \"" + _shx_file + "\"";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}

	// Read the SHX file Header info.  Since this is exactly the same
	// organization as the shapefile header, just read 100 bytes for now
	// (later read the header and make sure it is consistent with the
	// other header and get the different fiile length for the index file. 

	// Get the first 100 bytes of the .shx

	_shx_stream.read ( buffer100 );

	// Now read the number of shapes.  First allocate an array of
	// integers corresponding to the records that will hold the content length for the record.

	List<GRShape> shapes = getShapes();
	int num_shapes = shapes.size();
	_shx_content_length = new int[num_shapes];
	_shx_offset = new int[num_shapes];

	// Now loop and set the values...

	int shx_content_length = 0;
	int i_shx_content_length = 0;
	int shx_offset = 0;
	int record_number = 1;
	try {
		while ( i_shx_content_length < num_shapes ) {
			shx_offset = _shx_stream.readInt();
			shx_content_length = _shx_stream.readInt();
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "SHX:  record number: " + record_number +
					" offset: " + shx_offset + " content length: " + shx_content_length );
			}
			//Message.printStatus( 2, routine, "SHX:  record number: " + record_number +
			//	" offset: " + shx_offset + " content length: " + shx_content_length );
			_shx_offset[i_shx_content_length] = shx_offset;
			_shx_content_length[i_shx_content_length++] = shx_content_length;
			++record_number;
		}
	}
	catch ( IOException e ) {
		Message.printWarning ( 3, routine, "Error reading SHX record [" + i_shx_content_length + "]");
		Message.printWarning ( 3, routine, e );
	}
}

/**
Write a shapefile (.shp) and associated index file (.shx) and attribute (.dbf)
files.  This method does not currently take a GeoLayer as a parameter because
in the case of a grid layer type, the shapes are converted from GeoGrid cells to
GRPolygon and a subset of the grid may be written.
@param filename Name of shapefile to write (with or without .shp extension).
@param table DataTable to write.  There must be one record per shape.
@param shapes Vector of GRShape to write.  Only one type of shape can be
included.  The first shape is used to indicate the type of shapefile.
@param from_projection The projection used for the shapes, as is.
@param to_projection The projection to use for the output.
@exception IOException if there is an error writing the file.
*/
public static void write ( String filename, DataTable table, List<GRShape> shapes,
	GeoProjection from_projection, GeoProjection to_projection )
throws IOException
{	write ( filename, table, shapes, true, false, from_projection, to_projection );
}

/**
Write a shapefile (.shp) and associated index file (.shx) and attribute (.dbf)
files.  This method does not currently take a GeoLayer as a parameter because
in the case of a grid layer type, the shapes are converted from GeoGrid cells to
GRPolygon and a subset of the grid may be written.
@param filename Name of shapefile to write (with or without .shp extension).
@param table DataTable to write.  There must be one record per shape.
@param shapes Vector of GRShape to write.  Only one type of shape can be
included.  The first shape is used to indicate the type of shapefile.
@param visible_only Write any shapes that are marked as visible.
@param selected_only Write any shapes that are marked as selected.
@param from_projection The projection used for the shapes, as is.
@param to_projection The projection to use for the output.
@exception IOException if there is an error writing the file.
*/
public static void write ( String filename, DataTable table, List<GRShape> shapes, boolean visible_only,
	boolean selected_only, GeoProjection from_projection, GeoProjection to_projection )
throws IOException
{	// Get the file names for all 3 files...
	String shp_file = null;
	String shx_file = null;
	String dbf_file = null;
	if ( (filename.length() > 4) && (filename.regionMatches(
		true,(filename.length()-4),".shp",0,4))){
		shp_file = filename;
		shx_file = filename.substring(0,(filename.length() - 4))+".shx";
		dbf_file = filename.substring(0,(filename.length() - 4))+".dbf";
	}
	else {
		shp_file = filename + ".shp";
		shx_file = filename + ".shx";
		dbf_file = filename + ".dbf";
	}
	// Make sure files don't already exist.  If a file does exist, delete it
	// first.  Otherwise, the binary writes will just update the binary
	// files and large old binary files may waste disk space.
	if (IOUtil.fileExists(shp_file)) {
		File fileToDelete = new File(shp_file);
		fileToDelete.delete();
	}
	if (IOUtil.fileExists(shx_file)) {
		File fileToDelete = new File(shx_file);
		fileToDelete.delete();
	}
	if (IOUtil.fileExists(dbf_file)) {
		File fileToDelete = new File(dbf_file);
		fileToDelete.delete();
	}

	writeSHPAndSHX ( shp_file, shx_file, shapes, visible_only, selected_only, from_projection, to_projection );

	boolean[] write_record = null;
	int size = 0;
	if ( shapes != null ) {
		size = shapes.size();
	}
	GRShape shape = null;
	if ( size > 0 ) {
		write_record = new boolean[size];
		for ( int i = 0; i < size; i++ ) {
			shape = (GRShape)shapes.get(i);
			if ( !visible_only && !selected_only ) {
				write_record[i] = true;
			}
			else if ( visible_only && selected_only ) {
				if ( shape.is_visible && shape.is_selected ) {
					write_record[i] = true;
				}
				else {
					write_record[i] = false;
				}
			}
			else if ( visible_only ) {
				if ( shape.is_visible ) {
					write_record[i] = true;
				}
				else {
					write_record[i] = false;
				}
			}
			else if ( selected_only ) {
				if ( shape.is_selected ) {
					write_record[i] = true;
				}
				else {
					write_record[i] = false;
				}
			}
		}
	}
	writeDBF ( dbf_file, table, write_record );
}

/**
Write the Dbase file corresponding to a shapefile.  This calls the
DbaseDataTable.write() method.  All records are written (enhancement might be
to add a selected record attribute).
@param dbf_file Dbase file name to write (with or without .dbf extension).
@param table DataTable to write.
@param write_record An array indicating whether records should be written.  This
array is created based on the parameters of the shape.  Pass null if all records should be written.
@exception IOException if there is an error writing the Dbase file.
*/
public static void writeDBF ( String dbf_file, DataTable table, boolean[] write_record )
throws IOException
{	DbaseDataTable.write ( dbf_file, table, write_record );
}

/**
Write both the SHP and the SHX files.  All shapes are written.
Currently, only the polygon shape type is supported.
@param shpFile name of SHP file, with or without extension.
@param shxFile name of SHX file, with or without extension.
@param allShapes list of GRShape to save.  <b>Warning - if a projection is
required, the shape data will be modified as it is converted to the requested projection.</b>.
@param fromProjection Projection of raw data.
@param toProjection Desired projection of output.
*/
public static void writeSHPAndSHX (	String shpFile, String shxFile, List<GRShape> allShapes,
	boolean visibleOnly, boolean selectedOnly, GeoProjection fromProjection,
	GeoProjection toProjection )
throws IOException
{	// Number of GRShapes in vector
	int numberRecords = 0;
	if ( allShapes != null ) {
		numberRecords = allShapes.size();
	}
	if (Message.isDebugOn) {
		Message.printDebug(5,"writeSHPandSHX", "Total number of shapes: " + numberRecords);
	}

	// Make an array to hold the calculated contentLength for each record.
	// Go through the vector of shapes, determine how many points are in
	// each individual shape, and use that to calculate content length.  
	// This is done up front because the content length is also used to
	// set the overall file length in the header.

	// The content length for each record is the # of 16 bit words in each
	// SHP file record. The general formula for getting content length for
	// polygons is:
	//	+ 8 bytes for rec header
	//	+ 4 bytes for ShapeType (1st field in SHP rec)
	//	+ 32 bytes for defining the box (2nd field in rec)
	//	+ 4 bytes for num parts (3rd field)
	//	+ 4 bytes for num pts (4rd field)
	//	+ 4 bytes for index of 1st part (5rd field)
	// these add up to 56.  In addition, add add 16 bytes for each point (6th+ field for polygons)

	// If a subset of shapes is being written, shapes that are not written
	// will have the contLenArray value set to zero.  This value can be
	// checked later when writing the actual shape data rather than doing
	// the boolean checks again.  Note that even null shapes have a shape
	// type and position in the file, which will result in a non-zero content length.
	int[] contLenArray = null;
	if ( numberRecords > 0 ) {
		contLenArray = new int[numberRecords];
	}

	// All shapes written to a shp file must be of the same type.  Get the type from the first shape.

	GRShape shape = null;
	int grShapeType = 0;
	if ( numberRecords > 0 ) {
		shape =	allShapes.get(0);
		grShapeType = shape.type;
	}
	shape = null;
	int esriShpType = UNKNOWN;

	int sumOfContLengths = 0;
	double minX = 0.0, minY = 0.0, maxX = 0.0, maxY = 0.0, minZ = 0.0, maxZ = 0.0, minM = 0.0, maxM = 0.0;
	boolean doProject = GeoProjection.needToProject ( fromProjection, toProjection );
	int shapeCount = 0;	// Counter for shapes (some may not be written).
				// This is used when writing the index file
				// length record.  It is also used when
				// determining the shape max/min coordinates.
	int reclen = 0;		// Record length for a shape.
	if ( numberRecords == 0 ) {
		// No data.
		sumOfContLengths = 0;
		shapeCount = 0;
	}
	else if ( grShapeType == GRShape.POINT ) {
		esriShpType = POINT;
		// Content length for points is constant =
		// 4 bytes for record number
		// 4 bytes for content length
		// 4 bytes for shape type
		// 2*8 bytes for x and y
		reclen = 14; // 28/2;
		// Content length will be same for every record...
		GRPoint point = null;
		for ( int i = 0; i < numberRecords; i++ ) {
			shape = allShapes.get(i);
			if ( shape.type == UNKNOWN ) {
				// Unknown (null?) shape type in the data.  Skip it rather than keeping.
				contLenArray[i] = 0;
				continue;
			}
			point = (GRPoint)shape;
			if ( (visibleOnly && selectedOnly && (!point.is_visible || !point.is_selected)) ||
				(visibleOnly && !point.is_visible)  || (selectedOnly && !point.is_selected) ) {
				contLenArray[i] = 0;
				continue;
			}
			if ( doProject ) {
				GeoProjection.projectShape(fromProjection, toProjection,point, true );
			}
			if ( shapeCount == 0 ) {
				// Initialize
				minX = point.x;
				minY = point.y;
				maxX = point.x;
				maxY = point.y;
			}
			else {
				// Inline all this code...
				if ( point.x < minX ) {
					minX = point.x;
				}
				if ( point.y < minY ) {
					minY = point.y;
				}
				if ( point.x > maxX ) {
					maxX = point.x;
				}
				if ( point.y > maxY ) {
					maxY = point.y;
				}
			}
			contLenArray[i] = reclen;
			sumOfContLengths += reclen;
			++shapeCount;
		}
	}
	else if ( grShapeType == GRShape.POINT_ZM ) {
		esriShpType = POINT_ZM;
		// Content length for points is constant =
		// 4 bytes for record number
		// 4 bytes for content length
		// 4 bytes for shape type
		// 2*8 bytes for x and y
		// 2*8 bytes for z and m
		reclen = 22; // 44/2;
		// Content length will be same for every record...
		GRPointZM point = null;
		for ( int i = 0; i < numberRecords; i++ ) {
			shape = allShapes.get(i);
			if ( shape.type == UNKNOWN ) {
				// Unknown (null?) shape type in the data.  Skip it rather than keeping.
				contLenArray[i] = 0;
				continue;
			}
			point = (GRPointZM)shape;
			if ( (visibleOnly && selectedOnly && (!point.is_visible || !point.is_selected)) ||
				(visibleOnly && !point.is_visible)  || (selectedOnly && !point.is_selected) ) {
				contLenArray[i] = 0;
				continue;
			}
			if ( doProject ) {
				GeoProjection.projectShape(fromProjection, toProjection,point, true );
			}
			if ( shapeCount == 0 ) {
				// Initialize
				minX = point.x;
				minY = point.y;
				maxX = point.x;
				maxY = point.y;
				minZ = point.z;
				maxZ = point.z;
				minM = point.m;
				maxM = point.m;
			}
			else {
				// Inline all this code...
				if ( point.x < minX ) {
					minX = point.x;
				}
				if ( point.x > maxX ) {
					maxX = point.x;
				}
				if ( point.y < minY ) {
					minY = point.y;
				}
				if ( point.y > maxY ) {
					maxY = point.y;
				}
				if ( point.z < minZ ) {
					minZ = point.z;
				}
				if ( point.z > maxZ ) {
					maxZ = point.z;
				}
				if ( point.m < minM ) {
					minM = point.m;
				}
				if ( point.m > maxM ) {
					maxM = point.m;
				}
			}
			contLenArray[i] = reclen;
			sumOfContLengths += reclen;
			++shapeCount;
		}
	}
	else if ( grShapeType == GRShape.POLYPOINT ) {
		esriShpType = MULTIPOINT;
		GRPolypoint polypoint = null;
		for ( int i = 0; i < numberRecords; i++ ) {
			shape = allShapes.get(i);
			if ( shape.type == UNKNOWN ) {
				// Unknown (null?) shape type in the data.
				// Skip it rather than keeping.
				contLenArray[i] = 0;
				continue;
			}
			polypoint = (GRPolypoint)shape;
			if ( (visibleOnly && selectedOnly && (!polypoint.is_visible || !polypoint.is_selected)) ||
				(visibleOnly && !polypoint.is_visible) || (selectedOnly && !polypoint.is_selected) ) {
				contLenArray[i] = 0;
				continue;
			}
			if ( doProject ) {
				GeoProjection.projectShape(fromProjection, toProjection,polypoint, true );
			}
			// Polypoint record:
			// 4 bytes record number
			// 4 bytes content length
			// 4 bytes for shape type
			// 32 bytes (4 doubles) for limits
			// 4 bytes for number of points
			// polypoint.npts*16
			// Total = 48 + polypoint.pts*16
			//reclen = (48 + polypoint.npts * 16)/2;
			reclen = 24 + polypoint.npts*8;
			contLenArray[i] = reclen;
			sumOfContLengths += reclen;
			if ( shapeCount == 0 ) {
				minX = polypoint.xmin;
				minY = polypoint.ymin;
				maxX = polypoint.xmax;
				maxY = polypoint.ymax;
			}
			else {
				// Inline all this code...
				if ( polypoint.xmin < minX ) {
					minX = polypoint.xmin;
				}
				if ( polypoint.ymin < minY ) {
					minY = polypoint.ymin;
				}
				if ( polypoint.xmax > maxX ) {
					maxX = polypoint.xmax;
				}
				if ( polypoint.ymax > maxY ) {
					maxY = polypoint.ymax;
				}
			}
			++shapeCount;
		}
	}
	else if ( grShapeType == GRShape.POLYLINE ) {
		esriShpType = ARC;
		GRPolyline polyline = null;
		for ( int i = 0; i < numberRecords; i++ ) {
			shape = allShapes.get(i);
			if ( shape.type == UNKNOWN ) {
				// Unknown (null?) shape type in the data.
				// Skip it rather than keeping.
				contLenArray[i] = 0;
				continue;
			}
			polyline = (GRPolyline)shape;
			if ( (visibleOnly && selectedOnly && (!polyline.is_visible ||
				!polyline.is_selected)) || (visibleOnly && !polyline.is_visible)  ||
				(selectedOnly && !polyline.is_selected) ) {
				contLenArray[i] = 0;
				continue;
			}
			if ( doProject ) {
				GeoProjection.projectShape(fromProjection, toProjection,polyline, true );
			}
			// There is 1 polyline, written as a polyline list...
			// 4 bytes record number
			// 4 bytes content length
			// 4 bytes for shape type
			// 32 bytes (4 doubles) for limits
			// 4 bytes for number of polylines
			// 4 bytes for total number of points.
			// 4 bytes for index of where polyline starts (an array of 1 integer in this case)
			// polyline.npts*16
			// Total = 56 + polyline.pts*16
			//reclen = (56 + (polyline.npts * 16))/2;
			reclen = 28 + polyline.npts*8;
			contLenArray[i] = reclen;
			sumOfContLengths += reclen;
			if ( shapeCount == 0 ) {
				minX = polyline.xmin;
				minY = polyline.ymin;
				maxX = polyline.xmax;
				maxY = polyline.ymax;
			}
			else {
				// Inline all this code...
				if ( polyline.xmin < minX ) {
					minX = polyline.xmin;
				}
				if ( polyline.ymin < minY ) {
					minY = polyline.ymin;
				}
				if ( polyline.xmax > maxX ) {
					maxX = polyline.xmax;
				}
				if ( polyline.ymax > maxY ) {
					maxY = polyline.ymax;
				}
			}
			++shapeCount;
		}
	}
	else if ( grShapeType == GRShape.POLYLINE_LIST ) {
		esriShpType = ARC;
		GRPolylineList polylinelist = null;
		for ( int i = 0; i < numberRecords; i++ ) {
			shape = (GRShape)allShapes.get(i);
			if ( shape.type == UNKNOWN ) {
				// Unknown (null?) shape type in the data.
				// Skip it rather than keeping.
				contLenArray[i] = 0;
				continue;
			}
			polylinelist = (GRPolylineList)shape;
			if ( (visibleOnly && selectedOnly && (!polylinelist.is_visible ||
				!polylinelist.is_selected)) || (visibleOnly && !polylinelist.is_visible)  ||
				(selectedOnly && !polylinelist.is_selected) ) {
				contLenArray[i] = 0;
				continue;
			}
			if ( doProject ) {
				GeoProjection.projectShape(fromProjection, toProjection,polylinelist, true );
			}
			// Header + array indicating position within polyline list...
			// There is 1 polyline, written as a polyline list...
			// 4 bytes for record number
			// 4 bytes for content length
			// 4 bytes for shape type
			// 32 bytes (4 doubles) for limits
			// 4 bytes for number of polyline
			// 4 bytes for total number of points.
			// polylinelist.npolylines*4 bytes for index of where polyline starts
			// polyline.npts*16
			// 32 bit words/2...
			//reclen = (52 + 4*polylinelist.npolylines)/2;
			// 16-bit words...
			reclen = 26 + 2*polylinelist.npolylines;
			for ( int j = 0; j < polylinelist.npolylines; j++ ) {
				//reclen+=(polylinelist.polylines[j].npts*16)/2;
				reclen += (polylinelist.polylines[j].npts*8);
				if ( shapeCount == 0 ) {
					minX = polylinelist.xmin;
					minY = polylinelist.ymin;
					maxX = polylinelist.xmax;
					maxY = polylinelist.ymax;
				}
				else {
					// Inline all this code...
					if ( polylinelist.xmin < minX ) {
						minX = polylinelist.xmin;
					}
					if ( polylinelist.ymin < minY ) {
						minY = polylinelist.ymin;
					}
					if ( polylinelist.xmax > maxX ) {
						maxX = polylinelist.xmax;
					}
					if ( polylinelist.ymax > maxY ) {
						maxY = polylinelist.ymax;
					}
				}
			}
			contLenArray[i] = reclen;
			sumOfContLengths += reclen;
			++shapeCount;
		}
	}
	else if ( grShapeType == GRShape.POLYGON ) {
		// Although the GR shape is a polygon, shape files always store
		// polygons as polygon lists.  Therefore, treat as a single-polygon list.
		esriShpType = POLYGON;
		GRPolygon polygon = null;
		for ( int i = 0; i < numberRecords; i++ ) {
			shape = allShapes.get(i);
			if ( shape.type == UNKNOWN ) {
				// Unknown (null?) shape type in the data.
				// Skip it rather than keeping.
				contLenArray[i] = 0;
				continue;
			}
			polygon = (GRPolygon)shape;
			if ( (visibleOnly && selectedOnly && (!polygon.is_visible || !polygon.is_selected)) ||
				(visibleOnly && !polygon.is_visible) || (selectedOnly && !polygon.is_selected) ) {
				contLenArray[i] = 0;
				continue;
			}
			if ( doProject ) {
				GeoProjection.projectShape(fromProjection, toProjection,polygon, true );
			}
			// There is 1 polygon, written as a polygon list...
			// 4 bytes record number
			// 4 bytes content length
			// 4 bytes for shape type
			// 32 bytes (4 doubles) for limits
			// 4 bytes for number of polygons
			// 4 bytes for total number of points.
			// 4 bytes for index of where polygon starts (an array of 1 integer in this case)
			// polygon.npts*16
			// Total = 56 + polygon.pts*16
			//reclen = (56 + (polygon.npts * 16))/2;
			reclen = 28 + polygon.npts*8;
			contLenArray[i] = reclen;
			sumOfContLengths += reclen;
			if ( shapeCount == 0 ) {
				minX = polygon.xmin;
				minY = polygon.ymin;
				maxX = polygon.xmax;
				maxY = polygon.ymax;
			}
			else {
				// Inline all this code...
				if ( polygon.xmin < minX ) {
					minX = polygon.xmin;
				}
				if ( polygon.ymin < minY ) {
					minY = polygon.ymin;
				}
				if ( polygon.xmax > maxX ) {
					maxX = polygon.xmax;
				}
				if ( polygon.ymax > maxY ) {
					maxY = polygon.ymax;
				}
			}
			++shapeCount;
		}
	}
	else if ( grShapeType == GRShape.POLYGON_LIST ) {
		esriShpType = POLYGON;
		GRPolygonList polygonlist = null;
		for ( int i = 0; i < numberRecords; i++ ) {
			shape = allShapes.get(i);
			if ( shape.type == UNKNOWN ) {
				// Unknown (null?) shape type in the data.
				// Skip it rather than keeping.
				contLenArray[i] = 0;
				continue;
			}
			polygonlist = (GRPolygonList)shape;
			if ( (visibleOnly && selectedOnly && (!polygonlist.is_visible ||
				!polygonlist.is_selected)) || (visibleOnly && !polygonlist.is_visible)  ||
				(selectedOnly && !polygonlist.is_selected) ) {
				contLenArray[i] = 0;
				continue;
			}
			if ( doProject ) {
				GeoProjection.projectShape(fromProjection, toProjection,polygonlist, true );
			}
			// Header + array indicating position within polygon list...
			// There is 1 polygon, written as a polygon list...
			// 4 bytes for record number
			// 4 bytes for content length
			// 4 bytes for shape type
			// 32 bytes (4 doubles) for limits
			// 4 bytes for number of polygons
			// 4 bytes for total number of points.
			// polygonlist.npolygons*4 bytes for index of where polygon starts
			// polygon.npts*16
			// 32 bit words/2...
			//reclen = (52 + 4*polygonlist.npolygons)/2;
			// 16-bit words...
			reclen = 26 + 2*polygonlist.npolygons;
			for ( int j = 0; j < polygonlist.npolygons; j++ ) {
				//reclen += (polygonlist.polygons[j].npts*16)/2;
				reclen += (polygonlist.polygons[j].npts*8);
				if ( shapeCount == 0 ) {
					minX = polygonlist.xmin;
					minY = polygonlist.ymin;
					maxX = polygonlist.xmax;
					maxY = polygonlist.ymax;
				}
				else {
					// Inline all this code...
					if ( polygonlist.xmin < minX ) {
						minX = polygonlist.xmin;
					}
					if ( polygonlist.ymin < minY ) {
						minY = polygonlist.ymin;
					}
					if ( polygonlist.xmax > maxX ) {
						maxX = polygonlist.xmax;
					}
					if ( polygonlist.ymax > maxY ) {
						maxY = polygonlist.ymax;
					}
				}
			}
			contLenArray[i] = reclen;
			sumOfContLengths += reclen;
			++shapeCount;
		}
	}
	else {
		throw new IOException ( "Unsupported shape type " + grShapeType );
	}

	// Open the files...
	EndianRandomAccessFile raf_SHP_stream = null;
	if ( (shpFile.length() > 4) && shpFile.regionMatches(true,
		(shpFile.length() - 4),".shp",0,4) ) {
		raf_SHP_stream = new EndianRandomAccessFile(shpFile, "rw");
	}
	else {
		raf_SHP_stream = new EndianRandomAccessFile(shpFile + ".shp", "rw");
	}

	EndianRandomAccessFile raf_SHX_stream = null;
	if ( (shxFile.length() > 4) && shxFile.regionMatches(true,
		(shxFile.length() - 4),".shx",0,4) ) {
		raf_SHX_stream = new EndianRandomAccessFile(shxFile, "rw");
	}
	else {
		raf_SHX_stream = new EndianRandomAccessFile(shxFile + ".shx", "rw");
	}

	// Write the header information...

	// byte 0-3 - file magic number for shapefiles...
	raf_SHP_stream.writeInt(9994);
	raf_SHX_stream.writeInt(9994);

	// bytes 4-23 are empty
	raf_SHP_stream.seek(24);
	raf_SHX_stream.seek(24);

	// 24-27 file the length of each file
	// All content lengths and adding 50 to it 
	// (50 is from 100 byte header which is 50 in 16 bit words)
	int SHPFileLength = sumOfContLengths + 50;
	raf_SHP_stream.writeInt(SHPFileLength);

	// SHX file length is 50 + (#records *4)
	// file header contributes 50 and each record
	// in the SHX has size of 4 16bit word 
	int SHXFileLength = (shapeCount*4) + 50;
	raf_SHX_stream.writeInt(SHXFileLength);

	if (Message.isDebugOn) {
		Message.printDebug(10, "writeSHPandSHX",
		"Size of SHP file: "+SHPFileLength + "\nSize of SHX file: " +SHXFileLength);
	}

	// 28-31 version
	raf_SHP_stream.writeLittleEndianInt(1000);
	raf_SHX_stream.writeLittleEndianInt(1000);

	// 32-35 shape type
	raf_SHP_stream.writeLittleEndianInt(esriShpType);
	raf_SHX_stream.writeLittleEndianInt(esriShpType);

	// 36-43 min X value for entire coverage area
	raf_SHP_stream.writeLittleEndianDouble(minX);
	raf_SHX_stream.writeLittleEndianDouble(minX);
	// 44-51 min Y value
	raf_SHP_stream.writeLittleEndianDouble(minY);
	raf_SHX_stream.writeLittleEndianDouble(minY);
	// 52-59 max X value
	raf_SHP_stream.writeLittleEndianDouble(maxX);
	raf_SHX_stream.writeLittleEndianDouble(maxX);
	// 60-67 max Y value
	raf_SHP_stream.writeLittleEndianDouble(maxY);
	raf_SHX_stream.writeLittleEndianDouble(maxY);
	if ( esriShpType == POINT_ZM ) {
		// min Z value
		raf_SHP_stream.writeLittleEndianDouble(minZ);
		raf_SHX_stream.writeLittleEndianDouble(minZ);
		// max Z value
		raf_SHP_stream.writeLittleEndianDouble(maxZ);
		raf_SHX_stream.writeLittleEndianDouble(maxZ);
	}
	if ( esriShpType == POINT_ZM ) {
		// min M value
		raf_SHP_stream.writeLittleEndianDouble(minM);
		raf_SHX_stream.writeLittleEndianDouble(minM);
		// max M value
		raf_SHP_stream.writeLittleEndianDouble(maxM);
		raf_SHX_stream.writeLittleEndianDouble(maxM);
	}

	// through 99 are unused, so move to byte 100
	raf_SHP_stream.seek(100);
	raf_SHX_stream.seek(100);

	// Write the records for the shapes.  Write the SHP in one loop and the
	// SHX in another loop.  Hopefully this will increase performance some
	// by keeping the data together on the disk.  Might want to consider this when writing the header.
	//
	// NOTE - do not need to project again because it was done above when
	// the content length was determined.

	int j = 0, ntotalpts = 0, p = 0, pos = 0;
	GRPoint point = null;
	GRPointZM pointzm = null;
	GRPolypoint polypoint = null;
	GRPolygon polygon = null;
	GRPolygonList polygonlist = null;
	GRPolyline polyline = null;
	GRPolylineList polylinelist = null;
	int shape_count2 = 0;	// Need for record count below...
	for ( int i = 0; i < numberRecords; i++ ) {
		shape = allShapes.get(i);
		if ( contLenArray[i] == 0 ) {
			// Determined above that shape does not need to be written...
			continue;
		}
		// Shape file Record Header is always 8 bytes.
		// 4 bytes is record number, starting at 1
		raf_SHP_stream.writeInt(shape_count2 + 1);

		// Write content length to SHX file...
		raf_SHP_stream.writeInt(contLenArray[i]);

		// SHP file Record Body
		// write shape type
		raf_SHP_stream.writeLittleEndianInt(esriShpType);

		// We don't use this below, so OK to increment here...
		++shape_count2;

		if ( grShapeType == GRShape.POINT ) { 
			point = (GRPoint)allShapes.get(i);   
			raf_SHP_stream.writeLittleEndianDouble(point.x);
			raf_SHP_stream.writeLittleEndianDouble(point.y);
		}
		else if ( grShapeType == GRShape.POINT_ZM ) { 
			pointzm = (GRPointZM)allShapes.get(i);   
			raf_SHP_stream.writeLittleEndianDouble(pointzm.x);
			raf_SHP_stream.writeLittleEndianDouble(pointzm.y);
			raf_SHP_stream.writeLittleEndianDouble(pointzm.z);
			raf_SHP_stream.writeLittleEndianDouble(pointzm.m);
		}
		else if ( grShapeType == GRShape.POLYPOINT ) { 
			polypoint = (GRPolypoint)allShapes.get(i);   
			// Write out the values for the bounding box 
			raf_SHP_stream.writeLittleEndianDouble(polypoint.xmin);
			raf_SHP_stream.writeLittleEndianDouble(polypoint.ymin);
			raf_SHP_stream.writeLittleEndianDouble(polypoint.xmax);
			raf_SHP_stream.writeLittleEndianDouble(polypoint.ymax);

			// Write out the total number of points in the polypoint

			raf_SHP_stream.writeLittleEndianInt(polypoint.npts);

			// Write out all points...
			for ( p = 0; p < polypoint.npts; p++ ) {
				raf_SHP_stream.writeLittleEndianDouble(polypoint.pts[p].x);
				raf_SHP_stream.writeLittleEndianDouble(polypoint.pts[p].y);
			}
		}
		else if ( grShapeType == GRShape.POLYLINE ) { 
			// Write as a single-polyline list...
			polyline = (GRPolyline)allShapes.get(i);   
			// Write out the values for the bounding box 
			raf_SHP_stream.writeLittleEndianDouble(polyline.xmin);
			raf_SHP_stream.writeLittleEndianDouble(polyline.ymin);
			raf_SHP_stream.writeLittleEndianDouble(polyline.xmax);
			raf_SHP_stream.writeLittleEndianDouble(polyline.ymax); 
			// Write out the coordinates, assuming one polyline
			raf_SHP_stream.writeLittleEndianInt(1);
	
			// Write out the total number of points in the polylines (in this case 1 polyline)...

			raf_SHP_stream.writeLittleEndianInt(polyline.npts);

			// Write out the index of the 1st point
			raf_SHP_stream.writeLittleEndianInt(0);

			// Now need to write out all points 
			for ( p = 0; p < polyline.npts; p++ ) {
				raf_SHP_stream.writeLittleEndianDouble(polyline.pts[p].x);
				raf_SHP_stream.writeLittleEndianDouble(polyline.pts[p].y);
			}
		}
		else if ( grShapeType == GRShape.POLYLINE_LIST ) { 
			// Write as a polyline list...
			polylinelist = (GRPolylineList)allShapes.get(i);
			// Write out the values for the bounding box 
			raf_SHP_stream.writeLittleEndianDouble(polylinelist.xmin);
			raf_SHP_stream.writeLittleEndianDouble(polylinelist.ymin);
			raf_SHP_stream.writeLittleEndianDouble(polylinelist.xmax);
			raf_SHP_stream.writeLittleEndianDouble(polylinelist.ymax); 
			// Write out the number of polylines...
			raf_SHP_stream.writeLittleEndianInt(polylinelist.npolylines);
		
			// Write out the total number of points in the polylines
			ntotalpts = 0;
			for ( j = 0; j < polylinelist.npolylines; j++ ) {
				ntotalpts += polylinelist.polylines[j].npts;
			}
			raf_SHP_stream.writeLittleEndianInt(ntotalpts);

			pos = 0;
			for ( j = 0; j < polylinelist.npolylines; j++ ) {
				raf_SHP_stream.writeLittleEndianInt(pos);
				pos += polylinelist.polylines[j].npts;
			}

			for ( j = 0; j < polylinelist.npolylines; j++ ) {
				// Now need to write out all points 
				for ( p = 0; p < polylinelist.polylines[j].npts; p++ ) {
					raf_SHP_stream.writeLittleEndianDouble(
					polylinelist.polylines[j].pts[p].x);
					raf_SHP_stream.writeLittleEndianDouble(
					polylinelist.polylines[j].pts[p].y);
				}
			}
		}
		else if ( grShapeType == GRShape.POLYGON ) { 
			// Write as a single-polygon list...
			polygon = (GRPolygon)allShapes.get(i);   
			// Write out the values for the bounding box 
			raf_SHP_stream.writeLittleEndianDouble(polygon.xmin);
			raf_SHP_stream.writeLittleEndianDouble(polygon.ymin);
			raf_SHP_stream.writeLittleEndianDouble(polygon.xmax);
			raf_SHP_stream.writeLittleEndianDouble(polygon.ymax); 
			// Write out the coordinates, assuming one polygon
			raf_SHP_stream.writeLittleEndianInt(1);
	
			// Write out the total number of points in the polygons
			// (in this case 1 polygon)...

			raf_SHP_stream.writeLittleEndianInt(polygon.npts);

			// Write out the index of the 1st point
			raf_SHP_stream.writeLittleEndianInt(0);

			// Now need to write out all points 
			for ( p = 0; p < polygon.npts; p++ ) {
				raf_SHP_stream.writeLittleEndianDouble(polygon.pts[p].x);
				raf_SHP_stream.writeLittleEndianDouble(polygon.pts[p].y);
			}
		}
		else if ( grShapeType == GRShape.POLYGON_LIST ) {
			// Write as a polygon list...
			polygonlist = (GRPolygonList)allShapes.get(i);   
			// Write out the values for the bounding box 
			raf_SHP_stream.writeLittleEndianDouble(polygonlist.xmin);
			raf_SHP_stream.writeLittleEndianDouble(polygonlist.ymin);
			raf_SHP_stream.writeLittleEndianDouble(polygonlist.xmax);
			raf_SHP_stream.writeLittleEndianDouble(polygonlist.ymax); 
			// Write out the number of polygons...
			raf_SHP_stream.writeLittleEndianInt(
				polygonlist.npolygons);
		
			// Write out the total number of points in the polygons
			ntotalpts = 0;
			for ( j = 0; j < polygonlist.npolygons; j++ ) {
				ntotalpts += polygonlist.polygons[j].npts;
			}
			raf_SHP_stream.writeLittleEndianInt(ntotalpts);

			pos = 0;
			for ( j = 0; j < polygonlist.npolygons; j++ ) {
				raf_SHP_stream.writeLittleEndianInt(pos);
				pos += polygonlist.polygons[j].npts;
			}

			for ( j = 0; j < polygonlist.npolygons; j++ ) {
				// Now need to write out all points 
				for ( p = 0; p < polygonlist.polygons[j].npts; p++ ) {
					raf_SHP_stream.writeLittleEndianDouble(polygonlist.polygons[j].pts[p].x);
					raf_SHP_stream.writeLittleEndianDouble(polygonlist.polygons[j].pts[p].y);
				}
			}
		}
		else {
			Message.printWarning(2, 
			"ESRIShapefile.writeSHPAndSHX", "Error recognizing GR shape type " + grShapeType );
			continue;
		}
	}

	// Write SHX body records.  Each record in SHX file is 8 bytes long,
	// containing a Big Int for offset and a Big Int for Content Length.
	// Offset is the offset (in 16bit size) from the start of the SHP file
	// to the given record in that SHP file.  So the offset for the first
	// record in the shp file is 50 (size of main header)

	int offsetcntr = 50;
	for ( int i = 0; i < numberRecords; i++ ){
		if ( contLenArray[i] == 0 ) {
			// Not writing shape (determined above)...
			continue;
		}

		// Write the offset...
		raf_SHX_stream.writeInt(offsetcntr);

		// Write the content length
		raf_SHX_stream.writeInt(contLenArray[i]);
	
		// Now increment counter by record length...
		offsetcntr += contLenArray[i];
	}

	raf_SHP_stream.close();
	raf_SHX_stream.close();
}

}