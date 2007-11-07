//-----------------------------------------------------------------------------
// GeoViewAnimationProcessor - Threaded animation processor for animated
//	layers.
//-----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
// 2004-08-04	J. Thomas Sapienza, RTi	Initial version.
// 2004-08-09	JTS, RTi		* Revised GUI.
//					* Added process listener code.
// 2005-04-27	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.util.Vector;

import RTi.Util.IO.ProcessListener;

import RTi.Util.Time.DateTime;

/**
This class is a threaded processor that runs an animation.
*/
public class GeoViewAnimationProcessor
implements Runnable {

/**
Whether an animation is going on.
*/
private boolean __animating = false;

/**
Whether the animation is cancelled.
*/
private boolean __cancelled = false;

/**
Whether the animation is paused.
*/
private boolean __paused = false;

/**
The date currently being animated.
*/
private DateTime __currentDate;

/**
The last date to be animated.
*/
private DateTime __endDate;

/**
The gui that controls a layer animation.
*/
private GeoViewAnimationJFrame __animationJFrame;

/**
The component drawing the map.
*/
private GeoViewJComponent __viewComponent;

/**
The amount of time to pause between animation steps.
*/
private int __pause;

/**
Vector of listeners to be notified during a process.
*/
private Vector __listeners;

/**
Constructor.
@param parent the parent gui controlling the animation.
@param viewComponent the component on which the map is drawn.
@param startDate the date from which to being animating.
@param steps the number of steps of data to animate.
@param interval the interval between dates.
@param pause the amount of pause (in milliseconds) between animation updates.
*/
public GeoViewAnimationProcessor(GeoViewAnimationJFrame parent,
GeoViewJComponent viewComponent, DateTime startDate, DateTime endDate,
int pause) {
	__animationJFrame = parent;
	__viewComponent = viewComponent;
	__currentDate = new DateTime(startDate);
	__endDate = endDate;
	__pause = pause;
}

/**
Adds a process listener to be notified during processing.
@param listener the listener to be added.
*/
public void addProcessListener(ProcessListener p) {
	if (__listeners == null) {
		__listeners = new Vector();
	}
	__listeners.add(p);
}

/**
Cancels the animation.
*/
public void cancel() {
	__cancelled = true;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__currentDate = null;
	__endDate = null;
	__animationJFrame = null;
	__viewComponent = null;
	__listeners = null;
}

/**
Returns the current date of animation.
@return the current date of animation.
*/
public DateTime getCurrentDate() {
	return __currentDate;
}

/**
Returns whether an animation is going on or not.
@return whether an animation is going on or not.
*/
public boolean isAnimating() {
	return __animating;
}

/**
Notifies listeners of status messages.
@param code the code to send with status messages.
@param message the text of the status message.
*/
public void notifyListenersStatus(int code, String message) {
	if (__listeners == null) {
		return;
	}

	int size = __listeners.size();
	ProcessListener p = null;
	for (int i = 0; i < size; i++) {
		p = (ProcessListener)__listeners.elementAt(i);
		p.processStatus(code, message);
	}
}

/**
Pauses or unpauses the animation.
@param pause if true the animation is paused, if false it continues.
*/
public void pause(boolean paused) {
	__paused = paused;
}

/**
Runs the animation.
*/
public void run() {
	__paused = false;
	__cancelled = false;

	String currDateString = null;
	
	while (true) {
		__animating = true;
		currDateString = __currentDate.toString(
			DateTime.FORMAT_YYYY_MM);
		if (__paused) {
			notifyListenersStatus(1, "(Paused at "
				+ currDateString + ")");
			try {
				Thread.sleep(200);
			}
			catch (Exception e) {}
			continue;
		}
		else if (__cancelled) {
			__animationJFrame.animationDone();
			__animating = false;
			return;
		}

		notifyListenersStatus(1, "Retrieving data for "
			+ currDateString);
		__animationJFrame.fillData(__currentDate);
		notifyListenersStatus(0, __currentDate.toString(
			DateTime.FORMAT_YYYY_MM));
		notifyListenersStatus(1, "Drawing map display for "
			+ currDateString);
		try {
			Thread.sleep(100);
		}
		catch (Exception e) {}		
		__viewComponent.redraw();
		__currentDate.addMonth(1);
		try {
			Thread.sleep(200);
		}
		catch (Exception e) {}		

		if (((double)__pause / 1000.0) == 1) {
			notifyListenersStatus(1, "Waiting 1 second at "
				+ currDateString);
		}
		else {
			notifyListenersStatus(1, "Waiting " 
				+ ((double)__pause / 1000.0)
				+ " seconds at " + currDateString);
		}

		try {
			Thread.sleep(__pause);
		}
		catch (Exception e) {}

		if (__currentDate.greaterThanOrEqualTo(__endDate)) {
			__animationJFrame.animationDone();
			__animating = false;
			return;
		}
	}
}

/**
Sets the end date of the run.
@param date the last date of animation.
*/
public void setEndDate(DateTime endDate) {
	__endDate = endDate;
}

/**
Sets the amount of pause (in milliseconds) between animation updates.
@param pause the amount of pause between updates.
*/
public void setPause(int pause) {
	__pause = pause;
}

/**
Sets the start date (from which the animation begins).
@param date the starting date of animation.
*/
public void setStartDate(DateTime date) {
	__currentDate = new DateTime(date);
}

public void sleep(int sleep) {
	try {
		Thread.sleep(sleep);
	}
	catch (Exception e) {}
}

}
