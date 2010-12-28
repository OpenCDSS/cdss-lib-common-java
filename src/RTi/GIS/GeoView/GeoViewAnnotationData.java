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
Data limits for the rendered object (data units).
*/
private GRLimits __limits = null;

/**
Projection the rendered object (data units).
*/
private GeoProjection __projection = null;

/**
Construct an instance from primitive data.
@param annotationRenderer the object that will actually render the annotation (the rendering
may be complex due to domain data)
@param object the data object to be rendered (domain object)
@param label the label to be shown on the map and in the annotation legend
@param limits the limits of the rendered data, to aid in zooming to the annotations (data units)
@param projection the projection for the data (and limits), needed to project on the fly
*/
public GeoViewAnnotationData ( GeoViewAnnotationRenderer annotationRenderer, Object object,
	String label, GRLimits limits, GeoProjection projection )
{	__annotationRenderer = annotationRenderer;
	__object = object;
	__label = label;
	__limits = limits;
	__projection = projection;
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{
	super.finalize();
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
Return the object to be rendered.
@return the object to be rendered
*/
public Object getObject ()
{	return __object;
}

/**
Return the projection of the data.
@return the projection of the data
*/
public GeoProjection getProjection ()
{	return __projection;
}

/**
Return the string representation of the annotation - use the label.
*/
public String toString()
{
	return __label;
}

}