// TS_ListSelector_Listener - an interface for classes that want to be notified when time series are selected from a TS_ListSelector_JFrame.

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

// ----------------------------------------------------------------------------
// TS_ListSelector_Listener - an interface for classes that want to be 
//	notified when time series are selected from a TS_ListSelector_JFrame.
// ----------------------------------------------------------------------------
// History:
//
// 2005-03-29	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-04	JTS, RTi		timeSeriesSelected() now passes in the
//					text of the button that was pushed on
//					the GUI.
// ----------------------------------------------------------------------------

package RTi.TS;

import java.util.List;

/**
This interface is for classes that want to be notified when time series
are chosen from a TS_ListSelector_JFrame instance.
*/
public interface TS_ListSelector_Listener {

/**
Called when time series are selected from a TS_ListSelector_JFrame.
@param sender the instance of TS_ListSelector_JFrame that is sending the 
notification.
@param tsList the list of time series that were selected.  The list
will never be null, though it could be empty.
@param action the action performed by the button the user pushed which caused
this listener to be notified.  Possible values are:<p>
<ul>
<li>Graph -- when a time series is to be graphed.</li>
<li>OK -- when the user selected time series and pressed the "OK" button.  
This value may change, depending on whether the TS_ListSelector_JFrame was
instantiated with a different value for the NotifyButtonLabel property.</li>
<li>Summary -- when a time series summary should be displayed.</li>
<li>Table -- when a time series is to be displayed in a tabular format.</li>
</ul>
*/
public void timeSeriesSelected(TS_ListSelector_JFrame sender, List<TS> tsList, String action);

}
