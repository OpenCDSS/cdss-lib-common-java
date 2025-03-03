// PropertyFileFormatType - format type for a property file,
// intended to be used as needed, but currently with no tight bundling to other code

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
Format type for a property file, intended to be used as needed,
but currently with no tight bundling to other code.
For example, use the types for an applications configuration file.
*/
public enum PropertyFileFormatType
{

	// TODO SAM 2012-07-27 Evaluate adding JSON, XML, and CSV.
	/**
	Format of properties is an INI file:

	<pre>
	[Section]
	property=value
	</pre>

	The code should provide the option of reading only property names or Section.PropertyName for uniqueness.
	*/
	INI("INI"),

	/**
	Format of properties is PropertyName=Value, using double quotes for the value if necessary.
	*/
	NAME_VALUE("NameValue"),

	/**
	Format of properties is PropertyName=Type(Value), using double quotes for the value if necessary.
	Type is only used as appropriate to remove ambiguity of parsing to strings,
	for example DateTime("2010-01-15").
	*/
	NAME_TYPE_VALUE("NameTypeValue"),

	/**
	Format of properties is the same as NAME_TYPE_VALUE except that objects are formatted to be consistent with Python,
	which allows the property file to be used directly in Python to assign variables.
	*/
	NAME_TYPE_VALUE_PYTHON("NameTypeValuePython"),

	/**
	The file contains the property value and the name must be supplied by other code.
	*/
	VALUE("Value");

	/**
	 * The name that should be displayed when the format type is used in UIs and output.
	 */
	private final String displayName;

	/**
	 * Construct an enumeration value.
	 * @param displayName name that should be displayed in choices, etc.
	 */
	private PropertyFileFormatType(String displayName) {
	    this.displayName = displayName;
	}

	/**
	 * Return the display name for the enumeration.
	 * This is usually the same as the value but using appropriate mixed case.
	 * @return the display name.
	 */
	@Override
	public String toString() {
	    return displayName;
	}

	/**
	 * Return the enumeration value given a string name (case-independent).
	 * @param name the enumeration name to match
	 * @return the enumeration value given a string name (case-independent), or null if not matched.
	 */
	public static PropertyFileFormatType valueOfIgnoreCase(String name) {
	    if ( name == null ) {
	        return null;
	    }
	    PropertyFileFormatType [] values = values();
	    for ( PropertyFileFormatType t : values ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    }
	    return null;
	}

}