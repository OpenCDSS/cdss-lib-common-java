package RTi.GIS.GeoView;

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
protected String __label = null;

/**
Construct an instance from primitive data.
*/
public GeoViewAnnotationData ( GeoViewAnnotationRenderer annotationRenderer, Object object,
	String label )
{	__annotationRenderer = annotationRenderer;
	__object = object;
	__label = label;
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
Return the object to be rendered.
@return the object to be rendered
*/
public Object getObject ()
{	return __object;
}

}