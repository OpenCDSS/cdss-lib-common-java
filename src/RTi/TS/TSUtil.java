// TSUtil - utility functions for TS

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

//------------------------------------------------------------------------------
// TSUtil - utility functions for TS
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:	(1)	These are static data functions.  They are outside of
//			TS so that we do not end up with a huge TS class and
//			also to remove some of the circular dependency that
//			would result (TS having MonthTS instances, etc.)
//------------------------------------------------------------------------------
// History:
// 
// 14 Dec 1997	Steven A. Malers, RTi	Initial version of code.
// 13 Mar 1998	SAM, RTi		This code has been getting modified to
//					pull in C++ routines.  Continue to do
//					that and change the changeInterval code
//					to accept a PropList rather than a
//					bit mask.
// 25 Mar 1998	SAM, RTi		Add areUnitsCompatible.
// 05 Apr 1998	SAM, RTi		Add formatOutput, areIntervalsSame.
// 03 May 1998	SAM, RTi		Add generic fill that can handle several
//					fill methods at each time step.
// 20 May 1998	CEN, RTi		Add toArray.
// 26 May 1998	SAM, RTi		Add fillMonthly.
// 29 May 1998	CEN, RTi		Add fillRegressLinear.
// 01 Jun 1998	CEN, RTi		Add fillRegressLinear12.
// 14 Jun 1998	SAM, RTi		Change fillMonthly to
//					fillConstantByMonth.
// 17 Jun 1998	SAM, RTi		Change fillMonthlyIfMissing to
//					fillMonthly.
// 17 Jun 1998	CEN, RTi		Add changePeriodOfRecord.
// 21 Jun 1998	SAM, RTi		Add fillCarryForward.
// 26 Jun 1998	SAM, RTi		Realized that the change in Oct 1997
//					where we initialize TSDates to the
//					current time is not compatible with much
//					of this code.  Mixing those time series
//					with zerod time series results in loops
//					sometimes not handling the first and
//					last dates.  Change all the new TSDate
//					calls here to use the DATE_ZERO flag.
// 01 Jul 1998	SAM, RTi		Add enforceLimits.
// 23 Jul 1998	SAM, RTi		Removed changePeriodOfRecord.  CEN had
//					appropriately moved to the MonthTS
//					class (because it is associated with
//					data storage).  Add
//					createPeriodOfRecordTS.
// 20 Aug 1998	SAM, RTi		Complete toArray* methods.  Optimize
//					getDataLimits some.
// 31 Aug 1998	SAM, RTi		Change fillConstant to only fill
//					missing and add setConstant.
// 30 Sep 1998	SAM, RTi		Update fill to accept a property with
//					the monthly averages.
// 06 Nov 1998	SAM, RTi		Change TSRegressionData to TSRegression
//					and modify its use.  Add findTSFile.
// 04 Dec 1998	SAM, RTi		Update with new regression fill code.
// 02 Jan 1999	SAM, RTi		Add mean and sum to TSLimits data.
//					Add wrapper exception handling to many
//					methods (except the low-level
//					changeInterval code - too many!).
//					Fix bug where getDataLimits was getting
//					the wrong overall dates because a
//					TSDate was being reused.  Add
//					AVAILABLE_POR flag.
// 10 Mar 1999	SAM, RTi		Update so if filling using regression
//					and the independent value is zero,
//					use .001 as the observed value.
// 12 Apr 1999	SAM, RTi		Fill in DayTS change interval methods.
//					Add other features to support DayTS.
//					Update genesis calls.  Fix so that
//					if regression data are not available
//					for a month, the month is skipped.
// 10 Jun 1999	SAM, RTi		Add numIntervalsInMonth().
// 26 Jul 1999	SAM, RTi		Add fillInterpolate() and
//					changeInterval() to convert daily to
//					monthly using nearest end value.
// 09 Aug 1999	SAM, RTi		Enable linear and log regression for
//					daily time series.
// 28 Oct 1999	SAM, RTi		Add ability to ignore <= 0 values in
//					fill pattern averaging.
// 04 Dec 1999	SAM, RTi		Fix bug where in fill() the monthly
//					average fill would break out of filling
//					even if a missing data value was in the
//					historic averages.
// 25 Sep 2000	SAM, RTi		Add newTimeSeries, similar to C++.
// 11 Oct 2000	SAM, RTi		Add getTracesFromTS().
// 28 Oct 2000	SAM, RTi		Add normalize().
// 01 Nov 2000	SAM, RTi		Add findTS().
// 13 Nov 2000	SAM, RTi		Add another overload to getMonthTotals()
//					to support efficiency calculations.
// 18 Dec 2000	SAM, RTi		Change findTS() to indexOf().
// 31 Dec 2000	SAM, RTi		Update so the following change the
//					decriptions of the output time series
//					and (*) allow the calling code to
//					specify the description string:
//						createRunningAverageTS()
//						* fillConstantByMonth()
//						* fillRegress()
//						scale()
// 15 Jan 2001	SAM, RTi		Continue above with:
//						fillConstant()
//					Replace deprecated IO with IOUtil.
//					Calls to addToGenesis now do not have
//					routine.  Update add() and subtract()
//					to have missing data flag.
// 24 Jul 2001	SAM, RTi		Add parameter to indexOf() to allow
//					search in either direction.
// 25 Feb 2001	SAM, RTi		Update fillInterpolate() to have a
//					parameter indicating maximum number of
//					timesteps to interpolate and add an
//					interpolation type (but don't enable).
//					Update createRunningAverageTS() to
//					support daily data.  Add
//					fillDayTSFrom2MonthTSAnd1DayTS().
//					Add genesis and description info to
//					fillPattern().
// 15 Mar 2001	SAM, RTi		Add max(), allow indexOf() to search by
//					location.  Add sequence number overload
//					to indexOf().
// 13 Apr 2001	SAM, RTi		Overload getDataUnits() to ignore units.
// 08 May 2001	SAM, RTi		Add divide(), multiply().
// 27 Aug 2001	SAM, RTi		Add blend(), cumulate(), relativeDiff(),
//					replaceValue().
// 2001-11-07	SAM, RTi		Made a number of changes to other TS
//					classes so recompile this.
// 2002-01-25	SAM, RTi		Fix getPeriodFromTS() to allow first
//					TS to be null - search for first
//					non-null TS to initialize.
// 2002-02-26	SAM, RTi		Update regression code so names of the
//					time series variables are more easily
//					associated with filled and independent
//					data.
// 2002-03-24	SAM, RTi		Update to support MOVE1 and MOVE2
//					consistently with regression.
//					Add fillFromTS(), setFromTS().
// 2002-03-31	SAM, RTi		Update fillRegress() to reflect using 3
//					periods for MOVE2.
// 2002-04-15	SAM, RTi		Add shiftTimeByInterval().
// 2002-04-17	SAM, RTi		Add ARMA().
// 2002-04-18	SAM, RTi		Finish implemenenting disaggregate().
// 2002-04-19	SAM, RTi		Update ARMA() to consider ARMA interval.
// 2002-04-29	SAM, RTi		Update ARMA() to handle more ARMA
//					interval possibilities.
// 2002-05-01	SAM, RTi		Update ARMA() to allow ARMA interval
//					greater than the data interval.
// 2002-05-08	SAM, RTi		Update disaggregate() to handle all zero
//					input for the Ormsbee method and also
//					add a SameValue method.
// 2002-05-20	SAM, RTi		Overload toArrayByMonth() to allow more
//					than one month to be specified.  This
//					simplifies seasonal analysis.  Add
//					getTSFormatSpecifiers().  Add
//					adjustExtremes().
// 2002-06-05	SAM, RTi		Add createMonthSummary ().
// 2002-06-16	SAM, RTi		Fix when manipulating IrregularTS data -
//					need to call isDirty() directly because
//					the TSData cannot do so.  Need to
//					correct this by using TSIterator with
//					TS!  Fix bug where cumulate was not
//					setting the initial cumulative value -
//					had to call isDirty(true) for
//					IrregularTS.
// 2002-08-23	SAM, RTi		Add addConstant() method.
// 2003-03-06	SAM, RTi		Add the fillRepeat() method - similar to
//					fillCarryForward().
// 2003-05-20	SAM, RTi		Fix bug in createMonthSummary() where
//					the minimum was not getting computed
//					correctly.  The min and max were also
//					not getting initialized correctly.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TSUnits to DataUnits.
//					* Change TS.INTERVAL* to TimeInterval.
//					* Handle exceptions thrown by parsing
//					  code.
//					* Change TSInterval to TimeInterval.
// 2003-08-21	SAM, RTi		* Change TS.isDirty(boolean) calls to
//					  setDirty(boolean).
// 2003-10-06	SAM, RTi		* Add TRANSFER_BYDATE and
//					  TRANSFER_SEQUENTIALLY to indicate how
//					  time series should be transferred when
//					  iterating.
//					* Update setFromTS() to take a
//					  PropList() to indicate how the
//					  transfer should occur.
// 2003-11-17	SAM, RTi		* Add average() method.
// 2003-11-20	SAM, RTi		* Fix bug in changeToMonthTS(DayTS)
//					  where the search of daily data was not
//					  working correctly.
// 2003-12-22	SAM, RTi		* Fix bug in add() where the debug
//					  messages where printing the result of
//					  the add twice instead of the value to
//					  be added and the initial value.
// 2004-01-26	SAM, RTi		* Add Javadoc to formatOutput() to
//					  describe OutputStart and OutputEnd.
// 2004-02-22	SAM, RTi		* Add fillProrate(), similar to
//					  fillRepeat().
// 2004-08-12	SAM, RTi		* Fix bug in setConstantByMonth() where
//					  the genesis was using a null date.
// 2004-08-22	SAM, RTi		* Change fillPattern() status messages
//					  from level 1 to 2.
// 2004-09-07	SAM, RTi		* Fix bug in createMonthSummary() - it
//					  was not handling minute data.
// 2004-09-13	SAM, RTi		* Change convertUnits() to do nothing if
//					  the requested units are null or blank.
//					  An exception is no longer thrown for
//					  this case.
// 2004-11-08	SAM, RTi		* Add shiftTimeZone().
// 2005-01-25	SAM, RTi		* Add sort() to sort a time series list
//					  by ID.
// 2005-02-16	SAM, RTi		* Overload changeInterval() to adhere
//		LT, RTi			  to the new calling convention utilized
//					  by TSTool, and using a port of the
//					  C++ code.  SAM initialize the code and
//					  LT fill it out.
// 2005-02-23 	LT, RTi			* Redesigned the changeInterval() method
// to					  and all its helper method to better 
// 2005-03-04				  handle the current supported 
//					  conversions and to allow for future
//					  addition. Fixed a series of bugs found
//					  in the C++ code which was not dealing 
//					  with conversion beyond hour. Renamed 
//					  all the helper methods to better
//					  represent their functions. Fully docu-
//					  mented and cleanup the code.
// 2005-04-22	SAM, RTi		* Fix the getDataLimits(Vector,...)
//					  method.  Previously it always returned
//					  the full limits, even if dates were
//					  passed in.   The dates are now
//					  considered.
// 2005-04-26	J. Thomas Sapienza, RTi	Corrected a bug in getDataLimits() that
//					was causing it to use the entire period
//					for irregular time series.
// 2005-04-27	SAM, RTi		* Handle missing indicator better in
//					  fillPattern() for months outside
//					  the data period.
// 2005-05-12	SAM, RTi		* Update fillRegress to include the
//					  FillFlag parameter.
//					* Change fillRegress, fillRegressTotal,
//					  fillRegressMonthly to also throw
//					  Exception.
// 2005-05-17	SAM, RTi		* Overload fillConstantByMonth() to take
//					  a PropList and support FillFlag.
// 2005-05-18	SAM, RTi		* Overload fillConstant() to take
//					  a PropList and support FillFlag.
// 2005-05-20	SAM, RTi		* For fillRegression(), make the default
//					  description suffix use the alias if
//					  available.
// 2005-06-02	Luiz Teixeira, RTi	* Fixed bug in changeInterval_fromINST
//					  method by assing the old time series
//					  value to the new one only if the date 
//					  coincides with the date of the new 
//					  time series data point.
//					  See code commant
//					  "Assign value only if dates are equal"
// 2005-06-15	SAM, RTi		* Add FillFlag property to fillPattern()
//					  and SetFlag to enforceLimits().
//					* Show the number of values filled in
//					  the genesis information for constant,
//					  pattern, and average.
// 2005-07-17	SAM, RTi		* Update fillProrate() to take a
//					  PropList and call the PropList version
//					  from other methods.
//					* Update fillProrate() to support an
//					  analysis period and average for the
//					  factor.
// 2005-07-28	SAM, RTi		* Add findNearestDataValue().
//					* Add interpolateDataValue().
// 2005-09-12	SAM, RTi		* Fix bug where filling with pattern was
//					  not using the first character of the
//					  indicator when the flag was Auto, for
//					  regular time series.
// 2005-09-28	SAM, RTi		* Overload cumulate() to take a
//					  PropList and deprecate the old
//					  method.
// 2005-10-28	SAM, RTi		* Minor change to remove redundant
//					  catch/throw in getPeriodFromTS().
// 2005-12-14	SAM, RTi		* newTimeSeries() was not throwing an
//					  Exception for a bad interval - now it
//					  does.
//					* Change warning messages for add,
//					  consistent with new command standards.
// 2006-01-08	SAM, RTi		* Clarify convertUnits() to only show
//					  add factor if not zero.
// 2006-01-02	SAM, RTi		* Notes for scaling by
//					  DaysInMonthInverse were not getting
//					  handled, leading to a misleading
//					  number being shown in output notes.
// 2006-05-15	SAM, RTi		* DataUnits.getConversion() no longer
//					  throws TSException so some errors were
//					  not getting caught.  Change to catch
//					  Exception in a couple of places.
// 2007-01-30	KAT, RTi		Updated the fill() method to allow
//							the use of the FillFlag when using 
//							fillConstant.
// 2007-03-01	SAM, RTi		Fix bug in cumulate() where optional argument
//							was not being handled, resulting in null pointer.
//							Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
//EndHeader

package	RTi.TS;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.DataUnitsConversion;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Math.DataTransformationType;
import RTi.Util.Math.MathUtil;
import RTi.Util.Math.NumberOfEquationsType;
import RTi.Util.Math.RegressionType;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.InvalidTimeIntervalException;
import RTi.Util.Time.TZ;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.String;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

// TODO SAM 2016-01-31 Need to split out calculation methods into helper classes.
// Should only retain methods here that extend TS basic functionality.
/**
This class contains static utility functions that operate on time series.
Some of these routines are candidates for inclusion in the TS class, but are
included here first because changes in the TS C++ class require extensive
recompiles which slow development.  Putting the code here is almost as
efficient.  Most of these classes accept a TS or Vector of TS as an argument.
@see TS
@see DateTime
@see TSLimits
*/
public abstract class TSUtil
{

/**
Private flags used with TSUtil.fill.  Treat as bitmask for now, just in case
we ever allow a mask, but rely on properties as much as possible.
*/
/*
private static final int FILL_METHOD_CONSTANT		= 0x1;
private static final int FILL_METHOD_HIST_DAY_AVE	= 0x2;
private static final int FILL_METHOD_HIST_MONTH_AVE	= 0x4;
private static final int FILL_METHOD_HIST_SEASON_AVE	= 0x8;
private static final int FILL_METHOD_HIST_YEAR_AVE	= 0x10;
private static final int FILL_METHOD_INIT_ZERO		= 0x20;
private static final int FILL_METHOD_LOOKUP_TABLE	= 0x40;
private static final int FILL_METHOD_REGRESS_LINEAR	= 0x80;
private static final int FILL_METHOD_REGRESS_LINEAR_12	= 0x100;
private static final int FILL_METHOD_REGRESS_LOG	= 0x200;
private static final int FILL_METHOD_REGRESS_LOG_12	= 0x400;
private static final int FILL_METHOD_CARRY_FORWARD	= 0x800;
*/

/**
Used with getPeriodFromTS, and getPeriodFromLimits and others.  Find the maximum period.
*/
public final static int MAX_POR = 0;
/**
Used with getPeriodFromTS and others.  Find the minimum (overlapping) period.
*/
public final static int MIN_POR = 1;
/**
Use the available period.  For example, when adding time series, does the
resulting time series have the maximum (combined period),
minimum (overlapping period), or original period (of the first time series).
*/
public final static int AVAILABLE_POR = 2;
						
/**
Ignore missing data when adding, subtracting, etc.
*/
public final static int IGNORE_MISSING = 1;
/**
Set result to missing of other data being analyzed are missing.
*/
public final static int SET_MISSING_IF_OTHER_MISSING = 2;
/**
Set result to missing of any data being analyzed are missing.
*/
public final static int SET_MISSING_IF_ANY_MISSING = 3;

/**
When used as a property, indicates that a time series transfer or analysis
should occur strictly by date/time.  In other words, if using
TRANSFER_BYDATETIME, Mar 1 will always line up with Mar 1, regardless of whether
leap years are encountered.  However, if TRANSFER_SEQUENTIALLY is used, then
data from Feb 29 of a leap year may be transferred to Mar 1 on the non-leapyear time series.
*/
// The following has been replaced with TransferDataHowType enumeration.
//public static String TRANSFER_BYDATETIME = "ByDateTime";
//public static String TRANSFER_SEQUENTIALLY = "Sequentially";

// ----------------------------------------------------------------------------
// TSUtil.add - add time series together
// ----------------------------------------------------------------------------
// Notes:	(1)	It is assumed that the time series to add to has been
//			initialized and memory for data HAS been allocated
//			(this is a change from the original C version).
//		(2)	The interval for all time series being added must agree.
//			Units will be converted as necessary.
// ----------------------------------------------------------------------------
// History:
//
// 21 Dec 1995	Steven A. Malers, RTi	Original routine.
// 17 Jan 1995	SAM, RTi		Update call to TSGetDataPosition.
// 26 Jan 1995	SAM, RTi		Update call to TSGetPeriodFromTS to
//					use TSPOR_MAX flag.
// 30 Apr 1996	CEN, RTi		Initialize t, t1, t2 time data structs
// 03 Sep 1996	SAM, RTi		Fix so that description temporary
//					string is very large to handle many
//					additions.  The description in the
//					time series data structure is dynamic
//					already.
// 05 Mar 1998	SAM, RTi		Port to Java and clean up to take
//					advantage of OO structure.
// ----------------------------------------------------------------------------
// Variable	I/O	Description
//
// add		L	Factor to add during units conversion.
// am1, am2	L	Absolute months for beginning/ending period of record.
// aux		L	Auxiliary factor used during units conversion.
// flag		I	Indicates whether resulting time series should be
//			extendted to longest period in all time series
//			(TSPOR_AVAILABLE) or just use "tsadd" POR
//			(TSPOR_REQUESTED).
// hposadd	L	Column position for time series data that are being
//			added to.
// hposlist	L	Column position for time series data that are being
//			added.
// i		L	Loop counter for time series being added.
// inpor	L	Indicates whether requested interval is in the period
//			of record for the time series.
// message	L	Global message string.
// month1,year1	I	Starting month for add.
// month2,year2	I	Ending month for add.
// mposadd	L	Month position in "tsadd" data array.
// mposlist	L	Month position in "tslist" data array.
// mult		L	Factor to multiply by during units conversion.
// nmissing	L	Number of missing values from a time series to be added.
// ntslist	I	Number of time series in "tslist".
// routine	L	Name of this routine.
// status	L	Error status.
// t		L	Time data for loop.
// t1		L	Time data for loop start.
// t2		L	Time data for loop end.
// tsadd	I/O	Time series to be modified (added to).
// tslist	I	List of time series to add.
// tspt		L	Pointer to a time series.
// year1,year2	L	Beginning/ending years of interest.
// ----------------------------------------------------------------------------

/**
Add one time series to another.  The receiving time series description and
genesis information are updated to reflect the addition.
The IGNORE_MISSING flag is used for missing data.
@return The sum of the time series.
@param ts Time series to be added to.
@param ts_to_add Time series to add to "ts".
@exception TSException if there is an error adding the time series.
*/
public static <T extends TS> T add ( T ts, T ts_to_add )
throws TSException, Exception
{	String message, routine = "TSUtil.add";

	if ( ts == null ) {
		// Nothing to do...
		message = "Null time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	if ( ts_to_add == null ) {
		// Nothing to do...
		message = "Null time series to add";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	// Else, set up a vector and call the overload routine...
	List<T> v = new ArrayList<T>(1);
	v.add ( ts_to_add );
	double [] factor = new double[1];
	factor[0] = 1.0;
	try {
		return add ( ts, v, factor, IGNORE_MISSING );
	}
	catch ( TSException e ) {
		// Just rethrow...
		throw e;
	}
}

/**
Add a list of time series to another.  The receiving time series description and
genesis information are updated to reflect the addition.
The IGNORE_MISSING flag is used for missing data.
@return The sum of the time series.
@param ts Time series to be added to.
@param ts_to_add List of time series to add to "ts".
@exception TSException if an error occurs adding the time series.
*/
public static <T extends TS> T add ( T ts, List<T> ts_to_add )
throws Exception
{	return add ( ts, ts_to_add, IGNORE_MISSING );
}

/**
Add a list of time series to another.  The receiving time series description and
genesis information are updated to reflect the addition.
@return The sum of the time series.
@param ts Time series to be added to.
@param tsToAddList List of time series to add to "ts".
@param missing_flag See overloaded version for description.
@exception TSException if an error occurs adding the time series.
*/
public static <T extends TS> T add ( T ts, List<T> tsToAddList, int missing_flag )
throws Exception {
	return add ( ts, tsToAddList, missing_flag, null, null );
}

/**
Add a list of time series to another.  The receiving time series description and
genesis information are updated to reflect the addition.
@return The sum of the time series.
@param ts Time series to be added to.
@param tsToAddList List of time series to add to "ts".
@param missingFlag See overloaded version for description.
@param addStart starting date/time to add, or null to process full period
@param addEnd startin date/time to add, or null to process full period
@exception TSException if an error occurs adding the time series.
*/
public static <T extends TS> T add ( T ts, List<T> tsToAddList, int missingFlag, DateTime addStart, DateTime addEnd )
throws Exception
{	String message, routine = "TSUtil.add";

	// Call the main overload routine...
	if ( tsToAddList == null ) {
		message = "Null time series to add.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	int size = tsToAddList.size();
	double [] factor = new double[size];
	for ( int i = 0; i < size; i++ ) {
		factor[i] = 1.0;
	}
	try {
		return add ( ts, tsToAddList, factor, missingFlag, addStart, addEnd );
	}
	catch ( TSException e ) {
		// Just rethrow...
		throw e;
	}
}

/**
Add a list of time series to another.  The receiving time series description and
genesis information are updated to reflect the addition.
@return The sum of the time series.
@param ts Time series to be added to.
@param tsToAddList List of time series to add to "ts".
@param factor Used by subtract() or directly.  Specifies the factors to multiply
each time series by before adding.  The factors are applied after units conversion.
@param missingFlag Handle missing data as follows:
<pre>
IGNORE_MISSING               Missing data are ignored and have no effect on
                             results.   This may introduce inconsistencies if
                             sums involve different numbers of time series.
SET_MISSING_IF_OTHER_MISSING If any time series in "ts_to_add" has a missing
                             data set the value in "ts" to missing.  
SET_MISSING_IF_ANY_MISSING   If any time series in "ts_to_add" or "ts" has
                             missing data set the value in "ts" to missing.
</pre>
@exception RTi.TS.TSException if there is an error adding the time series.
*/
public static <T extends TS> T add ( T ts, List<T> tsToAddList, double factor[], int missingFlag )
throws Exception {
	return add ( ts, tsToAddList, factor, missingFlag, null, null );
}

/**
Add a list of time series to another.  The receiving time series description and
genesis information are updated to reflect the addition.
@return The sum of the time series.
@param ts Time series to be added to.
@param tsToAddList List of time series to add to "ts".
@param factor Used by subtract() or directly.  Specifies the factors to multiply
each time series by before adding.  The factors are applied after units conversion.
@param missingFlag Handle missing data as follows:
<pre>
IGNORE_MISSING               Missing data are ignored and have no effect on
                             results.   This may introduce inconsistencies if
                             sums involve different numbers of time series.
SET_MISSING_IF_OTHER_MISSING If any time series in "ts_to_add" has a missing
                             data set the value in "ts" to missing.  
SET_MISSING_IF_ANY_MISSING   If any time series in "ts_to_add" or "ts" has
                             missing data set the value in "ts" to missing.
</pre>
@param addStart starting date/time to add, or null to process full period
@param addEnd startin date/time to add, or null to process full period
@exception RTi.TS.TSException if there is an error adding the time series.
*/
public static <T extends TS> T add ( T ts, List<T> tsToAddList, double factor[], int missingFlag, DateTime addStart, DateTime addEnd )
throws TSException, Exception
{	String message, routine = "TSUtil.add";
	// Use for troubleshooting.
	// - should only be used in development and not production
	// - this results in more status messages
	boolean debug = false;
	int	dl = 20, nmissing = 0;
	double add = 0.0, mult = 1.0;
	T tspt = null;

	boolean missingIndicators[] = null;

	// Make sure that the pointers are OK...

	if ( tsToAddList == null ) {
		message = "NULL time series pointer for TS list";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	if ( ts == null ) {
		message = "NULL time series pointer for TS to receive sum";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Make sure that the intervals match.  Currently this is a requirement
	// to make sure that the results are not misrepresented.  At some point
	// may want to overload to allow a period to be added.

	try {
	if ( !intervalsMatch(tsToAddList, ts.getDataIntervalBase(), ts.getDataIntervalMult()) ) {
		message = "All time series in the list are not of interval " +
		TimeInterval.getName(ts.getDataIntervalBase(),1) + "," + ts.getDataIntervalMult();
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// If we want the period of record to be all-inclusive, resize the period of record...

/* not enabled in Java - we just add to the period from the original time
    series...
	if ( flag == TSPOR_AVAILABLE ) {
		// Get the POR from the list of time series...
		TSGetPeriodFromTS ( ntslist, tslist, &month1, &year1, &month2,
		&year2, TSPOR_MAX );
		am1 = HMAbsoluteMonth ( month1, year1 );
		am2 = HMAbsoluteMonth ( month2, year2 );
		// Now check the original time series...
		am1 = HMMIN ( am1, tsadd->am1 );
		am2 = HMMAX ( am2, tsadd->am2 );
		// Now actually change the period of record...
		HMMonthFromAbsolute ( am1, &month1, &year1 );
		HMMonthFromAbsolute ( am2, &month2, &year2 );
		if (	TSChangePOR(tsadd, month1, year1, month2, year2) ) {
			sprintf ( message,
			"Unable to change POR from %d/%d-%d/%d to %d/%d-%d/%d",
			tsadd->m1, tsadd->y1, tsadd->m2, tsadd->y2,
			month1, year1, month2, year2 );
			HMPrintWarning ( 2, routine, message );
			return HMSTATUS_FAILURE;
		}
	}
*/

	// Now loop through the time series list and add to the primary time series...

	// Set starting and ending time for time loop based on period of "tsadd"...

	int ntslist = tsToAddList.size();
	String req_units = ts.getDataUnits ();
	DataUnitsConversion conversion = null;
	int intervalBase = ts.getDataIntervalBase();
	int intervalMult = ts.getDataIntervalMult();
	int tsptIntervalBase;
	double dataValue, dataValueToAdd, tsMissing = ts.getMissing();
	boolean setToMissing = false;

	// If missing_flag indicates missing data should result in missing data
	// in the result, then a temporary array is needed to track missing...

	if ( (missingFlag == SET_MISSING_IF_OTHER_MISSING) || (missingFlag == SET_MISSING_IF_ANY_MISSING) ) {
		setToMissing = true;
		int ndata = ts.getDataSize();
		missingIndicators = new boolean[ndata];
		for ( int j = 0; j < ndata; j++ ) {
			missingIndicators[j] = false;
		}
	}
	int timestepIndex = 0;	 // Used with set_to_missing
	for ( int i = 0; i < ntslist; i++ ) {
		nmissing = 0;
		tspt = tsToAddList.get(i);
		if ( tspt == null ) {
			message = "Trouble getting [" + i + "]-th time series in list";
			Message.printWarning ( 3, routine, message );
			throw new TSException ( message );
		}
		if ( debug ) {
			Message.printStatus(2,routine,"Adding time series " + tspt.getIdentifierString() + " to output time series.");
		}
		// Get the units conversions to convert to the final TS...
		try {
		    conversion = DataUnits.getConversion( tspt.getDataUnits(), req_units );
			mult = conversion.getMultFactor();
			add = conversion.getAddFactor();
		}
		catch ( Exception e ) {
			// Can't get conversion.  This may not be a fatal
			// error, but we don't want to allow different units to be summed so return...
			message = "Cannot get conversion from \"" + tspt.getIdentifier().toString() + "\" data units \"" +
				tspt.getDataUnits() + "\" to \"" + ts.getIdentifier().toString() + "\" data units \"" +
				req_units + "\"";
			Message.printWarning ( 3, routine, message );
			throw new TSException ( message );
		}
		// Work on the one time series...

		tsptIntervalBase = tspt.getDataIntervalBase();

		if ( tsptIntervalBase == TimeInterval.IRREGULAR ) {
			// For this data type, if we can find a matching date,
			// add to that date.  Otherwise, add a new data point
			// for the date...  For now, don't support...
			Message.printWarning ( 3, routine, "IrregularTS not supported.  Not adding." + tspt.getIdentifier().toString() );
			continue;
/* Irregular not supported...
			IrregularTS irrts = (IrregularTS)ts;
			Vector alltsdata = irrts.getData();
			if ( alltsdata == null ) {
				// No data for the time series...
				return;
			}
			int nalltsdata = alltsdata.size();
			TSData tsdata = null;
			for ( int i = 0; i < nalltsdata; i++ ) {
				tsdata = (TSData)alltsdata.elementAt(i);
				data_value = tsdata.getData();
				if ( !ts.isDataMissing(data_value) ) {
					// Not missing, so do the conversion...
					data_value = (add + data_value*mult)*
						factor[i];
				}
				tsdata.setData(data_value);
			}
*/
		}
		else {
		    // Regular interval.  Loop using addInterval...
			DateTime startDate = new DateTime ( ts.getDate1() );
			if ( addStart != null ) {
				startDate.setDate(addStart); // Will ensure precision is correct
			}
			DateTime endDate = new DateTime ( ts.getDate2() );
			if ( addEnd != null ) {
				endDate.setDate(addEnd); // Will ensure precision is correct
			}
			DateTime date = new DateTime ( startDate );
			
			if ( debug ) {
				Message.printStatus(2,routine,"Time series " + tspt.getIdentifierString() + " has period " + tspt.getDate1() + " to " + tspt.getDate2() );
			}
			for ( timestepIndex = 0;
				date.lessThanOrEqualTo( endDate);
				date.addInterval(intervalBase, intervalMult),
				++timestepIndex ) {
				// If a previous time series had missing data at this time step and setToMissing is true
				// then the value has already been set to missing and there is no reason to do anything else.
				// - missingIndicators[timestepIndex] will be true if any previously processed time series were missing for date/time.
				if ( setToMissing && missingIndicators[timestepIndex] ) {
					// Increment this because treating as missing.
					++nmissing;
				    if ( debug ) {
				    	// Use status
						Message.printStatus ( 2, routine, "At " + date + ", setToMising=true and missingIndicators=true.  Not processing data." );
					}
					continue;
				}
				// If here, the previous time series in the loop did NOT have missing data at this
				// timestep (but the time series being added to might)...
				// Add the data, converting units if necessary...
				dataValueToAdd = tspt.getDataValue ( date );
				if ( tspt.isDataMissing ( dataValueToAdd ) ){
					// The value to add is missing so don't do it.  If we are tracking missing,
					// also set in the array.  This will prevent other time series from processing.
					++nmissing;
					if ( setToMissing ) {
						missingIndicators[timestepIndex] = true;
						ts.setDataValue ( date, tsMissing );
						if ( debug ) {
							// Use status
							Message.printStatus ( 2, routine, "At " + date + ", part time series " +
								tspt.getIdentifierString() + " is missing, setting output to missing." );
						}
					}
					else {
						if ( debug ) {
							// Use status
							Message.printStatus ( 2, routine, "At " + date + ", part time series " +
								tspt.getIdentifierString() + " is missing, ignoring part value." );
						}
					}
					continue;
				}
				// If here, there is a non-missing data value to add so do it.
				dataValue = ts.getDataValue ( date );
				if ( ts.isDataMissing( dataValue ) ) {
					// Original data is missing so set the value and multiply by the factor...
					if ( missingFlag == SET_MISSING_IF_ANY_MISSING ) {
						// No need to add since value will be set to missing late.
						missingIndicators[timestepIndex] = true;
						++nmissing;
						if ( debug ) {
							// Use status
							Message.printStatus ( 2, routine, "At " + date + ", output time series is missing and SET_MISSING_IF_ANY_MISSING ." );
						}
						continue;
					}
					else {
					    if ( Message.isDebugOn ) {
							Message.printDebug ( dl, routine, "At " + date + ", setting " + tspt.getIdentifierString() + " " +
							(dataValueToAdd*mult + add)*factor[i] + " over " + dataValue );
						}
						ts.setDataValue ( date, (dataValueToAdd*mult + add)*factor[i] );
					    if ( debug ) {
					    	// Use status
							Message.printStatus ( 1, routine, "At " + date + ", set " + 
							(dataValueToAdd*mult + add)*factor[i] + " over " + dataValue + " result is " + ts.getDataValue(date) );
						}
					}
				}
				else {
				    // Original time series value is non-missing.  Add the current value...
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "At " + date + ", adding " +
						(dataValueToAdd*mult + add)*factor[i] + " to " + dataValue );
					}
					ts.setDataValue ( date, dataValue + (dataValueToAdd*mult + add)*factor[i] );
					if ( debug ) {
						Message.printStatus ( 1, routine, "At " + date + ", added " + tspt.getIdentifierString() + " " +
						(dataValueToAdd*mult + add)*factor[i] + " to " + dataValue + " result is " + ts.getDataValue(date) );
					}
				}
			} // End of time loop
			if ( factor[i] >= 0.0 ) {
				if ( factor[i] == 1.0 ) {
					ts.setDescription ( ts.getDescription() + " + " + tspt.getDescription () );
					ts.addToGenesis ( "Added \"" + tspt.getIdentifierString() +
					    "\" to this time series (#missing=" + nmissing + ")." );
				}
				else {
				    ts.setDescription ( ts.getDescription() + " + " + StringUtil.formatString(
				         factor[i],"%.3f") + "*" + tspt.getDescription () );
					ts.addToGenesis ( "Added \"" + StringUtil.formatString( factor[i],"%.3f") + "*" +
					    tspt.getIdentifierString() + "\" to this time series (#missing=" + nmissing + ")." );
				}
			}
			else {
			    if ( factor[i] != -1.0 ) {
					    ts.setDescription ( ts.getDescription() + " minus " + StringUtil.formatString(
					        -factor[i],"%.3f") + "*" + tspt.getDescription () );
					ts.addToGenesis ( "Subtracted \"" + StringUtil.formatString(factor[i], "%.3f") + "*" +
					    tspt.getIdentifierString() + "\" from this time series (#missing=" + nmissing + ")." );
				}
				else {
				    ts.setDescription ( ts.getDescription() + " minus " + tspt.getDescription () );
					ts.addToGenesis ( "Subtracted \"" + tspt.getIdentifierString() +
					"\" from this time series (#missing=" + nmissing + ")." );
				}
			}
		}
	}
	return ts;
	}
	catch ( Exception e ) {
		missingIndicators = null;
		message = "Error adding time series.";
		Message.printWarning ( 3, routine, message );
		Message.printWarning ( 3, routine, e );
		// Log and rethrow the original exception so detail is not lost
		throw e;
		//throw new TSException ( message );
	}
}

/**
Add a constant to the time series.  The overloaded version is called with missing_flag=SET_MISSING_IF_ANY_MISSING.
@param ts Time series to add to.
@param start_date Date to start adding.
@param end_date Date to stop adding.
@param add_value Data value to add to the time series.
*/
public static void addConstant(	TS ts, DateTime start_date, DateTime end_date, double add_value )
{	String  routine = "TSUtil.addConstant";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Use the main utility routine...

	addConstant ( ts, start_date, end_date, -1, add_value, SET_MISSING_IF_ANY_MISSING );
}

/**
Add a constant value to the time series.  The overloaded version is called with
missing_flag=SET_MISSING_IF_ANY_MISSING.
@param ts Time series to add to.
@param start_date Date to start adding.
@param end_date Date to stop adding.
@param month Month to add.  If negative, add for every month.
@param add_value Data value to add to time series.
*/
public static void addConstant(	TS ts, DateTime start_date, DateTime end_date, int month, double add_value )
{
	addConstant ( ts, start_date, end_date, month, add_value, SET_MISSING_IF_ANY_MISSING );
}

/**
Add a constant value to the time series.
@param ts Time series to add to.
@param start_date Date to start adding.
@param end_date Date to stop adding.
@param month Month to add.  If negative, add for every month (no check for month).
@param add_value Data value to add to time series.
@param missing_flag Handle missing data as follows:
	<pre>
	IGNORE_MISSING               Missing data are ignored and have no effect on
	                             results - the constant value is always added.
	SET_MISSING_IF_OTHER_MISSING If the time series has a missing value, leave it
	                             missing.
	SET_MISSING_IF_ANY_MISSING   If the constant value or the time series is missing,
	                             leave it missing (essentially the same as the previous
	                             case).
	</pre>
*/
public static void addConstant(	TS ts, DateTime start_date,
				DateTime end_date, int month, double add_value, int missing_flag )
{	double	oldvalue;

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();
	
	if ( ts.isDataMissing(add_value) ) {
		// No need to do anything.
		return;
	}

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = (TSData)alltsdata.get(i);
			date = tsdata.getDate();
			if ( (month >= 0) && (date.getMonth() != month) ) {
				continue;
			}
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so
				// quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				oldvalue = tsdata.getDataValue();
				if ( !ts.isDataMissing(oldvalue) ) {
						tsdata.setDataValue(oldvalue + add_value);
						// Have to do this manually since TSData are being modified directly to improve performance...
						ts.setDirty ( true );
				}
				else if ( missing_flag == IGNORE_MISSING ) {
					tsdata.setDataValue ( add_value );
					// Have to do this manually since TSData are being modified directly to improve performance...
					ts.setDirty ( true );
				}
			}
		}
	}
	else {
	    // Loop using addInterval...
		// Set the precision of the date to that of the time series to prevent issues.
		start.setPrecision( interval_base );
		end.setPrecision ( interval_base );
		DateTime date = new DateTime ( start );
		
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			if ( (month >= 0) && (date.getMonth() != month) ) {
				continue;
			}
			oldvalue = ts.getDataValue(date);
			if ( !ts.isDataMissing(oldvalue) ) {
				ts.setDataValue(date, oldvalue + add_value );
			}
			else if ( missing_flag == IGNORE_MISSING ) {
				ts.setDataValue ( date, add_value );
			}
		}
	}

	// Fill in the genesis information...

	if ( month >= 0 ) { 
		ts.addToGenesis ( "Added " + StringUtil.formatString(add_value,"%.6f") +
			" " + start + " to " + end + " (month " + month + ")." );
	}
	else {
	    ts.addToGenesis ( "Added " + StringUtil.formatString(add_value,"%.6f") +
			" " + start + " to " + end + "." );
	}

	ts.setDescription ( ts.getDescription() + ", add " + StringUtil.formatString(add_value,"%.3f") );
}

/**
Adjust extreme values in a time series by maintaining "mass" and adjusting nearby values.
@param ts time series to adjust.
@param adjustMethod If "Average", then values on each side of the extreme are
added until the average does not exceed the extreme value.  Then each
non-missing value is set to the average.  If "WeightedAverage" (currently not implemented), a similar
approach is used except that the adjustment is distributed according to the
orginal data values, with values that exceed the extreme being set to the extreme.
@param extremeToAdjust AdjustMinimum or AdjustMaximum, indicating whether low or
high extremes are being adjusted.
@param extremeValue The extreme value that when exceeded will be adjusted.  For
example, specify zero and extremeToAdjust="AdjustMinimum" to adjust negative values.
@param maxIntervals The maximum number of intervals on each side of the extreme
that can be used to computed an adjusted value.  If the value is exceeded
without reaching a satisfactory adjustment, do not adjust.  Specify zero to allow any number of intervals.
@param analysisStart Start of the analysis period or null for the full period.
@param analysisEnd End of the analysis period or null for the full period.
@param setFlag flag to set for modified values (if null or empty don't set).
@param setFlagDescription description for setFlag.
@exception Exception if there is an input error.
*/
public static void adjustExtremes (	TS ts, String adjustMethod,	String extremeToAdjust,	double extremeValue,
    int maxIntervals, DateTime analysisStart, DateTime analysisEnd, String setFlag, String setFlagDescription )
throws Exception
{	String routine = "TSUtil.adjustExtremes";
	double oldvalue;

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, analysisStart, analysisEnd );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	// Booleans to eliminate string compares in the loop below...
	boolean adjust_low = true; // Whether adjusting the low values
	if ( extremeToAdjust.equalsIgnoreCase("AdjustMaximum") ) {
		adjust_low = false; // Now adjusting the high values
	}
	boolean adjust_average = true;
	if ( adjustMethod.equalsIgnoreCase("WeightedAverage") ) {
		adjust_average = false;
	}
	
	boolean doSetFlag = false;
	if ( (setFlag != null) && !setFlag.isEmpty() ) {
		doSetFlag = true;
	}

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		throw new Exception ( routine + " cannot process irregular time series." );
	}

	// Loop using addInterval...
	DateTime date = new DateTime ( start );
	DateTime left_date, right_date;
	double total = 0.0;	// Total of extreme and surrounding points, relative to the extreme value.
	double average = 0.0; // Average of extreme and surrounding points.
	int count = 0; // Number of points considered in the average.
	int nintervals = 0; // Number of intervals on each side.
	double left_value; // Values at left and right points
	double right_value;
	boolean do_adjust; // Indicates whether adjustment should be made.
	double total_values = 0.0; // Total of values being adjusted.
	int adjustCount = 0; // Number of values adjusted overall
		
	for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
		oldvalue = ts.getDataValue(date);
		if ( ts.isDataMissing(oldvalue) ) {
			continue;
		}
		if ( (adjust_low && (oldvalue < extremeValue)) || (!adjust_low && (oldvalue > extremeValue)) ) {
			// First find adjacent values...
			left_date = new DateTime ( date );
			right_date = new DateTime ( date );
			total = oldvalue - extremeValue;
			total_values = oldvalue;
			count = 1;
			nintervals = 0;
			do_adjust = true;	// Assume we can adjust until find out otherwise.
			while ( true ) {
				left_date.addInterval ( interval_base, -interval_mult );
				++nintervals;
				if ( (maxIntervals != 0) && (nintervals > maxIntervals) ) {
					if ( Message.isDebugOn ) {
						Message.printDebug (1,routine, "Maximum intervals ("+
							maxIntervals + ") exceeded trying to adjust value at " + date + " - not adjusting." );
					}
					do_adjust = false;
					break;
				}
				if ( left_date.lessThan(start) ) {
					if ( Message.isDebugOn ) {
						Message.printDebug(1, routine, "Reached period start trying to adjust value at " + date + " - not adjusting." );
					}
					do_adjust = false;
					break;
				}
				left_value = ts.getDataValue(left_date);
				if ( ts.isDataMissing(left_value) ) {
					if ( Message.isDebugOn ) {
						Message.printDebug (1,routine,"Found missing data at " + left_date + " - skipping it.");
					}
				}
				else {
				    total += (left_value - extremeValue);
					total_values += left_value;
					++count;
				}
				right_date.addInterval ( interval_base, interval_mult );
				if ( right_date.greaterThan(end) ) {
					if ( Message.isDebugOn ) {
						Message.printDebug (1,routine,"Reached period end trying to adjust value at " + date + " - not adjusting." );
					}
					do_adjust = false;
					break;
				}
				right_value = ts.getDataValue(right_date);
				if ( ts.isDataMissing(right_value) ) {
					if ( Message.isDebugOn ) {
						Message.printDebug (1,routine, "Found missing data at " + right_date +" - skipping it.");
					}
				}
				else {
				    total += (right_value - extremeValue);
					total_values += right_value;
					++count;
				}
				average = extremeValue + total/(double)count;
				// If adjusting minimum values, the average can be >= the extreme value
				// If adjusting maximum values, the average can be <= the extreme value
				if ( (adjust_low && (average >= extremeValue)) || (!adjust_low && (average <= extremeValue)) ) {
					// OK to do the adjustment...
					do_adjust = true;
					break;
				}
			}
			if ( !do_adjust ) {
				// Not able to adjust for some reason...
				continue;
			}
			// Use the average value for all adjusted points...
			for ( ; left_date.lessThanOrEqualTo( right_date);
			    left_date.addInterval( interval_base, interval_mult)){
				oldvalue =ts.getDataValue( left_date);
				if(ts.isDataMissing(oldvalue)) {
					continue;
				}
				if ( adjust_average ) {
					// All adjusted values have the same value...
					ts.addToGenesis ( "AdjustExtremes:  adjusted " +
					StringUtil.formatString( oldvalue, "%.6f") + " to " +
					StringUtil.formatString( average, "%.6f") + " at " + left_date );
					if ( doSetFlag ) {
						ts.setDataValue(left_date, average, setFlag, -1);
					}
					else {
						ts.setDataValue(left_date, average);
					}
					++adjustCount;
				}
				else {
				    // Use the weighted average value for all adjusted points...
					left_value = oldvalue - total*oldvalue/total_values;
					ts.addToGenesis ( "AdjustExtremes:  adjusted " +
					StringUtil.formatString( oldvalue, "%.6f") + " to " +
					StringUtil.formatString( left_value, "%.6f") + " at " + left_date );
					if ( doSetFlag ) {
						ts.setDataValue(left_date, left_value, setFlag, -1);
					}
					else {
						ts.setDataValue(left_date, left_value);
					}
					++adjustCount;
				}
			}
		}
	}

	// Fill in the genesis information...

	ts.addToGenesis ( "AdjustExtremes:  Adjusted extremes " + start + " to " + end +
	        " AdjustMethod=" + adjustMethod + " ExtremeToAdjust=" + extremeToAdjust +
	        " ExtremeValue=" + StringUtil.formatString(extremeValue,"%.6f") +
	        " MaxIntervals=" + maxIntervals + "." );
	ts.setDescription ( ts.getDescription() + ", AdjustExtremes" );
	if ( adjustCount > 0 ) {
		// Also set the flag description
		if ( (setFlagDescription != null) && !setFlagDescription.isEmpty() ) {
            ts.addDataFlagMetadata(new TSDataFlagMetadata(setFlag, setFlagDescription));
        }
        else {
            ts.addDataFlagMetadata(new TSDataFlagMetadata(setFlag, "Adjusted extremes for value " + extremeToAdjust));
        }
	}
}

/**
 * Returns whether any Time Series is editable.
 *  
 * @param tslist list of time series 
 * @return True if a time series is editable
 */
public static boolean areAnyTimeSeriesEditable(List<TS> tslist)
{
  int size =0;
  if ( tslist != null )
    {
      size = tslist.size();
    }
  TS ts;

  for ( int i = 0; i < size; i++ )
    {
      ts = tslist.get(i);
      if ( ts == null ) 
        {
          continue;
        }
      if (ts.isEditable())
        {
          return true;
        }
    }

  return false;
}

/**
Determine whether any time series are irregular.
@return true if any time series are irregular, false if not.
@param tslist list of time series to evaluate.
*/
public static boolean areAnyTimeSeriesIrregular ( List<TS> tslist )
{
    int size = 0;
    if ( tslist != null ) {
        size = tslist.size();
    }
    TS ts = null;
    for ( int i = 0; i < size; i++ ) {
        ts = tslist.get(i);
        if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
            return true;
        }
    }
    return false;
}


// FIXME SAM 2009-10-05 Evaluate whether to keep this or intervalsMatch()
/**
Determine whether the intervals for the time series are the same (the base and
multiplier for the interval must agree).
@return true if the intervals are the same.
@param tslist list of time series.
*/
public static boolean areIntervalsSame ( List<TS> tslist )
{
	if ( tslist == null ) {
		// No units.  Decide later whether to throw an exception.
		return true;
	}
	int size = tslist.size();
	if ( size < 2 ) {
		// No need to compare...
		return true;
	}
	// Loop through the time series and the intervals...
	TS ts = null;
	int interval_base, interval_base0 = 0;
	int interval_mult, interval_mult0 = 0;
	boolean first_found = false;
	for ( int i = 0; i < size; i++ ) {
		ts = tslist.get(i);
		if ( ts == null ) {
			continue;
		}
		if ( !first_found ) {
			// Initialize...
			interval_base0 = ts.getDataIntervalBase();
			interval_mult0 = ts.getDataIntervalMult();
			first_found = true;
			continue;
		}
		interval_base = ts.getDataIntervalBase();
		interval_mult = ts.getDataIntervalMult();
		if ( (interval_base != interval_base0) || (interval_mult != interval_mult0) ) {
			return false;
		}
	}
	return true;
}

/**
Determine the units for the time series compatible.
The units are allowed to be different as long as they are within the same
dimension (e.g., each is a length).
If it is necessary to guarantee that the units are exactly the same, call the
version of this method that takes the boolean flag.
@return true if the units in the time series are compatible.
@param tslist list of time series.
*/
public static boolean areUnitsCompatible ( List<TS> tslist )
{	return areUnitsCompatible ( tslist, false );
}

/**
Determine whether the units for a list of time series are compatible.
The units are allowed to be different as long as they are within the same
dimension (e.g., each is a length).
@return true if the units in the time series are compatible, according to the "require_same" flag.
@param tslist list of time series.
@param require_same Flag indicating whether the units must exactly match (no
conversion necessary).  If true, the units must be the same.  If false, the
units must only be in the same dimension (e.g., "CFS" and "GPM" would be compatible).
*/
public static boolean areUnitsCompatible ( List<TS> tslist, boolean require_same )
{	if ( tslist == null ) {
		// No units.  Decide later whether to throw an exception.
		return true;
	}
	int size = tslist.size();
	if ( size < 2 ) {
		// No need to compare...
		return true;
	}
	// Loop through the time series and get the units...
	List<String> units = new ArrayList<String>();
	TS ts = null;
	String units_string = null;
	for ( int i = 0; i < size; i++ ) {
		ts = tslist.get(i);
		if ( ts == null ) {
			continue;
		}
		units_string = ts.getDataUnits();
		if ( units_string == null ) {
			continue;
		}
		units.add ( units_string );
	}
	boolean result = DataUnits.areUnitsStringsCompatible(units, require_same);
	return result;
}

/**
Average the data for a list of time series by averaging values at each interval.
The new time series will have the period requested by the start and end dates.
The data type, units, etc., will be taken from the first time series in the list
and should be reset if necessary after the call.  It is assumed that the time
series align in date/time (i.e., this method cannot be used to shift time series in time before averaging).
@param tslist Time series to get data from, in order to calculate the average.
@param start_date Date to start data analysis (relative to the new average
time series).  If null, the earliest date/time is taken from the time series
list.
@param end_date Date to stop the data transfer (relative to the new average
time series).  If null, the latest date/time is taken from the time series list.
@param props Properties to control the average.  Recognized properties are:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>TransferData</b></td>
<td><b>Indicates how data should be transferred from one time series to
another.  Using "ByDateTime" will cause the dates in both time series to match,
which may be an issue if one has leap year data and the other does not.  Using
"Sequentially" will result in data from the independentTS to be copied
sequentially, regardless of date/time, which will preserve the continuity of
the data but perhaps not the date/time.</b>
<td>ByDateTime</td>
</tr>
</table>
@exception Exception if an error occurs (usually null input).
*/
public static TS average ( List<TS> tslist, DateTime start_date, DateTime end_date, PropList props )
throws Exception
{	String  message, routine = "TSUtil.average";

	if ( (tslist == null) || (tslist.size() == 0) ) {
		message = "No time series to average.";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}

	// Check the properties that influence this method...

	boolean transfer_bydate = true;	// Default - make dates match in both time series.
	if ( props != null ) {
		String prop_val = props.getValue("TransferData");
		if ( (prop_val != null) && prop_val.equalsIgnoreCase(TransferDataHowType.SEQUENTIALLY.toString()) ) {
			// Transfer sequentially...
			transfer_bydate = false;
		}
	}

	// Get valid dates because the ones passed in may have been null...

	DateTime start = null;
	if ( start_date != null ) {
		start = new DateTime ( start_date );
	}
	DateTime end = null;
	if ( end_date != null ) {
		end = new DateTime ( end_date );
	}
	if ( (start == null) || (end == null) ) {
		// Try to get from the data...
		TSLimits valid_dates = getPeriodFromTS ( tslist, MAX_POR );
		if ( start == null ) {
			start = valid_dates.getDate1();
		}
		if ( end == null ) {
			end = valid_dates.getDate2();
		}
	}
	TS ts = null;	// Used for temporary data.

	// Create a new time series to be returned, using the first time series in the list as a template...

	ts = tslist.get(0);
	TS newts = newTimeSeries ( ts.getIdentifierString(), true );
	// Only transfer generic fields, not scenario, etc...
	TSIdent tsident = new TSIdent();
	tsident.setLocation("unknown");
	tsident.setType(ts.getDataType());
	tsident.setInterval(ts.getIdentifier().getInterval());
	newts.setIdentifier ( tsident );
	newts.setDataUnits ( ts.getDataUnits() );
	newts.setDescription ( "Average time series" );
	newts.setDate1 ( start );
	newts.setDate2 ( end );
	int interval_base = newts.getDataIntervalBase();
	int interval_mult = newts.getDataIntervalMult();
	newts.allocateDataSpace ();

	// Loop through the time series and average the data...

	if ( interval_base == TimeInterval.IRREGULAR ) {
		message = "Averaging IrregularTS is not supported";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	DateTime date = new DateTime ( start );	// Start time for iterator.

	TS [] tsarray = null; // Time series list as an array, to improve performance.
	TSIterator [] tsi = null; // TS iterators for each time series in list.
	int size = tslist.size(); // Number of TS in list.
	int its = 0; // Loop counter for time series.
	tsarray = new TS[size];
	// Assign the time series to an array to increase performance...
	for ( its = 0; its < size; its++ ) {
		tsarray[its] = tslist.get(its);
	}
	// Create an iterator for each time series, if needed...
	if ( !transfer_bydate ) {
		// Don't specify the end date because without doing some
		// computations, we don't know for sure.  This should get set
		// to the end date of new TS, which is OK.
		tsi = new TSIterator[size];
		for ( its = 0; its < size; its++ ) {
			tsi[its] = tsarray[its].iterator(start,null);
		}
	}

	// Loop until the end date has been reached...

	double data_value = 0.0; // Data value from time series.
	double missing = newts.getMissing(); // Missing data value.
	double average = 0.0; // Average of time series data values at an interval.
	int count = 0; // Number of values averaged.
	for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
		// Initialize the average value...
		average = missing;
		count = 0;
		// Loop through the time series in the list...
		for ( its = 0; its < size; its++ ) {
			//ts = tsarray[its];
			if ( transfer_bydate ) {
				data_value = tsarray[its].getDataValue ( date );
			}
			else {
			    // Use the iterator...
				tsi[its].next();
				data_value = tsi[its].getDataValue ();
			}
			if ( !tsarray[its].isDataMissing(data_value) ) {
				++count;
				if ( !newts.isDataMissing(average) ) {
					// Have previously initialized the total...
					average += data_value;
				}
				else {
				    // Have not initialized the average...
					average = data_value;
				}
			}
		}
		// Set the data value if not missing...
		if ( !newts.isDataMissing(average) ) {
			newts.setDataValue ( date, average/count );
		}
	}
	
	// Fill in the genesis information...

	newts.addToGenesis ( "Averaged data " + start.toString() + " to " + end.toString() + " by using values from:" );
	for ( its = 0; its < size; its++ ) {
		ts = tsarray[its];
		newts.addToGenesis ( "    " + ts.getIdentifierString() );
	}
	if ( !transfer_bydate ) {
		newts.addToGenesis ( "Data values were transferred by date/time." );
	}
	else {
	    newts.addToGenesis ( "Data values were transferred sequentially from start date/time" );
	}
	return newts;
}

/**
Blend one time series into another.  Only regular time series
can be blended.  The resulting time series will have the combined period of both
time series.  Only data after the last date in the original time series will be blended.
@param ts Time series to be modified.
@param tsb Time series to blend.
@exception Exception if there is an error processing (e.g., incompatible units).
*/
public static void blend ( TS ts, TS tsb )
throws Exception
{	if ( ts == null ) {
		return;
	}
	if ( tsb == null ) {
		throw new Exception ( "Time series to blend is null." );
	}

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( (interval_base != tsb.getDataIntervalBase()) || (interval_mult != tsb.getDataIntervalMult()) ) {
		throw new Exception ( "Cannot blend data with different intervals (" +
		ts.getIdentifierString() + " and " + tsb.getIdentifierString() + ")." );
	}
	if ( interval_base == TimeInterval.IRREGULAR ) {
		throw new Exception ( "Cannot blend irregular interval data (" + ts.getIdentifierString() + ")" );
	}

	// The output period is at least that of "ts" and if the end of tsb
	// is > ts, then the end date is that of tsb...

	DateTime ts_date2_orig = new DateTime ( ts.getDate2() );
	DateTime start = new DateTime ( ts.getDate1() );
	DateTime end = new DateTime ( ts.getDate2() );
	if ( tsb.getDate2().greaterThan(end) ) {
		end = new DateTime ( tsb.getDate2() );
		// Change the period of the original time series...
		ts.changePeriodOfRecord ( start, end );
	}

	// Loop using addInterval.  Only blend the second time series after data in the first.

	DateTime date = null;
	if ( tsb.getDate1().greaterThan(ts_date2_orig) ) {
		// Reset to start transfer at the start of the second TS...
		date = new DateTime ( tsb.getDate1() );
	}
	else {
	    // Process a partial tsb time series after the end of the first
		// time series (end of first + one interval)...
		date = new DateTime ( ts_date2_orig );
		date.addInterval ( interval_base, interval_mult );
	}
	// Always end at the end of the second time series...
	end = new DateTime ( tsb.getDate2() );
		
	double oldvalue = 0.0;
	for (	; date.lessThanOrEqualTo( end );
		date.addInterval(interval_base, interval_mult) ) {
		oldvalue = tsb.getDataValue(date);
		if ( !tsb.isDataMissing(oldvalue) ) {
			ts.setDataValue ( date, oldvalue );
		}
	}

	// Fill in the genesis information...

	ts.addToGenesis ( "Blended " + start.toString() + " to " + end.toString() + ": " + tsb.getDescription() + "." );

	ts.setDescription ( ts.getDescription() + ", blend" + tsb.getDescription() );
}

/**
Determine the data size for a time series for a period.  If an IrregularTS
data size is needed, call the non-static IrregularTS.calculateDataSize() method.
@return The number of data points for a time series of the given data
interval for the specified period.  This is a utility function to call the
time series base class calculateDataSize methods.  An instance of the time
series is not required (note that this will not work for IrregularTS, where the
spacing of data is unknown unless a time series is supplied).
@param start_date The first date of the period.
@param end_date The last date of the period.
@param interval_base The time series data interval base.
@param interval_mult The time series data interval multiplier.
*/
public static int calculateDataSize ( DateTime start_date, DateTime end_date, int interval_base, int interval_mult )
{	String routine = "TSUtil.calculateDataSize";

	if ( start_date == null ) {
		Message.printWarning ( 2, routine, "Start date is null" );
		return 0;
	}
	if ( end_date == null ) {
		Message.printWarning ( 2, routine, "End date is null" );
		return 0;
	}
	if ( interval_base == TimeInterval.YEAR ) {
		return YearTS.calculateDataSize ( start_date, end_date, interval_mult );
	}
	else if ( interval_base == TimeInterval.MONTH ) {
		return MonthTS.calculateDataSize ( start_date, end_date, interval_mult );
	}
	else if ( interval_base == TimeInterval.DAY ) {
		return DayTS.calculateDataSize ( start_date, end_date, interval_mult );
	}
	else if ( interval_base == TimeInterval.HOUR ) {
		return HourTS.calculateDataSize ( start_date, end_date, interval_mult );
	}
	else if ( interval_base == TimeInterval.MINUTE ) {
		return MinuteTS.calculateDataSize ( start_date, end_date, interval_mult );
	}
	else if ( interval_base == TimeInterval.IRREGULAR ) {
		// This will just count the data values in the period...
		return IrregularTS.calculateDataSize ( start_date, end_date, interval_mult );
	}
	else {
	    // Interval is not supported.  Big problem!
		Message.printWarning ( 2, routine, "Time series interval " + interval_base + " is not supported" );
		return 0;
	}
}

/**
@return The number of data points for a time series of the given data
interval for the specified period.  This is a utility function to call the
time series base class calculateDataSize methods.  An instance of the time
series is required (use this for IrregularTS, especially).  If the start and
end dates are not specifiec, the time series limits will be used.
@param ts Time series of interest.
@param start_date The first date of the period.
@param end_date The last date of the period.
*/
public static int calculateDataSize ( TS ts, DateTime start_date, DateTime end_date )
{	if ( ts == null ) {
		return 0;
	}

	/* TODO SAM 2007-03-01 Evaluate logic
	if ( start_date == null ) {
		start_date2 = new DateTime ( ts.getDate1() );
	}
	if ( end_date == null ) {
		end_date2 = new DateTime ( ts.getDate2() );
	}
	*/
	if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
		// Need to do something different than for regular...
		IrregularTS irrts = (IrregularTS)ts;
		return irrts.calculateDataSize ( start_date, end_date );
	}
	else {
	    return calculateDataSize ( start_date, end_date, ts.getDataIntervalBase(), ts.getDataIntervalMult() );
	}
}

// ----------------------------------------------------------------------------
// convertUnits - Convert data in TS from current units to requested units
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 05 Oct 96	Steven A. Malers, RTi	Clean up code while debugging.  Figure
//					out the the error code from
//					HMGetNWSConversion was not being
//					handled properly.
// 31 Oct 96	SAM, RTi		Figure out that "chdummy" was a
//					non-initialized pointer that was
//					sometimes fouling up the conversion
//					routine.  Set to an empty string.
// 05 Mar 1998	SAM, RTi		Port to Java, C++.  Take legacy code
//					from the TS C library.
// ----------------------------------------------------------------------------
// Variable	I/O	Description
//
// add		L	addition factor
// mult		L	multiplication factor
// req_units	I	requested units
// ts		I/O	TimeSeries which contains data to convert
// ----------------------------------------------------------------------------

/**
Convert the units of the specified time series to those requested.  Use the
units abbreviations stored in the DataUnits class data.
@param ts Time series to convert units.
@param req_units Requested units.
@see DataUnits
@exception RTi.TS.TSException If the time series is null or if the conversion
cannot be found.  Requested units of blank or null result in no change.
*/
public static void convertUnits ( TS ts, String req_units ) throws TSException
{	double mult, add;
	String rtn = "TSUtil.convertUnits";
	int	dl = 20;

	// Make sure that there is a time series...

	if ( ts == null ) {
		Message.printWarning ( 2, rtn, "Null time series - no action taken." );
		return;
	}

	// Make sure units are valid...

	if ( (req_units == null) || (req_units.length() == 0) ) {
		// Probably no need to even notify the user.  Some software may
		// call this code even when units are set to a default.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, rtn, "No coversion necessary since units are null or blank." );
		}
		return;
	}

	// Make sure that a conversion is necessary...

	String units = ts.getDataUnits();
	if ( req_units.equalsIgnoreCase(units) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, rtn, "No coversion necessary since units \"" + units +
			"\" are the same as the requested units \"" + req_units+ "\"." );
		}
		return;
	}

	// Find conversion factor...

	DataUnitsConversion conversion = null;
	try {
	    conversion = DataUnits.getConversion ( units, req_units );
	}
	catch ( Exception e ) {
		throw new TSException ( "Unable to get conversion from \"" + units + "\" to \"" +
		req_units + "\"" );
	}
	if ( conversion == null ) {
		throw new TSException ( "Unable to get conversion from \"" + units + "\" to \"" +
		req_units + "\"" );
	}
	mult = conversion.getMultFactor();
	add = conversion.getAddFactor();

	// Loop through the entire time series...

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	double data_value;
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			data_value = tsdata.getDataValue();
			if ( !ts.isDataMissing(data_value) ) {
				// Not missing, so do the conversion...
				data_value = add + data_value*mult;
			}
			tsdata.setDataValue(data_value);
			// Have to do this manually since TSData are being modified directly to improve performance...
			ts.setDirty ( true );
		}
	}
	else {
	    // Loop using addInterval...
		DateTime start_date = new DateTime ( ts.getDate1() );
		DateTime end_date = new DateTime ( ts.getDate2() );
		DateTime date = new DateTime ( start_date );
		for ( ; date.lessThanOrEqualTo( end_date); date.addInterval(interval_base, interval_mult) ) {
			data_value = ts.getDataValue ( date );
			if ( !ts.isDataMissing(data_value) ) {
				// Not missing, so do the conversion...
				data_value = add + data_value*mult;
				ts.setDataValue ( date, data_value );
			}
		}
	}

	// Set the units field to reflect new units of data...

	if ( add == 0.0 ) {
		ts.addToGenesis ( "Converted data units from \"" + units
	 	+ "\" to \"" + req_units + "\" using *" + StringUtil.formatString(mult,"%.6f") );
	}
	else {
	    ts.addToGenesis ( "Converted data units from \"" + units + "\" to \"" + req_units + "\" using *" +
		StringUtil.formatString(mult,"%.6f") + "+" + StringUtil.formatString(add,"%.6f") );
	}
	ts.setDataUnits ( req_units );
}

/**
Copy one time series header to another.  This simply calls the TS.copyHeader()
method.
@param ts1 Source copy.
@param ts2 New copy.
@see TS#copyHeader
@deprecated Use TS.copyHeader.
*/
public static void copyHeader ( TS ts1, TS ts2 )
{	ts1.copyHeader ( ts2 );
}

/**
Count the number of data values that are isolated (have missing values on both sides).  This
is useful, for example, for determining whether graphs should be drawn with symbols to show the data.
@param ts time series to process
@param start the starting date/time for the count, or null to process the entire time series
@param end the ending date/time for the count, or null to process the entire time series
*/
public static int countIsolatedPoints ( TS ts, DateTime start, DateTime end )
throws Exception
{
	TSIterator tsi = ts.iterator(start,end);
	double val1 = ts.getMissing(); // Previous to previous to current value
	double val2 = ts.getMissing(); // Previous to current value
	double val3 = ts.getMissing(); // Current value
	int isolatedCount = 0;
	while ( tsi.next() != null ) {
		// Move the previous values forward (order of this is important!)
		val1 = val2;
		val2 = val3;
		// Get the current value
		val3 = tsi.getDataValue();
		// Now do the check of missing on the center point
		if ( !ts.isDataMissing(val2) && ts.isDataMissing(val1) && ts.isDataMissing(val3) ) {
			// Have a non-missing value with missing on each side, so an isolated point
			++isolatedCount;
		}
	}
	return isolatedCount;
}

/**
Create a cache of date/times for time series (all date/times that occur),
typically used to create an all-inclusive list of date/times for irregular time series,
such as when comparing or displaying time series.
Time series do not need to be irregular.
This code was extracted from TSViewTable_TableModel.java.
@param tslist list of time series to extract date/times from
@param analysisStart date/time to start extracting date/times (default is full overlapping time series period).
@param analysisEnd date/time to end extracting date/times (default is full overlapping time series period).
@return list of DateTime corresponding to all possible date/times in the provided time series
*/
public static List<DateTime> createTSDateTimeList ( List<TS> tslist, DateTime analysisStart, DateTime analysisEnd )
throws TSException
{   String routine = "TSUtil.createTSDateTimeList";
	List<DateTime> dateTimeList = new ArrayList<>();
    // More than one irregular time series.  They at least have to have the same date/time precision
    // for the period.  Otherwise it will be difficult to navigate the data.
	// Use the starting date/time in the time series to check for precision since date/times are processed here.
    int tsPrecision = -1;
    int size = tslist.size();
    TS ts;
    for ( int its = 0; its < size; its++ ) {
        if ( tslist.get(its) == null ) {
            continue;
        }
        ts = (IrregularTS)tslist.get(its);
        if ( ts.getDate1() == null ) {
            continue;
        }
        int tsPrecision2 = ts.getDate1().getPrecision();
        if ( tsPrecision < 0 ) {
        	// Assign
        	tsPrecision = tsPrecision2;
        }
        else if ( tsPrecision2 != tsPrecision ) {
            // This will be a problem in processing the data
            String message = "Time series date/times do not have the same precision by checking period start.  Can't process";
            Message.printWarning ( 2, routine, message );
            throw new UnequalTimeIntervalException ( message );
        }
    }
    // Was able to determine the precision of data so can continue
    // The logic works as follows:
    // 0) Advance the iterator for each time series to initialize
    // 1) Find the earliest date/time in the iterator current position
    // 2) Add cached date/times that will result in:
    //    - actual value if time series has a value at the date/time
    //    - values not at the same date/time result in blanks for the other time series
    // 3) For any values that will be output, advance that time series' iterator
    // 4) Go to step 1
    // Create iterators for each time series
    boolean doCheckPeriod = false;
    if ( (analysisStart != null) || (analysisEnd != null) ) {
    	// Period has been specified so 
    	doCheckPeriod = true;
    }
    TSIterator [] tsIteratorArray = new TSIterator[tslist.size()];
    for ( int its = 0; its < size; its++ ) {
        if ( tslist.get(its) == null ) {
            tsIteratorArray[its] = null; // Keep same order as time series
        }
        //ts = (IrregularTS)tslist.get(its);
        ts = tslist.get(its);
        try {
            // Iterate through full period
        	// - TODO smalers 2019-10-02 need to implement analysis period but need to make sure it aligns
            tsIteratorArray[its] = ts.iterator();
        }
        catch ( Exception e ) {
            tsIteratorArray[its] = null;; // Keep same order as time series
        }
    }
    int its;
    TSIterator itsIterator;
    // Use the following to extract dates from each time series
    // A call to the iterator next() method will return null when no more data, which is
    // the safest way to process the data
    TSData [] tsdata = new TSData[tslist.size()];
    TSData tsdataMin = null; // Used to find min date/time for all the iterators
    DateTime dtMin = null; // Used to compare date/times for all the iterators
    DateTime dtCached = null; // Cached date/time
    int iteratorMin = -1;
    int loopCount = 0;
    boolean doAdd = true; // When checking analysis period
    while ( true ) {
        // Using the current date/time, output the earliest value for all time series that have the value and
        // increment the iterator for each value that is output.
        ++loopCount;
        if ( loopCount == 1 ) {
            // Need to call next() one time on all time series to initialize all iterators to the first
            // data point in the time series.  Otherwise, next() is only called below to advance.
            for ( its = 0; its < size; its++ ) {
                itsIterator = tsIteratorArray[its];
                if ( itsIterator != null ) {
                    tsdata[its] = itsIterator.next();
                }
            }
        }
        // Loop through the iterators:
        // 1) Find the earliest date/time
        // 2) Add to the cache
        // 3) Advance the iterator(s)
        // Do this until all iterators next() have returned null
        tsdataMin = null;
        dtMin = null;
        for ( its = 0; its < size; its++ ) {
            if ( tsdataMin == null ) {
                // Find the first date/time for all the iterators at their current positions
                if ( tsdata[its] != null ) {
                    tsdataMin = tsdata[its];
                    dtMin = tsdataMin.getDate();
                    iteratorMin = its;
                    //Message.printStatus(2, routine, "Initializing minimum date/time to " + dtMin );
                }
            }
            else {
                // Have a non-null first date/time to compare to so check this iterator against it
                // The lessThan() method DOES NOT compare time zone in any case
                if ( (tsdata[its] != null) && tsdata[its].getDate().lessThan(dtMin) ) {
                    // Have found an earlier date/time to be used in the comparison
                    // Note - time zone is NOT checked
                    dtMin = tsdata[its].getDate();
                    tsdataMin = tsdata[its];
                    iteratorMin = its;
                    //Message.printStatus(2, routine, "Found earlier minimum date/time [" + its + "] " + dtMin );
                }
            }
        }
        // 2) Add to the cache
        if ( dtMin == null ) {
            // Done processing all data
            break;
        }
        // Create a new instance so independent of any data manipulations.
        // Create a fast instance since it will be used for iteration and data access but not be manipulated or checked
        dtCached = new DateTime(dtMin,DateTime.DATE_FAST);
        // TODO smalers 2019-01-02 deal with time zone later
        //if ( !__irregularTZSame ) {
            // Set the timezone to blank for cached date/times rather than showing wrong time zone that might be misinterpreted
            dtCached.setTimeZone("");
        //}
        if ( doCheckPeriod ) {
        	// Need to make sure the period is within the requested period
        	// Default is to add the date/time
        	doAdd = true;
        	if ( analysisStart != null ) {
        		// Need to check analysis start
        		if ( dtCached.lessThan(analysisStart) ) {
        			// Date/time is before the analysisStart so don't include
        			doAdd = false;
        		}
        	}
        	if ( doAdd && (analysisEnd != null) && dtCached.greaterThan(analysisEnd) ) {
        		// Date/time is OK for analysisStart (above)
        		// but date/time is after the analysis end
        		doAdd = false;
        	}
        	if ( doAdd ) {
        		// OK to add the date/time
        		dateTimeList.add(dtCached);
        	}
        }
        else {
        	dateTimeList.add(dtCached);
        }
        //Message.printStatus(2,routine,"Row [" + (__irregularDateTimeCache.size() - 1) + "] " + dtCached);
        // 3) Advance the iterator for the one with the minimum date/time and all with the same date/time
        // Note - time zone is NOT checked by equals()
        for ( its = 0; its < size; its++ ) {
            // First check below increases performance a bit
            // Use the prototype date if available to ensure that time zone is handled
        	/* TODO smalers 2019-10-02 deal with time zone later
            if ( (__irregularPrototypeDateTime != null) && (__irregularPrototypeDateTime[its] != null) ) {
                // Use the prototype DateTime (which has proper time zone) and overwrite the specific date/time values
                // Do not call setDate() because it will set whether to use time zone and defeat the purpose of the prototype
                __irregularPrototypeDateTime[its].setYear(dtMin.getYear());
                __irregularPrototypeDateTime[its].setMonth(dtMin.getMonth());
                __irregularPrototypeDateTime[its].setDay(dtMin.getDay());
                __irregularPrototypeDateTime[its].setHour(dtMin.getHour());
                __irregularPrototypeDateTime[its].setMinute(dtMin.getMinute());
                __irregularPrototypeDateTime[its].setSecond(dtMin.getSecond());
                dtMin = __irregularPrototypeDateTime[its];
            }
            */
            if ( (iteratorMin == its) || ((tsdata[its] != null) && dtMin.equals(tsdata[its].getDate())) ) {
                tsdata[its] = tsIteratorArray[its].next();
                //Message.printStatus(2, routine, "Advanced iterator[" + its + "] date/time to " + tsdata[its] );
            }
        }
    }
    return dateTimeList;
}

/**
Create a monthly summary report for the time series.  This format is suitable
for data intervals less than monthly.  Data are first accumulated to daily
values and are then accumulated to monthly for the final output.  This report
should not be confused with the MonthTS.formatOutput() method, which just displays raw data values.
@param ts Time series to process.
@param date1 First date/time to process.
@param date2 Last date/time to process.
@param props Properties to control formatting:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>CalendarType</b></td>
<td>The type of calendar, either "Water" (Oct through Sep), "NovToOct"
(Nov through Oct), or "Calendar" (Jan through Dec), consistent with YearType enumeration.
</td>
<td>CalenderYear (but may be made sensitive to the data type or units in the future).</td>
</tr>

<tr>
<td><b>DayType</b></td>
<td>
 If "Mean" values will be averaged to obtain the daily value.
If "Total", values will be totaled to obtain the daily value.
</td>
<td>"Mean"
</td>
</tr>

<tr>
<td><b>OutputPrecision</b></td>
<td>The precision of numbers as printed.  All data values are printed in a
9-digit column.  The precision controls how many digits are shown after the decimal.
</td>
<td>
If not specified, an attempt will be made to use the units to
look up the precision.  If that fails, a default of 1 will be used.
</td>
</tr>

</table>
@return a list of String containing the report.
@exception Exception if there is an error generating the report.
*/
public static List<String> createMonthSummary ( TS ts, DateTime date1, DateTime date2, PropList props )
throws Exception
{	// Pull much of the code from the MonthTS.formatOutput() method.  This
	// method creates a similar report but does not track data as specifically as is needed here.

	String message, routine = "TSUtil.createMonthSummary";
	int column, dl = 20, row;
	int data_interval_base = ts.getDataIntervalBase();
	int data_interval_mult = ts.getDataIntervalMult();

	if ( (data_interval_base != TimeInterval.DAY) && (data_interval_base != TimeInterval.HOUR) &&
		(data_interval_base != TimeInterval.MINUTE) ) {
		message = "The Month Summary Report can only be used for day, hour, and minute interval.";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}

	List<String> strings = new ArrayList<String>(20);
	StringUtil.addListToStringList ( strings, ts.formatHeader() );

	// Determine the units to output.  For now use what is in the time series...

	String req_units = ts.getDataUnits();

	// Get the precision...

	String prop_value = props.getValue ( "OutputPrecision" );
	String data_format = "%9.1f";
	if ( prop_value == null ) {
		// Try older...
		prop_value = props.getValue ( "Precision" );
		if ( prop_value != null ) {
			Message.printWarning ( 2, routine, "Need to switch Precision property to OutputPrecision");
		}
	}
	if ( prop_value == null ) {
		// Try to get units information for default...
		try {
		    DataUnits u = DataUnits.lookupUnits ( req_units );
			data_format = "%9." + u.getOutputPrecision() + "f";
		}
		catch ( Exception e ) {
			// Default...
			data_format = "%9.1f";
		}
	}
	else {
	    // Set to requested precision...
		data_format = "%9." + prop_value + "f";
	}

	// Determine whether water or calendar year...

	prop_value = props.getValue ( "CalendarType" );
	if ( prop_value == null ) {
		// Default to "Calendar"...
		prop_value = "" + YearType.CALENDAR;
	}
	YearType calendar = YearType.valueOfIgnoreCase(prop_value);

	// Determine the period to output.  For now always output the total...

	DateTime start_date = new DateTime (ts.getDate1() );
	DateTime end_date = new DateTime (ts.getDate2() );
	if ( (date1 != null) && (date2 != null) ) {
		start_date = new DateTime ( date1 );
		end_date = new DateTime ( date2 );
	}

	// Print the body of the summary...

	// Need to check the data type to determine if it is an average
	// or a total.  For now, make some guesses based on the units...

	strings.add ( "" );
		
	prop_value = props.getValue ( "DayType" );
	String year_column = "Average";
	boolean day_is_average = true;
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Total") ) {
		// Special cases hard-coded in the short term.
		year_column = "Total";
		day_is_average = false;
	}

	if ( calendar == YearType.WATER ) {
		// Water year...
		strings.add (
"Year    Oct       Nov       Dec       Jan       Feb       Mar       Apr       May       Jun       Jul        Aug      Sep     " + year_column );
			strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
	}
	else if ( calendar == YearType.NOV_TO_OCT ) {
		// Irrigation year...
		strings.add (
"Year    Nov       Dec       Jan       Feb       Mar       Apr       May       Jun       Jul       Aug        Sep      Oct     " + year_column );
		strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
	}
	else {
	    // Calendar year...
		strings.add (
"Year    Jan       Feb       Mar       Apr       May       Jun       Jul        Aug      Sep       Oct       Nov       Dec     " + year_column );
		strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
	}

	// Now transfer the monthly data into a summary matrix, which looks like:
	//
	// 0 - 11 Total/Ave
	// ...
	// statistics
	
	// Adjust the start and end dates to be on full years for the calendar that is requested...

	int year_offset = 0;
	if ( calendar == YearType.CALENDAR ) {
		// Just need to output for the full year...
		start_date.setMonth ( 1 );
		end_date.setMonth ( 12 );
	}
	else if ( calendar == YearType.NOV_TO_OCT ) {
		// Need to adjust for the irrigation year to make sure
		// that the first month is Nov and the last is Oct...
		if ( start_date.getMonth() < 11 ) {
			// Need to shift to include the previous irrigation year...
			start_date.addYear ( -1 );
		}
		// Always set the start month to Nov...
		start_date.setMonth ( 11 );
		if ( end_date.getMonth() > 11 ) {
			// Need to include the next irrigation year...
			end_date.addYear ( 1 );
		}
		// Always set the end month to Oct...
		end_date.setMonth ( 10 );
		// The year that is printed in the summary is actually later than the calendar for the Nov month...
		year_offset = 1;
	}
	else if ( calendar == YearType.WATER ) {
		// Need to adjust for the water year to make sure that the first month is Oct and the last is Sep...
		if ( start_date.getMonth() < 10 ) {
			// Need to shift to include the previous water year...
			start_date.addYear ( -1 );
		}
		// Always set the start month to Oct...
		start_date.setMonth ( 10 );
		if ( end_date.getMonth() > 9 ) {
			// Need to include the next water year...
			end_date.addYear ( 1 );
		}
		// Always set the end month to Sep...
		end_date.setMonth ( 9 );
		// The year that is printed in the summary is actually
		// later than the calendar for the Oct month...
		year_offset = 1;
	}

	// Make sure that the start and end date/times have values set for the
	// precision of the data to get a full month and calculate the number of needed values...

	// TODO SAM 2004-09-07 The start appears to be the interval-ending
	// date/time for the first interval in the month...

	int nvalues_in_day_needed = 0;
	if ( data_interval_base == TimeInterval.DAY ) {
		start_date.setPrecision ( DateTime.PRECISION_DAY );
		start_date.setDay ( 1 );
		end_date.setPrecision ( DateTime.PRECISION_DAY );
		end_date.setDay ( TimeUtil.numDaysInMonth(end_date.getMonth(), end_date.getYear()) );
		nvalues_in_day_needed = 1;
	}
	else if ( data_interval_base == TimeInterval.HOUR ) {
		start_date.setPrecision ( DateTime.PRECISION_HOUR );
		start_date.setDay ( 1 );
		if ( data_interval_mult == 24 ) {
			start_date.setHour ( 0 );
			start_date.setDay ( 2 );
			nvalues_in_day_needed = 1;
		}
		else {
		    start_date.setHour ( data_interval_mult );
			nvalues_in_day_needed = 24/data_interval_mult;
		}
		end_date.setPrecision ( DateTime.PRECISION_HOUR );
		end_date.setDay ( TimeUtil.numDaysInMonth(end_date.getMonth(), end_date.getYear()) );
		// First interval of next day..
		end_date.setHour ( 0 );
		end_date.addDay ( 1 );
	}
	else if ( data_interval_base == TimeInterval.MINUTE ) {
		/* TODO SAM 2004-09-07 Why do anything?  Why take such
		special care above?
		start_date.setPrecision ( DateTime.PRECISION_MINUTE );
		start_date.setMinute ( data_interval_mult );
		end_date.setPrecision ( DateTime.PRECISION_MINUTE );
		end_date.setDay ( TimeUtil.numDaysInMonth(end_date.getMonth(), end_date.getYear()) );
		// First interval of next day..
		end_date.setMinute ( 0 );
		end_date.setHour ( 0 );
		end_date.addDay ( 1 );
		*/
		nvalues_in_day_needed = 24*60/data_interval_mult;
	}
	Message.printStatus ( 1, routine, "Generating monthly summary for " + start_date + " to " + end_date );

	// Calculate the number of years...
	int num_years = (end_date.getAbsoluteMonth() - start_date.getAbsoluteMonth() + 1)/12;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Printing " + num_years + " years of summary for " +
		start_date.toString(DateTime.FORMAT_YYYY_MM) + " to " + end_date.toString(DateTime.FORMAT_YYYY_MM) );
	}
	// Allow for total column...
	double data[][] = new double[num_years][13];	// Monthly values
	double data_max[][] = new double[num_years][13];// Max daily value in a month.
	double data_min[][] = new double[num_years][13];// Min daily value in a month.

	// Initialize...
	double missing = ts.getMissing();
	int j;
	for ( int i = 0; i < num_years; i++ ) {
		for ( j = 0; j < 13; j++ ) {
			data[i][j] = missing;
			data_max[i][j] = missing;
			data_min[i][j] = missing;
		}
	}

	// Now loop through the time series and transfer to the proper location in the matrix...
	double day_total = missing, year_total = missing;
	double day_value = missing, month_value = missing, month_total = missing;
	double value = missing;
	int nvalues_in_day = 0;
	DateTime date = new DateTime(start_date,DateTime.DATE_FAST);
	StringBuffer buffer = null;
	int non_missing_in_row = 0;
	// We have adjusted the dates above, so we always start in column 0 (first month in year)...
	column = 0;
	row = 0;
	// Set the previous to the current so that we don't force processing
	// the first time through the loop (it will occur for daily data because of a check below.
	int day = date.getDay(), day_prev = day, month = date.getMonth(), month_prev = month;
	boolean need_to_process_day = false;
	boolean need_to_process_month = false;
	int nvalues_in_month = 0;
	int nvalues_in_month_needed = 0;
	// The loop goes through each value.  Accumulate to days and then to month, as needed...
	for ( ; date.lessThanOrEqualTo(end_date); date.addInterval(data_interval_base, data_interval_mult) ) {
		value = ts.getDataValue ( date );
/*
		//if ( Message.isDebugOn ) {
			//Message.printDebug ( dl, routine, "Processing " +
			Message.printStatus ( 1, routine, "Processing " + date +
			" row:" + row + " column:" + column + " value:"+value );
		//}
*/
		if ( !ts.isDataMissing(value) ) {
			if ( ts.isDataMissing(day_total) ) {
				day_total = value;
			}
			else {
			    day_total += value;
			}
			++nvalues_in_day;
		}
		// Check to see if the day is complete.  If so, compute the daily value.
		day = date.getDay();
		need_to_process_day = false;
		if ( data_interval_base == TimeInterval.DAY ) {
			// Daily time step so always need to process...
			need_to_process_day = true;
		}
		else if ( day != day_prev ) {
			// First interval in next day, which is still considered
			// part of the previous day because intervals are dated
			// at the end of the interval (assuming no instantaneous data)...
			need_to_process_day = true;
		}
		if ( !need_to_process_day ) {
			// Not a new day so go to the next value to get the next value to accumulate...
			day_prev = day;
			continue;
		}
		// Else, save the daily value for later use in monthly array...
		// Allow missing data values to be saved...
		day_prev = day;
		if ( nvalues_in_day != nvalues_in_day_needed ) {
			// Don't have the right number of values so set to missing...
			day_value = missing;
		}
		else {
		    // Compute the day value...
			if ( day_is_average ) {
				if ( !ts.isDataMissing(day_total) ) {
					if ( nvalues_in_day > 0 ) {
						day_value = day_total/(double)nvalues_in_day;
					}
					else {
					    day_value = missing;
					}
				}
			}
			else {
			    day_value = day_total;
			}
		}
/*
		Message.printStatus ( 1, routine, "SAMX Setting daily value "+
			day_value + " nvalues:" + nvalues_in_day + " nvalues_needed:" + nvalues_in_day_needed );
*/
		// Check the minimum and maximum daily values...
		data_max[row][column] = MathUtil.max ( data_max[row][column], day_value, missing );
		data_min[row][column] = MathUtil.min ( data_min[row][column], day_value, missing );
		// Add the daily value to the month.  Below will check to see if
		// it is time to process the month...
		if ( !ts.isDataMissing(day_value) ) {
			if ( ts.isDataMissing(month_total) ) {
				month_total = day_value;
			}
			else {
			    month_total += day_value;
			}
			++nvalues_in_month;
		}
		// Reset daily total and count...
		day_total = missing;
		nvalues_in_day = 0;
		day_prev = day;
		// Now check to see whether the month should be processed...
		month = date.getMonth();
		need_to_process_month = false;
		if ( data_interval_base == TimeInterval.DAY ) {
			if ( (day == TimeUtil.numDaysInMonth(month,date.getYear()))){
				// Daily interval so month is complete when last day of the month is encountered...
				need_to_process_month = true;
				// Use the current month...
				nvalues_in_month_needed = TimeUtil.numDaysInMonth( month, date.getYear() );
			}
		}
		else if ( month != month_prev ) {
			// First interval in next month, which is still considered part of the previous month because
			// intervals are dated at the end of the interval (assuming no instantaneous data)...
			need_to_process_month = true;
			// Need to use the previous month...
			if ( month_prev == 12 ) {
				nvalues_in_month_needed = TimeUtil.numDaysInMonth( month_prev, (date.getYear() - 1) );
			}
			else {
			    nvalues_in_month_needed = TimeUtil.numDaysInMonth( month_prev, date.getYear() );
			}
		}
		if ( !need_to_process_month ) {
			// Don't yet need to add the monthly value to the array (still accumulating days)...
			month_prev = month;
			continue;
		}
		// Add the monthly values to the arrays...
		// The number of values needed in the month is the days since
		// the data are always accumulated to days...
		if ( nvalues_in_month != nvalues_in_month_needed ) {
			// Don't have the right number of values so set to missing...
			month_value = missing;
		}
		else {	// Compute the month value...
			if ( day_is_average ) {
				if ( !ts.isDataMissing(month_total) ) {
					if ( nvalues_in_month > 0 ) {
						month_value = month_total/(double)nvalues_in_month;
					}
					else {
					    month_value = missing;
					}
				}
			}
			else {
			    month_value = month_total;
			}
		}
/*
		Message.printStatus ( 1, routine,
		"SAMX Setting monthly value " + month_value + " at row:" +
		row + " column:" + column + " nvalues:" + nvalues_in_month +
		" nvalues_needed:" + nvalues_in_month_needed);
*/
		data[row][column] = month_value;
		// Reset the monthly values...
		nvalues_in_month = 0;
		month_total = missing;
		month_prev = month;
		// Print out the data value and total/ave, if necessary...
		if ( column == 0 ) {
			// Allocate a new buffer and print the year...
			buffer = new StringBuffer();
			buffer.append ( StringUtil.formatString( (date.getYear() + year_offset), "%04d") + " " );
			non_missing_in_row = 0;
		}
		// Do not use an else here because if we are on column 0 we still want to print the data value.
		// Print the monthly value...
		if ( ts.isDataMissing(month_value) ) {
			buffer.append ( "    NC    " );
			data[row][column] = missing;
		}
		else {
		    buffer.append ( StringUtil.formatString(
			month_value, data_format) + " " );
			if ( ts.isDataMissing(year_total) ) {
				year_total = 0.0;
			}
			year_total += month_value;
			++non_missing_in_row;
		}
		if ( column == 11 ) {
			// Have processed the last month in the year so process the total or average.  We have been
			// adding to the total, so divide by the number of non-missing for the year if averaging...
			// Now reset the year-value to zero...
			if ( ts.isDataMissing(year_total) || (non_missing_in_row != 12) ) {
				buffer.append ( "    NC    " );
				data[row][12] = missing;
			}
			else {
			    if ( year_column.equals("Total") ) {
					buffer.append ( StringUtil.formatString( year_total, data_format) );
					data[row][12] = year_total;
				}
				else {
				    buffer.append ( StringUtil.formatString( year_total/(double)non_missing_in_row, data_format) );
					data[row][12] = year_total/(double)non_missing_in_row;
				}
			}
			// Add the row...
			strings.add(buffer.toString() );
			column = -1;	// Incremented at end of loop.
			year_total = missing;
			++row;
		}
		++column;
	}
	strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
	// Now need to do the statistics.  Loop through each column...
	if ( !day_is_average ) {
		// Daily values are totals so include a total at the bottom also...
		strings = StringUtil.addListToStringList (strings,
		createMonthSummaryStats(ts,data,num_years,"Tot ", data_format));
	}
	strings = StringUtil.addListToStringList (strings,
		createMonthSummaryStats(ts,data,num_years,"Min ", data_format));
	strings = StringUtil.addListToStringList (strings,
		createMonthSummaryStats(ts,data,num_years,"Max ", data_format));
	strings = StringUtil.addListToStringList (strings,
		createMonthSummaryStats(ts,data,num_years,"Mean", data_format));
	strings = StringUtil.addListToStringList (strings,
		createMonthSummaryStats(ts,data,num_years,"SDev", data_format));
	strings = StringUtil.addListToStringList (strings,
		createMonthSummaryStats(ts,data_max,num_years,"MMxD", data_format));
	strings = StringUtil.addListToStringList (strings,
		createMonthSummaryStats(ts,data_min,num_years,"MMnD", data_format));

	// Now do the notes...

	strings.add ( "" );
	strings.add ( "Notes:" );
	if ( calendar == YearType.WATER ) {
		strings.add ( "  Years shown are water years." );
		strings.add ( "  A water year spans Oct of the previous calendar year to Sep of the current calendar year (all within the indicated water year)." );
	}
	else if ( calendar == YearType.NOV_TO_OCT ) {
		strings.add ( "  Years shown span Nov of the previous calendar year to Oct of the current calendar year." );
	}
	else {
		strings.add ( "  Years shown are calendar years." );
	}
	strings.add ( "  Annual values and statistics are computed only on non-missing data." );
	strings.add ( "  NC indicates that a value is not computed because of missing data or the data value itself is missing." );
	strings.add ( "  Statistics are for values shown in the main table except:" );
	strings.add ( "    MMxD are the means of the maximum daily values in a month." );
	strings.add ( "    MMnD are the means of the minimum daily values in a month." );
	return strings;
}

/**
Format the output statistics row given for the month summary report for the data array.
@param ts Time series that is being processed.
@param data Data array to process.
@param num_years Number of years of data.
@param label Label for the statistics row.
@param data_format Format like "%9.1f" to format values.
*/
private static List<String> createMonthSummaryStats ( TS ts, double[][] data, int num_years, String label, String data_format )
{	List<String> strings = new ArrayList<String>();
	double stat;
	StringBuffer buffer = null;
	double[] array = new double[num_years];
	int column, row;

	for ( column = 0; column < 13; column++ ) {
		if ( column == 0 ) {
			buffer = new StringBuffer();
			buffer.append ( label );
		}
		// Extract the non-missing values...
		int num_not_missing = 0;
		for ( row = 0; row < num_years; row++ ) {
			if ( !ts.isDataMissing(data[row][column])){
				++num_not_missing;
			}
		}
		if ( num_not_missing > 0 ) {
			// Transfer to an array...
			array = new double[num_not_missing];
			num_not_missing = 0;
			for ( row = 0; row < num_years; row++ ){
				if ( !ts.isDataMissing( data[row][column]) ) {
					array[num_not_missing] = data[row][column];
					++num_not_missing;
				}
			}
			stat = 0.0;
			try {
			    if ( label.startsWith("Min") ) {
					stat = MathUtil.min ( array );
				}
				else if ( label.startsWith("Max") ) {
					stat = MathUtil.max ( array );
				}
				else if ( label.startsWith("Mean") || label.startsWith("MM") ) {
					stat = MathUtil.mean ( array );
				}
				else if ( label.startsWith("SDev") ) {
					stat = MathUtil.standardDeviation ( array );
				}
				else if ( label.startsWith("Tot") ) {
					stat = MathUtil.sum ( array );
				}
			}
			catch ( Exception e ) {
			}
			buffer.append ( StringUtil.formatString(stat," "+ data_format));
		}
		else {
		    buffer.append ( "     NC   " );
		}
	}
	strings.add( buffer.toString() );
	strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );

	return strings;
}

/**
Create a period of record time series where the time series value is the
specified value if the original data for the data are not-missing, and the
missing data value if the data are missing.  The header information for the
old time series is copied.  The resulting time series can be used for graphing.
@param ts Time series for which to create the period of record time series.
@param value Value to fill the period of record time series.
@exception RTi.TS.TSException if there is a problem creating and filling the new time series.
*/
public static TS createPeriodOfRecordTS ( TS ts, double value )
throws TSException
{	String message, routine = "TSUtil.createPeriodOfRecordTS";
	TS newts = null;

	if ( ts == null ) {
		message = "Input time series is null!";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Get a new time series of the proper type...

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.MONTH ) {
		newts = new MonthTS ();
		newts.copyHeader ( ts );
		newts.setDate1 ( ts.getDate1() );
		newts.setDate2 ( ts.getDate2() );
		newts.allocateDataSpace();
	}
	else {
	    message = "Only monthly time series POR time series is implemented!";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Now loop and fill the time series...

	DateTime date = new DateTime ( ts.getDate1() );
	DateTime end = new DateTime ( ts.getDate2() );
		
	for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
		if ( !ts.isDataMissing(ts.getDataValue(date) ) ) {
			// Data is not missing so reset to the indicator value...
			newts.setDataValue(date,value);
		}
	}

	// Add to the genesis...

	newts.addToGenesis ( "Created POR TS from original data." );
	return newts;
}

/**
Disaggregate a time series by converting from a longer interval to a shorter
interval.  Currently this is enabled only to convert a 1day (NOT 24Hour) time
series to 6hour.  The day's values are transferred to the 6hour time series
starting for interval-ending 06, 12, 18, and 00 hours.
@return a new time series that is the disaggregated result.
@param ts Time series to disaggregate.
@param method Method to disaggregate.  If "Ormsbee", use the procedure given
by TVA.  If "Transfer", do a simple transfer of values (currently not implemented).
@param new_datatype If "" or "*", transfer the datatype from the original
time series.  Otherwise, use the new data type in the disaggregated time series (including the identifier).
@param new_units If "" or "*", transfer the units from the original
time series.  Otherwise, use the new units in the disaggregated time series.
@param req_interval_base Interval base to disaggregate to (as per TimeInterval class).
@param req_interval_mult Interval multiplier to disaggregate to (as per TimeInterval class).
@param createData if true, then the data space will be allocated and computed; if false, only the main time
series metadata will be created (useful for building workflows without full run)
@exception Exception if an error occurs (e.g., improper disaggregation parameters).
*/
public static TS disaggregate (	TS ts, String method, String new_datatype,
	String new_units, int req_interval_base, int req_interval_mult, boolean createData )
throws Exception
{	String routine = "TSUtil.disaggregate";
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
		throw new Exception ( "Cannot disaggregate irregular time series" );
	}
	if ( method.equalsIgnoreCase("Ormsbee") ) {
		if ( !((ts.getDataIntervalBase() == TimeInterval.DAY) && (ts.getDataIntervalMult() == 1)) ) {
			Message.printWarning ( 2, routine, "Cannot disaggregate other than 1Day time series." );
			throw new Exception ( "Cannot disaggregate other than 1Day time series." );
		}

		// Create a 6-hour time series using as much header information
		// from the original time series as possible....

		TS newts = new HourTS ();
		newts.copyHeader ( ts );
		newts.setDataInterval ( TimeInterval.HOUR, 6 );
		TSIdent tsident = newts.getIdentifier();
		tsident.setInterval ( "6Hour" );
		newts.setIdentifier ( tsident );
		// Set the time series properties...
		if ( (new_units != null) && !new_units.equals("") && !new_units.equals("*") ) {
			newts.setDataUnits ( new_units );
		}
		if ( (new_datatype != null) && !new_datatype.equals("") && !new_datatype.equals("*") ) {
			newts.setDataUnits ( new_datatype );
		}

		// Set the dates.  Going from daily data will result in a start
		// on hour 6 (interval-ending value) to hour 0 of the day after the last)...
	
		DateTime newdate1 = new DateTime ( ts.getDate1() );
		newdate1.setPrecision ( DateTime.PRECISION_HOUR );
		newdate1.setHour ( 6 );
		newts.setDate1 ( newdate1 );
		DateTime newdate2 = new DateTime ( ts.getDate2() );
		newdate2.setPrecision ( DateTime.PRECISION_HOUR );
		newdate2.setHour ( 18 ); // Do so DateTime cascades as necessary
		newdate2.addHour ( 6 );
		newts.setDate2 ( newdate2 );
		if ( !createData ) {
		    // Return the time series without doing the data work
		    return newts;
		}
		newts.allocateDataSpace ();

		// Now transfer the values...

		double new_value = 0.0;
		int nprocessed = 0;
		DateTime end = new DateTime ( ts.getDate2() );
		DateTime date = new DateTime ( ts.getDate1() );
		DateTime newdate = new DateTime ( newdate1 );
		double missing = newts.getMissing();
		int j = 0;
      	double [] f = new double[3];
		double tx, vx, wt, wt1, tol, t, abs1, abs2;
		boolean iflag1, iflag2;

		// Iterate through the original time series, creating 4 6-hour values in the new time series...

		for ( ; date.lessThanOrEqualTo ( end ); ++nprocessed, date.addInterval(interval_base, interval_mult) ) {
			// Get the initial values.  If necessary, offset the
			// date used in the iterator to get previous/next
			// values, taking care to reset to the "current" value
			// so that it can be incremented correctly at the end of the loop...
			if ( nprocessed == 0 ) {
				// First day value...
				f[0] = ts.getDataValue ( date );
				f[1] = f[0];
				// Second day value...
				date.addDay ( 1 );
				f[2] = ts.getDataValue ( date ); 
				date.addDay ( -1 );
			}
			else if ( date.equals(end) ) {
				// 2nd to last value...
				date.addDay ( -1 );
				f[0] = ts.getDataValue ( date );
				date.addDay ( 1 );
				// Last value...
				f[1] = ts.getDataValue ( date );
				f[2] = f[1];
			}
			else {
			    // Previous day...
				date.addDay ( -1 );
				f[0] = ts.getDataValue ( date );
				date.addDay ( 1 );
				// Current day...
				f[1] = ts.getDataValue ( date );
				// Next day...
				date.addDay ( 1 );
				f[2] = ts.getDataValue ( date );
				date.addDay ( -1 );
			}
			if ( Message.isDebugOn ) {
				Message.printStatus ( 1, routine, "Day data:  "+
				date + " f[0]=" + f[0] + " f[1]=" + f[1] + " f[2]=" + f[2] );
			}
			// If any of the input values are missing, set the result to missing...
			if ( ts.isDataMissing ( f[0] ) || ts.isDataMissing ( f[1] ) || ts.isDataMissing ( f[2] ) ) {
				for ( j = 0; j < 4; j++, newdate.addHour ( 6 ) ) {
					newts.setDataValue ( newdate, missing );
				}
				continue;
			}
			// If all of the input values are zero, set the
			// result to zero (any precision problems with this?)...
			if ( (f[0] == 0.0) && (f[1] == 0.0) && (f[2] == 0.0) ) {
				for ( j = 0; j < 4; j++, newdate.addHour ( 6 ) ) {
					newts.setDataValue ( newdate, 0.0 );
				}
				continue;
			}
			tol = 0.00001;
			iflag1 = false;
			iflag2 = false;
			if ( (f[0] >= f[1]) && (f[1] >= f[2]) ) {
				iflag1 = true;
			}
			if ( (f[2] >= f[1]) && (f[1] >= f[0]) ) {
				iflag2 = true;
			}
			abs1 = Math.abs (f[0] - f[1]);
			abs2 = Math.abs (f[1] - f[2]);
			if ( (abs1 < tol) && (abs2 < tol) ) {
				tx = 24.0;
			}
			else if ( iflag1 || iflag2 ) {
				tx = 24.0*(f[0] - f[1])/(f[0] - f[2]);
			}
			else {
			    tx = 24.0*(f[0] - f[1])/(f[0] - 2.0*f[1] +f[2]);
			}
			vx = 12.0 * (f[1] + f[2]) - 0.5*tx*(f[2] - f[0]);
			wt1 = 0.0;
			if ( Message.isDebugOn ) {
				Message.printStatus ( 1, routine, "abs1=" + abs1 + " abs2=" + abs2 + " tx=" + tx + " vx=" + vx );
			}
			for ( j = 1; j <= 4; j++, newdate.addHour ( 6 ) ) {
				t = 6.0*j;
				if ( t <= tx ) {
					wt = f[0]*t/vx - (f[0]-f[1])*t*t/(2.0*vx*tx);
				}
				else {
				    wt = (f[1]+f[0])*tx/(2.0*vx) + f[1]*(t-tx)/vx - (f[1]-f[2])*(t-tx)*(t-tx)/(2.0*vx*(24.0-tx));
				}
            	new_value = 4.0*(f[1] * (wt - wt1));
				newts.setDataValue ( newdate, new_value );
            	wt1 = wt;
			}
		}
		newts.addToGenesis ( "disaggregate: Ormsbee method used to create 6Hour time series"+
		" from 1Day time series " + ts.getIdentifierString() + ", " );
		newts.addToGenesis ( "disaggregate:   " + ts.getDescription() );
		newts.addToGenesis ( "disaggregate: First day " + ts.getDate1()
		+ " applied as interval-ending hours 06, 12, 18, and 00" );
		return newts;
	}
	else if ( method.equalsIgnoreCase("SameValue") ) {
		TS newts = null;
		TSIdent tsident = null;
		if ( (interval_base == TimeInterval.YEAR) && (interval_mult == 1) &&
			(req_interval_base == TimeInterval.MONTH) && (req_interval_mult == 1) ) {
			// Year -> Month

			// Create a 1Month time series using as much header
			// information from the original time series as possible....

			newts = new MonthTS ();
			newts.copyHeader ( ts );
			newts.setDataInterval ( TimeInterval.MONTH, 1 );
			tsident = newts.getIdentifier();
			tsident.setInterval ( "Month" );

			// Set the dates.
	
			DateTime newdate1 = new DateTime ( ts.getDate1() );
			newdate1.setPrecision ( DateTime.PRECISION_MONTH );
			newdate1.setMonth ( 1 );
			newts.setDate1 ( newdate1 );
			DateTime newdate2 = new DateTime ( ts.getDate2() );
			newdate2.setPrecision ( DateTime.PRECISION_MONTH );
			newdate2.setMonth ( 12 );
			newts.setDate2 ( newdate2 );
	        if ( !createData ) {
	            // Return the time series without doing the data work
	            return newts;
	        }
			newts.allocateDataSpace ();

			// Now transfer the values...

			double new_value = 0.0;
			DateTime date = new DateTime ( newdate1 );
			date.setPrecision(DateTime.PRECISION_YEAR);
			DateTime newdate = new DateTime ( newdate1 );

			// Iterate through the original time series....

			for ( ; newdate.lessThanOrEqualTo ( newdate2 ); newdate.addInterval(req_interval_base, req_interval_mult) ) {
				if ( newdate.getMonth() == 1 ) {
					// New year so get a new value...
					date.setYear ( newdate.getYear() );
					new_value = ts.getDataValue ( date );
				}
				newts.setDataValue ( newdate, new_value );
			}
		}
		else if ((interval_base == TimeInterval.MONTH) && (interval_mult == 1) &&
			(req_interval_base == TimeInterval.DAY) && (req_interval_mult == 1) ) {
			// Month -> Day

			// Create a 1Day time series using as much header
			// information from the original time series as possible....

			newts = new DayTS ();
			newts.copyHeader ( ts );
			newts.setDataInterval ( TimeInterval.DAY, 1 );
			tsident = newts.getIdentifier();
			tsident.setInterval ( "Day" );

			// Set the dates.
	
			DateTime newdate1 = new DateTime ( ts.getDate1() );
			newdate1.setPrecision ( DateTime.PRECISION_DAY );
			newdate1.setDay ( 1 );
			newts.setDate1 ( newdate1 );
			DateTime newdate2 = new DateTime ( ts.getDate2() );
			newdate2.setPrecision ( DateTime.PRECISION_DAY );
			newdate2.setDay ( TimeUtil.numDaysInMonth( newdate2.getMonth(), newdate2.getYear()) );
			newts.setDate2 ( newdate2 );
            if ( !createData ) {
                // Return the time series without doing the data work
                return newts;
            }
			newts.allocateDataSpace ();

			// Now transfer the values...

			double new_value = 0.0;
			DateTime date = new DateTime ( newdate1 );
			date.setPrecision(DateTime.PRECISION_MONTH);
			DateTime newdate = new DateTime ( newdate1 );

			// Iterate through the original time series....

			for ( ; newdate.lessThanOrEqualTo ( newdate2 ); newdate.addInterval(req_interval_base, req_interval_mult) ) {
				if ( newdate.getDay() == 1 ) {
					// New month so get a new value...
					date.setYear ( newdate.getYear() );
					date.setMonth ( newdate.getMonth() );
					new_value = ts.getDataValue ( date );
				}
				newts.setDataValue ( newdate, new_value );
			}
		}
		else if ((interval_base == TimeInterval.DAY) &&
			(interval_mult == 1) &&
			(req_interval_base == TimeInterval.HOUR) ) {
			// Day -> NHour

			// Create an hourly time series using as much header
			// information from the original time series as possible....

			newts = new HourTS ();
			newts.copyHeader ( ts );
			newts.setDataInterval ( TimeInterval.HOUR, req_interval_mult );
			tsident = newts.getIdentifier();
			tsident.setInterval ( "" + req_interval_mult + "Hour" );

			// Set the dates.
	
			// Start date will be shifted to hour zero of the next day if hourly data...
			DateTime newdate1 = new DateTime ( ts.getDate1() );
			newdate1.setPrecision ( DateTime.PRECISION_HOUR );
			if ( req_interval_mult == 24 ) {
				newdate1.setHour ( 23 );
				newdate1.addHour ( 1 );
			}
			else {
			    newdate1.setHour ( req_interval_mult );
			}
			newts.setDate1 ( newdate1 );
			// End date is always hour zero of the next day...
			DateTime newdate2 = new DateTime ( ts.getDate2() );
			newdate2.setPrecision ( DateTime.PRECISION_HOUR );
			newdate2.setHour ( 23 );
			newdate2.addHour ( 1 );
			newts.setDate2 ( newdate2 );
            if ( !createData ) {
                // Return the time series without doing the data work
                return newts;
            }
			newts.allocateDataSpace ();

			// Now transfer the values...

			double new_value = ts.getMissing();
			DateTime date = new DateTime ( newdate1 );
			date.setPrecision(DateTime.PRECISION_DAY);
			// Decrement the day by one...
			date.addDay ( -1 );
			DateTime newdate = new DateTime ( newdate1 );

			// Iterate through the original time series....

			for ( ; newdate.lessThanOrEqualTo ( newdate2 ); newdate.addInterval(req_interval_base, req_interval_mult) ) {
				if ( req_interval_mult == 24 ) {
					// New hour zero value is the previous day's value.  The date was initially
					// set and can be incremented in step with the newdate increment of 24hours
					// since they are equivalent...
					new_value = ts.getDataValue ( date );
					newts.setDataValue (newdate, new_value);
					date.addDay ( 1 );
				}
				else {
				    if ( newdate.getHour() == req_interval_mult ) {
						// The zero hour will use the previous day's value but
						// others need to use the current day's value...
						date.addDay ( 1 );
						new_value = ts.getDataValue ( date );
					}
					newts.setDataValue (newdate, new_value);
				}
			}
		}
		else if ((interval_base == TimeInterval.HOUR) && (interval_mult == 1) &&
			(req_interval_base == TimeInterval.MINUTE) ) {
			// Hour -> Nminute

			// Create an minute time series using as much header
			// information from the original time series as possible....

			newts = new MinuteTS ();
			newts.copyHeader ( ts );
			newts.setDataInterval ( TimeInterval.MINUTE, req_interval_mult );
			tsident = newts.getIdentifier();
			tsident.setInterval ( "" + req_interval_mult + "Minute" );

			// Set the dates.
	
			// Start date will be shifted to hour zero of the next
			// day if hourly data...
			DateTime newdate1 = new DateTime ( ts.getDate1() );
			newdate1.setPrecision ( DateTime.PRECISION_MINUTE );
			if ( req_interval_mult == 60 ) {
				newdate1.setMinute ( 59 );
				newdate1.addMinute ( 1 );
			}
			else {
			    newdate1.setMinute ( req_interval_mult );
			}
			newts.setDate1 ( newdate1 );
			// End date is always hour zero of the next day...
			DateTime newdate2 = new DateTime ( ts.getDate2() );
			newdate2.setPrecision ( DateTime.PRECISION_MINUTE );
			newdate2.setMinute ( 59 );
			newdate2.addMinute ( 1 );
			newts.setDate2 ( newdate2 );
            if ( !createData ) {
                // Return the time series without doing the data work
                return newts;
            }
			newts.allocateDataSpace ();

			// Now transfer the values...

			double new_value = ts.getMissing();
			DateTime date = new DateTime ( newdate1 );
			date.setPrecision(DateTime.PRECISION_HOUR);
			// Decrement the hour by one...
			date.addHour ( -1 );
			DateTime newdate = new DateTime ( newdate1 );

			// Iterate through the original time series....

			for ( ; newdate.lessThanOrEqualTo ( newdate2 ); newdate.addInterval(req_interval_base, req_interval_mult) ) {
				if ( req_interval_mult == 60 ) {
					// New minute zero value is the previous hour's value.  The date was initially
					// set and can be incremented in step with the newdate increment of 60min
					// since they are equivalent...
					new_value = ts.getDataValue ( date );
					newts.setDataValue (newdate, new_value);
					date.addHour ( 1 );
				}
				else {
				    if ( newdate.getMinute() == req_interval_mult ) {
						// The zero minute will use the previous hour's value but
						// others need to use the current day's value...
						date.addHour ( 1 );
						new_value = ts.getDataValue ( date );
					}
					newts.setDataValue (newdate, new_value);
				}
			}
		}
		else {
		    throw new Exception ( "disaggregate() intervals are not supported." );
		}
		if ( newts != null ) {
			// General actions...
			newts.setIdentifier ( tsident );
			// Set the time series properties...
			if ( (new_units != null) && !new_units.equals("") && !new_units.equals("*") ) {
				newts.setDataUnits ( new_units );
			}
			if ( (new_datatype != null) && !new_datatype.equals("") && !new_datatype.equals("*") ) {
				newts.setDataUnits ( new_datatype );
			}
			newts.addToGenesis ( 
			"disaggregate: SameValue method used to create " + tsident.getInterval() + " time series from " +
			ts.getIdentifier().getInterval()+ " time series " + ts.getIdentifierString() + "," );
			newts.addToGenesis ( "disaggregate:   " + ts.getDescription() );
		}
		return newts;
	}
	throw new Exception ( "Disaggregation method \"" + method + "\" is not recognized for disaggregate()." );
}

/**
Divide an entire time series by another time series.  Only regular time series
can be multiplied.  If either value is missing, then the result is set to
missing.  If the denominator is zero, the result is set to missing.
@param ts Time series to multiply.
@param tsd Time series to divide by.
@exception Exception if there is an error processing.
*/
public static void divide ( TS ts, TS tsd )
throws Exception
{	divide ( ts, tsd, ts.getDate1(), ts.getDate2() );
}

/**
Divide3 a time series by another time series for the indicated period.  Only
regular time series can be divided.  If either value is missing, then the
result is set to missing.  If the denominator is zero, the result is set to
missing.  The units are set to UNITS/UNITS.
@param ts Time series to divide.
@param tsd Time series to divide by.
@param start_date Date to start divide.
@param end_date Date to stop divide.
@exception Exception if there is an error processing.
*/
public static void divide (	TS ts, TS tsd, DateTime start_date, DateTime end_date )
throws Exception
{	if ( ts == null ) {
		return;
	}
	if ( tsd == null ) {
		throw new Exception ( "Time series to divide by is null." );
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();
	valid_dates = null;

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( (interval_base != tsd.getDataIntervalBase()) || (interval_mult != tsd.getDataIntervalMult()) ) {
		throw new Exception ( "Cannot divide data with different intervals (" +
		ts.getIdentifierString() + " and " + tsd.getIdentifierString() + ")." );
	}
	if ( interval_base == TimeInterval.IRREGULAR ) {
		throw new Exception ( "Cannot divide irregular interval data (" + ts.getIdentifierString() + ")" );
	}

	// Loop using addInterval...
	DateTime date = new DateTime ( start );
		
	double missing = ts.getMissing();
	double oldvalue = 0.0;
	double div = 0.0;
	for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
		oldvalue = ts.getDataValue(date);
		div = tsd.getDataValue(date);
		if ( ts.isDataMissing(oldvalue) || ts.isDataMissing(div) || (div == 0.0) ) {
			ts.setDataValue ( date, missing );
		}
		else {
		    ts.setDataValue ( date, oldvalue/div );
		}
	}

	// Fill in the genesis information...

	if ( tsd.getDataUnits().trim().length() > 0 ) {
	    ts.setDataUnits ( ts.getDataUnits() + "/" + tsd.getDataUnits() );
	}
	ts.addToGenesis ( "Divided " + start_date.toString() +
		" to " + end_date.toString() + " by " + tsd.getDescription() + "." );

	ts.setDescription ( ts.getDescription() + ", /" + tsd.getDescription() );
}

/**
Enforce limits on the entire time series.  Any time series values greater than
the maximum allowed values will be set to the maximum.
@return The total number of time steps changed.
@param ts Time series to check.
@param dates list of dates corresponding to the limiting values.  The
date/limit pairs are in effect from the date until the next date encountered.
@param max_values Data value to check.
@param props See the overloaded version for a description.
@exception RTi.TS.TSException if there is a problem with input.
*/
public static int enforceLimits ( TS ts, List<DateTime> dates, double [] max_values, PropList props )
throws TSException, Exception
{	String routine = "TSUtil.enforceLimits";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Else, use the overall start and end dates for filling...

	try {
		return enforceLimits ( ts, ts.getDate1(), ts.getDate2(), dates, max_values, props );
	}
	catch ( TSException e ) {
		throw e;
	}
}

/**
Enforce limits on the entire time series.  Any time series values greater than
the maximum allowed values will be set to the maximum.
@return The total number of time steps changed.
@param start_date Date to start the enforcement.
@param end_date Date to end the enforcement.
@param ts Time series to check.
@param limit_dates_given list of dates corresponding to the limiting values.  
The
date/limit pairs are in effect from the date until the next date encountered.
The dates and corresponding values are sorted with the earlier dates first.
It is advisable to always pass in an initial condition date (e.g. a date and
value that are sure to occur before the period of the analysis).  Otherwise,
if the first date is after the initial date in the period of record, the
limits will have no effect until that date is reached.  Also, this routine does
not handle units or month total conversions (e.g., it will not take a limits
value in ACFT and adjust according to the number of days in each month).
@param max_values_given Data value to check.
@param props Property list to control function, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>LimitsScaleFactor</b></td>
<td>
Use a value of "DaysInMonth" to scale the
incoming limits values by the number of days in the month.  This can be used
for example to specify an incoming base limit (e.g., a flow value converted
to volume) that is dependent on an additional conversion.
</td>
<td>No scaling will occur.
</td>
</tr>

<tr>
<td><b>SetFlag</b></td>
<td>
If specified, then any value that is reduced in order to meet the limit
restriction will be flagged with the given flag value.  A single character is
recommended.  The flag will be appended to existing flags.
</td>
<td>If not specified, flags will not be set.
</td>
</tr>

</table>
@exception RTi.TS.TSException if there is a problem with input.
*/
public static int enforceLimits ( TS ts, DateTime start_date, DateTime end_date, List<DateTime> limit_dates_given,
					double [] max_values_given, PropList props )
throws TSException, Exception
{	String  routine = "TSUtil.enforceLimits";
	String	message;
	double	value = 0.0;
	int	count_changed = 0, dl = 30;

	if ( limit_dates_given == null ) {
		message = "No dates have been specified for enforcement.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	if ( max_values_given == null ) {
		message = "No limiting values have been specified for enforcement.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	boolean scale_by_days = false;
	String SetFlag = null;
	boolean SetFlag_boolean = false;
	if ( props != null ) {
		String prop_value = props.getValue ( "LimitsScaleFactor" );
		if ( (prop_value != null) &&
			prop_value.equalsIgnoreCase("DaysInMonth") ) {
			scale_by_days = true;
		}
		SetFlag = props.getValue ( "SetFlag" );
		if ( (SetFlag != null) && (SetFlag.length() > 0) ) {
			SetFlag_boolean = true;
			// Make sure that the data flag is allocated.
			ts.allocateDataFlagSpace (
				null,	// Initial flag value
				true );	// Keep old flags if already allocated
		}
	}
	int num_limit_dates = limit_dates_given.size();
	int num_max_values = max_values_given.length;
	if ( num_limit_dates != num_max_values ) {
		message = "The number of dates (" + num_limit_dates +
		") and values (" + num_max_values + ") for limits does not agree";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Enforcing limits for \"" + ts.getIdentifierString() + "\"" );
	}

	// Sort the dates and corresponding values...  First convert DateTimes to doubles...

	double [] double_dates = new double [num_limit_dates];
	int [] sort_order = new int [num_limit_dates];
	DateTime limit_date = null;
	for ( int i = 0; i < num_limit_dates; i++ ) {
		limit_date = limit_dates_given.get(i);
		double_dates[i] = limit_date.toDouble();
	}

	// Now sort...

	MathUtil.sort ( double_dates, MathUtil.SORT_QUICK, MathUtil.SORT_ASCENDING, sort_order, true );

	// Now reset the dates again...

	List<DateTime> limit_dates = new ArrayList<DateTime>(num_limit_dates);
	double [] max_values = new double[num_limit_dates];
	if ( Message.isDebugOn ) {
		for ( int i = 0; i < num_limit_dates; i++ ) {
			limit_date = limit_dates_given.get(i);
			Message.printDebug ( dl, routine, "Before sort by dates, limits[" + i + "] = " + max_values_given[i] + " on " + limit_date );
		}
	}
	for ( int i = 0; i < num_limit_dates; i++ ) {
		limit_dates.add ( limit_dates_given.get(sort_order[i]) );
		max_values[i] = max_values_given[sort_order[i]];
	}
	if ( Message.isDebugOn ) {
		for ( int i = 0; i < num_limit_dates; i++ ) {
			limit_date = (DateTime)limit_dates.get(i);
			Message.printDebug ( dl, routine, "After sort by dates, limits[" + i + "] = " + max_values[i] + " on " + limit_date );
		}
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();

	int	limit_pos = 0;
	limit_date = limit_dates.get(limit_pos);
	DateTime next_limit_date = null;

	// Enhancement:  Need to pick limiting date that is nearest the starting date...

	if ( (limit_pos + 1) != num_limit_dates ) {
		next_limit_date = limit_dates.get(limit_pos +1);
	}
	else {
		next_limit_date = null;
	}
	double limit_value0 = max_values[limit_pos];
	double limit_value;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Starting with max " + limit_value0 + " at " + limit_date );
		Message.printDebug ( dl, routine, "Next limit date is " + next_limit_date );
	}

	TSData tsdata = new TSData();	// Single data point.
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return 0;
		}
		int nalltsdata = alltsdata.size();
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				if ( next_limit_date != null ) {
					while ( true ) {
						if ( next_limit_date == null ) {
							// As a result of the loop...
							break;
						}
    					if ( next_limit_date.lessThanOrEqualTo(date) ) {
    						// Need to update the dates...
    						++limit_pos;
    						limit_date = next_limit_date;
    						limit_value0 = max_values[limit_pos];
    						if ( (limit_pos+1) != num_limit_dates){
    							next_limit_date = (DateTime)limit_dates.get(limit_pos + 1 );
    						}
    						else {
    							next_limit_date = null;
    						}
    					}
    					else {
    						// Check again in the next iteration...
    						break;
    					}
					} // end while
				}
				// Check to see that the value is less than the max...
				if ( scale_by_days ) {
					limit_value = limit_value0*TimeUtil.numDaysInMonth( date.getMonth(), date.getYear() );
				}
				else {
				    limit_value = limit_value0;
				}
				if ( value > limit_value ) {
					// Reset to the maximum...
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Resetting " + value + " to limit value " + limit_value + " at " + date );
					}
					tsdata.setDataValue ( limit_value );
					if ( SetFlag_boolean ) {
						// Also set the data flag, // appending to the old value...
						tsdata.setDataFlag ( tsdata.getDataFlag().trim() + SetFlag );
					}
					++count_changed;
				}
			}
		}
	}
	else {
	    // Loop using addInterval...
		DateTime date = new DateTime ( start );
		
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			value = ts.getDataValue ( date );
			// Make sure that the limiting date is updated...
			if ( next_limit_date != null ) {
				while ( true ) {
					if ( next_limit_date == null ) {
						// As a result of the loop...
						break;
					}
					if ( next_limit_date.lessThanOrEqualTo(date) ) {
						// Need to update the dates...
						++limit_pos;
						limit_date = next_limit_date;
						limit_value0 = max_values[limit_pos];
						if ( (limit_pos+1) != num_limit_dates){
							next_limit_date = (DateTime)limit_dates.get(limit_pos + 1 );
						}
						else {
						    next_limit_date = null;
						}
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, routine, "Updating limits at " + date + " to " + limit_value0 );
							Message.printDebug ( dl, routine, "Next limits date is " + next_limit_date );
						}
					}
					else {
					    // Check again in the next iteration...
						break;
					}
				} // end while
			}
			// Check to see that the value is less than the max...
			if ( scale_by_days ) {
				limit_value = limit_value0*TimeUtil.numDaysInMonth( date.getMonth(), date.getYear() );
			}
			else {
			    limit_value = limit_value0;
			}
			if ( value > limit_value ) {
				// Reset to the maximum...
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Resetting " + value + " to limit value " + limit_value + " at " + date );
				}
				if ( SetFlag_boolean ) {
					// Set the data flag, appending to the old value...
					tsdata = ts.getDataPoint ( date, tsdata );
					ts.setDataValue ( date, limit_value, (tsdata.getDataFlag().trim()+ SetFlag), 1 );
				}
				else {
				    ts.setDataValue ( date, limit_value );
				}
				++count_changed;
			}
		}
	}

	// Fill in the genesis information...

	ts.addToGenesis ( "Enforced step-function limits "+start_date.toString() + " to " + end_date.toString() );

	return count_changed;
}

// FIXME SAM 2009-08-30 Remove this code when able - comment out to break app code and force refactor - regression
// parameters are no longer a PropList due to fragility and this could cause this method to not work
// if something was missed
// ----------------------------------------------------------------------------
// fill - generic method to fill missing data values
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// Notes:	(1)	This routine used to be fillMonthTS().  The routine
//			uses some redudant code from the other fill routines
//			but is meant to allow a series of fill methods to be
//			applied at each time step.  It remains to be seen if
//			this performs significantly better than sequentially
//			calling the full routines.
// ----------------------------------------------------------------------------
// History:
// 
// 02 May 1998	Steven A. Malers, RTi	Initial version based on demandts
//					and tstool diversion and reservoir
//					filling as well as legacy RTi code.
// 29 May 1998 	Catherine E. Nutting-Lane, RTi
//					added code for 
//					FILL_METHOD_LINEAR_REGRESS and
//					FILL_METHOD_LINEAR_REGRESS_12
// 21 Jun 1998	SAM, RTi		Add FillCarryForward.
// ----------------------------------------------------------------------------

/**
Fill the missing data values in a time series.  One or more fill methods can
be used, each of which will be evaluated at each time step.  <b>This function
will only reset missing data values and therefore cannot be used for
general time series computations.</b>  The FillConstant fill method should be
specified as one of the fill methods to guarantee that missing data are filled.
@return Filled time series.
@param ts Time series to fill.  The time series is modified and returned.
@param needed_ts Time series that are needed for filling (e.g., when regression
methods are used).  If not needed, specify null.
@param props Properties that specify fill methodology.  The following properties
are available.  <b>Note:  the FillMethod value controls whether most of the
other
properties are necessary (and is listed first, followed by an alphabetized list
of the other properties).</b>
<p>

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>FillMethod</b></td>
<td>
A list of fill methods to be applied to the time series, separated by commas
if multiple values are specified.  Valid fill methods include:<br>
"FillCarryForward" to fill by carrying forward the last know value,<br>
"FillConstant" to fill with a constant value,<br>
"FillHistDayAve" to fill with the historical monthly average,<br>
"FillHistMonthAve" to fill with the historical monthly average,<br>
"FillHistSeasonAve" to fill with the historical monthly average,<br>
"FillHistYearAve" to fill with the historical monthly average,<br>
"FillInitZero" to fill initial missing data with zero,<br>
"FillLookupTable" to fill with values from a lookup table,<br>
"FillRegressLinear" to fill using one linear regression relationship,<br>
"FillRegressLinear12" or "FillRegressLinearMonthly" to fill using monthly
linear regression relationships,<br>
"FillRegressLog" to fill using one log (base 10) regression relationship,<br>
"FillRegressLog12" or "FillRegressLogMonthly" to fill using monthly log (base
10) regression relationships<br>
</td>
<td>FillConstant, with a FillConstantValue of 0.0.
</td>
</tr>

<tr>
<td><b>EndDate</b></td>
<td>
Date to end filling,
with a format like
"YYYY-MM-DD HH:mm" (with as much precision as necessary for the time
series data interval).
</td>
<td>End date for time series (process entire time series).
</td>
</tr>

<tr>
<td><b>FillConstantValue</b></td>
<td>
Value to fill with when using the FillConstant fill method.
</td>
<td>0.0
</td>
</tr>

<tr>
<td><b>FillHistMonthAveValues</b></td>
<td>
Values to fill with when using the FillHistMonthAveValues fill method (e.g.,
<code>1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,11.0,12.0</code), where the
first value corresponds to January, the second to Frebruary, etc.
If 12 values are not specified, then this information is ingored and the
averages are computed.
This information should be supplied when using FillHistMonthAve to guarantee
that the values are from the raw data (otherwise, the values are computed from
the incoming time series before ANY filling occurs).
</td>
<td>The averages will be computed from the time series before ANY filling
occurs.
</td>
</tr>

<tr>
<td><b>StartDate</b></td>
<td>
Date to start filling,
with a format like
"YYYY-MM-DD HH:mm" (with as much precision as necessary for the time
series data interval).
</td>
<td>Start date for time series (process entire time series).
</td>
</tr>

</table>

@exception RTi.TS.TSException If there is a problem filling the time series
(generally an input problem).
@deprecated use specific fill methods because this one is getting too complicated to handle errors.
*/
/*
public static TS fill (	TS ts, List needed_ts, PropList props )
// At some point need to pass in a TSLookupTable also?? - talking to Dan Weiler
throws TSException
{	String message, routine = "TSUtil.fill";
	int dl = 20, i, j;
	TSRegression rd=null, rdMonthly=null;
	TS simTS=null;
	double [] fill_hist_month_ave_values = new double[12];
	int [] fill_hist_month_ave_nvalues = new int[12];
	int month_pos;
	double value;

	// Make sure that there is a valid time series...

	if ( ts == null ) {
		message = "Null time series.  Cannot fill";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	double missing = ts.getMissing();

	// Get valid dates because the ones passed in may have been null...

	DateTime start = ts.getDate1();
	DateTime end = ts.getDate2();
	DateTime date = null;

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();

	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Don't support filling this type of data.  Can only fill regular interval data.
		message = "Can only fill regular-interval data.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Make sure we have a valid PropList so we don't have to constantly check for null...

	PropList proplist = PropList.getValidPropList ( props, "TSUtil.fill" );
	String prop_value = null;

	// See if the start and end dates have been specified...

	prop_value = proplist.getValue ( "StartDate" );
	if ( prop_value != null ) {
		DateTime d = null;
		try {
		    d = DateTime.parse ( prop_value );
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, "StartDate is invalid:  \"" + prop_value + "\"" );
			d = null;
		}
		if ( d != null ) {
			start = d;
		}
	}
	prop_value = proplist.getValue ( "EndDate" );
	if ( prop_value != null ) {
		DateTime d = null;
		try {
		    d = DateTime.parse ( prop_value );
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, "EndDate is invalid:  \"" + prop_value + "\"" );
			d = null;
		}
		if ( d != null ) {
			end = d;
		}
	}

	// Get the fill methods...

	String fill_methods_string = proplist.getValue ( "FillMethods" );
	double fill_constant_value = 0.0;
	if ( fill_methods_string == null ) {
		// Default is fill constant...
		fill_methods_string="FillConstant";
		fill_constant_value = 0.0;
	}

	// Now split out the fill methods into a vector of strings...

	List fill_methods_strings = StringUtil.breakStringList (
		fill_methods_string, ",", StringUtil.DELIM_SKIP_BLANKS );
	if ( fill_methods_strings == null ) {
		message = "Unable to get fill methods list";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	// Save the size of the list because we use over and over and set
	// an integer array of the same size.  We will later reset the number
	// of methods (some may not be implemented or are incorrect)...
	int nfill_methods = fill_methods_strings.size();
	int fill_methods[] = new int[nfill_methods];
	// Loop through the list and make sure the methods are valid and
	// then assign an integer method so that we do not have to do a bunch
	// of string compares later.  This improves performance.  Also, as each
	// fill method is detected, try to get the fill data for the fill method...
	String fill_method;
	int nfill_methods0 = nfill_methods;
	nfill_methods = 0;
	for ( i = 0; i < nfill_methods0; i++ ) {
		fill_method = (String)fill_methods_strings.get(i);
		if ( fill_method.equalsIgnoreCase("FillCarryForward") ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillCarryForward fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_CARRY_FORWARD;
			++nfill_methods;
		}
		else if ( fill_method.equalsIgnoreCase("FillConstant") ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillConstant fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_CONSTANT;
			++nfill_methods;
			// Need the value to fill with...
			prop_value = proplist.getValue("FillConstantValue");
			if ( prop_value == null ) {
				// Default is zero...
				fill_constant_value = 0.0;
			}
			else {
			    fill_constant_value = StringUtil.atod ( prop_value );
			}
			String FillFlag = props.getValue("FillFlag");
			if( FillFlag != null && FillFlag.equalsIgnoreCase("Auto") || FillFlag.length() == 1) {
				try {
					ts.allocateDataFlagSpace (
						FillFlag.length(),	// Max length of flag
						null,	// Initial flag value
						true );	// keep old flags
				}
				catch (Exception e) {
					Message.printWarning(2, routine, "Couldn't allocate data space for fill flags.\n");
					Message.printWarning(2, routine, e );
				}	
			}
		}
		else if ( fill_method.equalsIgnoreCase("FillHistDayAve") ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillHistDayAve fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_HIST_DAY_AVE;
			Message.printWarning ( 2, routine, "FillHistDayAve fill method not implemented" );
			++nfill_methods;
		}
		else if ( fill_method.equalsIgnoreCase("FillHistMonthAve") ) {
			// Need to compute the monthly averages for the time
			// series before any filling occurs.  At some point, maybe use getMonthTotals for this...
			boolean values_set = false;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillHistMonthAve fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_HIST_MONTH_AVE;
			// If the FillHistMonthAveValues property is set, use that information...
			prop_value =proplist.getValue("FillHistMonthAveValues");
			if ( prop_value != null ) {
				// Parse into a list.
				List values = StringUtil.breakStringList ( prop_value, ",", 0 );
				if ( values != null ) {
					if ( values.size() == 12 ) {
						for ( j = 0; j < 12; j++ ) {
							fill_hist_month_ave_values[j] = StringUtil.atod((String)values.get(j) );
						}
						values_set = true;
					}
				}
			}
			if ( !values_set ) {
				// Need to compute the averages here...
				for ( j = 0; j < 12; j++ ) {
					fill_hist_month_ave_values[j] = missing;
					fill_hist_month_ave_nvalues[j] = 0;
				}
				date = new DateTime(start, DateTime.DATE_FAST );
				for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
					// Get the data value...
					value = ts.getDataValue ( date );
					if ( ts.isDataMissing(value) ) {
						// Value is missing so no need to process...
						continue;
					}
					// Not missing...
					month_pos = date.getMonth() - 1;
					if ( ts.isDataMissing(fill_hist_month_ave_values[month_pos])){
						// Set the value...
						fill_hist_month_ave_values[month_pos] = value;
					}
					else {
					    // Add to value...
						fill_hist_month_ave_values[month_pos] += value;
					}
					++fill_hist_month_ave_nvalues[month_pos];
				}
				// Now compute the averages...
				for ( j = 0; j < 12; j++ ) {
					if ( !ts.isDataMissing(fill_hist_month_ave_values[j])){
						fill_hist_month_ave_values[j] /= fill_hist_month_ave_nvalues[j];
					}
				}
			}
			if ( Message.isDebugOn ) {
				for ( j = 0; j < 12; j++ ) {
					Message.printDebug ( dl, routine, "MonthAve[" + j + "]=" + fill_hist_month_ave_values[j] );
				}
			}
			++nfill_methods;
		}
		else if ( fill_method.equalsIgnoreCase("FillHistSeasonAve") ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillHistSeasonAve fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_HIST_SEASON_AVE;
			Message.printWarning ( 2, routine, "FillHistSeasonAve fill method not implemented" );
			++nfill_methods;
		}
		else if ( fill_method.equalsIgnoreCase("FillHistYearAve") ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillHistYearAve fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_HIST_YEAR_AVE;
			Message.printWarning ( 2, routine, "FillHistYearAve fill method not implemented" );
			++nfill_methods;
		}
		else if ( fill_method.equalsIgnoreCase("FillInitZero") ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillInitZero fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_INIT_ZERO;
			++nfill_methods;
		}
		else if ( fill_method.equalsIgnoreCase("FillLookupTable") ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillLookupTable fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_LOOKUP_TABLE;
			Message.printWarning ( 2, routine, "FillLookupTable fill method not implemented" );
			++nfill_methods;
		}
		else if ( fill_method.equalsIgnoreCase("FillRegressLinear") ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillRegressLinear fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_REGRESS_LINEAR;
			++nfill_methods;
			if ( Message.isDebugOn ) {
				Message.printDebug ( 10, routine, "In fill - using FillRegressLinear." );
			}
			simTS = (TS)needed_ts.get(0);
			try {
			    rd = new TSRegression ( simTS, ts );
			}
			catch ( Exception e ) {
			}
		}
		else if ( fill_method.equalsIgnoreCase("FillRegressLinear12") ||
			fill_method.equalsIgnoreCase( "FillRegressLinearMonthly")){
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillRegressLinear12 fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_REGRESS_LINEAR_12;
			++nfill_methods;
			simTS = (TS)needed_ts.get(0);
			try {
				PropList regprops = new PropList("regression");
				regprops.set( "NumberOfEquations=MonthlyEquations");
				rdMonthly = new TSRegression ( simTS, ts, regprops );
			}
			catch ( Exception e ) {
				rdMonthly = null;
			}
		}
		else if ( fill_method.equalsIgnoreCase("FillRegressLog") ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillRegressLog fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_REGRESS_LOG;
			PropList tmpProps = new PropList("specialRegress");
			tmpProps.set ( "Transformation", "Log" );
			try {
			    rd = new TSRegression( simTS, ts, tmpProps );
			}
			catch ( Exception e ) {
				rd = null;
			}
			++nfill_methods;
		}
		else if ( fill_method.equalsIgnoreCase("FillRegressLog12") ||
			fill_method.equalsIgnoreCase("FillRegressLogMonthly")) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Detected FillRegressLog12 fill method" );
			}
			fill_methods[nfill_methods] = FILL_METHOD_REGRESS_LOG_12;
			PropList tmpProps = new PropList("specialRegress");
			tmpProps.set ( "Transformation", "Log" );
			tmpProps.set ( "NumberOfEquations", MonthlyEquations" );
			try {
			    rdMonthly = new TSRegression( simTS, ts, tmpProps );
			}
			catch ( Exception e ) {
				rdMonthly = null;
			}
			++nfill_methods;
		}
		else {
		    Message.printWarning(2,routine, "Fill method \"" + fill_method + "\" not recognized...ignoring." );
		}
	}

	// Loop using addInterval...
	date = new DateTime ( start, DateTime.DATE_FAST );
		
	boolean	some_data_found = false;
	double fill_carry_forward_value = 0.0;
	double simValue = 0.0;	// for regression
	for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
		// Get the data value...
		value = ts.getDataValue ( date );
		if ( !ts.isDataMissing(value) ) {
			// No need to process since we only care about missing data...
			some_data_found = true;
			fill_carry_forward_value = value;
			continue;
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Detected missing data value at " + date.toString() );
		}
		// Loop through the list of desired fill methods.  If we are
		// able to fill the value using a fill method, break out and
		// continue processing date/value pairs.  List in the order of most likely first.
		for ( i = 0; i < nfill_methods; i++ ) {
			// For the debug messages below, don't repeat the date
			// since it was printed above.  Also, don't use
			// if, else, else because some of the if's may include
			// checks that do not result in a fill (e.g., if the
			// fill parameters have not been set for the particular date.
			if ( fill_methods[i] == FILL_METHOD_CARRY_FORWARD ) {
				// Keep track of last value used and carry forward if missing...
				if ( some_data_found ) {
					ts.setDataValue ( date, fill_carry_forward_value );
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Filled by carrying forward " + fill_carry_forward_value );
					}
					break;	// Don't need to find another fill method.
				}
			}
			else if ( fill_methods[i] == FILL_METHOD_CONSTANT ) {
				// check for FillFlag
				String fillFlag = proplist.getValue("FillFlag");
				if( fillFlag != null && fillFlag.length() == 1 || fillFlag.equalsIgnoreCase("Auto")) {
					ts.setDataValue( date, fill_constant_value, fillFlag, 1);
				}
				else {
				    // fill constant without fill flag
					ts.setDataValue ( date, fill_constant_value );
				}
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Filled using constant value " + fill_constant_value );
				}
				break;	// Don't need to find another fill method.
			}
			else if ( (fill_methods[i] == FILL_METHOD_INIT_ZERO) && !some_data_found ) {
				// No non-missing data have been found so set to zero...
				ts.setDataValue ( date, 0.0 );
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Filled using 0.0" );
				}
				break;	// Don't need to find another fill method.
			}
			else if ( fill_methods[i] == FILL_METHOD_HIST_MONTH_AVE ) {
				month_pos = date.getMonth() - 1;
				if ( !ts.isDataMissing(fill_hist_month_ave_values[month_pos])){
					// Historical average is not missing so OK to fill with historical average...
					ts.setDataValue ( date, fill_hist_month_ave_values[month_pos] );
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine,
						"Filled using month average " + fill_hist_month_ave_values[month_pos] );
					}
					break;	// Don't need to find another fill method.
				}
			}
			else if ( fill_methods[i] == FILL_METHOD_REGRESS_LINEAR ) {
				try {
				    simValue = simTS.getDataValue ( date );
					if ( !simTS.isDataMissing(simValue)) {
						ts.setDataValue ( date, simValue*rd.getB()+rd.getA());
					}
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Filled using linear regression value "
						+ simValue*rd.getB() + rd.getA() );
					}
					break;	// Don't need to find another fill method.
				}
				catch ( Exception e ) {
					// Probably an error with not having regression data.  Try the next method.
					;
				}
			}
			else if ( fill_methods[i] == FILL_METHOD_REGRESS_LINEAR_12 ) {
				try {
				    simValue = simTS.getDataValue ( date );
					if ( !simTS.isDataMissing(simValue)) {
						ts.setDataValue ( date, 
						simValue*rdMonthly.getB(date.getMonth()) + rdMonthly.getA(date.getMonth()));
					}
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Filled using linear regression value " +
						simValue*rdMonthly.getB(date.getMonth()) + rdMonthly.getA(date.getMonth()));
					}
					break;	// Don't need to find another fill method.
				}
				catch ( Exception e ) {
					// Probably an error with not having regression data.  Try the next method.
					;
				}
			}
		}
	}

	// Fill in the genesis information...

	ts.addToGenesis ( "Filled missing data using " + fill_methods_string + "." );

	// Return the time series...

	return ts;
}
*/

/**
Version of fill that does not require additional time series.
@return Filled time series.
@param ts Time series to fill.
@param props Property list to control filling (see main version of this method).
@exception RTi.TS.TSException If there is a problem filling the time series (generally an input problem).
@deprecated use specific fill methods because this one is getting too complicated to handle errors.
*/
/*
public static TS fill (	TS ts, PropList props )
throws TSException
{	try {
        return fill ( ts, null, props );
	}
	catch ( TSException e ) {
		throw e;
	}
}
*/

/**
Fill missing data by carrying forward the last known value.
@param ts Time series to fill.
@exception RTi.TS.TSException if an error occurs (usually null input).
*/
public static void fillCarryForward ( TS ts )
throws TSException
{	String routine = "TSUtil.fillCarryForward";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Else, use the overall start and end dates for filling...

	try {
	    fillCarryForward ( ts, ts.getDate1(), ts.getDate2() );
	}
	catch ( TSException e ) {
		throw e;
	}
}

/**
Fill missing data by carrying forward the last known value.
@param ts Time series to fill.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@exception RTi.TS.TSException if there is a problem filling data.
*/
public static void fillCarryForward ( TS ts, DateTime start_date, DateTime end_date )
throws TSException
{	String  routine = "TSUtil.fillCarryForward";
	String	message;

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		message = "Filling IrregularTS by carrying forward is not supported";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	// Loop using addInterval...
	DateTime date = new DateTime ( start );
	double data_value = 0.0;
	boolean last_found = false;
	double last_found_value = 0.0;
		
	for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
		data_value = ts.getDataValue ( date );
		if ( ts.isDataMissing ( data_value ) ) {
			if ( last_found ) {
				// Use the last value found to fill.  If no
				// value has been found, leave missing...
				ts.setDataValue ( date, last_found_value );
			}
		}
		else {
		    // Save the last data value...
			last_found_value = data_value;
			last_found = true;
		}
	}

	// Fill in the genesis information...

	ts.setDescription ( ts.getDescription() + ", fill forward" );
	ts.addToGenesis ( "Filled missing data " + start.toString() + " to " +
	end.toString() + " by carrying forward known values." );
}

/**
Fill missing data in the time series with a constant value.
@param value Data value to fill time series.
@param ts Time series to fill.
@deprecated Use the version that takes a PropList.
@exception if there is an error performing the fill.
*/
public static void fillConstant ( TS ts, double value )
throws Exception
{	String routine = "TSUtil.fillConstant";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Else, use the overall start and end dates for filling...

	fillConstant ( ts, ts.getDate1(), ts.getDate2(), value, null );
}

/**
Fill missing data in the time series with a constant value.
@param ts Time series to fill.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param value Data value to fill time series.
@deprecated Use the version that takes a PropList.
@exception if there is an error performing the fill.
*/
public static void fillConstant ( TS ts, DateTime start_date, DateTime end_date, double value )
throws Exception
{	fillConstant ( ts, ts.getDate1(), ts.getDate2(), value, null );
}

/**
Fill missing data in the time series with a constant value.
@param ts Time series to fill.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param value Data value to fill time series.
@param props Properties to control the method, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>DescriptionSuffix</b></td>
<td>
A string to append to the description (specify as a null string to append ", fill constant").
</td>
<td>null will cause the default to be used: ", fill w/ X", where X is the constant used for the fill.
</td>
</tr>

<tr>
<td><b>FillFlag</b></td>
<td>
A string to use as the data flag when a value is filled.  Typically a
one-character string is specified but longer flags can be specified.
</td>
<td>No flag value will be set.
</td>
</tr>

<tr>
<td><b>FillFlagDescription</b></td>
<td>
A string to use as the description for the data flag, for example in report legend.
</td>
<td>Generic description will be generated.
</td>
</tr>

</table>
@return the number of values filled
@exception if there is an error performing the fill.
*/
public static int fillConstant ( TS ts, DateTime start_date, DateTime end_date, double value, PropList props )
throws Exception
{	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	// Get the properties...

	if ( props == null ) {
		props = new PropList ( "fillConstant" );
	}
	// TODO SAM 2007-03-01 Evaluate use
	//String DescriptionSuffix = props.getValue ( "DescriptionSuffix" );

	String FillFlag = props.getValue ( "FillFlag" );
	boolean FillFlag_boolean = false;	// Indicate whether to use flag
	if ( (FillFlag != null) && (FillFlag.length() > 0) ) {
		FillFlag_boolean = true;
		// Make sure that the data flag is allocated.
		ts.allocateDataFlagSpace (
			null,	// Initial flag value
			true );	// Keep old flags if already allocated
	}
	String fillFlagDescription = props.getValue ( "FillFlagDescription" );

	int	interval_base = ts.getDataIntervalBase();
	int	interval_mult = ts.getDataIntervalMult();
	double oldvalue;
	int	nfilled = 0;
	TSData tsdata = new TSData(); // Data point used for irregular and for handling the data flag.
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return nfilled;
		}
		int nalltsdata = alltsdata.size();
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				oldvalue = tsdata.getDataValue();
				if ( irrts.isDataMissing(oldvalue) ) {
					// Do in any case...
					tsdata.setDataValue(value);
					if ( FillFlag_boolean ) {
						// Also set the data flag, appending to the old value...
						tsdata.setDataFlag ( tsdata.getDataFlag().trim() + FillFlag );
					}
					// Have to do this manually since TSData are being modified directly to improve performance...
					irrts.setDirty ( true );
					++nfilled;
				}
			}
		}
	}
	else {	// Loop using addInterval...
		DateTime date = new DateTime ( start );
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			oldvalue = ts.getDataValue ( date );
			if ( ts.isDataMissing(oldvalue) ) {
				if ( FillFlag_boolean ) {
					// Set the data flag, appending to the old value...
					tsdata = ts.getDataPoint ( date, tsdata );
					ts.setDataValue ( date, value, (tsdata.getDataFlag().trim()+FillFlag), 1 );
				}
				else {
				    // No data flag...
					ts.setDataValue ( date, value );
				}
				++nfilled;
			}
		}
	}

	// Fill in the genesis information...

	if ( nfilled > 0 ) {
		ts.setDescription ( ts.getDescription() + ", fill w/ " + StringUtil.formatString(value,"%.3f") );
		if ( FillFlag_boolean ) {
			ts.addToGenesis ( "Filled missing data " + start + " to " +
				end + " using constant " + value + ", flag \"" + FillFlag + "\" (" + nfilled + " values filled)." );
			if ( (fillFlagDescription != null) && !fillFlagDescription.isEmpty() ) {
	            ts.addDataFlagMetadata(new TSDataFlagMetadata(FillFlag, fillFlagDescription));
	        }
	        else {
	            ts.addDataFlagMetadata(new TSDataFlagMetadata(FillFlag, "Filled missing data using constant " + value));
	        }
		}
		else {
			ts.addToGenesis ( "Filled missing data " + start + " to " +
				end + " using constant " + value + " (" + nfilled + " values filled)." );
		}
	}
	return nfilled;
}

/**
Fill missing data in the time series with monthly values.  For example, values[0] is
used for any date in January.  This can be used, for example, to fill an entire
period with a repetitive monthly pattern.
@return the number of values that were filled.
@param ts Time series to fill.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param values Data values to fill time series (the first value is for January, the last is for December).
@param descriptionSuffix a string to append to the description (specify as a
null string to append default ", fill w/ mon avg").
@param fillFlag a string to use as the data flag when a value is filled.  Null will result in no flag being used.
Specify as "auto" to use the month abbreviation and "Avg".
@param fillFlagDesc the description of the fill flag.  If specified, append to the month abbreviation.
@exception if there is an error performing the fill.
*/
public static int fillConstantByMonth (	TS ts, DateTime start_date, DateTime end_date, double values[],
	String descriptionSuffix, String fillFlag, String fillFlagDescription )
throws Exception
{	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	boolean FillFlag_boolean = false;	// Indicate whether to use flag
	String [] fillFlagByMonth = null;
	if ( (fillFlag != null) && (fillFlag.length() > 0) ) {
		FillFlag_boolean = true;
		// Make sure that the data flag is allocated.
		ts.allocateDataFlagSpace (
			null,	// Initial flag value
			true );	// Keep old flags if already allocated
		// Define the fill flags to use for each month
		fillFlagByMonth = new String[12];
		for ( int i = 0; i < 12; i++ ) {
		    if ( fillFlag.equalsIgnoreCase("auto") ) {
		        fillFlagByMonth[i] = TimeUtil.monthAbbreviation(i + 1) + "Avg";
		    }
		    else {
		        fillFlagByMonth[i] = fillFlag;
		    }
		}
	}

	int	interval_base = ts.getDataIntervalBase();
	int	interval_mult = ts.getDataIntervalMult();
	double	oldvalue;
	int [] nfilled = new int[12];
	for ( int i = 0; i < 12; i++ ) {
		nfilled[i] = 0;
	}
	TSData tsdata = new TSData(); // Used for irrigular data and setting the flag in regular data.
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return 0;
		}
		int nalltsdata = alltsdata.size();
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = (TSData)alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				oldvalue = tsdata.getDataValue();
				if ( irrts.isDataMissing(oldvalue) ) {
					tsdata.setDataValue(values[date.getMonth() - 1]);
					if ( FillFlag_boolean ) {
						// Set the flag, appending to the old value...
						tsdata.setDataFlag ( tsdata.getDataFlag().trim() + fillFlagByMonth[date.getMonth() - 1] );
					}
					++nfilled[date.getMonth() - 1];
					// Have to do this manually since TSData are being modified directly to improve performance...
					irrts.setDirty ( true );
				}
			}
		}
	}
	else {
		// Loop using addInterval...
		DateTime date = new DateTime ( start );
		
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			oldvalue = ts.getDataValue ( date );
			if ( ts.isDataMissing(oldvalue) ) {
				if ( FillFlag_boolean ) {
					// Set the data flag, appending to the old value...
					tsdata = ts.getDataPoint ( date, tsdata );
					ts.setDataValue ( date, values[date.getMonth() - 1],
					    (tsdata.getDataFlag().trim()+ fillFlagByMonth[date.getMonth() - 1]), 1 );
				}
				else {
					// No data flag...
					ts.setDataValue ( date,
					values[date.getMonth() - 1] );
				}
				++nfilled[date.getMonth() - 1];
			}
		}
	}

	// Fill in the genesis information...

	int nfilledTotal = 0;
	for ( int i = 0; i < 12; i++ ) {
		nfilledTotal += nfilled[i];
	}
	ts.addToGenesis ( "Filled " + nfilledTotal + " total missing data value" + StringUtil.pluralS(nfilledTotal) +
	    " " + start.toString() + " to " + end.toString() + " with historical monthly average values:" );
	for ( int i = 0; i < 12; i++ ) {
		if ( !ts.isDataMissing(values[i]) ) {
			ts.addToGenesis ( "    " + TimeUtil.monthAbbreviation(i + 1) + ": " +
			StringUtil.formatString(values[i],"%12.4f") + " (filled " + nfilled[i] + " value" +
			StringUtil.pluralS(nfilled[i]) + ")" );
		}
		else {
			ts.addToGenesis ( TimeUtil.monthAbbreviation(i + 1) + ": Value to use for filling is missing.");
		}
		if ( fillFlagByMonth != null ) {
		    // Fill flags are being used
		    if ( nfilled[i] > 0 ) {
    		    // Add a description for the data flag
    		    if ( (fillFlag != null) && fillFlag.equalsIgnoreCase("auto") ) {
    		        // Flags will be different for each month...
    		        if ( (fillFlagDescription != null) && (fillFlagDescription.length() > 0) ) {
    		            ts.addDataFlagMetadata(new TSDataFlagMetadata(fillFlagByMonth[i], fillFlagDescription));
    		        }
    		        else {
    		            ts.addDataFlagMetadata(new TSDataFlagMetadata(
        		        fillFlagByMonth[i], "Filled " + nfilled[i] + " missing value" + StringUtil.pluralS(nfilled[i]) +
        		            " using historical monthly average " + StringUtil.formatString(values[i],"%.4f")));
    		        }
    		    }
		    }
		}
	}
    if ( (fillFlagByMonth != null) && !fillFlag.equalsIgnoreCase("auto") ) {
        // One flag for entire time series since not "auto"
        if ( (fillFlagDescription != null) && (fillFlagDescription.length() > 0) ) {
            ts.addDataFlagMetadata(new TSDataFlagMetadata( fillFlagByMonth[0], fillFlagDescription ) );
        }
        else {
            ts.addDataFlagMetadata(new TSDataFlagMetadata( fillFlagByMonth[0], "Filled " + nfilledTotal +
            " missing value" + StringUtil.pluralS(nfilledTotal) + " using historical monthly average values."));
        }
    }
	if ( descriptionSuffix == null ) {
		if ( nfilledTotal > 0 ) {
			ts.setDescription ( ts.getDescription() + ", fill w/ mon avg" );
		}
	}
	else {
		ts.setDescription ( ts.getDescription() + descriptionSuffix );
	}
	return nfilledTotal;
}

/**
Fill missing data in a daily time series using D1 = D2 * M1/M2.
@param dayts1 Daily time series to fill, D1.
@param monthts1 Monthly time series, M1.
@param dayts2 Daily time series, D2.
@param monthts2 Monthly time series, M2.
@exception RTi.TS.TSException if an error occurs (usually null input).
*/
public static void fillDayTSFrom2MonthTSAnd1DayTS (	DayTS dayts1, MonthTS monthts1, DayTS dayts2, MonthTS monthts2 )
throws TSException
{	String routine = "TSUtil.fillDayTSFrom2MonthTSAnd1DayTS";

	if ( dayts1 == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
		throw new TSException ( "D1 time series is null" );
	}

	// Else, use the overall start and end dates for filling...

	try {
	    fillDayTSFrom2MonthTSAnd1DayTS ( dayts1, monthts1, dayts2, monthts2, dayts1.getDate1(), dayts1.getDate2() );
	}
	catch ( TSException e ) {
		throw e;
	}
}

/**
Fill missing data in a daily time series using D1 = D2 * M1/M2.
@param dayts1 Daily time series to fill, D1.
@param monthts1 Monthly time series, M1.
@param dayts2 Daily time series, D2.
@param monthts2 Monthly time series, M2.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@exception RTi.TS.TSException if there is a problem filling data.
*/
public static void fillDayTSFrom2MonthTSAnd1DayTS (	TS dayts1, MonthTS monthts1, DayTS dayts2,
	MonthTS monthts2, DateTime start_date, DateTime end_date )
throws TSException
{	String routine = "TSUtil.fillDayTSFrom2MonthTSAnd1DayTS";

	if ( dayts1 == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null D1 time series" );
		throw new TSException ( "D1 time series is null" );
	}
	if ( dayts1.getDataIntervalBase() != TimeInterval.DAY ) {
		Message.printWarning ( 2, routine, "D1 is not daily TS" );
		throw new TSException ( "D1 time series is not daily." );
	}
	if ( monthts1 == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null M1 time series" );
		throw new TSException ( "M1 time series is null" );
	}
	if ( monthts1.getDataIntervalBase() != TimeInterval.MONTH ) {
		Message.printWarning ( 2, routine, "M1 is not monthly TS" );
		throw new TSException ( "M1 time series is not monthly." );
	}
	if ( dayts2 == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null D2 time series" );
		throw new TSException ( "D2 time series is null" );
	}
	if ( dayts2.getDataIntervalBase() != TimeInterval.DAY ) {
		Message.printWarning ( 2, routine, "D2 is not daily TS" );
		throw new TSException ( "D2 time series is not daily." );
	}
	if ( monthts2 == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null M2 time series" );
		throw new TSException ( "M2 time series is null" );
	}
	if ( monthts2.getDataIntervalBase() != TimeInterval.MONTH ) {
		Message.printWarning ( 2, routine, "M2 is not monthly TS" );
		throw new TSException ( "M2 time series is not monthly." );
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( dayts1, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	// Loop using addInterval...
	DateTime date = new DateTime ( start );
	double data_value = 0.0;
	double m1, m2, d2;
	double m1_m2 = -999;		// M1/M2
	int	fill_count = 0;
		
	for ( ; date.lessThanOrEqualTo( end ); date.addDay(1) ) {
		data_value = dayts1.getDataValue ( date );
		if ( !dayts1.isDataMissing ( data_value ) ) {
			continue;
		}
		// See if we need to compute monthly ratio...
		if (	(m1_m2 < 0.0) || (date.getDay() == 1) ) {
			m1 = monthts1.getDataValue ( date );
			if ( monthts1.isDataMissing(m1) ) {
				continue;
			}
			m2 = monthts2.getDataValue ( date );
			if ( monthts2.isDataMissing(m2) ) {
				continue;
			}
			if ( m2 == 0.0 ) {
				m1_m2 = 0.0;
			}
			else {
			    m1_m2 = m1/m2;
			}
		}
		// Now fill in the value...
		d2 = dayts2.getDataValue ( date );
		if ( dayts2.isDataMissing(d2) ) {
			continue;
		}
		dayts1.setDataValue ( date, d2*m1_m2 );
		++fill_count;
	}

	// Fill in the genesis information...

	dayts1.setDescription ( dayts1.getDescription() + ", fill D2*M1/M2" );
	dayts1.addToGenesis ( "Filled " + fill_count + " missing data points " +
		start.toString() + " to " + end.toString() + " using D1 = D2 * M1/M2." );
}

/**
Fill missing data by getting non-missing values from another time series.
@param dependentTS Time series to fill.
@param independentTS Time series to fill from.
@exception RTi.TS.TSException if an error occurs (usually null input).
*/
public static void fillFromTS ( TS dependentTS, TS independentTS )
throws TSException
{	String routine = "TSUtil.fillFromTS";

	if ( dependentTS == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Else, use the overall start and end dates for filling...

	fillFromTS ( dependentTS, independentTS, dependentTS.getDate1(), dependentTS.getDate2(), null, null );
}

/**
Fill missing data by getting non-missing values from another time series.
The data intervals do not need to be the same.
@param dependentTS Time series to fill.
@param independentTS Time series to fill from.
@exception RTi.TS.TSException if an error occurs (usually null input).
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param fillFlag a string to use as the data flag when a value is filled (null or empty string result in no flag).
@param fillFlagDesc the description of the fill flag.  If specified, append to the month abbreviation.
@exception RTi.TS.TSException if there is a problem filling data.
*/
public static void fillFromTS (	TS dependentTS, TS independentTS, DateTime fillStart, DateTime fillEnd,
    String fillFlag, String fillFlagDescription )
throws TSException
{	String routine = "TSUtil.fillFromTS";
	String message;

	if ( (dependentTS == null) || (independentTS == null) ) {
		return;
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod (dependentTS,fillStart,fillEnd);
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();
	
	boolean fillFlagSpecified = false;  // Indicate whether to use flag
    if ( (fillFlag != null) && (fillFlag.length() > 0) ) {
        fillFlagSpecified = true;
    }

	int interval_base = dependentTS.getDataIntervalBase();
	int interval_mult = dependentTS.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		message = "Filling IrregularTS by using another time series is not supported";
		Message.printWarning ( 2, routine, message );
		throw new IrregularTimeSeriesNotSupportedException(message);
	}
	// Loop using addInterval...
	double dependentValue = 0.0;
	double independentValue = 0.0;
	TSData dependentData = new TSData();
	for ( DateTime date = new DateTime ( start ); date.lessThanOrEqualTo( end );
	    date.addInterval(interval_base, interval_mult) ) {
		dependentData = dependentTS.getDataPoint ( date, dependentData );
		dependentValue = dependentData.getDataValue();
		if ( dependentTS.isDataMissing ( dependentValue ) ) {
			independentValue = independentTS.getDataValue ( date );
			if ( !independentTS.isDataMissing ( independentValue ) ) {
	             if ( fillFlagSpecified ) {
                    // Set the data flag, appending to the old value...
	                 dependentTS.setDataValue ( date, independentValue, fillFlag, 1 );
                }
                else {
                    // No flag so just set the data value
                    dependentTS.setDataValue ( date, independentValue );
                }
			}
		}
	}

	// Fill in the genesis information...

	dependentTS.setDescription ( dependentTS.getDescription() + ", fillFromTS" );
	dependentTS.addToGenesis ( "Filled missing data " + start.toString() +
		" to " + end.toString() + " by using known values from " + independentTS.getIdentifierString() );
    if ( fillFlagSpecified && (fillFlagDescription != null) && (fillFlagDescription.length() > 0) ) {
        dependentTS.addDataFlagMetadata(new TSDataFlagMetadata(fillFlag, fillFlagDescription));
    }
}

/**
Fill missing data in the time series using linear interpolation between non-missing values.
@param ts Time series to fill.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param intervals_can_fill Number of intervals (width of gap) that can be filled.
If 0 then there is no limit on the number of intervals that can be filled.
@param interpolate_method Reserved for future use.  Currently always linear.
@return number of values filled
@exception TSException if there is an error filling the time series.
*/
public static int fillInterpolate ( TS ts, DateTime start_date, DateTime end_date,
	int intervals_can_fill, int interpolate_method )
throws TSException, Exception
{
	PropList props = new PropList ( "fillInterpolate" );
	props.set( "MaxIntervals=" + intervals_can_fill );
	return fillInterpolate ( ts, start_date, end_date, props );
}

/**
Fill missing data in the time series using linear interpolation between
non-missing values.  There is no limit on how many points can be filled between known points.
@param ts Time series to fill.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@exception TSException if there is an error filling the time series.
*/
public static void fillInterpolate ( TS ts, DateTime start_date, DateTime end_date )
throws TSException, Exception
{	PropList props = new PropList ( "fillInterpolate" );
	props.set ( "MaxIntervals=0");
	fillInterpolate ( ts, start_date, end_date, props );
}

/**
Fill missing data in the time series using linear interpolation between non-missing values.
@param ts Time series to fill.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param intervals_can_fill Number of intervals (width of gap) that can be filled.
If 0 then there is no limit on the number of intervals that can be filled.
@param interpolate_method Reserved for future use.  Currently always linear.
@param props Properties to control the method, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>FillFlag</b></td>
<td>
A string to use as the data flag when a value is filled.  Typically a one-character string is specified.
</td>
<td>No flag value will be set.
</td>
</tr>

<tr>
<td><b>FillFlagDescription</b></td>
<td>
A string to use as the description for the data flag, for example in report legend.
</td>
<td>Generic description will be generated.
</td>
</tr>

<tr>
<td><b>MaxIntervals</b></td>
<td>
The maximum number of missing values in a contiguous gap to fill or zero to fill all.
</td>
<td>Fill over any size gap.
</td>
</tr>

</table>
@return number of values filled
@exception TSException if there is an error filling the time series.
*/
public static int fillInterpolate ( TS ts, DateTime start_date, DateTime end_date, PropList props )
throws Exception
{	String message, routine = "TSUtil.fillInterpolate";

	if ( props == null ) {
		props = new PropList ("");
	}
	int intervals_can_fill = 0;	// Defaults
	// TODO SAM 2007-07-11 Enable later - Log, etc.
	//int interpolate_method = 0;
	
	String MaxIntervals = props.getValue ( "MaxIntervals");
	if ( MaxIntervals != null ) {
		intervals_can_fill = StringUtil.atoi ( MaxIntervals );
	}

	if ( ts == null ) {
		// No time series...
		message = "Null time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	
	String FillFlag = props.getValue ( "FillFlag" );
	boolean FillFlag_boolean = false; // Indicate whether to use flag
	if ( (FillFlag != null) && (FillFlag.length() > 0) ) {
		FillFlag_boolean = true;
		// Make sure that the data flag is allocated.
		ts.allocateDataFlagSpace (
			null,	// Initial flag value
			true );	// Keep old flags if already allocated
	}
	
	String fillFlagDescription = props.getValue ( "FillFlagDescription" );

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	int	interval_base = ts.getDataIntervalBase();
	int	interval_mult = ts.getDataIntervalMult();
	double	delta = 0.0, oldvalue = 0.0, value = 0.0;
	TSData tsdata = new TSData(); // Data point used for setting the data flag handling the data flag.
	int fillCount = 0;
	if ( interval_base == TimeInterval.IRREGULAR ) {
		message = "Filling irregular time series using interpolation is not supported.";
		Message.printWarning ( 3, routine, message );
		throw new IrregularTimeSeriesNotSupportedException(message);
	}
	else {
	    // Loop using addInterval...
		DateTime date = new DateTime ( start );
		DateTime after, before;
		boolean after_found = false, before_found = false;
		int num_after_intervals = 0, num_before_intervals = 0;
		boolean previous_was_filled = false;
		double before_value = 0.0, after_value = 0.0;
		DateTime date1 = ts.getDate1();
		DateTime date2 = ts.getDate2();
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			oldvalue = ts.getDataValue ( date );
			if ( ts.isDataMissing(oldvalue) ) {
				// If the previous value was filled using interpolation then re-use the delta...
				if ( previous_was_filled ) {
					value = value + delta;
					if ( Message.isDebugOn ) {
						Message.printStatus(2, routine, "date=" + date + " delta=" + delta + " value=" + value);
					}
					if ( FillFlag_boolean ) {
						// Set the data flag, appending to the old value...
						tsdata = ts.getDataPoint ( date, tsdata );
						ts.setDataValue ( date, value, (tsdata.getDataFlag().trim()+FillFlag), 1 );
					}
					else {
						ts.setDataValue ( date, value );
					}
					++fillCount;
					previous_was_filled = true;
					continue;
				}
				// Search forward and back to find known values...  Inline the code here to improve performance...
				before_found = false;
				after_found = false;
				before = new DateTime ( date );
				after = new DateTime ( date );
				num_before_intervals = num_after_intervals = 0;
				// Search for date before with non-missing.  Start with the current date so decrement.
				// There are two cases:
				// 1) Missing value is immediately after a non-missing value, which is the typical case.
				//    In this case the logic looks forward to the bounding end value and the delta
				//    works properly with check above for 'previous_was_filled'.
				//      num_before_intervals = 1
				// 2) It is possible that non-missing is before the fill start (if a start was specified)
				//    so allow searching to the start of the time series.
				//    In this case additional delta values need to be added to jump over the
				//    values before the start period.
				//      num_before_intervals > 1
				//while ( before.greaterThanOrEqualTo(start) ) {
				while ( before.greaterThanOrEqualTo(date1) ) {
					// If not missing, break...
					if ( !ts.isDataMissing( ts.getDataValue(before)) ) {
						before_found = true;
						break;
					}
					before.addInterval(interval_base, -interval_mult);
					++num_before_intervals;
					if ( (intervals_can_fill != 0) && (num_before_intervals > intervals_can_fill) ) {
						break;
					}
				}
				// Now search for date after.
				// It is possible that non-missing is before the fill start so allow searching to the
				// start of the time series.
				while ( after.lessThanOrEqualTo(date2) ) {
					// If not missing, break...
					if ( !ts.isDataMissing( ts.getDataValue(after)) ) {
						after_found = true;
						break;
					}
					after.addInterval(interval_base, interval_mult);
					++num_after_intervals;
					if ( (intervals_can_fill != 0 ) && (num_after_intervals > intervals_can_fill) ) {
						break;
					}
				}
				if ( before_found && after_found && ((intervals_can_fill == 0) ||
					((num_before_intervals + num_after_intervals - 1) <= intervals_can_fill)) ) {
					// Can do the interpolation...
					before_value = ts.getDataValue(before);
					after_value = ts.getDataValue(after);
					delta =	(after_value - before_value)/(num_before_intervals + num_after_intervals);
					value = before_value + delta*num_before_intervals;
					if ( Message.isDebugOn ) {
						Message.printStatus(2, routine, "date=" + date + " before=" + before + " beforeValue=" + before_value +
							" after=" + after + " afterValue=" + after_value +
							" num_before_intervals=" + num_before_intervals + " num_after_intervals=" + num_after_intervals +
							" delta=" + delta + " value=" + value);
					}
					if ( FillFlag_boolean ) {
						// Set the data flag, appending to the old value...
						tsdata = ts.getDataPoint ( date, tsdata );
						ts.setDataValue ( date, value, (tsdata.getDataFlag().trim()+FillFlag), 1 );
					}
					else {
						ts.setDataValue ( date, value );
					}
					++fillCount;
					previous_was_filled = true;
				}
			}
			else {
			    // Not filling...
				previous_was_filled = false;
			}
		}
	}

	// Fill in the genesis information...

    String maxString = "maximum of " + intervals_can_fill + " intervals in gap allowed.";
    if ( intervals_can_fill == 0 ) {
        maxString = "no limit to number of intervals in a gap to fill.";
    }
	ts.addToGenesis ( "Filled " + fillCount + " missing values " + start + " to " +
	    end + " using linear interpolation from known values, " + maxString );
    if ( fillCount > 0 ) {
    	ts.setDescription ( ts.getDescription() + ",fill interpolate" );
		if ( (fillFlagDescription != null) && !fillFlagDescription.isEmpty() ) {
            ts.addDataFlagMetadata(new TSDataFlagMetadata(FillFlag, fillFlagDescription));
        }
        else {
            ts.addDataFlagMetadata(new TSDataFlagMetadata(FillFlag, "Filled missing data using interpolation (max intervals=" + intervals_can_fill + ")"));
        }
	}
    return fillCount;
}

/**
Fill the entire time series with monthly values if missing.  
For example, values[0] is used for any date in January.
@param ts Time series to fill.
@param values Data values to fill time series (the first value is for January, the last is for December).
@deprecated Use TSUtil.fillConstant or TSUtil.setConstant.
*/
public static void fillMonthly( TS ts, double values[] )
{	String routine = "TSUtil.fillMonthly";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Else, use the overall start and end dates for filling...

	fillMonthly( ts, ts.getDate1(), ts.getDate2(), values );
}

/**
Fill the entire time series with monthly values if missing.  
For example, values[0] is used for any date in January.
@param ts Time series to fill.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param values Data values to fill time series (the first value is for January, the last is for December).
@deprecated Use TSUtil.fillConstant or TSUtil.setConstant.
*/
public static void fillMonthly(	TS ts, DateTime start_date, DateTime end_date, double values[] )
{	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				if ( ts.isDataMissing ( ts.getDataValue ( date ))) {
				    tsdata.setDataValue(values[date.getMonth() - 1]);
				    // Have to do this manually since TSData are being modified directly to improve performance...
				    ts.setDirty ( true );
				}
			}
		}
	}
	else {
	    // Loop using addInterval...
		DateTime date = new DateTime ( start );
		
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			if ( ts.isDataMissing ( ts.getDataValue ( date )))
			ts.setDataValue ( date, values[date.getMonth() - 1] );
		}
	}

	// Fill in the genesis information...

	ts.addToGenesis ( "Filled missing data " + start_date.toString() + " to " + end_date.toString() + " with monthly values:" );
	for ( int i = 0; i < 12; i++ ) {
		if ( !ts.isDataMissing(values[i]) ) {
			ts.addToGenesis ( TimeUtil.MONTH_ABBREVIATIONS[i] + ": " + StringUtil.formatString(values[i],"%12.2f"));
		}
		else {
		    ts.addToGenesis ( TimeUtil.MONTH_ABBREVIATIONS[i] + ": No non-missing data available for filling.");
		}
	}
}

/**
Fill the time series missing data with monthly values based on the averages for
the given pattern.  For example, if Dec, 1995 is missing, and Dec 1995 is a
"wet" year according to the pattern, fill Dec, 1995 with the average of all wet year Decembers.
@param ts Time series to fill.
@param pattern_ts Pattern time series.
@param props See the full-argument version for information.
*/
public static void fillPattern ( TS ts, StringMonthTS pattern_ts, PropList props )
throws Exception
{	String routine = "TSUtil.fillPattern";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
		return;
	}
	if ( pattern_ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null pattern time series" );
		return;
	}

	// Else, use the overall start and end dates for filling...

	fillPattern ( ts, pattern_ts, ts.getDate1(), ts.getDate2(), null, props );
}

/**
Fill the time series missing data with monthly values based on the averages for
the given pattern.  For example, if Dec, 1995 is missing, and Dec 1995 is a
"wet" year according to the pattern, fill Dec, 1995 with the average of all wet year Decembers.
@param ts Time series to fill.
@param pattern_ts Pattern time series.
*/
public static void fillPattern ( TS ts, StringMonthTS pattern_ts )
throws Exception
{	String routine = "TSUtil.fillPattern";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
		return;
	}
	if ( pattern_ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null pattern time series" );
		return;
	}

	// Else, use the overall start and end dates for filling...

	fillPattern ( ts, pattern_ts, ts.getDate1(), ts.getDate2(), null, (PropList)null );
}

/**
Fill the time series missing data with monthly values based on the averages for
the given pattern.  For example, if Dec, 1995 is missing, and Dec 1995 is a
"wet" year according to the pattern, fill Dec, 1995 with the average of all wet year Decembers.
@param ts Time series to fill.
@param pattern_ts Pattern time series.
@param pattern_stats Time series pattern statistics returned from
TSUtil.getPatternStats.  In general, this is only used by complicated analysis code.
*/
public static void fillPattern ( TS ts, StringMonthTS pattern_ts, TSPatternStats pattern_stats )
throws Exception
{	String routine = "TSUtil.fillPattern";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
		return;
	}
	if ( pattern_ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null pattern time series" );
		return;
	}

	// Else, use the overall start and end dates for filling...

	fillPattern ( ts, pattern_ts, ts.getDate1(), ts.getDate2(), pattern_stats, (PropList)null );
}

/**
Fill the time series missing data with monthly values based on the averages for
the given pattern.  For example, if Dec, 1995 is missing, and Dec 1995 is a
"wet" year according to the pattern, fill Dec, 1995 with the average of all wet year Decembers.
@param ts Time series to fill.
@param pattern_ts Pattern time series.
@param start_date Starting date for fill.
@param end_date Ending date for fill.
@param pattern_stats Time series pattern statistics returned from
TSUtil.getPatternStats.  In general, this is only used by complicated analysis code.
*/
public static void fillPattern(	TS ts, StringMonthTS pattern_ts, DateTime start_date, DateTime end_date,
    TSPatternStats pattern_stats )
throws Exception
{	fillPattern ( ts, pattern_ts, start_date, end_date, pattern_stats, (PropList)null );
}

/**
Fill the time series missing data with monthly values based on the averages for
the given pattern.  For example, if Dec, 1995 is missing, and Dec 1995 is a
"wet" year according to the pattern, fill Dec, 1995 with the average of all wet year Decembers.
@param ts Time series to fill.
@param pattern_ts Pattern time series.
@param start_date Starting date for fill.
@param end_date Ending date for fill.
@param pattern_stats Time series pattern statistics returned from
TSUtil.getPatternStats.  In general, this is only used by complicated analysis code.
@param props PropList containing modifiers.
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>FillFlag</b></td>
<td>
Specify a 1-character value to tag filled data values.  If "Auto", the first
character of the pattern indicator will be used (e.g., "W" for "WET", "A" for
"AVG", and "D" for "DRY").
</td>
No flag is assigned.
</td>
</tr>

<tr>
<td><b>IgnoreLessThanOrEqualZero</b></td>
<td>
Indicates whether values <= zero should be treated as missing when computing averages.
</td>
False
</td>
</tr>

</table>
@exception Exception if an error occurs.
*/
public static void fillPattern(	TS ts, StringMonthTS pattern_ts, DateTime start_date, DateTime end_date,
	TSPatternStats pattern_stats, PropList props )
throws Exception
{	String routine = "TSUtil.fillPattern";
	double fill_value;
	String indicator;
	int	dl = 20;

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
		return;
	}
	if ( pattern_ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null pattern time series" );
		return;
	}

	if ( props == null ) {
		props = new PropList ( "fillPattern" );
	}
	String FillFlag = props.getValue ( "FillFlag" );
	boolean FillFlag_boolean = false;	// Indicate whether to use flag
	boolean FillFlagAuto_boolean = false;
	if ( FillFlag != null ) {
		FillFlag_boolean = true;
		// Make sure that the data flag is allocated.
		if ( FillFlag.equalsIgnoreCase("Auto") ) {
			FillFlagAuto_boolean = true;
			ts.allocateDataFlagSpace (
			null,	// Initial flag value
			true );	// Keep old flags if already allocated
		}
		else {
			ts.allocateDataFlagSpace (
			null,	// Initial flag value
			true );	// Keep old flags if already allocated
		}
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	// Get the average values for the incoming time series according to the pattern...

	TSPatternStats stats = null;
	if ( pattern_stats == null ) {
		stats = getPatternStats ( ts, pattern_ts, props );
	}
	else {
	    stats = pattern_stats;
	}
	Message.printStatus ( 2, routine, "Filling \"" + ts.getIdentifierString() + "\" for " + start + " to " + end );
	Message.printStatus ( 2, routine, stats.toString () );

	TSData tsdata = null;
	int nfilled = 0;	// Number of actual points that were filled
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				if ( ts.isDataMissing ( ts.getDataValue ( date ))) {
					indicator = pattern_ts.getDataValueAsString( date );
					if ( indicator.equals("") ) {
						if ( Message.isDebugOn ) {
							Message.printWarning (5, routine, "Unable to get pattern for date " + date + ".  Not filling." );
						}
						continue;
					}
					try {
					    fill_value = stats.getAverage ( indicator, date.getMonth() );
						if ( !ts.isDataMissing(fill_value) ) {
							tsdata.setDataValue ( fill_value );
							if ( FillFlag_boolean ){
								// Set the flag, appending to the old value...
								if ( FillFlagAuto_boolean ) {
									FillFlag = "" + indicator.charAt(0);
								}
								tsdata.setDataFlag ( tsdata.getDataFlag().trim() + FillFlag );
							}
							// Have to do this manually since TSData are being modified directly to improve performance...
							ts.setDirty ( true );
							if ( Message.isDebugOn){
								Message.printDebug ( dl, routine,
								"Filled using pattern \"" + indicator + "\" average: " + fill_value + " at " + date );
							}
							++nfilled;
						}
					}
					catch ( TSException e ) {
						Message.printWarning ( 2,
						routine, "Unable to get fill value for pattern \"" +indicator+ "\" on date " + date );
					}
				}
			}
		}
	}
	else {	// Loop using addInterval...
		DateTime date = new DateTime ( start );
		
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			if ( ts.isDataMissing ( ts.getDataValue ( date ))) {
				indicator=pattern_ts.getDataValueAsString(date);
				if ( indicator.equals("") ) {
					if ( Message.isDebugOn ) {
						Message.printWarning ( 5, routine, "Unable to get pattern for date " + date + ".  Not filling." );
					}
					continue;
				}
				try {
				    fill_value = stats.getAverage ( indicator, date.getMonth() );
					if ( !ts.isDataMissing(fill_value) ) {
						ts.setDataValue ( date, fill_value );
						if ( FillFlag_boolean ) {
							// Set the data flag, appending to the old value...
							if ( FillFlagAuto_boolean ) {
								FillFlag = "" + indicator.charAt(0);
							}
							tsdata = ts.getDataPoint( date, tsdata );
							ts.setDataValue ( date, fill_value, (tsdata.getDataFlag().trim()+FillFlag), 1 );
						}
						else {
						    // No data flag...
							ts.setDataValue ( date, fill_value );
						}
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, routine, "Filled using pattern \"" +
							indicator + "\" average: " + fill_value + " at " + date );
						}
						++nfilled;
					}
				}
				catch ( TSException e ) {
					Message.printWarning ( 2, routine,
					    "Unable to get fill value for pattern \"" + indicator + "\" on date " +date);
				}
			}
		}
	}
	if ( nfilled > 0 ) {
		// Only change the description if some data were filled...
		ts.setDescription( ts.getDescription() + ",fillpattern " + pattern_ts.getLocation() );
	}
	ts.addToGenesis ( "Filled using pattern information for " + start + " to " + end + " using pattern " +
		pattern_ts.getLocation() + " (" + nfilled + " values filled).");
}

/**
Fill missing data by prorating known value from an independent time series,
where the ratio is recalculated each time that values are present in the
independent and fill time series.  Data processing can occur forward
or backward in time.  The ratio is ts/independent for known points.  For
missing values in ts, the value is estimated as
ts_value = independent_value*(ratio).  Independent values of zero are ignored
when computing the ratio (fillConstant() can be used later to fill with zero if appropriate.
@param ts Time series to fill.
@param independent_ts Independent time series to find non-missing values to compute the ratio.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param direction -1 if filling backward, >= 0 if filling forward.
@param initial_value Initial value of the fill time series, to compute the ratio
on the end of the period.  See the overloaded method for possible values.
@exception RTi.TS.Exception if there is a problem filling data.
@deprecated Use the PropList version of this method.
*/
public static void fillProrate(	TS ts, TS independent_ts, DateTime start_date, DateTime end_date,
				int direction, double initial_value )
throws Exception
{	PropList props = new PropList ( "fillProrate" );
	if ( direction < 0 ) {
		props.set ( "Direction", "Backward" );
	}
	else {	props.set ( "Direction", "Forward" );
	}
	props.set ( "InitialValue", "" + initial_value );
	fillProrate ( ts, independent_ts, start_date, end_date, props );
}

/**
Fill missing data by prorating known value from an independent time series.
The ratio is ts/independent for known points.  For missing values in ts, the
value is estimated as ts_value = independent_value*(ratio).
@param ts Time series to fill.
@param independent_ts Independent time series to find non-missing values to
compute the ratio.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param props Properties to control the fill, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>AnalysisEnd</b></td>
<td>
The end of the analysis period as a date/time string,
used when "FactorMethod" is "AnalyzeAverage".
</td>
<td>
Analyze the full period.
</td>
</tr>

<tr>
<td><b>AnalysisStart</b></td>
<td>
The start of the analysis period as a date/time string,
used when "FactorMethod" is "AnalyzeAverage".
</td>
<td>
Analyze the full period.
</td>
</tr>

<tr>
<td><b>FactorMethod</b> or <b>CalculateFactorHow</b> (deprecated)</td>
<td>
Indicate how to calculate the factor.
<ol>
<li>	If "NearestPoint", then the nearest data values from the two time
	series are used to compute the factor (ratio).
	The factor is then used until a new value can be calculated.  The
	"Direction" parameter will control which points are processed first.
	</li>
<li>	If "AnalyzeAverage" is specified, then the "AnalysisStart" and
	"AnalysisEnd" are used to indicate a period, which is used to compute
	an average factor value for overlapping time series points.  The
	constant average factor is then used for scaling the independent time
	series in the fill period, in order to fill the dependent time series.
	</li>
</ol>
</td>
<td>"NearestPoint".
</td>
</tr>

<tr>
<td><b>FillDirection</b> or <b>Direction</b> (deprecated).</td>
<td>
Direction to fill data, "Backward" or "Forward".  If specified, the
"InitialValue" parameter will be used to substitute for missing data in the
filled time series to compute the initial factor.  This property is only used
when FactorMethod=NearestPoint
</td>
<td>"Forward".
</td>
</tr>

<tr>
<td><b>Divisor</b></td>
<td>
Indicate which time series should be used as the divisor (bottom) when computing
the ratio, used with FactorMethod=AnalyzeAverage, either "TS" or
"IndependentTS".
</td>
<td>IndependentTS.
</td>
</tr>

<tr>
<td><b>FillFlag</b></td>
<td>
Single character to flag filled data.
</td>
<td>No flag.
</td>
</tr>

<tr>
<td><b>InitialValue</b></td>
<td>
Initial value of the time series to fill, used to compute the factor at the ends
of the period when observations may not be availble.  This property is only used
when FactorMethod=NearestPoint.  Possible values are:
<ol>
<li>	NearestBackward - search backward in time from the first value being
	processed to find a value.</li>
<li>	NearestForeward - search foreward in time from the first value being
	processed to find a value.</li>
<li>	A number - use the specified number as the initial value of the time
	series, when an independent time series value is found and the time
	series value is missing.
</ol>
</td>
<td>None - a missing ration at the end-points will result in no filling.
</td>
</tr>

</table>
@return number of values filled
@exception RTi.TS.Exception if there is a problem filling data.
*/
public static int fillProrate(	TS ts, TS independent_ts, DateTime start_date, DateTime end_date, PropList props )
throws Exception
{	String  routine = "TSUtil.fillProrate";
	String	message;

	// Initialize a blank PropList if necessary...
	if ( props == null ) {
		props = new PropList ( "fillProrate" );
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime FillStart_DateTime = valid_dates.getDate1();
	DateTime FillEnd_DateTime = valid_dates.getDate2();

	boolean AnalyzeAverage_boolean = false;
	// TODO SAM 2007-03-01 Evalaute use
	//boolean NearestPoint_boolean = true;	// default
	String prop = props.getValue ( "FactorMethod" );
	if ( prop == null ) {
	    // Try old name
	    prop = props.getValue ( "CalculateFactorHow" );
	}
	if ( (prop != null) && prop.equalsIgnoreCase("AnalyzeAverage") ) {
		// TODO SAM 2007-03-01 Evaluate use
		//NearestPoint_boolean = false;
		AnalyzeAverage_boolean = true;
	}

	boolean DivisorIndependentTS_boolean = true; // Divisior is IndependentTS
	prop = props.getValue ( "Divisor" );
	if ( (prop != null) && prop.equalsIgnoreCase("TS") ) {
		DivisorIndependentTS_boolean = false;	// Divisior is TS
	}

	boolean Forward_boolean = true;	// default
	prop = props.getValue ( "FillDirection" );
	if ( prop == null) {
	    // Old
	    props.getValue ( "Direction" );
	}
	if ( (prop != null) && prop.equalsIgnoreCase("Backward") ) {
		Forward_boolean = false;
	}

	double InitialValue_double = ts.getMissing();
	boolean InitialValueDouble_boolean = false;
	String InitialValue = props.getValue ( "InitialValue" );
	if ( (InitialValue != null) && StringUtil.isDouble(prop) ) {
		InitialValue_double = StringUtil.atod(prop);
		InitialValueDouble_boolean = true;
	}

	String FillFlag = props.getValue ( "FillFlag" );
	if ( (FillFlag != null) && (FillFlag.length() > 1) ) {
		message = "The fill flag \"" + FillFlag + "\" should be a single character.";
		Message.printWarning ( 3, routine, message );
		throw new Exception ( message );
	}
	boolean FillFlag_boolean = false;	// Indicate whether to use flag
	if ( (FillFlag != null) && (FillFlag.length() > 0) ) {
		FillFlag_boolean = true;
		// Make sure that the data flag is allocated.
		ts.allocateDataFlagSpace (
			null,	// Initial flag value
			true );	// Keep old flags if already allocated
	}

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		message = "Filling IrregularTS by prorating is not supported";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}
	// Loop using addInterval...
	DateTime date = null;
	double data_value = 0.0, independent_data_value = 0.0;
	boolean ratio_found = false;
	double ratio = 0.0;	// The ratio of data_value/independent_data_value.
	int fill_count = 0;	// Counter for data points that are filled.

	if ( AnalyzeAverage_boolean ) {
		DateTime AnalysisStart_DateTime = null;
		DateTime AnalysisEnd_DateTime = null;
		// Precompute the ratio and then loop through...
		prop = props.getValue ( "AnalysisStart" );
		if ( prop == null ) {
			AnalysisStart_DateTime = new DateTime ( ts.getDate1() );
		}
		else {
		    AnalysisStart_DateTime = DateTime.parse(prop);
		}
		prop = props.getValue ( "AnalysisEnd" );
		if ( prop == null ) {
			AnalysisEnd_DateTime = new DateTime ( ts.getDate2() );
		}
		else {
		    AnalysisEnd_DateTime = DateTime.parse(prop);
		}
		// Find the average...
		double ratio_total = 0.0;
		int ratio_count = 0;
		for (	date = new DateTime(AnalysisStart_DateTime);
			date.lessThanOrEqualTo(AnalysisEnd_DateTime);
			date.addInterval ( interval_base, interval_mult) ) {
			data_value = ts.getDataValue ( date );
			independent_data_value = independent_ts.getDataValue ( date );
			if ( !ts.isDataMissing(data_value) && !independent_ts.isDataMissing( independent_data_value) ) {
				if ( DivisorIndependentTS_boolean && (independent_data_value != 0.0) ) {
					ratio = data_value/independent_data_value;
					ratio_total += ratio;
					++ratio_count;
				}
				else if(!DivisorIndependentTS_boolean && (data_value != 0.0) ) {
					ratio = independent_data_value/data_value;
					ratio_total += ratio;
					++ratio_count;
				}
			}
		}
		if ( ratio_count > 0 ) {
			// Average ratio...
			ratio = ratio_total/ratio_count;
			// Loop and fill the data.  No need to worry about direction so process forward.
			for ( date = new DateTime(FillStart_DateTime);
				date.lessThanOrEqualTo(FillEnd_DateTime);
				date.addInterval(interval_base, interval_mult)){
				data_value = ts.getDataValue ( date );
				independent_data_value = independent_ts.getDataValue ( date );
				if ( ts.isDataMissing(data_value) && !ts.isDataMissing( independent_data_value) ) {
					if ( DivisorIndependentTS_boolean ) {
						data_value = independent_data_value*ratio;
					}
					else {
					    data_value = independent_data_value/ratio;
					}
					if ( FillFlag_boolean ) {
						// Set the flag...
						ts.setDataValue ( date, data_value, FillFlag, 1 );
					}
					else {
					    ts.setDataValue ( date, data_value );
					}
					++fill_count;
				}
			}
			if ( fill_count > 0 ) {
				ts.setDescription ( ts.getDescription() + ", fill prorate" );
				String note = "";
				if ( DivisorIndependentTS_boolean ) {
					note = "*";
				}
				else {
				    note = "/";
				}
				ts.addToGenesis ( "Filled " + fill_count + " missing data points " + FillStart_DateTime + " to " +
				FillEnd_DateTime + " by prorating known values from \"" +
				independent_ts.getIdentifierString() + "\" " + note + " average factor " + ratio );
			}
		}
	}
	else {
	    // NearestPoint...
				
    	String loc = ts.getLocation();
    	String dt = ts.getDataType();
    	if ( Forward_boolean ) {
    		// Iterate forward.
    		// Determine whether an initial ratio is available to fill the end points...
    		if ( (InitialValue != null) && InitialValue.equalsIgnoreCase("NearestForward") ) {
    			for (	date = new DateTime(FillStart_DateTime);
    				date.lessThanOrEqualTo( FillEnd_DateTime );
    				date.addInterval(interval_base, interval_mult)){
    				independent_data_value = independent_ts.getDataValue(date);
    				if ( !independent_ts.isDataMissing(
    					independent_data_value) && (independent_data_value != 0.0) ) {
    					// First check to see if there is an actual value in ts...
    					data_value = ts.getDataValue(date);
    					if ( !ts.isDataMissing(data_value) ) {
    						ratio=data_value/independent_data_value;
    						ratio_found = true;
    						Message.printStatus ( 2, routine,
    						loc + " " + dt + " Ratio ts/indepts computed on "
    						+ date + " " + StringUtil.formatString(
    						data_value,"%.6f") + "/" + StringUtil.formatString(
    						independent_data_value,"%.6f")+	"=" + StringUtil.formatString(ratio,"%.6f") );
    						break;
    					}
    					// Next, if an initial value is given for ts, use it...
    					if ( InitialValueDouble_boolean ) {
    						ratio = InitialValue_double/independent_data_value;
    						ratio_found = true;
    						Message.printStatus ( 2,routine, loc + " " + dt +
    						" Ratio ts/indepts computed on " + date + " " +
    						StringUtil.formatString(InitialValue_double,"%.6f")+"/"+
    						StringUtil.formatString(
    						independent_data_value,"%.6f")+	"=" + StringUtil.formatString(ratio,"%.6f") );
    						break;
    					}
    					// Else, keep searching...
    				}
    			}
    		}
    		else if((InitialValue != null) &&
    			InitialValue.equalsIgnoreCase("NearestBackward") ) {
    			// Search backward from the fill start to the start of the time series...
    			DateTime date1 = ts.getDate1();
    			for ( date = new DateTime(FillStart_DateTime);
    				date.greaterThanOrEqualTo( date1 );
    				date.addInterval(interval_base,-interval_mult)){
    				independent_data_value = independent_ts.getDataValue(date);
    				if ( !independent_ts.isDataMissing(
    					independent_data_value) && (independent_data_value != 0.0) ) {
    					// First check to see if there is an actual value in ts...
    					data_value = ts.getDataValue(date);
    					if ( !ts.isDataMissing(data_value) ){
    						ratio=data_value/independent_data_value;
    						// For development...
    						Message.printStatus ( 2,routine, loc + " " + dt +
    						" Ratio ts/indepts computed on " + date + " " +
    						StringUtil.formatString( data_value,"%.6f") + "/" +
    						StringUtil.formatString(independent_data_value,"%.6f") +
    						"=" + StringUtil.formatString(ratio,"%.6f"));
    						ratio_found = true;
    						break;
    					}
    					// Next, if an initial value is given for ts, use it...
    					if ( InitialValueDouble_boolean ) {
    						ratio = InitialValue_double/independent_data_value;
    						// For development...
    						Message.printStatus ( 2,routine, loc + " " + dt +
    						" Ratio ts/indepts computed on " + date + " " +
    						StringUtil.formatString(InitialValue_double,"%.6f")+"/"+
    						StringUtil.formatString(independent_data_value,"%.6f")+
    						"=" + StringUtil.formatString(ratio,"%.6f") );
    						ratio_found = true;
    						break;
    					}
    					// Else, keep searching...
    				}
    			}
    		}
    		// Now fill the data...
    		for ( date = new DateTime(FillStart_DateTime);
    			date.lessThanOrEqualTo( FillEnd_DateTime );
    			date.addInterval(interval_base, interval_mult) ) {
    			data_value = ts.getDataValue ( date );
    			if ( ts.isDataMissing ( data_value ) ) {
    				independent_data_value = independent_ts.getDataValue ( date );
    				if ( ratio_found &&	!independent_ts.isDataMissing(independent_data_value) ) {
    					// Use the ratio to fill.  If no ratio has been found, leave missing...
    					if ( FillFlag_boolean ) {
    						// Set the flag...
    						ts.setDataValue ( date, independent_data_value*ratio, FillFlag, 1 );
    					}
    					else {
    					    ts.setDataValue ( date,	independent_data_value*ratio );
    					}
    					// For development...
    					Message.printStatus ( 2, routine, "Filling " + date + " with val*ratio "+
    					StringUtil.formatString(independent_data_value,"%.6f") + "*" +
    					StringUtil.formatString(ratio,"%.6f") +
    					"=" + StringUtil.formatString(independent_data_value*ratio,"%.6f") );
    					++fill_count;
    				}
    			}
    			else {
    			    // Have a data value.  See if the independent time series also has a value that can be
    				// used to recalculate the ratio.
    				independent_data_value = independent_ts.getDataValue ( date );
    				if ( !independent_ts.isDataMissing(
    					independent_data_value) && (independent_data_value != 0.0) ) {
    					// Recalculate the ratio...
    					ratio = data_value/independent_data_value;
    					// For development...
    					Message.printStatus ( 2, routine, loc + " " + dt +
    					" Ratio ts/indepts computed on " + date+ " " + StringUtil.formatString(
    					data_value,"%.6f") + "/" + StringUtil.formatString(
    					independent_data_value,"%.6f") + "=" + StringUtil.formatString(ratio,"%.6f"));
    					ratio_found = true;
    				}
    			}
    		}
    	}
    	else {
    	    // Iterate backward...
    		// Make sure that an initial ratio is available to fill the end points...
    		if ( (InitialValue != null) && InitialValue.equalsIgnoreCase("NearestBackward") ) {
    			for ( date = new DateTime(FillEnd_DateTime);
    				date.greaterThanOrEqualTo( FillStart_DateTime );
    				date.addInterval(interval_base,-interval_mult)){
    				independent_data_value = independent_ts.getDataValue(date);
    				if ( !independent_ts.isDataMissing(
    					independent_data_value) && (independent_data_value != 0.0) ) {
    					// First check to see if there is an actual value in ts...
    					data_value = ts.getDataValue(date);
    					if ( !ts.isDataMissing(data_value) ){
    						ratio=data_value/independent_data_value;
    						// For development...
    						Message.printStatus ( 2,routine, "Ratio ts/indepts computed on "+
    						date + " " + StringUtil.formatString(data_value,"%.6f") + "/" +
    						StringUtil.formatString(independent_data_value,"%.6f")+
    						"=" + StringUtil.formatString(ratio,"%.6f"));
    						ratio_found = true;
    						break;
    					}
    					// Next, if an initial value is given for ts, use it...
    					if ( InitialValueDouble_boolean ){
    						ratio = InitialValue_double/independent_data_value;
    						// For development...
    						Message.printStatus ( 2,routine, "Ratio ts/indepts computed on "+
    						date + " " + StringUtil.formatString(InitialValue_double,"%.6f")+"/"+
    						StringUtil.formatString( independent_data_value,"%.6f")+
    						"=" + StringUtil.formatString(ratio,"%.6f") );
    						ratio_found = true;
    						break;
    					}
    					// Else, keep searching...
    				}
    			}
    		}
    		else if((InitialValue != null) && InitialValue.equalsIgnoreCase("NearestForward") ) {
    			// Search foreward from the fill end to the end of the time series...
    			DateTime date2 = ts.getDate2();
    			for ( date = new DateTime(FillEnd_DateTime);
    				date.lessThanOrEqualTo( date2 );
    				date.addInterval(interval_base, interval_mult)){
    				independent_data_value = independent_ts.getDataValue(date);
    				if ( !independent_ts.isDataMissing(
    					independent_data_value) && (independent_data_value != 0.0) ) {
    					// First check to see if there is an actual value in ts...
    					data_value = ts.getDataValue(date);
    					if ( !ts.isDataMissing(data_value) ){
    						ratio=data_value/independent_data_value;
    						ratio_found = true;
    						Message.printStatus ( 2, routine, "Ratio ts/indepts computed on "+
    						date + " " + StringUtil.formatString(data_value,"%.6f") + "/" +
    						StringUtil.formatString(independent_data_value,"%.6f")+
    						"=" + StringUtil.formatString(ratio,"%.6f") );
    						break;
    					}
    					// Next, if an initial value is given for ts, use it...
    					if ( InitialValueDouble_boolean ) {
    						ratio = InitialValue_double/independent_data_value;
    						ratio_found = true;
    						Message.printStatus ( 2, routine, "Ratio ts/indepts computed on "+ date + " " +
    						StringUtil.formatString(InitialValue_double,"%.6f")+"/"+
    						StringUtil.formatString(independent_data_value,"%.6f")+
    						"=" + StringUtil.formatString(ratio,"%.6f") );
    						break;
    					}
    					// Else, keep searching...
    				}
    			}
    		}
    
    		// Now do the filling...
    		for ( date = new DateTime(FillEnd_DateTime);
    			date.greaterThanOrEqualTo( FillStart_DateTime );
    			date.addInterval(interval_base, -interval_mult) ) {
    			data_value = ts.getDataValue ( date );
    			if ( ts.isDataMissing ( data_value ) ) {
    				independent_data_value = independent_ts.getDataValue ( date );
    				if ( ratio_found &&	!independent_ts.isDataMissing(independent_data_value) ) {
    					// Use the ratio to fill.  If no ratio has been found, leave missing...
    					if ( FillFlag_boolean ) {
    						// Set the flag...
    						ts.setDataValue ( date, independent_data_value*ratio, FillFlag, 1 );
    					}
    					else {
    					    ts.setDataValue ( date, independent_data_value*ratio );
    					}
    					// For development...
    					Message.printStatus ( 2, routine, "Filling " + date + " with val*ratio "+
    					StringUtil.formatString( independent_data_value,"%.6f") + "*" +
    					StringUtil.formatString(ratio,"%.6f") + "=" + StringUtil.formatString(
    					independent_data_value*ratio,"%.6f") );
    					++fill_count;
    				}
    			}
    			else {
    			    // Have a data value.  See if the independent time series also has a value that can be
    				// used to recalculate the ratio.
    				independent_data_value = independent_ts.getDataValue ( date );
    				if ( !independent_ts.isDataMissing( independent_data_value) && (independent_data_value != 0.0) ) {
    					// Recalculate the ratio...
    					ratio = data_value/independent_data_value;
    					// For development...
    					Message.printStatus ( 2, routine, "Ratio ts/indepts computed on " + date + " " +
    					StringUtil.formatString(data_value,"%.6f") + "/" + StringUtil.formatString(
    					independent_data_value,"%.6f") + "=" + StringUtil.formatString(ratio,"%.6f"));
    					ratio_found = true;
    				}
    			}
    		}
    	}
    
    	// Fill in the genesis information...
    
    	if ( fill_count > 0 ) {
    		if ( Forward_boolean ) {
    			ts.setDescription ( ts.getDescription() + ", fill prorate forward" );
    			ts.addToGenesis ( "Filled " + fill_count + " missing data points " +
    			FillStart_DateTime + " to " + FillEnd_DateTime +
    			" by prorating known values (forward) from \"" + independent_ts.getIdentifierString() + "\"" );
    		}
    		else {
    		    ts.setDescription ( ts.getDescription() + ", fill prorate backward" );
    			ts.addToGenesis ( "Filled " + fill_count + " missing data points " +
    			FillStart_DateTime + " to " + FillEnd_DateTime +
    			" by prorating known values (backward) from \"" + independent_ts.getIdentifierString() + "\"" );
    		}
    	}
	} // NearestPoint
	
	return fill_count;
}

/**
Fill the missing data within a time series with values based on ordinary least squares, MOVE1, or MOVE2.
@return An instance of TSRegression containing the regression information
(references to inputs and statistical results).
@param tsToFill Time series to fill.
@param tsIndependent Independent time series.
@param tsRegression A previously computed TSRegression object - if specified as non-null it will be
used rather than computing the regression information from other parameters.  This may be useful for performance
reasons and memory management.
@param analysisMethod analysis method to use for the analysis
@param numberOfEquatiosn the number of equations (1 or 12 for monthly analysis)
@param intercept can be set to zero for OLS regression, ignored otherwise
@param analysisMonths an array of months (values 1 to 12) that indicate the months to analyze.  If one equation
is used, then only data for the specified months will be used.
@param transformation the transformation to apply to data.
@param leZeroLogValue if the log tranform is used this is the value used to replace values <= zero (if null
use the default given by TSRegression.getDefaultLEZeroLogValue() and NaN will cause the values to be discarded.
@param minimumDataCount the minimum number of overlapping values in a sample to allow filling.  If null,
don't have a minimum (other than constraints calculating the statistics).
@param minimumR the minimum correlation coefficient to have a relationship and allow filling.  If null,
don't have a minimum.
@param confidenceInterval the confidence interval % to impose - relationships that do not pass the
corresponding level T-test (not a strong relationship) are not used.  If null, the confidence interval
is not examined.
@param dependentAnalysisStart date/time to start the analysis of the dependent time series.
@param dependentAnalysisEnd date/time to end the analysis of the dependent time series.
@param independentAnalysisStart date/time to start the analysis of the independent time series.
@param independentAnalysisEnd date/time to end the analysis of the independent time series.
@param fillStart Date/time to start filling (this is used for the
analysis period for MOVE1 and OLS - MOVE2 uses TSRegression properties for analysis periods).
@param fillEnd Date/time to end filling (this is used for the
analysis period for MOVE1 and OLS - MOVE2 uses TSRegression properties for analysis periods).
@param fillFlag a string to mark filled values with
@param descriptionString a string to append to the existing time series description.
@param doFill if true, then fill the depending time series; if false, compute the relationships and
statistics, but do not actually fill the time series
@exception RTi.TS.TSException if there is a problem performing regression.
*/
public static TSRegression fillRegress ( TS tsToFill, TS tsIndependent,
    TSRegression tsRegression, RegressionType analysisMethod,
    NumberOfEquationsType numberOfEquations, Double intercept, int [] analysisMonths,
    DataTransformationType transformation, Double leZeroLogValue,
    Integer minimumDataCount, Double minimumR, Double confidenceInterval,
    DateTime dependentAnalysisStart, DateTime dependentAnalysisEnd,
    DateTime independentAnalysisStart, DateTime independentAnalysisEnd,
    DateTime fillStart, DateTime fillEnd,
    String fillFlag, String descriptionString, boolean doFill )
throws TSException, Exception
{	String routine = "TSUtil.fillRegress";
	String message;

	if ( Message.isDebugOn ) {
		Message.printDebug ( 20, routine, "In fillRegress." );
	}

	int interval_base = tsToFill.getDataIntervalBase();
	int interval_mult = tsToFill.getDataIntervalMult();

	if ( interval_base == TimeInterval.IRREGULAR ) {
		message="Analysis is not available for irregular time series.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	if ( (interval_base != tsIndependent.getDataIntervalBase()) ||
		(interval_mult != tsIndependent.getDataIntervalMult()) ) {
		message = "Analysis only available for same data interval.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	if ( numberOfEquations == null ) {
	    numberOfEquations = NumberOfEquationsType.ONE_EQUATION; // default
	}
	// The following throws TSException if there is an error...
	if ( numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS ) {
		return fillRegressMonthly ( tsToFill, tsIndependent, tsRegression,
	        analysisMethod, intercept, analysisMonths, transformation,
	        leZeroLogValue, minimumDataCount, minimumR, confidenceInterval,
	        dependentAnalysisStart, dependentAnalysisEnd,
	        independentAnalysisStart, independentAnalysisEnd,
	        fillStart, fillEnd, fillFlag, descriptionString, doFill );
	}
	else {
	    return fillRegressTotal ( tsToFill, tsIndependent, tsRegression,
            analysisMethod, intercept, analysisMonths, transformation,
            leZeroLogValue, minimumDataCount, minimumR, confidenceInterval,
            dependentAnalysisStart, dependentAnalysisEnd,
            independentAnalysisStart, independentAnalysisEnd,
            fillStart, fillEnd, fillFlag, descriptionString, doFill );
	}
}

/**
Fill the missing data within the time series with values based on monthly
equations.  Twelve relationships are determined.
@param tsToFill Time series to fill.
@param tsIndependent Independent time series.
@param tsRegression if null, then regression relationships will be determined using the specified parameters.
If not null, an existing regression relationship will be used.  This allows a previously calculated A, B to be
used rather than recalculating using data that may have been filled in some way.
@param analysisMethod the regression analysis method to use
@param intercept can be specified as zero for OLS regression to force the relationship through zero, not used with
other methods
@param analysisMonths if using one relationship, this specifies that only the specified months (array of values 1-12)
should be considered to develop the relationship, and only those months will be filled.  If monthly relationships, only
the specific months will be analyzed and filled.
@param transformation the data transformation to apply before analyzing.
@param leZeroLogValue if the log tranform is used this is the value used to replace values <= zero (if null
use the default given by TSRegression.getDefaultLEZeroLogValue() and NaN will cause the values to be discarded.
@param minimumDataCount the minimum number of overlapping values in a sample to allow filling.  If null,
don't have a minimum (other than constraints calculating the statistics).
@param minimumR the minimum correlation coefficient to have a relationship and allow filling.  If null,
don't have a minimum.
@param confidenceInterval the confidence interval % to impose - relationships that do not pass the
corresponding level T-test (not a strong relationship) are not used.  If null, the confidence interval
is not examined.
@param dependentAnalysisStart the analysis start for the dependent time series, used by all methods.
@param dependentAnalysisEnd the analysis end for the dependent time series, used by all methods.
@param independentAnalysisStart the analysis start for the independent time series, used by the MOVE2 method.
@param independentAnalysisEnd the analysis end for the independent time series, used by the MOVE2 method.
@param fillStart date/time to start filling 
@param fillEnd date/time to end filling
@param fillFlag the flag to use for filled data values
@param descriptionString the string to append to the time series description, if any values are filled
@param doFill if true, then fill the depending time series; if false, compute the relationships and
statistics, but do not actually fill the time series
@exception Exception if there is an error performing the regression.
*/
private static TSRegression fillRegressMonthly ( TS tsToFill, TS tsIndependent, TSRegression tsRegression,
    RegressionType analysisMethod, Double intercept, int [] analysisMonths,
    DataTransformationType transformation, Double leZeroLogValue,
    Integer minimumDataCount, Double minimumR, Double confidenceInterval,
    DateTime dependentAnalysisStart, DateTime dependentAnalysisEnd,
    DateTime independentAnalysisStart, DateTime independentAnalysisEnd,
    DateTime fillStart, DateTime fillEnd,
	String fillFlag, String descriptionString, boolean doFill )
throws TSException, Exception
{	String routine = "TSUtil.fillRegressMonthly";
	String message;
	int	dl = 50; // Debug level

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "In fillRegressMonthly." );
	}

	int interval_base = tsToFill.getDataIntervalBase();
	int interval_mult = tsToFill.getDataIntervalMult();

	if ( (interval_base != tsIndependent.getDataIntervalBase()) ||
		(interval_mult != tsIndependent.getDataIntervalMult()) ) {
		message="Analysis only available for same data interval.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	boolean regressLog = false;

	boolean fillFlag_boolean = false;	// Indicate whether to use flag
	if ( (fillFlag != null) && (fillFlag.length() > 0) ) {
		fillFlag_boolean = true;
		// Make sure that the data flag is allocated.
		tsToFill.allocateDataFlagSpace (
			null,	// Initial flag value
			true );	// Keep old flags if already allocated
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod( tsToFill, fillStart, fillEnd );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	TSRegression rd;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Getting TSRegression data.");
	}
	if ( tsRegression != null ) {
	    // Use an existing relationship
	    rd = tsRegression;
	}
	else {
	    // Compute the regression relationship
    	try {
    	    rd = new TSRegression ( tsIndependent, tsToFill, true, analysisMethod,
    	            intercept, NumberOfEquationsType.MONTHLY_EQUATIONS, analysisMonths,
    	            transformation, leZeroLogValue, confidenceInterval,
    	            dependentAnalysisStart, dependentAnalysisEnd,
    	            independentAnalysisStart, independentAnalysisEnd, fillStart, fillEnd );
    	}
    	catch ( Exception e ) {
    		message="Unable to complete analysis (" + e + ").";
    		Message.printWarning ( 3, routine, message );
    		Message.printWarning ( 3, routine, e );
    		throw new TSException ( message );
    	}
	}
	
	if ( !doFill ) {
	    // Return without filling the time series
	    return rd;
	}
		
	double newval = 0.0, x = 0.0;
	int fillCount = 0; // To know whether to add to genesis
	int month = 0, iMonth; // Used for analysisMonths check
	boolean foundMonth = false; // Used for analysisMonths check
	for ( DateTime date = new DateTime ( start ); date.lessThanOrEqualTo( end );
		date.addInterval(interval_base, interval_mult) ) {
		try {
		    // TODO SAM - need to evaluate this - use isAnalyzed() to improve performance
    		if ( tsToFill.isDataMissing(tsToFill.getDataValue(date)) ) {
    			// Try to fill the value...
    			x = tsIndependent.getDataValue ( date );
    			if ( !tsIndependent.isDataMissing(x)) {
    			    // Skip the month if not requested
    	            if ( analysisMonths != null ) {
    	                foundMonth = false;
    	                month = date.getMonth();
    	                for ( iMonth = 0; iMonth < analysisMonths.length; iMonth++ ) {
    	                    if ( month == analysisMonths[iMonth] ) {
    	                        foundMonth = true;
    	                        break;
    	                    }
    	                }
    	                if ( !foundMonth ) {
    	                    continue;
    	                }
    	            }
    				if ( Message.isDebugOn ) {
    					Message.printDebug ( dl, routine, "Found nonmissing independant data at " + date + " - value: " + x  );
    				}
    				if ( regressLog ) {
    					// Need to work on the log of the X value...
    					if ( x <= 0.0 ) {
    						// .001 observed
    						x = -3.00;
    					}
    					else {
    					    x = Math.log10(x);
    					}
    					if ( Message.isDebugOn ) {
    						Message.printDebug ( dl, routine, "Using log value: " + x);
    					}
    				}
    
    				newval = rd.getA(date.getMonth()) + rd.getB(date.getMonth())*x;
    
    				if ( Message.isDebugOn ) {
    					Message.printDebug ( dl, routine, "Calculated dependent value: " + newval );
    				}
    
    				if ( regressLog ) {
    					// Now convert Y back from log10 space...
    					if ( Message.isDebugOn ) {
    						Message.printDebug ( dl, routine, "Must use inverse log for " + newval );
    					}
    					if ( fillFlag_boolean ) {
    						// Set the flag...
    						tsToFill.setDataValue ( date, Math.pow ( 10, newval ), fillFlag, 1 );
    					}
    					else {
    					    // No flag...
    						tsToFill.setDataValue ( date, Math.pow ( 10, newval ));
    					}
    				}
    				else {
    				    if ( fillFlag_boolean ) {
    						// Set the flag...
    						tsToFill.setDataValue(date, newval, fillFlag, 1 );
    					}
    					else {
    					    // No flag...
    						tsToFill.setDataValue(date, newval );
    					}
    				}
    				// Increment the counter on the number of values filled
    				++fillCount;
    			}
    		}
		}
		catch ( Exception e ) {
			// Error filling interval but just continue.  The error
			// is most likely because the month did not have regression relationships.
			;
		}
	}

	// Fill in the genesis information...

	if ( fillCount > 0 ) {
    	tsToFill.addToGenesis ( "Filled " + fillCount + " missing values " + start + " to " + end + " using analysis results:" );
    
    	// The following comes back as multiple strings but to handle genesis
    	// information nicely, break into separate strings...
    
    	List<String> strings = StringUtil.breakStringList ( rd.toString(),
    		System.getProperty("line.separator"), StringUtil.DELIM_SKIP_BLANKS );
    	if ( strings != null ) {
    		int size = strings.size();
    		for ( int j = 0; j < size; j++ ) {
    			tsToFill.addToGenesis( (String)strings.get(j) );
    		}
    	}
    
    	if ( descriptionString != null ) {
    		// Description has been specified...
    		tsToFill.setDescription ( tsToFill.getDescription() + descriptionString );
    	}
    	else {
    	    // Automatically add to the description...
    		if ( analysisMethod == null ) {
    			// Default is OLS regression...
    			analysisMethod = RegressionType.OLS_REGRESSION;
    		}
    		else {
    		    String monthString = "monthly";
    		    if ( (analysisMonths != null) && (analysisMonths.length == 1) ) {
    		        // Filling one month so be specific in the description
    		        monthString = TimeUtil.monthAbbreviation(analysisMonths[0]);
    		    }
    		    if ( regressLog ) {
    				tsToFill.setDescription ( tsToFill.getDescription()+
    				", fill log " + analysisMethod + " " + monthString + " using " + tsIndependent.getIdentifierString() );
    			}
    			else {
    			    tsToFill.setDescription ( tsToFill.getDescription() +
    				", fill " + analysisMethod + " " + monthString + " using " + tsIndependent.getIdentifierString() );
    			}
    		}
    	}
	}
	
	// Return the regression information...

	return rd;
}

/**
Fill the missing data within the time series with values based on an ordinary least squares,
MOVE1, or MOVE2 analysis.
@param tsToFill Time series to fill.
@param tsIndependent Independent time series.
@param leZeroLogValue if the log tranform is used this is the value used to replace values <= zero (if null
use the default given by TSRegression.getDefaultLEZeroLogValue() and NaN will cause the values to be discarded.
@param minimumDataCount the minimum number of overlapping values in a sample to allow filling.  If null,
don't have a minimum (other than constraints calculating the statistics).
@param minimumR the minimum correlation coefficient to have a relationship and allow filling.  If null,
don't have a minimum.
@param confidenceInterval the confidence interval % to impose - relationships that do not pass the
corresponding level T-test (not a strong relationship) are not used.  If null, the confidence interval
is not examined.
@param fill_period_start Date/time to start filling (this is used for the
analysis period for MOVE1 and OLS - MOVE2 uses TSRegression properties for analysis periods).
@param fill_period_end Date/time to end filling (this is used for the
analysis period for MOVE1 and OLS - MOVE2 uses TSRegression properties for analysis periods).
@param prop_list Properties to control filling.  See the TSRegression properties.
@param descriptionString will be appended to the description (if not set, "fill regress using TSID" or
"fill log regress using TSID" will be used).
@param doFill if true, then fill the depending time series; if false, compute the relationships and
statistics, but do not actually fill the time series
@exception Exception if there is a problem doing regression.
*/
private static TSRegression fillRegressTotal ( TS tsToFill, TS tsIndependent, TSRegression tsRegression,
    RegressionType analysisMethod, Double intercept, int [] analysisMonths,
    DataTransformationType transformation,  Double leZeroLogValue,
    Integer minimumDataCount, Double minimumR, Double confidenceInterval,
    DateTime dependentAnalysisStart, DateTime dependentAnalysisEnd,
    DateTime independentAnalysisStart, DateTime independentAnalysisEnd,
    DateTime fillStart, DateTime fillEnd,
    String fillFlag, String descriptionString, boolean doFill )
throws TSException, Exception
{	String message, routine = "TSUtil.fillRegressTotal";
	boolean regressLog = false;

	if ( Message.isDebugOn ) {
		Message.printDebug ( 20, routine, "In fillRegress." );
	}

	int interval_base = tsToFill.getDataIntervalBase();
	int interval_mult = tsToFill.getDataIntervalMult();

	if ( (interval_base != tsIndependent.getDataIntervalBase()) ||
		(interval_mult != tsIndependent.getDataIntervalMult()) ) {
		message="Analysis only available for same data interval.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	boolean fillFlag_boolean = false;	// Indicate whether to use flag
	if ( (fillFlag != null) && (fillFlag.length() > 0) ) {
		fillFlag_boolean = true;
		// Make sure that the data flag is allocated.
		tsToFill.allocateDataFlagSpace (
			null,	// Initial flag value
			true );	// Keep old flags if already allocated
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod( tsToFill, fillStart, fillEnd );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	TSRegression rd;
    if ( tsRegression != null ) {
        // Use previous analysis results
        rd = tsRegression;
    }
    else {
        // Compute the resgression relationship
    	if ( Message.isDebugOn ) {
    		Message.printDebug ( 10, routine, "Analyzing data." );
    	}
    	try {
    	    rd = new TSRegression (	tsIndependent, tsToFill, true, analysisMethod,
                intercept, NumberOfEquationsType.ONE_EQUATION, analysisMonths, transformation,
                leZeroLogValue, confidenceInterval,
                dependentAnalysisStart, dependentAnalysisEnd,
                independentAnalysisStart, independentAnalysisEnd,
                fillStart, fillEnd );
    	}
    	catch ( Exception e ) {
    		message = "Error analyzing regression relationship (" + e + ").";
    		Message.printWarning ( 3, routine, message );
    		Message.printWarning ( 3, routine, e );
    		throw new TSException ( message );
    	}
    }
    
    if ( !doFill ) {
        // Return without filling the time series
        return rd;
    }
		
	double newval = 0.0, x = 0.0;
	int fillCount = 0;
	boolean foundMonth = false; // Used for analysisMonths check
	int month, iMonth; // Used for analysisMonths check
	for ( DateTime date = new DateTime ( start ); date.lessThanOrEqualTo( end );
		date.addInterval(interval_base, interval_mult) ) {
		try {
		    // Catch an error for the interval.  It is most likely due to not having analysis data...
			// TODO SAM 2010-06-10 - need to evaluate this - use isAnalyzed() to improve performance
    		if ( tsToFill.isDataMissing(tsToFill.getDataValue(date) ) ) {
    			x = tsIndependent.getDataValue ( date );
    			if ( !tsIndependent.isDataMissing(x)) {
    		        // Skip the month if not requested
    	            if ( analysisMonths != null ) {
    	                foundMonth = false;
    	                month = date.getMonth();
    	                for ( iMonth = 0; iMonth < analysisMonths.length; iMonth++ ) {
    	                    if ( month == analysisMonths[iMonth] ) {
    	                        foundMonth = true;
    	                        break;
    	                    }
    	                }
    	                if ( !foundMonth ) {
    	                    continue;
    	                }
    	            }
    				if ( Message.isDebugOn ) {
    					Message.printDebug ( 10, routine, "Found nonmissing independent data " + x + " at " + date );
    				}
    				if ( regressLog ) {
    					if ( x <= 0.0 ) {
    						// .001 observed
    						x = -3.00;
    					}
    					else {
    					    x = Math.log10(x);
    					}
    				}
    
    				newval = rd.getA() + rd.getB()*x;
    
    				if ( regressLog ) {
    					if ( fillFlag_boolean ) {
    						// Use data flag...
    						tsToFill.setDataValue ( date, Math.pow ( 10, newval ), fillFlag, 1);
    					}
    					else {
    					     // No data flag...
    						tsToFill.setDataValue ( date, Math.pow ( 10, newval ));
    					}
    				}
    				else {
    				    if ( fillFlag_boolean ) {
    						// Use data flag...
    						tsToFill.setDataValue ( date, newval, fillFlag, 1 );
    					}
    					else {
    					    tsToFill.setDataValue ( date, newval);
    					}
    				}
    				
    				// Count how many values are filled so that genesis, etc. is only appended to when
    				// 1+ values are filled.
    				++fillCount;
    			}
    		}
		}
		catch ( Exception e ) {
			// Error filling interval but just continue.  The error
			// is most likely because the month did not have analysis relationships.
			;
		}
	}

	// Fill in the genesis information...

	if ( fillCount > 0 ) {
    	tsToFill.addToGenesis ( "Filled " + fillCount + " missing values " + start + " to " + end + " using:" );
    
    	// The following comes back as multiple strings but to handle genesis
    	// information nicely, break into separate strings...
    
    	List<String> strings = StringUtil.breakStringList ( rd.toString(),
    		System.getProperty("line.separator"), StringUtil.DELIM_SKIP_BLANKS );
    	if ( strings != null ) {
    		int size = strings.size();
    		for ( int j = 0; j < size; j++ ) {
    			tsToFill.addToGenesis (strings.get(j) );
    		}
    	}
    
    	if ( descriptionString != null ) {
    		tsToFill.setDescription ( tsToFill.getDescription() + descriptionString );
    	}
    	else {
    	    String prop_value = "" + analysisMethod;
    		if ( analysisMethod == null ) {
    		    // Default
    			prop_value = "" + RegressionType.OLS_REGRESSION;
    		}
    		String id = tsIndependent.getIdentifierString();
    		if ( tsIndependent.getAlias().length() > 0 ) {
    			id = tsIndependent.getAlias();
    		}
    		if ( regressLog ) {
    			tsToFill.setDescription (tsToFill.getDescription() + ", fill log " + prop_value + " using " + id );
    		}
    		else {
    		    tsToFill.setDescription (tsToFill.getDescription() + ", fill " + prop_value + " using " + id );
    		}
    	}
	}

	// Return the regression information...

	return rd;
}

/**
Fill missing data by repeating the last known value, processing either forward or backward.
@param ts Time series to fill.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param direction -1 if filling backward, >= 0 if filling forward.
@param max_intervals Maximum number of intervals to fill in a gap.  Zero indicates to fill all (no maximum).
@param fillFlag flag to mark values that were filled, null or blank to not mark
@param number of values filled
@exception RTi.TS.TSException if there is a problem filling data.
*/
public static int fillRepeat (	TS ts, DateTime start_date, DateTime end_date, int direction, int max_intervals,
    String fillFlag )
{	String routine = "TSUtil.fillRepeat";
	String message;
	if ( (fillFlag != null) && fillFlag.equals("") ) {
	    // Set to null to simplify processing
	    fillFlag = null;
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		message = "Filling IrregularTS by repeating value is not supported";
		Message.printWarning ( 2, routine, message );
		throw new IrregularTimeSeriesNotSupportedException( message );
	}
	// Loop using addInterval...
	DateTime date = null;
	double data_value = 0.0;
	boolean last_found = false;
	double last_found_value = 0.0;
	int	fill_count = 0;	// Number of sequential intervals filled.
	int fillCountTotal = 0; // Total fill count
	TSData dataPoint = new TSData(); // Used when setting the flag
	if ( direction >= 0 ) {
		// Iterate forward...
		for ( date = new DateTime(start); date.lessThanOrEqualTo( end );
			date.addInterval(interval_base, interval_mult) ) {
			data_value = ts.getDataValue ( date );
			if ( ts.isDataMissing ( data_value ) ) {
				if ( last_found ) {
					// Use the last value found to fill.  If no value has been found, leave missing...
					if ( (max_intervals == 0) || (fill_count < max_intervals) ) {
					    if ( fillFlag != null ) {
					        // Update the flag value
		                    dataPoint = ts.getDataPoint(date, dataPoint );
		                    dataPoint.setDataFlag ( fillFlag ); // This will append if fillFlag has +
		                    ts.setDataValue ( date, last_found_value, dataPoint.getDataFlag(), dataPoint.getDuration() );
					    }
					    else {
					        ts.setDataValue ( date, last_found_value );
					    }
						++fill_count;
						++fillCountTotal;
					}
				}
			}
			else {
			    // Save the last data value...
				last_found_value = data_value;
				last_found = true;
				fill_count = 0;
			}
		}
	}
	else {
	    // Iterate backward...
		for ( date = new DateTime(end); date.greaterThanOrEqualTo( start );
			date.addInterval(interval_base, -interval_mult) ) {
			data_value = ts.getDataValue ( date );
			if ( ts.isDataMissing ( data_value ) ) {
				if ( last_found ) {
					// Use the last value found to fill.  If no value has been found, leave missing...
					if ( (max_intervals == 0) || (fill_count < max_intervals) ) {
                        if ( fillFlag != null ) {
                            // Update the flag value
                            dataPoint = ts.getDataPoint(date, dataPoint );
                            dataPoint.setDataFlag ( fillFlag ); // This will append if fillFlag has +
                            ts.setDataValue ( date, last_found_value, dataPoint.getDataFlag(), dataPoint.getDuration() );
                        }
                        else {
                            ts.setDataValue ( date, last_found_value );
                        }
						++fill_count;
						++fillCountTotal;
					}
				}
			}
			else {
			    // Save the last data value...
				last_found_value = data_value;
				last_found = true;
				fill_count = 0;
			}
		}
	}

	// Fill in the genesis information...

	String maxString = " up to gap of " + max_intervals  + " intervals.";
	if ( max_intervals == 0 ) {
	    maxString = " no limit in gap size to fill.";
	}
	String dir = "forward";
	if ( direction < 0 ) {
	    dir = "backward";
	}
    if ( fillCountTotal > 0 ) {
        ts.setDescription ( ts.getDescription() + ", fill repeat " + dir );
    }
    if ( fillCountTotal > 0 ) {
        String flag = StringUtil.remove(fillFlag,"+");
        message = "Filled " + fillCountTotal + " missing data values " + start +
        " to " + end + " by repeating " + dir + " known values," + maxString;
        ts.addDataFlagMetadata(new TSDataFlagMetadata( flag, message));
        ts.addToGenesis ( message );
    }
    
    return fillCountTotal;
}

/**
@return The nearest non-missing data value.  The search will not go past the
end of the time series.  Return the missing data value if no non-missing value
can be found.  If an irregular time series, always return missing (not implemented for irregular time series).
@param ts Time series to search.
@param date Starting date for search.
@param direction If zero or negative, then for each time step search backwards
first.  If positive, search forward first.
@param nforward Indicates how many time steps to search forward.  If zero, there
is no limitation on how far forward to search.
@param nback Indicates how many time steps to search backward.  If zero, there
is no limitation on how far back to search.
*/
public static double findNearestDataValue (	TS ts, DateTime date, int direction, int nforward, int nback )
{	if ( ts == null ) {
		return -999.0;
	}
	if ( date == null ) {
		return ts.getMissing();
	}
	if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
		throw new IrregularTimeSeriesNotSupportedException("Unable to find data nearest " + date +
		    " for irregular time series " + ts.getIdentifierString() );
	}

	DateTime forward_date = new DateTime(date);
	DateTime back_date = new DateTime(date);
	DateTime date1 = ts.getDate1();
	DateTime date2 = ts.getDate2();
	double value = ts.getMissing();
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	boolean back_done = false;
	int count = 0;
	boolean forward_done = false;
	while ( true ) {
		// First get the value, depending on the criteria...
		if ( direction <= 0 ) {
			// Check backwards first...
			if ( !back_done ) {
				value = ts.getDataValue ( back_date );
				if ( !ts.isDataMissing(value) ) {
					break;
				}
			}
			if ( !forward_done ) {
				value = ts.getDataValue ( forward_date );
				if ( !ts.isDataMissing(value) ) {
					break;
				}
			}
		}
		else {
		    // Check forwards first...
			if ( !forward_done ) {
				value = ts.getDataValue ( forward_date );
				if ( !ts.isDataMissing(value) ) {
					break;
				}
			}
			if ( !back_done ) {
				value = ts.getDataValue ( back_date );
				if ( !ts.isDataMissing(value) ) {
					break;
				}
			}
		}
		// Now increment the dates...
		++count;
		forward_date.addInterval ( interval_base, interval_mult );
		back_date.addInterval ( interval_base, -interval_mult );
		if (	forward_date.greaterThan(date2) ||
			// Revisit Precision
			((nforward != 0) && (count > nforward)) ) {
			forward_done = true;
		}
		if (	back_date.lessThan(date1) ||
			// Revisit Precision				
			((nback != 0) && (count > nback)) ) {
			back_done = true;
		}
		if ( back_done && forward_done ) {
			// Nothing found...
			break;
		}
	}

	// By here we either have a value or it is set to missing...
	return value;
}

/**
Returns the nearest TSData point at which non-missing data is found.  If
there are no non-missing data points found then null is returned.
@param inputTS Time series object to iterate.
@param date1 First DateTime of the iteration. If null, uses start date set for the given TS.
@param date2 Last DateTime of the iteration.  If null, uses end date set for the given TS.
@param reverse If true then the iteration will go from date2 to date1.  If
false then iteration starts at date1 and finishes at date2.
@return
 */
public static TSData findNearestDataPoint(TS inputTS, DateTime date1, DateTime date2, boolean reverse)
{
	if( inputTS == null ) {
		return null;
	}
	TSData nearestPoint = null;
	String routine = "TSUtil.findNearestDataPoint";
	TSIterator iter = null;
	
	// check date parameters and if null set to TS dates
	if( date1 == null ) {
		date1 = inputTS.getDate1();
	}
	if( date2 == null ) {
		date2 = inputTS.getDate2();
	}
	
	// setup the iterator for the TS
	try {
		iter = inputTS.iterator(date1, date2);
	} catch (Exception e) {
		Message.printWarning(3, routine, e);
		return null;
	}
	
	double value;
	TSData data = null;
	//Iterate 
	if( reverse ) {
		for ( ; (data = iter.previous()) != null; ) {
		    value = iter.getDataValue();
		    // Found nearest non-missing value
		    if( ! inputTS.isDataMissing( value ) ) {
		    	nearestPoint = data;
		    	break;
		    }
		}
	}
	else {
		for ( ; (data = iter.next()) != null; ) {
		    value = iter.getDataValue();
		    // Found nearest non-missing value
		    if( ! inputTS.isDataMissing( value ) ) {
		    	nearestPoint = data;
		    	break;
		    }
		}
	}
	return nearestPoint;
}

// TODO SAM - when is this ever used?
/**
Returns the index of the time series within the vector of time series
whose location or full id (depending upon the PropList) matches the
key passed in the parameter list.  Matching just the location is the default.
PropList values can be "MatchLocation=true" or "MatchFullID=true"
*/
public static int findTSIndex ( List<TS> tsVector, String key, PropList prop_list )
{
	PropList props;
	if ( prop_list == null ) {
		props = new PropList ( "findTS" );
	}
	else {
	    props = prop_list;
	}

	TSIdent ident = null;
	try {
	    ident = new TSIdent ( key );
	}
	catch ( Exception e ) {
		return -999;
	}
	// TODO SAM 2007-03-01 Evaluate use
	//boolean matchLocation = false;
	boolean matchFullID = false;
	String prop_value, token;

	//
	// convert key depending on the proplist
	//
	prop_value = props.getValue ( "MatchFullId" );
	if ( prop_value != null && prop_value.equals("true")) {
		token = ident.getIdentifier();	
		matchFullID = true;
	}
	else {
	    // "MatchLocation"
		token = ident.getLocation();	
		// TODO SAM 2007-03-01 Evaluate use
		//matchLocation = true;
	}

	int size = tsVector.size();
	for ( int i=0; i<size; i++ ) {
		TS ts = (TS)tsVector.get(i);
		if ( matchFullID ) {
			if (ts.getIdentifierString().equalsIgnoreCase(token)) {
				return i;
			}
		}
		else if (ts.getLocation().equalsIgnoreCase(token)) {
			return i;
		}
	}
	return -999;
}

/**
Format output for a vector of time series, resulting in a standard text
output.  This routine calls the formatOutput() member functions for each time
series (some of which may not be implemented at this time).  Null time series are skipped.
@return The lsit of strings for the complete output.
@param tslist List of time series to format.
@param proplist List of properties to modify output.  This list will be passed
to the lower-level routines and will be modified as necessary (e.g. if the
overall output is only supposed to have one set of header, comments, and notes,
but a summary for each time series).  Valid properties are:
<p>

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>OutputEnd</b></td>
<td>
The ending date/time for output, in a format that can be parsed by
DateTime.parse().
</td>
<td>null - output all available data.
</td>
</tr>

<tr>
<td><b>OutputStart</b></td>
<td>
The starting date/time for output, in a format that can be parsed by
DateTime.parse().
</td>
<td>null - output all available data.
</td>
</tr>

</table>
@see MinuteTS#formatOutput
@see MonthTS#formatOutput
@see HourTS#formatOutput
@see YearTS#formatOutput
@see IrregularTS#formatOutput
@exception RTi.TS.TSException Thrown if a lower-level routine throws an exception.
*/
public static List<String> formatOutput ( List<TS> tslist, PropList proplist )
throws TSException
{
	// Call the main utility version...

	return formatOutput ( (PrintWriter)null, (String)null, tslist, proplist );
}

/**
Version to take a single time series.
@param ts Time series to format.
@param proplist Property list to control formatting.
@exception TSException if there is a problem formatting output.
*/
public static List<String> formatOutput ( TS ts, PropList proplist )
throws TSException
{
	// Call the main utility version...

	List<TS> tslist = new ArrayList<TS>();
	tslist.add ( ts );
	return formatOutput ( (PrintWriter)null, (String)null, tslist, proplist );
}

/**
Version to write output to a file.
@return List of formatted output.
@param fname Name of file to receive output.  File is closed at output.
@param tslist list of time series to process.
@param proplist Properties to modify output.
@exception RTi.TS.TSException Thrown if there is an exception in the low-level code.
*/
public static List<String> formatOutput ( String fname, List<TS> tslist, PropList proplist )
throws TSException
{
	// Call the main utility version...

	return formatOutput ( (PrintWriter)null, fname, tslist, proplist );
}

/**
Version to write output to a Writer.
@return List of formatted output.
@param fp PrintWrither to receive output.
@param tslist list of time series to process.
@param proplist Properties to modify output.
@exception RTi.TS.TSException Thrown if there is an exception in the low-level code.
*/
public static List<String> formatOutput ( PrintWriter fp, List<TS> tslist, PropList proplist )
throws TSException
{
	// Call the main utility version...

	return formatOutput ( fp, (String)null, tslist, proplist );
}

/**
This is the main routine for formatOutput.
@param fp PrintWriter to receive output.  If not null, then output will be written.  If "fp" is null but "fname" is
not, then a writer will be opened.  If both are null, just generate the strings.  This should give a enough flexibility.
@param fname File name to write.
@param tslist List of time series to output.
@param proplist Properties to format the output.
@exception RTi.TS.TSException Thrown if there is an exception in the low-level routines.
*/
private static List<String> formatOutput ( PrintWriter fp, String fname, List<TS> tslist, PropList proplist )
throws TSException
{	String routine = "TSUtil.formatOutput(private)";
	List<String> formatted_output = null;
	int	dl = 20;
	// Get the full path to the file...
	String full_fname = null;
	if ( fname != null ) {
		full_fname = IOUtil.getPathUsingWorkingDir(fname);
	}

	if ( tslist == null ) {
		return formatted_output;
	}

	PropList props = null;
	if ( proplist == null ) {
		// Create an empty list so we do not have to check for null all over the place...
		props = new PropList ( "TSUtil.formatOutput" );
	}
	else {
	    props = proplist;
	}

	// Loop through the time series and generate a summary for each time series and append to the vector...

	// Need to add properties and corresponding code to only print one header, etc.

	int	size = tslist.size();
	TS ts = null;
	for ( int i = 0; i < size; i++ ) {
		ts = tslist.get(i);
		if ( ts == null ) {
			continue;
		}
		try {
			formatted_output = StringUtil.addListToStringList ( formatted_output, ts.formatOutput(props) );
		}
		catch ( TSException e ) {
			throw e;
		}
	}

	// Now check to see if we are supposed to write to a writer...

	boolean opened_here = false; // Indicates whether Writer was opened in this routine.
	if ( fp == null ) {
		// Check to see if a filename has been specified...
		if ( full_fname != null ) {
			try {
			    fp = new PrintWriter ( new FileWriter(full_fname) );
				opened_here = true;
			}
			catch ( Exception e ) {
				String message = "Unable to open file \"" + full_fname + "\"";
				opened_here = false;
				throw new TSException ( message );
			}
		}
	}
	if ( fp != null ) {
		// Writer has been specified or opened in the previous step so write to it...
		if ( formatted_output != null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Formatted output is " + formatted_output.size() + " lines" );
			}
	
			// Now write each string to the writer...

			String newline = System.getProperty ( "line.separator");
			size = formatted_output.size();
			for ( int i = 0; i < size; i++ ) {
				fp.print ( formatted_output.get(i) + newline );
			}
		}
		// If we opened the Writer here, close it...
		if ( opened_here ) {
			fp.close ();
		}
	}

	// Always return the strings...

	return formatted_output;
}

//------------------------------------------------------------------------------
// TSUtil.getDataLimits - get the data limits for the time series
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:	(1)	This used to be calcMaxMinValues.
//------------------------------------------------------------------------------
// History:
// 
// 06 Jan 1998	Steven A. Malers,	Split out of calcMaxMinValues.
//		Riverside Technology,
//		inc.
// 09 Jan 1998	SAM, RTi		Move from the individual time series
//					classes to the TSUtil library.
// 20 Aug 1998	SAM, RTi		Optimize by only creating dates when
//					they need to be saved and add an extra
//					loop to search backward to find the
//					last non-missing data point.
//------------------------------------------------------------------------------
// Variables:	I/O	Description		
//
// refresh_flag	I	If false, do not call refresh (because this routine is
//			being called by refresh and we don't want to get into
//			a circular reference).
//------------------------------------------------------------------------------

/**
@return The data limits for a time series between two dates.
@param ts Time series of interest.
@param start Starting date for the check.
@param end Ending date for the check.
@see TSLimits
*/
public static TSLimits getDataLimits ( TS ts, DateTime start, DateTime end )
{	return getDataLimits ( ts, start, end, false );
}

/**
@return The data limits for a time series between two dates.
@param ts Time series of interest.
@param start0 Starting date for the check.
@param end0 Ending date for the check.
@param refresh_flag Indicates whether the time series should be refreshed first
(in general this is used only within the TS package and the version of this
routine without the flag should be called).  If calling from a TS class
refresh() method, the flag should be false.
@see TSLimits
*/
protected static TSLimits getDataLimits ( TS ts, DateTime start0, DateTime end0, boolean refresh_flag )
{	String routine="TSUtil.getDataLimits";
	double max = 1.0, mean = 0.0, min = 0.0, sum = 0.0, value = 0.0;
	int base=0, missing_count = 0, mult = 0, non_missing_count = 0;
	boolean found = false;
	DateTime date, max_date = null, min_date = null, non_missing_data_date1 = null, non_missing_data_date2 = null, t = null;

	//Message.printStatus ( 2, routine, "Getting limits from " + start0 + " to " + end0 );

	// TODO WHY DOESN'T THIS JUST INSTANTIATE A TSLimits AND LET IT FILL IN THE VALUES - SAM 2001-03-22???
	// The code seems to be redundant

	if ( ts == null ) {
		Message.printWarning ( 2, routine, "NULL time series" );
		return new TSLimits();
	}

	// Initialize the sum and the mean...

	double missing = ts.getMissing();
	sum = missing;
	mean = missing;

	// Get valid date limits because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start0, end0 );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	// Make sure that the time series has current limits...

	base = ts.getDataIntervalBase();
	mult = ts.getDataIntervalMult();
	if ( refresh_flag ) {
		// TODO SAM 2010-07-15 Circular logic is confusing
		// Force a refresh of the time series.
		//
		// Need to be picky here because of the dependence on the type...
		if ( base == TimeInterval.MINUTE ) {
			MinuteTS temp = (MinuteTS)ts;
			temp.refresh ();
		}
		else if ( base == TimeInterval.HOUR ) {
			HourTS temp = (HourTS)ts;
			temp.refresh ();
		}
		else if ( base == TimeInterval.DAY ) {
			DayTS temp = (DayTS)ts;
			temp.refresh ();
		}
		else if ( base == TimeInterval.MONTH ) {
			MonthTS temp = (MonthTS)ts;
			temp.refresh ();
		}
		else if ( base == TimeInterval.YEAR ) {
			YearTS temp = (YearTS)ts;
			temp.refresh ();
		}
		else if ( base == TimeInterval.IRREGULAR ) {
			IrregularTS temp = (IrregularTS)ts;
			temp.refresh ();
		}
		else {
		    Message.printWarning ( 2, routine, "Unknown time series interval for refresh()" );
			return new TSLimits();
		}
	}

	// Loop through the dates and get max and min data values;

	if ( base == TimeInterval.IRREGULAR ) {
		// Loop through the dates and get max and min data values;
		// Need to cast as an irregular TS...

		IrregularTS its = (IrregularTS)ts;

		List<TSData> data_array = its.getData ();
		if ( data_array == null ) {
			Message.printWarning(2,routine,	"Null data for " + ts );
			return new TSLimits();
		}
		int size = data_array.size();
		TSData ptr = null;
		for ( int i = 0; i < size; i++ ) {
			ptr = data_array.get(i);
			date = ptr.getDate();
			if ( date.lessThan( start ) ) {
				// Still looking for data...
				continue;
			}
			else if ( date.greaterThan( end ) ) {
				// No need to continue processing...
				break;
			}

			value = ptr.getDataValue();
			if ( ts.isDataMissing( value ) ) {
				//The value is missing
				++missing_count;
                continue;
			}

			// Else, data value is not missing...

			if ( ts.isDataMissing(sum) ) {
				// Reset the sum...
				sum = value;
			}
			else {
			    // Add to the sum...
				sum += value;
			}
			++non_missing_count;

			if ( found ) {
				// Already found the first non-missing point so all we need to do is check the limits.  These
				// should only result in new DateTime a few times...
				if( value > max ) {
                	max = value;
					max_date = new DateTime ( date );
				}
				if( value < min ) {
                	min = value;
                	min_date = new DateTime ( date );
                }
			}
			else {
			    // Set the limits to the first value found...
				//date = new DateTime ( t );
				max = value;
				max_date = new DateTime ( date );
				min = value;
				min_date = max_date;
				non_missing_data_date1 = max_date;
				non_missing_data_date2 = max_date;
				found = true;
				continue;
			}
        }
		// Now search backward to find the first non-missing date...
		if ( found ) {
			for ( int i = (size - 1); i >= 0; i-- ){
				ptr = data_array.get(i);
				date = ptr.getDate();
				value = ptr.getDataValue();
				if ( date.greaterThan(end) ) {
					// Have not found data...
					continue;
				}
				else if ( date.lessThan(start) ) {
					// Passed start...
					break;
				}
				if( !ts.isDataMissing( value ) ) {
					// Found the one date we are after...
					non_missing_data_date2 = new DateTime ( date );
					break;
				}
			}
		}
	}
	else {
	    // A regular TS... easier to iterate...
		// First loop through and find the data limits and the minimum non-missing date...
		t = new DateTime ( start, DateTime.DATE_FAST );

		for ( ; t.lessThanOrEqualTo(end); t.addInterval( base, mult )){
			value = ts.getDataValue( t );
		
			if ( ts.isDataMissing( value ) ) {
				//The value is missing
				++missing_count;
                continue;
			}

			// Else, data value is not missing...

			if ( ts.isDataMissing(sum) ) {
				// Reset the sum...
				sum = value;
			}
			else {
			    // Add to the sum...
				sum += value;
			}
			++non_missing_count;

			if ( found ) {
				// Already found the first non-missing point so all we need to do is check the limits.  These
				// should only result in new DateTime a few times...
				if ( value > max ) {
                	max = value;
					max_date = new DateTime ( t );
				}
				if ( value < min ) {
                	min = value;
					min_date = new DateTime ( t );
                }
			}
			else {
			    // First non-missing point so set the initial values...
				date = new DateTime( t );
				max = value;
				max_date = date;
				min = value;
				min_date = date;
				non_missing_data_date1 = date;
				non_missing_data_date2 = date;
				found = true;
			}
        }
		// Now loop backward from he end date/time and find the last non-missing value...
		if ( found ) {
			t = new DateTime ( end, DateTime.DATE_FAST );
			for( ; t.greaterThanOrEqualTo(start); t.addInterval( base, -mult )) {
				value = ts.getDataValue( t );
				if( !ts.isDataMissing( value ) ) {
					// The value is not missing...
					non_missing_data_date2 = new DateTime(t);
					break;
				}
			}
		}
	}

	if ( !found ) {
		Message.printWarning( 3, routine, "\"" + ts.getIdentifierString() + "\": problems finding limits, whole POR missing!" );
		return new TSLimits();
	}

	if ( Message.isDebugOn ) {
		Message.printDebug( 10, routine, "Overall date limits are: " + start + " to " + end );
		Message.printDebug( 10, routine, "Found limits to be: " + min + " on " + min_date + " to "
		+ max + " on " + max_date );
		Message.printDebug( 10, routine,
		"Found non-missing data dates to be: " + non_missing_data_date1 + " -> " + non_missing_data_date2 );
	}

	TSLimits limits = new TSLimits ();

	// Set the basic information (all the dates have been created locally so no need for a "new")...

	limits.setDate1 ( start );
	limits.setDate2 ( end );
	limits.setMaxValue ( max, max_date );
	limits.setMinValue ( min, min_date );
	limits.setNonMissingDataDate1 ( non_missing_data_date1 );
	limits.setNonMissingDataDate2 ( non_missing_data_date2 );
	limits.setMissingDataCount ( missing_count );
	limits.setNonMissingDataCount ( non_missing_count );
	//int data_size = calculateDataSize ( ts, start, end );
	//limits.setNonMissingDataCount ( data_size - missing_count );
	if ( !ts.isDataMissing (sum) && (non_missing_count > 0) ) {
		mean = sum/(double)non_missing_count;
	}
	else {
	    mean = missing;
	}
	limits.setSum ( sum );
	limits.setMean ( mean );

	return limits;
}

/**
Determine the overall data limits for a list of time series.
@return The overall data limits for a list of time series between two dates.
The dates in the limits will be for the data, not the dates that are passed in.
@param tslist List of time series of interest.
@param start Starting date for the check.
@param end Ending date for the check.
@param req_units Units to use for check.  If not specified, the units from the first time series will be used.
@param refresh_flag Indicates whether the time series should be refreshed first
(in general this is used only within the TS package and the version of this
routine without the flag should be called).  Specifying true will result in slower execution.
@exception Exception If the data limits cannot be found.
@see TSLimits
*/
public static TSLimits getDataLimits ( List<TS> tslist, DateTime start, DateTime end, String req_units, boolean refresh_flag )
throws Exception
{	return getDataLimits ( tslist, start, end, req_units, refresh_flag, false );
}

/**
Determine the overall data limits for a list of time series.
@return The overall data limits for a list of time series between two dates.
The dates in the limits will be for the data, not the dates that are passed in.
@param tslist List of time series of interest.
@param start Starting date for the check, or null to evaluate the full period for each time series.
@param end Ending date for the check, or null to evaluate the full period for each time series.
@param req_units Units to use for check.  If not specified, the units from the
first non-null time series will be used.
@param refresh_flag Indicates whether the time series should be refreshed first
(in general this is used only within the TS package and the version of this
routine without the flag should be called).  Specifying true will result in slower execution.
@param ignore_units Indicates whether units should be ignored.  This is suitable
for getting the overall data limits for graphs where units can be ignored.
@exception Exception If the data limits cannot be found.
@see TSLimits
*/
public static TSLimits getDataLimits ( List<TS> tslist, DateTime start,
	DateTime end, String req_units, boolean refresh_flag, boolean ignore_units )
throws Exception
{	String	message = null, routine="TSUtil.getDataLimits(Vector,dates)";

	// Initialize to some reasonable out of bounds values...
	TSLimits limits = new TSLimits ();
	DateTime date = new DateTime();
	date.setYear ( 2100 );
	limits.setDate1 ( date );
	limits.setNonMissingDataDate1 ( date );
	limits.setMinValueDate ( date );
	limits.setMaxValueDate ( date );
	// Use very large/small values to initialize the data...
	limits.setMinValue ( 1.0e50 );
	limits.setMaxValue ( -1.0e50 );
	date.setYear ( 1700 );
	limits.setDate2 ( date );
	limits.setNonMissingDataDate2 ( date );

	// Make sure the vector is not null...

	if ( tslist == null ) {
		message = "Null time series vector";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Get the valid dates...

	DateTime start_date = start;
	DateTime end_date = end;
	if ( (start_date == null) || (end_date == null) ) {
		// Get the limits from the time series...
		TSLimits valid_dates = getPeriodFromTS(tslist, MAX_POR);
		start_date = valid_dates.getDate1();
		end_date = valid_dates.getDate2();
	}

	if ( start_date == null ) {
		message = "Null start date";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	if ( end_date == null ) {
		message = "Null end date";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Find the first non-null time series and get the units...

	String units = req_units;
	TS ts;
	int size = tslist.size();
	if ( !ignore_units ) {
		if ( (req_units == null) || req_units.equals("") ) {
			for ( int i = 0; i < size; i++ ) {
				ts = tslist.get(i);
				if ( ts == null ) {
					continue;
				}
				units = ts.getDataUnits();
				break;
			}
		}
	}
	if ( units == null ) {
		units = "";
	}

	// Loop through the time series...

	TSLimits tslimits = null;
	String	tsunits = "";
	DataUnitsConversion conversion = null;
	DateTime tslimits_date;
	double add = 0.0, tslimits_value = 0.0, mult = 1.0, value = 0.0;
	for ( int i = 0; i < size; i++ ) {
		ts = tslist.get(i);
		if ( ts == null ) {
			continue;
		}

		// Get the limits for the time series...
		if ( (start_date == null) && (end_date == null) ) {
			// This is fast since in many cases recomputation is not needed...
			tslimits = ts.getDataLimits ();
		}
		else {
		    // Get the limits, iterating through data if necessary...
			tslimits = TSUtil.getDataLimits ( ts, start_date, end_date );
		}
		if ( tslimits == null ) {
			continue;
		}
		// Get the units for the time series and get conversion factors
		// if necessary (this will throw an exception if the units are not found)...
		if ( !ignore_units ) {
			tsunits = ts.getDataUnits ();
			conversion = DataUnits.getConversion ( tsunits, units );
			mult = conversion.getMultFactor();
			add = conversion.getAddFactor();
		}
		// Check each data member, resetting if necessary...
		// First check the overall dates...
		tslimits_date = limits.getDate1();
		date = tslimits.getDate1();
		if ( (date != null) && (tslimits_date != null) ) {
			if ( date.lessThan(tslimits_date) ) {
				limits.setDate1 ( date );
			}
		}
		tslimits_date = limits.getDate2();
		date = tslimits.getDate2();
		if ( (date != null) && (tslimits_date != null) ) {
			if ( date.greaterThan(tslimits_date) ) {
				limits.setDate2 ( date );
			}
		}
		// Now check the non-missing data dates...
		tslimits_date = limits.getNonMissingDataDate1();
		date = tslimits.getNonMissingDataDate1();
		if ( (date != null) && (tslimits_date != null) ) {
			if ( date.lessThan(tslimits_date) ) {
				limits.setNonMissingDataDate1 ( date );
			}
		}
		tslimits_date = limits.getNonMissingDataDate2();
		date = tslimits.getNonMissingDataDate2();
		if ( (date != null) && (tslimits_date != null) ) {
			if ( date.greaterThan(tslimits_date) ) {
				limits.setNonMissingDataDate2 ( date );
			}
		}
		// Now check the max value...
		tslimits_value = limits.getMaxValue()*mult + add;
		value = tslimits.getMaxValue()*mult + add;
		if ( value > tslimits_value ) {
			// Reset the value and the associated date...
			limits.setMaxValueDate ( tslimits.getMaxValueDate() );
			limits.setMaxValue ( value );
		}
		// Now check the min value...
		tslimits_value = limits.getMinValue()*mult + add;
		value = tslimits.getMinValue()*mult + add;
		if ( value < tslimits_value ) {
			// Reset the value and the associated date...
			limits.setMinValueDate ( tslimits.getMinValueDate() );
			limits.setMinValue ( value );
		}
	}

	// TODO SAM 2005-04-22
	// Need to set limits to null, etc., if the final values are the initial (not actual) values.

	// Return the overall limits...

	limits.setLimitsFound ( true );
	return limits;
}

/**
Determine the overall data limits for a list of time series.
@return The overall data limits for a list of time series between two dates.
The dates in the limits will be for the data, not the dates that are passed in.
@param tslist List of time series of interest.
@param start Starting date for the check.
@param end Ending date for the check.
@param units Units to use for check.
@exception Exception If null data prevent limits from being computed.
@see TSLimits
*/
public static TSLimits getDataLimits ( List<TS> tslist, DateTime start, DateTime end, String units )
throws Exception
{	return getDataLimits ( tslist, start, end, units, false );
}

/**
Determine the overall data limits for a list of time series.
@return The overall data limits for a list of time series.
@param tslist List of time series of interest.
@param refresh_flag Indicates whether the time series should be refreshed first
(in general this is used only within the TS package and the version of this
routine without the flag should be called).  Specifying true will result in slower execution.
@exception Exception If null data prevent limits from being computed.
@see TSLimits
*/
public static TSLimits getDataLimits ( List<TS> tslist, boolean refresh_flag )
throws Exception
{	return getDataLimits ( tslist, null, null, null, refresh_flag );
}

/**
Determine the overall data limits for a list of time series.
@return The overall data limits for a list of time series.
@param tslist List of time series of interest.
@exception Exception If null data prevent limits from being computed.
@see TSLimits
*/
public static TSLimits getDataLimits ( List<TS> tslist )
throws Exception
{	return getDataLimits ( tslist, null, null, null, false );
}

/*
** getMonthTotals - get long-term historical monthly averages
**
**	(1)	Given a monthly time series, calculates the number of valid
**		values for each calendar month, the number of missing values
**		for each cal month, the total number of values (valid or
**		missing) for each month, the sum of valid values for each
**		calendar month and the average value for each calendar month.
**	(2)	If no long-term average is available, store the missing data
**		value in the arrays that are returned.  The calling routine
**		will be able to tell if data are available by checking the
**		num* arrays.
**
** Variable	I/O	Description
**
** am		L	Absolute month used in looping.
** avgvals	O	12 monthly average values.
** i		L	Loop counter.
** month	L	Calendar month corresponding to an absolute month.
** nummissing	O	Number of values missing and not used.
** numsummed	O	Number of values used in the average/summed calculation.
** numtotal	O	Sum of the above two arrays for each month.
** pos		L	Month position in data array.
** summedvals	O	12 monthly summed values.
** TS		I	Time series.
** year		L	Calendar year corresponding to an absolute month.
*/

/**
This routine is a port of the legacy TSMonthTotals and may be deprecated at
some point.  It is used in the State of Colorado StateDMI software.
@return An instance of MonthTotals containing the monthly totals for the time series.
@param ts Time series to evaluate.
@exception RTi.TS.TSException if there is a problem determining the totals.
@see MonthTotals
*/
public static MonthTotals getMonthTotals ( TS ts )
throws TSException
{
	try {
	    return getMonthTotals ( ts, ts.getDate1(), ts.getDate2() );
	}
	catch ( TSException e ) {
		throw e;
	}
}

/**
This routine is a port of the legacy TSMonthTotals and may be deprecated at
some point.  It is used in the State of Colorado StateDMI software.
@return An instance of MonthTotals containing the monthly totals for the
time series.  Data values (sums and averages) are initialized with the missing data value for the time series.
@param ts Time series to evaluate.
@param date1 Starting date to evaluate for totals.
@param date2 Ending date to evaluate for totals.
@exception RTi.TS.TSException if there is a problem determining the totals.
@see MonthTotals
*/
public static MonthTotals getMonthTotals ( TS ts, DateTime date1, DateTime date2 )
throws TSException
{	return getMonthTotals ( ts, date1, date2, (TS)null, (PropList)null );
}

/**
This routine is a port of the legacy TSMonthTotals and may be deprecated at
some point.  It is used in the State of Colorado StateDMI software.
@return An instance of MonthTotals containing the monthly totals for the
time series.  Data values (sums and averages) are initialized with the missing data value for the time series.
@param ts Time series to evaluate.
@param date1 Starting date to evaluate for totals.
@param date2 Ending date to evaluate for totals.
@param reference_ts If specified and the CheckReferencTS property is true, then
the reference time series is checked for missing data, etc., to limit the data
that are used.  This can be used, for example, when getting monthly averages
for diversion and demand time series.
@param props Properties to control execution, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>CheckeReferenceTS</b></td>
<td>
If <code>true</code>, then the reference time series is checked when analyzing
the data.  For example, if a diversion time series is used and demand time
series is specified as the reference time series, other properties can be used
to throw out diversion values if demand values are zero.
</td>
<td>false</td>
</tr>

<tr>
<td><b>IgnoreLEZero</b></td>
<td>
If <code>true</code>, ignore values that are less than or equal to zero (treat
as missing).
</td>
<td>false</td>
</tr>

<tr>
<td><b>IgnoreRefLEZero</b></td>
<td>
If <code>true</code>, ignore values when the corresponding value (by date/time)
in the reference time series is less than or equal to zero (treat
as missing).
</td>
<td>false</td>
</tr>
</table>
@exception RTi.TS.TSException if there is a problem determining the totals.
@see MonthTotals
*/
public static MonthTotals getMonthTotals ( TS ts, DateTime date1, DateTime date2, TS reference_ts, PropList props )
throws TSException
{	int i, month;
	String message, routine = "TSUtil.getMonthTotals";
	int [] numsummed = new int [12];
	int [] nummissing = new int [12];
	double missing = -999.0;
	double [] avgvals = new double [12];
	double [] summedvals = new double [12];
	MonthTS tspt = null;

	if ( ts == null ) {
		message = "Null time series";
		Message.printWarning ( 2, routine, message );	
		throw new TSException ( message );
	}

	// Set some booleans that are easy to check...
	boolean check_ref_ts = false;
	boolean ignore_le_zero = false;
	//TODO SAM 2007-03-01 Evaluate if needed
	//boolean ignore_ref_le_zero = false;
	if ( props != null ) {
		String prop_value = props.getValue ( "CheckRefTS" );
		if ( (prop_value != null) && prop_value.equalsIgnoreCase("true") ) {
			check_ref_ts = true;
		}
		prop_value = props.getValue ( "IgnoreLEZero" );
		if ( (prop_value != null) && prop_value.equalsIgnoreCase("true") ) {
			ignore_le_zero = true;
		}
		/* TODO SAM 2007-03-01 Evaluate if needed.
		prop_value = props.getValue ( "IgnoreRefLEZero" );
		if (	(prop_value != null) &&
			prop_value.equalsIgnoreCase("true") ) {
			ignore_ref_le_zero = true;
		}
		*/
	}

	tspt = (MonthTS)ts;
	if ( date1 == null ) {
		message = "Null start date";
		Message.printWarning ( 2, routine, message );	
		throw new TSException ( message );
	}
	if ( date2 == null ) {
		message = "Null end date";
		Message.printWarning ( 2, routine, message );	
		throw new TSException ( message );
	}

	if ( ts.getDataIntervalBase() != TimeInterval.MONTH ) {
		message = "Only monthly time series can be processed";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Use the missing data value from the time series to initialize the data...

	missing = ts.getMissing();

	for ( i = 0; i < 12; i++ ) {
		numsummed[i] = 0;
		nummissing[i] = 0;
		avgvals[i] = missing;
		summedvals[i] = missing;
	}

	DateTime start_date = new DateTime ( date1 );
	DateTime end_date = new DateTime ( date2 );
	DateTime date = new DateTime ( start_date );
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	double data_value; 
	
	for ( ; date.lessThanOrEqualTo( end_date); date.addInterval(interval_base, interval_mult)){
		data_value = ts.getDataValue ( date );
		month = date.getMonth();

		// Use "continue" to skip over unusable data...
		if ( tspt.isDataMissing ( data_value ) ){
			// The value to add is missing so don't do it...
			++nummissing[month-1];
			continue;
		}
		if ( ignore_le_zero && (data_value <= 0.0) ) {
			++nummissing[month-1];
			continue;
		}
		if ( (check_ref_ts) && (reference_ts != null) ) {
			if ( reference_ts.getDataValue(date) <= 0.0 ) {
				++nummissing[month-1];
				continue;
			}
		}
		// Else we have data that can be processed...
		// If the sum is missing, set the value.  Otherwise, add to the sum...
		if ( tspt.isDataMissing(summedvals[month - 1]) ) {
			// Set...
			summedvals[month-1] = data_value;
		}
		else {
		    // Add...
			summedvals[month-1] += data_value;
		}
		++numsummed[month-1];
	}

	for ( i = 0; i < 12; i++) {
		if ( numsummed[i] > 0 ) {
			avgvals[i] = summedvals[i]/numsummed[i];
		}
		else {
		    // Use the missing data value...
			summedvals[i] = missing;
			avgvals[i] = missing;
		}
	}

	MonthTotals totals = new MonthTotals ();
	totals.setSums ( summedvals );
	totals.setNumMissing ( nummissing );
	totals.setNumSummed ( numsummed );
	totals.setAverages ( avgvals );
	return totals;
}

/**
Return the list of identifier strings given the time series list.  The alias will be returned if set.  Otherwise
the full time series identifier string will be returned.
@param tsList list of time series to process.
@param includeInput if true, include the input part of the time series identifier.
If false, only include the 5-part information.
@return the list of time series identifier strings, useful for choices in applications - a non-null list is guaranteed.
*/
public static List<String> getTimeSeriesIdentifiers ( List<TS>tsList, boolean includeInput )
{
    List<String> list = new ArrayList<String>();
    if ( tsList != null ) {
        int tssize = tsList.size();
        TS ts;
        for ( int its = 0; its < tssize; its++ ) {
            ts = tsList.get(its);
            if ( !ts.getAlias().equals("") ) {
                // Use the alias if it is available.
                list.add( ts.getAlias() );
            }
            else {
                // Use the identifier.
                list.add ( ts.getIdentifier().toString(includeInput) );
            }
        }
    }
    return list;
}

/**
Return the interpolated data value between two time steps for a time series.
@param ts Time series to get data from.  Irregular time series are currently
not supported and always return missing data.
@param date Date/time for the end of the interval.
The precision of the date/time should be the same as the time series for accurate date comparisons.
@param double timeStepFraction Fraction of the time step to look back in time
from date.  Must be betweem 0.0 (return value at date) and 1.0 (return value one time step prior to date).
*/
public static double getInterpolatedDataValue ( TS ts, DateTime date, double timeStepFraction )
{	if ( ts == null ) {
		return -999.0;
	}
	if ( date == null ) {
		return ts.getMissing();
	}

	// get value at this time 
	double thisValue = ts.getDataValue ( date );
	if ( ts.isDataMissing(thisValue) ) {
		return ts.getMissing();
	}

	// check the interval fraction
	if ( timeStepFraction == 0.0 ) {
		return thisValue;
	}
	else if( timeStepFraction > 1.0 ) {
		return ts.getMissing();
	}

	// Get iteration information...
	int base = ts.getDataIntervalBase();
	int mult = ts.getDataIntervalMult();

	if ( base == TimeInterval.IRREGULAR ) {
		// Not yet supported...
		return ts.getMissing();
	}
	else {
	    // A regular TS... easier to interpolate...
		// get date for last time step
		DateTime idate = new DateTime(DateTime.DATE_STRICT);
		idate = new DateTime(date);
		idate.addInterval( base, -1*mult );

		// get value from last time step		
		double lastValue = ts.getDataValue ( idate );
		if ( ts.isDataMissing(lastValue) ) {
			return ts.getMissing();
		}
		
		//interpolate
		double value = thisValue + timeStepFraction * ( lastValue - thisValue );
		return value;
	}
}

/**
Determines time series statistics resulting from the application of a pattern
to the time series.  This is called by fillPattern.
@return The pattern statistics as a TSPatternStats instance, or null if there is a problem.
*/
public static TSPatternStats getPatternStats ( TS ts, StringMonthTS pattern_ts )
{	return getPatternStats ( ts, pattern_ts, (PropList)null );
}

/**
Determines time series statistics resulting from the application of a pattern
to the time series.  This is called by fillPattern.
@return The pattern statistics as a TSPatternStats instance, or null if there is a problem.
@param ts Time series to analyze.
@param pattern_ts Time series containing patterns.
@param props PropList to modify behavior.  Currently, only
"IgnoreLessThanOrEqualZero=true" is recognized.  If true, averages will ignore values <= 0.  The default is false.
*/
public static TSPatternStats getPatternStats ( TS ts, StringMonthTS pattern_ts, PropList props )
{	String	routine = "TSUtil.getPatternStats";

	boolean ignore_lezero = false;
	if ( props != null ) {
		String prop_value = props.getValue ( "IgnoreLessThanOrEqualZero" );
		if ( prop_value != null ) {
			if ( prop_value.equalsIgnoreCase ("true" ) ) {
				ignore_lezero = true;
			}
		}
	}

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
		return null;
	}
	if ( pattern_ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null pattern time series" );
		return null;
	}

	// The number of indicators will have been determined in the time series...

	List<String> indicators = pattern_ts.getUniqueData ();

	// Size the arrays in our statistics...

	TSPatternStats stats = new TSPatternStats ( indicators, ts, pattern_ts);

	// Loop through the data and fill for each month...

	int	interval_base = ts.getDataIntervalBase();
	int	interval_mult = ts.getDataIntervalMult();
	double data_value;
	String indicator;
	DateTime date;
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return null;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			data_value = tsdata.getDataValue();
			date = tsdata.getDate();
			if ( (!ignore_lezero && !ts.isDataMissing(data_value)) ||
				(ignore_lezero && ((data_value > 0.0) && !ts.isDataMissing(data_value))) ) {
				// Not missing, so add to stats...
				indicator = pattern_ts.getDataValueAsString ( date );
				stats.add(indicator,data_value,date.getMonth());
			}
		}
	}
	else {	// Loop using addInterval...
		DateTime start_date = new DateTime ( ts.getDate1() );
		DateTime end_date = new DateTime ( ts.getDate2() );
		date = new DateTime ( start_date );
		
		for ( ; date.lessThanOrEqualTo( end_date); date.addInterval(interval_base, interval_mult) ) {
			data_value = ts.getDataValue ( date );
			if ( (!ignore_lezero && !ts.isDataMissing(data_value)) ||
				(ignore_lezero && ((data_value > 0.0) && !ts.isDataMissing(data_value))) ) {
				indicator = pattern_ts.getDataValueAsString ( date );
				// Not missing, so add...
				stats.add(indicator,data_value,date.getMonth());
			}
		}
	}

	// Now return the values...

	return stats;
}

/*
@return the period given two dates and indicator for the type of calendar.
The maximum full year bounding period for the calendar type is returned, even
if it means extending the period to include missing data.
The dates are always returned as calendar dates (e.g., if water years are
requested, the first date will be adjusted to be October).
@param date1 First date of interest.
@param date2 Second date of interest.
@param calendar_type Calendar type.
@param por_flag Currently ignored.
*/
public static TSLimits getPeriodFromDates (	DateTime date1, DateTime date2, YearType calendar_type, int por_flag )
throws TSException
{	if ( date1 == null ) {
		throw new TSException ( "Null start date" );
	}
	if ( date2 == null ) {
		throw new TSException ( "Null end date" );
	}
	TSLimits limits = new TSLimits();
	limits.setDate1 ( date1 );
	limits.setDate2 ( date2 );
	int month1 = date1.getMonth();
	int month2 = date2.getMonth();
	if ( calendar_type == YearType.CALENDAR ) {
		if ( month1 != 1 ) {
			DateTime newdate1 = new DateTime ( date1 );
			newdate1.setMonth(1);
			limits.setDate1 ( newdate1 );
		}
		if ( month2 != 12 ) {
			DateTime newdate2 = new DateTime ( date2 );
			newdate2.setMonth(12);
			limits.setDate2 ( newdate2 );
		}
		return limits;
	}
	else if ( calendar_type == YearType.WATER ) {
		if ( month1 != 10 ) {
			DateTime newdate1 = new DateTime ( date1 );
			if ( month1 < 10 ) {
				// Need to adjust year...
				newdate1.addYear ( -1 );
			}
			// Always reset the month...
			newdate1.setMonth(10);
			limits.setDate1 ( newdate1 );
		}
		if ( month2 != 9 ) {
			DateTime newdate2 = new DateTime ( date2 );
			if ( month2 > 9 ) {
				// Need to adjust year...
				newdate2.addYear ( 1 );
			}
			// Always reset the month...
			newdate2.setMonth(9);
			limits.setDate2 ( newdate2 );
		}
		return limits;
	}
	else if ( calendar_type == YearType.NOV_TO_OCT ) {
		if ( month1 != 11 ) {
			DateTime newdate1 = new DateTime ( date1 );
			if ( month1 < 11 ) {
				// Need to adjust year...
				newdate1.addYear ( -1 );
			}
			// Always reset the month...
			newdate1.setMonth(11);
			limits.setDate1 ( newdate1 );
		}
		if ( month2 != 10 ) {
			DateTime newdate2 = new DateTime ( date2 );
			if ( month2 > 10 ) {
				// Need to adjust year...
				newdate2.addYear ( 1 );
			}
			// Always reset the month...
			newdate2.setMonth(10);
			limits.setDate2 ( newdate2 );
		}
		return limits;
	}
	throw new TSException ( "Unknown calendar \"" + calendar_type + "\"" );
}

// Might move the following to TSLimits at some point.
/**
Determine the limits for a list of time series given the TSLimits.
<pre>
Exaple of POR calculation:
   |         ------------------------  	TS1
   |   -------------------    	TS2
   |                --------------  TS3
   ----------------------------------------------
       ------------------------------  MAX_POR
                    ------             MIN_POR
</pre>
<p>
@return The TSLimits resulting from the combination of the given TSLimits.
@param limits A list of TSLimits of interest.
@param por_flag Use a *_POR flag.
@exception RTi.TS.TSException If the period cannot be determined from the
limits (e.g., requesting minimum and there is no overlap).
*/
public static TSLimits getPeriodFromLimits ( List<TSLimits> limits, int por_flag )
throws TSException
{	String 	message, routine="TSUtil.getPeriodFromLimits";
	int	vectorSize;
	int	dl = 10, i = 0;
	DateTime end, start;

	if ( limits == null ) {
		message = "TSLimits vector is null";
		Message.printWarning(2, routine, message );
		throw new TSException ( message );
	}

	vectorSize = limits.size();
	if( vectorSize == 0 ) {
		message="Zero TSLimits vector size";
		Message.printWarning(2, routine, message );
		throw new TSException ( message );
	}

	// Initialize the start and end dates to the first TS dates...

	TSLimits limits_pointer = limits.get(0);
	start = limits_pointer.getDate1();
	end	= limits_pointer.getDate2();

	// Now loop through the remaining TSLimits...

	DateTime limits_start;
	DateTime limits_end;
	for( i = 1; i < vectorSize; i++ ) {
		limits_pointer	= limits.get(i);
		if( limits_pointer == null ) {
			// Ignore the TSLimits...
			continue;
		}
		limits_start = limits_pointer.getDate1();
		limits_end	= limits_pointer.getDate2();
		if ( (limits_start == null) || (limits_end == null) ) {
			continue;
		}

		if ( por_flag == MAX_POR ) {
			if( limits_start.lessThan(start) ) {
				start = new DateTime ( limits_start );
			}
			if( limits_end.greaterThan(end) ) {
				end = new DateTime ( limits_end );
			}
		}
		else if ( por_flag == MIN_POR ) {
			if( limits_start.greaterThan(start) ) {
				start = new DateTime ( limits_start );
			}
			if( limits_end.lessThan(end) ) {
				end = new DateTime ( limits_end );
			}
		}
	}

	// If the time series do not overlap, then the limits may be
	// reversed.  In this case, throw an exception...
	if ( start.greaterThan(end) ) {
		message = "Periods do not overlap.  Can't determine minimum period.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	if( (por_flag == MAX_POR) && Message.isDebugOn ) {
		Message.printDebug( dl, routine, "Maximum POR limits are " + start + " to " + end );
	}
	if( (por_flag == MIN_POR) && Message.isDebugOn ) {
		Message.printDebug( dl, routine, "Minimum POR limits are " + start + " to " + end );
	}

	// Now return the dates as a new instance so we don't mess up what was in the time series...

	TSLimits newlimits = new TSLimits();
	newlimits.setDate1 ( new DateTime(start) );
	newlimits.setDate2 ( new DateTime(end) );

	return newlimits;
}

//------------------------------------------------------------------------------
// getPeriodFromTS - class to determine the minimum or maximum overlapping POR.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:	
// (1)		Exaple of POR calculation:
//            	|       ------------------------  	TS1
//	 	|	   -------------------    	TS2
//		|             		--------------  TS3
//		----------------------------------------------
//
//			------------------------------  MAX_POR
//					------		MIN_POR
//
//------------------------------------------------------------------------------
// History:
// 
// 03 Nov 1997	Daniel Weiler, RTi	Created initial version.   Based on
//					TSGetPeriodFromTS C code.
// 08 Jan 1998	Steven A. Malers, RTi	Move from the TS class to the TSUtil
//					class.  Return a TSLimits rather than
//					a Vector so that it is easier to
//					implement corresponding code in C++.
//					Previous code did not take advantage of
//					TSDate.  Use TSDate's conditional
//					functions now to compare dates.
//------------------------------------------------------------------------------
// Variable	I/O	Description
//
// por_flag	I	Flag indicating whether to get the maximum or minimum
//			period of record.
// routine	L	Name of this routine.
// tslimits	O	Time series limits.
// tsptr	L	A single time series.
// vectorSize	L	Size of the list of incoming time series.
//------------------------------------------------------------------------------

/**
Determine the limits for a single time series.  This is a utility routine to
overload the version that accepts a vector of time series.
@return The TSLimits for the single time series (recomputed).
@param ts The time series of interest.
@param por_flag Use a *_POR flag.
@exception RTi.TS.TSException If the period cannot be determined from the time
series (e.g., requesting minimum and there is no overlap).
*/
public static TSLimits getPeriodFromTS ( TS ts, int por_flag )
throws TSException
{	if ( ts == null ) {
		// Let other routine handle...
		return getPeriodFromTS ( (List<TS>)null, por_flag );
	}
	else {
        // Put single TS into a vector...
		List<TS> v = new ArrayList<TS>(1);
		v.add ( ts );
		return getPeriodFromTS ( v, por_flag );
	}
}

/**
Determine the limits for a single time series and a Vector.  This combinataion
is common where one time series is independent and a list of time series are
dependent.  This is a utility routine to
overload the version that accepts a vector of time series.
@return The TSLimits for all the time series (recomputed).
@param ts The time series of interest.
@param tslist A list of time series of interest.
@param por_flag Use a *_POR flag.
@exception RTi.TS.TSException If the period cannot be determined from the time
series (e.g., requesting minimum and there is no overlap).
*/
public static TSLimits getPeriodFromTS ( TS ts, List<TS> tslist, int por_flag )
throws TSException
{	if ( (ts == null) || (tslist == null) ) {
		// Let other routine handle...
		return getPeriodFromTS ( (List<TS>)null, por_flag );
	}
	else {
        // Put single TS and other TS into a vector...
		List<TS> v = new ArrayList<TS>(1);
		v.add ( ts );
		int size = v.size();
		for ( int i = 0; i < size; i++ ) {
			v.add ( tslist.get(i) );
		}
		return getPeriodFromTS ( v, por_flag );
	}
}

/**
Determine the limits for a list of time series.
<pre>
Example of POR calculation:
   |         ------------------------  	TS1
   |   -------------------    	TS2
   |                --------------  TS3
   ----------------------------------------------
       ------------------------------  MAX_POR
                    ------             MIN_POR
</pre>
@return The TSLimits for the list of time series (recomputed).  If the limits
do not overlap, return the maximum.
@param ts A vector of time series of interest.
@param por_flag Use a *_POR flag.
@exception RTi.TS.TSException If the period cannot be determined from the time series.
*/
public static TSLimits getPeriodFromTS ( List<? extends TS> ts, int por_flag )
throws TSException
{	String 	message, routine="TSUtil.getPeriodFromTS";
	TS tsPtr = null;
	int	dl = 10, i = 0;
	DateTime end = null, tsPtr_end, tsPtr_start, start = null;

	if( ts == null ) {
		message = "Unable to get period for time series - time series list is null";
		Message.printWarning(3, routine, message );
		throw new TSException ( message );
	}

	int vectorSize = ts.size();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Getting " + por_flag + "-flag limits for " + vectorSize + " time series" );
	}
	if( vectorSize == 0 ) {
		message="Unable to get period for time series - time series list is zero size";
		Message.printWarning(3, routine, message );
		throw new TSException ( message );
	}
	if ( (por_flag != MIN_POR) && (por_flag != MAX_POR) ) {
		message = "Unknown option for TSUtil.getPeriodForTS" + por_flag;
		Message.printWarning ( 3, routine, message );
		throw new TSException ( message );
	}

	// Initialize the start and end dates to the first TS dates...

    int nullcount = 0;
	for ( int its = 0; its < vectorSize; its++ ) {
		tsPtr = ts.get(its);
		if ( tsPtr != null ) {
            if ( tsPtr.getDate1() != null ) {
                start = tsPtr.getDate1();
            }
            if ( tsPtr.getDate2() != null ) {
                end = tsPtr.getDate2();
            }
			if ( (start != null) && (end != null) ) {
                // Done looking for starting date/times
				break;
			}
		}
        else {
            ++nullcount;
        }
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Starting comparison dates " + start + " " + end );
	}

	if ( (start == null) || (end == null) ) {
		message = "Unable to get period (all null dates) from " + vectorSize + " time series (" + nullcount +
        " null time series).";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Now loop through the remaining time series...

	for( i = 1; i < vectorSize; i++ ) {
		tsPtr = ts.get(i);
		if( tsPtr == null ) {
			// Ignore the time series...
			continue;
		}
		tsPtr_start = tsPtr.getDate1();
		tsPtr_end = tsPtr.getDate2();
		if ( (tsPtr_start == null) || (tsPtr_end == null) ) {
			continue;
		}
		Message.printDebug ( dl, routine, "Comparison dates " + tsPtr_start + " " + tsPtr_end );

		if ( por_flag == MAX_POR ) {
			if( tsPtr_start.lessThan(start) ) {
				start = new DateTime ( tsPtr_start );
			}
			if( tsPtr_end.greaterThan(end) ) {
				end = new DateTime ( tsPtr_end );
			}
		}
		else if ( por_flag == MIN_POR ) {
			if( tsPtr_start.greaterThan(start) ) {
				start = new DateTime ( tsPtr_start );
			}
			if( tsPtr_end.lessThan(end) ) {
				end = new DateTime ( tsPtr_end );
			}
		}
	}
	// If the time series do not overlap, then the limits may be reversed.  In this case, throw an exception...
	if ( start.greaterThan(end) ) {
		message = "Periods do not overlap.  Can't determine minimum period.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	if ( Message.isDebugOn ) {
		if ( por_flag == MAX_POR ) {
			Message.printDebug( dl, routine, "Maximum POR limits are " + start + " to " + end );
		}
		else if ( por_flag == MIN_POR ) {
			Message.printDebug( dl, routine, "Minimum POR limits are " + start + " to " + end );
		}
	}

	// Now return the dates as a new instance so we don't mess up what was in the time series...

	TSLimits limits = new TSLimits();
	limits.setDate1 ( new DateTime(start) );
	limits.setDate2 ( new DateTime(end) );
	limits.setLimitsFound ( true );
	return limits;
}

/**
Return an array of valid format specifiers for the TS.formatHeader() method
(which at some point may be moved to this class), in
the format "%X - Description" where X is the format specifier.  This is useful
for building graphical interfaces.
@return an array of format specifiers.
@param includeDescription If false, only the %X specifiers are returned.  if
True, the description is also returned.
*/
public static String[] getTSFormatSpecifiers(boolean includeDescription )
{	String [] formats = new String[22];
    // TODO SAM 2013-06-14 Evaluate other sort order to match TSID order
	if ( includeDescription ) {
		formats[0] = "%A - Alias";
		formats[1] = "%b - Interval, base";
		formats[2] = "%D - Description";
		formats[3] = "%F - Identifier";
		formats[4] = "%I - Interval";
		formats[5] = "%i - Input name";
		formats[6] = "%L - Location";
		formats[7] = "%l - Location, main";
		formats[8] = "%m - Interval, mult";
		formats[9] = "%p - Data period";
		formats[10] = "%S - Source";
		formats[11] = "%s - Source, main";
		formats[12] = "%U - Units";
		formats[13] = "%T - Data type";
		formats[14] = "%t - Data type, main";
		formats[15] = "%k - Data type, sub";
		formats[16] = "%w - Location, sub";
		formats[17] = "%x - Source, sub";
		formats[18] = "%y - Location type";
		formats[19] = "%Z - Scenario";
		formats[20] = "%z - Sequence ID";
		formats[21] = "%% - Literal %";
	}
	else {
	    formats[0] = "%A";
		formats[1] = "%b";
		formats[2] = "%D";
		formats[3] = "%F";
		formats[4] = "%I";
		formats[5] = "%i";
		formats[6] = "%L";
		formats[7] = "%l";
		formats[8] = "%m";
		formats[9] = "%p";
		formats[10] = "%S";
		formats[11] = "%s";
		formats[12] = "%U";
		formats[13] = "%T";
		formats[14] = "%k";
		formats[15] = "%u";
		formats[16] = "%w";
		formats[17] = "%x";
		formats[18] = "%x";
		formats[19] = "%Z";
		formats[20] = "%z";
		formats[21] = "%%";
	}
	return formats;
}

/**
This method takes two dates which are generally the start and end dates for an iteration.  If they are
specified, they are used (even if they are outside the range of the time series).  If a date is not specified,
then the appropriate date from the time series is used.  This routine may require logic at some point to
handle special cases.  For example, the incoming arguments may specify a start date but no end date.
If the start date from the time series is later than the specified end date, then what?
@return The limits given a suggested start and end date.  The date limits can
extend beyond the end of the time series dates.  If the suggestions are null,
the appropriate start/end dates from the time series are used.  New date instances
are created to protect against changing the original dates.
@param ts Time series of interest.
@param suggested_start Suggested start date.
@param suggested_end Suggested end date.
*/
public static TSLimits getValidPeriod (	TS ts, DateTime suggested_start, DateTime suggested_end )
{	TSLimits dates = new TSLimits();
	if ( (suggested_start == null) && (ts != null) ) {
        dates.setDate1( new DateTime(ts.getDate1()) );
	}
	else {
	    dates.setDate1 ( new DateTime(suggested_start) );
	}
	if ( (suggested_end == null) && (ts != null) ) {
		dates.setDate2( new DateTime(ts.getDate2()) );
	}
	else {
	    dates.setDate2 ( new DateTime(suggested_end) );
	}
	return dates;
}

/**
Get the valid period from a list of time series.  If the suggested start or end are specified, use them.
If the suggested start and end are null, determine the start and end from the maximum time series period.
@param tslist list of time series of interest
@param suggestedStart Suggested start date/time
@param suggestedEnd Suggested end date/time
*/
public static TSLimits getValidPeriod ( List<TS> tslist, DateTime suggestedStart, DateTime suggestedEnd )
{   TSLimits dates = new TSLimits();
    TSLimits datesFromTS = null;
    try {
        datesFromTS = getPeriodFromTS ( tslist, MAX_POR );
    }
    catch ( Exception e ) {
        datesFromTS = null;
    }
    if ( suggestedStart == null ) {
        if ( (datesFromTS != null) && (datesFromTS.getDate1() != null) ) {
            dates.setDate1( new DateTime(datesFromTS.getDate1()) );
        }
    }
    else {
        dates.setDate1 ( new DateTime(suggestedStart) );
    }
    if ( suggestedEnd == null ) {
        if ( (datesFromTS != null) && (datesFromTS.getDate2() != null) ) {
            dates.setDate2( new DateTime(datesFromTS.getDate2()) );
        }
    }
    else {
        dates.setDate2 ( new DateTime(suggestedEnd) );
    }
    return dates;
}

/**
Find a time series in a list.  The indicated field is searched and a
case-insensitive query is made.  The trace ID is not used in the search.
@param tslist List of time series to search.
@param id String identifier to match.
@param field Field to match (currently can only be "Alias" or "Location").
@param direction If >= 0, search forward.  If < 0, search backward.
@return the Vectorposition of the match or -1 if no match or the field is not recognized.
*/
public static int indexOf (	List<? extends TS> tslist, String id, String field, int direction )
{	return indexOf ( tslist, id, field, null, direction );
}

/**
Find a time series in a list.  The indicated field is searched and a case-insensitive query is made.
@param tslist List of time series to search.
@param id String identifier to match.
@param field Field to match (currently can only be "Alias" or "Location").
@param traceID If non-null and non-blank, the trace identifier is also checked to make a match.  This is used for ensembles.
@param direction If >= 0, search forward.  If < 0, search backward.
@return the list position (0+) of the match or -1 if no match or the field is not recognized.
*/
public static int indexOf (	List<? extends TS> tslist, String id, String field, String traceID, int direction )
{	if ( tslist == null ) {
		return -1;
	}
	int size = tslist.size();
	TS ts = null;
	int ifield = 1;	// Internal field flag to increase performance...
	if ( field.equalsIgnoreCase("Alias") ) {
		ifield = 1;
	}
	else if ( field.equalsIgnoreCase("Location") ) {
		ifield = 2;
	}
	if ( (ifield != 1) && (ifield != 2) ) {
		return -1;
	}
	if ( direction >= 0 ) {
		// Search forward...
		for ( int i = 0; i < size; i++ ) {
			ts = tslist.get(i);
			if ( ts == null ) {
				continue;
			}
			if ( ifield == 1 ) {
				if ( id.equalsIgnoreCase(ts.getAlias()) ) {
					if ( (traceID != null) && !traceID.equals("") ) {
						if ( ts.getSequenceID().equalsIgnoreCase(traceID) ) {
							return i;
						}
					}
					else {
					    return i;
					}
				}
			}
			else if ( ifield == 2 ) {
				if ( id.equalsIgnoreCase(ts.getLocation()) ) {
					if ( (traceID != null) && !traceID.equals("") ) {
						if ( ts.getSequenceID().equalsIgnoreCase(traceID) ) {
							return i;
						}
					}
					else {
					    return i;
					}
				}
			}
		}
	}
	else {
	    // Search backward...
		for ( int i = (size - 1); i >= 0; i-- ) {
			ts = tslist.get(i);
			if ( ts == null ) {
				continue;
			}
			if ( ifield == 1 ) {
				if ( id.equalsIgnoreCase(ts.getAlias()) ) {
					if ( (traceID != null) && !traceID.equals("") ) {
						if ( ts.getSequenceID().equalsIgnoreCase(traceID) ) {
							return i;
						}
					}
					else {
					    return i;
					}
				}
			}
			else if ( ifield == 2 ) {
				if ( id.equalsIgnoreCase(ts.getLocation()) ) {
					if ( (traceID != null) && !traceID.equals("") ) {
						if ( ts.getSequenceID().equalsIgnoreCase(traceID) ) {
							return i;
						}
					}
					else {
					    return i;
					}
				}
			}
		}
	}
	return -1;
}

/**
True if the first TS interval is greater.
@return true if the first TS interval is greater than the second TS interval
@param ts Time Series.
@param comparets Time Series to compare to.
*/
public static boolean isGreaterInterval ( TS ts, TS comparets )
{	boolean result = false;
	if ( ts == null || comparets == null ) {
		return result;
	}

	if (  ts.getDataIntervalBase() > comparets.getDataIntervalBase() ) {
		result = true;
	}

	return result;
}

/**
True if the first TS interval is smaller.
@return true if the first TS interval is smaller than the second TS interval
@param ts Time Series.
@param comparets Time Series to compare to.
*/
public static boolean isSmallerInterval ( TS ts, TS comparets )
{	boolean result = false;
	if ( ts == null || comparets == null ) {
		return result;
	}

	if (  ts.getDataIntervalBase() < comparets.getDataIntervalBase() ) {
		result = true;
	}

	return result;
}

/**
Determine whether two time series have matching data intervals.  The intervals must exactly match and not
just be equivalent (i.e., 1Day != 24Hour).  The interval base and multipler from identifiers
are compared, which does not require full time series objects.
@return true if the time series have the same data interval, false if not.
@param tsident1 first time series identifier to check
@param tsident2 second time series identifier to check
*/
public static boolean intervalsMatch ( TSIdent tsident1, TSIdent tsident2 )
{
	if ( (tsident1 == null) || (tsident2 == null) ) {
		return false;
	}
	if ( (tsident1.getIntervalBase() == tsident2.getIntervalBase()) &&
		(tsident1.getIntervalMult() == tsident2.getIntervalMult()) ) {
		return true;
	}
	return false;
}

/**
Determine whether two time series have matching data intervals.  The intervals must exactly match and not
just be equivalent (i.e., 1Day != 24Hour).
@return true if the time series have the same data interval, false if not.
@param ts1 first time series to check
@param ts2 second time series to check
*/
public static boolean intervalsMatch ( TS ts1, TS ts2 )
{
    List<TS> tslist = new ArrayList<TS>(2);
    tslist.add ( ts1 );
    tslist.add ( ts2 );
    return intervalsMatch ( tslist );
}

/**
Determine whether a list of time series have matching data intervals.  The intervals must exactly match and not
just be equivalent (i.e., 1Day != 24Hour).
@return true if all the time series in the list have the same interval as
the first time series in the list, false if not
@param tsList List of time series to check
*/
public static boolean intervalsMatch ( List<TS> tsList )
{	if ( tsList == null ) {
		return false;
	}
	if ( tsList.size() == 0 ) {
		return true;
	}
	// Find first non-null time series to compare...
	for ( TS ts : tsList ) {
		if ( ts != null ) {
		    return intervalsMatch ( tsList, ts.getDataIntervalBase(), ts.getDataIntervalMult() );
		}
	}
	// Could not find non-null time series to check
	return false;
}

/**
Determine whether a list of time series have data intervals that match the specified data interval.
The intervals must exactly match and not just be equivalent (i.e., 1Day != 24Hour).
@return true if all the time series in the list have the same interval as specified, false if not.
@param tsList List of time series to check
@param intervalBase Data interval base (e.g., TimeInterval.HOUR)
@param intervalMult Data interval multiplier
*/
public static boolean intervalsMatch ( List<? extends TS> tsList, int intervalBase, int intervalMult )
{
	if ( tsList == null ) {
		return false;
	}
	for ( TS ts : tsList ) {
		if ( ts == null ) {
			//Message.printWarning ( 3, "TSUtil.intervalsMatch", "TS [" + i + "] is null" );
			return false;
		}
		if ( (ts.getDataIntervalBase() != intervalBase) || (ts.getDataIntervalMult() != intervalMult) ) {
			return false;
		}
	}
	// All the intervals match...
	return true;
}

/**
Determine the maximum time series values.  The receiving time series
description and genesis information are updated to reflect the processing.
@return The time series with maximum time series values in each interval.
@param ts Time series to be added modified.
@param ts_to_check Time series to check against "ts".
@exception TSException if there is an error processing the time series.
*/
public static TS max ( TS ts, TS ts_to_check )
throws TSException
{	if ( ts == null ) {
		// Nothing to do...
		String message = "Null time series";
		Message.printWarning ( 2, "TSUtil.max", message );
		throw new TSException ( message );
	}
	if ( ts_to_check == null ) {
		// Nothing to do...
		String message = "Null time series to check";
		Message.printWarning ( 2, "TSUtil.max", message );
		throw new TSException ( message );
	}
	// Else, set up a list and call the overload routine...
	List<TS> v = new ArrayList<TS>(1);
	v.add ( ts_to_check );
	TS ts2 = max ( ts, v );
	return ts2;
}

/**
Determine the maximum values in a list of time series.  The receiving time
series description and genesis information are updated to reflect the addition.
@return The time series with maximum time series values in each interval.
@param ts Time series to be modified.
@param ts_to_check List of time series to check against "ts".
@exception RTi.TS.TSException if there is an error processing the time series.
*/
public static TS max (	TS ts, List<TS> ts_to_check )
throws TSException
{	String message, routine = "TSUtil.max(TS,Vector)";
	int	dl = 20;
	TS tspt = null;
	double add = 0.0, mult = 1.0;
	int set_count = 0;

	// Make sure that the pointers are OK...

	if ( ts_to_check == null ) {
		message = "NULL time series pointer for TS list";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	if ( ts == null ) {
		message = "NULL time series pointer for TS to receive max";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Make sure that the intervals match.  Currently this is a requirement
	// to make sure that the results are not misrepresented.  At some point
	// may want to overload to allow a period to be added.

	try {
	if ( !intervalsMatch(ts_to_check, ts.getDataIntervalBase(), ts.getDataIntervalMult()) ) {
		message = "All time series in the list are not of interval " +
		ts.getDataIntervalBase() + "," + ts.getDataIntervalMult();
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Now loop through the time series list and modify the primary time series...

	// Set starting and ending time for time loop based on period of "ts"...

	int ntslist = ts_to_check.size();
	String req_units = ts.getDataUnits ();
	DataUnitsConversion conversion = null;
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	int tspt_interval_base;
	double data_value, data_value_to_check;

	for ( int i = 0; i < ntslist; i++ ) {
		set_count = 0;
		tspt = ts_to_check.get(i);
		if ( tspt == null ) {
			message = "Trouble getting [" + i + "]-th time series in list";
			Message.printWarning ( 2, routine, message );
			throw new TSException ( message );
		}
		// Get the units conversions to convert to the final TS...
		try {
		    conversion = DataUnits.getConversion( tspt.getDataUnits(), req_units );
			mult = conversion.getMultFactor();
			add = conversion.getAddFactor();
		}
		catch ( Exception e ) {
			// Can't get conversion.  This may not be a fatal
			// error, but we don't want to allow different units to be summed so return...
			message = "Trouble getting conversion from \"" + tspt.getDataUnits() + "\" to \"" + req_units + "\"";
			Message.printWarning ( 2, routine, message );
			throw e;
		}
		// Work on the one time series...

		tspt_interval_base = tspt.getDataIntervalBase();

		if ( tspt_interval_base == TimeInterval.IRREGULAR ) {
			// For now, don't support...
			Message.printWarning ( 2, routine, "IrregularTS not supported.  Not processing max()." +
			tspt.getIdentifier().toString() );
			continue;
		}
		else {
		    // Regular interval.  Loop using addInterval...
			DateTime start_date = new DateTime ( ts.getDate1() );
			DateTime end_date = new DateTime ( ts.getDate2() );
			DateTime date = new DateTime ( start_date );
			
			for ( ; date.lessThanOrEqualTo( end_date); date.addInterval(interval_base, interval_mult)){
				data_value_to_check = tspt.getDataValue( date );
				if ( tspt.isDataMissing ( data_value_to_check)){
					// The value to check is missing so don't do it.  If we are tracking
					// missing, also set in the array.  This will prevent other time series from processing.
					continue;
				}
				// If here, there is a non-missing data value to check so do it...
				data_value = ts.getDataValue ( date );
				if ( ts.isDataMissing( data_value ) || (data_value_to_check > data_value) ) {
					// Original data is missing or less than than the max so reset...
					ts.setDataValue ( date, (data_value_to_check*mult + add) );
					++set_count;
				}
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"At " + date.toString() + ", changed max from " + data_value + " to " + data_value_to_check );
				}
			}
			ts.setDescription ( ts.getDescription() + ",max(" + tspt.getDescription () + ")" );
			ts.addToGenesis ( "Reset " + set_count + " values to max, comparing to \"" + tspt.getDescription() + "\"" );
		}
	}
	return ts;
	}
	catch ( Exception e ) {
		message = "Error doing max() of time series.";
		Message.printWarning ( 2, routine, message );
		Message.printWarning ( 2, routine, e );
		throw new TSException ( message );
	}
}

/**
Determine the minimum time series values.  The receiving time series
description and genesis information are updated to reflect the processing.
@return The time series with minimum time series values in each interval.
@param ts Time series to be added modified.
@param ts_to_check Time series to check against "ts".
@exception TSException if there is an error processing the time series.
*/
public static TS min ( TS ts, TS ts_to_check )
throws TSException
{	if ( ts == null ) {
		// Nothing to do...
		String message = "Null time series";
		Message.printWarning ( 2, "TSUtil.min", message );
		throw new TSException ( message );
	}
	if ( ts_to_check == null ) {
		// Nothing to do...
		String message = "Null time series to check";
		Message.printWarning ( 2, "TSUtil.min", message );
		throw new TSException ( message );
	}
	// Else, set up a list and call the overload routine...
	List<TS> v = new ArrayList<TS>(1);
	v.add ( ts_to_check );
	TS ts2 = min ( ts, v );
	return ts2;
}

/**
Determine the minimum values in a list of time series.  The receiving time
series description and genesis information are updated to reflect the processing.
@return The time series with minimum time series values in each interval.
@param ts Time series to be modified.
@param ts_to_check List of time series to check against "ts".
@exception RTi.TS.TSException if there is an error processing the time series.
*/
public static TS min ( TS ts, List<TS> ts_to_check )
throws TSException
{	String	message, routine = "TSUtil.min(TS,Vector)";
	int	dl = 20;
	TS	tspt = null;
	double add = 0.0, mult = 1.0;
	int set_count = 0;

	// Make sure that the pointers are OK...

	if ( ts_to_check == null ) {
		message = "NULL time series pointer for TS list";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	if ( ts == null ) {
		message = "NULL time series pointer for TS to receive min";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Make sure that the intervals match.  Currently this is a requirement
	// to make sure that the results are not misrepresented.  At some point
	// may want to overload to allow a period to be added.

	try {
	if ( !intervalsMatch(ts_to_check, ts.getDataIntervalBase(), ts.getDataIntervalMult()) ) {
		message = "All time series in the list are not of interval " +
		ts.getDataIntervalBase() + "," + ts.getDataIntervalMult();
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Now loop through the time series list and modify the primary time series...

	// Set starting and ending time for time loop based on period of "ts"...

	int ntslist = ts_to_check.size();
	String req_units = ts.getDataUnits ();
	DataUnitsConversion conversion = null;
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	int tspt_interval_base;
	double data_value, data_value_to_check;

	for ( int i = 0; i < ntslist; i++ ) {
		set_count = 0;
		tspt = ts_to_check.get(i);
		if ( tspt == null ) {
			message = "Trouble getting [" + i + "]-th time series in list";
			Message.printWarning ( 2, routine, message );
			throw new TSException ( message );
		}
		// Get the units conversions to convert to the final TS...
		try {
			conversion = DataUnits.getConversion( tspt.getDataUnits(), req_units );
			mult = conversion.getMultFactor();
			add = conversion.getAddFactor();
		}
		catch ( Exception e ) {
			// Can't get conversion.  This may not be a fatal
			// error, but we don't want to allow different units to be summed so return...
			message = "Trouble getting conversion from \"" + tspt.getDataUnits() + "\" to \"" + req_units + "\"";
			Message.printWarning ( 2, routine, message );
			throw e;
		}
		// Work on the one time series...

		tspt_interval_base = tspt.getDataIntervalBase();

		if ( tspt_interval_base == TimeInterval.IRREGULAR ) {
			// For now, don't support...
			Message.printWarning ( 2, routine, "IrregularTS not supported.  Not doing min()." + tspt.getIdentifier() );
			continue;
		}
		else {
		    // Regular interval.  Loop using addInterval...
			DateTime start_date = new DateTime ( ts.getDate1() );
			DateTime end_date = new DateTime ( ts.getDate2() );
			DateTime date = new DateTime ( start_date );
			
			for ( ; date.lessThanOrEqualTo( end_date); date.addInterval(interval_base, interval_mult)){
				data_value_to_check = tspt.getDataValue( date );
				if ( tspt.isDataMissing ( data_value_to_check)){
					// The value to check is missing so don't do it.  If we are tracking
					// missing, also set in the array.  This will prevent other time series from processing.
					continue;
				}
				// If here, there is a non-missing data value to check so do it...
				data_value = ts.getDataValue ( date );
				if ( ts.isDataMissing( data_value ) || (data_value_to_check < data_value) ) {
					// Original data is missing or greater than the minimum so reset...
					ts.setDataValue ( date, (data_value_to_check*mult + add) );
					++set_count;
				}
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"At " + date.toString() + ", changed min from " + data_value + " to " + data_value_to_check );
				}
			}
			ts.setDescription ( ts.getDescription() + ",min(" +
			tspt.getDescription () + ")" );
			ts.addToGenesis ( "Reset " + set_count + " values to min, comparing " + "to \"" + tspt.getDescription() + "\"" );
		}
	}
	return ts;
	}
	catch ( Exception e ) {
		message = "Error doing min() of time series.";
		Message.printWarning ( 2, routine, message );
		Message.printWarning ( 2, routine, e );
		throw new TSException ( message );
	}
}

/**
Get the count of missing data in a period, for all months in a year.
@return the count of missing values in a period.
@param start the starting date/time for the check.
@param end the ending date/time for the check.
*/
public static int missingCount ( TS ts, DateTime start, DateTime end )
throws Exception
{
    return missingCountForMonths ( ts, start, end, null );
}

/**
Get the count of missing data in a period, optionally analyzing only specified months.
@return the count of missing values in a period.
@param start the starting date/time for the check.
@param end the ending date/time for the check.
@param months an array specifying month numbers 1-12.  Any months that are indicated will be analyzed.
*/
public static int missingCountForMonths ( TS ts, DateTime start, DateTime end, int [] months )
throws Exception
{	if ( ts == null ) {
		return 0;
	}
	if ( start == null ) {
		start = new DateTime(ts.getDate1());
	}
	if ( end == null ) {
		end = new DateTime(ts.getDate2());
	}
	TSIterator tsi = ts.iterator(start,end);
	TSData data = null;
	int nMissing = 0;
	int month = 0;
	boolean found;
	int iMonth; // loop counter when looping through "month"
	while ( (data = tsi.next()) != null ) {
	    if ( months != null ) {
	        // Evaluate whether to skip the month
	        month = tsi.getDate().getMonth();
	        found = false;
	        for ( iMonth = 0; iMonth < months.length; iMonth++ ) {
	            if ( month == months[iMonth] ) {
	                found = true;
	                break;
	            }
	        }
	        if ( !found ) {
	            // Skip the month
	            continue;
	        }
	    }
		if ( ts.isDataMissing(data.getDataValue())) {
			++nMissing;
		}
	}
	return nMissing;
}

/**
Multiply an entire time series by another time series.  Only regular time series
can be multiplied.  If either value is missing, then the result is set to missing.
@param ts Time series to multiply.
@param tsm Time series to multiply by.
@exception Exception if there is an error processing.
*/
public static void multiply ( TS ts, TS tsm )
throws Exception
{	multiply ( ts, tsm, ts.getDate1(), ts.getDate2() );
}

/**
Multiply a time series by another time series for the indicated period.  Only
regular time series can be multiplied.  If either value is missing, then the result is set to missing.
@param ts Time series to multiply.
@param tsm Time series to multiply by.
@param startDate Date to start multiply.
@param endDate Date to stop multiply.
@exception Exception if there is an error processing.
*/
public static void multiply ( TS ts, TS tsm, DateTime startDate, DateTime endDate )
throws Exception
{	if ( ts == null ) {
		return;
	}
	if ( tsm == null ) {
		throw new Exception ( "Time series to multiply by is null." );
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits validDates = getValidPeriod ( ts, startDate, endDate );
	DateTime start = validDates.getDate1();
	DateTime end = validDates.getDate2();
	validDates = null;

	int intervalBase = ts.getDataIntervalBase();
	int intervalMult = ts.getDataIntervalMult();
	if ( (intervalBase != tsm.getDataIntervalBase()) || (intervalMult != tsm.getDataIntervalMult()) ) {
		throw new Exception ( "Cannot multiply data with different intervals (" +
		ts.getIdentifierString() + " and " + tsm.getIdentifierString() + ")." );
	}
	if ( intervalBase == TimeInterval.IRREGULAR ) {
		throw new Exception ( "Cannot multiply irregular interval data (" + ts.getIdentifierString() + ")" );
	}

	// Loop using addInterval...
	DateTime date = new DateTime ( start );
		
	double missing = ts.getMissing();
	double oldvalue = 0.0;
	double mult = 0.0;
	for ( ; date.lessThanOrEqualTo( end ); date.addInterval(intervalBase, intervalMult) ) {
		oldvalue = ts.getDataValue(date);
		mult = tsm.getDataValue(date);
		if ( ts.isDataMissing(oldvalue) || ts.isDataMissing(mult) ) {
			ts.setDataValue ( date, missing );
		}
		else {
		    ts.setDataValue ( date, oldvalue*mult );
		}
	}

	if ( tsm.getDataUnits().trim().length() > 0 ) {
	    ts.setDataUnits ( ts.getDataUnits() + "*" + tsm.getDataUnits() );
	}
	ts.setDescription ( ts.getDescription() + ", *" + tsm.getDescription() );
	
	// Fill in the genesis information...
    ts.addToGenesis ( "Multiplied " + startDate + " to " + endDate + " by " + tsm.getDescription() + "." );
}

// TODO SAM 2012-08-21 Consider a default precision if the time series date is null
/**
Create a new DateTime of a precision that matches the given time series.  Converting
from a low precision to high precision DateTime is typically handled by using first of
interval data (month=1, day=1, hour=0, minute=0, second=0).
If a non-null date is not provided, then the returned date/time should typically be
further manipulated to set its data values.
@return new DateTime of requested precision or null if the precision cannot
be determined from the time series.
@param ts Time series to check for date precision (date1 is checked).
@param date If null, the start date/time from the time series is used to initialize the date/time.
If not null, use the date information to initialize the returned date, to the appropriate precision.
*/
public static DateTime newPrecisionDateTime ( TS ts, DateTime date )
{	DateTime d1 = ts.getDate1();
	if ( d1 == null ) {
		return null;
	}
	DateTime d = new DateTime ( d1 );
	d.setDate ( date );
	// TODO sam 2017-04-09 thought the following was better but this method often gets called with date=null
	// so have to rely on d1 to provide something to return
	//DateTime d = new DateTime ( date );
	//d.setPrecision(d1.getPrecision());
	return d;
}

/**
Given a time series identifier as a string, determine the type of time series
to be allocated and creates a new instance.  Only the interval base and
multiplier are set (the memory allocation must occur elsewhere).  Time series metadata including the
identifier are also NOT set.
@param id time series identifier as a string.
@param long_id If true, then the string is a full identifier.  Otherwise,
the string is only the interval (e.g., "10min").
@return A pointer to the time series, or null if the time series type cannot be determined.
@exception if the identifier is not valid (e..g, if the interval is not recognized).
*/
public static TS newTimeSeries ( String id, boolean long_id )
throws Exception
{	int intervalBase = 0;
	int intervalMult = 0;
	String intervalString = "";
	if ( long_id ) {
		// Create a TSIdent so that the type of time series can be determined...

		TSIdent tsident = new TSIdent(id);

		// Get the interval and base...

		intervalString = tsident.getInterval();
		intervalBase = tsident.getIntervalBase();
		intervalMult = tsident.getIntervalMult();
	}
	else {
        // Parse a TimeInterval so that the type of time series can be determined...

	    intervalString = id;
		TimeInterval tsinterval = TimeInterval.parseInterval(intervalString);

		// Get the interval and base...

		intervalBase = tsinterval.getBase();
		intervalMult = tsinterval.getMultiplier();
	}
	// Now interpret the results and declare the time series...

	TS ts = null;
	if ( intervalBase == TimeInterval.MINUTE ) {
		ts = new MinuteTS();
	}
	else if ( intervalBase == TimeInterval.HOUR ) {
		ts = new HourTS();
	}
	else if ( intervalBase == TimeInterval.DAY ) {
		ts = new DayTS();
	}
	else if ( intervalBase == TimeInterval.MONTH ) {
		ts = new MonthTS();
	}
	else if ( intervalBase == TimeInterval.YEAR ) {
		ts = new YearTS();
	}
	else if ( intervalBase == TimeInterval.IRREGULAR ) {
		ts = new IrregularTS();
	}
	else {
        String message = "Cannot create a new time series for \"" + id + "\" (the interval \"" +
            intervalString + "\" [" + intervalBase + "] is not recognized).";
		Message.printWarning ( 3, "TSUtil.newTimeSeries", message );
		throw new Exception ( message );
	}

    // Set the multiplier...
	ts.setDataInterval(intervalBase,intervalMult);
	ts.setDataIntervalOriginal(intervalBase,intervalMult);
	// Set the genesis information
	ts.addToGenesis( "Created new time series with interval determined from TSID \"" + id + "\"" );

	// Return whatever was created...

	return ts;
}

/**
Normalize a time series by linearly interpolating each value between the time
series' minimum and maximum values (or zero, depending "minfromdata"), resulting in values in the range specified
by "newmin" and "newmax".  The data type is
left the same but the units are set to "".  If no data limits
can be found (all missing data), the data values are left as is but the other changes are made.
@param ts Time series to normalized (the time series will be modified).
@param minfromdata Indicate true if the minimum data value to use in the
normalization interpolation should be the minimum time series data value.
Indicate false if the minimum value should used for interpolation should be zero.
@param newmin New minimum value (generally specify 0.0).
@param newmax New maximum value (generally specify 1.0).
*/
public static void normalize ( TS ts, boolean minfromdata, double newmin, double newmax )
{	// Get the data limits...
	TSLimits limits = ts.getDataLimits ();
	double max = limits.getMaxValue ();
	double min = limits.getMinValue ();
	if ( !minfromdata ) {
		min = 0.0;
	}
	// Loop through the data and normalize the data.
	double oldvalue = 0.0;
	DateTime date = null;
	DateTime start = new DateTime ( ts.getDate1() );
	DateTime end = new DateTime ( ts.getDate2() );
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = (TSData)alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				oldvalue = tsdata.getDataValue();
				if ( !ts.isDataMissing(oldvalue) ) {
					tsdata.setDataValue(	MathUtil.interpolate(oldvalue,min,max,newmin,newmax) );
					// Have to do this manually since TSData are being modified directly to improve performance...
					ts.setDirty ( true );
				}
			}
		}
	}
	else {
	    // Loop using addInterval...
		date = new DateTime ( ts.getDate1() );
		for ( ;	date.lessThanOrEqualTo ( end );	date.addInterval(interval_base, interval_mult) ) {
			oldvalue = ts.getDataValue(date);
			if ( !ts.isDataMissing(oldvalue) ) {
				ts.setDataValue(date, MathUtil.interpolate(oldvalue,min,max, newmin,newmax));
			}
		}
	}
	// Set the time series properties...
	ts.setDataUnits ( "" );
	ts.addToGenesis ( 
	"Normalized time series using linear interpolation between min value " + min + " and max value " + max );
	ts.setDescription ( ts.getDescription() + ", normalized" );
}

/**
@return the number of intervals in a month.  This is useful when doing averages,
etc.  May need to later make this more generic.
@exception TSException if there is an error with input or an unsupported interval.
*/
public static int numIntervalsInMonth ( int month, int year, int interval_base, int interval_mult )
throws TSException
{
	if ( interval_base == TimeInterval.MONTH ) {
		if ( interval_mult == 1 ) {
			return 1;
		}
		else {
		    throw new TSException ( "Interval mult " + interval_mult + " is not valid for month data" );
		}
	}
	else if ( interval_base == TimeInterval.DAY ) {
		if ( interval_mult == 1 ) {
			return TimeUtil.numDaysInMonth(month,year);
		}
		else {
		    throw new TSException ( "Interval mult " + interval_mult + " is not valid for day data" );
		}
	}
	else if ( interval_base == TimeInterval.HOUR ) {
		if ( (interval_mult > 24) || (24%interval_mult != 0) ) {
			throw new TSException ( "Interval mult " + interval_mult + " is not valid for hour data" );
		}
		else {
		    int days_in_month = TimeUtil.numDaysInMonth(month,year);
			return days_in_month*24/interval_mult;
		}
	}
	else if ( interval_base == TimeInterval.MINUTE ) {
		if ( (interval_mult > 60) || (60%interval_mult != 0) ) {
			throw new TSException ( "Interval mult " + interval_mult + " is not valid for minute data" );
		}
		else {
		    int days_in_month = TimeUtil.numDaysInMonth(month,year);
			return days_in_month*24*60/interval_mult;
		}
	}
	else {
	    // Not a supported interval...
		String message = "Unknown interval " + interval_base;
		String routine = "TSUtil.numIntervalsInMonth";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
}

/**
Compute the relative difference of two time series: ts1 = (ts1 - ts2)/divisor.
Only regular time series can be analyzed.  If either value is missing, then the
result is set to missing.  If the denominator is zero, the result is set to
missing.  The units are set to "", which assumes that the relative difference
is determined using consistent units (although units are not currently checked).
@param ts1 Time series to be subtracted from.  This time series is also modified and returned.
@param ts2 Time series to subtract.
@param divisor The time series to divide by.  Although it can be any time
series, it should typically be ts1 or ts2.
@exception Exception if there is an error processing.
*/
public static void relativeDiff ( TS ts1, TS ts2, TS divisor )
throws Exception
{	relativeDiff ( ts1, ts2, divisor, ts1.getDate1(), ts1.getDate2() );
}

/**
Compute the relative difference of two time series: ts1 = (ts1 - ts2)/divisor.
Only regular time series can be analyzed.  If either value is missing, then the
result is set to missing.  If the denominator is zero, the result is set to
missing.  The units are set to "", which assumes that the relative difference
is determined using consistent units (although units are not currently checked).
@param ts1 Time series to be subtracted from.  This time series is also modified and returned.
@param ts2 Time series to subtract.
@param divisor The time series to divide by.  Although it can be any time
series, it should typically be ts1 or ts2.
@param start_date First date to process.
@param end_date Last date to process.
@exception Exception if there is an error processing.
*/
public static void relativeDiff ( TS ts1, TS ts2, TS divisor, DateTime start_date, DateTime end_date )
throws Exception
{	if ( ts1 == null ) {
		return;
	}
	if ( ts2 == null ) {
		throw new Exception ( "Time series to subtract is null." );
	}
	if ( divisor == null ) {
		throw new Exception ( "Divisor time series is null." );
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts1, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();
	valid_dates = null;

	int interval_base = ts1.getDataIntervalBase();
	int interval_mult = ts1.getDataIntervalMult();
	if ( (interval_base != ts2.getDataIntervalBase()) || (interval_mult != ts2.getDataIntervalMult()) ) {
		throw new Exception ( "Cannot determine relativeDiff for TS1, TS2 data with different intervals (" +
		ts1.getIdentifierString() + " and " + ts2.getIdentifierString() + ")." );
	}
	if ( (interval_base != divisor.getDataIntervalBase()) || (interval_mult != divisor.getDataIntervalMult()) ) {
		throw new Exception ( "Cannot determine relativeDiff for TS1, divisor data with different intervals (" +
		ts1.getIdentifierString() + " and " + divisor.getIdentifierString() + ")." );
	}
	if ( interval_base == TimeInterval.IRREGULAR ) {
		throw new Exception ( "Cannot compute relativeDiff for irregular interval data (" + ts1.getIdentifierString() + ")" );
	}

	// Loop using addInterval...
	DateTime date = new DateTime ( start );
		
	double missing = ts1.getMissing();
	double ts1value = 0.0;
	double ts2value = 0.0;
	double div = 0.0;
	for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
		ts1value = ts1.getDataValue(date);
		ts2value = ts2.getDataValue(date);
		div = divisor.getDataValue(date);
		if ( ts1.isDataMissing(ts1value) || ts2.isDataMissing(ts2value) || divisor.isDataMissing(div) || (div == 0.0) ) {
			ts1.setDataValue ( date, missing );
		}
		else {
		    ts1.setDataValue ( date, (ts1value - ts2value)/div );
		}
	}

	// Fill in the genesis information...

	ts1.setDataUnits ( "" );
	ts1.addToGenesis ("Converted to relativeDiff " + start_date.toString() +
		" to " + end_date.toString() + " by subtracting " + ts2.getDescription() + " and dividing by " +
		divisor.getDescription() );

	ts1.setDescription ( ts1.getDescription() + "-" + ts2.getDescription() + "/" + divisor.getDescription() );
}

/**
Replace a range of values in the time series data with a constant value.
The data range is checked regardless of whether the missing data value is in the range.
@param ts Time series to update.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param minValue Minimum data value to replace.
@param maxValue Maximum data value to replace.
@param matchFlag flag to match, in addition to matching the numerical value (ok if no numerical value) - currently
a simple case-dependent string match is performed
@param newValue Replacement data value (optional - do not need if action is specified).
@param action Action to perform: "Remove" to remove the point (only for irregular interval, treated as SetMissing
for regular interval data), "SetMissing" to set values to missing, and otherwise use "newvalue" to replace.
@param analysisWindowStart the starting date/time within a year to start the replacement,
consistent with the time series interval
@param analysisWindowEnd the ending date/time within a year to start the replacement,
consistent with the time series interval
@param setFlag flag value to set on data values when resetting a value
@param setFlagDesc description for set flag
*/
public static void replaceValue ( TS ts, DateTime start_date, DateTime end_date, Double minValue,
	Double maxValue, Double newValue, String matchFlag, String action,
	DateTime analysisWindowStart, DateTime analysisWindowEnd, String setFlag, String setFlagDescription )
{	String routine = "replaceValue";
    if ( (newValue == null) && ((action == null) || action.equals("")) ) {
        throw new InvalidParameterException(
            "Neither new value or action have been specified.  Cannot replace value in time series." );
    }
    if ( (minValue == null) && ((matchFlag == null) || matchFlag.equals("")) ) {
        throw new InvalidParameterException(
            "Neither minimum value or match flag have been specified.  Cannot replace value in time series." );
    }
    double minvalue = Double.NaN;
    if ( minValue != null ) {
        minvalue = minValue; // Does this increase performance?
    }
    double maxvalue = Double.NaN;
    if ( maxValue != null ) {
        maxvalue = maxValue;
    }
    double newvalue = Double.NaN;
    if ( newValue != null ) {
        newvalue = newValue;
    }
    // Get valid dates because the ones passed in may have been null...
	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();
	//if ( Message.isDebugOn ) {
	//	Message.printDebug(1, routine, "Period for replace is " + start + " to " + end);
	//}
	boolean doRemove = false;
	boolean doSetMissing = false;
	double missing = ts.getMissing();
	if ( action != null ) {
	    if ( action.equalsIgnoreCase("Remove") ) {
	        doRemove = true;
	    }
	    else if ( action.equalsIgnoreCase("SetMissing") ) {
            doSetMissing = true;
        }
	}
	boolean doMatchValue = false; // Whether checking numerical value for match
	if ( minValue != null ) {
	    doMatchValue = true;
	}
    boolean doMatchFlag = false; // Whether checking flag for match
    if ( (matchFlag != null) && !matchFlag.equals("") ) {
        doMatchFlag = true;
    }
	boolean doSetFlag = false;
	if ( (setFlag != null) && !setFlag.equals("") ) {
	    doSetFlag = true;
	}

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	double value = 0.0;
	// Initialize in-year analysis window
	boolean doAnalysisWindow = false;
    DateTime windowStart = null;
    if ( analysisWindowStart != null ) {
        windowStart = new DateTime(analysisWindowStart);
    }
    DateTime windowEnd = null;
    if ( analysisWindowEnd != null ) {
        windowEnd = new DateTime(analysisWindowEnd);
    }
    if ( (windowStart != null) && (windowEnd != null) ) {
        doAnalysisWindow = true;
    }
    int replaceCount = 0; // Number of values modified
    boolean matchedForReplace = false;
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		// This returns a pointer to the full data list (not a copy)
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		boolean pointRemoved; // To know if remove was success
		for ( int i = 0; i < nalltsdata; i++ ) {
		    matchedForReplace = false;
			tsdata = alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.lessThan(start) ) {
			    // TODO SAM 2012-03-22 Why not specify period in iterator?
			    continue;
			}
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( doMatchValue ) {
				value = tsdata.getDataValue();
				// Check the value
				if ( maxValue == null ) {
					// Only checking minimum
					if ( value >= minvalue ) {
						// Found a value to process
						matchedForReplace = true;
					}
				}
				else if ( (value >= minvalue) && (value <= maxvalue) ) {
					// Checking minimum and maximum values
					// Found a value to process
					matchedForReplace = true;
    			}
			}
    	    if ( doMatchFlag ) {
    	    	// Also check the flag - this is ANDed with the value
    	        if ( matchFlag.equals(tsdata.getDataFlag()) ) {
    	        	// Flag matches the check value
    	            if ( !doMatchValue || (doMatchValue && matchedForReplace) ) {
    	                // Criteria are met
    	            	// - only checking flag or flag and value match check
    	                matchedForReplace = true;
    	            }
    	            else {
    	                // Set to false because value was not matched
    	                matchedForReplace = false;
    	            }
    	        }
    	        else {
    	        	// Flag was not matched so don't match
    	            matchedForReplace = false;
    	        }
    	    }
			if ( matchedForReplace ) {
				// Value and/or flag were matched above.
			    // Also check whether in the analysis window.
	            if ( doAnalysisWindow ) {
                    // Check analysis window after checking values to increase performance
                    windowStart.setYear(date.getYear());
                    windowEnd.setYear(date.getYear());
                    if ( date.lessThan(windowStart) || date.greaterThan(windowEnd) ) {
                        // Not in analysis window so don't process
                        continue;
                    }
                }
	            // If here have matched the value for replacement.
			    if ( doRemove ) {
			        // This will remove the point at the date (there should only be one matching date/time)...
			    	// The following is a performance hit because it searches the entire time series.
			    	// Just remove from the data array.
			        //pointRemoved = irrts.removeDataPoint(date);
			    	pointRemoved = irrts.removeDataPoint(i);
			    	if ( pointRemoved ) {
			    		// TODO SAM 2010-08-18 Evaluate changing to iterator and remove following code
			    		// Decrement counters
			    		--i;
			    		--nalltsdata;
			    		++replaceCount;
			    	}
			    }
			    else if ( doSetMissing ) {
			    	// Set the value to missing
			        tsdata.setDataValue(missing);
			        ++replaceCount;
			    }
			    else {
			    	// Set to the new value
			        tsdata.setDataValue(newvalue);
			        ++replaceCount;
			    }
			    // Set the data flag
			    if ( doSetFlag ) {
			        tsdata.setDataFlag(setFlag);
			    }
				// Have to do this manually since TSData are being modified directly to improve performance...
				ts.setDirty ( true );
			}
		}
	}
	else {
	    // Loop using addInterval...
		DateTime date = new DateTime ( start );
		TSData tsdata = new TSData();
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			value = ts.getDataValue ( date );
			matchedForReplace = false;
			if ( doMatchValue ) {
				if ( maxValue == null ) {
					// Only checking minimum
					if ( value >= minvalue ) {
						// Found a value to process
						matchedForReplace = true;
					}
				}
				else if ( (value >= minvalue) && (value <= maxvalue) ) {
					// Checking minimum and maximum values
					// Found a value to process
					matchedForReplace = true;
    			}
			}
            // Also check the flag
            if ( doMatchFlag ) {
                tsdata = ts.getDataPoint(date, tsdata);
                if ( matchFlag.equals(tsdata.getDataFlag()) ) {
                    if ( !doMatchValue || (doMatchValue && matchedForReplace) ) {
                        // Criteria are met
                        matchedForReplace = true;
                    }
                    else {
                        // Set to false because value was not matched
                        matchedForReplace = false;
                    }
                }
                else {
                    matchedForReplace = false;
                }
            }
			if ( matchedForReplace ) {
                // Also check whether in the analysis window.
		        if ( doAnalysisWindow ) {
		            // Do check after checking values to increase performance
	                windowStart.setYear(date.getYear());
	                windowEnd.setYear(date.getYear());
	                if ( date.lessThan(windowStart) || date.greaterThan(windowEnd) ) {
	                    // Not in analysis window so don't proces
	                    continue;
	                }
	            }
			    if ( doRemove || doSetMissing ) {
			        if ( doSetFlag ) {
			            ts.setDataValue ( date, missing, setFlag, 1 );
			        }
			        else {
			            ts.setDataValue ( date, missing );
			        }
			    }
			    else {
			        if ( doSetFlag ) {
			            ts.setDataValue ( date, newvalue, setFlag, 1 );
			        }
			        else {
			            ts.setDataValue ( date, newvalue );
			        }
			    }
			    ++replaceCount;
			}
		}
	}

	// Set the genesis information...

	if ( replaceCount > 0 ) {
		if ( (setFlagDescription != null) && !setFlagDescription.isEmpty() ) {
			// Description was provided
			ts.addDataFlagMetadata(new TSDataFlagMetadata(setFlag, setFlagDescription));
        }
        else {
        	// Auto-generate flag description - set doRemove because regular time series will have missing values
        	if ( doRemove ) {
        		ts.addDataFlagMetadata(new TSDataFlagMetadata(setFlag, "Removed values that matched min=" +
        			StringUtil.formatString(minValue,"%.6f") + " max=" + StringUtil.formatString(maxValue,"%.6f") +
        			" flag=" + matchFlag) );
        	}
        	else if ( doSetMissing ) {
        		ts.addDataFlagMetadata(new TSDataFlagMetadata(setFlag, "Replaced values with missing for values that matched min=" +
        			StringUtil.formatString(minValue,"%.6f") + " max=" + StringUtil.formatString(maxValue,"%.6f") +
        			" flag=" + matchFlag) );
        	}
        	else {
        		ts.addDataFlagMetadata(new TSDataFlagMetadata(setFlag, "Replaced values with " +
        			StringUtil.formatString(newValue,"%.6f") +
        			"for values that matched min=" + StringUtil.formatString(minValue,"%.6f") +
        			" max=" + StringUtil.formatString(maxValue,"%.6f") + " flag=" + matchFlag) );
        	}
        }
    	if ( doRemove || doSetMissing ) {
    	    ts.setDescription ( ts.getDescription() + ", replaceValue(" +
                StringUtil.formatString(minvalue, "%.3f") + "," +
                StringUtil.formatString(maxvalue, "%.3f") + "," +
                action + ")" );
            ts.addToGenesis ( "Replace " + StringUtil.formatString(minvalue,"%.3f")+
               " - " + StringUtil.formatString(maxvalue,"%.3f") + " using action \"" + action + "\" " +
               start + " to " + end + " (" + replaceCount + " occurrences)." );
    	}
    	else {
    	    ts.setDescription ( ts.getDescription() + ", replaceValue(" +
                StringUtil.formatString(minvalue, "%.3f") + "," +
                StringUtil.formatString(maxvalue, "%.3f") + "," +
                StringUtil.formatString(newvalue, "%.3f") + ")" );
        	ts.addToGenesis ( "Replace " + StringUtil.formatString(minvalue,"%.3f")+
            	" - " + StringUtil.formatString(maxvalue,"%.3f") + " with " +
            	StringUtil.formatString(newvalue,"%.3f") + " " + start + " to " + end +
            	" (" + replaceCount + " occurrences)." );
    	}
	}
}

/**
Scale the entire time series by a constant value.
@param scale Data value to scale time series.
@param ts Time series to scale.
@exception Exception if the scale value is bad or another error occurs.
*/
public static void scale ( TS ts, double scale )
throws Exception
{	String routine = "TSUtil.scale";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Else, use the overall start and end dates for filling...

	scale ( ts, ts.getDate1(), ts.getDate2(), -1, scale );
}

/**
Scale the time series by a constant value.
@param ts Time series to scale.
@param start_date Date to start scaling.
@param end_date Date to stop scaling.
@param scale Data value to scale time series.
@exception Exception if the scale value is bad or another error occurs.
*/
public static void scale ( TS ts, DateTime start_date, DateTime end_date, double scale )
throws Exception
{	String  routine = "TSUtil.scale";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Use the main utility routine...

	scale ( ts, start_date, end_date, -1, scale );
}

/**
Scale the time series by a constant value.
@param ts Time series to scale.
@param start_date Date to start scaling.
@param end_date Date to stop scaling.
@param month Month to scale (1-12).  If negative, scale every month.
@param scale Data value to scale time series.
@exception Exception if the scale value is bad or another error occurs.
*/
public static void scale ( TS ts, DateTime start_date, DateTime end_date, int month, double scale )
throws Exception
{	scale ( ts, start_date, end_date, month, "" + scale );
}

/**
Scale the time series by a constant value.
@param ts Time series to scale.
@param start_date Date to start scaling.
@param end_date Date to stop scaling.
@param month Month to scale (1-12).  If negative, scale every month.
@param ScaleValue Data value to scale time series, as a number, DaysInMonth, or
DaysInMonthInverse.  For the latter two, the number of days in the month, as a
floating number, will be used as the scale value.  This is useful for
converting between a monthly interval and an interval shorter than monthly.
@exception Exception if the scale value is bad or another error occurs.
*/
public static void scale ( TS ts, DateTime start_date, DateTime end_date, int month, String ScaleValue )
throws Exception
{	String routine = "TSUtil.scale";
	double oldvalue;
	double ScaleValue_double = 1.0;	// Scale value as a double.
	boolean DaysInMonth_boolean = false; // If true, then ScaleValue is DaysInMonth
	boolean DaysInMonthInverse_boolean = false;
	if ( ScaleValue.equalsIgnoreCase("DaysInMonth") ) {
		DaysInMonth_boolean = true;
	}
	else if ( ScaleValue.equalsIgnoreCase("DaysInMonthInverse") ) {
		DaysInMonthInverse_boolean = true;
	}
	else {
	    if ( !StringUtil.isDouble(ScaleValue) ) {
			String message = "Scale value \"" + ScaleValue + "\" is not a number, DaysInMonth, or DaysInMonthInverse.";
			Message.printWarning ( 3, routine, message );
			throw new Exception ( message );
		}
		ScaleValue_double = StringUtil.atod(ScaleValue);
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			date = tsdata.getDate();
			if ( (month >= 0) && (date.getMonth() != month) ) {
				continue;
			}
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				oldvalue = tsdata.getDataValue();
				if ( !ts.isDataMissing(oldvalue) ) {
					if ( DaysInMonth_boolean ) {
						ScaleValue_double = TimeUtil.numDaysInMonth(tsdata.getDate());
					}
					else if ( DaysInMonthInverse_boolean ) {
						ScaleValue_double = 1.0/TimeUtil.numDaysInMonth(tsdata.getDate());
					}
					tsdata.setDataValue(oldvalue*ScaleValue_double);
					// Have to do this manually since TSData are being modified directly to improve performance...
					ts.setDirty ( true );
				}
			}
		}
	}
	else {
	    // Loop using addInterval...
		DateTime date = new DateTime ( start );
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			if ( (month >= 0) && (date.getMonth() != month) ) {
				continue;
			}
			oldvalue = ts.getDataValue(date);
			if ( !ts.isDataMissing(oldvalue) ) {
				if ( DaysInMonth_boolean ) {
					ScaleValue_double = TimeUtil.numDaysInMonth(date);
				}
				else if ( DaysInMonthInverse_boolean ) {
					ScaleValue_double = 1.0/TimeUtil.numDaysInMonth( date );
				}
				ts.setDataValue(date, oldvalue*ScaleValue_double);
			}
		}
	}

	// Fill in the genesis information...

	if ( month >= 0 ) { 
		if ( DaysInMonth_boolean || DaysInMonthInverse_boolean ) {
			ts.addToGenesis ( "Scaled " + start + " to " + end + " (month " + month + ") by " + ScaleValue + "." );
		}
		else {
		    ts.addToGenesis ( "Scaled " + start + " to " + end + " (month " + month + ") by " +
			StringUtil.formatString(ScaleValue_double,"%.6f") );
		}
	}
	else {
	    if ( DaysInMonth_boolean || DaysInMonthInverse_boolean ) {
			ts.addToGenesis ( "Scaled " + start + " to " + end + " by " + ScaleValue + "." );
		}
		else {
		    ts.addToGenesis ( "Scaled " + start + " to " + end + " by " +
			StringUtil.formatString(ScaleValue_double,"%.6f")+".");
		}
	}

	if ( DaysInMonth_boolean || DaysInMonthInverse_boolean ) {
		ts.setDescription ( ts.getDescription() + ", scale*" + ScaleValue );
	}
	else {
	    ts.setDescription ( ts.getDescription() + ", scale*" + StringUtil.formatString(ScaleValue_double,"%.3f") );
	}
}

/**
Scale the entire time series by a constant value, but only for the specified month.
@param ts Time series to scale.
@param month Month number (1-12) to scale.  Specify -1 to scale all months.
@param scale Data value to scale time series.
*/
public static void scaleByMonth ( TS ts, int month, double scale )
throws Exception
{	String routine = "TSUtil.scale";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Else, use the overall start and end dates for filling...

	scale ( ts, ts.getDate1(), ts.getDate2(), month, scale );
}

/**
Select time series from the list by matching criteria.  It is envisioned that
this method may be overloaded or transitioned to allow selects based on
header information or data.  Currently, its main purpose is to match one or
more time series identifiers strings with those in the time series list.
@return a Vector of matching time series (guaranteed to be non-null but may be
zero length).  If the requested TSID string has blank input type and name, then
the check is made ignoring the input type and name in the candidate time
series.  If the input type and name are present int he requested TSID, then the input type and name are checked.
@param tslist a list of TS to be checked.
@param tsids a list of time series identifier strings.  The comparison is made
by creating a TSIdent for each string and then calling
TSIdent.matches(TSIdentstring-from-TS,true,true), therefore the alias and input
name are checked for a match.  Each matching time series is returned in the list.
@param PropList props Properties to control processing, not currently used.
*/
public static List<TS> selectTimeSeries ( List<TS> tslist, List<String> tsids, PropList props )
{	List<TS> v = new ArrayList<TS>();
	int tsids_size = 0;
	if ( tsids != null ) {
		tsids_size = tsids.size();
	}
	int tslist_size = 0;
	if ( tslist != null ) {
		tslist_size = tslist.size();
	}
	TS ts = null;
	TSIdent tsident = null;
	int j;
	boolean check_input = true;
	for ( int i = 0; i < tsids_size; i++ ) {
		Message.printStatus ( 2, "", "Checking TSID \"" + tsids.get(i) + "\"" );
		try {
			tsident = new TSIdent ( (String)tsids.get(i) );
			if ( (tsident.getInputType().length() == 0) && (tsident.getInputName().length() == 0) ) {
				check_input = false;
			}
		}
		catch ( Exception e ) {
			// Ignore.  Just don't return a match.
		}
		for ( j = 0; j < tslist_size; j++ ) {
			ts = tslist.get(j);
			if ( tsident.matches( ts.getIdentifier().toString(true), true, check_input) ){
				v.add ( ts );
				Message.printStatus ( 2, "", "Match: \"" + ts.getIdentifier().toString(true)+ "\"" );
			}
			else {
				Message.printStatus ( 2, "", "No match: \"" + ts.getIdentifier().toString(true)+ "\"" );
			}
		}
	}
	return v;
}

/**
Set all the data in the time series to a constant value.
@param value Data value to set in the time series.
@param ts Time series to update.
*/
public static void setConstant ( TS ts, double value )
{	String routine = "TSUtil.setConstant";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Else, use the overall start and end dates for setting...

	setConstant ( ts, ts.getDate1(), ts.getDate2(), value );
}

/**
Set the time series data to a constant value.
@param ts Time series to update.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param value Data value to set as time series data.
*/
public static void setConstant ( TS ts, DateTime start_date, DateTime end_date, double value )
{	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				tsdata.setDataValue(value);
				// Have to do this manually since TSData are being modified directly to improve performance...
				irrts.setDirty ( true );
			}
		}
	}
	else {
	    // Loop using addInterval...
		DateTime date = new DateTime ( start );
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			ts.setDataValue ( date, value );
		}
	}

	// Set the genesis information...

	ts.setDescription ( ts.getDescription() + ", constant=" + StringUtil.formatString(value, "%.3f") );
	ts.addToGenesis ( "Set " + start + " to " + end + " to constant " + StringUtil.formatString(value,"%.3f") + "." );
}

/**
Set the entire time series to monthly values.  For example, values[0] is used for any date in January.
@param ts Time series to update.
@param values Data values to set time series data (the first value is for January, the last is for December).
If a value is null then the month value is not modified.  Missing values are expected to be set in the value array.
*/
public static void setConstantByMonth ( TS ts, Double values[] )
{	String routine = "TSUtil.setConstantByMonth";

	if ( ts == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Else, use the overall start and end dates for setting...

	setConstantByMonth ( ts, ts.getDate1(), ts.getDate2(), values );
}

/**
Set the entire time series to monthly values.  For example, values[0] is
used for any date in January.  This can be used, for example, to set an entire
period with a repetitive monthly pattern.
@param ts Time series to update.
@param start_date Date to start assignment.
@param end_date Date to stop assignment.
@param values Data values to set in the time series (the first value is for January, the last is for December) or
if null don't set the monthly value.  If setting to missing it is expected that the value has been set to the
missing value for the time series.
*/
public static void setConstantByMonth (	TS ts, DateTime start_date, DateTime end_date, Double values[] )
{	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();

	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	Double value;
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			value = values[date.getMonth() - 1];
			if ( value != null ) { // If null leave the old value
    			if ( date.greaterThanOrEqualTo(start) ) {
    				tsdata.setDataValue(value);
    				// Have to do this manually since TSData are being modified directly to improve performance...
    				irrts.setDirty ( true );
    			}
			}
		}
	}
	else {
	    // Loop using addInterval...
		DateTime date = new DateTime ( start );
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
		    value = values[date.getMonth() - 1];
		    if ( value != null ) {
		        ts.setDataValue ( date, value );
		    }
		}
	}

	// Add to the genesis information...

	ts.addToGenesis ( "Set " + start.toString() + " to " + end.toString() + " to monthly values:" );
	for ( int i = 0; i < 12; i++ ) {
		ts.addToGenesis ( TimeUtil.MONTH_ABBREVIATIONS[i] + ": " + StringUtil.formatString(values[i],"%12.2f"));
	}
}

/**
Set data by getting values from another time series.
@param dependentTS Time series to update.
@param independentTS Time series to copy from.
@exception Exception if an error occurs (usually null input).
*/
public static void setFromTS ( TS dependentTS, TS independentTS )
throws Exception
{	String routine = "TSUtil.setFromTS";

	if ( dependentTS == null ) {
		// No time series...
		Message.printWarning ( 2, routine, "Null time series" );
	}

	// Else, use the overall start and end dates for filling...

	setFromTS (	dependentTS, independentTS, dependentTS.getDate1(), dependentTS.getDate2(), null, null, null, true );
}

/**
Set data by getting values from another time series.
The data intervals do not need to be the same (truncation of dates will result if date precision is different).
@param dependentTS Time series to update.
@param independentTS Time series to copy from.
@param start_date Date to start data transfer (relative to the dependent time series).
@param end_date Date to stop the data transfer (relative to the dependent time series).
@param setWindowStart ignoring the year, the remainder indicates the start of the window within each year to set data
@param setWindowEnd ignoring the year, the remainder indicates the end of the window within each year to set data
@param props Properties to control the transfer.  Recognized properties are:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>HandleMissingHow</b></td>
<td><b>Indicates how missing data should be handled when encountered in the independent
time series.  If "SetMissing" then even the missing values will be transferred.
If "IgnoreMissing", then only non-missing values will be transferred.
If SetOnlyMissingValues, then only transfer the missing values (and optionally corresponding data flags).</b>
<td>SetMissing</td>
</tr>

<tr>
<td><b>SetFlag</b></td>
<td><b>String to use for the data flag when setting the value.</b>
<td>No flag set (previous flag remains).</td>
</tr>

<tr>
<td><b>SetFlagDescription</b></td>
<td><b>String description for the set flag.</b>
<td>No description set for the data flag.</td>
</tr>

<tr>
<td><b>TransferData</b></td>
<td><b>Indicates how data should be transferred from one time series to
another.  Using "ByDateTime" will cause the dates in both time series to match,
which may be an issue if one has leap year data and the other does not.  Using
"Sequentially" will result in data from the independentTS to be copied
sequentially, regardless of date/time, which will preserve the continuity of
the data but perhaps not the date/time</b>
<td>ByDateTime</td>
</tr>
</table>
@param setDataFlags if true, copy the data flags from the independent time series; if false leave the original data flags
@exception Exception if an error occurs (usually null input).
*/
public static void setFromTS ( TS dependentTS, TS independentTS, DateTime start_date, DateTime end_date,
    DateTime setWindowStart, DateTime setWindowEnd, PropList props, boolean setDataFlags )
throws Exception
{	String routine = "TSUtil.setFromTS";
	String message;

	if ( (dependentTS == null) || (independentTS == null) ) {
		return;
	}

	// Check the properties that influence this method...

	boolean transfer_bydate = true;	// Default - make dates match in both time series.
	boolean setMissing = true; // Default - either this or IgnoreMissing, which is setMissing=false
	boolean setOnlyMissingValues = false; // Only processing missing values
	boolean doSetFlag = false; // True if the SetFlag property was set - means a new flag is used (not from independent time series)
	String setFlag = null;
	String setFlagDescription = null;
	if ( props != null ) {
		String propVal = props.getValue("TransferData");
		if ( (propVal != null) && propVal.equalsIgnoreCase(TransferDataHowType.SEQUENTIALLY.toString()) ) {
			// Transfer sequentially...
			transfer_bydate = false;
		}
        propVal = props.getValue("HandleMissingHow");
        if ( propVal.equalsIgnoreCase("SetMissing") || (propVal == null) ) {
            setMissing = true; // Default
        }
        else if ( propVal.equalsIgnoreCase("IgnoreMissing") ) {
            // Ignore missing data (don't transfer)...
            setMissing = false;
        }
        else if ( propVal.equalsIgnoreCase("SetOnlyMissingValues") ) {
            // Only process missing values...
            setOnlyMissingValues = true;
            setMissing = true; // To simplify code below
        }
        else {
            // Unrecognized value
            throw new InvalidParameterException ( "HandleMissingHow=" + propVal + " is invalid." );
        }
        propVal = props.getValue("SetFlag");
        if ( (propVal != null) && !propVal.isEmpty() ) {
        	setFlag = propVal;
        	doSetFlag = true;
        }
        setFlagDescription = props.getValue("SetFlagDescription");
	}
	
	// It is possible that the data intervals are different, which will require some care.
	// This is really only relevant for the window or if a feature is added to control
	// which of the 1-to-many values are used to set.
	// A common use is to extract a single month's values to a year time series
	int dataIntervalComp = 0;
	if ( isGreaterInterval(dependentTS,independentTS) ) {
		dataIntervalComp = 1;
	}
	else if ( isSmallerInterval(dependentTS,independentTS) ) {
		dataIntervalComp = -1;
	}

	// Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod (dependentTS,start_date,end_date);
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();
	
	int interval_base = dependentTS.getDataIntervalBase();
	int interval_mult = dependentTS.getDataIntervalMult();
	int setCount = 0; // Number of values set
	if ( interval_base == TimeInterval.IRREGULAR ) {
		message = "Setting IrregularTS by using another time series is not supported";
		// Probably can just use the TSIterator between the two dates
		// regardless of the transfer flag.
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	DateTime date = new DateTime ( start );
	double dataValue = 0.0;

	TSIterator tsi = null;
	if ( !transfer_bydate ) {
		// Don't specify the end date because without doing some
		// computations, we don't know for sure.  This should get set
		// to the end date of the dependentTS, which is OK too.
		tsi = dependentTS.iterator(start,null);
	}

	boolean isMissing; // Whether a data value is missing
	TSData tsdata = new TSData();
	String dataFlag = null; // Data flag to transfer
	// Dates in the year corresponding to the window start and end, used when time series intervals are different
	DateTime dateSetWindowStart = null;
	boolean doSetWindow = false;
	if ( (setWindowStart != null) && (setWindowEnd != null) ) {
	    // Clone to have local copies - precision of this could be different from dependent
	    setWindowStart = new DateTime(setWindowStart);
	    setWindowEnd = new DateTime(setWindowEnd);
	    // Initialize to have correct precision of the window - should be same precision as independent
	    dateSetWindowStart = new DateTime(setWindowStart);
	    doSetWindow = true;
	}
	double outputMissingVal = dependentTS.getMissing();
	for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
		if ( transfer_bydate ) {
			// Default is to get the data value as if the intervals are the same - something will come back
			// This should work if the intervals are the same or the receiving is smaller interval
			dataValue = independentTS.getDataValue ( date );
			if ( (dataIntervalComp > 0) && doSetWindow ) {
				// The receiving time series has a larger interval.
				// For now loop through the independent for the window and return the first value
				// For example if the window is Jan-Jan a value will be returned for the Year interval
			    dateSetWindowStart.setYear(date.getYear());
			    dataValue = independentTS.getDataValue(dateSetWindowStart);  
			}
			if ( setDataFlags ) {
			    // Reuse the data point during data transfer
			    independentTS.getDataPoint( date, tsdata );
			    dataFlag = tsdata.getDataFlag();
			}
		}
		else {
            // Use the iterator...
			tsi.next();
			dataValue = tsi.getDataValue ();
			if ( setDataFlags ) {
			    dataFlag = tsi.getDataFlag();
			}
		}
		isMissing = independentTS.isDataMissing(dataValue);
		if ( !isMissing && setOnlyMissingValues ) {
		    // Only want to process missing values
		    continue;
		}
	    // Handle "IgnoreMissing" case (note setMissing=true was set above if setOnlyMissingValues=true)
		if ( isMissing && !setMissing ) {
		    continue;
		}
		if ( isMissing ) {
		    // Reset the missing value to that used in the output time series
		    dataValue = outputMissingVal;
		}
		// If the window is specified, check to see if the set date is in the window
		if ( doSetWindow ) {
		    // Set the window to the current year
		    // TODO SAM 2013-12-24 can possibly optimize to check year and previous year before doing reset
		    setWindowStart.setYear(date.getYear());
		    setWindowEnd.setYear(date.getYear());
		    if ( date.lessThan(setWindowStart) || date.greaterThan(setWindowEnd) ) {
		        // Not in desired window so don't set data
		        continue;
		    }
		}
		if ( setDataFlags ) {
			// Set using the data flag from the independent
			++setCount;
			if ( doSetFlag ) {
				// Reset with the new flag
				dependentTS.setDataValue ( date, dataValue, setFlag, 0 );
			}
			else {
				// Set with the flag from the independent time series
				dependentTS.setDataValue ( date, dataValue, dataFlag, 0 );
			}
		}
		else {
			++setCount;
			if ( doSetFlag ) {
				// Calling code specified the flag to set
				dependentTS.setDataValue ( date, dataValue, setFlag, 0 );
			}
			else {
				dependentTS.setDataValue ( date, dataValue );
			}
		}
	}
	
	// Fill in the genesis information...

	dependentTS.setDescription ( dependentTS.getDescription() +	", SetFromTS" );
	dependentTS.addToGenesis ( "Set data " + start.toString() +
		" to " + end.toString() + " by using values from " + independentTS.getIdentifierString() );
	String handleMissingHowString = ", HandleMissingHow=SetMissing";
	if ( !setMissing ) {
	    handleMissingHowString = ", HandleMissingHow=IgnoreMissing";
	}
    if ( setOnlyMissingValues ) {
        handleMissingHowString = ", HandleMissingHow=SetOnlyMissingValues";
    }
	if ( !transfer_bydate ) {
		dependentTS.addToGenesis ( "Data values were transferred by date/time" + handleMissingHowString + "." );
	}
	else {
        dependentTS.addToGenesis ( "Data values were transferred sequentially from start date/time" +
            handleMissingHowString + "." );
	}
	// TODO SAM 2015-05-23 Need to transfer data flags from independent time series, for all encountered flags
	// Set the data flag metadata if any values were set
	if ( setCount > 0 ) {
		if ( (setFlagDescription != null) && !setFlagDescription.isEmpty() ) {
			// Description was provided
			dependentTS.addDataFlagMetadata(new TSDataFlagMetadata(setFlag, setFlagDescription));
        }
        else {
        	// Auto-generate flag description
        	dependentTS.addDataFlagMetadata(new TSDataFlagMetadata(setFlag, "Set data using from " + independentTS.getIdentifierString()));
        }
	}
}

/**
Shift the time series in time (this should not be confused with a numerical
add or scale).  This results in a recreation of the time series
because data in a month may not shift directly to data in another month
(monthly time series shift with no issues because there are 12 values per year).
Time zone is NOT considered by this method.
@param oldts Time series to shift.
@param olddate Old date.
@param newdate New date to shift to.  The shift is therefore applied by moving
the old date to the new date and shifting all other dates accordingly.
@exception RTi.TS.TSException if there is a problem with input
*/
public static TS shift ( TS oldts, DateTime olddate, DateTime newdate )
throws Exception
{	String routine = "TSUtil.shift";
	int	dl = 1;

	String message;
	TS	newts = null;

	// If ts or dates null, throw exception.
	if ( oldts == null ) {
		message = "Time series is null for shift.";
		throw new TSException( message );
	}

	if ( olddate == null ) {
		message = "Old date is null to shift " + oldts.getIdentifierString();
		Message.printWarning ( 2, routine, message );
		throw new TSException( message );
	}
	if ( newdate == null ) {
		message = "New date is null to shift " + oldts.getIdentifierString();
		Message.printWarning ( 2, routine, message );
		throw new TSException( message );
	}

	//if ( Message.isDebugOn ) {
		Message.printStatus ( 1, routine,
		"Shifting " + oldts.getIdentifierString() + " from existing " + olddate + " to new " + newdate );
	//}

	// Get the original dates in the time series...

	DateTime olddate1 = new DateTime ( oldts.getDate1() );
	DateTime olddate2 = new DateTime ( oldts.getDate2() );

	// Determine if we have a date which is specified within the bounds of
	// the time series. May want to flag this with a boolean parameter.
	/*
	if ( olddate.lessThan( olddate1 ) || olddate.greaterThan( olddate2 ) ) {
		Message.printWarning( 1, routine, "The specified date: " +
		olddate + " does not lie within the original time series." );
		return null;
	}
	*/		
	DateTime date1 = new DateTime ( olddate1 );
	DateTime date2 = new DateTime ( olddate2 );

	// Compute the offset between the dates.  Subtract the first date
	// from the second, assuming that we generally offset from the past to
	// the future.  This should not affect the final results but needs to
	// be understood for debugging, etc....

	// This was not correct...  SAM 2001-03-20
	//DateTime offset = olddate.subtract ( newdate );
	// This is correct...
	// Consider the following examples...
	// olddate   newdate   offset
	// 1995      1996      positive
	// 1996      1995      negative
	DateTime offset = TimeUtil.diff ( newdate, olddate );
	//if ( Message.isDebugOn ) {
		Message.printStatus ( 1, routine, "Applying offset: " + offset );
	//}

	// Now shift to the new dates using the offset between the supplied
	// dates...  This is a new function...

	date1.add ( offset );
	date2.add ( offset );

	// This code is a little ugly.  Do we need a createTS routine that
	// will create one of the correct type?  If so, we still need to cast
	// so it is probably the same amount of work either way...

	int interval_base = oldts.getDataIntervalBase();
	int interval_mult = oldts.getDataIntervalMult();
	if ( interval_base == TimeInterval.YEAR ) {
		Message.printDebug ( dl, routine, "Creating new YearTS to shift from " + olddate + " to " + newdate );

		// Need to overload the contructor to the various TS to take
		// the dates and multiplier so it can call allocateDataSpace
		// directly.  Until then, do it the old way.  Also, we have to be careful with the copyHeader stuff!

		newts = new YearTS();
	}
	
	if ( interval_base == TimeInterval.MONTH ) {
		Message.printDebug ( dl, routine, "Creating new MonthTS to shift from " + olddate + " to " + newdate );
		newts = new MonthTS();
	}
	else if ( interval_base == TimeInterval.WEEK ) {
		Message.printDebug ( dl, routine, "Shifting a WeekTS is not currently supported." );
		
		//newts = new MonthTS();
		return null;
	}
	else if ( interval_base == TimeInterval.DAY ) {
		Message.printDebug ( dl, routine, "Creating new DayTS to shift from " + olddate + " to " + newdate );
		newts = new DayTS();
	}
	else if ( interval_base == TimeInterval.HOUR ) {
		Message.printDebug ( dl, routine, "Creating new HourTS to shift from " + olddate + " to " + newdate );
		newts = new HourTS();
	}
	else if ( interval_base == TimeInterval.MINUTE ) {
		Message.printDebug ( dl, routine, "Creating new MinuteTS to shift from " + olddate + " to " + newdate );
		newts = new MinuteTS();
	}
	else {
	    Message.printWarning( 1, routine,
		"Time series and output base interval of " + interval_base + " are not recognized." );
		return null;
	}

	// Copy the time series header...

	newts.copyHeader ( oldts );

	// Now set the dates, the interval, and the data space...

	newts.setDate1 ( date1 );
	newts.setDate2 ( date2 );
	newts.setDataInterval ( interval_base, interval_mult );
	newts.allocateDataSpace ();

	// Now loop through the dates and transfer the data...
	// In the loops, we increment the old and new dates separately and
	// break out when either has exceeded the end (should line up, but don't
	// know for sure with leap years, etc.!).

	DateTime olddate_i = new DateTime ( olddate1, DateTime.DATE_FAST );
	DateTime newdate_i = new DateTime ( date1, DateTime.DATE_FAST );

	double data_value;
	for ( ; newdate_i.lessThanOrEqualTo( date2 ) && olddate_i.lessThanOrEqualTo( olddate2 );
		newdate_i.addInterval(interval_base, interval_mult),
		olddate_i.addInterval(interval_base, interval_mult) ) {
		// Re-align if the leap years do not line up.  Only deal with daily for now...
		if ( interval_base == TimeInterval.DAY ) {
			if ( olddate_i.getMonth() == 3 ) {
				if ( olddate_i.getDay() == 1 ) {
					if ( (newdate_i.getMonth() == 2) && (newdate_i.getDay() == 29) ) {
						// Add the date 1 by one interval to make things line
						// up.  Will have missing in the new data for the 29th...
						newdate_i.addDay ( 1 );
					}
				}
			}
		}

		// Now get the data from the old time series and set in the new time series...
		data_value = oldts.getDataValue( olddate_i );
		newts.setDataValue( newdate_i, data_value );
		//Message.printStatus ( 1, routine, "Shifting old date " + olddate1.toString() +
		//" value " + data_value + " to new date " + date1 );

		if ( interval_base == TimeInterval.DAY ) {
			if ( olddate_i.getMonth() == 2 ) {
				if ( olddate_i.getDay() == 29 ) {
					if ( (newdate_i.getMonth() == 3) && (newdate_i.getDay() == 1) ) {
						// Decrement the date1 by one interval so the next iteration properly resets...
						newdate_i.addDay ( -1 );
					}
				}
			}
		}
	}

	// Now return the new time series...

	Message.printStatus ( 1, routine, "New period is " + newts.getDate1() + " to " + newts.getDate2() );
	newts.addToGenesis ( "Shifted dates from " + newdate + " to " + olddate );
	newts.addToGenesis ( "Period is " + newts.getDate1() + " to " + newts.getDate2() );
	return newts;
}

/**
Shift the time series in time (this should not be confused with a numerical
add or scale).  The original time series is modified and is returned.
The shift is accomplished by taking values from the original time series at
offsets from the current time and summing the weighted values.  An offset of -1
is the previous time step, 0 is the current time, and 1 is the next time step.
The new value is computed as data[i] = sum(data[interval_offsets[i]]*weights[i])
@param oldts Time series to shift.
@param interval_offsets Array of whole-interval shifts.
@param weights Array of double precision weights.
@return new time series.
@exception Exception if there is a problem with input
*/
public static TS shiftTimeByInterval ( TS oldts, int interval_offsets[], double weights[] )
throws TSException
{	String routine = "TSUtil.shiftTimeByInterval";

	String message;

	// If ts is null, throw exception.
	if ( oldts == null ) {
		message = "Time series is null for shift.";
		Message.printWarning ( 2, routine, message );
		throw new TSException( message );
	}

	if ( interval_offsets.length != weights.length ) {
		message = "Interval offsets and weights array lengths are different.";
		Message.printWarning ( 2, routine, message );
		throw new TSException( message );
	}
	int npairs = interval_offsets.length;

	DateTime date1 = new DateTime ( oldts.getDate1() );
	DateTime date2 = new DateTime ( oldts.getDate2() );

	// Copy the original time series.  The copy is used to retrieve data
	// but the original time series values are reset...

	TS tscopy = (TS)oldts.clone();

	int interval_base = oldts.getDataIntervalBase();
	int interval_mult = oldts.getDataIntervalMult();

	// Loop through the dates and transfer the data...

	double data_value, total;
	int i = 0;
	double missing = oldts.getMissing();
	// Will use an array of DateTimes.  Set the initial offsets and then iterate by one
	// interval with the other loop.  This is much faster than the old code that repositioned the date each time.
	DateTime [] shifted_date = new DateTime[npairs];
	boolean first_time = true; // Indicates to setup in first loop
	// Loop through each date/time in the time series that is being modified (oldts).
	for ( DateTime date = new DateTime(date1); date.lessThanOrEqualTo(date2);
		date.addInterval(interval_base,interval_mult) ) {
	    if ( first_time ) {
	        // First time through the loop.  Create and initialize the DateTimes.
	        for ( i = 0; i < npairs; i++ ) {
	            // Initialize to the start of the loop
	            shifted_date[i] = new DateTime (date);
	            // Now shift the date...
	            shifted_date[i].addInterval ( interval_base,interval_mult*interval_offsets[i] );
	        }
	        first_time = false;
	    }
	    else {
	        // Increment all of the dates by the interval.  This will track parallel with the
	        // original data and its iterated date.
	        for ( i = 0; i < npairs; i++ ) {
	            shifted_date[i].addInterval(interval_base,interval_mult);
	        }
	    }
	    // Initialize the new value to missing.
		total = missing;
		for ( i = 0; i < npairs; i++ ) {
			data_value = tscopy.getDataValue ( shifted_date[i] );
			// If missing, set the lagged data missing...
			if ( oldts.isDataMissing(data_value) ) {
				oldts.setDataValue ( date, missing );
				continue;
			}
			if ( oldts.isDataMissing(total) ) {
				total = data_value*weights[i];
			}
			else {
			    total += data_value*weights[i];
			}
		}
		oldts.setDataValue ( date, total );
	}

	// Now return the modified original time series...

	oldts.setDescription ( oldts.getDescription() + ",shift time" );
	oldts.addToGenesis ( "Shifted time using data[i]=sum(data[i + offset[i]]*wt[i]) where offset/wts: ");
	for ( i = 0; i < npairs; i++ ) {
		oldts.addToGenesis ( "    " + interval_offsets[i] + "," + StringUtil.formatString(weights[i], "%.6f") );
	}
	return oldts;
}

/**
Shift the time series from its current time zone to the specified time zone.
Currently, only shifts for HourTS are supported.
The shift is accomplished as follows:
<ul>
<li>	The time zone in the original time series is examined, based on the
	start date/time of the time series.  If the time zone is blank or there
	is no difference with the requested time zone, the reset_tz_if_same
	flag is checked to determine whether the new time zone is
	used in the dates in the time series and the time series is returned.
	Otherwise, the following steps are performed.
	</li>
<li>	If the requested time zone, when compared with the time zone in the
	original time series, results in an offset, a new copy of the time
	series is created, using the original header information, but with
	dates that have the new time zone.  The original time series is then
	iterated through and data are transferred to the new time series.  This
	transfer results in a consistent shift throughout the time zeries and
	should therefore occur using standard time zones only (not daylight savings time zones).</li>
</ul>
@param ts The time series to be modified.
@param req_tz A requested time zone abbreviation, as recognized by the
RTi.Util.Time.TZ class.  If null, or "" are specified, the original time series is returned.
@param from_date A DateTime used as a reference to determine daylight savings
time.  Currently this is not evaluated.  In the future it may be used to
allow conversion between standard and daylight time zones.  It is not currently
supported because standard/daylight shifts will vary in a long time series.
@param reset_tz_if_same If true and the requested time zone is numerically the
same as the original time series, then the requested time zone will be used
in the start and end dates for the time series (e.g, use to switch from "Z" to
"GMT").  If false and the time zones are numerically equal, use the original time zone.
@return the original time series if no shift has occurred, or a new time series with shifted data.
@exception if there is an error shifting the time zone (e.g., trying to shift
hourly data by 30 minutes, or the requested time zone is not recognized).
*/
public static TS shiftTimeZone ( TS ts, String req_tz, DateTime from_date, boolean reset_tz_if_same )
throws Exception
{	if ( (req_tz == null) || (req_tz.length() == 0) ) {
		// Not any information to do a shift so don't do anything...
		return ts;
	}
	// Get the shift between time zones...
	String ts_tz = ts.getDate1().getTimeZoneAbbreviation();
	@SuppressWarnings("deprecation")
	int offset = TZ.calculateOffsetMinutes ( ts_tz, req_tz, from_date );
	if ( offset == 0 ) {
		// The time zones are numerically equal.
		if ( reset_tz_if_same ) {
			// Reset the time zone in the start and end dates in
			// the time series (but do not do so in limits or irregular data...
			ts.getDate1().setTimeZone(req_tz);
			ts.getDate1Original().setTimeZone(req_tz);
			ts.getDate2().setTimeZone(req_tz);
			ts.getDate2Original().setTimeZone(req_tz);
		}
		return ts;
	}
	else {
	    // Need to create a new time series with the new dates and transfer the data.
		// Only support for hourly data...
		int interval_base = ts.getDataIntervalBase();
		int interval_mult = ts.getDataIntervalMult();
		if ( interval_base != TimeInterval.HOUR ) {
			throw new Exception ( "Only hourly data can have their time zone shifted.");
		}
		// TODO SAM 2004-11-08 The following is not very efficient
		// and could be optimized, in particular to avoid the initial
		// close of the data space and to avoid transfer altogether if
		// no shift in the data space is necessary.
		HourTS ts1 = (HourTS)ts;
		HourTS ts2 = (HourTS)ts1.clone();
		// Reset the dates...
		DateTime date = new DateTime ( ts1.getDate1() );
		date.shiftTimeZone ( req_tz );
		ts2.setDate1 ( date );
		if ( ts1.getDate1Original() != null ) {
			date = new DateTime ( ts1.getDate1Original() );
			date.shiftTimeZone ( req_tz );
			ts2.setDate1Original ( date );
		}
		date = new DateTime ( ts1.getDate2() );
		date.shiftTimeZone ( req_tz );
		ts2.setDate2 ( date );
		if ( ts1.getDate2Original() != null ) {
			date = new DateTime ( ts1.getDate2Original() );
			date.shiftTimeZone ( req_tz );
			ts2.setDate2Original ( date );
		}
		// Reallocate the memory...
		ts2.allocateDataSpace();
		// Transfer the data...
		date = new DateTime(ts1.getDate1());
		DateTime date_end = new DateTime(ts1.getDate2());
		DateTime date2 = new DateTime(ts2.getDate1());
		for ( ; date.lessThanOrEqualTo(date_end); date.addInterval(interval_base,interval_mult),
			date2.addInterval(interval_base,interval_mult) ) {
			ts2.setDataValue ( date2, ts1.getDataValue(date) );
		}
		return ts2;
	}
}

/**
Subtract one time series from another.  The receiving time series description
and genesis information are updated to reflect the subraction.
The IGNORE_MISSING flag is used for missing data.
@return The subtracted time series.
@param ts Time series to be subtracted from.
@param ts_to_subtract Time series to subtract from "ts".
*/
public static TS subtract ( TS ts, TS ts_to_subtract )
{	if ( ts == null ) {
		// Nothing to do...
		return ts;
	}
	if ( ts_to_subtract == null ) {
		// Nothing to do...
		return ts;
	}
	// Else, set up a vector and call the overload routine...
	List<TS> v = new ArrayList<TS>(1);
	v.add ( ts_to_subtract );
	double [] factor = new double[1];
	factor[0] = -1.0;
	try {
	    return add ( ts, v, factor, IGNORE_MISSING );
	}
	catch ( Exception e ) {;}
	return ts;
}

/**
Subtract a list of time series from another.  The receiving time series
description and genesis information are updated to reflect the addition.
The IGNORE_MISSING flag is used for missing data.
@return The subtracted time series.
@param ts Time series to be subtracted from.
@param ts_to_subtract List of time series to subtract from "ts".
*/
public static TS subtract ( TS ts, List<TS> ts_to_subtract )
throws Exception
{	return subtract ( ts, ts_to_subtract, IGNORE_MISSING );
}

/**
Subtract a list of time series from another.  The receiving time series
description and genesis information are updated to reflect the addition.
The IGNORE_MISSING flag is used for missing data.
@return The subtracted time series.
@param ts Time series to be subtracted from.
@param tsToSubtract List of time series to subtract from "ts".
@param missingFlag See documentation for add().
*/
public static <T extends TS> T subtract ( T ts, List<T> tsToSubtract, int missingFlag )
throws Exception {
	return subtract ( ts, tsToSubtract, missingFlag, null, null );
}

/**
Subtract a list of time series from another.  The receiving time series
description and genesis information are updated to reflect the addition.
The IGNORE_MISSING flag is used for missing data.
@return The subtracted time series.
@param ts Time series to be subtracted from.
@param tsToSubtract List of time series to subtract from "ts".
@param missingFlag See documentation for add().
@param subtractStart starting date/time for subtract, or null for full period
@param subtractEnd starting date/time for subtract, or null for full period
*/
public static <T extends TS> T subtract ( T ts, List<T> tsToSubtract, int missingFlag, DateTime subtractStart, DateTime subtractEnd )
throws Exception
{	// Call the main overload routine...
	if ( tsToSubtract == null ) {
		return ts;
	}
	int size = tsToSubtract.size();
	double [] factor = new double[size];
	for ( int i = 0; i < size; i++ ) {
		factor[i] = -1.0;
	}
	return add ( ts, tsToSubtract, factor, missingFlag, subtractStart, subtractEnd );
}

/**
Return an array containing the data values (including missing values) of the time series for the specified
period.  If the start date or end date are outside the period of
record for the time series, use the missing data value from the time series
for those values.  If the start date or end date are null, the start and end dates of the time series are used.
@return The array of data for to time series.  If an error, return null.
@param ts Time series to convert data to array format.
@param start_date Date corresponding to the first date of the returned array.
@param end_date Date corresponding to the last date of the returned array.
*/
public static double[] toArray ( TS ts, DateTime start_date, DateTime end_date )
throws InvalidTimeIntervalException
{	// Call the version that takes the month and pass a zero month
	// indicating that all months should be processed...
	return toArray ( ts, start_date, end_date, 0 );
}

/**
Return an array containing the data values (including missing values) of the time series for the specified
period.  If the start date or end date are outside the period of
record for the time series, use the missing data value from the time series
for those values.  If the start date or end date are null, the start and end
dates of the time series are used.  This is a utility routine mainly used by other versions of this routine.
@return The array of data for the time series.  If an error, return null.
@param ts Time series to convert data to array format.
@param start_date Date corresponding to the first date of the returned array.
@param end_date Date corresponding to the last date of the returned array.
@param month_index Month of interest (1=Jan, 12=Dec).  If zero, process all months.
*/
public static double[] toArray ( TS ts, DateTime start_date, DateTime end_date, int month_index )
throws InvalidTimeIntervalException
{	int [] month_indices = null;
	if ( month_index != 0 ) {
		month_indices = new int[1];
		month_indices[0] = month_index;
	}
	return toArray ( ts, start_date, end_date, month_indices, true );
}

/**
Return an array containing the data values of the time series for the specified
period.  If the start date or end date are outside the period of
record for the time series, use the missing data value from the time series
for those values.  If the start date or end date are null, the start and end
dates of the time series are used.  This is a utility routine mainly used by other versions of this routine.
@return The array of data for the time series.  If an error, return null.
@param ts Time series to convert data to array format.
@param start_date Date corresponding to the first date of the returned array.
@param end_date Date corresponding to the last date of the returned array.
@param month_index Month of interest (1=Jan, 12=Dec).  If zero, process all months.
@param includeMissing indicate whether missing values should be included in the result.
*/
public static double[] toArray ( TS ts, DateTime start_date, DateTime end_date, int month_index,
    boolean includeMissing )
throws InvalidTimeIntervalException
{   int [] month_indices = null;
    if ( month_index != 0 ) {
        month_indices = new int[1];
        month_indices[0] = month_index;
    }
    return toArray ( ts, start_date, end_date, month_indices, includeMissing );
}

/**
Return an array containing the data values of the time series for the specified
period, including missing values.  If the start date or end date are outside the period of
record for the time series, use the missing data value from the time series
for those values.  If the start date or end date are null, the start and end
dates of the time series are used.  This is a utility routine mainly used by other versions of this routine.
@return The array of data for the time series.  If an error, return null.  A zero size array may be returned.
@param ts Time series to convert data to array format.
@param startDate Date corresponding to the first date of the returned array.
@param endDate Date corresponding to the last date of the returned array.
@param monthIndices Months of interest (1=Jan, 12=Dec).  If null or an empty array, process all months.
*/
public static double[] toArray ( TS ts, DateTime startDate, DateTime endDate, int [] monthIndices )
throws InvalidTimeIntervalException
{
    return toArray ( ts, startDate, endDate, monthIndices, true );
}

/**
Return an array containing the data values of the time series for the specified
period.  If the start date or end date are outside the period of
record for the time series, use the missing data value from the time series
for those values.  If the start date or end date are null, the start and end
dates of the time series are used.  This is a utility routine mainly used by
other versions of this routine.
@return The array of data for the time series.  If an error, return null.  A zero size array may be returned.
@param ts Time series to convert data to array format.
@param start_date Date corresponding to the first date of the returned array.
@param end_date Date corresponding to the last date of the returned array.
@param month_indices Months of interest (1=Jan, 12=Dec).  If null or an empty array, process all months.
@param includeMissing if true, include missing values; if false, do not include missing values
*/
public static double[] toArray ( TS ts, DateTime start_date, DateTime end_date, int [] month_indices,
    boolean includeMissing )
throws InvalidTimeIntervalException
{
    return toArray ( ts, start_date, end_date, month_indices, includeMissing, true, null,
        TSToArrayReturnType.DATA_VALUE );
}

// TODO SAM 2013-02-04 Might want this to include a window, rather than just included months, in order
// to get seasonal data.
/**
Return an array containing the data values of the time series for the specified
period.  If the start date or end date are outside the period of
record for the time series, use the missing data value from the time series
for those values.  If the start date or end date are null, the start and end
dates of the time series are used.  This is a utility routine mainly used by
other versions of this routine.
@return The array of data for the time series.  If an error, return null.  A zero size array may be returned.
@param ts Time series to convert data to array format.
@param start_date Date corresponding to the first date of the returned array.
@param end_date Date corresponding to the last date of the returned array.
@param includeMonths Months of interest (1=Jan, 12=Dec) to include.  If null or an empty array, process all months.
@param includeMissing if true, include missing values; if false, do not include missing values
@param matchOtherNonmissing only used if "pairedTS" -s not null;
if true, then specify "pairedTS" and only return non-missing values that also are non-missing in "pairedTS";
if false, then specify "pairedTS" and only return non-missing values that are missing in "pairedTS"
other time series (this functionality is used to extract data for regression analysis)
@param pairedTS a second time series used to extract paired (or non-paired) data samples; if null then
the time series will not be checked as a constraint regardless of the "matchOtherNonmissing" value;
this capability is NOT availble for irregular time series
@param returnType indicates which value should be returned in the array; requesting the date/time will return
DateTime.getYear() for Year interval time series, DateTime.getAbsoluteMonth() for Month interval time series,
DateTime.getAbsoluteDay() for Day interval time series, and will throw an exception for other intervals.
*/
public static double[] toArray ( TS ts, DateTime start_date, DateTime end_date, int [] includeMonths,
    boolean includeMissing, boolean matchOtherNonmissing, TS pairedTS, TSToArrayReturnType returnType )
throws InvalidTimeIntervalException
{
    if ( pairedTS != null ) {
        if ( !TimeInterval.isRegularInterval(ts.getDataIntervalBase()) ) {
            throw new IrregularTimeSeriesNotSupportedException(
                "Irregular interval time series cannot have data array extracted using paired time series." );
        }
        if ( !TSUtil.intervalsMatch(ts, pairedTS) ) {
            throw new UnequalTimeIntervalException(
                "Time series from which to extract data has a different interval than paired time series." );
        }
    }
    // Get valid dates because the ones passed in may have been null...

	TSLimits valid_dates = getValidPeriod ( ts, start_date, end_date );
	DateTime start = valid_dates.getDate1();
	DateTime end = valid_dates.getDate2();
	
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();
	int size = 0;
	if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
		size = calculateDataSize ( ts, start, end );
	}
	else {
	    size = calculateDataSize ( start, end, interval_base, interval_mult );
	}
	if ( returnType == null ) {
	    returnType = TSToArrayReturnType.DATA_VALUE;
	}
	if ( returnType == TSToArrayReturnType.DATE_TIME ) {
	    // Only 1Year, 1Month, 1Day intervals are supported
	    if ( (interval_mult != 1) || ((interval_base != TimeInterval.YEAR) &&
	        (interval_base != TimeInterval.YEAR) && (interval_base != TimeInterval.YEAR)) ) {
	        throw new InvalidTimeIntervalException(
	            "Interval must be Year, Month, or Day (no multiplier) to return date/time as array.");
	    }
	}

    boolean [] includeMonthsMask = new boolean[12];
    if ( (includeMonths == null) || (includeMonths.length == 0) ) {
        for ( int i = 0; i < 12; i++ ) {
            includeMonthsMask[i] = true;
        }
    }
    else {
        for ( int i = 0; i < 12; i++ ) {
            includeMonthsMask[i] = false;
        }
        for ( int i = 0; i < includeMonths.length; i++ ) {
            includeMonthsMask[includeMonths[i] - 1] = true;
        }
    }

	if ( size == 0 ) {
		return new double[0];
	}

	double [] dataArray = new double[size]; // Initial size including missing
	int count = 0; // Number of values in array.
	int month = 0; // Month for date.
	double value; // Data value in time series

	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return null;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
                month = date.getMonth();
				if ( includeMonthsMask[month -1] ) {
	                value = tsdata.getDataValue ();
	                if ( includeMissing || !ts.isDataMissing(value) ) {
	                    if ( returnType == TSToArrayReturnType.DATA_VALUE ) {
	                        dataArray[count++] = value;
	                    }
	                    else if ( returnType == TSToArrayReturnType.DATE_TIME ) {
	                        if ( interval_base == TimeInterval.YEAR ) {
	                            dataArray[count++] = date.getYear();
	                        }
	                        else if ( interval_base == TimeInterval.MONTH ) {
                                dataArray[count++] = date.getAbsoluteMonth();
                            }
                            else if ( interval_base == TimeInterval.DAY ) {
                                dataArray[count++] = date.getAbsoluteDay();
                            }
	                    }
	                }
				}
			}
		}
	}
	else {
	    // Regular, increment the data by interval...
		DateTime date = new DateTime ( start );
		count = 0;
		boolean doTransfer = false;
		boolean isMissing = false;
		double value2;
		boolean isMissing2;
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
		    // First figure out if the data should be skipped because not in a requested month
            month = date.getMonth();
            if ( !includeMonthsMask[month -1] ) {
		        continue;
		    }
		    // Now transfer the value while checking the paired time series
		    doTransfer = false; // Do not transfer unless criteria are met below
            value = ts.getDataValue ( date );
            isMissing = ts.isDataMissing(value);
		    if ( pairedTS != null ) {
		        // Value in "ts" time series MUST be non-missing
		        if ( !isMissing ) {
    	            value2 = pairedTS.getDataValue ( date );
    	            isMissing2 = pairedTS.isDataMissing(value2);
    	            if ( matchOtherNonmissing ) {
    	                // Want non-missing in both "ts" and "pairedTS"
    	                if ( !isMissing2 ) {
    	                    doTransfer = true;
    	                }
    	            }
    	            else {
    	                // Want non-missing in "ts" and missing in "pairedTS"
    	                if ( isMissing2 ) {
    	                    doTransfer = true;
    	                }
    	            }
		        }
		    }
		    else {
    		    if ( includeMissing || !isMissing ) {
    		        // Value is not missing.
    		        doTransfer = true; 
    		    }
		    }
		    // OK to transfer the value...
			if ( doTransfer ) {
			    if ( returnType == TSToArrayReturnType.DATA_VALUE ) {
			        dataArray[count++] = value;
			    }
                else if ( returnType == TSToArrayReturnType.DATE_TIME ) {
                    if ( interval_base == TimeInterval.YEAR ) {
                        dataArray[count++] = date.getYear();
                    }
                    else if ( interval_base == TimeInterval.MONTH ) {
                        dataArray[count++] = date.getAbsoluteMonth();
                    }
                    else if ( interval_base == TimeInterval.DAY ) {
                        dataArray[count++] = date.getAbsoluteDay();
                    }
                }
			}
		}
	}

	if ( count != size ) {
		// The original array is too big and needs to be cut down to the exact size due to limited
	    // months or missing data being excluded)...
		double [] newDataArray = new double[count];
		for ( int j = 0; j < count; j++ ) {
			newDataArray[j] = dataArray[j];
		}
		return newDataArray;
	}
	// Return the full array...
	return dataArray;
}

/**
Return an array containing the data values of the time series for the specified
period.  If the start date or end date are outside the period of
record for the time series, use the missing data value from the time series
for those values.  If the start date or end date are null, the start and end
dates of the time series are used.
@return The array of data for the time series.  If an error, return null.
@param ts Time series to convert data to array format.
@param start_date Date corresponding to the first date of the returned array.
@param end_date Date corresponding to the last date of the returned array.
@param monthIndex Month of interest (1=Jan, 12=Dec).  If zero, process all months.
*/
public static double[] toArrayByMonth ( TS ts, DateTime start_date, DateTime end_date, int monthIndex )
throws InvalidTimeIntervalException
{	return toArray ( ts, start_date, end_date, monthIndex );
}

/**
Return an array containing the data values of the time series for the specified
period.  If the start date or end date are outside the period of
record for the time series, use the missing data value from the time series
for those values.  If the start date or end date are null, the start and end
dates of the time series are used.
@return The array of data for the time series.  If an error, return null.
@param ts Time series to convert data to array format.
@param start_date Date corresponding to the first date of the returned array.
@param end_date Date corresponding to the last date of the returned array.
@param monthIndex Month of interest (1=Jan, 12=Dec).  If zero, process all months.
@param includeMissing indicate whether missing values should be included.
*/
public static double[] toArrayByMonth ( TS ts, DateTime start_date, DateTime end_date, int monthIndex,
    boolean includeMissing )
throws InvalidTimeIntervalException
{   return toArray ( ts, start_date, end_date, monthIndex, includeMissing );
}

/**
Return an array containing the data values of the time series for the specified
period and for a date/time of interest.  The date/time should be the same precision as the
time series interval.  For example, if a monthly time series is processed and the date/time is
YYYY-01, then all the January values will be extracted.  If a daily time series is processed and the
date/time is YYYY-MM-01, then all the January 1 values are extracted.
If the start or end date are outside the period of
record for the time series, use the missing data value from the time series
for those values.  If the start date or end date are null, the start and end
dates of the time series are used.  Year, month, day, hour, and minute regular interval time series can be processed.
@return The array of data for the time series.  If an error, return null.  A zero size array may be returned.
An array of TSData is returned to allow the dates corresponding to values to be tracked, for example when
computing statistics like maximum where the date/time of the maximum is of interest.
@param ts Time series to convert data to array format.
@param startDate Date corresponding to the first date of the returned array.
@param endDate Date corresponding to the last date of the returned array.
@param dateTimeRequested a date/time matching the precision of the time series interval, for which values
are to be extracted.  For example, for a daily time series, this is the month and day of the year for
which values should be extracted (the other date/time parts are ignored).
@param includeMissing if true, include missing values; if false, do not include missing values
@exception InvalidTimeIntervalException if other than day or month time series are processed.
*/
public static TSData[] toArrayForDateTime ( TS ts, DateTime startDate, DateTime endDate,
    DateTime datetimeRequested, boolean includeMissing )
{   // Get month and data for the extraction
    int monthRequested = datetimeRequested.getMonth();
    int dayRequested = datetimeRequested.getDay();
    int hourRequested = datetimeRequested.getHour();
    int minuteRequested = datetimeRequested.getMinute();
    
    // Get valid dates because the ones passed in may have been null...
    TSLimits validDates = getValidPeriod ( ts, startDate, endDate );
    DateTime start = validDates.getDate1();
    DateTime end = validDates.getDate2();
    
    int intervalBase = ts.getDataIntervalBase();
    int intervalMult = ts.getDataIntervalMult();
    
    // Currently only support monthly and daily data
    if ( (intervalBase != TimeInterval.YEAR) && (intervalBase != TimeInterval.MONTH) &&
        (intervalBase != TimeInterval.DAY) && (intervalBase != TimeInterval.HOUR) &&
        (intervalBase != TimeInterval.MINUTE)) {
        throw new InvalidTimeIntervalException(
            "Only Year, Month, Day, Hour, and Minute time series can be processed.  Cannot process interval " +
            ts.getIdentifier().getInterval() + "." );
    }
    /*
    if ( intervalMult != 1 ) {
        throw new InvalidTimeIntervalException(
            "Only Year, Month, Day, Hour, and Minute time series with interval multipler of 1 can be processed.  Trying to process " +
            ts.getIdentifier().getInterval() + "." );
    }*/
    
    int size = 0; // Initial (maximum) size of data array - will be shortened at end
    if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
        size = calculateDataSize ( ts, start, end );
    }
    else {
        size = calculateDataSize ( start, end, intervalBase, intervalMult );
    }

    if ( size == 0 ) {
        return new TSData[0]; // Not possible to find data
    }

    TSData [] dataArray = new TSData[size]; // Initial size including missing
    int count = 0; // Number of values in array.
    int month = 0; // Month for iteration date.
    int day = 0; // Day for iteration date.
    int hour = 0; // Hour for iteration date.
    int minute = 0; // Minute for iteration date.
    double value; // Data value in time series

    if ( intervalBase == TimeInterval.IRREGULAR ) {
        throw new InvalidParameterException ( "Irregular time series are not currently supported.");
        /* Not currently supported
        // Get the data and loop through the vector...
        IrregularTS irrts = (IrregularTS)ts;
        List alltsdata = irrts.getData();
        if ( alltsdata == null ) {
            // No data for the time series...
            return null;
        }
        int nalltsdata = alltsdata.size();
        TSData tsdata = null;
        DateTime date = null;
        for ( int i = 0; i < nalltsdata; i++ ) {
            tsdata = (TSData)alltsdata.get(i);
            date = tsdata.getDate();
            if ( date.greaterThan(end) ) {
                // Past the end of where we want to go so quit...
                break;
            }
            if ( date.greaterThanOrEqualTo(start) ) {
                if ( month_indices_size == 0 ) {
                    // Transfer any value...
                    value = tsdata.getData ();
                    if ( includeMissing || !ts.isDataMissing(value) ) {
                        dataArray[count++] = value;
                    }
                }
                else {
                    // Transfer only if the month agrees with that requested...
                    month = date.getMonth();
                    for ( im = 0; im < month_indices_size; im++ ) {
                        if (month == month_indices[im]) {
                            value = tsdata.getData ();
                            if ( includeMissing || !ts.isDataMissing(value) ) {
                                dataArray[count++] = value;
                            }
                            break;
                        }
                    }
                }
            }
        }*/
    }
    else {
        // Regular, increment the data by interval...
        DateTime date = new DateTime ( start );
        count = 0;
        // FIXME SAM 2009-10-20 This is brute force.  It would be faster to add a year but leap years
        // cause problems.  Do this for now unless it is a performance problem.
        boolean processValue; // whether to process the data value
        for ( ; date.lessThanOrEqualTo( end ); date.addInterval(intervalBase, intervalMult) ) {
            month = date.getMonth();
            day = date.getDay();
            hour = date.getHour();
            minute = date.getMinute();
            processValue = false;
            if ( intervalBase == TimeInterval.YEAR ) {
                // No need to check any other date parts...
                processValue = true;
            }
            else if ( (intervalBase == TimeInterval.MONTH) && (month == monthRequested) ) {
                // Month must agree...
                processValue = true;
            }
            else if ( (intervalBase == TimeInterval.DAY) && (month == monthRequested) &&
                (day == dayRequested) ) {
                // Month and day must agree...
                processValue = true;
            }
            else if ( (intervalBase == TimeInterval.HOUR) && (month == monthRequested) &&
                (day == dayRequested) && (hour == hourRequested)) {
                // Month, day, and hour must agree...
                processValue = true;
            }
            else if ( (intervalBase == TimeInterval.MINUTE) && (month == monthRequested) &&
                (day == dayRequested) && (hour == hourRequested) && (minute == minuteRequested) ) {
                // Month, day, hour, and minute must agree...
                processValue = true;
            }
            if ( processValue ) {
                // Process the data value...
                value = ts.getDataValue ( date );
                if ( includeMissing || !ts.isDataMissing(value) ) {
                    // OK to include the value
                    TSData data = new TSData(new DateTime(date),value);
                    dataArray[count++] = data;
                }
            }
        }
    }

    if ( count != size ) {
        // The original array is too big and needs to be cut down to the exact size due to missing data
        // being excluded.  Retain the original object references.
        TSData [] new_dataArray = new TSData[count];
        for ( int j = 0; j < count; j++ ) {
            new_dataArray[j] = dataArray[j];
        }
        dataArray = null;
        return new_dataArray;
    }
    // Return the full array...
    return dataArray;
}

/**
Return an array containing the data values (do not include missing values) of the time series for
the specified period.  If the start date or end date are outside the period of
record for the time series, use the missing data value from the time series
for those values.  If the start date or end date are null, the start and end
dates of the time series are used.
@return The array of data for to time series.
@param ts Time series to convert data to array format.
@param start_date Date corresponding to the first date of the returned array.
@param end_date Date corresponding to the last date of the returned array.
*/
public static double[] toArrayNoMissing ( TS ts, DateTime start_date, DateTime end_date )
throws InvalidTimeIntervalException
{   // Call the version that takes the month indices but pass a null indicating to process all months...
    return toArray ( ts, start_date, end_date, null, false );
}

/** 
 * Returns the first editable Time Series in a list
 * @param tslist A list of TS (Time Series)
 * @return
 */
public static TS getFirstEditableTS(List<TS> tslist)
{
  int size =0;
  if ( tslist != null )
    {
      size = tslist.size();
    }
  TS ts;

  for ( int i = 0; i < size; i++ )
    {
      ts = tslist.get(i);
      if ( ts == null ) 
        {
          continue;
        }
      if (ts.isEditable())
        {
          return ts;
        }
    }
  return null;
}

}
