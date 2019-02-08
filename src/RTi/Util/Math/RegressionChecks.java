// RegressionChecks - this class provides storage for check on the regression data and results, including ensuring that the

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

/**
This class provides storage for check on the regression data and results, including ensuring that the
sample size, correlation coefficient, and T-test (confidence interval) meet required constraints.  The class
is immutable.
*/
public class RegressionChecks
{

/**
Whether the analysis could be performed OK.
*/
boolean __isAnalysisPerformedOK = false;    
    
/**
Minimum sample size required to demonstrate relationship.
*/
Integer __minimumSampleSize = null;

/**
Actual sample size.
*/
Integer __sampleSize = null;
    
/**
Is sample size OK?
*/
boolean __isSampleSizeOK = false;

/**
Minimum R required to demonstrate relationship.
*/
Double __minimumR = null;

/**
Actual R for relationship.
*/
Double __R = null;

/**
Is R OK?
*/
boolean __isROK = false;

/**
Confidence interval required to demonstrate relationship, percent.
*/
Double __confidenceIntervalPercent = null;

/**
Is the confidence interval met using T test?
*/
Boolean __isTestOK = null;

/**
Create check object indicating check criteria and actual values.
@param analysisPerformedOK if true, then the regression analysis was performed OK; if false there was an
error such as divide by zero
@param minimumSampleSize the minimum sample size that is required
@param sampleSize the actual sample size
@param minimumR the minimum R that is required
@param R the actual R
@param confidenceIntervalPercent the confidence interval in percent (e.g., 95.0) - can be null if the
confidence interval is not being checked - this is set by the calling code due to complexity in checking the
value based on data in other objects
@param isTestOK whether the relationship satisfies the confidence interval when T test is checked
*/
public RegressionChecks ( boolean analysisPerformedOK, Integer minimumSampleSize, Integer sampleSize,
    Double minimumR, Double R, Double confidenceIntervalPercent, Boolean isTestOK )
{
    __isAnalysisPerformedOK = analysisPerformedOK;
    
    __minimumSampleSize = minimumSampleSize;
    __sampleSize = sampleSize;
    __isSampleSizeOK = true;
    if ( __minimumSampleSize != null ) {
        if ( __minimumSampleSize <= 0 ) {
            throw new InvalidParameterException("Minimum sample size (" +
                __minimumSampleSize + ") is invalid - must be >= 0");
        }
        if ( (__sampleSize == null) || (__sampleSize < __minimumSampleSize) ) {
            __isSampleSizeOK = false;
        }
    }

    __minimumR = minimumR;
    __R = R;
    __isROK = true;
    if ( __minimumR != null ) {
        if ( (R == null) || (R < minimumR) ) {
            __isROK = false;
        }
    }
    
    __confidenceIntervalPercent = confidenceIntervalPercent;
    if ( __confidenceIntervalPercent == null ) {
        // No interval specified so OK
        __isTestOK = true;
    }
    else {
        __isTestOK = isTestOK;
    }
}

/**
Format a string explaining why a relationship was invalid.  This is useful for logging.
*/
public String formatInvalidRelationshipReason()
{
    StringBuffer b = new StringBuffer();
    if ( !getIsAnalysisPerformedOK() ) {
        b.append("analysis not performed" );
    }
    if ( !getIsSampleSizeOK() ) {
        if ( b.length() > 0 ) {
            b.append( ", ");
        }
        b.append("sample size (" + getSampleSize() + ") < " + getMinimumSampleSize() );
    }
    if ( !getIsROK() ) {
        if ( b.length() > 0 ) {
            b.append( ", ");
        }
        b.append("R (" + getR() + ") < " + getMinimumR() );
    }
    Double confidenceIntervalPercent = getConfidenceIntervalPercent();
    if ( confidenceIntervalPercent != null ) {
        if ( (getIsTestOK() == null) || !getIsTestOK() ) {
            if ( b.length() > 0 ) {
                b.append( ", ");
            }
            b.append("CI not met" );
        }
    }
    return b.toString();
}

/**
Return the confidence interval that must be met - if null then confidence interval is not checked.
@return the confidence interval that must be met.
*/
public Double getConfidenceIntervalPercent ()
{   return __confidenceIntervalPercent;
}

/**
Indicate whether the analysis was performed OK.
@return true if the analysis was performed OK, false if not.
*/
public boolean getIsAnalysisPerformedOK ()
{   return __isAnalysisPerformedOK;
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
Indicate whether the confidence interval has been met in the T test.
@return true if the confidence interval has been met in the T test.
*/
public Boolean getIsTestOK ()
{   return __isTestOK;
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
Return the actual R.
@return the actual R.
*/
public Double getR ()
{   return __R;
}

/**
Return the actual sample size.
@return the actual sample size, may be null.
*/
public Integer getSampleSize ()
{
    return __sampleSize;
}

/**
Return a simple string indicating the values in the object, useful for logging.
*/
public String toString()
{
    return ( "isSampleSizeOK=" + getIsSampleSizeOK() + ", isROK=" + getIsROK() +
        ", isAnalysisPerformedOK=" + getIsAnalysisPerformedOK() + ", isTestOK=" + getIsTestOK() );
}

}
