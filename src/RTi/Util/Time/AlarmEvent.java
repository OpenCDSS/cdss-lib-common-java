// 13 Apr 1999	SAM, RTi		Add finalize.

package RTi.Util.Time;

import java.util.EventObject;

@SuppressWarnings("serial")
public class AlarmEvent extends EventObject
{

public AlarmEvent (TimerThread source)
{
    super (source);
}

/**
Finalize before garbage collection.
*/
protected void finalize()
throws Throwable
{
	super.finalize();
}

} // End AlarmEvent class
