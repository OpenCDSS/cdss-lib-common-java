// SimpleJList - class to allow an easily-changeable JList

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

//---------------------------------------------------------------------------
// SimpleJList - class to allow an easily-changeable JList.
//---------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//---------------------------------------------------------------------------
// History:
//
// 2002-09-18	J. Thomas Sapienza, RTi	Initial version.
// 2002-09-25	JTS, RTi		Javadoc'd
// 2002-12-17	JTS, RTi		Added 'getSelectedItem()'
// 2003-03-25	JTS, RTi		Added the Array and Vector constructors.
// 2003-03-26	JTS, RTi		Added code to support an optional 
//					InverseListSelectionModel.
// 2004-05-04	JTS, RTi		Added the setListData() methods.
// 2004-05-06	JTS, RTi		Now uses the MutableJList_SelectionModel
// 2005-04-08	JTS, RTi		Renamed from MutableJList.
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//---------------------------------------------------------------------------

package RTi.Util.GUI;

import java.util.List;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.DefaultListModel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
Class wraps around JList and provides functionality for dynamically 
changing the contents of JList.<p>
Example of use:<p>
<code>
	SimpleJList sourceList = new SimpleJList();
	sourceList.addMouseListener(this);
	sourceList.add(RT);
	sourceList.add(WIS);
	sourceList.addListSelectionListener(this);
</code>
*/
public class SimpleJList 
extends JList 
implements ListSelectionListener {

/**
Whether to automatically update the list or not.
*/
private boolean __autoUpdate = true;

/**
The List model that the list will use.
*/
private DefaultListModel __dlm;

/**
The InverseListSelectionModel that can possibly be used.
*/
private InverseListSelectionModel __ilsm = null;

/**
The list selection model used by default.
*/
private SimpleJList_SelectionModel __mjlsm = null;

/**
Constructor.
*/
public SimpleJList() {
	super();
	__dlm = new DefaultListModel();
	initialize();
	setModel(__dlm);
}	

/**
Constructor.
@param array an array of objects that will be used to populate the list.
*/
public SimpleJList(Object array[]) {
	super();
	__dlm = new DefaultListModel();
	for (int i = 0; i < array.length; i++) {
		__dlm.addElement(array[i]);
	}
	initialize();
	setModel(__dlm);
}

/**
Constructor.
@param vector a list of objects that will be used to populate the list.
*/
public SimpleJList(List vector) {
	super();
	__dlm = new DefaultListModel();
	int size = vector.size();
	for (int i = 0; i < size; i++) {
		__dlm.addElement(vector.get(i));
	}
	initialize();
	setModel(__dlm);
}

/**
Adds an object to the JList in the last position.
@param o the object to add to the list.
*/
public void add(Object o) {
	__dlm.addElement(o);
	if (__ilsm != null) {
		__ilsm.add(__dlm.size());
	}
	else {
		__mjlsm.add(__dlm.size());
	}
	update();
}

/**
Adds an object to the JList in the given position.
@param o the object to add to the list
@param pos the position in the list at which to put the object.
*/
public void add(Object o, int pos) {
	__dlm.add(pos, o);
	if (__ilsm != null) {
		__ilsm.add(pos);
	}
	else {
		__mjlsm.add(pos);
	}
	update();
}

/**
Deselects the specified row.  Does not work with InverseListSelectionModels.
@param row the row to deselect.
*/
public void deselectRow(int row) {
	if (__ilsm != null) {
		return;
	}
	((SimpleJList_SelectionModel)getSelectionModel()).deselectRow(row);
}	

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__dlm = null;
	__mjlsm = null;
	__ilsm = null;
	super.finalize();
}

/**
Returns whether automatic updating of the list is turned on
@return __autoUpdate
@see #setAutoUpdate
*/
public boolean getAutoupdate() {
	return __autoUpdate;
}

/**
Returns the Object at the given position.
@param pos the position at which to return the Object.
@return the Object at the given position.
*/
public Object getItem(int pos) {
	return __dlm.get(pos);
}

/**
Returns the number of items in the list.
@return the number of items in the list.
*/
public int getItemCount() {
	return __dlm.size();
}

/**
Returns all the items in the list.
@return all the items in the list.
*/
public List getItems() {
	List v = new Vector(__dlm.size());
	for (int i = 0; i < __dlm.size(); i++) {
		v.add(__dlm.get(i));
	}
	return v;
}

/**
Returns the number of items in the list.
@return the number of items in the list.
*/
public int getListSize() {
	return __dlm.size();
}

/**
Returns only the first selected item in the list.
@return only the first selected item in the list.
*/
public Object getSelectedItem() {
	int[] indices = getSelectedIndices();

	return __dlm.get(indices[0]);
}

/**
Returns only the selected items in the list.  The returned Vector is guaranteed to be non-null.
@return only the selected items in the list.
*/
public List getSelectedItems() {
	List v = new Vector(getSelectedSize());
	int[] indices = getSelectedIndices();	
	for (int i = 0; i < indices.length; i++) {
		v.add(__dlm.get(indices[i]));
	}
	return v;
}

/**
Returns the number of rows selected in the list.
@return the number of rows selected in the list.
*/
public int getSelectedSize() {
	int[] indices = getSelectedIndices();
	return indices.length;
}

/**
Returns the index of the given object in the list.
@param o the Object for which to search in the list.
@return the index of the given object in the list.
*/
public int indexOf(Object o) {
	return __dlm.indexOf(o);
}

/**
Returns the first index of the given object in the list, starting from a
certain point.
@param o the Object for which to search in the list.
@param pos the position from which to start searching.
@return the index of the object in the list
*/
public int indexOf(Object o, int pos) {
	return __dlm.indexOf(o, pos);
}

/**
Initializes internal settings.
*/
private void initialize() {
	__mjlsm = new SimpleJList_SelectionModel(getItemCount());
	setSelectionModel(__mjlsm);
	__mjlsm.setSupportsDragAndDrop(true);
	__mjlsm.addListSelectionListener(this);
	addMouseListener(__mjlsm);
}

/**
Removes the object at the given position.
@param pos the position of the Object to be removed.
*/
public void remove(int pos) {
	__dlm.remove(pos);
	if (__ilsm != null) {
		__ilsm.remove(pos);
	}	
	else {
		__mjlsm.remove(pos);
	}
	update();
}

/**
Removes a given Object from the list
@param o the Object to be removed.
*/
public void remove(Object o) {
	int index = indexOf(o);
	__dlm.removeElement(o);
	if (__ilsm != null) {
		__ilsm.remove(index);
	}
	else {
		__mjlsm.remove(index);
	}
	update();
}

/**
Removes all objects form the list
*/
public void removeAll() {
	__dlm.removeAllElements();
	if (__ilsm != null) {
		__ilsm.update(0);
	}
	else {
		__mjlsm.update(0);
	}
	update();
}

/**
Selects a given row, deselecting all other rows.
@param i the row to select
*/
public void select(int i) {
	setSelected(i);
}

/**
Selects a given row, deselecting all other rows.
@param i the row to select
*/
public void selectRow(int i) {
	setSelected(i);
}

/**
Selects all the rows in the list.
*/
public void selectAll() {
	if (__ilsm != null) {
		__ilsm.selectAll();
	}
	else {
		setSelectionInterval(0, __dlm.size() - 1);
	}
}

/**
Sets the object at the given position.
@param pos the position at which to set the object.
@param o the object to set in the position, overwriting the old object.
*/
public void set(int pos, Object o) {
	__dlm.set(pos, o);
	update();
}

/**
Sets whether the list should be automatically updated any time it is changed
or not.
@param update if true, the list will be auto updated every time it changes.
*/
public void setAutoUpdate(boolean update) {
	__autoUpdate = update;
}

/**
Sets up the SimpleJList as having an inverse list selection model (or not).
@param inverse if true, the list selection model is set to be an 
InverseListSelectionModel.  If false, the normal DefaultListSelectionModel is
used.
*/
public void setInverseListSelection(boolean inverse) {
	if (inverse == true) {
		__ilsm = new InverseListSelectionModel(__dlm.size());
		setSelectionModel(__ilsm);
		removeMouseListener(__mjlsm);
	}
	else {
		__ilsm = null;
		initialize();
	}
}

/**
Sets the data in the list from an array of Objects.  Any data already in the
list will be lost.  Overloads the method in JList.
@param array the array of Objects to populate the list with.
*/
public void setListData(Object[] array) {
	__dlm = new DefaultListModel();
	for (int i = 0; i < array.length; i++) {
		__dlm.addElement(array[i]);
	}
	
	setModel(__dlm);
}

/**
Sets the data in the list from a Vector of Objects.  Any data already in the 
list will be lost.  Overloads the method in JList.
@param vector the Vector of Objects to populate the list with.
*/
public void setListData(List vector) {
	__dlm = new DefaultListModel();
	int size = vector.size();
	for (int i = 0; i < size; i++) {
		__dlm.addElement(vector.get(i));
	}
	setModel(__dlm);
}

/**
Sets the model to use with this SimpleJList.
@param dlm a non-null DefaultListModel to use.
*/
public void setModel(DefaultListModel dlm) {
	super.setModel(dlm);
	if (__ilsm != null) {
		__ilsm.update(dlm.size());
	}
	else {
		__mjlsm.update(dlm.size());
	}
}

/**
Selects a given row, deselecting all other rows.
@param i the row to select
*/
public void setSelected(int i) {
	setSelectedIndex(i);
}

/**
If autoupdate is true, update the list with the current list model
*/
public void update() {
	if (__autoUpdate == true) {
		setModel(__dlm);
	}
}

/**
If autoupdate is true, update the list with the current list model.
@param update autoupdate will be set to this value, so instead of calling
setAutoupdate(true) and then update(), both commands can be combined into
one by calling update(true);
*/
public void update(boolean update) {
	__autoUpdate = update;
	if (__autoUpdate == true) {	
		setModel(__dlm);
	}
}

/**
Repaints the list in response to list changes. From ListSelectionListener.
@param event the ListSelectionEvent that happened.
*/
public void valueChanged(ListSelectionEvent event) {
	repaint();
}

}
