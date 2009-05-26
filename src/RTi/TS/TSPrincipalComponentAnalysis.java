/*
 *
 */

package RTi.TS;

import RTi.Util.Math.MathUtil;
import RTi.Util.Math.PrincipalComponentAnalysis;
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
    double [][] xMatrix = null;
    double [] yArray = null;
    double [] xArray = null;

    int numTS = _independentTS.size();
    boolean includeAnalyzeMonthList = ((_analyze_month_list == null) || (_analyze_month_list.length == 0)) ?
        false : true;

    // Get data specified or all data if analysis months weren't specified
    yArray = includeAnalyzeMonthList ?
        TSUtil.toArray ( _dependentTS, _analysis_period_start, _analysis_period_end, _analyze_month_list ) :
        TSUtil.toArray ( _dependentTS, _analysis_period_start, _analysis_period_end );

    // Save independent observations in xMatrix[nObservations][numTS]
    int nObservations = yArray.length;
    // now we know number of stations and number of observations
    xMatrix = new double[nObservations][numTS];

    // fill all data into xMatrix
    for ( int nTS = 0; nTS < numTS; nTS++ ) {
        TS ts = _independentTS.get(nTS);
        xArray = includeAnalyzeMonthList ?
            TSUtil.toArray ( ts, _analysis_period_start, _analysis_period_end, _analyze_month_list ) :
            TSUtil.toArray ( ts, _analysis_period_start, _analysis_period_end );
        for ( int i=0; i<nObservations; i++)
            xMatrix[i][nTS] = xArray[i];
    }

    _pca = MathUtil.performPrincipalComponentAnalysis(yArray, xMatrix,
            _dependentTS.getMissing(), _independentTS.get(0).getMissing(), _maxCombinations );
}

private void initialize(TS dependentTS, List<TS> independentTS, DateTime start, DateTime end,
        int maxCombinations, String analysisMonths ) throws Exception {

    _dependentTS = dependentTS;
    _independentTS = independentTS;

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

    _analysis_period_end = (end == null) ? dependentTS.getDate1() : end;

    _analysis_period_start = (start == null) ? dependentTS.getDate1() : start;

    _maxCombinations = maxCombinations == 0 ? 20 : maxCombinations;


}

public PrincipalComponentAnalysis getPrincipalComponentAnalysis() {
    return _pca;
}

}
