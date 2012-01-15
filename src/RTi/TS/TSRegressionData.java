package RTi.TS;

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

}