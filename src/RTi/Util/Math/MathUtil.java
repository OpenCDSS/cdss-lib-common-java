//------------------------------------------------------------------------------
// MathUtil class - math utility class.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 16 Sep 1997	Matthew J. Rutherford	Created initial version.
//		RTi
// 26 May 1998	Steven A. Malers, RTi	Add log10 because JDK does not currently
//					have.
// 01 Jun 1998	Catherine E. Nutting,	Add regress.
//		RTi
// 02 Jun 1998  CEN, RTi		Add regressLog.
// 24 Aug 1998	SAM, RTi		Overload sort to take array of ints.
// 16 Nov 1998	SAM, RTi		Change RegressionData to Regression.
// 09 Jan 1999	SAM, RTi		Add number of points to regression data
//					since this is useful to be printed out
//					later.
// 26 Oct 2000	SAM, RTi		Add int overload for max, min.
//					Set arrays to null after use in these
//					methods to help garbage collection.  Add
//					interpolate().
// 30 Oct 2000	SAM, RTi		Fix bug in sort() where sort_order was
//					being checked for null even if sflag is
//					false.  Set the max and min values used
//					when performing regression.  Add
//					double versions of max/min that take
//					a missing data value, since that is
//					used so often.
// 2002-02-25	SAM, RTi		Overload mean(), standardDeviation(),
//					and variance() to take missing data
//					value.  Change the variable names in the
//					regression code to be clearer.
// 2002-03-18	SAM, RTi		Fix problem with handling missing data
//					in regression (reverse logic was
//					confusing).  Fix bug in regression
//					introduced when variable names were
//					changed.  Add more comments to
//					regression code to make it easier to
//					follow statistics books.
// 2002-03-24	SAM, RTi		Update for new Regression class.
// 2002-03-25	SAM, RTi		More changes to compute N1, N2 values
//					so that general output can be supported.
//					To do this the code does more data
//					manipulation but is more flexible and
//					allows more reporting.
// 2002-04-03	SAM, RTi		Save transformed RMSE in addition to the
//					untransformed RMSE when doing
//					regression.
// 2002-04-08	SAM, RTi		Update to handle Y mean and standard
//					deviation in regression.
// 2002-04-29	SAM, RTi		Add commonDenominator() method.
// 2003-05-14	SAM, RTi		Add an intercept option to the
//					regression.
// 2003-12-03	J. Thomas Sapienza, RTi	Added code to convert from decimal to
//					hex values.
// 2004-03-15	SAM, RTi		Overload sum to take a missing value.
// 2004-06-02	SAm, RTi		Add methods from old GR C++ code to
//					support standard log normal plot axes:
//						integrate()
//------------------------------------------------------------------------------
//EndHeader

package RTi.Util.Math;
import	RTi.Util.Message.Message;

import java.lang.Math;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
Static methods for processing statistics.
*/
public class MathUtil
{
/**
Sort using a bubble sort.
*/
public static final int SORT_BUBBLE = 1;
/**
Sort using a heap sort.
*/
public static final int SORT_HEAP = 2;
/**
Sort using a quick sort.
*/
public static final int SORT_QUICK = 3;

/**
Sort into ascending order.
*/
public static final int SORT_ASCENDING = 1;
/**
Sort into descending order.
*/
public static final int SORT_DESCENDING = 2;

/**
Method to integrate a function:  Gaussian quadrature integration.
*/
public static final int INTEGRATE_GAUSSIAN_QUADRATURE = 1;

/**
Determine the common denominators for a list of integers.
@param values Integer values to check.
@param return_num If -1, return the smallest common denominator (other than 1).
If 1, return the largest common denominator (may match one of the values).
If 0, return all common denominators, largest value first.
@return an array of common denominators, controlled by the "return_num" flag,
or null if no appropriate denominators can be determined.
*/
public static int[] commonDenominators ( int values[], int return_num )
{	int nvalues = 0;
	if ( values != null ) {
		nvalues = values.length;
	}
	if ( nvalues == 0 ) {
		return null;
	}
	// Don't really know a better way to do it, so just use the brute force method...
	int max_value = 0;
	try {
	    max_value = max ( values );
	}
	catch ( Exception e ) {
		return null;
	}
	int j = 0;
	boolean divisible;
	List<Integer> common_denominators = new Vector();
	for ( int i = max_value; i > 1; i-- ) {
		divisible = true;
		for ( j = 0; j < nvalues; j++ ) {
			if ( (values[j]%i) != 0 ) {
				// Not evenly divisible...
				divisible = false;
				break;
			}
		}
		if ( divisible ) {
			// Save the value...
			common_denominators.add ( new Integer(i) );
		}
	}
	// Now return...
	if ( common_denominators.size() == 0 ) {
		return null;
	}
	else if ( return_num == -1 ) {
		j = (common_denominators.get(common_denominators.size() - 1)).intValue();
		int [] iarray = new int[1];
		iarray[0] = j;
		return iarray;
	}
	else if ( return_num == 1 ) {
		j = (common_denominators.get(0)).intValue();
		int [] iarray = new int[1];
		iarray[0] = j;
		return iarray;
	}
	// Return all...
	int size = common_denominators.size();
	int [] iarray = new int[size];
	for ( int i = 0; i < size; i++ ) {
		iarray[i] = (common_denominators.get(i)).intValue();
	}
	return iarray;
}

/**
Converts a decimal number in the range 0-255 into a 2-character hex string.
@param num the number to convert to hexadecimal.
@return the two-digit hex string, or null if the number couldn't be converted.
*/
public static String decimalToHex(int num) {
	if (num < 0 || num > 255) {
		return null;
	}

	String hex = "";
	int div = num / 16;
	hex += decimalToHexHelper(div);
	hex += decimalToHexHelper((num % 16));

	return hex;
}

/**
Converts a one-digit number in the range 0-15 into a one-character hex value.
@param num the number to convert to hex.
@return the one-character hex representation.
*/
private static String decimalToHexHelper(int num) {
	String hex = "";
	if (num < 10) {
		hex = "" + num;
		return hex;
	}
	switch (num) {
		case 10:	hex = "A";	break;
		case 11:	hex = "B";	break;
		case 12:	hex = "C";	break;
		case 13:	hex = "D";	break;
		case 14:	hex = "E";	break;
		case 15:	hex = "F";	break;
	}
	return hex;
}

/**
Perform integration for a function.
@param method Method used to integrate.  See INTEGRATE_*.
@param func Function to integrate (using Function interface).
@param a0 Left end point for entire interval.
@param b0 Right end point for entire interval.
@param nseg Number of segments to divide interval (use higher number for functions that are not smooth).
@exception Exception if the function cannot be integrated.
*/
public static double integrate ( int method, Function func, double a0, double b0, int nseg )
throws Exception
{	double a, b; // End-points used for integration
	double dx; // increment to map "gx" values to real values
	double s; // integration value
	double seglen; // segment length
	double stot; // total for all segments
	double xm; // point in center of "a" and "b"
	double xr; // length of "a" to "b"
	int i; // counter for integration points
	int j; // counter for segments
	int ng = 5; // number of integration points
	double gx[] = {
	            0.1488743389, // unit-interval points to evaluate function
				0.4333953941,
				0.6794095682,
				0.8650633666,
				0.9739065285 },
		gw[] = {
	            0.2955242247, // Gauss weights corresponding to "gx"
				0.2692667193,
				0.2190863625,
				0.1494513491,
				0.0666713443 };
	
	if ( nseg <= 0 ) {
		nseg = 1;
	}

	// Divide the interval into segments and integrate each (helps with
	// functions that are not smooth, and long intervals)...

	seglen = (b0 - a0)/nseg;
	stot = 0.0;
	a = a0;
	b = a0 + seglen;

	double [] func_input1 = new double[1];
	double [] func_input2 = new double[1];
	for ( j = 0; j < nseg; j++ ) {
		xm	= 0.5*(b + a);
		xr	= 0.5*(b - a);
		s	= 0.0;

		for ( i = 0; i < ng; i++ ) {
			dx = xr*gx[i];
			func_input1[0] = xm + dx; 
			func_input2[0] = xm - dx; 
			s += gw[i]*(func.evaluate(func_input1) + func.evaluate(func_input2));
		}
		s *= xr;
		stot += s;

		a = b;
		b = a + seglen;
	}

	return stot;
}

/**
Do a linear interpolation/extrapolation.
@return The result.
@param x Value on "X" axis to interpolate to "Y" axis.
@param xmin "Left" value on "X" axis.
@param xmax "Right" value on "X" axis.
@param ymin "Left" value on "Y" axis.
@param ymax "Right" value on "Y" axis.
*/
public static double interpolate ( double x, double xmin, double xmax, double ymin, double ymax )
{	double	y;

	if ( (xmax - xmin) == 0.0 ) {
		y = ymin;
	}
	else {
	    y = ymin + (ymax - ymin)*(x - xmin)/(xmax - xmin);
	}
	return y;
}

/**
@return the Log base 10 value for a number.  This is a work-around because
JDK does not have a log10.  The value returned is simply log(x)/log(10).  No
error checks are in place.  Reference:   JDC bug 4074599.
@param x Value for which to determine log base 10.
*/
public static double log10 ( double x )
{
	return Math.log(x)/Math.log(10.0);
}

/**
Find the maximum of two values.
@return the maximum value.
@param x First value to check.
@param y Second value to check.
*/
public static double max ( double x, double y )
{	// Don't call array overload (try to optimize performance)!
	if ( x >= y ) {
		return x;
	}
	else {
	    return y;
	}
}

/**
Find the maximum of two values but ignore missing.
@return the maximum value or the missing data value if both are missing.
@param x First value to check.
@param y Second value to check.
@param missing Value to consider missing.
*/
public static double max ( double x, double y, double missing )
{	// Don't call array overload (try to optimize performance)!
	if ( (x == missing) && (y == missing) ) {
		// Both are missing so return missing.
		return missing;
	}
	else if ( x == missing ) {
		// Only x is missing...
		return y;
	}
	else if ( y == missing ) {
		// Only y is missing...
		return x;
	}
	return max ( x, y );
}

/**
Find the maximum in an array.
@return the maximum value.
@param x Array of values to compare.
@exception If there is an error.
*/
public static double max ( double x[] )
throws Exception
{
	try {
	    return max ( x.length, x );
	}
	catch ( Exception e ) {
		throw e;
	}
}

/**
@return The maximum value in an array.
@param n Number of values in array.
@param x Array of values.
@exception java.lang.Exception If the number of points is <= 0.
*/
public static double max ( int n, double x[] )
throws Exception
{	int	i;
	String	routine = "MathUtil.max";
	double	m = 0.0;

	if ( n <= 0 ) {
		String message = "Number of points <= 0";
		Message.printWarning ( 10, routine, message );
		throw new Exception ( message );
	}
	m = x[0];
	for ( i = 1; i < n; i++ ) {
		if ( x[i] > m ) {	
			m = x[i];
		}
	}
	return m;
}

/**
Find the maximum value in an integer list.
*/
public static int max ( int x, int y )
throws Exception
{
	int [] a = new int[2];
	a[0] = x;
	a[1] = y;
	try {
	    int m = max ( a );
		a = null;
		return m;
	}
	catch ( Exception e ) {
		a = null;
		throw e;
	}
}

public static int max ( int x[] )
throws Exception
{
	try {
	    return max ( x.length, x );
	}
	catch ( Exception e ) {
		throw e;
	}
}

/**
@return The maximum value in an array.
@param x Array of values.
@exception java.lang.Exception If the number of points is <= 0.
*/
public static int max ( int n, int x[] )
throws Exception
{	int	i;
	String routine = "MathUtil.max";
	int	m = 0;

	if ( n <= 0 ) {
		String message = "Number of points <= 0";
		Message.printWarning ( 10, routine, message );
		throw new Exception ( message );
	}
	m = x[0];
	for ( i = 1; i < n; i++ ) {
		if ( x[i] > m ) {	
			m = x[i];
		}
	}
	return m;
}

/*------------------------------------------------------------------------------
** mean - calculate the mean for a list of numbers
**------------------------------------------------------------------------------
** Copyright:   See the COPYRIGHT file.
**------------------------------------------------------------------------------
** History:
**
** 05-25-95	Steven A. Malers, RTi	Make function return a status and pass
**					mean in parameter list.
** 06 Sep 1996  Steven A. Malers, RTi   Split out of the HMStat.c file.
** 04 Apr 1998	SAM, RTi		Port to Java.
**------------------------------------------------------------------------------
** Variables	I/O	Description
**
** mean		O	Mean of the values.
** message	L	String for messages.
** n		I	Number of data values.
** routine	L	Name of this routine.
** sum		L	Sum of data values.
** x		I	Data values.
**------------------------------------------------------------------------------
*/
public static double mean ( double x[] )
throws Exception
{	try {
        return mean ( x.length, x );
	}
	catch ( Exception e ) {
		throw e;
	}
}

/**
Compute the mean of an array of values.
@return The mean value in an array.
@param x Array of values.
@exception java.lang.Exception If the number of points is <= 0.
*/
public static double mean ( int n, double x[] )
throws Exception
{	String	message, routine = "MathUtil.mean";
	double	thesum;
	double	mean = 0.0;

	if ( n <= 0 ) {
		message = "Number of points <= 0";
		Message.printWarning ( 10, routine, message );
		throw new Exception ( message );
	}
	try {
	    thesum = sum ( n, x );
	}
	catch (	Exception e ) {
		throw e;
	}
	mean = thesum/(double)(n);
	return mean;
}

/**
Compute the mean of an array of values.
@return The mean value in an array.
@param x Array of values.
@param missing Missing data value (to ignore).
@exception java.lang.Exception If the number of points is <= 0.
*/
public static double mean ( int n, double x[], double missing )
throws Exception
{	String	message, routine = "MathUtil.mean";
	double	thesum = 0.0;
	double	mean = 0.0;

	if ( n <= 0 ) {
		message = "Number of points <= 0";
		Message.printWarning ( 10, routine, message );
		throw new Exception ( message );
	}
	int n2 = 0;
	try {
	    for ( int i = 0; i < n; i++ ) {
			if ( x[i] != missing ) { 
				thesum += x[i];
				++n2;
			}
		}
	}
	catch (	Exception e ) {
		throw e;
	}
	if ( n2 <= 0 ) {
		message = "Number of non-missing points <= 0";
		Message.printWarning ( 10, routine, message );
		throw new Exception ( message );
	}
	mean = thesum/(double)(n2);
	return mean;
}

/**
Find the minimum of two values but ignore missing.
@return the minimum value or the missing data value if both are missing.
@param x First value to check.
@param y Second value to check.
@param missing Value to consider as missing - matching values will be ignored.
*/
public static double min ( double x, double y, double missing )
{	// Don't call array overload (try to optimize performance)!
	if ( (x == missing) && (y == missing) ) {
		// Both are missing so return missing.
		return missing;
	}
	else if ( x == missing ) {
		// Only x is missing...
		return y;
	}
	else if ( y == missing ) {
		// Only y is missing...
		return x;
	}
	return min ( x, y );
}

/**
Find the median value in an array.  If the number of values is even, the average of the middle two values is returned.
@param n Number of values from x to evaluate.
@param x The array to evaluate.
@return the median value from x
*/
public static double median(int n, double x[])
{
    double[] b = new double[n];
    System.arraycopy(x, 0, b, 0, n);
    Arrays.sort(b);

    if ( (n % 2) == 0) {
        // Even number in the sample so return the average of the middle two values.
        return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
    }
    else {
        // Return the value in the middle of the array.  Since integer math is used for the index,
        // the roundoff will result in the correct position.
        return b[b.length/2];
    }
}

/**
Find the minimum of two values.
@return minimum of two values.
@param x First value to check.
@param y Second value to check.
*/
public static double min ( double x, double y )
{	// Do not call the array version to optimize performance!
	if ( x <= y ) {
		return x;
	}
	else {
	    return y;
	}
}

/**
Find the minimum value in an array.
@return Minimum value in an array.
@param x Array of values to compare.
*/
public static double min ( double x[] )
throws Exception
{
	try {
	    return min ( x.length, x );
	}
	catch ( Exception e ) {
		throw e;
	}
}

/**
@return The minimum value in an array.
@param x Array of values.
@exception java.lang.Exception If the number of points is <= 0.
*/
public static double min ( int n, double x[] )
throws Exception
{	int	i;
	String	routine = "MathUtil.min";
	double	m = 0.0;

	if ( n <= 0 ) {
		String message = "Number of points <= 0.0";
		Message.printWarning ( 10, routine, message );
		throw new Exception ( message );
	}
	m = x[0];
	for ( i = 1; i < n; i++ ) {
		if ( x[i] < m ) {
			m = x[i];
		}
	}
	return m;
}

/**
Find the minimum value in a list.
*/
public static int min ( int x, int y )
throws Exception
{
	int [] a = new int[2];
	a[0] = x;
	a[1] = y;
	try {
	    int m = min ( a );
		a = null;
		return m;
	}
	catch ( Exception e ) {
		a = null;
		throw e;
	}
}

public static int min ( int x[] )
throws Exception
{
	try {
	    return min ( x.length, x );
	}
	catch ( Exception e ) {
		throw e;
	}
}

/**
@return The minimum value in an array.
@param x Array of values.
@exception java.lang.Exception If the number of points is <= 0.
*/
public static int min ( int n, int x[] )
throws Exception
{	int	i;
	String	routine = "MathUtil.min";
	int	m = 0;

	if ( n <= 0 ) {
		String message = "Number of points <= 0.0";
		Message.printWarning ( 10, routine, message );
		throw new Exception ( message );
	}
	m = x[0];
	for ( i = 1; i < n; i++ ) {
		if ( x[i] < m ) {
			m = x[i];
		}
	}
	return m;
}

public static PrincipalComponentAnalysis performPrincipalComponentAnalysis (
        double[] dependentArray, double[][] independentMatrix,
        double missingDependentValue, double missingIndependentValue,
        int maximumCombinations )
throws Exception
{
    PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis(
            dependentArray, independentMatrix, missingDependentValue, missingIndependentValue,
            maximumCombinations );
    return pca;
}

/**
Perform an ordinary least squares regression on two arrays of data.  See the
Regression class methods for more information.  Analysis results are saved and
can be retrieved using Regression class methods.  If no regression can be
performed, the limits are set to the missing data value indicator.
In addition to the results needed for the regression, additional statistics are
computed and are available in the Regression object.  Statistics that cannot be
computed are set to -999 (e.g., when N2 is 0, the X2 mean is set to -999).
@return a Regression object for the analysis.
@param xArray Array of independent (X) values.
@param yArray Array of dependent (Y) values.
@param useMissing true indicates to use "missingx" and "missingy" in
calculations (if false, the data values are not checked against "missingx" and
"missingy").  Using false increases performance but the calling code must itself
strip out missing data values.
@param missingx Missing data value indicator for "xArray".
@param missingy Missing data value indicator for "yArray".
@exception java.lang.Exception If no data are available to be regressed (e.g.,
all missing), or data array lengths are unequal.
@param force_intercept If true, use the "intercept" parameter to specify a 
forced intercept (A value).  If false, calculate the intercept.
@param intercept If "forceIntercept" is true, use this parameter to specify the
parameter.  Note that currently the intercept must be zero.
@exception java.lang.Exception If no data are available to be regressed (e.g.,
all missing), or data array lengths are unequal.
*/
public static Regression regress (	double [] xArray, double [] yArray, boolean useMissing, double missingx,
					double missingy, boolean force_intercept, double intercept )
throws Exception
{	return regress (xArray, yArray, useMissing, missingx, missingy, false, force_intercept, intercept );
}

/**
Perform an ordinary least squares regression on two arrays of data.  See the
Regression class methods for more information.  Analysis results are saved and
can be retrieved using Regression class methods.  If no regression can be
performed, the limits are set to the missing data value indicator.
In addition to the results needed for the regression, additional statistics are
computed and are available in the Regression object.  Statistics that cannot be
computed are set to -999 (e.g., when N2 is 0, the X2 mean is set to -999).
@return a Regression object for the analysis.
@param xArray Array of independent (X) values.
@param yArray Array of dependent (Y) values.
@param useMissing true indicates to use "missingx" and "missingy" in
calculations (if false, the data values are not checked against "missingx" and
"missingy").  Using false increases performance but the calling code must itself
strip out missing data values.
@param missingx Missing data value indicator for "xArray".
@param missingy Missing data value indicator for "yArray".
@param data_transformed Indicates whether the data being passed in have been
transformed (e.g., log10).  If so the RMSE are saved in the transformed RMSE data member.
@param forceIntercept If true, use the "intercept" parameter to specify a forced
intercept (A value).  If false, calculate the intercept.
@param intercept If "forceIntercept" is true, use this parameter to specify the
parameter.  Note that currently the intercept must be zero.
@exception java.lang.Exception If no data are available to be regressed (e.g.,
all missing), or data array lengths are unequal.
*/
private static Regression regress (	double [] xArray, double [] yArray, boolean useMissing, double missingx,
					double missingy, boolean data_transformed, boolean forceIntercept, double intercept )
throws Exception
{	String rtn = "MathUtil.regress";
	if ( xArray.length != yArray.length ) {
		String message = "Lengths of arrays are unequal.";
		Message.printWarning ( 2, rtn, message );
		throw new Exception ( message );
	}

	double N = xArray.length;
	int n1 = 0;
	int n2 = 0;
	double totalX1minusY1_sq = 0;
	double totalX1Y1 = 0;
	double totalX1 = 0;
	double totalX1_sq = 0;
	double totalY1 = 0;
	double totalY1_sq = 0;
	double a = 0;
	double b = 1;
	double rmse = 0;
	double r = 1;

	double	maxY1 = missingy, maxX1 = missingx, minY1 = missingy, minX1 = missingx;
	boolean limits_found = false;

	// To analyze the data, non-missing data are required for both X and
	// Y.  Create arrays for the N1 and N2 data...

	for ( int i = 0; i < N; i++ ) {
		if ( useMissing ) {
			if ((xArray[i] != missingx) && (yArray[i] != missingy)){
				// Both are not missing...
				++n1;
			}
			else if ( xArray[i] != missingx ) {
				// Have x but not y...
				++n2;
			}
		}
	}

	// Allocate the arrays for X1, X2 and Y1...

	double [] X1 = null;
	double [] X2 = null;
	double [] Y1 = null;

	if ( useMissing ) {
		// Allocate new sub-arrays for the analysis...
		X1 = new double[n1];
		Y1 = new double[n1];
		if ( n2 != 0 ) {
			X2 = new double[n2];
		}
		n1 = 0;	// Re-use
		n2 = 0;
		for ( int i = 0; i < N; i++ ) {
			if ((xArray[i] != missingx) && (yArray[i] != missingy)){
				// Both are not missing...
				X1[n1] = xArray[i];
				Y1[n1] = yArray[i];
				++n1;
			}
			else if ( xArray[i] != missingx ) {
				// Have x but not y...
				X2[n2] = xArray[i];
				++n2;
			}
		}
	}
	else {
	    // Use the original arrays...
		X1 = xArray;
		Y1 = yArray;
		n1 = xArray.length;
		n2 = 0;
	}

	if ( n1 == 0 ) {
		String message = "No data to create regression analysis.";
		Message.printWarning ( 2, rtn, message );
		throw new Exception ( message );
	}

	// For now use the original logic (SAMX need to optimize some)...

	for ( int i = 0; i < n1; i++ ) {
		totalX1minusY1_sq += ((X1[i] - Y1[i] )*(X1[i] - Y1[i]));
		totalX1Y1 += (X1[i] * Y1[i]);
		totalX1 += X1[i];
		totalX1_sq += (X1[i]*X1[i]);
		totalY1 += Y1[i];
		totalY1_sq += (Y1[i] * Y1[i]);
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, rtn, "X1: " + X1[i] + ", Y1: " + Y1[i] );
		}
		if ( !limits_found ) {
			// Initialize
			minX1 = maxX1 = X1[i];
			minY1 = maxY1 = Y1[i];
			limits_found = true;
		}
		else {
		    maxX1 = max ( maxX1, X1[i] );
			minX1 = min ( minX1, X1[i] );
			maxY1 = max ( maxY1, Y1[i] );
			minY1 = min ( minY1, Y1[i] );
		}
	}

	// now calculate rmsError, correlationCoeff, a and b
	//
	// rmsError ...
	rmse = Math.sqrt ( totalX1minusY1_sq / (double)n1 );

	// correlationCoeff, R ...
	//
	// The following is computed by...
	//
	// R = sum(XY) - [sum(x)*sum(y)]/n
	//     ---------------------------------------------------------
	//	sqrt(x^2 - [sum(x)^2]/n) * sqrt(y^2 - [sum(y)^2]/n)
	//
	// or
	//
	// R = N*sum(XY) - [sum(x)*sum(y)]
	//     ---------------------------------------------------------
	//	sqrt(N*x^2 - sum(x)^2) * sqrt(N*y^2 - sum(y)^2)
	//
	// or
	//
	// R = N*sum(XY) - [sum(x)*sum(y)]
	//     ---------------------------------------------------------
	//	sqrt([N*x^2 - sum(x)^2] * [N*y^2 - sum(y)^2])

	double denom = Math.sqrt ((((double)n1 * totalX1_sq) -
		(totalX1*totalX1)) * (((double)n1 * totalY1_sq) - (totalY1*totalY1)));
		
	if ( denom > 0 ) {
		r = (((double)n1 * totalX1Y1) - (totalX1 * totalY1))/denom;
	}
	else {
	    r = -999.0;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, rtn, "Data leading to regression analysis: ");
		Message.printDebug ( 10, rtn, "n1: " + n1 );
		Message.printDebug ( 10, rtn, "totalX1_sq: " + totalX1_sq );
		Message.printDebug ( 10, rtn, "totalX1: " + totalX1 );
		Message.printDebug ( 10, rtn, "totalY1: " + totalY1 );
		Message.printDebug ( 10, rtn, "totalX1Y1: " + totalX1Y1 );
	}

	// If the intercept is not specified:
	//
	// b = sum(xy) - sum(x)*sum(y)/n    n*sum(xy) - sum(x)*sum(y)
	//     ------------------------- =  -------------------------
	//     sum(x^2) - sum(x)^2/n        n*sum(x^2) - sum(x)^2
	//
	// If the intercept is specified as zero:
	//
	// b = sum(xy)/sum(x^2)

	if ( forceIntercept ) {
		if ( totalX1_sq == 0.0 ) {
			// TODO - should this throw an exception?
			b = -999.0;
		}
		else {
		    b = totalX1Y1/totalX1_sq;
		}
	}
	else {
	    denom = ((double)n1*totalX1_sq) - (totalX1*totalX1);
		if ( denom > 0 ) {
			b = (((double)n1*totalX1Y1) - (totalX1*totalY1))/denom;
		}
		else {
		    // TODO - should this throw an exception?
			b = -999.0;
		}
	}

	// If the intercept is not specified:
	//
	// a = sum(y)/n - b*sum(x)/n
	//
	// If the intercept is specified as zero:
	//
	// a = 0.0

	if ( forceIntercept ) {
		if ( intercept != 0.0 ) {
			throw new Exception ( "Only zero intercept is supported (specified as " + intercept + ")." );
		}
		a = intercept;
	}
	else {
	    // Compute...
		a = (totalY1/(double)n1) - (b*totalX1/(double)n1);
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, rtn, "Regression analysis results: " +
		"a: " + a + ", " + "b: " + b + ", " + "rmsError: " + rmse + ", " + "R: " + r );
	}

	// Save in Regression object...

	Regression rd = new Regression();
	rd.setA ( a );
	rd.setB ( b );
	if ( data_transformed ) {
		rd.setTransformedRMSE ( rmse );
	}
	else {
	    rd.setRMSE ( rmse );
	}
	rd.forceIntercept ( forceIntercept );
	rd.setN1 ( n1 );
	rd.setX1 ( X1 );
	rd.setY1 ( Y1 );
	rd.setN2 ( n2 );
	rd.setCorrelationCoefficient ( r );
	rd.isAnalyzed ( true );
	rd.setMeanX1 ( 0.0 );	// Until this is figured out.
	rd.setMeanY1 ( 0.0 );	// Until this is figured out.
	rd.setMaxX1 ( maxX1 );
	rd.setMinX1 ( minX1 );
	rd.setMaxY1 ( maxY1 );
	rd.setMinY1 ( minY1 );

	// Save values not computed above...

	// Assume for now that Y and Y1 are the same...

	rd.setMeanY ( rd.getMeanY1() );
	rd.setStandardDeviationY ( rd.getStandardDeviationY1() );

	if ( useMissing ) {
		rd.setMeanX ( mean(xArray.length, xArray, missingx) );
		rd.setStandardDeviationX ( standardDeviation(xArray.length, xArray, missingx) );
		rd.setStandardDeviationX1 ( standardDeviation(X1.length, X1, missingx) );
		if ( n2 == 0 ) {
			rd.setMeanX2 ( -999.0 );
			rd.setStandardDeviationX2 ( -999.0 );
		}
		else {
		    rd.setMeanX2 ( mean(X2.length, X2, missingx) );
			rd.setStandardDeviationX2 (	standardDeviation(X2.length, X2, missingx) );
		}
		rd.setStandardDeviationY1 ( standardDeviation(Y1.length, Y1, missingy) );
	}
	else {
	    rd.setMeanX ( mean(xArray.length, xArray) );
		rd.setStandardDeviationX ( standardDeviation(xArray.length, xArray) );
		rd.setStandardDeviationX1 ( standardDeviation(X1.length, X1) );
		if ( n2 == 0 ) {
			rd.setMeanX2 ( -999.0 );
			rd.setStandardDeviationX2 ( -999.0 );
		}
		else {
		    rd.setMeanX2 ( mean(X2.length, X2) );
			rd.setStandardDeviationX2 ( standardDeviation(X2.length, X2 ) );
		}
		rd.setStandardDeviationY1 ( standardDeviation(Y1.length, Y1 ) );
	}
	return rd;
}

/**
Perform an ordinary least squares regression on two arrays of data, by first
transforming the data using log10().  See the
Regression class methods for more information.  Analysis results are saved and
can be retrieved using Regression class methods.  If no regression can be
performed, the limits are set to the missing data value indicator.
@return a Regression instance containing the results of the analysis.
@param xArray Array of independent (X) values.
@param yArray Array of dependent (Y) values.
@param missingx Missing data value indicator for "xArray".
@param missingy Missing data value indicator for "yArray".
@param useMissing true indicates to use "missingx" and "missingy" in
calculations (if false the values are not checked against "missingx" and "missingy").
@exception java.lang.Exception If the length of the two arrays differ.
*/
public static Regression regressLog ( double [] xArray, double [] yArray, 
					boolean useMissing, double missingx, double missingy )
throws Exception
{	String rtn = "MathUtil.regressLog";
	if ( xArray.length != yArray.length ) {
		String message = "Lengths of arrays are unequal.";
		Message.printWarning ( 2, rtn, message );
		throw new Exception ( message );
	}

	// move all the information in each array into a new array which
	// is the log10 value for each item in the respective arrays

	int length = xArray.length;

	double [] logArrayX = new double[length];
	for ( int i=0; i<length; i++ ) {
		if ( useMissing && (xArray[i] == missingx) ) {
			logArrayX[i] = missingx;
		}
		else {
		    if ( xArray[i] > 0 ) {
				logArrayX[i] = log10(xArray[i]);
			}
			else {
			    logArrayX[i] = .001;
			}
		}
	}

	double [] logArrayY = new double[length];
	for ( int i=0; i<length; i++ ) {
		if ( useMissing && (yArray[i] == missingy) ) {
			logArrayY[i] = missingy;
		}
		else {
		    if ( yArray[i] > 0 ) {
				logArrayY[i] = log10(yArray[i]);
			}
			else {
			    logArrayY[i] = .001;
			}
		}
	}

	// Do the regression on the transformed data.  The RMSE will be saved in the transformed data member...

	Regression rd = regress ( logArrayX, logArrayY, useMissing, missingx, missingy, true, false, 0 );
	// Also compute the RMSE for the untransformed data...
	rd.setRMSE ( RMSError(xArray.length, xArray, yArray, useMissing, missingx, missingy) );

	// Reset with the actual limits (not the log values)...

	if ( rd.getMaxX1() != missingx ) {
		rd.setMaxX1 ( Math.pow(10.0,rd.getMaxX1()) );
	}
	if ( rd.getMinX1() != missingx ) {
		rd.setMinX1 ( Math.pow(10.0,rd.getMinX1()) );
	}
	if ( rd.getMaxY1() != missingy ) {
		rd.setMaxY1 ( Math.pow(10.0,rd.getMaxY1()) );
	}
	if ( rd.getMinY1() != missingy ) {
		rd.setMinY1 ( Math.pow(10.0,rd.getMinY1()) );
	}
	// Clean up memory...
	logArrayY = null;
	logArrayX = null;
	return rd;
}

/*------------------------------------------------------------------------------
** reverseArray - reverse an array of double numbers
**------------------------------------------------------------------------------
** Copyright:   See the COPYRIGHT file.
**------------------------------------------------------------------------------
** Notes:
**		(1)	Since we need to swap the top half of the array with
**			the bottom we only need to go from 0 to (ndata / 2).
**		(2)	The original array is overwritten by the new array.
**		(3)	Returns 0 for success and 1 for failure.
**------------------------------------------------------------------------------
** History:
**
** 16 Sep 1997	MJR, RTi	Ported to Java.
**------------------------------------------------------------------------------
** Variable	I/O	Description
**
** data		I/O	Incoming array of numbers
** half		L	Half of ndata
** i		L	Loop index
** j		L	Array index
** message	L	Global message string.
** ndata	I	Number of values in data
** routine	L	Routine name
** tempf	L	Temporary variable
**------------------------------------------------------------------------------
*/

/**
Reverse an array of doubles.
@return Zero if successful and 1 if not successful.
@param data An array of doubles to be reversed.
*/
public static int reverseArray(	double[] data )
{	int	i, j, half, ndata = data.length;
	double	tempf;
	String	routine="MathUtil.reverseArray";

	if ( data == null ) {
		Message.printWarning ( 2, routine, "No array to reverse!" );
		return ( 1 );
	}

	half = ndata / 2;
	j = ndata - 1;

	for ( i = 0; i < half; i++ ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, routine, "Moving data[" + i + "] (" + data[i] + ") to tempf." );
		}
		tempf = data[i];
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, routine, "Moving data[" +j+ "] (" +data[j]+ ") to data["+i+"].");
		}

		data[i]	= data[j];
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, routine, "Moving tempf (" +tempf+ ") to data[" +j+ "]." );
		}
		data[j]	= tempf;
		j--;
	}
	return ( 0 );
}

/**
Reverse an array of integers.
@return Zero if successful and 1 if not successful.
@param data An array of integers to be reversed.
*/
public static int reverseArray(	int[] data )
{	int	i, j, half, ndata = data.length;
	int	tempi;
	String routine="MathUtil.reverseArray";

	if ( data == null ) {
		Message.printWarning ( 2, routine, "No array to reverse!" );
		return ( 1 );
	}

	half = ndata / 2;
	j = ndata - 1;

	for ( i = 0; i < half; i++ ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, routine, "Moving data[" + i + "] (" + data[i] + ") to tempi." );
		}
		tempi = data[i];
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, routine, "Moving data[" +j+ "] (" +data[j]+ ") to data["+i+"].");
		}

		data[i]	= data[j];
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, routine, "Moving tempi (" +tempi+ ") to data[" +j+ "]." );
		}
		data[j]	= tempi;
		j--;
	}
	return ( 0 );
}

/**
Compute the RMS error between two sets as sqrt(sum((y - x)^2)/n).
@param n Number of points to consider (typically the length of the arrays).
@param x Independent data.
@param y Dependent data corresponding to x.
@param use_missing Indicates whether the missing data flags should be used to
ignore data.  If true, then having missing data in either array causes both
array values to be ignored.
@param xmissing Missing data value for x.
@param ymissing Missing data value for y.
@return the RMS error for the data set.  Return 0 if the number of points
considered is 0.
@exception Exception if there is an error computing the RMS error.
*/
public static double RMSError ( int n, double x[], double y[],
				boolean use_missing, double xmissing, double ymissing )
throws Exception
{	int n2 = 0;
	double sume2 = 0.0;
	for ( int i = 0; i < n; i++ ) {
		if ( use_missing ) {
			if ( (x[i] == xmissing) || (y[i] == ymissing) ) {
				continue;
			}
			sume2 += ((y[i] - x[i])*(y[i] - x[i]));
			++n2;
		}
	}
	if ( n2 == 0 ) {
		return 0.0;
	}
	return Math.sqrt ( sume2/(double)n2 );
}

/* ----------------------------------------------------------------------------
** roundToPercent - round a number to a nice percent
** ----------------------------------------------------------------------------
** Copyright:   See the COPYRIGHT file.
**------------------------------------------------------------------------------
** Notes:	(1)	This routine takes a number and finds the nearest
**			value, rounded within the given interval.
** ----------------------------------------------------------------------------
** History:
**
** 05-30-95	Steven A. Malers, RTi	Original routine.
** 06 Sep 1996  Steven A. Malers, RTi   Split out of the HMStat.c file.
** 05 Jun 1998	SAM, RTi		Port to Java.
** ----------------------------------------------------------------------------
** Variable	I/O	Description
**
** fact		L	Factor to convert all data to 100.0 maximum scale.
** interval	I	Allowable interval for output.
** interval100	L	Interval with 100.0 as the maximum value.
** message	L	String for messages.
** mflag	I	Flag indicating whether maximum value is 1.0 (0) or
**			100.0 (1).
** rflag	L	Flag indicating whether value should be rounded up (1),
**			down (-1) or to nearest (0).
** x		I	Raw data value.
** xhigh	L	High even interval value.
** xhighdif	L	Difference between "xhigh" and "xs".
** xlow		L	Low even interval value.
** xlowdif	L	Difference between "xlow" and "xs".
** xr		O	Rounded value.
** xs		L	Data in units of "interval100".
** ----------------------------------------------------------------------------
*/

public static double roundToPercent ( double x, double interval, int mflag, int rflag )
throws Exception
{	String	message, routine = "MathUtil.roundToPercent";
	double	fact, interval100, xhigh, xhighdif, xlow, xlowdif, xr, xs;

	xr = 0.0;
	// Figure out factors, etc. to convert everything to 100.0 scale...
	if ( mflag > 0 ) {
		if ( (x < 0.0) || (x > 1.0) ) {
			message = "" + x + " cannot be rounded because it is not > 0.0, < 1.0";
			Message.printWarning ( 1, routine, message );
			throw new Exception ( message );
		}
		fact = 1.0;
		interval100	= interval;
		xs = x;
		
	}
	else {
	    if ( (x < 0.0) || (x > 100.0) ) {
			message = "" + x + " cannot be rounded because it is not > 0.0, < 100.0";
			Message.printWarning ( 1, routine, message );
			throw new Exception ( message );
		}
		fact = 100.0;
		interval100	= interval*fact;
		xs = x*fact;
	}
	// Adjust so that the scale for the data is one unit per "interval100".
	xs /= interval100;
	xhigh = Math.ceil ( xs );
	xhighdif = Math.abs ( xhigh - xs );
	xlow = Math.floor ( xs );
	xlowdif	= Math.abs ( xlow - xs );
	if ( (xhighdif < xlowdif) || (rflag < 0) ) {
		xr = xhigh*interval100/fact;
	}
	else if ( (xhighdif >= xlowdif) || (rflag > 0) ) {
		xr = xlow*interval100/fact;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 5, routine,
		"Rounded value for " + x + " (interval=" + interval + ", # units=" + xs + ") is " + xr );
	}
	return xr;
}

/**
Sort an array of doubles.
@return Zero if successful and 1 if not successful.
@param data Array of doubles to sort.
@param method Method to use for sorting (see SORT_*).
@param order Order to sort (SORT_ASCENDING or SORT_DESCENDING).
@param sort_order Original locations of data after sort (array needs to be
allocated before calling routine).  For example, first sort double-data and then
sort associated data by using new_other_data[i] = old_other_data[sort_order[i]];
Can be null if sflag is false.
@param sflag Indicates whether "sort_order" is to be filled.
*/
public static int sort(double[] data, int method, int order, int[] sort_order, boolean sflag )
{	String	routine="MathUtil.sort(double[]...)";
	int	i=0, ndata=data.length;

	if ( data == null ) {
		Message.printWarning( 2, routine, "Incoming data array is NULL, cannot sort." );
		return 1;
	}
	if ( sflag && (sort_order == null) ) {
		Message.printWarning( 2, routine, "Incoming sort_order array is NULL, cannot sort." );
		return 1;
	}

	if ( sflag && (sort_order.length < data.length) ) {
		Message.printWarning( 2, routine, "sort_order array length (" + sort_order.length +
		" is smaller than data array length (" + data.length + ")" );
		return 1;
	}

	// Initialize "sort_order" to sequential numbers...
	
	if ( sflag ) {
		for ( i = 0; i < ndata; i++ ) {
			sort_order[i] = i;
		}
	}

	// Now sort into ascending order...

	if ( method == SORT_QUICK ) {
		if ( sortDQuick(data, sort_order, sflag) == 1 ) {
			return ( 1 );
		}
	}
	else if ( method == SORT_BUBBLE ) {
		Message.printWarning ( 2, routine, "Bubble sort not supported yet" );
		return 1;
	}
	else {
	    // Quick sort is default...
		Message.printWarning ( 2, routine, "Sort method " + method + " not supported.  Using quick sort" );
		if ( sortDQuick(data, sort_order, sflag) == 1 ) {
			return 1;
		}
	}

	// Now check to see if the arrays need to be reversed for descending order...

	if ( order == SORT_DESCENDING ) {
		if ( reverseArray(data ) == 1 ) {
			return 1;
		}
		if ( sflag ) {
			if ( reverseArray(sort_order ) == 1 ) {
				return 1;
			}
		}
	}

	return 0;
}

/**
Sort an array of integers.
@return Zero if successful and 1 if not successful.
@param data Array of integers to sort.
@param method Method to use for sorting (see SORT_*).
@param order Order to sort (SORT_ASCENDING or SORT_DESCENDING).
@param sort_order Original locations of data after sort (array needs to be allocated before calling routine).
@param sflag Indicates whether "sort_order" is to be filled.
*/
public static int sort (int[] data, int method, int order, int[] sort_order, boolean sflag )
{	String	routine="MathUtil.sort(int[]...)";
	int	i=0, ndata=data.length;

	if ( data == null ) {
		Message.printWarning( 2, routine, "Incoming data array is NULL, cannot sort." );
		return 1;
	}

	if ( sflag && (sort_order == null) ) {
		Message.printWarning( 2, routine, "Incoming sort_order array is NULL, cannot sort." );
		return 1;
	}

	if ( sflag && (sort_order.length < data.length) ){
		Message.printWarning( 2, routine, "sort_order array length (" + sort_order.length +
		") is smaller than data array length (" + data.length + ")." );
		return 1;
	}

	// Initialize "sort_order" to sequential numbers...
	
	if ( sflag ) {
		for ( i = 0; i < ndata; i++ ) {
			sort_order[i] = i;
		}
	}

	// Now sort into ascending order...

	if ( method == SORT_QUICK ) {
		if ( sortIQuick(data, sort_order, sflag) == 1 ) {
			return 1;
		}
	}
	else if ( method == SORT_BUBBLE ) {
		Message.printWarning ( 2, routine, "Bubble sort not supported yet" );
		return 1;
	}
	else {
	    // Quick sort is default...
		Message.printWarning ( 2, routine, "Sort method " + method + " not supported.  Using quick sort" );
		if ( sortIQuick(data, sort_order, sflag) == 1 ) {
			return 1;
		}
	}

	// Now check to see if the arrays need to be reversed for descending order...

	if ( order == SORT_DESCENDING ) {
		if ( reverseArray(data ) == 1 ) {
			return 1;
		}
		if ( sflag ) {
			if ( reverseArray(sort_order ) == 1 ) {
				return 1;
			}
		}
	}

	return 0;
}

/* ----------------------------------------------------------------------------
** sortDQuick - quick sort on a list of double
** ----------------------------------------------------------------------------
** Copyright:   See the COPYRIGHT file.
** ----------------------------------------------------------------------------
** Notes:	(1)	Default order is ascending (SORT_ASCENDING).
** ----------------------------------------------------------------------------
** History:
**
** 16 Sep 1997	MJR, RTi		Ported to Java.
** ----------------------------------------------------------------------------
** Variable	I/O	Description
**
** data		I	Data values to sort.
** ndata	I	Number of data values.
** sflag	I	Indicates whether the sort_order array should be
**			filled.
** sort_order	O	Original order of data after sort.
** ----------------------------------------------------------------------------
*/

/**
Sort an array of doubles into ascending order using the quick sort method.
@return Zero if successful and 1 if not successful.
@param data Array of doubles to sort.
@param sort_order Original locations of data after sort (array needs to be allocated before calling routine).
@param sflag Indicates whether "sort_order" is to be filled.
*/
public static int sortDQuick ( double[] data, int[] sort_order, boolean sflag )
{	int	i, ia=0, insertmax = 7, ndata=data.length, ir = ndata - 1, 
		itemp, j, jstack = 0, k, l = 0, NSTACK = 500;
	int[]	istack;
	double	a, temp;
	String	routine="MathUtil.sortDQuick";

	istack = new int [ NSTACK ];

	while ( true ) {
		if ( (ir - l) < insertmax ) {
			for ( j = (l + 1); j <= ir; j++ ) {
				a	= data[j];
				if ( sflag ) {
					ia	= sort_order[j];
				}
				for ( i = (j - 1); i >= 0; i-- ) {
					if ( data[i] <= a ) {
						break;
					}
					data[i + 1] = data[i];
					if ( sflag ) {
						sort_order[i+1]	= sort_order[i];
					}
				}
				data[i + 1] = a;
				if ( sflag ) {
					sort_order[i + 1] = ia;
				}
			}
			if ( jstack == 0 ) {
				break;
			}
			ir = istack[jstack--];
			l = istack[jstack--];
		}
		else {
		    k = (l + ir)/2;
			temp = data[k];
			data[k] = data[l + 1];
			data[l + 1]	= temp;
			if ( sflag ) {
				itemp = sort_order[k];
				sort_order[k] = sort_order[l + 1];
				sort_order[l+1] = itemp;
			}
			if ( data[l + 1] > data[ir] ) {
				temp = data[l + 1];
				data[l + 1] = data[ir];
				data[ir] = temp;
				if ( sflag ) {
					itemp = sort_order[l + 1];
					sort_order[l+1] = sort_order[ir];
					sort_order[ir] = itemp;
				}
			}
			if ( data[l] > data[ir] ) {
				temp = data[l];
				data[l] = data[ir];
				data[ir] = temp;
				if ( sflag ) {
					itemp = sort_order[l];
					sort_order[l] = sort_order[ir];
					sort_order[ir] = itemp;
				}
			}
			if ( data[l + 1] > data[l] ) {
				temp = data[l + 1];
				data[l + 1] = data[l];
				data[l] = temp;
				if ( sflag ) {
					itemp = sort_order[l + 1];
					sort_order[l+1] = sort_order[l];
					sort_order[l] = itemp;
				}
			}
			i = l + 1;
			j = ir;
			a = data[l];
			if ( sflag ) {
				ia = sort_order[l];
			}
			while ( true ) {
				do {
				    i++;
				} while ( data[i] < a );
				do {
				    j--;
				} while ( data[j] > a );
				if ( j < i ) {
					break;
				}
				temp = data[i];
				data[i] = data[j];
				data[j] = temp;
				if ( sflag ) {
					itemp = sort_order[i];
					sort_order[i] = sort_order[j];
					sort_order[j] = itemp;
				}
			}
			data[l]	= data[j];
			data[j] = a;
			if ( sflag ) {
				sort_order[l] = sort_order[j];
				sort_order[j] = ia;
			}
			jstack	+= 2;
			if ( jstack > (NSTACK - 1) ) {
				Message.printWarning ( 2, routine, "NSTACK (" + NSTACK + ") too small in sort" );
				// Free memory...
				istack = null;
				return 1;
			}
			if ( (ir - i + 1) >= (j - l) ) {
				istack[jstack] = ir;
				istack[jstack - 1] = i;
				ir = j - 1;
			}
			else {
			    istack[jstack] = j - 1;
				istack[jstack - 1] = l;
				l = i;
			}
		}
	}
	istack = null;
	return 0;
}

/* ----------------------------------------------------------------------------
** sortIQuick - quick sort on a list of integer
** ----------------------------------------------------------------------------
** Copyright:   See the COPYRIGHT file.
** ----------------------------------------------------------------------------
** Notes:	(1)	Default order is ascending (SORT_ASCENDING).
** ----------------------------------------------------------------------------
** History:
**
** 15 Apr 1998	CEN, RTi		Copied and modified sortDQuick
** ----------------------------------------------------------------------------
** Variable	I/O	Description
**
** data		I	Data values to sort.
** ndata	I	Number of data values.
** sflag	I	Indicates whether the sort_order array should be
**			filled.
** sort_order	O	Original order of data after sort.
** ----------------------------------------------------------------------------
*/

/**
Sort an array of integers into ascending order using the quick sort method.
@return Zero if successful and 1 if not successful.
@param data Array of integers to sort.
@param sort_order Original locations of data after sort (array needs to be
allocated before calling routine).
@param sflag Indicates whether "sort_order" is to be filled.
*/
public static int sortIQuick ( int[] data, int[] sort_order, boolean sflag )
{	int	i, ia=0, insertmax = 7, ndata=data.length, ir = ndata - 1, 
		itemp, j, jstack = 0, k, l = 0, NSTACK = 500;
	int[]	istack;
	int	a, temp;
	String	routine="MathUtil.sortIQuick";

	istack = new int [ NSTACK ];

	while ( true ) {
		if ( (ir - l) < insertmax ) {
			for ( j = (l + 1); j <= ir; j++ ) {
				a	= data[j];
				if ( sflag ) {
					ia	= sort_order[j];
				}
				for ( i = (j - 1); i >= 0; i-- ) {
					if ( data[i] <= a ) {
						break;
					}
					data[i + 1] = data[i];
					if ( sflag ) {
						sort_order[i+1]	= sort_order[i];
					}
				}
				data[i + 1] = a;
				if ( sflag ) {
					sort_order[i + 1] = ia;
				}
			}
			if ( jstack == 0 ) {
				break;
			}
			ir	= istack[jstack--];
			l	= istack[jstack--];
		}
		else {
		    k = (l + ir)/2;
			temp		= data[k];
			data[k]		= data[l + 1];
			data[l + 1]	= temp;
			if ( sflag ) {
				itemp		= sort_order[k];
				sort_order[k]	= sort_order[l + 1];
				sort_order[l+1]	= itemp;
			}
			if ( data[l + 1] > data[ir] ) {
				temp		= data[l + 1];
				data[l + 1]	= data[ir];
				data[ir]	= temp;
				if ( sflag ) {
					itemp		= sort_order[l + 1];
					sort_order[l+1]	= sort_order[ir];
					sort_order[ir]	= itemp;
				}
			}
			if ( data[l] > data[ir] ) {
				temp		= data[l];
				data[l]		= data[ir];
				data[ir]	= temp;
				if ( sflag ) {
					itemp		= sort_order[l];
					sort_order[l]	= sort_order[ir];
					sort_order[ir]	= itemp;
				}
			}
			if ( data[l + 1] > data[l] ) {
				temp		= data[l + 1];
				data[l + 1]	= data[l];
				data[l]		= temp;
				if ( sflag ) {
					itemp		= sort_order[l + 1];
					sort_order[l+1]	= sort_order[l];
					sort_order[l]	= itemp;
				}
			}
			i	= l + 1;
			j	= ir;
			a	= data[l];
			if ( sflag ) {
				ia	= sort_order[l];
			}
			while ( true ) {
				do {
				    i++;
				} while ( data[i] < a );
				do {
				    j--;
				} while ( data[j] > a );
				if ( j < i ) {
					break;
				}
				temp		= data[i];
				data[i]		= data[j];
				data[j]		= temp;
				if ( sflag ) {
					itemp		= sort_order[i];
					sort_order[i]	= sort_order[j];
					sort_order[j]	= itemp;
				}
			}
			data[l]	= data[j];
			data[j] = a;
			if ( sflag ) {
				sort_order[l] = sort_order[j];
				sort_order[j] = ia;
			}
			jstack	+= 2;
			if ( jstack > (NSTACK - 1) ) {
				Message.printWarning ( 2, routine, "NSTACK (" + NSTACK + ") too small in sort" );
				istack = null;
				return 1;
			}
			if ( (ir - i + 1) >= (j - l) ) {
				istack[jstack]		= ir;
				istack[jstack - 1]	= i;
				ir			= j - 1;
			}
			else {
			    istack[jstack]		= j - 1;
				istack[jstack - 1]	= l;
				l			= i;
			}
		}
	}
	istack = null;
	return 0;
}

/*------------------------------------------------------------------------------
** standardDeviation - calculate the standard deviation for a list of numbers
**------------------------------------------------------------------------------
** Copyright:   See the COPYRIGHT file.
**------------------------------------------------------------------------------
** History:
**
** 05-25-95	Steven A. Malers, RTi	Changed return status to an integer -
**					return stddev via the parameter list.
** 06 Sep 1996  Steven A. Malers, RTi   Split out of the HMStat.c file.
** 04 Jun 1998	SAM, RTi		Port to Java.
**------------------------------------------------------------------------------
** Variable	I/O	Description
**
** n		I	Number of data values.
** routine	L	Name of this routine.
** s		O	Standard deviation.
** var		L	Variance of the values.
** x		I	Data values.
**------------------------------------------------------------------------------
*/

public static double standardDeviation ( double x[] )
throws Exception
{
	try {
	    return standardDeviation ( x.length, x );
	}
	catch ( Exception e ) {
		throw e;
	}
}

public static double standardDeviation ( int n, double x[] )
throws Exception
{	String	message, routine = "MathUtil.standardDeviation";
	double	s, var;

	s = 0.0;
	try {
	    var = variance ( n, x );
	}
	catch ( Exception e ) {
		message = "Trouble calculating variance";
		Message.printWarning ( 50, routine, message );
		throw new Exception ( message );
	}
	if ( var <= 0.0 ) {
		message = "Variance (" + var + ") <= 0.  Cannot compute standard deviation.";
		Message.printWarning ( 50, routine, message );
		throw new Exception ( message );
	}
	s = Math.sqrt ( var );
	return s;
}

public static double standardDeviation ( int n, double x[], double missing )
throws Exception
{	String	message, routine = "MathUtil.standardDeviation";
	double	s, var;

	s = 0.0;
	try {
	    var = variance ( n, x, missing );
	}
	catch ( Exception e ) {
		message = "Trouble calculating variance";
		Message.printWarning ( 50, routine, message );
		throw new Exception ( message );
	}
	if ( var <= 0.0 ) {
		message = "Variance <= 0.  Cannot compute standard deviation.";
		Message.printWarning ( 50, routine, message );
		throw new Exception ( message );
	}
	s = Math.sqrt ( var );
	return s;
}

/**
Calculate the sum of a list of numbers.
@return the sum of a list of numbers, or zero if no values.
@param x Array of numbers.
*/
public static double sum ( double x[] )
{	return sum ( x.length, x );
}

/**
Calculate the sum of a list of numbers.
@return the sum of the array, or zero if no values.
@param x Array of numbers.
*/
public static double sum ( int n, double x[] )
{	double thesum = 0.0;

	for ( int i = 0; i < n; i++ ) {
		thesum += x[i];
	}
	return thesum;
}

/**
Calculate the sum of a list of numbers.
@return the sum of the array, or zero if no values.
@param x Array of numbers.
@param missing Value to consider as missing - matching values will be ignored.
*/
public static double sum ( int n, double x[], double missing )
{	double thesum = 0.0;

	for ( int i = 0; i < n; i++ ) {
		if ( x[i] != missing ) {
			thesum += x[i];
		}
	}
	return thesum;
}

/*------------------------------------------------------------------------------
** variance - calculate the sample variance for a list of numbers
**------------------------------------------------------------------------------
** History:
**
** 05-25-95	Steven A. Malers, RTi	Change so that the routine
**					returns an error code and the
**					mean is returned in the
**					parameter list.
** 04 Jun 1998	SAM, RTi		Port to Java.
**------------------------------------------------------------------------------
** Variables	I/O	Description
**
** dif		L	Difference between a data value and the mean for the
**			set.
** mean		L	Mean of the sample set (zero of there is trouble).
** message	L	String for messages.
** n		I	Number of data values.
** routine	L	Name of this routine.
** var		O	Variance of the values.
** x		I	Data values.
**------------------------------------------------------------------------------
*/

public static double variance ( double x[] )
throws Exception
{	try {
    return variance ( x.length, x );
	}
	catch ( Exception e ) {
		throw e;
	}
}

public static double variance ( int n, double x[] )
throws Exception
{	int	i;
	double	dif, meanx;
	String	message, routine = "MathUtil.variance";

	double var = 0.0;
	if ( n <= 1 ) {
		message = "Number of data values = " + n + " <= 1";
		Message.printWarning ( 50, routine, message );
		throw new Exception ( message );
	}
	try {
	    meanx = mean(n, x);
	}
	catch ( Exception e ) {
		Message.printWarning ( 50, routine, "Error calculating mean" );
		throw e;
	}
	for ( i = 0; i < n; i++ ) {
		dif = (x[i] - meanx);
		var += dif*dif;
	}
	var /= (double)(n - 1);
	return var;
}

public static double variance ( int n, double x[], double missing )
throws Exception
{	int	i;
	double	dif, meanx;
	String	message, routine = "MathUtil.variance";

	double var = 0.0;
	if ( n <= 1 ) {
		message = "Number of data values = " + n + " <= 1";
		Message.printWarning ( 50, routine, message );
		throw new Exception ( message );
	}
	try {
	    meanx = mean(n, x, missing);
	}
	catch ( Exception e ) {
		Message.printWarning ( 50, routine, "Error calculating mean" );
		throw e;
	}
	int n2 = 0;
	for ( i = 0; i < n; i++ ) {
		if ( x[i] != missing ) {
			dif = (x[i] - meanx);
			var += dif*dif;
			++n2;
		}
	}
	if ( n2 <= 1 ) {
		message = "Number of non-missing data values = " + n2 + " <= 1";
		Message.printWarning ( 50, routine, message );
		throw new Exception ( message );
	}
	var /= (double)(n2 - 1);
	return var;
}

} // End MathUtil
