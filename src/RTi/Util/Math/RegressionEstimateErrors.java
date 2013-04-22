package RTi.Util.Math;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.TestUtils;

/**
This class provides storage for regression analysis errors, determined by using the
results of the regression analysis to estimate values that were previously known (in N1) and then measuring the
error between the original value and the estimated value.  This class currently only stores the results of
the estimation - the calculations are performed elsewhere due to complications with transformations, etc.
*/
public class RegressionEstimateErrors
{
    
/**
Regression data used as input to compute the relationships.
*/
private RegressionData __regressionData = null;

/**
Regression results analyzing the relationships.
*/
private RegressionResults __regressionResults = null;
    
/**
Array of estimated dependent values in N1.
*/
private double [] __Y1est = new double[0];
    
/**
Mean of estimated Y in N1.
*/
private Double __meanY1est = null;

/**
Standard deviation of estimated Y in N1.
*/
private Double __stddevY1est = null;
    
/**
RMS Error for untransformed data.
*/
private Double __rmse = null;

/**
Standard error of estimate for untransformed data.
*/
private Double __see = null;

/**
Standard error of slope, for untransformed values.
*/
private Double __seSlope = null;

/**
Constructor.
@param Y1est dependent time series values estimated using the regression relationship
@param rmse RMSE computed for the estimated values
@param see standard error of estimate computed for the estimated values
@param seSlope standard error of the slope computed for the estimated values
*/
public RegressionEstimateErrors ( RegressionData data, RegressionResults results, double [] Y1est, Double rmse, Double see, Double seSlope )
{
    setRegressionData(data);
    setRegressionResults(results);
    setY1est(Y1est);
    setRMSE(rmse);
    setStandardErrorOfEstimate(see);
    setStandardErrorOfSlope(seSlope);
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable {
	super.finalize();
}

/**
Return the mean for the estimated dependent array, or null if not analyzed.
@return the mean for the estimated dependent array, or null if not analyzed.
*/
public Double getMeanY1est ()
{   Double meanY1est = __meanY1est;
    if ( meanY1est == null ) {
        meanY1est = MathUtil.mean(getY1est());
        setMean1Yest ( meanY1est );
    }
    return meanY1est;
}

/**
Return the RMS error (in original data units), or null if not analyzed.
@return The RMS error (in original data units), or null if not analyzed.
*/
public Double getRMSE ()
{	return __rmse;
}

/**
Return the regression data used for the error estimate.
*/
public RegressionData getRegressionData ()
{
    return __regressionData;
}

/**
Return the regression results used for the error estimate.
*/
public RegressionResults getRegressionResults ()
{
    return __regressionResults;
}

/**
Return the standard deviation for the estimated dependent array, or null if not analyzed.
@return the standard deviation for the estimated dependent array, or null if not analyzed.
*/
public Double getStandardDeviationY1est ()
{   Double stddevY1est = __stddevY1est;
    if ( stddevY1est == null ) {
        stddevY1est = MathUtil.standardDeviation(getY1est());
        setStandardDeviationY1est( stddevY1est );
    }
    return stddevY1est;
}

/**
Return the standard error of estimate, or null if not analyzed.
@return the standard error of estimate, or null if not analyzed.
*/
public Double getStandardErrorOfEstimate ()
{   return __see;
}

/**
Return the standard error of the slope in original data units, or null if not analyzed.
@return the standard error of the slope in original data units, or null if not analyzed.
*/
public Double getStandardErrorOfSlope ()
{   return __seSlope;
}

/**
Return the Student T Test quantile for the confidence interval (to be evaluated against the test score
for the slope of the regression line), to determine if it is a good relationship
@param confidenceIntervalPercent the confidence interval as a percent
@return null if unable to compute the quantile
*/
public Double getStudentTTestQuantile ( Double confidenceIntervalPercent )
{   String routine = getClass().getName() + ".getStudentTTestQuantile";
    if ( confidenceIntervalPercent == null ) {
        Message.printStatus(2,routine,"confidenceIntervalPercent is null - not computing quantile");
        return null;
    }
    if ( getRegressionData() == null ) {
        return null;
    }
    // Single tail exceedance probability in range 0 to 1.0
    // For example, a confidence interval of 95% will have p = .025
    double tScoreTestStat = getTestScore(getRegressionResults().getB());
    double alpha = (100.0 - confidenceIntervalPercent)/100.0;
    double alpha2 = alpha/2.0;
    double [] Y1 = getRegressionData().getY1();
    double [] Y1est = getY1est();
    if ( Y1 == null ) {
        return null;
    }
    if ( Y1est == null ) {
        return null;
    }
    // Length of array will be N1 by definition - subtract 2 for the intercept and slope to get
    // the degrees of freedom
    int dof = Y1est.length - 2;
    Double p = null;
    Message.printStatus(2,routine, "alpha=" + alpha );
    Message.printStatus(2,routine, "n=" + Y1est.length );
    Message.printStatus(2,routine, "dof=" + dof );
    Message.printStatus(2,routine, "T-score test statistic=" + tScoreTestStat );
    try {
        boolean useApacheMath = true;
        if ( useApacheMath ) {
            // Why is degrees of freedom a double?
            org.apache.commons.math3.distribution.TDistribution tDist = new TDistribution(dof);
            //p = tDist.inverseCumulativeProbability(alpha2); // Single tail value of alpha
            p = tDist.probability(tScoreTestStat); // Single tail value of alpha
            Message.printStatus(2,routine, "Single tail p-value=" + p + " alpha/2=" + alpha2 );
            //q = tDist.density((100.0 - p)/200.0); // Single tail value of alpha
            Message.printStatus(2,routine, StringUtil.formatArrays("Y1", Y1, "Y1est", Y1est, ",", "\n") );
            Message.printStatus(2,routine, "pairedT=" + TestUtils.pairedT(Y1,Y1est) );
            Message.printStatus(2,routine, "pairedTTest=" + TestUtils.pairedTTest(Y1,Y1est) );
            Message.printStatus(2,routine, "pairedTTest=" + TestUtils.pairedTTest(Y1,Y1est,alpha) );
            org.apache.commons.math3.distribution.TDistribution tDist2 = new TDistribution(99);
            double p2 = tDist.probability(2.29);
            Message.printStatus(2,routine, "Example p-value=" + p2 );
        }
        else {
            StudentTTest t = new StudentTTest();
            p = t.getStudentTQuantile(alpha2, dof );
        }
    }
    catch ( Exception e ) {
        // typically dof too small
        p = null; // Not computed
    }
    return p;
}

/**
Return the Student T Test score as b/SEslope
@param b the slope of the regression line
@return null if unable to compute the test score or Double.POSITIVE_INFINITY if division by zero.
*/
public Double getTestScore ( Double b )
{
    Double SEslope = getStandardErrorOfSlope();
    if ( (b == null) || (SEslope == null) ) {
        return null;
    }
    if ( b == 0.0 ) {
        return Double.POSITIVE_INFINITY;
    }
    return new Double(b/SEslope);
}

/**
Determine whether the relationship is valid for the confidence interval specified during analysis
@return true if the result of getTestScore() is >= the result from getStudentTTestQuantile();
return null if the inputs are not specified
*/
public Boolean getTestRelated ( Double testScore, Double testQuantile )
{   if ( (testScore == null) || (testQuantile == null) ) {
        return null;
    }
    else {
        if ( testScore >= testQuantile ) {
            return true;
        }
        else {
            return false;
        }
    }
}

/**
Return the estimated dependent array.
*/
public double [] getY1est ()
{
    return __Y1est;
}

/**
Set the mean for the estimated dependent data.
@param meanY1est Mean for the estimated dependent data.
*/
private void setMean1Yest ( Double meanY1est )
{   __meanY1est = meanY1est;
}

/**
Set the regression data.
*/
private void setRegressionData ( RegressionData regressionData )
{
    __regressionData = regressionData;
}

/**
Set the regression results.
*/
private void setRegressionResults ( RegressionResults regressionResults )
{
    __regressionResults = regressionResults;
}

/**
Set the RMS error.
@param rmse RMS error
*/
private void setRMSE ( Double rmse )
{	__rmse = rmse;
}

/**
Set the standard deviation for the estimated dependent data.
@param stddevY1est Standard deviation for the dependent data.
*/
private void setStandardDeviationY1est ( Double stddevY1est )
{   __stddevY1est = stddevY1est;
}

/**
Set the standard error of estimate, in original data units.
@param see the standard error of estimate, in original data units
*/
private void setStandardErrorOfEstimate ( Double see )
{   __see = see;
}

/**
Set the standard error of the slope, in original data units.
@param seSlope the standard error of the slope, in original data units
*/
private void setStandardErrorOfSlope ( Double seSlope )
{   __seSlope = seSlope;
}

/**
Set the Yest array - this is only called by the setYest() method if the array has not been constructed.
@param Y1est Yest array
*/
private void setY1est ( double [] Y1est )
{   __Y1est = Y1est;
}

}