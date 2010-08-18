//------------------------------------------------------------------------------
// IrregularTS - class for encapsulating sparse/irregular/infrequent TS.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// History:
// 
// 24 Sep 1997	Matthew J. Rutherford,	Ported to Java.
//		RTi
// 30 Oct 1997	Steven A. Malers, RTi	Knowing that the records are sorted in
//					ascending order by date, optimized some
//					of the loops to break out at the
//					appropriate times rather than looping
//					through all of the data.
// 26 Nov 1997  Daniel Weiler, SAM, RTi Added enforceDataGaps
// 02 Jan 1998	SAM, RTi		Update so that _data_index is
//					used by setDataValue and getDataValue.
//					This greatly speeds the access speed
//					when retrieving data from a time series
//					(it does not really help when initially
//					filling the time series).  This change
//					involved changing the previous
//					_loop_index to _data_index and using
//					_data_index inside of appropriate loops.
//					Also update so that -999 is not assumed
//					to be the missing data value (use
//					value returned by getMissing() instead).
//					Move calcMaxMinValues to TSUtil and add
//					refresh.
// 13 Mar 1998	SAM, RTi		Add calculateDataSize.
// 25 Mar 1998	SAM, RTi		Add javadoc.
// 13 Jul 1998	SAM, RTi		Fix bug in getDataValue where if data
//					is not found, the index was still being
//					used to return a data value.
// 22 Aug 1998	SAM, RTi		Change getDataPosition return type to
//					int [] to match base class.
// 12 Apr 1999	SAM, RTi		Add finalize.
// 11 Oct 2000	SAM, RTi		Add iterator() meethods.
// 21 Feb 2001	SAM, RTi		Add clone() and copy constructor.
// 30 Aug 2001	SAM, RTi		Fix clone() to be correct.  Clean up
//					Javadoc and set variables to null.
//					Get rid of redundant new String() calls.
//					Fix so in setDataValue() the next and
//					previous pointers are set (why wasn't
//					this ever done before?).
// 2001-11-06	SAM, RTi		Again...review javadoc.  Verify that
//					variables are set to null when no longer
//					used.  Remove construct from file since
//					it should never be called.  Change some
//					methods to have void return type to
//					agree with base class.
// 2002-06-16	SAM, RTi		Fix clone() yet again.
// 2002-09-10	SAM, RTi		Overload setDataValue() to take a flag
//					and duration.
// 2003-01-08	SAM, RTi		Add hasData().
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TSUnits to DataUnits.
//					* Change TS.INTERVAL* to TimeInterval.
// 2003-07-24	SAM, RTi		* Remove getDataDate().  It is not used
//					  by any current iteration code so
//					  remove.
//					* TSIterator now throws an Exception so
//					  declare the throw in iterator().
// 2003-12-01	SAM, RTi		* Override the base class getDate1() and
//					  getDate2() - return from the data
//					  array.
//					* Change private data to use __ as per
//					  other RTi code.
// 2004-03-04	J. Thomas Sapienza, RTi	* Class now implements Serializable.
//					* Class now implements Transferable.
//					* Class supports being dragged or 
//					  copied to clipboard.
// 					* Data members are no longer transient.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
This class stores irregular time step data.  Because the time step is irregular,
a date must be stored with each recording.  The time interval base is
TimeInterval.IRREGULAR.  Use the IrregularTSIterator class to get data or
retrieve the data array and process as a Vector.
@see #getData
*/
public class IrregularTS extends TS
implements Cloneable, Serializable, Transferable {

/**
The DataFlavor for transferring this specific class.
*/
public static DataFlavor irregularTSFlavor = new DataFlavor(RTi.TS.IrregularTS.class, "RTi.TS.IrregularTS");

/**
List of data points, initially null and will be initialized on first data point set.
*/
private	List<TSData> __ts_data_head = null;

/**
Previous setData() call pointer.  This is used to optimize set calls.
*/
private TSData __prevSetDataPointer = null;

/**
Previous removeDataPoint() call pointer (for next point).  This is used to optimize remove calls.
The next point is needed because if the previous point was removed, it will not be itself be available.
*/
private TSData __prevRemoveDataPointerNext = null;

/**
Index used when traversing the data array.
This is the element in _ts_data_head (0+) that was last accessed.
*/
protected int __data_index;

/**
Default constructor.  The data array is initialized to null.
*/
public IrregularTS ( )
{	super();
	init();
}

/**
Copy constructor.  Everything is copied by calling copyHeader() and then copying the data values.
*/
public IrregularTS ( IrregularTS ts )
{	if ( ts == null ) {
		return;
	}
	copyHeader ( ts );
	// Get the data and loop through the vector...
	List<TSData> all_tsdata = ts.getData();
	if ( all_tsdata == null ) {
		// No data for the time series...
		__data_index = ts.__data_index;
		return;
	}
	int nalltsdata = all_tsdata.size();
	//__ts_data_head = new Vector ( nalltsdata );
	TSData tsdata = null;
	for ( int i = 0; i < nalltsdata; i++ ) {
		// Would be nice to do this but the next/prev seem to get fouled up.
		//__ts_data_head.addElement (new TSData((TSData)tsdata.elementAt(i)) );
		tsdata = all_tsdata.get(i);
		if ( tsdata != null ) {
			// This is what actually causes the copy...
			setDataValue ( tsdata.getDate(), tsdata.getData() );
		}
	}
	__data_index = ts.__data_index;
	tsdata = null;
	addToGenesis ( "Copied from \"" + ts.getIdentifierString() + "\"" );
}

/**
Allocate the data flag space for the time series.
This is a place-holder and is not used since data
space is adjusted as necessary as data are added with setDataValue().
@param data_flag_length Maximum length of data flags.  If the data flags array
is already allocated, then the flag size will be increased by the specified
length.  This allows multiple flags to be concatenated.
@param initial_value Initial value (null is allowed and will result in the flags being initialized to spaces).
@param retain_previous_values If true, the array size will be increased if necessary, but
previous data values will be retained.  If false, the array will be reallocated and initialized to spaces.
@exception Exception if there is an error allocating the memory.
*/
public void allocateDataFlagSpace (	String initial_value, boolean retain_previous_values )
throws Exception
{	return;
}

/**
Allocate the data space.  This is a place-holder and is not used since data
space is adjusted as necessary as data are added with setDataValue().
*/
public int allocateDataSpace()
{	return 0;
}

/**
Calculate the number of data points between two dates.
<b>This method is not valid for IrregularTS but is maintained for compatibility.  Use the non-static calculateDataSize() method.</b>
@return A count of the data recordings between the given dates (always zero here).
@param start_date First date of interest.
@param end_date Last date of interest.
@param interval_mult Data interval multiplier (not used).
*/
public static int calculateDataSize ( DateTime start_date, DateTime end_date, int interval_mult )
{	Message.printWarning ( 1, "IrregularTS.calculateDataSize",
	"Must have an instance of IrregularTS to compute the data size.  Use " +
	"the non-static calculateDataSize() method" );
	return 0;
}

/**
Calculate the number of data points between two dates.
@return A count of the data recordings between the given dates.
@param start_date First date of interest.
@param end_date Last date of interest.
*/
public int calculateDataSize (	DateTime start_date, DateTime end_date )
{	int	datasize = 0;

	if ( __ts_data_head == null ) {
		return 0;
	}

	// Loop through the data and count the intervals in the given period...

	DateTime date = new DateTime (DateTime.DATE_STRICT);
	TSData ptr = null;
	int	size = __ts_data_head.size();
	for ( int i = 0; i < size; i++ ) {
		ptr = __ts_data_head.get(i);
		date = ptr.getDate();

		if ( date.lessThan(start_date) ) {
			// Still looking for data...
			continue;
		}
		else if ( date.greaterThan(end_date) ) {
			// No need to continue processing...
			break;
		}
		// Add to the counter...
		++datasize;
	}
	return datasize;
}

/**
Clone the object.  The TS base class clone() method is called and then the
the data array is cloned.  The result is a complete deep copy.
*/
public Object clone() {
	IrregularTS ts = (IrregularTS)super.clone();	// Clone base class data

	ts.copyHeader ( this );
	// Get the data and loop through the vector...
	List<TSData> all_tsdata = getData();
	if ( all_tsdata == null ) {
		// No data for the time series...
		ts.__data_index = __data_index;
		return ts;
	}
	int nalltsdata = all_tsdata.size();
	TSData tsdata = null;
	for ( int i = 0; i < nalltsdata; i++ ) {
		tsdata = all_tsdata.get(i);
		if ( tsdata != null ) {
			// This is what actually causes the copy...
			ts.setDataValue ( tsdata.getDate(), tsdata.getData() );
		}
	}
	ts.__data_index = __data_index;
	tsdata = null;
	addToGenesis ( "Cloned from \"" + ts.getIdentifierString() + "\"" );
	return ts;
/*
	// Make sure it is cleared out...
	ts.__ts_data_head = null;
	// Get the data and loop through the vector...
	if ( __ts_data_head != null ) {
		int nalltsdata = __ts_data_head.size();
		// Save copy...
		TSData tsdata = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			// Would be nice to do this but the next/prev seem to
			// get fouled up.
			//__ts_data_head.addElement (
			//	new TSData((TSData)tsdata.elementAt(i)) );
			tsdata = (TSData)__ts_data_head.elementAt(i);
			if ( tsdata != null ) {
				// This is what actually causes the copy...
				ts.setDataValue ( tsdata.getDate(),
				tsdata.getData() );
			}
		}
		tsdata = null;
	}
	ts.__data_index = __data_index;
	return ts;
*/
}

/**
Insert missing data points where a gap between known points is more than the
specified interval.  This is used to more accurately represent the irregular
data (e.g., if an IrregularTS is used to record values that actually are
recorded on a regular time step but are recorded infrequently).
<b>NOTE:  This method is not really needed for graphing anymore because the
GRTS TSView* package correctly displays irregular data.</b>
@return Zero if successful, one if not.
@param interval_base Base interval of the maximum allowable gap.
@param interval_mult Interval multiplier of the maximum allowable gap.
@param start_date First date of interest.
@param end_date Last date of interest.
*/
/* SAM TODO - need to change TSDateIterator to IrregularTSIterator
public int enforceDataGaps (	int interval_base, int interval_mult,
				DateTime date1, DateTime date2)
{	String	routine = "IrregularTS.enforceDataGaps";

	// Loop through the data using the given dates.  If the dates are not
	// specified, use the time series start and end dates.  Do this by
	// filling out the TS.getValidPeriod function.

	int	dl = 20;
	double	data_value = 0.0;
	TSLimits dates = TSUtil.getValidPeriod ( this, date1, date2 );
	DateTime	start = new DateTime ( dates.getNonMissingDataDate1() );
	DateTime	end = new DateTime ( dates.getNonMissingDataDate2() );
	DateTime 	t = new DateTime(),
			prevDate = new DateTime();

	// Now loop through the period.  If between any known data values the
	// time gap is longer than the interval indicated above, insert ONE
	// missing data value in the gap at some date in the middle of the
	// gap.
	
	prevDate = start;
	TSDateIterator tsdi;
	int missingcount = 0, datacount = 0;
	for ( tsdi = new TSDateIterator( this, start, end ); 
		!tsdi.isIterationComplete(); tsdi.advanceDate(), ++datacount ){

		t = tsdi.getCurrentDate();
		data_value = getDataValue(t);

		if ( Message.isDebugOn ) {
			Message.printDebug ( 2, routine,
			"Processing date " + t + " in period " +
			start + " to " + end );
		}
	
		if( !t.equals(start) ) {
			prevDate.addInterval( interval_base, interval_mult );
			if( prevDate.lessThan(t) && !isDataMissing(data_value)){
				// We have a gap that is not bounded by missing
				// data already so insert a missing data
				// point...
				setDataValue( prevDate, _missing );
				++missingcount;
				if( Message.isDebugOn ) {
					Message.printDebug(5, routine,
					" Set MISSING value at: " + prevDate);
				}
			}	
			prevDate = t;
		}
	
	}
	tsdi = null;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Set " + missingcount +
		" missing values after checking " + datacount + " values" );
	}
	routine = null;
	dates = null;
	start = null;
	end = null;
	t = null;
	prevDate = null;
	return 0;	
}
*/

/**
Finalize before garbage collection.
*/
protected void finalize()
throws Throwable
{	__ts_data_head = null;
	super.finalize();
}

/**
Format the time series for output.  This is not meant to be used for time
series conversion but to produce a general summary of the output.  At this time,
irregular time series are always output in a sequential summary format.
@return list of strings that can be displayed, printed, etc.
@param proplist Properties of the output, as described in the following table:

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>Delimiter</b></td>
<td>The delimiter to use for spreadsheet output.  Can be a single or multiple characters.
</td>
<td>|</td>
</tr>

<tr>
<td><b>Format</b></td>
<td>The overall format of the output.  Can be either "Summary" for an array
of data or "Spreadsheet" for a delimited
file suitable for a spreadsheet (see the "Delimiter" property).  At this time,
the determination of whether total or average annual values are shown is made
based on units.  AF, ACFT, IN, INCH, FEET, and FOOT are treated as totals and
all others as averages.  A more rigorous handling is being implemented to
use the units dimension to determine totals, etc.</td>
<td>Summary</td>
</tr>

<tr>
<td><b>Precision</b></td>
<td>The precision of numbers as printed.  All data values are printed in a
9-digit column.  The precision controls how many digits are shown after the
decimal.  If not specified, an attempt will be made to use the units to
look up the precision.  If that fails, a default of 1 will be used.
<td>Summary</td>
</tr>

<tr>
<td><b>PrintHeader</b></td>
<td>Print the time series header information in a format as follows:
<p>
<pre>
Time Series Identifier  = 07126500.CRDSS_USGS.streamflow.MONTH
Description             = PURGATOIRE RIVER AT NINEMILE DAM, NR HIGBEE, CO.
Data source             = CRDSS_USGS
Data type               = streamflow
Data units              = ACFT
Data interval           = MONTH
Requested Period        = 1980-01 to 1990-01
Available Period        = 1924-01 to 1995-12
</pre>
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintComments</b></td>
<td>Print the comments associated with the time series.  This may contain
information about the quality of data, station information, etc.  This
information is usually viarable-length text, and may not be available.
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintAllStats</b></td>
<td>Print all the statistics (currently maximum, minimum, and mean, although
standard deviation and others are being added).  Because statistics are being
added to output, it is advised that if formatting is to remain the same over
time, that output items be individually specified.  One way of doing this is
to turn all the statistics off and then turn specific items on (to true).
</td>
<td>false</td>
</tr>

<tr>
<td><b>PrintGenesis</b></td>
<td>Print the time series genesis information after the header in a
format as follows:
<p>
<pre>
Time series genesis (creation history):
Read from XXXX database.
Filled missing data with...
etc.
</pre>
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintMinStats</b></td>
<td>Print the minimum value statistics.
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintMaxStats</b></td>
<td>Print the maximum value statistics.
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintMeanStats</b></td>
<td>Print the mean value statistics.
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintNotes</b></td>
<td>Print notes about the output.  This consists of helpful information used
to understand the output (but does not consist of data).  For example:
<p>
<pre>
Notes:
  Years shown are calendar years.
  Annual values and statistics are computed only on non-missing data.
  NC indicates that a value is not computed because of missing data or the data value itself is missing.
</pre>
</td>
<td>true</td>
</tr>

<tr>
<td><b>UseCommentsForHeader</b></td>
<td>Use the time series comments for the header and do not print other header
information.  This can be used when the entire header is formatted elsewhere.
</td>
<td>false</td>
</tr>

</table>
@exception RTi.TS.TSException Throws if there is a problem formatting the
output.
*/
public List<String> formatOutput( PropList proplist )
throws TSException
{	String message = "", routine = "Irregular.formatOutput";	
	int dl = 20;
	List<String> strings = new Vector (20,10);
	PropList props = null;
	String format = "", prop_value = null;
	String data_format = "%9.1f";

	// If the property list is null, allocate one here so we don't have to constantly check for null...

	if ( proplist == null ) {
		// Create a PropList so we don't have to check for nulls all the time.
		props = new PropList ( "formatOutput" );
	}
	else {
	    props = proplist;
	}

	// Get the important formatting information from the property list...

	// Get the overall format...

	prop_value = props.getValue ( "Format" );
	if ( prop_value == null ) {
		// Default to "Summary"...
		format = "Summary";
	}
	else {
	    // Set to requested format...
		format = prop_value;
	}

	// Determine the units to output.  For now use what is in the time series...

	String req_units = _data_units;

	// Get the precision...

	prop_value = props.getValue ( "Precision" );
	if ( prop_value == null ) {
		// Try to get units information for default...
		try {
		    DataUnits u = DataUnits.lookupUnits ( req_units );
			data_format = "%9." + u.getOutputPrecision() + "f";
		}
		catch ( Exception e ) {
			// Default...
			data_format = "%9.1f";
		}
	}
	else {
	    // Set to requested precision...
		data_format = "%9." + prop_value + "f";
	}

	// Determine the period to output.  For now always output the total...

	if ( (_date1 == null) || (_date2 == null) ) {
		message = "Null period dates for time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	DateTime start_date = new DateTime (_date1);
	DateTime end_date = new DateTime (_date2);

	// Now generate the output based on the format...

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Creating output in format \"" + format + "\"" );
	}
	if ( format.equalsIgnoreCase("Spreadsheet") ) {
		// Spreadsheet
		prop_value = props.getValue ( "Delimiter" );
		if ( prop_value == null ) {
			// Default to "|"...
			format = "|";
		}
		else {
		    // Set to requested delimiter...
			format = prop_value;
		}
		Message.printWarning ( 1, routine, "Spreadsheet output format is not implemented" );
		return strings;
	}
	else if ( format.equalsIgnoreCase("Summary") ) {
		// Print the header for the matrix...

		prop_value = props.getValue ( "PrintHeader" );
		String print_header = null;
		if ( prop_value == null ) {
			// Default is true...
			print_header = "true";
		}
		else {
		    print_header = prop_value;
		}
		prop_value = props.getValue ( "UseCommentsForHeader" );
		String use_comments_for_header = null;
		if ( prop_value == null ) {
			// Default is false...
			use_comments_for_header = "false";
		}
		else {
		    use_comments_for_header = prop_value;
		}
		if ( print_header.equalsIgnoreCase("true") ) {
			if ( !use_comments_for_header.equalsIgnoreCase("true")){
				// Format the header...
				strings.add ( "" );
				List<String> strings2 = formatHeader();
				StringUtil.addListToStringList ( strings, strings2 );
			}
		}
		
		// Add comments if available...

		prop_value = props.getValue ( "PrintComments" );
		String print_comments = null;
		if ( prop_value == null ) {
			// Default is true...
			print_comments = "true";
		}
		else {
		    print_comments = prop_value;
		}
		if ( print_comments.equalsIgnoreCase("true") || use_comments_for_header.equalsIgnoreCase("true")){
			strings.add ( "" );
			if ( _comments != null ) {
				int ncomments = _comments.size();
				if ( !use_comments_for_header.equalsIgnoreCase("true")){
					strings.add ( "Comments:" );
				}
				if ( ncomments > 0 ) {
					for ( int i = 0; i < ncomments; i++ ) {
						strings.add( _comments.get(i));
					}
				}
				else {
					strings.add("No comments available.");
				}
			}
			else {
			    strings.add( "No comments available.");
			}
		}
		print_comments = null;
		use_comments_for_header = null;

		// Print the genesis information...

		prop_value = props.getValue ( "PrintGenesis" );
		String print_genesis = null;
		if ( prop_value == null ) {
			// Default is true...
			print_genesis = "true";
		}
		else {
		    print_genesis = prop_value;
		}
		if ( (_genesis != null) && print_genesis.equalsIgnoreCase("true") ) {
			int size = _genesis.size();
			if ( size > 0 ) {
				strings.add ( "" );
				strings.add ( "Time series creation history:" );
				strings = StringUtil.addListToStringList(strings, _genesis );
			}
		}
		print_genesis = null;
	
		// Print the body of the summary...

		// Need to check the data type to determine if it is an average
		// or a total.  For now, make some guesses based on the units...

		strings.add ( "" );
		
		if ( __ts_data_head == null ) {
			// No data for the time series...
			strings.add ( "No data available." );
			return strings;
		}

		strings.add ( "Date                          Value" );
		strings.add ( "--------------------------------------------" );

		// Now loop through the time series and transfer to the proper location in the matrix...
		double data_value;
		int nalltsdata = __ts_data_head.size();
		TSData tsdata = null;
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = __ts_data_head.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end_date) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start_date) ) {
				data_value = tsdata.getData();
				// Format the date according to the active
				// date precision but allow room for full date, to line up with headers...
				strings.add ( StringUtil.formatString(date.toString(),
				"%-26.26s") + "  " + StringUtil.formatString(data_value,data_format) );
			}
		}
		strings.add ( "--------------------------------------------" );
		// Now need to do the statistics.  Loop through each column...
		// First check to see if all stats should be printed (can be
		// dangerous if we add new statistics)..
		// NOT ENABLED IN IRREGULAR TIME SERIES
	}
	else {
	    message = "Unrecognized format: \"" + format + "\"";
		Message.printWarning ( 1, routine, message );
		throw new TSException ( message );
	}

	// Print footnotes to the output...
	return strings;
}

/**
Format the time series for output.
@return list of strings that are written to the file.
@param fp Writer to receive output.
@param props Properties to modify output.
@exception RTi.TS.TSException Throws if there is an error writing the output.
*/
public List<String> formatOutput ( PrintWriter fp, PropList props )
throws TSException
{	List<String> formatted_output = null;
	String routine = "MonthTS.formatOutput(Writer,props)";
	int	dl = 20;
	String message;

	if ( fp == null) {
		message = "Null PrintWriter for output";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// First get the formatted output...

	try {
	    formatted_output = formatOutput ( props );
		if ( formatted_output != null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Formatted output is " + formatted_output.size() + " lines" );
			}
	
			// Now write each string to the writer...

			String newline = System.getProperty ( "line.separator");
			int size = formatted_output.size();
			for ( int i = 0; i < size; i++ ) {
				fp.print ( formatted_output.get(i) + newline );
			}
			newline = null;
		}
	}
	catch ( TSException e ) {
		// Rethrow...
		throw e;
	}

	// Also return the list

	return formatted_output;
}

/**
Format the time series for output.
@return List of strings that are written to the file.
@param fname Name of output.
@param props Property list containing output modifiers.
@exception RTi.TS.TSException Throws if there is an error writing the output.
*/
public List<String> formatOutput ( String fname, PropList props )
throws TSException
{	String message = null, routine = "IrregularTS.formatOutput";
	List<String> formatted_output = null;
	PrintWriter	stream = null;

	// First open the output file...

	try {
	    stream = new PrintWriter ( new FileWriter(fname) );
	}
	catch ( Exception e ) {
		message = "Unable to open file \"" + fname + "\"";
		throw new TSException ( message );
	}
	if ( stream == null ){
		message = "Unable to open file \"" + fname + "\"";
		Message.printWarning( 2, routine, message );
		throw new TSException ( message );
	}

	try {
	    formatted_output = formatOutput ( stream, props );
		stream.close();
		stream = null;
	}
	catch ( TSException e ) {
		// Rethrow...
		throw e;
	}

	// Also return the list (consistent with C++ single return type.

	return formatted_output;
}

/**
Return the data array list.
@return The reference to the data array.  Use caution when manipulating.
*/
public List<TSData> getData()
{	return __ts_data_head;
}

/**
Return a data point for a date.
@return A TSData for the date of interest.
@param date Date of interest.
*/
public TSData getDataPoint ( DateTime date, TSData data_point )
{	int	i = 0;
	TSData ptr = null;

	if ( data_point == null ) {
	    data_point = new TSData();
	}
	data_point.setDate ( date );
	data_point.setUnits ( _data_units );
	data_point.setDataFlag ( "" ); // default
	data_point.setDuration ( 0 ); // default

	//Check the date coming in 

	if ( __ts_data_head == null ) {
		// No data!
		// Leave __data_index as is.
		data_point.setData( _missing );
		return data_point;
	}

	if ( date.lessThan(_date1) || date.greaterThan(_date2) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 30, "IrregularTS.getDataPoint",
			date + " not within POR (" + _date1 + " - " + _date2 + ")" );
			// Leave __data_index as is.
			data_point.setData( _missing );
			return data_point;
		}
	}

	// First check to see if the requested date is the next in line from
	// the previous access.  Do so by getting the data point from using
	// __data_index and then check its date to see if it matches the
	// requested date.  At this point, if they do not match, just go to the
	// next search techniques.  It is likely that the first call will not
	// use the following method because a starting value needs to be found.

	TSData next_data = getNextElement ();
	if ( next_data != null ) {
		// Do this code.  Otherwise, cascade to the next search techniques...
		DateTime next_date = next_data.getDate();
		if ( next_date.equals(date) ) {
			// We have a match!  Increment the date index so that
			// we can use it next time and return the matching TSData...
			++__data_index;
			return next_data;
		}
	}

	//
	// Determine if the date is closer to the tail of the list, or the head.
	//
	double date_double = date.toDouble();
	double date1_double = _date1.toDouble();
	double date2_double = _date2.toDouble();

	int size = __ts_data_head.size();
	int found_index = -1;
	if ( Math.abs( date_double - date1_double ) < Math.abs( date_double - date2_double ) ){
		for ( i=0; i < size; i++ ) {
			ptr = __ts_data_head.get(i); 
			if( ptr.getDate().equals( date ) ){
				found_index = i;
				break;
			}
		}
	}
	else {
	    for ( i=(size-1); i >= 0; i-- ) {
			ptr = __ts_data_head.get(i); 
			if( ptr.getDate().equals( date ) ) {
				found_index = i;
				break;
			}
		}
	}

	if( ptr == null ){
		Message.printWarning( 2, "IrregularTS.getDataPoint", "Cannot find data value in inernal time series." );
		// Leave __data_index as is.
		data_point.setData( _missing );
		return data_point;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug( 30, "IrregularTS.getDataPoint",
		ptr.getData() + " for " + date + " from _data[" + i + "]." );
	}

	// Set the data index to the found point and then return the data value...

	if ( found_index >= 0 ) {
		__data_index = found_index;
	}
	else {
	    data_point.setData ( _missing );
		return data_point;
	}

	// Return the data point...

	data_point.setData ( ptr.getData() );
	data_point.setDataFlag ( ptr.getDataFlag() );
	data_point.setDuration( ptr.getDuration() );

	return data_point;
}

/**
Return the data value for a date.
This routine uses some intelligence to try to find the data value as quickly as
possible.  This routine is most often called after the time series as been
filled by setDataValue() calls, and it is usually called in a loop where data
are accessed one after the other in a sequence.  Therefore, the fastest
retrieval is usually attained by incrementing one point from the previously
accessed point.  This method is checked first and then checks to search from
the front or back are made.  At this time a bisection algorithm is not employed
but might need to be in the future to increase performance.
@return The data value in the data array given a date, or the missing data
value if the date cannot be found in the data.
@param date Date of interest.
*/
public double getDataValue( DateTime date )
{	// Do not define routine here to increase performance.
	int	dl = 30, found_index = -1, i = 0;
	TSData ptr=null;

	//Check the date coming in 

	if ( __ts_data_head == null ) {
		// No data!  Leave __data_index as is.
		return _missing;
	}

	if ( date.lessThan(_date1) || date.greaterThan(_date2) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, "IrregularTS.getDataValue", date + " not within POR (" + _date1 + " - " + _date2 + ")" );
		}
		// Leave __data_index as is.
		return _missing;
	}

	// First check to see if the requested date is the next in line from
	// the previous access.  Do so by getting the data point from using
	// __data_index and then check its date to see if it matches the
	// requested date.  At this point, if they do not match, just go to the
	// next search techniques.  It is likely that the first call will not
	// use the following method because a starting value needs to be found.

	TSData next_data = getNextElement ();
	if ( next_data != null ) {
		// Do this code.  Otherwise, cascade to the next search techniques...
		DateTime next_date = next_data.getDate();
		if ( next_date.equals(date) ) {
			// We have a match!  Increment the date index so that
			// we can use it next time and return the matching TSData...
			++__data_index;
			return next_data.getData();
		}
	}

	//
	// Determine if the date is closer to the tail of the list, or the head.
	//
	double date_double = date.toDouble();
	double date1_double = _date1.toDouble();
	double date2_double = _date2.toDouble();

	int size = __ts_data_head.size();
	if ( Math.abs( date_double - date1_double ) < Math.abs( date_double - date2_double ) ){
		for ( i=0; i < size; i++ ) {
			ptr = __ts_data_head.get(i); 
			if( ptr.getDate().equals( date ) ){
				found_index = i;
				break;
			}
		}
	}
	else {
	    for ( i=(size-1); i >= 0; i-- ){
			ptr = __ts_data_head.get(i); 
			if( ptr.getDate().equals( date ) ){
				found_index = i;
				break;
			}
		}
	}

	if( ptr == null ){
		Message.printWarning( 2, "IrregularTS.getDataValue", "Unable to find data value in time series." );
		// Leave __data_index as is.
		return _missing;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug( dl, "IrregularTS.getDataValue",
		ptr.getData() + " for " + date + " from _data[" + i + "]." );
	}

	if ( found_index < 0 ) {
		// Did not find the data...
		Message.printDebug ( dl, "IrregularTS.getDataValue", "Can't find data matching date " + date );
		return _missing;
	}

	// Set the data index to the found point and then return the data value...
	__data_index = found_index;
	return ptr.getData();
}

/**
Return the first date in the data.  This is taken from the first data point
if data are available, or the base class method, if data are not available
(e.g., in cases when the time series header is defined without data).
*/
public DateTime getDate1()
{	if ( (__ts_data_head == null) || (__ts_data_head.size() == 0) ) {
		return super.getDate1();
	}
	else {
	    return __ts_data_head.get(0).getDate();
	}
}

/**
Return the last date in the data.  This is taken from the last data point
if data are available, or the base class method, if data are not available
(e.g., in cases when the time series header is defined without data).
*/
public DateTime getDate2()
{	if ( (__ts_data_head == null) || (__ts_data_head.size() == 0) ) {
		return super.getDate2();
	}
	else {
	    return __ts_data_head.get(__ts_data_head.size() - 1).getDate();
	}
}

/**
Get the next element in the data vector.
This method can be used to return the element after the previously accessed
element.  For example, when searching the data array, this routine can be used
to get the next data value.  The routine returns null if at the end of the data array.
@return The next data point after the previous access.
*/
public TSData getNextElement()
{	// Do not define routine here to increase performance.
	TSData tsdata=null;

	if ( __ts_data_head == null ) {
		return null;
	}

	int	size = __ts_data_head.size();

	// We are going to get the element after "__data_index" so do the
	// appropriate checks.  First make sure that we will not exceed the
	// bounds of the data vector...

	if ( ((__data_index + 1) >= size) || ((__data_index + 1) < 0) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug( 20, "IrregularTS.getNextElement",
			"Data index " + __data_index + " out of range, returning NULL.");
		}
		return null;
	}

	// Now return the next data value as a copy...

	tsdata = __ts_data_head.get( __data_index + 1 );

	return (TSData)tsdata.clone();
}

/**
Returns the data in the specified DataFlavor, or null if no matching flavor
exists.  From the Transferable interface.  Supported dataflavors are:<br>
<ul>
<li>IrregularTS - IrregularTS.class / RTi.TS.IrregularTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul> 
@param flavor the flavor in which to return the data.
@return the data in the specified DataFlavor, or null if no matching flavor exists.
*/
public Object getTransferData(DataFlavor flavor) {
	if (flavor.equals(irregularTSFlavor)) {
		return this;
	}
	else if (flavor.equals(TS.tsFlavor)) {
		return this;
	}
	else if (flavor.equals(TSIdent.tsIdentFlavor)) {
		return _id;
	}
	else {
		return null;
	}
}

/**
Returns the flavors in which data can be transferred.  From the Transferable interface.  
The order of the dataflavors that are returned are:<br>
<ul>
<li>IrregularTS - IrregularTS.class / RTi.TS.IrregularTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@return the flavors in which data can be transferred.
*/
public DataFlavor[] getTransferDataFlavors()
{
	DataFlavor[] flavors = new DataFlavor[3];
	flavors[0] = irregularTSFlavor;
	flavors[1] = TS.tsFlavor;
	flavors[2] = TSIdent.tsIdentFlavor;
	return flavors;
}

/**
Indicate whether the time series has data, determined by checking to see whether
the data space has &gt; 0 data points.  This method can be called after a time
series has been read - even if no data are available, the header information
may be complete.  The alternative of returning a null time series from a read
method if no data are available results in the header information being
unavailable.  Instead, return a TS with only the header information and call
hasData() to check to see if the data space has been assigned.
@return true if data are available (the data space has been allocated).
Note that true will be returned even if all the data values are set to the missing data value.
*/
public boolean hasData ()
{	if ( (__ts_data_head != null) && (__ts_data_head.size() > 0) ) {
		return true;
	}
	else {
	    return false;
	}
}

/**
Initialize data.
*/
private void init(  )
{	_data_interval_base = TimeInterval.IRREGULAR;
	_data_interval_mult = 1;
	_data_interval_base_original = TimeInterval.IRREGULAR;
	_data_interval_mult_original = 1;
	__ts_data_head = null;
	__data_index = -1;
}

/**
Determines whether the specified flavor is supported as a transfer flavor.
From the Transferable interface.  Supported dataflavors are:<br>
<ul>
<li>IrregularTS - IrregularTS.class / RTi.TS.IrregularTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul> 
@param flavor the flavor to check.
@return true if data can be transferred in the specified flavor, false if not.
*/
public boolean isDataFlavorSupported(DataFlavor flavor) {
	if (flavor.equals(irregularTSFlavor)) {
		return true;
	}
	else if (flavor.equals(TS.tsFlavor)) {
		return true;
	}
	else if (flavor.equals(TSIdent.tsIdentFlavor)) {
		return true;
	}
	else {
		return false;
	}
}

/**
Return an iterator for the time series using the full period for the time series.
@return an iterator for the time series.
@exception Exception if the time series dates are null.
*/
public TSIterator iterator ()
throws Exception
{	return new IrregularTSIterator ( this );
}

/**
Return an iterator for the time series using the specified period.
For regular interval time series, the iterator is that same. IrregularTS use the IrregularTSIterator.
@param date1 Start of data iteration.
@param date2 End of data iteration.
@return an iterator for the time series.
@exception Exception if the time series dates are null.
*/
public TSIterator iterator ( DateTime date1, DateTime date2 )
throws Exception
{	return new IrregularTSIterator ( this, date1, date2 );
}

/**
Refresh the derived data in the time series (e.g., recompute limits if data
has been set).  This is typically only called from other package routines.
*/
public void refresh ()
{	// If the data is not dirty, then we do not have to refresh the other information...

	if ( !_dirty ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 30, "IrregularTS.refresh", "Time series is not dirty.  Not recomputing limits" );
		}
		return;
	}

	// Else we need to refresh...

	if ( Message.isDebugOn ) {
		Message.printDebug( 30, "IrregularTS.refresh", "Time Series is dirty. Recomputing limits" );
	}

	TSLimits limits = TSUtil.getDataLimits ( this, _date1, _date2, false );
	if ( limits.areLimitsFound() ) {
		// Now reset the limits for the time series...
		setDataLimits ( limits );
	}

	_dirty = false;
}

/**
Remove a data point corresponding to the date.
@param date date/time for which to remove the data point.
*/
public void removeDataPoint ( DateTime date )
{
    if ( date == null ) {
        return;
    }
    int size = getDataSize();
    if ( size == 0 ) {
        // No action
        return;
    }
    else if ( size == 1 ) {
        // Remove the head
        __ts_data_head = null;
        __prevSetDataPointer = null;
        setDataSize(0);
        _dirty  = true;
        return;
    }
    // If here, need to search through the list and find the point
    boolean pointFound = false;
    // First check the previous set call and determine if the point is next on the list
    // This is quite common and so this code greatly improves performance
    TSData ptr = null;
    if ( __prevRemoveDataPointerNext != null ) {
        ptr = __prevRemoveDataPointerNext;
        if ( ptr != null ) {
            if ( ptr.getDate().equals(date) ) {
                // Have found the point
                pointFound = true;
            }
        }
    }
    if ( !pointFound ) {
        // Do a more exhaustive loop.
        // FIXME SAM 2010-08-17 Need to optimize search for the date, relinking list, etc.
        for ( TSData ptr2 : __ts_data_head ) {
        
            //if ( Message.isDebugOn ) {
            //  Message.printDebug( 50, "IrregularTS.setDataValue", "Comparing " + dateLocal + " to " + ptr.getDate() );
            //}
            
            if( ptr2.getDate().equals( date ) ) {
                // Found match.
                ptr = ptr2;
                pointFound = true;
            }
        }
    }
    if ( pointFound ) {
        // Do the removal - reroute pointers and then remove point from the list
        __prevRemoveDataPointerNext = ptr.getNext(); // Save for next call
        ptr.getPrevious().setNext(ptr.getNext());
        ptr.getNext().setPrevious(ptr.getPrevious());
        __ts_data_head.remove(ptr);
        // Mark dirty so that we recompute the data limits...
        _dirty  = true;
        // Decrement the data size...
        setDataSize ( getDataSize() - 1 );
    }
}

/**
Set the data value for the given date.  If the date has not already been set
with a value, add a data point in the proper order.  This calls the overloaded
method with a "" flag and duration of 0.
@param date Date of interest.
@param value Data value corresponding to the date.
*/
public void setDataValue( DateTime date, double value )
{	setDataValue ( date, value, "", 0 );
}

/**
Set the data value and associated information for the date.  First check to see if the point to be set exists as the
next point relative to the previous set call - this will be fast if values are being reset sequentially (e.g., after
being initialized to missing data and then reset with other values).
If not, utilize a bisection approach to find the point to set.
@param date Date of interest.
@param value Data value corresponding to date.
@param data_flag Data flag for value.
@param duration Duration for value (ignored - assumed to be 1-day or instantaneous depending on data type).
*/
public void setDataValue ( DateTime date, double value, String data_flag, int duration )
{	// Do not define routine here to increase performance.
	boolean	found;
	int	i;
	TSData ptr=null, tsdata=null;
	DateTime dateLocal = new DateTime(date);

	if ( __ts_data_head == null ) {
		// Need to set the head of the list.

		tsdata = new TSData();
		
		tsdata.setValues( dateLocal, value, _data_units, data_flag, duration );

		__ts_data_head = new Vector();

		__ts_data_head.add( tsdata );

		_date1 = new DateTime( tsdata.getDate() );
		_date2 = new DateTime( tsdata.getDate() );
	
		if ( Message.isDebugOn ) {
			Message.printDebug( 30, "IrregularTS.setDataValue", "Initiated the linked list with: \"" + tsdata + "\"." );
		}

		setDataSize ( 1 );
		return;
	}
	
	// First check the previous set call and determine if the point is next on the list
	// This is quite common and so this code greatly improves performance
	
	if ( __prevSetDataPointer != null ) {
	    ptr = __prevSetDataPointer.getNext();
	    if ( ptr != null ) {
    	    if ( ptr.getDate().equals(date) ) {
    	        // Have found the point
    	        ptr.setData ( value );
                ptr.setDataFlag ( data_flag );
                ptr.setDuration ( duration );
                _dirty = true;
                // Save the pointer for the next operation
                __prevSetDataPointer = ptr;
                return;
    	    }
	    }
	} // the previous pointer is set below for other cases

    // Determine if the date is closer to the tail of the list, or the head, using a simple bisection approach.

    double date_double = dateLocal.toDouble();
    double date1_double = _date1.toDouble();
    double date2_double = _date2.toDouble();

	found = false;
	int insert_position = -1;
	int size = __ts_data_head.size();
	if ( size == 0 ) {
		// Need to insert at position 0...
		insert_position = 0;
	}
	else if ( date_double < date1_double ) {
		// Need to insert at the front...
		insert_position = 0;
	}
	else if ( date_double > date2_double ) {
		// Need to insert at the end...
		insert_position = size;
	}
	// Else we need to insert somewhere in the list...
	else if(Math.abs( date_double - date1_double ) < Math.abs( date_double - date2_double ) ){
		// Try to find the position starting from the front of the list...
	
		//if ( Message.isDebugOn ) {
		//	Message.printDebug( 30, "IrregularTS.setDataValue", "Searching vector from start for " + dateLocal);
		//}
	
		for( i=0; i< size; i++ ){
			ptr = __ts_data_head.get(i); 
			
			//if ( Message.isDebugOn ) {
			//	Message.printDebug( 50, "IrregularTS.setDataValue", "Comparing " + dateLocal + " to " + ptr.getDate() );
			//}
			
			if( ptr.getDate().equals( dateLocal ) ){
			
				//if ( Message.isDebugOn ) {
				//	Message.printDebug( 50, "IrregularTS.setDataValue", "Setting " + value + " for " + dateLocal + " at " + i );
				//}
			
				// Set the dirty flag so that we know to recompute the limits if desired...

				_dirty = true;
				found  = true;

				ptr.setData ( value );
				ptr.setDataFlag ( data_flag );
				ptr.setDuration ( duration );
				__prevSetDataPointer = ptr;
				break;
			}
			else if ( ptr.getDate().greaterThan( dateLocal ) ) {
				// Have passed the next-lowest date and need to insert at the current position...
			
				insert_position = i;
				break;
			}
		}
	}
	else {
	    //if ( Message.isDebugOn ) {
		//	Message.printDebug( 20, "IrregularTS.setDataValue", "Searching vector from end for " + dateLocal );
		//}

		for ( i=(size-1); i >= 0; i-- ){
			ptr = __ts_data_head.get(i);

			//if ( Message.isDebugOn ) {
			//	Message.printDebug( 50, "IrregularTS.setDataValue", "Comparing " + dateLocal + " to " + ptr.getDate() );
			//}
		
			if ( ptr.getDate().equals( dateLocal ) ){
				
				//if ( Message.isDebugOn ) {
				//	Message.printDebug( 50, "IrregularTS.setDataValue", "Setting " + value + " for " + dateLocal + " at " + i );
				//}

				// Set the dirty flag so that we know to recompute the limits if desired...

				_dirty = true;
				found  = true;

				ptr.setData( value );
				ptr.setDataFlag( data_flag );
				ptr.setDuration( duration );
				__prevSetDataPointer = ptr;
				break;
			}
			else if ( ptr.getDate().lessThan(dateLocal) ) {
				// Have looped back too far without finding a date so break and add a new point...
				// Need to insert after the current position...

				insert_position = i + 1;
				break;
			}
		}
	}
	if ( found ) {
		//if ( Message.isDebugOn ) {
		//	Message.printDebug( 20, "IrregularTS.setDataValue", "Updated data value at: " + dateLocal + " to " + value + "." );
		//}
		return;
	}

	if ( Message.isDebugOn ) {
		//Message.printDebug( 20, "IrregularTS.setDataValue",
		//"Attempting to add new TSData object to list for " + dateLocal + " : " + value );
	}
	
	// If here, then we didn't find the date, we need to create the new tsdata object.

	tsdata = new TSData();
	tsdata.setValues( dateLocal, value, _data_units, data_flag, duration );

	// If have determined the insert position from above, then do it!...

	if ( insert_position >= 0 ) {
		__ts_data_head.add ( insert_position, tsdata );
		// Set the next/previous pointers (note this is done after the insert so compute positions accordingly...
		if ( insert_position == 0 ) {
			ptr = __ts_data_head.get(1);
			tsdata.setNext ( ptr );
			ptr.setPrevious ( tsdata );
		}
		else if ( insert_position == (__ts_data_head.size() - 1) ) {
			ptr = __ts_data_head.get(insert_position - 1);
			tsdata.setPrevious ( ptr );
			ptr.setNext ( tsdata );
		}
		else {
		    ptr = __ts_data_head.get(insert_position-1);
			tsdata.setPrevious ( ptr );
			ptr.setNext ( tsdata );
			ptr = __ts_data_head.get(insert_position + 1);
			tsdata.setNext ( ptr );
			ptr.setPrevious ( tsdata );
		}
		__prevSetDataPointer = ptr;
		
		//if ( Message.isDebugOn ) {
		//	Message.printDebug( 20, "IrregularTS.setDataValue",
		//	"Inserted data value at: " + dateLocal + " value: " + tsdata + "." );
		//}
	
		//
		// Why the hell was this line here?
		// _date2 	= tsdata.getDate();
	
		// Reset the limits of the data...
		if ( dateLocal.lessThan(_date1) ) {
			_date1 = new DateTime( dateLocal );
		}
		if ( dateLocal.greaterThan(_date2) ) {
			_date2 = new DateTime( dateLocal );
		}

		// Mark dirty so that we recompute the data limits...
		_dirty	= true;

		// Increment the data size...
		setDataSize ( getDataSize() + 1 );
		return;
	}

	// If we get to here, we must have a logic problem!
	Message.printWarning ( 1, "IrregularTS.setDataValue", "Logic problem in routine.  Need to fix!" );
}

}