// JSONObject - class to hold a JSON object as unstructured data

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

package RTi.Util.JSON;

import java.util.List;
import java.util.Map;

/**
 * Object to hold unstructured data objects using JSON:
 * - the top-level object can be a Map for {} or List (array) for [].
 */
public class JSONObject {

	/**
	The identifier for the JSON object.
	*/
	private String objectID = "";

	/**
	 * The object map, for example from Jackson mapper readValue() method.
	 * This is used when the JSON is surrounded with { }.
	 */
	private Map<?,?> objectMap = null;

	/**
	 * The object list, for example from Jackson mapper readValue() method.
	 * This is used when the JSON is surrounded with [ ].
	 */
	private List<?> objectArray = null;

	/**
	Construct a new JSON object and set the identifier to an empty string, used by utility code.
	*/
	public JSONObject () {
		this.objectID = "";
	}

	/**
	Construct a new JSON object and set the identifier.
	@param objectID object identifier.
	*/
	public JSONObject ( String objectID ) {
		this.objectID = objectID;
	}

	/**
	Return the JSON object array, when the top level object is an array.
	@return the JSON object array.
	*/
	public List<?> getObjectArray () {
    	return this.objectArray;
	}

	/**
	Return the JSON object identifier.
	@return the JSON object identifier.
	*/
	public String getObjectID () {
    	return this.objectID;
	}

	/**
	Return the JSON object map.
	@return the JSON object map.
	*/
	public Map<?,?> getObjectMap () {
    	return this.objectMap;
	}

	/**
	Set the JSON object array.
	@param the JSON object array.
	*/
	public void setObjectArray ( List<?> objectArray ) {
    	this.objectArray = objectArray;
	}

	/**
	Set the JSON object identifier.
	@param the JSON object identifier.
	*/
	public void setObjectID ( String objectID ) {
    	this.objectID = objectID;
	}

	/**
	Set the JSON object list, for the case where the top-level object is an map {}.
	@param the JSON object list.
	*/
	public void setObjectMap ( Map<?,?> objectMap ) {
    	this.objectMap = objectMap;
	}
}