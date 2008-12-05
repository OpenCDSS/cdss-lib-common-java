// ----------------------------------------------------------------------------
// TSSupplier - provide basic I/O interface to supply time series
// ----------------------------------------------------------------------------
// History:
//
// 2001-11-07	Steven A. Malers, RTi	Implemented code.
// 2002-04-25	SAM, RTi		Remove some methods - try to make
//					the interface as simple as possible.
//					Add getTSSupplierName().
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.util.List;

import RTi.Util.Time.DateTime;

/**
The TSSupplier interface should be implemented by classes that are supplying
time series information to higher level code.  For example, a class can be
written to read and write time series from a custom database.  The higher-level
class should include a method addTSSupplier() that makes the interface class
known to the higher level code.  This interface should be used in conjunction
with the TSReceiver and TSIdentSupplier interfaces.
*/
public abstract interface TSSupplier
{
/**
Return the name of the TSSupplier.  This is used for messages.
*/
public String getTSSupplierName();

/**
Read a time series given a time series identifier string.  The string may be
a file name if the time series are stored in files, or may be a true identifier
string if the time series is stored in a database.  The specified period is
read.  The data are converted to the requested units.
@param tsident_string Time series identifier or file name to read.
@param date1 First date to query.  If specified as null the entire period will
be read.
@param date2 Last date to query.  If specified as null the entire period will
be read.
@param req_units Requested units to return data.  If specified as null or an
empty string the units will not be converted.
@param read_data if true, the data will be read.  If false, only the time series
header will be read.
@return Time series of appropriate type (e.g., MonthTS, HourTS).
@exception Exception if an error occurs during the read.
*/
public abstract TS readTimeSeries (	String tsident_string,
					DateTime date1, DateTime date2,
					String req_units,
					boolean read_data )
throws Exception;

/**
Read a time series given an existing time series and a file name.
The specified period is read.
The data are converted to the requested units.
@param req_ts Requested time series to fill.  If null, return a new time series.
If not null, all data are reset, except for the identifier, which is assumed
to have been set in the calling code.  This can be used to query a single
time series from a file that contains multiple time series.
@param fname File name to read.
@param date1 First date to query.  If specified as null the entire period will
be read.
@param date2 Last date to query.  If specified as null the entire period will
be read.
@param req_units Requested units to return data.  If specified as null or an
empty string the units will not be converted.
@param read_data if true, the data will be read.  If false, only the time series
header will be read.
@return Time series of appropriate type (e.g., MonthTS, HourTS).
@exception Exception if an error occurs during the read.
*/
public abstract TS readTimeSeries (	TS req_ts, String fname,
					DateTime date1, DateTime date2,
					String req_units,
					boolean read_data )
throws Exception;

/**
Read a time series list from a file (this is typically used used where a time
series file can contain one or more time series).
The specified period is
read.  The data are converted to the requested units.
@param fname File to read.
@param date1 First date to query.  If specified as null the entire period will
be read.
@param date2 Last date to query.  If specified as null the entire period will
be read.
@param req_units Requested units to return data.  If specified as null or an
empty string the units will not be converted.
@param read_data if true, the data will be read.  If false, only the time series
header will be read.
@return List of time series of appropriate type (e.g., MonthTS, HourTS).
@exception Exception if an error occurs during the read.
*/
public abstract List readTimeSeriesList ( String fname,
						DateTime date1, DateTime date2,
						String req_units,
						boolean read_data )
throws Exception;

/**
Read a time series list from a file or database using the time series identifier
information as a query pattern.
The specified period is
read.  The data are converted to the requested units.
@param tsident A TSIdent instance that indicates which time series to query.
If the identifier parts are empty, they will be ignored in the selection.  If
set to "*", then any time series identifier matching the field will be selected.
If set to a literal string, the identifier field must match exactly to be
selected.
@param fname File to read.
@param date1 First date to query.  If specified as null the entire period will
be read.
@param date2 Last date to query.  If specified as null the entire period will
be read.
@param req_units Requested units to return data.  If specified as null or an
empty string the units will not be converted.
@param read_data if true, the data will be read.  If false, only the time series
header will be read.
@return List of time series of appropriate type (e.g., MonthTS, HourTS).
@exception Exception if an error occurs during the read.
*/
public abstract List readTimeSeriesList ( TSIdent tsident, String fname,
						DateTime date1, DateTime date2,
						String req_units,
						boolean read_data )
throws Exception;

}