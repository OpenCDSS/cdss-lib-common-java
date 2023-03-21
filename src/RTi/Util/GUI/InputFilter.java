// InputFilter - class to handle an input filter "Where...Is"

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

import java.awt.event.MouseListener;

import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTextField;

import RTi.Util.String.StringUtil;

// TODO SAM 2011-01-09 Need to add validator to each filter, via an interface:
// - can color code entry field
// - when does validation occur so as to not be irritating
// - is it passive with tooltip or active with popup?

// TODO SAM 2011-01-09 Need a way to limit the criteria where all choices do not work, for example,
// if "Matches" is the only string choice.

/**
This class provides a way to define an input filter,
for example for use in a GUI as a "[field] [constraint] [input] choice.
[field] is the data field that will be constrained.
[constraint] is the way the data field will be matched against (see the INPUT_* data members).
[input] is the value to constrain the field against.

In most cases, a list of these objects is created as appropriate by
specific software and is then managed by a InputFilter_JPanel object,
typically a class derived from InputFilter_JPanel.
*/
public class InputFilter implements Cloneable {

	/**
	Input filter type, for use with strings that exactly match a pattern.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_MATCHES = "Matches";

	/**
	Input filter type, for use with strings and numbers that match a case in a list.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_ONE_OF = "One of";

	/**
	Input filter type, for use with strings starting with a pattern.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_STARTS_WITH = "Starts with";

	/**
	Input filter type, for use with strings ending with a pattern.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_ENDS_WITH = "Ends with";

	/**
	Input filter type, for use with strings containing a pattern.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_CONTAINS = "Contains";

	/**
	Input filter type, for use with numbers that exactly match.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_EQUALS = "=";
	/**
	Legacy INPUT_EQUALS, phasing out.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_EQUALS_LEGACY = "Equals";

	/**
	Input filter type, for use with numbers that are between two values.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_BETWEEN = "Is between";

	/**
	Input filter type, for use with strings that are null or empty.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_IS_EMPTY = "Is empty";

	/**
	Input filter type, for use with numbers that are less than a value.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_LESS_THAN = "<";
	/**
	Legacy INPUT_LESS_THAN, phasing out.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_LESS_THAN_LEGACY = "Less than";

	/**
	Input filter type, for use with numbers that are less than or equal to a value.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_LESS_THAN_OR_EQUAL_TO = "<=";

	/**
	Input filter type, for use with numbers that are greater than a value.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_GREATER_THAN = ">";
	/**
	Legacy INPUT_GREATER_THAN, phasing out.
	@deprecated use InputFilterCriterionType
	*/
	public final static String INPUT_GREATER_THAN_LEGACY = "Greater than";

	/**
	Input filter type, for use with numbers that are greater than or equal to a value.
	@deprecated use InputFilterCriterionType
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

/**
The string that is internally used in a where clause (e.g., a database table column: "table.column").
*/
private String __whereInternal = "";

/**
The string that is internally used in a where clause (e.g., a database table column: "table.column").
This secondary where string can be used in case of times where in certain instances a different thing is queried against.
For instance, the first where string could be used for SQL queries and the second
where string could be used when querying against fields in a database view.
*/
private String __whereInternal2 = "";

/**
The string label to be visible to the user (e.g., "Station Name").
*/
private String __whereLabel="";

/**
The string label used in persistent forms such as command parameters,
often with minimal formatting (e.g., "StationName").
*/
private String __whereLabelPersistent ="";

/**
A value from RTi.Util.String.StringUtil.TYPE_*, indicating the expected input value.
The type is used to check the validity of the input with checkInput().
*/
private int __inputType = 0;

/**
A list of String choices to choose from.  If not null a JComboBox will be displayed to let the user choose from.
*/
private List<String> __choiceLabelList = null;

/**
The internal values (e.g., database column values) corresponding to the visible choices.
Always provide as strings but should ultimately match the column type.
*/
private List<String> __choiceInternalList = null;

/**
Used when the choices are not simple strings but contain informational notes.
For example "Station - Streamflow" would have a delimiter of "-".
The surrounding spaces are typically handled by UI code.
*/
private String __choiceDelimiter = null;

/**
Used when the choices are not simple strings but contain informational notes.
Index (relatative to 0) for the token position containing choice data for selection.
For example "Station - Streamflow" would use a value of 1 to indicate that "Streamflow" is the data to be used in the choice.
*/
private int __choiceToken = 0;

/**
Used with __choiceToken to indicate the type of the token for comparison purposes (see StringUtil.TYPE_*).
Default to unknown, meaning use the primary input type.
*/
private int __choiceTokenType = -1;

/**
The component used to enter input, typically assigned by external code like InputFilter_JPanel.
*/
private JComponent __inputComponent = null;

/**
If true, the JComboBox used with __choicesList will be editable
(usually choices are pre-defined and should not be editable, hence the default value of false).
*/
private boolean __areChoicesEditable = false;

/**
If the input component is going to be a text field,
this can be set to false to set the text field uneditable
(this is only useful in very limited circumstances and is why the default is true).
*/
private boolean __inputJTextFieldEditable = true;

/**
The width of the input JTextField (if used) on the GUI, roughly in characters.
*/
private int __inputJTextFieldWidth = 10;

/**
Specifies how many rows of values to show at one time in a GUI.
Possible values are JCOMBOBOX_ROWS_DISPLAY_ALL, JCOMBOBOX_ROWS_DISPLAY_DEFAULT, or a positive integer.
*/
private int __numInputJComboBoxRows = JCOMBOBOX_ROWS_DISPLAY_DEFAULT;

/**
If any tool tip text is defined for the InputFilter, it is stored here.  Otherwise, this value is null.
*/
private String __inputComponentToolTipText = null;

/**
The listeners that want to listen to mouse events on the input component.
*/
private List<MouseListener> __inputComponentMouseListeners = null;

/**
If not null, contains all the constraints that should NOT appear in the constraint combo box for this InputFilter.
*/
private List<String> __removedConstraints = null;

/**
Construct an input filter.
@param whereLabel A string to be listed in a choice to tell the user what input parameter is being filtered.
A blank string can be specified to indicate that the filter can be disabled.
@param whereInternal The internal ("persistent") value that can be used to perform a query.
For example, set to a database table and column ("table.column").
This can be set to null or empty if not used by other software.
@param inputType The input filter data type, see RTi.Util.String.StringUtil.TYPE_*.
@param choiceLabels A list of String containing choice values to be displayed to the user.
If null, the user will not be shown a list of choices.
@param choicesInternal A list of String containing choice values (e.g., database column values).
@param areChoicesEditable If true, and a non-null list of choices is provided,
the choices will also be editable (an editable JTextField part of the JComboBox will be shown).
*/
public InputFilter ( String whereLabel, String whereInternal,
	int inputType, List<String> choiceLabels, List<String> choicesInternal, boolean areChoicesEditable ) {
	this (
		whereLabel,
		whereInternal,
		"", // whereInternal2
		"", // whereLabelPersistent
		inputType,
		choiceLabels,
		choicesInternal,
		areChoicesEditable,
		null);
}

/**
Construct an input filter.
@param whereLabel A string to be listed in a choice to tell the user what input parameter is being filtered.
A blank string can be specified to indicate that the filter can be disabled.
@param whereInternal The internal ("persistent") value that can be used to perform a query.
For example, set to a database table and column ("table.column").
This can be set to null or empty if not used by other software.
@param whereInternal2 the internal value that can be used if in certain
cases a different field must be used for performing a query.
For instance, if a database can be used to query with SQL or to query against a database view.
It can be set to null or an empty string if it won't be used by software.
@param inputType The input filter data type, see RTi.Util.String.StringUtil.TYPE_*.
@param choiceLabels A list of String containing choice values to be displayed to the user.
If null, the user will not be shown a list of choices.
@param choicesInternal A list of String containing choice values (e.g., database column values).
Always provide strings even if the database column is integer, etc.
@param areChoicesEditable If true, and a non-null list of choices is provided,
the choices will also be editable (an editable JTextField part of the JComboBox will be shown).
*/
public InputFilter ( String whereLabel, String whereInternal, String whereInternal2, int inputType,
    List<String> choiceLabels, List<String> choicesInternal, boolean areChoicesEditable ) {
    this(
    	whereLabel,
    	whereInternal,
    	whereInternal2,
    	"", // whereLabelPersistent
    	inputType,
    	choiceLabels,
    	choicesInternal,
        areChoicesEditable,
        null);
}

/**
Construct an input filter.
@param whereLabel A string to be listed in a choice to tell the user what input parameter is being filtered.
A blank string can be specified to indicate that the filter can be disabled.
@param whereInternal The internal ("persistent") value that can be used to perform a query.
For example, set to a database table and column ("table.column").
This can be set to null or empty if not used by other software.
@param whereInternal2 the internal value that can be used if in certain
cases a different field must be used for performing a query.
For instance, if a database can be used to query with SQL or to query against a database view.
It can be set to null or an empty string if it won't be used by software.
@param whereLabelPersistent the where label to use for persistent forms such as command parameter,
useful when the displayed where label is too verbose or has punctuation that is prone to errors,
and when the internal labels may be redundant or confusing to users.
@param inputType The input filter data type, see RTi.Util.String.StringUtil.TYPE_*.
@param choiceLabels A list of String containing choice values to be displayed to the user.
If null, the user will not be shown a list of choices.
@param choicesInternal A list of String containing choice values (e.g., database column values).
Always provide strings even if the database column is integer, etc.
@param areChoicesEditable If true, and a non-null list of choices is provided,
the choices will also be editable (an editable JTextField part of the JComboBox will be shown).
*/
public InputFilter ( String whereLabel, String whereInternal, String whereInternal2, String whereLabelPersistent, int inputType,
    List<String> choiceLabels, List<String> choicesInternal, boolean areChoicesEditable ) {
    this(
    	whereLabel,
    	whereInternal,
    	whereInternal2,
    	whereLabelPersistent,
    	inputType,
    	choiceLabels,
    	choicesInternal,
        areChoicesEditable,
        null);
}

/**
Construct an input filter.
@param whereLabel A string to be listed in a choice to tell the user what input parameter is being filtered.
A blank string can be specified to indicate that the filter can be disabled.
@param whereInternal The internal value that can be used to perform a query.
For example, set to a database table and column ("table.column").
This can be set to null or empty if not used by other software.
@param whereInternal2 the internal value that can be used if in certain
cases a different field must be used for performing a query.
For instance, if a database can be used to query with SQL or to query against a database view.
It can be set to null or an empty string if it won't be used by software.
@param inputType The input filter data type, see RTi.Util.String.StringUtil.TYPE_*.
@param choiceLabels A list of String containing choice values to be displayed to the user.
If null, the user will not be shown a list of choices.
@param choicesInternal A list of String containing choice values (e.g., database column values).
@param areChoicesEditable If true, and a non-null list of choices is provided,
the choices will also be editable (an editable JTextField part of the JComboBox will be shown).
@param inputComponentToolTipText tool tip text for the input component.
*/
public InputFilter ( String whereLabel, String whereInternal, String whereInternal2, int inputType,
	List<String> choiceLabels, List<String> choicesInternal, boolean areChoicesEditable,
	String inputComponentToolTipText ) {
    this(
    	whereLabel,
    	whereInternal,
    	whereInternal2,
    	"", // whereLabelPersistent
    	inputType,
    	choiceLabels,
    	choicesInternal,
        areChoicesEditable,
        inputComponentToolTipText);
}

/**
Construct an input filter.
@param whereLabel A string to be listed in a choice to tell the user what input parameter is being filtered.
A blank string can be specified to indicate that the filter can be disabled.
@param whereInternal The internal ("persistent") value that can be used to perform a query.
For example, set to a database table and column ("table.column").
This can be set to null or empty if not used by other software.
@param whereInternal2 the internal value that can be used if in certain
cases a different field must be used for performing a query.
For instance, if a database can be used to query with SQL or to query against a database view.
It can be set to null or an empty string if it won't be used by software.
@param whereLabelPersistent the where label to use for persistent forms such as command parameter,
useful when the displayed where label is too verbose or has punctuation that is prone to errors,
and when the internal labels may be redundant or confusing to users.
This should be specified to control what is used in TSTool command parameters and then use
InputFilter_JPanel.toString(valuePos=3).
@param inputType The input filter data type, see RTi.Util.String.StringUtil.TYPE_*.
@param choiceLabels A list of String containing choice values to be displayed to the user.
If null, the user will not be shown a list of choices.
@param choicesInternal A list of String containing choice values (e.g., database column values).
@param areChoicesEditable If true, and a non-null list of choices is provided,
the choices will also be editable (an editable JTextField part of the JComboBox will be shown).
@param inputComponentToolTipText tool tip text for the input component.
*/
public InputFilter ( String whereLabel, String whereInternal, String whereInternal2, String whereLabelPersistent,
	int inputType, List<String> choiceLabels, List<String> choicesInternal, boolean areChoicesEditable,
	String inputComponentToolTipText ) {
	__whereLabel = whereLabel;
	__whereInternal = whereInternal;
	__whereInternal2 = whereInternal2;
	__whereLabelPersistent = whereLabelPersistent;
	__inputType = inputType;
	__choiceLabelList = choiceLabels;
	__choiceInternalList = choicesInternal;
	__areChoicesEditable = areChoicesEditable;
	//__toolTipText = toolTipText;
	setInputComponentToolTipText(inputComponentToolTipText);
}

/**
Adds a mouse listener to the input field.
This listener will be notified of any mouse events on the Input component.
@param listener the listener to add to the input component.
*/
public void addInputComponentMouseListener(MouseListener listener) {
	if (__inputComponentMouseListeners == null) {
		__inputComponentMouseListeners = new Vector<MouseListener>();
	}
	__inputComponentMouseListeners.add(listener);
}

/**
Indicate whether the input choices are editable.
If true, the user should be able to type in a value in addition to using the choices.
This is only used when choices are provided.
*/
public boolean areChoicesEditable() {
	return __areChoicesEditable;
}

/**
Clone the object.
The Object base class clone() method is called and then the data members are cloned.
The result is a complete deep copy.
The only exception is that the input component is set to null - it normally needs to be
defined by calling code after the clone() call occurs.
*/
public Object clone() {
	try {
        // Clone the base class.
		InputFilter filter = (InputFilter)super.clone();
		// Now clone the mutable objects.
		// The following clone automatically because they are primitives:
		//
		// __whereInternal
		// __whereLabel
		// __inputType
		// __areChoicesEditable
		// __choiceDelimiter
		// __choiceTtoken

		// Do not clone the __inputCcomponent because it will be set in calling code.

		filter.__inputComponent = null;

		// Copy the contents of the lists.

		if ( __choiceLabelList != null ) {
			int size = __choiceLabelList.size();
			filter.__choiceLabelList = new Vector<>( size );
			for ( int i = 0; i < size; i++ ) {
				filter.__choiceLabelList.add ( __choiceLabelList.get(i) );
			}
		}
		if ( __choiceInternalList != null ) {
			int size = __choiceInternalList.size();
			filter.__choiceInternalList = new Vector<>( size );
			for ( int i = 0; i < size; i++ ) {
				filter.__choiceInternalList.add ( __choiceInternalList.get(i) );
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
Evaluate whether a string object meets a string criterion.
@param s string being evaluated (e.g., "does s criterion s2"?)
@param criteria criteria being used in comparison
@param s2 string that is being used to match the criterion
*/
public static boolean evaluateCriterion ( String s, InputFilterStringCriterionType criterion, String s2 ) {
    if ( criterion == InputFilterStringCriterionType.CONTAINS ) {
        int index = StringUtil.indexOfIgnoreCase(s, s2, 0);
        if ( index >= 0 ) {
            return true;
        }
        else {
            return false;
        }
    }
    else if ( criterion == InputFilterStringCriterionType.ENDS_WITH ) {
        return StringUtil.endsWithIgnoreCase(s,s2);
    }
    else if ( criterion == InputFilterStringCriterionType.MATCHES ) {
        return s.equalsIgnoreCase(s2);
    }
    else if ( criterion == InputFilterStringCriterionType.STARTS_WITH ) {
        return StringUtil.startsWithIgnoreCase(s,s2);
    }
    return false;
}

/**
Return the choices that are visible to the user.
@return the choices that are visible to the user.
*/
public List<String> getChoiceLabels() {
	return __choiceLabelList;
}

/**
Return the delimiter used in choices, which is used to separate actual data from descriptive information.
@return the delimiter used in choices.
*/
public String getChoiceDelimiter () {
	return __choiceDelimiter;
}

/**
Return the token position used in choices, which is the position of actual data, compared to descriptive information.
@return the token position used in choices (0+).
*/
public int getChoiceToken () {
	return __choiceToken;
}

/**
Return the type of the choice token, which is used to evaluate the criteria.
*/
public int getChoiceTokenType () {
	return __choiceTokenType;
}

/**
Returns the constraints that should not appear in the constraint combo box for this InputFilter.
@return the constraints that should not appear in the constraint combo box for this InputFilter.
*/
protected List<String> getConstraintsToRemove() {
	return __removedConstraints;
}

/**
Return the user-supplied input, as entered in the input component.
If the input component is a SimpleJComboBox,
the internal value is returned if not blank and the input choice is not blank.
@return the user-supplied input, as a string, or null if the input component is null.
@param return_full If true, then full input is returned, which may include informational comments.
If false, only the specific data token is returned,
requiring that setTokenInfo() be called for the filter.
*/
public String getInput ( boolean return_full ) {
	if ( __inputComponent == null ) {
		return null;
	}
	else if ( __inputComponent instanceof SimpleJComboBox ) {
		SimpleJComboBox cb = (SimpleJComboBox)__inputComponent;
		if ( return_full || (__choiceDelimiter == null) ) {
			// Input choices are not formatted with additional information.
			return cb.getSelected();
		}
		else {
			// Input choices are formatted with additional information (e.g., "data - note").
		    // Return a token from the input choices.
			return StringUtil.getToken(cb.getSelected(), __choiceDelimiter,
				StringUtil.DELIM_SKIP_BLANKS, __choiceToken );
		}
	}
	else if ( __inputComponent instanceof JTextField ) {
		return ((JTextField)__inputComponent).getText();
	}
	return null;
}

/**
Return the input component for the data filter.
This is used by external code to manage GUI components used for input.
A distinct component should be available for each filter, usually a JComboBox or a JTextField.
@return the input component.
*/
public JComponent getInputComponent() {
	return __inputComponent;
}

/**
Returns the list of mouse listeners for the input component.  May be null if none have been set.
@return the list of mouse listeners for the input component.
*/
public List<MouseListener> getInputComponentMouseListeners() {
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
Return the input using internal notation.
If the input component is a SimpleJComboBox, the internal input is returned.
If the input component is a JTextField, the visible input is returned.
@return the input using internal notation.
*/
public String getInputInternal () {
	if ( __inputComponent instanceof SimpleJComboBox ) {
		SimpleJComboBox cb = (SimpleJComboBox)__inputComponent;
		// Get the token index that should be used.
		int pos = cb.getSelectedIndex();
		if (pos == -1) {
			// ComboBox item was not selected so return the text field value.
			return cb.getFieldText();
		}
		else {
			// Combobox item was selected so return the value at the index.
			return __choiceInternalList.get(pos);
		}
	}
	else {
	    // JTextField.
		return getInput ( true );
	}
}

/**
Returns the width of the input JTextField, roughly in characters.  Default is 10.
@return the width of the input JTextField.
*/
protected int getInputJTextFieldWidth() {
	return __inputJTextFieldWidth;
}

/**
Return the input type (one of StringUtil.TYPE_*).
@return the input type (one of StringUtil.TYPE_*).
*/
public int getInputType() {
	return __inputType;
}

/**
Returns the maximum number of rows to display for this InputFilter's input JComboBox.
Possible values are JCOMBOBOX_ROWS_DISPLAY_ALL,
JCOMBOBOX_ROWS_DISPLAY_DEFAULT and positive integers.
@return the maximum number of rows to display for this InputFilter's input JComboBox.
*/
protected int getNumberInputJComboBoxRows() {
	return __numInputJComboBoxRows;
}

/**
Return the internal Where field that corresponds to the visible label.
@return the internal Where field that corresponds to the visible label.
*/
public String getWhereInternal () {
	return __whereInternal;
}

/**
Returns the secondary internal Where field that corresponds to the visible label.
If no secondary internal where field has been defined, then the
primary internal where field is returned (identical to calling getWhereInternal()).
@return the secondary internal Where field that corresponds to the visible label.
*/
public String getWhereInternal2() {
	if (__whereInternal2 != null && !__whereInternal2.equals("")) {
		return __whereInternal2;
	}
	else {
		return getWhereInternal();
	}
}

/**
Return the Where field label that is visible to the user, such as user interfaces.
@return the Where field label that is visible to the user, such as user interfaces.
*/
public String getWhereLabel()
{	return __whereLabel;
}

/**
Return the Where field label that is used in persistent forms, such as command parameter.
@return the Where field label that is used in persistent forms, such as command parameter.
*/
public String getWhereLabelPersistent()
{	return __whereLabelPersistent;
}

/**
Returns whether the input text field (if a text field is being used for input) is editable or not.
@return whether the input text field (if a text field is being used for input) is editable or not.
*/
protected boolean isInputJTextFieldEditable() {
	return __inputJTextFieldEditable;
}

/**
Indicate whether a string matches the filter. This can be used, for example,
to see if the filter input matches a secondary string when applying the filter manually (e.g., outside of a database).
The filter type is checked and appropriate comparisons are made.
@param s String to compare to.  Numerical values are converted from the string
(false is returned if a conversion from string to number cannot be made).
@param operator Operator to apply to the filter (usually managed in InputFilter_JPanel).
@param ignore_case If true, then case is ignored when comparing the strings.
@return true if the string matches the current input for the input filter, or false otherwise.
*/
public boolean matches ( String s, String operator, boolean ignore_case ) {
	String input = getInput ( false );
	if ( __inputType == StringUtil.TYPE_STRING ) {
		if ( operator.equalsIgnoreCase(InputFilterCriterionType.INPUT_MATCHES.toString()) ) {
			// Full string must match.
			if ( ignore_case ) {
				return input.equalsIgnoreCase(s);
			}
			else {
			    return input.equals(s);
			}
		}
		else if ( operator.equalsIgnoreCase(InputFilterCriterionType.INPUT_STARTS_WITH.toString()) ) {
			if ( ignore_case ) {
			    return s.toUpperCase().matches(input.toUpperCase() + ".*" );
			}
			else {
			    return s.matches ( input + ".*" );
			}
		}
		else if ( operator.equalsIgnoreCase(InputFilterCriterionType.INPUT_ENDS_WITH.toString()) ) {
			if ( ignore_case ) {
				return s.toUpperCase().matches( ".*" + input.toUpperCase() );
			}
			else {
			    return s.matches ( ".*" + input );
			}
		}
		else if ( operator.equalsIgnoreCase(InputFilterCriterionType.INPUT_CONTAINS.toString()) ) {
			if ( ignore_case ) {
				return s.toUpperCase().matches( ".*" + input.toUpperCase() + ".*" );
			}
			else {
			    return s.matches ( ".*" + input + ".*" );
			}
		}
		// Operator not recognized.
		return false;
	}
	else if ( (__inputType == StringUtil.TYPE_INTEGER) && StringUtil.isInteger(s) ) {
		// Use the overloaded method.
		return matches ( StringUtil.atoi(s), operator );
	}
	else if ( (__inputType == StringUtil.TYPE_DOUBLE) && StringUtil.isDouble(s) ) {
		// Use the overloaded method.
		return matches ( StringUtil.atod(s), operator );
	}
	// Data type not recognized.
	return false;
}

/**
Indicate whether an integer matches the filter.  This can be used, for example,
to see if the filter input matches a secondary integer when applying the filter manually (e.g., outside of a database).
The filter type is checked and appropriate comparisons are made.
@param i Integer to compare to.  The filter type must be for an integer.
@param operator Operator to apply to the filter (usually managed in InputFilter_JFrame).
@return true if the integer matches the current input for the input filter, or false otherwise.
*/
public boolean matches ( int i, String operator ) {
	String input = getInput ( false );
	if ( __inputType != StringUtil.TYPE_INTEGER ) {
		return false;
	}
	if ( !StringUtil.isInteger ( input ) ) {
		return false;
	}
	int input_int = StringUtil.atoi(input);
	if ( operator.equals(InputFilterCriterionType.INPUT_EQUALS.toString()) ||
		operator.equalsIgnoreCase(InputFilterCriterionType.INPUT_EQUALS_LEGACY.toString()) ) {
		return (i == input_int);
	}
	else if ( operator.equals(InputFilterCriterionType.INPUT_LESS_THAN.toString()) ||
		operator.equalsIgnoreCase(InputFilterCriterionType.INPUT_LESS_THAN_LEGACY.toString()) ) {
		return (i < input_int);
	}
	else if ( operator.equals(InputFilterCriterionType.INPUT_LESS_THAN_OR_EQUAL_TO.toString()) ) {
		return (i <= input_int);
	}
	else if ( operator.equals(InputFilterCriterionType.INPUT_GREATER_THAN.toString()) ||
		operator.equalsIgnoreCase(InputFilterCriterionType.INPUT_GREATER_THAN_LEGACY.toString()) ) {
		return (i > input_int);
	}
	else if ( operator.equals(InputFilterCriterionType.INPUT_GREATER_THAN_OR_EQUAL_TO.toString()) ) {
		return (i >= input_int);
	}
	// Operator not recognized.
	return false;
}

/**
Indicate whether a double matches the filter.  This can be used, for example,
to see if the filter input matches a secondary double when applying the
filter manually (e.g., outside of a database).
The filter type is checked and appropriate comparisons are made.
@param d Double to compare to.  The filter type must be for a double.
@param operator Operator to apply to the filter (usually managed in InputFilter_JFrame).
@return true if the integer matches the current input for the input filter, or false otherwise.
*/
public boolean matches ( double d, String operator ) {
	String input = getInput ( false );
	if ( __inputType != StringUtil.TYPE_DOUBLE ) {
		return false;
	}
	if ( !StringUtil.isDouble ( input ) ) {
		return false;
	}
	double input_double = StringUtil.atod(input);
	if ( operator.equals(InputFilterCriterionType.INPUT_EQUALS.toString()) ||
		operator.equalsIgnoreCase(InputFilterCriterionType.INPUT_EQUALS_LEGACY.toString()) ) {
		return (d == input_double);
	}
	else if ( operator.equals(InputFilterCriterionType.INPUT_LESS_THAN.toString()) ||
		operator.equalsIgnoreCase(InputFilterCriterionType.INPUT_LESS_THAN_LEGACY.toString())) {
		return (d < input_double);
	}
	else if ( operator.equals(InputFilterCriterionType.INPUT_LESS_THAN_OR_EQUAL_TO.toString()) ) {
		return (d <= input_double);
	}
	else if ( operator.equals(InputFilterCriterionType.INPUT_GREATER_THAN.toString()) ||
		operator.equalsIgnoreCase(InputFilterCriterionType.INPUT_GREATER_THAN_LEGACY.toString())) {
		return (d > input_double);
	}
	else if ( operator.equals(InputFilterCriterionType.INPUT_GREATER_THAN_OR_EQUAL_TO.toString()) ) {
		return (d >= input_double);
	}
	// Operator not recognized.
	return false;
}

/**
Removes a constraint from the constraint combo box.
If the given constraint does not exist in the combo box, nothing will be done.
This is useful, for example, if only equals is to be enabled for numbers and not <, >, etc.,
for example for web service queries.
@param constraint the constraint (see the INPUT_* data members) to remove from the constraint combo box.
*/
public void removeConstraint(String constraint) {
	if (__removedConstraints == null) {
		__removedConstraints = new Vector<>();
	}
	__removedConstraints.add(constraint);
}

/**
Set the choices available to a filter.
This can be called after initialization to change the list of choices,
for example, based on dynamically selected information.
@param choiceLabels A list of String containing choice values to be
displayed to the user.  If null, the user will not be shown a list of choices.
@param choicesInternal A list of String containing choice values (e.g., database column values).
@param areChoicesEditable If true, and a non-null list of choices is provided,
the choices will also be editable (an editable JTextField part of the JComboBox will be shown).
*/
public void setChoices ( List<String> choiceLabels, List<String> choicesInternal, boolean areChoicesEditable )
{   // Clear the list and add the new list so that GUI components that use this class as the data model
    // retain the same references.
    this.__choiceLabelList.clear();
  	this.__choiceLabelList.addAll(choiceLabels);
    if ( this.__choiceInternalList != null ) {
    	// The internal list may not have been set originally.
    	this.__choiceInternalList.clear();
    }
    if ( choicesInternal != null ) {
    	// Have internal choices to set.
    	if ( this.__choiceInternalList == null ) {
    		// The internal choices were not originally allocated so allocate now.
    		this.__choiceInternalList = new Vector<>();
    	}
    	this.__choiceInternalList.addAll(choicesInternal);
    }
    this.__areChoicesEditable = areChoicesEditable;
}

/**
Set the input component for the data filter.
This is used by external code to manage GUI components used for input.
A distinct component should be available for each filter, usually a JComboBox or a JTextField.
@param input_component the input component.
*/
public void setInputComponent(JComponent input_component) {
	__inputComponent = input_component;

	if (__inputComponent != null) {
		__inputComponent.setToolTipText(__inputComponentToolTipText);
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
Sets whether the input text field (if a text field is being used) is editable or not.
By default the text field is editable.
This is only useful in limited cases, such as when a value is built in the software in a dialog box
and used to fill the text field.
@param editable whether the input text field is editable or not.
*/
public void setInputJTextFieldEditable(boolean editable) {
	__inputJTextFieldEditable = editable;
}

/**
Sets the width of the input JTextField, in a measurement which roughly corresponds to width of a character on the GUI.
Default is 10.
@param width the width to set the input JTextField to, in characters.
*/
public void setInputJTextFieldWidth(int width) {
	__inputJTextFieldWidth = width;
}

/**
Sets the maximum number of rows to display in the input JComboBox for this InputFilter.
If this InputFilter does not use a JComboBox as an input component, then this method will do nothing.
If JCOMBOBOX_ROWS_DISPLAY_ALL is passed in for the number of rows,
the JComboBox will be sized to display all of its rows.
@param num the maximum number of rows to display.
If JCOMBOBOX_ROWS_DISPLAY_ALL is passed in for the number of rows,
the JComboBox will be sized to display all of its rows.
*/
public void setNumberInputJComboBoxRows(int num) {
	__numInputJComboBoxRows = num;
}

/**
Set the information needed to parse an expanded input choice (e.g., "data - note")
into tokens so that only the data value can be retrieved.
It is assumed that the extracted token is a string.
@param delimiter The characters to be used as delimiters.
Multiple adjacent delimiters are treated as one delimiter when parsing.
@param token After parsing the input choice using the given delimiter,
indicate the token position for the data value.
*/
public void setTokenInfo ( String delimiter, int token ) {
	// Token type defaults to 0, meaning use the primary input type.
	setTokenInfo ( delimiter, token, -1 );
}

/**
Set the information needed to parse an expanded input choice (e.g.,
"data - note") into tokens so that only the data value can be retrieved.
@param delimiter The characters to be used as delimiters.
Multiple adjacent delimiters are treated as one delimiter when parsing.
@param token After parsing the input choice using the given delimiter,
indicate the token position for the data value.
@param tokenType the type of the token for comparison, StringUtil.TYPE_*.
For example, a string choice can be "Value - Note", where the initial filter type is string.
However, the choices should actually be appropriate for the token type, for example integer.
*/
public void setTokenInfo ( String delimiter, int token, int tokenType ) {
	__choiceDelimiter = delimiter;
	__choiceToken = token;
	__choiceTokenType = tokenType;
}

}