// MapProjectType - map project types

/* NoticeStart

GeoView Java Library
GeoView Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 2017-2024 Open Water Foundation

GeoView Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

GeoView Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with GeoView Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.geoprocessor.core;

/**
Map project types.
*/
public enum GeoMapProjectType {
    /**
     * Dashboard - maps and other information products.
     */
    DASHBOARD("Dashboard"),

    /**
     * Grid - multiple maps shown in a 2D grid.
     */
    GRID("Grid"),

    /**
     * Single map - stand-alone map.
     */
    SINGLE_MAP("SingleMap"),

    /**
     * Story - a sequence of maps and visualizations.
     */
    STORY("Story"),

    /**
     * Unknown - the project type is unknown (used during initialization).
     */
    UNKNOWN("Unknown");

    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private GeoMapProjectType(String displayName) {
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
    public static GeoMapProjectType valueOfIgnoreCase(String name) {
        if ( name == null ) {
            return null;
        }
        GeoMapProjectType [] values = values();
        for ( GeoMapProjectType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        }
        return null;
    }
}