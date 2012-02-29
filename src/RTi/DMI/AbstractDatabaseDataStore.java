package RTi.DMI;

import riverside.datastore.AbstractDataStore;

/**
Abstract implementation of DatabaseDataStore, to handle management of common data.
@author sam
*/
abstract public class AbstractDatabaseDataStore extends AbstractDataStore implements DatabaseDataStore
{

/**
The DMI for the data store.
*/
private DMI __dmi = null;

/**
Return the DMI for the data store.
@return the DMI for the data store.
*/
public DMI getDMI()
{
    return __dmi;
}

/**
Set the DMI for the data store.
@param dmi the DMI for the data store.
*/
public void setDMI ( DMI dmi )
{
    __dmi = dmi;
}

}