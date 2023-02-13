// GUIUtil - GUI utility methods class, containing static methods

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

import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.List;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.util.ArrayList;

import RTi.Util.String.StringUtil;

/**
This class provides useful static functions for handling GUI (graphical user interface) components.
Appropriate overloads for functions are provided where useful.
This class was previously named "GUI" but was renamed to be consistent with other utility classes.
See also the JGUIUtil class, which has features Swing "J" components.
*/
public abstract class GUIUtil
{

/**
Do not check substrings when determining if a String exists in a Choice.
*/
public final static int NONE = 0;

/**
Check substrings when determining if a String exists in a Choice.
*/
public final static int CHECK_SUBSTRINGS = 1;

/**
A general purpose bit flag that can be used in GUI component constructors.
*/
public final static int GUI_VISIBLE = 0x1;

/**
The following indicate whether the shift and control keys are pressed.
This is used with methods like updateListSelections.
*/
private static boolean _control_down = false;
private static boolean _shift_down = false;

/**
The following saves the last directory saved with a FileDialog.
*/
private static String _last_file_dialog_directory = null;

/**
Add a component to a container that uses GridBagLayout using the specified constraints.
<pre>
example:
Panel p;
GridBagConstraints gbc;
Label l;
p = new Panel();
p.setLayout( new GridBagLayout() );
GUIUtil.addComponent( p, l = new Label("test"),
	0, 0, 1, 1, 0, 0, gbc.NONE, gbc.WEST );
</pre>
@param container Container using the GridBagLayout (e.g., Panel).
@param component Component to add to the container.
@param gridx Relative grid location in the x direction (0+, starting at left).
@param gridy Relative grid location in the y direction (0+, starting at top).
@param gridwidth Number of columns the component occupies.
@param gridheight Number of rows the component occupies.
@param weightx Distributed spacing ratio in x-direction (0 - 1)
(e.g., if 3 components are added and 2 have weights of 1,
then extra space is distributed evenly among the two components).
@param weighty Distributed spacing ratio in y-direction (0 - 1).
@param fill Component's resize policy, as
GridBagConstraint.NONE, GridBagConstraint.BOTH, GridBagConstraint.HORIZONTAL, or GridBagConstraint.VERTICAL.
@param anchor Component's drift direction, as GridBagConstraint.CENTER,
GridBagConstraint.NORTH, GridBagConstraint.SOUTH, GridBagConstraint.EAST,
GridBagConstraint.WEST, GridBagConstraint.NORTHEAST, GridBagConstraint.NORTHWEST,
GridBagConstraint.SOUTHEAST, or GridBagConstraint.SOUTHWEST.
*/
public static void addComponent (Container container, Component component,
int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty,
int fill, int anchor) {
	addComponent(container, component, gridx, gridy, gridwidth, gridheight,
		(double)weightx, (double)weighty, fill, anchor);
}

/**
Add a component to a container that uses GridBagLayout using the specified constraints.
<pre>
example:
Panel p;
GridBagConstraints gbc;
Label l;
p = new Panel();
p.setLayout( new GridBagLayout() );
GUIUtil.addComponent( p, l = new Label("test"),
	0, 0, 1, 1, 0, 0, gbc.NONE, gbc.WEST );
</pre>
@param container Container using the GridBagLayout (e.g., Panel).
@param component Component to add to the container.
@param gridx Relative grid location in the x direction (0+, starting at left).
@param gridy Relative grid location in the y direction (0+, starting at top).
@param gridwidth Number of columns the component occupies.
@param gridheight Number of rows the component occupies.
@param weightx Distributed spacing ratio in x-direction (0 - 1)
(e.g., if 3 components are added and 2 have weights of 1,
then extra space is distributed evenly among the two components).
@param weighty Distributed spacing ratio in y-direction (0 - 1).
@param fill Component's resize policy, as GridBagConstraint.NONE, GridBagConstraint.BOTH, GridBagConstraint.HORIZONTAL,
or GridBagConstraint.VERTICAL.
@param anchor Component's drift direction, as GridBagConstraint.CENTER,
GridBagConstraint.NORTH, GridBagConstraint.SOUTH, GridBagConstraint.EAST,
GridBagConstraint.WEST, GridBagConstraint.NORTHEAST, GridBagConstraint.NORTHWEST,
GridBagConstraint.SOUTHEAST, or GridBagConstraint.SOUTHWEST.
*/
public static void addComponent (
	Container container, Component component,
	int gridx, int gridy, int gridwidth, int gridheight,
	double weightx, double weighty, int fill, int anchor ) {
	LayoutManager lm = container.getLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.fill = fill;
        gbc.anchor = anchor;
        ((GridBagLayout)lm).setConstraints(component, gbc);
        container.add (component);
}

/**
Add a component to a container that uses GridBagLayout using the specified constraints.
<pre>
example:
Panel p;
GridBagConstraints gbc;
Label l;
p = new Panel();
Insets insets = new Insets( 5,5,5,5 )
p.setLayout( new GridBagLayout() );
GUIUtil.addComponent( p, l = new Label("test"),
	0, 0, 1, 1, 0, 0, insets, gbc.NONE, gbc.WEST );
</pre>
@param container Container using the GridBagLayout (e.g., Panel).
@param component Component to add to the container.
@param gridx Relative grid location in the x direction (0+, starting at left).
@param gridy Relative grid location in the y direction (0+, starting at top).
@param gridwidth Number of columns the component occupies.
@param gridheight Number of rows the component occupies.
@param weightx Distributed spacing ratio in x-direction (0 - 1)
(e.g., if 3 components are added and 2 have weights of 1,
then extra space is distributed evenly among the two components).
@param weighty Distributed spacing ratio in y-direction (0 - 1).
@param insets External padding in pixels around the component.
@param fill Component's resize policy,
as GridBagConstraint.NONE, GridBagConstraint.BOTH, GridBagConstraint.HORIZONTAL, or GridBagConstraint.VERTICAL.
@param anchor Component's drift direction, as GridBagConstraint.CENTER,
GridBagConstraint.NORTH, GridBagConstraint.SOUTH, GridBagConstraint.EAST,
GridBagConstraint.WEST, GridBagConstraint.NORTHEAST, GridBagConstraint.NORTHWEST,
GridBagConstraint.SOUTHEAST, or GridBagConstraint.SOUTHWEST.
*/
public static void addComponent (
	Container container, Component component,
	int gridx, int gridy, int gridwidth, int gridheight,
	int weightx, int weighty, Insets insets, int fill, int anchor) {
	addComponent(container, component, gridx, gridy, gridwidth, gridheight,
		(double)weightx, (double)weighty, insets, fill, anchor);
}


/**
Add a component to a container that uses GridBagLayout using the specified constraints.
<pre>
example:
Panel p;
GridBagConstraints gbc;
Label l;
p = new Panel();
Insets insets = new Insets( 5,5,5,5 )
p.setLayout( new GridBagLayout() );
GUIUtil.addComponent( p, l = new Label("test"),
	0, 0, 1, 1, 0, 0, insets, gbc.NONE, gbc.WEST );
</pre>
@param container Container using the GridBagLayout (e.g., Panel).
@param component Component to add to the container.
@param gridx Relative grid location in the x direction (0+, starting at left).
@param gridy Relative grid location in the y direction (0+, starting at top).
@param gridwidth Number of columns the component occupies.
@param gridheight Number of rows the component occupies.
@param weightx Distributed spacing ratio in x-direction (0 - 1)
(e.g., if 3 components are added and 2 have weights of 1,
then extra space is distributed evenly among the two components).
@param weighty Distributed spacing ratio in y-direction (0 - 1).
@param insets External padding in pixels around the component.
@param fill Component's resize policy,
as GridBagConstraint.NONE, GridBagConstraint.BOTH, GridBagConstraint.HORIZONTAL, or GridBagConstraint.VERTICAL.
@param anchor Component's drift direction, as GridBagConstraint.CENTER,
GridBagConstraint.NORTH, GridBagConstraint.SOUTH, GridBagConstraint.EAST,
GridBagConstraint.WEST, GridBagConstraint.NORTHEAST, GridBagConstraint.NORTHWEST,
GridBagConstraint.SOUTHEAST, or GridBagConstraint.SOUTHWEST.
*/
public static void addComponent (
	Container container, Component component,
	int gridx, int gridy, int gridwidth, int gridheight,
	double weightx, double weighty, Insets insets, int fill, int anchor) {
	LayoutManager lm = container.getLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.insets = insets;
        gbc.fill = fill;
        gbc.anchor = anchor;
        ((GridBagLayout)lm).setConstraints(component, gbc);
        container.add (component);
}

/**
This version is identical to the previous addComponent() except that the
insets are passed as 4 separate integer values rather than one Inset value.
@param container Container using the GridBagLayout (e.g., Panel).
@param component Component to add to the container.
@param gridx Relative grid location in the x direction (0+, starting at left).
@param gridy Relative grid location in the y direction (0+, starting at top).
@param gridwidth Number of columns the component occupies.
@param gridheight Number of rows the component occupies.
@param weightx Distributed spacing ratio in x-direction (0 - 1)
(e.g., if 3 components are added and 2 have weights of 1,
then extra space is distributed evenly among the two components).
@param weighty Distributed spacing ratio in y-direction (0 - 1).
@param top_inset External top padding in pixels around the component.
@param left_inset External left padding in pixels around the component.
@param bottom_inset External bottom padding in pixels around the component.
@param right_inset External right padding in pixels around the component.
@param fill Component's resize policy,
as GridBagConstraint.NONE, GridBagConstraint.BOTH, GridBagConstraint.HORIZONTAL, or GridBagConstraint.VERTICAL.
@param anchor Component's drift direction, as GridBagConstraint.CENTER,
GridBagConstraint.NORTH, GridBagConstraint.SOUTH, GridBagConstraint.EAST,
GridBagConstraint.WEST, GridBagConstraint.NORTHEAST, GridBagConstraint.NORTHWEST,
GridBagConstraint.SOUTHEAST, or GridBagConstraint.SOUTHWEST.
*/
public static void addComponent(Container container, Component component,
	int gridx, int gridy, int gridwidth, int gridheight, int weightx,
	int weighty, int top_inset, int left_inset, int bottom_inset,
	int right_inset, int fill, int anchor) {
	addComponent(container, component, gridx, gridy, gridwidth, gridheight,
		(double)weightx, (double)weighty, top_inset, left_inset,
		bottom_inset, right_inset, fill, anchor);
}

/**
This version is identical to the previous addComponent() except that the
insets are passed as 4 separate integer values rather than one Inset value.
@param container Container using the GridBagLayout (e.g., Panel).
@param component Component to add to the container.
@param gridx Relative grid location in the x direction (0+, starting at left).
@param gridy Relative grid location in the y direction (0+, starting at top).
@param gridwidth Number of columns the component occupies.
@param gridheight Number of rows the component occupies.
@param weightx Distributed spacing ratio in x-direction (0 - 1)
(e.g., if 3 components are added and 2 have weights of 1,
then extra space is distributed evenly among the two components).
@param weighty Distributed spacing ratio in y-direction (0 - 1).
@param top_inset External top padding in pixels around the component.
@param left_inset External left padding in pixels around the component.
@param bottom_inset External bottom padding in pixels around the component.
@param right_inset External right padding in pixels around the component.
@param fill Component's resize policy,
as GridBagConstraint.NONE, GridBagConstraint.BOTH, GridBagConstraint.HORIZONTAL, or GridBagConstraint.VERTICAL.
@param anchor Component's drift direction, as GridBagConstraint.CENTER,
GridBagConstraint.NORTH, GridBagConstraint.SOUTH, GridBagConstraint.EAST,
GridBagConstraint.WEST, GridBagConstraint.NORTHEAST, GridBagConstraint.NORTHWEST,
GridBagConstraint.SOUTHEAST, or GridBagConstraint.SOUTHWEST.
*/
public static void addComponent(Container container, Component component,
	int gridx, int gridy, int gridwidth, int gridheight, double weightx,
	double weighty, int top_inset, int left_inset, int bottom_inset,
	int right_inset, int fill, int anchor) {
	LayoutManager lm = container.getLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets ( top_inset, left_inset, bottom_inset, right_inset );
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.insets = insets;
        gbc.fill = fill;
        gbc.anchor = anchor;
        ((GridBagLayout)lm).setConstraints(component, gbc);
        container.add (component);
}

/**
Given a list with selected items, add the specified string to the front of the
items if it is not already at the front of the items.
After the changes, the originally selected items are still selected.
This is useful, for example, when a popup menu toggles the contents of a list back and forth.
@param list List to modify.
@param prefix String to add.
*/
public static void addStringToSelected ( List list, String prefix ) {
	if ( (list == null) || (prefix == null) ) {
		return;
	}
	int selected_indexes[] = list.getSelectedIndexes();
	int selected_size = selectedSize ( list );
	int len = prefix.length();
	for ( int i = 0; i < selected_size; i++ ) {
		if ( !list.getItem( selected_indexes[i]).trim().regionMatches(true,0,prefix,0,len) ) {
			list.replaceItem ( prefix + list.getItem(selected_indexes[i]), selected_indexes[i] );
		}
	}
	// Make sure the selected indices remain as before.
	select ( list, selected_indexes );
}

/**
Add an array of strings to a list.  This is useful when a standard set of choices are available.
@param choice Choice to add items to.
@param items Items to add.
*/
public static void addToChoice ( Choice choice, String[] items ) {
	if ( (choice == null) || (items == null) ) {
		return;
	}
	for ( int i = 0; i < items.length; i++ ) {
		choice.add ( items[i] );
	}
}

/**
Add a list of strings to a list.  This is useful when a standard set of choices are available.
The toString() method of each object in the list is called, so even non-String items can be added.
@param choice Choice to add items to.
@param items Items to add.
*/
public static void addToChoice ( Choice choice, java.util.List<Object> items ) {
	if ( (choice == null) || (items == null) ) {
		return;
	}
	int size = items.size();
	for ( int i = 0; i < size; i++ ) {
		choice.add ( items.get(i).toString() );
	}
}

/**
Center a Component in the screen.  For example, center on the screen where the main JFrame exists.
<b>NOTE</b>: Make sure to call this <b>AFTER</b> calling pack(), or else it will produce unexpected results.
This seems to properly centers the component on the screen that originated the component.
However, some components (JFrames?) require that the parent component be specified (see the overloaded version).
<pre>
example:
JDialog d;
d.pack();
GUIUtil.center( d, parentFrame );
</pre>
@param c Component object to center
*/
public static void center ( Component c ) {
	// Call overloaded version and pass component as parent.
	// This will center on screen where component originated.
	center ( c, c );
}

/**
Center a Component in the screen using the current frame and screen dimensions.
<b>NOTE</b>: Make sure to call this <b>AFTER</b> calling pack(), or else it will produce unexpected results.
This always centers the component on the first screen (screen 0).
<pre>
example:
JFrame f;
f.pack();
GUIUtil.center( f, parent );
</pre>
@param c Component object.
@param parent parent of c that defines screen on which to center
*/
public static void center ( Component c, Component parent ) {
	Dimension component = c.getSize();
	int componentWidth = component.width;
	int componentHeight = component.height;
	Toolkit kit = c.getToolkit();
	if ( parent == null ) {
	    // Get dimensions for screen (always primary screen = screen 0).
	    Dimension screenDimension = kit.getScreenSize();

	    // Determine heights and widths.

	    int screenWidth = screenDimension.width;
	    int screenHeight = screenDimension.height;

	    // Determine centered coordinates and set the location.
	    int x = (screenWidth-componentWidth) / 2;
	    int y = (screenHeight-componentHeight) / 2;

	    // Adjust if x or y are off the screen to make sure window controls are visible.
	    if ( x < 0 ) {
	    	x = 0;
	    }
	    if ( (y + componentHeight) > screenHeight) {
	    	y = 0;
	    }
	    c.setLocation ( x, y  );
	}
	else {
		// Try newer approach for multiple monitors.
		GraphicsConfiguration gc = parent.getGraphicsConfiguration();
		Rectangle parentScreenBounds = gc.getBounds();
		// x and y of the bounds will be zero for the primary screen.
		// x will be the width of the primary if two devices (monitors) and the second is used by the parent (y will be zero).
	    // Adjust if x or y are off the screen to make sure window controls are visible.
		int x = parentScreenBounds.x + (parentScreenBounds.width - componentWidth)/2;
		int y = parentScreenBounds.y + (parentScreenBounds.height - componentHeight)/2;
	    if ( x < 0 ) {
	    	x = parentScreenBounds.x;
	    }
	    if ( (y + componentHeight) > parentScreenBounds.height) {
	    	y = parentScreenBounds.y;
	    }
	    // Set top left corner.
		c.setLocation(x,y);
	}
}

/**
Hides and dispose of a Frame Object.
@param frame Frame object to hide.
*/
public static void close ( Frame frame ) {
	if ( frame != null ) {
		frame.setVisible ( false );
		frame.dispose();
	}
}

/**
Indicate whether the control key is pressed.  This is used with the setControlDown() method.
@return if Control is pressed.
*/
public static boolean controlDown () {
	return _control_down;
}

/**
Create a canvas and set the color.
This is useful when a canvas is needed as a place-holder because a full canvas object cannot be created for some reason
(no data, error, etc.).
@return A new instance of a canvas of specified size and background color.
@param width Width of the canvas.
@param height Height of the canvas.
@param color Background color for the canvas.
If the color is null, white is used.
*/
public static Canvas createCanvas ( int width, int height, Color color ) {
	Canvas canvas = new Canvas ();
	if ( color == null ) {
		canvas.setBackground ( Color.white );
	}
	else {
		canvas.setBackground ( color );
	}
	canvas.setSize ( width, height );
	return canvas;
}

/**
Create a canvas with a white background.
@return A new instance of a canvas of specified size and white background.
@param width Width of the canvas.
@param height Height of the canvas.
*/
public static Canvas createCanvas ( int width, int height ) {
	return createCanvas ( width, height, Color.white );
}

/**
Deselect all items in a List.
@param list List to deselect all items.
*/
public static void deselectAll ( List list ) {
	if ( list == null ) {
		return;
	}
	int [] selected = list.getSelectedIndexes();
	int size = 0;
	if ( selected != null ) {
		size = list.getItemCount();
	}
	for ( int i = 0; i < size; i++ ) {
		list.deselect ( i );
	}
}

/**
Return the font style (mask of Font.BOLD, Font.ITALIC, Font.PLAIN) given a string.
The string is searched for case-independent occurances of "Bold",
"Italic", "Plain" and the integer equivalent is returned.
@param style Font style as string.  Return Font.PLAIN if the style is not a standard value.
@return a font style integer.
*/
public static int getFontStyle ( String style ) {
	int istyle = 0;
	if ( StringUtil.indexOfIgnoreCase(style,"Plain",0) >= 0 ) {
		istyle |= Font.PLAIN;
	}
	else if ( StringUtil.indexOfIgnoreCase(style,"Italic",0) >= 0 ) {
		istyle |= Font.ITALIC;
	}
	else if ( StringUtil.indexOfIgnoreCase(style,"Bold",0) >= 0 ) {
		istyle |= Font.BOLD;
	}
	if ( istyle == 0 ) {
		return Font.PLAIN;
	}
	else {
		return istyle;
	}
}

/**
Return the last directory set with setLastFileDialogDirectory().
This will have the directory separator at the end of the string.
@return the last directory set with setLastFileDialogDirectory().
*/
public static String getLastFileDialogDirectory() {
	return _last_file_dialog_directory;
}

/**
Determine position of a string in a List.
@param list List to search.
@param item String item to search for.
@param selected_only Indicates if only selected items should be searched.
@param ignore_case Indicates whether to ignore case (true) or not (false).
@return The index of the first match, or -1 if no match.
*/
public static int indexOf (	List list, String item, boolean selected_only, boolean ignore_case ) {
	if ( (list == null) || (item == null) || (item.length() == 0) ) {
		return -1;
	}
	int size = 0;
	String list_item = null;
	if ( selected_only ) {
		size = selectedSize ( list );
		int selected_indexes[] = list.getSelectedIndexes();
		for ( int i = 0; i < size; i++ ) {
			list_item = list.getItem(selected_indexes[i]);
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
	else {	size = list.getItemCount();
		for ( int i = 0; i < size; i++ ) {
			list_item = list.getItem(i);
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
Determine if the specified compare String exists within a Choice.
<ul>
<li>	Can compare the compare String against substrings for each item
	in the choice object if FLAG is set to CHECK_SUBSTRINGS.</li>
<li>	To not compare against substrings, set FLAG to NONE.</li>
</ul>
@param choice Choice object.
@param compare String to compare choice items against.
@param FLAG compare criteria (i.e, CHECK_SUBSTRINGS, NONE).
@param delimiter String containing delimiter to parse for CHECK_SUBSTRINGS, may be null if using FLAG == NONE.
@param index Index location where the compare String was located at index[0]
@return returns true if compare exist in the choice items list, false otherwise.
This is filled in unless it is passed as null.
*/
public static boolean isChoiceItem( Choice choice, String compare, int FLAG, String delimiter, int[] index ) {
		int             size;           // Number of items in the Choice object.
        int             curIndex,       // Current character position.
                        length;         // Length of curItem.
        String          curItem;        // Current Choice item.
        String          curChar;        // Current character.

        // Initialize variables.
        compare = compare.trim();
        size = choice.getItemCount();

    for( int i=0; i<size; i++ ) {
        curItem = choice.getItem( i ).trim();
        String sub = curItem;

        // Check substring where substrings are delineated by spaces.
        if ( FLAG == CHECK_SUBSTRINGS ) {
            // Jump over all characters until the delimiter is reached.
            // Break the remaining String into a SubString and compare to the compare String.
            length = sub.length();
            for ( curIndex = 0; curIndex < length; curIndex++ ) {
                curChar = String.valueOf( curItem.charAt( curIndex ) ).trim();
                if ( curChar.equals(delimiter) ) {
                    sub = sub.substring( curIndex+1).trim();
                }
            }
            // Compare the remaining String, sub, to the compare String.
            // If a match occurs, return true and the index in the list in which the match was found.
            if ( compare.equals( sub ) ) {
                index[0] = i;
                return true;
            }
        }
		else if ( FLAG == NONE ) {
			// Compare to the curItem String directly.
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
Determine whether a string is in a List.
@param list List to search.
@param item String item to search for.
@param selected_only Indicates if only selected items should be searched.
@param ignore_case Indicates whether to ignore case (true) or not (false).
@return true if the item is in the List.
@deprecated us indexOf().
*/
public static boolean isListItem (	List list, String item, boolean selected_only, boolean ignore_case ) {
	if ( indexOf ( list, item, selected_only, ignore_case ) >= 0 ) {
		return true;
	}
	return false;
}

/**
Return a new Choice that contains a list of standard fonts.
*/
public static Choice newFontNameChoice () {
	Choice fonts = new Choice();
	fonts.addItem ( "Arial" );
	fonts.addItem ( "Courier" );
	fonts.addItem ( "Helvetica" );
	return fonts;
}

/**
Return a new Choice that contains a list of standard font styles.
@return a new Choice that contains a list of standard font styles.
*/
public static Choice newFontStyleChoice () {
	Choice styles = new Choice();
	styles.addItem ( "Plain" );
	styles.addItem ( "PlainItalic" );
	styles.addItem ( "Bold" );
	styles.addItem ( "BoldItalic" );
	return styles;
}

/**
Given a list with selected items, add the specified string to the front of the
items if it is not already at the front of the items.
After the changes, the originally selected items are still selected.
This is useful, for example, when a popup menu toggles the contents of a list back and forth.
@param list List to modify.
@param prefix String to add.
*/
public static void removeStringFromSelected ( List list, String prefix ) {
	if ( (list == null) || (prefix == null) ) {
		return;
	}
	int selected_indexes[] = list.getSelectedIndexes();
	int selected_size = selectedSize ( list );
	int len = prefix.length();
	for ( int i = 0; i < selected_size; i++ ) {
		if ( list.getItem( selected_indexes[i]).trim().regionMatches(true,0,prefix,0,len) &&
			StringUtil.tokenCount( list.getItem( selected_indexes[i])," \t", StringUtil.DELIM_SKIP_BLANKS) > 1 ) {
			list.replaceItem ( list.getItem(
			selected_indexes[i]).substring(len).trim(),
			selected_indexes[i] );
		}
	}
	// Make sure the selected indices remain as before.
	select ( list, selected_indexes );
}

/**
Selected the indicated items.
@param list List to select from.
@param selected_indices Indices in the list to select.
*/
public static void select ( List list, int selected_indices[] ) {
	if ( list == null ) {
		return;
	}
	if ( selected_indices == null ) {
		return;
	}
	for ( int i = 0; i < selected_indices.length; i++ ) {
		list.select ( selected_indices[i] );
	}
}

/**
Select a single item in a List.  Only the first match is selected.
@param list List to select from.
@param item Item to select.
@param ignore_case Indicates whether case should be ignored when searching the list for a match.
*/
public static void select ( List list, String item, boolean ignore_case ) {
	if ( (list == null) || (item == null) ) {
		return;
	}
	int size = list.getItemCount();
	String list_item = null;
	for ( int i = 0; i < size; i++ ) {
		list_item = list.getItem(i);
		if ( ignore_case ) {
			if ( list_item.equalsIgnoreCase(item) ) {
				list.select(i);
				return;
			}
		}
		else if ( list_item.equals(item) ) {
			list.select(i);
			return;
		}
	}
}

/**
Select all items in a List.
@param list List to select all items.
*/
public static void selectAll ( List list ) {
	if ( list == null ) {
		return;
	}
	int size = list.getItemCount();
	for ( int i = 0; i < size; i++ ) {
		list.select ( i );
	}
}

/**
Return the index of the requested selected item.
For example, if there are 5 items selected out of 20 total,
requesting index 0 will return index of the first of the 5 selected items.
This is particularly useful when determining the first or last selected item in a list.
@param list List to check.
@param selected_index Position in the selected rows list.
@return the position in the original data for the requested selected index or -1 if unable to determine.
*/
public static int selectedIndex ( List list, int selected_index ) {
	if ( list == null ) {
		return -1;
	}
	int selected[] = list.getSelectedIndexes();
	if ( selected != null ) {
		int length = selected.length;
		if ( selected_index > (length - 1) ) {
			return -1;
		}
		int pos = selected[selected_index];
		return pos;
	}
	return 0;
}

/**
Select an item in a choice, ignoring the case.
This is useful when the choice shows a valid property by the property may not always
exactly match in case when read from a file or hand-edited.
@param c Choice to select from.
@param item Choice item to select as a string.
@exception Exception if the string is not found in the Choice.
*/
public static void selectIgnoreCase ( Choice c, String item )
throws Exception {
	// Does not look like Choice.select(String) throws an exception if the item is not found so go through the list every time.
	// Get the list size.
	int size = c.getItemCount();
	for ( int i = 0; i < size; i++ ) {
		if ( c.getItem(i).equalsIgnoreCase(item) ) {
			c.select ( i );
			return;
		}
	}
	throw new Exception ( "String \"" + item + "\" not found in Choice" );
}

/**
Return the number of items selected in a list.
@param list List to check.
@return the number items selected in a list, or 0 if a null List.
*/
public static int selectedSize ( List list ) {
	if ( list == null ) {
		return 0;
	}
	int selected[] = list.getSelectedIndexes();
	if ( selected != null ) {
		int length = selected.length;
		return length;
	}
	return 0;
}

/**
Select an item in a choice, using regionMatches() for string comparisons.
This is useful when the choice shows a valid property but the property may not always
exactly match in case when read from a file or hand-edited.
@param c Choice to select from.
@param ignore_case Indicates if case should be ignored when comparing strings.
@param start_in_choice Index position in Choice item strings to start the comparison.
@param item String item to compare to Choice items.
@param start_in_item Index in item string to start the comparison.
@param length Number of characters to compare.
@exception Exception if the string is not found in the Choice.
*/
public static void selectRegionMatches ( Choice c, boolean ignore_case, int start_in_choice,
String item, int start_in_item, int length )
throws Exception {
	// Does not look like Choice.select(String) throws an exception if the
	// item is not found so go through the list every time.
	// Get the list size.
	int size = c.getItemCount();
	for ( int i = 0; i < size; i++ ) {
		if ( c.getItem(i).regionMatches(ignore_case,start_in_choice,
			item,start_in_item,length) ) {
			c.select ( i );
			return;
		}
	}
	throw new Exception ( "String \"" + item + "\" not found in Choice" );
}

/**
Select an item in a choice, comparing a specific token in the choices.
This is useful when the choice shows an extended value (e.g., "Value - Description").
@param c Choice to select from.
@param ignore_case Indicates if case should be ignored when comparing strings.
@param delimiter String delimiter used by StringUtil.breakStringList().
@param flags Flags used by StringUtil.breakStringList().
@param token Token position in the Choice item, to be compared.
@param item String item to compare to Choice item tokens.
@exception Exception if the string is not found in the Choice.
*/
public static void selectTokenMatches (	Choice c, boolean ignore_case,
					String delimiter, int flags, int token, String item )
throws Exception {
	// Does not look like Choice.select(String) throws an exception if the
	// item is not found so go through the list every time.
	// Get the list size.
	int size = c.getItemCount();
	java.util.List<String> tokens = null;
	int ntokens = 0;
	String choice_token;
	for ( int i = 0; i < size; i++ ) {
		tokens = StringUtil.breakStringList ( c.getItem(i), delimiter, flags );
		ntokens = 0;
		if ( tokens != null ) {
			ntokens = tokens.size();
		}
		if ( ntokens <= token ) {
			continue;
		}
		// Now compare.  Do not use region matches because want an exact match on the token.
		choice_token = tokens.get(token);
		if ( ignore_case ) {
			if ( choice_token.equalsIgnoreCase(item) ) {
				c.select ( i );
				return;
			}
		}
		else {	if ( choice_token.equals(item) ) {
				c.select ( i );
				return;
			}
		}
	}
	throw new Exception ( "Token " + token + " \"" + item + "\" not found in Choice" );
}

/**
This function sets the cursor for the container and all the components within the container.
@param container Container to set the cursor.
@param i Cursor mask.
*/
public static void setCursor( Container container, int i ) {
	// Call the version that takes a cursor object.
	setCursor ( container, i, null );
}

/**
This function sets the cursor for all the container and all the components within the container.
Either a cursor type (int) or Cursor object can be specified.
The Cursor object takes precedence if specified.
@param container Container in which to set the cursor.
@param i Cursor mask (e.g., Cursur.DEFAULT_CURSOR).
@param cursor Cursor object.
*/
public static void setCursor ( Container container, int i, Cursor cursor ) {
	// Make sure we have a container instance.
	if ( container == null ) {
		return;
	}

    Cursor c = null;
	if ( cursor != null ) {
		c = cursor;
	}
	else {
		// Create a cursor from the type.
       	c = new Cursor( i );
	}
    container.setCursor( c );
	// set the cursor on all the components.
	Component comp = null;
	int count = container.getComponentCount();
	for ( int j=0; j < count; j++ ) {
		comp = container.getComponent(j);
		comp.setCursor( c );
	}
}

/**
Reset the cursor to default value for the container only (not all components managed by the container).
@param container to set the cursor mask
@param textField TextField that will receive a message "Ready" when ready.
*/
public static void setReady( Container container, TextField textField ) {
	// Make sure we have a Container instance.
	if ( container == null ) {
		return;
	}
    setCursor ( container, Cursor.DEFAULT_CURSOR,
	Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) );
	if ( textField != null ) {
		textField.setText( "Ready" );
	}
}

/**
Set whether the control key is down.  This is a utility method to simplify tracking of a GUI state.
@param down Indicates whether control is pressed.
*/
public static void setControlDown ( boolean down ) {
	_control_down = down;
}

/**
Set the last directory accessed with a FileDialog.
This is used with getLastFileDialogDirectory() and is useful for saving the state of a GUI
session so that users don't have to navigate quite as much.
If multiple directories are needed, use PropList and define properties for the application
or use multiple variables within the application.
@param last_file_dialog_directory The value from FileDialog.getDirectory(),
which includes a path separator at the end.
*/
public static void setLastFileDialogDirectory ( String last_file_dialog_directory ) {
	_last_file_dialog_directory = last_file_dialog_directory;
}

/**
Set whether the shift key is down.  This is a utility method to simplify tracking of a GUI state.
@param down Indicates whether shift is pressed.
*/
public static void setShiftDown ( boolean down ) {
	_shift_down = down;
}

/**
Set the cursor to the wait cursor (hourglass) or back to the default.
The cursor is not recursively changed for every component.
@param container Container to change the cursor for (e.g., a Frame).
@param waiting true if the cursor should be changed to the wait cursor,
and false if the cursor should be set to the default.
*/
public static void setWaitCursor ( Container container, boolean waiting ) {
	if ( container == null ) {
		return;
	}
	if ( waiting ) {
		// Set the cursor to the hourglass.
		setCursor ( container, Cursor.WAIT_CURSOR, Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) );
	}
	else {
		// Set to the default.
		setCursor ( container, Cursor.DEFAULT_CURSOR, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) );
	}
	// Try to force mouse to change even without mouse movement.
	Toolkit.getDefaultToolkit().sync();
}

/**
Indicate whether the shift key is pressed.  This is used with the setShiftDown() method.
@return if Shift is pressed.
*/
public static boolean shiftDown () {
	return _shift_down;
}

/**
Return the text from a TextArea as a list of strings, each of which has had the newline removed.
This is useful for exporting the text to a file or for printing.
At some point Sun may change the delimiter returned but we can isolate to this routine.
@param a TextArea of interest.
@return A list of strings containing the text from the text area or a list with no elements if a null TextArea.
*/
public static java.util.List<String> toList ( TextArea a ) {
	if ( a == null ) {
		return new ArrayList<String>();
	}
	java.util.List<String> v = StringUtil.breakStringList ( a.getText(), "\n", 0 );
	// Just to be sure, remove any trailing carriage-return characters from the end.
	String string;
	for ( int i = 0; i < v.size(); i++ ) {
		string = v.get(i);
		v.set(i,StringUtil.removeNewline(string));
	}
	return v;
}

/**
Update the selections in a list based on the event that is being processed.
This enforces Microsoft Windows Explorer conventions:
<ol>
<li>	Click selects on item.</li>
<li>	Shift-click selects forward or backward to nearest selection.</li>
<li>	Control-click toggles one item.</li>
</ol>
Use the values of controlDown() and shiftDown() to determine which buttons have been pressed
(this is necessary because ItemEvent and its parent classes do not track whether shift/control/meta have been pressed).
@param list List to process.
@param evt ItemEvent to process.
*/
public static void updateListSelections ( List list, ItemEvent evt ) {
	updateListSelections ( list, evt, _shift_down, _control_down );
}

/**
Update the selections in a list based on the event that is being processed.
This enforces Microsoft Windows Explorer conventions:
<ol>
<li>	Click selects on item.</li>
<li>	Shift-click selects forward or backward to nearest selection.</li>
<li>	Control-click toggles one item.</li>
</ol>
@param list List to process.
@param evt ItemEvent to process.
@param shift_pressed Indicates whether shift key is pressed.
@param control_pressed Indicates whether control key is pressed.
*/
public static void updateListSelections ( List list, ItemEvent evt,
	boolean shift_pressed, boolean control_pressed ) {
	// Get the position of the selected item.
	int pos = ((Integer)evt.getItem()).intValue();	// Item triggering event.

	if ( shift_pressed ) {
		// Select the items up to the previous selected.
		// If nothing is previously selected, select down the the next selected.
		// If in a selected block, don't do anything (but have to
		// reverse the default action of the list, which will have been to deselect the item!).
		boolean is_selected = list.isIndexSelected(pos);
		if ( !is_selected ) {
			// Re-select to leave unchanged.
			list.select(pos);
		}
		else {
			// Selected a new item.  Check to see if there is anything above that is selected.
			// If so, get the last item selected above.
			int [] selected_indexes = list.getSelectedIndexes();
			int selected_size = 0;
			if ( selected_indexes != null ) {
				selected_size = selected_indexes.length;
			}
			// Going through this loop will find the maximum position before the event position.
			int starting_pos = -1;
			int i = 0;
			for ( i = 0; i < selected_size; i++ ) {
				if ( selected_indexes[i] < pos ) {
					starting_pos = selected_indexes[i];
				}
			}
			if ( starting_pos >= 0 ) {
				deselectAll ( list );
				for ( i = starting_pos; i <= pos; i++ ) {
					list.select(i);
				}
				return;
			}
			// If here, check to see whether need to select after the current event position.
			int ending_pos = -1;
			for ( i = 0; i < selected_size; i++ ) {
				if ( selected_indexes[i] > pos ) {
					ending_pos = selected_indexes[i];
					break;
				}
			}
			if ( ending_pos >= 0 ) {
				deselectAll ( list );
				for ( i = pos; i <= ending_pos; i++ ) {
					list.select(i);
				}
			}
		}
	}
	else if ( control_pressed ) {
		// Toggle the item of interest.
		// Because the list has already toggled the item, don't need to do anything here.
	}
	else {
		// Simple select.  Highlight only the selected item.
		deselectAll ( list );
		list.select(pos);
	}
}

}