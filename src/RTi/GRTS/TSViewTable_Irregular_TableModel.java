// TSViewTable_Irregular_TableModel - provides a table model for displaying irregular TS

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

package RTi.GRTS;

import java.util.List;

import RTi.TS.IrregularTS;
import RTi.TS.TS;
import RTi.TS.TSData;
import RTi.TS.TSException;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
/**
 *  Table model for displaying irregular TS.
 *  @see TSViewTable_TableModel for displaying regular TS. 
 */
public class TSViewTable_Irregular_TableModel extends TSViewTable_TableModel
{

  /**
	 * Hash for serialization and to avoid Java warning.
	 */
	private static final long serialVersionUID = 1L;

  List<TSData> dataPoints;
  IrregularTS irrTS = null;
  
  public TSViewTable_Irregular_TableModel(List<TS> data, DateTime start,
          int intervalBase, int intervalMult, int dateFormat,
          String[] dataFormats, boolean useExtendedLegend)
          throws Exception {
      this(data, start, intervalBase, intervalMult, dateFormat, dataFormats, useExtendedLegend, 50);
  }

  public TSViewTable_Irregular_TableModel(List<TS> data, DateTime start,
          int intervalBase, int intervalMult, int dateFormat,
          String[] dataFormats, boolean useExtendedLegend,
          int cacheInterval) throws Exception {
      super(data, start, intervalBase, intervalMult, dateFormat, dataFormats, useExtendedLegend, cacheInterval);
  }

  /**
   * Determine the number of rows for the table model.
   * 
   * @param data 
   * @throws TSException
   */
  protected void calcRowCount(List<IrregularTS> data) throws TSException {
    String routine = "calcRowCount";
    if (data.get(0) instanceof IrregularTS) {
        irrTS = data.get(0);
    }
    else {       
        Message.printWarning(3, routine, "Not a irregularTS: " + irrTS.getIdentifierString() );
    }
    dataPoints = irrTS.getData();
    _rows = dataPoints.size();
  }
  
  /**
  Returns the data that should be placed in the JTable at the given row and column.
  @param row the row for which to return data.
  @param col the column for which to return data.
  @return the data that should be placed in the JTable at the given row and col.
  */
  public Object getValueAt(int row, int col) {
    TSData d = (TSData)dataPoints.get(row);
    
    if (col == 0) {
        return d.getDate();
    }
    else {
        return new Double(d.getDataValue());
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
    List dataPoints = irrTS.getData();

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
}