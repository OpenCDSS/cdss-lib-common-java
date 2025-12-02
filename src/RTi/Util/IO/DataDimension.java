// DataDimension - data dimension class

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

import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;

/**
The DataDimension class stores data dimension data and provides methods to interpret and use such data.
Data dimensions (e.g., "L" for length,
"L/T" for discharge) are primarily used when determining units conversions or labels for output.
Standard dimensions that have been used in RTi software include:
<pre>
DIRECTION (e.g., degrees).
CONSTANT
ENERGY
ENERGY_PER_AREA
POWER
LENGTH
SPEED
AREA
VOLUME
DISCHARGE
PRESSURE
TEMPERATURE
TIME
<pre>
*/
public class DataDimension
{

// Private static data members for object house-keeping.

private static List<DataDimension> __dimensionList = new Vector<DataDimension>(20);

// Data members.

// Abbreviation for dimension.  This is used in data units files to group units by dimension.  Example: "L".
private String	__abbreviation;
// Long name for dimension (e.g., "LENGTH).
private String 	__long_name;

/**
Construct using primitive data.
@param abbreviation the abbreviation to use
@param long_name the long_name to use
*/
public DataDimension ( String abbreviation, String long_name ) {
	setAbbreviation ( abbreviation );
	setLongName ( long_name );
}

/**
Copy constructor.
@param dim DataDimension to copy.
*/
public DataDimension ( DataDimension dim ) {
	this ( dim.getAbbreviation(), dim.getLongName() );
}

/**
Add a DataDimension to the internal list of dimensions.  After adding, the dimensions can be used throughout the application.
@param dim Instance of DataDimension to add to the list.
*/
public static void addDimension ( DataDimension dim ) {
	// First see if the dimension is already in the list.

	int size = __dimensionList.size();
	DataDimension pt = null;
	for ( int i = 0; i < size; i++ ) {
		// Get the dimension for the loop index.
		pt = (DataDimension)__dimensionList.get(i);
		// Now compare...
		if ( dim.getAbbreviation().equalsIgnoreCase( pt.getAbbreviation() ) ) {
			// The requested dimension matches something that is already in the list.  Reset the list.
			__dimensionList.set ( i, new DataDimension (dim) );
			return;
		}
	}
	// Need to add the units to the list.
	__dimensionList.add ( new DataDimension(dim) );
}

/**
Return the dimension abbreviation.
@return The dimension abbreviation.
*/
public String getAbbreviation ( ) {
	return __abbreviation;
}

/**
Return a list of DataDimension containing the static shared dimension data.
@return a list of DataDimension containing the static shared dimension data.
*/
public static List<DataDimension> getDimensionData () {
	return __dimensionList;
}

/**
Return the dimension long name.
@return The dimension long name.
*/
public String getLongName ( ) {
	return __long_name;
}

/**
Lookup a DataDimension given the dimension string abbreviation.
@return DataDimension given the dimension string abbreviation.
@param dimension_string Dimension abbreviation string.
@exception Exception If the data dimension cannot be determined from the string.
@deprecated Use lookupDimension
*/
@Deprecated
public static DataDimension lookup ( String dimension_string )
throws Exception {
	return lookupDimension ( dimension_string );
}

/**
Lookup a DataDimension given the dimension string abbreviation.
@return DataDimension given the dimension string abbreviation.
@param dimension_string Dimension abbreviation string.
@exception Exception If the data dimension cannot be determined from the string.
*/
public static DataDimension lookupDimension ( String dimension_string )
throws Exception {
	if ( dimension_string == null ) {
		throw new Exception ( "Null dimension string" );
	}
	if ( dimension_string.length() <= 0 ) {
		throw new Exception ( "Empty dimension string" );
	}

	int size = __dimensionList.size();
	DataDimension dim = null;
	for ( int i = 0; i < size; i++ ) {
		dim = __dimensionList.get(i);
		if ( dimension_string.equalsIgnoreCase( dim.getAbbreviation())){
			// Have a match.
			return dim;
		}
	}
	// Unable to find.
	String message = "Unable to look up dimension \"" + dimension_string + "\"";
	Message.printWarning ( 2, "DataDimension.lookupDimension", message );
	throw new Exception ( message );
}

/**
Set the dimension abbreviation.
@param abbreviation The dimension abbreviation.
*/
public void setAbbreviation ( String abbreviation ) {
	if ( abbreviation == null ) {
		return;
	}
	__abbreviation = abbreviation;
}

/**
Set the dimension long name.
@param long_name The dimension long name.
*/
public void setLongName ( String long_name ) {
	if ( long_name == null ) {
		return;
	}
	__long_name = long_name;
}

/**
Return a string representation of the DataDimension.
@return A string representation of the DataDimension.
*/
public String toString () {
	return "Dimension:  \"" + __abbreviation + "\", \"" + __long_name +"\"";
}

}