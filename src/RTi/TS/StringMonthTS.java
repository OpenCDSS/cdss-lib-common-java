// StringMonthTS - store a monthly time series as strings

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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.lang.String;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
This class is provided as an extension to MonthTS and holds monthly time series
as strings.  An example is:
<p>
<pre>
Year   Jan  Feb  Mar  Apr  May  Jun  Jul  Aug  Sep  Oct  Nov  Dec
1990   wet  wet  dry  dry  avg  avg  wet  avg  dry  wet  dry  dry
1991   dry  dry  wet  wet  wet  wet  avg  dry  avg  dry  avg  avg
</pre>
The strings within the pattern are referred to as data strings.
<p>

The time series can then be used, for example, for filling data.  Because the
data are treated as strings, they can in fact be used for anything (e.g., counts
within a month, indicators, etc.).  Because strings are used to store data,
different set/get routines are implemented compared to the standard time series
classes.  Currently, there are no read/write methods because it is anticipated
that the time series file formats will match those of other time series classes
(e.g., StateModMonthTS) and the read/write routines should be implemented in those classes.
@see MonthTS
*/
@SuppressWarnings("serial")
public class StringMonthTS 
extends MonthTS
implements Cloneable, Serializable, Transferable {
// Data members...

// The location part of the TSIdent is used as the time series name.
// The multiplier default of 1 is currently the only multiplier that is supported.

/**
The DataFlavor for transferring this specific class.
*/
public static DataFlavor stringMonthTSFlavor = new DataFlavor(
	RTi.TS.StringMonthTS.class, "RTi.TS.StringMonthTS");

/**
Array to hold data.
*/
private String _data[][] = null;
// TODO SAM 2010-07-30 Can String.intern() be used instead here?
/**
Unique strings in data.  Used when processing TSPatternStats.
*/
private List<String> _unique_data = new ArrayList<String>(10);
/**
Default for missing data.
*/
private String __missing_string = "";

/**
Constructor.
@param tsident_string Time series identifier string (this is the pattern
name that will be used - generally only the location needs to be specified).
@param date1 Starting date for time series.
@param date2 Ending date for time series.
@exception Exception if there is a problem allocating the data space.
*/
public StringMonthTS ( String tsident_string, DateTime date1, DateTime date2 )
throws Exception
{	super ();
	try {
	    init( tsident_string, date1, date2 );
		allocateDataSpace();
	}
	catch ( TSException e ) {
		throw e;
	}
}

/**
Copy constructor - not implemented.
@param ts Time series to copy.
*/
public StringMonthTS ( StringMonthTS ts )
{	String routine = "StringMonthTS.StringMonthTS(ts)";

	Message.printWarning ( 1, routine, "Not implemented" );
}

/**
Allocate the data space for the time series.  The start and end dates and the
data interval multiplier must have been set.  Initialize the data with
blank string attributes.
@return 1 if failure, 0 if success.
*/
public int allocateDataSpace()
{	return allocateDataSpace ( "" );
}

/**
Allocate the data space for the time series.  The start and end dates and the
data interval multiplier must have been set.  Fill with the specified data value.
@param value Value to initialize data space.
@return 1 if failure, 0 if success.
*/
public int allocateDataSpace( String value )
{	String routine="StringMonthTS.allocateDataSpace";
	int	i, nvals, nyears = 0;
	DateTime date;

	if ( (_date1 == null) || (_date2 == null) ) {
		Message.printWarning ( 2, routine, "Dates have not been set.  Cannot allocate data space" );
		return 1;
	}
	
	nyears = _date2.getYear() - _date1.getYear() + 1;
	
	if( nyears == 0 ){
		Message.printWarning( 2, routine, "TS has 0 years POR, maybe Dates haven't been set yet" );
		return 1;
	}

	_data = new String [nyears][];

	// Allocate memory...

	for ( i = 0, date = new DateTime(_date1,DateTime.DATE_FAST); i < nyears;
		i++, date.addInterval(_data_interval_base, _data_interval_mult) ) {
		if ( _data_interval_mult == 1 ) {
			// Easy to handle 1 month data...
			nvals = 12;
		}
		else {
		    // Do not know how to handle N-month interval...
			Message.printWarning ( 2, routine,
			"Only know how to handle 1 month data, not " + _data_interval_mult + "-month" );
			return 1;
		}
		_data[i] = new String[nvals];

		// Now fill the entire year with the missing data value...

		for ( int j = 0; j < nvals; j++ ) {
			_data[i][j] = new String ( value );
		}
	}

	// Set the data size...

	int datasize = calculateDataSize ( _date1, _date2, _data_interval_mult);
	setDataSize ( datasize );

	// Set the limits used for set/get routines...  These are in the MonthTS class...

	_min_amon = _date1.getAbsoluteMonth();
	_max_amon = _date2.getAbsoluteMonth();

	if ( Message.isDebugOn ) {
		Message.printDebug( 10, routine, "Successfully allocated " + nyears +
		" years of memory (" + datasize + " month values)" ); 
	}

	return 0;
}

/**
Dummy routine to prevent warnings.  This is mainly called when getting data limits.
@param date Date to get data.
@return 0.0 always.
*/
public double getDataValue ( DateTime date )
{	return 0.0;
}

/**
Return the data value as an double.
@param date Date of interest.
@exception RTi.TS.TSException if the data string is null.
*/
public double getDataValueAsDouble ( DateTime date )
throws TSException
{	String value = getDataValueAsString ( date );
	if ( value != null ) {
		throw new TSException ( "Null data value" );
	}
	return StringUtil.atod ( value );
}

/**
Return the data value as an int.
@param date Date of interest.
@exception RTi.TS.TSException if the data string is null.
*/
public int getDataValueAsInt ( DateTime date )
throws TSException
{	String value = getDataValueAsString ( date );
	if ( value != null ) {
		throw new TSException ( "Null data value" );
	}
	return StringUtil.atoi ( value );
}

/**
Return the String data value for the date.
@return The data value corresponding to the date.  This is very similar to the
MonthTS method except that a String is returned.
@param date Date of interest.
*/
public String getDataValueAsString ( DateTime date )
{	String	routine = "StringMonthTS.getDataValue";
	int	column=0, dl = 50, row=0;

	//Check the date coming in 

	if ( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, date + " not within data period (" + _date1 + " - " + _date2 + ")" );
		}
		return "";
	}

	// This is in the MonthTS base class...
	int [] pos = getDataPosition(date);
	if ( pos == null ) {
		if ( Message.isDebugOn ) {
			// Wrap in debug to boost performance...
			Message.printWarning( 3, routine, "Unable to get data position for " + date );
		}
		return "";
	}
	row	= pos[0];
	column	= pos[1];

	if ( Message.isDebugOn ) {
		Message.printDebug( dl, routine,
		_data[row][column] + " for " + date + " from _data[" + row + "][" + column + "]" );
	}

	return _data[row][column];
}

/**
Return the number of unique data values.
@return The number of unique data values.
*/
public int getNumUniqueData ()
{	refresh ();
	return _unique_data.size();
}

/**
Return the unique data values.
@return The unique data values.
*/
public List<String> getUniqueData ()
{	refresh ();
	return _unique_data;
}

/**
Returns the data in the specified DataFlavor, or null if no matching flavor
exists.  From the Transferable interface.  Supported dataflavors are:<br>
<ul>
<li>StringMonthTS - StringMonthTS.class / RTi.TS.StringMonthTS</li>
<li>MonthTS - MonthTS.class / RTi.TS.MonthTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul> 
@param flavor the flavor in which to return the data.
@return the data in the specified DataFlavor, or null if no matching flavor
exists.
*/
public Object getTransferData(DataFlavor flavor) {
	if (flavor.equals(stringMonthTSFlavor)) {
		return this;
	}
	else if (flavor.equals(MonthTS.monthTSFlavor)) {
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
<li>StringMonthTS - StringMonthTS.class / RTi.TS.StringMonthTS</li>
<li>MonthTS - MonthTS.class / RTi.TS.MonthTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@return the flavors in which data can be transferred.
*/
public DataFlavor[] getTransferDataFlavors() {
	DataFlavor[] flavors = new DataFlavor[4];
	flavors[0] = stringMonthTSFlavor;
	flavors[1] = MonthTS.tsFlavor;
	flavors[2] = TS.tsFlavor;
	flavors[3] = TSIdent.tsIdentFlavor;
	return flavors;
}

/**
Indicate whether the time series has data, determined by checking to see whether
the data space has been allocated.  This method can be called after a time
series has been read - even if no data are available, the header information
may be complete.  The alternative of returning a null time series from a read
method if no data are available results in the header information being
unavailable.  Instead, return a TS with only the header information and call
hasData() to check to see if the data space has been assigned.
@return true if data are available (the data space has been allocated).
Note that true will be returned even if all the data values are set to the missing data value.
*/
public boolean hasData ()
{	if ( _data != null ) {
		return true;
	}
	else {
	    return false;
	}
}

/**
Initialize the private data members.  This method accepts all the possible
parameters and handles null appropriately.  This method does not allocate memory.
@param tsident_string Time series identifier string (this is the pattern
name that will be used - generally only the location need be specified).
@param date1 Starting date for time series.
@param date2 Ending date for time series.
@exception Exception if an error occurs.
*/
private void init ( String tsident_string, DateTime date1, DateTime date2 )
throws Exception
{	if ( tsident_string == null ) {
		// Big problem...
		String message = "Null identifier.";
		Message.printWarning ( 2, "StringMonthTS.init", message );
		throw new TSException ( message );
	}
	// After setting, the following will be used by allocate data space...
	setIdentifier ( tsident_string );
	setDate1 ( date1 );
	setDate2 ( date2 );
}

/**
Determines whether the specified flavor is supported as a transfer flavor.
From the Transferable interface.  Supported dataflavors are:<br>
<ul>
<li>StringMonthTS - StringMonthTS.class / RTi.TS.StringMonthTS</li>
<li>MonthTS - MonthTS.class / RTi.TS.MonthTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul> 
@param flavor the flavor to check.
@return true if data can be transferred in the specified flavor, false if not.
*/
public boolean isDataFlavorSupported(DataFlavor flavor) {
	if (flavor.equals(stringMonthTSFlavor)) {
		return true;
	}
	else if (flavor.equals(MonthTS.monthTSFlavor)) {
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
Determine if a data value for the time series is missing.  The missing value can
be set using setMissing().
There is no straightforward way to check to see if a value is equal to NaN
(the code: if ( value == Double.NaN ) will always return false if one or both
values are NaN).  Consequently there is no way to see know if only one or both
values is NaN, using the standard operators.  Instead, we assume that NaN
should be interpreted as missing and do the check if ( value != value ), which
will return true if the value is NaN.  Consequently, code that uses time series
data should not check for missing and treat NaN differently because the TS
class treats NaN as missing.
@return true if the data value is missing, false if not.
@param value Value to check.
*/
public boolean isDataMissing ( String value )
{	if ( value.equalsIgnoreCase("NaN") ) {
		// Check for NaN...
		return true;
	}
	if ( value.equals(__missing_string) ) {
		return true;
	}
	return false;
}

/**
Refresh the derived data.  This will compute the number of unique attribute values.
*/
public void refresh ()
{	String routine = "StringMonthTS.refresh";

	// Call on the base class...

	if ( !_dirty ) {
		return;
	}

	super.refresh();
	// Will reset _dirty.

	// Now do our thing...

	// Figure out how many unique values there are (brute force)...

	String string_j, value;
	int	j, size;
	DateTime date = null;
	boolean	found;
	for ( date = new DateTime ( _date1, DateTime.DATE_FAST ); date.lessThanOrEqualTo(_date2);
		date.addInterval(_data_interval_base, _data_interval_mult) ) {
		// Get the value...
		value = getDataValueAsString(date);
		// Now search through the known list...
		size = _unique_data.size();
		found = false;
		for ( j = 0; j < size; j++ ) {
			string_j = (String)_unique_data.get(j);
			if ( string_j.equals(value) ) {
				// Same string.  Go to next string to compare...
				found = true;
				break;
			}
		}
		if ( !found ) {
			// A new string.  Add and break...
			_unique_data.add(value);
			if ( Message.isDebugOn ) {
				Message.printStatus ( 1, routine, "Adding unique string to list: \"" + value + "\"" );
			}
		}
	}
}

/**
Set the attribute for a date.  This has very similar logic to the MonthTS
function but uses a string instead.
@param date Date to set value.
@param value Data value to set.
*/
public void setDataValue ( DateTime date, String value )
{	int	column, dl = 50, row;

	if ( value == null ) {
		// Ignore the set.
		return;
	}

	int [] pos = getDataPosition(date);
	if ( pos == null ) {
		if ( Message.isDebugOn ) {
			// Wrap in debug to boost performance...
			Message.printWarning( 2, "StringMonthTS.setDataValue", "Unable to get data position for " + date );
		}
		return;
	}
	row	= pos[0];
	column	= pos[1];

	if ( Message.isDebugOn ) {
		Message.printDebug( dl, "StringMonthTS.setDataValue",
		"Setting " + value + " " + date + " at " + row + "," + column );
	}

	// Set the dirty flag so that we know to recompute the limits if desired...

	_dirty = true;

	// Save as a copy of the string...

	_data[row][column] = value;
}

/**
Set the missing data value for the time series.
@param missing Missing data value for time series.
*/
public void setMissing ( String missing )
{	__missing_string = missing;
}

}
