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