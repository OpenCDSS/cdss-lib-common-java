// Adapter - interface to build a Data Services adapter.

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

//------------------------------------------------------------------------------------
// Adapter - interface to build a Data Services adapter.
//------------------------------------------------------------------------------------
// History:
//
//      2006-06-16      Scott Townsend, RTi     Create initial version of this
//                                              Adapter interface. This interface
//						will help developers build new
//						Data Services adapters as the data
//						become available.
//------------------------------------------------------------------------------------
// Endheader

package RTi.DataServices.Adapter;

import java.lang.Exception;
import java.util.List;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.Util.Time.DateTime;

/**
 * The interface for all adapters instantiated in the adapter package.
 * 
 */
public interface Adapter {

/**
 * <p>Method which takes parameters and returns a RTi time series object.</p>
 * 
 * 
 * @param TSIdentifier A RTi time series identifier object.
 * @param reqDate1 Start time for the time series object.
 * @param reqDate2 End date time for the time series object.
 * @param reqUnits Requested units for the time series object.
 * @param readData Parameter which specifies whether or not to retrieve the data or only return.
 * @return The method returns an RTi time series object for further processing.
 */
	public TS readTimeSeries(TSIdent TSIdentifier, DateTime reqDate1, 
		DateTime reqDate2, String reqUnits, boolean readData) throws Exception;

/**
 * <p>Method which takes parameters and returns a Vector of RTi time series 
 * objects.</p>
 * 
 * 
 * @param TSIdentifier A RTi time series identifier object.
 * @param reqDate1 Start time for the time series object.
 * @param reqDate2 End date time for the time series object.
 * @param reqUnits Requested units for the time series object.
 * @param readData Parameter which specifies whether or not to 
 * retrieve the data or only return header/identifier information.
 * @return The method returns a Vector of RTi time series objects.
 */
    public List<TS> readTimeSeriesList(TSIdent TSIdentifier, DateTime reqDate1, 
    	DateTime reqDate2, String reqUnits, boolean readData) throws Exception;
} // End of interface Adapter

