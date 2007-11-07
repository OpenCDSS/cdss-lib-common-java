// ----------------------------------------------------------------------------
// MexicoCsmnTS - class for reading/writing Mexico CSMN time series file formats
// ----------------------------------------------------------------------------
// History:
//
// 2002-01-31	Steven A. Malers, RTi	Copy UsgsNwisTS and update as
//					appropriate.
// 2002-12-06	SAM, RTi		Fix bug where the last day of each month
//					was not getting transferred.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;

/**
The MexicoCsmnTS class reads and writes Mexico CSMN time series.  These time
series are comprized of a CATALOGO.TXT file listing all stations, and individual
XXX_YYY.csv time series files, where XXX is the abbreviation for a basin and
YYY is the abbreviation for a data type.
Water Information System time series.  A number of different file formats are
available.  The current code focuses on the daily surface water format.
*/
public class MexicoCsmnTS
{

/**
Vector of TS that are read from the catalog file.   Save the list here rather
than in an application like TSTool because only one catalog file is read
during a session.
*/
private static Vector __catalog_Vector = null;

/**
Return the Vector of TS header read from the catalog file.
@return the Vector of TS header read from the catalog file.
*/
public static Vector getCatalogTSList ()
{	return __catalog_Vector;
}

/**
Return a sample so that a user/developer knows what a file looks like.  Right
now, the samples are compiled into the code to make absolutely sure that the
programmer knows what sample is supported.
@return Sample file contents.
*/
public static Vector getSample ()
{	Vector	s = new Vector ( 50 );
	s.addElement ( "#" );
	s.addElement ("# The Mexico CSMN data use a catalog file for stations");
	s.addElement (
	"# and time series files for each state/data type combination." );
	s.addElement ( "#" );
	s.addElement ("# The catalog file is as follows:" );
	s.addElement ( "" );
	s.addElement (
"===============================================================================================" );
	s.addElement (
	" Station-         STN-NAME            DRAINAGE-BASIN    LAT- LAT- LAT- LON- LON- LON- ELEVATI" );
	s.addElement (
	"    ID                                                  DEGR MINU SECO DEGR MINU SECO   ON" );
	s.addElement (
	"                                                        EES  TES  NDS  EES  TES  NDS" );
	s.addElement (
	"-----------------------------------------------------------------------------------------------" );
	s.addElement (
	" 00001001 AGUASCALIENTES, AGS.     LERMA SANTIAGO       21   52   00   102  18   00   1,908.0" );
	s.addElement (
	" 00001003 CALVILLO, CALVILLO       LERMA SANTIAGO       21   53   00   102  43   00   1,702.0" );
	s.addElement (
	" 00001004 CAÑADA, HONDA, AGS.      LERMA SANTIAGO       22   00   00   102  11   00   1,185.0" );
	s.addElement (
	" 00001005 EL NIAGARA, AGS.         LERMA SANTIAGO       21   48   00   102  22   00   1,805.0" );
	s.addElement (
	" 00001006 EL TULE, ASIENTOS        LERMA SANTIAGO       22   05   00   102  06   00   1,970.0" );
	s.addElement ( "..." );
	s.addElement ( "" );
	s.addElement ("# The time series file is as follows:" );
	s.addElement ( "" );
	s.addElement (
"	Station-ID,ELEMENT-CODE,YEAR-MONTH,VALUE-1,VALUE-2,VALUE-3,VALUE-4,VALUE-5,VALUE-6,VALUE-7,VALUE-8,VALUE-9,VALUE-10,VALUE-11,VALUE-12,VALUE-13,VALUE-14,VALUE-15,VALUE-16,VALUE-17,VALUE-18,VALUE-19,VALUE-20,VALUE-21,VALUE-22,VALUE-23,VALUE-24,VALUE-25,VALUE-26,VALUE-27,VALUE-28,VALUE-29,VALUE-30,VALUE-31" );
	s.addElement (
	"00001001,005,1980-07,0,0,0,0,5.4,0,0,0,0,0,4.5,0,0,0,0,0,0,0,9,9.4,19,0,10,28,0.5,0,2.2,0.3,13,0.5,0" );
	s.addElement (
	"00001001,005,1980-10,0,0,0,0,0,0,0,0,0,0,6.8,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0.2,0,9.5,12.5,16.3,8" );
	s.addElement (
	"00001003,005,1932-01,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0" );
	s.addElement (
	"00001003,005,1932-08,8.3,0,0,0,0,0,10.7,8.1,0,0,0,0,0,0,0,0,0,0,-99999,-99999,-99999,-99999,-99999,-99999,-99999,-99999,-99999,-99999,-99999,-99999,-99999" );
	s.addElement (
	"00001003,005,1932-10,0,5,9.5,0.3,0,0,0,0,1.8,0.5,0,0,0,5,0,0,0,0,0,0,0,0,27,0,0,0,0,0,0,0,0" );
	s.addElement (
	"00001003,005,1932-11,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-99999" );
	s.addElement ( "..." );
	return s;
}

/**
Return the State abbreviation given the State number.
*/
public static String getStateAbbreviation ( int state_num )
{	// If this is a performance problem, might make the data static, but
	// try not to bloat the package with a bunch of static data...
	String state_abbrev[] = {	"AGS",	// 1
					"BC",	// 2
					"BCS",	// 3
					"CAMP",	// 4
					"COAH",	// 5
					"COL",	// 6
					"CHIS",	// 7
					"CHIH",	// 8
					"DF",	// 9
					"DGO",	// 10
					"GTO",	// 11
					"GRO",	// 12
					"HGO",	// 13
					"JAL",	// 14
					"MEX",	// 15
					"MICH",	// 16
					"MOR",	// 17
					"NAY",	// 18
					"NL",	// 19
					"OAX",	// 20
					"PUE",	// 21
					"QRO",	// 22
					"QROO",	// 23
					"SLP",	// 24
					"SIN",	// 25
					"SON",	// 26
					"TAB",	// 27
					"TAMPS",// 28
					"TLAX",	// 29
					"VER",	// 30
					"YUC",	// 31
					"ZAC"};	// 32
	return state_abbrev[state_num - 1];
}

/**
Return the list of State abbreviations and numbers in the form
public static String getStateAbbreviations ()
@return a Vector with the state abbreviations, sorted alphabetically.
@param format Currently ignored.
*/
public static Vector getStateAbbreviations ( int format )
{	Vector v = new Vector(32);
	for ( int i = 0; i < 32; i++ ) {
		v.addElement ( getStateAbbreviation ( (i + 1) ) );
	}
	return StringUtil.sortStringList ( v );
}

/**
Read the catalog file, returning a Vector of TS with only header information
being read.  This method returns the Vector and also sets the internal Vector,
which can be returned using getCatalogTSList().
@param filename Name of file to read.
@return Vector of DayTS for time series listed in the file.
*/
public static Vector readCatalogFile ( String filename )
{	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	Vector tslist = null;
	try {	BufferedReader in = new BufferedReader (
			 new InputStreamReader(
				IOUtil.getInputStream ( full_fname )) );
		tslist = new Vector();
		// Formats for fixed read...
		int format[] = {	StringUtil.TYPE_STRING,
					StringUtil.TYPE_STRING,
					StringUtil.TYPE_STRING };
		int format_w[] = {	9,
					26,
					20 };
		Vector tokens = new Vector(3);
		TS ts;
		String line;
		while ( true ) {
			line = in.readLine();
			if ( line == null ) {
				// No more data
				break;
			}
			if ( !line.startsWith(" 0") ) {
				// Not a station line...
				continue;
			}
			// Create a new time series...
			ts = new DayTS();
			// Break the fixed-format lines of input...
			tokens.removeAllElements();
			StringUtil.fixedRead ( line, format, format_w, tokens);
			// Set the data values.
			ts.setInputName ( full_fname );
			ts.setIdentifier ( ((String)tokens.elementAt(0)).trim(),
				"MexicoCSMN", "", "Day", "" );
			ts.setDescription(((String)tokens.elementAt(1)).trim());
			ts.setDataUnits("MM");
			ts.getIdentifier().setInputType("MexicoCSMN");
			ts.getIdentifier().setInputName(full_fname);
			//Message.printStatus ( 1, "", "SAMX catalog TS \"" +
				//ts.getIdentifierString() + "\"" );
			tslist.addElement ( ts );
		}
		in.close();
		in = null;
	}
	catch ( Exception e ) {
		Message.printWarning( 2,
		"UsgsNwisTS.readTimeSeries(String,...)",
		"Error reading file \"" + full_fname + "\"" );
	}
	__catalog_Vector = tslist;
	return __catalog_Vector;
}

/**
SEE THE OTHER READ METHOD - THIS ONE HAS NOT BEEN FULLY EVALUATED.
Read a time series from a Mexico CSMN EV or PP format file.
The resulting time series will have an identifier
like STATIONID.CSMN.EV.1Day.
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
@return a pointer to a newly-allocated time series if successful, a NULL
pointer if not.
@param filename Name of file to read.
@param date1 Starting date to initialize period (NULL to read the entire time
series).
@param date2 Ending date to initialize period (NULL to read the entire time
series).
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String filename, DateTime date1,
			DateTime date2, String units, boolean read_data )
{	TS	ts = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	try {	BufferedReader in = new BufferedReader (
			 new InputStreamReader(
				IOUtil.getInputStream ( full_fname )) );
		// Don't have a requested time series...
		ts = readTimeSeries ( (TS)null, in, full_fname, date1, date2,
			units, read_data );
		ts.setInputName ( full_fname );
		ts.getIdentifier().setInputType("MexicoCSMN");
		ts.getIdentifier().setInputName(full_fname);
		ts.addToGenesis ( "Read data from \"" + full_fname +
			"\" for period " + ts.getDate1() + " to " +
			ts.getDate2() );
		in.close();
		in = null;
	}
	catch ( Exception e ) {
		Message.printWarning( 2,
		"MexicoCsmnTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
	}
	return ts;
}

/**
THIS METHOD SHOULD BE CALLED - THE OTHER READ METHODS HAVE NOT BEEN FULLY
EVALUATED.
Read a time series from a Mexico CSMN EV or PP file.  The TSID string is
specified in addition to the path to the file.  It is expected that a TSID in
the file matches the TSID (and the path to the file, if included in the TSID
would not
propertly allow the TSID to be specified).  This method can be used with newer
code where the I/O path is separate from the TSID that is used to identify the
time series.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated time series if successful, a NULL pointer
if not.
@param tsident_string The full identifier for the time series to
read (where the scenario is NOT the file name).
@param filename The name of a file to read
(in which case the tsident_string must match one of the TSID strings in the
file).
@param date1 Starting date to initialize period (NULL to read the entire time
series).
@param date2 Ending date to initialize period (NULL to read the entire time
series).
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries (	String tsident_string, String filename,
					DateTime date1, DateTime date2,
					String units, boolean read_data )
throws Exception
{	TS	ts = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	if ( !IOUtil.fileReadable(full_fname) ) {
		Message.printWarning( 1,
		"MexicoCsmnTS.readTimeSeries",
		"Unable to determine file for \"" + filename + "\"" );
		return ts;
	}
	BufferedReader in = null;
	try {	in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 1,
		"MexicoCsmnTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	// Call the fully-loaded method...
	// Pass the file pointer and an empty time series, which
	// will be used to locate the time series in the file.
	ts = TSUtil.newTimeSeries ( tsident_string, true );
	if ( ts == null ) {
		Message.printWarning( 1,
		"MexicoCsmnTS.readTimeSeries(String,...)",
		"Unable to create time series for \"" +
		tsident_string + "\"" );
		return ts;
	}
	ts.setIdentifier ( tsident_string );
	//readTimeSeriesList (	ts, in,
	readTimeSeries (	ts, in,
				full_fname,
				date1, date2,
				units, read_data );
	ts.setInputName ( full_fname );
	ts.getIdentifier().setInputType ( "MexicoCSMN" );
	ts.getIdentifier().setInputName ( filename );
	ts.addToGenesis ( "Read data from \"" + full_fname +
		"\" for period " + ts.getDate1() + " to " +
		ts.getDate2() );
	in.close();
	return ts;
}

/**
Read a time series from a Mexico CSMN EV or PP file.
@return a pointer to time series if successful, null if not.  The calling code
is responsible for freeing the memory for the time series.
@param req_ts Pointer to time series to fill.  If null,
return a new time series.  All data are reset, except for the identifier, which
is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param filename Name of file that is being read.  This is needed because the
file is reopened to get the last dates in the file.
@param req_date1 Requested starting date to initialize period (or NULL to read
the entire time series).
@param req_date2 Requested ending date to initialize period (or NULL to read
the entire time series).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception Exception if there is an error reading the time series.
*/
public static TS readTimeSeries (	TS req_ts, BufferedReader in,
					String filename,
					DateTime req_date1, DateTime req_date2,
					String req_units, boolean read_data )
throws Exception
{	String	routine = "MexicoCSMN.readTimeSeries";
	String	string = null;
	int	dl = 10;
	DateTime	date1_file = null, date2_file = null;

	// Mexico CSMN files do not have header information with the period for
	// the time series.  However, because the data are monthly, there are
	// not a large number of records.  Therefore, all of the strings for the
	// requested time series are first read into memory and are then parsed
	// to determine the period and get data.

	Vector input_lines = new Vector();
	String line;
	String location = req_ts.getLocation();
	int linecount = 0;
	while ( true ) {
		line = in.readLine();
		if ( line == null ) {
			break;
		}
		++linecount;
		if ( line.startsWith(location) ) {
			input_lines.addElement ( line );
		}
		else if ( (linecount > 1) && (input_lines.size() > 0) ) {
			// Header has been read and all the grouped time series
			// lines have been read...
			break;
		}
	}

	if ( input_lines.size() == 0 ) {
		// No data have been found for the time series...
		String message = "Time series \""+req_ts.getIdentifierString() +
			"\" was not found in \"" + filename + "\"";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}

	// Now have the data lines.  Figure out the period and allocate
	// memory...

	line = (String)input_lines.elementAt(0);
	Vector tokens = StringUtil.breakStringList(line,",",0);
	date1_file = DateTime.parse ( (String)tokens.elementAt(2) );
	date1_file.setPrecision ( DateTime.PRECISION_DAY );
	date1_file.setDay ( 1 );
	line = (String)input_lines.elementAt(input_lines.size() - 1);
	tokens = StringUtil.breakStringList(line,",",0);
	date2_file = DateTime.parse ( (String)tokens.elementAt(2) );
	date2_file.setPrecision ( DateTime.PRECISION_DAY );
	date2_file.setDay ( TimeUtil.numDaysInMonth(date2_file.getMonth(),
						date2_file.getYear()) );
	DayTS ts = (DayTS)req_ts;
	ts.setDataType ( ts.getIdentifier().getType() );
	ts.setMissing ( -99999.0 );
	ts.setDataUnits ( "MM" );
	ts.setDataUnitsOriginal ( "MM" );
	// Original dates are what is in the file...
	ts.setDate1Original ( date1_file );
	ts.setDate2Original ( date2_file );
	if ( req_date1 == null ) {
		ts.setDate1 ( new DateTime(date1_file) );
	}
	else {	ts.setDate1 ( new DateTime(req_date1) );
	}
	if ( req_date2 == null ) {
		ts.setDate2 ( new DateTime(date2_file) );
	}
	else {	ts.setDate2 ( new DateTime(req_date2) );
	}

	// Get the description from the catalog file, which should have been
	// read previously.  If it was not (e.g., because running an existing
	// TSTool commands file), assume that it needs to be read now...

	Vector tslist = getCatalogTSList();
	if ( tslist == null ) {
		// Read the catalog file, assuming that it is in the same
		// directory as the time series file...
		File f = new File ( filename );
		readCatalogFile ( f.getParent() +File.separator+"CATALOGO.TXT");
	}
	// Now search the catalog to find a matching station, so the description
	// can be found...
	int size = 0;
	if ( tslist != null ) {
		size = tslist.size();
	}
	TS cts;
	for ( int i = 0; i < size; i++ ) {
		cts = (TS)tslist.elementAt(i);
		if ( cts.getLocation().equalsIgnoreCase(location) ) {
			ts.setDescription ( cts.getDescription() );
			break;
		}
	}

	// Always read the header.  Optional is whether the data are read...

	if ( !read_data ) {
		return ts;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Processing header..." );
	}

	// Now transfer the data...

	ts.allocateDataSpace();
	size = input_lines.size();
	DateTime idate = null;
	int iday, ndays, ntokens;
	try {
	for ( int i = 0; i < size; i++ ) {
		string = (String)input_lines.elementAt(i);
		if ( string == null ) {
			continue;
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Processing: \"" + string + "\"" );
		}

		// Have to parse every line because free format...

		tokens = StringUtil.breakStringList ( string, ",", 0 );
		ntokens = 0;
		if ( tokens != null ) {
			ntokens = tokens.size();
		}

		// This will be to monthly precision...

		idate = DateTime.parse((String)tokens.elementAt(2));
		idate.setPrecision ( DateTime.PRECISION_DAY );
		idate.setDay ( 1 );
		// For now don't check to see if in the requested period - can
		// add later to improve performance.
		ndays = TimeUtil.numDaysInMonth ( idate.getMonth(),
			idate.getYear() );
		if ( ntokens < (ndays + 3) ) {
			// Not enough tokens...
			continue;
		}
		for ( iday = 1; iday <= ndays; iday++, idate.addDay(1) ) {
			ts.setDataValue ( idate, StringUtil.atod(
					(String)tokens.elementAt(iday + 2)) );
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Value found at " +
				idate.toString() + ": " +
				(String)tokens.elementAt(iday + 2) );
			}
		}
	}
	} catch ( Exception e ) {
		Message.printWarning ( 2, routine,
		"Error processing line \"" + string + "\"");
		ts = null;
	}
	
	routine = null;
	string = null;
	date1_file = null;
	date2_file = null;
	location = null;
	tokens = null;
	idate = null;
	return ts;
}

} // End MexicoCsmnTS class
