package RTi.TS;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
Check time series values.
*/
public class TSUtil_CalculateTimeSeriesStatistic
{

/**
Time series to process.
*/
private TS __ts = null;

/**
Statistic to calculate.
*/
private String __statistic = null;

/**
Statistic result that was calculated, a Double for floating-point statistics like Mean, or an Integer
for integer statistics like Count.
*/
private Object __statisticResult = null;

/**
Date/time for statistic result that was calculated (e.g., date/time for max value).
*/
private DateTime __statisticResultDateTime = null;

/**
Start of analysis (null to analyze all).
*/
private DateTime __analysisStart = null;

/**
End of analysis (null to analyze all).
*/
private DateTime __analysisEnd = null;

/**
Value as input to analysis, depending on checkType.
*/
private Double __value1 = null;

/**
Start of analysis (null to analyze all).
*/
private Double __value2 = null;

/**
Start of analysis (null to analyze all).
*/
private Double __value3 = null;

/**
Constructor.
*/
public TSUtil_CalculateTimeSeriesStatistic ( TS ts, String statistic,
        DateTime analysisStart, DateTime analysisEnd, Double value1, Double value2, Double value3 )
{   // Save data members.
    __ts = ts;
    __statistic = statistic;
    __analysisStart = analysisStart;
    __analysisEnd = analysisEnd;
    __value1 = value1;
    __value2 = value2;
    __value3 = value3;
}

/**
Check the time series.
*/
public void calculateTimeSeriesStatistic ()
throws Exception
{
    String statistic = getStatistic();
    double value1 = (getValue1() == null) ? -999.0 : getValue1().doubleValue();
    double value2 = (getValue2() == null) ? -999.0 : getValue2().doubleValue();
    double value3 = (getValue3() == null) ? -999.0 : getValue3().doubleValue();
    TS ts = getTimeSeries();
    // If statistic takes more work, call other supporting code and then return
    if ( statistic.equalsIgnoreCase(TSStatistic.NqYY) ) {
        if ( !(ts instanceof DayTS) ) {
            throw new InvalidParameterException ( "Attempting to calculate statistic \"" + statistic +
                "\" on other than daily time series (TSID=" + ts.getIdentifierString() + ")");
        }
        NqYYFrequencyAnalysis nqyy = new NqYYFrequencyAnalysis ( (DayTS)ts, (int)(value1 + .01), value2,
            getAnalysisStart(), getAnalysisEnd(), (int)(value3 + .01) );
        nqyy.analyze();
        Double result = nqyy.getAnalysisResult();
        if ( result != null ) {
            setStatisticResult ( result );
        }
        return;
    }
    
    // Else, iterate through the time series and do the work here
    TSIterator tsi = ts.iterator(getAnalysisStart(), getAnalysisEnd());
    TSData data = null;

    /*
    String tsid = ts.getIdentifier().toString();
    if ( (ts.getAlias() != null) && !ts.getAlias().equals("") ) {
        tsid = ts.getAlias();
    }
    */
    double tsvalue; // time series data value
    //double tsvaluePrev = 0; // time series data value (previous) - will be used for change in value
    DateTime date; // Date corresponding to data value
    boolean isMissing;
    boolean sumNeeded = false;
    if ( statistic.equalsIgnoreCase("Mean") ) {
        sumNeeded = true;
    }
    int countNotMissing = 0;
    int countMissing = 0;
    //double diff;
    double sum = ts.getMissing(); // Sum of data values
    boolean sumCalculated = false; // To improve performance
    double statisticResult = ts.getMissing();
    DateTime statisticResultDateTime = null;
    boolean statisticCalculated = false;
    while ( (data = tsi.next()) != null ) {
        // Analyze the value - do this brute force with string comparisons and improve performance once logic is in place
        date = tsi.getDate();
        tsvalue = data.getData();
        isMissing = ts.isDataMissing(tsvalue);
        if ( isMissing ) {
            ++countMissing;
        }
        else {
            ++countNotMissing;
        }
        if ( sumNeeded ) {
            if ( !isMissing ) {
                if ( !sumCalculated ) {
                    // Initialize to total
                    sum = tsvalue;
                    sumCalculated = true;
                }
                else {
                    // Increment total
                    sum += tsvalue;
                }
            }
        }
        if ( statistic.equalsIgnoreCase(TSStatistic.Max) ) {
            if ( !isMissing ) {
                if ( !statisticCalculated || (tsvalue > statisticResult)) {
                    statisticResult = tsvalue;
                    statisticResultDateTime = new DateTime(date);
                    statisticCalculated = true;
                }
            }
        }
        else if ( statistic.equalsIgnoreCase(TSStatistic.Min) ) {
            if ( !isMissing ) {
                if ( !statisticCalculated || (tsvalue < statisticResult)) {
                    statisticResult = tsvalue;
                    statisticResultDateTime = new DateTime(date);
                    statisticCalculated = true;
                }
            }
        }
        //tsvaluePrev = tsvalue;
    }
    // Set the final statistic value
    // Handle counts differently to improve performance
    if ( statistic.equalsIgnoreCase(TSStatistic.Count) ) {
        setStatisticResult ( new Integer(countNotMissing + countMissing) ); 
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.Mean) ) {
        if ( countNotMissing > 0 ) {
            setStatisticResult ( new Double(sum/(double)countNotMissing) );
        }
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.MissingCount) ) {
        setStatisticResult ( new Integer(countMissing) ); 
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.MissingPercent) ) {
        int countTotal = countNotMissing + countMissing;
        if ( countTotal != 0 ) {
            setStatisticResult ( new Double((double)countMissing/(double)countTotal) );
        }
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.NonmissingCount) ) {
        setStatisticResult ( new Integer(countNotMissing) ); 
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.NonmissingPercent) ) {
        int countTotal = countNotMissing + countMissing;
        if ( countTotal != 0 ) {
            setStatisticResult ( new Double((double)countNotMissing/(double)countTotal) );
        }
    }
    else if ( statisticCalculated ) {
        // All other statistics - numerical values - just set from values computed locally above
        setStatisticResult ( statisticResult );
        setStatisticResultDateTime ( statisticResultDateTime );
    }
}

/**
Return the analysis end date/time.
@return the analysis end date/time.
*/
public DateTime getAnalysisEnd ()
{
    return __analysisEnd;
}

/**
Return the analysis start date/time.
@return the analysis start date/time.
*/
public DateTime getAnalysisStart ()
{
    return __analysisStart;
}

/**
Return the name of the statistic being calculated.
@return the name of the statistic being calculated.
*/
public String getStatistic ()
{
    return __statistic;
}

/**
Return the value of the statistic that was calculated.
@return the value of the statistic that was calculated, as a Double for floating point statistics and
Integer for integer statistics.
*/
public Object getStatisticResult()
{
    return __statisticResult;
}

/**
Return the date/time for the statistic that was calculated.
@return the date/time for the statistic that was calculated.
*/
public DateTime getStatisticResultDateTime()
{
    return __statisticResultDateTime;
}

/**
Get the list of statistics that can be performed.
*/
public static List<String> getStatisticChoices()
{
    List<String> choices = new Vector();
    choices.add ( TSStatistic.Count );
    choices.add ( TSStatistic.Max );
    choices.add ( TSStatistic.Mean );
    choices.add ( TSStatistic.Min );
    choices.add ( TSStatistic.MissingCount );
    choices.add ( TSStatistic.MissingPercent );
    choices.add ( TSStatistic.NonmissingCount );
    choices.add ( TSStatistic.NonmissingPercent );
    choices.add ( TSStatistic.NqYY );
    return choices;
}

/**
Return the number of values that are required to evaluate a criteria.
@return the number of values that are required to evaluate a criteria.
@param checkCriteria the check criteria that is being evaluated.
*/
public static int getRequiredNumberOfValuesForStatistic ( String statistic )
{
    // TODO SAM 2009-04-23 Need to convert to enumeration or something other than simple strings
    if ( statistic.equalsIgnoreCase(TSStatistic.Count) ) {
        return 0;
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.Max) ) {
        return 0;
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.Max) ) {
        return 0;
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.Mean) ) {
        return 0;
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.Min) ) {
        return 0;
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.MissingCount) ) {
        return 0;
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.MissingPercent) ) {
        return 0;
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.NonmissingCount) ) {
        return 0;
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.NonmissingPercent) ) {
        return 0;
    }
    else if ( statistic.equalsIgnoreCase(TSStatistic.NqYY) ) {
        // Need days to average (N), return frequency (YY), and allowed missing in average
        return 3;
    }
    else {
        String message = "Requested statistic is not recognized: " + statistic;
        String routine = "TSUtil_CalculateTimeSeriesStatistic.getRequiredNumberOfValuesForStatistic";
        Message.printWarning(3, routine, message);
        throw new InvalidParameterException ( message );
    }
}

/**
Return the time series being analyzed.
@return the time series being analyzed.
*/
public TS getTimeSeries ()
{
    return __ts;
}

/**
Return Value1 for the check.
@return Value1 for the check.
*/
public Double getValue1 ()
{
    return __value1;
}

/**
Return Value2 for the check.
@return Value2 for the check.
*/
public Double getValue2 ()
{
    return __value2;
}

/**
Return Value3 for the check.
@return Value3 for the check.
*/
public Double getValue3 ()
{
    return __value3;
}

/**
Set the statistic result.
*/
private void setStatisticResult ( double statisticResult )
{
    __statisticResult = new Double(statisticResult);
}

/**
Set the statistic result, for integer statistics like count.
*/
private void setStatisticResult ( int statisticResult )
{
    __statisticResult = new Integer(statisticResult);
}

/**
Set the date/time for statistic result.
*/
private void setStatisticResultDateTime ( DateTime statisticResultDateTime )
{
    __statisticResultDateTime = statisticResultDateTime;
}

}