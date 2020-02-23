// TSUtil_CreateTracesFromTimeSeries - create traces from a time series.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

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
Break a time series into a list of annual traces.  The description is
altered to indicate the year of the trace (e.g., "1995 trace: ...") and the
time series sequence number is set to the year for the start of the trace.
@return A list of the trace time series or null if an error.
@param ts Time series to break.  The time series is not changed.
@param TraceLength The length of each trace.  Specify as an interval string
like "1Year".  The interval can be longer than one year.  If blank, "1Year" is used as the default.
@param ReferenceDate_DateTime Date on which each time series trace is to start.
If the reference_date is null, the default is Jan 1 for daily data, Jan for
monthly data.  If specified, the precision of the reference date should match
that of the time series being examined.
@param outputYearType the output year type, used to set the sequence number
@param ShiftDataHow If "NoShift", then the traces are not shifted.  If annual
traces are requested.  The total list of time series when plotted should match the original time series.
If "ShiftToReference", then the start of each time
series is shifted to the reference date.  Consequently, when plotted, the time series traces will overlay.
@param transferDataHowType indicates how to transfer time series data, if null default to TransferDataHowType.SEQUENTIAL.
@param InputStart_DateTime First allowed date (use to constrain how many years of the time series are processed).
@param InputEnd_DateTime Last allowed date (use to constrain how many years of the time series are processed).
@param alias alias format string
@param descriptionFormat format to use to format the description using % specifiers
(default is "%z trace: %D").
@param createData if true fill the data values; if false, only set the metadata
@exception Exception if there is an error processing the time series
@exception IrregularTimeSeriesNotSupportedException if the method is called with an irregular time series.
*/
public List<TS> getTracesFromTS ( TS ts, String TraceLength, DateTime ReferenceDate_DateTime,
    YearType outputYearType, String ShiftDataHow, TransferDataHowType transferDataHowType,
    DateTime InputStart_DateTime, DateTime InputEnd_DateTime,
    String alias, String descriptionFormat, boolean createData )
throws IrregularTimeSeriesNotSupportedException, Exception
{   String routine = getClass().getSimpleName() + ".getTracesFromTS";
    if ( ts == null ) {
        return null;
    }
    
    if ( transferDataHowType == null ) {
    	transferDataHowType = TransferDataHowType.SEQUENTIALLY;
    }

    // For now do not handle IrregularTS (too hard to handle all the shifts
    // to get dates to line up nice.

    if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
        String message = "Can't handle irregular TS \"" + ts.getIdentifier() + "\".";
        Message.printWarning ( 2, routine, message );
        throw new IrregularTimeSeriesNotSupportedException ( message );
    }
    boolean ShiftToReference_boolean = false;
    if ( (ShiftDataHow != null) && (ShiftDataHow.equalsIgnoreCase("ShiftToReference")) ) {
        ShiftToReference_boolean = true;
    }

    // Alias for each trace...
    String aliasDefault = "%L_%z";
    if ( (alias == null) || alias.isEmpty() ) {
        alias = aliasDefault;
    }
    
    // Description for each trace
    if ( (descriptionFormat == null) || descriptionFormat.isEmpty() ) {
    	descriptionFormat = "%z trace: %D";
    }
    
    // Get the trace length as an interval...
    
    TimeInterval TraceLength_TimeInterval = null;
    if ( TraceLength != null ) {
        TraceLength_TimeInterval = TimeInterval.parseInterval ( TraceLength );
    }

    // First determine the overall period in the original time series that is to be processed
    // This will limit the data to be processed from the original

    TSLimits valid_dates = TSUtil.getValidPeriod ( ts, InputStart_DateTime, InputEnd_DateTime );
    // Reset the reference to the input period...
    InputStart_DateTime = new DateTime(valid_dates.getDate1());
    InputEnd_DateTime = new DateTime(valid_dates.getDate2());
    Message.printStatus(2, routine, "Period for input time series is InputStart=" + InputStart_DateTime +
        " InputEnd=" + InputEnd_DateTime );

    // Make sure there is a valid reference date for the start date/time...

    DateTime ReferenceDate_DateTime2 = null;
    if ( ReferenceDate_DateTime == null ) {
        // Create a reference date for Jan 1 that is of the correct precision...
        // If in discovery mode the dates may not be set in the time series so use a default to pass remaining logic
        if ( (ts == null) || (ts.getDate1() == null) ) {
            ReferenceDate_DateTime2 = new DateTime();
        }
        else {
            ReferenceDate_DateTime2 = TSUtil.newPrecisionDateTime ( ts, null );
            // The year will be set to each year in the source time series, or that of the reference date.
            // Now reset to Jan 1...
            ReferenceDate_DateTime2.setMonth(1);
            ReferenceDate_DateTime2.setDay(1);
            // TODO SAM 2007-12-13 Evaluate how the following works for INST, MEAN, ACCM data.
            ReferenceDate_DateTime2.setHour(0);
            ReferenceDate_DateTime2.setMinute(0);
        }
    }
    else {
        // Make sure the reference date is of the right precision...
        ReferenceDate_DateTime2 = TSUtil.newPrecisionDateTime ( ts, ReferenceDate_DateTime);
    }
    boolean transferByDateTime = false;  // To simplify logic below, currently only allowed if time interval is day
    if ( transferDataHowType == TransferDataHowType.BY_DATETIME ) {
        if ( ts.getDataIntervalBase() == TimeInterval.DAY ) {
            // Need logic if <= daily time series
            transferByDateTime = true;
        }
        else if ( ts.getDataIntervalBase() <= TimeInterval.DAY ) {
            // Don't handle yet because need to decrement full day of intervals via previous(),
            // which may involve more than one value - only handle day for now, which involves one call to previous()
            throw new UnsupportedTimeIntervalException("Creating traces using transfer" +
            TransferDataHowType.BY_DATETIME + " only supported for intervals >= Day.");
        }
        // Else month, year, etc are OK to treat as sequential in all cases because no discontinuity due to leap year
    }
    //if ( Message.isDebugOn ) {
        Message.printStatus ( 2, routine, "All traces will start on reference date " + ReferenceDate_DateTime2 );
    //}

    // Allocate the list for traces...

    List<TS> tslist = new ArrayList<>();
    
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
        date1_in.setYear ( InputStart_DateTime.getYear() + itrace );
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
                "output", ts, ReferenceDate_DateTime2, date1_out, TraceLength_TimeInterval );
        // Skip the year if the start and end dates in the source data are outside the requested period,
        // and break out of processing when the trace year is after the period to be processed.
        // The requested period is set to the full time series period if not specified.
        if ( date2_in.lessThan(InputStart_DateTime) ) {
            // Trace period is before the requested period so skip the trace.
            Message.printStatus(2, routine, "Requested InputStart=" + InputStart_DateTime +
                " InputEnd=" + InputEnd_DateTime + ", skipping trace date1_in=" + date1_in + " date2_in=" + date2_in );
            continue;
        }
        if ( date1_in.greaterThan(InputEnd_DateTime) ) {
            // Trace period is past the requested period so no more traces need to be generated.
            Message.printStatus(2, routine, "Requested InputStart=" + InputStart_DateTime +
                " InputEnd=" + InputEnd_DateTime + ", quit processing trace date1_in=" + date1_in + " date2_in=" + date2_in );
            break;
        }
        // Create a new time series using the old header as input...
        TS tracets = TSUtil.newTimeSeries ( ts.getIdentifierString(), true);
        tracets.copyHeader ( ts );
        if ( transferByDateTime ) {
        	tracets.addToGenesis("Data values from input are transferred ensuring date/time is retained (other than year shift).");
        	tracets.addToGenesis("- Leap year Feb 29 in trace controls.  Input time series values may be discarded or may be missing.");
        	if ( date1_out.isLeapYear() )  {
          		tracets.addToGenesis("- Trace start date/time year " + date1_out.getYear() + " IS a leap year and will have Feb 29 data.");
        	}
        	else {
          		tracets.addToGenesis("- Trace start date/time year " + date1_out.getYear() + " IS NOT a leap year and will not have Feb 29 data.");
        	}
        	if ( outputYearType != YearType.CALENDAR ) {
        		if ( TimeUtil.isLeapYear(date1_out.getYear() + 1) )  {
        			tracets.addToGenesis("- Year type is " + outputYearType + " and year after initial trace year ("
        				+ (date1_out.getYear() + 1) + " IS a leap year.");
        		}
        		else {
        			tracets.addToGenesis("- Year type is " + outputYearType + " and year after initial trace year ("
        				+ (date1_out.getYear() + 1) + " IS NOT a leap year.");
        		}
        	}
        }
        else {
        	tracets.addToGenesis("Data values from input are transferred sequentially.");
        	tracets.addToGenesis("- Leap year Feb 29 in trace controls." );
        	tracets.addToGenesis("- Input time series values date/times may shift due to sequential processing over Feb 29/March 1.");
        }
        int seqNum = date1_in.getYear();
        if ( outputYearType != null ) {
            // Adjust the sequence number by the first year offset
            // Use negative because the adjustment is from the year type to calendar year
            seqNum -= outputYearType.getStartYearOffset();
        }
        tracets.setSequenceID ( "" + seqNum );
        tracets.setDescription ( tracets.formatLegend(descriptionFormat) );
        // Set alias after other information is set
        tracets.setAlias( tracets.formatLegend(alias) );
        tracets.addToGenesis ( "Split trace out of time series for input: " + date1_in + " to " + date2_in +
                ", output: " + date1_out + " to " + date2_out );
        tracets.setDate1 ( date1_out );
        tracets.setDate2 ( date2_out );
        
        //if ( Message.isDebugOn ) {
            Message.printStatus ( 2, "",
            "Created new trace for year " + date1_in + " allocated for input: " + date1_in + " to " + date2_in +
            ", output:" + date1_out + " to " + date2_out );
        //}
        if ( createData ) {
            // 
            // Allocate the data space...
            tracets.allocateDataSpace();
            // Transfer the data using iterators so that the data sequence is continuous over leap years, etc.
            TSIterator tsi_in = ts.iterator ( date1_in, date2_in );
            TSIterator tsi_out = tracets.iterator ( date1_out, date2_out );
            TSData data_out = null;
            int dayIn, dayTrace, monthIn, monthTrace;
            while ( tsi_in.next() != null ) { // Advance the input
                // Get the next corresponding point in the output.
                data_out = tsi_out.next();
            	if ( transferByDateTime ) {
            		// If here, only day is supported (checked above)
            		// Need to see if the days do not align due to leap year.
            		// If this is the case, adjust the input date forward or back by one day.
            		// This check is complicated because may be using other than calendar year.
            		// The input time series date/time 'dtIn' will be advancing continuously.
            		// The output time series date/time 'dtTrace' will be advancing continuously (other than resets to the start).
            		//
            		// Case 1: if the output time series position is Feb 29 when the input time series position is March 1
            		// - set the output to missing
            		// - decrement the input time series iterator to the previous day so that 'next()'
            		//   will result in input time series being positioned at March 1
            		//   (adjustment only needs to be made once every 4 years)
            		//
            		// Case 2: if the output time series position is Mar 1 when the input time series position is Feb 29:
            		// - skip setting the value in the output time series since did not have a Feb 29
            		// - decrement the output time series iterator to the previous day so that 'next()'
            		//   will result in input time series being positioned at March 1
            		//   (adjustment only needs to be made once every 4 years)
            		dayIn = tsi_in.getDate().getDay();
            		monthIn = tsi_in.getDate().getMonth();
            		dayTrace = tsi_out.getDate().getDay();
            		monthTrace = tsi_out.getDate().getMonth();
            		if ( (monthIn == 3) && (dayIn == 1) && (monthTrace == 2) && (dayTrace == 29) ) {
            			// Case 1 - trace has leap year Feb 29 but input does not
            			Message.printStatus(2, routine, "Case 1 - setting trace to missing on Feb 29 for input year " + tsi_in.getDate().getYear());
            			if ( data_out != null ) {
            				// Set the trace to missing since input did not have
                        	tracets.setDataValue( tsi_out.getDate(), ts.getMissing());
                    	}
            			// Reset input iterator back one day so that next() called in next loop iteration will go to March 1, same as next() for output
            			Message.printStatus(2, routine, "  - input time series iterator before calling previous() is: " + tsi_in.getDate() );
            			tsi_in.previous();
            			Message.printStatus(2, routine, "  - input time series iterator after calling previous() is: " + tsi_in.getDate() );
            			// Don't want to set value as per sequential so jump to top of loop.
            			continue;
            		}
            		else if ( (monthIn == 2) && (dayIn == 29) && (monthTrace == 3) && (dayTrace == 1) ) {
            			// Case 2 - input has leap year Feb 29 but trace does not
            			// - don't set output value
            			// - decrement output iterator so that next call will process March 1 again
            			Message.printStatus(2, routine, "Case 2 - ignoring input Feb 29 for input year " + tsi_in.getDate().getYear());
            			tsi_out.previous();
            			// Don't want to set value as per sequential so jump to top of loop.
            			continue;
            		}
            		else {
            			// Not dealing with leap year issues, logic is similar to sequential and is handled below.
            		}
            	}
            	// If here, data are being transferred sequentially or transferring by date/time and no leap year issue for this data value.
            	// This is the simpler method because the iterator just move forward
            	// Only transfer if output is not null because null indicates the end of the
            	// output time series iterator period has been reached.
            	if ( data_out != null ) {
                    tracets.setDataValue( tsi_out.getDate(), ts.getDataValue(tsi_in.getDate()));
                }
            }
        }
        // Add the trace to the list of time series...
        tslist.add ( tracets );
    }
    return tslist;
}

}
