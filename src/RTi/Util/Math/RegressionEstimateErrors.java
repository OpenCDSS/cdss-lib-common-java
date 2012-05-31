package RTi.Util.Math;

/**
This class provides storage for regression analysis errors, determined by using the
results of the regression analysis to estimate values that were previously known (in N1) and then measuring the
error between the original value and the estimated value.  This class currently only stores the results of
the estimation - the calculations are performed elsewhere due to complications with transformations, etc.
*/
public class RegressionEstimateErrors
{
    
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
public RegressionEstimateErrors ( double [] Y1est, Double rmse, Double see, Double seSlope )
{
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