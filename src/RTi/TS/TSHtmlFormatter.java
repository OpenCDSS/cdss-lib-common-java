package RTi.TS;

import java.util.List;
import java.util.Vector;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.HTMLWriter;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

/**
Format a list of time series into HTML.
The toHtml() method actually does the formatting.
*/
public class TSHtmlFormatter
{
    
/**
List of time series to format.
*/
private List<TS> __tslist = null;
    
/**
Constructor to take a list of time series.
*/
public TSHtmlFormatter ( List<TS> tslist )
{
    setTSList ( tslist );
}

/**
Add a flag to the list of found flags.  This ensures that even time series that have not had flag
metadata added will have a note for each flag (and point out the need to change code to add metadata).
Comparisons are case-sensitive due to the likelihood that upper- and lower-case strings may have specific meaning.
@param foundFlagsList list of flags that have been found for a time series, based on data formatting.
@param flag data flag from data
*/
private void addToFoundFlags ( List<String> foundFlagsList, String flag )
{
    boolean found = false;
    for ( String foundFlag : foundFlagsList ) {
        if ( foundFlag.equals(flag) ) {
            // Found in list so no need to add again
            found = true;
            break;
        }
    }
    if ( !found ) {
        // Not previously encountered so add
        foundFlagsList.add ( flag );
    }
}

/**
Get the list of time series being processed.
*/
private List<TS> getTSList ()
{
    return __tslist;
}

/**
Set the list of time series being processed.
*/
private void setTSList ( List<TS> tslist )
{
    __tslist = tslist;
}

/**
Format a list of time series as HTML.
@param htmlTitle the title for the HTML output.
@param yearType the type of year for output.
@param outputStart start for output (will be rounded to full year).
@param outputEnd end for output (will be rounded to full year).
@param precision number of digits after the decimal (default is to determine from time series data units).
@return an HTML string that can be displayed or written to a file.
*/
public String toHTML ( String htmlTitle, YearType yearType, DateTime outputStart, DateTime outputEnd, Integer precision )
throws Exception
{
    // Write the HTML file
    
    try {
        // Create an HTML writer
        HTMLWriter html = new HTMLWriter( null, htmlTitle, false );
        // Start the file and write the head section
        html.htmlStart();
        writeHtmlHead(html,htmlTitle);
        // Start the body section
        html.bodyStart();
        // Write introduction information
        //htmlWriteCheckFileIntro( html, userTitle, processor, newComments2 );
        int count = 0;
        for ( TS ts : getTSList() ) {
            ++count;
            if ( count > 1 ) {
                // Add a horizontal separator
                html.horizontalRule();
            }
            // Determine the precision based on data units
            if ( precision == null ) {
                // Try to get units information for default...
                try {
                    DataUnits u = DataUnits.lookupUnits ( ts.getDataUnits() );
                    precision = new Integer(u.getOutputPrecision());
                }
                catch ( Exception e ) {
                    // Default...
                    precision = new Integer(2);
                }
            }
            writeHtmlForOneTimeSeries ( html, ts, yearType, outputStart, outputEnd, precision );
        }
        // Close the body section and file
        html.bodyEnd();
        html.htmlEnd();
        html.closeFile();
        return html.getHTML();
    }
    finally {
    }    
}

/**
Write the HTML for one month-interval time series.
@param ts monthly time series to format as HTML.
*/
private void writeHtmlForOneMonthTimeSeries ( HTMLWriter html, TS ts, YearType yearType,
    DateTime outputStart, DateTime outputEnd, Integer precision )
throws Exception
{
    html.heading (2,"Time series " + ts.getIdentifier() + " (" + ts.getDescription() + ")" );
    
    // Make a local copy of the output start/end
    
    if ( outputStart == null ) {
        outputStart = new DateTime(ts.getDate1());
    }
    else {
        outputStart = new DateTime ( outputStart );
    }
    if ( outputEnd == null ) {
        outputEnd = new DateTime(ts.getDate2());
    }
    else {
        outputEnd = new DateTime ( outputEnd );
    }
    
    // Write the table headings...
    
    html.tableStart();
    html.tableRowStart();
    String [] tableHeaders = new String[14];
    tableHeaders[0] = "Year";
    String units = ts.getDataUnits();
    if ( units.equalsIgnoreCase("AF") ||
        units.equalsIgnoreCase("ACFT") ||
        units.equalsIgnoreCase("FT") ||
        units.equalsIgnoreCase("FEET") ||
        units.equalsIgnoreCase("FOOT") ||
        units.equalsIgnoreCase("IN") ||
        units.equalsIgnoreCase("INCH") ) {
        // FIXME SAM 2009-02-25 Need to remove hard-coded units here
        // Assume totals...
        tableHeaders[13] = "Total";
    }
    else {
        // Assume averages...
        tableHeaders[13] = "Average";
    }
    // Other table headings depend on year type
    for ( int i = 0; i < 12; i++ ) {
        tableHeaders[i + 1] = TimeUtil.monthAbbreviation(i + 1);
    }
    html.tableHeaders( tableHeaders );
    html.tableRowEnd();
    // Make sure that the iterator processes full rows...
    int yearOffset = 0;
    if ( yearType == YearType.CALENDAR ) {
        // Just need to output for the full year...
        outputStart.setMonth ( 1 );
        outputEnd.setMonth ( 12 );
    }
    else {
        // Need to adjust for years with offsets
        if ( outputStart.getMonth() < yearType.getStartMonth() ) {
            // Need to shift to include the previous irrigation year...
            outputStart.addYear ( -1 );
        }
        outputStart.setMonth ( yearType.getStartMonth() );
        if ( outputEnd.getMonth() > yearType.getStartMonth() ) {
            // Need to include the next irrigation year...
            outputEnd.addYear ( 1 );
        }
        outputEnd.setMonth ( yearType.getEndMonth() );
        // The year that is printed in the summary is actually
        // later than the calendar for some months...
        yearOffset = 1;
    }
    DateTime date = new DateTime(outputStart,DateTime.DATE_FAST);
    TSData data;
    int monthPos = -1; // use 0 as reference (0-11) for months
    int year;
    double value;
    String flag = null;
    double yearTotal = ts.getMissing();
    int nonMissingInRow = 0;
    int missingCountTotal = 0;
    int flagCountTotal = 0;
    String dataFormat = "%." + precision + "f";
    String [] td = new String[1]; // Single cell value
    PropList propsMissing = new PropList("");
    propsMissing.set("class","missing");
    PropList propsFlaggedCell = new PropList("");
    propsFlaggedCell.set("class","flagcell");
    PropList propsFlag = new PropList("");
    propsFlag.set("class","flag");
    List<String> foundFlagsList = new Vector(); // Flags found from the data
    for ( ; date.lessThanOrEqualTo(outputEnd); date.addInterval(TimeInterval.MONTH,1) ) {
        ++monthPos;
        if ( monthPos == 0 ) {
            // Output the year
            year = date.getYear() + yearOffset;
            html.tableRowStart();
            html.tableCell( "" + year );
        }
        data = ts.getDataPoint ( date );
        value = data.getData();
        flag = data.getDataFlag();
        if ( flag != null ) {
            flag = flag.trim();
            if ( flag.length() > 0 ) {
                addToFoundFlags ( foundFlagsList, flag );
            }
        }
        if ( ts.isDataMissing(value) ) {
            td[0] = "";
            html.tableCells(td,propsMissing);
            ++missingCountTotal;
        }
        else {
            // Not missing
            if ( (flag == null) || (flag.length() == 0) ) {
                // Just display the value with no special formatting
                html.tableCell(StringUtil.formatString(value,dataFormat));
            }
            else {
                // Color the cell to indicate flagged value
                html.tableCellStart(propsFlaggedCell);
                html.write("" + StringUtil.formatString(value,dataFormat));
                html.span(flag,propsFlag);
                html.tableCellEnd();
                ++flagCountTotal;
            }
            // Process total/average
            if ( ts.isDataMissing(yearTotal) ) {
                yearTotal = 0.0;
            }
            yearTotal += value;
            ++nonMissingInRow;
        }

        if ( monthPos == 11 ) {
            // Have processed the last month in the year so process the total or average.  We have been
            // adding to the total, so divide by the number of non-missing for the year if averaging...
            // Now reset the year-value to zero...
            if ( ts.isDataMissing(yearTotal) || (nonMissingInRow != 12) ) {
                td[0] = "";
                html.tableCells(td, propsMissing);
                ++missingCountTotal;
            }
            else {
                if ( tableHeaders[13].equals("Total") ) {
                    html.tableCell(StringUtil.formatString(yearTotal, dataFormat) );
                }
                else {
                    html.tableCell (StringUtil.formatString(yearTotal/(double)nonMissingInRow,dataFormat) );
                }
            }
            html.tableRowEnd();
            yearTotal = ts.getMissing();
            nonMissingInRow = 0;
            monthPos = -1; // Will be incremented at the start of the loop
        }
    }
    html.tableEnd();

    // Write the data flag notes - loop through flags found in the data and then see if
    // flag metadata is available from the time series.  If so, use it.  If not display a general message.

    List<String> foundFlagsListSorted = StringUtil.sortStringList(foundFlagsList);
    if ( foundFlagsListSorted.size() > 0 ) {
        html.heading(2, "Data Flags" );
        List<TSDataFlagMetadata> flagMetadataList = ts.getDataFlagMetadataList();
        // Loop through the found flags
        boolean found;
        for ( String foundFlag : foundFlagsListSorted ) {
            // See if any metadata have been stored with the time series
            found = false;
            for ( TSDataFlagMetadata flagMetadata : flagMetadataList ) {
                if ( flagMetadata.getDataFlag().equals(foundFlag) ) {
                    // Use the found metadata...
                    html.write( foundFlag + " - " + flagMetadata.getDescription());
                    html.breakLine();
                    found = true;
                    break;
                }
            }
            if ( !found ) {
                // Generic message
                html.write ( foundFlag + " - no information available describing meaning" );
                html.breakLine();
            }
        }
    }
}

/**
Write the HTML for one time series.
@param ts time series to format as HTML
*/
private void writeHtmlForOneTimeSeries ( HTMLWriter html, TS ts, YearType yearType,
    DateTime outputStart, DateTime outputEnd, Integer precision )
throws Exception
{
    // Currently only support monthly data
    
    if ( (ts.getDataIntervalBase() == TimeInterval.MONTH) && (ts.getDataIntervalMult() == 1) ) {
        writeHtmlForOneMonthTimeSeries ( html, ts, yearType, outputStart, outputEnd, precision );
    }
    else {
        html.headerStart(2);
        html.write("Time series " + ts.getIdentifier() + " (" + ts.getDescription() +
            ") cannot be formatted - only Month interval is supported." );
        html.headerEnd(2);
        return;
    }
}

/**
Writes the start tags for the HTML check file.
@param html HTMLWriter object.
@param title title for the document.
@throws Exception
*/
private void writeHtmlHead( HTMLWriter html, String title ) throws Exception
{
    if ( html != null ) {
        html.headStart();
        html.title(title);
        writeHtmlStyles(html);
        html.headEnd();
    }
}

/**
Inserts the style attributes for a time series summary.
@throws Exception
 */
private void writeHtmlStyles(HTMLWriter html)
throws Exception
{
    html.write("<style>\n"
        + "#titles { font-weight:bold; color:#303044 }\n"
        + "table { background-color:black; text-align:left; border:1; bordercolor:black; cellspacing:1; cellpadding:1 }\n"  
        + "th { background-color:#333366; text-align:center; vertical-align:bottom; color:white }\n"
        + "tr { valign:bottom; halign:right }\n"
        + "td { background-color:white; text-align:right; vertical-align:bottom; }\n" 
        + "body { text-align:left; font-size:12pt; }\n"
        + "pre { font-size:12pt; margin: 0px }\n"
        + "p { font-size:12pt; }\n"
        + "/* The following controls formatting of data values in tables */\n"
        + ".flagcell { background-color:lightgray; }\n"
        + ".missing { background-color:yellow; }\n"
        + ".flag { vertical-align: super; }\n"
        + "</style>\n");
}

}