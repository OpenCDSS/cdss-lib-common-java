package RTi.TS;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
public class TSFormatSpecifiersJPanel extends JPanel implements ItemListener
{
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
        if ( width > 0 ) {
            __inputJTextField = new JTextField ( width );
        }
        else {
            __inputJTextField = new JTextField ();
        }
        int y = 0;
        int x = 0;
        JGUIUtil.addComponent(this, __inputJTextField,
            x, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        x += 2;
        JGUIUtil.addComponent(this, new JLabel("Insert:"),
            x++, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        __formatJComboBox = new SimpleJComboBox ( false );
        __formatJComboBox.setData(StringUtil.toList(TSUtil.getTSFormatSpecifiers(true)));
        __formatJComboBox.addItemListener ( this );
        JGUIUtil.addComponent(this, __formatJComboBox,
            x++, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    
    /**
     * Add a KeyListener for the text field.
     */
    public void addKeyListener ( KeyListener listener )
    {
        __inputJTextField.addKeyListener ( listener );
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
            int pos = __inputJTextField.getCaretPosition();
            String text = __inputJTextField.getText();
            String newText = text.substring(0,pos) + selection + text.substring(pos);
            __inputJTextField.setText ( newText );
        }
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