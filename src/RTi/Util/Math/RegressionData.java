package RTi.Util.Math;

import java.security.InvalidParameterException;

/**
Data used in a regression analysis.  The data are simple arrays; however, when combined to handle monthly
regression or other complex analysis, the object help manage data.
*/
public class RegressionData
{
    
/**
The independent data array, for points that overlap the dependent data array.
*/
double [] __x1 = new double[0];

/**
The independent data array, for points that DO NOT overlap the dependent data array.
*/
double [] __x2 = new double[0];

/**
The dependent data array, for points that overlap the independent data array.
*/
double [] __y1 = new double[0];

/**
Constructor.  Set the independent and dependent data arrays, which should exactly overlap and not contain
missing values.
@param x1 independent data array, for points that DO NOT overlap the dependent data array
@param y2 dependent data array, for points that overlap the independent data array
@param x2 independent data array, for points that DO NOT overlap the dependent data array
*/
public RegressionData ( double [] x1, double [] y1, double [] x2 )
{
    __x1 = x1;
    __y1 = y1;
    __x2 = x2;
    // The lengths of the overlapping arrays are required to be same
    if ( __x1.length != __y1.length ) {
        throw new InvalidParameterException( "Independent and dependent arrays are not the same length." );
    }
}

/**
Return the size of the overlapping arrays.
*/
public int getN1 ()
{
    return __x1.length;
}

/**
Return the size of the non-overlapping independent array.
*/
public int getN2 ()
{
    if ( __x1 == null ) {
        return 0;
    }
    else {
        return __x2.length;
    }
}

/**
Return the independent data array.
*/
public double [] getX1 ()
{
    return __x1;
}

/**
Return the independent data array.
*/
public double [] getX2 ()
{
    return __x2;
}

/**
Return the dependent data array.
*/
public double [] getY1 ()
{
    return __y1;
}

}