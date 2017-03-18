package RTi.Util.Time;

// TODO SAM 2012-04-11 This code needs to be reviewed in conjunction with DateTimeFormat.  Ideally, the
// DateTimeFormat class should just contain instances of DateTimeFormatterType (an enumeration) and the
// format string.  Then a DateTimeFormatter class could use DateTimeFormat to do formatting and this
// DateTimeParse class could be used for parsing. For now, include the DateTimeFormatter type as a data member
// in this class
/**
Parser for a date/time string.
*/
public class DateTimeParser
{
    
/**
The formatter that is used with parsing.  This indicates the expected format of the date/times.
*/
DateTimeFormatterType __formatterType = null;

/**
The format string used with the formatter to translate date/time strings to DateTime objects.
*/
private String __formatString = null;

/**
Current year - this is used when a 2-digit year format is parsed.
*/
private int __currentYear2 = 0;

/**
Current century - this is used when a 2-digit year format is parsed.
*/
private int __currentCentury = 0;

/**
Construct a parser with a format string to use for parsing.
The format type is determined from the first part of the string.
*/
public DateTimeParser ( String formatString )
{
    if ( formatString == null ) {
        throw new IllegalArgumentException( "Formatter string is null." );
    }
    int pos = formatString.indexOf(":");
    if ( pos > 0 ){
        // String might indicate the format type
        DateTimeFormatterType formatterType = DateTimeFormatterType.valueOfIgnoreCase(formatString.substring(0,pos));
        if ( formatterType == null ) {
            // Use default
            init ( DateTimeFormatterType.C, formatString );
        }
        else {
            init ( formatterType, formatString.substring(pos+1) );
        }
    }
    else {
        init ( DateTimeFormatterType.C, formatString );
    }
}

/**
Construct a parser with a formatter type and format string to use for parsing.
*/
public DateTimeParser ( DateTimeFormatterType formatterType, String formatString )
{
    init ( formatterType, formatString );
}

/**
Initialize the instance.
*/
private void init ( DateTimeFormatterType formatterType, String formatString )
{
    if ( formatterType != DateTimeFormatterType.C ) {
        throw new IllegalArgumentException( "Date/time formatter type " + formatterType + " is not supported." );
    }
    if ( formatString == null ) {
        throw new IllegalArgumentException( "Formatter string is null." );
    }
    setDateTimeFormatterType ( formatterType );
    setDateTimeFormatString ( formatString );
    // Determine the current year.
    DateTime now = new DateTime(DateTime.DATE_CURRENT);
    setCurrentCentury ( (now.getYear()/100) * 100); // For example, roundoff 2012 to 2000
    setCurrentYear2 ( now.getYear() - getCurrentCentury() );
}

/**
Parse a string into a date/time object.  If the provided DateTime instance is not null, then it will
be filled with the result; if null, a new DateTime instance will be created.
The precision of the result is set to the finest date/time part that is set.
@param dt DateTime instance to reuse, or null to create and return a new DateTime instance
@param dtString date/time string to parse, using the formatter specified in the constructor
@exception IllegalArgumentException if the input cannot be parsed
*/
public DateTime parse ( DateTime dt, String dtString )
{
    if ( dt == null ) {
        // Create a new date/time
        dt = new DateTime();
    }
    else {
        // Reinitialize
        dt.setToZero();
    }
    // Process the format and extract corresponding information from the string
    DateTimeFormatterType formatterType = getFormatterType();
    if ( formatterType == DateTimeFormatterType.C ) {
        parseC ( dt, dtString );
    }
    return dt;
}

/**
Parse the date/time string using the C formatter type.
*/
private void parseC ( DateTime dt, String dtString )
{
    int smallestPrecision = DateTime.PRECISION_YEAR;
    String formatString = getFormatString();
    int lenFormat = formatString.length();
    int icharFormat = 0; // Position in format string
    int icharString = 0; // Position in string being parsed
    char c; // Character in format string
    String s; // String carved out of string for parsing
    // Not sure what order information will be set so turn off checking
    dt.setPrecision(DateTime.DATE_FAST, true);
    while ( icharFormat < lenFormat ) {
        c = formatString.charAt(icharFormat);
        if ( c == '%') {
            // Have a format character to process
            ++icharFormat;
            if ( icharFormat >= lenFormat ) {
                // Past end of format string
                break;
            }
            c = formatString.charAt(icharFormat);
            ++icharFormat; // For next loop
            if ( c == 'a' ) {
                // Abbreviated weekday name.
                // Not handled
            }
            else if ( c == 'A' ) {
                // Full weekday name.
                // Not handled
            }
            else if ( c == 'b' ) {
                // Abbreviated month name - 3 characters
                s = dtString.substring(icharString,icharString+3);
                dt.setMonth(TimeUtil.monthFromAbbrev(s));
                icharString += 3;
                smallestPrecision = Math.min(smallestPrecision, DateTime.PRECISION_MONTH);
            }
            else if ( c == 'B' ) {
                // Long month name.
                // Not handled
            }
            else if ( c == 'c' ) {
                // Not handled
            }
            else if ( c == 'd' ) {
                // Day of month - 2 characters
                s = dtString.substring(icharString,icharString+2);
                dt.setDay(Integer.parseInt(s));
                icharString += 2;
                smallestPrecision = Math.min(smallestPrecision, DateTime.PRECISION_DAY);
            }
            else if ( c == 'H' ) {
                // Hour of day - 2 characters
                s = dtString.substring(icharString,icharString+2);
                dt.setHour(Integer.parseInt(s));
                icharString += 2;
                smallestPrecision = Math.min(smallestPrecision, DateTime.PRECISION_HOUR);
            }
            else if ( c == 'I' ) {
                // Hour of day 1-12
                // Not supported since need the AM/PM also
            }
            else if ( c == 'j' ) {
                // Day of year (assumes that year is already set)...
                // Not supported
            }
            else if ( c == 'm' ) {
                // Month of year...
                s = dtString.substring(icharString,icharString+2);
                dt.setMonth(Integer.parseInt(s));
                icharString += 2;
                smallestPrecision = Math.min(smallestPrecision, DateTime.PRECISION_MONTH);
            }
            else if ( c == 'M' ) {
                // Minute of hour - 2 digits
                s = dtString.substring(icharString,icharString+2);
                dt.setMinute(Integer.parseInt(s));
                icharString += 2;
                smallestPrecision = Math.min(smallestPrecision, DateTime.PRECISION_MINUTE);
            }
            else if ( c == 'p' ) {
                // AM or PM...
                // Not supported
            }
            else if ( c == 'S' ) {
                // Seconds of minute - 2 digits
                s = dtString.substring(icharString,icharString+2);
                dt.setSecond(Integer.parseInt(s));
                icharString += 2;
                smallestPrecision = Math.min(smallestPrecision, DateTime.PRECISION_SECOND);
            }
            else if ( (c == 'U') || (c == 'W')) {
                // Week of year...
                // Not supported
            }
            else if ( c == 'x' ) {
                // Not supported
            }
            else if ( c == 'X' ) {
             // Not supported
            }
            else if ( c == 'y' ) {
                // Two digit year...
                s = dtString.substring(icharString,icharString+2);
                int y2 = Integer.parseInt(s);
                // Initialize 4-digit year to current century
                int y4 = getCurrentCentury() + y2;
                if ( y2 > getCurrentYear2() ) {
                    // Assume date was actually in the last century
                    y4 -= 100;
                }
                dt.setYear(y4);
                icharString += 2;
                smallestPrecision = Math.min(smallestPrecision, DateTime.PRECISION_YEAR);
            }
            else if ( c == 'Y' ) {
                // 4-digit year
                s = dtString.substring(icharString,icharString+4);
                dt.setYear(Integer.parseInt(s));
                icharString += 4;
                smallestPrecision = Math.min(smallestPrecision, DateTime.PRECISION_YEAR);
            }
            else if ( c == 'Z' ) {
                // Time zone not supported - don't know length
            }
        }
        else {
            // Other characters in format are treated as placeholders - skip the character in format and string
            ++icharFormat;
            ++icharString;
        }
    }
    // Reset to strict so that further use of the date/time will enforce valid date/times
    dt.setPrecision(smallestPrecision);
    dt.setPrecision(DateTime.DATE_STRICT, true);
}

/**
Return the current century as 4-digit year.
*/
private int getCurrentCentury ()
{
    return __currentCentury;
}

/**
Return the current 2-digit year.
*/
private int getCurrentYear2 ()
{
    return __currentYear2;
}

/**
Return the format string.
*/
public String getFormatString ()
{
    return __formatString;
}

/**
Return the formatter type.
*/
public DateTimeFormatterType getFormatterType ()
{
    return __formatterType;
}

/**
Set the current century as 4-digit year.
*/
private void setCurrentCentury ( int currentCentury )
{
    __currentCentury = currentCentury;
}

/**
Set the current 2-digit year.
*/
private void setCurrentYear2 ( int currentYear2 )
{
    __currentYear2 = currentYear2;
}

/**
Set the date/time format string.
*/
private void setDateTimeFormatString ( String formatString )
{
    __formatString = formatString;
}

/**
Set the date/time formatter type.
*/
private void setDateTimeFormatterType ( DateTimeFormatterType formatterType )
{
    __formatterType = formatterType;
}

}