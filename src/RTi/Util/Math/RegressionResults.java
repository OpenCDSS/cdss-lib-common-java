// RegressionResults - this class provides storage for regression results

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
<p>
This class provides storage for regression results, including intermediate statistics and final
regression equation coefficients.  Data for the analysis are stored in RegressionData objects, error
estimates from the equations are stored in RegressionErrors, and checks to evaluate whether the equations
are valid are stored in RegressionChecks.
</p>
<p>
The regression relationships are calculated by calling MathUtil.regress() and other MathUtil methods,
often with control by higher level code such as that in the time series package.
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
This class does not know whether data have been transformed or not - that is controlled by higher level code.
</p>
*/
public class RegressionResults
{
    
/**
Regression data used to perform the analysis.
*/
private RegressionData __data = null;
    
/**
Forced intercept A in the computation code.
*/
private Double __forcedIntercept = null;
    
/**
Y-intercept of best fit line.
*/
private Double __a = null;

/**
Slope of best fit line.
*/
private Double __b = null;

/**
Correlation coefficient.
*/
private Double __r = null;

/**
Indicates whether the analysis was performed OK.
*/
private boolean __isAnalysisPerformedOK;

// TODO SAM 2012-01-17 Evaluate whether data is required - may lead to memory bloat (allow null?)
/**
Constructor.  Typically the values in this class are set by the MathUtil.regress*() or similar methods.
The default values are all null, indicating that an analysis was not completed.
@param transformation the transformation that is applied to data before analysis
@param forcedIntercept the forced intercept (must be zero), or null to not force the intercept - can only
be specified when no transform
*/
public RegressionResults ( RegressionData data, Double forcedIntercept, Double a, Double b, Double r )
{
    if ( forcedIntercept != null ) {
        // TODO SAM 2012-01-15 Evaluate if this is important here, given that transformation already will have
        // happened and transform checked against intercept
        //if ( transformation != DataTransformationType.NONE ) {
        //    throw new InvalidParameterException ( "The forced intercept can only be specified with no transformation." );
        //}
        if ( forcedIntercept != 0.0 ) {
            throw new IllegalArgumentException ( "The forced intercept if specified can only be set to 0.0 (" +
                forcedIntercept + " was provided)." );
        }
    }
    __data = data;
    __forcedIntercept = forcedIntercept;
    setA ( a );
    setB ( b );
    setCorrelationCoefficient( r );
    // If any of the values are not valid, set the boolean indicating that the results are not OK.
    __isAnalysisPerformedOK = true;
    if ( (a == null) || a.isNaN() || (b == null) || b.isNaN() || (r == null) || r.isNaN() ) {
        __isAnalysisPerformedOK = false;
    }
}

/**
Return data for regression equation
Used so that we can clear it after it is no longer needed.
@return data for regression equation
*/
public RegressionData get__data() {
	return __data;
}

/**
Return A (intercept) in regression equation, or null if not calculated.
@return A (intercept) in regression equation, or null if not calculated.
*/
public Double getA ()
{	return __a;
}

/**
Return B (slope) in regression equation, or null if not calculated.
@return B (slope) in regression equation, or null if not calculated.
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
    else if ( r.isNaN() ) {
        return Double.NaN;
    }
    else {
        return Double.valueOf(r*r);
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
Indicate whether the analysis was performed OK.
@return true if the analysis was performed OK, false if not.
*/
public boolean getIsAnalysisPerformedOK ()
{   return __isAnalysisPerformedOK;
}

/**
Set the A value (Y intercept).
@param a value to save as A.
*/
private void setA (Double a)
{	__a = a;
}

/**
Set the B value (slope).
@param b Value to save as B.
*/
private void setB (Double b)
{	__b = b;
}

/**
Set the correlation coefficient.
@param r Correlation coefficient.
*/
private void setCorrelationCoefficient ( Double r )
{	__r = r;
}

/**
Return a string representation of the data.
@return a string representation of the object, which is a verbose listing
of A, B, etc.  Typically this method needs to be overloaded in a more specific class.
*/
public String toString ()
{
		return "Intercept A = " + __a + ", " +
		"Slope B = " + __b + ", " +
		"R = " + __r;
}

}
