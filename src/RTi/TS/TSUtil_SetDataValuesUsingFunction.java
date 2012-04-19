package RTi.TS;

import RTi.Util.Time.DateTime;

/**
Set values in a time series using a a function.
*/
public class TSUtil_SetDataValuesUsingFunction
{

/**
Time series being processed.
*/
private TS __ts = null;

/**
Time series function being used to set the data.
*/
private TSFunctionType __function = null;

/**
Constructor.
*/
public TSUtil_SetDataValuesUsingFunction ( TS ts, TSFunctionType function )
{
	__ts = ts;
	__function = function;
}

/**
Calculate the function value.
*/
private double getFunctionValue ( TS ts, DateTime dt, TSFunctionType function )
{
    double value = ts.getMissing();
    switch ( function ) {
        case DATE_YYYY:
            value = dt.getYear();
            break;
        case DATE_YYYYMM:
            value = dt.getYear()*100 + dt.getMonth();
            break;
        case DATE_YYYYMMDD:
            value = dt.getYear()*10000.0 + dt.getMonth()*100.0 + dt.getDay();
            break;
        case DATETIME_YYYYMMDD_HH:
            value = dt.getYear()*10000.0 + dt.getMonth()*100.0 + dt.getDay() + dt.getHour()/100.0;
            break;
        case DATETIME_YYYYMMDD_HHMM:
            value = dt.getYear()*10000.0 + dt.getMonth()*100.0 + dt.getDay() +
                dt.getHour()/100.0 + dt.getMinute()/10000.0;
            break;
    }
    return value;
}

/**
Set the time series data to a repeating sequence of values.
*/
public void setDataValuesUsingFunction ()
throws Exception
{	// Get valid dates because the ones passed in may have been null...
    // TODO SAM 2012-04-18 Enable period later
	//TSLimits valid_dates = TSUtil.getValidPeriod ( ts, startDate, endDate );
    TS ts = __ts;
    TSFunctionType function = __function;
	DateTime start = ts.getDate1(); //valid_dates.getDate1();
	DateTime end = ts.getDate2(); //valid_dates.getDate2();
	
	TSIterator tsi = ts.iterator ( start, end );
	DateTime date;
	while ( tsi.next() != null ) {
		// The first call will set the pointer to the first data value in the period.
	    // next() will return null when the last date in the processing period has been passed.
		date = tsi.getDate();
		ts.setDataValue ( date, getFunctionValue(ts,date,function) );
	}
	// TODO SAM 2012-04-18 Evaluate whether should append to description
	// Set the genesis information...
	//ts.setDescription ( ts.getDescription() + "," + function );
	ts.addToGenesis ( "Set " + start + " to " + end + " to function=" + function );
}

}