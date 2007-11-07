// ----------------------------------------------------------------------------
// DIADvisor_DataChron.java - corresponds to DIADvisor DataChron table
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-02-06	Steven A. Malers, RTi	Initial version.  Copy and modify
//					DIADvisor_SiteDef.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI.DIADvisorDMI;

import java.util.Date;

import RTi.DMI.DMIDataObject;
import RTi.DMI.DMIUtil;

/**
The DIADvisor_DataChron class store data records from the DIADvisor DataChron
table.
*/
public class DIADvisor_DataChron extends DMIDataObject
{

// From table DataChron

protected Date _DateTime = DMIUtil.MISSING_DATE;
protected int _SensorID = DMIUtil.MISSING_INT;
protected int _Count = DMIUtil.MISSING_INT;
protected String _DataType = DMIUtil.MISSING_STRING;
protected String _Source = DMIUtil.MISSING_STRING;
protected double _DataValue = DMIUtil.MISSING_DOUBLE;
protected double _DataValue2 = DMIUtil.MISSING_DOUBLE;
protected long _SeqNum = DMIUtil.MISSING_LONG;
protected String _Comment = DMIUtil.MISSING_STRING;

/**
DIADvisor_SiteDef constructor.
*/
public DIADvisor_DataChron ()
{	super();
}

/**
Cleans up variables when the class is disposed of.  Sets all the member
variables (that aren't primitives) to null.
*/
protected void finalize() throws Throwable
{	_DateTime = null;
	_DataType = null;
	_Source = null;
	_Comment = null;
	super.finalize();
}

/**
Returns _Comment
@return _Comment
*/
public String getComment()
{	return _Comment;
}

/**
Returns _Count
@return _Count
*/
public int getCount()
{	return _Count;
}

/**
Returns _DateTime
@return _DateTime
*/
public Date getDateTime()
{	return _DateTime;
}

/**
Returns _DataType
@return _DataType
*/
public String getDataType()
{	return _DataType;
}

/**
Returns _DataValue
@return _DataValue
*/
public double getDataValue()
{	return _DataValue;
}

/**
Returns _DataValue2
@return _DataValue2
*/
public double getDataValue2()
{	return _DataValue2;
}

/**
Returns _Source
@return _Source
*/
public String getSource()
{	return _Source;
}

/**
Returns _SensorID
@return _SensorID
*/
public int getSensorID()
{	return _SensorID;
}

/**
Returns _SeqNum
@return _SeqNum
*/
public long getSeqNum()
{	return _SeqNum;
}

/**
Set _Comment
@param Comment value to assign to _Comment.
*/
public void setComment ( String Comment )
{	if ( Comment != null ) {
		_Comment = Comment;
	}
}

/**
Set _Count.
@param Count value to assign to _Count.
*/
public void setCount ( int Count )
{	_Count = Count;
}

/**
Set _DataType
@param DataType value to assign to _DataType.
*/
public void setDataType ( String DataType )
{	if ( DataType != null ) {
		_DataType = DataType;
	}
}

/**
Set _DataValue.
@param DataValue value to assign to _DataValue.
*/
public void setDataValue ( double DataValue )
{	_DataValue = DataValue;
}

/**
Set _DataValue2.
@param DataValue2 value to assign to _DataValue2.
*/
public void setDataValue2 ( double DataValue2 )
{	_DataValue2 = DataValue2;
}

/**
Set _DateTime
@param DateTime value to assign to _DateTime.
*/
public void setDateTime ( Date DateTime )
{	_DateTime = DateTime;
}

/**
Set _SensorID.
@param SensorID value to assign to _SensorID.
*/
public void setSensorID ( int SensorID )
{	_SensorID = SensorID;
}

/**
Set _Source
@param Source value to assign to _Source.
*/
public void setSource ( String Source )
{	if ( Source != null ) {
		_Source = Source;
	}
}

/**
Set _SeqNum.
@param SeqNum value to assign to _SeqNum.
*/
public void setSeqNum ( long SeqNum )
{	_SeqNum = SeqNum;
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

} // End DIADvisor_DataChron
