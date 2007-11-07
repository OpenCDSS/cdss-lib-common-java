// ----------------------------------------------------------------------------
// RiverWareTS - class for reading/writing RiverWare time series file formats
// ----------------------------------------------------------------------------
// History:
//
// 2002-05-29	Steven A. Malers, RTi	Copy UsgsNwisTS and update as
//					appropriate.
// 2002-06-06	SAM, RTi		Add set_units and set_scale as arguments
//					to fully loaded writeTimeSeries().
// 2002-07-25	SAM, RTi		For the writeTimeSeries() method, change
//					to get the full path to the file before
//					writing.  Change so that if the hour in
//					the header is zero, write out as hour
//					24 of the previous day.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
// 2005-05-31	SAM, RTi		* Overload writeTimeSeries() to take a
//					  PropList and make it the default
//					  method called by the other methods.
//					* Fix writeTimeSeries() to handle other
//					  than hourly data.
//					* Add Precision parameter to
//					  writeTimeSeries().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.TimeInterval;

/**
The RiverWareTS class reads and writes RiverWare time series files.
*/
public class RiverWareTS
{

/**
Private method to create a time series given the proper heading information
@param req_ts If non-null, an existing time series is expected to be passed
in.
@param filename name of file being read.  The location and data type are
taken from the file name.
@param timestep RiverWare timestep as string (essentially the same as TS except
there is a space between the multiplier and base).
@param units Units of the data.
@param date1 Requested start date.
@param date2 Requested end date.
@param date1_file Start date in the file.
@param date2_file End date in the file.
@exception Exception if an error occurs.
*/
private static TS createTimeSeries (	TS req_ts, String filename,
					String timestep,
					String units,
					DateTime date1, DateTime date2,
					DateTime date1_file,
					DateTime date2_file )
throws Exception
{	String routine = "RiverWareTS.createTimeSeries";

	// Declare the time series of the proper type based on the interval.
	// Use a TSIdent to parse out the interval information...

	String timestep2 = StringUtil.unpad(timestep, " ",
		StringUtil.PAD_FRONT_MIDDLE_BACK );
	String location = "";
	String datatype = "";
	Vector tokens = StringUtil.breakStringList ( filename, ".", 0 );
	if ( (tokens != null) && (tokens.size() == 2) ) {
		location = ((String)tokens.elementAt(0)).trim();
		// Only want the relative part...
		File f = new File ( location );
		location = f.getName();
		datatype = ((String)tokens.elementAt(1)).trim();
	}
	TSIdent ident = new TSIdent ( location, "", datatype, timestep2, "");

	TS ts = null;

	// Set the time series pointer to either the requested time series
	// or a newly-created time series.
	if ( req_ts != null ) {
		ts = req_ts;
		// Identifier is assumed to have been set previously.
	}
	else {	ts = TSUtil.newTimeSeries ( ident.toString(), true );
	}
	if ( ts == null ) {
		Message.printWarning ( 2, routine,
		"Unable to create new time series for \"" +
		ident.toString() + "\"" );
		return (TS)null;
	}

	// Only set the identifier if a new time series.  Otherwise assume the
	// the existing identifier is to be used (e.g., from a file name).
	if ( req_ts == null ) {
		ts.setIdentifier ( ident.toString() );
	}

	// Set the parameters from the input and override with the parameters...

	String description = "";
	if ( req_ts == null ) {
		ts.setLocation ( location );
		description = location + ", " +  datatype;
	}
	ts.setDataUnits ( units );
	ts.setDataType ( datatype );
	ts.setDescription ( description );
	ts.setDataUnitsOriginal ( units );
	// Original dates are what is in the file...
	ts.setDate1Original ( date1_file );
	ts.setDate2Original ( date2_file );
	ts.setDate1 ( new DateTime(date1) );
	ts.setDate2 ( new DateTime(date2) );

	// Set missing to NaN since this is what RiverWare uses...

	ts.setMissing ( Double.NaN );
	
	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, routine, 
			"Period to read is " +
			date1.toString() + " to " +
			date2.toString() );
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
public static Vector getSample ()
{	Vector	s = new Vector ( 50 );
	s.addElement ( "#" );
	s.addElement ( "# RiverWare time series format" );
	s.addElement ( "#" );
	s.addElement ( "# Comments are only allowed at the top of the file." );
	s.addElement ( "# UNITS are AFTER the scale is applied." );
	s.addElement ( "# SCALE*data = values for specified units." );
	s.addElement (
	"# SET_UNITS and SET_SCALE are used by RiverWare during read." );
	s.addElement ( "START_DATE: 1996-11-14 24:00" );
	s.addElement ( "END_DATE: 1996-12-31 24:00" );
	s.addElement ( "TIMESTEP: 24 Hour" );
	s.addElement ( "UNITS: CFS" );
	s.addElement ( "SCALE: 1000" );
	s.addElement ( "SET_UNITS: CMS" );
	s.addElement ( "SET_SCALE: 10" );
	s.addElement ( "0.00" );
	s.addElement ( "130.00" );
	s.addElement ( "..." );
	return s;
}

/**
Determine whether a file is a RiverWare file.  This can be used rather than
checking the source in a time series identifier.  If the file passes any of the
following conditions, it is assumed to be a RiverWare file:
<ol>
<li>	A line starts with "START_DATE:".</li>
</ol>
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
*/
public static boolean isRiverWareFile ( String filename )
{	BufferedReader in = null;
	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	try {	in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( full_fname )) );
		// Read lines and check for common strings that
		// indicate a DateValue file.
		String string = null;
		boolean	is_riverware = false;
		while( (string = in.readLine()) != null ) {
			if ( string.regionMatches(true,0, "START_DATE:",0,11) ){
				is_riverware = true;
				break;
			}
			else if ( (string.length() > 0) &&
				(string.charAt(0) != '#') ) {
				break;
			}
		}
		in.close();
		in = null;
		string = null;
		return is_riverware;
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Read the time series from a RiverWare file.
@param filename Name of file to read.
@return TS for data in the file or null if there is an error reading the
time series.
*/
public static TS readTimeSeries ( String filename )
{	return readTimeSeries ( filename, null, null, null, true );
}

/**
Read a time series from a RiverWare format file.  Currently only daily surface
water files are recognized.  The resulting time series will have an identifier
like STATIONID.RiverWare.Streamflow.1Day.
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
		// Don't have a requested time series but need the filename
		// to infer location and data type...
		ts = readTimeSeries ( (TS)null, in, full_fname, date1, date2,
			units, read_data );
		ts.setInputName ( full_fname );
		ts.getIdentifier().setInputType("RiverWare");
		ts.getIdentifier().setInputName(full_fname);
		ts.addToGenesis ( "Read data from \"" + full_fname +
			"\" for period " + ts.getDate1() + " to " +
			ts.getDate2() );
		in.close();
		in = null;
	}
	catch ( Exception e ) {
		Message.printWarning( 2,
		"RiverWareTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
	}
	return ts;
}

/**
Read a time series from a RiverWare format file.  The TSID string is specified
in addition to the path to the file.  It is expected that a TSID in the file
matches the TSID (and the path to the file, if included in the TSID would not
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
		"RiverWareTS.readTimeSeries",
		"Unable to determine file for \"" + filename + "\"" );
		return ts;
	}
	BufferedReader in = null;
	try {	in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 1,
		"RiverWareTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	// Call the fully-loaded method...
	// Pass the file pointer and an empty time series, which
	// will be used to locate the time series in the file.
	ts = TSUtil.newTimeSeries ( tsident_string, true );
	if ( ts == null ) {
		Message.printWarning( 1,
		"RiverWareTS.readTimeSeries(String,...)",
		"Unable to create time series for \"" + tsident_string + "\"" );
		return ts;
	}
	ts.setIdentifier ( tsident_string );
	readTimeSeries (	ts, in,
				full_fname,
				date1, date2,
				units, read_data );
	ts.setInputName ( full_fname );
	ts.getIdentifier().setInputType ( "RiverWare" );
	ts.getIdentifier().setInputName ( filename );
	ts.addToGenesis ( "Read data from \"" + full_fname +
		"\" for period " + ts.getDate1() + " to " +
		ts.getDate2() );
	in.close();
	return ts;
}

/**
Read a time series from a RiverWare format file.
@return a pointer to time series if successful, null if not.  The calling code
is responsible for freeing the memory for the time series.
@param req_ts Pointer to time series to fill.  If null,
return a new time series.  All data are reset, except for the identifier, which
is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param filename Name of file that is being read (use to get the location and
data type).
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
{	String	routine = "RiverWareTS.readTimeSeries";
	String	string = null, timestep_string = "", end_date_string = "",
		start_date_string = "", scale_string = "";
	int	dl = 10;
	DateTime	date1_file = null, date2_file = null;

	// Always read the header.  Optional is whether the data are read...

	int line_count = 0;

	String	units = "";
	String	token0, token1;
	DateTime	date1 = null, date2 = null;
	Vector	tokens = null;
	TS	ts = null;
	try {
	while ( true ) {
		string = in.readLine();
		if ( string == null ) {
			break;
		}
		++line_count;
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Processing: \"" + string + "\"" );
		}
		string = string.trim();
		if (	(string.length() == 0) ||
			(string.charAt(0) == '#') ) {
			// Skip comments and blank lines...
			continue;
		}
		if ( string.indexOf(':') < 0 ) {
			// No more header information so break out and read
			// data...
			break;
		}

		// Break the tokens using ':' as the delimiter...

		tokens = StringUtil.breakStringList( string, ":",
			StringUtil.DELIM_SKIP_BLANKS );

		token0 = (String)tokens.elementAt(0);
		token1 = (String)tokens.elementAt(1);
		if ( token0.equalsIgnoreCase("start_date") ) {
			start_date_string = token1.trim();
			date1_file = DateTime.parse ( start_date_string );
		}
		else if ( token0.equalsIgnoreCase("end_date") ) {
			end_date_string = token1.trim();
			date2_file = DateTime.parse ( end_date_string );
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
		Message.printWarning ( 2, routine,
		"Error processing line " + line_count + ": \"" + string + "\"");
		Message.printWarning ( 2, routine, e );
	}

	// Create an in-memory time series and set header information...

	if ( req_date1 != null ) {
		date1 = req_date1;
	}
	else {	date1 = date1_file;
	}
	if ( req_date2 != null ) {
		date2 = req_date2;
	}
	else {	date2 = date2_file;
	}
	ts = createTimeSeries (	req_ts, filename,
				timestep_string,
				units,
				date1, date2,
				date1_file, date2_file );
	if ( !read_data ) {
		return ts;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Reading data..." );
	}

	// Allocate the memory for the data array...

	if ( ts.allocateDataSpace() == 1 ) {
		Message.printWarning( 2, routine,
		"Error allocating data space..." );
		// Clean up memory...
		throw new Exception ( "Error allocating time series memory." );
	}

	int data_interval_base = ts.getDataIntervalBase ();
	int data_interval_mult = ts.getDataIntervalMult ();

	// The latest string that was read for the header should be the first
	// data line so make sure not to skip it...

	try {
	boolean first_data = true;
	// Dates are not specified in the file so iterate with date...
	DateTime date = new DateTime ( date1_file );
	double scale = 1.0;
	if ( StringUtil.isDouble(scale_string) ) {
		scale = StringUtil.atod ( scale_string );
	}
	for (	; date.lessThanOrEqualTo(date2_file);
		date.addInterval (data_interval_base, data_interval_mult ) ) {
		if ( first_data ) {
			first_data = false;
		}
		else {	string = in.readLine();
			++line_count;
			if ( string == null ) {
				break;
			}
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Processing: \"" + string + "\"" );
		}
		string = string.trim();
		if (	(string.length() == 0) ||
			(string.charAt(0) == '#') ) {
			// Skip comments and blank lines...
			continue;
		}

		// String will contain a data value or "NaN".  If "NaN", just
		// skip because the time series is initialized to missing data.

		if ( date.lessThan(date1) ) {
			// No need to do...
			continue;
		}
		else if ( date.greaterThan(date2) ) {
			if ( Message.isDebugOn ) {
				Message.printDebug (
				dl, routine, "Finished reading data"+
				" at: " + date.toString() );
			}
			// Will return below...
			break;
		}
		// Else set the data value...
		if ( !string.equalsIgnoreCase("NaN") ) {
			ts.setDataValue ( date, scale*StringUtil.atod(string) );
		}
	}
	} catch ( Exception e ) {
		Message.printWarning ( 2, routine,
		"Error processing line " + line_count + ": \"" + string + "\"");
		Message.printWarning ( 2, routine, e );
		ts = null;
	}
	
	routine = null;
	string = null;
	date1_file = null;
	date2_file = null;
	units = null;
	tokens = null;
	date1 = null;
	date2 = null;
	return ts;
}

/**
Write a RiverWare time series to the open PrintWriter.
@param ts Time series to write.
@param fp PrintWrite to write to.
@exception IOException if there is an error writing the file.
*/
private static void writeTimeSeries ( TS ts, PrintWriter fp )
throws IOException
{	writeTimeSeries ( ts, fp, (DateTime)null, (DateTime)null,
			(PropList)null, true );
}

/**
Write a time series to a RiverWare format file.  The entire period is written.
@param ts Single time series to write.
@param fname Name of file to write.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries ( TS ts, String fname )
throws IOException
{	PrintWriter	out = null;

	String full_fname = IOUtil.getPathUsingWorkingDir(fname);
	try {	out = new PrintWriter (new FileWriter(full_fname));
	}
	catch ( Exception e ) {
		String message =
		"Error opening \"" + full_fname + "\" for writing.";
		Message.printWarning ( 2,
		"RiverWareTS.writePersistent(TS,String)", message );
		out = null;
		throw new IOException ( message );
	}
	writeTimeSeries ( ts, out );
	out.close ();
	out = null;
}

/**
Write a time series to a RiverWare format file using a default format based on
the time series units.
@param ts Vector of pointers to time series to write.
@param fname Name of file to write.
@param req_date1 First date to write (if NULL write the entire time series).
@param req_date2 Last date to write (if NULL write the entire time series).
@param req_units Units to write.  If different than the current units the units
will be converted on output.
@param scale Scale to divide values by for output.  Ignored if <= 0.0.
@param set_units RiverWare "set_units" parameter.  If empty or null will not be
written.
@param set_scale RiverWare "set_scale" parameter.  If zero or negative will not
be written.
@param write_data Indicates whether data should be written (as opposed to only
writing the header) (<b>currently not used</b>).
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries (	TS ts, String fname,
					DateTime req_date1, DateTime req_date2,
					String req_units, double scale,
					String set_units, double set_scale,
					boolean write_data )
throws IOException
{	PropList props = new PropList ( "RiverWare" );
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
Write a time series to a RiverWare format file using a default format based on
the time series units.
@param ts Vector of pointers to time series to write.
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
<td>The Units to write to the RiverWare header.  The data values are NOT
converted.
</td>
<td></td>
</tr>

<tr>
<td><b>Precision</b></td>
<td>The precision (number of digits after decimal) to use when writing data
values.
</td>
<td></td>
</tr>

</table>
@param write_data Indicates whether data should be written (as opposed to only
writing the header) (<b>currently not used</b>).
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeries (	TS ts, String fname,
					DateTime req_date1, DateTime req_date2,
					PropList props, boolean write_data )
throws IOException
{	PrintWriter	out = null;

	String full_fname = IOUtil.getPathUsingWorkingDir(fname);
	try {	out = new PrintWriter (new FileWriter(full_fname));
	}
	catch ( Exception e ) {
		String message =
		"Error opening \"" + full_fname + "\" for writing.";
		Message.printWarning ( 2,"RiverWareTS.writeTimeSeries",message);
		out = null;
		throw new IOException ( message );
	}
	writeTimeSeries ( ts, out, req_date1, req_date2, props, write_data);
	out.close ();
	out = null;
}

/**
Write a time series to a RiverWare format file using a default format based on
the data units.
@param ts Time series to write.
@param fp PrintWriter to write to.
@param req_date1 First date to write (if NULL write the entire time series).
@param req_date2 Last date to write (if NULL write the entire time series).
@param req_units Units to write.  If different than the current units the units
will be converted on output.  This method does not support set_units.
@param scale Scale to divide values by for output.
@param set_units RiverWare "set_units" parameter.  If empty or null will not be
written.
@param set_scale RiverWare "set_scale" parameter.  If zero or negative will not
be written.
@param write_data Indicates whether data should be written (if false only the
header is written).
@exception IOException if there is an error writing the file.
*/
private static void writeTimeSeries (	TS ts, PrintWriter fp,
					DateTime req_date1, DateTime req_date2,
					PropList props, boolean write_data )
throws IOException
{	String	message, routine="RiverWareTS.writePersistent";

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

	// Get the interval information to facilitate use below...

	int data_interval_base = ts.getDataIntervalBase();
	int data_interval_mult = ts.getDataIntervalMult();

	// Get the dates for output...

	DateTime date1 = null;
	if ( req_date1 == null ) {
		date1 = new DateTime ( ts.getDate1() );
	}
	else {	date1 = new DateTime ( req_date1 );
		// Make sure the precision is that of the data...
		date1.setPrecision ( data_interval_base );
	}
	DateTime date2 = null;
	if ( req_date2 == null ) {
		date2 = new DateTime ( ts.getDate2() );
	}
	else {	date2 = new DateTime ( req_date2 );
		// Make sure the precision is that of the data...
		date2.setPrecision ( data_interval_base );
	}

	if ( props == null ) {
		props = new PropList ( "RiverWare" );
	}

	// Write header...

	fp.println ( "#" );
	IOUtil.printCreatorHeader ( fp, "#", 80, 0 );
	fp.println ( "#" );
	fp.println ( "# RiverWare Time Series File" );
	fp.println ( "#" );
	if ( data_interval_base == TimeInterval.HOUR ) {
		// Adjust the internal 0-23 clock to the 0-24 RiverWare clock...
		if ( date1.getHour() == 0 ) {
			// Set to hour 24 of the previous day...
			DateTime d = new DateTime ( date1 );
			d.addDay ( -1 );
			fp.println ( "start_date: " + d.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
		}
		else {	// OK to write the date/time as is with hour and
			// minute...
			fp.println ( "start_date: " + date1.toString(
			DateTime.FORMAT_YYYY_MM_DD_HH_mm ) );
		}
		if ( date2.getHour() == 0 ) {
			// Set to hour 24 of the previous day...
			DateTime d = new DateTime ( date2 );
			d.addDay ( -1 );
			fp.println ( "end_date: " + d.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
		}
		else {	// OK to write the date/time as is with hour and
			// minute...
			fp.println ( "end_date: " + date2.toString(
			DateTime.FORMAT_YYYY_MM_DD_HH_mm ) );
		}
	}
	else if ( data_interval_base == TimeInterval.DAY ) {
		// Use the in-memory day but always add 24:00 at the end...
		fp.println ( "start_date: " + date1.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
		fp.println ( "end_date: " + date2.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
	}
	else if ( data_interval_base == TimeInterval.MONTH ) {
		// Use the in-memory day but always add 24:00 at the end...
		// Set the day to the number of days in the month.  The day
		// will be ignored below during iteration because the precision
		// was set above to month.
		date1.setDay ( TimeUtil.numDaysInMonth(date1) );
		fp.println ( "start_date: " + date1.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
		date2.setDay ( TimeUtil.numDaysInMonth(date2) );
		fp.println ( "end_date: " + date2.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
	}
	else if ( data_interval_base == TimeInterval.YEAR ) {
		// Use the in-memory day but always add 24:00 at the end...
		// Set the month and day to the end of the year.  The month and
		// day will be ignored below during iteration because the
		// precision was set above to month.
		date1.setMonth ( 12 );
		date1.setDay ( 31 );
		fp.println ( "start_date: " + date1.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
		date2.setMonth ( 12 );
		date2.setDay ( 31 );
		fp.println ( "end_date: " + date2.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " 24:00" );
	}
	else {	// Interval is not supported...
		throw new IOException ( "Interval for \"" +
			ts.getIdentifier().toString() +
			"\" is not supported for RiverWare.");
	}
	// Print the interval, with multiplier, if provided...
	try {	TimeInterval interval = TimeInterval.parseInterval (
			ts.getIdentifier().getInterval() );
		if ( interval.getMultiplierString().equals("") ) {
			fp.println ( "timestep: 1 " +
			interval.getBaseString() );
		}
		else {	fp.println ( "timestep: " +
			interval.getMultiplierString() +" "+
			interval.getBaseString() );
		}
	}
	catch ( Exception e ) {
		; // Ignore for now
	}
	String Units = props.getValue ( "Units" );
	if ( (Units != null) && (Units.length() > 0) ) {
		fp.println ( "units: " + Units );
	}
	else {	fp.println ( "units: " + ts.getDataUnits() );
	}
	String Scale = props.getValue ( "Scale" );
	double Scale_double = 1.0;
	if ( (Scale == null) || (Scale.length() == 0) ) {
		Scale = "1";	// Default
	}
	else if ( StringUtil.isDouble(Scale) ) {
		Scale_double = StringUtil.atod ( Scale );
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
		Precision = "4";	// Default
	}
	String format = "%." + Precision + "f";

	// What format to use for data?  For now use .4 until all the data
	// units are integrated...

	DateTime date = new DateTime ( date1 );
	double value;
	for ( ; date.lessThanOrEqualTo(date2);
		date.addInterval(data_interval_base,data_interval_mult) ) {
		value = ts.getDataValue ( date );
		if ( ts.isDataMissing(value) ) {
			fp.println ( "NaN" );
		}
		else {	fp.println (StringUtil.formatString(
				value/Scale_double, format) );
		}
	}
	routine = null;
	date1 = null;
	date2 = null;
	date = null;
}

} // End RiverWareTS class
