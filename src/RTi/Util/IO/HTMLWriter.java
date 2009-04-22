// ----------------------------------------------------------------------------
// HTMLWriter - Creates files or Strings of HTML-formatted text.
// See href="http://www.willcam.com/cmat/html/crossref.html for a good 
// HTML tag reference.
// ----------------------------------------------------------------------------
// History:
//
// 2003-04-17	J. Thomas Sapienza, RTi	Initial version.
// 2003-04-21	JTS, RTi		* Revised, adding a lot of commenting.
//					* Removed the CENTER tag (use <P align=
//					  CENTER> instead).
//					* HTML now is written either directly
//					  to a file or to memory.
// 2003-09-26	JTS, RTi		* Renamed endHTML to closeFile().
//					* Made the closeFile() javadocs clearer.
// 2003-09-26	Anne Morgan Love, RTi	* Updated the image() method to
//					  use the correct tag and to include
//					  border, width, height, float, and
//					  alt tag options.
//					* Updated HEAD tag to take up to
//					  2 META tags.
// 2003-12-03	JTS, RTi		* Added finalize().  
//
// 2004-02-25	AML, RTi		* Updated tags to be in LowerCase
//					according to World Wide Web Consortium
//					(W3C).
//		
//					* added <q> Quote tag.
//
// 2004-03-02	AML, RTi		* Added htmlEncode(String s) to
//					* call checkText() and replace 
//					* special characters with
//					* encoded versions.
// 2004-11-18	JTS, RTi		* Added addLinkText(), which works like
//					  addText(), but doesn't append a 
//					  newline to the end of the text (which 
//					  can screw up links in IE).
//					* anchor() now uses addLinkText().
// 2007-03-15	Kurt Tometich, RTi	*Added new functions for CheckFiles
//					  that use html.  Fixed several functions that were not
//					  up to date.  Added some keyword replacements in the
//					  checkText() method.
// ----------------------------------------------------------------------------

package RTi.Util.IO;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
Class to format HTML content.  This class is meant to be used
at the most basic level for HTML formatting.  Therefore, HTML tags are
written in pieces.  It is expected that a higher-level class will be developed
to provide a more friendly interface to HTML.<p>

All of the HTML tag methods are named in a specific pattern (and the list of
supported tags along with their appropriate functions is 
<a href="#tagMethodTable">here</a>).  Each method is a verbose name (e.g.,
instead of a method called <tt>&quot;b()&quot;</tt>, there is one called 
<tt>&quot;bold()&quot;</tt> of the kind of tag it is used for and may have
<tt>Start</tt> or <tt>End</tt> appended to it according to the following table:

<p>
<b>Note:</b> in the following table, assume the HTML tag being referred to is
&lt;TAG&gt; and the verbose English name is tagName.<p>

<table border>
<tr valign=top>
<th>Method name pattern</th>
<th>Description of method's actions</th>
<tr>

<tr valign=top>
<td>tagName(String)</td>
<td>inserts &lt;TAG&gt;, followed by the String passed in to the method, followed by &lt;/TAG&gt;</td>
</tr>

<tr valign=top>
<td>tagNameEnd()</td>
<td>inserts &lt;/TAG&gt;</td>
</tr>

<tr valign=top>
<td>tagNameStart()</td>
<td>inserts &lt;TAG&gt;</td>
</tr>

<tr valign=top>
<td>tagNameStart(PropList)<br>tagNameStart(String)</td>
<td>inserts &lt;TAG followed by either: 
<ul>
<li>the list of parameters contained in the PropList (or)</li>
<li>the String passed into the method</li>
</ul>
and finally, the closing &gt;.
</td>
</tr>
</table>g

Here is an example of how it can be used:<p>
<BLOCKQUOTE><PRE>
HTMLWriter w = new HTMLWriter("Title of HTML Page");

w.tableStart();

w.tableRowStart();
w.tableHeader("Column 1");
w.tableHeader("Column 2");
w.tableRowEnd();

w.tableRowStart();
w.tableCell("Row 1 Column 1");
w.tableCell("Row 2 Column 2");
w.tableRowEnd();

w.tableEnd();

w.numberedListStart();
w.listItem("First item");
w.listItem("Second item");
w.numberedListEnd();

w.closeFile();
</BLOCKQUOTE>
</PRE>

Because in some cases the name of the method to use for inserting a specific
tag may not be immediately obvious, here is a list of the class methods for
inserting HTML tags, sorted by the HTML tag.
<a name="tagMethodTable">
<table border>
<tr>
<TH>HTML Tag</TH>    <TH>Tag description</TH>   <TH>Method name</TH>
</tr>

<tr valign=top>
<td>&lt;!-- ... --&gt;</td>
<td>Comments out everything between the tags</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#comment(java.lang.String)">
comment(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#commentEnd()">commentEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#commentStart()">commentStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;a href&gt;</td>
<td>Adds a hyperlink to another document</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#link(java.lang.String, java.lang.String)">
link(String, String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#linkEnd()">linkEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#linkStart(java.lang.String)">
linkStart(String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;A NAME&gt;</td>
<td>Named anchor</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#anchor(java.lang.String)">
anchor(String)</a></td>
</tr>

<tr valign=top>
<td>&lt;B&gt;</td>
<td>Sets text to bold</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#bold(java.lang.String)">
bold(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#boldEnd()">boldEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#boldStart()">boldStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;BLOCKQUOTE&gt;</td>
<td>Quotes a block of text and sets it in one tabstop</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#blockquote(java.lang.String)">
blockquote(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#blockquoteEnd()">blockquoteEnd()
</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#blockquoteStart()">
blockquoteStart()</a><br>
<A 
HREF=
"../../../RTi/Util/IO/HTMLWriter.html#blockquoteStart(RTi.Util.IO.PropList)">
blockquoteStart(PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#blockquoteStart(java.lang.String)"
>blockquoteStart(String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;BODY&gt;</td>
<td>Marks the body of the HTML document</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#bodyEnd()">bodyEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#bodyStart()">bodyStart()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#bodyStart(RTi.Util.IO.PropList)">
bodyStart(PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#bodyStart(java.lang.String)">
bodyStart(String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;BR&gt;</td>
<td>Breaks a line</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#breakLine()">breakLine()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;CAPTION&gt;</td>
<td>Captions text as a table caption</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#caption(java.lang.String)">
caption(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#captionEnd()">captionEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#captionStart()">captionStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;DD&gt;</td>
<td>Sets a definition in a definition list</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definition(java.lang.String)">
definition(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definition(java.lang.String, java.lang.String)">
definition(String, String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definitionEnd()">
definitionEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definitionStart()">
definitionStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;DL&gt;</td>
<td>Denotes a definition list</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definitionListEnd()">
definitionListEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definitionListStart()">
definitionListStart()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definitionListStart(RTi.Util.IO.PropList)">
definitionListStart(PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definitionListStart(java.lang.String)">
definitionListStart(String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;DT&gt;</td>
<td>Sets a definition term in a definition list</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definitionTerm(java.lang.String)">
definitionTerm(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definitionTermEnd()">
definitionTermEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#definitionTermStart()">
definitionTermStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;FONT&gt;</td>
<td>Sets text to a certain font</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#fontEnd()">fontEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#fontStart(RTi.Util.IO.PropList)">
fontStart(PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#fontStart(java.lang.String)">
fontStart(String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;H&gt;</td>
<td>Sets a heading</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#heading(int, java.lang.String)">
heading(int, String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#headingEnd(int)">
headingEnd(int)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#headingStart(int)">
headingStart(int)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#headingStart(int, RTi.Util.IO.PropList)">
headingStart(int, PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#headingStart(int, java.lang.String)">
headingStart(int, String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;HR&gt;</td>
<td>Creates a horizontal line across the document</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#horizontalRule()">
horizontalRule()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;HTML&gt;</td>
<td>Starts the HTML document</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#htmlEnd()">htmlEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#htmlStart()">htmlStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;I&gt;</td>
<td>Italicizes text</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#italic(java.lang.String)">
italic(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#italicEnd()">
italicEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#italicStart()">
italicStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;IMG&gt;</td>
<td>Inserts an image</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#image(RTi.Util.IO.PropList)">
image(PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#image(java.lang.String)">
image(String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;LI&gt;</td>
<td>Inserts an item into a list</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#listItem(java.lang.String)">
listItem(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#listItemEnd()">
listItemEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#listItemStart()">
listItemStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;NOBR&gt;</td>
<td>Marks text that is not to have line breaks</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#nobr(java.lang.String)">
nobr(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#nobrEnd()">nobrEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#nobrStart()">nobrStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;OL&gt;</td>
<td>Defines a numbered list</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#numberedListEnd()">
numberedListEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#numberedListStart()">
numberedListStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;P&gt;</td>
<td>Defines a paragraph</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#paragraph()">paragraph()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#paragraph(java.lang.String)">
paragraph(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#paragraphEnd()">
paragraphEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#paragraphStart()">
paragraphStart()</a><br>
<A 
HREF="../../../RTi/Util/IO/HTMLWriter.html#paragraphStart(RTi.Util.IO.PropList)"
>
paragraphStart(PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#paragraphStart(java.lang.String)">
paragraphStart(String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;PRE&gt;</td>
<td>Defines a block of pre-formatted text</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#pre(java.lang.String)">
pre(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#preEnd()">preEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#preStart()">preStart()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#preStart(RTi.Util.IO.PropList)">
preStart(PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#preStart(java.lang.String)">
preStart(java.lang.String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;SUB&gt;</td>
<td>Sets text to be subscripted</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#subscript(java.lang.String)">
subscript(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#subscriptEnd()">
subscriptEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#subscriptStart()">
subscriptStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;SUP&gt;</td>
<td>Sets text to be superscripted</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#superscript(java.lang.String)">
superscript(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#superscriptEnd()">
superscriptEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#superscriptStart()">
superscriptStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;TABLE&gt;</td>
<td>Creates a table</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableEnd()">tableEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableStart()">tableStart()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableStart(RTi.Util.IO.PropList)">
tableStart(PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableStart(java.lang.String)">
tableStart(String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;td&gt;</td>
<td>Creates a table cell</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableCell(java.lang.String)">
tableCell(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableCellEnd()">
tableCellEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableCellStart()">
tableCellStart()</a><br>
<A 
HREF="../../../RTi/Util/IO/HTMLWriter.html#tableCellStart(RTi.Util.IO.PropList)"
>
tableCellStart(PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableCellStart(java.lang.String)">
tableCellStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;TH&gt;</td>
<td>Creates a table header cell</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableHeader(java.lang.String)">
tableHeader(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableHeaderEnd()">
tableHeaderEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableHeaderStart()">
tableHeaderStart()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableHeaderStart(RTi.Util.IO.PropList)">
tableHeaderStart(PropList)</a><br>
<A 
HREF="../../../RTi/Util/IO/HTMLWriter.html#tableHeaderStart(java.lang.String)">
tableHeaderStart(String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;tr&gt;</td>
<td>Creates a table row</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableRowEnd()">
tableRowEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableRowStart()">tableRowStart()
</a><br>
<A 
HREF="../../../RTi/Util/IO/HTMLWriter.html#tableRowStart(RTi.Util.IO.PropList)">
tableRowStart(PropList)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#tableRowStart(java.lang.String)">
tableRowStart(String)</a>
</td>
</tr>

<tr valign=top>
<td>&lt;TT&gt;</td>
<td>Sets text to teletype monospaced</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#teletype(java.lang.String)">
teletype(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#teletypeEnd()">teletypeEnd()</a>
<br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#teletypeStart()">teletypeStart()
</a>
</td>
</tr>

<tr valign=top>
<td>&lt;U&gt;</td>
<td>Sets text to be underlined</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#underline(java.lang.String)">
underline(String)</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#underlineEnd()">
underlineEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#underlineStart()">
underlineStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;UL&gt;</td>
<td>Creates a bulleted list</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#bulletedListEnd()">
bulletedListEnd()</a><br>
<a href="../../../RTi/Util/IO/HTMLWriter.html#bulletedListStart()">
bulletedListStart()</a>
</td>
</tr>

<tr valign=top>
<td>&lt;WBR&gt;</td>
<td>Inserts a position at which the HTML rendered should try to break a word
if it needs to</td>
<td>
<a href="../../../RTi/Util/IO/HTMLWriter.html#wordBreak()">wordBreak()</a>
</td>
</tr>
</table>g

<p>
To specify tag properties, use one of the two tagNameStart() methods which take
a parameter (either a PropList or a String).  The method that takes the 
PropList will gather every property from the list and create a long string
of key=value statements and pass it to the other tagNameStart method that
takes a String parameter.
<p>
For instance, a PropList with two properties (Prop1: "align" = "left", Prop2:
"nowrap" = "") will be converted to this string:<br>
"align=left nowrap".

*/
public class HTMLWriter {

/**
Specifies whether to check text for metacharacters before writing it.
If set to true, the text that is visible on the HTML page is checked for 
metacharacters (e.g., "&gt;" or "&lt;") that may not appear properly and
the valid escape sequence is entered instead.  This can be expensive, because
every character is checked prior to being placed in the html text.  Text 
inside of a PRE or TT tag will not be checked for such things as multiple  spaces.
*/
private boolean __checkText = true;

/**
Specifies whether the HTML has been closed and written yet or not.
*/
private boolean __closed = false;

/**
Specifies whether the HTML has HTML and BODY tags.
If true, then this html has a header (an HTML and a BODY tag) that will need
to be closed before the HTML is written out to a file.  This is used to 
differentiate between HTML that is being written to a file (i.e., it has a
full set of HTML, TITLE, and BODY tags) and HTML that is being created for
use in pasting to the clipboard (i.e., it is incomplete HTML).
*/
private boolean __hasHeader = false;

/**
Whether this is an HTMLWriter using another writer's settings.
*/
private boolean __subWriter = false;

/**
Specifies whether to write the HTML to memory or a file.
If true, then the HTML will be written to the __html StringBuffer in memory.
If false the HTML will be written out to a a file.
*/
private boolean __writeToFile = false;

/**
The BufferedWriter that will be used if the HTML is written out to a file.
*/
private BufferedWriter __htmlFile = null;

/**
The number of &lt;H&gt; tags that are currently unclosed.
*/
private int[] __hL;

/**
The number of &lt;A&gt; (anchor) tags that are currently unclosed.
*/
private int __aL = 0;

/**
The number of &lt;B&gt; (bold) tags that are currently unclosed.
*/
private int __bL = 0;

/**
The number of &lt;BLOCKQUOTE&gt; tags that are currently unclosed.
*/
private int __blockquoteL = 0;

/**
The number of &lt;BODY&gt; tags that are currently unclosed.
*/
private int __bodyL = 0;

/**
The number of &lt;CAPTION&gt; tags that are currently unclosed.
*/
private int __captionL = 0;

/**
The number of &lt;COMMENT&gt; tags that are currently unclosed.
*/
private int __commentL = 0;

/**
The number of &lt;DD&gt; tags that are currently unclosed.
*/
private int __ddL = 0;

/**
The number of &lt;DL&gt; tags that are currently unclosed.
*/
private int __dlL = 0;

/**
The number of &lt;DT&gt; tags that are currently unclosed.
*/
private int __dtL = 0;

/**
The number of &lt;FONT&gt; tags that are currently unclosed.
*/
private int __fontL = 0;

/**
The number of &lt;HTML&gt; tags that are currently unclosed.
*/
private int __htmlL = 0;

/**
The number of &lt;I&gt; tags that are currently unclosed.
*/
private int __iL = 0;

/**
The number of &lt;LI&gt; tags that are currently unclosed.
*/
private int __liL = 0;

/**
The number of &lt;NOBR&gt; tags that are currently unclosed.
*/
private int __nobrL = 0;

/**
The number of &lt;OL&gt; tags that are currently unclosed.
*/
private int __olL = 0;

/**
The number of &lt;P&gt; tags that are currently unclosed.
*/
private int __pL = 0;

/**
The number of &lt;PRE&gt; tags that are currently unclosed.
*/
private int __preL = 0;

/**
The number of &lt;SUB&gt; tags that are currently unclosed.
*/
private int __subL = 0;

/**
The number of &lt;SUP&gt; tags that are currently unclosed.
*/
private int __supL = 0;

/**
The number of &lt;TABLE&gt; tags that are currently unclosed.
*/
private int __tableL = 0;

/**
The number of &lt;TH&gt; tags that are currently unclosed.
*/
private int __thL = 0;

/**
The number of &lt;tr&gt; tags that are currently unclosed.
*/
private int __trL = 0;

/**
The number of &lt;td&gt; tags that are currently unclosed.
*/
private int __tdL = 0;

/**
The number of &lt;TT&gt; tags that are currently unclosed.
*/
private int __ttL = 0;

/**
The number of &lt;U&gt; tags that are currently unclosed.
*/
private int __uL = 0;

/**
The number of &lt;UL&gt; tags that are currently unclosed.
*/
private int __ulL = 0;

/**
The StringBuffer of HTML that is being generated.
*/
private StringBuffer __htmlBuffer = null;

/**
Constructor.  Calls HTMLWriter(null, null, false) and sets up HTML that will
not be written to a file but will instead be written to a StringBuffer in memory.
*/
public HTMLWriter () 
throws Exception {
	this(null, null, false);
}

/**
Constructor.  Calls HTMLWriter(null, title, true) and sets up HTML that will
not be written out to a file but which WILL be formatted as a complete HTML
file (i.e., it has HTML, BODY and TITLE tags and is not just a snippet).
@param title the title of the HTML page.
*/
public HTMLWriter(String title)
throws Exception {
	this (null, title, true);
}

/**
Constructor.  Calls HTMLWriter(filename, title, true) and sets up a file to have HTML written to it.
@param filename the name of the file to write (or if it exists, overwrite)
@param title the title of the html page
*/
public HTMLWriter(String filename, String title) 
throws Exception {
	this (filename, title, true);
}

/**
Constructor.
@param filename the filename to which to write HTML.  Can be null, in which 
case the HTML will not be written to a file but will be stored in memory.
@param title the title to assign to the HTML.  Can be null.
@param createHead whether to create a head (an HTML and BODY tag) for this
HTML.  This should be true if creating an HTML page and false if only 
generating a snippet of HTML (e.g., for placing in the Clipboard).
*/
public HTMLWriter (String filename, String title, boolean createHead) 
throws Exception {
	if (filename == null) {
		__writeToFile = false;
		__htmlBuffer = new StringBuffer();
		__htmlFile = null;
	}
	else {
		__writeToFile = true;
		__htmlBuffer = null;
		__htmlFile = new BufferedWriter(new FileWriter(filename, false));
	}

	__hasHeader = createHead;
	if (__hasHeader) {
		htmlStart();
		head(title);
		bodyStart();
	}

	// The following are easier to initialize here, rather than above
	// where they are declared
	__hL = new int[7];
	__hL[1] = 0;
	__hL[2] = 0;
	__hL[3] = 0;
	__hL[4] = 0;
	__hL[5] = 0;
	__hL[6] = 0;
}

/**
Constructor for an HTMLWriter that will write into HTML already produced by another HTMLWriter.
@param htmlWriter an existing HTMLWriter into which this HTML will be written.
*/
public HTMLWriter(HTMLWriter htmlWriter) 
throws Exception {
	__writeToFile = htmlWriter.getWriteToFile();
	__htmlBuffer = htmlWriter.getStringBuffer();
	__htmlFile = htmlWriter.getBufferedWriter();
	__hasHeader = htmlWriter.getHasHeader();
	__checkText = htmlWriter.checkTextForMetaCharacters();

	// The following are easier to initialize here, rather than above where they are declared
	__hL = new int[7];
	__hL[1] = 0;
	__hL[2] = 0;
	__hL[3] = 0;
	__hL[4] = 0;
	__hL[5] = 0;
	__hL[6] = 0;

	__subWriter = true;
}

/**
Cleans up member variables.  If the HTML has not been closed yet (i.e., not
written to disk), this will attempt to do so.  This may be unreliable, so do not rely on it.
*/
public void finalize() 
throws Throwable {
	if (!__subWriter) {
		if (!__closed) {
			closeFile();
			__closed = true;
		}
		
		__htmlFile = null;
		__hL = null;
		__htmlBuffer = null;
	}
	
	super.finalize();
}

/**
Writes the given text to the HTML without appending a newline.  addText()
appends a newline after the text is added -- this can mess up links in IE.
@param s the text to write to the HTML.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void addLinkText(String s)
throws Exception {
	if (__checkText) {
		write(textToHtml(s));
	}
	else {
		write(s);
	}
}

/**
Writes the given text to the HTML.  
@param s the text to write to the HTML.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void addText(String s)
throws Exception {
	if (__checkText) {
		write(textToHtml(s) + "\n");
	}
	else {
		write(s + "\n" );
	}
}

/**
Creates an anchor tag with the given anchor name.
@param s the anchor name for the tag.
@see <a href="http://www.willcam.com/cmat/html/other.html#Anchor">&lt;A&gt; tag</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void anchor(String s)
throws Exception {
	write("<a name=\"");
	addLinkText(s);
	write("\">");
}

/**
Inserts the end of the anchor.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void anchorEnd()
throws Exception {
    __aL--;
    write("</a>");
}

/**
Inserts the start of the anchor with the given name.
@param s the anchor name.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void anchorStart(String s)
throws Exception {
    __aL++;
    write("<a name=\"");
    addLinkText(s);
    write("\"");
}

/**
Creates a BLOCKQUOTE tag around the given String.
@param s the String to enclose in a BLOCKQUOTE tag.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Block%20Quote">
&lt;BLOCKQUOTE&gt; tag.</a> 
@throws Exception if an error occurs writing HTML text to a file.
*/
public void blockquote(String s)
throws Exception {
	write("<blockquote>");
	addText(s);
	write("</blockquote>\n");
}

/**
Closes a BLOCKQUOTE tag.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Block%20Quote">
&lt;BLOCKQUOTE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void blockquoteEnd()
throws Exception {
	if (__blockquoteL == 0) {
		return;
	}
	__blockquoteL--;
	write("</blockquote>\n");
}

/**
Opens a BLOCKQUOTE tag.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Block%20Quote">
&lt;BLOCKQUOTE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void blockquoteStart()
throws Exception {
	blockquoteStart("");
}

/**
Opens a BLOCKQUOTE tag with the given proplist values supplying the 
BLOCKQUOTE parameters.
@param p PropList of the BLOCKQUOTE parameters.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Block%20Quote">
&lt;BLOCKQUOTE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void blockquoteStart(PropList p)
throws Exception {
	blockquoteStart(propListToString(p));
}

/**
Opens a BLOCKQUOTE tag with the string supplying the BLOCKQUOTE parameters.
@param s the BLOCKQUOTE parameters.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Block%20Quote">
&lt;BLOCKQUOTE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void blockquoteStart(String s)
throws Exception {
	if (s.trim().equals("")) {
		write("<blockquote>");
	}
	else {
		write("<blockquote " + s + ">");
	}
	__blockquoteL++;
}

/**
Ends a BODY declaration.
@see <a href="http://www.willcam.com/cmat/html/toplevel.html#Body">
&lt;BODY&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void bodyEnd()
throws Exception {
	if (__bodyL == 0) {
		return;
	}
	__bodyL--;
	write("\n</body>\n");
}

/**
Starts a body declaration and assigns the values in the proplist to the BODY.
@param p the values to use as the BODY parameters.
@see <a href="http://www.willcam.com/cmat/html/toplevel.html#Body">
&lt;BODY&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void bodyStart(PropList p)
throws Exception {
	bodyStart(propListToString(p));
}

/**
Starts a BODY declaration.
@see <a href="http://www.willcam.com/cmat/html/toplevel.html#Body">
&lt;BODY&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void bodyStart()
throws Exception {
	bodyStart("");
}

/**
Starts a BODY declaration and appends the String to the BODY tag.
@param s the BODY parameters.
@see <a href="http://www.willcam.com/cmat/html/toplevel.html#Body">
&lt;BODY&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void bodyStart(String s)
throws Exception {
	__bodyL++;
	write("<body");
	if (s.trim().equals("")) { 
		write(">\n");
	}
	else {
		write(s + ">\n");
	}
}

/**
Puts the given text in inside a set of B bold tags.
@param s the text to bold.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Bold">
&lt;B&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void bold(String s)
throws Exception {
	write("<b>");
	addText(s);
	write("</b>");
}

/**
Ends a bold text section
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Bold">
&lt;B&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void boldEnd()
throws Exception {
	if (__bL == 0) {
		return;
	}
	write("</b>");
	__bL--;
}

/**
Starts a bold text section.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Bold">
&lt;B&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void boldStart()
throws Exception {
	write("<b>");
	__bL++;
}

/**
Inserts a line break.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Line%20Break">
&lt;BR&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void breakLine()
throws Exception {
	write("<br>\n");
}

/**
Ends an bulleted list declaration.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Unordered%20List">
&lt;UL&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void bulletedListEnd()
throws Exception {
	if (__ulL == 0) {
		return;
	}
	write("\n</ul>\n");
	__ulL--;
}

/**
Starts an bulleted list declaration.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Unordered%20List">
&lt;UL&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void bulletedListStart()
throws Exception {
	__ulL++;
	write("\n<ul>\n");
}

/**
Adds the given string as a table caption.
@param s the String to caption.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Caption">
&lt;CAPTION&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void caption(String s)
throws Exception {
	write("<caption>");
	addText(s);
	write("</caption>\n");
}

/**
Ends a table caption.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Caption">
&lt;CAPTION&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void captionEnd()
throws Exception {
	if (__captionL == 0) {
		return;
	}
	write("</caption>\n");
}

/**
Starts a table caption.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Caption">
&lt;CAPTION&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void captionStart()
throws Exception {
	__captionL++;
	write("<caption>");
}

/**
Returns whether to check text for metacharacters.
@return whether to check text for metacharacters.
*/
public boolean checkTextForMetaCharacters() {
	return __checkText;
}

/**
Converts normal text to HTML, where special characters such as &lt;, &gt;, &amp;, and &quot; are
replaced with html escape codes for these characters.
@param s a String of text to search for special characters.
@return the String that was passed in to the method with HTML escape codes for the special characters.
*/
private String textToHtml(String s)
{
	int length = s.length();
	char ch;
	String rep = "";
	String front = "";
	String back = "";
	boolean replace = false;
	boolean spaceAfterSpace = false;
	
	for (int i = 0; i < length; i++) {
		ch = s.charAt(i);
	
		if (ch == '>') {
			rep = "&gt;";
			replace = true;
		}
		else if (ch == '<') {
		 	rep = "&lt;";
			replace = true;
		}
		else if (ch == ' ') {
			if (spaceAfterSpace) {
				rep = "&nbsp;";
				replace = true;
			}
			else {
				spaceAfterSpace = true;
				replace = false;
			}
		}
		else if (ch == '&') {
			rep = "&amp;";
			replace = true;
		}
		else if (ch == '\'') {
			rep = "&rsquo;";
			replace = true;
		}
		//else if (ch == '-') {
		//	rep = "&ndash;";
		//	replace = true;
		//}
		else if (ch == '"') {
			rep = "&quot;";
			replace = true;
		}
		else {
			spaceAfterSpace = false;
			replace = false;
		}

		if (replace) {
			front = s.substring(0, i);
			if (i < (length - 1)) {
				back = s.substring((i + 1), length);
			}
			else {
				back = "";
			}
		
			s = front + rep + back;
		
			length = s.length();
			i += rep.length() - 1;
		}
	}

	// FIXME SAM 2009-04-21 Evaluate the following - should not do anything special here for check file HTML
	// Do other checks related to check files
	// shouldn't affect anything unless they have a special
	// sequence of characters
	String tmp = s.replaceAll("%font_red", "<b><font color=red>");
	s = tmp.replaceAll("%font_end", "</font></b>");
	
	// Replace tooltips with HTML title strings
	if ( s.indexOf( "%tooltip" ) >= 0 ) {
		tmp = s.replaceAll("%tooltip_start", 
		// Need to wrap into a paragraph element otherwise
		// the title attribute cannot be used to create the tooltip hover capability.
		"<p title=\"");
		s = tmp.replaceAll("%tooltip_end", "\">");
		s = s + "</p>";
	}
	
	return s;
}

/**
Sets whether text should be checked for metacharacters prior to being written to the HTML.
@param check if true, text such as &gt; will be translated to "\&amp;gt;".
*/
public void checkTextForMetacharacters(boolean check) {
	__checkText = check;
}

/**
Closes all tags that are currently open.  It does this in a non-intelligent
fashion -- tags are tracked as they are opened and closed and then in a rough
ordering scheme closing tags are added to the HTML.  Don't rely on this; it 
will probably be changed to an error-checking routine in the future.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void closeAllOpenTags()
throws Exception {
	for (; __commentL > 0; __commentL--) {
		write("-->");
	}

	for (; __fontL > 0; __fontL--) {
		write("</font>");
	}
	for (; __tdL > 0; __tdL--) {
		write("</td>");
	}
	for (; __thL > 0; __thL--) {
		write("</th>");
	}
	for (; __trL > 0; __trL--) {
		write("</tr>");
	}
	for (; __tableL > 0; __tableL--) {
		write("</table>");
	}
	for (; __captionL > 0; __captionL--) {
		write("</caption>");
	}
	for (int i = 1; i < 7; i++) {
		for (; __hL[i] > 0; __hL[i]--) {
			write("</h" + i + ">");
		}
	}
	for (; __liL > 0; __liL--) {
		write("</li>");
	}
	for (; __olL > 0; __olL--) {
		write("</ol>");
	}
	for (; __ulL > 0; __ulL--) {
		write("</ul>");
	}	
	for (; __dtL > 0; __dtL--) {
		write("</dt>");
	}
	for (; __ddL > 0; __ddL--) {
		write("</dd>");
	}
	for (; __dlL > 0; __dlL--) {
		write("</dl>");
	}	
	for (; __aL > 0; __aL--) {
		write("</a>");
	}
	for (; __bL > 0; __bL--) {
		write("</b>");
	}
	for (; __iL > 0; __iL--) {
		write("</i>");
	}	
	for (; __uL > 0; __uL--) {
		write("</u>");
	}	
	for (; __blockquoteL > 0; __blockquoteL--) {
		write("</blockquote>");
	}
	for (; __pL > 0; __pL--) {
		write("</p>");
	}
	for (; __preL > 0; __preL--) {
		write("</pre>");
	}
	for (; __ttL > 0; __ttL--) {
		write("</tt>");
	}
	for (; __subL > 0; __subL--) {
		write("</sub>");
	}
	for (; __supL > 0; __supL--) {
		write("</sup>");
	}
	for (; __nobrL > 0; __nobrL--) {
		write("</nobr>");
	}
	write("\n");
}

/**
Closes an HTML file and writes it to disk.  If the HTMLWriter was constructed 
with a header (i.e., BODY and HTML tags), this method will add the matching
&lt;/BODY&gt; and &lt;/HTML&gt; tags at the bottom of the HTML document.  
Otherwise, no &lt;/BODY&gt; and &lt;/HTML&gt; tags will be written to the
end of the document.  The document will then be flushed to disk and written.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void closeFile()
throws Exception {
	if (__subWriter) {
		return;
	}
	if (__hasHeader) {
		bodyEnd();
		htmlEnd();
	}
	if (__writeToFile) {
		__htmlFile.close();
	}
	__closed = true;
}

/**
Adds the given string in "code" to the HTML.
@param s the String to output in code format.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void code(String s)
throws Exception {
    write("<code>");
    addText(s);
    write("</code>");
}

/**
Puts the given text inside a comment.
@see <a href="http://www.willcam.com/cmat/html/other.html#Comment">
&lt;COMMENT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void comment(String s)
throws Exception {
	write("<!-- " + s + " -->\n");
}

/**
Stops commenting.
@see <a href="http://www.willcam.com/cmat/html/other.html#Comment">
&lt;COMMENT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void commentEnd()
throws Exception {
	if (__commentL == 0) {
		return;
	}
	__commentL--;
	write("-->\n");
}

/**
Starts a comment.
@see <a href="http://www.willcam.com/cmat/html/other.html#Comment">
&lt;COMMENT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void commentStart()
throws Exception {
	__commentL++;
	write("<!--\n");
}

/**
Adds the given definition to a definition list.
@param s the string to use as the definition.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DD&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definition(String s)
throws Exception {
	if (__dlL == 0) {
		return;
	}
	write("<dd>");
	addText(s);
	write("</dd>\n");
}

/**
Adds the given definition term and definition to a definition list.
@param term the term to define.
@param def the definition of the term.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DL&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definition(String term, String def)
throws Exception {
	if (__dlL == 0) {
		return;
	}
	write("<dt>" + term + "</dt>\n");
	write("<dd>" + def + "</dd>\n");
}

/**
Ends a definition.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DD&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definitionEnd()
throws Exception {
	if (__ddL == 0) {
		return;
	}
	__ddL--;
	write("</dd>\n");
}

/**
Starts a definition.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DD&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definitionStart()
throws Exception {
	if (__dlL == 0) {
		return;
	}
	__ddL++;
	write("<dd>");
}

/**
Ends a definition list.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DL&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definitionListEnd()
throws Exception {
	if (__dlL == 0) {
		return;
	}
	__dlL--;
	write("</dl>\n");
}

/**
Starts a definition list.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DL&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definitionListStart()
throws Exception {
	definitionListStart("");
}

/**
Starts a definition list and uses the values in the proplist as the parameters.
@param p PropList containing the definition list parameters.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DL&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definitionListStart(PropList p)
throws Exception {
	definitionListStart(propListToString(p));
}

/**
Starts a definition list and uses the string as the string of parameters.
@param s the parameter list.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DL&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definitionListStart(String s)
throws Exception {
	__dlL++;
	if (s.trim().equals("")) {
		write("<dl>\n");
		return;
	}
	write("<dl " + s + ">\n");
}

/**
Puts the given string in a definition list as a term.
@param s the definition term
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definitionTerm(String s)
throws Exception {
	if (__dlL == 0) {
		return;
	}
	write("<dt>" + s + "</dt>\n");
}

/**
Ends a definition term.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definitionTermEnd()
throws Exception {
	if (__dtL == 0) {
		return;
	}
	__dtL--;
	write("</dt>\n");
}

/**
Starts a definition term.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Definition%20List">
&lt;DT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void definitionTermStart()
throws Exception {
	if (__dlL == 0) {
		return;
	}
	__dtL++;
	write("<dt>");
}

/**
@deprecated use closeFile() instead
*/
public void endHTML()
throws Exception {
	closeFile();
}

/**
Starts a new font with the parameters in the proplist.
@param p PropList containing the Font's parameters.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Font">
&lt;FONT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void fontStart(PropList p)
throws Exception {
	fontStart(propListToString(p));
}

/**
Ends a font declaration.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Font">
&lt;FONT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void fontEnd()
throws Exception {
	if (__fontL == 0) {
		return;
	}
	__fontL--;
	write("</font>");
}

/**
Starts a font with the parameters in the given String.
@param s String containing the font parameters.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Font">
&lt;FONT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void fontStart(String s)
throws Exception {
	__fontL++;
	write("<font " + s + ">");
}

/**
Returns the BufferedWriter used to write HTML.
@return the BufferedWriter used to write HTML.
*/
protected BufferedWriter getBufferedWriter() {
	return __htmlFile;
}

/**
Returns whether there is a header.
@return whether there is a header.
*/
protected boolean getHasHeader() {
	return __hasHeader;
}

/**
Returns the HTML that was written to memory, or null if no HTML was/has been written to memory.
@return a String of HTML.
*/
public String getHTML() {
	if (__htmlBuffer == null) {
		return null;
	}
	else {
		return __htmlBuffer.toString();
	}
}

/**
Returns the StringBuffer used to write HTML.
@return the StringBuffer used to write HTML.
*/
protected StringBuffer getStringBuffer() {
	return __htmlBuffer;
}

/**
Returns whether HTML is being written to a file or not.
@return whether HTML is being written to a file or not.
*/
protected boolean getWriteToFile() {
	return __writeToFile;
}

/**
Creates a HEAD tag and uses the given String as the title of the page.
@param s the title to set the page to.  Can be null.
@see <a href="http://www.willcam.com/cmat/html/toplevel.html#Head">
&lt;HEAD&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void head(String s)
throws Exception {
	if (s == null) {
		s = "";
	}
	write("<head>\n");
	title(s);
	headEnd();
}

/**
Creates a HEAD tag and uses the given title String as the title of the page
and the arrays (of 2 Strings) passed in for adding 1 META data tags. 
The first item in each array is used to assign the data for NAME part of 
the META tag and the second item in each array is assign the data to content part of the meta tag. 
(see description of below method for more details).
@param s the title to set the page to.  Can be null.
@param arrMeta1 the array of 2 String to use for the first META tag.  
arrMeta1[0] is used to assign the NAME part of the META tag and 
arrMeta1[1] is used to assign the data that goes after the content 
@throws Exception if an error occurs writing HTML text to a file.
*/
public void head(String s, String[] arrMeta1 ) throws Exception {
	head( s, arrMeta1,  new String[]{"",""} );
}


/**
Creates a HEAD tag and uses the given title String as the title of the page
and the 2 arrays (of 2 Strings) passed in for adding 2 META data tags. 
The first item in each array is used to assign the data for NAME part of 
the META tag and the second item in each array is assign the data to 
content part of the META tag. For example, for typical META tags such as the ones below:
<PRE>
<P><I><META NAME="keywords" content="hydrology, reservoir"></I>
<BR><I><META NAME="description" content="Project analyzed the ..."></I>
<table>
<tr>
<TH>Part of Meta Tag</TH><TH>Value in Array</TH><TH>Value in Meta Tag</TH></tr>
<tr><td COLSPAN="3">First META TAG</td></tr>
<tr><td>name</td><td>arrMeta1[0]</td><td>"keywords"</td></tr>
<tr> <td>content</td><td>arrMeta1[1]</td><td>"hydrology, reservoir"</td></tr>
<tr><td colspan="3">Second META TAG</td></tr>
<tr><td>name</td><td>arrMeta2[0]</td><td>"description"</td></tr>
<tr><td>content</td><td>arrMeta2[1]</td><td>"Project analyzed the hydrological..."</td></tr>
</tr></table></P></PRE>
@param s the title to set the page to.  Can be null.
@param arrMeta1 the array of 2 String to use for the first META tag.  
arrMeta1[0] is used to assign the NAME part of the META tag and 
arrMeta1[1] is used to assign the data that goes after the content 
part of the META tag.
@param arrMeta2 the array of 2 String to use for a second META tag.  
arrMeta2[0] is used to assign the NAME part of the META tag and 
arrMeta2[1] is used to assign the data that goes after the content 
part of the META tag.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void head(String s, String[] arrMeta1, String[] arrMeta2 )
throws Exception {
	if (s == null) {
		s = "";
	}
	String meta_name1 = null;
	String meta_content1 =null;
	String meta_name2 = null;
	String meta_content2 =null;

	meta_name1 = arrMeta1[0];
	meta_content1 = arrMeta1[1];

	meta_name2 = arrMeta2[0];
	meta_content2 = arrMeta2[1];

	if (( meta_name1 == null ) || ( meta_name1.length() <= 0 )) {
		//don't write any meta data...	
		write("<head>\n  <title>" + s + "</title>\n</head>\n");
	}
	else if (( meta_name2 == null ) || ( meta_name2.length() <= 0 )) {
		//just write first meta tag
		write("<head>\n  <title>" + s + "</title>\n <meta name=\"" +
		meta_name1 + "\" content =\"" + meta_content1 + "\">\n</head>\n");
	}
	else {
		//just write both meta tags
		write("<head>\n  <title>" + s + "</title>\n <meta name=\"" +
		meta_name1 + "\" content =\"" + meta_content1 + "\">\n" +
		" <meta name=\"" + meta_name2 + "\" content =\"" + 
		meta_content2 + "\">\n </head>\n");
	}
}

/**
Head end tag.
 * @throws Exception
 */
public void headEnd() throws Exception
{
	write("</head>\n");
}

/**
Writes a header end tag based on the size input needed.
Size of 1 refers to creating an H1 tag.
Size of 2 creates a H2 tag, and so on.
This is based on the valid sizes for HTML header tags.
@param size The size of the header tag.  1 is largest, 6 is smallest.
@param id The id to use for this tag if one is to be assigned.
@throws Exception if file can't be written to.
 */
public void headerEnd ( int size ) throws Exception
{
	// check for valid tag
	if ( size > 0 && size < 7 )
	{
		write( "</h" + size + ">\n" );
	}
}

/**
Writes a header start tag based on the size input needed.  
Size of 1 refers to creatingan H1 tag.
Size of 2 creates a H2 tag, and so on.
This is based on the valid sizes for HTML header tags.
@param size The size of the header tag.  1 is largest, 6 is smallest.
@param id The id to use for this tag if one is to be assigned.
@throws Exception if file can't be written to.
 */
public void headerStart( int size ) throws Exception
{
	// check for valid tag
	if ( size > 0 && size < 7 )
	{
		write( "<h" + size + ">\n" );
	}
}

/**
Writes a header tag based on the size input needed.
Size of 1 refers to creating an H1 tag.  
Size of 2 creates a H2 tag, and so on.
This is based on the valid sizes for HTML header tags.
@param int size - The size of the header tag.  1 is largest, 6 is smallest.
@param PropList p - Properties to assign to this tag.
@throws Exception if file can't be written to. 
 */
public void headerStart( int size, PropList p ) throws Exception
{
	String prop = propListToString( p );
	// check for valid tag
	if ( size > 0 && size < 7 )
	{
		if ( prop == null || prop.length() == 0 ) {
			headerStart( size );
		}
		else {
			write("<h" + size + " " + prop + ">\n");
		}
	}
}

/**
Creates a heading for the given size (1 is bigger, 6 is smallest) and the given text.
@param number the kind of heading (1-6) to make.
@param s the string to store in the heading.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Heading%201">
&lt;HX&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void heading(int number, String s)
throws Exception {
	if (number < 1 || number > 6) {
		addText(s);
		return;
	}
	__hL[number]++;
	write("\n<h" + number + ">" + s + "</h" + number + ">\n");
}

/**
Ends a heading.
@param number the kind of heading (1-6) to end.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Heading%201">
&lt;HX&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void headingEnd(int number)
throws Exception {
	if (number < 1 || number > 6) {
		return;
	}
	__hL[number]--;
	write("</h" + number + ">\n");
}

/**
Starts a heading section with the given size number.
@param number the kind of heading (1-6) to make.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Heading%201">
&lt;HX&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void headingStart(int number)
throws Exception {
	headingStart(number, "");
}

/**
Starts a heading section with the given size number and parameters.
@param number the kind of heading (1-6) to make.
@param p PropList containing the parameters for the heading.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Heading%201">
&lt;HX&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void headingStart(int number, PropList p)
throws Exception {
	headingStart(number, propListToString(p));
}

/**
Starts a heading section with the given size number and parameters.
@param number the kind of heading (1-6) to make.
@param s string of the parameters for the heading.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Heading%201">
&lt;HX&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void headingStart(int number, String s)
throws Exception {
	if (number < 1 || number > 6) {
		return;
	}
	__hL[number]++;
	if (s.trim().equals("")) {
		write("\n<h" + number + ">");
		return;
	}
	write("\n<h" + number + " " + s + ">");
}

/**
Start tag for head tag element
@throws Exception
 */
public void headStart() throws Exception
{
	write("<head>\n");
}

/**
Creates a horizontal rule across the page.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Horizontal%20Rule">
&lt;HR&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void horizontalRule()
throws Exception {
	write("\n<hr>\n");
}

/**
Encodes the String passed in so that special characters
are encoded for HTML.  For example: a ";" is changed to: "&amp;"
@param s String to encode
@return encoded string
*/
public String encodeHTML( String s ) {
	return textToHtml(s);
}

/**
Ends an HTML declaration.
@see <a href="http://www.willcam.com/cmat/html/toplevel.html#HTML">
&lt;HTML&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void htmlEnd()
throws Exception {
	if (__htmlL == 0) {
		return;
	}
	if (__bodyL > 0) {
		bodyEnd();
	}
	__htmlL--;
	write("</html>\n");
}

/**
Starts an HTML declaration.  Also add a DTD line for strict:
<pre>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
</pre>
@see <a href="http://www.willcam.com/cmat/html/toplevel.html#HTML">
&lt;HTML&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void htmlStart()
throws Exception {
	__htmlL++;
	write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
	write("<html>\n");
}

/**
Inserts an image tag with the given parameters.
@param p PropList containing image tag parameters.
@see <a href="http://www.willcam.com/cmat/html/other.html#Inline%20Image">
&lt;IMG&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void image(PropList p)
throws Exception {
	image(propListToString(p));
}

/**
Inserts an image tag using the image at the path passed in.
@param s Path to image.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void image(String s)
throws Exception {
	image( s, "", 0, -999, -999, "" );
}

/**
Inserts an image tag using the image at the path passed in and creates
an alt tag with the second string passed in.
@param s Path to image.
@param alt_str Text for alt Tag
@throws Exception if an error occurs writing HTML text to a file.
*/
public void image( String s, String alt_str )
throws Exception {
	image( s, alt_str.trim(), 0, -999, -999, "" );
}

/**
Inserts an image tag using the image at the path passed in and creates
an alt tag with the second string passed in.
@param s Path to image.
@param alt_str Text for alt Tag
@param border Int to use to set border around image.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void image( String s, String alt_str, int border )
throws Exception {
	image( s, alt_str.trim(), border, -999, -999, "" );
}

/**
Inserts an image tag with image path, alt tag, and border information.
@param s Path to image.
@param alt_str Text for alt Tag.  If "", will write "" as alt Tag.
@param border Int to use to set border around image.
@param width Int to indicate width.  Pass in -999 to not specify.
@param height Int to indicate heigth.  Pass in -999 to not specify.
@param floatStr "left" or "right" to indicate image float with 
margins of 10 pixels on all sides of the image.
@throws Exception if an error occurs writing HTML text to a file.
*/
private void image( String s, String alt_str, int border, 
	int width, int height, String floatStr ) throws Exception {
	if (s.trim().equals("")) {
		return;
	}
	if ( floatStr.equals("") ) {
		if ( ( width > 0 )  & ( height > 0 ) ) {
			write("<img src= \"" + s + "\" alt=\"" + 
			alt_str + "\" border=\"" + border + 
			"\" width=\"" + width + "\" height=\"" +
			height + "\">" );
		}
		else if ( ( width > 0 )  & ( height < 0 ) ) {
			write("<img src= \"" + s + "\" alt=\"" + 
			alt_str + "\" border=\"" + border + 
			"\" width=\"" + width + "\">" );
		}
		else if ( ( width < 0 )  & ( height > 0 ) ) {
			write("<img src= \"" + s + "\" alt=\"" + 
			alt_str + "\" border=\"" + border + 
			"\" height=\"" + height + "\">" );
		}
		else { //width and height are -999
			write("<img src= \"" + s + "\" alt=\"" + 
			alt_str + "\" border=\"" + border + "\">" );
		}
	}
	else {
		if ( ( width > 0 )  & ( height > 0 ) ) {
			write("<img src= \"" + s + "\" alt=\"" + 
			alt_str + "\" border=\"" + border + 
			"\" width=\"" + width + "\" height=\"" +
			height +"\" align=\"" + floatStr + "\" " +
			"STYLE=\"margin-left:10;margin-right:10;" +
			"margin-top:10;margin-bottom:10\">" );
		}
		else if ( ( width > 0 )  & ( height < 0 ) ) {
			write("<img src= \"" + s + "\" alt=\"" + 
			alt_str + "\" border=\"" + border + 
			"\" width=\"" + width + "\" align=\"" +
			floatStr +"\" " +
			"STYLE=\"margin-left:10;margin-right:10;" +
			"margin-top:10;margin-bottom:10\">" );
		}
		else if ( ( width < 0 )  & ( height > 0 ) ) {
			write("<img src= \"" + s + "\" alt=\"" + 
			alt_str + "\" border=\"" + border + 
			"\" height=\"" + height + "\" align=\"" +
			floatStr +"\" " +
			"STYLE=\"margin-left:10;margin-right:10;" +
			"margin-top:10;margin-bottom:10\">" );
		}
		else { //width and height are -999
			write("<img src= \"" + s + "\" alt=\"" + 
			alt_str + "\" border=\"" + border + "\" align=\"" +
			floatStr +"\" " +
			"STYLE=\"margin-left:10;margin-right:10;" +
			"margin-top:10;margin-bottom:10\">" );
		}
	}
}

/**
Inserts an image tag with image path, alt tag, and border information 
so that it floats to the Left of the text.
@param s Path to image.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void imageFloatLeft( String s  ) 
throws Exception {
	image ( s, "", 0, -999, -999, "left");
}

/**
Inserts an image tag with image path, alt tag, and border information 
so that it floats to the Left of the text.
@param s Path to image.
@param alt_str Text for alt Tag
@throws Exception if an error occurs writing HTML text to a file.
*/
public void imageFloatLeft( String s, String alt_str  ) throws Exception {
	image ( s, alt_str, 0, -999, -999, "left");
}

/**
Inserts an image tag with image path, alt tag, and border information 
so that it floats to the Left of the text.
@param s Path to image.
@param alt_str Text for alt Tag
@param border Int to use to set border around image.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void imageFloatLeft( String s, String alt_str, int border ) 
throws Exception {
	image ( s, alt_str, border, -999, -999, "left");
}

/**
Inserts an image tag with image path, alt tag, and border information 
so that it floats to the Left of the text.
@param s Path to image.
@param alt_str Text for alt Tag
@param border Int to use to set border around image.
@param width Int to indicate width.
@param height Int to indicate heigth.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void imageFloatLeft( String s, String alt_str, int border, int width, int height)
throws Exception {
	image ( s, alt_str, border, width, height, "left");
}

/**
Inserts an image tag with image path, alt tag, and border information 
so that it floats to the Right of the text.
@param s Path to image.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void imageFloatRight( String s )
throws Exception {
	image ( s, "", 0, -999, -999, "right");
}

/**
Inserts an image tag with image path, alt tag, and border information 
so that it floats to the Right of the text.
@param s Path to image.
@param alt_str Text for alt Tag
@throws Exception if an error occurs writing HTML text to a file.
*/
public void imageFloatRight( String s, String alt_str )
throws Exception {
	image ( s, alt_str, 0, -999, -999, "right");
}

/**
Inserts an image tag with image path, alt tag, and border information 
so that it floats to the Right of the text.
@param s Path to image.
@param alt_str Text for alt Tag
@param border Int to use to set border around image.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void imageFloatRight( String s, String alt_str, int border )
throws Exception {
	image ( s, alt_str, border, -999, -999, "right");
}

/**
Inserts an image tag with image path, alt tag, and border information 
so that it floats to the Right of the text.
@param s Path to image.
@param alt_str Text for alt Tag
@param border Int to use to set border around image.
@param width Int to indicate width.
@param height Int to indicate heigth.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void imageFloatRight( String s, String alt_str, int border, int width,
			int height) throws Exception {
	image ( s, alt_str, border, width, height, "right");
}

/**
Adds the given string in italics to the HTML.
@param s the String to italicize.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Italic">
&lt;I&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void italic(String s)
throws Exception {
	write("<i>");
	addText(s);
	write("</i>");
}

/**
Stops italicizing text.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Italic">
&lt;I&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void italicEnd()
throws Exception {
	if (__iL == 0) {
		return;
	}
	write("</i>");
	__iL--;
}

/**
Begings italicizing text.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Italic">
&lt;I&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void italicStart()
throws Exception {
	write("<i>");
	__iL++;
}

/**
Inserts a hyperlink to the given location with the given text.
@param aString the location to link to.
@param linkString the text to click on.
@see <a href="http://www.willcam.com/cmat/html/other.html#Anchor">
&lt;a href&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void link(String aString, String linkString)
throws Exception {
	write("<a href=\"" + aString + "\">");
	addLinkText(linkString);
	write("</a>\n");
}

/**
Inserts a hyperlink to the given location with the given text.
@param PropList p - List of properties for this tag.
@param aString the location to link to.
@param linkString the text to click on.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void link( PropList p, String aString, String linkString )
throws Exception {
	String prop = propListToString(p);
	if ( aString != null && aString.length() > 0 ) {
		write("<a " + prop + " href=" + aString + "> ");
	}
	else {
		write("<a " + prop + ">");
	}
	addLinkText( linkString );
	write("</a>\n");
}

/**
Ends a Hyperlink tag (the section that is clicked on to go to a link).
@see <a href="http://www.willcam.com/cmat/html/other.html#Anchor">
&lt;a href&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void linkEnd()
throws Exception {
	if (__aL == 0) {
		return;
	}
	__aL--;
	write("</a>");
}

/**
Inserts a hyperlink to the given location.
@param s the location to link to.
@see <a href="http://www.willcam.com/cmat/html/other.html#Anchor">
&lt;a href&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void linkStart(String s)
throws Exception {
	__aL++;
	write("<a href=\"" + s + "\">");
}

/**
Inserts a list item into a list.
@param s the item to insert in the list.
@see <a href="http://www.willcam.com/cmat/html/lists.html#List%20Item">
&lt;LI&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void listItem(String s)
throws Exception {
	if (__ulL == 0 && __olL == 0) {
		return;
	}
	__liL++;
	write("<li>");
	addText(s);
	write("</li>\n");
}

/**
Ends a list item declaration.
@see <a href="http://www.willcam.com/cmat/html/lists.html#List%20Item">
&lt;LI&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void listItemEnd()
throws Exception {
	if (__liL == 0) {
		return;
	}
	__liL--;
	write("</li>\n");
}

/**
Starts a list item declaration.
@see <a href="http://www.willcam.com/cmat/html/lists.html#List%20Item">
&lt;LI&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void listItemStart()
throws Exception {
	if (__ulL == 0 && __olL == 0) {
		return;
	}
	__liL++;
	write("<li>");
}

/**
Defines a block of text that has no line breaks.
@param s the text with no line breaks.
@see <a href="http://www.willcam.com/cmat/html/other.html#No%20Break">
&lt;NOBR&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void nobr(String s)
throws Exception {
	write("<nobr>" + s + "</nobr>");
}

/**
Ends a section of text with no line breaks.
@see <a href="http://www.willcam.com/cmat/html/other.html#No%20Break">
&lt;NOBR&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void nobrEnd()
throws Exception {
	if (__nobrL == 0) {
		return;
	}
	__nobrL--;
	write("</nobr>");
}

/**
Starts a section of text with no line breaks.
@see <a href="http://www.willcam.com/cmat/html/other.html#No%20Break">
&lt;NOBR&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void nobrStart()
throws Exception {
	write("<nobr>");
	__nobrL++;
}

/**
Starts a numbered list.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Ordered%20List">
&lt;OL&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void numberedListStart()
throws Exception {
	__olL++;
	write("\n<ol>\n");
}

/**
Ends a numbered list.
@see <a href="http://www.willcam.com/cmat/html/lists.html#Ordered%20List">
&lt;OL&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void numberedListEnd()
throws Exception {
	if (__olL == 0) {
		return;
	}
	__olL--;
	write("\n</ol>\n");
}

/**
Inserts a new paragraph.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Paragraph">
&lt;P&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void paragraph()
throws Exception {
	write("<p>\n");
}

/**
Inserts a new paragraph with the given text.
@param s the text to put in the paragraph.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Paragraph">
&lt;P&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void paragraph(String s)
throws Exception {
	write("<p>\n" + textToHtml(s) + "</p>\n");
}

/**
Ends a paragraph.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Paragraph">
&lt;P&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void paragraphEnd()
throws Exception {
	if (__pL == 0) {
		return;
	}
	__pL--;
	write("</p>\n");
}

/**
Starts a paragraph declaration.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Paragraph">
&lt;P&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void paragraphStart()
throws Exception {
	paragraphStart("");	
}

/**
Starts a paragraph declaration with the given parameters.
@param p the parameters to use in the paragraph declaration.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Paragraph">
&lt;P&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void paragraphStart(PropList p)
throws Exception {
	paragraphStart(propListToString(p));
}

/**
Starts a paragraph declaration with the given parameters.
@param s String containing the paragraph parameters.
@see <a href="http://www.willcam.com/cmat/html/pformat.html#Paragraph">
&lt;P&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void paragraphStart(String s)
throws Exception {
	__pL++;
	if ( s == null || s.trim().equals("")) {
		write("<p>\n");
	}
	else {
		write("<p " + s + ">\n");
	}
}

/**
Inserts a block of pre-formatted text.
@param s the text to be pre-formatted.
@see <a 
href="http://www.willcam.com/cmat/html/pformat.html#Preformatted%20Text">
&lt;PRE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void pre(String s)
throws Exception {
	write("<pre>");
	// don't call addToText on this one since pre should
	// print exactly what is shown.  No special characters
	// need to be substituted
	write(s);
	write("</pre>");
}

/**
Ends a block of pre-formatted text.
@see <a 
href="http://www.willcam.com/cmat/html/pformat.html#Preformatted%20Text">
&lt;PRE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void preEnd()
throws Exception {
	if (__preL == 0) {
		return;
	}
	__preL--;
	write("</pre>\n");
}

/**
Starts a block of pre-formatted text with the given parameters.
@param p the parameters for the pre-formatted text block.
@see <a 
href="http://www.willcam.com/cmat/html/pformat.html#Preformatted%20Text">
&lt;PRE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void preStart(PropList p)
throws Exception {
	preStart(propListToString(p));
}

/**
Starts a block of pre-formatted text.
@see <a 
href="http://www.willcam.com/cmat/html/pformat.html#Preformatted%20Text">
&lt;PRE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void preStart()
throws Exception {
	preStart("");
}

/**
Starts a block of pre-formatted text with the given parameters.
@param s String of the pre-formatted text parameters.
@see <a 
href="http://www.willcam.com/cmat/html/pformat.html#Preformatted%20Text">
&lt;PRE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void preStart(String s)
throws Exception {
	__preL++;
	if (s.trim().equals("")) {
		write("<pre>");
		return;
	}
	write("<pre " + s + ">");
}

/**
Takes a PropList and turns every key/value pair into a single string that 
contains <tt>key1=value1 key2=value2 ... keyN=valueN</tt> for each PropList
prop from 1 to N.
@param p PropList for which to get the properties.
@return a String with all the properties concatenated together and separated
by spaces.
@throws Exception if an error occurs writing HTML text to a file.
*/
private String propListToString(PropList p) {
	if (p == null) {
		return "";
	}
	int size = p.size();
	String s = "";
	String val = "";
	for (int i = 0; i < size; i++) {
		Prop prop = p.elementAt(i);
		val = prop.getValue();
		if (val.trim().equals("")) {
			s += prop.getKey();
		}
		else {
			s += prop.getKey() + "=" + prop.getValue() + " ";
		}
	}
	
	return s;
}


/**
Inserts a new quote.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void quote()
throws Exception {
	write("<q>\n");
}

/**
Inserts a new quote with the given text.
@param s the text to put in the quote.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void quote(String s)
throws Exception {
	write("<q>\n" + textToHtml(s) + "</q>\n");
}

/**
Ends a quote.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void quoteEnd()
throws Exception {
	write("</q>\n");
}

/**
Starts a quote declaration.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void quoteStart()
throws Exception {
	quoteStart("");	
}

/**
Starts a quote declaration with the given parameters.
@param p the parameters to use in the quote declaration.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void quoteStart(PropList p)
throws Exception {
	quoteStart(propListToString(p));
}

/**
Starts a quote declaration with the given parameters.
@param s String containing the quote parameters.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void quoteStart(String s)
throws Exception {
	if (s.trim().equals("")) {
		write("<q>\n");
	}
	else {
		write("<q " + s + ">\n");
	}
}

public void styleStart() throws Exception
{
	write("<style>\n");
}

public void styleEnd() throws Exception
{
	write("</style>\n");
}

/**
Inserts a block of subscripted text.
@param s the text to subscript
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Subscript">
&lt;SUB&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void subscript(String s)
throws Exception {
	write("<sub>");
	addText(s);
	write("</sub>");
}

/**
Ends a block of subscripted text.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Subscript">
&lt;SUB&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void subscriptEnd()
throws Exception {
	write("</sub>");
}

/**
Starts a block of subscripted text.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Subscript">
&lt;SUB&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void subscriptStart()
throws Exception {
	write("<sub>");
}

/**
Inserts a block of superscripted text.
@param s the text to superscript.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Superscript">
&lt;SUP&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void superscript(String s)
throws Exception {
	write("<sup>");
	addText(s);
	write("</sup>");
}

/**
Ends a block of superscripted text.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Superscript">
&lt;SUP&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void superscriptEnd()
throws Exception {
	write("</sup>");
}

/**
Starts a block of superscripted text.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Superscript">
&lt;SUP&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void superscriptStart()
throws Exception {
	write("<sup>");
}

/**
Ends a table declaration.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table">
&lt;TABLE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableEnd()
throws Exception {
	if (__tableL == 0) {
		return;
	}
	write("</table>\n");
}

/**
Starts a table declaration.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table">
&lt;TABLE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableStart()
throws Exception {
	tableStart("");
}

/**
Starts a table declaration with the given parameters.
@param p PropList of the table's parameters.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table">
&lt;TABLE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableStart(PropList p)
throws Exception {
	tableStart(propListToString(p));
}

/**
Starts a table declaration with the given parameters.
@param s String of the table parameters.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table">
&lt;TABLE&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableStart(String s)
throws Exception {
	__tableL++;
	if (s.trim().equals("")) {
		write("<table>\n");
		return;
	}
	write("<table " + s + ">\n");
}

/**
Starts a table declaration with the given parameters.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableStartFloatLeft()
throws Exception {
	tableStartFloatLeft("");
}

/**
Starts a table declaration with the given parameters.
@param p PropList of the table's parameters
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableStartFloatLeft(PropList p)
throws Exception {
	tableStartFloatLeft(propListToString(p));
}

/**
Starts a table declaration with the given parameters.
@param s String of the table parameters that floats to the left.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableStartFloatLeft(String s)
throws Exception {
	__tableL++;
	if (s.trim().equals("")) {
		write("<table align=\"left\">\n");
		return;
	}
	write("<table align=\"left\" " + s + ">\n");
}

/**
Inserts a cell into a table with the given text.
@param s the text to put into the cell.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Data">
&lt;td&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableCell(String s)
throws Exception {
	if (__trL == 0) {
		return;
	}
	write("<td>");
	addText(s.replaceAll("\n", ""));
	write("</td>\n");
}

/**
Inserts multiple cells into a table.
@param cells Array of Strings to write to each cell.
@throws Exception
 */
public void tableCells( String cells [] ) throws Exception
{	
	for(int i = 0; i < cells.length; i++ ) {
		if( cells[i] != null && cells[i].length() > 0 ) {
			tableCell( cells[i] );
		}
	}
}

/**
Inserts multiple cells into a table.
@param cells Array of Strings to write to each cell.
@param Property list for HTML attributes.
@throws Exception
 */
public void tableCells( String cells [], PropList props ) throws Exception
{	
	for(int i = 0; i < cells.length; i++ ) {
		if( cells[i] != null && cells[i].length() > 0 ) {
			tableCellStart( props );
			addText( cells[i] );
			tableCellEnd();
		}
	}
}


/**
Ends a table cell declaration.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Data">
&lt;td&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableCellEnd()
throws Exception {
	if (__tdL == 0) {
		return;
	}
	__tdL--;
	write("</td>\n");
}

/**
Starts a table cell declaration.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Data">
&lt;td&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableCellStart()
throws Exception {
	tableCellStart("");
}

/**
Starts a table cell declaration with the given properties.
@param p PropList of table cell properties.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Data">
&lt;td&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableCellStart(PropList p)
throws Exception {
	tableCellStart(propListToString(p));
}

/**
Starts a table cell declaration with the given parameters.
@param s String of table cell parameters.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Data">
&lt;td&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableCellStart(String s)
throws Exception {
	if (__trL == 0) {
		return;
	}
	__tdL++;
	if (s.trim().equals("")) {
		write("<td>");
		return;
	}
	write("<td " + s + ">");
}

/**
Inserts a table header cell with the given text.
@param s text to put in the table header cell.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Header">
&lt;TH&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableHeader(String s)
throws Exception {
	if (__trL == 0) {
		return;
	}
	write("<th>" + textToHtml( s.replaceAll("\n", "" ) ) + "</th>\n");
}

/**
Inserts multiple table headers with the Strings specified.
@param headers Array of Strings to use for each header.
@throws Exception
 */
public void tableHeaders(String [] headers ) throws Exception
{
	for(int i = 0; i < headers.length; i++ ) {
		if( headers[i] != null && headers[i].length() > 0 ) {
			tableHeader( headers[i].trim() );
		}
	}                                     
}

/**
Ends a table header cell declaration.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Header">
&lt;TH&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableHeaderEnd()
throws Exception {
	if (__thL == 0) {
		return;
	}
	__thL--;
	write("</th>\n");
}

/**
Starts a table header cell declaration.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Header">
&lt;TH&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableHeaderStart()
throws Exception {
	tableHeaderStart("");
}

/**
Starts a table header cell declaration with the given parameters.
@param p PropList of table header cell parameters.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Header">
&lt;TH&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableHeaderStart(PropList p)
throws Exception {
	tableHeaderStart(propListToString(p));
}

/**
Starts a table header cell declaration with the given parameters.
@param s String of table header cell parameters.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Header">
&lt;TH&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableHeaderStart(String s)
throws Exception {
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

/**
Ends a table row declaration.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Row">
&lt;tr&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableRowEnd()
throws Exception {
	if (__trL == 0) {
		return;
	}
	write("</tr>\n");
}

/**
Starts a table row declaration.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Row">
&lt;tr&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableRowStart()
throws Exception {
	tableRowStart("");
}

/**
Starts a table row declaration with the given parameters.
@param p the table row parameters.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Row">
&lt;tr&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableRowStart(PropList p)
throws Exception {
	tableRowStart(propListToString(p));
}

/**
Starts a table row declaration with the given parameters.
@param s String of the table row parameters.
@see <a href="http://www.willcam.com/cmat/html/tables.html#Table%20Row">
&lt;tr&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void tableRowStart(String s)
throws Exception {
	if (__tableL == 0) {
		return;
	}
	__trL++;
	if (s.trim().equals("")) {
		write("<tr>\n");
		return;
	}
	write("<tr " + s + ">\n");
}

/**
Inserts a block of teletype-formatted text.
@param s the teletype-formatted text to insert.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Teletype">
&lt;TT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void teletype(String s)
throws Exception {
	write("<tt>");
	addText(s);
	write("</tt>");
}

/**
Ends a teletype block declaration.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Teletype">
&lt;TT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void teletypeEnd()
throws Exception {
	if (__ttL == 0) {
		return;
	}
	write("</tt>");
	__ttL--;
}

/**
Starts a teletype block declaration.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Teletype">
&lt;TT&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void teletypeStart()
throws Exception {
	write("<tt>");
	__ttL++;
}

/**
Inserts the document title given the text.
@param s the title to insert.
@throws Exception if an error occurs writing HTML text to a file.
*/
public void title(String s)
throws Exception
{
    write("<title>");
    addText(s);
    write("</title>");
}

/**
Inserts a block of underlined text.
@param s the underlined text to insert.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Underlined">
&lt;U&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void underline(String s)
throws Exception {
	write("<u>");
	addText(s);
	write("</u>");
}

/**
Ends a block of underlined text.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Underlined">
&lt;U&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void underlineEnd()
throws Exception {
	if (__uL == 0) {
		return;
	}
	write("</u>");
	__uL--;
}

/**
Starts a block of underlined text.
@see <a href="http://www.willcam.com/cmat/html/lformat.html#Underlined">
&lt;U&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void underlineStart()
throws Exception {
	write("<u>");
	__uL++;
}

/**
Inserts a word break identifier, signifying where a word can be broken.
@see <a href="http://www.willcam.com/cmat/html/other.html#Word%20Break">
&lt;WBR&gt; tag.</a>
@throws Exception if an error occurs writing HTML text to a file.
*/
public void wordBreak()
throws Exception {
	write("<wbr>");
}

/**
Writes a String of text either to a file, or to an internal StringBuffer.
No checks are done on the content, so it should contain proper open and closing tags.
@param s the String to write.
@throws Exception if an error occurs writing to a file.
*/
public void write( String s )
throws Exception {
	if (__writeToFile) {
		__htmlFile.write(s);
	}
	else {
		__htmlBuffer.append(s);
	}
}

}