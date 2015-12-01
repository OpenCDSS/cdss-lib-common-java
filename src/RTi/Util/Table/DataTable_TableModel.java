package RTi.Util.Table;

import java.util.Date;

import RTi.DMI.DMIUtil;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
Table model for displaying data table data in a JWorksheet.
*/
public class DataTable_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
The classes of the fields, stored in an array for quicker access.
*/
private Class[] __fieldClasses;

/**
The field types as per the table field types.
*/
private int [] __fieldTypes;

/**
The table displayed in the worksheet.
*/
private DataTable __dataTable;

/**
The number of columns in the table model.
*/
private int __columns = 0;

/**
The formats of the table fields, stored in an array for quicker access.
*/
private String[] __fieldFormats;

/**
The names of the table fields, stored in the array for quicker access.
*/
private String[] __fieldNames;

/**
Constructor.
@param dataTable the table to show in a worksheet.
@throws NullPointerException if the dataTable is null.
*/
public DataTable_TableModel(DataTable dataTable) 
throws Exception {
	if (dataTable == null) {
		throw new NullPointerException();
	}

	__dataTable = dataTable;
	_rows = __dataTable.getNumberOfRecords();
	__columns = __dataTable.getNumberOfFields();

	__fieldNames = __dataTable.getFieldNames();
	__fieldFormats = __dataTable.getFieldFormats();
	__fieldTypes = __dataTable.getFieldDataTypes();
	__fieldClasses = determineClasses(__fieldTypes);
}

/**
Determines the kind of classes stored in each table field.
@param dataTypes the data types array from the data table.
@return an array of the Class of each field.
*/
private Class[] determineClasses(int[] dataTypes) {
	Class[] classes = new Class[dataTypes.length];

	for (int i = 0; i < dataTypes.length; i++) {
		if ( __dataTable.isColumnArray(dataTypes[i]) ) {
			classes[i] = String.class;
		}
		else {
			switch (dataTypes[i]) {
				case TableField.DATA_TYPE_ARRAY:
					// For the purposes of rendering in the table, treat array as formatted string "[ , , , ]"
					classes[i] = String.class;
					break;
				case TableField.DATA_TYPE_BOOLEAN:
					classes[i] = Boolean.class;
					break;
				case TableField.DATA_TYPE_INT:
					classes[i] = Integer.class;
					break;
				case TableField.DATA_TYPE_SHORT:
					classes[i] = Short.class;
					break;
				case TableField.DATA_TYPE_DOUBLE:
					classes[i] = Double.class;
					break;
				case TableField.DATA_TYPE_FLOAT:
					classes[i] = Float.class;
					break;
				case TableField.DATA_TYPE_STRING:
					classes[i] = String.class;
					break;
				case TableField.DATA_TYPE_DATE:
					classes[i] = Date.class;
					break;
	            case TableField.DATA_TYPE_DATETIME:
	                classes[i] = DateTime.class;
	                break;
	            case TableField.DATA_TYPE_LONG:
	                classes[i] = Long.class;
	                break;
	            default:
	            	throw new RuntimeException ( "TableField data type " + dataTypes[i] + " is not supported in DataTable table model." ); 
			}
		}
	}
	return classes;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	IOUtil.nullArray(__fieldClasses);
	__dataTable = null;
	IOUtil.nullArray(__fieldFormats);
	IOUtil.nullArray(__fieldNames);
	super.finalize();
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	return __fieldClasses[columnIndex];
}

/**
Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __columns;
}

/**
Returns the name of the column at the given position.
@param columnIndex the position of the column for which to return the name.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	String prefix = "";
	if (_worksheet != null) {
		prefix = _worksheet.getColumnPrefix(columnIndex);
	}
	return prefix + __fieldNames[columnIndex];
}

/**
Returns an array containing the column tool tips.
@return a String array containing the tool tips for each field (the field descriptions are used).
*/
public String[] getColumnToolTips() {
    String[] tips = new String[__columns];
    for (int i = 0; i < __columns; i++) {
        tips[i] = __dataTable.getTableField(i).getDescription();
    }
    return tips;
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
    int[] widths = new int[__columns];
    for (int i = 0; i < __columns; i++) {
        widths[i] = __dataTable.getFieldWidth(i);
        if ( widths[i] < 0 ) {
            widths[i] = 15; // Default
        }
    }
    return widths;
}

/**
Returns the format to be applied to data values in the column, for display in the table.
If the column contains an array, the format applies to the individual values in the array.
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (__fieldTypes[column]) {
		case TableField.DATA_TYPE_ARRAY:
			// For the purposes of rendering in the table, treat array as formatted string
			return "%s";
		default:
			return __fieldFormats[column];
	}
}

/**
Returns the number of rows of data in the table.
@return the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	try {
		if ( __dataTable.isColumnArray(__fieldTypes[col]) ) {
			// Column is an array of primitive types
			// Get the internal data type
			int dataType = __fieldTypes[col] - TableField.DATA_TYPE_ARRAY_BASE;
			// For the purposes of rendering in the table, treat array as formatted string [ val1, val2, ... ]
			// Where the formatting of the values is for the raw value
			StringBuilder b = new StringBuilder("[");
			Object oa = null;
			switch ( dataType ) {
				case TableField.DATA_TYPE_DATETIME:
					DateTime [] dta = (DateTime [])__dataTable.getFieldValue(row,col);
					for ( int i = 0; i < dta.length; i++ ) {
						if ( i > 0 ) {
							b.append(",");
						}
						if ( dta[i] != null ) {
							b.append("\"" + dta[i] + "\"");
						}
					}
					break;
				case TableField.DATA_TYPE_DOUBLE:
					oa = __dataTable.getFieldValue(row,col);
					double [] da = new double[0];
					if ( oa == null ) {
						return null;
					}
					else if ( oa instanceof double[] ) {
						da = (double [])oa;
					}
					else if ( oa instanceof Double[] ) {
						Double [] Da = (Double [])oa;
						da = new double[Da.length];
						for ( int i = 0; i < da.length; i++ ) {
							if ( DMIUtil.isMissing(Da[i])) {
								da[i] = DMIUtil.MISSING_DOUBLE;
							}
							else {
								da[i] = Da[i];
							}
						}
					}
					else {
						throw new RuntimeException ( "Don't know how to handle double array - is not double[] or Double[]" );
					}
					for ( int i = 0; i < da.length; i++ ) {
						if ( i > 0 ) {
							b.append(",");
						}
						if ( !DMIUtil.isMissing(da[i]) ) {
							// Need to get the TableField format because the overall column will be string
							// TODO SAM 2015-09-06
							//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
							b.append(StringUtil.formatString(da[i],"%.6f"));
						}
					}
					break;
				case TableField.DATA_TYPE_FLOAT:
					oa = __dataTable.getFieldValue(row,col);
					float [] fa = new float[0];
					if ( oa == null ) {
						return null;
					}
					else if ( oa instanceof float[] ) {
						fa = (float [])oa;
					}
					else if ( oa instanceof Float[] ) {
						Float [] Fa = (Float [])oa;
						fa = new float[Fa.length];
						for ( int i = 0; i < fa.length; i++ ) {
							if ( DMIUtil.isMissing(Fa[i])) {
								fa[i] = DMIUtil.MISSING_FLOAT;
							}
							else {
								fa[i] = Fa[i];
							}
						}
					}
					else {
						throw new RuntimeException ( "Don't know how to handle float array - is not float[] or Float[]" );
					}
					for ( int i = 0; i < fa.length; i++ ) {
						if ( i > 0 ) {
							b.append(",");
						}
						if ( !DMIUtil.isMissing(fa[i]) ) {
							// Need to get the TableField format because the overall column will be string
							// TODO SAM 2015-09-06
							//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
							b.append(StringUtil.formatString(fa[i],"%.6f"));
						}
					}
					break;
				case TableField.DATA_TYPE_INT:
					oa = __dataTable.getFieldValue(row,col);
					int [] ia = new int[0];
					if ( oa == null ) {
						return null;
					}
					else if ( oa instanceof int[] ) {
						ia = (int [])oa;
					}
					else if ( oa instanceof Integer[] ) {
						Integer [] Ia = (Integer [])oa;
						ia = new int[Ia.length];
						for ( int i = 0; i < ia.length; i++ ) {
							if ( DMIUtil.isMissing(Ia[i])) {
								ia[i] = DMIUtil.MISSING_INT;
							}
							else {
								ia[i] = Ia[i];
							}
						}
					}
					else {
						throw new RuntimeException ( "Don't know how to handle integer array - is not int[] or Integer[]" );
					}
					for ( int i = 0; i < ia.length; i++ ) {
						if ( i > 0 ) {
							b.append(",");
						}
						if ( !DMIUtil.isMissing(ia[i]) ) {
							// Need to get the TableField format because the overall column will be string
							// TODO SAM 2015-09-06
							//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
							b.append(ia[i]);
						}
					}
					break;
				case TableField.DATA_TYPE_LONG:
					oa = __dataTable.getFieldValue(row,col);
					long [] la = new long[0];
					if ( oa == null ) {
						return null;
					}
					else if ( oa instanceof long[] ) {
						la = (long [])oa;
					}
					else if ( oa instanceof Long[] ) {
						Long [] La = (Long [])oa;
						la = new long[La.length];
						for ( int i = 0; i < la.length; i++ ) {
							if ( DMIUtil.isMissing(La[i])) {
								la[i] = DMIUtil.MISSING_LONG;
							}
							else {
								la[i] = La[i];
							}
						}
					}
					else {
						throw new RuntimeException ( "Don't know how to handle long array - is not long[] or Long[]" );
					}
					for ( int i = 0; i < la.length; i++ ) {
						if ( i > 0 ) {
							b.append(",");
						}
						if ( !DMIUtil.isMissing(la[i]) ) {
							// Need to get the TableField format because the overall column will be string
							// TODO SAM 2015-09-06
							//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
							b.append(la[i]);
						}
					}
					break;
				case TableField.DATA_TYPE_BOOLEAN:
					oa = __dataTable.getFieldValue(row,col);
					Boolean [] Ba = new Boolean[0]; // Use Boolean object array because boolean can't indicate null value
					if ( oa == null ) {
						return null;
					}
					else if ( oa instanceof Boolean[] ) {
						Ba = (Boolean [])oa;
					}
					else if ( oa instanceof boolean[] ) {
						boolean [] ba= (boolean [])oa;
						Ba = new Boolean[ba.length];
						for ( int i = 0; i < ba.length; i++ ) {
							// No need to check for missing
							Ba[i] = ba[i];
						}
					}
					else {
						throw new RuntimeException ( "Don't know how to handle boolean array - is not boolean[] or Boolean[]" );
					}
					for ( int i = 0; i < Ba.length; i++ ) {
						if ( i > 0 ) {
							b.append(",");
						}
						if ( !DMIUtil.isMissing(Ba[i]) ) {
							// Need to get the TableField format because the overall column will be string
							// TODO SAM 2015-09-06
							//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
							b.append(Ba[i]);
						}
					}
					break;
				case TableField.DATA_TYPE_STRING:
					String [] sa = (String [])__dataTable.getFieldValue(row,col);
					if ( sa == null ) {
						return null;
					}
					for ( int i = 0; i < sa.length; i++ ) {
						if ( i > 0 ) {
							b.append(",");
						}
						if ( sa[i] != null ) {
							// Need to get the TableField format because the overall column will be string
							// TODO SAM 2015-09-06
							//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
							// TODO SAM 2015-11-05 Need to decide if strings in array should be quoted
							b.append(sa[i]);
						}
					}
					break;
				default:
					// Don't know the type so don't know how to format the array. Just leave blank
					break;
			}
			b.append("]");
			return b.toString();
		}
		else {
			return __dataTable.getFieldValue(row, col);
		}
	}
	catch (Exception e) {
		Message.printWarning(3, "getValueAt", "Error processing column \"" + getColumnName(col) + "\"");
		Message.printWarning(3, "getValueAt", e);
		return "";
	}
}

/**
Returns whether the cell at the given position is editable or not.  In this
table model all columns above #2 are editable.
@param rowIndex unused
@param columnIndex the index of the column to check for whether it is editable.
@return whether the cell at the given position is editable.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
}

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value.
@param col the col of the cell for which to set the value.
*/
public void setValueAt(Object value, int row, int col) {
	super.setValueAt(value, row, col);
}

}