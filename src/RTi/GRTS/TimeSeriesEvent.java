// TimeSeriesEvent - manage and provide time series annotation event data, for annotating time series graphs with events

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

package RTi.GRTS;

import RTi.TS.TS;
import RTi.Util.Time.TimeLocationEvent;

/**
Manage and provide time series annotation event data, for annotating time series graphs with events.
*/
public class TimeSeriesEvent
{

/**
Time series that is associated with the events.
*/
private TS ts = null;

/**
List of time series events.
*/
private TimeLocationEvent event = null;

/**
Constructor.
*/
public TimeSeriesEvent ( TS ts, TimeLocationEvent event )
{
    this.ts = ts;
    this.event = event;
}

/**
Return the time series for the event.
*/
public TS getTimeSeries ()
{
    return this.ts;
}

/**
Return the event for the time series event.
*/
public TimeLocationEvent getEvent ()
{
    return this.event;
}

}
