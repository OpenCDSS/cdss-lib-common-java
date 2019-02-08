// TSRegressionChecks - Immutable data indicating whether regression relationships meet
// criteria at an individual check and overall level (all checks must pass).

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
