// MessageEventQueue - event queue that will intercept AWEvents and log any exceptions that occur when the events are dispatched

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

package RTi.Util.Message;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;

import RTi.Util.Message.Message;

/**
Event queue that will intercept AWEvents and log any exceptions that occur when the events are dispatched.
Exceptions are logged at Message level 3.
Declare an instance of this class before any GUI components are initialized.
*/
public class MessageEventQueue extends EventQueue {
    
public MessageEventQueue() {
    // Replace the standard event queue with this instance
    Toolkit.getDefaultToolkit().getSystemEventQueue().push(this);
}

/**
Handle all AWTEvents but also log to the Message class when an event throws an exception.
@param event AWTEvent to handle.
*/
protected void dispatchEvent(AWTEvent event)
{   String routine = "EventQueueMessage.dispatchEvent";
    try {
        // If everything is OK then this will simply pass the event on
        super.dispatchEvent(event);
    } catch (ThreadDeath td) {
        td.printStackTrace();
        Message.printWarning(3, routine, "Thread death on AWT" );
        Message.printWarning(3, routine, td );
    } catch (Throwable t) {
        t.printStackTrace();
        Message.printWarning(3, routine, "Unexpected Internal Error" );
        Message.printWarning(3, routine, t );
    }
}

}
