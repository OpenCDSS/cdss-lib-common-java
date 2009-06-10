/*
 * These routines help manipulate data for Principal Component Analysis
 * by arranging the data in arrays suitable for calculations.  Also, a
 * fill routine is provided to use the results from PCA to fill missing data
 * in a TS.
 */

package RTi.TS;

import RTi.Util.Math.MathUtil;
import RTi.Util.Math.PrincipalComponentAnalysis;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import java.util.List;

/**
 *
 * @author cen
 */
public class TSPrincipalComponentAnalysis {
    
TS _dependentTS;
List<TS> _independentTS;

private DateTime _analysis_period_start;
private DateTime _analysis_period_end;  // Analysis period
private boolean [] _analyze_month;      // Indicates the months to analyze.
private int [] _analyze_month_list;     // List of month numbers to analyze.
private int _maxCombinations;           // Maximum combination of solutions to retain.
double [][] _xMatrix = null;            // matrix representation of independentTS data
double [] _yArray = null;               // array representation of dependentTS data
PrincipalComponentAnalysis _pca;        // Analysis, including statistics, etc.


/**
 *
@param dependentTS The dependent time series (Y).
@param independentTS A list of the independent time series (X).
@param AnalysisPeriodStart Date/time indicating analysis period start (full period of dependent TS if null)
@param AnalysisPeriodEnd Date/time indicating analysis period end (full period of dependent TS if null)
@param AnalysisMonths Indicate the months that are to be analyzed.
If monthly equations are being used, indicate the one month to analyze.
Specify the months separated by commas or white space.
 *
 * @throws java.lang.Exception
 */
public TSPrincipalComponentAnalysis ( TS dependentTS, List<TS> independentTS, 
        DateTime AnalysisPeriodStart, DateTime AnalysisPeriodEnd,
        int maxCombinations, String analysisMonths )
throws Exception {

    initialize ( dependentTS, independentTS, AnalysisPeriodStart, AnalysisPeriodEnd,
            maxCombinations, analysisMonths );
    analyze ( );

}

private void analyze() throws Exception {
    
    double [] xArray = null;

    int numTS = _independentTS.size();
    boolean includeAnalyzeMonthList = ((_analyze_month_list == null) || (_analyze_month_list.length == 0)) ?
        false : true;

    // Get data specified or all data if analysis months weren't specified
    _yArray = includeAnalyzeMonthList ?
        TSUtil.toArray ( _dependentTS, _analysis_period_start, _analysis_period_end, _analyze_month_list ) :
        TSUtil.toArray ( _dependentTS, _analysis_period_start, _analysis_period_end );

    // Save independent observations in xMatrix[nObservations][numTS]
    int nObservations = _yArray.length;
    // now we know number of stations and number of observations
    _xMatrix = new double[nObservations][numTS];

    // fill all data into xMatrix
    for ( int nTS = 0; nTS < numTS; nTS++ ) {
        TS ts = _independentTS.get(nTS);
        xArray = includeAnalyzeMonthList ?
            TSUtil.toArray ( ts, _analysis_period_start, _analysis_period_end, _analyze_month_list ) :
            TSUtil.toArray ( ts, _analysis_period_start, _analysis_period_end );
        for ( int i=0; i<nObservations; i++)
            _xMatrix[i][nTS] = xArray[i];
    }

    _pca = MathUtil.performPrincipalComponentAnalysis(_yArray, _xMatrix,
            _dependentTS.getMissing(), _independentTS.get(0).getMissing(), _maxCombinations );
}

private void initialize(TS dependentTS, List<TS> independentTS, DateTime start, DateTime end,
        int maxCombinations, String analysisMonths ) throws Exception {

    String rtn = "TSPrincipalComponentAnalysis.initialize";
    _dependentTS = dependentTS;
    _independentTS = independentTS;

    // verify all TS have same interval
    int interval_base = _dependentTS.getDataIntervalBase();
	int interval_mult = _dependentTS.getDataIntervalMult();
    int numTS = _independentTS.size();
    for ( int nTS = 0; nTS < numTS; nTS++ ) {
        TS ts = _independentTS.get(nTS);
        if ( ts.getDataIntervalBase() != interval_base ||
             ts.getDataIntervalMult() != interval_mult ) {
            Message.printWarning ( 1, rtn, "All TS mult have same data interval.  Cannot continue analysis.");
            return;
        }
    }

    _analyze_month = new boolean[12];
    _analyze_month_list = null;
    if ( (analysisMonths != null) && !analysisMonths.equals("*") && !analysisMonths.equals("") ) {
        List tokens = StringUtil.breakStringList ( analysisMonths, " ,\t", StringUtil.DELIM_SKIP_BLANKS );
        int size = 0;
        if ( tokens != null ) {
            size = tokens.size();
        }
        for ( int i=0; i<12; i++ ) {
            _analyze_month[i] = true; // Default is to analyze all months.
        }
        if ( size > 0 ) {
            // Reset all to false.  Selected months will be set to true below.
            _analyze_month_list = new int[size];
            for ( int i = 0; i < 12; i++ ) {
                _analyze_month[i] = false;
            }
        }
        int imon;
        for ( int i = 0; i < size; i++ ) {
            imon = Integer.parseInt((String)tokens.get(i) );
            if ( (imon >= 1) && (imon <= 12) ) {
                _analyze_month[imon - 1] = true;
            }
            _analyze_month_list[i] = imon;
        }
    }

    _analysis_period_end = (end == null) ? dependentTS.getDate2() : end;

    _analysis_period_start = (start == null) ? dependentTS.getDate1() : start;

    _maxCombinations = maxCombinations == 0 ? 20 : maxCombinations;


}

public PrincipalComponentAnalysis getPrincipalComponentAnalysis() {
    return _pca;
}

/*
 * This routine uses the results from the Principal Component Analysis
 */
public TS fill ( int regressionEqIndex, DateTime fillStart, DateTime fillEnd ) throws Exception
{
    TS tsToFill = (TS) _dependentTS.clone();
    // regressionEqIndex is 1-based, bcomb is 0-based.
    double regressionEquation[] = _pca.getBcombForIndex(regressionEqIndex-1);

    // at this point, _analysis_period_start correlates with the first data value
    // in regressionEquation
    TSData ydata = null;
    double xdata, value, missing = tsToFill.getMissing();
    int numTS = _independentTS.size();
    TSIterator tsi = new TSIterator( _dependentTS, _analysis_period_start, _analysis_period_end );
    int regressionIndex;    // index to regression equation
    int timeIndex=0;        // index to time; used to step through xMatrix

    while ( (ydata=tsi.next()) != null ) {
        if ( ydata.getData() == missing ) {
            // regressionEquation[0] is the intercept.  Start with that.
            value = regressionEquation[0];
            regressionIndex=0;
            for ( int nTS = 0; nTS < numTS; nTS++ ) {
                xdata = _xMatrix[timeIndex][nTS];
                if ( xdata == missing ) {
                    value = missing;
                    // end the loop
                    nTS = numTS;
                } else {
                    if ( regressionEquation[++regressionIndex] != missing ) {
                        value += xdata * regressionEquation[regressionIndex];
                    }
                }
            }
            if ( value != missing ) {
                tsToFill.setDataValue(tsi.getDate(), value);
            }
        }
        timeIndex++;
    }

    return tsToFill;
}

}
