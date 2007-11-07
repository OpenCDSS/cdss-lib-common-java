// ----------------------------------------------------------------------------
// DMITableModel.java - Table model for displaying data from the DMI in a 
// 	JTable
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2002-05-??i	J. Thomas Sapienza, RTi	Initial Version
// 2002-07-02	JTS, RTi		Changed the private member variables
//					from being final.  
//					Added the remove column method. 
//					DMITableModel also can now store a 
//					Vector of the tables used in 
//					generating it.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI;

import javax.swing.table.AbstractTableModel;

import java.util.Vector;

/**
This class is a table model that allows data from a query to be easily 
displayed in a JTable.
REVISIT (JTS - 2006-05-22)
I don't think this class is necessary anymore.  It has probably not been used
in 4 years and should be removed.
*/
public class DMITableModel 
extends AbstractTableModel {

/**
The number of columns in the table
*/
private int colCount;

/**
The number of rows in the table
*/
private int rowCount;

/**
Vector to hold the data to be displayed in the table
*/
private Vector data;

/**
Vector to hold the names of the columns in the table
*/
private Vector names;

/**
Vector to hold the name(s) of the table(s) in the table
*/
private Vector tableNames;

/**
Constructs a new DMITableModel with the given values and columns.  
@param values A vector of vectors that contains all the values to be
displayed by the JTable that uses this Table Model.  Each vector contained
inside this vector should contain objects that can be easily displayed in
a JTable. 
@param colNames A vector of strings containing the names of the columns
to be shown by this table model
*/
public DMITableModel(Vector values, Vector colNames) {
	data = values;
	names = colNames;

	// The values vector *could* be an empty vector.  This might happen
	// when no values were returned from a query, for instance.  The 
	// column names should still be shown, but with a blank row.  
	// So a vector of blank values is initialized so that the JTable will
	// at least show the column names.
	if (values.size() == 0) {
		Vector v = new Vector(names.size());
		for (int i = 0; i < names.size(); i++) {
			v.addElement("");
		}
		data.addElement(v);
		rowCount = 1;
		colCount = names.size();
	} else {			
		Vector v = (Vector)values.elementAt(0);
		rowCount = values.size();
		colCount = v.size();
	}
}

/**
Initializes an empty table model.  When this table model is used as the 
model for a JTable, the JTable will be empty.
*/
public DMITableModel() {
	data = null;
	names = null;
	tableNames = null;
	rowCount = 0;
	colCount = 0;
}

/**
Calculates how wide a column should be displayed at.  It sizes the column
to display the largest value held in the column, or the column name (if
that is larger than the largest value)
*/
public int calculateWidth(int col) {
	int width = ((getColumnName(col).length()) * 10) + 10;

	for (int i = 0; i < rowCount; i++) {
		String s = "" + getValueAt(i, col);
		System.out.println("s: '" + s + "' (" + s.length() + ")");	
		int w = s.length();
		w = (w * 10) + 10;
		if (w > width) {
			width = w;
		}
	}
	return (width);
}

/**
Returns the kind of field in a specified column
@param c the column number for which to get the class
@return the class of the specified column
*/
public Class getColumnClass(int c) {
	return(getValueAt(0, c).getClass());
}

/**
Returns the number of columns stored in the table model
@return the number of columns stored in the table model
*/
public int getColumnCount() {
	return(colCount);
}

/**
Returns the name of the specified column
@param column the column for which to return the name
@return the name of the specified column
*/
public String getColumnName(int column) {
	return((String)names.elementAt(column));
}

/**
Returns the number of rows in the table model
@return the number of rows in the table model
*/
public int getRowCount() {
	return(rowCount);
}

/**
Returns the names of the tables "involved" in the table model
@return the names of the tables "involved" in the table model
*/
public Vector getTableNames() {
	return tableNames;
}

/**
Returns the object stored at the specified position
@param row the row in which the object is located
@param col the column in which the object is located
@return the object stored at the specified position
*/
public Object getValueAt(int row, int col) {
	Vector v = (Vector)data.elementAt(row);
	if (v.elementAt(col) == null) {
		return "";
	} else {
		return v.elementAt(col);
	}
}

/**
Removes a column and all its associated data from the table model
@param columnName the name of the column to be removed
*/
public void removeColumn(String columnName) throws Exception {
	int columnNum = -1;

	for (int i = 0; i < colCount; i++) {
		String s = (String)names.elementAt(i);
		if (s.equalsIgnoreCase(columnName)) {
			columnNum = i;
		}
	}

	if (columnNum == -1) {
		throw new Exception("Column '" + columnName + 
			"' not found in table model.");
	}

	for (int i = 0; i < rowCount; i++) {
		((Vector)data.elementAt(i)).removeElementAt(columnNum);
	}

	colCount--;
	names.removeElementAt(columnNum);
}

/**
Removes a column and all its associated data from the table model
@param columnNum the number of the column to remove
*/
public void removeColumn(int columnNum) throws Exception {
	removeColumn(getColumnName(columnNum));
}

/**
Sets the vector containing the names of the tables "involved" in the table
model
@param tables a String vector of the names of the tables in the table model
*/
public void setTableNames(Vector tables) {
	tableNames = tables;
}

}
