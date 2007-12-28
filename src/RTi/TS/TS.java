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
// In general, the majority of the data members will be used for all derived
// time series.  However, some will not be appropriate.  The decision has been
// made to keep them here to make it less work to manage the first layer of
// derived classes.  For example, the irregular time series does not need to
// store _interval_mult.  Most of the other data still apply.
//
// Because this is a base class, anything derived from the class can only be as
// specific as the base class.  Therefore, routines to do conversions between
// time series types will have to be done outside of this class library.  Using
// the TS base class allows polymorphism when used by complicated objects.
//
// It is assumed that the first layer will define the best way to store the
// data.  In other words, different data intervals will have different storage
// methods.  The goal is not to conceptualize time series so much that they all
// consist of an array of date and data values.  For constant interval time
// series, the best performance will be to store the values in an ordered array
// that is indexed by month, etc.  If this assumption is wrong, then maybe the
// middle layer gets folded into the base class.
//
// It may be desirable to build some streaming control into this class.  For
// example, read multiple time series from the same file (for certain time
// series types) or may want to read one block of data from multiple files
// (e.g., when running a memory-sensitive application that only needs to have
// in memory one month of data for every time series in the system and reuses
// that space).  For now assume that we will always read the entire time series
// in but be aware that more control may be added later.
//
// New code does not have 3 layers.  Instead, I/O classes should have static
// methods like readTimeSeries, writeTimeSeries that operate on time series
// instances.  See DateValueTS for an example.  Older code may not follow this
// approach.
// ----------------------------------------------------------------------------
// History:
//
// Apr, May 96	Steven A. Malers, RTi	Start developing the library based on
//					the C version of the TS library.
// 10 Sep 96	SAM, RTi		Formalize the class enough so that we
//					can begin to use with the Operation
//					class to work on the DSS.
// 05 Feb 97	SAM, RTi		Add allocateDataSpace and setDataValue
//					virtual functions.  Add _dirty
//					so that we know when data has been
//					set (to indicate that we need to redo
//					calcMaxMin).  Add getDataPosition and
//					freeDataSpace.
// 26 May 97	SAM, RTi		Add writePersistentHeader.  To increase
//					performance in derived classes, make
//					more data members protected.  Also add
//					_data_date1 and _data_date2 to hold the
//					dates where we actually have data.
// 06 Jun 1997	SAM, RTi		Add a third position argument to
//					getDataPosition to work with the
//					MinuteTS data.  Other intervals will not
//					use the 2nd or 3rd positions.
//					Add TSIntervalFromString.
// 16 Jun 1997	MJR, RTi		Overloaded calcMaxMinValues to 
//					find and return max and min between
//					two dates that are passed in.
// 03 Nov 1997  Daniel Weiler, RTi	Added GetPeriodFromTS function
// 26 Nov 1997	SAM, DKW, RTi		Add getValidPeriod.
// 14 Dec 1997	SAM, RTi		Add copyHeader.
// 06 Jan 1998	SAM, RTi		Update to use getDataLimits,
//					setDataLimits, refresh(), and change
//					data limits to _data_limits.  Put all
//					but the original dates and overal date
//					limits in _data_limits.
//					Add _sequence_number.
// 22 Feb 1998	SAM, RTi		Add _data_size to hold the total number
//					of elements allocated for data.
// 31 Mar 1998	SAM, DLG, RTi		Add _legend for use with output plots,
//					reports, etc.
// 28 Apr 1998	SAM, RTi		Add _status to allow general use by
//					other programs (e.g., to indicate that
//					the TS should not be used in a later
//					computation).
// 01 May 1998	SAM, RTi		Add _extended_legend for use with output
//					reports, etc.  Change so that
//					setComments resets the comments.
// 07 May 1998	SAM, RTi		Add formatHeader.
// 13 Jul 1998	SAM, RTi		Update copyHeader documentation.
// 23 Jul 1998	SAM, RTi		Add changePeriodOfRecord as "virtual"
//					function.  This needs to be implemented
//					at the storage level (next level of
//					extension).
// 06 Aug 1998	SAM, RTi		Remove getDataPosition, getDataPointer
//					from this class.  Those routines are
//					often not needed and should be private
//					to the derived classes.
// 20 Aug 1998	SAM, RTi		OK, realized that getDataPosition is
//					valuable for derived classes, but change
//					to return an array of integers with the
//					positions.  Make class abstract to
//					pass compile with 1.2.
// 18 Nov 1998	SAM, RTi		Add copyData method.
// 11 Jan 1998	SAM, RTi		Add routine name to virtual functions
//					so we can track down problems.
// 13 Apr 1999	SAM, RTi		Add finalize.  Add genesis format flag.
//					Change so addToGenesis does not
//					include routine name.
// 28 Jan 2000	SAM, RTi		Add setMissingRange() to allow handling
//					of -999 and -998 missing data values
//					in NWS work.
// 11 Oct 2000	SAM, RTi		Add iterator(), getDataPoint().
// 16 Oct 2000	SAM, RTi		Add _enabled, _selected, and _plot*
//					data to work with visualization.
// 13 Nov 2000	SAM, RTi		copyHeader() was not copying the
//					interval base.
// 20 Dec 2000	SAM, RTi		Add _data_limits_original, which is
//					currently just a convenience for code
//					(like tstool) so the original data
//					limits can be saved for use with
//					filling.  This may actually be a good
//					way to compare before and after data
//					statistics.  This data item is not a
//					copy of the limits (whereas the current
//					data limits object is a copy - the old
//					convention may be problematic).
// 28 Jan 2001	SAM, RTi		Update javadoc to not rely on @return.
//					Add checks for null strings when adding
//					to comments/genesis.  Add
//					allocateDataSpace() that takes period,
//					consistent with C++.  Make sure methods
//					are alphabetized.  Change so setDate*
//					methods set the precision of the date
//					to be appropriate for the time series
//					interval.
// 21 Feb 2001	SAM, RTi		Implement clone().  Add getInputName()
//					and setInputName().
// 31 May 2001	SAM, RTi		Use TSDate.setPrecision(TS) to ensure
//					start and end dates are the correct
//					precision in set*Date() methods.
// 28 Aug 2001	SAM, RTi		Fix the clone() method to be a deep
//					copy.
// 2001-11-05	SAM, RTi		Full review of javadoc.  Verify that
//					variables are set to null when no longer
//					used.  Change methods to have return
//					type of void where appropriate.
//					Change calculateDataSize() to be a
//					static method.  Remove the deprecated
//					readPersistent(), writePersistent()
//					methods.
// 2002-01-21	SAM, RTi		Remove the plot data.  This is now
//					handled in the TSGraph* code.  By
//					removing here, we decouple the plot
//					properties from the TS, eliminating
//					problems.
// 2002-01-30	SAM, RTi		Add _has_data_flags, _data_flag_length,
//					hasDataFlags(), and setDataValue(with
//					data flag and duration) to support data
//					flags and duration.  Flags should be
//					returned by using the getDataPoint()
//					method in derived classes.  Remove the
//					input stream flags and data - this has
//					never been used.  Fix copyHeader() to
//					do a deep copy on the TSIdent.
// 2002-02-17	SAM, RTi		Change the sequence number initial value
//					to -1.
// 2002-04-17	SAM, RTi		Update setGenesis() to have append flag.
// 2002-04-23	SAM, RTi		Deprecated getSelected() and
//					setSelected() in favor of isSelected().
//					Add %z to formatLegend() to use the
//					sequence number.
// 2002-06-03	SAM, RTi		Add support for NaN as missing data
//					value.
// 2002-06-16	SAM, RTi		Add isDirty() to help IrregularTS.
// 2002-08-12	J. Thomas Sapienza, RTi	Added calcDataDate for use with JTable
//					models.
// 2002-09-04	SAM, RTi		Remove calcDataDate() - same effect
//					can occur by a call to a TSDate.
//					Add getDataFlagLength() to allow
//					DateValueTS to output the flags.
//					Update javadoc to explain that
//					allocateDataSpace() and
//					changePeriodOfRecord() should handle the
//					data flag - previously hasDataFlag.
// 2002-11-25	SAM, RTi		Change getDate*() methods to return null
//					if the requested data are null.
// 2003-01-08	SAM, RTi		Add a hasData() method to indicate
//					whether the time series has data.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TSUnits to DataUnits.
//					* Remove INTERVAL_* - TS package classes
//					  should use TimeInterval.* instead.
//					* Remove _date_type data member and
//					  associated DATES_* - they have never
//					  been used.
//					* Remove FORMAT_ since other classes
//					  handle formatting themselves.
//					* Remove the _data_type because it is
//					  stored in the TSIdent.
// 2003-07-24	SAM, RTi		* Fully remove commented out code for
//					  getDataDate() - it is not used by
//					  TSIterator any more and no other code
//					  uses.
//					* TSIterator constructor now throws an
//					  exception so declare throws here.
// 2003-08-21	SAM, RTi		* Change isSelected(boolean) back to
//					  setSelected() - ARG!
//					* Add isEditable() and setEditable().
//					* Remove deprecated constructor to take
//					  a String (filename) - extended classes
//					  should handle I/O.
//					* Remove deprecated addToGenesis() that
//					  took a routine name.
//					* Remove deprecated INFINITY - not used.
// 2004-01-28	SAM, RTi		* Change wording in text format header
//					  to more clearly identify original and
//					  current data period.
// 2004-03-04	J. Thomas Sapienza, RTi	* Class now implements serializable.
//					* Class now implements transferable.
// 2004-04-14	SAM, RTi		* Fix so that when setting the dates
//					  the original time zone precision
//					  information is not clobbered.
// 2004-11-23	SAM, RTi		* Move the sequence number to TSIdent
//					  since it is now part of the TSID.
//					* In formatLegend(), use instance data
//					  members instead of calling get()
//					  methods - performance increases.
// 2005-05-12	SAM, RTi		* Add allocateDataFlagSpace() to support
//					  enabling data flags after the initial
//					  allocation.
// 2006-10-03	SAM, RTi		* Add %p to formatLegend() for period.
// 2006-11-22	SAM, RTi		Fix but in addToComments() where the
//					wrong Vector was being used.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.String;
import java.util.Vector;

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
This class is the base class for all time series classes.  The code is written
to be similar to the C++ version of the TS library.  Consequently, this class
provides "virtual" functions that are meant to be overridden (in Java these
methods are just included here but are expected to be overridden - an interface
has not been implemented to force this behavior because the methods are not
"pure virtual").
*/
public class TS implements Cloneable, Serializable, Transferable
{

// FIXME SAM 2007-12-13 Need to move transfer objects to wrapper around this class.
/**
Data flavor for transferring this object.
*/
public static DataFlavor tsFlavor = new DataFlavor(RTi.TS.TS.class,"RTi.TS.TS");

/**
General string to use for status of the time series (use as appropriate by
high-level code).  This value is volatile - do not assume its value will remain
for long periods.  This value is not used much now that the GRTS package has
been updated.
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
For example, this may be used to indicate the period in a database, which is
different than the period that was actually queried and saved in memory.
*/
protected DateTime _date1_original;

/**
Original ending date/time for data, at a precision appropriate for the data.
For example, this may be used to indicate the period in a database, which is
different than the period that was actually queried and saved in memory.
*/
protected DateTime _date2_original;

/**
The data interval base.  See TimeInterval.HOUR, etc.
*/
protected int _data_interval_base;	

/**
The base interval multiplier (what to multiply _interval_base by to get the
real interval).  For example 15-minute data would have
_interval_base = TimeInterval.MINUTE and _interval_mult = 15.
*/
protected int _data_interval_mult;

/**
The data interval in the original data source (for example, the source may be
in days but the current time series is in months).
*/
protected int _data_interval_base_original;

/**
The data interval multiplier in the original data source.
*/
protected int _data_interval_mult_original;

/**
Number of data values inclusive of _date1 and _date2.  Set in the
allocateDataSpace() method.  This is useful for general information.
*/
protected int _data_size;	

/**
Data units.  A list of units and conversions is typically maintained in the
DataUnits* classes.
*/
protected String _data_units;

/**
Units in the original data source (e.g., the current data may be in CFS and the
original data were in CMS).
*/
protected String _data_units_original;

/**
The length of each data flag char[] (the maximum number of flag characters).
This should be set in the hasDataFlags(boolean,int) method.
*/
protected int _data_flag_length = 0;

/**
Indicates whether data flags are being used with data.  If enabled, the derived
classes that store data should override the allocateDataSpace(boolean, int)
method to create a data array to track the data flags.  It is recommended to
save space that the flags be treated as char[] data, padded with null
characters.
*/
protected boolean _has_data_flags = false;

// FIXME SAM 2007-12-13 Need to phase this out in favor of handling in DAO code.
/**
Version of the data format (mainly for use with files).
*/
protected String _version;

// FIXME SAM 2007-12-13 Need to evaluate renaming to avoid confusion with TSTIdent input name.
// Implementing a DataSource concept for input/output may help (but also have data source in
// TSIdent!).
/**
Input source information.  Filename if read from file or perhaps a database
name and table (e.g., HydroBase.daily_flow).  This is the actual location read,
which should not be confused with the TSIdent storage name (which may not be
fully expanded).
*/
protected String _input_name;	

/**
Time series identifier, which provides a unique and absolute handle on the time series.
An alias is provided within the TSIdent class.
*/
protected TSIdent _id;

/**
Indicates whether the time series data have been modified by calling
setDataValue().  Call refresh() to update the limits.  This is not used with
header data.
*/
protected boolean _dirty;

/**
Indicates whether the time series is editable.  This primarily applies to the
data (not the header information).  UI components can check to verify whether
users should be able to edit the time series.  It is not intended to be checked
by low-level code (manipulation is always granted).
*/
protected boolean _editable = false;

/**
A short description (e.g, "XYZ gage at ABC river").
*/
protected String _description;

/**
Comments that describe the data.  This can be anything from an original data
source.  Sometimes the comments are created on the fly to generate a standard
header (e.g., describe drainage area).
*/
protected Vector _comments;

/**
History of time series.  This is not the same as the comments but instead
chronicles how the time series is manipulated in memory.  For example the first
genesis note may be about how the time series was read.  The second may
indicate how it was filled.  Many TSUtil methods add to the genesis.
*/
protected Vector _genesis;

// TODO SAM 2007-12-13 Evaluate moving to NaN as a default.
/**
The missing data value.  Default is -999.0.
*/
protected double _missing;

/**
Lower bound on the missing data value (for quick comparisons and when missing
data ranges are used).
*/
protected double _missingl;

/**
Upper bound on the missing data value (for quick comparisons and when missing
data ranges are used).
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
Legend to show when plotting or tabulating a time series.  This is generally a
short legend.
*/
protected String _legend;

// TODO SAM 2007-12-13 Evaluate need now that GRTS is available.
/**
Legend to show when plotting or tabulating a time series.  This is usually a
long legend.  This may be phased out now that the GRTS package has been phased
in for visualization.
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
Construct a time series and initialize the member data.  Derived classes should
set the _data_interval_base.
*/
public TS ()
{	if ( Message.isDebugOn ) {
		Message.printDebug( 50, "TS.TS", "Constructing" );
	}
	init();
}

/**
Copy constructor.  Only the header is copied since derived classes should
copy the data.
@see #copyHeader
*/
public TS ( TS ts )
{	if ( this == null ) {
		return;
	}
	copyHeader ( this );
}

/**
Add a String to the comments associated with the time series (e.g., station remarks).
@param comment Comment string to add.
*/
public void addToComments( String comment )
{	if ( comment != null ) {
		_comments.addElement ( comment );
	}
}

/**
Add Vector of String to the comments associated with the time series (e.g.,
station remarks).
@param comments Comments strings to add.
*/
public void addToComments( Vector comments )
{	if ( comments == null ) {
		return;
	}
	String comment = null;
	int size = comments.size();
	for ( int i = 0; i < size; i++ ) {
		comment = (String)comments.elementAt(i);
		if ( comment != null ) {
			_comments.addElement ( comment );
		}
	}
	comment = null;
}

/**
Add a string to the genesis string list.  The genesis is a list of comments
indicating how the time series was read and manipulated.  Genesis information
should be added by methods that, for example, fill data and change the period.
@param genesis Comment string to add to genesis information.
*/
public void addToGenesis ( String genesis )
{	if ( genesis != null ) {
		_genesis.addElement ( genesis );
	}
}

/**
Allocate the data flag space for the time series.  This requires that the data
interval base and multiplier are set correctly and that _date1 and _date2 have
been set.  The allocateDataSpace() method will allocate the data flags if
appropriate.  Use this method when the data flags need to be allocated after
the initial allocation.  This method is meant to be overridden in derived
classes (e.g., MinuteTS, MonthTS) that are optimized for data storage for
different intervals.
@param data_flag_length Maximum length of data flags.  If the data flags array
is already allocated, then the flag size will be increased by the specified
length.  This allows multiple flags to be concatenated.
@param initial_value Initial value (null is allowed and will result in the
flags being initialized to spaces).
@param retain_previous_values If true, the array size will be increased if
necessary, but
previous data values will be retained.  If false, the array will be reallocated
and initialized to spaces.
@exception Exception if there is an error allocating the memory.
*/
public void allocateDataFlagSpace (	int data_flag_length,
					String initial_value,
					boolean retain_previous_values )
throws Exception
{	Message.printWarning ( 1, "TS.allocateDataFlagSpace", 
	"TS.allocateDataFlagSpace() is virtual, define in derived classes." );
}

/**
Allocate the data space for the time series.  This requires that the data
interval base and multiplier are set correctly and that _date1 and _date2 have
been set.  If data flags are used, hasDataFlags() should also be called before
calling this method.  This method is meant to be overridden in derived classes
(e.g., MinuteTS, MonthTS) that are optimized for data storage for different
intervals.
@return 0 if successful allocating memory, non-zero if failure.
*/
public int allocateDataSpace ( )
{	Message.printWarning ( 1, "TS.allocateDataSpace", 
	"TS.allocateDataSpace() is virtual, define in derived classes." );
	return 1;
}

/**
Allocate the data space for the time series using given dates.  This requires
that the data interval base and multiplier are set correctly in the derived
class (the allocateDataSpace(void) method of the derived class will be called).
@param date1 Start of period.
@param date2 End of period.
@return 1 if there is an error allocating the data space, 0 if success.
*/
public int allocateDataSpace ( DateTime date1, DateTime date2 )
{	//Set data
        setDate1 ( date1 );
        setDate1Original ( date1 );
        setDate2 ( date2 );
        setDate2Original ( date2 );

        // Now allocate memory for the new data space...
	if ( allocateDataSpace() != 0 ) {
		Message.printWarning ( 2,
		"TS.allocateDataSpace(DateTime,DateTime)",
		"Error allocating data space for " + date1.toString() + " to " +
		date2.toString() );
		return 1;
	}
	return 0;
}

/**
Calculate the data size (number of data points) for a time series, given the
interval multiplier and start and end dates.  This method is meant to be
overridden in derived classes (in which case the interval base will be known).
@param date1 Start of period.
@param date2 End of period.
@param multiplier Multiplier for interval base.
@return the number of data points in a period, given the interval mulitplier.
*/
public static int calculateDataSize (	DateTime date1, DateTime date2,
					int multiplier )
{	Message.printWarning ( 1, "TS.calculateDataSize", 
	"TS.calculateDataSize() is virtual, define in derived classes." );
	return 0;
}

/**
Change the period of record for the data.  If the period is lengthened, fill
with missing data.  If shortened, truncate the data.  This method should be
implemented in a derived class and should handle resizing of the data space
and data flag array.
@param date1 Start date for new period.
@param date2 End date for new period.
@exception RTi.TS.TSException if this method is called in the base class (or if
an error in the derived class).
*/
public void changePeriodOfRecord ( DateTime date1, DateTime date2 )
throws TSException
{	String routine = "TS.changePeriodOfRecord";

	Message.printWarning ( 1, routine, "TS.changePeriodOfRecord is a " +
	"virtual function, redefine in derived classes" );

	throw new TSException ( routine +
	": changePeriodOfRecord needs to be implemented in derived class!" );
}

/**
Clone the object.  The Object base class clone() method is called and then the
TS objects are cloned.  The result is a complete deep copy.
*/
public Object clone ()
{	try {	// Clone the base class...
		TS ts = (TS)super.clone();
		// Now clone mutable objects..
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
			ts._comments = (Vector)_comments.clone();
			size = _comments.size();
			for ( i = 0; i < size; i++ ) {
				ts._comments.setElementAt(
					(String)_comments.elementAt(i),i);
			}
		}
		if ( _genesis != null ) {
			ts._genesis = (Vector)_genesis.clone();
			size = _genesis.size();
			for ( i = 0; i < size; i++ ) {
				ts._genesis.setElementAt(
					(String)_genesis.elementAt(i),i);
			}
			ts.addToGenesis (
			"Made a copy of time series (previous history " +
			"information is for original)" );
		}
		if ( _data_limits != null ) {
			ts._data_limits = (TSLimits)_data_limits.clone();
		}
		if ( _data_limits_original != null ) {
			ts._data_limits_original =
				(TSLimits)_data_limits_original.clone();
		}
		return ts;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
Copy the data array from one time series to another.  This method should be
defined in a derived class.
@param ts The time series to copy the data from.
@param start_date The time to start copying the data (if null, use the first
date from this instance).
@param end_date The time to end copying the data (if null, use the last date
from this instance).
@param copy_missing If true, copy missing data (including out-of-period missing
data).  If false, ignore missing data.
@exception TSException if an error occurs.
*/
public void copyData ( TS ts, DateTime start_date, DateTime end_date,
			boolean copy_missing )
throws TSException
{	String message = "This method needs to be " +
	"implemented in a derived class (e.g., MonthTS)";
	Message.printWarning ( 2, "TS.copyData", message );
	throw new TSException ( message );
}

/**
Copy the data array from one time series to another.  This method should be
defined in a derived class.
@param ts The time series to copy the data from.
@param copy_missing If true, copy missing data (including out-of-period missing
data).  If false, ignore missing data.
@exception TSException if an error occurs.
*/
public void copyData ( TS ts, boolean copy_missing )
throws TSException
{	String message = "This method needs to be " +
	"implemented in a derived class (e.g., MonthTS)";
	Message.printWarning ( 2, "TS.copyData", message );
	throw new TSException ( message );
}

/**
Copy one time series header to this one.  This copies everything except data
related to the data space.  Note that the dates are also copied and
allocateDataSpace() should be called if necessary to reset the data space.  The
following data is copied (the associated set method is shown to allow individual
changes to be applied after the copy, if appropriate).  See also the second
table that indicates what is NOT copied.  This method may need to be overloaded
in the future to allow only a partial copy of the header.

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
public void copyHeader ( TS ts )
{ 	setVersion( ts.getVersion() );
	setStatus ( ts.getStatus() );

	setInputName ( ts.getInputName() );

	// Copy TSIdent...

	try {	setIdentifier ( new TSIdent(ts.getIdentifier()) );
	}
	catch ( Exception e ) {
		// Should not happen.
	}

	// Need to initialize DateTime somehow, but how do you pick defaults?

	// THIS IS DATA RELATED 13 JUL 1998
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

	_comments	= new Vector (2,1);
	_comments = StringUtil.addListToStringList ( _comments,
				ts.getComments() );
	_genesis	= new Vector (2,1);
	_genesis = StringUtil.addListToStringList ( _genesis,
				ts.getGenesis() );

	setDataUnits( ts.getDataUnits() );
	setDataUnitsOriginal( ts.getDataUnitsOriginal() );

	// First set the missing data value...
	setMissing( ts.getMissing() );
	// Now set the range itself in case it has been reset...
	setMissingRange ( ts.getMissingRange() );

	// THIS IS DATA RELATED 13 Jul 1998
	//_dirty = true;// We need to recompute limits when we get the chance

	// Copy legend information...
	_legend = ts.getLegend();
	_extended_legend = ts.getExtendedLegend();

	// Data flags...

	_has_data_flags = ts._has_data_flags;
	_data_flag_length = ts._data_flag_length;
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	_comments = null;
	_data_limits = null;
	_data_limits_original = null;
	_data_units = null;
	_data_units_original = null;
	_date1 = null;
	_date1_original = null;
	_date2 = null;
	_date2_original = null;
	_description = null;
	_extended_legend = null;
	_genesis = null;
	_id = null;
	_input_name = null;
	_legend = null;
	_status = null;
	_version = null;
	super.finalize();
}

/**
Return a formatted extended legend string without changing saved legend.
@return A formatted extended legend string for the time series but do not
update the time series extended legend data.
The format is the same as for formatLegend().
@param format Format string.
@see #formatLegend
*/
public String formatExtendedLegend ( String format )
{	return formatExtendedLegend ( format, false );
}

/**
Return a formatted extended legend string and optionally save the changed
legend.
@return A formatted extended legend string for the time series.
The format is the same as for formatLegend().
@param format Format string.
@param flag If true, the extended legend data member will be updated after
formatting.
@see #formatLegend
*/
public String formatExtendedLegend ( String format, boolean flag )
{	// First format using the normal legend string...
	String extended_legend = formatLegend ( format );
	// Now, if desired, save the legend...
	if ( flag ) {
		setExtendedLegend ( extended_legend );
	}
	return extended_legend;
}

/**
Format a standard time series header, for use with formatOutput.
@return A Vector of strings containing the header.  Blank lines at the start
and end of the header are not included.
*/
public Vector formatHeader ()
{	Vector header = new Vector ( 10, 5 );

	header.addElement ( "Time Series Identifier  = " +
		getIdentifier().toString() );
	header.addElement ( "Description             = " + getDescription() );
	header.addElement ( "Data source             = " +
		getIdentifier().getSource() );
	header.addElement ( "Data type               = "
		+ getIdentifier().getType() );
	header.addElement ( "Data interval           = "
		+ getIdentifier().getInterval() );
	header.addElement ( "Data units              = " + _data_units );
	if ( (_date1 != null) && (_date2 != null) ) {
		header.addElement ( "Period                  = " +
			_date1 + " to " + _date2 );
		//StringUtil.formatString(_date1.getYear(), "%04d") + "-" +
		//StringUtil.formatString(_date1.getMonth(), "%02d") + " to " +
		//StringUtil.formatString(_date2.getYear(), "%04d") + "-" +
		//StringUtil.formatString(_date2.getMonth(), "%02d") ) );
	}
	else {	header.addElement ( "Period                  = N/A" );
	}
	if (	(_date1_original != null) &&
		(_date2_original != null)) {
		header.addElement ( "Orig./Avail. period     = " +
			_date1_original + " to " + _date2_original );
		//StringUtil.formatString(
		//_date1_original.getYear(), "%04d") + "-" +
		//StringUtil.formatString(
		//_date1_original.getMonth(), "%02d") + " to " +
		//StringUtil.formatString(
		//_date2_original.getYear(), "%04d") + "-" +
		//StringUtil.formatString(
		//_date2_original.getMonth(), "%02d") ) );
	}
	else {	header.addElement ( "Orig./Avail. period     = N/A" );
	}
	return header;
}

/**
Return a formatted legend string, without changing the legend in memory.
@return A formatted legend string for the time series but do not update the
time series legend data.  See the version that accepts a flag for a description
of format characters.
@param format Format string.
*/
public String formatLegend ( String format )
{	return formatLegend ( format, false );
}

/**
Return a formatted legend string, optionally changing the legend in memory.
@return A formatted legend string for the time series but do not update the
time series legend data.
@param format Format string containing normal characters and formatting strings
which will be replaced with time series header information, as follows (grouped
in categories):
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
<td><b>%L</b><br><p>
<b>%l</b><p>
<b>%w</b></td>
<td>The full location part of the identifier (e.g., "12345678_FREE").<p>
Main location part of the identifier (e.g., "12345678").<p>
Sub-location part of the identifier (e.g., "FREE").</td>
</tr>

<tr>
<td><b>%p</b></td>
<td>The time series period using date/times corresponding to the precision of
the data (e.g., "2006-10-01 18 Z - 2006-10-03 18 Z").
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
Main data source part of the identifier (reserved for future use and currently
the same as the full data type).<p>
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
<td>Time series sequence number (e.g., year for traces).</td>
</tr>

</table>
<p>

@param update_ts If true, the legend data member in the time series will be
updated.  Otherwise a formatted string is returned but the internal data member
is left unchanged.
@see TSIdent
@see RTi.Util.IO.DataUnits
*/
public String formatLegend ( String format, boolean update_ts )
{	StringBuffer buffer = new StringBuffer();

	Message.printStatus ( 2, "", "Legend format is " + format );
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
			// Format modifier.  Get the next character...
			++i;
			if ( i >= len ) {
				break;
			}
			c = format.charAt ( i );
			if ( c == '%' ) {
				// Literal %...
				buffer.append ( c );
			}
			else if ( c == 'A' ) {
				// Abbreviation from TSIdent...
				buffer.append ( _id.getAlias() );
			}
			else if ( c == 'b' ) {
				// Data interval base...
				buffer.append ( TimeInterval.getName(
					_id.getIntervalBase()) );
			}
			else if ( c == 'D' ) {
				// Description...
				buffer.append ( _description );
			}
			else if ( c == 'F' ) {
				// Full identifier...
				//buffer.append (
				//_id.getIdentifier() );
				buffer.append ( _id.toString() );
			}
			else if ( c == 'I' ) {
				// Full interval...
				buffer.append ( _id.getInterval() );
			}
			else if ( c == 'L' ) {
				// Full location...
				buffer.append ( _id.getLocation() );
			}
			else if ( c == 'l' ) {
				// Main location...
				buffer.append ( _id.getMainLocation() );
			}
			else if ( c == 'm' ) {
				// Data interval multiplier...
				buffer.append ( _id.getIntervalMult() );
			}
			else if ( c == 'p' ) {
				// Period...
				buffer.append ( "" + _date1 + " - " + _date2 );
			}
			else if ( c == 'S' ) {
				// Full source...
				buffer.append ( _id.getSource() );
			}
			else if ( c == 's' ) {
				// Main source...
				buffer.append ( _id.getMainSource() );
			}
			else if ( c == 'l' ) {
				// Main location...
				buffer.append ( _id.getMainLocation() );
			}
			else if ( c == 'U' ) {
				// Units...
				buffer.append ( _data_units );
			}
			else if ( c == 'T' ) {
				// Data type...
				buffer.append ( _id.getType() );
			}
			else if ( c == 't' ) {
				// Data main type (reserved for future use - for
				// now return the total)...
				buffer.append ( _id.getType() );
			}
			else if ( c == 'y' ) {
				// Data sub type (reserved for future use)...
			}
			else if ( c == 'w' ) {
				// Sub-location...
				buffer.append ( _id.getSubLocation() );
			}
			else if ( c == 'x' ) {
				// Sub source...
				buffer.append ( _id.getSubSource() );
			}
			else if ( c == 'Z' ) {
				// Scenario...
				buffer.append ( _id.getScenario() );
			}
			else if ( c == 'z' ) {
				// Sequence number...
				buffer.append ( "" + _id.getSequenceNumber() );
			}
			else {	// No match.  Add the % and the character...
				buffer.append ( "%" );
				buffer.append ( c );
			}
		}
		else {	// Just add the character...
			buffer.append ( c );
		}
	}
	if ( update_ts ) {
		setLegend ( buffer.toString() );
	}
	return buffer.toString();
}

/**
Format the time series into a general output format.  This method should be
overridden in the derived class.  For example, all MonthTS should be have a
general text report format.  The properties can be used to customize the
output.  This method is meant as a general output format.  Specific formats
should be implemented as classes with static readTimeSeries()/writeTimeSeries()
methods.
@return Vector of strings containing formatted output.
@param props Modifiers for output.
@exception RTi.TS.TSException If low-level formatting code throws
an exception.
*/
public Vector formatOutput ( PropList props )
throws TSException
{	Message.printWarning( 1, "TS.formatOutput",
	"TS.formatOutput(PropList) is virtual, redefine in derived classes." );
	return null;
}

/**
Format the time series into a general output format and write to an output file.
This method should be overridden in the derived class.
@return Formatted output in a Vector, or null.
@param out PrintWriter to receive output.
@param props Modifiers for output.
@exception RTi.TS.TSException Thrown if low-level formatting code throws
an exception.
*/
public Vector formatOutput ( PrintWriter out, PropList props )
throws TSException
{	Message.printWarning( 1, "TS.formatOutput", "TS.formatOutput(" +
	"PrintWriter,PropList) is virtual, redefine in derived classes" );
	return null;
}

/**
Format the time series into a general output format.  This method should be
overridden in the derived class.
@return Vector containing formatted output, or null.
@param out Name of the output file.
@param props Modifiers for output.
@exception RTi.TS.TSException Thrown if low-level formatting code throws
an exception.
*/
public Vector formatOutput ( String out, PropList props )
throws TSException
{	String	routine="TS.formatOutput(string,int,long)";
	Message.printWarning( 1, routine, "TS.formatOutput(String,PropList)" +
	" function is virtual, redefine in derived classes" );
	return null;
}

/**
Return the time series alias from the TSIdent.
@return The alias part of the time series identifier.
*/
public String getAlias( )
{	return _id.getAlias();
}

/**
Return the time series comments.
@return The comments Vector.
*/
public Vector getComments ()
{	return _comments;
}

/**
Return the maximum length of data flags, to be used when data flags are enabled.
@return the maximum length of data flags.
*/
public int getDataFlagLength()
{	return _data_flag_length;
}

/**
Return the data interval base.
@return The data interval base (see TimeInterval.*).
*/
public int getDataIntervalBase()
{	return _data_interval_base;
}

/**
Return the original data interval base.
@return The data interval base of the original data.
*/
public int getDataIntervalBaseOriginal()
{	return _data_interval_base_original;
}

/**
Return the data interval multiplier.
@return The data interval multiplier.
*/
public int getDataIntervalMult()
{	return _data_interval_mult;
}

/**
Return the original data interval multiplier.
@return The data interval multiplier of the original data.
*/
public int getDataIntervalMultOriginal()
{	return _data_interval_mult_original;
}

/**
Return the time series data limits (a new copy is returned).  If necessary, the
limits are refreshed.  The refresh() method should be defined in the derived
class.
@return The time series data limits.
@see TSLimits
*/
public TSLimits getDataLimits ()
{	// Make sure that the limits have been set...
	refresh();
	if ( _data_limits == null ) {
		return null;
	}
	else {	return( new TSLimits(_data_limits) );
	}
}

/**
Return the original time series data limits.  The reference to the original
data is returned so that MonthTSLimits or others can be returned.
@return The original time series data limits.
@see TSLimits
*/
public TSLimits getDataLimitsOriginal ()
{	if ( _data_limits_original == null ) {
		return null;
	}
	else {	return ( _data_limits_original );
	}
}

/**
Return a TSData for a date.  This method should be defined in derived classes,
especially if data flags are being used.
@param date Date to get data.
@return a TSData for the specified date.  This method is meant to be overridden
in derived classes.
*/
public TSData getDataPoint ( DateTime date )
{	Message.printWarning( 1, "TS.getDataPoint", 
	"This is a virtual function, redefine in lower classes" );
	// Empty point...
	TSData data = new TSData();
	return data;
}

/**
Return the position in the data array for a date.  THIS IS BEING PHASED OUT
BECAUSE OF PERFORMANCE AND VARIATIONS BETWEEN STORAGE IN DERIVED CLASSES.
@return The position in the data array for the given date.  The returned array
may have a different number of components, depending on the storage scheme.
This method should be overruled in a derived class.
@return null here.
@param date Date corresponding to the data value.
*/
public int [] getDataPosition ( DateTime date )
{	Message.printWarning( 1, "TS.getDataPosition", 
	"TS.getDataPosition() is virtual, redefine in derived classes." );
	// For now, return null as a default...
	return null;
}

/**
Return the number of data points that are allocated in memory.
@return The number of data points included in the period.
*/
public int getDataSize ( )
{	return _data_size;
}

/**
Return the data type from the TSIdent or an empty string if no TSIdent has been
set.
@return The data type abbreviation.
*/
public String getDataType( )
{	if ( _id == null ) {
		return "";
	}
	else {	return _id.getType();
	}
}

/**
Return the data units.
@return The data units.
@see RTi.Util.IO.DataUnits
*/
public String getDataUnits( )
{	return _data_units;
}

/**
Return the original data units.
@return The data units in the original data.
@see RTi.Util.IO.DataUnits
*/
public String getDataUnitsOriginal( )
{	return _data_units_original;
}

/**
Return the data value for a date.
@return The data value associated with a date.  This should be
overridden in derived classes (always returns the missing data value here).
@param date Date corresponding to the data value.
*/
public double getDataValue( DateTime date )
{	Message.printWarning( 1, "TS.getDataValue", 
	"TS.getDataValue is a virtual function, redefine in derived classes" );
	return _missing;
}

/**
Return the first date in the period of record (returns a copy).
@return The first date in the period of record, or null if the date is null.
*/
public DateTime getDate1()
{	if ( _date1 == null ) {
		return null;
	}
	return new DateTime ( _date1 );
}

/**
Return the first date in the original period of record (returns a copy).
@return The first date of the original data source (generally equal to or
earlier than the time series that is actually read), or null if the date is
null.
*/
public DateTime getDate1Original()
{	if ( _date1_original == null ) {
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
public DateTime getDate2Original()
{	if ( _date2_original == null ) {
		return null;
	}
	return new DateTime ( _date2_original );
}

/**
Return the time series description.
@return The time series description.
*/
public String getDescription( )
{	return _description;
}

/**
Return value of flag indicating whether time series is enabled.
@return true if the time series is enabled, false if disabled.
*/
public boolean getEnabled()
{	return _enabled;
}

/**
Return the extended time series legend.
@return Time series extended legend.
*/
public String getExtendedLegend()
{	return _extended_legend;
}

/**
Return the genesis information.
@return The genesis comments.
*/
public Vector getGenesis ()
{	return _genesis;
}

/**
Return the time series identifier as a TSIdent.
@return the time series identifier as a TSIdent.
@see TSIdent
*/
public TSIdent getIdentifier()
{	return _id;
}

/**
Return the time series identifier as a String.  This returns
TSIdent.getIdentifier().
@return The time series identifier as a string.
@see TSIdent
*/
public String getIdentifierString()
{	return _id.getIdentifier();
}

/**
Return the input name (file or database table) for the time series.
@return the input name.
*/
public String getInputName ()
{	return _input_name;
}

/**
Return the time series legend.
@return Time series legend.
*/
public String getLegend()
{	return _legend;
}

/**
Return the location part of the time series identifier.
@return The location part of the time series identifier (from TSIdent).
*/
public String getLocation()
{	return _id.getLocation();
}

/**
Return the missing data value used for the time series (single value).
@return The value used for missing data.
*/
public double getMissing ()
{	return _missing;
}

/**
Return the missing data range (2 values).
@return The range of values for missing data.  The first value is the lowest
value, the second the highest.  A new array instance is returned.
*/
public double[] getMissingRange ()
{	double [] missing_range = new double[2];
	missing_range[0] = _missingl;
	missing_range[1] = _missingu;
	return missing_range;
}

/**
Return the date for the first non-missing data value.
@return The first date where non-missing data occurs (a copy is returned).
*/
public DateTime getNonMissingDataDate1( )
{	return new DateTime (_data_limits.getNonMissingDataDate1());
}

/**
Return the date for the last non-missing data value.
@return The last date where non-missing data occurs (a copy is returned).
*/
public DateTime getNonMissingDataDate2( )
{	return new DateTime (_data_limits.getNonMissingDataDate2());
}

/**
Return the sequence number for the time series.
@return The sequence number for the time series.  This is meant to be used
when an array of time series traces is maintained.
@return time series sequence number.
*/
public int getSequenceNumber ()
{	return _id.getSequenceNumber();
}

/**
Return the time series status.
@return The status flag for the time series.  This is a general purpose flag.
@see #setStatus
*/
public String getStatus ( )
{	return _status;
}

/**
Returns the data in the specified DataFlavor, or null if no matching flavor
exists.  From the Transferable interface.
Supported dataflavors are:<br>
</ul>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul> 
@param flavor the flavor in which to return the data.
@return the data in the specified DataFlavor, or null if no matching flavor
exists.
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

/**
Returns the flavors in which data can be transferred.  From the Transferable
interface.  The order of the dataflavors that are returned are:<br>
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
public String getVersion ()
{	return _version;
}

/**
Indicate whether the time series has data, which is determined by checking to
see whether memory has been allocated for the data space (which implies that
the dates have been set).  This method can be checked after data are read rather
than checking the dates.  This method should be defined in derived classes with
specific data storage schemes.
@return true if the time series has data, false if not.
*/
public boolean hasData ()
{	Message.printWarning( 1, "TS.getDataValue", 
	"TS.hasData() is a virtual function, redefine in derived classes" );
	return false;
}

/**
Indicate whether the time series has data flags.
@return true if data flags are enabled for the time series.
*/
public boolean hasDataFlags ()
{	return _has_data_flags;
}

/**
Set whether the time series has data flags.  This method should be called before
allocateDataSpace() is called.  If data flags are enabled, allocateDataSpace()
will allocate memory for the data and data flags.
@param has_data_flags Indicates whether data flags will be associated with each
data value.  The data flag is a character string (char []).
@param data_flag_length Maximum length of the data flags.  Each flag is
stored as a char[] to minimize memory use so the data flag length should be set
to the maximum expected size of the flag (which is usually defined for the time
series input).  If specified as zero, no memory will be allocated.
@return true if data flags are enabled for the time series, after the set.
*/
public boolean hasDataFlags ( boolean has_data_flags, int data_flag_length )
{	_has_data_flags = has_data_flags;
	_data_flag_length = data_flag_length;
	return _has_data_flags;
}

/**
Initialize data members.
*/
private void init( )
{	_version = "";

	_input_name = "";

	// Need to initialize an empty TSIdent...

	_id = new TSIdent ();
	_legend = "";
	_extended_legend = "";

	_data_size = 0;

	// DateTime need to be initialized somehow...

        setDataType( "" );

	_data_interval_base = 0;

	_data_interval_mult = 1;

	_data_interval_base_original = 1;

	_data_interval_mult_original = 0;

	setDescription( "" );

	_comments = new Vector (2,2);

	_genesis = new Vector (2,2);

	setDataUnits( "" );

	setDataUnitsOriginal( "" );

	setMissing ( -999.0 );

	_data_limits = new TSLimits();

	_dirty = true;	// We need to recompute limits when we get the chance

	_enabled = true;
	_selected = false;	// Let other code select, e.g., as query result
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
Determine if a data value for the time series is missing.  The missing value can
be set to a range of values or a single value, using setMissing().
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
public boolean isDataMissing ( double value )
{	if ( value != value ) {
		// Check for NaN...
		return true;
	}
	if ( 	(value >= _missingl) &&
		(value <= _missingu) ) {
		return true;
	}
	return false;
}

/**
Indicate whether the time series is dirty (data have been modified).
@return true if the time series is dirty, false if not.
*/
public boolean isDirty ()
{	return _dirty;
}

/**
Indicate whether the time series is editable.
@return true if the time series is editable, false if not.
*/
public boolean isEditable ()
{	return _editable;
}

/**
Indicate whether the time series is selected.
@return true if the time series is selected, false if not.
*/
public boolean isSelected()
{	return _selected;
}

/**
Create and return an iterator for the time series using the full period for the
time series.  For regular interval time series,
the iterator is TSIterator.  IrregularTS use the IrregularTSIterator.
@return an iterator for the time series.
@exception if the dates for the iterator are not set (they are not set in the
time series).
*/
public TSIterator iterator ()
throws Exception
{	return new TSIterator ( this );
}

/**
Return an iterator for the time series using the specified period.
For regular interval time series,
the iterator is that same. IrregularTS use the IrregularTSIterator.
@param date1 Start of data iteration.
@param date2 End of data iteration.
@return an iterator for the time series.
@exception if the dates for the iterator are not set (they are not set in the
time series).
*/
public TSIterator iterator ( DateTime date1, DateTime date2 )
throws Exception
{	return new TSIterator ( this, date1, date2 );
}

/**
Refresh the secondary data (e.g., data limits).  This should be overruled in
the derived class.
*/
public void refresh ()
{	Message.printWarning ( 1, "TS.refresh",
	"TS.refresh is virtual.  Define in the derived class!" );
}

/**
Set the time series identifier alias.
@param alias Alias of time series.
*/
public void setAlias ( String alias )
{	if ( alias != null ) {
		_id.setAlias( alias );
	}
}

/**
Set the comments string vector.
@param comments Comments to set.
*/
public void setComments ( Vector comments )
{	if ( comments != null ) {
		_comments = comments;
	}
}

/**
Set the data interval.
@param base Base interval (see TimeInterval.*).
@param mult Base interval multiplier.
*/
public void setDataInterval ( int base, int mult )
{	_data_interval_base = base;
	_data_interval_mult = mult;
}

/**
Set the data interval for the original data.
@param base Base interval (see TimeInterval.*).
@param mult Base interval multiplier.
*/
public void setDataIntervalOriginal ( int base, int mult )
{	_data_interval_base_original = base;
	_data_interval_mult_original = mult;
}

/**
Set the data limits for the time series.  This is generally only called by
internal routines.  A copy is saved.
@param limits Time series limits.
@see TSLimits
*/
public void setDataLimits( TSLimits limits )
{	_data_limits = new TSLimits ( limits );
}

/**
Set the original data limits for the time series.  This is currently typically
only called by application code (like TSTool).  The reference to the limits are
saved so that derived class data can be stored and returned with
getDataLimitsOriginal().  Make a copy of the data if it needs to be protected.
@param limits Time series data limits.
@see TSLimits
*/
public void setDataLimitsOriginal ( TSLimits limits )
{	if ( limits == null ) {
		_data_limits_original = null;
	}
	_data_limits_original = limits;
}

/**
Set the number of data points including the full period.  This should be called
by refresh().
@param data_size Number of data points in the time series.
*/
protected void setDataSize ( int data_size )
{	_data_size = data_size;
}

/**
Set the data type.
@param data_type Data type abbreviation.
*/
public void setDataType( String data_type )
{	if ( (data_type != null) && (_id != null) ) {
		_id.setType ( data_type );
	}
}

/**
Set the data units.
@param data_units Data units abbreviation.
@see RTi.Util.IO.DataUnits
*/
public void setDataUnits( String data_units )
{	if ( data_units != null ) {
		_data_units = data_units;
	}
}

/**
Set the data units for the original data.
@param units Data units abbreviation.
@see RTi.Util.IO.DataUnits
*/
public void setDataUnitsOriginal( String units )
{	if ( units != null ) {
		_data_units_original = units;
	}
}

/**
Set a data value for the specified date.
@param date Date of interest.
@param val Data value for date.
@see RTi.Util.Time.DateTime
*/
public void setDataValue ( DateTime date, double val )
{	Message.printWarning ( 1, "TS.setDataValue", "TS.setDataValue is " +
	"virtual and should be redefined in derived classes" );
}

/**
Set a data value and associated information for the specified date.  This method
should be defined in derived classes.
@param date Date of interest.
@param val Data value for date.
@param data_flag Data flag associated with the data value.
@param duration Duration (seconds) for the data value.
@see DateTime
*/
public void setDataValue ( DateTime date, double val, String data_flag,	int duration )
{	Message.printWarning ( 1, "TS.setDataValue", "TS.setDataValue is " +
	"virtual and should be redefined in derived classes" );
}

/**
Set the first date in the period.  A copy is made.
The date precision is set to the precision appropriate for the time series.
@param t First date in period.
@see DateTime
*/
public void setDate1 ( DateTime t )
{	if ( t != null ) {
		_date1 = new DateTime ( t );
		_date1.setPrecision ( _data_interval_base );
	}
}

/**
Set the first date in the period in the original data.  A copy is made.
The date precision is set to the precision appropriate for the time series.
@param t First date in period in the original data.
@see DateTime
*/
public void setDate1Original( DateTime t )
{	if ( t != null ) {
		_date1_original = new DateTime ( t );
		_date1_original.setPrecision ( _data_interval_base );
	}
}

/**
Set the last date in the period.  A copy is made.
The date precision is set to the precision appropriate for the time series.
@param t Last date in period.
@see DateTime
*/
public void setDate2 ( DateTime t )
{	if ( t != null ) {
		_date2 = new DateTime ( t );
		_date2.setPrecision ( _data_interval_base );
	}
}

/**
Set the last date in the period in the original data. A copy is made.
The date precision is set to the precision appropriate for the time series.
@param t Last date in period in the original data.
@see DateTime
*/
public void setDate2Original( DateTime t )
{	if ( t != null ) {
		_date2_original = new DateTime ( t );
		_date2_original.setPrecision ( _data_interval_base );
	}
}

/**
Set the description.
@param description Time series description (this is not the comments).
*/
public void setDescription( String description )
{	if ( description != null ) {
		_description = description;
	}
}

/**
Set whether the time series is dirty (data have been modified).
@param dirty true if the time series is dirty/edited, false if not.
*/
public void setDirty ( boolean dirty )
{	_dirty = dirty;
}

/**
Set whether the time series is editable.
@param editable true if the time series is editable.
*/
public void setEditable ( boolean editable )
{	_editable = editable;
}

/**
Indicate whether the time series is enabled.  This is being phased in and is
currently only used by graphics code.
@param enabled true if the time series is enabled or false if disabled.
*/
public void setEnabled ( boolean enabled )
{	_enabled = enabled;
}

/**
Set the time series extended legend.  This was used at one point to set a
secondary legend because the Java plotting package did not allow a long legend
in the graph.  Currently, this data item is being used to set the legend for
the table time series view.
@param extended_legend Time series extended legend (can be used for labels on 
graphs, etc.).
@see #formatExtendedLegend
*/
public void setExtendedLegend ( String extended_legend )
{	if ( extended_legend != null ) {
		_extended_legend = extended_legend.trim();
	}
}

/**
Set the genesis information.  The original is lost.
@param genesis Genesis comments.
*/
public void setGenesis ( Vector genesis )
{	setGenesis ( genesis, false );
}

/**
Set the genesis information.
@param genesis Genesis comments.
@param append Indicates whether genesis information should be appended.
*/
public void setGenesis ( Vector genesis, boolean append )
{	if ( !append ) {
		// Don't call removeAllElements() because the genesis may have
		// been retrieved and then reset using the same Vector!
		_genesis = new Vector();
	}
	_genesis = StringUtil.addListToStringList ( _genesis, genesis );
}

/**
Set the time series identifier using a TSIdent.
Note that this only sets the identifier but
does not set the separate data fields (like data type).
@param id Time series identifier.
@see TSIdent
@exception Exception If there is an error setting the identifier.
*/
public void setIdentifier ( TSIdent id )
throws Exception
{	if ( id != null ) {
		_id = new TSIdent ( id );
	}
}

/**
Set the time series identifier using a string.
Note that this only sets the identifier but
does not set the separate data fields (like data type).
@param identifier Time series identifier.
@exception Exception If there is an error setting the identifier.
*/
public void setIdentifier( String identifier )
throws Exception
{	if ( identifier != null ) {
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
@exception Exception If there is an error setting the identifier (e.g., interval
is not recognized).
*/
public void setIdentifier( 	String location, String source,
				String type, String interval,
				String scenario )
throws Exception
{	_id.setLocation( location );
	_id.setSource( source );
	_id.setType( type );
	_id.setInterval( interval );
	_id.setScenario( scenario );
}

/**
Set the input name (file or database table).
*/
public void setInputName ( String input_name )
{	if ( input_name != null ) {
		_input_name = input_name;
	}
}

/**
Set the time series legend.
@param legend Time series legend (can be used for labels on graphs, etc.).
@see #formatLegend
*/
public void setLegend ( String legend )
{	if ( legend != null ) {
		_legend = legend.trim();
	}
}

/**
Set the time series identifier location.
@param location Location of time series.
*/
public void setLocation ( String location )
{	if ( location != null ) {
		_id.setLocation( location );
	}
}

/**
Set the missing data value for the time series.  The upper and lower bounds
of missing data are set to this value *1.001 and *.999, to allow for
precision truncation.
@param missing Missing data value for time series.
*/
public void setMissing ( double missing )
{	_missing = missing;
	if ( missing != missing ) {
		// Will return true if NaN and can't do anything else.  The
		// isDataMissing() method has a similar check.
		return;
	}
	if ( missing < 0 ) {
		_missingl = missing * 1.001;
		_missingu = missing * 0.999;
	}
	else {	_missingl = missing * 0.999;
		_missingu = missing * 1.001;
	}
}

/**
Set the missing data range for the time series.  The value returned from
getMissing() is computed as the average of the values.  Two values must be
specified, neither of which can be a NaN.
@param missing Missing data range for time series.
*/
public void setMissingRange ( double [] missing )
{	if ( missing == null ) {
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
	else {	_missingl = missing[1];
		_missingu = missing[0];
	}
}

/**
Set the date for the first non-missing data value.
@param t Date for the first non-missing data value.
@see DateTime
*/
public void setNonMissingDataDate1( DateTime t )
{	if ( t != null ) {
		_data_limits.setNonMissingDataDate1 ( t );
	}
}

/**
Set the date for the last non-missing data value.
@param t Date for the last non-missing data value.
@see DateTime
*/
public void setNonMissingDataDate2( DateTime t )
{	if ( t != null ) {
		_data_limits.setNonMissingDataDate2( t );
	}
}

/**
Indicate whether the time series is selected.  This is being phased in and is
currently only used by graphics code.
*/
public void setSelected ( boolean selected )
{	_selected = selected;
}

/**
Set the sequence number.  This can be used to indicate a trace number.
@param sequence_number Sequence number for the time series.
*/
public void setSequenceNumber ( int sequence_number )
{	_id.setSequenceNumber ( sequence_number );
}

/**
Set the time series identifier source.
@param source Source for time series.
*/
public void setSource ( String source )
{	if ( source != null ) {
		_id.setSource ( source );
	}
}

/**
Set the status flag for the time series.  This is used by high-level code when
manipulating time series.  For example, a Vector of time series might be
passed to a routine for graphing.  Additionally, another display component may
list an extended legend.  The status allows the first component to disable some
time series because of incompatibility so the second component can detect.
This feature may be phased out.
@see #getStatus
*/
public void setStatus ( String status )
{	if ( status != null ) {
		_status = status;
	}
}

/**
Set the time series version, to be used with input file formats.
@param version Version number for time series file.
*/
public void setVersion( String version )
{	if ( version != null ) {
		_version = version;
	}
}

} // End of TS class
