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
 * Panel to provide editing capabilities to construct a format specifier string, which includes
 * one or more of the %L type of specifiers.  The control consists of an editable text field,
 * an Insert button, and a JChoice with a list of available specifiers.  Use getText() to get
 * the contents of the text field.
 * @author sam
 * @see TSUtil.getTSFormatSpecifiers
 */
@SuppressWarnings("serial")
public class TSFormatSpecifiersJPanel extends JPanel implements ItemListener
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
     */
    public TSFormatSpecifiersJPanel ( int width )
    {
        setLayout ( new GridBagLayout() );
        Insets insetsTLBR = new Insets(0,0,0,0);

        int y = 0;
        int x = 0;
        __formatJComboBox = new SimpleJComboBox ( false );
        __formatJComboBox.setToolTipText(
            "Selecting a specifier will insert at the cursor position for the alias." );
        List<String> choicesList = StringUtil.toList(TSUtil.getTSFormatSpecifiers(true));
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
        // Make sure caret stays visible even when not in focus
        //__inputJTextField.setCaretColor( Color.lightGray ); // Too hard to see
        __inputJTextField.setCaretColor( Color.blue ); // Better but changes to gray after initial display?
        __inputJTextField.getCaret().setVisible ( true );
        __inputJTextField.getCaret().setSelectionVisible ( true );
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
     * Return the text field, for example to allow setting more specific tool tip text.
     */
    public JTextField getTextField()
    {
        return __inputJTextField;
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
                // Make sure caret stays visible even when not in focus
                __inputJTextField.getCaret().setVisible ( true );
                __inputJTextField.getCaret().setSelectionVisible ( true );
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