// TSUtil_ChangeIntervalIrregularToRegular - create a new regular interval time series from irregular interval time series

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

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

/**
 * Create a new regular interval time series from irregular interval time series.
 */
public class TSUtil_ChangeIntervalIrregularToRegular {
    
	/**
	Data type for the new time series.
	*/
	private String newDataType = null;

	/**
	New time series data interval.
	*/
	private TimeInterval newInterval = null;

	/**
	Statistic computed while changing interval (e.g., max value when converting INST-small to INST-large).
	*/
	private TSStatisticType statisticType = null;

	/**
	 * Flag for calculated values.
	 */
	private String flag = "";

	/**
	 * Description for flag.
	 */
	private String flagDescription = "";
	
	/**
	 * Persist interval.
	 */
	private TimeInterval persistInterval = null;

	/**
	 * Persist value.
	 */
	private Double persistValue = null;
	
	/**
	 * Flag for persisted values.
	 */
	private String persistFlag = "";

	/**
	 * Description for persistFlag.
	 */
	private String persistFlagDescription = "";

	/**
	Units for the new time series.
	*/
	private String newUnits = null;

	/**
	 * Scale value.
	 */
	private Double scaleValue = null;

	/**
	Original time series that is being used as input.
	*/
	private TS oldTS = null;

	/**
	Output year type when new interval is year.
	*/
	private YearType outputYearType = null;

	/**
	Constructor.
	@param oldTS Time series used as data to generate the new time series.
	@param statisticType the output statistic type
	@param flag flag to assign to calculated values
	@param flagDescription description for 'flag'
	@param persistInterval interval that last value will persist in, to supply additional values, null to not use.
	@param persistValue value for persistInterval if not the last value (e.g., 0.0 for precipitation), null to use default of last value.
	@param persistFlag flag to assign to persisted values
	@param persistFlagDescription description for 'persistFlag'
	@param newInterval New interval as a string that can be parsed by RTi.Util.Time.TimeInterval.parseInterval(),
	indicating the new interval for the time series.
	@param outputYearType output year type used when the new interval is year (if null use calendar).
	@param newDataType the data type for the new time series.
	If null, the data type from the old time series will be used.
	@param newUnits Units for the new time series.  If null, use the units from the original time series.
	@param scaleValue value to scale output (used to convert units), null to not scale.
	*/
	public TSUtil_ChangeIntervalIrregularToRegular ( IrregularTS oldTS,
    	TSStatisticType statisticType, String flag, String flagDescription,
    	TimeInterval persistInterval, Double persistValue, String persistFlag, String persistFlagDescription,
		TimeInterval newInterval, YearType outputYearType, String newDataType, String newUnits, Double scaleValue ) {
    	// Check controlling information
    	// OldTS - Make sure it is not null and has a not zero length period of record.
    	if ( oldTS == null ) {
        	throw new IllegalArgumentException ( "Input time series is null.  Cannot change interval." );
    	}
    	// TODO SAM 2011-02-19 Evaluate if this is OK to pass - need to allow for discovery mode creation of TS
    	//if ( !oldTS.hasData() ) {
    	//    throw new IllegalArgumentException(  "Input time series has no data.  Cannot change interval." );
    	//}
    	this.oldTS = oldTS;
    
    	if ( newInterval == null ) {
        	throw new IllegalArgumentException ( "New interval is null.  Cannot change interval." );
    	}
    	this.newInterval = newInterval;
    	
    	// Make sure that input is IrregularTS and output is regular interval.
    	if ( !newInterval.isRegularInterval() ) {
        	throw new IllegalArgumentException ( "New interval is not a regular interva.  Cannot change interval." );
    	}
    
    	// Statistic
    	if ( statisticType != null ) {
        	boolean supported = false;
        	List<TSStatisticType> statistics = TSUtil_CalculateTimeSeriesStatistic.getStatisticChoices();
        	for ( TSStatisticType statistic : statistics ) {
            	if ( statisticType == statistic ) {
                	supported = true;
                	break;
            	}
        	}
        	if ( !supported ) {
            	throw new IllegalArgumentException ( "The statistic (" + statisticType + ") is not supported by this command." );
        	}
    	}
    	this.statisticType = statisticType;
    	if ( flag == null ) {
    		flag = "";
    	}
    	this.flag = flag;
    	if ( flagDescription == null ) {
    		flagDescription = "";
    	}
    	this.flagDescription = flagDescription;
    	
    	// Persist interval and value
    	this.persistInterval = persistInterval;
    	this.persistValue = persistValue;
    	if ( persistFlag == null ) {
    		persistFlag = "";
    	}
    	this.persistFlag = persistFlag;
    	if ( persistFlagDescription == null ) {
    		persistFlagDescription = "";
    	}
    	this.persistFlagDescription = persistFlagDescription;
    
    	if ( outputYearType == null ) {
        	outputYearType = YearType.CALENDAR;
    	}
    	this.outputYearType = outputYearType;
    
    	if ( (newDataType == null) || newDataType.isEmpty()  ) {
        	// Assume the data type of the old time series
        	this.newDataType = oldTS.getDataType();
    	}
    	else {
        	this.newDataType = newDataType;
    	}
    
    	if ( (newUnits == null) || newUnits.isEmpty() ) {
        	this.newUnits = oldTS.getDataUnits();
    	}
    	else {
        	this.newUnits = newUnits;
    	}
    	this.scaleValue = scaleValue;
	}

    /**
     * Return a new time series having a different interval from the source time series.
     * This is the main that which will call subordinate routines, depending on the intervals that are involved.
     * @return A new time series of the requested data interval. All of the original time series header
     * information will be essentially the same, except for the interval and possibly the data type.
     * @param createData if true, calculate values for the data array; if false, only assign metadata,
     * for use in TSTool command discovery mode.
     * @exception Exception if an error occurs (e.g., bad new interval string).
     */
    public TS changeInterval ( boolean createData )
    throws Exception {
        String routine = getClass().getSimpleName() + "changeInterval", status, warning;
        
        // Get the local values for this method
        TS oldTS = getOldTimeSeries();
        TSStatisticType statisticType = getStatistic();
        String flag = getFlag();
        String flagDescription = getFlagDescription();
        TimeInterval persistInterval = getPersistInterval();
        TimeInterval newInterval = getNewInterval();
        int newtsBase = newInterval.getBase();
        int newtsMultiplier = newInterval.getMultiplier();
        Double persistValue = getPersistValue();
        String persistFlag = getPersistFlag();
        String persistFlagDescription = getPersistFlagDescription();
        String newDataType = getNewDataType();
        YearType outputYearType = getOutputYearType();
        String newUnits = getNewUnits(); // Will check below when creating the time series.
        Double scaleValue = getScaleValue();
        
        // Create the new time series
        // From the old time series identifier create the new time series identifier.
        TSIdent newtsIdent = new TSIdent(oldTS.getIdentifier());
        // Set with the string here so that the interval is an exact match with what
        // was requested (e.g., "1Hour" remains and does not get converted to "hour").
        // Otherwise, the time series identifiers won't match.
        newtsIdent.setInterval("" + newInterval);

        // Create the new time series using the new identifier.
        // - this does not actually set the identifier, just creates the correct time series type
        TS newTS = TSUtil.newTimeSeries(newtsIdent.getIdentifier(), true);
        if (newTS == null) {
            throw new RuntimeException("Could not create the new time series using identifier \"" +
            	newtsIdent.getIdentifier() + " - cannot change interval.");
        }
        
        // Update the new time series properties with all required information.
        // Notice: copyHeader() overwrites, among several other things,
        // the Identifier, the DataInterval (Base and Multiplier).
        // It also set the dates, from the old time series.
        // Make sure to reset these properties to the values needed by the new time series.
        newTS.copyHeader(oldTS);
        newTS.setDataType(newDataType);
        newTS.setIdentifier(newtsIdent);
        newTS.setDataInterval(newtsBase, newtsMultiplier);
        // Get the bounding dates for the new time series based on the old time series.
        DateTime outputStart = null;
        DateTime outputEnd = null;
        // TODO smalers 2020-03-05 need to review this code and maybe use 'round'
        //outputStart.round(direction, interval_base, interval_mult);
        if ( createData ) {
            DateTime newts_date[] = getBoundingDatesForChangeInterval(oldTS, newtsBase, newtsMultiplier);
            outputStart = newts_date[0];
            outputEnd = newts_date[1];
            newTS.setDate1(outputStart);
            newTS.setDate2(outputEnd);
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
        
        // Set the flag descriptions if provided
        if ( !flag.isEmpty() && !flagDescription.isEmpty() ) {
        	newTS.addDataFlagMetadata(new TSDataFlagMetadata(flag, flagDescription));
    	}
        if ( !persistFlag.isEmpty() && !persistFlagDescription.isEmpty() ) {
        	newTS.addDataFlagMetadata(new TSDataFlagMetadata(persistFlag, persistFlagDescription));
        }
        
        if ( !createData ) {
            return newTS;
        }

        // Allocate the data space for data values.
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
        /*
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
        */
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
        
        // Call the main methods
        // - currently only support converting to a larger interval.
       
        toLarger ( oldTS, newTS, oldTSi, newTSi, outputStart, outputEnd,
        	flag );

        return newTS;
    }

	// History:
    //
    // 17 Aug 1998 SAM, RTi Update so that the resulting dates have the proper precision.
    // Otherwise some date fields disrupt output.
    // 2005-06-01 Luiz Teixeira, RTi Extended date1 by one time interval in all cases.
    // Changed the precision for the TimeInterval case from PRECISION_MONTH to PRECISION_YEAR.
    /**
     * Determine the bounding dates to be used for converting a time series from one interval to another.
     * This method may be overloaded or may be made more complex in the * future in order to better
     * determine the dates depending on data type and recording method
     * (e.g. to round to the previous interval or round to the nearest ending
     * interval depending on whether the data are instantaneous, sums, averages, etc.).
     * Currently, the date bounds are always extended in both directions,
     * possibly resulting in missing data at the ends when the changes is performed.
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
    Return the flag for calculated values.
    @return the flag for calculated values.
    */
    public String getFlag() {
        return this.flag;
    }
    
    /**
    Return the description for 'flag'.
    @return the description for 'flag'.
    */
    public String getFlagDescription() {
        return this.flagDescription;
    }
    
    /**
    Return the new data type.
    @return the new data type.
    */
    private String getNewDataType () {
        return this.newDataType;
    }

    /**
    Return the new interval.
    @return the new interval.
    */
    private TimeInterval getNewInterval () {
    	return this.newInterval;
	}

    /**
    Return the new units.
    @return the new units.
    */
    private String getNewUnits () {
        return this.newUnits;
    }

    /**
    Return the old time series.
    @return the old time series.
    */
    private TS getOldTimeSeries () {
        return this.oldTS;
    }

    /**
    Return the output year type.
    @return the output year type.
    */
    private YearType getOutputYearType () {
        return this.outputYearType;
    }

    /**
    Return the flag for persisted values.
    @return the flag for persisted values.
    */
    public String getPersistFlag() {
        return this.persistFlag;
    }
    
    /**
    Return the description for 'persistFlag'.
    @return the description for 'persistFlag'.
    */
    public String getPersistFlagDescription() {
        return this.persistFlagDescription;
    }
    
    /**
    Return the persist interval.
    @return the persist interval.
    */
    private TimeInterval getPersistInterval() {
        return this.persistInterval;
    }

    /**
    Return the persist value.
    @return the persist value.
    */
    private Double getPersistValue() {
        return this.persistValue;
    }

    /**
    Return the scale value.
    @return the scale value.
    */
    private Double getScaleValue() {
        return this.scaleValue;
    }

    /**
    Return the statistic being computed.
    @return the statistic being computed.
    */
    private TSStatisticType getStatistic() {
        return this.statisticType;
    }

    /**
    Get the list of statistics that can be created during the conversion.
    @return the list of statistic as TSStatisticType.
    */
    public static List<TSStatisticType> getStatisticChoices() {
    	List<TSStatisticType> choices = new ArrayList<>();
    	choices.add ( TSStatisticType.CHANGE ); // Useful for
    	choices.add ( TSStatisticType.CHANGE_ABS );
    	choices.add ( TSStatisticType.CHANGE_PERCENT );
    	choices.add ( TSStatisticType.CHANGE_PERCENT_ABS );
    	choices.add ( TSStatisticType.MAX );
    	choices.add ( TSStatisticType.MEAN );
    	choices.add ( TSStatisticType.MEDIAN );
    	choices.add ( TSStatisticType.MIN );
    	choices.add ( TSStatisticType.TOTAL );  // For precipitation accumulation
    	return choices;
    }

    /**
    Get the list of statistics that can be created during the conversion.  For example, for INST to INST the
    computed value can be the MAX or MIN or default (new INST).
    @return the statistic display names as strings.
    */
    public static List<String> getStatisticChoicesAsStrings() {
        List<TSStatisticType> choices = getStatisticChoices();
        List<String> stringChoices = new ArrayList<String>();
        for ( TSStatisticType choice : choices ) {
            stringChoices.add ( "" + choice );
        }
        return stringChoices;
    }

    /**
     * Convert the irregular input time series to regular output time series.
     * @param oldTS old (input) time series
     * @param newTS new (output) time series
     * @param oldTSi iterator for old time series
     * @param newTSi iterator for new time series
     * @param outputStart start for output
     * @param outputEnd end for output
     * @param flag flag for calculated values
     */
    private void toLarger ( TS oldTS, TS newTS, TSIterator oldTSi, TSIterator newTSi, DateTime outputStart, DateTime outputEnd,
    	String flag ) {
    	// Sample array for input
    	// - size to a reasonably large array and resize below if necessary
    	int sampleArraySize = 1000;
    	double sampleArray [] = new double[sampleArraySize];
    	int sampleArrayCount = 0;
    	boolean doFlag = false;
    	if ( !flag.isEmpty() ) {
    		doFlag = true;
    	}

    	// Loop through the output time series intervals
    	
    	DateTime outputIntervalStart = new DateTime(newTSi.getDate());
    	DateTime outputIntervalEnd = new DateTime(outputIntervalStart);
    	int intervalBase = newTS.getDataIntervalBase();
    	int intervalMult = newTS.getDataIntervalMult();
    	outputIntervalEnd.addInterval(intervalBase, intervalMult);
    	
    	double value; // Calculated value
    	for ( ; outputIntervalEnd.lessThanOrEqualTo(outputEnd); outputIntervalEnd.addInterval(intervalBase, intervalMult) ) {
    		// Increment
    		// Set the data value
    		value = 1.0;
    		if ( doFlag ) {
    			newTS.setDataValue(outputIntervalEnd, value);
    		}
    		else {
    			newTS.setDataValue(outputIntervalEnd, value, flag, -1);
    		}
    	}
    }

}