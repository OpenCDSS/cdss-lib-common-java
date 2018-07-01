package RTi.TS;

/**
 * Interface that requires the implementing class to return a TSIdent,
 * useful for UI objects that list time series and need to provide time series identifier
 * to TSTool in a generic way.
 * @author sam
 *
 */
public interface TimeSeriesIdentifierProvider {

	/**
	 * Return a TSIdent object.
	 * The implementing objec may throw an exception but currently a specific
	 * exception type is not required.
	 * @param pos position (0+), used to retrieve identifier from a set, sequence, etc.
	 */
	public TSIdent getTimeSeriesIdentifier(int pos);
}