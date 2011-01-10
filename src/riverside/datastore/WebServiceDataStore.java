package riverside.datastore;

import java.net.URI;

import riverside.datastore.DataStore;

/**
 * Interface definition for web service data store, which includes web server name and root URI.
 * TODO SAM 2011-01-06 Need to flesh out.
 */
public interface WebServiceDataStore extends DataStore
{
    /**
     * Get the root URI.
     */
    public URI getServiceRootURI();
}