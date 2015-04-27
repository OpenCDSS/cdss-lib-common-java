package riverside.datastore;

import java.util.List;

import javax.swing.JFrame;

import RTi.Util.IO.PropList;

/**
Interface for a datastore connection UI which provides an interactive interface to connect to the datastore.
*/
public interface DataStoreConnectionUIProvider
{
    
    /**
     * Open a UI to connect to a datastore, and return the opened datastore or null if canceled.
     * This version is used when a prompt is desired to enter database login credentials at start-up, using properties from a datastore configuration file.
     * @param props properties for the datastore, read from the datastore configuration file
     * @param frame the main UI, used to position the dialog
     * @param return the datastore
     */
    public DataStore openDataStoreConnectionUI ( PropList props, JFrame frame );
    
    /**
     * Open a UI to connect to a datastore, and return the opened datastore or null if canceled.
     * This version is used when (re)connecting to a datastore after initial startup, for example to change users.
     * @param datastoreList a list of ReclamationHDB datastores that were initially configured but may or may not be active/open.
	 * The user will first pick a datastore to access its properties, and will then enter a new login and password for the database connection.
	 * Properties for the datastores are used in addition to the login and password specified interactively to recreate the database connection.
     * @param frame the main UI, used to position the dialog
     * @param return the datastore that was updated
     */
    public DataStore openDataStoreConnectionUI ( List<? extends DataStore> datastoreList, JFrame frame );
    
}