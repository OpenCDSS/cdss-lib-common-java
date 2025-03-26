// TSViewTable_TableModel - table model for displaying regular TS data

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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
This class is a table model for displaying regular TS data.
It is more complicated than most table models,
primarily in order to achieve performance gains with getting values out of a time series.
The following is a brief description of some of the performance gain efforts made in this class.
These caching methods are both found in getValueAt().<p>
<b>Date Caching</b><p>
At creation time, dates throughout the range of the time series are cached into an internal array.
Then, when a row of data needs to be drawn in the worksheet,
the date nearest the row to be drawn is used instead of calculating the date from the first row.<p>
This is because adding many intervals at once to a date/time is an expensive operation.
Using a day time series as an example, adding X days to a date/time takes X times as long as adding 1 day.
Thus, by caching dates along the entire span of the time series,
it can be ensured that __cacheInterval will be the greatest number of intervals ever added to a single date/time at once.<p>
<b>Caching of the Top-Most Visible Row Number After Each Scroll Event</b><p>
Caching is also done of the top-most visible row every time the worksheet is scrolled,
and involves the __firstVisibleRowDate and __previousTopmostVisibleY member variables.
Each time getValueAt() is called, it checks to see if the
top Y value of the worksheet is different from it was when getValueAt() was last called.
If so, then the worksheet has been scrolled.
Each time the worksheet is scrolled, the date/time of the top-most visible row is calculated using date caching.
Then, the date of each row that is drawn for the current scroll position is calculated from the date of the top-most visible row.<p>
<b>Notes</b>
These caching steps may seem overkill, but JTS found during extensive testing
that they increase the speed of browsing through a table of time series dramatically.
Without the caching, scrolling to the end of a long time series can take many seconds,
whereas it is nearly instant with the steps taken here.
*/
@SuppressWarnings("serial")
public class TSViewTable_TableModel extends JWorksheet_AbstractRowTableModel<TS>
{

	/**
	 * Whether the time zones for all time series are the same.
	 * This is used to control how the column headings are formatted.
	 */
	private boolean areTimeZonesSame = true;
	
/**
An array of DateTime values that are pre-calculated in order to speed up calculation of DateTimes in the middle of the dataset.
Each element in this array contains the DateTime for the row at N*(__cacheInterval).
This is used with regular interval time series.
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
The top-most visible Y value of the JWorksheet at the time of the last call to getValueAt();
used to determine when the worksheet has been scrolled and
__firstVisibleRow and __firstVisibleRowDate need to be recalculated.
*/
private double __previousTopmostVisibleY = -1;

/**
The interval of date times that will be cached in __cachedDates.
Every Xth DateTime from the entire table, where X == __cacheInterval, will be pre-calculated and cached.
This is for regular time series only.
*/
private int __cacheInterval;

/**
Number of columns in the table model.
*/
private int __columns;

/**
Cache for date/times for irregular time series.
This contains all date/times that overlap one or more time series.
When displaying more than one time series, requesting a date/time from a time series that
did not have the value will return an empty cell.
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
The list of prototype DateTime objects to use for getValue() calls.
This ensures that the timezone is consistent with the time series.
All other values will be set before calling getValue().
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
Whether to use the TS extended legend as the TS's column title.  If false, the TS normal legend will be used.
*/
private boolean __useExtendedLegend = false;

/**
Constructor.  This builds the Model for displaying the given TS data and pre-calculates and caches every 50th row's date.
@param data List of TS to graph in the table.
The TS must have the same data interval and data units, but this will not be checked in the table model;
it should have been done previously.
@param start the first day of data to display in the table.
@param intervalBase the TS data interval (from TimeInterval.*)
@param intervalMult the TS data multiplier.
@param dateFormat the format in which to display the date column (column 0).
@param dataFormats the formats in which to display the data columns (columns 1 through N).
The format for data column N should be at array position N-1.
@param useExtendedLegend whether to use the extended TS legend for the TS column title, or the normal legend.
This is determined by the value of the propvalue "Table.UseExtendedLegend" passed into the TSViewJFrame.
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
@param data list of TS to graph in the table.  The TS must have the same data interval and data units,
but this will not be checked in the table model; it should have been done previously.
@param start the first day of data to display in the table.
@param intervalBase the TS data interval (from TimeInterval.*)
@param intervalMult the TS data multiplier.
@param dateFormat the format in which to display the date column (column 0).
@param dataFormats the formats in which to display the data columns (columns 1 through N).
The format for data column N should be at array position N-1.
@param useExtendedLegend whether to use the extended TS legend for the TS column title, or the normal legend.
This is determined by the value of the propvalue "Table.UseExtendedLegend" passed into the TSViewJFrame.
@param cacheInterval the interval of dates to pre-calculate and cache.
Every Nth date in the entire table, where N == cacheInterval, will be pre-calculated and cached to improve performance.
The other constructor passes in a value of 50 for the interval,
and this value has been found to be adequate for most table needs.
It takes some experimenting to find the optimal value where speed is most increased but not too much memory is used.<p>
It is recommended that if a table will display at most X rows at once, that the cacheInterval be no less than X*2.
@throws Exception if an invalid data or dmi was passed in.
*/
public TSViewTable_TableModel(List<TS> data, DateTime start,
int intervalBase, int intervalMult, int dateFormat, String[] dataFormats,
boolean useExtendedLegend, int cacheInterval )
throws Exception {
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
	    TS ts = data.get(0);
	    DateTime d = ts.getDate1();
	    if ( d == null ) {
	        d = ts.getDate1Original();
	    }
	    if ( d != null ) {
	        __irregularDateTimePrecision = d.getPrecision();
	    }
	    // Determine if all the time series have a consistent time zone.
	    // If yes, the time zone will be displayed in the date/time column.  If no, the time zone is removed.
	    // Column tool-tips include the time zone.
	    // Also save a prototype DateTime for each time series that matches Date1,
	    // which will be used for getValue() to make sure the time zone is handled.
	    __irregularTZSame = true;
	    __irregularTZ = null;
	    __irregularPrototypeDateTime = new DateTime[__columns - 1];
	    if ( (__irregularDateTimePrecision == DateTime.PRECISION_MINUTE) ||
	        (__irregularDateTimePrecision == DateTime.PRECISION_HOUR) ) {
    	    for ( int i = 1; i < __columns; i++ ) {
    	        ts = _data.get(i - 1);
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
	    // Cache for irregular time series is handled differently.
	    // Do this AFTER doing the time zone comparison above.
        createIrregularTSDateTimeCache ();
        __intervalMult = -1; // Should not be used for irregular.
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
		    _rows = TSUtil.calculateDataSize(data.get(0), __start, end);
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

	// Check whether time zones are the same, to help with table labeling.
	this.areTimeZonesSame = TSUtil.areTimeZonesSame(data);

	// Adjust columns if the row is being shown for troubleshooting.
    if ( __showRow ) {
        ++__columns;
    }
}

/**
Create a cache of date/times for irregular time series (all date/times that occur).
Requesting a date/time from an irregular time series will either return the matching data or blanks will be shown.
Each time series is iterated in parallel to find the list of date/times.
If the time zone for the time series is different, it is not shown in the date/time column but is indicated in the column heading.
The worksheet row=0 will correspond to the first date/time.
*/
private void createIrregularTSDateTimeCache ()
throws TSException {
    String routine = getClass().getSimpleName() + ".createIrregularTSDateTimeCache";
    // Use this when troubleshooting during development, not intenced for real-time troubleshooting.
    boolean debug = false;
    // More than one irregular time series.  They at least have to have the same date/time precision for the period.
	// Otherwise it will be difficult to navigate the data.
    int irrPrecision = __irregularDateTimePrecision;
    int tsPrecision;
    List<TS> tslist = _data;
    
    int size = tslist.size();
    IrregularTS ts;
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
            // Treat as minute precision.
            tsPrecision = DateTime.PRECISION_MINUTE;
        }
        if ( irrPrecision == -1 ) {
            // Just assign.
            irrPrecision = tsPrecision;
        }
        else if ( irrPrecision != tsPrecision ) {
            // This will be a problem in processing the data.
            String message = "Irregular time series do not have the same date/time precision by checking period start and end.  Can't display";
            Message.printWarning ( 2, routine, message );
            throw new UnequalTimeIntervalException ( message );
        }
    }
    // Was able to determine the precision of data so can continue.
    // The logic works as follows:
    //
    // 0) Advance the iterator for each time series to initialize
    // 1) Find the earliest date/time in the iterator current position
    // 2) Add cached date/times that will result in:
    //    - actual value if time series has a value at the date/time
    //    - values not at the same date/time result in blanks for the other time series
    // 3) For any values that will be output, advance that time series' iterator
    // 4) Go to step 1
    //
    // Create iterators for each time series.
    TSIterator [] tsIteratorArray = new TSIterator[tslist.size()];
    __irregularDateTimeCache = new ArrayList<>();
    for ( int its = 0; its < size; its++ ) {
        if ( tslist.get(its) == null ) {
            // Null time series.  Create a null iterator to keep the same order as time series.
            tsIteratorArray[its] = null;
        }
        else {
        	// Have a non-null time series:
        	// - will be an IrregularTS
        	// - get its iterator
        	ts = (IrregularTS)tslist.get(its);
        	try {
        		// Get the iterator:
        		// - currently, iterate through full period
        		// - will be an IrregularTSIterator
            	tsIteratorArray[its] = ts.iterator();
        	}
        	catch ( Exception e ) {
            	// Error getting an iterator.  Create a null iterator to keep the same order as time series.
            	tsIteratorArray[its] = null;
        	}
        }
    }

    int its;
    TSIterator itsIterator;
    // Use the following to extract dates from each time series.
    // A call to the iterator next() method will return null when no more data, which is the safest way to process the data.
    TSData [] tsdata = new TSData[tslist.size()];
    TSData tsdataMin = null; // Used to find the minimum date/time for all the iterators.
    DateTime dtMin = null; // Used to compare date/times for all the iterators.
    DateTime dtCached = null; // Cached date/time.
    int iteratorMin = -1;
    int loopCount = 0;
    while ( true ) {
        // Using the current date/time, output the earliest value for all time series that have the value and
        // increment the iterator for each value that is output.
        ++loopCount;
        
        // Initialize the iterators to the first data value.
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
        //
        // 1) Find the earliest date/time for each iterator.
        // 2) Add to the cache.
        // 3) Advance the iterator(s) that have a date/time value matching the earliest value from step 1.
        //
        // Do this until all iterators next() have returned null.
        // This allows time series with different numbers of date/times to be fully processed.
        
        // Initialize the minimum TSData and DateTime for this iteration.
        tsdataMin = null;
        dtMin = null;
        for ( its = 0; its < size; its++ ) {
            if ( tsdataMin == null ) {
                // Initialize the earliest date/time to the first non-null value.
                if ( tsdata[its] != null ) {
                    tsdataMin = tsdata[its];
                    dtMin = tsdataMin.getDate();
                    iteratorMin = its;
                    //Message.printStatus(2, routine, "Initializing minimum date/time to " + dtMin );
                }
            }
            else {
                // Have a non-null first date/time to compare to so check this iterator against it.
                // The lessThan() method DOES NOT compare time zone in any case.
                if ( (tsdata[its] != null) && tsdata[its].getDate().lessThan(dtMin) ) {
                    // Have found an earlier date/time:
                    // - note that time zone is NOT checked
                	// - save the minimum values
                    tsdataMin = tsdata[its];
                    dtMin = tsdataMin.getDate();
                    iteratorMin = its;
                    //Message.printStatus(2, routine, "Found earlier minimum date/time [" + its + "] " + dtMin );
                }
            }
        }

        // If the next minimum date/time is null, all date/times from all iterators have been exhausted so done processing.
        if ( dtMin == null ) {
            // Done processing all data.
            break;
        }

        // 2) Add the minimum date/time from above to the cache.

        // Create a new DateTime instance so that it is independent of any data manipulations that may occur on the time series.
        // Create a fast instance since it will be used for iteration and data access but not be manipulated or checked.
        dtCached = new DateTime(dtMin,DateTime.DATE_FAST);
        if ( !__irregularTZSame ) {
            // Set the timezone to blank for cached date/times rather than showing wrong time zone that might be misinterpreted.
            dtCached.setTimeZone("");
        }
        __irregularDateTimeCache.add(dtCached);
        if ( debug ) {
        	Message.printStatus(2,routine,"Irregular TS date/time cache, row [" + (__irregularDateTimeCache.size() - 1) + "] = " + dtCached);
        }

        // 3) Advance the iterator for the one with the minimum date/time and all with the same date/time.
        // Note - time zone is NOT checked by equals().
        for ( its = 0; its < size; its++ ) {
            // First check below increases performance a bit.
            // Use the prototype date if available to ensure that time zone is handled.
        	// TODO smalers 2023-07-10 but the following is not checking time zone?
            if ( (__irregularPrototypeDateTime != null) && (__irregularPrototypeDateTime[its] != null) ) {
                // Use the prototype DateTime (which has proper time zone) and overwrite the specific date/time values.
                // Do not call setDate() because it will set whether to use time zone and defeat the purpose of the prototype.
                __irregularPrototypeDateTime[its].setYear(dtMin.getYear());
                __irregularPrototypeDateTime[its].setMonth(dtMin.getMonth());
                __irregularPrototypeDateTime[its].setDay(dtMin.getDay());
                __irregularPrototypeDateTime[its].setHour(dtMin.getHour());
                __irregularPrototypeDateTime[its].setMinute(dtMin.getMinute());
                __irregularPrototypeDateTime[its].setSecond(dtMin.getSecond());
                dtMin = __irregularPrototypeDateTime[its];
            }
            if ( (iteratorMin == its) // Know that the matched iterator can be advanced.
            	|| ((tsdata[its] != null) && dtMin.equals(tsdata[its].getDate())) ) { // Other iterator with the same date/time.
                tsdata[its] = tsIteratorArray[its].next();
                if ( debug ) {
                	Message.printStatus(2, routine, "Advanced iterator[" + its + "] date/time to " + tsdata[its] );
                }
            }
        }
    }
    // Set the number of rows.
    _rows = __irregularDateTimeCache.size();
    if ( _rows > 0 ) {
        //Message.printStatus(2, routine, "Number of cached irregular date/times=" + _rows +
        //    " first date/time=" + __irregularDateTimeCache.get(0) +
        //    " last date/time=" + __irregularDateTimeCache.get(_rows - 1) );
    }
    if ( debug ) {
    	// Dump out the date/time values column values for the table model, for troubleshooting the code.
   	    Message.printStatus(2,routine, "All irregular time series date/times:");
    	for ( int row = 0; row < _rows; row++ ) {
    	    Message.printStatus(2,routine + "2","Row [" + row + "] " + getValueAtIrregular(row, 0));
    	}
    }
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	if ( columnIndex == 0 ) {
	    return String.class; // Date/Time.
	}
    else if ( __showRow && (columnIndex == (__columns - 1)) ) {
        return String.class;
    }
	else {
		// TS data.
	    // If data flags are superscripted, return a String.
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
For column 0, the name will be DATE or DATE/TIME depending on the date/time precision.
For time series the string will be alias (or location), sequence number, data type, units.
If a time series property TableViewHeaderFormat is set, then this format will be used to format the string.
The format can contain % and ${ts:Property} specifiers.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
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

	// Otherwise the column names depends on time series properties.
    TS ts = _data.get(columnIndex - 1);

    Object propVal = ts.getProperty("TableViewHeaderFormat");
    if ( (propVal != null) && !propVal.equals("") ) {
        String format = "" + propVal;
        return ts.formatLegend(format);
    }

	// The following are expensive String operations (concatenations, etc),
    // but this method is not called very often (just once when the table is first displayed?) so this shouldn't be a problem.

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
		if ( !ts.getDataUnits().isEmpty() ) {
			// Have units so include in the header.
			unitsString = " (" + ts.getDataUnits() + ")";
		}
		if ( !ts.getDataType().isEmpty() ) {
			// Have the data type so include in the header.
			datatypeString = ", " + ts.getDataType();
		}
		if ( !ts.getSequenceID().isEmpty() ) {
			// Have a sequence identifier for a trace so include in the header.
			sequenceString = " [" + ts.getSequenceID() + "]";
		}
		String columnName = null;
		if ( ts.getAlias().isEmpty() ) {
			// Don't have the alias so use the time series identifier location in the header.
			columnName = ts.getLocation() + sequenceString + datatypeString + unitsString;
		}
		else {
			// Use the alias in the header.
			columnName = ts.getAlias() + sequenceString + datatypeString + unitsString;
		}
		if ( !this.areTimeZonesSame ) {
			// If the time zone is used in both time series and is different, show it in the data value column heading.
			if ( ts.getDate1().getTimeZoneAbbreviation().isEmpty() ) {
				columnName += ", NO-TZ";
			}
			else {
				columnName += ", " + ts.getDate1().getTimeZoneAbbreviation();
			}
		}
		return columnName;
	}
}

/**
Returns an array containing the column tool tips.
@return an array containing the column tool tips.
*/
public String[] getColumnToolTips() {
    String[] tt = new String[__columns];
    TS ts;
    StringBuilder sb = new StringBuilder (
        "<html>The DATE or DATE/TIME is formatted according to the precision of date/times for the time series." );
    if ( this.areTimeZonesSame ) {
        sb.append("<br>The time zone for the date/time column is included " +
        	"because all time series have the same time zone.");
    }
    else {
        // Time zones are not equal and therefore time zone is set to blank for the date/time cache.
        sb.append("<br>The time zone for the date/time column has been removed because the time zone is different for the time series." );
        sb.append("<br>See column heading and tool tips for each time zone.");
        sb.append("<br>If desired, display a table for only time series to see the time zone.");
    }
    sb.append("</html>");
    tt[0] = sb.toString();
    int iend = __columns;
    if ( __showRow ) {
        --iend;
    }
    for ( int i = 1; i < iend; i++ ) {
        ts = _data.get(i - 1);
        sb = new StringBuilder(
            "<htmL>TSID = " + ts.getIdentifierString() + "<br>" +
            "Alias = " + ts.getAlias() + "<br>" +
            "Description = " + ts.getDescription() + "<br>" +
            "Units = " + ts.getDataUnits() );
        boolean includeZone = false;
        // Determine whether time zone is relevant.
        if ( ts.isRegularInterval() ) {
        	if ( this.__intervalBase <= TimeInterval.HOUR ) {
        		includeZone = true;
        	}
        }
        else {
        	// Have to parse the interval.
        	TimeInterval interval = TimeInterval.parseInterval(ts.getIdentifier().getInterval());
        	int precision = interval.getIrregularIntervalPrecision();
        	if ( (precision != TimeInterval.UNKNOWN) && (precision <= TimeInterval.HOUR) ) {
        		includeZone = true;
        	}
        }
        if ( includeZone ) {
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
Consecutive reads optimize performance when reads are typically done in sequence.
@param row row from which to return a value.
@param col column from which to return a value.
@return the value at the specified row and column.
*/
public Object getConsecutiveValueAt(int row, int col) {
    if ( this.__intervalBase == TimeInterval.IRREGULAR ) {
        // Irregular data have all the date/times cached consistent with rows so handle specifically.
        return getValueAtIrregular(row,col);
    }

	if (shouldResetGetConsecutiveValueAt()) {
		shouldResetGetConsecutiveValueAt(false);
		this.__priorRow = -1;
	}
	
	if (this.__priorRow == -1) {
		int precision = this.__cachedDates[row / this.__cacheInterval].getPrecision();
		// Control whether time zone is shown.
		if ( !this.areTimeZonesSame ) {
			// Time zone for time series are different:
			// - the time zone is NOT included in the date/time column value
			// - show the time zone in the in the column heading and tooltip
			precision |= DateTime.PRECISION_NO_TIME_ZONE;
		}

		DateTime temp = new DateTime( this.__cachedDates[row / this.__cacheInterval], precision);

		if (this.__intervalBase == TimeInterval.MINUTE) {
			temp.addMinute((row % this.__cacheInterval) * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.HOUR) {
			temp.addHour((row % this.__cacheInterval) * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.DAY) {
			temp.addDay((row % this.__cacheInterval) * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.MONTH) {
			temp.addMonth((row % this.__cacheInterval) * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.YEAR) {
			temp.addYear((row % this.__cacheInterval) * this.__intervalMult);
		}
		this.__priorDateTime = temp;
		this.__priorRow = row;
	}
	else if (this.__priorRow != row) {
		if (this.__intervalBase == TimeInterval.MINUTE) {
			this.__priorDateTime.addMinute(1 * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.HOUR) {
			this.__priorDateTime.addHour(1 * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.DAY) {
			this.__priorDateTime.addDay(1 * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.MONTH) {
			this.__priorDateTime.addMonth(1 * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.YEAR) {
			this.__priorDateTime.addYear(1 * this.__intervalMult);
		}
		this.__priorRow = row;
	}

	if (col > 0) {
		TS ts = this._data.get(col-1);
		return Double.valueOf(ts.getDataValue(this.__priorDateTime));
	}
	else {
		return this.__priorDateTime;
	}
}

/**
Returns the total number of characters in a DateTime object formatted with __dateFormat.
@return the total number of characters in a DateTime object formatted with __dateFormat.
*/
private int getDateFormatLength() {
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
Returns the format that the specified column should be displayed in when the table is being displayed in the given table format.
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
	        // Data value is represented as string with flag as superscript.
	        return "%s";
	    }
	    else {
	        // Not displaying data flags so format number.
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
Returns the interval multiplier for the time series.
@return the interval multiplier for the time series.
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
public TS getTS ( int i ) {
	return _data.get(i);
}

/**
Returns the time series.
@return the list of time series.
*/
public List<TS> getTSList() {
	return _data;
}

/**
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and column.
*/
public Object getValueAt(int row, int col) {
    if ( this.__intervalBase == TimeInterval.IRREGULAR ) {
        // Irregular data have all the date/times cached consistent with rows so handle specifically.
        return getValueAtIrregular(row,col);
    }
    
    // Below is for regular interval.

	if (shouldDoGetConsecutiveValueAt()) {
		// Do a consecutive get value at rather than this sequential one.
		return getConsecutiveValueAt(row, col);
	}

	double y = this.__worksheet.getVisibleRect().getY();

	// If it's a new Y point from the last time getValueAt was called,
	// then that means some scrolling has occurred and the top-most row is new.
	// Need to recalculate the date of the top most row.

	if (this.__previousTopmostVisibleY != y) {
		this.__previousTopmostVisibleY = y;
		this.__firstVisibleRow = this.__worksheet.rowAtPoint(new Point(0,(int)y));

		// Calculate its date time by looking up the nearest cached one and adding the remainder of intervals to it.
		this.__firstVisibleRowDate = new DateTime( this.__cachedDates[this.__firstVisibleRow / this.__cacheInterval]);
		int precision = 0;
		if (this.__intervalBase == TimeInterval.MINUTE) {
			precision = DateTime.PRECISION_MINUTE;
			this.__firstVisibleRowDate.addMinute( (this.__firstVisibleRow % this.__cacheInterval) * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.HOUR) {
			precision = DateTime.PRECISION_HOUR;
			this.__firstVisibleRowDate.addHour( (this.__firstVisibleRow % this.__cacheInterval) * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.DAY) {
			precision = DateTime.PRECISION_DAY;
			this.__firstVisibleRowDate.addDay( (this.__firstVisibleRow % this.__cacheInterval) * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.MONTH) {
			precision = DateTime.PRECISION_MONTH;
			this.__firstVisibleRowDate.addMonth( (this.__firstVisibleRow % this.__cacheInterval) * this.__intervalMult);
		}
		else if (this.__intervalBase == TimeInterval.YEAR) {
			precision = DateTime.PRECISION_YEAR;
			this.__firstVisibleRowDate.addYear( (this.__firstVisibleRow % this.__cacheInterval) * this.__intervalMult);
		}

		if ( !this.areTimeZonesSame ) {
			// Time zone for time series are different:
			// - the time zone is NOT included in the date/time column value
			// - show the time zone in the in the column heading and tooltip
			precision |= DateTime.PRECISION_NO_TIME_ZONE;
		}

		this.__workingDate = new DateTime(this.__firstVisibleRowDate, DateTime.DATE_FAST | precision);
		
		// Reset this so that on a scroll event none of the rows are drawn incorrectly.
		// Removing this line will result in a "scrambled"-looking JTable.
		this.__lastRowRead = -1;
	}

	if (this._sortOrder != null) {
		row = this._sortOrder[row];
	}

	// The getValueAt function is called row-by-row when a worksheet displays its data,
	// so the current working date (with which data for the current row is read)
	// only needs to be recalculated when a new row is moved to.
	if (row != this.__lastRowRead) {
		this.__lastRowRead = row;

		// Quicker than doing a 'new DateTime'.
		this.__workingDate.setHSecond ( this.__firstVisibleRowDate.getHSecond() );
		this.__workingDate.setSecond ( this.__firstVisibleRowDate.getSecond() );
		this.__workingDate.setMinute ( this.__firstVisibleRowDate.getMinute() );
		this.__workingDate.setHour ( this.__firstVisibleRowDate.getHour() );
		this.__workingDate.setDay ( this.__firstVisibleRowDate.getDay() );
		this.__workingDate.setMonth ( this.__firstVisibleRowDate.getMonth() );
		this.__workingDate.setYear ( this.__firstVisibleRowDate.getYear() );

		// Calculate the date for the current row read.
		if (this.__intervalBase == TimeInterval.MINUTE) {
			this.__workingDate.addMinute(((row - this.__firstVisibleRow)* this.__intervalMult));
		}
		else if (this.__intervalBase == TimeInterval.HOUR) {
			this.__workingDate.addHour(((row - this.__firstVisibleRow)* this.__intervalMult));
		}
		else if (this.__intervalBase == TimeInterval.DAY) {
			this.__workingDate.addDay(((row - this.__firstVisibleRow)* this.__intervalMult));
		}
		else if (this.__intervalBase == TimeInterval.MONTH) {
			this.__workingDate.addMonth(((row - this.__firstVisibleRow)* this.__intervalMult));
		}
		else if (this.__intervalBase == TimeInterval.YEAR) {
			this.__workingDate.addYear(((row - this.__firstVisibleRow)* this.__intervalMult));
		}
	}

	if (col == 0) {
		// Date/time column.
		return __workingDate.toString();
	}
	else {
		// Data value column.
		TS ts = this._data.get(col - 1);
		if ( this.__dataFlagVisualizationType == TSDataFlagVisualizationType.SUPERSCRIPT ) {
	    	return getValueAtFormatValueWithFlag(ts, this.__workingDate, this.__dataFormats[col-1]);
		}
		else {
	    	return Double.valueOf(ts.getDataValue(this.__workingDate));
		}
	}
}

/**
Format the data value with the flag.
@param ts time series with data
@param dt date/time corresponding to data
@param dataFormat data format to use
*/
private String getValueAtFormatValueWithFlag ( TS ts, DateTime dt, String dataFormat ) {
    TSData tsdata = ts.getDataPoint(dt, null);
    double value = tsdata.getDataValue();
    String flag = tsdata.getDataFlag();
    if ( flag == null ) {
        flag = "";
    }
    if ( ts.isDataMissing(value) ) {
        if ( flag.equals("") ) {
            // No value, no flag.
            return "";
        }
        else {
            // No value but have flag.
            return "^" + flag;
        }
    }
    else {
        // TODO SAM 2012-04-16 Figure out formatting for value.
        if ( flag.equals("") ) {
            // Value but no flag.
            return "" + StringUtil.formatString(value,dataFormat);
        }
        else {
            // Value and flag.
            return "" + StringUtil.formatString(value,dataFormat) + "^" + flag;
        }
    }
}

/**
Returns the data that should be placed in the JTable at the given row and column, for irregular time series.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and column.
*/
public Object getValueAtIrregular(int row, int col) {
    if (_sortOrder != null) {
        row = _sortOrder[row];
    }

    // Based on the row, determine the date/time for the data.
    DateTime dt = __irregularDateTimeCache.get(row);
    double value;
    if ( col == 0 ) {
        // Returning the date.
        return dt.toString();
    }
    else if ( __showRow && (col == (__columns - 1)) ) {
    	// Row number.
        return "" + row;
    }
    else {
        // Returning the time series data value.
        TS ts = _data.get(col - 1);
        if ( (__irregularPrototypeDateTime != null) && (__irregularPrototypeDateTime[col - 1] != null) ) {
            // Use the prototype DateTime (which has proper time zone for the time series)
            // and overwrite the specific date/time values.
            // Do not call setDate() because it will set whether to use time zone and defeat the purpose of the prototype.
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
            return Double.valueOf(value);
        }
    }
}

/**
Returns an array containing the widths (in number of characters) that the fields in the table should be sized to.
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
Returns whether the cell is editable or not.  Currently always returns false.
@param rowIndex unused.
@param columnIndex unused.
@return whether the cell is editable or not.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (columnIndex > 0) {
		// TODO (JTS - 2004-01-22) no editing supported yet.
		return false;
		/*
		if ( __dataFlagVisualizationType != TSDataFlagVisualizationType.NOT_SHOWN) {
    		// FIXME SAM (2010-07-15) Figure this out - we added some editing.
    		TS ts = _data.get(columnIndex - 1);
    		return ts.isEditable();
		}
		else {
		    // TODO SAM 2012-04-16 Editing when flags are shown is not yet implemented.
		    return false;
		}
		*/
	}
	return false;
}

/**
Set how data flags should be visualized.
*/
public void setDataFlagVisualizationType ( TSDataFlagVisualizationType dataFlagVisualizationType ) {
    __dataFlagVisualizationType = dataFlagVisualizationType;
}

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value.
@param col the column of the cell for which to set the value.
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

	TS ts = _data.get(col-1);

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
		ts.setDataValue(d, Double.valueOf((String)value).doubleValue());
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
Sets up the table model to prepare for a consecutive read.
For more information see the JWorksheet Javadoc about consecutive reads.
*/
public void startNewConsecutiveRead() {
	this.__priorRow = -1;
	this.__priorDateTime = null;
}

}