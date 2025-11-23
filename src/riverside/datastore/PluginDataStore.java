// PluginDataStore - interface to define behavior of plugin DataStore

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

package riverside.datastore;

import java.util.List;
import java.util.Map;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.Time.DateTime;

// TODO SAM 2016-04-16 This is pretty specific to the needs of TSTool so may move it to the TSTool package
/**
 * Interface for a plugin datastore, which allows integration of plugins with frameworks.
 */
public interface PluginDataStore {

	/**
	 * Create an input filter panel for the datastore, used to query the time series list.
	 * The providesTimeSeriesInputFilterPanel() method should be called first to determine whether this method is supported.
	 * @return an InputFilter_Panel instance, or null if the method is not supported.
	 */
	public InputFilter_JPanel createTimeSeriesListInputFilterPanel ();

	/**
	 * Create a time series list table model given the desired data type, time step (interval), and input filter.
	 * The datastore performs a suitable query and creates objects to manage in the time series list.
	 * @param dataType time series data type to query, controlled by the datastore
	 * @param timeStep time interval to query, controlled by the datastore
	 * @param ifp input filter panel that provides additional filter options
	 * @return a TableModel containing the defined columns and rows.
	 */
	@SuppressWarnings("rawtypes")
	public JWorksheet_AbstractRowTableModel createTimeSeriesListTableModel(String dataType, String timeStep, InputFilter_JPanel ifp );

	/**
	 * Return the list of plugin properties, used to facilitate integration of plugin with application.
	 * There is not a standard list of properties, but the following are useful:
	 * <ul>
	 * <li> Version - a version string following semantic versioning (e.g., "1.2.3" or "1.2.3 (2020-05-29)",
	 *      which can be used in documentation at a minimum.</li>
	 * </ul>
	 */
	public Map<String,Object> getPluginProperties();

	// TODO smalers 2025-11-21 it is a problem to change the interface because many plugins won't have.
	// For now, dynamically check for the method signature in TSTool using reflection.
	/**
	 * Return a list of comments for a time series from the table model.
	 * The comments will each be added as a command before the TSID command in TSTool.
	 * @param tableModel the table model from which to extract data
	 * @param row the displayed table row
	 * @return the list of comments, or null or empty list
	 */
	//@SuppressWarnings("rawtypes")
	//public List<String> getTimeSeriesCommentsFromTableModel(JWorksheet_AbstractRowTableModel tableModel, int row);

	/**
	 * Return the list of time series data type strings.
	 * These strings are specific to the datastore and may be simple like * "DataType1"
	 * or more complex like "DataStore1 - note for data type".
	 * @param dataInterval data interval from TimeInterval.getName(TimeInterval.HOUR,0) to filter the list of data types.
	 * If null, blank, or "*" the interval is not considered when determining the list of data types.
	 */
	public List<String> getTimeSeriesDataTypeStrings(String dataInterval);

	/**
	 * Return the list of time series data interval strings.
	 * This should result from calls like:  TimeInterval.getName(TimeInterval.HOUR, 0)
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 */
	public List<String> getTimeSeriesDataIntervalStrings(String dataType);

	/**
	 * Return the identifier for a time series in the table model.
	 * The TSIdent parts will be uses as TSID commands.
	 * @param tableModel the table model from which to extract data
	 * @param row the displayed table row
	 */
	@SuppressWarnings("rawtypes")
	public TSIdent getTimeSeriesIdentifierFromTableModel(JWorksheet_AbstractRowTableModel tableModel, int row);

    /**
     * Get the CellRenderer used for displaying the time series in a TableModel.
     */
    @SuppressWarnings("rawtypes")
	public JWorksheet_AbstractExcelCellRenderer getTimeSeriesListCellRenderer(JWorksheet_AbstractRowTableModel tableModel);

    /**
     * Indicate whether the plugin provides an input filter panel for the time series list.
     * This is a component that provides interactive query filters for user interfaces.
     * @param return true if an input filter is provided.
     */
    public boolean providesTimeSeriesListInputFilterPanel();

    /**
     * Get the TableModel used for displaying the time series.
     */
    @SuppressWarnings("rawtypes")
	public JWorksheet_AbstractRowTableModel getTimeSeriesListTableModel(List<? extends Object> data);

    /**
     * Read a time series given its time series identifier.
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsidentString2, DateTime readStart, DateTime readEnd, boolean readData );
}