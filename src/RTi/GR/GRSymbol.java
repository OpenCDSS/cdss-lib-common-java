// ----------------------------------------------------------------------------
// GRSymbol - store symbol definition information for points, lines, polygons
// ----------------------------------------------------------------------------
// History:
//
// 2000-10-15	Steven A. Malers, RTi	Add SYMBOL_NAMES array.
// 2001-09-17	SAM, RTi		Add classification data to allow
//					polygons to be drawn as colored
//					polygons.
// 2001-10-10	SAM, RTi		Add getStyle() to start phasing in line
//					and polygon styles.  Add getColorTable()
//					so it can be displayed in properties
//					dialog.  Add _label_field and
//					_label_format and associated set/get
//					methods.
// 2001-10-15	SAM, RTi		Change toInteger() to throw an exception
//					if the string symbol name cannot be
//					matched.  Add _label_selected_only
//					property to help clarify displays.
// 2001-12-02	SAM, RTi		Add getColorNumber().
// 2002-07-11	SAM, RTi		Add more symbol names/numbers to lookup
//					arrays to support more applications.
// 2002-07-23	SAM, RTi		Add VerticalBar-Signed and
//					TeaCup symbols.  Remove deprecated
//					static data.  Add isPrimarySymbol() to
//					allow an array of GRSymbol to be used
//					where one symbol is treated as a primary
//					symbol (e.g., to draw polygon where
//					secondary symbols are drawn at the
//					centroid).  Save the size as an X and
//					Y size to facilitate symbols like bars.
//					Change symbol "subtype" to "style".
// 2002-09-24	SAM, RTi		Break out the scaled classification
//					symbol data into the
//					GRScaledClassificationSymbol class.
// 2002-12-19	SAM, RTi		Add transparency data field for use with
//					filled polygons.
// ----------------------------------------------------------------------------
// 2003-05-08	J. Thomas Sapienza, RTi	Made changes following SAM's review
// 2004-08-10	JTS, RTi		Added support for scaled teacup symbols.
// 2004-09-16	JTS, RTi		Corrected 1-off bug in getting the 
//					colors for class break symbols.
// 2004-10-06	JTS, RTi		Added new symbol:
//					  VBARUNSIGNED
// 2004-10-26	JTS, RTi		Added new symbol:
//					  CIRCLE_PLUS
// 2004-10-27	JTS, RTi		Implements Cloneable.
// 2005-04-26	JTS, RTi		finalize() uses IOUtil.nullArray().
// 2006-02-08	JTS, RTi		Added:
//					  SYM_FUTRI_TOPLINE
//					  SYM_UTRI_TOPLINE
//					  SYM_FDTRI_BOTLINE
//					  SYM_DTRI_BOTLINE
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GR;

import RTi.Util.IO.IOUtil;

import RTi.Util.String.StringUtil;

/**
This class stores information necessary to draw symbology.  Symbols may be used
with points, lines (in which case a line style, width, and color are used),
and polygons (in which case color, outline color, and fill pattern are used).
Consequently, symbols can be used for graphs, maps, and other visualization.
Symbols can currently be varied in color based on a classification; however,
varying the size (e.g., for point symbols) is NOT enabled.  If classification
is used, then additional data like a color table are used.  Typically the color
table is constructed using GRColorTable.createColorTable().  If multiple symbols
are used for a shape, an array of GRSymbol can be used (e.g., see GRLegend).
Additional features may be added later to help with optimization of multi-symbol
layout.
NEED TO ADD DERIVED CLASSES TO STORE THE MORE SPECIFIC INFORMATION (like
color table) SO THIS CLASS IS NOT SO BLOATED.
TODO (JTS - 2003-05-05) color table has been moved out, but this still has a lot of stuff in it.
does it need any anti-bloating?
SAM: agree -- later
*/
public class GRSymbol
implements Cloneable
{

/**
Symbol types.
The first is no symbol.
*/
public static final int TYPE_NONE = 0;

/**
Draw a point symbol.  Color and symbol size can be specified.
*/
public static final int TYPE_POINT = 1;

/**
Draw a line symbol.  Color, line width, and pattern can be specified.
*/
public static final int TYPE_LINE = 2;

/**
Draw a line symbol with points.  This is a combination of lines and points,
suitable for a graph.
*/
public static final int TYPE_LINE_AND_POINTS = 3;

/**
Fill a polygon.  Color, outline color, and pattern can be specified.
*/
public static final int TYPE_POLYGON = 4;

/**
Fill a polygon and draw a symbol.  Symbol, color, outline color,
and pattern can be specified.
*/
public static final int TYPE_POLYGON_AND_POINT = 5;

/**
Names for symbols, for use with persistent storage.  These are consistent with
SYMBOL_NUMBERS.  Only symbols that are useful in a final application are
included (not building-block symbols).
*/
public static final String[] SYMBOL_NAMES = {	"None",	// Transparent
						"Arrow-Down",
						"Arrow-Down-Left",
						"Arrow-Down-Right",
						"Arrow-Left",
						"Arrow-Right",
						"Arrow-Up",
						"Arrow-Up-Left",
						"Arrow-Up-Right",
						"Asterisk",
						"Circle-Hollow",
						"Circle-Filled",
						"Circle-Plus",
						"Diamond-Hollow",
						"Diamond-Filled",
						"InstreamFlow",
						"Plus",
						"Plus-Square",
						"Square-Hollow",
						"Square-Filled",
						"TeaCup",
						"Triangle-Down-Hollow",
						"Triangle-Down-Filled",
						"Triangle-Left-Hollow",
						"Triangle-Left-Filled",
						"Triangle-Right-Hollow",
						"Triangle-Right-Filled",
						"Triangle-Up-Hollow",
						"Triangle-Up-Filled",
						"VerticalBar-Signed",
						"X",
						"X-Cap",
						"X-Diamond",
						"X-Edge",
						"X-Square",
						"Triangle-Up-Filled-Topline",
						"Triangle-Down-Filled-Botline",
						"Triangle-Up-Hollow-Topline",
						"Triangle-Down-Hollow-Botline"
					};

/**
Simple line symbol: no symbol</b>
*/
public static final int SYM_NONE = 0;
/**
Simple line symbol: <b>circle</b>
*/
public static final int SYM_CIR	= 1;
/**
Simple line symbol: <b>square</b>
*/
public static final int SYM_SQ = 2;
/**
Simple line symbol: <b>triangle pointing up</b>
*/
public static final int SYM_UTRI = 3;
/**
Simple line symbol: <b>triangle pointing down</b>
*/
public static final int SYM_DTRI = 4;
/**
Simple line symbol: <b>triangle pointing left</b>
*/
public static final int SYM_LTRI = 5;
/**
Simple line symbol: <b>triangle pointing right</b>
*/
public static final int SYM_RTRI = 6;
/**
Simple line symbol: <b>diamond</b>
*/
public static final int SYM_DIA	= 7;

/**
Compound line symbol: <b>+</b>
*/
public static final int SYM_PLUS = 8;
/**
Compound line symbol: <b>+ with square around it</b>
*/
public static final int SYM_PLUSQ = 9;
/**
Compound line symbol: <b>X</b>
*/
public static final int SYM_EX = 10;
/**
Compound line symbol: <b>X with line over top and bottom</b>
*/
public static final int SYM_EXCAP = 11;
/**
Compound line symbol: <b>X with lines on edge</b>
*/
public static final int SYM_EXEDGE = 12;
/**
Compound line symbol: <b>X with square around it</b>
*/
public static final int SYM_EXSQ = 13;
/**
Compound line symbol: <b>Right arrow</b>
*/
public static final int SYM_RARR = 14;
/**
Compound line symbol: <b>Left arrow</b>
*/
public static final int SYM_LARR = 15;
/**
Compound line symbol: <b>Up arrow</b>
*/
public static final int SYM_UARR = 16;
/**
Compound line symbol: <b>Down arrow</b>
*/
public static final int SYM_DARR = 17;
/**
Compound line symbol: <b>Asterisk</b>
*/
public static final int SYM_AST = 18;
/**
Compound line symbol: <b>X with diamond around it</b>
*/
public static final int SYM_EXDIA = 19;

/**
Simple filled symbol: <b>filled circle</b>
*/
public static final int SYM_FCIR = 20;	// filled circle
/**
Simple filled symbol: <b>filled square</b>
*/
public static final int SYM_FSQ	= 21;	// filled square
/**
Simple filled symbol: <b>filled triangle pointing up</b>
*/
public static final int SYM_FUTRI = 22;	// filled up triangle
/**
Simple filled symbol: <b>filled triangle pointing down</b>
*/
public static final int SYM_FDTRI = 23;	// filled down triangle
/**
Simple filled symbol: <b>filled triangle pointing left</b>
*/
public static final int SYM_FLTRI = 24;	// filled left triangle
/**
Simple filled symbol: <b>filled triangle pointing right</b>
*/
public static final int SYM_FRTRI = 25;	// filled right triangle
/**
Simple filled symbol: <b>filled diamond</b>
*/
public static final int SYM_FDIA = 26;	// filled diamond

// Start complicated filled symbols

/**
Complicated filled symbol: Diamond made of four diamonds with "1" in 
top diamond, nothing in other three number areas.
*/
public static final int SYM_EXDIA1	=27;
/**
Complicated filled symbol: Diamond made of four diamonds with "2" in right 
diamond, nothing in other three number areas.
*/
public static final int SYM_EXDIA2	=28;
/**
Complicated filled symbol: Diamond made of four diamonds with "3" in bottom 
diamond, nothing in other three number areas.
*/
public static final int SYM_EXDIA3	=29;
/**
Complicated filled symbol: Diamond made of four diamonds with "4" in bottom 
diamond, nothing in other three number areas.
*/
public static final int SYM_EXDIA4	=30;
/**
Complicated filled symbol: Diamond made of four diamonds with "1" in top 
diamond, "2" in right diamond, nothing in other two number areas.
*/
public static final int SYM_EXDIA12	=31;
/**
Complicated filled symbol: Diamond made of four diamonds with "1" in top 
diamond, "2" in right diamond, "3" in bottom diamond, nothing in fourth
diamond.
*/
public static final int SYM_EXDIA123	=32;
/**
Complicated filled symbol: Diamond made of four diamonds with "1" in top 
diamond, "2" in right diamond, "3" in bottom diamond, "4" in left diamond.
*/
public static final int SYM_EXDIA1234	=33;
/**
Complicated filled symbol: Diamond made of four diamonds with "1" in top 
diamond, "3" in bottom diamond, nothing in other two number areas.
*/
public static final int SYM_EXDIA13	=34;
/**
Complicated filled symbol: Diamond made of four diamonds with "1" in top 
diamond, "4" in left diamond, nothing in other two number areas.
*/
public static final int SYM_EXDIA14	=35;
/**
Complicated filled symbol: Diamond made of four diamonds with "2" in right 
diamond, "3" in bottom diamond, nothing in other two number areas.
*/
public static final int SYM_EXDIA23	=36;
/**
Complicated filled symbol: Diamond made of four diamonds with "2" in right 
diamond, "4" in left diamond, nothing in other two number areas.
*/
public static final int SYM_EXDIA24	=37;
/**
Complicated filled symbol: Diamond made of four diamonds with "2" in right 
diamond, "3" in bottom diamond, "4" in left diamond, nothing in fourth 
number area.
*/
public static final int SYM_EXDIA234	=38;
/**
Complicated filled symbol: Diamond made of four diamonds with "1" in top 
number area, "2" in right diamond, "4" in left diamond, nothing in fourth
number area.
*/
public static final int SYM_EXDIA124	=39;
/**
Complicated filled symbol: Diamond made of four diamonds with "1" in top 
number area, "3" in bottom diamond, "4" in left diamond, nother in fourth 
number area.
*/
public static final int SYM_EXDIA134	=40;

/**
Complicated filled symbol: <b>square filled on top</b>
*/
public static final int SYM_TOPFSQ = 41;
/**
Complicated filled symbol: <b>square filled on bottom</b>
*/
public static final int SYM_BOTFSQ = 42;
/**
Complicated filled symbol: <b>square filled on right</b>
*/
public static final int SYM_RFSQ = 43;
/**
Complicated filled symbol: <b>square filled on left</b>
*/
public static final int SYM_LFSQ = 44;
/**
Complicated filled symbol: <b>filled arrow to upper right</b>
*/
public static final int SYM_FARR1 = 45;
/**
Complicated filled symbol: <b>filled arrow to lower right</b>
*/
public static final int SYM_FARR2 = 46;
/**
Complicated filled symbol: <b>filled arrow to lower left</b>
*/
public static final int SYM_FARR3 = 47;
/**
Complicated filled symbol: <b>filled arrow to upper left</b>
*/
public static final int SYM_FARR4 = 48;
/**
Complicated filled symbol: <b>instream flow symbol</b>
*/
public static final int SYM_INSTREAM = 49;
/**
Complicated filled symbol: <b>tea cup symbol</b>
*/
public static final int SYM_TEACUP = 50;
/**
Complicated filled symbol: <b>vertical bar where positive values are above
center and negative values are below center</b>
*/
public static final int SYM_VBARSIGNED = 51;

/**
Building blocks (incomplete symbols): <b>minus (-)</b>
*/
public static final int SYM_MIN = 52;
/**
Building blocks (incomplete symbols): <b>bar (|)</b>
*/
public static final int SYM_BAR	= 53;
/**
Building blocks (incomplete symbols): <b>forward slash (/)</b>
*/
public static final int SYM_FSLASH = 54;
/**
Building blocks (incomplete symbols): <b>backslash (\)</b>
*/
public static final int SYM_BSLASH = 55;
/**
Building blocks (incomplete symbols): <b>line on top</b>
*/
public static final int SYM_TOPLINE = 56;
/**
Building blocks (incomplete symbols): <b>line on bottom</b>
*/
public static final int SYM_BOTLINE = 57;
/**
Building blocks (incomplete symbols): <b>line on right</b>
*/
public static final int SYM_RLINE = 58;
/**
Building blocks (incomplete symbols): <b>line on left</b>
*/
public static final int SYM_LLINE = 59;
/**
Building blocks (incomplete symbols): <b>lines on top and bottom</b>
*/
public static final int SYM_CAP	= 60;
/**
Building blocks (incomplete symbols): <b>lines on left and right</b>
*/
public static final int SYM_EDGE = 61;
/**
Building blocks (incomplete symbols): <b>caret (^)</b>
*/
public static final int SYM_UCAR = 62;
/**
Building blocks (incomplete symbols): <b>down caret</b>
*/
public static final int SYM_DCAR = 63;
/**
Building blocks (incomplete symbols): <b>left caret</b>
*/
public static final int SYM_LCAR = 64;
/**
Building blocks (incomplete symbols): <b>right caret</b>
*/
public static final int SYM_RCAR = 65;
/** 
Building blocks (incomplete symbols): <b>smaller X that can be placed 
inside of a diamond</b>
*/
public static final int SYM_EXFORDIA = 66;
/**
Building blocks (incomplete symbols): <b>filled top quad of a diamond</b>
*/
public static final int SYM_FDIA1 = 67;
/**
Building blocks (incomplete symbols): <b>filled right quad of a diamond</b>
*/
public static final int SYM_FDIA2 = 68;
/**
Building blocks (incomplete symbols): <b>filled bottom quad of a diamond</b>
*/
public static final int SYM_FDIA3 = 69;
/**
Building blocks (incomplete symbols): <b>filled left quad of a diamond</b>
*/
public static final int SYM_FDIA4 = 70;
/**
Building blocks (incomplete symbols): <b>filled upper right triangle in 
square</b>
*/
public static final int SYM_FSQTRI1 = 71;
/**
Building blocks (incomplete symbols): <b>filled lower right triangle in
square</b>
*/						
public static final int SYM_FSQTRI2 = 72;
/**
Building blocks (incomplete symbols): <b>filled lower left triangle in
square</b>
*/						
public static final int SYM_FSQTRI3 = 73;
/**
Building blocks (incomplete symbols): <b>filled upper left triangle in
square</b>
*/						
public static final int SYM_FSQTRI4 = 74;
/**
Building blocks (incomplete symbols): <b>filled upper half of diamond</b>
*/
public static final int SYM_FTOPDIA = 75;
/**
Building blocks (incomplete symbols): <b>filled bottom half of diamond</b>
*/
public static final int SYM_FBOTDIA = 76;
/**
Building blocks (incomplete symbols): <b>filled right half of diamond</b>
*/
public static final int SYM_FRDIA = 77;
/**
Building blocks (incomplete symbols): <b>filled left half of diamond</b>
*/
public static final int SYM_FLDIA = 78;
/**
Building blocks (incomplete symbols): <b>filled upper 1/4 of diamond</b>
*/
public static final int SYM_FTOPDIA4 = 79;
/**
Building blocks (incomplete symbols): <b>filled bottom 1/4 of diamond</b>
*/
public static final int SYM_FBOTDIA4 = 80;
/**
Building blocks (incomplete symbols): <b>filled right 1/4 of diamond</b>
*/
public static final int SYM_FRDIA4 = 81;
/**
Building blocks (incomplete symbols): <b>filled left 1/4 of diamond</b>
*/
public static final int SYM_FLDIA4 = 82;
/**
Complicated filled symbol: <b>vertical bar where all values are positive</b>.
The bar is centered on its X location and rises up from its Y location.
*/
public static final int SYM_VBARUNSIGNED = 83;
/**
Compound line symbol: <b>+ with circle around it</b>
*/
public static final int SYM_PLUSCIR = 84;

/**
Compound symbol: <b>Filled triangle pointing up with a line on the point.</b>
*/
public static final int SYM_FUTRI_TOPLINE = 85;

/**
Compound symbol: <b>Filled triangle pointing down with a line on the point.</b>
*/
public static final int SYM_FDTRI_BOTLINE = 86;

/**
Compound symbol: <b>Hollow triangle pointing up with a line on the point.</b>
*/
public static final int SYM_UTRI_TOPLINE = 87;

/**
Compound symbol: <b>Hollow triangle pointing down with a line on the point.</b>
*/
public static final int SYM_DTRI_BOTLINE = 88;

/**
Summary definition (used by routines to limit symbol selection): <b>
first "nice" symbol</b>
*/
public static final int SYM_FIRST = SYM_CIR;
/**
Summary definition (used by routines to limit symbol selection): <b>
last "nice" symbol</b>
*/
public static final int SYM_LAST = SYM_FARR4;
/**
Summary definition (used by routines to limit symbol selection): <b>
last of all symbols</b>
*/
public static final int SYM_LASTALL = SYM_FLDIA4;
/**
Summary definition (used by routines to limit symbol selection): <b>
first outline symbol</b>
*/
public static final int SYM_FIRST_OUT = SYM_CIR;
/**
Summary definition (used by routines to limit symbol selection): <b>
last outline symbol</b>
*/
public static final int SYM_LAST_OUT = SYM_DIA;
/**
Summary definition (used by routines to limit symbol selection): <b>
first filled symbol</b>
*/
public static final int SYM_FIRST_FILL = SYM_FCIR;
/**
Summary definition (used by routines to limit symbol selection): <b>
last filled symbol</b>
*/
public static final int SYM_LAST_FILL = SYM_FDIA;
/**
Summary definition (used by routines to limit symbol selection): <b>
first line symbol</b>
*/
public static final int SYM_FIRST_LINE = SYM_CIR;
/**
Summary definition (used by routines to limit symbol selection): <b>
last line symbol</b>
*/
public static final int SYM_LAST_LINE = SYM_EXDIA;

/**
Orientation for symbols.  The following means the left side of the symbol is
at the coordinate.
*/
public static final int SYM_LEFT = 0x01;
/**
Orientation for symbols.  The following means the device is centered around the
X point of the coordinate
*/
public static final int SYM_CENTER_X = 0x02;
/**
Orientation for symbols.  The following means the right side of the symbol is
at the coordinate.
*/
public static final int SYM_RIGHT = 0x04;
/**
Orientation for symbols.  The following means the bottom side of the symbol is
at the coordinate.
*/						
public static final int SYM_BOTTOM = 0x08;
/**
Orientation for symbols.  The following means the top side of the symbol is
at the coordinate.
*/
public static final int SYM_TOP = 0x10;
/**
Orientation for symbols.  The following means the device is centered around the
Y point of the coordinate
*/
public static final int SYM_CENTER_Y = 0x20;

/**
Symbol numbers.  These are consistent with SYMBOL_NAMES.
*/
public static final int[] SYMBOL_NUMBERS = {	SYM_NONE,
						SYM_DARR,
						SYM_FARR3,
						SYM_FARR2,
						SYM_LARR,
						SYM_RARR,
						SYM_UARR,
						SYM_FARR4,
						SYM_FARR1,
						SYM_AST,
						SYM_CIR,
						SYM_FCIR,
						SYM_PLUSCIR,
						SYM_DIA,
						SYM_FDIA,
						SYM_INSTREAM,
						SYM_PLUS,
						SYM_PLUSQ,
						SYM_SQ,
						SYM_FSQ,
						SYM_TEACUP,
						SYM_DTRI,
						SYM_FDTRI,
						SYM_LTRI,
						SYM_FLTRI,
						SYM_RTRI,
						SYM_FRTRI,
						SYM_UTRI,
						SYM_FUTRI,
						SYM_VBARSIGNED,
						SYM_EX,
						SYM_EXCAP,
						SYM_EXDIA,
						SYM_EXEDGE,
						SYM_EXSQ,
						SYM_FUTRI_TOPLINE,
						SYM_FDTRI_BOTLINE,
						SYM_UTRI_TOPLINE,
						SYM_DTRI_BOTLINE
					};

/**
Names for symbol classifications, for use with persistent storage and graphical
user interfaces.  These are consistent with
CLASSIFICATION_NUMBERS.
*/
public static final String[] CLASSIFICATION_NAMES = {
				"SingleSymbol",	// One symbol
				"UniqueValues",	// One symbol per value, but
						// all symbols are the same
						// other than color or other
						// characteristics that can be
						// automatically assigned.
				"ClassBreaks",	// Class breaks.  The symbol
						// is graded based on the data
						// value into groups of colors,
						// sizes, patterns, etc.
				"ScaledSymbol",	// A single symbol style is
						// used (e.g., vertical bars)
						// but the size of the symbol
						// is scaled to "exact" size
						// based on a data value (unlike
						// unique values where different
						// symbols are used for each
						// value or class breaks where
						// symbols have definite
						// breaks).
				"ScaledTeacupSymbol"
						// A single symbol style is used
						// (e.g., teacup), but the size
						// of the teacup and the amount
						// that the teacup is filled
						// are based on data values
						// read from an attribute table.
			};

/**
Classification number for a single symbol.
*/
public static final int CLASSIFICATION_SINGLE = 0;
/**
Classification number for one symbol per value, but all symbols are 
the same other than color or other characteristics that can automatically be
assigned.
*/
public static final int CLASSIFICATION_UNIQUE_VALUES = 1;
/**
Classification number for class breaks, in which the symbol is graded based on
the data value into groups of colors, sizes, patterns, etc.
*/
public static final int CLASSIFICATION_CLASS_BREAKS = 2;
/**
Classification number for a single symbol style, but the size of the symbol is
scaled to "exact" data size based on a data value (unlike unique values
where different symbols are used for each value or class breaks where symbols
have definite breaks).
*/
public static final int CLASSIFICATION_SCALED_SYMBOL = 3;

/**
Classification number for a single symbol style, where the size of the teacup
is scaled and the amount that the teacup is filled is dependent on values
read from an attribute table.
*/
public static final int CLASSIFICATION_SCALED_TEACUP_SYMBOL = 4;

/**
Symbol classification type numbers.  These are consistent with
CLASSIFICATION_NAMES.
*/
public static final int[] CLASSIFICATION_NUMBERS = {
				CLASSIFICATION_SINGLE,
				CLASSIFICATION_UNIQUE_VALUES,
				CLASSIFICATION_CLASS_BREAKS,
				CLASSIFICATION_SCALED_SYMBOL,
				CLASSIFICATION_SCALED_TEACUP_SYMBOL
			};

/**
Indicates whether only selected shapes should be labelled.  This is useful
to clarify displays.
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
private GRColor _color = null;		// Foreground color.
/**
Secondary foreground color (e.g., for negative bars)
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
private int _classification_type = CLASSIFICATION_SINGLE;
/**
Array of integer data corresponding to colors in the color table.
*/
private int _int_data[] = null;
/**
Label position (left-justified against the symbol, centered in Y direction).
*/
private int _label_position = GRText.LEFT|GRText.CENTER_Y;
/**
Symbol style.
*/
private int _style = SYM_NONE;
/**
Transparency level.  Default is totally opaque.  The transparency is the
reverse of the alpha.  0 transparency is totally opaque, alpha 255.
*/
private int __transparency = 0;
/**
The type of the symbol.
*/
private int _type = TYPE_NONE;

/**
The classification field to be used (used by higher-level code).
*/
private String _classification_field = "";
/**
Array of string data corresponding to the color table.
*/
private String _string_data[] = null;
/**
Name of field(s) to use for labelling.
*/
private String _label_field = null;
/**
Format to use for labelling.
*/
private String _label_format = null;
/**
Name of font for labels.
*/
private String _label_font_name = "Helvetica";

/**
Construct.  Colors are initialized to null and the symbol values to TYPE_NONE,
SYM_NONE.
*/
public GRSymbol ()
{	initialize();
}

/**
Construct using the given parameters.
@param type Symbol type (see TYPE_*).
@param style Indicates the symbol style for the symbol type.
For example, if the type is TYPE_POINT, then the style can be set to any
SYM_* values).
@param color Foreground color.
@param outline_color Outline color for polygons.
@param size Symbol size.  Currently units are not handled.  Treat is a storage
area for the size that will be specified to GR.drawSymbol().  The x and y
direction sizes are set to the single value.
*/
public GRSymbol ( int type, int style, GRColor color, GRColor outline_color,
		double size )
{	this ( type, style, color, outline_color, size, size );
}

/**
Construct using the given parameters.
@param type Symbol type (see TYPE_*).
@param style Indicates the symbol style for the symbol type.
For example, if the type is TYPE_POINT, then the style can be set to any
SYM_* values).
@param color Foreground color.
@param outline_color Outline color for polygons.
@param size_x Symbol size in the X direction.  Currently units are not handled.
Treat is a storage area for the size that will be specified to GR.drawSymbol().
@param size_y Symbol size in the Y direction.
*/
public GRSymbol ( int type, int style, GRColor color, GRColor outline_color,
		double size_x, double size_y )
{	initialize();
	_type = type;
	_color = color;
	_color2 = _color;
	_outline_color = outline_color;
	__size_x = size_x;
	__size_y = size_y;
	_style = style;
}

/**
Clear the data arrays used to look up a color in the color table.  This method
should be called when the lookup values are reset.
*/
private void clearData ()
{	_double_data = null;
	_int_data = null;
	_string_data = null;
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
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	_color = null;
	_color2 = null;
	_outline_color = null;
	_color_table = null;
	_classification_field = null;
	_double_data = null;
	_int_data = null;
	IOUtil.nullArray(_string_data);
	_label_field = null;
	_label_format = null;
	_label_font_name = null;
	super.finalize();
}

/**
Return the classification color.  If the classification type is single, the
main color is retured.  Otherwise the classification value is used to look up
the color in the color table.  This is useful when displaying a legend.
@param classification Symbol classification color.
@return the symbol classification color (zero index).
*/
public GRColor getClassificationColor ( int classification )
{	if ( _classification_type == CLASSIFICATION_SINGLE ) {
		return _color;
	}
	else {	
		return (GRColor)_color_table.elementAt ( classification );
	}
}

/**
Return the classification second color.  This is being phased in for use with
the CLASSIFICATION_SCALED_SYMBOL type.
@param classification Symbol classification second color.
@return the second symbol classification color (zero index).
*/
public GRColor getClassificationColor2 ( int classification )
{	if ( _classification_type == CLASSIFICATION_SINGLE ) {
		return _color2;
	}
	else {	return (GRColor)_color_table.elementAt ( classification );
	}
}

/**
Return the classification field.  This is just a place-holder to store the
classification field name (e.g., for display on a legend).
@return the symbol classification field.
*/
public String getClassificationField ()
{	return _classification_field;
}

/**
Return the classification label string.  For single classification this is an
empty string.  For class breaks, the label has "&lt; x" for the first class,
"&gt; x AND &lt;= y" for the middle classes and "&gt; x" for the last class.  
For unique values the label is just the unique value.
@param classification Classification to get a label for.
*/
public String getClassificationLabel ( int classification )
{	if ( _classification_type == CLASSIFICATION_CLASS_BREAKS ) {
		if ( _double_data != null ) {
			if ( classification == 0 ) {
				return "< " + StringUtil.formatString(
				_double_data[0],"%.3f");
			}
			else if ( classification == (_color_table.size() - 1)) {
				return ">= " + StringUtil.formatString(
				_double_data[classification],"%.3f");
			}
			else return ">= " + StringUtil.formatString(
				_double_data[classification - 1],"%.3f") +
				" AND < " + StringUtil.formatString(
				_double_data[classification],"%.3f");
		}
		else if ( _string_data != null ) {
			return "" + classification;
		}
		else if ( _int_data != null ) {
			if ( classification == 0 ) {
				return "< " + _int_data[0];
			}
			else if ( classification == (_color_table.size() - 1)) {
				return ">= " + _int_data[classification];
			}
			else return ">= " + _int_data[classification - 1] +
				" AND < " + _int_data[classification];
		}
	}
	else if ( _classification_type == CLASSIFICATION_UNIQUE_VALUES ) {
		// Need to work on this...
		return "" + classification;
	}
	return "";
}

/**
Return the classification type.
@return the symbol classification type.  See CLASSIFICATION_*.
*/
public int getClassificationType() {
	return _classification_type;
}

/**
Return the color used for the symbol if a single color is used.
Note that the color will be null if no color has been selected.
@return the color for the symbol (equivalent to the foreground color).
*/
public GRColor getColor()
{	return _color;
}

/**
Return the second color used for the symbol if a single color scheme is used (in
this case, two colors as part of the symbol are supported).
Note that the color will be null if no color has been selected.
@return the second color for the symbol (equivalent to the second foreground
color).
*/
public GRColor getColor2 ()
{	return _color2;
}

/**
Return the color used for the symbol if a classification is used.
Note that the color will be null if no color has been selected or no color
table has been set for the symbol.
The internal data arrays for double, integer, and String will be checked.  Only
one of these arrays is allowed to have values at any time.
@param value Data value for color.
@return the color for the symbol (equivalent to the foreground color).
*/
public GRColor getColor ( Object value )
{	if ( _double_data != null ) {
		return getColor ( ((Double)value).doubleValue() );
	}
	//else if ( _string_data != null ) {
		//return getColor ( (String)value );
	//}
	//else if ( _int_data != null ) {
		//return getColor ( ((Integer)value).intValue() );
	//}
	return null;
}

/**
Return the color used for the symbol.  If a classification or scaled symbol
is used the value is used to determine the symbol color.
Note that the color will be null if no color has been selected or no color
table has been set for the symbol.
@param value Data value to find color.
@return the color for the symbol (equivalent to the foreground color).
*/
public GRColor getColor ( double value )
{	if ( _classification_type == CLASSIFICATION_SINGLE ) {
		//Message.printStatus ( 1, "", "Returning single color" );
		return _color;
	}
	else if ( _classification_type == CLASSIFICATION_SCALED_SYMBOL ||
		_classification_type == CLASSIFICATION_SCALED_TEACUP_SYMBOL) {
		//Message.printStatus ( 1, "", "Returning single color" );
		if ( _style == SYM_VBARSIGNED ) {
			if ( value >= 0 ) {
				return _color;
			}
			else {	return _color2;
			}
		}
		else {	return _color;
		}
	}
	else if ( _classification_type == CLASSIFICATION_CLASS_BREAKS ) {
		// Need to check data value against ranges...
		if ( value < _double_data[0] ) {
			//Message.printStatus(1, "",
			//	"Returning first color for " + value );
			return (GRColor)_color_table.elementAt(0);
		}
		else if ( value >= _double_data[_double_data.length - 1] ) {
			//Message.printStatus(1, "",
			//	"Returning last color for "+ value  );
			return (GRColor)_color_table.elementAt(
			_double_data.length - 1);
		}
		else {	int iend = _double_data.length - 1;
			for ( int i = 1; i < iend; i++ ) {
				if (value < _double_data[i]) {
					//Message.printStatus(1, "",
					//	"Returning color " + i
					//	+ " for " + value
					//	+ "  (" + _double_data[i]
					//	+ ")");
					return (GRColor)
					_color_table.elementAt(i);
				}
			}
			return null;
		}
	}
	else {	// Unique value so just find the value...
		for ( int i = 0; i < _double_data.length; i++ ) {
			if ( _double_data[i] == value ) {
				return (GRColor)_color_table.elementAt(i);
			}
		}
		return null;
	}
}

/**
Return the color table color position for the symbol if a classification is
used.  This method is useful if an external set of colors is set up and only
the position is used.
Note that the position will be -1 if no color has been selected or no color
table has been set for the symbol.
@param value Data value to find color.
@return the color table position for the color for the symbol (equivalent to
the foreground color).
*/
public int getColorNumber ( double value )
{	if ( _classification_type == CLASSIFICATION_SINGLE ) {
		//Message.printStatus ( 1, "", "Returning single color" );
		return -1;
	}
	else if ( _classification_type == CLASSIFICATION_CLASS_BREAKS ) {
		// Need to check data value against ranges...
		if ( value < _double_data[0] ) {
			//Message.printStatus ( 1, "",
			//"Returning first color for " + value );
			return 0;
		}
		else if ( value >= _double_data[_double_data.length - 1] ) {
			//Message.printStatus ( 1, "",
			//"Returning last color for "+ value  );
			return _double_data.length - 1;
		}
		else {	int iend = _double_data.length - 1;
			for ( int i = 1; i < iend; i++ ) {
				if ( value < _double_data[i + 1] ) {
					//Message.printStatus ( 1, "",
					//"Returning color " + i+" for "+value);
					return i;
				}
			}
			return -1;
		}
	}
	else {	// Unique value so just find the value...
		for ( int i = 0; i < _double_data.length; i++ ) {
			if ( _double_data[i] == value ) {
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
public GRColorTable getColorTable()
{	return _color_table;
}

/**
Return the label field.
@return the label field.
*/
public String getLabelField ()
{	return _label_field;
}

/**
Return the label font height, points.
@return the label font height, points.
*/
public double getLabelFontHeight ()
{	return _label_font_height;
}

/**
Return the label font name.
@return the label font name.
*/
public String getLabelFontName ()
{	return _label_font_name;
}

/**
Return the label format.
@return the label format.
*/
public String getLabelFormat ()
{	return _label_format;
}

/**
Return the label position.
@return the label position.
*/
public int getLabelPosition ()
{	return _label_position;
}

/**
Return the number of classifications, which will be the number of colors in
the color table.  If a single classification (or scaled symbol) is used,
then 1 is returned.
@return the number of classifications.
*/
public int getNumberOfClassifications ()
{	if (	(_classification_type == CLASSIFICATION_SINGLE) ||
		(_classification_type == CLASSIFICATION_SCALED_SYMBOL) || 
		_classification_type == CLASSIFICATION_SCALED_TEACUP_SYMBOL) {
		//Message.printStatus ( 1, "", "Returning single color" );
		return 1;
	}
	else {	// Use the color table size so the data type checks don't have
		// to be done...
		return _color_table.size();
	}
}

/**
Return the outline color.  The outline color is used for all classifications.
@return the outline color.  Note that the color will be null if no color has
been selected.
*/
public GRColor getOutlineColor()
{	return _outline_color;
}

/**
Return the symbol size.  Although no distinction is made internally, the symbol
size is typically implemented in higher level software as a pixel size (not data
units).  Symbols can have different sizes in the X and Y direction but return
the X size here (assumed to be the same as the Y direction size).
@return the symbol size.
*/
public double getSize ()
{	return __size_x;
}

/**
Return the symbol size in the X direction.  Although no distinction is made
internally, the symbol size is typically implemented in higher level software
as a pixel size (not data units).
@return the symbol size in the X direction.
*/
public double getSizeX ()
{	return __size_x;
}

/**
Return the symbol size in the Y direction.  Although no distinction is made
internally, the symbol size is typically implemented in higher level software
as a pixel size (not data units).
@return the symbol size in the Y direction.
*/
public double getSizeY ()
{	return __size_y;
}

/**
Return the symbol style.  If a point type, then the point symbol will be
returned.  If a line type, then the line style (e.g., SOLID) will be returned.
If a polygon, then the fill type will be returned.  Currently only point types
are fully supported.
*/
public int getStyle ()
{	return _style;
}

/**
Return the transparency (255 = completely transparent, 0 = opaque).
@return the transparency.
*/
public int getTransparency ()
{	return __transparency;
}

/**
Return the symbol type (e.g., whether a point or polygon type).
@return the symbol type.  See TYPE_*.
*/
public int getType ()
{	return _type;
}

/**
Initialize data.
*/
private void initialize()
{	_color = null;
	_color2 = null;
	_outline_color = null;
	_style = SYM_NONE;
	__size_x = 0.0;
	__size_y = 0.0;
	_type = TYPE_NONE;
}

/**
Specify whether the symbol is a primary symbol.  A primary symbol is, for
example, the symbol for a polygon, where secondary symbols may be drawn at the
polygon centroid.
@param is_primary Indicates whether the symbol is a primary symbol.
@return the value of the flag after setting.
*/
public boolean isPrimary ( boolean is_primary )
{	__is_primary = is_primary;
	return __is_primary;
}

/**
Indicate whether the symbol is a primary symbol.  A primary symbol is, for
example, the symbol for a polygon, where secondary symbols may be drawn at the
polygon centroid.
@return true if the symbol is the primary symbol, false if not.
*/
public boolean isPrimary ()
{	return __is_primary;
}

/**
Indicate whether a symbol can be used for the specified classification.
@return true if the symbol can be used with scaled symbol classification.
*/
public static boolean isSymbolForClassification (	int classification_type,
							int sym )
{	if ( classification_type == CLASSIFICATION_SCALED_SYMBOL ) {
		if ( sym == SYM_VBARSIGNED 
			|| sym == SYM_VBARUNSIGNED) {
			return true;
		}
		return false;
	}
	return true;
}

/**
Indicate whether only selected shapes should be labelled.
@return true if only selected shapes should be labelled.
*/
public boolean labelSelectedOnly ()
{	return _label_selected_only;
}

/**
Set whether only selected shapes should be labelled.
@param label_selected_only Indicates that only selected shapes should be
labelled.
@return value of flag, after reset.
*/
public boolean labelSelectedOnly ( boolean label_selected_only )
{	_label_selected_only = label_selected_only;
	return _label_selected_only;
}

/**
Set the data that are used with a classification to look up a color for
drawing.  Currently, only one data type (double, int, etc.) can be used for the
classification.  It is assumed that external code is used to define the data
values and that the number of values corresponds to the number of colors in the
color table.
@param data Data to use for classification.  The number of data values should
be less than or equal to the number of colors in the color map.
@param make_copy Indicates whether a copy of the data array should be made
(true) or not (false), in which case the calling code should maintain the list.
*/
public void setClassificationData ( double[] data, boolean make_copy )
{	clearData();
	if ( data == null ) {
		return;
	}
	if ( make_copy ) {
		_double_data = new double[data.length];
		for ( int i = 0; i < data.length; i++ ) {
			_double_data[i] = data[i];
		}
	}
	else {	_double_data = data;
	}
}

/**
Set the classification field.  This is just a helper function to carry around
data that may be useful to an application (e.g., so a property display dialog
can be created) but the field name is not used internally.
@param classification_field field (e.g., from database) used to determine 
color, etc., for visualization.
*/
public void setClassificationField ( String classification_field )
{	if ( classification_field != null ) {
		_classification_field = classification_field;
	}	
}

/**
Set the classification type.
@param classification_type Classification field name correpsonding to
CLASSIFICATION_NAMES.
*/
public void setClassificationType ( String classification_type )
{	for ( int i = 0; i < CLASSIFICATION_NAMES.length; i++ ) {
		if (	classification_type.equalsIgnoreCase(
			CLASSIFICATION_NAMES[i]) ) {
			_classification_type = i;
			break;
		}
	}
}

/**
Set the color for the symbol (equivalent to the foreground color).  This is
used with single classification.
@param color Color to use for symbol.
*/
public void setColor ( GRColor color )
{	_color = color;
}

/**
Set the second color for the symbol (equivalent to the second foreground color).
This is used with scaled symbol classification where necessary.
@param color2 Second color to use for symbol.
*/
public void setColor2 ( GRColor color2 )
{	_color2 = color2;
}

/**
Set the color table for the symbol, used for looking up colors when other than
a single classification is being used.  The number of colors in the color table
should agree with the number of classifications.
@param color_table GRColorTable to use.
*/
public void setColorTable ( GRColorTable color_table )
{	_color_table = color_table;
}

/**
Set the color table for the symbol, given the color table name.  A new
GRColorTable will be created.
@param table_name GRColorTable name.
@param ncolors Number of colors to use.
*/
public void setColorTable ( String table_name, int ncolors )
{	_color_table = GRColorTable.createColorTable (
				table_name, ncolors, false );
}

/**
Set the label field.  Currently this is just carried around to help other
graphics code.
@param label_field field (e.g., from database) used to label a symbol.
*/
public void setLabelField ( String label_field )
{	if ( label_field != null ) {
		_label_field = label_field;
	}
}

/**
Set the label font height (points).
@param label_font_height Label font height.
*/
public void setLabelFontHeight ( double label_font_height )
{	_label_font_height = label_font_height;
}

/**
Set the label font name (e.g., "Helvetica").  More information about available
font names will be added later.  This does not create a Font for the symbol.  It
just stores the font name.
@param label_font_name Font name to use for labels.
*/
public void setLabelFontName ( String label_font_name )
{	if ( label_font_name != null ) {
		_label_font_name = label_font_name;
	}
}

/**
Set the label format.  The format is used to format label fields and should
contain a StringUtil format specifier appropriate for the label field data
types.
@param label_format Format used to label a symbol.
*/
public void setLabelFormat ( String label_format )
{	if ( label_format != null ) {
		_label_format = label_format;
	}
}

/**
Set the label position (to combination of GRText position mask values).
@param label_position Position for labels.
*/
public void setLabelPosition ( int label_position )
{	_label_position = label_position;
}

/**
Set the outline color.
*/
public void setOutlineColor ( GRColor outline_color )
{	_outline_color = outline_color;
}

/**
Set the symbol size.  Both the x and y direction sizes are set to the same
value.
*/
public void setSize ( double size )
{	__size_x = size;
	__size_y = size;
}

/**
Set the symbol size in the X direction.
@param size_x Symbol size in the X direction.
*/
public void setSizeX ( double size_x )
{	__size_x = size_x;
}

/**
Set the symbol size in the Y direction.
@param size_y Symbol size in the Y direction.
*/
public void setSizeY ( double size_y )
{	__size_y = size_y;
}

/**
Set the symbol style.
@param style For point symbols, see SYM_*.
*/
public void setStyle ( int style )
{	_style = style;
}

/**
Set the transparency (255 = completely transparent, 0 = opaque).
@param transparency the transparency.
*/
public void setTransparency ( int transparency )
{	if ( transparency < 0 ) {
		transparency = 0;
	}
	else if ( transparency > 255 ) {
		transparency = 255;
	}
	else {	__transparency = transparency;
	}
}

/**
Look up a symbol number given a name.  This is useful when the symbol names are
stored in a persistent way (avoid using numbers in config files because
symbol numbers may change).
@return the symbol number given the symbol name.
@exception Exception if a symbol cannot be matched.
*/
public static int toInteger ( String symbol_name )
throws Exception
{	if ( symbol_name == null ) {
		return SYM_NONE;
	}
	int length = SYMBOL_NAMES.length;
	for ( int i = 0; i < length; i++ ) {
		if ( symbol_name.equalsIgnoreCase(SYMBOL_NAMES[i]) ) {
			return SYMBOL_NUMBERS[i];
		}
	}
	throw new Exception ( "Cannot convert symbol \"" + symbol_name +
		"\" to integer value." );
}

/**
Return a string representation of the symbol.
@return a string representation of the symbol.
*/
public String toString ()
{	return new String ( "Type: " + _type + " Color: " + _color +
			" Color2: " + _color2 + " Size: " + __size_x + "," +
			__size_y );
}

/**
Look up a symbol name given a number.  This is useful when the symbol names are
stored in a persistent way (avoid using numbers in config files because
symbol numbers may change).
@param symbol_number the number of the symbol to look up.
@return the symbol name given the symbol number, or "None" if a matching
symbol cannot be found.
*/
public static String toString ( int symbol_number )
{	int length = SYMBOL_NUMBERS.length;
	for ( int i = 0; i < length; i++ ) {
		if ( symbol_number == SYMBOL_NUMBERS[i] ) {
			return SYMBOL_NAMES[i];
		}
	}
	return SYMBOL_NAMES[0];		// "None"
}

} // End of GRSymbol class
