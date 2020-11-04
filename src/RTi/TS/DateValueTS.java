// DateValueTS - class to process date-value format time series

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

// ----------------------------------------------------------------------------
// DateValueTS - class to process date-value format time series
// ----------------------------------------------------------------------------
// History:
//
// 01 Jan 2000	Steven A. Malers, RTi	Copy and modify DateValueMinuteTS to
//					evaluate whether static I/O methods are
//					a good alternative.  Issues to consider
//					with this approach:
//					* For some time series, it may be
//					  appropriate to declare the time series
//					  in one place and populate data in
//					  another.  In this case, a pointer to
//					  a time series may need to be supplied
//					  to the read method.
//					* What if there are data specific to the
//					  time series format that need to be
//					  carried around between read/write (use
//					  a PropList in TS base class)?
//					* What if a connection to the TS file
//					  is to remain open so that an
//					  incremental read/write can occur
//					  (PropList again or add some specific
//					  data members to TS base class)?
//					Need to consider these before going to
//					this approach for everything, but at
//					the very least, this DateValueTS class
//					may minimize the amount of code
//					duplicated in other DateValue* classes.
// 21 Feb 2000	SAM, RTi		Address a number of feedback issues.
//					Add ability to handle irregular time
//					series when writing.  Figure out why
//					the scenario needs to be specified in
//					the file ID when writing.  Initialize
//					the data type and other information to
//					empty strings when readying.  Add
//					readTimeSeries that takes a TS* pointer.
// 18 Mar 2000	SAM, RTi		Updated based on purify of code.
// 17 Jun 2000	SAM, RTi		Update to use new utility classes.
// 28 Jul 2000	SAM, RTi		Overload readTimeSeries to make
//					units and read_data flag optional.
// 11 Aug 2000	SAM, RTi		Add method to write multiple time
//					series.  Previously, much of this code
//					was in the mergets utility program.
//					Use this method for all writing.  Add
//					javadoc throughout.  Remove
//					freeStringList and use delete to avoid
//					purify PLK messages.
// 20 Aug 2000	SAM, RTi		Add units conversion on output to get
//					rid of some warnings at run-time.
// 03 Oct 2000	SAM, RTi		Port C++
// 23 Oct 2000	SAM, RTi		Update so NumTS property is recognized
//					on a read, but only handle cases where
//					NumTS is 1.
// 07 Nov 2000	SAM, RTi		Add additional information to header,
//					including a header line.
//					Enable reading of multiple time series
//					with readTimeSeriesList().
// 23 Nov 2000	SAM, RTi		Change printSample() to getSample() and
//					return a Vector.
// 22 Feb 2001	SAM, RTi		Add call to setInputName() when reading.
//					Change IO to IOUtil.
// 01 Mar 2001	SAM, RTi		Add call to
//					IOUtil.getPathUsingWorkingDir() to
//					allow batch and GUI code to use the
//					same code.
// 11 Apr 2001	SAM, RTi		Update to support reading multiple
//					time series.
// 25 Apr 2001	SAM, RTi		Use "MissingVal" everywhere and not
//					"Missing".  Fix so that when a single
//					time series is read, the time series
//					identifier that is passed can be either
//					a filename or a time series identifier
//					where the filename is in the scenario.
//					Change so that the readTimeSeriesList()
//					methods that used to take a TS parameter
//					now take a String tsident_string
//					parameter.  Do this because when reading
//					a single time series from
//					Add isDateValueFile() method.  This can
//					be used to determine the file format
//					without relying on the source.
// 01 Jun 2001	SAM, RTi		Remove @ when writing data with times -
//					use a space.  The read code will now
//					properly handle.
// 14 Aug 2001	SAM, RTi		Track down problem where some DateValue
//					time series are not getting read
//					correctly - data_interval_base was not
//					being set for requested time series.
// 28 Aug 2001	SAM, RTi		Track down problem where reading a file
//					and then writing does not include the
//					data type - the Java TS class carries
//					the type independent of the TSIdent so
//					had to set from the TSIdent.  Also, if
//					the data type is missing, do not set
//					when reading.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2001-11-20	SAM, RTi		If the start and end dates have a time
//					zone, set the time zone in the write
//					code to not have a time zone.  Otherwise
//					the output will be excessive and
//					redundant and will foul up the parsing
//					code.  The start and end dates with time
//					zones should be enough.  When writing
//					time series, if time is used, make
//					separate headers for the date and time
//					columns.
// 2002-01-14	SAM, RTi		Add readFromStringList().
// 2002-01-31	SAM, RTi		Overload readTimeSeries() to take a
//					requested TSID and a file name.  This
//					is consistent with new TSTool separate
//					handling of the TSID and storage path.
//					Start using the expanded identifier that
//					includes input type and input name.
// 2002-02-08	SAM, RTi		Call the new TSIdent.setInputType() and
//					TSIdent.setInputName() methods.
// 2002-04-16	SAM, RTi		Write the alias and handle reading the
//					alias.  This is needed to transfer
//					TSTool output persistently between runs.
//					Change the DateValue format version to
//					1.2.
// 2002-04-23	SAM, RTi		Fix bug where null time series could
//					not be written - should now result in
//					missing data and blanks in header.
// 2002-04-25	SAM, RTi		Change so when setting the input name
//					the passed in value is used (not the
//					full path).  This works better with
//					dynamic applications where the working
//					directory is set by the application.
//					Change all printWarning() calls to level
//					2.
// 2002-06-12	SAM, RTi		Fix bug where empty DataType header
//					information seems to be causing
//					problems.  In writeTimeSeries(),
//					surround DataType and Units with
//					strings.  Change the format version to
//					1.3.  Fix so that if the data type is
//					not set in the TS, use the TSIdent
//					data type on read and write.
// 2002-07-12	SAM, RTi		Fix problem where descriptions in
//					header were not being read correctly in
//					cases where the description contained
//					equals signs.  Also when reading only
//					the headers, quit reading if
//					"# Time series histories" has been
//					encountered.  Otherwise time series with
//					very long histories take a long time to
//					read for just the header.
// 2002-09-04	SAM, RTi		Update to support input/output of data
//					quality flags.  For now if a time series
//					has data flags, write them.  Backtrack
//					on the UsgsNwis format and only use the
//					data flags to set the data initially
//					but don't carry around (need a strategy
//					for how to handle data flags in
//					manipulation).  Change the version to
//					1.4.
// 2003-02-14	SAM, RTi		Allow NaN to be used as the missing data
//					value.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TSUnits to DataUnits.
//					* Change TS.INTERVAL* to TimeInterval.
// 2003-07-24	SAM, RTi		* Change writeTimeSeries() to
//					  writeTimeSeriesList() when multiple
//					  time series are written.
// 2003-07-31	SAM, RTi		Change so when passing a requested time
//					series identifier to the read method,
//					the aliases in the file are checked
//					first.
// 2003-08-12	SAM, RTi		* Minor cleanup based on review of C++
//					  code (which was recently ported from
//					  this Java code): 1) Initialize some
//					  size variables to 0 in
//					  readTimeSeriesList(); 2) Remove check
//					  for zero year in writeTimeSeriesList()
//					  - average time series may use zero
//					  year.
//					* Update the readTimeSeries() method
//					  that takes a TSID and file name to
//					  handle an alias as the TSID.
//					* When reading the time series, do not
//					  allow file names to be in the
//					  scenario.
//					* Handle sequence number in
//					  readTimeSeriesList().
// 2003-08-19	SAM, RTi		* No longer handle time series
//					  identifiers that have the scenario as
//					  the scenario.  Instead, rely on the
//					  full input name.
//					* Track down a problem apparently
//					  introduced in the last update - time
//					  series files without alias were not
//					  being read.
// 2003-10-06	SAM, RTi		* After reading the header, check for
//					  critical information (e.g., TSID) and
//					  assign defaults.  This will help with
//					  file problems and will also allow
//					  files without the properties to be
//					  read.
//					* Add time series comments to the
//					  header.
// 2003-11-19	SAM, RTi		* Print the time series TSID and alias
//					  in comments and genesis.
//					* Throw an exception in
//					  writeTimeSeriesList() if an error
//					  occurs.
//					* For IrregularTS in
//					  writeTimeSeriesList(), add the units
//					  conversion in again.
// 2004-03-01	SAM, RTi		* Format TSID as quoted strings on
//					  output.
//					* Some command descriptions now have
//					  include = so manually parse out the
//					  header tokens.
// 2005-08-10	SAM, RTi		Fix bug where bad file was passing a
//					null file pointer to the read, resulting
//					in many warnings.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
//EndHeader

package RTi.TS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.DataUnitsConversion;
import RTi.Util.IO.GzipToolkit;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.IO.ZipToolkit;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
Class to read/write RTi DateValue time series files, using static methods.
The file consists of header information as Property=Value strings and a data
section that is column based.  Time series must have the same interval.
Date/times for each record are parsed so records can be missing.
*/
public class DateValueTS
{
    
/**
Latest file version.  Use the integer version for internal comparisons.
*/
private static String __VERSION_CURRENT = "1.6";
private static int __VERSION_CURRENT_INT = 16000;

/**
Determine whether a file is a DateValue file.  This can be used rather than
checking the source in a time series identifier.  If the file passes any of the
following conditions, it is assumed to be a DateValue file:
<ol>
<li>	A line starts with "#DateValue".</li>
<li>	A line starts with "# DateValue".</li>
<li>	A line starts with "TSID" and includes "=".</li>
</ol>
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
*/
public static boolean isDateValueFile ( String filename )
{	BufferedReader in = null;
	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	try {
		if ( full_fname.toUpperCase().endsWith(".ZIP") ) {
			// Handle case where DateValue file is compressed (single file in .zip)
			ZipToolkit zt = new ZipToolkit();
			in = zt.openBufferedReaderForSingleFile(full_fname,0);
		}
		else {
			in = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream ( full_fname )) );
		}
	    boolean is_datevalue = false;
	    try {
    		// Read lines and check for common strings that indicate a DateValue file.
    		String string = null;
    		while( (string = in.readLine()) != null ) {
    			if ( string.startsWith("# DateValue") || string.startsWith("#DateValue") ) {
    				is_datevalue = true;
    				break;
    			}
    			if ( string.regionMatches(true,0,"TSID",0,4) && (string.indexOf("=") >= 0) ) {
    				is_datevalue = true;
    				break;
    			}
    		}
	    }
	    finally {
	        if ( in != null ) {
	            in.close();
	        }
	    }
		return is_datevalue;
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Parse a data string of the form "{dataflag1:"description",dataflag2:"description"}"
@param value the DateValue property value to parse
*/
private static List<TSDataFlagMetadata> parseDataFlagDescriptions(String value)
{
	List<TSDataFlagMetadata> metaList = new ArrayList<TSDataFlagMetadata>();
	value = value.trim().replace("{","").replace("}", "");
	List<String> parts = StringUtil.breakStringList(value, ",", StringUtil.DELIM_ALLOW_STRINGS | StringUtil.DELIM_ALLOW_STRINGS_RETAIN_QUOTES);
	for ( String part : parts ) {
		// Now have flag:description
		List<String> parts2 = StringUtil.breakStringList(part.trim(), ":", StringUtil.DELIM_ALLOW_STRINGS | StringUtil.DELIM_ALLOW_STRINGS_RETAIN_QUOTES);
		if ( parts2.size() == 2 ) {
			String propName = parts2.get(0).trim();
			String propVal = parts2.get(1).trim();
			if ( propVal.startsWith("\"") && propVal.endsWith("\"") ) {
				// Have a quoted string
				metaList.add(new TSDataFlagMetadata(propName,propVal.substring(1,propVal.length() - 1)));
			}
			else if ( propVal.equalsIgnoreCase("null") ) {
				metaList.add(new TSDataFlagMetadata(propName,""));
			}
		}
	}
	return metaList;
}

// TODO SAM 2015-05-18 This is brute force - need to make more elegant
/**
Parse a properties string of the form "{stringprop:"propval",intprop:123,doubleprop=123.456}"
*/
private static PropList parseTimeSeriesProperties(String value)
{
	PropList props = new PropList("");
	value = value.trim().replace("{","").replace("}", "");
	List<String> parts = StringUtil.breakStringList(value, ",", StringUtil.DELIM_ALLOW_STRINGS | StringUtil.DELIM_ALLOW_STRINGS_RETAIN_QUOTES);
	for ( String part : parts ) {
		// Now have Name:value
		List<String> parts2 = StringUtil.breakStringList(part.trim(), ":", StringUtil.DELIM_ALLOW_STRINGS | StringUtil.DELIM_ALLOW_STRINGS_RETAIN_QUOTES);
		if ( parts2.size() == 2 ) {
			String propName = parts2.get(0).trim();
			String propVal = parts2.get(1).trim();
			if ( propVal.startsWith("\"") && propVal.endsWith("\"") ) {
				// Have a quoted string
				props.setUsingObject(propName,propVal.substring(1,propVal.length() - 1));
			}
			else if ( propVal.equalsIgnoreCase("null") ) {
				props.setUsingObject(propName,null);
			}
			else if ( StringUtil.isInteger(propVal) ) {
				props.setUsingObject(propName, Integer.parseInt(propVal));
			}
			else if ( StringUtil.isDouble(propVal) ) {
				props.setUsingObject(propName, Double.parseDouble(propVal));
			}
		}
	}
	return props;
}

/**
Read at time series from a List of String.  Currently this is accomplished by
writing the contents to a temporary file and then reading using one of the
standard methods.  A more efficient method may be added in the future but this
approach works OK for smaller files.
@param strings List of String containing data in DateValue file format.
@param tsident_string Time series identifier as string (used for initial
settings - reset by file contents).
@param req_date1 Requested starting date to initialize period (or null to read the entire period).
@param req_date2 Requested ending date to initialize period (or null to read the entire period).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception Exception if there is an error reading the time series.
*/
public static TS readFromStringList ( List<String> strings, String tsident_string,
					DateTime req_date1, DateTime req_date2, String req_units, boolean read_data )
throws Exception
{	// Write the strings to a temporary file...
	String temp = IOUtil.tempFileName();
	PrintWriter pw = new PrintWriter ( new FileOutputStream(temp) );
	int size = 0;
	if ( strings != null ) {
		size = strings.size();
	}
	for ( int i = 0; i < size; i++ ) {
		pw.println ( strings.get(i) );
	}
	pw.close ();
	// Create a DateValueTS from the temporary file...
	TS ts = readTimeSeries ( temp, req_date1, req_date2, req_units, read_data );
	// Remove the temporary file...
	File tempf = new File ( temp );
	tempf.delete();
	// Return...
	return ts;
}

/**
Read a time series from a DateValue format file.
@return time series if successful, or null if not.
@param tsident_string One of the following:  1) the time series identifier to
read (where the scenario is the file name) or 2) the name of a file to read
(in which case it is assumed that only one time series exists in the
file - otherwise use the readTimeSeriesList() method).
@exception TSException if there is an error reading the time series.
*/
public static TS readTimeSeries ( String tsident_string )
throws Exception
{	return readTimeSeries ( tsident_string, null, null, null, true );
}

/**
Read a time series from a DateValue format file.  The entire file is read using the units from the file.
@return 0 if successful, 1 if not.
@param in Reference to open BufferedReader.
@exception TSException if there is an error reading the time series.
*/
public static TS readTimeSeries ( BufferedReader in )
throws Exception
{	return readTimeSeries ( in, null, null, null, true );
}

/**
Read a time series from a DateValue format file.
@return a time series if successful, null if not.
@param in Reference to open BufferedReader.
@param req_date1 Requested starting date to initialize period (or null to read the entire period).
@param req_date2 Requested ending date to initialize period (or null to read the entire period).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception Exception if there is an error reading the time series.
*/
public static TS readTimeSeries ( BufferedReader in, DateTime req_date1, DateTime req_date2,
					String req_units, boolean read_data )
throws Exception
{	// Call the generic method...
	return readTimeSeries ( (TS)null, in, req_date1, req_date2, req_units, read_data );
}

/**
Read a time series from a DateValue format file.
@return a time series if successful, null if not.  The units are taken from the file and all data are read
(not just the header).
@param tsident_string One of the following:  1) the time series identifier to
read (where the scenario is the file name) or 2) the name of a file to read
(in which case it is assumed that only one time series exists in the
file - otherwise use the readTimeSeriesList() method).
@param date1 Starting date to initialize period (or null to read entire time series).
@param date2 Ending date to initialize period (or null to read entire time
series).
*/
public static TS readTimeSeries ( String tsident_string, DateTime date1, DateTime date2 )
throws Exception
{	return readTimeSeries ( tsident_string, date1, date2, "", true );
}

/**
Read a time series from a DateValue format file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a time series if successful, null if not.
@param tsident_string The full identifier for the time series to
read with the file name in the ~DateValue~InputName part of the identifier or
2) the name of a file to read (in which case it is assumed that only one time
series exists in the file - otherwise use the readTimeSeriesList() method).
@param date1 Starting date to initialize period (null to read the entire time series).
@param date2 Ending date to initialize period (null to read the entire time series).
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String tsident_string, DateTime date1,
			DateTime date2, String units, boolean read_data )
throws Exception
{	TS ts = null;
	boolean	is_file = true;	// Is tsident_string a file?  Assume and check below

	String input_name = tsident_string;
	String full_fname = IOUtil.getPathUsingWorkingDir ( tsident_string );
	Message.printStatus(2, "", "Reading \"" + tsident_string + "\"" );
	if ( !IOUtil.fileReadable(full_fname) && (tsident_string.indexOf("~") > 0) ) {
	    // The string is a TSID string (implicit read command) with the file name in the
	    // input type.
		is_file = false;
		// Try the input name to get the file...
		TSIdent tsident = new TSIdent (tsident_string);
		full_fname = IOUtil.getPathUsingWorkingDir ( tsident.getInputName() );
		input_name = full_fname;
	}
	// By here the file name is set.
    if ( !IOUtil.fileExists(full_fname) ) {
        Message.printWarning( 2, "DateValueTS.readTimeSeries", "File does not exist: \"" + full_fname + "\"" );
    }
    if ( !IOUtil.fileReadable(full_fname) ) {
        Message.printWarning( 2, "DateValueTS.readTimeSeries", "File is not readable: \"" + full_fname + "\"" );
    }
	BufferedReader in = null;
	if ( full_fname.toUpperCase().endsWith(".ZIP") ) {
		// Handle case where DateValue file is compressed (single file in .zip)
		ZipToolkit zt = new ZipToolkit();
		in = zt.openBufferedReaderForSingleFile(full_fname,0);
	}
	else {
		// The following will throw an exception that is appropriate (like no file found).
	    in = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream ( full_fname )) );
	}
    try {
    	// Call the fully-loaded method...
    	if ( is_file ) {
    		// Expect that the time series file has one time series and should read it...
    		ts = readTimeSeries ( (TS)null, in, date1, date2, units, read_data );
    	}
    	else {
    	    // Pass the file pointer and an empty time series, which
    		// will be used to locate the time series in the file.
    		ts = TSUtil.newTimeSeries ( tsident_string, true );
    		if ( ts == null ) {
    		    String message = "Unable to create time series for \"" + tsident_string + "\"";
    			Message.printWarning( 2, "DateValueTS.readTimeSeries(String,...)", message );
    			throw new Exception ( message );
    		}
    		ts.setIdentifier ( tsident_string );
    		ts.getIdentifier().setInputType("DateValue");
    		readTimeSeriesList ( ts, in, date1, date2, units, read_data );
    	}
    	ts.setInputName ( full_fname );
    	ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
    	ts.getIdentifier().setInputName ( input_name );
    	
    }
    finally {
        if ( in != null ) {
            in.close();
        }
    }
	return ts;
}

/**
Read a time series from a DateValue format file.  The TSID string is specified
in addition to the path to the file.  It is expected that a TSID in the file
matches the TSID (and the path to the file, if included in the TSID would not
properly allow the TSID to be specified).  This method can be used with newer
code where the I/O path is separate from the TSID that is used to identify the time series.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a time series if successful, null if not.
@param tsident_string The full identifier for the time series to
read.  This string can also be the alias for the time series in the file.
@param filename The name of a file to read
(in which case the tsident_string must match one of the TSID strings in the file).
@param date1 Starting date to initialize period (null to read the entire time series).
@param date2 Ending date to initialize period (null to read the entire time series).
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String tsident_string, String filename,
					DateTime date1, DateTime date2, String units, boolean read_data )
throws Exception
{	TS ts = null;

	String input_name = filename;
	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
    if ( !IOUtil.fileExists(full_fname) ) {
        Message.printWarning( 2, "DateValueTS.readTimeSeries", "File does not exist: \"" + filename + "\"" );
    }
	if ( !IOUtil.fileReadable(full_fname) ) {
		Message.printWarning( 2, "DateValueTS.readTimeSeries", "File is not readable: \"" + filename + "\"" );
	}
	BufferedReader in = null;
	if ( full_fname.toUpperCase().endsWith(".ZIP") ) {
		// Handle case where DateValue file is compressed (single file in .zip)
		ZipToolkit zt = new ZipToolkit();
		in = zt.openBufferedReaderForSingleFile(full_fname,0);
	}
	else {
		in = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream ( full_fname )) );
	}
	// Pass the file pointer and an empty time series, which
	// will be used to locate the time series in the file.
	// The following is somewhat ugly because if we are using an alias we
	// cannot get the time series from newTimeSeries() because it does not
	// have an interval.  In this case, assume daily data.  This requires
	// special treatment in the readTimeSeriesList() method in order to
	// reset the time series to what is actually found in the file.
	// TODO - clean this up, perhaps by moving the time series creation
	// into the readTimeSeriesList() method rather than doing it here.
    try {
    	if ( tsident_string.indexOf(".") >= 0 ) {
    		// Normal time series identifier...
    		ts = TSUtil.newTimeSeries ( tsident_string, true );
    	}
    	else {
            // Assume an alias...
    		ts = new DayTS ();
    	}
    	if ( ts == null ) {
    		Message.printWarning( 2,
    		"DateValueTS.readTimeSeries(String,...)","Unable to create time series for \"" + tsident_string + "\"" );
    		return ts;
    	}
    	if ( tsident_string.indexOf(".") >= 0 ) {
    		ts.setIdentifier ( tsident_string );
    	}
    	else {
    	    ts.setAlias ( tsident_string );
    	}
    	List<TS> v = readTimeSeriesList ( ts, in,	date1, date2, units, read_data );
    	if ( tsident_string.indexOf(".") < 0 ) {
    		// The time series was specified with an alias so it needs
    		// to be replaced with what was read.  The alias will have been
    		// assigned in the readTimeSeriesList() method.
    		ts = v.get(0);
    	}
    	ts.getIdentifier().setInputType("DateValue");
    	ts.setInputName ( full_fname );
    	ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
    	ts.getIdentifier().setInputName ( input_name );
    }
    finally {
        if ( in != null ) {
            in.close();
        }
    }
	return ts;
}

/**
Read a time series from a DateValue format file.  The data units are taken from
the file and all data are read (not just the header).
@return a time series if successful, null if not.
@param req_ts time series to fill.  If null,
return a new time series.  If non-null, all data are reset, except for the
identifier, which is assumed to have been set in the calling code.
@param fname Name of file to read.
@param date1 Starting date to initialize period to.
@param date2 Ending date to initialize period to.
*/
public static TS readTimeSeries ( TS req_ts, String fname, DateTime date1, DateTime date2 )
throws Exception
{	return readTimeSeries ( req_ts, fname, date1, date2, "", true );
}

/**
Read a time series from a DateValue format file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a time series if successful, null if not.
@param req_ts time series to fill.  If null,
return a new time series.  All data are reset, except for the identifier, which
is assumed to have been set in the calling code.
@param fname Name of file to read.
@param date1 Starting date to initialize period to.
@param date2 Ending date to initialize period to.
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( TS req_ts, String fname, DateTime date1, DateTime date2,
					String units, boolean read_data )
throws Exception
{	TS ts = null;

	String input_name = fname;
	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
    if ( !IOUtil.fileExists(full_fname) ) {
        Message.printWarning( 2, "DateValueTS.readTimeSeries", "File does not exist: \"" + fname + "\"" );
    }
    if ( !IOUtil.fileReadable(full_fname) ) {
        Message.printWarning( 2, "DateValueTS.readTimeSeries", "File is not readable: \"" + fname + "\"" );
    }
	BufferedReader in = null;
	if ( full_fname.toUpperCase().endsWith(".ZIP") ) {
		// Handle case where DateValue file is compressed (single file in .zip)
		ZipToolkit zt = new ZipToolkit();
		in = zt.openBufferedReaderForSingleFile(full_fname,0);
	}
	else {
		in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( full_fname )) );
	}
    try {
    	ts = readTimeSeries ( req_ts, in, date1, date2, units, read_data );
    	ts.setInputName ( full_fname );
    	ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
    	ts.getIdentifier().setInputName ( input_name );
    }
    finally {
        if ( in != null ) {
            in.close();
        }
    }
	return ts;
}

/**
Read a time series from a DateValue format file.
@return a time series if successful, null if not.
@param req_ts time series to fill.  If null,return a new time series.
All data are reset, except for the identifier, which is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param req_date1 Requested starting date to initialize period (or null to read the entire time series).
@param req_date2 Requested ending date to initialize period (or null to read the entire time series).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read.
@exception Exception if there is an error reading the time series.
*/
public static TS readTimeSeries ( TS req_ts, BufferedReader in,	DateTime req_date1, DateTime req_date2,
	String req_units, boolean read_data )
throws Exception
{	List<TS> tslist = readTimeSeriesList ( req_ts, in, req_date1, req_date2, req_units, read_data );
	if ( (tslist == null) || (tslist.size() != 1) ) {
		return null;
	}
	else {
	    TS ts = tslist.get(0);
		return ts;
	}
}

/**
Read all the time series from a DateValue format file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a list of time series if successful, null if not.
@param fname Name of file to read.
@param date1 Starting date to initialize period (null to read the entire time series).
@param date2 Ending date to initialize period (null to read the entire time series).
@param units Units to convert to.
@param read_data Indicates whether data should be read.
@exception FileNotFoundException if the file is not found.
@exception IOException if there is an error reading the file.
*/
public static List<TS> readTimeSeriesList ( String fname, DateTime date1, DateTime date2, String units, boolean read_data)
throws Exception, IOException, FileNotFoundException
{	List<TS> tslist = null;
	String input_name = fname;
	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
    if ( !IOUtil.fileExists(full_fname) ) {
        Message.printWarning( 2, "DateValueTS.readTimeSeries", "File does not exist: \"" + fname + "\"" );
    }
    if ( !IOUtil.fileReadable(full_fname) ) {
        Message.printWarning( 2, "DateValueTS.readTimeSeries", "File is not readable: \"" + fname + "\"" );
    }
	BufferedReader in = null;
	try {
		if ( full_fname.toUpperCase().endsWith(".ZIP") ) {
			// Handle case where DateValue file is compressed (single file in .zip)
			ZipToolkit zt = new ZipToolkit();
			in = zt.openBufferedReaderForSingleFile(full_fname,0);
		}
		else if ( full_fname.toUpperCase().endsWith(".GZ") ) {
			// Handle case where DateValue file is compressed (single file in .gz)
			GzipToolkit zt = new GzipToolkit();
			in = zt.openBufferedReaderForSingleFile(full_fname,0);
		}
		else {
			in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( full_fname )) );
		}
    	tslist = readTimeSeriesList ( null, in, date1, date2, units, read_data);
    	TS ts;
    	int nts = 0;
    	if ( tslist != null ) {
    		nts = tslist.size();
    	}
    	for ( int i = 0; i < nts; i++ ) {
    		ts = tslist.get(i);
    		if ( ts != null ) {
    			ts.setInputName ( full_fname );
    			ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
    			ts.getIdentifier().setInputName ( input_name );
    		}
    	}
	}
	finally {
	    if ( in != null ) {
	        in.close();
	    }
	}
	return tslist;
}

// TODO SAM 2008-05-09 Evaluate types of exceptions that are thrown
/**
Read a time series from a DateValue format file.
@return a List of time series if successful, null if not.  The calling code
is responsible for freeing the memory for the time series.
@param req_ts time series to fill.  If null, return all new time series in the list.
All data are reset, except for the identifier, which is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param req_date1 Requested starting date to initialize period (or null to read the entire time series).
@param req_date2 Requested ending date to initialize period (or null to read the entire time series).
@param units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read.
@exception Exception if there is an error reading the time series.
*/
private static List<TS> readTimeSeriesList ( TS req_ts, BufferedReader in, DateTime req_date1,
						DateTime req_date2,	String req_units, boolean read_data )
throws Exception
{	String date_str, message = null, string = "", value, variable;
	String routine = "DateValueTS.readTimeSeriesList";
	int	dl = 10, dl2 = 30, numts = 1;
	DateTime date1 = new DateTime(), date2 = new DateTime();
	// Do not allow consecutive delimiters in header or data values.  For example:
	// 1,,2 will return
	// 2 values for version 1.3 and 3 values for version 1.4 (middle value is missing).
	int delimParseFlag = 0;

	// Always read the header.  Optional is whether the data are read...

	int line_count = 0;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Processing header..." );
	}
	String delimiter_default = " ";
	String alias = "", dataflag = "", datatype = "", delimiter = delimiter_default,
		description = "", identifier = "", missing = "", seqnum = "",
		units = "";
	List<String> alias_v = null;
	List<String> dataflag_v = null;
	boolean	[] ts_has_data_flag = null;
	int	[] ts_data_flag_length = null;
	List<String> datatype_v = null;
	List<String> description_v = null;
	List<String> identifier_v = null;
	List<String> missing_v = null;
	List<PropList> propertiesList = null;
	List<List<TSDataFlagMetadata>> dataFlagMetadataList = null;
	List<String> seqnum_v = null;
	List<String> units_v = null;
	boolean	include_count = false;
	boolean	include_total_time = false;
	int	size = 0;
	int	equal_pos = 0;	// Position of first '=' in line.
	int warning_count = 0;
	try {
	while( (string = in.readLine()) != null ) {
		++line_count;
		// Trim the line to better deal with blank lines...
		string = string.trim();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,"Processing: \"" + string + "\"" );
		}
		if ( !read_data && string.regionMatches(true,0,"# Time series histories",0,23) ) {
			if ( Message.isDebugOn ) {
				Message.printDebug( 10, routine, "Detected end of header." );
			}
			break;
		}
		if ( (string.equals(""))  ) {
			// Skip comments and blank lines for now...
			continue;
		}
		else if ( string.charAt(0) == '#' ) {
		    String version = "# DateValueTS";
	        if ( string.regionMatches(0,version,0,version.length()) ) {
	            // Have the file version so use to indicate how file is processed.
	            // This property should be used at the top because it impacts how other data are parsed.
	            double version_double =
	                StringUtil.atod ( StringUtil.getToken(string," ",StringUtil.DELIM_SKIP_BLANKS, 2) );
	            if ( (version_double > 0.0) && (version_double < 1.4) ) {
	                // Older settings...
	                delimParseFlag = StringUtil.DELIM_SKIP_BLANKS;
	            }
	            else {
	                // Default and new settings.
	                delimParseFlag = 0;
	            }
	        }
	        continue;
		}

		if ( (equal_pos = string.indexOf('=')) == -1 ) {
			// Assume this not a header definition variable and that we are done with the header...
			if ( Message.isDebugOn ) {
				Message.printDebug( 10, routine, "Detected end of header." );
			}
			break;
		}

		// Else, process the header string...

		// Don't parse out quoted strings here.  If any tokens use
		// quoted strings, they need to be processed below.  Because
		// some property values now contain the =, parse out manually..

		if ( equal_pos == 0 ) {
			if ( Message.isDebugOn ) {
				Message.printDebug( 10, routine, "Bad property for \"" + string + "\"." );
				++warning_count;
			}
			continue;
		}

		// Now the first token is the left side and the second token is the right side...

		variable = string.substring(0,equal_pos).trim();
		if ( equal_pos == (string.length() - 1) ) {
			value = "";
		}
		else {
		    // Trim the value so no whitespace on either end.
		    value = string.substring(equal_pos + 1).trim();
		}

		// Deal with the tokens...
		if ( variable.equalsIgnoreCase("Alias") ) {
			// Have the alias...
			alias = value;
			alias_v = StringUtil.breakStringList (
			        value, delimiter, delimParseFlag | StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( alias_v != null ) {
				size = alias_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 3, routine, "Number of Alias values using delimiter \"" + delimiter +
				        "\" (" + size +	") is != NumTS (" +	numts +	").  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					alias_v.add ( "" );
				}
			}
		}
		else if ( variable.toUpperCase().startsWith("DATAFLAGDESCRIPTIONS_") ) {
			// Found a properties string of the form DataFlagDescriptions_NN = { ... }
			if ( dataFlagMetadataList == null ) {
				// Create a list of data flag metadata for each time series
				dataFlagMetadataList = new ArrayList<List<TSDataFlagMetadata>>(numts);
				for ( int i = 0; i < numts; i++ ) {
					dataFlagMetadataList.add(new ArrayList<TSDataFlagMetadata>());
				}
			}
			// Now parse out the properties for this time series and set in the list
			int pos1 = variable.indexOf("_");
			if ( pos1 > 0 ) {
				int iprop = Integer.parseInt(variable.substring(pos1+1).trim());
				dataFlagMetadataList.set((iprop - 1), parseDataFlagDescriptions(value));
			}
		}
		else if ( variable.equalsIgnoreCase("DataFlags") ) {
			// Have the data flags indicator which may or may not be surrounded by quotes...
			dataflag = value;
			dataflag_v = StringUtil.breakStringList (
			        dataflag, delimiter,delimParseFlag|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( dataflag_v != null ) {
				size = dataflag_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of DataFlag values using delimiter \"" + delimiter +
				        "\" (" + size +	") is != NumTS (" + numts + "). Assuming no data flags.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					dataflag_v.add ( "false" );
				}
			}
			// Now further process the data flag indicators.  Need a boolean for each time series to indicate whether
			// data flags are used and need a width for the data flags
			ts_has_data_flag = new boolean[numts];
			ts_data_flag_length = new int[numts];
			for ( int ia = 0; ia < numts; ia++ ) {
				dataflag = dataflag_v.get(ia).trim();
				List<String> v = StringUtil.breakStringList (	dataflag,",",0);
				size = 0;
				if ( v != null ) {
					size = v.size();
				}
				if ( size == 0 ) {
					// Assume no data flag...
					ts_has_data_flag[ia] = false;
					continue;
				}
				// If the first value is "true", assume that the data flag is used...
				if ( v.get(0).trim().equalsIgnoreCase("true") ) {
					ts_has_data_flag[ia] = true;
				}
				else {
				    ts_has_data_flag[ia] = false;
				}
				// Now set the length...
				ts_data_flag_length[ia] = 2; // Default
				if ( size > 1 ) {
					ts_data_flag_length[ia] = StringUtil.atoi(((String)v.get(1)).trim());
				}
			}
		}
		else if ( variable.equalsIgnoreCase("DataType") ) {
			// Have the data type...
			datatype = value;
			datatype_v = StringUtil.breakStringList (
			        datatype, delimiter, delimParseFlag|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( datatype_v != null ) {
				size = datatype_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of DataType values using delimiter \"" + delimiter +
				        "\" (" + size + ") is != NumTS (" + numts + "). Assuming blank.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					datatype_v.add ( "" );
				}
			}
		}
		else if ( variable.equalsIgnoreCase("Delimiter") ) {
			// Have the delimiter.  This value is probably quoted so remove quotes.
		    String delimiter_previous = delimiter;
		    delimiter = StringUtil.remove(value, "\"");
		    delimiter = StringUtil.remove(delimiter, "\'");
			if ( value.length() == 0 ) {
			    delimiter = delimiter_default;
			}
			Message.printStatus( 2, routine, "Delimiter is \"" + delimiter +
			        "\" for remaining properties and data columns (previously was \"" + delimiter_previous + "\").");
		}
		else if ( variable.equalsIgnoreCase("Description") ) {
			// Have the description.  The description may contain "=" so get the second token manually...
			description = value;
			description_v = StringUtil.breakStringList (
			        description, delimiter, delimParseFlag|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( description_v != null ) {
				size = description_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of Description values using delimiter \"" + delimiter +
				        "\" (" + size +	") is != NumTS (" + numts + ").  Assuming blank.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					description_v.add ( "" );
				}
			}
		}
		else if ( variable.equalsIgnoreCase("End") ) {
			// Have the ending date.  This may be reset below by the requested end date..
			date2 = DateTime.parse ( value );
		}
		else if ( variable.equalsIgnoreCase("IncludeCount") ) {
			// Will have data column for the count...
			if ( value.equalsIgnoreCase("true") ) {
				include_count = true;
			}
			else {
			    include_count = false;
			}
		}
		else if ( variable.equalsIgnoreCase("IncludeTotalTime") ) {
			// Will have data column for the total time...
			if ( value.equalsIgnoreCase("true") ) {
				include_total_time = true;
			}
			else {
			    include_total_time = false;
			}
		}
		else if ( variable.equalsIgnoreCase("MissingVal") ) {
			// Have the missing data value...
			missing = value;
			missing_v = StringUtil.breakStringList (
				missing, delimiter,	delimParseFlag|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( missing_v != null ) {
				size = missing_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of Missing values using delimiter \"" + delimiter +
				        "\" (" + size + ") is != NumTS (" + numts + ").  Assuming -999.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					missing_v.add ( "" );
				}
			}
		}
		else if ( variable.equalsIgnoreCase("NumTS") ) {
			// Have the number of time series...
			numts = StringUtil.atoi(value);
		}
		else if ( variable.toUpperCase().startsWith("PROPERTIES_") ) {
			// Found a properties string of the form Properties_NN = { ... }
			if ( propertiesList == null ) {
				// Create a PropList for each time series
				propertiesList = new ArrayList<PropList>(numts);
				for ( int i = 0; i < numts; i++ ) {
					propertiesList.add(new PropList(""));
				}
			}
			// Now parse out the properties for this time series and set in the list
			int pos1 = variable.indexOf("_");
			if ( pos1 > 0 ) {
				int iprop = Integer.parseInt(variable.substring(pos1+1).trim());
				propertiesList.set((iprop - 1), parseTimeSeriesProperties(value));
			}
		}
		else if ( variable.equalsIgnoreCase("SequenceNum") || variable.equalsIgnoreCase("SequenceID")) {
			// Have sequence numbers...
			seqnum = value;
			seqnum_v = StringUtil.breakStringList (
				seqnum, delimiter, delimParseFlag|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( seqnum_v != null ) {
				size = seqnum_v.size();
				for ( int i = 0; i < size; i++ ) {
				    // Replace old -1 missing with blank string
				    if ( seqnum_v.get(i).equals("-1") ) {
				        seqnum_v.set(i, "");
				    }
				}
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of SequenceID (or SequenceNum) values using delimiter \"" + delimiter +
				        "\" (" + size + ") is != NumTS (" + numts + ").  Assuming -1.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					seqnum_v.add ( "" );
				}
			}
		}
		else if ( variable.equalsIgnoreCase("Start") ) {
			// Have the starting date.  This may be reset below by the requested start date....
			date1 = DateTime.parse ( value );
		}
		else if ( variable.equalsIgnoreCase("TSID") ) {
			// Have the TSIdent...
			identifier = value;
			identifier_v = StringUtil.breakStringList (
				identifier, delimiter, delimParseFlag|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( identifier_v != null ) {
				 size = identifier_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of TSID values using delimiter \"" + delimiter +
				        "\" (" + size + ") is != NumTS (" + numts + "). Assuming blank.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					identifier_v.add ( "" );
				}
			}
		}
		else if ( variable.equalsIgnoreCase("Units") ) {
			// Have the data units...
			units = value;
			units_v = StringUtil.breakStringList (
				units, delimiter, delimParseFlag|StringUtil.DELIM_ALLOW_STRINGS );
			if ( units_v != null ) {
				size = units_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of Units values using delimiter \"" + delimiter +
				        "\" (" + size + ") is != NumTS (" + numts + "). Assuming blank. Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					units_v.add ( "" );
				}
			}
		}
		else {
            Message.printWarning( 3, routine, "Property \"" + variable + "\" is not currently recognized." );
		}
	}
	}
	catch ( Exception e ) {
		message = "Unexpected error processing line " + line_count + ": \"" + string + "\"";
		Message.printWarning ( 3, routine, message );
		Message.printWarning ( 3, routine, e );
		throw new Exception ( message );
	}
	if ( warning_count > 0 ) {
	    // Print a warning and throw an exception about the header not being properly
	    message = "" + warning_count + " errors existing in file header.  Not reading data.";
        Message.printWarning ( 3, routine, message );
        // FIXME SAM 2008-04-14 Throw a more specific exception
        throw new Exception ( message );
	}
	// Reset for below.
	warning_count = 0;

	// Make sure the data flag boolean array is allocated.  This simplifies the logic below...

	if ( ts_has_data_flag == null ) {
		ts_has_data_flag = new boolean[numts];
		for ( int i = 0; i < numts; i++ ) {
			ts_has_data_flag[i] = false;
		}
	}

	// Check required data lists and assign defaults if necessary...

	if ( identifier_v == null ) {
		identifier_v = new ArrayList<String>(numts);
		// TODO SAM 2008-04-14 Evaluate tightening this constraint - throw exception?
		Message.printWarning ( 2, routine, "TSID property in file is missing.  Assigning default TS1, TS2, ..." );
		for ( int i = 0; i < numts; i++ ) {
			identifier_v.add ( "TS" + (i + 1) );
		}
	}

	// Declare the time series of the proper type based on the interval.
	// Use a TSIdent to parse out the interval information...

	TSIdent ident = null;
	int data_interval_base = 0;

	int req_ts_i = -1;	// Which time series corresponds to the requested time series.
	int req_ts_column = -1;	// Which column of data corresponds to the requested time series.
	int req_ts_column2 = -1;// Which column of data corresponds to the
				// requested time series, after adjustment for possible additional time column in date.
	TS ts = null;
	List<TS> tslist = null;
	TS ts_array[] = null;	// Use this to speed processing so we don't have to search through tslist all the time
	// Set the time series to either the requested time series
	// or a newly-created time series.  If a requested time series is
	// given but only its alias is available, create a new time series
	// using the matching TSID, which will contain the interval, etc.
	if ( req_ts != null ) {
		req_ts_i = -1;	// Index of found time series...
		// If there is only one time series in the file, assume it should be used, regardless...
		if ( numts == 1 ) {
			//Message.printStatus ( 1, "", "Using only TS because only one TS in file." );
			req_ts_i = 0;
		}
		if ( req_ts_i < 0 ) {
			// Need to keep searching.  Loop through all the time series identifiers and compare exactly.
		    // That way if only the scenarios are different we will find the correct time series.
			for ( int i = 0; i < numts; i++ ) {
				// Check the alias for a match.  This takes precedence over the identifier.
				if ( alias_v != null ) {
					alias = alias_v.get(i).trim();
					if ( !alias.equals("") && req_ts.getAlias().equalsIgnoreCase( alias) ) {
						// Found a matching time series...
						req_ts_i = i;
						//Message.printStatus ( 1, "", "Found matching TS "+req_ts_i+ " based on alias." );
						break;
					}
				}
				// Now check the identifier...
				identifier = identifier_v.get(i).trim();
				if ( req_ts.getIdentifierString().equalsIgnoreCase( identifier) ) {
					// Found a matching time series...
					req_ts_i = i;
					//Message.printStatus ( 1, "", "SAMX Found matching TS " + req_ts_i + " based on full TSID." );
					break;
				}
			}
		}
		if ( req_ts_i < 0 ) {
			// Did not find the requested time series...
		    message = "Did not find the requested time series \"" + req_ts.getIdentifierString() + "\" Alias \"" +
                req_ts.getAlias() + "\"";
			Message.printWarning ( 2, routine, message );
			throw new Exception ( message );
		}
		// If here a requested time series was found.  However, if the requested TSID used the
		// alias only, need to create a time series of the correct type using the header information...
		if ( req_ts.getLocation().equals("") && !req_ts.getAlias().equals("") ) {
			// The requested time series is only identified by the alias and needs to be recreated for the full
			// identifier.  This case is configured in the calling public readTimeSeries() method.
			identifier = identifier_v.get( req_ts_i).trim();
			//Message.printStatus ( 1, routine,"SAMX creating new req_ts for \"" +
			//identifier + "\" alias \"" + req_ts.getAlias() +"\"");
			ts = TSUtil.newTimeSeries ( identifier, true );
			ts.setIdentifier ( identifier );
			// Reset the requested time series to the new one because req_ts is checked below...
			ts.setAlias ( req_ts.getAlias() );
			req_ts = ts;
		}
		else {
		    // A full TSID was passed in for the requested time series and there is no need to reassign the requested
			// time series...
			//Message.printStatus ( 1, routine, "SAMX using existing ts for \"" +
			//identifier + "\" alias \"" + req_ts.getAlias() +"\"");
			ts = req_ts;
			// Identifier is assumed to have been set previously.
		}
		// Remaining logic is the same...
		tslist = new ArrayList<TS>(1);
		tslist.add ( ts );
		ts_array = new TS[1];
		ts_array[0] = ts;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, "Adding requested time series to list." );
		}
		ident = new TSIdent ( ts.getIdentifier() );
		data_interval_base = ident.getIntervalBase();
		// Offset other information because of extra columns...
		// Make sure to set the interval for use below...
		identifier = identifier_v.get( req_ts_i).trim();
		ident = new TSIdent( identifier );
		// Set the data type in the TS header using the information in the identifier.
		// It may be overwritten below if the DataType property is specified...
		ts.setDataType ( ident.getType() );
		// Reset the column to account for the date...
		req_ts_column = req_ts_i + 1;	// 1 is date.
		if ( include_count ) {
			++req_ts_column;
		}
		if ( include_total_time ) {
			++req_ts_column;
		}
		if ( dataflag_v != null ) {
			// At least one of the time series in the file uses data flags so adjust the column for
			// time series that may be before the requested time series...
			for ( int ia = 0; ia < req_ts_i; ia++ ) {
				if ( ts_has_data_flag[ia] ) {
					++req_ts_column;
				}
			}
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, "Time series \"" + req_ts.getIdentifierString() +
			"\" will be read from data column " + req_ts_column + " (date column = 0)" );
		}
	}
	else {
	    // Allocate here as many time series as indicated by numts...
		tslist = new ArrayList<TS>(numts);
		ts_array = new TS[numts];
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, "Allocated space for " + numts + " time series in list." );
		}
		for ( int i = 0; i < numts; i++ ) {
			identifier = identifier_v.get(i).trim();
			ident = new TSIdent( identifier );
			// Need this to check whether time may be specified on data line...
			data_interval_base = ident.getIntervalBase();
			ts = TSUtil.newTimeSeries ( identifier, true );
			if ( ts == null ) {
				Message.printWarning ( 2, routine, "Unable to create new time series for \"" + identifier + "\"" );
				return null;
			}
			// Only set the identifier if a new time series.
			// Otherwise assume the the existing identifier is to be used (e.g., from a file name).
			ts.setIdentifier ( identifier );
			ts.getIdentifier().setInputType("DateValue");
			tslist.add ( ts );
			ts_array[i] = ts;
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, "Created memory for \"" + ts.getIdentifierString() + "\"" );
			}
		}
	}

	// Set the parameters from the input variables and override with the
	// parameters in the file if necessary...

	if ( req_date1 != null ) {
		date1 = req_date1;
	}
	if ( req_date2 != null ) {
		date2 = req_date2;
	}
	if ( (date1 != null) && (date2 != null) && date1.greaterThan(date2) ) {
		Message.printWarning ( 2, routine, "Date2 (" + date2 + ") is > Date1 (" + date1 + ").  Errors are likely." );
		++warning_count;
	}
	try {
		for ( int i = 0; i < numts; i++ ) {
			if ( req_ts != null ) {
				if ( req_ts_i != i ) {
					// A time series was requested but does not match so continue...
					continue;
				}
				else {	// Found the matching requested time series...
					ts = ts_array[0];
				}
			}
			else {	// Reading a list...
				ts = ts_array[i];
			}
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, "Setting properties for \"" + ts.getIdentifierString() + "\"" );
			}
			if ( alias_v != null ) {
				alias = alias_v.get(i).trim();
				if ( !alias.equals("") ) {
					ts.setAlias ( alias );
				}
			}
			if ( datatype_v != null ) {
				datatype = datatype_v.get(i).trim();
				if ( !datatype.equals("") ) {
					ts.setDataType ( datatype );
				}
			}
			if ( units_v != null ) {
				units = units_v.get(i).trim();
				ts.setDataUnits ( units );
				ts.setDataUnitsOriginal ( units );
			}
			ts.setDate1 ( date1 );
			ts.setDate1Original ( date1 );
			ts.setDate2 ( date2 );
			ts.setDate2Original ( date2 );
			if ( description_v != null ) {
				description = description_v.get(i).trim();
				ts.setDescription ( description );
			}
			if ( missing_v != null ) {
				missing = missing_v.get(i).trim();
				if ( missing.equalsIgnoreCase("NaN") ) {
					ts.setMissing ( Double.NaN );
				}
				else if ( StringUtil.isDouble(missing) ) {
					ts.setMissing ( StringUtil.atod(missing) );
				}
			}
			if ( seqnum_v != null ) {
				seqnum = seqnum_v.get(i).trim();
				ts.setSequenceID ( seqnum );
			}
			if ( ts_has_data_flag[i] ) {
				// Data flags are being used.
				ts.hasDataFlags ( true, true );
			}
			if ( propertiesList != null ) {
				// Transfer the properties
				PropList props = propertiesList.get(i);
				for ( Prop prop : props.getList() ) {
					ts.setProperty(prop.getKey(), prop.getContents());
				}
			}
			if ( dataFlagMetadataList != null ) {
				// Transfer the data flag descriptions
				List<TSDataFlagMetadata> metaList = dataFlagMetadataList.get(i);
				for ( TSDataFlagMetadata meta : metaList ) {
					ts.addDataFlagMetadata(meta);
				}
			}
		}
	}
	catch ( Exception e ) {
	    message = "Unexpected error initializing time series.";
	    Message.printWarning(3, routine, message);
		Message.printWarning ( 3, routine, e );
		++warning_count;
	}
	if ( warning_count > 0 ) {
	    message = "" + warning_count + " errors occurred initializing time series.  Not reading data.";
	    Message.printWarning(3, routine, message);
	    // FIXME SAM 2008-04-14 Evaluate throwing more specific exception.
	    throw new Exception ( message );
	}
    if ( Message.isDebugOn ) {
        Message.printDebug ( 10, routine, "Read TS header" );
    }
	warning_count = 0; // Reset for reading data section below.

	// Check the header information.  If the data type has not been
	// specified but is included in the time series identifier, set in the data type...

	size = 0;
	if ( tslist != null ) {
		size = tslist.size();
	}
	for ( int i = 0; i < size; i++ ) {
		if ( ts.getDataType().trim().equals("") ) {
			ts.setDataType(ts.getIdentifier().getType());
		}
	}

	if ( !read_data ) {
		return tslist;
	}

	// Allocate the memory for the data array.  This needs to be done
	// whether a requested time series or list is being done...

	if ( req_ts != null ) {
		ts = ts_array[0];
		if ( ts.allocateDataSpace() != 0 ) {
		    message = "Error allocating data space for time series.";
			Message.printWarning( 3, routine, message );
			throw new Exception ( message );
		}
	}
	else {
	    for ( int i = 0; i < numts; i++ ) {
			ts = ts_array[i];
			if ( ts.allocateDataSpace() != 0 ) {
			    message = "Error allocating data space for time series.";
				Message.printWarning( 3, routine, message );
	            throw new Exception ( message );
			}
		}
	}

	// Now read the data.  Need to monitor if this is a real hog and optimize if so...
	warning_count = 0;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Reading data..." );
	}

	DateTime date;
	List<String> strings;
	double dvalue;	// Double data value
	String svalue;	// String data value
	boolean first = true;
	int nstrings = 0;
	boolean use_time = false;
	if ( (data_interval_base == TimeInterval.HOUR) || (data_interval_base == TimeInterval.MINUTE) ||
		(data_interval_base == TimeInterval.IRREGULAR) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Expect time to be given with dates - may be separate column." );
		}
		use_time = true;
	}
	// Compute the number of expected columns...
	int num_expected_columns = numts + 1;	// Number of expected columns
						// given the number of time
						// series, extra columns at the
						// front, data flag columns, and
						// a column for times
						// Column 1 is the date
	int num_extra_columns = 0;		// Number of extra columns at
						// the front of the data
						// (record count and total
						// time).
	if ( include_count ) {			// Record count
		++num_expected_columns;
		++num_extra_columns;
	}
	if ( include_total_time ) {		// Total time...
		++num_expected_columns;
		++num_extra_columns;
	}
	// Adjust the number of expected columns if data flags are included...
	int its = 0, i = 0;
	for ( its = 0; its < numts; its++ ) {
		if ( ts_has_data_flag[its] ) {
			++num_expected_columns;
		}
	}
	int first_data_column = 0;
	int num_expected_columns_p1 = num_expected_columns + 1;
	// Read lines until the end of the file...
	while ( true ) {
		try {
		if ( first ) {
			// Have read in the line above so process it in the
			// following code.  The line will either start with
			// "Date" or a date (e.g., MM/DD/YYYY), or will be
			// invalid.  Note that for some programs, the date and
			// all other columns actually have a suffix.  This may
			// be phased out at some time but is the reason why the
			// first characters are checked...
			first = false;
			if ( string.regionMatches(true,0,"date",0,4) ) {
				// Can ignore because it is the header line for columns...
				continue;
			}
		}
		else {
		    // Need to read a line...
			string = in.readLine();
			++line_count;
			if ( string == null ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Detected end of file." );
				}
				break;
			}
		}
		// Remove whitespace at front and back...
		string = string.trim();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl2, routine, "Processing: \"" + string + "\"" );
		}
		if ( (string.length() == 0) || ((string.length() > 0) && (string.charAt(0) == '#')) ) {
			// Skip comments and blank lines for now...
			continue;
		}
		if ( !Character.isDigit(string.charAt(0)) ) {
			// Not a data line...
			Message.printWarning ( 2, routine,
			"Error in data format for line " + line_count + ". Expecting number at start: \"" + string + "\"" );
			++warning_count;
			continue;
		}
		// Now parse the string...
		// If hour, or minute data, expect data line to be YYYY-MM-DD HH:MM Value
		// If there is a space between date and time, assume that the first two need to be concatenated.
		string = string.trim();
		if ( dataflag_v == null ) {
			// No data flags so parse without handling quoted strings.  This will in general be faster...
			strings = StringUtil.breakStringList ( string, delimiter, delimParseFlag );
		}
		else {
		    // Expect to have data flags so parse WITH handling quoted strings.  This will generally be slower...
			strings = StringUtil.breakStringList ( string,
			delimiter, delimParseFlag|StringUtil.DELIM_ALLOW_STRINGS );
		}
		nstrings = 0;
		if ( strings != null ) {
			nstrings = strings.size();
		}
		if ( nstrings == num_expected_columns ) {
			// Assume that there is NO space between date and time or that time field is not used...
			date_str = ((String)strings.get(0)).trim();	
			// Date + extra columns...
			first_data_column = 1 + num_extra_columns;
			req_ts_column2 = req_ts_column;
		}
		else if ( use_time && (nstrings == num_expected_columns_p1) ) {
			// Assume that there IS a space between the date and
			// time.  Concatenate together so that the DateTime.parse will work.
			date_str = ((String)strings.get(0)).trim() + " " + ((String)strings.get(1)).trim();	
			// Date + time + extra column...
			first_data_column = 2 + num_extra_columns;
			// Adjusted requested time series column...
			req_ts_column2 = req_ts_column + 1;
		}
		else {
		    Message.printWarning ( 2, routine, "Error in data format for line " + line_count + ". Have " +
		            nstrings + " fields using delimiter \"" + delimiter + "\" but expecting " +
		            num_expected_columns + ": \"" + string );
		    ++warning_count;
			//Message.printStatus ( 1, routine, "use_time=" + use_time + " num_expected_columns_p1=" +
			//num_expected_columns_p1 );
			// Ignore the line...
			strings = null;
			continue;
		}
		// Allow all common date formats, even if not the right precision...
		date = DateTime.parse(date_str);
		// The input line date may not have the proper resolution, so
		// set to the precision of the time series defined in the header.
		if ( data_interval_base == TimeInterval.MINUTE ) {
			date.setPrecision ( DateTime.PRECISION_MINUTE );
		}
		else if ( data_interval_base == TimeInterval.HOUR ) {
			date.setPrecision ( DateTime.PRECISION_HOUR );
		}
		else if ( data_interval_base == TimeInterval.DAY ) {
			date.setPrecision ( DateTime.PRECISION_DAY );
		}
		else if ( data_interval_base == TimeInterval.MONTH ) {
			date.setPrecision ( DateTime.PRECISION_MONTH );
		}
		else if ( data_interval_base == TimeInterval.YEAR ) {
			date.setPrecision ( DateTime.PRECISION_YEAR );
		}
		if ( date.lessThan(date1) ) {
			// No data of interest yet...
			strings = null;
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, "Ignoring data - before start date" );
			}
			continue;
		}
		else if ( date.greaterThan(date2) ) {
			// No need to keep reading...
			strings = null;
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, "Stop reading data - after start date" );
			}
			break;
		}

		// Else, save the data for each column...

		if ( req_ts != null ) {
			// Just have to process one column...
			svalue = ((String)strings.get(req_ts_column2)).trim();
			// This introduces a performance hit - maybe need to add a boolean array for each time series
			// to be able to check whether NaN is the missing - then can avoid the check.
			// For now just check the string.
			if ( svalue.equals("NaN") || (svalue == null) || (svalue.length() == 0)) {
			    // Treat the data value as missing.
				dvalue = ts_array[0].getMissing();
			}
			else {
			    // A numerical missing value like -999 will just get assigned.
			    dvalue = StringUtil.atod ( svalue );
			}
			if ( ts_has_data_flag[req_ts_i] ) {
				// Has a data flag...
				dataflag = ((String)strings.get( req_ts_column2 + 1)).trim();
				ts_array[0].setDataValue ( date, dvalue, dataflag, 1 );
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl2, routine, "For date " + date.toString() +
					", value=" + dvalue + ", flag=\"" +	dataflag + "\"" );
				}
			}
			else {	// No data flag...
				ts_array[0].setDataValue ( date, dvalue );
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl2, routine, "For date " + date.toString() + ", value=" + dvalue );
				}
			}
		}
		else {
		    // Loop through all the columns...
			for ( i = first_data_column, its = 0; i < nstrings; i++, its++ ) {
				// Set the data value in the requested time series.  If a requested time series is
				// being used, the array will only contain one time series, which is the requested time
				// series (SAMX 2002-09-05 so why the code above???)...
				//
				// This introduces a performance hit - maybe need to add a boolean array for each time
				// series to be able to check whether NaN is the missing - then can avoid the check.  For
				// now just check the string.
				svalue = ((String)strings.get(i)).trim();
				if ( svalue.equals("NaN") ) {
					dvalue = ts_array[its].getMissing();
				}
				else {
				    dvalue = StringUtil.atod ( svalue );
				}
				if ( ts_has_data_flag[its] ) {
					dataflag = ((String)
					strings.get(++i)).trim();
					ts_array[its].setDataValue ( date, dvalue, dataflag, 1 );
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl2, routine, "For date " + date.toString() +
						", value=" + dvalue + ", flag=\"" + dataflag + "\"" );
					}
				}
				else {
				    // No data flag...
					ts_array[its].setDataValue ( date, dvalue );
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl2, routine, "For date " + date.toString() + ", value=" + dvalue );
					}
				}
			}
		}

		// Clean up memory...

		strings = null;
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, "Unexpected error processing line " + line_count + ": \"" +
			        string + "\"" );
			Message.printWarning ( 3, routine, e );
			++warning_count;
		}
	}
	
	if ( warning_count > 0 ) {
	    message = "" + warning_count + " errors were detected reading data in file.";
	    Message.printWarning (2, routine, message);
	    // FIXME SAM 2008-04-14 Evaluate throwing a more specific exception
	    throw new Exception ( message );
	}

	//if ( Message::isDebugOn ) {
	//	long address = (long)ts;
	//	Message::printDebug ( 1, routine,
	//	ts->getIdentifierString() + " Read data for " +
	//	ts->getDate1().toString() + " Address " +
	//	String::valueOf(address) + " to " + ts->getDate2().toString());
	//}
	if ( req_ts != null ) {
		req_ts.addToGenesis ( "Read DateValue time series from " + ts.getDate1() + " to " + ts.getDate2() );
	}
	else {
	    for ( i = 0; i < numts; i++ ) {
			ts_array[i].addToGenesis ( "Read DateValue time series from " + ts.getDate1() + " to " + ts.getDate2() );
		}
	}
	return tslist;
}

/**
Write a time series to a DateValue format file.
@param ts single time series to write.
@param out PrintWriter to write to.
@exception Exception if an error occurs.
*/
public static void writeTimeSeries ( TS ts, PrintWriter out )
throws Exception
{	// Call the fully-loaded method...
	List<TS> v = new ArrayList<TS>( 1 );
	v.add ( ts );
	writeTimeSeriesList(v, out, (DateTime)null, (DateTime)null, null, true);
}

/**
Write a list of time series to a DateValue format file.
@param out PrintWrite to write to.
@param date1 First date to write (if null write the entire time series).
@param date2 Last date to write (if null write the entire time series).
@param units Units to write.  If different than the current units the units will be converted on output.
@param writeData Indicates whether data should be written (as opposed to only writing the header).
@exception Exception if an error occurs.
*/
public static void writeTimeSeries ( TS ts, PrintWriter out, DateTime date1, DateTime date2, String units, boolean writeData )
throws Exception
{	List<TS> v = new ArrayList<TS>( 1 );
	v.add ( ts );
	writeTimeSeriesList ( v, out, date1, date2, units, writeData );
}

/**
Write a time series to the specified file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@param ts Time series to write.
@param fname Name of file to write.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units will be converted on output.
@param writeData Indicates whether data should be written (as opposed to only writing the header).
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, String fname, DateTime date1, DateTime date2, String units, boolean writeData )
throws Exception
{   String routine = "DateValueTS.writeTimeSeries";

    String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
    try {
        FileOutputStream fos = new FileOutputStream( full_fname );
        PrintWriter fout = new PrintWriter ( fos );

        try {
            writeTimeSeries ( ts, fout, date1, date2, units, writeData );
        }
        finally {
            fout.close();
        }
    }
    catch ( Exception e ) {
        String message = "Error opening \"" + full_fname + "\" for writing.";
        Message.printWarning( 2, routine, message );
        throw new Exception (message);
    }
}

/**
Write a single time series to the specified file.  The entire period is written.
@param ts Time series to write.
@param fname Name of file to write.
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, String fname )
throws Exception
{   writeTimeSeries ( ts, fname, null, null, null, true );
}

/**
Write the data flag descriptions for a time series.
@param out PrintWriter to use for writing
@param ts time series for which to write data flag descriptions
@param its counter for time series (0+) to number the descriptions
*/
private static void writeTimeSeriesDataFlagDescriptions ( PrintWriter out, TS ts, int its )
{
	List<TSDataFlagMetadata> metaList = ts.getDataFlagMetadataList();
	if ( metaList.size() > 0 ) {
		StringBuilder b = new StringBuilder ( "DataFlagDescriptions_" + (its + 1) + " = {");
		TSDataFlagMetadata meta;
		for ( int iMeta = 0; iMeta < metaList.size(); iMeta++ ) {
			meta = metaList.get(iMeta);
			if ( iMeta > 0 ) {
				b.append(",");
			}
			// TODO SAM 2015-05-23 What to do if the data flag contains whitespace or characters that cause problems?  Quote?
			// What to do if the description contains double quotes?  Unlikely but possible.  For now remove.
			String desc = meta.getDescription().replace("\"","");
			b.append(meta.getDataFlag() + ":\"" + desc + "\"");
		}
		b.append("}");
		out.println(b.toString());
	}
}

/**
Write a list of time series to a DateValue format file.
Currently there is no way to indicate that the count or total time should be printed.
@param tslist list of time series to write.
@param out PrintWrite to write to.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units will be converted on output.
@param writeData Indicates whether data should be written (as opposed to only writing the header).
@exception Exception if there is an error writing the file (I/O error or invalid data).
*/
public static void writeTimeSeriesList ( List<TS> tslist, PrintWriter out,
    DateTime date1, DateTime date2, String units, boolean writeData )
throws Exception
{
    writeTimeSeriesList ( tslist, out, date1, date2, units, writeData, null );
}

/**
Write a list of time series to a DateValue format file.
Currently there is no way to indicate that the count or total time should be printed.
@param tslist list of time series to write.
@param out PrintWrite to write to.
@param date1 First date to write (if null write the entire time series).
@param date2 Last date to write (if null write the entire time series).
@param units Units to write.  If different than the current units the units will be converted on output.
@param writeData Indicates whether data should be written (as opposed to only writing the header).
@param props Properties to control output (see overloaded method for description).
@exception Exception if there is an error writing the file (I/O error or invalid data).
*/
public static void writeTimeSeriesList (List<TS> tslist, PrintWriter out, DateTime date1,
	DateTime date2, String units, boolean writeData, PropList props )
throws Exception
{	String message, routine = "DateValueTS.writeTimeSeriesList";
	DateTime outputStart = null, outputEnd = null, t = new DateTime( DateTime.DATE_FAST );

	// Check for a null time series list...

	if ( tslist == null ) {
		message = "Null time series list.  Output will be empty.";
		Message.printWarning ( 2, routine, message );
	}

	int size = 0;
	if ( tslist != null ) {
	    size = tslist.size();
	}
	if ( size == 0 ) {
		message = "No time series in list.  Output will be empty.";
		Message.printWarning ( 2, routine, message );
	}
	
	// Make sure that a non-null properties list is available
	if ( props == null ) {
	    props = new PropList ( "DateValueTS" );
	}

	// TODO SAM 2012-04-04 Eventually need to support intervals like IrregularMinute
	// Interval used with irregular time series.
	// This is needed because depending on how the irregular time series was initialized, its dates may
	// not properly indicate the precision.  For example, a DateTime precision (and interval) of IRREGULAR
	// may be used in cases where the data were read from a data format where the precision could not be
	// determined.
	TimeInterval irregularInterval = null;
	Object obj = props.getContents( "IrregularInterval" );
	if ( obj != null ) {
	    irregularInterval = (TimeInterval)obj;
	}
    String version = props.getValue( "Version" );
    boolean version14 = false; // Currently the only version that is supported other than current version
    int versionInt = __VERSION_CURRENT_INT;
    if ( version != null ) {
        if ( version.equals("1.4") ) {
            version14 = true;
        }
        versionInt = Integer.parseInt(version.trim().replace(".","")) * 1000;
    }

	// Set the parameters for output..

	TSLimits limits = new TSLimits ();
	if ( size > 0 ) {
    	if ( (date1 == null) || (date2 == null) ) {
    		// Get the limits...
    		try {
    		    limits = TSUtil.getPeriodFromTS ( tslist, TSUtil.MAX_POR );
    		}
    		catch ( Exception e ) {}
    	}
    	if ( date1 == null ) {
    		// Use the maximum period in the time series list...
    		outputStart = new DateTime ( limits.getDate1() );
    	}
    	else {
    	    outputStart = new DateTime ( date1 );
    	}
    
    	if ( date2 == null ) {
    		// Use the time series value...
    		outputEnd = new DateTime ( limits.getDate2() );
    	}
    	else {
    	    outputEnd = new DateTime ( date2 );
    	}
	}

	// Loop through the time series and make sure they have the same interval...

 	int dataIntervalBase = TimeInterval.UNKNOWN;
	int dataIntervalMult = 0;
	int iDataIntervalBase = 0;
	int iDataIntervalMult = 0;

	TS ts = null;
	// Set up conversion factors for units (apparently some compilers don't
	// like allocating one slot so always make it more and ignore the extra value)...
	double mult[] = new double[size + 1];
	double add[] = new double[size + 1];
	DataUnitsConversion conversion;
	int nonNullCount = 0;
	for ( int i = 0; i < size; i++ ) {
		mult[i] = 1.0;
		add[i] = 0.0;
		ts = tslist.get(i);
		if ( ts != null ) {
		    ++nonNullCount;
			iDataIntervalBase = ts.getDataIntervalBase();
			iDataIntervalMult = ts.getDataIntervalMult();
    		if ( nonNullCount == 1 ) {
    		    // First non-null time series so initialize interval
    			dataIntervalBase = iDataIntervalBase;
    			dataIntervalMult = iDataIntervalMult;
    		}
    		else if ( (dataIntervalBase != iDataIntervalBase) || (dataIntervalMult != iDataIntervalMult) ) {
    			message = "Time series do not have the same interval.  Can't write";
    			Message.printWarning ( 2, routine, message );
    			throw new UnequalTimeIntervalException ( message );
    		}
		}
		// Get the conversion factors to use for output.  Don't call
		// TSUtil.convertUnits because we don't want to alter the time series itself...
		if ( (ts != null) && (units != null) && (units.length() != 0) &&
			!units.equalsIgnoreCase(ts.getDataUnits()) ) {
			try {
			    conversion = DataUnits.getConversion ( ts.getDataUnits(), units );
				mult[i] = conversion.getMultFactor();
				add[i] = conversion.getAddFactor();
			}
			catch ( Exception e ) {
				Message.printWarning( 2, routine,
				"Unable to convert units to \"" + units + "\" leaving units as \"" + ts.getDataUnits() + "\"" );
			}
		}
	}

    // Write header.  See the printSample() method for an example string...

	String delim = " ";
	String propval = props.getValue ( "Delimiter" );
	if ( propval != null ) {
	    delim = propval;
	}
    int precision = 4;
    propval = props.getValue ( "Precision" );
    if ( (propval != null) && StringUtil.isInteger(propval) ) {
        precision = Integer.parseInt(propval);
    }
    // Override of missing value in the time series
    String missingValueString = props.getValue ( "MissingValue" );
    if ( (missingValueString != null) && !StringUtil.isDouble(missingValueString) ) {
        Message.printWarning ( 3, routine,
            "Specified missing value \"" + missingValueString + "\" is not a number - ignoring." );
        missingValueString = null;
    }
    // Indicate which properties should be written
    Object includePropertiesProp = props.getContents ( "IncludeProperties" );
    String [] includeProperties = null;
    if ( includePropertiesProp != null ) {
    	includeProperties = (String [])includePropertiesProp;
    	for ( int i = 0; i < includeProperties.length; i++ ) {
    		includeProperties[i] = includeProperties[i].replace("*", ".*"); // Change glob notation to Java regular expression
    	}
    }
    // Indicate whether data flag descriptions should be written
    String writeDataFlagDescriptions0 = props.getValue ( "WriteDataFlagDescriptions" );
    boolean writeDataFlagDescriptions = false; // default
    if ( (writeDataFlagDescriptions0 != null) && writeDataFlagDescriptions0.equalsIgnoreCase("true") ) {
    	writeDataFlagDescriptions = true;
    }
    String outputFormat = "%." + precision + "f";
	String nodataString = "?";
	StringBuffer aliasBuffer = new StringBuffer();
	boolean hasSeqnum = false;
	StringBuffer seqnumBuffer = new StringBuffer();
	StringBuffer dataflagBuffer = new StringBuffer();
	StringBuffer columnsBuffer = new StringBuffer();
	StringBuffer datatypeBuffer = new StringBuffer();
	StringBuffer descriptionBuffer = new StringBuffer();
	StringBuffer missingvalBuffer = new StringBuffer();
	StringBuffer tsidBuffer = new StringBuffer();
	StringBuffer unitsBuffer = new StringBuffer();

	if ( dataIntervalBase == TimeInterval.IRREGULAR ) {
	    // Check the start date/time to see if the data precision includes time.  If no start is
	    // defined, assume no time - will not matter since no data
	    String header = "Date" + delim;
	    if ( size == 1 ) {
	        ts = tslist.get(0);
	        if ( ts != null ) {
	            DateTime start = ts.getDate1();
	            if ( start != null ) {
	                int p = start.getPrecision();
	                if ( (p == DateTime.PRECISION_HOUR) || (p == DateTime.PRECISION_MINUTE) |
	                    (p == DateTime.PRECISION_SECOND) ) {
	                    header += "Time" + delim;
	                }
	            }
	        }
	    }
	    columnsBuffer.append ( header );
	}
	else if ( (dataIntervalBase == TimeInterval.MINUTE) || (dataIntervalBase == TimeInterval.HOUR) ) {
		columnsBuffer.append ( "Date" + delim + "Time" + delim );
	}
	else {
	    columnsBuffer.append ( "Date" + delim );
	}
	boolean hasDataFlags = false; // Only include data flags in output if
					// at least one time series actually has the flag.
	for ( int i = 0; i < size; i++ ) {
		ts = tslist.get(i);
		if ( i != 0 ) {
			// Append the delimiter...
			aliasBuffer.append ( delim );
			seqnumBuffer.append ( delim );
			columnsBuffer.append ( delim );
			dataflagBuffer.append ( delim );
			datatypeBuffer.append ( delim );
			descriptionBuffer.append ( delim );
			missingvalBuffer.append ( delim );
			tsidBuffer.append ( delim );
			unitsBuffer.append ( delim );
		}
		// Now add the data...
		if ( ts == null ) {
			aliasBuffer.append ( "\"" + nodataString + "\"" );
			seqnumBuffer.append ( "\"" + nodataString + "\"" );
			columnsBuffer.append ( nodataString );
			dataflagBuffer.append ( "\"" + nodataString + "\"" );
			datatypeBuffer.append ( nodataString );
			descriptionBuffer.append (	"\"" + nodataString + "\"" );
			missingvalBuffer.append ( nodataString );
			tsidBuffer.append ( "\"" + nodataString + "\"" );
			unitsBuffer.append ( nodataString );
		}
		else {
		    String alias = ts.getAlias();
		    aliasBuffer.append ( "\"" + alias + "\"" );
			if ( (ts.getSequenceID() != null) && !ts.getSequenceID().equals("") ) {
				// At least one time series has the sequence number so it will be output below.
				hasSeqnum = true;
			}
			if ( (ts.getSequenceID() == null) || ts.getSequenceID().equals("") ) {
			    if ( version14 ) {
			        seqnumBuffer.append ( "-1");
			    }
			    else {
			        seqnumBuffer.append ( "\"\"");
			    }
			}
			else {
			    if ( version14 ) {
			        // Used integer sequence numbers, so output -1 if not an integer
			        try {
			            Integer.parseInt(ts.getSequenceID());
			            seqnumBuffer.append ( ts.getSequenceID() );
			        }
			        catch ( NumberFormatException e ) {
			            seqnumBuffer.append ( "-1");
			        }
			    }
			    else {
			        seqnumBuffer.append ( "\"" + ts.getSequenceID() + "\"");
			    }
			}
			if ( !ts.getDataUnits().trim().equals("") ) {
				// Has units so display in column heading...
			    if ( alias.length() > 0 ) {
			        // Use the alias.
    				columnsBuffer.append ( "\"" + alias + ", " + ts.getDataUnits() + "\"" );
			    }
			    else {
	                 columnsBuffer.append ( "\"" + ts.getIdentifier().toString() + ", " + ts.getDataUnits() + "\"" );
			    }
			}
			else {
			    if ( alias.length() > 0 ) {
			        columnsBuffer.append ( "\"" + alias + "\"" );
			    }
			    else {
			        columnsBuffer.append ( "\"" + ts.getIdentifier().toString() + "\"" );
			    }
			}
			if ( ts.hasDataFlags() ) {
				hasDataFlags = true;
				dataflagBuffer.append ( "true" );
				columnsBuffer.append ( delim );
				columnsBuffer.append ( "DataFlag" );
			}
			else {
			    dataflagBuffer.append ( "false" );
			}
			if ( ts.getDataType().trim().equals("") ) {
				datatypeBuffer.append("\"" + ts.getIdentifier().getType() + "\"" );
			}
			else {
			    datatypeBuffer.append("\"" + ts.getDataType() + "\"" );
			}
			descriptionBuffer.append (	"\"" + ts.getDescription() + "\"" );
			// If the missing value is NaN, just print NaN.  Otherwise the %.Nf results in NaN.000...
			// The following is a trick to check for NaN...
			if ( missingValueString != null ) {
			    // Property has specified the missing value to use
			    missingvalBuffer.append ( missingValueString );
			}
			else {
    			if ( ts.getMissing() != ts.getMissing() ) {
    				missingvalBuffer.append ("NaN" );
    			}
    			else {
    			    // Assume that missing is indicated by a number...
    				missingvalBuffer.append ( StringUtil.formatString(	ts.getMissing(),outputFormat));
    			}
			}
			tsidBuffer.append ( "\"" +	ts.getIdentifier().toString() + "\"" );
			unitsBuffer.append ( "\"" + ts.getDataUnits() + "\"" );
		}
	}

	// Print the standard header...

	if ( version14 ) {
	    out.println ( "# DateValueTS 1.4 file" );
	}
	else {
	    out.println ( "# DateValueTS " + __VERSION_CURRENT + " file" );
	}
	IOUtil.printCreatorHeader ( out, "#", 80, 0 );
	Object o = props.getContents("OutputComments");
	if ( o != null ) {
	    // Write additional comments that were passed in
	    @SuppressWarnings("unchecked")
		List<String> comments = (List<String>)o;
	    int commentSize = comments.size();
	    if ( commentSize > 0 ) {
    	    for ( int iComment = 0; iComment < commentSize; iComment++ ) {
    	        out.println ( "# " + comments.get(iComment) );
    	    }
	    }
	}
	out.println ( "#" );
	out.println ( "Delimiter   = \"" + delim + "\"" );
	out.println ( "NumTS       = " + size );
	out.println ( "TSID        = " + tsidBuffer.toString() );
	out.println ( "Alias       = " + aliasBuffer.toString() );
	if ( hasSeqnum ) {
	    if ( version14 ) {
	        // Format 1.4, where sequence number is an integer, with -1 indicating no sequence number
	        out.println ( "SequenceNum = " + seqnumBuffer.toString() );
	    }
	    else {
	        // Format as of 1.5, where sequence ID is a string
	        out.println ( "SequenceID  = " + seqnumBuffer.toString() );
	    }
	}
	out.println ( "Description = " + descriptionBuffer.toString() );
	out.println ( "DataType    = " + datatypeBuffer.toString() );
	out.println ( "Units       = " + unitsBuffer.toString() );
	out.println ( "MissingVal  = " + missingvalBuffer.toString() );
	if ( hasDataFlags ) {
		// At least one of the time series in the list has data flags
		// so output the data flags information for all the time series...
		out.println ( "DataFlags   = " + dataflagBuffer.toString() );
	}
	if ( versionInt >= 16000 ) {
		// Writing time series properties and data flag descriptions was added to version 1.6
		if ( includeProperties != null ) {
			for ( int its = 0; its < tslist.size(); its++ ) {
				ts = tslist.get(its);
				if ( ts == null ) {
					continue;
				}
				else {
					// Output the properties
					writeTimeSeriesProperties(out,ts,its,includeProperties);
				}
			}
		}
		if ( writeDataFlagDescriptions ) {
			for ( int its = 0; its < tslist.size(); its++ ) {
				ts = tslist.get(its);
				if ( ts == null ) {
					continue;
				}
				else {
					writeTimeSeriesDataFlagDescriptions(out, ts, its);
				}
			}
		}
	}
	if ( size == 0 ) {
	    out.println ( "# Unable to determine data start and end - no time series." );
	}
	else {
        out.println ( "Start       = " + outputStart.toString() );
        out.println ( "End         = " + outputEnd.toString() );
	}

	// Print the comments/genesis information...

	out.println ( "#" );
	out.println ( "# Time series comments/histories:" );
	out.println ( "#" );

	String printGenesis = "true";
	List<String> genesis = null;
	List<String> comments = null;
	int j = 0, jsize = 0;
	for ( int i = 0; i < size; i++ ) {
		ts = tslist.get(i);
		if ( ts == null ) {
			out.println ( "#" );
			out.println ( "# Time series " + (i + 1) + " is null");
			out.println ( "#" );
			continue;
		}
		comments = ts.getComments();
		genesis = ts.getGenesis();
		if ( ((comments == null) || (comments.size() == 0)) && ((genesis == null) || (genesis.size() == 0)) ) {
			out.println ( "#" );
			out.println ( "# Time series " + (i + 1) + " (TSID=" +
			ts.getIdentifier().toString() + " Alias=" + ts.getAlias() + ") has no comments or history" );
			out.println ( "#" );
			continue;
		}
		if ( (comments != null) && (comments.size() > 0) ) {
			out.println ( "#" );
			out.println ( "# Comments for time series " + (i + 1) +
			" (TSID=" + ts.getIdentifier().toString() + " Alias=" +
			ts.getAlias() + "):" );
			out.println ( "#" );
			jsize = comments.size();
			for ( j = 0; j < jsize; j++ ) {
				out.println ( "#   " + comments.get(j) );
			}
		}
		if ( (genesis != null) && (genesis.size() > 0) && printGenesis.equalsIgnoreCase("true") ) {
			out.println ( "#" );
			out.println ( "# Creation history for time series " +
			(i + 1) + " (TSID=" + ts.getIdentifier().toString() + " Alias=" + ts.getAlias() + "):" );
			out.println ( "#" );
			jsize = genesis.size();
			for ( j = 0; j < jsize; j++ ) {
				out.println ( "#   " + genesis.get(j) );
			}
		}
	}
	out.println ( "#" );
	out.println ( "#EndHeader" );

	if ( !writeData ) {
		// Don't want to write the data...
		return;
	}

	// Header line indicating data columns....

	out.println ( columnsBuffer.toString() );
	
	double value = 0.0;
	String string_value;

	// Need to add iterator at some point - could use this to test performance...
	StringBuffer buffer = new StringBuffer();
	TSData datapoint = new TSData(); // Data point associated with a date - used to get flags.
	String dataflag; // Data flag associated with a data point.
	if ( dataIntervalBase == TimeInterval.IRREGULAR ) {
		// Irregular interval... loop through all of the values...
		// This assumes that _date1 and _date2 have been set.
	    if ( size == 1 ) {
	        // Legacy logic that works with one irregular time series (should be fast)
    		IrregularTS its = null;
    		List<TSData> alldata = null;
    		if ( ts != null ) {
    			its = (IrregularTS)ts;
    			alldata = its.getData();
    		}
    		if ( alldata == null ) {
    			return;
    		}
    		int dataSize = alldata.size();
    		TSData tsdata = null;
    		DateTime date;
    		buffer = new StringBuffer();
    		for ( int i = 0; i < dataSize; i++ ) {
    		    buffer.setLength(0);
    			tsdata = alldata.get(i);
    			if ( tsdata == null ) {
    				break;
    			}
    			date = tsdata.getDate();
    			if ( date.lessThan(outputStart) ) {
    				continue;
    			}
    			else if ( date.greaterThan(outputEnd) ) {
    				break;
    			}
    			// Else print the record...
    			value = tsdata.getDataValue();
    			if ( ts.isDataMissing(value) ) {
    		         if ( missingValueString != null ) {
    	                // Property has specified the missing value to use
    	                string_value = missingValueString;
    		         }
    		         else {
        				if ( Double.isNaN(value) ) {
        					string_value = "NaN";
        				}
        				else {
        				    string_value = StringUtil.formatString( tsdata.getDataValue(), outputFormat );
        				}
    		        }
    			}
    			else {
    			    // Convert the units...
    				string_value = StringUtil.formatString( (tsdata.getDataValue()*mult[0] + add[0]), outputFormat );
    			}
    			// Use the precision of the dates in the data - ISO formats will be used by default...
    			buffer.append ( date.toString() );
    			buffer.append ( delim );
    			buffer.append ( string_value );
                // Now print the data flag...
                if ( ts.hasDataFlags() ) {
                    dataflag = tsdata.getDataFlag();
                    // Always enclose the data flag in quotes because it may contain white space...
                    buffer.append ( delim );
                    buffer.append ( "\"" );
                    buffer.append ( dataflag );
                    buffer.append ( "\"" );
                }
                out.println ( buffer.toString() );
    		}
	    }
	    else {
	        // More than one irregular time series.  They at least have to have the same date/time precision
	        // for the period.  Otherwise it will be difficult to navigate the data.  For now develop this code
	        // separately but if the logic works out, it should be able to replace the above block of code.
	        int irrPrecision = -1;
	        int tsPrecision;
	        if ( irregularInterval != null ) {
	            // Use the precision that was specified
	            irrPrecision = irregularInterval.getBase();
	        }
	        else {
    	        for ( int its = 0; its < size; its++ ) {
                    if ( tslist.get(its) == null ) {
                        continue;
                    }
                    ts = (IrregularTS)tslist.get(its);
                    if ( ts.getDate1() == null ) {
                        continue;
                    }
                    tsPrecision = ts.getDate1().getPrecision();
                    if ( tsPrecision == TimeInterval.IRREGULAR ) {
                        // Treat as minute
                        tsPrecision = DateTime.PRECISION_MINUTE;
                    }
                    if ( irrPrecision == -1 ) {
                        // Just assign
                        irrPrecision = tsPrecision;
                    }
                    else if ( irrPrecision != tsPrecision ) {
                        // This will be a problem in processing the data
                        message = "Irregular time series do not have the same date/time precision.  Can't write";
                        Message.printWarning ( 2, routine, message );
                        throw new UnequalTimeIntervalException ( message );
                    }
    	        }
	        }
	        if ( irrPrecision == -1 ) {
	            // Apparently no non-null time series with data
                message = "Irregular time series do not have data to determine date/time precision (all empty?).  Can't write";
                Message.printWarning ( 2, routine, message );
                throw new IllegalArgumentException ( message );
	        }
            // Was able to determine the precision of data so can continue
	        // The logic works as follows:
	        // 0) Advance the iterator for each time series to initialize
	        // 1) Find the earliest date/time in the iterator current position
	        // 2) Output the values at the earliest date/time order
	        //    - actual value if time series has a value at the date/time
	        //    - values not at the same date/time result in blanks for the other time series
	        // 3) For any values printed, advance that time series' iterator
	        // 4) Go to step 1
	        // Create iterators for each time series
	        List<TSIterator> tsIteratorList = new ArrayList<TSIterator>(size);
	        for ( int its = 0; its < size; its++ ) {
	            if ( tslist.get(its) == null ) {
	                tsIteratorList.add(null); // Keep same order as time series
	            }
	            ts = (IrregularTS)tslist.get(its);
	            try {
	                tsIteratorList.add(ts.iterator(outputStart,outputEnd));
	            }
	            catch ( Exception e ) {
	                tsIteratorList.add(null); // Keep same order as time series
	            }
            }
	        int its;
	        TSIterator itsIterator;
	        DateTime dtEarliest;
	        // Use the following to extract data from each time series
	        // A call to the iterator next() method will return null when no more data, which is
	        // the safest way to process the data
	        TSData [] tsdata = new TSData[size];
	        DateTime dt;
	        int loopCount = 0;
	        DateTime dtEarliestOutput = new DateTime(irrPrecision); // Use for output to ensure precision
	        while ( true ) {
	            // Using the current date/time, output the earliest value for all time series that have the value and
	            // increment the iterator for each value that is output.
                dtEarliest = null;
                ++loopCount;
                if ( loopCount == 1 ) {
                    // Need to call next() one time on all time series to initialize all iterators to the first
                    // data point in the time series.  Otherwise, next() is only called below
                    // when an actual time series value is output.
                    for ( its = 0; its < size; its++ ) {
                        itsIterator = tsIteratorList.get(its);
                        if ( itsIterator != null ) {
                            tsdata[its] = itsIterator.next();
                        }
                    }
                }
                // Figure out the earliest date/time to output from the current iterator data
                for ( its = 0; its < size; its++ ) {
                    if ( tsdata[its] == null ) {
                        continue;
                    }
                    dt = tsdata[its].getDate();
                    if ( dt != null ) {
                        if ( dtEarliest == null ) {
                            dtEarliest = dt;
                        }
                        else if ( dt.lessThan(dtEarliest) ) {
                            dtEarliest = dt;
                        }
                    }
                }
                if ( dtEarliest == null ) {
                    // Done printing data records
                    break;
                }
                // Make sure the date/time for output is set the proper precision for equals() calls and formatting
                dtEarliestOutput.setDate(dtEarliest);
                dtEarliestOutput.setPrecision(irrPrecision);
	            // First print the date/time
	            buffer.setLength(0);
	            buffer.append ( "" + dtEarliestOutput );
	            for ( its = 0; its < size; its++ ) {
	                dt = null;
	                if ( tsdata[its] != null ) {
	                    dt = tsdata[its].getDate();
	                }
	                if ( (dt != null) && dtEarliestOutput.equals(dt) ) {
	                    // Output the value and if requested the flag (this copied from below for regular time series)
	                    if ( ts != null ) {
	                        value = tsdata[its].getDataValue();
	                    }
	                    if ( (ts == null) || ts.isDataMissing(value) ) {
	                        if ( missingValueString != null ) {
	                            // Property has specified the missing value to use
	                            string_value = missingValueString;
	                        }
	                        else {
	                            if ( Double.isNaN(value) ) {
	                                string_value = "NaN";
	                            }
	                            else {
	                                // Format the missing value number
	                                string_value = StringUtil.formatString( value, outputFormat);
	                            }
	                        }
	                    }
	                    else {
	                        // Format the data value
	                        string_value = StringUtil.formatString( (value*mult[its] + add[its]),outputFormat );
	                    }
	                    if ( its == 0 ) {
	                        buffer.append ( string_value );
	                    }
	                    else {
	                        buffer.append ( delim + string_value );
	                    }
	                    // Now print the data flag...
	                    if ( ts.hasDataFlags() ) {
	                        dataflag = tsdata[its].getDataFlag();
	                        // Always enclose the data flag in quotes because it may contain white space...
	                        buffer.append ( delim + "\""+ dataflag + "\"");
	                    }
	                    // Get the next value since this time series was able to output
	                    // Advancing past data will result in null for calls to request the date, etc.
	                    itsIterator = tsIteratorList.get(its);
	                    tsdata[its] = itsIterator.next();
	                }
	                else {
	                    // Output blanks (quoted blanks for illustration)
	                    buffer.append ( delim + "" );
	                    if ( ts.hasDataFlags() ) {
	                        buffer.append ( delim + "\"" + "" + "\"" );
	                    }
	                    // Keep the iterator at the same spot so that the value will be tested for order
	                }
	            }
	            // Output the line
	            out.println ( buffer.toString () );
	        }
        }
	}
	else if ( (dataIntervalBase != TimeInterval.IRREGULAR) && (outputStart != null) && (outputEnd != null) ) {
	    // Regular interval and have period to output...
		t = new DateTime ( outputStart);
		// Make sure no time zone is set to minimize output...
		t.setTimeZone ("");
		int its;
		for ( ;	t.lessThanOrEqualTo(outputEnd); t.addInterval(dataIntervalBase, dataIntervalMult)) {
			buffer.setLength(0);
			//buffer.append( t.toString().replace(' ','@') + delim);
			buffer.append( t.toString() + delim );
			for ( its = 0; its < size; its++ ) {
				ts = tslist.get(its);
				// Need to work on formatting number to a better precision.  For now just get to
				// output without major loss in precision...
				if ( ts != null ) {
					value = ts.getDataValue(t);
				}
				if ( (ts == null) || ts.isDataMissing(value) ) {
		            if ( missingValueString != null ) {
		                // Property has specified the missing value to use
		                string_value = missingValueString;
		            }
		            else {
    					if ( Double.isNaN(value) ) {
    						string_value = "NaN";
    					}
    					else {
    					    // Format the missing value number
    					    string_value = StringUtil.formatString( value, outputFormat);
    					}
		            }
				}
				else {
				    // Format the data value
				    string_value = StringUtil.formatString(	(value*mult[its] + add[its]),outputFormat );
				}
				if ( its == 0 ) {
					buffer.append ( string_value );
				}
				else {
				    buffer.append ( delim + string_value );
				}
				// Now print the data flag...
				if ( ts.hasDataFlags() ) {
					datapoint = ts.getDataPoint ( t, datapoint );
					dataflag = datapoint.getDataFlag();
					// Always enclose the data flag in quotes because it may contain white space...
					buffer.append ( delim +	"\""+ dataflag + "\"");
				}
			}
			out.println ( buffer.toString () );
		}
	}
}

/**
Write multiple time series to a single file.  This is useful when the time
series are to be read into a spreadsheet.  The standard DateValue time series
header is used, but each data item is separated by a | delimiter.  The time
series must have the same interval and the overall output period will be that
of the maximum bounds of the time series.  The following additional data
properties are included in the header:
<pre>
# Indicate the number of time series in the file.
NumTS = #
</pre>
@param tslist list of time series.
@param fname file name to write.
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeriesList ( List<TS> tslist, String fname )
throws Exception
{	writeTimeSeriesList ( tslist, fname, (DateTime)null, (DateTime)null, null, true );
}

/**
Write a list of time series to the specified file.
@param tslist list of time series to write.
@param fname Name of file to write.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units will be converted on output.
@param write_data Indicates whether data should be written (as opposed to only writing the header).
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeriesList (List<TS> tslist, String fname,
                    DateTime date1, DateTime date2, String units, boolean write_data )
throws Exception
{
    writeTimeSeriesList ( tslist, fname, date1, date2, units, write_data, null );
}

/**
Write a list of time series to the specified file.
@param tslist list of time series to write.
@param fname Name of file to write.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units will be converted on output.
@param writeData Indicates whether data should be written (as opposed to only writing the header).
@param props Properties to control output, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td> <td><b>Description</b></td> <td><b>Default</b></td>
</tr>

<tr>
<td><b>Delimiter</b></td>
<td><b>The delimiter to use in output.</b>
<td>Space</td>
</tr>

<tr>
<td><b>IncludeProperties</b></td>
<td><b>An array of strings indicating properties to write, with * to use glob-style pattern matching.</b>
<td>Do not write any properties.</td>
</tr>

<tr>
<td><b>IrregularInterval</b></td>
<td><b>The TimeInterval that indicates the interval for date/times when outputting more than one irregular time series</b>
<td>Determine from time series list (precision of starting date/time).</td>
</tr>

<tr>
<td><b>MissingValue</b></td>
<td><b>The missing value to be output for numerical values.</b>
<td>4</td>
</tr>

<tr>
<td><b>OutputComments</b></td>
<td><b>Additional comments to be output in the header, as a list of String.  The comment
lines are not added to in any way.</b>
<td>No additional comments.</td>
</tr>

<tr>
<td><b>Precision</b></td>
<td><b>The precision (number of digits after the decimal) to use for numerical values.</b>
<td>4</td>
</tr>

<tr>
<td><b>Version</b></td>
<td><b>The DateValue file format version.</b>
<td>Current version.</td>
</tr>

<tr>
<td><b>WriteDataFlagDescriptions</b></td>
<td><b>A string "true" or "false" indicating whether data flag descriptions should be written.</b>
<td>false (to adhere to legacy behavior)</td>
</tr>

</table>
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeriesList (List<TS> tslist, String fname,
	DateTime date1, DateTime date2, String units, boolean writeData, PropList props )
throws Exception
{	String	routine = "DateValueTS.writeTimeSeriesList";

	String full_fname = IOUtil.getPathUsingWorkingDir(fname);
	try {
	    FileOutputStream fos = new FileOutputStream ( full_fname );
		PrintWriter fout = new PrintWriter ( fos );

		try {
		    writeTimeSeriesList ( tslist, fout, date1, date2, units, writeData, props );
		}
		finally {
		    fout.close();
		}
	}
	catch ( UnequalTimeIntervalException e ) {
	    // Just rethrow because message will be specific
	    throw e;
	}
	catch ( Exception e ) {
		String message = "Error writing \"" + full_fname + "\" (" + e + ").";
		Message.printWarning( 2, routine, message );
		Message.printWarning( 3, routine, e );
		throw new Exception (message);
	}
}

/**
Write the properties for a time series.
*/
private static void writeTimeSeriesProperties ( PrintWriter out, TS ts, int its, String [] includeProperties )
{
	// Get the list of matching properties
	// TODO SAM 2015-05-18 Add support for wildcards - for now must match exactly
	Object o;
	StringBuilder b = new StringBuilder ( "Properties_" + (its + 1) + " = {");
	// Get all the properties.  Then extract the properties that match the IncludeProperties list
	HashMap<String,Object> props = ts.getProperties();
	List<String> matchedProps = new ArrayList<>();
	// Loop through the full list of properties and get those that match the pattern
	for ( int iprop = 0; iprop < includeProperties.length; iprop++ ) {
		for ( String key: props.keySet() ) {
			if ( key.matches(includeProperties[iprop]) ) {
				// The included property matches a property in the time series.
				// Make sure the property is not already in the list to include.
				// - could happen if multiple patterns were provided that return the same property names.
				boolean match = false;
				for ( String p : matchedProps ) {
					if ( p.equals(key) ) {
						match = true;
						break;
					}
				}
				if ( !match ) {
					matchedProps.add(key);
				}
			}
		}
	}
	// Output the properties that were requested and match and actual property.
	int iprop = -1;
	for ( String p : matchedProps ) {
		++iprop;
		o = ts.getProperty(p);
		if ( iprop > 0 ) {
			// Not first so append a comma to separate properties.
			b.append(",");
		}
		// Append the property name.
		b.append(p+":");
		// Append the property value.
		if ( o == null ) {
			b.append("null");
		}
		else if ( o instanceof Double ) {
			// Don't want default of exponential notation so always format
			b.append(StringUtil.formatString((Double)o,"%.6f"));
		}
		else if ( o instanceof Float ) {
			// Don't want default of exponential notation so always format
			b.append(StringUtil.formatString((Float)o,"%.6f"));
		}
		else if ( o instanceof Integer ) {
			b.append("" + o);
		}
		else if ( o instanceof Long ) {
			b.append("" + o);
		}
		else if ( o instanceof String ) {
			b.append("\""+o+"\"");
		}
		else {
			// Don't specifically handle the type so treat as a string.
			// TODO SAM 2015-05-18 this may cause problems if it contains newlines
			b.append("\""+o+"\"");
		}
	}
	b.append("}");
	out.println(b.toString());
}

}
