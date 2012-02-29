package riverside.datastore;

import java.net.URI;

/**
Abstract implementation of WebServiceDataStore, to handle management of common configuration data.
*/
abstract public class AbstractWebServiceDataStore extends AbstractDataStore implements WebServiceDataStore
{
    
/**
The root web service URI, to which more specific resource addresses will be appended (e.g.,
"http://data.rcc-acis.org".
*/
private URI __serviceRootURI = null;

/**
Return the service root URI for the data store.
@return the service root URI for the data store.
*/
public URI getServiceRootURI()
{
    return __serviceRootURI;
}

/**
Set the service root URI for the data store.
@param serviceRootURI the service root URI for the data store.
*/
public void setServiceRootURI ( URI serviceRootURI )
{
    __serviceRootURI = serviceRootURI;
}

}