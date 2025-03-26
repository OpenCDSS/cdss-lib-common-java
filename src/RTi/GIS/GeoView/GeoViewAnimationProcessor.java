// GeoViewAnimationProcessor - threaded animation processor for animated layers

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

import java.util.ArrayList;
import java.util.List;

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
List of listeners to be notified during a process.
*/
private List<ProcessListener> __processListeners;

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
	if (__processListeners == null) {
		__processListeners = new ArrayList<>();
	}
	__processListeners.add(p);
}

/**
Cancels the animation.
*/
public void cancel() {
	__cancelled = true;
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
	if (__processListeners == null) {
		return;
	}

	int size = __processListeners.size();
	ProcessListener p = null;
	for (int i = 0; i < size; i++) {
		p = __processListeners.get(i);
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

		notifyListenersStatus(1, "Retrieving data for " + currDateString);
		__animationJFrame.fillData(__currentDate);
		notifyListenersStatus(0, __currentDate.toString( DateTime.FORMAT_YYYY_MM));
		notifyListenersStatus(1, "Drawing map display for " + currDateString);
		try {
			Thread.sleep(100);
		}
		catch (Exception e) {
		}
		__viewComponent.redraw();
		__currentDate.addMonth(1);
		try {
			Thread.sleep(200);
		}
		catch (Exception e) {
		}

		if (((double)__pause / 1000.0) == 1) {
			notifyListenersStatus(1, "Waiting 1 second at " + currDateString);
		}
		else {
			notifyListenersStatus(1, "Waiting " + ((double)__pause / 1000.0) + " seconds at " + currDateString);
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