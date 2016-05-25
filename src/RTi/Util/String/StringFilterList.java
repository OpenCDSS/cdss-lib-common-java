package RTi.Util.String;

import java.util.ArrayList;
import java.util.List;

/**
 * List of string filter data to be evaluated by include/exclude checks.
 * @author sam
 *
 */
public class StringFilterList {
	
	/**
	 * List of keys, for example these can be column names or properties.
	 */
	private List<String> keys = new ArrayList<String>();
	
	/**
	 * List of filter patterns to match, no constraint on whether globbing or other regex.
	 */
	private List<String> patterns = new ArrayList<String>();

	/**
	 * Constructor.
	 */
	public StringFilterList () {
	}
	
	/**
	 * Add a filter.
	 * @param key key for filter
	 * @param pattern filter pattern
	 */
	public void add ( String key, String pattern ) {
		keys.add(key);
		patterns.add(pattern);
	}
	
	/**
	 * Return the key at the position.
	 * @param pos filter position 0+.
	 */
	public String getKey ( int pos ) {
		return keys.get(pos);
	}
	
	/**
	 * Return the filter pattern at the position.
	 * @param pos filter position 0+.
	 */
	public String getPattern ( int pos ) {
		return patterns.get(pos);
	}
	
	/**
	 * Return the size of the filter list.
	 */
	public int size () {
		return keys.size();
	}
}