package RTi.Util.Time;

import java.util.EventObject;

@SuppressWarnings("serial")
public class IncrementEvent extends EventObject {
  int elapsed;

  public IncrementEvent (TimerThread source) {
    super (source);
    elapsed = source.getElapsedTime();
  }

  public int getElapsedTime(int elapsed) {
    return elapsed;
  }
}
