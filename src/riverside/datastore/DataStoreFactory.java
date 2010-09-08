package riverside.datastore;

import RTi.Util.IO.PropList;

/**
 * Define implementation minimum for a DataStore factory.  Because properties are often read from
 * INI format property files, use a PropList for properties.
 * @author sam
 */
public interface DataStoreFactory
{
    /**
     * Instantiate a DataStore from a PropList of string properties.
     * @param props properties to describe the DataStore configuration.
     */
    public DataStore create ( PropList props );
}