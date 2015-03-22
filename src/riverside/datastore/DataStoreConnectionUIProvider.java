package riverside.datastore;

import javax.swing.JFrame;

import RTi.Util.IO.PropList;

/**
Interface for a datastore connection UI which provides an interactive interface to connect to the datastore.
*/
public interface DataStoreConnectionUIProvider
{
    
    /**
     * Open a UI to connect to a datastore, and return the opened datastore or null if canceled.
     * @param props properties for the datastore, read from the datastore configuration file
     * @param frame the main UI, used to position the dialog
     * @param return the datastore
     */
    public DataStore openDataStoreConnectionUI ( PropList props, JFrame frame );
    
}