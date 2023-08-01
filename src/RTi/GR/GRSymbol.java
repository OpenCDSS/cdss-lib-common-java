// GRSymbol - store symbol definition information for points, lines, polygons

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

package RTi.GR;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class stores information necessary to draw symbols.
Symbols may be used with points, lines (in which case a line style, width, and color are used),
and polygons (in which case color, outline color, and fill pattern are used).
Consequently, symbols can be used for graphs, maps, and other visualizations.
Symbols can currently be varied in color based on a classification; however,
varying the size (e.g., for point symbols) is NOT enabled.
If classification is used, then additional data like a color table are used.
Typically the color table is constructed using GRColorTable.createColorTable().
If multiple symbols are used for a shape, an array of GRSymbol can be used (e.g., see GRLegend).
Additional features may be added later to help with optimization of multi-symbol layout.
*/
public class GRSymbol
implements Cloneable
{

/**
Indicates whether only selected shapes should be labeled.  This is useful to clarify displays.
*/
private boolean _label_selected_only = false;

/**
Indicates whether the symbol is a primary symbol (simple).
*/
private boolean __is_primary = false;

/**
Array of double precision data corresponding to colors in the color table.
*/
private double _double_data[] = null;

/**
Font height in points for symbol labels.
*/
private double _label_font_height = 10.0;

/**
Symbol size in the X direction.
*/
private double __size_x = 0.0;

/**
Symbol size in the Y direction.
*/
private double __size_y = 0.0;

/**
Foreground color.
*/
private GRColor _color = null;

/**
Secondary foreground color (e.g., for negative bars).
*/
private GRColor _color2 = null;

/**
Outline color.
*/
private GRColor _outline_color = null;

/**
A color table is used when data fields are classified for output.
*/
private GRColorTable _color_table = null;

/**
The type of classification for this symbol.
*/
private GRClassificationType _classification_type = GRClassificationType.SINGLE;

/**
Array of integer data corresponding to colors in the color table.
*/
private int _int_data[] = null;

/**
Label position (left-justified against the symbol, centered in Y direction).
*/
private int _label_position = GRText.LEFT|GRText.CENTER_Y;

/**
 * Shape type for points.
 */
private GRSymbolShapeType shapeType = GRSymbolShapeType.NONE;

/**
Symbol style.
*/
// TODO smalers 2023-07-27 replaced with shapeType.
//private int _style = SYM_NONE;

/**
Transparency level.  Default is totally opaque.
The transparency is the reverse of the alpha.  0 transparency is totally opaque, alpha 255.
*/
private int __transparency = 0;

/**
The type of the symbol.
*/
private GRSymbolType _type = GRSymbolType.NONE;

/**
The classification field to be used (used by higher-level code).
*/
private String _classification_field = "";

/**
Array of string data corresponding to the color table.
*/
private String _string_data[] = null;

/**
Name of field(s) to use for labeling.
*/
private String _label_field = null;

/**
Format to use for labeling.
*/
private String _label_format = null;

/**
Name of font for labels.
*/
private String _label_font_name = "Helvetica";

/**
Construct.  Colors are initialized to null and the symbol values to TYPE_NONE, SYM_NONE.
*/
public GRSymbol () {
	initialize();
}

/**
Construct using the given parameters.
@param type Symbol type.
@param shapeType shape type for point symbols.
@param color Foreground color.
@param outline_color Outline color for polygons.
@param size Symbol size.  Currently units are not handled.
Treat is a storage area for the size that will be specified to GR.drawSymbol().
The x and y direction sizes are set to the single value.
*/
public GRSymbol ( GRSymbolType type, GRSymbolShapeType shapeType, GRColor color, GRColor outline_color, double size ) {
	this ( type, shapeType, color, outline_color, size, size );
}

/**
Construct using the given parameters.
@param type Symbol type.
@param shapeType Indicates the symbol shapeType for POINT type.
@param color Foreground color.
@param outline_color Outline color for polygons.
@param size_x Symbol size in the X direction.  Currently units are not handled.
Treat is a storage area for the size that will be specified to GR.drawSymbol().
@param size_y Symbol size in the Y direction.
*/
public GRSymbol ( GRSymbolType type, GRSymbolShapeType shapeType, GRColor color, GRColor outline_color, double size_x, double size_y ) {
	initialize();
	this._type = type;
	this._color = color;
	this._color2 = _color;
	this._outline_color = outline_color;
	this.__size_x = size_x;
	this.__size_y = size_y;
	this.shapeType = shapeType;
}

/**
Clear the data arrays used to look up a color in the color table.
This method should be called when the lookup values are reset.
*/
private void clearData () {
	this._double_data = null;
	this._int_data = null;
	this._string_data = null;
}

/**
Clones the object.
@return a Clone of the Object.
*/
public Object clone() {
	GRSymbol s = null;
	try {
		s = (GRSymbol)super.clone();
	}
	catch (Exception e) {
		return null;
	}

	if (_double_data != null) {
		s._double_data = (double[])_double_data.clone();
	}

	if (_color != null) {
		s._color = (GRColor)_color.clone();
	}

	if (_color2 != null) {
		s._color2 = (GRColor)_color2.clone();
	}

	if (_outline_color != null) {
		s._outline_color = (GRColor)_outline_color.clone();
	}

	if (_color_table != null) {
		s._color_table = (GRColorTable)_color_table.clone();
	}

	if (_int_data != null) {
		s._int_data = (int[])_int_data.clone();
	}

	if (_string_data != null) {
		s._string_data = (String[])_string_data.clone();
	}

	return s;
}

/**
Return the classification color.
If the classification type is single, the main color is returned.
Otherwise the classification value is used to look up the color in the color table.
This is useful when displaying a legend.
@param classification Symbol classification color position, when used with a color table (ignored for single symbol).
@return the symbol classification color (zero index).
*/
public GRColor getClassificationColor ( int classification ) {
	if ( this._classification_type == GRClassificationType.SINGLE ) {
		return this._color;
	}
	else {
		return this._color_table.get ( classification );
	}
}

/**
Return the classification second color.
This is being phased in for use with the CLASSIFICATION_SCALED_SYMBOL type.
@param classification Symbol classification color position, when used with a color table (ignored for single symbol).
@return the second symbol classification color (zero index).
*/
public GRColor getClassificationColor2 ( int classification ) {
	if ( this._classification_type == GRClassificationType.SINGLE ) {
		return this._color2;
	}
	else {
		return this._color_table.get ( classification );
	}
}

/**
Return the classification field.
This is just a place-holder to store the classification field name (e.g., for display on a legend).
@return the symbol classification field.
*/
public String getClassificationField () {
	return this._classification_field;
}

/**
Return the classification label string.
For single classification this is an empty string.  For class breaks, the label has "&lt; x" for the first class,
"&gt; x AND &lt;= y" for the middle classes and "&gt; x" for the last class.
For unique values the label is just the unique value.
@param classification Classification to get a label for.
*/
public String getClassificationLabel ( int classification ) {
	if ( this._classification_type == GRClassificationType.CLASS_BREAKS ) {
		if ( this._double_data != null ) {
			if ( classification == 0 ) {
				return "< " + StringUtil.formatString(this._double_data[0],"%.3f");
			}
			else if ( classification == (this._color_table.size() - 1)) {
				return ">= " + StringUtil.formatString(this._double_data[classification],"%.3f");
			}
			else return ">= " + StringUtil.formatString(this._double_data[classification - 1],"%.3f") +
				" AND < " + StringUtil.formatString(this._double_data[classification],"%.3f");
		}
		else if ( this._string_data != null ) {
			return "" + classification;
		}
		else if ( this._int_data != null ) {
			if ( classification == 0 ) {
				return "< " + this._int_data[0];
			}
			else if ( classification == (this._color_table.size() - 1)) {
				return ">= " + this._int_data[classification];
			}
			else return ">= " + this._int_data[classification - 1] +
				" AND < " + this._int_data[classification];
		}
	}
	else if ( this._classification_type == GRClassificationType.UNIQUE_VALUES ) {
		// Need to work on this.
		return "" + classification;
	}
	return "";
}

/**
Return the classification type.
@return the symbol classification type.
*/
public GRClassificationType getClassificationType() {
	return this._classification_type;
}

/**
Return the color used for the symbol if a single color is used.
Note that the color will be null if no color has been selected.
@return the color for the symbol (equivalent to the foreground color).
*/
public GRColor getColor() {
	return this._color;
}

/**
Return the second color used for the symbol if a single color scheme is used
(in this case, two colors as part of the symbol are supported).
Note that the color will be null if no color has been selected.
@return the second color for the symbol (equivalent to the second foreground color).
*/
public GRColor getColor2 () {
	return this._color2;
}

/**
Return the color used for the symbol if a classification is used.
The color will be null if no color has been selected or no color table has been set for the symbol.
The internal data arrays for double, integer, and String will be checked.
Only one of these arrays is allowed to have values at any time.
@param value Data value for color.
@return the color for the symbol (equivalent to the foreground color).
*/
public GRColor getColor ( Object value ) {
	if ( this._double_data != null ) {
		return getColor ( ((Double)value).doubleValue() );
	}
	//else if ( this._string_data != null ) {
		//return getColor ( (String)value );
	//}
	//else if ( this._int_data != null ) {
		//return getColor ( ((Integer)value).intValue() );
	//}
	return null;
}

/**
Return the color used for the symbol.
If a classification or scaled symbol is used the value is used to determine the symbol color.
Note that the color will be null if no color has been selected or no color table has been set for the symbol.
@param value Data value to find color.
@return the color for the symbol (equivalent to the foreground color).
*/
public GRColor getColor ( double value ) {
	if ( this._classification_type == GRClassificationType.SINGLE ) {
		//Message.printStatus ( 1, "", "Returning single color" );
		return this._color;
	}
	else if ( this._classification_type == GRClassificationType.SCALED_SYMBOL ||
		this._classification_type == GRClassificationType.SCALED_TEACUP_SYMBOL) {
		//Message.printStatus ( 1, "", "Returning single color" );
		if ( this.shapeType == GRSymbolShapeType.VERTICAL_BAR_SIGNED ) {
			if ( value >= 0 ) {
				return this._color;
			}
			else {
				return this._color2;
			}
		}
		else {
			return this._color;
		}
	}
	else if ( this._classification_type == GRClassificationType.CLASS_BREAKS ) {
		// Need to check data value against ranges.
		if ( value < this._double_data[0] ) {
			//Message.printStatus(1, "", "Returning first color for " + value );
			return this._color_table.get(0);
		}
		else if ( value >= this._double_data[this._double_data.length - 1] ) {
			//Message.printStatus(1, "", "Returning last color for "+ value  );
			return this._color_table.get(this._double_data.length - 1);
		}
		else {
			int iend = this._double_data.length - 1;
			for ( int i = 1; i < iend; i++ ) {
				if (value < this._double_data[i]) {
					//Message.printStatus(1, "", "Returning color " + i + " for " + value
					//	+ "  (" + _double_data[i] + ")");
					return this._color_table.get(i);
				}
			}
			return null;
		}
	}
	else {
		// Unique value so just find the value.
		for ( int i = 0; i < this._double_data.length; i++ ) {
			if ( this._double_data[i] == value ) {
				return this._color_table.get(i);
			}
		}
		return null;
	}
}

/**
Return the color table color position for the symbol if a classification is used.
This method is useful if an external set of colors is set up and only the position is used.
Note that the position will be -1 if no color has been selected or no color table has been set for the symbol.
@param value Data value to find color.
@return the color table position for the color for the symbol (equivalent to the foreground color).
*/
public int getColorNumber ( double value ) {
	if ( this._classification_type == GRClassificationType.SINGLE ) {
		//Message.printStatus ( 1, "", "Returning single color" );
		return -1;
	}
	else if ( this._classification_type == GRClassificationType.CLASS_BREAKS ) {
		// Need to check data value against ranges...
		if ( value < this._double_data[0] ) {
			//Message.printStatus ( 1, "", "Returning first color for " + value );
			return 0;
		}
		else if ( value >= this._double_data[_double_data.length - 1] ) {
			//Message.printStatus ( 1, "", "Returning last color for "+ value  );
			return this._double_data.length - 1;
		}
		else {
			int iend = this._double_data.length - 1;
			for ( int i = 1; i < iend; i++ ) {
				if ( value < this._double_data[i + 1] ) {
					//Message.printStatus ( 1, "", "Returning color " + i+" for "+value);
					return i;
				}
			}
			return -1;
		}
	}
	else {
		// Unique value so just find the value.
		for ( int i = 0; i < this._double_data.length; i++ ) {
			if ( this._double_data[i] == value ) {
				return i;
			}
		}
		return -1;
	}
}

/**
Return the GRColorTable, or null if one is not used.
@return the GRColorTable.
*/
public GRColorTable getColorTable() {
	return this._color_table;
}

/**
Return the label field.
@return the label field.
*/
public String getLabelField () {
	return this._label_field;
}

/**
Return the label font height, points.
@return the label font height, points.
*/
public double getLabelFontHeight () {
	return this._label_font_height;
}

/**
Return the label font name.
@return the label font name.
*/
public String getLabelFontName () {
	return this._label_font_name;
}

/**
Return the label format.
@return the label format.
*/
public String getLabelFormat () {
	return this._label_format;
}

/**
Return the label position.
@return the label position.
*/
public int getLabelPosition () {
	return this._label_position;
}

/**
Return the number of classifications, which will be the number of colors in the color table.
If a single classification (or scaled symbol) is used, then 1 is returned.
@return the number of classifications.
*/
public int getNumberOfClassifications () {
	Message.printStatus(2, "", "Classification type=" + this._classification_type);
	if ( (this._classification_type == GRClassificationType.SINGLE) ||
		(this._classification_type == GRClassificationType.SCALED_SYMBOL) ||
		this._classification_type == GRClassificationType.SCALED_TEACUP_SYMBOL) {
		//Message.printStatus ( 1, "", "Returning single color" );
		return 1;
	}
	else {
		// Use the color table size so the data type checks don't have to be done.
		return this._color_table.size();
	}
}

/**
Return the outline color.  The outline color is used for all classifications.
@return the outline color.  Note that the color will be null if no color has been selected.
*/
public GRColor getOutlineColor() {
	return this._outline_color;
}

/**
 * Return the list of point shape type names, suitable for listing in a user interface.
 * @param listContents enumeration value indicating the list to return
 * @return a list of shape types corresponding to the requested list contents
 * (if null, APP_CHOICES is used)
 */
public static String[] getShapeTypeNames ( GRSymbolShapeTypeListContents listContents ) {
	GRSymbolShapeType [] shapeTypes = getShapeTypes ( listContents );
	String [] shapeTypeNames = new String[shapeTypes.length];
	int i = -1;
	for ( GRSymbolShapeType shapeType : shapeTypes ) {
		++i;
		shapeTypeNames[i] = shapeType.toString();
	}
	return shapeTypeNames;
}

/**
 * Return the list of point shape type, suitable for listing in a user interface.
 * @param listContents enumeration value indicating the list to return:
 * <ul>
 * <li> <code>APP_CHOICES</code> - choices suitable for an application</li>
 * <li> <code>GEOLAYER_DEFAULT</code> - used for default GeoLayer symbols (map)</li>
 * <ul>
 * @return a list of shape types corresponding to the requested list contents
 * (if null, APP_CHOICES is used)
 */
public static GRSymbolShapeType [] getShapeTypes ( GRSymbolShapeTypeListContents listContents ) {
	if ( listContents == null ) {
		listContents = GRSymbolShapeTypeListContents.APP_CHOICES;
	}
	if ( listContents == GRSymbolShapeTypeListContents.APP_CHOICES ) {
		// Symbols used in UIs such as TSTool.
		GRSymbolShapeType [] shapeTypes = {
			GRSymbolShapeType.NONE,
			GRSymbolShapeType.ARROW_DOWN,
			GRSymbolShapeType.ARROW_DOWN_LEFT_FILLED,
			GRSymbolShapeType.ARROW_DOWN_RIGHT_FILLED,
			GRSymbolShapeType.ARROW_LEFT,
			GRSymbolShapeType.ARROW_RIGHT,
			GRSymbolShapeType.ARROW_UP,
			GRSymbolShapeType.ARROW_UP_LEFT_FILLED,
			GRSymbolShapeType.ARROW_UP_RIGHT_FILLED,
			GRSymbolShapeType.ASTERISK,
			GRSymbolShapeType.CIRCLE,
			GRSymbolShapeType.CIRCLE_FILLED,
			GRSymbolShapeType.CIRCLE_PLUS,
			GRSymbolShapeType.DIAMOND,
			GRSymbolShapeType.DIAMOND_FILLED,
			GRSymbolShapeType.ELLIPSE_HORIZONTAL,
			GRSymbolShapeType.ELLIPSE_HORIZONTAL_FILLED,
			GRSymbolShapeType.ELLIPSE_VERTICAL,
			GRSymbolShapeType.ELLIPSE_VERTICAL_FILLED,
			GRSymbolShapeType.INSTREAM_FLOW,
			GRSymbolShapeType.PLUS,
			GRSymbolShapeType.PLUS_SQUARE,
			GRSymbolShapeType.RECTANGLE_HORIZONTAL,
			GRSymbolShapeType.RECTANGLE_HORIZONTAL_FILLED,
			GRSymbolShapeType.RECTANGLE_VERTICAL,
			GRSymbolShapeType.RECTANGLE_VERTICAL_FILLED,
			GRSymbolShapeType.SQUARE,
			GRSymbolShapeType.SQUARE_FILLED,
			GRSymbolShapeType.TEACUP,
			GRSymbolShapeType.TRIANGLE_DOWN,
			GRSymbolShapeType.TRIANGLE_DOWN_FILLED,
			GRSymbolShapeType.TRIANGLE_LEFT,
			GRSymbolShapeType.TRIANGLE_LEFT_FILLED,
			GRSymbolShapeType.TRIANGLE_RIGHT,
			GRSymbolShapeType.TRIANGLE_RIGHT_FILLED,
			GRSymbolShapeType.TRIANGLE_UP,
			GRSymbolShapeType.TRIANGLE_UP_FILLED,
			//GRSymbolShapeType.VERTICAL_BAR_SIGNED,
			GRSymbolShapeType.X,
			GRSymbolShapeType.X_CAP,
			GRSymbolShapeType.X_DIAMOND,
			GRSymbolShapeType.X_EDGE,
			GRSymbolShapeType.X_SQUARE,
			GRSymbolShapeType.TRIANGLE_UP_TOP_LINE,
			GRSymbolShapeType.TRIANGLE_DOWN_BOTTOM_LINE,
			GRSymbolShapeType.TRIANGLE_UP_TOP_LINE,
			GRSymbolShapeType.TRIANGLE_DOWN_BOTTOM_LINE,
			GRSymbolShapeType.PUSHPIN_VERTICAL
		};
		return shapeTypes;
	}
	else if ( listContents == GRSymbolShapeTypeListContents.GEOLAYER_DEFAULT ) {
		// Symbols used in time series products:
		// - the order is consistent with historical GeoLayer default symbol
		// - may change in the future to be more like TSTool graphs since GeoLayer is not often used
		GRSymbolShapeType [] shapeTypes = {
			// Hollow.
			GRSymbolShapeType.CIRCLE,
			GRSymbolShapeType.SQUARE,
			GRSymbolShapeType.TRIANGLE_UP,
			GRSymbolShapeType.TRIANGLE_DOWN,
			GRSymbolShapeType.TRIANGLE_LEFT,
			GRSymbolShapeType.TRIANGLE_RIGHT,
			GRSymbolShapeType.DIAMOND,
			GRSymbolShapeType.ELLIPSE_HORIZONTAL,
			GRSymbolShapeType.ELLIPSE_VERTICAL,
			GRSymbolShapeType.RECTANGLE_HORIZONTAL,
			GRSymbolShapeType.RECTANGLE_VERTICAL,
			GRSymbolShapeType.PLUS,
			GRSymbolShapeType.PLUS_SQUARE,
			GRSymbolShapeType.X,
			GRSymbolShapeType.X_CAP,
			GRSymbolShapeType.X_EDGE,
			GRSymbolShapeType.X_SQUARE,

			GRSymbolShapeType.ARROW_RIGHT,
			GRSymbolShapeType.ARROW_LEFT,
			GRSymbolShapeType.ARROW_UP,
			GRSymbolShapeType.ARROW_DOWN,

			GRSymbolShapeType.ASTERISK,
			GRSymbolShapeType.X_DIAMOND,

			// Filled.

			GRSymbolShapeType.CIRCLE_FILLED,
			GRSymbolShapeType.SQUARE_FILLED,
			GRSymbolShapeType.TRIANGLE_UP_FILLED,
			GRSymbolShapeType.TRIANGLE_DOWN_FILLED,
			GRSymbolShapeType.TRIANGLE_LEFT_FILLED,
			GRSymbolShapeType.TRIANGLE_RIGHT_FILLED,
			GRSymbolShapeType.DIAMOND_FILLED,
			GRSymbolShapeType.ELLIPSE_HORIZONTAL_FILLED,
			GRSymbolShapeType.ELLIPSE_VERTICAL_FILLED,
			GRSymbolShapeType.RECTANGLE_HORIZONTAL_FILLED,
			GRSymbolShapeType.RECTANGLE_VERTICAL_FILLED,

			// TODO smalers 2023-07-31 Not sure about these.
			GRSymbolShapeType.ARROW_DOWN_LEFT_FILLED,
			GRSymbolShapeType.ARROW_DOWN_RIGHT_FILLED,
			GRSymbolShapeType.ARROW_UP_LEFT_FILLED,
			GRSymbolShapeType.ARROW_UP_RIGHT_FILLED,
			GRSymbolShapeType.CIRCLE_PLUS,
			GRSymbolShapeType.INSTREAM_FLOW,
			GRSymbolShapeType.TEACUP,
			GRSymbolShapeType.VERTICAL_BAR_SIGNED,
			GRSymbolShapeType.TRIANGLE_UP_TOP_LINE,
			GRSymbolShapeType.TRIANGLE_DOWN_BOTTOM_LINE,
			GRSymbolShapeType.TRIANGLE_UP_TOP_LINE,
			GRSymbolShapeType.TRIANGLE_DOWN_BOTTOM_LINE,
			GRSymbolShapeType.PUSHPIN_VERTICAL
		};
		return shapeTypes;
	}
	else {
		// Should never happen.
		return null;
	}
}

/**
Return the symbol shape type.
*/
public GRSymbolShapeType getShapeType () {
	return this.shapeType;
}

/**
Return the symbol size.
Although no distinction is made internally,
the symbol size is typically implemented in higher level software as a pixel size (not data units).
Symbols can have different sizes in the X and Y direction but return the X size here
(assumed to be the same as the Y direction size).
@return the symbol size.
*/
public double getSize () {
	return this.__size_x;
}

/**
Return the symbol size in the X direction.
Although no distinction is made internally,
the symbol size is typically implemented in higher level software as a pixel size (not data units).
@return the symbol size in the X direction.
*/
public double getSizeX () {
	return this.__size_x;
}

/**
Return the symbol size in the Y direction.
Although no distinction is made internally,
the symbol size is typically implemented in higher level software as a pixel size (not data units).
@return the symbol size in the Y direction.
*/
public double getSizeY () {
	return this.__size_y;
}

/**
Return the symbol style.  If a point type, then the point symbol will be returned.
If a line type, then the line style (e.g., SOLID) will be returned.
If a polygon, then the fill type will be returned.  Currently only point types are fully supported.
*/
/* TODO smalers 2023-07-27 replaced with get shapeType().
public int getStyle () {
	return this._style;
}
*/

/**
Return the style as a string name.
@return the style as a string name (e.g., "Circle-Filled").
*/
/* TODO smalers 2023-07-27 replaced with enumeration toString().
public String getStyleName() {
	return SYMBOL_NAMES[_style];
}
*/

/**
Return the transparency (255 = completely transparent, 0 = opaque).
@return the transparency.
*/
public int getTransparency () {
	return this.__transparency;
}

/**
Return the symbol type (e.g., whether a point or polygon type).
@return the symbol type.
*/
public GRSymbolType getType () {
	return this._type;
}

/**
Initialize data.
*/
private void initialize() {
	this._color = null;
	this._color2 = null;
	this._outline_color = null;
	this.shapeType = GRSymbolShapeType.NONE;
	this.__size_x = 0.0;
	this.__size_y = 0.0;
	this._type = GRSymbolType.NONE;
}

/**
Specify whether the symbol is a primary symbol.
A primary symbol is, for example, the symbol for a polygon,
where secondary symbols may be drawn at the polygon centroid.
@param is_primary Indicates whether the symbol is a primary symbol.
@return the value of the flag after setting.
*/
public boolean isPrimary ( boolean is_primary ) {
	this.__is_primary = is_primary;
	return this.__is_primary;
}

/**
Indicate whether the symbol is a primary symbol.
A primary symbol is, for example, the symbol for a polygon,
where secondary symbols may be drawn at the polygon centroid.
@return true if the symbol is the primary symbol, false if not.
*/
public boolean isPrimary () {
	return this.__is_primary;
}

/**
Indicate whether a symbol can be used for the specified classification.
@return true if the symbol can be used with scaled symbol classification.
*/
public static boolean isSymbolForClassification ( GRClassificationType classificationType, GRSymbolShapeType sym ) {
	if ( classificationType == GRClassificationType.SCALED_SYMBOL ) {
		if ( sym == GRSymbolShapeType.VERTICAL_BAR_SIGNED || sym == GRSymbolShapeType.VERTICAL_BAR_UNSIGNED) {
			return true;
		}
		return false;
	}
	return true;
}

/**
Indicate whether only selected shapes should be labeled.
@return true if only selected shapes should be labeled.
*/
public boolean labelSelectedOnly () {
	return this._label_selected_only;
}

/**
Set whether only selected shapes should be labeled.
@param label_selected_only Indicates that only selected shapes should be labeled.
@return value of flag, after reset.
*/
public boolean labelSelectedOnly ( boolean label_selected_only ) {
	this._label_selected_only = label_selected_only;
	return this._label_selected_only;
}

/**
Set the data that are used with a classification to look up a color for drawing.
Currently, only one data type (double, int, etc.) can be used for the classification.
It is assumed that external code is used to define the data values and that the number
of values corresponds to the number of colors in the color table.
@param data Data to use for classification.
The number of data values should be less than or equal to the number of colors in the color map.
@param make_copy Indicates whether a copy of the data array should be made
(true) or not (false), in which case the calling code should maintain the list.
*/
public void setClassificationData ( double[] data, boolean make_copy ) {
	clearData();
	if ( data == null ) {
		return;
	}
	if ( make_copy ) {
		this._double_data = new double[data.length];
		for ( int i = 0; i < data.length; i++ ) {
			this._double_data[i] = data[i];
		}
	}
	else {
		this._double_data = data;
	}
}

/**
Set the classification field.
This is just a helper function to carry around data that may be useful to an application
(e.g., so a property display dialog can be created) but the field name is not used internally.
@param classificationField field (e.g., from database) used to determine color, etc., for visualization.
*/
public void setClassificationField ( String classificationField ) {
	if ( classificationField != null ) {
		this._classification_field = classificationField;
	}
}

/**
Set the classification type.
@param classificationType Classification type for the layer
*/
public void setClassificationType ( GRClassificationType classificationType ) {
	this._classification_type = classificationType;
}

/**
Set the color for the symbol (equivalent to the foreground color).
This is used with single classification.
@param color Color to use for symbol.
*/
public void setColor ( GRColor color ) {
	this._color = color;
}

/**
Set the second color for the symbol (equivalent to the second foreground color).
This is used with scaled symbol classification where necessary.
@param color2 Second color to use for symbol.
*/
public void setColor2 ( GRColor color2 ) {
	this._color2 = color2;
}

/**
Set the color table for the symbol, used for looking up colors when other than a single classification is being used.
The number of colors in the color table should agree with the number of classifications.
@param color_table GRColorTable to use.
*/
public void setColorTable ( GRColorTable color_table ) {
	this._color_table = color_table;
}

/**
Set the color table for the symbol, given the color table name.  A new GRColorTable will be created.
@param table_name GRColorTable name.
@param ncolors Number of colors to use.
*/
public void setColorTable ( String table_name, int ncolors ) {
	this._color_table = GRColorTable.createColorTable ( table_name, ncolors, false );
}

/**
Set the label field.  Currently this is just carried around to help other graphics code.
@param label_field field (e.g., from database) used to label a symbol.
*/
public void setLabelField ( String label_field ) {
	if ( label_field != null ) {
		this._label_field = label_field;
	}
}

/**
Set the label font height (points).
@param label_font_height Label font height.
*/
public void setLabelFontHeight ( double label_font_height ) {
	this._label_font_height = label_font_height;
}

/**
Set the label font name (e.g., "Helvetica").  More information about available font names will be added later.
This does not create a Font for the symbol.  It just stores the font name.
@param label_font_name Font name to use for labels.
*/
public void setLabelFontName ( String label_font_name ) {
	if ( label_font_name != null ) {
		this._label_font_name = label_font_name;
	}
}

/**
Set the label format.  The format is used to format label fields and should
contain a StringUtil format specifier appropriate for the label field data types.
@param label_format Format used to label a symbol.
*/
public void setLabelFormat ( String label_format ) {
	if ( label_format != null ) {
		this._label_format = label_format;
	}
}

/**
Set the label position (to combination of GRText position mask values).
@param label_position Position for labels.
*/
public void setLabelPosition ( int label_position ) {
	this._label_position = label_position;
}

/**
Set the outline color.
*/
public void setOutlineColor ( GRColor outline_color ) {
	this._outline_color = outline_color;
}

/**
Set the symbol point type.
@param shapeType For point symbols, tye symbol type.
*/
public void setShapeType ( GRSymbolShapeType shapeType ) {
	this.shapeType = shapeType;
}

/**
Set the symbol size.  Both the x and y direction sizes are set to the same value.
*/
public void setSize ( double size ) {
	this.__size_x = size;
	this.__size_y = size;
}

/**
Set the symbol size in the X direction.
@param size_x Symbol size in the X direction.
*/
public void setSizeX ( double size_x ) {
	this.__size_x = size_x;
}

/**
Set the symbol size in the Y direction.
@param size_y Symbol size in the Y direction.
*/
public void setSizeY ( double size_y ) {
	this.__size_y = size_y;
}

/**
Set the symbol style.
@param style For point symbols, see SYM_*.
*/
/* TODO smalers 2023-07-27 replaced by setPointType
public void setStyle ( int style ) {
	this._style = style;
}
*/

/**
Set the transparency (255 = completely transparent, 0 = opaque).
@param transparency the transparency.
*/
public void setTransparency ( int transparency ) {
	if ( transparency < 0 ) {
		transparency = 0;
	}
	else if ( transparency > 255 ) {
		transparency = 255;
	}
	else {
		this.__transparency = transparency;
	}
}

/**
Return a string representation of the symbol.
@return a string representation of the symbol.
*/
public String toString () {
	return new String ( "Type: " + _type + " Color: " + _color +
			" Color2: " + _color2 + " Size: " + __size_x + "," + __size_y );
}

}