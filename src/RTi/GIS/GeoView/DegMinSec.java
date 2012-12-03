package RTi.GIS.GeoView;

/**
Class to hold degrees, minutes, and seconds, in particular useful for conversion from decimal degrees in a way
that holds discrete values.
*/
public class DegMinSec
{

/**
Degrees as integer.
*/
private int __deg = 0;

/**
Minutes as integer.
*/
private int __min = 0;

/**
Seconds as double because remainder is 100s of second.
*/
private double __sec = 0.0;

/**
Decimal degrees from original data, if available.
*/
private Double __decdeg = null;

/**
Construct from parts.
*/
public DegMinSec ( int deg, int min, double sec )
{
    __deg = deg;
    __min = min;
    __sec = sec;
}

/**
Construct from decimal degrees.
@param decdeg decimal degrees to parse
@return new DegMinSec instance corresponding to specified decimal degrees
*/
public DegMinSec ( double decdeg )
{
    parseDecdeg ( decdeg, this );
    __decdeg = new Double(decdeg);
}

/**
Return the decimal degrees corresponding to the instance.
*/
public double getDecDegrees ()
{
    if ( __decdeg != null ) {
        // Have the value from the constructor
        return __decdeg;
    }
    else {
        // Calculate from the parts
        double decdeg = __deg + __min/60.0 + __sec/3600.0;
        return decdeg;
    }
}

/**
Return the degrees.
*/
public int getDeg ()
{
    return __deg;
}

/**
Return the minutes.
*/
public int getMin ()
{
    return __min;
}

/**
Return the seconds.
*/
public double getSec ()
{
    return __sec;
}

/**
Create from a string.
*/
public static DegMinSec parseDegMinSec ( String dms, DegMinSecFormatType format )
{
    DegMinSec dmso = new DegMinSec(0,0,0.0);
    if ( format == DegMinSecFormatType.DEGMMSS ) {
        // Format is DegMMSS with no partial seconds.  Degrees can be 2 or 3 digits, zero padded.
        int len = dms.length();
        String ss = dms.substring(len - 2);
        String mm = dms.substring(len - 4,len-2);
        String dd = dms.substring(0,len-4);
        dmso.__deg = Integer.parseInt(dd);
        dmso.__min = Integer.parseInt(mm);
        dmso.__sec = Double.parseDouble(ss);
    }
    return dmso;
}

/**
Create from decimal degrees.  The seconds are not truncated/rounded.
@param decdeg decimal degrees
@param dms if non-null, use the object to set the parsed values (useful to improve performance by not
creating a new object); if null, return a new object
@return DegMinSec object corresponding to decimal degrees
*/
public static DegMinSec parseDecdeg ( double decdeg, DegMinSec dms )
{
    if ( dms == null ) {
        dms = new DegMinSec(0,0,0.0);
    }
    dms.__deg = (int)decdeg;
    int frac = (int)((decdeg*3600.0)%3600.0);
    dms.__min = frac/60;
    dms.__sec = frac%60.0;
    return dms;
}

/**
Return a string representation of the object.
@param format the format to use for the string:
<ol>
<li>    DEGMMSS corresponds to "DegMSS", where minutes and seconds
        are padded with zeros.  Fractions of seconds are truncated (no attempt to round).
        </li>
*/
public String toString ( DegMinSecFormatType format )
{
    if ( format == DegMinSecFormatType.DEGMMSS ) {
        StringBuffer b = new StringBuffer("");
        // Do not round the arc-seconds because there is no way to go the other direction with
        // only 4 decimals of output.
        int deg = getDeg();
        b.append ( "" + (int)deg);
        String min = "" + getMin();
        if ( min.length() == 1 ) {
            min = "0" + min;
        }
        b.append ( min );
        String sec = "" + (int)getSec();
        if ( sec.length() == 1 ) {
            sec = "0" + sec;
        }
        b.append ( sec );
        return b.toString();
    }
    else {
        return "";
    }
}

}