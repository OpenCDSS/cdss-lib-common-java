// TimeSeriesEventAnnotationCreator - Create annotation event data using input table and time series

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2022 Colorado Department of Natural Resources

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import RTi.TS.TS;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeLocationEvent;

/**
Create annotation event data using input table and time series.
*/
public class TimeSeriesEventAnnotationCreator
{

/**
Table that contains event data. 
*/
DataTable eventTable = null;

/**
Time series to be matched with events.
*/
TS ts = null;

/**
Constructor.
@param eventTable the data table containing events
@param ts time series to associate events
*/
public TimeSeriesEventAnnotationCreator ( DataTable eventTable, TS ts ) {
    this.eventTable = eventTable;
    this.ts = ts;
}

/**
Create a list of annotation events from the table and time series that were used to initialize the instance.
@param eventTypes event types (e.g., "Drought") to include
@param eventIDColumn event table column name for event ID
@param eventStartColumn event table column name for event start date/time
@param eventEndColumn event table column name for event end date/time
@param eventLabelColumn event table column name for event label
@param eventDescriptionColumn event table column name for event description
@param eventLocationColumnMap dictionary of event table column names for location type and location value.
If the key and value are the same, then the key indicates the location type as a column name, and the value is in that column.
If the key and value are different, then the key indicates the column name for location types
(e.g., "LocationType" column with values "County", "State", etc.),
and the value is the column name for location type values
(e.g., "Adams" for location type "County" and "CO" for location type "State".
@param tsLocationMap dictionary of time series location type and time series properties (e.g., "County", "${TS:County}").
@param start the starting DateTime for events, used to request a time window of events (null to process all events)
@param end the ending DateTime for events, used to request a time window of events (null to process all events)
*/
public List<TimeSeriesEvent> createTimeSeriesEvents ( List<String> eventTypes,
    String eventIDColumn, String eventTypeColumn, String eventStartColumn, String eventEndColumn,
    String eventLabelColumn, String eventDescriptionColumn,
    HashMap<String,String> eventLocationColumnMap, HashMap<String,String> tsLocationMap,
    DateTime start, DateTime end )
{   // Get the primary data.
    DataTable table = getEventTable();
    TS ts = getTimeSeries();
    // If event types are not specified, will return all.
    if ( eventTypes == null ) {
        eventTypes = new ArrayList<>();
    }
    // Determine the column numbers for the table data.
    int eventIDColumnNum = -1;
    try {
        eventIDColumnNum = table.getFieldIndex(eventIDColumn);
    }
    catch ( Exception e ) {
        throw new RuntimeException ( "Event table \"" + table.getTableID() + " event ID column \"" + eventIDColumn +
            "\" not found in table." );
    }
    int eventTypeColumnNum = -1;
    try {
        eventTypeColumnNum = table.getFieldIndex(eventTypeColumn);
    }
    catch ( Exception e ) {
        throw new RuntimeException ( "Event table \"" + table.getTableID() + " event type column \"" + eventTypeColumn +
            "\" not found in table." );
    }
    int eventStartColumnNum = -1;
    try {
        eventStartColumnNum = table.getFieldIndex(eventStartColumn);
    }
    catch ( Exception e ) {
        throw new RuntimeException ( "Event table \"" + table.getTableID() + " event start column \"" + eventStartColumn +
            "\" not found in table." );
    }
    int eventEndColumnNum = -1;
    try {
        eventEndColumnNum = table.getFieldIndex(eventEndColumn);
    }
    catch ( Exception e ) {
        throw new RuntimeException ( "Event table \"" + table.getTableID() + " event end column \"" + eventEndColumn +
            "\" not found in table." );
    }
    int eventLabelColumnNum = -1;
    try {
        eventLabelColumnNum = table.getFieldIndex(eventLabelColumn);
    }
    catch ( Exception e ) {
        throw new RuntimeException ( "Event table \"" + table.getTableID() + " label column \"" + eventLabelColumn +
            "\" not found in table." );
    }
    int eventDescriptionColumnNum = -1;
    try {
        eventDescriptionColumnNum = table.getFieldIndex(eventDescriptionColumn);
    }
    catch ( Exception e ) {
        throw new RuntimeException ( "Event table \"" + table.getTableID() + " description column \"" + eventDescriptionColumn +
            "\" not found in table." );
    }
    String [] eventLocationTypes = new String[eventLocationColumnMap.size()];
    int [] eventLocationColumnNum = new int[eventLocationColumnMap.size()];
    String [] eventLocationColumns = new String[eventLocationColumnNum.length];
    for ( int i = 0; i < eventLocationTypes.length; i++ ) {
        eventLocationColumnNum[i] = -1;
    }
    int ikey = -1;
    for ( Map.Entry<String,String> pairs: eventLocationColumnMap.entrySet() ) {
        eventLocationTypes[++ikey] = pairs.getKey();
        try {
            eventLocationColumns[ikey] = pairs.getValue();
            eventLocationColumnNum[ikey] = table.getFieldIndex(eventLocationColumns[ikey]);
        }
        catch ( Exception e ) {
            throw new RuntimeException ( "Event table \"" + table.getTableID() + "\" location column \"" +
                eventLocationColumns[ikey] + "\" not found in table." );
        }
    }
    // Determine the location types and ID values from the time series.
    ikey = -1;
    String [] tsLocationTypes = new String[tsLocationMap.size()];
    String [] tsLocationIDs = new String[tsLocationTypes.length];
    for ( Map.Entry<String,String> pairs: tsLocationMap.entrySet() ) {
        tsLocationTypes[++ikey] = pairs.getKey();
        // Expand the ID based on the time series properties.
        tsLocationIDs[ikey] = pairs.getValue();
    }
    // Loop through the table records and try to match the location types in the event records with the location types
    // in the time series.
    String eventID, eventType, eventLocationType = null, eventLocationID = null, label = null, description = null;
    Object eventStartO = null, eventEndO = null;
    DateTime eventStart = null, eventEnd = null;
    boolean includeEvent;
    List<TimeSeriesEvent> tsEventList = new ArrayList<>();
    for ( TableRecord rec : table.getTableRecords() ) {
        // Skip records that are not the correct event type.
        try {
            eventType = rec.getFieldValueString(eventTypeColumnNum);
        }
        catch ( Exception e ) {
            // Should not happen since valid index checked above.
            continue;
        }
        includeEvent = false;
        if ( eventTypes.size() == 0 ) {
            includeEvent = true;
        }
        else {
            for ( String eventTypeReq: eventTypes ) {
                if ( eventType.equalsIgnoreCase(eventTypeReq) ) {
                    includeEvent = true;
                    break;
                }
            }
        }
        if ( !includeEvent ) {
            continue;
        }
        // Reset because need to check for location type match.
        includeEvent = false;
        // Loop through the location data for the record.
        for ( int iloc = 0; iloc < eventLocationColumnNum.length; iloc++ ) {
            try {
                eventLocationType = eventLocationTypes[iloc];
            }
            catch ( Exception e ) {
                // Should not happen since column verified above.
            }
            try {
                eventLocationID = rec.getFieldValueString(eventLocationColumnNum[iloc]);
            }
            catch ( Exception e ) {
                // Should not happen since column verified above.
            }
            // Loop through the location information for the time series and see if the table record matches.
            for ( int itsloc = 0; itsloc < tsLocationTypes.length; itsloc++ ) {
                //Message.printStatus(2,"","Comparing event record location type \"" + eventLocationType +
                //    "\" with time series location type \"" + tsLocationTypes[itsloc] +
                //    "\" and event location ID \"" + eventLocationID + "\" with time series location ID \"" + tsLocationIDs[itsloc] +
                //    "\"" );
                if ( (eventLocationType != null) && eventLocationType.equalsIgnoreCase(tsLocationTypes[itsloc]) &&
                    (eventLocationID != null) && eventLocationID.equalsIgnoreCase(tsLocationIDs[itsloc]) ) {
                    // Event location type and location ID match the time series.
                    includeEvent = true;
                    //Message.printStatus(2,"","Found matching event record location type \"" + eventLocationType +
                    //"\" with time series location type \"" + tsLocationTypes[itsloc] +
                    //"\" and event location ID \"" + eventLocationID + "\" with time series location ID \"" + tsLocationIDs[itsloc] +
                    //"\"" );
                    break;
                }
            }
            if ( includeEvent ) {
                break;
            }
        }
        if ( !includeEvent ) {
            // Location did not match.
            continue;
        }
        // If here the event type was matched and the event location match the time series location.
        // Get the remaining data from the table and create a corresponding event.
        try {
            eventStartO = rec.getFieldValue(eventStartColumnNum);
            if ( eventStartO == null ) {
                eventStart = null;
            }
            else {
                if ( eventStartO instanceof DateTime ) {
                    // Just set.
                    eventStart = (DateTime)eventStartO;
                }
                else if ( eventStartO instanceof Date ) {
                    eventStart = new DateTime((Date)eventStartO);
                }
                else if ( eventStartO instanceof String ) {
                    eventStart = DateTime.parse((String)eventStartO);
                }
            }
        }
        catch ( Exception e ) {
            // Should not happen since valid index checked above.
            // TODO Handle date/time parsing exceptions.
            continue;
        }
        try {
            eventEndO = rec.getFieldValue(eventEndColumnNum);
            if ( eventEndO == null ) {
                eventEnd = null;
            }
            else {
                if ( eventEndO instanceof DateTime ) {
                    // Just set.
                    eventEnd = (DateTime)eventEndO;
                }
                else if ( eventEndO instanceof Date ) {
                    eventEnd = new DateTime((Date)eventEndO);
                }
                else if ( eventEndO instanceof String ) {
                    eventEnd = DateTime.parse((String)eventEndO);
                }
            }
        }
        catch ( Exception e ) {
            // Should not happen since valid index checked above.
            // TODO Handle date/time parsing exceptions.
            continue;
        }
        try {
            eventID = rec.getFieldValueString(eventIDColumnNum);
        }
        catch ( Exception e ) {
            // Should not happen since valid index checked above.
            continue;
        }
        try {
            label = rec.getFieldValueString(eventLabelColumnNum);
        }
        catch ( Exception e ) {
            // Should not happen since valid index checked above.
            continue;
        }
        try {
            description = rec.getFieldValueString(eventDescriptionColumnNum);
        }
        catch ( Exception e ) {
            // Should not happen since valid index checked above.
            continue;
        }
        tsEventList.add ( new TimeSeriesEvent(ts,
            new TimeLocationEvent(eventID, eventType, eventStart, eventEnd, eventLocationType, eventLocationID,
                label, description)));
    }
    return tsEventList;
}

/**
Return the event table.
*/
public DataTable getEventTable () {
    return this.eventTable;
}

/**
Return the time series being associated with events.
*/
public TS getTimeSeries() {
    return this.ts;
}

}