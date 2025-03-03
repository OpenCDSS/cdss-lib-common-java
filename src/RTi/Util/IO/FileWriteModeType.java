// FileWriteModeType - mode for writing files, intended to be used as needed, but currently with no tight bundling to other code

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
Mode for writing files, intended to be used as needed, but currently with no tight bundling to other code.
*/
public enum FileWriteModeType
{
	/**
	Append to the end of the file.
	*/
	APPEND("Append"),

	/**
	Overwrite the file with the new content.
	*/
	OVERWRITE("Overwrite"),

	/**
	Update the file contents by updating overlapping data and appending the rest.
	*/
	UPDATE("Update");

	/**
 	* The name that should be displayed when the file type is used in UIs and reports.
 	*/
	private final String displayName;

	/**
 	* Construct a write mode enumeration value.
 	* @param displayName name that should be displayed in choices, etc.
 	*/
	private FileWriteModeType(String displayName) {
    	this.displayName = displayName;
	}

	/**
 	* Return the display name for the write mode.
 	* This is usually the same as the value but using appropriate mixed case.
 	* @return the display name.
 	*/
	@Override
	public String toString() {
    	return displayName;
	}

	/**
 	* Return the enumeration value given a string name (case-independent), ignoring case.
 	* @param name the name to evaluate to return the corresponding enumeration
 	* @return the enumeration value given a string name (case-independent), or null if not matched.
 	*/
	public static FileWriteModeType valueOfIgnoreCase ( String name ) {
	    if ( name == null ) {
        	return null;
    	}
    	FileWriteModeType [] values = values();
    	for ( FileWriteModeType t : values ) {
        	if ( name.equalsIgnoreCase(t.toString()) ) {
            	return t;
        	}
    	}
    	return null;
	}

}