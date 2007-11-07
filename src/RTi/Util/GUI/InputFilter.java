//-----------------------------------------------------------------------------
// InputFilter - class to handle an input filter "Where...Is"
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2003-12-18	Steven A. Malers, RTi	Initial version, for use with software
//					that queries databases.
// 2004-02-01	SAM, RTi		* Add clone().
//					* Add delimiter information for choices
//					  to allow generic handling in
//					  getInput().
// 2004-01-06	SAM, RTi		* Add matches() to simplify comparison
//					  of input.
// 2004-10-26	SAM, RTi		* Overload matches() to operate on
//					  integer and double types and
//					  transparently handle different input
//					  types.
// 2005-01-31	J. Thomas Sapienza, RTi	* Added __where_internal_2.
//					* Added addInputComponentMouseListener()
//					* Added getConstraintsToRemove().
//					* Added isInputJTextFieldEditable().
//					* Added removeConstraint().
//					* Added setInputComponentToolTipText().
//					* Added setInputJTextFieldEditable().
// 2005-02-02	JTS, RTi		* Added capability to set the number
//					  of rows in an input JComboBox to
//					  display at once.
//					* Added capability to set the width
//					  of the input JTextFields.
// 2005-04-05	JTS, RTi		Modified getInputInternal() so that if
//					a user enters a value in a combo box
//					entry field, that value can be returned.
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

package RTi.Util.GUI;

import java.awt.event.MouseListener;

import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTextField;

import RTi.Util.String.StringUtil;

/**
This class provides a way to define an input filter, for example for use in a
GUI as a "[field] [constraint] [input] choice.  [field] is the data field 
that will be constrained.  [constraint] is the way the data field will be 
matched against (see the INPUT_* data members).  [input] is the value to 
constrain the field against.<p>

In most cases, a Vector of these objects is created as appropriate by 
specific software and is then managed by a InputFilter_JPanel object.
*/
public class InputFilter implements Cloneable
{

/**
Input filter type, for use with strings that exactly match a pattern.
*/
public final static String INPUT_MATCHES = "Matches";

/**
Input filter type, for use with strings and numbers that match a case in a list.
*/
public final static String INPUT_ONE_OF = "One of";

/**
Input filter type, for use with strings starting with a pattern.
*/
public final static String INPUT_STARTS_WITH = "Starts with";

/**
Input filter type, for use with strings ending with a pattern.
*/
public final static String INPUT_ENDS_WITH = "Ends with";

/**
Input filter type, for use with strings containing a pattern.
*/
public final static String INPUT_CONTAINS = "Contains";

/**
Input filter type, for use with numbers that exactly match a pattern.
*/
public final static String INPUT_EQUALS = "Equals";

/**
Input filter type, for use with numbers that are between two values.
*/
public final static String INPUT_BETWEEN = "Is between";

/**
Input filter type, for use with numbers that are less than a value.
*/
public final static String INPUT_LESS_THAN = "Less than";

/**
Input filter type, for use with numbers that are less than or equal to a value.
*/
public final static String INPUT_LESS_THAN_OR_EQUAL_TO = "<=";

/**
Input filter type, for use with numbers that are greater than a value.
*/
public final static String INPUT_GREATER_THAN = "Greater than";

/**
Input filter type, for use with numbers that are greater than or equal to a
value.
*/
public final static String INPUT_GREATER_THAN_OR_EQUAL_TO = ">=";

/**
Specifies for an input JComboBox to display all of its rows at once.
*/
public final static int JCOMBOBOX_ROWS_DISPLAY_ALL = -1;

/**
Specifies for an input JComboBox to display the default number of rows.
*/
public final static int JCOMBOBOX_ROWS_DISPLAY_DEFAULT = 0;

private String __where_internal = "";
				// The string that is internally used in a
				// where clause (e.g., a database table
				// column: "table.column").
private String __where_internal_2 = "";
				// The string that is internally used in a 
				// where clause (e.g., a database table 
				// column: "table.column").  This secondary
				// where string can be used in case of times
				// where in certain instances a different thing
				// is queried against.  For instance, the
				// first where string could be used for SQL 
				// queries and the second where string could
				// be used when querying against fields in 
				// a database view.  
private String __where_label="";// The string label to be visible to the user
				// (e.g., "Station Name").
private int __input_type = 0;	// A value from
				// RTi.Util.String.StringUtil.TYPE_*, indicating
				// the expected input value.  The type is used
				// to check the validity of the input with 
				// checkInput().
private Vector __choice_label_Vector = null;
				// A Vector of String choices to choose from.
				// If not null a JComboBox will be displayed
				// to let the user choose from.
private Vector __choice_internal_Vector = null;
				// The internal values (e.g., database table
				// and column) corresponding to the visible
				// choices.
private String __choice_delimiter = null;
				// Used when the choices are not simple strings
				// but contain informational notes - indicates
				// the delimiter between data and notes.
private int __choice_token = 0;
				// Used when the choices are not simple strings
				// but contain informational notes - indicates
				// the token position (relative to 0) for the
				// data in the choices.
private JComponent __input_component = null;
				// The component used to enter input, typically
				// assigned by external code like
				// InputFilter_JPanel
private boolean __are_choices_editable = false;
				// If true, it is expected that the JComboBox
				// used with __choices_Vecor is editable.

private boolean __inputJTextFieldEditable = true;
				// if the input component is going to be a 
				// text field, this can be set to false to
				// set the text field uneditable (this is only
				// useful in very limited circumstances).

private int __inputJTextFieldWidth = 10;
				// the width of the input JTextField (if used)
				// on the GUI, roughly in characters.

private int __numInputJComboBoxRows = JCOMBOBOX_ROWS_DISPLAY_DEFAULT;
				// specifies how many rows of values to show 
				// at one time in a GUI.  Possible values are
				// JCOMBOBOX_ROWS_DISPLAY_ALL,
				// JCOMBOBOX_ROWS_DISPLAY_DEFAULT, 
				// or a positive integer.

private String __inputComponentToolTipText = null;
				// if any tool tip text is defined for the
				// InputFilter, it is stored here.  Otherwise,
				// this value is null.

private Vector __inputComponentMouseListeners = null;
				// the listeners that want to listen to mouse
				// events on the input component.
private Vector __removedConstraints = null;
				// if not null, contains all the constraints
				// that should NOT appear in the constraint
				// combo box for this InputFilter.

/**
Construct an input filter.
@param where_label A string to be listed in a choice to tell the user what
input parameter is being filtered.  A blank string can be specified to indicate
that the filter can be disabled.
@param where_internal The internal value that can be used to perform a query.
For example, set to a database table and column ("table.column").  This can be
set to null or empty if not used by other software.
@param input_type The input filter data type, see
RTi.Util.String.StringUtil.TYPE_*.
@param choice_labels A Vector of String containing choice values to be
displayed to the user.  If null, the user will not be shown a list of choices.
@param choices_internal A Vector of String containing choice values (e.g.,
database column values).
@param are_choices_editable If true, and a non-null list of choices is provided,
the choices will also be editable (an editable JTextField part of the JComboBox
will be shown).
*/
public InputFilter (	String where_label, String where_internal,
			int input_type, Vector choice_labels,
			Vector choices_internal, boolean are_choices_editable )
{
	this(where_label, where_internal, "", input_type, choice_labels,
		choices_internal, are_choices_editable);
}

/**
Construct an input filter.
@param where_label A string to be listed in a choice to tell the user what
input parameter is being filtered.  A blank string can be specified to indicate
that the filter can be disabled.
@param where_internal The internal value that can be used to perform a query.
For example, set to a database table and column ("table.column").  This can be
set to null or empty if not used by other software.
@param where_internal_2 the internal value that can be used if in certain 
cases a different field must be used for performing a query.  For instance, if
a database can be used to query with SQL or to query against a database view.
It can be set to null or an empty string if it won't be used by software.
@param input_type The input filter data type, see
RTi.Util.String.StringUtil.TYPE_*.
@param choice_labels A Vector of String containing choice values to be
displayed to the user.  If null, the user will not be shown a list of choices.
@param choices_internal A Vector of String containing choice values (e.g.,
database column values).
@param are_choices_editable If true, and a non-null list of choices is provided,
the choices will also be editable (an editable JTextField part of the JComboBox
will be shown).
*/
public InputFilter (	String where_label, String where_internal,
			String where_internal_2, int input_type, 
			Vector choice_labels, Vector choices_internal, 
			boolean are_choices_editable )
{	__where_label = where_label;
	__where_internal = where_internal;
	__where_internal_2 = where_internal_2;
	__input_type = input_type;
	__choice_label_Vector = choice_labels;
	__choice_internal_Vector = choices_internal;
	__are_choices_editable = are_choices_editable;
}

/**
Adds a mouse listener to the input field.  This listener will be notified of
any mouse events on the Input component.
@param listener the listener to add to the input component.
*/
public void addInputComponentMouseListener(MouseListener listener) {
	if (__inputComponentMouseListeners == null) {
		__inputComponentMouseListeners = new Vector();
	}
	__inputComponentMouseListeners.add(listener);
}

/**
Indicate whether the input choices are editable.  If true, the user should be
able to type in a value in addition to using the choices.  This is only used
when choices are provided.
*/
public boolean areChoicesEditable()
{	return __are_choices_editable;
}

/**
Clone the object.  The Object base class clone() method is called and then the
data members are cloned.  The result is a complete deep copy.  The only
exception is that the input component is set to null - it normally needs to be
defined by calling code after the clone() call occurs.
*/
public Object clone()
{	try {	// Clone the base class...
		InputFilter filter = (InputFilter)super.clone();
		// Now clone the mutable objects...
		// The following clone automatically because they are
		// primitives:
		//
		// __where_internal
		// __where_label
		// __input_type
		// __are_choices_editable
		// __choice_delimiter
		// __choice_token

		// Do not clone the __input_component because it will be set
		// in calling code.

		filter.__input_component = null;

		// Copy the contents of the vectors...

		if ( __choice_label_Vector != null ) {
			int size = __choice_label_Vector.size();
			filter.__choice_label_Vector = new Vector ( size );
			for ( int i = 0; i < size; i++ ) {
				filter.__choice_label_Vector.addElement (
					__choice_label_Vector.elementAt(i) );
			}
		}
		if ( __choice_internal_Vector != null ) {
			int size = __choice_internal_Vector.size();
			filter.__choice_internal_Vector = new Vector ( size );
			for ( int i = 0; i < size; i++ ) {
				filter.__choice_internal_Vector.addElement (
					__choice_internal_Vector.elementAt(i) );
			}
		}
		return filter;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is clonable.
		throw new InternalError();
	}
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__where_internal = null;
	__where_internal_2 = null;
	__where_label=null;
	__choice_label_Vector = null;
	__choice_internal_Vector = null;
	__choice_delimiter = null;
	__input_component = null;
	__inputComponentToolTipText = null;
	__inputComponentMouseListeners = null;
	__removedConstraints = null;
	super.finalize();
}

/**
Return the choices that are visible to the user. 
@return the choices that are visible to the user. 
*/
public Vector getChoiceLabels()
{	return __choice_label_Vector;
}

/**
Return the delimiter used in choices, which is used to separate actual data from
descriptive information.
@return the delimiter used in choices.
*/
public String getChoiceDelimiter ()
{	return __choice_delimiter;
}

/**
Return the token position used in choices, which is the position of actual data,
compared to descriptive information.
@return the token position used in choices (0+).
*/
public int getChoiceToken ()
{	return __choice_token;
}

/**
Returns the constraints that should not appear in the constraint combo box
for this InputFilter.
@return the constraints that should not appear in the constraint combo box
for this InputFilter.
*/
protected Vector getConstraintsToRemove() {
	return __removedConstraints;
}

/**
Return the user-supplied input, as entered in the input component.
If the input component is a SimpleJComboBox, the internal value is returned if
not blank and the input choice is not blank.
@return the user-supplied input, as a string, or null if the input component
is null.
@param return_full If true, then full input is returned, which may include
informational comments.  If false, only the specific data token is returned,
requiring that setTokenInfo() be called for the filter.
*/
public String getInput ( boolean return_full )
{	if ( __input_component == null ) {
		return null;
	}
	else if ( __input_component instanceof SimpleJComboBox ) {
		SimpleJComboBox cb = (SimpleJComboBox)__input_component;
		if ( return_full || (__choice_delimiter == null) ) {
			// Input choices are not formatted...
			return cb.getSelected();
		}
		else {	// Return a token from the input choices.
			return StringUtil.getToken(cb.getSelected(),
				__choice_delimiter,
				StringUtil.DELIM_SKIP_BLANKS, __choice_token );
		}
	}
	else if ( __input_component instanceof JTextField ) {
		return ((JTextField)__input_component).getText();
	}
	return null;
}

/**
Return the input component for the data filter.  This is used by external code
to manage GUI components used for input.  A distinct component should be
available for each filter, usuall a JComboBox or a JTextField.
@return the input component.
*/
public JComponent getInputComponent()
{	return __input_component;
}

/**
Returns the Vector of mouse listeners for the input component.  May be null if
none have been set.
@return the Vector of mouse listeners for the input component.
*/
public Vector getInputComponentMouseListeners() {
	return __inputComponentMouseListeners;
}

/**
Returns the tool tip text assigned to the input component, if any.
@return the tool tip text assigned to the input component, if any.
*/
protected String getInputComponentToolTipText() {
	return __inputComponentToolTipText;
}

/**
Return the input using internal notation.  If the input component is a
SimpleJComboBox, the internal input is returned.  If the input component is a
JTextField, the visible input is returned.
@return the input using internal notation.
*/
public String getInputInternal ()
{	if ( __input_component instanceof SimpleJComboBox ) {
		SimpleJComboBox cb = (SimpleJComboBox)__input_component;
		int pos = cb.getSelectedIndex();
		if (pos == -1) {
			return cb.getFieldText();
		}
		else {
			return (String)__choice_internal_Vector.elementAt(pos);
		}
	}
	else {	//JTextField...
		return getInput ( true );
	}
}

/**
Returns the width of the input JTextField, roughly in characters.  Default is
10.
@return the width of the input JTextField.
*/
protected int getInputJTextFieldWidth() {
	return __inputJTextFieldWidth;
}

/**
Return the input type (one of StringUtil.TYPE_*).
@return the input type (one of StringUtil.TYPE_*).
*/
public int getInputType()
{	return __input_type;
}

/**
Returns the maximum number of rows to display for this InputFilter's input
JComboBox.  Possible values are JCOMBOBOX_ROWS_DISPLAY_ALL,
JCOMBOBOX_ROWS_DISPLAY_DEFAULT and positive 
integers.
@return the maximum number of rows to display for this InputFilter's input
JComboBox.
*/
protected int getNumberInputJComboBoxRows() {
	return __numInputJComboBoxRows;
}

/**
Return the internal Where field that corresponds to the visible label. 
@return the internal Where field that corresponds to the visible label. 
*/
public String getWhereInternal ()
{	return __where_internal;
}

/**
Returns the secondary internal Where field that corresponds to the visible 
label.  If no secondary internal where field has been defined, then the 
primary internal where field is returned (identical to calling 
getWhereInternal()).
@return the secondary internal Where field that corresponds to the visible 
label.
*/
public String getWhereInternal2() {
	if (__where_internal_2 != null && !__where_internal_2.equals("")) {
		return __where_internal_2;
	}
	else {
		return getWhereInternal();
	}
}

/**
Return the Where field label that is visible to the user. 
@return the Where field label that is visible to the user. 
*/
public String getWhereLabel()
{	return __where_label;
}

/**
Returns whether the input text field (if a text field is being used for input)
is editable or not.
@return whether the input text field (if a text field is being uesd for input)
is editable or not.
*/
protected boolean isInputJTextFieldEditable() {
	return __inputJTextFieldEditable;
}

/**
Indicate whether a string matches the filter.  This can be used, for example,
to see if the filter input matches a secondary string when applying the
filter manually (e.g., outside of a database).  The filter type is checked and
appropriate comparisons are made.
@param s String to compare to.  Numerical values are converted from the string
(false is returned if a conversion from string to number cannot be made).
@param operator Operator to apply to the filter (usually managed in
InputFilter_JFrame).
@param ignore_case If true, then case is ignored when comparing the strings.
@return true if the string matches the current input for the input filter, or
false otherwise.
*/
public boolean matches ( String s, String operator, boolean ignore_case )
{	String input = getInput ( false );
	if ( __input_type == StringUtil.TYPE_STRING ) {
		if ( operator.equalsIgnoreCase(INPUT_MATCHES) ) {
			// Full string must match...
			if ( ignore_case ) {
				return input.equalsIgnoreCase(s);
			}
			else {	return input.equals(s);
			}
		}
		else if ( operator.equalsIgnoreCase(INPUT_STARTS_WITH) ) {
			if ( ignore_case ) {
			return s.toUpperCase().matches(
					input.toUpperCase() + ".*" );
			}
			else {	return s.matches ( input + ".*" );
			}
		}
		else if ( operator.equalsIgnoreCase(INPUT_ENDS_WITH) ) {
			if ( ignore_case ) {
				return s.toUpperCase().matches(
					".*" + input.toUpperCase() );
			}
			else {	return s.matches ( ".*" + input );
			}
		}
		else if ( operator.equalsIgnoreCase(INPUT_CONTAINS) ) {
			if ( ignore_case ) {
				return s.toUpperCase().matches(
					".*" + input.toUpperCase() + ".*" );
			}
			else {	return s.matches ( ".*" + input + ".*" );
			}
		}
		// Operator not recognized.
		return false;
	}
	else if ( (__input_type == StringUtil.TYPE_INTEGER) &&
		StringUtil.isInteger(s) ) {
		// Use the overloaded method...
		return matches ( StringUtil.atoi(s), operator );
	}
	else if ( (__input_type == StringUtil.TYPE_DOUBLE) &&
		StringUtil.isDouble(s) ) {
		// Use the overloaded method...
		return matches ( StringUtil.atod(s), operator );
	}
	// Data type not recognized...
	return false;
}

/**
Indicate whether an integer matches the filter.  This can be used, for example,
to see if the filter input matches a secondary integer when applying the
filter manually (e.g., outside of a database).  The filter type is checked and
appropriate comparisons are made.
@param i Integer to compare to.  The filter type must be for an integer.
@param operator Operator to apply to the filter (usually managed in
InputFilter_JFrame).
@return true if the integer matches the current input for the input filter, or
false otherwise.
*/
public boolean matches ( int i, String operator )
{	String input = getInput ( false );
	if ( __input_type != StringUtil.TYPE_INTEGER ) {
		return false;
	}
	if ( !StringUtil.isInteger ( input ) ) {
		return false;
	}
	int input_int = StringUtil.atoi(input);
	if ( operator.equalsIgnoreCase(INPUT_EQUALS) ) {
		return (i == input_int);
	}
	else if ( operator.equals(INPUT_LESS_THAN) ) {
		return (i < input_int);
	}
	else if ( operator.equals(INPUT_LESS_THAN_OR_EQUAL_TO) ) {
		return (i <= input_int);
	}
	else if ( operator.equals(INPUT_GREATER_THAN) ) {
		return (i > input_int);
	}
	else if ( operator.equals(INPUT_GREATER_THAN_OR_EQUAL_TO) ) {
		return (i >= input_int);
	}
	// Operator not recognized...
	return false;
}

/**
Indicate whether a double matches the filter.  This can be used, for example,
to see if the filter input matches a secondary double when applying the
filter manually (e.g., outside of a database).  The filter type is checked and
appropriate comparisons are made.
@param d Double to compare to.  The filter type must be for a double.
@param operator Operator to apply to the filter (usually managed in
InputFilter_JFrame).
@return true if the integer matches the current input for the input filter, or
false otherwise.
*/
public boolean matches ( double d, String operator )
{	String input = getInput ( false );
	if ( __input_type != StringUtil.TYPE_DOUBLE ) {
		return false;
	}
	if ( !StringUtil.isDouble ( input ) ) {
		return false;
	}
	double input_double = StringUtil.atod(input);
	if ( operator.equalsIgnoreCase(INPUT_EQUALS) ) {
		return (d == input_double);
	}
	else if ( operator.equals(INPUT_LESS_THAN) ) {
		return (d < input_double);
	}
	else if ( operator.equals(INPUT_LESS_THAN_OR_EQUAL_TO) ) {
		return (d <= input_double);
	}
	else if ( operator.equals(INPUT_GREATER_THAN) ) {
		return (d > input_double);
	}
	else if ( operator.equals(INPUT_GREATER_THAN_OR_EQUAL_TO) ) {
		return (d >= input_double);
	}
	// Operator not recognized...
	return false;
}

/**
Removes a constraint from the constraint combo box.  If the given constraint
does not exist in the combo box, nothing will be done.
@param constraint the constraint (see the INPUT_* data members) to remove
from the constraint combo box.
*/
public void removeConstraint(String constraint) {
	if (__removedConstraints == null) {
		__removedConstraints = new Vector();
	}
	__removedConstraints.add(constraint);
}

/**
Set the input component for the data filter.  This is used by external code
to manage GUI components used for input.  A distinct component should be
availabel for each filter, usuall a JComboBox or a JTextField.
@param input_component the input component.
*/
public void setInputComponent(JComponent input_component) {
	__input_component = input_component;

	if (__input_component != null) {
		__input_component.setToolTipText(__inputComponentToolTipText);
	}
}

/**
Sets the tool tip text for the input component.
@param text the tool tip text for the input component.
*/
public void setInputComponentToolTipText(String text) {
	__inputComponentToolTipText = text;
}

/**
Sets whether the input text field (if a text field is being used) is editable
or not.  By default the text field is editable.  This is only useful in 
limited cases, such as when a value is built in the software in a dialog box
and used to fill the text field.
@param editable whether the input text field is editable or not.
*/
public void setInputJTextFieldEditable(boolean editable) {
	__inputJTextFieldEditable = editable;
}

/**
Sets the width of the input JTextField, in a measurement which roughly 
corresponds to width of a character on the GUI.  Default is 10.
@param width the width to set the input JTextField to, in characters.
*/
public void setInputJTextFieldWidth(int width) {
	__inputJTextFieldWidth = width;
}

/**
Sets the maximum number of rows to display in the input JComboBox for this
InputFilter.  If this InputFilter does not use a JComboBox as an input 
component, then this method will do nothing.  If JCOMBOBOX_ROWS_DISPLAY_ALL 
is passed in for the number of rows, the JComboBox will be sized 
to display all of its rows.
@param num the maximum number of rows to display.  If 
JCOMBOBOX_ROWS_DISPLAY_ALL is passed in for the number of rows, the 
JComboBox will be sized to display all of its rows.
*/
public void setNumberInputJComboBoxRows(int num)  {
	__numInputJComboBoxRows = num;
}

/**
Set the information needed to parse an expanded input choice (e.g.,
"data - note") into tokens so that only the data value can be retrieved.
@param delimiter The characters to be used as delimiters.  Multiple adjacent
delimiters are treated as one delimiter when parsing.
@param token After parsing the input choice using the given delimiter, indicate
the token position for the data value.
*/
public void setTokenInfo ( String delimiter, int token ) {
	__choice_delimiter = delimiter;
	__choice_token = token;
}

} // End of InputFilter
