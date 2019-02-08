// GRPointConnectType - enumeration of lines to connect points

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

package RTi.GR;

/**
Ways to connect points when drawing, needed for example to represent instantaneous values, averages, etc.
Handling of missing values with gaps is expected to occur as appropriate but is not indicated by this type.
*/
public enum GRPointConnectType
{
    /**
     * Connect points.
     */
    CONNECT("Connect"),
    
    /**
     * Step-function with line drawn backward from the point (point's y-coordinate is end of step). 
     */
    STEP_BACKWARD("StepBackward"),
    
    /**
     * Step-function with line drawn forward from the point (point's y-coordinate is start of step). 
     */
    STEP_FORWARD("StepForward");
    
    /**
     * The string name that should be displayed.
     */
    private final String displayName;
    
    /**
     * Construct a point connect type enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private GRPointConnectType(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Equals method to prevent common programming error of using the equals method instead of ==.
     */
    public boolean equals ( String arrowStyleType ) {
        if ( arrowStyleType.equalsIgnoreCase(this.displayName) ) {
            return true;
        }
        else {
            return false;
        }
    }
	
    /**
     * Return the display name for the arrow style type.  This is usually the same as the
     * value but using appropriate mixed case.
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
	public static GRPointConnectType valueOfIgnoreCase(String name)
	{
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values
	    for ( GRPointConnectType t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    } 
	    return null;
	}
}
