// TSDataPosition - simple class for returning data position in internal array

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

package RTi.TS;

/**
The TSDataPosition class is used by the getDataPosition methods in time series
classes to return data positions within the data arrays.
This class supports up to three array dimensions for the position.
It is used by accessor functions in the TS library but should generally not be used outside the library.
For example, if parallel data MonthTS and string StringMonthTS time series are maintained,
TSDataPosition can be used to look up the position in one for access in another.
In this way, only MonthTS has the position lookup code.
*/
public class TSDataPosition
{

private int	_position1;
private int	_position2;
private int	_position3;
private boolean	_found;

/**
Default constructor.  Each dimension is initialized to -1.
*/
TSDataPosition () {
	_found = false;
	_position1 = -1;
	_position2 = -1;
	_position3 = -1;
}

/**
Return the first array dimension position.
@return The first array dimension position.
*/
public int getPosition1 () {
	return _position1;
}

/**
Return the second array dimension position.
@return The second array dimension position.
*/
public int getPosition2 () {
	return _position2;
}

/**
Return The third array dimension position.
@return The third array dimension position.
*/
public int getPosition3 () {
	return _position3;
}

/**
Determine whether the position has been found.
@return true if the position has been found, false if not.
*/
public boolean positionFound() {
	return _found;
}

/**
Set the array dimension position for the first dimension.
@param position1 The first array dimension position.
*/
public void setPosition ( int position1 ) {
	_position1 = position1;
	_found = true;
}

/**
Set the array dimension position for the first and second dimension.
@param position1 The first array dimension position.
@param position2 The second array dimension position.
*/
public void setPosition ( int position1, int position2 ) {
	_position1 = position1;
	_position2 = position2;
	_found = true;
}

/**
Set the array dimension position for the first, second, and third dimension.
@param position1 The first array dimension position.
@param position2 The second array dimension position.
@param position3 The third array dimension position.
*/
public void setPosition ( int position1, int position2, int position3 ) {
	_position1 = position1;
	_position2 = position2;
	_position3 = position3;
	_found = true;
}

}