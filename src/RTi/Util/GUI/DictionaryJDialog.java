package RTi.Util.GUI;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
<p>
This class is a dialog for editing a dictionary string ("property:value,property:value,...").  The updated dictionary string is
returned via a response() method call as per the following sample code:
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
public class DictionaryJDialog extends JDialog implements ActionListener, KeyListener, WindowListener
{

/**
Button labels.
*/
private final String
	BUTTON_CANCEL = "Cancel",
	BUTTON_OK = "OK";

/**
Components to hold values from the TSIdent.
*/
private JTextField [] keyTextField = null;
private JTextField [] valueTextField = null;

/**
Dialog buttons.
*/
private SimpleJButton	
	cancelButton = null,
	okButton = null;

private String 
	response = null, // Dictionary string that is returned via response().
	dictString = null, // Dictionary string that is passed in.
	title = null,
	keyLabel = null,
	valueLabel = null;

int dictSize = 10;

private boolean error_wait = false; // Indicates if there is an error in input (true) from checkInput().

/**
Constructor.
@param parent the parent JFrame on which the dialog will appear.  This cannot
be null.  If necessary, pass in a new JFrame.
@param modal whether the dialog is modal.
@param dictString the dictionary string to edit.  Can be null, in which case <code>response()
</code> will return a new dictionary string filled with the values entered on the form.
@param title dialog title
@param keyLabel label above keys
@param valueLabel label above values
@param dictSize number of key/value pairs to show
*/
public DictionaryJDialog(JFrame parent, boolean modal, String dictString, String title, String keyLabel, String valueLabel,
    int dictSize )
{	super(parent, modal);

	this.dictString = dictString;
	this.title = title;
	this.keyLabel = keyLabel;
	this.valueLabel = valueLabel;
	this.dictSize = dictSize;
 	setupUI();
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	String s = event.getActionCommand();

	if (s.equals(this.BUTTON_CANCEL)) {
		response ( false );
	}
	else if (s.equals(this.BUTTON_OK)) {
		checkInputAndCommit ();
		if ( !this.error_wait ) {
			response( true );
		}
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInputAndCommit ()
{	
    // Previously show all input to user, even if in error, but check before saving
	this.error_wait = false;
	
	StringBuffer b = new StringBuffer();
	String chars = ":,\"";
	String message = "";
    // Get from the dialog...
	for ( int i = 0; i < this.keyTextField.length; i++ ) {
	    String key = this.keyTextField[i].getText().trim();
	    String value = this.valueTextField[i].getText().trim();
	    // Make sure that the key and value do not contain special characters :,"
	    if ( StringUtil.containsAny(key, chars, false) ) {
	        message += "\n" + this.keyLabel + " contains special character(s) \"" + chars + "\"";
	    }
	    if ( StringUtil.containsAny(value, chars, false) ) {
            message = "\n" + this.valueLabel + " contains special character(s) \"" + chars + "\"";
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
public void keyPressed(KeyEvent e)
{
}

/**
Does nothing.
*/
public void keyReleased(KeyEvent e)
{
}

/**
Does nothing.
*/
public void keyTyped(KeyEvent e) {}

/**
Return the user response and dispose the dialog.
@return the dialog response.  If <code>null</code>, the user pressed Cancel.
*/
public void response ( boolean ok )
{
	setVisible(false);
	dispose();
	if ( !ok ) {
		this.response = null;
	}
}

/**
Return the user response and dispose the dialog.
@return the dialog response.  If <code>null</code>, the user pressed Cancel.
*/
public String response ()
{
	return this.response;
}

/**
Sets up the GUI.
*/
private void setupUI()
{
    if ( this.title != null ) {
        setTitle(this.title );
    }

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());
	getContentPane().add("Center", panel);
	
	Insets insetsTLBR = new Insets(2,2,2,2);
	
	// Parse out the existing dictionary.
	String [] keyList = new String[0];
	String [] valueList = new String[0];
	if ( (this.dictString != null) && (this.dictString.length() > 0) ) {
	    // Have an existing dictionary string so parse and use to populate the dialog
	    String [] dictParts = this.dictString.split(",");
	    if ( dictParts != null ) {
    	    keyList = new String[dictParts.length];
    	    valueList = new String[dictParts.length];
    	    for ( int i = 0; i < dictParts.length; i++ ) {
    	        // Now split the part by :
    	        if ( dictParts[i].indexOf(":") > 0 ) {
    	            String [] parts2 = dictParts[i].split(":");
    	            if ( parts2.length == 2 ) {
    	                keyList[i] = parts2[0].trim();
    	                valueList[i] = parts2[1].trim();
    	            }
    	            else {
    	                keyList[i] = dictParts[i];
    	                valueList[i] = "";
    	            }
    	        }
    	        else {
    	            keyList[i] = dictParts[i].trim();
    	            valueList[i] = "";
    	        }
    	    }
	    }
	}
	if ( keyList.length > this.dictSize ) {
	    // Increase the size of the dictionary a bit over the initial size
	    dictSize = keyList.length + 5;
	}

	int y = -1;
	JGUIUtil.addComponent(panel, 
		new JLabel(this.keyLabel),
		0, ++y, 1, 1, 0, 0, insetsTLBR,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel, 
        new JLabel(this.valueLabel),
        1, y, 1, 1, 0, 0, insetsTLBR,
        GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    this.keyTextField = new JTextField[dictSize];
    this.valueTextField = new JTextField[dictSize];
    // Add key value pairs
    for ( int i = 0; i < this.dictSize; i++ ) {
        String key = "";
        String value = "";
        if ( i < keyList.length ) {
            key = keyList[i];
            value = valueList[i];
        }
        this.keyTextField[i] = new JTextField(key,10);
        JGUIUtil.addComponent(panel, this.keyTextField[i],
            0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        this.valueTextField[i] = new JTextField(value,40);
        JGUIUtil.addComponent(panel, this.valueTextField[i],
            1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    }
	
	JPanel south = new JPanel();
	south.setLayout(new FlowLayout(FlowLayout.RIGHT));

	this.okButton = new SimpleJButton(this.BUTTON_OK, this);
	this.okButton.setToolTipText("Press this button to accept any changes that have been made.");
	this.cancelButton = new SimpleJButton(this.BUTTON_CANCEL, this);
	this.cancelButton.setToolTipText("Press this button to discard changes.");

	south.add(this.okButton);
	south.add(this.cancelButton);

	getContentPane().add("South", south);

	pack();
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
public void windowActivated(WindowEvent evt) {}

/**
Does nothing.
*/
public void windowClosed(WindowEvent evt) {}

/**
Does nothing.
*/
public void windowDeactivated(WindowEvent evt) {}

/**
Does nothing.
*/
public void windowDeiconified(WindowEvent evt) {}

/**
Does nothing.
*/
public void windowIconified(WindowEvent evt) {}

/**
Does nothing.
*/
public void windowOpened(WindowEvent evt) {}

}