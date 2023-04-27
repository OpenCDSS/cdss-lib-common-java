// TS - base class from which all time series are derived

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

// ----------------------------------------------------------------------------
// TS - base class from which all time series are derived
// see C++ version for difference notes.
// ----------------------------------------------------------------------------
// This is the abstract base class for all time series.
// The derived class hierarchy is then:
//
//              -----------------------TS-----------------------------
//              |           |        |          |                    |
//              V           V        V          V                    V
//           MinuteTS     HourTS   DayTS     MonthTS             IrregularTS
//              |           |                   |
//
//         ~~~~~~~~~~~~~~~~~~~Below have static read/write methods~~~~~~~~~~
//              |           |                   |
//              V           V                   V
//       HECDSSMinuteTS NWSCardTS          DateValueTS		etc.
//
// In general, the majority of the data members will be used for all derived time series.
// However, some will not be appropriate.
// The decision has been made to keep them here to make it less work to manage the first layer of derived classes.
// For example, the irregular time series does not need to store _interval_mult.
// Most of the other data still apply.
//
// Because this is a base class, anything derived from the class can only be as specific as the base class.
// Therefore, routines to do conversions between
// time series types will have to be done outside of this class library.
// Using the TS base class allows polymorphism when used by complicated objects.
//
// It is assumed that the first layer will define the best way to store the data.
// In other words, different data intervals will have different storage methods.
// The goal is not to conceptualize time series so much that they all consist of an array of date and data values.
// For constant interval time series,
// the best performance will be to store the values in an ordered array that is indexed by month, etc.
// If this assumption is wrong, then maybe the middle layer gets folded into the base class.
//
// It may be desirable to build some streaming control into this class.
// For example, read multiple time series from the same file (for certain time series types)
// or may want to read one block of data from multiple files
// (e.g., when running a memory-sensitive application that only needs to have
// in memory one month of data for every time series in the system and reuses that space).
// For now assume that we will always read the entire time series
// in but be aware that more control may be added later.
//
// New code does not have 3 layers.  Instead, I/O classes should have static
// methods like readTimeSeries, writeTimeSeries that operate on time series
// instances.  See DateValueTS for an example.  Older code may not follow this approach.

package RTi.TS;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
This class is the base class for all time series classes.
General functionality is implemented in this class and specific functionality such as data set/get must be
implemented in derived classes.
*/
@SuppressWarnings("serial")
public class TS implements Cloneable, Serializable, Transferable
{

// FIXME SAM 2007-12-13 Need to move transfer objects to wrapper around this class.
/**
Data flavor for transferring this object.
*/
public static DataFlavor tsFlavor = new DataFlavor(RTi.TS.TS.class,"RTi.TS.TS");

/**
General string to use for status of the time series (use as appropriate by high-level code).
This value is volatile - do not assume its value will remain for long periods.
This value is not used much now that the GRTS package has been updated.
*/
protected String _status;

/**
Beginning date/time for data, at a precision appropriate for the data.
Missing data may be included in the period.
*/
protected DateTime _date1;

/**
Ending date/time for data, at a precision appropriate for the data.
Missing data may be included in the period.
*/
protected DateTime _date2;

/**
Original starting date/time for data, at a precision appropriate for the data.
For example, this may be used to indicate the period in a database,
which is different than the period that was actually queried and saved in memory.
*/
protected DateTime _date1_original;

/**
Original ending date/time for data, at a precision appropriate for the data.
For example, this may be used to indicate the period in a database,
which is different than the period that was actually queried and saved in memory.
*/
protected DateTime _date2_original;

/**
The data interval base.  See TimeInterval.HOUR, etc.
*/
protected int _data_interval_base;

/**
The base interval multiplier (what to multiply _interval_base by to get the real interval).
For example 15-minute data would have _interval_base = TimeInterval.MINUTE and _interval_mult = 15.
*/
protected int _data_interval_mult;

/**
The data interval in the original data source (for example,
the source may be in days but the current time series is in months).
*/
protected int _data_interval_base_original;

/**
The data interval multiplier in the original data source.
*/
protected int _data_interval_mult_original;

/**
The precision used to format data values,
can be used to override the precision if data units are not specified.
The default behavior is to determine the precision from units.
-1 indicates that no precision is set so use units or other default.
*/
protected short dataPrecision = -1;

/**
Number of data values inclusive of _date1 and _date2.
Set in the allocateDataSpace() method.  This is useful for general information.
*/
protected int _data_size;

/**
Data units.  A list of units and conversions is typically maintained in the DataUnits* classes.
*/
protected String _data_units;

/**
Units in the original data source (e.g., the current data may be in CFS and the original data were in CMS).
*/
protected String _data_units_original;

/**
Indicates whether data flags are being used with data.
If enabled, the derived classes that store data should override the allocateDataSpace(boolean, int)
method to create a data array to track the data flags.
It is recommended to save space that the flags be handled using String.intern().
*/
protected boolean _has_data_flags = false;

/**
Indicate whether data flags should use String.intern().
*/
protected boolean _internDataFlagStrings = true;

// FIXME SAM 2007-12-13 Need to phase this out in favor of handling in DAO code.
/**
Version of the data format (mainly for use with files).
*/
protected String _version;

// FIXME SAM 2007-12-13 Need to evaluate renaming to avoid confusion with TSIdent input name.
// Implementing a DataSource concept for input/output may help (but also have data source in TSIdent!).
/**
Input source information.  Filename if read from file or perhaps a database
name and table (e.g., HydroBase.daily_flow).  This is the actual location read,
which should not be confused with the TSIdent storage name (which may not be fully expanded).
*/
protected String _input_name;

/**
Time series identifier, which provides a unique and absolute handle on the time series.
An alias is provided within the TSIdent class.
*/
protected TSIdent _id;

/**
Indicates whether the time series data have been modified by calling setDataValue().
Call refresh() to update the limits.  This is not used with header data.
*/
protected boolean _dirty;

/**
Indicates whether the time series is editable.
This primarily applies to the data (not the header information).
UI components can check to verify whether users should be able to edit the time series.
It is not intended to be checked by low-level code (manipulation is always granted).
*/
protected boolean _editable = false;

/**
A short description (e.g, "XYZ gage at ABC river").
*/
protected String _description;

/**
Comments that describe the data.  This can be anything from an original data source.
Sometimes the comments are created on the fly to generate a standard header (e.g., describe drainage area).
*/
protected List<String> _comments;

/**
List of metadata about data flags.
This provides a description about flags encountered in the time series.
*/
private List<TSDataFlagMetadata> __dataFlagMetadataList = new ArrayList<>();

/**
History of time series.
This is not the same as the comments but instead chronicles how the time series is manipulated in memory.
For example the first genesis note may be about how the time series was read.
The second may indicate how it was filled.  Many TSUtil methods add to the genesis.
*/
protected List<String> _genesis;

/**
TODO SAM 2010-09-21 Evaluate whether generic "Attributable" interface should be implemented instead.
Properties for the time series beyond the built-in properties.
For example, location information like county and state can be set as a property.
*/
private LinkedHashMap<String,Object> __property_HashMap = null;

/**
 * Map of associated time series where the key is an identifying string, similar to a property name,
 * and the value is a time series.
 * This is used, for example, in graphing code for dynamically created data.
*/
private HashMap<String,TS> __associatedTimeSeries_HashMap = new HashMap<>();

/**
The missing data value.  Default for some legacy formats is -999.0 but increasingly Double.NaN is used.
*/
protected double _missing;

/**
Lower bound on the missing data value (for quick comparisons and when missing data ranges are used).
*/
protected double _missingl;

/**
Upper bound on the missing data value (for quick comparisons and when missing data ranges are used).
*/
protected double _missingu;

/**
Limits of the data.  This also contains the date limits other than the original dates.
*/
protected TSLimits _data_limits;

/**
Limits of the original data.  Currently only used by apps like TSTool.
*/
protected TSLimits _data_limits_original;

//TODO SAM 2007-12-13 Evaluate need now that GRTS is available.
/**
Legend to show when plotting or tabulating a time series.  This is generally a short legend.
*/
protected String _legend;

// TODO SAM 2007-12-13 Evaluate need now that GRTS is available.
/**
Legend to show when plotting or tabulating a time series.  This is usually a long legend.
This may be phased out now that the GRTS package has been phased in for visualization.
*/
protected String _extended_legend;

/**
Indicates whether time series is enabled (used to "comment" out of plots, etc).
This may be phased out.
*/
protected boolean _enabled;

/**
Indicates whether time series is selected (e.g., as result of a query).
Often time series might need to be programmatically selected (e.g., with TSTool
selectTimeSeries() command) to simplify output by other commands.
*/
protected boolean _selected;

/**
Construct a time series and initialize the member data.
Derived classes should set the _data_interval_base.
*/
public TS () {
	if ( Message.isDebugOn ) {
		Message.printDebug( 50, "TS.TS", "Constructing" );
	}
	init();
}

/**
Copy constructor.  Only the header is copied since derived classes should copy the data.
@see #copyHeader
*/
public TS ( TS ts ) {
	copyHeader ( this );
}

/**
Add a TSDataFlagMetadata instance to the list maintained with the time series, to explain flag meanings.
@param dataFlagMetadata instance of TSDataFlagMetadata to add to time series.
*/
public void addDataFlagMetadata ( TSDataFlagMetadata dataFlagMetadata ) {
    __dataFlagMetadataList.add(dataFlagMetadata);
}

/**
Add a String to the comments associated with the time series (e.g., station remarks).
@param comment Comment string to add.
*/
public void addToComments( String comment ) {
	if ( comment != null ) {
		_comments.add ( comment );
	}
}

/**
Add a list of String to the comments associated with the time series (e.g., station remarks).
@param comments Comments strings to add.
*/
public void addToComments( List<String> comments ) {
	if ( comments == null ) {
		return;
	}
	for ( String comment : comments ) {
		if ( comment != null ) {
			_comments.add ( comment );
		}
	}
}

/**
Add a string to the genesis string list.
The genesis is a list of comments indicating how the time series was read and manipulated.
Genesis information should be added by methods that, for example, fill data and change the period.
@param genesis Comment string to add to genesis information.
*/
public void addToGenesis ( String genesis ) {
	if ( genesis != null ) {
		_genesis.add ( genesis );
	}
}

/**
Allocate the data flag space for the time series.
This requires that the data interval base and multiplier are set correctly and that _date1 and _date2 have been set.
The allocateDataSpace() method will allocate the data flags if appropriate.
Use this method when the data flags need to be allocated after the initial allocation.
This method is meant to be overridden in derived classes (e.g., MinuteTS, MonthTS)
that are optimized for data storage for different intervals.
@param initialValue Initial value (null is allowed and will result in the flags being initialized to spaces).
@param retainPreviousValues If true, the array size will be increased if necessary,
but previous data values will be retained.  If false, the array will be reallocated and initialized to spaces.
@exception Exception if there is an error allocating the memory.
*/
public void allocateDataFlagSpace (	String initialValue, boolean retainPreviousValues )
throws Exception {
	Message.printWarning ( 1, "TS.allocateDataFlagSpace", "TS.allocateDataFlagSpace() is virtual, define in derived classes." );
}

/**
Allocate the data space for the time series.
This requires that the data interval base and multiplier are set correctly and that _date1 and _date2 have been set.
If data flags are used, hasDataFlags() should also be called before calling this method.
This method is meant to be overridden in derived classes (e.g., MinuteTS, MonthTS)
that are optimized for data storage for different intervals.
@return 0 if successful allocating memory, non-zero if failure.
*/
public int allocateDataSpace ( )
{	Message.printWarning ( 1, "TS.allocateDataSpace", "TS.allocateDataSpace() is virtual, define in derived classes." );
	return 1;
}

/**
Allocate the data space for the time series using given dates.
This requires that the data interval base and multiplier are set correctly in the derived class
(the allocateDataSpace(void) method of the derived class will be called).
@param date1 Start of period.
@param date2 End of period.
@return 1 if there is an error allocating the data space, 0 if success.
*/
public int allocateDataSpace ( DateTime date1, DateTime date2 ) {
	// Set data.
    setDate1 ( date1 );
    setDate1Original ( date1 );
    setDate2 ( date2 );
    setDate2Original ( date2 );

    // Allocate memory for the new data space.
	if ( allocateDataSpace() != 0 ) {
		Message.printWarning ( 3, "TS.allocateDataSpace(DateTime,DateTime)",
		"Error allocating data space for " + date1 + " to " + date2 );
		return 1;
	}
	return 0;
}

/**
Calculate the data size (number of data points) for a time series,
given the interval multiplier and start and end dates.
This method is meant to be overridden in derived classes (in which case the interval base will be known).
@param date1 Start of period.
@param date2 End of period.
@param multiplier Multiplier for interval base.
@return the number of data points in a period, given the interval mulitplier.
*/
public static int calculateDataSize ( DateTime date1, DateTime date2, int multiplier ) {
	Message.printWarning ( 1, "TS.calculateDataSize", "TS.calculateDataSize() is virtual, define in derived classes." );
	return 0;
}

/**
Change the period of record for the data.
If the period is lengthened, fill with missing data.  If shortened, truncate the data.
This method should be implemented in a derived class and should handle resizing of the data space and data flag array.
@param date1 Start date for new period.
@param date2 End date for new period.
@exception RTi.TS.TSException if this method is called in the base class (or if an error in the derived class).
*/
public void changePeriodOfRecord ( DateTime date1, DateTime date2 )
throws TSException {
	String routine = "TS.changePeriodOfRecord";

	Message.printWarning ( 1, routine, "TS.changePeriodOfRecord is a virtual function, redefine in derived classes" );

	throw new TSException ( routine + ": changePeriodOfRecord needs to be implemented in derived class!" );
}

/**
Clone the object.
The Object base class clone() method is called and then the TS objects are cloned.  The result is a complete deep copy.
*/
public Object clone () {
	try {
        // Clone the base class.
		TS ts = (TS)super.clone();
		// Clone mutable objects.
		if ( _date1 != null ) {
			ts._date1 = (DateTime)_date1.clone();
		}
		if ( _date2 != null ) {
			ts._date2 = (DateTime)_date2.clone();
		}
		if ( _date1_original != null ) {
			ts._date1_original = (DateTime)_date1_original.clone();
		}
		if ( _date2_original != null ) {
			ts._date2_original = (DateTime)_date2_original.clone();
		}
		if ( _id != null ) {
			ts._id = (TSIdent)_id.clone();
		}
		int size = 0;
		int i = 0;
		if ( _comments != null ) {
			ts._comments = new ArrayList<>(_comments.size());
			size = _comments.size();
			for ( i = 0; i < size; i++ ) {
				ts._comments.add( new String(_comments.get(i)));
			}
		}
		if ( _genesis != null ) {
			ts._genesis = new ArrayList<>(_genesis.size());
			size = _genesis.size();
			for ( i = 0; i < size; i++ ) {
				ts._genesis.add(new String(_genesis.get(i)));
			}
			ts.addToGenesis ("Made a copy of TSID=\"" + getIdentifier() +
			    "\" Alias=\"" + getAlias() + "\" (previous history information is for original)" );
		}
		if ( _data_limits != null ) {
			ts._data_limits = (TSLimits)_data_limits.clone();
		}
		if ( _data_limits_original != null ) {
			ts._data_limits_original = (TSLimits)_data_limits_original.clone();
		}
		return ts;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
Copy the data array from one time series to another.  This method should be defined in a derived class.
@param ts The time series to copy the data from.
@param start_date The time to start copying the data (if null, use the first date from this instance).
@param end_date The time to end copying the data (if null, use the last date from this instance).
@param copy_missing If true, copy missing data (including out-of-period missing data).
If false, ignore missing data.
@exception TSException if an error occurs.
*/
public void copyData ( TS ts, DateTime start_date, DateTime end_date, boolean copy_missing )
throws TSException {
	String message = "This method needs to be implemented in a derived class (e.g., MonthTS)";
	Message.printWarning ( 3, "TS.copyData", message );
	throw new TSException ( message );
}

/**
Copy the data array from one time series to another.  This method should be defined in a derived class.
@param ts The time series to copy the data from.
@param copy_missing If true, copy missing data (including out-of-period missing
data).  If false, ignore missing data.
@exception TSException if an error occurs.
*/
public void copyData ( TS ts, boolean copy_missing )
throws TSException {
	String message = "This method needs to be implemented in a derived class (e.g., MonthTS)";
	Message.printWarning ( 3, "TS.copyData", message );
	throw new TSException ( message );
}

/**
Copy one time series header to this one.
This copies everything except data related to the data space.
Note that the dates are also copied and allocateDataSpace() should be called if necessary to reset the data space.
The following data is copied (the associated set method is shown to allow individual
changes to be applied after the copy, if appropriate).
See also the second table that indicates what is NOT copied.
This method may need to be overloaded in the future to allow only a partial copy of the header.

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Data Member</b></td>	<td><b>Set Method</b></td>
</tr>

<tr>
<td>_comments</td>		<td>SetComments()</td>
</tr>

<tr>
<td>_data_flag_length</td>		<td>hasDataFlags()</td>
</tr>

<tr>
<td>_data_interval_base_original</td>	<td>setDataIntervalBaseOriginal()</td>
</tr>
<tr>
<td>_data_interval_mult_original</td>	<td>setDataIntervalMultOriginal()</td>
</tr>

<tr>
<td>_data_interval_mult</td>	<td>setDataIntervalMult()</td>
</tr>

<tr>
<td>_data_units</td>		<td>setDataUnits()</td>
</tr>

<tr>
<td>_data_units_original</td>	<td>setDataUnitsOriginal()</td>
</tr>

<tr>
<td>_date1</td>			<td>setDate1()</td>
</tr>
<tr>
<td>_date2</td>			<td>setDate2()</td>
</tr>

<tr>
<td>_date1_original</td>	<td>setDate1Original()</td>
</tr>
<tr>
<td>_date2_original</td>	<td>setDate2Original()</td>
</tr>

<tr>
<td>_date_type</td>		<td>Not implemented.</td>
</tr>

<tr>
<td>_description</td>		<td>SetDescription()</td>
</tr>

<tr>
<td>_extended_legend</td>	<td>setExtendedLegend()</td>
</tr>

<tr>
<td>_genesis</td>		<td>SetGenesis()</td>
</tr>

<tr>
<td>_has_data_flags</td>	<td>hasDataFlags()</td>
</tr>

<tr>
<td>_id</td>			<td>setIdentifier()</td>
</tr>

<tr>
<td>_input_name</td>		<td>setInputName()</td>
</tr>

<tr>
<td>_legend</td>		<td>setLegend()</td>
</tr>

<tr>
<td>_missing</td>		<td>setMissing() and setMissingRange()</td>
</tr>

<tr>
<td>_sequence_number</td>	<td>setSequenceNumber()</td>
</tr>

<tr>
<td>_status</td>		<td>setStatus()</td>
</tr>

<tr>
<td>_version</td>	<td>setVersion()</td>
</tr>

</table>

The following data are not copied:

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Data Member</b></td>	<td><b>Set Method</b></td>
</tr>

<tr>
<td>_data_size</td>		<td>Set in allocateDataSpace().</td>
</tr>

<tr>
<td>_data_interval_base</td>	<td>Set in the specific constructor.</td>
</tr>

<tr>
<td>_data_limits</td>		<td>Associated with data.</td>
</tr>

<tr>
<td>_dirty</td>			<td>Associated with data.</td>
</tr>

</table>
*/
public void copyHeader ( TS ts ) {
 	setVersion( ts.getVersion() );
	setStatus ( ts.getStatus() );

	setInputName ( ts.getInputName() );

	// Copy TSIdent.

	try {
		setIdentifier ( new TSIdent(ts.getIdentifier()) );
	}
	catch ( Exception e ) {
		// Should not happen.
	}

	// Need to initialize DateTime somehow, but how do you pick defaults?

	// TODO smalers 2022-03-04 can the following be removed?
	//_data_limits = new TSLimits ( ts.getDataLimits() );
	_date1 = new DateTime ( ts.getDate1() );
	_date2 = new DateTime ( ts.getDate2() );
	_date1_original = new DateTime ( ts.getDate1Original() );
	_date2_original = new DateTime ( ts.getDate2Original() );

    setDataType( ts.getDataType() );

	_data_interval_base = ts.getDataIntervalBase();
	_data_interval_mult = ts.getDataIntervalMult();
	_data_interval_base_original = ts.getDataIntervalBaseOriginal ();
	_data_interval_mult_original = ts.getDataIntervalMultOriginal ();

	setDescription( ts.getDescription() );

	_comments = new ArrayList<>();
	_comments = StringUtil.addListToStringList ( _comments, ts.getComments() );
	_genesis = new ArrayList<>();
	_genesis = StringUtil.addListToStringList ( _genesis, ts.getGenesis() );

	setDataUnits( ts.getDataUnits() );
	setDataUnitsOriginal( ts.getDataUnitsOriginal() );

	// First set the missing data value.
	setMissing( ts.getMissing() );
	// Now set the range itself in case it has been reset.
	setMissingRange ( ts.getMissingRange() );

	// TODO smalers 2022-03-04 can the following be removed?
	//_dirty = true; // Need to recompute limits when we get the chance.

	// Copy legend information.
	_legend = ts.getLegend();
	_extended_legend = ts.getExtendedLegend();

	// Data flags.

	_has_data_flags = ts._has_data_flags;
}

/**
 * Indicate whether the data interval uses time.
 * If irregular interval, the irregular interval precision is checked.
 * @return true if the data interval uses time, false if not
 */
public boolean dataIntervalUsesTime () {
	// The following are default checks:
	// - classes can overload with more robust logic
    if ( isRegularInterval() ) {
    	if ( (getDataIntervalBase() == TimeInterval.DAY) ||
    		(getDataIntervalBase() == TimeInterval.MONTH) ||
    		(getDataIntervalBase() == TimeInterval.YEAR) ) {
    		return false;
    	}
    }
    else {
    	// Irregular interval, must check the irregular precision.
    	if ( (getIdentifier().getIrregularIntervalPrecision() == TimeInterval.DAY) ||
    		(getIdentifier().getIrregularIntervalPrecision() == TimeInterval.MONTH) ||
    		(getIdentifier().getIrregularIntervalPrecision() == TimeInterval.YEAR) ) {
    		return false;
    	}
    }
    // Default is to use time.
    return true;
}

/**
Return a formatted extended legend string without changing saved legend.
@return A formatted extended legend string for the time series but do not update the time series extended legend data.
The format is the same as for formatLegend().
@param format Format string.
@see #formatLegend
*/
public String formatExtendedLegend ( String format ) {
	return formatExtendedLegend ( format, false );
}

/**
Return a formatted extended legend string and optionally save the changed legend.
@return A formatted extended legend string for the time series.
The format is the same as for formatLegend().
@param format Format string.
@param flag If true, the extended legend data member will be updated after formatting.
@see #formatLegend
*/
public String formatExtendedLegend ( String format, boolean flag ) {
	// First format using the normal legend string.
	String extended_legend = formatLegend ( format );
	// Now, if desired, save the legend.
	if ( flag ) {
		setExtendedLegend ( extended_legend );
	}
	return extended_legend;
}

/**
Format a standard time series header, for use with formatOutput.
@return A list of strings containing the header.
Blank lines at the start and end of the header are not included.
*/
public List<String> formatHeader () {
	List<String> header = new ArrayList<>( 10 );

    header.add ( "Time series alias       = " + getAlias() );
    header.add ( "Time series identifier  = " + getIdentifier() );
	header.add ( "Description             = " + getDescription() );
	header.add ( "Data source             = " + getIdentifier().getSource() );
	header.add ( "Data type               = " + getIdentifier().getType() );
	header.add ( "Data interval           = " + getIdentifier().getInterval() );
	header.add ( "Data units              = " + _data_units );
	if ( (_date1 != null) && (_date2 != null) ) {
		header.add ( "Period                  = " + _date1 + " to " + _date2 );
		//StringUtil.formatString(_date1.getYear(), "%04d") + "-" +
		//StringUtil.formatString(_date1.getMonth(), "%02d") + " to " +
		//StringUtil.formatString(_date2.getYear(), "%04d") + "-" +
		//StringUtil.formatString(_date2.getMonth(), "%02d") ) );
	}
	else {
	    header.add ( "Period                  = N/A" );
	}
	if ( (_date1_original != null) && (_date2_original != null)) {
		header.add ( "Orig./Avail. period     = " + _date1_original + " to " + _date2_original );
		//StringUtil.formatString(
		//_date1_original.getYear(), "%04d") + "-" +
		//StringUtil.formatString(
		//_date1_original.getMonth(), "%02d") + " to " +
		//StringUtil.formatString(
		//_date2_original.getYear(), "%04d") + "-" +
		//StringUtil.formatString(
		//_date2_original.getMonth(), "%02d") ) );
	}
	else {
	    header.add ( "Orig./Avail. period     = N/A" );
	}
	return header;
}

/**
Return a formatted legend string, without changing the legend in memory.
@return A formatted legend string for the time series but do not update the time series legend data.
See the version that accepts a flag for a description of format characters.
@param format Format string.
*/
public String formatLegend ( String format ) {
	return formatLegend ( format, false );
}

// FIXME SAM This code seems redundant with similar code in TSUtil.  Need to refactor and consolidate.
/**
Return a formatted legend string, optionally changing the legend in memory.
@return A formatted legend string for the time series but do not update the time series legend data.
@param format Format string containing normal characters and formatting strings
that will be replaced with time series header information.
Format specifiers can reference time series properties using ${ts:PropertyName} notation,
or use the % specifiers as follows (grouped in categories):
<p>

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Format specifier</b></td>	<td><b>Description</b></td>
</tr

<tr>
<td><b>%%</b></td>
<td>Literal percent character.</td>.
</tr>

<tr>
<td><b>%A</b></td>
<td>Alias (from TSIdent).</td>
</tr>

<tr>
<td><b>%D</b></td>
<td>Description (e.g., "RED RIVER BELOW MY TOWN").</td>
</tr>

<tr>
<td><b>%F</b></td>
<td>Full time series identifier (e.g.,
"12345678_FREE.CRDSS_USGS.QME.24Hour.Trace1")</td>.
</tr>

<tr>
<td><b>%I</b><br><p>
<b>%b</b><p>
<b>%m</b></td>
<td>The full data interval part of the identifier (e.g., "24Hour").<p>
Interval multiplier part of the identifier (e.g., "24").<p>
Base data interval part of the identifier (e.g., "Hour").</td>
</tr>

<tr>
<td><b>%i</b><br><p></td>
<td>The input name part of the identifier (e.g., the file name or database table from which the time series was read).</td>
</tr>

<tr>
<td><b>%L</b><br><p>
<b>%l</b><p>
<b>%w</b></td>
<td>The full location part of the identifier (e.g., "12345678_FREE").<p>
Main location part of the identifier (e.g., "12345678").<p>
Sub-location part of the identifier (e.g., "FREE").</td>
</tr>

<tr>
<td><b>%p</b></td>
<td>The time series period using date/times corresponding to the precision of the data (e.g., "2006-10-01 18 Z - 2006-10-03 18 Z").
</td>
</tr>

<tr>
<td><b>%S</b><br><p>
<b>%s</b><p>
<b>%x</b></td>
<td>The full data source part of the identifier (e.g., "CRDSS_USGS").<p>
Main data source part of the identifier (e.g., "CRDSS").<p>
Sub-source part of the identifier (e.g., "USGS").</td>
</tr>

<tr>
<td><b>%T</b><br><p>
<b>%t</b><p>
<b>%k</b></td>
<td>The full data type part of the identifier (e.g., "QME").<p>
Main data source part of the identifier (reserved for future use and currently the same as the full data type).<p>
Sub-type part of the identifier.</td>
</tr>

<tr>
<td><b>%U</b></td>
<td>Data units (e.g., "CFS"; see DataUnits).</td>
</tr>

<tr>
<td><b>%Z</b></td>
<td>Scenario part of the identifier (e.g., "Trace1").</td>
</tr>

<tr>
<td><b>%z</b></td>
<td>Time series sequence ID (e.g., year for traces).</td>
</tr>
</table>
<p>
@param update_ts If true, the legend data member in the time series will be
updated.  Otherwise a formatted string is returned but the internal data member is left unchanged.
@see TSIdent
@see RTi.Util.IO.DataUnits
*/
public String formatLegend ( String format, boolean update_ts ) {
	StringBuffer buffer = new StringBuffer();

	//Message.printStatus ( 2, "", "Legend format is " + format );
	// Loop through the format string and if format strings are found,
	// append to the buffer.  Otherwise, transfer all characters as given.
	if ( format == null ) {
		return "";
	}
	int len = format.length();
	char c;
	for ( int i = 0; i < len; i++ ) {
		c = format.charAt(i);
		if ( c == '%' ) {
			// Format modifier.  Get the next character.
			++i;
			if ( i >= len ) {
				break;
			}
			c = format.charAt ( i );
			if ( c == '%' ) {
				// Literal %.
				buffer.append ( c );
			}
			else if ( c == 'A' ) {
				// Alias from TSIdent.
				buffer.append ( _id.getAlias() );
			}
			else if ( c == 'b' ) {
				// Data interval base.
				buffer.append ( TimeInterval.getName( _id.getIntervalBase(),1) );
			}
			else if ( c == 'D' ) {
				// Description.
				buffer.append ( _description );
			}
			else if ( c == 'F' ) {
				// Full identifier.
				//buffer.append ( _id.getIdentifier() );
				buffer.append ( _id.toString() );
			}
			else if ( c == 'I' ) {
				// Full interval.
				buffer.append ( _id.getInterval() );
			}
	        else if ( c == 'i' ) {
	            // Input name.
	            buffer.append ( _id.getInputName() );
	        }
	        else if ( c == 'k' ) {
                // Sub source.
                buffer.append ( _id.getSubSource() );
            }
			else if ( c == 'L' ) {
				// Full location.
				buffer.append ( _id.getLocation() );
			}
			else if ( c == 'l' ) {
				// Main location.
				buffer.append ( _id.getMainLocation() );
			}
			else if ( c == 'm' ) {
				// Data interval multiplier.
				buffer.append ( _id.getIntervalMult() );
			}
			else if ( c == 'p' ) {
				// Period.
				buffer.append ( "" + _date1 + " - " + _date2 );
			}
			else if ( c == 'S' ) {
				// Full source.
				buffer.append ( _id.getSource() );
			}
			else if ( c == 's' ) {
				// Main source.
				buffer.append ( _id.getMainSource() );
			}
			else if ( c == 'U' ) {
				// Units.
				buffer.append ( _data_units );
			}
			else if ( c == 'T' ) {
				// Data type.
				buffer.append ( _id.getType() );
			}
			else if ( c == 't' ) {
				// Data main type (reserved for future use - for now return the total).
				buffer.append ( _id.getType() );
			}
			else if ( c == 'y' ) {
				// Data sub type (reserved for future use).
			}
			else if ( c == 'w' ) {
				// Sub-location.
				buffer.append ( _id.getSubLocation() );
			}
			else if ( c == 'x' ) {
				// Sub source.
				buffer.append ( _id.getSubSource() );
			}
			else if ( c == 'Z' ) {
				// Scenario.
				buffer.append ( _id.getScenario() );
			}
			else if ( c == 'z' ) {
				// Sequence ID (old sequence number).
				buffer.append ( _id.getSequenceID() );
			}
			else {
			    // No match.  Add the % and the character.
				buffer.append ( "%" );
				buffer.append ( c );
			}
		}
		else {
		    // Just add the character.
			buffer.append ( c );
		}
	}
	//Message.printStatus(2, routine, "After formatLegend(), string is \"" + s2 + "\"" );
    // Now replace ${ts:Property} strings with properties from the time series.
    // Put the most specific first so it is matched first.
    String startString = "${ts:";
    int startStringLength = 5;
    String endString = "}";
    Object propO;
    int start = 0; // Start at the beginning of the string.
    int pos2 = 0;
    String s2 = buffer.toString();
    while ( pos2 < s2.length() ) {
        int pos1 = StringUtil.indexOfIgnoreCase(s2, startString, start );
        if ( pos1 >= 0 ) {
            // Find the end of the property.
            pos2 = s2.indexOf( endString, pos1 );
            if ( pos2 > 0 ) {
                // Get the property.
                String propname = s2.substring(pos1+startStringLength,pos2);
                //Message.printStatus(2, routine, "Property=\"" + propname + "\" isTSProp=" + isTsProp + " pos1=" + pos1 + " pos2=" + pos2 );
                // By convention if the property is not found, keep the original string so can troubleshoot property issues.
                String propvalString = s2.substring(pos1,(pos2 + 1));
                // Get the property out of the time series.
                propO = getProperty(propname);
                if ( propO != null ) {
                    // This handles conversion of integers to strings.
                    propvalString = "" + propO;
                }
                // Replace the string and continue to evaluate s2
                s2 = s2.substring ( 0, pos1 ) + propvalString + s2.substring (pos2 + 1);
                // Next search will be at the end of the expanded string (end delimiter will be skipped in any case).
                start = pos1 + propvalString.length();
            }
            else {
                // No closing character so leave the property string as is and march on.
                start = pos1 + startStringLength;
                if ( start > s2.length() ) {
                    break;
                }
            }
        }
        else {
            // No more ${} property strings so done processing properties.
            // If checking time series properties will then check global properties in next loop
            break;
        }
    }
    if ( update_ts ) {
        setLegend ( buffer.toString() );
    }
	return s2;
}

/**
Format the time series into a general output format.
This method should be overridden in the derived class.
For example, all MonthTS should be have a general text report format.
The properties can be used to customize the output.
This method is meant as a general output format.
Specific formats should be implemented as classes with static readTimeSeries()/writeTimeSeries() methods.
@return list of strings containing formatted output.
@param props Modifiers for output.
@exception RTi.TS.TSException If low-level formatting code throws an exception.
*/
public List<String> formatOutput ( PropList props )
throws TSException {
	Message.printWarning( 3, "TS.formatOutput", "TS.formatOutput(PropList) is virtual, redefine in derived classes." );
	return null;
}

/**
Format the time series into a general output format and write to an output file.
This method should be overridden in the derived class.
@return Formatted output in a list, or null.
@param out PrintWriter to receive output.
@param props Modifiers for output.
@exception RTi.TS.TSException Thrown if low-level formatting code throws an exception.
*/
public List<String> formatOutput ( PrintWriter out, PropList props )
throws TSException {
	Message.printWarning( 3, "TS.formatOutput", "TS.formatOutput(" +
	"PrintWriter,PropList) is virtual, redefine in derived classes" );
	return null;
}

/**
Format the time series into a general output format.
This method should be overridden in the derived class.
@return List containing formatted output, or null.
@param out Name of the output file.
@param props Modifiers for output.
@exception RTi.TS.TSException Thrown if low-level formatting code throws an exception.
*/
public List<String> formatOutput ( String out, PropList props )
throws TSException {
	String routine="TS.formatOutput(string,int,long)";
	Message.printWarning( 1, routine, "TS.formatOutput(String,PropList)" +
	" function is virtual, redefine in derived classes" );
	return null;
}

/**
Return the time series alias from the TSIdent.
@return The alias part of the time series identifier.
*/
public String getAlias( ) {
	return _id.getAlias();
}

/**
 * Get an associated time series.
 * @param name name to use to look up the time series, similar to a property name
 * @return the associated time series matching the name
 */
public TS getAssociatedTimeSeries ( String name ) {
	return this.__associatedTimeSeries_HashMap.get(name);
}


/**
Return the time series comments.
@return The comments list.
*/
public List<String> getComments () {
	return _comments;
}

/**
Return the time series data flag meta-data list.
@return The data flag meta-data list.
*/
public List<TSDataFlagMetadata> getDataFlagMetadataList () {
    return __dataFlagMetadataList;
}

/**
Return the data interval base.
@return The data interval base (see TimeInterval.*).
*/
public int getDataIntervalBase() {
	return _data_interval_base;
}

/**
Return the original data interval base.
@return The data interval base of the original data.
*/
public int getDataIntervalBaseOriginal() {
	return _data_interval_base_original;
}

/**
Return the data interval multiplier.
@return The data interval multiplier.
*/
public int getDataIntervalMult() {
	return _data_interval_mult;
}

/**
Return the original data interval multiplier.
@return The data interval multiplier of the original data.
*/
public int getDataIntervalMultOriginal() {
	return _data_interval_mult_original;
}

/**
Return the time series data limits (a new copy is returned).
If necessary, the limits are refreshed.  The refresh() method should be defined in the derived class.
@return The time series data limits.
@see TSLimits
*/
public TSLimits getDataLimits () {
	// Make sure that the limits have been set.
	refresh();
	if ( _data_limits == null ) {
		return null;
	}
	else {
		// Create a new copy to protect.
	    return( new TSLimits(_data_limits) );
	}
}

/**
Return the original time series data limits.
The reference to the original data is returned so that MonthTSLimits or others can be returned.
@return The original time series data limits.
@see TSLimits
*/
public TSLimits getDataLimitsOriginal () {
	if ( _data_limits_original == null ) {
		return null;
	}
	else {
	    return ( _data_limits_original );
	}
}

/**
Return a TSData for a date.
This method should be defined in derived classes, especially if data flags are being used.
If the requested date/time is outside of the available data period,
the point should use the missing data value and an empty flag string.
@param date date/time to get data.
@param tsdata if null, a new instance of TSData will be returned.
If non-null, the provided instance will be used
(this is often desirable during iteration to decrease memory use and increase performance).
@return a TSData for the specified date/time.
*/
public TSData getDataPoint ( DateTime date, TSData tsdata ) {
	Message.printWarning( 3, "TS.getDataPoint", "This is a virtual function, redefine in child classes" );
	// Empty point.
	TSData data = new TSData();
	return data;
}

/**
Return the data precision.
@return The data precision.
*/
public short getDataPrecision() {
	return this.dataPrecision;
}

/**
 * Determine the output precision for a time series.
 * The internal precision for data is typically not limited.
 * However, the precision that is appropriate for output is typically limited.
 * The returned value is determined as follows:
 * 1. If time series data precision from 'getDataPrecision' is >= 0, use the value.
 * 2. If time series units are found in DataUnits, use the data units output precision.
 * 3. Use the default precision passed in.
 * 4. Return -1 if cannot be determined.
 *    The calling application will need to use an appropriate default.
 * @param defaultPrecision default precision to use if precision cannot be determined from time series
 * @return precision (number of digits after the decimal point) to use when formatting time series values
 */
public short getDataPrecision ( short defaultPrecision ) {
	if ( this.dataPrecision >= 0 ) {
		return this.dataPrecision;
	}
	if ( (this._data_units != null) && !this._data_units.isEmpty() ) {
		try {
	    	DataUnits u = DataUnits.lookupUnits ( this._data_units );
			return (short)u.getOutputPrecision();
		}
		catch ( Exception e ) {
			// No precision from units.
		}
	}
	// Default units.
	if ( defaultPrecision >= 0 ) {
		return defaultPrecision;
	}
	// Return -1 indicating precision not determined.
	return -1;
}

/**
Return the number of data points that are allocated in memory.
Zero will be returned if allocateDataSpace() has not been called.
Regular time series size involves knowing the data array size
whereas irregular time series overloads this method and returns the size of the TSData list.
@return The number of data points included in the period.
*/
public int getDataSize ( ) {
	return _data_size;
}

/**
Return the data type from the TSIdent or an empty string if no TSIdent has been set.
@return The data type abbreviation.
*/
public String getDataType( ) {
	if ( _id == null ) {
		return "";
	}
	else {
	    return _id.getType();
	}
}

/**
Return the data units.
@return The data units.
@see RTi.Util.IO.DataUnits
*/
public String getDataUnits( ) {
	return _data_units;
}

/**
Return the original data units.
@return The data units in the original data.
@see RTi.Util.IO.DataUnits
*/
public String getDataUnitsOriginal( ) {
	return _data_units_original;
}

/**
Return the data value for a date.
@return The data value associated with a date.
This should be overridden in derived classes (always returns the missing data value here).
@param date Date corresponding to the data value.
*/
public double getDataValue( DateTime date ) {
	Message.printWarning( 3, "TS.getDataValue", "TS.getDataValue is a virtual function, redefine in derived classes" );
	return _missing;
}

/**
Return the first date in the period of record (returns a copy).
@return The first date in the period of record, or null if the date is null.
*/
public DateTime getDate1() {
	if ( _date1 == null ) {
		return null;
	}
	return new DateTime ( _date1 );
}

/**
Return the first date in the original period of record (returns a copy).
@return The first date of the original data source
(generally equal to or earlier than the time series that is actually read), or null if the date is null.
*/
public DateTime getDate1Original() {
	if ( _date1_original == null ) {
		return null;
	}
	return new DateTime ( _date1_original);
}

/**
Return the last date in the period of record (returns a copy).
@return The last date in the period of record, or null if the date is null.
*/
public DateTime getDate2()
{	if ( _date2 == null ) {
		return null;
	}
	return new DateTime ( _date2 );
}

/**
Return the last date in the original period of record (returns a copy).
@return The last date of the original data source (generally equal to or
later than the time series that is actually read), or null if the date is null.
*/
public DateTime getDate2Original() {
	if ( _date2_original == null ) {
		return null;
	}
	return new DateTime ( _date2_original );
}

/**
Return the time series description.
@return The time series description.
*/
public String getDescription( ) {
	return _description;
}

/**
Return value of flag indicating whether time series is enabled.
@return true if the time series is enabled, false if disabled.
*/
public boolean getEnabled() {
	return _enabled;
}

/**
Return the extended time series legend.
@return Time series extended legend.
*/
public String getExtendedLegend() {
	return _extended_legend;
}

/**
Return the genesis information.
@return The genesis comments.
*/
public List<String> getGenesis () {
	return _genesis;
}

/**
Return the time series identifier as a TSIdent.
@return the time series identifier as a TSIdent.
@see TSIdent
*/
public TSIdent getIdentifier() {
	return _id;
}

/**
Return the time series identifier as a String.  This returns TSIdent.getIdentifier().
@return The time series identifier as a string.
@see TSIdent
*/
public String getIdentifierString() {
	return _id.getIdentifier();
}

/**
Return the input name (file or database table) for the time series.
@return the input name.
*/
public String getInputName () {
	return _input_name;
}

/**
Return whether data flag strings use String.intern().
@return True if data flag strings use String.intern(), false otherwise.
*/
public boolean getInternDataFlagStrings() {
    return _internDataFlagStrings;
}

/**
Return the time series legend.
@return Time series legend.
*/
public String getLegend() {
	return _legend;
}

/**
Return the location part of the time series identifier.  Does not include location type.
@return The location part of the time series identifier (from TSIdent).
*/
public String getLocation() {
	return _id.getLocation();
}

/**
Return the missing data value used for the time series (single value).
@return The value used for missing data.
*/
public double getMissing () {
	return _missing;
}

/**
Return the missing data range (2 values).
@return The range of values for missing data.
The first value is the lowest value, the second the highest.  A new array instance is returned.
*/
public double[] getMissingRange () {
	double [] missing_range = new double[2];
	missing_range[0] = _missingl;
	missing_range[1] = _missingu;
	return missing_range;
}

/**
Return the date for the first non-missing data value.
@return The first date where non-missing data occurs (a copy is returned).
*/
public DateTime getNonMissingDataDate1( ) {
	return new DateTime (_data_limits.getNonMissingDataDate1());
}

/**
Return the date for the last non-missing data value.
@return The last date where non-missing data occurs (a copy is returned).
*/
public DateTime getNonMissingDataDate2( ) {
	return new DateTime (_data_limits.getNonMissingDataDate2());
}

/**
Get the hashtable of properties, for example to allow display.
Only dynamic (not built-in) properties are returned.
@return the hashtable of properties, for example to allow display, guaranteed to not be null.
*/
public HashMap<String,Object> getProperties() {
    if ( __property_HashMap == null ) {
        __property_HashMap = new LinkedHashMap<String,Object>(); // Initialize to non-null for further use
    }
    return __property_HashMap;
}

/**
Get a time series property's contents (case-specific).
The surrounding ${ts:} used in TSTool should have been removed before calling.
The following built-in properties are checked in addition to dynamic properties
(case of name is ignored):
<ul>
<li>alias</li>
<li>description</li>
</ul>
@param propertyName name of property being retrieved.
@return property object corresponding to the property name.
*/
public Object getProperty ( String propertyName ) {
	Object propertyValue = null;
    if ( this.__property_HashMap != null ) {
    	// First check dynamic property.
    	propertyValue = this.__property_HashMap.get ( propertyName );
    }
    if ( propertyValue == null ) {
    	// Have not found the property from dynamic properties.
    	// Also check built-in properties - the surrounding ${ts:} should have been removed before call.
    	if ( propertyName.equalsIgnoreCase("alias") ) {
    		// Null is allowed.
    		propertyValue = this.getAlias();
    	}
    	else if ( propertyName.equalsIgnoreCase("description") ) {
    		// Null is allowed.
    		propertyValue = this.getDescription();
    	}
    }
    return propertyValue;
}

/**
Return the sequence identifier (old sequence number) for the time series.
@return The sequence identifier for the time series.
This is meant to be used when an array of time series traces is maintained.
@return time series trace ID.
*/
public String getSequenceID () {
    return _id.getSequenceID();
}

/**
Return the time series status.
@return The status flag for the time series.  This is a general purpose flag.
@see #setStatus
*/
public String getStatus ( ) {
	return _status;
}

// TODO SAM 2013-09-21 Need to move this out of this class to separate concerns.
/**
Returns the data in the specified DataFlavor, or null if no matching flavor exists.
From the Transferable interface.
Supported dataflavors are:<br>
</ul>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@param flavor the flavor in which to return the data.
@return the data in the specified DataFlavor, or null if no matching flavor exists.
*/
public Object getTransferData(DataFlavor flavor) {
	if (flavor.equals(tsFlavor)) {
		return this;
	}
	else if (flavor.equals(TSIdent.tsIdentFlavor)) {
		return _id;
	}
	else {
		return null;
	}
}

// TODO SAM 2013-09-21 Need to move this out of this class to separate concerns.
/**
Returns the flavors in which data can be transferred.  From the Transferable interface.
The order of the dataflavors that are returned are:<br>
<ul>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@return the flavors in which data can be transferred.
*/
public DataFlavor[] getTransferDataFlavors() {
	DataFlavor[] flavors = new DataFlavor[2];
	flavors[0] = tsFlavor;
	flavors[1] = TSIdent.tsIdentFlavor;
	return flavors;
}

/**
Return the time series input format version.
@return The time series version, to be used to indicate input file formats.
*/
public String getVersion () {
	return _version;
}

/**
Indicate whether the time series has data, which is determined by checking to
see whether memory has been allocated for the data space (which implies that the dates have been set).
This method can be checked after data are read rather than checking the dates.
This method should be defined in derived classes with specific data storage schemes.
@return true if the time series has data, false if not.
*/
public boolean hasData () {
	Message.printWarning( 1, "TS.getDataValue", "TS.hasData() is a virtual function, redefine in derived classes" );
	return false;
}

/**
Indicate whether the time series has data flags.
@return true if data flags are enabled for the time series.
*/
public boolean hasDataFlags () {
	return _has_data_flags;
}

// FIXME SAM 2010-08-20 Evaluate phasing this out.  setDataValue() now automatically turns on
// data flags when a flag is passed in.  Also, using intern strings improves memory management so
// allocating memory is not such a big deal as it was in the past.
/**
Set whether the time series has data flags.
This method should be called before allocateDataSpace() is called.
If data flags are enabled, allocateDataSpace() will allocate memory for the data and data flags.
@param hasDataFlags Indicates whether data flags will be associated with each data value.  The data flag is a String.
@param internDataFlagStrings if true, then String.intern() will be used to manage the data flags.
This generally is advised because flags often consist of the same strings used repeatedly.
However, if unique string values are used, then false can be specified so as to not bloat the global String table.
@return true if data flags are enabled for the time series, after the set.
*/
public boolean hasDataFlags ( boolean hasDataFlags, boolean internDataFlagStrings ) {
	_has_data_flags = hasDataFlags;
    _internDataFlagStrings = internDataFlagStrings;
	return _has_data_flags;
}

/**
Initialize data members.
*/
private void init( ) {
	_version = "";

	_input_name = "";

	// Need to initialize an empty TSIdent.

	_id = new TSIdent ();
	_legend = "";
	_extended_legend = "";
	_data_size = 0;
	// DateTime need to be initialized somehow.
    setDataType( "" );
	_data_interval_base = 0;
	_data_interval_mult = 1;
	_data_interval_base_original = 1;
	_data_interval_mult_original = 0;
	setDescription( "" );
	_comments = new ArrayList<>(2);
	_genesis = new ArrayList<>(2);
	setDataUnits( "" );
	setDataUnitsOriginal( "" );
	setMissing ( -999.0 );
	_data_limits = new TSLimits();
	_dirty = true;	// Need to recompute limits when we get the chance.
	_enabled = true;
	_selected = false;	// Let other code select, e.g., as query result.
	_editable = false;
}

/**
Determines whether the specified flavor is supported as a transfer flavor.
From the Transferable interface.
Supported dataflavors are:<br>
<ul>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@param flavor the flavor to check.
@return true if data can be transferred in the specified flavor, false if not.
*/
public boolean isDataFlavorSupported(DataFlavor flavor) {
	if (flavor.equals(tsFlavor)) {
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
Determine if a data value for the time series is missing.
The missing value can be set to a range of values or a single value, using setMissing().
There is no straightforward way to check to see if a value is equal to NaN
(the code: if ( value == Double.NaN ) will always return false if one or both values are NaN).
Consequently there is no way to see know if only one or both values is NaN, using the standard operators.
Instead, we assume that NaN should be interpreted as missing and do the check if ( value != value ),
which will return true if the value is NaN.  Consequently, code that uses time series
data should not check for missing and treat NaN differently because the TS class treats NaN as missing.
@return true if the data value is missing, false if not.
@param value Value to check.
*/
public boolean isDataMissing ( double value ) {
	if ( Double.isNaN(value) ) {
		return true;
	}
	if ( (value >= _missingl) && (value <= _missingu) ) {
		return true;
	}
	return false;
}

/**
Indicate whether the time series is dirty (data have been modified).
@return true if the time series is dirty, false if not.
*/
public boolean isDirty () {
	return _dirty;
}

/**
Indicate whether the time series is editable.
@return true if the time series is editable, false if not.
*/
public boolean isEditable () {
	return _editable;
}

/**
 * Indicate whether an irregular interval time series (always true).
 * Overriding this method in derived class is more foolproof in case the interval string has not been initialized.
 * @return true if an irregular interval time series, false if a regular interval time series
 */
public boolean isIrregularInterval () {
	// Attempt to determine from the interval.
	return TimeInterval.isIrregularInterval(getIdentifier().getInterval());
}

/**
 * Indicate whether a regular interval time series (always false).
 * Overriding this method in derived class is more foolproof in case the interval string has not been initialized.
 * @return true if a regular interval time series, false if an irregular interval time series
 */
public boolean isRegularInterval () {
	return TimeInterval.isRegularInterval(getIdentifier().getInterval());
}

/**
Indicate whether the time series is selected.
@return true if the time series is selected, false if not.
*/
public boolean isSelected() {
	return _selected;
}

/**
Create and return an iterator for the time series using the full period for the time series.
For regular interval time series, the iterator is TSIterator.  IrregularTS use the IrregularTSIterator.
@return an iterator for the time series.
@exception if the dates for the iterator are not set (they are not set in the time series).
*/
public TSIterator iterator ()
throws Exception {
	return new TSIterator ( this );
}

/**
Return an iterator for the time series using the specified period.
For regular interval time series, the iterator is that same. IrregularTS use the IrregularTSIterator.
@param date1 Start of data iteration.  If null, the first date/time will be used.
@param date2 End of data iteration.  If null, the last date/time will be used.
@return an iterator for the time series.
@exception if the dates for the iterator are not set (they are not set in the time series).
*/
public TSIterator iterator ( DateTime date1, DateTime date2 )
throws Exception {
	return new TSIterator ( this, date1, date2 );
}

/**
Refresh the secondary data (e.g., data limits).  This should be overruled in the derived class.
*/
public void refresh () {
	Message.printWarning ( 3, "TS.refresh", "TS.refresh is virtual.  Define in the derived class!" );
}

/**
Set the time series identifier alias.
@param alias Alias of time series.
*/
public void setAlias ( String alias ) {
	if ( alias != null ) {
		_id.setAlias( alias );
	}
}

/**
 * Set an associated time series.
 * @param name name to use for the time series, similar to a property name
 * @param ts associated time series to set
 */
public void setAssociatedTimeSeries ( String name, TS ts ) {
	this.__associatedTimeSeries_HashMap.put(name, ts);
}

/**
Set the comments string list.
@param comments Comments to set.
*/
public void setComments ( List<String> comments ) {
	if ( comments != null ) {
		_comments = comments;
	}
}

/**
Set the data interval.
@param base Base interval (see TimeInterval.*).
@param mult Base interval multiplier.
*/
public void setDataInterval ( int base, int mult ) {
	_data_interval_base = base;
	_data_interval_mult = mult;
}

/**
Set the data interval for the original data.
@param base Base interval (see TimeInterval.*).
@param mult Base interval multiplier.
*/
public void setDataIntervalOriginal ( int base, int mult ) {
	_data_interval_base_original = base;
	_data_interval_mult_original = mult;
}

/**
Set the data limits for the time series.
This is generally only called by internal routines.  A copy is saved.
@param limits Time series limits.
@see TSLimits
*/
public void setDataLimits( TSLimits limits ) {
	_data_limits = new TSLimits ( limits );
}

/**
Set the original data limits for the time series.
This is currently typically only called by application code (like TSTool).
The reference to the limits are saved so that derived class data can be stored and returned with getDataLimitsOriginal().
Make a copy of the data if it needs to be protected.
@param limits Time series data limits.
@see TSLimits
*/
public void setDataLimitsOriginal ( TSLimits limits ) {
	if ( limits == null ) {
		_data_limits_original = null;
	}
	_data_limits_original = limits;
}

/**
Set the number of precision of data (digits after period),
if set will override precision determined from the data units.
@param dataPrecision Number of data points in the time series.
*/
public void setDataPrecision ( short dataPrecision ) {
	this.dataPrecision = dataPrecision;
}

/**
Set the number of data points including the full period.  This should be called by refresh().
@param data_size Number of data points in the time series.
*/
protected void setDataSize ( int data_size ) {
	_data_size = data_size;
}

/**
Set the data type.
@param data_type Data type abbreviation.
*/
public void setDataType( String data_type ) {
	if ( (data_type != null) && (_id != null) ) {
		_id.setType ( data_type );
	}
}

/**
Set the data units.
@param data_units Data units abbreviation.
@see RTi.Util.IO.DataUnits
*/
public void setDataUnits( String data_units ) {
	if ( data_units != null ) {
		_data_units = data_units;
	}
}

/**
Set the data units for the original data.
@param units Data units abbreviation.
@see RTi.Util.IO.DataUnits
*/
public void setDataUnitsOriginal( String units ) {
	if ( units != null ) {
		_data_units_original = units;
	}
}

/**
Set a data value for the specified date.
@param date Date of interest.
@param val Data value for date.
@see RTi.Util.Time.DateTime
@param return the number of values set, 0 or 1, useful to know when a value is outside the allocated period
*/
public int setDataValue ( DateTime date, double val ) {
	Message.printWarning ( 1, "TS.setDataValue", "TS.setDataValue is virtual and should be redefined in derived classes" );
	return 0;
}

// TODO SAM 2010-08-03 if flag is null, should it be treated as empty string?  What about append?
/**
Set a data value and associated information for the specified date.  This method
should be defined in derived classes.
@param date Date of interest.
@param val Data value for date.
@param data_flag Data flag associated with the data value.
@param duration Duration (seconds) for the data value (specify as 0 if not relevant).
@see DateTime
@param return the number of values set, 0 or 1, useful to know when a value is outside the allocated period
*/
public int setDataValue ( DateTime date, double val, String data_flag,	int duration ) {
	Message.printWarning ( 3, "TS.setDataValue", "TS.setDataValue is virtual and should be implemented in derived classes" );
	return 0;
}

/**
Set the first date in the period.  A copy is made.
The date precision is set to the precision appropriate for the time series.
@param t First date in period.
@see DateTime
*/
public void setDate1 ( DateTime t ) {
	if ( t != null ) {
		_date1 = new DateTime ( t );
		if ( _data_interval_base != TimeInterval.IRREGULAR ) {
		    // For irregular, rely on the DateTime precision.
		    _date1.setPrecision ( _data_interval_base );
		}
	}
}

/**
Set the first date in the period in the original data.  A copy is made.
The date precision is set to the precision appropriate for the time series.
@param t First date in period in the original data.
@see DateTime
*/
public void setDate1Original( DateTime t ) {
	if ( t != null ) {
		_date1_original = new DateTime ( t );
		if ( _data_interval_base != TimeInterval.IRREGULAR ) {
            // For irregular, rely on the DateTime precision.
		    _date1_original.setPrecision ( _data_interval_base );
		}
	}
}

/**
Set the last date in the period.  A copy is made.
The date precision is set to the precision appropriate for the time series.
@param t Last date in period.
@see DateTime
*/
public void setDate2 ( DateTime t ) {
	if ( t != null ) {
		_date2 = new DateTime ( t );
		if ( _data_interval_base != TimeInterval.IRREGULAR ) {
            // For irregular, rely on the DateTime precision.
		    _date2.setPrecision ( _data_interval_base );
		}
	}
}

/**
Set the last date in the period in the original data. A copy is made.
The date precision is set to the precision appropriate for the time series.
@param t Last date in period in the original data.
@see DateTime
*/
public void setDate2Original( DateTime t ) {
	if ( t != null ) {
		_date2_original = new DateTime ( t );
		if ( _data_interval_base != TimeInterval.IRREGULAR ) {
            // For irregular, rely on the DateTime precision.
		    _date2_original.setPrecision ( _data_interval_base );
		}
	}
}

/**
Set the description.
@param description Time series description (this is not the comments).
*/
public void setDescription( String description ) {
	if ( description != null ) {
		_description = description;
	}
}

/**
Set whether the time series is dirty (data have been modified).
@param dirty true if the time series is dirty/edited, false if not.
*/
public void setDirty ( boolean dirty ) {
	_dirty = dirty;
}

/**
Set whether the time series is editable.
@param editable true if the time series is editable.
*/
public void setEditable ( boolean editable ) {
	_editable = editable;
}

/**
Indicate whether the time series is enabled.
This is being phased in and is currently only used by graphics code.
@param enabled true if the time series is enabled or false if disabled.
*/
public void setEnabled ( boolean enabled ) {
	_enabled = enabled;
}

/**
Set the time series extended legend.  This was used at one point to set a
secondary legend because the Java plotting package did not allow a long legend in the graph.
Currently, this data item is being used to set the legend for the table time series view.
@param extended_legend Time series extended legend (can be used for labels on graphs, etc.).
@see #formatExtendedLegend
*/
public void setExtendedLegend ( String extended_legend ) {
	if ( extended_legend != null ) {
		_extended_legend = extended_legend.trim();
	}
}

/**
Set the genesis information.  The original is lost.
@param genesis Genesis comments.
*/
public void setGenesis ( List<String> genesis ) {
	setGenesis ( genesis, false );
}

/**
Set the genesis information.
@param genesis Genesis comments.
@param append Indicates whether genesis information should be appended.
*/
public void setGenesis ( List<String> genesis, boolean append ) {
	if ( !append ) {
		// Don't call removeAllElements() because the genesis may have
		// been retrieved and then reset using the same list.
		_genesis = new ArrayList<>();
	}
	_genesis = StringUtil.addListToStringList ( _genesis, genesis );
}

/**
Set the time series identifier using a TSIdent.
Note that this only sets the identifier but does not set the separate data fields (like data type).
@param id Time series identifier.
@see TSIdent
@exception Exception If there is an error setting the identifier.
*/
public void setIdentifier ( TSIdent id )
throws Exception {
	if ( id != null ) {
		_id = new TSIdent ( id );
	}
}

/**
Set the time series identifier using a string.
Note that this only sets the identifier but does not set the separate data fields (like data type).
@param identifier Time series identifier.
@exception Exception If there is an error setting the identifier.
*/
public void setIdentifier( String identifier )
throws Exception {
	if ( identifier != null ) {
		_id.setIdentifier( identifier );
	}
}

/**
Set the time series identifier using a individual string parts.
@param location Location of the time series.
@param source Source of the data.
@param type Data type.
@param interval Data interval, including base and possibly multiplier.
@param scenario Scenario for data.
@exception Exception If there is an error setting the identifier (e.g., interval is not recognized).
*/
public void setIdentifier( String location, String source, String type, String interval, String scenario )
throws Exception {
	_id.setLocation( location );
	_id.setSource( source );
	_id.setType( type );
	_id.setInterval( interval );
	_id.setScenario( scenario );
}

/**
Set the input name (file or database table).
*/
public void setInputName ( String input_name ) {
	if ( input_name != null ) {
		_input_name = input_name;
	}
}

/**
Set the time series legend.
@param legend Time series legend (can be used for labels on graphs, etc.).
@see #formatLegend
*/
public void setLegend ( String legend ) {
	if ( legend != null ) {
		_legend = legend.trim();
	}
}

/**
Set the time series identifier location.
@param location Location of time series.
*/
public void setLocation ( String location ) {
	if ( location != null ) {
		_id.setLocation( location );
	}
}

/**
Set the missing data value for the time series.
The upper and lower bounds of missing data are set to this value +.001 and -.001, to allow for precision truncation.
The value is constrained to Double.MAX and Double.Min.
@param missing Missing data value for time series.
*/
public void setMissing ( double missing ) {
	_missing = missing;
	if ( Double.isNaN(missing) ) {
		// Set the bounding limits also just to make sure that values like -999 are not treated as missing.
	    _missingl = Double.NaN;
	    _missingu = Double.NaN;
		return;
	}
	if ( missing == Double.MAX_VALUE ) {
	    _missingl = missing - .001;
        _missingu = missing;
	}
	else {
	    // Set a range on the missing value check that is slightly on each side of the value.
        _missingl = missing - .001;
        _missingu = missing + .001;
	}
}

/**
Set the missing data range for the time series.
The value returned from getMissing() is computed as the average of the values.
Two values must be specified, neither of which can be a NaN.
@param missing Missing data range for time series.
*/
public void setMissingRange ( double [] missing ) {
	if ( missing == null ) {
		return;
	}
	if ( missing.length != 2 ) {
		return;
	}
	_missing = (missing[0] + missing[1])/2.0;
	if ( missing[0] < missing[1] ) {
		_missingl = missing[0];
		_missingu = missing[1];
	}
	else {
	    _missingl = missing[1];
		_missingu = missing[0];
	}
}

/**
Set the date for the first non-missing data value.
@param t Date for the first non-missing data value.
@see DateTime
*/
public void setNonMissingDataDate1( DateTime t ) {
	if ( t != null ) {
		_data_limits.setNonMissingDataDate1 ( t );
	}
}

/**
Set the date for the last non-missing data value.
@param t Date for the last non-missing data value.
@see DateTime
*/
public void setNonMissingDataDate2( DateTime t ) {
	if ( t != null ) {
		_data_limits.setNonMissingDataDate2( t );
	}
}

/**
Set a time series property's contents (case-specific).
@param propertyName name of property being set.
@param property property object corresponding to the property name.
*/
public void setProperty ( String propertyName, Object property ) {
    if ( __property_HashMap == null ) {
        __property_HashMap = new LinkedHashMap<String, Object>();
    }
    __property_HashMap.put ( propertyName, property );
}

/**
Indicate whether the time series is selected.
This is used by applications that are working on a list of time series.
*/
public void setSelected ( boolean selected ) {
	_selected = selected;
}

/**
Set the sequence identifier (old sequence number), used with ensembles.
@param sequenceID sequence identifier for the time series.
*/
public void setSequenceID ( String sequenceID ) {
	_id.setSequenceID ( sequenceID );
}

/**
Set the time series identifier source.
@param source Source for time series.
*/
public void setSource ( String source ) {
	if ( source != null ) {
		_id.setSource ( source );
	}
}

/**
Set the status flag for the time series.
This is used by high-level code when manipulating time series.
For example, a list of time series might be passed to a routine for graphing.
Additionally, another display component may list an extended legend.
The status allows the first component to disable some time series because of
incompatibility so the second component can detect.
This feature may be phased out in the future.
@see #getStatus
*/
public void setStatus ( String status ) {
	if ( status != null ) {
		_status = status;
	}
}

/**
Set the time series version, to be used with input file formats.
@param version Version number for time series file.
*/
public void setVersion( String version ) {
	if ( version != null ) {
		_version = version;
	}
}

}