package riverside.datastore;

import RTi.Util.IO.PropList;

/**
 * Interface for a data store, which is for persistent storage of data.
 * A data store can be a database, file, etc., although the initial work is focusing on
 * connections to databases.
 * @author sam
 *
 */
public interface DataStore {
    
    /**
     * Get the description for the data store.  This is a longer name that is helpful to the user
     * (e.g., "Historical temperature database.").
     * @param return the description of the data store
     */
    public String getDescription();
    
    /**
     * Get the name for the data store.  This is typically a short string (e.g., "HistoricalDB").
     * In some cases (such as time series identifiers), this is used as the input type.
     * @return the name of the data store
     */
    public String getName();
    
    /**
     * Get a property for the data store.  These are strings stored in the data store configuration.
     * Properties allow for custom-configuration of data stores beyond the basic behavior enforced by
     * the data store design.  "Name" and "Description" are specific properties that are required.
     * @return the string value of a data store configuration property, or null if not found
     * @param propertyName the name of a property to retrieve
     */
    public String getProperty( String propertyName );

    // TODO SAM 2012-02-29 Evaluate using a more general object than PropList
    /**
     * Set the list of properties for a data store (typically at creation from a configuration file).
     */
    public void setProperties ( PropList properties );
}