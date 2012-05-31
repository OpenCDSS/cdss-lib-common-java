package RTi.TS;
import RTi.Util.Math.RegressionEstimateErrors;

/**
Store information about TSRegressionAnalysis estimation errors.  Information for a single relationship as
well as monthly relationships are stored.
*/
public class TSRegressionEstimateErrors
{

/**
Regression errors using a single equation.
*/
RegressionEstimateErrors __singleEquationErrors = null;
/**
Regression errors using monthly equations.
*/
RegressionEstimateErrors [] __monthlyEquationErrors = null;

/**
Constructor
@param singleEquationErrors regression errors for a single equation (may contain a subset of months)
@param monthlyEquationErrors regression errors for each month (may only be complete for a subset of months).
*/
public TSRegressionEstimateErrors ( RegressionEstimateErrors singleEquationErrors, RegressionEstimateErrors [] monthlyEquationErrors )
{
    __singleEquationErrors = singleEquationErrors;
    __monthlyEquationErrors = monthlyEquationErrors;
}

/**
Return a monthly equation regression errors.
@return a monthly equation regression errors.
@param month the month of interest (1-12).
*/
public RegressionEstimateErrors getMonthlyEquationRegressionErrors ( int month )
{
    return __monthlyEquationErrors[month - 1];
}

/**
Return the single equation regression errors.
@return the single equation regression errors.
*/
public RegressionEstimateErrors getSingleEquationRegressionErrors ()
{
    return __singleEquationErrors;
}

}