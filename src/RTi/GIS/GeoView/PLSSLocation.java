// PLSSLocation - class for storing PLSS (Public Land Survey System) location data.

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

package RTi.GIS.GeoView;

import java.util.List;

import RTi.Util.String.StringUtil;

/**
This class represents a PLSS (public land survey system) location.  Currently baselines are not fully supported.
*/
public class PLSSLocation {

/**
The value to use when unsetting a range, section, or township value in a location.
*/
public final static int UNSET = -999;

/**
The location baseline from which grids are numbered.  Runs East-West.
*/
private int __baseline = UNSET;

/**
The half section.
*/
private String __halfSection = null;

/**
The principal meridian from which grids are numbered.  Runs North-South.
*/
private String __pm = null;

/**
The 160-acre 1/4 section.
*/
private String __q = null;

/**
The 40-acre 1/4 1/4 section.
*/
private String __qq = null;

/**
The 10-acre 1/4 1/4 1/4 section.
*/
private String __qqq = null;

/**
The grid location E or W of the principal meridian.
*/
private int __range = UNSET;

/**
The direction of the grid location.  Must be either EAST or WEST.
*/
private String __rangeDirection = null;

/**
The section of the grid location found by the range and township.
The grid location is divided into 36 squares, each of which is a section.
*/
private int __section = UNSET;

/**
The grid location N or S of the baseline.
*/
private int __township = UNSET;

/**
The direction of the grid location.  Must be either NORTH or SOUTH.
*/
private String __townshipDirection = null;

/**
Constructor.
*/
public PLSSLocation() {
}

/**
Constructor.  Parses the specified location string (see toString(false)) and fills the values from the string.
@param locationString the concise location string to parse.
@throws Exception if an error occurs.
*/
public PLSSLocation(String locationString)
throws Exception {
	this(locationString, false);
}

/**
Constructor.  Parses the specified location string and fills the values from the string.
@param locationString the concise location string to parse.
@param cdssFormat whether the location String is in CDSS format (true) or not.
@throws Exception if an error occurs.
*/
public PLSSLocation(String locationString, boolean cdssFormat)
throws Exception {
	PLSSLocation location = PLSSLocation.parse(locationString, cdssFormat);

	setPM(location.getPM());
	setBaseline(location.getBaseline());
	setTownship(location.getTownship());
	setTownshipDirection(location.getTownshipDirection());
	setRange(location.getRange());
	setRangeDirection(location.getRangeDirection());
	setSection(location.getSection());
	setHalfSection(location.getHalfSection());
	setQ(location.getQ());
	setQQ(location.getQQ());
	setQQQ(location.getQQQ());
}

/**
Returns the baseline used for the plss location.
@return the baseline used for the plss location.
*/
public int getBaseline() {
	return __baseline;
}

/**
Returns the half section.
@return the half section.
*/
public String getHalfSection() {
	return __halfSection;
}

/**
Returns the principal meridian used for the plss location.
@return the principal meridian used for the plss location.
*/
public String getPM() {
	return __pm;
}

/**
Returns the 160-acre quarter of the section.
@return the 160-acre quarter of the section.
*/
public String getQ() {
	return __q;
}

/**
Returns the 40-acre quarter of the section.
@return the 40-acre quarter of the section.
*/
public String getQQ() {
	return __qq;
}

/**
Returns the 10-acre quarter of the section.
@return the 10-acre quarter of the section.
*/
public String getQQQ() {
	return __qqq;
}

/**
Returns the range value, relative to the principal meridian.
@return the range value, relative to the principal meridian.
*/
public int getRange() {
	return __range;
}

/**
Returns the range direction.
@return the range direction.
*/
public String getRangeDirection() {
	return __rangeDirection;
}

/**
Returns the section.
@return the section.
*/
public int getSection() {
	return __section;
}

/**
Returns the township, relative to the baseline.
@return the township, relative to the baseline.
*/
public int getTownship() {	
	return __township;
}

/**
Returns the township direction.
@return the township direction.
*/
public String getTownshipDirection() {
	return __townshipDirection;
}

/**
Parses a representation (see toString(false)) of a plss location and
returns a PLSSLocation object with the location information filled in.
@param locationString a concise representation of a plss location.
@return a PLSSLocation object with the location information filled in.
@throws Exception if there is an eror parsing the location string or if there
is a problem filling data in the location object.
*/
public static PLSSLocation parse(String locationString) 
throws Exception {
	return parse(locationString, false);
}

/**
Parses a representation (see toString()) of a plss location and
returns a PLSSLocation object with the location information filled in.
@param locationString a concise representation of a plss location.
@param cdssFormat whether the location representation is in CDSS Format.
@return a PLSSLocation object with the location information filled in.
@throws Exception if there is an error parsing the location string or if there
is a problem filling data in the location object.
*/
public static PLSSLocation parse(String locationString, boolean cdssFormat) 
throws Exception {
	List<String> v = StringUtil.breakStringList(locationString, ",", 0);

	if (cdssFormat) {
		return parseCDSSLocation(locationString);
	}

	// REVISIT (JTS - 2005-01-12)
	// written but not yet tested!

	int index = -1;
	PLSSLocation location = new PLSSLocation();
	String s = null;
	String key = null;
	String value = null;
	String temp = null;

	for (int i = 0; i < v.size(); i++) {
		s = ((String)v.get(i)).trim();
		index = s.indexOf("=");

		if (index == -1) {
			throw new Exception("Invalid location string ('"
				+ locationString + "').  "
				+ "One of the comma-delimited parameters does "
				+ "not have an equals sign.");
		}

		key = s.substring(0, index);
		
		index = s.indexOf("\"");
	
		if (index == -1) {
			throw new Exception("Invalid location string.  ('"
				+ locationString + "').  "
				+ key + " does not have values in quotation "
				+ "marks.");
		}
		
		value = s.substring(index + 1, s.length() - 1);
		
		if (key.equalsIgnoreCase("PM")) {
			location.setPM(value);
		}
		else if (key.equalsIgnoreCase("TS")) {
			index = value.indexOf(" ");
			if (index == -1) {
				throw new Exception("Invalid TS value "
					+ "(no direction).");
			}
			temp = value.substring(0, index);
			location.setTownship(StringUtil.atoi(temp));
			temp = value.substring(index).trim();
			location.setTownshipDirection(temp);
		}
		else if (key.equalsIgnoreCase("Range")) {
			index = value.indexOf(" ");
			if (index == -1) {
				throw new Exception("Invalid Range value "
					+ "(no direction).");
			}
			temp = value.substring(0, index);
			location.setRange(StringUtil.atoi(temp));
			temp = value.substring(index).trim();
			location.setRangeDirection(temp);
		}
		else if (key.equalsIgnoreCase("Section")) {
			index = value.indexOf(" ");
			if (index == -1) {
				throw new Exception("Invalid section value "
					+ "(no half section).");
			}
			temp = value.substring(0, index);
			location.setSection(StringUtil.atoi(temp));
			temp = value.substring(index).trim();
			location.setHalfSection(temp);
		}
		else if (key.equalsIgnoreCase("Q")) {
			location.setQ(value);
		}
		else if (key.equalsIgnoreCase("QQ")) {
			location.setQQ(value);
		}
		else if (key.equalsIgnoreCase("QQQ")) {
			location.setQQQ(value);
		}
		else {
			throw new Exception("Unknown key: " + key);
		}
	}

	return location;
}

/**
Parses a representation (see toString(true)) of a plss location and
returns a PLSSLocation object with the location information filled in.
@param locationString a concise representation of a plss location.
@throws Exception if there is an eror parsing the location string or if there
is a problem filling data in the location object.
*/
public static PLSSLocation parseCDSSLocation(String locationString) 
throws Exception {
	List<String> v = StringUtil.breakStringList(locationString, ",", 0);

	if (v.size() == 7) {
		return parseOldCDSSLocation(locationString);
	}
	else if (v.size() == 12) {
		// continue on	
	}
	else {
		throw new Exception("Improperly-formed CDSS PLSS location "
			+ "string: " + locationString);
	}

	String s = null;

	PLSSLocation location = new PLSSLocation();

	s = ((String)v.get(0)).trim();
	if (s.equals("*")) {
		location.setPM(null);
	}
	else {
		location.setPM(s);
	}

	s = ((String)v.get(1)).trim();
	if (s.equals("*")) {
		location.setTownship(UNSET);
	}
	else {
		location.setTownship(StringUtil.atoi(s));
	}

	// half-township (2) is ignored

	s = ((String)v.get(3)).trim();
	if (s.equals("*")) {
		location.setTownshipDirection(null);
	}
	else {
		location.setTownshipDirection(s);
	}

	s = ((String)v.get(4)).trim();
	if (s.equals("*")) {
		location.setRange(UNSET);
	}
	else {
		location.setRange(StringUtil.atoi(s));
	}

	// half-range (5) is ignored

	s = ((String)v.get(6)).trim();
	if (s.equals("*")) {
		location.setRangeDirection(null);
	}
	else {
		location.setRangeDirection(s);
	}

	s = ((String)v.get(7)).trim();
	if (s.equals("*")) {
		location.setSection(UNSET);
	}
	else {
		location.setSection(StringUtil.atoi(s));
	}

	s = ((String)v.get(8)).trim();
	if (s.equals("*")) {
		location.setHalfSection(null);
	}
	else {
		location.setHalfSection(s);
	}

	s = ((String)v.get(9)).trim();
	if (s.equals("*")) {
		location.setQ(null);
	}
	else {
		location.setQ(s);
	}
	
	s = ((String)v.get(10)).trim();
	if (s.equals("*")) {
		location.setQQ(null);
	}
	else {
		location.setQQ(s);
	}

	s = ((String)v.get(11)).trim();
	if (s.equals("*")) {
		location.setQQQ(null);
	}
	else {
		location.setQQQ(s);
	}

	return location;
}

/**
Parses a representation of a plss location and
returns a PLSSLocation object with the location information filled in.  This
parses the old CDSS Location, in which directions were combined with their
areas.  For instance, "10 W" could be a range.  In the new CDSS location
Strings, every piece is in a separate comma-delimited part of the String.
@param locationString a concise representation of a plss location.
@return a PLSSLocation object with the location information filled in.
@throws Exception if there is an eror parsing the location string or if there
is a problem filling data in the location object.
*/
public static PLSSLocation parseOldCDSSLocation(String locationString) 
throws Exception {
	List<String> v = StringUtil.breakStringList(locationString, ",", 0);
	String s = null;

	PLSSLocation location = new PLSSLocation();

	s = ((String)v.get(0)).trim();
	if (s.equals("*")) {
		location.setPM(null);
	}
	else {
		location.setPM(s);
	}

	s = ((String)v.get(1)).trim();
	int index = -1;
	if (s.equals("*")) {
		location.setTownship(UNSET);
		location.setTownshipDirection(null);
	}
	else {
		index = s.indexOf(" ");
		if (index > -1) {
			location.setTownship(
				StringUtil.atoi(s.substring(0, index)));
			s = s.substring(index + 1).trim();
			if (s.equals("*")) {
				location.setTownshipDirection(null);
			}
			else {
				location.setTownshipDirection(s);
			}
		}
		else {
			location.setTownship(UNSET);
			location.setTownshipDirection(s);
		}
	}

	s = ((String)v.get(2)).trim();
	if (s.equals("*")) {
		location.setRange(UNSET);
		location.setRangeDirection(null);
	}
	else {
		index = s.indexOf(" ");
		if (index > -1) {
			location.setRange(
				StringUtil.atoi(s.substring(0, index)));
			s = s.substring(index + 1).trim();
			if (s.equals("*")) {
				location.setRangeDirection(null);
			}
			else {
				location.setRangeDirection(s);
			}
		}
		else {
			location.setRange(UNSET);
			location.setRangeDirection(s);
		}
	}

	s = ((String)v.get(3)).trim();
	if (s.equals("*")) {
		location.setSection(UNSET);
		location.setHalfSection(null);
	}
	else {
		index = s.indexOf(" ");
		if (index > -1) {
			location.setSection(
				StringUtil.atoi(s.substring(0, index)));
			s = s.substring(index + 1).trim();
			if (s.equals("*")) {
				location.setHalfSection(null);
			}
			else {
				location.setHalfSection(s);
			}			
		}
		else {
			location.setSection(UNSET);
			location.setHalfSection(s);
		}
	}

	s = ((String)v.get(4)).trim();
	if (s.equals("*")) {
		location.setQ(null);
	}
	else {
		location.setQ(s);
	}
	
	s = ((String)v.get(5)).trim();
	if (s.equals("*")) {
		location.setQQ(null);
	}
	else {
		location.setQQ(s);
	}

	s = ((String)v.get(6)).trim();
	if (s.equals("*")) {
		location.setQQQ(null);
	}
	else {
		location.setQQQ(s);
	}

	return location;	
}

/**
Sets the baseline.
@param baseline the baseline to set.
@throws Exception if there is an error setting the baseline.
*/
public void setBaseline(int baseline) 
throws Exception {
	// REVISIT (JTS - 2005-01-12)
	// what are valid baselines?
	__baseline = baseline;
}

/**
Sets the half section.
@param halfSection the half section to set.
*/
public void setHalfSection(String halfSection) {
	__halfSection = halfSection;
}

/**
Sets the principal meridian.
@param pm the principal meridian to set.  Must be one of B, C, N, S, or U.
*/
public void setPM(String pm) {
	__pm = pm;
}

/**
Sets the 160-acre section quarter.
@param q the quarter.  Must be one of NORTHWEST, NORTHEAST, SOUTHWEST, 
or SOUTHEAST.
*/
public void setQ(String q) {
	__q = q;
}

/**
Sets the 40-acre section quarter.
@param qq the quarter.  Must be one of NORTHWEST, NORTHEAST, SOUTHWEST, 
or SOUTHEAST.
*/
public void setQQ(String qq) {
	__qq = qq;
}

/**
Sets the the 10-acre section quarter.
@param qqq the quarter.  Must be one of NORTHWEST, NORTHEAST, SOUTHWEST, 
or SOUTHEAST.
*/
public void setQQQ(String qqq) {
	__qqq = qqq;
}

/**
Sets the range.
@param range the range.  Must be greater than 0.
@throws Exception if an invalid range is set.
*/
public void setRange(int range) 
throws Exception {
	if (range < 1 && range != UNSET) {
		throw new Exception("Range must be greater than 0");
	}

	__range = range;
}

/**
Sets the range direction.
@param direction the range direction.  Must be either WEST or EAST.
*/
public void setRangeDirection(String direction) {
	__rangeDirection = direction;
}

/**
Sets the section number.
@param section the section number to set.  Must be &gt;= 1 and &lt;= 36.
@throws Exception if an invalid section is set.
*/
public void setSection(int section) 
throws Exception {
	if (section < 1 && section != UNSET) {
		throw new Exception("Section must be greater than 0");
	}

	if (section > 36) {
		throw new Exception("Section must be less than 37");
	}

	__section = section;
}

/**
Sets the township.
@param township the township to set.  Must be greater than 0.
@throws Exception if an invalid township is set.
*/
public void setTownship(int township) 
throws Exception {
	if (township < 1 && township != UNSET) {
		throw new Exception("Township must be greater than 0");
	}

	__township = township;
}

/**
Sets the township direction.
@param direction the township direction to set.  Must be either NORTH or SOUTH.
*/
public void setTownshipDirection(String direction) {
	__townshipDirection = direction; 
}

/**
Returns a representation of this location as a String.
@return a representation of this location as a String.
*/
public String toString() {
	return toString(false);
}

/**
Returns this location as a String.  If any of the data values are unset 
(null Strings or ints == UNSET) then they are not included in the output.
@param cdssFormat whether to display the location in CDSS format 
("*, *, *, *, *, *, *") or in normal PLSS Location.
*/
public String toString(boolean cdssFormat) {
	return toString(cdssFormat, true);
}

/**
Returns this location as a String.  If any of the data values are unset 
(null Strings or ints == UNSET) then they are not included in the output.
@param cdssFormat whether to display the location in CDSS format 
("*, *, *, *, *, *, *") or in normal PLSS Location.
@param odlFormat whether to display the location in the old CDSS format,
which combined areas and their directions (for instance "... TS TDIR, ...")
or in the new one, in which all values are in a separate comma-delimited 
area.
*/
public String toString(boolean cdssFormat, boolean oldFormat) {
	String s = "";
// REVISIT (JTS - 2005-01-12)
// baseline is not yet supported fully
	
	int count = 0;
	if (cdssFormat) {
		if (oldFormat) {
			if (getPM() == null) {
				s += "*, ";
			}
			else {
				s += getPM() + ", ";
			}

			if (getTownship() == UNSET 
			    && getTownshipDirection() == null) {	
				s += "*, ";
			}
			else if (getTownship() != UNSET 
			    && getTownshipDirection() == null) {
			    	s += getTownship() + " *, ";
			}
			else if (getTownship() == UNSET 
			    && getTownshipDirection() != null) {
			    	s += getTownshipDirection() + ", ";
			}
			else {
				s += getTownship() + " " 
					+ getTownshipDirection() + ", ";
			}
				
			if (getRange() == UNSET 
			    && getRangeDirection() == null) {	
				s += "*, ";
			}
			else if (getRange() != UNSET 
			    && getRangeDirection() == null) {
			    	s += getRange() + " *, ";
			}
			else if (getRange() == UNSET 
			    && getRangeDirection() != null) {
			    	s += getRangeDirection() + ", ";
			}
			else {
				s += getRange() + " " + getRangeDirection() 
					+ ", ";
			}
	
			if (getSection() == UNSET && getHalfSection() == null) {
				s += "*, ";
			}
			else if (getSection() != UNSET 
			    && getHalfSection() == null) {
			    	s += getSection() + " *, ";
			}
			else if (getSection() == UNSET 
			    && getHalfSection() != null) {
			    	s += getHalfSection() + ", ";
			}
			else {
				s += getSection() + " " + getHalfSection() 
					+ ", ";
			}
	
			if (getQ() == null) {
				s += "*, ";
			}
			else {
				s += getQ() + ", ";
			}
	
			if (getQQ() == null) {
				s += "*, ";
			}
			else {
				s += getQQ() + ", ";
			}
	
			if (getQQQ() == null) {
				s += "*";
			}
			else {
				s += getQQQ();
			}
		}
		else {
			if (getPM() == null) {
				s += "*,";
			}
			else {
				s += getPM() + ",";
			}
	
			if (getTownship() == UNSET) {
				s += "*,";
			}
			else {
				s += "" + getTownship() + ",";
			}

			// half-township is not supported now
			s += "*,";
	
			if (getTownshipDirection() == null) {
				s += "*,";
			}
			else {
				s += "" + getTownshipDirection() + ",";
			}
	
			if (getRange() == UNSET) {
				s += "*,";
			}
			else {
				s += "" + getRange() + ",";
			}
	
			// half-range is not supported now
			s += "*,";
	
			if (getRangeDirection() == null) {
				s += "*,";
			}
			else {
				s += "" + getRangeDirection() + ",";
			}
	
			if (getSection() == UNSET) {
				s += "*,";
			}
			else {
				s += "" + getSection() + ",";
			}
	
			if (getHalfSection() == null) {
				s += "*,";
			}
			else {
				s += "" + getHalfSection() + ",";
			}
	
			if (getQ() == null) {
				s += "*,";
			}
			else {
				s += getQ() + ",";
			}
	
			if (getQQ() == null) {
				s += "*,";
			}
			else {
				s += getQQ() + ",";
			}
	
			if (getQQQ() == null) {
				s += "*";
			}
			else {
				s += getQQQ();
			}
		}
	}
	else {
		if (__pm != null) {
			s += "PM=\"" + __pm + "\"";
			count++;
		}

		if (__township != UNSET && __townshipDirection != null) {
			if (count > 0) {
				s += ", ";
			}
			s += "TS=\"" + __township + " " + __townshipDirection 
				+ "\"";
			count++;
		}
	
		if (__range != UNSET && __rangeDirection != null) {
			if (count > 0) {
				s += ", ";
			}
			s += "Range=\"" + __range + " " + __rangeDirection 
				+ "\"";
			count++;
		}

		if (__section != UNSET) {
			if (count > 0) {
				s += ", ";
			}
			s += "Section=\"" + __section + " " + __halfSection
				+ "\"";
			count++;
		}

		if (__q != null) {
			if (count > 0) {
				s += ", ";
			}
			s += "Q=\"" + __q + "\"";
			count++;
		}

		if (__qq != null) {
			if (count > 0) {
				s += ", ";
			}
			s += "QQ=\"" + __qq + "\"";
			count++;
		}

		if (__qqq != null) {
			if (count > 0) {
				s += ", ";
			}
			s += "QQQ=\"" + __qqq + "\"";
		}
	}
	return s;
}

// REVISIT (JTS - 2006-05-22)
// This may not be necessary anymore.
public static boolean unset(String val) {	
	if (val == null) {
		return true;
	}
	return false;
}

// REVISIT (JTS - 2006-05-22)
// This may not be necessary anymore.
public static boolean unset(int val) {
	if (val == UNSET) {	
		return true;
	}
	return false;
}

}
