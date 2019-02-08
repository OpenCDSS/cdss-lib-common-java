// GeoViewZoomHistory - history of zooms to allow tracing zoom history

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

// ----------------------------------------------------------------------------
// History:
// ----------------------------------------------------------------------------
// 08 Jul 1999	Catherine E.		Implemented code.
//		Nutting-Lane, RTi
// 07 Sep 1999	CEN, RTi		Added clear function.
// 2001-10-17	Steven A. Malers, RTi	Review javadoc.  Add finalize().  Set
//					unused data to null to help garbage
//					collection.  Fix bug where
//					getDataLimits() was not checking the
//					index properly to throw the exception.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.util.ArrayList;
import java.util.List;
import RTi.GR.GRLimits;

/**
Ghe GeoViewZoomHistory class saves data and device limits from zoom events in a
GeoView.  Any application using this class must implement GeoViewListener and
add the geoViewListener to the desired GeoView.
The application must instantiate a GeoViewZoomHistory and add
to the zoom history in the following manner:
<p>

<pre>
GeoView			_canvas = new GeoView();
GeoViewZoomHistory 	_zoomHistory = new GeoViewZoomHistory();

_canvas.addGeoViewListener ( this );

public geoViewZoom ( GRLimits devlim, GRLimits datalim )
{	_zoomHistory.addZoom ( devlim, datalim );
}
</pre>
<b>This class will be phased out as the new GeoViewPanel is used exclusively.
Its reference window does not have a zoom history.</b>
*/
public class GeoViewZoomHistory 
{

private List<GRLimits> _dataLimitsHistory;
private List<GRLimits> _devLimitsHistory;
private int _currentIndex;

/**
Construct a zoom history.
*/
public GeoViewZoomHistory ()
{	_currentIndex = -1;
	_dataLimitsHistory = new ArrayList<GRLimits>();
	_devLimitsHistory = new ArrayList<GRLimits>();
}

/**
Clean up for garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	_dataLimitsHistory = null;
	_devLimitsHistory = null;
	super.finalize();
}

/**
Save the zoom limits at the current index.  Therefore, if the current index is
not at the end of the zoom history, this set of limits will be inserted in the
history at the appropriate location.
@param devLimits current device limits to store
@param dataLimits current data limits to store
*/
public void addHistory ( GRLimits devLimits, GRLimits dataLimits )
{	_currentIndex++;

	if ( _currentIndex == _dataLimitsHistory.size()) {
		_dataLimitsHistory.add ( dataLimits );
	}
	else {	_dataLimitsHistory.add( _currentIndex, dataLimits );
	}

	if ( _currentIndex == _devLimitsHistory.size()) {
		_devLimitsHistory.add ( devLimits );
	}
	else {	_devLimitsHistory.add ( _currentIndex, devLimits );
	}
}

/**
Reset the members, which is useful if a new map is being read.
*/
public void clear ()
{	_currentIndex = -1;
	_dataLimitsHistory.clear();
	_devLimitsHistory.clear();
}

/**
Return the current index in the zoom history.
@return the current index in the zoom history.
*/
public int getCurrentIndex ( )
{	return _currentIndex;
}

/**
Return the current data limits.
@return Data limits associated with the current setting.
*/
public GRLimits getCurrentDataLimits ()
throws Exception
{	return getDataLimits ( _currentIndex );
}

/**
Return the data limits for the given zoom index position.
@return Data limits associated with the given index.
@param index index of desired data limits history.
@throws Exception if index is invalid.
*/
public GRLimits getDataLimits ( int index )
throws Exception
{	if ( (index < 0) || (index >= _dataLimitsHistory.size()) ) {
		throw new Exception ( "Index " + index + " out of bounds (" + 
		_dataLimitsHistory.size() + ")");
	}
	return ((GRLimits)_dataLimitsHistory.get(index));
}

/**
Return the last index in the zoom history.
@return the last index in the zoom history.
*/
public int getLastIndex ()
{	return (_dataLimitsHistory.size() - 1);
}

/**
Retrieve the next data limits if available and increment the current index if
changeCurrentIndex is true.
@return Data limits associated with the next index.
@param changeCurrentIndex indicates whether the currentIndex should be changed.
@exception Exception if there are no next limits.
*/
public GRLimits getNextDataLimits (boolean changeCurrentIndex)
throws Exception
{	int tmpIndex = _currentIndex+1;
	if ( tmpIndex == _dataLimitsHistory.size()) {
		throw new Exception ("No next data limits exist." );
	}

	if ( changeCurrentIndex ) {
		_currentIndex++;
	}

	return getDataLimits ( tmpIndex );
}

/**
Retrieve the previous data limits if available and decrements the current index
if changeCurrentIndex is true.
@return Data limits associated with the previous index.
@param changeCurrentIndex indicates whether the currentIndex should be changed.
@exception Exception if there is no previous limits.
*/
public GRLimits getPreviousDataLimits (boolean changeCurrentIndex)
throws Exception
{	int tmpIndex = _currentIndex-1;
	if ( tmpIndex < 0 ) {
		throw new Exception ("No previous data limits exist." );	
	}

	if ( changeCurrentIndex ) {
		_currentIndex--;
	}

	return getDataLimits ( tmpIndex );
}

} // End GeoViewZoomHistory
