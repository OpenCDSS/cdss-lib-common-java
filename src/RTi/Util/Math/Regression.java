// Regression - linear regression

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.Util.Math;

/**
This class provides storage for regression data and results.  This class can be
extended for more complicated analysis.  At this time, regression is not
implemented (only storage is).  This class was implemented as a base class for
the RTi.TS.TSRegression class.  The actual regression occurs by calling
MathUtil.regress() (or TSUtil regression methods).  This generic code is
typically then called to fill in the information in this class and possibly a derived class.
<p>

The regression determines the values of "a" and "b" in the relationship
Y = a + b * X, where Y is a value to be determined (e.g., missing data in a
dependent time series), a is the Y-intercept, b is the slope, and X is the known
value (e.g., known value in independent time series).  The correlation
coefficient (r) and RMS error (RMSE) are assumed to be computed in the normal fashion.
*/
public class Regression
{

/**
Forced intercept A in the computation code.
*/
protected Double _intercept = null;

/**
Y-intercept of best fit line.
*/
protected double _a = 0.0;

/**
Slope of best fit line.
*/
protected double _b = 0.0;

/**
Number of non-missing points in X and Y
*/
protected int _n1 = 0;

/**
Number of non-missing points in X that are not in _n1.
*/
protected int _n2 = 0;

/**
Standard error of estimate for untransformed data.
*/
protected double _see = 0.0;

/**
Standard error of estimate for transformed data.
*/
protected double _seeTransformed = 0.0;

/**
Correlation coefficient.
*/
protected double _r = 0.0;

/**
RMS Error for untransformed data.
*/
protected double _rmse = 0.0;

/**
RMS Error for transformed data.
*/
protected double _rmseTransformed = 0.0;

/**
Indicates whether the analysis has been completed.
*/
protected boolean _is_analyzed = false;

/**
Intervals that the second time series is lagged compared to the first when performing the regression
analysis.
TODO SAM 2009-03-10 Need to be double?
*/
protected int _lag_intervals = 0;

/**
Maximum value of X in N1.
*/
protected double _max_x1 = 0.0;

/**
Maximum value of Y in N1.
*/
protected double _max_y1 = 0.0;

/**
Minimum value of X in N1.
*/
protected double _min_x1 = 0.0;

/**
Minimum value of Y in N1.
*/
protected double _min_y1 = 0.0;

/**
Mean of X in N1 + N2.
*/
protected double _mean_x = 0.0;

/**
Mean of X in N1.
*/
protected double _mean_x1 = 0.0;

/**
Mean of X in N2.
*/
protected double _mean_x2 = 0.0;

/**
Mean of Y in N1 + N2.
*/
protected double _mean_y = 0.0;

/**
Mean of Y in N1.
*/
protected double _mean_y1 = 0.0;

/**
Mean of estimated Y in N1.
*/
protected double _mean_y1_estimated = 0.0;

/**
Standard deviation of X in N1 + N2.
*/
protected double _stddev_x = 0.0;

/**
Standard deviation of X in N1.
*/
protected double _stddev_x1 = 0.0;

/**
Standard deviation of X in N2.
*/
protected double _stddev_x2 = 0.0;

/**
Standard deviation of X in N1 + N2.
*/
protected double _stddev_y = 0.0;

/**
Standard deviation of Y in N1.
*/
protected double _stddev_y1 = 0.0;

/**
Standard deviation of estimated Y in N1.
*/
protected double _stddev_y1_estimated = 0.0;

/**
Array of X1 values.
*/
protected double [] _X1;

/**
Array of Y1 values.
*/
protected double [] _Y1;

/**
Default constructor.  Typically the data in this class are set by the MathUtil.regress*() or similar methods.
*/
public Regression ()
{
}

/**
Indicate whether the intercept (A) for the relationship is forced.
@return the forced intercept or null if not forced.
*/
public Double getIntercept ()
{	return _intercept;
}

/**
Return A (intercept) in correlation equation.
@return A (intercept) in correlation equation.
*/
public double getA ()
{	return _a;
}

/**
Return B (slope) in correlation equation.
@return B (slope) in correlation equation.
*/
public double getB ()
{	return _b;
}

/**
Return the correlation coefficient (r) for the relationship (-1 to 1).
@return The correlation coefficient for (r) the relationship (-1 to 1).
*/
public double getCorrelationCoefficient ()
{	return _r;
}

/**
Return The number of data intervals that the second series has been lagged
compared to the first.  This is used by higher-level code when performing an
analysis.  This is a new feature that is being tested.
@return The number of data intervals that the second series has been lagged compared to the first.
*/
public int getLagIntervals()
{	return _lag_intervals;
}

/**
Return the maximum data value for the independent array in the N1 space.
@return the maximum data value for the independent array in the N1 space.
*/
public double getMaxX1 ()
{	return _max_x1;
}

/**
Return the maximum data value for the dependent array in the N1 space.
@return the maximum data value for the dependent array in the N1 space.
*/
public double getMaxY1 ()
{	return _max_y1;
}

/**
Return the mean for the independent array in the N1 + N2 space.
@return the mean for the independent array in the N1 + N2 space.
*/
public double getMeanX ()
{	return _mean_x;
}

/**
Return the mean for the independent array in the N1 space.
@return the mean for the independent array in the N1 space.
*/
public double getMeanX1 ()
{	return _mean_x1;
}

/**
Return the mean for the independent array in the N2 space.
@return the mean for the independent array in the N2 space.
*/
public double getMeanX2 ()
{	return _mean_x2;
}

/**
Return the mean for the dependent array in the N1 + N2 space.
@return the mean for the dependent array in the N1 + N2 space.
*/
public double getMeanY ()
{	return _mean_y;
}

/**
Return the mean for the dependent array in the N1 space.
@return the mean for the dependent array in the N1 space.
*/
public double getMeanY1 ()
{	return _mean_y1;
}

/**
Return the mean for the estimated dependent array in the N1 space.
@return the mean for the estimated dependent array in the N1 space.
*/
public double getMeanY1Estimated ()
{	return _mean_y1_estimated;
}

/**
Return the minimum data value for the independent array in the N1 space.
@return the minimum data value for the independent array in the N1 space.
*/
public double getMinX1 ()
{	return _min_x1;
}

/**
Return the minimum data value for the dependent array in the N1 space.
@return the minimum data value for the dependent array in the N1 space.
*/
public double getMinY1 ()
{	return _min_y1;
}

/**
Return the number of non-missing data points in X and Y.
@return the number of non-missing data points in X and Y.
*/
public int getN1 ()
{	return _n1;
}

/**
Return the number of non-missing data points in X that are not in N1.
@return the number of non-missing data points in X that are not in N1.
*/
public int getN2 ()
{	return _n2;
}

/**
Return the RMS error.
@return The RMS error.
*/
public double getRMSE ()
{	return _rmse;
}

/**
Return the RMS error for transformed data.
@return The RMS error for transformed data.
*/
public double getRMSETransformed ()
{   return _rmseTransformed;
}

/**
Return the standard deviation for the independent array in the N1 + N2 space.
@return the standard deviation for the independent array in the N1 + N2 space.
*/
public double getStandardDeviationX ()
{	return _stddev_x;
}

/**
Return the standard deviation for the independent array in the N1 space.
@return the standard deviation for the independent array in the N1 space.
*/
public double getStandardDeviationX1 ()
{	return _stddev_x1;
}

/**
Return the standard deviation for the independent array in the N2 space.
@return the standard deviation for the independent array in the N2 space.
*/
public double getStandardDeviationX2 ()
{	return _stddev_x2;
}

/**
Return the standard deviation for the dependent array in the N1 + N2 space.
@return the standard deviation for the dependent array in the N1 + N2 space.
*/
public double getStandardDeviationY ()
{	return _stddev_y;
}

/**
Return the standard deviation for the dependent array in the N1 space.
@return the standard deviation for the dependent array in the N1 space.
*/
public double getStandardDeviationY1 ()
{	return _stddev_y1;
}

/**
Return the standard deviation for the estimated dependent array in the N1 space.
@return the standard deviation for the estimated dependent array in the N1 space.
*/
public double getStandardDeviationY1Estimated ()
{	return _stddev_y1_estimated;
}

/**
Return the standard error of estimate.
@return the standard error of estimate
*/
public double getStandardErrorOfEstimate ()
{   return _see;
}

/**
Return the standard error of estimate for transformed data.
@return the standard error of estimate for transformed data.
*/
public double getStandardErrorOfEstimateTransformed ()
{   return _seeTransformed;
}

/**
Return the X1 array.
@return the X1 array.
*/
public double [] getX1 ()
{	return _X1;
}

/**
Return the Y1 array.
@return the Y1 array.
*/
public double [] getY1 ()
{	return _Y1;
}

/**
Indicate whether the analysis has been completed.
@return true if the analysis has been completed.
*/
public boolean isAnalyzed ()
{	return _is_analyzed;
}

/**
Set whether the analysis has been completed.
@param is_analyzed true if the analysis is completed, false if not.
@return the analysis flag after setting.
*/
public boolean isAnalyzed ( boolean is_analyzed )
{	_is_analyzed = is_analyzed;
	return _is_analyzed;
}

/**
Set the A value (Y intercept).
@param a value to save as A.
*/
public void setA (double a)
{	_a = a;
}

/**
Set the B value (slope).
@param b Value to save as B.
*/
public void setB (double b)
{	_b = b;
}

/**
Set the correlation coefficient.
@param r Correlation coefficient.
*/
public void setCorrelationCoefficient ( double r )
{	_r = r;
}

/**
Set the intercept (A) for the relationship is forced.
@param intercept Set the intercept value of A that has been forced.  The
calculation of B should therefore use different equations (MathUtil.regress handles the option).
*/
public void setIntercept ( Double intercept )
{   _intercept = intercept;
}

/**
Set the number of data intervals that the second series has been lagged compared
to the first.  This is used by higher-level code when performing an analysis.
@param lag_intervals Number of intervals the second data set has been lagged compared to the first.
*/
public void setLagIntervals ( int lag_intervals )
{	_lag_intervals = lag_intervals;
}

/**
Set the maximum data value for the independent data in the N1 space.
@param max_x1 Maximum data value for the independent data in the N1 space.
*/
public void setMaxX1 ( double max_x1 )
{	_max_x1 = max_x1;
}

/**
Set the maximum data value for the dependent data in the N1 space.
@param max_y1 Maximum data value for the dependent data in the N1 space.
*/
public void setMaxY1 ( double max_y1 )
{	_max_y1 = max_y1;
}

/**
Set the mean for the independent data in the N1 + N2 space.
@param mean_x Mean for the independent data in the N1 + N2 space.
*/
public void setMeanX ( double mean_x )
{	_mean_x = mean_x;
}

/**
Set the mean for the independent data in the N1 space.
@param mean_x1 Mean for the independent data in the N1 space.
*/
public void setMeanX1 ( double mean_x1 )
{	_mean_x1 = mean_x1;
}

/**
Set the mean for the independent data in the N2 space.
@param mean_x2 Mean for the independent data in the N2 space.
*/
public void setMeanX2 ( double mean_x2 )
{	_mean_x2 = mean_x2;
}

/**
Set the mean for the dependent data in the N1 + N2 space.
@param mean_y Mean for the dependent data in the N1 + N2 space.
*/
public void setMeanY ( double mean_y )
{	_mean_y = mean_y;
}

/**
Set the mean for the dependent data in the N1 space.
@param mean_y1 Mean for the dependent data in the N1 space.
*/
public void setMeanY1 ( double mean_y1 )
{	_mean_y1 = mean_y1;
}

/**
Set the mean for the estimated dependent data in the N1 space.
@param mean_y1_estimated Mean for the estimated dependent data in the N1 space.
*/
public void setMeanY1Estimated ( double mean_y1_estimated )
{	_mean_y1_estimated = mean_y1_estimated;
}

/**
Set the minimum data value for the independent data in the N1 space.
@param min_x1 Minimum data value for the independent data in the N1 space.
*/
public void setMinX1 ( double min_x1 )
{	_min_x1 = min_x1;
}

/**
Set the minimum data value for the dependent data in the N1 space.
@param min_y1 Minimum data value for the dependent data in the N1 space.
*/
public void setMinY1 ( double min_y1 )
{	_min_y1 = min_y1;
}

/**
Set the number of non-missing points in X and Y.
@param n1 Number of non-missing points in X and Y.
*/
public void setN1 ( int n1 )
{	_n1 = n1;
}

/**
Set the number of non-missing points in X that are not in the N1 space.
@param n2 Number of non-missing points in X that are not in the N1 space.
*/
public void setN2 ( int n2 )
{	_n2 = n2;
}

/**
Set the RMS error.
@param rmse RMS error.
*/
public void setRMSE ( double rmse )
{	_rmse = rmse;
}

/**
Set the transformed RMS error.
@param rmseTransformed transformed RMS error.
*/
public void setRMSETransformed ( double rmseTransformed )
{   _rmseTransformed = rmseTransformed;
}

/**
Set the standard error of estimate.
@param see the standard error of estimate
*/
public void setStandardErrorOfEstimate ( double see )
{   _see = see;
}

/**
Set the standard error of estimate.
@param seeTransformed transformed standard error of estimate
*/
public void setStandardErrorOfEstimateTransformed ( double seeTransformed )
{   _seeTransformed = seeTransformed;
}

/**
Set the standard deviation for the independent data in the N1 + N2 space.
@param stddev_x Standard deviation for the independent data in the N1 + N2 space.
*/
public void setStandardDeviationX ( double stddev_x )
{	_stddev_x = stddev_x;
}

/**
Set the standard deviation for the independent data in the N1 space.
@param stddev_x1 Standard deviation for the independent data in the N1 space.
*/
public void setStandardDeviationX1 ( double stddev_x1 )
{	_stddev_x1 = stddev_x1;
}

/**
Set the standard deviation for the independent data in the N2 space.
@param stddev_x2 Standard deviation for the independent data in the N2 space.
*/
public void setStandardDeviationX2 ( double stddev_x2 )
{	_stddev_x2 = stddev_x2;
}

/**
Set the standard deviation for the dependent data in the N1 + N2 space.
@param stddev_y Standard deviation for the dependent data in the N1 + N2 space.
*/
public void setStandardDeviationY ( double stddev_y )
{	_stddev_y = stddev_y;
}

/**
Set the standard deviation for the dependent data in the N1 space.
@param stddev_y1 Standard deviation for the dependent data in the N1 space.
*/
public void setStandardDeviationY1 ( double stddev_y1 )
{	_stddev_y1 = stddev_y1;
}

/**
Set the standard deviation for the estimated dependent data in the N1 space.
@param stddev_y1_estimated Standard deviation for the dependent data in the N1 space.
*/
public void setStandardDeviationY1Estimated ( double stddev_y1_estimated )
{	_stddev_y1_estimated = stddev_y1_estimated;
}

/**
Set the X1 array.
@param x1 Data for X in N1.
*/
public void setX1 ( double [] x1 )
{	_X1 = x1;
}

/**
Set the Y1 array.
@param y1 Data for Y in N1.
*/
public void setY1 ( double [] y1 )
{	_Y1 = y1;
}

/**
Return a string representation of the data.
@return a string representation of the object, which is a verbose listing
of A, B, etc.  Typically this method needs to be overloaded in a more specific class.
*/
public String toString ()
{	if ( _is_analyzed ) {
		return "Intercept A = " + _a + ", " +
		"Slope B = " + _b + ", " +
		"N1 = " + _n1 + ", " +
		"N2 = " + _n2 + ", " +
		"RMSE = " + _rmse + ", " +
		"R = " + _r;
	}
	else {
	    return "Analysis not performed";
	}
}

}
