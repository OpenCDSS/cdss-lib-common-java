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
series.  The results include the percentage of time that a data value is
exceeded.
*/
public class TSDurationAnalysis
{

// Data members...

private	TS	_ts = null;		// Time series to analyze.
private double[] _percents = null;	// Percent of time that values are
					// exceeded.
private double[] _values = null;	// Data values

// Member functions...

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
	try {	values0 = TSUtil.toArray (	_ts,
						_ts.getDate1(), _ts.getDate2());
	}
	catch ( Exception e ) {
		message = "Error converting time series " +
		_ts.getIdentifier() + " to array.";
		_values = null;
		values0 = null;
		Message.printWarning ( 2, "TSDurationAnalysis.analyze",message);
		throw new TSException ( message );
	}

	if ( values0 == null ) {
		message = "Error converting time series " +
		_ts.getIdentifier() + " to array.";
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
	else {	// Transfer only the non-missing values...
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

	if ( MathUtil.sort ( _values, MathUtil.SORT_QUICK,
			MathUtil.SORT_DESCENDING, null, false ) != 0 ) {
		_values = null;
		message = "Error sorting time series data " +
		_ts.getIdentifier();
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
Return the percents (0 to 100) or null if data have not been successfully
analyzed.
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
@param ts Time series to analyize.
*/
private void initialize ( TS ts )
{	_ts = ts;
	_values = null;
	_percents = null;
}

} // End of TSDurationAnalysis
