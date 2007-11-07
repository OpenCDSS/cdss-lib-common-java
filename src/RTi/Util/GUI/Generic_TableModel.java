// ----------------------------------------------------------------------------
// Generic_TableModel - Table Model for a generic worksheet.  Manages 
// a Vector of GenericWorksheetData objects.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-12-10	J. Thomas Sapienza, RTi	Initial version.
// 2005-03-23	JTS, RTi		Removed reference to the 0th (row
//					count) column.
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.util.Date;
import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

import RTi.Util.IO.IOUtil;

/**
This class is a table model for displaying GenericWorksheetData objects in
a worksheet.  Unlike hard-coded table models (which are useful for more 
complicated worksheets), all the settings for this table model, such as 
column names, column formats, etc, can be set at run time.<p>
<b>How to Use a Generic Table Model</b><p>
There are several ways to go about creating a worksheet that displays data 
from GenericWorksheetObjects.  This first method can be used for when 
there is no actual data ready with which to populate the table model:<p>
<pre>
	// create a table model that will have 3 data fields
	Generic_TableModel model = new Generic_TableModel(3);

	// at this point, the class information for each field must be set.
	// There are two ways to go about this.  
	// First (if no GenericWorksheetData objects representative of
	// what will be stored in the worksheet are available):
	if (haveNoRepresentativeObjects) {
		model.setColumnClass(0, String.class);
		model.setColumnClass(1, Integer.class);
		model.setColumnClass(2, Double.class);
	}
	// but if an object representative of the objects that will be
	// stored in the worksheet is available (and has NO null values
	// stored in it), the following can be done:
	else {
		model.determineColumnClasses(representativeGenericData);
	}

	// set up the column information (name, format, etc)
	model.setColumnInformation(0, "String column", "%4.4s", 10, true);
	model.setColumnInformation(1, "Integer column", "%8d", 8, false);
	model.setColumnInformation(2, "Double column", "%10.2f", 12, true);

	// create the cell renderer
	Generic_CellRenderer renderer = new Generic_CellRenderer(model);

	// create the worksheet -- from this point on out, it's the same
	// as working with any other worksheet.
	__worksheet = new JWorksheet(renderer, model, propList);
</pre>
<p>
If, however, there is a Vector of data objects to be placed in the worksheet,
and the first GenericWorksheetData object in the Vector has NO null values,
the following can be done:<p>
<pre>
	// create the table model.  This will automatically determine the 
	// column classes from the first element in the Vector.
	Generic_TableModel model = new Generic_TableModel(vector);

	// set up the column information (name, format, etc)
	model.setColumnInformation(0, "String column", "%4.4s", 10, true);
	model.setColumnInformation(1, "Integer column", "%8d", 8, false);
	model.setColumnInformation(2, "Double column", "%10.2f", 12, true);

	// create the cell renderer
	Generic_CellRenderer renderer = new Generic_CellRenderer(model);

	// create the worksheet -- from this point on out, it's the same
	// as working with any other worksheet.
	__worksheet = new JWorksheet(renderer, model, propList);
</pre>
<p>
<b>Adding a new Row of Data to a Generic Table</b><p>
Adding a new row of data is a little trickier than normal, simply because
the order and classes of the fields in the new GenericWorksheetData object
must match exactly all the others in the worksheet.<p>
The first way to do this is to manually create a new (and empty) data object:<p>
<pre>
	GenericWorksheetData d = new GenericWorksheetData(3);
	d.setValueAt(0, "test");
	d.setValueAt(1, new Integer(111));
	d.setValueAt(2, new Double(999));
	__worksheet.addRow(d);
</pre>
<p>
The alternate way is to use a method in the GenericWorksheetData class to
have it build an empty data object:<p>
<pre>
	// get the first element in the worksheet in order to build a new one
	GenericWorksheetData d =(GenericWorksheetData)__worksheet.getRowData(0);

	GenericWorksheetData newRow = d.getEmptyGenericWorksheetData();
	__worksheet.addRow(newRow);
</pre>
*/
public class Generic_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
Array of whether each column is editable or not.
*/
private boolean[] __editable = null;

/**
Whether any tool tips have been set yet.
*/
private boolean __tipsSet = false;

/**
Array storing the kind of data class for each column.
*/
private Class[] __classes = null;

/**
Array of the column widths for each column.
*/
private int[] __widths = null;

/**
Number of columns in the table model.
*/
private int __COLUMNS = 0;

/**
Array of the formatting information for use by StringUtil.format() for each
column.
*/
private String[] __formats = null;

/**
Array of the names of each column.
*/
private String[] __names = null;

/**
Array of the tool tips for each column.
*/
private String[] __toolTips = null;

/**
Constructor.  
Sets up a table model for the given number of columns.  The
Class for each column <b>must</b> be set with setColumnClass() prior to
putting this model in a worksheet, or a call should be made to
determineColumnClasses(GenericWorksheetData), with a GenericWorksheetData
object that has no null values.
@param columns the number of columns in the table model.
*/
public Generic_TableModel(int columns) {
	__COLUMNS = columns;
	_rows = 0;

	initialize();
}

/**
Constructor.  
Sets up a table model and determines the number of columns from the first 
value in the data Vector.  If the data Vector is empty or null, 
an exception is thrown because the number of columns cannot be determined.<p>
The class type for each field is determined from the Classes of the data in 
the first element of the Vector, so the first element of the data Vector cannot
have any null values.  
@param data the Vector of GenericWorksheetData values to start the worksheet
with.  Must have a size &gt;0 and be non-null.  The first element cannot have
any null values.
@throws Exception if a null or 0-size data Vector is passed in, or if the
first element of the Vector has any null values.
*/
public Generic_TableModel(Vector data) 
throws Exception {
	if (data == null || data.size() == 0) {
		throw new Exception ("Cannot determine number of columns.");
	}
	else {
		GenericWorksheetData d =(GenericWorksheetData)data.elementAt(0);
		__COLUMNS = d.getColumnCount();
		_data = data;
		_rows = data.size();

		initialize();

		determineColumnClasses();
	}
}

/**
Determines the kinds of classes for each column of data from the first element
in the internal data Vector.
*/
public void determineColumnClasses() {
	if (_data.size() == 0) {
		return;
	}

	determineColumnClasses((GenericWorksheetData)_data.elementAt(0));
}

/**
Determines the kinds of classes for each column of data from the specified
GenericWorksheetData object.  It cannot contain any null values.
@param data the GenericWorksheetData object from which to determine the column
classes.
*/
public void determineColumnClasses(GenericWorksheetData data) {
	for (int i = 0; i < __COLUMNS; i++) {
		__classes[i] = (data.getValueAt(i)).getClass();
	}
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__editable = null;
	IOUtil.nullArray(__classes);
	__widths = null;
	IOUtil.nullArray(__formats);
	IOUtil.nullArray(__names);
	IOUtil.nullArray(__toolTips);
	super.finalize();
}

/**
Returns the class of the data stored in a given column.
@param column the column for which to return the data class.
*/
public Class getColumnClass (int column) {
	return __classes[column];
}

/**
Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
Returns the name of the column at the given position.
@return the name of the column at the given position.
*/
public String getColumnName(int column) {
	return __names[column];
}

/**
Returns the tool tips for the columns, or null if none have been set.
@return the tool tips for the columns, or null if none have been set.
*/
public String[] getColumnToolTips() {
	if (!__tipsSet) {
		return null;
	}
	for (int i = 0; i < __COLUMNS; i++) {
		if (__toolTips[i] != null) {
			return __toolTips;
		}
	}
	return null;
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	return __widths;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the
column.
*/
public String getFormat(int column) {
	return __formats[column];
}

/**
Returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns the data that should be placed in the worksheet at the given row and 
column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the worksheet at the given row and 
column.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	GenericWorksheetData d = (GenericWorksheetData)_data.elementAt(row);
	return d.getValueAt(col);
}

/**
Initializes the data arrays.  
*/
private void initialize() {
	__classes = new Class[__COLUMNS];
	__names = new String[__COLUMNS];

	for (int i = 0; i < __COLUMNS; i++) {
		__names[i] = "COLUMN " + i;
	}
	
	__formats = new String[__COLUMNS];

	for (int i = 0; i < __COLUMNS; i++) {
		__formats[i] = "";
	}	
	
	__toolTips = new String[__COLUMNS];
	__widths = new int[__COLUMNS];

	for (int i = 0; i < __COLUMNS; i++) {
		__widths[i] = 8;
	}		

	__editable = new boolean[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		__editable[i] = false;
	}
}

/**
Returns whether the specified cell is editable or not.
@return whether the specified cell is editable or not.
*/
public boolean isCellEditable(int row, int col) {
	return __editable[col];
}

/**
Sets the column class for the specified column.
@param column the column for which to set the class.  Cannot change the class
of column 0.
@param c the Class to set for the column.
*/
public void setColumnClass(int column, Class c) {
	__classes[column] = c;
}

/**
Sets whether the specified column is editable or not.
@param column the column to set editable.  Cannot change the editability of 
column 0.
@param editable whether the column is editable (true) or not.
*/
public void setColumnEditable(int column, boolean editable) {
	__editable[column] = editable;
}

/**
Sets the format (as used by StringUtil.format()) for the specified column.
@param column the column to set the editability of.  Cannot change the format
of column 0.
@param format the format to set for the column.
*/
public void setColumnFormat(int column, String format) {
	__formats[column] = format;
}

/**
Sets the column name, format, and width at once.
@param column the column to set the values for.
@param name the name to give the column.
@param format the format to use in the column.
@param width the width to set the column to.
*/
public void setColumnInformation(int column, String name, String format, 
int width) {
	setColumnName(column, name);
	setColumnFormat(column, format);
	setColumnWidth(column, width);
}

/**
Sets the column name, format, width, and tool tip at once.
@param column the column to set the values for.
@param name the name to give the column.
@param format the format to use in the column.
@param width the width to set the column to.
@param toolTip the toolTip to set on the column.
*/
public void setColumnInformation(int column, String name, String format, 
int width, String toolTip) {
	setColumnName(column, name);
	setColumnFormat(column, format);
	setColumnWidth(column, width);
	setColumnToolTip(column, toolTip);
}

/**
Sets the column name, format, width and editability at once.
@param column the column to set the values for.
@param name the name to give the column.
@param format the format to use in the column.
@param width the width to set the column to.
@param editable whether the column is editable or not.
*/
public void setColumnInformation(int column, String name, String format, 
int width, boolean editable) {
	setColumnName(column, name);
	setColumnFormat(column, format);
	setColumnWidth(column, width);
	setColumnEditable(column, editable);
}

/**
Sets the column name, format, width, tool tip and editability at once.
@param column the column to set the values for.
@param name the name to give the column.
@param format the format to use in the column.
@param width the width to set the column to.
@param toolTip the toolTip to set on the column.
@param editable whether the column is editable or not.
*/
public void setColumnInformation(int column, String name, String format, 
int width, String toolTip, boolean editable) {
	setColumnName(column, name);
	setColumnFormat(column, format);
	setColumnWidth(column, width);
	setColumnToolTip(column, toolTip);
	setColumnEditable(column, editable);	
}

/**
Sets the name of the specified column.
@param column the column to set the name for.
@param name the name to set for the column.
*/
public void setColumnName(int column, String name) {
	__names[column] = name;
}

/**
Sets the tool tip on the specified column.
@param column the column to set the tool tip for.
@param tip the tool tip to set on the column.
*/
public void setColumnToolTip(int column, String tip) {
	__toolTips[column] = tip;	
	__tipsSet = true;
}

/**
Sets the width of the specified column.
@param column the column the set the width for.
@param width the width to set on the column.
*/
public void setColumnWidth(int column, int width) {
	if (width < 0) {
		return;
	}
	__widths[column] = width;
}

/**
Sets the value in the table model data at the specified position.
@param value the value to set.
@param row the row of data in which to set the value.
@param col the column of data in which to set the value.
*/
public void setValueAt(Object value, int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	GenericWorksheetData d = (GenericWorksheetData)_data.elementAt(row);
	
	if (value == null) {
		Class c = __classes[col];
		if (c == String.class) {
			value = DMIUtil.MISSING_STRING;
		}
		else if (c == Double.class) {
			value = new Double(DMIUtil.MISSING_DOUBLE);
		}
		else if (c == Integer.class) {
			value = new Integer(DMIUtil.MISSING_INT);
		}
		else if (c == Date.class) {
			value = DMIUtil.MISSING_DATE;
		}
	}
	
	d.setValueAt(col, value);

	super.setValueAt(value, row, col);
}

}
