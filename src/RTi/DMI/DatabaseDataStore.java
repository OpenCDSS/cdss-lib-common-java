// DatabaseDataStore - interface to define behavior of database datastores

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

package RTi.DMI;

import riverside.datastore.DataStore;

/**
 * Interface definition for database data store, which includes database server name and database name.
 * Currently this information is used to interface with DMI instances
 * @author sam
 *
 */
public interface DatabaseDataStore extends DataStore
{
	/**
	 * Check the database connection and if has timed out, reconnect.
	 * This method is called by commands that use a datastore.
	 * @returns true if the connection is established, false if not.
	 */
	public boolean checkDatabaseConnection();
	
    /**
     * Get a DMI instance that corresponds to the database connection.
     */
    public DMI getDMI();
}
