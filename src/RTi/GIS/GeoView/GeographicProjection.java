// GeographicProjection - class for geographic projection

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

import RTi.GR.GRPoint;

/**
The Geographic projection corresponds to latitude and longitude.  Currently this
class does not handle variations in datums and is used more as a place-holder
(i.e., to know when a geographic projection is being used).
*/
public class GeographicProjection extends GeoProjection {

/**
This projection does not do any conversions but acts as a place-holder for
geographic data.
*/
public GeographicProjection() {
	super ( "Geographic" );
}

/**
Get the number of kilometers for a unit of the projection grid.  The
point that is used can be reused if necessary to increase performance.
<b>This is currently a rough estimate!</b>
@param p As input, specifies the location (in HRAP units) at which to determine the scale.
@param reuse_point Indicates whether the point that is passed in should be
re-used for the output (doing so saves memory).
*/
public GRPoint getKilometersForUnit ( GRPoint p, boolean reuse_point )
{	// Don't really know the right formula yet but estimate:
	// Earth radius = 6378 KM
	// Circumferance = 2PiR = 40,074 KM
	// KM/degree = 40,074/360 = 111.3 KM
	// Need to find the correct equation accounting for the starting coordinate.
	double xscale = 111.3*Math.cos(p.y*1.745329251994328e-2);
	if ( reuse_point ) {
		p.x = p.y = xscale;
		return p;
	}
	else {
		return new GRPoint ( xscale, xscale );
	}
}

/**
Project latitude and longitude to the geographic coordinate system.  This just
returns the original coordinates.
@return the projected (to longitude and latitude) points.
@param p Point to project from latitude and longitude. Assumes point comes in format (lon, lat)
@param reuse_point Indicates whether the point that is passed in should be re-used for the output 
(doing so saves memory).
*/
public GRPoint project ( GRPoint p, boolean reuse_point)
{	if ( reuse_point ) {
		return p;
	}
	// create a new point to return
	else {
		return new GRPoint ( p );
	}
}

/**
Un-project coordinates back to latitude and longitude.  This returns the same coordinates.
@return the un-projected points.  Assumes point comes in as (longitude, latitude)
@param p Point to un-project to latitude and longitude.
@param reuse_point Indicates whether the point that is passed in should be
re-used for the output (doing so saves memory).
*/
public GRPoint unProject(GRPoint p, boolean reuse_point)
{	if ( reuse_point ) {
		return p;
	}	
	else {
		return new GRPoint ( p );
	}
}
	
}
