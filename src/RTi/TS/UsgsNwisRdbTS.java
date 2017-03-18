package RTi.TS;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

// TODO SAM 2012-02-27 Need to enable other than daily format
/**
The UsgsNwisRdbTS class reads and writes Unites States Geological Survey National
Water Information System (NWIS) Rdb format time series files.
The current code focuses on the daily surface water format.
*/
public class UsgsNwisRdbTS
{

/**
Private method to create a time series given the proper heading information
@param req_ts If non-null, an existing time series is expected to be passed in.
@param agency_cd The agency code from the data.
@param site_no The site number from the data.
@param datatype Data type for the data.
@exception if an error occurs.
*/
private static TS createTimeSeries ( TS req_ts, TimeInterval interval,
	String agency_cd, String site_no, String datatype, String description, String units,
	DateTime date1, DateTime date2, DateTime date1_file, DateTime date2_file,
	List<String> dataFlagList, List<String> dataFlagDescList )
throws Exception
{	String routine = "UsgsNwisRdbTS.createTimeSeries";

	// Declare the time series of the proper type based on the interval.
	// Use a TSIdent to parse out the interval information...

	TSIdent ident = null;
	try {
	    ident = new TSIdent ( site_no, agency_cd, datatype, interval.toString(),"");
	}
	catch ( Exception e ) {
		// This should not happen because all fields are OK...
	}

	TS ts = null;

	// Set the time series pointer to either the requested time series or a newly-created time series.
	if ( req_ts != null ) {
		ts = req_ts;
		// Identifier is assumed to have been set previously.
	}
	else {
	    try {
	        ts = TSUtil.newTimeSeries ( ident.toString(), true );
		}
		catch ( Exception e ) {
			ts = null;
		}
	}
	if ( ts == null ) {
		Message.printWarning ( 3, routine, "Unable to create new time series for \"" + ident.toString() + "\"" );
		return (TS)null;
	}

	// Only set the identifier if a new time series.  Otherwise assume the
	// the existing identifier is to be used (e.g., from a file name).
	if ( req_ts == null ) {
		ts.setIdentifier ( ident.toString() );
	}

	// Set the parameters from the input and override with the parameters...

	ts.setDataType ( datatype );
	ts.setDataUnits ( units );
	ts.setDescription ( description );
	ts.setDataUnitsOriginal ( units );
	// Original dates are what is in the file...
	ts.setDate1Original ( date1_file );
	ts.setDate2Original ( date2_file );
	ts.setDate1 ( new DateTime(date1) );
	ts.setDate2 ( new DateTime(date2) );
	
	// Set the data flags
	int i = -1;
	for ( String dataFlag : dataFlagList ) {
		++i;
		ts.addDataFlagMetadata(new TSDataFlagMetadata(dataFlag, dataFlagDescList.get(i)));
	}
	
	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, routine, "Period to read is " + date1 + " to " + date2 );
		Message.printDebug ( 10, routine, "Read TS header" );
	}
	return ts;
}

/**
Return a sample so that a user/developer knows what a file looks like.  Right
now, the samples are compiled into the code to make absolutely sure that the
programmer knows what sample is supported.
@return Sample file contents.
*/
public static List<String> getSample ()
{	List<String> s = new ArrayList<String>();
	s.add ( "#" );
	s.add ( "# U.S. Geological Survey" );
	s.add ( "# National Water Information System" );
	s.add ( "# Retrieved: 2002-01-28 13:35:25 EST" );
	s.add ( "#" );
	s.add (	"# This file contains published daily mean streamflow data." );
	s.add ( "#" );
	s.add ( "# This information includes the following fields:" );
	s.add ( "#" );
	s.add ( "#  agency_cd   Agency Code" );
	s.add ( "#  site_no     USGS station number" );
	s.add ( "#  dv_dt       date of daily mean streamflow" );
	s.add (	"#  dv_va       daily mean streamflow value, in cubic-feet per-second");
	s.add (	"#  dv_cd       daily mean streamflow value qualification code" );
	s.add ( "#" );
	s.add ( "# Sites in this file include:" );
	s.add ( "#  USGS 03451500 FRENCH BROAD RIVER AT ASHEVILLE, NC" );
	s.add ( "#" );
	s.add ( "#" );
	s.add ( "agency_cd	site_no	dv_dt	dv_va	dv_cd" );
	s.add ( "5s	15s	10d	12n	3s" );
	s.add ( "USGS	03451500	1895-10-01	740	" );
	s.add ( "USGS	03451500	1895-10-02	740	" );
	s.add ( "USGS	03451500	1895-10-03	740	" );
	s.add ( "..." );
	return s;
}

// TODO SAM 2012-02-27 Need to make this more robust
/**
Determine whether a file is a USGS NWIS Rdb file.  This can be used rather than
checking the source in a time series identifier.  If the file passes any of the
following conditions, it is assumed to be a USGS NWIS file:
<ol>
<li>	A line starts with "# National Water Information System".</li>
</ol>
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
*/
public static boolean isUsgsNwisRdbFile ( String filename )
{	BufferedReader in = null;
	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	try {
	    in = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream ( full_fname )) );
		// Read lines and check for common strings that indicate a DateValue file.
		String string = null;
		boolean	is_usgsnwis = false;
		while( (string = in.readLine()) != null ) {
			if ( string.regionMatches(true,0,"# National Water Information System",0,35) ) {
				is_usgsnwis = true;
				break;
			}
			else if ( (string.length() > 0) && (string.charAt(0) != '#') ) {
				break;
			}
		}
		in.close();
		in = null;
		string = null;
		return is_usgsnwis;
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Read the time series from a file.
@param filename Name of file to read.
@return TS for data in the file or null if there is an error reading the time series.
*/
public static TS readTimeSeries ( String filename )
{	return readTimeSeries ( filename, null, null, null, null, "", null, true );
}

/**
Read a time series from a USGS NWIS format file.  Currently only daily surface
water files are recognized.  The resulting time series will have an identifier
like STATIONID.USGS.Streamflow.1Day.
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
@return a pointer to a newly-allocated time series if successful, a NULL pointer if not.
@param filename Name of file to read.
@param date1 Starting date to initialize period (NULL to read the entire time series).
@param date2 Ending date to initialize period (NULL to read the entire time series).
@param interval data interval for output time series (because not explicitly stated in file)
@param units units to assign to time series (because not explicitly stated in file)
@param outputUnits Units to convert to.
@param readData Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String filename, DateTime date1, DateTime date2,
	String dataType, TimeInterval interval, String units, String outputUnits, boolean readData )
{	TS ts = null;

	String fullFname = IOUtil.getPathUsingWorkingDir ( filename );
	BufferedReader in = null;
	try {
	    in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( fullFname )) );
		// Don't have a requested time series...
		ts = readTimeSeries ( (TS)null, in, fullFname, date1, date2, dataType, interval, units, outputUnits, readData );
		ts.setInputName ( fullFname );
		ts.getIdentifier().setInputType("UsgsNwisRdb");
		ts.getIdentifier().setInputName(fullFname);
		ts.addToGenesis ( "Read data from \"" + fullFname +
			"\" for period " + ts.getDate1() + " to " +	ts.getDate2() );
	}
	catch ( Exception e ) {
		String routine = "UsgsNwisRdbTS.readTimeSeries";
		Message.printWarning( 2, routine, "Error reading USGS RDB file \"" + fullFname + "\" (" + e + ")." );
		Message.printWarning(3, routine, e);
	}
	finally {
	    if ( in != null ) {
	        try {
	            in.close();
	        }
	        catch ( Exception e ) {
	            // Should not happen
	        }
	    }
	}
	return ts;
}

/**
Read a time series from a USGS NWIS format file.  The TSID string is specified
in addition to the path to the file.  It is expected that a TSID in the file
matches the TSID (and the path to the file, if included in the TSID would not
properly allow the TSID to be specified).  This method can be used with newer
code where the I/O path is separate from the TSID that is used to identify the time series.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated time series if successful, a NULL pointer if not.
@param tsident_string The full identifier for the time series to
read (where the scenario is NOT the file name).
@param filename The name of a file to read
(in which case the tsident_string must match one of the TSID strings in the file).
@param date1 Starting date to initialize period (NULL to read the entire time series).
@param date2 Ending date to initialize period (NULL to read the entire time series).
@param outputUnits Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String tsident_string, String filename,
	DateTime date1, DateTime date2, String outputUnits, boolean read_data )
throws Exception
{	TS ts = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	if ( !IOUtil.fileReadable(full_fname) ) {
		Message.printWarning( 2, "UsgsNwisRdbTS.readTimeSeries", "File is not readable: \"" + filename + "\"" );
		return ts;
	}
	BufferedReader in = null;
	try {
	    in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
       if ( in != null ) {
           in.close();
        }
		Message.printWarning( 2, "UsgsNwisRdbTS.readTimeSeries", "Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	// Call the fully-loaded method...
	// Pass the file pointer and an empty time series, which
	// will be used to locate the time series in the file.
	ts = TSUtil.newTimeSeries ( tsident_string, true );
	if ( ts == null ) {
		Message.printWarning( 2, "UsgsNwisRdbTS.readTimeSeries",
		"Unable to create time series for \"" + tsident_string + "\"" );
		if ( in != null ) {
		    in.close();
		}
		return ts;
	}
	ts.setIdentifier ( tsident_string );
	TimeInterval interval = TimeInterval.parseInterval(ts.getIdentifier().getInterval());
	readTimeSeries ( ts, in, full_fname, date1, date2, ts.getIdentifier().getType(), interval, "", outputUnits, read_data );
	ts.setInputName ( full_fname );
	ts.getIdentifier().setInputType ( "UsgsNwisRdb" );
	ts.getIdentifier().setInputName ( filename );
	ts.addToGenesis ( "Read data from \"" + full_fname + "\" for period " + ts.getDate1() + " to " + ts.getDate2() );
	if ( in != null ) {
	    in.close();
	}
	return ts;
}

/**
Read a time series from a USGS NWIS format file.
@return a pointer to time series if successful, null if not.  The calling code
is responsible for freeing the memory for the time series.
@param req_ts Pointer to time series to fill.  If null,
return a new time series.  All data are reset, except for the identifier, which
is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param filename Name of file that is being read.  This is needed because the
file is reopened to get the last dates in the file.
@param reqDate1 Requested starting date to initialize period (or NULL to read the entire time series).
@param reqDate2 Requested ending date to initialize period (or NULL to read the entire time series).
@param reqUnits Units to convert to (currently ignored).
@param readData Indicates whether data should be read (false=no, true=yes).
@exception Exception if there is an error reading the time series.
*/
public static TS readTimeSeries ( TS req_ts, BufferedReader in,	String filename,
	DateTime reqDate1, DateTime reqDate2,
	String dataType, TimeInterval interval, String units,
	String reqUnits, boolean readData )
throws Exception
{	String routine = "UsgsNwisRdbTS.readTimeSeries";
	String string = null;
	int	dl = 10;
	DateTime date1File = null, date2File = null;

	// USGS files do not have header information with the period for the time series.  Therefore,
	// grab a reasonable amount of the end of the file using a RandomAccessFile - then process lines
	// (broken by line breaks) until the last data line is encountered.  Do this first to get the end date
	// and then read from the top of the file using the BufferedReader that was passed into this method.
	// The files always contain the first 3 columns:
	// agency_cd site_no datetime

	if ( Message.isDebugOn ) {
	    Message.printDebug( dl, routine, "Getting end date from end of file..." );
	}
	RandomAccessFile ra = new RandomAccessFile ( filename, "r" );
	long length = ra.length();
	// Skip to 5000 bytes from the end.  This should get some actual data
	// lines.  Save in a temporary array in memory.
	if ( length >= 5000 ) {
		ra.seek ( length - 5000 );
	} // Otherwise just read from the top of the file to get all content
	byte[] b = new byte[5000];
	ra.read ( b );
	ra.close();
	ra = null;
	// Now break the bytes into records...
	String bs = new String ( b );
	List<String> v = StringUtil.breakStringList ( bs, "\n\r", StringUtil.DELIM_SKIP_BLANKS );
	// Loop through and figure out the last date.  Start at the second
	// record because it is likely that a complete record was not found in the first record.
	int size = v.size();
	String date2String = null;
	List<String> tokens = null;
	for ( int i = 1; i < size; i++ ) {
		string = v.get(i).trim();
		if ( (string.length() == 0) || (string.charAt(0) == '#') || (string.charAt(0) == '<') ) {
		    // Ignore blank lines, comments, and HTML-enclosing tags.
			continue;
		}
		//Message.printStatus(2, routine, "Processing \"" + string + "\"");
		tokens = StringUtil.breakStringList( string, "\t", StringUtil.DELIM_SKIP_BLANKS );
		// Set the date string - overwrite for each line until all the end lines are processed
		date2String = tokens.get(2);
		if ( Message.isDebugOn ) {
		    Message.printDebug( 2, routine, "Got end date \"" + date2String + "\" from line \"" + string + "\"" );
		}
	}
	date2File = DateTime.parse ( date2String );

	// Always read the header.  Optional is whether the data are read...

	int lineCount = 0;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Processing header..." );
	}

	String description = "";
	boolean	header1Found = false, header2Found = false;
	DateTime date1 = null, date2 = null;
	List<String> header1Tokens = new ArrayList<String>();
	List<String> dataFlagList = new ArrayList<String>(); // Flag for values
	List<String> dataFlagDescList = new ArrayList<String>();
	List<String> ddList = new ArrayList<String>(); // data provided at site
	List<String> ddParameterList = new ArrayList<String>();
	List<String> ddDescList = new ArrayList<String>();
	try {
    	while ( true ) {
    		string = in.readLine();
    		if ( string == null ) {
    		    // End of file.
    			break;
    		}
    		++lineCount;
    		// Although it appears that the file is supposed to be be fixed
    		// format, the column widths also seem to use tabs to make up some of the width...
    		if ( Message.isDebugOn ) {
    			Message.printDebug ( dl, routine, "Processing: \"" + string + "\"" );
    		}
    		string = string.trim();
    		if ( (string.length() == 0) || (string.charAt(0) == '#') || (string.charAt(0) == '<') ) {
    			// Skip comments, blank lines, and HTML lines that start with <...
    			if ( header1Found ) {
    				continue;
    			}
    		}
    		tokens = StringUtil.breakStringList( string, " \t",	StringUtil.DELIM_SKIP_BLANKS );
    
    		// Try to optimize the code by not processing tokens unless we
    		// need to.  Will probably need to add more error handling later.
    			
    		if ( !header1Found ) {
    			if ( !string.startsWith("#") && (string.indexOf("agency_cd") >= 0) ) {
           			// NWIS Header Format: Line 1
    				//
    				// agency_cd site_no dv_dt dv_va dv_cd
    				// or for real-time:  agency_cd	site_no	datetime	tz_cd	01_00060	01_00060_cd
    				// or for daily:  agency_cd       site_no datetime        02_00060_00001  02_00060_00001_cd       02_00060_00002  02_00060_00002_cd
    				header1Found = true;
    				header1Tokens = tokens;
    			}
    			else if ( string.startsWith("#") && string.indexOf("DD parameter") > 0 ) {
    				// List of time series in file, of the following form for real-time:
    				//# Data provided for site 09512500
    				//#    DD parameter   Description
    				//#    01   00060     Discharge, cubic feet per second
    				//
    				// And the following for daily:
    				//# Data provided for site 03451500
    				//#    DD parameter statistic   Description
    				//#    02   00060     00001     Discharge, cubic feet per second (Maximum)
    				//#    02   00060     00002     Discharge, cubic feet per second (Minimum)
    				//#    02   00060     00003     Discharge, cubic feet per second (Mean)
    				//#    03   00065     00001     Gage height, feet (Maximum)
    				//#    03   00065     00002     Gage height, feet (Minimum)
    				//#    03   00065     00003     Gage height, feet (Mean)
    				//#    17   00045     00006     Precipitation, total, inches (Sum)

    				while ( true ) {
    					string = in.readLine();
    		    		if ( (string == null) || string.trim().equals("#") || !string.startsWith("#") ) {
    		    		    // End of file or no more data flags (empty line).
    		    			break;
    		    		}
    		    		else {
    		    			// Have data flag line to process - parse carefully because there will be spaces in description.
    		    			String dataString = string.substring(1).trim();
    		    			int iSpace = dataString.indexOf(" ");
    		    			if ( iSpace < 0 ) {
    		    				continue;
    		    			}
    		    			ddList.add(dataString.substring(0,iSpace).trim());
    		    			// Now parse out parameter and description
    		    			dataString = dataString.substring(iSpace).trim();
    		    			iSpace = dataString.indexOf(" ");
    		    			if ( iSpace < 0 ) {
    		    				continue;
    		    			}
    		    			ddParameterList.add(dataString.substring(0,iSpace).trim());
    		    			ddDescList.add(dataString.substring(iSpace).trim());
    		    		}
    				}
    				if ( string == null ) {
    					break; // End of file
    				}
    			}
    			else if ( string.startsWith("#") && string.indexOf("Data-value qualification code") > 0 ) {
    				// Flags of form:
    				//   # Data-value qualification codes included in this output: 
    				//	 #     A  Approved for publication -- Processing and review completed.  
    				//	 #     P  Provisional data subject to revision.  
    				//	 #     e  Value has been estimated.
    				while ( true ) {
    					string = in.readLine();
    		    		if ( (string == null) || string.trim().equals("#") || !string.startsWith("#") ) {
    		    		    // End of file or no more data flags (empty line).
    		    			break;
    		    		}
    		    		else {
    		    			// Have data flag line to process - parse carefully because there will be spaces in description.
    		    			String dataString = string.substring(1).trim();
    		    			int iSpace = dataString.indexOf(" ");
    		    			if ( iSpace < 0 ) {
    		    				continue;
    		    			}
    		    			dataFlagList.add(dataString.substring(0,iSpace).trim());
    		    			dataFlagDescList.add(dataString.substring(iSpace).trim());
    		    		}
    				}
    				if ( string == null ) {
    					break; // End of file
    				}
    			}
    			// Legacy below this (have seen in old files)
    			else if ( string.regionMatches(true,0,"# Sites in this file include:",0,29) ) {
    				// Get the description from the next line...
    				description = in.readLine().trim();
    				// Remove leading #...
    				if ( description.length() > 3 ) {
    					description = description.substring(3);
    				}
    				++lineCount;
    			}
    		}
    		else if ( header1Found && !header2Found ) {
    			// Header 2 gives the column widths
    			// For now read as free format so don't need the column format inforamtion.
    			header2Found = true;
    			// Break out and read data below...
    			break;
    		}	
    	}
	} catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error processing line " + lineCount + ": \"" + string + "\"");
		Message.printWarning ( 3, routine, e );
	}

	// Now read the data.

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Reading data..." );
	}

	DateTime idate = null;
	TS ts = null;
	// Only read the first time series.
	// Figure out the token for data and flag
	int valueCol = -1;
	int flagCol = -1;
	int tzCol = -1;
	String timeZone = "";
	String flag = null;
	String valueString;
	double value;
	String dataTypeRdb = null; // dd_parameter_statistic for default datatype
	for ( int i = 0; i < header1Tokens.size(); i++ ) {
		String headerCol = header1Tokens.get(i);
		if ( headerCol.equals("tz_cd") ) {
			// Time zone
			tzCol = i;
		}
		if ( dataTypeRdb == null ) {
			if ( headerCol.equals("dv_va")) { // Legacy
				valueCol = i;
				dataTypeRdb = headerCol;
			}
			else if ( Character.isDigit(headerCol.charAt(0)) && !headerCol.endsWith("_cd") ) { // Current
				// Something like 02_00060_0003
				valueCol = i;
				dataTypeRdb = headerCol;
			}
		}
		else if ( headerCol.equals(dataTypeRdb + "_cd") || // Current
			headerCol.equals("dv_cd") ) { // Legacy
			// Flag column
			flagCol = i;
		}
	}
	
	if ( (dataType == null) || dataType.isEmpty() ) {
		// No data type specified as input so default to RDB data
		dataType = dataTypeRdb;
	}

	try {
    	int dataCount = 0;
    	while ( (string = in.readLine()) != null ) {
    		++lineCount;
    		// Don't trim the line because data are fixed-format.
    		if ( Message.isDebugOn ) {
    			Message.printDebug ( dl, routine, "Processing: \"" + string + "\"" );
    		}
    		string = string.trim();
    		if ( (string.length() == 0)
    			|| (string.charAt(0) == '#') // comment
    			|| (string.charAt(0) == '<') ) { // html
    			// Skip comments and blank lines for now...
    			continue;
    		}
    		if ( Message.isDebugOn ) {
    			Message.printDebug ( dl, routine, "Processing data string: \"" + string + "\"" );
    		}
    
    		// Have to parse every line because free format...
    		// Do not skip blanks because missing may be not included
    		tokens = StringUtil.breakStringList ( string, "\t", 0 );
    		size = 0;
    		if ( tokens != null ) {
    			size = tokens.size();
    		}
    
    		if ( dataCount == 0 ) {
    			// Need to interpret information from the first line to allocate the time series.
    		    // Some things are hard-coded for now until we get a specification...
    			// Now set dates to read...
    			if ( tzCol >= 0 ) {
    				timeZone = tokens.get(tzCol);
    			}
    			date1File = DateTime.parse(tokens.get(2) );
    			if ( (timeZone != null) && !timeZone.isEmpty() ) {
    				date1File.setTimeZone(timeZone);
    			}

    			if ( reqDate1 != null ) {
    				date1 = reqDate1;
    			}
    			else {
    			    date1 = date1File;
    			}
    			if ( reqDate2 != null ) {		
    				date2 = reqDate2;
    			}
    			else {
    			    date2 = date2File;
        			if ( (timeZone != null) && !timeZone.isEmpty() ) {
        				date2.setTimeZone(timeZone);
        			}
    			}
    
    			ts = createTimeSeries ( req_ts,
    				interval,
    				tokens.get(0), // Agency
    				tokens.get(1), // station ID
    				dataType, description, units,
    				date1, date2, date1File, date2File,
    				dataFlagList, dataFlagDescList );
    
    			if ( !readData ) {
    				// Don't need to allocate the data space...
    				break;
    			}
    
    			// Allocate the memory for the data array...
    
    			if ( ts.allocateDataSpace() == 1 ) {
    				Message.printWarning( 2, routine, "Error allocating data space..." );
    				// Clean up memory...
    				break;
    			}
    		}
    
    		// Increment to prevent above allocation from occurring more than once...
    
    		++dataCount;
    
    		// The rest of this is for data.
    
    		if ( size < 3 ) {
    			// Can't continue because not enough fields.
    			// Allow the agency, ID, and date (but no data).
    			Message.printWarning ( 3, routine,
    				"Error reading data at line " + lineCount + ".  File is corrupt." );
    			ts = null;
    			break;
    		}
    		// Always parse to make absolutely sure the data go into the correct place.
    		// This is slower but safe (there are cases where there are gaps in the USGS files)...
    		idate = DateTime.parse ( tokens.get(2) );
			if ( (timeZone != null) && !timeZone.isEmpty() ) {
				idate.setTimeZone(timeZone);
			}
    		//Message.printStatus(2, routine, (String)tokens.elementAt(2) );
    		if ( (reqDate1 == null) ||	idate.greaterThanOrEqualTo(date1)) {
    			// In the requested period so set the data...
    			flag = null;
    			if ( (flagCol > 0) && (flagCol < size) ) {
    				flag = tokens.get(flagCol);
    			}
    			if ( (valueCol > 0) && (valueCol < size) ) {
    				// Have a data value (sometimes do not!)...
    				valueString = tokens.get(valueCol);
    				if ( !valueString.isEmpty() ) {
    					value = Double.parseDouble(valueString);
    				}
    				else {
    					value = ts.getMissing();
    				}
					if ( (flag == null) || flag.isEmpty() ) {
						ts.setDataValue ( idate, value );
					}
					else {
						ts.setDataValue ( idate, value, flag, 0 );
					}
    				if ( Message.isDebugOn ) {
    					Message.printDebug ( dl, routine,
    					"Value found at " + idate.toString() + ": "+ StringUtil.atod(tokens.get(3)));
    				}
    			}
    			if ( idate.greaterThan(date2) ) {
    			    if ( Message.isDebugOn ) {
    					Message.printDebug ( dl, routine, "Finished reading data at: " + idate.toString() );
    				}
    				// Will return below...
    				break;
    			}
    		}
    	}
	} catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error processing line " + lineCount + ": \"" + string + "\"");
		ts = null;
	}
	return ts;
}

/**
Write a time series to the open PrintWriter.
@param ts Time series to write.
@param fp PrintWrite to write to.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, PrintWriter fp )
throws IOException
{	// Call the method that takes multiple time seres...
	writeTimeSeries ( ts, fp, (DateTime)null, (DateTime)null, "", true );
}

/**
Write a time series to a USGS NWIS format file.  The entire period is written.
@param ts Single time series to write.
@param fname Name of file to write.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, String fname )
throws IOException
{	PrintWriter	out = null;

	try {
	    out = new PrintWriter (new FileWriter(fname));
	}
	catch ( Exception e ) {
		String message = "Error opening \"" + fname + "\" for writing.";
		Message.printWarning ( 2, "UsgsNwisRdbTS.writePersistent(TS,String)", message );
		out = null;
		throw new IOException ( message );
	}
	try {
	    writeTimeSeries ( ts, out );
	}
    catch ( IOException e ) {
        // Just rethrow but handle graceful file close in finally below
        throw e;
    }
    finally {
        out.close();
    }
}

/**
Write a time series to a USGS NWIS format file using a default format based on the time series units.
@param ts Vector of pointers to time series to write.
@param fname Name of file to write.
@param req_date1 First date to write (if NULL write the entire time series).
@param req_date2 Last date to write (if NULL write the entire time series).
@param req_units Units to write.  If different than the current units the units will be converted on output.
@param write_data Indicates whether data should be written (as opposed to only
writing the header) (<b>currently not used</b>).
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, String fname, DateTime req_date1, DateTime req_date2,
					String req_units, boolean write_data )
throws IOException
{	PrintWriter	out = null;

	try {
	    out = new PrintWriter (new FileWriter(fname));
	}
	catch ( Exception e ) {
		String message = "Error opening \"" + fname + "\" for writing.";
		Message.printWarning ( 2, "UsgsNwisRdbTS.writeTimeSeries",message);
		out = null;
		throw new IOException ( message );
	}
	try {
	    writeTimeSeries ( ts, out, req_date1, req_date2, req_units, write_data);
	}
	catch ( IOException e ) {
	    // Just rethrow but handle graceful file close in finally below
	    throw e;
	}
	finally {
	    out.close();
	}
}

/**
Write a time series to a USGS NWIS format file using a default format based on the data units.
@param ts Time series to write.
@param fp PrintWriter to write to.
@param req_date1 First date to write (if NULL write the entire time series).
@param req_date2 Last date to write (if NULL write the entire time series).
@param req_units Units to write.  If different than the current units the units
will be converted on output.
@param write_data Indicates whether data should be written.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, PrintWriter fp, DateTime req_date1, DateTime req_date2,
					String req_units, boolean write_data )
throws IOException
{	String	message, routine="UsgsNwisRdbTS.writePersistent";

	if ( ts == null ) {
		message = "Time series is NULL, cannot continue.";
		Message.printWarning( 2, routine, message );
		throw new IOException ( message );
	}

	if ( fp == null ) {
		message = "Output stream is NULL, cannot continue.";
		Message.printWarning( 2, routine, message );
		throw new IOException ( message );
	}

	// Get the interval information.  This is used primarily for iteration.
	// The input time series must be hourly or 1Day.

	int data_interval_base = ts.getDataIntervalBase();
	int data_interval_mult = ts.getDataIntervalMult();

	if ( !((data_interval_base == TimeInterval.DAY) && (data_interval_mult == 1)) ) {
		message = "Currently, only 1Day time series can be saved as USGS NWIS";
		Message.printWarning( 2, routine, message );
		throw new IOException ( message );
	}

	// Write header...

	fp.println ( "#" );
	fp.println ( "# THIS FILE WAS WRITTEN BY THE PROGRAM INDICATED BELOW");
	fp.println ( "# (NOT THE USGS).  THE USGS FILE FORMAT HAS BEEN MAINTAINED FOR USE");
	fp.println ( "# BY OTHER SOFTWARE");
	fp.println ( "#" );
	IOUtil.printCreatorHeader ( fp, "#", 80, 0 );
	fp.println ( "#" );
	fp.println ( "# U.S. Geological Survey" );
	fp.println ( "# National Water Information System" );
	DateTime d = new DateTime ( DateTime.DATE_CURRENT );
	fp.println ( "# Retrieved: " + d.toString() );
	fp.println ( "#" );
	fp.println ( "# This file contains published daily mean streamflow data." );
	fp.println ( "#" );
	fp.println ( "# This information includes the following fields:" );
	fp.println ( "#" );
	fp.println ( "#  agency_cd   Agency Code" );
	fp.println ( "#  site_no     USGS station number" );
	fp.println ( "#  dv_dt       date of daily mean streamflow" );
	fp.println ( "#  dv_va       daily mean streamflow value, in cubic-feet per-second");
	fp.println ( "#  dv_cd       daily mean streamflow value qualification code" );
	fp.println ( "#" );
	fp.println ( "# Sites in this file include:" );
	fp.println ( "#  " + ts.getDescription() );
	fp.println ( "#" );
	fp.println ( "#" );
	fp.println ( "agency_cd	site_no	dv_dt	dv_va	dv_cd" );
	fp.println ( "5s	15s	10d	12n	3s" );

	// Set dates to write...
	DateTime	date1 = null, date2 = null;
	if ( req_date1 != null ) {
		date1 = req_date1;
	}
	else {
	    date1 = ts.getDate1();
	}
	if ( req_date2 != null ) {		
		date2 = req_date2;
	}
	else {
	    date2 = ts.getDate2();
	}

	// What format to use for data??
	DateTime date = new DateTime ( date1 );
	TSData tsdata = new TSData();
	String source = ts.getIdentifier().getSource();
	String id = ts.getLocation();
	for ( ; date.lessThanOrEqualTo(date2);
		date.addInterval(data_interval_base,data_interval_mult) ) {
		tsdata = ts.getDataPoint ( date, tsdata );
		fp.println ( source + "\t" + id + "\t" + date.toString(DateTime.FORMAT_YYYY_MM_DD) + "\t" +
		StringUtil.formatString(tsdata.getDataValue(), "%.0f") + "\t" + tsdata.getDataFlag() );
	}
}

}