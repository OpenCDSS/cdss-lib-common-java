// DictionaryJDialog - dialog for editing a dictionary string ("property:value,property:value,...")

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2022 Colorado Department of Natural Resources

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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

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
public class DictionaryJDialog extends JDialog implements ActionListener, KeyListener, MouseListener, WindowListener
{

/**
Button labels.
*/
private final String
    BUTTON_INSERT = "Insert",
    BUTTON_ADD = "Add",
    BUTTON_REMOVE = "Remove",
	BUTTON_CANCEL = "Cancel",
	BUTTON_OK = "OK";

/**
Components to hold values from the TSIdent.
*/
private ArrayList<JTextField> keyTextFieldList = new ArrayList<>();
private ArrayList<JTextField> valueTextFieldList = new ArrayList<>();

/**
Text field row (0-index) where 0=first (top) row in entry fields.
This is used to let the Insert functionality know where to insert.
*/
private int selectedTextFieldRow = -1;

/**
Dialog buttons.
*/
private SimpleJButton
    insertButton = null,
    addButton = null,
    removeButton = null,
	cancelButton = null,
	okButton = null;

/**
Scroll panel that manages dictionary entries.
*/
private JPanel scrollPanel = null;

private String 
	response = null, // Dictionary string that is returned via response().
	dictString = null, // Dictionary string that is passed in.
	title = null,
	keyLabel = null,
	valueLabel = null;

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
private int rowCount = 0;

private boolean error_wait = false; // Indicates if there is an error in input (true) from checkInput().

/**
Constructor.
@param parent the parent JFrame on which the dialog will appear.
This cannot be null.  If necessary, pass in a new JFrame.
@param modal whether the dialog is modal.
@param dictString the dictionary string to edit.  Can be null, in which case <code>response()
</code> will return a new dictionary string filled with the values entered on the form.
The left-side key values can contain duplicate values.
@param title dialog title
@param notes information to display at the top of the dialog, to help explain the input
@param keyLabel label above keys
@param valueLabel label above values
@param initDictSize initial number of key/value pairs to show - if negative do not allow additional values
*/
public DictionaryJDialog(JFrame parent, boolean modal, String dictString, String title, String [] notes,
    String keyLabel, String valueLabel,
    int initDictSize )
{	super(parent, modal);

	this.dictString = dictString;
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
 	setupUI();
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	String s = event.getActionCommand();

    if (s.equals(this.BUTTON_ADD)) {
        Insets insetsTLBR = new Insets(2,2,2,2);
        // Add a new row.  Rows are 1+ because the column names are in the first row.
        ++this.rowCount;
        JTextField ktf = new JTextField("",30);
        ktf.addMouseListener(this);
        this.keyTextFieldList.add(ktf);
        JGUIUtil.addComponent(this.scrollPanel, ktf,
            0, this.rowCount, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        JTextField vtf = new JTextField("",40);
        this.valueTextFieldList.add(vtf);
        JGUIUtil.addComponent(this.scrollPanel, vtf,
            1, this.rowCount, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        this.scrollPanel.revalidate();
    }
    else if (s.equals(this.BUTTON_INSERT)) {
        Insets insetsTLBR = new Insets(2,2,2,2);
        // Insert a new row before the row that was last selected.  Rows are 1+ because the column names are in the first row.
        if ( this.keyTextFieldList.size() == 0 ) {
            // Add at the end.
            ++this.rowCount;
            JTextField ktf = new JTextField("",30);
            this.keyTextFieldList.add(ktf);
            JGUIUtil.addComponent(this.scrollPanel, ktf,
                0, this.rowCount, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
            JTextField vtf = new JTextField("",40);
            this.valueTextFieldList.add(vtf);
            JGUIUtil.addComponent(this.scrollPanel, vtf,
                1, this.rowCount, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        }
        else {
            // Insert before the selected.
            if ( this.selectedTextFieldRow < 0 ) {
                // Nothing previously selected so reset.
                this.selectedTextFieldRow = 0;
            }
            // First loop through all the rows after the new and shift later in the grid bag.
            for ( int i = selectedTextFieldRow; i < this.keyTextFieldList.size(); i++ ) {
                // FIXME SAM 2014-03-02 TOO MUCH WORK - come back and fix this later - for not disable the Insert.
            }
            // Now add the new text field.
            JTextField ktf = new JTextField("",30);
            this.keyTextFieldList.add(this.selectedTextFieldRow,ktf);
            JGUIUtil.addComponent(this.scrollPanel, ktf,
                0, (this.selectedTextFieldRow + 1), 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
            JTextField vtf = new JTextField("",40);
            this.valueTextFieldList.add(this.selectedTextFieldRow,vtf);
            JGUIUtil.addComponent(this.scrollPanel, vtf,
                1, (this.selectedTextFieldRow + 1), 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
            ++this.rowCount;
        }

        this.scrollPanel.revalidate();
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
}

public void mouseClicked (MouseEvent e ) {
    setSelectedTextField(e.getComponent());
}

public void mouseEntered (MouseEvent e ) {
    
}

public void mouseExited (MouseEvent e ) {
    
}

public void mousePressed (MouseEvent e ) {
    setSelectedTextField(e.getComponent());
}

public void mouseReleased (MouseEvent e ) {
    
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag to true.
This should be called before response() is allowed to complete.
*/
private void checkInputAndCommit () {	
    // Previously show all input to user, even if in error, but check before saving.
	this.error_wait = false;
	
	StringBuffer b = new StringBuffer();
	String chars = ":,\"";
	String message = "";
    // Get data from the dialog.
	for ( int i = 0; i < this.keyTextFieldList.size(); i++ ) {
	    String key = this.keyTextFieldList.get(i).getText().trim();
	    String value = this.valueTextFieldList.get(i).getText().trim();
	    // Make sure that the key and value do not contain special characters :,"
	    // TODO SAM 2013-09-08 For now see if can parse out intelligently when ${} surrounds property, as in ${TS:property},
	    // but this is not a generic behavior and needs to be handled without hard-coding.
	    // Evaluate whether to implement:  It is OK in the value if the value is completely surrounded by single quotes.
	    if ( StringUtil.containsAny(key, chars, false) ) {
	        if ( !key.startsWith("${") && !key.endsWith("}") ) {
	            message += "\n" + this.keyLabel + " (" + key + ") is not ${Property} and contains special character(s) " +
	            	chars + "\nSurround with '  ' to protect or [  ] for array.";
	        }
	    }
	    if ( StringUtil.containsAny(value, chars, false) ) {
	        if ( ((value.charAt(0) != '\'') && (value.charAt(value.length() - 1) != '\'')) &&
	             ((value.charAt(0) != '[') && (value.charAt(value.length() - 1) != ']')) &&
	            (!value.startsWith("${") && !value.endsWith("}")) ) {
	            message = "\n" + this.valueLabel + " (" + value + ") is not ${Property} and contains special character(s) " +
	            	chars + "\nSurround with '  ' to protect or [  ] for array.";
	        }
        }
	    if ( key.length() > 0 ) {
	        if ( b.length() > 0 ) {
	            b.append ( "," );
	        }
	        b.append(key + ":" + value );
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

// TODO SAM 2014-03-02 This will need more work if controls are added to delete or re-order text fields.
/**
Set the selected text field, which indicates which row has been clicked on.
*/
private void setSelectedTextField ( Component c ) {
    // Figure out which of the text fields were selected and save the index.
    for ( int i = 0; i < keyTextFieldList.size(); i++ ) {
        if ( c == keyTextFieldList.get(i) ) {
            this.selectedTextFieldRow = i;
            return;
        }
    }
    for ( int i = 0; i < valueTextFieldList.size(); i++ ) {
        if ( c == valueTextFieldList.get(i) ) {
            this.selectedTextFieldRow = i;
            return;
        }
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
	    if ( dictString.indexOf(",") < 0 ) {
	        dictParts = new String[1];
	        dictParts[0] = dictString;
	    }
	    else {
	        dictParts = this.dictString.split(",");
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
    	            colonPos = dictParts[i].indexOf(":");
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
	if ( keyList.length > initDictSize ) {
	    // Increase the initial dictionary size.
	    initDictSize = keyList.length;
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
	JGUIUtil.addComponent(this.scrollPanel, 
		new JLabel(this.keyLabel),
		0, ++yScroll, 1, 1, 0, 0, insetsTLBR,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(this.scrollPanel, 
        new JLabel(this.valueLabel),
        1, yScroll, 1, 1, 0, 0, insetsTLBR,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    this.keyTextFieldList = new ArrayList<>(initDictSize);
    this.valueTextFieldList = new ArrayList<>(initDictSize);
    // Add key value pairs.
    for ( int i = 0; i < this.initDictSize; i++ ) {
        String key = "";
        String value = "";
        if ( i < keyList.length ) {
            key = keyList[i];
            value = valueList[i];
        }
        JTextField ktf = new JTextField(key,30);
        this.keyTextFieldList.add( ktf);
        JGUIUtil.addComponent(this.scrollPanel, ktf,
            0, ++yScroll, 1, 1, 1.00, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        JTextField vtf = new JTextField(value,40);
        this.valueTextFieldList.add(vtf);
        JGUIUtil.addComponent(this.scrollPanel, vtf,
            1, yScroll, 1, 1, 1.0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        ++this.rowCount;
    }
	
	JPanel south = new JPanel();
	south.setLayout(new FlowLayout(FlowLayout.RIGHT));

	if ( this.allowResize ) {
		this.insertButton = new SimpleJButton(this.BUTTON_INSERT, this);
		this.insertButton.setToolTipText("Insert a new row before the row that is currenty selected.");
		this.insertButton.setEnabled(false);
		this.addButton = new SimpleJButton(this.BUTTON_ADD, this);
		this.addButton.setToolTipText("Add a new row at the bottom of the list.");
		this.removeButton = new SimpleJButton(this.BUTTON_REMOVE, this);
		this.removeButton.setToolTipText("Remove the row that is currenty selected.");
		this.removeButton.setEnabled(false);
	}
	this.okButton = new SimpleJButton(this.BUTTON_OK, this);
	this.okButton.setToolTipText("Accept any changes that have been made.");
	this.cancelButton = new SimpleJButton(this.BUTTON_CANCEL, this);
	this.cancelButton.setToolTipText("Discard changes.");

	if ( this.allowResize ) {
		south.add(this.insertButton);
		south.add(this.addButton);
		south.add(this.removeButton);
	}
	south.add(this.okButton);
	south.add(this.cancelButton);

	getContentPane().add("South", south);

	pack();
	// Set the window size.  Otherwise large numbers of items in the dictionary will cause the scrolled panel to
	// be bigger than the screen at startup in some cases.
	setSize(650,400);
	setResizable ( true );
	JGUIUtil.center(this);
	setVisible(true);
	JGUIUtil.center(this);
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