package RTi.GRTS;

import java.beans.PropertyChangeListener;

import javax.swing.event.SwingPropertyChangeSupport;

import RTi.GR.GRPoint;
import RTi.TS.TS;
import RTi.TS.TSData;
import RTi.TS.TSIterator;
import RTi.Util.Math.MathUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/*
 * Provides editing functionality for graphs.
 */
public class TSGraphEditor
{
  /** Time Series being edited */
  private TS _ts;
  /** Retains last point edited */
  private GRPoint _prevPoint = null;
  private DateTime _prevDate = null;
  private DateTime _currentDate;
  private GRPoint _currentPoint;
  /** Tracks whether prev & current points have been swapped */
  private boolean _swapFlag = false;
  /** Controls whether auto-connect will be applied */
  private boolean _autoConnect = true;

  // property support
  private SwingPropertyChangeSupport _propertyChangeSupport =
             new SwingPropertyChangeSupport(this);
  
  /**
   * Creates an instance of TSGraphEditor.
   * <p>
   * The TSGraphEditor receives new GRPoints & applies them to the
   * TS. It is responsible for auto-connecting points.
   * 
   * @param ts
   */
  public TSGraphEditor(TS ts)
  {
    _ts = ts;
    
  }

  /**
   * Edit data point by clicking above or below it.
   * <p>
   * The date is determined by rounding to the nearest date.
   * 
   * @param datapt Point to be edited (data coordinates)
   */
  public void editPoint(GRPoint datapt)
  {
    //System.out.println("editPoint" + datapt.toString());
    DateTime date = new DateTime(datapt.x, true);

    int intervalBase = _ts.getDataIntervalBase();
    int intervalMult = _ts.getDataIntervalMult();
//    System.out.println(">>> base: " + intervalBase + " X: " + intervalMult);
    if (intervalBase == TimeInterval.HOUR)
      {
        //TODO: handle other time intervals
        if (intervalMult>0)
          {
            date.addHour(intervalMult/2);
          }
      }
//    System.out.println(">>> " + date + ": " + datapt.y);
    date.round(-1, intervalBase, intervalMult);
    _ts.setDataValue(date, datapt.y);
    
    // Notify listeners
    _propertyChangeSupport.firePropertyChange("TS_DATA_VALUE_CHANGE", null, _ts);
//    System.out.println(">r  " + date + ": " + datapt.y);

    // Save for potential operation
    _prevDate = _currentDate;
    _prevPoint = _currentPoint;
    _currentDate = date;
    _currentPoint = new GRPoint(date.toDouble(), datapt.y);

    if ( _prevPoint != null &&_autoConnect)
      {
        autoConnect();
      }
  }

  private void autoConnect()
  {
    doFillWithInterpolation();
  } 

  /**
   * Interpolates the values of y  for points between the last two points
   * edited.
   * <p>
   * Two points must have been edited prior to calling this functionality.
   * <p>
   * Auto-connect is only to be applied for currentDate occurring to the
   * right of prevDate.
   */
  public void doFillWithInterpolation()
  {
 
//    System.out.println("doFillWithInterpolation");
    if ( _prevPoint == null || _currentDate.equals(_prevDate))
      {
        return;
      }
    if (_currentDate.lessThan(_prevDate))
      {
        return; // 
//        if (!_autoConnect)
//          {
//            swapPoints();
//          }
      }
    try
    {
      TSIterator tsi = _ts.iterator(_prevDate, _currentDate);
      TSData data;  // This is volatile and the iterator reuses its reference

      // Skip the first as it has been edited
      tsi.next();
      while ( true ) 
        {
          data = tsi.next();

          if (data.getDate().equals(_currentDate))
            {
              // Notify listeners only once after all changes
              _propertyChangeSupport.firePropertyChange("TS_DATA_VALUE_CHANGE", _ts, _ts);

              if (_swapFlag)
                {
                  swapPoints();
                }
              // Skip the last as it has been edited
              return;
            }
          else
            {
              DateTime date = data.getDate();

              double val = MathUtil.interpolate(date.toDouble(), _prevDate.toDouble(),
                  _currentDate.toDouble(),
                  _prevPoint.getY(), _currentPoint.getY());

              _ts.setDataValue(date, val);
              //  System.out.println(" ------> newY");
            }
        }
    }
    catch (Exception e)
    {
      //  Message.printWarning(1, "doFillWithInterpolation", e.toString());
      System.out.println("prevDate:" + _prevDate 
          + " curDate: " + _currentDate);
      e.printStackTrace();
    }
  } // eof doFillWithInterpolation

  /**
   * Swaps the data for previous & current points.
   */
  private void swapPoints()
  {
    DateTime tmpDateTime= _prevDate;
    _prevDate = _currentDate;
    _currentDate = tmpDateTime;

    GRPoint tmpGRPoint = _prevPoint;
    _prevPoint = _currentPoint;
    _currentPoint = tmpGRPoint;
    _swapFlag  = !_swapFlag;
  }
  /**
   * Sets whether point editing uses auto-connect.
   * <p>
   * Auto-connect mode will use interpolation to determine
   * values between the previously edited point & the currently
   * edited point. The currently edited point must be to the right
   * of the previously edited point.
   * 
   * @param selected
   */
  public void setAutoConnect(boolean autoConnect)
  {
    _autoConnect = autoConnect; 
  }
  
  /* - - - Property Change Support - - - */
  public void addPropertyChangeListener(PropertyChangeListener l)
  {
     _propertyChangeSupport.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(PropertyChangeListener l)
  {
     _propertyChangeSupport.removePropertyChangeListener(l);
  }
}
