package RTi.Util.Table;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
This class will process an SQL ResultSet into a DataTable.
*/
public class ResultSetToDataTableFactory
{
    
/**
Constructor.
*/
public ResultSetToDataTableFactory ()
{
    
}

/**
Create a DataTable from a ResultSet.
@param rs the ResultSet from an SQL query
@param tableID the identifier to use for the table
*/
public DataTable createDataTable ( ResultSet rs, String tableID )
throws SQLException
{   String routine = getClass().getName() + ".createDataTable";
    DataTable table = new DataTable();
    table.setTableID ( tableID );
    // Define the table columns from the ResultSet metadata
    ResultSetMetaData meta = rs.getMetaData();
    int columnCount = meta.getColumnCount();
    String columnName;
    int columnType, precision, scale;
    int [] columnTypes = new int[columnCount];
    for ( int i = 1; i <= columnCount; i++ ) {
        columnName = meta.getColumnName(i);
        columnType = sqlToDMIColumnType(meta.getColumnType(i));
        columnTypes[i - 1] = columnType;
        precision = meta.getPrecision(i); // More like width
        scale = meta.getScale(i); // Precision for floating point
        //Message.printStatus(2,routine,"Column name=\"" + columnName + "\", sqlColumnType=" + meta.getColumnType(i) +
        //    ", tableColumnType=" + columnType +
        //    ", SQL precision (table width)=" + precision + ", SQL scale (table precision)=" + scale +
        //    ", displayWidth=" + meta.getColumnDisplaySize(i));
        // TODO SAM 2012-06-12
        // SQL Server behaves oddly in that the "scale" can be set to 0 (zero) but SQL Server Management Studio
        // still shows digits after the decimal point.  None of the properties on a column appear to be usable
        // to determine the number of digits to display.  To compensate, for now set the scale to 6 if
        // floating point and not specified
        if ( ((columnType == TableField.DATA_TYPE_DOUBLE) || (columnType == TableField.DATA_TYPE_FLOAT)) &&
            (scale == 0) ) {
            scale = 6;
        }
        table.addField( new TableField(columnType,columnName,precision,scale), null);
    }
    // Transfer each record in the ResultSet to the table
    String s;
    double d;
    float f;
    int i;
    Date date;
    TableRecord rec = null;
    boolean isNull;
    while (rs.next()) {
        rec = new TableRecord(columnCount);
        for ( int iCol = 1; iCol <= columnCount; iCol++ ) {
            int i0 = iCol - 1;
            try {
                isNull = false;
                if ( columnTypes[i0] == TableField.DATA_TYPE_DATE ) {
                    date = rs.getTimestamp(iCol);
                    if (!rs.wasNull()) {
                        rec.addFieldValue(new DateTime(date));
                    }
                    else {
                        isNull = true;
                    }
                }
                else if ( columnTypes[i0] == TableField.DATA_TYPE_DOUBLE ) {
                    d = rs.getDouble(iCol);
                    if (!rs.wasNull()) {
                        rec.addFieldValue(new Double(d));
                    }
                    else {
                        isNull = true;
                    }
                }
                else if ( columnTypes[i0] == TableField.DATA_TYPE_FLOAT ) {
                    f = rs.getFloat(iCol);
                    if (!rs.wasNull()) {
                        rec.addFieldValue(new Float(f));
                    }
                    else {
                        isNull = true;
                    }
                }
                else if ( columnTypes[i0] == TableField.DATA_TYPE_INT ) {
                    i = rs.getInt(iCol);
                    if (!rs.wasNull()) {
                        rec.addFieldValue(new Integer(i));
                    }
                    else {
                        isNull = true;
                    }
                }
                else if ( columnTypes[i0] == TableField.DATA_TYPE_STRING ) {
                    s = rs.getString(iCol);
                    if (!rs.wasNull()) {
                        rec.addFieldValue(s.trim());
                    }
                    else {
                        isNull = true;
                    }
                }
                else {
                    // Default is string
                    s = rs.getString(iCol);
                    if (!rs.wasNull()) {
                        rec.addFieldValue(s.trim());
                    }
                    else {
                        isNull = true;
                    }
                }
                if ( isNull ) {
                    rec.addFieldValue(null);
                }
            }
            catch ( Exception e ) {
                // Leave as null
                Message.printWarning(3,routine,e);
            }
        }
        try {
            table.addRecord(rec);
        }
        catch ( Exception e ) {
            // Should not happen
            Message.printWarning(3,routine,e);
        }
    }
    return table;
}

/**
Convert an SQL column type to the DataTable type.
*/
private int sqlToDMIColumnType(int sqlColumnType)
{
    switch ( sqlColumnType ) {
        case Types.BOOLEAN: return TableField.DATA_TYPE_INT;
        case Types.CHAR: return TableField.DATA_TYPE_STRING;
        case Types.DATE: return TableField.DATA_TYPE_DATE;
        case Types.DECIMAL: return TableField.DATA_TYPE_DOUBLE;
        case Types.DOUBLE: return TableField.DATA_TYPE_DOUBLE;
        case Types.FLOAT: return TableField.DATA_TYPE_FLOAT;
        case Types.INTEGER: return TableField.DATA_TYPE_INT;
        case Types.SMALLINT: return TableField.DATA_TYPE_INT;
        case Types.TIME: return TableField.DATA_TYPE_DATE;
        case Types.TIMESTAMP: return TableField.DATA_TYPE_DATE;
        case Types.TINYINT: return TableField.DATA_TYPE_INT;
        case Types.VARCHAR: return TableField.DATA_TYPE_STRING;
        default:
            Message.printWarning(2,"","Unknown SQL type for conversion to table: " + sqlColumnType +
                 ", using string.");
            return TableField.DATA_TYPE_STRING;
    }
}

}