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
Currently the object only contains data/methods to handle the "dirty" flag.
*/
public class DMIDataObject
extends Object {

/**
Flag to indicate whether the data object has been modified.
*/
protected boolean _dirty;

/**
A separate object that can be stored by any data object.  One use is to keep
a clone of the original version of a table record so that after the record is
modified and needs to be rewritten, it can be compared to the original.
*/
protected Object _original = null;

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
@return true if the object is dirty (has been modified).
@deprecated Use setDirty(boolean).
*/
public boolean isDirty ( boolean dirty )
{	_dirty = dirty;
	return _dirty;
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

/**
Checks to see if two strings (of which either or both may be null) are equal.
@param s1 the first String to check.
@param s2 the second String to check.
@return true if the Strings are equal (null == null), false if not.
@deprecated use StringUtil.stringsAreEqual()
*/
public boolean stringsAreEqual(String s1, String s2) {
	if (s1 == null && s2 == null) {
		return true;
	}
	if (s1 == null || s2 == null) {
		return false;
	}
	if (s1.trim().equals(s2.trim())) {
		return true;
	}
	return false;
}

}
