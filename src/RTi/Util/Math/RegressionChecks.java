package RTi.Util.Math;

/**
This class provides storage for check on the regression data and results, including ensuring that the
sample size, correlation coefficient, and T-test (confidence interval) are OK.
*/
public class RegressionChecks
{
    
/**
Minimum sample size required to demonstrate relationship.
*/
Integer __minimumSampleSize = null;
    
/**
Is sample size OK?
*/
boolean __isSampleSizeOK = false;

/**
Minimum R required to demonstrate relationship.
*/
Double __minimumR = null;

/**
Is R OK?
*/
boolean __isROK = false;

/**
Confidence interval required to demonstrate relationship.
*/
Double __confidenceInterval = null;

/**
Is the confidence interval OK?
*/
boolean __isConfidenceIntervalOK = false;

/**
Constructor.  The values to check are provided for transparency in case values need to be retrieved
for reporting, etc.
@param transformation the transformation that is applied to data before analysis
@param forcedIntercept the forced intercept (must be zero), or null to not force the intercept - can only
be specified when no transform
*/
public RegressionChecks ( Integer minimumSampleSize, boolean isSampleSizeOK,
    Double minimumR, boolean isMinimumROK,
    Double confidenceInterval, boolean isConfidenceIntervalOK )
{
    __minimumSampleSize = minimumSampleSize;
    __isSampleSizeOK = isSampleSizeOK;
    __minimumR = minimumR;
    __confidenceInterval = confidenceInterval;
    __isConfidenceIntervalOK = isConfidenceIntervalOK;
}

/**
Indicate whether the confidence interval has been met.
@return true if the confidence interval has been met.
*/
public boolean getIsConfidenceIntervalOK ()
{   return __isConfidenceIntervalOK;
}

/**
Indicate whether R has met the minimum criteria.
@return true if R has met the minimum criteria.
*/
public boolean getIsROK ()
{   return __isROK;
}

/**
Indicate whether the sample size has met the minimum criteria.
@return true if the sample size has met the minimum criteria.
*/
public boolean getIsSampleSizeOK ()
{   return __isSampleSizeOK;
}

/**
Return the minimum acceptable R - if null then R is not checked.
@return the minimum acceptable R.
*/
public Double getMinimumR ()
{   return __minimumR;
}

/**
Return the minimum acceptable sample size - if null then sample size is not checked.
@return the minimum acceptable sample size.
*/
public Integer getMinimumSampleSize ()
{   return __minimumSampleSize;
}

/**
Return the confidence interval that must be met - if null then confidence interval is not checked.
@return the confidence interval that must be met.
*/
public Double getConfidenceInterval ()
{   return __confidenceInterval;
}

}