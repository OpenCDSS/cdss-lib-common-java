// GeoViewAnnotationData - annotation data for map display

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

import RTi.GR.GRLimits;

/**
This class provides for data management of GeoViewAnnotationRenderer instances and associated data so
that the information can be used to provide a list of annotations in the GeoView interface and provide
data back to the renderers when the annotations need to be rendered.
*/
public class GeoViewAnnotationData
{

/**
Renderer for the data object.
*/
private GeoViewAnnotationRenderer __annotationRenderer = null;

/**
Object that will be rendered.
*/
private Object __object = null;

/**
Label for the object (displayed in the GeoView).
*/
private String __label = null;

/**
Data limits for the rendered object (original data units, unprojected).
*/
private GRLimits __limits = null;

/**
Projection for the rendered object (data units).
*/
private GeoProjection __limitsProjection = null;

/**
Construct an instance from primitive data.
@param annotationRenderer the object that will actually render the annotation (the rendering
may be complex due to domain data)
@param object the data object to be rendered (domain object)
@param label the label to be shown on the map and in the annotation legend
@param limits the limits of the rendered data, to aid in zooming to the annotations (data units)
@param limitsProjection the projection for the data limits, needed to handle zooming (projection on the
fly for specific objects in the data are handled through GeoRecord information with each object)
*/
public GeoViewAnnotationData ( GeoViewAnnotationRenderer annotationRenderer, Object object,
	String label, GRLimits limits, GeoProjection limitsProjection )
{	__annotationRenderer = annotationRenderer;
	__object = object;
	__label = label;
	__limits = limits;
	__limitsProjection = limitsProjection;
}

/**
Return the GeoViewAnnotationRenderer for the data.
@return the GeoViewAnnotationRenderer for the data
*/
public GeoViewAnnotationRenderer getGeoViewAnnotationRenderer ()
{	return __annotationRenderer;
}

/**
Return the label for the object.
@return the label for the object
*/
public String getLabel ()
{	return __label;
}

/**
Return the limits for the object.
@return the limits for the object
*/
public GRLimits getLimits ()
{	return __limits;
}

/**
Return the projection of the data limits.
@return the projection of the data limits
*/
public GeoProjection getLimitsProjection ()
{	return __limitsProjection;
}

/**
Return the object to be rendered.
@return the object to be rendered
*/
public Object getObject ()
{	return __object;
}

/**
Return the string representation of the annotation - use the label.
*/
public String toString()
{
	return __label;
}

}
