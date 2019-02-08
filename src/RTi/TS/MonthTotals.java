// MonthTotals - simple class for returning time series totals by month

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

// ----------------------------------------------------------------------------
// MonthTotals - simple class for returning time series totals by month
// ----------------------------------------------------------------------------
// History:
//
// 05 Jun 1998	Steven A. Malers, RTi	Initial version.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.TS;

/**
This class stores information about time series by month.  It is returned
by TSUtil.getMonthTotals.  This class may be deprecated in the future (include
in TSLimits?).
*/
public class MonthTotals
{

private int []		_numsummed;
private int []		_nummissing;
private double []	_avgvals;
private double []	_summedvals;

/**
Default constructor.  Initialize to null data.  The arrays must be set by
other code.
*/
public MonthTotals ()
{	initialize ();
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	_numsummed = null;
	_nummissing = null;
	_avgvals = null;
	_summedvals = null;
}

/**
Return the monthly averages.
@return the monthly averages.
*/
public double [] getAverages ()
{	return _avgvals;
}

/**
Return the number of missing for months.
@return the number of missing for months.
*/
public int [] getNumMissing ()
{	return _nummissing;
}

/**
Return the number summed for months.
@return the number summed for months.
*/
public int [] getNumSummed ()
{	return _numsummed;
}

/**
Return the monthly sums.
@return the monthly sums.
*/
public double [] getSums ()
{	return _summedvals;
}

/**
Initialize the data.
*/
private void initialize ()
{	_numsummed = null;
	_nummissing = null;
	_avgvals = null;
	_summedvals = null;
}

/**
Set the monthly averages.
@param avgvals Monthly averages.
*/
public void setAverages ( double [] avgvals )
{	_avgvals = avgvals;
}

/**
Set the monthly number of missing.
@param nummissing Monthly number of missing.
*/
public void setNumMissing ( int [] nummissing )
{	_nummissing = nummissing;
}

/**
Set the monthly number summed.
@param numsummed Monthly number summed.
*/
public void setNumSummed ( int [] numsummed )
{	_numsummed = numsummed;
}

/**
Set the monthly sums.
@param summedvals Monthly sums.
*/
public void setSums ( double [] summedvals )
{	_summedvals = summedvals;
}

} // End of MonthTotals
