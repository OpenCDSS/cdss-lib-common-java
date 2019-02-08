// StandardNormal - this class provides features related to the standard normal curve.

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

import java.lang.Math;

import RTi.Util.Message.Message;

/**
This class provides features related to the standard normal curve.
*/
public class StandardNormal implements Function
{

// Values of the standard normal density function (left side - the right side is symmetrical)
private static double [][] __zprob = {
				{	-5.0,	.000001 },
				{	-4.5,	.000004 }, 
				{	-4.0,	.000032 }, 
				{	-3.5,	.000233 },
				{	-3.0,	.001350 },
				{	-2.5,	.006210 },
				{	-2.0,	.022750 },
				{	-1.7,	.044566 },
				{	-1.5,	.066807 },
				{	-1.4,	.080757 },
				{	-1.3,	.096800 },
				{	-1.2,	.115069 },
				{	-1.1,	.135666 },
				{	-1.0,	.158655 },
				{	-0.9,	.184060 },
				{	-0.8,	.211855 },
				{	-0.7,	.241963 },
				{	-0.6,	.274253 },
				{	-0.5,	.308537 },
				{	-0.4,	.344578 },
				{	-0.3,	.382088 },
				{	-0.2,	.420740 },
				{	-0.1,	.460171 },
				{	0.0,	.500000 } };

/**
Return the cumulative value of the standard normal density function.
This method calculates the cumulative probability of a standard normal function
for a normalized variable "z" (z = 0 at mean).  It does so by using an
appropriate value as a starting point and then integrating to the desired value.
Half of the standard normal density curve is used for calculations since the curve is symmetrical.
@param z Normalized value (0 = mean).
@return the cumulative value of the standard normal density function.
@exception Exception if the there is an error evaluating the function.
*/
public static double cumulativeStandardNormal ( double z )
throws Exception
{	Function fp = new StandardNormal();	// Reference to function to integrate.
	double s;
	double z0 = z; // So user does not need to see z0 in javadoc
	int	i; // Loop counter.
	int nseg = 10; // Number of segments to divide interval into for integration.
	String routine = "MathUtil.cumulativeStandardNormal";

	// Account for very small values of "z"...

	if ( z0 <= -5.0 ) {
		return 0.0;
	}
	else if ( z0 >= 5.0 ) {
		return 1.0;
	}

	// Always want to work with a negative "z" for calculations...

	if ( z0 == 0.0 ) {
		return .5;
	}
	else if ( z0 > 0.0 ) {
		z = -z0;
	}
	else {
	    z = z0;
	}

	// Find the known point to start at...

	for ( i = 0; i < __zprob.length; i++ ) {
		if ( z == __zprob[i][0] ) {
			// Have exactly matched a value in __zprob so return the
			// proper value, depending on the sign of the z-value...
			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, routine,
				"Looking up " + z +	" - exactly matches zprob [" + i + "]" );
			}
			if ( z0 > 0.0 ) {
				return ( 1.0 - __zprob[i][1] );
			}
			else {
			    return __zprob[i][1];
			}
		}
		// _zprob values start with -5.0 at [0] and end with 0.0 at
		// [length - 1].  We want to find the z value
		// that is to the "left" of the requested z value...
		else if ( __zprob[i][0] > z ) {
			// Caught the -5.0 case above and would have returned already...
			if ( Message.isDebugOn ) {
				Message.printDebug ( 50, routine, "Looking up " + z + " - \"left\" value is zprob [" + i + "]" );
			}
			--i;
			break;
		}
	}

	if ( i >= __zprob.length ) {
		throw new Exception ( 
		"Trying to access zprob[" + i +	"] looking up " + z0 + " (neg is " + z +
		") - array size is " + __zprob.length );
	}

	// Integrate from the known point to the requested z value...

	s = __zprob[i][1] + MathUtil.integrate (
		MathUtil.INTEGRATE_GAUSSIAN_QUADRATURE, fp,	__zprob[i][0], z, nseg );

	// Recalculate if on the right side of the distribution...

	if ( z0 > 0.0 ) {
		s = 1.0 - s;
	}

	return s;
}

/**
Calculate the normalized data value "z" given a cumulative probability value.
Because the solution is not closed-form, numerically solve for a "z"
corresponding to a specified probability.  This amounts to solving for the root
where the x-axis is "z" and the y-axis is "prob - prob1".
@param prob Probability corresponding to "z" (0.0 to 1.0).
@return the normalized data value of "z" for a probability.
@exception Exception if there is an error calculating the value.
*/
public static double cumulativeStandardNormalInverse ( double prob )
throws Exception
{	double prob0 = prob; // So developers don't see prob0 in javadoc
	double a = 0.0, b = 0.0;// Estimates for "z".
	double fa, fb; // Probability for "z" values of "a" and "b".
	double prob1; // Value used in calculations.
	double ptol = .000001; // Minimum tolerance for ending estimation.
	double z = 0.0; // The normalized value corresponding to the given probability.
	int count = 0; // Counter for iterations to converge.
	int dl = 20; // Debug level for this method
	int maxcount = 50; // Maximum number of iterations before giving up
	String routine = "StandardNormal.cumulativeStandardNormalInverse";
	String message;

	if ( Message.isDebugOn ) {
		Message.printDebug ( 20, routine, "Trying to find Z value for probability " + prob0 );
	}

	// We can only work on probabilities that are between 0.0 and 1.0
	// (actually not exactly zero or one but close)...

	if ( (prob0 <= 0.0) || (prob0 >= 1.0) ) {
		message = "Requested probability (" + prob0 + ") not in the range > 0.0 to < 1.0";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}

	// Only work on the left half of the probability curve...

	if ( prob0 == .5 ) {
		return 0.0;
	}
	else if ( prob0 < .5 ) {
		prob1 = prob0;
	}
	else {
	    prob1 = 1.0 - prob0;
	}

	// Select good bounds for starting numerical estimate...

	for ( int i = (__zprob.length - 1); i >= 0; i-- ) {
		if ( prob1 == __zprob[i][1] ) {
			// Exact match with the GRzprob lookup table...
			if ( prob0 > .5 ) {
				return (-1.0*__zprob[i][0]);
			}
			else {
			    return __zprob[i][0];
			}
		}
		else if ( prob1 > __zprob[i][1] ) {
			a = __zprob[i][0];
			b = __zprob[i + 1][0];
			break;
		}
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Starting solution for prob " + prob1 + " using bounds " + a + " and " + b );
	}

	/*
	** The following bisection algorithm is generally too slow
	** (3-15 iterations) and is not currently used...
	**
	while ( 1 ) {
		z	= (a + b)/2.0;
		prob	= cumulativeStandardNormal ( z );
		if (	((Math.abs(prob - prob1)) <= ptol) ||
			(count == maxcount) )
			break;
		if ( prob < prob1 ) {
			a = z;
		}
		else {	b = z;
		}
		++count;
	}*/

	// Find the root using the secant method...

	fa = cumulativeStandardNormal ( a ) - prob1;
	fb = cumulativeStandardNormal ( b ) - prob1;
	while ( true ) {
		if ( fa == fb ) {
			// Both points are the same.  It has converged.  Exit the loop.
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "fa (" + fa + ") = fb at z = " + z + ".  Converged" );
			}
			break;
		}
		z = b - fb*(b - a)/(fb - fa);
		// Add to the convergence notes.  May want to comment this out
		// at some point, but currently am tracking down a bug...
		if ( (Math.abs(z - b)) < ptol ) {
			// Have converged...
			break;
		}
		if ( count > maxcount ) {
			Message.printWarning ( 20, routine,
			"Maximum iteration count (" + maxcount +
			") reached.  Values for " + prob1 + " are f(" + a +	")=" + fa + " f(" + b + ")=" + fb );
			break;
		}
		a = b;
		b = z;
		fa = fb;
		fb = cumulativeStandardNormal ( z ) - prob1;
		++count;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Found solution for prob " + prob1 + " after " + count + " iterations: " + z );
	}

	if ( prob0 > .5 ) {
		return (-1.0*z); // z corresponds to prob1
	}
	else {
	    return z;
	}
}

/**
Determine the value of standard normal density function.
This method calculates the probability of an event occurring at normalized value
"z" (z = 0 for the mean).  This is the familiar bell curve function for a mean
of zero and a standard deviation of one.
@return the value of standard normal density function.
@param params Parameters for the function.  The value of params[0] should be
the z value (the normalized value where 0 = mean and 1 = one standard deviation).
*/
public double evaluate ( double [] params )
{	double z = params[0];
	return (1.0/Math.sqrt(2.0*Math.PI))*Math.pow(Math.E,-1.0*z*z/2.0);
}

}
