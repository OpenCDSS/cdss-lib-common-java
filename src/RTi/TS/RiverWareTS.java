// RiverWareTS - read and write RiverWare time series files, including individual time series and RDF files that can contain ensembles.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.TS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.TimeInterval;

// TODO SAM 2013-09-21 Evaluate whether to refactor so static methods are not used (less memory footprint).
/**
The RiverWareTS class reads and writes RiverWare time series files,
including individual time series and RDF files that can contain ensembles.
*/
public class RiverWareTS
{

/**
Private method to create a time series given the proper heading information
@param req_ts If non-null, an existing time series is expected to be passed in.
@param filename name of file being read, assumed to start with ObjectName.SlotName,
which are used to determine the location ID and data type
@param timestep RiverWare timestep as string (essentially the same as TS except
there is a space between the multiplier and base).
@param units Units of the data.
@param date1 Requested start date.
@param date2 Requested end date.
@param date1_file Start date in the file.
@param date2_file End date in the file.
@exception Exception if an error occurs.
*/
private static TS createTimeSeries ( TS req_ts, String filename, String timestep,
	String units, DateTime date1, DateTime date2, DateTime date1_file, DateTime date2_file )
throws Exception {
	String routine = RiverWareTS.class.getSimpleName() + ".createTimeSeries";

	// Declare the time series of the proper type based on the interval.
	// Use a TSIdent to parse out the interval information..

	String timestep2 = StringUtil.unpad(timestep, " ", StringUtil.PAD_FRONT_MIDDLE_BACK );
	String location = "";
	String datatype = "";
	List<String> tokens = StringUtil.breakStringList ( filename, ".", 0 );
	if ( (tokens != null) && (tokens.size() >= 2) ) {
		location = tokens.get(0).trim();
		// Only want the relative part.
		File f = new File ( location );
		location = f.getName();
		datatype = tokens.get(1).trim();
	}
	TSIdent ident = new TSIdent ( location, "", datatype, timestep2, "");

	TS ts = null;

	// Set the time series pointer to either the requested time series or a newly-created time series.
	if ( req_ts != null ) {
		ts = req_ts;
		// Identifier is assumed to have been set previously.
	}
	else {
	    ts = TSUtil.newTimeSeries ( ident.toString(), true );
	}
	if ( ts == null ) {
		Message.printWarning ( 3, routine, "Unable to create new time series for \"" + ident + "\"" );
		return (TS)null;
	}

	// Only set the identifier if a new time series.
	// Otherwise assume the the existing identifier is to be used (e.g., from a file name).
	if ( req_ts == null ) {
		ts.setIdentifier ( ident.toString() );
	}

	// Set the parameters from the input and override with the parameters.

	String description = "";
	if ( req_ts == null ) {
		ts.setLocation ( location );
		description = location + ", " +  datatype;
	}
	ts.setDataUnits ( units );
	ts.setDataType ( datatype );
	ts.setDescription ( description );
	ts.setDataUnitsOriginal ( units );
	// Original dates are what is in the file.
	ts.setDate1Original ( date1_file );
	ts.setDate2Original ( date2_file );
	ts.setDate1 ( new DateTime(date1) );
	ts.setDate2 ( new DateTime(date2) );

	// Set missing to NaN since this is what RiverWare uses.

	ts.setMissing ( Double.NaN );

	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, routine, "Period to read is " + date1 + " to " + date2 );
		Message.printDebug ( 10, routine, "Read TS header" );
	}
	return ts;
}

/**
Determine whether a file is a RiverWare time series file.
This can be used rather than checking the source in a time series identifier.
@param filename name of file to check.  IOUtil.getPathUsingWorkingDir() is called to expand the filename.
@param checkForRdf if true, then if the file passes any of the
following conditions, it is assumed to be a RiverWare single time series file:
<ol>
<li>	A line starts with "START_DATE:".</li>
</ol>
If checkForRdf is false, true is returned if END_PACKAGE_PREAMBLE is found in the file.
*/
public static boolean isRiverWareFile ( String filename, boolean checkForRdf ) {
	BufferedReader in = null;
	String filenameFull = IOUtil.getPathUsingWorkingDir ( filename );
	try {
	    in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( filenameFull )) );
		// Read lines and check for common strings that indicate a RiverWare file.
	    // Search for a maximum number of non-comment lines.
	    int countNotComment = 0;
		String string = null;
		boolean	isRiverWare = false;
		while( (string = in.readLine()) != null ) {
			if ( !checkForRdf && string.regionMatches(true,0, "START_DATE:",0,11) ){
				isRiverWare = true;
				break;
			}
			else if ( checkForRdf && string.regionMatches(true,0, "END_PACKAGE_PREAMBLE",0,20) ){
	            isRiverWare = true;
	            break;
	        }
			else if ( (string.length() > 0) && (string.charAt(0) != '#') ) {
			    // Not a comment
				++countNotComment;
				if ( countNotComment > 100 ) {
				    // Had enough lines to check;
				    break;
				}
			}
		}
		return isRiverWare;
	}
	catch ( Exception e ) {
		return false;
	}
	finally {
	    if ( in != null ) {
	        try {
	            in.close();
	        }
	        catch ( Exception e ) {
	            // Absorb - should not happen.
	        }
	    }
	}
}

/**
Parse a RiverWare date/time string of the form YYYY-MM-DD HH:MM, allowing 1 or 2-digit parts.  Year must be 4 digits.
This is the date format found in RDF files.
@param dt date/time string
@param precision if > 0, the DateTime precision to set for the returned DateTime.
For example, if year interval, then issues of 24-hour RiverWare time can be ignored.
If @return parsed date/time or null
*/
private static DateTime parseRiverWareDateTime(String dt, int precision ) {
    if ( dt == null ) {
        return null;
    }
    dt = dt.trim();
    // First split by space.
    String [] dtParts = dt.split(" ");
    // Split the date.
    String [] dParts = dtParts[0].split("-");
    // Split the date.
    String [] tParts = dtParts[1].split(":");
    DateTime d = new DateTime(precision);
    if ( dParts.length > 0 ) {
        d.setYear(Integer.parseInt(dParts[0]));
    }
    if ( precision == DateTime.PRECISION_YEAR ) {
        return d;
    }
    if ( dParts.length > 1 ) {
        d.setMonth(Integer.parseInt(dParts[1]));
    }
    if ( precision == DateTime.PRECISION_MONTH ) {
        return d;
    }
    if ( dParts.length > 2 ) {
        d.setDay(Integer.parseInt(dParts[2]));
    }
    if ( precision == DateTime.PRECISION_DAY ) {
        return d;
    }
    if ( tParts.length > 0 ) {
        int hour = Integer.parseInt(tParts[0]);
        if ( hour == 24 ) {
            // RiverWare data files have hour 24, which is really hour 0 of the next day.
            d.addDay(1);
            d.setHour(0);
        }
        else {
            d.setHour(hour);
        }
    }
    if ( precision == DateTime.PRECISION_HOUR ) {
        return d;
    }
    if ( tParts.length > 1 ) {
        d.setMinute(Integer.parseInt(tParts[1]));
    }
    return d;
}

/**
Read the time series from a RiverWare file.
@param filename Name of file to read.
@return TS for data in the file or null if there is an error reading the time series.
*/
public static TS readTimeSeries ( String filename ) {
	return readTimeSeries ( filename, null, null, null, true );
}

/**
Read a time series from a RiverWare format file.
The resulting time series will have an identifier like STATIONID.RiverWare.Streamflow.1Day.
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
@return a pointer to a newly-allocated time series if successful, or null if not.
@param filename Name of file to read, assumed to start with ObjectName.SlotName
@param date1 Starting date to initialize period (null to read the entire time series).
@param date2 Ending date to initialize period (null to read the entire time series).
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String filename, DateTime date1, DateTime date2, String units, boolean read_data ) {
	TS ts = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	BufferedReader in = null;
	try {
	    in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( full_fname )) );
		// Don't have a requested time series but need the filename to infer location and data type.
		ts = readTimeSeries ( (TS)null, in, full_fname, date1, date2, units, read_data );
		ts.setInputName ( full_fname );
		ts.getIdentifier().setInputType("RiverWare");
		ts.getIdentifier().setInputName(full_fname);
		ts.addToGenesis ( "Read data from \"" + full_fname + "\" for period " + ts.getDate1() + " to " + ts.getDate2() );
	}
	catch ( Exception e ) {
		Message.printWarning( 3, "RiverWareTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
	}
	finally {
	    if ( in != null ) {
	        try {
	            in.close();
	        }
	        catch ( Exception e ) {
	            // Absorb - should not happen.
	        }
	    }
	}
	return ts;
}

/**
Read a time series from a RiverWare format file.
The TSID string is specified in addition to the path to the file.
It is expected that a TSID in the file matches the TSID (and the path to the file,
if included in the TSID would not properly allow the TSID to be specified).
This method can be used with newer code where the I/O path is separate from the TSID that is used to identify the time series.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated time series if successful, or null if not.
@param tsident_string The full identifier for the time series to read (where the scenario is NOT the file name).
@param filename The name of a file to read
(in which case the tsident_string must match one of the TSID strings in the file).
@param date1 Starting date to initialize period (NULL to read the entire time series).
@param date2 Ending date to initialize period (NULL to read the entire time series).
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries (String tsident_string, String filename,
    DateTime date1, DateTime date2, String units, boolean read_data )
throws Exception {
	TS ts = null;

	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	if ( !IOUtil.fileReadable(full_fname) ) {
		Message.printWarning( 3, "RiverWareTS.readTimeSeries",
		"Unable to determine file for \"" + filename + "\"" );
		return ts;
	}
	BufferedReader in = null;
	try {
	    in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 3, "RiverWareTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	try {
    	// Call the fully-loaded method.
    	// Pass the file pointer and an empty time series, which will be used to locate the time series in the file.
    	ts = TSUtil.newTimeSeries ( tsident_string, true );
    	if ( ts == null ) {
    		Message.printWarning( 3,
    		"RiverWareTS.readTimeSeries(String,...)",
    		"Unable to create time series for \"" + tsident_string + "\"" );
    		return ts;
    	}
    	ts.setIdentifier ( tsident_string );
    	readTimeSeries ( ts, in, full_fname, date1, date2, units, read_data );
    	ts.setInputName ( full_fname );
    	ts.getIdentifier().setInputType ( "RiverWare" );
    	ts.getIdentifier().setInputName ( filename );
    	ts.addToGenesis ( "Read data from \"" + full_fname +
    		"\" for period " + ts.getDate1() + " to " + ts.getDate2() );
	}
	catch ( Exception e ) {
	    // Just rethrow.
	    throw e;
	}
	finally {
	    if ( in != null ) {
	        in.close();
	    }
	}
	return ts;
}

/**
Read a time series from a RiverWare format file.
@return a pointer to time series if successful, null if not.
The calling code is responsible for freeing the memory for the time series.
@param req_ts Pointer to time series to fill.  If null, return a new time series.
All data are reset, except for the identifier, which is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param filename Name of file that is being read (assumed to start with ObjectName.SlotName,
which are used for the location and data type).
@param req_date1 Requested starting date to initialize period (or null to read the entire time series).
@param req_date2 Requested ending date to initialize period (or null to read the entire time series).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception Exception if there is an error reading the time series.
*/
public static TS readTimeSeries ( TS req_ts, BufferedReader in,
    String filename, DateTime req_date1, DateTime req_date2, String req_units, boolean read_data )
throws Exception {
	String routine = RiverWareTS.class.getSimpleName() + ".readTimeSeries";
	String string = null, timestep_string = "", end_date_string = "", start_date_string = "", scale_string = "";
	int	dl = 10;
	DateTime date1_file = null, date2_file = null;

	// Always read the header.  Optional is whether the data are read.

	int line_count = 0;

	String units = "";
	String token0, token1;
	DateTime date1 = null, date2 = null;
	TS ts = null;
	try {
    	while ( true ) {
    		string = in.readLine();
    		if ( string == null ) {
    			break;
    		}
    		++line_count;
    		if ( Message.isDebugOn ) {
    			Message.printDebug ( dl, routine, "Processing: \"" + string + "\"" );
    		}
    		string = string.trim();
    		if ( (string.length() == 0) || (string.charAt(0) == '#') ) {
    			// Skip comments and blank lines.
    			continue;
    		}
    		int pos = string.indexOf(":");
    		if ( pos < 0 ) {
    			// No more header information so break out and read data.
    			break;
    		}

    		// Break the tokens using ':' as the delimiter.  However, because dates typically
    		// use 24:00, have to take more care to get the correct token.

    		token0 = string.substring(0,pos).trim();
    		token1 = string.substring(pos+1).trim();
    		if ( token0.equalsIgnoreCase("start_date") ) {
    			start_date_string = token1.trim();
    		}
    		else if ( token0.equalsIgnoreCase("end_date") ) {
    			end_date_string = token1.trim();
    		}
    		else if ( token0.equalsIgnoreCase("timestep") ) {
    			timestep_string = token1.trim();
    		}
    		else if ( token0.equalsIgnoreCase("units") ) {
    			units = token1.trim();
    		}
    		else if ( token0.equalsIgnoreCase("scale") ) {
    			scale_string = token1.trim();
    		}
    		// Don't care about "set_scale" or "set_units" because they are
    		// only significant to RiverWare (not used by TS package).
    	}
	} catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error processing line " + line_count + ": \"" + string + "\"");
		Message.printWarning ( 3, routine, e );
	}

	// Process the dates.  RiverWare files always have 24:00 in the dates, even if the interval is >= daily.
	// This causes problems with the general DateTime.parse() method in that the
	// dates may roll over to the following month.  Therefore, strip the 24:00 off the date strings before parsing.

	if ( (StringUtil.indexOfIgnoreCase(timestep_string, "Day", 0) >= 0) ||
        (StringUtil.indexOfIgnoreCase(timestep_string, "Month", 0) >= 0) ||
        (StringUtil.indexOfIgnoreCase(timestep_string, "Year", 0) >= 0) ||
        (StringUtil.indexOfIgnoreCase(timestep_string, "Annual", 0) >= 0) ) {
	    // Remove the trailing 24:00 from start and end because it cases a problem parsing (rolls over to next month).
	    int pos = start_date_string.indexOf("24:00");
	    if ( pos > 0 ) {
	        start_date_string = start_date_string.substring(0,pos).trim();
	    }
        pos = end_date_string.indexOf("24:00");
        if ( pos > 0 ) {
            end_date_string = end_date_string.substring(0,pos).trim();
        }
	}
	date1_file = DateTime.parse ( start_date_string );
    date2_file = DateTime.parse ( end_date_string );
    int datePrecision = date1_file.getPrecision();
    // Further set the precision based on the data interval.
    if ( (StringUtil.indexOfIgnoreCase(timestep_string, "Day", 0) >= 0) ) {
        datePrecision = DateTime.PRECISION_DAY;
    }
    else if ( (StringUtil.indexOfIgnoreCase(timestep_string, "Month", 0) >= 0) ) {
        datePrecision = DateTime.PRECISION_MONTH;
    }
    else if ( (StringUtil.indexOfIgnoreCase(timestep_string, "Year", 0) >= 0) ||
        (StringUtil.indexOfIgnoreCase(timestep_string, "Annual", 0) >= 0) ) {
        datePrecision = DateTime.PRECISION_YEAR;
    }
    date1_file.setPrecision(datePrecision);
    date2_file.setPrecision(datePrecision);

	// Create an in-memory time series and set header information.

	if ( req_date1 != null ) {
		date1 = new DateTime(req_date1,datePrecision);
	}
	else {
	    date1 = date1_file;
	}
	if ( req_date2 != null ) {
		date2 = new DateTime(req_date2,datePrecision);
	}
	else {
	    date2 = new DateTime(date2_file);
	}
	ts = createTimeSeries (	req_ts, filename, timestep_string, units, date1, date2, date1_file, date2_file );
	if ( !read_data ) {
		return ts;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Reading data..." );
	}

	// Allocate the memory for the data array.

	if ( ts.allocateDataSpace() == 1 ) {
		Message.printWarning( 3, routine, "Error allocating data space..." );
		// Clean up memory.
		throw new Exception ( "Error allocating time series memory." );
	}

	int data_interval_base = ts.getDataIntervalBase ();
	int data_interval_mult = ts.getDataIntervalMult ();

	// The latest string that was read for the header should be the first data line so make sure not to skip it.

	try {
    	boolean first_data = true;
    	// Dates are not specified in the file so iterate with date.
    	DateTime date = new DateTime ( date1_file );
    	double scale = 1.0;
    	if ( StringUtil.isDouble(scale_string) ) {
    		scale = Double.valueOf ( scale_string );
    	}
    	for ( ; date.lessThanOrEqualTo(date2_file);
    		date.addInterval (data_interval_base, data_interval_mult ) ) {
    		if ( first_data ) {
    			first_data = false;
    		}
    		else {
    		    string = in.readLine();
    			++line_count;
    			if ( string == null ) {
    				break;
    			}
    		}
    		if ( Message.isDebugOn ) {
    			Message.printDebug ( dl, routine, "Processing: \"" + string + "\"" );
    		}
    		string = string.trim();
    		if ( (string.length() == 0) || (string.charAt(0) == '#') ) {
    			// Skip comments and blank lines...
    			continue;
    		}

    		// String will contain a data value or "NaN".
    		// If "NaN", just skip because the time series is initialized to missing data.

    		if ( date.lessThan(date1) ) {
    			// No need to do.
    			continue;
    		}
    		else if ( date.greaterThan(date2) ) {
    			if ( Message.isDebugOn ) {
    				Message.printDebug (
    				dl, routine, "Finished reading data at: " + date.toString() );
    			}
    			// Will return below.
    			break;
    		}
    		// Else set the data value.
    		if ( !string.equalsIgnoreCase("NaN") ) {
    		    if ( Message.isDebugOn ) {
    		        Message.printDebug(dl, routine, "Line " + line_count + " setting value " + string + "*scale at " + date );
    		    }
    			ts.setDataValue ( date, scale*Double.valueOf(string) );
    		}
    		else if ( Message.isDebugOn ) {
    		    Message.printDebug(dl, routine, "Line " + line_count + " setting value " + string + " at " + date );
    		}
    	}
	} catch ( Exception e ) {
		Message.printWarning ( 3, routine, "Error processing line " + line_count + ": \"" + string + "\"");
		Message.printWarning ( 3, routine, e );
	}
	return ts;
}

/**
Read multiple time series from a RiverWare RDF format file.
@return a pointer to a newly-allocated time series if successful, or null if not.
@param filename Name of file to read.
@param readStart Starting date to initialize period (null to read the entire time series).
@param readEnd Ending date to initialize period (null to read the entire time series).
@param units Units to convert to.
@param readData Indicates whether data should be read (false=no, true=yes).
*/
public static List<TS> readTimeSeriesListFromRdf ( String filename, DateTime readStart, DateTime readEnd,
    String units, boolean readData )
throws Exception {
    String routine = RiverWareTS.class.getSimpleName() + ".readTimeSeriesFromList", message;

    BufferedReader in = null;
    List<TS> tslist = new ArrayList<TS>();
    try {
        in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( filename )) );
        tslist = readTimeSeriesListFromRdf ( filename, in, readStart, readEnd, units, readData );
    }
    catch ( Exception e ) {
        message = "Error reading file \"" + filename + "\" ( " + e + ").";
        Message.printWarning( 3, routine, message );
        Message.printWarning( 3, routine, e );
        throw ( e );
    }
    finally {
        if ( in != null ) {
            try {
                in.close();
            }
            catch ( Exception e ) {
                // Absorb - should not happen.
            }
        }
    }
    return tslist;
}

/**
Read multiple time series from a RiverWare RDF format file.
@return a pointer to a newly-allocated time series if successful, or null if not.
@param filename Name of file to read.
@param readStart Starting date to initialize period (null to read the entire time series).
@param readEnd Ending date to initialize period (null to read the entire time series).
@param units Units to convert to.
@param readData Indicates whether data should be read (false=no, true=yes).
*/
public static List<TS> readTimeSeriesListFromRdf ( String filename, BufferedReader in, DateTime readStart, DateTime readEnd,
    String units, boolean readData )
throws IOException {
   String routine = RiverWareTS.class.getSimpleName() + ".readTimeSeriesListFromRdf";
    TS ts = null;
    ArrayList<TS> tslist = new ArrayList<>();
    String s, su, s2 = null;
    int lineCount = 0; // Line being read (so increment before reading).
    // Package properties.
    String packageName = "";
    String packageOwner = "";
    String packageDescription = "";
    String packageCreateDate = ""; // Use string because use 24 hour clock.
    DateTime packageCreateDate_DateTime = null;
    int packageNumberOfRuns = 0;
    // Run properties
    String runStart = "";
    DateTime runStart_DateTime = null;
    String runEnd = "";
    DateTime runEnd_DateTime = null;
    String runTimeStepUnit = "";
    TimeInterval runTimeStep_TimeInterval = null;
    int intervalBase = 0;
    int intervalMult = 0;
    int runUnitQuantity = -1;
    int runTimeSteps = -1;
    String runSlotSet = "";
    String runRuleSet = "";
    int runConsecutive = -1;
    int runIdxSequential = -1;
    // Slot properties
    String slotObjectType = "";
    String slotObjectName = "";
    String slotSlotName = "";
    String slotUnits = "";
    double slotScale = 1.0;
    int slotRows = -1;
    int slotCols = -1;
    double value = 0.0;
    boolean slotIsTable = false;
    // Loop over lines in file and parse - for now do it all linear rather than in separate methods because of data sharing.
    int colonPos;
    while ( true ) {
        ++lineCount;
        s = in.readLine();
        if ( s == null ) {
            break;
        }
        // Parse package preamble strings, in order of documentation.
        s = s.trim();
        su = s.toUpperCase();
        colonPos = s.indexOf(":");
        if ( su.startsWith("#")) {
            // Comment.
            continue;
        }
        else if ( su.startsWith("NAME:") ) {
            packageName = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
        }
        else if ( su.startsWith("OWNER:") ) {
            packageOwner = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
        }
        else if ( su.startsWith("DESCRIPTION:") ) {
            packageDescription = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
        }
        else if ( su.startsWith("CREATE_DATE:") ) {
            packageCreateDate = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
            try {
                // TODO SAM 2013-09-21 Looks like the date string can have single digits rather than zero padded.
                // Need to add format to parse.
                packageCreateDate_DateTime = parseRiverWareDateTime(packageCreateDate,DateTime.PRECISION_MINUTE);
            }
            catch ( Exception e ) {
                packageCreateDate_DateTime = null;
            }
        }
        else if ( su.startsWith("NUMBER_OF_RUNS:") ) {
            s2 = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1) : "" );
            try {
                packageNumberOfRuns = Integer.parseInt(s2);
            }
            catch ( NumberFormatException e ) {
                throw new IOException ( "number_of_runs (" + s2 + ") is not an integer." );
            }
        }
        else if ( s.equalsIgnoreCase("END_PACKAGE_PREAMBLE") ) {
            // Break and continue reading run data below.
            Message.printStatus(2, routine, "Detected END_PACKAGE_PREAMBLE at line " + lineCount );
            break;
        }
    }
    // Have read the package preamble ending with END_PACKAGE_PREAMBLE, now start on the runs.
    for ( int irun = 0; irun < packageNumberOfRuns; irun++ ) {
        Message.printStatus(2, routine, "Reading run [" + irun + "]" );
        while ( true ) {
            ++lineCount;
            s = in.readLine();
            if ( s == null ) {
                break;
            }
            // Parse run preamble strings, in order of documentation.
            s = s.trim();
            su = s.toUpperCase();
            colonPos = s.indexOf(":");
            if ( su.startsWith("START:") ) {
                runStart = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
            }
            else if ( su.startsWith("END:") ) {
                runEnd = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
            }
            else if ( su.startsWith("TIME_STEP_UNIT:") ) {
                runTimeStepUnit = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
            }
            else if ( su.startsWith("UNIT_QUANTITY:") ) {
                s2 = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                try {
                    runUnitQuantity = Integer.parseInt(s2);
                }
                catch ( NumberFormatException e ) {
                    throw new IOException ( "unit_quantity (" + s2 + ") is not an integer." );
                }
                if ( runUnitQuantity == 1 ) {
                    // Handles OK except for "week".
                    try {
                        runTimeStep_TimeInterval = TimeInterval.parseInterval(runTimeStepUnit);
                    }
                    catch ( Exception e ) {
                        throw new IOException ( "time_step_unit (" + runTimeStepUnit + ") is not recognized." );
                    }
                }
                else {
                    try {
                        runTimeStep_TimeInterval = TimeInterval.parseInterval("" + runUnitQuantity + runTimeStepUnit );
                    }
                    catch ( Exception e ) {
                        throw new IOException ( "time_step_unit (" + runTimeStepUnit + ") and unit_quantity (" + runUnitQuantity +
                            ") are not recognized." );
                    }
                }
                intervalBase = runTimeStep_TimeInterval.getBase();
                intervalMult = runTimeStep_TimeInterval.getMultiplier();
                // Now know the interval so can get the start and end to the proper precision.
                runStart_DateTime = parseRiverWareDateTime(runStart,runTimeStep_TimeInterval.getBase());
                runEnd_DateTime = parseRiverWareDateTime(runEnd,runTimeStep_TimeInterval.getBase());
            }
            else if ( su.startsWith("TIME_STEPS:") ) {
                s2 = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                try {
                    runTimeSteps = Integer.parseInt(s2);
                }
                catch ( NumberFormatException e ) {
                    throw new IOException ( "time_steps (" + s2 + ") is not an integer." );
                }
            }
            else if ( su.startsWith("SLOT_SET:") ) {
                runSlotSet = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
            }
            else if ( su.startsWith("RULE_SET:") ) {
                runRuleSet = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
            }
            else if ( su.startsWith("CONSECUTIVE:") ) {
                s2 = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                try {
                    runConsecutive = Integer.parseInt(s2);
                }
                catch ( NumberFormatException e ) {
                    throw new IOException ( "consecutive (" + s2 + ") is not an integer." );
                }
                if ( runConsecutive == 1 ) {
                    throw new IOException ( "Only consecutive=0 is currently supported." );
                }
            }
            else if ( su.startsWith("IDX_SEQUENTIAL:") ) {
                s2 = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                try {
                    runIdxSequential = Integer.parseInt(s2);
                }
                catch ( NumberFormatException e ) {
                    throw new IOException ( "idx_sequential (" + s2 + ") is not an integer." );
                }
            }
            else if ( s.equalsIgnoreCase("END_RUN_PREAMBLE") ) {
                Message.printStatus(2, routine, "Detected END_RUN_PREAMBLE at line " + lineCount );
                break;
            }
        }
        // If here the run preamble has been read.
        // Next read the dates for the run if time series or row numbers if a table.
        // TODO SAM 2013-09-21 is there any need to keep these and use later?.
        // If regular data the run start and end should match.
        for ( int idate = 0; idate < runTimeSteps; idate++ ) {
            ++lineCount;
            s = in.readLine();
            if ( (idate == 0) && (s.indexOf("-") > 0) ) {
                // Assume this is a time series with a date string.
                // Make sure date matches the run start.
                DateTime d1 = parseRiverWareDateTime(s.trim(),runTimeStep_TimeInterval.getBase());
                if ( !d1.equals(runStart_DateTime)) {
                    throw new IOException ( "At line " + lineCount + " date/time does not match start date/time." );
                }
            }
            else if ( (idate == (runTimeSteps - 1)) && (s.indexOf("-") > 0) ) {
                // Assume this is a time series with a date string.
                // Make sure date matches the run end.
                DateTime d2 = parseRiverWareDateTime(s.trim(),runTimeStep_TimeInterval.getBase());
                if ( !d2.equals(runEnd_DateTime)) {
                    throw new IOException ( "At line " + lineCount + " date/time does not match end date/time." );
                }
            }
        }
        Message.printStatus(2, routine, "Read last run date/time (or column row number) at line " + lineCount );
        // Read the slot data.  There is not property to indicate how many slots so have to loop until END_RUN indicates
        // that all slots are read for the run.
        boolean readingSlotsForRun = true;
        while ( readingSlotsForRun ) {
            // These two while statements could probably be joined but as shown clearly indicate separate blocks of data.
            Message.printStatus(2, routine, "Start reading slot data at line " + (lineCount + 1));
            while ( true ) {
                ++lineCount;
                s = in.readLine();
                if ( s == null ) {
                    // Premature end of file, break out of slot loop and let other code continue error handling.
                    readingSlotsForRun = false;
                    break;
                }
                s = s.trim();
                su = s.toUpperCase();
                if ( su.equals("END_RUN") ) {
                    // Done processing slots for run - break out of while and go back to for loop for run.
                    Message.printStatus(2, routine, "Detected END_RUN at line " + lineCount );
                    readingSlotsForRun = false;
                    break;
                }
                colonPos = s.indexOf(":");
                // Parse slot preamble strings, in order of documentation.
                if ( su.startsWith("OBJECT_TYPE:") ) {
                    slotObjectType = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                    Message.printStatus(2, routine, "Detected OBJECT_TYPE at line " + lineCount );
                    // TODO SAM 2013-09-21 Maybe this indicates whether a time series or table?
                }
                else if ( su.startsWith("OBJECT_NAME:") ) {
                    slotObjectName = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                    Message.printStatus(2, routine, "Detected OBJECT_NAME at line " + lineCount );
                }
                else if ( su.startsWith("SLOT_NAME:") ) {
                    slotSlotName = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                    Message.printStatus(2, routine, "Detected SLOT_NAME at line " + lineCount );
                }
                else if ( su.startsWith("ROWS:") ) {
                    // Indicates a table rather than time series.
                    slotIsTable = true;
                    try {
                        s2 = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                        slotRows = Integer.parseInt(s2);
                    }
                    catch ( NumberFormatException e ) {
                        throw new IOException ( "At line " + lineCount + " \"rows\" (" + s2 + ") is not an integer." );
                    }
                    Message.printStatus(2, routine, "Detected ROWS at line " + lineCount + " data object is table.  Will read but ignore." );
                }
                else if ( su.startsWith("COLS:") ) {
                    // Used with table.
                    try {
                        s2 = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                        slotCols = Integer.parseInt(s2);
                    }
                    catch ( NumberFormatException e ) {
                        throw new IOException ( "At line " + lineCount + " \"cols\" (" + s2 + ") is not an integer." );
                    }
                }
                else if ( su.equals("END_SLOT_PREAMBLE") ) {
                    Message.printStatus(2, routine, "Detected END_SLOT_PREAMBLE at line " + lineCount );
                    break;
                }
            }
            if ( !readingSlotsForRun ) {
                // Need to break out of this level also because END_RUN was detected.
                break;
            }
            // If here the slot preamble has been read.
            // Read the data for the slot.
            if ( slotIsTable ) {
                // Reading a table.
                // TODO SAM 2013-09-21 Need to actually handle table - for now just handle time series.
                Message.printStatus(2, routine, "Start reading table at line " + lineCount );
                for ( int irow = 0; irow < slotRows; irow++ ) {
                    ++lineCount;
                    s = in.readLine();
                }
                for ( int icol = 0; icol < slotCols; icol++ ) {
                    ++lineCount;
                    s = in.readLine();
                    colonPos = s.indexOf(":");
                    slotUnits = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                    ++lineCount;
                    s = in.readLine();
                    colonPos = s.indexOf(":");
                    s2 = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                    try {
                        s2 = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                        slotScale = Double.parseDouble(s2);
                    }
                    catch ( NumberFormatException e ) {
                        throw new IOException ( "At line " + lineCount + " \"slot_scale\" (" + s2 + ") is not a number." );
                    }
                    for ( int irow = 0; irow < slotRows; irow++ ) {
                        ++lineCount;
                        s = in.readLine();
                    }
                }
            }
            else {
                // Reading a time series, one value per dates that were read in the run preamble.
                Message.printStatus(2, routine, "Slot is time series.  Starting to read at line " + (lineCount + 1) );
                ++lineCount;
                s = in.readLine();
                colonPos = s.indexOf(":");
                slotUnits = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                ++lineCount;
                s = in.readLine();
                colonPos = s.indexOf(":");
                s2 = ( s.length() >= (colonPos + 1) ? s.substring(colonPos + 1).trim() : "" );
                try {
                    slotScale = Double.parseDouble(s2);
                }
                catch ( NumberFormatException e ) {
                    throw new IOException ( "At line " + lineCount + " \"slot_scale\" (" + s2 + ") is not a number." );
                }
                // Create the time series.
                String tsid = slotObjectName + ".RiverWare." + slotSlotName + "." + runTimeStep_TimeInterval;
                DateTime fileStart = new DateTime(runStart_DateTime);
                DateTime fileEnd = new DateTime(runEnd_DateTime);
                if ( ((runConsecutive == 0) && (packageNumberOfRuns > 1)) || (runIdxSequential == 1) ) {
                    // runConsecutive=0 means that a single run was done over the period start to end (?).
                    // runIdxSequential means that overlapping runs were made, with resequenced historical input.
                    // The run dates are already overlapping.  Set the sequence number to the year of the date for the run..
                    // TODO SAM 2013-09-21 Are the index sequential years truly sequential or can they be mixed?.
                    //int sequenceNum = fileStart.getYear() + irun;
                    // TODO SAM what is the unique identifier for the sequence number?  Year of historical trace, some other
                    // metadata?
                    int sequenceNum = irun + 1;
                    tsid = tsid + TSIdent.SEQUENCE_NUMBER_LEFT + sequenceNum + TSIdent.SEQUENCE_NUMBER_RIGHT;
                }
                Message.printStatus(2, routine, "Creating time series \"" + tsid + "\"" );
                try {
                    ts = TSUtil.newTimeSeries(tsid, true);
                }
                catch ( Exception e ) {
                    throw new IOException ( "Error creating time series using TSID \"" + tsid + "\" (" + e + ")." );
                }
                try {
                    ts.setIdentifier ( tsid );
                }
                catch ( Exception e ) {
                    throw new IOException ( "Error setting time seriies identifier \"" + tsid + "\" (" + e + ").");
                }
                if ( readStart != null ) {
                    ts.setDate1(readStart);
                }
                else {
                    ts.setDate1(fileStart);
                }
                if ( readEnd != null ) {
                    ts.setDate2(readEnd);
                }
                else {
                    ts.setDate2(fileEnd);
                }
                ts.setDate1Original(fileStart);
                ts.setDate2Original(fileEnd);
                ts.setDataUnits(slotUnits);
                ts.setDataUnitsOriginal(slotUnits);
                // Set all the properties (some of these also will be used for the ensemble if ensembles are read.
                ts.setProperty("PackageName", packageName);
                ts.setProperty("PackageOwner", packageOwner);
                ts.setProperty("PackageDescription", packageDescription);
                ts.setProperty("PackageCreateDate", packageCreateDate_DateTime);
                ts.setProperty("PackageNumberOfRuns", Integer.valueOf(packageNumberOfRuns));
                ts.setProperty("RunConsecutive", Integer.valueOf(runConsecutive));
                ts.setProperty("RunIdxSequential", Integer.valueOf(runIdxSequential));
                ts.setProperty("RunSlotSet", runSlotSet);
                ts.setProperty("RunRuleSet", runRuleSet);
                ts.setProperty("SlotObjectType", slotObjectType);
                ts.setProperty("SlotObjectName", slotObjectName);
                ts.setProperty("SlotSlotName", slotSlotName);
                ts.getIdentifier().setInputType("RiverWare");
                ts.getIdentifier().setInputName(filename);
                if ( readData ) {
                    ts.allocateDataSpace();
                }
                tslist.add ( ts );
                // Use the file date to read through data but time series will only have data within period.
                DateTime date = new DateTime(fileStart);
                for ( int istep = 0; istep < runTimeSteps; istep++ ) {
                    ++lineCount;
                    s = in.readLine().trim();
                    if ( readData ) {
                        if ( s.equalsIgnoreCase("NaN")) {
                            // Missing value.  Don't need to set anything.
                        }
                        else {
                            // Parse the value.
                            value = Double.parseDouble(s);
                            //Message.printStatus(2,routine,"Setting " + date + " " + value );
                            ts.setDataValue(date, slotScale*value);
                        }
                    }
                    date.addInterval(intervalBase,intervalMult);
                }
                Message.printStatus(2, routine, "Read last slot time series value at line " + lineCount );
            }
            // Read and check for expected end of data.
            ++lineCount;
            s = in.readLine();
            if ( !s.toUpperCase().equals("END_COLUMN") ) {
                throw new IOException ( "At line " + lineCount + " expecting END_COLUMN, have: " + s );
            }
            Message.printStatus(2, routine, "Detected END_COLUMN at line " + lineCount );
            ++lineCount;
            s = in.readLine();
            if ( !s.toUpperCase().equals("END_SLOT") ) {
                throw new IOException ( "At line " + lineCount + " expecting END_SLOT, have: " + s );
            }
            Message.printStatus(2, routine, "Detected END_SLOT at line " + lineCount );
            // At top of loop will look for END_RUN and if that is not found, another slot for the run will be read.
        } // Loop on slots in run.
    } // Loop on runs.
    Message.printStatus(2, routine, "Processed " + lineCount + " lines" );
    return tslist;
}

/**
Write a RiverWare time series to the open PrintWriter.
@param ts Time series to write.
@param fp PrintWrite to write to.
@exception IOException if there is an error writing the file.
*/
private static void writeTimeSeries ( TS ts, PrintWriter fp )
throws IOException {
	writeTimeSeries ( ts, fp, (DateTime)null, (DateTime)null, (PropList)null, true );
}

/**
Write a time series to a RiverWare format file.  The entire period is written.
@param ts Single time series to write.
@param fname Name of file to write.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, String fname )
throws IOException {
	PrintWriter	out = null;

	String full_fname = IOUtil.getPathUsingWorkingDir(fname);
	try {
	    out = new PrintWriter (new FileWriter(full_fname));
	}
	catch ( Exception e ) {
		String message = "Error opening \"" + full_fname + "\" for writing.";
		Message.printWarning ( 3, "RiverWareTS.writePersistent(TS,String)", message );
		throw new IOException ( message );
	}
	try {
	    writeTimeSeries ( ts, out );
	}
	catch ( IOException e ) {
	    // Rethrow.
	    throw e;
	}
	finally {
	    out.close ();
	}
}

/**
Write a time series to a RiverWare format file using a default format based on the time series units.
@param ts Vector of pointers to time series to write.
@param fname Name of file to write.
@param req_date1 First date to write (if NULL write the entire time series).
@param req_date2 Last date to write (if NULL write the entire time series).
@param req_units Units to write.  If different than the current units the units will be converted on output.
@param scale Scale to divide values by for output.  Ignored if <= 0.0.
@param set_units RiverWare "set_units" parameter.  If empty or null will not be written.
@param set_scale RiverWare "set_scale" parameter.  If zero or negative will not be written.
@param write_data Indicates whether data should be written (as opposed to only
writing the header) (<b>currently not used</b>).
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, String fname, DateTime req_date1, DateTime req_date2,
			String req_units, double scale, String set_units, double set_scale,	boolean write_data )
throws IOException {
	PropList props = new PropList ( "RiverWare" );
	if ( req_units != null ) {
		props.set ( "Units", req_units );
	}
	if ( scale >= 0.0 ) {
		props.set ( "Scale", "" + scale );
	}
	if ( set_units != null ) {
		props.set ( "SetUnits", set_units );
	}
	if ( set_scale >= 0.0 ) {
		props.set ( "SetScale", "" + set_scale );
	}
	writeTimeSeries ( ts, fname, req_date1, req_date2, props, write_data );
}

/**
Write a time series to a RiverWare format file using a default format based on the time series units.
@param ts List of pointers to time series to write.
@param fname Name of file to write.
@param req_date1 First date to write (if NULL write the entire time series).
@param req_date2 Last date to write (if NULL write the entire time series).
@param props Properties to control the write.  The following properties are
supported:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>Scale</b></td>
<td>The factor to divide values by for output.
</td>
<td>Do not divide (scale of 1).</td>
</tr>

<tr>
<td><b>SetUnits</b></td>
<td>The RiverWare "set_units" parameter.  If empty or null will not be written.
</td>
<td></td>
</tr>

<tr>
<td><b>SetScale</b></td>
<td>The RiverWare "set_scale" parameter.  If empty or null will not be written.
</td>
<td></td>
</tr>

<tr>
<td><b>Units</b></td>
<td>The Units to write to the RiverWare header.  The data values are NOT converted.
</td>
<td></td>
</tr>

<tr>
<td><b>Precision</b></td>
<td>The precision (number of digits after decimal) to use when writing data values.
</td>
<td></td>
</tr>

</table>
@param write_data Indicates whether data should be written
(as opposed to only writing the header) (<b>currently not used</b>).
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, String fname, DateTime req_date1, DateTime req_date2,
		PropList props, boolean write_data )
throws IOException {
	PrintWriter	out = null;

	String full_fname = IOUtil.getPathUsingWorkingDir(fname);
	try {
	    out = new PrintWriter (new FileWriter(full_fname));
	}
	catch ( Exception e ) {
		String message = "Error opening \"" + full_fname + "\" for writing.";
		Message.printWarning ( 3,"RiverWareTS.writeTimeSeries",message);
		throw new IOException ( message );
	}
	writeTimeSeries ( ts, out, req_date1, req_date2, props, write_data);
	out.close ();
}

/**
Write a time series to a RiverWare format file using a default format based on the data units.
@param ts Time series to write.
@param fp PrintWriter to write to.
@param req_date1 First date to write (if NULL write the entire time series).
@param req_date2 Last date to write (if NULL write the entire time series).
@param req_units Units to write.  If different than the current units the units
will be converted on output.  This method does not support set_units.
@param scale Scale to divide values by for output.
@param set_units RiverWare "set_units" parameter.  If empty or null will not be written.
@param set_scale RiverWare "set_scale" parameter.  If zero or negative will not be written.
@param write_data Indicates whether data should be written (if false only the
header is written).
@param props Properties to control output, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td> <td><b>Description</b></td> <td><b>Default</b></td>
</tr>

<tr>
<td><b>OutputComments</b></td>
<td><b>Additional comments to be output in the header, as a list of String.  The comment
lines are not added to in any way.</b>
<td>No additional comments.</td>
</tr>

<tr>
<td><b>WriteHeaderComments</b></td>
<td><b>Whether to write header comments.</b>
<td>Write header comments.</td>
</tr>
</table>
@exception IOException if there is an error writing the file.
*/
private static void writeTimeSeries ( TS ts, PrintWriter fp, DateTime req_date1, DateTime req_date2,
					PropList props, boolean write_data )
throws IOException {
	String message, routine = RiverWareTS.class.getSimpleName() + ".writeTimeSeries";

	if ( ts == null ) {
		message = "Time series is NULL, cannot continue.";
		Message.printWarning( 3, routine, message );
		throw new IOException ( message );
	}

	if ( fp == null ) {
		message = "Output stream is NULL, cannot continue.";
		Message.printWarning( 3, routine, message );
		throw new IOException ( message );
	}

	// Get the interval information to facilitate use below.

	int data_interval_base = ts.getDataIntervalBase();
	int data_interval_mult = ts.getDataIntervalMult();

	// Get the dates for output.

	DateTime date1 = null;
	if ( req_date1 == null ) {
		date1 = new DateTime ( ts.getDate1() );
	}
	else {
	    date1 = new DateTime ( req_date1 );
		// Make sure the precision is that of the data.
		date1.setPrecision ( data_interval_base );
	}
	DateTime date2 = null;
	if ( req_date2 == null ) {
		date2 = new DateTime ( ts.getDate2() );
	}
	else {
	    date2 = new DateTime ( req_date2 );
		// Make sure the precision is that of the data.
		date2.setPrecision ( data_interval_base );
	}

	if ( props == null ) {
		props = new PropList ( "RiverWare" );
	}
	boolean writeHeaderComments = true; // Default is to write header comments.
	String propVal = props.getValue("WriteHeaderComments");
	if ( (propVal != null) && propVal.equalsIgnoreCase("False") ) {
		writeHeaderComments = false;
	}

	// Write header.

	if ( writeHeaderComments ) {
		fp.println ( "#" );
		IOUtil.printCreatorHeader ( fp, "#", 80, 0 );
	    Object o = props.getContents("OutputComments");
	    if ( o != null ) {
	        // Write additional comments that were passed in.
	        @SuppressWarnings("unchecked")
			List<String> comments = (List<String>)o;
	        int commentSize = comments.size();
	        if ( commentSize > 0 ) {
	            for ( int iComment = 0; iComment < commentSize; iComment++ ) {
	                fp.println ( "# " + comments.get(iComment) );
	            }
	        }
	    }
		fp.println ( "#" );
		fp.println ( "# RiverWare Time Series File" );
		fp.println ( "#" );
	}
	if ( data_interval_base == TimeInterval.HOUR ) {
		// Adjust the internal 0-23 clock to the 0-24 RiverWare clock.
		if ( date1.getHour() == 0 ) {
			// Set to hour 24 of the previous day.
			DateTime d = new DateTime ( date1 );
			d.addDay ( -1 );
			fp.println ( "start_date: " + d.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
		}
		else {
		    // OK to write the date/time as is with hour and minute.
			fp.println ( "start_date: " + date1.toString( DateTime.FORMAT_YYYY_MM_DD_HH_mm ) );
		}
		if ( date2.getHour() == 0 ) {
			// Set to hour 24 of the previous day.
			DateTime d = new DateTime ( date2 );
			d.addDay ( -1 );
			fp.println ( "end_date: " + d.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
		}
		else {
		    // OK to write the date/time as is with hour and minute.
			fp.println ( "end_date: " + date2.toString(
			DateTime.FORMAT_YYYY_MM_DD_HH_mm ) );
		}
	}
	else if ( data_interval_base == TimeInterval.DAY ) {
		// Use the in-memory day but always add 24:00 at the end.
		fp.println ( "start_date: " + date1.toString(DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
		fp.println ( "end_date: " + date2.toString(	DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
	}
	else if ( data_interval_base == TimeInterval.MONTH ) {
		// Use the in-memory day but always add 24:00 at the end.
		// Set the day to the number of days in the month.
		// The day will be ignored below during iteration because the precision was set above to month.
		date1.setDay ( TimeUtil.numDaysInMonth(date1) );
		fp.println ( "start_date: " + date1.toString(DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
		date2.setDay ( TimeUtil.numDaysInMonth(date2) );
		fp.println ( "end_date: " + date2.toString(DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
	}
	else if ( data_interval_base == TimeInterval.YEAR ) {
		// Use the in-memory day but always add 24:00 at the end.
		// Set the month and day to the end of the year.
		// The month and day will be ignored below during iteration because the precision was set above to month.
		date1.setMonth ( 12 );
		date1.setDay ( 31 );
		fp.println ( "start_date: " + date1.toString( DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
		date2.setMonth ( 12 );
		date2.setDay ( 31 );
		fp.println ( "end_date: " + date2.toString(	DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
	}
	else {
	    // Interval is not supported.
		throw new IOException ( "Interval for \"" +	ts.getIdentifier() + "\" is not supported for RiverWare.");
	}
	// Print the interval, with multiplier, if provided.
	try {
	    TimeInterval interval = TimeInterval.parseInterval ( ts.getIdentifier().getInterval() );
		if ( interval.getMultiplierString().equals("") ) {
			fp.println ( "timestep: 1 " + interval.getBaseString() );
		}
		else {
		    fp.println ( "timestep: " + interval.getMultiplierString() + " " + interval.getBaseString() );
		}
	}
	catch ( Exception e ) {
		// Ignore for now.
	}
	String Units = props.getValue ( "Units" );
	if ( (Units != null) && (Units.length() > 0) ) {
		fp.println ( "units: " + Units );
	}
	else {
	    fp.println ( "units: " + ts.getDataUnits() );
	}
	String Scale = props.getValue ( "Scale" );
	double Scale_double = 1.0;
	if ( (Scale == null) || (Scale.length() == 0) ) {
		Scale = "1";	// Default.
	}
	else if ( StringUtil.isDouble(Scale) ) {
		Scale_double = Double.valueOf ( Scale );
	}
	fp.println ( "scale: " + Scale );
	String SetUnits = props.getValue ( "SetUnits" );
	if ( (SetUnits != null) && !SetUnits.equals("") ) {
		fp.println ( "set_units: " + SetUnits );
	}
	String SetScale = props.getValue ( "SetScale" );
	if ( (SetScale != null) && StringUtil.isDouble(SetScale) ) {
		fp.println ( "set_scale: " + SetScale );
	}
	String Precision = props.getValue ( "Precision" );
	if ( Precision == null ) {
		Precision = "4";	// Default.
	}
	String format = "%." + Precision + "f";

	DateTime date = new DateTime ( date1 );
	double value;
	for ( ; date.lessThanOrEqualTo(date2);
		date.addInterval(data_interval_base,data_interval_mult) ) {
		value = ts.getDataValue ( date );
		if ( ts.isDataMissing(value) ) {
			fp.println ( "NaN" );
		}
		else {
		    fp.println (StringUtil.formatString( value/Scale_double, format) );
		}
	}
}

}