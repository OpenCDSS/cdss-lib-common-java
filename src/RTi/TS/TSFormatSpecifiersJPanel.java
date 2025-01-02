// TSFormatSpecifiersJPanel - panel to provide editing capabilities to construct a format specifier string

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

package RTi.TS;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.String.StringUtil;

/**
 * Panel for editor time series format specifier string,
 * which includes one or more of the %L type of specifiers,
 * an optionally ${ts:property} strings for built-in properties.
 * The control consists of an editable text field, an Insert button,
 * and a JChoice with a list of available specifiers.
 * Use getText() to get the contents of the text field.
 * @see TSUtil.getTSFormatSpecifiers
 */
@SuppressWarnings("serial")
public class TSFormatSpecifiersJPanel extends JPanel implements ItemListener {
    /**
     * Hint to aid user.
     */
    private String hint = "-- Select Specifier --";

    /**
     * Text field containing the edited format specifier.
     */
    private JTextField inputJTextField = null;

    /**
     * Choices for the list of format specifiers.
     */
    private SimpleJComboBox formatJComboBox = null;

    /**
     * Control constructor.
     * @param width width of the JTextField to be included in the control (or -1) to not specify.
     */
    public TSFormatSpecifiersJPanel ( int width ) {
        setLayout ( new GridBagLayout() );
        Insets insetsTLBR = new Insets(0,0,0,0);

        int y = 0;
        int x = 0;
        this.formatJComboBox = new SimpleJComboBox ( false );
        this.formatJComboBox.setToolTipText( "Selecting a specifier will insert itext at the cursor position for the alias." );
        boolean includeDescription = true;
        List<String> choicesList = StringUtil.toList(TSUtil.getTSFormatSpecifiers(includeDescription));
        choicesList.add(0,this.hint);
        this.formatJComboBox.setData(choicesList);
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
        this.inputJTextField.setToolTipText(
            "Enter a combination of literal strings, ${ts:property}, and/or format specifiers from the list on the left.");
        // Make sure caret stays visible even when not in focus.
        //__inputJTextField.setCaretColor( Color.lightGray ); // Too hard to see.
        this.inputJTextField.setCaretColor( Color.blue ); // Better but changes to gray after initial display?
        this.inputJTextField.getCaret().setVisible ( true );
        this.inputJTextField.getCaret().setSelectionVisible ( true );
        JGUIUtil.addComponent(this, this.inputJTextField,
            x++, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    }

    /**
     * Add a KeyListener for the text field.
     * @param listener KeyListener for event handling
     */
    public void addKeyListener ( KeyListener listener ) {
        this.inputJTextField.addKeyListener ( listener );
    }

    /**
     * Add a DocumentListener for the text field.
     * @param listener DocumentListener for event handling
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
     * Return the text in the text field.
     * @return the text in the text field
     */
    public String getText() {
        return this.inputJTextField.getText();
    }

    /**
     * Return the text field, for example to allow setting more specific tool tip text.
     * @return the text field
     */
    public JTextField getTextField() {
        return this.inputJTextField;
    }

    /**
    Respond to ItemEvents - user has selected from the list so insert into the cursor position in the text field.
    @param event Item event due to list change, etc.
    */
    public void itemStateChanged ( ItemEvent event ) {
        // Only insert on select.
        if ( event.getStateChange() == ItemEvent.SELECTED ) {
            String selection = StringUtil.getToken ( this.formatJComboBox.getSelected(), "-", 0, 0 ).trim();
            if ( !selection.equals(this.hint)) {
                int pos = this.inputJTextField.getCaretPosition();
                String text = this.inputJTextField.getText();
                String newText = text.substring(0,pos) + selection + text.substring(pos);
                this.inputJTextField.setText ( newText );
                // Make sure caret stays visible even when not in focus.
                this.inputJTextField.getCaret().setVisible ( true );
                this.inputJTextField.getCaret().setSelectionVisible ( true );
            }
        }
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