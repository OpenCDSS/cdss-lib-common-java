// ERDiagram_Table - class representing a table object in an ER Diagram

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

package RTi.DMI;

import java.awt.Point;

import RTi.GR.GRArcFillType;
import RTi.GR.GRColor;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRLimits;
import RTi.GR.GRText;
import RTi.GR.GRUnits;
import RTi.Util.Message.Message;

/**
This class is the object representing a table in an ER Diagram.
It stores graphical information about the table and can draw itself on a drawing area.
*/
public class ERDiagram_Table extends DMIDataObject {

/**
Whether the bounds have been calculated for the current settings.
If false, the bounds (the height and width necessary for drawing the table)
will be recalculated at the next time the table is drawn.
*/
private boolean __boundsCalculated = false;

/**
Whether to draw the framework of the table (true), or the actual table.
Drawing the table as a framework is much faster, because only the outline lines are drawn.
*/
private boolean __drawFramework = false;

/**
Whether the table has been marked or not.
*/
private boolean __marked = false;

/**
Whether the table's text is visible or not.
*/
private boolean __textVisible = true;

/**
Whether the table's title is visible or not.
*/
private boolean __titleVisible = true;

/**
Whether the table is visible or not.  If it is not visible, it will not be drawn.
*/
private boolean __visible = true;

/**
The thickness (in pixels) of the border around the table fields.
*/
private double __borderThickness;

/**
The radius (in pixels) of the curve at the corner of the table's rectangular-ish shape.
If set to 0, the table will be stored in a true rectangle.
*/
private double __cornerRadius = 0;

/**
The space (in pixels) on either side of the line separating key fields from non-key fields.
It is calculated to be 1/2 of the average height of the text in the table, in calculateBounds().
*/
private double __dividerFieldHeight;

/**
The thickness (in pixels) of the line separating key fields from non-key fields.
*/
private double __dividerLineThickness;

/**
The distance (in pixels) that the dropshadow is off-set from the table.
If set to 0, there will be no drop shadow.
With a positive value, the drop shadow is displaced the specified number of pixels to the right of and below the table.
If the number is negative, the drop shadow will appear to the left and above the table.
*/
private double __dropShadowOffset;

/**
The spacing (in pixels) between the sides of the border and the longest text string in the table.
*/
private double __fieldNameHorizontalSpacing;

/**
The spacing (in pixels) between the top border and the first key field,
and the spacing between the last field and the bottom border.
*/
private double __fieldNameVerticalSpacing;

/**
The height of the table.  Calculated by calculateBounds().
*/
private double __height;

/**
The width of the table.  Calculated by calculateBounds().
*/
private double __width;

/**
The lower-left X position of the table.
*/
private double __x;

/**
The lower-left Y position of the table.
*/
private double __y;

/**
The color in which the area behind the table text is drawn.
*/
private GRColor __backgroundColor;

/**
The color in which the drop shadow is drawn.
*/
private GRColor __dropShadowColor;

/**
The old background color in which the table was drawn.
Used when the table is highlighted during a search, in order to restore the original table color when the table is deselected.
*/
private GRColor __oldBackgroundColor;

/**
The color in which the table borders and line separating key fields from non-key fields are drawn.
*/
private GRColor __outlineColor;

/**
The name of the table.
*/
private String __name;

/**
The table key fields.  Can be null.
*/
private String[] __keyFields;

/**
The table non-key fields.  Can be null.
*/
private String[] __nonKeyFields;

/**
Constructor.  Creates a table with no name.
*/
public ERDiagram_Table() {
	this((String)null);
}

/**
Constructor.  Creates a table with the specified name.
@param name the name of the table to create.
*/
public ERDiagram_Table(String name) {
	setName(name);

	// Initialize some default settings.
	__width = 0;
	__height = 0;
	__x = -10000;
	__y = -10000;
	//__visible = false;
	__visible = true;
	__textVisible = true;
	__titleVisible = true;
	__drawFramework = false;
	__boundsCalculated = false;
	__dropShadowOffset = 5;
	__dividerLineThickness = 1;
	__borderThickness = 1;
	__fieldNameVerticalSpacing = 3;
	__fieldNameHorizontalSpacing = 3;
	__cornerRadius = 5;
	__dividerFieldHeight = 10;
	__outlineColor = GRColor.black;
	__dropShadowColor = GRColor.gray;
	__backgroundColor = new GRColor(248, 251, 191);
	__oldBackgroundColor = new GRColor(248, 251, 191);
}

/**
Copy constructor.
@param table the ERDiagram_Table object to copy.
*/
public ERDiagram_Table(ERDiagram_Table table) {
	setName(new String(table.getName()));

	__width = table.getWidth();
	__height = table.getHeight();
	__textVisible = table.isTextVisible();
	__titleVisible = table.isTitleVisible();
	__x = table.getX();
	__y = table.getY();
	__visible = table.isVisible();
	__drawFramework = table.isFrameworkDrawn();
	__boundsCalculated = false;
	__dropShadowOffset = table.getDropShadowOffset();
	__dividerLineThickness = table.getDividerLineThickness();
	__fieldNameVerticalSpacing = table.getFieldNameVerticalSpacing();
	__fieldNameHorizontalSpacing = table.getFieldNameHorizontalSpacing();
	__cornerRadius = table.getCornerRadius();
	__dividerFieldHeight = table.getDividerFieldHeight();
	__outlineColor = table.getOutlineColor();
	__dropShadowColor = table.getDropShadowColor();
	__backgroundColor = table.getBackgroundColor();
	__oldBackgroundColor = __backgroundColor;
}

/**
Returns whether the bounds are calculated, or whether calculateBounds() needs to be called again.
@return whether the bounds are calculated.
*/
public boolean areBoundsCalculated() {
	return __boundsCalculated;
}

/**
Based on table settings, such as border thickness and the number and length of field names,
this method recalculates some internal settings necessary to draw the table properly.
If a call is made to most set*() methods,
then the setBoundsCalculated() flag will be set so that the bounds are reset next time draw() is called.
@param da the DrawingArea used to calculate some GRLimits (such as when getting text extents)
*/
public void calculateBounds(ERDiagram_DrawingArea da) {
	String routine = getClass().getSimpleName() + ".calculateBounds";
	setBoundsCalculated(true);

	double height = (__borderThickness * 2);

	double maxTextWidth = 0;

	GRLimits limits = null;

	int length = 0;

	double averageTextHeight = 0;
	int count = 0;

	GRDrawingAreaUtil.setFont(da, "Arial", 10);
	if (__keyFields != null) {
		length = __keyFields.length;
	}
	for (int i = 0; i < length; i++) {
		count++;
		limits = GRDrawingAreaUtil.getTextExtents(da, __keyFields[i], GRUnits.DEVICE);

		if (limits.getWidth() > maxTextWidth) {
			maxTextWidth = limits.getWidth();
		}

		averageTextHeight += limits.getHeight();

		if (i < (length - 1)) {
			height += getFieldNameVerticalSpacing() + limits.getHeight();
		}
		else {
			height += limits.getHeight();
		}
	}

	if (__nonKeyFields != null) {
		length = __nonKeyFields.length;
	}
	for (int i = 0; i < length; i++) {
		count++;
		limits = GRDrawingAreaUtil.getTextExtents(da, __nonKeyFields[i], GRUnits.DEVICE);

		if (limits.getWidth() > maxTextWidth) {
			maxTextWidth = limits.getWidth();
		}

		averageTextHeight += limits.getHeight();

		if (i < (length - 1)) {
			height += getFieldNameVerticalSpacing()	+ limits.getHeight();
		}
		else {
			height += limits.getHeight();
		}
	}

	averageTextHeight /= count;
	averageTextHeight /= 2;
	if (averageTextHeight < 5) {
		averageTextHeight = 5;
	}
	__dividerFieldHeight = averageTextHeight;

	height += (__dividerFieldHeight * 2) + __dividerLineThickness;

	height += getFieldNameVerticalSpacing() + (__cornerRadius * 2);

	__height = height;

	maxTextWidth += (__fieldNameHorizontalSpacing * 2) + (__borderThickness * 2);

	__width = maxTextWidth + (2 * __cornerRadius);
	if (__cornerRadius == 0) {
		__width += 5;
	}

	if ((__cornerRadius * 2) > __width || (__cornerRadius * 2) > __height) {
		__cornerRadius = 0;
	}

	//System.out.println("name: " + __name + " __x: " + __x + "  __y : "
	//	+ __y + "  __width: " + __width + "  __height: " + __height);
	if (((__x + __width) < 0) && ((__y + __height) < 0)) {
		Message.printStatus(2, routine, "Setting table to visible=false name =" + __name + " __x: " + __x + " __y : "
			+ __y + "  __width: " + __width + "  __height: " + __height);
		__visible = false;
	}
}

/**
Checks to see if the table contains the given X and Y point in its boundaries.
REVISIT: not taking into account the title of the table yet.
@param x the x value to check
@param y the y value to check
@return true if the point is contained in the table.  False if not.
*/
public boolean contains(double x, double y) {
	if ((x >= __x) && (x <= (__x + __width))) {
		if ((y >= __y) && (y <= (__y + __height))) {
			return true;
		}
	}

	return false;
}

/**
Draws the table to the specified drawing area.
@param da the drawing are to which to draw the table.
*/
public void draw(ERDiagram_DrawingArea da) {
	String routine = "draw";
	if (!areBoundsCalculated()) {
		calculateBounds(da);
	}

	double[] xs = new double[2];
	double[] ys = new double[2];

	Message.printStatus(2,routine,"Drawing table \"" + getName() + "\" at " + __x + ", " + __y );

	int length = 0;
	String text = null;
	GRLimits limits = null;
	double ypos = 0;
	double xpos = 0;

	xs[0] = __x + __cornerRadius;
	xs[1] = __x + __width - __cornerRadius;

	ys[0] = __y + __cornerRadius;
	ys[1] = __y + __height - __cornerRadius;

	if (!__drawFramework) {
		if (__dropShadowOffset > 0) {
			xs[0] += __dropShadowOffset;
			xs[1] += __dropShadowOffset;

			ys[0] -= __dropShadowOffset;
			ys[1] -= __dropShadowOffset;
			GRDrawingAreaUtil.setColor(da, __dropShadowColor);

			GRDrawingAreaUtil.fillRectangle(da, __x + __dropShadowOffset, ys[0], __width, ys[1] - ys[0]);
			GRDrawingAreaUtil.fillRectangle(da, xs[0], __y - __dropShadowOffset, xs[1] - xs[0], __height);

			GRDrawingAreaUtil.fillArc(da, xs[0], ys[0], __cornerRadius, __cornerRadius, 180, 90, GRArcFillType.CHORD);
			GRDrawingAreaUtil.fillArc(da, xs[1], ys[1], __cornerRadius, __cornerRadius, 0, 90, GRArcFillType.CHORD);
			GRDrawingAreaUtil.fillArc(da, xs[0], ys[1], __cornerRadius, __cornerRadius, 180, -90, GRArcFillType.CHORD);
			GRDrawingAreaUtil.fillArc(da, xs[1], ys[0], __cornerRadius, __cornerRadius, 0, -90, GRArcFillType.CHORD);

			xs[0] -= __dropShadowOffset;
			xs[1] -= __dropShadowOffset;

			ys[0] += __dropShadowOffset;
			ys[1] += __dropShadowOffset;
		}

		GRDrawingAreaUtil.setColor(da, __backgroundColor);
	}
	else {
		GRDrawingAreaUtil.setColor(da, GRColor.lightGray);
		GRDrawingAreaUtil.drawRectangle(da, __x, ys[0], __width, ys[1] - ys[0]);
		GRDrawingAreaUtil.drawRectangle(da, xs[0], __y, xs[1] - xs[0], __height);
		GRDrawingAreaUtil.drawArc(da, xs[0], ys[0], __cornerRadius, __cornerRadius, 180, 90);
		GRDrawingAreaUtil.drawArc(da, xs[1], ys[1], __cornerRadius, __cornerRadius, 0, 90);
		GRDrawingAreaUtil.drawArc(da, xs[0], ys[1], __cornerRadius, __cornerRadius, 180, -90);
		GRDrawingAreaUtil.drawArc(da, xs[1], ys[0], __cornerRadius, __cornerRadius, 0, -90);
	}

	if (!__drawFramework) {
		GRDrawingAreaUtil.fillRectangle(da, __x, ys[0], __width, ys[1] - ys[0]);
		GRDrawingAreaUtil.fillRectangle(da, xs[0], __y, xs[1] - xs[0], __height);
		GRDrawingAreaUtil.fillArc(da, xs[0], ys[0], __cornerRadius, __cornerRadius, 180, 90, GRArcFillType.CHORD);
		GRDrawingAreaUtil.fillArc(da, xs[1], ys[1], __cornerRadius, __cornerRadius, 0, 90, GRArcFillType.CHORD);
		GRDrawingAreaUtil.fillArc(da, xs[0], ys[1], __cornerRadius, __cornerRadius, 180, -90, GRArcFillType.CHORD);
		GRDrawingAreaUtil.fillArc(da, xs[1], ys[0], __cornerRadius, __cornerRadius, 0, -90, GRArcFillType.CHORD);

		GRDrawingAreaUtil.setColor(da, __outlineColor);

		GRDrawingAreaUtil.drawLine(da, xs[0], __y, xs[1], __y);
		GRDrawingAreaUtil.drawLine(da, xs[0], __y + __height, xs[1], __y + __height);

		GRDrawingAreaUtil.drawLine(da, __x, ys[0], __x, ys[1]);
		GRDrawingAreaUtil.drawLine(da, __x + __width, ys[0], __x + __width, ys[1]);

		GRDrawingAreaUtil.drawArc(da, xs[0], ys[0], __cornerRadius, __cornerRadius, 180, 90);
		GRDrawingAreaUtil.drawArc(da, xs[1], ys[1], __cornerRadius, __cornerRadius, 0, 90);
		GRDrawingAreaUtil.drawArc(da, xs[0], ys[1], __cornerRadius, __cornerRadius, 180, -90);
		GRDrawingAreaUtil.drawArc(da, xs[1], ys[0], __cornerRadius, __cornerRadius, 0, -90);

		ypos = __y + __height - __cornerRadius;
		if (__cornerRadius == 0) {
			xpos = __x + 5;
		}
		else {
			xpos = __x + __cornerRadius;
		}
	}

	String loc = " (" + __x + ", " + __y + ")";
	loc = "";
	GRDrawingAreaUtil.setFont(da, "Arial", 10);

	if (__titleVisible) {
		GRDrawingAreaUtil.drawText(da, __name + loc, __x, __y + __height + 3, 0, GRText.BOTTOM);
	}

	if (!__drawFramework) {
		GRDrawingAreaUtil.setFont(da, "Arial", 10);
		if (__keyFields != null) {
			length = __keyFields.length;
		}

		for (int i = 0; i < length; i++) {
			text = __keyFields[i];
			limits = GRDrawingAreaUtil.getTextExtents(da, text, GRUnits.DEVICE);

			if (__textVisible) {
				GRDrawingAreaUtil.drawText(da, text, xpos, ypos, 0, GRText.TOP);
			}

			if (i < (length - 1)) {
				ypos -= limits.getHeight() + __fieldNameVerticalSpacing;
			}
			else {
				ypos -= limits.getHeight();
			}
		}
		ypos -= __dividerFieldHeight;
		GRDrawingAreaUtil.drawLine(da, __x, ypos,  __x + __width, ypos);
		ypos -= __dividerLineThickness;
		ypos -= __dividerFieldHeight;

		if (__nonKeyFields != null) {
			length = __nonKeyFields.length;
		}

		for (int i = 0; i < length; i++) {
			text = __nonKeyFields[i];
			limits = GRDrawingAreaUtil.getTextExtents(da, text, GRUnits.DEVICE);

			if (__textVisible) {
				GRDrawingAreaUtil.drawText(da, text, xpos, ypos, 0, GRText.TOP);
			}

			if (i < (length - 1)) {
				ypos -= limits.getHeight() + __fieldNameVerticalSpacing;
			}
		}
	}
}

/**
Returns the table's background color.
@return the table's background color.
*/
public GRColor getBackgroundColor() {
	return __backgroundColor;
}

/**
Returns the thickness of the border (in pixels).
@return the thickness of the border (in pixels).
*/
public double getBorderThickness() {
	return __borderThickness;
}

/**
Returns the exact center point of the table.
@return the exact center point of the table.
*/
public Point getCenterPoint() {
	int x = (int)__x + (int)(__width / 2);
	int y = (int)__y + (int)(__height / 2);

	return new Point(x, y);
}

/**
Returns the radius (in pixels) of the corner curve.
@return the radius (in pixels) of the corner curve.
*/
public double getCornerRadius() {
	return __cornerRadius;
}

/**
Returns the divider's field height.
@return the divider's field height.
*/
public double getDividerFieldHeight() {
	return __dividerFieldHeight;
}

/**
Returns the thickness (in pixels) of the line separating the key and non-key fields.
@return the thickness (in pixels) of the line separating the key and non-key fields.
*/
public double getDividerLineThickness() {
	return __dividerLineThickness;
}

/**
Returns the drop shadow color.
@return the drop shadow color.
*/
public GRColor getDropShadowColor() {
	return __dropShadowColor;
}

/**
Returns the offset (in pixels) of the drop shadow.
@return the offset (in pixels) of the drop shadow.
*/
public double getDropShadowOffset() {
	return __dropShadowOffset;
}

/**
Returns the spacing (in pixels) between the left and right borders and the longest string in the table.
@return the spacing (in pixels) between the left and right borders and the longest string in the table.
*/
public double getFieldNameHorizontalSpacing() {
	return __fieldNameHorizontalSpacing;
}

/**
Returns the spacing (in pixels) between the top border and the first field and the bottom border and the last field.
@return the spacing (in pixels) between the top border and the first field and the bottom border and the last field.
*/
public double getFieldNameVerticalSpacing() {
	return __fieldNameVerticalSpacing;
}

/**
Get the Y position of the field with the specified name (case-insensitive) in the table.
This is used for calculating the position of connection lines between tables.
This method can return the value of Y at the Top, bottom,
or middle of the text, depending on the value of <pre>flag</pre>.
@param da the drawing are on which the table is drawn.
@param fieldName the name of the field for which to return the Y position of the text.
@param flag the flag specifying which Y value (the top, middle, or bottom) should be returned.
If GRText.TOP is specified, the top of the String will be returned as the Y location.
If GRText.BOTTOM is specified, the bottom of the String will be returned as the Y location.
If GRText.CENTER_Y is specified, the center of the String will be returned as the Y location.
If anything else is specified, the top of the String will be returned.
*/
public double getFieldY(ERDiagram_DrawingArea da, String fieldName, int flag) {
	if (!__boundsCalculated) {
		calculateBounds(da);
	}
	int length = 0;
	if (__keyFields != null) {
		length = __keyFields.length;
	}

	GRDrawingAreaUtil.setFont(da, "Arial", 10);
	double ypos = 0;
	ypos = __y + __height - __cornerRadius;
	String text = null;
	GRLimits limits = null;
	int colon;
	for (int i = 0; i < length; i++) {
		text = __keyFields[i];
		colon = text.indexOf(':');
		if (colon > -1) {
			text = text.substring(0, colon);
		}
		limits = GRDrawingAreaUtil.getTextExtents(da, text, GRUnits.DEVICE);
		if (text.equalsIgnoreCase(fieldName)) {
			if (flag == GRText.TOP) {
				return ypos;
			}
			else if (flag == GRText.BOTTOM) {
				return (ypos - limits.getHeight());
			}
			else if (flag == GRText.CENTER_Y) {
				return (double)((int)(ypos - (limits.getHeight() / 2)));
			}
			else {
				return ypos;
			}
		}

		if (i < (length - 1)) {
			ypos -= limits.getHeight() + __fieldNameVerticalSpacing;
		}
		else {
			ypos -= limits.getHeight();
		}
	}
	ypos -= __dividerFieldHeight;
	GRDrawingAreaUtil.drawLine(da, __x, ypos,  __x + __width, ypos);
	ypos -= __dividerLineThickness;
	ypos -= __dividerFieldHeight;

	if (__nonKeyFields != null) {
		length = __nonKeyFields.length;
	}

	for (int i = 0; i < length; i++) {
		text = __nonKeyFields[i];
		colon = text.indexOf(':');
		if (colon > -1) {
			text = text.substring(0, colon);
		}
		limits = GRDrawingAreaUtil.getTextExtents(da, text, GRUnits.DEVICE);

		if (text.equalsIgnoreCase(fieldName)) {
			if (flag == GRText.TOP) {
				return ypos;
			}
			else if (flag == GRText.BOTTOM) {
				return (ypos - limits.getHeight());
			}
			else if (flag == GRText.CENTER_Y) {
				return (double)((int)(ypos - (limits.getHeight() / 2)));
			}
			else {
				return ypos;
			}
		}

		if (i < (length - 1)) {
			ypos -= limits.getHeight() + __fieldNameVerticalSpacing;
		}
	}

	return -1;
}

/**
Returns the height (in pixels) of the curved rectangle containing the table data.
@return the height (in pixels) of the curved rectangle containing the table data.
*/
public double getHeight() {
	return __height;
}

/**
Returns the name of the key field at the given position in the key fields array.
@return the name of the key field at the given position in the key fields array.
*/
public String getKeyField(int field) {
	if (__keyFields == null) {
		return null;
	}
	return __keyFields[field];
}

/**
Returns the number of key fields in the table.
@return the number of key fields in the table.
*/
public int getKeyFieldCount() {
	if (__keyFields == null) {
		return 0;
	}
	return __keyFields.length;
}

/**
Returns the limits of the table.
@return the limits of the table.
*/
public GRLimits getLimits() {
	return new GRLimits(__x, __y, __x + __width, __y + __height);
}

/**
Returns the name of the table.
@return the name of the table.
*/
public String getName() {
	return __name;
}

/**
Returns the name of the non-key field at the given position in the non-key fields array.
@return the name of the non-key field at the given position in the non-key fields array.
*/
public String getNonKeyField(int field) {
	if (__nonKeyFields == null) {
		return null;
	}
	return __nonKeyFields[field];
}

/**
Returns the number of key fields in the table.
@return the number of key fields in the table.
*/
public int getNonKeyFieldCount() {
	if (__nonKeyFields == null) {
		return 0;
	}
	return __nonKeyFields.length;
}

/**
Returns the table's outline color.
@return the table's outline color.
*/
public GRColor getOutlineColor() {
	return __outlineColor;
}

/**
Returns the width (in pixels) of the curved rectangle containing the table data.
@return the width (in pixels) of the curved rectangle containing the table data.
*/
public double getWidth() {
	return __width;
}

/**
Returns the X position of the table.
@return the X position of the table.
*/
public double getX() {
	return __x;
}

/**
Returns the Y position of the table.
@return the Y position of the table.
*/
public double getY() {
	return __y;
}

/**
Returns whether the table's framework is drawn or not.
@return whether the table's framework is drawn or not.
*/
public boolean isFrameworkDrawn() {
	return __drawFramework;
}

/**
Returns whether the table has been marked or not.
@return whether the table has been marked or not.
*/
public boolean isMarked() {
	return __marked;
}

/**
Returns whether the table's text is visible (true) or not (false).
@return whether the table's text is visible or not.
*/
public boolean isTextVisible() {
	return __textVisible;
}

/**
Returns whether the table's title is visible (true) or not (false).
@return whether the table's title is visible or not.
*/
public boolean isTitleVisible() {
	return __titleVisible;
}

/**
Returns whether the table is visible or not.
@return whether the table is visible or not.
*/
public boolean isVisible() {
	return __visible;
}

/**
Checks to see whether any portion of the table can found within the given GRLimits.  If so, returns true.
@param limits the limits for which to check an intersection with the table.
@return true if any portion of the table can be found within the specified limits.
*/
public boolean isWithinLimits(GRLimits limits, double scale) {
	return limits.contains(__x * scale, __y * scale, (__x + __width) * scale, (__y + __height) * scale, false);
}

/**
Marks the table selected or not.
When the table is selected, it is currently drawn in a hard-coded color (red).
If the table is marked not selected, its old background color will be restored.
@param selected whether the table is selected or not.
*/
public void markSelected(boolean selected) {
	if (selected) {
		__oldBackgroundColor = __backgroundColor;
		__backgroundColor = GRColor.red;
		__marked = true;
	}
	else {
		__backgroundColor = __oldBackgroundColor;
		__marked = false;
	}
}

/**
Sets the thickness of the border around the table (in pixels).
If the value of the border thickness is changed, calculateBounds() will be called before the table is next drawn.
@param thickness the thickness to set.
*/
public void setBorderThickness(double thickness) {
	if (thickness != __borderThickness) {
		__boundsCalculated = false;
		__borderThickness = thickness;
	}
}

/**
Sets whether the bounds have been calculated, or whether they need to be calculated again.
@param boundsCalculated true if the bounds have been calculated,
or false if they should be recalculated before the table is drawn next time.
*/
private void setBoundsCalculated(boolean boundsCalculated) {
	__boundsCalculated = boundsCalculated;
}

/**
Sets the radius of the curve at the corner of the table rectangle.
If set to 0, the table will be stored in a true rectangle.
If the radius size is changed, calculateBounds() will be called before the table is next drawn.
@param radius the radius to set.
*/
public void setCornerRadius(double radius) {
	if (__cornerRadius != radius) {
		__boundsCalculated = false;
		__cornerRadius = radius;
	}
}

/**
Sets the thickness (in pixels) of the line separating the key fields from the non-key fields.
If the thickness of the divider lines is changed,
calculateBounds() will be called before the table is next drawn.
@param thickness the thickness to be set.
*/
public void setDividerLineThickness(double thickness) {
	if (__dividerLineThickness != thickness) {
		__boundsCalculated = false;
		__dividerLineThickness = thickness;
	}
}

/**
Sets whether the table's framework (outline lines) should be drawn or not.
When the framework is drawn, no field information is visible, but it is useful for debugging.
@param framework whether the framework should be drawn or not.
*/
public void setDrawFramework(boolean framework) {
	__drawFramework = framework;
}

/**
Sets the offset of the table dropshadow (in pixels).
If set to 0, there will be no drop shadow.
With a positive value, the drop shadow is displaced the specified number of pixels to the right of and below the table.
If the number is negative, the drop shadow will appear to the left and above the table.
If the drop shadow offset is changed, calculateBounds() will be called before the table is next drawn.
@param offset the offset to set.
*/
public void setDropShadowOffset(double offset) {
	if (__dropShadowOffset != offset) {
		__boundsCalculated = false;
		__dropShadowOffset = offset;
	}
}

/**
Sets the spacing (in pixels) between the right and left borders and the longest text string in the table.
If the spacing is changed, calculateBounds() will be called before the table is next drawn.
@param spacing the spacing to set.
*/
public void setFieldNameHorizontalSpacing(double spacing) {
	if (__fieldNameHorizontalSpacing != spacing) {
		__boundsCalculated = false;
		__fieldNameHorizontalSpacing = spacing;
	}
}

/**
Sets the spacing (in pixels) between the top border and the first text field and the bottom border and the last non-key field.
If the spacing is changed, calculateBounds() will be called before the table is next drawn.
@param spacing the spacing to set.
*/
public void setFieldNameVerticalSpacing(double spacing) {
	if (__fieldNameVerticalSpacing != spacing) {
		__boundsCalculated = false;
		__fieldNameVerticalSpacing = spacing;
	}
}

/**
Sets the key fields.  If keyFields is null, no keyfields will be put in the table.
calculateBounds() will be called before the table is next drawn.
@param keyFields the keyFields to set.  Can be null.
*/
public void setKeyFields(String[] keyFields) {
	__boundsCalculated = false;
	__keyFields = keyFields;
}

/**
Sets the name of the table.
If the name of the table changed, calculateBounds() will be called before the table is next drawn.
@param name the name of the table.
*/
public void setName(String name) {
	if (name == null) {
		__boundsCalculated = false;
		__name = "";
		return;
	}
	if (!name.equals(__name)) {
		__boundsCalculated = false;
		__name = new String(name);
	}
}

/**
Sets the non-key fields.  If nonKeyFields is null, no non-key fields will be put in the table.
@param nonKeyFields the non-key fields to set.  Can be null.
*/
public void setNonKeyFields(String[] nonKeyFields) {
	__boundsCalculated = false;
	__nonKeyFields = nonKeyFields;
}

/**
Sets whether the table's text will be visible or not.
@param visible whether the table's text will be visible (true) or not (false).
*/
public void setTextVisible(boolean visible) {
	__textVisible = visible;
}

/**
Sets whether the table's title will be visible or not.
@param visible whether the table's title will be visible (true) or not (false).
*/
public void setTitleVisible(boolean visible) {
	__titleVisible = visible;
}

/**
Sets whether the table is visible or not.
If the table's visibility changes, calculateBounds() will be called before the table is next drawn.
@param visible whether the table should be visible or not.
*/
public void setVisible(boolean visible) {
	if (__visible != visible) {
		__boundsCalculated = false;
		__visible = visible;
	}
}

/**
Sets the X position of the table.
If the value of X changes, calculateBounds() will be called before the table is next drawn.
@param x the X position of the table.
*/
public void setX(double x) {
	if (__x != x) {
		__boundsCalculated = false;
		__x = x;
	}
}

/**
Sets the Y position of the table.
If the value of Y changes, calculateBounds() will be called before the table is next drawn.
@param y the Y position of the table.
*/
public void setY(double y) {
	if (__y != y) {
		__boundsCalculated = false;
		__y = y;
	}
}

/**
Returns a String representing the table.
@return a String representing the table.
*/
public String toString() {
	String str =
		  "Table: '" + __name + "'\n"
		+ "  Position: " + __x + ", " + __y + "\n"
		+ "  Width:    " + __width + "\n"
		+ "  Height:   " + __height + "\n"
		+ "  Visible: " + __visible + "\n"
		+ "  Horizontal Field Spacing: " + __fieldNameHorizontalSpacing + "\n"
		+ "  Vertical Field Spacing:   " + __fieldNameVerticalSpacing + "\n"
		+ "  Divider Line Thickness:   " + __dividerLineThickness + "\n"
		+ "  Divider Field Height:     " + __dividerFieldHeight + "\n"
		+ "  Border Thickness:         " + __borderThickness + "\n"
		+ "  Corner Radius:            " + __cornerRadius + "\n"
		+ "  Drop Shadow Offset:       " + __dropShadowOffset + "\n"
		+ "  Outline Color:            " + __outlineColor + "\n"
		+ "  Background Color:         " + __backgroundColor + "\n"
		+ "  Drop Shadow Color:        " + __dropShadowColor + "\n"
		+ "  Key Fields: \n";
	if (__keyFields == null) {
		str += "    (None)\n";
	}
	else {
		for (int i = 0; i < __keyFields.length; i++) {
			str += "    [" + i + "]: '" + __keyFields[i] + "'\n";
		}
	}
	str += "  Non-Key Fields:\n";
	if (__nonKeyFields == null) {
		str += "    (None)\n";
	}
	else {
		for (int i = 0; i < __nonKeyFields.length; i++) {
			str += "    [" + i + "]: '" + __nonKeyFields[i] + "'\n";
		}
	}
	str += "\n";
	return str;
}

}