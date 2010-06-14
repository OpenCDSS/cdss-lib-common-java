package RTi.TS;

import java.util.List;
import java.util.Vector;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.HTMLWriter;
import RTi.Util.IO.IOUtil;
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
        if ( htmlTitle == null ) {
            htmlTitle = "Time Series Summary";
        }
        writeHtmlHead(html,htmlTitle);
        // Start the body section
        html.bodyStart();
        // Write introduction information
        String userTitle = null;
        List<String> notes = null;
        writeHtmlIntro( html, userTitle, notes );
        // Write the time series list
        List<TS> tslist = getTSList();
        writeHtmlTimeSeriesListTable ( html, tslist );
        // Write the summary for each time series.
        int count = 0;
        for ( TS ts : tslist ) {
            ++count;
            // Add a horizontal separator before the time series
            html.horizontalRule();
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
            writeHtmlForOneTimeSeries ( html, ts, count, yearType, outputStart, outputEnd, precision );
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
private void writeHtmlForOneMonthTimeSeries ( HTMLWriter html, TS ts, int count, YearType yearType,
    DateTime outputStart, DateTime outputEnd, Integer precision )
throws Exception
{   String routine = getClass().getName() + ".writeHtmlForOneMonthTimeSeries";
    
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
    // Month column headings depend on year type
    int iMonth = yearType.getStartMonth();
    for ( int i = 1; i <= 12; i++ ) {
        tableHeaders[i] = TimeUtil.monthAbbreviation(iMonth);
        ++iMonth;
        if ( iMonth == 13 ) {
            iMonth = 1;
        }
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
            // Need to shift to include the previous year...
            outputStart.addYear ( -1 );
        }
        outputStart.setMonth ( yearType.getStartMonth() );
        if ( outputEnd.getMonth() > yearType.getStartMonth() ) {
            // Need to include the next year...
            outputEnd.addYear ( 1 );
        }
        outputEnd.setMonth ( yearType.getEndMonth() );
        // The year that is printed in the summary is actually
        // later than the calendar for some months...
        yearOffset = 1;
    }
    Message.printStatus ( 2, routine, "Reset output period to full years " + outputStart + " to " + outputEnd );
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
            monthPos = -1; // Will be incremented at the start of the loop to be zero for first month column
        }
    }
    html.tableEnd();

    // Write the data flag notes - loop through flag meta-data that has been defined and then see if
    // any flags have been found.  This generally ensures that flags are written in the order of processing logic (e.g.,
    // fill with one method and then another), rather than the order of flags in the data, which can be rather random
    // if iterating through the period.  If flag meta-data is found, use it.  Otherwise, display a general message.
    if ( (foundFlagsList.size() > 0) || (missingCountTotal > 0) ) {
        // Display the colors used in the table
        if ( foundFlagsList.size() > 0 ) {
            html.heading(3, "Data Flags (alphabetized)" );
        }
        else {
            html.heading(3, "Data Flags" );
        }
        html.tableStart();
        html.tableRowStart();
        html.tableCellStart(propsFlaggedCell);
        html.write("Flagged Value");
        html.tableCellEnd();
        html.tableCellStart(propsMissing);
        html.write("Missing Value");
        html.tableCellEnd();
        html.tableRowEnd();
        html.tableEnd();
    }
    if ( foundFlagsList.size() > 0 ) {
        // Write the data flag notes - loop through flags found in the data and then see if
        // flag meta-data is available from the time series.  If so, use it.  If not display a general message.
        PropList propsFlagNote = new PropList("");
        propsMissing.set("class","flagnote");
        List<String> foundFlagsListSorted = StringUtil.sortStringList(foundFlagsList);
        List<TSDataFlagMetadata> flagMetadataList = ts.getDataFlagMetadataList();
        // Loop through the found flags
        boolean found;
        for ( String foundFlag : foundFlagsListSorted ) {
            // See if any meta-data have been stored with the time series
            found = false;
            for ( TSDataFlagMetadata flagMetadata : flagMetadataList ) {
                if ( flagMetadata.getDataFlag().equals(foundFlag) ) {
                    // Use the found meta-data...
                    html.write( foundFlag + " - " + flagMetadata.getDescription());
                    html.breakLine();
                    found = true;
                    break;
                }
            }
            if ( !found ) {
                // Generic message
                html.span ( foundFlag + " - no information available describing meaning", propsFlagNote );
                html.breakLine();
            }
        }
    }
}

/**
Write the HTML for one time series.
@param ts time series to format as HTML
*/
private void writeHtmlForOneTimeSeries ( HTMLWriter html, TS ts, int count, YearType yearType,
    DateTime outputStart, DateTime outputEnd, Integer precision )
throws Exception
{
    // Write the heading for the time series
    
    html.heading (2, "Time series " + ts.getIdentifier() + " (" + ts.getDescription() + ")", "ts" + count);
    
    // Now write the data section
    // Currently only support monthly data
    
    if ( (ts.getDataIntervalBase() == TimeInterval.MONTH) && (ts.getDataIntervalMult() == 1) ) {
        writeHtmlForOneMonthTimeSeries ( html, ts, count, yearType, outputStart, outputEnd, precision );
    }
    else {
        html.write("Time series " + ts.getIdentifier() + " (" + ts.getDescription() +
            ") cannot be formatted - only Month interval is supported." );
    }
    
    // Write the comment section
    
    List<String> comments = ts.getComments();
    if ( (comments != null) && (comments.size() > 0) ) {
        html.heading (3,"Time series comments" );
        html.preStart();
        for ( String comment : comments ) {
            html.write(comment + "\n");
        }
        html.preEnd();
    }
    
    // Write the genesis
    
    List<String> genesisList = ts.getGenesis();
    if ( (genesisList != null) && (genesisList.size() > 0) ) {
        html.heading (3, "Time Series Creation History" );
        html.preStart();
        for ( String genesis : genesisList ) {
            html.write(genesis + "\n");
        }
        html.preEnd();
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
Write the introduction to the report.
@param html HTMLWriter object.
@throws Exception
 */
private void writeHtmlIntro( HTMLWriter html, String userTitle, List<String> notes )
throws Exception
{
    // proplist provides an anchor link for this section used from the table of contents
    //PropList header_prop = new PropList("header");
    //header_prop.add("name=header");
    
    html.heading(1, IOUtil.getProgramName() + " Time Series Summary" );
    
    if ( userTitle != null ) {
        html.heading(2, userTitle );
    }
    
    // Table of contents using same heading and section names as main content...
    
    html.heading(2, "Table of Contents" );
    
    String environmentHeading = "Program/Environment Information";
    String environmentLink = "environment";
    String notesHeading = "Notes";
    String notesLink = "notes";
    String tslistHeading = "Time Series List";
    String tslistLink = "tslist";
    
    html.link("#"+environmentLink, environmentHeading );
    html.write ( " - generated at runtime");
    html.breakLine();
    if ( (notes != null) && (notes.size() > 0) ) {
        html.link("#"+notesLink, notesHeading );
        html.write ( " - as written to output file headers");
    }
    html.breakLine();
    html.link("#"+tslistLink, tslistHeading );
    html.write ( " - list of time series");
    html.breakLine();
    
    // Environment section of the report...
    
    html.heading(2, environmentHeading, environmentLink );
    
    DateTime now = new DateTime(DateTime.DATE_CURRENT);
    String [] tableHeaders = { "Property", "Value" };
    html.paragraphStart();
    html.tableStart();
    html.tableRowStart();
    html.tableHeaders( tableHeaders );
    html.tableRowEnd();
    String [] tds = new String[2];
    tds[0] = "Program";
    tds[1] = IOUtil.getProgramName() + " " + IOUtil.getProgramVersion();
    html.tableRow( tds );
    tds[0] = "User";
    tds[1] = IOUtil.getProgramUser();
    html.tableRow( tds );
    tds[0] = "Creation time";
    tds[1] = "" + now;
    html.tableRow( tds );
    tds[0] = "Computer";
    tds[1] = IOUtil.getProgramHost();
    html.tableRow( tds );
    StringBuffer b = new StringBuffer();
    b.append( IOUtil.getProgramName() );
    String [] args = IOUtil.getProgramArguments();
    for ( int i = 0; i < args.length; i++ ) {
        b.append ( " " + args[i] );
    }
    tds[0] = "Command line";
    tds[1] = b.toString();
    html.tableRow( tds );
    html.tableEnd();
    html.paragraphEnd();
    
    // Notes
    if ( (notes != null) && (notes.size() > 0) ) {
        html.heading(2, notesHeading, notesLink );
        html.preStart();
        for ( String note : notes ) {
            html.write(note + "\n");
        }
        html.preEnd();
    }

    html.heading(2, tslistHeading, tslistLink );
    //htmlWriteProblemCounts ( html );
    //htmlWriteCommands( html, processor.getCommands(), true ); // false to write text, true for table
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
        + "td { background-color:white; text-align:right; vertical-align:bottom; font-style:normal; " +
        		"font-family:courier; font-size:.75em }\n" 
        + "body { text-align:left; font-size:12pt; }\n"
        + "pre { font-size:12pt; margin: 0px }\n"
        + "p { font-size:12pt; }\n"
        + "/* The following controls formatting of data values in tables */\n"
        + ".flagcell { background-color:lightgray; }\n"
        + ".missing { background-color:yellow; }\n"
        + ".flag { vertical-align: super; }\n"
        + ".flagnote { font-style:normal; font-family:courier; font-size:.75em; }\n"
        + "</style>\n");
}

/**
Write HTML table listing all time series, with embedded links to each.
*/
private void writeHtmlTimeSeriesListTable ( HTMLWriter html, List<TS> tslist )
throws Exception
{
    if ( (tslist == null) || (tslist.size() == 0) ) {
        return;
    }
    html.tableStart();
    html.tableRowStart();
    String [] tableHeaders = new String[6];
    tableHeaders[0] = "#";
    tableHeaders[1] = "TSID";
    tableHeaders[2] = "Alias";
    tableHeaders[3] = "Description";
    tableHeaders[4] = "Start";
    tableHeaders[5] = "End";
    html.tableHeaders( tableHeaders );
    html.tableRowEnd();
    int count = 0;
    for ( TS ts : tslist ) {
        ++count;
        html.tableRowStart();
        html.tableCell( "" + count );
        html.tableCellStart();
        html.link ( "#ts" + count, ts.getIdentifierString() );
        html.tableCellEnd();
        html.tableCellStart();
        html.link( "#ts" + count, ts.getAlias());
        html.tableCellEnd();
        html.tableCellStart();
        html.link( "#ts" + count, ts.getDescription() );
        html.tableCellEnd();
        html.tableCell( "" + ts.getDate1() );
        html.tableCell( "" + ts.getDate2() );
        html.tableRowEnd();
    }
    html.tableEnd();
}

}