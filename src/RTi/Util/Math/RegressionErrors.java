package RTi.Util.Math;

/**
This class provides storage for regression analysis errors, generally determined by using the
results of the regression analysis to estimate values that were previously known and then measuring the
error between the original value and the estimated value.
*/
public class RegressionErrors
{
    
/**
Mean of estimated Y in N1.
*/
private Double __meanY1Estimated = null;

/**
Standard deviation of estimated Y in N1.
*/
private Double __stddevY1Estimated = null;
    
/**
RMS Error for untransformed data.
*/
private Double __rmse = null;

/**
RMS Error for transformed data.
*/
//private Double __rmseTransformed = null;

/**
Standard error of estimate for untransformed data.
*/
private Double __see = null;

/**
Standard error of estimate for transformed data.
*/
//private Double __seeTransformed = null;

/**
Standard error of prediction (SEP) for untransformed data.
*/
private Double __sep = null;

/**
Standard error of prediction (SEP), for transformed values.
*/
//private Double __sepTransformed = null;

/**
Standard error of slope, for untransformed values.
*/
private Double __seSlope = null;

/**
Standard error (SE) of slope, for transformed values.
*/
//private Double __seSlopeTransformed = null;

/**
Default constructor.  Typically the values in this class are set by the MathUtil.regress*() or similar methods.
The default values are all null, indicating that an analysis was not completed.
@param transformation the transformation that is applied to data before analysis
@param forcedIntercept the forced intercept (must be zero), or null to not force the intercept - can only
be specified when no transform
*/
public RegressionErrors ( Double rmse, Double see, Double seSlope )
{
    __rmse = rmse;
    __see = see;
    __seSlope = seSlope;
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
Return the mean for the estimated dependent array in the N1 sample, or null if not analyzed.
@return the mean for the estimated dependent array in the N1 sample, or null if not analyzed.
*/
public Double getMeanY1Estimated ()
{   return __meanY1Estimated;
}

/**
Return the RMS error (in original data units), or null if not analyzed.
@return The RMS error (in original data units), or null if not analyzed.
*/
public Double getRMSE ()
{	return __rmse;
}

/**
Return the RMS error (in transformed data units), or null if not analyzed.
@return The RMS error (in transformed data units), or null if not analyzed.
*/
/*
public Double getRMSETransformed ()
{   return __rmseTransformed;
}
*/

/**
Return the standard deviation for the estimated dependent array in the N1 sample, or null if not analyzed.
@return the standard deviation for the estimated dependent array in the N1 sample, or null if not analyzed.
*/
public Double getStandardDeviationY1Estimated ()
{   return __stddevY1Estimated;
}

/**
Return the standard error of estimate, or null if not analyzed.
@return the standard error of estimate, or null if not analyzed.
*/
public Double getStandardErrorOfEstimate ()
{   return __see;
}

/**
Return the standard error of estimate for transformed data, or null if not analyzed.
@return the standard error of estimate for transformed data, or null if not analyzed.
*/
/*
public Double getStandardErrorOfEstimateTransformed ()
{   return __seeTransformed;
}
*/

/**
Return the standard error of prediction, or null if not analyzed.
@return the standard error of prediction, or null if not analyzed.
*/
public Double getStandardErrorOfPrediction ()
{   return __sep;
}

/**
Return the standard error of prediction for transformed data, or null if not analyzed.
@return the standard error of prediction for transformed data, or null if not analyzed.
*/
/*
public Double getStandardErrorOfPredictionTransformed ()
{   return __sepTransformed;
}
*/

/**
Return the standard error of the slope in original data units, or null if not analyzed.
@return the standard error of the slope in original data units, or null if not analyzed.
*/
public Double getStandardErrorOfSlope ()
{   return __seSlope;
}

/**
Return the standard error of the slope for transformed data, or null if not analyzed.
@return the standard error of the slope for transformed data, or null if not analyzed.
*/
/*
public Double getStandardErrorOfSlopeTransformed ()
{   return __seSlopeTransformed;
}
*/

/**
Set the mean for the estimated dependent data in the N1 sample.
@param meanY1Estimated Mean for the estimated dependent data in the N1 sample.
*/
public void setMeanY1Estimated ( Double meanY1Estimated )
{   __meanY1Estimated = meanY1Estimated;
}

/**
Set the RMS error, in original data units.
@param rmse RMS error, in original data units.
*/
private void setRMSE ( Double rmse )
{	__rmse = rmse;
}

/**
Set the RMS error, in transformed data units.
@param rmseTransformed RMS error, in transformed data units.
*/
/*
public void setRMSETransformed ( Double rmseTransformed )
{   __rmseTransformed = rmseTransformed;
}
*/

/**
Set the standard deviation for the estimated dependent data in the N1 sample.
@param stddevY1Estimated Standard deviation for the dependent data in the N1 sample.
*/
public void setStandardDeviationY1Estimated ( Double stddevY1Estimated )
{   __stddevY1Estimated = stddevY1Estimated;
}

/**
Set the standard error of estimate, in original data units.
@param see the standard error of estimate, in original data units
*/
private void setStandardErrorOfEstimate ( Double see )
{   __see = see;
}

/**
Set the standard error of estimate, in transformed data units.
@param seeTransformed standard error of estimate, in transformed data units
*/
/*
public void setStandardErrorOfEstimateTransformed ( Double seeTransformed )
{   __seeTransformed = seeTransformed;
}
*/

/**
Set the standard error of prediction, in original data units.
@param sep the standard error of prediction, in original data units
*/
private void setStandardErrorOfPrediction ( Double sep )
{   __sep = sep;
}

/**
Set the standard error of prediction, in transformed data units.
@param sepTransformed standard error of prediction, in original data units
*/
/*
public void setStandardErrorOfPredictionTransformed ( Double sepTransformed )
{   __sepTransformed = sepTransformed;
}
*/

/**
Set the standard error of the slope, in original data units.
@param seSlope the standard error of the slope, in original data units
*/
public void setStandardErrorOfSlope ( Double seSlope )
{   __seSlope = seSlope;
}

/**
Set the standard error of the slope, in transformed data units.
@param seSlopeTransformed standard error of the slope, in transformed data units
*/
/*
public void setStandardErrorOfSlopeTransformed ( Double seSlopeTransformed )
{   __seSlopeTransformed = seSlopeTransformed;
}
*/

}