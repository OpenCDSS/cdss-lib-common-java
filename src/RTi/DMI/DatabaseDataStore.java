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
     * Get a DMI instance that corresponds to the database connection.
     */
    public DMI getDMI();
}