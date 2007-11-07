// ----------------------------------------------------------------------------
// JWorksheet_TableModelListener - An interface for classes that want to 
//	respond to JWorksheet table model events.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-10-277	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

/**
This interface is for classes that want to be able to respond to JWorksheet
table model events, such as when data are changed 
*/
public interface JWorksheet_TableModelListener {

/**
Called when a data value is changed.
@param rowNumber the number of the row in which data were changed
@param colNumber the number of the column in which data were changed
@param value the new value.
*/
public void tableModelValueChanged(int rowNumber, int colNumber, Object value);

}
