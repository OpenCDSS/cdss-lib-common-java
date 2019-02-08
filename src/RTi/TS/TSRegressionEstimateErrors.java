// TSRegressionEstimateErrors - store information about TSRegressionAnalysis estimation errors.

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
