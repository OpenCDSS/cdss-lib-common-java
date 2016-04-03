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