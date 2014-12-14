package RTi.Util.Table;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Class to read a fixed format file into a data table.
*/
public class FixedFormatFileDataTableReader
{
    
/**
Constructor.
*/
public FixedFormatFileDataTableReader ()
{
}

/**
Read the specified file into a data table.
*/
public DataTable readDataTable ( String filename, String dataFormat, String [] columnNames )
throws IOException
{   String routine = getClass().getSimpleName() + ".readDataTable";
    // Parse the format 
    List<Integer> columnTypes = new ArrayList<Integer>();
    List<Integer> columnWidths = new ArrayList<Integer>();
    StringUtil.fixedReadParseFormat(dataFormat, columnTypes, columnWidths);
    int [] columnTypeArray = new int[columnTypes.size()];
    int [] columnWidthArray = new int[columnWidths.size()];
    // Create a data table with the correct column types
    // TODO SAM 2014-03-30 Need to assign column names intelligently
    DataTable table = new DataTable();
    int columnCount = 0;
    for ( int iCol = 0; iCol < columnTypes.size(); iCol++ ) {
        int columnType = columnTypes.get(iCol);
        columnTypeArray[iCol] = columnType;
        columnWidthArray[iCol] = columnWidths.get(iCol);
        if ( columnType == StringUtil.TYPE_SPACE ) {
            continue;
        }
        ++columnCount;
        String columnName = "Column" + columnCount;
        if ( (columnNames != null) && (columnNames.length > 0) && (columnNames.length >= columnCount) ) {
            columnName = columnNames[columnCount - 1];
        }
        if ( (columnType == StringUtil.TYPE_CHARACTER) || (columnType == StringUtil.TYPE_STRING ) ) {
            table.addField(new TableField(TableField.DATA_TYPE_STRING, columnName, columnWidths.get(iCol), -1), null);
        }
        else if ( columnType == StringUtil.TYPE_FLOAT ) {
            table.addField(new TableField(TableField.DATA_TYPE_FLOAT, columnName, columnWidths.get(iCol), 6), null);
        }
        else if ( columnType == StringUtil.TYPE_DOUBLE ) {
            table.addField(new TableField(TableField.DATA_TYPE_DOUBLE, columnName, columnWidths.get(iCol), 6), null);
        }
        else if ( columnType == StringUtil.TYPE_INTEGER ) {
            table.addField(new TableField(TableField.DATA_TYPE_INT, columnName, columnWidths.get(iCol), -1), null);
        }
    }
    BufferedReader in = null;
    String iline;
    int linecount = 0;
    List<Object> dataList = new ArrayList<Object>(); // Will be reused for each line read
    try {
        in = new BufferedReader ( new FileReader ( filename ));
        Object o;
        while ( (iline = in.readLine()) != null ) {
            ++linecount;
            // check for comments
            if (iline.startsWith("#") || iline.trim().length()==0) {
                continue;
            }
            // The following will only return valid data types ("x" format is used only for spacing and not returned)
            StringUtil.fixedRead ( iline, columnTypeArray, columnWidthArray, dataList );
            //Message.printStatus ( 2, routine, "Fixed read returned " + dataList.size() + " values");
            if ( Message.isDebugOn ) {
                Message.printDebug ( 50, routine, "Fixed read returned " + dataList.size() + " values");
            }
            TableRecord rec = new TableRecord();
            for ( int iCol = 0; iCol < dataList.size(); iCol++ ) {
                o = dataList.get(iCol);
                if ( (o != null) && (o instanceof String) ) {
                    rec.addFieldValue(((String)o).trim());
                }
                else {
                    rec.addFieldValue(o);
                }
            }
            table.addRecord(rec);
        }
    }
    catch (Exception e) {
        Message.printWarning ( 3, routine, "Error reading \"" + filename + "\" at line " + linecount );
        throw new IOException(e);
    }
    finally {
        if ( in != null ) {
            in.close();
        }
    }
    return table;
}

}