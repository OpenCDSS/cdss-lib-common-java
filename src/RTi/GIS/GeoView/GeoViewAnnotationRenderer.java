package RTi.GIS.GeoView;

/**
Objects that implement this interface can be added to the GeoViewJPanel to annotate the map by drawing
additional objects on top of the map.  This is useful, for example, to highlight information beyond
a normal selection.  For example, the annotation might show related information.
*/
public interface GeoViewAnnotationRenderer {

	/**
	 * This method will be called by the GeoViewJComponent when rendering the map, passing back the
	 * object from getAnnotationObject().
	 * @param geoviewJComponent the map object
	 * @param objectToRender the object to render as an annotation on the map
	 * @param label the string that is used to label the annotation on the map
	 */
	public void renderGeoViewAnnotation ( GeoViewJComponent geoviewJComponent, Object objectToRender,
		String label );
	
	/**
	 * Return the object to render.
	 */
	//public Object getAnnotationObject ();
	
	/**
	 * Return the label for the object to render.  This will be listed in the GeoViewPanel.
	 */
	//public String getAnnotationLabel ();
}