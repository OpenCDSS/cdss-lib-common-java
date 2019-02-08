// TSRegressionResults - store the results of a TSRegressionAnalysis

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
