// ----------------------------------------------------------------------------
// TSRegression - this class is a wrapper for the RTi.Util.Math.Regression
//		class.  It allows analysis methods related to regression to be
//		applied to time series data.
// ----------------------------------------------------------------------------
// History:
//
// 27 May 1998	Catherine E.
//		Nutting-Lane, RTi	Created initial version.
// 04 Nov 1998	Steven A. Malers, RTi	Alphabetize methods.  Add a toString
//					method to get a nice summary of the
//					regression statistics (for log files,
//					etc.).  Enable the regression on
//					construction (CEN wanted to do this
//					before and I have wised up to the idea).
//					Add exceptions to the get routines if
//					the regression was not successful.
// 08 Jan 1999	SAM, RTi		Update to give better messages in
//					toString when there is a data problem.
//					Track the number of points used so we
//					can see the sample size.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 05 Nov 2000	SAM, RTi		Use setMax*(), etc. in the base class to
//					store max/min values that can be used
//					for graphing.  Clean up the
//					documentation some.  Update toString()
//					to have a non-data-filling version.
// 13 Apr 2001	SAM, RTi		Update to format regression results to
//					6-digits of precision (still quite a
//					lot but better than default of many
//					more digits).  Fix bug where min/max
//					values for the second time series were
//					not getting set correctly.
// 26 Apr 2001	SAM, RTi		Change the line separator in toString()
//					to "\n".
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2002-02-25	SAM, RTi		Add support for the MOVE2 algorithm.
//					Rename variables to be more clear what
//					the is independent (X) and what is the
//					dependent (Y) variable.  The previous
//					logic had them reversed, at least in the
//					variable names versus documentation!
//					This did not impact the filling as long
//					as the output period was set but was
//					causing problems for general use.
// 2002-03-22	SAM, RTi		Add support for the MOVE1 algorithm.
//					Add more information to the results.
//					Support monthly and log analysis for
//					MOVE1 and MOVE2.  Add an analysis
//					period.  Generalize the code to not be
//					so focused on regression.  For example,
//					change the RegressMethod property to
//					AnalysisMethod and change the regress()
//					method to analyze().  Change variable
//					names to be generic for the analysis
//					methods (e.g., use N1 for the number of
//					points in the overlapping period).
// 2002-03-31	SAM, RTi		Change in plan... the MOVE2 method needs
//					2 analysis periods, one each for the
//					independent and dependent time series.
//					The periods passed in to the method are
//					done so via properties.
// 2002-04-03	SAM, RTi		Expand the RMSE to show both the
//					transformed and untransformed data
//					coordinates.
// 2002-04-04	SAM, RTi		Complete the new analyzeOLSRegression()
//					method.
// 2002-04-05	SAM, RTi		Add more statistics MeanY and SY and
//					split the output into two tables to
//					make it easier to fit on a page of
//					output.
// 2002-05-20	SAM, RTi		Add AnalysisMonth property to support
//					seasonal comparisons.  Add getPropList()
//					to allow properties to be compared with
//					user-defined properties (e.g, in
//					TSViewPropertiesFrame).  Add
//					getAnalyzeMonth() to return boolean
//					array indicating whether months are
//					analyzed.  Add more error handling to
//					more gracefully indicate when an
//					analysis could not be completed.  The
//					isAnalyzed() method should be called
//					rather than catching an exception on
//					the constructor because a partial
//					monthly analysis may be possible.
// 2003-05-14	SAM, RTi		* Add an Intercept property, to be used
//					  with linear regression.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
// 2003-09-26	SAM, RTi		* Tracking down a problem in the plot -
//					  display more information in toString()
//					  when isAnalyzed() is false. 
// 2005-04-14	SAM, RTi		* Add getPropList() method to get the
//					  properties that were provided at
//					  construction and which control the
//					  logic.
//					* Add getIndependentAnalysisStart(),
//					  getIndependentAnalysisEnd(),
//					  getDependentAnalysisStart(),
//					  getDependnetAnalysisEnd() to
//					  facilitate processing.
// 2005-04-18	SAM, RTi		* Overload the constructor to pass in
//					  the data arrays.  This will allow
//					  chaining of processing so that
//					  conversion from time series to arrays
//					  can be minimized.  Additional
//					  optimization may occur as performance
//					  is evaluated (e.g., if the arrays
//					  stay the same, then there may be no
//					  need to recompute basic statistics).
// 2005-05-03	Luiz Teixeira, RTi	Added the method createPredictedTS().
//					Added the method getPredictedTS().
// 2005-05-04	LT, RTi			Update the method createPredictedTS().
//					to compute the residual.
//					Added the method getResidualTS().
// 2005-05-06	SAM, RTi		Clean up some of the properties to be
//					more consistent with parameters used
//					in TSTool and other software:
//					* FillPeriodStart -> FillStart
//					* FillPeriodEnd -> FillEnd
//					* IndependentAnalysisPeriodStart ->
//					  IndependentAnalysisStart
//					* IndependentAnalysisPeriodEnd ->
//					  IndependentAnalysisEnd
//					* DependentAnalysisPeriodStart ->
//					  DependentAnalysisStart
//					* DependentAnalysisPeriodEnd ->
//					  DependentAnalysisEnd
//					Support the old versions but print a
//					warning level 2.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import RTi.Util.Math.DataTransformationType;
import RTi.Util.Math.MathUtil;
import RTi.Util.Math.NumberOfEquationsType;
import RTi.Util.Math.Regression;
import RTi.Util.Math.RegressionType;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;

/**
<P>
The TSRegression class performs regression, MOVE1, and MOVE2 analysis on time
series.  The RTi.Util.Math.Regression base class stores some data, as
appropriate (e.g., total-period, non-monthly, parameters and results).
The results can then be used for filling (e.g., by the TSUtil.fillRegress() method.
</P>
<P>
TSRegression allows provides a relatively simple interface to programmers,
minimizing the need to understand time series.  However, this can lead to
performance problems if many combinations of time series are analyzed.  To allow
for performance optimization, a constructor is provided that accepts the
data arrays.  The calling code must track when a time series is reused and
should request the arrays from a previous TSRegression in order to pass to a
new TSRegression.  This form of the constructor will likely be used only by advanced tools.
</P>
*/
public class TSRegression extends Regression
{

// Data members...

private TS __yTSpredicted = null;
private TS __yTSresidual  = null;
/**
Analysis method (regression type).
*/
private RegressionType __analysisMethod = RegressionType.OLS_REGRESSION;
/**
Dependent time series, Y.
*/
private	TS _yTS;
/**
Independent, X.
*/
private	TS _xTS;
/**
One equation X array (independent).
*/
private double [] __X;
/**
Monthly equation X array (independent).
*/
private double [][]	__X_monthly;
/**
One equation X1 array (independent).
*/
private double [] __X1;
/**
Monthly equation X1 array (independent).
*/
private double [][] __X1_monthly;
/**
One equation Y1 array (dependent).
*/
private double [] __Y1;
/**
Monthly equation Y1 array.
*/
private double [][]	__Y1_monthly;
// TODO SAM 2005-08-05 The following arrays are envisioned to be used by the
// mixed station analysis to optimize data processing.  Basically the data
// arrays will be extracted up front and re-used.
/**
Indicate whether the dependent data are provided in array format (instead of just the time series format).
*/
private boolean __dependent_arrays_provided = false;
/**
Indicate whether the independent data are provided in array format (instead of just the time series format).
*/
private boolean __independent_arrays_provided = false;
/**
Analysis period start for the dependent (Y) time series.
*/
private DateTime _dep_analysis_period_start;
/**
Analysis period end for the dependent (Y) time series.
*/
private DateTime _dep_analysis_period_end;
/**
Analysis period start for the independent (X) time series.  For OLS and MOVE2,
this is the same as the dependent analysis period.  For MOVE2 it can be different.
*/
private DateTime _ind_analysis_period_start;
/**
Analysis period end for the independent (X) time series.  For OLS and MOVE2,
this is the same as the dependent analysis period.  For MOVE2 it can be different.
*/
private DateTime _ind_analysis_period_end;
/**
Fill period start for the dependent (Y) - used to compute error when _filling is true.
*/
private DateTime _fill_period_start;
/**
Fill period end for the dependent (Y) - used to compute error when _filling is true.
*/
private DateTime _fill_period_end;
/**
_n1 on a monthly basis - the number of non-missing points in X and Y in the analysis period.
*/
private int [] _n1_monthly;
/**
_n2 on a monthly basis - the number of non-missing points in X and Y in the analysis period.
*/
private int [] _n2_monthly;
/**
The intercept to force, or null if not forcing.
*/
private Double __intercept = null;
/**
_a on a monthly basis.
*/
private double [] _a_monthly;
/**
_b on a monthly basis.
*/
private double [] _b_monthly;
/**
RMSE on a monthly basis.
*/
private double [] _rmse_monthly;
/**
Transformed RMS error on a monthly basis.
*/
private double [] _transformed_rmse_monthly;
/**
Indicates whether analysis results are available for monthly analysis.
*/
private boolean [] _is_analyzed_monthly;
/**
_correlationCoeff on a monthly basis
*/
private double [] _r_monthly;
/**
Indicates the data transformation.
*/
private DataTransformationType __transformation = null;
/**
Indicates if the time series are correlated on a monthly basis.
*/
private NumberOfEquationsType __numberOfEquations = null;
/**
Indicates the months to analyze, 12 values for Jan - Dec, were true means the month is included in the analysis.
*/
private boolean [] _analyze_month;
/**
List of month numbers to analyze, where each month is 1-12 (Jan - Dec), or null to analyze all months.
*/
private int [] _analyze_month_list;

private double [] _X_max_monthly;
private double [] _X1_max_monthly;
private double [] _X2_max_monthly;
private double [] _Y1_max_monthly;

private double [] _X_min_monthly;
private double [] _X1_min_monthly;
private double [] _X2_min_monthly;
private double [] _Y1_min_monthly;

private double [] _X_mean_monthly;
private double [] _Y_mean_monthly;
private double [] _X1_mean_monthly;
private double [] _X2_mean_monthly;
private double [] _Y1_mean_monthly;
private double [] _Y1_estimated_mean_monthly;

private double [] _X_stddev_monthly;
private double [] _Y_stddev_monthly;
private double [] _X1_stddev_monthly;
private double [] _X2_stddev_monthly;
private double [] _Y1_stddev_monthly;
private double [] _Y1_estimated_stddev_monthly;

/**
Indicates whether regression is being computed for filling (or just comparison).
*/
private boolean _filling = false;

/**
Default constructor.
@deprecated Use a version that specifies time series.
*/
public TSRegression ()
{
}

/**
Perform a regression using the specified time series.  The results can be used
to fill data using the relationship:
<pre>
     Y = a + bX;
</pre>
@exception Exception if an error occurs performing the analysis.  However, it
is best to check the isAnalyzed() value to determine whether results can be used.
@param independentTS The independent time series (X).
@param dependentTS The dependent time series (Y).
@param props Property list indicating how the regression should be performed.
Possible properties are listed in the table below:
<p>

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr

<tr>
<td><b>AnalysisMethod</b></td>
<td>Set to "OLSRegression" if a ordinary least squares regression, "MOVE1" if
using the MOVE.1 procedure or "MOVE2" if using the MOVE.2 procedure.
<td>"OLSRegression"</td>
</tr>

<tr>
<td><b>AnalysisMonth</b></td>
<td>If one equation is being used, indicate the months that are to be analyzed.
If monthly equations are being used, indicate the one month to analyze.
Specify the months separated by commas or white space.
<td>"" (analyze all months)</td>
</tr>

<td><b>AnalyzeForFilling</b></td>
<td>Set to "true" if analysis is being done as part of a data filling process,
in which case the FillStart and FillEnd can be specified.
The RMSE is computed as the difference between Y and Y-estimated where Y is
known.  The default ("false") is used to compare time series (e.g., for
calibration), in which case the RMSE is computed using the difference between
X and Y.</td>
<td>false</td>
</tr>

<tr>
<td><b>DependentAnalysisEnd</b></td>
<td>Date/time as a string indicating analysis period end for the dependent time
series.  This can be specified for all analysis methods.  For OLS and MOVE1,
this period will also be used for the independent time series.</td>
<td>Full period.</td>
</tr>

<tr>
<td><b>DependentAnalysisPeriodEnd</b></td>
<td>Obsolete, use DependentAnalysisEnd.</td>
<td>Full period.</td>
</tr>

<tr>
<td><b>DependentAnalysisPeriodStart</b></td>
<td>Obsolete, use DependentAnalysisStart.</td>
<td></td>
</tr>

<tr>
<td><b>DependentAnalysisStart</b></td>
<td>Date/time as a string indicating analysis period start for the dependent
time series.  This can be specified for all analysis methods.  For OLS and
MOVE1, this period will also be used for the independent time series.</td>
<td>Full period.</td>
</tr>

<tr>
<td><b>FillEnd</b></td>
<td>Date/time as a string indicating filling period end.  Specify when
AnalyzeForFilling is true.</td>
<td>Full period.</td>
</tr>

<tr>
<td><b>FillPeriodEnd</b></td>
<td>Obsolete, replaced by FillEnd.</td>
<td></td>
</tr>

<tr>
<td><b>FillPeriodStart</b></td>
<td>Obsolete, replaced by FillStart.</td>
<td></td>
</tr>

<tr>
<td><b>FillStart</b></td>
<td>Date/time as a string indicating filling period start.  Specify when
AnalyzeForFilling is true.</td>
<td>Full period.</td>
</tr>

<tr>
<td><b>IndependentAnalysisEnd</b></td>
<td>Date/time as a string indicating analysis period end for the independent
time series.  This can be specified for the MOVE2 analysis method.</td>
<td>Full period.</td>
</tr>

<tr>
<td><b>IndependentAnalysisPeriodEnd</b></td>
<td>Obsolete, use IndependentAnalysisEnd.</td>
<td></td>
</tr>

<tr>
<td><b>IndependentAnalysisPeriodStart</b></td>
<td>Obsolete, use IndependentAnalysisStart.</td>
<td></td>
</tr>

<tr>
<td><b>IndependentAnalysisStart</b></td>
<td>Date/time as a string indicating analysis period start for the independent
time series.  This can be specified for the MOVE2 analysis method.</td>
<td>Full period.</td>
</tr>

<tr>
<td><b>Intercept</b></td>
<td>If specified, indicate the intercept (A-value in best fit equation) that
should be forced when analyzing the data.  This is currently only implemented
for Linear (no) transformation and can currently only have a value of 0.
If specified as blank, no intercept is used.  This feature is typically only
used when analyzing data for filling.
<td>Not used - intercept is calculated.</td>
</tr>

<tr>
<td><b>NumberOfEquations</b></td>
<td>Set to "OneEquation" to calculate one relationship.  Set to
"MonthlyEquations" if a monthly analysis should be done (in which
case 12 relationships will be determined).  In the future support for seasonal equations may be added.</td>
<td>OneEquation</td>
</tr>

<tr>
<td><b>Transformation</b></td>
<td>Set to "Log" if a log10 regression should be done, or "None" if no
transformation (the older "Linear" is also recognized and is equivalent to "None").</td>
<td>false</td>
</tr>
</table>
*/
public TSRegression ( TS independentTS, TS dependentTS,
        boolean analyzeForFilling, RegressionType analysisMethod,
        Double intercept, NumberOfEquationsType numberOfEquations, int [] analysisMonths,
        DataTransformationType transformation,
        DateTime dependentAnalysisStart, DateTime dependentAnalysisEnd,
        DateTime independentAnalysisStart, DateTime independentAnalysisEnd,
        DateTime fillStart, DateTime fillEnd )
throws Exception
{	super ();
	initialize ( independentTS, dependentTS, analyzeForFilling, analysisMethod,
        intercept, numberOfEquations, analysisMonths, transformation,
        dependentAnalysisStart, dependentAnalysisEnd,
        independentAnalysisStart, independentAnalysisEnd,
        fillStart, fillEnd );
	analyze ();
}

/**
Construct a TSRegression object and perform the analysis.  See the overloaded
version for a description of the analysis.
This version should be used when an initial analysis has occurred (e.g., with
a previous TSRegression) and data arrays can be re-used for a subsequent
analysis.  For example, the same dependent and independent time series may be
used, but with different analysis method or transformation.  In this case, there
is no reason to recreate the data arrays from the time series.  An example of use is as follows:
<pre>
// First analysis...
TSRegression reg1 = new TSRegression ( ts_ind, ts_dep, props );
// Change the properties...
props.set ( ... );
// Analyze again, using the same data arrays as in the previous analysis...
TSRegression reg2 = new TSRegression ( ts_ind, ts_dep, props,
reg1.getX(), reg1.getXMonthly(), reg1.getX1(), reg1.getX1Monthly(),
reg1.getY1(), reg1.getY1Monthly() );
</pre>
In this way, the data arrays can be used repeatedly, thus improving performance.
Note that the "1" and "2" arrays are computed based on the overlap between the
independent and dependent time series.  Therefore, changing the combination will
require that the arrays are recreated (use the first version of the constructor).
@exception Exception if an error occurs performing the analysis.  However, it
is best to check the isAnalyzed() value to determine whether results can be used.
@param independentTS The independent time series (X).
@param dependentTS The dependent time series (Y).
@param props Property list indicating how the regression should be performed.
See the overloaded method for possible properties.
*/
public TSRegression ( TS independentTS, TS dependentTS, double[] X, double[][] XMonthly,
    boolean analyzeForFilling, RegressionType analysisMethod,
    Double intercept, NumberOfEquationsType numberOfEquations, int [] analysisMonths,
    DataTransformationType transformation,
    DateTime dependentAnalysisStart, DateTime dependentAnalysisEnd,
    DateTime independentAnalysisStart, DateTime independentAnalysisEnd,
    DateTime fillStart, DateTime fillEnd)
throws Exception
{	super ();
	initialize ( independentTS, dependentTS, analyzeForFilling, analysisMethod,
	        intercept, numberOfEquations, analysisMonths, transformation,
	        dependentAnalysisStart, dependentAnalysisEnd,
	        independentAnalysisStart, independentAnalysisEnd,
	        fillStart, fillEnd );
	analyze ();
}

/**
This routine is the most general analysis routine and should be called in all
cases.  It evaluates the property list set in the constructor and calls the
appropriate secondary routines.  See the constructor documentation for possible property values.
@exception RTi.TS.Exception if there is a problem performing regression.
*/
private void analyze ()
{	if ( __analysisMethod == RegressionType.MOVE2 ) {
		analyzeMOVE2();
	}
	else if ( __analysisMethod == RegressionType.OLS_REGRESSION ) {
	    analyzeOLSRegression();
	}
}

/**
Analyze two time series using the MOVE2 method.
The data in the dependent time series can then be filled using
<pre>
Y = a + bX
</pre>
*/
private void analyzeMOVE2 ()
{	String routine = "TSRegression.analyzeMOVE2";

	int num_equations = 1;
	if ( __numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS ) {
		num_equations = 12;
	}

	// Loop through the equations.

	double [] x1Array = null;
	double [] y1Array = null;
	double [] xArray = null;
	int n1 = 0;
	int n2 = 0;
	int ind_interval_base = _xTS.getDataIntervalBase();
	int ind_interval_mult = _xTS.getDataIntervalMult();
	DateTime date = null;	// For iteration
	double data_value = 0.0;
	double rmse_total = 0.0, transformed_rmse_total = 0.0;
	double n1_total = 0;
	for ( int ieq = 1; ieq <= num_equations; ieq++ ) {
		try {
		// Get the data array for the dependent analysis period (N1).
		if ( (num_equations == 1) && ((_analyze_month_list == null) || (_analyze_month_list.length == 0)) ) {
			// Input data is for all the months to be analyzed...
			x1Array = TSUtil.toArray ( _xTS, _dep_analysis_period_start, _dep_analysis_period_end );
			y1Array = TSUtil.toArray ( _yTS, _dep_analysis_period_start, _dep_analysis_period_end );
			xArray = TSUtil.toArray ( _xTS, _ind_analysis_period_start, _ind_analysis_period_end );
		}
		else if ((num_equations == 1) && (_analyze_month_list != null)){
			// Only get the months to analyze...
			x1Array = TSUtil.toArray ( _xTS, _ind_analysis_period_start, _ind_analysis_period_end,
				_analyze_month_list );
			y1Array = TSUtil.toArray ( _yTS, _dep_analysis_period_start, _dep_analysis_period_end,
				_analyze_month_list );
			xArray = TSUtil.toArray ( _xTS, _xTS.getDate1(), _xTS.getDate2(), _analyze_month_list );
		}
		else {
		    if ( !_analyze_month[ieq - 1] ) {
				continue;
			}
			// Get the input data by month...
			x1Array = TSUtil.toArrayByMonth ( _xTS, _dep_analysis_period_start, _dep_analysis_period_end, ieq );
			y1Array = TSUtil.toArrayByMonth ( _yTS, _dep_analysis_period_start, _dep_analysis_period_end, ieq );
			xArray = TSUtil.toArrayByMonth ( _xTS, _ind_analysis_period_start, _ind_analysis_period_end, ieq );
		}

		// Initially indicate that the analysis is not complete...

		if ( num_equations == 1 ) {
			isAnalyzed ( false );
		}
		else {
		    isAnalyzed ( ieq, false );
		}

		if ( (x1Array == null) || (y1Array == null) ) {
			// Not enough data...
			if ( num_equations == 1 ) {
				throw new TSException ( "No data.  Not performing analysis." );
			}
			else {
			    Message.printWarning ( 2, routine, "Not enough data.  Not performing analysis for month " + ieq + "." );
				continue;
			}
		}

		// The array lengths should be the same, even if padded with missing data...

		if ( (x1Array.length != y1Array.length) ) {
			if ( num_equations == 1 ) {
				throw new TSException ( "Data set lengths are not equal." );
			}
			else {
			    Message.printWarning ( 2, routine, "Data set lengths are not equal.  Not performing analysis for month " + ieq );
				continue;
			}
		}

		// First get the data arrays for the N1 space, which is the
		// overlapping data in the dependent analysis period...

		// First get the counts where X and Y are non-missing...

		n1 = 0;
		for ( int i = 0; i < x1Array.length; i++ ) {
			if ( !_xTS.isDataMissing(x1Array[i]) && !_yTS.isDataMissing(y1Array[i]) ) {
				// both are not missing...
				++n1;
			}
		}

		if ( n1 == 0 ) {
			if ( num_equations == 1 ) {
				String message = "The number of overlapping points is 0.  Cannot perform MOVE2 analysis.";
				Message.printWarning ( 2, routine, message );
				throw new Exception ( message );
			}
			else {
			    String message = "The number of overlapping points is 0 for month " + ieq +
				".  Cannot perform MOVE2 analysis.";
				Message.printWarning ( 2, routine, message );
				continue;
			}
		}

		// Set the number of overlapping values because this is used
		// in other code (e.g., plotting) to allow a single point plot...

		if ( num_equations == 1 ) {
			setN1 ( n1 );
		}
		else {
		    setN1 ( ieq, n1 );
		}

		// Now transfer the values to temporary arrays for the
		// independent variable for N1 and N2 values.  If doing a log
		// transformation, do it here and then treat all other
		// operations as if no transformation...

		double [] X1_data = new double[n1]; // non-missing X data where non-missing Y for n1
		double [] Y1_data = new double[n1]; // non-missing Y data where non-missing X for n1
		double [] orig_X1_data = null;
		double [] orig_Y1_data = null;
		if ( __transformation == DataTransformationType.LOG ) {
			orig_X1_data = new double[n1];
			orig_Y1_data = new double[n1];
		}

		n1 = 0;	// Can reuse
		for ( int i = 0; i < x1Array.length; i++ ) {
			if ( !_xTS.isDataMissing(x1Array[i]) && !_yTS.isDataMissing(y1Array[i]) ) {
				// both are not missing...
				if ( __transformation == DataTransformationType.LOG ) {
					if ( x1Array[i] <= 0.0 ) {
						// Assume X = .001
						X1_data[n1] = -3.0;
					}
					else {
					    X1_data[n1] = MathUtil.log10(x1Array[i] );
					}
					if ( y1Array[i] <= 0.0 ) {
						// Assume Y = .001
						Y1_data[n1] = -3.0;
					}
					else {
					    Y1_data[n1] = MathUtil.log10(y1Array[i] );
					}
					orig_X1_data[n1] = x1Array[i];
					orig_Y1_data[n1] = y1Array[i];
				}
				else {
				    X1_data[n1] = x1Array[i];
					Y1_data[n1] = y1Array[i];
				}
				++n1;
			}
		}

		// Now evaluate the independent time series analysis period...

		// First loop through and determine in this period where there
		// is a non-missing X value and a missing Y value.  The period
		// outside the dependent analysis period is considered to have missing Y data.

		// Because N2 values are not used to evaluate RMSE, only need
		// one set of data (the original or transformed)...

		n2 = 0;
		for ( date = new DateTime(_ind_analysis_period_start);
			date.lessThanOrEqualTo(_ind_analysis_period_end);
			date.addInterval(ind_interval_base, ind_interval_mult)){
			if ( (num_equations != 1) && (date.getMonth() != ieq) ) {
				continue;
			}
			if ( !_xTS.isDataMissing(_xTS.getDataValue(date)) ) {
				if ( date.lessThan(_dep_analysis_period_start) ||
					date.greaterThan(_dep_analysis_period_end) ||
					_yTS.isDataMissing(_yTS.getDataValue(date)) ) {
					// OK to increment...
					++n2;
				}
			}
		}

		if ( n2 == 0 ) {
			if ( num_equations == 1 ) {
				String message = "The number of non-overlapping points is 0.  Cannot perform MOVE2.";
				Message.printWarning ( 2, routine, message );
				throw new Exception ( message );
			}
			else {
			    String message = "The number of non-overlapping points for month " + ieq +" is 0.  Cannot perform MOVE2.";
				Message.printWarning ( 2, routine, message );
				throw new Exception ( message );
			}
		}

		double [] X2_data = new double[n2]; // non-missing X data in n2

		// Now loop through again and transfer the data...

		n2 = 0;
		for ( date = new DateTime(_ind_analysis_period_start);
			date.lessThanOrEqualTo(_ind_analysis_period_end);
			date.addInterval(ind_interval_base,ind_interval_mult)) {
			if ( (num_equations != 1) && (date.getMonth() != ieq) ) {
				continue;
			}
			data_value = _xTS.getDataValue(date);
			if ( !_xTS.isDataMissing(data_value) ) {
				if ( date.lessThan(_dep_analysis_period_start) ||
					date.greaterThan(_dep_analysis_period_end) ||
					_yTS.isDataMissing(_yTS.getDataValue(date)) ) {
					if ( __transformation == DataTransformationType.LOG ) {
						if ( data_value <= 0.0 ) {
							// Assume X = .001
							X2_data[n2] = -3.0;
						}
						else {
						    X2_data[n2] = MathUtil.log10( data_value );
						}
					}
					else {
					    X2_data[n2] = data_value;
					}
					++n2;
				}
			}
		}

		// Compute the mean values...

		double X1_mean = MathUtil.mean ( X1_data );
		double Y1_mean = MathUtil.mean ( Y1_data );
		double X2_mean = MathUtil.mean ( X2_data );

		// Compute the standard deviations...

		double X1_stddev = MathUtil.standardDeviation ( X1_data );
		double X2_stddev = MathUtil.standardDeviation ( X2_data );
		double Y1_stddev = MathUtil.standardDeviation ( Y1_data );

		// Do the regression to get the correlation coefficient.
		// Missing data should not have come through so don't check again...

		Regression rd = null;
		try {
		    rd = MathUtil.regress ( X1_data, Y1_data, false, _xTS.getMissing(), _yTS.getMissing(), null );
		}
		catch ( Exception e ) {
			if ( num_equations == 1 ) {
				throw new TSException ( "Error performing regression on N1." );
			}
			else {
			    Message.printWarning ( 2, routine, "Error performing regression on N1 for month " + ieq );
			}
		}
		if ( rd == null ) {
			if ( num_equations == 1 ) {
				throw new TSException ( "Error performing regression on N1." );
			}
			else {
			    Message.printWarning ( 2, routine, "Error performing regression on N1 for month " + ieq );
			}
		}

		double r = rd.getCorrelationCoefficient();
		double b = r*Y1_stddev/X1_stddev;

		double Sy_sq = (1.0/((double)n1 + (double)n2 - 1.0))*
		(
		((double)n1 - 1.0)*Y1_stddev*Y1_stddev +
		((double)n2 - 1.0)*b*b*X2_stddev*X2_stddev +
		(double)n2*
			((double)n1 - 4)*((double)n1 - 1)*(1 - r*r)*
			Y1_stddev*Y1_stddev/
			(((double)n1 - 3)*((double)n1 - 2)) +
		(double)n1*(double)n2/
			((double)n1 + (double)n2)*b*b*
			(X2_mean - X1_mean)*(X2_mean - X1_mean)
		);

		double Ybar = Y1_mean + (double)n2*b* (X2_mean - X1_mean)/((double)n1 + (double)n2);

		if ( __transformation == DataTransformationType.LOG ) {
			// Update the X array to logs...
			for ( int j = 0; j < xArray.length; j++ ) {
				if ( _xTS.isDataMissing(xArray[j]) ) {
					continue;
				}
				if ( xArray[j] <= 0.0 ) {
					// Assume X = .001
					xArray[j] = -3.0;
				}
				else {
				    xArray[j] = MathUtil.log10( xArray[j] );
				}
			}
		}
		double X_mean = MathUtil.mean ( xArray.length, xArray, _xTS.getMissing() );
		double X_stddev = MathUtil.standardDeviation ( xArray.length, xArray, _xTS.getMissing() );

		b = Math.sqrt(Sy_sq)/X_stddev;
		double a = Ybar - b*X_mean;

		double rmse = 0.0, transformed_rmse = 0.0;
		double [] Y1_estimated = null;	// Estimated Y1 if filling data.
		if ( _filling ) {
			// Now if filling, estimate Y1 using A and B and compute
			// the RMSE from Y1 - Y.  Just loop through the X1
			// because we know these points originally lined up with Y1...

			Y1_estimated = new double[n1];
			double ytemp1, ytemp2;
			for ( int i = 0; i < n1; i++ ) {
				if ( __transformation == DataTransformationType.LOG ) {
					// Estimate Y in log10 space.  X1 was transformed to log above...
					Y1_estimated[i] = a + X1_data[i]*b;					
					transformed_rmse += ((Y1_estimated[i] - Y1_data[i])*(Y1_estimated[i] - Y1_data[i]));						
					transformed_rmse_total += ((Y1_estimated[i] - Y1_data[i])*(Y1_estimated[i] - Y1_data[i]));
					// Always do untransformed data.  To do so, un-transform the estimated
					// log10 Y value and compare to the original untransformed Y value...
					ytemp1=Math.pow(10.0, Y1_estimated[i]);					
					ytemp2=orig_Y1_data[i];
					rmse +=((ytemp1 - ytemp2)*(ytemp1 - ytemp2));
					rmse_total +=((ytemp1 - ytemp2)*(ytemp1 - ytemp2));			
				}
				else {
				    Y1_estimated[i] = a + X1_data[i]*b;
					rmse += ((Y1_estimated[i] - Y1_data[i])*(Y1_estimated[i] - Y1_data[i]));
						
					rmse_total += ((Y1_estimated[i] - Y1_data[i])*(Y1_estimated[i] - Y1_data[i]));		
				}
			}
		}
		else {
		    // Just use available data...
			double ytemp, xtemp;
			for ( int i = 0; i < n1; i++ ) {
				if ( __transformation == DataTransformationType.LOG ) {
					transformed_rmse += ((Y1_data[i] - X1_data[i])*(Y1_data[i] - X1_data[i]));
					transformed_rmse_total += ((Y1_data[i] - X1_data[i])*(Y1_data[i] - X1_data[i]));
					// Always do untransformed data...
					ytemp = Math.pow(10.0, Y1_data[i]);
					xtemp = Math.pow(10.0, X1_data[i]);
					rmse += ((ytemp - xtemp)*(ytemp - xtemp));
					rmse_total += ((ytemp - xtemp)*(ytemp - xtemp));
				}
				else {
				    rmse += ((Y1_data[i] - X1_data[i])*(Y1_data[i] - X1_data[i]));
					rmse_total += ((Y1_data[i] - X1_data[i])*(Y1_data[i] - X1_data[i]));
				}
			}
		}
		if ( __transformation == DataTransformationType.LOG ) {
			transformed_rmse=Math.sqrt(transformed_rmse/(double)n1);
		}
		// Always do untransformed data...
		rmse = Math.sqrt ( rmse/(double)n1 );
		n1_total += n1;	// Need below to compute total RMSE

		// Transfer results from local object to the base class...

		if ( num_equations == 1 ) {
			isAnalyzed ( true );
			setA ( a );
			setB ( b );
			setCorrelationCoefficient ( r );
			setN1 ( n1 );
			setN2 ( n2 );
			if ( __transformation == DataTransformationType.LOG  ) {
				setTransformedRMSE ( transformed_rmse );
			}
			setRMSE ( rmse );
			setMaxX1 ( rd.getMaxX1() );
			setMinX1 ( rd.getMinX1() );
			setMaxY1 ( rd.getMaxY1() );
			setMinY1 ( rd.getMinY1() );

			setMeanX ( X_mean );
			setStandardDeviationX ( X_stddev );
			setMeanY ( Ybar );
			setStandardDeviationY ( Math.sqrt(Sy_sq) );
			setMeanX1 ( X1_mean );
			setStandardDeviationX1 ( X1_stddev );
			setMeanX2 ( X2_mean );
			setStandardDeviationX2 ( X2_stddev );
			setMeanY1 ( Y1_mean );
			setStandardDeviationY1 ( Y1_stddev );
			if ( _filling ) {
				setMeanY1Estimated ( MathUtil.mean(Y1_estimated) );
				setStandardDeviationY1Estimated ( MathUtil.standardDeviation(Y1_estimated) );
			}

			setLagIntervals ( rd.getLagIntervals() );
		}
		else {
		    isAnalyzed ( ieq, true );
			setA ( ieq, a );
			setB ( ieq, b );
			setCorrelationCoefficient ( ieq, r );
			setN1 ( ieq, n1 );
			setN2 ( ieq, n2 );
			if ( __transformation == DataTransformationType.LOG  ) {
				setTransformedRMSE ( ieq, transformed_rmse );
			}
			setRMSE ( ieq, rmse );
			if ( ieq == 12 ) {
				// Save the total in the non-monthly data values...
				rmse_total = Math.sqrt ( rmse_total/(double)n1_total );
				transformed_rmse_total = Math.sqrt ( transformed_rmse_total/(double)n1_total);
				setRMSE( rmse_total );
				if ( __transformation == DataTransformationType.LOG  ) {
					setTransformedRMSE ( transformed_rmse_total );
				}
			}
			setMaxX1 ( ieq, rd.getMaxX1() );
			setMinX1 ( ieq, rd.getMinX1() );
			setMaxY1 ( ieq, rd.getMaxY1() );
			setMinY1 ( ieq, rd.getMinY1() );

			setMeanX ( ieq, X_mean );
			setStandardDeviationX ( ieq, X_stddev );
			setMeanY ( ieq, Ybar );
			setStandardDeviationY ( ieq, Math.sqrt(Sy_sq) );
			setMeanX1 ( ieq, X1_mean );
			setStandardDeviationX1 ( ieq, X1_stddev );
			setMeanX2 ( ieq, X2_mean );
			setStandardDeviationX2 ( ieq, X2_stddev );
			setMeanY1 ( ieq, Y1_mean );
			setStandardDeviationY1 ( ieq, Y1_stddev );
			if ( _filling ) {
				setMeanY1Estimated ( ieq, MathUtil.mean(Y1_estimated) );
				setStandardDeviationY1Estimated ( ieq, MathUtil.standardDeviation(Y1_estimated) );
			}
		}

		//Message.printStatus ( 1, "", "sy = " + Math.sqrt(_MOVE2_Sy2));

		X1_data = null;
		X2_data = null;
		Y1_data = null;
		Y1_estimated = null;
		rd = null;
	}
	catch ( Exception e ) {
		// Error doing the analysis.
		if ( num_equations == 1 ) {
			isAnalyzed ( false );
		}
		else {
		    isAnalyzed ( ieq, false );
		}
	}
	}

	x1Array = null;
	y1Array = null;
	xArray = null;
}

/**
Perform OLS regression analysis using either a single or 12 monthly
relationships.  Data are also optionally transformed using Log10.
*/
private void analyzeOLSRegression ()
{	String	routine = "TSRegression.analyzeOLSRegression";
	
	Regression rd = null;
	int num_equations = 1;
	if ( __numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS ) {
		num_equations = 12;
	}
	double [] x1Array = null;
	double [] xArray = null;
	double [] y1Array = null;
	int n1 = 0;
	int n2 = 0;
	int ind_interval_base = _xTS.getDataIntervalBase();
	int ind_interval_mult = _xTS.getDataIntervalMult();
	DateTime date = null;
	double data_value = 0.0;
	for ( int ieq = 1; ieq <= 12; ieq++ ) {
		try {
		// Get the data array for the dependent analysis period (N1).
		// For OLS regression the independent and dependent analysis period are the same.
		if ( (num_equations == 1) && ((_analyze_month_list == null) || (_analyze_month_list.length == 0)) ) {
			// Get all of the data because one equation is used and it is not a specific month.
			if ( __independent_arrays_provided ) {
				x1Array = __X1;
				xArray = __X;
			}
			else {
			    x1Array = TSUtil.toArray ( _xTS, _ind_analysis_period_start, _ind_analysis_period_end );
				xArray = TSUtil.toArray ( _xTS, _xTS.getDate1(), _xTS.getDate2() );
			}
			if ( __dependent_arrays_provided ) {
				y1Array = __Y1;
			}
			else {
			    y1Array = TSUtil.toArray ( _yTS, _dep_analysis_period_start, _dep_analysis_period_end );
			}
		}
		else if ((num_equations == 1) && (_analyze_month_list != null)){
			// This analyzes a "season" of data consisting of one or more months.
			if ( __independent_arrays_provided ) {
				x1Array = __X1;
				xArray = __X;
			}
			else {
			    x1Array = TSUtil.toArray ( _xTS, _ind_analysis_period_start, _ind_analysis_period_end, _analyze_month_list );
				xArray = TSUtil.toArray ( _xTS, _xTS.getDate1(), _xTS.getDate2(), _analyze_month_list );
			}
			if ( __independent_arrays_provided ) {
				y1Array = __Y1;
			}
			else {
			    y1Array = TSUtil.toArray ( _yTS, _dep_analysis_period_start, _dep_analysis_period_end, _analyze_month_list );
			}
		}
		else {
		    // Analyze data for the month indicated by the loop index...
			if ( !_analyze_month[ieq - 1] ) {
				continue;
			}
			if ( __independent_arrays_provided ) {
				x1Array = __X1_monthly[ieq - 1];
				xArray = __X_monthly[ieq - 1];
			}
			else {
			    x1Array = TSUtil.toArrayByMonth ( _xTS, _ind_analysis_period_start, _ind_analysis_period_end, ieq );
				xArray = TSUtil.toArrayByMonth ( _xTS, _xTS.getDate1(), _xTS.getDate2(), ieq );
			}
			if ( __independent_arrays_provided ) {
				y1Array = __Y1_monthly[ieq - 1];
			}
			else {
			    y1Array = TSUtil.toArrayByMonth ( _yTS, _dep_analysis_period_start, _dep_analysis_period_end, ieq);
			}
		}

		// Initially indicate that the analysis is not complete...

		if ( num_equations == 1 ) {
			isAnalyzed ( false );
		}
		else {
		    isAnalyzed ( ieq, false );
		}

		if ( (x1Array == null) || (y1Array == null) ) {
			// Not enough data...
			if ( num_equations == 1 ) {
				Message.printWarning ( 10, routine, "No data.  Not performing analysis." );
				throw new TSException ( "No data.  Not performing analysis." );
			}
			else {
			    Message.printWarning ( 10, routine, "No data.  Not performing analysis for month "+ieq+".");
			}
			continue;
		}

		// The array lengths should be the same, even if padded with missing data...

		if ( x1Array.length != y1Array.length ) {
			// Not enough data...
			if ( num_equations == 1 ) {
				Message.printWarning ( 10, routine, "Data set lengths are not equal." );
				throw new TSException ( "Data set lengths are not equal." );
			}
			else {
			    Message.printWarning ( 10, routine, "Data set lengths are not the same.  " +
				"Not performing analysis for month "+ieq+".");
				continue;
			}
		}

		// First get the data arrays for the N1 space, which is the
		// overlapping data in the dependent analysis period...

		// First get the counts where X and Y are non-missing...

		n1 = 0;
		for ( int i = 0; i < x1Array.length; i++ ) {
			if ( !_xTS.isDataMissing(x1Array[i]) && !_yTS.isDataMissing(y1Array[i]) ) {
				// both are not missing...
				++n1;
			}
		}

		if ( n1 == 0 ) {
			if ( num_equations == 1 ) {
				String message = "The number of overlapping points is 0.  Cannot perform OLS regression.";
				Message.printWarning ( 2, routine, message );
				throw new TSException ( message );
			}
			else {
			    String message = "The number of overlapping points is 0 for month " + ieq +
				".  Cannot perform OLS regression.";
				Message.printWarning ( 2, routine, message );
				continue;
			}
		}

		// Set the number of overlapping values because this is used
		// in other code (e.g., plotting) to allow a single point plot...

		if ( num_equations == 1 ) {
			setN1 ( n1 );
		}
		else {
		    setN1 ( ieq, n1 );
		}

		// Now transfer the values to temporary arrays for the
		// independent variable for N1 and N2 values.  If doing a log
		// transformation, do it here and then treat all other operations as if no transformation...

		double [] X1_data = new double[n1]; // non-missing X data where non-missing Y for n1
		double [] Y1_data = new double[n1]; // non-missing Y data where non-missing X for n1
		double [] orig_X1_data = null;
		double [] orig_Y1_data = null;
		if ( __transformation == DataTransformationType.LOG ) {
			orig_X1_data = new double[n1];
			orig_Y1_data = new double[n1];
		}

		n1 = 0;	// Can reuse
		for ( int i = 0; i < x1Array.length; i++ ) {
			if ( !_xTS.isDataMissing(x1Array[i]) && !_yTS.isDataMissing(y1Array[i]) ) {
				// both are not missing...
				if ( __transformation == DataTransformationType.LOG ) {
					if ( x1Array[i] <= 0.0 ) {
						// Assume X = .001
						X1_data[n1] = -3.0;
					}
					else {
					    X1_data[n1] = MathUtil.log10(x1Array[i] );
					}
					if ( y1Array[i] <= 0.0 ) {
						// Assume Y = .001
						Y1_data[n1] = -3.0;
					}
					else {
					    Y1_data[n1] = MathUtil.log10(y1Array[i] );
					}
					orig_X1_data[n1] = x1Array[i];
					orig_Y1_data[n1] = y1Array[i];
				}
				else {
				    X1_data[n1] = x1Array[i];
					Y1_data[n1] = y1Array[i];
				}
				++n1;
			}
		}

		// Now evaluate the independent time series analysis period...

		// First loop through and determine in this period where there
		// is a non-missing X value and a missing Y value.  The period
		// outside the dependent analysis period is considered to have missing Y data.

		// Because N2 values are not used to evaluate RMSE, only need
		// one set of data (the original or transformed)...

		n2 = 0;
		for ( date = new DateTime(_ind_analysis_period_start);
			date.lessThanOrEqualTo(_ind_analysis_period_end);
			date.addInterval(ind_interval_base,ind_interval_mult)) {
			if ( (num_equations != 1) && (date.getMonth() != ieq) ) {
				continue;
			}
			if ( !_xTS.isDataMissing(_xTS.getDataValue(date)) ) {
				if ( date.lessThan( _dep_analysis_period_start) || date.greaterThan( _dep_analysis_period_end) ||
					_yTS.isDataMissing(_yTS.getDataValue(date)) ) {
					// OK to increment...
					++n2;
				}
			}
		}

		// N2 of 0 is not fatal.

		double [] X2_data = null;
		if ( n2 > 0 ) {
			X2_data = new double[n2]; // non-missing X data in n2

			// Now loop through again and transfer the data...

			n2 = 0;
			for (	date = new DateTime(_ind_analysis_period_start);
				date.lessThanOrEqualTo(_ind_analysis_period_end);
				date.addInterval(ind_interval_base,
				ind_interval_mult)) {
				if ( (num_equations != 1) && (date.getMonth() != ieq) ) {
					continue;
				}
				data_value = _xTS.getDataValue(date);
				if ( !_xTS.isDataMissing(data_value) ) {
					if ( date.lessThan(_dep_analysis_period_start) ||
						date.greaterThan(_dep_analysis_period_end) || _yTS.isDataMissing(_yTS.getDataValue(date)) ) {
						if ( __transformation == DataTransformationType.LOG ) {
							if ( data_value <= 0.0){
								// Assume X = .001
								X2_data[n2] = -3.0;
							}
							else {
							    X2_data[n2] = MathUtil.log10(data_value );
							}
						}
						else {
						    X2_data[n2] = data_value;
						}
						++n2;
					}
				}
			}
		}

		// Compute the mean values...

		double X1_mean = MathUtil.mean ( X1_data );
		double Y1_mean = MathUtil.mean ( Y1_data );
		double X2_mean = 0.0;
		if ( n2 > 0 ) {
			X2_mean = MathUtil.mean ( X2_data );
		}

		// Compute the standard deviations...

		double X1_stddev = MathUtil.standardDeviation ( X1_data );
		double Y1_stddev = MathUtil.standardDeviation ( Y1_data );
		double X2_stddev = 0.0;
		if ( n2 > 0 ) {
			try {
			    X2_stddev = MathUtil.standardDeviation(X2_data);
			}
			catch ( Exception e ) {
				// Not used anywhere so set to zero.
				X2_stddev = 0.0;
			}
		}

		// Data are already transformed so just use the normal log.
		// There is no reason to consider missing data because missing values were removed above...

		try {
		    rd = MathUtil.regress ( X1_data, Y1_data, false, _xTS.getMissing(), _yTS.getMissing(), __intercept );
		}
		catch ( Exception e ) {
			if ( num_equations == 1 ) {
				Message.printWarning ( 10, routine, "Error performing analysis.");
				isAnalyzed ( false );
				Message.printWarning ( 10, routine, e );
				throw new TSException ( "Error performing analysis" );
			}
			else {
			    // Non-fatal (just set the analysis flag to false...
				Message.printWarning ( 10, routine, "Error performing analysis for month "+ieq+".");
				isAnalyzed ( ieq, false );
				continue;
			}
		}
		if ( rd == null ) {
			if ( num_equations == 1 ) {
				Message.printWarning ( 10, routine, "Error performing analysis.");
				isAnalyzed ( false );
				throw new TSException ( "Error performing analysis" );
			}
			else {
			    // Non-fatal (just set the analysis flag to false...
				Message.printWarning ( 10, routine, "Error performing analysis for month "+ieq+".");
				isAnalyzed ( ieq, false );
				continue;
			}
		}

		if ( __transformation == DataTransformationType.LOG ) {
			// Update the X array to logs...
			for ( int j = 0; j < xArray.length; j++ ) {
				if ( _xTS.isDataMissing(xArray[j]) ) {
					continue;
				}
				if ( xArray[j] <= 0.0 ) {
					// Assume X = .001
					xArray[j] = -3.0;
				}
				else {
				    xArray[j] = MathUtil.log10( xArray[j] );
				}
			}
		}
		double X_mean = MathUtil.mean ( xArray.length, xArray, _xTS.getMissing() );
		double X_stddev = MathUtil.standardDeviation ( xArray.length, xArray, _xTS.getMissing() );

		double a = rd.getA();
		double b = rd.getB();
		double rmse = 0.0, transformed_rmse = 0.0;
		double [] Y1_estimated = null;	// Estimated Y1 if filling data.
		if ( _filling ) {
			// Now if filling, estimate Y1 using A and B and compute
			// the RMSE from Y1 - Y.  Just loop through the X1
			// because we know these points originally lined up with Y1...

			Y1_estimated = new double[n1];
			double ytemp1, ytemp2;
			for ( int i = 0; i < n1; i++ ) {
				if ( __transformation == DataTransformationType.LOG ) {
					Y1_estimated[i] = a + X1_data[i]*b;
					transformed_rmse += ((Y1_estimated[i] - Y1_data[i])*(Y1_estimated[i] - Y1_data[i]));
					// Always do untransformed data...
					ytemp1=Math.pow(10.0, Y1_estimated[i]);
					ytemp2=orig_Y1_data[i];
					rmse +=((ytemp1 - ytemp2)*(ytemp1 - ytemp2));
				}
				else {
				    Y1_estimated[i] = a + X1_data[i]*b;
					rmse += ((Y1_estimated[i] - Y1_data[i])*(Y1_estimated[i] - Y1_data[i]));
				}
			}
		}
		else {
		    // Just use available data...
			double ytemp, xtemp;
			for ( int i = 0; i < n1; i++ ) {
				if ( __transformation == DataTransformationType.LOG ) {
					transformed_rmse += ((Y1_data[i] - X1_data[i])*(Y1_data[i] - X1_data[i]));
					// Always do untransformed data...
					ytemp = Math.pow(10.0, Y1_data[i]);
					xtemp = Math.pow(10.0, X1_data[i]);
					rmse += ((ytemp - xtemp)*(ytemp - xtemp));
				}
				else {
				    rmse += ((Y1_data[i] - X1_data[i])*(Y1_data[i] - X1_data[i]));
				}
			}
		}
		if ( __transformation == DataTransformationType.LOG ) {
			transformed_rmse=Math.sqrt(transformed_rmse/(double)n1);
		}
		// Always do untransformed data...
		rmse = Math.sqrt ( rmse/(double)n1 );

		// Save the results in this instance...

		if ( num_equations == 1 ) {
			isAnalyzed ( true );
			setA ( rd.getA() );
			setB ( rd.getB() );
			setCorrelationCoefficient ( rd.getCorrelationCoefficient() );
			setN1 ( rd.getN1() );
			setN2 ( n2 );
			if ( __transformation == DataTransformationType.LOG ) {
				setTransformedRMSE ( transformed_rmse );
			}
			setRMSE ( rmse );
			setMaxX1 ( rd.getMaxX1() );
			setMinX1 ( rd.getMinX1() );
			setMaxY1 ( rd.getMaxY1() );
			setMinY1 ( rd.getMinY1() );

			setMeanX ( X_mean );
			setStandardDeviationX ( X_stddev );
			setMeanX1 ( X1_mean );
			setStandardDeviationX1 ( X1_stddev );
			setMeanX2 ( X2_mean );
			setStandardDeviationX2 ( X2_stddev );
			setMeanY1 ( Y1_mean );
			setStandardDeviationY1 ( Y1_stddev );
			if ( _filling ) {
				setMeanY1Estimated(MathUtil.mean(Y1_estimated));
				setStandardDeviationY1Estimated (MathUtil.standardDeviation(Y1_estimated) );
			}
			setLagIntervals ( rd.getLagIntervals() );
		}
		else {
		    isAnalyzed ( ieq, true );
			setA ( ieq, rd.getA() );
			setB ( ieq, rd.getB() );
			setCorrelationCoefficient ( ieq, rd.getCorrelationCoefficient() );
			setN1 ( ieq, n1 );
			setN2 ( ieq, n2 );
			// There is no N2...
			if ( __transformation == DataTransformationType.LOG ) {
				setTransformedRMSE ( ieq, transformed_rmse );
			}
			setRMSE ( ieq, rmse );
			setMaxX1 ( ieq, rd.getMaxX1() );
			setMinX1 ( ieq, rd.getMinX1() );
			setMaxY1 ( ieq, rd.getMaxY1() );
			setMinY1 ( ieq, rd.getMinY1() );

			setMeanX ( ieq, X_mean );
			setStandardDeviationX ( ieq, X_stddev );
			setMeanX1 ( ieq, X1_mean );
			setStandardDeviationX1 ( ieq, X1_stddev );
			setMeanX2 ( ieq, X2_mean );
			setStandardDeviationX2 ( ieq, X2_stddev );
			setMeanY1 ( ieq, Y1_mean );
			setStandardDeviationY1 ( ieq, Y1_stddev );
			if ( _filling ) {
				setMeanY1Estimated ( ieq, MathUtil.mean(Y1_estimated) );
				setStandardDeviationY1Estimated ( ieq, MathUtil.standardDeviation(Y1_estimated) );
			}
			setLagIntervals ( rd.getLagIntervals() );
		}
	}
	catch ( Exception e ) {
		// Error doing the analysis.
		if ( num_equations == 1 ) {
			isAnalyzed ( false );
		}
		else {
		    isAnalyzed ( ieq, false );
		}
	}
	}
	rd = null;
	routine = null;
	x1Array = null;
	y1Array = null;
}

/**
Returns a time series containing the predicted values for the dependent time
computed using the Y = a + bX where X is the values from the independent time
series. The time series has the same header as the dependent time series but
the location contains the "_predicted" extension.  The method also computes
the residual values between the dependent predicted and actual values. These
residual values are saved in a time series that has the same header as the
dependent time series but the location contains the "_residual" extension.
*/
public TS createPredictedTS()
throws Exception
{	
	String mthd = "TSRegression.createPredictedTS", mssg;
	
	// If the time series were already created, just return it.
	if ( __yTSpredicted != null && __yTSresidual != null ) {
		return __yTSpredicted;
	}
	
	// Create the time series for the predicted and residual time series.
	TSIdent predictedIdent = null;
	TSIdent residualIdent  = null;
	try {
		// Create a time series that has the same header as the dependent
		// time series, but with a location that has + "_predicted".
		predictedIdent = new TSIdent( _yTS.getIdentifier() );
		predictedIdent.setLocation ( predictedIdent.getLocation() + "_predicted");
		// Create the new time series using the full identifier.
		__yTSpredicted = TSUtil.newTimeSeries(  predictedIdent.getIdentifier(), true );
		if ( __yTSpredicted == null ) {
			mssg = "Could not create the predicted time series.";
			throw new TSException ( mssg );
		}
		
		// Create a time series that has the same header as the dependent
		// time series, but with a location that has + "_residual".
		residualIdent = new TSIdent( _yTS.getIdentifier() );
		residualIdent.setLocation ( residualIdent.getLocation() + "_residual" );
		// Create the new time series using the full identifier.
		__yTSresidual = TSUtil.newTimeSeries( residualIdent.getIdentifier(), true );
		if ( __yTSresidual == null ) {
			mssg = "Could not create the residual time series.";
			throw new TSException ( mssg );
		}
		
	} catch ( Exception e ) {
		Message.printWarning ( 3, mthd, e );
		mssg ="Error creating new time series.";
		Message.printWarning ( 3, mthd, mssg );
		throw new TSException ( mssg );
	}
	
	// Update the new time series properties with all required information.
	// Notice: CopyHeader() overwrites, among several other things,
	//	   the Identifier, the DataInterval (Base and Multiplier).
	//         It also set the dates, from the old time series. Make sure to
	//         reset these properties to the values needed by the new time
	//	   series. Finally allocate data space.
	__yTSpredicted.copyHeader ( _yTS );
	__yTSpredicted.setIdentifier ( predictedIdent  ); 
	__yTSpredicted.allocateDataSpace ();
	__yTSresidual.copyHeader ( _yTS );
	__yTSresidual.setIdentifier ( residualIdent  ); 
	__yTSresidual.allocateDataSpace ();
	
	// Define the number of equations to be used in the loop
	int num_equations = 1;
	if ( __numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS ) {
		num_equations = 12;
	}
	
	// Use the equation to set the data values in the predicted time series
	DateTime startDate = new DateTime ( __yTSpredicted.getDate1() );
	DateTime endDate = new DateTime ( __yTSpredicted.getDate2() );
	int interval_base = _yTS.getDataIntervalBase();
	int interval_mult = _yTS.getDataIntervalMult();
	DateTime date;
	double Xvalue, Yvalue, preYvalue, resYvalue;
	double A, B;
	
	for ( int ieq = 1; ieq <= num_equations; ieq++ ) {
		
		try {
			if ( num_equations == 1 ) {
				A = getA();
				B = getB();
			}
			else {
				A = getA( ieq );
				B = getB( ieq );	
			}
			
			for ( date = new DateTime(startDate); date.lessThanOrEqualTo( endDate );
				date.addInterval( interval_base, interval_mult ) ) {
	
				// Process only for a single month if monthly
				if ( ( num_equations != 1 ) && ( date.getMonth() != ieq ) ) {
					continue;
				}
					
				// If not in the dependent analysis period let missing in the __yTSpredicted.
				if ( date.lessThan( _dep_analysis_period_start) || date.greaterThan(_dep_analysis_period_end ) ) {
					continue;	
				}
					
				// Get the value from the independentTS.
				Xvalue = _xTS.getDataValue( date );
				Yvalue = _yTS.getDataValue( date );
				if ( _xTS.isDataMissing( Xvalue ) ) {
					// If missing let missing in the __yTSpredicted.
					continue;
				}
		
				// Now if filling, estimate Y1 using A and B
				if ( __transformation == DataTransformationType.LOG ) {
					// double Ytemp, Xtemp;
					// Estimate in log10 space.
					if ( Xvalue <= 0.0 ) {
						// Assume X = .001
						Xvalue = -3.0;
					}
					else {
						Xvalue  = MathUtil.log10(Xvalue);
					}
					// Compute the estimated value.
					preYvalue = A + Xvalue * B;		
					// Un-transform from log space.  
					preYvalue = Math.pow( 10.0, preYvalue );
				}
				else {
					preYvalue = A + Xvalue * B;	
				}
				
				// Saving the predicted value.
				__yTSpredicted.setDataValue ( date, preYvalue );
				
				// Computing and saving the residual value, if the YValue is not missing.
				if ( !_yTS.isDataMissing( Yvalue ) ) {
					resYvalue = preYvalue - Yvalue;
					__yTSresidual.setDataValue( date, resYvalue );
				} 			
			}		
		}
		catch ( Exception e ) {
			// Error computing the predicted values.'
			mssg = "Error computing the predicted/residual values";
			Message.printWarning( 3, mthd, mssg );
			Message.printWarning( 3, mthd, e );
			continue;
		}
	}
	
	return __yTSpredicted; 
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	
	__yTSpredicted = null;
	__yTSresidual  = null;
	
	_xTS = null;
	_yTS = null;

	_b_monthly = null;
	_a_monthly = null;
	_n1_monthly = null;
	_rmse_monthly = null;
	_r_monthly = null;
	_is_analyzed_monthly = null;
	super.finalize();
}

/**
Free the resources computed during the analysis, including arrays of numbers.  Normally these are left in
memory to facilitate reporting or further analysis (although currently there are no getter methods).
Freeing the resources may be necessary in large analysis.
*/
public void freeResources ()
{
    __X = null;
    __X_monthly = null;
    __X1 = null;
    __X1_monthly = null;
    __Y1 = null;
    __Y1_monthly = null;
}

/**
Return The "a" value in the equation Y = a + b * X where Y is the estimated
time series value, a is the intercept of the equation, b is the slope, and X is
the known value.  This is a value that has been calculated for each month.
The base class has a getA() when only one relationship is used.
@return A intercept value.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no regression data available for the month.
@see RTi.Util.Math.Regression#getA
*/
public double getA ( int monthIndex )
throws TSException
{   if ( !isAnalyzed(monthIndex) ) {
        throw new TSException ( "No regression computed for month " + 
        monthIndex );
    }
    if ( (monthIndex < 1) || (monthIndex > 12) ) {
        throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
    }
    return _a_monthly[monthIndex-1];
}

/**
Return the analysis method.
@return the analysis method.
*/
public RegressionType getAnalysisMethod ()
{
    return __analysisMethod;
}

/**
Return an array indicating if a month is to be analyzed.  This information
corresponds to the AnalysisMonth property that is passed in at construction.
*/
public boolean [] getAnalyzeMonth ()
{   return _analyze_month;
}

/**
Return an array indicating the months to be analyzed, each value 1-12.  This information
corresponds to the AnalysisMonth property that is passed in at construction.
@return the array containing the months (1-12) to be analyzed, or null if all months will be analyzed.
*/
public int [] getAnalysisMonths ()
{   return _analyze_month_list;
}

/**
Return the "b" value in the equation Y = a + b * X where Y is the estimated ts
value, a is the intercept of the equation, b is the slope, and X is the known
value.  This is a value which has been calculated for each month.  The base
class has a getB when only one relationship is used.
@return B slope value.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no regression data available for the month.
@see RTi.Util.Math.Regression#getB
*/
public double getB ( int monthIndex )
throws TSException
{   if ( !isAnalyzed(monthIndex) ) {
        throw new TSException ( "No regression computed for month " + monthIndex );
    }
    if ( (monthIndex < 1) || (monthIndex > 12) ) {
        throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
    }
    return _b_monthly[monthIndex-1];
}

/**
Return the correlation coefficient between the two time series that have been
analyzed.  This is a value that has been calculated for each month.  The base
class has a getCorrelationCoefficient when only one relationship is used.
@return The correlation coefficient R.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no regression data available for the month.
@see RTi.Util.Math.Regression#getCorrelationCoefficient
*/
public double getCorrelationCoefficient ( int monthIndex )
throws TSException
{   if ( !isAnalyzed(monthIndex) ) {
        throw new TSException ( "No regression computed for month " + 
        monthIndex );
    }
    if ( (monthIndex < 1) || (monthIndex > 12) ) {
        throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
    }
    return _r_monthly[monthIndex-1];
}

/**
Return the dependent time series analysis end.
@return the dependent time series analysis end.
*/
public DateTime getDependentAnalysisEnd()
{	return _dep_analysis_period_end;
}

/**
Return the dependent time series analysis start.
@return the dependent time series analysis start.
*/
public DateTime getDependentAnalysisStart()
{	return _dep_analysis_period_start;
}

/**
Return the dependent (Y) time series.
@return the dependent (Y) time series.
*/
public TS getDependentTS()
{	return _yTS;
}

/**
Return the fill end (used when analyzing for filling).
@return the fill end.
*/
public DateTime getFillEnd()
{   return _fill_period_end;
}

/**
Return the fill start (used when analyzing for filling).
@return the fill start.
*/
public DateTime getFillStart()
{   return _fill_period_start;
}

/**
Return the independent time series analysis end.
@return the independent time series analysis end.
*/
public DateTime getIndependentAnalysisEnd()
{	return _ind_analysis_period_end;
}

/**
Return the independent time series analysis start.
@return the independent time series analysis start.
*/
public DateTime getIndependentAnalysisStart()
{	return _ind_analysis_period_start;
}

/**
Return the independent (X) time series.
@return the independent (X) time series.
*/
public TS getIndependentTS()
{	return _xTS;
}

/**
Return the predicted (Y) time series.
@return the predicted (Y) time series or null if the method createPredictedTS() was not called yet.
*/
public TS getPredictedTS()
{	
	return __yTSpredicted;
}

/**
Return the residual TS ( difference between the predicted and the original dependent time series.
@return the residual TS ( difference between the predicted and the original dependent time series.
*/
public TS getResidualTS()
{	
	return __yTSresidual;
}

/**
Return the maximum for X.
@return the maximum for X.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMaxX ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X_max_monthly[monthIndex-1];
}

/**
Return the maximum for X1.
@return the maximum for X1.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMaxX1 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X1_max_monthly[monthIndex-1];
}

/**
Return the maximum for X2.
@return the maximum for X2.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMaxX2 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X2_max_monthly[monthIndex-1];
}

/**
Return the maximum for Y1.
@return the maximum for Y1.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMaxY1 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _Y1_max_monthly[monthIndex-1];
}

/**
Return the mean for X.
@return the mean for X.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMeanX ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X_mean_monthly[monthIndex-1];
}

/**
Return the mean for X1.
@return the mean for X1.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMeanX1 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X1_mean_monthly[monthIndex-1];
}

/**
Return the mean for X2.
@return the mean for X2.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMeanX2 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X2_mean_monthly[monthIndex-1];
}

/**
Return the mean for Y.
@return the mean for Y.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMeanY ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _Y_mean_monthly[monthIndex-1];
}

/**
Return the mean for Y1.
@return the mean for Y1.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMeanY1 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _Y1_mean_monthly[monthIndex-1];
}

/**
Return the mean for Y1_estimated.
@return the mean for Y1_estimated.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMeanY1Estimated ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _Y1_estimated_mean_monthly[monthIndex-1];
}

/**
Return the minimum for X.
@return the minimum for X.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMinX ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X_min_monthly[monthIndex-1];
}

/**
Return the minimum for X1.
@return the minimum for X1.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMinX1 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X1_min_monthly[monthIndex-1];
}

/**
Return the minimum for X2.
@return the minimum for X2.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMinX2 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X2_min_monthly[monthIndex-1];
}

/**
Return the minimum for Y1.
@return the minimum for Y1.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getMinY1 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _Y1_min_monthly[monthIndex-1];
}

/**
Return The number of points N1 used in the analysis for the two time series.
The number is by month.  The base class has a getN1 when only one relationship is used.
@return the number of data points N1 used in an analysis.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if the month index is out of range.
@see RTi.Util.Math.Regression#getRMSE
*/
public int getN1 ( int monthIndex )
throws TSException
{	// Always return because the number of points may illustrate a data problem.
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex	+ " out of range 1-12." );
	}
	return _n1_monthly[monthIndex-1];
}

/**
Return The number of points N2 used in the analysis for the two time series.
The number is by month.  The base class has a getN1 when only one relationship is used.
@return the number of data points N2 used in an analysis.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if the month index is out of range.
@see RTi.Util.Math.Regression#getRMSE
*/
public int getN2 ( int monthIndex )
throws TSException
{	// Always return because the number of points may illustrate a data problem.
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _n2_monthly[monthIndex-1];
}

// TODO SAM 2009-08-29 Replace this method with normal get methods
/**
Return the properties that are used to control the analysis.  The properties are
a full list.  The original properties passed in to the constructor may not have
defined all values.  However, default property values assigned internally are
reflected in the returned PropList.  See also methods that return values 
directly (e.g., getDependentAnalysisPeriodStart()).
@return the properties that are used to control the analysis.
*/
/*
public PropList getPropList ()
{	// Create a copy of the main PropList...
	PropList props = new PropList ( _props );
	// Assign values that may have been determined internally as defaults...
	props.set ( "AnalysisMethod", _AnalysisMethod );
	if ( _analyze_month_list == null ) {
		props.set ( "AnalysisMonth", "" );
	}
	props.set ( "AnalyzeForFilling", "" + _filling );
	// Use the following instead of calling toString(), to allow null,
	// although hopefully that will not occur...
	props.set ( "DependentAnalysisStart", "" + _dep_analysis_period_start );
	props.set ( "DependentAnalysisEnd", "" + _dep_analysis_period_end );
	props.set ( "FillStart", "" + _fill_period_start );
	props.set ( "FillEnd", "" + _fill_period_end );
	props.set ( "IndependentAnalysisStart", ""+_ind_analysis_period_start );
	props.set ( "IndependentAnalysisEnd", "" + _ind_analysis_period_end );
	props.set ( "Intercept", "" + __intercept );
}
*/

/**
Indicate whether the analysis is performed using one equation or a monthly basis.
@return the number of equations used for the analysis.
*/
public NumberOfEquationsType getNumberOfEquations( )
{   return __numberOfEquations;
}

/**
Return the RMS error for the correlation between the two time series that have
been analyzed.  This is a value which has been calculated for each month.
The base class has a getRMSE() when only one relationship is used.
@return the RMS error.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no regression data available for the month.
@see RTi.Util.Math.Regression#getRMSE
*/
public double getRMSE ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _rmse_monthly[monthIndex-1];
}

/**
Return the standard deviation for X.
@return the standard deviation for X.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getStandardDeviationX ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X_stddev_monthly[monthIndex-1];
}

/**
Return the standard deviation for X1.
@return the standard deviation for X1.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getStandardDeviationX1 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X1_stddev_monthly[monthIndex-1];
}

/**
Return the standard deviation for X2.
@return the standard deviation for X2.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getStandardDeviationX2 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _X2_stddev_monthly[monthIndex-1];
}

/**
Return the standard deviation for Y.
@return the standard deviation for Y.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getStandardDeviationY ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _Y_stddev_monthly[monthIndex-1];
}

/**
Return the standard deviation for Y1.
@return the standard deviation for Y1.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getStandardDeviationY1 ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _Y1_stddev_monthly[monthIndex-1];
}

/**
Return the standard deviation for Y1_estimated.
@return the standard deviation for Y1_estimated.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no analysis data available for the month.
*/
public double getStandardDeviationY1Estimated ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _Y1_estimated_stddev_monthly[monthIndex-1];
}

/**
Get the transformation that has been applied to the data prior to the analysis.
@return the transformation that has been applied to the data prior to the analysis.
*/
public DataTransformationType getTransformation ( )
{   return __transformation;
}

/**
Return the RMS error for the transformed data for a month.
The base class has a getTransformedRMSE() when only one relationship is used.
@return the RMS error for the transformed data.
@param monthIndex The integer representation for the month of interest (1 is January).
@exception TSException if there is no data available for the month.
@see RTi.Util.Math.Regression#getRMSE
*/
public double getTransformedRMSE ( int monthIndex )
throws TSException
{	if ( !isAnalyzed(monthIndex) ) {
		throw new TSException ( "No analysis results available for month " + monthIndex );
	}
	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		throw new TSException ( "Month index " + monthIndex + " out of range 1-12." );
	}
	return _transformed_rmse_monthly[monthIndex-1];
}

/**
Initialize instance data.
@param xTS Independent time series.
@param yTS Dependent time series.
*/
private void initialize ( TS xTS, TS yTS, boolean analyzeForFilling, RegressionType analysisMethod,
    Double intercept, NumberOfEquationsType numberOfEquations, int [] analysisMonths,
    DataTransformationType transformation,
    DateTime dependentAnalysisStart, DateTime dependentAnalysisEnd,
    DateTime independentAnalysisStart, DateTime independentAnalysisEnd,
    DateTime fillStart, DateTime fillEnd )
{    
    // FIXME SAM 2009-04-03 Evaluate going away from PropList - too easy to have errors.  Or, add check
    // to warn about properties that are not recognized - fixed when mixed station is fixed.
    
    __yTSpredicted = null;
    __yTSresidual  = null;
    
    _xTS = xTS;
    _yTS = yTS;
    
    _filling = analyzeForFilling; // Default previously was false

    // Check for analysis method...

    __analysisMethod = analysisMethod;
    if ( __analysisMethod == null ) {
        __analysisMethod = RegressionType.OLS_REGRESSION; // Default
    }

    __intercept = intercept;

    // Check for monthly or one relationship (later add seasonal)...

    __numberOfEquations = numberOfEquations;
    if ( numberOfEquations == null ) {
        __numberOfEquations = NumberOfEquationsType.ONE_EQUATION; // Default
    }

    // Check for log10 or normal regression...

    __transformation = transformation;
    if ( __transformation == null ) {
        __transformation = DataTransformationType.NONE; // Default
    }

    // Check dependent analysis period...

    _dep_analysis_period_start = yTS.getDate1(); // Default and parse below
    if ( dependentAnalysisStart != null ) {
        _dep_analysis_period_start = dependentAnalysisStart;
    }
    _dep_analysis_period_end = yTS.getDate2(); // Default and parse below
    if ( dependentAnalysisEnd != null ) {
        _dep_analysis_period_end = dependentAnalysisEnd;
    }

    // Check independent analysis period...

    if ( __analysisMethod == RegressionType.MOVE2 ) {
        _ind_analysis_period_start = xTS.getDate1();
        if ( independentAnalysisStart != null ) {
            _ind_analysis_period_start = independentAnalysisStart;
        }
        _ind_analysis_period_end = xTS.getDate2();
        if ( independentAnalysisEnd != null ) {
            _ind_analysis_period_end = independentAnalysisEnd;
        }
    }
    else {
        // Independent analysis period is the same as the dependent...
        _ind_analysis_period_start = _dep_analysis_period_start;
        _ind_analysis_period_end = _dep_analysis_period_end;
    }

    // Check fill period...

    _fill_period_start = yTS.getDate1();
    if ( fillStart != null ) {
        _fill_period_start = fillStart;
    }
    _fill_period_end = yTS.getDate2();
    if ( fillEnd != null ) {
        _fill_period_end = fillEnd;
    }

    _b_monthly = new double[12];
    _a_monthly = new double[12];
    _n1_monthly = new int[12];
    _n2_monthly = new int[12];
    _X_max_monthly = new double[12];
    _X1_max_monthly = new double[12];
    _X2_max_monthly = new double[12];
    _Y1_max_monthly = new double[12];
    _X_min_monthly = new double[12];
    _X1_min_monthly = new double[12];
    _X2_min_monthly = new double[12];
    _Y1_min_monthly = new double[12];
    _X_mean_monthly = new double[12];
    _Y_mean_monthly = new double[12];
    _X1_mean_monthly = new double[12];
    _X2_mean_monthly = new double[12];
    _Y1_mean_monthly = new double[12];
    _Y1_estimated_mean_monthly = new double[12];
    _X_stddev_monthly = new double[12];
    _Y_stddev_monthly = new double[12];
    _X1_stddev_monthly = new double[12];
    _X2_stddev_monthly = new double[12];
    _Y1_stddev_monthly = new double[12];
    _Y1_estimated_stddev_monthly = new double[12];
    _rmse_monthly = new double[12];
    _transformed_rmse_monthly = new double[12];
    _r_monthly = new double[12];
    _is_analyzed = false;
    _is_analyzed_monthly = new boolean[12];
    _analyze_month = new boolean[12];
    _analyze_month_list = null;

    for ( int i=0; i<12; i++ ) {
        _a_monthly[i] = 0;
        _b_monthly[i] = 0;
        _n1_monthly[i] = 0;
        _n2_monthly[i] = 0;
        _X_max_monthly[i] = 0.0;
        _X1_max_monthly[i] = 0.0;
        _X2_max_monthly[i] = 0.0;
        _Y1_max_monthly[i] = 0.0;
        _X_min_monthly[i] = 0.0;
        _X1_min_monthly[i] = 0.0;
        _X2_min_monthly[i] = 0.0;
        _Y1_min_monthly[i] = 0.0;
        _X_mean_monthly[i] = 0.0;
        _Y_mean_monthly[i] = 0.0;
        _X1_mean_monthly[i] = 0.0;
        _X2_mean_monthly[i] = 0.0;
        _Y1_estimated_mean_monthly[i] = 0.0;
        _X_stddev_monthly[i] = 0.0;
        _Y_stddev_monthly[i] = 0.0;
        _X1_stddev_monthly[i] = 0.0;
        _X2_stddev_monthly[i] = 0.0;
        _Y1_stddev_monthly[i] = 0.0;
        _Y1_estimated_stddev_monthly[i] = 0.0;
        _rmse_monthly[i] = 0;
        _transformed_rmse_monthly[i] = 0;
        _is_analyzed_monthly[i] = false;
        _r_monthly[i] = 0;
        _analyze_month[i] = true; // Default is to analyze all months.
    }

    // Check the month to analyze (default is all)...

    if ( (analysisMonths != null) && (analysisMonths.length > 0)  ) {
        _analyze_month_list = analysisMonths;
        if ( analysisMonths.length > 0 ) {
            // Reset all monthly flags to false.  Selected months will be set to true below.
            for ( int i = 0; i < 12; i++ ) {
                _analyze_month[i] = false;
            }
            int imon;
            for ( int i = 0; i < analysisMonths.length; i++ ) {
                imon = analysisMonths[i];
                // TODO SAM what to do with list if not right?  Allow exception to be thrown
                //if ( (imon >= 1) && (imon <= 12) ) {
                    _analyze_month[imon - 1] = true;
                //}
            }
        }
    }
}

/**
Determine if monthly analysis results are available.
@return true if the analysis relationship is available for the requested month (1 is January).
@param monthIndex Index for month.
*/
public boolean isAnalyzed ( int monthIndex )
{	if ( (monthIndex < 1) || (monthIndex > 12) ) {
		return false;
	}
	return _is_analyzed_monthly[monthIndex - 1];
}

/**
Set the flag indicating whether a monthly analysis relationship is available.
@param monthIndex month (1 is January).
@param flag true if the time series have been analyzed and results are available.
*/
public boolean isAnalyzed ( int monthIndex, boolean flag )
{	_is_analyzed_monthly[monthIndex - 1] = flag;
	return flag;
}

/**
Indicate whether the analysis is for monthly equations.
@return true if the analysis is monthly.
*/
public boolean isMonthlyAnalysis ()
{
    if ( __numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS ) {
        return true;
    }
    else {
        return false;
    }
}

/**
Set the A value for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param A Value of A.
*/
public void setA ( int monthIndex, double A )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_a_monthly[monthIndex-1] = A;
	}
}

/**
Set the B value for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param B Value of B.
*/
public void setB ( int monthIndex, double B )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_b_monthly[monthIndex-1] = B;
	}
}

/**
Set the correlation coefficient for a particular month.  
@param monthIndex The numerical value for the intended month (1 is January).
@param coeff The correlation coefficient for the month.
*/
public void setCorrelationCoefficient ( int monthIndex, double coeff )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_r_monthly[monthIndex-1] = coeff;
	}
}

/**
Set the maximum of X for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param max maximum for the indicated month.
*/
public void setMaxX ( int monthIndex, double max )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X_max_monthly[monthIndex-1] = max;
	}
}

/**
Set the maximum of X1 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param max maximum for the indicated month.
*/
public void setMaxX1 ( int monthIndex, double max )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X1_max_monthly[monthIndex-1] = max;
	}
}

/**
Set the maximum of X2 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param max maximum for the indicated month.
*/
public void setMaxX2 ( int monthIndex, double max )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X2_max_monthly[monthIndex-1] = max;
	}
}

/**
Set the maximum of Y1 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param max maximum for the indicated month.
*/
public void setMaxY1 ( int monthIndex, double max )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_Y1_max_monthly[monthIndex-1] = max;
	}
}

/**
Set the mean of X for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param mean mean for the indicated month.
*/
public void setMeanX ( int monthIndex, double mean )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X_mean_monthly[monthIndex-1] = mean;
	}
}

/**
Set the mean of X1 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param mean mean for the indicated month.
*/
public void setMeanX1 ( int monthIndex, double mean )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X1_mean_monthly[monthIndex-1] = mean;
	}
}

/**
Set the mean of X2 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param mean mean for the indicated month.
*/
public void setMeanX2 ( int monthIndex, double mean )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X2_mean_monthly[monthIndex-1] = mean;
	}
}

/**
Set the mean of Y for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param mean mean for the indicated month.
*/
public void setMeanY ( int monthIndex, double mean )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_Y_mean_monthly[monthIndex-1] = mean;
	}
}

/**
Set the mean of Y1 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param mean mean for the indicated month.
*/
public void setMeanY1 ( int monthIndex, double mean )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_Y1_mean_monthly[monthIndex-1] = mean;
	}
}

/**
Set the mean of Y1_estimated for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param mean mean for the indicated month.
*/
public void setMeanY1Estimated ( int monthIndex, double mean )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_Y1_estimated_mean_monthly[monthIndex-1] = mean;
	}
}

/**
Set the minimum of X for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param min minimum for the indicated month.
*/
public void setMinX ( int monthIndex, double min )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X_min_monthly[monthIndex-1] = min;
	}
}

/**
Set the minimum of X1 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param min minimum for the indicated month.
*/
public void setMinX1 ( int monthIndex, double min )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X1_min_monthly[monthIndex-1] = min;
	}
}

/**
Set the minimum of X2 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param min minimum for the indicated month.
*/
public void setMinX2 ( int monthIndex, double min )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X2_min_monthly[monthIndex-1] = min;
	}
}

/**
Set the minimum of Y1 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param min minimum for the indicated month.
*/
public void setMinY1 ( int monthIndex, double min )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_Y1_min_monthly[monthIndex-1] = min;
	}
}

/**
Set the number of points N1 used in the analysis for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param n1 The number of points used in the analysis for the month (non-missing overlapping data).
*/
public void setN1 ( int monthIndex, int n1 )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_n1_monthly[monthIndex-1] = n1;
	}
}

/**
Set the number of points N2 used in the analysis for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param n2 The number of points used in the analysis for the month (non-missing overlapping data).
*/
public void setN2 ( int monthIndex, int n2 )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_n2_monthly[monthIndex-1] = n2;
	}
}

/**
Set the RMS error for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param rmse RMS error for the indicated month.
*/
public void setRMSE ( int monthIndex, double rmse )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_rmse_monthly[monthIndex-1] = rmse;
	}
}

/**
Set the standard deviation of X for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param stddev Standard deviation for the indicated month.
*/
public void setStandardDeviationX ( int monthIndex, double stddev )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X_stddev_monthly[monthIndex-1] = stddev;
	}
}

/**
Set the standard deviation of X1 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param stddev Standard deviation for the indicated month.
*/
public void setStandardDeviationX1 ( int monthIndex, double stddev )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X1_stddev_monthly[monthIndex-1] = stddev;
	}
}

/**
Set the standard deviation of X2 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param stddev Standard deviation for the indicated month.
*/
public void setStandardDeviationX2 ( int monthIndex, double stddev )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_X2_stddev_monthly[monthIndex-1] = stddev;
	}
}

/**
Set the standard deviation of Y for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param stddev Standard deviation for the indicated month.
*/
public void setStandardDeviationY ( int monthIndex, double stddev )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_Y_stddev_monthly[monthIndex-1] = stddev;
	}
}

/**
Set the standard deviation of Y1 for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param stddev Standard deviation for the indicated month.
*/
public void setStandardDeviationY1 ( int monthIndex, double stddev )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_Y1_stddev_monthly[monthIndex-1] = stddev;
	}
}

/**
Set the standard deviation of Y1_estimated for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param stddev Standard deviation for the indicated month.
*/
public void setStandardDeviationY1Estimated ( int monthIndex, double stddev )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_Y1_estimated_stddev_monthly[monthIndex-1] = stddev;
	}
}

/**
Set the RMS error for the transformed data for a particular month.
@param monthIndex The numerical value for the intended month (1 is January).
@param transformed_rmse RMS error for the transformed data for the indicated month.
*/
public void setTransformedRMSE ( int monthIndex, double transformed_rmse )
{	if ( (monthIndex >= 1) && (monthIndex <= 12) ) {
		_transformed_rmse_monthly[monthIndex-1] = transformed_rmse;
	}
}

/**
Return string representation of analysis data.
@return A string representation of the regression data.  A multi-line table
of results is returned, suitable for inclusion in a report.  The lines are separated by "\n".
*/
public String toString ()
{	String nl = "\n";
	StringBuffer stats = new StringBuffer();

	// Print header information...

	if ( _filling ) {
		stats.append (
		"Independent time series (X, " + _xTS.getDate1() + " - " +
		_xTS.getDate2() + "): " +
		_xTS.getIdentifierString() + " (" +_xTS.getDescription() + ") "+
		_xTS.getDataUnits() + nl +
		"Time series being filled (Y, dependent, " +
		_yTS.getDate1() + " - " + _yTS.getDate2() + "): " +
		_yTS.getIdentifierString() + " (" +_yTS.getDescription() + ") "+
		_yTS.getDataUnits() + nl );
	}
	else {
	    stats.append (
		"Independent time series (X, " + _xTS.getDate1() + " - " +
		_xTS.getDate2() + "): " +
		_xTS.getIdentifierString()+ " (" + _xTS.getDescription() + ") "+
		_xTS.getDataUnits() + nl +
		"Dependent time series (Y, " + _yTS.getDate1() + " - " +
		_yTS.getDate2() + "): " +
		_yTS.getIdentifierString()+ " (" + _yTS.getDescription() + ") "+
		_yTS.getDataUnits() + nl );
	}
	
	if ( __analysisMethod == RegressionType.MOVE2 ) {
		stats.append ( "Dependent analysis period:  " + _dep_analysis_period_start.toString()
			+ " to " + _dep_analysis_period_end.toString() + nl );
		stats.append ( "Independent analysis period:  " + _ind_analysis_period_start.toString()
			+ " to " + _ind_analysis_period_end.toString() + nl );
	}
	else {
	    // Analysis period applies to both dependent and independent
	    stats.append ( "Analysis period:  " + _dep_analysis_period_start.toString()
			+ " to " + _dep_analysis_period_end.toString() + nl );
	}
	if ( _filling ) {
		stats.append ( "Fill period:  " + _fill_period_start.toString()
			+ " to " + _fill_period_end.toString() + nl );
	}
	if ( __intercept != null ) {
		stats.append ("Intercept (A) was assigned, not calculated."+nl);
	}
	if ( __analysisMethod == RegressionType.OLS_REGRESSION ) {
		stats.append ( "Analysis method:  Ordinary Least Squares Regression" + nl);
	}
	else if ( __analysisMethod == RegressionType.MOVE1 ) {
		stats.append ( "Analysis method:  Maintenance of Variance Extension (MOVE.1)" + nl );
	}
	else if ( __analysisMethod == RegressionType.MOVE2 ) {
		stats.append ( "Analysis method:  Maintenance of Variance Extension (MOVE.2)" + nl );
	}
	
	stats.append ( "Data transformation:  " + __transformation + nl );

	if ( __numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS ) {
		stats.append ( "Number of equations:  12 (Monthly)" + nl );
		for ( int i = 1; i <= 12; i++ ) {
			if ( !isAnalyzed(i) ) {
				stats.append ( "THE ANALYSIS FOR " + TimeUtil.monthAbbreviation(i) +
				" FAILED FOR SOME REASON.  The results shown are for information only." + nl );
			}
		}
		stats.append ( nl );
	}
	else {
		stats.append ( "Number of equations:  1" + nl);
		if ( !isAnalyzed() ) {
			stats.append ( "THE ANALYSIS FAILED FOR SOME REASON.  The results " +
			"shown are for information only." + nl );
		}
		stats.append ( nl );
	}

	// Print a table with the independent station information...

	String format = "%12.2f"; // Normal data - probably should check units (SAMX)
	if ( __transformation != DataTransformationType.NONE ) {
		format = "%12.6f"; // Need more precision for transformed data
	}
	stats.append ( "-------------------------------------------------------------------------------------------------" );
	stats.append ( nl );
	stats.append ( "|   |                                     Independent (X)                                       |" + nl );
	stats.append ( "|Mon|  N1  |   MeanX1   |    SX1     |  N2  |   MeanX2   |    SX2     |    MeanX   |     SX     |" + nl );
	//	           "|XXX XXXXXX XXXXXXXXX.XX XXXXXXXXX.XX XXXXXX XXXXXXXXX.XX XXXXXXXXX.XX XXXXXXXXX.XX XXXXXXXXX.XX|"
	//      "XXXXXXXXX.XX XXXXXXXXX.XX sX.XXXX XXXXXXXXX.XX "
	//			       "XXXXXXXXX.XX XXXXXXXXX.XX "
	stats.append ( "-------------------------------------------------------------------------------------------------" + nl );
	if ( __numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS ) {
		for ( int i = 1; i <= 12; i++ ) {
			stats.append ( "|" + TimeUtil.monthAbbreviation(i) + "|" );
			try {
			    stats.append (
				StringUtil.formatString(getN1(i),"%6d") + "|" +
				StringUtil.formatString(getMeanX1(i),format)+"|"+
				StringUtil.formatString(getStandardDeviationX1(i),format)+"|"+
				StringUtil.formatString(getN2(i),"%6d") + "|" +
				StringUtil.formatString(getMeanX2(i),format)+"|"+
				StringUtil.formatString(getStandardDeviationX2(i),format)+"|"+
				StringUtil.formatString(getMeanX(i),format)+"|"+
				StringUtil.formatString(getStandardDeviationX(i),format)+"|"+ nl);
			}
			catch ( TSException e ) {
				// Should never get this since we checked isAnalyzed().
				stats.append ( nl );
			}
		}
	}
	else {
	    // Single equation...
		try {
		    // Else do full format...
			stats.append ( "|All|" +
			StringUtil.formatString(getN1(),"%6d") + "|" +
			StringUtil.formatString(getMeanX1(),format)+"|"+
			StringUtil.formatString(getStandardDeviationX1(),format)+"|"+
			StringUtil.formatString(getN2(),"%6d") + "|" +
			StringUtil.formatString(getMeanX2(),format)+"|"+
			StringUtil.formatString(getStandardDeviationX2(),format)+"|"+
			StringUtil.formatString(getMeanX(),format)+"|"+
			StringUtil.formatString(getStandardDeviationX(),format)+"|"+nl);
		}
		catch ( Exception e ) {
			// TODO SAM 2009-03-10 need to handle
		}
	}
	stats.append ( "-------------------------------------------------------------------------------------------------" + nl );

	// Now print a table with dependent and line fit results...

	stats.append ( "----------------------------------------------------------------------------------------------------------------" );
	if ( __transformation != DataTransformationType.NONE ) {
		stats.append ( "-------------" );
	}
	if ( _filling ) {
		stats.append ( "--------------------------" );
	}
	stats.append ( nl );
	stats.append ( "|   |                   Dependent (Y)                   |         Line Fit Results                |            " );
	if ( __transformation != DataTransformationType.NONE ) {
		stats.append ( "             " );
	}
	if ( _filling ) {
		stats.append ( "                          |" );
	}
	else {
	    stats.append ( "|" );
	}
	stats.append ( nl );
	stats.append ( "|Mon|   MeanY1   |    SY1     |   MeanY    |     SY     |     A      |      B     |   R   |  R^2  |" );
	if ( __transformation != DataTransformationType.NONE ) {
		stats.append ( "RMSE (log10)|" );
		stats.append ( "RMSE (data) |" );
	}
	else {
	    stats.append ( "    RMSE    |" );
	}
	if ( _filling ) {
		stats.append ( " MeanY1_est |   SY1_est  |" );
	}
	stats.append ( nl );
	//stats.append (
	//"         RMS Error  NumPoints" + nl );
	//	"XXX XXXXXX XXXXXXXXX.XX XXXXXXXXX.XX XXXXXX XXXXXXXXX.XX "
	//      "XXXXXXXXX.XX XXXXXXXXX.XX XXXXXXXXX.XX XXXXXXXXX.XX "
	//      "XXXXXXXXX.XX XXXXXXXXX.XX sX.XXXX XXXXXXXXX.XX "
	//			       "XXXXXXXXX.XX XXXXXXXXX.XX "
	stats.append ( "----------------------------------------------------------------------------------------------------------------" );
	if ( __transformation != DataTransformationType.NONE ) {
		stats.append ( "-------------" );
	}
	if ( _filling ) {
		stats.append ( "--------------------------" );
	}
	stats.append ( nl );
	if ( __numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS ) {
		for ( int i = 1; i <= 12; i++ ) {
			stats.append ( "|" + TimeUtil.monthAbbreviation(i) + "|" );
			try {
			    stats.append ( StringUtil.formatString(getMeanY1(i),format)+"|"+
				StringUtil.formatString(getStandardDeviationY1(i),format)+"|"+
				StringUtil.formatString(getMeanY(i),format)+"|"+
				StringUtil.formatString(getStandardDeviationY(i),format)+"|"+
				StringUtil.formatString(getA(i),format)+ "|" +
				StringUtil.formatString(getB(i),format)+ "|" +
				StringUtil.formatString(getCorrelationCoefficient(i),"%7.4f")+ "|" +
				StringUtil.formatString(getCorrelationCoefficient(i)*getCorrelationCoefficient(i),"%7.4f")+ "|" );
				if ( __transformation != DataTransformationType.NONE ) {
					stats.append (StringUtil.formatString(getTransformedRMSE(i),"%12.4f")+ "|" );
				}
				stats.append (
				StringUtil.formatString(getRMSE(i),"%12.2f")+ "|" );
				if ( _filling ) {
					stats.append (
					StringUtil.formatString(getMeanY1Estimated(i),format)+"|"+
					StringUtil.formatString(getStandardDeviationY1Estimated(i),format)+"|" );
				}
				stats.append ( nl );
			}
			catch ( TSException e ) {
				// Should never get this since we checked isAnalyzed().
				stats.append ( nl );
			}
		}
	}
	else {
	    // Single equation...
		try {
		    stats.append ( "|All|" + StringUtil.formatString(getMeanY1(),format)+"|"+
			StringUtil.formatString(getStandardDeviationY1(),format)+"|"+
			StringUtil.formatString(getMeanY(),format)+"|"+
			StringUtil.formatString(getStandardDeviationY(),format)+"|"+
			StringUtil.formatString(getA(),format)+ "|" +
			StringUtil.formatString(getB(),format)+ "|" +
			StringUtil.formatString(getCorrelationCoefficient(),"%7.4f")+ "|" +
			StringUtil.formatString(getCorrelationCoefficient()*getCorrelationCoefficient(),"%7.4f")+ "|" );
			if ( __transformation != DataTransformationType.NONE ) {
				stats.append ( StringUtil.formatString(getTransformedRMSE(),"%12.4f")+ "|" );
			}
			stats.append ( StringUtil.formatString(getRMSE(),"%12.2f")+ "|" );
			if ( _filling ) {
				stats.append ( StringUtil.formatString(getMeanY1Estimated(),format)+"|"+
					StringUtil.formatString(getStandardDeviationY1Estimated(),format)+"|" );
			}
			stats.append ( nl );
		}
		catch ( Exception e ) {
			// FIXME SAM 2009-03-10 need to handle
		}
	}
	stats.append ("----------------------------------------------------------------------------------------------------------------" );
	if ( __transformation != DataTransformationType.NONE ) {
		stats.append ( "-------------" );
	}
	if ( _filling ) {
		stats.append ( "--------------------------" );
	}
	stats.append ( nl );

	if ( __numberOfEquations == NumberOfEquationsType.MONTHLY_EQUATIONS) {
		// Include a total RMSE row...
		stats.append ( "|All|                                                                                     |" );
		if ( __transformation != DataTransformationType.NONE ) {
			stats.append ( StringUtil.formatString(getTransformedRMSE(),"%12.4f")+ "|" );
		}
		stats.append (
		StringUtil.formatString( getRMSE(),"%12.2f")+ "|" );
		if ( _filling ) {
			stats.append ( "                         |" );
		}
		stats.append ( nl );
		stats.append ( "----------------------------------------------------------------------------------------------------------------" );
		if ( __transformation != DataTransformationType.NONE ) {
			stats.append ( "-------------" );
		}
		if ( _filling ) {
			stats.append ( "--------------------------" );
		}
		stats.append ( nl );
	}

	// Common footnotes...

	//stats.append (
	//"Minimum/Maximum from independent (X) time series = " +
	//_min_x1 + "/" + StringUtil.formatString(_max_x1,"%.6f") + nl +
	//"Minimum/Maximum from dependent (Y) series = " +
	//_min_y1 + "/" + StringUtil.formatString(_max_y1,"%.6f") + nl +
	stats.append ( "N1 indicates analysis period where X and Y are non-missing.  " +
		"N2 indicates analysis period where only X is non-missing." + nl );
	if ( __analysisMethod == RegressionType.MOVE2 ) {
		stats.append ( "The variance of the X2 values is considered in the MOVE.2 procedure." + nl );
		stats.append ( "MeanX and SX are for the period N1 + N2." + nl);
	}
	else {
	    stats.append ( "The N2 and full period values are provided as" +
			" information but are not considered in the regression analysis." + nl );
		stats.append ( "MeanX and SX are for the dependent available period (may be different than the analysis period)." + nl );
	}
	if ( _filling ) {
		stats.append ( "RMSE = sqrt(sum((Y1_est - Y1)^2)/N1), where" +
		" Y1_est is estimated using X1, A, and B, and Y1 is observed." + nl );
	}
	else {
	    stats.append ( "RMSE = sqrt(sum((Y1 - X1)^2)/N1), where Y1 is dependent and X1 is independent." + nl );
	}
	if ( __transformation == DataTransformationType.LOG ) {
		stats.append ( "RMSE is shown for original data units and log10 transform of data." + nl );
	}
	return stats.toString();
}

}