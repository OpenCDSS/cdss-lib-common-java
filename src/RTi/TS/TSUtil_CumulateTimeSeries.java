package RTi.TS;

import java.util.Vector;

import RTi.Util.IO.PropList;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
Cumulate values in a time series.
*/
public class TSUtil_CumulateTimeSeries
{

/**
Constructor.
*/
public TSUtil_CumulateTimeSeries ()
{
	// Does nothing.
}

/**
Add each value over time to create a cumulative value.
@param ts Time series to process.
@param start_date Date to start cumulating.
@param end_date Date to stop cumulating.
@param props Parameters to control processing:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>    <td><b>Description</b></td> <td><b>Default</b></td>
</tr>

<tr>
<td><b>HandleMissingHow</b></td>
<td>
If CarryForwardIfMissing, a missing value will be set to the
previous cumulative value (carry forward).  If SetMissingIfMissing,
the result will be set to missing.  Subsequent non-missing data will in any
case increment the last non-missing total.
</td>
<td>SetMissingIfMissing
</td>
</tr>

<tr>
<td><b>Reset</b></td>
<td>
Indicate when to reset the cumulative value to zero.  Specify as a recognized date/time format.
Set the year to zero (e.g., 0000-MM-DD) to reset every year.
</td>
<td>SetMissingIfMissing
</td>
</tr>

</table>
*/
public void cumulate ( TS ts, DateTime start_date, DateTime end_date, PropList props )
throws Exception
{   double  oldvalue;

    // Get valid dates because the ones passed in may have been null...

    TSLimits valid_dates = TSUtil.getValidPeriod ( ts, start_date, end_date );
    DateTime start  = valid_dates.getDate1();
    DateTime end    = valid_dates.getDate2();

    int interval_base = ts.getDataIntervalBase();
    int interval_mult = ts.getDataIntervalMult();

    if ( props == null ) {
        props = new PropList ( "cumulate" );
    }
    String HandleMissingHow = props.getValue ( "HandleMissingHow" );
    boolean HandleMissingHow_CarryForward_boolean = false;  // Set to missing if missing (do not carry forward).
    if ( (HandleMissingHow != null) && HandleMissingHow.equalsIgnoreCase("CarryForwardIfMissing") ) {
        HandleMissingHow_CarryForward_boolean = true;
    }
    String Reset = props.getValue ( "Reset" );
    DateTime Reset_DateTime = null;     // Reset date/time - will be modified during processing below
    DateTime Reset_DateTime0 = null;    // Copy of original reset
    if ( (Reset != null) && !Reset.equals("") ) {
        Reset_DateTime = DateTime.parse( Reset );
        Reset_DateTime0 = new DateTime ( Reset_DateTime );
        if ( interval_base == TimeInterval.IRREGULAR ) {
            String message = "Using Reset to cumulate is not supported for irregular time series.";
            throw new IrregularTimeSeriesNotSupportedException ( message );
        }
    }
    double total = ts.getMissing();
    boolean is_missing = false;
    if ( interval_base == TimeInterval.IRREGULAR ) {
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
                // Past the end of where we want to go so quit...
                break;
            }
            if ( date.greaterThanOrEqualTo(start) ) {
                oldvalue = tsdata.getData();
                is_missing = ts.isDataMissing(oldvalue);
                if ( !is_missing ) {
                    // Not missing.  Add to total and set value...
                    if ( ts.isDataMissing(total) ) {
                        total = oldvalue;
                    }
                    else {
                        total += oldvalue;
                    }
                    tsdata.setData(total);
                    // Have to do this manually since TSData are being modified directly to improve performance...
                    ts.setDirty ( true );
                }
                else if ( HandleMissingHow_CarryForward_boolean ) {
                    // Missing but want to carry forward previous total...
                    tsdata.setData(total);
                    // Have to do this manually since TSData are being modified directly to improve performance...
                    ts.setDirty ( true );
                }
                // Else, missing and don't want to carry forward so leave as is.
            }
        }
    }
    else {
        // Loop using addInterval...
        for ( DateTime date = new DateTime ( start ); date.lessThanOrEqualTo( end );
            date.addInterval(interval_base, interval_mult) ) {
            oldvalue = ts.getDataValue(date);
            is_missing = ts.isDataMissing(oldvalue);
            if ( !is_missing ) {
                // Not missing.  Add to total and set value...
                if ( ts.isDataMissing(total) ) {
                    total = oldvalue;
                }
                else {
                    total += oldvalue;
                }
                ts.setDataValue(date,total);
            }
            else if ( HandleMissingHow_CarryForward_boolean ) {
                // Missing but want to carry forward previous total...
                ts.setDataValue(date,total);
            }
            // Else, missing and don't want to carry forward so leave as is.
            // Check to determine whether the cumulative value needs to be reset to zero.  Note that this
            // will throw away the previous value that was at this point.
            if ( Reset_DateTime != null ) {
                // First set the year if the original one was zero.
                if ( Reset_DateTime0.getYear() == 0 ) {
                    Reset_DateTime.setYear(date.getYear());
                }
                if ( Reset_DateTime.equals(date) ) {
                    total = 0.0;
                    ts.setDataValue(date,total);
                }
            }
        }
    }

    // Fill in the genesis information...

    ts.addToGenesis ( "Cumulated " + start + " to " + end + "." );
    ts.setDescription ( ts.getDescription() + ", cumulative" );
}

}
