// UnknownProjection - class to indicate unknown projection

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
The unknown projection can be used as a place-holder when a projection is not
know.  Many times for a GeoView project no projections will be defined so a place-holder is needed just to do comparisons.
*/
public class UnknownProjection extends GeoProjection {

/**
This projection does not do any conversions and acts as a place-holder for projections.
*/
public UnknownProjection() {
	super ( "Unknown" );
}

/**
This just returns the original coordinates.  If a comparison of projections is made, this method will likely never be called.
@return the projected points.
@param p Point to project from latitude and longitude. Assumes point comes in format (lon, lat)
@param reuse_point Indicates whether the point that is passed in should be re-used for the output (doing so saves memory).
*/
public GRPoint project ( GRPoint p, boolean reuse_point) {
	if ( reuse_point ) {
		return p;
	}
	// Create a new point to return.
	else {
		return new GRPoint ( p );
	}
}

/**
Un-project coordinates back to latitude and longitude.  This returns the same coordinates.
@return the un-projected points.
@param p Point to un-project to latitude and longitude.
@param reuse_point Indicates whether the point that is passed in should be re-used for the output (doing so saves memory).
*/
public GRPoint unProject(GRPoint p, boolean reuse_point) {
	if ( reuse_point ) {
		return p;
	}
	else {
		return new GRPoint ( p );
	}
}

}