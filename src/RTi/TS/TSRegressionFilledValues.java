package RTi.TS;

import RTi.Util.Math.MathUtil;
import RTi.Util.Math.RegressionFilledValues;

/**
Immutable data to store values filled using TSRegressionAnalysis and related objects.
Data for a single relationship as well as monthly relationships are stored.
*/
public class TSRegressionFilledValues
{

/**
Dependent time series.
*/
TS __dependentTS = null;

/**
Filled values using a single equation.
*/
RegressionFilledValues __singleEquationFilledValues = null;

/**
Filled values using monthly equations.
*/
RegressionFilledValues [] __monthlyEquationFilledValues = null;

/**
Constructor.
@param singleEquationFilledValues regression filled values for a single equation (may contain a subset of months)
@param monthlyEquationFilledValues regression filled values for each month (may only be complete for a subset of months).
*/
public TSRegressionFilledValues ( TS dependentTS,
    RegressionFilledValues singleEquationFilledValues, RegressionFilledValues [] monthlyEquationFilledValues )
{
    __dependentTS = dependentTS;
    __singleEquationFilledValues = singleEquationFilledValues;
    __monthlyEquationFilledValues = monthlyEquationFilledValues;
}

/**
Return the dependent (Y) time series.
@return the dependent (Y) time series.
*/
public TS getDependentTS()
{   return __dependentTS;
}

/**
Return a monthly equation regression filled values.
@return a monthly equation regression filled values.
@param month the month of interest (1-12).
*/
public RegressionFilledValues getMonthlyEquationRegressionFilledValues ( int month )
{
    return __monthlyEquationFilledValues[month - 1];
}

/**
Return the single equation regression filled values.
@return the single equation regression filled values.
*/
public RegressionFilledValues getSingleEquationRegressionFilledValues ()
{
    return __singleEquationFilledValues;
}

/**
Return a new copy of this instance where all of the data arrays have been untransformed from log10.
*/
public TSRegressionFilledValues untransformLog10 ()
{
    double [] y = MathUtil.exp ( getSingleEquationRegressionFilledValues().getYFilled() );
    RegressionFilledValues singleEquationDataTransformed = new RegressionFilledValues(y);
    RegressionFilledValues [] monthlyEquationDataTransformed = new RegressionFilledValues[12];
    for ( int iMonth = 1; iMonth <= 12; iMonth++ ) {
        y = MathUtil.exp ( getMonthlyEquationRegressionFilledValues(iMonth).getYFilled() );
        monthlyEquationDataTransformed[iMonth - 1] = new RegressionFilledValues(y);
    }
    return new TSRegressionFilledValues ( getDependentTS(), singleEquationDataTransformed,
        monthlyEquationDataTransformed );
}

}