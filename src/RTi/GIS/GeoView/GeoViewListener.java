// ----------------------------------------------------------------------------
// GeoViewListener - listener for important GeoView events
// ----------------------------------------------------------------------------
// History:
//
// 24 Jun 1999	Steven A. Malers, RTi	Implemented code.
// 14 Jul 1999	SAM, RTi		Add Vector to select methods to return
//					data that is selected.
// 30 Aug 1999	SAM, RTi		Add geoViewGetLabel method to interface.
// 2001-10-04	SAM, RTi		Add geoViewInfo() method, similar to
//					geoViewSelect().
// 2001-12-07	SAM, RTi		Add append flags to the methods that
//					are used for selection.  This allows
//					Ctrl-select to occur.
// ----------------------------------------------------------------------------
// 2003-05-06	J. Thomas Sapienza, RTi Brought code in line with the non-Swing
//					version.
//-----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.util.Vector;

import RTi.GR.GRLimits;
import RTi.GR.GRPoint;
import RTi.GR.GRShape;

/**
This interface should be used to capture events from a GeoView. 
GeoView itself will handle any needed state changes (like zooming, etc.).
This interface works in conjunction with the GeoView.setInteractionMode()
settings.  For select methods, GeoView returns a Vector of GeoRecord, even if
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
labelled.  This object is reused; therefore, make a copy of data to guarantee
persistence.
*/
public abstract String geoViewGetLabel ( GeoRecord record );

/**
GeoView will call this method if the GeoView is in INTERACTION_INFO mode
and the mouse is released (the press point is returned if the mouse movement
is less than the select tolerance).
@param devpt Mouse position in device coordinates (in the native device
coordinates).
@param datapt Mouse position in data coordinates.
@param selected Vector of selected data (GeoView uses Vector of GeoRecord).
*/
// REVISIT (JTS - 2003-05-06)
// this one might need to be removed (from the old code)
public abstract void geoViewInfo (	GRPoint devpt, GRPoint datapt,
					Vector selected );

/**
GeoView will call this method if the GeoView is in INTERACTION_INFO mode
and the mouse is released (the press point is returned if the mouse movement
is less than the select tolerance; otherwise the extent of the select region is
used).
@param dev_shape Mouse position (GRPoint, GRLimits, etc) in device coordinates
(in the native device coordinates).
@param data_shape Mouse position in data coordinates.
@param selected Vector of selected data (GeoView uses Vector of GeoRecord).
*/
public abstract void geoViewInfo (	GRShape dev_shape, GRShape data_shape,
					Vector selected );

/**
GeoView will call this method if the GeoView is in INTERACTION_INFO mode
and the mouse is released (the limits are returned if the mouse movement
is greater than the select tolerance).
@param devlimits Mouse limits in device coordinates (in the native device
coordinates).
@param datalimits Mouse limits in data coordinates.
@param selected Vector of selected data (GeoView uses Vector of GeoRecord).
*/
public abstract void geoViewInfo (	GRLimits devlimits, GRLimits datalimits,
					Vector selected );

/**
GeoView will call this method if the GeoView mouse tracker is enabled.
@param devpt Mouse position in device coordinates (in the native device
coordinates).
@param datapt Mouse position in data coordinates.
*/
public abstract void geoViewMouseMotion ( GRPoint devpt, GRPoint datapt );

/**
GeoView will call this method if the GeoView is in INTERACTION_SELECT mode
and the mouse is released (the press point is returned if the mouse movement
is less than the select tolerance).
@param devpt Mouse position in device coordinates (in the native device
coordinates).
@param datapt Mouse position in data coordinates.
@param selected Vector of selected data (GeoView uses Vector of GeoRecord).
@param append Indicates whether the results should be appended to a previous
select.
*/
// REVISIT (JTS - 2003-05-06)
// this one is from the old code, it might be able to be removed
public abstract void geoViewSelect (	GRPoint devpt, GRPoint datapt,
					Vector selected, boolean append );

/**
GeoView will call this method if the GeoView is in INTERACTION_SELECT mode
and the mouse is released (the limits are returned if the mouse movement
is greater than the select tolerance).
@param devlimits Mouse limits in device coordinates (in the native device
coordinates).
@param datalimits Mouse limits in data coordinates.
@param selected Vector of selected data (GeoView uses Vector of GeoRecord).
@param append Indicates whether the results should be appended to a previous
select.
*/
// REVISIT (JTS - 2003-05-06)
// this one is from the old code, it might be able to be removed
public abstract void geoViewSelect (	GRLimits devlimits, GRLimits datalimits,
					Vector selected, boolean append );

/**
GeoView will call this method if the GeoView is in INTERACTION_SELECT mode
and the mouse is released.  If the mouse movement is less than the selected
tolerance, a GRPoint may be returned.  Otherwise, a GRLimits or GRArc can be
returned.
@param dev_shape Mouse position (GRPoint, GRLimits, etc.) in device coordinates
(in the native device coordinates).
@param data_shape Mouse position in data coordinates.
@param selected Vector of selected data (GeoView uses Vector of GeoRecord).
@param append Indicates whether the results should be appended to a previous
select.
*/
public abstract void geoViewSelect (	GRShape dev_shape, GRShape data_shape,
					Vector selected, boolean append );

/**
GeoView will call this method if the GeoView is in INTERACTION_ZOOM mode
and the mouse is pressed, dragged, and released.
@param devlimits Mouse limits in device coordinates (in the native device
coordinates).
@param datalimits Mouse limits in data coordinates.
*/
// REVISIT (JTS - 2003-05-06)
// this one is from the old code, it might be able to be removed
public abstract void geoViewZoom ( GRLimits devlimits, GRLimits datalimits );

/**
GeoView will call this method if the GeoView is in INTERACTION_ZOOM mode
and the mouse is pressed, dragged, and released.
@param dev_shape Mouse limits in device coordinates (in the native device
coordinates).
@param data_shape Mouse limits in data coordinates.
*/
public abstract void geoViewZoom ( GRShape dev_shape, GRShape data_shape );

}
