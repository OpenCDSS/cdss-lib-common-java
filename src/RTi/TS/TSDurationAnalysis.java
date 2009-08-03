// ----------------------------------------------------------------------------
// TSDurationAnalysis - analyze time series and produce duration data (the
//			percent of time a value is exceeded).
// ----------------------------------------------------------------------------
// History:
//
// 30 Oct 2000	Steven A. Malers, RTi	Initial version to support TSTool.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// ----------------------------------------------------------------------------

package RTi.TS;

import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;

/**
The TSDurationAnalysis class performs a duration analysis on a single time
series.  The results include the percentage of time that a data value is exceeded.
*/
public class TSDurationAnalysis
{

// Data members...

/**
Time series to analyze.
*/
private	TS _ts = null;

/**
Percent of time that values are exceeded.
*/
private double[] _percents = null;

/**
Data values.
*/
private double[] _values = null;

/**
Perform a duration analysis using the specified time series.
@param ts The time series supplying values.
@exception TSException if an error occurs.
*/
public TSDurationAnalysis ( TS ts )
throws TSException
{	initialize ( ts );
	analyze ();
}

/**
Perform a duration analysis using the specified time series.
@param ts The time series supplying values.
@param analyze indicate whether analysis should occur.
@exception TSException if an error occurs.
*/
private TSDurationAnalysis ( TS ts, boolean analyze )
throws TSException
{   initialize ( ts );
    if ( analyze ) {
        analyze ();
    }
}

/**
Analyze the time series and produce the duration data.
@exception TSException if there is a problem performing the analysis.
*/
private void analyze ()
throws TSException
{	String message = "";

	if ( _ts == null ) {
		message = "Null time series for analysis";
		Message.printWarning ( 2, "TSDurationAnalysis.analyze",message);
		throw new TSException ( message );
	}

	// Get the data as an array...

	double [] values0 = null;
	try {
	    values0 = TSUtil.toArray ( _ts, _ts.getDate1(), _ts.getDate2());
	}
	catch ( Exception e ) {
		message = "Error converting time series " + _ts.getIdentifier() + " to array.";
		_values = null;
		values0 = null;
		Message.printWarning ( 2, "TSDurationAnalysis.analyze",message);
		throw new TSException ( message );
	}

	if ( values0 == null ) {
		message = "Error converting time series " + _ts.getIdentifier() + " to array.";
		_values = null;
		Message.printWarning ( 2, "TSDurationAnalysis.analyze",message);
		throw new TSException ( message );
	}

	// Count the missing values...

	int size = values0.length;
	int nmissing = 0;
	for ( int i = 0; i < size; i++ ) {
		if ( _ts.isDataMissing(values0[i]) ) {
			++nmissing;
		}
	}

	// Now resize the array by throwing out missing values...

	int newsize = size;
	if ( nmissing == 0 ) {
		// Just use what came back...
		_values = values0;
	}
	else {
	    // Transfer only the non-missing values...
		newsize = size - nmissing;
		_values = new double[newsize];
		int j = 0;
		for ( int i = 0; i < size; i++ ) {
			if ( !_ts.isDataMissing(values0[i]) ) {
				_values[j++] = values0[i];
			}
		}
		// Don't need anymore...
		values0 = null;
	}

	// Sort into descending order.  Duplicates are OK...

	if ( MathUtil.sort ( _values, MathUtil.SORT_QUICK, MathUtil.SORT_DESCENDING, null, false ) != 0 ) {
		_values = null;
		message = "Error sorting time series data " + _ts.getIdentifier();
		_values = null;
		Message.printWarning ( 2, "TSDurationAnalysis.analyze",message);
		throw new TSException ( message );
	}

	// Now assign the percentages...

	_percents = new double[newsize];
	for ( int i = 0; i < newsize; i++ ) {
		// Simple plotting positions...
		_percents[i] = (((double)(i) + 1.0)/(double)(newsize))*100.0;
	}
	message = null;
	values0 = null;
	
	// Quick test for the filtered analysis
	/*
	Message.printStatus(2, "", "Testing filtering..." );
	TSDurationAnalysis da = filterResultsUsingValueDifference ( (_ts.getDataLimits().getMaxValue() - _ts.getDataLimits().getMinValue())/100.0 );
	double [] percents = da.getPercents();
	double [] values = da.getValues();
	for ( int i = 0; i < percents.length; i++ ) {
	    Message.printStatus(2, "", "Filtered percent = " + percents[i] + " value=" + values[i]);
	}
	setPercents ( percents );
	setValues ( values );
	*/
}

/**
Get the duration analysis filtered by throwing out intermediate points where the difference between two
points has a data value difference that is less than the specified amount.  This is useful, for example,
to improve visualization tools where fewer points are appropriate.  The returned object is a clone of the
original analysis but with fewer points.  For analysis with many points, this typically reduced the number
of points in the flat parts of the curve.
@param requiredValueDiff required difference between data points' data values (e.g., take max - min/200).
@return a TSDurationAnalysis object that has been filtered to reduce the number of points
*/
public TSDurationAnalysis filterResultsUsingValueDifference ( double requiredValueDiff )
throws TSException
{
    // If the original arrays are null, the analysis may not have been performed, so do it
    double [] percents = getPercents();
    double [] values = getValues();
    if ( percents == null ) {
        analyze();
    }
    // Initially use an array size that is the same as the current results - change at end
    // Use primitives and not objects to improve performance
    double [] filteredPercents = new double[percents.length];
    double [] filteredValues = new double[values.length];
    int filteredCount = 0; // Count of retained points
    // Loop through and evaluate the percents...
    // Always add the first and last points
    filteredPercents[filteredCount] = percents[0];
    filteredValues[filteredCount++] = values[0];
    // TODO SAM 2009-08-03 Evaluate whether to also always add intermediate points (like exactly 10%), but if
    // all we wanted were even percents we'd probably add a different method
    int iend = percents.length - 2;
    double diff;
    // Since first point has been added, examine the 2nd compared with the 1st, etc.
    int iVal = 0; // Location of last point that has been saved
    int iSearch = iVal + 1; // Location of point being checked
    while ( (iVal < iend) && (iSearch < iend)) {
        // Increment the index of the value being checked...
        iSearch = iVal + 1;
        while ( iSearch < iend ) {
            // Handle typical direction of change and negatives...
            diff =  values[iVal] - values[iSearch];
            if ( diff < 0 ) {
                diff += -1.0;
            }
            if ( diff >= requiredValueDiff ) {
                // The difference in values is >= the required so add the point
                // and reset the current value to the last saved value
                //Message.printStatus(2, "", "Found point to keep at " + iSearch + " iVal=" + iVal + " diff=" + diff);
                filteredPercents[filteredCount] = percents[iSearch];
                filteredValues[filteredCount++] = values[iSearch];
                iVal = iSearch;
                break; // To go to external loop to restart the search for the next point
            }
            else {
                // Else the point is not acceptable so advance the search
                //Message.printStatus(2, "", "Skipping value at " + iSearch + " iVal=" + iVal + " diff=" + diff);
                ++iSearch;
            }
        }
    }
    Message.printStatus(2, "", "Filtered duration reduced from " + percents.length +
        " points to " + filteredCount );
    // Always add the last point
    filteredPercents[filteredCount] = percents[percents.length - 1];
    filteredValues[filteredCount++] = values[values.length - 1];
    // Resize the arrays to final size and return the new TSDurationAnalysis object
    double [] filteredValuesSized = new double[filteredCount];
    double [] filteredPercentsSized = new double[filteredCount];
    System.arraycopy(filteredValues, 0, filteredValuesSized, 0, filteredCount);
    System.arraycopy(filteredPercents, 0, filteredPercentsSized, 0, filteredCount);
    TSDurationAnalysis filteredDA = new TSDurationAnalysis ( this.getTS(), false );
    filteredDA.setValues ( filteredValuesSized );
    filteredDA.setPercents ( filteredPercentsSized );
    return filteredDA;
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize ()
throws Throwable
{	_ts = null;
	_values = null;
	_percents = null;
	super.finalize();
}

/**
Return the time series that was analyzed.
@return the time series that was analyzed.
*/
public TS getTS()
{   return _ts;
}

/**
Return the percents (0 to 100) or null if data have not been successfully analyzed.
@return the percents array.
*/
public double [] getPercents()
{	return _percents;
}

/**
Return the values or null if data have not been successfully analyzed.
@return data values array.
*/
public double [] getValues()
{	return _values;
}

/**
Initialize the object.
@param ts Time series to analyze.
*/
private void initialize ( TS ts )
{	_ts = ts;
	_values = null;
	_percents = null;
}

/**
Set the array of percents.
@param percents array of percents to set.
*/
private void setPercents ( double [] percents )
{
    _percents = percents;
}

/**
Set the array of values.
@param values array of values to set.
*/
private void setValues ( double [] values )
{
    _values = values;
}

}