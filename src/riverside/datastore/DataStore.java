package riverside.datastore;

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
     */
    public String getDescription();
    
    /**
     * Get the name for the data store.  This is typically a short string (e.g., "HistoricalDB").
     * In some cases (such as time series identifiers), this is used as the input type.
     */
    public String getName();
}