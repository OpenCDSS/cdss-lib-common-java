// TSUtil_ChangeInterval - change a time series interval, creating a new time series.

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

package RTi.TS;

import java.util.ArrayList;
import java.util.List;

import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeScaleType;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

/**
 * Change a time series interval, creating a new time series.
 */
public class TSUtil_ChangeInterval
{
    
/**
How to handle interval end-points when going from smaller interval to larger interval.
*/
private TSUtil_ChangeInterval_HandleEndpointsHowType __handleEndpointsHow = null;

/**
How to handle missing values in input.
<pre>
Legacy doc (related to real-time data transmission):
Default to Regular Transmission
Used by:
 1) changeInterval_fromIRREGULAR ()
 2) changeInterval_toMEANorACCM ()
 3) changeInterval_fromINST ()
to replace old TS missing data values,
if Alert increment data (precipitation increments) by Zero
if Alert regular for other data types Repeat
For REGULAR cases nothing will be changed, the missing values will be replaced by missing values.
</pre>
*/
private TSUtil_ChangeInterval_HandleMissingInputHowType __handleMissingInputHow = null;

/**
Number of missing values allowed in the input data and still compute the result.
This is ultimately used to perform checks, with the percent optionally being used to compute this value.
*/
private int __allowMissingCount = 0;

/**
Number of consecutive missing values allowed in the input data and still compute the result.
This is ultimately used to perform checks, with the percent optionally being used to compute this value.
*/
private int __allowMissingConsecutive = 0;

/**
Percent (0 - 100) of missing values allowed in the input data and still compute the result.
This value is optionally specified at construction but is converted to __allowMissingCount
once the time series interval ratio is known.
*/
private double __allowMissingPercent = -1.0; // -1 indicates that percent is not used

/**
Data type for the new time series.
*/
private String __newDataType = null;

/**
New time series data interval.
*/
private TimeInterval __newInterval = null;

/**
New time time scale for output time series.
*/
private TimeScaleType __newTimeScale = null;

/**
Statistic computed while changing interval (e.g., max value when converting INST-small to INST-large).
*/
private TSStatisticType __statistic = null;

/**
Units for the new time series.
*/
private String __newUnits = null;

/**
Original time series that is being used as input.
*/
private TS __oldTS = null;

/**
Original time time scale for input time series.
*/
private TimeScaleType __oldTimeScale = null;

/**
Output fill method when going from larger interval to smaller interval.
*/
private TSUtil_ChangeInterval_OutputFillMethodType __outputFillMethod = null;

/**
Output year type when new interval is year.
*/
private YearType __outputYearType = null;

/**
Tolerance (e.g., .01) used with convergence tests (when needed).
*/
private double __tolerance = 0.01;

/**
Legacy constructor used when running old methods in this class.
For most conversions use the constructor with the full list of parameters.
*/
public TSUtil_ChangeInterval ()
{
    
}

/**
Constructor.
@param oldTS Time series used as data to generate the new time series.
@param newInterval New interval as a string that can be parsed by RTi.Util.Time.TimeInterval.parseInterval(),
indicating the new interval for the time series.
@param oldTimeScale The old time scale indicates whether values in the old time series are accumulated over
the interval ("ACCM"), an average over the interval ("MEAN"), or instantaneous for the point in time ("INST")).
@param newTimeScale The new time scale indicates whether values in the new time series are accumulated over
the interval ("ACCM"), an average over the interval ("MEAN"), or instantaneous for the point in time ("INST")).
@param statisticType the output statistic type, support for some calculations (currently only INST-small
to INST-large), for example compute daily max from instantaneous small interval.  Specify null to use the
default statistic for the time scale and interval combination.
@param outputYearType output year type used when the new interval is year (if null use calendar).
@param newDataType the data type for the new time series.  If null, the data type from the old time series
will be used (Warning: for some systems, the meaning of the data type changes depending on the interval.
For example "Streamflow" may be instantaneous for irregular data and mean for daily data.)
@param newUnits Units for the new time series.  If null, use the units from the original time series.
@param tolerance Used when converting large interval MEAN data to small interval INST data - see TSTool documentation.
@param handleEndpointsHow when changing from INST to MEAN, small interval to large interval (but only daily or
smaller), indicate how the end-points should be handled.
@param allowMissingCount Indicate the number of missing values that can be missing in the input data and still
allow computation of the result. For example, if daily data are being converted to monthly, a value of 5 would
allow <= 5 missing values to be present in a month's daily data, and still generate a monthly value.
The missing values are evaluated for the block of input needed to compute the result.
This parameter is not used for conversion from irregular time series or conversions from larger
intervals to smaller ones.  If specified as null, the default value is 0, meaning that any missing data in
the input data will result in missing data in the result.
@param allowMissingPercent Indicate the percent of missing values that can be missing in the input data and
still allow computation of the result.  For example, if daily data are being converted to monthly,
a value of 25 would allow <= 25% missing values to be present in a month's daily data, and still
generate a monthly value.  The missing values are evaluated for the block of input needed to compute the result.
Because months have different numbers of days, using a percentage may result in a different threshold for
different months. This argument is not used for conversion from irregular time series or conversions
from larger intervals to smaller ones.  If specified as null, the default value is 0,
meaning that any missing data in the input data will result in missing data in the result.  However, if the
allowMissingCount is specified, it will override this default.
@param allowMissingConsecutive the number of consecutive missing values allowed and still compute the result,
considered at the same time as allowMissingCount
@param outputFillMethod This property is used only by conversions from INST to MEAN when the conversion
is from larger to smaller time intervals.  The options are:
INTERPOLATE - The new time series values are interpolated between the old time series data points.
REPEAT - The new time series values are carry forward or backward from one of the bounding old data values
depending on the time interval (<Day or >=Day) data point. The default if specified as null is REPEAT.
@param handleMissingImputHow Indicate how to handle the missing data. The options are:
KEEP_MISSING (regular), missing data are kept as missing,
REPEAT (precipitation alert data), missing data are replaced by zero,
SET_TO_ZERO (other alert data), missing data are replaced by previous value.
This parameter is used by supporting routines to handle missing data.
The default if specified as null is KEEP_MISSING.
TODO SAM 2009-11-02 need to evaluate the references to ALERT versus general behavior.
*/
public TSUtil_ChangeInterval ( TS oldTS, TimeInterval newInterval,
    TimeScaleType oldTimeScale, TimeScaleType newTimeScale, TSStatisticType statisticType, YearType outputYearType,
    String newDataType, String newUnits, Double tolerance,
    TSUtil_ChangeInterval_HandleEndpointsHowType handleEndpointsHow,
    TSUtil_ChangeInterval_OutputFillMethodType outputFillMethod,
    TSUtil_ChangeInterval_HandleMissingInputHowType handleMissingInputHow,
    Integer allowMissingCount, Double allowMissingPercent, Integer allowMissingConsecutive )
{
    // Check controlling information
    // OldTS - Make sure it is not null and has a not zero length period of record.
    if ( oldTS == null ) {
        throw new IllegalArgumentException ( "Input time series is null.  Cannot change interval." );
    }
    // TODO SAM 2011-02-19 Evaluate if this is OK to pass - need to allow for discovery mode creation of TS
    //if ( !oldTS.hasData() ) {
    //    throw new IllegalArgumentException(  "Input time series has no data.  Cannot change interval." );
    //}
    setOldTimeSeries ( oldTS );
    int oldIntervalBase = oldTS.getDataIntervalBase();
    
    if ( newInterval == null ) {
        throw new IllegalArgumentException ( "New interval is null.  Cannot change interval." );
    }
    setNewInterval ( newInterval );
    
    // Time scale for the old time series
    if ( oldTimeScale == null ) {
        throw new IllegalArgumentException ( "Old time scale is null.  Cannot change interval.");
    }
    setOldTimeScale ( oldTimeScale );

    // Time scale for the new time series
    if ( newTimeScale == null ) {
        throw new IllegalArgumentException ( "New time scale is null.  Cannot change interval.");
    }
    setNewTimeScale ( newTimeScale );
    
    // Statistic is only allowed for INST (small) to INST (large)
    if ( statisticType != null ) {
        if ( oldTimeScale != TimeScaleType.INST ) {
            throw new IllegalArgumentException ( "Statistic can only be specified with INST(small) to INST(large).");
        }
        if ( newTimeScale != TimeScaleType.INST ) {
            throw new IllegalArgumentException ( "Statistic can only be specified with INST(small) to INST(large).");
        }
        // TODO SAM 2010-03-24 need check for small interval to large
        boolean supported = false;
        List<TSStatisticType> statistics = TSUtil_CalculateTimeSeriesStatistic.getStatisticChoices();
        for ( TSStatisticType statistic : statistics ) {
            if ( statisticType == statistic ) {
                supported = true;
                break;
            }
        }
        if ( !supported ) {
            throw new IllegalArgumentException ( "The statistic (" + statisticType +
                ") is not supported by this command." );
        }
    }
    setStatistic ( statisticType );
    
    if ( outputYearType == null ) {
        outputYearType = YearType.CALENDAR;
    }
    setOutputYearType ( outputYearType );
    
    if ( (newDataType == null) || newDataType.equals("")  ) {
        // Assume the data type of the old time series
        setNewDataType( oldTS.getDataType() );
    }
    else {
        setNewDataType ( newDataType );
    }
    
    if ( (newUnits == null) || newUnits.equals("") ) {
        setNewUnits ( oldTS.getDataUnits() );
    }
    else {
        setNewUnits ( newUnits );
    }
    
    if ( tolerance == null ) {
        tolerance = new Double ( 0.01 ); // Default
    }
    // If the given value is less than 0, throw exception.
    if ( (tolerance.doubleValue() < 0.0) || (tolerance.doubleValue() > 1.0) ) {
        throw new IllegalArgumentException("Tolerance_double (" + tolerance + ") is not between 0 and 1.  " +
            "Cannot change interval.");
    }
    setTolerance ( tolerance.doubleValue() );
    
    // HandleEndpointsHow - Used when changing from INST to MEAN, small interval to large (daily or less) interval.
    if ( (oldTimeScale == TimeScaleType.INST) && (newTimeScale == TimeScaleType.MEAN) &&
        (newInterval.getBase() <= TimeInterval.DAY) && (oldIntervalBase < TimeInterval.DAY) ) {
        if ( handleEndpointsHow == null ) {
            // Default...
            handleEndpointsHow = TSUtil_ChangeInterval_HandleEndpointsHowType.AVERAGE_ENDPOINTS;
        }
        // Else go with the user value
    }
    else {
        if ( handleEndpointsHow == null ) {
            // Legacy code used this for other combinations - gives the correct numbers
            //handleEndpointsHow = TSUtil_ChangeInterval_HandleEndpointsHowType.INCLUDE_FIRST_ONLY;
            // OK to leave as null as default will be handled in other code.
        }
        else {
            throw new IllegalArgumentException("Specifying HandleEndpointsHow is only valid when changing " +
        	    "from INST to MEAN small interval to large (daily or less).");
        }
    }
    setHandleEndpointsHow ( handleEndpointsHow );
    
    // OutputFillMethod - Used when changing from INST to MEAN time
    // series going from larger to smaller Time interval.
    if ( outputFillMethod == null ) {
        outputFillMethod = TSUtil_ChangeInterval_OutputFillMethodType.REPEAT; // Default
    }
    setOutputFillMethod ( outputFillMethod );
    
    // HandleMissingInputHow
    if ( handleMissingInputHow == null ) {
        handleMissingInputHow = TSUtil_ChangeInterval_HandleMissingInputHowType.KEEP_MISSING; // Default
    }
    // Not used for conversion to annual...
    if ( ((newTimeScale == TimeScaleType.MEAN) || (newTimeScale == TimeScaleType.ACCM)) &&
        (newInterval.getBase() == TimeInterval.YEAR) &&
        (handleMissingInputHow != TSUtil_ChangeInterval_HandleMissingInputHowType.KEEP_MISSING)) {
        throw new IllegalArgumentException (
            "HandleMissingInputHow cannot be specified for MEAN or ACCM, Year interval output." );
    }
    setHandleMissingInputHow ( handleMissingInputHow );
    
    // AllowMissingCount or AllowMissingPercent properties.
    // Only one of these properties is expected.
    // If both are given, throw an exception.
    if ( (allowMissingCount != null) && (allowMissingPercent != null) ) {
        throw new IllegalArgumentException ( "Only one of these parameters should be provided as non-null: " +
            "AllowMissingCount or AllowMissingPercent.  Cannot change interval." );
    }
    
    // AllowMissingCount
    if ( allowMissingCount == null ) {
        allowMissingCount = new Integer(0); // Default is don't allow missing
    }
    // If the given value is less than 0, throw exception.
    if ( allowMissingCount.intValue() < 0 ) {
        throw new IllegalArgumentException("AllowMissingCount (" + allowMissingCount +
            ") is negative - must be >= 0.  Cannot change interval.");
    }
    setAllowMissingCount ( allowMissingCount.intValue() );

    // AllowMissingPercent
    if ( allowMissingPercent != null ) {
        // If the given value is less than 0, throw exception.
        if ( allowMissingPercent.doubleValue() < 0.0 ) {
            throw new IllegalArgumentException("AllowMissingPercent (" + allowMissingPercent +
                ") is negative - must be between 0 and 100.  Cannot change interval.");
        }
        else if ( allowMissingPercent.doubleValue() > 100.0 ) {
            throw new IllegalArgumentException("AllowMissingPercent (" + allowMissingPercent +
                ") is > 100 - must be between 0 and 100.  Cannot change interval.");
        }
        setAllowMissingPercent ( allowMissingPercent.doubleValue() );
    }
    
    // AllowMissingConsecutive
    if ( allowMissingConsecutive == null ) {
        if ( allowMissingCount > 0 ) {
            // Missing allowed, so allow all to be in sequence
            allowMissingConsecutive = new Integer(allowMissingCount);
        }
        else {
            allowMissingConsecutive = new Integer(0); // Default is don't allow missing
        }
    }
    // If the given value is less than 0, throw exception.
    if ( allowMissingConsecutive.intValue() < 0 ) {
        throw new IllegalArgumentException("AllowMissingConsecutive (" + allowMissingConsecutive +
            ") is negative - must be >= 0.  Cannot change interval.");
    }
    setAllowMissingConsecutive ( allowMissingConsecutive );
}

    /**
     * Once a simulated instantaneous time series has been created, compare it to an
     * existing mean time series.  After summing the simulated instantaneous values
     * over each mean interval (using the average of the end-points), if the difference
     * between the simulated and given values is outside a given tolerance, adjust
     * each value in the simulated accordingly.  The beginning end-point is actually
     * an average of the current and previous simulated values.
     *
     * @param TSMean - old Mean TS we are trying to change to instantaneous
     * @param TSInst - newly created instantaneous TS
     * @param tolerance - a fraction 0 to 1
     */
    private void adjustInstantaneousBasedOnMean(TS oldTS, TS newTS, double tolerance ) throws Exception {
        String routine = "TSUtil_ChangeInterval_adjustInstantaneousBaseOnMean";
        int oldIntervalBase = oldTS.getDataIntervalBase();
        int oldIntervalMult = oldTS.getDataIntervalMult();
        int newIntervalBase = newTS.getDataIntervalBase();
        int newIntervalMult = newTS.getDataIntervalMult();
        int newPrecision = newTS.getDate1().getPrecision();
        double sum, simulatedMean, oldValue, firstValueNew, lastValueNew, ratio, tmp, savedPreviousEndPoint;

        // get the dates of the first and second data values of the old TS
        DateTime startDateOld = new DateTime (oldTS.getDate1());
        DateTime nextDateOld = new DateTime (startDateOld);
        nextDateOld.addInterval(oldTS.getDataIntervalBase(), oldTS.getDataIntervalMult());

        // provide for the corresponding dates of the new TS
        DateTime startDateNew;
        DateTime lastDateNew; // last date of the current interval we are analyzing
        DateTime currentDateNew; // this date will move from startDateNew to lastDateNew

        int nintervals = TimeUtil.getNumIntervals(startDateOld, nextDateOld, newIntervalBase, newIntervalMult);

        boolean needsAdjustment = true;
        int iterations = 0;
        savedPreviousEndPoint = newTS.getMissing();
        while ( needsAdjustment && iterations < 15 ) {
            needsAdjustment = false;

            // initialize the mean dates
            startDateOld = new DateTime (oldTS.getDate1());
            nextDateOld = new DateTime (startDateOld);
            nextDateOld.addInterval(oldTS.getDataIntervalBase(), oldTS.getDataIntervalMult());

            // initialize the start and end dates for the instantaneous
            startDateNew = new DateTime (startDateOld);
            startDateNew.setPrecision(newPrecision);
            lastDateNew = new DateTime (nextDateOld);      // last date of the current interval we are analyzing
            lastDateNew.setPrecision(newPrecision);

            while ( !lastDateNew.greaterThan(newTS.getDate2())) {
                sum = 0;
                oldValue = oldTS.getDataValue(startDateOld);
                if ( oldTS.isDataMissing(oldValue)) {
                    savedPreviousEndPoint = newTS.getMissing();
                }
                else {
                    // first and last values are averaged
                    // currentDateNew collects middle values
                    currentDateNew = new DateTime ( startDateNew );
                    currentDateNew.addInterval(newIntervalBase, newIntervalMult);
                    firstValueNew = newTS.getDataValue(startDateNew);
                    lastValueNew = newTS.getDataValue(lastDateNew);
                    savedPreviousEndPoint = newTS.isDataMissing(savedPreviousEndPoint) ? firstValueNew : savedPreviousEndPoint;
                    // sum middle values
                    for ( int i=0; i<nintervals-1; i++) {
                        sum += newTS.getDataValue(currentDateNew);
                        currentDateNew.addInterval(newIntervalBase, newIntervalMult);
                    }
                    // add the averaged first and last values
                    sum += (savedPreviousEndPoint + lastValueNew)/2.0;
                    simulatedMean = sum/nintervals;

                    if ( (Math.abs(simulatedMean - oldValue) / oldValue) > tolerance ) {
                        //
                        // adjust new ts
                        //
                        needsAdjustment = true;
                        ratio = oldValue/simulatedMean;
                        currentDateNew = new DateTime (startDateNew);

                        // BEGINNING ENDPOINT:
                        tmp = newTS.getDataValue(currentDateNew);
                        newTS.setDataValue(currentDateNew, ((tmp + (tmp * ratio))) / 2.0 );
                        currentDateNew.addInterval(newIntervalBase, newIntervalMult);
                        
                        // MIDDLE VALUES:
                        for ( int i=0; i<nintervals-1; i++) {
                            newTS.setDataValue(currentDateNew, newTS.getDataValue(currentDateNew) * ratio );
                            currentDateNew.addInterval(newIntervalBase, newIntervalMult);
                        }
                        // ENDING ENDPOINT: rather than setting the last data value like the others, we average the new
                        // adjusted value with the old (not from the old TS but the old simulated value)
                        tmp = newTS.getDataValue(currentDateNew);
                        savedPreviousEndPoint = tmp;
                        newTS.setDataValue(currentDateNew, (tmp + (tmp * ratio)) / 2.0 );
                    }
                    else {
                        savedPreviousEndPoint = lastValueNew;
                    }
                }
                // move oldTS dates to next interval
                startDateOld.addInterval(oldIntervalBase, oldIntervalMult);
                nextDateOld.addInterval(oldIntervalBase, oldIntervalMult);

                // set newTS dates accordingly
                startDateNew = new DateTime (startDateOld);
                lastDateNew = new DateTime (nextDateOld);
                startDateNew.setPrecision(newPrecision);
                lastDateNew.setPrecision(newPrecision);
            }
            iterations++;
        }

        if ( iterations == 15 ) {
            Message.printWarning(2, routine, "Maximum number of iterations reached during adjustment.");
        }
        else {
            Message.printStatus ( 10, routine, "Iterated " + iterations + " time(s) to adjust instantaneous ts to mean ts." );
        }

    }

    /**
     * Return a new time series having a different interval from the source time series.
     * This is the main routine which will call subordinate routines, depending on the
     * intervals that are involved.
     * @return A new time series of the requested data interval. All of the original time series header
     * information will be essentially the same, except for the interval and possibly the data type.
     * @param createData if true, calculate values for the data array; if false, only assign metadata
     * @exception Exception if an error occurs (e.g., bad new interval string).
     */
    public TS changeInterval ( boolean createData )
    throws Exception {
        String routine = "changeInterval", status, warning;
        
        TS oldTS = getOldTimeSeries();
        TimeInterval newInterval = getNewInterval();
        int newtsBase = newInterval.getBase();
        int newtsMultiplier = newInterval.getMultiplier();
        TimeScaleType oldTimeScale = getOldTimeScale();
        TimeScaleType newTimeScale = getNewTimeScale();
        TSStatisticType statistic = getStatistic();
        YearType outputYearType = getOutputYearType();
        String newUnits = getNewUnits(); // Will check below when creating the time series.
        String newDataType = getNewDataType();
        TSUtil_ChangeInterval_OutputFillMethodType outputFillMethod = getOutputFillMethod();
        double tolerance = getTolerance();
        TSUtil_ChangeInterval_HandleEndpointsHowType handleEndpointsHow = getHandleEndpointsHow();
        TSUtil_ChangeInterval_HandleMissingInputHowType handleMissingInputHow = getHandleMissingInputHow();
        int allowMissingCount = getAllowMissingCount();
        int allowMissingConsecutive = getAllowMissingConsecutive();
        double allowMissingPercent = getAllowMissingPercent();

        // ??????????????????????????????????????????????????????????????????????
        // Create the new time series
        // From the old time series identifier create the new time series identifier.
        TSIdent newtsIdent = new TSIdent(oldTS.getIdentifier());
        // Set with the string here so that the interval is an exact match with what
        // was requested (e.g., "1Hour" remains and does not get converted to "hour").
        // Otherwise, the time series identifiers won't match.
        //newtsIdent.setInterval(newtsBase, newtsMultiplier);
        newtsIdent.setInterval("" + newInterval);

        // Create the new time series using the new identifier.
        TS newTS = TSUtil.newTimeSeries(newtsIdent.getIdentifier(), true);
        // newTS = newTimeSeries( new_interval, false ); // or simple 1Month
        if (newTS == null) {
            throw new RuntimeException("Could not create the new time series - cannot change interval.");
        }

        // Update the new time series properties with all required information.
        // Notice: CopyHeader() overwrites, among several other things,
        // the Identifier, the DataInterval (Base and Multiplier).
        // It also set the dates, from the old time series. Make sure to
        // reset these properties to the values needed by the new time series.
        newTS.copyHeader(oldTS);
        newTS.setDataType(newDataType);
        newTS.setIdentifier(newtsIdent);
        newTS.setDataInterval(newtsBase, newtsMultiplier);
        // Get the bounding dates for the new time series based on the old time series.
        if ( createData ) {
            DateTime newts_date[] = getBoundingDatesForChangeInterval(oldTS, newtsBase, newtsMultiplier);
            newTS.setDate1(newts_date[0]);
            newTS.setDate2(newts_date[1]);
            newTS.setDate1Original(oldTS.getDate1());
            newTS.setDate2Original(oldTS.getDate2());
        }
        
        // If the output is a different year type, adjust the output time series to fully
        // encompass the original time series period.
        if ( (newInterval.getBase() == TimeInterval.YEAR) && (outputYearType != YearType.CALENDAR) ) {
            if ( (outputYearType.getStartYearOffset() < 0) &&
                (oldTS.getDate1().getMonth() >= outputYearType.getStartMonth()) ) {
                // The old time series starts >= after the beginning of the output year and would result
                // in an extra year at the start so increment the first year. For example, if the water year
                // and the start is Oct, 2000, need to increment the output year to 2001.
                DateTime date1 = newTS.getDate1();
                date1.addYear ( 1 );
                newTS.setDate1 ( date1 );
                Message.printStatus(2, routine, "Adjusting output time series start to " + date1 +
                    " to align with " + outputYearType + " year type." );
            }
            // Similarly shift the end of the year...
            if ( (outputYearType.getStartYearOffset() < 0) &&
                (oldTS.getDate2().getMonth() >= outputYearType.getStartMonth()) ) {
                DateTime date2 = newTS.getDate2();
                date2.addYear ( 1 );
                newTS.setDate2 ( date2 );
                Message.printStatus(2, routine, "Adjusting output time series start to " + date2 +
                    " to align with " + outputYearType + " year type." );
            }
        }
        
        // Set the units if specified...
        if ( (newUnits != null) && !newUnits.equals("") ) {
            newTS.setDataUnits( newUnits );
        }
        
        if ( !createData ) {
            return newTS;
        }

        // Finally allocate data space.
        newTS.allocateDataSpace();

        // Currently it is not possible to change interval from regular to
        // irregular. ( these might be implemented later to get, e.g., annual peak flows with dates )
        if (newTS.getDataIntervalBase() == TimeInterval.IRREGULAR) {
            warning = "Change intervals from regular to irregular time series is not supported.";
            throw new IllegalArgumentException(warning);
        }

        // Debugging messages.
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if (Message.isDebugOn) {
            Message.printStatus(2, routine, "oldTS Identifier = " + oldTS.getIdentifier() );
            Message.printStatus(2, routine, "NewDataType = " + newDataType );
            Message.printStatus(2, routine, "newtsBase = " + newtsBase);
            Message.printStatus(2, routine, "newtsMultiplier = " + newtsMultiplier);
            Message.printStatus(2, routine, "new_interval = " + newInterval);

            Message.printStatus(2, routine, "newTS.getIdentifier() = " + newTS.getIdentifier());
            Message.printStatus(2, routine, "newTS.getDataType() = " + newTS.getDataType());
            Message.printStatus(2, routine, "newTS.getDataIntervalBase() = " + newTS.getDataIntervalBase());
            Message.printStatus(2, routine, "newTS.getDataIntervalMult() = " + newTS.getDataIntervalMult());
        }
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        // Check if the intervals are multiples of each other.
        // The intervalRelation is in fact the number of intervals of one ts in
        // a single interval of the other for conversion that does not involve
        // IRREGULAR time series. If the old interval is smaller than the newer
        // interval the intervalRelation is negative. If the old interval is
        // greater than the new interval the intervalRelation is positive.
        // If the intervals are not multiple of each other than the
        // intervalRelation is 0. If the older time series is IRREGULAR the
        // intervalRelation is -1. Cases where both time series are IRREGULAR
        // or the newer time series is IRREGULAR should not happen because they
        // are not supported and the code should throw an exception before get here.
        int intervalRelation = getIntervalRelation(oldTS, newTS);
        if (intervalRelation == 0) {
            warning = "Intervals are not multiples of each other.  Cannot change interval.";
            throw new IllegalArgumentException(warning);
        }

        // Using AllowMissingPercent to get allowMissingCount_int
        // If the AllowMissingPercent property was given (is not the initial -1 value), get the its value.
        // Indicate the percent of missing values that can
        // be missing in the input data and still allow computation of the result.
        
         if ( allowMissingPercent >= 0.0 ) {
             // Compute the number of missing data points allowed per interval
             // based on the AllowMissingPercent property value.
             // Notice: Because the intervalRelation can be negative to indicate
             // that the old interval is less than the new interval,
             // we need to use the abs(intervalRelation) to properly get
             // a positive number of allowed missing values.
             allowMissingCount = (int)(Math.abs(intervalRelation) * allowMissingPercent/100.0);
             if ( (allowMissingCount > 0) && (allowMissingConsecutive == 0) ) {
                 // Default to number of missing
                 allowMissingConsecutive = allowMissingCount;
             }
             setAllowMissingCount ( allowMissingCount );
         }
         
        // Define the OldTS Iterator
        TSIterator oldTSi = null;
        oldTSi = oldTS.iterator(oldTS.getDate1(), oldTS.getDate2());
        oldTSi.setBeginTime(oldTS.getDate1());

        // Define the NewTS Iterator
        TSIterator newTSi = null;
        newTSi = newTS.iterator();

        // Set the iterator of the new time series to be the first data point
        // possible to be computed, if needed.
        // TODO SAM 2007-03-01 Evaluate use of the following
        // DateTime newTSAdjustedStartDate = newTS.getDate1();
        if (intervalRelation < 0) {
            // Older interval < than newer interval
            while (oldTSi.getDate().greaterThan(newTSi.getDate())) {
                newTSi.next();
            }
        }
        else {
            // Older interval >= than newer interval
            while (newTSi.getDate().lessThan(oldTSi.getDate())) {
                newTSi.next();
            }
        }
        newTSi.setBeginTime(newTSi.getDate());

        // From this point on, do not run the next() method for either the old
        // or the new time series. Let the helper methods deal with the
        // iterations starting from the beginning.

        // Debugging messages.
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Message.printStatus(2, routine," ts" + "\t" + "data1     " + "\t" + "date      " + "\t" + "date2     ");
        status = " Old" + "\t" + oldTS.getDate1() + "\t" + oldTSi.getDate() + "\t" + oldTS.getDate2();
        Message.printStatus(2, routine, status);
        status = " New" + "\t" + newTS.getDate1() + "\t" + newTSi.getDate() + "\t" + newTS.getDate2();
        Message.printStatus(2, routine, status);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        // Processing all the different change interval options.
        boolean returnTS = false;

        if ( (newtsBase == TimeInterval.YEAR) && (outputYearType != YearType.CALENDAR) ) {
            // Special case - handle with simpler code that does not utilize all the parameters
            // The old interval must be Day or Month
            if ( (oldTS.getDataIntervalBase() != TimeInterval.DAY) &&
                (oldTS.getDataIntervalBase() != TimeInterval.MONTH) &&
                (oldTS.getDataIntervalMult() != 1) ) {
                warning = "Conversion using output year type \"" + outputYearType +
                "\" is only available for daily and monthly input time series.";
                throw new IllegalArgumentException(warning);
            }
            if ( (newTimeScale != TimeScaleType.ACCM) && (newTimeScale != TimeScaleType.MEAN) ) {
                warning = "Conversion using output year type \"" + outputYearType +
                "\" is only available for an output time scale of " + TimeScaleType.ACCM + " and " +
                TimeScaleType.MEAN + ".";
                throw new IllegalArgumentException(warning);
            }
            changeIntervalToDifferentYearType ( oldTS, newTS, newTimeScale, outputYearType, allowMissingCount,
                allowMissingConsecutive );
            returnTS = true;
        }
        else if (oldTS.getDataIntervalBase() == TimeInterval.IRREGULAR) {
            // -------------------------------------------------------------
            // From IRREGULAR ACCM, MEAN and INST input TS use routine
            // changeInterval_fromIRREGULAR
            // IRREGULAR ACCM to ACCM
            // IRREGULAR ACCM to MEAN ( Not supported, maybe not feasible )
            // IRREGULAR ACCM to INST ( Not supported, maybe not feasible )
            // IRREGULAR MEAN to ACCM ( Not supported, maybe not feasible )
            // IRREGULAR MEAN to MEAN
            // IRREGULAR MEAN to INST ( Not supported, maybe not feasible )
            // IRREGULAR INST to ACCM ( Not supported, maybe not feasible )
            // IRREGULAR INST to MEAN
            // IRREGULAR INST to INST
            // -------------------------------------------------------------
            // These are the supported combinations...
            if (((oldTimeScale == TimeScaleType.ACCM) && (newTimeScale == TimeScaleType.ACCM)) ||
                ((oldTimeScale == TimeScaleType.MEAN) && (newTimeScale == TimeScaleType.MEAN)) ||
                ((oldTimeScale == TimeScaleType.INST) && (newTimeScale == TimeScaleType.MEAN)) ||
                ((oldTimeScale == TimeScaleType.INST) && (newTimeScale == TimeScaleType.INST))) {
                if (changeInterval_fromIRREGULAR(oldTSi, newTSi, oldTimeScale, newTimeScale,
                    handleMissingInputHow)) {
                    returnTS = true;
                }
            }
            else {
                // Everything else is not supported...
                warning = "Conversion from \"" + oldTimeScale + "\" to \"" + newTimeScale + "\" is not supported!";
                throw new IllegalArgumentException(warning);
            }
        }
        else {
            // Depending on the scale, different methods are used
            if ( (oldTimeScale == TimeScaleType.MEAN) || (oldTimeScale == TimeScaleType.ACCM) ) {
                // -----------------------------------------------------
                // From REGULAR ACCM and MEAN input TS use routine
                // changeInterval_toMEANorACCM
                // REGULAR ACCM to MEAM
                // REGULAR MEAN to MEAN
                // REGULAR ACCM to ACCM
                // REGULAR MEAN to ACCM
                // REGULAR ACCM to INST ( Not supported )
                // REGULAR MEAN to INST
                // -----------------------------------------------------
                if ( (newTimeScale == TimeScaleType.MEAN) || (newTimeScale == TimeScaleType.ACCM) ) {
                    if (changeInterval_toMEANorACCM(oldTSi, newTSi, intervalRelation, oldTimeScale, newTimeScale,
                        handleMissingInputHow, allowMissingCount, allowMissingConsecutive,
                        outputFillMethod, handleEndpointsHow )) {
                        returnTS = true;
                    }
                }
                else if ( newTimeScale == TimeScaleType.INST ) {
                    if (changeInterval_toINST(oldTSi, newTSi, intervalRelation, 
                        oldTimeScale, newTimeScale, handleMissingInputHow, tolerance )) {
                        returnTS = true;
                    }
                }
                else {
                    warning = "Conversion from \"" + oldTimeScale + "\" to \"" + newTimeScale + "\" is not supported!";
                    throw new IllegalArgumentException(warning);
                }
            }
            else if (oldTimeScale == TimeScaleType.INST ) {
                if ( newTimeScale == TimeScaleType.INST ) {
                    // ---------------------------------------------
                    // From REGULAR INST to INST use routine changeInterval_fromINST
                    // ---------------------------------------------
                    if (changeInterval_fromINST(oldTSi, newTSi, intervalRelation, oldTimeScale, newTimeScale,
                        statistic, handleMissingInputHow, allowMissingCount, allowMissingConsecutive) ) {
                        returnTS = true;
                    }
                }
                else if ( newTimeScale == TimeScaleType.MEAN ) {
                    // ---------------------------------------------
                    // From REGULAR INST to MEAN use routine changeInterval_toMEANorACCM
                    // ---------------------------------------------
                    if (changeInterval_toMEANorACCM(oldTSi, newTSi, intervalRelation,
                        oldTimeScale, newTimeScale, handleMissingInputHow, allowMissingCount,
                        allowMissingConsecutive, outputFillMethod, handleEndpointsHow )) {
                        returnTS = true;
                    }
                }
                else {
                    // ---------------------------------------------
                    // From REGULAR INST to ACCM, (not supported)
                    // ---------------------------------------------
                    warning = "Conversion from \"" + oldTimeScale + "\" to \"" + newTimeScale + "\" is not supported!";
                    throw new IllegalArgumentException(warning);
                }
            }
            else {
                warning = "Cannot change interval from time scales \"" + oldTimeScale + "\" to \"" + newTimeScale + "\".";
                throw new IllegalArgumentException(warning);
            }
        }

        if (returnTS) {
            return newTS;
        }

        // To prevent compiler from complaining
        return null;
    }

    /**
     * Change intervals from INST to INST for regular interval time series.
     * @param oldTSi Reference to the iterator object for the old time series.
     * @param newTSi Reference to the iterator object for the new time series.
     * @param intervalRelation ratio of intervals, negative if newTS interval is longer than the oldTS.
     * @param oldTimeScale time scale of the old time series.
     * @param newTimeScale time scale of the new time series.
     * @param handleMissingInputHow Indicates how to treat missing values in the input time series
     * @param allowMissingCount the number of missing values allowed (only used with statistic).
     * @param allowMissingConsecutive only compute the new data value if the number of consecutive missing in the
     * input interval is <= the specified count.
     * @return true if successful or false if an error.
     */
    private boolean changeInterval_fromINST(TSIterator oldTSi, TSIterator newTSi, int intervalRelation,
        TimeScaleType oldTimeScale, TimeScaleType newTimeScale, TSStatisticType statistic,
        TSUtil_ChangeInterval_HandleMissingInputHowType handleMissingInputHow, int allowMissingCount,
        int allowMissingConsecutive )
        throws Exception {
        String routine = "TSUtil.changeInterval_fromINST", warning;

        // Make sure the required conversion is supported
        if ( (oldTimeScale == TimeScaleType.INST) && (newTimeScale == TimeScaleType.INST) ) {
            // Recognized combination
        }
        else {
            warning = "Conversion from \"" + oldTimeScale + "\" to \"" + newTimeScale + "\" is not supported!";
            throw new IllegalArgumentException(warning);
        }

        if (intervalRelation < 0) {
            // The old interval is shorter than the new interval
            // Loop through the new time series

            TS newTS = newTSi.getTS();
            TS oldTS = oldTSi.getTS();

            double newMissing = newTS.getMissing();
            double oldMissing = oldTS.getMissing();
            double currentValue, lastValue = oldMissing;

            // The first next() call does not increment the date.
            TSData oldData = oldTSi.next();
            double [] statisticSample = new double[500]; // Sample when computing statistic (guess at size)
            int statisticSampleSize = 0; // For statistic - sample size from input (non-missing)
            int statisticSampleMissingCount = 0; // For statistic - count of missing excluded from sample size
            int nMissingConsecutive = 0; // Number of consecutive missing values in input interval
            int nMissingConsecutiveMax = 0; // Maximum number of consecutive missing values in input interval
            boolean previousIsMissing = false; // Whether previous value in input interval was missing
            boolean okToCompute = true; // Whether an output interval value can be computed
            for (; newTSi.next() != null;) {

                currentValue = newMissing;
                statisticSampleSize = 0;
                statisticSampleMissingCount = 0;
                nMissingConsecutive = 0;
                nMissingConsecutiveMax = 0;
                previousIsMissing = false;
                
                // Just use the last recorded instantaneous old currentValue within the new interval
                while (oldData != null && oldTSi.getDate().lessThanOrEqualTo(newTSi.getDate())) {

                    // TODO SAM 2010-03-24 Should interpolation be allowed?
                    // Should carry forward on input be allowed (seems to be implied by replaceDataValue() call
                    // below.
                    if ( statistic == null ) {
                        // Legacy behavior is to pick off values...assign value only if dates are equal
                        if (oldTSi.getDate().equals(newTSi.getDate())) {
                            currentValue = oldTSi.getDataValue();
                        }
                    }
                    else {
                        // Computing a statistic so need to get all input values...
                        currentValue = oldTSi.getDataValue();
                    }

                    // Replace missing data in currentValue as per handleMissingHow parameter:
                    // by newMissing if REGULAR data;
                    // by 0 if ALERT INCREMENT data and
                    // by lastValue if ALERT REGULAR.
                    if (oldTS.isDataMissing(currentValue)) {
                        currentValue = replaceDataValue(handleMissingInputHow, lastValue, newMissing);
                    }
                    else {
                        // Current value was not missing so save it as the last non-missing value,
                        // for use in calls in the above clause.
                        lastValue = currentValue;
                    }
                    
                    //Message.printStatus(2,routine,"" + oldTSi.getDate() + " currentValue=" + currentValue );
                    
                    // If a statistic is being computed, save each of the old values as they are iterated...
                    if ( statistic != null ) {
                        // Save the value in the sample, but only if not missing
                        if ( oldTS.isDataMissing(currentValue) ) {
                            // Keep track of missing count and don't add to sample
                            ++statisticSampleMissingCount;
                            // Also check for consecutive missing...
                            if ( previousIsMissing ) {
                                // Increment the consecutive missing count
                                ++nMissingConsecutive;
                            }
                            else {
                                // Restart the consecutive count
                                nMissingConsecutive = 1;
                            }
                            // Keep track of the longest consecutive missing streak in the input interval
                            nMissingConsecutiveMax = Math.max(nMissingConsecutive, nMissingConsecutiveMax);
                            // Set for the next loop
                            previousIsMissing = true;
                            // ... end check for consecutive missing
                        }
                        else {
                            // Not missing so add to sample
                            if ( statisticSampleSize == statisticSample.length ) {
                                // Increase the size of the array to handle more values
                                double [] statisticSampleNew = new double[statisticSample.length + 100];
                                // Copy old sample into larger array..
                                System.arraycopy(statisticSample, 0, statisticSampleNew, 0, statisticSample.length);
                                // Reset the sample to the new array
                                statisticSample = statisticSampleNew;
                            }
                            //Message.printStatus(2,routine,"Sample[" + statisticSampleSize +"]=" + currentValue );
                            statisticSample[statisticSampleSize++] = currentValue;
                        }
                    }

                    // Increment the old data pointer to the next value...
                    oldData = oldTSi.next();
                }

                if (Message.isDebugOn) {
                    warning = "Old TS: " + oldTSi.getDate() + " -> " + currentValue +
                        "  New TS: " + newTSi.getDate();
                    Message.printDebug(40, routine, warning);
                }
                if ( statistic == null ) {
                    // Simple assignment based on picking a smaller interval value at the
                    // matching date/time for the new longer interval
                    newTS.setDataValue(newTSi.getDate(), currentValue);
                }
                else {
                    // Compute the statistic based on the sample
                    double statisticValue = newMissing;
                    //Message.printStatus (2,routine,"Calculating max with sample size=" + statisticSampleSize +
                    //        " statisticSampleMissingCount=" + statisticSampleMissingCount +
                     //       " allowMissingCount=" + allowMissingCount);
                    okToCompute = true;
                    if ( statisticSampleSize == 0 ) {
                        // No data...
                        okToCompute = false;
                    }
                    else if ( statisticSampleMissingCount > allowMissingCount ) {
                        // Too much missing data...
                        okToCompute = false;
                    }
                    else if ( nMissingConsecutiveMax > allowMissingConsecutive ) {
                        // Too many consecutive missing data...
                        okToCompute = false;
                    }
                    if ( okToCompute ) {
                        // Have enough values to compute the sample.
                        if ( statistic == TSStatisticType.MAX ) {
                            statisticValue = MathUtil.max(statisticSampleSize, statisticSample);
                        }
                        else if ( statistic == TSStatisticType.MIN ) {
                            statisticValue = MathUtil.min(statisticSampleSize, statisticSample);
                        }
                    }
                    newTS.setDataValue(newTSi.getDate(), statisticValue );
                }
            }
        }
        else {
            // The old interval is longer than the new interval.
            // Passing true as the last parameter, meaning that it is not
            // necessary to consider where to time stamp the data because
            // this method deals only with instantaneous data.
            return changeIntervalFromInstByInterpolation(oldTSi, newTSi, intervalRelation,
                oldTimeScale, newTimeScale, handleMissingInputHow, true);
        }
        return true;
    }

    /**
     * Change intervals from INST time series by interpolation. This method should only be used when
     * converting from larger to shorter interval where interpolation makes sense.  This method should
     * only be called from TSUtil.changeInterval_fromINST and TSUtil.changeInterval_toMEANorACCM when
     * the old interval < new interval and for the following conversion:
     * <p>
     * <table width=100% cellpadding=10 cellspacing=0 border=2>
     * <tr>
     * <td><b>From method</b></td>
     * <tr>
     * <td><b>From</b></td>
     * <td><b>To</b></td>
     * </tr>
     * <tr>
     * <td><b>changeInterval_fromINST</b></td>
     * <tr>
     * <td><b>INST</b></td>
     * <td><b>INST</b></td>
     * </tr>
     * <tr>
     * <td><b>changeInterval_toMEANorACCM</b></td>
     * <tr>
     * <td><b>INST</b></td>
     * <td><b>MEAN</b></td>
     * </tr>
     * </table>
     * 
     * @param oldTSi Reference to the iterator object for the old time series.
     * @param newTSi Reference to the iterator object for the new time series.
     * @param intervalRelation ratio of intervals, negative if newTS interval is longer oldTS.
     * This parameter is used only for verification.
     * @param oldTimeScale - The time scale of the old time series.
     * This parameter is used only for verification.
     * @param newTimeScale - The time scale of the new time series.
     * This parameter is used only for verification.
     * @param handleMissingInputHow Indicates how to treat missing values in the input TS
     * @param timeStampedAtEnd - This argument is used to time stamp the data at the beginning of the period.
     * If passed as false, the data will be time stamped at end of the interval, otherwise at the beginning.
     * @return true if successful or false if an error.
     */
    private boolean changeIntervalFromInstByInterpolation(TSIterator oldTSi, TSIterator newTSi,
        int intervalRelation, TimeScaleType oldTimeScale, TimeScaleType newTimeScale,
        TSUtil_ChangeInterval_HandleMissingInputHowType handleMissingInputHow,
        boolean timeStampedAtEnd) throws Exception {
        String warning;

        // Make sure the required conversion is supported
        if ( ((oldTimeScale == TimeScaleType.INST) && (newTimeScale == TimeScaleType.INST)) ||
            ((oldTimeScale == TimeScaleType.INST) && (newTimeScale == TimeScaleType.MEAN)) ) {
            // Recognized combinations
        }
        else {
            warning = "Conversion from \"" + oldTimeScale + "\" to \"" + newTimeScale + "\" is not supported!";
            throw new IllegalArgumentException(warning);
        }

        TS newTS = newTSi.getTS();
        TS oldTS = oldTSi.getTS();

        // Make sure the older interval is longer than the newer interval.
        if (intervalRelation < 0) {
            warning = "This method should only be applied to conversions from longer to shorter time intervals!";
            throw new IllegalArgumentException(warning);
        }

        // Old time series interval is indeed longer then the new time series interval.

        double newMissing = newTS.getMissing();
        double oldMissing = oldTS.getMissing();

        boolean previousValueMissing = true;
        boolean currentValueMissing = true;

        double previousDateDouble;
        double currentDateDouble;

        double previousValue;
        double currentValue;

        double lastValue = oldMissing;

        DateTime newTSpreviousDate, newDate;

        double newValue, diffValue, offsetLength, intervalLength;

        // Loop through the older time series then the newer time series

        // Get first value in the old TS
        // This is the first call to next(). It does not increment the date.
        oldTSi.next();

        // Saving the current date as previous date.
        newTSpreviousDate = new DateTime(newTSi.getDate());

        // Get first value in the new TS
        // This is the first call to next(). It does not increment the date.
        newTSi.next(); // Does not increment the date

        previousDateDouble = oldTSi.getDate().toDouble();
        previousValue = oldTSi.getDataValue();

        if (oldTS.isDataMissing(previousValue)) {
            // Replace missing data value:
            // by oldMissing if REGULAR data;
            // by 0 if ALERT INCREMENT data and
            // by lastValue if ALERT REGULAR.
            previousValue = replaceDataValue(handleMissingInputHow, lastValue, oldMissing);
        }
        else {
            lastValue = previousValue;
        }

        // Set the missing flag for the previous value.
        previousValueMissing = oldTS.isDataMissing(previousValue);

        // Just in case we have just one value in the old time series,
        // the loop below will not run, so we need to say that this
        // data point is the current one, instead of the previous, to
        // allow the code after the loop to save this single point.
        currentDateDouble = previousDateDouble;
        currentValue = previousValue;
        currentValueMissing = previousValueMissing;

        // Loop, starting from 2nd data point in old TS until the end
        // of the old time series.
        for (; oldTSi.next() != null;) {

            // Get data value and check and replace missing data
            currentDateDouble = oldTSi.getDate().toDouble();
            currentValue = oldTSi.getDataValue();

            if (oldTS.isDataMissing(currentValue)) {
                // Replace missing data value:
                // by oldMissing if REGULAR data;
                // by 0 if ALERT INCREMENT data and
                // by lastValue if ALERT REGULAR.
                currentValue = replaceDataValue(handleMissingInputHow, lastValue, oldMissing);
            }
            else {
                lastValue = previousValue;
            }

            // Set the missing flag for the current value.
            currentValueMissing = oldTS.isDataMissing(currentValue);

            // Save the first point (missing or previousValue) and advance
            // to the next.
            // TODO [LT] 2005-03-01. Are we sure that this first old data
            // point is aligned with the new data point? Maybe not! Resolve ASAP.
            if (timeStampedAtEnd) {
                newDate = new DateTime(newTSi.getDate());
            }
            else {
                // Previous date (one interval) if >= DAY
                newDate = new DateTime(newTSpreviousDate);
            }

            // Saving
            if (previousValueMissing) {
                newTS.setDataValue(newDate, newMissing);
            }
            else {
                newTS.setDataValue(newDate, previousValue);
            }

            // Saving the current date as previous date.
            newTSpreviousDate = new DateTime(newTSi.getDate());
            newTSi.next();

            // Save the points between the previousValue and
            // current value with missing if any of the bounding
            // values are missing or linear interpolation if both
            // bounding values are non-missing.
            if (previousValueMissing || currentValueMissing) {
                while (newTSi.getDate().lessThan(oldTSi.getDate())) {

                    if (timeStampedAtEnd) {
                        newDate = new DateTime(newTSi.getDate());
                    }
                    else {
                        // Previous date(one interval) if >= DAY
                        newDate = new DateTime(newTSpreviousDate);
                    }

                    // Saving the current date as previous date.
                    newTS.setDataValue(newDate, newMissing);
                    newTSi.next();
                }
            }
            else {
                // Interpolate the new values linearly

                // Get the difference in values.
                diffValue = currentValue - previousValue;

                // Get the length of the interval.
                intervalLength = currentDateDouble - previousDateDouble;

                // Loop through the new TS until the end of the
                // old interval - 1 new interval, computing and
                // saving the interpolated values.
                while (newTSi.getDate().lessThan(oldTSi.getDate())) {

                    // Get date offset of the new TS value.
                    offsetLength = newTSi.getDate().toDouble() - previousDateDouble;

                    // Interpolate.
                    newValue = previousValue + diffValue * (offsetLength / intervalLength);

                    // Save new TS value.
                    if (timeStampedAtEnd) {
                        newDate = new DateTime(newTSi.getDate());
                    }
                    else {
                        // Previous date(one interval) if >= DAY
                        newDate = new DateTime(newTSpreviousDate);
                    }
                    // Saving
                    newTS.setDataValue(newDate, newValue);

                    // Saving the current date as previous date.
                    newTSpreviousDate = new DateTime(newTSi.getDate());
                    newTSi.next();
                }
            }

            // Set the previous values to the current ones
            previousDateDouble = currentDateDouble;
            previousValue = currentValue;
            previousValueMissing = currentValueMissing;
        }

        // Save the last data point ( missing or currentValue )
        if (timeStampedAtEnd) {
            newDate = new DateTime(newTSi.getDate());
        }
        else {
            // Previous date (one interval) if >= DAY
            newDate = new DateTime(newTSpreviousDate);
        }
        // Saving
        if (currentValueMissing) {
            newTS.setDataValue(newDate, newMissing);
        }
        else {
            newTS.setDataValue(newDate, currentValue);
        }

        return true;
    }

    /**
     * Change the interval from irregular to the interval of the new timeseries. Supported conversion are:
     * <p>
     * <table width=100% cellpadding=10 cellspacing=0 border=2>
     * <tr>
     * <td><b>From</b></td>
     * <td><b>To</b></td>
     * </tr>
     * <tr>
     * <td><b>ACCM</b></td>
     * <td><b>ACCM</b></td>
     * </tr>
     * <tr>
     * <td><b>MEAN</b></td>
     * <td><b>MEAN</b></td>
     * </tr>
     * <tr>
     * <td><b>INST</b></td>
     * <td><b>MEAN</b></td>
     * </tr>
     * <tr>
     * <td><b>INST</b></td>
     * <td><b>INST</b></td>
     * </tr>
     * </table>
     * 
     * @param oldTSi Reference to the iterator object for the old time series.
     * @param newTSi Reference to the iterator object for the new time series.
     * @param oldTimeScale The time scale of the old time series. This parameter is used only for verification.
     * @param newTimeScale The time scale of the new time series. This parameter is used only for verification.
     * @param missingValueFlag Indicates how to treat missing values in the input TS
     */
    private boolean changeInterval_fromIRREGULAR(TSIterator oldTSi, TSIterator newTSi,
        TimeScaleType oldTimeScale, TimeScaleType newTimeScale,
        TSUtil_ChangeInterval_HandleMissingInputHowType handleMissingInputHow )
        throws Exception {
        String routine = "TSUtil.changeInterval_fromIRREGULAR", warning;

        // Make sure the required conversion is supported.

        if ( ((oldTimeScale == TimeScaleType.ACCM) && (newTimeScale == TimeScaleType.ACCM)) ||
            ((oldTimeScale == TimeScaleType.MEAN) && (newTimeScale == TimeScaleType.MEAN)) ||
            ((oldTimeScale == TimeScaleType.INST) && (newTimeScale == TimeScaleType.MEAN)) ||
            ((oldTimeScale == TimeScaleType.INST) && (newTimeScale == TimeScaleType.INST)) ) {
            // Recognized combinations
        }
        else {
            warning = "Conversion from \"" + oldTimeScale + "\" to \"" + newTimeScale + "\" is not supported!";
            throw new IllegalArgumentException(warning);
        }

        // Old time series related variables initialization
        TS oldTS = oldTSi.getTS();
        DateTime oldDate = new DateTime(DateTime.DATE_FAST);
        double oldMissing = oldTS.getMissing();
        double oldTSValue = oldMissing;
        double previousOldTsValue = oldMissing;
        double lastValidOldTSValue = oldMissing;

        // New time series related variables initialization
        TS newTS = newTSi.getTS();
        DateTime newDate = new DateTime(DateTime.DATE_FAST);
        double newMissing = newTS.getMissing();
        double newTSValue = newMissing;
        double lastNewTSValue = newMissing;
        int noNewDataPoints = 0;

        // New time series interval base and multiplier.
        int TSBase = newTS.getDataIntervalBase();
        int TSMult = newTS.getDataIntervalMult();

        // Do not execute the conversion for these conditions until the method is fully implemented.
        if ( (newTimeScale != TimeScaleType.INST) && (TSBase >= TimeInterval.DAY) ) {
            warning = "Conversion from Irregular '" + oldTimeScale + "' to regular other than instantaneous'" + newTimeScale + "' (interval>=DAY ) is not yet supported!";
            throw new IllegalArgumentException(warning);
        }

        // Others
        boolean incrementDate = false;
        double sum = 0;
        int pointsInInterval = 0;
        if ( pointsInInterval < 0 ) {
        	// TODO pointsInInterval is unused
        }

        // TODO [LT] 2005-03-02 - Pending documentation.
        boolean first = true;
        // TODO [LT] 2005-06-06 - Comment this was required to proper
        // compute INST date. DELETE ASA convinced.
        // ?? if( newTimeScale == INST ) {
        // ?? first = false;
        // ?? }

        // Highest precision.
        // Since the precision of the oldTS dates can be <, = or > than the
        // precision of newTS dates we need to find out the highest precision
        // between them to pass along for date comparison.
        int highestPrecision = TimeUtil.highestPrecision(oldTS.getDate1(), newTS.getDate1());

        // New and old time series precision.
        // There is no need to make any validity tests here, since all tes
        // int newTSprecision = newTS.getDate1().getPrecision();
        // int oldTSprecision = oldTS.getDate1().getPrecision();

        // Initializing the oldTS and the newTS Data objects.
        TSData oldData = oldTSi.next(); // First call does not increment date
        TSData newData = newTSi.next(); // First call does not increment date
        if (oldData == null) {
            // Should never happen
            warning = "First TSData object from the oldTS returned null!";
            throw new RuntimeException(warning);
        }
        if (newData == null) {
            // Should never happen
            warning = "First TSData object from the newTS returned null!";
            throw new RuntimeException(warning);
        }

        // TODO [LT] 2005-02-18.
        // ??????????????????????????????????????????????????????????????????????
        // At the first time step, make sure we have enough data for the new interval
        DateTime minumumOldDate = newTSi.getDate();
        minumumOldDate.addInterval(TSBase, TSMult * -1);
        oldDate = oldTSi.getDate();

        if (Message.isDebugOn) {
            Message.printDebug(10, routine, " Old initial date is: " + oldDate );
            Message.printDebug(10, routine, " New initial date is: " + newTSi.getDate() );
            Message.printDebug(10, routine, " minumumOldDate   is: " + minumumOldDate );
        }

        while (oldDate.lessThanOrEqualTo(minumumOldDate, highestPrecision)) {
            oldData = oldTSi.next();
            if (oldData != null) {
                oldDate = oldTSi.getDate();
            }
            else {
                // There is no data in the oldTS?
                throw new RuntimeException("There is no data in the oldTS!");
            }
        }

        if (Message.isDebugOn) {
            Message.printDebug(10, routine, " Old adjusted date is: " + oldDate);
        }
        // ??????????????????????????????????????????????????????????????????????

        // While Loop
        while (true) {
            // TODO [LT] 2005-03-06 - This is one of the possible
            // location for the code to make this method general.
            newDate = newTSi.getDate();
            if (oldData != null) {
                oldDate = oldTSi.getDate();
            }

            // Save the previous value
            previousOldTsValue = oldTSValue;
            lastNewTSValue = newTSValue;

            if (oldData != null && oldDate.equals(newDate, highestPrecision)) {

                Message.printWarning(2, "", "= Old date is: " + oldDate);
                Message.printWarning(2, "", "= New date is: " + newDate);

                if (first) {
                    // Do not set data at the first time interval
                    // we do not know for which time scale it was valid
                    first = false;
                }
                else {
                    oldTSValue = oldTSi.getDataValue();
                    if (oldTS.isDataMissing(oldTSValue)) {
                        // Replace missing data value:
                        // by newMissing if REGULAR data;
                        // by 0 if ALERT INCREMENT data and
                        // by previousOldTsValue if ALERT REGULAR.
                        if ( (handleMissingInputHow == TSUtil_ChangeInterval_HandleMissingInputHowType.KEEP_MISSING) ||
                            (handleMissingInputHow == TSUtil_ChangeInterval_HandleMissingInputHowType.REPEAT) ||
                            (handleMissingInputHow == TSUtil_ChangeInterval_HandleMissingInputHowType.SET_TO_ZERO) ) {
                            oldTSValue = replaceDataValue(handleMissingInputHow, previousOldTsValue, newMissing);
                            // TODO [LT] 2005-03-06 - Why MT used oldMissing and not newMissing here?
                        }
                    }

                    // Set new data value
                    newTS.setDataValue(newDate, oldTSValue);
                }

                // Increment the newTS date. Bail out if no new data point
                newData = newTSi.next();
                if (newData == null) {
                    break;
                }

                // Increment the oldTS date
                oldData = oldTSi.next();
            }

            // Current newDateTime is smaller than the oldDateTime -
            // we have to fill in one or more regular 'new' data points
            // in this irregular 'old' interval
            // TODO [LT] Does this EqualTo ever get executed, considering the equals() test above?
            else if (oldData != null && oldDate.greaterThanOrEqualTo(newDate, highestPrecision)) {
                if (first) {
                    first = false;
                    newData = newTSi.next();
                    if (newData == null) {
                        break;
                    }
                }
                else {
                    // Get the irregular data value at the current old TS date
                    oldTSValue = oldTSi.getDataValue();

                    Message.printWarning(2, "", " >= Old date is: " + oldDate);
                    Message.printWarning(2, "", " >= New date is: " + newDate);

                    // Just to make sure to capture missing data in the irregularTS
                    while (oldTS.isDataMissing(oldTSValue)) {
                        // Increment irregular 'old' date
                        oldData = oldTSi.next();
                        Message.printWarning(2, "", " >= Old date is: " + oldDate);
                        // If there is a data point, get value also
                        // fill new TS with data in that interval
                        if (oldData != null) {
                            oldTSValue = oldTSi.getDataValue();
                            oldDate = oldTSi.getDate();
                        }
                        else {
                            // End of data, need a value
                            oldTSValue = previousOldTsValue;
                            break;
                        }
                    }

                    // Find out how many regular intervals will be in the
                    // irregular interval. This return 1 even when less than
                    // one interval fits between the dates!!
                    // i.e. if newDate is xx:xx:xx 6:00 and old date is
                    // xx:xx:xx 6:45 and the new interval is 1 hour, this will return 1
                    noNewDataPoints = TimeUtil.getNumIntervals(newDate, oldDate, TSBase, TSMult) + 1;
                    Message.printWarning(2, "", "# of interv: " + noNewDataPoints);
                    Message.printWarning(2, "", "New date is: " + newDate);

                    // ??????????????????????????????????????????????????????
                    // TODO [LT] 2005-02-28 We may need to have something
                    // double considering the real number of
                    // intervals from the previous oldTsDate to
                    // the oldDate. Doing the way it is done
                    // here it is looking like we are losing
                    // information every time we move from one
                    // old data point to the other, when the
                    // new data point is in between.
                    // ??????????????????????????????????????????????????????

                    // Write to the new regular TS: this is only executed
                    // when more than one new interval in old interval
                    for (int i = 0; i < noNewDataPoints; i++) {
                        switch (handleMissingInputHow) {
                        case SET_TO_ZERO: // ALERT_INCREMENT
                        case REPEAT: // ALERT_REGULAR
                            // Take care of ALERT type data set to
                            // respective 'missing' value if dates are not equal.
                            if (oldDate.equals(newDate, highestPrecision)) {
                                newTSValue = oldTSValue;
                                // ....2........4.......1.....2
                                // ......x......4......x......2
                            }
                            else {
                                // .............4.............2
                                // 0000 00000000 0000000 00000
                                // .............4444444 4444442
                                // Replace missing data value:
                                // by 0 if ALERT INCREMENT data
                                // by previousOldTsValue if ALERT REGULAR.
                                newTSValue = replaceDataValue(handleMissingInputHow, previousOldTsValue, newTSValue);
                                    // This last value is not used, because we are processing cases 1 & 2 only
                            }
                            break;
                        case KEEP_MISSING: // REGULAR
                            // Regular data
                            switch (newTimeScale) {
                            case ACCM:
                                // Accumulated
                                newTSValue = oldTSValue / noNewDataPoints;
                                break;
                            case MEAN:
                                // Mean
                                newTSValue = oldTSValue;
                                break;
                            default:
                                // Includes instantaneous interpolate
                                if (previousOldTsValue != oldMissing) {
                                    pointsInInterval++;
                                    newTSValue = previousOldTsValue + (i + 1) / (double) noNewDataPoints * (oldTSValue - previousOldTsValue);

                                    // ??????????????????????????????????????????????????????
                                    // TODO [LT] 2005-02-28 We may need to have something
                                    // considering the real number of
                                    // intervals from the previous oldTsDate to
                                    // the oldDate. Doing the way it is done
                                    // here it is looking like we may lose
                                    // information when moving from one
                                    // old data point to the other, when the
                                    // new data point is in between.
                                    // Here we interpolate from the
                                    // previousOldTsValue, using the
                                    // noNewDataPoints that was computed from
                                    // the newDate which may not coincide with
                                    // the date of the previousOldTsValue.
                                    // ??????????????????????????????????????????????????????

                                }
                                else {
                                    // No last data point to interpolate use last known value
                                    newTSValue = oldTSValue;
                                }
                            }
                            break;
                        default: // KEEP MISSING (-1)
                            if (oldDate.equals(newDate, highestPrecision)) {
                                newTSValue = oldTSValue;
                            }
                            else {
                                newTSValue = newMissing;
                            }
                        }

                        if (oldDate.greaterThan(newDate, highestPrecision)) {

                            // Set new data value
                            newTS.setDataValue(newDate, newTSValue);
                            // Increment date
                            incrementDate = true;
                            newData = newTSi.next();
                            // Bail out if no new data point
                            if (newData == null) {
                                break;
                            }
                            newDate = newTSi.getDate();
                        }

                        // We have an offset in the interval dates
                        // i.e. oldDate = 1:30, newDate = 1:35
                        // use the last old value
                        else {
                            if ( (handleMissingInputHow == TSUtil_ChangeInterval_HandleMissingInputHowType.SET_TO_ZERO) ||
                                (handleMissingInputHow == TSUtil_ChangeInterval_HandleMissingInputHowType.REPEAT) ) {
                                // ALERT_INCREMENT and
                                // ALERT REGULAR
                                incrementDate = false;
                                // newTSValue = oldTSValue;
                            }
                            else {
                                // REGULAR and KeepMissing
                                // Set new data value
                                newTS.setDataValue(newDate, newTSValue);
                                // Increment date
                                incrementDate = true;
                                newData = newTSi.next();
                                // Bail out if no new data point
                                if (newData == null) {
                                    break;
                                }
                                newDate = newTSi.getDate();
                            }
                        }
                    } // End of the for loop

                    // Increment the oldTS date
                    if (incrementDate) {
                        oldData = oldTSi.next();
                    }
                } // End of the "if (first)" test.
            }

            // Here the oldTSdate is smaller than the newTSDate - we have to work on the old data
            // TODO [LT] Does this EqualTo ever get executed, considering
            // the equals() test above the previous test?
            else {
                // oldTSi->getDate()<newTSi->getDate() or OldData==NULL

                Message.printWarning(2, "", "< Old date is: " + oldDate);
                Message.printWarning(2, "", "< New date is: " + newDate);

                // Loop through the old irregular intervals until the
                // current oldDateTime is larger or equal to the newDateTime
                sum = 0.0;
                int i = 0;
                while (oldData != null && oldDate.lessThanOrEqualTo(newDate, highestPrecision)) {

                    // Get data value
                    oldTSValue = oldTSi.getDataValue();

                    // Take care of missing data, i.e do not sum up
                    if (!oldTS.isDataMissing(oldTSValue)) {
                        i++;
                        sum += oldTSValue;
                        lastValidOldTSValue = oldTSValue;
                    }

                    // Increment the oldTS date
                    oldData = oldTSi.next();

                    if (oldData != null) {
                        oldDate = oldTSi.getDate();
                    }
                }

                // ?????????????????????????????????????????????????????
                // TODO [LT] 2005-02-28 Here we should find out the
                // proportional value for ACCM, MEAN anD the interpolation interval.
                // newTSValue = sum/(iLeft + i + iRight);
                // ?????????????????????????????????????????????????????

                // Now the oldDateTime is larger than the newDateTime
                // process the old data in this interval

                if (i > 0) {
                    // Means we have non missing old data

                    switch (newTimeScale) {
                    case ACCM:
                        // Accumulated
                        newTSValue = sum;
                        break;
                    case MEAN:
                        // Mean
                        newTSValue = sum / i;
                        break;
                    default:
                        // Includes instantaneous. Set to last non missing value in interval
                        newTSValue = lastValidOldTSValue;
                    }
                }

                else {
                    // There is no old data for this new TS interval. The value for this new TS data
                    // point would be missing, but we allow it to be changed, if ALERT type data is used

                    // Replace missing data value:
                    // by 0 if ALERT INCREMENT data;
                    // by lastNewTSValue if ALERT REGULAR and
                    // by newMissing if REGULAR data.
                    if ( (handleMissingInputHow == TSUtil_ChangeInterval_HandleMissingInputHowType.KEEP_MISSING)||
                        (handleMissingInputHow == TSUtil_ChangeInterval_HandleMissingInputHowType.REPEAT)||
                        (handleMissingInputHow == TSUtil_ChangeInterval_HandleMissingInputHowType.SET_TO_ZERO) ) {
                        newTSValue = replaceDataValue(handleMissingInputHow, lastNewTSValue, newMissing);
                    }
                    else {
                        // TODO [LT] 2005-03-05 - Same as using the replaceDataValue() method.
                        // By default with missingValueFlag 0 or
                        // -1, the data is replaced by the 3rd parameter which is newMissing.
                        // This else{} block should not be required!
                        newTSValue = newMissing;
                    }
                }

                // Set new data value
                // TODO [LT] 2005-03-06 - This is one of the possible
                // location for the code to make this method general.
                newTS.setDataValue(newDate, newTSValue);

                // Increment the interval
                newData = newTSi.next();

                // Bail out if no new data point
                if (newData == null) {
                    break;
                }
            }
        }
        return true;
    }

    /**
     * Change intervals to INST time series. Call only from TSUtil.changeInterval! Supported conversion are:
     * <p>
     * <table width=100% cellpadding=10 cellspacing=0 border=2>
     * <tr>
     * <td><b>From</b></td>
     * <td><b>To</b></td>
     * </tr>
     * <tr>
     * <td><b>MEAN</b></td>
     * <td><b>INST</b></td>
     * </tr>
     * </table>
     *
     * @param oldTSi Reference to the iterator object for the old time series.
     * @param newTSi Reference to the iterator object for the new time series.
     * @param intervalRelation ratio of intervals, negative if newTS interval is longer than the oldTS.
     * @param oldTimeScale the time scale of the old time series.
     * @param newTimeScale the time scale of the new time series.
     * @param handleMissingInputHow Indicates how to treat missing values in the input time series.
     * @param tolerance used to detect convergence
     * @return true if successful or false if an error.
     */
    private boolean changeInterval_toINST(TSIterator oldTSi, TSIterator newTSi, int intervalRelation,
            TimeScaleType oldTimeScale, TimeScaleType newTimeScale,
            TSUtil_ChangeInterval_HandleMissingInputHowType handleMissingInputHow, double tolerance )
        throws Exception {
        String routine = "TSUtil.changeInterval_toINST", warning;

        // Make sure the required conversion is supported
        if ( (oldTimeScale == TimeScaleType.MEAN) && (newTimeScale == TimeScaleType.INST) ) {
            // Recognized combination
        }
        else {
            warning = "Conversion from \"" + oldTimeScale + "\" to \"" + newTimeScale + "\" is not supported!";
            throw new IllegalArgumentException(warning);
        }

        if (intervalRelation > 0) {
            // The old interval is longer than the new interval.
            // Loop through the old time series.
            // To clarify which TS each variable is referring to, variables
            // will either start with "old" or "new" so the variable
            // oldCurrentValue isn't as strange as it sounds - it's just the currentValue of the old TS.
            TS newTS = newTSi.getTS();
            TS oldTS = oldTSi.getTS();
            int oldIntervalBase = oldTS.getDataIntervalBase();
            int oldIntervalMult = oldTS.getDataIntervalMult();
            int newIntervalBase = newTS.getDataIntervalBase();
            int newIntervalMult = newTS.getDataIntervalMult();
            int newPrecision = newTS.getDate1().getPrecision();
            double newMissing = newTS.getMissing();
            double oldMissing = oldTS.getMissing();
            boolean oldPreviousValueMissing = true;
            boolean oldCurrentValueMissing = true;
            boolean oldNextValueMissing = true;
            double oldPreviousValue;
            double oldCurrentValue;
            double oldNextValue;
            double oldLastValue = oldMissing;
            DateTime oldTSPreviousDate, oldTSCurrentDate, oldTSNextDate;
            DateTime oldTSDate2 = oldTS.getDate2();

            // This is the first call to next().
            // Does not increment the date.
            newTSi.next();
            oldTSi.next();

            oldPreviousValue = oldTSi.getDataValue();
            if (oldTS.isDataMissing(oldPreviousValue)) {
                oldPreviousValue = replaceDataValue(handleMissingInputHow, oldLastValue, oldMissing);
            }
            else {
                oldLastValue = oldPreviousValue;
            }

            // Set the missing flag for the previous value.
            oldPreviousValueMissing = oldTS.isDataMissing(oldPreviousValue);

            // Just in case we have just one value in the old time series,
            // the loop below will not run, so we need to say that this
            // data point is the current one, instead of the previous, to
            // allow the code after the loop to save this single point.
            oldCurrentValue = oldPreviousValue;
            oldCurrentValueMissing = oldPreviousValueMissing;
            oldTSCurrentDate = new DateTime(oldTSi.getDate());
            oldLastValue = oldCurrentValue;

            // Set first data value in newTS
            newTS.setDataValue(new DateTime(newTSi.getDate()), oldPreviousValue);
            newTSi.next();

            // Loop, starting from 1st data point in old TS until the end of the old time series.
            //while (!oldTSi.isLastDateProcessed()) {
            while (!oldTSi.isIterationComplete() ) {
                Message.printDebug (20, routine, "Working on old date: " + oldTSi.getDate());
                oldTSCurrentDate = new DateTime(oldTSi.getDate());
                oldTSPreviousDate = new DateTime(oldTSCurrentDate);
                oldTSPreviousDate.subtractInterval(oldIntervalBase, oldIntervalMult);
                oldTSNextDate = new DateTime(oldTSCurrentDate);
                oldTSNextDate.addInterval(oldIntervalBase, oldIntervalMult);
                // we can't be trying to assign data to newTS past it's end-point
                if ( oldTSNextDate.greaterThan(oldTSDate2))
                    oldTSNextDate = new DateTime (newTS.getDate2());

                // Get data values and check and replace missing data
                oldPreviousValue = oldCurrentValue;
                oldCurrentValue = oldTSi.getDataValue();
                if (oldTS.isDataMissing(oldCurrentValue)) {
                    oldCurrentValue = replaceDataValue(handleMissingInputHow, oldLastValue, oldMissing);
                }
                else {
                    oldLastValue = oldCurrentValue;
                }

                oldNextValue = oldTS.getDataValue(oldTSNextDate);
                if ( oldTS.isDataMissing(oldNextValue)) {
                    oldNextValue = replaceDataValue(handleMissingInputHow, oldLastValue, oldMissing);
                }

                // Set the missing flag for the current value.
                oldCurrentValueMissing = oldTS.isDataMissing(oldCurrentValue);
                oldPreviousValueMissing = oldTS.isDataMissing(oldPreviousValue);
                oldNextValueMissing = oldTS.isDataMissing(oldNextValue);

                // Save the points between the previousValue and
                // current value with missing if any of the bounding
                // values are missing or linear interpolation if both
                // bounding values are non-missing.
                if (oldCurrentValueMissing) {
                    while (newTSi.getDate().lessThan(oldTSNextDate)) {
                        newTS.setDataValue(newTSi.getDate(), newMissing);
                        newTSi.next();
                    }
                }
                else if ( oldPreviousValueMissing || oldNextValueMissing ) {
                    while (newTSi.getDate().lessThanOrEqualTo(oldTSNextDate)) {
                        newTS.setDataValue(newTSi.getDate(), oldCurrentValue);
                        newTSi.next();
                    }
                }
                else if ( Math.abs( oldNextValue - oldCurrentValue ) < tolerance &&
                           Math.abs( oldPreviousValue - oldCurrentValue ) < tolerance) {
                    while (newTSi.getDate().lessThanOrEqualTo(oldTSNextDate)) {
                        newTS.setDataValue(newTSi.getDate(), oldCurrentValue);
                        newTSi.next();
                    }
                }
                else if ( (oldPreviousValue <= oldCurrentValue && oldCurrentValue <= oldNextValue) ||
                    (oldPreviousValue >= oldCurrentValue && oldCurrentValue >= oldNextValue)) {
                    //
                    // Continuing rise or fall
                    //
                    // Interpolate the new values
                    double startValue, endValue;
                    DateTime startEndPointDate = new DateTime ( oldTSCurrentDate );
                    startEndPointDate.setPrecision(newPrecision);
                    DateTime endEndPointDate = new DateTime ( oldTSNextDate );
                    endEndPointDate.setPrecision(newPrecision);

                    // this works whether we are on a continuing rise or fall
                    startValue = oldCurrentValue - ( .25 * ( oldCurrentValue - oldPreviousValue ));
                    startValue = startValue < 0 ? 0.000001 : startValue;
                    endValue = oldCurrentValue + ( .25 * ( oldNextValue - oldCurrentValue ));
                    endValue = endValue < 0 ? 0.000001 : endValue;
                    changeIntervalToInstByNWSRFSInterpolation ( newTSi,
                        startEndPointDate, startValue,
                        endEndPointDate, endValue,
                        oldPreviousValue, oldCurrentValue, oldNextValue );

                }
                else if ( (oldPreviousValue <= oldCurrentValue && oldCurrentValue >= oldNextValue ) || // peak
                    (oldPreviousValue >= oldCurrentValue && oldCurrentValue <= oldNextValue)) { // trough
                    //
                    // peak or trough
                    //
                    boolean peak = false;
                    if ( oldPreviousValue <= oldCurrentValue && oldCurrentValue >= oldNextValue ) {
                        peak = true;
                    }
                    //
                    // Calculate midpoint date and instantaneous peak or trough value
                    // End-points...
                    double startValue, endValue;
                    DateTime startEndPointDate = new DateTime ( oldTSCurrentDate );
                    startEndPointDate.setPrecision(newPrecision);
                    DateTime endEndPointDate = new DateTime ( oldTSNextDate );
                    endEndPointDate.setPrecision(newPrecision);
                    double preDiff = Math.abs(oldCurrentValue - oldPreviousValue);
                    double postDiff = Math.abs(oldNextValue - oldCurrentValue);

                    // Midpoint...
                    int numIntervals = TimeUtil.getNumIntervals(startEndPointDate, endEndPointDate, newIntervalBase, newIntervalMult);
                    DateTime midpointDate = new DateTime ( startEndPointDate );
                    /*
                    double ratio;
                    int n = numIntervals/2;
                    if ( preDiff > postDiff ) {
                        ratio = postDiff > tolerance ? preDiff/postDiff : preDiff;
                        n += (ratio/2);
                    } else {
                        ratio = preDiff > tolerance ? postDiff/preDiff : postDiff;
                        n -= ratio/2;
                    }
                    midpointDate.addInterval(newIntervalBase, n*newIntervalMult); */
                    midpointDate.addInterval(newIntervalBase, newIntervalMult* (int)(numIntervals*(preDiff/(preDiff+postDiff))));
                    if ( midpointDate.greaterThanOrEqualTo(endEndPointDate )) {
                        midpointDate = endEndPointDate;
                        midpointDate.subtractInterval(newIntervalBase, newIntervalMult);
                    }

                    // Midpoint value...
                    // 1/4 of the average differences between the current mean and the previous and next means
                    double midpointValue = .25 * (( preDiff + postDiff ) / 2.0);
                    midpointValue = peak? oldCurrentValue + midpointValue : oldCurrentValue - midpointValue;
                    midpointValue = midpointValue < 0.0 ? 0.00001 : midpointValue;

                    // this works whether we are on a peak or trough
                    startValue = oldCurrentValue - ( .25 * ( oldCurrentValue - oldPreviousValue ));
                    startValue = startValue < 0.0 ? 0.000001 : startValue;
                    endValue = oldCurrentValue + ( .25 * ( oldNextValue - oldCurrentValue ));
                    endValue = endValue < 0.0 ? 0.000001 : endValue;

                    changeIntervalToInstByInterpolation ( newTSi,
                        startEndPointDate, startValue,
                        midpointDate, midpointValue );
                    changeIntervalToInstByInterpolation ( newTSi,
                        midpointDate, midpointValue,
                        endEndPointDate, endValue );
                } 

                // Set the previous values to the current ones
                oldPreviousValue = oldCurrentValue;
                oldPreviousValueMissing = oldCurrentValueMissing;
                Message.printDebug (5, routine, "Completed new date: " + newTSi.getDate());
                oldTSi.next();
            }

            // carry final value of new time series through to end of old ts.
            /*
            DateTime tmpDate = new DateTime ( oldTSCurrentDate );
            tmpDate.addInterval( oldIntervalBase, oldIntervalMult );
            double tmp = newTSi.getDataValue();
            while (newTSi.getDate().lessThanOrEqualTo(newTS.getDate2())) {
                newTS.setDataValue(newTSi.getDate(), tmp);
                newTSi.next();
            }
             */

            // need to allow user to specify tolerance:
            adjustInstantaneousBasedOnMean ( oldTSi.getTS(), newTSi.getTS(), tolerance );
            return true;
        }
        else {
            warning = "Conversion from \"" + oldTimeScale + "\" to \"" +
                    newTimeScale + "\" is not supported!  To convert to instantaneous, the new interval must be shorter than the old interval.";
            throw new IllegalArgumentException(warning);
        }
    }
    
/**
Change the time series interval to a new output year type.  This is a simpler method that operates only
on daily and monthly interval input; consequently, all input interval data fall nicely in the output interval
without edge effects and zero time issues.
The output year type controls the operation and can only be ACCM (in which case all input values are
accumulated) and MEAN (in which case the input values are averaged).
@param allowMissingConsecutive only compute the new data value if the number of consecutive missing in the
input interval is <= the specified count.
*/
private void changeIntervalToDifferentYearType ( TS oldTS, TS newTS,
    TimeScaleType newTimeScale, YearType outputYearType, int allowMissingCount, int allowMissingConsecutive )
{   String routine = getClass().getName() + ".changeIntervalToDifferentYearType";
    // Create dates that have the correct precision (matching the old time series)
    // and initialize for the first year.  These are used to step through the time series.
    // The iteration year based on the old time series dates may start one year too early but that
    // is OK since the output time series will simply not have a value added out of period.
    DateTime yearStart = new DateTime ( oldTS.getDate1() );
    yearStart.addYear( outputYearType.getStartYearOffset() );
    yearStart.setMonth( outputYearType.getStartMonth() );
    yearStart.setDay( 1 ); // Will not be used if monthly input
    DateTime yearEnd = new DateTime ( yearStart );
    yearEnd.addMonth ( 11 );
    yearEnd.setDay ( TimeUtil.numDaysInMonth(yearEnd.getMonth(), yearEnd.getYear()));
    // Always create a new instance of date for the iterator to protect original data
    int intervalBase = oldTS.getDataIntervalBase();
    int intervalMult = oldTS.getDataIntervalMult();
    double value;
    DateTime newYear = new DateTime(yearStart,DateTime.PRECISION_YEAR);
    newYear.addYear( -1*outputYearType.getStartYearOffset() );
    // Loop until all the new years are processed
    // The new time series period should have been defined to align with the year type
    // Checking the year start will allow any period - with missing values filling in if necessary
    while ( yearStart.lessThanOrEqualTo(newTS.getDate2()) ) {
        // Initialize for the new year
        double sum = 0.0;
        int nMissing = 0;
        int nMissingConsecutive = 0; // Number of consecutive missing values in input interval
        int nMissingConsecutiveMax = 0; // Maximum number of consecutive missing values in input interval
        boolean previousIsMissing = false; // Whether previous value in input interval was missing
        int nNotMissing = 0;
        // Extract data from the old time series for the new year
        // This loops over the full year so that missing values on the end will impact results
        Message.printStatus(2, routine, "Processing old time series for " + yearStart + " to " + yearEnd +
            " to create output year " + newYear );
        for ( DateTime idate = new DateTime(yearStart); idate.lessThanOrEqualTo(yearEnd);
            idate.addInterval(intervalBase,intervalMult)) {
            // Process the data values
            value = oldTS.getDataValue ( idate );
            if ( oldTS.isDataMissing(value) ) {
                ++nMissing;
                // Also check for consecutive missing...
                if ( previousIsMissing ) {
                    // Increment the consecutive missing count
                    ++nMissingConsecutive;
                }
                else {
                    // Restart the consecutive count
                    nMissingConsecutive = 1;
                }
                // Keep track of the longest consecutive missing streak in the input interval
                nMissingConsecutiveMax = Math.max(nMissingConsecutive, nMissingConsecutiveMax);
                // Set for the next loop
                previousIsMissing = true;
                // ... end check for consecutive missing
            }
            else {
                ++nNotMissing;
                sum += value;
            }
        }
        // Save the results - only compute if the allowed number of missing is adhered to
        Message.printStatus(2, routine, "For output year " + newYear + ", sum=" + sum + ", nMissing=" +
            nMissing + ", nNotMissing=" + nNotMissing + ", nMissingConsecutiveMax=" + nMissingConsecutiveMax );
        boolean okToCompute = true;
        if ( nNotMissing == 0 ) {
            // No data in input...
            okToCompute = false;
        }
        else if ( nMissing > allowMissingCount ) {
            // Too many missing in input...
            okToCompute = false;
        }
        else if ( nMissingConsecutiveMax > allowMissingConsecutive ) {
            // Too many consecutive missing...
            okToCompute = false;
        }
        if ( okToCompute ) {
            if ( newTimeScale == TimeScaleType.ACCM ) {
                newTS.setDataValue(newYear, sum );
            }
            else if ( newTimeScale == TimeScaleType.MEAN ) {
                newTS.setDataValue(newYear, sum/nNotMissing );
            }
        }
        // Reset the annual value and increment the dates.
        yearStart.addYear ( 1 );
        // Also reset the day because the number of days in the month may depend on the year
        yearEnd.addYear ( 1 );
        yearEnd.setDay ( TimeUtil.numDaysInMonth(yearEnd.getMonth(), yearEnd.getYear()));
        newYear.addYear ( 1 );
    }
}

    // Every parameter but newTSi refers to information from the old time series.
    // newTSi should be set to the start of the new time series start date.  After
    // routine has run, it will be set at or before nextDate.  Additionally, all checks
    // have been done by this point to ensure no missing data is present.
    private boolean changeIntervalToInstByInterpolation ( TSIterator newTSi,
            DateTime previousDate, double previousValue,
            DateTime nextDate, double nextValue ) {

            String routine = "TSUtil_ChangeInterval.changeIntervalToInstByInterpolation";
            double previousDateDouble = previousDate.toDouble(); 
            double diffValue, intervalLength, offsetLength, newValue;
            DateTime newDate;
            TS newTS = newTSi.getTS();

            // Get the difference in values.
            diffValue = nextValue - previousValue;

            // Get the length of the interval.
            intervalLength = nextDate.toDouble() - previousDate.toDouble();

            // We are creating a ratio:
            // newValue = previousValue + diffValue * (offsetLength / intervalLength);
            // diffValue and intervalLength don't change so let's set that ratio.
            double ratio = diffValue / intervalLength;

            // Loop through the new TS until the end of the
            // old interval - 1 new interval, computing and
            // saving the interpolated values.
            // while ( !newTSi.isLastDateProcessed() && newTSi.getDate().lessThanOrEqualTo(nextDate)) {
            while ( !newTSi.isIterationComplete() && newTSi.getDate().lessThanOrEqualTo(nextDate)) {
                // Get date offset of the new TS value.
                offsetLength = newTSi.getDate().toDouble() - previousDateDouble;

                // Interpolate.
                newValue = previousValue + ( ratio * offsetLength );
                newDate = new DateTime(newTSi.getDate());
                Message.printStatus(10, routine, "Interpolating for " + newDate + ": " + newValue );

                // Saving
                newTS.setDataValue(newDate, newValue);
                newTSi.next();
            }
            
            return true;
    }

    // newTSi should be set to the start of the new time series start date.  After
    // routine has run, it will be set to nextDate.  Additionally, all checks
    // have been done by this point to ensure no missing data is present.
    // This routine creates a curved interpolation by more strongly using the next
    // or previous old values (depending on whether the difference between current
    // and next values is larger or smaller than the difference between current and previous values.
    private boolean changeIntervalToInstByNWSRFSInterpolation ( TSIterator newTSi,
            DateTime previousDate, double previousValue,
            DateTime nextDate, double nextValue,
            double Qinpre, double Qtoday, double Qtom ) {

            String routine = "TSUtil_ChangeInterval.changeIntervalToInstByNWSRFSInterpolation";
            double change, newValue;
            int nintervals;
            DateTime newDate = new DateTime(newTSi.getDate());
            TS newTS = newTSi.getTS();

            // Get the difference in values.
            change = nextValue - previousValue;
            double preDiff = Math.abs(Qtoday - Qinpre);
            double postDiff = Math.abs(Qtom - Qtoday);

            // Get the length of the interval.
            nintervals = TimeUtil.getNumIntervals(previousDate, nextDate, newTS.getDataIntervalBase(), newTS.getDataIntervalMult());

            // We are creating a ratio:
            // newValue = previousValue + 1/(nintervals-n) * change * preDiff/postDiff
            // change, preDiff, postDiff don't change so let's set that ratio.
            double ratio = 1;
            
            // Note that the .01 below is not the same as the tolerance
            if ( preDiff > postDiff ) {
                ratio = preDiff > 0.01 ? change*postDiff/preDiff : change;
            }
            else {
                ratio = postDiff > 0.01 ? change*preDiff/postDiff : change;
            }

            // Loop through the new TS until the end of the
            // old interval - 1 new interval, computing and
            // saving the interpolated values.
            for ( int n=1; n<nintervals; n++ ) {
                // Interpolate.
                if ( preDiff > postDiff ) {
                    newValue = nextValue - ( ratio / n);
                }else {
                    newValue = previousValue + ( ratio / (nintervals - n) );
                }
                newDate = new DateTime(newTSi.getDate());
                Message.printStatus(10, routine, "Date: " + newDate + ": " + newValue );
 
                // Saving
                newTS.setDataValue(newDate, newValue);
                newTSi.next();
            }
            
            if ( newTSi != null ) {
                // set last data value
                newTS.setDataValue(new DateTime(newTSi.getDate()), nextValue);
                newTSi.next();
            }
            return true;
    }

    /**
     * Change intervals for MEAN or ACCM time series. Call only from TSUtil.changeInterval! Supported conversion are:
     * <p>
     * <table width=100% cellpadding=10 cellspacing=0 border=2>
     * <tr>
     * <td><b>From</b></td>
     * <td><b>To</b></td>
     * </tr>
     * <tr>
     * <td><b>ACCM</b></td>
     * <td><b>MEAN</b></td>
     * </tr>
     * <tr>
     * <td><b>MEAN</b></td>
     * <td><b>MEAN</b></td>
     * </tr>
     * <tr>
     * <td><b>INST</b></td>
     * <td><b>MEAN</b></td>
     * </tr>
     * <tr>
     * <td><b>ACCM</b></td>
     * <td><b>ACCM</b></td>
     * </tr>
     * <tr>
     * <td><b>MEAN</b></td>
     * <td><b>ACCM</b></td>
     * </tr>
     * </table>
     * 
     * @param oldTSi Reference to the iterator object for the old time series.
     * @param newTSi Reference to the iterator object for the new time series.
     * @param intervalRelation ratio of intervals, negative if newTS interval is longer oldTS.
     * @param oldTimeScale the time scale of the old time series: MeasTimeScale.INST if the oldTS is an
     * instantaneous, MeasTimeScale.ACCM if the oldTS is an
     * accumulation, MeasTimeScale.MEAN if the oldTS is mean.
     * @param newTimeScale the time scale of the new time series, MeasTimeScale.ACCM if the newTS is an
     * accumulation, MeasTimeScale.MEAN if the newTS is mean.
     * @param handleMissingInputHow Indicates how to treat missing values in the input TS
     * @param maxMissingPerInterval the maximum number of missing value in the old time series allowed per
     * new time series interval. New value will be considered missing if this max value is exceeded.
     * This is only applicable when going from larger intervals to shorter ones.
     * @param allowMissingConsecutive if >= 0, only compute the new data value if the number of
     * consecutive missing in the input interval is <= the specified count.
     * @param outputFillMethod this argument is only used when going from larger intervals to smaller.
     * It allow for the new values to be interpolated (INTERPOLATE) between the old data point,
     * carried forward (CARRYFORWARD) from the 1st data point of the oldTS interval or
     * backward (CARRYBACKWARD) from the last data point of the oldTS time interval.
     * @param handleEndpointsHow when changing from small interval to large interval (but only daily or smaller),
     * indicate how the end-points should be handled.
     * @return true if successful or false if an error.
     */
   private boolean changeInterval_toMEANorACCM(TSIterator oldTSi, TSIterator newTSi, int intervalRelation,
       TimeScaleType oldTimeScale, TimeScaleType newTimeScale,
       TSUtil_ChangeInterval_HandleMissingInputHowType handleMissingInputHow, int maxMissingPerInterval,
       int allowMissingConsecutive, TSUtil_ChangeInterval_OutputFillMethodType outputFillMethod,
       TSUtil_ChangeInterval_HandleEndpointsHowType handleEndpointsHow )
   throws Exception {
        String routine = "TSUtil.changeInterval_toMEANorACCM", warning;
        int dl = 40;

        // Make sure the required conversion is supported - use if statements to filter valid combinations.

        if ( ((oldTimeScale == TimeScaleType.ACCM) && (newTimeScale == TimeScaleType.MEAN) ) ||
            ((oldTimeScale == TimeScaleType.MEAN) && (newTimeScale == TimeScaleType.MEAN) ) ||
            ((oldTimeScale == TimeScaleType.INST) && (newTimeScale == TimeScaleType.MEAN) ) ||
            ((oldTimeScale == TimeScaleType.ACCM) && (newTimeScale == TimeScaleType.ACCM) ) ||
            ((oldTimeScale == TimeScaleType.MEAN) && (newTimeScale == TimeScaleType.ACCM) ) ) {
            // Recognized combinations
        }
        else {
            warning = "Conversion from \"" + oldTimeScale + "\" to \"" + newTimeScale + "\" is not supported!";
            throw new IllegalArgumentException(warning);
        }

        // Declaring time series, interval base and interval multipliers for the
        // newTS and the oldTS time series
        TS newTS = newTSi.getTS();
        int newTSBase = newTS.getDataIntervalBase();
        int newTSMult = newTS.getDataIntervalMult();

        TS oldTS = oldTSi.getTS();
        int oldTSBase = oldTS.getDataIntervalBase();
        int oldTSMult = oldTS.getDataIntervalMult();

        // Define if the old time series values are time stamped at the
        // end of the interval ( interval < DAY ).
        // If the abs(intervalRelation) is 1, we are converting the old time
        // series to a new time series of the same interval. In this case
        // we should not time stamp the new data at the beginning.
        boolean oldTsTimeStampedAtEnd = true;
        // Valid only for ACCM and MEAN when the time step is < DAY and if not
        // converting between the same interval.
        // TODO [LT] Math.abs(intervalRelation) might not be necessary when
        // the whole logic is right. Or maybe. It remains to find out. 2005-03-07.
        if (oldTSBase >= TimeInterval.DAY && Math.abs(intervalRelation) != 1) {
            oldTsTimeStampedAtEnd = false;
        }

        // Same as day but still hourly time series.
        // TODO [LT 2005-03-25]. This test is not necessary. 24HOUR time
        // series is still Hourly time series and so the data should be time stamped at the end.
        // if( oldTSBase == TimeInterval.HOUR && oldTSMult == 24 &&
        // Math.abs(intervalRelation) != 1 ) {
        // oldTsTimeStampedAtEnd = false;
        // }

        // Define if the new time series values should be time stamped at the
        // end of the interval ( interval < DAY ).
        // If the abs(intervalRelation) is 1, we are converting the old time
        // series to an new time series of the same interval. In this case
        // we should not time stamp the new data at the beginning.
        boolean newTsTimeStampedAtEnd = true;
        if (newTSBase >= TimeInterval.DAY && Math.abs(intervalRelation) != 1) {
            newTsTimeStampedAtEnd = false;
        }

        // Quality flag is set to S (no missing values) or (M) if missing
        // values were found and allowed.
        // TODO SAM 2007-03-01 Evaluate how used
        // String qualityFlag = "S";

        // Initialization
        double newMissing = newTS.getMissing();
        double oldMissing = oldTS.getMissing();

        TSData oldData;
        DateTime newDate;
        double lastValue = -999.99, value = oldTS.getMissing(), sum, lastEndpointValue, firstEndpointValue;
        int missingCount, dataCount;
        int nMissingConsecutive; // Number of missing consecutive values in input interval
        int nMissingConsecutiveMax; // Maximum number of missing consecutive values in input interval
        boolean previousIsMissing = false; // Is previous value in input interval missing?
        boolean okToCompute = true; // is it OK to compute the output interval value?
        boolean missingFlag, first_time;

        // Change Interval
        if (intervalRelation < 0) {

            // Old interval < new interval.
            // Loop through the new time series then the old one.

            // This reseting of the oldTsTimeStampedAtEnd to FALSE if the
            // input time scale is INST, should only be used when the
            // input interval is shorter than the output interval.
            if (oldTimeScale == TimeScaleType.INST) {
                oldTsTimeStampedAtEnd = false;
            }

            // The first next() call does not increment the date.
            oldData = oldTSi.next();

            // save data at end point
            value = oldData.getDataValue();
            firstEndpointValue = value;

            // If the old ts is < Day (stamped in the end) we need to advance one old interval.
            if (oldTsTimeStampedAtEnd) {
                oldData = oldTSi.next();
            }

            // Set a variable for how to handle the end points.
            // If the user has chosen to average the end points AND
            // the new time series interval is daily or finer, average the end points.
            boolean averageEndpoints = false;
            if ( (handleEndpointsHow != null) &&
                (handleEndpointsHow == TSUtil_ChangeInterval_HandleEndpointsHowType.AVERAGE_ENDPOINTS) &&
                    ( newTS.getDataIntervalBase() == TimeInterval.DAY ||
                      newTS.getDataIntervalBase() == TimeInterval.HOUR ||
                      newTS.getDataIntervalBase() == TimeInterval.MINUTE ||
                      newTS.getDataIntervalBase() == TimeInterval.SECOND ||
                      newTS.getDataIntervalBase() == TimeInterval.HSECOND )) {
                averageEndpoints = true;
            }

            // Save the previous newTS date.
            DateTime previousNewTSDate = new DateTime(newTSi.getDate());

            for (; newTSi.next() != null;) {
                first_time = true;
                sum = 0.0;
                missingCount = 0;
                dataCount = 0;
                nMissingConsecutive = 0;
                nMissingConsecutiveMax = 0;
                previousIsMissing = false;
                // TODO SAM 2007-03-01 Evaluate use.
                // qualityFlag = "S";

                // If the old time series is < DAY (stamped in the end)
                // we need to iterate until the end of the period, which
                // is the new ts current data point. If the old time
                // series is >= DAY, we only have to iterate until the
                // old data point prior the the current new date.

                // Variable newTS_endDate valid only when the oldTS time
                // interval is < DAY and is not ACCM or MEAN.
                DateTime newTS_endDate = new DateTime(newTSi.getDate());

                // Update the variable newTS_endDate if the oldTS time
                // interval is >= DAY or in any case if INST.
                if (!oldTsTimeStampedAtEnd) {
                    newTS_endDate.addInterval(oldTSBase, -oldTSMult);
                }

                // This variable is used to allow counting of missing
                // values (in the old time series) past the end of the
                // old time series, if the end of the old time series
                // does not coincide with the new time series data point.
                DateTime oldTSiPreviousDate = null;

                // Accumulate the values from the older time series.
                while (oldData != null && (oldTSi.getDate().lessThanOrEqualTo(newTS_endDate))) {
                    // Get and check data value
                    value = oldTSi.getDataValue();
                    missingFlag = false;

                    // Saving the old time series date
                    oldTSiPreviousDate = oldTSi.getDate();

                    if (oldTS.isDataMissing(value)) {
                        // Replace missing data value:
                        // by newMissing if REGULAR data;
                        // by 0 if ALERT INCREMENT data and
                        // by lastValue if ALERT REGULAR.
                        value = replaceDataValue(handleMissingInputHow, lastValue, oldMissing);

                        // Increment the missing data indicator if value is still missing
                        if (oldTS.isDataMissing(value)) {
                            missingCount++;
                            missingFlag = true;
                            // Also check the consecutive missing...
                            if ( previousIsMissing ) {
                                // Increment the consecutive missing count
                                ++nMissingConsecutive;
                            }
                            else {
                                // Restart the consecutive count
                                nMissingConsecutive = 1;
                            }
                            // Keep track of the longest consecutive missing streak in the input interval
                            nMissingConsecutiveMax = Math.max(nMissingConsecutive, nMissingConsecutiveMax);
                            // Set for the next loop
                            previousIsMissing = true;
                            // ... end check for consecutive missing
                        }

                        // Set quality flag to missing to indicate that data was missing.
                        // TODO SAM 2007-03-01 Evaluate how used
                        // qualityFlag = "M";
                    }
                    else {
                        lastValue = value;
                    }

                    if ( first_time ) {
                        firstEndpointValue = oldData.getDataValue();
                        first_time = false;
                    }

                    if (Message.isDebugOn) {
                        warning = "Old TS: " + oldTSi.getDate() + " -> " + value + "  New TS: " + newTSi.getDate();
                        Message.printDebug(10, routine, warning);
                    }

                    // Accumulate if not missing.
                    if (!missingFlag) {
                        sum += value;
                        dataCount++;
                    }

                    oldData = oldTSi.next();
                }

                lastEndpointValue = oldTSi.getDataValue();
                if (oldTS.isDataMissing(lastEndpointValue)) {
                    lastEndpointValue = replaceDataValue(handleMissingInputHow, lastValue, oldMissing);
                }
                if ( averageEndpoints &&
                    !oldTS.isDataMissing(lastEndpointValue) && !oldTS.isDataMissing(firstEndpointValue) ) {
                    // Don't use the first value, use the average of the first and last values (half of each)
                    sum -= firstEndpointValue;
                    sum += (firstEndpointValue+lastEndpointValue)/2.0;
                }

                // Compute the value for the newer time series and assign it.
                // If the new time series is < DAY (stamped at the
                // end) we need to assign the value to the new time series current data point.
                // If the new time series is >= DAY (stamped at the
                // beginning), we need to assign the value to the previous new date.
                if (newTsTimeStampedAtEnd) {
                    // < DAY and it is not INST
                    newDate = new DateTime(newTSi.getDate());
                }
                else {
                    // >= DAY
                    newDate = new DateTime(previousNewTSDate);
                }

                // If the oldTSiPreviousDate is not equal to the
                // newTSi.getDate() that is because we are processing
                // the last interval in the period and the old time
                // series data terminate short than the new time series
                // date. If the end of the old time series coincide
                // with the new time series data point, this variable
                // will be null, so make sure to check for null first.
                if (oldTSiPreviousDate != null && !oldTSiPreviousDate.equals(newTS_endDate)) {
                    for (DateTime dt = new DateTime(oldTSiPreviousDate); dt.lessThanOrEqualTo(newTS_endDate); dt.addInterval(oldTSBase, oldTSMult)) {
                        missingCount++;
                        // Also check the consecutive missing...
                        if ( previousIsMissing ) {
                            // Increment the consecutive missing count
                            ++nMissingConsecutive;
                        }
                        else {
                            // Restart the consecutive count
                            nMissingConsecutive = 1;
                        }
                        // Keep track of the longest consecutive missing streak in the input interval
                        nMissingConsecutiveMax = Math.max(nMissingConsecutive, nMissingConsecutiveMax);
                        // Set for the next loop
                        previousIsMissing = true;
                        // ... end check for consecutive missing
                    }
                }

                // Set the data value for this DateTime point to
                // missing, if the number of missing values is greater
                // than the max allowed or if no non-missing values
                // were found. Otherwise compute the new value either as mean or accumulation.
                okToCompute = true;
                if ( dataCount == 0 ) {
                    // No input interval data
                    okToCompute = false;
                }
                else if ( missingCount > maxMissingPerInterval ) {
                    // Too many missing values in the input interval
                    okToCompute = false;
                }
                else if ( nMissingConsecutiveMax > allowMissingConsecutive ) {
                    // Too many consecutive missing values in the input interval
                    okToCompute = false;
                }
                if ( !okToCompute ) {
                    newTS.setDataValue(newDate, newMissing);
                    if ( Message.isDebugOn ) {
                        Message.printDebug ( dl, routine, "Set new TS " + newDate + " -> " + newMissing +
                            " missingCount=" + missingCount + " dataCount=" + dataCount +
                            ", nMissingConsecutiveMax=" + nMissingConsecutiveMax );
                    }
                    /* newDate, newMissing, qualityFlag, 0 ); */
                    // TODO [LT] 2005-03-01 Quality flag is currently not used.
                }
                else {
                    // If MEAN, divide by the number of points.
                    if (newTimeScale == TimeScaleType.MEAN) {
                        if ( Message.isDebugOn ) {
                            Message.printDebug ( dl, routine, "Set new TS " + newDate + " -> " + sum + "/" +
                                dataCount + "=" + sum/dataCount);
                        }
                        sum /= dataCount;
                    }
                    else {
                        // Sum - no need to divide by the number of points
                        if ( Message.isDebugOn ) {
                            Message.printDebug ( dl, routine, "Set new TS " + newDate + " -> " + sum +
                                " dataCount=" + dataCount );
                        }
                    }

                    // Set data value for this date.
                    // Mean or accumulation.
                    newTS.setDataValue(newDate, sum);

                    // newDate, sum, qualityFlag, 0 );
                    // TODO [LT] 2005-03-01 Quality flag is currently not used.
                }

                // Save current newTS date as the previous newTS date for the next iteration.
                previousNewTSDate = new DateTime(newTSi.getDate());
            }
        }
        else {
            // Old interval > new interval.
            // Loop through the old time series then the new one.

            // For conversion from INST to MEAN, interpolation may be more
            // appropriated than moving the data FORWARD or BACKWARD from the bounding data points.
            if ( (oldTimeScale == TimeScaleType.INST) && (newTimeScale == TimeScaleType.MEAN)
                    && (outputFillMethod == TSUtil_ChangeInterval_OutputFillMethodType.INTERPOLATE) ) {
                return changeIntervalFromInstByInterpolation(oldTSi, newTSi, intervalRelation,
                    oldTimeScale, newTimeScale, handleMissingInputHow, newTsTimeStampedAtEnd);
            }

            // Disable this part of the code, if not working as expected
            // warning = "Conversion from \"" + OldTimeScale + "\" to \"" + NewTimeScale + "\" in reverse is not yet supported!"; throw new TSException ( warning );

            newTSi.next(); // Does not increment the date

            // If the old ts is >= Day (not stamped at the end) we need
            // to advance one old interval, but using the previous old
            // value to compute the values up to but not including the
            // new data point that coincides with the current old data
            // point. So run the first oldTSi.next() before the loop.
            double previousOldValue = oldTSi.getDataValue();
            if (!oldTsTimeStampedAtEnd) {
                oldData = oldTSi.next();
                previousOldValue = oldTSi.getDataValue();
            }

            for (; oldTSi.next() != null;) {
                // Get the old data value to use in the conversion.
                if (oldTsTimeStampedAtEnd) {
                    // < Day
                    value = oldTSi.getDataValue();
                }
                else {
                    // >=Day
                    value = previousOldValue;
                }

                // Check if the old data value is missing and
                // updated it according to the missingValueFlag.
                // TODO SAM 2007-03-01 Evaluate use.
                // qualityFlag = "S";
                missingFlag = false;
                if (oldTS.isDataMissing(value)) {
                    // Set quality flag to missing.
                    // TODO SAM 2007-03-01 Evaluate use.
                    // qualityFlag = "M";
                    // Replace missing data value:
                    // by newMissing if REGULAR data;
                    // by 0 if ALERT INCREMENT data and
                    // by lastValue if ALERT REGULAR.
                    value = replaceDataValue(handleMissingInputHow, lastValue, newMissing);
                    // Set the missing flag to false
                    // if data was left missing
                    if (oldTS.isDataMissing(value)) {
                        missingFlag = true;
                    }
                }
                else {
                    lastValue = value;
                }

                // Compute the date of the last data point of the newTS to assign values to.
                // First the date valid when the oldTS interval is < DAY
                DateTime newTS_endDate = new DateTime(oldTSi.getDate());
                // Then update it if the oldTS interval is >=DAY or INST
                if (!oldTsTimeStampedAtEnd) {
                    newTS_endDate.addInterval(newTSBase, -newTSMult);
                }

                // How many data points to assign values to?
                // TODO [LT] 2005-03-01 - This block of code could
                // run only twice, assuming that the data count for the
                // first iteration might be less then the number of
                // actual number of new interval in the old interval.
                // But since this code in general, it apply also to
                // months, when the number of new interval in the old
                // interval will change from month to month
                // Consider add control flags to improve the performance
                // when running conversions from time series with
                // intervals shorter than month.
                if ( (newTimeScale == TimeScaleType.ACCM) ||
                    ((oldTimeScale == TimeScaleType.ACCM) && (newTimeScale == TimeScaleType.MEAN))) {
                    // to ACCM or from ACCM to MEAN
                    dataCount = 0;

                    if (oldTsTimeStampedAtEnd) {
                        // Less than Day, use the intervalRelation
                        dataCount = intervalRelation;
                    }
                    else {
                        // Save the current date to reset the iterator.
                        DateTime newTSDate = new DateTime(newTSi.getDate());
                        // How many new intervals fit in the old one.
                        while (newTSi.getDate().lessThanOrEqualTo(newTS_endDate)) {
                            dataCount++;
                            newTSi.next();
                        }
                        // Reset the iterator back to the original value
                        newTSi.setBeginTime(newTSDate);
                        newTSDate = null;
                    }

                    // Precaution to avoid division by zero, just in case.
                    if (dataCount < 1) {
                        dataCount = 1;
                        value = newMissing;
                    }
                }
                else {
                    // from INST or MEAN to MEAN
                    dataCount = 1;
                }

                // Assign the values to the output time series
                while (newTSi.getDate().lessThanOrEqualTo(newTS_endDate)) {

                    newDate = new DateTime(newTSi.getDate());
                    if (missingFlag == true) {
                        newTS.setDataValue(newDate, newMissing);
                    }
                    else {
                        newTS.setDataValue(newDate, value / dataCount);
                        // ,qualityFlag, 0 );
                        // TODO [LT] 2005-03-01 Quality flag is currently not used for this method.
                    }
                    newTSi.next();
                }

                previousOldValue = oldTSi.getDataValue();
            }
        }
        return true;
    }

    /**
     * @return New DayTS that has been converted from an irregular time series.  This is used by some
     * legacy code that at some point could be converted to use the above methods.
     * @param oldts Irregular time series to convert.
     * @param newmult New daily time series.
     */
    public DayTS OLDchangeToDayTS(IrregularTS oldts, int newmult ) {
        String routine = "TSUtil.changeToDayTS(IrregularTS)";
        int dl = 20;

        if (Message.isDebugOn) {
            Message.printDebug(dl, routine, "Changing to " + newmult + "-day time series");
        }

        // First declare a new time series..

        DayTS newts = new DayTS();

        // Copy the header and the dates. Set the dates as the rounded dates to handle the old time series...

        newts.copyHeader(oldts);
        DateTime newts_date[] = getBoundingDatesForChangeInterval(oldts, newts.getDataIntervalBase(), newmult);
        newts.setDate1(newts_date[0]);
        newts.setDate2(newts_date[1]);
        newts.setDate1Original(oldts.getDate1());
        newts.setDate2Original(oldts.getDate2());
        Message.printDebug(dl, routine, "Using regular TS period " + newts.getDate1() + " to " + newts.getDate2());

        // Now override with new information...

        newts.setDataInterval(TimeInterval.DAY, newmult);
        TSIdent tsident = newts.getIdentifier();
        tsident.setInterval(TimeInterval.DAY, newmult);

        // Allocate space based on the dates in the header...

        newts.allocateDataSpace();

        // Now loop through the new period and fill in with averaged irregular data as it is found...

        DateTime start = new DateTime(newts.getDate1());
        DateTime end = new DateTime(newts.getDate2());
        DateTime date_new = new DateTime(start);

        // Initialize the date after which we can accumulate to...

        DateTime previous_date = new DateTime(date_new);
        previous_date.addDay(-newmult);

        // Initialize the irregular data...

        List<TSData> alldata = oldts.getData();
        if (alldata == null) {
            // No data...
            return newts;
        }
        TSData data = null;
        int count = 0, i = 0, iend = alldata.size();
        double ave, sum = 0.0, value;
        DateTime t = null;

        for (; date_new.lessThanOrEqualTo(end); date_new.addDay(newmult), previous_date.addDay(newmult)) {
            if (Message.isDebugOn) {
                Message.printDebug(dl, routine, "Processing " + newmult + "-day date " + date_new.toString(DateTime.FORMAT_Y2K_LONG));
                Message.printDebug(dl, routine, "Previous date is " + previous_date.toString(DateTime.FORMAT_Y2K_LONG));
            }

            // Now loop through the irregular time series data and transfer
            // to the new date. If the date is past what we are looking
            // for, leave it and use in the next time step...

            sum = 0.0;
            count = 0;
            for (; i < iend; i++) {
                data = alldata.get(i);
                t = data.getDate();
                if (Message.isDebugOn) {
                    Message.printDebug(dl, routine, "Processing IRRTS date " + t.toString(DateTime.FORMAT_Y2K_LONG));
                }

                // If the irregular data date is greater than the
                // current loop date, then we accumulate and break
                // (so the day interval updates). Also make sure to
                // consider the last observation (move that last check
                // outside the loop to increase performance)...

                // Put this first so we can check to see if we have data...

                if (t.greaterThan(previous_date) && (t.lessThanOrEqualTo(date_new))) {
                    // Add the data values...

                    value = oldts.getDataValue(t);
                    if (Message.isDebugOn) {
                        Message.printDebug(dl, routine, "Value returned fron IRRTS " + value + " at " + t.toString(DateTime.FORMAT_Y2K_LONG));
                    }
                    if (!oldts.isDataMissing(value)) {
                        sum += value;
                        ++count;
                    }
                }
                if (t.greaterThan(date_new) || date_new.equals(end)) {
                    // Need to break and increment the outside date
                    // counter. First do the computations and transfer the numbers...
                    if (count > 0) {
                        ave = sum / (double) count;
                        // Now set the value in the new time series. It should be OK to use the
                        newts.setDataValue(date_new, ave);
                        if (Message.isDebugOn) {
                            Message.printDebug(dl, routine, "Averaged " + count + " values to get " + ave + " at " + date_new.toString(DateTime.FORMAT_Y2K_LONG));
                        }
                    }
                    else {
                        if (Message.isDebugOn) {
                            Message.printDebug(dl, routine, "No non-missing data to average at " + date_new.toString(DateTime.FORMAT_Y2K_LONG));
                        }
                    }
                    // Now reinitialize the sums...
                    sum = 0.0;
                    count = 0;
                    break;
                }
            }
        }
        return newts;
    }
 
    /**
     * Convert a daily time series to monthly by searching for values near the end of the month.
     * This is used to convert daily reservoir observations to end of month value, for example.
     * 
     * @param oldts Daily time series to convert.
     * @param newmult Interval multiplier for monthly time series.
     * @param bracket number of days to search the daily time series from the end of the month, to find
     * @param createData if true, do full processing; if false, only create time series metadata
     * the monthly value (required).
     */
    public MonthTS newEndOfMonthTSFromDayTS(DayTS oldts, int newmult, Integer bracket, boolean createData ) {
        String routine = getClass().getSimpleName() + ".newEndOfMonthTSFromDayTS";
        if (bracket == null) {
            throw new IllegalArgumentException ( "The bracket must be specified." );
        }

        // Else get the interval count... This should be a generic procedure
        // that can be applied to other conversions also...

        int nearness = bracket.intValue();

        // Now create the new time series...

        MonthTS newts = new MonthTS();

        // Copy the header, but set the date to an even roundoff of the interval...

        newts.copyHeader(oldts);
        DateTime newts_date[] = null;
        if ( !createData && (oldts == null) || (oldts.getDate1() == null) || (oldts.getDate2() == null) ) {
        	// OK to skip the dates since not creating data (likely discovery mode in TSTool)
        	newts_date = new DateTime[2];
        	newts_date[0] = null;
        	newts_date[1] = null;
        }
        else {
        	newts_date = getBoundingDatesForChangeInterval(oldts, newts.getDataIntervalBase(), newmult);
        }

        // Now override with new information...

        newts.setDataInterval(TimeInterval.MONTH, newmult);
        TSIdent tsident = newts.getIdentifier();
        tsident.setInterval(TimeInterval.MONTH, newmult);

        // Set the dates after setting the interval so that the precision is correct...

        Message.printStatus(2, routine, "Dates before set...: " + newts_date[0] + " to " + newts_date[1]);
        newts.setDate1(newts_date[0]);
        newts.setDate2(newts_date[1]);
        Message.printStatus(2, routine, "Dates after set...: " + newts.getDate1() + " to " + newts.getDate2());
        // Retain the original dates also...
        newts.setDate1Original(oldts.getDate1Original());
        newts.setDate2Original(oldts.getDate2Original());
        // Message.printStatus ( 2, routine,
        // "Using regular TS period " + newts.getDate1() + " to " + newts.getDate2 () );
        if ( !createData ) {
            return newts;
        }

        // Allocate space based on the dates in the header...

        newts.allocateDataSpace();

        // Now loop through...

        DateTime end = new DateTime(oldts.getDate2());
        DateTime t = new DateTime(new DateTime(newts.getDate1()));
        double value = 0.0;

        // Loop through months...

        DateTime dayts_date = null;
        if (nearness > 0) {
            // Construct a date so we get the characteristics of the original time series dates...
            dayts_date = new DateTime(oldts.getDate1());
        }
        for (; t.lessThanOrEqualTo(end); t.addInterval(TimeInterval.MONTH, newmult)) {
            if (nearness > 0) {
                // Change intervals by finding the nearest value in the original time series.
                // Reuse the date from the original. Start by setting the year and month to
                // that of the monthly time series...
                for (int isearch = 0; isearch < nearness; isearch++) {
                    // Go backward in the current month and forward in the next month until we find
                    // a value. If a value is not found, don't set (leave missing)...
                    dayts_date.setYear(t.getYear());
                    dayts_date.setMonth(t.getMonth());
                    dayts_date.setDay(TimeUtil.numDaysInMonth(t.getMonth(), t.getYear()));
                    dayts_date.addDay(-isearch);
                    value = oldts.getDataValue(dayts_date);
                    if (!oldts.isDataMissing(value)) {
                        // Found it...
                        newts.setDataValue(t, value);
                        break;
                    }
                    // Check the next month.
                    dayts_date.setYear(t.getYear());
                    dayts_date.setMonth(t.getMonth());
                    dayts_date.setDay(TimeUtil.numDaysInMonth(t.getMonth(), t.getYear()));
                    dayts_date.addDay(isearch);
                    value = oldts.getDataValue(dayts_date);
                    if (!oldts.isDataMissing(value)) {
                        // Found it...
                        newts.setDataValue(t, value);
                        break;
                    }
                }
            }
        }
        if (nearness > 0) {
            newts.addToGenesis("Converted to monthly using daily TS \"" + oldts.getIdentifierString() +
                "\" using " + nearness + " day proximity to month end.");
        }
        return newts;
    }

    /* TODO SAM 2009-11-02 Does not seem to be used anywhere
    public MonthTS OLD_changeToMonthTS(IrregularTS oldts, int newmult, PropList proplist) {
        String routine = "TSUtil.changeToMonthTS(IrregularTS)";
        MonthTS newts = null;
        int dl = 30;

        // First declare a new time series..

        if (Message.isDebugOn) {
            Message.printDebug(dl, routine, "Changing to " + newmult + "-month time series");
        }
        newts = new MonthTS();

        // Copy the header...

        newts.copyHeader(oldts);
        DateTime newts_date[] = getBoundingDatesForChangeInterval(oldts, newts.getDataIntervalBase(), newmult);
        newts.setDate1(newts_date[0]);
        newts.setDate2(newts_date[1]);
        newts.setDate1Original(oldts.getDate1Original());
        newts.setDate2Original(oldts.getDate2Original());
        Message.printStatus(1, routine, "Using regular TS period " + newts.getDate1() + " to " + newts.getDate2());

        // Now override with new information...

        newts.setDataInterval(TimeInterval.MONTH, newmult);
        TSIdent tsident = newts.getIdentifier();
        tsident.setInterval(TimeInterval.MONTH, newmult);

        // Allocate space based on the dates in the header...

        newts.allocateDataSpace();

        // Now loop through...

        DateTime start = new DateTime(oldts.getDate1());
        DateTime end = new DateTime(oldts.getDate2());
        DateTime t = null, tprev = null;
        int count = 0, month, prev_month, prev_year, year;
        double ave, sum = 0.0, value;
        if (Message.isDebugOn) {
            Message.printDebug(dl, routine, "Iterator start/end are: " + start + " " + end);
        }
        List alldata = oldts.getData();
        if (alldata == null) {
            // No data...
            return newts;
        }
        TSData data = null;
        int iend = alldata.size();
        for (int i = 0; i < iend; i++) {
            data = (TSData) alldata.get(i);
            t = data.getDate();

            if (Message.isDebugOn) {
                Message.printDebug(dl, routine, "Processing date " + t);
            }

            value = oldts.getDataValue(t);
            if (Message.isDebugOn) {
                Message.printDebug(dl, routine, "Value returned fron IRRTS is " + value);
            }

            month = t.getMonth();
            year = t.getYear();

            if (i == 0) {
                // First point...
                if (!oldts.isDataMissing(value)) {
                    sum = value;
                    ++count;
                } else {
                    sum = 0.0;
                    count = 0;
                }
            } else { // Not first point checked...
                // Get the previous points data. We can only treat as
                // the same hour if the same day, month, year!
                prev_month = tprev.getMonth();
                prev_year = tprev.getYear();
                if ((month == prev_month) && (year == prev_year)) {
                    // Same hour, so add to the sum if not
                    // missing...
                    if (!oldts.isDataMissing(value)) {
                        sum += value;
                        ++count;
                    }
                } else { // A different month. Process the previous day
                    // and then reset...
                    if (count > 0) {
                        // Compute the average during the day.
                        // Don't worry about the NWS way of
                        // doing it where we average one value
                        // from the bounding days since this
                        // is irregular data...
                        ave = sum / (double) count;
                        // Now set the value in the new time
                        // series. It should be OK to use the
                        newts.setDataValue(tprev, ave);
                        if (Message.isDebugOn) {
                            Message.printDebug(dl, routine, "Averaged " + count + " values to get " + ave + " for " + tprev);
                        }
                    }
                    sum = 0.0;
                    count = 0;
                }
            }

            // Save so we can check the next time around...

            tprev = new DateTime(t);
        }
        // Always have to process the last day...
        if (t != null) {
            if (count > 0) {
                // Compute the average during the day.
                // Don't worry about the NWS way of
                // doing it where we average one value
                // from the bounding days since this
                // is irregular data...
                ave = sum / (double) count;
                // Now set the value in the new time
                // series. It should be OK to use the
                newts.setDataValue(t, ave);
                if (Message.isDebugOn) {
                    Message.printDebug(dl, routine, "Averaged " + count + " values to get " + ave + " for " + t);
                }
            }
        }
        return newts;
    }
    */
 
/**
Return the allowMissingConsecutive value.
@return the allowMissingConsecutive value.
*/
private int getAllowMissingConsecutive ()
{
    return __allowMissingConsecutive;
}
    
/**
Return the allowMissingCount value.
@return the allowMissingCount value.
*/
private int getAllowMissingCount ()
{
    return __allowMissingCount;
}

/**
Return the allowMissingPercent value.
@return the allowMissingPercent value.
*/
private double getAllowMissingPercent ()
{
    return __allowMissingPercent;
}

    // History:
    //
    // 17 Aug 1998 SAM, RTi Update so that the resulting dates
    // have the proper precision. Otherwise
    // some date fields disrupt output.
    // 2005-06-01 Luiz Teixeira, RTi Extended date1 by one time interval
    // in all cases
    // Changed the precision for the
    // TimeInterval case from PRECISION_MONTH
    // to PRECISION_YEAR.
    /**
     * Determine the bounding dates to be used for converting a time series from one interval to another. This method may be overloaded or may be made more complex in the
     * future in order to better determine the dates depending on data type and recording method (e.g. to round to the previous interval or round to the nearest ending
     * interval depending on whether the data are instantaneous, sums, averages, etc.). Currently, the date bounds are always extended in both directions, possibly
     * resulting in missing data at the ends when the changes is performed.
     * 
     * @return Array of two DateTime containing bounding dates for the new time series.
     * @param oldts Original time series that is being changed to a different interval.
     * @param newbase Base data interval for new time series.
     * @param newmult Multiplier for new time series.
     */
    public static DateTime[] getBoundingDatesForChangeInterval(TS oldts, int newbase, int newmult) {
        DateTime newts_date[] = new DateTime[2];
        String routine = "TSUtil.getBoundingDatesForChangeInterval";

        // Depending on the desired interval, round the dates appropriately...

        DateTime old_date1 = oldts.getDate1();
        DateTime old_date2 = oldts.getDate2();

        // If migrating to a time series of smaller interval, add the number of intervals required
        // to preserve same ending date.
        boolean to_smaller = false;
        DateTime old_date1_plus_one = new DateTime(old_date1);
        old_date1_plus_one.addInterval(oldts.getDataIntervalBase(), oldts.getDataIntervalMult());
        int nintervals = TimeUtil.getNumIntervals(old_date1, old_date1_plus_one, newbase, newmult);
        nintervals = nintervals == 0? 1 : nintervals;
        if ( oldts.getDataIntervalBase() > newbase ) {
            to_smaller = true;
        }

        if (newbase == TimeInterval.IRREGULAR) {
            // Can use the original dates as is...
            newts_date[0] = new DateTime(oldts.getDate1());
            newts_date[1] = new DateTime(oldts.getDate2());
        }
        else if (newbase == TimeInterval.MINUTE) {
            newts_date[0] = new DateTime(DateTime.PRECISION_MINUTE);
            newts_date[1] = new DateTime(DateTime.PRECISION_MINUTE);
            // Transfer...
            newts_date[0].setYear(old_date1.getYear());
            newts_date[0].setMonth(old_date1.getMonth());
            newts_date[0].setDay(old_date1.getDay());
            newts_date[0].setHour(old_date1.getHour());
            newts_date[0].setMinute(old_date1.getMinute());
            newts_date[1].setYear(old_date2.getYear());
            newts_date[1].setMonth(old_date2.getMonth());
            newts_date[1].setDay(old_date2.getDay());
            newts_date[1].setHour(old_date2.getHour());
            newts_date[1].setMinute(old_date2.getMinute());
            // Round the minutes to the new multiplier...
            newts_date[0].setMinute(newmult * (newts_date[0].getMinute() / newmult));
            newts_date[1].setMinute(newmult * (newts_date[1].getMinute() / newmult));
            // Extend by nintervals
            if (to_smaller) {
                newts_date[1].addInterval(newbase, newmult*nintervals);
            }
        }
        else if (newbase == TimeInterval.HOUR) {
            newts_date[0] = new DateTime(DateTime.PRECISION_HOUR);
            newts_date[1] = new DateTime(DateTime.PRECISION_HOUR);
            // Transfer...
            newts_date[0].setYear(old_date1.getYear());
            newts_date[0].setMonth(old_date1.getMonth());
            newts_date[0].setDay(old_date1.getDay());
            newts_date[0].setHour(old_date1.getHour());
            newts_date[1].setYear(old_date2.getYear());
            newts_date[1].setMonth(old_date2.getMonth());
            newts_date[1].setDay(old_date2.getDay());
            newts_date[1].setHour(old_date2.getHour());
            // Round the hours to the nearest ones that make sense. Since
            // we only know how to average this type of data, set the hour
            // to the interval-ending value.
            int newts_hour = 0;
            if ((newts_date[0].getHour() % newmult) != 0) {
                // The dates do not line up so offset...
                // Not sure why the +1, other than for extending to end-of-interval..
                // newts_hour =(newts_date[0].getHour()/newmult+1)*newmult;
                newts_hour = (newts_date[0].getHour() / newmult) * newmult;
                if (newts_hour < 24) {
                    // Just need to reset the hour...
                    newts_date[0].setHour(newts_hour);
                }
                else {
                    // Need to set the date into the next day...
                    newts_date[0].addDay(1);
                    newts_date[0].setHour(0);
                }
            }
            if ((newts_date[1].getHour() % newmult) != 0) {
                // See note above...
                // newts_hour =(newts_date[1].getHour()/newmult+1)*newmult;
                newts_hour = (newts_date[1].getHour() / newmult) * newmult;
                if (newts_hour < 24) {
                    // Just need to reset the hour...
                    newts_date[1].setHour(newts_hour);
                }
                else {
                    // Need to set the date into the next day...
                    newts_date[1].addDay(1);
                    newts_date[1].setHour(0);
                }
            }
            // Extend by one interval
            if (to_smaller) {
                newts_date[1].addInterval(newbase, newmult*nintervals);
            }
        }
        else if (newbase == TimeInterval.DAY) {
            // Use the old dates except set everything to zero values other
            // than month and year and day...
            newts_date[0] = new DateTime(DateTime.PRECISION_DAY);
            newts_date[1] = new DateTime(DateTime.PRECISION_DAY);
            // Transfer...
            newts_date[0].setYear(old_date1.getYear());
            newts_date[0].setMonth(old_date1.getMonth());
            newts_date[0].setDay(old_date1.getDay());
            newts_date[1].setYear(old_date2.getYear());
            newts_date[1].setMonth(old_date2.getMonth());
            newts_date[1].setDay(old_date2.getDay());
            // Extend by one interval
            if (to_smaller) {
                newts_date[1].addInterval(newbase, newmult*nintervals);
            }
        }
        else if (newbase == TimeInterval.MONTH) {
            // Use the old dates except set everything to zero values other than month and year...
            // Note that the date items less than month are not really used
            // since the timestep is monthly, but sometimes for displays the
            // day may be used to position output (e.g., set the day to 15
            // to force plotting at the center of the month. For now, set to 1.
            newts_date[0] = new DateTime(DateTime.PRECISION_MONTH);
            newts_date[1] = new DateTime(DateTime.PRECISION_MONTH);
            // Transfer...
            newts_date[0].setYear(old_date1.getYear());
            newts_date[0].setMonth(old_date1.getMonth());
            newts_date[1].setYear(old_date2.getYear());
            newts_date[1].setMonth(old_date2.getMonth());
            // Extend by one interval
            if (to_smaller) {
                newts_date[1].addInterval(newbase, newmult*nintervals);
            }
        }
        else if (newbase == TimeInterval.YEAR) {
            // Similar to monthly above, but also set month to 1...
            newts_date[0] = new DateTime(DateTime.PRECISION_YEAR);
            newts_date[1] = new DateTime(DateTime.PRECISION_YEAR);
            // Transfer...
            newts_date[0].setYear(old_date1.getYear());
            newts_date[1].setYear(old_date2.getYear());
            // Extend by one interval
            if (to_smaller) {
                newts_date[1].addInterval(newbase, newmult*nintervals);
            }
        }
        else {
            Message.printWarning(2, routine, "Getting dates to change to interval " + newbase + " not supported.  Using original dates.");
        }
        Message.printStatus(10, routine, "Bounding dates for new time series are: " + newts_date[0] + " to " + newts_date[1]);
        return newts_date;
    }
    
/**
Return the handleEndpointsHow value.
@return the handleEndpointsHow value.
*/
private TSUtil_ChangeInterval_HandleEndpointsHowType getHandleEndpointsHow ()
{
    return __handleEndpointsHow;
}

/**
Return the handleMissingInputHow value.
@return the handleMissingInputHow value.
*/
private TSUtil_ChangeInterval_HandleMissingInputHowType getHandleMissingInputHow ()
{
    return __handleMissingInputHow;
}
    
/**
 * Get the interval relation between two time series while making sure they are multiples of each other.
 * 
 * @param TS1 Reference to 1st time series (older).
 * @param TS2 Reference to 2nd time series (newer).
 * @return -1 * TS2IntSeconds/TS1IntSeconds if the 1st time series interval is shorter than the 2nd,
 * TS1IntSeconds/TS2IntSeconds if the 1st time series interval is longer or equal to the 2nd,
 * 0 if they are not multiples, (not supported).
 * -1 if the 1st time series interval is IRREGULAR,
 * 1 if the 2nd or both time series interval is IRREGULAR (not supported).
 */
private int getIntervalRelation(TS TS1, TS TS2)
{
    String routine = "TSUtil.getIntervalRelation", warning;

    // For an irregular time series, assume that it's interval is shorter than the other one.
    if (TS1.getDataIntervalBase() == TimeInterval.IRREGULAR) {
        if (TS2.getDataIntervalBase() == TimeInterval.IRREGULAR) {
            // Both are IRREGULAR, return 1.
            return 1;
        }
        // First only is IRREGULAR, return -1.
        return -1;
    }
    else if (TS2.getDataIntervalBase() == TimeInterval.IRREGULAR) {
        // Second only is IRREGULAR, return 1.
        return 1;
    }

    // Get the interval (in seconds) for the TS1.
    // Using the toSecondsApproximate() because month and year do not allow
    // for the precise computation of toSeconds().
    TimeInterval TS1_ti = new TimeInterval(TS1.getDataIntervalBase(), TS1.getDataIntervalMult());
    int TS1IntSeconds = TS1_ti.toSecondsApproximate();

    // Get the interval (in seconds) for the TS2.
    // Using the toSecondsApproximate() because month and year do not allow
    // for the precise computation of toSeconds(). Do not use this value
    //  to step through the interval, use only for interval comparisons.
    TimeInterval TS2_ti = new TimeInterval(TS2.getDataIntervalBase(), TS2.getDataIntervalMult());
    int TS2IntSeconds = TS2_ti.toSecondsApproximate();

    // Then use these intervals (in seconds) to figure out if one interval
    // is multiple of the other. If so return either 1 or -1, otherwise 0.
    // The toSecondsApproximate() values are fine here.

    // First ts interval is smaller than the second ts interval and
    // the second ts interval is multiple of the first ts interval
    if ((TS1IntSeconds < TS2IntSeconds) && (TS2IntSeconds % TS1IntSeconds == 0)) {
        return -1 * TS2IntSeconds / TS1IntSeconds;
        // First ts interval is greater than the second ts interval and
        // the first ts interval is multiple of the second ts interval
    }
    else if ((TS1IntSeconds >= TS2IntSeconds) && (TS1IntSeconds % TS2IntSeconds == 0)) {
        return TS1IntSeconds / TS2IntSeconds;
        // The intervals are not multiple of each other.
    }
    else {
        warning = "Intervals are not multiples of each other";
        Message.printWarning(2, routine, warning);
        return 0;
    }
}

/**
Return the new data type.
@return the new data type.
*/
private String getNewDataType ()
{
    return __newDataType;
}

/**
Return the new interval.
@return the new interval.
*/
private TimeInterval getNewInterval ()
{
    return __newInterval;
}

/**
Return the new time scale.
@return the new time scale.
*/
private TimeScaleType getNewTimeScale ()
{
    return __newTimeScale;
}

/**
Return the new units.
@return the new units.
*/
private String getNewUnits ()
{
    return __newUnits;
}

/**
Return the old time scale.
@return the old time scale.
*/
private TimeScaleType getOldTimeScale ()
{
    return __oldTimeScale;
}
    
/**
Return the old time series.
@return the old time series.
*/
private TS getOldTimeSeries ()
{
    return __oldTS;
}

/**
Return the output fill method.
@return the output fill method.
*/
private TSUtil_ChangeInterval_OutputFillMethodType getOutputFillMethod ()
{
    return __outputFillMethod;
}

/**
Return the output year type.
@return the output year type.
*/
private YearType getOutputYearType ()
{
    return __outputYearType;
}

/**
Return the statistic being computed.
@return the statistic being computed.
*/
private TSStatisticType getStatistic()
{
    return __statistic;
}

/**
Get the list of statistics that can be created during the conversion.  For example, for INST to INST the
computed value can be the MAX or MIN or default (new INST).
@return the list of statistic display names as TSStatisticType.
*/
public static List<TSStatisticType> getStatisticChoices()
{
    List<TSStatisticType> choices = new ArrayList<TSStatisticType>();
    choices.add ( TSStatisticType.MAX );
    choices.add ( TSStatisticType.MIN );
    return choices;
}

/**
Get the list of statistics that can be created during the conversion.  For example, for INST to INST the
computed value can be the MAX or MIN or default (new INST).
@return the statistic display names as strings.
*/
public static List<String> getStatisticChoicesAsStrings()
{
    List<TSStatisticType> choices = getStatisticChoices();
    List<String> stringChoices = new ArrayList<String>();
    for ( TSStatisticType choice : choices ) {
        stringChoices.add ( "" + choice );
    }
    return stringChoices;
}

/**
Return the tolerance used with convergence tests.
@return the tolerance used with convergence tests.
*/
private double getTolerance ()
{
    return __tolerance;
}

    /**
     * Replaces a missing data value according to flag. This method is used by the new changeInterval method.
     * 
     * @param handleMissingInputHow indicate how to handle missing values in input
     * @param replacementValue used to replace a data value when handleMissingInputHow = REPEAT
     * @param defaultReplacementValue used to replace a data value when handleMissingInputHow is not REPEAT or
     * SET_TO_ZERO (generally the missing value).
     */
    private static double replaceDataValue(TSUtil_ChangeInterval_HandleMissingInputHowType handleMissingInputHow,
        double replacementValue, double defaultReplacementValue)
    {
        switch (handleMissingInputHow) {
        case SET_TO_ZERO:
            return 0.0;
        case REPEAT:
            return replacementValue;
        default:
            return defaultReplacementValue;
        }
    }

/**
Set the number of input values allowed to be missing and still compute the result.
@param allowMissingCount number of input values allowed to be missing and still compute the result.
*/
private void setAllowMissingConsecutive ( int allowMissingConsecutive )
{
    __allowMissingConsecutive = allowMissingConsecutive;
}
    
/**
Set the number of input values allowed to be missing and still compute the result.
@param allowMissingCount number of input values allowed to be missing and still compute the result.
*/
private void setAllowMissingCount ( int allowMissingCount )
{
    __allowMissingCount = allowMissingCount;
}

/**
Set the percentage of input values allowed to be missing and still compute the result.
@param allowMissingPercent percent of input values allowed to be missing and still compute the result.
*/
private void setAllowMissingPercent ( double allowMissingPercent )
{
    __allowMissingPercent = allowMissingPercent;
}
    
/**
Set the handleEndpointsHow property.
@param handleEndpointsHow method.
*/
private void setHandleEndpointsHow ( TSUtil_ChangeInterval_HandleEndpointsHowType handleEndpointsHow )
{
    __handleEndpointsHow = handleEndpointsHow;
}

/**
Set the handleMissingInputHow property.
@param handleMissingInputHow method.
*/
private void setHandleMissingInputHow ( TSUtil_ChangeInterval_HandleMissingInputHowType handleMissingInputHow )
{
    __handleMissingInputHow = handleMissingInputHow;
}
    
/**
Set the new time series data type.
@param newDataType new time series data type.
*/
private void setNewDataType ( String newDataType )
{
    __newDataType = newDataType;
}

/**
Set the new interval.
@param newInterval new interval.
*/
private void setNewInterval ( TimeInterval newInterval )
{
    __newInterval = newInterval;
}
    
/**
Set the new time scale.
@param newTimeScale new time scale.
*/
private void setNewTimeScale ( TimeScaleType newTimeScale )
{
    __newTimeScale = newTimeScale;
}

/**
Set the new time series data units.
@param newUnits new time series units.
*/
private void setNewUnits ( String newUnits )
{
    __newUnits = newUnits;
}
    
/**
Set the old time scale.
@param oldTimeScale old time scale (from the original time series).
*/
private void setOldTimeScale ( TimeScaleType oldTimeScale )
{
    __oldTimeScale = oldTimeScale;
}

/**
Set the old time series.
@param oldTimeSeries old time series.
*/
private void setOldTimeSeries ( TS oldTimeSeries )
{
    __oldTS = oldTimeSeries;
}

/**
Set the output fill method.
@param outputFillMethod output fill method.
*/
private void setOutputFillMethod ( TSUtil_ChangeInterval_OutputFillMethodType outputFillMethod )
{
    __outputFillMethod = outputFillMethod;
}

/**
Set the output year type.
@param outputYearType output year type.
*/
private void setOutputYearType ( YearType outputYearType )
{
    __outputYearType = outputYearType;
}

/**
Set the statistic type.
@param statistic statistic being computed (e.g., MAX for INST-small to INST-large).
*/
private void setStatistic ( TSStatisticType statistic )
{
    __statistic = statistic;
}

/**
Set the tolerance used with convergence tests.
@param tolerance tolerance used with convergence tests.
*/
private void setTolerance ( double tolerance )
{
    __tolerance = tolerance;
}

}
