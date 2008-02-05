package RTi.TS;

import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
Compute an error time series from an "observed" and "simulated" time series.
*/
public class TSUtil_ComputeErrorTimeSeries
{

/**
Constructor.
*/
public TSUtil_ComputeErrorTimeSeries ()
{
	// Does nothing.
}

/**
Create an error time series that has error from comparing the simulated to observed time
series.  The time series must have the same interval and irregular time series are not supported.
@param observed_ts Observed time series.
@param simulated_ts Simulated time series.
@param error_measure The error measure to assign in output.  Currently only "PercentError" is
supported.
@exception IrregularTimeSeriesNotSupportedException if the method is called with an
irregular time series.
*/
public TS computeErrorTimeSeries (  TS observed_ts, TS simulated_ts, String error_measure )
throws IrregularTimeSeriesNotSupportedException, Exception
{   String routine = getClass().getName() + ".computeErrorTimeSeries";
    String message;
    int wl = 3; // Warning level
    if ( observed_ts == null ) {
        message = "The observed time series is null.";
        Message.printWarning( wl, routine, message);
        throw new RuntimeException ( message );
    }
    if ( simulated_ts == null ) {
        message = "The simulated time series is null.";
        Message.printWarning( wl, routine, message);
        throw new RuntimeException ( message );
    }

    // Do not handle IrregularTS.

    if ( observed_ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
        message = "Can't handle irregular TS \"" + observed_ts.getIdentifier() + "\".";
        Message.printWarning ( wl, routine, message );
        throw new IrregularTimeSeriesNotSupportedException ( message );
    }
    if ( simulated_ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
        message = "Can't handle irregular TS \"" + simulated_ts.getIdentifier() + "\".";
        Message.printWarning ( wl, routine, message );
        throw new IrregularTimeSeriesNotSupportedException ( message );
    }
    
    // Time series must have the same interval. Put in a list to get the period also.

    Vector tslist = new Vector(2);
    tslist.add ( observed_ts );
    tslist.add ( simulated_ts );
    if ( !TSUtil.intervalsMatch(tslist) ) {
        message = "The time series intervals do not match for " + observed_ts.getIdentifier() + " and " +
        simulated_ts.getIdentifier() + ".";
        Message.printWarning( wl, routine, message);
        throw new RuntimeException ( message );
    }
    
    // Only PercentError is supported
    
    if ( !error_measure.equalsIgnoreCase("PercentError") ) {
        message = "The error measure (" + error_measure + ") must be PercentError.";
        Message.printWarning ( wl, routine, message );
        throw new RuntimeException ( message );
    }
    
    // Determine the overall period to be processed...

    TSLimits valid_dates = TSUtil.getPeriodFromTS ( tslist, TSUtil.MAX_POR );
    // Reset the reference to the input period...
    DateTime date1 = new DateTime(valid_dates.getDate1());
    DateTime date2 = new DateTime(valid_dates.getDate2());

    // Create a new time series using the simulation header as input...
    TS error_ts = TSUtil.newTimeSeries ( simulated_ts.getIdentifierString(), true);
    error_ts.copyHeader ( simulated_ts );
    // TODO SAM 2008-02-05 Set data type here if different error measures are enabled.
    error_ts.setDataUnits ( "% Error" );
    error_ts.setDataUnitsOriginal ( "% Error" );
    //tracets.setAlias( ts.getLocation() + "_" + tracets.getSequenceNumber() );
    error_ts.setDescription ( simulated_ts.getDescription() + ", error" );
    error_ts.addToGenesis ( "Created error time series using for period " + date1 + " to " + date2 +
            " using simulated \"" + simulated_ts.getIdentifier() + "\" and observed \"" + observed_ts + "\"" );
    error_ts.setDate1 ( date1 );
    error_ts.setDate2 ( date2 );
    error_ts.setDate1Original ( date1 );
    error_ts.setDate2Original ( date2 );
    
    // Allocate the data space...
    error_ts.allocateDataSpace();
    // Transfer the data using an iterator.
    TSIterator tsi_error = error_ts.iterator ( date1, date2 );
    TSIterator tsi_observed = observed_ts.iterator ( date1, date2 );
    TSIterator tsi_simulated = simulated_ts.iterator ( date1, date2 );
    TSData data_error = null;
    TSData data_observed = null;
    TSData data_simulated = null;
    double value_simulated;
    double value_observed;
    double value_error;
    while ( true ) {
        // Get the points from the input time series...
        data_error = tsi_error.next();
        if ( data_error == null ) {
            break;
        }
        data_observed = tsi_observed.next();
        data_simulated = tsi_simulated.next();
        if ( (data_observed == null) || (data_simulated == null) ) {
            // Cannot compute error so leave it missing in the result...
            continue;
        }
        // Only transfer if output is not null because null indicates the end of the
        // output time series iterator period has been reached.
        value_simulated = data_simulated.getData();
        value_observed = data_observed.getData();
        if ( observed_ts.isDataMissing(value_observed) || value_observed == 0.0 ) {
            // Unable to compute error so leave missing in the result.
            continue;
        }
        if ( simulated_ts.isDataMissing(value_simulated) ) {
            // Unable to compute error so leave missing in the result.
            continue;
        }
        // TODO SAM 2008-02-04 This is where other error measures would be computed.
        // For now it is always percent error.
        value_error = 100.0*(value_simulated - value_observed)/value_observed;
        error_ts.setDataValue( tsi_error.getDate(), value_error );
    }
    return error_ts;
}

}
