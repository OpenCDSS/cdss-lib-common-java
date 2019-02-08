// GeoViewUtil - utility functions for GeoView

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
