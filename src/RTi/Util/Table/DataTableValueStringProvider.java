package RTi.Util.Table;

/**
 * The DataTableValueGetter interface defines behavior for retrieving object from a DataTable.
 * Implementation of this interface is useful when manipulating the data for a cell
 * prior to setting or after retrieval.
 * @author sam
 *
 */
public interface DataTableValueStringProvider {

	/**
	 * Return the object value in a table cell.
	 * @param valueFormat a format string to be interpreted by called code,
	 * for example ${Property} when used with TSTool.
	 */
	public String getTableCellValueAsString ( String valueFormat );
}