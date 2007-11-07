// ----------------------------------------------------------------------------
// DIADvisor_SensorDef.java - corresponds to DIADvisor SensorDef table
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
The DIADvisor_SensorDef class store data records from the DIADvisor SensorDef
table.
*/
public class DIADvisor_SensorDef extends DMIDataObject
{

// From table SensorDef, in order of table design

protected int _SensorID = DMIUtil.MISSING_INT;
protected int _SiteID = DMIUtil.MISSING_INT;
protected String _Type = DMIUtil.MISSING_STRING;
protected String _Group = DMIUtil.MISSING_STRING;
protected String _Description = DMIUtil.MISSING_STRING;
protected int _MaxCount = DMIUtil.MISSING_INT;
protected int _MinCount = DMIUtil.MISSING_INT;
protected int _PosDelta = DMIUtil.MISSING_INT;
protected int _NegDelta = DMIUtil.MISSING_INT;
protected String _RatingType = DMIUtil.MISSING_STRING;
protected String _RatingInterpolation = DMIUtil.MISSING_STRING;
protected double _RatingShift = DMIUtil.MISSING_DOUBLE;
protected double _CalibrationOffset = DMIUtil.MISSING_DOUBLE;
protected Date _CalibrationDate = DMIUtil.MISSING_DATE;
protected double _Slope = DMIUtil.MISSING_DOUBLE;
protected double _ReferenceLevel = DMIUtil.MISSING_DOUBLE;
protected String _DisplayUnits = DMIUtil.MISSING_STRING;
protected int _Decimal = DMIUtil.MISSING_INT;
protected String _DisplayUnits2 = DMIUtil.MISSING_STRING;
protected int _Decimal2 = DMIUtil.MISSING_INT;
protected boolean _InService = false;
protected boolean _Suspect = false;
protected boolean _Alarms = false;
protected boolean _Notify = false;
protected double _Timeout = DMIUtil.MISSING_DOUBLE;
protected boolean _Children = false;
protected Date _MostRecentTime = DMIUtil.MISSING_DATE;
protected double _MostRecentData = DMIUtil.MISSING_DOUBLE;
protected Date _LastValidTime = DMIUtil.MISSING_DATE;
protected double _LastValidData = DMIUtil.MISSING_DOUBLE;
protected double _LastCount = DMIUtil.MISSING_DOUBLE;
protected String _Equation = DMIUtil.MISSING_STRING;
protected String _Equation2 = DMIUtil.MISSING_STRING;
protected Date _LastUpdate = DMIUtil.MISSING_DATE;

/**
DIADvisor_SensorDef constructor.
*/
public DIADvisor_SensorDef ()
{	super();
}

/**
Cleans up variables when the class is disposed of.  Sets all the member
variables (that aren't primitives) to null.
*/
protected void finalize() throws Throwable
{	_Type = null;
	_Group = null;
	_Description = null;
	_RatingType = null;
	_RatingInterpolation = null;
	_CalibrationDate = null;
	_DisplayUnits = null;
	_DisplayUnits2 = null;
	_MostRecentTime = null;
	_LastValidTime = null;
	_Equation = null;
	_Equation2 = null;
	_LastUpdate = null;
	super.finalize();
}

/**
Returns _Alarms
@return _Alarms
*/
public boolean getAlarms()
{	return _Alarms;
}

/**
Returns _CalibrationDate
@return _CalibrationDate
*/
public Date getCalibrationDate()
{	return _CalibrationDate;
}

/**
Returns _CalibrationOffset
@return _CalibrationOffset
*/
public double getCalibrationOffset()
{	return _CalibrationOffset;
}

/**
Returns _Children
@return _Children
*/
public boolean getChildren()
{	return _Children;
}

/**
Returns _Decimal
@return _Decimal
*/
public int getDecimal()
{	return _Decimal;
}

/**
Returns _Decimal2
@return _Decimal2
*/
public int getDecimal2()
{	return _Decimal2;
}

/**
Returns _Description
@return _Description
*/
public String getDescription()
{	return _Description;
}

/**
Returns _DisplayUnits
@return _DisplayUnits
*/
public String getDisplayUnits()
{	return _DisplayUnits;
}

/**
Returns _DisplayUnits2
@return _DisplayUnits2
*/
public String getDisplayUnits2()
{	return _DisplayUnits2;
}

/**
Returns _Equation
@return _Equation
*/
public String getEquation()
{	return _Equation;
}

/**
Returns _Equation2
@return _Equation2
*/
public String getEquation2()
{	return _Equation2;
}

/**
Returns _Group
@return _Group
*/
public String getGroup()
{	return _Group;
}

/**
Returns _InService
@return _InService
*/
public boolean getInService()
{	return _InService;
}

/**
Returns _LastCount
@return _LastCount
*/
public double getLastCount()
{	return _LastCount;
}

/**
Returns _LastUpdate
@return _LastUpdate
*/
public Date getLastUpdate()
{	return _LastUpdate;
}

/**
Returns _LastValidData
@return _LastValidData
*/
public double getLastValidData()
{	return _LastValidData;
}

/**
Returns _LastValidTime
@return _LastValidTime
*/
public Date getLastValidTime()
{	return _LastValidTime;
}

/**
Returns _MostRecentData
@return _MostRecentData
*/
public double getMostRecentData()
{	return _MostRecentData;
}

/**
Returns _MostRecentTime
@return _MostRecentTime
*/
public Date getMostRecentTime()
{	return _MostRecentTime;
}

/**
Returns _MaxCount
@return _MaxCount
*/
public int getMaxCount()
{	return _MaxCount;
}

/**
Returns _MinCount
@return _MinCount
*/
public int getMinCount()
{	return _MinCount;
}

/**
Returns _NegDelta
@return _NegDelta
*/
public int getNegDelta()
{	return _NegDelta;
}

/**
Returns _Notify
@return _Notify
*/
public boolean getNotify()
{	return _Notify;
}

/**
Returns _PosDelta
@return _PosDelta
*/
public int getPosDelta()
{	return _PosDelta;
}

/**
Returns _RatingInterpolation
@return _RatingInterpolation
*/
public String getRatingInterpolation()
{	return _RatingInterpolation;
}

/**
Returns _RatingShift
@return _RatingShift
*/
public double getRatingShift()
{	return _RatingShift;
}

/**
Returns _RatingType
@return _RatingType
*/
public String getRatingType()
{	return _RatingType;
}

/**
Returns _ReferenceLevel
@return _ReferenceLevel
*/
public double getReferenceLevel()
{	return _ReferenceLevel;
}

/**
Return _SensorID
@return _SensorID
*/
public int getSensorID()
{	return _SensorID;
}

/**
Return _SiteID
@return _SiteID
*/
public int getSiteID()
{	return _SiteID;
}

/**
Returns _Slope
@return _Slope
*/
public double getSlope()
{	return _Slope;
}

/**
Returns _Suspect
@return _Suspect
*/
public boolean getSuspect()
{	return _Suspect;
}

/**
Returns _Timeout
@return _Timeout
*/
public double getTimeout()
{	return _Timeout;
}

/**
Returns _Type
@return _Type
*/
public String getType()
{	return _Type;
}

/**
Set _Alarms
@param Alarms value to assign to _Alarms.
*/
public void setAlarms ( boolean Alarms )
{	_Alarms = Alarms;
}

/**
Set _CalibrationDate
@param CalibrationDate value to assign to _CalibrationDate.
*/
public void setCalibrationDate ( Date CalibrationDate )
{	_CalibrationDate = CalibrationDate;
}

/**
Set _CalibrationOffset
@param CalibrationOffset value to assign to _CalibrationOffset.
*/
public void setCalibrationOffset ( double CalibrationOffset )
{	_CalibrationOffset = CalibrationOffset;
}

/**
Set _Children
@param Children value to assign to _Children.
*/
public void setChildren ( boolean Children )
{	_Children = Children;
}

/**
Set _Decimal
@param Decimal value to assign to _Decimal.
*/
public void setDecimal ( int Decimal )
{	_Decimal = Decimal;
}

/**
Set _Decimal2
@param Decimal2 value to assign to _Decimal2.
*/
public void setDecimal2 ( int Decimal2 )
{	_Decimal2 = Decimal2;
}

/**
Set _Description
@param Description value to assign to _Description.
*/
public void setDescription ( String Description )
{	if ( Description != null ) {
		_Description = Description;
	}
}

/**
Set _DisplayUnits
@param DisplayUnits value to assign to _DisplayUnits.
*/
public void setDisplayUnits ( String DisplayUnits )
{	if ( DisplayUnits != null ) {
		_DisplayUnits = DisplayUnits;
	}
}

/**
Set _DisplayUnits2
@param DisplayUnits2 value to assign to _DisplayUnits2.
*/
public void setDisplayUnits2 ( String DisplayUnits2 )
{	if ( DisplayUnits2 != null ) {
		_DisplayUnits2 = DisplayUnits2;
	}
}

/**
Set _Equation
@param Equation value to assign to _Equation.
*/
public void setEquation ( String Equation )
{	if ( Equation != null ) {
		_Equation = Equation;
	}
}

/**
Set _Equation2
@param Equation2 value to assign to _Equation2.
*/
public void setEquation2 ( String Equation2 )
{	if ( Equation2 != null ) {
		_Equation2 = Equation2;
	}
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
Set _InService
@param InService value to assign to _InService.
*/
public void setInService ( boolean InService )
{	_InService = InService;
}

/**
Set _LastCount
@param LastCount value to assign to _LastCount.
*/
public void setLastCount ( double LastCount )
{	_LastCount = LastCount;
}

/**
Set _LastUpdate
@param LastUpdate value to assign to _LastUpdate.
*/
public void setLastUpdate ( Date LastUpdate )
{	_LastUpdate = LastUpdate;
}

/**
Set _LastValidData
@param LastValidData value to assign to _LastValidData.
*/
public void setLastValidData ( double LastValidData )
{	_LastValidData = LastValidData;
}

/**
Set _LastValidTime
@param LastValidTime value to assign to _LastValidTime.
*/
public void setLastValidTime ( Date LastValidTime )
{	_LastValidTime = LastValidTime;
}

/**
Set _MaxCount
@param MaxCount value to assign to _MaxCount.
*/
public void setMaxCount ( int MaxCount )
{	_MaxCount = MaxCount;
}

/**
Set _MinCount
@param MinCount value to assign to _MinCount.
*/
public void setMinCount ( int MinCount )
{	_MinCount = MinCount;
}

/**
Set _MostRecentData
@param MostRecentData value to assign to _MostRecentData.
*/
public void setMostRecentData ( double MostRecentData )
{	_MostRecentData = MostRecentData;
}

/**
Set _MostRecentTime
@param MostRecentTime value to assign to _MostRecentTime.
*/
public void setMostRecentTime ( Date MostRecentTime )
{	_MostRecentTime = MostRecentTime;
}

/**
Set _NegDelta
@param NegDelta value to assign to _NegDelta.
*/
public void setNegDelta ( int NegDelta )
{	_NegDelta = NegDelta;
}

/**
Set _Notify
@param Notify value to assign to _Notify.
*/
public void setNotify ( boolean Notify )
{	_Notify = Notify;
}

/**
Set _PosDelta
@param PosDelta value to assign to _PosDelta.
*/
public void setPosDelta ( int PosDelta )
{	_PosDelta = PosDelta;
}

/**
Set _RatingInterpolation
@param RatingInterpolation value to assign to _RatingInterpolation.
*/
public void setRatingInterpolation ( String RatingInterpolation )
{	if ( RatingInterpolation != null ) {
		_RatingInterpolation = RatingInterpolation;
	}
}

/**
Set _RatingShift.
@param RatingShift value to assign to _RatingShift.
*/
public void setRatingShift ( double RatingShift )
{	_RatingShift = RatingShift;
}

/**
Set _RatingType
@param RatingType value to assign to _RatingType.
*/
public void setRatingType ( String RatingType )
{	if ( RatingType != null ) {
		_RatingType = RatingType;
	}
}

/**
Set _ReferenceLevel
@param ReferenceLevel value to assign to _ReferenceLevel.
*/
public void setReferenceLevel ( double ReferenceLevel )
{	_ReferenceLevel = ReferenceLevel;
}

/**
Set _SensorID
@param SensorID value to assign to _SensorID.
*/
public void setSensorID ( int SensorID )
{	_SensorID = SensorID;
}

/**
Set _SiteID
@param SiteID value to assign to _SiteID.
*/
public void setSiteID ( int SiteID )
{	_SiteID = SiteID;
}

/**
Set _Slope
@param Slope value to assign to _Slope.
*/
public void setSlope ( double Slope )
{	_Slope = Slope;
}

/**
Set _Suspect
@param Suspect value to assign to _Suspect.
*/
public void setSuspect ( boolean Suspect )
{	_Suspect = Suspect;
}

/**
Set _Timeout.
@param Timeout value to assign to _Timeout.
*/
public void setTimeout ( double Timeout )
{	_Timeout = Timeout;
}

/**
Set _Type
@param Type value to assign to _Type.
*/
public void setType ( String Type )
{	if ( Type != null ) {
		_Type = Type;
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

} // End DIADvisor_SensorDef
