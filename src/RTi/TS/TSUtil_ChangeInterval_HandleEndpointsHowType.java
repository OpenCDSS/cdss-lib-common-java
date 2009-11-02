package RTi.TS;

import java.util.List;
import java.util.Vector;

/**
To be used with the TSUtil_ChangeInterval class, indicating how to handle end-points converting from
small to large interval, for interval smaller than daily.
*/
public enum TSUtil_ChangeInterval_HandleEndpointsHowType
{
/**
Average both end-points.
*/
AVERAGE_ENDPOINTS ( "AverageEndpoints" ),
/**
Only include the first end-point.
*/
INCLUDE_FIRST_ONLY( "IncludeFirstOnly" );

/**
The name that is used for choices and other technical code (terse).
*/
private final String displayName;

/**
Construct a time series statistic enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private TSUtil_ChangeInterval_HandleEndpointsHowType(String displayName) {
    this.displayName = displayName;
}

/**
Get the list of enumerations.
@return the list of enumerations.
*/
public static List<TSUtil_ChangeInterval_HandleEndpointsHowType> getHandleEndpointsHowChoices()
{
    List<TSUtil_ChangeInterval_HandleEndpointsHowType> choices = new Vector();
    choices.add ( TSUtil_ChangeInterval_HandleEndpointsHowType.AVERAGE_ENDPOINTS );
    choices.add ( TSUtil_ChangeInterval_HandleEndpointsHowType.INCLUDE_FIRST_ONLY );
    return choices;
}

/**
Get the list of enumerations as strings.
@return the list of enumerations strings.
*/
public static List<String> getHandleEndpointsHowChoicesAsStrings()
{
    List<TSUtil_ChangeInterval_HandleEndpointsHowType> choices = getHandleEndpointsHowChoices();
    List<String> stringChoices = new Vector();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
}

/**
Return the short display name for the statistic.  This is the same as the value.
@return the display name.
*/
@Override
public String toString() {
    return displayName;
}

/**
Return the enumeration value given a string name (case-independent).
@param name the time scale string to match.
@return the enumeration value given a string name (case-independent).
@exception IllegalArgumentException if the name does not match a valid time scale.
*/
public static TSUtil_ChangeInterval_HandleEndpointsHowType valueOfIgnoreCase (String name)
{
    TSUtil_ChangeInterval_HandleEndpointsHowType [] values = values();
    for ( TSUtil_ChangeInterval_HandleEndpointsHowType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    }
    throw new IllegalArgumentException (
        "The following does not match a recognized HandleEndpointsHow value: \"" + name + "\"");
}

}