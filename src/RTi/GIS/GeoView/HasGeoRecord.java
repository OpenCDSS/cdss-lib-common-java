// HasGeoRecord - interface to indicate whether an object has a GeoRecord

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
