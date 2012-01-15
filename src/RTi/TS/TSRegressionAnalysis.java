package RTi.TS;

import RTi.Util.Math.DataTransformationType;
import RTi.Util.Math.RegressionData;
import RTi.Util.Math.RegressionType;
import RTi.Util.Time.DateTime;

/**
Perform a regression analysis on two time series using either Ordinary Least Squares (OLS) or
Maintenance of Variation 2 (MOVE2) approach.  This code is written in a way to allow for the
Mixed Station Analysis (MSA) to utilize the code.  Consequently, a number of input parameters and
calculations go beyond basic regression analysis.
*/
public class TSRegressionAnalysis
{
    
/**
Independent, X.
*/
private TS __xTS = null;

/**
Dependent, Y.
*/
private TS __yTS = null;

/**
Analysis method (regression type).
*/
private RegressionType __analysisMethod = RegressionType.OLS_REGRESSION; // Default

/**
Whether to analyze a single equation.
*/
private boolean __analyzeSingleEquation = true;

/**
Whether to analyze monthly equations.
*/
private boolean __analyzeMonthlyEquations = true;

/**
Analysis period start for the dependent (Y) time series.
*/
private DateTime __dependentAnalysisStart = null;

/**
Analysis period end for the dependent (Y) time series.
*/
private DateTime __dependentAnalysisEnd = null;

/**
Analysis period start for the independent (X) time series.  For OLS and MOVE2,
this is the same as the dependent analysis period.  For MOVE2 it can be different.
*/
private DateTime __independentAnalysisStart = null;

/**
Analysis period end for the independent (X) time series.  For OLS and MOVE2,
this is the same as the dependent analysis period.  For MOVE2 it can be different.
*/
private DateTime __independentAnalysisEnd = null;

/**
List of month numbers to analyze when using one equation, where each month is 1-12 (Jan - Dec),
or null to analyze all months.
*/
private int [] __analysisMonths = null;

/**
Indicates the data transformation.
*/
private DataTransformationType __transformation = null;

/**
Data value to substitute for the original when using a log transform and the original value is <= 0.
Can be any number > 0.
TODO SAM 2010-12-17 Allow NaN to throw the number away, but this changes counts, etc.
*/
private Double __leZeroLogValue = new Double(getLEZeroLogValueDefault()); // Default

/**
The intercept to force, or null if not forcing.  If set, only zero is allowed and it is only used with
OLS regression.
*/
private Double __intercept = null;

/**
Data used by the regression analysis - will be populated by the extractDataArraysFromTimeSeries() method.
*/
private TSRegressionData __tsRegressionData = null;

/**
Results of the regression analysis - will be populated by the determineRegressionRelationships() method.
*/
private TSRegressionResults __tsRegressionResults = null;

/**
Errors of the regression analysis - will be populated by the estimateEquationErrors() method.
*/
private TSRegressionErrors __tsRegressionErrors = null;

/**
Checks of the regression analysis, indicating if relationships are acceptable -
will be populated by the checkRegressionRelationships() method.
*/
private TSRegressionChecks __tsRegressionChecks = null;
    
/**
Constructor.  The input data are checked.
@param analysisMonths If one equation is being used, indicate the months that are to be analyzed.
If monthly equations are being used, indicate the one month to analyze.  ?? array is months to include ??
*/
public TSRegressionAnalysis ( TS independentTS, TS dependentTS, RegressionType analysisMethod,
    boolean analyzeSingleEquation, boolean analyzeMonthlyEquations, int [] analysisMonths,
    DataTransformationType transformation, Double leZeroLogValue, Double intercept,
    DateTime dependentAnalysisStart, DateTime dependentAnalysisEnd,
    DateTime independentAnalysisStart, DateTime independentAnalysisEnd )
{
    if ( independentTS == null ) {
        throw new IllegalArgumentException ( "Independent time series is null.  Cannot perform regression." );
    }
    else {
        __xTS = independentTS;
    }
    if ( dependentTS == null ) {
        throw new IllegalArgumentException ( "Dependent time series is null.  Cannot perform regression." );
    }
    else {
        __yTS = dependentTS;
    }
    if ( __analysisMethod == null ) {
        __analysisMethod = RegressionType.OLS_REGRESSION; // Default
    }
    else {
        __analysisMethod = analysisMethod;
    }
    __analyzeSingleEquation = analyzeSingleEquation;
    __analyzeMonthlyEquations = analyzeMonthlyEquations;
    if ( __analysisMonths == null ) {
        __analysisMonths = new int[0];
    }
    for ( int i = 0; i < __analysisMonths.length; i++ ) {
        if ( (__analysisMonths[i] < 1) || (__analysisMonths[i] > 12) ) {
            throw new IllegalArgumentException ( "Analysis month (" + __analysisMonths[i] + ") is not in range 1-12." );
        }
    }
    // OK if null...
    __analysisMonths = analysisMonths;
    if ( __transformation == null ) {
        __transformation = DataTransformationType.NONE; // Default
    }
    else {
         __transformation = transformation;
    }
    // If null use the default...
    if ( leZeroLogValue != null ) {
        __leZeroLogValue = leZeroLogValue;
    }
    __intercept = intercept;
    if ( (__intercept != null) && (__intercept != 0.0) ) {
        throw new IllegalArgumentException ( "Intercept (" + __intercept + ") can only be specified as zero." );
    }
    // Dependent analysis period
    // If dates are null, get from the time series
    if ( dependentAnalysisStart != null ) {
        __dependentAnalysisStart = new DateTime(dependentAnalysisStart);
    }
    else {
        __dependentAnalysisStart = new DateTime(__yTS.getDate1());
    }
    if ( dependentAnalysisEnd != null ) {
        __dependentAnalysisEnd = new DateTime(dependentAnalysisEnd);
    }
    else {
        __dependentAnalysisEnd = new DateTime(__yTS.getDate2());
    }
    // Independent analysis period...
    if ( __analysisMethod == RegressionType.OLS_REGRESSION ) {
        // Independent analysis period is the same as the dependent...
        __independentAnalysisStart = new DateTime(__dependentAnalysisStart);
        __independentAnalysisEnd = new DateTime(__dependentAnalysisEnd);
    }
    else if ( __analysisMethod == RegressionType.MOVE2 ) {
        if ( independentAnalysisStart != null ) {
            __independentAnalysisStart = new DateTime(independentAnalysisStart);
        }
        else {
            __independentAnalysisStart = new DateTime(__xTS.getDate1());
        }

        if ( independentAnalysisEnd != null ) {
            __independentAnalysisEnd = new DateTime(independentAnalysisEnd);
        }
        else {
            __independentAnalysisEnd = new DateTime(__xTS.getDate2());
        }
    }
    // Extract the data from the time series (needs to be done regardless of later steps and better to
    // do here and find problems early...
    extractDataArraysFromTimeSeries();
}

// TODO SAM 2012-01-14 Does it make sense to allow absolute value of R to check inverse relationships?
// TODO SAM 2012-01-14 Does it make sense to have these criteria be monthly?
/**
<p>
Check the relationships against criteria, including:
<ol>
<li> is the sample size (number of over overlapping points) large enough?</li>
<li> is the minimum R met?</li>
<li> is the confidence interval met?</li>
</ol>
</p>
<p>
This results in the internal TSRegressionChecks being populated.
</p>
*/
private void checkRegressionRelationships (
    Integer minimumSampleSize, Double minimumR, Double confidenceInterval )
{
    // Rest to defaults if necessary
    if ( minimumSampleSize == null ) {
        minimumSampleSize = 2; // Less than this and will have division by zero
    }
    // Initialize check values
    boolean okSingleSampleSize = false;
    boolean [] okMonthlySampleSize = new boolean[12];
    boolean okSingleR = false;
    boolean [] okMonthlyR = new boolean[12];
    boolean okSingleTtest = false;
    boolean [] okMonthlyTtest = new boolean[12];
    // Check the data and relationship information and assign check results
    // Check the minimum sample size...
    TSRegressionData data = getTSRegressionData ();
    TSRegressionResults results = getTSRegressionResults ();
    if ( data.getSingleEquationRegressionData().getN1() >= minimumSampleSize ) {
        okSingleSampleSize = true;
    }
    for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
        if ( data.getMonthlyEquationRegressionData(iMonth).getN1() >= minimumSampleSize ) {
            okMonthlySampleSize[iMonth - 1] = true;
        }
    }
    // Check the minimum R...
    if ( minimumR == null ) {
        // Always passes
        okSingleR = true;
        for ( int i = 0; i < 12; i++ ) {
            okMonthlyR[i] = true;
        }
    }
    else {
        if ( results.getSingleEquationRegressionResults().getCorrelationCoefficient() >= minimumR ) {
            okSingleSampleSize = true;
        }
        for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
            if ( results.getMonthlyEquationRegressionResults(iMonth).getCorrelationCoefficient() >= minimumR ) {
                okMonthlySampleSize[iMonth - 1] = true;
            }
        }
    }
    // Check the T test...
    // Check the minimum R...
    if ( confidenceInterval == null ) {
        // Always passes
        okSingleTtest = true;
        for ( int i = 0; i < 12; i++ ) {
            okMonthlyTtest[i] = true;
        }
    }
    else {
        // Have to check the T test...
    }
    // Finally, set the check results to indicate whether the relationships are within acceptable parameters
    setTSRegressionChecks ( new TSRegressionChecks ( minimumSampleSize, okSingleSampleSize, okMonthlySampleSize,
        minimumR, okSingleR, okMonthlyR,
        confidenceInterval, okSingleTtest, okMonthlyTtest) );
}

/**
Estimate error statistics from the relationship equations.  This is done by estimating each value in the
dependent time series that originally had a value and comparing the estimate to the original.
*/
private void calculateEquationErrors ()
{
    
}

/**
Determine the regression equation relationships and compute statistics.
*/
private void calculateRegressionRelationships ()
{
    
}

//TODO SAM 2012-01-14 Perhaps in the future this should omit data values flagged as being previously
//filled or otherwise not observations.
/**
Extract data arrays needed for the analysis.
*/
private void extractDataArraysFromTimeSeries ()
{
    // Get data used in this method
    TS xTS = getIndependentTS();
    TS yTS = getDependentTS();
    DateTime dependentAnalysisStart = getDependentAnalysisStart();
    DateTime dependentAnalysisEnd = getDependentAnalysisEnd();
    DateTime independentAnalysisStart = getIndependentAnalysisStart();
    DateTime independentAnalysisEnd = getIndependentAnalysisEnd();
    int [] analysisMonths = getAnalysisMonths();
    // Extract data from time series for single equation (may only contain specific months)...
    double [] x1Single = TSUtil.toArray(xTS, dependentAnalysisStart, dependentAnalysisEnd,
        analysisMonths, false, // Do not include missing
        true, // Match non-missing for the following time series
        yTS );
    double [] y1Single = TSUtil.toArray(yTS, dependentAnalysisStart, dependentAnalysisEnd,
        analysisMonths, false, // Do not include missing
        true, // Match non-missing for the following time series
        xTS );
    double [] x2Single = TSUtil.toArray(xTS, independentAnalysisStart, independentAnalysisEnd,
        analysisMonths, false, // Do not include missing
        false, // DO NOT match non-missing for the following time series
        yTS );
    RegressionData dataSingle = new RegressionData ( x1Single, y1Single, x2Single );
    // Extract data arrays from time series for monthly equations...
    double [][] x1Monthly = new double[12][];
    double [][] y1Monthly = new double[12][];
    double [][] x2Monthly = new double[12][];
    RegressionData [] dataMonthly = new RegressionData[12];
    for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
        // Only include requested months...
        if ( isMonthInAnalysis(iMonth,analysisMonths) ) {
            x1Monthly[iMonth - 1] = TSUtil.toArray(xTS, dependentAnalysisStart, dependentAnalysisEnd,
                analysisMonths, false, // Do not include missing
                true, // Match non-missing for the following time series
                yTS );
            y1Monthly[iMonth - 1] = TSUtil.toArray(yTS, dependentAnalysisStart, dependentAnalysisEnd,
                analysisMonths, false, // Do not include missing
                true, // Match non-missing for the following time series
                xTS );
            x2Monthly[iMonth - 1] = TSUtil.toArray(xTS, independentAnalysisStart, independentAnalysisEnd,
                analysisMonths, false, // Do not include missing
                false, // DO NOT match non-missing for the following time series
                yTS );
        }
        else {
            x1Monthly[iMonth - 1] = new double[0];
            y1Monthly[iMonth - 1] = new double[0];
            x2Monthly[iMonth - 1] = new double[0];
        }
        dataMonthly[iMonth - 1] = new RegressionData (x1Monthly[iMonth - 1], y1Monthly[iMonth - 1], x2Monthly[iMonth - 1]);
    }
    // Now store the input data in the TSRegressionData object
    setTSRegressionData ( new TSRegressionData ( xTS, yTS, dataSingle, dataMonthly ) );
}

/**
Return an array indicating the months to be analyzed, each value 1-12.  This information
corresponds to the AnalysisMonth property that is passed in at construction.
@return the array containing the months (1-12) to be analyzed, or null if all months will be analyzed.
*/
public int [] getAnalysisMonths ()
{   return __analysisMonths;
}

/**
Return the dependent time series analysis end.
@return the dependent time series analysis end.
*/
public DateTime getDependentAnalysisEnd()
{   return __dependentAnalysisEnd;
}

/**
Return the dependent time series analysis start.
@return the dependent time series analysis start.
*/
public DateTime getDependentAnalysisStart()
{   return __dependentAnalysisStart;
}

/**
Return the dependent (X) time series.
@return the dependent (X) time series.
*/
public TS getDependentTS()
{   return __yTS;
}

/**
Return the independent time series analysis end.
@return the independent time series analysis end.
*/
public DateTime getIndependentAnalysisEnd()
{   return __independentAnalysisEnd;
}

/**
Return the independent time series analysis start.
@return the independent time series analysis start.
*/
public DateTime getIndependentAnalysisStart()
{   return __independentAnalysisStart;
}

/**
Return the independent (X) time series.
@return the independent (X) time series.
*/
public TS getIndependentTS()
{   return __xTS;
}

/**
Return the default value that will be used for the log transform if the original is <= 0.
@return the default value that will be used for the log transform if the original is <= 0.
*/
public static double getLEZeroLogValueDefault ()
{   return .001;
}

/**
Return the checks of the regression analysis.
@return the checks of the regression analysis.
*/
public TSRegressionChecks getTSRegressionChecks ()
{   return __tsRegressionChecks;
}

/**
Return the data used as input to the regression analysis.
@return the data used as input to the regression analysis.
*/
public TSRegressionData getTSRegressionData ()
{   return __tsRegressionData;
}

/**
Return the regression analysis results.
@return the regression analysis results.
*/
public TSRegressionResults getTSRegressionResults ()
{   return __tsRegressionResults;
}

/**
Indicate whether the month should be included in the analysis.
@return true if analysisMonths is not specified (meaning analyze all months) or analysisMonths
is specified and contains a matching month (where months are 1 to 12).
@param month the month of interest (1 to 12)
@param analysisMonths the months to analyze
*/
private boolean isMonthInAnalysis ( int month, int [] analysisMonths )
{
    if ( (analysisMonths == null) || (analysisMonths.length == 0) ) {
        return true;
    }
    for ( int iMonth = 0; iMonth < analysisMonths.length; iMonth++ ) {
        if ( month == analysisMonths[iMonth] ) {
            return true;
        }
    }
    return false;
}

/**
Set the TSRegressionChecks indicating whether the relationships are OK.
@param tsRegressionChecks the regression checks object.
*/
private void setTSRegressionChecks ( TSRegressionChecks tsRegressionChecks )
{
    __tsRegressionChecks = tsRegressionChecks;
}

/**
Set the TSRegressionData used in the analysis.
@param tsRegressionData the regression data used as input to the analysis.
*/
private void setTSRegressionData ( TSRegressionData tsRegressionData )
{
    __tsRegressionData = tsRegressionData;
}

/**
Set the TSRegressionErrors from the analysis.
@param tsRegressionErrors the regression errors estimated using the regression relationships.
*/
private void setTSRegressionErrors ( TSRegressionErrors tsRegressionErrors )
{
    __tsRegressionErrors = tsRegressionErrors;
}

/**
Set the TSRegressionResults from in the analysis.
@param tsRegressionResults the regression results from the analysis.
*/
private void setTSRegressionResults ( TSRegressionData tsRegressionResults )
{
    __tsRegressionData = tsRegressionResults;
}

}