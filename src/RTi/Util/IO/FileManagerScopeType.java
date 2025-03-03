// FileWriteScopeType - the scope for a FileManager file

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

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Scope type, to indicate the component that a FileManager file is related to.
*/
public enum FileManagerScopeType {

	/**
	The scope is the application.
	*/
	APPLICATION("Application"),

	/**
	The scope is a command.
	*/
	COMMAND("Command"),

	/**
	The scope is a plugin (not datastore or command).
	*/
	PLUGIN("Plugin");

	/**
 	* The name that should be displayed when the component scope is used in UIs and reports.
 	*/
	@JsonValue
	private final String displayName;

	/**
 	* Construct an enumeration value.
 	* @param displayName name that should be displayed in choices, etc.
 	*/
	private FileManagerScopeType ( String displayName ) {
    	this.displayName = displayName;
	}

	/**
 	* Return the display name for the scope type.
 	* This is usually the same as the value but using appropriate mixed case.
 	* @return the display name.
 	*/
	@Override
	public String toString() {
    	return displayName;
	}

	/**
 	* Return the enumeration value given a string name (case-independent).
 	* @return the enumeration value given a string name (case-independent), or null if not matched.
 	*/
	public static FileManagerScopeType valueOfIgnoreCase ( String name ) {
	    if ( name == null ) {
        	return null;
    	}
    	FileManagerScopeType [] values = values();
    	for ( FileManagerScopeType t : values ) {
        	if ( name.equalsIgnoreCase(t.toString()) ) {
            	return t;
        	}
    	} 
    	return null;
	}

}