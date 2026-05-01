// TSDataFlagMetadata - metadata about flags used with a time series.

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

package RTi.TS;

/**
Metadata about flags used with a time series.
Instances of this class can be added to a time series via TS.addDataFlagMetaData() method.
This information is useful for output reports and displays, to explain the meaning of data flags.
The class is immutable.
*/
public class TSDataFlagMetadata
{

	/**
	Data flag.  Although this is a string, flags are typically each one character.
	*/
	private String dataFlag = "";
	
	/**
	 * Display name, a short phrase, for example to use in a choice.
	 */
	private String displayName = "";
	
	/**
	Description for the data flag.
	*/
	private String description = "";
	
	/**
	Constructor for flag and display name, with no description.
	This version of the method has traditionally been used.
	@param dataFlag data flag (generally one character).
	@param description description of the data flag, roughly a sentence
	*/
	public TSDataFlagMetadata ( String dataFlag, String description ) {
	    this.dataFlag = dataFlag;
	    this.description = description;
	}

	/**
	Constructor.
	This version of the method is newer and reflects data sources that provide the display name.
	@param dataFlag data flag (generally one character).
	@param displayName a short display name, suitable for a UI choice
	@param description description of the data flag, roughly a sentence
	*/
	public TSDataFlagMetadata ( String dataFlag, String displayName, String description ) {
	    this.dataFlag = dataFlag;
	    this.displayName = displayName;
	    this.description = description;
	}

	/**
	Return the data flag.
	@return the data flag
	*/
	public String getDataFlag () {
	    return this.dataFlag;
	}

	/**
	Return the data flag description.
	@return the data flag description
	*/
	public String getDescription () {
	    return this.description;
	}

	/**
	Return the display name
	@return the display name
	*/
	public String getDisplayName () {
	    return this.displayName;
	}
	
}