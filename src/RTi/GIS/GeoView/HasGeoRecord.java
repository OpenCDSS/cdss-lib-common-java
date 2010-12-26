package RTi.GIS.GeoView;

/**
Simple interface to provide a GeoRecord.  For example, data objects in that implement this interface
allow themselves to be tagged with geographic information through set/get methods passing a GeoRecord
instance.
*/
public interface HasGeoRecord {

/**
Return the GeoRecord associated with the object.  For example, this can be used to draw the object
on a map.
@return the the GeoRecord to use for the object
*/
public GeoRecord getGeoRecord ();

/**
Set the GeoRecord associated with the object.  For example, this can be used to tag the object with
spatial information, perhaps looked up from a geographic data layer via the object's identifier.
@param geoRecord the GeoRecord to use for the object
*/
public void setGeoRecord ( GeoRecord geoRecord );

}