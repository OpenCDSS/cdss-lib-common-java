package RTi.TS;

import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
Create traces from a time series.
*/
public class TSUtil_CreateTracesFromTimeSeries
{

/**
Constructor.
*/
public TSUtil_CreateTracesFromTimeSeries ()
{
	// Does nothing.
}

/**
Break a time series into a vector of annual traces.  The description is
altered to indicate the year of the trace (e.g., "1995 trace: ...") and the
time series sequence number is set to the year for the start of the trace.
@return A Vector of the trace time series or null if an error.
@param ts Time series to break.  The time series is not changed.
@param interval_string The length of each trace.  Specify as an interval string
like "1Year".  The interval can be longer than one year.  If blank, "1Year" is
used as the default.
@param reference_date Date on which each time series trace is to start.
If the reference_date is null, the default is Jan 1 for daily data, Jan for
monthly data.  If specified, the precision of the reference date should match
that of the time series being examined.
@param shift_type If "NoShift", then the traces are not shifted.  If annual
traces are requested.  The total list of time series when plotted should match
the original time series.
If "ShiftToReference", then the start of each time
series is shifted to the reference date.  Consequently, when plotted, the time
series traces will overlay.
@param start_date First allowed date (use to constrain how many years of the
time series are processed).
@param end_date Last allowed date (use to constrain how many years of the time
series are processed).
@exception Exception if there is an error processing the time series
*/
public Vector getTracesFromTS (  TS ts, String interval_string, DateTime reference_date,
                    String shift_type, DateTime start_date, DateTime end_date )
throws Exception
{   String routine = getClass().getName() + ".getTracesFromTS";
    if ( ts == null ) {
        return null;
    }

    // For now do not handle IrregularTS (too hard to handle all the shifts
    // to get dates to line up nice.

    if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
        Message.printWarning ( 2, "TSUtil.getTracesFromTS", "Can't handle irregular TS." );
        return null;
    }
    int interval_base = ts.getDataIntervalBase();
    int interval_mult = ts.getDataIntervalMult();
    boolean do_shift = false;
    if ( (shift_type != null) && (shift_type.equalsIgnoreCase("ShiftToReference")) ) {
        do_shift = true;
    }

    // First determine the overall period in the original time series that
    // is to be processed...

    TSLimits valid_dates = TSUtil.getValidPeriod ( ts, start_date, end_date );
    DateTime start = valid_dates.getDate1();
    DateTime end = valid_dates.getDate2();
    valid_dates = null;

    // Make sure there is a valid reference date for the start date/time...

    DateTime use_reference_date = null;
    if ( reference_date == null ) {
        // Create a reference date that is of the correct precision...
        use_reference_date = TSUtil.newPrecisionDateTime ( ts, null );
        // Now reset to Jan 1...
        use_reference_date.setMonth(1);
        use_reference_date.setDay(1);
        use_reference_date.setHour(1);
        use_reference_date.setMinute(1);
    }
    else {  // Make sure the reference date is of the right precision...
        use_reference_date = TSUtil.newPrecisionDateTime ( ts, reference_date);
    }
    //if ( Message.isDebugOn ) {
        Message.printStatus ( 2, routine, "Reference date is " + use_reference_date.toString() );
    //}

    // Because of complexities with the shift, only allow start of year
    // reference dates...

    if ( (use_reference_date.getDay() != 1) || (use_reference_date.getMonth() != 1) ) {
        String message = "To create traces, can only use a reference date that is Jan 1 of a year.";
        Message.printWarning ( 1, "TSUtil.getTracesFromTS", message );
        throw new Exception ( message );
    }

    // Determine the start/end date/time for the first trace.  The start
    // date has the month, day, etc., of the reference date and a year that
    // is <= to the first date in the time series...

    DateTime date1 = new DateTime ( use_reference_date );
    date1.setYear ( start.getYear() );
    // Make sure we get the first year...
    if ( date1.greaterThan(start) ) {
        date1.addYear ( -1 );
    }

    // The end date is the start date plus the specified interval...

    DateTime date2 = new DateTime ( date1 );
    TimeInterval tsi = TimeInterval.parseInterval ( interval_string );
    if ( tsi.getBase() == 0 ) {
        // Use the default...
        date2.addYear ( 1 );
        date2.addMonth ( -1 );
    }
    else {  // Use the interval specified...
        date2.addInterval ( tsi.getBase(), tsi.getMultiplier() );
        date2.addDay ( -1 );
    }

    // Allocate the Vector for traces, estimating the size based on the
    // full period...

    Vector tslist = new Vector ( end.getYear() - start.getYear() + 1 );

    // Use a while loop to go through the traces, stopping when the start
    // and end dates for the trace are outside the requested limits...

    TS newts = null;
    TS shifted_ts = null;
    DateTime date = null;
    while ( true ) {
        // If "newts" is not null and we need to shift it, do so, but
        // shift the same day.  This should have been enforced above...
        if ( (newts != null) && do_shift ) {
            Message.printStatus ( 2, routine,
            "Calling shift with " + newts.getDate1() + " and " + use_reference_date );
            shifted_ts = TSUtil.shift ( newts, newts.getDate1(), use_reference_date );
            // Replace in the list...
            tslist.setElementAt ( shifted_ts, (tslist.size() - 1) );
        }
        // Determine if done...
        if ( date1.greaterThan(end) ) {
            break;
        }
        // Create a new time series using the old as input...
        newts = TSUtil.newTimeSeries ( ts.getIdentifierString(), true);
        newts.copyHeader ( ts );
        newts.setDescription ( date1.getYear() + " trace: " + newts.getDescription() );
        newts.addToGenesis ( "Split trace out of time series for " + date1 + " to " + date2 );
        newts.setDate1 ( date1 );
        newts.setDate2 ( date2 );
        newts.setSequenceNumber ( date1.getYear() );
        //if ( Message.isDebugOn ) {
            Message.printStatus ( 1, "",
            "Created new trace for year " + date1 + " allocated for " + date1 + " to " + date2 );
        //}
        // Allocate memory using the offset dates...
        newts.allocateDataSpace();
        // Add to the list...
        tslist.addElement ( newts );
        // Now transfer the data...
        for ( date = new DateTime(date1); date.lessThanOrEqualTo(date2);
            date.addInterval(interval_base, interval_mult) ) {
            newts.setDataValue ( date, ts.getDataValue(date) );
        }
        // Increment the dates by one year...
        date1.addYear ( 1 );
        date2.addYear ( 1 );
    }
    date = null;
    date1 = null;
    date2 = null;
    end = null;
    newts = null;
    shifted_ts = null;
    start = null;
    return tslist;
}

}
