// AbstractDataStore - abstract base class for datastores

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

import RTi.Util.IO.PropList;

// TODO SAM 2015-03-22 Need to fix issue that name and description are data members and also can be in properties.
/**
Abstract implementation of DataStore, to handle management of common configuration data.
*/
abstract public class AbstractDataStore implements DataStore
{

/**
The description for the datastore (usually a short sentence).
*/
private String __description = "";

/**
The name for the datastore (usually a single string without spaces, suitable for unique identification).
*/
private String __name = "";

/**
Property list for data properties read from configuration file.
*/
private PropList __props = new PropList("");

/**
Status of the datastore (0=Ok, 1=Error).
*/
private int __status = 0;

/**
Message corresponding to the status (e.g., error message).
*/
private String __statusMessage = "";
    
/**
Return the description for the datastore.
@return the description for the datastore.
*/
public String getDescription()
{
    return __description;
}

/**
Return the name for the datastore.
@return the name for the datastore.
*/
public String getName()
{
    return __name;
}

/**
Return the string property list for the datastore configuration.
The datastore property list is returned, not a copy.
@return the string property list for the datastore configuration, guaranteed to be non-null.
@param propertyName name of the property
*/
public PropList getProperties ()
{
    return __props;
}

/**
Return the string value for a datastore configuration property.
@return the string value for a datastore configuration property, or null if not matched.
@param propertyName name of the property
*/
public String getProperty ( String propertyName )
{
    return __props.getValue(propertyName);
}

/**
Return the status for the datastore.
@return the status for the datastore.
*/
public int getStatus()
{
    return __status;
}

/**
Return the status message for the datastore.
@return the status message for the datastore.
*/
public String getStatusMessage()
{
    return __statusMessage;
}


/**
Set the identifier for the datastore.
@param description the identifier for the datastore.
*/
public void setDescription ( String description )
{
    __description = description;
}

/**
Set the name for the datastore.
@param name the name for the datastore.
*/
public void setName ( String name )
{
    __name = name;
}

/**
Set the list of properties for the datastore.
@param props the list of properties for the datastore
*/
public void setProperties ( PropList props )
{
    __props = props;
}

/**
Set the status for the datastore.
@param status the status for the datastore.
*/
public void setStatus ( int status )
{
    __status = status;
}

/**
Set the status message for the datastore, for example when the status indicates an error.
@param statusMessage the status message for the datastore.
*/
public void setStatusMessage ( String statusMessage )
{
    __statusMessage = statusMessage;
}

}
