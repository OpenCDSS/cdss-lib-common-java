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