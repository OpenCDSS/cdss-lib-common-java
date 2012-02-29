package riverside.datastore;

import RTi.Util.IO.PropList;

/**
Abstract implementation of WebServiceDataStore, to handle management of common configuration data.
*/
abstract public class AbstractDataStore implements DataStore
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
Property list for data properties read from configuration file.
*/
private PropList __props = new PropList("");
    
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
Return the string value for a data store configuration property.
@return the string value for a data store configuration property, or null if not matched.
@param propertyName name of the property
*/
public String getProperty ( String propertyName )
{
    return __props.getValue(propertyName);
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
Set the list of properties for the data store.
@param props the list of properties for the data store
*/
public void setProperties ( PropList props )
{
    __props = props;
}

}