// ----------------------------------------------------------------------------
// TSAnalyst - class to analyze a time series
// ----------------------------------------------------------------------------
// History:
//
// 21 Dec 1998	Steven A. Malers, RTi	Initial version.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 07 Jun 1999	SAM, RTi		Add createStatisticsMonthTS method and
//					the coverage report.
// 22 Mar 2001	SAM, RTi		Convert static methods to instance
//					methods.
// 2001-11-06	SAM, RTi		Update javadoc and verify that unused
//					variables are set to null.  Remove
//					static from internal data to minimize
//					memory footprint.
// 2001-12-18	SAM, RTi		Add createStatisticsYearTS().
// 2002-01-08	SAM, RTi		Add "SetMissingIfMissing" property to
//					createStatisticsYearTS().
// 2002-11-26	SAM, RTi		Update the data coverage report to use
//					2 more symbols and breaks.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TS.INTERVAL* to TimeInterval.
// 2005-09-12	SAM, RTi		* Rename main create methods to use
//					  "Statistic" not "Statistics".
// 2005-09-20	SAM, RTi		* Combine the code for year statistic.
//					* Add additional year statistics.
// 2005-09-22	SAM, RTi		* Add AllowMissingCount for the year
//					  statistic code.
// 2005-09-28	SAM, RTi		* Add DayOfMax, DayOfMin, Max, Min
//					  statistics.
//					* Add CountGE, CountGT, CountLE, CountLT
//					  statistics.
// 2005-10-12	SAM, RTi		Fix bug where DayOfFirstGE did not set
//					the units.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Vector;

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
This class analyzes a time series.  It is currently in development.
Declare an instance of this class with a string-based input that is similar to
SQL.
*/
public class TSAnalyst
{

// Private flags...

/**
Data types that are queried or limited in where clauses.
*/
//private final int QUERY_VALUE = 1;
//private final int QUERY_VALUE_BY_MONTH = 2;
//private final int QUERY_VALUE_BY_DAY = 3;
//private final int QUERY_VALUE_BY_YEAR = 4;

/**
Functions for the query.
*/
// TODO SAM 2007-05-09 Need to enable or remove flags
//private final int FUNCTION_COUNT = 1;
//private final int FUNCTION_MAX = 2;
//private final int FUNCTION_MIN = 3;
//private final int FUNCTION_MEAN = 4;
//private final int FUNCTION_STDDEV = 5;
//private final int FUNCTION_SUM = 6;

/**
Conditions placed on the select.
*/
//private final int LESS_THAN_OR_EQUALS = 1;
//private final int LESS_THAN = 2;
//private final int EQUALS = 3;
//private final int GREATER_THAN = 4;
//private final int GREATER_THAN_OR_EQUALS = 5;
//private final int NOT_EQUALS = 6;
//private final int IS_MISSING = 7;
//private final int AND = 8;
//private final int OR = 9;

// Private data...

//private DateTime _date1;	// First date to consider when processing.
//private DateTime _date2;	// Last date to consider when processing.
private TS	_ts;		// Time series of interest.
//private String	_query_string;	// Query string to process.
//private PrintWriter _out;	// Output (file or stdout).
//private int	_query_field = QUERY_VALUE;
				// Data field from TS to query (default is data
				// value.
protected Vector _data_coverage_report_Vector = null;
protected DateTime _data_coverage_report_date1 = null;
protected DateTime _data_coverage_report_date2 = null;
protected PropList _data_coverage_report_props = null;

/**
Empty constructor.  Call additional methods to start and fill out reports.
*/
public TSAnalyst ()
{	initialize ( null, null );
}

/**
Construct with a time series and an analysis query string.
Initialize the dates to null and the limits to zeros.
@param ts Time series to analyze.
@param query_string Query string specifying analysis.
*/
public TSAnalyst ( TS ts, String query_string )
{	initialize ( ts, query_string );
}

/**
Append a record to the overall data coverage summary report.
@param monthts Statistics month time series.
*/
public void appendToDataCoverageSummaryReport ( MonthTS monthts )
{	String delim = "|";
	String string100 = "##";
	String string75 = "**";
	String string50 = "++";
	String string25 = "--";
	String stringnot0 = "..";
	String string0 = "  ";

	if ( monthts == null ) {
		return;
	}
	StringBuffer data_string = new StringBuffer ();
	DateTime date = new DateTime ( _data_coverage_report_date1 );
	String cal = _data_coverage_report_props.getValue ( "CalendarType" );
	if ( cal == null ) {
		cal = "CalendarYear";
	}
	// By this point the start and end dates will line up exactly with
	// the requested year type so just process 12 months in a row to
	// get totals, etc...
	int count = 0;
	double sum = 0.0, ave = 0.0, value = 0.0;
	int imonth = 0;
	for (	; date.lessThanOrEqualTo ( _data_coverage_report_date2);
		date.addMonth(1) ) {
		// Get data value...
		value = monthts.getDataValue ( date );
		++imonth;
		if ( !monthts.isDataMissing(value) ) {
			sum += value;
			++count;
		}
		if ( imonth == 12 ) {
			// Print the value and reset the numbers...
			if ( count == 0 ) {
				data_string.append ( string0 + delim );
			}
			else {	ave = sum/(double)count;
				if ( ave == 0.0 ) {
					data_string.append ( string0 + delim );
				}
				else if ( ave < 25.0 ) {
					data_string.append ( stringnot0+delim );
				}
				else if ( ave < 50.0 ) {
					data_string.append ( string25+delim );
				}
				else if ( ave < 75.0 ) {
					data_string.append ( string50+delim );
				}
				else if ( ave < 100.0 ) {
					data_string.append ( string75+delim );
				}
				else {	data_string.append ( string100+delim );
				}
			}
			count = 0;
			imonth = 0;
			sum = 0.0;
		}
	}
	// Now add the whole line...
	_data_coverage_report_Vector.addElement (
		StringUtil.formatString(monthts.getLocation(),"%-20.20s") +
		delim +
		StringUtil.formatString(monthts.getDescription(),"%-40.40s") +
		delim + data_string.toString() );
	delim = null;
	string100 = null;
	string50 = null;
	stringnot0 = null;
	string0 = null;
	data_string = null;
	date = null;
	cal = null;
}

/**
Create a monthly time series that contains statistics in each data value (e.g.,
percent missing, percent not missing).  The full period is used.
@return The statistics time series.
@param ts Time series to analyze (can be any interval).
@param props Properties to consider when analyzing.  Currently not used (the
percentage of data available is always computed).
@exception TSException if there is an error analyzing the time series.
*/
public static MonthTS createStatisticMonthTS ( TS ts, PropList props )
throws TSException
{	// Call the overloaded version...
	TSAnalyst tsa = new TSAnalyst();
	return tsa.createStatisticMonthTS ( ts, null, null, props );
}

/**
Create a monthly time series that contains statistics in each data value (e.g.,
percent missing, percent not missing).  The full period is used.
@return The statistics time series.
@param ts Time series to analyze (can be any interval).
@param props Properties to consider when analyzing.  Currently not used (the
percentage of data available is always computed).
@exception TSException if there is an error analyzing the time series.
@deprecated Use createStatisticMonthTS.
*/
public static MonthTS createStatisticsMonthTS ( TS ts, PropList props )
throws TSException
{	// Call the overloaded version...
	TSAnalyst tsa = new TSAnalyst ();
	return tsa.createStatisticMonthTS ( ts, null, null, props );
}

/**
Create a monthly time series that contains statistics in each data value (e.g.,
percent missing, percent not missing).  Currently this also writes to the
data coverage report but may want to change.
@return The statistics time series.
@param ts Time series to analyze (can be any interval).
@param start_date Starting date for analysis.
@param end_date Ending date for analysis.
@param props Properties to consider when analyzing.  Currently not used (the
percentage of data available is always computed).
@exception TSException if there is an error analyzing the time series.
*/
public MonthTS createStatisticMonthTS ( TS ts, DateTime start_date,
					DateTime end_date, PropList props )
throws TSException
{	String message, routine = "TSAnalyst.createStatisticMonthTS";
	int dl = 10;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Trying to create statistics month TS for \"" +
		ts.getIdentifierString() + "\"" );
	}
	if ( ts == null ) {
		// Nothing to do...
		message = "Null input time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	try {	// Main try...
	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = TSUtil.getValidPeriod (
				ts, start_date, end_date );
	DateTime start	= new DateTime ( valid_dates.getDate1() );
	DateTime end	= new DateTime ( valid_dates.getDate2() );
	valid_dates = null;

	// Create a monthly time series to be filled...

	MonthTS monthts = new MonthTS();
	monthts.addToGenesis ( "Initialized statistics month TS from \"" +
		ts.getIdentifierString() + "\"" );
	monthts.copyHeader ( ts );
	// Need to make sure the base and multiplier are not clobbered...
	monthts.setDataInterval ( TimeInterval.MONTH, 1 );
	monthts.setDate1 ( start );
	monthts.setDate2 ( end );
	monthts.setDataInterval ( TimeInterval.MONTH, 1 );
	// This will fill with missing data...
	monthts.allocateDataSpace();
	// Now set to zero...
	TSUtil.setConstant ( monthts, 0.0 );
	// Reset the units to percent...
	monthts.setDataUnits ( "PCT" );
	monthts.setDescription ( monthts.getDescription() +", % data coverage");

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();

	DateTime month_date = new DateTime ( start, DateTime.PRECISION_MONTH );
	double month_value = 0.0, value = 0.0;

	if ( interval_base == TimeInterval.IRREGULAR ) {
/* Do later... when we pass in a property with a suggested interval
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		Vector alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = (TSData)alltsdata.elementAt(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so
				// quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				tsdata.setData(value);
			}
		}
*/
	}
	else {	// Loop using addInterval...
		DateTime date = new DateTime ( start );
		int lastmonth = date.getMonth();
		int lastyear = date.getYear();
		month_value = 0.0;
		int data_count = 0;
		int month = 0, year = 0;
		for (	;
			date.lessThanOrEqualTo( end );
			date.addInterval(interval_base, interval_mult) ) {
			month = date.getMonth();
			year = date.getYear();
			if (	((month != 0) && (month != lastmonth)) 
				|| (year != lastyear) ) {
				// New month so set last month's values...
				// We are setting the percentage of data found
				// for the month.
				if ( data_count == 0 ) {
					month_value = 0.0;
				}
				else {	month_value = (double)data_count*100.0/
					TSUtil.numIntervalsInMonth(
					lastmonth, lastyear,
					interval_base, interval_mult );
				}
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"For " + date + " found " + data_count
					+ " values." );
				}
				monthts.setDataValue (
					month_date, month_value );
				// Now increment the month to be ready for the
				// next month...
				month_date.addMonth ( 1 );
				// Reset the value for next month...
				data_count = 0;
			}
			// Now evaluate the data for this interval.
			// If the data value is not missing, add to the value...
			value = ts.getDataValue ( date );
			if ( !ts.isDataMissing(value) ) {
				++data_count;
			}
			// Set the month and year so we can check in the next
			// iteration...
			lastmonth = month;
			lastyear = year;
		}
		// Always do the last value using the same logic as above...
		if ( month != 0 ) {
			// New month so set last month's values...
			// We are setting the percentage of data found
			// for the month.
			if ( data_count == 0 ) {
				month_value = 0.0;
			}
			else {	month_value = (double)data_count*100.0/
				TSUtil.numIntervalsInMonth( lastmonth, lastyear,
				interval_base, interval_mult );
			}
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"For " + date + " found " + data_count
				+ " values." );
			}
			monthts.setDataValue ( month_date, month_value );
		}
		date = null;
	}

	// Set the genesis information...

	ts.addToGenesis ( "Evaluated missing data from " + start.toString() +
		" to " + end.toString() + "." );
	// Clean up...
	month_date = null;
	start = null;
	end = null;
	message = null;
	routine = null;
	return monthts;
	}
	catch ( Exception e ) {
		message = "Error creating statistics month time series .";
		Message.printWarning ( 2, routine, message );
		Message.printWarning ( 2, routine, e );
		throw new TSException ( message );
	}
}

/**
Create a monthly time series that contains statistics in each data value (e.g.,
percent missing, percent not missing).  Currently this also writes to the
data coverage report but may want to change.
@return The statistics time series.
@param ts Time series to analyze (can be any interval).
@param start_date Starting date for analysis.
@param end_date Ending date for analysis.
@param props Properties to consider when analyzing.  Currently not used (the
percentage of data available is always computed).
@deprecated Use createStatisticMonthTS
@exception TSException if there is an error analyzing the time series.
*/
public static MonthTS createStatisticsMonthTS ( TS ts, DateTime start_date,
						DateTime end_date,
						PropList props )
throws TSException
{	TSAnalyst tsa = new TSAnalyst();
	return tsa.createStatisticMonthTS ( ts, start_date, end_date, props );
}

/**
Create a time series that contains statistics in each data value.  The period of
the result is the same as the source.  Each value in the result corresponds to a
statistic computed from a sample taken from the years in the period.  For example,
for a daily time series, the "Mean" statistic would return for every Jan 1, the mean
of all the non-missing Jan 1 values.
@return The statistics time series.
@param ts Time series to analyze (must be a regular interval).
@param AnalysisStart_DateTime Starting date/time for analysis, in precision of the
original data.
@param AnalysisEnd_DateTime Ending date for analysis, in precision of the
original data.
@param OutputStart_DateTime Output start date/time.
If null, the period of the original time series will be output.
@param OutputEnd_DateTime Output end date/time.
If null, the entire period will be analyzed.
@param props Properties to consider when analyzing.  The following properties
are recognized:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>Statistic</b></td>
<td>The statistic to process.
</td>
<td>Missing data report.</td>
</tr>

<tr>
<td><b>TestValue</b></td>
<td>Test value (e.g., threshold value) needed to process some statistics.
</td>
<td>Not currently enabled.</td>
</tr>

</table>
@exception Exception if there is an error analyzing the time series.
*/
public TS createStatisticTimeSeries ( Object tsobject,
					DateTime AnalysisStart_DateTime,
					DateTime AnalysisEnd_DateTime,
					DateTime OutputStart_DateTime,
					DateTime OutputEnd_DateTime,
					PropList props )
throws Exception
{	String message, routine = "TSAnalyst.createStatisticTimeSeries";
	int dl = 10;

    TS ts = null;
    TSEnsemble tsensemble = null;
    if ( tsobject instanceof TS ) {
        ts = (TS)tsobject;
        if ( Message.isDebugOn ) {
            Message.printDebug ( dl, routine, "Trying to create statistic time series using time series \"" +
            ts.getIdentifierString() + "\" as input." );
        }
    }
    else if ( tsobject instanceof TSEnsemble ) {
        tsensemble = (TSEnsemble)tsobject;
        // Use the first time series in the ensemble for period, copying for output etc.
        ts = tsensemble.get(0);
        if ( Message.isDebugOn ) {
            Message.printDebug ( dl, routine, "Trying to create statistic time series using ensemble \"" +
            tsensemble.getEnsembleID() + "\" for input." );
        }
    }

	if ( ts == null ) {
		// Nothing to do...
		message = "Null input time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	if ( props == null ) {
		props = new PropList ( "TSAnalyst" );
	}
	
	String Statistic = props.getValue ( "Statistic" );
	if ( !Statistic.equalsIgnoreCase(TSStatistic.Mean)) {
		throw new Exception ( "Only statistic Mean is currently recognized.");
	}

	try {
        // Main try...

	// Get valid dates for the output time series because the ones passed in may have been null...

    // The period over which to analyze the time series (within a trace when dealing with an ensemble)...
	TSLimits valid_dates = TSUtil.getValidPeriod ( ts, AnalysisStart_DateTime, AnalysisEnd_DateTime );
	DateTime analysis_start	= new DateTime ( valid_dates.getDate1() );
	DateTime analysis_end = new DateTime ( valid_dates.getDate2() );
	
    // The period to create the output statistic time series (within a trace when dealing with an ensemble)...
	valid_dates = TSUtil.getValidPeriod ( ts, OutputStart_DateTime, OutputEnd_DateTime );
	DateTime output_start = new DateTime ( valid_dates.getDate1() );
	DateTime output_end	= new DateTime ( valid_dates.getDate2() );

	// Create an output time series to be filled...

	TS output_ts = TSUtil.newTimeSeries(ts.getIdentifierString(), true);
	output_ts.copyHeader ( ts );
	output_ts.addToGenesis ( "Initialized statistic time series as copy of \"" + ts.getIdentifierString() + "\"" );

	// Reset the identifier if the user has specified it...

	String NewTSID = props.getValue ( "NewTSID" );
	try {
        if ( (NewTSID != null) && (NewTSID.length() > 0) ) {
			TSIdent tsident = new TSIdent ( NewTSID );
			output_ts.setIdentifier ( tsident );
		}
		else if ( (Statistic != null) && (Statistic.length() > 0) ) {
			// Default is to reset the data type to the statistic...
			output_ts.setDataType ( Statistic );
		}
	}
	catch ( Exception e ) {
		message = "Unable to set new time series identifier \""+NewTSID + "\".";
		Message.printWarning ( 3, routine,message );
		throw (e);
	}

	// Automatically sets the precision...
	output_ts.setDate1 ( output_start );
	output_ts.setDate2 ( output_end );

	// This will fill with missing data...
	output_ts.allocateDataSpace();

	// Process the statistic of interest...

    TS stat_ts = null;
    if ( tsensemble == null ) {
        // Analyze the single time series to get a statistic...
        stat_ts = createStatisticTimeSeries_ComputeStatistic ( ts, analysis_start, analysis_end, Statistic );
        // Now use the statistic to repeat every year...
        createStatisticTimeSeries_FillOuput ( stat_ts, output_ts, output_start, output_end, Statistic );
    }
    else {
        // Analyse the ensemble to compute and assign the statistic to the output time series...
        createStatisticTimeSeries_ComputeStatisticFromEnsemble ( tsensemble, output_ts,
                analysis_start, analysis_end, Statistic );
    }

	// Return the statistic result...
	return output_ts;

	}
	catch ( Exception e ) {
		message = "Error creating statistic time series .";
		Message.printWarning ( 2, routine, message );
		Message.printWarning ( 2, routine, e );
		throw new TSException ( message );
	}
}

/**
Create the statistic data from another time series.  The results are saved in a time
series having the interval of the input data, from Jan 1, 00 to Dec 31 (one interval from
the start of the next year).  Year 2000 is used because it was a leap year.  This allows
Feb 29 data to be computed as a statistic, although it may no be used in the final output.
@param ts Time series to be analyzed.
@param analysis_start Start of period to analyze.
@param analysis_end End of period to analyze.
@param statistic Statistic to compute.
@return The statistics in a single-year time series.
*/
private TS createStatisticTimeSeries_ComputeStatistic ( TS ts, DateTime analysis_start, DateTime analysis_end, String statistic )
throws Exception
{	// Get the dates for the one-year statistic time series...
	DateTime date1 = new DateTime ( analysis_start );	// To get precision
	date1.setYear( 2000 );
	date1.setMonth ( 1 );
	date1.setDay( 1 );
	date1.setHour ( 0 );
	date1.setMinute ( 0 );
	DateTime date2 = new DateTime ( date1 );
	// Add one year...
	date2.addYear( 1 );
	// Now subtract one interval...
	date2.addInterval( ts.getDataIntervalBase(), -ts.getDataIntervalMult());
	// Create time series for the sum, count of data values, and final statistic...
	TS sum_ts = TSUtil.newTimeSeries ( ts.getIdentifierString(), true );
	TS count_ts = TSUtil.newTimeSeries ( ts.getIdentifierString(), true );
	TS stat_ts = TSUtil.newTimeSeries ( ts.getIdentifierString(), true );
	// Copy the header information...
	sum_ts.copyHeader ( ts );
	count_ts.copyHeader ( ts );
	stat_ts.copyHeader ( ts );
	// Reset the data type...
	sum_ts.setDataType("Sum");
	count_ts.setDataType("CountNonMissing");
	stat_ts.setDataType(statistic);
	// Set the dates in the statistic time series...
	sum_ts.setDate1( date1 );
	sum_ts.setDate2 ( date2 );
	count_ts.setDate1( date1 );
	count_ts.setDate2 ( date2 );
	stat_ts.setDate1( date1 );
	stat_ts.setDate2 ( date2 );
	// Now allocate the data space...
	sum_ts.allocateDataSpace();
	count_ts.allocateDataSpace();
	stat_ts.allocateDataSpace();
	// Iterate through the raw data and increment the statistic time series.  For now,
	// only handle a few statistics.
	TSIterator tsi = ts.iterator();
	DateTime date;
	double value;	// Value from the input time series
	double sum_value;	// Value in the sum time series
	DateTime date_tmp = new DateTime ( date1 );	// For setting data into stat_ts
	while ( tsi.next() != null ) {
		// Get the date out of the iterator (may skip over leap years).
		date = tsi.getDate();
		value = tsi.getDataValue();
		if ( ts.isDataMissing(value) ) {
			// Ignore missing data...
			continue;
		}
		// Set the date information in the reused date (year is set above)...
		date_tmp.setMonth(date.getMonth());
		date_tmp.setDay(date.getDay());
		date_tmp.setHour(date.getHour());
		date_tmp.setMinute(date.getMinute());
		sum_value = sum_ts.getDataValue ( date_tmp );
		if ( ts.isDataMissing(sum_value) ) {
			// Just assign the value
			sum_ts.setDataValue ( date_tmp, value );
			count_ts.setDataValue ( date_tmp, 1.0 );
		}
		else {
			// Increment the total in the time series...
			sum_ts.setDataValue( date_tmp, sum_value + value);
			count_ts.setDataValue ( date_tmp, count_ts.getDataValue(date_tmp) + 1.0 );
		}
	}
	// Now loop through the sum time series and compute the final one-year statistic time series...
	tsi = count_ts.iterator();
	// TODO SAM 2007-11-05 Fix this if statistics other than Mean are added
	double count_value;
	while ( tsi.next() != null ) {
		date = tsi.getDate();
		count_value = tsi.getDataValue();
		if ( count_value > 0.0 ) {
			stat_ts.setDataValue ( date, sum_ts.getDataValue(date)/count_value);
		}
	}
	// Return the result.
	return stat_ts;
}

/**
Create the statistic data from an ensemble of time series.  The results are saved in a time
series having the interval of the input data.
@param tsensemble Time series ensemble to be analyzed.
@param stat_ts Statistic (output) time series to be created.
@param analysis_start Start of period to analyze.
@param analysis_end End of period to analyze.
@param statistic Statistic to compute.
@return The statistics in a single-year time series.
*/
private TS createStatisticTimeSeries_ComputeStatisticFromEnsemble ( TSEnsemble tsensemble, TS stat_ts,
        DateTime analysis_start, DateTime analysis_end, String statistic )
throws Exception
{   // Initialize the iterators using the analysis period...
    TSIterator tsi_stat = stat_ts.iterator ( analysis_start, analysis_end );
    int size = tsensemble.size();
    TS ts;
    // To improve performance, initialize an array of time series...
    TS [] ts_array = tsensemble.toArray();
    // Now iterate through all of the traces and get data for each date/time...
    DateTime date;
    int i;  // Index for time series in loop.
    double value;   // Value from the input time series
    double sum_value = ts_array[0].getMissing();   // Value in the sum time series
    int count;
    while ( tsi_stat.next() != null ) {
        date = tsi_stat.getDate();
        // Loop through the time series...
        count = 0;
        for ( i = 0; i < size; i++ ) {
            ts = ts_array[i];
            if ( i == 0 ) {
                sum_value = ts.getMissing();
            }
            value = ts.getDataValue(date);
            if ( ts.isDataMissing(value) ) {
                // Ignore missing data...
                continue;
            }
            if ( ts.isDataMissing(sum_value) ) {
                // Just assign the value
                sum_value = value;
                ++count;
            }
            else {
                // Increment the total...
                sum_value += value;
                ++count;
            }
        }
        // Now compute the statistic time series.  Currently this is always the mean.
        // FIXME 
        if ( count > 0 ) {
            stat_ts.setDataValue ( date, sum_value/(double)count );
        }
    }

    // Return the result.
    return stat_ts;
}

/**
Fill the output repeating time series with the statistic values.
@param stat_ts Year-long time series with statistic.
@param output_ts Output time series to fill.
@param output_start Output period start.
@param output_end Output period end.
@param Statistic Statistic being generated.
*/
private void createStatisticTimeSeries_FillOuput ( TS stat_ts, TS output_ts, DateTime output_start, DateTime output_end, String Statistic )
throws Exception
{
	TSIterator tsi = output_ts.iterator();
	DateTime date;
	DateTime stat_date = null;
	while ( tsi.next() != null ) {
		// Get the date of interest in the output...
		date = tsi.getDate();
		if ( stat_date == null ) {
			// Initialize to the same precision, etc., by copying the other.
			stat_date = new DateTime ( date );
			stat_date.setYear ( 2000 );
		}
		// Get the corresponding value from the one-year statistic time series.  Do this
		// by resetting the stat_date information to that of the other date but use a year of
		// 2000 since that is used in the statistics time series processing.
		stat_date.setMonth ( date.getMonth() );
		stat_date.setDay ( date.getDay() );
		stat_date.setHour ( date.getHour() );
		stat_date.setMinute ( date.getMinute() );
		output_ts.setDataValue ( date, stat_ts.getDataValue(stat_date));
	}
}

/**
Create a year time series that contains statistics in each data value (e.g.,
percent missing, percent not missing).
@return The statistics time series.
@param ts Time series to analyze (can be any interval).
@param AnalysisStart_DateTime Starting date for analysis, in precision of the
original data.
The year time series will have a start date with the year matching this date.
If null, the entire period will be analyzed.
@param AnalysisEnd_DateTime Ending date for analysis, in precision of the
original data.
The year time series will have an end date with the year matching this date.
If null, the entire period will be analyzed.
@param props Properties to consider when analyzing (see overloaded version for details).
@exception TSException if there is an error analyzing the time series.
*/
public YearTS createStatisticYearTS ( TS ts,
                    DateTime AnalysisStart_DateTime,
                    DateTime AnalysisEnd_DateTime,
                    PropList props )
throws TSException
{
    return createStatisticYearTS ( ts, AnalysisStart_DateTime, AnalysisEnd_DateTime, null, null, props );
}

/**
Create a year time series that contains statistics in each data value (e.g.,
percent missing, percent not missing).
@return The statistics time series.
@param ts Time series to analyze (can be any interval).
@param AnalysisStart_DateTime Starting date for analysis, in precision of the
original data.
The year time series will have a start date with the year matching this date.
If null, the entire period will be analyzed.
@param AnalysisEnd_DateTime Ending date for analysis, in precision of the
original data.  If null, the entire period will be analyzed.
The year time series will have an end date with the year matching this date.
If null, the entire period will be analyzed.
@param AnalysisWindowStart_DateTime Starting date/time for analysis within the year, in precision of the
original data.  If null, the entire year of data will be analyzed.
@param AnalysisWindowEnd_DateTime Ending date for analysis within the year, in precision of the
original data.  If null, the entire year of data will be analyzed.
@param props Properties to consider when analyzing.  The following properties
are recognized:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>SetMissingIfMissing</b></td>
<td>If a data coverage report (the default), then no data will result in a 0.0
value.  Specifying "true" will set the value to the missing data value (-999)
when no data are found.
</td>
<td>False.</td>
</tr>

<tr>
<td><b>Statistic</b></td>
<td>The statistic to process.
</td>
<td>Missing data report.</td>
</tr>

<tr>
<td><b>TestValue</b></td>
<td>Test value (e.g., threshold value) needed to process some statistics.
</td>
<td>None - use is specific to the statistic.</td>
</tr>

</table>
@exception TSException if there is an error analyzing the time series.
*/
public YearTS createStatisticYearTS (	TS ts,
					DateTime AnalysisStart_DateTime,
					DateTime AnalysisEnd_DateTime,
					DateTime AnalysisWindowStart_DateTime,
                    DateTime AnalysisWindowEnd_DateTime,
					PropList props )
throws TSException
{	String message, routine = "TSAnalyst.createStatisticYearTS";
	int dl = 10;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Trying to create statistic year TS for \"" +
		ts.getIdentifierString() + "\"" );
	}
	if ( ts == null ) {
		// Nothing to do...
		message = "Null input time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	if ( props == null ) {
		props = new PropList ( "TSAnalyst" );
	}
	
	String Statistic = props.getValue ( "Statistic" );

	try {	// Main try...

	// Get valid dates for the output time series because the ones passed in
	// may have been null...

	TSLimits valid_dates = TSUtil.getValidPeriod ( ts, AnalysisStart_DateTime, AnalysisEnd_DateTime );
	DateTime start = new DateTime ( valid_dates.getDate1() );
	DateTime end = new DateTime ( valid_dates.getDate2() );
	valid_dates = null;

	// Create a year time series to be filled...

	YearTS yearts = new YearTS();
	yearts.addToGenesis ( "Initialized statistic year TS from \"" +	ts.getIdentifierString() + "\"" );
	yearts.copyHeader ( ts );

	// Reset the identifier if the user has specified it...

	String NewTSID = props.getValue ( "NewTSID" );
	try {
	    if ( (NewTSID != null) && (NewTSID.length() > 0) ) {
			TSIdent tsident = new TSIdent ( NewTSID );
			yearts.setIdentifier ( tsident );
		}
		else if ( (Statistic != null) && (Statistic.length() > 0) ) {
			// Default is to reset the data type to the statistic...
			yearts.setDataType ( Statistic );
		}
	}
	catch ( Exception e ) {
		message = "Unable to set new time series identifier \"" + NewTSID + "\".";
		Message.printWarning ( 3, routine,message );
		throw (e);
	}

	// Need to make sure the base and multiplier are not clobbered...
	yearts.setDataInterval ( TimeInterval.YEAR, 1 );

	// Automatically sets the precision...
	yearts.setDate1 ( start );
	yearts.setDate2 ( end );

	// This will fill with missing data...
	yearts.allocateDataSpace();

	// Process the statistic of interest...

	if (	
		Statistic.equals(TSStatistic.CountGE) ||
		Statistic.equals(TSStatistic.CountGT) ||
		Statistic.equals(TSStatistic.CountLE) ||
		Statistic.equals(TSStatistic.CountLT) ||
		Statistic.equals(TSStatistic.DayOfFirstGE) ||
		Statistic.equals(TSStatistic.DayOfFirstGT) ||
		Statistic.equals(TSStatistic.DayOfFirstLE) ||
		Statistic.equals(TSStatistic.DayOfFirstLT) ||
		Statistic.equals(TSStatistic.DayOfLastGE) ||
		Statistic.equals(TSStatistic.DayOfLastGT) ||
		Statistic.equals(TSStatistic.DayOfLastLE) ||
		Statistic.equals(TSStatistic.DayOfLastLT) ||
		Statistic.equals(TSStatistic.DayOfMax) ||
		Statistic.equals(TSStatistic.DayOfMin) ||
		Statistic.equals(TSStatistic.Max) ||
		Statistic.equals(TSStatistic.Mean) ||
		Statistic.equals(TSStatistic.Min) ||
		Statistic.equals(TSStatistic.Total) ) {
		processStatistic ( ts, yearts, props, start, end,
		        AnalysisWindowStart_DateTime, AnalysisWindowEnd_DateTime  );
	}
	else {
	    // TODO SAM 2005-09-12
		// The following is for the data coverage report and needs to
		// be migrated to a method.  This currently the default.
		processStatisticDataCoveragePercent ( ts, yearts, props, start, end );
	}

	// Return the statistic result...
	return yearts;

	}
	catch ( Exception e ) {
		message = "Error creating statistic year time series .";
		Message.printWarning ( 2, routine, message );
		Message.printWarning ( 2, routine, e );
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

	_data_coverage_report_Vector = null;
	_data_coverage_report_date1 = null;
	_data_coverage_report_date2 = null;
	_data_coverage_report_props = null;
	super.finalize();
}

/**
Return the contents of the data coverage report as a Vector of String.
@return the results of the data coverage report.  This returns the report
contents (not a copy).
*/
public Vector getDataCoverageReport ()
{	return _data_coverage_report_Vector;
}

/**
Initialize the instance.
@param ts Time series to analyze.
@param query_string Query string specifying analysis.
*/
private void initialize ( TS ts, String query_string )
{	//_query_string = query_string;
	_ts = ts;
	if ( (_ts != null) && (query_string != null) ) {
		parseQuery ();
	}
}

/**
Parse the query string.  For now, always print results using messages.
@param out PrintWriter to write results.
@param query_string Query string specifying analysis.
*/
public void parseQuery ( PrintWriter out, String query_string )
{	if ( query_string != null ) {
		//_query_string = query_string;
		parseQuery ();
	}
}

/**
Parse the query string and perform the query on the time series.
*/
public void parseQuery ( )
{	
/*
	if ( _query_string == null ) {
		Message.printWarning ( 2, routine, "No query string specified");
		return;
	}
	if ( _ts == null ) {
		Message.printWarning ( 2, routine, "No time series specified");
		return;
	}

	Vector tokens = StringUtil.breakStringList ( _query_string,
		" \t\n(),", StringUtil.DELIM_RETURN_DELIMITERS );
	if ( tokens == null ) {
		return;
	}

	int ntokens = tokens.size();

	String token = null;
	boolean select_found = false;
	int function = 0;
	boolean function_detected = false;
	for ( int itoken = 0; itoken < ntokens; itoken++ ) {
		token = (String)tokens.elementAt(itoken);
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Token is \"" +
			token + "\"");
		}
		if ( token == null ) {
			continue;
		}
		if ( token.length() == 0 ) {
			continue;
		}
		if ( token.equalsIgnoreCase ( "select" ) ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Found \"select\"" );
			}
			select_found = true;
			continue;
		}
		if ( select_found ) {
			// We are looking for a function name or data field...
			if ( token.regionMatches(true,0,"count",0,5) ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Found \"count()\" function" );
				}
				function = FUNCTION_COUNT;
				function_detected = true;
			}
			else if ( token.regionMatches(true,0,"max",0,3) ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Found \"max()\" function" );
				}
				function = FUNCTION_MAX;
				function_detected = true;
			}
			else if ( token.regionMatches(true,0,"mean",0,4) ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Found \"mean()\" function" );
				}
				function = FUNCTION_MEAN;
				function_detected = true;
			}
			else if ( token.regionMatches(true,0,"min",0,3) ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Found \"min()\" function" );
				}
				function = FUNCTION_MIN;
				function_detected = true;
			}
			else if ( token.regionMatches(true,0,"value",0,5) ) {
				// Need to check for "value by month", etc...
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Selecting \"value\"." );
				}
				int skip = parseQueryField (
					tokens, itoken, false );
				if ( skip < 0 ) {
					// Default to value.
					Message.printWarning ( 2, routine,
					"Defaulting query field to value." );
					_query_field = QUERY_VALUE;
				}
				else {	// Skip tokens...
					itoken += skip - 1;
				}
			}
			else if ( token.charAt(0) == ' ' ||
				token.charAt(0) == '\t' ||
				token.charAt(0) == '\n' ||
				token.charAt(0) == '(' ||
				token.charAt(0) == ')' ) {
				// Delimiters...
				continue;
			}
			else {	Message.printWarning ( 1, routine,
				"Unknown field or function \"" + token + "\"" );
			}
			if ( function_detected ) {
				// Figure out what the function is operating
				// on by passing the ( and remaining tokens...
				int skip = parseQueryField (
					tokens, (itoken + 1), true );
				if ( skip < 0 ) {
					// Default to value.
					Message.printWarning ( 2, routine,
					"Defaulting query field to value." );
					_query_field = QUERY_VALUE;
				}
				else {	// Skip tokens...
					itoken += skip - 1;
				}
				// Only hanlde one function at a time...
				function_detected = false;
			}
		}
	}

	// Now the string has been parsed so execute the query...

	if ( _query_field == QUERY_VALUE_BY_MONTH ) {
		Message.printWarning ( 1, routine, "Querying values by month " +
		"is not enabled.  Querying full period." );
	}

	TSLimits limits;
	if ( function == FUNCTION_COUNT ) {
		try {	limits = TSUtil.getDataLimits ( _ts );
			Message.printStatus ( 2, routine,
			"Non-missing data count is " +
			limits.getNonMissingDataCount() +
			" Missing data count is " +
			limits.getMissingDataCount() );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine,
			"Error executing count()" );
		}
	}
	else if ( function == FUNCTION_MAX ) {
		try {	limits = TSUtil.getDataLimits ( _ts );
			Message.printStatus ( 2, routine,
			"Max value is " + limits.getMaxValue() + " on " +
			limits.getMaxValueDate() );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine,
			"Error executing max()" );
		}
	}
*/
/*
	else if ( function == FUNCTION_MEAN ) {
		try {	limits = TSUtil.getDataLimits ( _ts );
			Message.printStatus ( 2, routine,
			"Mean value is " + limits.getMaxValue() + " on " +
			limits.getMaxValueDate() );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine,
			"Error executing max()" );
		}
	}
*/
/*
	else if ( function == FUNCTION_MIN ) {
		try {	limits = TSUtil.getDataLimits ( _ts );
			Message.printStatus ( 2, routine,
			"Min value is " + limits.getMinValue() + " on " +
			limits.getMinValueDate() );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine,
			"Error executing min()" );
		}
	}
*/
	//tokens = null;
	//token = null;
	//limits = null;
}

/**
Parse the query string (e.g., "value by field") and set the query field data
member.
@param tokens The Vector of token strings.
@param itoken The position in the Vector to start checking for the query field.
@param isfunction true if the tokens are part of a function (in which case the
first token should be "(") or false if the tokens are the main part of the
select (in which case the first token should be "value").
@return The number of tokens to skip in the calling routine (-1 if there is
a problem).
*//*
private int parseQueryField ( Vector tokens, int itoken, boolean isfunction )
{	String routine = "TSAnalyst.parseQueryField";

	if ( tokens == null ) {
		return -1;
	}
	int size = tokens.size();
	boolean value_found = false;
	boolean by_found = false;
	boolean month_found = false;
	// Pretty simple, just look for "value" "by" "month"...
	String token = null;
	int itoken0 = itoken;
	// This needs to be made more robust if we include it in anything...
	// For example, check for order of tokens.
	for ( ; itoken < size; itoken++ ) {
		token = (String)tokens.elementAt(itoken);
		if ( token.length() == 0 ) {
			continue;
		}
		if ( token.charAt(0) == ' ' ||
			token.charAt(0) == '\t' ||
			token.charAt(0) == '\n' ) {
			// Delimiters...
			continue;
		}
		// Check the first token...
		if ( itoken == itoken0 ) {
			if ( isfunction ) {
				// First token should be a "("...
				if ( !token.equals ("(") ) {
					Message.printWarning ( 2, routine,
					"\"(\" expected to start function " +
					"query field." );
					return -1;
				}
			}
			else {	// First token should be "value"...
				if ( !token.equalsIgnoreCase ("value") ) {
					Message.printWarning ( 2, routine,
					"\"value\" expected to start " +
					"query field." );
					return -1;
				}
			}
		}
		// Check for recognized strings...
		if ( token.equalsIgnoreCase("value") ) {
			value_found = true;
		}
		else if ( token.equalsIgnoreCase("by") ) {
			by_found = true;
		}
		else if ( token.equalsIgnoreCase("month") ) {
			month_found = true;
		}
		if ( isfunction ) {
			// Break when we encounter a ")"...
			if ( token.equals(")") ) {
				break;
			}
		}
		else {	// Break when we encounter something that is not
			// expected...
			if ( !token.equalsIgnoreCase("value") &&
				!token.equalsIgnoreCase("by") &&
				!token.equalsIgnoreCase("month") ) {
				break;
			}
		}
	}
	// Now evaluate the settings...
	if ( value_found && by_found && month_found ) {
		//_query_field = QUERY_VALUE_BY_MONTH;
	}
	else if ( value_found ) {
		//_query_field = QUERY_VALUE;
	}
	else {	// Can't handle anything else...
		return -1;
	}
	routine = null;
	token = null;
	return itoken - itoken0 + 1;
}
*/

/**
Process a time series to create the the following statistics:
<ol>
<li>	CountGE</li>
<li>	CountGT</li>
<li>	CountLE</li>
<li>	CountLT</li>
<li>	DayOfFirstGE</li>
<li>	DayOfFirstGT</li>
<li>	DayOfFirstLE</li>
<li>	DayOfFirstLT</li>
<li>	DayOfLastGE</li>
<li>	DayOfLastGT</li>
<li>	DayOfLastLE</li>
<li>	DayOfLastLT</li>
<li>	DayOfMax</li>
<li>	DayOfMin</li>
<li>	Max</li>
<li>	Min</li>
</ol>
@param ts Time series to analyze.
@param yearts YearTS to fill with the statistic.
@param props Properties to control the analysis, as follows:
@param start Start of the analysis (precision matching ts).
@param end End of the analysis (precision matching ts).
@param AnalysisWindowStart_DateTime If not null, specify the start of the window within
the year for data, for example to specify a season.
Currently only Month... to precision are evaluated (not day... etc.).
@param AnalysisWindowEnd_DateTime If not null, specify the end of the window within
the year for data, for example to specify a season.
Currently only Month... to precision are evaluated (not day... etc.).
*/
private void processStatistic (	TS ts, YearTS yearts,
				PropList props,
				DateTime start, DateTime end,
				DateTime AnalysisWindowStart_DateTime, DateTime AnalysisWindowEnd_DateTime )
throws Exception
{	String routine = "TSAnalyst.processStatistic", message;
	DateTime year_date = new DateTime ( end, DateTime.PRECISION_YEAR );
	double year_value = 0.0,	// Statistic value for year.
		value = 0.0,		// Time series data value.
		extreme_value = 0.0;	// Extreme value in a year.
	boolean iterate_forward = true;	// Direction for iteration
					// True = forward, false = backward
	int dl = 1;	// Debug level for this method

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base != TimeInterval.DAY ) {
		message = "Only daily time series can be processed.";
		Message.printWarning ( 3, routine, message );
		throw new Exception ( message );
	}
	boolean is_regular = TimeInterval.isRegularInterval(interval_base);

	// TODO SAM 2005-09-22
	// Currently this is used below in setting the description.  However,
	// some statistics may not need a test value.

	String TestValue = props.getValue ( "TestValue" );
	double TestValue_double = -1.0;
	if ( (TestValue != null) && (TestValue.length() > 0) )  {
		if ( !StringUtil.isDouble(TestValue) ) {
			message = "TestValue (" +TestValue+") is not a number.";
			Message.printWarning ( 3, routine, message );
			throw new Exception ( message );
		}
		else {
		    TestValue_double = StringUtil.atod ( TestValue );
		}
	}

	// Put this here because it may be overwridden below if not allowed...

	String SearchStart = props.getValue ( "SearchStart" );

	String Statistic = props.getValue ( "Statistic" );
	int Statistic_CountGE = 0;	// Used to control logic below
	int Statistic_CountGT = 1;
	int Statistic_CountLE = 2;
	int Statistic_CountLT = 3;
	int Statistic_DayOfFirstGE = 4;
	int Statistic_DayOfFirstGT = 5;
	int Statistic_DayOfFirstLE = 6;
	int Statistic_DayOfFirstLT = 7;
	int Statistic_DayOfLastGE = 8;
	int Statistic_DayOfLastGT = 9;
	int Statistic_DayOfLastLE = 10;
	int Statistic_DayOfLastLT = 11;
	int Statistic_DayOfMax = 12;
	int Statistic_DayOfMin = 13;
	int Statistic_Max = 14;
	int Statistic_Mean = 15;
	int Statistic_Min = 16;
	int Statistic_Total = 17;
	int Statistic_int = -1;	
	int Test_GE = 0;	// Tests to perform >=
	int Test_GT = 1;	// >
	int Test_LE = 2;	// <=
	int Test_LT = 3;	// <
	int Test_Max = 4;	// max()
	int Test_Min = 5;	// min()
	int Test_Accumulate = 6;   // Accumulate values instead of testing
	int Test_int = -1;	
	boolean Statistic_isCount = false;	// Indicates if the statistic is
						// one that keeps a count of
						// test passes.
	if ( Statistic.equals(TSStatistic.CountGE) ) {
		iterate_forward = true;
		Statistic_int = Statistic_CountGE;
		Test_int = Test_GE;
		yearts.setDescription ( "Count of values >= " + TestValue );
		SearchStart = "01-01";	// Always default to first of year
		yearts.setDataUnits ( "Count" );
		Statistic_isCount = true;
	}
	else if ( Statistic.equals(TSStatistic.CountGT) ) {
		iterate_forward = true;
		Statistic_int = Statistic_CountGT;
		Test_int = Test_GT;
		yearts.setDescription ( "Count of values > " + TestValue );
		SearchStart = "01-01";	// Always default to first of year
		yearts.setDataUnits ( "Count" );
		Statistic_isCount = true;
	}
	else if ( Statistic.equals(TSStatistic.CountLE) ) {
		iterate_forward = true;
		Statistic_int = Statistic_CountLE;
		Test_int = Test_LE;
		yearts.setDescription ( "Count of values <= " + TestValue );
		SearchStart = "01-01";	// Always default to first of year
		yearts.setDataUnits ( "Count" );
		Statistic_isCount = true;
	}
	else if ( Statistic.equals(TSStatistic.CountLT) ) {
		iterate_forward = true;
		Statistic_int = Statistic_CountLT;
		Test_int = Test_LT;
		yearts.setDescription ( "Count of values < " + TestValue );
		SearchStart = "01-01";	// Always default to first of year
		yearts.setDataUnits ( "Count" );
		Statistic_isCount = true;
	}
	else if ( Statistic.equals(TSStatistic.DayOfFirstGE ) ) {
		iterate_forward = true;
		Statistic_int = Statistic_DayOfFirstGE;
		Test_int = Test_GE;
		yearts.setDescription ( "Day of year for first value >= " +	TestValue );
		yearts.setDataUnits ( "DayOfYear" );
	}
	else if ( Statistic.equals(TSStatistic.DayOfFirstGT ) ) {
		iterate_forward = true;
		Statistic_int = Statistic_DayOfFirstGT;
		Test_int = Test_GT;
		yearts.setDescription ( "Day of year for first value > " + TestValue );
		yearts.setDataUnits ( "DayOfYear" );
	}
	else if ( Statistic.equals(TSStatistic.DayOfFirstLE ) ) {
		iterate_forward = true;
		Statistic_int = Statistic_DayOfFirstLE;
		Test_int = Test_LE;
		yearts.setDescription ( "Day of year for first value <= " + TestValue );
		yearts.setDataUnits ( "DayOfYear" );
	}
	else if ( Statistic.equals(TSStatistic.DayOfFirstLT ) ) {
		iterate_forward = true;
		Statistic_int = Statistic_DayOfFirstLT;
		Test_int = Test_LT;
		yearts.setDescription ( "Day of year for first value < " + TestValue );
		yearts.setDataUnits ( "DayOfYear" );
	}
	else if ( Statistic.equals(TSStatistic.DayOfLastGE) ) {
		iterate_forward = false;
		Statistic_int = Statistic_DayOfLastGE;
		Test_int = Test_GE;
		yearts.setDescription ( "Day of year for last value >= " + TestValue );
		yearts.setDataUnits ( "DayOfYear" );
	}
	else if ( Statistic.equals(TSStatistic.DayOfLastGT) ) {
		iterate_forward = false;
		Statistic_int = Statistic_DayOfLastGT;
		Test_int = Test_GT;
		yearts.setDescription ( "Day of year for last value > " + TestValue );
		yearts.setDataUnits ( "DayOfYear" );
	}
	else if ( Statistic.equals(TSStatistic.DayOfLastLE) ) {
		iterate_forward = false;
		Statistic_int = Statistic_DayOfLastLE;
		Test_int = Test_LE;
		yearts.setDescription ( "Day of year for last value <= " + TestValue );
		yearts.setDataUnits ( "DayOfYear" );
	}
	else if ( Statistic.equals(TSStatistic.DayOfLastLT) ) {
		iterate_forward = false;
		Statistic_int = Statistic_DayOfLastLT;
		Test_int = Test_LT;
		yearts.setDescription ( "Day of year for last value < " + TestValue );
		yearts.setDataUnits ( "DayOfYear" );
	}
	// TODO SAM 2005-09-28
	// Need to decide if iteration direction and SearchStart should be a
	// parameter for max and min
	else if ( Statistic.equals(TSStatistic.DayOfMax) ) {
		iterate_forward = true;
		Statistic_int = Statistic_DayOfMax;
		Test_int = Test_Max;
		yearts.setDescription ( "Day of year for maximum value" );
		SearchStart = "01-01";	// Always default to first of year
		yearts.setDataUnits ( "DayOfYear" );
	}
	else if ( Statistic.equals(TSStatistic.DayOfMin) ) {
		iterate_forward = true;
		Statistic_int = Statistic_DayOfMin;
		Test_int = Test_Min;
		yearts.setDescription ( "Day of year for minimum value" );
		SearchStart = "01-01";	// Always default to first of year
		yearts.setDataUnits ( "DayOfYear" );
	}
	else if ( Statistic.equals(TSStatistic.Max) ) {
		iterate_forward = true;
		Statistic_int = Statistic_Max;
		Test_int = Test_Max;
		yearts.setDescription ( "Maximum value" );
		SearchStart = "01-01";	// Always default to first of year
		yearts.setDataUnits ( ts.getDataUnits() );
	}
    else if ( Statistic.equals(TSStatistic.Mean) ) {
        iterate_forward = true;
        Statistic_int = Statistic_Mean;
        Test_int = Test_Accumulate;
        yearts.setDescription ( "Mean value" );
        SearchStart = "01-01";  // Always default to first of year
        yearts.setDataUnits ( ts.getDataUnits() );
        //Statistic_isCount = true;   // Needed for computations
    }
	else if ( Statistic.equals(TSStatistic.Min) ) {
		iterate_forward = true;
		Statistic_int = Statistic_Min;
		Test_int = Test_Min;
		yearts.setDescription ( "Minimum value" );
		SearchStart = "01-01";	// Always default to first of year
		yearts.setDataUnits ( ts.getDataUnits() );
	}
    else if ( Statistic.equals(TSStatistic.Total) ) {
        iterate_forward = true;
        Statistic_int = Statistic_Total;
        Test_int = Test_Accumulate;
        yearts.setDescription ( "Total value" );
        SearchStart = "01-01";  // Always default to first of year
        yearts.setDataUnits ( ts.getDataUnits() );
        //Statistic_isCount = true;   // Needed for computation checks
    }
	else { 	message = "Unknown statistic (" + Statistic + ").";
		Message.printWarning ( 3, routine, message );
		throw new Exception ( message );
	}

	String AllowMissingCount = props.getValue ( "AllowMissingCount" );
	int AllowMissingCount_int = 0;
	if ( (AllowMissingCount != null) && !AllowMissingCount.equals("") ) {
		if ( !StringUtil.isInteger(AllowMissingCount) ) {
			message = "AllowMissingCount (" + AllowMissingCount + ") is not an integer.";
			Message.printWarning ( 3, routine, message );
			throw new Exception ( message );
		}
		AllowMissingCount_int = StringUtil.atoi ( AllowMissingCount );
		if ( AllowMissingCount_int < 0 ) {
			AllowMissingCount_int = 0;
		}
	}

	DateTime SearchStart_DateTime = null;
	if ( (SearchStart != null) && (SearchStart.length() > 0) ) {
		try {
		    SearchStart_DateTime = DateTime.parse ( SearchStart );
		}
		catch ( Exception e )
		{
			message = "SearchStart (" + SearchStart + ") is not a date.";
			Message.printWarning ( 3, routine, message );
			throw new Exception ( message );
		}
	}
	else {
	    // Set to space for message below.
		SearchStart = "";
	}

	// Loop through data, starting at the front of the time series...
	DateTime date = null,
		date_search = null;	// DateTime corresponding to SearchStart
					// for a particular year
	if ( iterate_forward ) {
		date = new DateTime ( start );
	}
	else {
	    date = new DateTime ( end );
	}
	int year_prev = date.getYear();
	int year = 0;
	TSIterator tsi = ts.iterator();
	TSData data = null;
	DateTime AnalysisWindowEndInYear_DateTime = null;    // End of analysis window in a year
	boolean need_to_analyze = true;	// Need to analyze value for current year
	boolean first_interval = true;
	int missing_count = 0;     // missing count in a year
	int nonmissing_count = 0;  // nonmissing count in a year
	int gap = 0;			// Number of missing values in a gap
					// at the end of the period.
	boolean end_of_data = false;	// Use to indicate end of data because
					// checking for data == null directly
					// can't be done with SearchStart logic.
	while ( true ) {
		if ( iterate_forward ) {
		    // First call will initialize and return first point.
			data = tsi.next();	
		}
		else {
		    // First call will initialize and return last point.
		    data = tsi.previous();	
		}
		if ( data != null ) {
			date = tsi.getDate();
			year = date.getYear();
		}
		else {
		    end_of_data = true;
		}
		if ( (year != year_prev) ||	// New year so save previous year's data value.
			(end_of_data) ||	// End of data so save previous year's data value.
			first_interval ) {	// First interval so initialize (but do not save).
			// New year or last interval so save the results from the previous interval analysis...
			if ( !first_interval ) {
				year_date.setYear ( year_prev );
				if ( Statistic_isCount &&
					yearts.isDataMissing(year_value) &&
					(missing_count <= AllowMissingCount_int) ) {
					// Never assigned a count but missing
					// data were not an issue so assign the
					// value to 0.
					year_value = 0.0;
				}
				// Now recheck to see if the value should be set (not missing)...
				if ( !yearts.isDataMissing(year_value) ) {
					// Have a value to assign to the statistic...
				    if ( Statistic_int == Statistic_Total ) {
				        if ( (missing_count <= AllowMissingCount_int) && !yearts.isDataMissing(year_value) ) {
				            if ( Message.isDebugOn ) {
				                Message.printDebug ( dl, routine, "Setting " + date + " year value=" + year_value );
				            }
				            yearts.setDataValue ( year_date, year_value );
				        }
				    }
				    else if ( Statistic_int == Statistic_Mean ) {
                        if ( (missing_count <= AllowMissingCount_int) && (nonmissing_count > 0) &&
                                !yearts.isDataMissing(year_value) ) {
                            year_value = year_value/(double)nonmissing_count;
                            if ( Message.isDebugOn ) {
                                Message.printDebug ( dl, routine, "Setting " + date + " year value=" + year_value );
                            }
                            yearts.setDataValue ( year_date, year_value );
                        }
                    }
				    else {
				        // Simple assignment of a statitic.
	                    yearts.setDataValue ( year_date, year_value );
	                    if ( Message.isDebugOn ) {
	                        Message.printDebug ( dl, routine,
	                        "Setting value for "+ year_prev + " to " + year_value );
	                    }
				    }
				}
			}
			if ( end_of_data ) {
				// All data have been processed.
				break;
			}
			// Do the following for the first interval or if a new
			// year has started...
			// Initialize for next processing interval...
			first_interval = false;	// Other checks will now control
			year_value = yearts.getMissing();
			extreme_value = yearts.getMissing();
			missing_count = 0;
			nonmissing_count = 0;
			year_prev = year;
			need_to_analyze = true;
			// Adjust the starting point if necessary.  Find the
			// nearest value later than or equal to the search start...
			// FIXME SAM 2008-02-05 Need to phase out SearchStart and just use the analysis window.
			// For now use the search start to be the start of the search.
			if ( (SearchStart_DateTime != null) || (AnalysisWindowStart_DateTime != null) ) {
				date_search = new DateTime ( date );
				if ( SearchStart_DateTime != null ) {
				    date_search.setMonth( SearchStart_DateTime.getMonth());
				    date_search.setDay ( SearchStart_DateTime.getDay());
                    if (Message.isDebugOn) {
                        Message.printDebug ( dl, routine,
                        "Will start processing in year on SearchStart: " + date_search );
                    }
				}
				if ( AnalysisWindowStart_DateTime != null ) {
				    // The AnalysisWindow takes precendence.
                    date_search.setMonth( AnalysisWindowStart_DateTime.getMonth());
                    date_search.setDay ( AnalysisWindowStart_DateTime.getDay());
                    // Also set the end date in the year to include.
                    AnalysisWindowEndInYear_DateTime = new DateTime(date_search);
                    AnalysisWindowEndInYear_DateTime.setMonth( AnalysisWindowEnd_DateTime.getMonth());
                    AnalysisWindowEndInYear_DateTime.setDay ( AnalysisWindowEnd_DateTime.getDay());
                    if (Message.isDebugOn) {
                        Message.printDebug ( dl, routine,
                        "Will start processing in year on AnalysisWindowStart: " + date_search );
                        Message.printDebug ( dl, routine,
                        "Will end processing in year on AnalysisWindowEnd: " + AnalysisWindowEndInYear_DateTime );
                    }
				}
				data = tsi.goTo ( date_search, false );
				if ( data == null ) {
					// Did not find the requested starting date so must have run out of data.
					// The original date still applies in some cases.
				    // Also evaluate for missing data if a regular time series.
					if (Message.isDebugOn) {
						Message.printDebug ( dl, routine, "Did not find search start using " + date_search );
					}
					if ( is_regular ) {
						// Need to skip over the end period to a date in the period,
					    // keeping track of the missing count...
						if ( iterate_forward ) {
							if ( date_search.greaterThan( date) ) {
								// Ran out of data at end...
								gap = -1;
							}
							else {
							    // Not enough data before search start...
								gap = TimeUtil.getNumIntervals(	TimeUtil.min(date_search,start),
								TimeUtil.max( date_search, start),
								interval_base, interval_mult );
							}
						}
						else {
						    // Iterate backward
							if ( date_search.lessThan( start) ) {
								// Ran out of data at end...
								gap = -1;
							}
							else {
							    // Not enough data before search start...
								gap = TimeUtil.getNumIntervals(	TimeUtil.min( end, date_search),
								TimeUtil.max( end, date_search),
								interval_base, interval_mult );
							}
						}
						if ( gap >= 0 ) {
							if (Message.isDebugOn) {
								Message.printDebug ( dl, routine,
								"Found " + gap + " missing values between search start and data." );
							}
							missing_count += gap;
						}
						else {
						    // Don't have enough data...
							need_to_analyze = false;
							end_of_data = true;
							if (Message.isDebugOn) {
								Message.printDebug ( dl, routine,
								"Don't have data at end of period to analyze." );
							}
						}
					}
					// Irregular...
					// just process what is available.
					// TODO SAM 2005-09-27
					// Need to review this when other than
					// Day interval data are supported.
				}
				else {
				    // Able to position the iterator so
					// reset the date of the iterator
					// and process below...
					date = data.getDate();
				}
			}
		}
		// Analyze...
		// If need_to_analyze is false, then the data can be skipped.
		// This will occur either if the value is found or too much
		// missing data have been found and the result cannot be used.
		// TODO SAM 2005-09-22
		// Some of the following is in place because a TSIterator is
		// being used to accomodate regular and irregular data.  It
		// should be possible to jump over data but for now the brute
		// force search is performed.
		if ( need_to_analyze && !end_of_data ) {
		    if ( (AnalysisWindowEndInYear_DateTime != null) && date.greaterThan(AnalysisWindowEndInYear_DateTime) ) {
		        // Just skip the data.
		        continue;
		    }
			value = tsi.getDataValue ();
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Processing " + date + " value=" + value +
				        " year value (before this value)=" + year_value );
			}
			// Put an initial check because the missing data count
			// could have been set while setting the SearchStart...
			if ( missing_count > AllowMissingCount_int ) {
				// Have too much missing data to generate the statistic...
				need_to_analyze = false;
				Message.printDebug ( dl, "",
				"Will not analyze year because more" +
				" than " + AllowMissingCount_int +
				" missing values were found (" + missing_count +
				")." );
				continue;
			}
			// If missing data have not been a problem so far, continue with the check...
			if ( ts.isDataMissing ( value ) ) {
				++missing_count;
				if ( missing_count > AllowMissingCount_int ) {
					// Have too much missing data to generate the statistic...
					need_to_analyze = false;
					Message.printDebug ( dl, "",
					"Will not analyze year because more than " + AllowMissingCount_int +
					" missing values were found." );
				}
			}
			else {
			    // Data value is not missing so evaluate the test...
			    ++nonmissing_count;
				if ( (Test_int == Test_GE) && (value >= TestValue_double) ) {
					if (Statistic_int == Statistic_CountGE){
						if(yearts.isDataMissing( year_value) ) {
							year_value = 1.0;
						}
						else {
						    year_value += 1.0;
						}
					}
					else if ((Statistic_int == Statistic_DayOfFirstGE) ||
						(Statistic_int == Statistic_DayOfLastGE) ){
						year_value = TimeUtil.dayOfYear(date);
						need_to_analyze = false;	
						// Found value for the year.
					}
				}
				else if((Test_int == Test_GT) && (value > TestValue_double) ) {
					if (Statistic_int == Statistic_CountGT){
						if(yearts.isDataMissing( year_value) ) {
							year_value = 1.0;
						}
						else {
						    year_value += 1.0;
						}
					}
					else if ((Statistic_int == Statistic_DayOfFirstGT) ||
						(Statistic_int == Statistic_DayOfLastGT) ){
						year_value = TimeUtil.dayOfYear( date);
						need_to_analyze = false;	
						// Found value for the year.
					}
				}
				else if((Test_int == Test_LE) && (value <= TestValue_double) ) {
					if (Statistic_int == Statistic_CountLE){
						if(yearts.isDataMissing( year_value) ) {
							year_value = 1.0;
						}
						else {
						    year_value += 1.0;
						}
					}
					else if ((Statistic_int == Statistic_DayOfFirstLE) ||
						(Statistic_int == Statistic_DayOfLastLE) ){
						year_value = TimeUtil.dayOfYear( date);
						need_to_analyze = false;	
						// Found value for the year.
					}
				}
				else if((Test_int == Test_LT) && (value < TestValue_double) ) {
					if (Statistic_int == Statistic_CountLT){
						if(yearts.isDataMissing( year_value) ) {
							year_value = 1.0;
						}
						else {
						    year_value += 1.0;
						}
					}
					else if ((Statistic_int == Statistic_DayOfFirstLT) ||
						(Statistic_int == Statistic_DayOfLastLT) ){
						year_value = TimeUtil.dayOfYear( date);
						need_to_analyze = false;	
						// Found value for the year.
					}
				}
				else if ( Test_int == Test_Max ) {
					if(yearts.isDataMissing(extreme_value)||(value > extreme_value) ) {
						// Set the max...
						if ( Statistic_int == Statistic_DayOfMax ) {
							year_value = TimeUtil.dayOfYear( date);
						}
						else {
						    year_value = value;
						}
						extreme_value = value;
					}
					// Need to continue analyzing period so do not set need_to_analyze to false.
				}
				else if ( Test_int == Test_Min ) {
					if(yearts.isDataMissing(extreme_value)||(value < extreme_value) ) {
						// Set the min...
						if ( Statistic_int == Statistic_DayOfMin ) {
							year_value = TimeUtil.dayOfYear( date);
						}
						else {
						    year_value = value;
						}
						extreme_value = value;
					}
					// Need to continue analyzing period so do not set need_to_analyze to false.
				}
                else if( Test_int == Test_Accumulate ) {
                    // Need to accumulate the value (for Mean or Total)
                    // Accumulate into the year_value
                    if(yearts.isDataMissing( year_value) ) {
                        year_value = value;
                    }
                    else {
                        year_value += value;
                    }
                }
			}
		}
	}
	if ( ts.getAlias().length() > 0 ) {
		yearts.addToGenesis ( "Created " + Statistic +
			" statistic time series (TestValue=" + TestValue +
			",SearchStart=" + SearchStart +
			",AllowMissingCount=" + AllowMissingCount +
			") from input: " + ts.getAlias() );
	}
	else {
	    yearts.addToGenesis ( "Created " + Statistic +
			" statistic time series (TestValue=" + TestValue +
			",SearchStart=" + SearchStart +
			",AllowMissingCount=" + AllowMissingCount +
			") from input: " + ts.getIdentifier() );
	}
}

/**
Process a time series to create the DataCoveragePercent statistic.
*/
private void processStatisticDataCoveragePercent (	TS ts, YearTS yearts,
							PropList props,
							DateTime start,
							DateTime end )
throws Exception
{	String routine = "TSAnalyst.processStatisticDataCoveragePercent";
	int dl = 10;
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Not supported.
		throw new TSException (
		"Irregular time series cannot be analyzed." );
	}

	boolean set_missing_if_missing = false;
	String SetMissingIfMissing = props.getValue ( "SetMissingIfMissing" );
	if (	(SetMissingIfMissing != null) &&
		SetMissingIfMissing.equalsIgnoreCase("true") ) {
		set_missing_if_missing = true;
	}

	// Now set to zero...
	TSUtil.setConstant ( yearts, 0.0 );
	// Reset the units to percent...
	yearts.setDataUnits ( "PCT" );
	yearts.setDescription ( yearts.getDescription() +", % data coverage");

	DateTime year_date = new DateTime ( start, DateTime.PRECISION_YEAR );
	double year_value = 0.0, value = 0.0;

	// Loop using addInterval...
	DateTime date = new DateTime ( start );
	int lastyear = date.getYear();
	year_value = yearts.getMissing();
	int data_count = 0;
	int year = 0;
	for (	;
		date.lessThanOrEqualTo( end );
		date.addInterval(interval_base, interval_mult) ) {
		year = date.getYear();
		if ( year != lastyear ) {
			// New year so set last year's values...
			// We are setting the percentage of data found
			// for the year.
			if ( data_count == 0 ) {
				if ( !set_missing_if_missing ) {
					year_value = 0.0;
				}
				else {	year_value =yearts.getMissing();
				}
			}
			else {	int nintervals = 0;
				for ( int im = 1; im <= 12; im++ ) {
					nintervals +=
					TSUtil.numIntervalsInMonth( im,
					lastyear, interval_base,
					interval_mult);
				}
				year_value = (double)data_count*100.0/
						nintervals;
			}
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"For " + date + " found " + data_count
				+ " values." );
			}
			yearts.setDataValue ( year_date, year_value );
			// Now increment the year to be ready for the
			// next year...
			year_date.addYear ( 1 );
			// Reset the value for next year...
			data_count = 0;
		}
		// Now evaluate the data for this interval.
		// If the data value is not missing, add to the value...
		value = ts.getDataValue ( date );
		if ( !ts.isDataMissing(value) ) {
			++data_count;
		}
		// Set the year so we can check in the next iteration...
		lastyear = year;
	}
	// Always do the last value using the same logic as above...
	if ( year != 0 ) {
		// New year so set last year's values...
		// We are setting the percentage of data found
		// for the year.
		if ( data_count == 0 ) {
			if ( !set_missing_if_missing ) {
				year_value = 0.0;
			}
			else {	year_value = yearts.getMissing();
			}
		}
		else {	int nintervals = 0;
			for ( int im = 1; im <= 12; im++ ) {
				nintervals +=
				TSUtil.numIntervalsInMonth( im,
				lastyear, interval_base, interval_mult);
			}
			year_value = (double)
				data_count*100.0/nintervals;
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"For " + date + " found " + data_count
			+ " values." );
		}
		yearts.setDataValue ( year_date, year_value );
	}

	// Set the genesis information...

	ts.addToGenesis ( "Evaluated missing data from " + start.toString() +
		" to " + end.toString() + "." );
}

/**
Run an interactive shell that prompts for commands.
@param ts Time series to analyze.
*/
public static void runShell ( TS ts )
{	String		full_string = "", routine = "TSAnalyst.runShell",
			string = "";
	boolean		showid = false;
	InputStream	console_in = System.in;
	PrintStream	console_out = System.out;
	int		bytes_size = 256;
	byte []		bytes = new byte[bytes_size];
	String		output_file = "console";
	PrintWriter	out = new PrintWriter ( System.out );
	int		dl = 10;

	if ( ts == null ) {
		Message.printWarning ( 1, routine, "No time series" );
		return;
	}
	int i;
	while ( true ) {
		if ( string.length() > 0 ) {
			if ( string.charAt ( string.length() - 1 ) != '\\' ) {
				// Not appending so clear the string...
				full_string = "";
			}
		}
		if ( showid ) {
			console_out.print ( "TS (" + ts.getLocation() + ")> " );
		}
		else {	console_out.print ( "TS> " );
		}
		try {	// Clear the bytes first...
			for ( i = 0; i < bytes_size; i++ ) {
				bytes[i] = 0;
			}
			if ( console_in.read ( bytes ) == - 1 ) {
				// End of file;
				continue;
			}
			string = new String ( bytes );
		}
		catch ( IOException e ) {
			continue;
		}
		if ( string == null ) {
			continue;
		}
		string = string.trim();
		if ( string.charAt ( string.length() - 1 ) == '\\' ) {
			// line continuation...
			full_string = full_string + string;
			// Read another line...
			continue;
		}
		if ( string.length() == 0 ) {
			continue;
		}
		// Prepare the full string for use later...
		full_string = full_string + string;
		if ( Message.isDebugOn ) {
			Message.printStatus ( dl, routine,
			"Full string is \"" + full_string + "\"" );
		}
		// Now check for recognized commands...
		if ( string.regionMatches(true,0,"close",0,5) ) {
			Message.printStatus ( 1, routine, "Close \"" +
			output_file + "\".  Output now printed to console." );
			out.flush();
			out.close();
			out = new PrintWriter ( System.out );
			output_file = "console";
		}
		else if ( string.regionMatches(true,0,"debug",0,5) ) {
			// Set debug level...
			String debug = string.substring ( 5 ).trim();
			Message.setDebugLevel ( debug );
			if ( debug.charAt(0) == '0' ) {
				Message.isDebugOn = false;
			}
			else {	Message.isDebugOn = true;
			}
			debug = null;
		}
		else if ( string.regionMatches(true,0,"limits",0, 6) ) {
			// Get the time series limits...
			try {	String limits_string = null;
				TSLimits limits = null;
				if ( ts.getDataIntervalBase() ==
					TimeInterval.MONTH ) {
					limits = new MonthTSLimits (
						(MonthTS)ts );
				}
				else {	limits = new TSLimits ( ts );
				}
				limits_string = limits.toString();
				out.println ( limits_string );
				Message.printStatus ( 2, routine,limits_string);
				limits = null;
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine,
				"Unable to print time series limits." );
			}
		}
		else if ( string.regionMatches(true,0,"open",0,4) ) {
			// Open the output file...
			try {	output_file = string.substring ( 4 ).trim();
				out = new PrintWriter ( new FileWriter(
					output_file) );
				Message.printStatus ( 2, routine,
				"Output will be printed to \"" + output_file +
				"\"" );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine,
				"Unable to open file \"" + output_file + "\"" );
				output_file = "console";
				out = new PrintWriter ( System.out );
			}
		}
		else if ( string.regionMatches(true,0,"quit",0,4) ) {
			// End session...
			break;
		}
		else if ( string.regionMatches(true,0,"select",0,6) ) {
			// Execute a select on the data...
			new TSAnalyst ( ts, full_string );
		}
		else if ( string.regionMatches(true,0,"showid",0,6) ) {
			// Toggle id...
			if ( showid ) {
				showid = false;
			}
			else {	showid = true;
			}
		}
		else if ( string.regionMatches(true,0,"status",0,6) ) {
			// Print the status of the session...
			Message.printStatus ( 2, routine,
			"Output is printing to \"" + output_file + "\"" );
		}
		else if ( string.regionMatches(true,0,"summary",0, 7) ) {
			PropList props = new PropList ( "summary" );
			props.set ( "PrintAllStats=true" );
			try {	Vector v = TSUtil.formatOutput ( ts, props );
				if ( v == null ) {
					continue;
				}
				int size = v.size();
				for ( i = 0; i < size; i++ ) {
					Message.printStatus ( 2, routine,
					(String)v.elementAt(i) );
					out.println ( (String)v.elementAt(i) );
				}
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, routine,
				"Unable to print time series summary." );
			}
			props = null;
		}
		else {	// Unknown command...
			Message.printWarning ( 1, routine,
			"Unknown command \"" + string + "\"" );
		}
	}
	full_string = null;
	routine = null;
	string = null;
	console_in = null;
	console_out = null;
	bytes = null;
	output_file = null;
	out = null;
}

/**
Start the report used for a data coverage report, which has years across the
top and stations as records.  Each year is shown as 2 digits with slots filled
in with ## for 100% coverage, ** for &gt;= 75% coverage, ++ for &gt;= 50% 
coverage, - for &gt;= 25% coverage, .. for &gt; 0 and
space for zero coverage.  Later may make this a separate class if we can
generalize to other data statistics.
@param start Start date for output.
@param end End date for output.
@param props Properties of the output, as described in the following table:

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>CalendarType</b></td>
<td>The type of calendar, either "WaterYear" (Oct through Sep), "IrrigationYear"
(Nov through Oct), or "CalendarYear" (Jan through Dec).
</td>
<td>CalenderYear (but may be made sensitive to the data type or units in the
future).</td>
</tr>
</table>
*/
public void startDataCoverageReport (	DateTime start, DateTime end,
					PropList props )
throws TSException
{	String delim = "|";
	if ( start == null ) {
		throw new TSException ( "Null start date for report" );
	}
	if ( end == null ) {
		throw new TSException ( "Null end date for report" );
	}
	if ( props == null ) {
		_data_coverage_report_props =
		new PropList ( "DataCoverageReport" );
	}
	else {	_data_coverage_report_props = props;
	}

	// Get the calendar type...

	String year_type = _data_coverage_report_props.getValue("CalendarType");
	if ( year_type == null ) {
		year_type = "CalendarYear";
	}

	// Get the full year dates to use for looping...

	TSLimits limits = null;
	try {	limits = TSUtil.getPeriodFromDates (
		start, end, year_type, 0 );
	}
	catch ( Exception e ) {
		// ignore...
		Message.printWarning ( 2, "TSAnalyst.startDataCoverageReport",
		"Error getting dates for summary report" );
		return;
	}

	_data_coverage_report_date1 = new DateTime ( limits.getDate1() );
	_data_coverage_report_date2 = new DateTime ( limits.getDate2() );
	_data_coverage_report_Vector = new Vector ( 100, 100 );
	_data_coverage_report_Vector.addElement ( "" );
	_data_coverage_report_Vector.addElement ( "Data Coverage Report" );
	_data_coverage_report_Vector.addElement ( "" );
	_data_coverage_report_Vector.addElement (
	"Years shown in the report are for calendar type:  " + year_type );
	_data_coverage_report_Vector.addElement ( "Start:  " +
		_data_coverage_report_date1.toString (DateTime.FORMAT_YYYY_MM));
	_data_coverage_report_Vector.addElement ( "End:    " +
		_data_coverage_report_date2.toString (DateTime.FORMAT_YYYY_MM));
	_data_coverage_report_Vector.addElement ( "" );
	_data_coverage_report_Vector.addElement ( "## indicates 100% coverage");
	_data_coverage_report_Vector.addElement("** indicates >= 75% coverage");
	_data_coverage_report_Vector.addElement("++ indicates >= 50% coverage");
	_data_coverage_report_Vector.addElement("-- indicates >= 25% coverage");
	_data_coverage_report_Vector.addElement(".. indicates > 0% coverage");
	_data_coverage_report_Vector.addElement ("spaces indicate 0% coverage");
	_data_coverage_report_Vector.addElement ( "" );

	// Add a header...

	StringBuffer data_string = new StringBuffer ();
	DateTime date = new DateTime ( _data_coverage_report_date1 );
	for (	; date.lessThanOrEqualTo ( _data_coverage_report_date2);
		date.addYear(1) ) {
		if ( year_type.equalsIgnoreCase("WaterYear") ) {
			// Offset...
			data_string.append ( StringUtil.formatString(
			(date.getYear() + 1),"%04d").substring(2) + delim );
		}
		else {	data_string.append ( StringUtil.formatString(
			date.getYear(),"%04d").substring(2) + delim );
		}
	}
	_data_coverage_report_Vector.addElement (
		StringUtil.formatString("Station","%-20.20s") +
		delim +
		StringUtil.formatString("Name","%-40.40s") +
		delim + data_string.toString() );
	delim = null;
	year_type = null;
	limits = null;
	data_string = null;
	date = null;
}

/**
Return a string representation of the analyst.  Currently returns an empty
string.
@return A verbose string representation of the results.
*/
public String toString ( )
{	return "";
}

} // End of TSAnalyst
