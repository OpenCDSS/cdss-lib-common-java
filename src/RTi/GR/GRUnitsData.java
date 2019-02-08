// GRUnitsData - table to convert between units

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

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
