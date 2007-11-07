// ----------------------------------------------------------------------------
// DIADvisor_RegularTSRecord.java - corresponds to DIADvisor regular interval
//					time series tables
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-02-16	Steven A. Malers, RTi	Initial version.  Copy and modify
//					DIADvisor_DataChron.
// 2003-04-04	SAM, RTi		Rename from DIADvisor_TSRecord.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI.DIADvisorDMI;

import java.util.Date;

import RTi.DMI.DMIDataObject;
import RTi.DMI.DMIUtil;

/**
The DIADvisor_RegularTSRecord class store data records from the DIADvisor
Interval, Hour, and Day tables, which all have the same format.
*/
public class DIADvisor_RegularTSRecord extends DMIDataObject
{

// From table Interval, Hour, or Day

protected Date _StartTime = DMIUtil.MISSING_DATE;
protected int _SensorID = DMIUtil.MISSING_INT;
protected int _Count = DMIUtil.MISSING_INT;
protected double _Value = DMIUtil.MISSING_DOUBLE;

/**
DIADvisor_RegularTSRecord constructor.
*/
public DIADvisor_RegularTSRecord ()
{	super();
}

/**
Cleans up variables when the class is disposed of.  Sets all the member
variables (that aren't primitives) to null.
*/
protected void finalize() throws Throwable
{	_StartTime = null;
	super.finalize();
}

/**
Returns _Count
@return _Count
*/
public int getCount()
{	return _Count;
}

/**
Returns _SensorID
@return _SensorID
*/
public int getSensorID()
{	return _SensorID;
}

/**
Returns _StartTime
@return _StartTime
*/
public Date getStartTime()
{	return _StartTime;
}

/**
Returns _Value
@return _Value
*/
public double getValue()
{	return _Value;
}

/**
Set _Count.
@param Count value to assign to _Count.
*/
public void setCount ( int Count )
{	_Count = Count;
}

/**
Set _SensorID.
@param SensorID value to assign to _SensorID.
*/
public void setSensorID ( int SensorID )
{	_SensorID = SensorID;
}

/**
Set _StartTime
@param StartTime value to assign to _StartTime.
*/
public void setStartTime ( Date StartTime )
{	_StartTime = StartTime;
}

/**
Set _Value.
@param Value value to assign to _Value.
*/
public void setValue ( double Value )
{	_Value = Value;
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

} // End DIADvisor_RegularTSRecord
