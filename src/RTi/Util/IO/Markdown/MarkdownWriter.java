// MarkdownWriter - creates files or Strings of Markdown-formatted text.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2022 Colorado Department of Natural Resources
Copyright (C) 2022 Open Water Foundation

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


package RTi.Util.IO.Markdown;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
Class to format Markdown content.
This class is meant to be used at the most basic level for Markdown formatting.
It is expected that the component(s) that implement viewing will control CSS
and support the Markdown standards being used,
such as standard Markdown, GitHub flavored Markdown, etc.
*/
public class MarkdownWriter {

	/*
	 * Indent to use to indicate block quotes, etc.
	 */
	private final String indentString = "    ";
	
	/*
	 * String indicating bold in Markdown.
	 */
	private final String boldString = "**";
	
	/**
	Specifies whether the Markdown writer has been closed and written yet or not.
	*/
	private boolean closed = false;

	/**
	Specifies whether to write the Markdown to memory or a file.
	If true, then the Markdown will be written to the 'markdown' StringBuffer in memory.
	If false the Markdown will be written out to a a file.
	*/
	private boolean writeToFile = false;

	/**
	The BufferedWriter that will be used if the HTML is written out to a file.
	*/
	private BufferedWriter markdownFile = null;

	/**
	The StringBuffer of Markdown that is being generated.
	*/
	private StringBuffer markdownBuffer = null;

	/**
 	* Whether the write is using the buffer and file of a parent writer.
 	*/
	private boolean subWriter = false;

	/**
	Constructor.
	The Markdown will be stored in memory and can be retrieved using getMarkdown().
	@throws IOException if an error occurs setting up the writer.
	*/
	public MarkdownWriter () throws IOException {
		this ( (String)null, false );
	}

	/**
	Constructor.
	@param filename the filename to which to write Markdown.
	@param append if true, append to the file, if false do not append
	Can be null, in which case the Markdown will not be written to a file but will be stored in memory.
	*/
	public MarkdownWriter (String filename, boolean append ) throws IOException {
		if (filename == null) {
			this.writeToFile = false;
			this.markdownBuffer = new StringBuffer();
			this.markdownFile = null;
		}
		else {
			this.writeToFile = true;
			this.markdownBuffer = null;
			this.markdownFile = new BufferedWriter(new FileWriter(filename, append));
		}
	}

	/**
	Constructor for an MarkdownWriter that will write into Markdown already produced by another MarkdownWriter.
	@param markdownWriter an existing MarkdownWriter into which this Markdown will be written.
	@throws IOException if an error occurs writing to a file.
	*/
	public MarkdownWriter(MarkdownWriter markdownWriter) throws IOException {
		this.writeToFile = markdownWriter.getWriteToFile();
		this.markdownBuffer = markdownWriter.getStringBuffer();
		this.markdownFile = markdownWriter.getBufferedWriter();
		this.subWriter = true;
	}

	/**
	Creates an indented block quote, indented by 4 spaces.
	@param s the String content for block quote
	@throws IOException if an error occurs writing to a file.
	*/
	public void blockquote(String s) throws IOException {
		write(s);
	}

	/**
	Inserts a line break using HTML <code><br></code>.
	@throws IOException if an error occurs writing to a file.
	*/
	public void breakLine() throws IOException {
		write("<br>");
	}

	/**
	Ends an bulleted list declaration using HTML <code><ul></code>.
	@throws IOException if an error occurs writing to a file.
	*/
	public void unorderedListEnd() throws IOException {
		write("</ul>");
	}

	/**
	Starts a bulleted list declaration using HTML <code></ul></code>.
	@throws IOException if an error occurs writing to a file.
	*/
	public void unorderedListStart() throws IOException {
		write("<ul>");
	}

	/**
	Add the given string as a table or figure caption.
	@param caption the String for the caption.
	@throws IOException if an error occurs writing to a file.
	*/
	public void caption(String caption) throws IOException {
		write("\n**<p style=\"text-align: center;\">" + caption + "</p>**\n" );
	}

	/**
	Closes a Markdown file and writes it to disk.
	@throws IOException if an error occurs writing text to a file.
	*/
	public void closeFile() throws IOException {
		if (this.subWriter) {
			return;
		}
		if (this.writeToFile) {
			this.markdownFile.close();
		}
		this.closed = true;
	}

	/**
	Adds the given string in "code" to the HTML.
	@param s the String to output in code format.
	@throws IOException if an error occurs writing to a file.
	*/
	public void code(String s) throws IOException {
    	write("```\n");
    	write(s);
    	write("```\n");
	}

	/**
	Write the given text inside a comment ending with a newline.
	@throws IOException if an error occurs writing to a file.
	*/
	public void comment(List<String> comments) throws IOException {
		commentStart();
		for ( String comment : comments ) {
			write(comment + "\n");
		}
		commentEnd();
	}

	/**
	Write the given text inside a comment ending with a newline.
	@param comment comment string to write
	@throws IOException if an error occurs writing to a file.
	*/
	public void comment(String comment) throws IOException {
		write("<!-- " + comment + " -->\n");
	}

	/**
	Stops commenting.
	@throws IOException if an error occurs writing to a file.
	*/
	public void commentEnd() throws IOException {
		write("-->\n");
	}

	/**
	Starts a comment.
	@throws IOException if an error occurs writing to a file.
	*/
	public void commentStart() throws IOException {
		write("<!--");
	}

	/**
	Returns the BufferedWriter used to write HTML.
	@return the BufferedWriter used to write HTML.
	*/
	protected BufferedWriter getBufferedWriter() {
		return this.markdownFile;
	}

	/**
	Returns the HTML that was written to memory, or null if no HTML was/has been written to memory.
	@return a String of HTML.
	*/
	public String getMarkdown() {
		if (this.markdownBuffer == null) {
			return null;
		}
		else {
			return this.markdownBuffer.toString();
		}
	}

	/**
	Returns the StringBuffer used to write Markdown.
	@return the StringBuffer used to write Markdown.
	*/
	protected StringBuffer getStringBuffer() {
		return this.markdownBuffer;
	}

	/**
	Returns whether Markdown is being written to a file or not.
	@return whether Markdown is being written to a file or not.
	*/
	protected boolean getWriteToFile() {
		return this.writeToFile;
	}

	/**
	Starts a heading section for the given level.
	@param level the level of heading (1+) to make.
	@throws IOException if an error occurs writing to a file.
	*/
	public void heading ( int level, String text) throws IOException {
		for ( int i = 0; i < level; i++ ) {
			write("#");
		}
		write ( " " );
		if ( text != null ) {
			write ( text );
		}
		write ( " " );
		for ( int i = 0; i < level; i++ ) {
			write("#");
		}
		write("\n\n");
	}

	/**
	Creates a horizontal rule across the page.
	@throws IOException if an error occurs writing to a file.
	*/
	public void horizontalRule() throws IOException {
		write("\n-------\n");
	}

	/**
	Inserts a hyperlink to the given location with the given text.
	@param linkText the text to display
	@param url the location to link to.
	@throws IOException if an error occurs writing to a file.
	*/
	public void link( String linkText, String url ) throws IOException {
		write("[" + linkText + "](" + url + ")");
	}

	/**
	Inserts a list item into a list.
	@param s the item to insert in the list.
	@throws IOException if an error occurs writing to a file.
	*/
	public void listItem(String s, int level) throws IOException {
		writeIndent(level);
		write(s);
		write("\n");
	}

	/**
	Starts a numbered (ordered) list.
	@throws IOException if an error occurs writing to a file.
	*/
	public void numberedListStart() throws IOException {
		write("<ol>");
	}

	/**
	Ends a numbered (ordered) list.
	@throws IOException if an error occurs writing to a file.
	*/
	public void numberedListEnd() throws IOException {
		write("</ol>");
	}

	/**
	Inserts a new paragraph with the given text.
	@param s the text to put in the paragraph.
	@throws IOException if an error occurs writing to a file.
	*/
	public void paragraph(String s) throws IOException {
		paragraphStart();
		write(s);
		paragraphEnd();
	}

	/**
	Ends a paragraph (insert newline).
	@throws IOException if an error occurs writing to a file.
	*/
	public void paragraphEnd() throws IOException {
		write("\n");
	}

	/**
	Starts a paragraph (insert newline).
	@throws IOException if an error occurs writing to a file.
	*/
	public void paragraphStart() throws IOException {
		write("\n");
	}

	/**
	Inserts a block of pre-formatted text.
	@param s the text to be pre-formatted.
	@throws IOException if an error occurs writing to a file.
	 */
	public void pre(String s) throws IOException {
		preStart();
		write(s);
		preEnd();
	}

	/**
	Ends a block of pre-formatted text.
	@throws IOException if an error occurs writing to a file.
	*/
	public void preEnd() throws IOException {
		write("```\n");
	}

	/**
	Starts a block of pre-formatted text as a block (not inlined).
	@throws IOException if an error occurs writing to a file.
	*/
	public void preStart() throws IOException {
		write("```\n");
	}

	/**
	Inserts a new quote with the given text.
	@param s the text to put in the quote.
	@throws IOException if an error occurs writing to a file.
	*/
	public void quote(String s) throws IOException {
		write("```" + s + "```");
	}

	/**
	Ends a quote.
	@throws IOException if an error occurs writing to a file.
	*/
	public void quoteEnd() throws IOException {
		write("```");
	}

	/**
	Starts a quote declaration.
	@throws IOException if an error occurs writing to a file.
	*/
	public void quoteStart() throws IOException {
		write("```");	
	}

	/**
	Write a quote string.
	@param s String to quote.
	@throws IOException if an error occurs writing to a file.
	*/
	public void quoteStart(String s) throws IOException {
		write("```" + s + "```");
	}

	/**
	Inserts a span element.
	@param spanText text to insert.
	@param props property list for HTML attributes, for example style
	@throws IOException if an error occurs writing to a file.
 	*/
	public void span ( String spanText ) throws IOException {
	    spanStart("");
    	write ( spanText );
    	spanEnd();
	}

	/**
	Ends a span declaration.
	@throws IOException if an error occurs writing to a file.
	*/
	public void spanEnd() throws IOException {
    	write("</span>");
	}

	/**
	Starts a span declaration with the given parameters.
	@param s String of span parameters.
	@throws IOException if an error occurs writing to a file.
	*/
	public void spanStart(String s) throws IOException {
    	if (s.trim().equals("")) {
        	write("<span>");
        	return;
    	}
    	write("<span " + s + ">");
	}

	/**
	Inserts a cell into a table with the given text with ending <code>|</code> and no newline.
	@param s the text to put into the cell.
	@throws IOException if an error occurs writing to a file.
	*/
	public void tableCell(String s) throws IOException {
		if ( s != null ) {
			write(s.replaceAll("\n", ""));
		}
		write("|");
	}

	/**
	Inserts multiple cells into a table.
	@param cells Array of Strings to write to each cell.  Null strings are inserted as empty strings.
	@param Property list for HTML attributes.
	@throws IOException if an error occurs writing to a file.
 	*/
	/*
	public void tableCells( String cells [] ) throws IOException {
		String cell;
		for ( int i = 0; i < cells.length; i++ ) {
			cell = cells[i];
			if ( cells[i] == null ) {
				cell = "";
			}
			//tableCellStart();
			addText( cell );
			tableCellEnd();
		}
	}
	*/

	/**
	Inserts a table header separator <code>--|</code>.
	@throws IOException if an error occurs writing to a file.
	*/
	public void tableHeaderSeparator() throws IOException {
		write( "--|" );
	}

	/**
	Inserts multiple table headers with the Strings specified and header separator row.
	@param headers Array of Strings to use for each header.
	@throws IOException if an error occurs writing to a file.
 	*/
	public void tableHeaders(String [] headers ) throws IOException {
		for ( int i = 0; i < headers.length; i++ ) {
			String header = headers[i];
			if ( headers[i] == null ) {
				header = "";
			}
			tableHeader( header );
		}                                     
		// Write the headers separator line.
		write ( "\n|" );
		for ( int i = 0; i < headers.length; i++ ) {
			tableHeaderSeparator();
		}
		write ( "\n" );
	}

	/**
	Inserts a table header cell as bold with the given text and trailing |.
	@param s text to put in the table header cell.
	@throws IOException if an error occurs writing to a file.
	*/
	public void tableHeader(String s) throws IOException {
		write ( this.boldString + s.replaceAll("\n", "" ) + this.boldString + "|");
	}


	/**
	Ends a table header cell declaration.
	@throws IOException if an error occurs writing to a file.
	*/
	/*
	public void tableHeaderEnd() throws IOException {
		if (__thL == 0) {
			return;
		}
			__thL--;
		write("</th>\n");
	}
	*/

	/**
	Starts a table header cell declaration.
	@throws IOException if an error occurs writing to a file.
	*/
	/*
	public void tableHeaderStart() throws IOException {
		tableHeaderStart("");
	}
	*/

	/**
	Starts a table header cell declaration with the given parameters.
	@param p PropList of table header cell parameters.
	@throws IOException if an error occurs writing to a file.
	*/
	/*
	public void tableHeaderStart(PropList p) throws IOException {
		tableHeaderStart(propListToString(p));
	}
	*/

	/**
	Starts a table header cell declaration with the given parameters.
	@param s String of table header cell parameters.
	@throws IOException if an error occurs writing to a file.
	*/
	/*
	public void tableHeaderStart(String s) throws IOException {
		if (__trL == 0) {
			return;
		}
		__thL++;
		if (s.trim().equals("")) {
			write("<th>");
			return;
		}
		write("<th " + s + ">");
	}
	*/

	/**
	Inserts multiple cells into a table with the row start and end tags.
	@param cells Array of Strings to write to each cell.
	@throws IOException
 	*/
	public void tableRow( String cells [] ) throws IOException {   
    	tableRowStart();
    	//tableCells ( cells );
    	tableRowEnd();
	}

	/**
	Ends a table row declaration.  Just writes a newline.
	@throws IOException if an error occurs writing to a file.
	*/
	public void tableRowEnd() throws IOException {
		write("\n");
	}

	/**
	Starts a table row declaration.
	@throws IOException if an error occurs writing to a file.
	*/
	public void tableRowStart() throws IOException {
		write("|");
	}

	/**
	Writes a String of text either to a file, or to an internal StringBuffer.
	No checks are done on the content, so it should contain proper open and closing tags.
	No newline is written so add the newline to the string before calling if needed for formatting.
	@param s the String to write.
	@throws IOException if an error occurs writing to a file.
	*/
	public void write( String s ) throws IOException {
		if (this.writeToFile) {
			this.markdownFile.write(s);
		}
		else {
			this.markdownBuffer.append(s);
		}
	}
	
	/**
	Writes indent text for a block quote, list level, etc.
	@throws IOException if an error occurs writing to a file.
	*/
	public void writeIndent ( int level ) throws IOException {
		for ( int i = 1; i <= level; i++ ) {
			write(this.indentString);
		}
	}

}