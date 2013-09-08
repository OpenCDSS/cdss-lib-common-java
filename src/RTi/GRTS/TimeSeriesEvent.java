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