// GeoMap - definition of a map

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
import java.util.SortedMap;
import java.util.TreeMap;

/**
    Map that when serialized to JSON will result in a map that can be used by other tools such as Leaflet map viewer.
    The organization of objects is:

    GeoMapProject                         # File that represents 1+ map's configuration.
    *   GeoMap []                         # File and object that represent a map's configuration.
            GeoLayer []                   # Shared list of layers, if maps can share for viewing (leaflet cannot share).
            GeoLayerViewGroup []          # Groups of layer views, as per typical GIS legend.
                GeoLayerView []           # A single map view that uses one or more layers.
                    GeoLayer              # ID will be used in output to reference above GeoLayer [].
                    GeoLayerSymbol        # Symbol configuration for the layer.
 */
public class GeoMap {

	/**
	 * Unique identifier for the map.
	 */
	private String geoMapId = "";
	
	/**
	 * Name for the project.
	 */
	private String name = "";
	
	/**
	 * Description for the project.
	 */
	private String description = "";
	
	/**
	 * List of properties for the project, sorted alphabetically by the property (key) name.
	 */
	private SortedMap<String,Object> properties = new TreeMap<>();
	
	/**
	 *  Data path for the GeoMap, folders or URL path to look for data:
     *  - the geolayer file names will be relative to this, if not specified as absolute path
	 */
	private String dataPath = "";
	
	/**
	 * List of GeoLayer in the map.
	 */
	private List<GeoLayer> geoLayers = new ArrayList<>();
	
	/**
	 * List of GeoLayerViewGroup in the map.
	 */
	private List<GeoLayerViewGroup> geoLayerViewGroups = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public GeoMap () {
	}

	/**
	 * Constructor.
	 * @param id GeoMap project identifier
	 * @param name project name
	 * @param description project description
	 * @param properties project properties
	 */
	public GeoMap ( String geoMapId, String name, String description, SortedMap<String,Object> properties ) {
		this.geoMapId = geoMapId;
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
	 * Add a GeoLayer.
	 * @param geoLayer the layer to add
	 */
	public void addGeoLayer ( GeoLayer geoLayer ) {
		this.geoLayers.add ( geoLayer );
	}

	/**
	 * Add a GeoLayerViewGroup.
	 * @param geoLayerViewGroup the view group to add
	 */
	public void addGeoLayerViewGroup ( GeoLayerViewGroup geoLayerViewGroup ) {
		this.geoLayerViewGroups.add ( geoLayerViewGroup );
	}

	/**
	 * Return the description.
	 * @return the description
	 */
	public String getDescription () {
		return this.description;
	}

	/**
	 * Return the layer view group matching the given identifier.
	 * @param geoLayerViewGroupId the layer view group identifier
	 * @return the matching layer view group in the map, or null if not found
	 */
	public GeoLayerViewGroup getLayerViewGroupForLayerViewGroupId ( String geoLayerViewGroupId ) {
		if ( geoLayerViewGroupId == null ) {
			// No layer view group ID was provided.
			return null;
		}
		for ( GeoLayerViewGroup geoLayerViewGroup : this.geoLayerViewGroups ) {
			if ( geoLayerViewGroup.getGeoLayerViewGroupId().equals(geoLayerViewGroupId) ) {
				return geoLayerViewGroup;
			}
		}
		// Not found.
		return null;
	}

	/**
	 * Return the layers.
	 * @return the layers
	 */
	public List<GeoLayer> getGeoLayers() {
		return this.geoLayers;
	}

	/**
	 * Return the layer view groups.
	 * @return the layer view groups
	 */
	public List<GeoLayerViewGroup> getGeoLayerViewGroups() {
		return this.geoLayerViewGroups;
	}

	/**
	 * Return the identifier.
	 * @return the identifier
	 */
	public String getGeoMapId() {
		return this.geoMapId;
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
}