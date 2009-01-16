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
@param startDate Date to start assignment.
@param endDate Date to stop assignment.
@param patternValues Data values to set as time series data.
@param patternFlags String flag values to set as data, or null to not use flags.
*/
public void setDataValuesUsingPattern (	TS ts, DateTime startDate,
					DateTime endDate, double [] patternValues, String [] patternFlags )
throws Exception
{	// Get valid dates because the ones passed in may have been null...
	TSLimits valid_dates = TSUtil.getValidPeriod ( ts, startDate, endDate );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	TSIterator tsi = ts.iterator ( startDate, endDate );
	int ipattern = 0;
	int iflag = 0;
	DateTime date;
	boolean usingDataFlags = ts.hasDataFlags();
	while ( tsi.next() != null ) {
		// The first call will set the pointer to the first data value in the period.
	    // next() will return null when the last date in the processing period has been passed.
		date = tsi.getDate();
		if ( usingDataFlags ) {
		    ts.setDataValue ( date, patternValues[ipattern++], patternFlags[iflag++], 0 );
		}
		else {
		    ts.setDataValue ( date, patternValues[ipattern++] );
	    }
		if ( ipattern == patternValues.length ) {
			// Reset to start at the beginning of the pattern...
			ipattern = 0;
		}
        if ( usingDataFlags && (iflag == patternFlags.length) ) {
            // Reset to start at the beginning of the flags...
           iflag = 0;
        }
	}
	// Set the genesis information...
	ts.setDescription ( ts.getDescription() + ", pattern" );
	StringBuffer patternbuf = new StringBuffer ();
	for ( int i = 0; i < patternValues.length; i++ ) {
		if ( i != 0 ) {
			patternbuf.append ( ",");
		}
		patternbuf.append ( StringUtil.formatString(patternValues[i],"%.3f") );
	}
    StringBuffer flagbuf = new StringBuffer ();
    if ( usingDataFlags ) {
        for ( int i = 0; i < patternFlags.length; i++ ) {
            if ( i != 0 ) {
                flagbuf.append ( ",");
            }
            flagbuf.append ( patternFlags[i] );
        }
        if ( flagbuf.length() > 0 ) {
            flagbuf.insert(0, ", flags=" );
        }
    }
	ts.addToGenesis ( "Set " + start.toString() + " to " +
	end.toString() + " to pattern=" + patternbuf.toString() + flagbuf.toString());

}

}