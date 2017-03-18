/******************************************************************************
* File: CheckFile.java
* Author: KAT
* Date: 2007-03-15
* Stores all information for a CheckFile.  A check file is an html file that
* has information on data checks for various components.
*******************************************************************************
* Revisions 
*******************************************************************************
* 2007-03-15	Kurt Tometich	Initial version.
******************************************************************************/
package RTi.Util.IO;

import java.util.List;
import java.util.Vector;

/**
 Stores all information for a CheckFile.  A check file is an HTML file that
 has information on data checks for various components.  This class does not
 perform data checks or validation, it is only used to format information
 into an HTML file.  Numerous Check File Data Models can be added to a
 CheckFile.  The CheckFile was designed for the State of Colorado to provide
 an easier way to navigate and filter on final product data checks.
 */
public class CheckFile 
{
	private String __check_file;	// File for data check info
	private String __commands;		// Command file name
	
	private List<String> __header;	// Header text for the check file.
									// This contains text about the
									// current program such as version
									// and other program specific
									// properties and system configurations
									// This is static and is not tied to the
									// data.  It is more of an overview of the
									// program and its state.  If empty then the
									// header section will be blank.
	
	private List<String> __run_msgs;// List of runtime messages
									// Runtime messages are problems that
									// have occurred during processing checks
									// on the data.
	
	// Shared proplists used for html attributes.
	// Id's can be used to style or format
	// a specific HTML tag.  __title_prop is used
	// to set an id for HTML tags that are used
	// as titles to a section.  __td_prop is used
	// to set attributes for the td tag.
	private PropList __title_prop = new PropList("html_id");
	private PropList __td_prop = new PropList("html_td_tag");
	// stores invalid data for specific data checks	
	private List<CheckFile_DataModel> __spec_data;	
	// stores invalid data for general data checks
	private List<CheckFile_DataModel> __gen_data;
	
/**
Constructor that initializes the check file.
@param fname Name of the check file.
@param command_file Name of the command file.
 */
public CheckFile( String command_file, String commands_asString )
{
	if( isValidStr( command_file ) ) {
		__check_file = command_file + ".html";
	}
	else {
		__check_file = "";
	}
    if( isValidStr( commands_asString ) ) {
    	__commands = commands_asString;
    }
    else {
    	__commands = "";
    }
    
	// add the id value to be used to format HTML tags
	__title_prop.add( "id=titles" );
	// add the td attributes to the proplist
	__td_prop.add("valign=bottom");
	__run_msgs = new Vector<String>();		// Runtime error messages
	__header = new Vector<String>();		// Header for check file
	__gen_data = new Vector<CheckFile_DataModel>();		// general data checks
	__spec_data = new Vector<CheckFile_DataModel>();		// specific data checks
}

/**
Adds data to the data vectors for the check file.  Does some simple checks
to make sure data is available.
@param data Model for the specific checked data.
@param table_headers List of column headers.
@param header Header for this data.
@param title Title for this data.
@param gen_data Model for the general checked data.
 */
public void addData( CheckFile_DataModel data, CheckFile_DataModel gen_data )
{
	if ( data != null ) {
		__spec_data.add( data );
		if ( gen_data != null ) {
			__gen_data.add( gen_data );
		}
		// if no general data exists create a blank object for it
		else {
			__gen_data.add(new CheckFile_DataModel(new Vector(), new String[]{}, "", "", 0, 0 ) );
		}
	}
}

/**
Adds runtime messages (errors or warnings) to the runtime warning Vector.
@param str String to add to the list.
 */
public void addRuntimeMessage( String str )
{
	if( isValidStr( str ) ) {
		__run_msgs.add( str );
	}
}

/**
Adds a string to the current header.
@param str String to add to the header.
*/
public void addToHeader( String str )
{
	if( isValidStr(str) ) {
		__header.add( str );
	}
}

/**
Adds an array of strings to the current header.
@param strs List of Strings to add to the header.
 */
public void addToHeader ( String[] strs )
{
	for ( int i = 0; i < strs.length; i++ ) {
		if( strs[i] != null ) {
			addToHeader( strs[i] );
		}
	}
}

/**
Close all open tags and finish writing the html to the file.
@param html HTMLWriter object.
@throws Exception 
 */
private void endHTML( HTMLWriter html)  throws Exception
{
	if ( html != null ) {
		html.bodyEnd();
		html.htmlEnd();
		html.closeFile();
	}
}

/**
Writes all sections of the check file in HTML.  Requires that
the data Vectors have been populated with the correct data and
headers.
@throws Exception 
 */
public String finalizeCheckFile() throws Exception 
{
	HTMLWriter html = new HTMLWriter( __check_file, "Check File", false );
	startHTML( html );
	writeTableOfContents( html );
	writeHeader( html );
	writeCommandFile( html );
	writeRuntimeMessages( html );
	writeDataChecks( html );
	endHTML( html );
	
	return __check_file;
}

/**
Helper method to return the header string for the HTML file.
@return Header text for the HTML file.
 */
private String getHeaderString()
{
	String header = "";
	if ( __header != null && __header.size() > 0 ) {
		for( int i = 0; i < __header.size(); i++ ) {
			String tmp = (String)__header.get(i) ;
			if( !tmp.endsWith("\n") && !tmp.endsWith("\r")) {
				header += (tmp + "\n" );
			}
			else { header += tmp; }
		}
	}
	return header;
}

/**
Inserts the head tags and style information for RTi check files.
@throws Exception
 */
public void headForCheckFile( HTMLWriter html ) throws Exception
{
    html.headStart();
    writeCheckFileStyle(html);
    html.headEnd();
}

/**
Checks if the given string is null or has a length of zero.
@param  str String to check.
@return If string is valid or not.
 */
private boolean isValidStr( String str )
{
	if( str != null && str.length() > 0 ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Writes the start tags for the HTML check file.
@param html HTMLWriter object.
@throws Exception
 */
private void startHTML( HTMLWriter html ) throws Exception
{
	if ( html != null ) {
		html.htmlStart();
		headForCheckFile(html);
		html.bodyStart();
	}
}

/**
An overridden method for toString().  Returns the name of
the check file.
@return Name of the check file.
 */
public String toString()
{
	return __check_file;
}

/**
Inserts the style attributes for a check file.
@throws Exception
 */
public void writeCheckFileStyle(HTMLWriter html) throws Exception
{
    html.write("<style>\n"
            + "#titles { font-weight:bold; color:#303044 }\n"
            + "table { background-color:black; text-align:left }\n"  
            + "th {background-color:#333366; text-align:center;"
            + " vertical-align:bottom; color:white }\n" 
            + "td {background-color:white; text-align:center;"
            + " vertical-align:bottom; }\n" 
            + "body { text-align:left; font-size:12; }\n"
            + "pre { font-size:12; }\n"
            + "p { font-size:12; }\n"
            + "</style>\n");
}

/**
Writes the command file in HTML using the <pre> tag.  This will
force the output to match the command files text exactly.
@param html HTMLWriter object.
@throws Exception
 */
private void writeCommandFile( HTMLWriter html )  throws Exception
{
	if ( html != null ) {
		// proplist provides an anchor link for this section used
		// from the table of contents
		PropList command_file_prop = new PropList("Command File");
		command_file_prop.add("name=command_file");
		html.paragraphStart( __title_prop );
		html.link( command_file_prop, "", "Command File" );
		html.paragraphEnd();
		html.pre( __commands );
		html.horizontalRule();
	}
}

/**
Writes the HTML for the specific data checks. 
@param html HTMLWriter object.
@throws Exception
 */
private void writeDataChecks( HTMLWriter html )  throws Exception
{
	if ( html != null ) {
		// proplist provides an anchor link for this section used
		// from the table of contents
		PropList tableStart = new PropList("Table");
		tableStart.add("border=\"1\"");
		tableStart.add("bordercolor=black");
		tableStart.add("cellspacing=1");
		tableStart.add("cellpadding=1");
		// write out all component data checks
		for ( int i = 0; i < __spec_data.size(); i++ ) {
			writeGenericDataChecks( html, tableStart, ( CheckFile_DataModel )__gen_data.get(i), i );
			html.breakLine();
			writeOtherData( html, tableStart, ( CheckFile_DataModel )__spec_data.get(i), i );
		}
	}
}

/**
Writes the HTML for the general data checks.
@param html HTMLWriter object.
@param tableStart - List of properties for the HTML table. 
@param int index Current index of the data list.
@throws Exception
 */
private void writeGenericDataChecks( HTMLWriter html,
PropList tableStart, CheckFile_DataModel gen_data_model,
int index ) throws Exception
{
	if ( html != null ) {
		// grab the data from the model
		List gen_data = new Vector();
		gen_data = gen_data_model.getData();
		// proplist provides an anchor link for this section used
		// from the table of contents
		PropList gen_prop = new PropList( "Gen" );
		gen_prop.add( "name=generic" + index );
		// start the generic data section
		html.paragraphStart( __title_prop );
		html.link( gen_prop, "", gen_data_model.getTitle() );
		if ( gen_data.size() > 0 ) {
			// table start
			html.tableStart( tableStart );
			html.tableRowStart();
			html.tableHeaders( gen_data_model.getTableHeader() );
			html.tableRowEnd();
			// loop through the actual data and add table cells
			for ( int j = 0; j < gen_data.size(); j++ ) {
				String [] tds = ((String [])gen_data.get(j));
				if ( tds != null && tds.length > 0 ) {
					html.tableRowStart();
					html.tableCells( tds, __td_prop );
					html.tableRowEnd();
				}
			}
			html.tableEnd();
		}
	}
}

/**
Helper method to write the header section of the check file.
@param html HTMLWriter object.
@throws Exception
 */
private void writeHeader( HTMLWriter html )  throws Exception
{
	if ( html != null ) {
		// proplist provides an anchor link for this section used
		// from the table of contents
		PropList header_prop = new PropList("header");
		header_prop.add("name=header");
		html.paragraphStart( __title_prop );
		html.link( header_prop, "", "Header" );
		html.paragraphEnd();
		html.pre( getHeaderString() );
		html.horizontalRule();
	}
}

/**
Writes the specific data checks to the check file.
@param html HTMLWriter object.
@param tableStart List of HTML table properties.
@param index The current index of the data Vector.
@throws Exception
 */
private void writeOtherData( HTMLWriter html, PropList tableStart,
CheckFile_DataModel data_model, int index ) throws Exception
{
	if ( html != null ) {
		// Get the data from the model
		List data = data_model.getData();
		// proplist provides an anchor link for this section used
		// from the table of contents
		PropList data_prop = new PropList( "Data " + index );
		data_prop.add( "name=data" + index );
		// write the more component specific data
		html.paragraphStart( __title_prop );
		html.link( data_prop, "", data_model.getTitle() );
		html.pre( data_model.getInfo() );
		if ( data.size() > 0 ) {
			// table start
			html.tableStart( tableStart );
			html.tableRowStart();
			html.tableHeaders( data_model.getTableHeader() );
			html.tableRowEnd();
			// loop through the data
			for ( int j = 0; j < data.size(); j++ ) {
				String [] tds = ((String [])data.get(j));
				if ( tds != null && tds.length > 0 ) {
					html.tableRowStart();
					html.tableCells( tds, __td_prop );
					html.tableRowEnd();
				}
			}
			html.tableEnd();
		}
	}
}

/**
Writes all runtime messages to the runtime message section
in the check file.
@param html HTMLWriter object.
@throws Exception
 */
private void writeRuntimeMessages( HTMLWriter html )  throws Exception
{
	if ( html != null ) {
		// proplist provides an anchor link for this section used
		// from the table of contents
		PropList runmsg_prop = new PropList("Run Messages");
		runmsg_prop.add("name=run_msgs");
		html.paragraphStart( __title_prop );
		html.link( runmsg_prop, "", "Run Messages" );
		html.paragraphEnd();
		String message = "";
		if ( __run_msgs.size() == 0 ) {
			__run_msgs.add( "No warnings or errors encountered" );
		}
		// loop through all the run messages and print them out
		for ( int i = 0; i < __run_msgs.size(); i++ ) {
			String msg = (String)__run_msgs.get(i);
			if ( isValidStr ( msg )) {
				if( !msg.endsWith("\n") && !msg.endsWith("\r")) {
					message += ( msg + "\n" );
				}
				else { message += msg; }
			}
		}
		html.pre( message );
		html.horizontalRule();
	}
}

/**
Writes the HTML table of contents for the check file.
@param html HTMLWriter object.
@throws Exception
 */
private void writeTableOfContents( HTMLWriter html )  throws Exception
{
	if ( html != null ) {
		// properties for the table of contents HTML table
		String tcontents = "Table Of Contents";
		PropList tableStart = new PropList("Table");
		tableStart.add("border=\"1\"");
		tableStart.add("bordercolor=black");
		tableStart.add("cellspacing=1");
		tableStart.add("cellpadding=1");
		String [] data_table_header = {"Component", "Type of Check", "# Problems", "# Total Checks"};
		//html follows ...
		html.headerStart( 4, __title_prop );	// <h3> tag
		html.addText( tcontents );
		html.headerEnd( 4 );
		html.link( "#header", "Header" );
		html.breakLine();
		html.link( "#command_file", "Command File" );
		html.breakLine();
		html.link( "#run_msgs", "Runtime Messages (" + __run_msgs.size() + ")" );
		html.breakLine();
		// Table of contents data records (there may be many of these)
		// this is written as a table of components and there
		// general and specific data checks
		html.tableStart( tableStart );
		html.tableRowStart();
		html.tableHeaders( data_table_header );
		html.tableRowEnd();
		// Write out the data and links to data checks
		// as a table with links to missing and specific data checks
		for ( int i = 0; i < __spec_data.size(); i++ ) {
			// get the data models
			CheckFile_DataModel dm = ( CheckFile_DataModel )__spec_data.get(i);
			CheckFile_DataModel dm_gen = ( CheckFile_DataModel )__gen_data.get(i);
			// get the data needed for the TOC from the data models
			//String data_size = new Integer( 
			//		dm_gen.getDataSize() ).toString();
			String data_size = new Integer ( 
				dm_gen.getTotalNumberProblems()).toString();
			String total_size = new Integer( 
				dm_gen.getTotalChecked() ).toString();
			String[] toc_values = { dm.getTitle(), "Zero or Missing",
				data_size, total_size };
			// write the first data section (row)
			// this section has the general data check info and links
			writeTocDataSection( html, toc_values, i );
			data_size = new Integer( dm.getTotalNumberProblems()).toString();
			total_size = new Integer( 
				dm.getTotalChecked() ).toString();
			String[] toc_values2 = { dm.getTitle(), data_size, total_size };
			// write the second data section (row)
			// this section has the specific data check info and links
			writeTocDataSection( html, toc_values2, i );
		}
		html.tableEnd();
		html.horizontalRule();
	}
}

/**
Writes the data portion of the table of contents.  This section contains
a table with links to data checks.
@param html HTMLWriter object.
@param toc Table of contents list data.
@param index Current index of the iteration of the data Vectors.
@throws Exception
 */
private void writeTocDataSection( HTMLWriter html, String[] toc, int index ) 
throws Exception 
{
	if ( html != null && toc != null ) {
		// HTML anchors used to link table of contents to data sections
		String base_anchor = "#data" + index;
		String generic_anchor = "#generic" + index;
		// proplist for <td rowspan=2>
		// need this for formatting the data table
		PropList tdStart = new PropList("Table");
		tdStart.add("valign=bottom");
		tdStart.add("rowspan=2");
		// If it has 4 elements, this is the first row of data
		// because it contains the type of data or component name.
		// This row must be written differently since the column
		// has to span two rows.
		if ( toc.length == 4 ) {
			html.tableRowStart();
			for ( int i = 0; i < toc.length; i++ ) {
				switch ( i ) {
					case 0: html.tableCellStart( tdStart );
						html.addText( toc[i] );
						html.tableCellEnd();
					break;
					case 1: html.tableCellStart( __td_prop );
						html.link( generic_anchor, toc[i] );
						html.tableCellEnd();
					break;
					case 2: html.tableCellStart( __td_prop );
						html.link( generic_anchor, toc[i] );
						html.tableCellEnd();
					break;
					case 3: html.tableCellStart( __td_prop );
						html.addText( toc[i] );
						html.tableCellEnd();
					break;
					default: ;
				}
			}
			html.tableRowEnd();
		}
		// If it has 3 elements then this is the second row
		// for this data element.  This row is formatted differently 
		// because it doesn't have a component name.
		else if ( toc.length == 3 ) {
			html.tableRowStart();
			for ( int i = 0; i < toc.length; i++ ) {
				switch ( i ) {
					case 0: html.tableCellStart( __td_prop );
						html.link( base_anchor, toc[i] );
						html.tableCellEnd();
					break;
					case 1: html.tableCellStart( __td_prop );
						html.link( base_anchor, toc[i] );
						html.tableCellEnd();
					break;
					case 2: html.tableCellStart( __td_prop );
						html.addText( toc[i] );
						html.tableCellEnd();
					break;
					default: ;
				}
			}
			html.tableRowEnd();
		}
	}
}

}