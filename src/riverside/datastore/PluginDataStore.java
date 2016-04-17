package riverside.datastore;

import java.util.List;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.Time.DateTime;

// TODO SAM 2016-04-16 This is pretty specific to the needs of TSTool so may move it to the TSTool package
/**
 * Interface for a plugin datastore, which allows integration of plugins with frameworks.
 * @author sam
 *
 */
public interface PluginDataStore {
    
	/**
	 * Create an input filter panel for the datastore, used to query the time series list.
	 * The providesTimeSeriesInputFilterPanel() method should be called first to determine whether this
	 * method is supported.
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
	public JWorksheet_AbstractRowTableModel createTimeSeriesListTableModel(String dataType, String timeStep, InputFilter_JPanel ifp );
	
	/**
	 * Return the list of time series data type strings.
	 * These strings are specific to the datastore and may be simple like "DataType1"
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
	public TSIdent getTimeSeriesIdentifierFromTableModel(JWorksheet_AbstractRowTableModel tableModel, int row);
	
    /**
     * Get the CellRenderer used for displaying the time series in a TableModel.
     * 
     */
    public JWorksheet_AbstractExcelCellRenderer getTimeSeriesListCellRenderer(JWorksheet_AbstractRowTableModel tableModel);
	
    /**
     * Indicate whether the plugin provides an input filter panel for the time series list.
     * This is a component that provides interactive query filters for user interfaces.
     * @param return true if an input filter is provided.
     */
    public boolean providesTimeSeriesListInputFilterPanel();
    
    /**
     * Get the TableModel used for displaying the time series.
     * 
     */
    public JWorksheet_AbstractRowTableModel getTimeSeriesListTableModel(List<? extends Object> data);
    
    /**
     * Read a time series given its time series identifier.
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsidentString2, DateTime readStart, DateTime readEnd, boolean readData );
}