// ----------------------------------------------------------------------------
// JWorksheet_Listener - An interface for classes that want to respond to
//	JWorksheet events.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-06-30	J. Thomas Sapienza, RTi	Initial version.
// 2006-03-02	JTS, RTi		* All existing method names now 
//					  preceded by "worksheet".
//					* Added worksheetSelectAllRows() and
//					  worksheetDeselectAllRows().
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

/**
This interface is for classes that want to be able to respond to JWorksheet
events, such as when rows are added or removed.   This class will likely be
renamed in the future.
*/
public interface JWorksheet_Listener {

/**
Called when deselectAllRows() is called on the worksheet.  This method is never
called for any sort of selection done with keyboard or mouse, but <i>only</i>
when the JWorksheet.deselectAllRows() method is called. <p>
In order to capture all selection events, selection events will need to be
bubbled up from the column and row selection models to the worksheet.
@param timeframe when the even occurred.  One of 
JWorksheet.PRE_SELECTION_CHANGE or JWorksheet.POST_SELECTION_CHANGE.
*/
public void worksheetDeselectAllRows(int time);

/**
Called when the worksheet adds a row.  By the time this is called, the 
row has already been added.
@param rowNumber the number of the row to add
*/
public void worksheetRowAdded(int rowNumber);

/**
Called when the worksheet deletes a row.  By the time this is called, the 
row has already been deleted.
@param rowNumber the number of the deleted row.
*/
public void worksheetRowDeleted(int rowNumber);

/**
Called when selectAllRows() is called on the worksheet.  This method is never
called for any sort of selection done with keyboard or mouse, but <i>only</i>
when the JWorksheet.selectAllRows() method is called.  <p>
In order to capture all selection events, selection events will need to be
bubbled up from the column and row selection models to the worksheet.
@param timeframe when the even occurred.  One of 
JWorksheet.PRE_SELECTION_CHANGE or JWorksheet.POST_SELECTION_CHANGE.
*/
public void worksheetSelectAllRows(int timeframe);

/**
Called when the number of rows in the worksheet changes drastically, i.e,
when setData() is called.
@param rows the number of rows in the worksheet.
*/
public void worksheetSetRowCount(int rows);

}
