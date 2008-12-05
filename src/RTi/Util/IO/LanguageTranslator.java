//-----------------------------------------------------------------------------
// LanguageTranslator - manage translation file and perform String translations
//-----------------------------------------------------------------------------
// History:
//
// 2002-04-05	Morgan Sheedy, RTi	Initial Implementation- based on 
//					code written for Central Asia GUI
//					located on rogue:
//					/projects/ahps/nwsrfsgui/src/
//					RTi/NWSRFSGUI
//					Specifically see class: UnicodeUtil.
// 2002-10-13	Steven A. Malers, RTi	Review and clean up code for inclusion
//					into RTi.Util.IO.  In particular:
//					* update the Javadoc
//					* rename methods to be more
//					  straightforward
//					* decide which data and methods should
//					  be included in UnicodeUtil
//					* remove the use of StringUtil for 
//					  translation functionality
//					* Use a PropList for translations
//					  rather than a Vector that needs to
//					  be parsed each time.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.IO;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.String.UnicodeUtil;

/**
The LanguageTranslator class manages translations between English and other
languages, typically for use in user interfaces.  The translation database is
typically stored in a text file that resembles a generic PropList with entries
of the form:<br>
<pre>
	englishword = "translated word with unicode chars".
</pre><br>
For Example:<br>
<pre>
	forecast_title_string = "Pron\u00F3stico"
</pre><br>

where the \u00F3 translates to an accented "o".  Note that the unicode character
representations are simple ASCII characters ('\\', 'u', '0', '0', 'F', '3'), not
some visual representation of an actual binary unicode character.  The English
word is used as the key (or phrase) to search for the translated word.  The key
can be either a lookup string (e.g., an identifier corresponding to a component)
or a literal English string (e.g., the text label that is displayed for a
component).  Future development may enable features to internally assign
translation data or to use translation data in a database (without reading the
entire translation database into memory).  Currently, however, it is assumed
that a relatively small number of translations are required and the translation
database can reside in memory during the life of an application session.

The basic steps in utilizing this class are as follows:
<ol>
<li>	Create a translation file (referred to as a database once in memory).
	Example of lines from a translation file are:<br>
	<pre>
	word = "palabra" 
	station = "estaci\u00F3n"
	</pre>
	The createTranslationFile method is a utility method to help create such
	a file.
	</li>
<li>	Create an instance of this class (e.g., by calling the constructor and
	passing in the path to the translation file that consists of an English
	keyword set equal to the translated version).
	</li>
<li>	Set the instance of this class as the global translator that can be
	used throughout an application:<br>
	<pre>
	LanguageTranslator translator = new LanguageTranslator(
		"/opt/RTi/NWSRFS/system/NwsrfsGUI_Spanish.txt" );
	LanguageTranslator.setTranslator ( translator );
	</pre>
	</li>
<li>	When creating Strings in the application (e.g., for graphical user
	interface component labels), call:<br>
	<pre>
	LanguageTranslator.getTranslator()
	</pre>
	If the above method returns null, then translations will not be
	possible (or are not enabled).  Use this information to control how
	strings are processed in the application.
	</li>
<li>	To translate Strings, call the translate() method for the translator
	intance, which takes two arguments.  The first is the English "key"
	(left side of tranlation file data) that is used to look up the
	translation String.  The second argument is the default value for the
	tranlated String in case no translation found (specifying null indicates
	that a translation is required).  For example:<br>
	<pre>
		LanguageTranslator translator = new LanguageTranslator( 
			"/opt/RTi/NWSRFS/system/NwsrfsGUI_Spanish.txt" );
		LanguageTranslator.setTranslator ( translator );
		
		//later ...
		String close_string = null;
		LanguageTranslator translator =
			LanguageTranslator.getTranslator();
		if ( translator != null ) {
			// If the translation database has "close" on the left
			// side...
			close_string = translator.translate (
				"close", "Exit Application" );		
			// Or if the translation database has "Exit Application"
			// on the left side...
			close_string = translator.translate (
				"Exit Application", "Exit Application" );
		}
	</pre>
	</li>
</ol>
*/
public class LanguageTranslator {

/**
Static reference to a LanguageTranslator instance that can be used throughout
an application.  An application typically knows the location of its translation
file and creates an instance of LanguageTranslator.  Then, call
LanguageTranslator.setTranslator() with the resulting instance.  Application
code modules can then translate strings by getting the translator and using its
translate() method.
*/
private static LanguageTranslator __translator = null;

/**
PropList that holds all the phrases in a translation file.  The left side of
the translation is saved as the key and the right side as the contents/value
String.  The contents contain the unconverted string (the literal characters
from the translation file) and the value contains the Unicode string that can be
used for displays.
*/
private static PropList __translation_PropList = null;

/**
Construct a LanguageTranslator from a translation file.  The translation file is
read and each phrase is stored in a PropList.  Each line of the file is in
format:<br>
<pre>
station = "estaci\u00F3n"
<pre>
where the left side is a string key that is used to look up phrases to be
translated and the right side can contain Unicode characters represented by
ASCII characters.  
@param file_name Name of the file that contains translations.
@exception Exception if there is an error reading the file (e.g., the file name
passed in does not represent a file).
*/ 
public LanguageTranslator ( String file_name )
throws Exception
{	String routine = "LanguageTranslator";

	//see if the file_name represents a valid/readable file.
	if ( !IOUtil.fileExists( file_name ) ) {
		Message.printWarning( 2, routine,
		"Translation file \"" + file_name + "\" does not exist." );
		throw new Exception ( "Translation file \"" + file_name +
		"\" does not exist." );
	}

	readTranslationFile( file_name );
}

/**
Create a translation file from a spreadsheet translation table file.  This
utility method offers one approach to creating a translation file.
Reads in an existing file in format: 
<p>
<pre>
	english text( Latin characters) (new line) 
	non_latin characters text.
</pre>
Writes out: a new file with english and unicode text: 
<p>
<pre>
english text = "\u0042\u004a" 
</pre>
<p>

The file that is read in is assumed to have been created using a process similar
to the one outlined below:
<UL>
<LI>	Create spreadsheet in Microsoft Excel with English characters in one 
	column and the non-ascii characters in the next (how is this
	done?).</LI>
<LI>	Copy and paste the columns into Microsoft Word (specifically how -
	paste special?, choose encoding?).</LI>
<LI>	Verify that there are no column headings or blank lines at the top or
	bottom (so did we paste into a table?)</LI>
<LI>	Save from Microsoft Word as Unicode (.txt) (SAM does not see "Unicode"
	in the Save as type options in Word 2000?)</LI>
<LI>	Once saved, there are no longer 2 columns.  Instead the first line will
	be the English, ASCII text, and the second will be the Unicode for the
	non-ASCII text.  Lines will alternate according to this pattern.</LI>
<LI>	FTP the .txt document via BINARY ftp to the Unix box (what if not on
	UNIX?).</LI>
<LI>	Remove the first two extraneous characters that are displayed before the
	first word in the document after FTP-ing it to Unix (how? - why do we
	think that these are there?).</LI>
<li>	The translation file can then be used to as input to this method in
	order to create a translation file.</li>
</UL>
@param encoding String representation of encoding that the file_to_translate
uses (e.g., Cp1251 for Cyrillic; SJIS for ??).  What is the reference for these
encodings (where are they defined?).
@param file_to_translate Name of the file that needs to be converted to a
standard translation file.
@param file_to_create Name of the translation file to create.
@param reverse_bits Used to indicate if the file needs translated
from big endian to little endian or vice versa.  This occurs
when transferring files from PC to Unix (what does this mean? - so for the
above example do we use true?  What if the file is used on the PC - false?
I suspect this is because Java is Big Endian and PCs are Little Endian).
Note that if specified as true, a temporary file is created with the same name
as file_to_translate with _reversed in the name.
@exception Exception if an error occurs creating the translation file.
*/
public void createTranslationFile ( String encoding,
				String file_to_translate,
				String file_to_create,
				boolean reverse_bits ) throws Exception {

	String routine = "LanguageTranslator.createTranslationFile";
	if ( Message.isDebugOn ) {
		Message.printDebug( 100, routine, routine + " called with "+
		"encoding: \"" + encoding + "\" and file to " +
		"translate: \"" + file_to_translate + "\" and " +
		"file to create: \"" + file_to_create + "\"." );
	}
	
	String filename2 = null;
	char c1 = '\0', c2 = '\0';
	byte b1, b2;
	//used if going from PC to Unix
	if ( reverse_bits ) {
		filename2 = file_to_translate + "_reversed";
		//read in file_to_translate and write it out reversed
		EndianDataInputStream is = new EndianDataInputStream(
			new FileInputStream( file_to_translate ) );
		DataOutputStream os = new DataOutputStream( 
			new FileOutputStream( filename2 ) );

		//write out in reverse order
		while ( true ) {
			try {   c1 = is.readLittleEndianChar1();
				c2 = is.readLittleEndianChar1();
				// Write in reversed order...
				b1 = (byte)c1;
				b2 = (byte)c2;
				os.writeByte ( b2 );
				os.writeByte ( b1 );
			}
			catch ( Exception e ) {
			// End of file...
				is.close();
				os.close();
				break;
			}
		}
		
	}
	else {	// leave file_to_translate as is
		filename2 = file_to_translate;	
	}

	//make the files for both input and output
	File inputFile = null;
	//File outputFile = null;
	inputFile = new File ( filename2 );

	//now we have file to read and write already to go.
	StringBuffer buffer = new StringBuffer();
	if (( inputFile != null ) && (inputFile.canRead() )) {
		Message.printWarning ( 2, routine,
		"Cannot read input file \"" + inputFile + "\"" );
		throw new Exception ( 
		"Cannot read input file \"" + inputFile + "\"" );
	}
	//get output file ready
	FileOutputStream fos = new FileOutputStream( file_to_create );
	OutputStreamWriter osw = new OutputStreamWriter( fos, "UTF8" );
	PrintWriter pw = new PrintWriter( osw );

	//get input file ready
	FileInputStream fis = new FileInputStream( inputFile );
	//encoding from windows cyrillic is: Cp1251
	//encoding from windows English is 1252
	InputStreamReader isr = new InputStreamReader( fis, encoding );

	BufferedReader br = new BufferedReader( isr );
	String string;
	String unicode_str;
	int count = 0;

	String eng_word = null;
	String russ_unicode = null;	// Call it "russ" for Russian but can
					// be anything
	int p = 0;

	char c; // Character
	int ic, ic1 = 0, ic2 = 0;
	//Character as unicode integer
	int ccount;     // 1 for first char, 2 for second...
	while ( true ) {
		// Read a line...
		buffer.setLength(0);
		ccount = 1;
		while ( true ) {
			if ( ccount == 1 ) {
				ic1 = isr.read();
				ccount = 2;
				if ( ic1 < 0 ) {
					break;
				}
				c1 = (char)ic1;
				if ( Message.isDebugOn ) {
					Message.printDebug(15, routine,
					"Reading char " + "Unicode char=" +
					UnicodeUtil.toUnicodeString( c1) );
				}
			}
			else {  ic2 = isr.read();
				if ( ic2 < 0 ) {
					break;
				}
				c2 = (char)ic2;
				if ( Message.isDebugOn ) {
					Message.printDebug( 15, routine,
					"Reading char " + "Unicode char=" +
					UnicodeUtil.toUnicodeString( c2) );
				}
				// Take the 2 "characters" and combine into one
				// - why is this necessary???
				if( c2 == '\r' ) {
					// Don't add to buffer and expect to
					// get a '\n' next...
					ccount = 1;
					continue;
				}
				else if ( c2 == '\n' ) {
					// End of a line...
					break;
				}
				// Add to buffer...
				// For some reason it ?? (what?)
				ic = ic1*256 + ic2;
				c = (char)ic;
				if ( Message.isDebugOn ) {
					Message.printDebug( 15, routine,
					"Appending " + "Unicode char=" +
					UnicodeUtil.toUnicodeString(c) );
				}
				buffer.append ( c );
				ccount = 1;
			}
		}
		if ( ( ic1 < 0 ) || ( ic2 < 0 ) ) {
			//end of file
			break;
		}

		string = buffer.toString();
		p++;
		// Need 1st word- which should be the english word... Then the
		// translation of that word in unicode should be the 2nd unicode
		// translation after the english word.
		if ( p == 1 ) {
			eng_word = string;
			if ( Message.isDebugOn ) {
				Message.printDebug( 20, routine,
				"english word = \"" + eng_word + "\"");
			}
		}
		unicode_str = UnicodeUtil.toUnicodeString( string );
		if ( p == 2 ) {
			russ_unicode = unicode_str;
			p=0;
			if ( Message.isDebugOn ) {
				Message.printDebug( 20, routine,
				"russian unicode = " + russ_unicode );
			}
		}

		// all the strings
		if ( Message.isDebugOn ) {
			Message.printDebug( 15, routine, "inputString = \"" +
			string + "\"" );
			Message.printDebug( 15, routine, "Unicode String = \"" +
			unicode_str + "\"");
		}

		// What we need written to file is the first (english) word,
		// skip the next unicode string because that is the English word
		// in unicode, skip the next 'string' because that is the
		// russian word in engl, and then write the corresponding
		// unicode for that russian.  Now write to file
		if ( p == 0 ) {
			pw.println( eng_word + " = \"" +
			russ_unicode + "\"" );
		}
		/* Uncomment if really need to debug...
		if ( Message.isDebugOn ) {
			//now to hex
			String hexstring = "";
			for ( int i = 0; 
				i < string.length(); i++ ) {
				// This appears to be buggy, according
				// to the javasoft web site...
				//int unicode_num =
				//Character.getNumericValue(
				//	string.charAt(i));
				int unicode_num = (int)string.charAt(i);
				if ( unicode_num < 0 ) {
					hexstring = "";
				}
				else {	hexstring = Integer.toHexString(
					unicode_num );
					if ( hexstring.length()== 1 ) {
						hexstring = "000" + hexstring;
					}
					else if(hexstring.length()==2) {
						hexstring = "00" + hexstring;
					}
					else if(hexstring.length()==3) {
						hexstring= "0" + hexstring;
					}
				}
				if ( Message.isDebugOn ) {
					Message.printDebug( 15, routine,
					"Char is '" + string.charAt(i) +
					"' unicode_dec=" + unicode_num +
					" unicode_hex='\\u" + hexstring + "'" );
				}
			}
		}
		*/
		++count;
	}
	br.close();
	pw.close();
	osw.close();
}

/**
Returns the current instance of the LanguageTranslator.  This translator can be
used globally within an application to translate strings in the translation
database.
@return An instance of the current LanguageTranslator 
class to allow access to the <I>translate</I> method.  Return null if no valid
translator is in effect.
*/
public static LanguageTranslator getTranslator() {
	return __translator;
}

/**
Read in the translation file and store each line of the file as an item in
the translation PropList.  The literal string is stored in the contents and the
expanded Unicode string is stored in the value.  Consequently, the value can
be used immediately when found in the PropList.
@param fileToRead translation file to read.
@exception Exception if an error occurs reading the file.
*/
private void readTranslationFile( String fileToRead )
throws Exception {
	String routine = "LanguageTranslator.readTranslationFile";

	__translation_PropList = new PropList( "Language Props" );
	if ( __translation_PropList != null ) {
		if ( Message.isDebugOn ) {
			Message.printDebug( 10, routine, 
			"There are: " +  __translation_PropList.size() +
			" lines of translated information.");
		}
	}
	//open file, read in
	String s = null;

	File inputFile = new File( fileToRead );
	FileInputStream fis = new FileInputStream( inputFile );
	InputStreamReader isr = new InputStreamReader( fis );
	BufferedReader br = new BufferedReader( isr );
	String translated_right_str = null;
	String left_of_equals_str = null;
	String right_of_equals_str = null;
	List v=null;
	while ( true ) {
		s = br.readLine();
		if ( s == null ) {
			//no more lines
			break;
		}
		if ( s.startsWith("#")) {
			//ignore it b/c is comment
			continue;
		}
		else if ( s.length() <= 0 ) {
			//ignore it, it is an empty line
			continue;
		}
		else if ( s.indexOf("=") <= 0 ) {
			//ignore it because it is not formatted as expected. 
			//Expecting: "xxx=yyy"
			Message.printWarning( 15, routine,
			"Line: \"" + s + "\" is incorrectly formatted." );
			continue;
		}
		else {	// should be valid.  Break up line by the "="
			v = StringUtil.breakStringList( s, "=", StringUtil.DELIM_ALLOW_STRINGS);
			if (( v!=null ) && ( v.size() == 2 )) {
				left_of_equals_str = ((String)v.get(0)).trim();
				right_of_equals_str = ((String)v.get(1)).trim();
			}

			// Process the right hand side of string, which
			// will "translate" any unicode character sequences
			// ("\\u0034") into the actual Unicode character value.
			translated_right_str = UnicodeUtil.parseUnicode( 
				right_of_equals_str );
				
			// Make this a Prop before adding to PropList
			Prop p = new Prop( left_of_equals_str,
				right_of_equals_str,
				translated_right_str );

			// add to PropList
			__translation_PropList.set(p);
		}
	}

	//clean up
	br = null;
	isr = null;
	fis = null;
}

/**
Sets the current instance of the LanguageTranslator, which can be requested
using getTranslator(). 
@param translator Instance of LanguageTranslator.  A null translator is allowed
to turn off translations.
*/
public static void setTranslator( LanguageTranslator translator ) {
	__translator = translator;

	if ( __translator == null ) { 
		Message.printWarning( 5, "LanguageTranslator.setTranslator",
		"Instance of LanguageTranslator passed in was null. " +
		"No tranlsator set." );
	}
}

/**
Determine the translation for the requested string.
@return the translation for the requested string.  If a translation does not
exist in the translation database, return the default_translation string.
@param key String to use as the lookup key in the translation database.
@param default_translation String to return if there is no matching key
in the translation table (can be null).  Note that if the default translation
is null and no translation is found, null will be returned and the calling
application must decide to use the untranslated string.
*/
public String translate ( String key, String default_translation ) {
	if (	(__translation_PropList == null) ||
		(__translation_PropList.size() == 0) ) {
		// Return the default value...
		return default_translation;
	}

	// Search the PropList...

	String translated_str = __translation_PropList.getValue ( key );
	if ( translated_str == null ) {
		// Return the default value...
		return default_translation;
	}
	else {	// Return the found value...
		return translated_str;
	}
}
 
} // End LanguageTranslator
