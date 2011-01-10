package riverside.datastore;

import java.net.URI;

/**
Abstract implementation of WebServiceDataStore, to handle management of common configuration data.
*/
abstract public class AbstractWebServiceDataStore implements WebServiceDataStore
{
    
/**
The description for the data store (usually a short sentence).
*/
private String __description = "";

/**
The name for the data store (usually a single string without spaces, suitable for unique identification).
*/
private String __name = "";

/**
The root web service URI, to which more specific resource addresses will be appended (e.g.,
"http://data.rcc-acis.org".
*/
private URI __serviceRootURI = null;
    
/**
Return the description for the data store.
@return the description for the data store.
*/
public String getDescription()
{
    return __description;
}

/**
Return the name for the data store.
@return the name for the data store.
*/
public String getName()
{
    return __name;
}

/**
Return the service root URI for the data store.
@return the service root URI for the data store.
*/
public URI getServiceRootURI()
{
    return __serviceRootURI;
}

/**
Set the identifier for the data store.
@param description the identifier for the data store.
*/
public void setDescription ( String description )
{
    __description = description;
}

/**
Set the name for the data store.
@param id the name for the data store.
*/
public void setName ( String name )
{
    __name = name;
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