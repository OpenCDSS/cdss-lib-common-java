package RTi.TS;

import java.util.Vector;

import RTi.Util.IO.MeasTimeScale;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
 * Change a time series interval, creating a new time series.
 */
public class TSUtil_ChangeInterval {

    /**
     * Constructor.
     */
    public TSUtil_ChangeInterval() {
        // Does nothing.
    }

    /**
     * Get the interval relation between two time series while making sure they are multiples of each other.
     * 
     * @param TS1 Reference to 1st time series (older).
     * @param TS2 Reference to 2nd time series (newer).
     * @return -1 * TS2IntSeconds/TS1IntSeconds if the 1st time series interval is shorter than the 2nd, TS1IntSeconds/TS2IntSeconds if the 1st time series interval is
     *         longer or equal to the 2nd, 0 if they are not multiples, (not supported). -1 if the 1st time series interval is IRREGULAR, 1 if the 2nd or both time series
     *         interval is IRREGULAR (not supported).
     */
    private int getIntervalRelation(TS TS1, TS TS2) {
        String routine = "TSUtil.getIntervalRelation", warning;

        // For an irregular time series, assume that it's interval is shorter
        // than the other one.
        if (TS1.getDataIntervalBase() == TimeInterval.IRREGULAR) {
            if (TS2.getDataIntervalBase() == TimeInterval.IRREGULAR) {
                // Both are IRREGULAR, return 1.
                return 1;
            }
            // First only is IRREGULAR, return -1.
            return -1;
        } else if (TS2.getDataIntervalBase() == TimeInterval.IRREGULAR) {
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
        // for to step througth the interval, use only for interval comparisons.
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
        } else if ((TS1IntSeconds >= TS2IntSeconds) && (TS1IntSeconds % TS2IntSeconds == 0)) {
            return TS1IntSeconds / TS2IntSeconds;

            // The intervals are not multiple of each other.
        } else {
            warning = "Intervals are not multiples of each other";
            Message.printWarning(2, routine, warning);
            return 0;
        }
    }

    /**
     * Return a new time series having a different interval from the source time series. This is the main routine which will call subordinate routines, depending on the
     * intervals that are involved. This method uses a string to specify the new interval, in order to be different from the version that uses integers for the new
     * interval
     * 
     * @return A new time series of the requested data interval. All of the original time series header information will be essentially the same, except for the interval
     *         and possibly the data type.
     * @param oldTS Time series used as data to generate the new time series.
     * @param new_interval New interval as a string that can be parsed by RTi.Util.Time.TimeInterval.parseInterval(), indicating the new interval for the time series.
     * @param props Properties to use during processing. The following properties are recognized:
     *            <p>
     * 
     * <table width=100% cellpadding=10 cellspacing=0 border=2>
     * 
     * <tr>
     * <td><b>Property</b></td>
     * <td><b>Description</b></td>
     * <td><b>Default</b></td>
     * </tr>
     * 
     * <tr>
     * <td><b>allowMissingCount</b></td>
     * <td> Indicate the number of missing values that can be missing in the input data and still allow computation of the result. For example, if daily data are being
     * converted to monthly, a value of 5 would allow <= 5 missing values to be present in a month's daily data, and still generate a monthly value. The missing values
     * are evaluated for the block of input needed to compute the result. This argument is not used for conversion from irregular time series or conversions from larger
     * intervals to smaller ones</b>
     * <td>0, meaning that any missing data in the input data will result in missing data in the result.</td>
     * </tr>
     * 
     * <tr>
     * <td><b>AllowMissingPercent</b></td>
     * <td> THIS PROPERTY IS CURRENTLY DISABLED Indicate the percent of missing values that can be missing in the input data and still allow computation of the result.
     * For example, if daily data are being converted to monthly, a value of 25 would allow <= 25% missing values to be present in a month's daily data, and still
     * generate a monthly value. The missing values are evaluated for the block of input needed to compute the result. Because months have different numbers of days,
     * using a percentage may result in a different threshold for different months. This argument is not used for conversion from irregular time series or conversions
     * from larger intervals to smaller ones</b>
     * <td>0, meaning that any missing data in the input data will result in missing data in the result.</b> </tr>
     * 
     * <tr>
     * <td><b>NewDataType</b></td>
     * <td>The data type for the new time series.
     * <td>Same as the input time series. Warning: for some systems, the meaning of the data type changes depending on the interval. For example "Streamflow" may be
     * instantaneous for irregular data and mean for daily data. If this property is not given, the DataType of the oldTS will be used.</td>
     * </tr>
     * 
     * <tr>
     * <td><b>NewTimeScale</b></td>
     * <td>The new time scale indicates whether values in the new time series are accumulated over the interval ("ACCM"), an average over the interval ("MEAN"), or
     * instantaneous for the point in time ("INST")).</td>
     * <td>Determined from the new data type. Note that because data types may not be standardized for all applications (e.g., TSTool), it may not be reliable to use a
     * default.</td>
     * </tr>
     * 
     * <tr>
     * <td><b>OldTimeScale</b></td>
     * <td>The old time scale indicates whether values in the old time series are accumulated over the interval ("ACCM"), an average over the interval ("MEAN"), or
     * instantaneous for the point in time ("INST")).</td>
     * <td>Determined from the new data type. Note that because data types may not be standardized for all applications (e.g., TSTool), it may not be reliable to use a
     * default.</td>
     * </tr>
     * 
     * <tr>
     * <td><b>HandleMissingImputHow</b></td>
     * <td>Indicate how to handle the missing data. The options are: KEEPMISSING (regular), missing data are kept as missing, REPEAT (precipitation alert data), missing
     * data are replaced by zero, SETTOZERO (other alert data), missing data are replaced by previous value. This parameter is used by the auxiliar routines to handle
     * missing data.</td>
     * <td>Default to KEEPMISSING if not given or not maching the allowed types.</td>
     * </tr>
     * 
     * <tr>
     * <td><b>KeepMissing</b></td>
     * <td>This property forces the code to keep all missing independently of the actual HandleMissingInputHow.</td>
     * <td>Default is to keep the HandleMissingInputHow as is.</td>
     * </tr>
     *
     * <tr>
     * <td><b>NewUnits</b></td>
     * <td>Units for the new time series.</td>
     * <td>Use the units from the original time series.</td>
     * </tr>
     * 
     * <tr>
     * <td><b>OutputFillMethod</b></td>
     * <td>This property is used only by conversions from instantaneos to mean when the conversion is from larger to smaller time intervals. The options are: INTERPOLATE -
     * The new time series values are interpolated between the old time series data points. REPEAT - The new time series values are carry foreward or backward from one of
     * the bounding old data values depending on the time interval (<Day or >=Day) data point.
     * <td>Default is REPEAT.</td>
     * </tr>
     * 
     * </table>
     * @exception Exception if an error occurs (e.g., bad new interval string).
     */
    public TS changeInterval(TS oldTS, String new_interval, PropList props) throws Exception {
        String routine = "changeInterval", status, warning;

        final String INTERPOLATE = "INTERPOLATE";
        final String REPEAT = "REPEAT";

        // Proplist - Make sure it is not null.
        PropList proplist = props;
        if (props == null) {
            warning = "PropList is null. Cannot processed without properties.";
            throw new TSException(warning);
        }

        // OldTS - Make sure it is not null and has a not zero lenght period of
        // record.
        if (oldTS == null) {
            warning = "Input ts is null. Cannot processed without an input ts. ";
            throw new TSException(warning);
        }
        if (oldTS.hasData() == false) {
            warning = "Input ts has no data.";
            throw new TSException(warning);
        }

        // Get the time scale for the old time series from the prop list.
        String OldTimeScale = proplist.getValue("OldTimeScale");
        if (OldTimeScale == null) {
            warning = "Unable to get value for the property OldTimeScale.";
            throw new TSException(warning);
        }
        if (!OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST) && !OldTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM)
                && !OldTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN)) {
            warning = "OldTimeScale '" + OldTimeScale + "' is not valid. Valid values are: INST, ACCM and MEAN";
            throw new TSException(warning);
        }

        // Get the time scale for the new time series from the prop list.
        String NewTimeScale = proplist.getValue("NewTimeScale");
        if (NewTimeScale == null) {
            warning = "Unable to get value for the property NewTimeScale.";
            throw new TSException(warning);
        }
        if ((!NewTimeScale.equalsIgnoreCase(MeasTimeScale.INST) && !NewTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM) && !NewTimeScale
                .equalsIgnoreCase(MeasTimeScale.MEAN))) {
            warning = "NewTimeScale '" + NewTimeScale + "' is not valid. Valid values are: INST, ACCM and MEAN";
            throw new TSException(warning);
        }

        // NewDataType - The data type for the new time series. If this property
        // is not given the DataType of the old TS is assumed.
        String NewDataType = proplist.getValue("NewDataType");
        if (NewDataType == null) {
            NewDataType = oldTS.getDataType();
        }
        
        // Get the new units.
        String NewUnits = proplist.getValue("NewUnits");
        // Will check below when creating the time series.

        // OutputFillMethod - Used when moving from INST to MEAN time
        // series going from larger to smaller Time interval.
        // is not given the DataType of the old TS is assumed.
        String OutputFillMethod = proplist.getValue("OutputFillMethod");
        if (OutputFillMethod == null) {
            OutputFillMethod = REPEAT;
        } else {
            if (!OutputFillMethod.equalsIgnoreCase(INTERPOLATE) && !OutputFillMethod.equalsIgnoreCase(REPEAT)) {
                warning = "OutputFillMethod '" + OutputFillMethod + "' is not valid. Valid values are:" + " INTERPOLATE and REPEAT.";
                throw new TSException(warning);
            }
        }

        // newts_TimeInterval - Parse the new TimeInterval from the method
        // argument "new_interval"
        // REVISIT [LT 2005-05-25] Should we have some kind of validity text
        // done here, and throw an exception if invalid?
        TimeInterval newtsTimeInterval;
        newtsTimeInterval = TimeInterval.parseInterval(new_interval);
        int newtsBase = newtsTimeInterval.getBase();
        int newtsMultiplier = newtsTimeInterval.getMultiplier();

        // HandleMissingInputHow - Get the transmission type from the prop list.
        // If not given it will default to Regular Transmission
        // when the missingValueFlag is set to 0
        //
        // HandleMissingInputHow Missing is replaced by: missingValueFlag
        // ---------------------------------------------------------------------
        // KEEPMISSING Regular Missing 0
        // SETTOZERO Alert increment Zeros 1
        // REPEAT Alert regular Repeat 2
        // ---------------------------------------------------------------------
        // The missingValueFlag will be used by:
        // 1) changeInterval_fromIRREGULAR ()
        // 2) changeInterval_toMEANorACCM ()
        // 3) changeInterval_fromINST ()
        // to replace old TS missing data values,
        // if Alert increment data (precip increments) by Zero
        // if Alert regular for other datatypes Previous
        // For REGULAR cases nothing will be changed, the missing values
        // will be replaced by missing values.
        // ---------------------------------------------------------------------
        // REVISIT [LT] 2005-02-16 - This should be modified to use predefined
        // flags as for the TimeScales above.
        // ---------------------------------------------------------------------
        int missingValueFlag = 0; // Default - Regular Transmission.
        String HandleMissingInputHow = proplist.getValue("HandleMissingInputHow");
        if (HandleMissingInputHow != null) {
            if (HandleMissingInputHow.equalsIgnoreCase("KEEPMISSING")) {
                missingValueFlag = 0;
            } else if (HandleMissingInputHow.equalsIgnoreCase("SETTOZERO")) {
                missingValueFlag = 1;
            } else if (HandleMissingInputHow.equalsIgnoreCase("REPEAT")) {
                missingValueFlag = 2;
            } else {
                warning = "HandleMissingInputHow '" + HandleMissingInputHow + "' is not valid. Valid values are:" + " KEEPMISSING, SETTOZERO and REPEAT.";
                throw new TSException(warning);
            }
        }

        // KeepMissing - Get the KeepMissing property from the prop list.
        //      
        // This property has effect only when convertion from
        // irregular time series (changeInterval_fromIRRREGULAR())
        // If set to true, the missingValueFlag variable is
        // updated to -1, which have the effect of changing
        // the behavior of the changeInterval_fromIRRREGULAR() method,
        // forcing it the handles all the missing data in the old
        // time series as missing before processing as well as to
        // use missing values to fill between irregular data points
        // instead of the TrasmissionType dependent filling code.
        //
        // HandleMissingInputHow Missing data is replaced by missingValueFlag
        // ---------------------------------------------------------------------
        // KeepMissing=TRUE Missing -1
        // ---------------------------------------------------------------------
        // REVISIT [LT] 2005-02-18 - There should be a better logical way to
        // handle this issue.
        // ---------------------------------------------------------------------
        String KeepMissing = proplist.getValue("KeepMissing");
        if (KeepMissing != null) {
            if (KeepMissing.equalsIgnoreCase("TRUE")) {
                missingValueFlag = 0;
            } else if (KeepMissing.equalsIgnoreCase("FALSE")) {
                // Do nothing.
            } else {
                warning = "KeepMissing '" + KeepMissing + "' is not valid. Valid values are:" + " TRUE or FALSE.";
                throw new TSException(warning);
            }
        }

        // REVISIT [LT] 2005-02-18 - The AllowMissingPercent will not be used
        // at this time. SAM decided to remove this property
        // because of the way the AllowMissingCount_int was computed,
        // using the intervalRelation. For instance going from monthly to
        // daily, intervalRelation will always consider 30 days months and
        // this may be a issue.
        // ??????????????????????????????????????????????????????????????????????
        // AllowMissingCount or AllowMissingPercent properties.
        // Only one of these properties is expected.
        /*
         * String AllowMissingCount = proplist.getValue( "AllowMissingCount" ); String AllowMissingPercent = proplist.getValue( "AllowMissingPercent" ); // If both are
         * given, throw an exception. if( allowMissingCount != null && allowMissingPercent != null ) { warning = "Only one of these properties should be set: " +
         * "AllowMissingCount or AllowMissingPercent."; throw new TSException ( warning ); }
         */
        // ??????????????????????????????????????????????????????????????????????

        // AllowMissingCount
        String AllowMissingCount = proplist.getValue("AllowMissingCount");
        int AllowMissingCount_int = 0; // Default is don't allow missing

        if (AllowMissingCount != null) {
            if (!StringUtil.isInteger(AllowMissingCount)) {
                warning = "AllowMissingCount (" + AllowMissingCount + ") is not an integer.";
                throw new TSException(warning);
            } else {
                // Get the value.
                AllowMissingCount_int = StringUtil.atoi(AllowMissingCount);

                // If the given value is less than 0, throw exception.
                if (AllowMissingCount_int < 0) {
                    warning = "AllowMissingCount (" + AllowMissingCount + ") is negative.";
                    throw new TSException(warning);
                }
            }
        }

        // REVISIT [LT] 2005-02-18 - The AllowMissingPercent will not be used
        // at this time. SAM decided to remove this property from the
        // because of the way the AllowMissingCount_int was computed,
        // using the intervalRelation. For instance going from monthly to
        // daily, intervalRelation will always consider 30 days months and
        // this may be a issue.
        // ??????????????????????????????????????????????????????????????????????
        // AllowMissingPercent
        /*
         * boolean AllowMissingPercent_boolean = false; int AllowMissingPercent_double = 0.0;// Default is don't allow missing
         * 
         * if( AllowMissingPercent != null ) { if ( !StringUtil.isDouble( AllowMissingPercent ) ) { warning = "AllowMissingPercent (" + AllowMissingPercent + ") is not a
         * double."; throw new TSException ( warning ); } else { // Get the value AllowMissingPercent_double = StringUtil.atof( AllowMissingPercent );
         *  // If the given value is < 0, or > 100 throw and // exception. if ( AllowMissingPercent_double < 0.0 ) { warning = "AllowMissingPercent (" +
         * AllowMissingPercent_double + ") is negative."; throw new TSException ( warning ); } else if ( AllowMissingPercent_double > 100.0 ) { warning =
         * "AllowMissingPercent (" + AllowMissingPercent_double + ") is greater than 100.0."; throw new TSException ( warning ); }
         * 
         * AllowMissingPercent_boolean = true; }
         */
        // ??????????????????????????????????????????????????????????????????????
        // Create the new time series
        // From the old time series identifier create the new time series
        // identifier.
        TSIdent newtsIdent = new TSIdent(oldTS.getIdentifier());
        newtsIdent.setInterval(newtsBase, newtsMultiplier);

        // Create the new time series using the new identifier.
        TS newTS = TSUtil.newTimeSeries(newtsIdent.getIdentifier(), true);
        // newTS = newTimeSeries( new_interval, false ); // or simple 1Month
        if (newTS == null) {
            warning = "Could not create the new time series.";
            throw new TSException(warning);
        }

        // Get the bounding dates for the new time series based on the
        // old time series.
        DateTime newts_date[] = getBoundingDatesForChangeInterval(oldTS, newtsBase, newtsMultiplier);

        // Update the new time series properties with all required information.
        // Notice: CopyHeader() overwrites, among several other things,
        // the Identifier, the DataInterval (Base and Multiplier).
        // It also set the dates, from the old time series. Make sure to
        // reset these properties to the values needed by the new time
        // series.
        newTS.copyHeader(oldTS);
        newTS.setDataType(NewDataType);
        newTS.setIdentifier(newtsIdent);
        newTS.setDataInterval(newtsBase, newtsMultiplier);
        newTS.setDate1(newts_date[0]);
        newTS.setDate2(newts_date[1]);
        newTS.setDate1Original(oldTS.getDate1());
        newTS.setDate2Original(oldTS.getDate2());
        
        // Set the units if specified...
        if ( (NewUnits != null) && !NewUnits.equals("") ) {
            newTS.setDataUnits( NewUnits );
        }

        // Finally allocate data space.
        newTS.allocateDataSpace();

        // Currently it is not possible to change interval from regular to
        // irregular. ( these might be implemented later to get, e.g., annual
        // peak flows with dates )
        if (newTS.getDataIntervalBase() == TimeInterval.IRREGULAR) {
            warning = "Change intervals from regular to irregular " + " time series is not supported.";
            throw new TSException(warning);
        }

        // Debugging messages.
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if (Message.isDebugOn) {
            status = "oldTS Identifier = " + oldTS.getIdentifier().toString();
            Message.printStatus(2, routine, status);

            status = "NewDataType = " + NewDataType;
            Message.printStatus(2, routine, status);
            status = "newtsBase = " + newtsBase;
            Message.printStatus(2, routine, status);
            status = "newtsMultiplier = " + newtsMultiplier;
            Message.printStatus(2, routine, status);
            status = "new_interval = " + new_interval;
            Message.printStatus(2, routine, status);

            status = "newTS.getIdentifier() = " + newTS.getIdentifier().toString();
            Message.printStatus(2, routine, status);
            status = "newTS.getDataType() = " + newTS.getDataType();
            Message.printStatus(2, routine, status);
            status = "newTS.getDataIntervalBase() = " + newTS.getDataIntervalBase();
            Message.printStatus(2, routine, status);
            status = "newTS.getDataIntervalMult() = " + newTS.getDataIntervalMult();
            Message.printStatus(2, routine, status);
        }
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        // Check if the intervals are multiples of each other.
        // The intervalRelation is in fact the number of intervals of one ts in
        // a single interval of the other for conversion that does not involve
        // IRREGULAR time series. If the old interval is smaller than the newer
        // interval the intervalRelation is negative. If the old interval is
        // greater than the new interval the intervalRelation is positive.
        // If the intervals are not multiple of each other than the
        // intervalRelation is 0. If the older time sereis is IRREGULAR the
        // intervalRelation is -1. Cases where both time series are IRREGULAR
        // or the newer time series is IRREGULAR should not happen because they
        // are not supported and the code should throw an exception before get
        // here.
        int intervalRelation = getIntervalRelation(oldTS, newTS);
        if (intervalRelation == 0) {
            warning = "Intervals are not multiples of each other. " + "Cannot change interval.";
            throw new TSException(warning);
        }

        // Using AllowMissingPercent to get allowMissingCount_int
        // REVISIT [LT] 2005-02-18 - The AllowMissingPercent will not be used
        // at this time. SAM decided to remove this property from the
        // because of the way the AllowMissingCount_int was computed,
        // using the intervalRelation. For instance going from monthly to
        // daily, intervalRelation wil always consider 30 days months and
        // this may be a issue.
        // ??????????????????????????????????????????????????????????????????????
        // If the AllowMissingPercent property was given, get the its value.
        // Indicate the percent of missing values that can
        // be missing in the input data and still allow computation
        // of the result.
        /*
         * if ( AllowMissingPercent_boolean ) {
         *  // Compute the number of missing datapoints allowed per interval // based // on the AllowMissingPercent property value. // Notice: Because the
         * intervalRelation can be negative to indicate // that the old interval is less than the new interval, // we need to use the abs(intervalRelation) to properly
         * get // a positive number of allowed missing values. allowMissingCount_int = Math.abs(intervalRelation) * allowMissingPercent_double / 100.0; }
         */
        // ??????????????????????????????????????????????????????????????????????

        // Define the OldTS Iterator
        TSIterator oldTSi = null;
        oldTSi = oldTS.iterator(oldTS.getDate1(), oldTS.getDate2());
        oldTSi.setBeginTime(oldTS.getDate1());

        // Define the NewTS Iterator
        TSIterator newTSi = null;
        newTSi = newTS.iterator();

        // Set the iterator of the new time series to be the first data point
        // possible to be computed, if needed.
        // TODO SAM 2007-03-01 Evaluate use
        // DateTime newTSAdjustedStartDate = newTS.getDate1();
        if (intervalRelation < 0) {
            // Older interval < than newer interval
            while (oldTSi.getDate().greaterThan(newTSi.getDate())) {
                newTSi.next();
            }
        } else {
            // Older interval >= than newer interval
            while (newTSi.getDate().lessThan(oldTSi.getDate())) {
                newTSi.next();
            }
        }
        newTSi.setBeginTime(newTSi.getDate());

        // From this point on, do not run the next() method for either the old
        // or the new time series. Let the helper methods deal with the
        // iterations starting from the beginning.

        // Debuging messages.
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        status = " ts" + "\t" + "data1     " + "\t" + "date      " + "\t" + "date2     ";
        Message.printStatus(2, routine, status);
        status = " Old" + "\t" + oldTS.getDate1().toString() + "\t" + oldTSi.getDate().toString() + "\t" + oldTS.getDate2().toString();
        Message.printStatus(2, routine, status);
        status = " New" + "\t" + newTS.getDate1().toString() + "\t" + newTSi.getDate().toString() + "\t" + newTS.getDate2().toString();
        Message.printStatus(2, routine, status);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        // Processing all the different change interval options.
        boolean returnTS = false;

        if (oldTS.getDataIntervalBase() == TimeInterval.IRREGULAR) {

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
            if ((OldTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM))
                    || ((OldTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN) || OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST)) && NewTimeScale
                            .equalsIgnoreCase(MeasTimeScale.MEAN))
                    || (OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.INST))) {
                if (changeInterval_fromIRREGULAR(oldTSi, newTSi, OldTimeScale, NewTimeScale, missingValueFlag)) {
                    returnTS = true;
                }
            } else {
                warning = "Conversion from '" + OldTimeScale + "' to '" + NewTimeScale + "' is not supported!";
                throw new TSException(warning);
            }
        }

        else {
            // Depending on the scale, different methods are used
            if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN) || OldTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM)) {
                // -----------------------------------------------------
                // From REGULAR ACCM and MEAN input TS use routine
                // changeInterval_toMEANorACCM
                // REGULAR ACCM to MEAM
                // REGULAR MEAN to MEAN
                // REGULAR ACCM to ACCM
                // REGULAR MEAN to ACCM
                // REGULAR ACCM to INST ( Not supported )
                // REGULAR MEAN to INST ( Not supported )
                // -----------------------------------------------------
                if ((NewTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN)) || (NewTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM))) {
                    if (changeInterval_toMEANorACCM(oldTSi, newTSi, intervalRelation, OldTimeScale, NewTimeScale, missingValueFlag, AllowMissingCount_int,
                            OutputFillMethod)) {
                        returnTS = true;
                    }
                } else {
                    warning = "Conversion from \"" + OldTimeScale + "\" to \"" + NewTimeScale + "\" is not supported!";
                    throw new TSException(warning);
                }
            }

            else if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST)) {

                if (NewTimeScale.equalsIgnoreCase(MeasTimeScale.INST)) {
                    // ---------------------------------------------
                    // From REGULAR INST to INST use routine
                    // changeInterval_fromINST
                    // ---------------------------------------------
                    if (changeInterval_fromINST(oldTSi, newTSi, intervalRelation, OldTimeScale, NewTimeScale, missingValueFlag)) {
                        returnTS = true;
                    }
                } else if (NewTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN)) {
                    // ---------------------------------------------
                    // From REGULAR INST to MEAN use routine
                    // changeInterval_toMEANorACCM
                    // ---------------------------------------------
                    if (changeInterval_toMEANorACCM(oldTSi, newTSi, intervalRelation, OldTimeScale, NewTimeScale, missingValueFlag, AllowMissingCount_int,
                            OutputFillMethod)) {
                        returnTS = true;
                    }
                } else {
                    // ---------------------------------------------
                    // From REGULAR INST to ACCM, (not supported)
                    // ---------------------------------------------
                    warning = "Conversion from \"" + OldTimeScale + "\" to \"" + NewTimeScale + "\" is not supported!";
                    throw new TSException(warning);
                }
            }

            else {
                warning = "Cannot change interval from time scales '" + OldTimeScale + "' to '" + NewTimeScale + "'.";
                throw new TSException(warning);
            }
        }

        // Set to null for garbage collection.
        oldTSi = null;
        newTSi = null;

        if (returnTS) {
            return newTS;
        }

        // To prevent compiler from complaining
        return null;
    }

    /**
     * Change intervals from INST time series. Call only from TSUtil.changeInterval! Supported conversion are:
     * <p>
     * <table width=100% cellpadding=10 cellspacing=0 border=2>
     * <tr>
     * <td><b>From</b></td>
     * <td><b>To</b></td>
     * </tr>
     * <tr>
     * <td><b>INST</b></td>
     * <td><b>INST</b></td>
     * </tr>
     * </table>
     * 
     * @param oldTSi Reference to the iterator object for the old time series.
     * @param newTSi Reference to the iterator object for the new time series.
     * @param intervalRelation ratio of intervals, negative if newTS interval is longer than the oldTS.
     * @param OldTimeScale - The time scale of the old time series.
     * @param NewTimeScale - The time scale of the new time series.
     * @param missingValueFlag Indicates how to treat missing values in the input TS
     *            <p>
     *            <table width=100% cellpadding=10 cellspacing=0 border=2>
     *            <tr>
     *            <td><b>Value/b></td>
     *            <td><b>Action</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>0</b></td>
     *            <td><b>leave missing</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>1</b></td>
     *            <td><b>set to zero</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>2</b></td>
     *            <td><b>use persistence</b></td>
     *            </tr>
     * @return true if successful or false if an error.
     */
    private boolean changeInterval_fromINST(TSIterator oldTSi, TSIterator newTSi, int intervalRelation, String OldTimeScale, String NewTimeScale,
            int missingValueFlag) throws Exception {
        String routine = "TSUtil.changeInterval_fromINST", warning;

        // Make sure the required conversion is supported
        if (!(OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.INST))) {
            warning = "Conversion from \"" + OldTimeScale + "\" to \"" + NewTimeScale + "\" is not supported!";
            throw new TSException(warning);
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

            for (; newTSi.next() != null;) {

                currentValue = newMissing;
                // Just use the last recorded instantaneous old
                // currentValue within the new interval
                while (oldData != null && oldTSi.getDate().lessThanOrEqualTo(newTSi.getDate())) {

                    // Assign value only if dates are equal
                    if (oldTSi.getDate().equals(newTSi.getDate())) {
                        currentValue = oldTSi.getDataValue();
                    }

                    // Replace missing data currentValue:
                    // by newMissing if REGULAR data;
                    // by 0 if ALERT INCREMENT data and
                    // by lastValue if ALERT REGULAR.
                    if (oldTS.isDataMissing(currentValue)) {
                        currentValue = replaceDataValue(missingValueFlag, lastValue, newMissing);
                    } else {
                        lastValue = currentValue;
                    }

                    oldData = oldTSi.next();
                }

                if (Message.isDebugOn) {
                    warning = "Old TS: " + oldTSi.getDate().toString() + " -> " + String.valueOf(currentValue) + " %%%  New TS: " + newTSi.getDate().toString();
                    Message.printDebug(40, routine, warning);
                }

                newTS.setDataValue(newTSi.getDate(), currentValue);
            }
        }

        else {
            // The old interval is longer than the new interval.
            // Passing true as the last parameter, meaning that it is not
            // necessary to consider where to time stamp the data because
            // this method deals only with instantaneous data.
            return changeIntervalFromInstByInterpolation(oldTSi, newTSi, intervalRelation, OldTimeScale, NewTimeScale, missingValueFlag, true);
        }

        return true;
    }

    /**
     * Change intervals from INST time series by interpolation. This method should only be used when converting from larger to shorter interval where interpolation makes
     * sense. This method should only be called from TSUtil.changeInterval_fromINST and TSUtil.changeInterval_toMEANorACCM when the old interval < new interval and for
     * the following conversion:
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
     * @param intervalRelation ratio of intervals, negative if newTS interval is longer oldTS. This parameter is used only for verification.
     * @param OldTimeScale - The time scale of the old time series. This parameter is used only for verification.
     * @param NewTimeScale - The time scale of the new time series. This parameter is used only for verification.
     * @param missingValueFlag Indicates how to treat missing values in the input TS
     *            <p>
     *            <table width=100% cellpadding=10 cellspacing=0 border=2>
     *            <tr>
     *            <td><b>Value/b></td>
     *            <td><b>Action</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>0</b></td>
     *            <td><b>leave missing</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>1</b></td>
     *            <td><b>set to zero</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>2</b></td>
     *            <td><b>use persistence</b></td>
     *            </tr>
     *            </table>
     * @param timeStampedAtEnd - This argument is used to time stamp the data at the begining of the period. If passed as false, the data will be time stamped at end of
     *            the interval, otherwise at the begining.
     * @return true if successful or false if an error.
     */
    private boolean changeIntervalFromInstByInterpolation(TSIterator oldTSi, TSIterator newTSi, int intervalRelation, String OldTimeScale, String NewTimeScale,
            int missingValueFlag, boolean timeStampedAtEnd) throws Exception {
        String routine = "TSUtil.changeIntervalFromInstByInterpolation", warning;

        //Message.printStatus(2, routine, " Running ...");

        // Make sure the required conversion is supported
        if (!(OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.INST))
                && !(OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN))) {
            warning = "Conversion from \"" + OldTimeScale + "\" to \"" + NewTimeScale + "\" is not supported!";
            throw new TSException(warning);
        }

        TS newTS = newTSi.getTS();
        TS oldTS = oldTSi.getTS();

        // Make sure the older interval is longer than the newer interval.
        if (intervalRelation < 0) {
            warning = "This method should only be applied to conversions " + "from longer to shorter time intervals!";
            throw new TSException(warning);
        }

        // Old time series interval is indeed longer then the new time series
        // interval.

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

        // Loop through the older time series then the newer timeseries

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
            previousValue = replaceDataValue(missingValueFlag, lastValue, oldMissing);
        } else {
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
        // of the old timeseries.
        for (; oldTSi.next() != null;) {

            // Get data value and check and replace missing data
            currentDateDouble = oldTSi.getDate().toDouble();
            currentValue = oldTSi.getDataValue();

            if (oldTS.isDataMissing(currentValue)) {
                // Replace missing data value:
                // by oldMissing if REGULAR data;
                // by 0 if ALERT INCREMENT data and
                // by lastValue if ALERT REGULAR.
                currentValue = replaceDataValue(missingValueFlag, lastValue, oldMissing);
            } else {
                lastValue = previousValue;
            }

            // Set the missing flag for the current value.
            currentValueMissing = oldTS.isDataMissing(currentValue);

            // Save the first point (missing or previousValue) and advance
            // to the next.
            // REVISIT [LT] 2005-03-01. Are we sure that this first old data
            // point is aligned with the new data
            // point? Maybe not! Resolve ASAP.
            if (timeStampedAtEnd) {
                newDate = new DateTime(newTSi.getDate());
            } else {
                // Previous date (one interval) if >= DAY
                newDate = new DateTime(newTSpreviousDate);
            }

            // Saving
            if (previousValueMissing) {
                newTS.setDataValue(newDate, newMissing);
            } else {
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
                    } else {
                        // Previous date(one interval) if >= DAY
                        newDate = new DateTime(newTSpreviousDate);
                    }

                    // Saving the current date as previous date.
                    newTS.setDataValue(newDate, newMissing);
                    newTSi.next();
                }
            } else { // Interpolate the new values linearly

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
                    } else {
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
        } else {
            // Previous date (one interval) if >= DAY
            newDate = new DateTime(newTSpreviousDate);
        }
        // Saving
        if (currentValueMissing) {
            newTS.setDataValue(newDate, newMissing);
        } else {
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
     * @param OldTimeScale The time scale of the old time series. This parameter is used only for verification.
     * @param NewTimeScale The time scale of the new time series. This parameter is used only for verification.
     * @param missingValueFlag Indicates how to treat missing values in the input TS
     *            <p>
     *            <table width=100% cellpadding=10 cellspacing=0 border=2>
     *            <tr>
     *            <td><b>Value/b></td>
     *            <td><b>Action</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>0</b></td>
     *            <td><b>leave missing</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>1</b></td>
     *            <td><b>set to zero</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>2</b></td>
     *            <td><b>use persistence</b></td>
     *            </tr>
     *            </table>
     */
    private boolean changeInterval_fromIRREGULAR(TSIterator oldTSi, TSIterator newTSi, String OldTimeScale, String NewTimeScale, int missingValueFlag)
            throws Exception {
        String routine = "TSUtil.changeInterval_fromIRREGULAR", mssg, warning;

        //Message.printStatus(2, routine, " Running ...");

        // Allowed TimeScale to convert to.
        final int ACCM = 1;
        final int MEAN = 2;
        final int INST = 3;

        // Make sure the required conversion is supported.
        // If supported set the int newTimeScale variable to match the
        // NewTimeScale string. Using int instead of String will prevent time
        // consuming string comparisons during processing.
        int newTimeScale = 0;
        if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM)) {
            newTimeScale = ACCM;
        } else if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN)) {
            newTimeScale = MEAN;
        } else if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN)) {
            newTimeScale = MEAN;
        } else if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.INST)) {
            newTimeScale = INST;
        } else {
            warning = "Conversion from '" + OldTimeScale + "' to '" + NewTimeScale + "' is not supported!";
            throw new TSException(warning);
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

        // Do not execute the conversion for these conditions until the
        // method is fully implemented.
        if (newTimeScale != INST && TSBase >= TimeInterval.DAY) {
            warning = "Conversion from Irregular '" + OldTimeScale + "' to regular other than instantaneous'" + NewTimeScale + "' (interval>=DAY ) is not yet supported!";
            throw new TSException(warning);
        }

        // Others
        boolean incrementDate = false;
        double sum = 0;
        int pointsInInterval = 0;

        // REVISIT [LT] 2005-03-02 - Pending documentation.
        boolean first = true;
        // REVISIT [LT] 2005-06-06 - Comment this was required to proper
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
        if (oldData == null) { // Should never happen
            warning = "First TSData object from the oldTS returned null!";
            throw new TSException(warning);
        }
        if (newData == null) { // Should never happen
            warning = "First TSData object from the newTS returned null!";
            throw new TSException(warning);
        }

        // REVISIT [LT] 2005-02-18.
        // ??????????????????????????????????????????????????????????????????????
        // At the first time step, make sure we have enough data for the
        // new interval
        DateTime minumumOldDate = newTSi.getDate();
        minumumOldDate.addInterval(TSBase, TSMult * -1);
        oldDate = oldTSi.getDate();

        if (Message.isDebugOn) {
            mssg = " Old initial date is: " + oldDate.toString();
            Message.printDebug(10, routine, mssg);
            mssg = " New initial date is: " + newTSi.getDate().toString();
            Message.printDebug(10, routine, mssg);
            mssg = " minumumOldDate   is: " + minumumOldDate.toString();
            Message.printDebug(10, routine, mssg);
        }

        while (oldDate.lessThanOrEqualTo(minumumOldDate, highestPrecision)) {
            oldData = oldTSi.next();
            if (oldData != null) {
                oldDate = oldTSi.getDate();
            } else { // There is no data in the oldTS?
                warning = "There is no data in the oldTS!";
                throw new TSException(warning);
            }
        }

        if (Message.isDebugOn) {
            mssg = " Old adjusted date is: " + oldDate.toString();
            Message.printDebug(10, routine, mssg);
        }
        // ??????????????????????????????????????????????????????????????????????

        // While Loop
        while (true) {

            // REVISIT [LT] 2005-03-06 - This is one of the possible
            // location for the code to make this method general.
            newDate = newTSi.getDate();
            if (oldData != null) {
                oldDate = oldTSi.getDate();
            }

            // Save the previous value
            previousOldTsValue = oldTSValue;
            lastNewTSValue = newTSValue;

            if (oldData != null && oldDate.equals(newDate, highestPrecision)) {

                Message.printWarning(2, "", "= Old date is: " + oldDate.toString());
                Message.printWarning(2, "", "= New date is: " + newDate.toString());

                if (first) {
                    // Do not set data at the first time interval
                    // we do not know for which time scale it was
                    // valid
                    first = false;
                } else {

                    oldTSValue = oldTSi.getDataValue();
                    if (oldTS.isDataMissing(oldTSValue)) {

                        // Replace missing data value:
                        // by newMissing if REGULAR data;
                        // by 0 if ALERT INCREMENT data and
                        // by previousOldTsValue if
                        // ALERT REGULAR.
                        if (missingValueFlag > -1) {
                            oldTSValue = replaceDataValue(missingValueFlag, previousOldTsValue, newMissing);
                            // REVISIT [LT] 2005-03-06 - Why MT used
                            // oldMissing and not newMissing here?
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
            // we have to fill in one or more regular 'new' datapoints
            // in this irregular 'old' interval
            // REVISIT [LT] Does this EqualTo ever get executed, considering
            // the equals() test above?
            else if (oldData != null && oldDate.greaterThanOrEqualTo(newDate, highestPrecision)) {

                if (first) {
                    first = false;
                    newData = newTSi.next();
                    if (newData == null) {
                        break;
                    }
                } else {

                    // Get the irregular data value at the current old TS
                    // date
                    oldTSValue = oldTSi.getDataValue();

                    Message.printWarning(2, "", " >= Old date is: " + oldDate.toString());
                    Message.printWarning(2, "", " >= New date is: " + newDate.toString());

                    // Just to make sure to capture missing data in the
                    // irregularTS
                    while (oldTS.isDataMissing(oldTSValue)) {
                        // Increment irregular 'old' date
                        oldData = oldTSi.next();
                        Message.printWarning(2, "", " >= Old date is: " + oldDate.toString());
                        // If there is a data point, get value also
                        // fill new TS with data in that interval
                        if (oldData != null) {
                            oldTSValue = oldTSi.getDataValue();
                            oldDate = oldTSi.getDate();
                        } else { // End of data, need a value
                            oldTSValue = previousOldTsValue;
                            break;
                        }
                    }

                    // Find out how many regular intervals will be in the
                    // irregular interval. This return 1 even when less than
                    // one interval fits between the dates!!
                    // i.e. if newDate is xx:xx:xx 6:00 and old date is
                    // xx:xx:xx 6:45 and the new interval is 1 hour, this
                    // will return 1
                    noNewDataPoints = TimeUtil.getNumIntervals(newDate, oldDate, TSBase, TSMult) + 1;
                    Message.printWarning(2, "", "# of interv: " + noNewDataPoints);
                    Message.printWarning(2, "", "New date is: " + newDate.toString());

                    // ??????????????????????????????????????????????????????
                    // REVISIT [LT] 2005-02-28 We may need to have something
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

                        switch (missingValueFlag) {
                        case 1: // ALERT_INCREMENT
                        case 2: // ALERT_REGULAR
                            // Take care of ALERT type data set to
                            // respective 'missing' value if dates
                            // are not equal.
                            if (oldDate.equals(newDate, highestPrecision)) {
                                newTSValue = oldTSValue;
                                // ....2........4.......1.....2
                                // ......x......4......x......2
                            } else {
                                // .............4.............2
                                // 0000 00000000 0000000 00000
                                // .............4444444 4444442
                                // Replace missing data value:

                                // Replace missing data value:
                                // by 0 if ALERT INCREMENT data
                                // by previousOldTsValue if
                                // ALERT REGULAR.
                                newTSValue = replaceDataValue(missingValueFlag, previousOldTsValue, newTSValue);// This last
                                // value is not used, be
                                // cause we are proces
                                // sing cases 1 & 2 only
                            }
                            break;
                        case 0: // REGULAR
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
                                // Includes instantaneous
                                // interpolate
                                if (previousOldTsValue != oldMissing) {
                                    pointsInInterval++;
                                    newTSValue = previousOldTsValue + (i + 1) / (double) noNewDataPoints * (oldTSValue - previousOldTsValue);

                                    // ??????????????????????????????????????????????????????
                                    // REVISIT [LT] 2005-02-28 We may need to have something
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
                                    // the newDate which may not coinside with
                                    // the date of the previousOldTsValue.
                                    // ??????????????????????????????????????????????????????

                                } else { // no last datapoint to
                                    // interpolate
                                    // use last known value
                                    newTSValue = oldTSValue;
                                }
                            }
                            break;
                        default: // KEEP MISSING (-1)
                            if (oldDate.equals(newDate, highestPrecision)) {
                                newTSValue = oldTSValue;
                            } else {
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
                            if (missingValueFlag > 0) {
                                // ALERT_INCREMENT and
                                // ALERT REGULAR
                                incrementDate = false;
                                // newTSValue = oldTSValue;
                            } else {
                                // REGULAR and
                                // KeepMissing
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

            // Here the oldTSdate is smaller than the newTSDate - we have
            // to work on the old data
            // REVISIT [LT] Does this EqualTo ever get executed, considering
            // the equals() test above the previos test?
            else { // oldTSi->getDate()<newTSi->getDate() or OldData==NULL

                Message.printWarning(2, "", "< Old date is: " + oldDate.toString());
                Message.printWarning(2, "", "< New date is: " + newDate.toString());

                // Loop through the old irregular intervals until the
                // current oldDateTime is larger or equal to the
                // newDateTime
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
                // REVISIT [LT] 2005-02-28 Here we should find out the
                // proportional value for ACCM,
                // MEAN anD the interpolation
                // interval.
                // newTSValue = sum/(iLeft + i + iRight);
                // ?????????????????????????????????????????????????????

                // Now the oldDateTime is larger than the newDateTime
                // process the old data in this interval

                if (i > 0) { // Means we have non missing old data

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
                        // Includes instantaneous. Set to last
                        // non missing value in interval
                        newTSValue = lastValidOldTSValue;
                    }
                }

                else { // There is no old data for this new TS
                    // interval. The value for this new TS data
                    // point would be missing, but we allow it to be
                    // changed, if ALERT type data is used

                    // Replace missing data value:
                    // by 0 if ALERT INCREMENT data;
                    // by lastNewTSValue if ALERT REGULAR and
                    // by newMissing if REGULAR data.
                    if (missingValueFlag > -1) {
                        newTSValue = replaceDataValue(missingValueFlag, lastNewTSValue, newMissing);
                    } else {
                        // REVISIT [LT] 2005-03-05 - Same as
                        // using the replaceDataValue() method.
                        // By default with missingValueFlag 0 or
                        // -1, the data is replaced by the 3rd
                        // parameter which is newMissing.
                        // This else{} block should not be
                        // required!
                        newTSValue = newMissing;
                    }
                }

                // Set new data value
                // REVISIT [LT] 2005-03-06 - This is one of the possible
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
     * @param OldTimeScale - The time scale of the old time series: MeasTimeScale.INST if the oldTS is an instantaneous, MeasTimeScale.ACCM if the oldTS is an
     *            accumulation, MeasTimeScale.MEAN if the oldTS is mean.
     * @param NewTimeScale - The time scale of the new time series, MeasTimeScale.ACCM if the newTS is an accumulation, MeasTimeScale.MEAN if the newTS is mean.
     * @param missingValueFlag Indicates how to treat missing values in the input TS
     *            <p>
     *            <table width=100% cellpadding=10 cellspacing=0 border=2>
     *            <tr>
     *            <td><b>Value/b></td>
     *            <td><b>Action</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>0</b></td>
     *            <td><b>leave missing</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>1</b></td>
     *            <td><b>set to zero</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>2</b></td>
     *            <td><b>use persistence</b></td>
     *            </tr>
     * @param maxMissingPerInterval the maximum number of missing value in the old time series allowed per new time series interval. New value will be considered missing
     *            if this max value is exceeded. This is only applicable when going from larger intervals to shorter ones.
     * @param String OutputFillMethod this argument is only used when going from larger intervals to smaller. It allow for the new values to be interpolated (INTERPOLATE)
     *            between the old data point, carried forward (CARRYFORWARD) from the 1st data point of the oldTS interval or backward (CARRYBACKWARD) from the last data
     *            point of the oldTS time interval.
     * @return true if successful or false if an error.
     */
    private boolean changeInterval_toMEANorACCM(TSIterator oldTSi, TSIterator newTSi, int intervalRelation, String OldTimeScale, String NewTimeScale,
            int missingValueFlag, int maxMissingPerInterval, String OutputFillMethod) throws Exception {
        String routine = "TSUtil.changeInterval_toMEANorACCM", warning;

        //Message.printStatus(2, routine, " Running ...");

        // Allowed TimeScale to convert to.
        final int ACCM = 1;
        final int MEAN = 2;
        final int INST = 3;

        // Make sure the required conversion is supported.
        // If supported set the int newTimeScale variable to match the
        // NewTimeScale string. Using int instead of String will prevent time
        // consuming string comparisons during processing.
        int newTimeScale = 0;
        int oldTimeScale = 0;
        if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN)) {
            oldTimeScale = ACCM;
            newTimeScale = MEAN;
        } else if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN)) {
            oldTimeScale = MEAN;
            newTimeScale = MEAN;
        } else if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN)) {
            oldTimeScale = INST;
            newTimeScale = MEAN;
        } else if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM)) {
            oldTimeScale = ACCM;
            newTimeScale = ACCM;
        } else if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM)) {
            oldTimeScale = MEAN;
            newTimeScale = ACCM;
        } else {
            warning = "Conversion from \"" + OldTimeScale + "\" to \"" + NewTimeScale + "\" is not supported!";
            throw new TSException(warning);
        }

        // Declaring timeseries, interval base and interval multipliers for the
        // newTS and the oldTS timeseries
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
        // we should not time stamp the new data at the begining.
        boolean oldTsTimeStampedAtEnd = true;
        // Valid only for ACCM and MEAN when the time step is < DAY and if not
        // converting between the same interval.
        // REVISIT [LT] Math.abs(intervalRelation) might not be necessary when
        // the whole logic is right. Or maybe. It ramains to find
        // out. 2005-03-07.
        if (oldTSBase >= TimeInterval.DAY && Math.abs(intervalRelation) != 1) {
            oldTsTimeStampedAtEnd = false;
        }

        // Same as day but still hourly time series.
        // REVISIT [LT 2005-03-25]. This test is not necessary. 24HOUR time
        // series is still Hourly time series and so the data should be
        // timestamped at the end.
        // if( oldTSBase == TimeInterval.HOUR && oldTSMult == 24 &&
        // Math.abs(intervalRelation) != 1 ) {
        // oldTsTimeStampedAtEnd = false;
        // }

        // Define if the new time series values should be time stamped at the
        // end of the interval ( interval < DAY ).
        // If the abs(intervalRelation) is 1, we are converting the old time
        // series to an new time series of the same interval. In this case
        // we should not time stamp the new data at the begining.
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
        double lastValue = -999.99, value, sum;
        int missingCount, dataCount;
        boolean missingFlag;

        // Change Interval
        if (intervalRelation < 0) {

            // Old interval < new interval.
            // Loop through the new time series then the old one.

            // This reseting of the oldTsTimeStampedAtEnd to FALSE if the
            // input time scale is INST, should only be used when the
            // input interval is shorter than the output interval.
            if (oldTimeScale == INST) {
                oldTsTimeStampedAtEnd = false;
            }

            // The first next() call does not increment the date.
            oldData = oldTSi.next();

            // If the old ts is < Day (stamped in the end) we need
            // to advance one old interval.
            if (oldTsTimeStampedAtEnd) {
                oldData = oldTSi.next();
            }

            // Save the previous newTS date.
            DateTime previousNewTSDate = new DateTime(newTSi.getDate());

            for (; newTSi.next() != null;) {

                sum = 0.0;
                missingCount = 0;
                dataCount = 0;
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
                // does not coinside with the new time series data
                // point.
                DateTime oldTSiPreviousDate = null;

                // Accumulate the values from the older timeseries.
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
                        value = replaceDataValue(missingValueFlag, lastValue, oldMissing);

                        // Increment the missing data indicator
                        // if value is still missing
                        if (oldTS.isDataMissing(value)) {
                            missingCount++;
                            missingFlag = true;
                        }

                        // Set quality flag to missing to
                        // indicate that data was missing.
                        // TODO SAM 2007-03-01 Evaluate how used
                        // qualityFlag = "M";
                    } else {
                        lastValue = value;
                    }

                    if (Message.isDebugOn) {
                        warning = "Old TS: " + oldTSi.getDate().toString() + " -> " + String.valueOf(value) + " %%%  New TS: " + newTSi.getDate().toString();
                        Message.printDebug(10, routine, warning);
                    }

                    // Accumulate if not missing.
                    if (!missingFlag) {
                        sum += value;
                        dataCount++;
                    }

                    oldData = oldTSi.next();
                }

                // Compute the value for the newer time series and
                // assign it.
                // If the new time series is < DAY (stamped at the
                // end) we need to assign the value to the new
                // time series current data point.
                // If the new time series is >= DAY (stamped at the
                // begining), we need to assign the value to the
                // previous new date.
                if (newTsTimeStampedAtEnd) {
                    // < DAY and it is not INST
                    newDate = new DateTime(newTSi.getDate());
                } else {
                    // >= DAY
                    newDate = new DateTime(previousNewTSDate);
                }

                // If the oldTSiPreviousDate is not equal to the
                // newTSi.getDate() that is because we are processing
                // the last interval in the period and the old time
                // series data terminate short than the new time series
                // date. If the end of the old time series coinside
                // with the new time series data point, this variable
                // will be null, so make sure to check for null first.
                if (oldTSiPreviousDate != null && !oldTSiPreviousDate.equals(newTS_endDate)) {
                    for (DateTime dt = new DateTime(oldTSiPreviousDate); dt.lessThanOrEqualTo(newTS_endDate); dt.addInterval(oldTSBase, oldTSMult)) {
                        missingCount++;
                    }
                }

                // Set the data value for this DateTime point to
                // missing, if the number of missing values is greater
                // than the max allowed or if no non-missing values
                // were found. Otherwise compute the new value either
                // as mean or accumulation.
                if (missingCount > maxMissingPerInterval || dataCount == 0) {
                    newTS.setDataValue(newDate, newMissing);
                    /* newDate, newMissing, qualityFlag, 0 ); */
                    // REVISIT [LT] 2005-03-01 Quality flag is
                    // currently not used.
                } else {
                    // If MEAN, divide by the number of points.
                    if (newTimeScale == MEAN) {
                        sum /= dataCount;
                    }

                    // Set data value for this date.
                    // Mean or accumulation.
                    newTS.setDataValue(newDate, sum);
                    // newDate, sum, qualityFlag, 0 );
                    // REVISIT [LT] 2005-03-01 Quality flag
                    // is cureently not used.
                }

                // Save current newTS date as the previous newTS date
                // for the next iteration.
                previousNewTSDate = new DateTime(newTSi.getDate());
            }
        }

        else {
            // Old interval > new interval.
            // Loop through the old time series then the new one.

            // For conversion from INST to MEAN, interpolation may be more
            // appropriated than moving the data FORWARD or BACKWARD from
            // the bounding data points.
            if (OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST) && NewTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN)
                    && OutputFillMethod.equalsIgnoreCase("INTERPOLATE")) {

                return changeIntervalFromInstByInterpolation(oldTSi, newTSi, intervalRelation, OldTimeScale, NewTimeScale, missingValueFlag, newTsTimeStampedAtEnd);
            }

            // Disable this part of the code, if not working as expected
            /*
             * warning = "Conversion from \"" + OldTimeScale + "\" to \"" + NewTimeScale + "\" in reverse is not yet supported!"; throw new TSException ( warning );
             */

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
                if (oldTsTimeStampedAtEnd) { // < Day
                    value = oldTSi.getDataValue();
                } else { // >=Day
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
                    value = replaceDataValue(missingValueFlag, lastValue, newMissing);
                    // Set the missing flag to false
                    // if data was left missing
                    if (oldTS.isDataMissing(value)) {
                        missingFlag = true;
                    }
                } else {
                    lastValue = value;
                }

                // Computer the date of the last data point of the newTS
                // to assign values to.
                // First the date valid when the oldTS interval is < DAY
                DateTime newTS_endDate = new DateTime(oldTSi.getDate());
                // Then update it if the oldTS interval is >=DAY or INST
                if (!oldTsTimeStampedAtEnd) {
                    newTS_endDate.addInterval(newTSBase, -newTSMult);
                }

                // How many data points to assign values to?
                // REVISIT [LT] 2005-03-01 - This block of code could
                // run only twice, assuming that the data count for the
                // first iteration might be less then the number of
                // actual number of new interval in the old interval.
                // But since this code in general, it apply also to
                // months, when the number of new interval in the old
                // interval will change from month to month
                // Consider add control flags to improve the performance
                // when running conversions from time series with
                // intervals shorter than month.
                if (newTimeScale == ACCM || (oldTimeScale == ACCM && newTimeScale == MEAN)) {
                    // to ACCM or from ACCM to MEAN
                    dataCount = 0;

                    if (oldTsTimeStampedAtEnd) {
                        // Less than Day, use the intervalRelation
                        dataCount = intervalRelation;
                    } else {
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
                } else {
                    // from INST or MEAN to MEAN
                    dataCount = 1;
                }

                // Assign the values to the output time series
                while (newTSi.getDate().lessThanOrEqualTo(newTS_endDate)) {

                    newDate = new DateTime(newTSi.getDate());
                    if (missingFlag == true) {
                        newTS.setDataValue(newDate, newMissing);
                    } else {
                        newTS.setDataValue(newDate, value / dataCount);
                        // ,qualityFlag, 0 );
                        // REVISIT [LT] 2005-03-01
                        // Quality flag is currently
                        // not used for this method.
                    }
                    newTSi.next();
                }

                previousOldValue = oldTSi.getDataValue();
            }
        }
        return true;
    }

    /**
     * @return New DayTS that has been converted from an irregular time series.
     * @param oldts Irregular time series to convert.
     * @param newmult New daily time series.
     * @param proplist Controlling parameters.
     */
    public DayTS OLDchangeToDayTS(IrregularTS oldts, int newmult, PropList proplist) {
        String routine = "TSUtil.changeToDayTS(IrregularTS)";
        int dl = 20;

        if (Message.isDebugOn) {
            Message.printDebug(dl, routine, "Changing to " + newmult + "-day time series");
        }

        // First declare a new time series..

        DayTS newts = new DayTS();

        // Copy the header and the dates. Set the dates as the rounded dates
        // to handle the old time series...

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

        // Now loop through the new period and fill in with averaged irregular
        // data as it is found...

        DateTime start = new DateTime(newts.getDate1());
        DateTime end = new DateTime(newts.getDate2());
        DateTime date_new = new DateTime(start);

        // Initialize the date after which we can accumulate to...

        DateTime previous_date = new DateTime(date_new);
        previous_date.addDay(-newmult);

        // Initialize the irregular data...

        Vector alldata = oldts.getData();
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
                data = (TSData) alldata.elementAt(i);
                t = data.getDate();
                if (Message.isDebugOn) {
                    Message.printDebug(dl, routine, "Processing IRRTS date " + t.toString(DateTime.FORMAT_Y2K_LONG));
                }

                // If the irregular data date is greater than the
                // current loop date, then we accumulate and break
                // (so the day interval updates). Also make sure to
                // consider the last observation (move that last check
                // outside the loop to increase performance)...

                // Put this first so we can check to see if we have
                // data...

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
                    // counter. First do the computations and
                    // transfer the numbers...
                    if (count > 0) {
                        ave = sum / (double) count;
                        // Now set the value in the new time
                        // series. It should be OK to use the
                        newts.setDataValue(date_new, ave);
                        if (Message.isDebugOn) {
                            Message.printDebug(dl, routine, "Averaged " + count + " values to get " + ave + " at " + date_new.toString(DateTime.FORMAT_Y2K_LONG));
                        }
                    } else {
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
     * Change monthly time series to daily. Only know how to change to 1-Day time series (not multiple day time series).
     * 
     * @return new DayTS or null if problems.
     * @param oldts Old MonthTS to convert.
     * @param newmult Multiplier for days (currently only 1 is accepted).
     * @param proplist Controlling parameters.
     */
    /*
     * REVISIT SAM private static DayTS changeToDayTS( MonthTS oldts, int newmult, PropList proplist ) { String routine="TSUtil.changeToDayTS(MonthTS)"; DayTS newts =
     * null; int dl = 20;
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Changing to " + newmult + "-day time series" ); }
     *  // First declare a new time series..
     * 
     * newts = new DayTS ();
     *  // Copy the header, but set the date to an even roundoff of the // interval...
     * 
     * newts.copyHeader ( oldts );
     * 
     * DateTime newts_date[] = getBoundingDatesForChangeInterval ( oldts, newts.getDataIntervalBase(), newmult ); newts.setDate1 ( newts_date[0] ); newts.setDate2 (
     * newts_date[1] );
     *  // Retain the original dates also... newts.setDate1Original ( oldts.getDate1Original() ); newts.setDate2Original ( oldts.getDate2Original() ); Message.printStatus (
     * 1, routine, "Using regular TS period " + newts.getDate1() + " to " + newts.getDate2 () );
     *  // Now override with new information...
     * 
     * newts.setDataInterval ( TimeInterval.DAY, newmult ); TSIdent tsident = newts.getIdentifier(); tsident.setInterval ( TimeInterval.DAY, newmult );
     *  // Allocate space based on the dates in the header...
     * 
     * newts.allocateDataSpace ();
     *  // Now loop through...
     * 
     * DateTime start = new DateTime ( oldts.getDate1() ); DateTime end = new DateTime ( oldts.getDate2() ); DateTime t = null, tprev = null; TSDateIterator tsdi; int
     * count = 0, i, minute, hour, day, month, year, prev_minute, prev_hour, prev_day, prev_month, prev_year; double ave, sum = 0.0, value; if ( Message.isDebugOn ) {
     * Message.printDebug ( dl, routine, "Iterator start/end are: " + start + " " + end ); } for( t = start, i = 0; t.lessThanOrEqualTo( end ); t.addInterval(
     * TimeInterval.DAY, newmult ), i++ ){
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Processing date " + t ); }
     * 
     * value = oldts.getDataValue(t); if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Value returned from MONTHTS is " + value ); } newts.setDataValue( t,
     * value ); } // Now fill by carrying forward... try { fillCarryForward ( newts ); } catch ( TSException e ) { ; // For now just rely on the previous warning about //
     * not being able to fill. }
     * 
     * return newts; }
     */

    /*
     * REVISIT SAM //12 Jul 1998 SAM, RTi Change so that the dates are nicely // rounded. //29 Jul 1998 CGB, RTi Implemented this method and include // SAM's comment as I
     * am not sure what // this comment refers to. private static HourTS changeToHourTS( HourTS oldts, int newmult, PropList proplist ) { String routine =
     * "TSUtil.changeToHourTS(HourTS)"; HourTS newts = null; int dl = 20;
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Changing to " + newmult + "-hour time series" ); }
     *  // First declare a new time series..
     * 
     * newts = new HourTS ();
     *  // Copy the header, but set the date to an even roundoff of the // interval...
     * 
     * newts.copyHeader ( oldts );
     * 
     * DateTime newts_date[] = getBoundingDatesForChangeInterval ( oldts, newts.getDataIntervalBase(), newmult ); newts.setDate1 ( newts_date[0] ); newts.setDate2 (
     * newts_date[1] );
     *  // Retain the original dates also... newts.setDate1Original ( oldts.getDate1Original() ); newts.setDate2Original ( oldts.getDate2Original() ); Message.printStatus (
     * 1, routine, "Using regular TS period " + newts.getDate1() + " to " + newts.getDate2 () );
     *  // Now override with new information...
     * 
     * newts.setDataInterval ( TimeInterval.HOUR, newmult ); TSIdent tsident = newts.getIdentifier(); tsident.setInterval ( TimeInterval.HOUR, newmult );
     *  // Allocate space based on the dates in the header...
     * 
     * newts.allocateDataSpace ();
     *  // Now loop through...
     * 
     * DateTime start = new DateTime ( oldts.getDate1() ); DateTime end = new DateTime ( oldts.getDate2() ); DateTime t = null, tprev = null; TSDateIterator tsdi; int
     * count = 0, i, minute, hour, day, month, year, prev_minute, prev_hour, prev_day, prev_month, prev_year; double ave, sum = 0.0, value; if ( Message.isDebugOn ) {
     * Message.printDebug ( dl, routine, "Iterator start/end are: " + start + " " + end ); } for( t = start, i = 0; t.lessThanOrEqualTo( end ); t.addInterval(
     * TimeInterval.HOUR, newmult ), i++ ){
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Processing date " + t ); }
     * 
     * value = oldts.getDataValue(t); if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Value returned from MONTHTS is " + value ); } newts.setDataValue( t,
     * value ); } // Now fill by carrying forward... try { fillCarryForward ( newts ); } catch ( TSException e ) { ; // For now just rely on the previous warning about //
     * not being able to fill. }
     * 
     * return newts; }
     */

    // 12 Jul 1998 SAM, RTi Change so that the dates are nicely
    // rounded.
    private HourTS OLDchangeToHourTS(IrregularTS oldts, int newmult, PropList proplist) {
        String routine = "TSUtil.changeToHourTS(IrregularTS)";
        HourTS newts = null;
        int dl = 20;

        if (Message.isDebugOn) {
            Message.printDebug(dl, routine, "Changing to " + newmult + "-hour time series");
        }

        // First declare a new time series..

        newts = new HourTS();

        // Copy the header and the dates. Set the dates as the rounded dates
        // to handle the old time series...

        newts.copyHeader(oldts);
        DateTime newts_date[] = getBoundingDatesForChangeInterval(oldts, newts.getDataIntervalBase(), newmult);
        newts.setDate1(newts_date[0]);
        newts.setDate2(newts_date[1]);
        newts.setDate1Original(oldts.getDate1());
        newts.setDate2Original(oldts.getDate2());
        Message.printDebug(dl, routine, "Using regular TS period " + newts.getDate1() + " to " + newts.getDate2());

        // Now override with new information...

        newts.setDataInterval(TimeInterval.HOUR, newmult);
        TSIdent tsident = newts.getIdentifier();
        tsident.setInterval(TimeInterval.HOUR, newmult);

        // Allocate space based on the dates in the header...

        newts.allocateDataSpace();

        // Now loop through the new period and fill in with averaged irregular
        // data as it is found...

        DateTime start = new DateTime(newts.getDate1());
        DateTime end = new DateTime(newts.getDate2());
        DateTime date_new = new DateTime(start);

        // Initialize the date after which we can accumulate to...

        DateTime previous_date = new DateTime(date_new);
        previous_date.addHour(-newmult);

        // Initialize the irregular data...

        Vector alldata = oldts.getData();
        if (alldata == null) {
            // No data...
            return newts;
        }
        TSData data = null;
        int count = 0, i = 0, iend = alldata.size();
        double ave, sum = 0.0, value;
        DateTime t = null;

        for (; date_new.lessThanOrEqualTo(end); date_new.addHour(newmult), previous_date.addHour(newmult)) {
            if (Message.isDebugOn) {
                Message.printDebug(dl, routine, "Processing " + newmult + "-hour date " + date_new.toString(DateTime.FORMAT_Y2K_LONG));
                Message.printDebug(dl, routine, "Previous date is " + previous_date.toString(DateTime.FORMAT_Y2K_LONG));
            }

            // Now loop through the irregular time series data and transfer
            // to the new date. If the date is past what we are looking
            // for, leave it and use in the next time step...

            sum = 0.0;
            count = 0;
            for (; i < iend; i++) {
                data = (TSData) alldata.elementAt(i);
                t = data.getDate();
                if (Message.isDebugOn) {
                    Message.printDebug(dl, routine, "Processing IRRTS date " + t.toString(DateTime.FORMAT_Y2K_LONG));
                }

                // If the irregular data date is greater than the
                // current loop date, then we accumulate and break
                // (so the hour interval updates). Also make sure to
                // consider the last observation (move that last check
                // outside the loop to increase performance)...

                // Put this first so we can check to see if we have
                // data...

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
                    // counter. First do the computations and
                    // transfer the numbers...
                    if (count > 0) {
                        ave = sum / (double) count;
                        // Now set the value in the new time
                        // series. It should be OK to use the
                        newts.setDataValue(date_new, ave);
                        if (Message.isDebugOn) {
                            Message.printDebug(dl, routine, "Averaged " + count + " values to get " + ave + " at " + date_new.toString(DateTime.FORMAT_Y2K_LONG));
                        }
                    } else {
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

    /*
     * REVISIT SAM private static HourTS changeToHourTS( MonthTS oldts, int newmult, PropList proplist ) { String routine = "TSUtil.changeToHourTS(MonthTS)"; HourTS newts =
     * null; int dl = 20;
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Changing to " + newmult + "-hour time series" ); }
     *  // First declare a new time series..
     * 
     * newts = new HourTS ();
     *  // Copy the header, but set the date to an even roundoff of the // interval...
     * 
     * newts.copyHeader ( oldts );
     * 
     * DateTime newts_date[] = getBoundingDatesForChangeInterval ( oldts, newts.getDataIntervalBase(), newmult ); newts.setDate1 ( newts_date[0] ); newts.setDate2 (
     * newts_date[1] );
     *  // Retain the original dates also... newts.setDate1Original ( oldts.getDate1Original() ); newts.setDate2Original ( oldts.getDate2Original() ); Message.printStatus (
     * 1, routine, "Using regular TS period " + newts.getDate1() + " to " + newts.getDate2 () );
     *  // Now override with new information...
     * 
     * newts.setDataInterval ( TimeInterval.HOUR, newmult ); TSIdent tsident = newts.getIdentifier(); tsident.setInterval ( TimeInterval.HOUR, newmult );
     *  // Allocate space based on the dates in the header...
     * 
     * newts.allocateDataSpace ();
     *  // Now loop through...
     * 
     * DateTime start = new DateTime ( oldts.getDate1() ); DateTime end = new DateTime ( oldts.getDate2() ); DateTime t = null, tprev = null; TSDateIterator tsdi; int
     * count = 0, i, minute, hour, day, month, year, prev_minute, prev_hour, prev_day, prev_month, prev_year; double ave, sum = 0.0, value; if ( Message.isDebugOn ) {
     * Message.printDebug ( dl, routine, "Iterator start/end are: " + start + " " + end ); } for( t = start, i = 0; t.lessThanOrEqualTo( end ); t.addInterval(
     * TimeInterval.HOUR, newmult ), i++ ){
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Processing date " + t ); }
     * 
     * value = oldts.getDataValue(t); if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Value returned from MONTHTS is " + value ); } newts.setDataValue( t,
     * value ); } // Now fill by carrying forward... try { fillCarryForward ( newts ); } catch ( TSException e ) { ; // For now just rely on the previous warning about //
     * not being able to fill. }
     * 
     * return newts; }
     */

     /*
     * REVISIT SAM private static MinuteTS changeToMinuteTS( HourTS oldts, int newmult, PropList proplist )
     *  { String routine = "TSUtil.changeToMinuteTS(HourTS)"; MinuteTS newts = null; int dl = 20;
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Changing to " + newmult + "-minute time series" ); }
     *  // First declare a new time series..
     * 
     * newts = new MinuteTS ();
     *  // Copy the header, but set the date to an even roundoff of the // interval...
     * 
     * newts.copyHeader ( oldts ); DateTime newts_date[] = getBoundingDatesForChangeInterval ( oldts, newts.getDataIntervalBase(), newmult ); newts.setDate1 (
     * newts_date[0] ); newts.setDate2 ( newts_date[1] );
     *  // Retain the original dates also... newts.setDate1Original ( oldts.getDate1Original() ); newts.setDate2Original ( oldts.getDate2Original() ); Message.printStatus (
     * 1, routine, "Using regular TS period " + newts.getDate1() + " to " + newts.getDate2 () );
     *  // Now override with new information...
     * 
     * newts.setDataInterval ( TimeInterval.MINUTE, newmult ); TSIdent tsident = newts.getIdentifier(); tsident.setInterval ( TimeInterval.MINUTE, newmult );
     *  // Allocate space based on the dates in the header...
     * 
     * newts.allocateDataSpace ();
     *  // Now loop through...
     * 
     * DateTime start = new DateTime ( oldts.getDate1() ); DateTime end = new DateTime ( oldts.getDate2() ); DateTime t = null, tprev = null; TSDateIterator tsdi; int
     * count = 0, i, minute, hour, day, month, year, prev_minute, prev_hour, prev_day, prev_month, prev_year; double ave, sum = 0.0, value; if ( Message.isDebugOn ) {
     * Message.printDebug ( dl, routine, "Iterator start/end are: " + start + " " + end ); } for( t = start, i = 0; t.lessThanOrEqualTo( end ); t.addInterval(
     * TimeInterval.MINUTE, newmult ), i++ ){
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Processing date " + t ); }
     * 
     * value = oldts.getDataValue(t); if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Value returned from HOURTS is " + value ); } newts.setDataValue( t,
     * value ); } // Now fill by carrying forward... try { fillCarryForward ( newts ); } catch ( TSException e ) { ; // For now just rely on the previous warning about //
     * not being able to fill. }
     * 
     * return newts; }
     */

    /**
     * Currently, the values are transferred to the MinuteTS simply by trying to set the data at the IrregularTS time stamps.
     */
    /*
     * REVISIT SAM private static MinuteTS changeToMinuteTS( IrregularTS oldts, int newmult, PropList proplist ) { String routine="TSUtil.changeToMinuteTS(IrregularTS)";
     * MinuteTS newts = null; int dl = 30; // debug level
     *  // First declare a new time series..
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Changing to " + newmult + "-minute time series" ); } newts = new MinuteTS ();
     *  // Copy the header, but set the date to an even roundoff of the // interval...
     * 
     * newts.copyHeader ( oldts ); DateTime newts_date[] = getBoundingDatesForChangeInterval ( oldts, newts.getDataIntervalBase(), newmult ); newts.setDate1 (
     * newts_date[0] ); newts.setDate2 ( newts_date[1] );
     *  // Retain the original dates also... newts.setDate1Original ( oldts.getDate1Original() ); newts.setDate2Original ( oldts.getDate2Original() ); Message.printStatus (
     * 1, routine, "Using regular TS period " + newts.getDate1() + " to " + newts.getDate2 () );
     *  // Now override with new information...
     * 
     * newts.setDataInterval ( TimeInterval.MINUTE, newmult ); TSIdent tsident = newts.getIdentifier(); tsident.setInterval ( TimeInterval.MINUTE, newmult );
     *  // Allocate space based on the dates in the header...
     * 
     * newts.allocateDataSpace ();
     *  // Now loop through...
     * 
     * DateTime start = new DateTime ( oldts.getDate1() ); DateTime end = new DateTime ( oldts.getDate2() ); DateTime t = null, tprev = null; TSDateIterator tsdi; int
     * count = 0, i, minute, hour, day, month, year, prev_minute, prev_hour, prev_day, prev_month, prev_year; double ave, sum = 0.0, value; if ( Message.isDebugOn ) {
     * Message.printDebug ( dl, routine, "Iterator start/end are: " + start + " " + end ); } for( t = start, i = 0; t.lessThanOrEqualTo( end ); t.addInterval(
     * TimeInterval.MINUTE, newmult ), i++ ){
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Processing date " + t ); }
     * 
     * value = oldts.getDataValue(t); if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Value returned fron IRRTS is " + value ); } newts.setDataValue( t,
     * value ); } return newts; }
     */

    /*
     * SAM REVISIT - need to implement C++ code. private static MinuteTS changeToMinuteTS( MonthTS oldts, int newmult, PropList proplist ) { String routine =
     * "TSUtil.changeToMinuteTS(MonthTS)"; MinuteTS newts = null; int dl = 20;
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Changing to " + newmult + "-minute time series" ); }
     *  // First declare a new time series..
     * 
     * newts = new MinuteTS ();
     *  // Copy the header, but set the date to an even roundoff of the // interval...
     * 
     * newts.copyHeader ( oldts ); DateTime newts_date[] = getBoundingDatesForChangeInterval ( oldts, newts.getDataIntervalBase(), newmult ); newts.setDate1 (
     * newts_date[0] ); newts.setDate2 ( newts_date[1] ); // Retain the original dates also... newts.setDate1Original ( oldts.getDate1Original() ); newts.setDate2Original (
     * oldts.getDate2Original() ); Message.printStatus ( 1, routine, "Using regular TS period " + newts.getDate1() + " to " + newts.getDate2 () );
     *  // Now override with new information...
     * 
     * newts.setDataInterval ( TimeInterval.MINUTE, newmult ); TSIdent tsident = newts.getIdentifier(); tsident.setInterval ( TimeInterval.MINUTE, newmult );
     *  // Allocate space based on the dates in the header...
     * 
     * newts.allocateDataSpace ();
     *  // Now loop through...
     * 
     * DateTime start = new DateTime ( oldts.getDate1() ); DateTime end = new DateTime ( oldts.getDate2() ); DateTime t = null, tprev = null; TSDateIterator tsdi; int
     * count = 0, i, minute, hour, day, month, year, prev_minute, prev_hour, prev_day, prev_month, prev_year; double ave, sum = 0.0, value; if ( Message.isDebugOn ) {
     * Message.printDebug ( dl, routine, "Iterator start/end are: " + start + " " + end ); } for( t = start, i = 0; t.lessThanOrEqualTo( end ); t.addInterval(
     * TimeInterval.MINUTE, newmult ), i++ ){
     * 
     * if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Processing date " + t ); }
     * 
     * value = oldts.getDataValue(t); if ( Message.isDebugOn ) { Message.printDebug ( dl, routine, "Value returned from MONTHTS is " + value ); } newts.setDataValue( t,
     * value ); } // Now fill by carrying forward... try { fillCarryForward ( newts ); } catch ( TSException e ) { ; // For now just rely on the previous warning about //
     * not being able to fill. }
     * 
     * if ( Message.isDebugOn ) { Message.printStatus ( dl, routine, "Finished changing interval for TS period " + newts.getDate1() + " to " + newts.getDate2 () ); }
     * 
     * return newts; }
     */

    /**
     * Convert a daily time series to monthly.
     * 
     * @param oldts Daily time series to convert.
     * @param newmult Interval multiplier for monthly time series.
     * @param proplist Property list from main changeInterval() method. Currently, only the UseNearestToEnd method of conversion is recognized.
     */
    public MonthTS OLDchangeToMonthTS(DayTS oldts, int newmult, PropList proplist) {
        String routine = "TSUtil.changeToMonthTS";
        String prop_value = proplist.getValue("UseNearestToEnd");
        if (prop_value == null) {
            Message.printWarning(1, routine, "Changing from DayTS to MonthTS is not yet enabled.");
            return null;
        }

        // Else get the interval count... This should be a generic procedure
        // that can be applied to other conversions also...

        int nearness = StringUtil.atoi(prop_value);

        // Now create the new time series...

        MonthTS newts = new MonthTS();

        // Copy the header, but set the date to an even roundoff of the
        // interval...

        newts.copyHeader(oldts);
        DateTime newts_date[] = getBoundingDatesForChangeInterval(oldts, newts.getDataIntervalBase(), newmult);

        // Now override with new information...

        newts.setDataInterval(TimeInterval.MONTH, newmult);
        TSIdent tsident = newts.getIdentifier();
        tsident.setInterval(TimeInterval.MONTH, newmult);

        // Set the dates after setting the interval so that the precision is
        // correct...

        Message.printStatus(1, routine, "Dates before set...: " + newts_date[0] + " to " + newts_date[1]);
        newts.setDate1(newts_date[0]);
        newts.setDate2(newts_date[1]);
        Message.printStatus(1, routine, "Dates after set...: " + newts.getDate1() + " to " + newts.getDate2());
        // Retain the original dates also...
        newts.setDate1Original(oldts.getDate1Original());
        newts.setDate2Original(oldts.getDate2Original());
        // Message.printStatus ( 1, routine,
        // "Using regular TS period " + newts.getDate1() + " to " +
        // newts.getDate2 () );

        // Allocate space based on the dates in the header...

        newts.allocateDataSpace();

        // Now loop through...

        DateTime end = new DateTime(oldts.getDate2());
        DateTime t = new DateTime(new DateTime(newts.getDate1()));
        double value = 0.0;

        // Loop through months...

        DateTime dayts_date = null;
        if (nearness > 0) {
            // Construct a date so we get the characteristics of the
            // original time series dates...
            dayts_date = new DateTime(oldts.getDate1());
        }
        for (; t.lessThanOrEqualTo(end); t.addInterval(TimeInterval.MONTH, newmult)) {
            if (nearness > 0) {
                // Change intervals by finding the nearest value in the
                // original time series... Reuse the date from the
                // original. Start by setting the year and month to
                // that of the monthly time series...
                for (int isearch = 0; isearch < nearness; isearch++) {
                    // Go backward in the current month and
                    // forward in the next month until we find
                    // a value. If a value is not found, don't
                    // set (leave missing)...
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
            newts.addToGenesis("Converted to monthly using daily TS \"" + oldts.getIdentifierString() + "\" using " + nearness + " day proximity to month end.");
        }
        return newts;
    }

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
        Vector alldata = oldts.getData();
        if (alldata == null) {
            // No data...
            return newts;
        }
        TSData data = null;
        int iend = alldata.size();
        for (int i = 0; i < iend; i++) {
            data = (TSData) alldata.elementAt(i);
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

        if (newbase == TimeInterval.IRREGULAR) {
            // Can use the original dates as is...
            newts_date[0] = new DateTime(oldts.getDate1());
            newts_date[1] = new DateTime(oldts.getDate2());
        } else if (newbase == TimeInterval.MINUTE) {
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
            // Extend by one interval
            newts_date[1].addInterval(newbase, newmult);
        } else if (newbase == TimeInterval.HOUR) {
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
                // Not sure why the +1, other than for extending to
                // end-of-interval..
                // newts_hour =(newts_date[0].getHour()/newmult+1)*newmult;
                newts_hour = (newts_date[0].getHour() / newmult) * newmult;
                if (newts_hour < 24) {
                    // Just need to reset the hour...
                    newts_date[0].setHour(newts_hour);
                } else { // Need to set the date into the next day...
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
                } else { // Need to set the date into the next day...
                    newts_date[1].addDay(1);
                    newts_date[1].setHour(0);
                }
            }
            // Extend by one interval
            newts_date[1].addInterval(newbase, newmult);
        } else if (newbase == TimeInterval.DAY) {
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
            newts_date[1].addInterval(newbase, newmult);
        } else if (newbase == TimeInterval.MONTH) {
            // Use the old dates except set everything to zero values other
            // than month and year...
            // Note that the date items less than month are not really used
            // since the timestep is monthly, but sometimes for displays the
            // day may be used to position output (e.g., set the day to 15
            // to force plotting at the center of the month. For now, set
            // to 1.
            newts_date[0] = new DateTime(DateTime.PRECISION_MONTH);
            newts_date[1] = new DateTime(DateTime.PRECISION_MONTH);
            // Transfer...
            newts_date[0].setYear(old_date1.getYear());
            newts_date[0].setMonth(old_date1.getMonth());
            newts_date[1].setYear(old_date2.getYear());
            newts_date[1].setMonth(old_date2.getMonth());
            // Extend by one interval
            newts_date[1].addInterval(newbase, newmult);
        } else if (newbase == TimeInterval.YEAR) {
            // Similar to monthly above, but also set month to 1...
            newts_date[0] = new DateTime(DateTime.PRECISION_YEAR);
            newts_date[1] = new DateTime(DateTime.PRECISION_YEAR);
            // Transfer...
            newts_date[0].setYear(old_date1.getYear());
            newts_date[1].setYear(old_date2.getYear());
            // Extend by one interval
            newts_date[1].addInterval(newbase, newmult);
        } else {
            Message.printWarning(2, routine, "Getting dates to change to interval " + newbase + " not supported.  Using original dates.");
        }
        Message.printStatus(1, routine, "Bounding dates for new time series are: " + newts_date[0] + " to " + newts_date[1]);
        return newts_date;
    }

    /**
     * Replaces a missing data value according to flag. This method is used by the new changeInterval method.
     * 
     * @param missingValueFlag - Determined the method of replacement: <table width=100% cellpadding=10 cellspacing=0 border=2>
     *            <tr>
     *            <td><b>Flag</b></td>
     *            <td><b>Replace by</b></td>
     *            <td><b>Type</b></td>
     *            </tr>
     *            <tr>
     *            <td><b>0</b></td>
     *            <td>defaultReplacementValue</td>
     *            <td>REGULAR</td>
     *            </tr>
     *            <tr>
     *            <td><b>1</b></td>
     *            <td>zero</td>
     *            <td>ALERT_INCREMENT</td>
     *            </tr>
     *            <tr>
     *            <td><b>2</b></td>
     *            <td>replacementValue<</td>
     *            <td>ALERT_REGULAR</td>
     *            </tr>
     *            </table>
     * @param replacementValue used to replace a data value when missingValueFlag = 2
     * @param defaultReplacementValue used to replace a data value when missingValueFlag is not 1 or 2
     */
    private static double replaceDataValue(int missingValueFlag, double replacementValue, double defaultReplacementValue) {
        switch (missingValueFlag) {
        case 1:
            return 0.0;
        case 2:
            return replacementValue;
        default:
            return defaultReplacementValue;
        }
    }

}