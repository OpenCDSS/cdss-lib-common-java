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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Vector;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.DataUnitsConversion;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

public class DateValueTS {

/**
Return a sample so that a user/developer knows what a file looks like.
Currently, the samples are compiled into the code to make absolutely sure that
the programmer knows what sample is supported.  Perhaps they could be stored in
sample files in the future.  
@return Sample file contents.
*/
public static Vector getSample ()
{	Vector	s = new Vector ( 50 );
	s.addElement ("# DateValueTS 1.3 file" );
	s.addElement ("#" );
	s.addElement ("# This is a sample of a typical DateValue minute time series.  This format");
	s.addElement ("# was developed by Riverside Technology, inc. to store time series data.  An");
	s.addElement ("# example file is as follows and conforms to the following guidelines:");
	s.addElement ("#" );
	s.addElement ("# * Comments are lines that start with #." );
	s.addElement ("# * Applications often add a comments section at the top indicating how the");
	s.addElement ("#   file was created" );
	s.addElement ("# * Any line that starts with a number is assumed to be a data line." );
	s.addElement ("# * Date hours should be in the range 0 to 23 (an hour of 24 will be" );
	s.addElement ("#   converted to hour 0 of the next day)." );
	s.addElement ("# * If a time is necessary, the date/time may be separated by a space, T, : or @.");
	s.addElement ("#   If a space is used, use a date and time column heading,");
	s.addElement ("#   if headings are used.");
	s.addElement ("# * The same general format is used for year, month, day, hour, and minute" );
	s.addElement ("#   data, except the format of the date is adjusted accordingly." );
	s.addElement ("# * If multiple time series are written, header variables are delimited with");
	s.addElement ("#   space or tab characters.  Data are delimited by tab or space (or use the");
	s.addElement ("#   Delimiter property to set the delimiters used for data lines)" );
	s.addElement ("# * Internally, the time series identifier is initially set using the file");
	s.addElement ("#   name.  For example, a file name of XXX.USGS.Streamflow.MONTH will result");
	s.addElement ("#   in the location being set to \"XXX\", the data source to \"USGS\", the data");
	s.addElement ("#   type to \"Streamflow\", and the interval to 1 month.  The identifier");
	s.addElement ("#   information is reset if individual properties are specified in the file.");
	s.addElement ("# * This format is free-format and additional information may be added in");
	s.addElement ("#   future (e.g., data quality strings)." );
	s.addElement ("# * For portability, data in a DateValue file should have compatible intervals.");
	s.addElement ("# * Header variables and column headers can be enclosed in double quotes if");
	s.addElement ("#   the data contain spaces.");
	s.addElement ("# * Missing data can either be coded as the missing data value or no value");
	s.addElement ("# * Missing records will result in missing data being used when read.");
	s.addElement ("#" );
	s.addElement ("# The following header variables are recognized.  This information can be");
	s.addElement ("# used by software." );
	s.addElement ("Version = 1.4                # Optional.  File format version (to handle format changes)");
	s.addElement ("Delimiter = \" \"              # Optional.  Delimiter for data lines (default is space and tab)");
	s.addElement ("NumTS = 2                    # Optional.  Number of time series in file (default is 1)");
	s.addElement ("TSID = XXX.USGS.Streamflow.15MINUTE YYY.USGS.Streamflow.15Minute" );
	s.addElement ("                             # Required.");
	s.addElement ("                             # List of time series identifiers in file");
	s.addElement ("                             # Location.Source.DataType.Interval.Scenario");
	s.addElement ("                             # Do not include input type and name in identifier.");
	s.addElement ("Alias = X Y" );
	s.addElement ("                             # Optional.  Alias used internally instead of TSID.");
	s.addElement ("SequenceNum = -1 0           # Optional.  Used with traces when the TSID by" );
	s.addElement ("                             # itself does not uniquely identify the TS." );
	s.addElement ("Description = \"Flow at XXX\" \"Flow at Y\"");
	s.addElement ("                             # Optional.  Description for each time series." );
	s.addElement ("DataFlags = true,1 false" );
	s.addElement ("                             # Optional.  Indicates whether each value has a");
	s.addElement ("                             # a data flag.  If true, specify max flag characters.");
	s.addElement ("DataType = \"Streamflow\" \"Streamflow\"" );
	s.addElement ("                             # Optional.  Data types for each time series" );
	s.addElement ("                             # (consistent with TSID if specified).");
	s.addElement ("                             # The default is to use the data type in the TSID");
	s.addElement ("                             # Supplied to simplify use by other programs.");
	s.addElement ("Units = \"CFS\" \"CFS\"          # Optional.  Units for each time series (default is no units).");
	s.addElement ("MissingVal = -999 -999       # Optional.  Missing data value for each time series (default is -999)");
	s.addElement ("IncludeCount = true          # Optional.  If true, column after date/time" );
	s.addElement ("                             # is record count (1...) (default is false).");
	s.addElement ("IncludeTotalTime = true      # Optional.  If true, column after date is cumulative time (0...) (default is false).");
	s.addElement ("# Both of above can be true, and both columns will be added after the date");
	s.addElement ("Start = 1996-10-18:00:00     # Required.  Start date for time series");
	s.addElement ("End = 1997-06-14:00:00       # Required.  End date for time series");
	s.addElement ("                             # Period dates should be of a precision consistent" );
	s.addElement ("                             # with the dates used in the data section below." );
	s.addElement ("# Optional.  The following line can be read into a spreadsheet or database for");
	s.addElement ("# headers.  The lines above this line can be ignored in a spreadsheet import.");
	s.addElement ("# The number of headings should agree with the number of columns.");
	s.addElement ("Date \"Count\" \"TotalTime\" \"Description 1\" \"Description 2\"");
	s.addElement ("1996-10-18:00:00 1 0 110.74" );
	s.addElement ("1996-10-18:00:15 2 15 110.74" );
	s.addElement ("..." );
	return s;
}

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
	    in = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream ( full_fname )) );
		// Read lines and check for common strings that indicate a DateValue file.
		String string = null;
		boolean	is_datevalue = false;
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
		in.close();
		in = null;
		string = null;
		return is_datevalue;
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Read at time series from a Vector of String.  Currently this is accomplished by
writing the contents to a temporary file and then reading using one of the
standard methods.  A more efficient method may be added in the future but this
approach works OK for smaller files.
@param strings Vector of String containing data in DateValue file format.
@param tsident_string Time series identifier as string (used for initial
settings - reset by file contents).
@param req_date1 Requested starting date to initialize period (or null to
read the entire period).
@param req_date2 Requested ending date to initialize period (or null to read the
entire period).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception Exception if there is an error reading the time series.
*/
public static TS readFromStringList (	Vector strings, String tsident_string,
					DateTime req_date1, DateTime req_date2,
					String req_units, boolean read_data )
throws Exception
{	// Write the strings to a temporary file...
	String temp = IOUtil.tempFileName();
	PrintWriter pw = new PrintWriter ( new FileOutputStream(temp) );
	int size = 0;
	if ( strings != null ) {
		size = strings.size();
	}
	for ( int i = 0; i < size; i++ ) {
		pw.println ( (String)strings.elementAt(i) );
	}
	pw.close ();
	pw = null;
	// Create a DateValueTS from the temporary file...
	TS ts = readTimeSeries ( temp, req_date1, req_date2, req_units, read_data );
	// Remove the temporary file...
	File tempf = new File ( temp );
	tempf.delete();
	tempf = null;
	// Return...
	return ts;
}

/**
Read a time series from a DateValue format file.
@return a pointer to a newly-allocated time series if successful, a NULL
pointer if not.  The pointer should be deleted when no longer needed.
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
Read a time series from a DateValue format file.  The entire file is read using
the units from the file.
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
@return a pointer to a newly-allocated time series if successful, null if not.
@param in Reference to open BufferedReader.
@param req_date1 Requested starting date to initialize period (or null to
read the entire period).
@param req_date2 Requested ending date to initialize period (or null to read the
entire period).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception Exception if there is an error reading the time series.
*/
public static TS readTimeSeries (	BufferedReader in,
					DateTime req_date1, DateTime req_date2,
					String req_units, boolean read_data )
throws Exception
{	// Call the generic method...
	return readTimeSeries ( (TS)null, in, req_date1, req_date2, req_units, read_data );
}

/**
Read a time series from a DateValue format file.
@return a pointer to a newly-allocated time series if successful, a
NULL pointer if not.  The units are taken from the file and all data are read
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
@return a pointer to a newly-allocated time series if successful, a NULL pointer if not.
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
{	TS	ts = null;
	boolean	is_file = true;	// Is tsident_string a file?  Assume and check below

	String input_name = tsident_string;
	String full_fname = IOUtil.getPathUsingWorkingDir ( tsident_string );
	if ( !IOUtil.fileReadable(full_fname) ) {
		is_file = false;
		// Try the input name...
		TSIdent tsident = new TSIdent (tsident_string);
		full_fname = IOUtil.getPathUsingWorkingDir ( tsident.getInputName() );
		input_name = full_fname;
		if ( !IOUtil.fileReadable(full_fname) ) {
			Message.printWarning( 2, "DateValueTS.readTimeSeries",
			"Unable to determine file for \"" + tsident_string + "\"" );
			return ts;
		}
	}
	BufferedReader in = null;
	try {
	    in = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, "DateValueTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
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
			Message.printWarning( 2, "DateValueTS.readTimeSeries(String,...)",
			"Unable to create time series for \"" + tsident_string + "\"" );
			return ts;
		}
		ts.setIdentifier ( tsident_string );
		ts.getIdentifier().setInputType("DateValue");
		readTimeSeriesList ( ts, in, date1, date2, units, read_data );
	}
	ts.setInputName ( full_fname );
	ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
	ts.getIdentifier().setInputName ( input_name );
	in.close();
	return ts;
}

/**
Read a time series from a DateValue format file.  The TSID string is specified
in addition to the path to the file.  It is expected that a TSID in the file
matches the TSID (and the path to the file, if included in the TSID would not
properly allow the TSID to be specified).  This method can be used with newer
code where the I/O path is separate from the TSID that is used to identify the time series.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated time series if successful, a NULL pointer if not.
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
{	TS	ts = null;

	String input_name = filename;
	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	if ( !IOUtil.fileReadable(full_fname) ) {
		Message.printWarning( 2, "DateValueTS.readTimeSeries",
		"Unable to determine file for \"" + filename + "\"" );
		return ts;
	}
	BufferedReader in = null;
	try {
	    in = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, "DateValueTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
		return ts;
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
	Vector v = readTimeSeriesList (	ts, in,	date1, date2, units, read_data );
	if ( tsident_string.indexOf(".") < 0 ) {
		// The time series was specified with an alias so it needs
		// to be replaced with what was read.  The alias will have been
		// assigned in the readTimeSeriesList() method.
		ts = (TS)v.elementAt(0);
	}
	ts.getIdentifier().setInputType("DateValue");
	ts.setInputName ( full_fname );
	ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
	ts.getIdentifier().setInputName ( input_name );
	in.close();
	return ts;
}

/**
Read a time series from a DateValue format file.  The data units are taken from
the file and all data are read (not just the header).
@return a pointer to a time series if successful, a NULL pointer if not.
@param req_ts Pointer to time series to fill.  If null,
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
@return a pointer to a time series if successful, a NULL pointer if not.
@param req_ts Pointer to time series to fill.  If null,
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
{	String	routine = "DateValueTS.readTimeSeries(TS *,...)";
	TS	ts = null;

	String input_name = fname;
	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
	BufferedReader in = null;
	try {
	    in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, "Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	ts = readTimeSeries ( req_ts, in, date1, date2, units, read_data );
	ts.setInputName ( full_fname );
	ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
	ts.getIdentifier().setInputName ( input_name );
	in.close();
	return ts;
}

/**
Read a time series from a DateValue format file.
@return a pointer to time series if successful, null if not.  The calling code
is responsible for freeing the memory for the time series.
@param req_ts Pointer to time series to fill.  If null,
return a new time series.  All data are reset, except for the identifier, which
is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param req_date1 Requested starting date to initialize period (or null to read
the entire time series).
@param req_date2 Requested ending date to initialize period (or null to read
the entire time series).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read.
@exception Exception if there is an error reading the time series.
*/
public static TS readTimeSeries ( TS req_ts, BufferedReader in,	DateTime req_date1, DateTime req_date2,
					String req_units, boolean read_data )
throws Exception
{	Vector tslist = readTimeSeriesList ( req_ts, in, req_date1, req_date2, req_units, read_data );
	if ( (tslist == null) || (tslist.size() != 1) ) {
		tslist = null;
		return null;
	}
	else {	TS ts = (TS)tslist.elementAt(0);
		tslist = null;
		return ts;
	}
}

/**
Read all the time series from a DateValue format file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated Vector of time series if successful,
a NULL pointer if not.
@param fname Name of file to read.
@param date1 Starting date to initialize period (null to read the entire time
series).
@param date2 Ending date to initialize period (null to read the entire time
series).
@param units Units to convert to.
@param read_data Indicates whether data should be read.
*/
public static Vector readTimeSeriesList ( String fname, DateTime date1, DateTime date2,
						String units, boolean read_data)
throws Exception
{	Vector	tslist = null;

	String input_name = fname;
	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
	BufferedReader in = null;
	try {
	    in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, "DateValueTS.readTimeSeriesList",
		"Unable to open file \"" + full_fname + "\"" );
		return null;
	}
	tslist = readTimeSeriesList ( null, in, date1, date2, units, read_data);
	TS ts;
	int nts = 0;
	if ( tslist != null ) {
		nts = tslist.size();
	}
	for ( int i = 0; i < nts; i++ ) {
		ts = (TS)tslist.elementAt(i);
		if ( ts != null ) {
			ts.setInputName ( full_fname );
			ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
			ts.getIdentifier().setInputName ( input_name );
		}
	}
	in.close();
	return tslist;
}

/**
Read a time series from a DateValue format file.
@return a Vector of time series if successful, null if not.  The calling code
is responsible for freeing the memory for the time series.
@param req_ts Pointer to time series to fill.  If null,
return all new time series in the vector.  All data are reset, except for the
identifier, which is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param req_date1 Requested starting date to initialize period (or null to read
the entire time series).
@param req_date2 Requested ending date to initialize period (or null to read
the entire time series).
@param units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read.
@exception Exception if there is an error reading the time series.
*/
private static Vector readTimeSeriesList ( TS req_ts, BufferedReader in, DateTime req_date1,
						DateTime req_date2,	String req_units, boolean read_data )
throws Exception
{	String	date_str, message = null, string = "", value, variable;
	String	routine = "DateValueTS.readTimeSeriesList";
	int	dl = 10, dl2 = 30, numts = 1;
	DateTime	date1 = new DateTime(), date2 = new DateTime();

	// Always read the header.  Optional is whether the data are read...

	int line_count = 0;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Processing header..." );
	}
	String delimiter_default = " ";
	String	alias = "", dataflag = "", datatype = "", delimiter = delimiter_default,
		description = "", identifier = "", missing = "", seqnum = "",
		units = "";
	Vector	alias_v = null;
	Vector	dataflag_v = null;
	boolean	[] ts_has_data_flag = null;
	int	[] ts_data_flag_length = null;
	Vector	datatype_v = null;
	Vector	description_v = null;
	Vector	identifier_v = null;
	Vector	missing_v = null;
	Vector	seqnum_v = null;
	Vector	units_v = null;
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
		if ( (string.equals("")) ||	((string.length() > 0) && (string.charAt(0) == '#')) ) {
			// Skip comments and blank lines for now...
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
		    value = string.substring(equal_pos + 1).trim();
		}

		// Deal with the tokens...
		if ( variable.equalsIgnoreCase("Alias") ) {
			// Have the alias...
			alias = value;
			alias_v = StringUtil.breakStringList (
				value, delimiter, StringUtil.DELIM_SKIP_BLANKS|	StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( alias_v != null ) {
				size = alias_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 3, routine, "Number of Alias values using delimiter \"" + delimiter +
				        "\" (" + size +	") is != NumTS (" +	numts +	").  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					alias_v.addElement ( "" );
				}
			}
		}
		else if ( variable.equalsIgnoreCase("DataFlags") ) {
			// Have the data flags indicator which may or may not be surrounded by quotes...
			dataflag = value;
			dataflag_v = StringUtil.breakStringList (
				dataflag, delimiter,StringUtil.DELIM_SKIP_BLANKS|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( dataflag_v != null ) {
				size = dataflag_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of DataFlag values using delimiter \"" + delimiter +
				        "\" (" + size +	") is != NumTS (" + numts + "). Assuming no data flags.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					dataflag_v.addElement ( "false" );
				}
			}
			// Now further process the data flag indicators.  Need a boolean for each time series to indicate whether
			// data flags are used and need a width for the data flags
			ts_has_data_flag = new boolean[numts];
			ts_data_flag_length = new int[numts];
			for ( int ia = 0; ia < numts; ia++ ) {
				dataflag = ((String)dataflag_v.elementAt(ia)).trim();
				Vector v = StringUtil.breakStringList (	dataflag,",",0);
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
				if ( ((String)v.elementAt(0)).trim().equalsIgnoreCase("true") ) {
					ts_has_data_flag[ia] = true;
				}
				else {
				    ts_has_data_flag[ia] = false;
				}
				// Now set the length...
				ts_data_flag_length[ia] = 2; // Default
				if ( size > 1 ) {
					ts_data_flag_length[ia] = StringUtil.atoi(((String)v.elementAt(1)).trim());
				}
			}
		}
		else if ( variable.equalsIgnoreCase("DataType") ) {
			// Have the data type...
			datatype = value;
			datatype_v = StringUtil.breakStringList (
				datatype, delimiter,StringUtil.DELIM_SKIP_BLANKS|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( datatype_v != null ) {
				size = datatype_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of DataType values using delimiter \"" + delimiter +
				        "\" (" + size + ") is != NumTS (" + numts + "). Assuming blank.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					datatype_v.addElement ( "" );
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
				description, delimiter,	StringUtil.DELIM_SKIP_BLANKS|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( description_v != null ) {
				size = description_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of Description values using delimiter \"" + delimiter +
				        "\" (" + size +	") is != NumTS (" + numts + ").  Assuming blank.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					description_v.addElement ( "" );
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
				missing, delimiter,	StringUtil.DELIM_SKIP_BLANKS|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( missing_v != null ) {
				size = missing_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of Missing values using delimiter \"" + delimiter +
				        "\" (" + size + ") is != NumTS (" + numts + ").  Assuming -999.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					missing_v.addElement ( "" );
				}
			}
		}
		else if ( variable.equalsIgnoreCase("NumTS") ) {
			// Have the number of time series...
			numts = StringUtil.atoi(value);
		}
		else if ( variable.equalsIgnoreCase("SequenceNum") ) {
			// Have sequence numbers...
			seqnum = value;
			seqnum_v = StringUtil.breakStringList (
				seqnum, delimiter, StringUtil.DELIM_SKIP_BLANKS|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( seqnum_v != null ) {
				size = seqnum_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of SequenceNum values using delimiter \"" + delimiter +
				        "\" (" + size + ") is != NumTS (" + numts + ").  Assuming -1.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					seqnum_v.addElement ( "-1" );
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
				identifier, delimiter, StringUtil.DELIM_SKIP_BLANKS|StringUtil.DELIM_ALLOW_STRINGS );
			size = 0;
			if ( identifier_v != null ) {
				 size = identifier_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of TSID values using delimiter \"" + delimiter +
				        "\" (" + size + ") is != NumTS (" + numts + "). Assuming blank.  Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					identifier_v.addElement ( "" );
				}
			}
		}
		else if ( variable.equalsIgnoreCase("Units") ) {
			// Have the data units...
			units = value;
			units_v = StringUtil.breakStringList (
				units, delimiter, StringUtil.DELIM_SKIP_BLANKS|StringUtil.DELIM_ALLOW_STRINGS );
			if ( units_v != null ) {
				size = units_v.size();
			}
			if ( size != numts ) {
				Message.printWarning ( 2, routine, "Number of Units values using delimiter \"" + delimiter +
				        "\" (" + size + ") is != NumTS (" + numts + "). Assuming blank. Read errors may occur." );
				++warning_count;
				for ( int ia = size; ia < numts; ia++ ) {
					units_v.addElement ( "" );
				}
			}
		}
		else if ( variable.equalsIgnoreCase("Version") ) {
		    // TODO SAM 2008-04-14 Evaluate using version
			// Have the file version...
			//version = value; 
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

	// Check required data vectors and assign defaults if necessary...

	if ( identifier_v == null ) {
		identifier_v = new Vector(numts);
		// TODO SAM 2008-04-14 Evaluate tightening this constraint - throw exception?
		Message.printWarning ( 2, routine, "TSID property in file is missing.  Assigning default TS1, TS2, ..." );
		for ( int i = 0; i < numts; i++ ) {
			identifier_v.addElement ( "TS" + (i + 1) );
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
	Vector tslist = null;
	TS ts_array[] = null;	// Use this to speed processing so we don't have to search through tslist all the time
	// Set the time series pointer to either the requested time series
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
					alias = ((String)alias_v.elementAt(i)).trim();
					if ( !alias.equals("") && req_ts.getAlias().equalsIgnoreCase( alias) ) {
						// Found a matching time series...
						req_ts_i = i;
						//Message.printStatus ( 1, "", "Found matching TS "+req_ts_i+ " based on alias." );
						break;
					}
				}
				// Now check the identifier...
				identifier = ((String)identifier_v.elementAt(i)).trim();
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
			identifier = ((String)identifier_v.elementAt( req_ts_i)).trim();
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
		tslist = new Vector(1);
		tslist.addElement ( ts );
		ts_array = new TS[1];
		ts_array[0] = ts;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, "Adding requested time series to list." );
		}
		ident = new TSIdent ( ts.getIdentifier() );
		data_interval_base = ident.getIntervalBase();
		// Offset other information because of extra columns...
		// Make sure to set the interval for use below...
		identifier = ((String)identifier_v.elementAt( req_ts_i)).trim();
		ident = new TSIdent( identifier );
		// Set the data type in the TS header using the information in the identifier.
		// It may be overwritten below if the DataType property is specifed...
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
		tslist = new Vector(numts);
		ts_array = new TS[numts];
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, "Allocated space for " + numts + " time series in list." );
		}
		for ( int i = 0; i < numts; i++ ) {
			identifier = ((String)identifier_v.elementAt(i)).trim();
			ident = new TSIdent( identifier );
			// Need this to check whether time may be specified on data line...
			data_interval_base = ident.getIntervalBase();
			ts = TSUtil.newTimeSeries ( (String)identifier, true );
			if ( ts == null ) {
				Message.printWarning ( 2, routine, "Unable to create new time series for \"" + identifier + "\"" );
				return null;
			}
			// Only set the identifier if a new time series.
			// Otherwise assume the the existing identifier is to be used (e.g., from a file name).
			ts.setIdentifier ( identifier );
			ts.getIdentifier().setInputType("DateValue");
			tslist.addElement ( ts );
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
				// A time series was requested but does not
				// match so continue...
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
			alias = ((String)alias_v.elementAt(i)).trim();
			if ( !alias.equals("") ) {
				ts.setAlias ( alias );
			}
		}
		if ( datatype_v != null ) {
			datatype = ((String)datatype_v.elementAt(i)).trim();
			if ( !datatype.equals("") ) {
				ts.setDataType ( datatype );
			}
		}
		if ( units_v != null ) {
			units = ((String)units_v.elementAt(i)).trim();
			ts.setDataUnits ( units );
			ts.setDataUnitsOriginal ( units );
		}
		ts.setDate1 ( date1 );
		ts.setDate1Original ( date1 );
		ts.setDate2 ( date2 );
		ts.setDate2Original ( date2 );
		if ( description_v != null ) {
			description=((String)description_v.elementAt(i)).trim();
			ts.setDescription ( description );
		}
		if ( missing_v != null ) {
			missing = ((String)missing_v.elementAt(i)).trim();
			if ( missing.equalsIgnoreCase("NaN") ) {
				ts.setMissing ( Double.NaN );
			}
			else if ( StringUtil.isDouble(missing) ) {
				ts.setMissing ( StringUtil.atod(missing) );
			}
		}
		if ( seqnum_v != null ) {
			seqnum = ((String)seqnum_v.elementAt(i)).trim();
			if ( StringUtil.isInteger(seqnum) ) {
				ts.setSequenceNumber ( StringUtil.atoi(seqnum));
			}
		}
		if ( ts_has_data_flag[i] ) {
			// Data flags are being used.
			ts.hasDataFlags ( true, ts_data_flag_length[i] );
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
		ts = null;
		ts_array = null;
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
	else {	for ( int i = 0; i < numts; i++ ) {
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

	DateTime	date;
	Vector		strings;
	double		dvalue;	// Double data value
	String		svalue;	// String data value
	boolean		first = true;
	int		nstrings = 0;
	boolean		use_time = false;
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
			strings = StringUtil.breakStringList ( string, delimiter, StringUtil.DELIM_SKIP_BLANKS );
		}
		else {
		    // Expect to have data flags so parse WITH handling quoted strings.  This will generally be slower...
			strings = StringUtil.breakStringList ( string,
			delimiter, StringUtil.DELIM_SKIP_BLANKS|StringUtil.DELIM_ALLOW_STRINGS );
		}
		nstrings = 0;
		if ( strings != null ) {
			nstrings = strings.size();
		}
		if ( nstrings == num_expected_columns ) {
			// Assume that there is NO space between date and time or that time field is not used...
			date_str = ((String)strings.elementAt(0)).trim();	
			// Date + extra columns...
			first_data_column = 1 + num_extra_columns;
			req_ts_column2 = req_ts_column;
		}
		else if ( use_time && (nstrings == num_expected_columns_p1) ) {
			// Assume that there IS a space between the date and
			// time.  Concatenate together so that the DateTime.parse will work.
			date_str = ((String)strings.elementAt(0)).trim() + " " + ((String)strings.elementAt(1)).trim();	
			// Date + time + extra column...
			first_data_column = 2 + num_extra_columns;
			// Adusted requested time series column...
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
			svalue = ((String)strings.elementAt(req_ts_column2)).trim();
			// This introduces a performance hit - maybe need to add a boolean array for each time series
			// to be able to check whether NaN is the missing - then can avoid the check.
			// For now just check the string.
			if ( svalue.equals("NaN") ) {
				dvalue = ts_array[0].getMissing();
			}
			else {
			    dvalue = StringUtil.atod ( svalue );
			}
			if ( ts_has_data_flag[req_ts_i] ) {
				// Has a data flag...
				dataflag = ((String)strings.elementAt( req_ts_column2 + 1)).trim();
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
				svalue = ((String)strings.elementAt(i)).trim();
				if ( svalue.equals("NaN") ) {
					dvalue = ts_array[its].getMissing();
				}
				else {
				    dvalue = StringUtil.atod ( svalue );
				}
				if ( ts_has_data_flag[its] ) {
					dataflag = ((String)
					strings.elementAt(++i)).trim();
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
	ts = null;
	ts_array = null;
	return tslist;
}

/**
Write a time series to a DateValue format file.
@param ts Pointer to single time series to write.
@param out PrintWriter to write to.
@exception Exception if an error occurs.
*/
public static void writeTimeSeries ( TS ts, PrintWriter out )
throws Exception
{	// Call the fully-loaded method...
	Vector v = new Vector ( 1 );
	v.addElement ( ts );
	writeTimeSeriesList(v, out, (DateTime)null, (DateTime)null, null, true);
}

/**
Write a Vector of time series to a DateValue format file.
@param out PrintWrite to write to.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units
will be converted on output.
@param write_data Indicates whether data should be written (as opposed to only
writing the header).
@exception Exception if an error occurs.
*/
public static void writeTimeSeries (	TS ts, PrintWriter out,
					DateTime date1,
					DateTime date2, String units,
					boolean write_data )
throws Exception
{	Vector v = new Vector ( 1 );
	v.addElement ( ts );
	writeTimeSeriesList ( v, out, date1, date2, units, write_data );
}

/**
Write a time series to the specified file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@param ts Time series to write.
@param fname Name of file to write.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units
will be converted on output.
@param write_data Indicates whether data should be written (as opposed to only
writing the header).
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeries (    TS ts, String fname, DateTime date1,
                    DateTime date2, String units,
                    boolean write_data )
throws Exception
{   String  routine = "DateValueTS.writeTimeSeries";

    String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
    try {   FileOutputStream fos = new FileOutputStream( full_fname );
        PrintWriter fout = new PrintWriter ( fos );

        writeTimeSeries ( ts, fout, date1, date2, units, write_data );

        fout.flush();
        fout.close();
    }
    catch ( Exception e ) {
        String message = "Error opening \"" + full_fname +
        "\" for writing.";
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
Write a Vector of time series to a DateValue format file.
Currently there is no way to indicate that the count or total time should be
printed.
@param tslist Vector of pointers to time series to write.
@param out PrintWrite to write to.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units
will be converted on output.
@param write_data Indicates whether data should be written (as opposed to only
writing the header).
@exception Exception if there is an error writing the file (I/O error or invalid
data).
*/
public static void writeTimeSeriesList (Vector tslist, PrintWriter out,
                    DateTime date1,
                    DateTime date2, String units,
                    boolean write_data )
throws Exception
{
    writeTimeSeriesList ( tslist, out, date1, date2, units, write_data, null );
}

/**
Write a Vector of time series to a DateValue format file.
Currently there is no way to indicate that the count or total time should be
printed.
@param tslist Vector of pointers to time series to write.
@param out PrintWrite to write to.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units
will be converted on output.
@param write_data Indicates whether data should be written (as opposed to only
writing the header).
@param props Properties to control output (see overloaded method for description).
@exception Exception if there is an error writing the file (I/O error or invalid
data).
*/
public static void writeTimeSeriesList (Vector tslist, PrintWriter out,
					DateTime date1,
					DateTime date2, String units,
					boolean write_data, PropList props )
throws Exception
{	String	message, routine = "DateValueTS.writeTimeSeriesList";
	DateTime ts_start, ts_end, t = new DateTime( DateTime.DATE_FAST );
	int	i = 0;

	// Check for a null time series list...

	if ( tslist == null ) {
		message = "Null time series list.  Not writing.";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}

	int size = tslist.size();
	if ( size == 0 ) {
		message = "No time series in list.  Not writing.";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}
	
	// Make sure that a non-null properties list is available
	if ( props == null ) {
	    props = new PropList ( "DateValueTS" );
	}

	// Set the parameters for output..

	TSLimits limits = new TSLimits ();
	if ( (date1 == null) || (date2 == null) ) {
		// Get the limits...
		try {	limits = TSUtil.getPeriodFromTS (
				tslist, TSUtil.MAX_POR );
		}
		catch ( Exception e ) {}
	}
	if ( date1 == null ) {
		// Use the maximum period in the time series list...
		ts_start = new DateTime ( limits.getDate1() );
	}
	else {
	    ts_start = new DateTime ( date1 );
	}

	if ( date2 == null ) {
		// Use the time series value...
		ts_end = new DateTime ( limits.getDate2() );
	}
	else {
	    ts_end = new DateTime ( date2 );
	}

	// Loop through the time series and make sure they have the same interval...

 	int data_interval_base = 0;
	int data_interval_mult = 0;
	int data_interval_base_i = 0;
	int data_interval_mult_i = 0;

	TS ts = null;
	// Set up conversion factors for units (apparently some compilers don't
	// like allocating one slot so always make it more and ignore the extra
	// value)...
	double mult[] = new double[size + 1];
	double add[] = new double[size + 1];
	DataUnitsConversion conversion;
	for ( i = 0; i < size; i++ ) {
		mult[i] = 1.0;
		add[i] = 0.0;
		ts = (TS)tslist.elementAt(i);
		if ( ts != null ) {
			data_interval_base_i = ts.getDataIntervalBase();
			data_interval_mult_i = ts.getDataIntervalMult();
		}
		if ( i == 0 ) {
			data_interval_base = data_interval_base_i;
			data_interval_mult = data_interval_mult_i;
		}
		else if ( (data_interval_base != data_interval_base_i) ||
			(data_interval_mult != data_interval_mult_i) ) {
			mult = null;
			add = null;
			message = "Time series do not have the same interval.  Can't write";
			Message.printWarning ( 2, routine, message );
			throw new Exception ( message );
		}
		// Get the conversion factors to use for output.  Don't call
		// TSUtil.convertUnits because we don't want to alter the time
		// series itself...
		if (	(ts != null) && (units != null) &&
			(units.length() != 0) &&
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

	if ( (data_interval_base == TimeInterval.IRREGULAR) && (size > 1) ) {
		message = "Currently, only one irregular TS can be written to a file.";
		Message.printWarning ( 2, routine, message );
		mult = null;
		add = null;
		throw new Exception ( message );
	}

    // Write header.  See the printSample() method for an example string...

	String delim = " ";
	String propval = props.getValue ( "Delimiter" );
	if ( propval != null ) {
	    delim = propval;
	}
	String nodata_string = "?";
	StringBuffer alias_buffer = new StringBuffer();
	boolean has_seqnum = false;
	StringBuffer seqnum_buffer = new StringBuffer();
	StringBuffer dataflag_buffer = new StringBuffer();
	StringBuffer columns_buffer = new StringBuffer();
	StringBuffer datatype_buffer = new StringBuffer();
	StringBuffer description_buffer = new StringBuffer();
	StringBuffer missingval_buffer = new StringBuffer();
	StringBuffer tsid_buffer = new StringBuffer();
	StringBuffer units_buffer = new StringBuffer();

	if (	(data_interval_base == TimeInterval.IRREGULAR) ||
		(data_interval_base == TimeInterval.MINUTE) ||
		(data_interval_base == TimeInterval.HOUR) ) {
		columns_buffer.append ( "Date" + delim + "Time" + delim );
	}
	else {
	    columns_buffer.append ( "Date" + delim );
	}
	boolean has_data_flags = false;	// Only include data flags in output if
					// at least one time series actually has the flag.
	for ( i = 0; i < size; i++ ) {
		ts = (TS)tslist.elementAt(i);
		if ( i != 0 ) {
			// Append the delimiter...
			alias_buffer.append ( delim );
			seqnum_buffer.append ( delim );
			columns_buffer.append ( delim );
			dataflag_buffer.append ( delim );
			datatype_buffer.append ( delim );
			description_buffer.append ( delim );
			missingval_buffer.append ( delim );
			tsid_buffer.append ( delim );
			units_buffer.append ( delim );
		}
		// Now add the data...
		if ( ts == null ) {
			alias_buffer.append ( "\"" + nodata_string + "\"" );
			seqnum_buffer.append ( "\"" + nodata_string + "\"" );
			columns_buffer.append ( nodata_string );
			dataflag_buffer.append ( "\"" + nodata_string + "\"" );
			datatype_buffer.append ( nodata_string );
			description_buffer.append (	"\"" + nodata_string + "\"" );
			missingval_buffer.append ( nodata_string );
			tsid_buffer.append ( "\"" + nodata_string + "\"" );
			units_buffer.append ( nodata_string );
		}
		else {
		    String alias = ts.getAlias();
		    alias_buffer.append ( "\"" + alias + "\"" );
			if ( ts.getSequenceNumber() >= 0 ) {
				// At least one time series has the sequence
				// number so it will be output below.
				has_seqnum = true;
			}
			seqnum_buffer.append ( ts.getSequenceNumber() );
			if ( !ts.getDataUnits().trim().equals("") ) {
				// Has units so display in column heading...
			    if ( alias.length() > 0 ) {
			        // Use the alias.
    				columns_buffer.append ( "\"" + alias + ", " + ts.getDataUnits() + "\"" );
			    }
			    else {
	                 columns_buffer.append ( "\"" + ts.getIdentifier().toString() + ", " + ts.getDataUnits() + "\"" );
			    }
			}
			else {
			    if ( alias.length() > 0 ) {
			        columns_buffer.append ( "\"" + alias + "\"" );
			    }
			    else {
			        columns_buffer.append ( "\"" + ts.getIdentifier().toString() + "\"" );
			    }
			}
			if ( ts.hasDataFlags() ) {
				has_data_flags = true;
				dataflag_buffer.append ( "true," + ts.getDataFlagLength() );
				columns_buffer.append ( delim );
				columns_buffer.append ( "DataFlag" );
			}
			else {
			    dataflag_buffer.append ( "false" );
			}
			if ( ts.getDataType().trim().equals("") ) {
				datatype_buffer.append("\"" + ts.getIdentifier().getType() + "\"" );
			}
			else {
			    datatype_buffer.append("\"" + ts.getDataType() + "\"" );
			}
			description_buffer.append (	"\"" + ts.getDescription() + "\"" );
			// If the missing value is NaN, just print NaN.
			// Otherwise the %.4f results in NaN.000...
			// The following is a trick to check for NaN...
			if ( ts.getMissing() != ts.getMissing() ) {
				missingval_buffer.append ("NaN" );
			}
			else {
			    // Assume that missing is indicated by a number...
				missingval_buffer.append ( StringUtil.formatString(	ts.getMissing(),"%.4f"));
			}
			tsid_buffer.append ( "\"" +	ts.getIdentifier().toString() + "\"" );
			units_buffer.append ( "\"" + ts.getDataUnits() + "\"" );
		}
	}

	// Print the standard header...

	out.println ( "# DateValueTS 1.3 file" );
	IOUtil.printCreatorHeader ( out, "#", 80, 0 );
	out.println ( "#" );
	out.println ( "Delimiter   = \"" + delim + "\"" );
	out.println ( "NumTS       = " + size );
	out.println ( "TSID        = " + tsid_buffer.toString() );
	out.println ( "Alias       = " + alias_buffer.toString() );
	if ( has_seqnum ) {
		out.println ( "SequenceNum = " + seqnum_buffer.toString() );
	}
	out.println ( "Description = " + description_buffer.toString() );
	out.println ( "DataType    = " + datatype_buffer.toString() );
	out.println ( "Units       = " + units_buffer.toString() );
	out.println ( "MissingVal  = " + missingval_buffer.toString() );
	if ( has_data_flags ) {
		// At least one of the time series in the list has data flags
		// so output the data flags information for all the time series...
		out.println ( "DataFlags   = " + dataflag_buffer.toString() );
	}
	out.println ( "Start       = " + ts_start.toString() );
	out.println ( "End         = " + ts_end.toString() );

	// Print the comments/genesis information...

	out.println ( "#" );
	out.println ( "# Time series comments/histories:" );
	out.println ( "#" );

	String print_genesis = "true";
	Vector genesis = null;
	Vector comments = null;
	int j = 0, jsize = 0;
	for ( i = 0; i < size; i++ ) {
		ts = (TS)tslist.elementAt(i);
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
			ts.getIdentifier().toString() +
			" Alias=" + ts.getAlias() +
			") has no comments or history" );
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
				out.println ( "#   " + (String)comments.elementAt(j) );
			}
		}
		if (	(genesis != null) && (genesis.size() > 0) &&
			print_genesis.equalsIgnoreCase("true") ) {
			out.println ( "#" );
			out.println ( "# Creation history for time series " +
			(i + 1) + " (TSID=" + ts.getIdentifier().toString() +
			" Alias=" + ts.getAlias() + "):" );
			out.println ( "#" );
			jsize = genesis.size();
			for ( j = 0; j < jsize; j++ ) {
				out.println ( "#   " + (String)genesis.elementAt(j) );
			}
		}
	}
	out.println ( "#" );
	out.println ( "#EndHeader" );

	if ( !write_data ) {
		// Don't want to write the data...
		mult = null;
		add = null;
		return;
	}

	// Header line indicating data columns....

	out.println ( columns_buffer.toString() );
	
	double value = 0.0;
	String string_value;

	// Need to add iterator at some point - could use this to test
	// performace...
	StringBuffer buffer = new StringBuffer();
	TSData datapoint;	// Data point associated with a date.
	String dataflag;	// Data flag associated with a data point.
	if ( data_interval_base == TimeInterval.IRREGULAR ) {
		// Irregular interval... loop through all of the values...
		// This assumes that _date1 and _date2 have been set.
		IrregularTS its = null;
		Vector alldata = null;
		if ( ts != null ) {
			its = (IrregularTS)ts;
			alldata = its.getData();
		}
		if ( alldata == null ) {
			mult = null;
			add = null;
			return;
		}
		size = alldata.size();
		TSData tsdata = null;
		DateTime date;
		for ( i = 0; i < size; i++ ) {
			tsdata = (TSData)alldata.elementAt(i);
			if ( tsdata == null ) {
				break;
			}
			date = tsdata.getDate();
			if ( date.lessThan(ts_start) ) {
				continue;
			}
			else if ( date.greaterThan(ts_end) ) {
				break;
			}
			// Else print the record...
			value = tsdata.getData();
			if ( ts.isDataMissing(value) ) {
				if ( value != value ) {
					// This trick is used to figure out if
					// missing data are indicated by NaN...
					string_value = "NaN";
				}
				else {
				    string_value = StringUtil.formatString( tsdata.getData(), "%.4f" );
				}
			}
			else {	// Convert the units...
				string_value = StringUtil.formatString( (tsdata.getData()*mult[0] + add[0]), "%.4f" );
			}
			// Don't think @ is needed given new DateValueTS
			// capabilities...
			//out.println (
			//date.toString(
			//DateTime.FORMAT_YYYY_MM_DD_HH_mm).replace(' ','@') +
			//" " + string_value );
			// Use the precision of the dates in the data - ISO
			// formats will be used by default...
			out.println ( date.toString() + " " + string_value );
		}
	}
	else {	// Regular interval...
		t = new DateTime ( ts_start);
		// Make sure no time zone is set to minimize output...
		t.setTimeZone ("");
		for ( ;	t.lessThanOrEqualTo(ts_end); t.addInterval(data_interval_base, data_interval_mult)) {
			buffer.setLength(0);
			//buffer.append( t.toString().replace(' ','@') + delim);
			buffer.append( t.toString() + delim );
			for ( i = 0; i < size; i++ ) {
				ts = (TS)tslist.elementAt(i);
				// Need to work on formatting number to a
				// better precision.  For now just get to
				// output without major loss in precision...
				if ( ts != null ) {
					value = ts.getDataValue(t);
				}
				if ( (ts == null) || ts.isDataMissing(value) ) {
					if ( value != value ) {
						// This trick is used to figure
						// out if missing data are
						// indicated by NaN...
						string_value = "NaN";
					}
					else {
					    string_value = StringUtil.formatString( value, "%.4f");
					}
				}
				else {
				    string_value = StringUtil.formatString(	(value*mult[i] + add[i]),"%.4f" );
				}
				if ( i == 0 ) {
					buffer.append ( string_value );
				}
				else {
				    buffer.append ( delim + string_value );
				}
				// Now print the data flag...
				if ( ts.hasDataFlags() ) {
					datapoint = ts.getDataPoint ( t );
					dataflag = datapoint.getDataFlag();
					// Always enclose the dataflag in quotes
					// because it may contain white space...
					buffer.append ( delim +	"\""+ dataflag + "\"");
				}
			}
			out.println ( buffer.toString () );
		}
	}
	// Clean up...
	columns_buffer = null;
	datatype_buffer = null;
	dataflag_buffer = null;
	description_buffer = null;
	missingval_buffer = null;
	tsid_buffer = null;
	units_buffer = null;
	mult = null;
	add = null;
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
@param tslist Vector of pointers to time series.
@param fname file name to write.
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeriesList ( Vector tslist, String fname )
throws Exception
{	writeTimeSeriesList ( tslist, fname, (DateTime)null, (DateTime)null,
			null, true );
}

/**
Write a Vector of time series to the specified file.
@param tslist Vector of time series to write.
@param fname Name of file to write.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units
will be converted on output.
@param write_data Indicates whether data should be written (as opposed to only
writing the header).
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeriesList (Vector tslist, String fname,
                    DateTime date1, DateTime date2,
                    String units, boolean write_data )
throws Exception
{
    writeTimeSeriesList ( tslist, fname, date1, date2, units, write_data, null );
}

/**
Write a Vector of time series to the specified file.
@param tslist Vector of time series to write.
@param fname Name of file to write.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units
will be converted on output.
@param write_data Indicates whether data should be written (as opposed to only
writing the header).
@param props Properties to control output, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>    <td><b>Description</b></td> <td><b>Default</b></td>
</tr>

<tr>
<td><b>Delimiter</b></td>
<td><b>The delimiter to use in output.</b>
<td>Space</td>
</tr>
</table>
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeriesList (Vector tslist, String fname,
					DateTime date1, DateTime date2,
					String units, boolean write_data, PropList props )
throws Exception
{	String	routine = "DateValueTS.writeTimeSeriesList";

	String full_fname = IOUtil.getPathUsingWorkingDir(fname);
	try {	FileOutputStream fos = new FileOutputStream ( full_fname );
		PrintWriter fout = new PrintWriter ( fos );

		writeTimeSeriesList (	tslist, fout, date1, date2,
					units, write_data, props );

		fout.flush();
		fout.close();
	}
	catch ( Exception e ) {
		String message = "Error writing \"" + full_fname + "\".";
		Message.printWarning( 2, routine, message );
		Message.printWarning( 2, routine, e );
		throw new Exception (message);
	}
}

} // End DateValueTS
