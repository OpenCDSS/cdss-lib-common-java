package RTi.TS;

import java.security.InvalidParameterException;

import RTi.Util.Math.LogPearsonType3Distribution;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;

/**
Perform a low flow frequency analysis consistent with NqYY, conventions (e.g., 7q10).
In this case the analysis is applied to daily streamflow as follows (is not valid for other
than daily data):
<ol>
<li>Determine the 7-day average within a year.  For each day in a year, use 3 days prior to the
day and 3 days after the day to compute the average.  For end-points, use 3 days from an adjoining
year if necessary (4 days in the current year).  Select the lowest 7-day average for each year.</li>
<li>Rank the values for each year from low to high.</li>
<li>Use a log-Pearson Type III probability distribution (by default) to determine the 10-year recurrence
interval (nonexceedance probability of 10 percent).
</ol>
*/
public class NqYYFrequencyAnalysis
{
    
/**
Analysis result (the value of NqYY).
*/
private Double __analysisResult = null;

/**
Daily time series being analyzed.
*/
private DayTS __ts = null;

/**
Number of values to average.
*/
private int __numberInAverage = 7;

/**
Recurrence interval.
*/
private double __recurrenceInterval = 10;

/**
Start of analysis or null to analyze all data.
*/
private DateTime __analysisStart = null;

/**
End of analysis or null to analyze all data.
*/
private DateTime __analysisEnd = null;

/**
Number of missing values allowed in each averaging period.
*/
private int __allowMissingCount = 0;

/**
Construct the analysis object.
@param ts daily time series to analyze.
@param n Number of daily values to average (7 for 7q10).
@param yy Recurrence interval in years (10 for 7q10).
@param analysisStart the starting date/time for analysis (only the year is used).
@param analysisEnd the ending date/time for analysis (only the year is used).
@param allowMissingCount number of values allowed to be missing in the averaging period
*/
public NqYYFrequencyAnalysis ( DayTS ts, int n, double yy, DateTime analysisStart, DateTime analysisEnd,
    int allowMissingCount )
{
    setTS ( ts );
    // For now only allow odd number of days so that even number is on each side.
    if ( n%2 != 1 ) {
        throw new InvalidParameterException ( "Number of values to average must be odd." );
    }
    setNumberInAverage ( n );
    setRecurrenceInterval ( yy );
    if ( analysisStart != null ) {
        setAnalysisStart ( new DateTime(analysisStart) );
    }
    if ( analysisEnd != null ) {
        setAnalysisEnd ( new DateTime(analysisEnd) );
    }
    setAllowMissingCount ( allowMissingCount );
}

/**
Perform the analysis.
*/
public void analyze ()
throws Exception
{   String routine = getClass().getName() + ".analyze";
    DayTS ts = getTS();
    int numberInAverage = getNumberInAverage(); // Number expected to be included in each averaging period
    int allowMissingCount = getAllowMissingCount(); // Number of values allowed to be missing in each averaging period
    int bracket = numberInAverage/2; // number on each side to average
    double recurrenceInterval = getRecurrenceInterval();
    DateTime analysisStart = getAnalysisStart();
    DateTime analysisEnd = getAnalysisEnd();
    
    // Adjust the analysis period to be full years.
    
    if ( analysisStart == null ) {
        analysisStart = new DateTime ( ts.getDate1() );
    }
    if ( analysisEnd == null ) {
        analysisEnd = new DateTime ( ts.getDate2() );
    }
    
    // Number of average values to analyze (one per year) - may have missing values after initial fill.
    double [] yearMinimumArray = new double[analysisEnd.getYear() - analysisStart.getYear() + 1];
    
    int yearEnd = analysisEnd.getYear();
    double value; // time series value to analyze
    double sum;
    int sumCount = 0; // number of values to average
    double average; // average for values bracketing day
    DateTime dt = new DateTime ( analysisStart ); // used for iteration
    int dayIncrement;  // Value to increment day at start of "day" loop.
    DateTime dtSample = new DateTime(dt); // Date/time for sample loop
    DateTime yearMinimumDateTime = null; // DateTime for minimum N day average in year (center day)
    // Loop through the years to analyze
    for ( int year = analysisStart.getYear(); year <= yearEnd; year++ ) {
        // Loop through the days in the year
        double yearMinimum = Double.MAX_VALUE;
        dt.setYear ( year );
        dt.setMonth ( 1 );
        dt.setDay ( 1 );
        dayIncrement = 0;
        int daysInYear = TimeUtil.numDaysInYear(year);
        for ( int day = 1; day <= daysInYear; day++ ) {
            // Get the values needed for the average...
            dt.addDay( dayIncrement );
            if ( dayIncrement == 0 ) {
                dayIncrement = 1;
            }
            // For troubleshooting
            // Message.printStatus(2, routine, "Starting 7-day analysis on " + dt );
            dtSample.setMonth(dt.getMonth());
            dtSample.setYear(dt.getYear());
            dtSample.setDay(dt.getDay()); // Set day last because leap year may cause problem otherwise
            sum = 0;
            sumCount = 0;
            // Shift to initial offset day...
            dtSample.addDay( -bracket );
            for ( int dayOffset = -bracket; dayOffset <= bracket; dayOffset++, dtSample.addDay(1) ) {
                value = ts.getDataValue ( dtSample );
                if ( !ts.isDataMissing(value)) {
                    sum += value;
                    ++sumCount;
                }
            }
            // Compute the average value and check for the minimum
            if ( sumCount >= (numberInAverage - allowMissingCount) ) {
                average = sum/sumCount;
                if ( average < yearMinimum ) {
                    yearMinimum = average;
                    yearMinimumDateTime = new DateTime(dt);
                }
            }
            // Add to the main array
            yearMinimumArray[year - analysisStart.getYear()] = yearMinimum;
        }
        Message.printStatus ( 2, routine, "Minimum " + numberInAverage + " average for " + year +
            " is " + yearMinimum + " centered on day " + yearMinimumDateTime );
    }
    // Discard values in the array for years that could not be computed
    int discardCount = 0;
    for ( int i = 0; i < yearMinimumArray.length; i++ ) {
        if ( yearMinimumArray[i] == Double.MAX_VALUE ) {
            ++discardCount;
        }
    }
    double [] yearMinimumArray2 = yearMinimumArray;
    if ( discardCount > 0 ) {
        int nonMissingCount = 0;
        yearMinimumArray2 = new double[yearMinimumArray.length - discardCount];
        for ( int i = 0; i < yearMinimumArray.length; i++ ) {
            if ( yearMinimumArray[i] != Double.MAX_VALUE ) {
                yearMinimumArray2[nonMissingCount++] = yearMinimumArray[i];
            }
        }
        Message.printStatus(2, routine, "Ignored " + discardCount + " years because not enough data.");
    }
    
    // Determine the YY recurrence interval value...
    
    LogPearsonType3Distribution dist = new LogPearsonType3Distribution (yearMinimumArray2);
    double result = dist.calculateForRecurrenceInterval ( recurrenceInterval );
    setAnalysisResult ( result );
}

/**
Return the number of values allowed in each averaging period.
@return the number of values allowed in each averaging period.
*/
public int getAllowMissingCount ()
{
    return __allowMissingCount;
}

/**
Return the analysis end.
@return the analysis end, or null if the entire period is being analyzed.
*/
public DateTime getAnalysisEnd ()
{
    return __analysisEnd;
}

/**
Return the analysis result.
@return the analysis result, or null if not computed (e.g., too much missing data).
*/
public Double getAnalysisResult ()
{
    return __analysisResult;
}

/**
Return the analysis start.
@return the analysis start, or null if the entire period is being analyzed.
*/
public DateTime getAnalysisStart ()
{
    return __analysisStart;
}

/**
Return the number of values in the average.
@return the number of values in the average.
*/
public int getNumberInAverage ()
{
    return __numberInAverage;
}

/**
Return the recurrence interval.
@return the recurrence interval.
*/
public double getRecurrenceInterval ()
{
    return __recurrenceInterval;
}

/**
Return the time series being analyzed.
@return the time series being analyzed.
*/
public DayTS getTS ()
{
    return __ts;
}

/**
Set the number of values allowed to be missing in each averaging period.
@param the number of values allowed to be missing in each averaging period.
*/
private void setAllowMissingCount ( int allowMissingCount )
{
    __allowMissingCount = allowMissingCount;
}

/**
Set the end of the analysis period.
@param analysisEnd ending date/time for the analysis.
*/
private void setAnalysisEnd ( DateTime analysisEnd )
{
    __analysisEnd = analysisEnd;
}

/**
Set the analysis result.
@param analysisResult result of the analysis.
*/
private void setAnalysisResult ( double analysisResult )
{
    __analysisResult = new Double(analysisResult);
}

/**
Set the start of the analysis period.
@param analysisStart starting date/time for the analysis.
*/
private void setAnalysisStart ( DateTime analysisStart )
{
    __analysisStart = analysisStart;
}

/**
Set the number of values in the average.
@param numberInAverage number of values in the average for analysis.
*/
private void setNumberInAverage ( int numberInAverage )
{
    __numberInAverage = numberInAverage;
}

/**
Set the recurrence interval.
@param recurrenceInterval recurrence interval for analysis.
*/
private void setRecurrenceInterval ( double recurrenceInterval )
{
    __recurrenceInterval = recurrenceInterval;
}

/**
Set the time series being analyzed.
@param ts time series being analyzed.
*/
private void setTS ( DayTS ts )
{
    __ts = ts;
}

}