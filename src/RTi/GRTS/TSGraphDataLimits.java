package RTi.GRTS;

import java.util.List;

import RTi.GR.GRLimits;

/**
Immutable helper class to store data limits and identifiers for time series, needed for graphing.
The main purpose of this class is to group data so that it can more easily be passed between methods.
*/
public class TSGraphDataLimits
{

/**
Number of time series in the graph.
*/
private int __numTimeSeries;

/**
List of time series identifiers for time series being graphed.
*/
private List<String> __tsidList;

/**
Data limits for time series being graphed.
*/
private GRLimits __dataLimits;

/**
Constructor.
*/
public TSGraphDataLimits ( int numTimeSeries, List<String> tsidList, GRLimits dataLimits )
{
    this.__numTimeSeries = numTimeSeries;
    this.__tsidList = tsidList;
    this.__dataLimits = dataLimits;
}

/**
Return the data limits for time series on the graph.
*/
public GRLimits getDataLimits ()
{
    return __dataLimits;
}

/**
Return the number of time series on the graph.
*/
public int getNumTimeSeries ()
{
    return __numTimeSeries;
}

/**
Return the time series identifiers for time series being graphed.
*/
public List<String> getTimeSeriesIds ()
{
    return __tsidList;
}

}