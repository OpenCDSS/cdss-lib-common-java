package RTi.Util.Table;

/**
 * This enumeration defines data table function types, which are functions that
 * are used to assign data to table cells.
 */
public enum DataTableFunctionType {
	/**
	 * The table row (1+).
	 */
	ROW("Row"),
	/**
	 * The table row (0+).
	 */
	ROW0("Row0");

	/**
	 * The name that should be displayed in UIs and reports.
	 */
	private final String displayName;

	/**
	 * Construct a time series statistic enumeration value.
	 * 
	 * @param displayName
	 *            name that should be displayed in choices, etc.
	 */
	private DataTableFunctionType(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Return the display name for the type. This is usually similar to the
	 * value but using appropriate mixed case.
	 * 
	 * @return the display name.
	 */
	@Override
	public String toString() {
		return displayName;
	}

	/**
	 * Return the enumeration value given a string name (case-independent).
	 * 
	 * @return the enumeration value given a string name (case-independent), or
	 *         null if not matched.
	 */
	public static DataTableFunctionType valueOfIgnoreCase(String name) {
		if (name == null) {
			return null;
		}
		DataTableFunctionType[] values = values();
		for (DataTableFunctionType t : values) {
			if (name.equalsIgnoreCase(t.toString())) {
				return t;
			}
		}
		return null;
	}

}