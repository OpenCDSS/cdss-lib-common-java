//------------------------------------------------------------------------------
// BigPictureLayer - store data for big picture layer (multiple bar symbols)
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:	(1) Assumes first column is the id (String), second column is
//			the name (String), and remaining columns are doubles
//------------------------------------------------------------------------------
// History:
// 
// 29 Jun 1999	Catherine E.		Created initial version of class.
//		Nutting-Lane, RTi	
// 21 Sep 2001	Steven A. Malers, RTi	Change Table to DataTable.
// 2001-10-18	SAM, RTi		Review javadoc.  Add finalize().  Set
//					unused data to null.  Remove unused
//					classes in imports.
// 2002-06-20	SAM, RTi		Update to have a join field for the
//					related layers, which is used to relate
//					to other layers.  Previously the join
//					field was assumed to be the first field
//					(position 0) but this does not work with
//					shapefiles!
//------------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.util.List;
import RTi.Util.Table.DataTable;
import RTi.Util.Message.Message;
import RTi.GR.GRLimits;

/**
The BigPictureLayer is used with the StateMod GUI to display one or more bars
associated with the StateMod delplt utility program output.  The functionality
of this class will be migrated to the standard GeoView classes so that compound
symbols can be more generally applied.
*/
public class BigPictureLayer extends GeoLayer {

private List<GeoLayer> _parent_geolayers;
private List<String> _parent_join_fields;
private DataTable _big_picture_table;
private GRLimits _big_picture_limits;

/**
Constructor.
@param filename Comma-delimited file used to create the DataTable.
@param parent_geolayers Vector of GeoLayer being drawn.  These supply the
spatial information that is matched against the attributes.
@param parent_join_fields Field names in the parent layers that contain the
identifier field to join to.
@param bigPictureTable DataTable containing delimited data.  The first field
is the identifier, the second the name, and 3+ contain data attributes that can be plotted.
*/
public BigPictureLayer( String filename, List<GeoLayer> parent_geolayers,
			List<String> parent_join_fields, DataTable bigPictureTable )
{	super ( filename );
	initialize( parent_geolayers, parent_join_fields, bigPictureTable );
}

/**
Calculates the max and min values for all data fields  (2...n)
*/
public static GRLimits computeBigPictureLimits ( DataTable bigPictureTable )
throws Exception
{	int num_records = bigPictureTable.getNumberOfRecords();
	int num_fields = bigPictureTable.getNumberOfFields();
	double ytmp;

	double ymin = ((Double)bigPictureTable.getFieldValue ( 0, 2 )).doubleValue();
	double ymax = ((Double)bigPictureTable.getFieldValue ( 0, 2 )).doubleValue();

	for ( int i=0; i<num_records; i++ ) {
		for ( int j=2; j<num_fields; j++ ) {
			ytmp = ((Double)bigPictureTable.getFieldValue( i, j )).doubleValue();
			if ( ytmp > ymax ) {
				ymax = ytmp;
			}
			if ( ytmp < ymin ) {
				ymin = ytmp;
			}
		}
	}

	return new GRLimits ( 0, ymin, 1, ymax );
}

/**
Clean up for garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	_parent_geolayers = null;
	_parent_join_fields = null;
	_big_picture_table = null;
	_big_picture_limits = null;
	super.finalize();
}

public DataTable getBigPictureTable () {
	return _big_picture_table;
}

private void initialize ( List<GeoLayer> parent_geolayers, List<String> parent_join_fields, DataTable bigPictureTable ) {
	_parent_geolayers = parent_geolayers;
	_parent_join_fields = parent_join_fields;
	_big_picture_table = bigPictureTable;
	setShapeType ( BIG_PICTURE );
	try {	
		GRLimits limits = parent_geolayers.get(0).getLimits();
		for ( int i=1; i < parent_geolayers.size(); i++ ) {
			limits = limits.max ( parent_geolayers.get(i).getLimits());
		}
		setLimits ( limits );
	}
	catch ( Exception e2 ) {
		Message.printWarning ( 1, "BigPictureLayer",
		"Error computing drawing limits of big picture data." );
	}
	try {
		_big_picture_limits = computeBigPictureLimits ( bigPictureTable );
	}
	catch ( Exception e )  {
		Message.printWarning ( 1, "BigPictureLayer", "Problems computing limits of big picture data." );
	}
}

public DataTable getAttributeTable(int index) {
	return ((GeoLayer)_parent_geolayers.get(index)).
		getAttributeTable();
}

public GRLimits getBigPictureLimits ()
{	return _big_picture_limits;
}

public String getJoinField(int index) {
	return (String)_parent_join_fields.get(index);
}

public int getNumAssociatedLayers ()
{	return _parent_geolayers.size();
}

public List getShapes(int index) {
	return ((GeoLayer)_parent_geolayers.get(index)).getShapes();
}

}