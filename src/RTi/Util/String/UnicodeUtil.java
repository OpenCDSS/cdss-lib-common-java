// UnicodeUtil - utility classes to help with Unicode string translations

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

//-----------------------------------------------------------------------------
// UnicodeUtil - utility classes to help with Unicode string translations
//-----------------------------------------------------------------------------
// History:
//
// 2002-02-13 	Morgan Sheedy		Initial Implementation.
// 2002-02-12 	Steven A. Malers	Developed translateFileToEngUnicodeFile.
// 2002-10-13	SAM, RTi		Additional review and cleanup during
//					addition to the RTi.Util.String package.
//					Remove all unneeded imports.
// 2002-10-13	AML, RTi		Additional cleanup.
// 2002-10-17	SAM, RTi		Yet more cleanup.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.String;

import java.lang.StringBuffer;

import RTi.Util.Message.Message;

/**
This UnicodeUtil class provides utility methods to handle conversions needed
when using Unicode characters.   It is used to translate non-Latin characters
(English) into Unicode characters in order to display those characters in Java
Swing Components, which interpret the unicode character sequences.
the non-Latin translation.
*/

public class UnicodeUtil {
/**
Return an ASCII string representing the string argument.  For example, a
two-character string containing Unicode characters may result in the following
six-character ASCII string:  "\u1234\u5678".  This method is useful for
formatting internal Unicode strings for text files that can be viewed and
edited.  Reconstruction of the internal Unicode string can occur using the
parseUnicode() method.
@param string String to translate into the unicode character code.
@return a string consisting of the unicode character codes for the ASCII
string.
*/
public static String toUnicodeString ( String string ) {
	String routine = "UnicodeUtil.toUnicodeString";
	String result = "";
	for (int index = 0; index < string.length (); index++) {
		result = result + toUnicodeString (string.charAt (index));
		if ( Message.isDebugOn ) {
			Message.printDebug( 25, routine, "result: \"" +
			result + "\"." );
		} 
	}
	return result;
}

/**
Return an ASCII string representing the character argument.  For example, a
single Unicode character results in a six-character ASCII string similar to the
following:  "\u1234".  This method is useful for formatting internal Unicode
strings for text files that can be viewed and edited.  Reconstruction of the
internal Unicode string can occur using the parseUnicode() method.
@param character Single character to translate into the unicode character code.
@return a string consisting of the unicode character codes for the ASCII
string.
*/
public static String toUnicodeString ( char character ) {
	String routine = "UnicodeUtil.toUnicodeString";
	short unicode = (short) character;
	char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7',
		         '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	char[] array = { hexDigit[( unicode >> 12) & 0x0f],
			hexDigit[( unicode >> 8) & 0x0f],
		       	hexDigit[( unicode >> 4) & 0x0f],
			hexDigit[ unicode & 0x0f] };
	String result = "\\u" + new String (array);
	if ( Message.isDebugOn ) {
		Message.printDebug( 25, routine, "result: \"" +
		result + "\"." );
	} 
	return result; 
}

/**
Converts a String which may contain unicode characters
(eg chacters '\\','u','0','0','4','e' equivalent to String "\\u004e") 
and regular, ASCII characters into a String containing the actual Unicode
characters.  Each 6-character Unicode representation and each ASCII character
is therefore converted to a two-byte Java char representation in the String.
For example, the Spanish translation of station is: "estaci\u00f3n" 
@param unic_to_transform String in format "ABC\u0411\u004a\u0044" to interpret.
@return interpretted String or null.
*/
public static String parseUnicode ( String unic_to_transform )
{	// Only uncomment if there is a serious bug to track down...
	//if ( Message.isDebugOn ) {
	//	Message.printDebug( 50, routine,
	//	"unic_to_transform = \""+ unic_to_transform + "\".");
	//}

	// Look for the \\u sequence.  If there are none, return without having
	// to do the translation...
	if ( unic_to_transform.indexOf("\\u") < 0 ) {
		// Then we have a string straight up with no unicode...
		return unic_to_transform;
	}
	// If here, there are some Unicode characters that need to be converted.
	// Traverse the string character by character.  If a unicode character
	// is encountered, then 6 characters (e.g., "\u004e") are read and
	// converted to a two-byte Java character
		
	StringBuffer buffer = new StringBuffer();	// Final concatenation
							// of Unicode characters
	int len = unic_to_transform.length();
	char c;	// Character in original string
	String unic_string;	// Hexadecimal string for single Unicode
				// character to convert
	for ( int i = 0; i < len; i++ ) {
		// Check to see if at the position in the string there is a
		// Unicode character.  Make sure that there are at least 6
		// characters left in the string to do the check...
		c = unic_to_transform.charAt(i);
		if (	(c == '\\') && ((i+5) < len) &&
			(unic_to_transform.charAt(i+1) == 'u') ) {
			// Start of a Unicode character...  Get the hexadecimal
			// string by replacing the leading two characters with
			// 0x...
			unic_string = "0x"+unic_to_transform.substring(i+2,i+6);
			// The trick here is that a Java Unicode character is
			// equivalent to the integer value of the Unicode
			// hexadecimal code...
			buffer.append ( (char)(Integer.decode(
				unic_string).intValue()) );
			// Increment the counter to the last character that
			// is processed (it will be incremented to the next
			// character by the for loop)...
			i += 5;
		}
		else {	// Assume a simple ASCII character...
			buffer.append ( c );
		}
	}
	// Only uncomment if there is a serious bug to track down...
	//if ( Message.isDebugOn ) {
	//	Message.printDebug( 50, routine,
	//	"StringBuffer to return as a string: \"" + 
	//	buffer.toString() + "\"." );
	//}
	return buffer.toString();
}

} // End UnicodeUtil
