//------------------------------------------------------------------------------
// TZ - class to encapsulate time zone information
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 01 Oct 1997	Matthew J. Rutherford,	Created class description.
//		RTi
// 02 Jan 1998	Steven A. Malers, RTi	Clean up code a little.
// 25 Mar 1998	SAM, RTi		Add javadoc.
// 21 Jun 1998	SAM, RTi		Update to match C++, where the time zone
//					list includes a blank zone, equivalent
//					to no offset and the end of the list has
//					an offset of -999.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 30 Aug 2001	SAM, RTi		Fix clone() to be more robust.
//					Clean up javadoc.  Alphabetize methods.
//					Change RESET options to non-static since
//					they are not used outside this class.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2001-12-13	SAM, RTi		Copy TSTimeZone to this class.  Change
//					to throw Exception when information
//					cannot be found.  Change "code" in
//					method to "abbr" - easier to keep clear
//					with the time zone number.
// 2001-12-19	SAM, RTi		Use a new TZData structure that allows
//					more detail.  Remove
//					getDefinedAbbreviation().
//					Remove reset().  A time zone can either
//					be looked up in the known data or
//					defined.
// 2003-11-03	SAM, RTi		Add getMatchingDefinedTZ().  This is
//					used by the SHEF time series writer to
//					determine whether a time series time
//					zone can be matched with a SHEF time
//					zone.
// 2004-04-14	SAM, RTi		* Add getMatchingDefinedTZ() that uses
//					  the offset as input.
//					* Minor cleanup to some of the
//					  documentation for the overloaded
//					  method.
//------------------------------------------------------------------------------
// EndHeader

package	RTi.Util.Time;

import RTi.Util.Message.Message;

import java.util.ArrayList;
import java.util.List;

/**
TODO SAM 2016-03-11 need to use built-in java.util.TimeZone and Java 8 time API.
@deprecated need to use built-in java.util.TimeZone and Java 8 time API.
The TZ class stores instances of time zone information and static data used to
convert between time zones.  Most often, for storage and performance reasons,
the time zone abbreviations are
used to look up and manipulate time zones (e.g., the String abbreviations are
stored in a database or other objects, rather than an instance of TZ).  Time
zone data that are defined can be examined using the getDefined*() methods.
This class is not named TimeZone
because it would conflict with the Java class name.  Currently recognized time
zones are the standard character zones used in the United States.
<b>Note that calls to getDefined*() methods have a performance penalty because
the time zone data needs to be searched.  Code should be optimized to re-use
time zone shift data as much as possible while detecting the need to use the shifts.</b>
*/
public class TZ
{

// Time zone data for common time zones.  If anything more than basic
// information is needed, will need to add code to read from some type of time
// zone database.  Use a Vector so that new time zones can be added on the fly.
private static List<TZ> TZData = new ArrayList<TZ>(66);
static {
	TZData.add ( new TZ( "", "", 0, 0, 0 ) );
	TZData.add ( new TZ( "GMT", "Greenwich Mean Time", 0, 0, 0 ));
	TZData.add ( new TZ("UTC","Coordinated Universal Time",0,0,0 ));
	TZData.add ( new TZ( "Z", "Zulu Time", 0, 0, 0 ));

	TZData.add ( new TZ("A", "Atlantic Local Time", -240, -1, 60 ));
	TZData.add ( new TZ("AS","Atlantic Standard Time",-240, 0, 0 ));
	TZData.add ( new TZ("AST","Atlantic Standard Time",-240,0, 0 ));
	TZData.add ( new TZ("AD","Atlantic Daylight Time",-240,1, 60 ));

	TZData.add ( new TZ( "E", "Eastern Local Time", -300, -1, 60 ));
	TZData.add ( new TZ("ES","Eastern Standard Time", -300, 0, 0 ));
	TZData.add ( new TZ("EST","Eastern Standard Time",-300, 0, 0 ));
	TZData.add ( new TZ("ED","Eastern Daylight Time",-300, 1, 60 ));
	TZData.add ( new TZ("EDT","Eastern Daylight Time",-300,1, 60 ));

	TZData.add ( new TZ( "C", "Central Local Time", -360, -1, 60 ));
	TZData.add ( new TZ("CS", "Central Standard Time", -360, 0, 0));
	TZData.add ( new TZ("CST","Central Standard Time", -360, 0, 0));
	TZData.add ( new TZ("CD","Central Daylight Time",-360, 1, 60 ));
	TZData.add ( new TZ("CDT","Central Daylight Time",-360,1, 60 ));

	TZData.add ( new TZ("M", "Mountain Local Time", -420, -1, 60 ));
	TZData.add ( new TZ("MS","Mountain Standard Time",-420, 0, 0 ));
	TZData.add ( new TZ("MST","Mountain Standard Time",-420,0, 0 ));
	TZData.add ( new TZ("MD","Mountain Daylight Time",-420,1, 60 ));
	TZData.add ( new TZ("MDT","Mountain Daylight Time",-420,1,60 ));

	TZData.add ( new TZ("P", "Pacific Local Time", -480, -1, 60 ));
	TZData.add ( new TZ("PS","Pacific Standard Time",-480, 0, 0 ));
	TZData.add ( new TZ("PST","Pacific Standard Time",-480,0, 0 ));
	TZData.add ( new TZ("PD","Pacific Daylight Time",-480,1, 60 ));
	TZData.add ( new TZ("PDT","Pacific Daylight Time",-480,1,60 ));

	TZData.add ( new TZ( "Y", "Yukon Local Time", -540, -1, 60 ));
	TZData.add ( new TZ("YS", "Yukon Standard Time", -540, 0, 0 ));
	TZData.add ( new TZ("YST","Yukon Standard Time", -540, 0, 0 ));
	TZData.add ( new TZ("YD","Yukon Daylight Time", -540, 1, 60 ));
	TZData.add ( new TZ("YDT","Yukon Daylight Time",-540, 1, 60 ));

	// L, LS, LD are SHEF
	TZData.add ( new TZ( "L", "Alaska Local Time", -540, -1, 60 ));
	TZData.add ( new TZ("LS","Alaska Standard Time",-540, 0, 0 ));
	TZData.add ( new TZ("AKST","Alaska Standard Time",-540,0, 0 ));
	TZData.add ( new TZ("LD","Alaska Daylight Time",-540,1, 60 ));
	TZData.add ( new TZ("AKDT","Alaska Daylight Time",-540,1,60 ));

	// All are SHEF
	TZData.add ( new TZ( "B", "Bering Local Time", -660, -1, 60 ));
	TZData.add ( new TZ("BS","Bering Standard Time", -660, 0, 0 ));
	TZData.add ( new TZ("BD","Bering Daylight Time",-660, 1, 60 ));

	TZData.add ( new TZ( "Z0", "Z0", 0, 0, 0 ));
	TZData.add ( new TZ( "Z+1", "Z+1", 60, 0, 0 ));
	TZData.add ( new TZ( "Z+2", "Z+2", 120, 0, 0 ));
	TZData.add ( new TZ( "Z+3", "Z+3", 180, 0, 0 ));
	TZData.add ( new TZ( "Z+4", "Z+4", 240, 0, 0 ));
	TZData.add ( new TZ( "Z+5", "Z+5", 300, 0, 0 ));
	TZData.add ( new TZ( "Z+6", "Z+6", 360, 0, 0 ));
	TZData.add ( new TZ( "Z+7", "Z+7", 420, 0, 0 ));
	TZData.add ( new TZ( "Z+8", "Z+8", 480, 0, 0 ));
	TZData.add ( new TZ( "Z+9", "Z+9", 540, 0, 0 ));
	TZData.add ( new TZ( "Z+10", "Z+10", 600, 0, 0 ));
	TZData.add ( new TZ( "Z+11", "Z+11", 660, 0, 0 ));
	TZData.add ( new TZ( "Z+12", "Z+12", 720, 0, 0 ));
	TZData.add ( new TZ( "Z-1", "Z-1", -60, 0, 0 ));
	TZData.add ( new TZ( "Z-2", "Z-2", -120, 0, 0 ));
	TZData.add ( new TZ( "Z-3", "Z-3", -180, 0, 0 ));
	TZData.add ( new TZ( "Z-4", "Z-4", -240, 0, 0 ));
	TZData.add ( new TZ( "Z-5", "Z-5", -300, 0, 0 ));
	TZData.add ( new TZ( "Z-6", "Z-6", -360, 0, 0 ));
	TZData.add ( new TZ( "Z-7", "Z-7", -420, 0, 0 ));
	TZData.add ( new TZ( "Z-8", "Z-8", -480, 0, 0 ));
	TZData.add ( new TZ( "Z-9", "Z-9", -540, 0, 0 ));
	TZData.add ( new TZ( "Z-10", "Z-10", -600, 0, 0 ));
	TZData.add ( new TZ( "Z-11", "Z-11", -660, 0, 0 ));
	TZData.add ( new TZ( "Z-12", "Z-12", -720, 0, 0 ));
}

// Data to check a local time zone to see whether it is daylight savings or
// standard time.  These values apply only to the USA in general but do not
// cover special locations in the USA.  A more rigorous method of checking for
// time zone changes needs to be implemented, but there will be a performance
// penalty.
private static TZChange[] TimeChangeData = new TZChange[65];
static {TimeChangeData[0] = new TZChange ( 1976,	26,	31 );
	TimeChangeData[1] = new TZChange ( 1977,	24,	30 );
	TimeChangeData[2] = new TZChange ( 1978,	30,	29 );
	TimeChangeData[3] = new TZChange ( 1979,	29,	28 );
	TimeChangeData[4] = new TZChange ( 1980,	27,	26 );
	TimeChangeData[5] = new TZChange ( 1981,	26,	25 );
	TimeChangeData[6] = new TZChange ( 1982,	25,	31 );
	TimeChangeData[7] = new TZChange ( 1983,	24,	30 );
	TimeChangeData[8] = new TZChange ( 1984,	29,	28 );
	TimeChangeData[9] = new TZChange ( 1985,	28,	27 );
	TimeChangeData[10] = new TZChange ( 1986,	27,	26 );
	TimeChangeData[11] = new TZChange ( 1987,	5,	25 );
	TimeChangeData[12] = new TZChange ( 1988,	3,	30 );
	TimeChangeData[13] = new TZChange ( 1989,	2,	29 );
	TimeChangeData[14] = new TZChange ( 1990,	1,	28 );
	TimeChangeData[15] = new TZChange ( 1991,	7,	27 );
	TimeChangeData[16] = new TZChange ( 1992,	5,	25 );
	TimeChangeData[17] = new TZChange ( 1993,	4,	31 );
	TimeChangeData[18] = new TZChange ( 1994,	3,	30 );
	TimeChangeData[19] = new TZChange ( 1995,	2,	29 );
	TimeChangeData[20] = new TZChange ( 1996,	7,	27 );
	TimeChangeData[21] = new TZChange ( 1997,	6,	26 );
	TimeChangeData[22] = new TZChange ( 1998,	5,	25 );
	TimeChangeData[23] = new TZChange ( 1999,	4,	31 );
	TimeChangeData[24] = new TZChange ( 2000,	2,	29 );
	TimeChangeData[25] = new TZChange ( 2001,	1,	28 );
	TimeChangeData[26] = new TZChange ( 2002,	7,	27 );
	TimeChangeData[27] = new TZChange ( 2003,	6,	26 );
	TimeChangeData[28] = new TZChange ( 2004,	4,	31 );
	TimeChangeData[29] = new TZChange ( 2005,	3,	30 );
	TimeChangeData[30] = new TZChange ( 2006,	2,	29 );
	TimeChangeData[31] = new TZChange ( 2007,	1,	28 );
	TimeChangeData[32] = new TZChange ( 2008,	6,	26 );
	TimeChangeData[33] = new TZChange ( 2009,	5,	25 );
	TimeChangeData[34] = new TZChange ( 2010,	4,	31 );
	TimeChangeData[35] = new TZChange ( 2011,	3,	30 );
	TimeChangeData[36] = new TZChange ( 2012,	1,	28 );
	TimeChangeData[37] = new TZChange ( 2013,	7,	27 );
	TimeChangeData[38] = new TZChange ( 2014,	6,	26 );
	TimeChangeData[39] = new TZChange ( 2015,	5,	25 );
	TimeChangeData[40] = new TZChange ( 2016,	3,	30 );
	TimeChangeData[41] = new TZChange ( 2017,	2,	29 );
	TimeChangeData[42] = new TZChange ( 2018,	1,	28 );
	TimeChangeData[43] = new TZChange ( 2019,	7,	27 );
	TimeChangeData[44] = new TZChange ( 2020,	5,	25 );
	TimeChangeData[45] = new TZChange ( 2021,	4,	31 );
	TimeChangeData[46] = new TZChange ( 2022,	3,	30 );
	TimeChangeData[47] = new TZChange ( 2023,	2,	29 );
	TimeChangeData[48] = new TZChange ( 2024,	7,	27 );
	TimeChangeData[49] = new TZChange ( 2025,	6,	26 );
	TimeChangeData[50] = new TZChange ( 2026,	5,	25 );
	TimeChangeData[51] = new TZChange ( 2027,	4,	31 );
	TimeChangeData[52] = new TZChange ( 2028,	2,	29 );
	TimeChangeData[53] = new TZChange ( 2029,	1,	28 );
	TimeChangeData[54] = new TZChange ( 2030,	7,	27 );
	TimeChangeData[55] = new TZChange ( 2031,	6,	26 );
	TimeChangeData[56] = new TZChange ( 2032,	4,	31 );
	TimeChangeData[57] = new TZChange ( 2033,	3,	30 );
	TimeChangeData[58] = new TZChange ( 2034,	2,	29 );
	TimeChangeData[59] = new TZChange ( 2035,	1,	28 );
	TimeChangeData[60] = new TZChange ( 2036,	6,	26 );
	TimeChangeData[61] = new TZChange ( 2037,	5,	25 );
	TimeChangeData[62] = new TZChange ( 2038,	4,	31 );
	TimeChangeData[63] = new TZChange ( 2039,	3,	30 );
	TimeChangeData[64] = new TZChange ( 2040,	1,	28 );
}

/**
Time zone abbreviation (e.g., "EST").
*/
protected String _abbreviation;

/**
Time zone description (e.g., "Eastern Standard Time").
*/
protected String _description;

/**
Daylight savings flag (0 is standard [e.g., specify for "EST"], 1 if daylight
savings [e.g., specify for "EDT"], -1 if local time and daylight
savings offset from standard time should be determined at run time from the
date [e.g., specify for "E"]).
*/
protected int _dsflag;

/**
Offset from standard time in minutes when daylight savings is in effect.
Frequently this is 60.
*/
protected int _ds_offset_minutes;

/**
Time zone offset in minutes from Z for standard time (0=Z).
*/
protected int _zulu_offset_minutes;

/**
Constructor to initialize offsets to 0, no daylight savings, and empty strings
(basically an unknown time zone), suitable for general use when time zone is
not important.
*/
public TZ ()
{	init();
}

/**
Construct given a time zone abbreviation.  The abbreviation is used to look up
known information for the time zone.
@exception Exception if the time zone data cannot be determined.
*/
public TZ ( String abbreviation )
throws Exception
{	init ();
	_abbreviation = abbreviation;
	int pos = getDefinedDataPosition ( abbreviation );
	if ( pos < 0 ) {
		throw new Exception ( "Data for time zone \"" + abbreviation +
		"could not be found" );
	}
	TZ tz = (TZ)TZData.get(pos);
	_zulu_offset_minutes = tz._zulu_offset_minutes;
	_description = tz._description;
	_dsflag = tz._dsflag;
	_ds_offset_minutes = tz._ds_offset_minutes;
	tz = null;
}

/**
Construct using the individual data items.  This can be used, for example, when
a new time zone is defined.
@param abbreviation Time zone abbreviation.
@param description Description.
@param zulu_offset_minutes offset from Zulu in minutes.
@param dsflag Daylight savings.
@param ds_offset_minutes Offset for daylight savings.
*/
public TZ (	String abbreviation, String description,
		int zulu_offset_minutes, int dsflag, int ds_offset_minutes )
{	_abbreviation = abbreviation;
	_description = description;
	_zulu_offset_minutes = zulu_offset_minutes;
	_dsflag = dsflag;
	_ds_offset_minutes = ds_offset_minutes;
}

/**
Add a time zone to the defined time zones.  The time zone is added to the end
of the list.  If there is a conflict in the abbreviation (another time zone with
the same abbreviation), the second time zone will not be found.
@param tz Fully-defined TZ to add to the list of defined time zones.
REVISIT JAVADOC: see RTi.Util.Time.TZ.insertTimeZoneAt
*/
public static void addTimeZone ( TZ tz )
{	TZData.add ( tz );
}

/**
Determine the daylight savings offset for time zones that can be either
standard or daylight savings time (e.g., "A" for Atlantic Local Time).  This
method is called internally.
@return the daylight savings flag given date information.  It is assumed that
the time zone corresponding to the date can have daylight savings as described
in the TZChange data.
@param tz Time zone data.
@param date Date of interest.
@exception Exception if there is an error computing the offset.
*/
private static int calculateDSOffsetMinutes ( TZ tz, DateTime date )
throws Exception
{	// Look up using the static data
    int year = date.getYear();
    int month = date.getMonth();
    int day = date.getDay();
    int hour = date.getHour();
	if ( (year < TimeChangeData[0].year) ||	(year > TimeChangeData[TimeChangeData.length - 1].year) ) {
		throw new Exception ( "Year " + date.getYear() + " is outside known time zone data." );
	}
	int yearpos = year - TimeChangeData[0].year;
	// Else have data...
	if ( (month < 4) || (month > 10) ) {
		// Standard time (no offset)...
		return 0;
	}
	else if ( month == 4 ) {
		if ( (day == TimeChangeData[yearpos].apr_tods) && (hour >= 2) ) {
			return tz._ds_offset_minutes;
		}
		else if ( day > TimeChangeData[yearpos].apr_tods ) {
			return tz._ds_offset_minutes;
		}
		else {
            return 0;
		}
	}
	else if ( month == 10 ) {
		if ( (day == TimeChangeData[yearpos].oct_tost) && (hour >= 2) ) {
			return 0;
		}
		else if ( day > TimeChangeData[yearpos].oct_tost ) {
			return 0;
		}
		else {
            return tz._ds_offset_minutes;
		}
	}
	else {	// Months in DS...
		return tz._ds_offset_minutes;
	}
}

/**
Calculate the offset in minutes between two time zones.
@return Offset in minutes.  If either time zone is null or an empty string, 0
will be returned.
@param from_tz_abbr The original time zone abbreviation.
@param to_tz_abbr New time zone abbreviation.
@param from_date This argument is required if the time zone is a "local" time,
with daylight savings only being known from a date.  For time zones that
do not need the date, it will be ignored.  If the date is required but is
specified as null, then no adjustment for daylight savings time will occur.
<b>It is assumed that this method is typically used to convert observation
dates to a fixed time zone.  Consequently, the "from" values are measured and
the "to" time zone is fixed.  In order to completely account for daylight
savings, a "to_date" would also be required with "local" time zones.  However,
this would result in a performance hit in many cases and the calculation at
a switch between daylight savings and standard time would be iterative.
Therefore, from_date is used to check local time for both from_tz and to_tz,
resulting in an offset being incorrect by the daylight savings offset for
only roughly 2 hours per year.  If standard time is used for the "to" data,
the issue is minimized.</b>
@exception Exception if there is an error getting time zone offset information.
*/
public static int calculateOffsetMinutes (	String from_tz_abbr,
						String to_tz_abbr,
						DateTime from_date )
throws Exception
{	if ( from_tz_abbr == null || to_tz_abbr == null ){
		return 0;
	}
	if ( from_tz_abbr.equals("") ) {
		return 0;
	}
	if ( to_tz_abbr.equals("") ) {
		return 0;
	}

	int from_pos = getDefinedDataPosition ( from_tz_abbr );
	if ( from_pos == -1 ) {
		throw new Exception ( "Unable to get time zone data for \"" +
		from_tz_abbr + "\"" );
	}

	int to_pos = getDefinedDataPosition ( to_tz_abbr );
	if ( to_pos == -1 ) {
		throw new Exception ( "Unable to get time zone data for \"" +
		to_tz_abbr + "\"" );
	}

	// Now work with the raw data...

	TZ from_tz = (TZ)TZData.get(from_pos);
	TZ to_tz = (TZ)TZData.get(to_pos);
	int from_offset = from_tz._zulu_offset_minutes;
	int to_offset = to_tz._zulu_offset_minutes;

	// Adjust the offsets by daylight savings...

	int dsflag = from_tz._dsflag;
	if ( (dsflag == -1) && (from_date != null) ) {
		// Need to use the date to determine the DS flag on the fly...
		dsflag = calculateDSOffsetMinutes ( from_tz, from_date );
	}
	if ( dsflag == 1 ) {
		from_offset += from_tz._ds_offset_minutes;
	}

	dsflag = to_tz._dsflag;
	if ( (dsflag == -1) && (from_date != null) ) {
		// Need to use the date to determine the DS flag on the fly...
		dsflag = calculateDSOffsetMinutes ( to_tz, from_date );
	}
	if ( dsflag == 1 ) {
		to_offset += to_tz._ds_offset_minutes;
	}

	// Take the difference between the two.

	int diff = to_offset - from_offset;

	if ( Message.isDebugOn ) {
		Message.printDebug ( 100, "TZ.calculateDSOffsetMinutes",
		"Offset from " + from_tz_abbr + " -> " + to_tz_abbr + " is " +
		diff + " minutes." );
	}

	return diff;
}

/**
Clone the time series.  All primitive data are copied by the base class clone().
A deep copy is returned.
@return a copy of the TZ.
*/
public Object clone()
{	try {	TZ time_zone = (TZ)super.clone();
		return time_zone;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
Determine whether time zone instances are equal.  The zulu offset, daylight
savings flag, and daylight savings offset are checked (it is possible that a
time zone has more than one abbreviation, but if the numerical data are equal,
the time zones are equivalent.  The abbreviation is not checked.
@return true if the time zone object equals the instance.
@param tz Instance of another time zone.
*/
public boolean equals ( TZ tz )
{	if ( 	(_zulu_offset_minutes == tz._zulu_offset_minutes) && 
		(_dsflag == tz._dsflag) &&
		(_ds_offset_minutes == tz._ds_offset_minutes) ){
		return true;
	}
        return false;
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	_abbreviation = null;
	_description = null;
	super.finalize();
}

/**
Return the time zone abbreviation.
@return Time zone abbreviation.
*/
public String getAbbreviation()
{	return _abbreviation;
}

/**
Return the internal TZData Vector position given an abbreviation.  A null
string abbreviation is treated like an empty string (Zulu time).
@return The array position, or -1 if the abbreviation is not found.
@param abbr Time zone abbreviation.
*/
private static int getDefinedDataPosition ( String abbr )
{	if ( abbr == null ) {
		// Treat like no time zone...
		abbr = "";
	}

	TZ tz = null;
	int size = TZData.size();
	for ( int i = 0; i < size; i++ ) {
		tz = (TZ)TZData.get(i);
		if( abbr.equalsIgnoreCase( tz._abbreviation ) ){
			tz = null;
			return i;
		}
	}
	tz = null;
	return -1;
}

/**
Return the time zone description given an abbreviation.
@return The description for the time zone abbreviation.
@param abbr Time zone abbreviation.
@exception Exception if the time zone cannot be determined.
*/
public static String getDefinedDescription ( String abbr )
throws Exception
{	if ( abbr == null ){
		Message.printWarning( 20, "TZ.getDefinedDescription",
		"Incoming abbreviation is null, cannot continue." );
		throw new Exception (
		"Unable to look up time zone description for \"" +
		abbr + "\"" );
	}

	int pos = getDefinedDataPosition(abbr);
	if ( pos < 0 ) {
		if ( Message.isDebugOn ) {
			Message.printWarning( 20,
			"TZ.getDefinedDescription",
			"Zone \"" + abbr + 
			"\" is not found in look-up list" );
		}
		throw new Exception (
		"Unable to look up time zone description for \"" +
		abbr + "\"" );
	}
	return ((TZ)TZData.get(pos))._description;
}

/**
Return the daylight savings flag for a time zone abbreviation.
@return The daylight savings flag for a time zone abbreviation.
@param abbr Time zone abbreviation.
@exception Exception if the time zone cannot be determined.
*/
public static int getDefinedDSFlag ( String abbr )
throws Exception
{	if( abbr == null ){
		Message.printWarning( 20, "TZ.getDefinedDSFlag",
		"Incoming abbreviation is null, cannot continue." );
		throw new Exception (
		"Unable to look up daylight savings flag for \"" +
		abbr + "\"" );
	}

	int pos = getDefinedDataPosition(abbr);
	if ( pos < 0 ) {
		if ( Message.isDebugOn ) {
			Message.printWarning( 20, "TZ.getDefinedDSFlag",
			"Zone \"" + abbr +
			"\" is not found in look-up list.");
		}
		throw new Exception (
		"Unable to look up daylight savings flag for \"" +
		abbr + "\"" );
	}
	return ((TZ)TZData.get(pos))._dsflag;
}

/**
Return the daylight savings offset for a time zone abbreviation.
@return The daylight savings offset for a time zone abbreviation.
@param abbr Time zone abbreviation.
@exception Exception if the time zone cannot be determined.
*/
public static int getDefinedDSOffsetMinutes ( String abbr )
throws Exception
{	if( abbr == null ){
		Message.printWarning( 20, "TZ.getDefinedDSOffsetMinutes",
		"Incoming abbreviation is null, cannot continue." );
		throw new Exception (
		"Unable to look up daylight savings flag for \"" +
		abbr + "\"" );
	}

	int pos = getDefinedDataPosition(abbr);
	if ( pos < 0 ) {
		if ( Message.isDebugOn ) {
			Message.printWarning( 20,
			"TZ.getDefinedDSOffsetMinutes", "Zone \"" + abbr +
			"\" is not found in look-up list");
		}
		throw new Exception (
		"Unable to look up daylight savings offset for \"" +
		abbr + "\"" );
	}
	return ((TZ)TZData.get(pos))._ds_offset_minutes;
}

/**
Return the list of defined time zones as TZ.
@return The list of defined time zone data.
*/
public static List<TZ> getDefinedTimeZones()
{	return TZData;
}

/**
Return the defined time zone given an abbreviation.
@return The time zone.
@param abbr Time zone abbreviation.
@exception Exception if the time zone cannot be determined.
*/
public static TZ getDefinedTZ ( String abbr )
throws Exception
{	if( abbr == null ){
		Message.printWarning( 20, "TZ.getDefinedTZ ",
		"Incoming abbreviation is null, cannot continue." );
		throw new Exception (
		"Unable to look up time zone number for \"" +
		abbr + "\"" );
	}

	int pos = getDefinedDataPosition(abbr);
	if ( pos < 0 ) {
		if ( Message.isDebugOn ) {
			Message.printWarning( 20,
			"TZ.getDefinedTZ", "Zone \"" + abbr + 
			"\" is not found in look-up list." );
		}
		throw new Exception (
		"Unable to look up time zone for \"" + abbr + "\"" );
	}
	return (TZ)TZData.get(pos);
}

/**
Return the time zone offset given an abbreviation.
@return The time zone number.
@param abbr Time zone abbreviation.
@exception Exception if the time zone cannot be determined.
*/
public static int getDefinedZuluOffsetMinutes ( String abbr )
throws Exception
{	if( abbr == null ){
		Message.printWarning( 20, "TZ.getDefinedZuluOffsetMinutes ",
		"Incoming abbreviation is null, cannot continue." );
		throw new Exception (
		"Unable to look up time zone number for \"" +
		abbr + "\"" );
	}

	int pos = getDefinedDataPosition(abbr);
	if ( pos < 0 ) {
		if ( Message.isDebugOn ) {
			Message.printWarning( 20,
			"TZ.getDefinedZuluOffsetMinutes ",
			"Zone \"" + abbr + 
			"\" is not found in look-up list." );
		}
		throw new Exception (
		"Unable to look up time zone number for \"" +
		abbr + "\"" );
	}
	return ((TZ)TZData.get(pos))._zulu_offset_minutes;
}

/**
Return the time zone description.
@return Time zone description.
*/
public String getDescription()
{	return _description;	
}

/**
Return the daylight savings flag.
@return Daylight savings flag.
*/
public int getDSFlag()
{	return _dsflag;
}

/**
Return the daylight savings offset from standard time, in minutes.
@return Daylight savings offset.
*/
public int getDSOffsetMinutes()
{	return _ds_offset_minutes;
}

/**
Return a list of defined time zone data that match the time zone for
the specified offset and daylight savings flag.
@return Matching time zones, or null if none are found.
@param zulu_offset_minutes Offset in minutes between zulu time and the standard
time zone of interest.
@param dsflag If 1, search for time zones where daylight savings is in
effect.  For example, if searching for MDT (Mountain Daylight Time), pass true
and pass -7 for zulu_offset_minutes (the offset for MST).
If 0, search for time zones where daylight savings is not in effect.
If -1, search for the generic time zone that can be used to represent daylight
savings or standard time, depending on the time of the year (it is not
recommended to use this value unless specifically looking for generic
time zones, for example to display as a list of choices).
@exception Exception if the time zone cannot be determined.
*/
public static List<TZ> getMatchingDefinedTZ ( int zulu_offset_minutes, int dsflag )
throws Exception
{	List<TZ> matches = new ArrayList<TZ>();
	int size = TZData.size();
	TZ tz2 = null;
	for ( int i = 0; i < size; i++ ) {
		tz2 = (TZ)TZData.get(i);
		if (	(tz2._zulu_offset_minutes == zulu_offset_minutes) &&
			(tz2._dsflag == dsflag) ) {
			// Time zones match...
			matches.add ( tz2 );
		}
	}
	if ( matches.size() == 0 ) {
		return null;
	}
	return matches;
}

/**
Return a list of defined time zone data that match the time zone for
the specified abbreviation.  The comparison is done on the data for the given
time zone, not just the string.
@return Matching time zones, or null if none are found.
@param abbr Time zone abbreviation.
@param include_self If true, include the original time zone.  If false, do not include.
@exception Exception if the time zone cannot be determined.
*/
public static List<TZ> getMatchingDefinedTZ ( String abbr, boolean include_self )
throws Exception
{	TZ tz = getDefinedTZ ( abbr );
	List<TZ> matches = new ArrayList<TZ>();
	int size = TZData.size();
	TZ tz2 = null;
	for ( int i = 0; i < size; i++ ) {
		tz2 = (TZ)TZData.get(i);
		// Comparison is done on data, not the abbreviation...
		if ( tz2.equals(tz) ) {
			if ( include_self || !tz.getAbbreviation().equalsIgnoreCase(tz2.getAbbreviation()) ) {
				matches.add ( tz2 );
			}
		}
	}
	if ( matches.size() == 0 ) {
		return null;
	}
	return matches;
}

/**
Return the offset of standard time from zulu time, in minutes.
@return the offset of standard time from zulu time, in minutes.
*/
public int getZuluOffsetMinutes()
{	return _zulu_offset_minutes;
}

/**
Initialize instance data.
*/
private void init()
{	_zulu_offset_minutes = 0;
	_dsflag	= 0;
	_ds_offset_minutes = 0;
	_abbreviation = "";
	_description = "";
}

/**
Add a time zone to the defined time zones, at the specific position.  This is
typically used to add a time zone at position 0.
If there is a conflict in the abbreviation (another time zone with
the same abbreviation), the first time zone found will be used.
@param tz Fully-defined TZ to add to the list of defined time zones.
*/
public static void insertTimeZoneAt ( TZ tz, int index )
{	TZData.add ( index, tz );
}

/**
Return a string representation of the time zone which is
"abbreviation,description,zulu_offset_minutes,dsflag,ds_offset_minutes)".
@return A string representation of the time zone.
*/
public String toString()
{	return _abbreviation + "," + _description + "," + _zulu_offset_minutes +
		"," + _dsflag + "," + _ds_offset_minutes;
}

} // End TZ
