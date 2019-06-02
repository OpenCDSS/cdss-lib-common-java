// DataSet_ComponentDataCheck - base class for checking data on components

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

/******************************************************************************
* File: ComponentDataCheck.java
* Author: KAT
* Date: 2007-03-15
*******************************************************************************
* Revisions 
*******************************************************************************
* 2007-03-15	Kurt Tometich	Initial version.
******************************************************************************/
package RTi.Util.IO;

import java.util.List;

import RTi.TS.TS;

/**
* Base class for checking data on components.  Only shared components methods 
* should be added to this class.  This is envisioned to be inherited by other
* products such as StateMod or StateCU.  Then each product can implement their
* own methods that are specific to that product and still benefit from the
* shared methods stored here. 
 */
public class DataSet_ComponentDataCheck
{
	public CheckFile __check_file;	// keeps track of all data checks
	public int __numMissingValues = 0;	// keeps track of missing values
/**
Constructor that initializes the check file and component type.
@param int component - Component type.
@param CheckFile file - Check file that stores all data check information. 
 */
public DataSet_ComponentDataCheck( int component, CheckFile file )
{
		__check_file = file;
}
	
/**
Performs checks for Time Series data.
@param ts list of time series objects.
 */
public void checkTSData( List<TS> ts ) 
{
	// TODO KAT 2007-04-02
	// add ts data checks here
}

/**
 Formats a data string that has been found to be invalid based on
 certain rules.  This method adds a font tag with the color set to
 red so that when the html is viewed the invalid data sticks out.
 If the data is empty then a special character is added since to
 format the data there must be some type of character present.
 * @param data
 * @return
 */
public String createHTMLErrorTooltip( Status status, Object data )
{
	String rval = "";
	String data_str = "";
	// check to see if value is missing
	if ( data == null ) {
		data_str = "?";
	}
	else {
		data_str = data.toString();
		if ( data_str.length() < 1 ) {
			data_str = "?";
		}
	}
	// Add identifier strings to add an HTML tooltip with the status
	// of the validation run.
	rval = "%tooltip_start" + status.toString() + "%tooltip_end" +
	"%font_red" + data_str + "%font_end";
	return rval;
}

}