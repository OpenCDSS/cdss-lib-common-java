package RTi.TS;

import java.util.List;
import java.util.Vector;

// TODO SAM 2007-12-13 Evaluate whether to implement or extend from List
// For now just use a list internally for data and implement List methods as needed.
/**
A collection for time series, to be represented as an ensemble.  At this time, it
is expected that each time series has been created or read using code that
understands ensembles.  There are not currently hard constraints for ensembles but
it is expected that they have similar time series charactersitics like period of record,
data type, and interval.  More constraints may be added over time.
*/
public class TSEnsemble
{

/**
Ensemble of time series data.  Use a Vector by default to avoid null checks.
*/
private List __tslist = new Vector();

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

// FIXME SAM 2007-12-13 Need to use generics to force TS in list
/**
Create a new ensemble, given a Vector of time series.
@param tslist List of time series.
*/
public TSEnsemble ( String id, String name, List tslist )
{
    setEnsembleID ( id );
    setEnsembleName ( name );
    __tslist = tslist;
}

/**
Add a time series to the ensemble.
@param ts Time series to add to the ensemble.
*/
public void add ( TS ts )
{   __tslist.add ( ts );
}

/**
Get a time series from the ensemble.
@param pos Position (0+) in the ensemble for the requested time series.
@return The time series from the ensemble.
*/
public TS get ( int pos )
{   return (TS)__tslist.get ( pos );
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
Set the ensemble identifier.
@param id The ensemble identifier.
*/
public void setEnsembleID ( String id )
{   __id = id;
}

/**
Set the ensemble name.
@param name The ensemble name.
*/
public void setEnsembleName ( String name )
{   __name = name;
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
        array[i] = (TS)__tslist.get(i);
    }
    return array;
}

}
