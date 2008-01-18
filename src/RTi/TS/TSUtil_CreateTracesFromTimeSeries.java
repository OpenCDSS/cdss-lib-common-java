package RTi.TS;

import java.util.Vector;

import RTi.Util.Message.Message;
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
private DateTime computeDateWithOffset ( String info, TS ts, DateTime ReferenceDate_DateTime,
        DateTime date1, TimeInterval offset )
{
    // The end date is the start date plus the specified trace length...

    if ( offset == null ) {
        // Use the default (one year)...
        offset = new TimeInterval ( TimeInterval.YEAR, 1 );
    }
    // Add the interval to the reference date...
    DateTime ref_adjusted = null;
    // Use the reference date as the starting point to compute the number of intervals in trace...
    ref_adjusted = new DateTime(ReferenceDate_DateTime);
    ref_adjusted.addInterval ( offset.getBase(), offset.getMultiplier() );
    // Need to subtract one time series interval to make the trace length be as requested
    // (as such it is one interval too long)...  This is because addInterval() adds 1 to the year,
    // not add 1 year duration (a subtle issue).
    ref_adjusted.addInterval ( ts.getDataIntervalBase(), -1*ts.getDataIntervalMult() );
    int nintervals = TSUtil.calculateDataSize ( ts, ReferenceDate_DateTime, ref_adjusted );
    // Subtract one interval to avoid counting both end points...
    --nintervals;
    DateTime date2 = new DateTime ( date1 );
    date2.addInterval( ts.getDataIntervalBase(), nintervals*ts.getDataIntervalMult() );
   
    //Message.printStatus( 2, "", "For " + info + " start date " + date1 + " and trace length " + offset + " end date is " + date2 );
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
                "input", ts, ReferenceDate_DateTime2, date1_in, TraceLength_TimeInterval );
        // The output trace start depends on the ShiftDataHow parameter...
        if ( ShiftToReference_boolean ) {
            // Output should be shifted to the reference date...
            date1_out = new DateTime ( ReferenceDate_DateTime2 );
        }
        else {
            // The start of the trace is the same as the original data, which is iterating through above...
            date1_out = new DateTime ( date1_in );
        }
        DateTime date2_out = computeDateWithOffset (
                "output", ts, ReferenceDate_DateTime2, date1_out, TraceLength_TimeInterval );;
        // Break out if the start and end dates in the source data are outside the requested range:
        if ( InputEnd_DateTime.lessThan(date1_in) || InputStart_DateTime.greaterThan(date2_in) ) {
            // Trace does not overlap data so done processing.
            Message.printStatus(2, routine, "Breaking out of processing InputStart=" + InputStart_DateTime +
                    " InputEnd=" + InputEnd_DateTime + " date1_in=" + date1_in + " date2_in=" + date2_in );
            break;
        }
        // Create a new time series using the old header as input...
        TS tracets = TSUtil.newTimeSeries ( ts.getIdentifierString(), true);
        tracets.copyHeader ( ts );
        tracets.setSequenceNumber ( date1_in.getYear() );
        tracets.setAlias( ts.getLocation() + "_" + tracets.getSequenceNumber() );
        tracets.setDescription ( date1_in.getYear() + " trace: " + tracets.getDescription() );
        tracets.addToGenesis ( "Split trace out of time series for input: " + date1_in + " to " + date2_in +
                ", output: " + date1_out + " to " + date2_out );
        tracets.setDate1 ( date1_out );
        tracets.setDate2 ( date2_out );
        
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

}
