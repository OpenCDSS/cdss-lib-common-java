// ----------------------------------------------------------------------------
// ModsimTS - class to process MODSIM output file format time series
// ----------------------------------------------------------------------------
// History:
//
// 2002-06-13	Steven A. Malers, RTi	Copy and modify DateValueTS.
//					Do not include writeTimeSeries() yet -
//					if needed, copy from DateValueTS.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TS.INTERVAL* to TimeInterval.
// 2004-07-16   Marc L. Baldo, RTi	Handle dot characters '.' in 
// 					MODSIM link names.  Dot characters
// 					are reserved as separators in 
//					TS Identifiers.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
//EndHeader

package RTi.TS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Vector;

import RTi.TS.TSException;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

public class ModsimTS {

/**
Return an array of strings indicating the available data types for a MODSIM
file.
@return an array of strings indicating the available data types for a MODSIM
file or null if the data types cannot be determined.
@param file Name of file.
@param read_file If false, the file extension is used to determine standard data
types.  If true, the file is read and the header is scanned to determine the
available file types (NOT IMPLEMENTED).
*/
public static String[] getAvailableDataTypes ( String file, boolean read_file )
{	if ( read_file ) {
		return null;
	}
	String ext = IOUtil.getFileExtension ( file );
	String [] dt = null;
	if ( ext.equalsIgnoreCase("ACC") ) {
		dt = new String[6];
		dt[0] = "STGL";
		dt[1] = "ACRL";
		dt[2] = "FLOW";
		dt[3] = "GLINK";
		dt[4] = "GSTGL";
		dt[5] = "GACRL";
	}
	else if ( ext.equalsIgnoreCase("DEM") ) {
		dt = new String[4];
		dt[0] = "DEMAND";
		dt[1] = "SURF_IN";
		dt[2] = "GW_IN";
		dt[3] = "DEM_SHT";
	}
	else if ( ext.equalsIgnoreCase("FLO") ) {
		dt = new String[3];
		dt[0] = "FLOW";
		dt[1] = "LOSS";
		dt[2] = "NATFLOW";
	}
	else if ( ext.equalsIgnoreCase("GW") ) {
		dt = new String[2];
		dt[0] = "GWInfiltration";
		dt[1] = "FromGWtoNode";
	}
	else if ( ext.equalsIgnoreCase("RES") ) {
		dt = new String[20];
		dt[0] = "DWS_REL";
		dt[1] = "ELEV_END";
		dt[2] = "EVP_LOSS";
		dt[3] = "EVP_RATE";
		dt[4] = "GWATER";
		dt[5] = "HEAD_AVG";
		dt[6] = "HYDRO_REQ";
		dt[7] = "HYDR_SHT";
		dt[8] = "POWR_2ND";
		dt[9] = "POWR_AVG";
		dt[10] = "POWR_PK";
		dt[11] = "PUMP_IN";
		dt[12] = "PUMP_OUT";
		dt[13] = "SEEPAGE";
		dt[14] = "SPILLS";
		dt[15] = "STOR_BEG";
		dt[16] = "STOR_END";
		dt[17] = "STOR_TRG";
		dt[18] = "UNREG_IN";
		dt[19] = "UPS_REL";
	}
	return dt;
}

/**
Return a sample so that a user/developer knows what a file looks like.
Currently, the samples are compiled into the code to make absolutely sure that
the programmer knows what sample is supported.  Perhaps they could be stored in
sample files in the future.  
@return Sample file contents.
*/
public static Vector getSample ()
{	Vector	s = new Vector ( 50 );
	s.addElement (
"#" );
	s.addElement (
"# This is a sample of a typical MODSIM output file.");
	s.addElement (
"# * Comments shown in this output are for illustration only." );
	s.addElement (
"# * Node/Link numbers are used internally and should not be used." );
	s.addElement (
"# * Dates are for the last MODSIM run." );
	s.addElement (
"# * Dates always include day.  The day is set to mid-month for monthly data.");
	s.addElement (
"# * Data units are determined from MODSIM configuration.");
	s.addElement (
"# * Daily data have data lines that start with NODE/LINK, NAME, WEEK, DAY," );
	s.addElement (
"#   CALENDAR_YEAR, CALENDAR_MONTH, CALENDAR_DATE" );
	s.addElement (
"# * Monthly data have data lines that start with NODE/LINK, NAME, YEAR, MONTH," );
	s.addElement (
"#   CALENDAR_YEAR, CALENDAR_MONTH, CALENDAR_DATE" );
	s.addElement (
"# * Data for one YEAR of data for all nodes/link are grouped, followed by");
	s.addElement (
"#   the next YEAR.");
	s.addElement (
"\"NODE\", \"NAME\", \"YEAR\", \"MONTH\", \"CALENDAR_YEAR\", \"CALENDAR_MONTH\", \"CALENDAR_DATE\", \"DEMAND\", \"SURF_IN\", \"GW_IN\", \"DEM_SHT\"" );
	s.addElement (
"69, \"MINFLO\", 1990, 1, 1989, 11, 15, 1487, 0, 0, 1487" );
	s.addElement (
"69, \"MINFLO\", 1990, 2, 1989, 12, 15, 1537, 0, 0, 1537" );
	s.addElement (
"69, \"MINFLO\", 1990, 3, 1990, 1, 15, 1537, 0, 0, 1537" );
	s.addElement (
"69, \"MINFLO\", 1990, 4, 1990, 2, 14, 1388, 0, 0, 1388" );
	s.addElement (
"69, \"MINFLO\", 1990, 5, 1990, 3, 15, 1537, 0, 0, 1537" );
	s.addElement (
"69, \"MINFLO\", 1990, 6, 1990, 4, 15, 2201, 0, 0, 2201" );
	s.addElement (
"69, \"MINFLO\", 1990, 7, 1990, 5, 15, 6824, 0, 0, 6824" );
	s.addElement (
"69, \"MINFLO\", 1990, 8, 1990, 6, 15, 7436, 0, 0, 7436" );
	s.addElement (
"69, \"MINFLO\", 1990, 9, 1990, 7, 15, 7684, 0, 0, 7684" );
	s.addElement (
"69, \"MINFLO\", 1990, 10, 1990, 8, 15, 7684, 0, 0, 7684" );
	s.addElement (
"69, \"MINFLO\", 1990, 11, 1990, 9, 15, 3688, 0, 0, 3688" );
	s.addElement (
"69, \"MINFLO\", 1990, 12, 1990, 10, 15, 3074, 0, 0, 3074" );
	s.addElement (
"70, \"WARNOK\", 1990, 1, 1989, 11, 15, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 2, 1989, 12, 15, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 3, 1990, 1, 15, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 4, 1990, 2, 14, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 5, 1990, 3, 15, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 6, 1990, 4, 15, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 7, 1990, 5, 15, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 8, 1990, 6, 15, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 9, 1990, 7, 15, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 10, 1990, 8, 15, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 11, 1990, 9, 15, 0, 0, 0, 0" );
	s.addElement (
"70, \"WARNOK\", 1990, 12, 1990, 10, 15, 0, 0, 0, 0" );
	s.addElement (
"..." );
	return s;
}

/**
Determine whether a file is a MODSIM file.  This can be used rather than
checking the source in a time series identifier.  If the file passes any of the
following conditions, it is assumed to be a MODSIM file:
<ol>
<li>	A line starts with "NODE.</li>
<li>	A line starts with "LINK.</li>
</ol>
IOUtil.getPathUsingWorkingDir() is called to expand the filename.
*/
public static boolean isMODSIMFile ( String filename )
{	BufferedReader in = null;
	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	try {	in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( full_fname )) );
		// Read lines and check for common strings that
		// indicate a MODSIM file.
		String string = null;
		boolean	is_modsim = false;
		while( (string = in.readLine()) != null ) {
			if (	string.regionMatches(true,0,"\"NODE",0,5) ||
				string.regionMatches(true,0,"\"LINK",0,5) ) {
				is_modsim = true;
				break;
			}
		}
		in.close();
		in = null;
		string = null;
		return is_modsim;
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Read a time series from a MODSIM format file.
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
Read a time series from a MODSIM format file.  The entire file is read.
@return 0 if successful, 1 if not.
@param in Reference to open BufferedReader.
@param filename File being read.
@exception TSException if there is an error reading the time series.
*/
public static TS readTimeSeries ( BufferedReader in, String filename )
throws Exception
{	return readTimeSeries ( in, filename, null, null, null, true );
}

/**
Read a time series from a MODSIM format file.
@return a pointer to a newly-allocated time series if successful, null if not.
@param in Reference to open BufferedReader.
@param filename File to read.
@param req_date1 Requested starting date to initialize period (or NULL to
read the entire period).
@param req_date2 Requested ending date to initialize period (or NULL to read the
entire period).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read (false=no, true=yes).
@exception Exception if there is an error reading the time series.
*/
public static TS readTimeSeries (	BufferedReader in, String filename,
					DateTime req_date1, DateTime req_date2,
					String req_units, boolean read_data )
throws Exception
{	// Call the generic method...
	return readTimeSeries ( (TS)null, in, filename, req_date1, req_date2,
			req_units, read_data );
}

/**
Read a time series from a MODSIM format file.
@return a pointer to a newly-allocated time series if successful, a
NULL pointer if not.  The units are taken from the file and all data are read
(not just the header).
@param tsident_string One of the following:  1) the time series identifier to
read (where the scenario is the file name) or 2) the name of a file to read
(in which case it is assumed that only one time series exists in the
file - otherwise use the readTimeSeriesList() method).
@param date1 Starting date to initialize period (or NULL to read entire time
series).
@param date2 Ending date to initialize period (or NULL to read entire time
series).
*/
public static TS readTimeSeries (	String tsident_string,
					DateTime date1, DateTime date2 )
throws Exception
{	return readTimeSeries ( tsident_string, date1, date2, "", true );
}

/**
Read a time series from a MODSIM format file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated time series if successful, a NULL pointer
if not.
@param tsident_string The full identifier for the time series to
read (where the scenario is the file name) or 2) the name of a file to read
(in which case it is assumed that only one time series exists in the
file - otherwise use the readTimeSeriesList() method).
@param date1 Starting date to initialize period (NULL to read the entire time
series).
@param date2 Ending date to initialize period (NULL to read the entire time
series).
@param units Units to convert to.
@param read_data Indicates whether data should be read (false=no, true=yes).
*/
public static TS readTimeSeries ( String tsident_string, DateTime date1,
			DateTime date2, String units, boolean read_data )
throws Exception
{	TS	ts = null;
	boolean	is_file = true;	// Is tsident_string a file?
				// Assume and check below

	String input_name = tsident_string;
	String full_fname = IOUtil.getPathUsingWorkingDir ( tsident_string );
	if ( !IOUtil.fileReadable(full_fname) ) {
		is_file = false;
		// Try the scenario (SAMX this is old-style TSID support and
		// should be phased out??)...
		TSIdent tsident = new TSIdent (tsident_string);
		full_fname = IOUtil.getPathUsingWorkingDir (
				tsident.getScenario() );
		input_name = full_fname;
		if ( !IOUtil.fileReadable(full_fname) ) {
			Message.printWarning( 2,
			"ModsimTS.readTimeSeries",
			"Unable to determine file for \"" + tsident_string +
			"\"" );
			return ts;
		}
	}
	BufferedReader in = null;
	try {	in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2,
		"ModsimTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	// Call the fully-loaded method...
	if ( is_file ) {
		// Expect that the time series file has one time series and
		// should read it...
		ts = readTimeSeries (	(TS)null, in, full_fname, date1, date2,
					units, read_data );
	}
	else {	// Pass the file pointer and an empty time series, which
		// will be used to locate the time series in the file.
		TSIdent ident = new TSIdent ( tsident_string );
		if ( ident.getInterval().equals("") ) {
			// Figure out if the file is monthly or daily data...
			in.mark(256);
			String string = in.readLine();
			boolean isdaily = false;
			if(StringUtil.indexOfIgnoreCase(string,"WEEK",0) >= 0){
				isdaily = true;
			}
			in.reset();
			// Need to set the data interval...
			if ( isdaily ) {
				ident.setInterval("Day");
			}
			else {	ident.setInterval("Month");
			}
			ts = TSUtil.newTimeSeries ( ident.toString(), true );
		}
		else {	// Trust the identifier that was passed in...
			ts = TSUtil.newTimeSeries ( tsident_string, true );
		}
		if ( ts == null ) {
			Message.printWarning( 2,
			"ModsimTS.readTimeSeries(String,...)",
			"Unable to create time series for \"" +
			tsident_string + "\"" );
			return ts;
		}
		ts.setIdentifier ( tsident_string );
		ts.getIdentifier().setInputType("MODSIM");
		readTimeSeriesList (	ts, in, full_fname,
					date1, date2,
					units, read_data );
	}
	ts.setInputName ( full_fname );
	ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
	ts.getIdentifier().setInputName ( input_name );
	in.close();
	return ts;
}

/**
Read a time series from a MODSIM format file.  The TSID string is specified
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
@param read_data Indicates whether data should be read (0=no, 1=yes).
*/
public static TS readTimeSeries (	String tsident_string, String filename,
					DateTime date1, DateTime date2,
					String units, boolean read_data )
throws Exception
{	TS	ts = null;

	String input_name = filename;
	String full_fname = IOUtil.getPathUsingWorkingDir ( filename );
	if ( !IOUtil.fileReadable(full_fname) ) {
		Message.printWarning( 2,
		"ModsimTS.readTimeSeries",
		"Unable to determine file for \"" + filename + "\"" );
		return ts;
	}
	BufferedReader in = null;
	try {	in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2,
		"ModsimTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	// Call the fully-loaded method...
	// Pass the file pointer and an empty time series, which
	// will be used to locate the time series in the file.
	TSIdent ident = new TSIdent ( tsident_string );
	if ( ident.getInterval().equals("") ) {
		// Figure out if the file is monthly or daily data...
		in.mark(256);
		String string = in.readLine();
		boolean isdaily = false;
		if ( StringUtil.indexOfIgnoreCase(string,"WEEK",0) >= 0 ) {
			isdaily = true;
		}
		in.reset();
		// Need to set the data interval...
		if ( isdaily ) {
			ident.setInterval("Day");
		}
		else {	ident.setInterval("Month");
		}
		ts = TSUtil.newTimeSeries ( ident.toString(), true );
	}
	else {	// Trust the identifier that was passed in...
		ts = TSUtil.newTimeSeries ( tsident_string, true );
	}
	if ( ts == null ) {
		Message.printWarning( 2,
		"ModsimTS.readTimeSeries(String,...)",
		"Unable to create time series for \"" +
		tsident_string + "\"" );
		return ts;
	}
	ts.setIdentifier ( tsident_string );
	ts.getIdentifier().setInputType("MODSIM");
	readTimeSeriesList (	ts, in, full_fname,
				date1, date2,
				units, read_data );
	ts.setInputName ( full_fname );
	ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
	ts.getIdentifier().setInputName ( input_name );
	in.close();
	return ts;
}

/**
Read a time series from a MODSIM format file.  The data units are taken from
the file and all data are read (not just the header).
@return a pointer to a time series if successful, a NULL pointer if not.
@param req_ts Pointer to time series to fill.  If null,
return a new time series.  If non-null, all data are reset, except for the
identifier, which is assumed to have been set in the calling code.
@param fname Name of file to read.
@param date1 Starting date to initialize period to.
@param date2 Ending date to initialize period to.
*/
public static TS readTimeSeries (	TS req_ts, String fname,
					DateTime date1, DateTime date2 )
throws Exception
{	return readTimeSeries ( req_ts, fname, date1, date2, "", true );
}

/**
Read a time series from a MODSIM format file.
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
public static TS readTimeSeries (	TS req_ts, String fname,
					DateTime date1, DateTime date2,
					String units, boolean read_data )
throws Exception
{	String	routine = "MODSIMTS.readTimeSeries(TS *,...)";
	TS	ts = null;

	String input_name = fname;
	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
	BufferedReader in = null;
	try {	in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine,
		"Unable to open file \"" + full_fname + "\"" );
		return ts;
	}
	ts = readTimeSeries ( req_ts, in, full_fname, date1, date2, units,
				read_data );
	ts.setInputName ( full_fname );
	ts.addToGenesis ( "Read time series from \"" + full_fname + "\"" );
	ts.getIdentifier().setInputName ( input_name );
	in.close();
	return ts;
}

/**
Read a time series from a MODSIM format file.
@return a pointer to time series if successful, null if not.  The calling code
is responsible for freeing the memory for the time series.
@param req_ts Pointer to time series to fill.  If null,
return a new time series.  All data are reset, except for the identifier, which
is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param full_fname Path to file name.
@param req_date1 Requested starting date to initialize period (or NULL to read
the entire time series).
@param req_date2 Requested ending date to initialize period (or NULL to read
the entire time series).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read.
@exception TSException if there is an error reading the time series.
*/
public static TS readTimeSeries (	TS req_ts, BufferedReader in,
					String full_fname,
					DateTime req_date1, DateTime req_date2,
					String req_units, boolean read_data )
throws Exception
{	Vector tslist = readTimeSeriesList (	req_ts, in, full_fname,
						req_date1, req_date2,
						req_units, read_data );
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
Read all the time series from a MODSIM format file.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@return a pointer to a newly-allocated Vector of time series if successful,
a NULL pointer if not.
@param fname Name of file to read.
@param date1 Starting date to initialize period (NULL to read the entire time
series).
@param date2 Ending date to initialize period (NULL to read the entire time
series).
@param units Units to convert to.
@param read_data Indicates whether data should be read.
*/
public static Vector readTimeSeriesList (	String fname,
						DateTime date1, DateTime date2,
						String units, boolean read_data)
throws Exception
{	Vector	tslist = null;

	String input_name = fname;
	String full_fname = IOUtil.getPathUsingWorkingDir ( fname );
	BufferedReader in = null;
	try {	in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( full_fname )) );
	}
	catch ( Exception e ) {
		Message.printWarning( 2,
		"ModsimTS.readTimeSeries(String,...)",
		"Unable to open file \"" + full_fname + "\"" );
	}
	tslist = readTimeSeriesList ( null, in, full_fname, date1, date2,
			units, read_data);
	TS ts;
	int nts = 0;
	if ( tslist != null ) {
		nts = tslist.size();
	}
	for ( int i = 0; i < nts; i++ ) {
		ts = (TS)tslist.elementAt(i);
		if ( ts != null ) {
			ts.setInputName ( full_fname );
			ts.addToGenesis ( "Read time series from \"" +
					full_fname + "\"" );
			ts.getIdentifier().setInputName ( input_name );
		}
	}
	in.close();
	return tslist;
}

/**
Read a time series from a MODSIM format file.
@return a Vector of time series if successful, null if not.  The calling code
is responsible for freeing the memory for the time series.
@param req_ts Pointer to time series to fill.  If null,
return all new time series in the vector.  All data are reset, except for the
identifier, which is assumed to have been set in the calling code.
@param in Reference to open input stream.
@param filename Name of the input file.  This is required because the period of
record is not saved at the top of the MODSIM file.  Therefore the file is opened
and the last line is read to determine the end date.
@param req_date1 Requested starting date to initialize period (or NULL to read
the entire time series).
@param req_date2 Requested ending date to initialize period (or NULL to read
the entire time series).
@param req_units Units to convert to (currently ignored).
@param read_data Indicates whether data should be read.
@exception TSException if there is an error reading the time series.
*/
public static Vector readTimeSeriesList (	TS req_ts, BufferedReader in,
						String filename,
						DateTime req_date1,
						DateTime req_date2,
						String req_units,
						boolean read_data )
throws Exception
{	String	string = "";
	String	routine = "ModsimTS.readTimeSeries";
	int	dl = 10, dl2 = 30, num_param = 1;
	int	numts_read = 0;
	int	node_in_model_year = 0;
	DateTime	date1 = null, date2 = null;
	DateTime	date1_file = null, date2_file = null;
	TSIdent ident = null;
	boolean	file_has_nodes = true;

	// MODSIM files do not have header information with the period for the
	// time series.  Therefore, grab a reasonable amount of the end of the
	// file - then read lines (broken by line breaks) until the last data
	// line is encountered...

	RandomAccessFile ra = new RandomAccessFile ( filename, "r" );
	long length = ra.length();
	// Skip to 5000 bytes from the end.  This should get some actual data
	// lines.  Save in a temporary array in memory.
	if ( length >= 5000 ) {
		ra.seek ( length - 5000 );
	}
	byte[] b = new byte[5000];
	ra.read ( b );
	ra.close();
	ra = null;
	// Now break the bytes into records...
	String bs = new String ( b );
	Vector v = StringUtil.breakStringList ( bs, "\n\r",
		StringUtil.DELIM_SKIP_BLANKS );
	// The last item will contain the last line from the file...
	Vector endstrings = StringUtil.breakStringList (
		(String)v.elementAt(v.size() - 1), " \t,",
		StringUtil.DELIM_SKIP_BLANKS|
		StringUtil.DELIM_ALLOW_STRINGS );

	// Because of the organization of the file, the entire file needs
	// to be read whether the data are read or not.

	int line_count = 0;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Processing header..." );
	}
	String	identifier = "";
	boolean	need_header = true;
	boolean getting_period = true;
	int	data_interval_base = 0;
	int	size = 0;
	int	first_data_col = 7;	// First field with data (0 index)
	DateTime	date = null;		// Date used for data lines.
	int	day, month, year = -1;	// Date fields.
	int	model_year = -1, model_year_prev = -1;
					// Years for model (e.g., water years),
					// which is what the file is broken
					// organized by
	int	model_year1 = 0;	// First model year in file.
	int	nstrings;		// Number of tokens per data line.
	int	its;			// Counter for time series.
	int	icol;			// Counter for columns.
	int	ts_pos = 0;		// Position in tslist for first time
					// series for a node.

	String name = "";	// Name of the node.
	String name2 = "";	// Name of the node.
	String node = "";	// Node number as string - used for the name if
				// the name is blank.
	String node_prev = "";	// Node number from previous row of data.
	String name_prev = "";	// Name of the node from the previous line that
	String name_prev2 = "";	// was read - initial to blank so it can be
				// checked when getting the period.
	// If a requested time series is specified, only the data for a node
	// name that matches the request will be processed...
	String	name_req_ts = null;
	int	req_ts_column = -1;
	if ( req_ts != null ) {
		name_req_ts = req_ts.getLocation();
	}

	TS ts = null;
	TS [] ts_array = null;	// Use to speed time series processing.
	Vector tslist = null;
	Vector header = null, strings = null;
	double dvalue = 0.0;	// Data value.

	try {

	// Need to read 
	while( (string = in.readLine()) != null ) {
		++line_count;
		// Unpad the line so that we can better deal with blank lines...
		string = string.trim();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			//Message.printStatus ( 1, routine,
			"Processing: \"" + string + "\"" );
		}
		if (	(string.equals("")) ||
			((string.length() > 0) && (string.charAt(0) == '#')) ) {
			// Skip comments and blank lines for now...
			continue;
		}

		// Always parse the line...

		strings = StringUtil.breakStringList ( string, " \t,",
			StringUtil.DELIM_SKIP_BLANKS|
			StringUtil.DELIM_ALLOW_STRINGS );
		if ( strings == null ) {
			continue;
		}
		nstrings = strings.size();

		if (	need_header &&
			(string.regionMatches(true,0,"\"Node",0,5) ||
			string.regionMatches(true,0,"\"Link",0,5)) ) {
			// First header line...
			header = strings;
			if ( string.regionMatches(true,0,"\"Link",0,5) ) {
				file_has_nodes = false;
			}
			// Mark the position of the file.  To support daily
			// data, assume that one row of data can be 80bytes x
			// 365 days x 100 years = 2920000 bytes that need to be
			// saved.  For monthly assume 80x12x100 = 96000.  This
			// mark is saved so that we read ahead to determine the
			// period and then can return to read the data...
			if ( StringUtil.indexOfIgnoreCase(string,"WEEK",0)>= 0){
				// Assume daily data...
				data_interval_base = TimeInterval.DAY;
				in.mark ( 2920000 );
				date = new DateTime ( DateTime.PRECISION_DAY );
			}
			else {	// Assume monthly data...
				data_interval_base = TimeInterval.MONTH;
				in.mark ( 96000 );
				date = new DateTime( DateTime.PRECISION_MONTH );
			}

			// If a requested time series is given, find the data
			// column using the header...

			num_param = nstrings - first_data_col;
						// Number of parameters in the
						// file for each node.
			Message.printStatus ( 1, routine,
			"There are " + num_param + " data columns." );
			if ( req_ts != null ) {
				tslist = new Vector(1);
				String datatype_req_ts =
					req_ts.getIdentifier().getType();
				for (	icol = first_data_col; icol < nstrings;
					icol++ ) {
					if ( datatype_req_ts.equalsIgnoreCase(
						(String)
						strings.elementAt(icol)) ) {
						req_ts_column = icol;
						break;
					}
				}
			}
			else {	// Just guess.  The true number will be
				// num_param* the number of nodes.
				tslist = new Vector ( num_param*100 );
			}

			// No need to run the code below and can avoid a long
			// indented section so continue...
			need_header = false;
			continue;
		}

		// The remainder of the lines are for data.

		node_prev = node;
		node = (String)strings.elementAt(0);

		name_prev = name;
		name = (String)strings.elementAt(1);

		// Because sometime the name is blank, use the node number if
		// necessary to give a unique identifier..

		// There is no limit to the characters available for MODSIM names,
		// allowing users to use a dot ('.') in the name.  Dots are reserved
		// in TS Identifiers, causing unexpected results.  The answer for now
		// is to lose the name information when this bug would be triggered.
		if ( (name.indexOf('.') >= 0) || name.equals("") ) {
			name = "";
			if ( file_has_nodes ) {
				name2 = "Node_" + node;
			}
			else {	name2 = "Link_" + node;
			}
		}
		else {	name2 = name;
		}
		if ( (name_prev.indexOf('.') >= 0) || name_prev.equals("") ) {
			name_prev = "";
			if ( file_has_nodes ) {
				name_prev2 = "Node_" + node_prev;
			}
			else {	name_prev2 = "Link_" + node_prev;
			}
		}
		else {	name_prev2 = name_prev;
		}

		//Message.printStatus ( 1, routine, "SAMX name=\"" + name +
		//"\" name2=\"" + name2 + "\" name_prev=\"" + name_prev +
		//"\" name_prev2=\"" + name_prev2 + "\"" );
		//Message.printStatus ( 1, routine, "SAMX node=\"" + node +
		//"\" node_prev=\"" + node_prev + "\"" );

		if ( getting_period ) {
			// Process a line of output to get the starting date...
			year = StringUtil.atoi((String)strings.elementAt(4));
			model_year =
				StringUtil.atoi((String)strings.elementAt(2));
			model_year1 = model_year;
			month = StringUtil.atoi((String)strings.elementAt(5));
			day = StringUtil.atoi((String)strings.elementAt(6));
			// Use from the data that was read with the random
			// access file...
			int year2 = StringUtil.atoi((String)
					endstrings.elementAt(4));
			int month2 = StringUtil.atoi((String)
					endstrings.elementAt(5));
			int day2 = StringUtil.atoi((String)
					endstrings.elementAt(6));
			if ( data_interval_base == TimeInterval.DAY ) {
				date1_file = new DateTime (
					DateTime.PRECISION_DAY );
				date2_file = new DateTime (
					DateTime.PRECISION_DAY );
				date1_file.setYear ( year );
				date1_file.setMonth ( month );
				date1_file.setDay ( day );
				date2_file.setYear ( year2 );
				date2_file.setMonth ( month2 );
				date2_file.setDay ( day2 );
			}
			else if ( data_interval_base == TimeInterval.MONTH ) {
				date1_file = new DateTime (
					DateTime.PRECISION_MONTH );
				date2_file = new DateTime (
					DateTime.PRECISION_MONTH );
				date1_file.setYear ( year );
				date1_file.setMonth ( month );
				date2_file.setYear ( year2 );
				date2_file.setMonth ( month2 );
			}
			// Reset to the first line of data...
			in.reset ();
			line_count = 1;
			// Save the dates that are actually used for reading...
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
			Message.printStatus ( 1, routine,
			"Period of file determined to be " +
			date1_file + " to " + date2_file );

			// No need to execute code below...

			getting_period = false;

			// Reinitialize...
			name = "";
			name_prev = "";
			node = "";
			node_prev = "";
			name2 = "";
			name_prev2 = "";
			model_year = -1;
			model_year_prev = -1;
			year = -1;
			continue;
		}

		if ( (req_ts != null) && !name2.equalsIgnoreCase(name_req_ts) ){
			// A specific time series has been requested but it
			// does not match this line of data so don't need to
			// process...
			if (	!read_data &&
				name_prev2.equalsIgnoreCase(name_req_ts) ) {
				// Have already processed the requested time
				// series and don't need data so can break...
				break;
			}
			else {	// Still looking...
				continue;
			}
		}

		// Below here filling in the time series data...

		model_year_prev = model_year;
		model_year = StringUtil.atoi((String)strings.elementAt(2));
		year = StringUtil.atoi((String)strings.elementAt(4));
		if ( !name2.equalsIgnoreCase(name_prev2) ) {
			// A new node is encountered.
			if ( model_year == model_year1 ) {
				// First time through so declare and initialize
				// time series...
				//Message.printStatus ( 1, routine,
				//"SAMX - defining new time series for " +
				//name2 );
				if ( req_ts != null ) {
					numts_read = 1;
					ts = req_ts;
					// Identifier is assumed to have been
					// set previously.
					tslist.addElement ( ts );
					if ( Message.isDebugOn ) {
						Message.printDebug ( 1, routine,
						"Adding existing requested " +
						"time series to list." );
					}
					// Set the data type in the TS header
					// using the information in the
					// identifier.
					ts.setDataType (
						(String)header.elementAt(
						req_ts_column) );
					ts.setDescription ( name2 + ", " +
						ts.getDataType() );
	
					ts.setDate1 ( date1 );
					ts.setDate1Original ( date1_file );
					ts.setDate2 ( date2 );
					ts.setDate2Original ( date2_file );
	
					if ( read_data ) {
						if (	ts.allocateDataSpace()
							!= 0 ) {
							Message.printWarning( 2,
							routine,
							"Error allocating " +
							"data space..." );
							// Clean up memory...
							ts = null;
							tslist = null;
							return null;
						}
					}
				}
				else {	// Allocate a new time series for each
					// data column...
					for ( its = 0; its < num_param; its++ ){
						if (	data_interval_base ==
							TimeInterval.DAY ) {
							identifier = name2 +
							".." + (String)
							header.elementAt(
							first_data_col + its) +
							".DAY";
						}
						else if(data_interval_base ==
							TimeInterval.MONTH ) {
							identifier = name2 +
							".." + (String)
							header.elementAt(
							first_data_col + its) +
							".MONTH";
						}
						ident = new TSIdent(identifier);
						ts = TSUtil.newTimeSeries (
							(String)identifier,
							true );
						if ( ts == null ) {
							Message.printWarning (
							2, routine,
							"Unable to create " +
							"new time series for" +
							" \""+identifier+"\"" );
							return null;
						}
						// Only set the identifier if a
						// new time series.  Otherwise
						// assume the the existing
						// identifier is to be used
						// (e.g., from a file name).
						ts.setIdentifier ( identifier );
						ts.getIdentifier().setInputType(
						"MODSIM");
						// Java TS carries around
						// separate variable for the
						// data type...
						ts.setDataType (
							ident.getType() );
						ts.setDescription ( name2 +", " +
							ts.getDataType() );
						tslist.addElement ( ts );
						if ( Message.isDebugOn ) {
							Message.printDebug ( 1,
							routine, "Created " +
							"memory for \"" +
							ts.getIdentifierString()
							+ "\"" );
						}
						ts.setDate1 ( date1 );
						ts.setDate1Original(date1_file);
						ts.setDate2 ( date2 );
						ts.setDate2Original(date2_file);
						if ( read_data ) {
							if (
							ts.allocateDataSpace()
							!= 0 ) {
							Message.printWarning( 2,
								routine,
								"Error " +
								"allocating " +
								"data " +
								"space..." );
								// Clean up
								// memory...
								ts = null;
								tslist = null;
								return null;
							}
						}
						// Increment the number of time
						// series that are read.
						++numts_read;
					}
				}
			}
			else {	// Year not equal to first year...
				if ( !read_data ) {
					// Don't need to read any more...
					break;
				}
			}
			if ( model_year != model_year_prev ) {
				//Message.printStatus ( 1, routine,
				//"Starting new year of model data for " +
				//model_year + " line " + line_count );
				if (	(req_ts == null) && read_data &&
					(model_year == (model_year1 + 1)) ) {
					// Second year of data.  To optimize
					// reading data below, define an array
					// to look up array positions.
					//Message.printStatus ( 1, "",
					//"SAMX - allocating array to increase"+
					//" performance model_year =" +
					//model_year );
					size = numts_read;
					ts_array = new TS[size];
					for ( its = 0; its < size; its++ ) {
						ts_array[its] =
						(TS)tslist.elementAt(its);
					}
				}
				// The model year has changed so the list of
				// nodes is starting at the beginning again.
				// Reset count of nodes with time series...
				node_in_model_year = 0;
			}
			// ts_pos is the position of the first time series for
			// the node.  It is used below when reading data to
			// locate the time series to populate...
			ts_pos = node_in_model_year*num_param;
			//Message.printStatus ( 1, "",
			//"SAMX node_in_model_year="+
			//node_in_model_year + " ts_pos=" + ts_pos +
			//" tslist.size()=" + tslist.size() );
			++node_in_model_year;
		}

		// Now assign the data.  First figure out the date...

		if ( !read_data ) {
			continue;
		}
		
		month = StringUtil.atoi((String)strings.elementAt(5));
		date.setYear ( year );
		date.setMonth ( month );
		if ( data_interval_base == TimeInterval.DAY ) {
			day = StringUtil.atoi((String)strings.elementAt(6));
			date.setDay ( day );
		}

		// Check to see if date is in the requested period...

		if ( date.lessThan(date1) ) {
			// No data of interest yet...
			strings = null;
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Ignoring data - before start date" );
			}
			continue;
		}
		else if ( date.greaterThan(date2) ) {
			// No need to save data but may need to keep reading...
			strings = null;
			if ( req_ts != null ) {
				// If here then a specific time series has been
				// requested and we are in the section for that
				// data.  Since only one can be requested, it is
				// OK to break out...
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine,
					"Stop reading data - after start date");
				}
				break;
			}
			else {	// Need to keep processing time series...
				continue;
			}
		}

		if ( req_ts != null ) {
			// A requested time series has been specified so only
			// transfer the single value
			dvalue = StringUtil.atod( ((String)
				strings.elementAt(req_ts_column)).trim());
			ts.setDataValue ( date, dvalue );
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl2, routine,
				"For date " + date.toString() +
				", value=" + dvalue );
			}
		}
		else {	// Loop through all the columns...
			for (	icol = first_data_col, its = ts_pos;
				icol < nstrings; icol++, its++ ){
				if ( ts_array != null ) {
					// Should be the case after one year...
					ts = ts_array[its];
				}
				else {	// First year will be slower...
					ts = (TS)tslist.elementAt( its );
				}
				dvalue = StringUtil.atod( ((String)
					strings.elementAt(icol)).trim());
				// Set the data value in the requested
				// time series.  If a requested time series is
				// being used, the array will only contain one
				// time series, which is the requested time
				// series...
				ts.setDataValue ( date, dvalue );
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl2, routine,
					"For date " + date.toString() +
					", value=" + dvalue );
				}
			}
		}
	}
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,
		"Error processing line " + line_count
		+ ": \"" + string + "\"" );
		Message.printWarning ( 2, routine, e );
	}

	if ( req_ts != null ) {
		req_ts.addToGenesis ( "Read MODSIM time series from " +
			ts.getDate1().toString() + " to " +
			ts.getDate2().toString() );
	}
	else {	for ( its = 0; its < num_param; its++ ) {
			ts = (TS)tslist.elementAt(its);
			ts.addToGenesis ( "Read MODSIM time series from " +
			ts.getDate1().toString() + " to " +
			ts.getDate2().toString() );
		}
	}
	ts = null;
	ts_array = null;
	return tslist;
}

} // End ModsimTS
