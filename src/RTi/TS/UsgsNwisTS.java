// ----------------------------------------------------------------------------
// UsgsNwisTS - class for reading/writing USGS NWIS time series file formats
// ----------------------------------------------------------------------------
// History:
//
// 2002-01-31	Steven A. Malers, RTi	Copy NWSCardTS and update as
//					appropriate.
// 2002-02-11	SAM, RTi		Update to set the input type and name
//					in TSIdent, consistent with new
//					conventions.
// 2002-03-24	SAM, RTi		Add information to the genesis
//					describing where the time series is
//					read and for what period.  Also add
//					ability to handle case when no data
//					value is found for a date.
// 2002-09-05	SAM, RTi		Change so the data flags are not saved
//					after the initial read.  They are used
//					to set missing data.  They are not
//					saved because work needs to be done to
//					figure out how to handle in TSTool -
//					original data flags likely will not
//					apply when data are filled.  It is
//					better to not use the flags than to
//					misrepresent other data.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TS.INTERVAL* to TimeInterval.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
The UsgsNwisTS class reads and writes Unites States Geological Survey National
Water Information System time series.  A number of different file formats are
available.  The current code focuses on the daily surface water format.
*/
public class UsgsNwisTS
{

/**
Private method to create a time series given the proper heading information
@param req_ts If non-null, an existing time series is expected to be passed in.
@param agency_cd The agency code from the data.
@param site_no The site number from the data.
@param datatype Data type for the data.
@exception if an error occurs.
*/
private static TS createTimeSeries (	TS req_ts,
					int data_interval_base,
					int data_interval_mult,
					String agency_cd, String site_no,
					String datatype, String description,
					String units,
					DateTime date1, DateTime date2,
					DateTime date1_file,
					DateTime date2_file )
throws Exception
{	String routine = "UsgsNwisTS.createTimeSeries";

	// Declare the time series of the proper type based on the interval.
	// Use a TSIdent to parse out the interval information...

	TSIdent ident = null;
	try {
	    ident = new TSIdent ( site_no, agency_cd, datatype, "Day","");
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
public static List getSample ()
{	List s = new Vector ( 50 );
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

/**
Determine whether a file is a USGS NWIS file.  This can be used rather than
checking the source in a time series identifier.  If the file passes any of the
following conditions, it is assumed to be a USGS NWIS file:
<ol>
<li>	A line starts with "# National Water Information System".</li>
</ol>
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
*/
public static boolean isUsgsNwisFile ( String filename )
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
{	return readTimeSeries ( filename, null, null, null, true );
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
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String filename, DateTime date1, DateTime date2, String units, boolean read_data )
{	TS	ts = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	BufferedReader in = null;
	try {
	    in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( full_fname )) );
		// Don't have a requested time series...
		ts = readTimeSeries ( (TS)null, in, full_fname, date1, date2, units, read_data );
		ts.setInputName ( full_fname );
		ts.getIdentifier().setInputType("USGSNWIS");
		ts.getIdentifier().setInputName(full_fname);
		ts.addToGenesis ( "Read data from \"" + full_fname +
			"\" for period " + ts.getDate1() + " to " +	ts.getDate2() );
	}
	catch ( Exception e ) {
		Message.printWarning( 2,
		"UsgsNwisTS.readTimeSeries(String,...)", "Unable to open file \"" + full_fname + "\"" );
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
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String tsident_string, String filename,
					DateTime date1, DateTime date2, String units, boolean read_data )
throws Exception
{	TS	ts = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	if ( !IOUtil.fileReadable(full_fname) ) {
		Message.printWarning( 2, "UsgsNwisTS.readTimeSeries", "File is not readable: \"" + filename + "\"" );
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
		Message.printWarning( 2, "UsgsNwisTS.readTimeSeries", "Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	// Call the fully-loaded method...
	// Pass the file pointer and an empty time series, which
	// will be used to locate the time series in the file.
	ts = TSUtil.newTimeSeries ( tsident_string, true );
	if ( ts == null ) {
		Message.printWarning( 2, "UsgsNwisTS.readTimeSeries",
		"Unable to create time series for \"" + tsident_string + "\"" );
		if ( in != null ) {
		    in.close();
		}
		return ts;
	}
	ts.setIdentifier ( tsident_string );
	readTimeSeries ( ts, in, full_fname, date1, date2, units, read_data );
	ts.setInputName ( full_fname );
	ts.getIdentifier().setInputType ( "USGSNWIS" );
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
@param req_date1 Requested starting date to initialize period (or NULL to read the entire time series).
@param req_date2 Requested ending date to initialize period (or NULL to read the entire time series).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception Exception if there is an error reading the time series.
*/
public static TS readTimeSeries ( TS req_ts, BufferedReader in,	String filename,
					DateTime req_date1, DateTime req_date2,
					String req_units, boolean read_data )
throws Exception
{	String routine = "UsgsNwisTS.readTimeSeries";
	String string = null;
	int	dl = 10;
	DateTime date1_file = null, date2_file = null;

	// USGS files do not have header information with the period for the time series.  Therefore,
	// grab a reasonable amount of the end of the file using a RandomAccessFile - then process lines
	// (broken by line breaks) until the last data line is encountered.  Do this first to get the end date
	// and then read from the top of the file using the BufferedReader that was passed into this method.

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
	List v = StringUtil.breakStringList ( bs, "\n\r", StringUtil.DELIM_SKIP_BLANKS );
	// Loop through and figure out the last date.  Start at the second
	// record because it is likely that a complete record was not found in the first record.
	int size = v.size();
	String date2_string = null;
	List tokens = null;
	for ( int i = 1; i < size; i++ ) {
		string = ((String)v.get(i)).trim();
		if ( (string.length() == 0) || (string.charAt(0) == '#') || (string.charAt(0) == '<') ) {
		    // Ignore blank lines, comments, and HTML-enclosing tags.
			continue;
		}
		tokens = StringUtil.breakStringList( string, " \t", StringUtil.DELIM_SKIP_BLANKS );
		// Set the date string - overwrite for each line until all the end lines are processed
		date2_string = (String)tokens.get(2);
		if ( Message.isDebugOn ) {
		    Message.printDebug( 2, routine, "Got end date \"" + date2_string + "\" from line \"" + string + "\"" );
		}
	}
	v = null;
	bs = null;
	date2_file = DateTime.parse ( date2_string );

	// Always read the header.  Optional is whether the data are read...

	int line_count = 0;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Processing header..." );
	}

	String datatype = "", description = "", units = "";
	String token0;
	boolean	header1_found = false, header2_found = false;
	DateTime date1 = null, date2 = null;
	try {
    	while ( true ) {
    		string = in.readLine();
    		if ( string == null ) {
    		    // End of file.
    			break;
    		}
    		++line_count;
    		// Although it appears that the file is supposed to be be fixed
    		// format, the column widths also seem to use tabs to make up some of the width...
    		if ( Message.isDebugOn ) {
    			Message.printDebug ( dl, routine, "Processing: \"" + string + "\"" );
    		}
    		string = string.trim();
    		if ( (string.length() == 0) || (string.charAt(0) == '#') || (string.charAt(0) == '<') ) {
    			// Skip comments, blank lines, and HTML lines that start with <...
    			if ( header1_found ) {
    				continue;
    			}
    		}
    		tokens = StringUtil.breakStringList( string, " \t",	StringUtil.DELIM_SKIP_BLANKS );
    
    		// Try to optimize the code by not processing tokens unless we
    		// need to.  Will probably need to add more error handling later.
    			
    		if ( !header1_found ) {
    			token0 = (String)tokens.get(0);
    			if ( token0.equalsIgnoreCase("agency_cd") ) {
           			// NWIS Header Format: Line 1
    				//
    				// agency_cd site_no dv_dt dv_va dv_cd
    				header1_found = true;
    			}
    			else if ( string.regionMatches(true,0,"# Sites in this file include:",0,29) ) {
    				// Get the description from the next line...
    				description = in.readLine().trim();
    				// Remove leading #...
    				if ( description.length() > 3 ) {
    					description = description.substring(3);
    				}
    				++line_count;
    			}
    		}
    		else if ( header1_found && !header2_found ) {
    			// Header 2 gives the column widths
    			// For now read as free format...
    			header2_found = true;
    			// Break out and read data below...
    			break;
    		}	
    	}
	} catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error processing line " + line_count + ": \"" + string + "\"");
		Message.printWarning ( 3, routine, e );
	}

	// Now read the data.

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Reading data..." );
	}

	DateTime idate = null;
	TS ts = null;
	int data_interval_base = TimeInterval.DAY;
	int data_interval_mult = 1;

	try {
    	int data_count = 0;
    	while ( (string = in.readLine()) != null ) {
    		++line_count;
    		// Don't trim the line because data are fixed-format.
    		if ( Message.isDebugOn ) {
    			Message.printDebug ( dl, routine, "Processing: \"" + string + "\"" );
    		}
    		string = string.trim();
    		if ( (string.length() == 0) || (string.charAt(0) == '#') || (string.charAt(0) == '<') ) {
    			// Skip comments and blank lines for now...
    			continue;
    		}
    		if ( Message.isDebugOn ) {
    			Message.printDebug ( dl, routine, "Processing data string: \"" + string + "\"" );
    		}
    
    		// Have to parse every line because free format...
    
    		tokens = StringUtil.breakStringList ( string, " \t", StringUtil.DELIM_SKIP_BLANKS );
    		size = 0;
    		if ( tokens != null ) {
    			size = tokens.size();
    		}
    
    		if ( data_count == 0 ) {
    			// Need to interpret information from the first line to allocate the time series.
    		    // Some things are hard-coded for now until we get a specification...
    			units = "CFS";
    			datatype = "Streamflow";
    			// Now set dates to read...
    			date1_file =DateTime.parse((String)tokens.get(2));
    			if ( req_date1 != null ) {
    				date1 = req_date1;
    			}
    			else {
    			    date1 = date1_file;
    			}
    			if ( req_date2 != null ) {		
    				date2 = req_date2;
    			}
    			else {
    			    date2 = date2_file;
    			}
    
    			data_interval_base = TimeInterval.DAY;
    			data_interval_mult = 1;
    			ts = createTimeSeries ( req_ts,
    				data_interval_base, data_interval_mult,
    				(String)tokens.get(0), (String)tokens.get(1),
    				datatype, description, units,
    				date1, date2, date1_file, date2_file );
    
    			if ( !read_data ) {
    				// Don't need to allocate the data space...
    				break;
    			}
    
    			// Allocate the memory for the data array...
    
    			if ( ts.allocateDataSpace() == 1 ) {
    				Message.printWarning( 2, routine, "Error allocating data space..." );
    				// Clean up memory...
    				break;
    			}
    
    			// Turn on data flags (use one-character flag until more is known about the format)...
    			// TODO 2002-09-05 Disable for now until we figure out how to handle in TSTool.
    			//ts.hasDataFlags ( true, 1 );
    		}
    
    		// Increment to prevent above allocation from occurring more than once...
    
    		++data_count;
    
    		// The rest of this is for data.
    
    		if ( size < 3 ) {
    			// Can't continue because not enough fields.
    			// Allow the agency, ID, and date (but no data).
    			Message.printWarning ( 3, routine,
    				"Error reading data at line " + line_count + ".  File is corrupt." );
    			ts = null;
    			break;
    		}
    		// Always parse to make absolutely sure the data go into the correct place.
    		// This is slower but safe (there are cases where there are gaps in the USGS files)...
    		idate = DateTime.parse ( (String)tokens.get(2) );
    		//Message.printStatus(2, routine, (String)tokens.elementAt(2) );
    		if ( (req_date1 == null) ||	idate.greaterThanOrEqualTo(date1)) {
    			// In the requested period so set the data...
    			// TODO SAM 2007-05-09 Need to handle quality quality_flag = "";
    			if ( size > 4 ) {
    				//quality_flag = (String)tokens.elementAt(4);
    			}
    			if ( size > 3 ) {
    				// Have a data value (sometimes do not!)...
    				//ts.setDataValue ( idate, StringUtil.atod((String)tokens.elementAt(3)), quality_flag, 0 );
    				// TODO 2002-09-05 Disable quality flag until figure out how to handle consistently in TSTool.
    				ts.setDataValue ( idate, StringUtil.atod((String)tokens.get(3)) );
    				if ( Message.isDebugOn ) {
    					Message.printDebug ( dl, routine,
    					"Value found at " + idate.toString() + ": "+ StringUtil.atod((String)tokens.get(3)));
    				}
    			}
    			if ( (req_date2 == null) || idate.lessThan(date2) ) {
    				idate.addInterval ( data_interval_base, data_interval_mult);
    			}
    			else {
    			    if ( Message.isDebugOn ) {
    					Message.printDebug ( dl, routine, "Finished reading data at: " + idate.toString() );
    				}
    				// Will return below...
    				break;
    			}
    		}
    	}
	} catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error processing line " + line_count + ": \"" + string + "\"");
		ts = null;
	}
	
	routine = null;
	string = null;
	date1_file = null;
	date2_file = null;
	datatype = null;
	description = null;
	units = null;
	tokens = null;
	date1 = null;
	date2 = null;
	idate = null;
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
		Message.printWarning ( 2, "UsgsNwisTS.writePersistent(TS,String)", message );
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
        out = null;
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
		Message.printWarning ( 2, "UsgsNwisTS.writeTimeSeries",message);
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
	    out = null;
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
{	String	message, routine="UsgsNwisTS.writePersistent";

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
		StringUtil.formatString(tsdata.getData(), "%.0f") + "\t" + tsdata.getDataFlag() );
	}
}

}