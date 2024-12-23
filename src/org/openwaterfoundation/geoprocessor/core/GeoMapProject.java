// GeoMapProject - definition of a map project

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
    Map project that when serialized using to JSON will
    result in a map project file that can be used by other tools such as Leaflet map viewer.
    The organization of objects is:

    GeoMapProject                         # File that represents 1+ map's configuration.
        GeoMap []                         # File and object that represent a map's configuration.
            GeoLayer []                   # Shared list of layers, if maps can share for viewing (leaflet cannot share).
            GeoLayerViewGroup []          # Groups of layer views, as per typical GIS legend.
                GeoLayerView []           # A single map view that uses one or more layers.
                    GeoLayer              # ID will be used in output to reference above GeoLayer [].
                    GeoLayerSymbol        # Symbol configuration for the layer.
 */
public class GeoMapProject {

	/**
	 * Unique identifier for the project.
	 */
	private String geoMapProjectId = "";

	/**
	 * Name for the project.
	 */
	private String name = "";

	/**
	 * Description for the project.
	 */
	private String description = "";

	/**
	 * Type of the project.
	 */
	private GeoMapProjectType projectType = GeoMapProjectType.SINGLE_MAP;

	/**
	 * List of properties for the project, sorted alphabetically by the property (key) name.
	 */
	private SortedMap<String,Object> properties = new TreeMap<>();

	/**
	 * List of GeoMap instances in the project.
	 */
	private List<GeoMap> geoMaps = new ArrayList<>();

	/**
	 * Constructor.
	 * @param geoMapProjectId GeoMap project identifier
	 * @param name project name
	 * @param description project description
	 * @param properties project properties
	 */
	public GeoMapProject ( String geoMapProjectId, String name, String description, SortedMap<String,Object> properties ) {
		this.geoMapProjectId = geoMapProjectId;
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
	 * Add a map to the project.
	 * @param map map to add to the project
	 */
	public void addMap ( GeoMap geomap ) {
		this.geoMaps.add(geomap);
	}

	/**
	 * Return the description.
	 * @return the description
	 */
	public String getDescription () {
		return this.description;
	}

	/**
	 * Return the list of GeoMap.
	 * @return the list of GeoMap
	 */
	public List<GeoMap> getGeoMaps () {
		return this.geoMaps;
	}

	/**
	 * Return the project identifier.
	 * @return the project identifier
	 */
	public String getGeoMapProjectId() {
		return this.geoMapProjectId;
	}

	/**
	 * Return the map matching the given identifier.
	 * @param geoMapId the map identifier
	 * @return the matching map in the project, or null if not found
	 */
	public GeoMap getMapForId ( String geoMapId ) {
		if ( geoMapId == null ) {
			// No map ID was provided.
			return null;
		}
		for ( GeoMap map : this.geoMaps ) {
			if ( map.getGeoMapId().equals(geoMapId) ) {
				return map;
			}
		}
		// Not found.
		return null;
	}

	/**
	 * Return the name.
	 * @return the name.
	 */
	public String getName () {
		return this.name;
	}

	/**
	 * Return the project type.
	 * @return the project type
	 */
	public GeoMapProjectType getProjectType () {
		return this.projectType;
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