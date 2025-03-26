// DataUnitsConversion - data units conversion class

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package RTi.Util.IO;

/**
The DataUnitsConversion class stores the conversion factors for changing from
one set of units to another.  An instance is returned from
DataUnits.getConversion.
The DataUnits.getConversion() method normally initializes some of the fields in
this class.  Currently, there are no plans to move getConversion() to this class
because that function depends on DataUnits data.
@see DataUnits#getConversion
*/
public class DataUnitsConversion
{

private double	_add_factor;		// Factor to add to convert from _original_units to _new_units.
private double	_mult_factor;		// Factor to multiply by to convert from _original_units to _new_units.
private String	_original_units;	// The original data units.
private String	_new_units;		// The new data units (result of applying the factors).

/**
Construct and set the multiplication factor to 1.0 and the add factor to 0.0.
*/
public DataUnitsConversion ( )
{	initialize ();
}

/**
Construct using data values.
Construct using data values.  The add factor is set to zero.
@param original_units Units before conversion.
@param new_units Units after conversion.
@param mult_factor Factor to multiply old units by to get new units.
@param add_factor Factor to add old units (after multiplication) to get
new units.
*/
public DataUnitsConversion (	String original_units, String new_units,
				double mult_factor, double add_factor )
{	initialize ();
	setOriginalUnits ( original_units );
	setNewUnits ( new_units );
	_mult_factor = mult_factor;
	_add_factor = add_factor;
}

/**
Construct using data values.  The add factor is set to zero.
@param original_units Units before conversion.
@param new_units Units after conversion.
@param mult_factor Factor to multiply old units by to get new units.
*/
public DataUnitsConversion (	String original_units, String new_units,
				double mult_factor )
{	initialize ();
	setOriginalUnits ( original_units );
	setNewUnits ( new_units );
	_mult_factor = mult_factor;
	_add_factor = 0.0;
}

/**
Copy constructor.
@param conv DataUnitsConversion instance to copy.
*/
public DataUnitsConversion ( DataUnitsConversion conv )
{	initialize();
	setAddFactor ( conv._add_factor );
	setMultFactor ( conv._mult_factor );
	setOriginalUnits ( conv._original_units );
	setNewUnits ( conv._new_units );
}

/**
Return the factor to add by to perform the conversion.
@return The factor to add by to perform the conversion.
*/
public double getAddFactor ( )
{	return _add_factor;
}

/**
Return the factor to multiply by to perform the conversion.
@return The factor to multiply by to perform the conversion.
*/
public double getMultFactor ( )
{	return _mult_factor;
}

/**
Return the new units (after conversion).
@return The new units (after conversion).
*/
public String getNewUnits ( )
{	return _new_units;
}

/**
Return the original units (before conversion).
@return The original units (before conversion).
*/
public String getOriginalUnits ( )
{	return _original_units;
}

/**
Initialize data members.
*/
private void initialize ()
{	_original_units = "";
	_new_units = "";
	_add_factor = 0.0;	// Results in no conversion.
	_mult_factor = 1.0;
}

/**
Set the addition factor for the conversion (this is normally only needed for
temperature conversions).
@param add_factor The addition factor.
*/
public void setAddFactor ( double add_factor )
{	_add_factor = add_factor;
}

/**
Set the multiplication factor for the conversion.
@param mult_factor The multiplication factor.
*/
public void setMultFactor ( double mult_factor )
{	_mult_factor = mult_factor;
}

/**
Set the new units (after conversion).
@param new_units Data units after conversion.
*/
public void setNewUnits ( String new_units )
{	if ( new_units == null ) {
		return;
	}
	_new_units = new_units;
}

/**
Set the original units (before conversion).
@param original_units Data units before conversion.
*/
public void setOriginalUnits ( String original_units )
{	if ( original_units == null ) {
		return;
	}
	_original_units = original_units;
}

/**
Return a string representation of the units (verbose output).
@return A string representation of the units (verbose output).
*/
public String toString ()
{	return
	"Conv:  \"" + _original_units + "\" to \"" +
	_new_units + "\", MultBy:" + _mult_factor + ", Add:" + _add_factor;
}

} // End of DataUnitsConversion
