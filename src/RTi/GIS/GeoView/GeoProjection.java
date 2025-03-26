// GeoProjection - projection base class

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

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import RTi.GR.GRArc;
import RTi.GR.GRLimits;
import RTi.GR.GRPoint;
import RTi.GR.GRPointZM;
import RTi.GR.GRPolygon;
import RTi.GR.GRPolygonList;
import RTi.GR.GRPolyline;
import RTi.GR.GRPolylineList;
import RTi.GR.GRPolylineZM;
import RTi.GR.GRPolylineZMList;
import RTi.GR.GRPolypoint;
import RTi.GR.GRShape;
import RTi.Util.Message.Message;

/**
GeoProjection is a base class to be used for projections.  Currently this class
only defines a couple of methods that need to be defined for each projection.
As more familiarity with the projection requirements is gained, more data and
methods will be added to this base class.  In the future, may add some type of
query ability here so that an application can list the projections that are available.
*/
public abstract class GeoProjection
implements Cloneable
{

/**
A single GeographicProjection instance is made available to allow code to be
optimized.  The GeographicProjection is the base projection that is used when changing projections.
*/
public static GeographicProjection geographic_projection = new GeographicProjection();

/**
Datum used (e.g., "NAD83").
*/
protected String _datum = "";

/**
Eccentricity constant (this is a GCTP parameter).
*/
protected double _e0 = 0.0;

/**
Eccentricity constant (this is a GCTP parameter).
*/
protected double _e1 = 0.0;

/**
Eccentricity constant (this is a GCTP parameter).
*/
protected double _e2 = 0.0;

/**
Eccentricity constant (this is a GCTP parameter).
*/
protected double _e = 0.0;

/**
Eccentricity constant (this is a GCTP parameter).
*/
protected double _es = 0.0;

/**
Eccentricity constant (this is a GCTP parameter).
*/
protected double _esp = 0.0;

/**
Eccentricity constant (this is a GCTP parameter).
*/
protected double _e3 = 0.0;

/**
False easting (this is a GCTP parameter).
*/
protected double _false_easting = 0.0;

/**
False northing (this is a GCTP parameter).
*/
protected double _false_northing = 0.0;

/**
Spherical flag (this is a GCTP parameter).
*/
protected boolean _ind = false;

/**
Central latitude for (this is a GCTP parameter).
*/
protected double _lat_origin = 0.0;

/**
Central longitude (meridian) (this is a GCTP parameter).
*/
protected double _lon_center = 0.0;

/**
Small value for m (this is a GCTP parameter).
*/
protected double _ml0 = 0.0;

/**
Projection name (e.g., "Geographic").  Currently only simple names are used but
in the future longer names may be used as more formal handling of projections occurs.
*/
protected String _projection_name = "Unknown";

/**
The projection number is an internal number to keep track of and compare projections.
*/
protected int _projection_number = 0;

/**
Radius of sphere (GCTP parameter).
*/
protected double _radius = 0.0;

/**
Semi-major axis for spheroid (GCTP parameter).
*/
protected double _r_major = 0.0;

/**
Semi-minor axis for spheroid (GCTP parameter).
*/
protected double _r_minor = 0.0;

/**
Scale for projection (GCTP parameter).
*/
protected double _scale_factor = 1.0;

/**
Zone used by UTM and StatePlane projections.  This is used to look up
default _central_longitude and other parameters.
*/
protected int _zone = 0;

/**
List of defined projections.  Currently this is a list of projections that have
been defined but it does not list available projections.  Use
getProjectionNames() to get available projections and
getDefinedProjectionNames() to get those that are defined in memory.
*/
static private String[] _defined_projections = null;

/**
Construct a projection.  This method should be called by derived classes to
set base class information.  Each time that a projection is constructed, its
name is checked against existing projections in memory.  If there is not a
match, then a new projection is added to the active list and a projection number
is assigned.  The projection numbers should be used for comparisons for
optimized on-the-fly projections.  Because these checks are done each time a
projection is created, care should be taken to minimize the number of new
projections that are created (e.g., don't create inside a loop when a single
projection is sufficient).  In most cases, a projection will be created and
associated with a layer when it is read and then the same projection can be
used for conversions.  This procedure is a simple form of reference counting and
will allow projections to be dynamically added (which are not in the recognized list).
@param name Projection name ("Geographic", "HRAP", "UTM").
*/
public GeoProjection ( String name )
{	_projection_name = name;
	// Add a new projection to the list if necessary...
	if ( _defined_projections == null ) {
		// Allocate a new list..
		_defined_projections = new String[1];
		_defined_projections[0] = name;
		_projection_number = 0;
	}
	else {	// Resize the list and add to the end...
		boolean found = false;
		for ( int i = 0; i < _defined_projections.length; i++ ) {
			if ( _defined_projections[i].equalsIgnoreCase(name) ) {
				// Already in the list...
				_projection_number = i;
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Add a new projection.  Always add at the end.
			// If an alphabetized list of projections is needed,
			// the names can always be sorted later.
			String tmp[] =new String[_defined_projections.length+1];
			for ( int i = 0; i < _defined_projections.length; i++ ){
				tmp[i] = _defined_projections[i];
			}
			tmp[_defined_projections.length] = name;
			_projection_number = _defined_projections.length;
			_defined_projections = tmp;
			tmp = null;
		}
	}
}

/**
Copy constructor.
@param p GeoProjection instance to copy.
*/
public GeoProjection ( GeoProjection p )
{	_projection_name = p._projection_name;
	_projection_number = p._projection_number;
	_e0 = p._e0;
	_e1 = p._e1;
	_e2 = p._e2;
	_e3 = p._e3;
	_e = p._e;
	_es = p._es;
	_esp = p._esp;
	_false_easting = p._false_easting;
	_false_northing = p._false_northing;
	_ind = p._ind;
	_lat_origin = p._lat_origin;
	_lon_center = p._lon_center;
	_ml0 = p._ml0;
	_radius = p._radius;
	_r_major = p._r_major;
	_r_minor = p._r_minor;
	_scale_factor = p._scale_factor;
	_zone = p._zone;
}

/*
Adjust a longitude angle to range from -180 to 180 radians (method from GCTP package).
@param x Angle in radians.
*/
protected static double adjust_lon ( double x )
{	long count = 0;
	double TWO_PI = Math.PI*2.0;
	double MAXLONG = 2147483647.;
	double DBLLONG = 4.61168601e18;
	for ( ;; ) {
		if ( Math.abs(x) <= Math.PI ) {
			break;
		}
		else if (((long)Math.abs(x / Math.PI)) < 2) {
			x = x-(sign(x)*TWO_PI);
		}
		else if (((long)Math.abs(x / TWO_PI)) < MAXLONG) {
			x = x-(((long)(x / TWO_PI))*TWO_PI);
		}
		else if (((long)Math.abs(x / (MAXLONG * TWO_PI))) < MAXLONG) {
			x = x-(((long)(x/(MAXLONG*TWO_PI)))*(TWO_PI * MAXLONG));
		}
		else if (((long)Math.abs(x / (DBLLONG * TWO_PI))) < MAXLONG) {
			x = x-(((long)(x/(DBLLONG*TWO_PI)))*(TWO_PI * DBLLONG));
		}
		else {
			x = x-(sign(x) *TWO_PI);
		}
		count++;
		if ( count > 4 ) {
			break;
		}
	}
	return x;
}

/*
Eliminate roundoff errors in asin.
*/
protected static double asinz ( double con )
{	if ( Math.abs(con) > 1.0 ) {
		if (con > 1.0 ) {
			con = 1.0;
		}
		else {
			con = -1.0;
		}
	}
	return Math.asin(con);
}

/**
Clones the object.
@return a clone of the object.
*/
public Object clone() {
	try {
		return (GeoProjection)super.clone();
	}
	catch (Exception e) {
		return null;
	}
}

/**
Compute the constant e0, which is used in a series for calculating the distance along a meridian.
@return value of e0.
@param x the eccentricity squared.
*/
protected static double e0fn ( double x )
{	return(1.0-0.25*x*(1.0+x/16.0*(3.0+1.25*x)));
}

/**
Compute the constant e1, which is used in a series for calculating the distance along a meridian.
@return value of e1.
@param x the eccentricity squared.
*/
protected static double e1fn ( double x )
{	return(0.375*x*(1.0+0.25*x*(1.0+0.46875*x)));
}

/**
Compute the constant e2, which is used in a series for calculating the distance along a meridian.
@return value of e2.
@param x the eccentricity squared.
*/
protected static double e2fn ( double x )
{	return(0.05859375*x*x*(1.0+0.75*x));
}

/**
Compute the constant e3, which is used in a series for calculating the distance along a meridian.
@return value of e3.
@param x the eccentricity squared.
*/
protected static double e3fn ( double x ) 
{	return(x*x*x*(35.0/3072.0));
}

/**
Determine if projections are equal.  Currently, the name, datum, and zone are the only items checked.
@param other Other projection to compare to.
@return true if the projections are equal.
*/
public boolean equals ( GeoProjection other )
{	if ( _datum.equalsIgnoreCase(other._datum) &&
		_projection_name.equalsIgnoreCase(other._projection_name) && (_zone == other._zone) ) {
		return true;
	}
	return false;
}

/**
Get the number of kilometers for a unit of the projection grid.  The
point that is used can be reused if necessary to increase performance.
This method should be defined in derived classes.  The version in this base
class always returns the original point.
@param p As input, specifies the location (in projected units) at which to determine the scale.
@param reuse_point Indicates whether the point that is passed in should be
re-used for the output (doing so saves memory).
*/
public GRPoint getKilometersForUnit ( GRPoint p, boolean reuse_point )
{	return p;
}

/**
Get the projection name.
@return the projection name.
*/
public String getProjectionName ()
{	return _projection_name;	
}

/**
Get the list of available projections or null if no projections have been defined.
@param type If 0, lists the available projections known to the GeoView package.
If 1, lists the defined projections (projections that have been instantiated during processing).
*/
public static List<String> getProjectionNames ( int type )
{	if ( type == 0 ) {
		List<String> v = new ArrayList<String> ( 4 );
		v.add ( "Geographic" );
		v.add ( "HRAP" );
		v.add ( "UTM" );
		v.add ( "Unknown" );
	}
	else if ( type == 1 ) {
		if ( _defined_projections == null ) {
			return null;
		}
		List<String> v = new ArrayList<String> ( _defined_projections.length );
		for ( int i = 0; i < _defined_projections.length; i++ ) {
			v.add ( _defined_projections[i] );
		}
		return v;
	}
	return null;
}

/**
Get the projection number.
@return the projection number.
*/
public int getProjectionNumber ()
{	return _projection_number;	
}

/**
Compute the distance along a meridian from the Equator to latitude phi.
Method is from the GCTP package.
*/
protected static double mlfn (	double e0, double e1, double e2, double e3, double phi )
{	return (e0*phi - e1*Math.sin(2.0*phi) + e2*Math.sin(4.0*phi) - e3*Math.sin(6.0*phi));
}

/**
Determine whether a projection needs to be made.
@return false if the projection numbers are the same or either projection is unknown, true otherwise.
@param projection1 First projection.
@param projection2 Second projection.
*/
public static boolean needToProject ( GeoProjection projection1, GeoProjection projection2 )
{	if ( (projection1 == null) || (projection2 == null) ) {
		return false;
	}
	if ( projection1.getProjectionName().equalsIgnoreCase("Unknown") ||
		projection2.getProjectionName().equalsIgnoreCase("Unknown") ) {
		return false;
	}
/*
	if ( projection1.getProjectionNumber() == projection2.getProjectionNumber() ) {
		return false;
	}
*/
	// Rely on the more robust equals() method...
	if ( projection1.equals(projection2) ) {
		return false;
	}
	return true;
}

/**
Parse a projection string and return an instance of the projection.
Currently this only works for known projections but generic classes of
projections with parameters, can be added in the future.
@param projection_string String containing projection definition (e.g.,
"Geographic").  Strings must adhere to the following to be recognized:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Projection</b></td>   <td><b>Description</b></td>   
<td><b>Example</b></td>
</tr

<tr>
<td><b>Geographic</b></td>
<td>Longitude, Latitude.</td>
<td>Geographic</td>
</tr>

<tr>
<td><b>HRAP</b></td>
<td>National Weather Service coordinate system.</td>
<td>HRAP</td>
</tr>

<tr>
<td><b>UTM,Zone,Datum,FalseEasting,FalseNorthing,CentralLongitude,
OriginLatitude,Scale</b><br>
Specify no value to use the default.
The default Datum is NAD83.
The default FalseEasting is 500000.0.
The default FalseNorthing is 500000.0.
The default CentralLongitude is determined from the zone.
The default OriginLatitude is 0.
The default Scale is .9996.
</td>
<td>Universal Transvers Mercator</td>
<td>UTM,19,NAD83,500000.0,0.0,,,.9996<br>
UTM,19<br></td>
</tr>

</table>
@exception Exception if a projection cannot be determined from the string.
*/
public static GeoProjection parseProjection ( String projection_string )
throws Exception
{	if ( projection_string.regionMatches(true,0,"Geographic",0,10) ) {
		return new GeographicProjection();
	}
	else if ( projection_string.regionMatches(true,0,"HRAP",0,4) ) {
		return new HRAPProjection();
	}
	else if ( projection_string.regionMatches(true,0,"Unknown",0,7) ) {
		return new UnknownProjection();
	}
	else if ( projection_string.regionMatches(true,0,"UTM",0,3) ) {
		return UTMProjection.parse(projection_string);
	}
	else {
		throw new Exception ( "Unknown projection \"" + projection_string + "\"" );
	}
}

/**
Project latitude and longitude to the projection's coordinate system.
@param p Point to project from latitude and longitude.
@param reuse_point Indicates whether the point that is passed in should be
re-used for the output (doing so saves memory).
*/
public GRPoint project ( GRPoint p, boolean reuse_point )
{	Message.printStatus ( 2, "GeoProjection.project",
	"This method should be defined in the derived class.  Returning the original point." );
	return p;
}

/**
Project a shape from one projection to another.  Note that GRArc radii are not currently projected.
@param from Projection to convert from.
@param to Projection to convert to.
@param shape Shape to convert.
@param reuseShape Indicates whether shape should be reused (doing so saves memory resources).
*/
public static GRShape projectShape ( GeoProjection from, GeoProjection to, GRShape shape, boolean reuseShape )
{	if ( shape.type == GRShape.ARC ) {
		GRArc arc = null;
		if ( reuseShape ) {
			arc = (GRArc)shape;
			// Need to do this so there will be clean start on getting limits...
			arc.limits_found = false;
		}
		else {
			arc = new GRArc ( (GRArc)shape );
		}
		from.unProject(arc.pt,true);
		to.project(arc.pt,true);
		// Also need to project the radii (do later - for now require radii to be pre-projected)...
		// This is necessary to recalculate the max/min values, which
		// ultimately get used when deciding if the shape should be drawn...
		arc.setPoint ( arc.pt );
		if ( reuseShape ) {
			arc = null;
			return shape;
		}
		else {
			return arc;
		}
	}
	else if ( shape.type == GRShape.POLYGON ) {
		GRPolygon polygon = null;
		if ( reuseShape ) {
			polygon = (GRPolygon)shape;
			// Need to do this so there will be clean start on getting limits...
			polygon.limits_found = false;
		}
		else {
			polygon = new GRPolygon ( (GRPolygon)shape );
		}
		for ( int i = 0; i < polygon.npts; i++ ) {
			from.unProject(polygon.pts[i],true);
			to.project(polygon.pts[i],true);
			// This is necessary to recalculate the max/min values, which ultimately get used when
			// deciding if the shape should be drawn...
			polygon.setPoint ( i, polygon.pts[i] );
		}
		if ( reuseShape ) {
			polygon = null;
			return shape;
		}
		else {
			return polygon;
		}
	}
	else if ( shape.type == GRShape.POLYGON_LIST ) {
		GRPolygonList polygonlist = null;
		if ( reuseShape ) {
			polygonlist = (GRPolygonList)shape;
			// Need to do this so there will be clean start on getting limits...
			polygonlist.limits_found = false;
		}
		else {
			polygonlist = new GRPolygonList ( (GRPolygonList)shape);
		}
		// Loop through the polygons in the list and project each...
		for ( int i = 0; i < polygonlist.npolygons; i++ ) {
			projectShape ( from, to, polygonlist.polygons[i], true);
			// This is necessary to recalculate the max/min
			// values, which ultimately get used when deciding if the shape should be drawn...
			polygonlist.setPolygon ( i, polygonlist.polygons[i] );
		}
		if ( reuseShape ) {
			polygonlist = null;
			return shape;
		}
		else {
			return polygonlist;
		}
	}
	else if ( shape.type == GRShape.POLYLINE ) {
		GRPolyline polyline = null;
		if ( reuseShape ) {
			polyline = (GRPolyline)shape;
			// Need to do this so there will be clean start on getting limits...
			polyline.limits_found = false;
		}
		else {
			polyline = new GRPolyline ( (GRPolyline)shape );
		}
		for ( int i = 0; i < polyline.npts; i++ ) {
			from.unProject(polyline.pts[i],true);
			to.project(polyline.pts[i],true);
			// This is necessary to recalculate the max/min values, which ultimately get used when
			// deciding if the shape should be drawn...
			polyline.setPoint ( i, polyline.pts[i] );
		}
		if ( reuseShape ) {
			polyline = null;
			return shape;
		}
		else {
			return polyline;
		}
	}
	else if ( shape.type == GRShape.POLYLINE_ZM ) {
		GRPolylineZM polyline = null;
		if ( reuseShape ) {
			polyline = (GRPolylineZM)shape;
			// Need to do this so there will be clean start on getting limits...
			polyline.limits_found = false;
		}
		else {
			polyline = new GRPolylineZM ( (GRPolylineZM)shape );
		}
		for ( int i = 0; i < polyline.npts; i++ ) {
			from.unProject(polyline.pts[i],true);
			to.project(polyline.pts[i],true);
			// This is necessary to recalculate the max/min values, which ultimately get used when
			// deciding if the shape should be drawn...
			polyline.setPoint ( i, polyline.pts[i] );
		}
		if ( reuseShape ) {
			polyline = null;
			return shape;
		}
		else {
			return polyline;
		}
	}
	else if ( shape.type == GRShape.POLYLINE_LIST ) {
		GRPolylineList polylinelist = null;
		if ( reuseShape ) {
			polylinelist = (GRPolylineList)shape;
			// Need to do this so there will be clean start on getting limits...
			polylinelist.limits_found = false;
		}
		else {
			polylinelist =new GRPolylineList((GRPolylineList)shape);
		}
		// Loop through the polylines in the list and project each...
		for ( int i = 0; i < polylinelist.npolylines; i++ ) {
			projectShape (from, to, polylinelist.polylines[i],true);
			// This is necessary to recalculate the max/min
			// values, which ultimately get used when deciding if the shape should be drawn...
			polylinelist.setPolyline(i, polylinelist.polylines[i] );
		}
		if ( reuseShape ) {
			polylinelist = null;
			return shape;
		}
		else {
			return polylinelist;
		}
	}
	else if ( shape.type == GRShape.POLYLINE_ZM_LIST ) {
		GRPolylineZMList polylinelist = null;
		if ( reuseShape ) {
			polylinelist = (GRPolylineZMList)shape;
			// Need to do this so there will be clean start on getting limits...
			polylinelist.limits_found = false;
		}
		else {
			polylinelist = new GRPolylineZMList((GRPolylineZMList)shape);
		}
		// Loop through the polylines in the list and project each...
		for ( int i = 0; i < polylinelist.npolylines; i++ ) {
			projectShape (from, to, polylinelist.polylines[i],true);
			// This is necessary to recalculate the max/min
			// values, which ultimately get used when deciding if the shape should be drawn...
			polylinelist.setPolyline(i, polylinelist.polylines[i] );
		}
		if ( reuseShape ) {
			polylinelist = null;
			return shape;
		}
		else {
			return polylinelist;
		}
	}
	else if ( shape.type == GRShape.POINT ) {
		GRPoint point = null;
		if ( reuseShape ) {
			point = (GRPoint)shape;
		}
		else {
			point = new GRPoint ( (GRPoint)shape );
		}
		from.unProject(point,true);
		to.project(point,true);
		if ( reuseShape ) {
			point = null;
			return shape;
		}
		else {
			return point;
		}
	}
	else if ( shape.type == GRShape.POINT_ZM ) {
		GRPointZM point = null;
		if ( reuseShape ) {
			point = (GRPointZM)shape;
		}
		else {
			point = new GRPointZM ( (GRPointZM)shape );
		}
		from.unProject(point,true);
		to.project(point,true);
		if ( reuseShape ) {
			point = null;
			return shape;
		}
		else {
			return point;
		}
	}
	else if ( shape.type == GRShape.POLYPOINT ) {
		GRPolypoint polypoint = null;
		if ( reuseShape ) {
			polypoint = (GRPolypoint)shape;
			// Need to do this so there will be clean start on getting limits...
			polypoint.limits_found = false;
		}
		else {
			polypoint = new GRPolypoint ( (GRPolypoint)shape );
		}
		for ( int i = 0; i < polypoint.npts; i++ ) {
			from.unProject(polypoint.pts[i],true);
			to.project(polypoint.pts[i],true);
			// This is necessary to recalculate the max/min values, which ultimately get used when
			// deciding if the shape should be drawn...
			polypoint.setPoint ( i, polypoint.pts[i] );
		}
		if ( reuseShape ) {
			polypoint = null;
			return shape;
		}
		else {
			return polypoint;
		}
	}
	else if ( shape instanceof GRLimits ) {
		GRLimits limits = (GRLimits)shape;
		GRPoint pointMin = new GRPoint(limits.getMinX(),limits.getMinY());
		GRPoint pointMax = new GRPoint(limits.getMaxX(),limits.getMaxY());
		if ( reuseShape ) {
			limits = (GRLimits)shape;
		}
		else {
			limits = new GRLimits ( (GRLimits)shape );
		}
		from.unProject(pointMin,true);
		to.project(pointMin,true);
		from.unProject(pointMax,true);
		to.project(pointMax,true);
		if ( reuseShape ) {
			limits.setLeftX(pointMin.getX());
			limits.setBottomY(pointMin.getY());
			limits.setRightX(pointMax.getX());
			limits.setTopY(pointMax.getY());
			return limits;
		}
		else {
			limits = new GRLimits(pointMin.getX(),pointMin.getY(),pointMax.getX(),pointMax.getY());
			return limits;
		}
	}
	// For now just return
	return shape;
}

/**
Set the spheroid information (_r_major, _r_minor, _radius) given the datum string.  This is called
from the derived projections.  This code was taken from the GCTP sphdz() function.
@param datum Datum string (currently only "NAD27" and "NAD83" are recognized.
*/
protected void setSpheroid ( String datum )
{	if ( datum.equalsIgnoreCase("NAD27") ) {
		// GCTP 0: Clarke 1866 (default)
		_datum = datum;
		_r_major = 6378206.4;
		_r_minor = 6356583.8;
		_radius = 6370997.0; // GCTP 19: Sphere of Radius 6370997 meters
	}
	else if ( datum.equalsIgnoreCase("NAD83") ) {
		// GCTP 8: GRS 1980
		_datum = datum;
		_r_major = 6378137.0;
		_r_minor = 6356752.31414;
		_radius = 6370997.0; // GCTP 19: Sphere of Radius 6370997 meters
	}
	else {
		// GCTP 0: Clarke 1866 (default)
		_datum = "NAD27";
		_r_major = 6378206.4;
		_r_minor = 6356583.8;
		_radius = 6370997.0; // GCTP 19: Sphere of Radius 6370997 meters
	}
}

/**
Return the sign of an argument.
@return the sign of an argument.
*/
protected static int sign ( double x )
{	if ( x < 0.0 ) {
    	return -1;
	}
	else {
		return 1;
	}
}

/**
Return the name of the projection.
@return the projection name as a String.
*/
public String toString ()
{	return _projection_name;
}

/**
Un-project coordinates back to latitude and longitude.
@return the un-projected points.
@param p Point to un-project to latitude and longitude.
@param reusePoint Indicates whether the point that is passed in should be
re-used for the output (doing so saves memory).
*/
public GRPoint unProject ( GRPoint p, boolean reusePoint )
{	Message.printStatus ( 2, "GeoProjection.unProject",
	"This method should be defined in the derived class.  Returning the original point." );
	return p;
}

}
