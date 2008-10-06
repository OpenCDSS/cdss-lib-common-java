// ----------------------------------------------------------------------------
// ShefATS - class for I/O of '.A' type SHEF (Standard Hydrological Exchange
//				Format) time series
// ----------------------------------------------------------------------------
// History:
//
// 2001-06-13	Michael Thiemann, RTi	Initial version (C++).
// 2003-07-22	Steven A. Malers, RTi	Port to Java.  Include fewer methods
//					than the C++ and utilize a PropList
//					to pass parameters, since there are
//					so many options.
// 2003-11-03	SAM, RTi		* Finish initial port, for testing with
//					  the Idaho Power system.
//					* Add more Javadoc.
//					* When writing time series, check the
//					  TS start date time zone.
// 2003-11-19	SAM, RTi		* Add comments to the top of output.
//					* Remove overloaded methods that were
//					  commented out.
// 2003-11-24	SAM, RTi		* Duration was being written twice when
//					  specified as input.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import RTi.Util.IO.DataType;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.DataUnitsConversion;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.MeasTimeScale;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TZ;

/**
This ShefATS class provides static methods for reading and writing .A format
SHEF (Standard Hydrological Exchange Format) time series.  Currently, only
write methods are available.
*/
public class ShefATS
{

/**
Use the in-memory DataType data to lookup the SHEF PE codes for a list of time
series, using the time series data types.  These values can be used when calling
writeTimeSeriesList().  A blank PE code will be assigned if
it is not found, which will cause the time series to not be written.
*/
public static Vector getPEForTimeSeries ( Vector tslist )
{	String routine = "ShefATS.getPEForTimeSeries";
	Vector v = new Vector();
	int size = 0;
	if ( tslist != null ) {
		size = tslist.size();
	}
	TS ts;
	DataType datatype;
	for ( int i = 0; i < size; i++ ) {
		ts = (TS)tslist.elementAt(i);
		if ( ts == null ) {
			v.addElement ( "" );
			continue;
		}
		try {
            datatype = DataType.lookupDataType ( ts.getDataType() );
			v.addElement ( datatype.getSHEFpe() );
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine,
			"Unable to look up data type for \"" +
			ts.getIdentifierString() + "\" (" + ts.getDataType() +
			").  Setting SHEF PE to blank." );
			v.addElement ( "" );
			continue;
		}
	}
	return v;
}

/**
Get a SHEF time zone from another time zone string.  Recognized SHEF time zones
are taken from Table 8 of the SHEF handbook.
@return a recognized SHEF time zone matching the characteristics of the
specified time zone.
@param ts_tz Time zone abbreviation recognized by the RTi.Util.Time.TZ class.
@exception Exception if the specified time zone cannot be converted to a
recognized SHEF time zone.
*/
public static String getSHEFTimeZone ( String ts_tz )
throws Exception
{	// Important time zones known to be SHEF time zones...
	String [] shef_tz = {
                "N", "NS",
				"A", "AD", "AS",
				"E", "ED", "ES",
				"C", "CD", "CS",
				"J",
				"M", "MD", "MS",
				"P", "PD", "PS",
				"Y", "YD", "YS",
				"H", "HS",
				"L", "LD", "LS",
				"B", "BD", "BS",
				"Z" };
	// Loop through the time zones.  If the abbreviation exactly matches a
	// SHEF time zone, assume that it is OK.
	for ( int i = 0; i < shef_tz.length; i++ ) {
		if ( ts_tz.equalsIgnoreCase(shef_tz[i]) ) {
			return shef_tz[i];	// Return the SHEF value in case
						// there is an upper/lower case
						// issue.
		}
	}
	// Get time zones with the same characteristics...
	Vector matching_tz = TZ.getMatchingDefinedTZ ( ts_tz, false );
	int size = 0;
	if ( matching_tz != null ) {
		size = matching_tz.size();
	}
	// Loop through the matches and compare with the SHEF time zones that
	// are know.  If a match occurs, then return the matching SHEF time
	// zone...
	String tz_abbrev;
	for ( int j = 0; j < size; j++ ) {
		tz_abbrev = ((TZ)matching_tz.elementAt(j)).getAbbreviation();
		for ( int i = 0; i < shef_tz.length; i++ ) {
			if ( tz_abbrev.equalsIgnoreCase(shef_tz[i]) ) {
				return shef_tz[i];
			}
		}
	}
	throw new Exception ("Unable to find SHEF time zone for \""+ts_tz+"\"");
	//return null;	// Compiler complains if no return.
}

/**
Write a single time series to a SHEF .A format file.
@param ts Single time series to write.
@param fname Name of file to write.
The IOUtil.getPathUsingWorkingDir() method is applied to the filename.
@param date1 First date to write (if NULL write the entire time series).
@param date2 Last date to write (if NULL write the entire time series).
@param units Units to write.  If different than the current units the units
will be converted on output.  Units are defined in Appendix A of the SHEF
documentation.
@param write_data Indicates whether data should be written (as opposed to only
writing the header).
@param PE physical element corresponding to the time series data type,
according to Table 9 of the SHEF documentation.  This is required.
@param duration Duration of the data, as defined by tables 3 and 11 of the
SHEF documentation.
@param alt_id Aternate identifier, different from the time series identifier
location, to be used in SHEF output.
@param props See the overloaded method for a description.
@exception Exception if there is an error writing the file.
*/
public static void writeTimeSeries (	TS ts, String fname,
					DateTime date1, DateTime date2,
					String units, boolean write_data,
					String PE,
					String duration,
					String alt_id,
					PropList props )
throws Exception
{	// Call the fully-loaded method...
	Vector v = new Vector(1);
	v.addElement ( ts );
	Vector PEList = null, UnitsList = null, DurationList = null,
		AltIDList = null;
	if ( (PE != null) && !PE.equals("") ) {
		PEList = new Vector ( 1 );
		PEList.addElement ( PE );
	}
	if ( (units != null) && !units.equals("") ) {
		UnitsList = new Vector ( 1 );
		UnitsList.addElement ( units );
	}
	if ( (alt_id != null) && !alt_id.equals("") ) {
		AltIDList = new Vector ( 1 );
		AltIDList.addElement ( alt_id );
	}
	if ( (duration != null) && !duration.equals("") ) {
		DurationList = new Vector ( 1 );
		DurationList.addElement ( duration );
	}

	writeTimeSeriesList ( v, fname, date1, date2,
		UnitsList, PEList, DurationList, AltIDList, props );
}

/**
Write a Vector of time series to a SHEF A format file.
@param tslist Vector of time series to write.
@param out PrintWriter to write to.
@param date1 First date to write (if null write the entire time series).
@param date2 Last date to write (if null write the entire time series).
@param unitsList List of units other than the default, if not an empty list,
then one per time series.
@param PE Vector of PE Physical element codes (see SHEF Handbook), one per time
series.
@param DurList Vector of duration codes (see SHEF Handbook), one per time
series.
@param AltID Vector of alternate identifiers to output (default is to use the
TS location).
@param props Properties to control the output, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td>CreationDate</td>
<td>The creation date part of the SHEF message, which will be applied to all
records that are output (e.g., "DCYYMMDDHHmm").</td>
<td>No creation date is included in the output output.</td>
</tr>

<tr>
<td>Duration</td>
<td>The duration part of the SHEF message, which will be applied to all records
that are output.  Specifying this string may be needed in cases where the
default cannot be determined correctly (e.g., to specify a duration of "DH1200"
for forecasted maximum and minimum temperatures).</td>
<td>Determine the duration from the data interval of the time series.  For
example, a 24Hour interval would result in "DH2400".</td>
</tr>

<tr>
<td>HourMax</td>
<td>The maximum integer hour in the day.  Specify 23 (for 0-23 clock) or
24 (for 1-24 clock).</td>
<td>23</td>
</tr>

<tr>
<td>TimeZone</td>
<td>The time zone abbreviation, which be applied for all SHEF messages.
The time zone should match a value from according to Table 8 of the SHEF
documentation.  If null or blank, the time zone from the time series start date
will be used.  If that is blank, "Z" will be used.  If a non-null time zone is
determined from the parameter or time series, an attempt will be made to convert
the time zone to standard SHEF time zones (e.g., "MST" becomes "MS").  Note
that the time series time zone, if known internally is not shifted (data remain
in the same hour as originally read).</TD>
<td>Blank, which results in "Z" being used in SHEF messages.</td>
</tr>

</table>
*/
public static void writeTimeSeriesList (Vector tslist, PrintWriter out,
					DateTime date1, DateTime date2,
					Vector unitsList,
					Vector PE,
					Vector DurList,
					Vector AltID,
					PropList props )
throws Exception
{	String message, routine = "ShefATS.writeTimeSeriesList";
	DateTime ts_start = null, ts_end = null;

	// Check for a null time series list...

	if ( tslist == null ) {
		Message.printWarning ( 2, routine, "Null time series list.  Not writing SHEFA file." );
		return;
	}

	int size = tslist.size();
	if ( size == 0 ) {
		Message.printWarning ( 2, routine, "No time series in list.  Not writing." );
		return;
	}

	// Initialize properties so don't have to check for null list below...
	if ( props == null ) {
		props = new PropList ( "ShefATS" );
	}

	// Check properties...

	boolean Hour24Flag = false;
	String prop_val = props.getValue ( "HourMax" );
	String hour_max_prop = "";
	if ( (prop_val != null) && prop_val.equals("24") ) {
		Hour24Flag = true;
		hour_max_prop = prop_val;
	}
	String duration_prop = "";
	prop_val = props.getValue ( "Duration" );
	if ( prop_val != null ) {
		duration_prop = prop_val;
	}
	String creation_date_prop = "";
	prop_val = props.getValue ( "CreationDate" );
	if ( prop_val != null ) {
		creation_date_prop = prop_val;
	}	// After this point, no more changes to the duration.
	String time_zone_prop = "";
	prop_val = props.getValue ( "TimeZone" );
	if ( prop_val != null ) {
		time_zone_prop = prop_val;
	}	// After this point, no more changes to the time zone.

	// Use the requested output period if not null...

	if ( date1 != null ) {
		ts_start = new DateTime ( date1 );
	}
	if ( date2 != null ) {
		ts_end = new DateTime ( date2 );
	}

	// Check time zone

	String SHEFTimeZone = "";
	if ( (time_zone_prop != null) && (time_zone_prop.length() > 0) ) {
		// Use the specified time zone for all time series.
		SHEFTimeZone = time_zone_prop;
	}
	else {	// Default is Zulu...
		SHEFTimeZone = "Z";
	}

	// Check if alternative IDs are defined
	boolean alternativeIDDefined = false;
	if ( (AltID != null) && (AltID.size() == size) ) {
		alternativeIDDefined = true;
	}

	// Check if durations are defined
	boolean durationDefined = false;
	if ( (DurList != null) && (DurList.size() == size) ) {
		durationDefined = true;
	}

	TS ts = null;

	boolean newUnitsDefined = false;
	if ( (unitsList != null) && (unitsList.size() == size) ) {
		newUnitsDefined = true;
	}
	
	double value = 0.0;
	double mult, add;
	int system, newSystem;		// Units system
	boolean scale = false;
	String durationCode = "";
	DateTime t;

	// Output some header comments...

	out.println ( ": SHEF A file" );
	IOUtil.printCreatorHeader ( out, ":", 80, 0 );
	out.println ( ":" );
	out.println ( ": Time series and requested output are as follows." );
	out.println ( ": Blanks indicate that values will be determined automatically." );
	out.println ( ":" );
	String units, pe, dur, altid;
	out.println ( ":Count Location        Units PE      Duration AltID" );
	out.println ( ":" );
	for ( int i = 0; i < size; i++ ) {
		ts = (TS)tslist.elementAt(i);
		if ( ts == null ) {
			continue;
		}
		units = "";
		pe = "";
		dur = "";
		altid = "";
		if ( (unitsList != null) && (unitsList.size() == size) ) {
			units = (String)unitsList.elementAt(i);
		}
		if ( (PE != null) && (PE.size() == size) ) {
			pe = (String)PE.elementAt(i);
		}
		if ( (DurList != null) && (DurList.size() == size) ) {
			dur = (String)DurList.elementAt(i);
		}
		if ( (AltID != null) && (AltID.size() == size) ) {
			altid = (String)AltID.elementAt(i);
		}
		out.println ( ": " +
			StringUtil.formatString((i + 1),"%4d" ) + " " +
			StringUtil.formatString(ts.getLocation(),"%-15.15s")+" "
			+ StringUtil.formatString(units,"%-5.5s" ) + " "
			+ StringUtil.formatString(pe,"%-7.7s" ) + " "
			+ StringUtil.formatString(dur,"%-8.8s" ) + " "
			+ StringUtil.formatString(altid,"%s") );
	}
	out.println ( ":" );
	out.println ( ": Override properties for output are as follows:" );
	out.println ( ":" );
	if ( creation_date_prop.equals("") ) {
		out.println ( ": CreationDate = default (not specified)" );
	}
	else {
        out.println ( ": CreationDate = " + creation_date_prop );
	}
	if ( duration_prop.equals("") ) {
		out.println ( ": Duration = default (determined from data interval)" );
	}
	else {
        out.println ( ": Duration = " + duration_prop );
	}
	if ( hour_max_prop.equals("") ) {
		out.println ( ": HourMax = default (23).  Hours are 0-23" );
	}
	else {
        out.println ( ": HourMax = " + hour_max_prop );
	}
	if ( time_zone_prop.equals("") ) {
		out.println ( ": TimeZone = default (Z)." );
	}
	else {
        out.println ( ": TimeZone = " + time_zone_prop );
	}
	out.println ( ":" );

	DataUnits tsUnits;
	DataUnitsConversion conversion;
	String SHEFID, SHEFMessage;
	String dateString,qualityFlag;
	String PhysicalElement, scaleType;
	String SHEFSystem, unitsFormat;
	int tsMult, tsBase;

	TSIterator tsi;

	// Loop through the time series...

	String SHEFTimeZone_ts = SHEFTimeZone;	// SHEF time zone to be used for
						// a time series.
	String timeZone_ts = null;		// Time zone from a time series
						// start date/time.
	String creation_date=creation_date_prop;// Used to indicate when the
						// data record was created.
	for ( int i = 0; i < size; i++ ) {
		ts = (TS)tslist.elementAt(i);
		if ( ts == null ) {
			continue;
		}

		if ( date1 == null ) {
			// Use the start from the time series...
			ts_start = ts.getDate1();
			if ( ts_start == null ) {
				continue;
			}
		}
		if ( date2 == null ) {
			// Use the end from the time series...
			ts_end = ts.getDate2();
			if ( ts_end == null ) {
				continue;
			}
		}

		// Check the time zone to see if it should be taken from the
		// time series...
	
		SHEFTimeZone_ts = SHEFTimeZone;	 // Default determined above
		if ((time_zone_prop == null) || (time_zone_prop.length() == 0)){
			// No time zone was specified as an input parameter
			// so try to get it from the time series...
			timeZone_ts = ts.getDate1().getTimeZoneAbbreviation();
			if ( timeZone_ts.length() != 0 ) {
				// Time series has a time zone so use it...
				try {
                    SHEFTimeZone_ts = getSHEFTimeZone ( timeZone_ts );
				}
				catch ( Exception e ) {
					// Unable to get a valid time zone...
					message = "Time zone from time series (\"" +
					timeZone_ts + "\") not recognized.  Skipping output.";
					out.println ( ": " + message );
					Message.printWarning ( 2, routine, message );
					continue;
				}
			}
		}

		tsi = ts.iterator ( ts_start, ts_end );
		if ( tsi == null ) {
			continue;
		}

		// get the identifier

		SHEFID = ts.getLocation();
		if ( alternativeIDDefined ) {
			if ( ((String)AltID.elementAt(i)).length() > 0 ) {
				SHEFID = (String)AltID.elementAt(i);
			}
		}

		// get the PE code
 		PhysicalElement = (String)PE.elementAt(i);
		if ( PhysicalElement.equals("") ) {
			message = "No PE code specified for \"" + ts.getIdentifierString() + "\"... skipping SHEF .A write.";
			out.println ( ": " + message );
			Message.printWarning ( 2, routine, message );
			continue;
		}

		// get multiplier and base
		tsMult = ts.getDataIntervalMult();
		tsBase = ts.getDataIntervalBase();

		// Get the original TS units
		try {	tsUnits = DataUnits.lookupUnits( ts.getDataUnits());
			system =  tsUnits.getSystem();
			unitsFormat = DataUnits.getOutputFormatString( ts.getDataUnits(), 0, 0 );
		}
		catch ( Exception e ) {
			message = "Error getting units for " +
			ts.getIdentifierString() + ": " + ts.getDataUnits() +
			". Will skip.";
			out.println ( ": " + message );
			Message.printWarning ( 2, routine, message );
			Message.printWarning( 2, routine, e );
			continue;
		}

		// Set the scale if the TS is accumulated or a mean.  If so, SHEF must have a duration
		scale = false;
		if ( durationDefined ) {
			scaleType = (String)DurList.elementAt( i );

			if (	scaleType.equalsIgnoreCase(MeasTimeScale.ACCM)||
				scaleType.equalsIgnoreCase(MeasTimeScale.MEAN)){
				scale = true;
				// a 'V' after the PE indicates that the
				// duration of the data is defined elsewhere in
				// the message
				PhysicalElement += "V";
			}
		}

		durationCode = "";
		//Set the duration code if the TS is regular
		if ( scale && (tsBase != TimeInterval.IRREGULAR) ) {
			durationCode = "/DV";
			String multString = 
			StringUtil.formatString( tsMult, "%02d" );
			if ( tsBase == TimeInterval.SECOND) {
				durationCode = durationCode + "S" + multString;
			}
			else if ( tsBase == TimeInterval.MINUTE) {
				durationCode = durationCode + "N" + multString;
			}
			else if( tsBase == TimeInterval.HOUR ) {
				durationCode = durationCode + "H" + multString;
			}
			else if( tsBase == TimeInterval.DAY ) {
				durationCode = durationCode + "D" + multString;
			}
			else if( tsBase == TimeInterval.WEEK) {
				multString = 
				StringUtil.formatString( tsMult * 7, "%02d" );
				durationCode = durationCode + "D" + multString;
			}
			else if( tsBase == TimeInterval.MONTH) {
				durationCode = durationCode + "M" + multString;
			}
			else if( tsBase == TimeInterval.YEAR) {
				durationCode = durationCode + "Y" + multString;
			}
		}

		// get conversion if units are defined
		mult = 1.0;
		add = 0.0;				
		if ( newUnitsDefined ) {
			units = (String)unitsList.elementAt( i );

			if ( (units != null) && (units.length() != 0) && !units.equalsIgnoreCase(ts.getDataUnits()) ) {
				try {
                    DataUnits newUnits = DataUnits.lookupUnits( units );
					newSystem = newUnits.getSystem();
					conversion = DataUnits.getConversion ( ts.getDataUnits(), units );
					mult = conversion.getMultFactor();
					add = conversion.getAddFactor();
					system = newSystem;
					unitsFormat = DataUnits.getOutputFormatString(units, 0, 0);
				}
				catch ( Exception e ) {
					message = "Unable to convert units to \"" +
					units + "\" leaving units as \"" +
					ts.getDataUnits() + "\"";
					out.println (": " + message + " :");
					Message.printWarning( 2, routine,
					message );
				}
			}
		}

		// get the units system
		if ( system == DataUnits.SYSTEM_SI ){
			SHEFSystem = "/DUS";
		}
		else if((system == DataUnits.SYSTEM_ENGLISH) || (system == DataUnits.SYSTEM_ALL) ){
			// Units system does not matter.  Default to English...
			SHEFSystem = "/DUE";
		}
		else {
            message = "Cannot find valid units system.  SHEF will default to ENGLISH.";
			Message.printWarning( 2, routine, message );

			SHEFSystem = "/DUE";
						
			out.println (": " + message + " :");
		}

		int duration = 0;
		int mod;

		// REVISIT - why is some of the following code in the loop.
		// Can't it be determined once outside the loop?

		while ( tsi.next() != null ) {
			value = tsi.getDataValue();
			if ( !ts.isDataMissing( value ) ) {
				value *= mult + add;
				// Revisit - is the contructor necessary?
				t = new DateTime(tsi.getDate());

				if ( Hour24Flag ) {
					if ( t.getHour() == 0 ) {
						t.setHour( 24 );
						t.addDay( -1 );
					}
				}

				dateString = t.toString( DateTime.FORMAT_YYYYMMDDHHmm );
				qualityFlag = "";

				if( tsBase == TimeInterval.IRREGULAR ) {
					if ( scale ) {
						// The duration is in seconds.
						duration =((IrregularTSIterator)tsi).getDuration();

						// TODO - this code is kind of ugly - maybe it can be done cleaner
						if ( duration > 0 ) {
							mod = duration/60; 
							if( mod > 0 ) {
								// Duration in minutes
								duration = mod;
								mod = duration/60; 
								durationCode = "/DVN";
								if( mod > 0 ) {
									//duration inhours
									duration = mod;
									mod = duration/24; 
									durationCode = "/DVH";
									if( mod > 0 ) { //duration in days
										duration = mod;
										mod = duration/30;
										durationCode = "/DVD";
										if( mod > 0 ) { //duration in month
											duration = mod;
											mod = duration/12;
											durationCode = "/DVM";
											if( mod > 0 ) { //duration in years
												duration = mod;
												durationCode = "/DVY";
											}
										}
									}
								}
							}
							else {
                                durationCode = "/DVS";
							}
						}

						durationCode = durationCode + StringUtil.formatString( duration, "%02d" );
					}
					
					qualityFlag = tsi.getDataFlag();
					if ( (qualityFlag != null) && (qualityFlag.length() > 0) ) {
						qualityFlag = "/DQ" + qualityFlag;
					}
				}

				// Override with the creation date if specified by the user...

				if ( creation_date_prop.length() == 0 ) {
					// Use the default...
					creation_date = "DH" + dateString.substring( 8 );
				}

				// Override the duration code determined above with the value specified by the user...

				if ( duration_prop.length() != 0 ) {
					durationCode = "/" + duration_prop;
				}

				SHEFMessage = ".A " + SHEFID + " " +
					dateString.substring( 0, 8 ) + " " + 
					SHEFTimeZone_ts + " " +
					creation_date +
					SHEFSystem +
					qualityFlag +
					durationCode +
					"/" + PhysicalElement + 
					" " + StringUtil.formatString( 
						value, unitsFormat );

				out.println ( SHEFMessage );
			}
		}
	}	
}

/**
Write a Vector of time series to the specified SHEF A file.
@param tslist Vector of time series to write.
@param fname Name of file to write.
@param date1 First date to write (if null write the entire time series).
@param date2 Last date to write (if null write the entire time series).
@param units Vector of units to write, one per time series.  If different than
the current units the units will be converted on output.  Specify null to use
the time series units.
@param Vector of PE Physical element codes, one per time series (see SHEF
Handbook).  This information must be supplied.
@param props See the overloaded method for a description.
@exception IOException if there is an error writing the file.
*/
public static void writeTimeSeriesList(	Vector tslist, String fname,
					DateTime date1, DateTime date2,
					Vector units,
					Vector PEList, Vector DurList,
					Vector AltIDList, PropList props )
throws Exception
{	String routine = "ShefATS.writeTimeSeriesList";

	String full_fname = IOUtil.getPathUsingWorkingDir(fname);
	try {	FileOutputStream fos = new FileOutputStream ( full_fname );
		PrintWriter fout = new PrintWriter ( fos );

		writeTimeSeriesList (	tslist, fout, date1, date2, units,
					PEList, DurList, AltIDList, props );

		fout.flush();
		fout.close();
	}
	catch ( Exception e ) {
		String message = "Error writing \"" +
		full_fname + "\" for writing.";
		Message.printWarning( 2, routine, message );
		Message.printWarning( 2, routine, e );
		throw new Exception (message);
	}
}

} // End ShefATS
