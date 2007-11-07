package RTi.Util.Time;

import java.util.*;

public interface TimerListener extends EventListener {
  public void alarmUpdate(AlarmEvent e);
  public void incrementUpdate(IncrementEvent e);
}
