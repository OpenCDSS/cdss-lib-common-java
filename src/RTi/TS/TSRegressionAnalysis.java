package RTi.TS;

import RTi.Util.Math.DataTransformationType;
import RTi.Util.Math.MathUtil;
import RTi.Util.Math.RegressionChecks;
import RTi.Util.Math.RegressionData;
import RTi.Util.Math.RegressionEstimateErrors;
import RTi.Util.Math.RegressionResults;
import RTi.Util.Math.RegressionType;
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
The confidence interval that is required for the relationship, triggers execution of the Student T Test
for the slope of the line.
*/
private Double __confidenceIntervalPercent = null;

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
private TSRegressionEstimateErrors __tsRegressionEstimateErrors = null;
private TSRegressionEstimateErrors __tsRegressionEstimateErrorsTransformed = null;

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
    DateTime independentAnalysisStart, DateTime independentAnalysisEnd,
    Double confidenceIntervalPercent )
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
    
    __analysisMonths = analysisMonths;
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
    if ( transformation == null ) {
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
    __confidenceIntervalPercent = confidenceIntervalPercent;
    // Extract the data from the time series (needs to be done regardless of later steps and better to
    // do here and find problems early)...
    extractDataArraysFromTimeSeries();
}

/**
Analyze the data for filling.  In this case the full analysis is performed including the following steps:
<ol>
<li>    Analyze the data to determine the regression relationships (parameters from the constructor are
        used to control the analysis).</li>
<li>    Estimate errors by using the regression relationships to estimate previous known values and
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
    calculateRegressionRelationships(getAnalysisMethod(), getTransformation(), getForcedIntercept());
    calculateEquationErrorsWhenFilling(getTransformation());
    if ( (minimumSampleSize == null) || (minimumSampleSize < 2) ) {
        // At least 2 points are needed to avoid division by zero in computations
        minimumSampleSize = 2;
    }
    checkRegressionRelationships(minimumSampleSize, minimumR, confidenceInterval);
}

/**
Set the analysis months mask, which is an array of 12 booleans that indicate whether a month's
data should be in the analysis.
Both input and output are 0 indexed.
@return an array of 12 booleans, where the first is for January, indicating whether the month should
be included in the analysis.
@param analysisMonths an array indicating which months are to be included in the analysis - if null
or empty all months will be included. January is 1.
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
This analysis is redundant in that if time are actually filled, the steps will be repeated.  However, separating
the analysis and the filling allows the analysis results to be evaluated to determine if the relationship
is valid.  This analysis is performed on all the data; however, a later filling step such as in the TSTool
FillRegression() command may fill on a smaller subset.
@param transformation transformation to be used for the data prior to the analysis
*/
private void calculateEquationErrorsWhenFilling ( DataTransformationType transformation ) {
    TSRegressionData regressionData = getTSRegressionData();
    TSRegressionData regressionDataTransformed = getTSRegressionDataTransformed();
    TSRegressionResults regressionResults = getTSRegressionResults();
    TSRegressionResults regressionResultsTransformed = getTSRegressionResultsTransformed();
    
    //single equation
    RegressionEstimateErrors placeholder = new RegressionEstimateErrors(
            null, null, new double[0], null, null, null);
    
    RegressionEstimateErrors[] temp = {placeholder,placeholder};
    
    if (__analyzeSingleEquation) {
    	//outsource the actual calculations (so we can do the same for monthly instead of copying code)
    	temp = calcRmseSeeSeSlope(transformation,
    			regressionResults.getSingleEquationRegressionResults(), regressionData.getSingleEquationRegressionData(),
    			regressionDataTransformed.getSingleEquationRegressionData(), regressionResultsTransformed.getSingleEquationRegressionResults() );
    }
    
    //put single results into final objects
    RegressionEstimateErrors singleEquationErrors = temp[0];
    RegressionEstimateErrors singleEquationErrorsTransformed = temp[1];
    
    
    RegressionEstimateErrors [] monthlyEquationErrors = new RegressionEstimateErrors[12];
    RegressionEstimateErrors [] monthlyEquationErrorsTransformed = new RegressionEstimateErrors[12];
    
    //monthly calculations
    //5-29-2013: Should the final be initialized beforehand so blanks show problems?
    if ( __analyzeMonthlyEquations) {
    	for ( int i = 1; i <= 12; i++ ) {
    		//get all the data....
    		RegressionResults resultsMonthly = regressionResults.getMonthlyEquationRegressionResults(i);
    		RegressionData dataMonthly = regressionData.getMonthlyEquationRegressionData(i);
    		RegressionData transformedDataMonthly = regressionDataTransformed.getMonthlyEquationRegressionData(i);
    		RegressionResults transformedResultsMonthly = regressionResultsTransformed.getMonthlyEquationRegressionResults(i);

    		//outsource calculations
    		RegressionEstimateErrors[] monthTemp = calcRmseSeeSeSlope(transformation, resultsMonthly,
    				dataMonthly, transformedDataMonthly, transformedResultsMonthly);

    		//put into final objects
    		monthlyEquationErrors[i-1] = monthTemp[0];
    		monthlyEquationErrorsTransformed[i-1] = monthTemp[1];

    		//clean up so we don't get previous values moving forward if errors
    		monthTemp = null;
    		resultsMonthly = null;
    		dataMonthly = null;
    		transformedDataMonthly = null;
    	}
    }
    
    setTSRegressionEstimateErrors(new TSRegressionEstimateErrors(singleEquationErrors, monthlyEquationErrors));
    setTSRegressionEstimateErrorsTransformed(
        new TSRegressionEstimateErrors(singleEquationErrorsTransformed, monthlyEquationErrorsTransformed));
}

/**
 * Calculates errors: RMSE, SEE, SESlope, and estimated Y1. Both transformed and untransformed.
 * Exists so single equation and monthly equation calculations can use the same code.
 * Should not be called outside of here; used to make calculateEquationErrorsWhenFilling cleaner.
 * 
 * @param transformation transformation to be used for the data prior to the analysis
 * @param results The RegressionResults to calculate the error for.
 * @param data The RegressionData to calculate the error for.
 * @param transformedData The RegressionData for the transformed series to calculate the error for.
 * @return RegressionEstimateErrors[] The objects we're looking for in the first place; 0 is untransformed, 1 is transformed.
 */
private RegressionEstimateErrors[] calcRmseSeeSeSlope(DataTransformationType transformation,
		RegressionResults results, RegressionData data, RegressionData transformedData, RegressionResults transformedResults) {

	//get all the data needed for calculations
	Double a;
	Double b;
	if (results == null) {
		//problem calculating relationship
		a = null;
		b = null;
	}
	else {
		a = results.getA();
		b = results.getB();
	}
	
    RegressionEstimateErrors untransformed = null;
    RegressionEstimateErrors transformed = null;
    RegressionEstimateErrors[] output = {untransformed, transformed};
    
    //temporary objects for output
    Double rmse = null;
    Double rmseTransformed = null;
    Double see = null;
    Double seeTransformed = null;
    Double seSlope = null;
    Double seSlopeTransformed = null;
    
	int n1 = data.getN1();
    double [] Y1transformedEstimated = new double[n1];
    double [] Y1estimated = new double[n1];
	
	if (a != null && b != null && !a.equals(Double.NaN) && !b.equals(Double.NaN) && data != null) {
		//If any of those were false, we didn't calculate a relationship, so don't bother calculating the errors.
		double [] X1 = data.getX1();
		double X1mean = data.getMeanX1();
		double [] Y1 = data.getY1();
		double X1transformedMean = transformedData.getMeanX1();
		double [] X1transformed = transformedData.getX1();
		double [] Y1transformed = transformedData.getY1();
		
		//initialize variables to hold final data
	    double rmseSingleSum = 0.0, rmseSingleTransformedSum = 0.0;
	    double seSlopeBottomSingleSum = 0.0, seeSlopeBottomSingleTransformedSum = 0.0;
	    
		for ( int i = 0; i < n1; i++ ) {
	        if ( transformation == DataTransformationType.LOG ) {
	            // RMSE calculated on transformed data...
	            Y1transformedEstimated[i] = a + X1transformed[i]*b;
	            rmseSingleTransformedSum +=
	                ((Y1transformedEstimated[i] - Y1transformed[i])*(Y1transformedEstimated[i] - Y1transformed[i]));
	            // Also un-transform estimate...
	            Y1estimated[i]=Math.pow(10.0, Y1transformedEstimated[i]);
	            // SESlope bottom term...
	            seeSlopeBottomSingleTransformedSum +=
	                ((X1transformed[i] - X1transformedMean)*((X1transformed[i] - X1transformedMean)));
	            seSlopeBottomSingleSum += ((X1[i] - X1mean)*((X1[i] - X1mean)));
	        }
	        else {
	            // RMSE calculate on raw data...
	            Y1estimated[i] = a + X1[i]*b;
	            // Transformed same as original since no transformation
	            Y1transformedEstimated[i] = Y1estimated[i];
	            // SESlope bottom term...
	            seSlopeBottomSingleSum += ((X1[i] - X1mean)*((X1[i] - X1mean)));
	            seeSlopeBottomSingleTransformedSum = seSlopeBottomSingleSum; // Same as non-transformed
	        }
			// Untransformed RMSE is always computed...
	        rmseSingleSum += ((Y1estimated[i] - Y1[i])*(Y1estimated[i] - Y1[i]));
	        if (transformation == DataTransformationType.NONE) {
	        	rmseSingleTransformedSum = rmseSingleSum; //same as non-transformed
	        }
	    }
	    // Final step computing statistics, taking sample size into account
	    // Transformed and untransformed are calculated (may be the same if no transformation)
	    if ( (n1 > 0) && (rmseSingleSum > 0.0) ) {
	    	//if not, something went wrong, so don't bother
	    	rmse = Math.sqrt(rmseSingleSum/n1);
	        rmseTransformed = Math.sqrt(rmseSingleTransformedSum/n1);
	    }
	    if ( ((n1 - 2) > 0)) {
	    	see = Math.sqrt(rmseSingleSum/(n1 - 2));
	    	seeTransformed = Math.sqrt(rmseSingleTransformedSum/(n1 - 2));
	    	seSlope = see/Math.sqrt(seSlopeBottomSingleSum);
	    	seSlopeTransformed = seeTransformed/Math.sqrt(seeSlopeBottomSingleTransformedSum);
	    }
	}
	
	untransformed = new RegressionEstimateErrors(data, results, Y1estimated, rmse, see, seSlope);
	transformed = new RegressionEstimateErrors(transformedData, transformedResults,
			Y1transformedEstimated, rmseTransformed, seeTransformed, seSlopeTransformed);
    
	output[0] = untransformed;
	output[1] = transformed;
	
	return output;
}

/**
Determine the regression equation relationships for all equations (the single equation and monthly equations)
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
{   String routine = getClass().getName() + ".calculateRegressionRelationships";
    if ( transformation == DataTransformationType.LOG ) {
        forcedIntercept = null;
    }
    // Always compute the regression relationships on the transformed data (raw and transformed will be
    // the same if no transformation is used)
    RegressionResults singleRegressionResults = null;
    TSRegressionData tsRegressionDataTransformed = getTSRegressionDataTransformed();
    
    if (__analyzeSingleEquation) {
	    try {
	        if ( Message.isDebugOn ) {
	            Message.printStatus(2, routine, "Calculating single equation relationship..." );
	        }
	        RegressionData data = tsRegressionDataTransformed.getSingleEquationRegressionData();
	        if (data.getX1().length == 0) {
	        	Message.printWarning(3, routine, "No overlap between "+__xTS.getLocation()+" and "+__yTS.getLocation()+"." );
	            singleRegressionResults = new RegressionResults(
	                data, forcedIntercept, Double.NaN, Double.NaN, Double.NaN );
	        }
	        else {
	        	singleRegressionResults = MathUtil.ordinaryLeastSquaresRegression(data, forcedIntercept);
	        }
	    }
	    catch ( Exception e ) {
	        Message.printWarning(3, routine, "Error computing single regression relationship (" + e + ")." );
	        singleRegressionResults = new RegressionResults(tsRegressionDataTransformed.getSingleEquationRegressionData(),
	            forcedIntercept, Double.NaN, Double.NaN, Double.NaN );
	    }
    }
    RegressionResults [] monthlyRegressionResults = new RegressionResults[12];
    if (__analyzeMonthlyEquations) {
	    for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
	        try {
	            if ( Message.isDebugOn ) {
	                Message.printStatus(2, routine, "Calculating month " + iMonth + " equation relationship..." );
	            }
	            RegressionData data = getTSRegressionDataTransformed().getMonthlyEquationRegressionData(iMonth);
	            if (data.getX1().length == 0) {
	            	//no overlap, don't go into regression and spit out a million errors
	            	Message.printWarning(3, routine, "No overlap between "+__xTS.getLocation()+" and "+__yTS.getLocation()+
	            		" at month "+iMonth);
		            monthlyRegressionResults[iMonth - 1] = new RegressionResults(
		                data, forcedIntercept, Double.NaN, Double.NaN, Double.NaN );
	            }
	            else {
	            	//we have data, everything's OK
	            	monthlyRegressionResults[iMonth - 1] =
	            		MathUtil.ordinaryLeastSquaresRegression(data, forcedIntercept);
	            }
	        }
	        catch ( Exception e ) {
	            Message.printWarning(3, routine, "Error computing month " + iMonth + " regression relationship (" + e + ") for "+
	            	__yTS.getLocation()+" and "+__xTS.getLocation()+"." );
	            Message.printWarning(3, routine, e);
	            monthlyRegressionResults[iMonth - 1] = new RegressionResults(
	                tsRegressionDataTransformed.getMonthlyEquationRegressionData(iMonth),
	                forcedIntercept, Double.NaN, Double.NaN, Double.NaN );
	        }
	    }
    }
    setTSRegressionResultsTransformed ( new TSRegressionResults(singleRegressionResults, monthlyRegressionResults));
    //if ( transformation == DataTransformationType.NONE ) {
        // Also set raw results to same as transformed...
        setTSRegressionResults ( getTSRegressionResultsTransformed() );
    //}
}

// TODO SAM 2012-01-14 Does it make sense to allow absolute value of R to check inverse relationships?
/**
<p>
Check the relationships against criteria, including:
<ol>
<li>is the sample size (number of over overlapping points) large enough?</li>
<li>is the minimum R met?</li>
<li>is the confidence interval met?</li>
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
    // Reset to defaults if necessary
    if ( minimumSampleSize == null || minimumSampleSize < 3 ) {
        minimumSampleSize = 3; // Less than this and will have division by zero
    }
    //Transformed will be same as untransformed if no transformation 
    TSRegressionData data = getTSRegressionDataTransformed ();
    TSRegressionResults results = getTSRegressionResults ();
    TSRegressionEstimateErrors errors = getTSRegressionErrorsTransformed ();
    // Finally, set the check results to indicate whether the relationships are within acceptable parameters
    
    //single
    RegressionChecks regressionChecksSingle = null;
    if (__analyzeSingleEquation) {
    	regressionChecksSingle = new RegressionChecks(
    			results.getSingleEquationRegressionResults().getIsAnalysisPerformedOK(),
    			minimumSampleSize, data.getSingleEquationRegressionData().getN1(),
    			minimumR, results.getSingleEquationRegressionResults().getCorrelationCoefficient(),
    			getConfidenceIntervalPercent(),
    			errors.getSingleEquationRegressionErrors().getTestRelated(
    					errors.getSingleEquationRegressionErrors().getTestScore(
    							results.getSingleEquationRegressionResults().getB()),
    							errors.getSingleEquationRegressionErrors().getStudentTTestQuantile(
    									getConfidenceIntervalPercent())));
    }
    
    //monthly
    RegressionChecks [] regressionChecksMonthly = new RegressionChecks[12];
    if (__analyzeMonthlyEquations) {
    	for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
    		Double testScore;
    		try {
    			//program mixed station analysis was based off took the absolute value of the test score
    			//so we're doing that here
    			testScore = Math.abs(errors.getMonthlyEquationRegressionErrors(iMonth).getTestScore(
						results.getMonthlyEquationRegressionResults(iMonth).getB()));
    		}
    		catch (NullPointerException e) {
    			//test score came out null, so couldn't take the absolute value
    			//set it to null
    			testScore = null;
    		}
    		regressionChecksMonthly[iMonth - 1] = new RegressionChecks(
    			results.getMonthlyEquationRegressionResults(iMonth).getIsAnalysisPerformedOK(),
    				minimumSampleSize, data.getMonthlyEquationRegressionData(iMonth).getN1(),
    				minimumR, results.getMonthlyEquationRegressionResults(iMonth).getCorrelationCoefficient(),
    				getConfidenceIntervalPercent(),
    				errors.getMonthlyEquationRegressionErrors(iMonth).getTestRelated(
    					testScore, errors.getMonthlyEquationRegressionErrors(iMonth).getStudentTTestQuantile(
    						getConfidenceIntervalPercent())));
    	}
    }
    setTSRegressionChecksTransformed ( new TSRegressionChecks ( regressionChecksSingle, regressionChecksMonthly) );
}

//TODO SAM 2012-01-14 Perhaps in the future this should omit data values flagged as being previously
//filled or otherwise not observations.
/**
Extract data arrays needed for the analysis.
*/
private void extractDataArraysFromTimeSeries ()
{   String routine = getClass().getName() + ".extractDataArraysFromTimeSeries";
    // Get data used in this method
    TS xTS = getIndependentTS();
    TS yTS = getDependentTS();
    DateTime dependentAnalysisStart = getDependentAnalysisStart();
    DateTime dependentAnalysisEnd = getDependentAnalysisEnd();
    DateTime independentAnalysisStart = getIndependentAnalysisStart();
    DateTime independentAnalysisEnd = getIndependentAnalysisEnd();
    int [] analysisMonths = getAnalysisMonths();
    boolean [] analysisMonthsMask = getAnalysisMonthsMask();
    // Extract data from time series for single equation (may only contain specific months)...
    RegressionData dataSingle = null;
    if (__analyzeSingleEquation) {
    	double [] x1Single = TSUtil.toArray(xTS, dependentAnalysisStart, dependentAnalysisEnd,
    			analysisMonths, false, // Do not include missing
    			true, // Match non-missing for the following time series
    			yTS, TSToArrayReturnType.DATA_VALUE );
    	double [] y1Single = TSUtil.toArray(yTS, dependentAnalysisStart, dependentAnalysisEnd,
    			analysisMonths, false, // Do not include missing
    			true, // Match non-missing for the following time series
    			xTS, TSToArrayReturnType.DATA_VALUE );
    	double [] x2Single = TSUtil.toArray(xTS, independentAnalysisStart, independentAnalysisEnd,
    			analysisMonths, false, // Do not include missing
    			false, // DO NOT match non-missing for the following time series
    			yTS, TSToArrayReturnType.DATA_VALUE );
    	double [] y3Single = TSUtil.toArray(yTS, dependentAnalysisStart, dependentAnalysisEnd,
    			analysisMonths, false, // Do not include missing
    			false, // DO NOT match non-missing for the following time series
    			xTS, TSToArrayReturnType.DATA_VALUE );
    	
		Message.printStatus(2, routine, "Size of data arrays (x1[overlap],y1[overlap]," +
				"x2[indep only],y3[dep only]]): " + x1Single.length + "," +
				y1Single.length + "," +
				x2Single.length + "," +
				y3Single.length );
    	dataSingle = new RegressionData ( x1Single, y1Single, x2Single, y3Single );
    }
    // Extract data arrays from time series for monthly equations...
    RegressionData [] dataMonthly = new RegressionData[12];
    if (__analyzeMonthlyEquations) {

    	for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
    		
    		//initialize arrays as empty, only add if there is data....
    		double [] x1Monthly = new double[0];
	    	double [] y1Monthly = new double[0];
	    	double [] x2Monthly = new double[0];
	    	double [] y3Monthly = new double[0];
    		
    		// Only include requested months...
    		if ( analysisMonthsMask[iMonth - 1] ) {
    			int [] analysisMonths2 = new int[1];
    			
    			analysisMonths2[0] = iMonth;
    			x1Monthly = TSUtil.toArray(xTS, dependentAnalysisStart, dependentAnalysisEnd,
    					analysisMonths2, false, // Do not include missing
    					true, // Match non-missing for the following time series
    					yTS, TSToArrayReturnType.DATA_VALUE );
    			y1Monthly = TSUtil.toArray(yTS, dependentAnalysisStart, dependentAnalysisEnd,
    					analysisMonths2, false, // Do not include missing
    					true, // Match non-missing for the following time series
    					xTS, TSToArrayReturnType.DATA_VALUE );
    			x2Monthly = TSUtil.toArray(xTS, independentAnalysisStart, independentAnalysisEnd,
    					analysisMonths2, false, // Do not include missing
    					false, // DO NOT match non-missing for the following time series
    					yTS, TSToArrayReturnType.DATA_VALUE );
    			y3Monthly = TSUtil.toArray(yTS, dependentAnalysisStart, dependentAnalysisEnd,
    					analysisMonths2, false, // Do not include missing
    					false, // DO NOT match non-missing for the following time series
    					xTS, TSToArrayReturnType.DATA_VALUE );
    		}

    		Message.printStatus(2, routine, "Size of data arrays (x1[overlap],y1[overlap]," +
    				"x2[indep only],y3[dep only]]) for month " + iMonth +
    				": " + x1Monthly.length + "," +
    				y1Monthly.length + "," +
    				x2Monthly.length + "," +
    				y3Monthly.length );
    		dataMonthly[iMonth - 1] = new RegressionData (x1Monthly, y1Monthly,
    				x2Monthly, y3Monthly);
    	}
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
        // Transform the original data
    	setTSRegressionDataTransformed(getTSRegressionData().transformLog10(getLEZeroLogValue(),
    			__analyzeMonthlyEquations, __analyzeSingleEquation));
    }
}

/**
Return whether or not the tests came out OK.
@return whether or not the tests were OK
*/
public boolean isOk() {
	if (__analyzeSingleEquation) {
		boolean[] test = getTSRegressionChecksMaskSingle();
		for (boolean single : test) {
			if (single) return single;
		}
		return false;
	}
	if (__analyzeMonthlyEquations) {
		boolean[] test = getTSRegressionChecksMaskMonthly();
		for (boolean month : test) {
			if (month) return month;
		}
		return false;
	}
	return false;
}

/**
Remove the dependent time series (not needed for filling in mixed station) for memory purposes.
*/
public void removeDependent() {
	__yTS = null;
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
Set the months to be analyzed.
Exists so that during a monthly fill, the same data set can be used for all months, simply changing which ones to ignore.
@param months The month numbers to use in the analysis.
*/
public void setAnalysisMonths(int[] months) {
	__analysisMonths = months;
	__analysisMonthsMask = calculateAnalysisMonthsMask ( __analysisMonths );
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
Return whether or not to analyze for a single equation. 
@return whether or not to analyze for a single equation
*/
public boolean getAnalyzeSingleEquation() {
	return __analyzeSingleEquation;
}

/**
Return whether or not to analyze for monthly equations
@return whether or not to analyze for monthly equations
*/
public boolean getAnalyzeMonthlyEquations() {
	return __analyzeMonthlyEquations;
}

/**
Return the confidence interval, percent.
@return the confidence interval, percent, or null if not specified.
*/
public Double getConfidenceIntervalPercent ()
{   return __confidenceIntervalPercent;
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
public TSRegressionEstimateErrors getTSRegressionEstimateErrors ()
{   return __tsRegressionEstimateErrors;
}

/**
Return the error estimate statistics for the regression analysis, for transformed data.
@return the error estimate statistics for the regression analysis, for transformed data.
*/
public TSRegressionEstimateErrors getTSRegressionErrorsTransformed ()
{   return __tsRegressionEstimateErrorsTransformed;
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
This array is useful rather than checking several individual bits of information.
@return the boolean[12] array indicating whether the equations for the months are valid. It is 0 indexed.
*/
public boolean [] getTSRegressionChecksMaskMonthly ()
{   String routine = "TSRegressionAnalysis.getTSRegressionChecksMaskMonthly";
    boolean [] analysisMonthsMask = getAnalysisMonthsMask();
    if ( __tsRegressionChecksMaskMonthly == null ) {
        // Have not yet constructed the data array so do it
        __tsRegressionChecksMaskMonthly = new boolean[12];
        TSRegressionChecks tsChecks = getTSRegressionChecksTransformed();
        Message.printStatus(2, routine, "Relationship: "+__xTS.getLocation()+" and "+__yTS.getLocation());
        for ( int i = 0; i < 12; i++ ) {
            // Initialize
            __tsRegressionChecksMaskMonthly[i] = false;
            // The relationships are valid only if the analysis months are enabled for the month and if the
            // checks have passed for the month
            if ( analysisMonthsMask[i] ) {
                // Now check each of the check criteria
                RegressionChecks checks = tsChecks.getMonthlyEquationRegressionChecks(i + 1);
                Message.printStatus(2, routine, "Monthly regression checks [" + i + "] = " + checks );

                //debug
                Message.printStatus(2, routine, "Test Score: "+getTSRegressionErrorsTransformed().getMonthlyEquationRegressionErrors(i+1).getTestScore(
                		getTSRegressionResultsTransformed().getMonthlyEquationRegressionResults(i+1).getB()));
                Message.printStatus(2, routine, "Test Quantile: "+getTSRegressionErrorsTransformed().getMonthlyEquationRegressionErrors(i+1).getStudentTTestQuantile(
						getConfidenceIntervalPercent()));
                //
                
                if ( checks == null ) {
                    __tsRegressionChecksMaskMonthly[i] = false;
                    Message.printWarning(3,routine,"Monthly equation regression checks is null for month [" +
                        i + "]" );
                }
                else if ( checks.getIsSampleSizeOK() && checks.getIsROK() && checks.getIsTestOK() &&
                    checks.getIsAnalysisPerformedOK() ) {
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
@return the boolean[12] array indicating whether the equations for the months are valid. It is 0 indexed.
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
                //Message.printStatus(2,"","Month [" + i + "] is in analysis.");
                // Now check each of the check criteria
            	Message.printStatus(2,"","Relationship for: "+__xTS.getLocation()+" and "+__yTS.getLocation());
                Message.printStatus(2,"","OK sample size [" + i + "] is " + checks.getIsSampleSizeOK() );
                Message.printStatus(2,"","OK minimum R [" + i + "] is " + checks.getIsROK() );
                Message.printStatus(2,"","OK confidenceInterval [" + i + "] is " + checks.getIsTestOK() );
                if ( checks.getIsSampleSizeOK() && checks.getIsROK() &&
                    ((checks.getIsTestOK() != null) && checks.getIsTestOK())
                    && checks.getIsAnalysisPerformedOK()) {
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
@param tsRegressionEstimateErrors the regression errors estimated using the regression relationships.
*/
private void setTSRegressionEstimateErrors ( TSRegressionEstimateErrors tsRegressionEstimateErrors )
{
    __tsRegressionEstimateErrors = tsRegressionEstimateErrors;
}

/**
Set the TSRegressionErrors from the analysis, for the transformed data.
@param tsRegressionEstimateErrorsTransformed the regression errors estimated using the regression relationships, for
transformed data.
*/
private void setTSRegressionEstimateErrorsTransformed ( TSRegressionEstimateErrors tsRegressionEstimateErrorsTransformed )
{
    __tsRegressionEstimateErrorsTransformed = tsRegressionEstimateErrorsTransformed;
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