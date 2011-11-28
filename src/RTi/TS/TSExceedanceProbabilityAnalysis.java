package RTi.TS;

import java.util.List;
import java.util.Vector;

/**
Exceedance probability analysis.  The original time series data are processed to
create new time series with values that correspond to the exceedance probability values.
The time series can then be graphed using appropriate visualization techniques, including
line graph, stacked-area plot, or stacked bar chart.
*/
public class TSExceedanceProbabilityAnalysis
{

/**
The time series ensemble that is being processed.
*/
private TSEnsemble __ensemble = null;

/**
The input time series used to compute the exceedance probability time series.
*/
private List<TS> __tsList = new Vector();

/**
The exceedance probabilities to calculate output time series.
*/
private double [] __probabilities = new double[0];

/**
The time series that correspond to the exceedance probabilities.
*/
private List<TS> __probtsList = new Vector();

/**
The time series containing the minimum values from each sample date/time.
*/
private TS __mints = null;

/**
The time series containing the maximum values from each sample date/time.
*/
private TS __maxts = null;
    
/**
Construct an exceedance probability analysis from an ensemble.
Weibull plotting positions are used to calculate the probabilities and numerical values are
interpolated to fit the exact probabilities that are passed in.
@param ensemble the time series ensemble for which to compute the exceedance probability time series
@param 
*/
public TSExceedanceProbabilityAnalysis ( TSEnsemble ensemble, double [] probabilities )
{
    __ensemble = ensemble;
    if ( __ensemble != null ) {
        __tsList = ensemble.getTimeSeriesList(false);
    }
    __probabilities = probabilities;
    if ( __probabilities == null ) {
        __probtsList = new Vector();
    }
    else {
        __probtsList = new Vector(__probabilities.length);
    }
    
    analyze();
}

/**
Analyze the time series, resulting in the probability time series.
*/
private void analyze ()
{
    // Create time series for the minimum, maximum, and probability exceedance values
    // The output time series have the same header information as the input time series except:
    //   - for probabilities the data type is appended with '-N%' where N is the exceedance probability
    //   - for statistics the min, max, etc. are added to the data type
    // What about the alias?
}

/**
Return the ensemble that is used as input for the analysis.
@return the ensemble that is used as input for the analysis (null if a simple list of time series
is used as input).
*/
public TSEnsemble getEnsemble ( )
{
    return __ensemble;
}

/**
Return the list of probability time series.
@return the list of probability time series.
*/
public List<TS> getProbabilityTimeSeriesList ( )
{
    return __probtsList;
}

/**
Return the list of time series that is used as input for the analysis.
@return the list of time series that is used as input for the analysis (guaranteed to be non-null).
*/
public List<TS> getTimeSeriesList ( )
{
    return __tsList;
}

}