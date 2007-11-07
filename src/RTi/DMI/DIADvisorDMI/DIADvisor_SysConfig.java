// ----------------------------------------------------------------------------
// DIADvisor_SysConfig.java - corresponds to DIADvisor SysConfig table
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-03-30	Steven A. Malers, RTi	Initial version.  Copy and modify
//					RiversideDB_SiteDef.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI.DIADvisorDMI;

import java.util.Date;

import RTi.DMI.DMIDataObject;
import RTi.DMI.DMIUtil;

/**
The DIADvisor_SysConfig class stores data records from the DIADvisor SysConfig
table.  This class is incomplete - only Interval is handled at this time.
*/
public class DIADvisor_SysConfig extends DMIDataObject
{

protected String _Environment = DMIUtil.MISSING_STRING;
protected String _Units = DMIUtil.MISSING_STRING;
protected int _Alarms = DMIUtil.MISSING_INT;
protected int _Notify = DMIUtil.MISSING_INT;
protected int _Interval = DMIUtil.MISSING_INT;
protected String _Group = DMIUtil.MISSING_STRING;
protected double _RainScale = DMIUtil.MISSING_DOUBLE;
protected int _AreaRain = DMIUtil.MISSING_INT;
protected int _SubBasinRain = DMIUtil.MISSING_INT;
protected int _RainDisplay = DMIUtil.MISSING_INT;
protected String _Map = DMIUtil.MISSING_STRING;
protected String _Palette = DMIUtil.MISSING_STRING;
protected int _DuplicateTime = DMIUtil.MISSING_INT;
protected String _SaveKey = DMIUtil.MISSING_STRING;
protected String _ArchiveKey = DMIUtil.MISSING_STRING;
protected String _DataKey = DMIUtil.MISSING_STRING;
protected String _TableKey = DMIUtil.MISSING_STRING;
protected int _KeepDays = DMIUtil.MISSING_INT;
protected int _CopyDays = DMIUtil.MISSING_INT;
protected Date _ArchiveTime = DMIUtil.MISSING_DATE;
protected int _LastSeqNum = DMIUtil.MISSING_INT;
protected Date _LastArch = DMIUtil.MISSING_DATE;
protected String _ArchiveDb = DMIUtil.MISSING_STRING;
protected String _CopyDb = DMIUtil.MISSING_STRING;
protected Date _LastUpdate = DMIUtil.MISSING_DATE;
protected double _FFGLow = DMIUtil.MISSING_DOUBLE;
protected double _FFGHigh = DMIUtil.MISSING_DOUBLE;
protected int _MWW = DMIUtil.MISSING_INT;
protected int _AppHandle = DMIUtil.MISSING_INT;
protected int _BuildGeo = DMIUtil.MISSING_INT;
protected String _LocalEmailUser = DMIUtil.MISSING_STRING;
protected String _LocalEmailPassword = DMIUtil.MISSING_STRING;
protected int _BitmapExport = DMIUtil.MISSING_INT;
protected String _BitmapExportFile = DMIUtil.MISSING_STRING;
protected int _HTMLExport = DMIUtil.MISSING_INT;
protected String _HTMLExportDirectory = DMIUtil.MISSING_STRING;
protected Date _HTMLExportFrom = DMIUtil.MISSING_DATE;
protected Date _HTMLExportTo = DMIUtil.MISSING_DATE;

/**
DIADvisor_SiteDef constructor.
*/
public DIADvisor_SysConfig ()
{	super();
}

/**
Cleans up variables when the class is disposed of.  Sets all the member
variables (that aren't primitives) to null.
*/
protected void finalize() throws Throwable
{	_Environment = null;
	_Units = null;
	_Group = null;
	_Map = null;
	_Palette = null;
	_SaveKey = null;
	_ArchiveKey = null;
	_DataKey = null;
	_TableKey = null;
	_ArchiveTime = null;
	_LastArch = null;
	_ArchiveDb = null;
	_CopyDb = null;
	_LastUpdate = null;
	_LocalEmailUser = null;
	_LocalEmailPassword = null;
	_BitmapExportFile = null;
	_HTMLExportDirectory = null;
	_HTMLExportFrom = null;
	_HTMLExportTo = null;
	super.finalize();
}

/**
Returns _Interval
@return _Interval
*/
public int getInterval()
{	return _Interval;
}

/**
Set _Interval.
@param Interval value to assign to _Interval.
*/
public void setInterval ( int Interval )
{	_Interval = Interval;
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

} // End DIADvisor_SysConfig
