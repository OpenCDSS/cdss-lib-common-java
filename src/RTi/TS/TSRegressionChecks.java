package RTi.TS;

import RTi.Util.Math.RegressionChecks;

/**
Immutable data indicating whether regression relationships meet criteria at an individual check and overall
level (all checks must pass).
*/
public class TSRegressionChecks
{
    
/**
Regression checks using a single equation.
*/
RegressionChecks __singleEquationChecks = null;

/**
Regression data using monthly equations.
*/
RegressionChecks [] __monthlyEquationChecks = null;

/**
Constructor.
@param singleEquationChecks regression data for a single equation (may contain a subset of months)
@param monthlyEquationChecks regression data for each month (may only be complete for a subset of months).
*/
public TSRegressionChecks ( RegressionChecks singleEquationChecks, RegressionChecks [] monthlyEquationChecks )
{   
    __singleEquationChecks = singleEquationChecks;
    __monthlyEquationChecks = monthlyEquationChecks;
}

/**
Return a monthly equation regression checks.
@return a monthly equation regression checks.
@param month the month of interest (1-12).
*/
public RegressionChecks getMonthlyEquationRegressionChecks ( int month )
{
    return __monthlyEquationChecks[month - 1];
}

/**
Return the single equation regression checks.
@return the single equation regression checks.
*/
public RegressionChecks getSingleEquationRegressionChecks ()
{
    return __singleEquationChecks;
}

}