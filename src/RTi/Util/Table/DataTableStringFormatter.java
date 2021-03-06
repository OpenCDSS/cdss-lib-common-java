// DataTableStringFormatter - format table columns to create an output column string

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

package RTi.Util.Table;

import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Format table columns to create an output column string.
*/
public class DataTableStringFormatter
{
    
/**
Data table on which to perform math.
*/
private DataTable __table = null;

/**
Construct an instance using the table to operate on.
*/
public DataTableStringFormatter ( DataTable table )
{
    __table = table;
}

/**
Format a string
@param inputColumns the name of the first column to use as input
@param format the operator to execute for processing data
@param inputColumn2 the name of the second column to use as input (if input2 is not specified), or null if not used
@param inputValue2 the constant input to use as input (if inputColumn2 is not specified), or null if not used
@param outputColumn the name of the output column
@param insertBeforeColumn the name of the column before which to insert the new column
@param problems a list of strings indicating problems during processing
*/
public void format ( String [] inputColumns, String format, String outputColumn, String insertBeforeColumn, List<String> problems )
{   String routine = getClass().getName() + ".format" ;
    // Look up the columns for input and output (do input last since new column may be inserted)
    int insertBeforeColumnNum = -1;
    if ( (insertBeforeColumn != null) && !insertBeforeColumn.equals("") ) {
        try {
            insertBeforeColumnNum = __table.getFieldIndex(insertBeforeColumn);
        }
        catch ( Exception e ) {
            problems.add ( "Insert before column \"" + insertBeforeColumn + "\" not found in table \"" + __table.getTableID() + "\"" );
        }
    }
    int outputColumnNum = -1;
    try {
        outputColumnNum = __table.getFieldIndex(outputColumn);
    }
    catch ( Exception e ) {
        Message.printStatus(2, routine, "Output column \"" + outputColumn + "\" not found in table \"" +
            __table.getTableID() + "\" - automatically adding." );
        __table.addField(insertBeforeColumnNum, new TableField(TableField.DATA_TYPE_STRING,outputColumn,-1,-1), null );
        try {
            outputColumnNum = __table.getFieldIndex(outputColumn);
        }
        catch ( Exception e2 ) {
            // Should not happen.
            problems.add ( "Output column \"" + outputColumn + "\" not found in table \"" + __table.getTableID() + "\".  Error adding column." );
        }
    }
    int [] inputColumnNum = new int[inputColumns.length];
    for ( int i = 0; i < inputColumns.length; i++ ) {
        inputColumnNum[i] = -1;
        try {
            inputColumnNum[i] = __table.getFieldIndex(inputColumns[i]);
        }
        catch ( Exception e ) {
            problems.add ( "Input column (1) \"" + inputColumns[i] + "\" not found in table \"" + __table.getTableID() + "\"" );
        }
    }
    
    if ( problems.size() > 0 ) {
        // Return if any problems were detected
        return;
    }

    // Loop through the records, get the input column objects, and format for output
    int nrec = __table.getNumberOfRecords();
    String outputVal = null;
    List<Object> values = new Vector<Object>();
    for ( int irec = 0; irec < nrec; irec++ ) {
        // Get the input values
        values.clear();
        for ( int iCol = 0; iCol < inputColumnNum.length; iCol++ ) {
            if ( inputColumnNum[iCol] < 0 ) {
                // Set the result to null
                values.clear();
                break;
            }
            else {
                try {
                    values.add(__table.getFieldValue(irec, inputColumnNum[iCol]));
                }
                catch ( Exception e ) {
                    problems.add ( "Error getting value for input column (" + e + ")." );
                    values.clear();
                    break;
                }
            }
        }
        if ( inputColumnNum.length != values.size() ) {
            // Don't have the right number of values from the number of specified input columns 
            outputVal = null;
        }
        else {
            //Message.printStatus(2, routine, "format=\"" + format + "\"" );
            //for ( int i = 0; i < values.size(); i++ ) {
            //    Message.printStatus(2, routine, "value=\"" + values.get(i) + "\"" );
            //}
            outputVal = StringUtil.formatString(values,format);
        }
        // Set the value...
        try {
            __table.setFieldValue(irec, outputColumnNum, outputVal );
        }
        catch ( Exception e ) {
            problems.add ( "Error setting value (" + e + ")." );
        }
    }   
}

}
