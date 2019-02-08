// TSDataFlagVisualizationType - indicate how data flags should be visualized in table displays

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

package RTi.GRTS;

/**
Indicate how data flags should be visualized in table displays
*/
public enum TSDataFlagVisualizationType
{
    /**
    Data flags are not shown.
    */
    NOT_SHOWN("Not shown"),
    /**
    Data flags are shown in separate column of table.
    */
    SEPARATE_COLUMN("Separate column"),
    /**
    Data flags are shown as superscript on data values.
    */
    SUPERSCRIPT("Superscript");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSDataFlagVisualizationType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the display name.
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
    public static TSDataFlagVisualizationType valueOfIgnoreCase(String name)
    {
        if ( name == null ) {
            return null;
        }
        TSDataFlagVisualizationType [] values = values();
        for ( TSDataFlagVisualizationType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}
