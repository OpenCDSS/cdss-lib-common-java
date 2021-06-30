// CommandFileOrderType - this class provides an enumeration of possible command file @order values.  

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

package RTi.Util.IO;

/**
This class provides an enumeration of possible command status values.  
*/
public enum CommandFileOrderType {
	
	/**
	 * Command file should be before a command file.
	 */
    AFTER("after"),
	/**
	 * Command file should be before a command file.
	 */
    BEFORE("before");
    
	/**
	 * Type name.
	 */
	private String typename;
	
	/**
	 * Construct the status type using the type/severity and name.  It is
	 * private because other code should use the predefined instances.
	 * @param type
	 * @param typename
	 */
	private CommandFileOrderType ( String typename ){
		this.typename = typename;
	}
	
	/**
	 * Return a String representation of the command status.
	 * @return a String representation of the command status.
	 */
	public String toString () {
		return this.typename;
	}

	/**
 	* Return the enumeration value given a string name (case-independent).
 	* @param name the type name to match in enumeration names (e.g., "before")
 	* @return the enumeration value given a string name (case-independent), or null if not matched.
 	*/
	public static CommandFileOrderType valueOfIgnoreCase(String name) {
	    if ( (name == null) || name.isEmpty() ) {
        	return null;
    	}
    	CommandFileOrderType [] values = values();
    	for ( CommandFileOrderType t : values ) {
        	if ( name.equalsIgnoreCase(t.toString()) ) {
            	return t;
        	}
    	} 
    	return null;
	}
}