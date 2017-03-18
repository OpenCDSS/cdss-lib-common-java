//------------------------------------------------------------------------------
// XmrgGridLayer - read/write/manipulate NWS XMRG grid files
//------------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 8 Aug 2001	Morgan Sheedy,RTi	Initial Implementation
// 
// 13 Aug 2001	AMS,RTi			Added readData and getData methods
// 
// 20 Aug 2001	AMS,RTi			Changed method name readData to
// 					readAllData.  Added method to read
// 					and store only data that is >0.  New
// 					method named readPositiveDataOnly.
// 
// 27 Aug 2001	AMS,RTi			writeShapeFile changed to reflect
// 					new methods: writeSHPandSHX and 
// 					writeDBF.
// 
// 					Removed memory intensive calls to
// 					multiple methods when processing the 
// 					40+thousand individual cell x,y coords.
// 
// 2001-09-17	Steven A. Malers, RTi	Rename the class from Xmrg to
// 					XmrgGridLayer to give a better
// 					indication of its contents and
// 					differenciate from other grid classes.
// 					Make significant changes to reuse the
// 					GeoGridLayer and GeoLayer base class
// 					methods and to allow for on-the-fly
// 					access to data, similar to the Dbase
// 					files in shapefiles.  Streamline the
// 					code to read and write files now that
// 					the code has been debugged (hopefully).
// 2001-10-08	SAM, RTi		Review Javadoc and clean up before C++
// 					port.  Set the data format in
// 					_data_format.
// 2002-12-19	SAM, RTi		Add constructor to create an empty grid
// 					in memory.  Add a method to write the
// 					grid to XMRG grid format.  Replace the
// 					use of TSDate with DateTime.
// 2003-01-17	SAM, RTi		Fix a bug in the writeXmrgFile() method.
// 					The random access file needed to be
// 					removed before writing and use "rw".
// 2003-03-25	SAM, RTi		Change so that an EndianRandomAccessFile
//					is used for I/O so that HP (big-endian)
//					and Linux (little-endian) versions can
//					be handled.  The XMRG file is read
//					correctly regardless of its source.
//					The latest NWS XMRG standard (AWIPS
//					build 5.2.2) split the user id field
//					into a leading 2-characters for the
//					operating system and 8-characters for
//					the user id.
// 2004-09-09	J. Thomas Sapienza, RTi	Added a method (resize) that works 
//					similar to the extract_xmrg program.  
//					Given points that define a new grid
//					boundary, the grid is resized to have
//					a new area.  Typically this will be used
//					to clip the area, but there's no reason
//					it couldn't be used to make the area
//					larger.
//					  Also added a new version of
//					convertEndianXmrgFile that allows 
//					simultaneous conversion and resizing.
// 2004-09-16	JTS, RTi		Added accumulate().
// 2004-10-19	JTS, RTi		Changed the way exceptions are handled
//					in accumulate().
// 2004-11-11	JTS, RTi		Added getGrid() and isBigEndian().
// 2004-11-15	JTS, RTi		* When XMRG grids were resized, the
//					  header max value was not being 
//					  recomputed.  That is done now.
//					* When grids are resized their
//					  limits are now recomputed.
// 2004-11-16	JTS, RTi		Added determineMaxValueHeader().
// 2004-11-30	JTS, RTi		* Added setProcessFlag().
//					* Added setUserID().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package RTi.GIS.GeoView;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

//import RTi.GIS.GeoView.HRAPProjection;
import RTi.GR.GRLimits;
import RTi.GR.GRShape;
import RTi.Util.IO.EndianRandomAccessFile;
					// Use this because Xmgr files are
					// written on UNIX workstatations that
					// are big-endian OR Linux that are
					// kittle-endian.
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
This class reads, stores, and writes the official format National Weather
Service xmrg files.  The official xmrg format is a binary file that consists of
2 header records and multiple data records.  Each record is <B>preceeded</B> and
<B>followed</B> by 4 bytes, each an integer containing the number of bytes in
the record.  Apparently there is no standard as to whether the files are always
big or little endian - they conform to the machine that the system runs on.
In memory, the representation will always be for the machine.
The general format is as follows:
<PRE>
<TABLE>
<TH WIDTH="80">Header 1</TH><TH>Description</TH>
<TH>Type of Data/Number of bytes</TH><TH>Bytes for the field</TH> <TR>
<TD WIDTH="80">Field 1</TD>
<TD>HRAP-X coordinate of SouthWest corner of grid</TD>
<TD>int I*4</TD>
<TD>4-4</TD> <TR>
<TD WIDTH="80">Field 2</TD>
<TD>HRAP-Y coordinate of SouthWest corner of grid</TD>
<TD>int I*4</TD>
<TD>8-12</TD> <TR>
<TD WIDTH="80">Field 3</TD>
<TD>Number of HRAP grid boxes in the X direction(MAXX)</TD>
<TD>int I*4</TD>
<TD>12-16</TD> <TR>
<TD WIDTH="80">Field 4</TD>
<TD>Number of HRAP grid boxes in the Y direction(MAXY)</TD>
<TD>int I*4</TD>
<TD>16-20</TD>
<TR><TR><TR>
<TH WIDTH="80">Header 2</TH><TH>Description</TH>
<TH>Type of Data/Number of bytes</TH><TH>Bytes for the field</TH> <TR>
<TD WIDTH="80">Field 1</TD>
<TD>User ID</TD>
<TD>Logname of creator</TD>
<TD>char char*10</TD>
<TD>28-38</TD> <TR>
<TD WIDTH="80">Field 2</TD>
<TD>Saved date/time</TD>
<TD>ccyy-mm-dd hh:mm:ss (Z time)</TD>
<TD>char char*20</TD>
<TD>38-58</TD> <TR>
<TD WIDTH="80">Field 3</TD>
<TD>Process flag</TD>
<TD>Defines grid encoder processes</TD>
<TD>char char*8</TD>
<TD>58-66</TD> <TR>
<TD WIDTH="80">Field 4</TD>
<TD>Valid date/time</TD>
<TD>ccyy-mm-dd hh:mm:ss (Z time)</TD>
<TD>char char*20</TD>
<TD>66-86</TD> <TR>
<TD WIDTH="80">Field 5</TD>
<TD>Maximum Value</TD>
<TD>units = mm</TD>
<TD>int I*4</TD>
<TD>86-90</TD> <TR>
<TD WIDTH="80">Field 6</TD>
<TD>Version number</TD>
<TD>AWIPS build number</TD>
<TD>float R*4</TD>
<TD>90-94</TD> <TR>
<TR><TR><TR>
<TH WIDTH="80">Data Records</TH>
<TH>Type of Data/Number of bytes</TH><TH>bytes of the field</TH> <TR>
<TD>MAXY-1 records</TD>
<TD>with MAXX pieces of data/record</TD>
<TD>short I*2</TD>
<TD>First data <B>record</B>at: 98</TD>
</TABLE>
</PRE>
The data are laid out in a grid with the cell being identified by the HRAP
coordinates of the lower-left corner.  The grid cell size is 1 HRAP unit.
The SouthWest corner of the grid is the origin.  HRAP-X data increases to the
East and HRAP-Y data increases to the North.
*/
public class XmrgGridLayer extends GeoGridLayer
{

// Random access file to read in data.
private EndianRandomAccessFile __raf = null;

private String __oper_sys = "";		// Operating system creating the file.

// User identifier.
private String __userID = "";

// Saved date/time.
private DateTime __savedDate = null;

// AWIPS process flag.
private String __procFlag = "";

// Valid date/time.
private DateTime __validDate = null;

// Maximum value in header (applies to full grid).
private int __max_value_header = -999;

// AWIPS version number.
private float __versionNum = (float)0.0;

// Indicate whether the input that is read is big- or little-endian.
private boolean __big_endian = true;

/**
Construct an XmrgGridLayer and initialize with missing data.  This constructor
can be used when creating a grid in memory (e.g., for writing later with the
writeXmrgFile() method).  The maximum value is assigned to missing (-999) and
the AWIPS build number is set to ???.
@param filename	Name of the xmrg file - this can be null.
@param user_id String user identifier for creator of the file (e.g., login
name), up to 10 characters.
@param saved_date Date/time (to seconds) that the Xmrg file is created, Zulu
time.
@param proc_flag AWIPS build 5.0 process flag, up to 8 characters.  For example,
Use "RTM24" (RTi, Manual, 24 hour) if creating a 1day summed XMRG grid from the
NWSRFS GUI.
@param valid_date Valid date (to seconds) for the Xmrg file, Zulu time -
typically the same as the save date.
@param xor The HRAP x-coordinate of the southwest corner of the grid.
@param yor The HRAP y-coordinate of the southwest corner of the grid.
@param maxx The number of HRAP grid columns.
@param maxy The number of HRAP grid rows.
*/
public XmrgGridLayer ( String filename, String user_id, DateTime saved_date,
	String proc_flag, DateTime valid_date, int xor, int yor, int maxx, int maxy )
{	super ( filename );
	setDataFormat ( "XMRG" );
	setShapeType ( GRID );
	// Create a grid of the requested dimensions...
	GeoGrid grid = new GeoGrid();
	setGrid ( grid );
	grid.setUnits ( "MM" );
	// Set the information for the grid layer (floating point limits)...
	grid.xmin = (double)xor;
	grid.ymin = (double)yor;
	grid.xmax = (double)(xor + maxx);
	grid.ymax = (double)(yor + maxy);
	setLimits ( grid.xmin, grid.ymin, grid.xmax, grid.ymax );
	List<GRShape> shapes = getShapes();
	shapes.add ( grid );
	// Set the grid size using the grid integer row/column positions...
	grid.setSize ( xor, yor, (xor + maxx - 1), (yor + maxy - 1) );
	grid.setSizeFull ( xor, yor, (xor + maxx - 1), (yor + maxy - 1) );
	grid.setMissing ( -999.0 );
	grid.allocateDataSpace ();
	// Assign data maintained by this class...
	__userID = user_id;
	__savedDate = saved_date;
	__procFlag = proc_flag;
	__validDate = valid_date;
	__max_value_header = -999;
	__versionNum = 500;
}

/**
Constructor for class Xmrg.
@param filename	 Name of xmrg file to read.
@param read_data  A value of false indicates that only the header should be
read in (the data can be read on-the-fly later).  A value of true
indicates the header and all data will be read.
@param remain_open Indicates whether file should remain open (for additional reads).
@exception IOException if there is a read error.
*/
public XmrgGridLayer ( String filename, boolean read_data, boolean remain_open )
throws IOException
{
	super ( filename );
	setDataFormat ( "XMRG" );
	setShapeType ( GRID );
	// The entire grid will be read.
	GeoGrid grid = new GeoGrid ();
	setGrid ( grid );
	List<GRShape> shapes = getShapes();
	shapes.add ( grid );
	read ( read_data, remain_open );
}

/**
Constructor for class Xmrg.  This version allows a subset of the overall data
set to be used.  For example, if a full grid is available but only a certain
region is of interest, then only the region of interest will be processed,
although any cell in the grid can be read if necessary.
@param filename	 Name of xmrg file to read.
@param read_data  A value of false indicates that only the header should be
	read in.  A value of true indicates the header and all data will be read.
@param mincol Minimum HRAP column to consider.
@param minrow Minimum HRAP row to consider.
@param maxcol Maximum HRAP column to consider.
@param maxrow Maximum HRAP row to consider.
@param remain_open Indicates whether file should remain open (for additional reads).
@exception IOException if there is a read error.
*/
public XmrgGridLayer (	String filename, boolean read_data, boolean remain_open,
			int mincol, int minrow, int maxcol, int maxrow )
throws IOException {
	super ( filename );
	setDataFormat ( "XMRG" );
	setShapeType ( GRID );
	// The entire grid will be read.
	GeoGrid grid = new GeoGrid ();
	setGrid ( grid );
	List<GRShape> shapes = getShapes();
	shapes.add ( grid );
	grid.setSize ( mincol, minrow, maxcol, maxrow );
	read ( read_data, remain_open );
}

/**
Accumulates the values from one grid into the current grid, accounting 
for missing information (-999.0).
@param layer the layer from which to add values into the current layer.
*/
public void accumulate(XmrgGridLayer layer) {
	String routine = "XmrgGridLayer.accumulate";

	double totalValue = -999.0;
	double layerValue = -999.0;

	GRLimits gridLimits = layer.getLimits();
	int leftX = (int)gridLimits.getLeftX();
	int bottomY = (int)gridLimits.getBottomY();
	int rightX = (int)gridLimits.getRightX();
	int topY = (int)gridLimits.getTopY();
	int y = 0;

	try {
	for (int x = leftX; x < rightX; x++) {
		for (y = bottomY; y < topY; y++) {
			// if hourly cell value is less than zero (missing), 
			// do nothing to the 24 hr grid.  If  hourly cell 
			// value is greater than zero, look at 24 hr grid 
			// cell value to determine if the hourly cell value 
			// will replace the 24hr grid cell value (this only 
			// happens if the 24hr grid cell value was a 
			// negative (missing) value) OR if the hourly value 
			// is ADDED to the 24-hr value.

			try {
				layerValue = layer.getDataValue(x, y);
			}
			catch (Exception e) {
				layerValue = -999.0;
			}

			// if the hourly value is missing, then there is no
			// point in accumulating it to the current total.
			if (layerValue < 0) {
				continue;
			}

			try {
				totalValue = getDataValue(x, y);
			}
			catch (Exception e) {
				totalValue = -999.0;
			}

			if (totalValue < 0) {
				setDataValue(x, y, layerValue);
			}
			else {
				setDataValue(x, y, (totalValue + layerValue));
			}
		} 
	}
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Could not accumulate on "
			+ "layer due to errors.");
		Message.printWarning(2, routine, e);
	}
}

/**
Convert an XMRG file from one endian-ness to another.
@param input_file input xmrg file path.
@param output_file output xmrg file path.
@param big_endian  True if the file should be written out as big-endian, 
false if it should be written as little-endian.
@exception Exception if an error occurs (input file does not exist, file cannot
be processed, or output file cannot be written).
*/
public static void convertEndianXmrgFile (	String input_file,
						String output_file,
						boolean big_endian )
throws Exception
{	// Make a XmrgGridLayer (read all data and then close)...
	XmrgGridLayer input_grid = new XmrgGridLayer( input_file,
		true, false );
	input_grid.writeXmrgFile( output_file, big_endian );
	input_grid = null;
}

/**
Convert an XMRG file from one endian-ness to another and resize it at the
same time.
@param input_file input xmrg file path.
@param output_file output xmrg file path.
@param big_endian  True if the file should be written out as big-endian, 
false if it should be written as little-endian.
@throws Exception if an error occurs (input file does not exist, file cannot
be processed, or output file cannot be written).
@throws Exception if the number of columns or rows is less than 1, or if
there is any other error resizing the grid.
*/
public static void convertAndResizeEndianXmrgFile(String input_file,
String output_file, boolean big_endian, int leftX, int bottomY, int numColumns,
int numRows)
throws Exception
{	// Make a XmrgGridLayer (read all data and then close)...
	XmrgGridLayer input_grid = new XmrgGridLayer( input_file,
		true, false );
	input_grid.resize(leftX, bottomY, numColumns, numRows);
	input_grid.writeXmrgFile( output_file, big_endian );
	input_grid = null;
}

/**
Determines what the maximum value in the XmrgGridLayer is and sets that value
into the max value header, via setMaxValueHeader().
*/
public void determineMaxValueHeader() {
	GRLimits gridLimits = getLimits();
	int leftX = (int)gridLimits.getLeftX();
	int bottomY = (int)gridLimits.getBottomY();
	int rightX = (int)gridLimits.getRightX();
	int topY = (int)gridLimits.getTopY();	

	double max = Double.MIN_VALUE;
	double val = 0;
	int y = 0;

	for (int x = leftX; x < rightX; x++){
		for (y = bottomY; y < topY; y++) {
			try {
				val = getDataValue(x , y);
				if (val > max) {
					max = val;
				}
			}
			catch (Exception e) {
//				Message.printWarning(2, routine, e);
			}
		}
	}	

	setMaxValueHeader((int)max);
}

/**
Cleans up and calls garbage collector.
@exception Throwable if there is an error.
*/
public void finalize() throws Throwable {
	if ( __raf != null ) {
		__raf.close();
	}
	__raf = null;
	__oper_sys = null;
	__userID = null;
	__savedDate = null;
	__procFlag = null; 
	__validDate = null;
	super.finalize();
}

/**
Returns the data value for the column and row.
@param col The value for the column.
@param row The value for the row.
@return Data value for the specified grid cell.  If the x-y value is out of 
the range of the grid, the value used for missing data will be returned 
(default -999).
@exception IOException if problem when reading.
*/
public double getDataValue ( int col, int row ) throws IOException
{	GeoGrid grid = getGrid();
	if ( (row < grid.getMinRowFull()) || (row > grid.getMaxRowFull()) ||
		(col < grid.getMinColumnFull()) || (col > grid.getMaxColumnFull()) ) {
		Message.printWarning(2, "getDataValue", "column: " + col +
		" row: " + row + " is out of range. " +
		"Grid column range is: " + grid.getMinColumnFull() +
		"-" + grid.getMaxColumnFull() +
		" and row range is: " + grid.getMinRowFull() + "-" +
		grid.getMaxRowFull() );
		return grid._missing;
	}
	if ( (grid._double_data != null) && grid.contains(col,row) ) {
		// Get the value from the in-memory data.
		return grid.getDataValue ( col, row );
	}
	else {
		// Either the data were never stored in memory or the request is
		// for a cell outside the active grid space.  Read the data
		// value from the file...
		return readDataValue ( col, row );
	}
}

/**
Return the maximum value in the data, according to the header.  The value is
unchanged from the file and for precipitation data may therefore contain the
number of hundredths of MM.
@return the maximum value in header.
*/
public int getMaxValueHeader ()
{	return __max_value_header;
}

/**
Returns the operating system assigned in header.
@return the operating system from the header ("HP" or "LX" or "" if unknown).
*/
public String getOperSys()
{	return __oper_sys;
}

/**
Returns the AWIPS process flag.
@return String representing the process flag used in the file.
	For AWIPS Bld 5.0, the process flag is defined as:
	XXyHH where XX=process code, y=A (automatic) or M (manual),
	and HH = duration in hours.
*/
public String getProcessFlag() {
	return __procFlag;
}

/**
Returns the save date.
@return File save date.
*/
public DateTime getSavedDate() {
	return __savedDate;
}

/**
Returns the user identifier assigned in header.
@return String for the User ID from the header.
*/
public String getUserID() {
	return __userID;
}

/**
Returns the valid date as assigned in header.
@return Valid date retrieved from header.
*/
public DateTime getValidDate() {
	return __validDate;
}

/**
Returns the version number assigned in header.
@return Float for version number from header.
*/
public float getVersion() {
	return __versionNum;
}

/**
Returns whether the file is big endian or not.
@return whether the file is big endian or not.
*/
public boolean isBigEndian() {
	return __big_endian;
}

/**
Indicate whether a file is an Xmrg file.  Currently, the check consists only of
checking for "xmrg" in the file name with no "txt".
@param filename name of file to evaluate.
@return true if the file is an Xmrg file, false if not.
*/
public static boolean isXmrg ( String filename )
{	return isXmrg ( filename, false );
}

/**
Indicate whether a file is an Xmrg file.  Currently, the check consists only of
checking for "xmrg" in the file name with no "txt".
@param filename name of file to evaluate.
@param strict If true, strict checks will be done (currently not enabled).
@return true if the file is an Xmrg file, false if not.
*/
public static boolean isXmrg ( String filename, boolean strict )
{	String ufilename = filename.toUpperCase();
	File file = new File ( ufilename );
	// Do this because don't want a match in the leading path to bias the
	// results...
	if (	(StringUtil.indexOfIgnoreCase(file.getName(),"XMRG",0) >= 0) &&
		(StringUtil.indexOfIgnoreCase(file.getName(),"txt",0) < 0) ) {
		return true;
	}
	file = null;
	ufilename = null;
	return false;
}

/**
List the XMRG files in a directory that have non-zero maximum values, based on
the header information.  The directory should be a full path or relative to the
programs working directory.
This method is meant for testing and should not at this time be used in a
final application.
@return a Vector of String with full paths that have non-zero data, or null if
the directory is not found.
@param directory Name of a directory with XMRG files.
*/
public static List<String> listNonZeroFiles ( String directory )
{	File f = new File ( directory );
	String [] list = f.list();
	if ( list == null ) {
		return null;
	}
	XmrgGridLayer xmrg = null;
	List<String> nonzero_files = new Vector<String>();
	String file = null;
	for ( int i = 0; i < list.length; i++ ) {
		if ( !isXmrg ( list[i], false ) ) {
			continue;
		}
		file = directory + File.separator + list[i];
		try {	xmrg = new XmrgGridLayer ( file, false, false );
			if ( xmrg.getMaxValueHeader() > 0 ) {
				nonzero_files.add ( file );
			}
		}
		catch ( Exception e ) {
			continue;
		}
	}
	return nonzero_files;
}

/**
Process a string that represents a date in format
ccyy-mm-dd hh:mm:ss and formats it as a DateTime.  If an error occurs parsing
the string, the date is set to zeros.
@param strDate String that represents the date.
@return DateTime from header (for either valid date or saved date).
*/
private DateTime makeDate ( String strDate ) {
	DateTime date = null;
	
	try {	date = DateTime.parse(strDate);
	}
	catch (Exception tse) {
		// In non-strict xmrg files, sometimes the date is not filled in
		// with a real date, but with words.  Catch this and use a
		// default date of 0000-00-00 00:00:00.
		Message.printWarning(2, "XmrgGridLayer.makeDate",
		"String: \"" + strDate + "\" could not be formatted as a " +
		"DateTime.  Date set to 0.");
		try {	date = DateTime.parse("0000-00-00 00:00:00");
		}
		catch (Exception e) {
		}
	}
	return date;
}

/**
Read the Xmrg file after the file has been opened.  The file is read correctly
whether it is little or big endian.
@param read_data Indicates whether the data should be read.  The header is
always read. 
@param remain_open Indicates whether the file should remain open.  If true, then
data values can be read from the file, even after the initial read.  In typical
use, the file will either be completely read into memory or will only have the
header read, and data will be accessed later.
@exception IOException if there is an error reading the file.
*/
private void read ( boolean read_data, boolean remain_open )
throws IOException
{	int dl = 3;
	// Open a RandomAccessFile to read in the binary xmrg file and store its info.

	__raf = new EndianRandomAccessFile(getFileName(), "r");

	// Figure out whether the file is big (HP) or little (Linux) endian,
	// based on the HRAP column count.  First assume big-endian and read
	// that way.  If the result is greater than 10000, assume that the file
	// was actually little-endian...

	__big_endian = true;
	__raf.seek ( 4 );
	int test = __raf.readInt();

	if (Message.isDebugOn) {
		Message.printStatus(1, "read", "  Test value: " + test);
	}
	
	if ( (test > 10000) || (test < -10000) ) {
		// The value is out of the expected range, assume that we have
		// a little endian file...
		__big_endian = false;
	}

	// Read the header data...

	// Read an integer at the front of the first header record (from
	// FORTRAN).  Currently ignore.

	__raf.seek ( 0 );
	if ( __big_endian ) {
		__raf.readInt();
	}
	else {
		__raf.readLittleEndianInt();
	}

	// Coordinate for the SouthWest corner...

	
	int minX = 0, minY = 0;
	if ( __big_endian ) {
		minX = __raf.readInt();
		minY = __raf.readInt();
	}
	else {
		minX = __raf.readLittleEndianInt();
		minY = __raf.readLittleEndianInt();
	}

	// Number of columns and rows...

	int colCount = 0, rowCount = 0;
	if ( __big_endian ) {
		colCount = __raf.readInt();
		rowCount = __raf.readInt();
	}
	else {
		colCount = __raf.readLittleEndianInt();
		rowCount = __raf.readLittleEndianInt();
	}

	// Now set information in the grid shape and also the GeoLayer base class...

	GeoGrid grid = getGrid();
	grid.xmin = (double)minX;
	grid.ymin = (double)minY;
	grid.xmax = (double)(minX + colCount);
	grid.ymax = (double)(minY + rowCount);
	setLimits ( grid.xmin, grid.ymin, grid.xmax, grid.ymax );
	//if ( Message.isDebugOn ) {
		//Message.printDebug ( dl, "XmrgGridLayer.read",
		Message.printStatus ( 5, "XmrgGridLayer.read",
		"HRAP SW corner = " + minX + "," + minY );
		//Message.printDebug ( dl, "XmrgGridLayer.read",
		Message.printStatus ( 5, "XmrgGridLayer.read",
		"Number of columns = " + colCount + " number of rows = " +
		rowCount );
	//}

	// Set the grid size.  If the size is set to zero values, then reset
	// here to the full size of the grid.  If the size has already been
	// set (e.g., in the constructor), then only set the full size here and
	// leave the active size as is.

	if (	(grid.getMinColumn() == 0) && (grid.getMinRow() == 0) &&
		(grid.getMaxColumn() == 0) && (grid.getMaxRow() == 0) ) {
		grid.setSize ( minX, minY, (minX + colCount - 1),
				(minY + rowCount - 1) );
		// The data space for the grid will be allocated in the
		// readGridData() method.
	}
	grid.setSizeFull ( minX, minY, (minX + colCount - 1), (minY + rowCount - 1) );

	// Read an integer at the end of the first header record (from
	// FORTRAN).  Currently don't check the value.
	if ( __big_endian ) {
		__raf.readInt();
	}	
	else {
		__raf.readLittleEndianInt();
	}

	// Start the second header record...

	// Read an integer at the start of the second header record (from
	// FORTRAN).  Currently don't check the value.
	if ( __big_endian ) {
		__raf.readInt();
	}	
	else {
		__raf.readLittleEndianInt();
	}

	// Operating system and user id (handle the same whether big or little endian)...

	byte [] bytebuffer = new byte[10];
	__raf.readFully(bytebuffer);
	__userID = new String(bytebuffer);

	// AWIPS 5.2.2 uses a 2-char operating system and 8-char user ID so check and adjust...

	if ( __userID.regionMatches(false,0,"LX",0,2) ||__userID.regionMatches(false,0,"HP",0,2) ) {
		// Assume a newer file with the operating system...
		__oper_sys = __userID.substring(0,2);
		__userID = __userID.substring(2,__userID.length());
	}
	else {	// Assume an old file.  In order to force migration to the new
		// format, assign the operating system based on the endian-ness...
		if ( __big_endian ) {
			__oper_sys = "HP";
		}
		else {	__oper_sys = "LX";
		}
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, "XmrgGridLayer.read",
		"Oper Sys = \"" + __oper_sys + "\"" );
		Message.printDebug ( dl, "XmrgGridLayer.read",
		"User ID = \"" + __userID + "\"" );
	}

	// Saved date (handle the same whether big or little endian)

	bytebuffer = new byte[20];
	__raf.readFully ( bytebuffer );
	String strSavedDate = new String (bytebuffer);
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, "XmrgGridLayer.read",
		"Save date = \"" + strSavedDate + "\"" );
	}
	// Turn date string into a DateTime.  This will return a date value of
	// all 0's if string can't be converted to date
	__savedDate = makeDate(strSavedDate);
	strSavedDate = null;

	// AWIPS Process flag (handle the same whether big or little endian).
	bytebuffer = new byte[8];
	__raf.readFully ( bytebuffer );
	__procFlag = new String (bytebuffer);
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, "XmrgGridLayer.read",
		"AWIPS process flag = \"" + __procFlag + "\"" );
	}

	// Valid date (handle the same whether big or little endian).
	bytebuffer = new byte[20];
	__raf.readFully ( bytebuffer );
	String strValidDate = new String (bytebuffer);
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, "XmrgGridLayer.read",
		"Valid date = \"" + strValidDate + "\"" );
	}
	// Turn date string into a DateTime.  This will return a date value of
	// all 0's if string can't be converted to date
	__validDate = makeDate(strValidDate);
	strValidDate = null;
	bytebuffer = null;

	// Maximum value
	if ( __big_endian ) {
		__max_value_header = __raf.readInt (); 
	}
	else {	__max_value_header = __raf.readLittleEndianInt (); 
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, "XmrgGridLayer.read",
		"Maximum value in full grid = " + __max_value_header );
	}

	// AWIPS version number
	if ( __big_endian ) {
		__versionNum = __raf.readFloat ();
	}
	else {	__versionNum = __raf.readLittleEndianFloat ();
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, "XmrgGridLayer.read",
		"AWIPS version number = " + __versionNum );
	}

	// Read an integer at the end of the second header record (from
	// FORTRAN).  Currently don't check the value.
	if ( __big_endian ) {
		__raf.readInt();
	}
	else {	__raf.readLittleEndianInt();
	}

	// If read_data is set to true, read in the data.

	if ( !read_data ) {
		return;
	}

	readGridData ();

	// Close the file...
	if ( !remain_open ) {
		__raf.close();
		__raf = null;
	}
}

/**
Read a data value from the grid.  Using this method rather than reading the
data all at once can be slower.  However, it will be faster overall if only a
few cells are needed.  This method does not set any values into the GeoGrid data space.
@param c Column to read.
@param r Row to read.
@exception IOException if there is an error reading the value.
*/
public double readDataValue ( int c, int r )
throws IOException
{	// Compute the position to read using the full grid extents.  The first
	// row in the file is the one corresponding to the smallest row value.
	//
	// 98 bytes for header
	// 2 bytes for each column in row for previous full rows + 2 4-byte ints
	// 4 byte record start + 2 bytes for each column before current column
	int dl = 50;
	GeoGrid grid = getGrid();
	long pos = 98 // Header size,
		+ (r - grid.getMinRowFull())*
			(grid.getNumberOfColumnsFull()*2 + 8)
		+ 4
		+ (c - grid.getMinColumnFull())*2;
	// Now read the short and convert to 
	__raf.seek ( pos );
	double value = 0.0;
	if ( __big_endian ) {
		value = (double)__raf.readShort();
	}
	else {	value = (double)__raf.readLittleEndianShort();
	}
	double mm = 0.0;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl,
		"XmrgGridLayer.readDataValue",
		"[C" + c + "][R" + r + "] = " + value );
	}
	if ( value < 0.0 ) {
		// If the value is less than 0, then there is no data for the
		// cell so assign the missing value (-999)
		mm = grid.getMissing();
	}
	else {	// Divide by 100 to get mm (values in the file are mm*100).
		mm = value/100.0;
	}
	return mm;
}

/**
Read the double array that holds all the data points with the starting point
being in the SouthWest corner of the grid. THIS CURRENTLY ASSUMES THE FULL GRID
IS READ.  NEED TO CHANGE SO THE START AND END INTEGER READ ARE HANDLED CORRECTLY
FOR A GRID SUBSET.
@exception IOException if error occurs while reading.
*/
private void readGridData ()
throws IOException
{	int dl = 3;

	// Allocate the active data space for the grid.  This initializes all
	// data values to missing.
	GeoGrid grid = getGrid();
	grid.setMissing ( -999.0 );
	grid.allocateDataSpace();

	int r = 0;	// Row
	int c = 0;	// Column

	// Each row record is preceeded and followed by a 4-byte integer.  Read
	// these values and compare to make sure there is not an error reading the data.
	int endint = 0;
	int startint = 0;

	// Now try to read in all data.  Move cursor to the first data record,
	// which is at byte 98.  Remember that there is an extra 4 bytes at the
	// end and beginning of each record
	__raf.seek(98);
	if (Message.isDebugOn) {
		Message.printDebug( dl, "XmrgGridLayer.readGridData",
		"Cursor pointer is at: " + __raf.getFilePointer() +
		" when beginning to read data.");
	}

	// For now assume the data are MM.  May need a method to set if flash
	// flood guidance or other data are processed.
	grid.setUnits ( "MM" );

	short short_value = (short)0;
	short max_mm = (short)-1;	// Maximum value in file (MM)
	int rmax = grid.getMaxRowFull();
	int cmax = grid.getMaxColumnFull();
	double missing = grid.getMissing();
	short mm = (short)0;	// Millimeters
	int num_positive_values = 0;
	int cmin = grid.getMinColumnFull();
	for ( r = grid.getMinRowFull(); r <= rmax; r++ ) {
		// Integer at the start of the record...
		if ( __big_endian ) {
			startint = __raf.readInt();
		}
		else {	startint = __raf.readLittleEndianInt();
		}

		// read one row 
		for ( c = cmin; c <= cmax; c++ ) {
			if ( __big_endian ) {
				short_value = __raf.readShort();
			}
			else {	short_value = __raf.readLittleEndianShort();
			}
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl+2,
				"XmrgGridLayer.readGridData",
				"[C" + c + "][R" + r + "] = " + short_value );
			}
			if ( short_value < 0 ) {
				// If the value is less than 0, then there is
				// no data for the cell so assign the missing
				// value (-999)
				grid.setDataValue ( c, r, missing );
			}
			else {	// Divide by 100 to get mm (values in the file
				// are mm*100).
				mm = (short)(short_value/100);
				grid.setDataValue ( c, r, (double)mm );

				//count the number of valid data points
				if ( mm > 0 ) {
					// Count the number of real values.
					++num_positive_values;
				}
				if ( mm > max_mm ) {
					max_mm = mm;
				}
			}
		}
	
		// Read integer at the end of each record.

		if ( __big_endian ) {
			endint = __raf.readInt();
		}
		else {	endint = __raf.readLittleEndianInt();
		}

		// Check that startint and endint are the same.

		if ( startint != endint ) {
			throw new IOException("[C"+c+"][R" + r+"] Starting and "
				+ "ending integers for each record should be "+
				"equal.  Begin = " + startint + ", end = " +
				endint);
		}
	}

	// Reset the maximum value and count of positive values...

	__max_value_header = (int)max_mm;

	grid.setNumberOfPositiveValues ( num_positive_values );

	if (Message.isDebugOn){
		Message.printDebug(dl, "XmrgGridLayer.readGridData",
		"Total number of values >= 0.0: " + num_positive_values );
	}
}

/**
Resizes the layer's grid.  If the random access file which originally contained
the layer is still open, it is closed and set to null.  If the new grid goes
outside the boundary of the original grid, the cells which were not in the 
original grid will be filled with missing values.
@param leftX the X value that will be the new origin X.
@param bottomY the Y value that will be the new origin Y.
@param numColumns the number of columns in the new grid. 
@param numRows the number of rows in the new grid.
@throws Exception if there is an error closing the random access file.
@throws Exception if the number of columns or rows is less than 1.
*/
public void resize(int leftX, int bottomY, int numColumns, int numRows) 
throws Exception {
	if (__raf != null) {
		__raf.close();
		__raf = null;
	}

	GeoGrid grid = getGrid ();
	grid.resize(leftX, bottomY, numColumns, numRows);

	int rightX = leftX + numColumns;
	int topY = bottomY + numRows;
	setLimits(leftX, bottomY, rightX, topY);
	double max = Double.MIN_VALUE;
	double val = 0;
	int y = 0;

	for (int x = leftX; x < rightX; x++){
		for (y = bottomY; y < topY; y++) {
			try {
				val = getDataValue(x , y);
				if (val > max) {
					max = val;
				}
			}
			catch (Exception e) {
//				Message.printWarning(2, routine, e);
			}
		}
	}	

	setMaxValueHeader((int)max);
}

/**
Set the maximum value in the header.
@param max_value_header The maximum value for the grid, specified in the header.
*/
public void setMaxValueHeader ( int max_value_header )
{	__max_value_header = max_value_header;
}

/**
Sets the process flag in the header.
@param procFlag the process flag to set in the header.
*/
public void setProcessFlag(String procFlag) {
	__procFlag = procFlag;
}

/**
Sets the user ID in the header.
@param userID the user ID to set in the header.
*/
public void setUserID(String userID) {
	__userID = userID;
}

/**
Write the Xmrg grid as an ESRI shapefile.  All data values are written.
@param filename name of shapefile to write.
@param projection Projection for the output.
*/
public void writeShapefile ( String filename, GeoProjection projection )
throws IOException
{	// Call overloaded method
	writeShapefile ( filename, projection, false, 0.0, 0.0 );
}

/**
Write the Xmrg grid as an ESRI shapefile.
@param filename name of shapefile to write.
@param projection Projection that the data are to be written.
@param use_data_limits If true, the following arguments are used to limit
output to cells within the specified range.
@param min_data_value Cells with data values >= the value are written.
@param max_data_value Cells with data values <= the value are written.
WHY IS THIS METHOD NEEDED?
*/
public void writeShapefile (	String filename, GeoProjection projection,
				boolean use_data_limits, double min_data_value,
				double max_data_value )
throws IOException
{	// Call overloaded method
	super.writeShapefile ( filename, projection, use_data_limits,
			min_data_value, max_data_value );
}

/** Morgan - you should review this.
Create a text file containing the converted xmrg file information.  Both header
and data values are written to the file.  Each file consists of individual
records with an integer at the beginning and end of each record indicating the
number of bytes in the record.
The format of the file is:
<PRE>
<H3>Record 1</H3>
<TABLE>
<TH>FIELD NUMBER</TH><TH>Number(and type) of bytes</TH><TH>Brief Desription</TH>
<TR><TD>1</TD><TD>Int 4</TD><TD>HRAP-X coordinate for SouthWest corner</TD><TR>
<TD>2</TD><TD>Int 4</TD><TD>HRAP-Y coordinate for SouthWest corner</TD><TR>
<TD>3</TD><TD>Int 4</TD><TD>Number of columns of data</TD><TR>
<TD>4</TD><TD>Int 4</TD><TD>Number of rows of data</TD><TR>
</TABLE>
<H3>Record 2</H3>
<TABLE>
<TH>FIELD NUMBER</TH><TH>Number(and type) of bytes</TH><TH>Brief Desription</TH>
<TR><TD>1</TD><TD>Char 10</TD><TD>The user ID</TD><TR>
<TD>2</TD><TD>Char 20</TD><TD>Saved Date</TD><TR>
<TD>3</TD><TD>Char 8</TD><TD>Process Flag</TD><TR>
<TD>4</TD><TD>Char 20</TD><TD>Valid Date</TD><TR>
<TD>5</TD><TD>Int 4</TD><TD>Maximum Value</TD><TR>
<TD>6</TD><TD>Float 4</TD><TD>Version Number</TD><TR>
</TABLE>
<H3>Record 3-Max Number of Rows of Data</H3>
<TABLE>
<TD>3 to Max number of rows</TD><TD>Short 2</TD><TD>Data Value</TD><TR>
</TABLE>
</PRE>
@param filename Name of the file to create, including the extension.
@exception IOException if error occurs while reading.
*/
public void writeTextFile(String filename) throws IOException
{	// Open up a PrintWriter with automatic flushing for outputfile
	PrintWriter pw_out = new PrintWriter(new FileWriter( filename), true);

	// Maximum column in the active grid...
	GeoGrid grid = getGrid();
	int maxcol = grid.getMaxColumn();
	int mincol = grid.getMinColumn();
	int maxrow = grid.getMaxRow();
	int minrow = grid.getMinRow();

	// Write the header information...

	pw_out.println("HRAP X-Y coordinates: " + mincol + "," + minrow );
	pw_out.println("Number of columns(MAXX): " +grid.getNumberOfColumns());
	pw_out.println("Number of rows(MAXY): " + grid.getNumberOfRows());
	pw_out.println("Oper Sys: " + __oper_sys );
	pw_out.println("User ID: " +getUserID());
	pw_out.println("Saved date/time: " +getSavedDate());
	pw_out.println("Process Flag: " +getProcessFlag());
	pw_out.println("Valid date/time: " +getValidDate());
	pw_out.println("Maximum value: " +getMaxValueHeader());
	pw_out.println("Version number: " +getVersion());
	pw_out.println("Number of values > 0.0: " +
		grid.getNumberOfPositiveValues());
	pw_out.println();

	// Loop indices
	int c = 0;
	int r = 0;

	//print out 7 column headers
	StringBuffer buffer = new StringBuffer("row/col|");
	for ( c = mincol; c <= maxcol; c++ ) {
		buffer.append(StringUtil.formatString(c,"%7d|"));
	}
	pw_out.println(buffer.toString());
	buffer.setLength(0);
	buffer.append ( "-------" );
	for ( c = mincol; c <= maxcol; c++ ) {
		buffer.append("+-------");
	}
	buffer.append("+");
	pw_out.println(buffer.toString());

	// Print data with row numbers in first column...
	double value = 0.0;
	for ( r = maxrow; r >= minrow; r-- ) {
		buffer.setLength(0);
		buffer.append ( StringUtil.formatString(r, "%7d|"));

		for ( c = mincol; c <= maxcol; c++ ) {
			// See how this does on performance.  It may be a little
			// slow for large grids (might need to make the grid
			// data array public).
			value =	getDataValue ( c, r );
			if ( value < 0.0 ) {
				// Missing - always set to -1.
				value = -1.0;
			}
			buffer.append(StringUtil.formatString(value, "%7.2f") +
				"|");
		}
		pw_out.println(buffer.toString());
	}

	// Clean up
	pw_out.close();
	pw_out = null;
	buffer = null;
}

/** Morgan - you should review this
Create a text file containing the converted xmrg file information.  Both header
and data values are written to the file.  The name of the xmrg file that was
read is used for the output file name, with a ".txt" extension.  If no input
file was read (if data were created in memory), use the name "xmrg.txt".
@exception IOException if error occurs while reading.
*/
public void writeTextFile() throws IOException
{	String filename = getFileName();
	if ( filename.equals("") ) {
		writeTextFile("xmrg.txt");
	}
	else {	writeTextFile(filename+ ".txt");
	}
	filename = null;
}

/**
Write the entire layer to an Xmrg file as a big-endian file.
@param filename Full name of the file to write.
@exception IOException if there is an error writing the file.
*/
public void writeXmrgFile ( String filename )
throws IOException
{	GeoGrid grid = getGrid();
	writeXmrgFile ( filename,
			grid.getMinColumnFull(), grid.getMinRowFull(),
			(grid.getMaxColumnFull() - grid.getMinColumnFull()+1),
			(grid.getMaxRowFull() - grid.getMinRowFull() + 1),
			true );
}

/**
Write the entire layer to an Xmrg file for the requested endian-ness.
@param filename Full name of the file to write.
@param big_endian If true, the file will be written for use on a big-endian
system (e.g., HP UNIX).  If false, the file will be written for use on a
little-endian system (e.g., Linux).
@exception IOException if there is an error writing the file.
*/
public void writeXmrgFile ( String filename, boolean big_endian )
throws IOException
{	GeoGrid grid = getGrid();
	writeXmrgFile ( filename,
			grid.getMinColumnFull(), grid.getMinRowFull(),
			(grid.getMaxColumnFull() - grid.getMinColumnFull()+1),
			(grid.getMaxRowFull() - grid.getMinRowFull() + 1),
			big_endian );
}

/**
Write the layer to a big-endian Xmrg file.  A larger or smaller area than the
actual data can be written.  Missing data will be written using the missing
data flag.
@param filename Full name of the file to write.  This can be used to specify a
file other than the original file.
@param xor The HRAP x-coordinate of the southwest corner of the grid.
@param yor The HRAP y-coordinate of the southwest corner of the grid.
@param maxx The number of HRAP grid columns.
@param maxy The number of HRAP grid rows.
@exception IOException if there is an error writing the file.
*/
public void writeXmrgFile ( String filename, int xor, int yor,
			int maxx, int maxy )
throws IOException
{	writeXmrgFile ( filename, xor, yor, maxx, maxy, true );
}

/**
Write the layer to a big-endian Xmrg file.  A larger or smaller area than the
actual data can be written.  Missing data will be written using the missing
data flag.
@param filename Full name of the file to write.  This can be used to specify a
file other than the original file.
@param xor The HRAP x-coordinate of the southwest corner of the grid.
@param yor The HRAP y-coordinate of the southwest corner of the grid.
@param maxx The number of HRAP grid columns.
@param maxy The number of HRAP grid rows.
system (e.g., HP UNIX).  If false, the file will be written for use on a
little-endian system (e.g., Linux).
@exception IOException if there is an error writing the file.
*/
public void writeXmrgFile ( String filename, int xor, int yor, int maxx, int maxy, boolean big_endian )
throws IOException
{	int dl = 3;
	// Open a RandomAccessFile to write the binary xmrg file.  Make sure the
	// file does not already exist.  If a file does exist, delete it first.
	// Otherwise, the binary write will just update the existing binary file.
	if (IOUtil.fileExists(filename)) {
		File fileToDelete = new File(filename);
		fileToDelete.delete();
		fileToDelete = null;
	}

	EndianRandomAccessFile raf = new EndianRandomAccessFile(filename, "rw");

	// Write the header data...

	// Write an integer at the front of the first header record (from FORTRAN)...
	if ( big_endian ) {
		raf.writeInt ( 16 );
	}
	else {	raf.writeLittleEndianInt ( 16 );
	}

	// Coordinate for the SouthWest corner...

	if ( big_endian ) {
		raf.writeInt ( xor );
		raf.writeInt ( yor );
	}
	else {	raf.writeLittleEndianInt ( xor );
		raf.writeLittleEndianInt ( yor );
	}

	// Number of columns and rows...

	if ( big_endian ) {
		raf.writeInt ( maxx );
		raf.writeInt ( maxy );
	}
	else {	raf.writeLittleEndianInt ( maxx );
		raf.writeLittleEndianInt ( maxy );
	}

	// Write an integer at the end of the first header record (from
	// FORTRAN)...
	if ( big_endian ) {
		raf.writeInt ( 16 );
	}
	else {	raf.writeLittleEndianInt ( 16 );
	}

	// Start the second header record...

	// Write an integer at the start of the second header record (from
	// FORTRAN)...
	if ( big_endian ) {
		raf.writeInt ( 66 );
	}
	else {	raf.writeLittleEndianInt ( 66 );
	}

	// Oper sys... Write based on the endian-ness...

	StringBuffer buffer = new StringBuffer ();
	if ( big_endian ) {
		buffer.append ( "HP" );
	}
	else {	buffer.append ( "LX" );
	}
	for ( int i = buffer.length(); i < 2; i++ ) {
		buffer.append('\0');
	}	
	if ( buffer.length() > 2 ) {
		buffer.setLength(2);
	}
	raf.writeBytes(buffer.toString());

	// User id...

	buffer = new StringBuffer ( __userID );
	for ( int i = buffer.length(); i < 8; i++ ) {
		buffer.append('\0');
	}	
	if ( buffer.length() > 8 ) {
		buffer.setLength(8);
	}
	raf.writeBytes(buffer.toString());

	// Saved date

	if ( __savedDate == null ) {
		buffer = new StringBuffer ( "0000-00-00 00:00:00" );
	}
	else {	buffer = new StringBuffer ( __savedDate.toString(
			DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS) );
	}
	for ( int i = buffer.length(); i < 20; i++ ) {
		buffer.append('\0');
	}	
	if ( buffer.length() > 20 ) {
		buffer.setLength(20);
	}
	raf.writeBytes(buffer.toString());

	// AWIPS Process flag

	buffer = new StringBuffer ( __procFlag );
	for ( int i = buffer.length(); i < 8; i++ ) {
		buffer.append('\0');
	}	
	if ( buffer.length() > 8 ) {
		buffer.setLength(8);
	}
	raf.writeBytes(buffer.toString());

	// Valid date.

	if ( __validDate == null ) {
		buffer = new StringBuffer ( "0000-00-00 00:00:00" );
	}
	else {	buffer = new StringBuffer ( __validDate.toString(
			DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS) );
	}
	for ( int i = buffer.length(); i < 20; i++ ) {
		buffer.append('\0');
	}	
	if ( buffer.length() > 20 ) {
		buffer.setLength(20);
	}
	raf.writeBytes(buffer.toString());

	// Maximum value

	if ( big_endian ) {
		raf.writeInt ( __max_value_header );
	}
	else {	raf.writeLittleEndianInt ( __max_value_header );
	}

	// AWIPS version number

	if ( big_endian ) {
		raf.writeFloat ( __versionNum );
	}
	else {	raf.writeLittleEndianFloat ( __versionNum );
	}

	// Write an integer at the end of the second header record (from
	// FORTRAN)...
	if ( big_endian ) {
		raf.writeInt ( 66 );
	}
	else {	raf.writeLittleEndianInt ( 66 );
	}

	// Write the grid data...

	int r = 0;	// Row
	int c = 0;	// Column

	double value = 0.0;	// Data value
	GeoGrid grid = getGrid();
	int rmax = grid.getMaxRowFull();
	int cmax = grid.getMaxColumnFull();
	int rmin = grid.getMinRowFull();
	int cmin = grid.getMinColumnFull();
	double missing = grid.getMissing();
	int rmax_req = yor + maxy - 1;
	int cmax_req = xor + maxx - 1;
	int recsize = maxx*2;	// Bytes for data record -
				// number of columns*2 bytes (for a short)
	for ( r = yor; r <= rmax_req; r++ ) {
		// Integer at the start of the record...
		if ( big_endian ) {
			raf.writeInt ( recsize );
		}
		else {	raf.writeLittleEndianInt ( recsize );
		}

		// write one row 
		for ( c = xor; c <= cmax_req; c++ ) {
//Message.printStatus(1, "", "[" + c + "][" + r + "] (" + cmin + "," + rmin 
//	+ ") (" + cmax + "," + rmax + ") (" + maxx + "," + maxy + ")");
			// Write values as 1/100 of the original value...
			if ((r<rmin) || (r > rmax) || (c < cmin) || (c >cmax)) {
				// Not in the grid so need to write missing...
				value = missing;
			}
			else {
				value = grid.getDataValue ( c, r );
			}
			if ( value < 0.0 ) {
				// If the value is less than 0, then there is
				// no data so write -1...
				value = -1.0;
			}
			else {	// Multiply by 100 to get mm (values in the file
				// are mm*100).
				value *= 100.0;
			}
			if ( big_endian ) {
				raf.writeShort ( (short)value );
			}
			else {	raf.writeLittleEndianShort ( (short)value );
			}
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl,
				"XmrgGridLayer.readGridData",
				"[C" + c + "][R" + r + "] = " + value );
			}
		}
	
		// Write integer at the end of each record.

		if ( big_endian ) {
			raf.writeInt(recsize);
		}
		else {	raf.writeLittleEndianInt(recsize);
		}
	}

	// Close the file...
	raf.close();
}

}