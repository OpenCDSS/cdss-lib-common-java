// DateTimeRange - this class stores a range defined by two DateTime end-points.

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

package RTi.Util.Time;

/**
This class stores a range defined by two DateTime end-points.
It is useful for specifying a processing period.
It is NOT the same as DateTimeWindow, which is a window within each year of a period.
Currently the instance is immutable and copies of the DateTime data are copied at construction.
Null date/times are allowed.  Currently there is no validation done.
*/
public class DateTimeRange
{

    /**
	Starting DateTime for the range.
	*/
	private DateTime start = null;
	
	/**
	Ending DateTime for the range.
	*/
	private DateTime end = null;
	
	/**
	Constructor.
	@param start starting date/time in the range.  Can be null to indicate open-ended range (from available start).
	@param end ending date/time in the range.  Can be null to indicate open-ended range (to available end).
	*/
	public DateTimeRange ( DateTime start, DateTime end )
	{
	    if ( start == null ) {
	        this.start = null;
	    }
	    else {
	        this.start = new DateTime ( start );
	    }
	    if ( end == null ) {
	        this.end = null;
	    }
	    else {
	        this.end = new DateTime ( end );
	    }
	}
	
	/**
	Return the ending date/time in the range (can be null) if open-ended.
	@return the ending date/time in the range (can be null) if open-ended.
	*/
	public DateTime getEnd () {
	    return this.end;
	}
	
	/**
	Return the starting date/time in the range (can be null) if open-ended.
	@return the starting date/time in the range (can be null) if open-ended.
	*/
	public DateTime getStart () {
	    return this.start;
	}

}
