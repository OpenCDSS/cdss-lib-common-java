package RTi.TS;

import RTi.Util.Math.MathUtil;
import RTi.Util.Math.RegressionData;

/**
Immutable data used as input to a TSRegressionAnalysis.  Data for a single relationship as well as monthly
relationships are stored.
*/
public class TSRegressionData
{

/**
Dependent time series.
*/
TS __dependentTS = null;

/**
Independent time series.
*/
TS __independentTS = null;

/**
Regression data using a single equation.
*/
RegressionData __singleEquationData = null;

/**
Regression data using monthly equations.
*/
RegressionData [] __monthlyEquationData = null;

/**
Constructor.
@param singleEquationData regression data for a single equation (may contain a subset of months)
@param monthlyEquationData regression data for each month (may only be complete for a subset of months).
*/
public TSRegressionData ( TS independentTS, TS dependentTS,
    RegressionData singleEquationData, RegressionData [] monthlyEquationData )
{
    __independentTS = independentTS;
    __dependentTS = dependentTS;
    __singleEquationData = singleEquationData;
    __monthlyEquationData = monthlyEquationData;
}

/**
Return the dependent (Y) time series.
@return the dependent (Y) time series.
*/
public TS getDependentTS()
{   return __dependentTS;
}

/**
Return the independent (X) time series.
@return the independent (X) time series.
*/
public TS getIndependentTS()
{   return __independentTS;
}

/**
Return a monthly equation regression data.
@return a monthly equation regression data.
@param month the month of interest (1-12).
*/
public RegressionData getMonthlyEquationRegressionData ( int month )
{
    return __monthlyEquationData[month - 1];
}

/**
Return the single equation regression data.
@return the single equation regression data.
*/
public RegressionData getSingleEquationRegressionData ()
{
    return __singleEquationData;
}

/**
Return a new copy of this instance where all of the data arrays have been transformed by log10.
@param leZeroLog10 the value to assign when the input data are <= 0 (e.g., .001 or Double.NaN).
@param monthly whether or not monthly equations are being used (so we don't transform data that we aren't using).
@param single whether or not a single equation is being used (so we don't transform data that we aren't using).
*/
public TSRegressionData transformLog10 ( double leZeroLog10, boolean monthly, boolean single )
{
	//initialize as empty and add data if it is needed
	RegressionData singleEquationDataTransformed = null;
    RegressionData [] monthlyEquationDataTransformed = new RegressionData[12];
    if (monthly) {
    	//transform monthly data
    	for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
    		double [] x1Transformed = MathUtil.log10 ( getMonthlyEquationRegressionData(iMonth).getX1(), leZeroLog10 );
    		double [] y1Transformed = MathUtil.log10 ( getMonthlyEquationRegressionData(iMonth).getY1(), leZeroLog10 );
    		double [] x2Transformed = MathUtil.log10 ( getMonthlyEquationRegressionData(iMonth).getX2(), leZeroLog10 );
    		double [] y3Transformed = MathUtil.log10 ( getMonthlyEquationRegressionData(iMonth).getY3(), leZeroLog10 );
    		monthlyEquationDataTransformed[iMonth - 1] =
    			new RegressionData(x1Transformed, y1Transformed, x2Transformed, y3Transformed);
    	}
    }
    if (single) {
    	//transform single data
    	double [] x1Transformed = MathUtil.log10 ( getSingleEquationRegressionData().getX1(), leZeroLog10 );
    	double [] y1Transformed = MathUtil.log10 ( getSingleEquationRegressionData().getY1(), leZeroLog10 );
    	double [] x2Transformed = MathUtil.log10 ( getSingleEquationRegressionData().getX2(), leZeroLog10 );
    	double [] y3Transformed = MathUtil.log10 ( getSingleEquationRegressionData().getY3(), leZeroLog10 );
    	singleEquationDataTransformed =
    		new RegressionData(x1Transformed, y1Transformed, x2Transformed, y3Transformed);
    }
    return new TSRegressionData ( getIndependentTS(), getDependentTS(), singleEquationDataTransformed,
        monthlyEquationDataTransformed );
}

}