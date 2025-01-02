// GeoLayerViewGroup - group for data layer views

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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Map layer group.
 */
public class GeoLayerViewGroup implements Cloneable {

	/**
	 * Unique identifier for the layer view group.
	 */
	private String geoLayerViewGroupId = "";
	
	/**
	 * Name for the layer view.
	 */
	private String name = "";
	
	/**
	 * Description for the layer view.
	 */
	private String description = "";
	
	/**
	 * List of properties for the layer view group, sorted alphabetically by the property (key) name.
	 */
	private SortedMap<String,Object> properties = new TreeMap<>();
	
	/**
	 * List of layer views in the group.
	 */
	private List<GeoLayerView> geoLayerViews = new ArrayList<>();
	
	/**
	 * Constructor.
	 */
	public GeoLayerViewGroup () {
	}
	
	/**
	 * Constructor.
	 * @param geoLayerViewGroupId GeoLayerViewGroup identifier
	 * @param name group name
	 * @param description group description
	 * @param properties group properties
	 */
	public GeoLayerViewGroup ( String geoLayerViewGroupId, String name, String description, SortedMap<String,Object> properties ) {
		this.geoLayerViewGroupId = geoLayerViewGroupId;
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
	 * Add a layer view to the group.
	 * @param geoLayerView the layer view to add to the group
	 */
	public void addGeoLayerView ( GeoLayerView geoLayerView ) {
		this.geoLayerViews.add ( geoLayerView );
	}

	/**
	Clone the layer view group object.
	*/
	public Object clone () {
		try {
			// Clone the base class (Object).
        	GeoLayerViewGroup layerViewGroup = (GeoLayerViewGroup)super.clone();
        	// Primitives like 'name' will be automatically cloned.
        	// Clone the layer views.
        	if ( this.geoLayerViews == null ) {
        		layerViewGroup.geoLayerViews = null;
        	}
        	else {
        		layerViewGroup.geoLayerViews = new ArrayList<>();
        		for ( GeoLayerView layerView : this.geoLayerViews ) {
        			layerViewGroup.geoLayerViews.add ( (GeoLayerView)layerView.clone() );
        		}
        	}
        	// Return the cloned object.
        	return layerViewGroup;
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
	 * Return the layer view matching the given identifier.
	 * @param geoLayerViewId the layer view identifier
	 * @return the matching layer view in the group, or null if not found
	 */
	public GeoLayerView getLayerViewForLayerViewId ( String geoLayerViewId ) {
		if ( geoLayerViewId == null ) {
			// No layer view ID was provided.
			return null;
		}
		for ( GeoLayerView geoLayerView : this.geoLayerViews ) {
			if ( geoLayerView.getGeoLayerViewId().equals(geoLayerViewId) ) {
				return geoLayerView;
			}
		}
		// Not found.
		return null;
	}

	/**
	 * Return the identifier.
	 * @return the identifier
	 */
	public String getGeoLayerViewGroupId() {
		return this.geoLayerViewGroupId;
	}

	/**
	 * Return the list of views.
	 * @return the list of views
	 */
	public List<GeoLayerView> getGeoLayerViews() {
		return this.geoLayerViews;
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
	
}