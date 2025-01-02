// GeoLayerGeometryType - geometry types for layer

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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;

/**
Layer types.
*/
public enum GeoLayerGeometryType {
	/**
	 * GeometryCollection
	 */
    GEOMETRY_COLLECTION("WKT:GeometryCollection"),

    /**
     * LineString.
     */
    LINESTRING("WKT:LineString"),

    /**
     * MultiLineString.
     */
    MULTI_LINESTRING("WKT:MultiLineString"),

    /**
     * MultiPoint.
     */
    MULTI_POINT("WKT:MultiPoint"),

    /**
     * MultiPolygon.
     */
    MULTI_POLYGON("WKT:MultiPolygon"),

    /**
     * Point.
     */
    POINT("WKT:Point"),

    /**
     * Polygon.
     */
    POLYGON("WKT:Polygon"),

    /**
     * Raster.
     */
    RASTER("Raster");


    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private GeoLayerGeometryType(String displayName) {
        this.displayName = displayName;
    }

	/**
	Get the list of enumerations in the requested order.
	@param alphabetical return alphabetical choices (true) or logical order (false)
	@return the list of command types.
	*/
	public static List<GeoLayerGeometryType> getChoices ( boolean alphabetical ) {
    	List<GeoLayerGeometryType> choices = new ArrayList<>();
    	if ( alphabetical ) {
    		// List in alphabetical order.
    		choices.add ( GeoLayerGeometryType.GEOMETRY_COLLECTION );
    		choices.add ( GeoLayerGeometryType.LINESTRING );
    		choices.add ( GeoLayerGeometryType.MULTI_LINESTRING );
    		choices.add ( GeoLayerGeometryType.MULTI_POINT );
    		choices.add ( GeoLayerGeometryType.MULTI_POLYGON );
    		choices.add ( GeoLayerGeometryType.POINT );
    		choices.add ( GeoLayerGeometryType.POLYGON );
    		choices.add ( GeoLayerGeometryType.RASTER );
    	}
    	else {
    		// List in logical order (simple to complex).
    		choices.add ( GeoLayerGeometryType.POINT );
    		choices.add ( GeoLayerGeometryType.LINESTRING );
    		choices.add ( GeoLayerGeometryType.POLYGON );
    		choices.add ( GeoLayerGeometryType.MULTI_POINT );
    		choices.add ( GeoLayerGeometryType.MULTI_LINESTRING );
    		choices.add ( GeoLayerGeometryType.MULTI_POLYGON );
    		choices.add ( GeoLayerGeometryType.GEOMETRY_COLLECTION );
    		choices.add ( GeoLayerGeometryType.RASTER );
    	}
    	return choices;
	}

	/**
	Get the list of command type as strings.
	@param alphabetical return alphabetical choices (true) or logical order (false)
	@param includeNote return choices with note (e.g., "Copy - copy a project") - currently not implemented.
	@return the list of command types as strings.
	*/
	public static List<String> getChoicesAsStrings ( boolean alphabetical, boolean includeNote ) {
    	List<GeoLayerGeometryType> choices = getChoices ( alphabetical );
    	List<String> stringChoices = new ArrayList<>();
    	for ( int i = 0; i < choices.size(); i++ ) {
        	GeoLayerGeometryType choice = choices.get(i);
        	String choiceString = "" + choice;
        	//if ( includeNote ) {
            //	choiceString = choiceString + " - " + choice.toStringVerbose();
        	//}
        	stringChoices.add ( choiceString );
    	}
    	return stringChoices;
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
    public static GeoLayerGeometryType valueOfIgnoreCase(String name) {
        if ( name == null ) {
            return null;
        }
        GeoLayerGeometryType [] values = values();
        for ( GeoLayerGeometryType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        }
        return null;
    }
}