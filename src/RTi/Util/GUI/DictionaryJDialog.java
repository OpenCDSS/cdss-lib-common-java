// DictionaryJDialog - dialog for editing a dictionary string ("property:value,property:value,...")

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
<p>
This class is a dialog for editing a dictionary string ("property:value,property:value,...").
The updated dictionary string is returned via a response() method call as per the following sample code:
</p>
<code><pre>
	String dict = "Prop1:Value1,Prop2:Value1";
	String dict2 = (new DictionaryJDialog(parentJFrame, true, dict, "Title", "Property", "Value",10)).response();
	if (dict2 == null) {
		// user canceled the dialog
	}
	else {
		// user made changes -- accept them.
	}
</pre></code>
<p>
The dictionary is displayed as scrollable pairs of key and value pairs.
</p>
*/
@SuppressWarnings("serial")
public class DictionaryJDialog extends JDialog implements ActionListener, ItemListener, KeyListener, WindowListener
{

	/**
	 * Possible values for rowHandling.
	 */
	private int DO_BLANK = 1; // Legacy behavior, has issue.
	private int EDIT_IN_PLACE = 2; // New try, does not work because layout gets confused?  Need to evaluate how to remove from GridBagLayout.

	/**
	 * Whether keeping blank rows (true) or deleting based on user action (false).
	 */
	private int rowHandling = DO_BLANK;

	/**
	 * Whether to print debug message.
	 */
	boolean debug = false;

	/**
	Button labels.
	*/
	private final String
    	BUTTON_ADD = "Add",
    	BUTTON_INSERT = "Insert",
    	BUTTON_REMOVE = "Remove",
		BUTTON_OK = "OK",
		BUTTON_CANCEL = "Cancel";

	/**
	CheckBoxes to select/deselect all items.
	This is stored in row 0 of the form.
	All dictionary data rows are 1+.
	*/
	private JCheckBox allCheckBox = null;

	/**
	CheckBoxes to select dictionary items (does not include the 'all' row.
	This list may include empty rows at the end, which won't be returned in output.
	*/
	private ArrayList<JCheckBox> checkBoxList = new ArrayList<>();

	/**
	 * Whether to ignore item events, used when programmatically manipulating UI components.
	 */
	private boolean ignoreItemEvents = false;

	/**
	Labels for row numbers. Labels will be 1+.
	This list may include empty rows at the end, which won't be returned in output.
	*/
	private ArrayList<JLabel> rowNumberLabelList = new ArrayList<>();

	/**
	Component to hold keys from the dictionary.
	This list may include empty rows at the end, which won't be returned in output.
	*/
	private ArrayList<JTextField> keyTextFieldList = new ArrayList<>();

	/**
	Component to hold values from the dictionary.
	This list may include empty rows at the end, which won't be returned in output.
	*/
	private ArrayList<JTextField> valueTextFieldList = new ArrayList<>();

	/**
	 * Major delimiter, separating dictionary entries.
	 */
	private String majorDelim = ",";

	/**
	 * Minor delimiter, separating the parts in entry.
	 */
	private String minorDelim = ":";

	/**
	Dialog buttons.
	*/
	private SimpleJButton
    	addButton = null,
    	insertButton = null,
    	removeButton = null,
		okButton = null,
		cancelButton = null;

	/**
	Scroll panel that manages dictionary entries.
	*/
	private JPanel scrollPanel = null;

	/**
	 * Dictionary string that is returned via response().
	 */
	private String response = null;

	/**
	 * Dictionary string that is passed in.
	 */
	private String dictString = null;

	/**
	 * Title for the dialog.
	 */
	private String title = null;

	/**
	 * Label for the key column.
	 */
	private String keyLabel = null;

	/**
	 * Label for the value column.
	 */
	private String valueLabel = null;

	/**
	 * Notes to show at the top of the dialog.
	 */
	private String [] notes = null;

	/**
	Requested dictionary size.
	*/
	private int initDictSize = 10;

	/**
 	* Whether the number of items in the dictionary can change.
 	*/
	private boolean allowResize = true;

	/**
	Number of rows in the dictionary (some may be blank).
	The initial number is displayed and then the Add button can add more.
	*/
	//private int rowCount = 0;

	/**
	 * Indicates if there is an error in input (true) from checkInput().
	 */
	private boolean error_wait = false;

	/**
	Constructor.  Defaults are major delimiter is a comma and minor delimiter is a colon.
	@param parent the parent JFrame on which the dialog will appear.
	This cannot be null.  If necessary, pass in a new JFrame.
	@param modal whether the dialog is modal.
	@param dictString the dictionary string to edit.  Can be null, in which case <code>response()</code>
	will return a new dictionary string filled with the values entered on the form.
	The left-side key values can contain duplicate values.
	@param title dialog title
	@param notes information to display at the top of the dialog, to help explain the input
	@param keyLabel label above keys
	@param valueLabel label above values
	@param initDictSize initial number of key/value pairs to show.  If negative do not allow additional values.
	If specified as zero, the default size will default to 10 rows.
	*/
	public DictionaryJDialog (
		JFrame parent,
		boolean modal,
		String dictString,
		String title,
		String [] notes,
    	String keyLabel,
    	String valueLabel,
    	int initDictSize ) {
		this (
			parent,
			modal,
			dictString,
			// Default delimiters as per historical.
			",",
			":",
			title,
			notes,
    		keyLabel,
    		valueLabel,
    		initDictSize );
	}

	/**
	Constructor.
	@param parent the parent JFrame on which the dialog will appear.
	This cannot be null.  If necessary, pass in a new JFrame.
	@param modal whether the dialog is modal.
	@param dictString the dictionary string to edit.  Can be null, in which case <code>response()</code>
	will return a new dictionary string filled with the values entered on the form.
	The left-side key values can contain duplicate values.
	@param title dialog title
	@param notes information to display at the top of the dialog, to help explain the input
	@param keyLabel label above keys
	@param valueLabel label above values
	@param initDictSize initial number of key/value pairs to show.  If negative do not allow additional values.
	If specified as zero, the default size will default to 10 rows.
	*/
	public DictionaryJDialog (
		JFrame parent,
		boolean modal,
		String dictString,
		String majorDelim,
		String minorDelim,
		String title,
		String [] notes,
    	String keyLabel,
    	String valueLabel,
    	int initDictSize ) {
		super(parent, modal);

		this.dictString = dictString;
		if ( (majorDelim != null) && !majorDelim.isEmpty() ) {
			this.majorDelim = majorDelim;
		}
		if ( (minorDelim != null) && !minorDelim.isEmpty() ) {
			this.minorDelim = minorDelim;
		}
		this.title = title;
		if ( notes == null ) {
	    	notes = new String[0];
		}
		this.notes = notes;
		this.keyLabel = keyLabel;
		this.valueLabel = valueLabel;
		this.initDictSize = initDictSize;
		if ( this.initDictSize < 0 ) {
			this.initDictSize = -this.initDictSize;
			this.allowResize = false;
		}
		else if ( this.initDictSize == 0 ) {
			// Default to 10 rows for convenience.
			this.initDictSize = 10;
		}
 		setupUI();
	}

	/**
	Responds to ActionEvents.
	@param event ActionEvent object
	*/
	public void actionPerformed(ActionEvent event) {
		String s = event.getActionCommand();

    	if (s.equals(this.BUTTON_ADD)) {
    		addEmptyRow();
    	}
    	else if (s.equals(this.BUTTON_INSERT)) {
			// Insert a new row before the first selected row.
			// If nothing is selected, add at the front.

			// Determine the first selected dictionary data row, 0+.
			int firstSelectedRow = getFirstSelectedRow();
			if ( firstSelectedRow < 0 ) {
				firstSelectedRow = 0;
			}

			// First add an empty row at the end similar to the Add button.
			addEmptyRow ();

			// Shift the rows down by one row to open a new row:
			// - the contents of the rows will be shifted
			shiftRows ( firstSelectedRow, 1 );

			// Set the row numbers for all:
			// - easier to just reset than handle individually
			setRowNumbers();

			this.scrollPanel.revalidate();
			this.scrollPanel.repaint();
    	}
		else if (s.equals(this.BUTTON_REMOVE)) {
			// Remove the selected rows:
			// - work backwards so only items at the end need to be (re)adjusted
			int removeCount = 0;
			int firstSelectedRow = getFirstSelectedRow();
			for ( int i = (this.keyTextFieldList.size() - 1); i >= 0; i-- ) {
				JCheckBox checkBox = this.checkBoxList.get(i);
				JTextField keyTextField = this.keyTextFieldList.get(i);
				JTextField valueTextField = this.valueTextFieldList.get(i);
				JLabel numberLabel = this.rowNumberLabelList.get(i);
				if ( checkBox.isSelected() ) {
					if ( this.rowHandling == this.DO_BLANK ) {
						// Set the content to blank, but don't actually remove the row.
						this.ignoreItemEvents = true;
						checkBox.setSelected(false);
						this.ignoreItemEvents = false;
						// Number is reset later.
						keyTextField.setText("");
						valueTextField.setText("");
					}
					else if ( this.rowHandling == this.EDIT_IN_PLACE ) {
						// Remove the components from the panel and the lists.
						scrollPanel.remove(checkBox);
						this.checkBoxList.remove(checkBox);
						scrollPanel.remove(numberLabel);
						this.rowNumberLabelList.remove(numberLabel);
						scrollPanel.remove(keyTextField);
						this.keyTextFieldList.remove(keyTextField);
						scrollPanel.remove(valueTextField);
						this.valueTextFieldList.remove(valueTextField);
					}

					// Increment the number of rows removed, used below.
					++removeCount;
				}
			}

			// Compress the component lists to remove empty rows:
			// - only need to do once after text fields have been cleared out
			// - do from the earliest item removed because may want to leave an empty row that was previously inserted
			if ( removeCount > 0 ) {
				if ( this.rowHandling == this.DO_BLANK ) {
					// This will set the row numbers.
					compressLists(firstSelectedRow);
				}
				else if ( this.rowHandling == this.EDIT_IN_PLACE ) {
					setRowNumbers();
				}

				// Redraw the UI.
				this.scrollPanel.revalidate();
				this.scrollPanel.repaint();
			}

		}
    	else if (s.equals(this.BUTTON_CANCEL)) {
			response ( false );
		}
		else if (s.equals(this.BUTTON_OK)) {
			checkInputAndCommit ();
			if ( !this.error_wait ) {
				response( true );
			}
		}

		// Check the UI state.
		checkUiState();
	}

	/**
	 * Add a blank row at the end.
	 */
	private void addEmptyRow () {
       	Insets insetsTLBR = new Insets(2,2,2,2);

		// Add a new row at the end:
       	// - row zero is the checkbox for all items so all other rows are numbered 1+.
		int row = this.keyTextFieldList.size() + 1;

		// Checkbox is on the far left.
		JCheckBox checkBox = new JCheckBox();
		checkBox.addItemListener(this);
		this.checkBoxList.add(checkBox);
		JGUIUtil.addComponent(this.scrollPanel, checkBox,
			0, row, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

		// Then the row number 1+
		JLabel label = new JLabel("" + row);
		this.rowNumberLabelList.add(label);
		JGUIUtil.addComponent(this.scrollPanel, label,
			1, row, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

		// Then the text field for the dictionary key.
       	JTextField ktf = new JTextField("",30);
       	this.keyTextFieldList.add(ktf);
       	JGUIUtil.addComponent(this.scrollPanel, ktf,
           	2, row, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

       	// Then the text field for the dictionary value.
       	JTextField vtf = new JTextField("",40);
       	this.valueTextFieldList.add(vtf);
       	JGUIUtil.addComponent(this.scrollPanel, vtf,
           	3, row, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
       	
       	// Invalidate the scrollPanel so the new row is drawn.
       	this.scrollPanel.revalidate();
       	this.scrollPanel.repaint();
	}

	/**
	Check the input.  If errors exist, warn the user and set the __error_wait flag to true.
	This should be called before response() is allowed to complete.
	*/
	private void checkInputAndCommit () {
    	// Previously show all input to user, even if in error, but check before saving.
		this.error_wait = false;

		StringBuffer b = new StringBuffer();
		String reservedChars = majorDelim + minorDelim + "\"";
		String message = "";
    	// Get data from the dialog.
		for ( int i = 0; i < this.keyTextFieldList.size(); i++ ) {
	    	String key = this.keyTextFieldList.get(i).getText().trim();
	    	String value = this.valueTextFieldList.get(i).getText().trim();
	    	// Make sure that the key and value do not contain reserved characters including double quote and the delimiters.
	    	// TODO SAM 2013-09-08 For now see if can parse out intelligently when ${} surrounds property, as in ${TS:property},
	    	// but this is not a generic behavior and needs to be handled without hard-coding.
	    	// Evaluate whether to implement:  It is OK in the value if the value is completely surrounded by single quotes.
	    	if ( StringUtil.containsAny(key, reservedChars, false) ) {
	        	if ( !key.startsWith("${") && !key.endsWith("}") ) {
	            	message += "\n" + this.keyLabel + " (" + key + ") is not ${Property} and contains reserved character(s) " +
	            		reservedChars + "\nSurround with '  ' to protect or [  ] for array.";
	        	}
	    	}
	    	if ( StringUtil.containsAny(value, reservedChars, false) ) {
	        	if ( ((value.charAt(0) != '\'') && (value.charAt(value.length() - 1) != '\'')) &&
	             	((value.charAt(0) != '[') && (value.charAt(value.length() - 1) != ']')) &&
	            	(!value.startsWith("${") && !value.endsWith("}")) ) {
	            	message = "\n" + this.valueLabel + " (" + value + ") is not ${Property} and contains reserved character(s) " +
	            		reservedChars + "\nSurround with '  ' to protect or [  ] for array.";
	        	}
        	}
	    	if ( key.length() > 0 ) {
	        	if ( b.length() > 0 ) {
	            	b.append ( majorDelim );
	        	}
	        	b.append(key + minorDelim + value );
	    	}
		}
		if ( message.length() > 0 ) {
	    	Message.printWarning(1, "", message);
        	this.error_wait = true;
		}
		else {
	    	this.response = b.toString();
		}
	}

	/**
	 * Check the UI state.  Enable/disable buttons as appropriate.
	 * Events are ignored when changing state.
	 */
	public void checkUiState () {
		String routine = getClass().getSimpleName() + ".checkUiState";
		
		if ( this.debug ) {
			Message.printStatus(2, routine, "Number of items in dictionary lists = " + this.checkBoxList.size() );
		}

		// Count of selected checkboxes in dictionary data rows (does not count the "all" row).
		int checkBoxSelectedCount = 0;
		int checkBoxWithTextSelectedCount = 0;
		// Count of keys that have non-empty text.
		int keyWithTextCount = 0;
		int keyWithNoTextCount = 0;

		int listSize = this.checkBoxList.size();
		for ( int i = 0; i < listSize; i++ ) {
			if ( this.checkBoxList.get(i).isSelected() ) {
				++checkBoxSelectedCount;
				if ( ! this.keyTextFieldList.get(i).getText().trim().isEmpty() ) {
					++checkBoxWithTextSelectedCount;
				}
			}
			if ( this.keyTextFieldList.get(i).getText().trim().isEmpty() ) {
				++keyWithNoTextCount;
			}
			else {
				++keyWithTextCount;
			}
		}
		if ( this.debug ) {
			//Message.printStatus(2, routine, "Number of selected items in dictionary = " + checkBoxSelectedCount );
			Message.printStatus(2, routine, "Number of items with non-empty keys = " + keyWithTextCount );
			Message.printStatus(2, routine, "Number of selected checkboxes with non-empty keys = " + checkBoxWithTextSelectedCount );
		}

		if ( (checkBoxWithTextSelectedCount > 0) && (checkBoxWithTextSelectedCount == keyWithTextCount) ) {
			// All the checkboxes with text are selected:
			// - could have been because "all" was selected, resulting all the checkboxes being selected,
			//   or the user individually selected all the checkboxes
			// - set the "all" CheckBox to selected
			// - enable action buttons
			this.ignoreItemEvents = true;
			this.allCheckBox.setSelected(true);
			this.ignoreItemEvents = false;
		}
		else if ( (checkBoxWithTextSelectedCount == 0) || (checkBoxWithTextSelectedCount != keyWithTextCount) ) {
			// No string checkbox is selected or at least one with text is not selected:
			// - set the "all" CheckBox to NOT selected
			// - disable action buttons
			this.ignoreItemEvents = true;
			this.allCheckBox.setSelected(false);
			this.ignoreItemEvents = false;
		}

		if ( this.allCheckBox.isSelected() || (checkBoxSelectedCount > 0) ) {
			// Enable action buttons.
			this.insertButton.setEnabled(true);
			this.removeButton.setEnabled(true);
		}
		else {
			// Disable action buttons.
			this.insertButton.setEnabled(false);
			this.removeButton.setEnabled(false);
		}
	}

	/**
	 * Compress the component lists so empty rows are only at the end.
	 * @param firstRow first row to check (0+)
	 */
	private void compressLists ( int firstRow ) {
		// It does not really matter whether the compression begins at the front or back
		// since two loops are required so process from the front.
		this.ignoreItemEvents = true;
		for ( int i = firstRow; i < this.keyTextFieldList.size(); i++ ) {
			JTextField ktf_i = this.keyTextFieldList.get(i);
			JTextField vtf_i = this.valueTextFieldList.get(i);
			if ( ktf_i.getText().isEmpty() && vtf_i.getText().isEmpty()) {
				// The current row is empty so shift all remaining content forward to this position:
				// - rather than moving components, just change the text field text and checkbox state
				// - if later text fields are also blank, it is not a big hit to just repetitively shift
				for ( int j = (i + 1); j < this.keyTextFieldList.size(); j++ ) {
					// j - 1 components:
					JCheckBox checkbox_jm1 = this.checkBoxList.get(j - 1);
					JTextField ktf_jm1 = this.keyTextFieldList.get(j - 1);
					JTextField vtf_jm1 = this.valueTextFieldList.get(j - 1);
					// j components:
					JCheckBox checkbox_j = this.checkBoxList.get(j);
					JTextField ktf_j = this.keyTextFieldList.get(j);
					JTextField vtf_j = this.valueTextFieldList.get(j);
					// Shift the row.
					checkbox_jm1.setEnabled(checkbox_j.isEnabled());
					ktf_jm1.setText(ktf_j.getText());
					vtf_jm1.setText(vtf_j.getText());
				}
			}
		}
		this.ignoreItemEvents = false;

		// Renumber the rows.
		setRowNumbers();
	}

	/**
	 * Determine the first data dictionary selected row (0+).
	 * The 'all' row is separate.
	 * @return the first data dictionary selected row (0+) or -1 if none are selected
	 */
	private int getFirstSelectedRow () {
		for ( int i = 0; i < this.checkBoxList.size(); i++ ) {
			if ( this.checkBoxList.get(i).isSelected() ) {
				return i;
			}
		}
		return -1;
	}

	/**
	Handle ItemEvent events.
	@param e ItemEvent to handle.
	*/
	public void itemStateChanged ( ItemEvent e ) {
		//String routine = getClass().getSimpleName() + ".itemStateChanged";
		//boolean debug = true;

		if ( this.ignoreItemEvents ) {
			return;
		}
		Object o = e.getSource();
    	if ( o == this.allCheckBox ) {
    		if ( e.getStateChange() == ItemEvent.DESELECTED ) {
    			// Deselect all the checkboxes:
    			// - deselect all keys, even if blank
    			// - temporarily disable item state events
    			this.ignoreItemEvents = true;
    			for ( JCheckBox checkBox : this.checkBoxList ) {
    				checkBox.setSelected(false);
    			}
    			this.ignoreItemEvents = false;
    		}
    		else if ( e.getStateChange() == ItemEvent.SELECTED ) {
    			// Select all the checkboxes:
    			// - only select keys that are not blank
    			// - temporarily disable item state events
    			this.ignoreItemEvents = true;
    			for ( int i = 0; i < this.keyTextFieldList.size(); i++ ) {
    				String key = this.keyTextFieldList.get(i).getText();
    				String value = this.valueTextFieldList.get(i).getText();
    				if ( !key.isEmpty() && !value.isEmpty() ) {
    					JCheckBox checkBox = this.checkBoxList.get(i);
    					checkBox.setSelected(true);
    				}
    			}
    			this.ignoreItemEvents = false;
    		}
    	}
    	else {
    		// The 'checkUIState' method will select/deselect the 'allCheckBox'.
    	}

    	// Check the GUI state so buttons can be enabled/disabled.

    	checkUiState();
	}

	/**
	Does nothing.
	*/
	public void keyPressed(KeyEvent e) {
	}

	/**
	Does nothing.
	*/
	public void keyReleased(KeyEvent e) {
	}

	/**
	Does nothing.
	*/
	public void keyTyped(KeyEvent e) {
	}

	/**
	Return the user response and dispose the dialog.
	@return the dialog response.  If <code>null</code>, the user pressed Cancel.
	*/
	public void response ( boolean ok ) {
		setVisible(false);
		dispose();
		if ( !ok ) {
			this.response = null;
		}
	}

	/**
	Return the user response and dispose the dialog.
	The key values do not need to be unique since handled in a string rather than a hash.
	@return the dialog response in form:  key1:value1,key2,value2
 	If <code>null</code> is returned, the user pressed Cancel.
	*/
	public String response () {
		return this.response;
	}

	/**
	 * Set the row numbers to 1+ sequential.
	 * It is easier to renumber after manipulation than try to individually manipulate.
	 */
	private void setRowNumbers () {
		for ( int i = 0; i < this.rowNumberLabelList.size(); i++ ) {
			this.rowNumberLabelList.get(i).setText("" + (i + 1) );
		}
	}

	/**
	Sets up the GUI.
	*/
	private void setupUI() {
    	if ( this.title != null ) {
        	setTitle(this.title );
    	}

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		getContentPane().add("Center", panel);

		Insets insetsTLBR = new Insets(2,2,2,2);

		// Display the notes.
    	int y = -1;
		for ( int i = 0; i < this.notes.length; i++ ) {
	    	JGUIUtil.addComponent(panel,
	        	new JLabel(this.notes[i]),
	        	0, ++y, 2, 1, 0, 0, insetsTLBR,
	        	GridBagConstraints.NONE, GridBagConstraints.WEST);
		}

		// Parse out the existing dictionary.
		String [] keyList = new String[0];
		String [] valueList = new String[0];
		if ( (this.dictString != null) && (this.dictString.length() > 0) ) {
	    	// Have an existing dictionary string so parse and use to populate the dialog.
	    	String [] dictParts;
	    	if ( dictString.indexOf(majorDelim) < 0 ) {
	        	dictParts = new String[1];
	        	dictParts[0] = dictString;
	    	}
	    	else {
	        	dictParts = this.dictString.split(majorDelim);
	    	}
	    	if ( dictParts != null ) {
    	    	keyList = new String[dictParts.length];
    	    	valueList = new String[dictParts.length];
    	    	for ( int i = 0; i < dictParts.length; i++ ) {
    	        	// Now split the part by :
    	        	// It is possible that the dictionary entry value contains a protected ':' so have to split manually.
    	        	// For example, this is used with Property:${TS:property} to retrieve time series properties
    	        	// or ${TS:property}:Property to set properties.
    	        	int colonPos = dictParts[i].indexOf("}:");
    	        	if ( colonPos > 0 ) {
    	            	// Have a ${property} property in the key.
    	            	++colonPos; // Increment one position since }: is 2 characters.
    	        	}
    	        	else {
    	            	// No ${property} in the key.
    	            	colonPos = dictParts[i].indexOf(minorDelim);
    	        	}
    	        	if ( colonPos >= 0 ) {
  	                	keyList[i] = dictParts[i].substring(0,colonPos).trim();
  	                	if ( colonPos == (dictParts[i].length() - 1) ) {
  	                    	// Colon is at the end of the string.
                        	valueList[i] = "";
  	                	}
  	                	else {
    	                	valueList[i] = dictParts[i].substring(colonPos + 1).trim();
    	            	}
    	        	}
    	        	else {
    	            	keyList[i] = dictParts[i].trim();
    	            	valueList[i] = "";
    	        	}
    	    	}
	    	}
		}

		this.scrollPanel = new JPanel();
		// Don't set preferred size because it seems to mess up the scroll bars (visible but no "thumb").
		//this.scrollPanel.setPreferredSize(new Dimension(600,300));
		this.scrollPanel.setLayout(new GridBagLayout());
    	JGUIUtil.addComponent(panel,
       	new JScrollPane(this.scrollPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS),
       	0, ++y, 2, 1, 1.0, 1.0, insetsTLBR,
       	GridBagConstraints.BOTH, GridBagConstraints.WEST);

    	int yScroll = -1;

    	this.allCheckBox = new JCheckBox();
		JGUIUtil.addComponent(this.scrollPanel,
			this.allCheckBox,
			0, ++yScroll, 1, 1, 0, 0, insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST);
		this.allCheckBox.addItemListener(this);

		JGUIUtil.addComponent(this.scrollPanel,
			new JLabel("#"),
			1, yScroll, 1, 1, 0, 0, insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST);

		JGUIUtil.addComponent(this.scrollPanel,
			new JLabel(this.keyLabel),
			2, yScroll, 1, 1, 0, 0, insetsTLBR,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    	JGUIUtil.addComponent(this.scrollPanel,
        	new JLabel(this.valueLabel),
        	3, yScroll, 1, 1, 0, 0, insetsTLBR,
        	GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    	int size = initDictSize;
		if ( keyList.length > initDictSize ) {
	    	// Increase the initial dictionary size.
	    	size = keyList.length;
		}
    	this.keyTextFieldList = new ArrayList<>(size);
    	this.valueTextFieldList = new ArrayList<>(size);
    	// Add key value pairs.
    	for ( int i = 0; i < size; i++ ) {
        	String key = "";
        	String value = "";
        	if ( i < keyList.length ) {
            	key = keyList[i];
            	value = valueList[i];
        	}

			JCheckBox checkBox = new JCheckBox();
			this.checkBoxList.add(checkBox);
			JGUIUtil.addComponent(this.scrollPanel, checkBox,
				0, ++yScroll, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
			checkBox.addItemListener(this);

			JLabel label = new JLabel("" + (i + 1) );
			this.rowNumberLabelList.add(label);
			JGUIUtil.addComponent(this.scrollPanel, label,
				1, yScroll, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

        	JTextField ktf = new JTextField(key,30);
        	this.keyTextFieldList.add( ktf);
        	JGUIUtil.addComponent(this.scrollPanel, ktf,
            	2, yScroll, 1, 1, 1.0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

        	JTextField vtf = new JTextField(value,40);
        	this.valueTextFieldList.add(vtf);
        	JGUIUtil.addComponent(this.scrollPanel, vtf,
            	3, yScroll, 1, 1, 1.0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    	}

		JPanel south = new JPanel();
		south.setLayout(new FlowLayout(FlowLayout.RIGHT));

		if ( this.allowResize ) {
			// Buttons that are enabled only if the dictionary can have rows added or removed.
			this.addButton = new SimpleJButton(this.BUTTON_ADD, this);
			this.addButton.setToolTipText("Add a new empty row at the bottom of the list.");
			south.add(this.addButton);

			this.insertButton = new SimpleJButton(this.BUTTON_INSERT, this);
			this.insertButton.setToolTipText("Insert a new empty row before the first row that is currenty selected.");
			south.add(this.insertButton);

			this.removeButton = new SimpleJButton(this.BUTTON_REMOVE, this);
			this.removeButton.setToolTipText("Remove the row that is currenty selected.  Empty rows are shifted to the end and will be ignored when saved.");
			south.add(this.removeButton);
		}

		// Standard buttons.
		this.okButton = new SimpleJButton(this.BUTTON_OK, this);
		this.okButton.setToolTipText("Accept any changes that have been made.");
		south.add(this.okButton);

		this.cancelButton = new SimpleJButton(this.BUTTON_CANCEL, this);
		this.cancelButton.setToolTipText("Discard changes.");
		south.add(this.cancelButton);

		getContentPane().add("South", south);
		
		// Check the GUI state.
		checkUiState();

		pack();
		// Set the window size.
		// Otherwise large numbers of items in the dictionary will cause the scrolled panel to
		// be bigger than the screen at startup in some cases.
		if ( this.notes.length <= 7 ) {
			// Default size.
			setSize(650,400);
		}
		else {
			// Add more height to show all the notes and a reasonable number of data rows.
			int fieldHeight = keyTextFieldList.get(0).getHeight();
			int height = 400 + ((notes.length - 7) * fieldHeight);
			if ( height > 800) {
				// Don't let the height be more than 800.
				height = 800;
			}
			setSize(650,height);
		}
		setResizable ( true );
		JGUIUtil.center(this);
		setVisible(true);
		JGUIUtil.center(this);
	}

	/**
	 * Shift row contents to new positions.
	 * Typically this is called after adding a row at the end and shifting some rows to fill that row.
	 * @param firstRowToShift existing first row to shift (1+)
	 * @param shiftCount number of rows to shift (positive means shift down, negative means shift up)
	 */
	private void shiftRows ( int firstRowToShift, int shiftCount ) {
		this.ignoreItemEvents = true;
		if ( shiftCount > 0 ) {
			// Shift down:
			// - work backward so previous contents are retained
			// - can only process as many rows as fit,
			//   which should be OK if add was called to add one at the end and then shift forward by one
			// - data lists are index 0+
			//
			// For example:
			// Row (zero index):
			// 0
			// 1
			// 2 - firstRowToShift (selected row, new row will be above), 
			// 3
			// 4
			// 5 - new row that was just added
			//
			// initial j = 6 - 1 - 1 = 4
			// j >= 
			for ( int j = (this.keyTextFieldList.size() - 1 - shiftCount); j >= firstRowToShift; j-- ) {
				// j - 1 components:
				JCheckBox checkbox_jshift = this.checkBoxList.get(j + shiftCount);
				JTextField ktf_jshift = this.keyTextFieldList.get(j + shiftCount);
				JTextField vtf_jshift = this.valueTextFieldList.get(j + shiftCount);
				// j components:
				JCheckBox checkbox_j = this.checkBoxList.get(j);
				JTextField ktf_j = this.keyTextFieldList.get(j);
				JTextField vtf_j = this.valueTextFieldList.get(j);
				// Shift the row.
				checkbox_jshift.setEnabled(checkbox_j.isEnabled());
				ktf_jshift.setText(ktf_j.getText());
				vtf_jshift.setText(vtf_j.getText());
			}

			// Blank out the rows that were shifted over since essentially new content.
			for ( int j = firstRowToShift; j <= (firstRowToShift + shiftCount - 1); j++ ) {
				JCheckBox checkbox_j = this.checkBoxList.get(j);
				JTextField ktf_j = this.keyTextFieldList.get(j);
				JTextField vtf_j = this.valueTextFieldList.get(j);
				checkbox_j.setSelected(false);
				ktf_j.setText("");
				vtf_j.setText("");
			}

			this.ignoreItemEvents = false;
		}
		else {
			// Negative shift is not yet supported, since not currently used internally.
		}

		// Renumber the rows.
		setRowNumbers();
	}

	/**
	Respond to WindowEvents.
	@param event WindowEvent object.
	*/
	public void windowClosing(WindowEvent event) {
		this.response = null;
		response ( false );
	}

	/**
	Does nothing.
	*/
	public void windowActivated(WindowEvent evt) {
	}

	/**
	Does nothing.
	*/
	public void windowClosed(WindowEvent evt) {
	}

	/**
	Does nothing.
	*/
	public void windowDeactivated(WindowEvent evt) {
	}

	/**
	Does nothing.
	*/
	public void windowDeiconified(WindowEvent evt) {
	}

	/**
	Does nothing.
	*/
	public void windowIconified(WindowEvent evt) {
	}

	/**
	Does nothing.
	*/
	public void windowOpened(WindowEvent evt) {
	}

}