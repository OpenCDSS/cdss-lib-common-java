package RTi.DMI;

/**
Abstract implementation of DatabaseDataStore, to handle management of common data.
@author sam
*/
abstract public class AbstractDatabaseDataStore implements DatabaseDataStore
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
The DMI for the data store.
*/
private DMI __dmi = null;

/**
Return the description for the data store.
@return the description for the data store.
*/
public String getDescription()
{
    return __description;
}

/**
Return the DMI for the data store.
@return the DMI for the data store.
*/
public DMI getDMI()
{
    return __dmi;
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
Set the DMI for the data store.
@param dmi the DMI for the data store.
*/
public void setDMI ( DMI dmi )
{
    __dmi = dmi;
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

}