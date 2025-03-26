// JWorksheet_JComboBoxCellEditor - class that implements a specialized cell editor
// for columns that need to insert data with either JComboBoxes or text fields or both,
// in different cells in the same column

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

package RTi.Util.GUI;

import java.awt.Color;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.border.LineBorder;

import javax.swing.event.CellEditorListener;

import javax.swing.table.TableCellEditor;

/**
This class implements a specialized cell editor for columns that need to
allow editing of data via JComboBoxes, JTextFields, or both.  <p>
This class is a fairly complex amalgamation of methods from many different
table-related classes, and some of the code is adapted from the original java code.<p>
Some background:<br>
The JTable does not normally allow different editors to be used on
separate cells with in a column.  Cell editors and renderers are assigned
on a column-by-column by basis.  The need was expressed for RTi to be able
to mix and match editors within a single column.  This class fulfills that need.<p>
Using this class, JComboBoxes (which could eventually be moved to
SimpleJComboBoxes so that the values are selectable <i>and</i> editable)
can be used to select values for entry into a JTable field, or JTextfields
can be used for typing in values for data entry, in exactly the fashion in
which the JTable's editing normally works.<p>
Because the cell editing is normally handled column-by-column, this class
has do some data handling (e.g., it has to know the number of rows
currently in the table) that seems extraneous, but this is in order to
simulate a different editors on different rows.<p>
Because different cells can use different data models for their JComboBoxes,
this class also does some semi-intelligent caching of data models.  For
example:<br>
A table has <b>10</b> rows and the even rows have one data model and the
odd rows have another.  The even rows' combo boxes show a list of color names,
and the odd rows show a list of states. <p>
Instead of storing 10 different data models, one for each cell, when a cell
is assigned a data model, this class will check to see if any other classes
are using the same data model.  If so, they will both share the same
data model (and reduce the memory footprint of this class).
*/
@SuppressWarnings("serial")
public class JWorksheet_JComboBoxCellEditor
extends AbstractCellEditor
implements JWorksheet_Listener, ActionListener, TableCellEditor {

/**
Whether the SimpleJComboBox for the column should be editable.
REVISIT (JTS - 2003-07-08)
Probably expand this so in the future individual cells can have editable
combo boxes or not.
*/
private boolean __editable = false;

/**
If this is set to true, then when a new row is added to the table (and this
is notified), the next row's data model for this column will be the same
as the data model for the row immediately above it.
*/
private boolean __previousRowCopy = false;

/**
Borrowed from JTable's implementation of some code.  Used to generically
refer to constructors for data that could appear in this column.
*/
private Constructor __constructor;

/**
The number of rows that are being managed by this editor.  If a table has
more rows than this class knows about, the rows with row numbers &gr;
__size will be handled with normal JTable editing text fields.
*/
private int __size = 0;

/**
The last SimpleJComboBox that was created by a call to
getTableCellEditorComponent().
*/
private SimpleJComboBox __lastJCB = null;

/**
The last JTextField that was created by a call to getTableCellEditorComponent().
*/
private JTextField __lastJTF = null;

/**
Borrowed from JTable code.  Used to refer back to the data value being edited.
*/
private Object __value;

/**
This list contains all the different data models being used in the table column.
*/
private List __models = null;

/**
This list maps row numbers to the data model number that they are using.
More than row can use the same data model.
There should be one entry in this Vector for every row in the table.
*/
private List __rowToModel = null;

/**
Constructor.
@param worksheet the worksheet in which this class will be used.
@param rows the number of the rows in the table when this cell editor was applied.
@param editable whether the SimpleJComboBox for this column should be editable.
*/
public JWorksheet_JComboBoxCellEditor(JWorksheet worksheet, int rows,
boolean editable) {
	worksheetSetRowCount(rows);
	addCellEditorListener(worksheet);
	__editable = editable;
}

/**
Responds to action events on the JComponents used for editing.  Calls stopCellEditing().
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	stopCellEditing();
}


/**
Adds a cell editor listener; from AbstractCellEditor.
@param l the cell editor to add.
*/
public void addCellEditorListener(CellEditorListener l) {
	super.addCellEditorListener(l);
}

/**
Stops any editing; from AbstractCellEditor.  Calls fireEditingCanceled().
*/
public void cancelCellEditing() {
	fireEditingCanceled();
}

/**
Counts the number of rows that are referring to the model stored in the __models List at the given position.
This is used to determine when a data model can be removed from the __models List
(i.e., when there are no references to it in the __rowToModel Vector).
@param modelNum the modelNum for which to see how many rows use it as their data model
@return the number of rows that use the specified model as their model.
*/
private int countModelUse(int modelNum) {
	int size = __rowToModel.size();

	int count = 0;
	int j = -1;
	for (int i = 0; i < size; i++) {
		j = ((Integer)__rowToModel.get(i)).intValue();
		if (modelNum == j) {
			count++;
		}
	}
	return count;
}

/**
Looks through the models already set in this class for various and checks to see if the specified row matches any of them.
If the specified one matches, the model number (the element of the model in __models) will be return.
Otherwise, -1 is returned.
@param v a Vector of values (SimpleJComboBox data model) to see if is already present in the __models List.
@return -1 if the model cannot be found, or the model number of the model in the __models List if it matched.
*/
private int findModelInModels(List v) {
	int size = __models.size();

	for (int i = 0; i < size; i++) {
		if (v.equals((List)__models.get(i))) {
			return i;
		}
	}
	return -1;
}

/**
Gets the value of the JComponent cell editor as an Object; from CellEditor.
@return the value of the JComponent cell editor.
*/
public Object getCellEditorValue() {
	if (__lastJCB != null) {
		return __lastJCB.getSelectedItem();
	}
	else if (__lastJTF != null) {
		return __value;
	}
	return null;
}

/**
Returns the value of the JComponent cell editor as a String.  For JComboBoxes,
this method is the same as getCellEditorValue().  For JTextFields, however,
instead of returning __value, the actual text in the textfield is returned.
*/
public Object getCellEditorValueString() {
	if (__lastJCB != null) {
		return __lastJCB.getSelectedItem();
	}
	else if (__lastJTF != null) {
		return __lastJTF.getText();
	}
	return null;
}

/**
Returns the combox box data model stored at the specific row.
@param row the row to return the data model for.
@return null if the row doesn't use a combo box, or the Vector of values stored
in the combo box if it does.
*/
public List getJComboBoxModel(int row) {
	Integer I = (Integer)__rowToModel.get(row);
	if (I.intValue() == -1) {
		return null;
	}
	return (List)(__models.get(I.intValue()));
}

/**
Returns whether a new row added to the JWorksheet should use the same
data model for the SimpleJComboBox as the one immediately preceding it.
@return true if the new row should use the data model of the row above it.
*/
public boolean getPreviousRowCopy() {
	return __previousRowCopy;
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
@param column the column of the cell being edited
@return the component for editing
*/
public Component getTableCellEditorComponent(JTable table, Object value,
boolean isSelected, int row, int column) {
	// Get the number in the data model vector of the data model used by this row.
	// If the number is -1, then the row will be edited with a JTextField.
	int modelNum = -1;
	if (row < __size) {
		modelNum = ((Integer)__rowToModel.get(row)).intValue();
	}

	// Set the current value of the editor component to the value passed-in.
	setValue(value);

	// The following code was borrowed from the java JTable's code for validating values upon entry into the table.
	try {
		Class type = table.getColumnClass(column);
		// Since our obligation is to produce a value which is
		// assignable for the required type it is OK to use the
		// String constructor for columns which are declared
		// to contain Objects. A String is an Object.
		if (type == Object.class) {
			type = String.class;
		}
		Class[] argTypes = new Class[] { String.class };
		__constructor = type.getConstructor(argTypes);
	}
	catch (Exception e) {
		return null;
	}
	// end of code borrowed straight from JTable.java
	////////////////////////////////

	// Do different things depending on whether a JTextField or a
	// SimpleJComboBox will be returned.
	if (modelNum != -1 && modelNum < __models.size()) {
		List v = (List)__models.get(modelNum);
		SimpleJComboBox jcb = new SimpleJComboBox(v, __editable);
		jcb.addActionListener(this);
		jcb.setSelectedItem(value);
		__lastJTF = null;
		__lastJCB = jcb;
		return jcb;
	}
	else {
		JTextField jtf = new JTextField();
		jtf.addActionListener(this);
		if (value == null) {
			jtf.setText("");
		}
		else {
			jtf.setText(value.toString());
		}
		__lastJCB = null;
		__lastJTF = jtf;
		jtf.setBorder(null);
		return jtf;
	}
}

/**
Returns whether the cell is editable; from AbstractCellEditor.  Returns true,
unless it was called in response to a single mouse click.
@param event the event that caused this method to be called
@return whether the cell is editable.
*/
public boolean isCellEditable(EventObject event) {
	if (event instanceof MouseEvent) {
		return ((MouseEvent)event).getClickCount() >= 1;
	}
	return true;
}

/**
Removes a cell editor listener; from AbstractCellListener.
@param l the cell editor listener to add.
*/
public void removeCellEditorListener(CellEditorListener l) {
	super.removeCellEditorListener(l);
}

/**
Sets a data model to be be used for a specific row in the JWorksheet.
@param row the row to set the SimpleJComboBox data model for.
@param v a Vector of values (Doules, Integers, Strings, Dates) that will be
used to populate the values in the SimpleJComboBox to use at the given row.
*/
public void setJComboBoxModel(int row, List v) {
	// check to see if the row already has a model assigned to it, and
	// if so, remove it from __models if it is the only instance.
	int modelNum = ((Integer)__rowToModel.get(row)).intValue();
	if (modelNum > -1) {
		int matches = countModelUse(modelNum);
		if (matches == 1) {
			__models.remove(modelNum);
		}
	}

	int i = findModelInModels(v);
	if (i == -1) {
		__models.add(v);
		__rowToModel.set(row, Integer.valueOf(__models.size()-1));
	}
	else {
		__rowToModel.set(row, Integer.valueOf(i));
	}
}

/**
Sets whether a new row added to the JWorksheet should use the same
data model for the SimpleJComboBox as the one immediately preceding it.
@param copy true if the new row should use the data model of the row above
it.
*/
public void setPreviousRowCopy(boolean copy) {
	__previousRowCopy = copy;
}

/**
Sets the editor JComponent's initial value.
@param value the value to set the JComponent to intially.
*/
private void setValue(Object value) {
	if (__lastJCB != null) {
		__lastJCB.setSelectedItem(value);
	}
	else if (__lastJTF != null) {
		if (value == null) {
			__lastJTF.setText("");
		}
		else {
			__lastJTF.setText(value.toString());
		}
	}
	__value = value;
}

/**
Returns true, unless the event that was passed in was a mouse drag event; from AbstractCellEditor.
@param event the event from which it should be determined whether to select the cell or not.
@return whether the cell should be selected
*/
public boolean shouldSelectCell(EventObject event) {
	if (event instanceof MouseEvent) {
		MouseEvent e = (MouseEvent)event;
		if(e.getID() != MouseEvent.MOUSE_DRAGGED) {
			return true;
		}
		else {
			return false;
		}
	}
	return true;
}

/**
Returns whether cell editing can start; from AbstractCellEditor.  Always returns true.
@return true
*/
public boolean startCellEditing() {
	return true;
}

/**
Returns whether or not the cell should stop editing; from AbstractCellEditor.
Unless there is an error while parsing the value in a JTextField, it mostly
just calls fireEditingStopped() and returns true.
@return whether cell editing should stop.
*/
public boolean stopCellEditing() {
	if (__lastJCB != null) {
		if (__lastJCB.isEditable()) {
			__lastJCB.actionPerformed(new ActionEvent(this, 0, ""));
		}
	}

	else if (__lastJTF != null) {
		String s = (String)getCellEditorValueString();
		if (s.equals("")) {
			if (__constructor.getDeclaringClass() == String.class) {
				__value = s;
			}
		}

		try {
			__value = __constructor.newInstance(new Object[]{s});
		}
		catch (Exception e) {
			((JComponent)__lastJTF).setBorder(
				new LineBorder(Color.red));
			return false;
		}
	}

	fireEditingStopped();
	return true;
}

/**
Responds to a row being added to the JTable; from JWorksheet_Listener.
By the time this is called, the JWorksheet has already added the row and taken care of its own internal bookkeeping.
@param row the number of the row that was added.
*/
public void worksheetRowAdded(int row) {
	__size++;
	if (__previousRowCopy && (row != 0)) {
		Integer prevRowModel = (Integer)__rowToModel.get(row - 1);
		__rowToModel.add(row, prevRowModel);
	}
	else {
		__rowToModel.add(row, Integer.valueOf(-1));
	}
}

/**
Responds to a row being deleted from the JTable; from JWorksheet_Listener.
By the time this is called, the JWorksheet has already deleted the row and taken care of its own internal bookkeeping.
@param row the number of the row that was deleted.
*/
public void worksheetRowDeleted(int row) {
	__size--;

	if (row >= __rowToModel.size()) {
		return;
	}

	int modelNum = ((Integer)__rowToModel.get(row)).intValue();

	if (modelNum > -1) {
		int matches = countModelUse(modelNum);
		if (matches <= 1 && modelNum != -1 && modelNum < __models.size()) {
			__models.remove(modelNum);
		}
		else if (modelNum >= __models.size()) {
			// REVISIT (JTS - 2003-12-01)
			// for some reason, the number of models in use is getting out of whack (particularly in the
			// statemod graphing tool worksheet).  Not a fatal
			// error, but it shouldn't be happening!!  Track
			// it down sometime.
		}
	}

	__rowToModel.remove(row);
}

/**
Sets the count of rows for which this class is managing editor components; from JWorksheet_Listener.
@param rows the number of rows to manage.
*/
public void worksheetSetRowCount(int rows) {
	__size = rows;
	__rowToModel = new ArrayList();
	__models = new ArrayList();
	for (int i = 0; i < __size; i++) {
		__rowToModel.add(Integer.valueOf(-1));
	}
}

/**
Does nothing.
*/
public void worksheetSelectAllRows(int time) {
}

/**
Does nothing.
*/
public void worksheetDeselectAllRows(int time) {
}

}