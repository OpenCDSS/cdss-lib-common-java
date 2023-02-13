// StringListJDialog - dialog for editing a string list ("string1,string2,...")

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
import java.util.List;

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
This class is a dialog for editing a delimited string list ("string1,string2,...").
The updated delimited string is returned via a response() method call as per the following sample code:
</p>
<code><pre>
	String csv = "string1,string2,string3";
	String csv = (new StringListJDialog(parentJFrame, true, csv, "Title", "Property", ",", 10)).response();
	if (csv == null) {
		// User canceled the dialog.
	}
	else {
		// User made changes -- accept them.
	}
</pre></code>
<p>
The list is displayed a scrollable list of text fields.
*/
@SuppressWarnings("serial")
public class StringListJDialog extends JDialog implements ActionListener, ItemListener, KeyListener, WindowListener {

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
	CheckBoxes to select/deselect all strings.
	*/
	private JCheckBox allCheckBox = null;

	/**
	CheckBoxes to select strings.
	*/
	private ArrayList<JCheckBox> checkBoxList = new ArrayList<>();

	/**
	 * Whether to ignore item events, used when programmatically manipulating UI components.
	 */
	private boolean ignoreItemEvents = false;

	/**
	Labels for string numbers.
	*/
	private ArrayList<JLabel> numberLabelList = new ArrayList<>();

	/**
	Components to hold strings.
	*/
	private ArrayList<JTextField> textFieldList = new ArrayList<>();

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
	 * Delimited string that is returned via response().
	 */
	private String response = null;

	/**
	 * List string that is passed in.
	 */
	private String delimitedString = null;

	/**
	 * Dialog title.
	 */
	private String title = null;

	/**
	 * Label for for the string list column.
	 */
	private String listLabel = null;

	/**
	 * Notes to display at the top of the dialog.
	 */
	private String [] notes = null;

	/**
	Requested list size.
	*/
	private int initListSize = 10;

	/**
 	* Whether the number of items in the dictionary can change.
 	*/
	private boolean allowResize = true;

	/**
	 * Delimiter for the string list.
	 */
	private String delim = ",";

	/**
	 * Indicates if there is an error in input (true) from checkInput().
	 */
	private boolean errorWait = false;

	/**
	Constructor.
	@param parent the parent JFrame on which the dialog will appear.
	This cannot be null.  If necessary, pass in a new JFrame.
	@param modal whether the dialog is modal.
	@param delimitedString the delimited string to edit.  Can be null, in which case <code>response()
	</code> will return a new list string filled with the values entered in the form.
	@param title dialog title
	@param notes information to display at the top of the dialog, to help explain the input
	@param listLabel label above list strings
	@param delim delimiter for the string list
	@param initListSize initial number of items to show. If negative do not allow additional values.
	If specified as zero, the default size will default to 10 rows.
	*/
	public StringListJDialog(JFrame parent, boolean modal, String delimitedString, String title, String [] notes,
    	String listLabel,
    	String delim,
    	int initListSize ) {
		super(parent, modal);

		this.delimitedString = delimitedString;
		this.title = title;
		if ( notes == null ) {
	    	notes = new String[0];
		}
		this.notes = notes;
		this.listLabel = listLabel;
		this.initListSize = initListSize;
		if ( this.initListSize < 0 ) {
			this.initListSize = -this.initListSize;
			this.allowResize = false;
		}
		else if ( this.initListSize == 0 ) {
			// Default to 10 rows for convenience.
			this.initListSize = 10;
		}
		if ( (delim != null) && !delim.isEmpty() ) {
			this.delim = delim;
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
			addRow();

			this.scrollPanel.revalidate();
		}
		else if (s.equals(this.BUTTON_INSERT)) {
			// Insert a new row before the first selected row.
			// If nothing is selected, add at the front.

			// Determine the first selected row.
			int firstSelectedRow = getFirstSelectedRow();
			if ( firstSelectedRow < 0 ) {
				firstSelectedRow = 0;
			}

			// First add a row at the end similar to the Add button.
			addRow ();

			// Shift the rows to open a new row
			shiftRows ( firstSelectedRow, 1 );

			// Set the row numbers for all:
			// - easier to just reset than handle individually
			setRowNumbers();

			this.scrollPanel.revalidate();
		}
		else if (s.equals(this.BUTTON_REMOVE)) {
			// Remove the selected rows:
			// - work backwards so only items at the end need to be (re)adjusted
			int removeCount = 0;
			int firstSelectedRow = getFirstSelectedRow();
			for ( int i = (this.textFieldList.size() - 1); i >= 0; i-- ) {
				JCheckBox checkBox = this.checkBoxList.get(i);
				JTextField textField = this.textFieldList.get(i);
				if ( checkBox.isSelected() ) {
					// Set the content to blank.
					this.ignoreItemEvents = true;
					checkBox.setSelected(false);
					this.ignoreItemEvents = false;
					textField.setText("");
					
					// Increment the number of rows removed, used below.
					++removeCount;
				}
			}

			// Compress the component lists to remove empty rows:
			// - only need to do once after text fields have been cleared out
			// - do from the earliest item removed because may want to leave an empty row that was previously inserted
			if ( removeCount > 0 ) {
				compressLists(firstSelectedRow);
			}
		}
		else if (s.equals(this.BUTTON_CANCEL)) {
			response ( false );
		}
		else if (s.equals(this.BUTTON_OK)) {
			checkInputAndCommit ();
			if ( !this.errorWait ) {
				response( true );
			}
		}
		
		// Check the UI state.
		checkUiState();
	}

	/**
	 * Add a blank row at the end.
	 */
	private void addRow () {
		Insets insetsTLBR = new Insets(2,2,2,2);

		// Add a new row at the end.
		int row = this.textFieldList.size() + 1;

		JCheckBox checkBox = new JCheckBox();
		this.checkBoxList.add(checkBox);
		JGUIUtil.addComponent(this.scrollPanel, checkBox,
			0, row, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

		JLabel label = new JLabel("" + row);
		this.numberLabelList.add(label);
		JGUIUtil.addComponent(this.scrollPanel, label,
			1, row, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

		JTextField tf = new JTextField("",30);
		this.textFieldList.add(tf);
		JGUIUtil.addComponent(this.scrollPanel, tf,
			2, row, 1, 1, 1.0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
		this.scrollPanel.revalidate();
	}

	/**
	 * Check the UI state.  Enable/disable buttons as appropriate.
	 */
	public void checkUiState () {
		//String routine = getClass().getSimpleName() + ".checkUiState";
		//boolean debug = true;

		int checkBoxSelectedCount = 0;

		for ( JCheckBox checkBox : this.checkBoxList ) {
			if ( checkBox.isSelected() ) {
				++checkBoxSelectedCount;
			}
		}

		if ( checkBoxSelectedCount > 0 ) {
			// At least one string checkbox is selected:
			// - set the "all" CheckBox to selected
			// - enable action buttons
			this.ignoreItemEvents = true;
			//this.allCheckBox.setSelected(true);
			this.ignoreItemEvents = false;

			// Enable action buttons.
			//this.insertButton.setEnabled(true);
			this.removeButton.setEnabled(true);
		}
		else {
			// No string checkbox is selected:
			// - set the "all" CheckBox to NOT selected
			// - disable action buttons
			this.ignoreItemEvents = true;
			this.allCheckBox.setSelected(false);
			this.ignoreItemEvents = false;

			// Disable action buttons.
			//this.insertButton.setEnabled(false);
			this.removeButton.setEnabled(false);
		}
	}

	/**
	Check the input.  If errors exist, warn the user and set the __error_wait flag to true.
	This should be called before response() is allowed to complete.
	*/
	private void checkInputAndCommit () {
    	// Previously show all input to user, even if in error, but check before saving.
		this.errorWait = false;

		StringBuffer b = new StringBuffer();
		String chars = ":,\"";
		String message = "";
    	// Get data from the dialog.
		for ( int i = 0; i < this.textFieldList.size(); i++ ) {
	    	String string = this.textFieldList.get(i).getText().trim();
	    	// Make sure that the string does not contain special characters :,"
	    	// TODO smalers 2013-09-08 For now see if can parse out intelligently when ${} surrounds property, as in ${TS:property},
	    	// but this is not a generic behavior and needs to be handled without hard-coding.
	    	// Evaluate whether to implement:  It is OK in the value if the value is completely surrounded by single quotes.
	    	if ( StringUtil.containsAny(string, chars, false) ) {
	        	if ( !string.startsWith("${") && !string.endsWith("}") ) {
	            	message += "\n" + this.listLabel + " (" + string + ") is not ${Property} and contains special character(s) " +
	            		chars + "\nSurround with '  ' to protect or [  ] for array.";
	        	}
	    	}
	    	if ( string.length() > 0 ) {
	        	if ( b.length() > 0 ) {
	            	b.append ( "," );
	        	}
	        	b.append(string);
	    	}
		}
		if ( message.length() > 0 ) {
	    	Message.printWarning(1, "", message);
        	this.errorWait = true;
		}
		else {
	    	this.response = b.toString();
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
		for ( int i = firstRow; i < this.textFieldList.size(); i++ ) {
			JTextField tf_i = this.textFieldList.get(i);
			if ( tf_i.getText().isEmpty() ) {
				// The current row is empty so shift all remaining content forward to this position:
				// - rather than moving components, just change the text field text and checkbox state
				// - if later text fields are also blank, it is not a big hit to just repetitively shift
				for ( int j = (i + 1); j < this.textFieldList.size(); j++ ) {
					// j - 1 components:
					JCheckBox checkbox_jm1 = this.checkBoxList.get(j - 1);
					JTextField tf_jm1 = this.textFieldList.get(j - 1);
					// j components:
					JCheckBox checkbox_j = this.checkBoxList.get(j);
					JTextField tf_j = this.textFieldList.get(j);
					// Shift the row.
					checkbox_jm1.setEnabled(checkbox_j.isEnabled());
					tf_jm1.setText(tf_j.getText());
				}
			}
		}
		this.ignoreItemEvents = false;

		// Renumber the rows.
		setRowNumbers();
	}
	
	/**
	 * Determine the first selected row (0+).
	 * @return the first selected row (0+) or -1 if none are selected
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
    			for ( int i = 0; i < this.textFieldList.size(); i++ ) {
    				String string = this.textFieldList.get(i).getText();
    				if ( !string.isEmpty() ) {
    					JCheckBox checkBox = this.checkBoxList.get(i);
    					checkBox.setSelected(true);
    				}
    			}
    			this.ignoreItemEvents = false;
    		}
    	}

    	// Check the GUI state so buttons can be enabled/disabled.

    	checkUiState();
	}

	/**
	Does nothing.
	*/
	public void keyPressed(KeyEvent e) {
		// Call just in case other code gets into a bad state.
		this.removeButton.setEnabled(false);
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
		for ( int i = 0; i < this.numberLabelList.size(); i++ ) {
			this.numberLabelList.get(i).setText("" + (i + 1) );
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

		// Parse out the existing list.
    	List<String> stringList = new ArrayList<>();
		if ( (this.delimitedString != null) && !delimitedString.isEmpty() ) {
	    	// Have an existing list string so parse and use to populate the dialog.
	    	if ( this.delimitedString.indexOf(",") < 0 ) {
	    		// Single part.
	    		stringList.add(this.delimitedString);
	    	}
	    	else {
	    		// Break the string:
	    		// - allow quoted strings, in particular single quotes around strings that include double quotes
	        	stringList = StringUtil.breakStringList(this.delimitedString, this.delim, StringUtil.DELIM_ALLOW_STRINGS_RETAIN_QUOTES);
	    	}
		}
		this.scrollPanel = new JPanel();
		// Don't set preferred size because it seems to mess up the scroll bars (visible but no "thumb").
		//this.scrollPanel.setPreferredSize(new Dimension(600,300));
		this.scrollPanel.setLayout(new GridBagLayout());
    	JGUIUtil.addComponent(panel,
    		new JScrollPane(this.scrollPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS),
    		0, ++y, 2, 1, 1.0, 1.0, insetsTLBR,
    		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

    	// Add the column headings as labels.
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
			new JLabel(this.listLabel),
			2, yScroll, 1, 1, 1.0, 0, insetsTLBR,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    	// Add the column contents:
		// - first column is a sequential number 1+ (no resize)
		// - second column is the string (resizes horizontally)
		// - rows is either the requested initial size or big enough to accommodate the string list
		int size = this.initListSize;
		if ( stringList.size() > this.initListSize ) {
			size = stringList.size();
		}
		this.textFieldList = new ArrayList<>(size);
    	// Add list items.
    	for ( int i = 0; i < size; i++ ) {
        	String string = ""; // Default value.
        	if ( i < stringList.size() ) {
        		// String from the delimited list.
            	string = stringList.get(i);
        	}

			JCheckBox checkBox = new JCheckBox();
			this.checkBoxList.add(checkBox);
			JGUIUtil.addComponent(this.scrollPanel, checkBox,
				0, ++yScroll, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
			checkBox.addItemListener(this);

			JLabel label = new JLabel("" + (i + 1) );
			this.numberLabelList.add(label);
			JGUIUtil.addComponent(this.scrollPanel, label,
				1, yScroll, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

        	JTextField tf = new JTextField(string,30);
        	this.textFieldList.add( tf);
        	JGUIUtil.addComponent(this.scrollPanel, tf,
            	2, yScroll, 1, 1, 1.0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    	}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		if ( this.allowResize ) {
			// Optional buttons if the list is allowed to change size from the initial.
			this.addButton = new SimpleJButton(this.BUTTON_ADD, this);
			this.addButton.setToolTipText("Add a new empty row at the bottom of the list.");
			buttonPanel.add(this.addButton);

			this.insertButton = new SimpleJButton(this.BUTTON_INSERT, this);
			this.insertButton.setToolTipText("Insert a new empty row before the row that is currenty selected.");
			buttonPanel.add(this.insertButton);

			this.removeButton = new SimpleJButton(this.BUTTON_REMOVE, this);
			this.removeButton.setToolTipText("Remove the row(s) that are currenty selected.");
			buttonPanel.add(this.removeButton);
		}

		// Standard buttons
		this.okButton = new SimpleJButton(this.BUTTON_OK, this);
		this.okButton.setToolTipText("Accept any changes that have been made.");
		buttonPanel.add(this.okButton);

		this.cancelButton = new SimpleJButton(this.BUTTON_CANCEL, this);
		this.cancelButton.setToolTipText("Discard changes.");
		buttonPanel.add(this.cancelButton);

		// Check the UI state so buttons are enabled appropriately.
		checkUiState();

		// Add the button panel to the layout.
		getContentPane().add("South", buttonPanel);

		pack();
		// Set the window size.  Otherwise large numbers of items in the dictionary will cause the scrolled panel to
		// be bigger than the screen at startup in some cases.
		//setSize(650,400);
		setResizable ( true );
		JGUIUtil.center(this);
		setVisible(true);
		JGUIUtil.center(this);
	}

	/**
	 * Shift row contents to new positions.
	 * Typically this is called after adding a row at the end and shifting some rows to fill that row.
	 * @param firstRowToShift existing first row to shift
	 * @param shiftCount number of rows to shift (positive means shift later, negative means shift earlier)
	 */
	private void shiftRows ( int firstRowToShift, int shiftCount ) {
		this.ignoreItemEvents = true;
		if ( shiftCount > 0 ) {
			// Shift forward:
			// - go backward so previous contents are retained
			// - can only process as many rows as fit,
			//   which should be OK if add was called to add one at the end and then shift forward by one
			for ( int j = (this.textFieldList.size() - 1 - shiftCount); j >= firstRowToShift; j-- ) {
				// j - 1 components:
				JCheckBox checkbox_jshift = this.checkBoxList.get(j + shiftCount);
				JTextField tf_jshift = this.textFieldList.get(j + shiftCount);
				// j components:
				JCheckBox checkbox_j = this.checkBoxList.get(j);
				JTextField tf_j = this.textFieldList.get(j);
				// Shift the row.
				checkbox_jshift.setEnabled(checkbox_j.isEnabled());
				tf_jshift.setText(tf_j.getText());
			}

			// Blank out the rows that were shifted over since essentially new content.
			for ( int j = firstRowToShift; j <= (firstRowToShift + shiftCount - 1); j++ ) {
				JCheckBox checkbox_j = this.checkBoxList.get(j);
				JTextField tf_j = this.textFieldList.get(j);
				checkbox_j.setSelected(false);
				tf_j.setText("");
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