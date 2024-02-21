// DataTableHtmlWriter - write a table to a simple HTML file

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import RTi.Util.IO.HTMLWriter;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Write a table to a simple HTML file.
*/
public class DataTableHtmlWriter
{

/**
The first table to be compared.
*/
private DataTable __table = null;

/**
Create the instance and check for initialization problems.
@param table the data table to be written
*/
public DataTableHtmlWriter ( DataTable table ) {
    // The tables being compared must not be null.
    if ( table == null ) {
        throw new InvalidParameterException( "The  table to write is null." );
    }
    else {
        setTable ( table );
    }
}

/**
Return the table being written.
@return the table being written
*/
private DataTable getTable () {
    return this.__table;
}

/**
Set the table being written.
@param table table being written
*/
private void setTable ( DataTable table ) {
    this.__table = table;
}

/**
Formats a table to an HTML string.  This currently is used only by writeHtmlFile().
@param writeHeader If true, the field names will be read from the fields and written as a one-line header of field names.
If all headers are missing, then the header line will not be written.
@param htmlTitle HTML title, will be shown in browser tab.
@param comments a list of Strings to put at the top of the file as comments.
@param styleMaskArray an array with integer values for each cell in the table.
These values will be used to look up styles from the "stylesMap" hash map.
For example a styleMaskArray value of "1" would match the style in stylesMap.get(1),
which would be the style name used in "customStyleText" text passed in (e.g, ".bad { background-color:yellow; }\n".
@param stylesMap a map of styles to use with 'styleMaskArray', as described above
@param customStyleText is text to be inserted in the "<style>" section of the HTML.
It should consist of lines as illustrated above.
@throws Exception if an error occurs.
*/
private String toHtml( boolean writeHeader, String htmlTitle, List<String> comments, int [][] styleMaskArray,
    HashMap<Integer,String> stylesMap, String customStyleText )
    throws Exception {
    String routine = getClass().getSimpleName() + ".toHtml";
    DataTable table = getTable();
    // Create an HTML writer.
    HTMLWriter html = new HTMLWriter( null, htmlTitle, false );
    // Start the file and write the head section.
    html.htmlStart();
    if ( (htmlTitle == null) || htmlTitle.isEmpty() ) {
        htmlTitle = "Data Table";
    }
    writeHtmlHead(html, htmlTitle, customStyleText);
    // Start the body section.
    html.bodyStart();
    // Put the standard header at the top of the file.
    //IOUtil.printCreatorHeader ( out, "#", 80, 0 );
    // If any comments have been passed in, print them at the top of the file.
    /* TODO SAM 2010-12-07 Add comments later - they are messing with formatting.
    if ( comments != null ) {
        html.commentStart();
        html.write( "Comments:\n" );
        for ( String comment: comments ) {
            html.write( html.encodeHTML(comment) + "\n" );
        }
        html.commentEnd();
    }
    */

    int nTableCols = table.getNumberOfFields();
    if (nTableCols == 0) {
        Message.printWarning(3, routine, "Table has 0 columns.  Nothing will be written.");
    }
    else {
        //StringBuffer line = new StringBuffer();

        //int nonBlank = 0; // Number of non-blank table headings.
        html.tableStart();
        if (writeHeader) {
            // First determine if any headers are non-blank.
            /*
            for (int col = 0; col < cols; col++) {
                if ( getFieldName(col).length() > 0 ) {
                    ++nonBlank;
                }
            }
            if ( nonBlank > 0 ) {
                out.println ( "# Column headings are first line below, followed by data lines.");
                line.setLength(0);
                for (int col = 0; col < (cols - 1); col++) {
                    line.append( "\"" + getFieldName(col) + "\"" + delimiter);
                }
                line.append( "\"" + getFieldName((cols - 1)) + "\"");
                out.println(line);
            }
            */
            html.tableRowStart();
            String [] tableHeaders = new String[nTableCols];
            for ( int iCol = 0; iCol < nTableCols; iCol++ ) {
                tableHeaders[iCol] = table.getFieldName(iCol);
            }
            html.tableHeaders( tableHeaders );
            html.tableRowEnd();
        }

        String cellString;
        int tableFieldType;
        int precision;
        PropList maskProps = new PropList(""); // Reused below to stylize cells.
        Object cellObject;
        
        int nTableRows = table.getNumberOfRecords();
        int nMaskRows = 0;
        int nMaskCols = 0;
        if ( styleMaskArray != null ) {
        	nMaskRows = styleMaskArray.length;
        	nMaskCols = styleMaskArray[0].length;
        	//Message.printStatus(2, routine, "Table has " + nTableRows + " rows and " + nTableCols + " columns.");
        	//Message.printStatus(2, routine, "Style mask array has " + nMaskRows + " rows and " + nMaskCols + " columns.");
        }
        else {
        	//Message.printStatus(2, routine, "Table style mask array is null.");
        }

        // Cell data when using styling.
        String [] cellAsArray = new String[1];
        // Class to use for table cell styling.
        String className = null;
        for ( int iRow = 0; iRow < nTableRows; iRow++) {
            html.tableRowStart();
            for ( int iCol = 0; iCol < nTableCols; iCol++) {
                tableFieldType = table.getFieldDataType(iCol);
                precision = table.getFieldPrecision(iCol);
                cellObject = table.getFieldValue(iRow,iCol);
                // TODO SAM 2010-12-18 Why not get the format from the table?
                if ( cellObject == null ) {
                    cellString = "";
                }
                else if ( ((tableFieldType == TableField.DATA_TYPE_FLOAT) ||
                    (tableFieldType == TableField.DATA_TYPE_DOUBLE)) && (precision > 0) ) {
                    // Format according to the precision if floating point.
                    cellString = StringUtil.formatString(cellObject,"%." + precision + "f");
                }
                else {
                    // Use default formatting.
                    cellString = "" + cellObject;
                }
                className = null;
                if ( (styleMaskArray != null) && (stylesMap != null) ) {
                	if ( (iRow < nMaskRows) && (iCol < nMaskCols) ) {
                		// Table row and column are within the mask row and column limits.
                		className = stylesMap.get(styleMaskArray[iRow][iCol]);
                		//Message.printStatus(2, routine, "string=\"" + cellString + "\" mask=" + styleMaskArray[iRow][iCol] +
                		//		" className=\"" + className + "\"" );
                		if ( className == null ) {
                			// Null will result in no styling.
                		}
                		else if ( className.isEmpty() ) {
                			// No class for the cell:
                			// - set to null to handle below
                			className = null;
                		}
                		else {
                			// Class is used to style the cell.
                			maskProps.set("class",className);
                		}
                	}
                }
                if ( className != null ) {
                    // Cell matches a mask so use the property to set the style.
                    cellAsArray[0] = cellString;
                    html.tableCells ( cellAsArray, maskProps );
                }
                else {
                    // No special style/formatting for cell.
                    html.tableCell ( cellString );
                }
            }
            html.tableRowEnd();
        }
        html.tableEnd();
    }
    html.bodyEnd();
    html.htmlEnd();
    html.closeFile();
    return html.getHTML();
}

/**
Writes a table to an HTML file.
@param filename the file to write to.
@param writeHeader If true, the field names will be read from the fields and written as a one-line header of field names.
If all headers are missing, then the header line will not be written.
@param comments a list of Strings to put at the top of the file as comments.
@param styleMaskArray an array with integer values for each cell in the table.
These values will be used to look up styles from the "styles" array.
For example a styleMaskArray value of "1" would match the style in styles[1],
which would be the style name used in "customStyleText" text passed in (e.g, ".bad { background-color:yellow; }\n".
This capability is being evaluated for effectiveness.
@param styles an array of styles to use with 'styleMaskArray', as described above
@param customStyleText is text to be inserted in the "<style>" section of the HTML.
It should consist of lines as illustrated above.
@throws Exception if an error occurs.
*/
public void writeHtmlFile(String filename, boolean writeHeader, List<String> comments, int [][] styleMaskArray,
    String [] styles, String customStyleText )
    throws IOException, Exception {
	// Create a dictionary for the styles, which is more flexible than an array with fixed index positions.
	HashMap<Integer,String> stylesMap = new LinkedHashMap<>();
	for ( int i = 0; i < styles.length; i++ ) {
		stylesMap.put ( Integer.valueOf((i + 1)), styles[i] );
	}
	writeHtmlFile(filename, writeHeader, comments, styleMaskArray, stylesMap, customStyleText );
}

/**
Writes a table to an HTML file.
@param filename the file to write to.
@param writeHeader If true, the field names will be read from the fields and written as a one-line header of field names.
If all headers are missing, then the header line will not be written.
@param comments a list of Strings to put at the top of the file as comments.
@param styleMaskArray an array with integer values for each cell in the table.
These values will be used to look up styles from the "stylesMap" hash map.
For example a styleMaskArray value of "1" would match the style in stylesMap.get(1),
which would be the style name used in "customStyleText" text passed in (e.g, ".bad { background-color:yellow; }\n".
@param stylesMap a map of styles to use with 'styleMaskArray', as described above
@param customStyleText is text to be inserted in the "<style>" section of the HTML.
It should consist of lines as illustrated above.
@throws Exception if an error occurs.
*/
public void writeHtmlFile ( String filename, boolean writeHeader, List<String> comments, int [][] styleMaskArray,
    HashMap<Integer,String> stylesMap, String customStyleText )
throws IOException, Exception {
	String title = "";
	writeHtmlFile(filename, title, writeHeader, comments, styleMaskArray, stylesMap, customStyleText );
}

/**
Writes a table to an HTML file.
@param filename the file to write to.
@param title title for the HTML, will be shown in browser tab.
@param writeHeader If true, the field names will be read from the fields and written as a one-line header of field names.
If all headers are missing, then the header line will not be written.
@param comments a list of Strings to put at the top of the file as comments.
@param styleMaskArray an array with integer values for each cell in the table.
These values will be used to look up styles from the "stylesMap" hash map.
For example a styleMaskArray value of "1" would match the style in stylesMap.get(1),
which would be the style name used in "customStyleText" text passed in (e.g, ".bad { background-color:yellow; }\n".
@param stylesMap a map of styles to use with 'styleMaskArray', as described above
@param customStyleText is text to be inserted in the "<style>" section of the HTML.
It should consist of lines as illustrated above.
@throws Exception if an error occurs.
*/
public void writeHtmlFile ( String filename, String title, boolean writeHeader, List<String> comments, int [][] styleMaskArray,
    HashMap<Integer,String> stylesMap, String customStyleText )
throws IOException, Exception {
    String routine = getClass().getSimpleName() + ".writeHtmlFile";

    if ( filename == null ) {
        Message.printWarning ( 2, routine, "Cannot write data table to null HTML file.");
        throw new InvalidParameterException("Cannot write data table to null HTML file.");
    }

    PrintWriter out = null;
    try {
        out = new PrintWriter( new BufferedWriter(new FileWriter(filename)));
        out.print( toHtml(writeHeader, title, comments, styleMaskArray, stylesMap, customStyleText ) );
    }
    finally {
        if ( out != null ) {
            out.close();
        }
    }
}

/**
Writes the start tags for the HTML check file.
@param html HTMLWriter object used to write the HTML file.
@param title title for the document.
@param customStyleText custom style text to write, for example for class styles
@throws Exception
*/
private void writeHtmlHead ( HTMLWriter html, String title, String customStyleText ) throws Exception {
    if ( html != null ) {
        html.headStart();
        html.title(title);
        writeHtmlStyles(html, customStyleText);
        html.headEnd();
    }
}

/**
Inserts the style attributes for a table.
This was copied from the TSHtmlFormatter since tables are used with time series also.
@param html HTMLWriter instance used to write the file
@param customStyleText custom style text, for example containing class properties
@throws Exception
*/
private void writeHtmlStyles ( HTMLWriter html, String customStyleText )
throws Exception {
    html.write("<style>\n"
        + "@media screen {\n"
        + "#titles {\n"
        + "  font-weight: bold;\n"
        + "  color: #303044\n"
        + "}\n"
        + "table {\n"
        + "  background-color: black;\n"
        + "  text-align: left;\n"
        + "  border: 1;\n"
        + "  bordercolor: black;\n"
        + "  cellspacing: 1;\n"
        + "  cellpadding: 1\n"
        + "}\n"
        + "th {\n"
        + "  background-color: #333366;\n"
        + "  text-align: center;\n"
        + "  vertical-align: bottom;\n"
        + "  color: white\n"
        + "}\n"
        + "tr {\n"
        + "  valign: bottom;\n"
        + "  halign: right\n"
        + "}\n"
        + "td {\n"
        + "  background-color: white;\n"
        + "  text-align: right;\n"
        + "  vertical-align: bottom;\n"
        + "  font-style: normal;\n"
        + "  font-family: courier;\n"
        + "  font-size: .75em\n"
        + "}\n"
        + "body {\n"
        + "  text-align: left;\n"
        + "  font-size: 12pt;\n"
        + "}\n"
        + "pre {\n"
        + "  font-size: 12pt;\n"
        + "  margin: 0px\n"
        + "}\n"
        + "p {\n"
        + "  font-size: 12pt;\n"
        + "}\n"
        + "/* The following controls formatting of data values in tables. */\n"
        + ".flagcell {"
        + "  background-color: lightgray;"
        + "}\n"
        + ".missing {\n"
        + "  background-color: yellow;\n"
        + "}\n"
        + ".flag {\n"
        + "  vertical-align: super;\n"
        + "}\n"
        + ".flagnote {\n"
        + "  font-style: normal;\n"
        + "  font-family: courier;\n"
        + "  font-size: .75em;\n"
        + "}\n" );
    if ( (customStyleText != null) && !customStyleText.isEmpty() ) {
        html.write ( customStyleText );
    }
    html.write (
        "}\n"
        + "@media print {\n"
        + "#titles {\n"
        + "  font-weight: bold;\n"
        + "  color: #303044\n"
        + "}\n"
        + "table {\n"
        + "  border-collapse: collapse;\n"
        + "  background-color: white;\n"
        + "  text-align: left;\n"
        + "  border: 1pt solid #000000;\n"
        + "  cellspacing: 2pt;\n"
        + "  cellpadding: 2pt\n"
        + "}\n"
        + "th {\n"
        + "  background-color: white;\n"
        + "  text-align: center;\n"
        + "  vertical-align: bottom;\n"
        + "  color: black\n"
        + "}\n"
        + "tr {\n"
        + "  valign: bottom;\n"
        + "  halign: right;\n"
        + "}\n"
        + "td {\n"
        + "  background-color: white;\n"
        + "  border: 1pt solid #000000;\n"
        + "  text-align: right;\n"
        + "  vertical-align: bottom;\n"
        + "  font-style: normal;\n"
        + "  font-family: courier;\n"
        + "  font-size: 11pt;\n"
        + "  padding: 2pt;\n"
        + "}\n"
        + "body {\n"
        + "  text-align: left;\n"
        + "  font-size: 11pt;\n"
        + "}\n"
        + "pre {\n"
        + "  font-size: 11pt;\n"
        + "  margin: 0px\n"
        + "}\n"
        + "p {\n"
        + "  font-size: 11pt;\n"
        + "}\n"
        + "/* The following controls formatting of data values in tables. */\n"
        + ".flagcell {\n"
        + "  background-color: lightgray;\n"
        + "}\n"
        + ".missing {\n"
        + "  background-color: yellow;\n"
        + "}\n"
        + ".flag {\n"
        + "  vertical-align: super;\n"
        + "}\n"
        + ".flagnote {\n"
        + "  font-style: normal;\n"
        + "  font-family: courier;\n"
        + "  font-size: 11pt;\n"
        + "}\n" );
    if ( (customStyleText != null) && !customStyleText.isEmpty() ) {
        html.write ( customStyleText );
    }
    html.write (
        "}\n"
        + "}\n"
        + "</style>\n");
}

}