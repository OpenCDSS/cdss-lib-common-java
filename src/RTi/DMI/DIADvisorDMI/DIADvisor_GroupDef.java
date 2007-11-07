// ----------------------------------------------------------------------------
// DIADvisor_GroupDef.java - corresponds to DIADvisor GroupDef table
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-04-06	Steven A. Malers, RTi	Initial version.  Copy and modify
//					RiversideDB_SiteDef.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI.DIADvisorDMI;

import RTi.DMI.DMIDataObject;
import RTi.DMI.DMIUtil;

/**
The DIADvisor_GroupDef class store data records from the DIADvisor GroupDef
table.
*/
public class DIADvisor_GroupDef extends DMIDataObject
{

// From table GroupDef

protected String _Group = DMIUtil.MISSING_STRING;
protected String _Operation = DMIUtil.MISSING_STRING;
protected String _Units1 = DMIUtil.MISSING_STRING;
protected String _Units2 = DMIUtil.MISSING_STRING;
protected int _Display = DMIUtil.MISSING_INT;

/**
DIADvisor_GroupDef constructor.
*/
public DIADvisor_GroupDef ()
{	super();
}

/**
Cleans up variables when the class is disposed of.  Sets all the member
variables (that aren't primitives) to null.
@exception Throwable if there is an error.
*/
protected void finalize() throws Throwable
{	_Group = DMIUtil.MISSING_STRING;
	_Operation = DMIUtil.MISSING_STRING;
	_Units1 = DMIUtil.MISSING_STRING;
	_Units2 = DMIUtil.MISSING_STRING;
	super.finalize();
}

/**
Returns _Display
@return _Display
*/
public int getDisplay()
{	return _Display;
}

/**
Returns _Group
@return _Group
*/
public String getGroup()
{	return _Group;
}

/**
Returns _Operation
@return _Operation
*/
public String getOperation()
{	return _Operation;
}

/**
Returns _Units1
@return _Units1
*/
public String getUnits1()
{	return _Units1;
}

/**
Returns _Units2
@return _Units2
*/
public String getUnits2()
{	return _Units2;
}

/**
Set _Display.
@param Display value to assign to _Display.
*/
public void setDisplay ( int Display )
{	_Display = Display;
}

/**
Set _Group
@param Group value to assign to _Group.
*/
public void setGroup ( String Group )
{	if ( Group != null ) {
		_Group = Group;
	}
}

/**
Set _Operation
@param Operation value to assign to _Operation.
*/
public void setOperation ( String Operation )
{	if ( Operation != null ) {
		_Operation = Operation;
	}
}

/**
Set _Units1
@param Units1 value to assign to _Units1.
*/
public void setUnits1 ( String Units1 )
{	if ( Units1 != null ) {
		_Units1 = Units1;
	}
}

/**
Set _Units2
@param Units2 value to assign to _Units2.
*/
public void setUnits2 ( String Units2 )
{	if ( Units2 != null ) {
		_Units2 = Units2;
	}
}

/** 
Return a string representation of this object.
@return a string representation of this object.
*/
public String toString()
{	return "";
/*
	return ( "RiversideDB_MeasLoc{" 		+ "\n" +
		"MeasLoc_num:  " + _MeasLoc_num		+ "\n" +
		"Geoloc_num:   " + _Geoloc_num		+ "\n" +
		"Identifier:   " + _Identifier		+ "\n" +
		"MeasLoc_name: " + _MeasLoc_name	+ "\n" +
		"Source_abbreb:" + _Source_abbrev	+ "\n" + 
		"Meas_loc_type:" + _Meas_loc_type	+ "\n" + 
		"Comment:      " + _Comment		+ "}"
	);
*/
}

} // End DIADvisor_GroupDef
