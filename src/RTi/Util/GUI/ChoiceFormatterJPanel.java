// ChoiceFormatterJPanel - panel to provide simple editing capabilities to construct a string given a list of choices

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2026 Colorado Department of Natural Resources

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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import RTi.Util.String.StringUtil;

/**
Panel to provide simple editing capabilities to construct a string given a list of choices from which one or more selections can be made.
The panel exposes the getText() method from the JTextField so that the edited contents can be retrieved.
*/
@SuppressWarnings("serial")
public class ChoiceFormatterJPanel extends JPanel implements ItemListener
{

/**
Hint to aid user.
*/
private String hint = "-- Select Specifier --";

/**
Delimiter that separates choice from description (e.g., "Choice - Description...").
*/
private String choiceDelim = null;

/**
Delimiter that separates selected choices (e.g., comma in "choice1,choice2").
*/
private String insertDelim = null;

/**
Whether append is allowed.  If true, then append choices using the delimiter.
If false, overwrite the text field contents with choice.
*/
private boolean append = true;

/**
Text field containing the edited format specifier.
*/
private JTextField inputJTextField = null;

/**
Choices for the list of format specifiers.
*/
private SimpleJComboBox formatJComboBox = null;

/**
Control constructor.
@param choices string choices that will be displayed to the user;
choices can be simple strings or compound strings consisting of values and description
(the "delim" parameter indices if a delimiter is used, for example as VALUE - DESCRIPTION)
@param choiceDelim delimiter that is used to separate data choices and description in the choices string;
specify as null or blank if not used
@param tooltip tooltip for the choice component
@param hint string to display by default to guide the user like "Please select...", and which will be ignored as a valid choice
@param insertDelim delimiter to be inserted when transferring choices
(e.g, if a comma is specified then a comma is automatically inserted when choices are selected;
specify as null or blank if not used @param width width of the JTextField to be included in the control (or -1) to not specify.
@param append if true, then choices are appended to the text field, separated by the delimited; if false,
choices replace the contents of the text field
*/
public ChoiceFormatterJPanel ( List<String> choices, String choiceDelim, String tooltip, String hint,
    String insertDelim, int width, boolean append ) {
    this.hint = hint;
    this.choiceDelim = choiceDelim;
    this.insertDelim = insertDelim;
    this.append = append;
    setLayout ( new GridBagLayout() );
    Insets insetsTLBR = new Insets(0,0,0,0);

    int y = 0;
    int x = 0;
    this.formatJComboBox = new SimpleJComboBox ( false );
    // Copy the choices so that the incoming data won't be modified.
    List<String> choices2 = new ArrayList<>();
    for ( String choice : choices ) {
    	choices2.add ( choice );
    }
    if ( (hint != null) && (hint.length() > 0) ) {
        choices2.add(0,this.hint);
    }
    this.formatJComboBox.setData(choices2);
    this.formatJComboBox.addItemListener ( this );
    JGUIUtil.addComponent(this, this.formatJComboBox,
        x++, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(this, new JLabel(" => "),
        x++, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    if ( width > 0 ) {
        this.inputJTextField = new JTextField ( width );
    }
    else {
        this.inputJTextField = new JTextField ();
    }
    if ( (tooltip != null) && (tooltip.length() > 0) ) {
        this.formatJComboBox.setToolTipText(tooltip);
    }
    // Make sure caret stays visible even when not in focus,
    // but use light gray so as to not confuse with the component that is in focus
    this.inputJTextField.setCaretColor( Color.lightGray );
    this.inputJTextField.getCaret().setVisible ( true );
    this.inputJTextField.getCaret().setSelectionVisible ( true );
    JGUIUtil.addComponent(this, this.inputJTextField,
        x++, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
}

/**
 * Add a KeyListener for the text field.
 */
public void addKeyListener ( KeyListener listener ) {
    this.inputJTextField.addKeyListener ( listener );
}

/**
 * Add a DocumentListener for the text field.
 */
public void addDocumentListener ( DocumentListener listener ) {
    this.inputJTextField.getDocument().addDocumentListener ( listener );
}

/**
Return the Document associated with the text field.
*/
public Document getDocument() {
    return this.inputJTextField.getDocument();
}

/**
Return the SimpleJComboBox used in the panel, useful for setting properties in calling code.
*/
public SimpleJComboBox getSimpleJComboBox() {
    return this.formatJComboBox;
}

/**
Return the text in the text field.
*/
public String getText() {
    return this.inputJTextField.getText();
}

/**
Respond to ItemEvents - user has selected from the list so insert into the cursor position in the text field.
@param evt Item event due to list change, etc.
*/
public void itemStateChanged ( ItemEvent evt ) {
    // Only insert on select.
    if ( evt.getStateChange() == ItemEvent.SELECTED ) {
        String selection = this.formatJComboBox.getSelected();
        if ( (this.hint == null) || (this.hint.length() == 0) || !selection.equals(this.hint)) {
            // Selection is not the hint so process the selection.
            if ( (this.choiceDelim != null) && (this.choiceDelim.length() != 0) ) {
                // Further split out the selection.
                selection = StringUtil.getToken ( this.formatJComboBox.getSelected(), this.choiceDelim, 0, 0 ).trim();
            }
            if ( this.append ) {
                int pos = this.inputJTextField.getCaretPosition();
                String text = this.inputJTextField.getText();
                String delim1 = "", delim2 = "";
                if ( (this.insertDelim != null) && !this.insertDelim.equals("") ) {
                    if ( pos == 0 ) {
                        // Inserting at the start.
                        if ( (text.length() > 0) && (text.charAt(0) != ',') ) {
                            delim2 = ",";
                        }
                    }
                    else if ( pos == text.length() ) {
                        // Inserting at the end.
                        if ( (text.charAt(pos - 1) != ',') ) {
                            delim1 = ",";
                        }
                    }
                    else {
                        // Inserting in the middle.
                        if ( (text.charAt(pos - 1) != ',') ) {
                            delim1 = ",";
                        }
                        if ( (text.charAt(pos) != ',') ) {
                            delim2 = ",";
                        }
                    }
                }
                String newText = text.substring(0,pos) + delim1 + selection + delim2 + text.substring(pos);
                this.inputJTextField.setText ( newText );
                // Make sure caret stays visible even when not in focus.
                this.inputJTextField.getCaret().setVisible ( true );
                this.inputJTextField.getCaret().setSelectionVisible ( true );
            }
            else {
                // Just transfer the value to the text field..
                this.inputJTextField.setText ( selection );
            }
        }
    }
}

/**
 * Set the choice strings.
 * This can be called after the initialization,
 * for example to set the choices in response to a UI selection.
 * The hint, if previously specified, will be retained.
 */
public void setChoices ( List<String> choices ) {
	// Remove all the choices first.
	this.formatJComboBox.removeAll();

    // Copy the choices so that the incoming data won't be modified.
    List<String> choices2 = new ArrayList<>();
    for ( String choice : choices ) {
    	choices2.add ( choice );
    }
    if ( (hint != null) && (hint.length() > 0) ) {
        choices2.add(0,this.hint);
    }
    this.formatJComboBox.setData(choices2);
}

/**
Set the components enabled state.
@param enabled whether or not the components should be enabled
*/
public void setEnabled ( boolean enabled ) {
    this.inputJTextField.setEnabled ( enabled );
    this.formatJComboBox.setEnabled ( enabled );
}

/**
 * Set the text in the text field.
 * @param text text to set in the textfield
 */
public void setText( String text ) {
    this.inputJTextField.setText ( text );
}

}