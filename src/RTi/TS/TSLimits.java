// ----------------------------------------------------------------------------
// TSLimits - simple class for managing time series data limits
// ----------------------------------------------------------------------------
// History:
//
// 24 Sep 1997	Steven A. Malers, RTi	Initial version.
// 05 Jan 1998	SAM, RTi		Update based on C++ port.  Add
//					_date1 and _date2 so that we can use
//					TSLimits as a return from routines that
//					return date limits.  Add
//					_non_missing_data_count,
//					getNonMissingDataCount(), and
//					hasNonMissingData().
// 19 Mar 1998	SAM, RTi		Add javadoc.
// 20 Aug 1998	SAM, RTi		Add setMissingDataCount().
// 02 Jan 1999	SAM, RTi		Add sum and mean to the data that are
//					tracked to support searches, etc.
//					Change the return type of set routines
//					to void and don't return anything (the
//					methods always returned 0).  Add
//					constructors and getDataLimits methods
//					so that this routine actually does the
//					computations.  This allows independent
//					use from TSUtil.getDataLimits, which
//					is desirable but may not perform well
//					in some cases.
// 12 Apr 1999	SAM, RTi		Add finalize.
// 28 Oct 1999	SAM, RTi		Add flag to ignore <= 0 values in
//					computations (treat as missing).
// 22 Mar 2001	SAM, RTi		Change toString() to print nicer so
//					output can be used in reports.
// 28 Aug 2001	SAM, RTi		Implement clone().  A copy constructor
//					is already implemented but clone() is
//					used by TS and might be preferred by
//					some developers.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2003-03-25	SAM, RTi		Add data units as a data member and
//					add getDataUnits().  This is needed
//					because TSTool now has a
//					convertDataUnits() command and the units
//					for TSLimits need to be set for the
//					current and original data.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TS.INTERVAL* to TimeInterval.
// 2004-02-17	SAM, RTi		* Change private data members to start
//					  with __, as per RTi standards.
//					* Change the private getDataLimits() to
//					  calculateDataLimits() to more
//					  accurately reflect its purpose.
//					* Fix copy constructor to set the _flags
//					  and __ts, but do not copy the time
//					  series.
// 2004-03-04	J. Thomas Sapienza, RTi	Class is now serializable.
// 2005-02-04	SAM, RTi		Add getTimeSeries() to return the time
//					series associated with the limits.
//					Add setTimeSeries() also.
//					When limits cannot be calculated in
//					calculateDataLimits(), only print the
//					warning message at debug level 2.
// 2005-05-25	SAM, RTi		Change so "low level" warning messages
//					are handled and level 3 instead of 2, to
//					mininimize visibility in the log viewer.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.io.Serializable;

import java.util.List;

import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
The TSLimits class stores information about the data and date limits of a time
series, including maximum, minimum, and mean, and important date/times.
An instance is used by the TS base class and TSUtil routines use TSLimits to
pass information.  In general, code outside of the TS package will only use the
get*() methods (because TS or TSUtil methods will set the data).
This TSLimits base class can be used for any time series.  More detailed limits
like MonthTSLimits can be extended to contain more information.  The
toString() method should be written to provide output suitable for use in a report.
*/
@SuppressWarnings("serial")
public class TSLimits
implements Cloneable, Serializable
{

// Public flags...

/**
Flags used to indicate how limits are to be computed.
The following indicates that a time series' full limits should be refreshed.
This is generally used only by code internal to the TS library.
*/
public static final int REFRESH_TS = 0x1;

/**
Do not compute the total limits (using this TSLimits class).  This is used by
classes such as MonthTSLimits to increase performance.
*/
public static final int NO_COMPUTE_TOTALS = 0x2;

/**
Do not compute the detailed limits (e.g., using MonthTSLimits).  This is used by
classes such as MonthTSLimits to increase performance.
*/
public static final int NO_COMPUTE_DETAIL = 0x4;

/**
Ignore values <= 0 when computing averages (treat as missing data).
This make sense for time series
such as reservoirs and flows.  It may be necessary at some point to allow any
value to be ignored but <= 0 is considered a common and special case.
*/
public static final int IGNORE_LESS_THAN_OR_EQUAL_ZERO = 0x8;

// Data members...

private TS __ts = null;	// Time series being studied.
private DateTime __date1;
private DateTime __date2;
protected int _flags;		// Flags to control behavior.
private double __max_value;
private DateTime __max_value_date;
private double __mean;
private double __median;
private double __min_value;
private DateTime __min_value_date;
private int __missing_data_count;
private int __non_missing_data_count;
private DateTime __non_missing_data_date1;
private DateTime __non_missing_data_date2;
private double __skew;
private double __stdDev;
private double __sum;
private String __data_units=""; // Data units (just copy from TS at the time of creation).

private boolean	__found = false;

/**
Default constructor.  Initialize the dates to null and the limits to zeros.
*/
public TSLimits ()
{	initialize ();
}

/**
Copy constructor.  A deep copy is made, except that the time series is not copied.
@param limits Instance to copy.
*/
public TSLimits ( TSLimits limits )
{	initialize ();

	if ( limits.__date1 != null ) {
		__date1 = new DateTime ( limits.__date1 );
	}
	if ( limits.__date2 != null ) {
		__date2 = new DateTime ( limits.__date2 );
	}
	__max_value = limits.__max_value;
	if ( limits.__max_value_date != null ) {
		__max_value_date = new DateTime ( limits.__max_value_date );
	}
	__min_value = limits.__min_value;
	if ( limits.__min_value_date != null ) {
		__min_value_date = new DateTime ( limits.__min_value_date );
	}
	if ( limits.__non_missing_data_date1 != null ) {
		__non_missing_data_date1 = new DateTime ( limits.__non_missing_data_date1 );
	}
	if ( limits.__non_missing_data_date2 != null ) {
		__non_missing_data_date2 = new DateTime ( limits.__non_missing_data_date2 );
	}
	__non_missing_data_count = limits.__non_missing_data_count;
	__missing_data_count = limits.__missing_data_count;
	__mean = limits.__mean;
	__median = limits.__median;
	__sum = limits.__sum;
	__found = limits.__found;
	_flags = limits._flags;
	__skew = limits.__skew;
	__stdDev = limits.__stdDev;
	__ts = limits.__ts;
}

/**
Constructor to compute the limits given a TS.  This is the main constructor
and is overloaded in a variety of ways.  If a variant of this
constructor that does not take a TS is used, the limits are not computed in
this class and must be set from calling code.
@param ts Time series of interest.
@param startdate Starting date for the check.
@param enddate Ending date for the check.
@param flags Indicates special operations performed during computations.
See the TSLimits.REFRESH_TS, TSLimits.NO_COMPUTE_DETAIL, and
TSLimits.NO_COMPUTE_TOTALS flags for explanations.
@exception TSException if there is an error computing the limits.
*/
public TSLimits ( TS ts, DateTime startdate, DateTime enddate, int flags )
throws TSException
{	try {
		initialize ();
		__ts = ts;
		__data_units = ts.getDataUnits();
		boolean refresh_flag = false;
		_flags = flags;
		if ( (flags & TSLimits.REFRESH_TS) != 0 ) {
			refresh_flag = true;
		}
		// Make sure that this version is called...
		calculateDataLimits ( ts, startdate, enddate, refresh_flag );
	}
	catch ( Exception e ) {
		String message, routine = "TSLimits(TS,DateTime,DateTime,int)";
		message = "Error computing TSLimits";
		Message.printWarning ( 3, routine, message );
		throw new TSException ( message );
	}
}

/**
Construct the TS limits for the full period.
@param ts Time series of interest.
@exception TSException if there is an error computing the limits.
*/
public TSLimits ( TS ts )
throws TSException
{	try {
		initialize ();
		__ts = ts;
		__data_units = ts.getDataUnits();
		// Make sure that this version is called...
		calculateDataLimits(ts, (DateTime)null, (DateTime)null, false );
	}
	catch ( Exception e ) {
		String message, routine = "TSLimits(TS)";
		message = "Error computing TSLimits";
		Message.printWarning ( 3, routine, message );
		throw new TSException ( message );
	}
}

/**
Construct the TS limits between two dates.
@param ts Time series of interest.
@param startdate Starting date for the check.
@param enddate Ending date for the check.
@exception TSException if there is an error computing the limits.
*/
public TSLimits ( TS ts, DateTime startdate, DateTime enddate )
throws TSException
{	try {
		initialize ();
		__ts = ts;
		__data_units = ts.getDataUnits();
		// Make sure this version is called...
		calculateDataLimits ( ts, startdate, enddate, false );
	}
	catch ( Exception e ) {
		String message, routine = "TSLimits(TS,DateTime,DateTime)";
		message = "Error computing TSLimits";
		Message.printWarning ( 3, routine, message );
		throw new TSException ( message );
	}
}

/**
Indicates whether the time series data and dates have been fully processed. 
Sometimes only some of the data members are used.  This routine is most often used by low-level code.
@return A boolean indicating whether the limits have been found.
*/
public boolean areLimitsFound ()
{	return __found;
}

/**
Calculate the total data limits for a time series between two dates.
This code was taken from the TSUtil.getDataLimits method.
@param ts Time series of interest.
@param start0 Starting date for the check.
@param end0 Ending date for the check.
@param refresh_flag Indicates whether the time series should be refreshed first
(in general this is used only within the TS package and the version of this
routine without the flag should be called).
@see TSLimits
@exception if there is an error computing limits.
*/
private void calculateDataLimits ( TS ts, DateTime start0, DateTime end0, boolean refresh_flag )
throws TSException
{	String message, routine="TSLimits.getDataLimits";
	double max = 1.0, mean = 0.0, min = 0.0, sum = 0.0, value = 0.0;
	int base=0, missing_count = 0, mult = 0, non_missing_count = 0;
	boolean found = false;
	DateTime date, max_date = null, min_date = null, non_missing_data_date1 = null,
		non_missing_data_date2 = null, t = null, ts_date1, ts_date2;

	try {
		// Main try...

		if ( ts == null ) {
			message = "NULL time series";
			Message.printWarning ( 3, routine, message );
			throw new TSException ( message );
		}
	
		// Initialize the sum and the mean...
	
		double missing = ts.getMissing();
		sum = missing;
		mean = missing;
	
		// Get valid date limits because the ones passed in may have been null...
	
		TSLimits valid_dates = TSUtil.getValidPeriod ( ts, start0, end0 );
		DateTime start = valid_dates.getDate1();
		DateTime end = valid_dates.getDate2();
		valid_dates = null;
	
		// Make sure that the time series has current limits...
	
		base = ts.getDataIntervalBase();
		mult = ts.getDataIntervalMult();
		if ( refresh_flag ) {
			// Force a refresh of the time series.
		    ts.refresh();
		}
	
		// Get the variables that are used often in this function.
	
		ts_date1 = ts.getDate1 ();
		ts_date2 = ts.getDate2 ();
	
		// Figure out if we are treating data <= 0 as missing...
	
		boolean ignore_lezero = false;
		if ( (_flags & IGNORE_LESS_THAN_OR_EQUAL_ZERO) != 0 ) {
			ignore_lezero = true;
		}
	
		// Loop through the dates and get max and min data values;
		// TODO SAM 2010-06-15 Need to consolidate code to use iterator
	
		if ( base == TimeInterval.IRREGULAR ) {
			// Loop through the dates and get max and min data values;
			// Need to cast as an irregular TS...
	
			IrregularTS its = (IrregularTS)ts;
	
			List<TSData> data_array = its.getData ();
			if ( data_array == null ) {
				message = "Null data for " + ts;
				Message.printWarning ( 3, routine, message );
				throw new TSException ( message );
			}
			int size = data_array.size();
			TSData ptr = null;
			for ( int i = 0; i < size; i++ ) {
				ptr = data_array.get(i);
				date = ptr.getDate();
		
				if ( date.lessThan( ts_date1 ) ) {
					// Still looking for data...
					continue;
				}
				else if ( date.greaterThan( ts_date2 ) ) {
					// No need to continue processing...
					break;
				}
	
				value = ptr.getDataValue();
			
				if ( ts.isDataMissing( value ) || (ignore_lezero && (value <= 0.0)) ) {
					//The value is missing
					++missing_count;
	                continue;
				}
	
				// Else, data value is not missing...
	
				if ( ts.isDataMissing(sum) ) {
					// Reset the sum...
					sum = value;
				}
				else {
					// Add to the sum...
					sum += value;
				}
				++non_missing_count;
	
				if ( found ) {
					// Already found the first non-missing point so
					// all we need to do is check the limits.  These
					// should only result in new DateTime a few times...
					if( value > max ) {
	                	max = value;
						max_date = new DateTime ( date );
					}
					if( value < min ) {
	                	min = value;
						min_date = new DateTime ( date );
	                }
				}
				else {
					// Set the limits to the first value found...
					//date = new DateTime ( t );
					max = value;
					max_date = new DateTime ( date );
					min = value;
					min_date = max_date;
					non_missing_data_date1 = max_date;
					non_missing_data_date2 = max_date;
					found = true;
					continue;
				}
	        }
			// Now search backwards to find the first non-missing date...
			if ( found ) {
				for ( int i = (size - 1); i >= 0; i-- ){
					ptr = data_array.get(i);
					date = ptr.getDate();
					value = ptr.getDataValue();
					if ( date.greaterThan(end) ) {
						// Have not found data...
						continue;
					}
					else if ( date.lessThan(start) ) {
						// Passed start...
						break;
					}
					if ( (!ignore_lezero && !ts.isDataMissing(value)) ||
						(ignore_lezero && ((value > 0.0) && !ts.isDataMissing(value))) ) {
						// Found the one date we are after...
						non_missing_data_date2 = new DateTime ( date );
						break;
					}
				}
			}
		}
		else {
			// A regular TS... easier to iterate...
			// First loop through and find the data limits and the minimum non-missing date...
			t = new DateTime ( start, DateTime.DATE_FAST );
			for ( ; t.lessThanOrEqualTo(end); t.addInterval( base, mult )) {
	
				value = ts.getDataValue( t );
			
				if ( ts.isDataMissing(value) || (ignore_lezero && (value <= 0.0)) ) {
					//The value is missing
					++missing_count;
	                continue;
				}
	
				// Else, data value is not missing...
	
				if ( ts.isDataMissing(sum) ) {
					// Reset the sum...
					sum = value;
				}
				else {
					// Add to the sum...
					sum += value;
				}
				++non_missing_count;
	
				if ( found ) {
					// Already found the first non-missing point so
					// all we need to do is check the limits.  These
					// should only result in new DateTime a few times...
					if ( value > max ) {
	                	max = value;
						max_date = new DateTime ( t );
					}
					if ( value < min ) {
	                	min = value;
						min_date = new DateTime ( t );
	                }
				}
				else {
					// First non-missing point so set the initial values...
					date = new DateTime( t );
					max = value;
					max_date = date;
					min = value;
					min_date = date;
					non_missing_data_date1 = date;
					non_missing_data_date2 = date;
					found = true;
				}
	        }
			// Now loop backwards and find the last non-missing value...
			t = new DateTime ( end, DateTime.DATE_FAST );
			if ( found ) {
				for( ; t.greaterThanOrEqualTo(start); t.addInterval( base, -mult )) {
					value = ts.getDataValue( t );
					if ( (!ignore_lezero && !ts.isDataMissing(value)) ||
						(ignore_lezero && ((value > 0.0) && !ts.isDataMissing(value))) ) {
						// The value is not missing...
						non_missing_data_date2 =new DateTime(t);
						break;
					}
				}
			}
		}
		
		// TODO SAM 2010-06-15 This is a performance hit, but not too bad
		// TODO SAM 2010-06-15 Consider treating other statistics similarly but need to define unit tests
		// TODO SAM 2010-06-15 This code would need to be changed if doing Lag-1 correlation because order matters
		// For newly added statistics, use helper method to get data, ignoring missing...
		double [] dataArray = TSUtil.toArray(ts, start, end, 0, false );
		// Check for <= 0 values if necessary
		int nDataArray = dataArray.length;
		if ( ignore_lezero ) {
		    for ( int i = 0; i < nDataArray; i++ ) {
		        if ( dataArray[i] <= 0.0 ) {
		            // Just exchange with the last value and reduce the size
		            double temp = dataArray[i];
		            dataArray[i] = dataArray[nDataArray - 1];
		            dataArray[nDataArray - 1] = temp;
		            --nDataArray;
		        }
		    }
		}
		if ( nDataArray > 0 ) {
		    setMedian ( MathUtil.median (nDataArray, dataArray) );
		}
        if ( nDataArray > 1 ) {
            try {
                setStdDev ( MathUtil.standardDeviation(nDataArray, dataArray) );
            }
            catch ( Exception e ) {
                // Likely due to small sample size
            }
        }
        if ( nDataArray > 2 ) {
            try {
                setSkew ( MathUtil.skew(nDataArray, dataArray) );
            }
            catch ( Exception e ) {
                // Likely due to small sample size
            }
        }
	
		if ( !found ) {
			message = "\"" + ts.getIdentifierString() + "\": problems finding limits, whole POR missing!";
			Message.printWarning( 3, routine, message );
			throw new TSException ( message );
		}
	
		if ( Message.isDebugOn ) {
			Message.printDebug( 10, routine, "Overall date limits are: " + start + " to " + end );
			Message.printDebug( 10, routine,
			"Found limits to be: " + min + " on " + min_date + " to " + max + " on " + max_date );
			Message.printDebug( 10, routine,
			"Found non-missing data dates to be: " + non_missing_data_date1 + " -> " + non_missing_data_date2 );
		}
	
		// Set the basic information...
	
		setDate1 ( start );
		setDate2 ( end );
		setMaxValue ( max, max_date );
		setMinValue ( min, min_date );
		setNonMissingDataDate1 ( non_missing_data_date1 );
		setNonMissingDataDate2 ( non_missing_data_date2 );
		setMissingDataCount ( missing_count );
		setNonMissingDataCount ( non_missing_count );
		//int data_size = calculateDataSize ( ts, start, end );
		//limits.setNonMissingDataCount ( data_size - missing_count );
		if ( !ts.isDataMissing (sum) && (non_missing_count > 0) ) {
			mean = sum/(double)non_missing_count;
		}
		else {
			mean = missing;
		}
		setSum ( sum );
		setMean ( mean );
	}
	catch ( Exception e ) {
		message = "Error computing limits.";
		Message.printWarning ( 3, routine, message );
		// Put in debug because output sometimes is overwhelming when data are not available.
		if ( Message.isDebugOn ) {
			Message.printWarning ( 3, routine, e );
		}
		throw new TSException ( message );
	}
}

/**
Check to see if ALL the dates have been set (are non-null) and if so set the
_found flag to true.  If a TSLimits is being used for something other than fill
limits analysis, then external code may need to call setLimitsFound() to manually set the found flag.
*/
private void checkDates ()
{	if ( (__date1 != null) && (__date2 != null) &&
		(__max_value_date != null) && (__min_value_date != null) &&
		(__non_missing_data_date1 != null) && (__non_missing_data_date2 != null) ) {
		// The dates have been fully processed (set)...
		__found = true;
	}
}

/**
Clone the object.  The Object base class clone() method is called and then the
TS objects are cloned.  The result is a complete deep copy for all object
except the reference to the associated time series.
*/
public Object clone ()
{	try {
		// Clone the base class...
		TSLimits limits = (TSLimits)super.clone();
		limits.__ts = __ts;
		if ( __date1 != null ) {
			limits.__date1 = (DateTime)__date1.clone();
		}
		if ( __date2 != null ) {
			limits.__date2 = (DateTime)__date2.clone();
		}
		if ( __max_value_date != null ) {
			limits.__max_value_date = (DateTime)__max_value_date.clone();
		}
		if ( __min_value_date != null ) {
			limits.__min_value_date = (DateTime)__min_value_date.clone();
		}
		if ( __non_missing_data_date1 != null ) {
			limits.__non_missing_data_date1 = (DateTime)__non_missing_data_date1.clone();
		}
		if ( __non_missing_data_date2 != null ) {
			limits.__non_missing_data_date2 = (DateTime)__non_missing_data_date2.clone();
		}
		return limits;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	__ts = null;
	__date1 = null;
	__date2 = null;
	__max_value_date = null;
	__min_value_date = null;
	__non_missing_data_date1 = null;
	__non_missing_data_date2 = null;
	super.finalize();
}

/**
Return the data units for the data limits.
*/
public String getDataUnits ()
{	return __data_units;
}

/**
Return the first date for the time series according to the memory allocation.
@return The first date for the time series according to the memory allocation.
A copy of the date is returned.
*/
public DateTime getDate1 ()
{	if ( __date1 == null ) {
		return __date1;
	}
	return new DateTime ( __date1 );
}

/**
Return the last date for the time series according to the memory allocation.
@return The last date for the time series according to the memory allocation.
A copy of the date is returned.
*/
public DateTime getDate2 ()
{	if ( __date2 == null ) {
		return __date2;
	}
	return new DateTime ( __date2 );
}

/**
Return the maximum data value for the time series.
@return The maximum data value for the time series.
*/
public double getMaxValue ()
{	return __max_value;
}

/**
Return the date corresponding to the maximum data value for the time series.
@return The date corresponding to the maximum data value for the time series.
A copy of the date is returned.
*/
public DateTime getMaxValueDate ()
{	if ( __max_value_date == null ) {
		return __max_value_date;
	}
	return new DateTime ( __max_value_date );
}

/**
Return the mean data value for the time series.
@return The mean data value for the time series.
*/
public double getMean ()
{	return __mean;
}

/**
Return the median data value for the time series.
@return The median data value for the time series, or NaN if not computed.
*/
public double getMedian ()
{   return __median;
}

/**
Return the minimum data value for the time series.
@return The minimum data value for the time series.
*/
public double getMinValue ()
{	return __min_value;
}

/**
Return the date corresponding to the minimum data value for the time series.
@return The date corresponding to the minimum data value for the time series.
A copy of the date is returned.
*/
public DateTime getMinValueDate ()
{	if ( __min_value_date == null ) {
		return __min_value_date;
	}
	return new DateTime ( __min_value_date );
}

/**
Return the count for the number of missing data in the time series.
@return The count for the number of missing data in the time series.
*/
public int getMissingDataCount ()
{	return __missing_data_count;
}

/**
Return the count for the number of non-missing data in the time series.
@return The count for the number of non-missing data in the time series.
*/
public int getNonMissingDataCount ()
{	return __non_missing_data_count;
}

/**
Return the date corresponding to the first non-missing data in the time series.
@return The date corresponding to the first non-missing data in the time series.
A copy of the date is returned.
*/
public DateTime getNonMissingDataDate1 ()
{	if ( __non_missing_data_date1 == null ) {
		return __non_missing_data_date1;
	}
	return new DateTime ( __non_missing_data_date1 );
}

/**
Return the date corresponding to the last non-missing data in the time series.
@return The date corresponding to the last non-missing data in the time series.
A copy of the date is returned.
*/
public DateTime getNonMissingDataDate2 ()
{	if ( __non_missing_data_date2 == null ) {
		return __non_missing_data_date2;
	}
	return new DateTime ( __non_missing_data_date2 );
}

/**
Return the sum of non-missing data values for the time series.
@return The sum of non-missing data values for the time series.
*/
public double getSum ()
{	return __sum;
}

/**
Return the time series that is associated with these limits.
@return the time series that is associated with these limits.
*/
public TS getTimeSeries()
{	return __ts;
}

/**
Determine whether non-missing data are available.
@return true if time series has non-missing data, false if not.
*/
public boolean hasNonMissingData ()
{	if ( __non_missing_data_count > 0 ) {
		return true;
	}
	return false;
}

/**
Return the skew for the time series.
@return The skew for the time series, or NaN if not computed.
*/
public double getSkew ()
{   return __skew;
}

/**
Return the standard deviation for the time series.
@return The standard deviation for the time series, or NaN if not computed.
*/
public double getStdDev ()
{   return __stdDev;
}

/**
Initialize the data.
Need to rework code to use an instance of TS so we can initialize to missing
data values used by the time series!
*/
private void initialize ()
{	__ts = null;
	__data_units = "";
	__date1 = null;
	__date2 = null;
	_flags = 0;
	__max_value = 0.0;
	__max_value_date = null;
	__mean = -999.0; // Assume.
	__median = Double.NaN; // Assume.
	__min_value = 0.0;
	__min_value_date = null;
	__missing_data_count = 0;
	__non_missing_data_count = 0;
	__non_missing_data_date1 = null;
	__non_missing_data_date2 = null;
	__skew = Double.NaN;
	__stdDev = Double.NaN;
	__sum = -999.0;		// Assume.
	__found = false;
}

/**
Set the first date for the time series.  This is used for memory allocation.
@param date1 The first date for the time series.
@see TS#allocateDataSpace
*/
public void setDate1 ( DateTime date1 )
{	if ( date1 != null ) {
		__date1 = new DateTime ( date1 );
	}
	checkDates();
}

/**
Set the last date for the time series.  This is used for memory allocation.
@param date2 The last date for the time series.
@see TS#allocateDataSpace
*/
public void setDate2 ( DateTime date2 )
{	if ( date2 != null ) {
		__date2 = new DateTime ( date2 );
	}
	checkDates();
}

/**
Set whether the limits have been found.  This is mainly used by routines in
the package when only partial limits are needed (as opposed to checkDates(),
which checks all the dates in a TSLimits).  Call this method after any methods
that set the date to offset the check done by checkDates().
@param flag Indicates whether the limits have been found (true or false).
*/
protected void setLimitsFound ( boolean flag )
{	__found = flag;
}

/**
Set the maximum data value for the time series.
@param max_value The maximum data value.
*/
public void setMaxValue ( double max_value )
{	__max_value = max_value;
}

/**
Set the maximum data value for the time series.
@param max_value The maximum data value.
@param max_value_date The date corresponding to the maximum data value.
*/
public void setMaxValue ( double max_value, DateTime max_value_date )
{	__max_value = max_value;
	setMaxValueDate ( max_value_date );
}

/**
Set the date corresponding to the maximum data value for the time series.
@param max_value_date The date corresponding to the maximum data value.
*/
public void setMaxValueDate ( DateTime max_value_date )
{	if ( max_value_date != null ) {
		__max_value_date = new DateTime ( max_value_date );
	}
	checkDates();
}

/**
Set the mean data value for the time series.
@param mean The mean data value.
*/
public void setMean ( double mean )
{	__mean = mean;
}

/**
Set the median data value for the time series.
@param median The median data value.
*/
public void setMedian ( double median )
{   __median = median;
}

/**
Set the minimum data value for the time series.
@param min_value The minimum data value.
*/
public void setMinValue ( double min_value )
{	__min_value = min_value;
}

/**
Set the minimum data value for the time series.
@param min_value The minimum data value.
@param min_value_date The date corresponding to the minimum data value.
*/
public void setMinValue ( double min_value, DateTime min_value_date )
{	__min_value = min_value;
	setMinValueDate ( min_value_date );
}

/**
Set the date corresponding to the minimum data value for the time series.
@param min_value_date The date corresponding to the minimum data value.
*/
public void setMinValueDate ( DateTime min_value_date )
{	if ( min_value_date != null ) {
		__min_value_date = new DateTime ( min_value_date );
	}
	checkDates();
}

/**
Set the counter for missing data.
@param missing_data_count The number of missing data in the time series.
*/
public void setMissingDataCount ( int missing_data_count )
{	__missing_data_count = missing_data_count;
}

/**
Set the counter for non-missing data.
@param non_missing_data_count The number of non-missing data in the time series.
*/
public void setNonMissingDataCount ( int non_missing_data_count )
{	__non_missing_data_count = non_missing_data_count;
}

/**
Set the date for the first non-missing data value.
@param date The date for the first non-missing data value.
*/
public void setNonMissingDataDate1 ( DateTime date )
{	if ( date != null ) {
		__non_missing_data_date1 = new DateTime ( date );
	}
	checkDates();
}

/**
Set the date for the last non-missing data value.
@param date The date for the last non-missing data value.
*/
public void setNonMissingDataDate2 ( DateTime date )
{	if ( date != null ) {
		__non_missing_data_date2 = new DateTime ( date );
	}
	checkDates();
}

/**
Set the skew for the time series.
@param skew The skew data value.
*/
public void setSkew ( double skew )
{   __skew = skew;
}

/**
Set the standard deviation for the time series.
@param stdDev The standard deviation.
*/
public void setStdDev ( double stdDev )
{   __stdDev = stdDev;
}

/**
Set the sum of data values for the time series.
@param sum The sum of data value.
*/
public void setSum ( double sum )
{	__sum = sum;
}

/**
Set the time series.  No analysis occurs after the set.  This method is most
often used by the derived classes.
@param ts The time series.
*/
public void setTimeSeries ( TS ts )
{	__ts = ts;
}

/**
Return a string representation.
@return A verbose string representation of the limits.
*/
public String toString ( )
{	String units = "";
	if ( __data_units.length() > 0 ) {
		units = " " + __data_units;
	}
	double missing_percent = 0.0;
	double non_missing_percent = 0.0;
	if ( (__missing_data_count + __non_missing_data_count) > 0 ) {
		missing_percent = 100.0*(double)__missing_data_count/
				(double)(__missing_data_count + __non_missing_data_count);
		non_missing_percent = 100.0*(double)__non_missing_data_count/
				(double)(__missing_data_count + __non_missing_data_count);
	}
	return 
	"Min:  " + StringUtil.formatString(__min_value,"%20.4f") + units + " on " + __min_value_date + "\n" +
	"Max:  " + StringUtil.formatString(__max_value,"%20.4f") + units + " on " + __max_value_date + "\n" +
	"Sum:  " + StringUtil.formatString(__sum,"%20.4f") + units + "\n" +
	"Mean: " + StringUtil.formatString(__mean,"%20.4f") + units + "\n" +
	"Median: " + StringUtil.formatString(__median,"%20.4f") + units + "\n" +
	"StdDev: " + StringUtil.formatString(__stdDev,"%20.4f") + units + "\n" +
	"Skew: " + StringUtil.formatString(__skew,"%20.4f") + units + "\n" +
	"Number Missing:     " + __missing_data_count + " (" +
			StringUtil.formatString(missing_percent,"%.2f")+"%)\n" +
	"Number Not Missing: " + __non_missing_data_count + " (" +
			StringUtil.formatString(non_missing_percent,"%.2f") + "%)\n" +
	"Total period: " + __date1 + " to " + __date2 + "\n" +
	"Non-missing data period: " + __non_missing_data_date1 + " to " + __non_missing_data_date2;
}

}