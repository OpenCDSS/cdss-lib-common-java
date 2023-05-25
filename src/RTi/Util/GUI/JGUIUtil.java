// JGUIUtil - Swing GUI utility methods class, containing static methods

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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
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
This class provides useful static functions for handling SWING GUI (graphical user interface) components.
This class extends GUIUtil and so inherits the ability to work with AWT components, as well.
*/
public abstract class JGUIUtil extends GUIUtil {

// TODO sam 2017-02-26 seems to only be used in GeoView - maybe should not be global static here?
/**
The current status of the wait cursor, as set by the setWaitCursor method.
This will therefore be global within an application.
*/
private static boolean __waitCursor = false;

/**
The icon to use for an application.
*/
private static ImageIcon __applicationIcon = null;

/**
The 'pretty' version of the application name that can be displayed in window titles, dialog boxes, and more.
*/
private static String __applicationName = "";

/**
Given a JList with selected items, add the specified string to the front of the
items if it is not already at the front of the items.
After the changes, the originally selected items are still selected.
This is useful, for example, when a popup menu toggles the contents of a list back and forth.
The list model must be the DefaultListModel or an extended class.
REVISIT JAVADOC: see removeStringFromSelected
@param list JList to modify.
@param prefix String to add.
*/
public static <I> void addStringToSelected ( JList<String> list, String prefix ) {
	if ( (list == null) || (prefix == null) ) {
		return;
	}
	int selected_indices[] = list.getSelectedIndices();
	int selected_size = selectedSize ( list );
	int len = prefix.length();
	DefaultListModel<String> model = (DefaultListModel<String>)list.getModel();
	String item;
	for ( int i = 0; i < selected_size; i++ ) {
		item = model.getElementAt(selected_indices[i]);
		if ( item.trim().regionMatches(true,0,prefix,0,len) ) {
			model.setElementAt ( prefix + item, selected_indices[i] );
		}
	}
	// Make sure the selected indices remain as before.
	list.setSelectedIndices ( selected_indices );
}

/**
Add an array of strings to a JList.  This is useful when a standard set of choices are available.
@param comboBox Choice to add items to.
@param items Items to add.
*/
public static void addToJComboBox ( JComboBox<String> comboBox, String[] items ) {
	if ( (comboBox == null) || (items == null) ) {
		return;
	}
	for ( String s : items ) {
		comboBox.addItem ( s );
	}
}

/**
Add a list of strings to a JList.  This is useful when a standard set of choices are available.
The toString() method of each object in the list is called, so even non-String items can be added.
@param comboBox Choice to add items to.
@param items Items to add.
*/
public static void addToJComboBox ( JComboBox<String> comboBox, List<Object> items ) {
	if ( (comboBox == null) || (items == null) ) {
		return;
	}
	for ( Object item: items ) {
		comboBox.addItem ( item.toString() );
	}
}

/**
Clears the system clipboard of whatever data exists on it.
This should be called at System.exit() time by applications that use the clipboard,
otherwise any data put on the clipboard by the application will remain there and use system resources.
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
public static void close (JFrame frame) {
	if ( frame != null ) {
		frame.setVisible ( false );
		frame.dispose();
	}
}

/**
Compute the optimal coordinates to display a JPopupMenu.
This is necessary because of a limitation in JPopupMenu where it does not
automatically adjust for cases where some of the menu would be displayed off the screen.
See JavaSoft bug 4425878.
@param pt Candidate point (e.g., from MouseEvent.getPoint()).
@param c Component that menu is associated with (e.g., from MouseEvent.getComponent()).
@param menu JPopupMeni instance to check.
@return a Point containing the optimal coordinates.
*/
public static Point computeOptimalPosition ( Point pt, Component c, JPopupMenu menu ) {
	// The code below is partially taken from the bug report.
	// However, the fix there for computing coordinates was actually pretty simplistic,
	// so an improvement has been implemented here.
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
Copies a String to the system clipboard.
Once the string has been copied to the clipboard, it can be pasted into other applications.
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
@param comp an array of all the JComponents on the form that can be enabled when something is selected.
@param compNeverEnabled an array of the components in comp[] that should never be editable.
These components are disabled after there others are enabled.  Specify as -1 to ignore.
@param editable Indicates whether the form is editable or not.
If the form is not editable, then some components may be disabled to prevent input.
*/
public static void enableComponents ( JComponent[] comp, int[] compNeverEnabled, boolean editable ) {
	for (int i = 0; i < comp.length; i++) {
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
			@SuppressWarnings("rawtypes")
			JComboBox cb = (JComboBox)comp[i];
			if (editable) {
				setEnabled ( comp[i], true );
				cb.setEditable(true);
			}
			else {
			    cb.setEditable(false);
				setEnabled ( comp[i], false );
			}
		}
		else {
		    setEnabled ( comp[i], true );
		}
	}
	if ( compNeverEnabled != null ) {
		for (int i = 0; i < compNeverEnabled.length; i++) {
			if ( compNeverEnabled[i] >= 0 ) {
				if ( comp[compNeverEnabled[i]] instanceof JTextComponent ) {
					// Text is hard to read when disabled so just set not editable.
					((JTextComponent)comp[compNeverEnabled[i]]).setEditable(false);
				}
				else {
				    // All other components.
					setEnabled (comp[compNeverEnabled[i]], false );
				}
			}
		}
	}
}

/**
Disable a list of components.  For example, use when no data item is selected.
See enableComponents(), which will also disable components globally if the editable flag is set to false.
@param comp an array of all the JComponents on the form to be disabled.
@param cleartext If true, text components will be cleared when disabled.
If false, the text component text will not be changed.
*/
public static void disableComponents ( JComponent[] comp, boolean cleartext ) {
	for (int i = 0; i < comp.length; i++) {
		if (comp[i] instanceof JTextComponent) {
			if ( cleartext ) {
				((JTextComponent)comp[i]).setText("");
			}
			((JTextComponent)comp[i]).setEditable(false);
		}
		else if (comp[i] instanceof JComboBox) {
			@SuppressWarnings("rawtypes")
			JComboBox cb = (JComboBox)comp[i];
			setEnabled ( comp[i], false );
			cb.setEditable(false);
		}
		else {
		    setEnabled ( comp[i], false );
		}
	}
}

/**
Forces the component to repaint immediately.
The JComponent is guaranteed to be repainted by the time this method returns.
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
Returns the 'pretty' version of the application name,
which can be used in windows, dialog boxes, and titles.
@return the nice version of the application name.  Will never return <tt>null</tt>.
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
public static int indexOf (	JList<String> list, String item, boolean selected_only, boolean ignore_case ) {
	if ( (list == null) || (item == null) || (item.length() == 0) ) {
		return -1;
	}
	int size = 0;
	String list_item = null;
	DefaultListModel<String> model = (DefaultListModel<String>)list.getModel();
	if ( selected_only ) {
		size = selectedSize ( list );
		int [] selected_indices = list.getSelectedIndices();
		for ( int i = 0; i < size; i++ ) {
			list_item = model.elementAt( selected_indices[i]);
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
			list_item = model.elementAt(i);
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
See the overloaded method for a full description.
This version matches any substring (when flag=CHECK_SUBSTRINGS) and is case-sensitive.
@param comboBox SimpleJComboBox object.
@param compare String to compare comboBox items against.  If null, false is returned.
@param flag compare criteria (CHECK_SUBSTRINGS or NONE); currently any substring that matches will return true
@param delimiter String containing delimiter to parse for flag=CHECK_SUBSTRINGS;
@param index Index location where the compare String was located
(index[0] is set to the first ComboBox item that matches).
*/
public static boolean isSimpleJComboBoxItem ( SimpleJComboBox comboBox,
    String compare, int flag, String delimiter, int[] index ) {
    return isSimpleJComboBoxItem ( comboBox, compare, flag, delimiter, -1, index, false );
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
@param flag compare criteria (CHECK_SUBSTRINGS or NONE);
currently any trimmed substring that matches will return true
@param delimiter String containing delimiter(s) to parse for flag=CHECK_SUBSTRINGS.
Each character in the string will be considered a delimiter.
However, if the delimiter string starts with "seq:" the multi-character string is used for the delimiter
(e.g. "seq: - " to use a dash surrouned by spaces).
@param compareIndex if >= 0, the substring part to compare (e.g.,
may be null if using flag=NONE; specify -1 to compare all parts
@param index Index location in ComboBox data where the compare String was located
(index[0] is set to the first ComboBox item that matches).
@param ignoreCase true to ignore case in comparisons; false to require that case matches
This is filled in unless it is passed as null.
For example use this when checking substrings so that an item can be selected
(rather than setting a full string that may not totally match).
@return returns true if compare exist in the comboBox items list, false otherwise.
*/
public static boolean isSimpleJComboBoxItem ( SimpleJComboBox comboBox,
	String compare, int flag, String delimiter, int compareIndex, int[] index, boolean ignoreCase ) {
	String curItem; // Current Choice item.
	String delimiterOrig = delimiter;
   	if ( (delimiter != null) && delimiter.startsWith("seq:") ) {
   		delimiter = delimiter.substring(4); // Delimiter string without leading "seq".
   		//Message.printStatus(2, "isSimpleJComboBoxItem", "Using sequence delimiter \"" + delimiter + "\"");
   	}

	if ( compare == null ) {
		return false;
	}
    // Initialize variables.
    compare = compare.trim();
    int size = comboBox.getItemCount(); // Number of items in the choices.

    int tokenPos = 0;
    for( int i=0; i<size; i++ ) {
        curItem = comboBox.getItem( i ).trim();
		//Message.printStatus(2, "isSimpleJComboBoxItem", "Checking item \"" + curItem + "\" length " + curItem.length() );
        tokenPos = -1;
        if ( flag == CHECK_SUBSTRINGS ) {
            // Split the combo box item using the delimiter and check the parts.
        	List<String> choiceParts = null;
        	if ( delimiterOrig.startsWith("seq:") ) {
        		// The delimiter string contains multiple characters that must be matched to indicate a delimiter.
        		choiceParts = new ArrayList<>();
        		int dlen = delimiter.length(); // Length of the delimiter.
        		int pos = 0; // Position in the string.
        		int delimEnd = -1; // Position of last character of delimiter.
        		while ( pos < curItem.length() ) {
        			//Message.printStatus(2, "isSimpleJComboBoxItem", "Checking at pos " + pos);
        			// See if the delimiter exists at the string position.
        			if ( (pos + dlen) > curItem.length() ) {
        				// Can't check substring but allow increment so will reach end of string below.
        				++pos;
        			}
        			else {
        				//Message.printStatus(2, "isSimpleJComboBoxItem", "Checking \"" + curItem.substring(pos,pos+dlen) + "\"");
        				if ( curItem.substring(pos,pos+dlen).equals(delimiter) ) {
        					// Found another token.
        					if ( pos == 0 ) {
        						// Delimiter is at the start so consider as an empty string token.
        						choiceParts.add("");
        					}
        					else {
        						// Token is between delimiters.
        						String token = curItem.substring((delimEnd+1),pos);
        						choiceParts.add(token);
        						//Message.printStatus(2, "isSimpleJComboBoxItem", "Added token \"" + token + "\"");
        					}
        					// Advance over the delimiter.
        					delimEnd = pos + dlen - 1;
        					pos = delimEnd + 1;
        				}
        				else {
        					// Advance the position by one character.
        					++pos;
        				}
        			}
        			// Handle the end of string.
        			if ( pos >= curItem.length() ) {
        				// End of string.
       					//Message.printStatus(2, "isSimpleJComboBoxItem", "End of string.");
        				if ( delimEnd >= 0 ) {
        					// Previously processed a delimiter so need to add the token at the end of the string.
        					String token = curItem.substring(delimEnd + 1);
        					choiceParts.add(token);
        					//Message.printStatus(2, "isSimpleJComboBoxItem", "Added end token \"" + token + "\"");
        				}
        			}
        		}
        	}
        	else {
        		// Each character in the delimiter indicates a delimiter.
        		choiceParts = StringUtil.breakStringList(curItem, delimiter, 0);
        	}
        	if ( (choiceParts != null) && (choiceParts.size() > 0) ) {
        		String subTrimmed;
        		for ( String sub : choiceParts ) {
        			++tokenPos;
        			subTrimmed = sub.trim(); // Trim substring (generally what is needed to compare core content value).
        			// If a match occurs, return true and the index in the list in which the match was found.
        			// If a requested compare string index was specified, only compare that part.
        			if ( (compareIndex < 0) || (compareIndex == tokenPos) ) {
        				if ( (ignoreCase && subTrimmed.equalsIgnoreCase(compare)) ||
        					(!ignoreCase && subTrimmed.equals(compare)) ) {
        					if ( index != null ) {
        						index[0] = i;
        					}
        					return true;
        				}
                    }
                }
            }
        }
		else if ( flag == NONE ) {
			// Compare to the curItem String directly.
            if ( (ignoreCase && curItem.equalsIgnoreCase(compare)) ||
                (!ignoreCase && curItem.equals(compare)) ) {
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
@param location the location at which the icon can be found,
either a path to an image on a drive or a path within a JAR file.
@return the ImageIcon that was loaded, or null if there was a problem loading the ImageIcon.
@throws exception if no file could be found at the specified location.
*/
public static ImageIcon loadIconImage(String location)
throws Exception {
	// first try loading the image as if it were specified in a JAR file.
	URL iconURL = ClassLoader.getSystemResource(location);
	if (iconURL == null) {
		// If that failed, try loading the image as if it were specified in a proper file name.
		File f = new File(location);
		if (!f.exists()) {
			throw new Exception("No icon could be found at location '" + location + "'");
		}
		iconURL = f.toURI().toURL();
		if (iconURL == null) {
			throw new Exception("No icon could be found at location '" + location + "'");
		}
	}
	ImageIcon i = new ImageIcon(iconURL);

	return i;
}

/**
Return a new SimpleJComboBox that contains a list of standard fonts.
Use the overloaded version to also include a longer list of fonts available in the local graphical environment.
@return a new SimpleJComboBox that contains a list of standard fonts.
*/
public static SimpleJComboBox newFontNameJComboBox ( ) {
	return newFontNameJComboBox ( true, false );
}

/**
Return a new SimpleJComboBox that contains a list of fonts.
@param includeCommonAtTop if true, include common fonts at the top for ease of selection.
@param includeLocal if true, include all available font family names from the local graphics environment.
@return a new SimpleJComboBox that contains a list of standard fonts.
*/
public static SimpleJComboBox newFontNameJComboBox ( boolean includeCommonAtTop, boolean includeLocal ) {
	SimpleJComboBox fonts = new SimpleJComboBox(false);
	// Always put common "generic" ones at top, which should be portable to any platform.
	if ( includeCommonAtTop ) {
		fonts.add ( "Arial" );
		fonts.add ( "Courier" );
		fonts.add ( "Courier New" );
		fonts.add ( "Helvetica" );
		fonts.add ( "Times Roman New" );
	}
	if ( includeLocal ) {
		// Get more fonts from the local computing environment - some risk these won't be on all machines.
		String [] localFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		// Add to end.
		for ( int i = 0; i < localFonts.length; i++ ) {
			fonts.add(localFonts[i]);
		}
	}
	return fonts;
}

/**
Return a new SimpleJComboBox that contains a list of standard font styles.
@return a new SimpleJComboBox that contains a list of standard font styles.
*/
public static SimpleJComboBox newFontStyleJComboBox () {
	SimpleJComboBox styles = new SimpleJComboBox(false);
	styles.add( "Plain" );
	styles.add( "PlainItalic" );
	styles.add( "Bold" );
	styles.add( "BoldItalic" );
	return styles;
}

/**
Given a list with selected items,
remove the specified string from the front of the items if is at the front of the items.
After the changes, the originally selected items are still selected.
This is useful, for example, when a popup menu toggles the contents of a list back and forth.
The list model must be the DefaultListModel or an extended class.
TODO JAVADOC: see addStringToSelected
@param list JList to modify.
@param prefix String to add.
*/
public static void removeStringFromSelected ( JList<String> list, String prefix ) {
	if ( (list == null) || (prefix == null) ) {
		return;
	}
	int selected_indices[] = list.getSelectedIndices();
	int selected_size = selectedSize ( list );
	int len = prefix.length();
	DefaultListModel<String> model = (DefaultListModel<String>)list.getModel();
	String item;
	for ( int i = 0; i < selected_size; i++ ) {
		item = model.getElementAt( selected_indices[i]);
		if ( item.trim().regionMatches(true,0,prefix,0,len) &&
			StringUtil.tokenCount( item," \t", StringUtil.DELIM_SKIP_BLANKS) > 1 ) {
			model.setElementAt (  item.substring(len).trim(), selected_indices[i] );
		}
	}
	// Make sure the selected indices remain as before.
	list.setSelectedIndices ( selected_indices );
}

/**
Select a single matching item in a JList.  Only the first match is selected.
The DefaultListModel or an extended class should be used for the list model.
@param list JList to select from.
@param item Item to select.
@param ignore_case Indicates whether case should be ignored when searching the list for a match.
*/
public static void select ( JList<String> list, String item, boolean ignore_case ) {
	if ( (list == null) || (item == null) ) {
		return;
	}
	DefaultListModel<String> model = (DefaultListModel<String>)list.getModel();
	int size = model.size();
	String list_item = null;
	for ( int i = 0; i < size; i++ ) {
		list_item = model.getElementAt(i);
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
public static <I> void selectAll ( JList<I> list ) {
	if ( list == null ) {
		return;
	}
	// There is no "select" method so need to select all.
	// Rather than do this item by item, send an array and do it all at once - this should hopefully give the best performance.
	int [] selected = new int[list.getModel().getSize()];
	int size = selected.length;
	for ( int i = 0; i < size; i++ ) {
		selected[i] = i;
	}
	list.setSelectedIndices ( selected );
}

/**
Return the index of the requested selected item.
For example, if there are 5 items selected out of 20 total,
requesting index 0 will return index of the first of the 5 selected items.
This is particularly useful when determining the first or last selected item in a list.
@param list JList to check.
@param selected_index Position in the selected rows list.
@return the position in the original data for the requested selected index or -1 if unable to determine.
*/
public static <I> int selectedIndex ( JList<I> list, int selected_index ) {
	if ( list == null ) {
		return -1;
	}
	int selected[] = list.getSelectedIndices();
	if ( selected != null ) {
		int length = selected.length;
		if ( selected_index > (length - 1) ) {
			return -1;
		}
		int pos = selected[selected_index];
		return pos;
	}
	return -1;
}

/**
Return the number of items selected in a JList.
@param list JList to check.
@return the number items selected in the JList, or 0 if a null List.
*/
public static <I> int selectedSize ( JList<I> list ) {
	if ( list == null ) {
		return 0;
	}
	int selected[] = list.getSelectedIndices();
	if ( selected != null ) {
		int length = selected.length;
		return length;
	}
	return 0;
}

/**
Select an item in a SimpleJComboBox, ignoring the case.
This is useful when the SimpleJComboBox shows a valid property but the property may not always exactly
match in case when read from a file or hand-edited.
@param c SimpleJComboBox to select from.
@param item SimpleJComboBox item to select as a string.
@exception Exception if the string is not found in the SimpleJComboBox.
*/
public static void selectIgnoreCase ( SimpleJComboBox c, String item )
throws Exception {
	// Does not look like SimpleJComboBox.select(String) throws an exception if the item is not found
	// (especially if the SimpleJComboBox is editable) so go through the list every time.
	// Get the list size.
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
Select an item in a JComboBox, comparing a specific token in the choices.
This is useful when the combo box shows an extended value (e.g., "Value - Description").
@param c JComboBox to select from.
@param ignore_case Indicates if case should be ignored when comparing strings.
@param delimiter String delimiter used by StringUtil.breakStringList().
@param flags Flags used by StringUtil.breakStringList().
@param token Token position in the JComboBox item, to be compared.
@param item String item to compare to JComboBox item tokens.
@param default_item String If null, only "item" is evaluated.
If not null and "item" is not found, then an attempt to match "default" is made,
using the same tokenizing parameters.  If a match is found, it is selected.
If a match is not found, an Exception is thrown.
This parameter is useful when defaulting a combo box to a value for a new instance of an object.
@exception Exception if the string is not found in the JComboBox.
*/
public static <I> void selectTokenMatches (	JComboBox<I> c, boolean ignore_case, String delimiter, int flags,
	int token, String item, String default_item )
throws Exception {
	selectTokenMatches ( c, ignore_case, delimiter, flags, token, item,	default_item, false );
}

/**
Select an item in a JComboBox, comparing a specific token in the choices.
This is useful when the combo box shows an extended value (e.g., Value - Description").
@param c JComboBox to select from.
@param ignore_case Indicates if case should be ignored when comparing strings.
@param delimiter String delimiter used by StringUtil.breakStringList().  If null, compare the whole string.
@param flags Flags used by StringUtil.breakStringList().
@param token Token position in the JComboBox item, to be compared.
@param item String item to compare to JComboBox item tokens.
@param default_item If null, only "item" is evaluated.
If not null and "item" is not found, then an attempt to match "default" is made, using the same tokenizing parameters.
If a match is found, it is selected.  If a match is not found, an Exception is thrown.
This parameter is useful when defaulting a combo box to a value for a new instance of an object.
@param trim_tokens Indicate whether the tokens should be trimmed when trying to match - the default is not to trim.
@exception Exception if the string is not found in the JComboBox.
*/
public static <I> void selectTokenMatches (	JComboBox<I> c, boolean ignore_case, String delimiter, int flags,
	int token, String item, String default_item, boolean trim_tokens )
throws Exception {
	// Does not look like Choice.select(String) throws an exception if the
	// item is not found so go through the list every time.
	// Get the list size.
	int size = c.getItemCount();
	List<String> tokens = null;
	int ntokens = 0;
	String choice_token;
	for ( int i = 0; i < size; i++ ) {
	    if ( delimiter != null ) {
	        // Use the delimiter to split the choice.
    		tokens = StringUtil.breakStringList ( c.getItemAt(i).toString(), delimiter, flags );
    		ntokens = 0;
    		if ( tokens != null ) {
    			ntokens = tokens.size();
    		}
    		if ( ntokens <= token ) {
    			continue;
    		}
    		// Now compare.  Do not use region matches because we want an exact match on the token.
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
	// If here, allow the default to be selected.Because all choices need to be evaluated again,
	// just call the code recursively using the default instead of the item.
	if ( default_item != null ) {
		selectTokenMatches ( c, ignore_case, delimiter, flags, token, default_item, null );
	}
	else {
	    // No default was specified so throw an exception.
		throw new Exception ( "Token " + token + " \"" + item + "\" not found in available choices" );
	}
}

/**
Sets the 'pretty' version of the application name,
which can be displayed in dialog boxes, frames, and window titles.
@param appName the application name to use.
If null, the application name will be set to an empty string ("").
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
Enable a component if the state is different from the requested state.
The benefit of this method is that it only changes the state if necessary.
Changing the state without the check may result in unnecessary flashing of the interface.
The object is also checked for null.
@param component A component to enable/disable.
@param enabled Indicates whether to enable or disable the component.
*/
public static void setEnabled ( Component component, boolean enabled ) {
	if ( component == null ) {
		return;
	}
	if ( enabled ) {
		// Need to enable the item, but only if it is not already.
		if ( !component.isEnabled() ) {
			component.setEnabled ( true );
		}
	}
	else {
	    // Need to disable the item, but only if it is not already.
		if ( component.isEnabled() ) {
			component.setEnabled ( false );
		}
	}
}

/**
Sets an icon in a JFrame; all JDialogs that are opened with this frame as their parent will take this icon, as well.
@param frame the frame in which to set an icon.
@param i the ImageIcon to use as the window icon.
*/
public static void setIcon(JFrame frame, ImageIcon i) {
	try {
		if (i != null) {
			Image image = i.getImage();
			if (image != null) {
				frame.setIconImage(image);
			}
		}
	}
	catch (Exception e) {
		String routine = "JGUIUtil.setIcon()";
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

// The following static data are used by setWaitCursor().
//Static map of glass pane component and adapters that were added to intercept events.
//Use these maps to clear adapters when going to normal state.
//Otherwise a new adapter will get added every time waiting is turned on.
private static HashMap<Component,KeyAdapter> waitKeyListenerMap = new HashMap<Component,KeyAdapter>();
private static HashMap<Component,MouseMotionAdapter> waitMouseMotionListenerMap = new HashMap<Component,MouseMotionAdapter>();

/**
Activates the wait cursor for a component and by default use the glass pane
if a JFrame or JDialog to intercept mouse and key actions while in wait mode (state=true).
@param component the JFrame or JDialog for which to set the wait cursor
@param state whether to set the wait cursor to on (true) or off (false)
*/
public static void setWaitCursor(Component component, boolean state) {
	// By default use the glass pane to intercept key and mouse events on the component
	// because this is the historical behavior.
	setWaitCursor(component, state, true);
}

/**
Activates the wait cursor for a top-level Swing container (JFrame, JDialog, JApplet).
@param component the top-level component for which to set the wait cursor
@param state whether to set the wait cursor to on (true) or off (false)
@param useGlassPaneToInterceptEvents whether to use the glass pane on the component
to intercept key and mouse events. This should only be specified as true
when the glass pane is not used in any other way, such as an overlaid mouse tracker.
*/
public static void setWaitCursor(Component component, boolean state, boolean useGlassPaneToInterceptEvents ) {
	if (component == null) {
		return;
	}

	Component rootPane = SwingUtilities.getRoot(component);
	Component glassPane = null;

	if (rootPane != null && rootPane.isShowing()) {
		if ( component instanceof JFrame ) {
			glassPane = ((JFrame)component).getGlassPane();
		}
		else if ( component instanceof JDialog ) {
			glassPane = ((JDialog)component).getGlassPane();
		}
		else if ( component instanceof JApplet ) {
			glassPane = ((JApplet)component).getGlassPane();
		}
		if (state) {
			// Setting state to wait.
			if ( useGlassPaneToInterceptEvents ) {
				// The following will intercept events on the component until the wait is disabled.
				// TODO sam 2017-02-25 problem is that repeated calls will add more and more listeners
				// that are never removed.  Is this really doing anything?
				if ( glassPane != null ) {
					// Temporarily add a key listener so that user's key events don't pass to the component.
					// This is removed later using the map to find the listener.
					WaitCursorKeyListener kl = new WaitCursorKeyListener(glassPane);
					glassPane.addKeyListener(kl);
					waitKeyListenerMap.put(glassPane,kl);
					// Temporarily add a mouse motion listener so that user's mouse events don't pass to the component.
					// This is removed later using the map to find the listener.
					WaitCursorMouseMotionListener mml = new WaitCursorMouseMotionListener(glassPane);
					glassPane.addMouseMotionListener(mml);
					waitMouseMotionListenerMap.put(glassPane,mml);
				}
			}
			if ( glassPane != null ) {
				glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
			rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		else {
			// Setting state to not wait (clear wait).
			if ( glassPane != null ) {
				glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				// Remove the listeners that were intercepting events.
				// Although the same listeners might be appropriate in the next call,
				// new listeners will get re-added above just to make sure the proper glass pane is used
				// (other code may manipulate the glass pane).
				waitKeyListenerMap.remove(glassPane);
				waitMouseMotionListenerMap.remove(glassPane);
			}
			rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		if ( useGlassPaneToInterceptEvents ) {
			// If the glass pane is set to not visible, then the adapters won't be active.
			glassPane.setVisible(state);
		}
	}

	__waitCursor = state;
}

/**
Sets the current java look and feel to be like the current System theme (Windows, Motif, etc) or the Java Metal theme.
@param set if set is true, the look and feel is set to be the standard system look and feel.
If false, the Java metal theme is used.
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
Returns a String version of a JCheckBox's state suitable for display on a text form.
The JCheckbox will be represented by <pre>"[X]"</pre> if it is selected, otherwise by <pre>"[ ]"</pre>.
@return a String version of a JCheckBox's state suitable for display on a form.
*/
public static String toString(JCheckBox box) {
	if (box.isSelected()) {
		return "[X]";
	}
	return "[ ]";
}

/**
Return the text from a TextArea as a list of strings, each of which has had the newline removed.
This is useful for exporting the text to a file or for printing.
At some point Sun may change the delimiter returned but we can isolate to this routine.
@param ta TextArea of interest.
@return A list of strings containing the text from the text area or a list with no elements if a null TextArea.
*/
public static List<String> toList (JTextArea ta) {
	if ( ta == null ) {
		return new ArrayList<>();
	}
	List<String> list = StringUtil.breakStringList ( ta.getText(), "\n", 0 );
	// Just to be sure, remove any trailing carriage-return characters from the end.
	String string = null;
	for ( int i = 0; i < list.size(); i++ ) {
		string = list.get(i);
		list.set(i,StringUtil.removeNewline(string));
	}
	return list;
}

/**
Write a JTextArea to a file.
For example, this can be used to save a report that is displayed in a JTextArea.
@param ta JTextArea to write.
@param filename Name of file to write.
@exception if there is an error writing the file.
*/
public static void writeFile ( JTextArea ta, String filename )
throws Exception {
	List<String> list = toList ( ta );
	PrintWriter out = new PrintWriter( new FileWriter( filename ) );
	// Write each element of the export list to a file.
	for ( String s : list ) {
		out.println ( s );
	}
	// Close the PrintStream Object.
	out.flush();
	out.close();
}

}