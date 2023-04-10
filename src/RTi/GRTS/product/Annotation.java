// Annotation - data object used for serialization

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.GRTS.product;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Representation of data for a SubProduct, consistent with "TSProduct".
 */
@JsonPropertyOrder({"properties","data","annotations"})
public class Annotation {

	/**
	 * Hashmap of alphabetized properties.
	 * Allow any object to be saved but it is highly likely that most properties will be strings
	 * consistent with the legacy time series product properties.
	 */
	private HashMap<String,Object> properties = new LinkedHashMap<>();

	/**
	 * Constructor.
	 */
	public Annotation () {
	}

	/**
	 * Get all the properties, needed for serialization.
	 */
	public HashMap<String,Object> getProperties () {
		return this.properties;
	}

	/**
	 * Get a property value.
	 */
	public Object getProperty ( String key ) {
		return this.properties.get(key);
	}

	/**
	 * Set a property value.
	 */
	public void setProperty ( String key, Object value ) {
		this.properties.put(key, value);
	}
}