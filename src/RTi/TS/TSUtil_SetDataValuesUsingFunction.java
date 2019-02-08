// TSUtil_SetDataValuesUsingFunction - set values in a time series using a a function.

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
        case RANDOM_0_1:
            value = Math.random();
            break;
        case RANDOM_0_1000:
            value = Math.random()*1000.0;
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
