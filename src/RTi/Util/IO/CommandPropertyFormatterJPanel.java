// CommandPropertyFormatterJPanel - panel to provide editing capabilities to construct a format specifier string

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

package RTi.Util.IO;

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
 * Panel to provide editing capabilities to construct a format specifier string, which includes
 * specifiers like ${c:PropName} where the "c" indicates command scope and PropName is a parameter name.
 * The control consists of an editable text field, an Insert button, and a JChoice with a list of
 * available specifiers.  Use getText() to get the contents of the text field.
 * @author sam
 */
@SuppressWarnings("serial")
public class CommandPropertyFormatterJPanel extends JPanel implements ItemListener
{
    /**
     * Hint to aid user.
     */
    String __hint = "-- Select Specifier --";
    
    /**
     * Text field containing the edited format specifier.
     */
    JTextField __inputJTextField = null;
    
    /**
     * Choices for the list of format specifiers.
     */
    SimpleJComboBox __formatJComboBox = null;
    
    /**
     * Control constructor.
     * @param width width of the JTextField to be included in the control (or -1) to not specify.
     * @param choices specifier choices, used because there is not yet a standard bullet-proof way
     * to generically request the choices, for example from a Command instance
     */
    public CommandPropertyFormatterJPanel ( int width, List<String> choices )
    {
        setLayout ( new GridBagLayout() );
        Insets insetsTLBR = new Insets(0,0,0,0);

        int y = 0;
        int x = 0;
        __formatJComboBox = new SimpleJComboBox ( false );
        __formatJComboBox.setToolTipText(
            "Selecting a specifier will insert at the cursor position in the text field." );
        List<String> choicesList = choices;
        choicesList.add(0,__hint);
        __formatJComboBox.setData(choicesList);
        __formatJComboBox.addItemListener ( this );
        JGUIUtil.addComponent(this, __formatJComboBox,
            x++, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(this, new JLabel(" => "),
            x++, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        
        if ( width > 0 ) {
            __inputJTextField = new JTextField ( width );
        }
        else {
            __inputJTextField = new JTextField ();
        }
        __inputJTextField.setToolTipText(
            "Enter a combination of literal strings and/or format specifiers from the list on the left.");
        JGUIUtil.addComponent(this, __inputJTextField,
            x++, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    }
    
    /**
     * Add a KeyListener for the text field.
     */
    public void addKeyListener ( KeyListener listener )
    {
        __inputJTextField.addKeyListener ( listener );
    }
    
    /**
     * Add a DocumentListener for the text field.
     */
    public void addDocumentListener ( DocumentListener listener )
    {
        __inputJTextField.getDocument().addDocumentListener ( listener );
    }
    
    /**
    Return the Document associated with the text field.
    */
    public Document getDocument()
    {
        return __inputJTextField.getDocument();
    }
    
    /**
     * Return the text in the text field.
     */
    public String getText()
    {
        return __inputJTextField.getText();
    }
    
    /**
    Respond to ItemEvents - user has selected from the list so insert into the cursor position in the
    text field.
    @param evt Item event due to list change, etc.
    */
    public void itemStateChanged ( ItemEvent evt )
    {
        // Only insert on select..
        if ( evt.getStateChange() == ItemEvent.SELECTED ) {
            String selection = StringUtil.getToken ( __formatJComboBox.getSelected(), "-", 0, 0 ).trim();
            if ( !selection.equals(__hint)) {
                int pos = __inputJTextField.getCaretPosition();
                String text = __inputJTextField.getText();
                String newText = text.substring(0,pos) + selection + text.substring(pos);
                __inputJTextField.setText ( newText );
            }
        }
    }
    
    /**
    Set the components enabled state.
    @param enabled whether or not the components should be enabled
    */
    public void setEnabled ( boolean enabled )
    {
        __inputJTextField.setEnabled ( enabled );
        __formatJComboBox.setEnabled ( enabled );
    }
    
    /**
     * Set the text in the text field.
     * @param text text to set in the textfield
     */
    public void setText( String text )
    {
        __inputJTextField.setText ( text );
    }
}
