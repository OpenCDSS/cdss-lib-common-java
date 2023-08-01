// GRSymbolShapeType - enumeration of symbol shape types

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

/**
Symbol shape types, for example "Circle" and "Square".
This are not mostly listed alphabetically.
However, some newer shapes such as DIAMOND and after are added at the end so as to not
disrupt historical order in applications.
Some of of the data such as number are retained from the legacy code to facilitate transition and may be phased out.
*/
public enum GRSymbolShapeType {
	/**
	Simple line symbol: no symbol</b>
	Previously NONE.
	*/
	NONE ( 0, "None" ),

	// Start outline/line/hollow symbols.

	/**
	Simple line symbol: <b>circle</b>
	Previously CIR.
	The first symbol that can be used in an application should have a number of 1.
	This is the first "nice" symbol that is typically used in an application (legacy SYM_FIRST).
	This is the first "nice" symbol that can be an outline (legacy SYM_FIRST_OUT, meaning "outline").
	This is the first "nice" symbol that consists of line work (legacy SYM_FIRST_LINE, meaning "line work").
	*/
	CIRCLE ( 1, "Circle-Hollow" ),

	/**
	Simple line symbol: <b>square</b>
	Previously SQ.
	*/
	SQUARE ( 2, "Square-Hollow" ),

	/**
	Simple line symbol: <b>triangle pointing up</b>
	Previously UTRI.
	*/
	TRIANGLE_UP ( 3, "Triangle-Up-Hollow" ),

	/**
	Simple line symbol: <b>triangle pointing down</b>
	Previously DTRI.
	*/
	TRIANGLE_DOWN ( 4, "Triangle-Down-Hollow" ),

	/**
	Simple line symbol: <b>triangle pointing left</b>
	Previously LTRI.
	*/
	TRIANGLE_LEFT ( 5, "Triangle-Left-Hollow" ),

	/**
	Simple line symbol: <b>triangle pointing right</b>
	Previously RTRI.
	*/
	TRIANGLE_RIGHT ( 6, "Triangle-Right-Hollow" ),

	/**
	Simple line symbol: <b>diamond</b>
	Previously DIA.
	*/
	DIAMOND ( 7, "Diamond-Hollow" ),
	
	/**
	Simple line symbol: <b>horizontal ellipse</b>
	*/
	ELLIPSE_HORIZONTAL ( 8, "Ellipse-Horizontal-Hollow" ),

	/**
	Simple line symbol: <b>vertical ellipse</b>
	*/
	ELLIPSE_VERTICAL ( 9, "Ellipse-Vertical-Hollow" ),

	/**
	Simple line symbol: <b>horizontal rectangle</b>
	*/
	RECTANGLE_HORIZONTAL ( 10, "Rectangle-Horizontal-Hollow" ),

	/**
	Simple line symbol: <b>vertical rectangle</b>
	This is the last "nice" symbol that can be an outline (legacy SYM_LAST_OUT, meaning "outline").
	*/
	RECTANGLE_VERTICAL ( 11, "Rectangle-Vertical-Hollow" ),

	/**
	Compound line symbol: <b>+</b>
	Previously PLUS.
	*/
	PLUS ( 12, "Plus" ),

	/**
	Compound line symbol: <b>+ with square around it</b>
	Previously PLUSQ.
	*/
	PLUS_SQUARE ( 13, "Plus-Square" ),

	/**
	Compound line symbol: <b>X</b>
	Previously X.
	*/
	X ( 14, "X" ),

	/**
	Compound line symbol: <b>X with line over top and bottom</b>
	Previously EXCAP.
	*/
	X_CAP ( 15, "X-Cap" ),

	/**
	Compound line symbol: <b>X with lines on edge</b>
	Previously EXEDGE.
	*/
	X_EDGE ( 16, "X-Edge" ),

	/**
	Compound line symbol: <b>X with square around it</b>
	Previously EXSQ.
	*/
	X_SQUARE ( 17, "X-Square" ),

	/**
	Compound line symbol: <b>Right arrow</b>
	Previously RARR.
	*/
	ARROW_RIGHT ( 18, "Arrow-Right" ),

	/**
	Compound line symbol: <b>Left arrow</b>
	Previously LARR.
	*/
	ARROW_LEFT ( 19, "Arrow-Left" ),

	/**
	Compound line symbol: <b>Up arrow</b>
	Previously UARR.
	*/
	ARROW_UP ( 20, "Arrow-Up" ),

	/**
	Compound line symbol: <b>Down arrow</b>
	Previously DARR.
	*/
	ARROW_DOWN ( 21, "Arrow-Down" ),

	/**
	Compound line symbol: <b>Asterisk</b>
	Previously AST.
	*/
	ASTERISK ( 22, "Asterisk" ),

	/**
	Compound line symbol: <b>X with diamond around it</b>
	Previously EXDIA.
	This is the last "nice" symbol that consists of line work (legacy SYM_LAST_LINE, meaning "line work").
	*/
	X_DIAMOND ( 23, "X-Diamond"),

	// End outline/line/hollow symbols.

	// Start filled symbols.

	/**
	Simple filled symbol: <b>filled circle</b>
	Previously FCIR.
	First nice filled symbol (legacy SYM_FIRST_FILL).
	*/
	CIRCLE_FILLED ( 24, "Circle-Filled" ),

	/**
	Simple filled symbol: <b>filled square</b>
	Previously FSQ.
	*/
	SQUARE_FILLED ( 25, "Square-Filled" ),

	/**
	Simple filled symbol: <b>filled triangle pointing up</b>
	Previously FUTRI.
	*/
	TRIANGLE_UP_FILLED ( 26, "Triangle-Up-Filled" ),

	/**
	Simple filled symbol: <b>filled triangle pointing down</b>
	Previously FDTRI.
	*/
	TRIANGLE_DOWN_FILLED ( 27, "Triangle-Down-Filled" ),

	/**
	Simple filled symbol: <b>filled triangle pointing left</b>
	Previously FLTRI.
	*/
	TRIANGLE_LEFT_FILLED ( 28, "Triangle-Left-Filled" ),

	/**
	Simple filled symbol: <b>filled triangle pointing right</b>
	Previously FRTRI.
	*/
	TRIANGLE_RIGHT_FILLED ( 29, "Triangle-Right-Filled" ),

	/**
	Simple filled symbol: <b>filled diamond</b>
	Previously FDIA.
	*/
	DIAMOND_FILLED ( 30, "Diamond-Filled" ),

	/**
	Simple filled symbol: <b>filled horizontal ellipse</b>
	*/
	ELLIPSE_HORIZONTAL_FILLED ( 31, "Ellipse-Horizontal-Filled" ),

	/**
	Simple filled symbol: <b>filled vertical ellipse</b>
	*/
	ELLIPSE_VERTICAL_FILLED ( 32, "Ellipse-Vertical-Filled" ),

	/**
	Simple filled symbol: <b>filled horizontal rectangle</b>
	*/
	RECTANGLE_HORIZONTAL_FILLED ( 33, "Rectangle-Horizontal-Filled" ),

	/**
	Simple filled symbol: <b>filled vertical rectangle</b>
	Last "nice" filled symbol (legacy SYM_LAST_FILL).
	*/
	RECTANGLE_VERTICAL_FILLED ( 34, "Rectangle-Vertical-Filled" ),

	// End filled symbols.

	// Start complicated filled symbols.

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	nothing in other three number areas, with X.
	<pre>
	
	 /\      1
	/\/\
	\/\/   4   2
 	 \/      3
	</pre>
	*/
	EXDIA1 ( 35, "Ex-Diamond-Top-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "2" in right diamond,
	nothing in other three number areas.
	*/
	EXDIA2 ( 36, "Ex-Diamond-Right-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "3" in bottom diamond,
	nothing in other three number areas.
	*/
	EXDIA3 ( 37, "Ex-Diamond-Bottom-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "4" in bottom diamond,
	nothing in other three number areas.
	*/
	EXDIA4 ( 38, "Ex-Diamond-Left-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	"2" in right diamond, nothing in other two number areas.
	*/
	EXDIA12	( 39, "Ex-Diamond-TopRight-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	"2" in right diamond, "3" in bottom diamond, nothing in fourth diamond.
	*/
	EXDIA123 ( 40, "Ex-Diamond-TopRightBottom-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	"2" in right diamond, "3" in bottom diamond, "4" in left diamond.
	*/
	EXDIA1234 ( 41, "Ex-Diamond-TopRightBottomLeft-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	"3" in bottom diamond, nothing in other two number areas.
	*/
	EXDIA13	( 42, "Ex-Diamond-TopBottom-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	"4" in left diamond, nothing in other two number areas.
	*/
	EXDIA14	( 43, "Ex-Diamond-TopLeft-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "2" in right diamond,
	"3" in bottom diamond, nothing in other two number areas.
	*/
	EXDIA23	( 44, "Ex-Diamond-RightBottomFilled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "2" in right diamond,
	"4" in left diamond, nothing in other two number areas.
	*/
	EXDIA24	( 45, "Ex-Diamond-RightLeft-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "2" in right diamond,
	"3" in bottom diamond, "4" in left diamond, nothing in fourth number area.
	*/
	EXDIA234 ( 46, "Ex-Diamond-RightBottomLeft-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top number area,
	"2" in right diamond, "4" in left diamond, nothing in fourth number area.
	*/
	EXDIA124 ( 47, "Ex-Diamond-TopRightLeft-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top number area,
	"3" in bottom diamond, "4" in left diamond, nothing in 2nd diamond area.
	*/
	EXDIA134 ( 48, "Ex-Diamond-TopBottomLeft-Filled" ),

	/**
	Complicated filled symbol: <b>square filled on top</b>
	Previously TOPFSQ.
	*/
	SQUARE_TOP_FILLED ( 49, "Square-Top-Filled" ),

	/**
	Complicated filled symbol: <b>square filled on bottom</b>
	Previously BOTFSQ.
	*/
	SQUARE_BOTTOM_FILLED ( 50, "Square-Bottom-Filled" ),

	/**
	Complicated filled symbol: <b>square filled on right</b>
	Previously RFSQ.
	*/
	SQUARE_RIGHT_FILLED ( 51, "Square-Right-Filled" ),

	/**
	Complicated filled symbol: <b>square filled on left</b>
	Previously LFSQ.
	*/
	SQUARE_LEFT_FILLED ( 52, "Square-Left-Filled" ),

	/**
	Complicated filled symbol: <b>filled arrow to upper right</b>
	Previously FARR1.
	*/
	ARROW_UP_RIGHT_FILLED ( 53, "Arrow-Up-Right" ),

	/**
	Complicated filled symbol: <b>filled arrow to lower right</b>
	Previously FARR2.
	*/
	ARROW_DOWN_RIGHT_FILLED ( 54, "Arrow-Down-Right" ),

	/**
	Complicated filled symbol: <b>filled arrow to lower left</b>
	Previously FARR3.
	*/
	ARROW_DOWN_LEFT_FILLED ( 55, "Arrow-Down-Left" ),

	/**
	Complicated filled symbol: <b>filled arrow to upper left</b>
	Previously FARR4.
	This is the last "nice" symbol that may be used in an application (legacy SYM_LAST).
	*/
	ARROW_UP_LEFT_FILLED ( 56, "Arrow-Up-Left" ),

	/**
	Complicated filled symbol, used with State of Colorado: <b>instream flow symbol</b>
	Previously INSTREAM.
	*/
	INSTREAM_FLOW ( 57, "InstreamFlow" ),

	/**
	Complicated filled symbol, used to visualize reservoir storage: <b>tea cup symbol</b>
	Previously TEACUP.
	*/
	TEACUP ( 58, "TeaCup" ),

	/**
	Complicated filled symbol, used to visualize values on a map: <b>vertical bar where positive values are above
	center and negative values are below center</b>
	Previously VBARSIGNED.
	*/
	VERTICAL_BAR_SIGNED ( 59, "Vertical-Bar-Signed" ),

	// Building blocks (incomplete symbols).

	/**
	Building blocks (incomplete symbols): <b>minus (-)</b>
	Previously MIN.
	*/
	MINUS ( 60, "Minus" ),

	/**
	Building blocks (incomplete symbols): <b>bar (|)</b>
	Previously BAR.
	*/
	BAR	( 61, "Bar" ),

	/**
	Building blocks (incomplete symbols): <b>forward slash (/)</b>
	Previously FSLASH.
	*/
	SLASH_FORWARD ( 62, "Slash-Forward" ),

	/**
	Building blocks (incomplete symbols): <b>backslash (\)</b>
	Previously BSLASH.
	*/
	SLASH_BACKWARD ( 63, "Slash-Backward" ),

	/**
	Building blocks (incomplete symbols): <b>line on top</b>
	Previously TOPLINE.
	*/
	LINE_TOP ( 64, "Line-Top" ),

	/**
	Building blocks (incomplete symbols): <b>line on bottom</b>
	Previously BOTLINE.
	*/
	LINE_BOTTOM ( 65, "Line-Bottom" ),

	/**
	Building blocks (incomplete symbols): <b>line on right</b>
	Previously RLINE.
	*/
	LINE_RIGHT ( 66, "Line-Right" ),

	/**
	Building blocks (incomplete symbols): <b>line on left</b>
	Previously LLINE.
	*/
	LINE_LEFT ( 67, "Line-Left" ),

	/**
	Building blocks (incomplete symbols): <b>lines on top and bottom</b>
	Previously CAP.
	*/
	LINE_TOP_BOTTOM ( 68, "Line-Top-Bottom" ),

	/**
	Building blocks (incomplete symbols): <b>lines on left and right</b>
	Previously EDGE.
	*/
	LINE_LEFT_RIGHT ( 69, "Line-Left-Right" ),

	/**
	Building blocks (incomplete symbols): <b>caret (^)</b>
	Previously UCAR.
	*/
	CARET_UP ( 70, "Caret-Up" ),

	/**
	Building blocks (incomplete symbols): <b>down caret</b>
	Previously DCAR.
	*/
	CARET_DOWN ( 71, "Caret-Down" ),

	/**
	Building blocks (incomplete symbols): <b>left caret</b>
	Previously LCAR.
	*/
	CARET_LEFT ( 72, "Caret-Left" ),

	/**
	Building blocks (incomplete symbols): <b>right caret</b>
	Previously RCAR.
	*/
	CARET_RIGHT ( 73, "Caret-Right" ),

	/**
	Building blocks (incomplete symbols): <b>smaller X that can be placed inside of a diamond</b>
	Previously EXFORDIA.
	*/
	X_FOR_DIAMOND ( 74, "X-For-Diamond" ),

	/**
	Building blocks (incomplete symbols): <b>filled top quad of a diamond (no X)</b>
	*/
	FDIA1 ( 75, "Diamond-Top-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled right quad of a diamond (no X)</b>
	*/
	FDIA2 ( 76, "Diamond-Right-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled bottom quad of a diamond (no X)</b>
	*/
	FDIA3 ( 77, "Diamond-Bottom-Filled" ),
	
	/**
	Building blocks (incomplete symbols): <b>filled left quad of a diamond (no X)</b>
	*/
	FDIA4 ( 78, "Diamond-Left-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled upper right triangle in square, used to create arrows</b>
	Previously FSQTRI1.
	*/
	SQUARE_TRIANGLE_UPPER_RIGHT_FILLED ( 79, "Square-Triangle-Upper-Right-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled lower right triangle in square, used to create arrows</b>
	Previously FSQTRI2.
	*/
	SQUARE_TRIANGLE_LOWER_RIGHT_FILLED ( 80, "Square-Triangle-Lower-Right-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled lower left triangle in square, used to create arrows</b>
	Previously FSQTRI3.
	*/
	SQUARE_TRIANGLE_LOWER_LEFT_FILLED ( 81, "Square-Triangle-Lower-Left-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled upper left triangle in square, used to create arrows</b>
	Previously FSQTRI4.
	*/
	SQUARE_TRIANGLE_UPPER_LEFT_FILLED ( 82, "Square-Triangle-Upper-Left-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled upper half of diamond</b>
	Previously FTOPDIA.
	*/
	DIAMOND_TOP_HALF_FILLED ( 83, "Diamond-Top-Half-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled bottom half of diamond</b>
	Previously FBOTDIA.
	*/
	DIAMOND_LOWER_HALF_FILLED ( 84, "Diamond-Lower-Half-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled right half of diamond</b>
	Previously FRDIA.
	*/
	DIAMOND_RIGHT_HALF_FILLED ( 85, "Diamond-Right-Half-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled left half of diamond</b>
	Previously FLDIA
	*/
	DIAMOND_LEFT_HALF_FILLED ( 86, "Diamond-Left-Half-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled upper 1/4 of diamond</b>
	Previously FTOPDIA4.
	*/
	FTOPDIA4 ( 87, "Diamond-Upper-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled bottom 1/4 of diamond</b>
	*/
	FBOTDIA4 ( 88, "Diamond-Bottom-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled right 1/4 of diamond</b>
	*/
	FRDIA4 ( 89, "Diamond-Right-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled left 1/4 of diamond</b>
	*/
	FLDIA4 ( 90, "Diamond-Left-Filled" ),

	/**
	Complicated filled symbol: <b>vertical bar where all values are positive</b>.
	The bar is centered on its X location and rises up from its Y location.
	Previously VBARUNSIGNED.
	*/
	VERTICAL_BAR_UNSIGNED ( 91, "Vertical-Bar-Unsigned" ),

	/**
	Compound line symbol: <b>+ with circle around it</b>
	Previously PLUSCIR.
	*/
	CIRCLE_PLUS ( 92, "Circle-Plus" ),

	/**
	Compound symbol: <b>Filled triangle pointing up with a line on the point.</b>
	Previously FUTRI_TOPLINE.
	*/
	TRIANGLE_UP_FILLED_TOP_LINE ( 93, "Triangle-Up-Filled-Topline" ),

	/**
	Compound symbol: <b>Filled triangle pointing down with a line on the point.</b>
	Previously FDTRI_BOTLINE.
	*/
	TRIANGLE_DOWN_FILLED_BOTTOM_LINE ( 94, "Triangle-Down-Filled-Botline" ),

	/**
	Compound symbol: <b>Hollow triangle pointing up with a line on the point.</b>
	Previously UTRI_TOPLINE.
	*/
	TRIANGLE_UP_TOP_LINE ( 95, "Triangle-Up-Hollow-Topline" ),

	/**
	Compound symbol: <b>Hollow triangle pointing down with a line on the point.</b>
	Previously DTRI_BOTLINE.
	*/
	TRIANGLE_DOWN_BOTTOM_LINE ( 96, "Triangle-Down-Hollow-Botline" ),

	/**
	Complex symbol: <b>Filled push-pin, vertical (no lean).</b>
	Previously PUSHPIN_VERTICAL.
	*/
	PUSHPIN_VERTICAL ( 97, "Pushpin-Vertical" );

    /**
     * The string name that should be displayed.
     */
    private final String displayName;
    
    /**
     * Symbol number, used to process symbols in a sequence, iterations, etc.
     */
    //int number = 0;

    /**
     * Construct an enumeration value.
     * @param number the symbol number, used by internal code (should not be used publicly)
     * @param displayName name that should be displayed in choices, etc.
     */
    private GRSymbolShapeType(int number, String displayName) {
    	//this.number = number;
        this.displayName = displayName;
    }

    /**
     * Equals method to prevent common programming error of using the equals method instead of ==.
     */
    public boolean equals ( String lineStyleType ) {
        if ( lineStyleType.equalsIgnoreCase(this.displayName) ) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Get the number corresponding to the type.
     * @return the symbol number, used by internal code
     */
    /* TODO smalers 2023-07-31 historical use, but don't really need.
    public int getNumber () {
    	return this.number;
    }
    */

    /**
     * Return the display name for the symbol type.
     * This is usually the same as the value but using appropriate mixed case.
     * @return the display name.
     */
    @Override
    public String toString() {
        return this.displayName;
    }

	/**
	 * Return the enumeration value given a shape type number.
     * @param number the symbol number, used by internal code (should not be used publicly)
	 * @return the enumeration value given a shape type number, or null if not matched.
	 */
    /*
	public static GRSymbolShapeType valueOf(int number) {
	    // Currently supported values.
	    for ( GRSymbolShapeType t : values() ) {
	        if ( number == t.getNumber() ) {
	            return t;
	        }
	    }
	    return null;
	}
	*/

	/**
	 * Return the enumeration value given a string name (case-independent).
     * @param name the shape type name
	 * @return the enumeration value given a string name (case-independent), or null if not matched.
	 */
	public static GRSymbolShapeType valueOfIgnoreCase(String name) {
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values.
	    for ( GRSymbolShapeType t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    }
	    return null;
	}
}