package RTi.Util.Time;

/**
Representation of a single time or timespan event that is associated with a single location.
For example, this information is used to indicate when and where an event occurred so that it can
be visualized on time series graphs and time-aware maps.  Event start and end are represented as follows:
<pre>
eventStart             eventEnd                Interpretation
-------------------    --------------------    -----------------------------------------------------------------------------
(1) DateTime           DateTimeIndefinite      Event has a start time but no end time (event is ongoing).
(2) DateTime           null                    Event is a point event (interpret start and end time to be the same).
* For now case 1 above is not enabled and case 2 is used instead to represent 1.  Case to requires 2 date/times. 
* If this works, then case 1 will never be enabled.
DateTime               DateTime                Event has a discrete start and end.
DateTimeIndefinite     DateTime                Event has an indefinite start time (started long ago) but a definite end time.
</pre>
*/
public class TimeLocationEvent
{

/**
Event identifier.
*/
private String eventID = "";

/**
Event type (e.g., "Drought").
*/
private String eventType = "";

/**
Start date/time for the event.
*/
private DateTime eventStart;

/**
End date/time for the event (can be null if the event is a single-point event, or ongoing indefinitely).
*/
private DateTime eventEnd;

/**
Location type for event (e.g., "County").
*/
private String locationType = "";

/**
Location ID for event (e.g., "Adams" county).
*/
private String locationID = "";

/**
Short label to use for the event, suitable for graph or map rendered labels.
*/
String label = "";

/**
Full descriptive note for the event, suitable for a popup or report summarizing the events.
*/
String description = "";

/**
Constructor.
@param eventID identifier for event
@param eventType type of event, a string appropriate for the event data set
@param eventStart start of the event, or null if start was indeterminate
@param eventEnd end of the event, or null if ongoing (should be same as eventStart if a point in time event)
@param label label for the event, a short string appropriate for rendering on graphs and maps
@param description longer description of the event, such as a narrative for a pop-up or report
@param locations a dictionary that includes the event location type (e.g., "County"),
and location identifier (e.g., "Adams" county).
*/
public TimeLocationEvent ( String eventID, String eventType, DateTime eventStart, DateTime eventEnd,
    String locationType, String locationID, String label, String description )
{
    this.eventID = eventID;
    this.eventType = eventType;
    this.eventStart = eventStart;
    this.eventEnd = eventEnd;
    this.label = label;
    this.description = description;
    this.locationType = locationType;
    this.locationID = locationID;
}

/**
Return the longer description for the event.
*/
public String getDescription ()
{
    return this.description;
}

/**
Return the event end, null if the event is ongoing.  If not null, the event has a discrete end.
*/
public DateTime getEventEnd ()
{
    return this.eventEnd;
}

/**
Return the event identifier.
*/
public String getEventID ()
{
    return this.eventID;
}

/**
Return the event start.
*/
public DateTime getEventStart ()
{
    return this.eventStart;
}

/**
Return the event type.
*/
public String getEventType ()
{
    return this.eventType;
}

/**
Return the short label to be used for rendering the data.
*/
public String getLabel ()
{
    return this.label;
}

/**
Return the location ID for the event.
*/
public String getLocationID ()
{
    return this.locationID;
}

/**
Return the location type for the event.
*/
public String getLocationType ()
{
    return this.locationType;
}

}