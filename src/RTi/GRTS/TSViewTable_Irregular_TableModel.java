package RTi.GRTS;

import java.util.Vector;

import RTi.TS.IrregularTS;
import RTi.TS.TSData;
import RTi.TS.TSException;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
/**
 *  Provides a table model for displaying irregular TS.
 *  <p>
 *  
 *  @see TSViewTable_TableModel for displaying regular TS. 
 */
public class TSViewTable_Irregular_TableModel extends
        TSViewTable_TableModel
{

  Vector dataPoints;
  IrregularTS irrTS = null;
  
  public TSViewTable_Irregular_TableModel(Vector data, DateTime start,
          int intervalBase, int intervalMult, int dateFormat,
          String[] dataFormats, boolean useExtendedLegend)
          throws Exception
    {
      this(data, start, intervalBase, intervalMult, dateFormat,
              dataFormats, useExtendedLegend, 50);
      // TODO Auto-generated constructor stub
    }

  public TSViewTable_Irregular_TableModel(Vector data, DateTime start,
          int intervalBase, int intervalMult, int dateFormat,
          String[] dataFormats, boolean useExtendedLegend,
          int cacheInterval) throws Exception
    {
      super(data, start, intervalBase, intervalMult, dateFormat,
              dataFormats, useExtendedLegend, cacheInterval);
    }

  /**
   * Determine the number of rows for the table model.
   * 
   * @param data 
   * @throws TSException
   */
  protected void calcRowCount(Vector data) throws TSException
  {
    String routine = "calcRowCount";
    if (data.elementAt(0) instanceof IrregularTS)
      {
        irrTS = (IrregularTS)data.elementAt(0);
      }
    else
      {       
        Message.printWarning(3, routine,
                "Not a irregularTS: " + irrTS.getIdentifierString() );
      }
    dataPoints = irrTS.getData();
   //TODO: dre verify right start & end dates
   // _rows = irrTS.calculateDataSize(__start, irrTS.getDate2());
    _rows = dataPoints.size();
  }
  
  /**
  Returns the data that should be placed in the JTable at the given row and 
  column.
  @param row the row for which to return data.
  @param col the column for which to return data.
  @return the data that should be placed in the JTable at the given row and col.
  */
  public Object getValueAt(int row, int col) 
  {
    TSData d = (TSData)dataPoints.elementAt(row);
    
    if (col == 0)
      {
        return d.getDate();
      }
    else
      {
        return new Double(d.getData());
      }
  }
  
  
  /**
   * Initialize the dates for cache.
   */
  /* FIXME SAM 2008-03-24 Need to enable irregular time series viewing
  protected void initializeCacheDates()
  {
    if(true) return;
    
    IrregularTS irrTS = (IrregularTS)_data.elementAt(0);
    __cachedDates = new DateTime[(_rows / __cacheInterval) + 1];

    // Cache the dates of each __cacheInterval row through the time series.
    Vector dataPoints = irrTS.getData();

    int index = 0;
    for (int i = 0; i < __cachedDates.length; i++)
      {
        index += __cacheInterval;
        TSData d = (TSData)dataPoints.elementAt(index);
        d.getDate();
        __cachedDates[i] = new DateTime(d.getDate());
      }
  }
  */
} // eof class TSViewTable_Irregular_TableModel
