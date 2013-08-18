package RTi.TS;

import RTi.Util.Math.RegressionResults;

/**
Store the results of a TSRegressionAnalysis.  Information for a single relationship as well as monthly
relationships are stored.
*/
public class TSRegressionResults
{

/**
Regression results using a single equation.
*/
RegressionResults __singleEquationResults = null;
/**
Regression results using monthly equations.
*/
RegressionResults [] __monthlyEquationResults = null;

/**
Constructor
@param singleEquationResults regression results for a single equation (may contain a subset of months)
@param monthlyEquationResults regression results for each month (may only be complete for a subset of months).
*/
public TSRegressionResults ( RegressionResults singleEquationResults, RegressionResults [] monthlyEquationResults )
{
    __singleEquationResults = singleEquationResults;
    __monthlyEquationResults = monthlyEquationResults;
}

/**
Return a monthly equation regression data.
@return a monthly equation regression data.
@param month the month of interest (1-12).
*/
public RegressionResults getMonthlyEquationRegressionResults ( int month )
{
    return __monthlyEquationResults[month - 1];
}

/**
Return the single equation regression data.
@return the single equation regression data.
*/
public RegressionResults getSingleEquationRegressionResults ()
{
    return __singleEquationResults;
}

}