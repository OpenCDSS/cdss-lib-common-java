// GeoMapProjectResponse - map project response, to handle reading JSON with named project

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
 * Map project response to handle reading web service or JSON file.
 */
public class GeoMapProjectResponse {

	/**
	 * Unique identifier for the project.
	 */
	private GeoMapProject geoMapProject = null;

	/**
	 * Constructor.
	 * @param geoMapProjectId GeoMap project identifier
	 * @param name project name
	 * @param description project description
	 * @param properties project properties
	 */
	public GeoMapProjectResponse ( ) {
	}

	/**
	 * Return the GeoMapProject instance.
	 * @return the GeoMapProject instance.
	 */
	public GeoMapProject getGeoMapProject () {
		return this.geoMapProject;
	}
}