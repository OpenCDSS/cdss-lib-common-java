// CheckFile_DataModel - DataModel class to store data objects for the CheckFile

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

/*****************************************************************************
CheckFile_DataModel class - 2007-03-27 - KAT
******************************************************************************
Revisions
2007-03-27	Kurt Tometich, RTi		Initial version.
*****************************************************************************/
package RTi.Util.IO;
import java.util.List;
import java.util.Vector;

/**
DataModel class to store data objects for the CheckFile.
Each DataModel has a title, header, info, total number
of checks and a list of data.
*/
public class CheckFile_DataModel 
{
	private List __data;				// stores data from running checks
	private String __info;				// stores information and suggestions
										// about the latest data check run
	
	private String[] __table_header;	// column headers for the data 
	private String __title;				// title or name of the component
	private int __total_problems;		// total number of product rows that
										// had problems
	private int __total_checked;		// total number of component objects
										// checked on last run
	
	/**
	Initializes a DataModel object for a Check File.
	@param data The list of data to check.
	@param table_header The column headers for the data.
	@param title The title or name of the data being checked (data component).
	@param info
	@param num_problems
	@param total
	*/
	public CheckFile_DataModel( List data, String[] table_header,
	String title, String info, int num_problems, int total )
	{
		// store data from checks
		if ( data != null ) {
			__data = data;
		}
		else {
			__data = new Vector<String []>();
		}
		// store table column headers
		if ( table_header != null ) {
			__table_header = table_header;
		}
		else {
			__table_header = new String[]{};
		}
		// store title or name of component
		if ( title != null ) {
			__title = title;
		}
		else {
			__title = "Data";
		}
		// store info on current data checks
		if ( info != null ) {
			__info = info;
		}
		else {
			__info = "";
		}
		// store the total number of component objects checked
		__total_checked = total;
		__total_problems = num_problems;
	}
	
	/**
	Returns the data list for this model.
	@return list of invalid data.
	*/
	public List getData()
	{
		return __data;
	}
	
	/**
	Returns the size of the data list or number of
	invalid rows.
	@return Size of the data list.
	 */
	public int getDataSize()
	{
		return __data.size();
	}
	
	/**
	Returns the list of table column headers.
	@return List of table headers.
	*/
	public String[] getTableHeader()
	{
		return __table_header;
	}
	
	/**
	Returns the title or name of the data component that
	was checked.
	@return Name or title of the data component.
	*/
	public String getTitle()
	{
		return __title;
	}
	
	/**
	Returns extra information about the current data check
	that was run.  This may include reasons for failure or
	extra information pertaining to the specific data
	component.   
	@return Extra information about the data checks.
	 */
	public String getInfo()
	{
		return __info;
	}
	
	/**
	Returns the total number of data component objects checked.
	@return Number of data component objects that were checked.
	*/
	public int getTotalChecked()
	{
		return __total_checked;
	}
	
	/**
	Returns the total number of data component that have problems.
	@return Number of data component objects that have problems.
	*/
	public int getTotalNumberProblems()
	{
		return __total_problems;
	}
}
