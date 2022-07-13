// DataStore - interface to define general datastore behavior

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2022 Colorado Department of Natural Resources

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

/**
 * Interface for a datastore, which is for persistent storage of data.
 * A datastore can be a database, web service, file, etc.
 * @author sam
 *
 */
public interface DataStore {
    
    /**
     * Get the description for the datastore.
     * This is a longer name that is helpful to the user (e.g., "Historical temperature database.").
     * @param return the description of the datastore
     */
    public String getDescription();
    
    /**
     * Get the name for the datastore.  This is typically a short string (e.g., "HistoricalDB").
     * In some cases (such as time series identifiers), this is used as the input type.
     * @return the name of the datastore
     */
    public String getName();

    /**
     * Get the property list for the datastore.  These are strings stored in the datastore configuration.
     * Properties allow for custom-configuration of datastores beyond the basic behavior enforced by the datastore design.
     * "Name" and "Description" are specific properties that are required.
     * @return the property list for the datastore
     */
    public PropList getProperties();
    
    /**
     * Get a property for the datastore.  These are strings stored in the datastore configuration.
     * Properties allow for custom-configuration of datastores beyond the basic behavior enforced by the datastore design.
     * "Name" and "Description" are specific properties that are required.
     * @return the string value of a datastore configuration property, or null if not found
     * @param propertyName the name of a property to retrieve
     */
    public String getProperty( String propertyName );
    
    /**
     * Get the status of datastore.
     * @return the status as 0=OK, 1=Error.  In the future more specific error codes may be implemented.
     */
    public int getStatus();
    
    /**
     * Get the status message for datastore.
     * @return the status message, for example an error message.
     */
    public String getStatusMessage();

    // TODO SAM 2012-02-29 Evaluate using a more general object than PropList, such as a Map.
    /**
     * Set the list of properties for a datastore (typically at creation from a configuration file).
     */
    public void setProperties ( PropList properties );
    
    /**
     * Set the status of datastore.
     * @param status as 0=OK, 1=Error, 2=Closed.
     * TODO SAM 2015-02-14 In the future additional error codes may be implemented or convert to enumeration.
     */
    public void setStatus(int status);
    
    /**
     * Set the status message for the datastore.
     * @param statusMessage message corresponding to status, for example error message.
     */
    public void setStatusMessage(String statusMessage);
}