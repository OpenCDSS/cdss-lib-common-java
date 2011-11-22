//------------------------------------------------------------------------------
// Regression - math regression class.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 27 May 1998	Catherine E.		Created initial version.
//		Nutting-Lane, RTi
// 04 Nov 1998	Steven A. Malers, RTi	Add javadoc.  Still need to add
//					constructors, etc. that actually do the
//					regression.
// 09 Jan 1999	SAM, RTi		Add number of points.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 28 Oct 2000	SAM, RTi		Add "lag_intervals" and set/get methods
//					for use by code that analyzes lagged
//					time series.  Add code to store and
//					access the minimum and maximum values
//					used in the analysis.  This is useful
//					when setting axis labels in graphs.
// 2002-03-18	SAM, RTi		Clarify documentation to use X and Y
//					rather than Q and S.
// 2002-03-24	SAM, RTi		Change methods around to be compatible
//					with general X versus Y regression.  For
//					example, change *Max1() to *MaxX().  Add
//					data and methods to support more complex
//					derived classes (e.g., add n1, n2).
// 2002-04-03	SAM, RTi		Add transformed RMSE so that both
//					transformed and untransformed error can
//					be reported.
// 2002-04-08	SAM, RTi		Update to have storage for Y mean and
//					standard deviation.
// 2002-05-14	SAM, RTi		Add parameter for forced intercept.
// 2005-04-26	J. Thomas Sapienza, RTi	Added all data members to finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.Math;

import java.security.InvalidParameterException;

/**
<p>
This class provides storage for regression data and results.  The actual regression occurs by calling
MathUtil.regress(), often with control by higher level code such as that in the time series package.
</p>
<p>
The regression determines the values of "a" and "b" in the relationship
Y = a + b * X, where Y is a value to be determined (e.g., missing data in a
dependent time series), a is the Y-intercept, b is the slope, and X is the known
value (e.g., known value in independent time series).  See the documentation for the TSTool FillRegression()
command for descriptions of equations.
</p>
<p>
When analyzing for filling the X values are the independent and the Y values are the dependent.
</p>
<p>
All generally named data and methods in this class refer to the transformed data, if a transform is being used.
In other words, getA() refers to the intercept from the transformed data relationship.  Where appropriate,
some statistics traditionally refer to the untransformed data (such as RMSE).  In these cases, the method like
getRmse() refers to the original data space and getRmseTransformed() refers to the transformed data.
Where no data transform is used, the raw and transformed values will be the same.  Consequently, refer to the
documentation to understand the meaning.
</p>
*/
public class RegressionResults
{
    
// The following are controlling information for the analysis...
    
/**
Confidence interval to test the relationship for the slope.
*/
private Double __confidenceInterval = null;

/**
The transformation that is applied to the data prior to its analysis.
*/
private DataTransformationType __transformation = null;

/**
Forced intercept A in the computation code.
*/
private Double __forcedIntercept = null;
    
// The following are data associated with the analysis (the sample)...

/**
Number of non-missing overlapping points in X and Y
*/
private Integer __n1 = null;

/**
Number of non-missing points in X (independent for filling) that are not in _n1.
*/
private Integer __n2 = null;

// The following are results of the analysis

/**
Y-intercept of best fit line.
*/
private Double __a = null;

/**
Slope of best fit line.
*/
private Double __b = null;

/**
Indicates whether the analysis has been completed.  For example if the sample size is too small, the
analysis may not have been completed.  The analysis results values will in this case be null.
*/
private boolean __isAnalyzed = false;

/**
Maximum value of X in N1.
*/
private Double __maxX1 = null;

/**
Maximum value of Y in N1.
*/
private Double __maxY1 = null;

/**
Minimum value of X in N1.
*/
private Double __minX1 = null;

/**
Minimum value of Y in N1.
*/
private Double __minY1 = null;

/**
Mean of X in N1 + N2.
*/
private Double __meanX = null;

/**
Mean of X in N1.
*/
private Double __meanX1 = null;

/**
Mean of X in N2.
*/
private Double __meanX2 = null;

/**
Mean of Y in N1 + N2.
*/
private Double __meanY = null;

/**
Mean of Y in N1.
*/
private Double __meanY1 = null;

/**
Mean of estimated Y in N1.
*/
private Double __meanY1Estimated = null;

/**
Correlation coefficient.
*/
private Double __r = null;

/**
RMS Error for untransformed data.
*/
private Double __rmse = null;

/**
RMS Error for transformed data.
*/
private Double __rmseTransformed = null;

/**
Standard error of estimate for untransformed data.
*/
private Double __see = null;

/**
Standard error of estimate for transformed data.
*/
private Double __seeTransformed = null;

/**
Standard error of prediction (SEP) for untransformed data.
*/
private Double __sep = null;

/**
Standard error of prediction (SEP), for transformed values.
*/
private Double __sepTransformed = null;

/**
Standard error of slope, for untransformed values.
*/
private Double __seSlope = null;

/**
Standard error (SE) of slope, for transformed values.
*/
private Double __seSlopeTransformed = null;

/**
Standard deviation of X in N1 + N2.
*/
private Double __stddevX = null;

/**
Standard deviation of X in N1.
*/
private Double __stddevX1 = null;

/**
Standard deviation of X in N2.
*/
private Double __stddevX2 = null;

/**
Standard deviation of X in N1 + N2.
*/
private Double __stddevY = null;

/**
Standard deviation of Y in N1.
*/
private Double __stddevY1 = null;

/**
Standard deviation of estimated Y in N1.
*/
private Double __stddevY1Estimated = null;

/**
Whether the T test shows that the samples are related.
*/
private Boolean __testResultsShowRelated = null;

// TODO SAM 2010-12-20 Evaluate if this is needed - need to document clearly whether transformed.
/**
Array of X1 values.
*/
//private Double [] __X1;

/**
Array of Y1 values.
*/
//private Double [] __Y1;

/**
Default constructor.  Typically the values in this class are set by the MathUtil.regress*() or similar methods.
The default values are all null, indicating that an analysis was not completed.
@param transformation the transformation that is applied to data before analysis
@param forcedIntercept the forced intercept (must be zero), or null to not force the intercept - can only
be specified when no transform
*/
public RegressionResults ( DataTransformationType transformation, Double forcedIntercept )
{
    if ( transformation == null ) {
        throw new InvalidParameterException ( "The data transformation must be specified as non-null." );
    }
    if ( forcedIntercept != null ) {
        if ( transformation != DataTransformationType.NONE ) {
            throw new InvalidParameterException ( "The forced intercept can only be specified with no transformation." );
        }
        if ( forcedIntercept != 0.0 ) {
            throw new InvalidParameterException ( "The forced intercept if specified can only be set to 0.0 (" +
                forcedIntercept + " was provided)." );
        }
    }
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
Return A in correlation equation, or null if not calculated.
@return A in correlation equation, or null if not calculated.
*/
public Double getA ()
{	return __a;
}

/**
Return B in correlation equation, or null if not calculated.
@return B in correlation equation, or null if not calculated.
*/
public Double getB ()
{	return __b;
}

/**
Return the coefficient of determination (r*r) for the relationship (0 to 1), or null if not calculated.
@return The coefficient of determination (r*r) for the the relationship (-1 to 1), or null if not calculated.
*/
public Double getCoefficientOfDetermination ()
{   Double r = getCorrelationCoefficient();
    if ( r == null ) {
        return null;
    }
    else {
        return new Double(r*r);
    }
}

/**
Return the correlation coefficient (r) for the relationship (-1 to 1), or null if not calculated.
@return The correlation coefficient for (r) the relationship (-1 to 1), or null if not calculated.
*/
public Double getCorrelationCoefficient ()
{	return __r;
}

/**
Get the forced intercept (A) for the relationship, or null if not forced.
@return the forced intercept or null if not forced.
*/
public Double getForcedIntercept ()
{   return __forcedIntercept;
}

/**
Indicate whether the analysis has been completed.
@return true if the analysis has been completed.
*/
public boolean getIsAnalyzed ()
{   return __isAnalyzed;
}

/**
Return the maximum data value for the independent array in the N1 sample, or null if not analyzed.
@return the maximum data value for the independent array in the N1 sample, or null if not analyzed.
*/
public Double getMaxX1 ()
{	return __maxX1;
}

/**
Return the maximum data value for the dependent array in the N1 sample, or null if not analyzed.
@return the maximum data value for the dependent array in the N1 sample, or null if not analyzed.
*/
public Double getMaxY1 ()
{	return __maxY1;
}

/**
Return the mean for the independent array in the N1 + N2 sample, or null if not analyzed.
@return the mean for the independent array in the N1 + N2 sample, or null if not analyzed.
*/
public Double getMeanX ()
{	return __meanX;
}

/**
Return the mean for the independent array in the N1 sample, or null if not analyzed.
@return the mean for the independent array in the N1 sample, or null if not analyzed.
*/
public Double getMeanX1 ()
{	return __meanX1;
}

/**
Return the mean for the independent array in the N2 sample, or null if not analyzed.
@return the mean for the independent array in the N2 sample, or null if not analyzed.
*/
public Double getMeanX2 ()
{	return __meanX2;
}

/**
Return the mean for the dependent array in the N1 + N2 sample, or null if not analyzed.
@return the mean for the dependent array in the N1 + N2 sample, or null if not analyzed.
*/
public Double getMeanY ()
{	return __meanY;
}

/**
Return the mean for the dependent array in the N1 sample, or null if not analyzed.
@return the mean for the dependent array in the N1 sample, or null if not analyzed.
*/
public Double getMeanY1 ()
{	return __meanY1;
}

/**
Return the mean for the estimated dependent array in the N1 sample, or null if not analyzed.
@return the mean for the estimated dependent array in the N1 sample, or null if not analyzed.
*/
public Double getMeanY1Estimated ()
{	return __meanY1Estimated;
}

/**
Return the minimum data value for the independent array in the N1 sample, or null if not analyzed.
@return the minimum data value for the independent array in the N1 sample, or null if not analyzed.
*/
public Double getMinX1 ()
{	return __minX1;
}

/**
Return the minimum data value for the dependent array in the N1 sample, or null if not analyzed.
@return the minimum data value for the dependent array in the N1 sample, or null if not analyzed.
*/
public Double getMinY1 ()
{	return __minY1;
}

/**
Return the number of non-missing data points in X and Y, or null if not analyzed.
@return the number of non-missing data points in X and Y, or null if not analyzed.
*/
public int getN1 ()
{	return __n1;
}

/**
Return the number of non-missing data points in X that are not in N1, or null if not analyzed.
@return the number of non-missing data points in X that are not in N1, or null if not analyzed.
*/
public int getN2 ()
{	return __n2;
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
public Double getRMSETransformed ()
{   return __rmseTransformed;
}

/**
Return the standard deviation for the independent array in the N1 + N2 sample, or null if not analyzed.
@return the standard deviation for the independent array in the N1 + N2 sample, or null if not analyzed.
*/
public Double getStandardDeviationX ()
{	return __stddevX;
}

/**
Return the standard deviation for the independent array in the N1 sample, or null if not analyzed.
@return the standard deviation for the independent array in the N1 sample, or null if not analyzed.
*/
public Double getStandardDeviationX1 ()
{	return __stddevX1;
}

/**
Return the standard deviation for the independent array in the N2 sample, or null if not analyzed.
@return the standard deviation for the independent array in the N2 sample, or null if not analyzed.
*/
public Double getStandardDeviationX2 ()
{	return __stddevX2;
}

/**
Return the standard deviation for the dependent array in the N1 + N2 sample, or null if not analyzed.
@return the standard deviation for the dependent array in the N1 + N2 sample, or null if not analyzed.
*/
public Double getStandardDeviationY ()
{	return __stddevY;
}

/**
Return the standard deviation for the dependent array in the N1 sample, or null if not analyzed.
@return the standard deviation for the dependent array in the N1 sample, or null if not analyzed.
*/
public Double getStandardDeviationY1 ()
{	return __stddevY1;
}

/**
Return the standard deviation for the estimated dependent array in the N1 sample, or null if not analyzed.
@return the standard deviation for the estimated dependent array in the N1 sample, or null if not analyzed.
*/
public Double getStandardDeviationY1Estimated ()
{	return __stddevY1Estimated;
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
public Double getStandardErrorOfEstimateTransformed ()
{   return __seeTransformed;
}

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
public Double getStandardErrorOfPredictionTransformed ()
{   return __sepTransformed;
}

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
public Double getStandardErrorOfSlopeTransformed ()
{   return __seSlopeTransformed;
}

/**
Return whether the samples are related, or null if not analyzed.
@return whether the samples are related, or null if not analyzed.
*/
public Boolean getTestResultsShowRelated ()
{   return __testResultsShowRelated;
}

/**
Return the X1 array.
@return the X1 array.
*/
//public Double [] getX1 ()
//{	return __X1;
//}

/**
Return the Y1 array.
@return the Y1 array.
*/
//public Double [] getY1 ()
//{	return __Y1;
//}

/**
Set the A value (Y intercept).
@param a value to save as A.
*/
public void setA (Double a)
{	__a = a;
}

/**
Set the B value (slope).
@param b Value to save as B.
*/
public void setB (Double b)
{	__b = b;
}

/**
Set the correlation coefficient.
@param r Correlation coefficient.
*/
public void setCorrelationCoefficient ( Double r )
{	__r = r;
}

/**
Set whether the analysis has been completed.
@param isAnalyzed true if the analysis is completed, false if not.
@return the analysis flag after setting.
*/
public boolean setIsAnalyzed ( boolean isAnalyzed )
{   __isAnalyzed = isAnalyzed;
    return __isAnalyzed;
}

/**
Set the maximum data value for the independent data in the N1 sample.
@param maxX1 Maximum data value for the independent data in the N1 sample.
*/
public void setMaxX1 ( Double maxX1 )
{	__maxX1 = maxX1;
}

/**
Set the maximum data value for the dependent data in the N1 sample.
@param maxY1 Maximum data value for the dependent data in the N1 sample.
*/
public void setMaxY1 ( Double maxY1 )
{	__maxY1 = maxY1;
}

/**
Set the mean for the independent data in the N1 + N2 sample.
@param meanX Mean for the independent data in the N1 + N2 sample.
*/
public void setMeanX ( Double meanX )
{	__meanX = meanX;
}

/**
Set the mean for the independent data in the N1 sample.
@param meanX1 Mean for the independent data in the N1 sample.
*/
public void setMeanX1 ( Double meanX1 )
{	__meanX1 = meanX1;
}

/**
Set the mean for the independent data in the N2 sample.
@param meanX2 Mean for the independent data in the N2 sample.
*/
public void setMeanX2 ( Double meanX2 )
{	__meanX2 = meanX2;
}

/**
Set the mean for the dependent data in the N1 + N2 sample.
@param meanY Mean for the dependent data in the N1 + N2 sample.
*/
public void setMeanY ( Double meanY )
{	__meanY = meanY;
}

/**
Set the mean for the dependent data in the N1 sample.
@param meanY1 Mean for the dependent data in the N1 sample.
*/
public void setMeanY1 ( Double meanY1 )
{	__meanY1 = meanY1;
}

/**
Set the mean for the estimated dependent data in the N1 sample.
@param meanY1Estimated Mean for the estimated dependent data in the N1 sample.
*/
public void setMeanY1Estimated ( Double meanY1Estimated )
{	__meanY1Estimated = meanY1Estimated;
}

/**
Set the minimum data value for the independent data in the N1 sample.
@param minX1 Minimum data value for the independent data in the N1 sample.
*/
public void setMinX1 ( Double minX1 )
{	__minX1 = minX1;
}

/**
Set the minimum data value for the dependent data in the N1 sample.
@param minY1 Minimum data value for the dependent data in the N1 sample.
*/
public void setMinY1 ( Double minY1 )
{	__minY1 = minY1;
}

/**
Set the number of non-missing points in X and Y.
@param n1 Number of non-missing points in X and Y.
*/
public void setN1 ( int n1 )
{	__n1 = n1;
}

/**
Set the number of non-missing points in X that are not in the N1 sample.
@param n2 Number of non-missing points in X that are not in the N1 sample.
*/
public void setN2 ( int n2 )
{	__n2 = n2;
}

/**
Set the RMS error, in original data units.
@param rmse RMS error, in original data units.
*/
public void setRMSE ( Double rmse )
{	__rmse = rmse;
}

/**
Set the RMS error, in transformed data units.
@param rmseTransformed RMS error, in transformed data units.
*/
public void setRMSETransformed ( Double rmseTransformed )
{   __rmseTransformed = rmseTransformed;
}

/**
Set the standard deviation for the independent data in the N1 + N2 sample.
@param stddevX Standard deviation for the independent data in the N1 + N2 sample.
*/
public void setStandardDeviationX ( Double stddevX )
{	__stddevX = stddevX;
}

/**
Set the standard deviation for the independent data in the N1 sample.
@param stddevX1 Standard deviation for the independent data in the N1 sample.
*/
public void setStandardDeviationX1 ( Double stddevX1 )
{	__stddevX1 = stddevX1;
}

/**
Set the standard deviation for the independent data in the N2 sample.
@param stddevX2 Standard deviation for the independent data in the N2 sample.
*/
public void setStandardDeviationX2 ( Double stddevX2 )
{	__stddevX2 = stddevX2;
}

/**
Set the standard deviation for the dependent data in the N1 + N2 sample.
@param stddevY Standard deviation for the dependent data in the N1 + N2 sample.
*/
public void setStandardDeviationY ( Double stddevY )
{	__stddevY = stddevY;
}

/**
Set the standard deviation for the dependent data in the N1 sample.
@param stddevY1 Standard deviation for the dependent data in the N1 sample.
*/
public void setStandardDeviationY1 ( Double stddevY1 )
{	__stddevY1 = stddevY1;
}

/**
Set the standard deviation for the estimated dependent data in the N1 sample.
@param stddevY1Estimated Standard deviation for the dependent data in the N1 sample.
*/
public void setStandardDeviationY1Estimated ( Double stddevY1Estimated )
{	__stddevY1Estimated = stddevY1Estimated;
}

/**
Set the standard error of estimate, in original data units.
@param see the standard error of estimate, in original data units
*/
public void setStandardErrorOfEstimate ( Double see )
{   __see = see;
}

/**
Set the standard error of estimate, in transformed data units.
@param seeTransformed standard error of estimate, in transformed data units
*/
public void setStandardErrorOfEstimateTransformed ( Double seeTransformed )
{   __seeTransformed = seeTransformed;
}

/**
Set the standard error of prediction, in original data units.
@param sep the standard error of prediction, in original data units
*/
public void setStandardErrorOfPrediction ( Double sep )
{   __sep = sep;
}

/**
Set the standard error of prediction, in transformed data units.
@param sepTransformed standard error of prediction, in original data units
*/
public void setStandardErrorOfPredictionTransformed ( Double sepTransformed )
{   __sepTransformed = sepTransformed;
}

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
public void setStandardErrorOfSlopeTransformed ( Double seSlopeTransformed )
{   __seSlopeTransformed = seSlopeTransformed;
}

/**
Set the test results indicating whether the samples are related.
@param testResultsShowRelated the test results indicating whether the samples are related
*/
public void setTestResultsShowRelation ( Boolean testResultsShowRelated )
{   __testResultsShowRelated = testResultsShowRelated;
}

/**
Set the X1 array.
@param x1 Data for X in N1.
*/
///public void setX1 ( Double [] x1 )
//{	__X1 = x1;
//}

/**
Set the Y1 array.
@param y1 Data for Y in N1.
*/
//public void setY1 ( Double [] y1 )
//{	__Y1 = y1;
//}

/**
Return a string representation of the data.
@return a string representation of the object, which is a verbose listing
of A, B, etc.  Typically this method needs to be overloaded in a more specific class.
*/
/*
public String toString ()
{	if ( __isAnalyzed ) {
		return "Intercept A = " + __a + ", " +
		"Slope B = " + __b + ", " +
		"N1 = " + __n1 + ", " +
		"N2 = " + __n2 + ", " +
		"RMSE = " + __rmse + ", " +
		"R = " + __r;
	}
	else {
	    return "Analysis not performed";
	}
}
*/

}