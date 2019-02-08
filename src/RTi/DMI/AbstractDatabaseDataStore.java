// AbstractDatabaseDataStore - base class for database datastores

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

import riverside.datastore.AbstractDataStore;

/**
Abstract implementation of DatabaseDataStore, to handle management of common data.
@author sam
*/
abstract public class AbstractDatabaseDataStore extends AbstractDataStore implements DatabaseDataStore
{

/**
The DMI for the data store.
*/
private DMI __dmi = null;

/**
 * Check the database connection and if has timed out, reconnect.
 * This method is called by commands that use a datastore.
 * @returns true if the connection is established, false if not.
 * Because this version is in the abstract class, return getDMI().isOpen().
 * The method should be overloaded in datastores that handle opening the connection.
 */
public boolean checkDatabaseConnection()
{
	DMI dmi = getDMI();
	if ( dmi == null ) {
		return false;
	}
	return dmi.isOpen();
}

/**
Return the DMI for the data store.
@return the DMI for the data store.
*/
public DMI getDMI()
{
    return __dmi;
}

/**
Set the DMI for the data store.
@param dmi the DMI for the data store.
*/
public void setDMI ( DMI dmi )
{
    __dmi = dmi;
}

}
