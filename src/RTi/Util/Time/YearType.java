// YearType - Year types, which indicate the span of months that define a year

package RTi.Util.Time;

import java.util.ArrayList;
import java.util.List;

/**
Year types, which indicate the span of months that define a year.  For example "Water Year" is often
used in the USA to indicate annual water volumes, based on seasons.  This enumeration should be used to
indicate common full-year definitions.  Year types that include only a season or part of the year could
specify this enumeration for full years and would otherwise need to define the year in some other way.
By convention, non-calendar year types that do not contain "Year" in the name start in the previous
calendar year and end in the current calendar year.  As new year types are added they should conform to the
standard of starting with "Year" if the start matches the calendar year, and ending with "Year" if the end
matches the calendar year.
*/
public enum YearType
{
/**
Standard calendar year.
*/
CALENDAR ( "Calendar", 0, 1, 0, 12 ),
/**
November (of previous year) to October (of current) year.
*/
NOV_TO_OCT ( "NovToOct", -1, 11, 0, 10 ),
/**
Water year (October to September).
*/
WATER ( "Water", -1, 10, 0, 9 ),
/**
May to April, with year agreeing with start.
*/
YEAR_MAY_TO_APR ( "YearMayToApr", 0, 5, 1, 4 );

/**
The name that is used for choices and other technical code (terse).
*/
private final String __displayName;

/**
The calendar year offset in which the year starts.
For example, -1 indicates that the year starts in the previous calendar year.
*/
private final int __startYearOffset;

/**
The calendar year offset in which the year ends.
For example, 0 indicates that the year ends in the previous calendar year.
*/
private final int __endYearOffset;

/**
The calendar month (1-12) when the year starts.  For example, 10 indicates that the year starts in October.
*/
private final int __startMonth;

/**
The calendar month (1-12) when the year ends.  For example, 9 indicates that the year ends in September.
*/
private final int __endMonth;

/**
Construct an enumeration value.
@param displayName name that should be displayed in choices, etc.
@param startYearOffset the offset to the calendar year for the start of the year.  For example, does the
output year start in the same year as the calendar year (0), previous calendar year (-1), or next calendar year (1)?
@param startMonth the first calendar month (1-12) for the year type.
@param endYearOffset the offset to the calendar year for the end of the year.  For example, does the
output year end in the same year as the calendar year (0), previous calendar year (-1), or next calendar year (1)?
@param endMonth the last calendar month (1-12) for the year type.
*/
private YearType(String displayName, int startYearOffset, int startMonth, int endYearOffset, int endMonth ) {
    this.__displayName = displayName;
    this.__startYearOffset = startYearOffset;
    this.__startMonth = startMonth;
    this.__endYearOffset = endYearOffset;
    this.__endMonth = endMonth;
}

/**
Return the last month (1-12) in the year.
@return the last month in the year.
*/
public int getEndMonth ()
{
    return __endMonth;
}

/**
Return the end year offset.
@return the end year offset.
*/
public int getEndYearOffset ()
{
    return __endYearOffset;
}

/**
 * Return the year start (calendar notation for the current year and year type).
 * For example, if Water year (Oct 1 of previous calendar year to Sep 30 of ending calendar year),
 * return Oct 1 of the previous year.
 * @return
 */
public DateTime getStartDateTimeForCurrentYear () {
	DateTime now = new DateTime(DateTime.DATE_CURRENT);
	now.setDay(1);
	now.setHour(0);
	now.setMinute(0);
	now.setSecond(0);
	if ( getStartYearOffset() < 0 ) {
		if ( now.getMonth() < getStartMonth() ) {
			// The start date/time needs to be adjusted (generally to previous year)
			now.addYear(getStartYearOffset());
		}
	}
	now.setMonth(getStartMonth());
	return now;
}

/**
Return the first month (1-12) in the year.
@return the first month in the year.
*/
public int getStartMonth ()
{
    return __startMonth;
}

/**
Return the start year offset.
For example, -1 indicates that the year starts in the previous calendar year.
@return the start year offset.
*/
public int getStartYearOffset ()
{
    return __startYearOffset;
}

/**
Get the list of year types.
@return the list of year types.
*/
public static List<YearType> getYearTypeChoices()
{
    List<YearType> choices = new ArrayList<YearType>();
    choices.add ( YearType.CALENDAR );
    choices.add ( YearType.NOV_TO_OCT );
    choices.add ( YearType.WATER );
    return choices;
}

/**
Get the list of year types.
@return the list of year types as strings.
*/
public static List<String> getYearTypeChoicesAsStrings()
{
    List<YearType> choices = getYearTypeChoices();
    List<String> stringChoices = new ArrayList<String>();
    for ( int i = 0; i < choices.size(); i++ ) {
        YearType choice = choices.get(i);
        String choiceString = "" + choice;
        stringChoices.add ( choiceString );
    }
    return stringChoices;
}

/**
Return the short display name for the statistic.  This is the same as the value.
@return the display name.
*/
@Override
public String toString() {
    return __displayName;
}

/**
Return the enumeration value given a string name (case-independent).
@param name the year type string to match.  
@return the enumeration value given a string name (case-independent), or null if not matched.
@exception IllegalArgumentException if the name does not match a valid year type.
*/
public static YearType valueOfIgnoreCase (String name)
{	if ( name == null ) {
		return null;
	}
    YearType [] values = values();
    for ( YearType t : values ) {
        if ( name.equalsIgnoreCase(t.toString())) {
            return t;
        }
    }
    return null;
}

/**
Indicate if the year for the year type matches the calendar year for the start.
This will be the case if the start offset is zero.
*/
public boolean yearMatchesStart()
{
    if ( getStartYearOffset() == 0 ) {
        return true;
    }
    else {
        return false;
    }
}

}
