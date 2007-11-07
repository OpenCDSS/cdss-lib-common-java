package RTi.TS;

import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
Set values in a time series using a pattern.
*/
public class TSUtil_SetDataValuesUsingPattern
{

/**
Constructor.
*/
public TSUtil_SetDataValuesUsingPattern ()
{
	// Does nothing.
}

/**
Set the time series data to a repeating sequence of values.
@param ts Time series to update.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param pattern_values Data values to set as time series data.
*/
public void setDataValuesUsingPattern (	TS ts, DateTime start_date,
					DateTime end_date, double [] pattern_values )
throws Exception
{	// Get valid dates because the ones passed in may have been null...
	TSLimits valid_dates = TSUtil.getValidPeriod ( ts, start_date, end_date );
	DateTime start	= valid_dates.getDate1();
	DateTime end	= valid_dates.getDate2();

	TSIterator tsi = ts.iterator ( start_date, end_date );
	int ipattern = 0;
	DateTime date;
	while ( tsi.next() != null ) {
		// The first call will set the pointer to the
		// first data value in the period.  next() will return
		// null when the last date in the processing period
		// has been passed.
		date = tsi.getDate();
		ts.setDataValue ( date, pattern_values[ipattern++] );
		if ( ipattern == pattern_values.length ) {
			// Reset to start at the beginning of the pattern...
			ipattern = 0;
		}
	}
	// Set the genesis information...
	ts.setDescription ( ts.getDescription() + ", pattern" );
	StringBuffer patternbuf = new StringBuffer ();
	for ( int i = 0; i < pattern_values.length; i++ ) {
		if ( i != 0 ) {
			patternbuf.append ( ",");
		}
		patternbuf.append ( StringUtil.formatString(pattern_values[i],"%.3f") + "." );
	}
	ts.addToGenesis ( "Set " + start.toString() + " to " +
	end.toString() + " to pattern " + patternbuf.toString() );

}

}
