package RTi.GR;

import RTi.Util.Message.Message;

/**
This class stores a list of GRPolylineZM, which allows storage of ESRI Arc
shapes (with Z and measure).  Data are public to
increase performance during draws but the set methods should be used to set
data.  Currently, the number of polylines cannot be dynamically extended.
*/
public class GRPolylineZMList extends GRShape
{

/**
Number of polylines.
*/
public int npolylines = 0;

/**
Total number of points.
*/
public int total_npts = 0;

/**
List of polylines.
*/
public GRPolylineZM[] polylines = null;

/**
Construct with zero polylines.
*/
public GRPolylineZMList ()
{
	super ();
	type = POLYLINE_ZM_LIST;
	total_npts = 0;
	xmin = xmax = 0.0;
	ymin = ymax = 0.0;
}

/**
Construct with zero polylines and set index.
@param att_index attribute index.
*/
public GRPolylineZMList ( long att_index )
{
	super ( att_index );
	type = POLYLINE_ZM_LIST;
	total_npts = 0;
	xmin = xmax = 0.0;
	ymin = ymax = 0.0;
}

/**
Construct with the specified number of polylines.
The array space for the polylines
is created but not initialized.  setPolyline should then be called to set the polyline.
@param npolylines_set Number of polylines.
*/
public GRPolylineZMList ( int npolylines_set )
{
	super ();
	type = POLYLINE_ZM_LIST;
	setNumPolylines ( npolylines_set );
}

/**
Copy constructor.  A deep copy is made.
@param polylinelist the polylineList to copy.
*/
public GRPolylineZMList ( GRPolylineZMList polylinelist )
{
	super ( polylinelist.index );
	type = POLYLINE_ZM_LIST;
	setNumPolylines ( polylinelist.npolylines );
	for ( int i = 0; i < npolylines; i++ ) {
		setPolyline ( i, new GRPolylineZM ( polylinelist.polylines[i]) );
	}
	// Set base class data here...
	xmin = polylinelist.xmin;
	xmax = polylinelist.xmax;
	ymin = polylinelist.ymin;
	ymax = polylinelist.ymax;
	limits_found = polylinelist.limits_found;
	is_visible = polylinelist.is_visible;
	is_selected = polylinelist.is_selected;
	associated_object = polylinelist.associated_object;
}

/**
Returns true if the polylineList matches the one being compared.
Each polyline is compared.  The number of polylines must agree.
@return true if the polylineList matches the one being compared.  
*/
public boolean equals ( GRPolylineZMList polylinelist )
{
	if ( npolylines != polylinelist.npolylines ) {
		return false;
	}
	for ( int i = 0; i < npolylines; i++ ) {
		if ( !polylines[i].equals(polylinelist.polylines[i]) ) {
			return false;
		}
	}
	return true;
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
Returns the number of polylines.
@return the number of polylines.
*/
public int getNumPolylines ( )
{	
	return npolylines;
}

/**
Returns a polyline from the array or null if outside the bounds of the array.
A reference to the polyline is returned.  Reference the public data directly to speed performance.
@param i index position in polyline array (starting at zero).
@return a polyline from the array or null if outside the bounds of the array.
*/
public GRPolylineZM getPolyline ( int i )
{	
	if ( (i < 0) || (i > (npolylines - 1)) ) {
		return null;
	}
	else {
		return polylines[i];
	}
}

/**
Reinitialize the polylines array to the specified size.  The polyline data must be re-set.
@param npolylines_set Number of polylines to size the polylines array.
*/
public void setNumPolylines ( int npolylines_set )
{	
	try {
		polylines = new GRPolylineZM[npolylines_set];
		npolylines = npolylines_set;
		xmin = xmax = ymin = ymax = 0.0;
		limits_found = false;
	}
	catch ( Throwable t ) {
		Message.printWarning ( 2, "GRPolylineZMList.setNumPolylines",
		"Error allocating memory for " + npolylines_set + " polylines." );
	}
}

/**
Set the polyline at an index.  It is assumed that the number of polylines has
already been specified, thus allocating space for the polylines.  A reference 
to the given polyline is saved, not a copy of the data.
@param i Array position for polyline (starting at zero).
@param polyline Polyline to set (null polylines are allowed).
*/
public void setPolyline ( int i, GRPolylineZM polyline )
{	
	if ( (i < 0) || (i > (npolylines - 1)) ) {
		return;
	}
	polylines[i] = polyline;
	if ( !limits_found ) {
		// Set the limits...
		xmin = polyline.xmin;
		xmax = polyline.xmax;
		ymin = polyline.ymin;
		ymax = polyline.ymax;
		limits_found = true;
	}
	else {
		if ( polyline.xmax > xmax ) {
			xmax = polyline.xmax;
		}
		if ( polyline.xmin < xmin ) {
			xmin = polyline.xmin;
		}
		if ( polyline.ymax > ymax ) {
			ymax = polyline.ymax;
		}
		if ( polyline.ymin < ymin ) {
			ymin = polyline.ymin;
		}
	}
}

}