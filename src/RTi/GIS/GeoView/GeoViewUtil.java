package RTi.GIS.GeoView;

import RTi.Util.IO.PropList;

/**
Static utility methods for geographic processing.
*/
public class GeoViewUtil
{

/**
Create a blank layer that has an empty shape and attribute table list.
This is useful when adding new layers (that have not yet been persisted, or as a
place-holder when a bad data location is specified.
@param filename Name of layer file (will be set but the file will not be read).
*/
public static GeoLayer newLayer ( String filename )
{
	GeoLayer layer = new GeoLayer( (PropList)null ); // No properties initially
	layer.setFileName ( filename );
	return layer;
}

/**
Create a blank layer view that has an empty shape and attribute table list.
This is useful when adding new layers (that have not yet been persisted, or as a
place-holder when a bad data location is specified.
The layer is first initialized and then default symbol properties are assigned based on the count of the layers.
An empty attributes table is assigned.
@param filename Name of layer file.
@param props Properties (see GeoLayerView constructor).
@param count Count of layers being added.  This affects the default symbols
that are assigned.  The first value should be 1.  <b>This is not the
GeoLayerView number in a GVP file - it is the count of layers shown.</b>
*/
public static GeoLayerView newLayerView ( String filename, PropList props, int count )
{	GeoLayerView layerView = new GeoLayerView();
	layerView.setLayer ( newLayer(filename) );
	layerView.setProperties ( props );
	// Set default symbol, legend.  This will normally be reset (e.g., when reading in GeoViewProject)
	layerView.setDefaultLegend ( count );
	return layerView;
}

}