// TimeSeriesData - class to store a single time series record in a DataStore

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

package riverside.datastore;

import RTi.DMI.DMIDataObject;
import RTi.DMI.DMIUtil;
import RTi.Util.Time.DateTime;

/**
Store single value record data from time series table layouts.  Some data fields are optional
depending on the table layout.  This class is meant to be used internally in the RiversideDB_DMI package,
for example during transfer of database records to time series objects.
*/
public class TimeSeriesData extends DMIDataObject
{

/**
Internal key to link time series record to time series metadata.
*/
//private long metaId = DMIUtil.MISSING_LONG;

/**
DateTime corresponding to the data value.
*/
private DateTime dateTime = null;

/**
Data value.
*/
private double value = DMIUtil.MISSING_DOUBLE;

/**
Optional data quality flag.
*/
private String flag = DMIUtil.MISSING_STRING;

/**
Optional duration of value in seconds.
*/
//private int duration = DMIUtil.MISSING_INT;

/**
Optional DateTime corresponding to insert date/time for the value.  Tables that use this
column must be sorted first by _Date_Time and then _Creation_Time.
*/
//private DateTime creationTime = null;

/**
Constructor.  
*/
public TimeSeriesData ( DateTime dateTime, double value, String flag )
{	super();
    this.dateTime = dateTime;
    this.value = value;
    this.flag = flag;
}

/**
Returns the DateTime for the value
@return the DateTime for the value
*/
public DateTime getDateTime() {
    return this.dateTime;
}

/**
Returns the flag for the value
@return the flag for the value
*/
public String getFlag() {
    return this.flag;
}


/**
Returns the data value
@return the data value
*/
public double getValue() {
    return this.value;
}

}
