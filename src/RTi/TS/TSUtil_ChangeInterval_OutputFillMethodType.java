package RTi.TS;

import java.util.List;
import java.util.Vector;

/**
To be used with the TSUtil_ChangeInterval class.  Used when converting from INST to MEAN time
series going from larger to smaller time interval.
*/
public enum TSUtil_ChangeInterval_OutputFillMethodType
{
/**
Required intervening values are interpolated from end-points of longer interval.
*/
INTERPOLATE ( "Interpolate" ),
/**
Required intervening values are repeated from previous values.
*/
REPEAT ( "Repeat" );

/**
The name that is used for choices and other technical code (terse).
*/
private final String displayName;

/**
Construct an output fill method enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private TSUtil_ChangeInterval_OutputFillMethodType(String displayName) {
    this.displayName = displayName;
}

/**
Get the list of time scales.
@return the list of time scales.
*/
public static List<TSUtil_ChangeInterval_OutputFillMethodType> getOutputFillMethodChoices()
{
    List<TSUtil_ChangeInterval_OutputFillMethodType> choices = new Vector();
    choices.add ( TSUtil_ChangeInterval_OutputFillMethodType.INTERPOLATE );
    choices.add ( TSUtil_ChangeInterval_OutputFillMethodType.REPEAT );
    return choices;
}

/**
Get the list of output fill methods.
@return the list of output fill methods as strings.
*/
public static List<String> getOutputFillMethodChoicesAsStrings( )
{
    List<TSUtil_ChangeInterval_OutputFillMethodType> choices = getOutputFillMethodChoices();
    List<String> stringChoices = new Vector();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
}

/**
Return the short display name for the output fill method.  This is the same as the value.
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
public static TSUtil_ChangeInterval_OutputFillMethodType valueOfIgnoreCase (String name)
{
    TSUtil_ChangeInterval_OutputFillMethodType [] values = values();
    for ( TSUtil_ChangeInterval_OutputFillMethodType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    }
    throw new IllegalArgumentException ( "The following does not match a output fill method: \"" + name + "\"");
}

}