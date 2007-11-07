// ----------------------------------------------------------------------------
// GRColorTable - class to store list of colors for a classification
// ----------------------------------------------------------------------------
// History:
//
// 2001-09-17	Steven A. Malers, RTi	Initial version to support GIS feature
//					classification.
// 2001-09-21	SAM, RTi		Change to extend from Vector.
// 2001-10-10	SAM, RTi		Add toString(int) and getName().
// 2001-12-02	SAM, RTi		Overload constructor to take no
//					arguments.
// 2004-10-27	J. Thomas Sapienza, RTi	Implements Cloneable.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GR;

import java.util.Vector;

/**
Class to store a color table.  This can be used, for example, to organize
colors for a legend for a map layer.  This class uses a Vector of GRColor and
internal arrays to associate the colors with numbers and data values.
REVISIT (JTS - 2006-05-23)
Examples of use
*/
public class GRColorTable 
extends Vector
implements Cloneable
{

/**
Gray color table.
*/
public static final int GRAY = 0;
/**
Blue to cyan color gradient.
*/
public static final int BLUE_TO_CYAN = 1;
/**
Cyan to magenta color gradient.
*/
public static final int BLUE_TO_MAGENTA = 2;
/**
Blue to red color gradient.
*/
public static final int BLUE_TO_RED = 3;
/**
Cyan to yellow color gradient.
*/
public static final int CYAN_TO_YELLOW = 4;
/**
Magenta to cyan color gradient.
*/
public static final int MAGENTA_TO_CYAN = 5;
/**
Magenta to red color gradient.
*/
public static final int MAGENTA_TO_RED = 6;
/**
Yellow to magenta color gradient.
*/
public static final int YELLOW_TO_MAGENTA = 7;
/**
Yellow to red color gradient.
*/
public static final int YELLOW_TO_RED = 8;

/**
Color table names.
*/
public static String[] COLOR_TABLE_NAMES = {	"Gray",
						"BlueToCyan",
						"BlueToMagenta",
						"BlueToRed",
						"CyanToYellow",
						"MagentaToCyan",
						"MagentaToRed",
						"YellowToMagenta",
						"YellowToRed" };

/**
Name for the color table.  Use this, for example if the color table is
not one of the standard color tables.
*/
protected String _name = "";

/**
Create a color table.
*/
public GRColorTable ()
{	super ( 1 );
}

/**
Create a color table.
@param size The size hint allows for optimal initial sizing of the
GRColor Vector.  If a likely size is not know, specify 0.
*/
public GRColorTable ( int size )
{	super ( size );
}

/**
Add a color to the color table.  The data value associated with the value is
set to zero.
@param color Color to add to the color table.
*/
public void addColor ( GRColor color )
{	addElement ( color );
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
Create a color table using one of the stanard GR named color tables.
Null is returned if the color table name does not match a known name.
See the overloaded method for more information.
@param table_name Color table name (see COLOR_TABLE_NAME.*).
@param ncolors	Number of colors to be in color table.
@param rflag Indicates whether colors should be reversed (true) or left
in the initial order (0) (this feature makes it so only one versions of each
standard table is defined).
@return a new GRColorTable for the table name, or null if unable to match
the color table name.
*/
public static GRColorTable createColorTable (	String table_name, int ncolors,
						boolean rflag)
{	for ( int i = 0; i < COLOR_TABLE_NAMES.length; i++ ) {
		if ( COLOR_TABLE_NAMES[i].equalsIgnoreCase(table_name) ) {
			return createColorTable ( i, ncolors, rflag );
		}
	}
	return null;
}

/**
Create a color table using one of the standard GR named color tables.
This method forms colors using RGB values as floating point numbers in the range
0.0 to 1.0.  The floating point values are then converted to GRColor instances.
For single color families (e.g., shades of blue), the table is centered on the
color and is shaded on each side.  For multi-color families (e.g., several prime
colors), each section of the table is centered on a prime color, with shades
on each side.
Colors returned for multi-color families are hard-coded for color requests less
than the minimum for the table.  This ensures that roundoff error, etc., will
not return bogus colors.  Do the hard-coding by filling in the end-colors first
and then filling in the middle.  This ensures that a relatively nice gradation
is used.
Additionally, in some cases the relationship (iend - ibeg) is zero.  In these
cases, skip the next blend operation (only assign the main color in the section
and go on to the next.  This generally occurs when the number of colors
requested is &gt;5 but still low enough that integer math can result in some
stretches of the table being ignored.
@param table_num Color table to be used (e.g., GRColorTable.GRAY).
@param ncolors	Number of colors to be in color table.
@param rflag Indicates whether colors should be reversed (true) or left
in the initial order (0) (this feature makes it so only one versions of each
standard table is defined).
@return GRColorTable with number of requested colors or null if not able to
create the color table.
*/
public static GRColorTable createColorTable (	int table_num, int ncolors,
						boolean rflag)
{	double		drgb,		// Color increment to be applied when
					// varying shades.
			r[] = null,
			g[] = null,
			b[] = null;
	int		i, ibeg, iend;

	if ( ncolors == 0 ) {
		return null;
	}
	r = new double[ncolors];
	g = new double[ncolors];
	b = new double[ncolors];
	if  ( table_num == MAGENTA_TO_CYAN ) {
		// Magenta to blue to cyan...
		r[0] = 1.0;
		g[0] = 0.0;	// magenta
		b[0] = 1.0;
		ibeg = 1;
		iend = ncolors/2;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = 1; i < iend; i++ ) {
				r[i] = r[i - 1] - drgb;
				g[i] = 0.0;
				b[i] = 1.0;
			} // blue
		}
		ibeg = iend;
		iend = ncolors;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = ibeg; i < iend; i++ ) {
				b[i] = 1.0;
				r[i] = 0.0;
				g[i] = g[i - 1] + drgb;
			} // cyan
		}
	}
	else if ( table_num == BLUE_TO_CYAN ) {
		// blue to cyan
		r[0] = 0.0;
		g[0] = 0.0;	// blue
		b[0] = 1.0;
		ibeg = 1;
		iend = ncolors;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = ibeg; i < iend; i++ ) {
				b[i] = 1.0;
				r[i] = 0.0;
				g[i] = g[i - 1] + drgb;
			}	// cyan
		}
	}
	else if ( table_num == CYAN_TO_YELLOW ) {
		// Only green hues...
		r[0] = 0.0;
		g[0] = 1.0;	// cyan
		b[0] = 1.0;
		ibeg = 1;
		iend = ncolors/2;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = 1; i < iend; i++ ) {
				r[i] = 0.0;
				g[i] = 1.0;
				b[i] = b[i - 1] - drgb;
			}	// green
		}
		ibeg = iend;
		iend = ncolors;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = ibeg; i < iend; i++ ) {
				r[i] = r[i - 1] + drgb;
				g[i] = 1.0;
				b[i] = 0.0;
			}	// yellow
		}
	}
	else if ( table_num == YELLOW_TO_RED ) {
		// yellow to red...
		r[0] = 1.0;
		g[0] = 1.0;	// yellow
		b[0] = 0.0;
		ibeg = 1;
		iend = ncolors;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = 1; i < iend; i++ ) {
				r[i] = 1.0;
				g[i] = g[i - 1] - drgb;
				b[i] = 0.0;
			}	// red
		}
	}
	else if ( table_num == YELLOW_TO_MAGENTA ) {
		// Only red hues
		r[0] = 1.0;
		g[0] = 1.0;	// yellow
		b[0] = 0.0;
		ibeg = 1;
		iend = ncolors/2;
		if ( iend > ibeg ) {
			drgb = 1.0/(iend - ibeg);
			for ( i = 1; i < iend; i++ ) {
				r[i] = 1.0;
				g[i] = g[i - 1] - drgb;
				b[i] = 0.0;
			}	// red
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
	else if ( table_num == BLUE_TO_RED ) {
		// No magenta, white, or black
		//
		// Set the colors manually for requests for less than
		// five colors...
		if (	(ncolors >= 1) && (ncolors <= 4) ) {
			// blue
			r[0] = 0.0;
			g[0] = 0.0;
			b[0] = 1.0;
		}
		if (	(ncolors >= 2) && (ncolors <= 4) ) {
			// add red...
			r[ncolors - 1] = 1.0;
			g[ncolors - 1] = 0.0;
			b[ncolors - 1] = 0.0;
		}
		if ( ncolors == 3 ) {
			// add green...
			r[2] = 0.0;
			g[2] = 1.0;
			b[2] = 0.0;
		}
		else if ( ncolors == 4 ) {
			// add cyan, yellow
			r[2] = 0.0;	// no green
			g[2] = 1.0;
			b[2] = 1.0;
			r[3] = 1.0;
			g[3] = 1.0;
			b[3] = 0.0;
		}
		if ( ncolors >= 5 ) {
			// Interpolate between 5 known colors (the number of
			// colors to be used because some colors to not be
			// well represented...
			r[0] = 0.0;
			g[0] = 0.0;
			b[0] = 1.0;		// First color always blue
			ibeg = 1;
			iend = ncolors/4;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in cyan...
				r[ibeg] = 0.0;
				g[ibeg] = 1.0;
				b[ibeg] = 1.0;
			}	// cyan
			else {	// Do some shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = g[i - 1] + drgb;
					b[i] = 1.0;
				}	// cyan
			}
			ibeg = iend;
			iend = ncolors*2/4;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in green...
				r[ibeg] = 0.0;
				g[ibeg] = 1.0;
				b[ibeg] = 0.0;
			}	// green
			else {	// Do some shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = 1.0;
					b[i] = b[i - 1] - drgb;
				}	// green
			}
			ibeg = iend;
			iend = ncolors*3/4;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in yellow...
				r[ibeg] = 1.0;
				g[ibeg] = 1.0;
				b[ibeg] = 0.0;
			}	// yellow
			else {	// Do some shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = r[i - 1] + drgb;
					g[i] = 1.0;
					b[i] = 0.0;
				}	// yellow
			}
			ibeg = iend;
			iend = ncolors;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in red...
				r[ibeg] = 1.0;
				g[ibeg] = 1.0;
				b[ibeg] = 0.0;
			}	// red
			else {	// Do some shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 1.0;
					g[i] = g[i - 1] - drgb;
					b[i] = 0.0;
				}	// red
			}
		}
	}
	else if ( table_num == MAGENTA_TO_RED ) {
		// Set the colors manually for requests for less than
		// five colors...
		if (	(ncolors >= 1) && (ncolors <= 4) ) {	// magenta
			r[0] = 1.0;
			g[0] = 0.0;
			b[0] = 1.0;
		}
		if (	(ncolors >= 2) && (ncolors <= 4) ) {	// red
			r[ncolors - 1] = 1.0;
			g[ncolors - 1] = 0.0;
			b[ncolors - 1] = 0.0;
		}
		if ( ncolors == 3 ) {		// green
			r[2] = 0.0;
			g[2] = 1.0;
			b[2] = 0.0;
		}
		else if ( ncolors == 4 ) {	// add cyan, yellow
			r[2] = 0.0;		// no green
			g[2] = 1.0;
			b[2] = 1.0;
			r[3] = 1.0;
			g[3] = 1.0;
			b[3] = 0.0;
		}
		if ( ncolors >= 5 ) {
			r[0] = 1.0;
			g[0] = 0.0;
			b[0] = 1.0;		// First color always magenta
			ibeg = 1;
			iend= ncolors/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in blue...
				r[iend] = 0.0;
				g[iend] = 0.0;
				b[iend] = 1.0;
			}	// blue
			else {	// Put in shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = r[i - 1] - drgb;
					g[i] = 0.0;
					b[i] = 1.0;
				}
			}	// blue
			ibeg = iend;
			iend= ncolors*2/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in cyan...
				r[iend] = 0.0;
				g[iend] = 1.0;
				b[iend] = 1.0;
			}	// cyan
			else {	// Put in shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = g[i - 1] + drgb;
					b[i] = 1.0;
				}	// cyan
			}
			ibeg = iend;
			iend = ncolors*3/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in green...
				r[iend] = 0.0;
				g[iend] = 1.0;
				b[iend] = 0.0;
			}	// green
			else {	// Put in shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = 1.0;
					b[i] = b[i - 1] - drgb;
				}	// green
			}
			ibeg = iend;
			iend = ncolors*4/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in yellow...
				r[iend] = 1.0;
				g[iend] = 1.0;
				b[iend] = 0.0;
			}	// yellow
			else {	// Put in shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = r[i - 1] + drgb;
					g[i] = 1.0;
					b[i] = 0.0;
				}	// yellow
			}
			ibeg = iend;
			iend = ncolors;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in red...
				r[iend] = 1.0;
				g[iend] = 0.0;
				b[iend] = 0.0;
			}	// red
			else {	// Put in shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 1.0;
					g[i] = g[i - 1] - drgb;
					b[i] = 0.0;
				}	// red
			}
		}
	}
	else if ( table_num == BLUE_TO_MAGENTA ) {
		// Set the colors manually for requests for less than
		// five colors...
		if (	(ncolors >= 1) && (ncolors <= 4) ) {	// blue
			r[0] = 0.0;
			g[0] = 0.0;
			b[0] = 1.0;
		}
		if (	(ncolors >= 2) && (ncolors <= 4) ) {	// add magenta
			r[ncolors - 1] = 1.0;
			g[ncolors - 1] = 0.0;
			b[ncolors - 1] = 1.0;
		}
		if ( ncolors == 3 ) {	// add green
			r[2] = 0.0;
			g[2] = 1.0;
			b[2] = 0.0;
		}
		else if ( ncolors == 4 ) {	// add cyan, yellow
			r[2] = 0.0;	// no green
			g[2] = 1.0;
			b[2] = 1.0;
			r[3] = 1.0;
			g[3] = 1.0;
			b[3] = 0.0;
		}
		if ( ncolors >= 5 ) {
			r[0] = 0.0;
			g[0] = 0.0;
			b[0] = 1.0;	// First color always blue
			ibeg     = 1;
			iend	 = ncolors/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in cyan...
				r[iend] = 0.0;
				g[iend] = 1.0;
				b[iend] = 1.0;
			}	// cyan
			else {	// Put in shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = g[i - 1] + drgb;
					b[i] = 1.0;
				}
			}	// cyan
			ibeg = iend;
			iend = ncolors*2/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in green...
				r[iend] = 0.0;
				g[iend] = 1.0;
				b[iend] = 0.0;
			}	// green
			else {	// Put in shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 0.0;
					g[i] = 1.0;
					b[i] = b[i - 1] - drgb;
				}
			}	// green
			ibeg = iend;
			iend = ncolors*3/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in yellow...
				r[iend] = 1.0;
				g[iend] = 1.0;
				b[iend] = 0.0;
			}	// yellow
			else {	// Put in shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = r[i - 1] + drgb;
					g[i] = 1.0;
					b[i] = 0.0;
				}
			}	// yellow
			ibeg = iend;
			iend = ncolors*4/5;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in red...
				r[iend] = 1.0;
				g[iend] = 0.0;
				b[iend] = 0.0;
			}	// red
			else {	// Put in shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 1.0;
					g[i] = g[i - 1] - drgb;
					b[i] = 0.0;
				}
			}	// red
			ibeg = iend;
			iend = ncolors;
			if ( (iend - ibeg) == 0 ) {
				// Not enough colors to put shades so just
				// stick in magenta...
				r[iend] = 1.0;
				g[iend] = 0.0;
				b[iend] = 1.0;
			}	// magenta
			else {	// Put in shades...
				drgb = 1.0/(iend - ibeg);
				for ( i = ibeg; i < iend; i++ ) {
					r[i] = 1.0;
					g[i] = 0.0;
					b[i] = b[i - 1] + drgb;
				}	// magenta
			}
		}
	}
	else if ( table_num == GRAY ) {
		// Gray hues...
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
		// Reverse order of colors...
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
	// Now create new GRColor...
	GRColorTable table = new GRColorTable(ncolors);
	for ( i = 0; i < ncolors; i++ ) {
		// Check roundoff...
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
	table.setName ( COLOR_TABLE_NAMES[table_num] );
	r = null;
	g = null;
	b = null;
	return table;
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	_name = null;
	super.finalize();
}

/**
Return the color table name.
@return the color table name.
*/
public String getName ()
{	return _name;
}

/**
Set the color table name.
@param name Color table name.
*/
public void setName ( String name )
{	if ( name != null ) {
		_name = name;
	}
}

/**
Convert the color table integer to its string name.
@param color_table Internal color table number.
@return String name of color table or "Unknown" if not found.
*/
public static String toString ( int color_table )
{	if ( (color_table >= GRAY) && (color_table <= YELLOW_TO_RED) ) {
		return COLOR_TABLE_NAMES[color_table];
	}
	return "Unknown";
}

} // End of GRColorTable
