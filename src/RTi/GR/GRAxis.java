// GRAxis - class to store axis properties and provides axis utility functions

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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
This class stores axis properties and provides axis utility functions.
*/
public class GRAxis
{

/**
Linear axis.
*/
public static final int LINEAR = 1;
/**
Log 10 axis.
*/
public static final int LOG = 2;
/**
Standard normal probability axis.
*/
public static final int STANDARD_NORMAL_PROBABILITY = 3;

// TODO sam 2017-02-06 evaluate whether need to convert to enum.
/**
X axis.  Used by some utility methods.
*/
public static final int X = 1;
/**
Y axis.
*/
public static final int Y = 2;

//TODO sam 2017-02-06 evaluate whether need to convert to enum.
/**
Left axis.
*/
public static final int LEFT = 1;

/**
Right axis.
*/
public static final int RIGHT = 2;

/**
Bottom axis.
*/
public static final int BOTTOM = 3;

/**
Top axis.
*/
public static final int TOP = 4;

/**
Draw a box around a set of axes.
*/
public static final int GRID_BOX = 0x1;
/**
Use solid lines for the grid.
*/
public static final int GRID_SOLID = 0x2;
/**
Use dotted lines for the grid.
*/
public static final int GRID_DOTTED = 0x4;

/**
Draw a grid in a drawing area (for a 2D plot).
@param da Drawing area.
@param nxg Number of x-coordinates.
@param xg X-coordinates of grid lines.
@param nyg Number of y-coordinates.
@param yg Y-coordinates of grid lines.
@param flag Flag indicating characteristics of grid.
*/
public static void drawGrid ( GRDrawingArea da, int nxg, double [] xg, int nyg, double [] yg, int flag )
{	double [] x = new double[2];	// X-coordinates for drawing.
	double [] y = new double[2];	// Y-coordinates for drawing.

	if ( (flag & GRID_BOX) != 0 ) {
		// Draw a box around the grid...
		GRDrawingAreaUtil.drawRectangle ( da, xg[0], yg[0], (xg[nxg - 1] - xg[0]), (yg[nyg - 1] - yg[0]) );
	}
	if ( (flag & GRID_SOLID) != 0 ) {
		// Make sure that line is solid...
		GRDrawingAreaUtil.setLineDash ( da, 0, null, 0.0 );
	}
	else if ( (flag & GRID_DOTTED) != 0 ) {
		// Change line type to dashed...
		double [] dash = new double[2];
		dash[0]	= (xg[nxg - 1] - xg[0])/500.0;
		GRDrawingAreaUtil.setLineDash ( da, 2, dash, 0.0 );
	}
	for ( int i = 0; i < nxg; i++ ) {
		// Draw vertical lines...
		x[0] = xg[i];
		y[0] = yg[0];
		x[1] = x[0];
		y[1] = yg[nyg - 1];
		GRDrawingAreaUtil.drawLine ( da, x, y );
	}
	for ( int i = 0; i < nyg; i++ ) {
		// Draw horizontal lines...
		x[0] = xg[0];
		y[0] = yg[i];
		x[1] = xg[nxg - 1];
		y[1] = y[0];
		GRDrawingAreaUtil.drawLine ( da, x, y );
	}
	if ( (flag & GRID_DOTTED) != 0 ) {
		GRDrawingAreaUtil.setLineDash ( da, 0, null, 0.0 );	
		// set back to solid
	}
}

/**
Label an axis given a list of floating point numbers
@param da Drawing area.
@param nl Number of labels.
@param xl Coordinates of labels.
@param ref Reference coordinate for axis (in plane of axis).
@param axis Indicates which axis is being plotted (X or Y).
@param req_format Format for labels for the StringUtil.formatString() method (if null, %g is used).
If the label value is less than the precision, label with more precision.  Otherwise, for example,
the graph will have duplicate labels like "0", "0" for small numbers.
@param tflag Text attributes flag for labels (see GRText).
*/
public static void drawLabels (	GRDrawingArea da, int nl, double xl[], double ref, int axis, String req_format,
	int tflag )
{	int	dl = 30, i, iend,
		iplot; // Indicates which value to plot (depends on whether GRText.REVERSE_LABELS is specified).
	String default_format = "%g", format = null, routine = "GRAxis.labelAxis", string;
	int	tflag2;

	// Always assume that we are plotting in the following order:
	//
	// X	left to right
	// Y	bottom to top
	//
	// Regardless of whether the limits of the data are reversed (lower data
	// value at top of Y or right on X.  In other words, in this code we
	// are always assuming that we are plotting the axes from bottom to
	// top (Y) or left to right (X).  As for shifting the ends, let GRText handle that.

	iend = nl - 1;
	if ( (tflag & GRText.REVERSE_LABELS) != 0 ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Reversing labels array." );
		}
	}
	if ( (tflag & GRText.SHIFT_ENDS) != 0 ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Shifting ends." );
		}
	}
	for ( i = 0; i <= iend; i++ ) {
		if ( (tflag & GRText.REVERSE_LABELS) != 0 ) {
			// The labels that are coming in not specified from
			// bottom to top or left to right.  Therefore, need
			// to plot from the end of the array...
			iplot = iend - i;
		}
		else {
		    // The labels ARE specified from bottom to top or left to right...
			iplot = i;
		}
		// Figure out the format...
	    if ( req_format == null) {
	        format = default_format;
	    }
	    else if ( req_format.equals("") ) {
	        format = default_format;
	    }
	    else {
	        format = req_format;
	    }
		string = StringUtil.formatString(xl[iplot],format);
	    // If necessary, adjust the format in case the number is too small to be visible as formatted
		// For example, a label of .0015
		/* FIXME SAM 2010-11-29 Need to consider not only this value but adjoining to make sure that the
		 * label is unique.  This is tricky because adjoining labels should be different, but has the code
		 * been tested for the same values for all labels?
        int digits = (int)Math.log10(Math.abs((xl[iplot])));
        int pos = string.indexOf(".");
        */
		tflag2 = tflag;
		if ( (axis & X) != 0 ) {
			if ( (tflag & GRText.SHIFT_ENDS) != 0 ) {
				// Need to shift so that the lowest label has its bottom on the tic mark and the
				// topmost label has its top on the tic mark.
				// Assume that the first label is at the bottom.
				if ( (i == 0) || (i == iend) ) {
					// Clear the flags...
					if ( (tflag2 & GRText.CENTER_X) != 0 ) {
						tflag2 ^= GRText.CENTER_X;
					}
					if ( (tflag2 & GRText.RIGHT) != 0 ) {
						tflag2 ^= GRText.RIGHT;
					}
					if ( (tflag2 & GRText.LEFT) != 0 ) {
						tflag2 ^= GRText.LEFT;
					}
					if ( i == 0 ) {
						// Doing the leftmost label and we want the left edge to be against the tic...
						tflag2 |= GRText.LEFT;
					}
					else if ( i == iend ) {
						// Doing the rightmost label and we want the right edge to be against the tic...
						tflag2 |= GRText.RIGHT;
					}
				}
			}
			GRDrawingAreaUtil.drawText ( da, string, xl[iplot], ref, 0.0, tflag2 );
		}
		else if ( (axis & Y) != 0 ) {
			if ( ((tflag & GRText.SHIFT_ENDS) != 0) && ((i == 0) || (i == iend)) ) {
				// Need to shift so that the lowest label has its bottom on the tic mark and the
				// topmost label has its top on the tic mark...
				// Clear the flags as appropriate...
				if ( (tflag2 & GRText.TOP) != 0 ) {
					tflag2 ^= GRText.TOP;
				}
				if ( (tflag2 & GRText.CENTER_Y) != 0 ) {
					tflag2 ^= GRText.CENTER_Y;
				}
				if ( (tflag2 & GRText.BOTTOM) != 0 ) {
					tflag2 ^= GRText.BOTTOM;
				}
				if ( i == 0 ) {
					if ( xl[0] < xl[iend] ) {
						// This is the bottom label...
						tflag2 |= GRText.BOTTOM;
					}
					else {	// This is the top label...
						tflag2 |= GRText.TOP;
					}
				}
				else if ( i == iend ) {
					// This is the top label...
					if ( xl[iend] < xl[0] ) {
					// This is the bottom label...
						tflag2 |= GRText.BOTTOM;
					}
					else {
					    // This is the top label...
						tflag2 |= GRText.TOP;
					}
				}
			}
			GRDrawingAreaUtil.drawText ( da, string, ref, xl[iplot], 0.0, tflag2 );
		}
	}
}

/**
Draw tick marks on one axis.  This does not draw the grid.
It is assumed that the color has already been set.
@param da Drawing area.
@param axis GRAxis.X or GRAxis.Y
@param edge GRAxis.LEFT or GRAxis.RIGHT
@param ncoords Number of coordinates.
@param coords coordinates of ticks, in data units.
@param daFraction fraction of the drawing area for tick length, 0.0 if not used, -1.0 for auto (8 pixels).
@param lengthDev length of ticks in device units.
*/
public static void drawTicks ( GRDrawingArea da, int axis, int edge, int ncoords, double [] coords, double daFraction, double lengthDev )
{	double [] x = new double[2]; // X-coordinates for drawing.
	double [] y = new double[2]; // Y-coordinates for drawing.

	if ( axis == X ) {
		GRLimits datalim = da.getDataLimits();
		double ticLength = 0.0; // Default
		if ( (daFraction > -.000001) && (daFraction < .000001) ) {
			// Zero.  Use the device size
			GRLimits dalim = new GRLimits(lengthDev, 1.0);
			GRLimits datalim2 = GRDrawingAreaUtil.getDataExtents(da, dalim, 0);
			ticLength = datalim2.getHeight();
		}
		else {
			// Not zero.  Tick length is specified as fraction.
			if ( daFraction < 0.0 ) {
				// Automatic sizing - pick number of pixels of 8
				GRLimits dalim = new GRLimits(8.0, 8.0);
				GRLimits datalim2 = GRDrawingAreaUtil.getDataExtents(da, dalim, 0);
				ticLength = datalim2.getHeight();
			}
			else {
				// Use as is
				ticLength = datalim.getHeight()*daFraction;
			}
		}
		double bottom = 0.0;
		if ( edge == BOTTOM ) {
			bottom = datalim.getBottomY();
		}
		else if ( edge == TOP ) {
			bottom = datalim.getTopY() - ticLength;
		}
		y[0] = bottom;
		y[1] = bottom + ticLength;
		for ( int i = 0; i < ncoords; i++ ) {
			// Draw horizontal lines...
			x[0] = coords[i];
			x[1] = coords[i];
			GRDrawingAreaUtil.drawLine ( da, x, y );
		}
	}
	else if ( axis == Y ) {
		GRLimits datalim = da.getDataLimits();
		double ticLength = 0.0; // Default
		if ( (daFraction > -.000001) && (daFraction < .000001) ) {
			// Zero.  Use the device size
			GRLimits dalim = new GRLimits(lengthDev, 1.0);
			GRLimits datalim2 = GRDrawingAreaUtil.getDataExtents(da, dalim, 0);
			ticLength = datalim2.getWidth();
		}
		else {
			// Not zero.  Tick length is specified as fraction.
			if ( daFraction < 0.0 ) {
				// Automatic sizing - pick number of pixels of 8
				GRLimits dalim = new GRLimits(8.0, 8.0);
				GRLimits datalim2 = GRDrawingAreaUtil.getDataExtents(da, dalim, 0);
				ticLength = datalim2.getWidth();
			}
			else {
				// Use as is
				ticLength = datalim.getWidth()*daFraction;
			}
		}
		double left = 0.0;
		if ( edge == LEFT ) {
			left = datalim.getLeftX();
		}
		else if ( edge == RIGHT ) {
			left = datalim.getRightX() - ticLength;
		}
		x[0] = left;
		x[1] = left + ticLength;
		for ( int i = 0; i < ncoords; i++ ) {
			// Draw horizontal lines...
			y[0] = coords[i];
			y[1] = coords[i];
			GRDrawingAreaUtil.drawLine ( da, x, y );
		}
	}
}

/**
Find labels for an axis.
<ul>
<li>This method does no graphing or drawing.  It assumes
that this will be handled with other routines.</li>
<li>If "xmax" is bigger than "xmin", this routine calculates
the labels as if were the other way, and then reverses the order.
</li>
</ul>
@param xmin0 Minimum value in data.
@param xmax0 Maximum value in data.
@param include_end_points Indicates whether ends are to agree with minimum and
maximum data (true), or are they to extend past the data to
have the same interval as the rest of the data (false).
@param pflag Indicates the nearness (power) that labels should be to
data within the range (see findLimits).
@return label points for a graph given limits of data.
*/
public static double [] findLabels ( double xmin0, double xmax0, boolean include_end_points, int pflag )
{	int	i, nlabels0 = 100;	// Size of "xlabel" array
	boolean	rflag = false; // Indicates whether values need to be reversed afterwards
	double	incx,			// largest of "ixmax", "ixmin"
		lxmax, lxmin,		// "nice" label limits
		x,			// value for min-points (tic marks)
		xmax, xmin;
	String routine = "GRAxis.findLabels";

	if ( Message.isDebugOn ) {
		Message.printDebug ( 20, routine,
		"Trying to find labels for " + xmin0 + " " + xmax0 + " (pflag="+ pflag + ")");
	}

	// Allocate temporary work space...
	double [] xlabel = new double[nlabels0];

	int nlabels	= 0;		// Number of labels calculated

	if ( xmin0 == xmax0 ) {
		Message.printWarning ( 2, routine, "Xmax = Xmin! ( " + xmin0 + "0)" );
		return null;
	}
	else if ( xmin0 > xmax0 ) {
		// Will have to reverse later...
		rflag = true;
		xmin = xmax0;
		xmax = xmin0;
	}
	else {
	    xmin = xmin0;
		xmax = xmax0;
	}
	
	GRLimits limits = findLimits ( xmin, xmax, pflag );
	if ( limits == null ) {
		Message.printWarning ( 2, routine, "Unable to find nice data limits for " + xmin + "," + xmax );
		return null;
	}
	lxmin = limits.getLeftX();
	lxmax = limits.getRightX();
	incx = limits.getBottomY();

	if ( include_end_points ) {
		xlabel[nlabels++] = xmin;
	}
	else {
	    xlabel[nlabels++] = lxmin;
	}

	for ( x = (lxmin + incx); x < xmax; x += incx ) {
		// ticks...
		if ( nlabels == nlabels0 ) {
			// Need to add more to the work space...
			double [] temp_xlabel = new double[nlabels0];
			int temp_nlabels0 = nlabels0;
			for ( i = 0; i < temp_nlabels0; i++ ) {
				temp_xlabel[i] = xlabel[i];
			}
			// Double temporary space...
			nlabels0 *= 2;
			xlabel = new double[nlabels0];
			for ( i = 0; i < temp_nlabels0; i++ ) {
				xlabel[i] = temp_xlabel[i];
			}
			temp_xlabel = null;
		}
		xlabel[nlabels++] = x;
	}

	if ( nlabels == nlabels0 ) {
		// Need to add more to the work space...
		double [] temp_xlabel = new double[nlabels0];
		int temp_nlabels0 = nlabels0;
		for ( i = 0; i < temp_nlabels0; i++ ) {
			temp_xlabel[i] = xlabel[i];
		}
		// Need one more point...
		nlabels0 += 1;
		xlabel = new double[nlabels0];
		for ( i = 0; i < temp_nlabels0; i++ ) {
			xlabel[i] = temp_xlabel[i];
		}
		temp_xlabel = null;
	}

	// Sometimes an extra label will be inserted because the floating point
	// increments cause the loop to go one extra time (e.g., .999999999 and
	// 1.0000000).  Check the end point and if the last label is less than
	// .5 an increment over the previous label, assume the last one can
	// be thrown out.  The increment should always be positive here.
	if ( (nlabels > 2) && ((xlabel[nlabels - 1] - xlabel[nlabels - 2]) < incx/2.0) ) {
		--nlabels;
	}

	if ( include_end_points ) {
		// Maximum endpoint...
		xlabel[nlabels++] = xmax;
	}
	else {
	    xlabel[nlabels++] = lxmax;
	}

	// Resize the labels to exactly the right size...

	double [] temp_xlabel = new double[nlabels];
	for ( i = 0; i < nlabels; i++ ) {
		temp_xlabel[i] = xlabel[i];
	}
	xlabel = new double[nlabels];
	for ( i = 0; i < nlabels; i++ ) {
		xlabel[i] = temp_xlabel[i];
	}
	temp_xlabel = null;

	// Reverse the labels if requested...

	if ( rflag ) {
		temp_xlabel = new double[nlabels];
		for ( i = 0; i < nlabels; i++ ) {
			temp_xlabel[nlabels - i - 1] = xlabel[i];
		}
		for ( i = 0; i < nlabels; i++ ) {
			xlabel[i] = temp_xlabel[i];
		}
		temp_xlabel = null;
	}

	return xlabel;
}

/**
Returns limits for a graph given limits of data or null if there is a problem.
The value of "xmin" must be greater than "xmax".
@param xmin Minimum value in data.
@param xmax Maximum value in data.
@param pflag Indicates the nearness (power) that labels should be to data within the range:
<ul>
<li> pflag = 0 for same power of 10 (i.e. if data is in 100's, labels will be spaced using 100)</li>
<li> pflag = 1 for one power lower (i.e. if data is in 100's, labels will be spaced using 10)</li>
<li> pflag = -n use increment="increment_0*pflag/-100"</li>
</ul>
@return limits for a graph given limits of data or null if there is a problem.
*/
public static GRLimits findLimits ( double xmin, double xmax, int pflag )
{	int	i, pxmax = 0, pxmin = 0; // log10 powers of "xmax" and "xmin".
	double axmax, axmin, // Absolute values of "xmax" and "xmin".
		ixmax, ixmin, // Increments for determining "lxmax" and "lxmin".
		xtest; // Value to be tested against (includes an offset to guard
				// against incorrect answers due to machine precision).
	String routine = "GRAxis.findLimits";

	if ( xmin >= xmax ) {
		return null;
	}

	// Use the absolute values for limits determination...

	axmin = Math.abs ( xmin );
	axmax = Math.abs ( xmax );

	if ( axmin != 0.0 ) {
		pxmin = (int)Math.log10 ( axmin );
	}
	if ( axmax != 0.0 ) {
		pxmax = (int)Math.log10 ( axmax );
	}
	if ( axmin == 0.0 ) {
		pxmin = pxmax;
	}
	if ( axmax == 0.0 ) {
		pxmax = pxmin;
	}

	double lxmin = 0.0;
	double lxmax = 0.0;
	double incx	= 0.0;	// Largest of "ixmax", "ixmin".

	ixmin = Math.pow ( 10.0, (double)pxmin );	// Use largest
	ixmax = Math.pow ( 10.0, (double)pxmax );	// Increment from ends
	if ( ixmax > ixmin ) {
		incx = ixmax;
	}
	else {
	    incx = ixmin;
	}
	if ( pflag < 0 ) {
		incx *= (double)(pflag)/-100.0;
	}
	else {
	    for ( i = 0; i < pflag; i++ ) {
			incx /= 10.0;
		}
	}

	// Rather than starting limits search at zero, which can result in
	// long searches, start at a value determined using the increment.  The
	// resulting number should always be less than the value...

	lxmin = incx*(double)((int)(axmin/incx));
	lxmax = incx*(double)((int)(axmax/incx));

	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, "GRAxis.findLimits",
		"Look for nice lim using incx=" + incx + " lxmin=" + lxmin + " lxmax=" + lxmax + " pflag=" + pflag );
	}

	// If xmin or xmax = 0.0, then use that as the limit...

	if ( xmin > 0.0 ) {
		while ( lxmin < xmin ) {
			lxmin += incx;
		}
		lxmin -= incx;		// Need to back up one
	}
	else if ( xmin < 0.0 ) {
		while ( lxmin > xmin ) {
			lxmin -= incx;
		}
		// lxmin -= incx;		need to go one past */
	}
	if ( xmax > 0.0 ) {
		xtest = xmax;
		while ( lxmax < xtest ) {
			lxmax += incx;
		}
	}
	else if ( xmax < 0.0 ) {
		while ( lxmax > xmax ) {
			lxmax -= incx;
		}
		lxmax += incx;		// Need to back up one
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, routine, "For " + xmin + "," + xmax + ", nice limits are " +
		lxmin + "," + lxmax + " (pflag=" + pflag + ", incx=" + incx + ")" );
	}
	return new GRLimits ( lxmin, incx, lxmax, incx );
}

/**
Returns log label points for a graph given limits of data.  This routine 
does no graphing or drawing.  It assumes that this will be handled with 
other routines.  Log labels ALLWAYS consist of the inclusive major log
divisions, as well as minor log divisions within the major divisions.<p>
For example, if the data values to be plotted are .3 to 10.5, label 
values will be: .1, .2, .3, .4, .5, .6, .7, .8, .9, 1.0, 2.0, 3.0, 4.0, 5.0,
6.0, 7.0, 8.0, 9.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0.<p>
If "xmin" > "xmax", the limits will be reversed at the
beginning, and the results will be reversed at the end.
@param xmin0 Minimum value in data as passed to routine.
@param xmax0 Maximum value in data as passed to routine.
@return log label points for a graph.
*/
public static double [] findLogLabels ( double xmin0, double xmax0 )
{	int	i,		// Counter for major log divisions (powers of ten).
		j,		// Counter for minor log divisions within major decisions.
		nlabels0 = 100,	// Dimension of "xlabel".
		pxmax, pxmin;	// Powers of data limits.
	boolean	rflag = false;	// Indicates whether values need to be reversed.
	double powi,		// 10 to the power "i".
		xmax, xmin;	// Maximum value in data used for calculations.
	String routine = "findLogLabels";

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Trying to find log labels for " + xmin0 + "," + xmax0 );
	}

	int	nlabels	= 0;	// Number of labels calculated.
	double [] xlabel = new double[nlabels0]; // Array of labels.

	if ( (xmin0 == xmax0) || ((xmin0 < 0.0) && (xmax0 < 0.0)) ) {
		return null;
	}
	else if ( xmin0 > xmax0 ) {
		rflag = true;
		xmin = xmax0;
		xmax = xmin0;
	}
	else {
	    xmin = xmin0;
		xmax = xmax0;
	}

	if ( xmin <= 0.0 ) {
		xmin = 0.001;	// need to set this better??
	}
	pxmin = (int)Math.log10 ( xmin );
	pxmax = (int)Math.log10 ( xmax );

	if ( Math.pow((double)1.0,(double)(pxmin)) > xmin ) {
		// Make sure it bounds value...
		--pxmin;
	}
	if ( Math.pow((double)1.0,(double)(pxmin)) < xmax ) {
		// make sure it bounds value...
		++pxmax;
	}

	for ( i = pxmin; i <= pxmax; i++ ) {
		powi = Math.pow ( (double)10.0, (double)(i) );
		for ( j = 1; j < 10; j++ ) {
			if ( nlabels == nlabels0 ) {
				// Need to add more to the work space...
				double [] temp_xlabel = new double[nlabels0];
				int temp_nlabels0 = nlabels0;
				for ( i = 0; i < temp_nlabels0; i++ ) {
					temp_xlabel[i] = xlabel[i];
				}
				// Double temporary space...
				nlabels0 *= 2;
				xlabel = new double[nlabels0];
				for ( i = 0; i < temp_nlabels0; i++ ) {
					xlabel[i] = temp_xlabel[i];
				}
				temp_xlabel = null;
			}
			xlabel[nlabels++] = j*powi;
			if ( i == pxmax ) {
				break;	// Only need first one...
			}
		}
	}

	// Resize the labels to exactly the right size...

	double [] temp_xlabel = new double[nlabels];
	for ( i = 0; i < nlabels; i++ ) {
		temp_xlabel[i] = xlabel[i];
	}
	xlabel = new double[nlabels];
	for ( i = 0; i < nlabels; i++ ) {
		xlabel[i] = temp_xlabel[i];
	}
	temp_xlabel = null;

	if ( rflag ) {
		// Reverse the labels...
		temp_xlabel = new double[nlabels];
		for ( i = 0; i < nlabels; i++ ) {
			temp_xlabel[nlabels - i - 1] = xlabel[i];
		}
		for ( i = 0; i < nlabels; i++ ) {
			xlabel[i] = temp_xlabel[i];
		}
		temp_xlabel = null;
	}

	return xlabel;
}

/**
Returns labels for a set of data (# of labels is bounded).  The values of
"minl" and "maxl" specify the minimum and maximum acceptable number of labels.
The internal number representation is not optimized for the number of decimal points;
therefore, call formatLabels() if necessary.
@param xmin0 Minimum value in data.
@param xmax0 Maximum value in data.
@param include_end_points true if end-points are to be used as labels.
@param minl Minimum number of labels.
@param maxl Maximum number of labels.
@return labels for a set of data.
*/
public static double [] findNLabels ( double xmin0, double xmax0,
					boolean include_end_points, int minl, int maxl )
{	int		dl = 1,	// Debug level
			i, nlabels = 0,	// Dimension of "xlabel".
			npflag = 0,
			pflag[] = null;	// Nearness flag (see findLabels).
			// "Nearness" flag (see findLabels).
	String routine = "GRAxis.findNLabels";
	double xmax = 0.0, xmin = 0.0;	// Maximum & minimum data values
	double [] xlabel = null;		// Label values.

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Trying to find " + minl + "-" + maxl + " labels for " + xmin0 + "," + xmax0 );
	}

	if ( minl >= maxl ) {
		Message.printWarning ( 20, routine,
		"Min # of labels (" + minl + ") >= Max # of labels (" + maxl + ")" );
		return null;
	}

	// If the limits will cause problems, reset and continue on...

	if ( xmin0 == xmax0 ) {
		if ( xmin0 == 0.0 ) {
			// No good guess at the values so use 0 to 1...
			xmin = 0.0;
			xmax = 1.0;
			Message.printWarning ( 20, routine, "Both xmin and xmax == 0.  Setting to 0, 1 for labels");
		}
		else if ( xmin0 > 0.0 ) {
			// Reset so that we go from 0 to the maximum...
			xmin = 0.0;
			xmax = xmax0;
			Message.printWarning ( 20, routine, "xmin == xmax (" + xmin0 + ").  Setting xmin to 0 for labels" );
		}
		else if ( xmin0 < 0.0 ) {
			// Reset so that we go from the minimum to 0...
			xmin = xmin0;
			xmax = 0.0;
			Message.printWarning ( 20, routine, "xmin == xmax (" + xmin0 + ").  Setting xmax to 0 for labels" );
		}
	}
	else {
	    // Just use what we got...
		xmin = xmin0;
		xmax = xmax0;
	}

	// Call Marcio's algorithm (at bottom of this file)...

	boolean marcio = false;

// TODO (JTS - 2003-05-05) this code should be cleaned up at the very least.
// Comments by smalers related to the above:
// - label positioning is a "science" that can always use more attention
// - leave for now

	if ( marcio ) {
		GRLimits marcio_limits = chooseLabels ( xmin, xmax, minl, maxl );
		// Fill in the array...
		double newmin = marcio_limits.getLeftX();
		double newmax = marcio_limits.getRightX();
		double newinc = marcio_limits.getBottomY();
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "GRAxis.findNLabels",
			"Found nice min " + newmin + " max " + newmax + " increment " + newinc );
		}
		// Always add one on the endpoint...
		int size = (int)(((newmax - newmin)/newinc) + 1.01);
		xlabel = new double[size];
		for ( int ix = 0; ix < size; ix++ ) {
			xlabel[ix] = newmin + newinc*ix;
		}
		return xlabel;
	}

	// Determine the "pflag" information based on the ratio of the range
	// of values to the magnitude.  If the range is small compared to the
	// magnitude, then the pflags should cause smaller increments.  Use the
	// average value of the limits for the magnitude.

	double range = xmin - xmax;
	if ( range < 0.0 ) {
		range *= -1.0;
	}
	double average = (xmin + xmax)/2.0;
	if ( average < 0.0 ) {
		average *= -1.0;
	}
	double ratio = range/average;
	if ( ratio < 100.0 ) {
		// Range of numbers is generally less than the magnitudes so
		// want to use small increments...
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, "GRAxis.findNLabels", "range/average is low: " + ratio );
		}
		npflag = 16;
		pflag = new int[npflag];
		pflag[0] = 0;
		pflag[1] = 1;
		pflag[2] = -50;
		pflag[3] = -25;
		pflag[4] = -20;
		pflag[5] = -40;
		pflag[6] = -10;
		pflag[7] = -5;
		pflag[8] = -2;
		pflag[9] = -4;
		pflag[10] = -1;
		pflag[11] = -500;
		pflag[12] = -250;
		pflag[13] = -200;
		pflag[14] = -400;
		pflag[15] = -100;
	}
	else {
	    // Range of numbers is generally similar to magnitudes so use larger increments...
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, "GRAxis.findNLabels", "range/average is high: " + ratio );
		}
		npflag = 16;
		pflag = new int[npflag];
		pflag[0] = 0;
		pflag[1] = 1;
		pflag[2] = -50;
		pflag[3] = -25;
		pflag[4] = -20;
		pflag[5] = -40;
		pflag[6] = -10;
		pflag[7] = -5;
		pflag[8] = -2;
		pflag[9] = -4;
		pflag[10] = -1;
		pflag[11] = -500;
		pflag[12] = -250;
		pflag[13] = -200;
		pflag[14] = -400;
		pflag[15] = -100;
	} 

	// Loop from the mid-point out and try to find labels.  Therefore,
	// hopefully the number of labels found will be closer to the mid-point
	// than not.  Start with the requested mid-point...
	// SAM 2000-11-06 - just loop through all the options and use the one that returns the most labels...
	//int nlabels_i = nlabels_mid;
	int nlabels_i = maxl;
	// Break out below...
	while ( true ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, "GRAxis.findNLabels", "Trying to find " + nlabels_i + " labels" );
		}
		for ( i = 0; i < npflag; i++ ) {
			xlabel = findLabels(xmin, xmax, include_end_points, pflag[i]);
			if ( xlabel == null ) {
				Message.printWarning ( 20, routine, "Error getting labels" );
				continue;
			}
			nlabels = xlabel.length;
			if ( Message.isDebugOn ) {
				Message.printDebug ( 20, routine,
				"For x (" + xmin + "," + xmax + "), pflag = " + pflag[i] + ", nlabels = " + nlabels +
				", limits=(" + xlabel[0] + "," + xlabel[nlabels - 1] );
			}
			// New code.  We want to try to match the exact number...
			if ( nlabels == nlabels_i ) {
				// Found the requested number of labels...
				pflag = null;
				return xlabel;
			}
			// Old code...
			//if ( (nlabels >= minl) && (nlabels <= maxl) ) {
			//	pflag = null;
			//	return xlabel;
			//}
		}

		--nlabels_i;
		if ( nlabels_i < minl ) {
			break;
		}
/* Before 2000-11-06
TODO (JTS - 2003-05-05) can this chunk be removed? SAM: Not yet.

		// Now increment the requested number of labels...
		if ( nlabels_i < minl ) {
			min_checked = true;
		}
		if ( nlabels_i > maxl ) {
			max_checked = true;
		}
		if ( min_checked && max_checked ) {
			// Have evaluated all options so break;
			break;
		}
		if ( (trycount%2) == 0 ) {
			// Even.  Try higher...
			nlabels_i = nlabels_mid + trycount;
		}
		else {	// Odd.  Go lower...
			nlabels_i = nlabels_mid - trycount;
		}
*/
	}

	// If can't find limits, use Marcio's code.  Limits are not rounded
	// to as intuitive values, but works where the above may not.  Need to
	// evaluate both approaches and come up with something bulletproof...

	GRLimits marcio_limits = chooseLabels ( xmin, xmax, minl, maxl );
	// Fill in the array...
	double newmin = marcio_limits.getLeftX();
	double newmax = marcio_limits.getRightX();
	double newinc = marcio_limits.getBottomY();
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "GRAxis.findNLabels",
		"Found nice min " + newmin + " max " + newmax + " increment " + newinc );
	}
	// Always add one on the endpoint...
	int size = (int)(((newmax - newmin)/newinc) + 1.01);
	xlabel = new double[size];
	for ( int ix = 0; ix < size; ix++ ) {
		xlabel[ix] = newmin + newinc*ix;
	}
	return xlabel;

/*
	Message.printWarning ( 20, routine,
	"Unable to find requested number of labels" );
	return null;
*/
}

/**
 * Format an array of floating point numbers as nice strings with general rules:
 * - format most numbers as integers
 * @param labelValues
 * @return array of strings with nice labels, suitable for legend, etc that does not need the precision of the data
 */
public static String [] formatLabels ( double[] labelValues ) {
	if ( labelValues == null ) {
		return new String [0];
	}
	String [] labels = new String[labelValues.length];
	int i = -1;
	for ( double value : labelValues ) {
		++i;
		if ( Math.abs(value) < 1.0 ) {
			labels[i] = String.format("%.1f", value );
		}
		else {
			labels[i] = String.format("%.0f", value );
		}
	}
	// TODO smalers 2021-08-29 need to loop through again and make sure that there are no duplicates,
	// typically due to numbers < 0 needing more digits after the decimal point.
	return labels;
}

// TODO X (JTS - 2003-05-05) this code is only used by marcio's limits.  If we're not using Marcio's
// code, it can probably be removed.
// SAM: Later.
static GRLimits chooseLabels ( double minValue, double maxValue, int minLabels, int maxLabels )
{	double newMin = 0.0, newMax = 0.0, newIncrement = 0.0;

	// Make reasonable assumptions about bogus input values

	if( minValue >= maxValue ) {
		maxValue = minValue + 1;
	}

	if( minLabels < 2 ) {
		minLabels = 2;
	}

	if( maxLabels < minLabels ) {
		maxLabels = minLabels;
	}

	// Try finding the best fit values

	double curError = -1;

	// Figure out range of values

	double range = maxValue - minValue;

	// Optimize choice of number of labels beginning with the maximum number of labels that we are allowed

	int numLabels = maxLabels;
	while( numLabels > minLabels ) {

		// Figure out increment satisfying bounds on number of labels

		double tIncrement = range / (numLabels - 1);

		// Make number "nice" by going up to the next nice number

		tIncrement =  niceDouble( tIncrement );

		// Given the increment, calculate lower and upper bounds for the range using this increment

		double tMax = Math.ceil( maxValue/tIncrement ) * tIncrement;
		double tMin = Math.floor( minValue/tIncrement ) * tIncrement;

		// Calculate the number of labels required for this new choice of increment

		int tNumLabels = (int)((tMax - tMin) / tIncrement) + 1;
		if( (tNumLabels < minLabels) || (tNumLabels > numLabels) ) {
			break;
		}

		// Calculate the maximum error produced by these sets of values

		double error = Math.max( minValue - tMin, tMax - maxValue );

		if( (curError == -1) || (error < curError) ) {
			curError = error;
			newMin = tMin;
			newMax = tMax;
			newIncrement = tIncrement;
		}

		numLabels = tNumLabels - 1;
	}

	if ( curError == -1 ) {
		Message.printWarning ( 3, "GRAxis.chooseLabels",
		"Can't meet constraints to get nice labels (min=" + minValue +
		" max=" + maxValue + " minLabels=" + minLabels + " maxLabels= " + maxLabels + ")" );
		newMin = minValue;
		newMax = maxValue;
		newIncrement = range / (maxLabels - 1);
	}

	//return curError;
	return new GRLimits ( newMin, newIncrement, newMax, newIncrement );
}

/**
This function takes a number and tries to round it up to the next "nice" number.
@param number the number to try to round up.
@return the next "nice" number.
TODO (JTS - 2003-05-05) we should have an example here -- what is a "nice" number?
And maybe should this be moved to MathUtil?
SAM: Actually, functionality should be in MathUtil.  Revisit later.
*/
static double niceDouble( double number ) {
	int exponent = (int)Math.log10( number );
	double power = Math.pow( 10, exponent );
	double mantissa = number / power;
	return Math.ceil( mantissa ) * power;
}

}
