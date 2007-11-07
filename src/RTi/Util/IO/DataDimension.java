// ----------------------------------------------------------------------------
// DataDimension - data dimension class
// ----------------------------------------------------------------------------
// History:
//
// 13 Jan 1998	Steven A. Malers, RTi	Initial version.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
//					Change some methods to have void return
//					type.
// 2003-05-23	SAM, RTi		* Remove the internal integer dimension.
//					  Dimension data is now being read from
//					  databases like RiversideDB and the
//					  hard-coded values are difficult to
//					  keep consistent.  Put alist of
//					  "standard" dimensions in the class
//					  header documentation, as a reference.
//					* Remove the default initialization
//					  method - the data should always be
//					  initialized externally from a database
//					  or file.
//					* Change private data to use __ in
//					  front, consistent with other RTi code.
//					* Deprecate lookup() in favor of
//					  lookupDimension().
// 2003-12-04	SAM, RTi		* Add getDimensionData().
// ----------------------------------------------------------------------------

package RTi.Util.IO;

import java.util.Vector;

import RTi.Util.Message.Message;

/**
The DataDimension class stores data dimension data and provides methods to
interpret and use such data.  Data dimensions (e.g., "L" for length,
"L/T" for discharge) are primarily used when determining units conversions or
labels for output.  Standard dimensions that have been used in RTi software
include:
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

// Private static data members for object house-keeping...

private static Vector __dimension_Vector = new Vector(20);

// Data members...

private String	__abbreviation;		// Abbreviation for dimension.  This is
					// used in data units files to group
					// units by dimension.  Example: "L"
private String 	__long_name;		// Long name for dimension (e.g.,
					// "LENGTH).

/**
Construct using primitive data.
@param abbreviation the abbreviation to use
@param long_name the long_name to use
*/
public DataDimension ( String abbreviation, String long_name )
{	setAbbreviation ( abbreviation );
	setLongName ( long_name );
}

/**
Copy constructor.
@param dim DataDimension to copy.
*/
public DataDimension ( DataDimension dim )
{	this ( dim.getAbbreviation(), dim.getLongName() );
}

/**
Add a DataDimension to the internal list of dimensions.  After adding, the
dimensions can be used throughout the application.
@param dim Instance of DataDimension to add to the list.
*/
public static void addDimension ( DataDimension dim )
{	// First see if the dimension is already in the list...

	int size = __dimension_Vector.size();
	DataDimension pt = null;
	for ( int i = 0; i < size; i++ ) {
		// Get the dimension for the loop index...
		pt = (DataDimension)__dimension_Vector.elementAt(i);
		// Now compare...
		if (	dim.getAbbreviation().equalsIgnoreCase(
			pt.getAbbreviation() ) ) {
			// The requested dimension matches something that is
			// already in the list.  Reset the list...
			__dimension_Vector.setElementAt (
			new DataDimension (dim), i );
			return;
		}
	}
	// Need to add the units to the list...
	__dimension_Vector.addElement ( new DataDimension(dim) );
	pt = null;
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	__abbreviation = null;
	__long_name = null;
	super.finalize();
}

/**
Return the dimension abbreviation.
@return The dimension abbreviation.
*/
public String getAbbreviation ( )
{	return __abbreviation;
}

/**
Return a Vector of DataDimension containing the static shared dimension data.
@return a Vector of DataDimension containing the static shared dimension data.
*/
public static Vector getDimensionData ()
{	return __dimension_Vector;
}

/**
Return the dimension long name.
@return The dimension long name.
*/
public String getLongName ( )
{	return __long_name;
}

/**
Lookup a DataDimension given the dimension string abbreviation.
@return DataDimension given the dimension string abbreviation.
@param dimension_string Dimension abbreviation string.
@exception Exception If the data dimension cannot be determined from the string.
@deprecated Use lookupDimension
*/
public static DataDimension lookup ( String dimension_string )
throws Exception
{	return lookupDimension ( dimension_string );
}

/**
Lookup a DataDimension given the dimension string abbreviation.
@return DataDimension given the dimension string abbreviation.
@param dimension_string Dimension abbreviation string.
@exception Exception If the data dimension cannot be determined from the string.
*/
public static DataDimension lookupDimension ( String dimension_string )
throws Exception
{	if ( dimension_string == null ) {
		throw new Exception ( "Null dimension string" );
	}
	if ( dimension_string.length() <= 0 ) {
		throw new Exception ( "Empty dimension string" );
	}
		
	int size = __dimension_Vector.size();
	DataDimension dim = null;
	for ( int i = 0; i < size; i++ ) {
		dim = (DataDimension)__dimension_Vector.elementAt(i);
		if ( dimension_string.equalsIgnoreCase( dim.getAbbreviation())){
			// Have a match...
			return dim;
		}
	}
	// Unable to find...
	String message = "Unable to look up dimension \"" + dimension_string +
	"\"";
	Message.printWarning ( 2, "DataDimension.lookupDimension", message );
	throw new Exception ( message );
}

/**
Set the dimension abbreviation.
@param abbreviation The dimension abbreviation.
*/
public void setAbbreviation ( String abbreviation )
{	if ( abbreviation == null ) {
		return;
	}
	__abbreviation = abbreviation;
}

/**
Set the dimension long name.
@param long_name The dimension long name.
*/
public void setLongName ( String long_name )
{	if ( long_name == null ) {
		return;
	}
	__long_name = long_name;
}

/**
Return a string representation of the DataDimension.
@return A string representation of the DataDimension.
*/
public String toString ()
{	return "Dimension:  \"" + __abbreviation + "\", \"" + __long_name +"\"";
}

} // End of DataDimension
