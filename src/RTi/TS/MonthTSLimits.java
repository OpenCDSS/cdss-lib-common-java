// ----------------------------------------------------------------------------
// MonthTSLimits - simple class for returning time series data limits
// ----------------------------------------------------------------------------
// Notes:	(1)	This class stores the data limits for data space.
//			It stores the maximum and minimum values and the dates
//			associated with the values.
//		(2)	This class extends the TSLimits class.  Monthly time
//			series are meant to have the overall limits and limits
//			by month.
// ----------------------------------------------------------------------------
// History:
//
// 30 Dec 1998	Steven A. Malers, RTi	Initial version.  Extend TSLimits.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2003-03-25	SAM, RTi		Update to use new data units member
//					in the base class, to deal with current
//					and original units better.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
// 2004-05-23	SAM, RTi		* Add percent to the toString() output
//					  for missing and non-missing
//					  (requested by Leonard Rice and makes
//					  sense since other intervals show it
//					  in output).
// 2005-02-04	SAM, RTi		Add merge() to merge two MonthTSLimits()
//					together.
//					The toString() method for missing
//					monthly data was not handling null min
//					and max dates for the period - fix it.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;

/**
This class stores information about the data and date limits of a monthly time series.
If detailed information is not necessary, use the TSLimits class for overall limits.
In summary, since this class extends TSLimits, both full period limits and limits by month are computed.
*/
@SuppressWarnings("serial")
public class MonthTSLimits extends TSLimits
{

// TODO SAM 2005-02-07 This also seems to be in the base class!  Need to use
// the single copy.  Only merge() seems to use both.
// Time series that is being studied...

MonthTS _ts = null;

// Data are by month (12 values)...

private double [] _max_value_by_month = null;
private DateTime [] _max_value_date_by_month = null;
private double [] _mean_by_month = null;
private double [] __medianByMonth = null;
private double [] _min_value_by_month = null;
private DateTime [] _min_value_date_by_month = null;
private int [] _missing_data_count_by_month = null;
private int [] _non_missing_data_count_by_month = null;
private DateTime [] _non_missing_data_date1_by_month = null;
private DateTime [] _non_missing_data_date2_by_month = null;
private double [] __skewByMonth = null;
private double [] __stdDevByMonth = null;
private double [] _sum_by_month = null;

/**
Default constructor.  Initialize the dates to null and the limits to zeros.
@exception TSException if there is error an creating the limits.
*/
public MonthTSLimits ()
throws TSException
{	super ();
	try {
		initialize ();
	}
	catch ( Exception e ) {
		String message = "Error creating MonthTSLimits";
		Message.printWarning ( 3, "MonthTSLimits", message );
		throw new TSException ( message );
	}
}

/**
Copy constructor.
@param limits Instance to copy.
@exception TSException if there is an error c creating the limits.
*/
public MonthTSLimits ( MonthTSLimits limits )
throws TSException
{	super ();
	try {
		initialize ();
	
		for ( int i = 0; i < 12; i++ ) {
			_max_value_by_month[i] = limits.getMaxValue ( i + 1 );
			_min_value_by_month[i] = limits.getMaxValue ( i + 1 );
			_max_value_date_by_month[i] = new DateTime ( limits.getMaxValueDate ( i + 1 ) );
			_min_value_date_by_month[i] = new DateTime ( limits.getMinValueDate ( i + 1 ) );
			_mean_by_month[i] = limits.getMean ( i + 1 );
			__medianByMonth[i] = limits.getMedian ( i + 1 );
			_missing_data_count_by_month[i] = limits.getMissingDataCount ( i + 1 );
			_non_missing_data_count_by_month[i] = limits.getNonMissingDataCount ( i + 1 );
			_non_missing_data_date1_by_month[i] = new DateTime ( limits.getNonMissingDataDate1 ( i + 1 ) );
			_non_missing_data_date2_by_month[i] = new DateTime ( limits.getNonMissingDataDate2 ( i + 1 ) );
			__skewByMonth[i] = limits.getSkew ( i + 1 );
			__stdDevByMonth[i] = limits.getStdDev ( i + 1 );
			_sum_by_month[i] = limits.getSum ( i + 1 );
		}
	}
	catch ( Exception e ) {
		String message = "Error creating MonthTSLimits";
		Message.printWarning ( 2, "MonthTSLimits.copy", message );
		throw new TSException ( message );
	}
}

/**
Constructor to compute the limits given a MonthTS.  This is the main constructor
and is overloaded in a variety of ways.  If a variant of this
constructor that does not take a MonthTS is used, the limits are not computed
in this class and must be set from calling code.
@param ts Time series of interest.
@param startdate Starting date for the check.
@param enddate Ending date for the check.
@param flags Indicates special operations performed during computations.
See the TSLimits.REFRESH_TS, TSLimits.NO_COMPUTE_DETAIL, and
TSLimits.NO_COMPUTE_TOTALS flags for explanations.
@exception TSException if there is an error c creating the limits.
*/
public MonthTSLimits ( MonthTS ts, DateTime startdate, DateTime enddate, int flags )
throws TSException
{	// Compute the total limits...
	super ( ts, startdate, enddate, flags );
	try {
		// Compute the monthly limits...
		initialize ();
		_ts = ts;
		boolean refresh_flag = false;
		if ( (flags & TSLimits.REFRESH_TS) != 0 ) {
			refresh_flag = true;
		}
		calculateDataLimits ( ts, startdate, enddate, refresh_flag );
	}
	catch ( Exception e ) {
		String message = "Error creating MonthTSLimits (" + e + ")";
		Message.printWarning ( 3, "MonthTSLimits(MonthTS,...)", message);
		throw new TSException ( message );
	}
}

/**
Construct the MonthTS limits for the full period.
@param ts Time series of interest.
@exception TSException if there is an error computing the limits.
*/
public MonthTSLimits ( MonthTS ts )
throws TSException
{	// Compute the total limits...
	super ( ts );
	try {
		// Compute the monthly limits...
		initialize ();
		_ts = ts;
		calculateDataLimits ( ts, (DateTime)null, (DateTime)null, false );
	}
	catch ( Exception e ) {
		String message = "Error creating MonthTSLimits";
		Message.printWarning ( 2, "MonthTSLimits(MonthTS)", message );
		throw new TSException ( message );
	}
}

/**
Construct the MonthTS limits between two dates.
@param ts Time series of interest.
@param startdate Starting date for the check.
@param enddate Ending date for the check.
@exception TSException if there is an error computing the limits.
*/
public MonthTSLimits ( MonthTS ts, DateTime startdate, DateTime enddate )
throws TSException
{	// Compute the total limits...
	super ( ts, startdate, enddate );
	try {
		// Compute the monthly limits...
		initialize ();
		_ts = ts;
		calculateDataLimits ( ts, startdate, enddate, false );
	}
	catch ( Exception e ) {
		String message = "Error creating MonthTSLimits";
		Message.printWarning ( 2, "MonthTSLimits(MonthTS)", message );
		throw new TSException ( message );
	}
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	_ts = null;
	for ( int i = 0; i < 12; i++ ) {
		_max_value_date_by_month[i] = null;
		_min_value_date_by_month[i] = null;
		_non_missing_data_date1_by_month[i] = null;
		_non_missing_data_date2_by_month[i] = null;
	}
	_max_value_by_month = null;
	_min_value_by_month = null;
	_max_value_date_by_month = null;
	_min_value_date_by_month = null;
	_mean_by_month = null;
	_missing_data_count_by_month = null;
	_non_missing_data_count_by_month = null;
	_non_missing_data_date1_by_month = null;
	_non_missing_data_date2_by_month = null;
	_sum_by_month = null;
	super.finalize();
}

/**
Compute the monthly data limits for a monthly time series between two dates.
This code was taken from the TSUtil.getDataLimits method.  This method should
be private.  Otherwise, the base class may call this method when it really needs to call its own version.
@param ts Time series of interest.
@param start0 Starting date for the check.
@param end0 Ending date for the check.
@param refresh_flag Indicates whether the time series should be refreshed first
(in general this is used only within the TS package and the version of this
routine without the flag should be called).
@see TSLimits
@exception TSException if there is an error computing the detailed limits.
*/
private void calculateDataLimits ( TS ts, DateTime start0, DateTime end0, boolean refresh_flag )
throws TSException
{	String message, routine="MonthTSLimits.getDataLimits";
	double value = 0.0;
	double [] max_by_month = null, min_by_month = null, sum_by_month = null;
	int base=0, month_index = 0, mult = 0;
	int [] missing_count_by_month = null, non_missing_count_by_month = null;
	boolean found = false;
	boolean [] found_by_month = null;
	DateTime date, t = null;
	DateTime []	max_date_by_month = null, min_date_by_month = null,
		non_missing_data_date1_by_month = null, non_missing_data_date2_by_month = null;

	try {
		// Overall try...

		if ( ts == null ) {
			message = "NULL time series";
			Message.printWarning ( 3, routine, message );
			throw new TSException ( message );
		}
	
		// Get valid date limits because the ones passed in may have been null...
	
		double missing = ts.getMissing();
		TSLimits valid_dates = TSUtil.getValidPeriod ( ts, start0, end0 );
		DateTime start = valid_dates.getDate1();
		DateTime end = valid_dates.getDate2();
		valid_dates = null;
	
		// Make sure that the time series has current limits...
	
		base = ts.getDataIntervalBase();
		mult = ts.getDataIntervalMult();
	
		// Get the variables that are used often in this function.
	
		// Loop through the dates and get max and min data values;
	
		// A regular TS... easier to iterate...
	
		found_by_month = new boolean[12];
		max_by_month = new double[12];
		max_date_by_month = new DateTime[12];
		min_by_month = new double[12];
		min_date_by_month = new DateTime[12];
		missing_count_by_month = new int[12];
		non_missing_data_date1_by_month = new DateTime[12];
		non_missing_data_date2_by_month = new DateTime[12];
		non_missing_count_by_month = new int[12];
		sum_by_month = new double[12];
		for ( int i = 0; i < 12; i++ ) {
			found_by_month[i] = false;
			max_by_month[i] = missing;
			max_date_by_month[i] = null;
			min_by_month[i] = missing;
			min_date_by_month[i] = null;
			missing_count_by_month[i] = 0;
			non_missing_data_date1_by_month[i] = null;
			non_missing_data_date2_by_month[i] = null;
			non_missing_count_by_month[i] = 0;
			sum_by_month[i] = missing;
		}
	
		// Figure out if we are treating data <= 0 as missing...
	
		boolean ignore_lezero = false;
		if ( (_flags & IGNORE_LESS_THAN_OR_EQUAL_ZERO) != 0 ) {
			ignore_lezero = true;
		}
	
		// First loop through and find the data limits and the
		// minimum non-missing date...
	
		t = new DateTime ( start, DateTime.DATE_FAST );
		for ( ; t.lessThanOrEqualTo(end); t.addInterval( base, mult )) {
			// Save the month index...
			month_index = t.getMonth() - 1;
	
			value = ts.getDataValue( t );
			
			if ( ts.isDataMissing(value) || (ignore_lezero && (value <= 0.0)) ) {
				// The value is missing
				++missing_count_by_month[month_index];
	            continue;
			}
	
			// Else, data value is not missing...
	
			if ( ts.isDataMissing(sum_by_month[month_index]) ) {
				// Reset the sum...
				sum_by_month[month_index] = value;
			}
			else {
				// Add to the sum...
				sum_by_month[month_index] += value;
			}
			++non_missing_count_by_month[month_index];
	
			if ( found_by_month[month_index] ) {
				// Already found the first non-missing point so
				// all we need to do is check the limits.  These
				// should only result in new DateTime a few times...
				if ( value > max_by_month[month_index] ) {
	               	max_by_month[month_index] = value;
					max_date_by_month[month_index] = new DateTime ( t );
				}
				if ( value < min_by_month[month_index] ) {
	               	min_by_month[month_index] = value;
					min_date_by_month[month_index] = new DateTime ( t );
	            }
			}
			else {
				// First non-missing point so set the initial values...
				date = new DateTime( t );
				max_by_month[month_index] = value;
				max_date_by_month[month_index] = date;
				min_by_month[month_index] = value;
				min_date_by_month[month_index] = date;
				non_missing_data_date1_by_month[month_index] = date;
				non_missing_data_date2_by_month[month_index] = date;
				found_by_month[month_index] = true;
				found = true;	// Overall check.
			}
		}
	
		if( !found ){
			message = "\"" + ts.getIdentifierString() + "\": problems finding limits, whole POR missing!";
			Message.printWarning( 2, routine, message );
			throw new TSException ( message );
		}
	
		// Set the monthly values...
	
		double mean;
		for ( int i = 1; i <= 12; i++ ) {
			setMaxValue( i, max_by_month[i - 1], max_date_by_month[i - 1]);
			setMinValue( i, min_by_month[i - 1], min_date_by_month[i - 1]);
			setNonMissingDataDate1(i, non_missing_data_date1_by_month[i-1]);
			setNonMissingDataDate2(i, non_missing_data_date2_by_month[i-1]);
			setMissingDataCount ( i, missing_count_by_month[i - 1] );
			setNonMissingDataCount ( i, non_missing_count_by_month[i - 1] );
			if ( (
				(!ignore_lezero && !ts.isDataMissing (sum_by_month[i - 1])) ||
				(ignore_lezero && ((sum_by_month[i - 1] > 0.0) &&
				!ts.isDataMissing (sum_by_month[i - 1]))) )
				&& (non_missing_count_by_month[i - 1] > 0) ) {
				mean = sum_by_month[i - 1]/(double)non_missing_count_by_month[i-1];
			}
			else {
				mean = missing;
			}
			setSum ( i, sum_by_month[i - 1] );
			setMean ( i, mean );
			
			// TODO SAM 2010-06-15 This is a performance hit, but not too bad
	        // TODO SAM 2010-06-15 Consider treating other statistics similarly but need to define unit tests
	        // TODO SAM 2010-06-15 This code would need to be changed if doing Lag-1 correlation because order matters
	        // For newly added statistics, use helper method to get data, ignoring missing...
	        double [] dataArray = TSUtil.toArray(ts, start, end, i, false );
	        // Check for <= 0 values if necessary
	        int nDataArray = dataArray.length;
	        if ( ignore_lezero ) {
	            for ( int iData = 0; iData < nDataArray; iData++ ) {
	                if ( dataArray[iData] <= 0.0 ) {
	                    // Just exchange with the last value and reduce the size
	                    double temp = dataArray[iData];
	                    dataArray[iData] = dataArray[nDataArray - 1];
	                    dataArray[nDataArray - 1] = temp;
	                    --nDataArray;
	                }
	            }
	        }
	        if ( nDataArray > 0 ) {
	            setMedian ( i, MathUtil.median (nDataArray, dataArray) );
	        }
	        if ( nDataArray > 1 ) {
	            // At least 2 values required
	            try {
	                setStdDev ( i, MathUtil.standardDeviation(nDataArray, dataArray) );
	            }
	            catch ( Exception e ) {
	                // Likely due to small sample size
	            }
	        }
	        if ( nDataArray > 2 ) {
	            try {
	                setSkew ( i, MathUtil.skew(nDataArray, dataArray) );
	            }
                catch ( Exception e ) {
                    // Likely due to small sample size
                }
	        }
		}
	}
	catch ( Exception e ) {
		message = "Error computing data limits.";
		Message.printWarning ( 3, routine, message );
		Message.printWarning ( 3, routine, e );
		throw new TSException ( message );
	}
}

/**
Return The maximum data value for the indicated month.
@return The maximum data value for the indicated month, or -999 if the month is invalid.
@param month Month of interest (1-12).
*/
public double getMaxValue ( int month )
{	if ( (month >= 1) && (month <= 12) ) {
		return _max_value_by_month[month - 1];
	}
	else {
		return -999.0;
	}
}

/**
Return the date corresponding to the maximum data value for the indicated month.
@return The date corresponding to the maximum data value for the indicated
month, or null if the month is invalid.  A copy of the date is returned.
@param month Month of interest (1-12).
*/
public DateTime getMaxValueDate ( int month )
{	if ( (month >= 1) && (month <= 12) ) {
		if ( _max_value_date_by_month[month - 1] == null ) {
			return _max_value_date_by_month[month - 1];
		}
		return new DateTime ( _max_value_date_by_month[month - 1] );
	}
	else {
		return null;
	}
}

/**
Return the mean data value for the indicated month.
@return The mean data value for the indicated month, or -999 if the month is invalid.
@param month Month of interest (1-12).
*/
public double getMean ( int month )
{	if ( (month >= 1) && (month <= 12) ) {
		return _mean_by_month[month - 1];
	}
	else {
		return -999.0;
	}
}

/**
Return the mean data value array.
@return The mean data value array (12 monthly values with the first value
corresponding to January).  This can be used, for example, to pass the
values to the TSUtil.fillConstant method to a fill the missing data in a
time series with missing data.  The array will be null if a time series has not been analyzed.
*/
public double [] getMeanArray ()
{	return _mean_by_month;
}

/**
Return the median data value for the indicated month.
@return The median data value for the indicated month, or -999 if the month is invalid.
@param month Month of interest (1-12).
*/
public double getMedian ( int month )
{   if ( (month >= 1) && (month <= 12) ) {
        return __medianByMonth[month - 1];
    }
    else {
        return -999.0;
    }
}

/**
Return the median data value array.
@return The median data value array (12 monthly values with the first value
corresponding to January).  The array will be null if a time series has not been analyzed.
*/
public double [] getMedianArray ()
{   return __medianByMonth;
}

/**
Return the minimum data value for the indicated month.
@return The minimum data value for the indicated month, or -999 if the month is invalid.
@param month Month of interest (1-12).
*/
public double getMinValue ( int month )
{	if ( (month >= 1) && (month <= 12) ) {
		return _min_value_by_month[month - 1];
	}
	else {
		return -999.0;
	}
}

/**
Return the date corresponding to the minimum data value for the indicated month.
@return The date corresponding to the minimum data value for the indicated
month, or null if the month is invalid.  A copy is returned.
@param month Month of interest (1-12).
*/
public DateTime getMinValueDate ( int month )
{	if ( (month >= 1) && (month <= 12) ) {
		if ( _min_value_date_by_month[month - 1] == null ) {
			return _min_value_date_by_month[month - 1];
		}
		return new DateTime ( _min_value_date_by_month[month - 1] );
	}
	else {
		return null;
	}
}

/**
Return the count for the number of missing data for the indicated month.
@return The count for the number of missing data for the indicated month, or -1 if month is invalid.
@param month Month of interest (1-12).
*/
public int getMissingDataCount ( int month )
{	if ( (month >= 1) && (month <= 12) ) {
		return _missing_data_count_by_month[month - 1];
	}
	else {
		return -1;
	}
}

/**
Return the count for the number of non-missing data for the indicated month.
@return The count for the number of non-missing data for the indicated month, or -1 if month is invalid.
@param month Month of interest (1-12).
*/
public int getNonMissingDataCount ( int month )
{	if ( (month >= 1) && (month <= 12) ) {
		return _non_missing_data_count_by_month[month - 1];
	}
	else {
		return -1;
	}
}

/**
Return the date corresponding to the first non-missing data for the indicated month.
@return The date corresponding to the first non-missing data for the indicated
month.  A copy of the date is returned.
@param month Month of interest (1-12).
*/
public DateTime getNonMissingDataDate1 ( int month )
{	if ( (month >= 1) && (month <= 12) ) {
		if ( _non_missing_data_date1_by_month[month - 1] == null ) {
			return _non_missing_data_date1_by_month[month - 1];
		}
		return new DateTime (_non_missing_data_date1_by_month[month-1]);
	}
	else {
		return null;
	}
}

/**
Return the date corresponding to the last non-missing data for the indicated month.
@return The date corresponding to the last non-missing data for the indicated
month or null if month is out of range.  A copy of the date is returned.
@param month Month of interest (1-12).
*/
public DateTime getNonMissingDataDate2 ( int month )
{	if ( (month >= 1) && (month <= 12) ) {
		if ( _non_missing_data_date2_by_month[month - 1] == null ) {
			return _non_missing_data_date2_by_month[month - 1];
		}
		return new DateTime(_non_missing_data_date2_by_month[month -1]);
	}
	else {
		return null;
	}
}

/**
Return the skew for the indicated month.
@return The skew for the indicated month, or -999 if the month is invalid.
@param month Month of interest (1-12).
*/
public double getSkew ( int month )
{   if ( (month >= 1) && (month <= 12) ) {
        return __skewByMonth[month - 1];
    }
    else {
        return -999.0;
    }
}

/**
Return the skew array.
@return The skew array (12 monthly values with the first value
corresponding to January).  The array will be null if a time series has not been analyzed.
*/
public double [] getSkewArray ()
{   return __skewByMonth;
}

/**
Return the standard deviation for the indicated month.
@return The standard deviation for the indicated month, or -999 if the month is invalid.
@param month Month of interest (1-12).
*/
public double getStdDev ( int month )
{   if ( (month >= 1) && (month <= 12) ) {
        return __stdDevByMonth[month - 1];
    }
    else {
        return -999.0;
    }
}

/**
Return the standard deviation array.
@return The standard deviation array (12 monthly values with the first value
corresponding to January).  The array will be null if a time series has not been analyzed.
*/
public double [] getStdDevArray ()
{   return __stdDevByMonth;
}

/**
Return the sum for the indicated month.
@return The sum for the indicated month, or -999 if the month is invalid.
@param month Month of interest (1-12).
*/
public double getSum ( int month )
{	if ( (month >= 1) && (month <= 12) ) {
		return _sum_by_month[month - 1];
	}
	else {
		return -999.0;
	}
}

/**
Initialize the instance.
*/
private void initialize ()
{	_ts = null;	// No time series specified in constructor.
	_max_value_by_month = new double[12];
	_min_value_by_month = new double[12];
	_max_value_date_by_month = new DateTime[12];
	_min_value_date_by_month = new DateTime[12];
	_mean_by_month = new double[12];
	__medianByMonth = new double[12];
	_missing_data_count_by_month = new int[12];
	_non_missing_data_count_by_month = new int[12];
	_non_missing_data_date1_by_month = new DateTime[12];
	_non_missing_data_date2_by_month = new DateTime[12];
	__skewByMonth = new double[12];
	__stdDevByMonth = new double[12];
	_sum_by_month = new double[12];
	for ( int i = 0; i < 12; i++ ) {
		_max_value_by_month[i] = 0.0;
		_min_value_by_month[i] = 0.0;
		_max_value_date_by_month[i] = null;
		_min_value_date_by_month[i] = null;
		__medianByMonth[i] = -999.0;
		_mean_by_month[i] = -999.0;
		_missing_data_count_by_month[i] = 0;
		_non_missing_data_count_by_month[i] = 0;
		_non_missing_data_date1_by_month[i] = null;
		_non_missing_data_date2_by_month[i] = null;
		__skewByMonth[i] = -999.0;
		__stdDevByMonth[i] = -999.0;
		_sum_by_month[i] = -999.0;
	}
}

/**
Set the maximum data value for the indicated month.
@param month Month of interest (1-12).
@param max_value The maximum data value.
*/
public void setMaxValue ( int month, double max_value )
{	if ( (month >= 1) && (month <= 12) ) {
		_max_value_by_month[month - 1] = max_value;
	}
}

/**
Set the maximum data value for the indicated month.
@param month Month of interest (1-12).
@param max_value The maximum data value.
@param max_value_date The date corresponding to the maximum data value.
*/
public void setMaxValue ( int month, double max_value, DateTime max_value_date )
{	if ( (month >= 1) && (month <= 12) ) {
		_max_value_by_month[month - 1] = max_value;
		setMaxValueDate ( month, max_value_date );
	}
}

/**
Set the date corresponding to the maximum data value for the indicated month.
@param month Month of interest (1-12).
@param max_value_date The date corresponding to the maximum data value.
*/
public void setMaxValueDate ( int month, DateTime max_value_date )
{	if ( (max_value_date != null) && (month >= 1) && (month <= 12) ) {
		_max_value_date_by_month[month - 1] =
			new DateTime ( max_value_date );
	}
	//checkDates();
}

/**
Set the mean for data for the indicated month.
@param month Month of interest (1-12).
@param mean The mean for the month.
*/
public void setMean ( int month, double mean )
{	if ( (month >= 1) && (month <= 12) ) {
		_mean_by_month[month - 1] = mean;
	}
}

/**
Set the median for data for the indicated month.
@param month Month of interest (1-12).
@param median The median for the month.
*/
public void setMedian ( int month, double median )
{   if ( (month >= 1) && (month <= 12) ) {
        __medianByMonth[month - 1] = median;
    }
}

/**
Set the minimum data value for the indicated month.
@param month Month of interest (1-12).
@param min_value The minimum data value.
*/
public void setMinValue ( int month, double min_value )
{	if ( (month >= 1) && (month <= 12) ) {
		_min_value_by_month[month - 1] = min_value;
	}
}

/**
Set the minimum data value for the month of interest.
@param month Month of interest (1-12).
@param min_value The minimum data value.
@param min_value_date The date corresponding to the minimum data value.
*/
public void setMinValue ( int month, double min_value, DateTime min_value_date )
{	if ( (month >= 1) && (month <= 12) ) {
		_min_value_by_month[month - 1] = min_value;
		setMinValueDate ( month, min_value_date );
	}
}

/**
Set the date corresponding to the minimum data value for the month of interest.
@param month Month of interest (1-12).
@param min_value_date The date corresponding to the minimum data value.
*/
public void setMinValueDate ( int month, DateTime min_value_date )
{	if ( (month >= 1) && (month <= 12) && (min_value_date != null) ) {
		_min_value_date_by_month[month - 1] = new DateTime ( min_value_date );
	}
	//checkDates();
}

/**
Set the counter for missing data.
@param month Month of interest (1-12).
@param missing_data_count The number of missing data in the time series.
*/
public void setMissingDataCount ( int month, int missing_data_count )
{	if ( (month >= 1) && (month <= 12) ) {
		_missing_data_count_by_month[month - 1] = missing_data_count;
	}
}

/**
Set the counter for non-missing data.
@param month Month of interest (1-12).
@param non_missing_data_count The number of non-missing data in the time series.
*/
public void setNonMissingDataCount ( int month, int non_missing_data_count )
{	if ( (month >= 1) && (month <= 12) ) {
		_non_missing_data_count_by_month[month - 1] = non_missing_data_count;
	}
}

/**
Set the date for the first non-missing data value.
@param month Month of interest (1-12).
@param date The date for the first non-missing data value.
*/
public void setNonMissingDataDate1 ( int month, DateTime date )
{	if ( (date != null) && (month >= 1) && (month <= 12) ) {
		_non_missing_data_date1_by_month[month - 1] =
			new DateTime ( date );
	}
	//checkDates();
}

/**
Set the date for the last non-missing data value.
@param month Month of interest (1-12).
@param date The date for the last non-missing data value.
*/
public void setNonMissingDataDate2 ( int month, DateTime date )
{	if ( (date != null) && (month >= 1) && (month <= 12) ) {
		_non_missing_data_date2_by_month[month - 1] = new DateTime ( date );
	}
	//checkDates();
}

/**
Set the skew for data for the indicated month.
@param month Month of interest (1-12).
@param skew The skew for the month.
*/
public void setSkew ( int month, double skew )
{   if ( (month >= 1) && (month <= 12) ) {
        __skewByMonth[month - 1] = skew;
    }
}

/**
Set the standard deviation for data for the indicated month.
@param month Month of interest (1-12).
@param stdDev The skew for the month.
*/
public void setStdDev ( int month, double stdDev )
{   if ( (month >= 1) && (month <= 12) ) {
        __stdDevByMonth[month - 1] = stdDev;
    }
}

/**
Set the sum for data for the indicated month.
@param month Month of interest (1-12).
@param sum The sum for the month.
*/
public void setSum ( int month, double sum )
{	if ( (month >= 1) && (month <= 12) ) {
		_sum_by_month[month - 1] = sum;
	}
}

/**
Return a verbose string representation of the limits.
@return A verbose string representation of the limits.
*/
public String toString ( )
{	String nl = System.getProperty ( "line.separator" );

	StringBuffer buffer = new StringBuffer ( );
	if ( _ts != null ) {
		buffer.append ( "Time series:  " + _ts.getIdentifierString() + " (" + getDataUnits() + ")" +nl );
	}
	buffer.append (
	"Monthly limits for period " + getDate1() + " to " + getDate2() + " are:" + nl  +
	"                                                       #      %      # Not  % Not " + nl +
	"Month    Min    MinDate     Max    MaxDate     Sum     Miss.  Miss.  Miss.  Miss.     Mean  " +
	"    Median     StdDev      Skew"+ nl +
	"--------------------------------------------------------------------------------------------" +
	"---------------------------------" + nl );
	String date_string = null;
	int num_values = 0;		// Used for percents
	for ( int i = 0; i < 12; i++ ) {
		// Get the individual data that may be null...
		// Now format the output line...
		buffer.append ( TimeUtil.monthAbbreviation(i + 1) + "  " +
		StringUtil.formatString(_min_value_by_month[i],"%10.1f") + " ");
		if ( _min_value_date_by_month[i] == null ) {
			date_string = "       ";
		}
		else {
			date_string = _min_value_date_by_month[i].toString(DateTime.FORMAT_YYYY_MM);
		}
		buffer.append ( date_string + " " );
		buffer.append (
		StringUtil.formatString(_max_value_by_month[i],"%10.1f") + " ");
		if ( _max_value_date_by_month[i] == null ) {
			date_string = "       ";
		}
		else {
			date_string = _max_value_date_by_month[i].toString(DateTime.FORMAT_YYYY_MM);
		}
		buffer.append ( date_string + " " );
		buffer.append (
			StringUtil.formatString(_sum_by_month[i],"%10.1f")+" " +
			StringUtil.formatString(_missing_data_count_by_month[i],"%6d") + " " );
		num_values = _missing_data_count_by_month[i] + _non_missing_data_count_by_month[i];
		if ( num_values == 0 ) {
			buffer.append (StringUtil.formatString(100.0,"%6.2f")+" ");
		}
		else {
			buffer.append ( StringUtil.formatString((100.0*(double)_missing_data_count_by_month[i]/
			(double)num_values),"%6.2f")+" ");
		}
		buffer.append (
			StringUtil.formatString(_non_missing_data_count_by_month[i],"%6d") + " " );
		if ( num_values == 0 ) {
			buffer.append (StringUtil.formatString(0.0,"%6.2f")+" ");
		}
		else {
			buffer.append ( StringUtil.formatString(
			(100.0*(double)_non_missing_data_count_by_month[i]/(double)num_values),"%6.2f")+" ");
		}
		buffer.append ( StringUtil.formatString(_mean_by_month[i],"%10.1f") );
		buffer.append ( StringUtil.formatString(__medianByMonth[i]," %10.1f") );
		buffer.append ( StringUtil.formatString(__stdDevByMonth[i]," %10.2f") );
		buffer.append ( StringUtil.formatString(__skewByMonth[i]," %10.4f") + nl );
	}
	buffer.append ( "-----------------------------------------------------"+
	"------------------------------------------------------------------------" + nl );
	String mindate_string = "       ";
	String maxdate_string = "       ";
	if ( getMinValueDate() != null ) {
		mindate_string = getMinValueDate().toString(DateTime.FORMAT_YYYY_MM );
	}
	if ( getMaxValueDate() != null ) {
		maxdate_string = getMaxValueDate().toString(DateTime.FORMAT_YYYY_MM );
	}
	buffer.append (
		"Period"+
		StringUtil.formatString(getMinValue(),"%9.1f") + " " + mindate_string + " "+
		StringUtil.formatString(getMaxValue(),"%10.1f") + " " + maxdate_string + " " +
		StringUtil.formatString(getSum(),"%10.1f")+" " +
		StringUtil.formatString(getMissingDataCount(),"%6d")+" " );
	num_values = getMissingDataCount() + getNonMissingDataCount();
	if ( num_values == 0 ) {
		buffer.append ( StringUtil.formatString(100.0,"%6.2f")+" ");
	}
	else {
		buffer.append ( StringUtil.formatString(
		(100.0*(double)getMissingDataCount()/(double)num_values),"%6.2f")+" ");
	}
	buffer.append ( StringUtil.formatString(getNonMissingDataCount(),"%6d")+" ");
	if ( num_values == 0 ) {
		buffer.append ( StringUtil.formatString(0.0,"%6.2f")+" ");
	}
	else {
		buffer.append ( StringUtil.formatString(
		(100.0*(double)getNonMissingDataCount()/(double)num_values),"%6.2f")+" ");
	}
	buffer.append ( StringUtil.formatString(getMean(),"%10.1f") );
	buffer.append ( StringUtil.formatString(getMedian()," %10.1f") );
	buffer.append ( StringUtil.formatString(getStdDev()," %10.2f") );
	buffer.append ( StringUtil.formatString(getSkew()," %10.4f") + nl );
	buffer.append (
	"--------------------------------------------------------------------------------------------" +
	"---------------------------------" + nl );
	String r = buffer.toString();
	return r;
}

}