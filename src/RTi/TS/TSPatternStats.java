// ----------------------------------------------------------------------------
// TSPatternStats - simple class to hold and manipulate statistics associated
//			with time series patterns
// ----------------------------------------------------------------------------
// History:
//
// 06 Jul 1998	Steven A. Malers, RTi	Initial version.
// 05 Oct 1998	SAM, RTi		Add pattern name so that it can be
//					printed in the output.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.TS;

import java.util.Vector;

import RTi.Util.String.StringUtil;

/**
The TSPatternStats class stores statistical information time series when
analyzed using a pattern.  An instance of this class is used, for example, by
TSUtil.fillPattern.
@see StringMonthTS
@see TSUtil#fillPattern
*/
public class TSPatternStats
{

// Data...

private TS _ts = null;			// Time series being analyzed.
private TS _pattern_ts;			// Time series used for pattern.
private int _num_indicators = 0;	// Number of rows.
private double _average[][] = null;	// Averages by pattern and month.
private double _sum[][] = null;		// Sums by pattern and month.
private int _count[][] = null;		// Count by pattern and month (number
					// of non-missing data).
private String _indicator[] = null;	// Indicator strings for rows.
private boolean _dirty = false;		// Indicates if data have been modified.

/**
Default constructor.  Initialize with missing data for the number of rows
shown (data are stored with the number of rows being equal to the number of
patterns and the columns being the number of months (12).
@param indicators Vector of indicator strings (e.g., "WET", "DRY", "AVG").
@param ts Time series to analyze.
*/
public TSPatternStats ( Vector indicators, TS ts )
{	initialize ( indicators, ts, null );
}

/**
Initialize with missing data for the number of rows shown (data are stored with
the number of rows being equal to the number of patterns and the columns being
the number of months (12).
@param indicators Vector of indicator strings (e.g., "WET", "DRY", "AVG").
@param ts Time series to analyze.
@param pattern_ts Existing pattern time series.
*/
public TSPatternStats ( Vector indicators, TS ts, TS pattern_ts )
{	initialize ( indicators, ts, pattern_ts );
}

/**
For an indicator string and the month, add a data value to the statistics...
@param indicator Indicator string corresponding to row in statistics.
@param value Data value for the indicated month.
@param month Calendar month for data value (1-12).
*/
public void add ( String indicator, double value, int month )
{	// First find the indicator row...
	if ( _ts.isDataMissing(value) ) {
		return;
	}
	int row = findRow ( indicator );
	if ( row >= 0 ) {
		// Found the row...  Add the value...
		if ( _ts.isDataMissing(_sum[row][month - 1]) ) {
			// Just reset...
			_sum[row][month - 1] = value;
		}
		else {	// Add...
			_sum[row][month - 1] += value;
		}
		_dirty = true;
		++_count[row][month - 1];
	}
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize ()
throws Throwable
{	_ts = null;
	_pattern_ts = null;
	_average = null;
	_sum = null;
	_count = null;
	_indicator = null;
	super.finalize();
}

/**
The row corresponding to the indicator or -1 if the row is not found.
@return The row corresponding to the indicator.
*/
public int findRow ( String indicator )
{	for ( int i = 0; i < _num_indicators; i++ ) {
		if ( _indicator[i].equals(indicator) ) {
			return i;
		}
	}
	return -1;
}

/**
Return a computed average value for the given indicator string and month.
@return A computed average value for the given indicator string and month.
@exception TSException if the indicator cannot be found in the pattern.
*/
public double getAverage ( String indicator, int month )
throws TSException
{	refresh();
	int _row = findRow ( indicator );
	if ( _row >= 0 ) {
		return _average[_row][month - 1];
	}
	else {	throw new TSException ( "Can't find indicator" );
	}
}

/**
Initialize the data.
@param indicators Vector of indicator strings (e.g., "WET", "DRY", "AVG").
@param ts Time series to analyze.
@param pattern_ts Existing pattern time series.
*/
private void initialize ( Vector indicators, TS ts, TS pattern_ts )
{	_num_indicators = indicators.size();
	_average = new double[_num_indicators][12];
	_sum = new double[_num_indicators][12];
	_count = new int[_num_indicators][12];
	double missing = ts.getMissing();
	_indicator = new String[_num_indicators];
	_ts = ts;
	_pattern_ts = pattern_ts;

	for ( int i = 0; i < _num_indicators; i++ ) {
		for ( int j = 0; j < 12; j++ ) {
			_sum[i][j] = missing;
			_average[i][j] = missing;
			_count[i][j] = 0;
		}
		_indicator[i] = (String)indicators.elementAt(i);
	} 
}

/**
Refresh the derived values (averages).
*/
public void refresh ()
{	if ( !_dirty ) {
		return;
	}
	for ( int i = 0; i < _num_indicators; i++ ) {
		for ( int j = 0; j < 12; j++ ) {
			if (	(_count[i][j] > 0) &&
				!_ts.isDataMissing(_sum[i][j]) ) {
				_average[i][j] = _sum[i][j]/
					(double)_count[i][j];
			}
		}
	}
	_dirty = false;
}

/**
Return a string representation of the object.
*/
public String toString ()
{	StringBuffer buffer = new StringBuffer();
	String newline = System.getProperty("line.separator");

	refresh();
	if ( _pattern_ts != null ) {
		buffer.append("Pattern statistics for \"" +
		_ts.getIdentifierString() + "\" using pattern \"" +
		_pattern_ts.getLocation() + "\"" + newline );
	}
	else {	buffer.append("Pattern statistics for \"" +
		_ts.getIdentifierString() + "\"" + newline );
	}
	buffer.append(
	"        Jan       Feb       Mar       Apr       May       Jun       Jul       Aug       Sep       Oct       Nov       Dec       Total" + newline );    
	int	total;
	double	dtotal;
	double	missing = _ts.getMissing();
	for ( int i = 0; i < _num_indicators; i++ ) {
		buffer.append ( "Indicator:  \"" + _indicator[i] + "\""  +
			newline );
		buffer.append ( "SUM: " );
		dtotal = missing;
		for ( int j = 0; j < 12; j++ ) {
			if ( !_ts.isDataMissing(_sum[i][j]) ) {
				if ( _ts.isDataMissing(dtotal) ) {
					dtotal = _sum[i][j];
				}
				else {	dtotal += _sum[i][j];
				}
			}
			buffer.append ( StringUtil.formatString(_sum[i][j],
			"%10.2f") );
		}
		buffer.append ( StringUtil.formatString(dtotal, "%10.2f") );
		buffer.append ( newline );
		buffer.append ( "NUM: " );
		total = 0;
		for ( int j = 0; j < 12; j++ ) {
			total += _count[i][j];
			buffer.append ( StringUtil.formatString(_count[i][j],
			"%10d") );
		}
		buffer.append ( StringUtil.formatString(total,"%10d") );
		buffer.append ( newline );
		dtotal = missing;
		buffer.append ( "AVE: " );
		for ( int j = 0; j < 12; j++ ) {
			if ( !_ts.isDataMissing(_average[i][j]) ) {
				if ( _ts.isDataMissing(dtotal) ) {
					dtotal = _average[i][j];
				}
				else {	dtotal += _average[i][j];
				}
			}
			buffer.append ( StringUtil.formatString(_average[i][j],
			"%10.2f") );
		}
		buffer.append ( StringUtil.formatString(dtotal, "%10.2f") );
		buffer.append ( newline );
	}

	String s = buffer.toString();
	buffer = null;
	newline = null;
	return s;
}

} // End of TSPatternStats class definition
