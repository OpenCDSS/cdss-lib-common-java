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
	@param oldts Time series to shift.
	@param a Array of a coefficients.
	@param b Array of b coefficients.
	@param ARMA_interval The interval used to develop ARMA coefficients.  The
	interval must be <= that of the time series.  Use an interval like "1Day", "6Hour", etc.
	@param outputStart output start date/time or null to be same as input time series
	@param outputEnd output end date/time or null to be the same as input time series
	@return new time series.
	@exception Exception if there is a problem with input
	*/
	public TS ARMA ( TS oldts, String ARMA_interval, double a[], double b[],
		double [] inputInitialValues, double outputMinimum, DateTime outputStart, DateTime outputEnd, boolean readData )
	throws Exception
	{	String message, routine = "TSUtil_ARMA";

		// If ts is null, throw exception.
		if ( oldts == null ) {
			message = "Time series is null for ARMA.";
			Message.printWarning ( 2, routine, message );
			throw new TSException( message );
		}

		// Always need b but a is optional.  A single b value of 1 would be a "unity" operation.
		if ( (b == null) || (b.length == 0) ) {
			message = "Array of b-coefficients is zero-length.";
			Message.printWarning ( 2, routine, message );
			throw new TSException( message );
			
		}
		int n_a = a.length;
		int n_b = b.length;

		// Get the ratio of the time series interval to that of the ARMA
		// interval.  Use seconds to do the ratio.  Use TimeInterval as much
		// as possible because it throws exceptions.

		TimeInterval interval_data = new TimeInterval(
			oldts.getDataIntervalBase(), oldts.getDataIntervalMult() );
		TimeInterval interval_ARMA =TimeInterval.parseInterval ( ARMA_interval);
		int interval_ratio = 1;	// Factor to expand data due to ARMA interval being less than data interval.
		int ARMA_ratio = 1;	// Factor to skip expanded data due to ARMA
					// interval being longer than for the expanded data.
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

		DateTime date1 = new DateTime ( oldts.getDate1() );
		DateTime date2 = new DateTime ( oldts.getDate2() );

		// Because there is potential that the ARMA coefficients were calculated
		// at a different time step than the time series that is being analyzed
		// here, convert the time series to an array.

		double [] oldts_data0 = TSUtil.toArray ( oldts, oldts.getDate1(), oldts.getDate2() );

		// Now expand the time series because of the interval_ratio.

		double [] oldts_data = null; // Old data in base interval (interval that divides into data and ARMA interval).
		double [] newts_data = null;	// Routed data in base interval.
		double missing = oldts.getMissing();
		if ( interval_ratio == 1 ) {
			// Just use the original array (no reason to expand)...
			oldts_data = oldts_data0;
			newts_data = new double[oldts_data.length];
			int j = 0;
			for ( int i = 0; i < oldts_data0.length; i++ ) {
				for ( j = 0; j < interval_ratio; j++ ) {
					newts_data[i*interval_ratio + j] = missing;
				}
			}
		}
		else {
		    // Allocate a new data array in the base interval.  Carry
			// forward each original value as necessary to fill out the base interval data...
			oldts_data = new double[oldts_data0.length*interval_ratio];
			newts_data = new double[oldts_data.length];
			int j = 0;
			for ( int i = 0; i < oldts_data0.length; i++ ) {
				for ( j = 0; j < interval_ratio; j++ ) {
					oldts_data[i*interval_ratio + j] = oldts_data0[i];
					newts_data[i*interval_ratio + j] = missing;
				}
			}
		}

		// Now perform the ARMA looping through the base interval data...

		double total = 0.0;
		int jpos = 0;
		int genesis_length = oldts.getGenesis().size();
		double data_value;
		int ia = 0, ib = 0;
		boolean value_set = false;
		for ( int j = 0; j < oldts_data.length; j++ ) {
			total = missing;
			value_set = false;
			// Want the first position to be t - 1 (one ARMA interval) from
			// the current time.  The second point is t - 2 (two ARMA
			// intervals) from the current time.
			for ( ia = 0; ia < n_a; ia++ ) {
				// ARMA_ratio of 1 gives j - ... 1, 2, 3, etc.
				// ARMA_ratio of 2 gives j - ... 2, 4, 6, etc.
				jpos = j - (ia + 1)*ARMA_ratio;
				if ( jpos < 0 ) {
					// Before start of any data...
					newts_data[j] = missing;
					value_set = true;
					break;
				}
				// Get previous outflow value...
				data_value = newts_data[jpos];
				if ( oldts.isDataMissing(data_value) ) {
					// Previous outflow value is missing so try using the input time series value...
					data_value = oldts_data[jpos];
					//Message.printStatus ( 1, routine,
					//"O missing I for " + shifted_date + " is " + data_value );
					if ( oldts.isDataMissing(data_value) ) {
						newts_data[j] = missing;
						//Message.printStatus ( 1, routine, "Setting O at " + date + " to " + data_value );
						//oldts.addToGenesis( "ARMA:  Using missing (" + StringUtil.formatString(
						// data_value,"%.6f") + ") at " + date +
						//" because input and output for a" + (i + 1) + " are missing.");
						value_set = true;
						break;
					}
				}
				// If get to here, have data so can increment another term in the total...
				if ( oldts.isDataMissing(total) ) {
					// Assign the value...
					total = data_value*a[ia];
				}
				else {
				    // Sum the value...
					total += data_value*a[ia];
				}
			}
			if ( value_set ) {
				// Set to missing above so no reason to continue processing the time step...
				continue;
			}
			// Want the values to be for offset of 0, t - 1 (one ARMA
			// interval), t - 2 (two ARMA intervals), etc.
			for ( ib = 0; ib < n_b; ib++ ) {
				// ARMA_ratio of 1 gives j - ... 0, 1, 2, etc.
				// ARMA_ratio of 2 gives j - ... 0, 2, 4, etc.
				jpos = j - ib*ARMA_ratio;
				if ( jpos < 0 ) {
					newts_data[j] = missing;
					//Message.printStatus ( 1, routine, "Setting O at " + date + " to " + data_value );
					//oldts.addToGenesis( "ARMA:  Using missing (" +
					//StringUtil.formatString(data_value,"%.6f") + ") at " + date +
					//" because input and output for a" + (i + 1) + " are missing.");
					value_set = true;
					break;
				}
				data_value = oldts_data[jpos];
				if ( oldts.isDataMissing(data_value) ) {
					newts_data[j] = missing;
					value_set = true;
					break;
				}
				// If get to here have non-missing data...
				if ( oldts.isDataMissing(total) ) {
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
				continue;
			}
			newts_data[j] = total;
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
				if ( oldts.isDataMissing(data_value) ) {
					break;
				}
				else {	// Have non-missing data...
					if ( oldts.isDataMissing(oldts_data0[i]) ) {
						oldts_data0[i] = newts_data[i*interval_ratio + j];
					}
					else {
					    oldts_data0[i] += newts_data[i*interval_ratio + j];
					}
				}
			}
			if ( !oldts.isDataMissing(oldts_data0[i]) ) {
				oldts_data0[i] /= double_ratio;
			}
		}

		// Now transfer the array back to the original time series...

		int i = 0;
		int interval_base = oldts.getDataIntervalBase();
		int interval_mult = oldts.getDataIntervalMult();
		for ( DateTime date = new DateTime(date1); date.lessThanOrEqualTo(date2);
			date.addInterval(interval_base,interval_mult), i++ ) {
			oldts.setDataValue ( date, oldts_data0[i] );
		}

		// Now return the modified original time series...

		List<String> genesis = oldts.getGenesis();
		oldts.setDescription ( oldts.getDescription() + ",ARMA" );
		genesis.add(genesis_length, "Applied ARMA(p=" + n_a + ",q=" + (n_b - 1) +
		 ") using ARMA interval " + ARMA_interval +	" and coefficients:" );
		for ( i = 0; i < n_a; i++ ) {
			genesis.add((genesis_length + 1 + i),
			"    a" + (i + 1) + " = "+StringUtil.formatString(a[i], "%.6f") );
		}
		for ( i = 0; i < n_b; i++ ) {
			genesis.add ( (genesis_length + n_a + 1 + i), "    b" + i + " = " + StringUtil.formatString(b[i], "%.6f") );
		}
		genesis.add ((genesis_length + n_a + n_b + 1),
		"ARMA: The original number of data points were expanded by a factor " );
		genesis.add ((genesis_length + n_a + n_b + 2),
		"ARMA: of " + interval_ratio + " before applying ARMA." );
		if ( ARMA_ratio == 1 ) {
			genesis.add ((genesis_length + n_a + n_b + 3), "ARMA: All points were then used as the final result." );
		}
		else {	genesis.add ((genesis_length + n_a + n_b + 3),
			"ARMA: 1/" + ARMA_ratio + " points were then used to compute the averaged final result." );
		}
		oldts.setGenesis ( genesis );
		return oldts;
	}

}