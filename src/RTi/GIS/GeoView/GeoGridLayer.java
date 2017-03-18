// -----------------------------------------------------------------------------
// GeoGridLayer - GeoLayer to store grid data
// -----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file
// -----------------------------------------------------------------------------
// History:
//
// 2001-10-02	Steven A. Malers, RTi	Create this class to handle generic
//					output of shapefiles.  Most other
//					functionality is in GeoLayer, GeoGrid,
//					GRGrid, and derived classes like
//					XmrgGridLayer.
// 2001-10-08	SAM, RTi		Review javadoc.
// 2002-12-19	SAM, RTi		Add setDataValue().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// -----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import RTi.GR.GRShape;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;

/**
The GeoGridLayer class extends GeoLayer and stores GeoGrid data using a Vector
of GRGrid.  Although it is possible that a Vector of GRGrid could be saved,
currently only a single GRGrid shape is typically stored in the shape list (e.g.,
for use by XmrgGridLayer).  This class implements methods that can be used for
any grid-based layer, such as saving the cells with > 0 data values as a shapefile.
*/
public class GeoGridLayer extends GeoLayer
{

/**
Grid to hold the data.
*/
private GeoGrid __grid = null;

/**
Constructor.
@param filename Name of layer file.
*/
public GeoGridLayer ( String filename )
{	super ( filename );
}

/**
Clean up for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__grid = null;
	super.finalize();
}

/**
Get the data value for a column and row.  This method should be defined in
a derived class to take advantage of on-the-fly reading.  If not defined and
accessed in a derived class, the GeoGrid.getDataValue() method is called.
@param column Column of cell to read data for.
@param row Row of cell to read data for.
@exception IOException if there is an error reading the data.
*/
public double getDataValue ( int column, int row )
throws IOException
{	return __grid.getDataValue ( column, row );
}

/**
Returns the grid containing the data.
@return the grid containing the data.
*/
public GeoGrid getGrid() {
	return __grid;
}

/**
Set the data value for a column and row.  This method should be defined in
a derived class to take advantage of on-the-fly writing.  If not defined and
accessed in a derived class, the GeoGrid.setDataValue() method is called.
@param column Column of cell to read data for.
@param row Row of cell to read data for.
@param value Value to set for the row and cell.
@exception IOException if there is an error setting the data value.
*/
public void setDataValue ( int column, int row, double value )
throws IOException
{	__grid.setDataValue ( column, row, value );
}

/**
Set the grid containing the data.
@return the grid containing the data.
*/
public void setGrid ( GeoGrid grid )
{
	__grid = grid;
}

/**
Write an ESRIShapefile.  This method exists in this class to allow a shapefile
to be created for any GeoGridLayer.  The actual writing of the files occurs in
ESRIShapefile but the packaging of the necessary data occurs in this class.
Minimum and maximum data values can be specified to allow a range of cells to be written.
@param filename Name of shapefile to write (with or without .shp).
@param to_projection Projection that data should be written.
@param use_data_limits If true, then the min_data_value and max_data_value
values are checked.  Only cells with data in the limits are output.
@param min_data_value Minimum cell data value to consider when writing.
@param max_data_value Maximum cell data value to consider when writing.
@exception IOException if there is an error writing the shapefile.
*/
public void writeShapefile ( String filename, GeoProjection to_projection,
	boolean use_data_limits, double min_data_value, double max_data_value )
throws IOException
{	// Create the DataTable from the grid.  Note that GeoLayer (the base
	// class) has an _attribute_table object.  However, at this time it
	// is probably ok to just create one when we need rather than try to
	// carry around.  This may change if we allow the attribute table to
	// be viewed in a GUI, etc.

	List<TableField> fields = new Vector<TableField> ( 3 );
	fields.add ( new TableField(TableField.DATA_TYPE_INT, "COLUMN", 10, 0 ) );
	fields.add ( new TableField(TableField.DATA_TYPE_INT, "ROW", 10, 0 ) );
	fields.add ( new TableField(TableField.DATA_TYPE_DOUBLE, "VALUE", 10, 4 ) );
	DataTable table = new DataTable ( fields );
	fields = null;

	// Now create the polygons to write to the shapefile.  In the future it
	// might be possible to let the ESRIShapefile class know more about
	// the GRID shape but for now create a list of GRPolygon that can
	// be written in ESRIShapefile.

	List<GRShape> shapes = new Vector<GRShape>();	// could optimize more
	int c = 0;
	double value = 0.0;
	TableRecord record = null;
	int min_row = __grid.getMinRow();
	int min_column = __grid.getMinColumn();
	int max_row = __grid.getMaxRow();
	int max_column = __grid.getMaxColumn();
	for ( int r = min_row; r <= max_row; r++ ) {
		for ( c = min_column; c <= max_column; c++ ) {
			try {
				value = getDataValue ( c, r );
			}
			catch ( Exception e ) {
				continue;
			}
			if ( use_data_limits && ((value < min_data_value) || (value > max_data_value)) ) {
				continue;
			}
			// Create the shape...
			shapes.add ( __grid.getCellPolygon ( c, r ) );
			// Create the attribute record...
			record = new TableRecord(3);
			record.addFieldValue ( new Integer(c) );
			record.addFieldValue ( new Integer(r) );
			record.addFieldValue ( new Double(value) );
			try {
				table.addRecord ( record );
			}
			catch ( Exception e ) {
				// Should never happen.
			}
		}
	}

	// Write the shapefile.  The "from" projection is just this layer's projection...

	ESRIShapefile.write ( filename, table, shapes, getProjection(), to_projection );
}

}