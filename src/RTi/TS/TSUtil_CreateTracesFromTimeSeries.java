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
Compute the end date for the trace given the start and the offset.  This can be used
for the source data and the end result.
@param date1 the original date/time.
@param offset the interval by which to offset the original date/time.
@return a new DateTime that is offset from the original by the specified interval.
*/
private DateTime computeDateWithOffset ( TS ts, DateTime ReferenceDate_DateTime, String ShiftDataHow,
        DateTime date1, TimeInterval offset )
{
    // If shifting the data to the reference, the number of days may need to be adjusted so that each
    // trace length is the same as the reference year.
    boolean check_leap = false;
    if ( ShiftDataHow.equalsIgnoreCase("ShiftToReference") ) {
        check_leap = true;
    }
    // The end date is the start date plus the specified trace length...

    DateTime date2 = new DateTime ( date1 );
    if ( offset == null ) {
        // Use the default (one year)...
        offset = new TimeInterval ( TimeInterval.YEAR, 1 );
    }
    date2.addInterval ( offset.getBase(), offset.getMultiplier() );
    // Need to subtract one time series interval to make the trace length be as requested
    // (as such it is one interval too long)...
    date2.addInterval ( ts.getDataIntervalBase(), -1*ts.getDataIntervalMult() );
    int leapcount = 0;
    if ( check_leap && (date1.getYear() != ReferenceDate_DateTime.getYear()) ) {
        // Also need to subtract one day for every leap year that is included in the period.  Otherwise
        // each leap year will have an extra day of data in the sequence.
        // Only do this when the starting date is not the reference year.
            for ( DateTime date = new DateTime(date1); date.lessThanOrEqualTo(date2); date.addYear(1) ) {
            if ( date.isLeapYear() ) {
                ++leapcount;
            }
        }
        
        if ( ReferenceDate_DateTime.isLeapYear() && !date1.isLeapYear() ) {
            // Add the days...
            date2.addDay( leapcount );
        }
        else if ( !ReferenceDate_DateTime.isLeapYear() && date1.isLeapYear() ) {
            // Subtract the days...
            leapcount *= -1;
            date2.addDay( leapcount );
        }
    }
    
    Message.printStatus( 2, "", "For start date " + date1 + " and trace length " + offset +
            " end date is " + date2 + " with " + leapcount + " leap year days added." );
    return date2;
}

/**
Break a time series into a vector of annual traces.  The description is
altered to indicate the year of the trace (e.g., "1995 trace: ...") and the
time series sequence number is set to the year for the start of the trace.
@return A Vector of the trace time series or null if an error.
@param ts Time series to break.  The time series is not changed.
@param TraceLength The length of each trace.  Specify as an interval string
like "1Year".  The interval can be longer than one year.  If blank, "1Year" is
used as the default.
@param ReferenceDate_DateTime Date on which each time series trace is to start.
If the reference_date is null, the default is Jan 1 for daily data, Jan for
monthly data.  If specified, the precision of the reference date should match
that of the time series being examined.
@param ShiftDataHow If "NoShift", then the traces are not shifted.  If annual
traces are requested.  The total list of time series when plotted should match
the original time series.
If "ShiftToReference", then the start of each time
series is shifted to the reference date.  Consequently, when plotted, the time
series traces will overlay.
@param InputStart_DateTime First allowed date (use to constrain how many years of the
time series are processed).
@param InputEnd_DateTime Last allowed date (use to constrain how many years of the time
series are processed).
@exception Exception if there is an error processing the time series
@exception IrregularTimeSeriesNotSupportedException if the method is called with an
irregular time series.
*/
public Vector getTracesFromTS (  TS ts, String TraceLength, DateTime ReferenceDate_DateTime,
                    String ShiftDataHow, DateTime InputStart_DateTime, DateTime InputEnd_DateTime )
throws IrregularTimeSeriesNotSupportedException, Exception
{   String routine = getClass().getName() + ".getTracesFromTS";
    if ( ts == null ) {
        return null;
    }

    // For now do not handle IrregularTS (too hard to handle all the shifts
    // to get dates to line up nice.

    if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
        String message = "Can't handle irregular TS \"" + ts.getIdentifier() + "\".";
        Message.printWarning ( 2, "TSUtil.getTracesFromTS", message );
        throw new IrregularTimeSeriesNotSupportedException ( message );
    }
    int interval_base = ts.getDataIntervalBase();
    int interval_mult = ts.getDataIntervalMult();
    boolean ShiftToReference_boolean = false;
    if ( (ShiftDataHow != null) && (ShiftDataHow.equalsIgnoreCase("ShiftToReference")) ) {
        ShiftToReference_boolean = true;
    }
    
    // Get the trace length as an interval...
    
    TimeInterval TraceLength_TimeInterval = null;
    if ( TraceLength != null ) {
        TraceLength_TimeInterval = TimeInterval.parseInterval ( TraceLength );
    }

    // First determine the overall period in the original time series that is to be processed...

    TSLimits valid_dates = TSUtil.getValidPeriod ( ts, InputStart_DateTime, InputEnd_DateTime );
    // Reset the reference to the input period...
    InputStart_DateTime = new DateTime(valid_dates.getDate1());
    InputEnd_DateTime = new DateTime(valid_dates.getDate2());

    // Make sure there is a valid reference date for the start date/time...

    DateTime ReferenceDate_DateTime2 = null;
    if ( ReferenceDate_DateTime == null ) {
        // Create a reference date that is of the correct precision...
        ReferenceDate_DateTime2 = TSUtil.newPrecisionDateTime ( ts, null );
        // The year will be set to each year in the source time series, or that of the reference date.
        // Now reset to Jan 1...
        ReferenceDate_DateTime2.setMonth(1);
        ReferenceDate_DateTime2.setDay(1);
        // TODO SAM 2007-12-13 Evaluate how the following works for INST, MEAN, ACCM data.
        ReferenceDate_DateTime2.setHour(0);
        ReferenceDate_DateTime2.setMinute(0);
    }
    else {
        // Make sure the reference date is of the right precision...
        ReferenceDate_DateTime2 = TSUtil.newPrecisionDateTime ( ts, ReferenceDate_DateTime);
    }
    //if ( Message.isDebugOn ) {
        Message.printStatus ( 2, routine, "Reference date is " + ReferenceDate_DateTime2.toString() );
    //}

    // Allocate the Vector for traces...

    Vector tslist = new Vector ();
    
    // Allocate start dates for the input and output time series by copying the reference date.
    // The precision and position within the year will therefore be correct.  Set the year below
    // as needed.
    
    DateTime date1_in = new DateTime ( ReferenceDate_DateTime2 );
    DateTime date1_out = new DateTime ( ReferenceDate_DateTime2 );

    // Loop to go through the traces, stopping when both the start
    // and end dates for the trace are outside the requested limits...
    
    for ( int itrace = 0; ; itrace++ ) {
        // Determine the start and end date/times for the source data and the resulting data.
        // The input time series should loop through the years 
        date1_in.setYear ( ts.getDate1().getYear() + itrace );
        DateTime date2_in = computeDateWithOffset (
                ts, ReferenceDate_DateTime, ShiftDataHow, date1_in, TraceLength_TimeInterval );
        // The output trace start depends on the ShiftDataHow parameter...
        if ( ShiftDataHow.equalsIgnoreCase("ShiftToReference") ) {
            // Output should be shifted to the reference date...
            date1_out = new DateTime ( ReferenceDate_DateTime2 );
        }
        else {
            // The start of the trace is the same as the original data, which is iterating through
            // above...
            date1_out = new DateTime ( date1_in );
        }
        DateTime date2_out = computeDateWithOffset (
                ts, ReferenceDate_DateTime, ShiftDataHow, date1_out, TraceLength_TimeInterval );;
        // Break out if the start and end dates in the source data are outside the requested range:
        if ( (InputStart_DateTime != null) && (InputEnd_DateTime != null) ) {
            if ( InputEnd_DateTime.lessThan(date1_in) || InputStart_DateTime.greaterThan(date2_in) ) {
                break;
            }
        }
        // Create a new time series using the old header as input...
        TS tracets = TSUtil.newTimeSeries ( ts.getIdentifierString(), true);
        tracets.copyHeader ( ts );
        tracets.setDescription ( date1_in.getYear() + " trace: " + tracets.getDescription() );
        tracets.addToGenesis ( "Split trace out of time series for input: " + date1_in + " to " + date2_in +
                ", output: " + date1_out + " to " + date2_out );
        tracets.setDate1 ( date1_out );
        tracets.setDate2 ( date2_out );
        tracets.setSequenceNumber ( date1_in.getYear() );
        //if ( Message.isDebugOn ) {
            Message.printStatus ( 2, "",
            "Created new trace for year " + date1_in + " allocated for input: " + date1_in + " to " + date2_in +
            ", output:" + date1_out + " to " + date2_out );
        //}
        // Allocate the data space...
        tracets.allocateDataSpace();
        // Transfer the data using iterators so that the data sequence is continuous over leap years, etc.
        TSIterator tsi_in = ts.iterator ( date1_in, date2_in );
        TSIterator tsi_out = tracets.iterator ( date1_out, date2_out );
        TSData data_out = null;
        while ( tsi_in.next() != null ) {
            // Get the corresponding point in the output.
            data_out = tsi_out.next();
            // Only transfer if output is not null because null indicates the end of the
            // output time series iterator period has been reached.
            if ( data_out != null ) {
                tracets.setDataValue( tsi_out.getDate(), ts.getDataValue(tsi_in.getDate()));
            }
        }
        // Add the trace to the list of time series...
        tslist.addElement ( tracets );
    }
    return tslist;
}

/**
Break a time series into a vector of annual traces.  The description is
altered to indicate the year of the trace (e.g., "1995 trace: ...") and the
time series sequence number is set to the year for the start of the trace.
@return A Vector of the trace time series or null if an error.
@param ts Time series to break.  The time series is not changed.
@param TraceLength The length of each trace.  Specify as an interval string
like "1Year".  The interval can be longer than one year.  If blank, "1Year" is
used as the default.
@param ReferenceDate_DateTime Date on which each time series trace is to start.
If the reference_date is null, the default is Jan 1 for daily data, Jan for
monthly data.  If specified, the precision of the reference date should match
that of the time series being examined.
@param ShiftDataHow If "NoShift", then the traces are not shifted.  If annual
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
@exception IrregularTimeSeriesNotSupportedException if the method is called with an
irregular time series.
*/
private Vector OLD_getTracesFromTS (  TS ts, String TraceLength, DateTime ReferenceDate_DateTime,
                    String ShiftDataHow, DateTime start_date, DateTime end_date )
throws IrregularTimeSeriesNotSupportedException, Exception
{   String routine = getClass().getName() + ".getTracesFromTS";
    if ( ts == null ) {
        return null;
    }

    // For now do not handle IrregularTS (too hard to handle all the shifts
    // to get dates to line up nice.

    if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
        String message = "Can't handle irregular TS \"" + ts.getIdentifier() + "\".";
        Message.printWarning ( 2, "TSUtil.getTracesFromTS", message );
        throw new IrregularTimeSeriesNotSupportedException ( message );
    }
    int interval_base = ts.getDataIntervalBase();
    int interval_mult = ts.getDataIntervalMult();
    boolean ShiftToReference_boolean = false;
    if ( (ShiftDataHow != null) && (ShiftDataHow.equalsIgnoreCase("ShiftToReference")) ) {
        ShiftToReference_boolean = true;
    }

    // First determine the overall period in the original time series that is to be processed...

    TSLimits valid_dates = TSUtil.getValidPeriod ( ts, start_date, end_date );
    DateTime start = valid_dates.getDate1();
    DateTime end = valid_dates.getDate2();
    valid_dates = null;

    // Make sure there is a valid reference date for the start date/time...

    DateTime ReferenceDate_DateTime2 = null;
    if ( ReferenceDate_DateTime == null ) {
        // Create a reference date that is of the correct precision...
        ReferenceDate_DateTime2 = TSUtil.newPrecisionDateTime ( ts, null );
        // The year will be set to each year in the source time series, or that of the reference date.
        // Now reset to Jan 1...
        ReferenceDate_DateTime2.setMonth(1);
        ReferenceDate_DateTime2.setDay(1);
        // TODO SAM 2007-12-13 Evaluate how the following works for INST, MEAN, ACCM data.
        ReferenceDate_DateTime2.setHour(0);
        ReferenceDate_DateTime2.setMinute(0);
    }
    else {
        // Make sure the reference date is of the right precision...
        ReferenceDate_DateTime2 = TSUtil.newPrecisionDateTime ( ts, ReferenceDate_DateTime);
    }
    //if ( Message.isDebugOn ) {
        Message.printStatus ( 2, routine, "Reference date is " + ReferenceDate_DateTime2.toString() );
    //}

    // Because of complexities with the shift, only allow start of year reference dates...
/* FIXME SAM 2007-12-13 This should not be a limitation!
    if ( (ReferenceDate_DateTime2.getDay() != 1) || (ReferenceDate_DateTime2.getMonth() != 1) ) {
        String message = "To create traces, can only use a reference date that is Jan 1 of a year.";
        Message.printWarning ( 1, "TSUtil.getTracesFromTS", message );
        throw new Exception ( message );
    }
    */

    // Determine the start/end date/time for the first trace.  The start
    // date has the month, day, etc., of the reference date and a year that
    // is <= to the first date in the time series...

    DateTime date1 = new DateTime ( ReferenceDate_DateTime2 );
    date1.setYear ( start.getYear() );
    // Make sure we get the first year...
    if ( date1.greaterThan(start) ) {
        date1.addYear ( -1 );
    }

    // The end date is the start date plus the specified trace length...

    DateTime date2 = new DateTime ( date1 );
    TimeInterval tsi = TimeInterval.parseInterval ( TraceLength );
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
        if ( (newts != null) && ShiftToReference_boolean ) {
            Message.printStatus ( 2, routine,
            "Calling shift with " + newts.getDate1() + " and " + ReferenceDate_DateTime2 );
            shifted_ts = TSUtil.shift ( newts, newts.getDate1(), ReferenceDate_DateTime2 );
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
