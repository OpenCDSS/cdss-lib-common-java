// GRColorTable - class to store list of colors for a classification

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

import java.util.Vector;

/**
Class to store a color table.
This can be used, for example, to organize colors for a legend for a map layer.
This class uses a list of GRColor and internal arrays to associate the colors with numbers and data values.
*/
@SuppressWarnings("serial")
public class GRColorTable
extends Vector<GRColor>
implements Cloneable
{

// TODO smalers 2021-08-27 moved the following go GRColorRampType enumeration.
// Remove this code when tested out.
/**
Gray color table.
*/
//public static final int GRAY = 0;

/**
Blue to cyan color gradient.
*/
//public static final int BLUE_TO_CYAN = 1;

/**
Cyan to magenta color gradient.
*/
//public static final int BLUE_TO_MAGENTA = 2;

/**
Blue to red color gradient.
*/
//public static final int BLUE_TO_RED = 3;

/**
Cyan to yellow color gradient.
*/
//public static final int CYAN_TO_YELLOW = 4;

/**
Magenta to cyan color gradient.
*/
//public static final int MAGENTA_TO_CYAN = 5;

/**
Magenta to red color gradient.
*/
//public static final int MAGENTA_TO_RED = 6;

/**
Yellow to magenta color gradient.
*/
//public static final int YELLOW_TO_MAGENTA = 7;

/**
Yellow to red color gradient.
*/
//public static final int YELLOW_TO_RED = 8;

/**
Color table names.
*/
/*
public static String[] COLOR_TABLE_NAMES = {
    "Gray",
	"BlueToCyan",
	"BlueToMagenta",
	"BlueToRed",
	"CyanToYellow",
	"MagentaToCyan",
	"MagentaToRed",
	"YellowToMagenta",
	"YellowToRed" };
	*/

/**
Name for the color table.
Use this, for example if the color table is not one of the standard color tables.
*/
protected String _name = "";

/**
Create a color table.
*/
public GRColorTable () {
	super ( 1 );
}

/**
Create a color table.
@param size The size hint allows for optimal initial sizing of the GRColor list.  If a likely size is not know, specify 0.
*/
public GRColorTable ( int size ) {
	super ( size );
}

/**
Add a color to the color table.  The data value associated with the value is set to zero.
@param color Color to add to the color table.
*/
public void addColor ( GRColor color ) {
	add ( color );
}

/**
Clones the Object.
@return a Clone of the Object.
*/
public Object clone() {
	GRColorTable t = null;
	try {
		t = (GRColorTable)super.clone();
	}
	catch (Exception e) {
		return null;
	}

	return t;
}

/**
Create a color table using one of the standard GR named color tables.
Null is returned if the color table name does not match a known name.
See the overloaded method for more information.
@param rampName Color ramp name (see GRColorRampType).
@param ncolors	Number of colors to be in color table.
@param rflag Indicates whether colors should be reversed (true) or left in the initial order (0)
(this feature makes it so only one versions of each standard table is defined).
@return a new GRColorTable for the table name, or null if unable to match the color table name.
*/
public static GRColorTable createColorTable ( String rampName, int ncolors, boolean rflag) {
	GRColorRampType rampType = GRColorRampType.valueOfIgnoreCase(rampName);
	if ( rampType != null ) {
		return createColorTable ( rampType, ncolors, rflag );
	}
	return null;
}

/**
Create a color table using one of the standard GR named color tables.
This method forms colors using RGB values as floating point numbers in the range 0.0 to 1.0.
The floating point values are then converted to GRColor instances.
For single color families (e.g., shades of blue), the table is centered on the color and is shaded on each side.
For multi-color families (e.g., several prime colors),
each section of the table is centered on a prime color, with shades on each side.
Colors returned for multi-color families are hard-coded for color requests less than the minimum for the table.
This ensures that roundoff error, etc., will not return bogus colors.
Do the hard-coding by filling in the end-colors first and then filling in the middle.
This ensures that a relatively nice gradation is used.
Additionally, in some cases the relationship (iend - ibeg) is zero.
In these cases, skip the next blend operation (only assign the main color in the section and go on to the next.
This generally occurs when the number of colors requested is &gt;5
but still low enough that integer math can result in some stretches of the table being ignored.
@param rampType Color ramp type be used.
@param ncolors	Number of colors to be in color table.
@param rflag Indicates whether colors should be reversed (true) or left
in the initial order (false) (this feature makes it so only one versions of each standard table is defined).
@return GRColorTable with number of requested colors or null if not able to create the color table.
*/
public static GRColorTable createColorTable ( GRColorRampType rampType, int ncolors, boolean rflag) {
	double drgb; // Color increment to be applied when varying shades.
	double r[] = null;
	double g[] = null;
	double b[] = null;
	int i, ibeg, iend;

	if ( ncolors == 0 ) {
		return null;
	}
	r = new double[ncolors];
	g = new double[ncolors];
	b = new double[ncolors];
	if  ( rampType == GRColorRampType.MAGENTA_TO_CYAN ) {
		// Magenta to blue to cyan.
		r[0] = 1.0;
		g[0] = 0.0;	// Magenta.
		b[0] = 1.0;
		ibeg = 1;
		iend = ncolors/2;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = 1; i < iend; i++ ) {
				r[i] = r[i - 1] - drgb;
				g[i] = 0.0;
				b[i] = 1.0;
			} // Blue.
		}
		ibeg = iend;
		iend = ncolors;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = ibeg; i < iend; i++ ) {
				b[i] = 1.0;
				r[i] = 0.0;
				g[i] = g[i - 1] + drgb;
			} // Cyan.
		}
	}
	else if ( rampType == GRColorRampType.BLUE_TO_CYAN ) {
		// Blue to cyan.
		r[0] = 0.0;
		g[0] = 0.0;	// Blue.
		b[0] = 1.0;
		ibeg = 1;
		iend = ncolors;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = ibeg; i < iend; i++ ) {
				b[i] = 1.0;
				r[i] = 0.0;
				g[i] = g[i - 1] + drgb;
			}	// Cyan.
		}
	}
	else if ( rampType == GRColorRampType.CYAN_TO_YELLOW ) {
		// Only green hues.
		r[0] = 0.0;
		g[0] = 1.0;	// Cyan.
		b[0] = 1.0;
		ibeg = 1;
		iend = ncolors/2;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = 1; i < iend; i++ ) {
				r[i] = 0.0;
				g[i] = 1.0;
				b[i] = b[i - 1] - drgb;
			} // Green.
		}
		ibeg = iend;
		iend = ncolors;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = ibeg; i < iend; i++ ) {
				r[i] = r[i - 1] + drgb;
				g[i] = 1.0;
				b[i] = 0.0;
			}	// Yellow.
		}
	}
	else if ( rampType == GRColorRampType.YELLOW_TO_RED ) {
		// Yellow to red.
		r[0] = 1.0;
		g[0] = 1.0;	// Yellow.
		b[0] = 0.0;
		ibeg = 1;
		iend = ncolors;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = 1; i < iend; i++ ) {
				r[i] = 1.0;
				g[i] = g[i - 1] - drgb;
				b[i] = 0.0;
			} // Red.
		}
	}
	else if ( rampType == GRColorRampType.YELLOW_TO_MAGENTA ) {
		// Only red hues.
		r[0] = 1.0;
		g[0] = 1.0;	// Yellow.
		b[0] = 0.0;
		ibeg = 1;
		iend = ncolors/2;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = 1; i < iend; i++ ) {
				r[i] = 1.0;
				g[i] = g[i - 1] - drgb;
				b[i] = 0.0;
			} // Red.
		}
		ibeg = iend;
		iend = ncolors;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = ibeg; i < iend; i++ ) {
				r[i] = 1.0;
				g[i] = 0.0;
				b[i] = b[i - 1] + drgb;
			}	// magenta
		}
	}
	else if ( rampType == GRColorRampType.BLUE_TO_RED ) {
		// No magenta, white, or black.
		//
		// Set the colors manually for requests for less than five colors.
		if ( (ncolors >= 1) && (ncolors <= 4) ) {
			// Blue.
			r[0] = 0.0;
			g[0] = 0.0;
			b[0] = 1.0;
		}
		if ( (ncolors >= 2) && (ncolors <= 4) ) {
			// add red...
			r[ncolors - 1] = 1.0;
			g[ncolors - 1] = 0.0;
			b[ncolors - 1] = 0.0;
		}
		if ( ncolors == 3 ) {
			// Add green.
			r[2] = 0.0;
			g[2] = 1.0;
			b[2] = 0.0;
		}
		else if ( ncolors == 4 ) {
			// Add cyan, yellow.
			r[2] = 0.0;	// No green.
			g[2] = 1.0;
			b[2] = 1.0;
			r[3] = 1.0;
			g[3] = 1.0;
			b[3] = 0.0;
		}
		if ( ncolors >= 5 ) {
			// Interpolate between 5 known colors (the number of colors to be used because some colors to not be well represented).
			r[0] = 0.0;
			g[0] = 0.0;
			b[0] = 1.0;		// First color always blue.
			ibeg = 1;
			iend = ncolors/4;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in cyan.
				r[ibeg] = 0.0;
				g[ibeg] = 1.0;
				b[ibeg] = 1.0;
			}	// Cyan.
			else {
				// Do some shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = g[i - 1] + drgb;
					b[i] = 1.0;
				} // Cyan.
			}
			ibeg = iend;
			iend = ncolors*2/4;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in green.
				r[ibeg] = 0.0;
				g[ibeg] = 1.0;
				b[ibeg] = 0.0;
			} // Green.
			else {
				// Do some shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = 1.0;
					b[i] = b[i - 1] - drgb;
				} // Green.
			}
			ibeg = iend;
			iend = ncolors*3/4;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in yellow.
				r[ibeg] = 1.0;
				g[ibeg] = 1.0;
				b[ibeg] = 0.0;
			} // Yellow.
			else {
				// Do some shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = r[i - 1] + drgb;
					g[i] = 1.0;
					b[i] = 0.0;
				} // Yellow.
			}
			ibeg = iend;
			iend = ncolors;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in red.
				r[ibeg] = 1.0;
				g[ibeg] = 1.0;
				b[ibeg] = 0.0;
			} // Red.
			else {
				// Do some shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 1.0;
					g[i] = g[i - 1] - drgb;
					b[i] = 0.0;
				} // Red.
			}
		}
	}
	else if ( rampType == GRColorRampType.MAGENTA_TO_RED ) {
		// Set the colors manually for requests for less than five colors.
		if ( (ncolors >= 1) && (ncolors <= 4) ) { // Magenta.
			r[0] = 1.0;
			g[0] = 0.0;
			b[0] = 1.0;
		}
		if ( (ncolors >= 2) && (ncolors <= 4) ) { // Red.
			r[ncolors - 1] = 1.0;
			g[ncolors - 1] = 0.0;
			b[ncolors - 1] = 0.0;
		}
		if ( ncolors == 3 ) { // Green.
			r[2] = 0.0;
			g[2] = 1.0;
			b[2] = 0.0;
		}
		else if ( ncolors == 4 ) { // Add cyan, yellow>
			r[2] = 0.0; // No green.
			g[2] = 1.0;
			b[2] = 1.0;
			r[3] = 1.0;
			g[3] = 1.0;
			b[3] = 0.0;
		}
		if ( ncolors >= 5 ) {
			r[0] = 1.0;
			g[0] = 0.0;
			b[0] = 1.0; // First color always magenta.
			ibeg = 1;
			iend= ncolors/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in blue.
				r[iend] = 0.0;
				g[iend] = 0.0;
				b[iend] = 1.0;
			}	// blue
			else {
				// Put in shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = r[i - 1] - drgb;
					g[i] = 0.0;
					b[i] = 1.0;
				}
			} // Blue.
			ibeg = iend;
			iend= ncolors*2/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in cyan.
				r[iend] = 0.0;
				g[iend] = 1.0;
				b[iend] = 1.0;
			} // Cyan.
			else {
				// Put in shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = g[i - 1] + drgb;
					b[i] = 1.0;
				} // Cyan.
			}
			ibeg = iend;
			iend = ncolors*3/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in green.
				r[iend] = 0.0;
				g[iend] = 1.0;
				b[iend] = 0.0;
			}	// green
			else {
				// Put in shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = 1.0;
					b[i] = b[i - 1] - drgb;
				} // Green.
			}
			ibeg = iend;
			iend = ncolors*4/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in yellow.
				r[iend] = 1.0;
				g[iend] = 1.0;
				b[iend] = 0.0;
			} // Yellow.
			else {
				// Put in shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = r[i - 1] + drgb;
					g[i] = 1.0;
					b[i] = 0.0;
				} // Yellow.
			}
			ibeg = iend;
			iend = ncolors;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in red.
				r[iend] = 1.0;
				g[iend] = 0.0;
				b[iend] = 0.0;
			} // Red.
			else {
				// Put in shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 1.0;
					g[i] = g[i - 1] - drgb;
					b[i] = 0.0;
				} // Red.
			}
		}
	}
	else if ( rampType == GRColorRampType.BLUE_TO_MAGENTA ) {
		// Set the colors manually for requests for less than five colors.
		if ( (ncolors >= 1) && (ncolors <= 4) ) { // Blue.
			r[0] = 0.0;
			g[0] = 0.0;
			b[0] = 1.0;
		}
		if ( (ncolors >= 2) && (ncolors <= 4) ) { // Add magenta.
			r[ncolors - 1] = 1.0;
			g[ncolors - 1] = 0.0;
			b[ncolors - 1] = 1.0;
		}
		if ( ncolors == 3 ) { // Add green.
			r[2] = 0.0;
			g[2] = 1.0;
			b[2] = 0.0;
		}
		else if ( ncolors == 4 ) { // Add cyan, yellow.
			r[2] = 0.0;	// No green.
			g[2] = 1.0;
			b[2] = 1.0;
			r[3] = 1.0;
			g[3] = 1.0;
			b[3] = 0.0;
		}
		if ( ncolors >= 5 ) {
			r[0] = 0.0;
			g[0] = 0.0;
			b[0] = 1.0;	// First color always blue.
			ibeg = 1;
			iend = ncolors/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in cyan.
				r[iend] = 0.0;
				g[iend] = 1.0;
				b[iend] = 1.0;
			} // Cyan.
			else {
				// Put in shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = g[i - 1] + drgb;
					b[i] = 1.0;
				}
			} // Cyan.
			ibeg = iend;
			iend = ncolors*2/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in green.
				r[iend] = 0.0;
				g[iend] = 1.0;
				b[iend] = 0.0;
			} // Green.
			else {
				// Put in shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = 1.0;
					b[i] = b[i - 1] - drgb;
				}
			} // Green.
			ibeg = iend;
			iend = ncolors*3/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in yellow.
				r[iend] = 1.0;
				g[iend] = 1.0;
				b[iend] = 0.0;
			} // Yellow.
			else {
				// Put in shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = r[i - 1] + drgb;
					g[i] = 1.0;
					b[i] = 0.0;
				}
			} // Yellow.
			ibeg = iend;
			iend = ncolors*4/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in red.
				r[iend] = 1.0;
				g[iend] = 0.0;
				b[iend] = 0.0;
			} // Red.
			else {
				// Put in shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 1.0;
					g[i] = g[i - 1] - drgb;
					b[i] = 0.0;
				}
			} // Red.
			ibeg = iend;
			iend = ncolors;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just stick in magenta.
				r[iend] = 1.0;
				g[iend] = 0.0;
				b[iend] = 1.0;
			} // Magenta.
			else {
				// Put in shades.
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 1.0;
					g[i] = 0.0;
					b[i] = b[i - 1] + drgb;
				} // Magenta.
			}
		}
	}
	else if ( rampType == GRColorRampType.GRAY ) {
		// Gray hues.
		r[0] = 0.0;
		g[0] = 0.0;
		b[0] = 0.0;
		drgb = 1.0/(ncolors - 1);
		for ( i = 1; i < ncolors; i++ ) {
			r[i] = r[i - 1] + drgb;
			g[i] = g[i - 1] + drgb;
			b[i] = b[i - 1] + drgb;
		}
	}
//	for ( i = 0; i < ncolors; i++ ) {
//		//Message.printDebug ( dl, "", "Color[" + i + "]=(" +
//		Message.printStatus ( 1, "", "Color[" + i + "]=(" +
//		r[i] + "," + g[i] + "," + b[i] + ")" );
//	}
	if ( rflag ) {
		// Reverse order of colors.
		double [] r2 = new double[ncolors];
		double [] g2 = new double[ncolors];
		double [] b2 = new double[ncolors];
		for ( i = 0; i < ncolors; i++ ) {
			r2[i] = r[ncolors - i - 1];
			g2[i] = g[ncolors - i - 1];
			b2[i] = b[ncolors - i - 1];
		}
		for ( i = 0; i < ncolors; i++ ) {
			r[i] = r2[i];
			g[i] = g2[i];
			b[i] = b2[i];
		}
		r2 = null;
		g2 = null;
		b2 = null;
	}
	// Now create new GRColor.
	GRColorTable table = new GRColorTable(ncolors);
	for ( i = 0; i < ncolors; i++ ) {
		// Check roundoff.
		if ( r[i] < 0.0 ) {
			r[i] = 0.0;
		}
		if ( g[i] < 0.0 ) {
			g[i] = 0.0;
		}
		if ( b[i] < 0.0 ) {
			b[i] = 0.0;
		}
		if ( r[i] > 1.0 ) {
			r[i] = 1.0;
		}
		if ( g[i] > 1.0 ) {
			g[i] = 1.0;
		}
		if ( b[i] > 1.0 ) {
			b[i] = 1.0;
		}
		table.addColor ( new GRColor(r[i], g[i], b[i]) );
	}
	table.setName ( rampType.toString() );
	return table;
}

/**
Get the color at an index.
*/
public GRColor get(int index) {
	return (GRColor)get(index);
}

/**
Return the color table name.
@return the color table name.
*/
public String getName () {
	return _name;
}

/**
Set the color table name.
@param name Color table name.
*/
public void setName ( String name ) {
	if ( name != null ) {
		_name = name;
	}
}

/**
Convert the color table integer to its string name.
@param color_table Internal color table number.
@return String name of color table or "Unknown" if not found.
*/
public static String toString ( GRColorRampType rampType ) {
	return rampType.toString();
}

}