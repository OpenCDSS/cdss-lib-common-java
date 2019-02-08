// GeoGrid - GRGrid grid that also has data and methods to treat as GIS layer

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
// GeoGrid - GRGrid grid that also has data and methods to treat as GIS layer
// ----------------------------------------------------------------------------
// History:
//
// 2001-09-17	Steven A. Malers,	Initial version.  Add basic features to
//		Riverside Technology,	support extension to Xmrg and precip
//		inc.			grid.
// 2001-10-08	SAM, RTi		Review javadoc.
// 2001-12-13	SAM, RTi		Add getUnits().
// 2004-09-09	J. Thomas Sapienza, RTi	Added the resize() method which can be
//					use to clip the grid to a smaller area
//					or to enlarge the grid.
// 2004-11-10	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.io.IOException;

import RTi.GR.GRGrid;

/**
The GeoGrid class defines a grid object for storing double-precision data.  The
grid shape information (number of rows, origin, etc.) is stored in the GRGrid
base class.  This base class defines the conventions for identifying cells,
rows, and columns.  Methods typically use column and then row as parameters to
correspond to X and Y directions.  Data are stored in this class.  The reading
and writing of specific grid formats should be implemented in classes extended
from GeoGridLayer.
*/
public class GeoGrid extends GRGrid
{

/**
Data array for grid data.  The first dimension is grid row, the second is grid column.
*/
protected double _double_data[][] = null;

/**
Missing data value.
*/
protected double _missing = -999.0;

/**
Maximum value in the grid.  Currently this is not used for anything.
*/
protected double _max_value = -999.0;

/**
Number of positive values in the grid (useful for precipitation, etc.).
Currently this data member can be set and retrieved but is not calculated in this class.
*/
protected int _num_positive_values = 0;

/**
Units for data in grid (e.g., "MM").  These units should be consistent with the
units used in the RTi.TS package.
*/
protected String _units = "";

/**
Create a grid of zero size.  The size can be set using the setSize() and
setSizeFull() methods in GRGrid.
*/
public GeoGrid ()
{	super();
}

/**
Allocate the data space for the grid data, using the active data space.
This allocates the double[][] array to hold data.  This method should only be
called if data are to be held in memory.  In many cases, the grid data values
will be processed on the fly and will never by held in memory.
*/
public void allocateDataSpace ()
{	_double_data = new double[_max_row - _min_row + 1][_max_column - _min_column + 1];

	// Initialize with missing data...

	int r = 0, c = 0;
	for ( r = _min_row; r <= _max_row; r++) {
		for ( c=_min_column; c <= _max_column; c++ ) {
			//use the value for missing data
			_double_data[r - _min_row][c - _min_column] = _missing;
		}
	}
}

/**
Cleans up the member variables.
*/
public void finalize()
throws Throwable {
	_double_data = null;
	_units = null;
	super.finalize();
}

/**
Get a data value for a cell.  This method should be defined in a derived class
if on-the-fly data reading is enabled.  Otherwise, the data must always be read into memory.
@param column Column of cell to get value for.
@param row Row of cell to get value for.
@return Data value for cell.
@exception IOException if there is an error getting the data value.
*/
public double getDataValue ( int column, int row )
throws IOException
{	return _double_data[row - _min_row][column - _min_column];
}

public double getAbsDataValue ( int column, int row )
throws IOException
{	return _double_data[row][column];
}

/**
Return the value used to indicate missing data.  The default value is -999.
@return the value used to indicate missing data.
*/
public double getMissing()
{	return _missing;
}

/**
Return the number of positive data values in the active grid.
@return the number of positive data values in the active grid.
*/
public int getNumberOfPositiveValues ()
{	return _num_positive_values;
}

/**
Return the data units.
@return the data units.
*/
public String getUnits()
{	return _units;
}

/**
Resizes the grid.  If the new grid goes outside the boundary of the original 
grid, the cells which were not in the original grid will be filled with missing values.
@param leftX the X value that will be the new origin X.
@param bottomY the Y value that will be the new origin Y.
@param numColumns the number of columns in the new grid. 
@param numRows the number of rows in the new grid.
@throws Exception if there is an error transferring data into the resized grid.
@throws Exception if the number of rows or columns is less than 1.
*/
public void resize(int leftX, int bottomY, int numColumns, int numRows)
throws Exception {
	if (numColumns < 1) {
		throw new Exception("Invalid number of columns: " + numColumns);
	}

	if (numRows < 1) {
		throw new Exception("Invalid number of rows: " + numRows);
	}


	if (leftX == _min_column && bottomY == _min_row 
	    && (leftX + numColumns - 1) == _max_column
	    && (bottomY + numRows - 1) == _max_row) {
		// the size didn't change, so do nothing
		return;
	}

	double[][] d = new double[numRows][numColumns];

	int j = 0;
	for (int i = 0; i < numColumns; i++) {	
		for (j = 0; j < numRows; j++) {
			d[j][i] = _missing;

			if ((leftX + i) < _min_column || (leftX + i) > _max_column) {
		    	// out of bounds!  Let it be missing.
				continue;
			}

			if ((bottomY + j) < _min_row || (bottomY + j) > _max_row) {
		    	// out of bounds!  Let it be missing.
				continue;
			}			

			d[j][i] = getDataValue(leftX + i, bottomY + j);
		}
	}

	_min_column_full = _min_column = leftX;
	_min_row_full = _min_row = bottomY;
	_max_column_full = _max_column = leftX + numColumns - 1;
	_max_row_full = _max_row = bottomY + numRows - 1;

	_double_data = d;
}

/**
Set a data value for a cell.  This should only be called if the data are read
into memory.  The method can also be defined in a derived class if on-the-fly
writing to a file is needed.
@param column Column of cell to set value for.
@param row Row of cell to set value for.
@param value Data value to set.
*/
public void setDataValue ( int column, int row, double value )
{	_double_data[row - _min_row][column - _min_column] = value;
}

/**
Set the value used to indicate missing data.
@param missing the value used to indicate missing data.
*/
public void setMissing ( double missing )
{	_missing = missing;
}

/**
Set the number of positive values.  This data member can be manipulated by
derived classes but is not internally set.
@param num_positive_values Number of positive values in the grid.
*/
public void setNumberOfPositiveValues ( int num_positive_values )
{	_num_positive_values = num_positive_values;
}

/**
Set the data units for the grid.
@param units Data units for the grid.
*/
public void setUnits ( String units )
{	_units = units;
}

}
