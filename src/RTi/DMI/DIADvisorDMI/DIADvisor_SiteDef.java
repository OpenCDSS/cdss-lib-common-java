// ----------------------------------------------------------------------------
// DIADvisor_SiteDef.java - corresponds to DIADvisor SiteDef table
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-02-02	Steven A. Malers, RTi	Initial version.  Copy and modify
//					RiversideDB_MeasLoc.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI.DIADvisorDMI;

import java.util.Date;

import RTi.DMI.DMIDataObject;
import RTi.DMI.DMIUtil;

/**
The DIADvisor_SiteDef class store data records from the DIADvisor SiteDef table.
*/
public class DIADvisor_SiteDef extends DMIDataObject
{

// From table SiteDef

protected String _SiteName = DMIUtil.MISSING_STRING;
protected int _SiteID = DMIUtil.MISSING_INT;
protected double _Latitude = DMIUtil.MISSING_DOUBLE;
protected double _Longitude = DMIUtil.MISSING_DOUBLE;
protected double _XCoord = DMIUtil.MISSING_DOUBLE;
protected double _YCoord = DMIUtil.MISSING_DOUBLE;
protected long _PKey = DMIUtil.MISSING_LONG;
protected String _RepeaterGroup = DMIUtil.MISSING_STRING;
protected double _Elevation = DMIUtil.MISSING_DOUBLE;
protected String _SitePicture = DMIUtil.MISSING_STRING;
protected String _Zone = DMIUtil.MISSING_STRING;
protected String _FIPS = DMIUtil.MISSING_STRING;
protected Date _LastUpdate = DMIUtil.MISSING_DATE;

/**
DIADvisor_SiteDef constructor.
*/
public DIADvisor_SiteDef ()
{	super();
}

/**
Cleans up variables when the class is disposed of.  Sets all the member
variables (that aren't primitives) to null.
*/
protected void finalize() throws Throwable {
	_SiteName = null;
	_RepeaterGroup = null;
	_SitePicture = null;
	_Zone = null;
	_FIPS = null;
	_LastUpdate = null;
	super.finalize();
}

/**
Returns _Elevation
@return _Elevation
*/
public double getElevation()
{	return _Elevation;
}

/**
Returns _FIPS
@return _FIPS
*/
public String getFIPS()
{	return _FIPS;
}

/**
Returns _LastUpdate
@return _LastUpdate
*/
public Date getLastUpdate()
{	return _LastUpdate;
}

/**
Returns _Latitude
@return _Latitude
*/
public double getLatitude()
{	return _Latitude;
}

/**
Returns _Longitude
@return _Longitude
*/
public double getLongitude()
{	return _Longitude;
}

/**
Return _PKey
@return _PKey
*/
public long getPKey()
{	return _PKey;
}

/**
Return _RepeaterGroup
@return _RepeaterGroup
*/
public String getRepeaterGroup()
{	return _RepeaterGroup;
}

/**
Return _SiteID
@return _SiteID
*/
public int getSiteID()
{	return _SiteID;
}

/**
Return _SiteName
@return _SiteName
*/
public String getSiteName()
{	return _SiteName;
}

/**
Return _SitePicture
@return _SitePicture
*/
public String getSitePicture()
{	return _SitePicture;
}

/**
Returns _XCoord
@return _XCoord
*/
public double getXCoord()
{	return _XCoord;
}

/**
Returns _YCoord
@return _YCoord
*/
public double getYCoord()
{	return _YCoord;
}

/**
Return _Zone
@return _Zone
*/
public String getZone()
{	return _Zone;
}

/**
Set _Elevation.
@param Elevation value to assign to _Elevation.
*/
public void setElevation ( double Elevation )
{	_Elevation = Elevation;
}

/**
Set _FIPS
@param FIPS value to assign to _FIPS.
*/
public void setFIPS ( String FIPS )
{	if ( FIPS != null ) {
		_FIPS = FIPS;
	}
}

/**
Set _LastUpdate
@param LastUpdate value to assign to _LastUpdate.
*/
public void setLastUpdate ( Date LastUpdate )
{	_LastUpdate = LastUpdate;
}

/**
Set _Latitude.
@param Latitude value to assign to _Latitude.
*/
public void setLatitude ( double Latitude )
{	_Latitude = Latitude;
}

/**
Set _Longitude.
@param Longitude value to assign to _Longitude.
*/
public void setLongitude ( double Longitude )
{	_Longitude = Longitude;
}

/**
Set _PKey.
@param PKey value to assign to _PKey.
*/
public void setPKey ( long PKey )
{	_PKey = PKey;
}

/**
Set _RepeaterGroup
@param RepeaterGroup value to assign to _RepeaterGroup.
*/
public void setRepeaterGroup ( String RepeaterGroup )
{	if ( RepeaterGroup != null ) {
		_RepeaterGroup = RepeaterGroup;
	}
}

/**
Set _SiteID.
@param SiteID value to assign to _SiteID.
*/
public void setSiteID ( int SiteID )
{	_SiteID = SiteID;
}

/**
Set _SiteName
@param SiteName value to assign to _SiteName.
*/
public void setSiteName ( String SiteName )
{	if ( SiteName != null ) {
		_SiteName = SiteName;
	}
}

/**
Set _SitePicture
@param SitePicture value to assign to _SitePicture.
*/
public void setSitePicture ( String SitePicture )
{	if ( SitePicture != null ) {
		_SitePicture = SitePicture;
	}
}

/**
Set _XCoord.
@param XCoord value to assign to _XCoord.
*/
public void setXCoord ( double XCoord )
{	_XCoord = XCoord;
}

/**
Set _YCoord.
@param YCoord value to assign to _YCoord.
*/
public void setYCoord ( double YCoord )
{	_YCoord = YCoord;
}

/**
Set _Zone
@param Zone value to assign to _Zone.
*/
public void setZone ( String Zone )
{	if ( Zone != null ) {
		_Zone = Zone;
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

} // End DIADvisor_SiteDef
