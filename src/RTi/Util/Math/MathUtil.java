// MathUtil - static methods for calculating statistics.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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
import	RTi.Util.Message.Message;

import java.lang.Math;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
Static methods for calculating statistics.
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
Compute the angle on a standard circle (0=3 o'clock and from there counterclockwise), essentially
the bearing from one point to another
@param x0 x-coordinate of first point (center of circle)
@param y0 y-coordinate of first point (center of circle)
@param x1 x-coordinate of second point
@param y1 y-coordinate of second point
@return the angle in radians or NaN if the points are the same
*/
public static double angleFromPoints ( double x0, double y0, double x1, double y1 )
{
	// Find the angle at which the line leaves the center
	double xDiff = x1 - x0;
	double yDiff = y1 - y0;
	double angle = Double.NaN;
	// Do the computations going counter-clockwise from zero angle
	// Special cases first in each condition.  Note that the "opposite" and "adjacent" change depending
	// on the quadrant
	if ( (xDiff == 0.0) && (yDiff == 0.0) ) {
		angle = Double.NaN;
	}
	else if ( (yDiff == 0.0) && (xDiff > 0.0) ) {
		angle = 0.0;
	}
	else if ( yDiff > 0.0 ) {
		if ( xDiff > 0.0 ){
			// First quadrant
			angle = Math.atan(yDiff/xDiff);
		}
		else if ( xDiff == 0.0 ) {
			angle = Math.PI/2.0;
		}
		else {
			// Second quadrant - atan will be negative with angle measured counter-clockwise from vertical
			// so subtract result
			angle = Math.PI/2.0 - Math.atan(xDiff/yDiff);
		}
	}
	else if ( (yDiff == 0.0) && (xDiff < 0.0) ) {
		angle = Math.PI;
	}
	else if ( yDiff < 0.0 ) {
		if ( xDiff < 0.0 ){
			// Third quadrant - atan will be positive when measured counter-clockwise from horizontal left
			// so add result
			angle = Math.PI + Math.atan(yDiff/xDiff);
		}
		else if ( xDiff == 0.0 ) {
			angle = Math.PI*1.5;
		}
		else {
			// Fourth quadrant - atan will be negative when measured counter-clockwise from vertical down
			// so subtract result
			angle = Math.PI*1.5 - Math.atan(xDiff/yDiff);
		}
	}
	//Message.printStatus ( 2, "", "xdiff=" + xDiff + " ydiff=" + yDiff + " angle=" + Math.toDegrees(angle) );
	return angle;
}

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
	List<Integer> common_denominators = new Vector<Integer>();
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
Calculate the exceedance probability.  Values are sorted in descending order (position 1 is largest value and
smallest exceedance probability).
@return The exceedance probability given the sample and one of the values in the sample.
@param n the number of values in the sample array to process
@param x sample values
@param xi specific value for which to compute the exceedance probability (must be in the range of values in x array)
@exception IllegalArgumentException If the number of points is <= 0 or xi is outside the range of the data array
*/
public static double exceedanceProbability ( int n, double x[], double xi )
{   String routine = "MathUtil.exceedanceProbability";

    if ( n <= 0 ) {
        String message = "Number of points <= 0";
        Message.printWarning ( 10, routine, message );
        throw new IllegalArgumentException ( message );
    }
    // First sort the numbers into descending order (largest value in data array will be first)...
    double [] x2 = new double[x.length];
    System.arraycopy(x, 0, x2, 0, n);
    sort(x2, SORT_QUICK, SORT_DESCENDING, null, false);
    
    // Find the first matching value and return the exceedance probability using the plotting position
    // = rank/(n + 1), where largest value = position 1
    for ( int i = 0; i < n; i++ ) {
        if ( x2[i] == xi ) {
            // Value is in sample so return the plotting position
            return (i + 1)/(double)(n + 1);
        }
        else if ( (xi > x2[0]) || (xi < x2[n - 1]) ) {
            // Don't want to guess about exceedance probability and doing a linear interpolation may give
            // probability < 0 or > 1 so just don't handle this case
            String message =
                "Value (" + xi + ") is outside sample range (" + x2[n - 1] + " to " + x2[0] +
                ") - not extrapolating exceedance probability";
            Message.printWarning ( 10, routine, message );
            throw new IllegalArgumentException ( message );
        }
        else if ( xi > x2[i] ) {
            // Value is between sample values so interpolate the plotting position of the bounding values.
            // Often the value passed in will be the same as one of the array values but off by a very
            // small amount so this interpolation introduces minor error.
            double epHigh = i/(double)(n + 1); // For previous value (i + 1 - 1 = i)
            double epLow = (i + 1)/(double)(n + 1); // For current value
            return interpolate(xi, x2[i - 1], x2[i], epHigh, epLow);
        }
    }
    String message = "Requested value " + xi + " does not match a value in the array";
    Message.printWarning ( 10, routine, message );
    throw new IllegalArgumentException ( message );
}

/**
@return The sample value corresponding to the given exceedance probability.
@param n the number of values in the sample array to process
@param x sample values
@param probability the exceedance probability to consider (0.0 to 1.0)
@exception IllegalArgumentException If the number of points is <= 0
*/
public static double exceedanceProbabilityValue ( int n, double x[], double probability )
{   String routine = "MathUtil.exceedanceProbabilityValue";
    double value = 0;

    if ( n <= 0 ) {
        String message = "Number of points <= 0";
        Message.printWarning ( 10, routine, message );
        throw new IllegalArgumentException ( message );
    }
    // First sort the numbers into ascending order...
    double [] x2 = new double[x.length];
    System.arraycopy(x, 0, x2, 0, n);
    sort(x2, SORT_QUICK, SORT_ASCENDING, null, false);
    
    // Calculate the distribution information
    double [] eProb = new double[n];
    int n1 = n + 1;
    for ( int i = 0; i < n; i++ ) {
        if ( n == 1 ) {
            eProb[i] = 1.0;
        }
        else {
            eProb[i] = (n - i)/(double)n1;
        }
    }
    
    // Calculate the value for the requested probability.  Do so by going
    // past the value and then interpolating back to the value using
    // bracketing points.  If the requested probability is outside the
    // range of the data use the appropriate endpoint (this is conservative).
    // The data values are in ascending order, so the exceedance probabilities
    // will be in descending order.
    // Do not extrapolate past the ends of the data (will be conservative
    // on the high data end and inaccurate on the low data end).
    int n0  = n - 1;
    for ( int i = 0; i < n; i++ ) {
        if ( eProb[i] < probability ) {
            if ( i == 0 ) {
                value = x2[0];
            }
            else {
                value = interpolate(probability, eProb[i - 1], eProb[i], x2[i - 1], x2[i] );
            }
            break;
        }
    }
    if ( probability < eProb[n0] ) {
        value = x2[n0];
    }
    return value;
}

/**
Calculate the exp() of each value in the array, returning a new array with the exp() values.
@return an array of exp() values, calculated from the original array
@param x array of values to transform
*/
public static double[] exp ( double [] x )
{
    double [] xt = new double[x.length];
    for ( int i = 0; i < x.length; i++ ) {
        x[i] = Math.exp(x[i]);
    }
    return xt;
}

/**
Compute the geometric mean of an array of values.
@return the geometric mean of the array of values
@param n the number of values from the array to process
@param x array of values
@exception IllegalArgumentException if the number of points is <= 0.
*/
public static double geometricMean ( int n, double x[] )
{   String message, routine = "MathUtil.geometricMean";

    if ( n <= 0 ) {
        message = "Number of values <= 0";
        Message.printWarning ( 10, routine, message );
        throw new IllegalArgumentException ( message );
    }
    double sum = 1.0;
    for ( int i = 0; i < n; i++ ) {
        if ( x[i] <= 0.0 ) {
            message = "x["+i+"]=" + x[i] + " is <= 0";
            Message.printWarning ( 10, routine, message );
            throw new IllegalArgumentException ( message );
        }
        else {
            sum = sum*x[i];
        }
    }
    return ( Math.pow(sum, 1.0/(double)n) );
}

/**
Perform integration for a function.
@param method Method used to integrate.  See INTEGRATE_*.
@param func Function to integrate (using Function interface).
@param a0 Left end point for entire interval.
@param b0 Right end point for entire interval.
@param nseg Number of segments to divide interval (use higher number for functions that are not smooth).
*/
public static double integrate ( int method, Function func, double a0, double b0, int nseg )
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
		xm = 0.5*(b + a);
		xr = 0.5*(b - a);
		s = 0.0;

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
{
	if ( (xmax - xmin) == 0.0 ) {
		return ymin;
	}
	else {
	    return ( ymin + (ymax - ymin)*(x - xmin)/(xmax - xmin) );
	}
}

/**
Calculate the lag-k auto correlation, defined as:
<pre>
(sum_i_N-k(Xi - Xmean)(Xi+k - Xmean))/(sum_i_N(Xi - Xmean)^2)
</pre>
@param n number of values at start of array to use for sample size
@param y sample set
@param k lag (a lag of zero should return a autocorrelation of 1
@return lag-k auto correlation value
*/
public static double lagAutoCorrelation ( int n, double y[], int k )
{   //String message, routine = "MathUtil.lagAutoCorrelation";

    double mean = mean ( n, y );
    double numeratorSum = 0.0;
    int iend = n - k;
    for ( int i = 0; i < iend; i++ ) {
        numeratorSum += ((y[i] - mean)*(y[i+k] - mean));
    }
    double denominatorSum = 0.0;
    for ( int i = 0; i < n; i++ ) {
        denominatorSum += Math.pow((y[i] - mean),2.0);
    }
    double rk = numeratorSum/denominatorSum;
    return rk;
}

/**
Calculate the log10 of each value in the array, returning a new array with the log10 values.
@return an array of log10 values, calculated from the original array
@param x array of values to transform
@param leZeroLog10 the value to assign when the input data are <= 0 (e.g., .001 or Double.NaN).
*/
public static double[] log10 ( double [] x, double leZeroLog10 )
{
    double [] xt = new double[x.length];
    for ( int i = 0; i < x.length; i++ ) {
        if ( x[i] <= 0.0 ) {
            xt[i] = leZeroLog10;
        }
        else {
            xt[i] = Math.log10(x[i]);
        }
    }
    return xt;
}

/**
Find the maximum of two values.
@return the maximum value.
@param x First value to check.
@param y Second value to check.
*/
public static double max ( double x, double y )
{
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
{
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
*/
public static double max ( double x[] )
{
    return max ( x.length, x );
}

/**
@return The maximum value in an array.
@param n number of values in array to process
@param x array of values
@exception IllegalArgumentException If the number of points is <= 0.
*/
public static double max ( int n, double x[] )
{	int	i;
	String routine = "MathUtil.max";
	double m = 0.0;

	if ( n <= 0 ) {
		String message = "Number of points <= 0";
		Message.printWarning ( 10, routine, message );
		throw new IllegalArgumentException ( message );
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
Find the maximum of two integer values
@return the maximum value
@param x one integer value
@param y another integer value
*/
public static int max ( int x, int y )
{
	if ( x > y ) {
	    return x;
	}
	else {
	    return y;
	}
}

/**
@return The maximum value in an array.
@param x Array of values.
*/
public static int max ( int x[] )
{
    return max ( x.length, x );
}

/**
@return The maximum value in an array.
@param n number of values in array to compare
@param x array of values
@exception IllegalArgumentException if the number of points is <= 0.
*/
public static int max ( int n, int x[] )
{	int	i;
	String routine = "MathUtil.max";
	int	m = 0;

	if ( n <= 0 ) {
		String message = "Number of points <= 0";
		Message.printWarning ( 10, routine, message );
		throw new IllegalArgumentException ( message );
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
Compute the mean of an array of values.
@return The mean of the array of values
@param x array of values
*/
public static double mean ( double x[] )
{
    return mean ( x.length, x );
}

/**
Compute the mean of an array of values.
@return The mean of the array of values
@param n the number of values from the array to process
@param x array of values
@exception IllegalArgumentException if the number of points is <= 0.
*/
public static double mean ( int n, double x[] )
{	String message, routine = "MathUtil.mean";
	double thesum;

	if ( n <= 0 ) {
		message = "Number of values <= 0";
		Message.printWarning ( 10, routine, message );
		throw new IllegalArgumentException ( message );
	}
    thesum = sum ( n, x );
	return ( thesum/(double)(n) );
}

/**
Compute the mean of an array of values.
@return The mean value in an array.
@param n the number of values from the array to process
@param x array of values
@param missing Missing data value (to ignore)
@exception IllegalArgumentException if the number of points is <= 0.
*/
public static double mean ( int n, double x[], double missing )
{	String message, routine = "MathUtil.mean";
	double thesum = 0.0;

	if ( n <= 0 ) {
		message = "Error computing mean - the number of points <= 0.";
		Message.printWarning ( 10, routine, message );
		throw new IllegalArgumentException ( message );
	}
	int n2 = 0;
    for ( int i = 0; i < n; i++ ) {
		if ( x[i] != missing ) { 
			thesum += x[i];
			++n2;
		}
	}
	if ( n2 <= 0 ) {
		message = "Error computing mean - the number of non-missing points <= 0.";
		Message.printWarning ( 10, routine, message );
		throw new IllegalArgumentException ( message );
	}
	return ( thesum/(double)(n2) );
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
Find the minimum of two values.
@return minimum of two values.
@param x First value to check.
@param y Second value to check.
*/
public static double min ( double x, double y )
{
	if ( x < y ) {
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
{
    return min ( x.length, x );
}

/**
@return The minimum value in an array.
@param n the number of values in the array to compare
@param x array of values
@exception IllegalArgumentException if the number of points is <= 0.
*/
public static double min ( int n, double x[] )
{	int	i;
	String routine = "MathUtil.min";
	double m = 0.0;

	if ( n <= 0 ) {
		String message = "Number of points <= 0.0";
		Message.printWarning ( 10, routine, message );
		throw new IllegalArgumentException ( message );
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
Find the minimum of two integer values
@return the minimum value
@param x one integer value
@param y another integer value
*/
public static int min ( int x, int y )
{
    if ( x < y ) {
        return x;
    }
    else {
        return y;
    }
}

/**
Find the minimum value in an array
@return the minimum value
@param x array of integer values to check
*/
public static int min ( int x[] )
{
    return min ( x.length, x );
}

/**
@return The minimum value in an array.
@param n number of values in array to check
@param x array of values
@exception IllegalArgumentException if the number of points is <= 0.
*/
public static int min ( int n, int x[] )
{	int	i;
	String routine = "MathUtil.min";
	int	m = 0;

	if ( n <= 0 ) {
		String message = "Number of points <= 0";
		Message.printWarning ( 10, routine, message );
		throw new IllegalArgumentException ( message );
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
@return The nonexceedance probability given the sample and one of the values in the sample.
Values are sorted in descending order (position 1 is largest value and largest nonexceedance probability)
@param n the number of values in the sample array to process
@param x sample values
@param xi specific value for which to compute the nonexceedance probability
(must be within the range of the values in x array)
@exception IllegalArgumentException If the number of points is <= 0 or xi is outside the range of the data array
*/
public static double nonexceedanceProbability ( int n, double x[], double xi )
{   
    return 1.0 - exceedanceProbability(n, x, xi);
}

/**
@return the sample value corresponding to the given nonexceedance probability.
@param n the number of values in the sample array to process
@param x sample values
@param neprobability the nonexceedance probability to consider (0.0 to 1.0)
@exception IllegalArgumentException If the number of points is <= 0
*/
public static double nonexceedanceProbabilityValue ( int n, double x[], double neprobability )
{
	return exceedanceProbabilityValue ( n, x, (1.0 - neprobability) );
}

/**
Perform an ordinary least squares regression on data that has already been transformed and processed for
missing data.  A minimal number of analysis statistics and results are saved and
can be retrieved using RegressionResults methods, in particular:
<ol>
</ol>
@return a RegressionResults object for the analysis.
@param data regression data, from which the following are extracted for the analysis:<br>
x1 Array of non-missing independent (X) values that overlap y1.<br>
y1 Array of non-missing dependent (Y) values that overlap x1.
@param forcedIntercept the intercept to force (currently the intercept must be zero if specified) or null to not force intercept.
@exception IllegalArgumentException if no data are available to be regressed (e.g.,
all missing), or data array lengths are unequal.
*/
public static RegressionResults ordinaryLeastSquaresRegression ( RegressionData data, Double forcedIntercept )
{   String rtn = "ordinaryLeastSquaresRegression";
    double [] x1 = data.getX1();
    double [] y1 = data.getY1();
    if ( x1.length != y1.length ) {
        String message = "Lengths of arrays are unequal.";
        Message.printWarning ( 3, rtn, message );
        throw new IllegalArgumentException( message );
    }

    int n1 = x1.length;
    if ( n1 == 0 ) {
        String message = "No data to create regression analysis.";
        Message.printWarning ( 3, rtn, message );
        throw new IllegalArgumentException ( message );
    }

    double totalX1Y1 = 0.0;
    double totalX1 = 0.0;
    double totalX1_sq = 0.0;
    double totalY1 = 0.0;
    double totalY1_sq = 0.0;
    Double a = null;
    Double b = null;
    Double r = null;
    
    for ( int i = 0; i < n1; i++ ) {
        totalX1Y1 += (x1[i] * y1[i]);
        totalX1 += x1[i];
        totalX1_sq += (x1[i]*x1[i]);
        totalY1 += y1[i];
        totalY1_sq += (y1[i] * y1[i]);
        if ( Message.isDebugOn ) {
            Message.printDebug ( 50, rtn, "X1: " + x1[i] + ", Y1: " + y1[i] );
        }
    }
    
    // TODO SAM 2012-12-15 Logic was kept migrating legacy code and consequently
    // the math does not simply use standard deviation, etc.  This adds a little
    // inefficiency computing statistics below.  For now leave as is.

    // correlationCoeff, R ...
    //
    // The following is computed by...
    //
    // R = sum(xy) - [sum(x)*sum(y)]/n
    //     ---------------------------------------------------------
    //  sqrt(sum(x^2) - [sum(x)^2]/n) * sqrt(sum(y^2) - [sum(y)^2]/n)
    //
    // or
    //
    // R = N*sum(xy) - [sum(x)*sum(y)]
    //     ---------------------------------------------------------
    //  sqrt(N*sum(x^2) - sum(x)^2) * sqrt(N*sum(y^2) - sum(y)^2)
    //
    // or
    //
    // R = N*sum(xy) - [sum(x)*sum(y)]
    //     ---------------------------------------------------------
    //  sqrt([N*sum(x^2) - sum(x)^2] * [N*sum(y^2) - sum(y)^2])

    double denom = Math.sqrt ((((double)n1 * totalX1_sq) -
        (totalX1*totalX1)) * (((double)n1 * totalY1_sq) - (totalY1*totalY1)));
        
    if ( denom > 0 ) {
        r = (((double)n1 * totalX1Y1) - (totalX1 * totalY1))/denom;
    }
    else {
        r = null;
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
    //or, r*StDev(y)/StDev(x)
    //
    // If the intercept is specified as zero:
    //
    // b = sum(xy)/sum(x^2)

    if ( forcedIntercept != null ) {
        if ( totalX1_sq == 0.0 ) {
            String message = "Using forced intercept and totalX1_sq is zero.  Setting b=null";
            Message.printWarning ( 3, rtn, message );
            b = null;
        }
        else {
            b = totalX1Y1/totalX1_sq;
        }
    }
    else {
    	try {
    		b = r*data.getStandardDeviationY1()/data.getStandardDeviationX1();
    	}
    	catch (NullPointerException e) {
    		//standard deviation could not be calculated, set b to null
    		String message = "Standard deviation of x ("+data.getStandardDeviationY1()+") or y ("+
    			data.getStandardDeviationX1()+") could not be calculated, setting b=null";
    		Message.printWarning ( 3, rtn, message );
    		b = null;
    	}
    }

    // If the intercept is not specified:
    //
    // a = sum(y)/n - b*sum(x)/n
    //
    // If the intercept is specified as zero:
    //
    // a = 0.0

    if ( forcedIntercept != null) {
        if ( forcedIntercept != 0.0 ) {
            throw new IllegalArgumentException ( "Only zero intercept is supported (specified as " + forcedIntercept + ")." );
        }
        a = forcedIntercept;
    }
    else {
        // Compute...
        if ( b == null ) {
            a = null;
        }
        else {
        	a = data.getMeanY1() - b*data.getMeanX1();
        }
    }

    if ( Message.isDebugOn ) {
        Message.printDebug ( 10, rtn, "Regression analysis results: " +
            "a: " + a + ", " + "b: " + b + ", " + "R: " + r );
        Message.printStatus ( 2, rtn,
            "Regression analysis results: n=" + n1 + ", a=" + a + ", b=" + b + ", R=" + r );
    }

    // Save in Regression object (null are OK because they will be checked later)...

    return ( new RegressionResults ( data, forcedIntercept, a, b, r ) );
}

public static PrincipalComponentAnalysis performPrincipalComponentAnalysis (
    double[] dependentArray, double[][] independentMatrix,
    double missingDependentValue, double missingIndependentValue, int maximumCombinations )
throws Exception
{
    PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis(
        dependentArray, independentMatrix, missingDependentValue, missingIndependentValue, maximumCombinations );
    return pca;
}

/**
Calculate the plotting position for a distribution.  Values are sorted in the specified order prior to determining the rank.
Plotting position is then calculated from the rank based on the equation for the distribution.
If the value is tied with others, the average of the ranks for the tied values is used to compute the plotting position.
@return The plotting position (0 to 1) given the sample and one of the values in the sample.
@param n the number of values in the sample array to process
@param x sample values
@param xi specific value for which to compute the rank (must match one of the values in x array)
@exception IllegalArgumentException If the number of points is <= 0 or xi does not exactly match a value in the data array
*/
public static double plottingPosition ( int n, double x[], SortOrderType sortOrderType, DistributionType distributionType,
    Hashtable<String,String> distributionParameters, double xi )
{   String routine = "MathUtil.plottingPosition";

    if ( n <= 0 ) {
        String message = "Number of points <= 0";
        Message.printWarning ( 10, routine, message );
        throw new IllegalArgumentException ( message );
    }
    // First determine the rank
    double rank = rank(n, x, sortOrderType, xi);
    // The plotting position is calculated based on the distribution
    if ( distributionType == DistributionType.WEIBULL ) {
        return rank/(n + 1.0);
    }
    else if ( distributionType == DistributionType.GRINGORTEN ) {
        // TODO SAM 2013-12-09 need to extract the parameter and have a distribution object so the
        // following does not have to be repeated every time this method is called
        // Expect "a" to be in parameters
        Object o = distributionParameters.get("a");
        double a;
        if ( o == null ) {
            String message = "Expected distribution parameter \"a\" was not specified.";
            Message.printWarning ( 10, routine, message );
            throw new IllegalArgumentException ( message );
        }
        else {
            if ( o instanceof String ) {
                a = Double.parseDouble((String)o);
            }
            else if ( o instanceof Double ) {
                a = (Double)o;
            }
            else {
                String message = "Unable to determine distribution parameter \"a\" from object " + o + ".";
                Message.printWarning ( 10, routine, message );
                throw new IllegalArgumentException ( message );
            }
        }
        return (rank - a)/(n + 1.0 - 2.0*a);
    }
    else {
        String message = "Do not know how to calculate plotting position for distribution " + distributionType;
        Message.printWarning ( 10, routine, message );
        throw new IllegalArgumentException ( message );
    }
}

/**
Calculate the rank.  Values are sorted in the specified order prior to determining the rank.
If the value is tied with others, the average of the ranks for the tied values is returned.
@return The rank value (1+) given the sample and one of the values in the sample.
@param n the number of values in the sample array to process
@param x sample values
@param sortOrderType the sort order to sort the data array before computing the rank.
@param xi specific value for which to compute the rank (must match one of the values in x array)
@exception IllegalArgumentException If the number of points is <= 0 or xi does not exactly match a value in the data array
*/
public static double rank ( int n, double x[], SortOrderType sortOrderType, double xi )
{   String routine = "MathUtil.rank";

    if ( n <= 0 ) {
        String message = "Number of points <= 0";
        Message.printWarning ( 10, routine, message );
        throw new IllegalArgumentException ( message );
    }
    // First sort the numbers into descending order (largest value in data array will be first)...
    double [] x2 = new double[x.length];
    System.arraycopy(x, 0, x2, 0, n);
    if ( (sortOrderType == null) || (sortOrderType == SortOrderType.HIGH_TO_LOW) ) {
        sort(x2, SORT_QUICK, SORT_DESCENDING, null, false);
    }
    else {
        sort(x2, SORT_QUICK, SORT_ASCENDING, null, false);
    }
    
    // Find the first matching value and return the position
    for ( int i = 0; i < n; i++ ) {
        if ( x2[i] == xi ) {
            // Value is in sample - search adjoining values to see if there are additional matches and if so
            // return the average of the ranks
            int count = 1;
            double rank = (double)(i + 1);
            for ( int j = (i + 1); j < n; j++ ) {
                if ( x2[j] == xi ) {
                    rank += (double)(j + 1);
                    ++count;
                }
            }
            return rank/count;
        }
    }
    String message = "Requested value " + xi + " does not match a value in the array";
    Message.printWarning ( 10, routine, message );
    throw new IllegalArgumentException ( message );
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
@param intercept the intercept to force (currently the intercept must be zero) or null to not force intercept.
*/
public static Regression regress ( double [] xArray, double [] yArray, boolean useMissing, double missingx,
	double missingy, Double intercept )
{	return regress (xArray, yArray, useMissing, missingx, missingy, false, intercept );
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
@param intercept the intercept to force (currently the intercept must be zero) or null to not force intercept.
@exception IllegalArgumentException If no data are available to be regressed (e.g.,
all missing), or data array lengths are unequal.
*/
private static Regression regress (	double [] xArray, double [] yArray, boolean useMissing, double missingx,
	double missingy, boolean data_transformed, Double intercept )
{	String rtn = "MathUtil.regress";
	if ( xArray.length != yArray.length ) {
		String message = "Lengths of arrays are unequal.";
		Message.printWarning ( 2, rtn, message );
		throw new IllegalArgumentException ( message );
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

	double maxY1 = missingy, maxX1 = missingx, minY1 = missingy, minX1 = missingx;
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
		throw new IllegalArgumentException ( message );
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
	// R = sum(xy) - [sum(x)*sum(y)]/n
	//     ---------------------------------------------------------
	//	sqrt(sum(x^2) - [sum(x)^2]/n) * sqrt(sum(y^2) - [sum(y)^2]/n)
	//
	// or
	//
	// R = N*sum(xy) - [sum(x)*sum(y)]
	//     ---------------------------------------------------------
	//	sqrt(N*sum(x^2) - sum(x)^2) * sqrt(N*sum(y^2) - sum(y)^2)
	//
	// or
	//
	// R = N*sum(xy) - [sum(x)*sum(y)]
	//     ---------------------------------------------------------
	//	sqrt([N*sum(x^2) - sum(x)^2] * [N*sum(y^2) - sum(y)^2])

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

	if ( intercept != null ) {
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

	if ( intercept != null) {
		if ( intercept.doubleValue() != 0.0 ) {
			throw new IllegalArgumentException ( "Only zero intercept is supported (specified as " + intercept + ")." );
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
		rd.setRMSETransformed ( rmse );
	}
	else {
	    rd.setRMSE ( rmse );
	}
	rd.setIntercept ( intercept );
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
@exception IllegalArgumentException if the length of the two arrays differ.
*/
public static Regression regressLog ( double [] xArray, double [] yArray, 
	boolean useMissing, double missingx, double missingy )
throws Exception
{	String rtn = "MathUtil.regressLog";
	if ( xArray.length != yArray.length ) {
		String message = "Lengths of arrays are unequal.";
		Message.printWarning ( 2, rtn, message );
		throw new IllegalArgumentException ( message );
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
				logArrayX[i] = Math.log10(xArray[i]);
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
				logArrayY[i] = Math.log10(yArray[i]);
			}
			else {
			    logArrayY[i] = .001;
			}
		}
	}

	// Do the regression on the transformed data.  The RMSE will be saved in the transformed data member...

	Regression rd = regress ( logArrayX, logArrayY, useMissing, missingx, missingy, true, null );
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
	return rd;
}

/**
Reverse an array of doubles.
@return Zero if successful and 1 if not successful.
@param data An array of doubles to be reversed.
*/
public static int reverseArray(	double[] data )
{	int	i, j, half, ndata = data.length;
	double tempf;
	String routine="MathUtil.reverseArray";

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
Reverse an array of longs.
@return Zero if successful and 1 if not successful.
@param data An array of longs to be reversed.
*/
public static int reverseArray( long[] data )
{   int i, j, half, ndata = data.length;
    long tempi;
    String routine="MathUtil.reverseArray";

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

        data[i] = data[j];
        if ( Message.isDebugOn ) {
            Message.printDebug ( 50, routine, "Moving tempi (" +tempi+ ") to data[" +j+ "]." );
        }
        data[j] = tempi;
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
ignore data.  If true, then having missing data in either array causes both array values to be ignored.
@param xmissing Missing data value for x.
@param ymissing Missing data value for y.
@return the RMS error for the data set.  Return 0 if the number of points considered is 0.
*/
public static double RMSError ( int n, double x[], double y[],
	boolean use_missing, double xmissing, double ymissing )
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

// TODO SAM 2012-01-11 The description for the method is not clear.
/**
Round a floating point value to percent.
@param x raw data value
@param interval allowable interval for output (e.g., 1.0 for 1%).
@param mflag fag indicating whether maximum value is 1.0 (0) or 100.0 (1).
@param rflag flag indicating whether value should be rounded up (1), down (-1) or to nearest (0).
*/
public static double roundToPercent ( double x, double interval, int mflag, int rflag )
{	String message, routine = "MathUtil.roundToPercent";
	double fact;
	double interval100; // Interval with 100.0 as the maximum value.
	double xhigh; // High even interval value.
	double xhighdif; // Difference between "xhigh" and "xs".
	double xlow; // Low even interval value.
	double xlowdif; // Difference between "xlow" and "xs".
	double xr; // Rounded value, returned.
	double xs; // Data in units of "interval100".

	xr = 0.0;
	// Figure out factors, etc. to convert everything to 100.0 scale...
	if ( mflag > 0 ) {
		if ( (x < 0.0) || (x > 1.0) ) {
			message = "" + x + " cannot be rounded because it is not > 0.0, < 1.0";
			Message.printWarning ( 3, routine, message );
			throw new IllegalArgumentException ( message );
		}
		fact = 1.0; // Factor to convert all data to 100.0 maximum scale.
		interval100	= interval;
		xs = x;
		
	}
	else {
	    if ( (x < 0.0) || (x > 100.0) ) {
			message = "" + x + " cannot be rounded because it is not > 0.0, < 100.0";
			Message.printWarning ( 3, routine, message );
			throw new IllegalArgumentException ( message );
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
Calculate the coefficient of skew, defined as:
<pre>
Cs = (n*Sum_n(xi - x_mean)^3)/(n - 1)(n - 2)s^3

where s = sample standard deviation

See Applied Hydrology, Chow, et. al.
</pre>
@param n number of values at start of array to use for sample size
@param x sample set
@return skew coefficient
*/
public static double skew ( int n, double x[] )
{   String message, routine = "MathUtil.skew";

    if ( n < 3 ) {
        message = "Number in sample (" + n + ") must be >= 3.  Cannot compute skew.";
        Message.printWarning ( 3, routine, message );
        throw new IllegalArgumentException ( message );
    }
    // Sample standard deviation
    double stddev = standardDeviation ( n, x );
    if ( stddev == 0.0 ) {
        message = "Standard dev = 0.  Cannot compute skew.";
        Message.printWarning ( 3, routine, message );
        throw new IllegalArgumentException ( message );
    }
    double mean = mean ( n, x );
    double numeratorSum = 0.0;
    for ( int i = 0; i < n; i++ ) {
        numeratorSum += Math.pow((x[i] - mean),3.0);
    }
    double skew = (n*numeratorSum)/((n - 1)*(n - 2)*Math.pow(stddev,3.0));
    return skew;
}

/**
Sort an array of doubles.
@return Zero if successful and 1 if not successful.
@param data Array of doubles to sort.
@param method Method to use for sorting (see SORT_*).
@param order Order to sort (SORT_ASCENDING or SORT_DESCENDING).
@param sort_order Original locations of data after sort (array needs to be
allocated to the same size as the data array before calling routine).
For example, first sort double-data and then
sort associated data by using new_other_data[i] = old_other_data[sort_order[i]];
Can be null if sflag is false.
@param sflag Indicates whether "sort_order" is to be filled.
*/
public static int sort(double[] data, int method, int order, int[] sort_order, boolean sflag )
{	String routine="MathUtil.sort(double[]...)";
	int	i=0, ndata=data.length;

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
{	String routine="MathUtil.sort(int[]...)";
	int	i=0, ndata=data.length;

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

/**
Sort an array of longs.
@return Zero if successful and 1 if not successful.
@param data Array of longs to sort.
@param method Method to use for sorting (see SORT_*).
@param order Order to sort (SORT_ASCENDING or SORT_DESCENDING).
@param sort_order Original locations of data after sort (array needs to be allocated before calling routine).
@param sflag Indicates whether "sort_order" is to be filled.
*/
public static int sort (long[] data, int method, int order, int[] sort_order, boolean sflag )
{   String routine="MathUtil.sort(int[]...)";
    int i=0, ndata=data.length;

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
        if ( sortLQuick(data, sort_order, sflag) == 1 ) {
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
        if ( sortLQuick(data, sort_order, sflag) == 1 ) {
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
Sort an array of doubles into ascending order using the quick sort method.
@return Zero if successful and 1 if not successful.
@param data Array of doubles to sort.
@param sort_order Original locations of data after sort (array needs to be allocated before calling routine).
@param sflag Indicates whether "sort_order" is to be filled.
*/
public static int sortDQuick ( double[] data, int[] sort_order, boolean sflag )
{	int	i, ia=0, insertmax = 7, ndata=data.length, ir = ndata - 1, 
		itemp, j, jstack = 0, k, l = 0, NSTACK = 500;
	int[] istack;
	double a, temp;
	String routine="MathUtil.sortDQuick";

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
			jstack += 2;
			if ( jstack > (NSTACK - 1) ) {
				Message.printWarning ( 2, routine, "NSTACK (" + NSTACK + ") too small in sort" );
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
	return 0;
}

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
				a = data[j];
				if ( sflag ) {
					ia = sort_order[j];
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
			jstack += 2;
			if ( jstack > (NSTACK - 1) ) {
				Message.printWarning ( 2, routine, "NSTACK (" + NSTACK + ") too small in sort" );
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
	return 0;
}

/**
Sort an array of longs into ascending order using the quick sort method.
@return Zero if successful and 1 if not successful.
@param data Array of longs to sort.
@param sort_order Original locations of data after sort (array needs to be
allocated before calling routine).
@param sflag Indicates whether "sort_order" is to be filled.
*/
public static int sortLQuick ( long[] data, int[] sort_order, boolean sflag )
{   int i, ia=0, insertmax = 7, ndata=data.length, ir = ndata - 1, 
        itemp, j, jstack = 0, k, l = 0, NSTACK = 500;
    int[] istack;
    long a, temp;
    String routine="MathUtil.sortLQuick";

    istack = new int [ NSTACK ];

    while ( true ) {
        if ( (ir - l) < insertmax ) {
            for ( j = (l + 1); j <= ir; j++ ) {
                a = data[j];
                if ( sflag ) {
                    ia = sort_order[j];
                }
                for ( i = (j - 1); i >= 0; i-- ) {
                    if ( data[i] <= a ) {
                        break;
                    }
                    data[i + 1] = data[i];
                    if ( sflag ) {
                        sort_order[i+1] = sort_order[i];
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
            data[l + 1] = temp;
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
            data[l] = data[j];
            data[j] = a;
            if ( sflag ) {
                sort_order[l] = sort_order[j];
                sort_order[j] = ia;
            }
            jstack += 2;
            if ( jstack > (NSTACK - 1) ) {
                Message.printWarning ( 2, routine, "NSTACK (" + NSTACK + ") too small in sort" );
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
    return 0;
}

/**
Compute the sample standard deviation (square root of the sample variance).
@param x array of values to process
@return the sample standard deviation
*/
public static double standardDeviation ( double x[] )
{
    return standardDeviation ( x.length, x );
}

/**
Compute the sample standard deviation (square root of the sample variance).
@param n number of values in array to process
@param x array of values to process
@return the sample standard deviation
@throws IllegalArgumentException if the number of values is < 2 or variance is zero
*/
public static double standardDeviation ( int n, double x[] )
{	String message, routine = "MathUtil.standardDeviation";
	double var;

	try {
	    var = variance ( n, x );
	}
	catch ( Exception e ) {
		message = "Error calculating variance - cannot calculate standard deviation.";
		Message.printWarning ( 50, routine, message );
		throw new IllegalArgumentException ( message );
	}
	if ( var <= 0.0 ) {
		message = "Variance (" + var + ") <= 0.  Cannot calculate standard deviation.";
		Message.printWarning ( 50, routine, message );
		throw new IllegalArgumentException ( message );
	}
	return Math.sqrt ( var );
}

/**
Compute the sample standard deviation (square root of the sample variance).
@param n number of values in array to process
@param x array of values to process
@param missing the value to use for missing data (they will be ignored)
@return the sample standard deviation
@throws IllegalArgumentException if the number of values is < 2 or variance is zero
*/
public static double standardDeviation ( int n, double x[], double missing )
{	String message, routine = "MathUtil.standardDeviation";
	double var;

	try {
	    var = variance ( n, x, missing );
	}
	catch ( Exception e ) {
		message = "Trouble calculating variance";
		Message.printWarning ( 50, routine, message );
		throw new IllegalArgumentException ( message );
	}
	if ( var <= 0.0 ) {
		message = "Variance <= 0.  Cannot compute standard deviation.";
		Message.printWarning ( 50, routine, message );
		throw new IllegalArgumentException ( message );
	}
	return Math.sqrt ( var );
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
@param n number of values to use from array
@param x array of numbers
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
@param n number of values to use from array
@param x array of numbers
@param missing value to consider as missing - matching values will be ignored
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

/**
Compute the sample variance (sum(x_i - mean(x))^2)/(n - 1).
@return the sample variance
@param x the sample array
*/
public static double variance ( double x[] )
throws Exception
{	return variance ( x.length, x );
}

/**
Compute the sample variance (sum(x_i - mean(x))^2)/(n - 1).
@return the sample variance
@param n the number of values to use from the array
@param x the sample array
@exception IllegalArgumentException if n is < 2, which would result in division by zero.
*/
public static double variance ( int n, double x[] )
throws Exception
{	int	i;
	double dif, meanx;
	String routine = "MathUtil.variance";

	double var = 0.0;
	if ( n <= 1 ) {
		String message = "Error calculating sample variance - the number of data values (" + n + ") must be >= 2).";
		Message.printWarning ( 50, routine, message );
		throw new IllegalArgumentException ( message );
	}
	try {
	    meanx = mean(n, x);
	}
	catch ( Exception e ) {
		Message.printWarning ( 50, routine, "Error calculating mean - cannot calculate sample variance." );
		throw e;
	}
	for ( i = 0; i < n; i++ ) {
		dif = (x[i] - meanx);
		var += dif*dif;
	}
	var /= (double)(n - 1);
	return var;
}

/**
Compute the sample variance (sum(x_i - mean(x))^2)/(n - 1).
@param n number of values to use from the array
@param x array of values to process.
@param missing the value to use for missing values (will be ignored)
@return the sample variance
@throws IllegalArgumentException if the sample size after considering missing values is < 2,
which would result in division by zero
*/
public static double variance ( int n, double x[], double missing )
{	int	i;
	double dif, meanx;
	String message, routine = "MathUtil.variance";

	double var = 0.0;
	if ( n <= 1 ) {
		message = "Error calculating sample variance - the number of data values (" + n + ") must be >= 2).";
		Message.printWarning ( 50, routine, message );
		throw new IllegalArgumentException ( message );
	}
	meanx = mean(n, x, missing);
	int n2 = 0;
	for ( i = 0; i < n; i++ ) {
		if ( x[i] != missing ) {
			dif = (x[i] - meanx);
			var += dif*dif;
			++n2;
		}
	}
	if ( n2 <= 1 ) {
		message = "Error calculating sample variance - the number of data values (" + n + ") must be >= 2).";
		Message.printWarning ( 50, routine, message );
		throw new IllegalArgumentException ( message );
	}
	var /= (double)(n2 - 1);
	return var;
}

}
