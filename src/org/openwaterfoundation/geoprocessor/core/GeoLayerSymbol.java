// GeoLayerSymbol - spatial data layer symbol

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

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Layer symbol.
 */
public class GeoLayerSymbol implements Cloneable {

	/**
	 * Name for the symbol.
	 */
	private String name = "";
	
	/**
	 * Description for the symbol.
	 */
	private String description = "";
	
	/**
	 * Symbol classification type.
	 */
	private GeoLayerSymbolClassificationType classificationType = GeoLayerSymbolClassificationType.UNKNOWN;
	
	/**
	 * Classification attribute.
	 */
	private String classificationAttribute = "";

	/**
	 * List of properties for the symbol, sorted alphabetically by the property (key) name.
	 */
	private TreeMap<String,Object> properties = new TreeMap<>();
	
	/**
	 * Constructor needed for deserialization.
	 */
	public GeoLayerSymbol () {
	}

	/**
	 * Constructor.
	 * @param name symbol name
	 * @param description symbol description
	 * @param properties symbol properties
	 */
	public GeoLayerSymbol ( String name, String description, SortedMap<String,Object> properties ) {
		this.name = name;
		this.description = description;
	}

	/**
	Clone the symbol object.
	*/
	public Object clone () {
		try {
			// Clone the base class (Object).
        	GeoLayerSymbol layerSymbol = (GeoLayerSymbol)super.clone();
        	// Primitives will automatically be cloned.
        	return layerSymbol;
		}
		catch ( CloneNotSupportedException e ) {
			// Should not happen because everything is clone-able.
			throw new InternalError();
		}
	}

	/**
	 * Return the classification attribute.
	 * @return the classification attribute
	 */
	public String getClassificationAttribute () {
		return this.classificationAttribute;
	}

	/**
	 * Return the classification type.
	 * @return the classification type
	 */
	public GeoLayerSymbolClassificationType getClassificationType () {
		return this.classificationType;
	}

	/**
	 * Return the description.
	 * @return the description
	 */
	public String getDescription () {
		return this.description;
	}
	
	/**
	 * Return the name.
	 * @return the name.
	 */
	public String getName () {
		return this.name;
	}

	/**
	 * Return the the property matching the name, or null if not found.
	 * @param name the property name to match
	 * @return the property matching the name
	 */
	public Object getProperty ( String name ) {
		return this.properties.get(name);
	}
	
	/**
	 * Set the classification attribute.
	 * @param classificationAttribute the classification attribute.
	 */
	public void setClassificationAttribute ( String classificationAttribute ) {
		this.classificationAttribute = classificationAttribute;
	}

	/**
	 * Set the classification type.
	 * @param classificationType the classification type.
	 */
	public void setClassificationType ( GeoLayerSymbolClassificationType classificationType ) {
		this.classificationType = classificationType;
	}

}