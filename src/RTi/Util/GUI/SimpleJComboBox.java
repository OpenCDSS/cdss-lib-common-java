//---------------------------------------------------------------------------
// SimpleJComboBox - a simplified interface to a JComboBox, optimized for
// 	use with Strings.
//---------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//---------------------------------------------------------------------------
// History:
//
// 2002-10-01	J. Thomas Sapienza, RTi	Initial version.
// 2002-10-10	JTS, RTi		Javadoc'd
// 2002-11-12	JTS, RTi		Changed init() to initialize().  Added
//					some more javadocs, revised some code,
//					and added some methods to bring into
//					line with methods in java.awt.Choice.
// 2003-05-21	JTS, RTi		Added setSelectedPrefixItem for use
//					with some CWRAT code.
// 2003-08-27	JTS, RTi		Added constructor that takes only a 
//					boolean.
// 2003-09-03	JTS, RTi		Renamed 'remove(int)' to 'removeAt(int)'
//					because the old method was conflicting
//					with a method that did a completely
//					different thing in the Container
//					class from which JComboBox is 
//					extended.
// 2003-09-17	JTS, RTi		Added setData() to replace all the
//					values in the combo box at once.
// 2003-09-18	JTS, RTI		setData() now clones the data vector
//					before using it.
// 2003-10-08	JTS, RTi		has() deprecated for contains().
// 2003-12-10	SAM, RTi		Change so that add() does not select
//					the item.
// 2003-12-12	JTS, RTi		* Javadoc'd a few methods that had 
//					  not been doc'd yet.
//					* Changed getSelected() so that if the
//					  Combo Box is editable, the text that
//					  the user typed in is returned, rather
//					  than the currently-selected value.
// 2004-02-24	JTS, RTi		Added isEditable().
// 2004-06-02	JTS, RTi		Added selectIgnoreCase().
// 2004-08-03	JTS, RTi		Added indexOf().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//---------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

import java.util.List;
import java.util.Vector;

/**
A simplified interface to many methods in the JComboBox.
Part of what this does is treat the JComboBox more as 
a JTextField, as JTextField's methods for retrieving and
setting text are more intuitive, but it also emulates a 
little of the functionality of the old Choice classes to
make porting a little more painless.<p>
In addition, it assumes only strings.  Normal JComboBoxes
can have any type of object in them, which makes for 
some annoying cast problems.  SimpleJComboBox only ever
assumes that strings will be placed in the Combo Box.<p>
<b>Editable Combo Boxes</b><p>
The combox box class supports editable combo boxes -- ones in which users
can not only select a value, but can also type in a new value if they don't
find any that meet their needs.<p>
This slightly complicates things for the developer, but not to a great extent.
Here is a list of all the methods that may have non-intuitive responses 
depending on whether a combo box is editable or not.<p>
<ul>
<li><b>getFieldText()</b> - In a non-editable combobox, this method is not
useful and will just return an empty string ("").  In an editable combobox,
this method will return whatever value the user has entered.</li>
<li><b>getItem()</b> - This returns the value in the list of combo box values
that is stored at a specified position.  Because the list of values is stored
internally, this can never be used to return a user-entered value, <b>unless</b>
the value is inserted in the list after being typed in the combo box field.</li>
<li><b>getItemAt()</b> - This returns the value in the list of combo box values
that is stored at a specified position.  Because the list of values is stored
internally, this can never be used to return a user-entered value, <b>unless</b>
the value is inserted in the list after being typed in the combo box field.</li>
<li><b>getSelected()</b> - This returns the currently-selected item.  If the
combo box is editable and the user has not typed in anything, the 
currently-selected value is returned.  If the user has typed anything in, 
that is returned.  In a non-editable combo box, it just returns the 
currently- selected item.</li>
<li><b>getSelectedItem()</b> - Returns the currently-selected value.  This is
the method used by the super class and should not be used.  Instead, use
getSelected().  If used with any combo box, editable or not, it will only 
return the currently-selected value, never anything the user has typed in.</li>
<li><b>getStringAt()</b> - This returns the value in the list of combo box 
values that is stored at a specified position.  Because the list of values is 
stored internally, this can never be used to return a user-entered value, 
<b>unless</b> the value is inserted in the list after being typed in the combo 
box field.</li>
<li><b>getSelectedIndex()</b> - In a non-editable combo box, this will return
the index of the currently-selected value.  In an editable combox box, it will
do the same, unless the user has typed in a value.  In that case, it will 
return -1.</li>
</ul>
*/
public class SimpleJComboBox 
extends JComboBox {

/**
Refers to the very last position in the combo box, for use with 
setSelectionFailureFallback.
*/
public int LAST = -999;

/**
Whether the text field for this combo box is editable or not.
*/
private boolean __editable = false;

/**
The position at which fall back text (see setSelectionFailureFallback) will
be inserted in the combo box.
*/
private int __fallbackPos = -1;

/**
The fallback text to be inserted in the combo box (see 
setSelectionFailureFallback).
*/
private String __fallbackString = "";

/**
Constructor
*/
public SimpleJComboBox() {
	super();
	initialize(-1, false);
}

/**
 * Creates a JComboBox that takes it's items from an existing ComboBoxModel.
 */
public SimpleJComboBox(ComboBoxModel aModel)
	{
		super(aModel);
		initialize(-1, false);
	}

/**
Constructor.
<p><b>Note:</b> if using an editable combo box, the method 
<tt>getSelected()</tt> should be used instead of <tt>getSelectedItem()</tt>.
@param editable if true, then the values in the combo box can be edited.
*/
public SimpleJComboBox(boolean editable) {
	super();
	initialize(-1, editable);
}

/**
Constructor.  Also populates the SimpleJComboBox with the contents of the 
Vector passed in.  The default width of the SimpleJComboBox will be the 
width of the widest String in the Vector.
<p><b>Note:</b> if using an editable combo box, the method 
<tt>getSelected()</tt> should be used instead of <tt>getSelectedItem()</tt>.
@param v a Vector of Strings to be placed in the SimpleJComboBox.
*/
public SimpleJComboBox(List v) {
	super(new Vector(v));
	initialize(-1, false);
}

/**
Constructor.  Also populates the SimpleJComboBox with the contents of the
Vector passed in.  The default width of the SimpleJComboBox will be the
width of the widest String in the Vector.
<p><b>Note:</b> if using an editable combo box, the method 
<tt>getSelected()</tt> should be used instead of <tt>getSelectedItem()</tt>.
@param v a Vector of Strings to be placed in the SimpleJComboBox.
@param editable if true, then the values in the combo box can be edited.
*/
public SimpleJComboBox(List v, boolean editable) {
	super(new Vector(v));
	initialize(-1, editable);
}

/**
Constructor.  Sets the default width of the SimpleJComboBox and also sets 
whether the SimpleJComboBox is editable or not.
<p><b>Note:</b> if using an editable combo box, the method 
<tt>getSelected()</tt> should be used instead of <tt>getSelectedItem()</tt>.
@param defaultSize the default field width of the SimpleJComboBox.
@param editable whether the SimpleJComboBox should be editable (true) or not.
*/
public SimpleJComboBox(int defaultSize, boolean editable) {
	super();
	initialize(defaultSize, editable);
}

/**
Constructor.  Populates the SimpleJComboBox with the contents of the 
Vector of Strings passed in, sets the default field width, and whether 
the combo box is editable.
<p><b>Note:</b> if using an editable combo box, the method 
<tt>getSelected()</tt> should be used instead of <tt>getSelectedItem()</tt>.
@param v a Vector of Strings to be placed in the SimpleJComboBox.
@param defaultSize the default field width of the SimpleJComboBox.
@param editable whether the SimpleJComboBox should be editable (true) or not.
*/
public SimpleJComboBox(List v, int defaultSize, boolean editable) {
	super(new Vector(v));
	initialize(defaultSize, editable);
}

/**
Finalize method.
*/
public void finalize() 
throws Throwable {
	__fallbackString = null;
	super.finalize();
}

/**
Adds a String to the end of the SimpleJComboBox.
@param s the String to add to the SimpleJComboBox.
*/
public void add(String s) {
	addItem(s);
}

/**
Adds a string to a list of strings in a SimpleJComboBox and adds it in
alphabetical order.
@param s the string to add alphabetically to the combo box
*/
public void addAlpha(String s) {
	int size = getItemCount();
	for (int i = 0; i < size; i++) {
		int comp = s.compareTo((String)getItemAt(i));
		if (comp < 0) {
			addAt(s, i);
			return;
		}
	}
	add(s);
}

/**
Adds a string to a list of strings in a SimpleJComboBox and adds it in
alphabetical order.
@param s the string to add alphabetically to the combo box
@param skip the number of initial rows to skip before doing the alphabetical
comparison.
*/
public void addAlpha(String s, int skip) {
	int size = getItemCount();
	for (int i = skip; i < size; i++) {
		int comp = s.compareTo((String)getItemAt(i));
		if (comp < 0) {
			addAt(s, i);
			return;
		}
	}
	add(s);
}

/**
Adds a set of Listeners to the SimpleJComboBox.  The ActionListener is 
used to tell when the SimpleJComboBox selection changes, and the KeyListener
is placed on the SimpleJComboBox's text field to tell whenever a key is 
pressed in the SimpleJComboBox.
@param a an ActionListener.
@param k a KeyListner.
*/
public void addActionAndKeyListeners(ActionListener a, KeyListener k) {
	addActionListener(a);
	addTextFieldKeyListener(k);
}

/**
Adds a String at a certain point in the SimpleJComboBox.
@param s the String to add to the SimpleJComboBox.
@param loc the location at which the String should be inserted.
*/
public void addAt(String s, int loc) {
	insertItemAt(s, loc);
}

/**
Adds a KeyListener to the SimpleJComboBox's text field.  
@param k a KeyListener.
*/
public void addTextFieldKeyListener(KeyListener k) {
	((JTextComponent)getEditor().getEditorComponent()).addKeyListener(k);	
}

/**
Searches through the SimpleJComboBox to see if it contains a given String.
@param s the String for which to search the SimpleJComboBox.
@return true if the String is contained already by the SimpleJComboBox, 
or false if it is not.  False is returned if s is null.
*/
public boolean contains(String s) {
	if (s == null) {
		return false;
	}
	int size = getItemCount();
	for (int i = 0; i < size; i++) {
		if (s.equals((String)getItemAt(i))) {
			return true;
		}
	}
	return false;
}

/**
Returns the text currently displayed by the Simple JComboBox -- more
specifically, the text that has been entered by the user into the editable
text field of the combo box.
@return a String containing the text of the text field of the 
SimpleJComboBox.
*/
public String getFieldText() {
	return ((JTextComponent)getEditor().getEditorComponent()).getText();
}

/** 
Returns the String at the given index.  Mimics Choice's getItem(int) method.
If any edits have been made to the combo box (for instance, if it is editable 
and the user has entered a new value), those edits will not be represented in 
this call.<p>
If the location is out of bounds, null is returned.
@param location the index in the SimpleJComboBox of the item to return.
@return the String at the given index.
*/
public String getItem(int location) {
	return (String)getItemAt(location);
}

/**
Returns the list item at the specified index.  If index is out of range (less
than zero or greater than or equal to size), it will return null.  If any
edits have been made to the combo box (for instance, if it is editable and
the user has entered a new value), those edits will not be represented in 
this call.<p>
If the location is out of bounds, null is returned.
@param index an integer indicating hte list position, where the first item
starts at zero.
@return the Object at that list position, or null if out of range.
*/
public Object getItemAt(int index) {
	return super.getItemAt(index);
}

/**
Searches through the SimpleJComboBox to see if it contains a given 
String and then returns the position in the SimpleJComboBox of the String.
@param s the String for which to search in the SimpleJComboBox.
@return the numeric location of the String in the SimpleJComboBox 
(base 0), or -1 if the String was not found.
*/
public int getPosition(String s) {
	int size = getItemCount();
	for (int i = 0; i < size; i++) {
		if (s.equals((String)getItemAt(i))) {
			return i;
		}
	}
	return -1;
}	

/**
Returns the currently-selected SimpleJComboBox option's text.  
See getFieldText() for information as to how these methods differ.
@return a String containing the text of the currently-selected 
SimpleJComboBox value.
*/
public String getSelected() {
	if (__editable) {
		return getFieldText();
	}
	else {
		return (String)getSelectedItem();
	}
}

/**
Returns the current selected item.<p>
<B>Don't use.  Use getSelected() instead, or strange behavior may be 
encountered with editable combo boxes.</b><p>
If the combo box is editable, then this value may not have been added to the 
combo box with <tt>addItem</tt>, <tt>insertItemAt</tt>, or the data 
constructors.
@return the current selected Object.
*/
public Object getSelectedItem() {
	return super.getSelectedItem();
}

/**
Returns the value stored at the specified position.
@param i the position from which to return the String.
@return the value stored at the specified position.
*/
public String getStringAt(int i) {
	return (String)(super.getItemAt(i));
}

/**
Do not use.
@deprecated do not use.  Use getSelected() instead.
*/
public String getText() {
	return (String)getSelected();
}

/**
Returns the combo box's text editor.  This is what the user types values into
in an editable combo box.  In a non-editable combo box, this returns null.
@return the combo box's text editor, or null if the combo box is uneditable.
*/
public JTextComponent getJTextComponent() {
	return ((JTextComponent)(getEditor().getEditorComponent()));
}

/**
Searches through the SimpleJComboBox to see if it contains a given String and 
returns the index of the string in the box.
@param s the String for which to search the SimpleJComboBox.
@return -1 if the string is not found, or the index of the first match.
*/
public int indexOf(String s) {
	int size = getItemCount();
	for (int i = 0; i < size; i++) {
		if (s.equals((String)getItemAt(i))) {
			return i;
		}
	}
	return -1;
}

/**
Initializes the SimpleJComboBox with a defaultSize and editable value.  If 
defaultSize is set to -1, the defaultSize will be calculated from the width
of the widest String in the SimpleJComboBox.
@param defaultSize the width to make the SimpleJComboBox.  If defaultSize 
is set to -1, the width will be calculated from the width of the widest 
String in the SimpleJComboBox.
@param editable whether the SimpleJComboBox should be editable (true) or not.
*/
private void initialize(int defaultSize, boolean editable) {
	if (defaultSize > -1) {
		String s = "";
		for (int i = 0; i < defaultSize; i++) {
			s += "W";
		}
		setPrototypeDisplayValue(s);
	}

	setEditable(editable);
	__editable = editable;
}

/** 
Inserts a String at the given position into the SimpleJComboBox.  Mimics
Choice's insert(String, int) method.
@param str the String to be inserted.
@param location the index at which to insert the String.
*/
public void insert(String str, int location) {
	insertItemAt(str, location);
}

/**
Inserts an item into the item list at a given index.  This method works only
if the <tt>JComboBox</tt> uses a mutable data model.  This method will not
work if the Object to be inserted is not a String.
@param anObject the Object to add to the list.
@param index an integer specifying the position at which to add the item.
*/
public void insertItemAt(Object anObject, int index) {
	if (anObject == null || anObject instanceof String) {
		super.insertItemAt(anObject, index);
	}
}

/**
Returns whether the text is editable or not.
@return whether the text is editable or not.
*/
public boolean isEditable() {
	return __editable;
}

/**
Removes the String at the given Index from the SimpleJComboBox.  Mimics
Choice's remove(int) method.
@param location the index in the SimpleJComboBox of the String to be removed.
If location is greater than the number of elements in the combo box, or less
than 0, nothing will be done.
*/
public void removeAt(int location) {
	if (location >= getItemCount() || location < 0) {
		return;
	}
	else {
		removeItemAt(location);
	}
}

/**
Do not call.
@deprecated use removeAt instead.
*/
public void remove(int location) {
	super.remove(location);
}

/**
Removes the first occurrence of a String from the SimpleJComboBox.  
Mimics Choice's remove(String) method.
@param s the String to remove from the SimpleJComboBox.
@return true if the String was found and removed, or false if the String
was not found in the SimpleJComboBox.
*/
public boolean remove(String s) {
	if (contains(s)) {
		removeItem(s);
		return true;
	}
	return false;
}

/**
Removes all items from the SimpleJComboBox.  Mimics Choice's removeAll() 
method.
*/
public void removeAll() {
	removeAllItems();
}

/**
Removes the KeyListener from the SimpleJComboBox's text field.
@param k the KeyListener to remove.
*/
public void removeTextFieldKeyListener(KeyListener k) {
	((JTextComponent)getEditor().getEditorComponent()).removeKeyListener(k);
}

/**
Sets the String at the given index as the currently-selected String.  Mimics
Choice's select(int) method.
@param location the index in the SimpleJComboBox of the String to be the
currently-selected String.
*/
public void select(int location) {
	setSelectedIndex(location);
}

/**
Sets the given String as the currently-selected String.  Mimics
Choice's select(String) method.
@param str the String to set as the currently-selected String.
*/
public void select(String str) {
	setSelectedItem(str);
}

/**
Selects the given String (if it exists in the combo box), ignoring case.
If the string does not exist in the combo box, no change will be made to the
current selection.
@param str the String to select.
*/
public void selectIgnoreCase(String str) {
	int size = getItemCount();
	String s = null;
	for (int i = 0; i < size; i++) {
		s = getStringAt(i);
		if (s.equalsIgnoreCase(str)) {
			select(i);
			return;
		}
	}	
}

/**
Sets the data stored in the combo box all at once.
@param v a Vector of Strings, each of which will be an item in the combo box.
*/
public void setData(List v)
{
	setModel(new DefaultComboBoxModel(new Vector(v)));
	repaint();
}

/**
Sets whether the textfield of this combobox is editable or not.
<p><b>Note:</b> if using an editable combo box, the method 
<tt>getSelected()</tt> should be used instead of <tt>getSelectedItem()</tt>.
@param editable whether the textfield is editable or not.
*/
public void setEditable(boolean editable) {
	__editable = editable;
	super.setEditable(editable);
}

/**
Sets the selected item in the combo box display area to the object in the
argument.  If anObject is in the list, the display area shows anObject
selected.  <b>anObject must be a String or nothing will happen</b>.<p>
If anObject is <i>not</i> in the list and the combo box is uneditable, it
will not change the current selection.  For editable combo boxes, the selection
will change to anObject.<p>
If this constitutes a change in the selected item, ItemListeners added to the 
combo box will be notified with one or two ItemEvents.  If there is a current
selected item, an ItemEvent will be fired and the state change will be 
ItemEvent.DESELECTED.  If anObject is in the list and is not currently 
selected then an ItemEvent will be fired and the StateChange will be 
ItemEvent.SELECTED.<p>
ActionListeners added to the combo box will be notified with an ActionEvent 
when this method is called.
@param anObject the list object to select; use null to clear the selection.
*/
public void setSelectedItem(Object anObject) {
	if (anObject == null || anObject instanceof String) {
		super.setSelectedItem(anObject);
	}
}

/**
Sets the currently selected item to be the first value in the list that 
starts with the characters in prefix (case-sensitive).
@param prefix the prefix to match.
@return true if a matching item was found; false if not.
*/
public boolean setSelectedPrefixItem(String prefix) {
	int size = getItemCount();
	String s = null;
	for (int i = 0; i < size; i++) {
		s = (String)getItemAt(i);
		if (s.startsWith(prefix)) {
			setSelectedIndex(i);
			return true;
		}
	}
	if (__fallbackPos != -1 && !prefix.trim().equals("")) {
		if (__fallbackPos == LAST) {
			if (__fallbackString == null) {
				select(getItemCount() - 1);
			}
			else {
				int index = __fallbackString.indexOf("~");
				String value = null;
				if (index > -1) {
					value = __fallbackString.substring(
						0, index);
					value += prefix;
					value += __fallbackString.substring(
						index + 1);
				}
				else {
					value = new String(__fallbackString);
				}
				addAt(value, getItemCount());
				select(getItemCount() - 1);
			}
		}
		else {
			if (__fallbackString == null) {
				select(__fallbackPos);
			}
			else {
				int index = __fallbackString.indexOf("~");
				String value = null;
				if (index > -1) {
					value = __fallbackString.substring(
						0, index);
					value += prefix;
					value += __fallbackString.substring(
						index + 1);
				}
				else {
					value = new String(__fallbackString);
				}
				if (__fallbackPos == 0) {
					insertItemAt(value, __fallbackPos);
				}
				else {
					addAt(value, __fallbackPos);
				}
				select(__fallbackPos);
			}
		}
	}
	return false;
}

/**
Sets the item that should be inserted into the list if no items could be
matched by selecting a specific prefix.  If there is a "~" in the fallback
String, then whatever the value that was to be matched was will be inserted
in the string at the position of the first tilde.<p>
For instance if the following were done:<p>
<pre>	comboBox.setSelectionFailureFallback("Value (~) not found", 0);</pre>
And a call was made to <pre>setSelectedPrefixItem("Station 1");</pre><br>
If no Strings were found that match the prefix "Station 1", the following
String would be inserted at the very beginning of the Combo Box:<p>
<pre>	"Value (Station 1) not found"</pre>
@param text the fallback String to insert in the combo box if the selected
prefix item could not be found.  If null, the fallback String process will be
disabled.
@param i the position in the combo box at which to insert the fallback item.
If -1, the fallback String process will be disabled.
*/
public void setSelectionFailureFallback(String text, int i) {
	if (text == null || i == -1) {
		__fallbackString = null;
		__fallbackPos = -1;
	}
	else {
		__fallbackString = text;
		__fallbackPos = i;
	}
}

/**
Sets the current text of the SimpleJComboBox to the given String.  
If the String is already in the SimpleJComboBox, then that element of the 
SimpleJComboBox is made the currently-selected element.  If the String is 
not found, the String will be added and then made the currently-selected 
element.
@param s the String to set the currently-selected element to.
*/
public void setText(String s) {
	if (!contains(s)) {
		add(s);
	}
	setSelectedItem(s);
}

}
