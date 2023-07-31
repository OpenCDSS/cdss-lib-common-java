// GRSymbolType - enumeration of symbol types

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
Symbol types, for example "Square".
*/
public enum GRSymbolShapeType {
	/**
	Simple line symbol: no symbol</b>
	Previously NONE.
	*/
	NONE ( 0, "None" ),

	/**
	Simple line symbol: <b>circle</b>
	Previously CIR.
	The first symbol that can be used in an application should have a number of 1.
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
	Compound line symbol: <b>+</b>
	Previously PLUS.
	*/
	PLUS ( 8, "Plus" ),

	/**
	Compound line symbol: <b>+ with square around it</b>
	Previously PLUSQ.
	*/
	PLUS_SQUARE ( 9, "Plus-Square" ),

	/**
	Compound line symbol: <b>X</b>
	Previously X.
	*/
	X ( 10, "X" ),

	/**
	Compound line symbol: <b>X with line over top and bottom</b>
	Previously EXCAP.
	*/
	X_CAP ( 11, "X-Cap" ),

	/**
	Compound line symbol: <b>X with lines on edge</b>
	Previously EXEDGE.
	*/
	X_EDGE ( 12, "X-Edge" ),

	/**
	Compound line symbol: <b>X with square around it</b>
	Previously EXSQ.
	*/
	X_SQUARE ( 13, "X-Square" ),

	/**
	Compound line symbol: <b>Right arrow</b>
	Previously RARR.
	*/
	ARROW_RIGHT ( 14, "Arrow-Right" ),

	/**
	Compound line symbol: <b>Left arrow</b>
	Previously LARR.
	*/
	ARROW_LEFT ( 15, "Arrow-Left" ),

	/**
	Compound line symbol: <b>Up arrow</b>
	Previously UARR.
	*/
	ARROW_UP ( 16, "Arrow-Up" ),

	/**
	Compound line symbol: <b>Down arrow</b>
	Previously DARR.
	*/
	ARROW_DOWN ( 17, "Arrow-Down" ),

	/**
	Compound line symbol: <b>Asterisk</b>
	Previuosly AST.
	*/
	ASTERISK ( 18, "Asterisk" ),

	/**
	Compound line symbol: <b>X with diamond around it</b>
	Previously EXDIA.
	*/
	X_DIAMOND ( 19, "X-Diamond"),

	/**
	Simple filled symbol: <b>filled circle</b>
	Previously FCIR.
	*/
	CIRCLE_FILLED ( 20, "Circle-Filled" ),

	/**
	Simple filled symbol: <b>filled square</b>
	Previously FSQ.
	*/
	SQUARE_FILLED ( 21, "Square-Filled" ),

	/**
	Simple filled symbol: <b>filled triangle pointing up</b>
	Previously FUTRI.
	*/
	TRIANGLE_UP_FILLED ( 22, "Triangle-Up-Filled" ),

	/**
	Simple filled symbol: <b>filled triangle pointing down</b>
	Previously FDTRI.
	*/
	TRIANGLE_DOWN_FILLED ( 23, "Triangle-Down-Filled" ),

	/**
	Simple filled symbol: <b>filled triangle pointing left</b>
	Previously FLTRI.
	*/
	TRIANGLE_LEFT_FILLED ( 24, "Triangle-Left-Filled" ),

	/**
	Simple filled symbol: <b>filled triangle pointing right</b>
	Previously FRTRI.
	*/
	TRIANGLE_RIGHT_FILLED ( 25, "Triangle-Right-Filled" ),

	/**
	Simple filled symbol: <b>filled diamond</b>
	Previously FDIA.
	*/
	DIAMOND_FILLED ( 26, "Diamond-Filled" ),

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
	EXDIA1 ( 27, "Ex-Diamond-Top-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "2" in right diamond,
	nothing in other three number areas.
	*/
	EXDIA2 ( 28, "Ex-Diamond-Right-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "3" in bottom diamond,
	nothing in other three number areas.
	*/
	EXDIA3 ( 29, "Ex-Diamond-Bottom-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "4" in bottom diamond,
	nothing in other three number areas.
	*/
	EXDIA4 ( 30, "Ex-Diamond-Left-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	"2" in right diamond, nothing in other two number areas.
	*/
	EXDIA12	( 31, "Ex-Diamond-TopRight-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	"2" in right diamond, "3" in bottom diamond, nothing in fourth diamond.
	*/
	EXDIA123 ( 32, "Ex-Diamond-TopRightBottom-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	"2" in right diamond, "3" in bottom diamond, "4" in left diamond.
	*/
	EXDIA1234 ( 33, "Ex-Diamond-TopRightBottomLeft-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	"3" in bottom diamond, nothing in other two number areas.
	*/
	EXDIA13	( 34, "Ex-Diamond-TopBottom-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top diamond,
	"4" in left diamond, nothing in other two number areas.
	*/
	EXDIA14	( 35, "Ex-Diamond-TopLeft-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "2" in right diamond,
	"3" in bottom diamond, nothing in other two number areas.
	*/
	EXDIA23	( 36, "Ex-Diamond-RightBottomFilled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "2" in right diamond,
	"4" in left diamond, nothing in other two number areas.
	*/
	EXDIA24	( 37, "Ex-Diamond-RightLeft-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "2" in right diamond,
	"3" in bottom diamond, "4" in left diamond, nothing in fourth number area.
	*/
	EXDIA234 ( 38, "Ex-Diamond-RightBottomLeft-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top number area,
	"2" in right diamond, "4" in left diamond, nothing in fourth number area.
	*/
	EXDIA124 ( 39, "Ex-Diamond-TopRightLeft-Filled" ),

	/**
	Complicated filled symbol: Diamond made of four diamonds with "1" in top number area,
	"3" in bottom diamond, "4" in left diamond, nothing in 2nd diamond area.
	*/
	EXDIA134 ( 40, "Ex-Diamond-TopBottomLeft-Filled" ),

	/**
	Complicated filled symbol: <b>square filled on top</b>
	Previously TOPFSQ.
	*/
	SQUARE_TOP_FILLED ( 41, "Square-Top-Filled" ),

	/**
	Complicated filled symbol: <b>square filled on bottom</b>
	Previously BOTFSQ.
	*/
	SQUARE_BOTTOM_FILLED ( 42, "Square-Bottom-Filled" ),

	/**
	Complicated filled symbol: <b>square filled on right</b>
	Previously RFSQ.
	*/
	SQUARE_RIGHT_FILLED ( 43, "Square-Right-Filled" ),

	/**
	Complicated filled symbol: <b>square filled on left</b>
	Previously LFSQ.
	*/
	SQUARE_LEFT_FILLED ( 44, "Square-Left-Filled" ),

	/**
	Complicated filled symbol: <b>filled arrow to upper right</b>
	Previously FARR1.
	*/
	ARROW_UP_RIGHT_FILLED ( 45, "Arrow-Up-Right" ),

	/**
	Complicated filled symbol: <b>filled arrow to lower right</b>
	Previously FARR2.
	*/
	ARROW_DOWN_RIGHT_FILLED ( 46, "Arrow-Down-Right" ),

	/**
	Complicated filled symbol: <b>filled arrow to lower left</b>
	Previously FARR3.
	*/
	ARROW_DOWN_LEFT_FILLED ( 47, "Arrow-Down-Left" ),

	/**
	Complicated filled symbol: <b>filled arrow to upper left</b>
	Previously FARR4.
	*/
	ARROW_UP_LEFT_FILLED ( 48, "Arrow-Up-Left" ),

	/**
	Complicated filled symbol, used with State of Colorado: <b>instream flow symbol</b>
	Previously INSTREAM.
	*/
	INSTREAM_FLOW ( 49, "InstreamFlow" ),

	/**
	Complicated filled symbol, used to visualize reservoir storage: <b>tea cup symbol</b>
	Previously TEACUP.
	*/
	TEACUP ( 50, "TeaCup" ),

	/**
	Complicated filled symbol, used to visualize values on a map: <b>vertical bar where positive values are above
	center and negative values are below center</b>
	Previously VBARSIGNED.
	*/
	VERTICAL_BAR_SIGNED ( 51, "Vertical-Bar-Signed" ),

	// Building blocks (incomplete symbols).

	/**
	Building blocks (incomplete symbols): <b>minus (-)</b>
	Previously MIN.
	*/
	MINUS ( 52, "Minus" ),

	/**
	Building blocks (incomplete symbols): <b>bar (|)</b>
	Previously BAR.
	*/
	BAR	( 53, "Bar" ),

	/**
	Building blocks (incomplete symbols): <b>forward slash (/)</b>
	Previously FSLASH.
	*/
	SLASH_FORWARD ( 54, "Slash-Forward" ),

	/**
	Building blocks (incomplete symbols): <b>backslash (\)</b>
	Previously BSLASH.
	*/
	SLASH_BACKWARD ( 55, "Slash-Backward" ),

	/**
	Building blocks (incomplete symbols): <b>line on top</b>
	Previously TOPLINE.
	*/
	LINE_TOP ( 56, "Line-Top" ),

	/**
	Building blocks (incomplete symbols): <b>line on bottom</b>
	Previously BOTLINE.
	*/
	LINE_BOTTOM ( 57, "Line-Bottom" ),

	/**
	Building blocks (incomplete symbols): <b>line on right</b>
	Previously RLINE.
	*/
	LINE_RIGHT ( 58, "Line-Right" ),

	/**
	Building blocks (incomplete symbols): <b>line on left</b>
	Previously LLINE.
	*/
	LINE_LEFT ( 59, "Line-Left" ),

	/**
	Building blocks (incomplete symbols): <b>lines on top and bottom</b>
	Previously CAP.
	*/
	LINE_TOP_BOTTOM ( 60, "Line-Top-Bottom" ),

	/**
	Building blocks (incomplete symbols): <b>lines on left and right</b>
	Previously EDGE.
	*/
	LINE_LEFT_RIGHT ( 61, "Line-Left-Right" ),

	/**
	Building blocks (incomplete symbols): <b>caret (^)</b>
	Previously UCAR.
	*/
	CARET_UP ( 62, "Caret-Up" ),

	/**
	Building blocks (incomplete symbols): <b>down caret</b>
	Previously DCAR.
	*/
	CARET_DOWN ( 63, "Caret-Down" ),

	/**
	Building blocks (incomplete symbols): <b>left caret</b>
	Previously LCAR.
	*/
	CARET_LEFT ( 64, "Caret-Left" ),

	/**
	Building blocks (incomplete symbols): <b>right caret</b>
	Previously RCAR.
	*/
	CARET_RIGHT ( 65, "Caret-Right" ),

	/**
	Building blocks (incomplete symbols): <b>smaller X that can be placed inside of a diamond</b>
	Previously EXFORDIA.
	*/
	X_FOR_DIAMOND ( 66, "X-For-Diamond" ),

	/**
	Building blocks (incomplete symbols): <b>filled top quad of a diamond (no X)</b>
	*/
	FDIA1 ( 67, "Diamond-Top-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled right quad of a diamond (no X)</b>
	*/
	FDIA2 ( 68, "Diamond-Right-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled bottom quad of a diamond (no X)</b>
	*/
	FDIA3 ( 69, "Diamond-Bottom-Filled" ),
	
	/**
	Building blocks (incomplete symbols): <b>filled left quad of a diamond (no X)</b>
	*/
	FDIA4 ( 70, "Diamond-Left-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled upper right triangle in square, used to create arrows</b>
	Previously FSQTRI1.
	*/
	SQUARE_TRIANGLE_UPPER_RIGHT_FILLED ( 71, "Square-Triangle-Upper-Right-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled lower right triangle in square, used to create arrows</b>
	Previously FSQTRI2.
	*/
	SQUARE_TRIANGLE_LOWER_RIGHT_FILLED ( 72, "Square-Triangle-Lower-Right-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled lower left triangle in square, used to create arrows</b>
	Previously FSQTRI3.
	*/
	SQUARE_TRIANGLE_LOWER_LEFT_FILLED ( 73, "Square-Triangle-Lower-Left-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled upper left triangle in square, used to create arrows</b>
	Previously FSQTRI4.
	*/
	SQUARE_TRIANGLE_UPPER_LEFT_FILLED ( 74, "Square-Triangle-Upper-Left-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled upper half of diamond</b>
	Previously FTOPDIA.
	*/
	DIAMOND_TOP_HALF_FILLED ( 75, "Diamond-Top-Half-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled bottom half of diamond</b>
	Previously FBOTDIA.
	*/
	DIAMOND_LOWER_HALF_FILLED ( 76, "Diamond-Lower-Half-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled right half of diamond</b>
	Previously FRDIA.
	*/
	DIAMOND_RIGHT_HALF_FILLED ( 77, "Diamond-Right-Half-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled left half of diamond</b>
	Previously FLDIA
	*/
	DIAMOND_LEFT_HALF_FILLED ( 78, "Diamond-Left-Half-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled upper 1/4 of diamond</b>
	Previously FTOPDIA4.
	*/
	FTOPDIA4 ( 79, "Diamond-Upper-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled bottom 1/4 of diamond</b>
	*/
	FBOTDIA4 ( 80, "Diamond-Bottom-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled right 1/4 of diamond</b>
	*/
	FRDIA4 ( 81, "Diamond-Right-Filled" ),

	/**
	Building blocks (incomplete symbols): <b>filled left 1/4 of diamond</b>
	*/
	FLDIA4 ( 82, "Diamond-Left-Filled" ),

	/**
	Complicated filled symbol: <b>vertical bar where all values are positive</b>.
	The bar is centered on its X location and rises up from its Y location.
	Previously VBARUNSIGNED.
	*/
	VERTICAL_BAR_UNSIGNED ( 83, "Vertical-Bar-Unsigned" ),

	/**
	Compound line symbol: <b>+ with circle around it</b>
	Previously PLUSCIR.
	*/
	CIRCLE_PLUS ( 84, "Circle-Plus" ),

	/**
	Compound symbol: <b>Filled triangle pointing up with a line on the point.</b>
	Previously FUTRI_TOPLINE.
	*/
	TRIANGLE_UP_FILLED_TOP_LINE ( 84, "Triangle-Up-Filled-Topline" ),

	/**
	Compound symbol: <b>Filled triangle pointing down with a line on the point.</b>
	Previously FDTRI_BOTLINE.
	*/
	TRIANGLE_DOWN_FILLED_BOTTOM_LINE ( 86, "Triangle-Down-Filled-Botline" ),

	/**
	Compound symbol: <b>Hollow triangle pointing up with a line on the point.</b>
	Previously UTRI_TOPLINE.
	*/
	TRIANGLE_UP_TOP_LINE ( 87, "Triangle-Up-Hollow-Topline" ),

	/**
	Compound symbol: <b>Hollow triangle pointing down with a line on the point.</b>
	Previously DTRI_BOTLINE.
	*/
	TRIANGLE_DOWN_BOTTOM_LINE ( 88, "Triangle-Down-Hollow-Botline" ),

	/**
	Complex symbol: <b>Filled push-pin, vertical (no lean).</b>
	Previously PUSHPIN_VERTICAL.
	*/
	PUSHPIN_VERTICAL ( 89, "Pushpin-Vertical" );

    /**
     * The string name that should be displayed.
     */
    private final String displayName;
    
    /**
     * Symbol number, used to process symbols in a sequence, iterations, etc.
     */
    int number = 0;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private GRSymbolShapeType(int number, String displayName) {
    	this.number = number;
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
     */
    public int getNumber () {
    	return this.number;
    }

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
	 * @return the enumeration value given a shape type number, or null if not matched.
	 */
	public static GRSymbolShapeType valueOf(int number) {
	    // Currently supported values.
	    for ( GRSymbolShapeType t : values() ) {
	        if ( number == t.getNumber() ) {
	            return t;
	        }
	    }
	    return null;
	}

	/**
	 * Return the enumeration value given a string name (case-independent).
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