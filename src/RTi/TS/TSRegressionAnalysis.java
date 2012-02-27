package RTi.TS;

import RTi.Util.Math.DataTransformationType;
import RTi.Util.Math.MathUtil;
import RTi.Util.Math.RegressionChecks;
import RTi.Util.Math.RegressionData;
import RTi.Util.Math.RegressionErrors;
import RTi.Util.Math.RegressionResults;
import RTi.Util.Math.RegressionType;
import RTi.Util.Math.TDistribution;
import RTi.Util.Message.Message;
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
Array indicating whether months should be included in the analysis.
*/
private boolean [] __analysisMonthsMask = null;

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
private Double __forcedIntercept = null;

/**
Data used by the regression analysis - will be populated by the extractDataArraysFromTimeSeries() method.
*/
private TSRegressionData __tsRegressionData = null;
private TSRegressionData __tsRegressionDataTransformed = null;

/**
Results of the regression analysis - will be populated by the determineRegressionRelationships() method.
*/
private TSRegressionResults __tsRegressionResults = null;
private TSRegressionResults __tsRegressionResultsTransformed = null;

/**
Errors of the regression analysis - will be populated by the estimateEquationErrors() method.
*/
private TSRegressionErrors __tsRegressionErrors = null;
private TSRegressionErrors __tsRegressionErrorsTransformed = null;

/**
Checks of the regression analysis, indicating if relationships are acceptable -
will be populated by the checkRegressionRelationships() method.  The checks are performed on
transformed data since that is what the regression equation applies to.
*/
private TSRegressionChecks __tsRegressionChecksTransformed = null;

/**
Array indicating whether months have valid relationships (for a single equation).
*/
private boolean [] __tsRegressionChecksMaskSingle = null;

/**
Array indicating whether months have valid relationships (for a single equation).
*/
private boolean [] __tsRegressionChecksMaskMonthly = null;
    
/**
Constructor.  The input parameters are checked and the data are extracted from time series into arrays
needed for the analysis.  The analyzeForFilling() or analyzeForComparison() methods must be called to
perform the analysis.
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
    __analysisMonthsMask = calculateAnalysisMonthsMask ( __analysisMonths );
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
    __forcedIntercept = intercept;
    if ( (__forcedIntercept != null) && (__forcedIntercept != 0.0) ) {
        throw new IllegalArgumentException ( "Intercept (" + __forcedIntercept + ") can only be specified as zero." );
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
    // do here and find problems early)...
    extractDataArraysFromTimeSeries();
}

/**
This method is not yet implemented - it is envisioned to be used to compare time series, for example
when comparing model simulations against observed data during calibration.  As a work-around,
use the legacy TSRegression() class to analyze for comparison. 
*/
public void analyzeForComparison ()
{
    // TODO SAM 2012-01-15 Need to include some of the original logic from TSRegression here if the
    // comparison capability is enabled
    //calculateRegressionRelationships();
    //calculateEquationErrorsWhenComparing();
    //checkRegressionRelationships(minimumSampleSize, minimumR, confidenceInterval);
}

/**
Analyze the data for filling.  In this case the full analysis is performed including the following steps:
<ol>
<li>    Analyze the data to determine the regression relationships (parameters from the constructor are
        used to control the analysis).</li>
<li>    Estimate errors by using the regression relationships to estimate previous know values and
        determining the error from the differences (parameters from the constructor are used to control
        the analysis).</li>
<li>    Performing checks on the output to determine if the relationships are OK (parameters passed to this
        method are used to control the analysis).
</ol>
@param minimumSampleSize the minimum sample size that is accepted when checking the relationship(s)
@param mimimumR the minimum R that is accepted when checking the relationship(s)
@param confidenceInterval the confidence interval that is used to perform the T test when checking the
relationship(s)
*/
public void analyzeForFilling ( Integer minimumSampleSize, Double minimumR, Double confidenceInterval )
{
    // Single equation...
    calculateRegressionRelationships(getAnalysisMethod(), getTransformation(), getForcedIntercept());
    calculateEquationErrorsWhenFilling(getTransformation());
    if ( minimumSampleSize == null ) {
        // At least 2 points are needed to avoid division by zero
        minimumSampleSize = 2;
    }
    checkRegressionRelationships(minimumSampleSize, minimumR, confidenceInterval);
    // Monthly equations...
}

/**
Set the analysis months mask, which is an array of 12 booleans that indicate whether a month's
data should be in the analysis.
@return an array of 12 booleans, where the first is for January, indicating whether the month should
be included in the analysis.
@param analysisMonths an array indicating which months are to be included in the analysis - if null
or empty all months will be included
*/
private boolean [] calculateAnalysisMonthsMask ( int [] analysisMonths )
{
    boolean [] analysisMonthsMask = new boolean[12];
    if ( (analysisMonths == null) || (analysisMonths.length == 0) ) {
        for ( int i = 0; i < 12; i++ ) {
            analysisMonthsMask[i] = true;
        }
    }
    else {
        for ( int i = 0; i < 12; i++ ) {
            analysisMonthsMask[i] = false;
        }
        for ( int i = 0; i < analysisMonths.length; i++ ) {
            analysisMonthsMask[analysisMonths[i] - 1] = true;
        }
    }
    return analysisMonthsMask;
}

/**
Estimate error statistics from the relationship equations.  This is done by estimating each value in the
dependent time series that originally had a value and comparing the estimate to the original.
*/
private void calculateEquationErrorsWhenComparing ()
{
    /*
    double rmse = 0.0, rmseTransformed = 0.0;
    double [] Y1_estimated = null;  // Estimated Y1 if filling data.

    else {
        // Just use available data...
        double ytemp, xtemp;
        for ( int i = 0; i < n1; i++ ) {
            if ( __transformation == DataTransformationType.LOG ) {
                rmseTransformed += ((Y1_data[i] - X1_data[i])*(Y1_data[i] - X1_data[i]));
                // Always do untransformed data...
                ytemp = Math.pow(10.0, Y1_data[i]);
                xtemp = Math.pow(10.0, X1_data[i]);
                rmse += ((ytemp - xtemp)*(ytemp - xtemp));
            }
            else {
                rmse += ((Y1_data[i] - X1_data[i])*(Y1_data[i] - X1_data[i]));
            }
        }
    }
    */
}

/**
Estimate error statistics from the relationship equations.  This is done by estimating each value in the
dependent time series that originally had a value and comparing the estimate to the original.
*/
private void calculateEquationErrorsWhenFilling ( DataTransformationType transformation )
{
    /*
    double rmse = 0.0, rmseTransformed = 0.0;
    double [] Y1_estimated = null;  // Estimated Y1 if filling data.
    
        // Now if filling, estimate Y1 using A and B and compute the RMSE from Y1 - Y.
        // Just loop through the X1 because these points originally lined up with Y1...

        Y1_estimated = new double[n1];
        double ytemp1, ytemp2;
        for ( int i = 0; i < n1; i++ ) {
            if ( __transformation == DataTransformationType.LOG ) {
                Y1_estimated[i] = a + X1_data[i]*b;
                rmseTransformed += ((Y1_estimated[i] - Y1_data[i])*(Y1_estimated[i] - Y1_data[i]));
                // Always do untransformed data...
                ytemp1=Math.pow(10.0, Y1_estimated[i]);
                ytemp2=orig_Y1_data[i];
                rmse +=((ytemp1 - ytemp2)*(ytemp1 - ytemp2));
            }
            else {
                Y1_estimated[i] = a + X1_data[i]*b;
                rmse += ((Y1_estimated[i] - Y1_data[i])*(Y1_estimated[i] - Y1_data[i]));
            }
        }
        // Check to see if the relationship is within the confidence level...
        confidenceIntervalMet = true;
        if ( __confidenceInterval != null ) {
            // Get the limiting value given the confidence interval
            double alpha = (1.0 - __confidenceInterval.doubleValue()/100.0); // double-tailed
            double tMet = TDistribution.getTQuantile(alpha/2.0, n1 - 2 ); // Single-tailed so divide by 2
            Message.printStatus ( 2, routine, "T based on confidence interval = " + tMet );
            // Compute the statistic based on standard error of the estimate;
            //double ssxy = sxy - sx*my1;
            //double t = ssxy/see/Math.sqrt(ssx);
            //if ( t >= tMet ) {
            //    confidenceIntervalMet = true;
            //}
            
        }
    }
    */
    // FIXME SAM 2012-01-16 Enable calculating the statistics
    double rmse = 1.0, see = 2.0, seSlope = 3.0;
    RegressionErrors singleEquationErrors = new RegressionErrors(rmse, see, seSlope);
    RegressionErrors [] monthlyEquationErrors = new RegressionErrors[12];
    for ( int i = 0; i < 12; i++ ) {
        monthlyEquationErrors[i] = new RegressionErrors(rmse, see, seSlope);
    }
    setTSRegressionErrors(new TSRegressionErrors(singleEquationErrors, monthlyEquationErrors));
}

/**
Determine the regression equation relationships for all equation (the single equation and monthly equations)
and compute statistics.  Calculations are always performed on the transformed data (even if the transformation
is none, in which case the transformed data is the same as the original data).
@param analysisMethod the analysis method to use for regression
@param transformation the transformation used in the analysis
@param forcedIntercept the intercept to apply when analyzing - if a log10 transformation is being applied
then the data have already been transformed and the intercept will be set to null since it is not
allowed for log10 transform
*/
private void calculateRegressionRelationships ( RegressionType analysisMethod,
    DataTransformationType transformation, Double forcedIntercept )
{
    if ( transformation == DataTransformationType.LOG ) {
        forcedIntercept = null;
    }
    // Always compute the regression relationships on the transformed data (raw and transformed will be
    // the same if no transformation is used)
    RegressionResults singleRegressionResults = MathUtil.ordinaryLeastSquaresRegression(
        getTSRegressionDataTransformed().getSingleEquationRegressionData(), forcedIntercept);
    RegressionResults [] monthlyRegressionResults = new RegressionResults[12];
    for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
        monthlyRegressionResults[iMonth - 1] =
            MathUtil.ordinaryLeastSquaresRegression(
                getTSRegressionDataTransformed().getMonthlyEquationRegressionData(iMonth), forcedIntercept);
    }
    setTSRegressionResultsTransformed ( new TSRegressionResults(singleRegressionResults, monthlyRegressionResults));
    if ( transformation == DataTransformationType.NONE ) {
        // Also set raw results to same as transformed...
        setTSRegressionResults ( getTSRegressionResultsTransformed() );
    }
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
@param minimumSampleSize the minimum sample size that is accepted when checking the relationship(s)
@param mimimumR the minimum R that is accepted when checking the relationship(s)
@param confidenceInterval the confidence interval that is used to perform the T test when checking the
relationship(s)
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
    RegressionChecks regressionChecksSingle = new RegressionChecks(
        minimumSampleSize, okSingleSampleSize, minimumR, okSingleR, confidenceInterval, okSingleTtest );
    RegressionChecks [] regressionChecksMonthly = new RegressionChecks[12];
    for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
        regressionChecksMonthly[iMonth - 1] = new RegressionChecks(
            minimumSampleSize, okMonthlySampleSize[iMonth - 1],
            minimumR, okMonthlyR[iMonth - 1] ,
            confidenceInterval, okMonthlyTtest[iMonth - 1] );
    }
    setTSRegressionChecksTransformed ( new TSRegressionChecks ( regressionChecksSingle, regressionChecksMonthly) );
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
    double [] y3Single = TSUtil.toArray(yTS, dependentAnalysisStart, dependentAnalysisEnd,
        analysisMonths, false, // Do not include missing
        false, // DO NOT match non-missing for the following time series
        xTS );
    RegressionData dataSingle = new RegressionData ( x1Single, y1Single, x2Single, y3Single );
    // Extract data arrays from time series for monthly equations...
    double [][] x1Monthly = new double[12][];
    double [][] y1Monthly = new double[12][];
    double [][] x2Monthly = new double[12][];
    double [][] y3Monthly = new double[12][];
    RegressionData [] dataMonthly = new RegressionData[12];
    boolean [] analysisMonthsMask = getAnalysisMonthsMask();
    for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
        // Only include requested months...
        if ( analysisMonthsMask[iMonth - 1] ) {
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
            y3Monthly[iMonth - 1] = TSUtil.toArray(yTS, dependentAnalysisStart, dependentAnalysisEnd,
                analysisMonths, false, // Do not include missing
                false, // DO NOT match non-missing for the following time series
                xTS );
        }
        else {
            x1Monthly[iMonth - 1] = new double[0];
            y1Monthly[iMonth - 1] = new double[0];
            x2Monthly[iMonth - 1] = new double[0];
        }
        dataMonthly[iMonth - 1] = new RegressionData (x1Monthly[iMonth - 1], y1Monthly[iMonth - 1],
            x2Monthly[iMonth - 1], y3Monthly[iMonth - 1]);
    }
    // Store the input data in the TSRegressionData object
    setTSRegressionData ( new TSRegressionData ( xTS, yTS, dataSingle, dataMonthly ) );
    
    // If a transformation is requested, transform the data and store in a separate object
    DataTransformationType transformation = getTransformation();
    if ( transformation == DataTransformationType.NONE ) {
        // Just use the original data, ok since the data will not be modified
        setTSRegressionDataTransformed(getTSRegressionData());
    }
    else {
        // Go through each array and transform
        getTSRegressionData().transformLog10(getLEZeroLogValue());
    }
}



/**
Return a string summarizing a regression check failure, useful for logging.
@return a string summarizing a regression check failure, useful for logging
@param regressionData regression data
@param regressionChecks regression check criteria and results of check
@param regressionResults regression results
*/
public String formatInvalidRelationshipReason ( RegressionData regressionData,
    RegressionChecks regressionChecks, RegressionResults regressionResults )
{
    StringBuffer b = new StringBuffer();
    //if ( (getMinimumSampleSize() != null) &&  ) {
    //if ( getMinimumSampleSize() >= )
    return b.toString();
}

/**
Return the analysis method.
@return the analysis method.
*/
public RegressionType getAnalysisMethod ()
{
    return __analysisMethod;
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
Return an array indicating whether or not each month is to be analyzed.  This information
corresponds to the AnalysisMonth data but has been filled out for each month to facilitate use.
@return the boolean[12] array indicating whether the months should be analyzed.
*/
public boolean [] getAnalysisMonthsMask ()
{   return __analysisMonthsMask;
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
Return the dependent (Y) time series.
@return the dependent (Y) time series.
*/
public TS getDependentTS()
{   return __yTS;
}

/**
Return the forced intercept.
@return the forced intercept.
*/
public Double getForcedIntercept ()
{   return __forcedIntercept;
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
Return the value that will be used for the log transform if the original is <= 0.
@return the value that will be used for the log transform if the original is <= 0.
*/
public double getLEZeroLogValue ()
{   return __leZeroLogValue;
}

/**
Return the default value that will be used for the log transform if the original is <= 0.
This value can be used if calling code has not specified a value.
@return the default value that will be used for the log transform if the original is <= 0.
*/
public static double getLEZeroLogValueDefault ()
{   return .001;
}

/**
Get the transformation that is being applied to the data prior to the analysis.
@return the transformation that is being applied to the data prior to the analysis.
*/
public DataTransformationType getTransformation ( )
{   return __transformation;
}

/**
Return the checks of the regression analysis, using the transformed data (which will be the same as the
original data if no transformation).
@return the checks of the regression analysis, using the transformed data.
*/
public TSRegressionChecks getTSRegressionChecksTransformed ()
{   return __tsRegressionChecksTransformed;
}

/**
Return the data used as input to the regression analysis.
@return the data used as input to the regression analysis.
*/
public TSRegressionData getTSRegressionData ()
{   return __tsRegressionData;
}

/**
Return the error estimate statistics for the regression analysis.
@return the error estimate statistics for the regression analysis.
*/
public TSRegressionErrors getTSRegressionErrors ()
{   return __tsRegressionErrors;
}

/**
Return the error estimate statistics for the regression analysis, for transformed data.
@return the error estimate statistics for the regression analysis, for transformed data.
*/
public TSRegressionErrors getTSRegressionErrorsTransformed ()
{   return __tsRegressionErrorsTransformed;
}

/**
Return the data (transformed) used as input to the regression analysis.
@return the data (transformed) used as input to the regression analysis.
*/
public TSRegressionData getTSRegressionDataTransformed ()
{   return __tsRegressionDataTransformed;
}

/**
Return an array indicating whether or not each month has valid relationships (for example that can
then be used to fill missing data).  This information is determined when the relationships are checked.
@return the boolean[12] array indicating whether the equations for the months are valid.  If the analysis
is for a 
*/
public boolean [] getTSRegressionChecksMaskMonthly ()
{
    boolean [] analysisMonthsMask = getAnalysisMonthsMask();
    if ( __tsRegressionChecksMaskMonthly == null ) {
        // Have not yet constructed the data array so do it
        __tsRegressionChecksMaskMonthly = new boolean[12];
        TSRegressionChecks tsChecks = getTSRegressionChecksTransformed();
        for ( int i = 0; i < 12; i++ ) {
            // Initialize
            __tsRegressionChecksMaskMonthly[i] = false;
            // The relationships are valid only if the analysis months are enabled for the month and if the
            // checks have passed for the month
            if ( analysisMonthsMask[i] ) {
                // Now check each of the check criteria
                RegressionChecks checks = tsChecks.getMonthlyEquationRegressionChecks(i + 1);
                if ( checks.getIsSampleSizeOK() && checks.getIsROK() && checks.getIsConfidenceIntervalOK() ) {
                    __tsRegressionChecksMaskMonthly[i] = true;
                }
            }
        }
    }
    return __tsRegressionChecksMaskMonthly;
}

/**
Return an array indicating whether or not each month has valid relationships (for example that can
then be used to fill missing data).  This information is determined when the relationships are checked.
@return the boolean[12] array indicating whether the equations for the months are valid.  If the analysis
is for a 
*/
public boolean [] getTSRegressionChecksMaskSingle ()
{   boolean [] analysisMonthsMask = getAnalysisMonthsMask();
    if ( __tsRegressionChecksMaskSingle == null ) {
        // Have not yet constructed the data array so do it
        __tsRegressionChecksMaskSingle = new boolean[12];
        TSRegressionChecks tsChecks = getTSRegressionChecksTransformed();
        for ( int i = 0; i < 12; i++ ) {
            // Initialize
            __tsRegressionChecksMaskSingle[i] = false;
            // The relationships are valid only if the analysis months are enabled for the month and if the
            // checks have passed for the single equation
            RegressionChecks checks = tsChecks.getSingleEquationRegressionChecks();
            if ( analysisMonthsMask[i] ) {
                Message.printStatus(2,"","Month [" + i + "] is in analysis.");
                // Now check each of the check criteria
                Message.printStatus(2,"","Sample size [" + i + "] is " + checks.getIsSampleSizeOK() );
                Message.printStatus(2,"","Minimum R [" + i + "] is " + checks.getIsROK() );
                Message.printStatus(2,"","ConfidenceInterval [" + i + "] is " + checks.getIsConfidenceIntervalOK() );
                if ( checks.getIsSampleSizeOK() && checks.getIsROK() && checks.getIsConfidenceIntervalOK() ) {
                    __tsRegressionChecksMaskSingle[i] = true;
                }
            }
        }
    }
    return __tsRegressionChecksMaskSingle;
}

/**
Return the regression analysis results.
@return the regression analysis results.
*/
public TSRegressionResults getTSRegressionResults ()
{   return __tsRegressionResults;
}

/**
Return the regression analysis results, for the transformed data.
@return the regression analysis results, for the transformed data.
*/
public TSRegressionResults getTSRegressionResultsTransformed ()
{   return __tsRegressionResultsTransformed;
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
Set the TSRegressionData (transformed) used in the analysis.
@param tsRegressionData the regression data (transformed) used as input to the analysis.
*/
private void setTSRegressionDataTransformed ( TSRegressionData tsRegressionDataTransformed )
{
    __tsRegressionDataTransformed = tsRegressionDataTransformed;
}

/**
Set the TSRegressionChecks indicating whether the relationships are OK, based on the transformed data (which
will be the same as the original data for no transformation).
@param tsRegressionChecksTransformed the regression checks object.
*/
private void setTSRegressionChecksTransformed ( TSRegressionChecks tsRegressionChecksTransformed )
{
    __tsRegressionChecksTransformed = tsRegressionChecksTransformed;
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
private void setTSRegressionResults ( TSRegressionResults tsRegressionResults )
{
    __tsRegressionResults = tsRegressionResults;
}

/**
Set the TSRegressionResults from in the analysis, for transformed data values.
@param tsRegressionResultsTransformed the regression results from the analysis, for transformed data values.
*/
private void setTSRegressionResultsTransformed ( TSRegressionResults tsRegressionResultsTransformed )
{
    __tsRegressionResultsTransformed = tsRegressionResultsTransformed;
}

}