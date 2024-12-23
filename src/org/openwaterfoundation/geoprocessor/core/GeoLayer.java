// GeoLayer - spatial data layer

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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Map layer.
 */
public class GeoLayer {

	/**
	 * Unique identifier for the layer.
	 */
	private String geoLayerId = "";
	
	/**
	 * Name for the layer.
	 */
	private String name = "";
	
	/**
	 * Description for the layer.
	 */
	private String description = "";

	/**
	 * Coordinate reference system (CFS).
	 */
	private String crs = "";

	/**
	 * Geometry type.
	 */
	private String geometryType = "";

	/**
	 * History of changes to the layer.
	 */
	private List<String> history = new ArrayList<>();
	
	/**
	 * Layer type.
	 */
	private String layerType = "";
	
	/**
	 * The source format.
	 */
	private String sourceFormat = "";
	
	/**
	 * The source path to the layer, can be a path to a local file or URL.
	 */
	private String sourcePath = "";
	
	/**
	 * List of properties for the layer, sorted alphabetically by the property (key) name.
	 */
	private SortedMap<String,Object> properties = new TreeMap<>();
	
	/**
	 * Constructor.
	 */
	public GeoLayer () {
	}

	/**
	 * Constructor.
	 * @param geoLayerId GeoLayer identifier
	 * @param name layer name
	 * @param description layer description
	 * @param properties layer properties
	 */
	public GeoLayer ( String geoLayerId, String name, String description, SortedMap<String,Object> properties ) {
		this.geoLayerId = geoLayerId;
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
	 * Append to the history.
	 * @param historyComment a comment to add to the history
	 */
	public void appendToHistory ( String historyComment ) {
		this.history.add(historyComment);
	}

	/**
	 * Return the CRS.
	 * @return the CRS 
	 */
	public String getCrs() {
		return this.crs;
	}

	/**
	 * Return the description.
	 * @return the description
	 */
	public String getDescription () {
		return this.description;
	}

	/**
	 * Return the identifier.
	 * @return the identifier
	 */
	public String getGeoLayerId() {
		return this.geoLayerId;
	}

	/**
	 * Return the geometry type.
	 * @return the geometry type
	 */
	public String getGeometryType() {
		return this.geometryType;
	}

	/**
	 * Return the layer history.
	 * @return the layer history 
	 */
	public List<String> getHistory() {
		return this.history;
	}
	
	/**
	 * Return the layer type.
	 * @return the layer type
	 */
	public String getLayerType() {
		return this.layerType;
	}
	
	/**
	 * Return the name.
	 * @return the name.
	 */
	public String getName () {
		return this.name;
	}

	/**
	 * Return the property matching the name, or null if not found.
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
	 * Return the source format for the layer
	 * @return the source format for the layer
	 */
	public String getSourceFormat () {
		return this.sourceFormat;
	}

	/**
	 * Return the source path to use in the map, typically a URL for final output.
	 * @return the source path to use in the map
	 */
	public String getSourcePath () {
		return this.sourcePath;
	}

	/**
	 * Set the coordinate reference system (CRS).
	 * @param crs the CRS for the layer (EPSG:4326 for GeoJSON).
	 */
	public void setCrs ( String crs ) {
		this.crs = crs;
	}

	/**
	 * Set the geometry type.
	 * @param geometryType the geometry type for the layer (e.g., WKT:Polggon, Raster)
	 */
	public void setGeometryType ( String geometryType ) {
		this.geometryType = geometryType;
	}

	/**
	 * Set the layer type.
	 * @param layer the layer type for the layer (e.g., Vector, Raster)
	 */
	public void setLayerType ( String layerType ) {
		this.layerType = layerType;
	}

	/**
	 * Set the source format.
	 * @param sourceFormat the format of the layer (e.g., GeoJSON or WMTS for Web Map Tile Service).
	 */
	public void setSourceFormat ( String sourceFormat ) {
		this.sourceFormat = sourceFormat;
	}

	/**
	 * Set the source path to use in the map, typically a URL for a GeoJSON or GeoTIF file.
	 * @param sourcePath path to the layer
	 */
	public void setSourcePath ( String sourcePath ) {
		this.sourcePath = sourcePath;
	}
}