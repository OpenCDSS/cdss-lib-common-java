package RTi.TS;

import java.util.List;
import java.util.Vector;

// TODO SAM 2007-12-13 Evaluate whether to implement or extend from List
// For now just use a list internally for data and implement List methods as needed.
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
private List<TS> __tslist = new Vector();

/**
Identifier for the ensemble.
*/
private String __id = "";

/**
Name for the ensemble, a descriptive phrase.
*/
private String __name = "";

/**
Create a new ensemble.  An empty list of time series will be used.
*/
public TSEnsemble ()
{
}

/**
Create a new ensemble, given a list of time series.
@param tslist List of time series.
*/
public TSEnsemble ( String id, String name, List<TS> tslist )
{
    setEnsembleID ( id );
    setEnsembleName ( name );
    if ( tslist == null ) {
        tslist = new Vector();
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
TSEnsemble objects are cloned.  The result is a complete deep copy, including a copy
of all the time series.
*/
public Object clone ()
{   try {
        // Clone the base class...
        TSEnsemble ensemble = (TSEnsemble)super.clone();
        // Now clone mutable objects...
        int size = size();
        // Need a new vector...
        ensemble.__tslist = new Vector(size);
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
        List<TS> tslist = new Vector();
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