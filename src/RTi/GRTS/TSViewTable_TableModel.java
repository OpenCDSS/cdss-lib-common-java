// ----------------------------------------------------------------------------
// TSViewTable_TableModel - Table model for displaying regular TS data
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-07-10	J. Thomas Sapienza, RTi	Initial version.
// 2003-07-11	JTS, RTi		Worked on caching model to speed
//					up large table performance.
// 2003-07-16	JTS, RTi		Expanded table model to work with more
//					data than Day alone.
// 2003-07-21	JTS, RTi		* Javadoc'd, cleaned up.
//					* Revised following SAM's review.
// 2003-08-14	JTS, RTi		Corrected a bug that was resulting in
//					the cached dates being incorrectly
//					calculated if the time series interval
//					was greater than 1.
// 2003-11-04	JTS, RTi		* Added valid setValueAt() code.
//					* Added isCellEditable() code.
// 2003-12-16	JTS, RTi		Corrected bug that caused the number of
//					rows in the table model to only be
//					calculated from the first TS's size.
// 2004-02-18	JTS, RTi		Greatly increased performance in the
//					getValueAt() method.
// 2005-04-27	JTS, RTi		Added finalize().
// 2005-10-20	JTS, RTi		Corrected the getConsecutiveValueAt()
//					code, which was not working properly.
// 2006-01-10	JTS, RTi		Corrected a bug that was resulting in
//					a scrambled-looking worksheet when the
//					worksheet was scrolled downward before
//					any cells were selected.  Caused by 
//					not setting the __lastRowRead variable 
//					in getValueAt().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GRTS;

import java.awt.Point;

import RTi.Util.GUI.JWorksheet;

import java.util.List;

import RTi.TS.TS;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

import RTi.Util.IO.IOUtil;

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
current scroll position is calculated from the date of the top-most visible 
row.<p>
<b>Notes</b>
These caching steps may seem overkill, but JTS found during extensive testing
that they increase the speed of browsing through a table of time series 
dramatically.  Without the caching, scrolling to the end of a long time series
can take many seconds, whereas it is nearly instant with the steps taken here.
*/
public class TSViewTable_TableModel 
extends JWorksheet_AbstractRowTableModel {

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
The working date time from which dates and times for data read from the
worksheet are calculated.
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
DateTime from the entire table, where X == __cacheInterval, will be 
pre-calculated and cached.
*/
private int __cacheInterval;

/**
Number of columns in the table model.
*/
private int __columns;

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
*/
private String[] __dataFormats;

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
public TSViewTable_TableModel(List data, DateTime start, 
int intervalBase, int intervalMult, int dateFormat, String[] dataFormats, 
boolean useExtendedLegend)
throws Exception {
	this(data, start, intervalBase, intervalMult, dateFormat, dataFormats,
		useExtendedLegend, 50);
}

/**
Constructor.  This builds the Model for displaying the given TS data.
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
@param cacheInterval the interval of dates to pre-calculate and cache.  Every
Nth date in the entire table, where N == cacheInterval, will be pre-calculated
and cached to improve performance.  The other constructor passes in a value of
50 for the interval, and this value has been found to be adequate for most
table needs.  It takes some experimenting to find the optimal value where 
speed is most increased but not too much memory is used.  <p>
JTS recommends that if a table will display at most X rows at once, that 
the cacheInterval be no less than X*2.
@throws Exception if an invalid data or dmi was passed in.
*/
public TSViewTable_TableModel(List data, DateTime start, 
int intervalBase, int intervalMult, int dateFormat, String[] dataFormats, 
boolean useExtendedLegend, int cacheInterval)
throws Exception {
	if (data == null) {
		throw new Exception ("Null data Vector passed to TSViewTable_TableModel constructor.");
	}	
	_data = data;
	__cacheInterval = cacheInterval;
	__columns = data.size() + 1;
	__intervalBase = intervalBase;
	__intervalMult = intervalMult;
	__dateFormat = dateFormat;
	__dataFormats = dataFormats;
	__useExtendedLegend = useExtendedLegend;
	__start = start;
	
	if (__columns > 1) {		
		TSLimits limits = TSUtil.getPeriodFromTS(data, TSUtil.MAX_POR);
		DateTime end = limits.getDate2();
		_rows = TSUtil.calculateDataSize((TS)data.get(0), __start, end);
	}
	
	__firstVisibleRow = 0;
	__firstVisibleRowDate = __start;
	__workingDate = new DateTime(__start);
	__cachedDates = new DateTime[(_rows / __cacheInterval) + 1];

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
	switch (columnIndex) {
		case  0: return String.class;	// Date/Time
		default: return Double.class;	// TS data
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
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case  0:	
			if ((__intervalBase == TimeInterval.HOUR) || (__intervalBase == TimeInterval.MINUTE)) {
				return "DATE/TIME";
			}
			return "DATE";
	}

	TS ts = (TS)_data.get(columnIndex - 1);

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
		if (ts.getSequenceNumber() >= 0) {
			sequenceString = " [" + ts.getSequenceNumber() + "]";
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
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case  0:	return "%" + getDateFormatLength() + "s";
		default:	return __dataFormats[column-1];
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
Returns the number of rows of data in the table.
@return the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns the time series.
@return the Vector of time series.
*/
public List getTSList() {	
	return _data;
}

/**
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (shouldDoGetConsecutiveValueAt()) {
		// do a consecutive get value at rather than this sequential one.
		return getConsecutiveValueAt(row, col);
	}

	double y = __worksheet.getVisibleRect().getY();	
	
	// if it's a new Y point from the last time getValueAt was called,
	// then that means some scrolling has occurred and the top-most row
	// is new.  Need to recalculate the date of the top most row

	if (__previousTopmostVisibleY != y) {
		__previousTopmostVisibleY = y;
		__firstVisibleRow = __worksheet.rowAtPoint(new Point(0,(int)y));

		// calculate its date time by looking up the nearest 
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

		// reset this so that on a scroll event none of the rows are
		// drawn incorrectly.  Removing this line will result in a "scrambled"-looking JTable.
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

		// quicker than doing a 'new DateTime'
		__workingDate.setHSecond ( __firstVisibleRowDate.getHSecond() );
		__workingDate.setSecond ( __firstVisibleRowDate.getSecond() );
		__workingDate.setMinute ( __firstVisibleRowDate.getMinute() );
		__workingDate.setHour ( __firstVisibleRowDate.getHour() );
		__workingDate.setDay ( __firstVisibleRowDate.getDay() );
		__workingDate.setMonth ( __firstVisibleRowDate.getMonth() );
		__workingDate.setYear ( __firstVisibleRowDate.getYear() );

		// calculate the date for the current row read
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

	// TODO (JTS - 2006-05-24)
	// It's possible that a VERY slight performance gain could be made
	// by using an array to access the time series, rather than doing a 
	// cast out of a Vector.  JTS has found that given these two statements:
	// 	- ts = (TS) vector.elementAt(i);
	//	- ts = array[i];
	// the array statement is about 4 times faster.
	TS ts = (TS)_data.get(col - 1);
	
	return new Double(ts.getDataValue(__workingDate));
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
		widths[0] = getDateFormatLength() 
			+ (int)(getDateFormatLength() / 10) + 1;
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
			// REVISIT (JTS - 2004-01-22)
			// no editing supported yet
			return false;
		}	
		TS ts = (TS)_data.get(columnIndex - 1);
		return ts.isEditable();
	}
	return false;
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
@param worksheet the worksheet in which the instance of this table model is 
used.
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
