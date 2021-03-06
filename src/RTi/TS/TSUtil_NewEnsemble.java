// TSUtil_NewEnsemble - create a TSEnsemble from 0+ time series.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.TS;

import java.util.ArrayList;
import java.util.List;

import RTi.Util.Time.DateTime;

/**
Create a TSEnsemble from 0+ time series.
*/
public class TSUtil_NewEnsemble
{

/**
List of problems generated by this command, guaranteed to be non-null.
*/
private List<String> __problems = new ArrayList<String>();

/**
Ensemble identifier.
*/
private String __ensembleID = null;

/**
Ensemble name.
*/
private String __ensembleName = null;

/**
Data table being filled with time series.
*/
private TSEnsemble __ensemble = null;

/**
Time series to process.
*/
private List<TS> __tsList = null;;

/**
Start of data period (null to use full time series).
*/
private DateTime __setStart = null;

/**
End of data period (null to use full time series).
*/
private DateTime __setEnd = null;

/**
Indicate whether time series are copied into the ensemble (true) or original references used (false, default).
*/
private boolean __copyTimeSeries = false;

/**
Constructor.
@param ensembleID identifier for the new ensemble.
@param ensembleName name for the new ensemble.
@param tslist list of time series being placed in the ensemble.
@param setStart first date/time to be transferred.
@param setEnd last date/time to be transferred.
@param copyTimeSeries if false, just include references to the original time series in the ensemble; if true,
copy the time series and then include in the ensemble
*/
public TSUtil_NewEnsemble ( String ensembleID, String ensembleName,
    List<TS> tslist, DateTime outputStart, DateTime outputEnd, boolean copyTimeSeries )
{   //String message;
    //String routine = getClass().getName() + ".constructor";
	// Save data members.
    __ensembleID = ensembleID;
    __ensembleName = ensembleName;
    __tsList = tslist;
    __setStart = outputStart;
    __setEnd = outputEnd;
    __copyTimeSeries = copyTimeSeries;
    // Make sure that the time series are regular and of the same interval
    if ( (tslist != null) && !TSUtil.intervalsMatch(tslist) ) {
        throw new UnequalTimeIntervalException (
            "Time series don't have the same interval - cannot create an ensemble.");
    }
    if ( (tslist != null) && TSUtil.areAnyTimeSeriesIrregular(tslist) ) {
        throw new IrregularTimeSeriesNotSupportedException (
            "One or more time series are irregular - cannot create an ensemble.");
    }
    if ( (tslist != null) && !TSUtil.areUnitsCompatible(tslist, true) ) {
        throw new UnequalTimeIntervalException (
            "Time series don't have the units - cannot create an ensemble.");
    }
}

/**
Copy the time series into the table.
@return the new ensemble.
*/
public TSEnsemble newEnsemble ()
{
    // Create a new list of problems
    __problems = new ArrayList<String>();
    
    List<TS> tslist = getTimeSeriesList();
    int tslistSize = 0;
    if ( tslist != null ) {
        tslistSize = tslist.size();
    }
    
    // Create the ensemble, with no time series in the list...
    TSEnsemble ensemble = null;
    if ( !getCopyTimeSeries() || (tslistSize == 0) ) {
        // Just create the ensemble by assembling time series from the list...
        ensemble = new TSEnsemble ( getEnsembleID(), getEnsembleName(), getTimeSeriesList() );
    }
    else {
        // Need to copy the time series (and optionally if the dates have been specified, change the period
        // during the copy.
        List<TS> tslist2 = new ArrayList<TS>();
        DateTime start = getSetStart();
        DateTime end = getSetEnd();
        TS ts;
        boolean doAdd;
        for ( int i = 0; i < tslistSize; i++ ) {
            // Clone the time series because need a copy
            ts = (TS)tslist.get(i).clone();
            doAdd = true;
            if ( (start != null) && (end != null) ) {
                // Change the period that that which is requested
                try {
                    ts.changePeriodOfRecord(start,end);
                }
                catch ( Exception e ) {
                    __problems.add ( "Unable to change period for \"" + ts.getIdentifier() +
                          "\" - not adding to ensemble.");
                    doAdd = false;
                }
            }
            if ( doAdd ) {
                tslist2.add ( ts );
            }
        }
        // Finally create the ensemble...
        ensemble = new TSEnsemble ( getEnsembleID(), getEnsembleName(), tslist2 );
    }
    setEnsemble ( ensemble );
    return ensemble;
}

/**
Return the indicator of whether to copy the time series (from the constructor).
*/
private boolean getCopyTimeSeries()
{
    return __copyTimeSeries;
}

/**
Return the time series ensemble that results from processing in this class.
@return the time series ensemble.
*/
public TSEnsemble getEnsemble ()
{
    return __ensemble;
}

/**
Return the ensemble identifier from constructor input.
*/
private String getEnsembleID ()
{
    return __ensembleID;
}

/**
Return the ensemble name from constructor input.
*/
private String getEnsembleName ()
{
    return __ensembleName;
}

/**
Return a list of problems for the time series.
*/
public List<String> getProblems ()
{
    return __problems;
}

/**
Return the set end date/time.
@return the set end date/time.
*/
public DateTime getSetEnd ()
{
    return __setEnd;
}

/**
Return the set start date/time.
@return the set start date/time.
*/
public DateTime getSetStart ()
{
    return __setStart;
}

/**
Return the time series that are input to the processing (use ensemble for final list that is included).
@return the time series that are input to the processing.
*/
public List<TS> getTimeSeriesList ()
{
    return __tsList;
}

/**
Set the ensemble that is the result of processing in this class.
@param ensemble Ensemble to set.
*/
private void setEnsemble ( TSEnsemble ensemble )
{
    __ensemble = ensemble;
}

}
