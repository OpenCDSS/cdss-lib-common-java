// BigPictureLayer - store data for big picture layer (multiple bar symbols)

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package RTi.GIS.GeoView;

import java.util.List;
import RTi.Util.Table.DataTable;
import RTi.Util.Message.Message;
import RTi.GR.GRLimits;
import RTi.GR.GRShape;

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
	return _parent_geolayers.get(index).getAttributeTable();
}

public GRLimits getBigPictureLimits ()
{	return _big_picture_limits;
}

public String getJoinField(int index) {
	return _parent_join_fields.get(index);
}

public int getNumAssociatedLayers ()
{	return _parent_geolayers.size();
}

public List<GRShape> getShapes(int index) {
	return _parent_geolayers.get(index).getShapes();
}

}
