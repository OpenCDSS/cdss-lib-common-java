// TSUtil_ARMA - Perform Autoregressive Moving Average calculation (ARMA) to lag and attenuate a time series.

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

import java.util.List;

import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
 * Perform Autoregressive Moving Average calculation (ARMA) to lag and attenuate a time series.
 * @author sam
 *
 */
public class TSUtil_ARMA {
	
	/**
	 * Constructor.
	 */
	public TSUtil_ARMA () {
		
	}
	
	/**
	Shift and attenuate the time series using the ARMA (AutoRegressive Moving Average) approach.
	The time series is copied internally and is then updated where the output time series is computed with:
	O[t] = a1*O[t-1] + a2*O[t-2] + ... + ap*O[t-p] + b0*I[t] + b1*I[t-1] + ... + bq*I[t-q]
	Startup values that are missing in O are set to I.
	@param inputts input time series to shift (if outputts is null then this will be updated).
	@param outputts output time series (if null update the input time series).
	@param a Array of a coefficients.
	@param b Array of b coefficients.
	@param inputPreviousValues array of values to be prepended to input time series
	@param outputPreviousValues array of values to be prepended before output time series, prior to output start
	@param ARMA_interval The interval used to develop ARMA coefficients.  The
	interval must be <= that of the time series.  Use an interval like "1Day", "6Hour", etc.
	@param outputStart output start date/time or null to be same as input time series
	@param outputEnd output end date/time or null to be the same as input time series
	@return new time series.
	@exception Exception if there is a problem with input
	*/
	public TS ARMA ( TS inputts, TS outputts, String ARMA_interval, double a[], double b[],
		double [] inputPreviousValues, double [] outputPreviousValues, double outputMinimum, double outputMaximum,
		DateTime outputStart, DateTime outputEnd )
	throws Exception
	{	String message, routine = getClass().getSimpleName() + ".ARMA";

		// If input time series is null, throw exception.
		if ( inputts == null ) {
			message = "Input time series for ARMA is null.";
			Message.printWarning ( 2, routine, message );
			throw new TSException( message );
		}

		// Always need b but a is optional.  A single b value of 1 would be a "unity" operation.
		if ( a == null ) {
			// Define to simplify following code
			a = new double[0];
		}
		if ( (b == null) || (b.length == 0) ) {
			message = "Array of b-coefficients is zero-length.";
			Message.printWarning ( 2, routine, message );
			throw new TSException( message );
			
		}

		// Get the ratio of the time series interval to that of the ARMA
		// interval.  Use seconds to do the ratio.  Use TimeInterval as much
		// as possible because it throws exceptions.

		TimeInterval interval_data = new TimeInterval( inputts.getDataIntervalBase(), inputts.getDataIntervalMult() );
		TimeInterval interval_ARMA =TimeInterval.parseInterval ( ARMA_interval);
		int interval_ratio = 1;	// Factor to expand data due to ARMA interval being less than data interval.
		int ARMA_ratio = 1;	// Factor to skip expanded data due to ARMA interval being longer than for the expanded data.
		if ( !interval_data.equals(interval_ARMA) ) {
			// Intervals are different.  For daily and less, can get the
			// seconds and compare.  For longer, don't currently support.
			int seconds_data = interval_data.toSeconds();
			if ( seconds_data < 0 ) {
				message = "Cannot currently use ARMA with data interval > daily when ARMA interval is > daily";
				Message.printWarning ( 2, routine, message );
				throw new TSException ( message );
			}
			int seconds_ARMA = interval_ARMA.toSeconds();
			if ( seconds_ARMA < 0 ) {
				message = "Cannot currently use ARMA with ARMA interval > daily";
				Message.printWarning ( 2, routine, message );
				throw new TSException ( message );
			}
			int [] seconds_values = new int[2];
			seconds_values[0] = seconds_data;
			seconds_values[1] = seconds_ARMA;
			int [] common_denoms = MathUtil.commonDenominators ( seconds_values, 0 );
			if ( common_denoms == null ) {
				message = "Cannot currently use ARMA when ARMA interval and data interval do not have a common denominator.";
				Message.printWarning ( 2, routine, message );
				throw new TSException ( message );
			}
			// The interval ratio is the factor by which the original time
			// series needs to be expanded.  For example, if the time series
			// is 6 hour and the ARMA interval is 2 hours, seconds_data
			// will be 21600 and seconds_ARMA will be 7200.  The largest
			// common denominator will therefore be 7200 and the
			// interval_ratio will be 21600/7200 = 3.  If the data interval
			// is 6 hour and the ARMA interval is 4 hour, the
			// interval_ratio will still be 21600/7200 because the largest
			// common denominator is 2 hours.  The interval_ratio is just
			// used to expand the data.  The code that applies the
			// coefficients will operate on the ARMA interval.
			interval_ratio = seconds_data/common_denoms[0];
			ARMA_ratio = seconds_ARMA/common_denoms[0];
		}

		// Set some flags to simplify checks below
		boolean doMinCheck = false;
		boolean doMaxCheck = false;
		if ( !Double.isNaN(outputMinimum) ) {
			doMinCheck = true;
		}
		if ( !Double.isNaN(outputMaximum) ) {
			doMaxCheck = true;
		}

		// By default process all of the input time series - will limit to output period later
		DateTime date1 = new DateTime ( inputts.getDate1() );
		DateTime date2 = new DateTime ( inputts.getDate2() );

		// Because there is potential that the ARMA coefficients were calculated
		// at a different time step than the time series that is being analyzed
		// here, convert the time series to an array.
		// Always do the math on the input time series period - restrict to output period later

		double [] oldts_data0 = TSUtil.toArray ( inputts, inputts.getDate1(), inputts.getDate2() );
		// If additional input have been provided, prepend those to the array and keep track of the array
		// position for transfer back to the time series later.
		if ( inputPreviousValues == null ) {
			inputPreviousValues = new double[0];
		}
		// oldtsDataIndexForT0 is the index of index of the inputPreviousValues corresponding to t0,
		// basically the length of inputPreviousValues array in base interval, used to evaluate when previous values can be used.
		int oldtsDataIndexForT0 = 0;
		if ( inputPreviousValues.length > 0 ) {
			// Make sure that the size of the inputPreviousValues is the size of b minus 1
			// Otherwise the values would have no effect
			if ( (b.length - 1) != inputPreviousValues.length ) {
				message = "Number of input previous values (" + inputPreviousValues.length +
					") does not match the number of b-coefficients (" + b.length + ") minus 1 (" + (b.length - 1) + ").";
				Message.printWarning ( 2, routine, message );
				throw new TSException ( message );
			}
			double [] oldts_data_tmp = new double[oldts_data0.length + inputPreviousValues.length];
			System.arraycopy(inputPreviousValues, 0, oldts_data_tmp, 0, inputPreviousValues.length);
			System.arraycopy(oldts_data0, 0, oldts_data_tmp, inputPreviousValues.length, oldts_data0.length);
			// Reset to the merged array
			oldts_data0 = oldts_data_tmp;
			// The index of input time series after the InputPreviousValues
			// Needed to understand where OutputPreviousValues apply
			oldtsDataIndexForT0 = inputPreviousValues.length*ARMA_ratio;
		}
		if ( Message.isDebugOn ) {
			Message.printStatus(2,routine,"xx oldtsDataIndexForT0=" + oldtsDataIndexForT0);
		}
		
		// Create previous output values to simplify code below.
		if ( outputPreviousValues == null ) {
			outputPreviousValues = new double[0];
		}
		if ( outputPreviousValues.length > 0 ) {
			// Make sure that the size of the a array matches the OutputPreviousValues
			// Otherwise some of the index math gets messed up below since a is in one order and OutputPreviousValues is another.
			if ( a.length != outputPreviousValues.length ) {
				message = "Number of output previous values (" + outputPreviousValues.length +
					") does not match the number of a-coefficiencts (" + a.length + ").";
				Message.printWarning ( 2, routine, message );
				throw new TSException ( message );
			}
		}

		// Now expand the time series because of the interval_ratio.

		double [] oldts_data = null; // Old data in base interval (interval that divides into data and ARMA interval).
		double [] newts_data = null; // ARMA output in base interval.
		double missing = inputts.getMissing();
		if ( interval_ratio == 1 ) {
			// Just use the original array (no reason to expand)...
			oldts_data = oldts_data0;
			newts_data = new double[oldts_data.length];
			for ( int i = 0; i < oldts_data0.length; i++ ) {
				newts_data[i] = missing;
			}
			// Also use original OutputPreviousValues array as is
		}
		else {
		    // Allocate a new data array in the base interval.
			// Carry forward each original value as necessary to fill out the base interval data.
			// Important, all the iteration below occurs on the expanded input time series array, which may contain InputPreviousValues at front
			// Specifying OutputPreviousValues is relative to the array position of the original input array
			oldts_data = new double[oldts_data0.length*interval_ratio];
			newts_data = new double[oldts_data.length];
			int j = 0;
			for ( int i = 0; i < oldts_data0.length; i++ ) {
				for ( j = 0; j < interval_ratio; j++ ) {
					oldts_data[i*interval_ratio + j] = oldts_data0[i];
					newts_data[i*interval_ratio + j] = missing;
				}
			}
			// Also duplicate the output previous values, order is from old to new like time series
			double [] OutputPreviousValues2 = new double[outputPreviousValues.length*interval_ratio];
			for ( int i = 0; i < outputPreviousValues.length; i++ ) {
				for ( j = 0; j < interval_ratio; j++ ) {
					OutputPreviousValues2[i*interval_ratio + j] = outputPreviousValues[i];
					if ( Message.isDebugOn ) {
						Message.printStatus(2, routine, "OutputPreviousValues2[" + (i*interval_ratio + j) + "]=" + outputPreviousValues[i] );
					}
				}
			}
			// Reset so original array variable can be used below with output previous values at the base interval 
			outputPreviousValues = OutputPreviousValues2;
		}

		if ( Message.isDebugOn ) {
			for ( int i = 0; i < outputPreviousValues.length; i++ ) {
				Message.printStatus(2, routine, "After interval adjustment, OutputPreviousValues[" + (i) + "]=" + outputPreviousValues[i]);
			}
		}
		
		// If OutputPreviousValues are specified could have one of the following situations depending on number of a, b:
		// I=input time series, i=values from InputPreviousValues
		// O=calculated output time series, o=values from OutputPreviousValues
		//               t
		//               IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII (no InputPreviousValues)
		//           iiiiIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII (InputPreviousValues specified)
		//               OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO (no OutputPreviousValues)
		//            oooOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO (OutputPreviousValues within overall input array)
		//     ooooooooooOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO (OutputPreviousValues beyond input array)
		//               ^
		//               oldtsDataIndexForT0 (reflects previous input "i" values so would be 0 for all examples, other than 4 for second example)
		//               t=0 corresponds with the start of the input array + any input previous values
		//               t=-1 corresponds to the position of the value for a1 and b1
		//               t=-2 corresponds to the position of the value for a2 and b2
		//               etc.
		//
		// The ARMA processing below loops over the input time series array which may or may not include "i" in addition to "I".
		// The output array is created consistent with the input array ("i" and "I" extent).
		// To properly handle the output values "o" and "O" it is necessary to check for whether OutputPreviousValues is non-zero length and deal with offset from first I.
		
		int lastIndexForOutputPreviousValues = oldtsDataIndexForT0 + (a.length - 1)*ARMA_ratio; // Last t that depends on output previous values

		// Now perform the ARMA looping through the base interval data...

		double total = 0.0;
		int tpos = 0; // Position in t array for offset values
		int toffset = 0; // Offset from t to get value, can be negative (tpos = t + tpos)
		double data_value;
		int ia = 0, ib = 0;
		boolean value_set = false; // Indicates if newts_data[t] value has been set (e.g., to missing) and should not be reset by calculations
		// Loop through entire original input array, with previous input at the start
		for ( int t = 0; t < oldts_data.length; t++ ) {
			total = missing;
			value_set = false;
			// Want the first position to be t - 1 (one ARMA interval) from the current time.
			// The second point is t - 2 (two ARMA intervals) from the current time.
			for ( ia = 0; ia < a.length; ia++ ) {
				// ARMA_ratio of 1 gives j - ... 1, 2, 3, etc.
				// ARMA_ratio of 2 gives j - ... 2, 4, 6, etc.
				toffset = -(ia + 1)*ARMA_ratio;
				tpos = t + toffset;
				if ( Message.isDebugOn ) {
					Message.printStatus(2,routine,"ia=" + ia + ", t=" + t + ", tpos=" + tpos);
				}
				data_value = inputts.getMissing();
				if ( t < oldtsDataIndexForT0 ) {
					// Are processing data before original input time series start so output is always missing
					if ( Message.isDebugOn ) {
						Message.printStatus(2,routine,"processing before original input time series start so always missing, t (" + t +
							") < oldtsDataIndexForT0 (" + oldtsDataIndexForT0 + ") - setting newts_data[" + t + "].");
					}
					newts_data[t] = missing;
					value_set = true;
					break;
				}
				else if ( (t >= oldtsDataIndexForT0) && (t <= lastIndexForOutputPreviousValues) ) {
					// In the time slots where previous output is needed and outputPreviousValues can be used.
					if ( Message.isDebugOn ) {
						Message.printStatus(2,routine,"outputPreviousValues can be used since t (" + t + ") >= oldtsDataIndexForT0 (" + oldtsDataIndexForT0 +
							") and <= lastIndexForOutputPreviousValues (" + lastIndexForOutputPreviousValues + ").");
					}
					if ( outputPreviousValues.length == 0 ) {
						// No previous values were supplied
						// Since this is inside the a-coefficient loop the ARMA output is missing
						if ( Message.isDebugOn ) {
							Message.printStatus(2,routine,"xx outputPreviousValues size is 0 so set newts_data[" + t + "] to missing.");
						}
						newts_data[t] = missing;
						value_set = true;
						break;
					}
					else {
						// Have output previous values - a check above made sure the number of values matched the number of a-coefficients
						// outputPreviousValues array is in order earliest time to latest time so reverse the index to pull out from the end
						// toffset is negative going from -1, -2, etc. so the following works
						// However, fewer and fewer outputPreviousValues are needed as t moves forward so account for that
						if ( tpos < oldtsDataIndexForT0 ) {
							// Need to get data out of previous values
							data_value = outputPreviousValues[outputPreviousValues.length + toffset];
							if ( Message.isDebugOn ) {
								Message.printStatus(2, routine, "Using output (previous value), tpos=" + tpos + " oldtsDataIndexForT0=" + oldtsDataIndexForT0 +
									" data_value = " + outputPreviousValues[outputPreviousValues.length + toffset]);
							}
						}
						else {
							// Can get data out of output (not previous values array)
							data_value = newts_data[tpos];
							if ( Message.isDebugOn ) {
								Message.printStatus(2, routine, "Using output (not previous value), tpos=" + tpos + " oldtsDataIndexForT0=" + oldtsDataIndexForT0 +
									" data_value = " + outputPreviousValues[outputPreviousValues.length + toffset]);
							}
						}
					}
				}
				else {
					// After time slot where previous output values are an option
					// Get previous outflow value from array
					if ( Message.isDebugOn ) {
						Message.printStatus(2, routine, "After time slot where previous output values are an option so use newts_data[" + tpos + "] =" + data_value );
					}
					data_value = newts_data[tpos];
				}
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "After extraction, data_value=" + data_value );
				}
				// If here data_value came from either the output time series or was supplied by outputPreviousValues.
				// Continue with the logic
				if ( inputts.isDataMissing(data_value) ) {
					// Previous outflow value is missing so try using the input time series value...
					data_value = oldts_data[tpos];
					//Message.printStatus ( 1, routine,
					//"O missing I for " + shifted_date + " is " + data_value );
					if ( inputts.isDataMissing(data_value) ) {
						newts_data[t] = missing;
						//Message.printStatus ( 1, routine, "Setting O at " + date + " to " + data_value );
						//oldts.addToGenesis( "ARMA:  Using missing (" + StringUtil.formatString(
						// data_value,"%.6f") + ") at " + date +
						//" because input and output for a" + (i + 1) + " are missing.");
						value_set = true; // Value is set to missing
						break;
					}
				}
				// If get to here, have data so can increment another term in the total...
				if ( inputts.isDataMissing(total) ) {
					// Assign the value...
					total = data_value*a[ia];
				}
				else {
				    // Sum the value...
					total += data_value*a[ia];
				}
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "Total = " + total);
				}
			}
			if ( value_set ) {
				// newts_data[t] Set to missing above so no reason to continue processing the time step...
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "newts_data[" + t + "] = " + newts_data[t]);
				}
				continue;
			}
			// Process b-coefficient values
			// Want the values to be for offset of 0, t - 1 (one ARMA interval), t - 2 (two ARMA intervals), etc.
			for ( ib = 0; ib < b.length; ib++ ) {
				// ARMA_ratio of 1 gives j - ... 0, 1, 2, etc.
				// ARMA_ratio of 2 gives j - ... 0, 2, 4, etc.
				tpos = t - ib*ARMA_ratio;
				if ( tpos < 0 ) {
					// Before the initial data.  If inputPreviousValues were specified this is even before that.
					// Because t=0 corresponds to the start of the inputPreviousValues, once t is the original input (before previous values)
					// non-missing will result
					if ( Message.isDebugOn ) {
						Message.printStatus(2, routine, "ib=" + ib + " t=" + t + " tpos (" + tpos + ") < 0, before input previous values can be used so set output to missing" );
					}
					newts_data[t] = missing;
					//Message.printStatus ( 1, routine, "Setting O at " + date + " to " + data_value );
					//oldts.addToGenesis( "ARMA:  Using missing (" +
					//StringUtil.formatString(data_value,"%.6f") + ") at " + date +
					//" because input and output for a" + (i + 1) + " are missing.");
					value_set = true;
					break;
				}
				data_value = oldts_data[tpos];
				if ( inputts.isDataMissing(data_value) ) {
					if ( Message.isDebugOn ) {
						Message.printStatus(2, routine, "ib=" + ib + "t=" + t + " tpos=" + tpos + " data_value = oldts_data[" + tpos + "]=" + data_value );
					}
					newts_data[t] = missing;
					value_set = true;
					break;
				}
				// If get to here have non-missing data...
				if ( inputts.isDataMissing(total) ) {
					// Assign the value...
					total = data_value*b[ib];
				}
				else {
				    // Sum the value...
					total += data_value*b[ib];
				}
			}
			if ( value_set ) {
				// Set to missing above...
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "ib=" + ib + " newts_data[" + t + "] = " + newts_data[t] );
				}
				continue;
			}
			// Check to see if an output previous value has been specified
			// This is relative to the output start
			// TODO SAM 2016-09-21 why does this do nothing?  Were there supposed to be checks for maximum and minimum?
			if ( outputPreviousValues.length > 0 ) {
				if ( outputStart == null ) {
					
				}
			}
			// Check the minimum and maximum values
			if ( doMaxCheck && total > outputMaximum ) {
				total = outputMaximum;
			}
			if ( doMinCheck && total < outputMinimum ) {
				total = outputMinimum;
			}
			newts_data[t] = total;
			if ( Message.isDebugOn ) {
				Message.printStatus(2, routine, "newts_data[" + t + "] = " + newts_data[t]);
			}
		}

		// Routing of the individual values in the ARMA interval is now
		// complete.  Total back to the original data time step.  Use the
		// original oldts_data0 array to store the values (next step will put back in the time series)...

		int j = 0;
		double double_ratio = (double)interval_ratio;
		for ( int i = 0; i < oldts_data0.length; i++ ) {
			oldts_data0[i] = missing;
			for ( j = 0; j < interval_ratio; j++ ) {
				data_value = newts_data[i*interval_ratio + j];
				if ( inputts.isDataMissing(data_value) ) {
					break;
				}
				else {	// Have non-missing data...
					if ( inputts.isDataMissing(oldts_data0[i]) ) {
						oldts_data0[i] = newts_data[i*interval_ratio + j];
					}
					else {
					    oldts_data0[i] += newts_data[i*interval_ratio + j];
					}
				}
			}
			if ( !inputts.isDataMissing(oldts_data0[i]) ) {
				oldts_data0[i] /= double_ratio;
			}
		}

		// Now transfer the array back to the requested output time series.
		// If minimum and maximum were specified, apply here.
		
		if ( outputts == null ) {
			// Modify the input time series
			outputts = inputts;
		}
		
		// Override if requested with passed-in value
		// Do a set so that precision is OK
		if ( outputStart == null ) {
			outputStart = new DateTime(date1);
		}
		if ( outputEnd == null ) {
			outputEnd = new DateTime(date2);
		}

		int interval_base = inputts.getDataIntervalBase();
		int interval_mult = inputts.getDataIntervalMult();
		double value;
		// Loop over the output period to do transfer
		// - date1 and date2 contain the input time series
		int i = 0;
		if ( Message.isDebugOn ) {
			Message.printStatus(2,routine,"Transferring output from date1 " + date1 + " to date2 " + date2 + " outputStart=" + outputStart + " outputEnd=" + outputEnd );
		}
		for ( DateTime date = new DateTime(date1); date.lessThanOrEqualTo(date2);
			date.addInterval(interval_base,interval_mult), i++ ) {
			// The offset between the output dates and array position depends on the difference between the
			// input and output start, and also whether there was any additional data provided
			if ( date.lessThan(outputStart) ) {
				continue;
			}
			else if ( date.greaterThan(outputEnd) ) {
				// Done transferring
				break;
			}
			// Translate "i" to align with date1
			value = oldts_data0[i + inputPreviousValues.length];
			outputts.setDataValue ( date, value );
			if ( Message.isDebugOn ) {
				Message.printStatus(2, routine, "Setting " + date + " value=" + value);
			}
		}

		// Return the output time series...

		List<String> genesis = outputts.getGenesis();
		outputts.setDescription ( outputts.getDescription() + ",ARMA" );
		int genesis_length = outputts.getGenesis().size();
		genesis.add(genesis_length, "Applied ARMA(p=" + a.length + ",q=" + (b.length - 1) + ") using ARMA interval " + ARMA_interval + " and coefficients:" );
		for ( i = 0; i < a.length; i++ ) {
			genesis.add((genesis_length + 1 + i), "    a" + (i + 1) + " = "+StringUtil.formatString(a[i], "%.6f") );
		}
		for ( i = 0; i < b.length; i++ ) {
			genesis.add ( (genesis_length + a.length + 1 + i), "    b" + i + " = " + StringUtil.formatString(b[i], "%.6f") );
		}
		genesis.add ((genesis_length + a.length + b.length + 1), "ARMA: The original number of data points were expanded by a factor " );
		genesis.add ((genesis_length + a.length + b.length + 2), "ARMA: of " + interval_ratio + " before applying ARMA." );
		if ( ARMA_ratio == 1 ) {
			genesis.add ((genesis_length + a.length + b.length + 3), "ARMA: All points were then used as the final result." );
		}
		else {	genesis.add ((genesis_length + a.length + b.length + 3),
			"ARMA: 1/" + ARMA_ratio + " points were then used to compute the averaged final result." );
		}
		outputts.setGenesis ( genesis );
		return outputts;
	}

}
