// ----------------------------------------------------------------------------
// JWorksheet_DefaultTableCellEditor - Class that overrides the default 
//	worksheet cell editor.  
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-10-13	J. Thomas Sapienza, RTi	Initial version.
// 2003-10-22	JTS, RTi		* Added getCellEditorValue().
//					* Added stopCellEditing().
//					* Adapted code from the source code
//					  for DefaultCellEditor so this class
//					  works as desired.
// 2003-11-18	JTS, RTi		* Added code to set the edit cell in
//					  the worksheet.
//					* Overrode cancelCellEditing().
//					* Added finalize().
// 2004-02-02	JTS, RTi		Changed the default value that is shown
//					when editing to a blank ("") so that
//					the editor functions more like Excel.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Color;
import java.awt.Component;

import java.lang.reflect.Constructor;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.border.LineBorder;

import javax.swing.table.TableCellEditor;

import RTi.DMI.DMIUtil;

import RTi.Util.IO.IOUtil;

/**
This class replaces the normal cell editor class in the JWorksheet.  It mimics
all the behavior of the original cell editor, but it also gives the ability to
do some nice handling of MISSING values as well as highlighting the cell that
is currently being edited.
*/
public class JWorksheet_DefaultTableCellEditor
extends DefaultCellEditor
implements TableCellEditor {

/**
Array of values used to allow editing of multiple types of classes in one
editor.  From the original java code.
*/
private Class[] __argTypes = new Class[]{String.class};

/**
Object used to do some complicated handling of different data types in the same
editor object.  From the original java code.
*/
private Constructor __constructor;

/**
The worksheet in which this editor was initialized.
*/
private JWorksheet __worksheet;

/**
Used to store the data object that will be returned when editing is successful.
From the original java code.
*/
private Object __editorValue;

/**
Constructor.
*/
public JWorksheet_DefaultTableCellEditor() {
	super(new JTextField());
}

/**
Cancels the cell editing going on in the cell editor.  Overrides method from
DefaultCellEditor and just forwards the call on to the super class.
*/
public void cancelCellEditing() {
	__worksheet.setEditCell(-1, -1);
	super.cancelCellEditing();
}	

/**
Cleans up member variables.
*/
public void finalize() 
throws Throwable {
	IOUtil.nullArray(__argTypes);
	__constructor = null;
	__worksheet = null;
	__editorValue = null;
	super.finalize();
}

/**
Returns a component that is used for editing the data in the cell; from 
TableCellEditor.<p>
Its original javadocs:<br>
Sets an initial <code>value</code> for the editor. This will cause the editor 
to <code>stopEditing</code> and lose any partially edited value if the editor 
is editing when this method is called.<p>
Returns the component that should be added to the client's 
<code>Component</code> hierarchy. Once installed in the client's hierarchy 
this component will then be able to draw and receive user input.
@param table the JTable that is asking the editor to edit; can be null
@param value the value of the cell to be edited; it is up to the specific 
editor to interpret and draw the value. For example, if value is the string 
"true", it could be rendered as a string or it could be rendered as a check 
box that is checked. null is a valid value
@param isSelected true if the cell is to be rendered with highlighting
@param row the row of the cell being edited
@param column the visible column of the cell being edited
@return the component for editing
*/
public Component getTableCellEditorComponent(JTable table, Object value, 
boolean isSelected, int row, int column) {
	int absColumn = ((JWorksheet)table).getAbsoluteColumn(column);

	// The following code was taken from DefaultCellEditor.java ...

	__editorValue = null;
	Class columnClass = table.getColumnClass(absColumn);
	// Since our obligation is to produce a value which is
	// assignable for the required type it is OK to use the
	// String constructor for columns which are declared
	// to contain Objects. A String is an Object.
	if (columnClass == Object.class) {
		columnClass = String.class;
	}
	try {
		__constructor = columnClass.getConstructor(__argTypes);
	}
	catch (Exception e) {
		e.printStackTrace();
		return null;
	}

	__worksheet = (JWorksheet)table;
	__worksheet.setEditCell(row, column);

	// The preceding code was taken from DefaultCellEditor.java ...
	
	boolean setNumericAlignment = false;

	if (columnClass == Double.class) {
		// When editing fields with missing double values, don't put
		// -999.0 into the editor initially -- instead, fill the editor
		// with a blank String.  The cell renderer renders cells with
		// missing values as empty cells, so having a number pop up
		// if the cell is edited could be confusing.
		if (DMIUtil.isMissing(((Double)value).doubleValue())) {
			value = new String("");
		}
		// also, mark this cell as one for which to put the edited
		// value on the right side of the cell (like in Excel).
		setNumericAlignment = true;
	}
	else if (columnClass == Integer.class) {
		// When editing fields with missing integer values, don't put
		// -999 into the editor initially -- instead, fill the editor
		// with a blank String.  The cell renderer renders cells with
		// missing values as empty cells, so having a number pop up
		// if the cell is edited could be confusing.	
		if (DMIUtil.isMissing(((Integer)value).intValue())) {
			value = new String("");
		}
		// also, mark this cell as one for which to put the edited
		// value on the right side of the cell (like in Excel).		
		setNumericAlignment = true;
	}

//	value = "";

	// create the standard editing component for this cell
	JComponent jc = (JComponent)super.getTableCellEditorComponent(
		table, value, isSelected, row, column);
	// set it to have the standard border
//	jc.setBorder(new LineBorder(Color.black));

	// if the alignment of the text in the editor needs adjusted because
	// a number is being edited, do so.
	if (setNumericAlignment) {
		((JTextField)jc).setHorizontalAlignment(JTextField.RIGHT);
	}
	
//	((JTextField)jc).getCaret().setSelectionVisible(true);
//	((JTextField)jc).selectAll();

	// set the border to something slightly more visible
	jc.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
	return (Component)jc;
}

/**
Returns the editor value that was saved after editing was successfully stopped.
This entire method was borrowed from code in JTable.java.
@return the editor value that was saved after editing was successfully stopped.
*/
public Object getCellEditorValue() {
	return __editorValue;
}

/**
Tries to stop the editing that is going on in the cell.  If editing cannot be
stopped (because an invalid value [e.g., 'U7' entered into an Integer field]
is entered), return false and highlight the editor component with a red 
outline.  Otherwise, close the cell editor and the value will be entered into
the table model via the setValueAt() method.  This entire method was borrowed
from code in JTable.java.
@return true if editing was stopped successfully, otherwise false.
*/
public boolean stopCellEditing() {
	String s = (String)super.getCellEditorValue();
	// Here we are dealing with the case where a user
	// has deleted the string value in a cell, possibly
	// after a failed validation. Return null, so that
	// they have the option to replace the value with
	// null or use escape to restore the original.
	// For Strings, return "" for backward compatibility.
	if ("".equals(s)) {
		if (__constructor.getDeclaringClass() == String.class) {
			__editorValue = s;
		}
		boolean result = super.stopCellEditing();
		if (result) {
			__worksheet.setEditCell(-1, -1);			
		}
	}
	
	try {
		__editorValue = __constructor.newInstance(new Object[]{s});
	}
	catch (Exception e) {
		((JComponent)getComponent()).setBorder(
			new LineBorder(Color.red));
		return false;
	}

	__worksheet.setEditCell(-1, -1);
	return super.stopCellEditing();
}

}
