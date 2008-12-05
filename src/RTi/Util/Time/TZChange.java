//------------------------------------------------------------------------------
// TZChange - time-zone change information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2001-12-19	Steven A. Malers, RTi	Initial version.
//------------------------------------------------------------------------------

package	RTi.Util.Time;

/**
The TZChange class provides storage for a time zone change data (dates when
time zones change from standard to daylight savings) for the USA.  An array of
data is maintained in the TZ class.  TZChange typically only need to be
accessed from the TZ class.  Note that there are many exceptions to these
general parameters.  In order to provide complete support, it is likely that
a database of time zone parameters would need to be read or a more complex way
to add time zone change rules needs to be added.
The time changes occur at 2AM.
@see TZ 
*/
public class TZChange
{

/**
Year of interest.
*/
public int year;

/**
Day in April when the time changes from standard to daylight savings time.
*/
public int apr_tods;

/**
Day in October when the time changes from daylight savings to standard time.
*/
public int oct_tost;

/**
Construct using the individual data items.  See the description of public data
for more information.
@param y Year.
@param a_tods Day in April to switch to daylight savings time.
@param o_tost Day in October to switch to standard time.
*/
public TZChange ( int y, int a_tods, int o_tost )
{	year = y;
	apr_tods = a_tods;
	oct_tost = o_tost;
}

}