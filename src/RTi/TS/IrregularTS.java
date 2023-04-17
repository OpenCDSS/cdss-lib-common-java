// IrregularTS - this class stores irregular time step data.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

// TODO smalers 2023-01-17 need to update to get the precision from the irregular interval (e.g., "IrregDay").
/**
This class stores irregular time step data.
Because the time step is irregular,
a date/time must be stored with each recording and the precision of date/times are taken from the start date/time.
The time interval base is TimeInterval.IRREGULAR.
Use the IrregularTSIterator class to get data or retrieve the data array and process as a list.
@see #getData
*/
@SuppressWarnings("serial")
public class IrregularTS extends TS
implements Cloneable, Serializable, Transferable {

/**
The DataFlavor for transferring this specific class.
*/
public static DataFlavor irregularTSFlavor = new DataFlavor(RTi.TS.IrregularTS.class, "RTi.TS.IrregularTS");

/**
List of data points, initially null and will be initialized on first data point set.
*/
private	List<TSData> __tsDataList = null;

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
This is used internally between method calls and should not be assumed to have state between data get calls.
*/
private int __data_index;

/**
Default constructor.  The data array is initialized to null.
*/
public IrregularTS ( ) {
	super();
	init();
}

/**
Copy constructor.  Everything is copied by calling copyHeader() and then copying the data values.
*/
public IrregularTS ( IrregularTS ts ) {
	if ( ts == null ) {
		return;
	}
	copyHeader ( ts );
	// Get the data and loop through the list.
	List<TSData> all_tsdata = ts.getData();
	if ( all_tsdata == null ) {
		// No data for the time series.
		__data_index = ts.__data_index;
		return;
	}
	int nalltsdata = all_tsdata.size();
	//__ts_data_head = new ArrayList ( nalltsdata );
	TSData tsdata = null;
	for ( int i = 0; i < nalltsdata; i++ ) {
		// Would be nice to do this but the next/prev seem to get fouled up.
		//__ts_data_head.addElement (new TSData((TSData)tsdata.elementAt(i)) );
		tsdata = all_tsdata.get(i);
		if ( tsdata != null ) {
			// This is what actually causes the copy.
			setDataValue ( tsdata.getDate(), tsdata.getDataValue(), tsdata.getDataFlag(), tsdata.getDuration() );
		}
	}
	__data_index = ts.__data_index;
	addToGenesis ( "Copied from \"" + ts.getIdentifierString() + "\"" );
}

/**
Allocate the data flag space for the time series.
This is a place-holder and is not used since data
space is adjusted as necessary as data are added with setDataValue().
@param data_flag_length Maximum length of data flags.
If the data flags array is already allocated, then the flag size will be increased by the specified length.
This allows multiple flags to be concatenated.
@param initial_value Initial value (null is allowed and will result in the flags being initialized to spaces).
@param retain_previous_values If true, the array size will be increased if necessary, but
previous data values will be retained.  If false, the array will be reallocated and initialized to spaces.
@exception Exception if there is an error allocating the memory.
*/
public void allocateDataFlagSpace (	String initial_value, boolean retain_previous_values )
throws Exception {
	return;
}

/**
Allocate the data space.
This is a place-holder and is not used since data space is adjusted as necessary as data are added with setDataValue().
*/
public int allocateDataSpace() {
	return 0;
}

/**
Calculate the number of data points between two dates.
<b>This method is not valid for IrregularTS but is maintained for compatibility.  Use the non-static calculateDataSize() method.</b>
@return A count of the data recordings between the given dates (always zero here).
@param start_date First date of interest.
@param end_date Last date of interest.
@param interval_mult Data interval multiplier (not used).
*/
public static int calculateDataSize ( DateTime start_date, DateTime end_date, int interval_mult ) {
	String routine = IrregularTS.class.getSimpleName() + ".calculateDataSize";
	Message.printWarning ( 1, routine,
	"Must have an instance of IrregularTS to compute the data size.  Use the non-static calculateDataSize() method" );
	return 0;
}

/**
Calculate the number of data points between two dates.
@return A count of the data recordings between the given dates.
@param start_date First date of interest.
@param end_date Last date of interest.
*/
public int calculateDataSize (	DateTime start_date, DateTime end_date ) {
	int	datasize = 0;

	if ( __tsDataList == null ) {
		return 0;
	}

	// Loop through the data and count the intervals in the given period.

	DateTime date = new DateTime (DateTime.DATE_STRICT);
	TSData ptr = null;
	int	size = __tsDataList.size();
	for ( int i = 0; i < size; i++ ) {
		ptr = __tsDataList.get(i);
		date = ptr.getDate();

		if ( date.lessThan(start_date) ) {
			// Still looking for data.
			continue;
		}
		else if ( date.greaterThan(end_date) ) {
			// No need to continue processing.
			break;
		}
		// Add to the counter.
		++datasize;
	}
	return datasize;
}

/**
 * Change the period of record.
 * Because irregular time series don't need to fill the period with a data array this method does nothing.
 */
public void changePeriodOfRecord ( DateTime date1, DateTime date2 )
throws TSException {
}

/**
Clone the object.
The TS base class clone() method is called and then the the data array is cloned.
The result is a complete deep copy.
*/
public Object clone() {
	IrregularTS ts = (IrregularTS)super.clone();	// Clone base class data.

	ts.copyHeader ( this );
	// Get the data and loop through the list.
	List<TSData> all_tsdata = getData();
	if ( all_tsdata == null ) {
		// No data for the time series.
		ts.__data_index = __data_index;
		return ts;
	}
	int nalltsdata = all_tsdata.size();
	TSData tsdata = null;
	// Initialize a new data array so that the clone is a deep copy.
	ts.__tsDataList = null; // Will trigger initialization on first setDataValue call.
	ts.setDataSize ( 0 );
	ts.__prevSetDataPointer = null;
	ts.__prevRemoveDataPointerNext = null;
	ts.__data_index = -1;

	for ( int i = 0; i < nalltsdata; i++ ) {
		tsdata = all_tsdata.get(i);
		if ( tsdata != null ) {
			// Set the data value in the copy.
			ts.setDataValue ( tsdata.getDate(), tsdata.getDataValue(), tsdata.getDataFlag(), tsdata.getDuration() );
		}
	}
	// TODO smalers 2012-03-23 Need to evaluate use of the index.
	//ts.__data_index = __data_index;
	addToGenesis ( "Cloned from \"" + ts.getIdentifierString() + "\"" );
	return ts;
}

/**
Insert missing data points where a gap between known points is more than the specified interval.
This is used to more accurately represent the irregular data
(e.g., if an IrregularTS is used to record values that actually are
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
public int enforceDataGaps ( int interval_base, int interval_mult, DateTime date1, DateTime date2) {
	String	routine = getClass().getSimpleName() + ".enforceDataGaps";

	// Loop through the data using the given dates.
	// If the dates are not specified, use the time series start and end dates.
	// Do this by filling out the TS.getValidPeriod function.

	int	dl = 20;
	double	data_value = 0.0;
	TSLimits dates = TSUtil.getValidPeriod ( this, date1, date2 );
	DateTime start = new DateTime ( dates.getNonMissingDataDate1() );
	DateTime end = new DateTime ( dates.getNonMissingDataDate2() );
	DateTime t = new DateTime(), prevDate = new DateTime();

	// Now loop through the period.
	// If between any known data values the time gap is longer than the interval indicated above,
	// insert ONE missing data value in the gap at some date in the middle of the gap.

	prevDate = start;
	TSDateIterator tsdi;
	int missingcount = 0, datacount = 0;
	for ( tsdi = new TSDateIterator( this, start, end );
		!tsdi.isIterationComplete(); tsdi.advanceDate(), ++datacount ){

		t = tsdi.getCurrentDate();
		data_value = getDataValue(t);

		if ( Message.isDebugOn ) {
			Message.printDebug ( 2, routine, "Processing date " + t + " in period " + start + " to " + end );
		}

		if( !t.equals(start) ) {
			prevDate.addInterval( interval_base, interval_mult );
			if( prevDate.lessThan(t) && !isDataMissing(data_value)){
				// Have a gap that is not bounded by missing
				// data already so insert a missing data point.
				setDataValue( prevDate, _missing );
				++missingcount;
				if( Message.isDebugOn ) {
					Message.printDebug(5, routine, " Set MISSING value at: " + prevDate);
				}

			prevDate = t;
		}

	}
	tsdi = null;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Set " + missingcount + " missing values after checking " + datacount + " values" );
	}
	return 0;
}
*/

/**
 * Indicate whether the data interval uses time.
 * For irregular time series, this depends on the irregular interval precision.
 * @return true if the interval precision uses time
 */
public boolean dataIntervalUsesTime () {
	if ( this.getIdentifier().getIrregularIntervalPrecision() <= TimeInterval.HOUR ) {
		return true;
	}
	else {
		return false;
	}
}

/**
 * Find nearest data point to the given DateTime.
 * It is assumed that the precision of the DateTime is consistent with the precision of DateTime in the time series.
 * @param dt DateTime that is is of interest to match
 * @param searchStart a DateTime known to exist in the time series, to start the search
 * @param searchEnd a DateTime known to exist in the time series, to end the search
 * @param returnMatch if true and the requested DateTime matches an existing value,
 * return the data for that point (false will return the next point).
 * @return a TSData objects containing the nearest future data point to the requested,
 * or null if not found (goes beyond end of data)
 */
public TSData findNearestNext ( DateTime dt, DateTime searchStart, DateTime searchEnd, boolean returnMatch ) {
	TSData tsdataNext = null;
	TSData tsdataLeft, tsdataRight;
	// Currently search the entire time series and use bisection.
	if ( searchStart != null ) {
		// Verify that it exists in the data.
		tsdataLeft = this.getDataPoint(searchStart, null);
		if ( tsdataLeft == null ) {
			return null;
		}
	}
	else {
		// Search from the start.
		searchStart = new DateTime(getDate1());
		tsdataLeft = this.getDataPoint(searchStart, null);
	}
	if ( searchEnd != null ) {
		// Verify that it exists in the data.
		tsdataRight = this.getDataPoint(searchEnd, null);
		if ( tsdataRight == null ) {
			return null;
		}
	}
	else {
		// Search from the end.
		searchEnd = new DateTime(getDate2());
		tsdataRight = this.getDataPoint(searchEnd, null);
	}
	// See if the request is outside of the bounds of the search.
	if ( dt.lessThan(searchStart) ) {
		return null;
	}
	if ( dt.greaterThan(searchEnd) ) {
		return null;
	}
	int iLeft = 0;
	int iRight = this.__tsDataList.size() - 1;
	int iMiddle;
	TSData tsdataMiddle;
	DateTime dtMiddle;
	while ( true ) {
		// Have left and right so bisect in the middle and evaluate.
		iMiddle = (iLeft + iRight)/2;
		tsdataMiddle = this.__tsDataList.get(iMiddle);
		dtMiddle = tsdataMiddle.getDate();
		// TODO sam 2017-03-05 could make the following more efficient.
		if ( dt.equals(dtMiddle) && returnMatch ) {
			// Found an exact match for the requested DateTime.
			tsdataNext = new TSData(tsdataMiddle);
			break;
		}
		if ( dt.greaterThan(dtMiddle) ) {
			// Reset the left.
			iLeft = iMiddle;
		}
		else {
			// Reset the right.
			iRight = iMiddle;
		}
		if ( (iRight - iLeft) == 1 ) {
			// Converged, value on right should be returned.
			tsdataNext = new TSData(__tsDataList.get(iRight));
			break;
		}
	}
	return tsdataNext;
}

/**
Format the time series for output.
This is not meant to be used for time series conversion but to produce a general summary of the output.
At this time, irregular time series are always output in a sequential summary format.
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
of data or "Spreadsheet" for a delimited file suitable for a spreadsheet (see the "Delimiter" property).
At this time, the determination of whether total or average annual values are shown is made based on units.
AF, ACFT, IN, INCH, FEET, and FOOT are treated as totals and all others as averages.
A more rigorous handling is being implemented to use the units dimension to determine totals, etc.</td>
<td>Summary</td>
</tr>

<tr>
<td><b>Precision</b></td>
<td>The precision of numbers as printed.  All data values are printed in a 9-digit column.
The precision controls how many digits are shown after the decimal.
If not specified, an attempt will be made to use the units to look up the precision.
If that fails, a default of 1 will be used.
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
<td>Print the comments associated with the time series.
This may contain information about the quality of data, station information, etc.
This information is usually variable-length text, and may not be available.
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintAllStats</b></td>
<td>Print all the statistics (currently maximum, minimum, and mean,
although standard deviation and others are being added).
Because statistics are being added to output,
it is advised that if formatting is to remain the same over time, that output items be individually specified.
One way of doing this is to turn all the statistics off and then turn specific items on (to true).
</td>
<td>false</td>
</tr>

<tr>
<td><b>PrintGenesis</b></td>
<td>Print the time series genesis information after the header in a format as follows:
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
<td>Print notes about the output.
This consists of helpful information used to understand the output (but does not consist of data).  For example:
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
<td>Use the time series comments for the header and do not print other header information.
This can be used when the entire header is formatted elsewhere.
</td>
<td>false</td>
</tr>

</table>
@exception RTi.TS.TSException Throws if there is a problem formatting the output.
*/
public List<String> formatOutput( PropList proplist )
throws TSException {
	String message = "";
	String routine = getClass().getSimpleName() + ".formatOutput";
	int dl = 20;
	List<String> strings = new ArrayList<>();
	PropList props = null;
	String format = "", prop_value = null;
	String data_format = "%9.1f";

	// If the property list is null, allocate one here so don't have to constantly check for null.

	if ( proplist == null ) {
		// Create a PropList so don't have to check for nulls all the time.
		props = new PropList ( "formatOutput" );
	}
	else {
	    props = proplist;
	}

	// Get the important formatting information from the property list.

	// Get the overall format.

	prop_value = props.getValue ( "Format" );
	if ( prop_value == null ) {
		// Default to "Summary".
		format = "Summary";
	}
	else {
	    // Set to requested format.
		format = prop_value;
	}

	// Determine the units to output.  For now use what is in the time series.

	String req_units = _data_units;

	// Get the precision.

	prop_value = props.getValue ( "Precision" );
	if ( prop_value == null ) {
		// Try to get units information for default.
		try {
		    DataUnits u = DataUnits.lookupUnits ( req_units );
			data_format = "%9." + u.getOutputPrecision() + "f";
		}
		catch ( Exception e ) {
			// Default.
			data_format = "%9.1f";
		}
	}
	else {
	    // Set to requested precision.
		data_format = "%9." + prop_value + "f";
	}

	// Determine the period to output.  For now always output the total.

	if ( (_date1 == null) || (_date2 == null) ) {
		message = "Null period dates for time series.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	DateTime start_date = new DateTime (_date1);
	DateTime end_date = new DateTime (_date2);

	// Now generate the output based on the format.

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Creating output in format \"" + format + "\"." );
	}
	if ( format.equalsIgnoreCase("Spreadsheet") ) {
		// Spreadsheet.
		prop_value = props.getValue ( "Delimiter" );
		if ( prop_value == null ) {
			// Default to "|".
			format = "|";
		}
		else {
		    // Set to requested delimiter.
			format = prop_value;
		}
		Message.printWarning ( 1, routine, "Spreadsheet output format is not implemented." );
		return strings;
	}
	else if ( format.equalsIgnoreCase("Summary") ) {
		// Print the header for the matrix.

		prop_value = props.getValue ( "PrintHeader" );
		String print_header = null;
		if ( prop_value == null ) {
			// Default is true.
			print_header = "true";
		}
		else {
		    print_header = prop_value;
		}
		prop_value = props.getValue ( "UseCommentsForHeader" );
		String use_comments_for_header = null;
		if ( prop_value == null ) {
			// Default is false.
			use_comments_for_header = "false";
		}
		else {
		    use_comments_for_header = prop_value;
		}
		if ( print_header.equalsIgnoreCase("true") ) {
			if ( !use_comments_for_header.equalsIgnoreCase("true")){
				// Format the header.
				strings.add ( "" );
				List<String> strings2 = formatHeader();
				StringUtil.addListToStringList ( strings, strings2 );
			}
		}

		// Add comments if available.

		prop_value = props.getValue ( "PrintComments" );
		String print_comments = null;
		if ( prop_value == null ) {
			// Default is true.
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

		// Print the genesis information.

		prop_value = props.getValue ( "PrintGenesis" );
		String print_genesis = null;
		if ( prop_value == null ) {
			// Default is true.
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

		// Print the body of the summary.

		// Need to check the data type to determine if it is an average or a total.
		// For now, make some guesses based on the units.

		strings.add ( "" );

		if ( __tsDataList == null ) {
			// No data for the time series.
			strings.add ( "No data available." );
			return strings;
		}

		strings.add ( "Date                          Value" );
		strings.add ( "--------------------------------------------" );

		// Now loop through the time series and transfer to the proper location in the matrix.
		double data_value;
		int nalltsdata = __tsDataList.size();
		TSData tsdata = null;
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = __tsDataList.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end_date) ) {
				// Past the end of where want to go so quit.
				break;
			}
			if ( date.greaterThanOrEqualTo(start_date) ) {
				data_value = tsdata.getDataValue();
				// Format the date according to the active date precision but allow room for full date, to line up with headers.
				strings.add ( StringUtil.formatString(date.toString(),
				"%-26.26s") + "  " + StringUtil.formatString(data_value,data_format) );
			}
		}
		strings.add ( "--------------------------------------------" );
		// Now need to do the statistics.  Loop through each column.
		// First check to see if all statistics should be printed (can be dangerous if add new statistics).
		// NOT ENABLED IN IRREGULAR TIME SERIES.
	}
	else {
	    message = "Unrecognized format: \"" + format + "\".";
		Message.printWarning ( 1, routine, message );
		throw new TSException ( message );
	}

	// Print footnotes to the output.
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
throws TSException {
	List<String> formatted_output = null;
	String routine = getClass().getSimpleName() + ".formatOutput(Writer,props)";
	int	dl = 20;
	String message;

	if ( fp == null) {
		message = "Null PrintWriter for output.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// First get the formatted output.

	try {
	    formatted_output = formatOutput ( props );
		if ( formatted_output != null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Formatted output is " + formatted_output.size() + " lines." );
			}

			// Now write each string to the writer.

			String newline = System.getProperty ( "line.separator");
			int size = formatted_output.size();
			for ( int i = 0; i < size; i++ ) {
				fp.print ( formatted_output.get(i) + newline );
			}
			newline = null;
		}
	}
	catch ( TSException e ) {
		// Rethrow.
		throw e;
	}

	// Also return the list.

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
throws TSException {
	String message = null;
	List<String> formatted_output = null;
	PrintWriter	stream = null;

	// First open the output file.

	try {
	    stream = new PrintWriter ( new FileWriter(fname) );
	}
	catch ( Exception e ) {
		message = "Unable to open file \"" + fname + "\".";
		throw new TSException ( message );
	}

	try {
	    formatted_output = formatOutput ( stream, props );
	}
	catch ( TSException e ) {
		// Rethrow.
		throw e;
	}
	finally {
		if ( stream != null ) {
			stream.close();
		}
	}

	// Also return the list (consistent with C++ single return type).

	return formatted_output;
}

/**
Return the data list.
@return The reference to the data array.  Use caution when manipulating.
*/
public List<TSData> getData() {
	return __tsDataList;
}

/**
Return the data list list.
@return the data list size.
*/
public int getDataSize() {
	if ( this.__tsDataList == null ) {
		return 0;
	}
	else {
		return this.__tsDataList.size();
	}
}

/**
Return a data point for a date.
@return A TSData for the date of interest.
@param date Date of interest.
*/
public TSData getDataPoint ( DateTime date, TSData data_point ) {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".getDataPoint";
	}
	int	i = 0;
	TSData ptr = null;

	if ( data_point == null ) {
	    data_point = new TSData();
	}
	data_point.setDate ( date );
	data_point.setUnits ( _data_units );
	data_point.setDataFlag ( "" ); // Default.
	data_point.setDuration ( 0 ); // Default.

	// Check the date coming in.

	if ( __tsDataList == null ) {
		// No data!
		// Leave __data_index as is.
		data_point.setDataValue( _missing );
		return data_point;
	}

	if ( date.lessThan(_date1) || date.greaterThan(_date2) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 30, routine, date + " not within POR (" + _date1 + " - " + _date2 + ")" );
			// Leave __data_index as is.
			data_point.setDataValue( _missing );
			return data_point;
		}
	}

	// First check to see if the requested date is the next in line from the previous access.
	// Do so by getting the data point from using __data_index and then check
	// its date to see if it matches the requested date.
	// At this point, if they do not match, just go to the next search techniques.
	// It is likely that the first call will not
	// use the following method because a starting value needs to be found.

	TSData next_data = getNextElement ();
	if ( next_data != null ) {
		// Do this code.  Otherwise, cascade to the next search techniques.
		DateTime next_date = next_data.getDate();
		if ( next_date.equals(date) ) {
			// Have a match.  Increment the date index so that can use it next time and return the matching TSData.
			++__data_index;
			return next_data;
		}
	}

	// Determine whether the date is closer to the tail of the list, or the head.

	double date_double = date.toDouble();
	double date1_double = _date1.toDouble();
	double date2_double = _date2.toDouble();

	int size = __tsDataList.size();
	int found_index = -1;
	if ( Math.abs( date_double - date1_double ) < Math.abs( date_double - date2_double ) ){
		for ( i=0; i < size; i++ ) {
			ptr = __tsDataList.get(i);
			if( ptr.getDate().equals( date ) ){
				found_index = i;
				break;
			}
		}
	}
	else {
	    for ( i=(size-1); i >= 0; i-- ) {
			ptr = __tsDataList.get(i);
			if( ptr.getDate().equals( date ) ) {
				found_index = i;
				break;
			}
		}
	}

	if( ptr == null ) {
		String routine2 = getClass().getSimpleName() + ".getDataPoint";
		Message.printWarning( 2, routine2, "Cannot find data value in inernal time series." );
		// Leave __data_index as is.
		data_point.setDataValue( _missing );
		return data_point;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug( 30, routine, ptr.getDataValue() + " for " + date + " from _data[" + i + "]." );
	}

	// Set the data index to the found point and then return the data value.

	if ( found_index >= 0 ) {
		__data_index = found_index;
	}
	else {
	    data_point.setDataValue ( _missing );
		return data_point;
	}

	// Return the data point.

	data_point.setDataValue ( ptr.getDataValue() );
	data_point.setDataFlag ( ptr.getDataFlag() );
	data_point.setDuration( ptr.getDuration() );

	return data_point;
}

/**
Return the data value for a date.
This routine uses some intelligence to try to find the data value as quickly as possible.
This routine is most often called after the time series as been
filled by setDataValue() calls, and it is usually called in a loop where data
are accessed one after the other in a sequence.
Therefore, the fastest retrieval is usually attained by incrementing one point from the previously accessed point.
This method is checked first and then checks to search from the front or back are made.
At this time a bisection algorithm is not employed
but might need to be in the future to increase performance.
@return The data value in the data array given a date, or the missing data value if the date cannot be found in the data.
@param date Date of interest.
*/
public double getDataValue( DateTime date ) {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".getDataValue";
	}

	// Do not define routine here to increase performance.
	int	dl = 30, found_index = -1, i = 0;
	TSData ptr=null;

	// Check the date coming in.

	if ( __tsDataList == null ) {
		// No data!  Leave __data_index as is.
		return _missing;
	}

	if ( date.lessThan(_date1) || date.greaterThan(_date2) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, routine, date + " not within POR (" + _date1 + " - " + _date2 + ")." );
		}
		// Leave __data_index as is.
		return _missing;
	}

	// First check to see if the requested date is the next in line from the previous access.
	// Do so by getting the data point from using
	// __data_index and then check its date to see if it matches the requested date.
	// At this point, if they do not match, just go to the next search techniques.
	// It is likely that the first call will not use the following method because a starting value needs to be found.

	TSData next_data = getNextElement ();
	if ( next_data != null ) {
		// Do this code.  Otherwise, cascade to the next search techniques.
		DateTime next_date = next_data.getDate();
		if ( next_date.equals(date) ) {
			// Have a match!
			// Increment the date index so that can use it next time and return the matching TSData.
			++__data_index;
			return next_data.getDataValue();
		}
	}

	// Determine whether the date is closer to the tail of the list, or the head.

	double date_double = date.toDouble();
	double date1_double = _date1.toDouble();
	double date2_double = _date2.toDouble();

	int size = __tsDataList.size();
	if ( Math.abs( date_double - date1_double ) < Math.abs( date_double - date2_double ) ){
	    // Closer to the front of the list.
		for ( i=0; i < size; i++ ) {
			ptr = __tsDataList.get(i);
			if( ptr.getDate().equals( date ) ){
				found_index = i;
				break;
			}
		}
	}
	else {
	    // Closer to the end of the list.
	    for ( i=(size-1); i >= 0; i-- ){
			ptr = __tsDataList.get(i);
			if( ptr.getDate().equals( date ) ){
				found_index = i;
				break;
			}
		}
	}

	if( ptr == null ){
		String routine2 = getClass().getSimpleName() + ".getDataValue";
		Message.printWarning( 2, routine2, "Unable to find data value in time series." );
		// Leave __data_index as is.
		return _missing;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug( dl, routine, ptr.getDataValue() + " for " + date + " from _data[" + i + "]." );
	}

	if ( found_index < 0 ) {
		// Did not find the data.
		Message.printDebug ( dl, routine, "Can't find data matching date " + date );
		return _missing;
	}

	// Set the data index to the found point and then return the data value.
	__data_index = found_index;
	return ptr.getDataValue();
}

/**
Return the first date in the data.
This is taken from the first data point if data are available, or the base class method,
if data are not available (e.g., in cases when the time series header is defined without data).
*/
public DateTime getDate1() {
	if ( (__tsDataList == null) || (__tsDataList.size() == 0) ) {
		return super.getDate1();
	}
	else {
	    return __tsDataList.get(0).getDate();
	}
}

/**
Return the last date in the data.
This is taken from the last data point if data are available, or the base class method,
if data are not available (e.g., in cases when the time series header is defined without data).
*/
public DateTime getDate2() {
	if ( (__tsDataList == null) || (__tsDataList.size() == 0) ) {
		return super.getDate2();
	}
	else {
	    return __tsDataList.get(__tsDataList.size() - 1).getDate();
	}
}

/**
Get the next element in the data list.
This method can be used to return the element after the previously accessed element.
For example, when searching the data array, this routine can be used to get the next data value.
The routine returns null if at the end of the data array.
@return The next data point after the previous access.
*/
public TSData getNextElement() {
	// Do not define routine here to increase performance.
	TSData tsdata=null;

	if ( __tsDataList == null ) {
		return null;
	}

	int	size = __tsDataList.size();

	// Will get the element after "__data_index" so do the appropriate checks.
	// First make sure that will not exceed the bounds of the data list.

	if ( ((__data_index + 1) >= size) || ((__data_index + 1) < 0) ) {
		if ( Message.isDebugOn ) {
			String routine = getClass().getSimpleName() + ".getNextElement";
			Message.printDebug( 20, routine, "Data index " + __data_index + " out of range, returning NULL.");
		}
		return null;
	}

	// Now return the next data value as a copy.

	tsdata = __tsDataList.get( __data_index + 1 );

	return (TSData)tsdata.clone();
}

// TODO smalers 2023-04-16 need to move UI code out of this data class.
/**
Returns the data in the specified DataFlavor, or null if no matching flavor exists.
From the Transferable interface.  Supported dataflavors are:<br>
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

// TODO smalers 2023-04-16 need to move UI code out of this data class.
/**
Returns the flavors in which data can be transferred.
From the Transferable interface.  The order of the dataflavors that are returned are:<br>
<ul>
<li>IrregularTS - IrregularTS.class / RTi.TS.IrregularTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@return the flavors in which data can be transferred.
*/
public DataFlavor[] getTransferDataFlavors() {
	DataFlavor[] flavors = new DataFlavor[3];
	flavors[0] = irregularTSFlavor;
	flavors[1] = TS.tsFlavor;
	flavors[2] = TSIdent.tsIdentFlavor;
	return flavors;
}

/**
Indicate whether the time series has data,
determined by checking to see whether the data space has &gt; 0 data points.
This method can be called after a time series has been read - even if no data are available,
the header information may be complete.
The alternative of returning a null time series from a read method if no data are available
results in the header information being unavailable.
Instead, return a TS with only the header information and call hasData() to check to see if the data space has been assigned.
@return true if data are available (the data space has been allocated).
Note that true will be returned even if all the data values are set to the missing data value.
*/
public boolean hasData () {
	if ( (__tsDataList != null) && (__tsDataList.size() > 0) ) {
		return true;
	}
	else {
	    return false;
	}
}

/**
Indicate whether the irregular time series has data flags.
Because some code may edit the list of TSData directly, it is difficult to intercept calls to
setDataValue(...flag...) so assume all irregular data has data flags.
This should not be a major issue because blank flags will be ignored in most code and interned strings are memory-efficient.
@return true if data flags are used, false if not.
*/
public boolean hasDataFlags () {
    if ( !hasData() ) {
        return false;
    }
    else {
        return true;
    }
}

/**
Initialize data.
*/
private void init( ) {
	_data_interval_base = TimeInterval.IRREGULAR;
	_data_interval_mult = 1;
	_data_interval_base_original = TimeInterval.IRREGULAR;
	_data_interval_mult_original = 1;
	__tsDataList = null;
	__data_index = -1;
}

// TODO smalers 2023-04-16 need to move UI code out of this data class.
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
 * Indicate whether an irregular interval time series (always true).
 * @return true always
 */
@Override
public boolean isIrregularInterval () {
	return true;
}

/**
 * Indicate whether a regular interval time series (always false).
 * @return false always
 */
@Override
public boolean isRegularInterval () {
	return false;
}

/**
Return an iterator for the time series using the full period for the time series.
@return an iterator for the time series.
@exception Exception if the time series dates are null.
*/
public TSIterator iterator ()
throws Exception {
	return new IrregularTSIterator ( this );
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
throws Exception {
	return new IrregularTSIterator ( this, date1, date2 );
}

/**
Refresh the derived data in the time series (e.g., recompute limits if data has been set).
This is typically only called from other package routines.
*/
public void refresh () {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".refresh";
	}

	// If the data is not dirty, then do not have to refresh the other information.

	if ( !_dirty ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 30, routine, "Time series is not dirty.  Not recomputing limits." );
		}
		return;
	}

	// Else need to refresh.

	if ( Message.isDebugOn ) {
		Message.printDebug( 30, routine, "Time Series is dirty. Recomputing limits." );
	}

	TSLimits limits = TSUtil.getDataLimits ( this, _date1, _date2, false );
	if ( limits.areLimitsFound() ) {
		// Now reset the limits for the time series.
		setDataLimits ( limits );
	}

	_dirty = false;
}

/**
Remove a data point corresponding to the index of the data array.
This is used in internal code.
@param index index in the data array (0+)
@return true if the point was removed, false if not (index was not found)
*/
public boolean removeDataPoint ( int index ) {
	int size = __tsDataList.size();
	if ( index < size ) {
		// Index is in the array space.
        __tsDataList.remove(index);
        // Mark dirty so that recompute the data limits.
        _dirty  = true;
        // Decrement the data size.
        setDataSize ( getDataSize() - 1 );
        return true;
	}
	else {
		return false;
	}
}

/**
Remove a data point corresponding to the date.
@param date date/time for which to remove the data point.
@return true if the point was removed, false if not (date was not found).
*/
public boolean removeDataPoint ( DateTime date ) {
    if ( date == null ) {
        return false;
    }
    int size = getDataSize();
    if ( size == 0 ) {
        // No action.
        return false;
    }
    else if ( size == 1 ) {
        // Remove the head.
        __tsDataList = null;
        __prevSetDataPointer = null;
        setDataSize(0);
        _dirty  = true;
        return true;
    }
    // If here, need to search through the list and find the point.
    boolean pointFound = false;
    // First check the previous set call and determine if the point is next on the list.
    // This is quite common and so this code greatly improves performance.
    TSData ptr = null;
    if ( __prevRemoveDataPointerNext != null ) {
        ptr = __prevRemoveDataPointerNext;
        if ( ptr != null ) {
            if ( ptr.getDate().equals(date) ) {
                // Have found the point.
                pointFound = true;
            }
        }
    }
    if ( !pointFound ) {
        // Do a more exhaustive loop.
        // FIXME SAM 2010-08-17 Need to optimize search for the date, relinking list, etc.
        for ( TSData ptr2 : __tsDataList ) {

            //if ( Message.isDebugOn ) {
            //  Message.printDebug( 50, routine, "Comparing " + dateLocal + " to " + ptr.getDate() );
            //}

            if( ptr2.getDate().equals( date ) ) {
                // Found match.
                ptr = ptr2;
                pointFound = true;
            }
        }
    }
    if ( pointFound ) {
        // Do the removal - reroute pointers and then remove point from the list.
        __prevRemoveDataPointerNext = ptr.getNext(); // Save for next call.
        // There may be cases at the start or end of the time series where nulls could be encountered
        // so be careful about the reset.
        TSData ptrPrev = ptr.getPrevious();
        TSData ptrNext = ptr.getNext();
        if ( ptrPrev != null ) {
            ptrPrev.setNext(ptrNext);
        }
        if ( ptrNext != null ) {
            ptrNext.setPrevious(ptrPrev);
        }
        __tsDataList.remove(ptr);
        // Mark dirty so that recompute the data limits.
        _dirty  = true;
        // Decrement the data size.
        setDataSize ( getDataSize() - 1 );
        return true;
    }
    return false;
}

/**
Set the data value for the given date.
If the date has not already been set with a value, add a data point in the proper order.
This calls the overloaded method with a "" flag and duration of 0.
@param date Date of interest.
@param value Data value corresponding to the date.
@param return the number of values set, 0 or 1, useful to know when a value is outside the allocated period
*/
public int setDataValue( DateTime date, double value ) {
	return setDataValue ( date, value, "", 0 );
}

/**
Set the data value and associated information for the date.
First check to see if the point to be set exists as the next point relative to the previous set call.
This will be fast if values are being reset sequentially
(e.g., after being initialized to missing data and then reset with other values).
If not, utilize a bisection approach to find the point to set.
@param date Date of interest.
@param value Data value corresponding to date.
@param data_flag Data flag for value.
@param duration Duration for value
@param return the number of values set, 0 or 1, useful to know when a value is outside the allocated period
*/
public int setDataValue ( DateTime date, double value, String data_flag, int duration ) {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".setDataValue";
	}

	// Do not define routine here to increase performance.
	boolean	found;
	int	i;
	TSData ptr=null, tsdata=null;
	DateTime dateLocal = new DateTime(date);

	if ( __tsDataList == null ) {
		// Need to set the head of the list.

		tsdata = new TSData();

		tsdata.setValues( dateLocal, value, _data_units, data_flag, duration );

		__tsDataList = new ArrayList<>();

		__tsDataList.add( tsdata );

		_date1 = new DateTime( tsdata.getDate() );
		_date2 = new DateTime( tsdata.getDate() );

		//Message.printStatus( 2, "IrregularTS.setDataValue", "Initialized the linked list with: \"" + tsdata + "\"." );
		if ( Message.isDebugOn ) {
			Message.printDebug( 30, routine, "Initialized the linked list with: \"" + tsdata + "\"." );
		}

		setDataSize ( __tsDataList.size() );
		__prevSetDataPointer = tsdata;
		return 1;
	}

	// First check the previous set call and determine if the point is next on the list.
	// This is quite common and so this code greatly improves performance.

	if ( __prevSetDataPointer != null ) {
	    ptr = __prevSetDataPointer.getNext();
	    if ( ptr != null ) {
    	    if ( ptr.getDate().equals(date) ) {
    	        // Have found the point.
    	        //Message.printStatus(2,routine,"Setting data value next to the previous value at " + dateLocal );
    	        ptr.setDataValue ( value );
                ptr.setDataFlag ( data_flag );
                ptr.setDuration ( duration );
                _dirty = true;
                // Save the pointer for the next operation.
                __prevSetDataPointer = ptr;
                return 1;
    	    }
	    }
	} // The previous pointer is set below for other cases.

    // Determine if the date is closer to the tail of the list, or the head, using a simple bisection approach.
	// It is possible (likely?) that the first and last dates have been set but the check needs to use the first and
	// last dates in the data.

    double date_double = dateLocal.toDouble();
    double date1_double = 0.0;
    double date2_double = 0.0;
    if ( (__tsDataList != null) && (__tsDataList.size() > 0) ) {
        date1_double = __tsDataList.get(0).getDate().toDouble();
        date2_double = __tsDataList.get(__tsDataList.size() - 1).getDate().toDouble();
    }
    //Message.printStatus(2, routine, "date_double=" + date_double + ", date1Double=" + date1_double +
    //    ", date2Double=" + date2_double );

	found = false;
	int insert_position = -1;
	int size = __tsDataList.size();
	if ( size == 0 ) {
		// Need to insert at position 0.
	    //Message.printStatus(2,routine,"Size = 0, Setting 1st data value at the head of the list for " + dateLocal );
		insert_position = 0;
	}
	else if ( date_double < date1_double ) {
		// Need to insert at the front.
	    //Message.printStatus(2,routine,"" + date_double + " < " + date1_double +
	    //    ", Setting data value at the start of the list for " + dateLocal );
		insert_position = 0;
	}
	else if ( date_double > date2_double ) {
		// Need to insert at the end.
	    //Message.printStatus(2,routine,"" + date_double + " > " + date2_double +
	    //    ", setting data value at the end of the list for " + dateLocal );
		insert_position = size;
	}
	// Else need to insert somewhere in the list.
	else if(Math.abs( date_double - date1_double ) < Math.abs( date_double - date2_double ) ){
	    //Message.printStatus(2,routine,"Closer to front of list for " + dateLocal );
		// Try to find the position starting from the front of the list.

		//if ( Message.isDebugOn ) {
		//	Message.printDebug( 30, routine, "Searching list from start for " + dateLocal);
		//}

		for( i=0; i< size; i++ ){
			ptr = __tsDataList.get(i);

			//if ( Message.isDebugOn ) {
			//	Message.printDebug( 50, routine, "Comparing " + dateLocal + " to " + ptr.getDate() );
			//}

			if( ptr.getDate().equals( dateLocal ) ){

				//if ( Message.isDebugOn ) {
				//	Message.printDebug( 50, routine, "Setting " + value + " for " + dateLocal + " at " + i );
				//}

				// Set the dirty flag so that know to recompute the limits if desired.

				_dirty = true;
				found = true;

				ptr.setDataValue ( value );
				ptr.setDataFlag ( data_flag );
				ptr.setDuration ( duration );
				__prevSetDataPointer = ptr;
				break;
			}
			else if ( ptr.getDate().greaterThan( dateLocal ) ) {
				// Have passed the next-lowest date and need to insert at the current position.

				insert_position = i;
				break;
			}
		}
	}
	else {
	    //Message.printStatus(2,"","Closer to the end of the list for " + dateLocal );
	    //if ( Message.isDebugOn ) {
		//	Message.printDebug( 20, routine, "Searching list from end for " + dateLocal );
		//}

		for ( i=(size-1); i >= 0; i-- ){
			ptr = __tsDataList.get(i);

			//if ( Message.isDebugOn ) {
			//	Message.printDebug( 50, routine, "Comparing " + dateLocal + " to " + ptr.getDate() );
			//}

			if ( ptr.getDate().equals( dateLocal ) ){

				//if ( Message.isDebugOn ) {
				//	Message.printDebug( 50, routine, "Setting " + value + " for " + dateLocal + " at " + i );
				//}

				// Set the dirty flag so that know to recompute the limits if desired.

				_dirty = true;
				found = true; // Indicates below that existing data point was found.

				ptr.setDataValue( value );
				ptr.setDataFlag( data_flag );
				ptr.setDuration( duration );
				__prevSetDataPointer = ptr;
				break;
			}
			else if ( ptr.getDate().lessThan(dateLocal) ) {
				// Have looped back too far without finding a date so break and add a new point.
				// Need to insert after the current position.

				insert_position = i + 1;
				break;
			}
		}
	}
	if ( found ) {
		//if ( Message.isDebugOn ) {
		//	Message.printDebug( 20, routine, "Updated data value at: " + dateLocal + " to " + value + "." );
		//}
		return 1;
	}

	if ( Message.isDebugOn ) {
		//Message.printDebug( 20, routine, "Attempting to add new TSData object to list for " + dateLocal + " : " + value );
	}

	// If here, then didn't find the date, need to create the new tsdata object.

	tsdata = new TSData();
	tsdata.setValues( dateLocal, value, _data_units, data_flag, duration );

	// If have determined the insert position from above, then do it.

	if ( insert_position >= 0 ) {
	    // Add the data in the given position.
		__tsDataList.add ( insert_position, tsdata );
		// Set the next/previous pointers (note this is done after the insert so compute positions accordingly.
		if ( insert_position == 0 ) {
		    // Added at beginning.
			ptr = __tsDataList.get(1);
			tsdata.setNext ( ptr ); // Previous will be the default of null.
			ptr.setPrevious ( tsdata );
		}
		else if ( insert_position == (__tsDataList.size() - 1) ) {
		    // Added at end.
			ptr = __tsDataList.get(insert_position - 1);
			tsdata.setPrevious ( ptr ); // Next will be the default of null.
			ptr.setNext ( tsdata );
		}
		else {
		    // Added somewhere in the middle.
		    ptr = __tsDataList.get(insert_position-1);
			tsdata.setPrevious ( ptr );
			ptr.setNext ( tsdata );
			ptr = __tsDataList.get(insert_position + 1);
			tsdata.setNext ( ptr );
			ptr.setPrevious ( tsdata );
		}
		__prevSetDataPointer = ptr;

		//if ( Message.isDebugOn ) {
		//	Message.printDebug( 20, routine, "Inserted data value at: " + dateLocal + " value: " + tsdata + "." );
		//}

		// Reset the limits of the data.
		if ( dateLocal.lessThan(_date1) ) {
			_date1 = new DateTime( dateLocal );
		}
		if ( dateLocal.greaterThan(_date2) ) {
			_date2 = new DateTime( dateLocal );
		}

		// Mark dirty so that the data limits will be recomputed.
		_dirty = true;

		// Update the data size.
		setDataSize ( this.__tsDataList.size() );
		return 1;
	}

	// If here, must have a logic problem.
	String routine2 = getClass().getSimpleName() + ".setDataValue";
	Message.printWarning ( 3, routine2, "Logic problem in routine.  Need to fix!" );
	return 0;
}

}