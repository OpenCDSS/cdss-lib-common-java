// ----------------------------------------------------------------------------
// UTMProjection - implement Universal Transverse Mercatum projection
// ----------------------------------------------------------------------------
// Copyright: see the COPYRIGHT file.
// ----------------------------------------------------------------------------
//
// Notes from GCTP:
//
/*******************************************************************************
NAME                            UNIVERSAL TRANSVERSE MERCATOR

PURPOSE:	Transforms input longitude and latitude to Easting and
		Northing for the Universal Transverse Mercator projection.
		The longitude and latitude must be in radians.  The Easting
		and Northing values will be returned in meters.

PROGRAMMER              DATE		REASON
----------              ----		------
D. Steinwand, EROS      Nov, 1991
T. Mittan		Mar, 1993
S. Nelson		Feb, 1995	Divided tmfor.c into two files, one
					for UTM (utmfor.c) and one for 
					TM (tmfor.c).  This was a
					necessary change to run forward
					projection conversions for both
					UTM and TM in the same process.

ALGORITHM REFERENCES

1.  Snyder, John P., "Map Projections--A Working Manual", U.S. Geological
    Survey Professional Paper 1395 (Supersedes USGS Bulletin 1532), United
    State Government Printing Office, Washington D.C., 1987.

2.  Snyder, John P. and Voxland, Philip M., "An Album of Map Projections",
    U.S. Geological Survey Professional Paper 1453 , United State Government
    Printing Office, Washington D.C., 1989.
*******************************************************************************/
// History:
//
// 2001-11-28	Steve Malers, RTi	Initial implementation.
//					Copy HRAPProjection and update using the
//					GCTP code, specifically:
//					*	change static data to member
//						data
//					*	replace sincos() call with
//						individual calls
//					*	inline SQUARE to multiply two
//						values
//					*	inline constants like HALF_PI.
//					*	put supporting methods in the
//						GeoProjection base class.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.lang.Math;
import java.util.List;

import RTi.GR.GRPoint;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
The UTMProjection class projects and unprojects the Universal Transverse
Mercator (UTM) projection.  This class implements logic from the GCTP package.
*/
public class UTMProjection extends GeoProjection {

/**
Constructor.
*/
public UTMProjection()
{	super ( "UTM" );
}

/**
Finalize and clean up.
@exception Throwable if there is an error.
*/
protected void finalize() throws Throwable
{	super.finalize();
}

/**
Parse a projection and return an instance of UTMProjection if a valid string is provided.
@param projection_string String describing the projection.  The string contains
comma-separated parameters to define the projection.  At a minimum, "UTM" and
a zone are required.  Optionally, include datum (default "NAD83"),
false easting (default 500000), false northing (default 0),
central longitude (default computed from zone), origin latitude (default 0), and scale (default .9996).
Empty strings are allowed and result in appropriate defaults.
@return UTMProjection corresponding to the string.
@exception Exception if there is an error parsing the projection information.
*/
public static UTMProjection parse ( String projection_string )
throws Exception
{	List tokens = StringUtil.breakStringList ( projection_string, ",", 0);
	int size = 0;
	if ( tokens != null ) {
		size = tokens.size();
	}
	if ( size < 2 ) {
		throw new Exception ( "UTM projection requires at least 2 parameters" );
	}
	UTMProjection projection = new UTMProjection ();
	// 0 is the "UTM"
	// 1 is the zone
	projection._zone = StringUtil.atoi((String)tokens.get(1));
	if ( (Math.abs(projection._zone) < 1) || (Math.abs(projection._zone) > 60)) {
		throw new Exception ( "Illegal UTM zone number " + projection._zone);
	}

	// 2 is the datum for spheroid (this sets _datum, _r_major and _r_minor)...
	if ( size >= 3 ) {
		projection.setSpheroid ( (String)tokens.get(2) );
	}
	else {
		// Will use default...
		projection.setSpheroid ( "NAD83" );
	}

	// 3 is false Easting...
	// 4 is false Northing...

	projection._false_easting = 500000.0;
	if ( projection._zone < 0 ) {
		projection._false_northing = 10000000.0;
	}
	else {
		projection._false_northing = 0.0;
	}

	if ( size >= 4 ) {
		// Input string may have easting...
		if ( StringUtil.isDouble((String)tokens.get(3)) ) {
			projection._false_easting = StringUtil.atod((String)tokens.get(3) );
		}
	}
	if ( size >= 5 ) {
		// Input string may have northing...
		if ( StringUtil.isDouble((String)tokens.get(4)) ) {
			projection._false_northing = StringUtil.atod((String)tokens.get(4) );
		}
	}

	double D2R = 1.745329251994328e-2;
	projection._lon_center = ((6 * Math.abs(projection._zone)) - 183) * D2R;
	if ( size >= 6 ) {
		if ( StringUtil.isDouble((String)tokens.get(5)) ) {
			projection._lon_center = StringUtil.atod((String)tokens.get(5) )*D2R;
		}
	}

	projection._lat_origin = 0.0;
	if ( size >= 7 ) {
		if ( StringUtil.isDouble((String)tokens.get(6)) ) {
			projection._lat_origin = StringUtil.atod((String)tokens.get(6) )*D2R;
		}
	}

	projection._scale_factor = .9996;
	if ( size >= 8 ) {
		if ( StringUtil.isDouble((String)tokens.get(7)) ) {
			projection._scale_factor = StringUtil.atod((String)tokens.get(7) );
		}
	}

	double temp = projection._r_minor/projection._r_major;
	projection._es = 1.0 - temp*temp;
	projection._e = Math.sqrt(projection._es);
	projection._e0 = e0fn(projection._es);
	projection._e1 = e1fn(projection._es);
	projection._e2 = e2fn(projection._es);
	projection._e3 = e3fn(projection._es);
	projection._ml0 = projection._r_major * mlfn( projection._e0,
					projection._e1,
					projection._e2,
					projection._e3,
					projection._lat_origin);
	projection._esp = projection._es/(1.0 - projection._es);
	if ( projection._es < .00001 ) {
		projection._ind = true;
	}

	tokens = null;
	return projection;
}

/**
Project latitude and longitude to the UTM coordinate system.
@return the projected (to UTM) points.
@param p Point to project from longitude, latitude.
@param reuse_point Indicates whether the point that is passed in should be re-used for the output 
(doing so saves memory).
*/
public GRPoint project ( GRPoint p, boolean reuse_point )
{	double delta_lon;	// Delta longitude (Given longitude - center)
	double sin_phi, cos_phi;// sin and cos value
	double al, als;		// temporary values
	double b;		// temporary values
	double c, t, tq;	// temporary values
	double con, n, ml;	// cone constant, small m

	double D2R = 1.745329251994328e-2;
	double lon = p.x*D2R;	// Longitude to project, radians
	double lat = p.y*D2R;	// Latitude to project, radians
	double x = 0.0;		// UTM X
	double y = 0.0;		// UTM Y

	// Forward equations
	delta_lon = adjust_lon(lon - _lon_center);
	sin_phi = Math.sin ( lat );
	cos_phi = Math.cos ( lat );

	// This part was in the fortran code and is for the spherical form
	if ( _ind ) {
		b = cos_phi * Math.sin(delta_lon);
		if ((Math.abs(Math.abs(b) - 1.0)) < .0000000001) {
			Message.printWarning ( 2, "UTMProjection.project", "Point projects into infinity" );
			x = 0.0;
			y = 0.0;
		}
		else {
			x = .5*_r_major*_scale_factor * Math.log((1.0 + b)/(1.0 - b));
			con = Math.acos(cos_phi * Math.cos(delta_lon)/Math.sqrt(1.0 - b*b));
			if ( lat < 0 ) {
				con = -con;
			}
			y = _r_major * _scale_factor * (con - _lat_origin); 
		}
	}
	else {
		al  = cos_phi * delta_lon;
		als = al*al;
		c   = _esp*cos_phi*cos_phi;
		tq  = Math.tan(lat);
		t   = tq*tq;
		con = 1.0 - _es*sin_phi*sin_phi;
		n   = _r_major / Math.sqrt(con);
		ml  = _r_major * mlfn(_e0, _e1, _e2, _e3, lat);
		
		x  = _scale_factor*n*al*(1.0 + als/6.0*(1.0 - t + c + als/20.0 *
      			(5.0 - 18.0*t + t*t + 72.0*c - 58.0*_esp))) + _false_easting;
		
		y  = _scale_factor*(ml - _ml0 + n*tq*(als * (0.5 + als/24.0*
      			(5.0 - t + 9.0*c + 4.0*c*c + als/30.0*
			(61.0 - 58.0*t + t*t + 600.0*c - 330.0*_esp))))) + _false_northing;
	}

	if (reuse_point) {
		p.x = x;
		p.y = y;
		p.xmax = p.x;
		p.xmin = p.x;
		p.ymax = p.y;
		p.ymin = p.y;
		return p;
	}
	else {
		return new GRPoint( x, y);
	}
}

/**
Un-project coordinates from UTM back to longitude, latitude.
@return the un-projected (from UTM) points.
@param p Point to un-project to longitude, latitude.
@param reuse_point Indicates whether the point that is passed in should be re-used for the output
(doing so saves memory).
*/
public GRPoint unProject(GRPoint p, boolean reuse_point)
{	double x = p.x;
	double y = p.y;
	double lon;
	double lat;
	double con,phi;		// temporary angles
	double delta_phi;	// difference between longitudes
	long i;			// counter variable
	double sin_phi, cos_phi, tan_phi;	// sin cos and tangent values
	double c, cs, t, ts, n, r, d, ds;	// temporary variables
	double f, h, g, temp;			// temporary variables
	long max_iter = 6;			// maximun number of iterations
	double HALF_PI = Math.PI*.5;
	double EPSLN = 1.0e-10;

	// Fortran code for spherical form 

	if ( _ind ) {
		f = Math.exp(x/(_r_major * _scale_factor));
		g = .5 * (f - 1/f);
		temp = _lat_origin + y/(_r_major * _scale_factor);
		h = Math.cos(temp);
		con = Math.sqrt((1.0 - h * h)/(1.0 + g * g));
		lat = asinz(con);
		if ( temp < 0 ) {
     			lat = -lat;
		}
		if ((g == 0) && (h == 0)) {
			lon = _lon_center;
		}
		else {
			lon = adjust_lon(Math.atan2(g,h) + _lon_center);
		}
	}
	else {
		// Inverse equations
		x = x - _false_easting;
		y = y - _false_northing;

		con = (_ml0 + y / _scale_factor) / _r_major;
		phi = con;
		for (i=0;;i++) {
			delta_phi=((con + _e1 * Math.sin(2.0*phi) -
					_e2 * Math.sin(4.0*phi) + _e3 * Math.sin(6.0*phi))/_e0) - phi;
			// Commented in GCTP code...
			//delta_phi = ((con + e1 * sin(2.0*phi) - e2 * sin(4.0*phi)) / e0) - phi;
			phi += delta_phi;
			if (Math.abs(delta_phi) <= EPSLN) {
				break;
			}
			if (i >= max_iter) { 
				Message.printWarning ( 3, "UTMProjection.unProject", "Latitude failed to converge"); 
				lat = 0.0;
				lon = 0.0;
				break;
			}
		}

		if (Math.abs(phi) < HALF_PI) {
			sin_phi = Math.sin(phi);
			cos_phi = Math.cos(phi);
			tan_phi = Math.tan(phi);
			c = _esp*cos_phi*cos_phi;
			cs   = c*c;
			t    = tan_phi*tan_phi;
			ts   = t*t;
			con  = 1.0 - _es*sin_phi*sin_phi; 
			n    = _r_major /Math.sqrt(con);
			r    = n * (1.0 - _es) / con;
			d    = x / (n * _scale_factor);
			ds   = d*d;
			lat = phi - (n*tan_phi*ds/r)*(0.5 -
				ds/24.0*(5.0 + 3.0*t + 10.0*c - 4.0*cs -
				9.0*_esp - ds/30.0 * (61.0 + 90.0*t +
				298.0*c + 45.0*ts - 252.0*_esp - 3.0*cs)));
			lon = adjust_lon(_lon_center + (d*(1.0 -
				ds/6.0*(1.0 + 2.0*t + c - ds/20.0*(5.0 - 2.0*c +
				28.0*t - 3.0*cs + 8.0*_esp +
				24.0*ts)))/cos_phi));
		}
		else {
			lat = HALF_PI * sign(y);
			lon = _lon_center;
		}
	}
	double R2D = 57.2957795131;
	if(reuse_point) {
		p.x=lon*R2D;
		p.y=lat*R2D;
		p.xmax = p.x;
		p.xmin = p.x;
		p.ymax = p.y;
		p.ymin = p.y;
		return p;
	}	
	//if reuse is false, create new GRPoint
	else {
		return new GRPoint(lon*R2D,lat*R2D);
	}
}
	
}