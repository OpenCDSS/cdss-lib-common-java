// GeoLayerSymbolClassificationType - classification types for layer symbols

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

import com.fasterxml.jackson.annotation.JsonValue;

/**
Map project types.
*/
public enum GeoLayerSymbolClassificationType implements Cloneable {
    /**
     * Categorized - categories (bins, ranges) of values
     */
    CATEGORIZED("Categorized"),

    /**
     * Graduated - a ramp of values
     */
    GRADUATED("Graduated"),

    /**
     * Single symbol - used for all features
     */
    SINGLE_SYMBOL("SingleSymbol"),

    /**
     * Unknown - the symbol type is unknown (used during initialization).
     */
    UNKNOWN("Unknown");

    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private GeoLayerSymbolClassificationType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the display name.
     * @return the display name.
     */
    @Override
    @JsonValue
    public String toString() {
        return displayName;
    }

    /**
     * Return the enumeration value given a string name (case-independent).
     * @return the enumeration value given a string name (case-independent), or null if not matched.
     */
    public static GeoLayerSymbolClassificationType valueOfIgnoreCase(String name) {
        if ( name == null ) {
            return null;
        }
        GeoLayerSymbolClassificationType [] values = values();
        for ( GeoLayerSymbolClassificationType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        }
        return null;
    }
}