package RTi.GRTS;

import java.awt.Point;

import RTi.Util.GUI.JWorksheet;

import java.util.ArrayList;
import java.util.List;

import RTi.TS.IrregularTS;
import RTi.TS.TS;
import RTi.TS.TSData;
import RTi.TS.TSException;
import RTi.TS.TSIterator;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;
import RTi.TS.UnequalTimeIntervalException;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
This class is a table model for displaying regular TS data.  It is more 
complicated than most table models, primarily in order to achieve performance
gains with getting values out of a time series.  The following is a brief 
description of some of the performance gain efforts made in this class.  
These caching methods are both found in getValueAt().<p>
<b>Date Caching</b><p>
At creation time, dates throughout the range of the time series are cached
into an internal array.  Then, when a row of data needs to be drawn in the
worksheet, the date nearest the row to be drawn is used instead of calculating
the date from the first row.<p>
This is because adding many intervals at once to a date/time is an expensive
operation.  Using a day time series as an example, adding X days to a date/time
takes X times as long as adding 1 day.  Thus, by caching dates along the 
entire span of the time series, it can be ensured that __cacheInterval will be
the greatest number of intervals ever added to a single date/time at once.<p>
<b>Caching of the Top-Most Visible Row Number After Each Scroll Event</b><p>
Caching is also done of the top-most visible row every time the worksheet is
scrolled, and involves the __firstVisibleRowDate and __previousTopmostVisibleY
member variables.  Each time getValueAt() is called, it checks to see if the 
top Y value of the worksheet is different from it was when getValueAt() was
last called.  If so, then the worksheet has been scrolled.  Each time the 
worksheet is scrolled, the date/time of the top-most visible row is calculated
using date caching.  Then, the date of each row that is drawn for the 
current scroll position is calculated from the date of the top-most visible row.<p>
<b>Notes</b>
These caching steps may seem overkill, but JTS found during extensive testing
that they increase the speed of browsing through a table of time series 
dramatically.  Without the caching, scrolling to the end of a long time series
can take many seconds, whereas it is nearly instant with the steps taken here.
*/
public class TSViewTable_TableModel <T> extends JWorksheet_AbstractRowTableModel
{

/**
Whether to use the TS extended legend as the TS's column title.  If false,
the TS normal legend will be used.
*/
private boolean __useExtendedLegend = false;

/**
An array of DateTime values that are pre-calculated in order to speed up 
calculation of DateTimes in the middle of the dataset.  Each element in this
array contains the DateTime for the row at N*(__cacheInterval)
*/
private DateTime[] __cachedDates;

/**
The date/time of the first row visible on the JWorksheet.
*/
private DateTime __firstVisibleRowDate;

/**
The prior date time from which data was read in a consecutive read.
*/
private DateTime __priorDateTime = null;

/**
The first date/time in the table model.
*/
private DateTime __start;

/**
The working date time from which dates and times for data read from the worksheet are calculated.
*/
private DateTime __workingDate = null;

/**
The top-most visible Y value of the JWorksheet at the time of the last call
to getValueAt(); used to determine when the worksheet has been scrolled and
__firstVisibleRow and __firstVisibleRowDate need to be recalculated.
*/
private double __previousTopmostVisibleY = -1;

/**
The interval of date times that will be cached in __cachedDates.  Every Xth
DateTime from the entire table, where X == __cacheInterval, will be pre-calculated and cached.
This is for regular time series only.
*/
private int __cacheInterval;

/**
Number of columns in the table model.
*/
private int __columns;

/**
Cache for date/times for irregular time series.
*/
private List<DateTime> __irregularDateTimeCache;

/**
Whether the irregular time series time zone is the same.
If the same, then the date/time column will show the time zone.
If not the same, then the date/time column will NOT show the time zone.
The time zone is shown in the tool tips for the column header.
*/
private boolean __irregularTZSame = true;

/**
The time zone for all irregular time series (if __irregularTZSame).
*/
private String __irregularTZ = "";

/**
The list of prototype DateTime objects to use for getValue() calls.  This ensures that the timezone is consistent
with the time series.  All other values will be set before calling getValue().
*/
private DateTime [] __irregularPrototypeDateTime = null;

/**
The format in which dates should be displayed.  See DateTime.FORMAT_*.
*/
private int __dateFormat;

/**
The first row that is visible on the JWorksheet; used in caching.
*/
private int __firstVisibleRow;

/**
The TS data interval; one of TimeInterval.*
*/
private int __intervalBase;

/**
The TS interval multiplier.
*/
private int __intervalMult;

/**
The date/time precision if irregular time series (set to TimeInterval.UNKNOWN since DateTime does not have similar).
*/
private int __irregularDateTimePrecision = TimeInterval.UNKNOWN;

/**
The row for which data was previously read.
*/
private int __lastRowRead = -1;

/**
The row for which data was previously read in a consecutive read.
*/
private int __priorRow = -1;

/**
The worksheet in which this table model is being used.
*/
private JWorksheet __worksheet;
/**
The formats (in printf() style) in which the column data should be displayed.
This depends on the time series data units.
*/
private String[] __dataFormats;

/**
Indicate how data flags should be visualized.
*/
private TSDataFlagVisualizationType __dataFlagVisualizationType = TSDataFlagVisualizationType.NOT_SHOWN;

/**
Indicate whether an extra data row column should be shown.  Use for troubleshooting.
TODO SAM 2014-04-06 Remove when not needed.
*/
private boolean __showRow = false;

/**
Constructor.  This builds the Model for displaying the given TS data and
pre-calculates and caches every 50th row's date.
@param data Vector of TS to graph in the table.  The TS must have the same
data interval and data units, but this will not be checked in the table model; 
it should have been done previously.
@param start the first day of data to display in the table.
@param intervalBase the TS data interval (from TimeInterval.*)
@param intervalMult the TS data multiplier.
@param dateFormat the format in which to display the date column (column 0).
@param dataFormats the formats in which to display the data columns (columns 1
through N).  The format for data column N should be at array position N-1.  
@param useExtendedLegend whether to use the extended TS legend for the TS column
title, or the normal legend.  This is determined by the value of the propvalue
"Table.UseExtendedLegend" passed into the TSViewJFrame.
@throws Exception if an invalid data or dmi was passed in.
*/
public TSViewTable_TableModel(List<TS> data, DateTime start, 
int intervalBase, int intervalMult, int dateFormat, String[] dataFormats, 
boolean useExtendedLegend)
throws Exception {
	this(data, start, intervalBase, intervalMult, dateFormat, dataFormats, useExtendedLegend, 50);
}

/**
Constructor.  This builds the Model for displaying the given TS data.
@param data list of TS to graph in the table.  The TS must have the same
data interval and data units, but this will not be checked in the table model; 
it should have been done previously.
@param start the first day of data to display in the table.
@param intervalBase the TS data interval (from TimeInterval.*)
@param intervalMult the TS data multiplier.
@param dateFormat the format in which to display the date column (column 0).
@param dataFormats the formats in which to display the data columns (columns 1
through N).  The format for data column N should be at array position N-1.  
@param useExtendedLegend whether to use the extended TS legend for the TS column
title, or the normal legend.  This is determined by the value of the propvalue
"Table.UseExtendedLegend" passed into the TSViewJFrame.
@param cacheInterval the interval of dates to pre-calculate and cache.  Every
Nth date in the entire table, where N == cacheInterval, will be pre-calculated
and cached to improve performance.  The other constructor passes in a value of
50 for the interval, and this value has been found to be adequate for most
table needs.  It takes some experimenting to find the optimal value where 
speed is most increased but not too much memory is used.<p>
JTS recommends that if a table will display at most X rows at once, that the cacheInterval be no less than X*2.
@throws Exception if an invalid data or dmi was passed in.
*/
public TSViewTable_TableModel(List<TS> data, DateTime start, 
int intervalBase, int intervalMult, int dateFormat, String[] dataFormats, 
boolean useExtendedLegend, int cacheInterval )
throws Exception
{
    //Message.printStatus(2,"TSView_TableModel","data=" + data + " start=" + start + " intervalBase=" + intervalBase +
    //    " intervalMult=" + intervalMult );
    //if ( data != null ) {
    //    Message.printStatus(2,"TSView_TableModel","data.size()=" + data.size() );
    //}
    
	if (data == null) {
		throw new Exception ("Null data list passed to TSViewTable_TableModel constructor.");
	}	
	_data = data;
	__columns = data.size() + 1;
	if ( intervalBase == TimeInterval.IRREGULAR ) {
	    TS ts = (TS)data.get(0);
	    DateTime d = ts.getDate1();
	    if ( d == null ) {
	        d = ts.getDate1Original();
	    }
	    if ( d != null ) {
	        __irregularDateTimePrecision = d.getPrecision();
	    }
	    // Determine if all the time series have a consistent time zone.  If yes, the time zone will
	    // be displayed in the date/time column.  If no, the time zone is removed.
	    // Column tool-tips include the time zone.
	    // Also save a prototype DateTime for each time series that matches Date1, which will be used for getValue()
	    // to make sure the time zone is handled.
	    __irregularTZSame = true;
	    __irregularTZ = null;
	    __irregularPrototypeDateTime = new DateTime[__columns - 1];
	    if ( (__irregularDateTimePrecision == DateTime.PRECISION_MINUTE) ||
	        (__irregularDateTimePrecision == DateTime.PRECISION_HOUR) ) {
    	    for ( int i = 1; i < __columns; i++ ) {
    	        ts = (TS)_data.get(i - 1);
                String tz = "";
                DateTime dt = ts.getDate1();
                __irregularPrototypeDateTime[i - 1] = null;
                if ( dt != null ) {
                    tz = dt.getTimeZoneAbbreviation();
                    __irregularPrototypeDateTime[i - 1] = new DateTime(dt);
                }
                if ( __irregularTZ == null ) {
                    __irregularTZ = tz;
                }
                else if ( __irregularTZSame && !__irregularTZ.equalsIgnoreCase(tz) ) {
                    __irregularTZSame = false;
                    __irregularTZ = "";
                }
    	    }
	    }
	    // Cache for irregular time series is handled differently
	    // Do this AFTER doing the time zone comparison above
        createIrregularTSDateTimeCache ();
        __intervalMult = -1; // Should not be used for irregular
	}
	else {
	    __cacheInterval = cacheInterval;
	    __intervalMult = intervalMult;
	}
	__intervalBase = intervalBase;
	__dateFormat = dateFormat;
	__dataFormats = dataFormats;
	__useExtendedLegend = useExtendedLegend;
	__start = start;
	
	if (__columns > 1) {		
		TSLimits limits = TSUtil.getPeriodFromTS(data, TSUtil.MAX_POR);
		DateTime end = limits.getDate2();
		if ( intervalBase == TimeInterval.IRREGULAR ) {
		    _rows = __irregularDateTimeCache.size();
		}
		else {
		    _rows = TSUtil.calculateDataSize((TS)data.get(0), __start, end);
		}
	}

	if ( intervalBase != TimeInterval.IRREGULAR ) {
    	__firstVisibleRow = 0;
    	__firstVisibleRowDate = __start;
    	__workingDate = new DateTime(__start);
    	if ( __cacheInterval == 0 ) {
    	    __cachedDates = new DateTime[1];
    	}
    	else {
    	    __cachedDates = new DateTime[(_rows / __cacheInterval) + 1];
    	}
    
    	// Cache the dates of each __cacheInterval row through the time series.
    
    	__cachedDates[0] = __start;
    	for (int i = 1; i < __cachedDates.length; i++) {
    		__cachedDates[i] = new DateTime(__cachedDates[i - 1]);
    
    		if (__intervalBase == TimeInterval.MINUTE) {
    			__cachedDates[i].addMinute(__cacheInterval * __intervalMult);
    		}	
    		else if (__intervalBase == TimeInterval.HOUR) {
    			__cachedDates[i].addHour(__cacheInterval * __intervalMult);
    		}
    		else if (__intervalBase == TimeInterval.DAY) {
    			__cachedDates[i].addDay(__cacheInterval * __intervalMult);
    		}	
    		else if (__intervalBase == TimeInterval.MONTH) {
    			__cachedDates[i].addMonth(__cacheInterval * __intervalMult);
    		}
    		else if (__intervalBase == TimeInterval.YEAR) {
    			__cachedDates[i].addYear(__cacheInterval * __intervalMult);
    		}
    	}
	}
	// Adjust columns if the row is being shown for troubleshooting
    if ( __showRow ) {
        ++__columns;
    }
}

/**
Create a cache of date/times for irregular time series (all date/times that occur).
The worksheet row=0 will correspond to the first date/time.
*/
private void createIrregularTSDateTimeCache ()
throws TSException
{   String routine = getClass().getSimpleName() + ".createIrregularTSDateTimeCache";
    // More than one irregular time series.  They at least have to have the same date/time precision
    // for the period.  Otherwise it will be difficult to navigate the data.
    int irrPrecision = __irregularDateTimePrecision;
    int tsPrecision;
    List<Object> dataList = _data;
    List<TS> tslist = _data;
    int size = tslist.size();
    TS ts;
    for ( int its = 0; its < size; its++ ) {
        if ( tslist.get(its) == null ) {
            continue;
        }
        ts = (IrregularTS)tslist.get(its);
        if ( ts.getDate1() == null ) {
            continue;
        }
        tsPrecision = ts.getDate1().getPrecision();
        if ( tsPrecision == TimeInterval.IRREGULAR ) {
            // Treat as minute
            tsPrecision = DateTime.PRECISION_MINUTE;
        }
        if ( irrPrecision == -1 ) {
            // Just assign
            irrPrecision = tsPrecision;
        }
        else if ( irrPrecision != tsPrecision ) {
            // This will be a problem in processing the data
            String message = "Irregular time series do not have the same date/time precision by checking period start and end.  Can't display";
            Message.printWarning ( 2, routine, message );
            throw new UnequalTimeIntervalException ( message );
        }
    }
    // Was able to determine the precision of data so can continue
    // The logic works as follows:
    // 0) Advance the iterator for each time series to initialize
    // 1) Find the earliest date/time in the iterator current position
    // 2) Add cached date/times that will result in:
    //    - actual value if time series has a value at the date/time
    //    - values not at the same date/time result in blanks for the other time series
    // 3) For any values that will be output, advance that time series' iterator
    // 4) Go to step 1
    // Create iterators for each time series
    TSIterator [] tsIteratorArray = new TSIterator[tslist.size()];
    __irregularDateTimeCache = new ArrayList<DateTime>();
    for ( int its = 0; its < size; its++ ) {
        if ( tslist.get(its) == null ) {
            tsIteratorArray[its] = null; // Keep same order as time series
        }
        ts = (IrregularTS)tslist.get(its);
        try {
            tsIteratorArray[its] = ts.iterator(); // Iterate through full period
        }
        catch ( Exception e ) {
            tsIteratorArray[its] = null;; // Keep same order as time series
        }
    }
    int its;
    TSIterator itsIterator;
    // Use the following to extract dates from each time series
    // A call to the iterator next() method will return null when no more data, which is
    // the safest way to process the data
    TSData [] tsdata = new TSData[tslist.size()];
    TSData tsdataMin = null; // Used to find min date/time for all the iterators
    DateTime dtMin = null; // Used to compare date/times for all the iterators
    DateTime dtCached = null; // Cached date/time
    int iteratorMin = -1;
    int loopCount = 0;
    while ( true ) {
        // Using the current date/time, output the earliest value for all time series that have the value and
        // increment the iterator for each value that is output.
        ++loopCount;
        if ( loopCount == 1 ) {
            // Need to call next() one time on all time series to initialize all iterators to the first
            // data point in the time series.  Otherwise, next() is only called below to advance.
            for ( its = 0; its < size; its++ ) {
                itsIterator = tsIteratorArray[its];
                if ( itsIterator != null ) {
                    tsdata[its] = itsIterator.next();
                }
            }
        }
        // Loop through the iterators:
        // 1) Find the earliest date/time
        // 2) Add to the cache
        // 3) Advance the iterator(s)
        // Do this until all iterators next() have returned null
        tsdataMin = null;
        dtMin = null;
        for ( its = 0; its < size; its++ ) {
            if ( tsdataMin == null ) {
                // Find the first date/time for all the iterators at their current positions
                if ( tsdata[its] != null ) {
                    tsdataMin = tsdata[its];
                    dtMin = tsdataMin.getDate();
                    iteratorMin = its;
                    //Message.printStatus(2, routine, "Initializing minimum date/time to " + dtMin );
                }
            }
            else {
                // Have a non-null first date/time to compare to so check this iterator against it
                // The lessThan() method DOES NOT compare time zone in any case
                if ( (tsdata[its] != null) && tsdata[its].getDate().lessThan(dtMin) ) {
                    // Have found an earlier date/time to be used in the comparison
                    // Note - time zone is NOT checked
                    dtMin = tsdata[its].getDate();
                    tsdataMin = tsdata[its];
                    iteratorMin = its;
                    //Message.printStatus(2, routine, "Found earlier minimum date/time [" + its + "] " + dtMin );
                }
            }
        }
        // 2) Add to the cache
        if ( dtMin == null ) {
            // Done processing all data
            break;
        }
        // Create a new instance so independent of any data manipulations.
        // Create a fast instance since it will be used for iteration and data access but not be manipulated or checked
        dtCached = new DateTime(dtMin,DateTime.DATE_FAST);
        if ( !__irregularTZSame ) {
            // Set the timezone to blank for cached date/times rather than showing wrong time zone that might be misinterpreted
            dtCached.setTimeZone("");
        }
        __irregularDateTimeCache.add(dtCached);
        //Message.printStatus(2,routine,"Row [" + (__irregularDateTimeCache.size() - 1) + "] " + dtCached);
        // 3) Advance the iterator for the one with the minimum date/time and all with the same date/time
        // Note - time zone is NOT checked by equals()
        for ( its = 0; its < size; its++ ) {
            // First check below increases performance a bit
            // Use the prototype date if available to ensure that time zone is handled
            if ( (__irregularPrototypeDateTime != null) && (__irregularPrototypeDateTime[its] != null) ) {
                // Use the prototype DateTime (which has proper time zone) and overwrite the specific date/time values
                // Do not call setDate() because it will set whether to use time zone and defeat the purpose of the prototype
                __irregularPrototypeDateTime[its].setYear(dtMin.getYear());
                __irregularPrototypeDateTime[its].setMonth(dtMin.getMonth());
                __irregularPrototypeDateTime[its].setDay(dtMin.getDay());
                __irregularPrototypeDateTime[its].setHour(dtMin.getHour());
                __irregularPrototypeDateTime[its].setMinute(dtMin.getMinute());
                __irregularPrototypeDateTime[its].setSecond(dtMin.getSecond());
                dtMin = __irregularPrototypeDateTime[its];
            }
            if ( (iteratorMin == its) || ((tsdata[its] != null) && dtMin.equals(tsdata[its].getDate())) ) {
                tsdata[its] = tsIteratorArray[its].next();
                //Message.printStatus(2, routine, "Advanced iterator[" + its + "] date/time to " + tsdata[its] );
            }
        }
    }
    // Set the number of rows
    _rows = __irregularDateTimeCache.size();
    if ( _rows > 0 ) {
        //Message.printStatus(2, routine, "Number of cached irregular date/times=" + _rows +
        //    " first date/time=" + __irregularDateTimeCache.get(0) +
        //    " last date/time=" + __irregularDateTimeCache.get(_rows - 1) );
    }
    // Dump out the calls to the table model
    //for ( int row = 0; row < _rows; row++ ) {
    //    Message.printStatus(2,routine + "2","Row [" + row + "] " + getValueAtIrregular(row, 0));
    //}
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	IOUtil.nullArray(__cachedDates);
	__firstVisibleRowDate = null;
	__priorDateTime = null;
	__start = null;
	__workingDate = null;
	__worksheet = null;
	IOUtil.nullArray(__dataFormats);
	super.finalize();
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	if ( columnIndex == 0 ) {
	    return String.class; // Date/Time
	}
    else if ( __showRow && (columnIndex == (__columns - 1)) ) {
        return String.class;
    }
	else {
		// TS data
	    // If data flags are superscripted, return a String
	    if ( __dataFlagVisualizationType == TSDataFlagVisualizationType.SUPERSCRIPT ) {
	        return String.class;
	    }
	    else {
	        return Double.class;
	    }
	}
}

/**
Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __columns;
}

/**
Returns the name of the column at the given position.
For column 0, the name will be DATE or DATE/TIME depending on the date/time precision.  For time series the string will
be alias (or location), sequence number, data type, units.  If a time series property TableViewHeaderFormat is set, then this
format will be used to format the string.  The format can contain % and ${ts:Property} specifiers.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex)
{
	if ( columnIndex == 0 ) {
		if ((__intervalBase == TimeInterval.HOUR) || (__intervalBase == TimeInterval.MINUTE)) {
			return "DATE/TIME";
		}
		else if ( __intervalBase == TimeInterval.IRREGULAR ) {
	        if ( (__irregularDateTimePrecision == DateTime.PRECISION_MINUTE) || (__irregularDateTimePrecision == DateTime.PRECISION_HOUR) ) {
	            return "DATE/TIME";
	        }
	        else {
	            return "DATE";
	        }
		}
		return "DATE";
	}
	else if ( __showRow && (columnIndex == (__columns - 1)) ) {
	    return "ROW";
	}
	
	// Otherwise the column names depends on time series properties
    TS ts = (TS)_data.get(columnIndex - 1);
    
    Object propVal = ts.getProperty("TableViewHeaderFormat");
    if ( (propVal != null) && !propVal.equals("") ) {
        String format = "" + propVal;
        return ts.formatLegend(format);
    }

	// The following are expensive String operations (concats, etc), but
	// this method is not called very often (just once when the table is
	// first displayed?) so this shouldn't be a problem.

	if (__useExtendedLegend && (ts.getExtendedLegend().length() != 0)) {
		return ts.formatLegend(ts.getExtendedLegend());
	}
	else if (ts.getLegend().length() > 0) {
		return ts.formatLegend(ts.getLegend());
	}
	else {	
		String unitsString = "";
		String datatypeString = "";
		String sequenceString = "";
		if (ts.getDataUnits().length() > 0) {
			unitsString = ", " + ts.getDataUnits();
		}
		if (ts.getDataType().length() == 0) {
			datatypeString = ", " + ts.getIdentifier().getType();
		}
		else {
			datatypeString = ", " + ts.getDataType();
		}
		if (ts.getSequenceID().length() > 0) {
			sequenceString = " [" + ts.getSequenceID() + "]";
		}
		if (ts.getAlias().equals("")) {
			return ts.getLocation() + sequenceString + datatypeString + unitsString;
		}
		else {
			return ts.getAlias() + sequenceString + datatypeString + unitsString;
		}
	}
}

/**
Returns an array containing the column tool tips.
@return an array containing the column tool tips.
*/
public String[] getColumnToolTips()
{
    String[] tt = new String[__columns];
    TS ts;
    StringBuilder sb = new StringBuilder (
        "<html>The DATE or DATE/TIME is formatted according to the precision of date/times for the time series." );
    if ( __irregularTZSame ) {
        sb.append("<br>The time zone for the date/time column is included " +
        	"because all irregular time series have the same time zone.");
    }
    else {
        // Time zones are not equal and therefore time zone is set to blank for the date/time cache
        sb.append("<br>The time zone for the date/time column has been removed because the time zone is different for the time series." );
        sb.append("<br>See column heading tool tips for each time zone.");
        sb.append("<br>If desired, display a table for only time series with the same time zone.");
    }
    sb.append("</html>");
    tt[0] = sb.toString();
    int iend = __columns;
    if ( __showRow ) {
        --iend;
    }
    for ( int i = 1; i < iend; i++ ) {
        ts = (TS)_data.get(i - 1);
        sb = new StringBuilder(
            "<htmL>TSID = " + ts.getIdentifierString() + "<br>" +
            "Alias = " + ts.getAlias() + "<br>" +
            "Description = " + ts.getDescription() );
        if ( (__intervalBase == TimeInterval.MINUTE) || (__intervalBase == TimeInterval.HOUR) ||
            (__intervalBase == TimeInterval.IRREGULAR) ) {
            String tz = "";
            DateTime dt = ts.getDate1();
            if ( dt != null ) {
                tz = dt.getTimeZoneAbbreviation();
            }
            sb.append ( "<br>Date/time time zone = " + tz );
        }
        sb.append ( "</html>" );
        tt[i] = sb.toString();
    }
    return tt;
}

/**
Does a consecutive read to get the value at the specified row and column.
See JWorksheet for more information on consecutive reads.
@param row row from which to return a value.
@param col column from which to return a value.
@return the value at the specified row and column.
*/
public Object getConsecutiveValueAt(int row, int col) {
	if (shouldResetGetConsecutiveValueAt()) {	
		shouldResetGetConsecutiveValueAt(false);
		__priorRow = -1;
	}

	if (__priorRow == -1) {
		DateTime temp = new DateTime( __cachedDates[row / __cacheInterval]);
		if (__intervalBase == TimeInterval.MINUTE) {
			temp.addMinute((row % __cacheInterval) * __intervalMult);
		}	
		else if (__intervalBase == TimeInterval.HOUR) {
			temp.addHour((row % __cacheInterval) * __intervalMult);
		}
		else if (__intervalBase == TimeInterval.DAY) {
			temp.addDay((row % __cacheInterval) * __intervalMult);
		}	
		else if (__intervalBase == TimeInterval.MONTH) {
			temp.addMonth((row % __cacheInterval) * __intervalMult);
		}
		else if (__intervalBase == TimeInterval.YEAR) {
			temp.addYear((row % __cacheInterval) * __intervalMult);
		}
		__priorDateTime = temp;
		__priorRow = row;
	}			
	else if (__priorRow != row) {
		if (__intervalBase == TimeInterval.MINUTE) {
			__priorDateTime.addMinute(1 * __intervalMult);
		}	
		else if (__intervalBase == TimeInterval.HOUR) {
			__priorDateTime.addHour(1 * __intervalMult);
		}
		else if (__intervalBase == TimeInterval.DAY) {
			__priorDateTime.addDay(1 * __intervalMult);
		}	
		else if (__intervalBase == TimeInterval.MONTH) {
			__priorDateTime.addMonth(1 * __intervalMult);
		}
		else if (__intervalBase == TimeInterval.YEAR) {
			__priorDateTime.addYear(1 * __intervalMult);
		}
		__priorRow = row;
	}
	
	if (col > 0) {
		TS ts = (TS)_data.get(col-1);
		return new Double(ts.getDataValue(__priorDateTime));
	}
	else {
		return __priorDateTime;
	}
}

/**
Returns the total number of characters in a DateTime object formatted with __dateFormat.
@return the total number of characters in a DateTime object formatted with __dateFormat.
*/
private int getDateFormatLength()
{
	// TODO (SAM - 2003-07-21) might add something similar to DateTime.
	switch (__dateFormat) {
		case DateTime.FORMAT_MM:
			return 2;
		case DateTime.FORMAT_HHmm:
		case DateTime.FORMAT_YYYY:
			return 4;
		case DateTime.FORMAT_HH_mm:
		case DateTime.FORMAT_MM_DD:
		case DateTime.FORMAT_MM_SLASH_DD:
			return 5;
		case DateTime.FORMAT_MM_SLASH_YYYY:
		case DateTime.FORMAT_YYYY_MM:
			return 7;
		case DateTime.FORMAT_MM_SLASH_DD_SLASH_YY:
			return 8;
		case DateTime.FORMAT_MM_SLASH_DD_SLASH_YYYY:
		case DateTime.FORMAT_YYYY_MM_DD:
			return 10;
		case DateTime.FORMAT_YYYYMMDDHHmm:
			return 12;
		case DateTime.FORMAT_MM_SLASH_DD_SLASH_YYYY_HH:
		case DateTime.FORMAT_MM_DD_YYYY_HH:
		case DateTime.FORMAT_YYYY_MM_DD_HH:
			return 13;
		case DateTime.FORMAT_AUTOMATIC:
		case DateTime.FORMAT_NONE:
		case DateTime.FORMAT_YYYY_MM_DD_HHmm:
			return 15;
		case DateTime.FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm:
		case DateTime.FORMAT_YYYY_MM_DD_HH_mm:
			return 16;
		case DateTime.FORMAT_YYYY_MM_DD_HH_ZZZ:
			return 17;
		case DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS:
			return 19;
		case DateTime.FORMAT_YYYY_MM_DD_HH_mm_ZZZ:
			return 20;
		case DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS_hh:
			return 22;
		case DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ:
			return 23;
		case DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ:
			return 26;
		default:
			return 15;
	}
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	if ( column == 0 ) {
	    return "%" + getDateFormatLength() + "s";
	}
    else if ( __showRow && (column == (__columns - 1)) ) {
        return "%s";
    }
	else {
	    if ( __dataFlagVisualizationType == TSDataFlagVisualizationType.SUPERSCRIPT ) {
	        // Data value is represented as string with flag as superscript
	        return "%s";
	    }
	    else {
	        // Not displaying data flags so format number
	        return __dataFormats[column-1];
	    }
	}
}

/**
Returns the interval base for the time series.
@return the interval base for the time series.
*/
public int getIntervalBase() {
	return __intervalBase;
}

/**
Returns the interval mult for the time series.
@return the interval mult for the time series.
*/
public int getIntervalMult() {
	return __intervalMult;
}

/**
Returns the irregular date/time precision.
@return the irregular date/time precision.
*/
public int getIrregularDateTimePrecision() {
    return __irregularDateTimePrecision;
}

/**
Returns the number of rows of data in the table.
@return the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns the time series.
@return the time series at a specific index i.
*/
public TS getTS ( int i )
{	
	return (TS)_data.get(i);
}

/**
Returns the time series.
@return the Vector of time series.
*/
public List<TS> getTSList() {	
	return _data;
}

/**
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col)
{
    if ( __intervalBase == TimeInterval.IRREGULAR ) {
        // Irregular data have all the date/times cached consistent with rows so handle specifically
        return getValueAtIrregular(row,col);
    }

	if (shouldDoGetConsecutiveValueAt()) {
		// Do a consecutive get value at rather than this sequential one.
		return getConsecutiveValueAt(row, col);
	}

	double y = __worksheet.getVisibleRect().getY();	
	
	// If it's a new Y point from the last time getValueAt was called,
	// then that means some scrolling has occurred and the top-most row
	// is new.  Need to recalculate the date of the top most row

	if (__previousTopmostVisibleY != y) {
		__previousTopmostVisibleY = y;
		__firstVisibleRow = __worksheet.rowAtPoint(new Point(0,(int)y));

		// Calculate its date time by looking up the nearest 
		// cached one and adding the remainder of intervals to it
		__firstVisibleRowDate = new DateTime( __cachedDates[__firstVisibleRow / __cacheInterval]);
		int precision = 0;
		if (__intervalBase == TimeInterval.MINUTE) {
			precision = DateTime.PRECISION_MINUTE;
			__firstVisibleRowDate.addMinute( (__firstVisibleRow % __cacheInterval) * __intervalMult);
		}	
		else if (__intervalBase == TimeInterval.HOUR) {
			precision = DateTime.PRECISION_HOUR;
			__firstVisibleRowDate.addHour( (__firstVisibleRow % __cacheInterval) * __intervalMult);
		}
		else if (__intervalBase == TimeInterval.DAY) {
			precision = DateTime.PRECISION_DAY;
			__firstVisibleRowDate.addDay( (__firstVisibleRow % __cacheInterval) * __intervalMult);
		}	
		else if (__intervalBase == TimeInterval.MONTH) {
			precision = DateTime.PRECISION_MONTH;
			__firstVisibleRowDate.addMonth( (__firstVisibleRow % __cacheInterval) * __intervalMult);
		}
		else if (__intervalBase == TimeInterval.YEAR) {
			precision = DateTime.PRECISION_YEAR;
			__firstVisibleRowDate.addYear( (__firstVisibleRow % __cacheInterval) * __intervalMult);
		}

		__workingDate = new DateTime(__firstVisibleRowDate, DateTime.DATE_FAST | precision);

		// Reset this so that on a scroll event none of the rows are drawn incorrectly.
		// Removing this line will result in a "scrambled"-looking JTable.
		__lastRowRead = -1;
	}

	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	// getValueAt is called row-by-row when a worksheet displays its
	// data, so the current working date (with which data for the current
	// row is read) only needs to be recalculated when a new row is moved to.
	if (row != __lastRowRead) {
		__lastRowRead = row;

		// Quicker than doing a 'new DateTime'
		__workingDate.setHSecond ( __firstVisibleRowDate.getHSecond() );
		__workingDate.setSecond ( __firstVisibleRowDate.getSecond() );
		__workingDate.setMinute ( __firstVisibleRowDate.getMinute() );
		__workingDate.setHour ( __firstVisibleRowDate.getHour() );
		__workingDate.setDay ( __firstVisibleRowDate.getDay() );
		__workingDate.setMonth ( __firstVisibleRowDate.getMonth() );
		__workingDate.setYear ( __firstVisibleRowDate.getYear() );

		// Calculate the date for the current row read
		if (__intervalBase == TimeInterval.MINUTE) {
			__workingDate.addMinute(((row - __firstVisibleRow)* __intervalMult));
		}	
		else if (__intervalBase == TimeInterval.HOUR) {
			__workingDate.addHour(((row - __firstVisibleRow)* __intervalMult));
		}
		else if (__intervalBase == TimeInterval.DAY) {
			__workingDate.addDay(((row - __firstVisibleRow)* __intervalMult));
		}	
		else if (__intervalBase == TimeInterval.MONTH) {
			__workingDate.addMonth(((row - __firstVisibleRow)* __intervalMult));
		}
		else if (__intervalBase == TimeInterval.YEAR) {
			__workingDate.addYear(((row - __firstVisibleRow)* __intervalMult));
		}
	}
	
	if (col == 0) {
		return __workingDate.toString();
	}

	// TODO JTS - 2006-05-24
	// It's possible that a VERY slight performance gain could be made
	// by using an array to access the time series, rather than doing a 
	// cast out of a Vector.  JTS has found that given these two statements:
	// 	- ts = (TS) vector.elementAt(i);
	//	- ts = array[i];
	// the array statement is about 4 times faster.
	TS ts = (TS)_data.get(col - 1);

	if ( __dataFlagVisualizationType == TSDataFlagVisualizationType.SUPERSCRIPT ) {
	    return getValueAtFormatValueWithFlag(ts, __workingDate,__dataFormats[col-1]);
	}
	else {
	    return new Double(ts.getDataValue(__workingDate));
	}
}

/**
Format the data value with the flag.
*/
private String getValueAtFormatValueWithFlag ( TS ts, DateTime dt, String dataFormat )
{
    TSData tsdata = ts.getDataPoint(dt, null);
    double value = tsdata.getDataValue();
    String flag = tsdata.getDataFlag();
    if ( flag == null ) {
        flag = "";
    }
    if ( ts.isDataMissing(value) ) {
        if ( flag.equals("") ) {
            // No value, no flag
            return "";
        }
        else {
            // No value but have flag
            return "^" + flag;
        }
    }
    else {
        // TODO SAM 2012-04-16 Figure out formatting for value
        if ( flag.equals("") ) {
            // Value but no flag
            return "" + StringUtil.formatString(value,dataFormat);
        }
        else {
            // Value and flag
            return "" + StringUtil.formatString(value,dataFormat) + "^" + flag;
        }
    }
}

/**
Returns the data that should be placed in the JTable at the given row and column, for irregular time series.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAtIrregular(int row, int col)
{
    if (_sortOrder != null) {
        row = _sortOrder[row];
    }
    
    // Based on the row, determine the date/time for the data
    DateTime dt = __irregularDateTimeCache.get(row);
    double value;
    if ( col == 0 ) {
        // Returning the date
        return dt.toString();
    }
    else if ( __showRow && (col == (__columns - 1)) ) {
        return "" + row;
    }
    else {
        // Returning the time series data value
        TS ts = (TS)_data.get(col - 1);
        if ( (__irregularPrototypeDateTime != null) && (__irregularPrototypeDateTime[col - 1] != null) ) {
            // Use the prototype DateTime (which has proper time zone for the time series)
            // and overwrite the specific date/time values.
            // Do not call setDate() because it will set whether to use time zone and defeat the purpose of the prototype
            // Then, reset the date/time for the column to one that matches the time series and get the value.
            __irregularPrototypeDateTime[col - 1].setYear(dt.getYear());
            __irregularPrototypeDateTime[col - 1].setMonth(dt.getMonth());
            __irregularPrototypeDateTime[col - 1].setDay(dt.getDay());
            __irregularPrototypeDateTime[col - 1].setHour(dt.getHour());
            __irregularPrototypeDateTime[col - 1].setMinute(dt.getMinute());
            __irregularPrototypeDateTime[col - 1].setSecond(dt.getSecond());
            dt = __irregularPrototypeDateTime[col - 1];
        }
        if ( __dataFlagVisualizationType == TSDataFlagVisualizationType.SUPERSCRIPT ) {
            return getValueAtFormatValueWithFlag(ts, dt, __dataFormats[col-1]);
        }
        else {
            value = ts.getDataValue(dt);
            //Message.printStatus(2, "getValue", "" + "Row [" + row + "] " + dt + " Col [" + col + "] value=" + value +
            //    " Row [" + (_rows - 1) + "] " + getValueAtIrregular(_rows - 1, 0) );
            return new Double(value);
        }
    }
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__columns];
	String colName = null;
	int len = 0;
	
	if (__columns > 0) {
		widths[0] = getDateFormatLength() + (int)(getDateFormatLength() / 10) + 1;
	}
	for (int i = 1; i < __columns; i++) {
		colName = getColumnName(i);
		len = colName.length();
		if (len > 13) {
			widths[i] = len;
		}
		else {
			// 10.2f
			widths[i] = 10;
		}
	}
	return widths;
}

/**
Returns whether the cell is editable or not.  Returns false.
@param rowIndex unused.
@param columnIndex unused.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (columnIndex > 0) {
		if (1 == 1) {
			// TODO (JTS - 2004-01-22) no editing supported yet
			return false;
		}
		if ( __dataFlagVisualizationType != TSDataFlagVisualizationType.NOT_SHOWN) {
    		// FIXME SAM (2010-07-15) Figure this out - we added some editing.
    		TS ts = (TS)_data.get(columnIndex - 1);
    		return ts.isEditable();
		}
		else {
		    // TODO SAM 2012-04-16 Editing when flags are shown is not yet implemented
		    return false;
		}
	}
	return false;
}

/**
Set how data flags should be visualized.
*/
public void setDataFlagVisualizationType ( TSDataFlagVisualizationType dataFlagVisualizationType )
{
    __dataFlagVisualizationType = dataFlagVisualizationType;
}

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value.
@param col the col of the cell for which to set the value.
*/
public void setValueAt(Object value, int row, int col) {	
	DateTime d = null;
	try {
		d = DateTime.parse((String)getValueAt(row, 0));
	}
	catch (Exception ex) {
		if (IOUtil.testing()) {
			ex.printStackTrace();
		}
		return;
	}

	TS ts = (TS)_data.get(col-1);

	if (ts == null) {
		return;
	}

	if (value == null) {
		ts.setDataValue(d, -999.0);
	}
	else if (value instanceof Double) {
		ts.setDataValue(d, ((Double)value).doubleValue());
	}
	else if (value instanceof String) {
		ts.setDataValue(d, (new Double((String)value)).doubleValue());
	}
	
	super.setValueAt(value, row, col);
}

/**
Sets the worksheet in which this table model is being used.
@param worksheet the worksheet in which the instance of this table model is used.
*/
public void setWorksheet(JWorksheet worksheet) {
	__worksheet = worksheet;
}

/**
Sets up the table model to prepare for a consecutive read.  For more information
see the JWorksheet javadoc about consecutive reads.
*/
public void startNewConsecutiveRead() {
	__priorRow = -1;
	__priorDateTime = null;
}

}