package RTi.Util.Time;

import java.util.List;
import java.util.Vector;

/**
Year types, which indicate the span of months that define a year.  For example "Water Year" is often
used in the USA to indicate annual water volumes, based on seasons.  This enumeration should be used to
indicate common full-year definitions.  Year types that include only a season or part of the year could
specify this enumeration for full years and would otherwise need to define the year in some other way.
*/
public enum YearType
{
/**
Standard calendar year.
*/
CALENDAR ( "Calendar", 0, 1 ),
/**
November (of previous year) to October (of current) year.
*/
NOV_TO_OCT ( "NovToOct", -1, 11 ),
/**
Water year (October to September).
*/
WATER ( "Water", -1, 10 );

/**
The name that is used for choices and other technical code (terse).
*/
private final String __displayName;

/**
The calendar year offset in which the year starts.  For example, -1 indicates that the year starts
in the previous calendar year.
*/
private final int __yearOffset;

/**
The calendar month (1-12) when the year starts.  For example, 10 indicates that the year starts in October.
*/
private final int __firstMonth;

/**
Construct an enumeration value.
@param displayName name that should be displayed in choices, etc.
@param yearOffset the offset to the calendar year for the start of the year.
@param firstMonth the first calendar month (1-12) for the year type.
*/
private YearType(String displayName, int yearOffset, int firstMonth ) {
    this.__displayName = displayName;
    this.__yearOffset = yearOffset;
    this.__firstMonth = firstMonth;
}

/**
Return the first month in the year.
@return the first month in the year.
*/
public int getFirstMonth ()
{
    return __firstMonth;
}

/**
Get the list of year types.
@return the list of year types.
*/
public static List<YearType> getYearTypeChoices()
{
    List<YearType> choices = new Vector();
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
    List<String> stringChoices = new Vector();
    for ( int i = 0; i < choices.size(); i++ ) {
        YearType choice = choices.get(i);
        String choiceString = "" + choice;
        stringChoices.add ( choiceString );
    }
    return stringChoices;
}

/**
Return the year offset.
@return the year offset.
*/
public int getYearOffset ()
{
    return __yearOffset;
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
{
    YearType [] values = values();
    for ( YearType t : values ) {
        if ( name.equalsIgnoreCase(t.toString())) {
            return t;
        }
    }
    throw new IllegalArgumentException ( "The following does not match a valid year type: \"" + name + "\"");
}

}