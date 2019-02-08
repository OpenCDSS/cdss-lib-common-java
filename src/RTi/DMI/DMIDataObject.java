// DMIDataObject - base class for data objects

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

//-----------------------------------------------------------------------------
// DMIDataObject - base class for data objects
//-----------------------------------------------------------------------------
// History:
//
// 2002-06-25	Steven A. Malers, RTi	Initial version.
// 2004-01-19	SAM, RTi		Change setDirty(boolean) to
//					setDirty(boolean) to be more consistent
//					with other Java code.
// 2004-01-26	J. Thomas Sapienza, RTi	* Added _newRecord.
//					* Added _object.
// 2004-01-30	JTS, RTi		* Removed _newRecord.
//					* Renamed _object to _original.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.DMI;

/**
The DMIDataObject class is the base class for objects managed with DMI classes.
Currently the object only contains data/methods to handle the "dirty" flag and original copy of the data.
*/
public class DMIDataObject
extends Object {

/**
Flag to indicate whether the data object has been modified.
*/
private boolean _dirty;

/**
A separate object that can be stored by any data object.  One use is to keep
a clone of the original version of a table record so that after the record is
modified and needs to be rewritten, it can be compared to the original.
*/
private Object _original = null;

/**
Constructor. 
*/
public DMIDataObject ()
{	_dirty = false;
	_original = null;
}

/**
Returns the object stored in this object.
@return the object stored in this object.
*/
public Object getOriginal() {
	return _original;
}

/**
Indicate whether the object is dirty (has been modified).
@return true if the object is dirty (has been modified).
*/
public boolean isDirty()
{	return _dirty;
}

/**
Set whether the object is dirty (has been modified).
This method is mean to be called after the initial database read, indicating
a change by an application.
@param dirty true if the object has been modified after the read.
*/
public void setDirty ( boolean dirty )
{	_dirty = dirty;
}

/**
Sets the original version of this record.
*/
public void setOriginal(Object original) {
	_original = original;
}

}
