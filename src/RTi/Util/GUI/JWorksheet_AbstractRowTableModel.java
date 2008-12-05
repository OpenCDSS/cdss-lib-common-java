// ----------------------------------------------------------------------------
// JWorksheet - Class from which all the Table Models 
// 	that will be used in a JWorksheet and which will have individual 
//	data objects in each row should be built.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-03-06	J. Thomas Sapienza, RTi	Initial version.
// 2003-03-20	JTS, RTi		Renamed to JWorksheet_RowTableModel and
//					now extends 
//					JWorksheet_AbstractTableModel
// 2003-05-20	JTS, RTi		Added code so that getRowData returns
//					the proper row of data even if the 
//					data is sorted.
// 2003-07-23	JTS, RTi		Renamed to 
//					JWorksheet_AbstractRowTableModel
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

/**
This is the class from which all the classes that will be used as RowTableModels
in a JWorksheet, and which will have individual data objects in each row should
be built.It implements a method to return the data stored at a given row.
<P>
TODO (JTS - 2006-05-25)
If I could do this over, I would combine this table model with 
AbstractTableModel, in order to simplify things.  I don't see a very good reason
to require both of these, honestly.
*/
public abstract class JWorksheet_AbstractRowTableModel extends JWorksheet_AbstractTableModel {

/**
Returns the Object stored in the Table Model data at the given position, or
null if the given row is out of the range of the rows.
@param row the row for which to return data.
@return the Object stored in the _data Vector at the given position.
*/
public Object getRowData(int row) {
	if (row > _rows || row < 0) {
		return null;
	}

	if (_sortOrder == null) {
		return _data.get(row);
	}
	else {
		int realRow = _sortOrder[row];
		return _data.get(realRow);
	}
}

}