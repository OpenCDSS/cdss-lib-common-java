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
private String __dataFlag = "";

/**
Description for the data flag.
*/
private String __description = "";

/**
Constructor.
@param dataFlag data flag (generally one character).
@param description description of the data flag.
*/
public TSDataFlagMetadata ( String dataFlag, String description ) {
    setDataFlag ( dataFlag );
    setDescription ( description );
}

/**
Return the data flag.
@return the data flag
*/
public String getDataFlag () {
    return __dataFlag;
}

/**
Return the data flag description.
@return the data flag description
*/
public String getDescription () {
    return __description;
}

/**
Set the data flag.
@param dataFlag the data flag
*/
private void setDataFlag ( String dataFlag ) {
    __dataFlag = dataFlag;
}

/**
Set the description for the data flag.
@param description the data flag description
*/
private void setDescription ( String description ) {
    __description = description;
}

}