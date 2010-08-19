package RTi.TS;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Vector;

import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
Compute a delta time series containing the difference between a value and the previous value.
*/
public class TSUtil_Delta
{
    
/**
Input time series to process.
*/
private TS __ts = null;

/**
Analysis start.
*/
private DateTime __analysisStart = null;

/**
Analysis end.
*/
private DateTime __analysisEnd = null;

/**
List of problems generated by this command, guaranteed to be non-null.
*/
private List<String> __problems = new Vector();

/**
The trend type expected for the data.
*/
private TrendType __trendType = null;

/**
Reset minimum value.
*/
private Double __resetMin = null;

/**
Reset maximum value.
*/
private Double __resetMax = null;

/**
Flag to label problem data points.
*/
private String __flag = null;

/**
Constructor for Delta calculation, which will create a new time series where each data value is the
change from the previous value.
@param ts time series to process
@param analysisStart the start of processing
@param analysisEnd the end of processing
@param trendType indicates the expected trend, which controls how the resets are handled
@param resetMin the minimum value, to which the data are reset to when the maximum is reached
@param resetMax the maximum value, which is used to compute the delta when a reset occurs
@param flag flag to label data points that have issues (use "Auto" to use internally determined defaults).
*/
public TSUtil_Delta ( TS ts, DateTime analysisStart, DateTime analysisEnd,
    TrendType trendType, Double resetMin, Double resetMax, String flag )
{
    __ts = ts;
    __analysisStart = analysisStart;
    __analysisEnd = analysisEnd;
    __trendType = trendType;
    if ( trendType == null ) {
        __trendType = TrendType.VARIABLE;
    }
    // Make sure that the trend type is handled
    if ( (__trendType != TrendType.DECREASING) && (__trendType != TrendType.INCREASING) &&
        (__trendType != TrendType.VARIABLE) ) {
        throw new InvalidParameterException ( "Trend type \"" + trendType + "\" is invalid." );
    }
    __resetMax = resetMax;
    __resetMin = resetMin;
    if ( ((__resetMax == null) && (__resetMin != null)) || ((__resetMax != null) && (__resetMin == null)) ) {
        throw new InvalidParameterException ( "ResetMin and ResetMax must both be specified or both be null." );
    }
    __flag = flag;
}

/**
Create a new time series that is the delta.
*/
public TS delta ()
throws Exception
{
    // Create a new list of problems
    __problems = new Vector();
    // Initialize a new time series as a copy of the previous time series
    TS ts = __ts;
    TS newts = TSUtil.newTimeSeries(ts.getIdentifier().getInterval(), false);
    newts.copyHeader( ts );
    newts.setMissing(Double.NaN); // Default is -999
    newts.setDataType(newts.getDataType() + "-Delta" );
    if ( __analysisStart != null ) {
        newts.setDate1 ( __analysisStart );
    }
    if ( __analysisEnd != null ) {
        newts.setDate1 ( __analysisEnd );
    }
    if ( (__flag != null) && !__flag.equals("") ) {
        newts.hasDataFlags(true,true);
    }
    newts.allocateDataSpace();
    // Loop through the time series
    TSIterator tsi = ts.iterator(__analysisStart, __analysisEnd);
    TSData tsdata;
    double newMissing = newts.getMissing();
    double valuePrev = ts.getMissing(); // Previous value from time series
    double valuePrevPrev = ts.getMissing(); // Previous value to valuePrev - only used when value is > resetMax
    double value; // Current value from time series
    double diff = 0.0; // Difference between current and previous value
    DateTime dt, dtPrev = null;
    // Reset values are optional
    boolean haveReset = false;
    double resetMin = 0.0, resetMax = 0.0;
    if ( __resetMin != null ) {
        haveReset = true;
        resetMin = __resetMin.doubleValue();
    }
    if ( __resetMax != null ) {
        resetMax = __resetMax.doubleValue();
    }
    // Determine the flags to use for labeling problem points
    String resetFlagHigh = null; // Value during reset is out of range high
    String resetFlagLow = null; // Value during reset is out of range low
    String valueFlagHigh = null; // Value outside of reset is out of range high
    String valueFlagLow = null; // Value outside of reset is out of range low
    if ( haveReset && (__flag != null) ) {
        if ( __flag.equalsIgnoreCase("Auto") ) {
            resetFlagHigh = "+R";
            resetFlagLow = "+r";
            valueFlagHigh = "+V";
            valueFlagLow = "+v";
            // Add metadata without +
            newts.addDataFlagMetadata(new TSDataFlagMetadata("R", "Previous value for reset is > ResetMax (" +
                StringUtil.formatString(resetMax,"%.6f") + ") - difference may be in error."));
            newts.addDataFlagMetadata(new TSDataFlagMetadata("r", "Previous value for reset is < ResetMin (" +
                StringUtil.formatString(resetMin,"%.6f") + ") - difference may be in error"));
            newts.addDataFlagMetadata(new TSDataFlagMetadata("V", "Value is > ResetMax (" +
                StringUtil.formatString(resetMax,"%.6f") + ")."));
            newts.addDataFlagMetadata(new TSDataFlagMetadata("v", "Value is < ResetMin (" +
                StringUtil.formatString(resetMin,"%.6f") + ")."));
        }
        else {
            // Use user value for all flags
            resetFlagHigh = __flag;
            resetFlagLow = __flag;
            valueFlagHigh = __flag;
            valueFlagLow = __flag;
        }
    }
    boolean diffComputed; // Used to determine if the difference has been computed
    String flag = null; // Used to flag bad values
    // Use iterator over the original time series and set in the new time series by specific date/time
    // Make sure that the loop completes (no "continue" so that previous iteration values are set)
    while ( (tsdata = tsi.next()) != null ) { // tsdata is reused - don't use below when setting the flag
        dt = tsdata.getDate();
        value = tsdata.getData();
        diffComputed = false;
        flag = null;
        // Can only compute the difference if the previous value was not missing
        if ( !ts.isDataMissing(valuePrev) && !ts.isDataMissing(value) ) {
            // Have non-missing current and previous values.  Check to see if a reset situation.
            if ( haveReset ) {
                // The reset handling is based on the trend parameter
                // The difference is computed as the change to the first reset (e.g., to resetMax) plus the change
                // from the second reset (e.g., resetMin to current value).
                if ( __trendType == TrendType.DECREASING ) {
                    if ( value > valuePrev ) {
                        //Message.printStatus ( 2, "", "At " + dt + " diff computed inside reset.");
                        // Value increased so account for reset
                        if ( valuePrev < resetMin ) {
                            if ( !ts.isDataMissing(valuePrevPrev) ) {
                                __problems.add( "Previous value " + valuePrev + " at " + dtPrev + " is < ResetMin " +
                                    resetMin + ".  Computing diff using second previous value " + valuePrevPrev );
                                diff = valuePrev - valuePrevPrev;
                            }
                            else {
                                // Previous value was above the maximum so can't compute diff component on top
                                // of the previous value
                                __problems.add( "Previous value " + valuePrev + " at " + dtPrev + " is < ResetMin " +
                                    resetMax + ".  Ignoring ambiguous amount below ResetMin in previous value for diff." );
                                diff = 0.0;
                            }
                            flag = resetFlagLow; // Will flag the data to indicate overrun (out of range)
                        }
                        else {
                            // Assume that the previous value transitioned to the min and then to the new value.
                            // This will be a negative number
                            diff = resetMin - valuePrev;
                        }
                        // Now add the top part of the reset
                        if ( value > resetMax ) {
                            __problems.add( "Value " + value + " at " + dt + " is > ResetMax " +
                                resetMin + ".  Adding amount above ResetMax to diff (decrease magnitude of negative diff)." );
                            flag = resetFlagHigh; // Will flag the data to indicate underrun (out of range)
                            diff += (value - resetMax);
                        }
                        else {
                            diff -= (resetMax - value);
                        }
                        // The difference has been computed so no need to compute below
                        diffComputed = true;
                    }
                }
                else if ( __trendType == TrendType.INCREASING ) {
                    if ( value < valuePrev ) {
                        //Message.printStatus ( 2, "", "At " + dt + " diff computed inside reset.");
                        // Value decreased so account for reset
                        if ( valuePrev > resetMax ) {
                            if ( !ts.isDataMissing(valuePrevPrev) ) {
                                __problems.add( "Previous value " + valuePrev + " at " + dtPrev + " is > ResetMax " +
                                    resetMax + ".  Computing diff using second previous value " + valuePrevPrev );
                                diff = valuePrev - valuePrevPrev;
                            }
                            else {
                                // Previous value was above the maximum so can't compute diff component on top
                                // of the previous value
                                __problems.add( "Previous value " + valuePrev + " at " + dtPrev + " is > ResetMax " +
                                    resetMax + ".  Ignoring ambiguous amount above ResetMax in previous value for diff." );
                                diff = 0.0;
                            }
                            flag = resetFlagHigh; // Will flag the data to indicate overrun (out of range)
                        }
                        else {
                            // Assume that the previous value transitioned to the max and then to the new value.
                            diff = resetMax - valuePrev;
                        }
                        // Now add the bottom part of the reset
                        if ( value < resetMin ) {
                            __problems.add( "Value " + value + " at " + dt + " is < ResetMin " +
                                resetMin + ".  Subtracting amount below ResetMin from diff." );
                            flag = resetFlagLow; // Will flag the data to indicate underrun (out of range)
                            diff -= (resetMin - value);
                        }
                        else {
                            diff += (value - resetMin);
                        }
                        // The difference has been computed so no need to compute below
                        diffComputed = true;
                    }
                }
            }
            if ( !diffComputed ) {
                //Message.printStatus ( 2, "", "At " + dt + " diff computed outside reset.");
                // Difference was not computed as reset case above so compute here
                // Can only compute a difference if the previous value was not missing
                diff = value - valuePrev;
                diffComputed = true;
                // Also generate warnings for out of range values that will skew the differences
                if ( haveReset ) {
                    // Reset limits were specified but a reset was not detected above - do range check
                    if ( value < resetMin ) {
                        __problems.add( "Value " + value + " at " + dt + " is < ResetMin " +
                            resetMin + ".  Difference may be in error." );
                        flag = valueFlagLow; // Will flag the data to indicate underrun (out of range)
                    }
                    else if ( value > resetMax ) {
                        __problems.add( "Value " + value + " at " + dt + " is > ResetMax " +
                            resetMax + ".  Difference may be in error." );
                        //Message.printStatus(2, "", __problems.get(__problems.size() - 1));
                        flag = valueFlagHigh; // Will flag the data to indicate overrun (out of range)
                    }
                }
            }
            if ( diffComputed ) {
                // The difference was computed so set the value
                //Message.printStatus(2, "", "At " + dt + " diff is " + diff + " flag is \"" + flag + "\"" );
                if ( flag != null ) {
                    // Set the data value and flag (duration left as before)
                    newts.setDataValue(dt, diff, TSData.appendDataFlag(tsdata.getDataFlag(),flag), tsdata.getDuration());
                }
                else {
                    newts.setDataValue(dt, diff);
                }
            }
            else {
                //Message.printStatus(2, "", "At " + dt + " diff is not computed - leaving missing." );
            }
        }
        // Fall through in cases where missing was encountered is to leave the new time series missing
        // However, for irregular time series, add a value at the date to be consistent with the input
        // time series.
        if ( !diffComputed ) {
            //Message.printStatus(2, "", "At " + dt + " diff not computed - set to missing " + newMissing +
            //    " flag is \"" + flag + "\"." );
            if ( flag != null ) {
                // Set the data value and flag (flag can have value even if data value is missing)
                newts.setDataValue(dt, newMissing, TSData.appendDataFlag(tsdata.getDataFlag(),flag), tsdata.getDuration());
            }
            else {
                newts.setDataValue(dt, newMissing);
            }
        }
        // Set the previous value for the next iteration
        valuePrevPrev = valuePrev; // Set before updating on next line
        valuePrev = value;
        dtPrev = dt;
        //Message.printStatus(2, "", "At " + dt + " TS value after set is " + newts.getDataValue(dt) + " flag \"" +
        //    newts.getDataPoint(dt,null).getDataFlag() + "\"");
    }
    
    return newts;
}

/**
Return a list of problems for the time series.
*/
public List<String> getProblems ()
{
    return __problems;
}

}