//-----------------------------------------------------------------------------
// JGUIUtil - Swing GUI utility methods class, containing static methods
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2002-09-16	J. Thomas Sapienza, RTi	Initial Version
// 2002-11-05	Steven A. Malers, RTi	Add selectAll() for JList (deselectAll
//					is not needed because
//					JList.clearSelection() can be used.
//					Extend this class from GUIUtil so that
//					all features are available in one class.
//					Remove createPanel() since it conflicts
//					with the GUIUtil base class and needs to
//					be phased out anyway (it is used only in
//					very limited cases and does not deserve
//					to take up space here).
// 2002-11-06	SAM, RTi		Add computeOptimalPosition().
//					Add newFontNameJChoice() and
//					newFontStyleJChoice().
//					Add selectIgnoreCase().
//					Add selectedSize().
// 2003-03-24	JTS, RTi		Added setWaitCursor().
// 2003-04-10	SAM, RTi		Add selectTokenMatches().
// 2003-05-08	JTS, RTi		* Added addToJComboBox().
//					* Added setSystemLookAndFeel().
// 2003-05-12	SAM, RTi		Add setEnabled().
// 2003-06-16	SAM, RTi		Add isChoiceItem() for SimpleJChoice.
// 2003-06-18	SAM, RTi		Add writeFile() to write a JTextArea to
//					a file.  This is more generic and simple
//					than the RTi.Util.IO.ExportJGUI way of
//					doing it.
// 2003-07-24	JTS, RTi		Added forceRepaint()
// 2003-09-18	JTS, RTI		* Added setIconImage() and 
//					  getIconImage().
//					* Added setAppNameForWindows() and
//					  getAppNameForWindows().
//					* Added setIcon().
//					* Added loadImageIcon.
// 2003-09-30	SAM, RTI		* Added setIcon(JDialog...).
// 2003-10-06	JTS, RTi		* loadIconImage() now throws an 
//					  exception if no icon could be found 
//					  at the specified location. 
//					* setIconImage(String) now throws an
//					  exception for the same reason.
// 2003-10-06	SAM, RTi		* Add addStringToSelected(), similar to
//					  old GUIUtil, but use Swing components.
// 					* Add removeStringFromSelected(),
//					  similar to old GUIUtil, but use Swing
//					  components.
//					* Add select() similar to old GUIUtil,
//					  to allow choices to be selected when
//					  ignoring case.
//					* Add isSimpleJComboBoxItem() similar to
//					  GUIUtil isChoiceItem().
//					* Change jcheckboxToString() to
//					  simply toString().
//					* Add indexOf() for JList, similar to
//					  the GUIUtil version.
// 2003-12-10	SAM, RTi		* Add selectIgnoreCase(SimpleJComboBox).
// 					* Add newFontNameJComboBox().
// 					* Add newFontStyleJComboBox().
// 2004-05-10	JTS, RTi		* Add copyToClipboard().
//					* Add clearClipboard().
// 2004-07-21	SAM, RTi		In isSimpleJComboBoxItem(), return false
//					if the compare string is null.
// 2004-08-26	SAM, RTi		Overload selectTokenMatches() with the
//					trim_tokens parameter.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

package RTi.Util.GUI;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.net.URL;

import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.UIManager;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class provides useful static functions for handling SWING GUI (graphical 
user interface) components.  This class extends GUIUtil and so inherits the
ability to work with AWT components, as well.
*/
public abstract class JGUIUtil extends GUIUtil {
/**
The current status of the wait cursor, as set by the setWaitCursor method.
*/
private static boolean __waitCursor = false;

/**
The icon to use for an application.
*/
private static ImageIcon __applicationIcon = null;

/**
The 'pretty' version of the application name that can be displayed in window
titles, dialog boxes, and more.
*/
private static String __applicationName = "";

/**
Given a JList with selected items, add the specified string to the front of the
items if it is not already at the front of the items.  After the changes, the
originally selected items are still selected.  This is useful, for example,
when a popup menu toggles the contents of a list back and forth.
The list model must be the DefaultListModel or an extended class.
REVISIT JAVADOC: see removeStringFromSelected
@param list JList to modify.
@param prefix String to add.
*/
public static void addStringToSelected ( JList list, String prefix )
{	if ( (list == null) || (prefix == null) ) {
		return;
	}
	int selected_indices[] = list.getSelectedIndices();
	int selected_size = selectedSize ( list );
	int len = prefix.length();
	DefaultListModel model = (DefaultListModel)list.getModel();
	String item;
	for ( int i = 0; i < selected_size; i++ ) {
		item = (String)model.getElementAt(selected_indices[i]);
		if ( item.trim().regionMatches(true,0,prefix,0,len) ) {
			model.setElementAt ( prefix + item, selected_indices[i] );
		}
	}
	// Make sure the selected indices remain as before...
	list.setSelectedIndices ( selected_indices );
	selected_indices = null;
}

/**
Add an array of strings to a JList.  This is useful when a standard set of choices are available.
@param comboBox Choice to add items to.
@param items Items to add.
*/
public static void addToJComboBox ( JComboBox comboBox, String[] items )
{	if ( (comboBox == null) || (items == null) ) {
		return;
	}
	for ( int i = 0; i < items.length; i++ ) {
		comboBox.addItem ( items[i] );
	}
}

/**
Add a list of strings to a JList.  This is useful when a standard set of
choices are available.  The toString() method of each object in the Vector is
called, so even non-String items can be added.
@param comboBox Choice to add items to.
@param items Items to add.
*/
public static void addToJComboBox ( JComboBox comboBox, List items )
{	if ( (comboBox == null) || (items == null) ) {
		return;
	}
	int size = items.size();
	for ( int i = 0; i < size; i++ ) {
		comboBox.addItem ( items.get(i).toString() );
	}
}

/**
Clears the system clipboard of whatever data exists on it.  This should be 
called at System.exit() time by applications that use the clipboard, otherwise
any data put on the clipboard by the application will remain there and use system resources.
*/
public static void clearClipboard() {
	StringBuffer buffer = new StringBuffer("");
	StringSelection selection = new StringSelection(buffer.toString());	
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	clipboard.setContents(selection, selection);
}	

/**
Hides and dispose of a JFrame Object.
@param frame JFrame object to hide.
*/
public static void close (JFrame frame)
{	if ( frame != null ) {
		frame.setVisible ( false );
		frame.dispose();
	}
}

/**
Compute the optimal coordinates to display a JPopupMenu.
This is necessary because of a limitation in JPopupMenu where it does not
automatically adjust for cases where some of the menu would be displayed off
the screen.  See JavaSoft bug 4425878.
@param pt Candidate point (e.g., from MouseEvent.getPoint()).
@param c Component that menu is associated with (e.g., from MouseEvent.getComponent()).
@param menu JPopupMeni instance to check.
@return a Point containing the optimal coordinates.
*/
public static Point computeOptimalPosition ( Point pt, Component c, JPopupMenu menu ) 
{	// The code below is partially taken from the bug report.  However,
	// the fix there for computing coordinates was actually pretty
	// simplistic, so an improvement has been implemented here...
	Dimension menuSize = menu.getPreferredSize();
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	SwingUtilities.convertPointToScreen ( pt, c );
	Point optimal_pt = new Point ( pt );
	if ( (pt.x + menuSize.width) > screenSize.width ) {
		optimal_pt.x -= menuSize.width;
	}
	if ( (pt.y + menuSize.height) > screenSize.height ) {
		optimal_pt.y -= menuSize.height;
	}
	SwingUtilities.convertPointFromScreen ( optimal_pt, c );
	return optimal_pt;
}

/**
Copies a String to the system clipboard.  Once the string has been copied
to the clipboard, it can be pasted into other applications.
@param s the String to copy to the clipboard.
*/
public static void copyToClipboard(String s) {
	StringBuffer buffer = new StringBuffer(s);
	StringSelection selection = new StringSelection(buffer.toString());	
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	clipboard.setContents(selection, selection);
}

/**
Enable a list of components.  This method can be called, for example, 
when a data object is selected from a list of objects (e.g., in a JList or JWorksheet).
@param comp an array of all the JComponents on the form that can be
enabled when something is selected.
@param comp_never_enabled an array of the components in comp[] that should 
never be editable.  These components are disabled after ther others are
enabled.  Specify as -1 to ignore.
@param editable Indicates whether the form is editable or not.  If the form is
not editable, then some components may be disabled to prevent input.
*/
public static void enableComponents ( JComponent[] comp, int[] comp_never_enabled, boolean editable )
{	for (int i = 0; i < comp.length; i++) {
		if (comp[i] instanceof JTextComponent) {
			if (editable) {
				setEnabled ( comp[i], true );
				((JTextComponent)comp[i]).setEditable(true);
			}
			else {
			    ((JTextComponent)comp[i]).setEditable(false);
			}
		}
		else if (comp[i] instanceof JComboBox) {
			if (editable) {
				setEnabled ( comp[i], true );
				((JComboBox)comp[i]).setEditable(true);
			}
			else {
			    ((JComboBox)comp[i]).setEditable(false);
				setEnabled ( comp[i], false );
			}
		}
		else {
		    setEnabled ( comp[i], true );
		}
	}
	if ( comp_never_enabled != null ) {
		for (int i = 0; i < comp_never_enabled.length; i++) {
			if ( comp_never_enabled[i] >= 0 ) {
				if ( comp[comp_never_enabled[i]] instanceof JTextComponent ) {
					// Text is hard to read when disabled so just set not editable...
					((JTextComponent)comp[comp_never_enabled[i]]).setEditable(false);
				}
				else {
				    // All other components...
					setEnabled (comp[comp_never_enabled[i]], false );
				}
			}
		}
	}
}

/**
Disable a list of components.  For example, use when no data item is
selected.  See enableComponents(), which will also disable components globally
if the editable flag is set to false.
@param comp an array of all the JComponents on the form to be disabled.
@param cleartext If true, text components will be cleared when disabled.  If 
false, the text component text will not be changed.
*/
public static void disableComponents ( JComponent[] comp, boolean cleartext )
{	for (int i = 0; i < comp.length; i++) {
		if (comp[i] instanceof JTextComponent) {
			if ( cleartext ) {
				((JTextComponent)comp[i]).setText("");
			}
			((JTextComponent)comp[i]).setEditable(false);
		}
		else if (comp[i] instanceof JComboBox) {
			setEnabled ( comp[i], false );
			((JComboBox)comp[i]).setEditable(false);
		}
		else {
		    setEnabled ( comp[i], false );
		}
	}
}

/**
Forces the component to repaint immediately.  The JComponent is guaranteed 
to be repainted by the time this method returns.
@param component the JComponent to repaint.
*/
public static void forceRepaint(JComponent component) {
	Rectangle rect = component.getBounds();
	if (rect == null) {
		return;
	}
	rect.x = 0;
	rect.y = 0;
	component.paintImmediately(rect);
}

/**
Returns the 'pretty' version of the application name, which can be used
in windows, dialog boxes, and titles.
@return the nice version of the application name.  Will never return 
<tt>null</tt>.
*/
public static String getAppNameForWindows() {
	return __applicationName;
}

/**
Returns the image to use as the application icon.
@return the image to use as the application icon.
*/
public static ImageIcon getIconImage() {
	return __applicationIcon;
}

/**
Returns the current wait cursor state.
@return the current wait cursor state.
*/
public static boolean getWaitCursor() {
	return __waitCursor;
}

/**
Determine position of a string in a JList.
The JList must use a DefaultListModel or object derived from this class.
@param list JList to search.
@param item String item to search for.
@param selected_only Indicates if only selected items should be searched.
@param ignore_case Indicates whether to ignore case (true) or not (false).
@return The index of the first match, or -1 if no match.
*/
public static int indexOf (	JList list, String item, boolean selected_only, boolean ignore_case )
{	if ( (list == null) || (item == null) || (item.length() == 0) ) {
		return -1;
	}
	int size = 0;
	String list_item = null;
	DefaultListModel model = (DefaultListModel)list.getModel();
	if ( selected_only ) {
		size = selectedSize ( list );
		int [] selected_indices = list.getSelectedIndices();
		for ( int i = 0; i < size; i++ ) {
			list_item = (String)model.elementAt( selected_indices[i]);
			if ( ignore_case ) {
				if ( list_item.equalsIgnoreCase(item) ) {
					return i;
				} 
			}
			else if ( list_item.equals(item) ) {
				return i;
			}
		}
	}
	else {
	    size = model.size();
		for ( int i = 0; i < size; i++ ) {
			list_item = (String)model.elementAt(i);
			if ( ignore_case ) {
				if ( list_item.equalsIgnoreCase(item) ) {
					return i;
				} 
			}
			else if ( list_item.equals(item) ) {
				return i;
			}
		}
	}
	return -1;
}

/**
Determine if the specified compare String exists within a SimpleJComboBox - CASE SENSITIVE.
<ul>
<li>	Can compare the compare String against substrings for each item
	in the comboBox object if FLAG is set to CHECK_SUBSTRINGS.</li>
<li>	To not compare against substrings, set FLAG to NONE.</li>
</ul>
@param comboBox SimpleJComboBox object.
@param compare String to compare comboBox items against.  If null, false is returned.
@param FLAG compare criteria (i.e, CHECK_SUBSTRINGS, NONE).
@param delimiter String containing delimiter to parse for CHECK_SUBSTRINGS,
may be null if using FLAG == NONE.
@param index Index location where the compare String was located at index[0]
@return returns true if compare exist in the comboBox items list,
false otherwise.  This is filled in unless it is passed as null.
*/
public static boolean isSimpleJComboBoxItem ( SimpleJComboBox comboBox,
	String compare, int FLAG, String delimiter, int[] index )
{	int size; // number of items in the choices
    int curIndex; // current character position
    int length; // length of curItem
    String curItem; // current Choice item
    String curChar; // current character
 
	if ( compare == null ) {
		return false;
	}
    // Initialize variables
    compare = compare.trim();
    size = comboBox.getItemCount();

    for( int i=0; i<size; i++ ) {
        curItem = comboBox.getItem( i ).trim(); 
        String sub = curItem;

        // check substring where substrings are delineated by spaces
        if ( FLAG == CHECK_SUBSTRINGS ) {
            // Jump over all characters until the delimiter is reached.  Break the remaining
            // String into a SubString and compare to the compare String.
            length = sub.length();
            for ( curIndex = 0; curIndex < length; curIndex++ ) {
                curChar = String.valueOf(curItem.charAt( curIndex ) ).trim();
                if ( curChar.equals(delimiter) ) {
                    sub = sub.substring( curIndex+1).trim();
                }
            }
            // Compare the remaining String, sub, to the compare
			// String.  If a match occurs, return true and the index
			// in the list in which the match was found.
            if ( compare.equals( sub ) ) {
                index[0] = i;
                return true;
            }
        }
		else if ( FLAG == NONE ) {
			// Compare to the curItem String directly
            if ( curItem.equals(compare) ) {
        		if ( index != null ) {
                	index[0] = i;
        		}
                return true;
            }
        }
    }
    return false;
}

/**
Loads an image icon from a location and returns it.  
@param location the location at which the icon can be found, either a path
to an image on a drive or a path within a JAR file.  
@return the ImageIcon that was loaded, or null if there was a problem loading the ImageIcon.
@throws exception if no file could be found at the specified location.
*/
public static ImageIcon loadIconImage(String location) 
throws Exception {
	// first try loading the image as if it were specified in a JAR file.
	URL iconURL = ClassLoader.getSystemResource(location);
	if (iconURL == null) {
		// If that failed, try loading the image as if it were specified in a proper file name
		File f = new File(location);
		if (!f.exists()) {
			throw new Exception("No icon could be found at location '" + location + "'");
		}
		iconURL = f.toURL();
		if (iconURL == null) {
			throw new Exception("No icon could be found at location '" + location + "'");
		}
	}
	ImageIcon i = new ImageIcon(iconURL);

	return i;
}

/**
Return a new SimpleJComboBox that contains a list of standard fonts.
@return a new SimpleJComboBox that contains a list of standard fonts.
*/
public static SimpleJComboBox newFontNameJComboBox ()
{	SimpleJComboBox fonts = new SimpleJComboBox(false);
	// TODO - need to add more choices or make the lookup dynamic
	fonts.add ( "Arial" );
	fonts.add ( "Courier" );
	fonts.add ( "Helvetica" );
	return fonts;
}

/**
Return a new SimpleJComboBox that contains a list of standard font styles.
@return a new SimpleJComboBox that contains a list of standard font styles.
*/
public static SimpleJComboBox newFontStyleJComboBox ()
{	SimpleJComboBox styles = new SimpleJComboBox(false);
	styles.add( "Plain" );
	styles.add( "PlainItalic" );
	styles.add( "Bold" );
	styles.add( "BoldItalic" );
	return styles;
}

/**
Given a list with selected items, remove the specified string from the front of
the items if is at the front of the items.  After the changes, the
originally selected items are still selected.  This is useful, for example,
when a popup menu toggles the contents of a list back and forth.
The list model must be the DefaultListModel or an extended class.
REVISIT JAVADOC: see addStringToSelected
@param list JList to modify.
@param prefix String to add.
*/
public static void removeStringFromSelected ( JList list, String prefix )
{	if ( (list == null) || (prefix == null) ) {
		return;
	}
	int selected_indices[] = list.getSelectedIndices();
	int selected_size = selectedSize ( list );
	int len = prefix.length();
	DefaultListModel model = (DefaultListModel)list.getModel();
	String item;
	for ( int i = 0; i < selected_size; i++ ) {
		item = (String)model.getElementAt( selected_indices[i]);
		if ( item.trim().regionMatches(true,0,prefix,0,len) &&
			StringUtil.tokenCount( item," \t", StringUtil.DELIM_SKIP_BLANKS) > 1 ) {
			model.setElementAt (  item.substring(len).trim(), selected_indices[i] );
		}
	}
	// Make sure the selected indices remain as before...
	list.setSelectedIndices ( selected_indices );
	selected_indices = null;
}

/**
Select a single matching item in a JList.  Only the first match is selected.
The DefaultListModel or an extended class should be used for the list model.
@param list JList to select from.
@param item Item to select.
@param ignore_case Indicates whether case should be ignored when searching the list for a match.
*/
public static void select ( JList list, String item, boolean ignore_case )
{	if ( (list == null) || (item == null) ) {
		return;
	}
	DefaultListModel model = (DefaultListModel)list.getModel();
	int size = model.size();
	String list_item = null;
	for ( int i = 0; i < size; i++ ) {
		list_item = (String)model.getElementAt(i);
		if ( ignore_case ) {
			if ( list_item.equalsIgnoreCase(item) ) {
				list.setSelectedIndex(i);
				return;
			}
		}
		else if ( list_item.equals(item) ) {
			list.setSelectedIndex(i);
			return;
		}
	}
}

/**
Select all items in a JList.
@param list JList to select all items.
*/
public static void selectAll ( JList list )
{	if ( list == null ) {
		return;
	}
	// There is no "select" method so need to select all.  Rather than do
	// this item by item, send an array and do it all at once - this should
	// hopefully give the best performance...
	int [] selected = new int[list.getModel().getSize()];
	int size = selected.length;
	for ( int i = 0; i < size; i++ ) {
		selected[i] = i;
	}
	list.setSelectedIndices ( selected );
	selected = null;
}

/**
Return the index of the requested selected item.  For example, if there are 5
items selected out of 20 total, requesting index 0 will return index of the
first of the 5 selected items.  This is particularly useful when determining
the first or last selected item in a list.
@param list JList to check.
@param selected_index Position in the selected rows list.
@return the position in the original data for the requested selected index or
-1 if unable to determine.
*/
public static int selectedIndex ( JList list, int selected_index )
{	if ( list == null ) {
		return -1;
	}
	int selected[] = list.getSelectedIndices();
	if ( selected != null ) {
		int length = selected.length;
		if ( selected_index > (length - 1) ) {
			selected = null;
			return -1;
		}
		int pos = selected[selected_index];
		selected = null;
		return pos;
	}
	return -1;
}

/**
Return the number of items selected in a JList.
@param list JList to check.
@return the number items selected in the JList, or 0 if a null List.
*/
public static int selectedSize ( JList list )
{	if ( list == null ) {
		return 0;
	}
	int selected[] = list.getSelectedIndices();
	if ( selected != null ) {
		int length = selected.length;
		selected = null;
		return length;
	}
	return 0;
}

/**
Select an item in a SimpleJComboBox, ignoring the case.  This is useful when the
SimpleJComboBox shows a valid property but the property may not always exactly
match in case when read from a file or hand-edited.
@param c SimpleJComboBox to select from.
@param item SimpleJComboBox item to select as a string.
@exception Exception if the string is not found in the SimpleJComboBox.
*/
public static void selectIgnoreCase ( SimpleJComboBox c, String item )
throws Exception
{	// Does not look like SimpleJComboBox.select(String) throws an exception
	// if the item is not found (especially if the SimpleJComboBox is
	// editable) so go through the list every time...
	// Get the list size...
	int size = c.getItemCount();
	for ( int i = 0; i < size; i++ ) {
		if ( c.getItem(i).equalsIgnoreCase(item) ) {
			c.select ( i );
			return;
		}
	}
	throw new Exception ( "String \"" + item + "\" not found in SimpleJComboBox" );
}

/**
Select an item in a JComboBox, comparing a specific token in the choices.  This
is useful when the combo box shows an extended value (e.g., "Value - Description").
@param c JComboBox to select from.
@param ignore_case Indicates if case should be ignored when comparing strings.
@param delimiter String delimiter used by StringUtil.breakStringList().
@param flags Flags used by StringUtil.breakStringList().
@param token Token position in the JComboBox item, to be compared.
@param item String item to compare to JComboBox item tokens.
@param default_item String If null, only "item" is evaluated.  If not null and
"item" is not found, then an attempt to match "default" is made, using the same
tokenizing parameters.  If a match is found, it is selected.  If a match is not
found, an Exception is thrown.  This parameter is useful when defaulting a
combo box to a value for a new instance of an object.
@exception Exception if the string is not found in the JComboBox.
*/
public static void selectTokenMatches (	JComboBox c, boolean ignore_case, String delimiter, int flags,
	int token, String item, String default_item )
throws Exception
{	selectTokenMatches ( c, ignore_case, delimiter, flags, token, item,	default_item, false );
}

/**
Select an item in a JComboBox, comparing a specific token in the choices.  This
is useful when the combo box shows an extended value (e.g., Value - Description").
@param c JComboBox to select from.
@param ignore_case Indicates if case should be ignored when comparing strings.
@param delimiter String delimiter used by StringUtil.breakStringList().  If null, compare the whole string.
@param flags Flags used by StringUtil.breakStringList().
@param token Token position in the JComboBox item, to be compared.
@param item String item to compare to JComboBox item tokens.
@param default_item If null, only "item" is evaluated.  If not null and
"item" is not found, then an attempt to match "default" is made, using the same
tokenizing parameters.  If a match is found, it is selected.  If a match is not
found, an Exception is thrown.  This parameter is useful when defaulting a
combo box to a value for a new instance of an object.
@param trim_tokens Indicate whether the tokens should be trimmed when trying to
match - the default is not to trim.
@exception Exception if the string is not found in the JComboBox.
*/
public static void selectTokenMatches (	JComboBox c, boolean ignore_case, String delimiter, int flags,
	int token, String item, String default_item, boolean trim_tokens )
throws Exception
{	// Does not look like Choice.select(String) throws an exception if the
	// item is not found so go through the list every time...
	// Get the list size...
	int size = c.getItemCount();
	List<String> tokens = null;
	int ntokens = 0;
	String choice_token;
	for ( int i = 0; i < size; i++ ) {
	    if ( delimiter != null ) {
	        // Use the delimiter to split the choice
    		tokens = StringUtil.breakStringList ( c.getItemAt(i).toString(), delimiter, flags );
    		ntokens = 0;
    		if ( tokens != null ) {
    			ntokens = tokens.size();
    		}
    		if ( ntokens <= token ) {
    			continue;
    		}
    		// Now compare.  Do not use region matches because we want an exact match on the token...
    		choice_token = tokens.get(token);
	    }
	    else {
	        choice_token = c.getItemAt(i).toString();
	    }
		if ( trim_tokens ) {
			choice_token = choice_token.trim();
		}
		if ( ignore_case ) {
			if ( choice_token.equalsIgnoreCase(item) ) {
				c.setSelectedIndex ( i );
				return;
			}
		}
		else {
		    if ( choice_token.equals(item) ) {
				c.setSelectedIndex ( i );
				return;
			}
		}
	}
	// If here, allow the default to be selected.  Because all choices need
	// to be evaluated again, just call the code recursively using the
	// default instead of the item...
	if ( default_item != null ) {
		selectTokenMatches ( c, ignore_case, delimiter, flags, token, default_item, null );
	}
	else {
	    // No default was specified so throw an exception...
		throw new Exception ( "Token " + token + " \"" + item + "\" not found in available choices" );
	}
}

/**
Sets the 'pretty' version of the application name, which can be displayed in
dialog boxes, frames, and window titles.
@param appName the application name to use.  If null, the application name
will be set to an empty string ("").
*/
public static void setAppNameForWindows(String appName) {
	if (appName == null) {
		__applicationName = "";
	}
	else {
	    __applicationName = appName;
	}
}

/**
Enable a component if the state is different from the requested state.  The
benefit of this method is that it only changes the state if necessary.  Changing
the state without the check may result in unnecessary flashing of the interface.
The object is also checked for null.
@param component A component to enable/disable.
@param enabled Indicates whether to enable or disable the component.
*/
public static void setEnabled ( Component component, boolean enabled )
{	if ( component == null ) {
		return;
	}
	if ( enabled ) {
		// Need to enable the item, but only if it is not already...
		if ( !component.isEnabled() ) {
			component.setEnabled ( true );
		}
	}
	else {
	    // Need to disable the item, but only if it is not already...
		if ( component.isEnabled() ) {
			component.setEnabled ( false );
		}
	}
}

/**
Sets an icon in a JFrame; all JDialogs that are opened with this frame as their
parent will take this icon, as well.
@param frame the frame in which to set an icon.
@param i the ImageIcon to use as the window icon.
*/
public static void setIcon(JFrame frame, ImageIcon i) {
	String routine = "JGUIUtil.setIcon()";
	try {
		if (i != null) {
			Image image = i.getImage();
			if (image != null) {
				frame.setIconImage(image);
			}
		}
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
	}
}

/**
Sets the icon to use for an application.
@param i the ImageIcon to use.  Can be null.
*/
public static void setIconImage(ImageIcon i) {
	__applicationIcon = i;
}

/**
Sets the icon to use for an application.  The icon is read from the specified location and stored.
@param location a path to an icon or the location within a jar file.
@throws Exception if no icon could be found at the specified location.
*/
public static void setIconImage(String location) 
throws Exception {
	ImageIcon icon = loadIconImage(location);
	if (icon != null) {
		setIconImage(icon);
	}
}

public static void setWaitCursor(JDialog dialog, boolean state) {
/*
	if (state) {
		setCursor(dialog, Cursor.WAIT_CURSOR);
	}
	else {
		setCursor(dialog, Cursor.DEFAULT_CURSOR);
	}
*/
	if (dialog == null) {
		return;
	}

	Component parent = SwingUtilities.getRoot((Component)dialog);
	Component glassPane = null;

	if (parent != null && parent.isShowing()) {
		glassPane = dialog.getGlassPane();
		if (state) {
			glassPane.addKeyListener(new KeyAdapter() {});
			glassPane.addMouseListener(new MouseAdapter() {});
			glassPane.setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		else {
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		glassPane.setVisible(state);
	}
	
	__waitCursor = state;
}

/**
Activates the wait cursor for a JFrame.
@param frame the JFrame for which to set the wait cursor
@param state whether to set the wait cursor to on (true) or off (false)
*/
public static void setWaitCursor(JFrame frame, boolean state) {
/*
	System.out.println("[[[[[[[[[[[[[        set wait cursor: " + state);
	try {
	throw new Exception ("wait");
	}
	catch (Exception e) {
	e.printStackTrace();
	}
*/

	// still doesn't work as well as it should

	if (frame == null) {
		return;
	}

	Component parent = SwingUtilities.getRoot((Component)frame);
	Component glassPane = null;

	if (parent != null && parent.isShowing()) {
		glassPane = frame.getGlassPane();
		if (state) {
			glassPane.addKeyListener(new KeyAdapter() {});
			glassPane.addMouseListener(new MouseAdapter() {});
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		else {
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		glassPane.setVisible(state);
	}
	
	__waitCursor = state;
}

/**
Sets the current java look and feel to be like the current System theme 
(Windows, Motif, etc) or the Java Metal theme.
@param set if set is true, the look and feel is set to be the standard system
look and feel.  If false, the Java metal theme is used.
*/
public static void setSystemLookAndFeel(boolean set) {
	boolean error = false;
	if (set == true) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			error = true;
			if (Message.isDebugOn) {
				Message.printWarning(2, "", e);
			}
		}			
	}
	else {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch (Exception e) {
			error = true;
			if (Message.isDebugOn) {
				Message.printWarning(2, "", e);
			}
		}
	}

	if (error) {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch (Exception e) {
			Message.printWarning(1, "setWindowsLookAndFeel", "Unable to set a new look and feel.");
			Message.printWarning(2, "setWindowsLookAndFeel", e);
		}
	}
}

/**
Returns a String version of a JCheckBox's state suitable for display on a 
text form.  The JCheckbox will be represented by <pre>"[X]"</pre> if it is
selected, otherwise by <pre>"[ ]"</pre>.
@return a String version of a JCheckBox's state suitable for display on a form.
*/
public static String toString(JCheckBox box) {
	if (box.isSelected()) {
		return "[X]";
	}
	return "[ ]";
}

/**
Return the text from a TextArea as a Vector of strings, each of which has had
the newline removed.  This is useful for exporting the text to a file or for
printing.  At some point Sun may change the delimiter returned but we can isolate to this routine.
@param ta TextArea of interest.
@return A list of strings containing the text from the text area or a 
Vector with no elements if a null TextArea.
*/
public static List<String> toList (JTextArea ta) {
	if ( ta == null ) {
		return new Vector();
	}
	List<String> v = StringUtil.breakStringList ( ta.getText(), "\n", 0 );
	// Just to be sure, remove any trailing carriage-return characters from the end...
	String string;
	for ( int i = 0; i < v.size(); i++ ) {
		string = v.get(i);
		v.set(i,StringUtil.removeNewline(string));
	}
	return v;
}

/**
Write a JTextArea to a file.  For example, this can be used to save a report
that is displayed in a JTextArea.
@param ta JTextArea to write.
@param filename Name of file to write.
@exception if there is an error writing the file.
*/
public static void writeFile ( JTextArea ta, String filename )
throws Exception
{	List v = toList ( ta );
	PrintWriter out = new PrintWriter( new FileWriter( filename ) );
	//
	// Write each element of the _export Vector to a file.
	//
	int size = v.size();
	for ( int i = 0; i < size; i++ ) {
		out.println ( (String)v.get(i) );
	}
	//
	// close the PrintStream Object
	//
	out.flush(); 
	out.close(); 
}

}