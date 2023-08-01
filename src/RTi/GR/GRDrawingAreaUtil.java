// GRDrawingAreaUtil - GR drawing area utility routines and definitions to operate on GRDrawingArea objects

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

import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.IO.PropList;

/**
GR Drawing Area utility methods and data fields.
All data passed to drawing methods are in data units (not device units), unless otherwise indicated.
*/
public class GRDrawingAreaUtil {

/**
Static data that are used when drawing symbols.
Defined this way instead of dynamically within drawSymbol to avoid some overhead.
*/
private static double []
	__sxm = new double[4],
	__sym = new double[4];

/**
Print a comment to the output (generally only useful for hard-copy devices).
This should be overruled in derived classes.
@param da Drawing area.
@param comment_string Comment string to print.
*/
public static void comment ( GRDrawingArea da, String comment_string ) {
	da.comment ( comment_string );
}

/**
Draws the annotation described in the PropList.  Currently only supported for GRJComponentDrawingAreas.
@param da the drawing area on which to draw the annotation.
@param p the PropList describing the annotation.
*/
public static void drawAnnotation(GRDrawingArea da, PropList p) {
	// TODO (JTS - 2006-05-23) I would change this instead to be something like the following:
	// GRDrawingArea would have an abstract method:
	//	public boolean canDrawAnnotations();
	// that returns true for GRJComponentDrawingAreas but is extended
	// in all others to return false.  That would be much faster, I think.
	if (da instanceof GRJComponentDrawingArea) {
		((GRJComponentDrawingArea)da).drawAnnotation(p);
	}
	else {
		Message.printStatus(1, "", "Annotations not supported on the specified drawing area.");
	}
}

/**
Draw a circular/elliptical arc around a point.
@param da Drawing area.
@param x X-coordinate of center.
@param y Y-coordinate of center.
@param rx Radius in x direction.
@param ry Radius in y direction.
@param a1 Angle at which drawing is to begin (degrees), counter-clockwise from due east.
@param a2 Angle at which drawing is to end (degrees), counter-clockwise from due east.
*/
public static void drawArc ( GRDrawingArea da, double x, double y, double rx, double ry, double a1, double a2 ) {
	double xs = da.scaleXData ( x );
	double ys = da.scaleYData ( y );
	double rxs = da.scaleXData(rx) - da.scaleXData(0.0);
	double rys = da.scaleYData(ry) - da.scaleYData(0.0);
	if ( rxs < 0.0 ) {
		rxs *= -1.0;
	}
	if ( rys < 0.0 ) {
		rys *= -1.0;
	}
	da.drawArc ( xs, ys, rxs, rys, a1, a2 );
	da.setLastXY ( x, y );
}

/**
Draw a circular/elliptical arc around a point.
@param da Drawing area.
@param arc GRArc to draw.
*/
public static void drawArc ( GRDrawingArea da, GRArc arc ) {
	double xs = da.scaleXData ( arc.pt.x );
	double ys = da.scaleYData ( arc.pt.y );
	double rxs = da.scaleXData(arc.xradius) - da.scaleXData(0.0);
	double rys = da.scaleYData(arc.yradius) - da.scaleYData(0.0);
	if ( rxs < 0.0 ) {
		rxs *= -1.0;
	}
	if ( rys < 0.0 ) {
		rys *= -1.0;
	}
	da.drawArc ( xs, ys, rxs, rys, arc.angle1, arc.angle2 );
	// If a GRLocatorArc, also draw the center cross-hairs.
	if ( arc.type == GRShape.LOCATOR_ARC ) {
		GRLocatorArc locator_shape = (GRLocatorArc)arc;
		// Horizontal line.
		drawLine ( da,
			(arc.pt.x - locator_shape.xcross_width), arc.pt.y,
			(arc.pt.x + locator_shape.xcross_width), arc.pt.y );
		// Vertical line.
		drawLine ( da,
			arc.pt.x, (arc.pt.y - locator_shape.ycross_height),
			arc.pt.x, (arc.pt.y + locator_shape.ycross_height) );
	}
	da.setLastXY ( arc.pt.x, arc.pt.y );
}

/**
Draw an arrow from one point to another.
This consists of drawing a line between the points and then optionally adding the arrowheads.
@param da Drawing area.
@param x0 X coordinates of the first point.
@param y0 Y coordinates of the first point.
@param x1 X coordinates of the second point.
@param y1 Y coordinates of the second point.
*/
public static void drawArrow ( GRDrawingArea da, double x0, double y0, double x1, double y1,
	GRLineStyleType lineStyle, double[] dashPattern, double dashOffset,
	GRArrowStyleType fromArrowStyle, GRArrowStyleType toArrowStyle, double arrowWidth, double arrowLength ) {
	// Set the dash pattern for the line.
	if ( lineStyle == GRLineStyleType.SOLID ) {
		da.setLineDash( null, (double)0.0);
	}
	else {
		da.setLineDash( dashPattern, dashOffset );
	}
	// Draw the line connecting the points.
	drawLine ( da, x0, y0, x1, y1 );
	// Draw the from arrowhead.
	if ( fromArrowStyle == GRArrowStyleType.SOLID ) {
		// Fill a polygon.
		double [] x = new double[3];
		double [] y = new double[3];
		x[0] = x0;
		y[0] = y0;
		// Treating the node as the center of a circle, find the angle at which the line leaves the node.
		double angleLine = MathUtil.angleFromPoints(x0, y0, x1, y1);
		// Angle that is 1/2 the arrowhead.
		double angleArrowHalf = Math.atan((arrowWidth/2)/arrowLength);
		// Angle to each point is the original angle plus/minus the arrowhead angle.
		double angle1 = angleLine - angleArrowHalf;
		double angle2 = angleLine + angleArrowHalf;
		//Message.printStatus(2, "", "From line angle=" + Math.toDegrees(angleLine) + " angle1="+
		//	Math.toDegrees(angle1) + " angle2=" + Math.toDegrees(angle2) );
		// Now compute the coordinates, first edge of arrow.
		x[1] = x[0] + Math.cos(angle1)*arrowLength;
		y[1] = y[0] + Math.sin(angle1)*arrowLength;
		// Second edge of arrow.
		x[2] = x[0] + Math.cos(angle2)*arrowLength;
		y[2] = y[0] + Math.sin(angle2)*arrowLength;
		//Message.printStatus(2, "", "" + x[0] +"," + y[0] + " " + x[1] + "," + y[1] + " " + x[2] + "," + y[2] );
		// Fill the polygon.
		fillPolygon ( da, 3, x, y );
	}
	if ( toArrowStyle == GRArrowStyleType.SOLID ) {
		// Fill a polygon - same logic as above but reverse the order of computation on the xDiff, yDiff calculation.
		double [] x = new double[3];
		double [] y = new double[3];
		x[0] = x1;
		y[0] = y1;
		// Treating the node as the center of a circle, find the angle at which the line leaves the node.
		double angleLine = MathUtil.angleFromPoints( x1, y1, x0, y0);
		// Angle that is 1/2 the arrowhead
		double angleArrowHalf = Math.atan((arrowWidth/2)/arrowLength);
		// Angle to each point is the original angle plus/minus the arrowhead angle.
		double angle1 = angleLine - angleArrowHalf;
		double angle2 = angleLine + angleArrowHalf;
		//Message.printStatus(2, "", "To line angle=" + Math.toDegrees(angleLine) + " angle1="+
		//	Math.toDegrees(angle1) + " angle2=" + Math.toDegrees(angle2) );
		// Now compute the coordinates, first edge of arrow.
		x[1] = x[0] + Math.cos(angle1)*arrowLength;
		y[1] = y[0] + Math.sin(angle1)*arrowLength;
		// Second edge of arrow.
		x[2] = x[0] + Math.cos(angle2)*arrowLength;
		y[2] = y[0] + Math.sin(angle2)*arrowLength;
		//Message.printStatus(2, "", "" + x[0] +"," + y[0] + " " + x[1] + "," + y[1] + " " + x[2] + "," + y[2] );
		// Fill the polygon.
		fillPolygon ( da, 3, x, y );
	}
}

/**
Draw an arrow from one point to another.
This consists of drawing a line between the points and then optionally adding the arrowheads.
@param da Drawing area.
@param x0 X coordinates of the first point.
@param y0 Y coordinates of the first point.
@param x1 X coordinates of the second point.
@param y1 Y coordinates of the second point.
*/
public static void drawArrowSave ( GRDrawingArea da, double x0, double y0, double x1, double y1,
	GRLineStyleType lineStyle, double[] dashPattern, double dashOffset,
	GRArrowStyleType fromArrowStyle, GRArrowStyleType toArrowStyle, double arrowWidth, double arrowLength ) {
	// Set the dash pattern for the line.
	if ( lineStyle == GRLineStyleType.SOLID ) {
		da.setLineDash( null, (double)0.0);
	}
	else {
		da.setLineDash( dashPattern, dashOffset );
	}
	// Draw the line connecting the points.
	drawLine ( da, x0, y0, x1, y1 );
	// Draw the from arrowhead.
	if ( fromArrowStyle == GRArrowStyleType.SOLID ) {
		// Fill a polygon.
		double [] x = new double[3];
		double [] y = new double[3];
		x[0] = x0;
		y[0] = y0;
		// Treating the node as the center of a circle, find the angle at which the line leaves the node.
		double angleLine = MathUtil.angleFromPoints(x0, y0, x1, y1);
		// Angle that is 1/2 the arrowhead
		double angleArrowHalf = Math.atan((arrowWidth/2)/arrowLength);
		// Angle to each point is the original angle plus/minus the arrowhead angle.
		double angle1 = angleLine - angleArrowHalf;
		double angle2 = angleLine + angleArrowHalf;
		Message.printStatus(2, "", "From line angle=" + Math.toDegrees(angleLine) + " angle1="+
			Math.toDegrees(angle1) + " angle2=" + Math.toDegrees(angle2) );
		// Now compute the coordinates, first edge of arrow.
		x[1] = x[0] + Math.cos(angle1)*arrowLength;
		y[1] = y[0] + Math.sin(angle1)*arrowLength;
		// Second edge of arrow.
		x[2] = x[0] + Math.cos(angle2)*arrowLength;
		y[2] = y[0] + Math.sin(angle2)*arrowLength;
		Message.printStatus(2, "", "" + x[0] +"," + y[0] + " " + x[1] + "," + y[1] + " " + x[2] + "," + y[2] );
		// Fill the polygon.
		fillPolygon ( da, 3, x, y );
	}
	if ( toArrowStyle == GRArrowStyleType.SOLID ) {
		// Fill a polygon - same logic as above but reverse the order of computation on the xDiff, yDiff calculation.
		double [] x = new double[3];
		double [] y = new double[3];
		x[0] = x1;
		y[0] = y1;
		// Treating the node as the center of a circle, find the angle at which the line leaves the node.
		double angleLine = MathUtil.angleFromPoints( x1, y1, x0, y0);
		// Angle that is 1/2 the arrowhead.
		double angleArrowHalf = Math.atan((arrowWidth/2)/arrowLength);
		// Angle to each point is the original angle plus/minus the arrowhead angle.
		double angle1 = angleLine - angleArrowHalf;
		double angle2 = angleLine + angleArrowHalf;
		Message.printStatus(2, "", "To line angle=" + Math.toDegrees(angleLine) + " angle1="+
			Math.toDegrees(angle1) + " angle2=" + Math.toDegrees(angle2) );
		// Now compute the coordinates, first edge of arrow.
		x[1] = x[0] + Math.cos(angle1)*arrowLength;
		y[1] = y[0] + Math.sin(angle1)*arrowLength;
		// Second edge of arrow.
		x[2] = x[0] + Math.cos(angle2)*arrowLength;
		y[2] = y[0] + Math.sin(angle2)*arrowLength;
		Message.printStatus(2, "", "" + x[0] +"," + y[0] + " " + x[1] + "," + y[1] + " " + x[2] + "," + y[2] );
		// Fill the polygon.
		fillPolygon ( da, 3, x, y );
	}
}

/**
Draw a line from one point to another.
@param da Drawing area.
@param x0 X coordinates of the first point.
@param y0 Y coordinates of the first point.
@param x1 X coordinates of the second point.
@param y1 Y coordinates of the second point.
*/
public static void drawLine ( GRDrawingArea da, double x0, double y0, double x1, double y1 ) {
	double x[] = new double[2];
	double y[] = new double[2];
	x[0] = x0;
	y[0] = y0;
	x[1] = x1;
	y[1] = y1;
	drawLine ( da, x, y );
}

/**
Draw a line from one point to another.
@param da Drawing area.
@param x X coordinates of points.
@param y Y coordinates of points.
*/
public static void drawLine ( GRDrawingArea da, double [] x, double [] y ) {
	drawPolyline ( da, 2, x, y );
	// Save the last point.
	da.setLastXY ( x[1], y[1] );
}

/**
Draw a polygon (last point is connected to first).
@param da Drawing area.
@param polygon GRPolygon to draw.
*/
public static void drawPolygon ( GRDrawingArea da, GRPolygon polygon ) {
	if ( polygon.npts == 0 ) {
		return;
	}
	double [] xs = new double[polygon.npts];
	double [] ys = new double[polygon.npts];
	for ( int i = 0; i < polygon.npts; i++ ) {
		xs[i] = da.scaleXData ( polygon.pts[i].x );
		ys[i] = da.scaleYData ( polygon.pts[i].y );
	}
	// Reduce the number of points.
	//GRReducePoints ( xs, ys, &npts, 0 );
	da.drawPolygon ( polygon.npts, xs, ys );
	da.setLastXY( polygon.pts[polygon.npts - 1].x, polygon.pts[polygon.npts - 1].y );
}

/**
Draw a polygon (last point is connected to first).
@param da Drawing area.
@param npts Number of points to draw.
@param x X-coordinates of points.
@param y Y-coordinates of points.
*/
public static void drawPolygon ( GRDrawingArea da, int npts, double x[], double y[] ) {
	if ( npts == 0 ) {
		return;
	}
	double [] xs = new double[npts];
	double [] ys = new double[npts];
	for ( int i = 0; i < npts; i++ ) {
		xs[i] = da.scaleXData ( x[i] );
		ys[i] = da.scaleYData ( y[i] );
	}
	// Reduce the number of points.
	//GRReducePoints ( xs, ys, &npts, 0 );
	da.drawPolygon ( npts, xs, ys );
	da.setLastXY( x[npts - 1], y[npts - 1] );
}

/**
Draw a segmented line
@param da the drawing area to draw on
@param polyline the polyline to draw.
*/
public static void drawPolyline ( GRDrawingArea da, GRPolyline polyline ) {
	double [] xs, ys;
	int i;

	if ( polyline.npts == 0 ) {
		return;
	}
	xs = new double[polyline.npts];
	ys = new double[polyline.npts];
	for ( i = 0; i < polyline.npts; i++ ) {
		xs[i] = da.scaleXData ( polyline.pts[i].x );
		ys[i] = da.scaleYData ( polyline.pts[i].y );
	}
	da.drawPolyline ( polyline.npts, xs, ys );
	da.setLastXY ( polyline.pts[polyline.npts - 1].x, polyline.pts[polyline.npts - 1].y);
}

/**
Draw a segmented line.
@param da the drawing area to draw on
@param npts the number of points in the polyline
@param x array of x coordinates
@param y array of y coordinates
*/
public static void drawPolyline ( GRDrawingArea da, int npts, double x[], double y[] ) {
	double [] xs, ys;
	int i;

	if ( npts == 0 ) {
		return;
	}
	xs = new double[npts];
	ys = new double[npts];
	for ( i = 0; i < npts; i++ ) {
		xs[i] = da.scaleXData ( x[i] );
		ys[i] = da.scaleYData ( y[i] );
	}
	// Reduce the number of points.
	//npts = reducePoints ( xs, ys, npts, 0 );
	da.drawPolyline ( npts, xs, ys );
	da.setLastXY (x[npts - 1], y[npts - 1]);
}

/**
Draw a segmented line
@param da the drawing area to draw on
@param polyline the polyline to draw.
*/
public static void drawPolylineZM ( GRDrawingArea da, GRPolylineZM polyline ) {
	double [] xs, ys;
	int i;

	if ( polyline.npts == 0 ) {
		return;
	}
	xs = new double[polyline.npts];
	ys = new double[polyline.npts];
	for ( i = 0; i < polyline.npts; i++ ) {
		xs[i] = da.scaleXData ( polyline.pts[i].x );
		ys[i] = da.scaleYData ( polyline.pts[i].y );
	}
	da.drawPolyline ( polyline.npts, xs, ys );
	da.setLastXY ( polyline.pts[polyline.npts - 1].x, polyline.pts[polyline.npts - 1].y);
}

/**
Draw a rectangle given a drawing area and rectangle information in data units.
@param da GR drawing area.
@param xll X-coordinates of lower left corner of rectangle.
@param yll Y-coordinates of lower left corner of rectangle.
@param width Width of rectangle (can be negative, in which case xll will be recomputed).
@param height Height of rectangle (can be negative, in which case yll will be recomputed).
*/
public static void drawRectangle ( GRDrawingArea da, double xll, double yll, double width, double height ) {
	double [] xs, ys;

	xs = new double[4];
	ys = new double[4];
	// Scale the data.  Allow the width and height to be negative and adjust the dimensions accordingly.
	xs[0] = da.scaleXData ( xll );
	xs[1] = da.scaleXData ( xll + width );
	xs[2] = xs[1];
	xs[3] = xs[0];

	ys[0] = da.scaleYData ( yll );
	ys[1] = ys[0];
	ys[2] = da.scaleYData ( yll + height );
	ys[3] = ys[2];
/*
	if ( width >= 0.0 ) {
		xs[0] = da.scaleXData ( xll );
		xs[1] = da.scaleXData ( xll + width );
	}
	else {	xs[0] = da.scaleXData ( xll + width );
		xs[1] = da.scaleXData ( xll );
	}
	// Same no matter what...
	xs[2] = xs[1];
	xs[3] = xs[0];
	if ( height >= 0.0 ) {
		ys[0] = da.scaleXData ( yll );
		ys[2] = da.scaleXData ( yll + height );
	}
	else {	ys[0] = da.scaleYData ( yll + height );
		xs[2] = da.scaleYData ( yll );
	}
	// Same no matter what...
	ys[1] = ys[0];
	ys[3] = xs[2];
*/
	da.drawPolygon ( 4, xs, ys );
	da.setLastXY ( xll, yll );
}

/**
Draw a GRShape.  The shape must be one of the core GR shapes defined within the GR package (not a derived shape).
There is a performance hit by calling this method.
If code needs to be optimized, make calls from higher level code.  The shape will not be filled.
@param da Drawing area to draw to.
@param shape GRShape to draw.
*/
public static void drawShape ( GRDrawingArea da, GRShape shape ) {
	drawShape ( da, shape, false );
}

/**
Draw a GRShape.  The shape must be one of the core GR shapes (not a derived shape).
There is a performance hit by calling this method.
If code needs to be optimized, make calls from higher level code.  The shape will not be filled.
@param da Drawing area to draw to.
@param shape GRShape to draw.
@param fill True if the shape should be filled (for shapes that support filling, like GRPolygon).
The fill is assumed to be solid.
*/
public static void drawShape ( GRDrawingArea da, GRShape shape, boolean fill ) {
	drawShape ( da, shape, fill, 0 );
}

/**
Draw a GRShape.  The shape must be one of the core GR shapes (not a derived shape).
There is a performance hit by calling this method.
If code needs to be optimized, make calls from higher level code.
@param da Drawing area to draw to.
@param shape GRShape to draw.
@param fill True if the shape should be filled (for shapes that support filling, like GRPolygon).
@param transparency Indicates the level of transparency when used with filled shapes.
If 0, the fill is totally opaque (solid).  If 255, the fill is totally transparent.
This is the reverse of the alpha parameter.
*/
public static void drawShape ( GRDrawingArea da, GRShape shape, boolean fill, int transparency ) {
	if ( shape == null ) {
		return;
	}
	int type = shape.type;
	if ( (type == GRShape.ARC) || (type == GRShape.LOCATOR_ARC) ) {
		GRArc arc = (GRArc)shape;
		if ( fill ) {
			fillArc ( da, arc, GRArcFillType.CHORD );
		}
		else {
		    drawArc ( da, arc );
		}
	}
	else if ( type == GRShape.POLYGON ) {
		GRPolygon polygon = (GRPolygon)shape;
		if ( fill ) {
			if ( transparency == 0 ) {
				fillPolygon ( da, polygon );
			}
			else {
			    fillPolygon ( da, polygon, transparency );
			}
		}
		else {
		    drawPolygon ( da, polygon );
		}
	}
	else if ( type == GRShape.POLYGON_LIST ) {
		GRPolygonList polygonlist = (GRPolygonList)shape;
		int n = polygonlist.npolygons;
		for ( int i = 0; i < n; i++ ) {
			if ( polygonlist.polygons[i] == null ) {
				continue;
			}
			if ( fill ) {
				if ( transparency == 0 ) {
					fillPolygon ( da, polygonlist.polygons[i] );
				}
				else {
				    fillPolygon ( da, polygonlist.polygons[i], transparency );
				}
			}
			else {
			    drawPolygon ( da, polygonlist.polygons[i] );
			}
		}
	}
	else if ( type == GRShape.POLYLINE ) {
		GRPolyline polyline = (GRPolyline)shape;
		drawPolyline ( da, polyline );
	}
	else if ( type == GRShape.POLYLINE_ZM ) {
		GRPolylineZM polyline = (GRPolylineZM)shape;
		drawPolylineZM ( da, polyline );
	}
	else if ( type == GRShape.POLYLINE_LIST ) {
		GRPolylineList polylinelist = (GRPolylineList)shape;
		int n = polylinelist.npolylines;
		for ( int i = 0; i < n; i++ ) {
			if ( polylinelist.polylines[i] == null ) {
				continue;
			}
			drawShape ( da, polylinelist.polylines[i] );
		}
	}
	else if ( type == GRShape.POLYLINE_ZM_LIST ) {
		GRPolylineZMList polylinelist = (GRPolylineZMList)shape;
		int n = polylinelist.npolylines;
		for ( int i = 0; i < n; i++ ) {
			if ( polylinelist.polylines[i] == null ) {
				continue;
			}
			drawShape ( da, polylinelist.polylines[i] );
		}
	}
	else {
		// Else, don't know what to do so ignore.
		Message.printStatus(2, "", "Don't know how to draw GRShape " + type );
	}
}

/**
Draw a symbol at a point.  See the overloaded version for more information.
@param da Drawing area.
@param symbol Symbol to be drawn at point.
@param x X-coordinate of the point in data units.
@param y Y-coordinate of the point in data units.
@param size Size of the symbol (see "flag").
@param flag Indicates whether data (GRUNIT_DATA) or device (GRUNIT_DEV) units are being used for "size".
@param orient Orientation for symbol (see flag in GRSymbol, e.g., GRSymbolShapeType.SYM_LEFT).
*/
public static void drawSymbol ( GRDrawingArea da, GRSymbolShapeType symbol,
	double x, double y, double size, int flag, int orient ) {
	// Assume the same x and y dimension for the symbol and no secondary data.
	drawSymbol ( da, symbol, x, y, size, size, 0.0, 0.0, null, flag, orient );
}

/**
Draw a symbol at a point.
<ul>
<li>	Direct calls to driver routines are made if "flag" is GRUNIT_DEV because
	the size of the symbol is given in device units.
	This prevents the symbol from being scaled if the calling program zooms in on a plot.
	It also removes the need to back-calculate dimensions to data units in the X and Y direction.</li>
<li>	If "flag" is GRUNIT_DATA, then the size is scaled to device units first.
	This is generally slower and should be avoided if possible.
	The symbol size will also change as zooming occurs.</li>
<li>	If the device needs the Y axis flipped,
	deal with that here because the drawing is done with scaled data.
	This only impacts symbols that are non-symmetric vertically.</li>
</ul>
@param da Drawing area.
@param symbol Symbol to be drawn at point.
@param x X-coordinate of the point in data units.
@param y Y-coordinate of the point in data units.
@param size_x Size of the symbol in the x-direction (see "flag").
@param size_y Size of the symbol in the y-direction (see "flag").
@param offset_x X offset of the symbol from the given location.  This can be
used to plot multiple symbols at a point.
@param offset_y Y offset of the symbol from the given location.  This can be
used to plot multiple symbols at a point.
@param data Array of secondary data.
For example, for scaled symbols like the teacup this indicates some dynamic data used to draw the symbol.
This is only used for some symbols.
@param flag Indicates whether data (GRUNIT_DATA) or device (GRUNIT_DEV) units are being used for "size".
@param orient Orientation for symbol (see flags in GRSymbol, e.g. GRSymbolShapeType.SYM_LEFT).
*/
public static void drawSymbol ( GRDrawingArea da, GRSymbolShapeType symbol, double x, double y, double size_x, double size_y,
	double offset_x, double offset_y, double [] data, int flag, int orient ) {
	drawSymbol(da, symbol, x, y, size_x, size_y, offset_x, offset_y, data, flag, orient, null);
}

/**
Draw a symbol at a point.
<ul>
<li>	Direct calls to driver routines are made if "flag" is GRUNIT_DEV because
	the size of the symbol is given in device units.
	This prevents the symbol from being scaled if the calling program zooms in on a plot.
	It also removes the need to back-calculate dimensions to data units in the X and Y direction.</li>
<li>	If "flag" is GRUNIT_DATA, then the size is scaled to device units first.
	This is generally slower and should be avoided if possible.
	The symbol size will also change as zooming occurs.</li>
<li>	If the device needs the Y axis flipped,
	deal with that here because the drawing is done with scaled data.
	This only impacts symbols that are non-symmetric vertically.</li>
</ul>
@param da Drawing area.
@param symbol Symbol to be drawn at point.
@param x X-coordinate of the point in data units.
@param y Y-coordinate of the point in data units.
@param size_x Size of the symbol in the x-direction (see "flag").
@param size_y Size of the symbol in the y-direction (see "flag").
@param offset_x X offset of the symbol from the given location.
This can be used to plot multiple symbols at a point.
@param offset_y Y offset of the symbol from the given location.
This can be used to plot multiple symbols at a point.
@param data Array of secondary data.
For example, for scaled symbols like the teacup this indicates some dynamic data used to draw the symbol.
This is only used for some symbols.
@param flag Indicates whether data (GRUNIT_DATA) or device (GRUNIT_DEV) units are being used for "size".
@param orient Orientation for symbol (see flags in GRSymbol, e.g. GRSymbolShapeType.SYM_LEFT).
@param props PropList that is only used (currently) when drawing TeaCups.
If not null, the teacup will be filled to a level specified by the data parameter.
*/
public static void drawSymbol ( GRDrawingArea da, GRSymbolShapeType symbol, double x, double y, double size_x, double size_y,
	double offset_x, double offset_y, double [] data, int flag, int orient, PropList props)	{
	drawSymbol(da, symbol, x, y, size_x, size_y, offset_x, offset_y, data, flag, orient, props, null);
}

/**
Draw a symbol at a point.
<ul>
<li>	Direct calls to driver routines are made if "flag" is GRUNIT_DEV because
	the size of the symbol is given in device units.
	This prevents the symbol from being scaled if the calling program zooms in on a plot.
	It also removes the need to back-calculate dimensions to data units in the X and Y direction.</li>
<li>	If "flag" is GRUNIT_DATA, then the size is scaled to device units first.
	This is generally slower and should be avoided if possible.
	The symbol size will also change as zooming occurs.</li>
<li>	If the device needs the Y axis flipped, deal with that here because the drawing is done with scaled data.
	This only impacts symbols that are non-symmetric vertically.</li>
</ul>
@param da Drawing area.
@param symbol Symbol to be drawn at point (see GRSymbolShapeType.SYM_*).
@param x X-coordinate of the point in data units.
@param y Y-coordinate of the point in data units.
@param size_x Size of the total symbol area in the x-direction (see "flag"), the specified symbol will cover a portion.
@param size_y Size of the total symbol area in the y-direction (see "flag"), the specified symbol will cover a portion.
@param offset_x X offset of the symbol from the given location.
This can be used to plot multiple symbols at a point.
@param offset_y Y offset of the symbol from the given location.
This can be used to plot multiple symbols at a point.
@param data Array of secondary data.
For example, for scaled symbols like the teacup this indicates some dynamic data used to draw the symbol.
This is only used for some symbols.
@param flag Indicates whether data (GRUNIT_DATA) or device (GRUNIT_DEV) units are being used for "size".
@param orient Orientation for symbol (see flags in GRSymbol, e.g. GRSymbolShapeType.SYM_LEFT).
@param props PropList that is only used (currently) when drawing TeaCups.
If not null, the teacup will be filled to a level specified by the data parameter.
@param outlineColor currently only used by filled triangles, specifies the color to draw the outline of the symbol in.
*/
public static void drawSymbol ( GRDrawingArea da, GRSymbolShapeType symbol, double x, double y, double size_x, double size_y,
	double offset_x, double offset_y, double [] data, int flag, int orient, PropList props, GRColor outlineColor) {
	double msizex, // Symbol x direction size, device units.
		msizex2, // 1/2 of 'msizex'.
		msizey, // Symbol y direction size, device units.
		msizey2, // 1/2 of 'msizey'.
		xs, // X coordinate scaled to device units.
		ys; // Y coordinate scaled to device units.

	if ( flag == GRUnits.DEVICE ) {
		// Symbol size is already computed and can be added to the scaled values.
		msizex = size_x;
		msizey = size_y;
		msizex2	= msizex/2.0;
		msizey2	= msizex2;
		xs = da.scaleXData(x) + offset_x;
		ys = da.scaleYData(y) + offset_y;
	}
	else {
		// if ( flag == GRUnits.DATA )
		// Symbol size needs to be computed from data units.
		// To do so, use a unit in data units and then scale
		// (this will generally be close but may be a problem for some projections if not a true aspect).
		// This is seldom used.
		// Get X size in device units.
		msizex = da.scaleXData(size_x) - da.scaleXData(0);
		// Now scale the Y size.
		msizey = da.scaleYData(size_y) - da.scaleYData(0);
		// Is this still needed?
		msizey = msizey*(da.scaleXData(1.0) - da.scaleXData(0.0))/(da.scaleYData(1.0) - da.scaleYData(0.0));
		msizex2	= msizex/2.0;
		msizey2	= msizey/2.0;
		xs = da.scaleXData(x + offset_x);
		ys = da.scaleYData(y + offset_y);
	}
	// Now position the symbol if requested.
	// Do so by offsetting the previously calculated position by 1/2 the symbol size.
	// The default is to be centered on the point, which are the coordinates previously used.
	if ( (orient & GRSymbolPosition.LEFT) != 0 ) {
		xs += msizex2;
	}
	else if ( (orient & GRSymbolPosition.RIGHT) != 0 ) {
		xs -= msizex2;
	}
	if ( (orient & GRSymbolPosition.TOP) != 0 ) {
		if ( da._reverse_y ) {
			ys += msizey2;
		}
		else {
			ys -= msizey2;
		}
	}
	else if ( (orient & GRSymbolPosition.BOTTOM) != 0 ) {
		if ( da._reverse_y ) {
			ys -= msizey2;
		}
		else {
			ys += msizey2;
		}
	}

	// Now draw the symbol:
	// - a large "if" statement is used to check the symbol style
	// - TODO smalers 2023-07-31 need to sort this by likely use, for example filled symbols first

	if ( symbol == GRSymbolShapeType.ASTERISK ) {
		drawSymbol(da,GRSymbolShapeType.PLUS, x, y, size_x, size_y, offset_x, offset_y, data, flag, orient );
		__sxm[0] = xs - msizex2*.707;	__sym[0] = ys-msizey2*.707;
		__sxm[1] = xs + msizex2*.707;	__sym[1] = ys+msizey2*.707;
		//if ( flag == GRUnits.DEVICE ) {
			// Symbol size in device units.
			da.drawLine ( __sxm, __sym );
		//}
		//else {
		//	drawLine ( da, __sxm, __sym );
		//}
		__sxm[0] = xs - msizex2*.707;	__sym[0] = ys+msizey2*.707;
		__sxm[1] = xs + msizex2*.707;	__sym[1] = ys-msizey2*.707;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.BAR ) {
		__sxm[0] = xs;	__sym[0] = ys - msizey2;
		__sxm[1] = xs;	__sym[1] = ys + msizey2;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SQUARE_BOTTOM_FILLED ) {
		drawSymbol( da, GRSymbolShapeType.SQUARE, x, y, size_x, size_y, offset_x, offset_y, data, flag, orient );
		__sxm[0] = xs + msizex2;	__sym[0] = ys;
		__sxm[1] = __sxm[0];
		if ( da._reverse_y ) {
			__sym[1] = ys + msizey2;
		}
		else {
			__sym[1] = ys - msizey2;
		}
		__sxm[2] = xs - msizex2;	__sym[2] = __sym[1];
		__sxm[3] = __sxm[2];		__sym[3] = ys;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.LINE_BOTTOM ) {
		__sxm[0] = xs - msizex2;
		if ( da._reverse_y ) {
			__sym[0] = ys + msizey2;
		}
		else {	__sym[0] = ys - msizey2;
		}
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SLASH_BACKWARD ) {
		__sxm[0] = xs - msizex2;
		__sxm[1] = xs + msizex2;
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[1] = ys + msizey2;
		}
		else {	__sym[0] = ys + msizey2;
			__sym[1] = ys - msizey2;
		}
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.LINE_TOP_BOTTOM ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys + msizey2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
		__sym[0] = ys - msizey2;
		__sym[1] = __sym[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.CIRCLE ) {
		if (da instanceof GRJComponentDrawingArea) {
//			da.drawArc ( xs, ys, msizex2, msizey2, 0.0, 360.0 );
			((GRJComponentDrawingArea)da).drawOval(xs, ys, msizex2, msizey2);
		}
		else {
			da.drawArc ( xs, ys, msizex2, msizey2, 0.0, 360.0 );
		}

		//if ( flag == GRUnits.DEVICE ) {
		//	da.drawArc ( xs, ys, msizex2, msizey2, 0.0, 360.0 );
		//}
		//else {drawArc ( da, xs, ys, msizex2, msizey2, 0.0, 360.0 );
		//}
	}
	else if ( symbol == GRSymbolShapeType.ELLIPSE_HORIZONTAL ) {
		// Horizontal dimension of the ellipse is twice the vertical dimension.
		if (da instanceof GRJComponentDrawingArea) {
//			da.drawArc ( xs, ys, msizex2, msizey2, 0.0, 360.0 );
			((GRJComponentDrawingArea)da).drawOval(xs, ys, msizex2, msizey2/2.0);
		}
		else {
			da.drawArc ( xs, ys, msizex2, msizey2/2.0, 0.0, 360.0 );
		}

		//if ( flag == GRUnits.DEVICE ) {
		//	da.drawArc ( xs, ys, msizex2, msizey2, 0.0, 360.0 );
		//}
		//else {drawArc ( da, xs, ys, msizex2, msizey2, 0.0, 360.0 );
		//}
	}
	else if ( symbol == GRSymbolShapeType.ELLIPSE_VERTICAL ) {
		// Vertical dimension of the ellipse is twice the horizontal dimension.
		if (da instanceof GRJComponentDrawingArea) {
//			da.drawArc ( xs, ys, msizex2, msizey2, 0.0, 360.0 );
			((GRJComponentDrawingArea)da).drawOval(xs, ys, msizex2/2.0, msizey2);
		}
		else {
			da.drawArc ( xs, ys, msizex2/2.0, msizey2, 0.0, 360.0 );
		}

		//if ( flag == GRUnits.DEVICE ) {
		//	da.drawArc ( xs, ys, msizex2, msizey2, 0.0, 360.0 );
		//}
		//else {drawArc ( da, xs, ys, msizex2, msizey2, 0.0, 360.0 );
		//}
	}
	else if ( symbol == GRSymbolShapeType.ARROW_DOWN ) {
		drawSymbol ( da, GRSymbolShapeType.CARET_DOWN, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.BAR, x, y, size_x, size_y,
		 	offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.CARET_DOWN ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys;
		__sxm[1] = xs;
		if ( da._reverse_y ) {
			__sym[1] = ys + msizey2;
		}
		else {
			__sym[1] = ys - msizey2;
		}
		__sxm[2] = xs + msizex2;	__sym[2] = ys;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolyline ( 3, __sxm, __sym );
		//}
		//else {	drawPolyline ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.DIAMOND ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys;
		__sxm[1] = xs;		__sym[1] = ys + msizey2;
		__sxm[2] = xs + msizex2;	__sym[2] = ys;
		__sxm[3] = xs;		__sym[3] = ys - msizey2;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolygon ( 4, __sxm, __sym );
		//}
		//else {	drawPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_DOWN ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		else {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		__sxm[0] = xs - msizex2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = xs;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolygon ( 3, __sxm, __sym );
		//}
		//else {	drawPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.LINE_LEFT_RIGHT ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys + msizey2;
		__sxm[1] = __sxm[0];		__sym[1] = ys - msizey2;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
		__sxm[0] = xs + msizex2;
		__sxm[1] = __sxm[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.X ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys + msizey2;
		__sxm[1] = xs + msizex2;	__sym[1] = ys - msizey2;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
		__sxm[0] = xs + msizex2;	__sym[0] = ys + msizey2;
		__sxm[1] = xs - msizex2;	__sym[1] = ys - msizey2;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.X_CAP ) {
		drawSymbol ( da, GRSymbolShapeType.X, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.LINE_TOP_BOTTOM, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.X_DIAMOND ) {
		drawSymbol ( da, GRSymbolShapeType.X_FOR_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA1 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA1, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA12 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA1, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA2, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA123 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA1, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA2, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA3, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA124 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA1, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA2, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA4, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA1234 ) {
		drawSymbol ( da, GRSymbolShapeType.DIAMOND_FILLED, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA13 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA1, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA3, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA134 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA1, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA3, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA4, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA14 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA1, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA4, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA2 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA2, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA23 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA2, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA3, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA234 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA2, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA3, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA4, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA24 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA2, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA4, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA3 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA3, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.EXDIA4 ) {
		drawSymbol ( da, GRSymbolShapeType.X_DIAMOND, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.FDIA4, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.X_EDGE ) {
		drawSymbol ( da, GRSymbolShapeType.X, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.LINE_LEFT_RIGHT, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.X_FOR_DIAMOND ) {
		__sxm[0] = xs - msizex2/2.0;	__sym[0] = ys +msizey2/2.0;
		__sxm[1] = xs + msizex2/2.0;	__sym[1] = ys -msizey2/2.0;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
		__sxm[0] = xs + msizex2/2.0;	__sym[0] = ys +msizey2/2.0;
		__sxm[1] = xs - msizex2/2.0;	__sym[1] = ys -msizey2/2.0;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.X_SQUARE ) {
		drawSymbol ( da, GRSymbolShapeType.X, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.SQUARE, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.ARROW_UP_RIGHT_FILLED ) {
		drawSymbol ( da, GRSymbolShapeType.SLASH_FORWARD, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.SQUARE_TRIANGLE_UPPER_RIGHT_FILLED, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.ARROW_DOWN_RIGHT_FILLED ) {
		drawSymbol ( da, GRSymbolShapeType.SLASH_BACKWARD, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.SQUARE_TRIANGLE_LOWER_RIGHT_FILLED, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient);
	}
	else if ( symbol == GRSymbolShapeType.ARROW_DOWN_LEFT_FILLED ) {
		drawSymbol ( da, GRSymbolShapeType.SLASH_FORWARD, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.SQUARE_TRIANGLE_LOWER_LEFT_FILLED, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.ARROW_UP_LEFT_FILLED ) {
		drawSymbol ( da, GRSymbolShapeType.SLASH_BACKWARD, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.SQUARE_TRIANGLE_UPPER_LEFT_FILLED, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.DIAMOND_LOWER_HALF_FILLED ) {
		__sxm[0] = xs + msizex2;	__sym[0] = ys;
		__sxm[1] = xs;
		if ( da._reverse_y ) {
			__sym[1] = ys + msizey2;
		}
		else {
			__sym[1] = ys - msizey2;
		}
		__sxm[2] = xs - msizex2;	__sym[2] = ys;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.FBOTDIA4 ) {
		__sxm[0] = xs + msizex2/2.0;
		__sxm[1] = xs;
		if ( da._reverse_y ) {
			__sym[0] = ys + msizey2/2.0;
			__sym[1] = ys + msizey2;
		}
		else {
			__sym[0] = ys - msizey2/2.0;
			__sym[1] = ys - msizey2;
		}
		__sxm[2] = xs - msizex2/2.0;	__sym[2] = __sym[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.CIRCLE_FILLED ) {
		//if ( flag == GRUnits.DEVICE ) {
		if (da instanceof GRJComponentDrawingArea) {
//			da.fillArc ( xs, ys, msizex2, msizey2, 0.0, 360.0, FILL_CHORD );
			((GRJComponentDrawingArea)da).fillOval(xs, ys, msizex2, msizey2);
		}
		else {
			da.fillArc ( xs, ys, msizex2, msizey2, 0.0, 360.0, GRArcFillType.CHORD );
		}
		//}
		//else {
//			fillArc ( da, xs, ys, msizex2, msizey2, 0.0, 360.0, GRArcFillType.CHORD );
		//}
	}
	else if ( symbol == GRSymbolShapeType.ELLIPSE_HORIZONTAL_FILLED ) {
		// Horizontal dimension of the ellipse is twice the vertical dimension.
		//if ( flag == GRUnits.DEVICE ) {
		if (da instanceof GRJComponentDrawingArea) {
//			da.fillArc ( xs, ys, msizex2, msizey2, 0.0, 360.0, FILL_CHORD );
			((GRJComponentDrawingArea)da).fillOval(xs, ys, msizex2, msizey2/2.0);
		}
		else {
			da.fillArc ( xs, ys, msizex2, msizey2/2.0, 0.0, 360.0, GRArcFillType.CHORD );
		}
		//}
		//else {
//			fillArc ( da, xs, ys, msizex2, msizey2, 0.0, 360.0, GRArcFillType.CHORD );
		//}
	}
	else if ( symbol == GRSymbolShapeType.ELLIPSE_VERTICAL_FILLED ) {
		// Vertical dimension of the ellipse is twice the horizontal dimension.
		//if ( flag == GRUnits.DEVICE ) {
		if (da instanceof GRJComponentDrawingArea) {
//			da.fillArc ( xs, ys, msizex2, msizey2, 0.0, 360.0, FILL_CHORD );
			((GRJComponentDrawingArea)da).fillOval(xs, ys, msizex2/2.0, msizey2);
		}
		else {
			da.fillArc ( xs, ys, msizex2/2.0, msizey2, 0.0, 360.0, GRArcFillType.CHORD );
		}
		//}
		//else {
//			fillArc ( da, xs, ys, msizex2, msizey2, 0.0, 360.0, GRArcFillType.CHORD );
		//}
	}
	else if ( symbol == GRSymbolShapeType.DIAMOND_FILLED ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys;
		__sxm[1] = xs;
		__sxm[2] = xs + msizex2;	__sym[2] = ys;
		__sxm[3] = xs;
		if ( da._reverse_y ) {
			__sym[1] = ys - msizey2;
			__sym[3] = ys + msizey2;
		}
		else {
			__sym[1] = ys + msizey2;
			__sym[3] = ys - msizey2;
		}
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.FDIA1 ) {
		__sxm[0] = xs;
		__sxm[1] = xs + msizex2/2.0;
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[1] = ys - msizey2/2.0;
		}
		else {
			__sym[0] = ys + msizey2;
			__sym[1] = ys + msizey2/2.0;
		}
		__sxm[2] = xs;			__sym[2] = ys;
		__sxm[3] = xs - msizex2/2.0;	__sym[3] = __sym[1];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.FDIA2 ) {
		__sxm[0] = xs + msizex2/2.0;
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2/2.0;
			__sym[2] = ys + msizey2/2.0;
		}
		else {
			__sym[0] = ys + msizey2/2.0;
			__sym[2] = ys - msizey2/2.0;
		}
		__sxm[1] = xs + msizex2;		__sym[1] = ys;
		__sxm[2] = __sxm[0];
		__sxm[3] = xs;			__sym[3] = ys;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.FDIA3 ) {
		__sxm[0] = xs;			__sym[0] = ys;
		__sxm[1] = xs + msizex2/2.0;
		if ( da._reverse_y ) {
			__sym[1] = ys + msizey2/2.0;
			__sym[2] = ys + msizey2;
		}
		else {
			__sym[1] = ys - msizey2/2.0;
			__sym[2] = ys - msizey2;
		}
		__sxm[2] = xs;
		__sxm[3] = xs - msizex2/2.0;	__sym[3] = __sym[1];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.FDIA4 ) {
		__sxm[0] = xs - msizex2/2.0;
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2/2.0;
			__sym[2] = ys + msizey2/2.0;
		}
		else {
			__sym[0] = ys + msizey2/2.0;
			__sym[2] = ys - msizey2/2.0;
		}
		__sxm[1] = xs;			__sym[1] = ys;
		__sxm[2] = __sxm[0];
		__sxm[3] = xs - msizex2;		__sym[3] = ys;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_DOWN_FILLED ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		__sxm[0] = xs - msizex2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = xs;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		if (outlineColor != null) {
			da.setColor(outlineColor);
			da.drawPolygon ( 3, __sxm, __sym );
		}
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.DIAMOND_LEFT_HALF_FILLED ) {
		__sxm[0] = xs;
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		__sxm[1] = xs - msizex2;	__sym[1] = ys;
		__sxm[2] = xs;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.FLDIA4 ) {
		__sxm[0] = xs - msizex2/2.0;
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2/2.0;
			__sym[1] = ys + msizey2/2.0;
		}
		else {
			__sym[0] = ys + msizey2/2.0;
			__sym[1] = ys - msizey2/2.0;
		}
		__sxm[1] = __sxm[0];
		__sxm[2] = xs - msizex2;		__sym[2] = ys;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_LEFT_FILLED ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys;
		__sxm[1] = xs + msizex2;
		if ( da._reverse_y ) {
			__sym[1] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		else {
			__sym[1] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		__sxm[2] = __sxm[1];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		if (outlineColor != null) {
			da.setColor(outlineColor);
			da.drawPolygon ( 3, __sxm, __sym );
		}
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.DIAMOND_RIGHT_HALF_FILLED ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		__sxm[0] = xs;
		__sxm[1] = xs + msizex2;	__sym[1] = ys;
		__sxm[2] = xs;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.FRDIA4 ) {
		__sxm[0] = xs + msizex2;		__sym[0] = ys;
		__sxm[1] = xs + msizex2/2.0;
		if ( da._reverse_y ) {
			__sym[1] = ys + msizey2/2.0;
			__sym[2] = ys - msizey2/2.0;
		}
		else {
			__sym[1] = ys - msizey2/2.0;
			__sym[2] = ys + msizey2/2.0;
		}
		__sxm[2] = __sxm[1];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_RIGHT_FILLED ) {
		__sxm[0] = xs - msizex2;
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		__sxm[1] = xs + msizex2;	__sym[1] = ys;
		__sxm[2] = __sxm[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		if (outlineColor != null) {
			da.setColor(outlineColor);
			da.drawPolygon ( 3, __sxm, __sym );
		}
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SLASH_FORWARD ) {
		__sxm[0] = xs + msizex2;
		__sxm[1] = xs - msizex2;
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[1] = ys + msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
			__sym[1] = ys - msizey2;
		}
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SQUARE_FILLED ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys + msizey2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = __sxm[1];		__sym[2] = ys - msizey2;
		__sxm[3] = __sxm[0];		__sym[3] = __sym[2];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.RECTANGLE_HORIZONTAL_FILLED ) {
		// Horizontal dimension is twice the vertical dimension.
		__sxm[0] = xs - msizex2;	__sym[0] = ys + msizey2/2.0;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = __sxm[1];		__sym[2] = ys - msizey2/2.0;
		__sxm[3] = __sxm[0];		__sym[3] = __sym[2];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.RECTANGLE_VERTICAL_FILLED ) {
		// Horizontal dimension is half the vertical dimension.
		__sxm[0] = xs - msizex2/2.0;	__sym[0] = ys + msizey2;
		__sxm[1] = xs + msizex2/2.0;	__sym[1] = __sym[0];
		__sxm[2] = __sxm[1];		__sym[2] = ys - msizey2;
		__sxm[3] = __sxm[0];		__sym[3] = __sym[2];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SQUARE_TRIANGLE_UPPER_RIGHT_FILLED ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
		}
		__sxm[0] = xs + msizex2;
		__sxm[1] = __sxm[0];		__sym[1] = ys;
		__sxm[2] = xs;		__sym[2] = __sym[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SQUARE_TRIANGLE_LOWER_RIGHT_FILLED ) {
		if ( da._reverse_y ) {
			__sym[1] = ys + msizey2;
		}
		else {
			__sym[1] = ys - msizey2;
		}
		__sxm[0] = xs + msizex2;	__sym[0] = ys;
		__sxm[1] = __sxm[0];
		__sxm[2] = xs;		__sym[2] = __sym[1];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SQUARE_TRIANGLE_LOWER_LEFT_FILLED ) {
		if ( da._reverse_y ) {
			__sym[0] = ys + msizey2;
		}
		else {
			__sym[0] = ys - msizey2;
		}
		__sxm[0] = xs;
		__sxm[1] = xs - msizex2;	__sym[1] = __sym[0];
		__sxm[2] = __sxm[1];		__sym[2] = ys;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SQUARE_TRIANGLE_UPPER_LEFT_FILLED ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
		}
		__sxm[0] = xs;
		__sxm[1] = xs - msizex2;	__sym[1] = ys;
		__sxm[2] = __sxm[1];		__sym[2] = __sym[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.DIAMOND_TOP_HALF_FILLED ) {
		__sxm[0] = xs + msizex2;	__sym[0] = ys;
		__sxm[1] = xs - msizex2;	__sym[1] = ys;
		__sxm[2] = xs;
		if ( da._reverse_y ) {
			__sym[2] = ys - msizey2;
		}
		else {
			__sym[2] = ys + msizey2;
		}
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.FTOPDIA4 ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2/2.0;
			__sym[2] = ys - msizey2;
		}
		else {
			__sym[0] = ys + msizey2/2.0;
			__sym[2] = ys + msizey2;
		}
		__sxm[0] = xs + msizex2/2.0;
		__sxm[1] = xs - msizex2/2.0;	__sym[1] = __sym[0];
		__sxm[2] = xs;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_UP_FILLED ) {
		if ( da._reverse_y ) {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		else {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		__sxm[0] = xs - msizex2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = xs;
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 3, __sxm, __sym );
		if (outlineColor != null) {
			da.setColor(outlineColor);
			da.drawPolygon ( 3, __sxm, __sym );
		}
		//}
		//else {	fillPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.INSTREAM_FLOW ) {
		drawSymbol ( da, GRSymbolShapeType.LINE_BOTTOM, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.BAR, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.ARROW_LEFT ) {
		drawSymbol ( da, GRSymbolShapeType.CARET_LEFT, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.MINUS, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.CARET_LEFT ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		__sxm[0] = xs;
		__sxm[1] = xs - msizex2;	__sym[1] = ys;
		__sxm[2] = xs;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolyline ( 3, __sxm, __sym );
		//}
		//else {	drawPolyline ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SQUARE_LEFT_FILLED ) {
		drawSymbol ( da, GRSymbolShapeType.SQUARE, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[1] = ys + msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
			__sym[1] = ys - msizey2;
		}
		__sxm[0] = xs;
		__sxm[1] = __sxm[0];
		__sxm[2] = xs - msizex2;	__sym[2] = __sym[1];
		__sxm[3] = __sxm[2];		__sym[3] = __sym[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.LINE_LEFT ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys + msizey2;
		__sxm[1] = __sxm[0];		__sym[1] = ys - msizey2;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_LEFT ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys;
		__sxm[1] = xs + msizex2;
		if ( da._reverse_y ) {
			__sym[1] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		else {
			__sym[1] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		__sxm[2] = __sxm[1];
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolygon ( 3, __sxm, __sym );
		//}
		//else {	drawPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.MINUS ) {	// -
		__sxm[0] = xs - msizex2;	__sym[0] = ys;
		__sxm[1] = xs + msizex2;	__sym[1] = ys;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.NONE ) {
		// Draw nothing
	}
	else if ( symbol == GRSymbolShapeType.PLUS ) {
		drawSymbol ( da, GRSymbolShapeType.MINUS, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.BAR, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.PLUS_SQUARE ) {
		drawSymbol ( da, GRSymbolShapeType.PLUS, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.SQUARE, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.CIRCLE_PLUS ) {
		drawSymbol ( da, GRSymbolShapeType.PLUS, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.CIRCLE, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.ARROW_RIGHT ) {
		drawSymbol ( da, GRSymbolShapeType.CARET_RIGHT, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.MINUS, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.CARET_RIGHT ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		__sxm[0] = xs;
		__sxm[1] = xs + msizex2;	__sym[1] = ys;
		__sxm[2] = xs;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolyline ( 3, __sxm, __sym );
		//}
		//else {	drawPolyline ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SQUARE_RIGHT_FILLED ) {
		drawSymbol ( da, GRSymbolShapeType.SQUARE, x, y, size_x, size_y, offset_x, offset_y, data, flag, orient );
		__sxm[0] = xs;		__sym[0] = ys - msizey2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = __sxm[1];		__sym[2] = ys + msizey2;
		__sxm[3] = __sxm[0];		__sym[3] = __sym[2];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.LINE_RIGHT ) {
		__sxm[0] = xs + msizex2;	__sym[0] = ys + msizey2;
		__sxm[1] = __sxm[0];		__sym[1] = ys - msizey2;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_RIGHT ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys + msizey2;
		__sxm[1] = xs + msizex2;	__sym[1] = ys;
		__sxm[2] = __sxm[0];		__sym[2] = ys - msizey2;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolygon ( 3, __sxm, __sym );
		//}
		//else {	drawPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.SQUARE ) {
		__sxm[0] = xs - msizex2;	__sym[0] = ys - msizey2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = __sxm[1];		__sym[2] = ys + msizey2;
		__sxm[3] = __sxm[0];		__sym[3] = __sym[2];
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolygon ( 4, __sxm, __sym );
		//}
		//else {	drawPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.RECTANGLE_HORIZONTAL ) {
		// Horizontal dimension is twice the vertical dimension.
		__sxm[0] = xs - msizex2;	__sym[0] = ys - msizey2/2.0;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = __sxm[1];		__sym[2] = ys + msizey2/2.0;
		__sxm[3] = __sxm[0];		__sym[3] = __sym[2];
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolygon ( 4, __sxm, __sym );
		//}
		//else {	drawPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.RECTANGLE_VERTICAL ) {
		// Horizontal dimension is half the vertical dimension.
		__sxm[0] = xs - msizex2/2.0;	__sym[0] = ys - msizey2;
		__sxm[1] = xs + msizex2/2.0;	__sym[1] = __sym[0];
		__sxm[2] = __sxm[1];		__sym[2] = ys + msizey2;
		__sxm[3] = __sxm[0];		__sym[3] = __sym[2];
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolygon ( 4, __sxm, __sym );
		//}
		//else {	drawPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if (symbol == GRSymbolShapeType.TEACUP) {
		double msizex4 = msizex2 / 2;
		double msizexx = -1;
		double pct = -1;

		if (props != null) {
			double fill = (data[2] - data[1]) / (data[0] - data[1]);
			pct = msizey * fill;
			msizexx = msizex4 * fill;
			__sxm[0] = xs - msizex4 - msizexx;
			__sym[0] = ys + msizey2 - pct;
			__sxm[1] = xs - msizex4;
			__sym[1] = ys + msizey2;
			__sxm[2] = xs + msizex4;
			__sym[2] = ys + msizey2;
			__sxm[3] = xs + msizex4 + msizexx;
			__sym[3] = ys + msizey2 - pct;
			da.fillPolygon(4, __sxm, __sym);

			da.setColor(GRColor.black);

			da.setLineWidth(2.0);
		}

		__sxm[0] = xs - msizex2;	__sym[0] = ys - msizey2;
		__sxm[1] = xs - msizex4;	__sym[1] = ys + msizey2;
		da.drawLine(__sxm, __sym);

		__sxm[0] = xs - msizex4;	__sym[0] = ys + msizey2;
		__sxm[1] = xs + msizex4;	__sym[1] = ys + msizey2;
		da.drawLine(__sxm, __sym);

		__sxm[0] = xs + msizex4;	__sym[0] = ys + msizey2;
		__sxm[1] = xs + msizex2;	__sym[1] = ys - msizey2;
		da.drawLine(__sxm, __sym);

		if (props != null) {
			da.setLineWidth(1.0);
			__sxm[0] = xs - msizex4 - msizexx;
			__sym[0] = ys + msizey2 - pct;
			__sxm[1] = xs + msizex4 + msizexx;
			__sym[1] = ys + msizey2 - pct;
			da.drawLine(__sxm, __sym);
		}
	}
	else if ( symbol == GRSymbolShapeType.SQUARE_TOP_FILLED ) {
		drawSymbol ( da, GRSymbolShapeType.SQUARE, x, y, size_x, size_y, offset_x, offset_y, data, flag, orient );
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
		}
		__sxm[0] = xs + msizex2;
		__sxm[1] = __sxm[0];		__sym[1] = ys;
		__sxm[2] = xs - msizex2;	__sym[2] = ys;
		__sxm[3] = __sxm[2];		__sym[3] = __sym[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.fillPolygon ( 4, __sxm, __sym );
		//}
		//else {	fillPolygon ( da, 4, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.LINE_TOP ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
		}
		__sxm[0] = xs - msizex2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		//if ( flag == GRUnits.DEVICE ) {
			da.drawLine ( __sxm, __sym );
		//}
		//else {	drawLine ( da, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.ARROW_UP ) {
		drawSymbol ( da, GRSymbolShapeType.CARET_UP, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
		drawSymbol ( da, GRSymbolShapeType.BAR, x, y, size_x, size_y,
			offset_x, offset_y, data, flag, orient );
	}
	else if ( symbol == GRSymbolShapeType.CARET_UP ) {
		if ( da._reverse_y ) {
			__sym[1] = ys - msizey2;
		}
		else {
			__sym[1] = ys + msizey2;
		}
		__sxm[0] = xs - msizex2;	__sym[0] = ys;
		__sxm[1] = xs;
		__sxm[2] = xs + msizex2;	__sym[2] = ys;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolyline ( 3, __sxm, __sym );
		//}
		//else {	drawPolyline ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_UP ) {
		if ( da._reverse_y ) {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		else {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		__sxm[0] = xs - msizex2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = xs;
		//if ( flag == GRUnits.DEVICE ) {
			da.drawPolygon ( 3, __sxm, __sym );
		//}
		//else {	drawPolygon ( da, 3, __sxm, __sym );
		//}
	}
	else if ( symbol == GRSymbolShapeType.VERTICAL_BAR_SIGNED ) {
		// Draw a polygon counter-clockwise starting from lower left.
		double height = msizey;
		if ( (data != null) && (data.length > 0) ) {
			// First data value is expected to be a fraction ( (0.0 - 1.0) of the overall height.
			height = msizey*data[0];
		}
		if ( da._reverse_y ) {
			__sym[2] = ys - height;
		}
		else {
			__sym[2] = ys + height;
		}
		__sxm[0] = xs - msizex2;	__sym[0] = ys;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = __sxm[1];
		__sxm[3] = __sxm[0];		__sym[3] = __sym[2];
		da.fillPolygon ( 4, __sxm, __sym );
	}
	else if ( symbol == GRSymbolShapeType.VERTICAL_BAR_UNSIGNED ) {
		// Draw a polygon counter-clockwise starting from lower left.
		double height = msizey;
		if ( (data != null) && (data.length > 0) ) {
			// First data value is expected to be a fraction (0.0 - 1.0) of the overall height.
			height = msizey*data[0];
		}
		if ( da._reverse_y ) {
			__sym[2] = ys - height;
		}
		else {
			__sym[2] = ys + height;
		}
		__sxm[0] = xs - msizex2;	__sym[0] = ys;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = __sxm[1];
		__sxm[3] = __sxm[0];		__sym[3] = __sym[2];
		da.fillPolygon ( 4, __sxm, __sym );
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_UP_FILLED_TOP_LINE ) {
		if ( da._reverse_y ) {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		else {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		__sxm[0] = xs - msizex2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = xs;
		da.fillPolygon ( 3, __sxm, __sym );
		if (outlineColor != null) {
			da.setColor(outlineColor);
			da.drawPolygon ( 3, __sxm, __sym );
		}
		__sxm[0] = xs - msizex2;	__sxm[1] = xs + msizex2;
		if (da._reverse_y) {
			__sym[0] = __sym[1] = ys + msizey2;
		}
		else {
			__sym[0] = __sym[1] = ys - msizey2;
		}
		da.drawLine(__sxm, __sym);
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_UP_TOP_LINE ) {
		if ( da._reverse_y ) {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		else {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		__sxm[0] = xs - msizex2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = xs;
		da.drawPolygon ( 3, __sxm, __sym );
		__sxm[0] = xs - msizex2;	__sxm[1] = xs + msizex2;
		if (outlineColor != null) {
			da.setColor(outlineColor);
		}
		if (da._reverse_y) {
			__sym[0] = __sym[1] = ys + msizey2;
		}
		else {
			__sym[0] = __sym[1] = ys - msizey2;
		}
		da.drawLine(__sxm, __sym);
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_DOWN_FILLED_BOTTOM_LINE ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		else {
			__sym[0] = ys + msizey2;
			__sym[2] = ys - msizey2;
		}
		__sxm[0] = xs - msizex2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = xs;
		da.fillPolygon ( 3, __sxm, __sym );
		if (outlineColor != null) {
			da.setColor(outlineColor);
			da.drawPolygon ( 3, __sxm, __sym );
		}
		__sxm[0] = xs - msizex2;	__sxm[1] = xs + msizex2;
		if (da._reverse_y) {
			__sym[0] = __sym[1] = ys + msizey2;
		}
		else {
			__sym[0] = __sym[1] = ys - msizey2;
		}
		da.drawLine(__sxm, __sym);
	}
	else if ( symbol == GRSymbolShapeType.TRIANGLE_DOWN_BOTTOM_LINE ) {
		if ( da._reverse_y ) {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		else {
			__sym[0] = ys - msizey2;
			__sym[2] = ys + msizey2;
		}
		__sxm[0] = xs - msizex2;
		__sxm[1] = xs + msizex2;	__sym[1] = __sym[0];
		__sxm[2] = xs;
		da.drawPolygon ( 3, __sxm, __sym );
		__sxm[0] = xs - msizex2;	__sxm[1] = xs + msizex2;
		if (outlineColor != null) {
			da.setColor(outlineColor);
		}
		if (da._reverse_y) {
			__sym[0] = __sym[1] = ys + msizey2;
		}
		else {
			__sym[0] = __sym[1] = ys - msizey2;
		}
		da.drawLine(__sxm, __sym);
	}
	else if ( symbol == GRSymbolShapeType.PUSHPIN_VERTICAL ) {
		double [] sxm = new double[12];
		double [] sym = new double[12];
		double ySign = 1.0;
		if ( da._reverse_y ) {
			ySign = -1.0;
		}
		double pinWidth = msizex*.08;
		double outsideWidth = msizex*.5;
		double insideWidth = msizex*.3;
		double bottomRimHeight = .1*msizey;
		double topRimHeight = .1*msizey;
		double pinLength = .4*msizey;
		double handleLength = .3*msizey; // Does not include rim.
		// Pushpin always has point on the symbol point.
		// Point.
		sxm[0] = xs;	sym[0] = ys;
		// Left base of point.
		sxm[1] = xs - pinWidth/2.0;	sym[1] = ys + ySign*pinLength;
		// Left edge of bottom.
		sxm[2] = sxm[0] - outsideWidth/2.0;	sym[2] = sym[1];
		// Top left of bottom rim.
		sxm[3] = sxm[0] - insideWidth/2.0;	sym[3] = sym[2] + ySign*bottomRimHeight;
		// Bottom left of top rim.
		sxm[4] = sxm[3];	sym[4] = sym[3] + ySign*handleLength;
		// Top left of top rim.
		sxm[5] = sxm[2];	sym[5] = sym[4] + ySign*topRimHeight;
		// Right edge is symmetrical with left.
		sxm[6] = sxm[5] + outsideWidth;	sym[6] = sym[5];
		sxm[7] = sxm[4] + insideWidth;	sym[7] = sym[4];
		sxm[8] = sxm[7];	sym[8] = sym[3];
		sxm[9] = sxm[6];	sym[9] = sym[2];
		sxm[10] = xs + pinWidth;	sym[10] = sym[1];
		sxm[11] = sxm[0];	sym[11] = sym[0];
		da.fillPolygon ( 12, sxm, sym );
		if (outlineColor != null) {
			da.setColor(outlineColor);
		}
		da.drawPolyline( 12, sxm, sym);
	}
	else {
		Message.printWarning ( 3, "drawSymbol", "Symbol " + symbol + " is not implemented." );
	}
}

/**
Draw multiple symbols.
@param symbol the symbol to draw
@param nsymbols the number of symbols to draw
@param x array of x coordinates
@param y array of y coordinates
@param size the size of the symbol
@param flag the symbol flag
@param orient the symbol orientation
*/
public static void drawSymbols ( GRDrawingArea da, GRSymbolShapeType symbol, int nsymbols, double[] x, double[] y,
	double size, int flag, int orient ) {
	if ( (x == null) || (y == null) ) {
		return;
	}
	for ( int i = 0; i < nsymbols; i++ ) {
		drawSymbol ( da, symbol, x[i], y[i], size, flag, orient );
	}
}

/**
Draw symbol and text, adjusting the text position for the symbol.
The adjustment is done in the low-level driver code.  The text color is the same as the symbol color.
@param da Drawing area to draw to.
@param symbol Symbol to draw.
@param x Data coordinate for symbol.
@param y Data coordinate for symbol.
@param size Size of symbol.
@param text Text to draw at symbol.
@param a Angle of text.
@param tflag Text orientation flags (see GRText).
@param flag Units of symbol (GRUnits.DEVICE or GRUnits.DATA).  The symbol size will be scaled appropriately.
@param orient Symbol orientation (see GRGRSymbolShapeType.SYM.*);
*/
public static void drawSymbolText ( GRDrawingArea da, GRSymbolShapeType symbol, double x, double y,
	double size, String text, double a, int tflag, int flag, int orient ) {
	drawSymbolText ( da, symbol, x, y, size, text, null, a, tflag, flag, orient );
}

/**
Draw symbol and text, adjusting the text position for the symbol.
The adjustment is done in the low-level driver code.
@param da Drawing area to draw to.
@param symbol Symbol to draw (see GRSymbolShapeType.SYM*).
If less than 0, no symbol will be drawn (though the text still will be).
@param x Data coordinate for symbol.
@param y Data coordinate for symbol.
@param size Size of symbol.
@param text Text to draw at symbol.  Can be null (no text will be drawn).
@param text_color Color to use for text.  The drawing area color change is not persistent.
@param a Angle of text.
@param tflag Text orientation flags (see GRText).
@param flag Units of symbol (GRUnits.DEVICE or GRUnits.DATA).  The symbol size will be scaled appropriately.
@param orient Symbol orientation (see GRGRSymbolShapeType.SYM.*);
*/
public static void drawSymbolText (	GRDrawingArea da, GRSymbolShapeType symbol, double x, double y,
	double size, String text, GRColor text_color, double a, int tflag, int flag, int orient ) {
	double xt = x, yt = y;
	// Always draw the symbol as specified.

	if ( (symbol != null) && (symbol != GRSymbolShapeType.NONE) ) {
		drawSymbol ( da, symbol, x, y, size, flag, orient );
	}
	if (text == null) {
		return;
	}
	// Get the symbol size in data units.
	double xsize_data = 0.0, ysize_data = 0.0;
	GRPoint pt1, pt2;
	if ( flag == GRUnits.DATA ) {
		// Symbol size is specified in data units.
		xsize_data = size;
		ysize_data = size;
	}
	else {
		// Symbol size is specified in device units.  Use a unit size to interpolate the size.
		pt1 = da.getDataXY(0.0,0.0,GRCoordinateType.DEVICE);
		pt2 = da.getDataXY(size,size,GRCoordinateType.DEVICE);
		xsize_data = pt2.x - pt1.x;
		ysize_data = pt2.y - pt1.y;
	}
/*
	if ( xsize_data < 0.0 ) {
		xsize_data *= -1.0;
	}
	if ( ysize_data < 0.0 ) {
		ysize_data *= -1.0;
	}
	Message.printStatus ( 1, "drawSymboText", "Symbol size in data units is " + xsize_data + "," + ysize_data );
*/
	// Get symbol diameter.
	double xsize_data2 = xsize_data/2.0;
	double ysize_data2 = ysize_data/2.0;
	// Shift x...
	if ( (tflag&GRText.LEFT) != 0 ) {
		// Left justify.
		xt = x + xsize_data2;
	}
	else if ( (tflag&GRText.RIGHT) != 0 ) {
		// Right justify.
		xt = x - xsize_data2;
	}
	// Shift y.
	if ( (tflag&GRText.BOTTOM) != 0 ) {
		// Shift down.
		yt = y - ysize_data2;
	}
	else if ( (tflag&GRText.TOP) != 0 ) {
		// Shift up.
		yt = y + ysize_data2;
	}
	//Message.printStatus ( 1, "drawSymboText", "x,y = " + x + "," + y + " xt,yt=" + xt + "," + yt );
	GRColor color_save = null;
	if ( text_color != null ) {
		// Save the old color and reset for the text color.
		color_save = da._color;
		// Have to do this because _graphics is modified.
		da.setColor ( text_color );
	}
	drawText ( da, text, xt, yt, a, tflag );
	if ( text_color != null ) {
		// Reset the drawing area color to the saved color.
		da.setColor ( color_save );
	}
}

/**
Draw text.
@param da the drawing area to draw on
@param text the text to draw
@param x the x coordinate
@param y the y coordinates
@param a the angle of rotation, counter-clockwise
@param flag the text orientation flags (see GRText.*)
*/
public static void drawText ( GRDrawingArea da, String text, double x, double y, double a, int flag) {
	drawText(da, text, x, y, a, flag, 0);
}

/**
Draw text.
If the text contains the character '\n' it will be treated as newlines and will be used to draw several lines or text,
each below the last line drawn.
@param da the drawing area to draw on
@param text the text to draw
@param x the x coordinate
@param y the y coordinates
@param a the transparency
@param flag the text orientation flags (see GRText.*)
@param degrees the degrees of rotation (this parameter is actually used in the Swing code, and the rotation is clockwise from east)
*/
public static void drawText ( GRDrawingArea da, String text, double x,
	double y, double a, int flag, double degrees) {
	double	xs, ys;

	// Scale the data.

	xs = da.scaleXData ( x );
	ys = da.scaleYData ( y );
	if ( Message.isDebugOn ) {
		Message.printDebug ( 25, "drawText", "At " + x + "," + y +
		" (" + xs + "," + ys + ") text \"" + text + "\"" );
	}

	// Check to see if this has one line or several.

	String separator = "\n";
	int index = text.indexOf(separator);
	if (index == -1) {
		// Just one line of text.
		da.drawText ( text, xs, ys, a, flag, degrees );
		da.setLastXY ( x, y );
		return;
	}

	// Separate out each line.
	List<String> v = new ArrayList<>();
	boolean done = false;
	String s = null;
	int len = separator.length();
	while (!done) {
		s = text.substring(0, index);
		v.add(s);
		text = text.substring(index + len);
		index = text.indexOf(separator);
		if (index == -1) {
			done = true;
		}
	}
	v.add(text);

	// Put the lines into an array for quicker traversal.
	int size = v.size();
	String[] lines = new String[size];
	for (int i = 0; i < size; i++) {
		lines[i] = v.get(i);
	}

	// Calculate the limits of each individual lines and put it in an array.
	// Also keep track of the widest text.  Heights should all be the same.
	GRLimits[] limits = new GRLimits[size];
	double widest = 0;
	for (int i = 0; i < size; i++) {
		limits[i] = getTextExtents(da, lines[i], GRUnits.DEVICE);
		if (limits[i].getWidth() > widest) {
			widest = limits[i].getWidth();
		}
	}

	int xPos = 0;
	int yPos = 0;

	// Separate out the specific flags denoting location because they will be recombined later,
	// and also for quicker access below.

	if ((flag & GRText.TOP) == GRText.TOP) {
		yPos = GRText.TOP;
	}
	else if ((flag & GRText.BOTTOM) == GRText.BOTTOM) {
		yPos = GRText.BOTTOM;
	}
	else {
		yPos = GRText.CENTER_Y;
	}

	if ((flag & GRText.RIGHT) == GRText.RIGHT) {
		xPos = GRText.RIGHT;
	}
	else if ((flag & GRText.LEFT) == GRText.LEFT) {
		xPos = GRText.LEFT;
	}
	else {
		xPos = GRText.CENTER_X;
	}

	// Used with text centered around a Y-point to tell whether there are an even number or odd number of lines of text.
	// Even-numbered centered text is handled specially.
	boolean even = false;

	// The top Y point from which drawing the lines begins.
	double startingY = 0;

	// In the following,
	// remember that by this point the X and Y values have been scaled and converted into Java point coordinates,
	// so that // the Y values at the top of the screen are smaller and get bigger as they go down the screen.

	if (yPos == GRText.TOP) {
		// If the text is drawn relative to its top, then the starting Y is the y value passed in.
		startingY = ys;
	}
	else if (yPos == GRText.BOTTOM) {
		// Otherwise, need to calculate relative to the last line that will be drawn for bottom-aligned text.
		// The starting y is the bottom of the first line of text.
		// Need to calculate the total height of all the other lines and subtract it from the specified Y to find that value.
		double total = 0;
		for (int i = 1; i < size; i++) {
			total += limits[i].getHeight();
		}
		startingY = ys - total;
	}
	else {
		// For centered text, the Y value depends on whether there are an even or odd number of lines of text to be drawn.
		int start = size / 2;
		if (size % 2 == 0) {
			// If there are an even number of lines of text,
			// then the center of all the text lies between the middle two lines.
			// the center is at the BOTTOM of the line that is in the array at position ((size / 2) - 1).
			// So in this case the starting Y position will be calculated the the bottom of the
			// 0th line and then the Y position flag will be changed from CENTER_Y to BOTTOM,
			// since text will be drawn from the BOTTOM of each line.
			even = true;
			double total = 0;
			for (int i = start - 1; i > 0; i--) {
				total += limits[i].getHeight();
			}
			flag = xPos | GRText.BOTTOM;
			startingY = ys - total;
		}
		else {
			// If there are an odd number of lines of text,
			// then need to calculate the center of the 0th line of text. That's the starting Y.
			// Each additional line is at a Y value equal to half the current line's
			// height plus half the previous line's height.
			double total = (limits[start].getHeight() / 2);
			for (int i = start - 1; i > 0; i--) {
				total += limits[i].getHeight();
			}
			total += (limits[0].getHeight() / 2);
			startingY = ys - total;
		}
	}

	// Go through and draw each line.

	double currY = startingY;
	for (int i = 0; i < size; i++) {
		// The original drawing code can still be used (so no recursion).
		da.drawText(lines[i], xs, currY, 0, flag, 0);

		// The next section calculates the Y value for the next line of text,
		// given the y positioning type specified in the flag.

		if (yPos == GRText.TOP) {
			currY += limits[i].getHeight();
		}
		else if (yPos == GRText.BOTTOM || even == true) {
			if (i < (size - 1)) {
				currY += limits[i + 1].getHeight();
			}
		}
		else {
			if (i < (size - 1)) {
				currY += (limits[i].getHeight() / 2);
				currY += (limits[i + 1].getHeight() / 2);
			}
		}
	}
}

/**
Fill the entire drawing area with the specified color.
The color becomes the active color for the drawing area.
@param color GRColor to fill the drawing are with.
@see GRColor
*/
public static void fill ( GRDrawingArea da, GRColor color ) {
	// Get the plotting limits in device units.
	GRLimits limits = da.getPlotLimits ( GRCoordinateType.DATA );
	// Set the color.
	da.setColor ( color );
	// Can call the drawing area method directly since the units are already for the device.
	da.fillRectangle ( limits );
	limits = null;
}

/**
Draw a filled circular/elliptical arc around a point.
@param da Drawing area.
@param x X-coordinate of center.
@param y Y-coordinate of center.
@param rx Radius in x direction.
@param ry Radius in y direction.
@param a1 Angle at which drawing is to begin (degrees), counter-clockwise from 3 o'clock.  Thus:<br>
<pre>
     90
180 	  0
     270
</pre>
@param a2 Angle at which drawing is to end (degrees), clockwise from 3 o'clock.  Thus:<br>
<pre>
     90
180 	  0
     270
</pre>
@param fillmode Indicates how arc is to be filled.
*/
public static void fillArc ( GRDrawingArea da, double x, double y, double rx,
				double ry, double a1, double a2, GRArcFillType fillmode ) {
	double xs = da.scaleXData ( x );
	double ys = da.scaleYData ( y );
	double rxs = da.scaleXData(rx) - da.scaleXData(0.0);
	double rys = da.scaleYData(ry) - da.scaleYData(0.0);
	if ( rxs < 0.0 ) {
		rxs *= -1.0;
	}
	if ( rys < 0.0 ) {
		rys *= -1.0;
	}
	da.fillArc ( xs, ys, rxs, rys, a1, a2, fillmode );
	da.setLastXY ( x, y );
}

/**
Draw a filled circular/elliptical arc around a point.
@param da Drawing area.
@param arc GRArc to fill.
@param fillmode indicates how arc is to be filled.
*/
public static void fillArc ( GRDrawingArea da, GRArc arc, GRArcFillType fillmode ) {
	double xs = da.scaleXData ( arc.pt.x );
	double ys = da.scaleYData ( arc.pt.y );
	double rxs = da.scaleXData(arc.xradius) - da.scaleXData(0.0);
	double rys = da.scaleYData(arc.yradius) - da.scaleYData(0.0);
	if ( rxs < 0.0 ) {
		rxs *= -1.0;
	}
	if ( rys < 0.0 ) {
		rys *= -1.0;
	}
	da.fillArc ( xs, ys, rxs, rys, arc.angle1, arc.angle2, fillmode );
	da.setLastXY ( arc.pt.x, arc.pt.y );
}

/**
Fill a polygon with the current color.
@param da Drawing area.
@param polygon GRPolygon to fill.
*/
public static void fillPolygon ( GRDrawingArea da, GRPolygon polygon ) {
	if ( polygon.npts == 0 ) {
		return;
	}
	double [] xs = new double[polygon.npts];
	double [] ys = new double[polygon.npts];
	for ( int i = 0; i < polygon.npts; i++ ) {
		xs[i] = da.scaleXData ( polygon.pts[i].x );
		ys[i] = da.scaleYData ( polygon.pts[i].y );
	}
	// Reduce the number of points.
	//GRReducePoints ( xs, ys, &npts, 0 );
	da.fillPolygon ( polygon.npts, xs, ys );
	da.setLastXY ( polygon.pts[polygon.npts - 1].x, polygon.pts[polygon.npts - 1].y );
	xs = null;
	ys = null;
}

/**
Fill a polygon with the current color and a degree of transparency.
@param da Drawing area.
@param polygon GRPolygon to fill.
@param transparency Indicates transparency (255=transparent, 0=opaque).
*/
public static void fillPolygon ( GRDrawingArea da, GRPolygon polygon, int transparency ) {
	if ( polygon.npts == 0 ) {
		return;
	}
	double [] xs = new double[polygon.npts];
	double [] ys = new double[polygon.npts];
	for ( int i = 0; i < polygon.npts; i++ ) {
		xs[i] = da.scaleXData ( polygon.pts[i].x );
		ys[i] = da.scaleYData ( polygon.pts[i].y );
	}

	// Reduce the number of points.
	//GRReducePoints ( xs, ys, &npts, 0 );
	da.fillPolygon ( polygon.npts, xs, ys, transparency );
	da.setLastXY ( polygon.pts[polygon.npts - 1].x, polygon.pts[polygon.npts - 1].y );
}

/**
Fill a polygon given a drawing area and polygon information in data units.
@param da GR drawing area.
@param npts Number of points in the polygon.
@param x X-coordinates of points.
@param y Y-coordinates of points.
*/
public static void fillPolygon ( GRDrawingArea da, int npts, double x[], double y[] ) {
	double [] xs, ys;
	int i;

	if ( npts == 0 ) {
		return;
	}
	xs = new double[npts];
	ys = new double[npts];
	for ( i = 0; i < npts; i++ ) {
		xs[i] = da.scaleXData ( x[i] );
		ys[i] = da.scaleYData ( y[i] );
	}
	// Reduce the number of points.
	//GRReducePoints ( xs, ys, &npts, 0 );
	da.fillPolygon ( npts, xs, ys );
	da.setLastXY ( x[npts - 1], y[npts - 1] );
}

/**
Fill a rectangle given a drawing area and rectangle information in data units.
@param da GR drawing area.
@param xll X-coordinates of lower left corner of rectangle.
@param yll Y-coordinates of lower left corner of rectangle.
@param width Width of rectangle (can be negative, in which case xll will be recomputed).
@param height Height of rectangle (can be negative, in which case yll will be recomputed).
*/
public static void fillRectangle ( GRDrawingArea da, double xll, double yll, double width, double height ) {
	double [] xs, ys;

	xs = new double[4];
	ys = new double[4];
	// Scale the data.  Allow the width and height to be negative and adjust the dimensions accordingly.
	xs[0] = da.scaleXData ( xll );
	xs[1] = da.scaleXData ( xll + width );
	xs[2] = xs[1];
	xs[3] = xs[0];

	ys[0] = da.scaleYData ( yll );
	ys[1] = ys[0];
	ys[2] = da.scaleYData ( yll + height );
	ys[3] = ys[2];
/*
	if ( width >= 0.0 ) {
		xs[0] = da.scaleXData ( xll );
		xs[1] = da.scaleXData ( xll + width );
	}
	else {
		xs[0] = da.scaleXData ( xll + width );
		xs[1] = da.scaleXData ( xll );
	}
	// Same no matter what.
	xs[2] = xs[1];
	xs[3] = xs[0];
	if ( height >= 0.0 ) {
		ys[0] = da.scaleXData ( yll );
		ys[2] = da.scaleXData ( yll + height );
	}
	else {
		ys[0] = da.scaleYData ( yll + height );
		xs[2] = da.scaleYData ( yll );
	}
	// Same no matter what.
	ys[1] = ys[0];
	ys[3] = xs[2];
*/
	da.fillPolygon ( 4, xs, ys );
	da.setLastXY ( xll, yll );
}

/**
Returns the clipping shape on the current graphics context.
@return the clipping shape on the current graphics context.
*/
public static Shape getClip(GRDrawingArea da) {
	return da.getClip();
}

/**
Returns the data extents given a delta in DA units.
This routine takes as input delta-x and delta-y values and calculates the corresponding data extents.
This is useful when it is known (guessed?) that output needs to be,
for example, 15 points high but it is not known what the corresponding data values are.
This can be used, for example, to draw a box around text.
The flags need to be implemented to allow the extents to be determined
exactly at the limits given, as the centroid of the drawing area, etc.
For now, calculate at the centroid so that projection issues do not cause problems.
@param limits the limits for the drawing area.
@param flag indicates whether units should be returned in device or data units.
TODO (JTS - 2003-05-05) This parameter isn't even used.  SAM: in the future
@return the data extents given a delta in DA units.
*/
public static GRLimits getDataExtents (	GRDrawingArea da, GRLimits limits, int flag ) {
	return da.getDataExtents ( limits, flag );
}

/**
Returns the extents of a string in either data or device units.
If the text has the character '\n',
it will be treated as line separators and the extents returned will take into account all the lines of text.
Rotation is assumed to be zero.
@return the extents of a string in either data or device units.
@param da Drawing area.
@param text String to determine size.
@param flag GRUnits.DATA or GRUnits.DEVICE, indicating the units that should be returned for the text size.
*/
public static GRLimits getTextExtents(GRDrawingArea da, String text, int flag) {
	return getTextExtents(da, text, 0.0, flag);
}

/**
Returns the extents of a string in either data or device units.
If the text has the character '\n',
it will be treated as line separators and the extents returned will take into account all the lines of text.
@return the extents of a string in either data or device units.
@param da Drawing area.
@param text String to determine size.
@param rotation Rotation of the text, were 0 is due east (horizontal), measured clockwise in degrees.
@param flag GRUnits.DATA or GRUnits.DEVICE, indicating the units that should be returned for the text size.
*/
public static GRLimits getTextExtents(GRDrawingArea da, String text, double rotation, int flag) {
	// Check to see if there are any newline markers.
	String separator = "\n";
	int index = text.indexOf(separator);
	boolean oneLine = false;
	if (index == -1) {
		oneLine = true;
	}

	// If one line then there are no newline characters and the old code will work.
	if (oneLine) {
		// The following always returns device units.
		GRLimits limits = da.getTextExtents ( text, flag );
		if ( limits == null ) {
			return null;
		}
		if ( flag == GRUnits.DEVICE ) {
			return limits;
		}
		else {
			// Data.
			GRPoint pt1 = da.getDataXY(0.0,0.0, GRCoordinateType.DEVICE);
			GRPoint pt2 = da.getDataXY(0.0,limits.getHeight(), GRCoordinateType.DEVICE);
			double height = pt1.y - pt2.y;
			if ( height < 0 ) {
				height *= -1.0;
			}
			pt2 = da.getDataXY(limits.getWidth(), 0.0, GRCoordinateType.DEVICE);
			double width = pt1.x - pt2.x;
			if ( width < 0 ) {
				width *= -1.0;
			}
			return new GRLimits ( width, height );
		}
	}

	// Now calculate the extents for multiple lines of text.

	List<String> v = new ArrayList<>();
	boolean done = false;
	String s = null;
	int len = separator.length();
	while (!done) {
		s = text.substring(0, index);
		v.add(s);
		text = text.substring(index + len);
		index = text.indexOf(separator);
		if (index == -1) {
			done = true;
		}
	}
	v.add(text);

	int size = v.size();
	String[] lines = new String[size];
	for (int i = 0; i < size; i++) {
		lines[i] = v.get(i);
	}

	double widest = 0;
	double height = 0;
	GRLimits limits = null;
	for (int i = 0; i < size; i++) {
		limits = getTextExtents(da, lines[i], flag);
		if (limits.getWidth() > widest) {
			widest = limits.getWidth();
		}
		height += limits.getHeight();
	}

	return new GRLimits(widest, height);
}

/**
Returns the extents for text drawn in a specific font.
@param da the drawing area on which to find the extents.
@param text the text string for which to find the extents.
@param flag one of GRUnits.DEVICE or GRUnits.DATA.
@param fontname the name of the font the text is in.
@param style the style the text is.
@param size the size of the text.
@return GRLimits describing the extent of the font.
*/
public static GRLimits getTextExtents ( GRDrawingArea da, String text,
	int flag, String fontname, String style, int size) {
	// The following always returns device units.
	setFont(da, fontname, style, (double)size);
	return getTextExtents(da, text, flag);
}

/**
Checks to see if this drawing area's device is drawing antialised.
@return true if the device is drawing anti aliased, false if not.
*/
public static boolean isDeviceAntiAliased(GRDrawingArea da) {
	return da.isDeviceAntiAliased();
}

/**
Draw a line to a point.
@param x data X coordinate.
@param y data Y coordinate
*/
public static void lineTo ( GRDrawingArea da, double x, double y ) {
	da.lineTo ( da.scaleXData(x), da.scaleYData(y) );
}

/**
Move to a point.  The next lineTo() call will draw from this point.
@param x data X coordinate.
@param y data Y coordinate
*/
public static void moveTo ( GRDrawingArea da, double x, double y ) {
	da.moveTo ( da.scaleXData(x), da.scaleYData(y) );
}

/**
End of page (flush page).
TODO (SAM - 2003-05-07) Seems like we might want to have to Device code but leave here for now
@param da Drawing area.
*/
public static void pageEnd ( GRDrawingArea da ) {
	da.comment ( "Ending page (initiated by DA " + da.getName() + ")");
	da.pageEnd ();
}

/**
Start of page (setup page).
TODO (SAM - 2003-05-07) Seems like we might want to have to Device code but leave here for now
@param da Drawing area.
*/
public static void pageStart ( GRDrawingArea da ) {
	da.comment ( "Starting page (initiated by DA " + da.getName() + ")" );
	da.pageStart ();
}

/**
Sets whether this drawing area's device should begin drawing antialiased.
Currently, on GRJComponentDevice Objects support this.
@param antiAlias if true, the device will be told to begin drawing antialiased.
*/
public static void setDeviceAntiAlias(GRDrawingArea da, boolean antiAlias) {
	da.setDeviceAntiAlias(antiAlias);
}

/**
Set the type of axis for a drawing area.
@param da Drawing area.
@param xaxis X-axis type (see GRAxis.LINEAR, etc)
@param yaxis Y-axis type (see GRAxis.LINEAR, etc)
*/
public static void setAxes ( GRDrawingArea da, GRAxisScaleType xaxis, GRAxisScaleType yaxis ) {
	da._axisx = xaxis;
 	da._axisy = yaxis;
	if ( da._drawset ) {
		da.setPlotLimits ();
	}
}

/**
Sets the clipping area, in data limits.  If null, the clip is removed.
@param dataLimits the limits of the data area that will be clipped.
*/
public static void setClip(GRDrawingArea da, GRLimits dataLimits) {
	da.setClip(dataLimits);
}

/**
Sets the clipping shape.  If null, the clip is removed.
@param clipShape the shape to clip to.
*/
public static void setClip(GRDrawingArea da, Shape clipShape) {
	da.setClip(clipShape);
}

/**
Set color for all drawing.
@param da Drawing area.
@param c Color to draw with.
*/
public static void setColor ( GRDrawingArea da, GRColor c ) {
	int	dl = 30;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, "setColor", "Request RGB: " + c.getRed()+","+ c.getGreen()+","+c.getBlue());
	}
	da.setColor ( c );
}

/**
Set the font and its height to be used in drawing area.  A plain style font is used.
@param da Drawing area.
@param font Font name (e.g., "Helvetica").
@param fontht Font height, in points (1 inch=72 points).
*/
public static void setFont ( GRDrawingArea da, String font, double fontht ) {
	da.setFont ( font, "Plain", fontht );
}

/**
Set the font and its height and style to be used in drawing area.
@param da Drawing area.
@param font Font name (e.g., "Helvetica").
@param style Font style ("Plain", "Bold", "Italic").
@param fontht Font height, in points (1 inch=72 points).
*/
public static void setFont ( GRDrawingArea da, String font, String style, double fontht ) {
	da.setFont ( font, style, fontht );
}

/**
Set the line dash for line-drawing commands.
TODO (SAM - 2003-05-07) Need to see what Graphics2D has to offer.
This version maintained for historic reasons.
*/
public static void setLineDash ( GRDrawingArea da, int ndash, double [] dash, double offset ) {
	setLineDash ( da, dash, offset );
}

/**
Set the line dash for line-drawing commands.
@param da the drawing area on which to set the line dash
@param dash a double array specifying the dash pattern.  If null, line dashes will be turned off.
@param offset the initial dash offset.
*/
public static void setLineDash ( GRDrawingArea da, double [] dash, double offset ) {
	da.setLineDash ( dash, offset );
}

/**
Sets how lines are joined, for GRJComponentDrawingAreas.
@param da the drawing area on which to set the line join style.
@param join the line join style (one of java.awt.BasicStroke.JOIN_BEVEL,
BasicStroke.JOIN_MITER, or BasicStroke.JOIN_ROUND).
*/
public static void setLineJoin(GRJComponentDrawingArea da, int join) {
	da.setLineJoin(join);
}

/**
Set the line width to be used in drawing area.
@param da the drawing area on which to set the line join style.
@param lineWidth the width of the line
*/
public static void setLineWidth(GRDrawingArea da, double lineWidth) {
	da.setLineWidth ( lineWidth);
}

/**
Set the line width to be used for GRJComponentDrawingAreas.
@param da the drawing area on which to set the line join style.
@param lineWidth the width of the line
*/
public static void setLineWidth(GRJComponentDrawingArea da, int lineWidth) {
	da.setLineWidth(lineWidth);
}

/**
Set the line cap to be used for GRJComponentDrawingAreas.
@param da the drawing area on which to set the line join style.
@param cap the cap style to use (one of java.awt.BasicStroke.CAP_BUTT,
BasicStroke.CAP_ROUND, BasicStroke.CAP_SQUARE).
*/
public static void setLineCap(GRJComponentDrawingArea da, int cap) {
	da.setLineCap(cap);
}

}