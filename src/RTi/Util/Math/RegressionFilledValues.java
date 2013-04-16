package RTi.Util.Math;

/**
Filled (previously missing) values resulting from a regression analysis.
The information is useful to compare the statistics of filled values compared to the non-filled values.
The data are simple arrays; however, when combined to handle monthly
regression or other complex analysis, the object help manage data.
*/
public class RegressionFilledValues
{

// Data for filled Y values...

/**
The dependent data array, for points that have been filled/estimated using the regression equation.
*/
private double [] __yFilled = new double[0];

/**
Minimum filled Y values.
*/
private Double __minYfilled = null;

/**
Maximum filled Y values.
*/
private Double __maxYfilled = null;

/**
Mean of filled Y values.
*/
private Double __meanYfilled = null;

/**
Skew of filled Y values.
*/
private Double __skewYfilled = null;

/**
Standard deviation of filled Y values.
*/
private Double __stddevYfilled = null;

/**
Constructor.  Set the dependent filled data array, which should contain values estimated using the regression
relationship (and which were previously missing).
The data array is used to compute basic statistics such as mean and standard deviation,
but only when the get methods are called.  Null arrays will be set to empty arrays.
@param yFilled dependent data array, for values that have been filled (estimated).
*/
public RegressionFilledValues ( double [] yFilled )
{
    if ( yFilled == null ) {
        __yFilled = new double[0];
    }
    else {
        __yFilled = yFilled;
    }
    // Basic statistics will be calculated lazily (if requested)
}

/**
Return the maximum data value for the filled dependent array, or null if not analyzed.
@return the maximum data value for the filled dependent array, or null if not analyzed.
*/
public Double getMaxYFilled ()
{   Double maxYfilled = __maxYfilled;
    if ( (maxYfilled == null) && (getYFilled().length > 0) ) {
        maxYfilled = MathUtil.max(getYFilled());
        setMaxYFilled ( maxYfilled );
    }
    return maxYfilled;
}

/**
Return the mean for the filled dependent array, or null if not analyzed.
@return the mean for the filled dependent array, or null if not analyzed.
*/
public Double getMeanYFilled ()
{   Double meanYFilled = __meanYfilled;
    if ( (meanYFilled == null) && (getYFilled().length > 0) ) {
        meanYFilled = MathUtil.mean(getYFilled());
        setMeanYFilled ( meanYFilled );
    }
    return meanYFilled;
}

/**
Return the minimum data value for the filled dependent array, or null if not analyzed.
@return the minimum data value for the filled dependent array, or null if not analyzed.
*/
public Double getMinYFilled ()
{   Double minYFilled = __minYfilled;
    if ( (minYFilled == null) && (getYFilled().length > 0) ) {
        minYFilled = MathUtil.min(getYFilled());
        setMinYFilled ( minYFilled );
    }
    return minYFilled;
}

/**
Return the size of the filled dependent array.
*/
public int getNFilled ()
{
    if ( __yFilled == null ) {
        return 0;
    }
    else {
        return __yFilled.length;
    }
}

/**
Return the skew for the filled dependent array, or null if not analyzed.
@return the skew for the filled dependent array, or null if not analyzed.
*/
public Double getSkewYFilled ()
{   Double skewYfilled = __skewYfilled;
    if ( (skewYfilled == null) && (getYFilled().length >= 3) ) {
        skewYfilled = MathUtil.skew(getYFilled().length, getYFilled());
        setSkewYFilled( skewYfilled );
    }
    return skewYfilled;
}

/**
Return the standard deviation for the filled dependent array, or null if not analyzed.
@return the standard deviation for the filled dependent array, or null if not analyzed.
*/
public Double getStandardDeviationYFilled ()
{   Double stddevYfilled = __stddevYfilled;
    if ( (stddevYfilled == null) && (getYFilled().length >= 2) ) {
        stddevYfilled = MathUtil.standardDeviation(getYFilled());
        setStandardDeviationYFilled( stddevYfilled );
    }
    return stddevYfilled;
}

/**
Return the filled dependent data array.
*/
public double [] getYFilled ()
{
    return __yFilled;
}

/**
Set the maximum data value for the filled dependent data.
@param maxYFilled Maximum data value for the filled dependent data.
*/
private void setMaxYFilled ( Double maxYFilled )
{   __maxYfilled = maxYFilled;
}

/**
Set the mean for the filled dependent data.
@param meanYFilled Mean for the filled dependent data.
*/
private void setMeanYFilled ( Double meanYFilled )
{   __meanYfilled = meanYFilled;
}

/**
Set the minimum data value for the filled dependent data.
@param minYFilled Minimum data value for the filled dependent data.
*/
private void setMinYFilled ( Double minYFilled )
{   __minYfilled = minYFilled;
}

/**
Set the skew for the filled dependent data.
@param skewYFilled skew for the filled dependent data.
*/
private void setSkewYFilled ( Double skewYFilled )
{   __skewYfilled = skewYFilled;
}

/**
Set the standard deviation for the filled dependent data.
@param stddevYFilled Standard deviation for the filled dependent data.
*/
private void setStandardDeviationYFilled ( Double stddevYFilled )
{   __stddevYfilled = stddevYFilled;
}

}