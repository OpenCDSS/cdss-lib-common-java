package RTi.TS;

/**
Immutable data indicating whether regression relationships meet criteria at an individual check and overall
level (all checks must pass).
*/
public class TSRegressionChecks
{
    
/**
Minimum sample size required to demonstrate relationship.
*/
Integer __minimumSampleSize = null;
    
/**
Sample size satisfied for a single equation.
*/
boolean __okSingleSampleSize = false;

/**
Sample size satisfied for a monthly equations.
*/
boolean [] __okMonthlySampleSize = new boolean[12];

/**
Minimum R required to demonstrate relationship.
*/
Double __minimumR = null;

/**
R satisfied for a single equation.
*/
boolean __okSingleR = false;

/**
R satisfied for a monthly equations.
*/
boolean [] __okMonthlyR = new boolean[12];

/**
Confidence interval required to demonstrate relationship.
*/
Double __confidenceInterval = null;

/**
T test satisfied for a single equation.
*/
boolean __okSingleTtest = false;

/**
T test satisfied for a monthly equations.
*/
boolean [] __okMonthlyTtest = new boolean[12];

// TODO SAM 2012-01-14 This might be fragile since it is all booleans and someone could get confused
/**
Constructor.
@param singleEquationData regression data for a single equation (may contain a subset of months)
@param monthlyEquationData regression data for each month (may only be complete for a subset of months).
*/
public TSRegressionChecks ( Integer minimumSampleSize, boolean okSingleSampleSize, boolean [] okMonthlySampleSize,
    Double minimumR, boolean okSingleR, boolean [] okMonthlyR,
    Double confidenceInterval, boolean okSingleTtest, boolean [] okMonthlyTtest )
{   
    __minimumSampleSize = minimumSampleSize;
    __okSingleSampleSize = okSingleSampleSize;
    __okMonthlySampleSize = okMonthlySampleSize;
    __minimumR = minimumR;
    __okSingleR = okSingleR;
    __okMonthlyR = okMonthlyR;
    __confidenceInterval = confidenceInterval;
    __okSingleTtest = okSingleTtest;
    __okMonthlyTtest = okMonthlyTtest;
}

}