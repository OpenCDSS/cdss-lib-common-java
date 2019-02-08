// TSRegressionFilledValues - immutable data to store values filled using TSRegressionAnalysis and related objects.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

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
