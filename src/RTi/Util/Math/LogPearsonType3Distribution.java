// LogPearsonType3Distribution - this class provides features related to the F distribution.

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

import java.security.InvalidParameterException;

import RTi.Util.Message.Message;

/**
This class provides features related to the F distribution.
*/
public class LogPearsonType3Distribution
{
    
/**
Sample data set (no missing values), in original form (not transformed).
*/
//private double [] __X = null;

/**
Skew coefficient G.
*/
private double __G;

/**
Mean of log10(X).
*/
private double __logXmean;

/**
Standard deviation of log10(X).
*/
private double __sigmaLogX;
    
/**
Constructor.
*/
public LogPearsonType3Distribution ( double [] X )
{   String routine = getClass().getName() + ".constructor";
    if ( (X == null) || (X.length < 3) ) {
        // Use n - 2 in divisor below
        throw new InvalidParameterException (
            "Sample size is too small - cannot analyze for log-Pearson Type III distribution." );
    }
    //setData ( X );
    // Compute the basic statistics
    // If a data value is <= 0.0 assume that it is .001 when computing the log
    // First compute logXbar = sum(log10(X))/n
    double logXbar = 0.0;
    for ( int i = 0; i < X.length; i++ ) {
        if ( X[i] <= 0 ) {
            logXbar += Math.log10(.001);
        }
        else {
            logXbar += Math.log10(X[i]);
        }
    }
    logXbar /= (double)X.length;
    setLogXMean ( logXbar );
    
    // Compute the standard deviation of X:
    //     sqrt(sum(log(X) - logXbar)^2)/(n - 1))
    // First compute the sum of the numerator
    double sigmaLogX = 0.0;
    double logX;
    double diff;
    for ( int i = 0; i < X.length; i++ ) {
        if ( X[i] <= 0 ) {
            logX = Math.log10(.001);
        }
        else {
            logX = Math.log10(X[i]);
        }
        diff = (logX - logXbar);
        sigmaLogX += diff*diff;
    }
    if ( sigmaLogX == 0.0 ) {
        // TODO SAM 2009-07-29 is it appropriate to handle samples that are all the same value?
        // All the sample values are the same.  This will cause an error below.  For now
        // just throw an exception.
        throw new InvalidParameterException (
            "All samples are the same value - cannot analyze log-Pearson Type III distribution." );
    }
    // Divide by the divisor and take the square root to get the final result
    sigmaLogX = Math.sqrt(sigmaLogX/(X.length - 1));
    setSigmaLogX ( sigmaLogX );

    // Compute the skew coefficient G as:
    // Eq1:  n* sum((logX - logXbar)^3)/((n - 1)(n - 2)(sigmaLogX)^3)
    // Eq2: more terms see Linsley section 13-4
    
    double top = 0.0;
    double sumLogX = 0.0, sumLogX2 = 0.0, sumLogX3 = 0.0;
    boolean eq1 = false; // Indicate which format to use
    for ( int i = 0; i < X.length; i++ ) {
        if ( X[i] <= 0 ) {
            logX = Math.log10(.001);
        }
        else {
            logX = Math.log10(X[i]);
        }
        if ( eq1 ) {
            diff = (logX - logXbar);
            top = top + Math.pow(diff, 3.0);
        }
        else {
            sumLogX3 += Math.pow(logX,3.0);
            sumLogX2 += logX*logX;
            sumLogX += logX;
        }
    }
    double G = 0.0;
    int n = X.length;
    if ( eq1 ) {
        G = (top*(double)n)/((double)(n - 1)*(double)(n - 2)*Math.pow(sigmaLogX,3.0));
    }
    else {
        G = (n*n*sumLogX3 - 3*n*sumLogX*sumLogX2 + 2*Math.pow(sumLogX,3.0))/(n*(n - 1)*(n - 2)*Math.pow(sigmaLogX,3.0));
    }
    setG ( G );
    
    // For diagnostics...
    
    Message.printStatus(2, routine, "Sample size=" + X.length + " mean(logX)=" + logXbar +
        " sumtop=" + top + " sigmaLogX=" + sigmaLogX + " G=" + G );
}

/**
Calculate the data value for percent chance.  The percent chance is converted to a recurrence interval, which is
then used for the calculation.
@param percentChance chance of event occurring in any one year (0-100).
*/
public double calculateForPercentChance ( double percentChance )
{
    return calculateForRecurrenceInterval ( 100.0/percentChance );
}

/**
Calculate the data value for a recurrence interval.  K is looked up for G (computed at initialization) and
the requested recurrence interval.
*/
public double calculateForRecurrenceInterval ( double recurrenceInterval )
{   String routine = getClass().getName() + ".calculateForRecurrenceInterval";
    if ( (recurrenceInterval < 1.0100) || (recurrenceInterval > 200.01) ) {
        throw new InvalidParameterException ( "Requested recurrence interval (" + recurrenceInterval +
            ") is outside supported range 1.0101 to 200." );
    }
    double K = lookupK ( getG(), recurrenceInterval );
    // Log X = logXmean + K*sigmaLogX
    double logXmean = getLogXMean();
    double sigmaLogX = getSigmaLogX();
    double logX = logXmean + K*sigmaLogX;
    // Now take the inverse log to get the value
    double value = Math.pow(10.0,logX);
    Message.printStatus ( 2, routine, "For G = " + getG() + " and recurrenceInterval=" + recurrenceInterval +
        " K=" + K + " value=" + value );
    return ( value );
}

/**
Return the skew coefficient G.
@return the skew coefficient G.
*/
public double getG()
{
    return __G;
}

/**
Return the mean of log10 for the sample.
@return the mean of log10 for the sample.
*/
private double getLogXMean ()
{
    return __logXmean;
}

/**
Return the standard deviation of log10 for the sample.
@return the standard deviation of log10 for the sample.
*/
private double getSigmaLogX ()
{
    return __sigmaLogX;
}

/**
Interpolate to find K given two G rows.
@param GrowLow the row in K corresponding to the lower G bound.
@param GrowHigh the row in K corresponding to the higher G bound.
@param K the values of K.
@param KrecInt the recurrence intervals used in the K table.
@param recurrenceInterval the requested recurrence interval.
*/
private double interpolateK ( int GrowLow, int GrowHigh, double G, double [][] K,
    double [] KrecInt, double recurrenceInterval )
{   String routine = getClass().getName() + ".interpolateK";
    // Find the lower and upper bounds on the recurrence interval column in the K table
    int ColLow = 0; // Bounding column for low recurrence interval (left in table)
    int ColHigh = 0; // Bounding column for high recurrence interval (right in table)
    if ( recurrenceInterval <= KrecInt[1] ) {
        // Use leftmost minimum value column
        ColLow = 1; // column 0 in K table is G
        ColHigh = 1;
    }
    else if ( recurrenceInterval >= KrecInt[KrecInt.length - 1] ) {
        // Use rightmost maximum value column
        ColLow = KrecInt.length - 1; // column 0 in K table is G
        ColHigh = KrecInt.length - 1;
    }
    else {
        // Find the bounding column (skip column 0 since G)
        for ( int i = 2; i < KrecInt.length - 1 ; i++ ) {
            if ( recurrenceInterval <= KrecInt[i] ) {
                ColHigh = i;
                ColLow = i - 1;
                break;
            }
        }
    }
    Message.printStatus ( 2, routine, "K [columns] that bound recurrence interval " + recurrenceInterval +
        " are " + ColLow + " (" + KrecInt[ColLow] + ") and " + ColHigh + " (" + KrecInt[ColHigh] + ")" );
    Message.printStatus ( 2, routine, "G [rows] that bound G " + G +
            " are GrowHigh=" + GrowHigh + " (" + K[GrowHigh][0] + ") and GrowLow=" + GrowLow + "(" +
            K[GrowLow][0] + ")" );
    // Interpolate the lower and upper K values in each column for the requested G
    double K1 = MathUtil.interpolate ( G,
            K[GrowHigh][0], K[GrowLow][0],
            K[GrowHigh][ColLow], K[GrowLow][ColLow] );
    double K2 = MathUtil.interpolate ( G,
            K[GrowLow][0], K[GrowHigh][0],
            K[GrowLow][ColHigh], K[GrowHigh][ColHigh] );
    Message.printStatus ( 2, routine, "K values for the columns are " + K1 + " and " + K2 );
    // Now interpolate the results between recurrence intervals...
    return MathUtil.interpolate ( recurrenceInterval,
            KrecInt[ColLow], KrecInt[ColHigh],
            K1, K2 );
}

/**
Lookup the K value.
@param G skew coefficient.
@param recurrenceInterval recurrence interval (year).
*/
public double lookupK ( double G, double recurrenceInterval )
{   //String routine = getClass().getName() + ".lookupK";
    // Frequency Factors K for Gamma and log-Pearson Distributions (Haan, 1977, Table 7.7)
    // First column is Gamma, followed by:
    // Recurrence interval, years:  1.0101,  2,  5, 10, 25, 50, 100, 200
    // Percent chance:                  99, 50, 20, 10,  4,  2,   1,  .5
    double [] KrecInt = { 0.0, 1.0101, 2, 5, 10, 25, 50, 100 }; // Ignore leading 0 - use to align with K
    double [][] K = {
            { 3.0,-0.667,-0.396,0.42, 1.18, 2.278,3.152,4.051,4.97 },
            { 2.9,-0.69, -0.39, 0.44, 1.195,2.277,3.134,4.013,4.904 },
            { 2.8,-0.714,-0.384,0.46, 1.21, 2.275,3.114,3.973,4.847 },
            { 2.7,-0.74, -0.376,0.479,1.224,2.272,3.093,3.932,4.783 },
            { 2.6,-0.769,-0.368,0.499,1.238,2.267,3.071,3.889,4.718 },
            { 2.5,-0.799,-0.36, 0.518,1.25, 2.262,3.048,3.845,4.652 },
            { 2.4,-0.832,-0.351,0.537,1.262,2.256,3.023,3.8,  4.584 },
            { 2.3,-0.867,-0.341,0.555,1.274,2.248,2.997,3.753,4.515 },
            { 2.2,-0.905,-0.33, 0.574,1.284,2.24, 2.97, 3.705,4.444 },
            { 2.1,-0.946,-0.319,0.592,1.294,2.23, 2.942,3.656,4.372 },
            { 2.0,-0.99, -0.307,0.609,1.302,2.219,2.912,3.605,4.298 },
            { 1.9,-1.037,-0.294,0.627,1.31, 2.207,2.881,3.553,4.223 },
            { 1.8,-1.087,-0.282,0.643,1.318,2.193,2.848,3.499,4.147 },
            { 1.7,-1.14, -0.268,0.66, 1.324,2.179,2.815,3.444,4.069 },
            { 1.6,-1.197,-0.254,0.675,1.329,2.163,2.78, 3.388,3.99 },
            { 1.5,-1.256,-0.24, 0.69, 1.333,2.146,2.743,3.33, 3.91 },
            { 1.4,-1.318,-0.225,0.705,1.337,2.128,2.706,3.271,3.828 },
            { 1.3,-1.383,-0.21, 0.719,1.339,2.108,2.666,3.211,3.745 },
            { 1.2,-1.449,-0.195,0.732,1.34, 2.087,2.626,3.149,3.661 },
            { 1.1,-1.518,-0.18, 0.745,1.341,2.066,2.585,3.087,3.575 },
            { 1.0,-1.588,-0.164,0.758,1.34, 2.043,2.542,3.022,3.489 },
            { 0.9,-1.66, -0.148,0.769,1.339,2.018,2.498,2.957,3.401 },
            { 0.8,-1.733,-0.132,0.78, 1.336,1.993,2.453,2.891,3.312 },
            { 0.7,-1.806,-0.116,0.79, 1.333,1.967,2.407,2.824,3.223 },
            { 0.6,-1.88, -0.099,0.8,  1.328,1.939,2.359,2.755,3.132 },
            { 0.5,-1.955,-0.083,0.808,1.323,1.91, 2.311,2.686,3.041 },
            { 0.4,-2.029,-0.066,0.816,1.317,1.88, 2.261,2.615,2.949 },
            { 0.3,-2.104,-0.05, 0.824,1.309,1.849,2.211,2.544,2.856 },
            { 0.2,-2.178,-0.033,0.83, 1.301,1.818,2.159,2.472,2.763 },
            { 0.1,-2.252,-0.017,0.836,1.292,1.785,2.107,2.4,  2.67 },
            { 0.0,-2.326, 0,    0.842,1.282,1.751,2.054,2.326,2.576 },
            { -0.1,-2.4,  0.017,0.846,1.27, 1.716,2,    2.252,2.482 },
            { -0.2,-2.472,0.033,0.85, 1.258,1.68, 1.945,2.178,2.388 },
            { -0.3,-2.544,0.05, 0.853,1.245,1.643,1.89, 2.104,2.294 },
            { -0.4,-2.615,0.066,0.855,1.231,1.606,1.834,2.029,2.201 },
            { -0.5,-2.686,0.083,0.856,1.216,1.567,1.777,1.955,2.108 },
            { -0.6,-2.755,0.099,0.857,1.2,  1.528,1.72, 1.88, 2.016 },
            { -0.7,-2.824,0.116,0.857,1.183,1.488,1.663,1.806,1.926 },
            { -0.8,-2.891,0.132,0.856,1.166,1.448,1.606,1.733,1.837 },
            { -0.9,-2.957,0.148,0.854,1.147,1.407,1.549,1.66, 1.749 },
            { -1.0,-3.022,0.164,0.852,1.128,1.366,1.492,1.588,1.664 },
            { -1.1,-3.087,0.18, 0.848,1.107,1.324,1.435,1.518,1.581 },
            { -1.2,-3.149,0.195,0.844,1.086,1.282,1.379,1.449,1.501 },
            { -1.3,-3.211,0.21, 0.838,1.064,1.24, 1.324,1.383,1.424 },
            { -1.4,-3.271,0.225,0.832,1.041,1.198,1.27, 1.318,1.351 },
            { -1.5,-3.33, 0.24, 0.825,1.018,1.157,1.217,1.256,1.282 },
            { -1.6,-3.88, 0.254,0.817,0.994,1.116,1.166,1.197,1.216 },
            { -1.7,-3.444,0.268,0.808,0.97, 1.075,1.116,1.14, 1.155 },
            { -1.8,-3.499,0.282,0.799,0.945,1.035,1.069,1.087,1.097 },
            { -1.9,-3.553,0.294,0.788,0.92, 0.996,1.023,1.037,1.044 },
            { -2.0,-3.605,0.307,0.777,0.895,0.959,0.98, 0.99, 0.995 },
            { -2.1,-3.656,0.319,0.765,0.869,0.923,0.939,0.946,0.949 },
            { -2.2,-3.705,0.33, 0.752,0.844,0.888,0.9,  0.905,0.907 },
            { -2.3,-3.753,0.341,0.739,0.819,0.855,0.864,0.867,0.869 },
            { -2.4,-3.8,  0.351,0.725,0.795,0.823,0.83, 0.832,0.833 },
            { -2.5,-3.845,0.36, 0.711,0.711,0.793,0.798,0.799,0.8 },
            { -2.6,-3.899,0.368,0.696,0.747,0.764,0.768,0.769,0.769 },
            { -2.7,-3.932,0.376,0.681,0.724,0.738,0.74, 0.74, 0.741 },
            { -2.8,-3.973,0.384,0.666,0.702,0.712,0.714,0.714,0.714 },
            { -2.9,-4.013,0.39, 0.651,0.681,0.683,0.689,0.69, 0.69 },
            { -3.0,-4.051,0.396,0.636,0.66, 0.666,0.666,0.667,0.667 }
    };
    int nrows = K.length; // Number of rows
    if ( G >= K[0][0] ) {
        // Probably precision problem.  Use the first row to find K
        return interpolateK ( 0, 0, G, K, KrecInt, recurrenceInterval );
    }
    else if ( G <= K[K.length - 1][0] ) {
        // Probably precision problem.  Use the last row to find K
        return interpolateK ( (K.length - 1), (K.length - 1), G, K, KrecInt, recurrenceInterval );
    }
    else {
        // Else find the rows that bound G so that K can be interpolated
        int i_lowG = -1, i_highG = -1;
        for ( int i = 1; i < nrows; i++ ) {
            if ( G >= K[i][0] ) {
                i_lowG = i;
                i_highG = i - 1; // Higher number in table
                break;
            }
        }
        return interpolateK ( i_lowG, i_highG, G, K, KrecInt, recurrenceInterval );
    }
}

/**
Set the sample dataset.
@param X sample data (no missing data).
*/
//private void setData ( double [] X ) 
//{
//    __X = X;
//}

/**
Set the skew coefficient G.
@param G skew coefficient G.
*/
private void setG ( double G ) 
{
    __G = G;
}

/**
Set the mean of log10 for the sample dataset.
@param logXmean the mean of log10 for the sample dataset.
*/
private void setLogXMean ( double logXmean ) 
{
    __logXmean = logXmean;
}

/**
Set the standard deviation of log10 for the sample dataset.
@param sigmaLogX the standard deviation of log10 for the sample dataset.
*/
private void setSigmaLogX ( double sigmaLogX ) 
{
    __sigmaLogX = sigmaLogX;
}

}
