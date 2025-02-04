// TSProgressListener - this interface defines behavior for listening to time series processing

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

package RTi.TS;

/**
This interface defines behavior for listening to time series processing progress.
This is similar to the CommandProgressListener, but is used for granular listening when processing a time series within a command.
Use, for example, when a read command is processing data records and the progress is passed back to a UI for a progress bar.
If processing allows pause, then the individual messages will be important to indicate the current state of processing.
Because there is a performance cost for updating UI components,
progress should normally be updated for a chunk, such as 5% breaks.
The time series progress is unaware of other progress steps that may be handled by the command,
for example an initial web services request.
Consequently, if the implemented listener calls 'commandProgress' for the CommandProgressListener,
the 'commandProgress' method needs to handle the overall progress.
*/
public interface TSProgressListener {

	/**
	Indicate the progress that is occurring when processing a time series.
	This may be a chained call from a CommandProcessor that implements CommandListener to listen to a command.
	This level of monitoring is useful if a command cannot by itself provide dynamic progress updates.
	@param istep The number of steps being executed in time series processing (0+), for example loop index of objects being processed.
	A value of 0 resets the progress bar limits and subsequent calls increment the progress.
	Call with 1+ specify 'percentComplete' > 0.
	@param nstep The total number of steps to process,
	for example total number of time series data records being read or time series values being processed.
	@param percentcomplete if > 0, the value will be used to indicate progress and take precedence over the 'istep' value.
	@param message A short message describing the status (e.g., "Reading value 1 of 1000..." ).
	*/
	public void timeSeriesProgress ( int istep, int nstep, float percentComplete, String message );

}