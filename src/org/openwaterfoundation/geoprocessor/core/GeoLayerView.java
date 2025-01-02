// GeoLayerView - spatial data layer view configuration

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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Map layer.
 */
public class GeoLayerView implements Cloneable {

	/**
	 * Unique identifier for the layer view.
	 */
	private String geoLayerViewId = "";
	
	/**
	 * Name for the layer view.
	 */
	private String name = "";
	
	/**
	 * Description for the layer view.
	 */
	private String description = "";
	
	/**
	 * List of properties for the layer view, sorted alphabetically by the property (key) name.
	 */
	private SortedMap<String,Object> properties = new TreeMap<>();
	
	/**
	 * GeoLayer ID, used to associate a layer with the view.
	 */
	private String geoLayerId = "";
	
	/**
	 * Symbol to use for the layer view.
	 */
	GeoLayerSymbol geoLayerSymbol = null;
	
	/**
	 * Constructor.
	 */
	public GeoLayerView () {
	}

	/**
	 * Constructor.
	 * @param geoLayerViewId GeoLayerView identifier
	 * @param name view name
	 * @param description view description
	 * @param properties view properties
	 */
	public GeoLayerView ( String geoLayerViewId, String name, String description, SortedMap<String,Object> properties ) {
		this.geoLayerViewId = geoLayerViewId;
		this.name = name;
		this.description = description;
		if ( properties != null ) {
			this.properties = properties;
		}
		else {
			// Use the default empty map.
		}
	}

	/**
	Clone the layer view group object.
	*/
	public Object clone () {
		try {
			// Clone the base class (Object).
        	GeoLayerView layerView = (GeoLayerView)super.clone();
        	// Primitives like 'name' will be automatically cloned.
        	// Clone the layer views.
        	if ( this.geoLayerSymbol == null ) {
        		layerView.geoLayerSymbol = null;
        	}
        	else {
        		layerView.geoLayerSymbol = (GeoLayerSymbol)this.geoLayerSymbol.clone();
        	}
        	// Return the cloned object.
        	return layerView;
		}
		catch ( CloneNotSupportedException e ) {
			// Should not happen because everything is clone-able.
			throw new InternalError();
		}
	}

	/**
	 * Return the description.
	 * @return the description
	 */
	public String getDescription () {
		return this.description;
	}

	/**
	 * Return the layer identifier.
	 * @return the layer identifier
	 */
	public String getGeoLayerId() {
		return this.geoLayerId;
	}

	/**
	 * Return the layer symbol.
	 * @return the layer symbol
	 */
	public GeoLayerSymbol getGeoLayerSymbol() {
		return this.geoLayerSymbol;
	}

	/**
	 * Return the view identifier.
	 * @return the view identifier
	 */
	public String getGeoLayerViewId() {
		return this.geoLayerViewId;
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
	 * Return the the properties.
	 * @return the property list
	 */
	public Map<String,Object> getProperties () {
		return this.properties;
	}
	
	/**
	 * Set the layer ID used by the layer view.
	 * @param geoLayerId the layer ID used for the layer view
	 */
	public void setGeoLayerId ( String geoLayerId ) {
		this.geoLayerId = geoLayerId;
	}

	/**
	 * Set the symbol used by the layer view.
	 * @param symbol the symbol used for the layer view
	 */
	public void setGeoLayerSymbol ( GeoLayerSymbol geoLayerSymbol ) {
		this.geoLayerSymbol = geoLayerSymbol;
	}
}