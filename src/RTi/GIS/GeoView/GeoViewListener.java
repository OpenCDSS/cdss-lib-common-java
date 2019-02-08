// GeoViewListener - listener for GeoView map events

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

import java.util.List;

import RTi.GR.GRLimits;
import RTi.GR.GRPoint;
import RTi.GR.GRShape;

/**
This interface should be used to capture events from a GeoView. 
GeoView itself will handle any needed state changes (like zooming, etc.).
This interface works in conjunction with the GeoView.setInteractionMode()
settings.  For select methods, GeoView returns a list of GeoRecord, even if
the select is a point.  This will allow selections based on proximity, etc.
*/
public abstract interface GeoViewListener
{
/**
GeoView will call this method for GeoLayerView where the "Label" property is
set to "UsingGeoViewListener".  The implemented method should format the label
according to the settings in the application.  The label string will then be
used to label symbols, etc.  The first listener that returns a non-null string
is assumed to be returning a valid label.
@param record GeoRecord containing shape and layer of shape that is being
labeled.  This object is reused; therefore, make a copy of data to guarantee persistence.
*/
public abstract String geoViewGetLabel ( GeoRecord record );

/**
GeoView will call this method if the GeoView is in INTERACTION_INFO mode
and the mouse is released (the press point is returned if the mouse movement
is less than the select tolerance).
@param devpt Mouse position in device coordinates (in the native device coordinates).
@param datapt Mouse position in data coordinates.
@param selected list of selected data (GeoView uses Vector of GeoRecord).
*/
// TODO (JTS - 2003-05-06) this one might need to be removed (from the old code)
public abstract void geoViewInfo ( GRPoint devpt, GRPoint datapt, List<GeoRecord> selected );

/**
GeoView will call this method if the GeoView is in INTERACTION_INFO mode
and the mouse is released (the press point is returned if the mouse movement
is less than the select tolerance; otherwise the extent of the select region is used).
@param dev_shape Mouse position (GRPoint, GRLimits, etc) in device coordinates
(in the native device coordinates).
@param data_shape Mouse position in data coordinates.
@param selected list of selected data (GeoView uses Vector of GeoRecord).
*/
public abstract void geoViewInfo ( GRShape dev_shape, GRShape data_shape, List<GeoRecord> selected );

/**
GeoView will call this method if the GeoView is in INTERACTION_INFO mode
and the mouse is released (the limits are returned if the mouse movement
is greater than the select tolerance).
@param devlimits Mouse limits in device coordinates (in the native device coordinates).
@param datalimits Mouse limits in data coordinates.
@param selected list of selected data (GeoView uses Vector of GeoRecord).
*/
public abstract void geoViewInfo ( GRLimits devlimits, GRLimits datalimits, List<GeoRecord> selected );

/**
GeoView will call this method if the GeoView mouse tracker is enabled.
@param devpt Mouse position in device coordinates (in the native device coordinates).
@param datapt Mouse position in data coordinates.
*/
public abstract void geoViewMouseMotion ( GRPoint devpt, GRPoint datapt );

/**
GeoView will call this method if the GeoView is in INTERACTION_SELECT mode
and the mouse is released (the press point is returned if the mouse movement
is less than the select tolerance).
@param devpt Mouse position in device coordinates (in the native device coordinates).
@param datapt Mouse position in data coordinates.
@param selected list of selected data (GeoView uses Vector of GeoRecord).
@param append Indicates whether the results should be appended to a previous select.
*/
// TODO (JTS - 2003-05-06) this one is from the old code, it might be able to be removed
public abstract void geoViewSelect ( GRPoint devpt, GRPoint datapt, List<GeoRecord> selected, boolean append );

/**
GeoView will call this method if the GeoView is in INTERACTION_SELECT mode
and the mouse is released (the limits are returned if the mouse movement
is greater than the select tolerance).
@param devlimits Mouse limits in device coordinates (in the native device coordinates).
@param datalimits Mouse limits in data coordinates.
@param selected list of selected data (GeoView uses Vector of GeoRecord).
@param append Indicates whether the results should be appended to a previous select.
*/
// TODO (JTS - 2003-05-06) this one is from the old code, it might be able to be removed
public abstract void geoViewSelect ( GRLimits devlimits, GRLimits datalimits, List<GeoRecord> selected, boolean append );

/**
GeoView will call this method if the GeoView is in INTERACTION_SELECT mode
and the mouse is released.  If the mouse movement is less than the selected
tolerance, a GRPoint may be returned.  Otherwise, a GRLimits or GRArc can be returned.
@param dev_shape Mouse position (GRPoint, GRLimits, etc.) in device coordinates
(in the native device coordinates).
@param data_shape Mouse position in data coordinates.
@param selected list of selected data (GeoView uses Vector of GeoRecord).
@param append Indicates whether the results should be appended to a previous select.
*/
public abstract void geoViewSelect ( GRShape dev_shape, GRShape data_shape, List<GeoRecord> selected, boolean append );

/**
GeoView will call this method if the GeoView is in INTERACTION_ZOOM mode
and the mouse is pressed, dragged, and released.
@param devlimits Mouse limits in device coordinates (in the native device coordinates).
@param datalimits Mouse limits in data coordinates.
*/
// TODO (JTS - 2003-05-06) this one is from the old code, it might be able to be removed
public abstract void geoViewZoom ( GRLimits devlimits, GRLimits datalimits );

/**
GeoView will call this method if the GeoView is in INTERACTION_ZOOM mode
and the mouse is pressed, dragged, and released.
@param dev_shape Mouse limits in device coordinates (in the native device coordinates).
@param data_shape Mouse limits in data coordinates.
*/
public abstract void geoViewZoom ( GRShape dev_shape, GRShape data_shape );

}
