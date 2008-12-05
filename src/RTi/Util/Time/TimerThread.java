// TODO SAM 2007-05-09 is this class used?

package RTi.Util.Time;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class TimerThread implements Runnable, Serializable {
  protected int increment = 1000;
  protected int elapsed = 0;
  protected int alarm;
  protected List timerListeners = new Vector ();
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
      ((TimerListener) timerListeners.get (i)).incrementUpdate (event);
    }
  }

  protected void alarmUpdate () {
    AlarmEvent event = new AlarmEvent (this);
    //Vector timerListeners = (Vector) this.timerListeners.clone ();
    
    for (int i = 0; i < timerListeners.size (); ++ i)
      ((TimerListener) timerListeners.get (i)).alarmUpdate (event);
  }
}   
