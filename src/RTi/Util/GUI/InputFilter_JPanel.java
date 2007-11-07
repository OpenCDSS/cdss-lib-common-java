//-----------------------------------------------------------------------------
// InputFilter_JPanel - class to display and manage a Vector of InputFilter
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2003-12-18	Steven A. Malers, RTi	Initial version, for use with software
//					that queries databases.
// 2004-05-19	SAM, RTi		Fix bug where a change in the operator
//					JComboBox was not causing the entry
//					component to show the proper component.
//					Actually - after review it seems OK.
// 2004-08-26	SAM, RTi		* Add toString() to return a string
//					  representation of the filter.
//					* Add addEventListener() to allow
//					  higher level code when to refresh
//					  based on a change in the filter panel.
// 2004-08-28	SAM, RTi		To facilitate this class being a base
//					class, modify setInputFilters() to
//					take the list of filters and PropList -
//					then it can be called separately.
// 2004-09-10	SAM, RTi		* Add getInput() method to return the
//					  input that has been entered for a
//					  requested item.
// 2005-01-06	J. Thomas Sapienza, RTi	Fixed bugs that were making it so that
//					single filters could not be displayed in
//					the panel.
// 2004-01-11	JTS, RTi		Added the property 
//					'NumWhereRowsToDisplay'.  If set, it 
//					specifies the number of rows to be 
//					displayed when the drop-down list is
//					opened on one of the combo boxes that
// 					lists all the fields that a Where clause
//					can operated on.
// 2005-01-12	JTS, RTi		Changed slightly the formatting of 
//					errors in checkInput() to be cleaner.
// 2005-01-31	JTS, RTi		* fillOperatorJComboBox() now takes 
//					  another parameter that specifies the
//					  constraints that should NOT appear in
//					  the combo box.
//					* When the input text field is created
//					  its editability is set according to
//					  the value stored in the filter in:
//					  isInputJTextFieldEditable().
// 2005-02-02	JTS, RTi		When the input JComboBoxes are made
//					they now have a parameter to set the 
//					number of visible rows.
// 2005-04-26	JTS, RTi		Added finalize().
// 2006-04-25	SAM, RTi		Minor change to better handle input
//					filter string at initialization - was
//					having problem with ";Matches;".
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

package RTi.Util.GUI;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

// REVISIT SAM 2004-05-20 - see limitation below.
/**
This class provides a JPanel to display a Vector of InputFilter.  The
constructor should indicate the number of input filter groups to be displayed,
where an input filter group lists all input filters.  For example, if three
input filters are specified (in the constructor or later with setInputFilters),
and NumFilterGroups=2 in the constructor properties, then two rows of input will
be shown, each with the "where" listing each input filter.  The
InputFilter_JPanel internally maintains components to properly display the
correct visible componients based on user selections.
There is a limitation in that if an input filter has a list of string choices
and an operator like "Ends with" is chosen, the full list of string choices is
still displayed in the input.  Thefore, a substring cannot be operated on.  This
limitation needs to be removed in later versions of the software if it impacts
functionality.
*/
public class InputFilter_JPanel extends JPanel implements ItemListener
{

private int __num_filter_groups = 0;		// Number of filter groups to
						// display.  One filter group
						// will list all filters.

private Vector [] __input_filter_Vector = null;	// Vector of InputFilter to
						// display.  The original input
						// filter that is supplied is
						// copied for as many input
						// filter groups as necessary,
						// with the InputFilter
						// instances managing the input
						// field components for the
						// specific filter.
	
private Vector __where_component_Vector = new Vector();
						// Vector of JComponent that
						// display the "where" label
						// for each filter group that
						// is displayed.  This may be
						// a SimpleJComboBox or a
						// JLabel, depending on how
						// many filters are available.
						// Each item in the Vector
						// corresponds to a different
						// input filter group.

private Vector __operator_component_Vector = null;
						// Vector of the operator
						// components between the where
						// an input components, one
						// SimpleJComboBox per input
						// filter.
						// Each input filter group has
						// a Vector of operators and the
						// operators are reset as needed
						// in the filter group.

private PropList __props = null;		// Properties to control the
						// display.

/**
Construct an input filter panel.  The setInputFilters() method must be called
at some point during initialization.
*/
public InputFilter_JPanel ()
{	GridBagLayout gbl = new GridBagLayout();
	setLayout ( gbl );
}

/**
Construct an input filter panel.
@param input_filters A Vector of InputFilter, to be displayed.
@param props Properties to control the input filter panel, as described in the
setInputFilters() method.
*/
public InputFilter_JPanel ( Vector input_filters, PropList props )
{	GridBagLayout gbl = new GridBagLayout();
	setLayout ( gbl );
	if ( props == null ) {
		props = new PropList ( "InputFilter" );
	}
	setInputFilters ( input_filters, props );
}

/**
Add listeners for events generated in the input components in the input
filter.  The code must have implemented ItemListener and KeyListener.
@param component The component that wants to listen for events from
InputFilter_JPanel components.
*/
public void addEventListeners ( Component component )
{	// Component is used above (instead of JComponent) because JDialog is
	// not derived from JComponent.
	// Loop through the filter groups...
	boolean is_KeyListener = false;
	boolean is_ItemListener = false;
	if ( component instanceof KeyListener ) {
		is_KeyListener = true;
	}
	if ( component instanceof ItemListener ) {
		is_ItemListener = true;
	}
	SimpleJComboBox cb;
	for ( int ifg = 0; ifg < __num_filter_groups; ifg++ ) {
		// The where...
		if ( is_ItemListener ) {
			if (	__where_component_Vector.elementAt(ifg)
				instanceof SimpleJComboBox ) {
				cb = (SimpleJComboBox)
					__where_component_Vector.elementAt(ifg);
				cb.addItemListener ( (ItemListener)component );
			}
			// The operator...
			cb = (SimpleJComboBox)
				__operator_component_Vector.elementAt(ifg);
			cb.addItemListener ( (ItemListener)component );
		}
		// The input...
		int num_filters = 0;
		if ( __input_filter_Vector[ifg] != null ) {
			num_filters = __input_filter_Vector[ifg].size();
		}
		InputFilter filter = null;
		JComponent input_component;
		JTextField tf;
		for ( int ifilter = 0; ifilter < num_filters; ifilter++ ) {
			filter = (InputFilter)
				__input_filter_Vector[ifg].elementAt(ifilter);
			input_component = filter.getInputComponent();
			if ( input_component instanceof JTextField ) {
				if ( is_KeyListener ) {
					tf = (JTextField)input_component;
					tf.addKeyListener (
						(KeyListener)component );
				}
			}
			else if ( is_ItemListener ) {
				// Combo box...
				cb = (SimpleJComboBox)input_component;
				cb.addItemListener ( (ItemListener)component );
			}
		}
	}
}

/**
Check the input for the current input filter selections.  For example, if an
input filter is for a text field, verify that the contents of the field are
appropriate for the type of input.
@param display_warning If true, display a warning if there are errors in the
input.  If false, do not display a warning (the calling code should generally
display a warning.
@return true if no errors occur or false if there are input errors.
*/
public boolean checkInput ( boolean display_warning )
{	String warning = "\n";
	// Loop through the filter groups...
	InputFilter filter;
	String input;		// Input string selected by user.
	String where;		// Where label for filter selected by user.
	int input_type;		// Input type for the filter.
	for ( int ifg = 0; ifg < __num_filter_groups; ifg++ ) {
		filter = getInputFilter ( ifg );
		where = filter.getWhereLabel();
		if ( where.equals("") ) {
			// Blank where indicates that filter is disabled...
			continue;
		}
		input = filter.getInput(false).trim();
		input_type = filter.getInputType();
		if ( input_type == StringUtil.TYPE_STRING ) {
			// Any limitations?  For now assume not.
		}
		else if ( (input_type == StringUtil.TYPE_DOUBLE) ||
			(input_type == StringUtil.TYPE_FLOAT) ) {
			if ( !StringUtil.isDouble(input) ) {
				warning +=
				"Input filter \"" + filter.getWhereLabel() +
				"\", input is not a number:  \"" + input + "\""
				+ "\n";
			}
		}
		else if	( input_type == StringUtil.TYPE_INTEGER ) {
			if ( !StringUtil.isInteger(input) ) {
				warning +=
				"Input filter \"" + filter.getWhereLabel() +
				"\", input is not an integer:  \""+input + "\""
				+ "\n";
			}
		}
	}
	if ( warning.length() > 1 ) {
		if ( display_warning ) {
			Message.printWarning ( 1,
			"InputFilter_JPanel.checkInput", warning );
		}
		return false;
	}
	else {	return true;
	}
}

/**
Clears all the selections the user has made to the combo boxes in the panel.
*/
public void clearInput() {
	SimpleJComboBox cb = null;
	for ( int ifg = 0; ifg < __num_filter_groups; ifg++ ) {
		if (__where_component_Vector.elementAt(ifg)
			instanceof SimpleJComboBox) {
			cb = (SimpleJComboBox)
				__where_component_Vector.elementAt(ifg);
			cb.select(0);
		}
	}
}

/**
Fill a SimpleJComboBox with the appropriate operators for the data type.
The previous contents are first removed.
@param cb the SimpleJComboBox to fill.
@param type See StringUtil.TYPE_*.
@param constraintsToRemove the constraints that should NOT appear in the 
combo box.  Can be null, if all the appropriate constraints should be shown.
*/
private void fillOperatorJComboBox ( SimpleJComboBox cb, int type,
Vector constraintsToRemove)
{	if ( cb == null ) {
		return;
	}
	// Remove existing...
	cb.removeAll();
	if ( type == StringUtil.TYPE_STRING ) {
		cb.add ( InputFilter.INPUT_MATCHES );
		// REVISIT - add later
		//cb.add ( InputFilter.INPUT_ONE_OF );
		cb.add ( InputFilter.INPUT_STARTS_WITH );
		cb.add ( InputFilter.INPUT_ENDS_WITH );
		cb.add ( InputFilter.INPUT_CONTAINS );
	}
	else if ( (type == StringUtil.TYPE_DOUBLE) ||
		(type == StringUtil.TYPE_FLOAT) ||
		(type == StringUtil.TYPE_INTEGER) ) {
		cb.add ( InputFilter.INPUT_EQUALS );
		// REVISIT - add later
		//cb.add ( InputFilter.INPUT_ONE_OF );
		//cb.add ( InputFilter.INPUT_BETWEEN );
		cb.add ( InputFilter.INPUT_LESS_THAN );
		cb.add ( InputFilter.INPUT_LESS_THAN_OR_EQUAL_TO );
		cb.add ( InputFilter.INPUT_GREATER_THAN );
		cb.add ( InputFilter.INPUT_GREATER_THAN_OR_EQUAL_TO );
	}

	// remove any constraints that have been explicitly set to NOT
	// appear in the combo box.
	if (constraintsToRemove != null) {
		int size = constraintsToRemove.size();
		for (int i = 0; i < size; i++) {
			cb.remove((String)constraintsToRemove.elementAt(i));
		}
	}
	
	if (cb.getItemCount() > 0) {
		// Select the first one...
		cb.select ( 0 );
	}

	// REVISIT - need to handle "NOT" and perhaps null
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	IOUtil.nullArray(__input_filter_Vector);
	__where_component_Vector = null;
	__operator_component_Vector = null;
	__props = null;		
	super.finalize();
}

/**
Return the input that has been entered in the panel, for a requested parameter.
If the requested where_label is not selected in any of the input filters, a zero
length vector will be returned.
Currently only the string input type is enabled.
@return the input that has been entered in the panel, for a requested parameter.
@param where_label The visible label for the input filter.
@param use_wildcards If true, the returned information will be returned using
wildcards, suitable for "matches" calls (e.g., "*inputvalue*").  If false,
the returned information will be returned in verbose format as per the
toString() method (e.g., "contains;inputvalue").
@param delim Delimiter character to use if use_wildcards=false.  See the
toString() method.  If null, use ";".
*/
public Vector getInput ( String where_label, boolean use_wildcards,String delim)
{	Vector input_Vector = new Vector();
	if ( delim == null ) {
		delim = ";";
	}
	InputFilter filter;
	String input;		// Input string selected by user.
	String where;		// Where label for filter selected by user.
	int input_type;		// Input type for the filter.
	for ( int ifg = 0; ifg < __num_filter_groups; ifg++ ) {
		filter = getInputFilter ( ifg );
		where = filter.getWhereLabel();
		if (	!where.equalsIgnoreCase(where_label) ||
			where.equals("") ) {
			// No need to evaluate...
			continue;
		}
		input = filter.getInput(false).trim();
		input_type = filter.getInputType();
		if ( input_type == StringUtil.TYPE_STRING ) {
			if ( use_wildcards ) {
				if ( getOperator(ifg).equals(
					InputFilter.INPUT_MATCHES) ) {
					input_Vector.addElement( input );
				}
				else if ( getOperator(ifg).equals(
					InputFilter.INPUT_CONTAINS) ) {
					input_Vector.addElement("*" +input+"*");
				}
				else if ( getOperator(ifg).equals(
					InputFilter.INPUT_STARTS_WITH) ) {
					input_Vector.addElement( input + "*" );
				}
				else if ( getOperator(ifg).equals(
					InputFilter.INPUT_ENDS_WITH) ) {
					input_Vector.addElement( "*" + input );
				}
			}
			else {	input_Vector.addElement (
					getOperator(ifg) + delim + input );
			}
		}
		/* REVISIT SAM 2004-09-10 Need to enable at some point
		else if ( (input_type == StringUtil.TYPE_DOUBLE) ||
			(input_type == StringUtil.TYPE_FLOAT) ) {
		}
		else if	( input_type == StringUtil.TYPE_INTEGER ) {
		}
		*/
	}
	return input_Vector;
}

/**
Return the current filter for a filter group.
@return the current filter for a filter group.
@param ifg Input filter group.
*/
public InputFilter getInputFilter ( int ifg )
{	int filter_pos = 0;
	if (__where_component_Vector.elementAt(ifg) instanceof SimpleJComboBox){
		// The where lists all the filter where labels so the current
		// filter is given by the position in the combo box...
		SimpleJComboBox cb = (SimpleJComboBox)
			__where_component_Vector.elementAt(ifg);
		filter_pos = cb.getSelectedIndex();
	}
	else {	// A simple JLabel so there is only one item in the filter...
		filter_pos = 0;
	}
	return (InputFilter)__input_filter_Vector[ifg].elementAt(filter_pos);
}

/**
Return the number of filter groups.
@return the number of filter groups.
*/
public int getNumFilterGroups ()
{	return __num_filter_groups;
}

/**
Return the operator for a filter group (one of InputFilter.INPUT_*).
@return the operator for a filter group.
@param ifg Filter group.
*/
public String getOperator ( int ifg )
{	SimpleJComboBox cb =
		(SimpleJComboBox)__operator_component_Vector.elementAt(ifg);
	return cb.getSelected();
}

/**
Handle item events for JComboBox selections.
*/
public void itemStateChanged ( ItemEvent event )
{	Object o = event.getItemSelectable();

	if ( event.getStateChange() != ItemEvent.SELECTED ) {
		// No reason to process the event...
		return;
	}

	InputFilter filter;
	// Loop through the filter groups, checking the where component to find
	// the match...
	SimpleJComboBox where_JComboBox, operator_JComboBox;
	int filter_pos = 0;	// Position of the selected filter in the group.
	int operator_pos = 0;	// Position of the selected operator in the
				// filter group.

	JComponent component = null;
				
	for ( int ifg = 0; ifg < __num_filter_groups; ifg++ ) {
		component = (JComponent)__where_component_Vector.elementAt(ifg);
		if (component instanceof SimpleJComboBox) {
			where_JComboBox = (SimpleJComboBox)component;
		}
		else {
			where_JComboBox = null;
		}

		operator_JComboBox = (SimpleJComboBox)
			__operator_component_Vector.elementAt(ifg);
		if (where_JComboBox != null && o == where_JComboBox ) {
			// Found the component, which indicates which filter
			// group was changed.  Update the operator list.
			// Note that if the original __where_component_Vector
			// item was a JLabel, we would not even be here because
			// no ItemEvent would have been generated.
			//Message.printStatus ( 1, "",
			//"SAMX Found where component: " + ifg +
			//" resetting operators..." );
			// Figure out which filter is selected for the filter
			// group.  Because all groups have the same list of
			// filters, the absolute position will be the same in
			// all the lists.
			filter_pos = where_JComboBox.getSelectedIndex();
			filter =(InputFilter)__input_filter_Vector[ifg].
					elementAt( filter_pos);
			//Message.printStatus ( 1, "", "Where changed." );
			fillOperatorJComboBox ( (SimpleJComboBox)
				__operator_component_Vector.
				elementAt(ifg), filter.getInputType(),
				filter.getConstraintsToRemove());
			// Set the appropriate component visible and all others
			// not visible.  There is an input component for each
			// filter for this filter group...
			operator_pos = 0;
			showInputFilterComponent ( ifg, filter_pos,
				operator_pos );
			// No need to keep searching components...
			break;
		}
		else if ( o == operator_JComboBox ) {
			// Set the appropriate component visible and all others
			// not visible.  There is an input component for each
			// filter for this filter group...
			//filter_pos = getInputFilter();
			// Test...
			//Message.printStatus ( 1, "", "Operator changed." );
			// Figure out which operator...
			if (where_JComboBox == null) {
				showInputFilterComponent ( ifg,
					0,
					operator_JComboBox.getSelectedIndex() );
			}
			else {	
				showInputFilterComponent ( ifg,
					where_JComboBox.getSelectedIndex(),
					operator_JComboBox.getSelectedIndex() );
			}
			// No need to keep searching components...
			break;
		}
	}
}

/**
Remove input filters and related components from the panel.
*/
private void removeInputFilters()
{	int size = 0;
	if ( __input_filter_Vector != null ) {
		size = __input_filter_Vector[0].size();
	}
	InputFilter filter;
	for ( int ifg = 0; ifg < __num_filter_groups; ifg++ ) {
		// Each group contains the same filter information, but using
		// distinct components...
		for ( int ifilter = 0; ifilter < size; ifilter++ ) {
			filter = (InputFilter)
				__input_filter_Vector[ifg].elementAt(ifilter);
			// Remove the input components for the filter...
			filter.setInputComponent(null);
		}
	}
	// Remove all the components from this JPanel...
	removeAll();
	__input_filter_Vector = null;
	__where_component_Vector = null;
	__operator_component_Vector = null;
}

/**
Set the contents of an input filter.
@param ifg The Filter group to be set (0+).
@param input_filter_string The where clause as a string, using visible
information in the input filters:
<pre>
   WhereValue; Operator; InputValue
</pre>
@param delim The delimiter used for the above information.
@exception Exception if there is an error setting the filter data.
*/
public void setInputFilter ( int ifg, String input_filter_string, String delim )
throws Exception
{	Vector v = StringUtil.breakStringList ( input_filter_string, delim, 0 );
	String where = ((String)v.elementAt(0)).trim();
	String operator = ((String)v.elementAt(1)).trim();
	// Sometimes during initialization an ending token may not be provided
	// (e.g., ";Matches;") so handle below...
	String input = "";
	if ( v.size() > 2 ) {
		input = ((String)v.elementAt(2)).trim();
	}

	// Set the where...

	JComponent component;
	component = (JComponent)__where_component_Vector.elementAt ( ifg );
	SimpleJComboBox cb;
	JTextField tf;
	JLabel label;
	if ( component instanceof SimpleJComboBox ) {
		cb = (SimpleJComboBox)component;
		JGUIUtil.selectIgnoreCase ( cb, where );
	}
	else if ( component instanceof JLabel ) {
		label = (JLabel)component;
		label.setText ( where );
	}

	// Set the operator...

	cb = (SimpleJComboBox)
		__operator_component_Vector.elementAt ( ifg );
	JGUIUtil.selectIgnoreCase ( cb, operator );

	// Set the input...

	InputFilter input_filter = getInputFilter ( ifg );
	component = input_filter.getInputComponent();
	if ( component instanceof SimpleJComboBox ) {
		cb = (SimpleJComboBox)component;
		JGUIUtil.selectTokenMatches ( cb, true,
					input_filter.getChoiceDelimiter(),
					0,
					input_filter.getChoiceToken(),
					input, null, true );
	}
	else if ( component instanceof JTextField ) {
		tf = (JTextField)component;
		tf.setText ( input );
	}
}

/**
Set the input filters.  The previous contents of the panel are removed and new
contents are created based on the parameters.
@param input_filters A Vector of InputFilter, containing valid data.  The input
component will be set in each input filter as the GUI is defined.
@param props Properties to control the display of the panel, as described in the
following table.
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	
<td><b>Default</b></td>
</tr>

<tr>
<td><b>NumFilterGroups</b></td>
<td><b>Indicates how many filter groups should be displayed.  
Each group will include all the filters supplied at construction or with a
setInputFilters() call.
<td>1</td>
</tr>

<tr>
<td><b>NumWhereRowsToDisplay</b></td>
<td><b>Indicates the number of rows to be displayed in the drop-down list of
one of the combo boxes that lists the fields for a filter.
<td>(system-dependent JComboBox default)</td>
</tr>

</table>
*/
public void setInputFilters ( Vector input_filters, PropList props )
{	// Make sure non-null properties are available internally...
	if ( props == null ) {
		__props = new PropList ( "InputFilter_JPanel" );
	}
	else {	__props = props;
	}
	// First remove the existing input filters...
	removeInputFilters();
	// Duplicate the input filters for each filter group...
	String prop_val = __props.getValue ( "NumFilterGroups" );
	__num_filter_groups = 0;	// Number of filter groups
	int num_filters = input_filters.size();	// Number of filters in a filter
						// group
	if ( input_filters != null ) {
		num_filters = input_filters.size();
	}
	if ( (input_filters == null) || (input_filters.size() == 0) ) {
		// Only display if we actually have data...
		__num_filter_groups = 0;
	}
	else {	if ( prop_val != null ) {
			// Calling code has requested the number of filters...
			__num_filter_groups = StringUtil.atoi(prop_val);
		}
		else {	// Assume one filter group...
			__num_filter_groups = 1;
		}
	}
	__input_filter_Vector = new Vector[__num_filter_groups];
	InputFilter filter;
	for ( int ifg = 0; ifg < __num_filter_groups; ifg++ ) {
		if ( ifg == 0 ) {
			// Assign the original...
			__input_filter_Vector[0] = input_filters;
		}
		else {	// Copy the original...
			__input_filter_Vector[ifg] = new Vector(num_filters);
			for (	int ifilter = 0;
				ifilter < num_filters; ifilter++ ) {
				filter = (InputFilter)
					input_filters.elementAt(ifilter);
				__input_filter_Vector[ifg].addElement (
					filter.clone() );
			}
		}
	}
	// Now add the new input filters...
	Insets insetsNNNN = new Insets(0,0,0,0);
	int x = 0, y = 0;
	int num = 0;
	// Layout is as follows...
	//
	//      0              1            2         3          4
	//   where_label   operators   ......SimpleJComboBox............
	//
	//				OR...
	//
	//                             JTextField    AND     JTextField
	//
	//				Depending on whether the choices are
	//				provided.
	//
	// where positions 2-4 are used as necessary based on the type of the
	// input.
	__where_component_Vector = new Vector(__num_filter_groups);
	__operator_component_Vector = new Vector(__num_filter_groups);

	String numRowsToDisplayString=__props.getValue("NumWhereRowsToDisplay");
	int numRowsToDisplay = -1;
	if (numRowsToDisplayString != null) {
		numRowsToDisplay = StringUtil.atoi(numRowsToDisplayString);
	}
	
	for ( int ifg = 0; ifg < __num_filter_groups; ifg++, y++ ) {
		x = 0;
		if ( num_filters == 1 ) {
			// Just use a label since the user cannot pick...
			filter = (InputFilter)
				__input_filter_Vector[ifg].elementAt(0);
			JLabel where_JLabel = new JLabel (
				"Where " + filter.getWhereLabel() + ":" );
        		JGUIUtil.addComponent(this, where_JLabel,
				x++, y, 1, 1, 0.0, 0.0, insetsNNNN,
				GridBagConstraints.NONE, GridBagConstraints.EAST);
			__where_component_Vector.addElement ( where_JLabel );
		}
		else {	// Put the labels in a combo box so the user can pick...
        		JGUIUtil.addComponent(this, new JLabel("Where:"),
				x++, y, 1, 1, 0.0, 0.0, insetsNNNN,
				GridBagConstraints.NONE, GridBagConstraints.EAST);
			SimpleJComboBox where_JComboBox =
				new SimpleJComboBox ( false );
			Vector where_Vector = new Vector(num_filters);
			for (	int ifilter = 0; ifilter < num_filters;
				ifilter++ ) {
				filter = (InputFilter)
					__input_filter_Vector[ifg].elementAt(
					ifilter);
				where_Vector.addElement(filter.getWhereLabel());
			}
			where_JComboBox.setData ( where_Vector );
			where_JComboBox.addItemListener ( this );

			if (numRowsToDisplay > -1) {
				where_JComboBox.setMaximumRowCount(
					numRowsToDisplay);
			}
				
        		JGUIUtil.addComponent(this, where_JComboBox,
				x++, y, 1, 1, 0.0, 0.0, insetsNNNN,
				GridBagConstraints.NONE, GridBagConstraints.EAST);
			__where_component_Vector.addElement (where_JComboBox);
		}
		// The operators are reused in the filter group.  Initialize to
		// the first filter...
		filter = (InputFilter)__input_filter_Vector[ifg].elementAt(0);
		// Initialize operators to the first filter.
		// This is reused because it is a simple list
		// based on the current input type.
		SimpleJComboBox operator_JComboBox = new SimpleJComboBox(false);
		fillOperatorJComboBox(operator_JComboBox, filter.getInputType(),
			filter.getConstraintsToRemove());
		operator_JComboBox.addItemListener ( this );
		__operator_component_Vector.addElement ( operator_JComboBox );
		JGUIUtil.addComponent(this, operator_JComboBox,
			x++, y, 1, 1, 0.0, 0.0, insetsNNNN,
			GridBagConstraints.NONE, GridBagConstraints.EAST);
		// Now initialize the components used for input, one component
		// per filter in the group...
		for ( int ifilter = 0; ifilter < num_filters; ifilter++ ) {
			filter =(InputFilter)
				__input_filter_Vector[ifg].elementAt(ifilter);
			if ( filter.getChoiceLabels() == null ) {
				// No choices are provided so use a text
				// field...
				num = filter.getInputJTextFieldWidth();
				JTextField input_JTextField=new JTextField(num);
        			JGUIUtil.addComponent(this, input_JTextField,
					x, y, 1, 1, 0.0, 0.0, insetsNNNN,
					GridBagConstraints.NONE, GridBagConstraints.WEST);
				Vector listeners = 
				       filter.getInputComponentMouseListeners();
				if (listeners != null) {
					int lsize = listeners.size();
					for (int l = 0; l < lsize; l++) {
						input_JTextField
						 	.addMouseListener(
							(MouseListener)
							listeners.elementAt(l));
					}
				}
				// REVISIT - need to be distinct for each
				// group, not shared...
				//Message.printStatus ( 1, "",
				//"SAMX adding text field as input component ");
				input_JTextField.setEditable(
					filter.isInputJTextFieldEditable());
				filter.setInputComponent( input_JTextField);
				if ( ifilter != 0 ) {
					input_JTextField.setVisible ( false );
				}
			}
			else {	// Add the choices...
				SimpleJComboBox input_JComboBox =
					new SimpleJComboBox (
					filter.getChoiceLabels(),
					filter.areChoicesEditable() );
				num = filter.getNumberInputJComboBoxRows();
				if (num > 0) {
					input_JComboBox.setMaximumRowCount(num);
				}
				else if (num 
				    == InputFilter.JCOMBOBOX_ROWS_DISPLAY_DEFAULT) {
				    	// do nothing, display in the default
					// way
				}
				else if (num 
					== InputFilter.JCOMBOBOX_ROWS_DISPLAY_ALL) {
					num = input_JComboBox.getItemCount();
					input_JComboBox.setMaximumRowCount(num);
				}
					
        			JGUIUtil.addComponent(this, input_JComboBox,
					x, y, 1, 1, 0.0, 0.0, insetsNNNN,
					GridBagConstraints.NONE, GridBagConstraints.WEST);
				// REVISIT - need to be distinct for each
				// group, not shared...
				//Message.printStatus ( 1, "",
				//"SAMX adding combo box as input component ");
				filter.setInputComponent( input_JComboBox );
				// Only set the first input component visible...
				if ( ifilter != 0 ) {
					input_JComboBox.setVisible ( false );
				}
				Vector listeners = 
				       filter.getInputComponentMouseListeners();
				if (listeners != null) {
					int lsize = listeners.size();
					for (int l = 0; l < lsize; l++) {
						input_JComboBox
						 	.addMouseListener(
							(MouseListener)
							listeners.elementAt(l));
					}
				}				
			}
		}
	}
}

/**
Show the appropriate input filter component.  This is the component that the
user will enter a value to check for.  It can be either a predefined list
(JComboBox) or a text field, depending on how the InputFilter was intially
defined and depending on the current operator that is selected.
@param ifg the InputFilter group to be updated.
@param filter_pos the input filter component that is currently selected (the
where item).
@param operator_pos the operator item that is currently selected.  The input
component that matches this criteria is set visible and all others are set to
not visible.  In actuality, the input component is the same regardless of the
operator component.  If the input are available as choices, then the operator
will use these choices.  If the input is a text field, then the operator will
require user text.  The only limitation is that for a string input type where
the input choices have been supplied, the user will be limited to only available
strings and will therefore not be able to do substrings.
*/
private void showInputFilterComponent ( int ifg, int filter_pos,
					int operator_pos )
{	int nfilters = __input_filter_Vector[0].size();
	InputFilter filter;		// Input filter to check
	for ( int ifilter = 0; ifilter < nfilters; ifilter++) {
		filter = (InputFilter)
			__input_filter_Vector[ifg].elementAt(ifilter);
		if ( ifilter == filter_pos ) {
			// The input component for the selected
			// filter needs to be visible...
			//Message.printStatus ( 1, "",
			//"SAMX enabling input component " + ifilter );
			((JComponent)
				filter.getInputComponent()).setVisible(true);
		}
		else {	// All other input components should not be visible...
			//Message.printStatus ( 1, "",
			//"SAMX disabling input component " + ifilter );
			((JComponent)
				filter.getInputComponent()).setVisible(false);
		}
	}
}

/**
Return a string representation of an input filter group.  This can be used, for
example, with software that may place a filter in a command.
@param ifg the Input filter group
@param delim Delimiter for the returned filter information.  If null, use ";".
*/
public String toString ( int ifg, String delim )
{	InputFilter filter = getInputFilter ( ifg );
	if ( delim == null ) {
		delim = ";";
	}
	return filter.getWhereLabel() + delim + getOperator(ifg) + delim +
		filter.getInput(false);
}

} // End of InputFilter_JPanel
