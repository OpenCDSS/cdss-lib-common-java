// TimerThread - handles timing a thread

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

// TODO SAM 2007-05-09 is this class used?

package RTi.Util.Time;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("serial")
public class TimerThread implements Runnable, Serializable {
  protected int increment = 1000;
  protected int elapsed = 0;
  protected int alarm;
  protected List<TimerListener> timerListeners = new Vector<TimerListener> ();
  transient protected Thread runner;

  public TimerThread () {
  }

  public TimerThread (int t) {
    alarm = t;
  }

  public void setIncrementTime (int t) {
    increment = t;
  }

  public int getIncrementTime () {
    return increment;
  }

  public void setAlarmTime (int t) {
    alarm = t;
  }

  public int getAlarmTime () {
    return alarm;
  }

  public void setElapsedTime (int t) {
    elapsed = t;
  }

  public int getElapsedTime () {
    return elapsed;
  }

  public synchronized void start () {
    if ( runner == null ) {
      runner = new Thread (this);
      runner.start ();
    }
  }

  public synchronized void stop () {
    //Thread t = runner;
    if ( runner != null) {
      //runner.stop ();
      runner = null;
    }
  }

  public void run () {
    while(true) {
      if (elapsed >= alarm)
	break;
      try {
	Thread.sleep(increment);
	elapsed += increment;
	incrementUpdate();
      }
      catch(Exception e) {}  
    }
    alarmUpdate();
  }

  public void addTimerListener (TimerListener listener) {
    timerListeners.add (listener);
  }

  public void removeTimerListener (TimerListener listener) {
    timerListeners.remove (listener);
  }

  protected void incrementUpdate () {
    IncrementEvent event = new IncrementEvent (this);
    //Vector timerListeners = (Vector) this.timerListeners.clone ();
    
    for (int i = 0; i < timerListeners.size (); ++ i) {
     timerListeners.get(i).incrementUpdate (event);
    }
  }

  protected void alarmUpdate () {
    AlarmEvent event = new AlarmEvent (this);
    //Vector timerListeners = (Vector) this.timerListeners.clone ();
    
    for (int i = 0; i < timerListeners.size (); ++ i)
      timerListeners.get(i).alarmUpdate (event);
  }
}   
