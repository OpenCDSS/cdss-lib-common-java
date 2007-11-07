// ----------------------------------------------------------------------------
// GRText - store text attributes
// ----------------------------------------------------------------------------
// History:
//
// 2001-10-11	Steven A. Malers, RTi	Add getLabelPositions() and toString().
// 2001-10-15	SAM, RTi		Change to getTextPositions() and add
//					parseTextPosition().
// 2002-02-07	SAM, RTi		Change text positions "LeftCenter" to
//					"Left" and "RightCenter" to "Right" to
//					be consistent with C++.
// ----------------------------------------------------------------------------

package RTi.GR;

import RTi.Util.Message.Message;

/**
Class to store text attributes.  Note that internally text positions are
specified using justification.  For example, LEFT means that the text is
left-justified but will be to the right of a symbol.  For user interfaces, the
positions are identified using relative positions (not justification).
Therefore, for the above example, the position would be specified as
Right or one of the other "Right" positions.
*/
public class GRText
{

/**
Flags that indicate position of text.  These flags are used as a mask with
text drawing methods.
The left edge of the text will be at the point used in drawing routines.
*/
public static final int LEFT = 0x1;
/**
The horizontal center of the text will be at the point used in drawing routines.
*/
public static final int CENTER_X = 0x2;
/**
The right edge of the text will be at the point used in drawing routines.
*/
public static final int RIGHT = 0x4;
/**
The bottom edge of the text will be at the point used in drawing routines.
*/
public static final int BOTTOM = 0x8;
/**
The top edge of the text will be at the point used in drawing routines.
*/
public static final int TOP = 0x10;
/**
The vertical middle center of the text will be at the point used in drawing
routines.
*/
public static final int CENTER_Y = 0x20;

/**
For axis labels - sheft ends so that they are not centered.
*/
public static final int SHIFT_ENDS = 0x40;
/**
Labels are coming in with the bottom or right one last.
*/
public static final int REVERSE_LABELS = 0x80;
/**
Put in for now - needs to be completed.
*/
public static final int SHIFT_ENDS_INVERTED = 0x100;

/**
Return available text positions.  The text positions are suitable for
positioning relative to a point, as follows:
<pre>
UpperLeft |  Above | UpperRight
--------------------------------
     Left | Center | Right
--------------------------------
LowerLeft | Below  | LowerRight
</pre>
@return a String array containing possible text positions (e.g., "UpperRight").
These strings can be used for properties for maps, time series plots, etc.
*/
public static String[] getTextPositions()
{	String [] positions = new String[9];
	positions[0] = "AboveCenter";
	positions[1] = "BelowCenter";
	positions[2] = "Center";
	positions[3] = "Left";
	positions[4] = "LowerLeft";
	positions[5] = "LowerRight";
	positions[6] = "Right";
	positions[7] = "UpperLeft";
	positions[8] = "UpperRight";
	return positions;
}

/**
Parse a text position and return the integer equivalent.
@param position Position for text, corresponding to a value returned from
getTextPositions (e.g., "UpperRight").
@return the integer equivalent of a text position.
@exception Exception if the position is not recognized.  In this case the
calling code should probably use a reasonable default like LEFT|CENTER_Y.
*/
public static int parseTextPosition ( String position )
throws Exception
{	if ( position.equalsIgnoreCase("AboveCenter") ) {
		return BOTTOM|CENTER_X;
	}
	else if ( position.equalsIgnoreCase("BelowCenter") ) {
		return TOP|CENTER_X;
	}
	else if ( position.equalsIgnoreCase("Center") ) {
		return CENTER_Y|CENTER_X;
	}
	else if ( position.equalsIgnoreCase("Left") ) {
		return RIGHT|CENTER_Y;
	}
	else if ( position.equalsIgnoreCase("LeftCenter") ) {
		Message.printWarning ( 2, "",
		"Label position LeftCenter is obsolete - change to Left" );
		return RIGHT|CENTER_Y;
	}
	else if ( position.equalsIgnoreCase("LowerLeft") ) {
		return RIGHT|TOP;
	}
	else if ( position.equalsIgnoreCase("LowerRight") ) {
		return LEFT|TOP;
	}
	else if ( position.equalsIgnoreCase("Right") ) {
		return LEFT|CENTER_Y;
	}
	else if ( position.equalsIgnoreCase("RightCenter") ) {
		Message.printWarning ( 2, "",
		"Label position RightCenter is obsolete - change to Right" );
		return LEFT|CENTER_Y;
	}
	else if ( position.equalsIgnoreCase("UpperLeft") ) {
		return RIGHT|BOTTOM;
	}
	else if ( position.equalsIgnoreCase("UpperRight") ) {
		return LEFT|BOTTOM;
	}
	else {	throw new Exception ( "Unknown text position \"" + position +
		"\"");
	}
}

/**
Return String corresponding to position information.
@param position Combination of position bit mask values.
@return String corresponding to position information.
*/
public static String toString ( int position )
{	if ( ((position&CENTER_X) != 0) && ((position&BOTTOM) != 0) ) {
		return "AboveCenter";
	}
	else if ( ((position&CENTER_X) != 0) && ((position&TOP) != 0) ) {
		return "BelowCenter";
	}
	else if ( ((position&CENTER_X) != 0) && ((position&CENTER_Y) != 0) ) {
		return "Center";
	}
	else if ( ((position&RIGHT) != 0) && ((position&CENTER_Y) != 0) ) {
		return "Left";
	}
	else if ( ((position&TOP) != 0) && ((position&RIGHT) != 0) ) {
		return "LowerLeft";
	}
	else if ( ((position&TOP) != 0) && ((position&LEFT) != 0) ) {
		return "LowerRight";
	}
	else if ( ((position&LEFT) != 0) && ((position&CENTER_Y) != 0) ) {
		return "Right";
	}
	else if ( ((position&BOTTOM) != 0) && ((position&LEFT) != 0) ) {
		return "UpperRight";
	}
	else if ( ((position&BOTTOM) != 0) && ((position&RIGHT) != 0) ) {
		return "UpperLeft";
	}
	else return "Center";
}

} // End GRText class
