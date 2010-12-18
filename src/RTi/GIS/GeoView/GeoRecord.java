// ----------------------------------------------------------------------------
// GeoRecord - class to hold a join of tabular and shape data
// ============================================================================
// Copyright:	See the COPYRIGHT file.
// ============================================================================
// Notes:	(1)	This is somewhat similar to the Avenue FTab except that
//			we also keep track of the layer for the data so that
//			we can pass vectors of GeoRecords from mixed layers.
// ----------------------------------------------------------------------------
// History:
//
// 14 Jul 1999	Steven A. Malers	Initial version.
//		Riverside Technology,
//		inc.
// 2001-10-04	SAM, RTi		Add GeoLayerView as data member.
//					Although this is not strictly needed,
//					it is useful for some GUI tasks (such
//					as displaying a GeoLayerView name with
//					GeoRecord information).
// 2001-10-17	SAM, RTi		Update javadoc.  Make sure unused data
//					is set to null.  Remove initialize()
//					method since everything is initialized
//					to null at construction.
// ----------------------------------------------------------------------------
// 2003-05-06	J. Thomas Sapienza, RTi	Ensure class matched the non-swing
//					version of the class.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import RTi.GR.GRShape;
import RTi.Util.Table.TableRecord;

/**
This class provides a record for geographic (shape) and tabular data.  GeoLayer
and GeoLayerView references are also maintained in case mixed layers are used
in a list.  In the future, data members may be made public to increase performance.
*/
public class GeoRecord
{

/**
Shape for GeoRecord.
*/
protected GRShape _shape = null;

/**
Table record for GeoRecord.
*/
protected TableRecord _record = null;

/**
GeoLayer that the record is queried from.
*/
protected GeoLayer _layer = null;

/**
GeoLayerView that the record is queried from.
*/
protected GeoLayerView _layer_view = null;

/**
Construct an empty GeoRecord (null shape, table record, GeoLayer, and GeoLayerView).
*/
public GeoRecord ()
{
}

/**
Construct a GeoRecord using the given shape, table record, GeoLayer, and GeoLayerView.
@param shape GRShape associated with the record.
@param record TableRecord associated with the record.
@param layer GeoLayer associated with the record.
@param layer_view GeoLayerView associated with the record.
*/
public GeoRecord ( GRShape shape, TableRecord record, GeoLayer layer, GeoLayerView layer_view )
{	_layer = layer;
	_layer_view = layer_view;
	_record = record;
	_shape = shape;
}

/**
Construct a GeoRecord using the given shape, table record, and layer.
@param shape GRShape associated with the record.
@param record TableRecord associated with the record.
@param layer GeoLayer associated with the record.
*/
public GeoRecord ( GRShape shape, TableRecord record, GeoLayer layer )
{	_layer = layer;
	_record = record;
	_shape = shape;
}

/**
Return the GeoLayer for the GeoRecord.
@return the layer for the GeoRecord (can be null).
*/
public GeoLayer getLayer ()
{	return _layer;
}

/**
Return the GeoLayerView for the GeoRecord.
@return the layer view for the GeoRecord (can be null).
*/
public GeoLayerView getLayerView ()
{	return _layer_view;
}

/**
Return the shape for the record.
@return the shape for the GeoRecord (can be null).
*/
public GRShape getShape ()
{	return _shape;
}

/**
Return the TableRecord for the record.
@return the table record for the GeoRecord (can be null).
*/
public TableRecord getTableRecord ()
{	return _record;
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	_layer = null;
	_layer_view = null;
	_record = null;
	_shape = null;
	super.finalize();
}

/**
Set the layer for the GeoRecord (can be null).
@param layer GeoLayer for record.
*/
public void setLayer ( GeoLayer layer )
{	_layer = layer;
}

/**
Set the layer view for the GeoRecord (can be null).
@param layer_view GeoLayerView for record.
*/
public void setLayerView ( GeoLayerView layer_view )
{	_layer_view = layer_view;
}

/**
Set the shape for the GeoRecord (can be null).
@param shape GRShape for the record.
*/
public void setShape ( GRShape shape )
{	_shape = shape;
}

/**
Set the table record for the GeoRecord (can be null).
@param record TableRecord for the record.
*/
public void setTableRecord ( TableRecord record )
{	_record = record;
}

}