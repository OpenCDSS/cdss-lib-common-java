// ----------------------------------------------------------------------------
// GRUnitsData - Table to convert between units
// ----------------------------------------------------------------------------
// GRConvertUnits - convert a value from one set of units to another
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 12 Sep 1996  Steven A. Malers, RTi   Split code out of GRUtil.c file.
// ----------------------------------------------------------------------------
// 2003-05-08	J. Thomas Sapienza, RTi	Made changes following SAM's review.
// 2005-04-26	JTS, RTi		Added finalize().
// ----------------------------------------------------------------------------

package RTi.GR;

/**
Table for converting between data units.
TODO (SAM - 2003-05-08) What is the base unit?  Code is rarely if ever used.
*/
public class GRUnitsData
{

public int code;
public double conversionFactor;
public String abbreviation;

public GRUnitsData ( int code0, double conversionFactor0, String abbreviation0 )
{
	conversionFactor = conversionFactor0;
	code = code0;
	abbreviation = new String ( abbreviation0 );
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	abbreviation = null;
	super.finalize();
}

}
