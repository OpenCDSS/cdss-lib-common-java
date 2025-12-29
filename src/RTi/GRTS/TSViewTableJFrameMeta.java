// TSViewTableJFrameMeta - metadata for TSViewTableJFrame

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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
package RTi.GRTS;

/**
 * This class stores metadata for the TSViewTableJFrame,
 * including the number of worksheets with data for various intervals.
 * The data values are protected for direct access by the TSViewTableJFrame class.
 */
public class TSViewTableJFrameMeta {

	/**
	The number of worksheets in the minute panel.
	*/
	protected int numWorksheetsInMinutePanel = 0;

	/**
	The number of worksheets in the hour panel.
	*/
	protected int numWorksheetsInHourPanel = 0;
	
	/**
	The number of worksheets in the day panel.
	*/
	protected int numWorksheetsInDayPanel = 0;
	
	/**
	The number of worksheets in the month panel.
	*/
	protected int numWorksheetsInMonthPanel = 0;
	
	/**
	The number of worksheets in the year panel.
	*/
	protected int numWorksheetsInYearPanel = 0;
	
	/**
	The number of worksheets in the irregular second panel.
	*/
	protected int numWorksheetsInIrregularSecondPanel = 0;

	/**
	The number of worksheets in the irregular minute panel.
	*/
	protected int numWorksheetsInIrregularMinutePanel = 0;
	
	/**
	The number of worksheets in the irregular hour panel.
	*/
	protected int numWorksheetsInIrregularHourPanel = 0;
	
	/**
	The number of worksheets in the irregular day panel.
	*/
	protected int numWorksheetsInIrregularDayPanel = 0;
	
	/**
	The number of worksheets in the irregular month panel.
	*/
	protected int numWorksheetsInIrregularMonthPanel = 0;
	
	/**
	The number of worksheets in the irregular year panel.
	*/
	protected int numWorksheetsInIrregularYearPanel = 0;
	
	/**
	The number of total visible worksheets.
	*/
	protected int numWorksheetsTotalVisible = 0;

	/**
	The number of total panels with visible worksheets.
	*/
	protected int numPanelsWithVisibleWorksheets = 0;

	/**
	 * Constructor.
	 */
	public TSViewTableJFrameMeta () {
	}

}