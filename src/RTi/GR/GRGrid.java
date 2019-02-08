// GRGrid - GR grid 

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

// ----------------------------------------------------------------------------
// GRGrid - GR grid 
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 17 Sep 2001	Steven A. Malers, RTi	Initial Java version.  Only support
//					regular grids at this time.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GR;

/**
This class stores a two-dimensional regular or irregular grid.  All grid lines
are parallel in the data's projection but the distance between grid lines does
not need to be constant.  This class can be used by GIS or other classes to
store gridded data but this class only saves information about the grid layout,
not the data itself.  The grid always represents coordinates with the origin at
the lower left, as shown in the following figure.  Because grid files can
contain more data than is necessary for viewing/processing, the class allows
active minimum and maximum coordinates to be set.  These coordinates are for
the lower-left and the upper-right cells.  Note that when identifying cells, the
cell should be identified by the row and column coordinates of the cell (not
the edge).

The full grid is used with *Full() methods.  Otherwise, the methods use the
active grid.  The coordinates of row and column edges are defined by floating
point values and can be set by using the GRShape base class xmin, ymin, xmax,
ymax data members.  Methods to set edge values for irregular data have not yet
been implemented.
<pre>
                      +----+----+----+----+----+----+
                      |    |    |    |    |    |    |
                      +----+----+----+----+----+----+
                      |    |    |XXXX|XXXX|    |    |
     increasing X ^   +----+----+----+----+----+----+
                      |    |    |XXXX|XXXX|    |    |
                      +----+----+----+----+----+----+
                      |    |    |    |    |    |    |
                      +----+----+----+----+----+----+
               origin/
                            increasing Y ->
</pre>
Currently this class includes functionality only for regular grids and only
integer coordinates are used.
*/
public class GRGrid extends GRShape
{

/**
Minimum cell column for active grid.
*/
protected int _min_column = 0;
/**
Minimum cell row for active grid.
*/
protected int _min_row = 0;
/**
Maximum cell column for active grid.
*/
protected int _max_column = 0;
/**
Maximum cell row for active grid.
*/
protected int _max_row = 0;
/**
Cell width when regular grid.
*/
protected double _cell_width = 0.0;
/**
Cell height when regular grid.
*/
protected double _cell_height = 0.0;

/**
Minimum cell column for full grid.
*/
protected int _min_column_full = 0;
/**
Minimum cell row for full grid.
*/
protected int _min_row_full = 0;
/**
Maximum cell column for full grid.
*/
protected int _max_column_full = 0;
/**
Maximum cell row for full grid.
*/
protected int _max_row_full = 0;

/**
Cell column edge limits if irregular grid.
*/
protected double _x[] = null;
/**
Cell row edge limits if irregular grid.
*/
protected double _y[] = null;

/**
Overall minimum x coordinate for full grid.
*/
protected double _xmin_full = 0.0;
/**
Overall minimum y coordinate for full grid.
*/
protected double _ymin_full = 0.0;
/**
Overall maximum x coordinate for full grid.
*/
protected double _xmax_full = 0.0;
/**
Overall maximum y coordinate for full grid.
*/
protected double _ymax_full = 0.0;
/**
Column edge x coordinates if irregular grid.
*/
protected double _x_full[] = null;
/**
Row edge y coordinates if irregular grid.
*/
protected double _y_full[] = null;

/**
Construct and initialize to grid with no rows or columns with an origin (0.0).
*/
public GRGrid ()
{	super ();
	type = GRID;
}

/**
Indicate if the requested column and row are in the active grid space.
@param column Column to check.
@param row Row to check.
@return true if the indicated column and row are in the active grid, false if
not.
*/
public boolean contains ( int column, int row )
{	if (	(column >= _min_column) && (column <= _max_column) &&
		(row >= _min_row) && (row <= _max_row) ) {
		return true;
	}
	return false;
}

/**
Finalize before garbage collection.
@throws Throwable if an error.
*/
protected void finalize ()
throws Throwable
{	_x = null;
	_y = null;
	_x_full = null;
	_y_full = null;
	super.finalize();
}

/**
Return a polygon of 5 points to represent a a single grid cell.  The order of
points in the polygon is clockwise, starting in the SouthWest corner (southwest,
northwest, northeast, southeast, southwest).  The dimension of the cell is
controlled by the grid dimensions.  The units are those of the grid data.
@param column Cell column of interest.
@param row Cell row of interest.
@return  A GRPolygon with coordinates lat/long coordinates.
*/
public GRPolygon getCellPolygon ( int column, int row )
{	double x_sw = 0.0;
	double y_sw = 0.0;
	double x_nw = 0.0;
	double y_nw = 0.0;
	double x_ne = 0.0;
	double y_ne = 0.0;
	double x_se = 0.0;
	double y_se = 0.0;
	if ( _x == null ) {
		// Regular grid (only one currently supported... 
		x_sw = xmin + (column - _min_column)*_cell_width;
		y_sw = ymin + (row - _min_row)*_cell_height;
		x_nw = x_sw;
		y_nw = y_sw + _cell_height;
		x_ne = x_sw + _cell_width;
		y_ne = y_sw + _cell_height;
		x_se = x_sw + _cell_width;
		y_se = y_sw;
	}
	GRPolygon cellPolygon = new GRPolygon(5);
	cellPolygon.setPoint(0, new GRPoint(x_sw,y_sw) );
	cellPolygon.setPoint(1, new GRPoint(x_nw,y_nw) );
	cellPolygon.setPoint(2, new GRPoint(x_ne,y_ne) );
	cellPolygon.setPoint(3, new GRPoint(x_se,y_se) );
	cellPolygon.setPoint(4, new GRPoint(x_sw,y_sw) );
	return cellPolygon;
}

/**
Return the maximum column in the active grid.
@return maximum column in the active grid.
*/
public int getMaxColumn ()
{	return _max_column;
}

/**
Return the maximum column in the full grid.
@return the maximum column in the full grid.
*/
public int getMaxColumnFull ()
{	return _max_column_full;
}

/**
Return the maximum row in the active grid.
@return the maximum row in the active grid.
*/
public int getMaxRow ()
{	return _max_row;
}

/**
Return the maximum row in the full grid.
@return the maximum row in the full grid.
*/
public int getMaxRowFull ()
{	return _max_row_full;
}

/**
Return the minimum column in the active grid.
@return the minimum column in the active grid.
*/
public int getMinColumn ()
{	return _min_column;
}

/**
Return the minimum column in the full grid.
@return the minimum column in the full grid.
*/
public int getMinColumnFull ()
{	return _min_column_full;
}

/**
Return the minimum row in the active grid.
@return the minimum row in the active grid.
*/
public int getMinRow ()
{	return _min_row;
}

/**
Return the minimum row in the full grid.
@return the minimum row in the full grid.
*/
public int getMinRowFull ()
{	return _min_row_full;
}

/**
Return the number of columns in the active grid.
@return the number of columns in the active grid.
*/
public int getNumberOfColumns()
{	return (_max_column - _min_column + 1);
}

/**
Return the number of columns in the full grid.
@return the number of columns in the full grid.
*/
public int getNumberOfColumnsFull()
{	return (_max_column_full - _min_column_full + 1);
}

/**
Return the number of rows in the active grid.
@return the number of rows in the active grid.
*/
public int getNumberOfRows()
{	return (_max_row - _min_row + 1);
}

/**
Return the number of rows in the full grid.
@return the number of rows in the full grid.
*/
public int getNumberOfRowsFull()
{	return (_max_row_full - _min_row_full + 1);
}

/**
Set the active size of the grid.  <b>Call this method after setting the
minimum X and Y coordinates.</b>
@param min_column Left-most column.
@param min_row Bottom-most row.
@param max_column Right-most column.
@param max_row Top-most row.
*/
public void setSize ( int min_column, int min_row, int max_column, int max_row )
{	_min_column = min_column;
	_min_row = min_row;
	_max_column = max_column;
	_max_row = max_row;
	if ( (_max_column - _min_column + 1) > 0 ) {
		_cell_width = (xmax - xmin)/(_max_column - _min_column + 1);
	}
	if ( (_max_row - _min_row + 1) > 0 ) {
		_cell_height = (ymax - ymin)/(_max_row - _min_row + 1);
	}
}

/**
Set the full size of the grid.
@param min_column Left-most column.
@param min_row Bottom-most row.
@param max_column Right-most column.
@param max_row Top-most row.
*/
public void setSizeFull (	int min_column, int min_row, int max_column,
				int max_row )
{	_min_column_full = min_column;
	_min_row_full = min_row;
	_max_column_full = max_column;
	_max_row_full = max_row;
}

} // End GRGrid class
