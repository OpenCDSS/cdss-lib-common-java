package RTi.Util.Time;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

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
 * one or more of the individual specifiers and literals.  The control consists of an editable text field,
 * an Insert button, and a JChoice with a list of available specifiers.  Use getText() to get
 * the contents of the text field.
 */
public class DateTimeFormatterSpecifiersJPanel extends JPanel implements ItemListener
{
/**
Hint to aid user.
*/
private String __hint = "----- Select Specifier -----";

/**
Text field containing the edited format specifier.
*/
private JTextField __inputJTextField = null;

/**
Choices for the list of formatter types.
*/
private SimpleJComboBox __formatterTypeJComboBox = null;

/**
Choices for the list of format specifiers.
*/
private SimpleJComboBox __specifierJComboBox = null;

/**
Default formatter type for use with blank formatter type choice.
*/
private DateTimeFormatterType __defaultFormatter = DateTimeFormatterType.C;

/**
Indicate whether format specifier choices should be shown for output (true) or parsing (false).
*/
private boolean __forOutput = false;

/**
Indicate whether format specifier choices should include properties like ${YearTypeYear}, used only for output.
*/
private boolean __includeProps = false;

/**
Control constructor.
@param width width of the JTextField to be included in the control (or -1) to not specify.
@param includeFormatterType if true, include a choice of the supported formatter types with "C", "ISO", etc.; if
false the default is C
@param includeBlankFormatterType if true, include a blank choice in the formatter type; this is useful when
the panel is being used for a command parameter and the formatter is optional
@param defaultFormatter if specified, this is the default formatter that is used when the choice is blank (default is
DateTimeFormatterType.C)
@param forOutput if true, then include more specifiers used for formatting output; if false, include only choices that have
been enabled for parsing
@param includeProps if true, include properties like ${YearTypeYear}, which are a more verbose way of specifying properties
(only used for output)
*/
public DateTimeFormatterSpecifiersJPanel ( int width, boolean includeFormatterType, boolean includeBlankFormatterType,
    DateTimeFormatterType defaultFormatter, boolean forOutput, boolean includeProps )
{
    if ( defaultFormatter == null ) {
        defaultFormatter = DateTimeFormatterType.C;
    }
    __forOutput = forOutput;
    __includeProps = includeProps;
    setLayout ( new GridBagLayout() );
    Insets insetsTLBR = new Insets(0,0,0,0);

    int y = 0;
    int x = 0;
    
    if ( includeFormatterType ) {
        __formatterTypeJComboBox = new SimpleJComboBox ( false );
        __formatterTypeJComboBox.setToolTipText( "Select the formatter type to use." );
        __formatterTypeJComboBox.setPrototypeDisplayValue(""+DateTimeFormatterType.ISO);
        List<String> choicesList = new Vector<String>();
        if ( includeBlankFormatterType ) {
            choicesList.add("");
        }
        choicesList.add("" + DateTimeFormatterType.C);
        // TODO SAM 2012-04-10 Need to add other formatter types
        __formatterTypeJComboBox.setData(choicesList);
        // Don't select choice here.  Do it below so that event will trigger populating specifier choices
        __formatterTypeJComboBox.addItemListener ( this );
        JGUIUtil.addComponent(this, __formatterTypeJComboBox,
            x++, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
    
    __specifierJComboBox = new SimpleJComboBox ( false );
    __specifierJComboBox.setToolTipText(
        "Selecting a specifier will insert at the cursor position in the format string." );
    __specifierJComboBox.setPrototypeDisplayValue(__hint+"WWWWWWWW"); // Biggest formatter name
    __specifierJComboBox.addItemListener ( this );
    JGUIUtil.addComponent(this, __specifierJComboBox,
        x++, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( includeFormatterType ) {
        __formatterTypeJComboBox.select(null);
        __formatterTypeJComboBox.select(0); // Do this here to trigger population of the format specifier choices
    }
    if ( __specifierJComboBox.getItemCount() > 0 ) {
        __specifierJComboBox.select(0); // Now select the specifier corresponding to the formatter
    }
    
    JGUIUtil.addComponent(this, new JLabel(" => "),
        x++, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    if ( width > 0 ) {
        __inputJTextField = new JTextField ( width );
    }
    else {
        __inputJTextField = new JTextField ();
    }
    __inputJTextField.setToolTipText(
        "Enter a combination of literal strings and/or format specifiers from the list on the left.");
    // Make sure caret stays visible even when not in focus
    __inputJTextField.setCaretColor( Color.lightGray );
    __inputJTextField.getCaret().setVisible ( true );
    __inputJTextField.getCaret().setSelectionVisible ( true );
    JGUIUtil.addComponent(this, __inputJTextField,
        x, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
}

/**
Add a DocumentListener for the text field.
*/
public void addDocumentListener ( DocumentListener listener )
{
    __inputJTextField.getDocument().addDocumentListener ( listener );
}

/**
Add an ItemListener for the formatter type combo box.  A listener for the format specifier combo box is not added
because its selections result in document events, which can be listed to by calling addDocumentListener().
*/
public void addFormatterTypeItemListener ( ItemListener listener )
{
    __formatterTypeJComboBox.addItemListener ( listener );
}

/**
Add a KeyListener for the text field.
*/
public void addKeyListener ( KeyListener listener )
{
    __inputJTextField.addKeyListener ( listener );
}

/**
Return the DateTimeFormatterType that is in effect for the format string.  This can be used, for example, to
prefix a format string to indicate the formatter type.  The value returned should not be null.
@return the formatter type that is visible or in effect, depending on "visible" parameter
@param onlyIfVisible if false and no formatter is shown in the choice, return null; if true and no formatter is shown,
return the default
*/
public DateTimeFormatterType getDateTimeFormatterType ( boolean onlyIfVisible )
{
    if ( onlyIfVisible ) {
        if ( (__formatterTypeJComboBox == null) || __formatterTypeJComboBox.getSelected().equals("") ) {
            return null; // No formatter visible in interface
        }
        else {
            // Get formatter that corresponds to what is shown
            return DateTimeFormatterType.valueOfIgnoreCase(__formatterTypeJComboBox.getSelected());
        }
    }
    else {
        // Return the formatter that is in effect, whether visibly shown or not
        if ( (__formatterTypeJComboBox == null) || __formatterTypeJComboBox.getSelected().equals("") ) {
            return DateTimeFormatterType.C; // Default when not visible
        }
        else {
            // Get formatter that corresponds to what is shown
            return DateTimeFormatterType.valueOfIgnoreCase(__formatterTypeJComboBox.getSelected());
        }
    }
}

/**
Return the Document associated with the text field.
*/
public Document getDocument()
{
    return __inputJTextField.getDocument();
}

/**
Return the selected formatter type (e.g., "C").
*/
public String getSelectedFormatterType()
{
    if ( __formatterTypeJComboBox == null ) {
        return "";
    }
    else {
        return __formatterTypeJComboBox.getSelected();
    }
}

/**
Return the text in the text field and do not prepend the formatter type.
*/
public String getText()
{
    return getText(false,false);
}

/**
Return the text in the text field.
@param includeFormatterType if false, return the text field contents; if true and a formatter is known,
prepend the formatter display name (e.g., "C:xxxx").
@param onlyIfVisible if true, only include the formatter type prefix if the formatter is visible; if false,
always include the formatter type
*/
public String getText( boolean includeFormatterType, boolean onlyIfVisible )
{
    if ( includeFormatterType ) {
        // Get the formatter for what is visible
        DateTimeFormatterType t = getDateTimeFormatterType(onlyIfVisible);
        if ( t == null ) {
            return __inputJTextField.getText();
        }
        else {
            return "" + t + ":" + __inputJTextField.getText();
        }
    }
    else {
        return __inputJTextField.getText();
    }
}

/**
Return the text field component, for example to allow tool tips to be set.
@return the text field component.
*/
public JTextField getTextField ()
{
	return __inputJTextField;
}

/**
Respond to ItemEvents - user has selected from the list so insert into the cursor position in the
text field.
@param evt Item event due to list change, etc.
*/
public void itemStateChanged ( ItemEvent evt )
{   Object source = evt.getSource();
    // Only insert on select..
    if ( evt.getStateChange() == ItemEvent.SELECTED ) {
        if ( source == __specifierJComboBox ) {
            String selection = StringUtil.getToken ( __specifierJComboBox.getSelected(), "-", 0, 0 ).trim();
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
        else if ( (__formatterTypeJComboBox != null) && (source == __formatterTypeJComboBox) ) {
            populateFormatSpecifiers();
        }
    }
}

/**
Populate the format specifiers based on the formatter type.
This does not select any of the items (should do that immediately after calling this method).
*/
private void populateFormatSpecifiers()
{   String selectedFormatterType = __formatterTypeJComboBox.getSelected();
    DateTimeFormatterType formatterType = null;
    try {
        formatterType = DateTimeFormatterType.valueOfIgnoreCase(selectedFormatterType);
    }
    catch ( IllegalArgumentException e ) {
        formatterType = null;
    }
    List<String> choicesList = null;
    // Because the choices get reset there is a chance that this will cause layout problems.  Consequently, it is
    // best to make sure that the hint takes up enough space that the choice width does not change when repopulated
    choicesList = new ArrayList<String>();
    if ( (formatterType == null) && (__defaultFormatter != null) ) {
        formatterType = __defaultFormatter;
    }
    if ( formatterType == null ) {
        choicesList.add(__hint);
    }
    else if ( formatterType == DateTimeFormatterType.C ) {
        choicesList.add(__hint);
        choicesList.addAll(Arrays.asList(TimeUtil.getDateTimeFormatSpecifiers(true,__forOutput,__includeProps)));
    }
    __specifierJComboBox.setData(choicesList);
    int max = 20;
    if ( choicesList.size() < 20 ) {
        max = choicesList.size();
    }
    __specifierJComboBox.setMaximumRowCount(max);
}

/**
Select the formatter type.  Select the empty string if formatterType=null.
*/
public void selectFormatterType ( DateTimeFormatterType formatterType )
{
    if ( formatterType == null ) {
        __formatterTypeJComboBox.selectIgnoreCase("");
    }
    __formatterTypeJComboBox.selectIgnoreCase("" + formatterType);
}

/**
Set the text in the text field.
@param text text to set in the textfield
*/
public void setText( String text )
{
    __inputJTextField.setText ( text );
}

}