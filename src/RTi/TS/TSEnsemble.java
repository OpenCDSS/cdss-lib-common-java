package RTi.TS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
A collection for time series, to be represented as an ensemble.  At this time, it
is expected that each time series has been created or read using code that
understands ensembles.  There are not currently hard constraints for ensembles but
it is expected that they have similar time series characteristics like period of record,
data type, and interval.  More constraints may be added over time.
*/
public class TSEnsemble implements Cloneable
{

/**
Ensemble of time series data, guaranteed to exist but may be empty.
*/
private List<TS> __tslist = new ArrayList<TS>();

/**
Identifier for the ensemble.
*/
private String __id = "";

/**
Name for the ensemble, a descriptive phrase.
*/
private String __name = "";

/**
TODO SAM 2010-09-21 Evaluate whether generic "Attributable" interface should be implemented instead.
Properties for the time series beyond the built-in properties.  For example, location
information like county and state can be set as a property.
*/
private LinkedHashMap<String,Object> __property_HashMap = null;

/**
Create a new ensemble.  An empty list of time series will be used.
*/
public TSEnsemble ()
{
}

/**
Create a new ensemble, given a list of time series.
@param id ensemble ID
@param name ensemble name
*/
public TSEnsemble ( String id, String name )
{
    setEnsembleID ( id );
    setEnsembleName ( name );
}

/**
Create a new ensemble, given a list of time series.
@param id ensemble ID
@param name ensemble name
@param tslist List of time series
*/
public TSEnsemble ( String id, String name, List<TS> tslist )
{
    setEnsembleID ( id );
    setEnsembleName ( name );
    if ( tslist == null ) {
        tslist = new ArrayList<TS>();
    }
    __tslist = tslist;
}

/**
Add a time series to the ensemble.
@param ts time series to add to the ensemble.
*/
public void add ( TS ts )
{   __tslist.add ( ts );
}

/**
Clone the object.  The Object base class clone() method is called and then the
TSEnsemble objects are cloned.  The result is a complete deep copy, including a copy of all the time series.
*/
public Object clone ()
{   try {
        // Clone the base class...
        TSEnsemble ensemble = (TSEnsemble)super.clone();
        // Now clone mutable objects...
        int size = size();
        // Need a new list...
        ensemble.__tslist = new ArrayList<TS>(size);
        TS ts;
        for ( int i = 0; i < size; i++ ) {
            ts = get(i);
            if ( ts == null ) {
                ensemble.add ( null );
            }
            else {
                ensemble.add ( (TS)ts.clone() );
            }
        }
        return ensemble;
    }
    catch ( CloneNotSupportedException e ) {
        // Should not happen because everything is cloneable.
        throw new InternalError();
    }
}

/**
Get a time series from the ensemble.
@param pos Position (0+) in the ensemble for the requested time series.
@return The time series from the ensemble.
*/
public TS get ( int pos )
{   return __tslist.get ( pos );
}

/**
Return the ensemble identifier.
@return The ensemble identifier.
*/
public String getEnsembleID ()
{   return __id;
}

/**
Return the ensemble name.
@return The ensemble name.
*/
public String getEnsembleName ()
{   return __name;
}

/**
Get the hashtable of properties, for example to allow display.
@return the hashtable of properties, for example to allow display, may be null.
*/
public HashMap<String,Object> getProperties()
{   if ( __property_HashMap == null ) {
        __property_HashMap = new LinkedHashMap<String,Object>(); // Initialize to non-null for further use
    }
    return __property_HashMap;
}

/**
Get a time series ensemble property's contents (case-specific).
@param propertyName name of property being retrieved.
@return property object corresponding to the property name.
*/
public Object getProperty ( String propertyName )
{
    if ( __property_HashMap == null ) {
        return null;
    }
    return __property_HashMap.get ( propertyName );
}

/**
Return the time series list.
@param copyList if true, the list is copied (but the time series contents remain the same).
Use this when the list object is going to be modified.
*/
public List<TS> getTimeSeriesList ( boolean copyList )
{
    if ( !copyList ) {
        return __tslist;
    }
    else {
        List<TS> tslist = new ArrayList<TS>(__tslist.size());
        int size = __tslist.size();
        for ( int i = 0; i < size; i++ ) {
            tslist.add( __tslist.get(i));
        }
        return tslist;
    }
}

/**
Remove the time series object from the ensemble.
@param ts Object (time series) to remove.
@return true if the object was found and removed, false if not in the list.
*/
public boolean remove ( Object ts )
{
    return __tslist.remove ( ts );
}

/**
Set the ensemble identifier.
@param id The ensemble identifier.
*/
public void setEnsembleID ( String id )
{   if ( id == null ) {
        id = "";
    }
    __id = id;
    // Also set the property to allow for generic property request
    setProperty("EnsembleID",id);
}

/**
Set the ensemble name.
@param name The ensemble name.
*/
public void setEnsembleName ( String name )
{   if ( name == null ) {
        name = "";
    }
    __name = name;
    // Also set the property to allow for generic property request
    setProperty("EnsembleName",name);
}

/**
Set a time series ensemble property's contents (case-specific).
@param propertyName name of property being set.
@param property property object corresponding to the property name.
*/
public void setProperty ( String propertyName, Object property )
{
    if ( __property_HashMap == null ) {
        __property_HashMap = new LinkedHashMap<String, Object>();
    }
    // Do not allow EnsembleID to be set because it is fundamental to the identification of the ensemble and should be immutable
    if ( propertyName.equals("EnsembleID") ) {
    	return;
    }
    __property_HashMap.put ( propertyName, property );
}

/**
Set the time series in the ensemble.  If the list is too small, null time series will be added.
@param index Index (0+) at which to set the ensemble.
@param ts Time series to set.
*/
public void set ( int index, TS ts )
{
    int size = size();
    if ( index >= size ) {
        for ( int i = size; i <= index; i++ ) {
            __tslist.set ( index, null );
        }
    }
    // Set the time series...
    __tslist.set( index, ts);
}

/**
Get the number of time series in the ensemble.
@return The number of time series in the ensemble.
*/
public int size ( )
{   return __tslist.size();
}

/**
Return the list of time series in the ensemble as an array.
*/
public TS [] toArray ()
{
    int size = size();
    TS [] array = new TS[size];
    for ( int i = 0; i < size; i++ ) {
        array[i] = __tslist.get(i);
    }
    return array;
}

}