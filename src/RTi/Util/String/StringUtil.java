// StringUtil - string functions

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

package RTi.Util.String;

import java.lang.Character;
import java.lang.Double;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Math;
import java.lang.String;
import java.lang.StringBuffer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import RTi.Util.Message.Message;

/**
This class provides static utility routines for manipulating strings.
*/
public final class StringUtil {

// Global data...

/**
Indicates that strings should be sorted in ascending order.
*/
public static final int SORT_ASCENDING = 1;

/**
Indicates that strings should be sorted in descending order.
*/
public static final int SORT_DESCENDING = 2;

/**
Token types for parsing routines.
*/
public static final int TYPE_CHARACTER = 1; 
public static final int TYPE_DOUBLE = 2;
public static final int TYPE_FLOAT = 3;
public static final int TYPE_INTEGER = 4;
public static final int TYPE_STRING = 5;
public static final int TYPE_SPACE = 6;

/**
For use with breakStringList.  Skip blank fields (adjoining delimiters are merged).
*/
public static final int DELIM_SKIP_BLANKS = 0x1;
/**
For use with breakStringList.  Allow tokens that are surrounded by quotes.  For example, this is
used when a data field might contain the delimiting character.
*/
public static final int DELIM_ALLOW_STRINGS = 0x2;
/**
For use with breakStringList.  When DELIM_ALLOW_STRINGS is set, include the quotes in the returned string.
*/
public static final int DELIM_ALLOW_STRINGS_RETAIN_QUOTES = 0x4;

/**
For use with padding routines.  Pad/unpad back of string.
*/
public static final int PAD_BACK = 0x1;
/**
For use with padding routines.  Pad/unpad front of string.
*/
public static final int PAD_FRONT = 0x2;
/**
For use with padding routines.  Pad/unpad middle of string.  This is private
because for middle unpadding we currently only allow the full PAD_FRONT_MIDDLE_BACK option.
*/
private static final int PAD_MIDDLE = 0x4;
/**
For use with padding routines.  Pad/unpad front and back of string.
*/
public static final int PAD_FRONT_BACK = PAD_FRONT | PAD_BACK;
/**
For use with padding routines.  Pad/unpad front, back, and middle of string.
*/
public static final int PAD_FRONT_MIDDLE_BACK = PAD_FRONT | PAD_MIDDLE|PAD_BACK;

/**
Add a list of Strings to another list of Strings.  If the first list is
null, the second list will be returned.  If the second list is null, the
first list will be returned.  If both are null, null will be returned.
@return Combined list.
@param v list of Strings - will be modified if not null when passed in.
@param newv list of Strings to add.
*/
public static List<String> addListToStringList ( List<String> v, List<String> newv )
{	if ( newv == null ) {
		return v;
	}
	List<String> vmain = null;
	if ( v == null ) {
		// Create a list...
		vmain = new ArrayList<String>(50);
	}
	else {
		// Modify the old list
		vmain = v;
	}
	int length = newv.size ();
	for ( int i = 0; i < length; i++ ) {
		vmain.add ( newv.get(i) );
	}
	return vmain;
}

/**
Add a String to a list of String.  If the list is null, a new list
will be returned, containing the string.  The String will always be added
to the list, even if the String is null.
@return list after String is added.
@param v list of Strings.
@param string String to add to the list.
*/
public static List<String> addToStringList ( List<String> v, String string )
{	List<String> vmain = null;
	if ( v == null ) {
		// Create a list...
		vmain = new ArrayList<String>();
	}
	else {
		vmain = v;
	}
	vmain.add ( string );
	return vmain;
}

/**
Add an array of String to a list of String.  If the list is null, a new
list will be returned, containing the strings.  The Strings will always be
added to the list, even if they are null.
@return list after String is added.
@param v list of Strings.
@param strings Array of String to add to list.
*/
public static List<String> addToStringList ( List<String> v, String [] strings )
{	List<String> vmain = null;
	if ( v == null ) {
		// Create a list...
		vmain = new ArrayList<String>(50);
	}
	else {
		vmain = v;
	}
	if ( strings == null ) {
		return vmain;
	}
	for ( int i = 0; i < strings.length; i++ ) {
		vmain.add ( strings[i] );
	}
	return vmain;
}

/**
Convert a String to an int, similar to C language atoi() function.
@param s String to convert.
@return An int as converted from the String or 0 if conversion fails.
*/
public static int atoi( String s )
{	if ( s == null ) {
		return 0;
	}
	int value=0;
	try {
		value = Integer.parseInt( s.trim() );
	}
	catch( NumberFormatException e ){
		Message.printWarning( 50, "StringUtil.atoi", "Unable to convert \"" + s + "\" to int." );
		value = 0;
	}
	return value;
}

/**
Convert a String to a float, similar to C language atof() function.
@param s String to convert.
@return A float as converted from the String, or 0.0 if there is a conversion error.
*/
public static float atof( String s )
{	if ( s == null ) {
		return (float)0.0;
	}
	float value=(float)0.0;
	try {
		value = new Float( s.trim() ).floatValue();
	}
	catch( NumberFormatException e ){
		Message.printWarning ( 50, "StringUtil.atof", "Unable to convert \"" + s + "\" to float." );
		value = (float)0.0;
	}
	return value;
}

/**
Convert a String to a double.
@param s String to convert.
@return A double as converted from the String, or 0.0 if a conversion error.
*/
public static double atod( String s )
{	if ( s == null ) {
		return 0.0;
	}
	double value=0.0;
	try {
		value = new Double( s.trim() ).doubleValue();
	}
	catch( NumberFormatException e ){
		Message.printWarning( 50, "StringUtil.atod", "Unable to convert \"" + s + "\" to double." );
		value = 0.0;
	}
	return value;
}

/**
Convert a String to a long.
@param s String to convert.
@return A long as converted from the String, or 0 if an error.
*/
public static long atol( String s )
{	if ( s == null ) {
		return 0;
	}
	long value=0;
	try {
		value = new Long( s.trim() ).longValue();
	}
	catch( NumberFormatException e ){
		Message.printWarning( 50, "StringUtil.atol", "Unable to convert \"" + s + "\" to long." );
		value = 0;
	}
	return value;
}

/*------------------------------------------------------------------------------
** HMBreakStringList - get a list of strings from a string
**------------------------------------------------------------------------------
** Copyright:	See the COPYRIGHT file.
**------------------------------------------------------------------------------
** Notes:	(1)	The list is assumed to be of the form "val,val,val",
**			where the commas indicate the delimiter character.
**		(2)	Call "HMFreeStringList" when done with the list.
**		(3)	The list always has one NULL element at the end so that
**			we know how to free the memory.  However, "nlist" does
**			not include this element.
**		(4)	If the HMDELIM_ALLOW_STRINGS flag is set, then we
**			strings to be treated as one token, even if they contain
**			blanks.  The first quote character, either " or ' is
**			used to contain the string.  The quote characters
**			cannot be in the list of delimiting characters.
**		(5)	It would be nice to allow return of all the tokens.
**			Add the "flag" variable to allow for this enhancement
**			in the future.
**------------------------------------------------------------------------------
** History:
**
** ?		Steven A. Malers, RTi	Created routine.
** 06-08-95	SAM, RTi		Document all variables.
** 08-21-95	SAM, RTi		Change so that delimiter list is a
**					string so that more than one
**					"whitespace" character can be used
**					(e.g., spaces and tabs).  Also allow
**					more than one whitespace character in
**					sequence (skip them all).  Also add
**					check to make sure that substring is not
**					overrun.
** 04 Oct 1995	SAM, RTi		Use HMAddToStringList to do bulk of
**					work.
** 02 Sep 1996	SAM, RTi		Break this routine out of HMUtil.c.  Do
**					minor cleanup to make more stand-alone.
** 07 Oct 1996	SAM, RTi		Add <string.h> to prototype functions.
** 21 Jan 1997	SAM, RTi		Add flag to allow quoted strings to be
**					separated out.
** 17 Jun 1997	Matthew J. Rutherford, RTi
**					Adjust string stuff so that a quote
**					in the middle of a string is found.
**------------------------------------------------------------------------------
** Variable	I/O	Description
**
** delim	I	Character delimiter list.
** flag		I	Flag to modify parsing.
** i		L	Counter for characters in substring.
** instring	L	Indicates if we are inside a quoted string.
** list		L	List of broken out strings.
** nlist	O	Number of strings in the final list.
** nlist2	L	Used when adding strings to list.
** pt		L	Pointer to original string.
** pt2		L	Pointer to split out string.
** quote	L	Character used for a quoted string.
** routine	L	Name of this routine.
** string	I	String of delimiter-separated items.
** tempstr	L	String used when splitting out sub-strings.
**------------------------------------------------------------------------------
*/
//TODO SAM 2010-09-21 Evaluate phasing out this method in favor of built-in parsing
// features in Java (which are now more mature then when breakStringList() was originally written).
/**
Break a delimited string into a list of Strings.  The end of the string is
considered as a delimiter so "xxxx,xxxx" returns two strings if the comma is a
delimiter and "xxxxx" returns one string if the comma is the delimiter.  If a
delimiter character is actually the last character, no empty/null field is returned at
the end.  If multiple delimiters are at the front and skip blanks is specified,
all the delimiters will be skipped.  Escaped single characters are passed through
as is.  Therefore \" (two characters) will be two characters in the output.  Other
code needs to interpret the two characters as the actual special character.
@return A list of Strings, guaranteed to be non-null
@param string The string to break.
@param delim A String containing characters to treat as delimiters.  Each
character in the string is checked (the complete string is not used as a
multi-character delimiter).  Cannot be null.
@param flag Bitmask indicating how to break the string.  Specify
DELIM_SKIP_BLANKS to skip blank fields (delimiters that are next to each other
are treated as one delimiter - delimiters at the front are ignored).  Specify
DELIM_ALLOW_STRINGS to allow quoted strings (which may contain delimiters).
Specify DELIM_ALLOW_STRINGS_RETAIN_QUOTES to retain the quotes in the return strings when
DELIM_ALLOW_QUOTES is used.
Specify 0 (zero) to do simple tokenizing where repeated delimiters are not
merged and quoted strings are not handled as one token.  Note that when allowing
quoted strings the string "xxxx"yy is returned as xxxxyy because no intervening delimiter is present.
*/
public static List<String> breakStringList( String string, String delim, int flag )
{	String routine = "StringUtil.breakStringList";
	List<String> list = new ArrayList<String>();
	
	if ( string == null ) {
	 	return list;
	}
	if ( string.length() == 0 ) {
	 	return list;
	}
	//if ( Message.isDebugOn ) {
	//	Message.printDebug ( 50, routine,
	//	Message.printStatus ( 1, routine,
	//	"SAMX Breaking \"" + string + "\" using \"" + delim + "\"" );
	//}
	int	length_string = string.length();
	boolean	instring = false;
	boolean retainQuotes = false;
	int	istring = 0;
	char cstring;
	char quote = '\"';
	StringBuffer tempstr = new StringBuffer ();
	boolean allow_strings = false, skip_blanks = false;
	if ( (flag & DELIM_ALLOW_STRINGS) != 0 ) {
		allow_strings = true;
	}
	if ( (flag & DELIM_SKIP_BLANKS) != 0 ) {
		skip_blanks = true;
	}
    if ( allow_strings && ((flag & DELIM_ALLOW_STRINGS_RETAIN_QUOTES) != 0) ) {
        retainQuotes = true;
    }
	// Loop through the characters in the string.  If in the main loop or
	// the inner "while" the end of the string is reached, the last
	// characters will be added to the last string that is broken out...
	boolean at_start = true;	// If only delimiters are at the front this will be true.
	for ( istring = 0; istring < length_string; ) {
		cstring = string.charAt(istring);
		// Start the next string in the list.  Move characters to the
		// temp string until a delimiter is found.  If inside a string
		// then go until a closing delimiter is found.
		instring = false;
		tempstr.setLength ( 0 );	// Clear memory.
		while ( istring < length_string ) {
			// Process a sub-string between delimiters...
			cstring = string.charAt ( istring );
			// Check for escaped special characters...
			if ( (cstring == '\\') && (istring < (length_string - 1)) &&
			    (string.charAt(istring + 1) == '\"') ) {
			    // Add the backslash and the next character - currently only handle single characters
			    tempstr.append ( cstring );
			    // Now increment to the next character...
			    ++istring;
			    cstring = string.charAt ( istring );
			    tempstr.append ( cstring );
			    ++istring;
			    continue;
			}
			//Message.printStatus ( 2, routine, "SAMX Processing character " + cstring );
			if ( allow_strings ) {
				// Allowing quoted strings so do check for the start and end of quotes...
				if ( !instring && ((cstring == '"')||(cstring == '\'')) ){
					// The start of a quoted string...
					instring = true;
					at_start = false;
					quote = cstring;
					if ( retainQuotes ) {
					    tempstr.append(cstring);
					}
					// Skip over the quote since we don't want to /store or process again...
					++istring;
					// cstring set at top of while...
					//Message.printStatus ( 1, routine, "SAMX start of quoted string " + cstring );
					continue;
				}
				// Check for the end of the quote...
				else if ( instring && (cstring == quote) ) {
					// In a quoted string and have found the closing quote.  Need to skip over it.
					// However, could still be in the string and be escaped, so check for that
					// by looking for another string. Any internal escaped quotes will be a pair "" or ''
					// so look ahead one and if a pair, treat as characters to be retained.
					// This is usually only going to be encountered when reading CSV files, etc.
                    if ( (istring < (length_string - 1)) && (string.charAt(istring + 1) == quote) ) {
                    	// Found a pair of the quote character so absorb both and keep looking for ending quote for the token
                    	tempstr.append(cstring); // First quote retained because it is literal
                    	//Message.printStatus(2,routine,"found ending quote candidate at istring=" + istring + " adding as first in double quote");
                    	++istring;
                    	if ( retainQuotes ) {
                    		// Want to retain all the quotes
                    		tempstr.append(cstring); // Second quote
                        	//Message.printStatus(2,routine,"Retaining 2nd quote of double quote at istring=" + istring );
                    	}
                    	++istring;
                    	// instring still true
                    	continue;
                    }
                    // Else... process as if not an escaped string but an end of quoted string
                    if ( retainQuotes ) {
                        tempstr.append(cstring);
                    }
					instring = false;
					//Message.printStatus ( 1, routine, "SAMX end of quoted string" + cstring );
					++istring;
					if ( istring < length_string ) {
						cstring =string.charAt(istring);
						// If the current string is now another quote, just continue so it can be processed
						// again as the start of another string (but don't by default add the quote character)...
						if ( (cstring == '\'') || (cstring == '"') ) {
	                        if ( retainQuotes ) {
	                            tempstr.append(cstring);
	                        }
							continue;
						}
					}
					else {
						// The quote was the last character in the original string.  Break out so the
						// last string can be added...
						break;
					}
					// If here, the closing quote has been skipped but don't want to break here
					// in case the final quote isn't the last character in the sub-string
					// (e.g, might be ""xxx).
				}
			}
			// Now check for a delimiter to break the string...
			if ( delim.indexOf(cstring) != -1 ) {
				// Have a delimiter character that could be in a string or not...
				if ( !instring ) {
					// Not in a string so OK to break...
					//Message.printStatus ( 1, routine, "SAMX have delimiter outside string" + cstring );
					break;
				}
			}
			else {
				// Else, treat as a character that needs to be part of the token and add below...
				at_start = false;
			}
			// It is OK to add the character...
			tempstr.append ( cstring );
			// Now increment to the next character...
			++istring;
			// Go to the top of the "while" and evaluate the current character that was just set.
			// cstring is set at top of while...
		}
		// Now have a sub-string and the last character read is a
		// delimiter character (or at the end of the original string).
		//
		// See if we are at the end of the string...
		if ( instring ) {
			if ( Message.isDebugOn ) {
				Message.printWarning ( 10, routine, "Quoted string \"" + tempstr + "\" is not closed" );
			}
			// No further action is required...
		}
		// Check for and skip any additional delimiters that may be present in a sequence...
		else if ( skip_blanks ) {
			while ( (istring < length_string) && (delim.indexOf(cstring) != -1) ) {
				//Message.printStatus ( 1, routine, "SAMX skipping delimiter" + cstring );
				++istring;
				if ( istring < length_string ) {
					cstring = string.charAt ( istring );
				}
			}
			if ( at_start ) {
				// Just want to skip the initial delimiters without adding a string to the returned list...
				at_start = false;
				continue;
			}
			// After this the current character will be that which needs to be evaluated.  "cstring" is reset
			// at the top of the main "for" loop but it needs to be assigned here also because of the check
			// in the above while loop
		}
		else {
			// Not skipping multiple delimiters so advance over the character that triggered the break in
			// the main while loop...
			++istring;
			// cstring will be assigned in the main "for" loop
		}
		// Now add the string token to the list...
		list.add ( tempstr.toString() );
		//if ( Message.isDebugOn ) {
			//Message.printDebug ( 50, routine,
			//Message.printStatus ( 1, routine,
			//"SAMX Broke out list[" + (list.size() - 1) + "]=\"" + tempstr + "\"" );
		//}
	}
	return list;
}

/**
Returns The hexadecimal String representation of a byte.  For example, passing
in a byte with value 63 would result in a String "3f".  Note that there is no
leading "0x" in the returned String.  Note also that values less than 0 or 
greater than 255 may cause bad results, and are not supported by this method.
@param b Byte to convert to a hexadecimal String.
@return String that represents the Hexadecimal value of the byte.
*/
public static String byteToHex( byte b )
{	char hexDigit[] = {
	'0', '1', '2', '3', '4', '5', '6', '7',
	'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };

	return new String(array);
}

/**
Center a string by padding with spaces.
@return The centered string.
@param orig The original string.
@param width Width of the centered string.
*/
public static String centerString ( String orig, int width )
{	if ( orig.length() >= width ) {
		return orig;
	}
	int border = (width - orig.length())/2;
	StringBuffer centered = new StringBuffer ( orig );
	for ( int i = 0; i < border; i++ ) {
		centered.insert(0,' ');
	}
	return centered.toString();
}

/**
Returns The hexadecimal String representation of char c.  For example, passing
in a char with a value 'c' would result in a String "0063".  Note that there is
no leading "0x" in the returned String.  Because the Java char type is two bytes
in order to store Unicode characters, the returned string is four characters
(two per byte).
@param c char to convert to hexadecimal String.
@return String that represents the Hexadecimal value of the char.
*/
public static String charToHex(char c)
{	byte hi = (byte) (c >>> 8);
	byte lo = (byte) (c & 0xff);
	return byteToHex(hi) + byteToHex(lo);
}

/**
 * Compare two strings lexicographically (alphabetically) using an operator.
 * Convert to upper or lower case prior to calling to compare by ignoring case.
 * @param s1 first string.
 * @param operator operator (>, >=, <, <=, = or ==, !=).
 * @param s2 second string.
 */
public static boolean compareUsingOperator(String s1, String operator, String s2) {
	if ( operator.equals("=") || operator.equals("==") ) {
		if ( s1.equals(s2) ) {
			return true;
		}
		else {
			return false;
		}
	}
	else if ( operator.equals("!=") ) {
		if ( ! s1.equals(s2) ) {
			return true;
		}
		else {
			return false;
		}
	}
	else if ( operator.equals("<") ) {
		if ( s1.compareTo(s2) < 0 ) {
			return true;
		}
		else {
			return false;
		}
	}
	else if ( operator.equals("<=") ) {
		if ( s1.compareTo(s2) <= 0 ) {
			return true;
		}
		else {
			return false;
		}
	}
	else if ( operator.equals(">") ) {
		if ( s1.compareTo(s2) > 0 ) {
			return true;
		}
		else {
			return false;
		}
	}
	else if ( operator.equals(">=") ) {
		if ( s1.compareTo(s2) >= 0 ) {
			return true;
		}
		else {
			return false;
		}
	}
	return false;
}

/**
Indicate whether the string contains any of the specified characters.  This
can be used to check for restricted characters in input.
@param s String to check.
@param chars Characters to check for in string.
@param ignore_case Specify to true if case should be ignored.
@return true if the checked string contains any of the specified characters.
*/
public static boolean containsAny ( String s, String chars, boolean ignore_case )
{
    if ( (s == null) || (chars == null) ) {
        return false;
    }
    // Convert the case here once rather than in indexOfIgnoreCase()
    if ( ignore_case ) {
        s = s.toUpperCase();
        chars = chars.toUpperCase();
    }
    int size = chars.length();
    for ( int i = 0; i < size; i++ ) {
        if ( s.indexOf(chars.charAt(i)) >= 0 ) {
            return true;
        }
    }
    return false;
}

/**
Convert a string containing a number sequence like "1,4-5,13" to zero offset like "0,3-4,12".
This method is used to convert user-based parameters that have values 1+ with internal code values 0+, which
is useful when high-level (i.e., user-specified) parameters need to be converted to the zero offset form
used by low-level code.
@param sequenceString a string of positions like "1,4-5,13", where each index is 1+.
@return the string of positions like "0,3-4,12", where each index is 0+.
*/
public static String convertNumberSequenceToZeroOffset ( String sequenceString )
{
    if ( (sequenceString == null) || (sequenceString.length() == 0) ) {
        return sequenceString;
    }
    StringBuffer b = new StringBuffer();
    List<String> v = StringUtil.breakStringList ( sequenceString, ", ", StringUtil.DELIM_SKIP_BLANKS );
    int vsize = 0;
    if ( v != null ) {
        vsize = v.size();
    }
    for ( int i = 0; i < vsize; i++ ) {
        String vi = v.get(i);
        if ( i != 0 ) {
            b.append (",");
        }
        if ( StringUtil.isInteger(vi)) {
            int index = Integer.parseInt(vi);
            b.append ( "" + (index - 1) );
        }
        else {
            int pos = vi.indexOf("-");
            if ( pos >= 0 ) {
                // Specifying a range of values...
                int first_in_range = -1;
                int last_in_range = -1;
                if ( pos == 0 ) {
                    // First index is 1 (will be decremented below)...
                    first_in_range = 1;
                }
                else {
                    // Get first to skip...
                    first_in_range = Integer.parseInt(vi.substring(0,pos).trim());
                }
                last_in_range = Integer.parseInt(vi.substring(pos+1).trim());
                b.append ( "" + (first_in_range - 1) + "-" + (last_in_range - 1));
            }
        }
    }
    return b.toString();
}

/**
Determine whether one strings ends with the specified substring, ignoring case.
@param s String to evaluate.
@param pattern End-string to compare.
@return true if the String "s" ends with "pattern", ignoring case.  If the
pattern string is null or empty, false is returned.
*/
public static boolean endsWithIgnoreCase ( String s, String pattern )
{	if ( (s == null) || (pattern == null) ) {
		return false;
	}
	int plen = pattern.length();
	int slen = s.length();
	if ( (plen == 0) || (slen < plen) ) {
		return false;
	}
	String sub = s.substring(slen - plen);
	return sub.regionMatches(true,0,pattern,0,plen);
}

/**
 * Expand a string by replacing ${Property}.
 * This currently does not handled nested properties.
 * @param s String to expand, optionally containing ${Property} notation.
 * @param map Map of properties where key is the property name and value is used for the property.
 * Objects are converted to string only if the value is not null.
 */
public static String expandForProperties ( String s, HashMap<String,Object> map ) {
	// Iterate through the map and replace strings.
	String sExpanded = s;
	String sValue;
	for ( Map.Entry<String,Object> entry : map.entrySet() ) {
		String key = entry.getKey();
		Object value = entry.getValue();
		if ( value == null ) {
			continue;
		}
		else {
			// TODO smalers 2019-12-13 this may need fixed for floating point formatting to avoid scientific notation
			sValue = "" + value;
		}
		sExpanded = sExpanded.replace("${" + key + "}", sValue);
	}
	return sExpanded;
}

/**
Parse a fixed-format string (e.g., a FORTRAN data file) using a simplified
notation.  <b>This routine needs to be updated to accept C-style formatting
commands.  Requesting more fields than there are data results in default (zero
or blank) data being returned.</b>
This method can be used to read integers and floating point numbers from a
string containing fixed-format information.
@return A List of objects that are read from the string according to the
specified format described below.  Integers are returned as Integers, doubles
as Doubles, etc.  Blank "x" fields are not returned (therefore the list of returned
objects has a size of all non-x formats).
@param string String to parse.
@param format Format to use for parsing, as shown in the following table.
An example is: "i5f10x3a10", or in general
"v#", where "v" indicates a variable type and "#" indicates the TOTAL width
of the variable field in the string.
NO WHITESPACE OR DELIMITERS IN THE FORMAT!
<p>

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Data Type</b></td>	<td><b>Format</b></td>	<td><b>Example</b></td>
</tr

<tr>
<td><b>integer, Integer</b></td>
<td>i</td>
<td>i5</td>
</tr>

<tr>
<td><b>float, Float</b></td>
<td>f</td>
<td>f10</td>
</tr>

<tr>
<td><b>double, Double</b></td>
<td>d</td>
<td>d10</td>
</tr>

<tr>
<td><b>Spaces (not returned)</b></td>
<td>x</td>
<td>x20</td>
</tr>

<tr>
<td><b>char</b></td>
<td>c</td>
<td>c</td>
</tr>

<tr>
<td><b>String</b></td>
<td>s, a</td>
<td>s10, a10</td>
</tr>
</table>
*/
public static final List<Object> fixedRead ( String string, String format )
{	// Determine the format types and widths...
	// THIS CODE INLINED FROM THE METHOD BELOW.  MODIFY THE OTHER METHOD AND THEN MAKE THIS CODE AGREE....

	// First loop through the format string and count the number of valid format specifier characters...
	int format_length = 0;
	if ( format != null ) {
		format_length = format.length();
	}
	int field_count = 0;
	char cformat;
	for ( int i = 0; i < format_length; i++ ) {
		cformat = format.charAt(i);
		if ( (cformat == 'a') || (cformat == 'A') ||
			(cformat == 'c') || (cformat == 'C') ||
			(cformat == 'd') || (cformat == 'D') ||
			(cformat == 'f') || (cformat == 'F') ||
			(cformat == 'i') || (cformat == 'I') ||
			(cformat == 's') || (cformat == 'S') ||
			(cformat == 'x') || (cformat == 'X') ) {
			++field_count;
		}
	}
	// Now set the array sizes for formats...
	int [] field_types = new int[field_count];
	int [] field_widths = new int[field_count];
	field_count = 0;	// Reset for detailed loop...
	StringBuffer width_string = new StringBuffer();
	for ( int iformat = 0; iformat < format_length; iformat++ ) {
		width_string.setLength ( 0 );
		// Get a format character...
		cformat = format.charAt ( iformat );
		//System.out.println ( "Format character is: " + cformat );
		if ( (cformat == 'c') || (cformat == 'C') ) {
			field_types[field_count] = TYPE_CHARACTER;
			field_widths[field_count] = 1;
			continue;
		}
		else if ( (cformat == 'd') || (cformat == 'D') ) {
			field_types[field_count] = TYPE_DOUBLE;
		}
		else if ( (cformat == 'f') || (cformat == 'F') ) {
			field_types[field_count] = TYPE_FLOAT;
		}
		else if ( (cformat == 'i') || (cformat == 'I') ) {
			field_types[field_count] = TYPE_INTEGER;
		}
		else if ( (cformat == 'a') || (cformat == 'A') ||
			(cformat == 's') || (cformat == 'S') ) {
			field_types[field_count] = TYPE_STRING;
		}
		else if ( (cformat == 'x') || (cformat == 'X') ) {
			field_types[field_count] = TYPE_SPACE;
		}
		else {
			// Problem!!!
			continue;
		}
		// Determine the field width...
		++iformat;
		while ( iformat < format_length ) {
			cformat = format.charAt ( iformat );
			if ( !Character.isDigit(cformat) ) {
				// Went into the next field...
				--iformat;
				break;
			}
			width_string.append ( cformat );
			++iformat;
		}
		field_widths[field_count] = atoi ( width_string.toString() );
		++field_count;
	}
	width_string = null;
	// ...END OF INLINED CODE
	// Now do the read...	
	List<Object> v = fixedRead ( string, field_types, field_widths, null );
	return v;
}

/**
Parse a fixed string.
@return A list of objects that are read from the string according to the
specified format.  Integers are returned as Integers, doubles as Doubles, etc.
Blank TYPE_SPACE fields are not returned.
@param string String to parse.
@param format Format of string (see overloaded method for explanation).
@param results If specified and not null, the list will be used to save the
results.  This allows a single list to be reused in repetitive reads.
The list is cleared before reading.
*/
public static final List<Object> fixedRead ( String string, String format, List<Object> results )
{	// First loop through the format string and count the number of valid format specifier characters...
	int format_length = 0;
	if ( format != null ) {
		format_length = format.length();
	}
	int field_count = 0;
	char cformat;
	for ( int i = 0; i < format_length; i++ ) {
		cformat = string.charAt(i);
		if ( (cformat == 'a') || (cformat == 'A') ||
			(cformat == 'c') || (cformat == 'C') ||
			(cformat == 'd') || (cformat == 'D') ||
			(cformat == 'f') || (cformat == 'F') ||
			(cformat == 'i') || (cformat == 'I') ||
			(cformat == 's') || (cformat == 'S') ||
			(cformat == 'x') || (cformat == 'X') ) {
			++field_count;
		}
	}
	// Now set the array sizes for formats...
	int [] field_types = new int[field_count];
	int [] field_widths = new int[field_count];
	field_count = 0;	// Reset for detailed loop...
	StringBuffer width_string = new StringBuffer();
	for ( int iformat = 0; iformat < format_length; iformat++ ) {
		width_string.setLength ( 0 );
		// Get a format character...
		cformat = format.charAt ( iformat );
		//System.out.println ( "Format character is: " + cformat );
		if ( (cformat == 'c') || (cformat == 'C') ) {
			field_types[field_count] = TYPE_CHARACTER;
			field_widths[field_count] = 1;
			continue;
		}
		else if ( (cformat == 'd') || (cformat == 'D') ) {
			field_types[field_count] = TYPE_DOUBLE;
		}
		else if ( (cformat == 'f') || (cformat == 'F') ) {
			field_types[field_count] = TYPE_FLOAT;
		}
		else if ( (cformat == 'i') || (cformat == 'I') ) {
			field_types[field_count] = TYPE_INTEGER;
		}
		else if ( (cformat == 'a') || (cformat == 'A') ||
			(cformat == 's') || (cformat == 'S') ) {
			field_types[field_count] = TYPE_STRING;
		}
		else if ( (cformat == 'x') || (cformat == 'X') ) {
			field_types[field_count] = TYPE_SPACE;
		}
		else {
			// Problem!!!
			continue;
		}
		// Determine the field width...
		++iformat;
		while ( iformat < format_length ) {
			cformat = format.charAt ( iformat );
			if ( !Character.isDigit(cformat) ) {
				// Went into the next field...
				--iformat;
				break;
			}
			width_string.append ( cformat );
			++iformat;
		}
		field_widths[field_count] = atoi ( width_string.toString() );
		++field_count;
	}
	width_string = null;
	List<Object> v = fixedRead ( string, field_types, field_widths, results );
	return v;
}

/**
Parse a fixed-format string (e.g., a FORTRAN data file).
Requesting more fields than there are data results in default (zero
or blank) data being returned.</b>
This method can be used to read integers and floating point numbers from a
string containing fixed-format information.
@return A List of objects that are read from the string according to the
specified format.  Integers are returned as Integers, doubles as Doubles, etc.
Blank TYPE_SPACE fields are not returned.
@param string String to parse.
@param field_types Field types to use for parsing 
@param field_widths Array of fields widths.
@param results If specified and not null, the list will be used to save the
results.  This allows a single list to be reused in repetitive reads.
The list is cleared before reading.
*/
public static final List<Object> fixedRead ( String string, int[] field_types, int [] field_widths, List<Object> results )
{	int	dtype = 0,	// Indicates type of variable (from "format").
		isize,		// Number of characters in a data value
				// (as integer).
		j,		// Index for characters in a field.
		nread = 0;	// Number of values read from file.
	boolean	eflag = false;	// Indicates that the end of the line has been
				// reached before all of the format has been
				// evaluated.

	int size = field_types.length;
	int string_length = string.length();
	List<Object> tokens = null;
	if ( results != null ) {
		tokens = results;
		tokens.clear();
	}
	else {
		tokens = new ArrayList<Object>(size);
	}

	StringBuffer var = new StringBuffer();
	int istring = 0;	// Position in string to parse.
	for ( int i = 0; i < size; i++ ) {
		dtype = field_types[i];
		// Read the variable...
		var.setLength ( 0 );
		if ( eflag ) {
			// End of the line has been reached before the processing has finished...
		}
		else {
			//System.out.println ( "Variable size=" + size);
			isize = field_widths[i];
			for ( j = 0; j < isize; j++, istring++ ) {
				if ( istring >= string_length ) {
					// End of the string.  Process the rest of the variables so that they are
					// given a value of zero...
					eflag = true;
					break;
				}
				else {
					var.append ( string.charAt(istring) );
				}
			}
		}
		// 1. Convert the variable that was read as a character
		//    string to the proper representation.  Apparently
		//    most atomic objects can be instantiated from a
		//    String but not a StringBuffer.
		// 2. Add to the list.
		//Message.printStatus ( 2, "", "String to convert to object is \"" + var + "\"" );
		if ( dtype == StringUtil.TYPE_CHARACTER ) {
			tokens.add ( new Character(var.charAt(0)) );
		}
		else if ( dtype == StringUtil.TYPE_DOUBLE ) {
			String sdouble = var.toString().trim();
			if ( sdouble.length() == 0 ) {
				tokens.add ( new Double ( "0.0" ) );
			}
			else {
				tokens.add ( new Double ( sdouble ) );
			}
		}
		else if ( dtype == StringUtil.TYPE_FLOAT ) {
			String sfloat = var.toString().trim();
			if ( sfloat.length() == 0 ) {
				tokens.add ( new Float ( "0.0" ) );
			}
			else {
				tokens.add ( new Float ( sfloat ) );
			}
		}
		else if ( dtype == StringUtil.TYPE_INTEGER ) {
			String sinteger = var.toString().trim();
			if ( sinteger.length() == 0 ) {
				tokens.add ( new Integer ( "0" ) );
			}
			else {
				// check for "+"
				if ( sinteger.startsWith("+")) {
					sinteger = sinteger.substring(1);
				}
				tokens.add ( new Integer ( sinteger ) );
			}
		}
		else if ( dtype == StringUtil.TYPE_STRING ) {
			tokens.add ( var.toString() );
		}
		++nread;
		if ( nread < 0 ) {
			// TODO smalers 2019-05-28 figure out what to do with nread
		}
	}
	return tokens;
}

// TODO SAM 2014-03-30 Refactor the fixedRead methods to call the following method
/**
Parse the format string for fixedRead into lists that can be used for other fixedRead commands.
The field types and widths WILL INCLUDE "x" formats because fixedRead() needs that information.
@param format the fixedRead method format (e.g., "d10f8i3x3s15")
@param fieldTypes a non-null List<Integer> that will be set to the field types for each format part.
@param fieldWidths a non-null List<Integer> that will be set to the field widths for each format part.
*/
public static void fixedReadParseFormat ( String format, List<Integer> fieldTypes, List<Integer> fieldWidths )
{
    // Now set the array sizes for formats...
    StringBuffer width_string = new StringBuffer();
    char cformat;
    for ( int iformat = 0; iformat < format.length(); iformat++ ) {
        width_string.setLength ( 0 );
        // Get a format character...
        cformat = format.charAt ( iformat );
        //System.out.println ( "Format character is: " + cformat );
        if ( (cformat == 'c') || (cformat == 'C') ) {
            fieldTypes.add(TYPE_CHARACTER);
            fieldWidths.add(1);
            continue;
        }
        else if ( (cformat == 'd') || (cformat == 'D') ) {
            fieldTypes.add(TYPE_DOUBLE);
        }
        else if ( (cformat == 'f') || (cformat == 'F') ) {
            fieldTypes.add(TYPE_FLOAT);
        }
        else if ( (cformat == 'i') || (cformat == 'I') ) {
            fieldTypes.add(TYPE_INTEGER);
        }
        else if ( (cformat == 'a') || (cformat == 'A') ||
            (cformat == 's') || (cformat == 'S') ) {
            fieldTypes.add(TYPE_STRING);
        }
        else if ( (cformat == 'x') || (cformat == 'X') ) {
            fieldTypes.add(TYPE_SPACE);
        }
        else {
            // Problem!!!
            continue;
        }
        // Determine the field width...
        ++iformat;
        while ( iformat < format.length() ) {
            cformat = format.charAt ( iformat );
            if ( !Character.isDigit(cformat) ) {
                // Went into the next field...
                --iformat;
                break;
            }
            width_string.append ( cformat );
            ++iformat;
        }
        fieldWidths.add(atoi ( width_string.toString()) );
    }
}

/**
Format two arrays of number parallel to each other.
This is a convenience method useful for logging.
@param label1 label for the first array
*/
public static String formatArrays ( String label1, double [] array1, String label2, double [] array2,
    String delim, String lineDelim )
{
    StringBuffer b = new StringBuffer();
    int len = array1.length;
    if ( array2.length > array1.length ) {
        len = array2.length;
    }
    if ( (label1 != null) && !label1.equals("") ) {
        b.append ( label1 );
    }
    if ( delim != null ) {
        b.append ( delim );
    }
    if ( (label2 != null) && !label2.equals("") ) {
        b.append ( label2 );
    }
    if ( (b.length() > 0) && (lineDelim != null) ) {
        b.append(lineDelim);
    }
    int lineCount = 0;
    int im1;
    for ( int i = 0; i < len; i++ ) {
        im1 = i - 1;
        if ( lineCount > 0 ) {
            if ( lineDelim != null ) {
                b.append(lineDelim);
            }
        }
        if ( im1 <= array1.length ) {
            b.append ( "" + array1[i] );
        }
        if ( delim != null ) {
            b.append ( delim );
        }
        if ( im1 <= array2.length ) {
            b.append ( "" + array2[i] );
        }
        ++lineCount;
    }
    return b.toString();
}

/**
Format an array as a sequence of numbers separated by a delimiter.  A blank string is returned if
the array is null or empty.
@return a string containing the formatted sequence of integers.
@param array array of numbers to format
@param delim delimiter to user between numbers (no extra spaces will be added)
*/
public static String formatNumberSequence ( int [] array, String delim )
{
    StringBuffer b = new StringBuffer();
    if ( array != null ) {
        for ( int i = 0; i < array.length; i++ ) {
            if ( (i > 0) && (delim != null) ) {
                b.append(delim);
            }
            b.append ( "" + array[i] );
        }
    }
    return b.toString();
}

// Format a string like the C sprintf
//
// Notes:	(1)	We accept any of the formats:
//
//			%%		- literal percent
//			%c		- single character
//			%s %8.8s %-8s	- String
//			%d %4d		- Integer
//			%8.2f %#8.2f	- Float
//			%8.2F %#8.2F	- Double
//
/**
Format a string like the C sprintf function.
@return The formatted string.
@param v The list of objects to format.  Floating point numbers must be Double, etc. because
the toString function is called for each object (actually, a number can be
passed in as a String since toString will work in that case too).
@param format The format to use for formatting, containing normal characters
and the following formatting strings:
<p>
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Data Type</b></td>	<td><b>Format</b></td>	<td><b>Example</b></td>
</tr

<tr>
<td><b>Literal %</b></td>
<td>%%</td>
<td>%%5</td>
</tr>

<tr>
<td><b>Single character</b></td>
<td>%c</td>
<td>%c</td>
</tr>

<tr>
<td><b>String</b></td>
<td>%s</td>
<td>%s, %-20.20s</td>
</tr>

<tr>
<td><b>Integer</b></td>
<td>%d</td>
<td>%4d, %04d</td>
</tr>

<tr>
<td><b>Float, Double</b></td>
<td>%f, %F</td>
<td>%8.2f, %#8.2f</td>
</tr>

</table>
<p>

The format can be preceded by a - (e.g., %-8.2f, %-s) to left-justify the
formatted string.  The default is to left-justify strings and right-justify
numbers.  Numeric formats, if preceded by a 0 will result in the format being
padded by zeros (e.g., %04d will pad an integer with zeros up to 4 digits).
To force strings to be a certain width use a format like %20.20s.  To force
floating point numbers to always use a decimal point use the #.
Additional capabilities may be added later.
*/
public static final String formatString ( List<? extends Object> v, String format )
{	StringBuilder buffer = new StringBuilder();
	int dl = 75;

	if ( v == null ) {
		return buffer.toString();
	}
	if ( format == null ) {
		return buffer.toString();
	}

	// Now loop through the format and as format specifiers are encountered
	// put them in the formatted string...

	int	diff;
	int	i;
	int	iend;
	int	iformat;
	int	iprecision;
	int	iwidth;
	int	j = 0;
	int	length_format = format.length ();
	int	length_temp;
	int	offset = 0;	// offset in string or array
	int	precision = 0; 	// precision as integer
	int	sign;
	int	width = 0;
	int	vindex = 0;
	char cformat;
	char cvalue;
	char sprecision[] = new char[20]; // should be enough
	char swidth[] = new char[20];
	boolean	dot_found, first, left_shift, pound_format, zero_format;
	int	vsizem1 = v.size() - 1;

	for ( iformat = 0; iformat < length_format; iformat++ ) {
		cformat = format.charAt ( iformat );
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, "StringUtil.formatString",
			"Format character :\"" + cformat + "\", vindex = " + vindex );
		}
		if ( cformat == '%' ) {
			// The start of a format field.  Get the rest so that we can process.  First advance one...
			dot_found = false;
			left_shift = false;
			pound_format = false;
			zero_format = false;
			iprecision = 0;
			iwidth = 0;
			++iformat;
			if ( iformat >= length_format ) {
				// End of format...
				break;
			}
			// On the character after the %
			first = true;
			for ( ; iformat < length_format; iformat++ ) {
				cformat = format.charAt ( iformat );
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, "StringUtil.formatString",
					"Format character :\"" + cformat + "\" vindex =" + vindex );
				}
				if ( first ) {
					// First character after the %...
					// Need to update so that some of the following can be combined.
					if ( cformat == '%' ) {
						// Literal percent...
						buffer.append ( '%' );
						first = false;
						break;
					}
					else if ( cformat == 'c' ) {
						// Append a Character from the list...
						buffer.append (	v.get(vindex).toString());
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, "StringUtil.formatString",
							"Processed list[" + vindex + "], a char" );
						}
						++vindex;
						first = false;
						break;
					}
					else if ( cformat == '-' ) {
						// Left shift...
						left_shift = true;
						continue;
					}
					else if ( cformat == '#' ) {
						// Special format...
						pound_format = true;
						continue;
					}
					else if ( cformat == '0' ) {
						// Leading zeros...
						zero_format = true;
						continue;
					}
					else {
						// Not a recognized formatting character so we will just go
						// to the next checks outside this loop...
						first = false;
					}
				}
				// Else retrieving characters until an ending "s", "i", "d", or "f" is encountered.
				if ( Character.isDigit(cformat) || (cformat == '.') ) {
					if ( cformat == '.' ) {
						dot_found = true;
						continue;
					}
					if ( dot_found ) {
						// part of the precision...
						sprecision[iprecision] = cformat;
						++iprecision;
					}
					else {
						// part of the width...
						swidth[iwidth] = cformat;
						++iwidth;
					}
					continue;
				}
				if ( (cformat != 'd') && (cformat != 'f') && (cformat != 'F') && (cformat != 's') ) {
					Message.printWarning ( 3, "StringUtil.formatString", "Invalid format string character (" + cformat + ") in format (" + format + ").");
					break;
				}
				// If here, have a valid format string and need to process...

				// First get the width and precision on the format...

				// Get the desired output width and precision (already initialize to zeros above)...

				if ( iwidth > 0 ) {
					width = Integer.parseInt ((new String(swidth)).substring(0,iwidth));
				}

				if ( iprecision > 0 ) {
					precision = Integer.parseInt ((new String(sprecision)).substring(0,iprecision));
				}

				// Check to see if the number of formats is greater than the input list.  If so, this
				// is likely a programming error so print a warning so the developer can fix.

				if ( vindex > vsizem1 ) {
					Message.printWarning ( 3, "StringUtil.formatString",
					"The number of format strings \"" + format + "\" is > the number of data.  Check code." );
					return buffer.toString();
				}

				// Now format for the different data types...

				if ( cformat == 'd' ) {
				    // Integer.  If NULL or an empty string, just add a blank string of the desired width...
					if ( v.get(vindex) == null ) {
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, "StringUtil.formatString", "NULL integer" );
						}
						// NULL string.  Set it to be spaces for the width requested.
						for ( i = 0; i < width; i++ ){
							buffer.append ( ' ' );
						}
						++vindex;
						break;
					}
					StringBuilder temp = new StringBuilder (v.get(vindex).toString());
					if ( temp.length() == 0 ) {
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, "StringUtil.formatString", "Zero length string for integer" );
						}
						// Empty string.  Set it to be spaces for the width requested.
						for ( i = 0; i < width; i++ ){
							buffer.append ( ' ' );
						}
						++vindex;
						break;
					}
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, "StringUtil.formatString",
						"Processing list[" + vindex + "], an integer \"" + temp + "\"" );
					}
					++vindex;
					cvalue = temp.charAt ( 0 );
					if ( cvalue == '-' ) {
						sign = 1;
					}
					else {
					    sign = 0;
					}
					// String will be left-justified so we need to see if we need to shift
					// right.  Allow overflow.  "temp" already has the sign in it.
					length_temp = temp.length();
					diff =	width - length_temp;
					if ( diff > 0 ){
						if ( left_shift ) {
							if ( zero_format ) {
								// Need to add zeros in the front...
								if ( sign == 1 ) {
									offset = 1;
								}
								else {
									offset = 0;
								}
								for ( j = 0; j < diff; j++) {
									temp.insert(offset, '0');
								}
							}
							else {
								// Add spaces at the end...
								for ( j = 0; j < diff; j++){
									temp.insert( length_temp,' ');
								}
							}
						}
						else {
							// Add spaces at the beginning...
							if ( sign == 1 ) {
								offset = 1;
							}
							else {
								offset = 0;
							}
							if ( zero_format ) {
								// Add zeros...
								for ( j = 0; j < diff; j++) {
									temp.insert(offset,'0');
								}
							}
							else {
								for ( j = 0; j < diff; j++) {
									temp.insert(0, ' ');
								}
							}
						}
					}
					buffer.append ( temp );
				}
				else if	( (cformat == 'f') || (cformat == 'F')){
					// Float.  First, get the whole number as a string...
					// If NULL, just add a blank string of the desired width...
					if ( v.get(vindex) == null ) {
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, "StringUtil.formatString", "NULL float" );
						}
						// NULL string.  Set it to be spaces for the width requested.
						for ( i = 0; i < width; i++ ){
							buffer.append ( ' ' );
						}
						++vindex;
						break;
					}
					StringBuilder temp = new StringBuilder();
					String whole_number_string;
					String remainder_string;
					String number_as_string = "";
					int	point_pos;
					if ( cformat == 'f' ) {
						number_as_string = v.get(vindex).toString();
					}
					else if ( cformat == 'F' ) {
						number_as_string = v.get(vindex).toString();
					}
					if ( number_as_string.length() == 0 ) {
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, "StringUtil.formatString", "Zero length string for float" );
						}
						// Empty string.  Set it to be spaces for the width requested.
						for ( i = 0; i < width; i++ ){
							buffer.append ( ' ' );
						}
						++vindex;
						break;
					}
					else if ( number_as_string.equals("NaN") ) {
					    // Pad with spaces and justify according to the formatting.
					    if ( left_shift ) {
					        buffer.append ( "NaN" );
		                    for ( i = 0; i < (width - 3); i++ ){
	                            buffer.append ( ' ' );
	                        }
					    }
					    else {
                            for ( i = 0; i < (width - 3); i++ ){
                                buffer.append ( ' ' );
                            } 
                            buffer.append ( "NaN" );
					    }
		                ++vindex;
					    break;
					}
					// Need to check here as to whether the number is less than 10^-3 or greater
					// than 10^7, in which case the string comes back in exponential notation
					// and fouls up the rest of the process...
					int E_pos = number_as_string.indexOf('E');
					if ( E_pos >= 0 ) {
						// Scientific notation.  Get the parts to the number and then
						// put back together.  According to the documentation, the
						// format is -X.YYYE-ZZZ where the first sign is optional, the first digit (X)
						// is mandatory (and non-zero), the YYYY are variable length, the sign after the E is
						// mandatory, and the exponent is variable length.  The sign after the E appears to be optional.
						if ( Message.isDebugOn ) {
							Message.printDebug(dl, "StringUtil.formatString",
							"Detected scientific notation for Double: " + number_as_string );
						}
						StringBuilder expanded_string = new StringBuilder ();
						int sign_offset = 0;
						if ( number_as_string.charAt(0) == '-' ) {
							expanded_string.append("-");
							sign_offset = 1;
						}
						// Position of dot in float...
						int dot_pos = number_as_string.indexOf('.');
						// Sign of the exponent...
						char E_sign = number_as_string.charAt( E_pos+1);
						// Exponent as an integer...
						int exponent = 0;
						if ( (E_sign == '-') || (E_sign == '+') ) {
							exponent = atoi( number_as_string.substring( E_pos + 2) );
						}
						else {
							// No sign on exponent.
							exponent = atoi( number_as_string.substring( E_pos + 1) );
						}
						// Left side of number...
						String left = number_as_string.substring(sign_offset, dot_pos);
						// Right side of number...
						String right = number_as_string.substring( (dot_pos + 1), E_pos );
						// Add to the buffer on the left side of the number...
						if ( E_sign == '-' ) {
							// Add zeros on the left...
							int dot_shift =	exponent - 1;
							expanded_string.append(	"." );
							for ( int ishift = 0; ishift < dot_shift; ishift++ ) {
								expanded_string.append("0");
							}
							expanded_string.append(left);
							expanded_string.append(right);
						}
						else {
							// Shift the decimal to the right...
							expanded_string.append( left );
							// Now transfer as many digits as available.
							int len_right = right.length();
							for ( int ishift = 0; ishift < exponent; ishift++ ) {
								if ( ishift <= (len_right - 1) ) {
									expanded_string.append( right.charAt(ishift) );
								}
								else {
									expanded_string.append("0");
								}
							}
							expanded_string.append( "." );
							// If we did not shift through all the original right-side digits, add them now...
							if ( exponent < len_right ) {
								expanded_string.append( right.substring( exponent ) );
							}
						}
						// Now reset the string...
						number_as_string = expanded_string.toString();
						if ( Message.isDebugOn ) {
							Message.printDebug(dl, "StringUtil.formatString",
							"Expanded number: \"" + number_as_string + "\"" );
						}
					}
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, "StringUtil.formatString",
						"Processing list[" + vindex + "], a float or double \"" + number_as_string + "\"" );
					}
					++vindex;
					// Figure out if negative...
					if ( number_as_string.charAt(0) == '-'){
						sign = 1;
					}
					else {
						sign = 0;
					}
					// Find the position of the decimal point...
					point_pos = number_as_string.indexOf ( '.' );
					if ( point_pos == -1 ) {
						// No decimal point.
						whole_number_string = number_as_string;
						remainder_string = "";
					}
					else {
						// has decimal point
						whole_number_string = number_as_string.substring(0,point_pos);
						remainder_string = number_as_string.substring(point_pos + 1);
					}
					// Round the number so that the number of precision digits exactly matches what we want...
					if ( precision < remainder_string.length() ) {
						number_as_string = StringUtil.round( number_as_string, precision );
						// We may need to recompute the parts of the string.  Just do it for now...
						// Figure out if negative...
						if ( number_as_string.charAt(0) == '-'){
							sign = 1;
						}
						else {
							sign = 0;
						}
						// Find the position of the decimal point...
						point_pos = number_as_string.indexOf ( '.' );
						if ( point_pos == -1 ) {
							// No decimal point.
							whole_number_string = number_as_string;
							remainder_string = "";
						}
						else {
							// has decimal point
							whole_number_string = number_as_string.substring(0,point_pos);
							remainder_string = number_as_string.substring(point_pos + 1);
						}
					}
					// Now start at the back of the string and start adding parts...
					if ( precision > 0 ) {
						int iprec;
						// First fill with zeros for the precision amount...
						for ( iprec = 0; iprec < precision; iprec++ ) {
							temp.insert ( 0, '0' );
						}
						// Now overwrite with the actual numbers...
						iend = remainder_string.length();
						if ( iend > precision ) {
							iend = precision;
						}
						for ( iprec = 0; iprec < iend; iprec++ ) {
							temp.setCharAt ( iprec, remainder_string.charAt(iprec) );
						}
						// Round off the last one if there is truncation.  Deal with this later...
						if ( precision < remainder_string.length() ) {
							// TODO - old comment: working on doing the round above...
						}
						// Now add the decimal point...
						temp.insert ( 0, '.' );
					}
					else if ( (precision == 0) && pound_format ) {
						// Always add a decimal point...
						temp.insert ( 0, '.' );
					}
					// Now add the whole number.  If it overflows, that is OK.  If it is
					// less than the width we will deal with it in the next step.
					temp.insert ( 0, whole_number_string );
					// If the number that we have now is less than the desired width, we need
					// to add spaces.  Depending on the sign in the format, we add them at the left or right.
					if ( temp.length() < width ) {
						int ishift;
						iend = width - temp.length();
						if ( left_shift ) {
							// Add at the end...
							for ( ishift = 0; ishift < iend; ishift++ ) {
								temp.insert ( temp.length(), ' ' );
							}
						}
						else {
							// Add at the end..
							for ( ishift = 0; ishift < iend; ishift++ ) {
								if ( zero_format ) {
									// Format was similar to "%05.1f"
									temp.insert ( 0, '0' );
								}
								else {
									temp.insert ( 0, ' ' );
								}
							}
						}
					}
				
					// Append to our main string...
					buffer.append ( temp );
				}
				else if ( cformat == 's' ) {
					// First set the string the requested size, which is the precision.  If the
					// precision is zero, do the whole thing.  String will be left-justified so we
					// need to see if we need to shift right.  Allow overflow...
					// If NULL, just add a blank string of the desired width...
					if ( v.get(vindex) == null ) {
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, "StringUtil.formatString", "NULL string");
						}
						// NULL string.  Set it to be spaces for the width requested.
						for ( i = 0; i < precision; i++ ) {
							buffer.append ( ' ' );
						}
						++vindex;
						break;
					}
					StringBuilder temp = new StringBuilder ( v.get(vindex).toString());
					if ( temp.length() == 0 ) {
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, "StringUtil.formatString", "Zero length string" );
						}
						// Empty string.  Set it to be spaces for the width requested.
						for ( i = 0; i < width; i++ ){
							buffer.append ( ' ' );
						}
						++vindex;
						break;
					}
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, "StringUtil.formatString",
						"Processing list[" + vindex + "], a string \"" + temp + "\"" );
					}
					++vindex;
					if ( iprecision > 0 ) {
						// Now figure out whether we need to right-justify...
						diff = precision - temp.length();
						if ( !left_shift ) {
							// Right justify...
							if ( diff > 0 ) {
								for ( j = 0; j < diff; j++) {
									temp.insert(0, ' ');
								}
							}
						}
						else {
							// Left justify.  Set the buffer to the precision...
							temp.setLength ( precision );
							// Now fill the end with spaces instead of NULLs...
							for ( j = (precision - diff); j < precision; j++ ){
								temp.setCharAt( j, ' ');
							}
						}
						// If our string length is longer than the string, append a substring...
						if ( temp.length() > precision ) {
							buffer.append ( temp.toString().substring(0,precision));
						}
						else {
							// Do the whole string...
							buffer.append ( temp.toString());
						}
					}
					else {
						// Write the whole string...
						if ( temp != null ) {
							buffer.append ( temp );
						}
					}
				}
				// End of a format string.  Break out and look for the next one...
				break;
			}
		}
		else {
			// A normal character so just add to the buffer...
			buffer.append ( cformat );
		}
	}

	return buffer.toString ();
}

// Simple variations on formatString for single objects...
// TODO SAM 2010-06-15 Need to figure out how to not create lists on each call, but needs to be thread safe

/**
Format a double as a string.
@return Formatted string.
@param d A double to format.
@param format Format to use.
*/
public static final String formatString ( double d, String format )
{	List<Double> v = new ArrayList<>(1);
	v.add ( new Double(d) );
	return formatString ( v, format );
}

/**
Format a Double as a string.
@return Formatted string.
@param d A Double to format.
@param format Format to use.
*/
public static final String formatString ( Double d, String format )
{	List<Double> v = new ArrayList<>(1);
	v.add ( d );
	return formatString ( v, format );
}

/**
Format a float as a string.
@return Formatted string.
@param f A float to format.
@param format Format to use.
*/
public static final String formatString ( float f, String format )
{	List<Float> v = new ArrayList<>(1);
	v.add ( new Float(f) );
	return formatString ( v, format );
}

/**
Format an int as a string.
@return Formatted string.
@param i An int to format.
@param format Format to use.
*/
public static final String formatString ( int i, String format )
{	List<Integer> v = new ArrayList<>(1);
	v.add ( new Integer(i) );
	return formatString ( v, format );
}

/**
Format an Integer as a string.
@return Formatted string.
@param i An Integer to format.
@param format Format to use.
*/
public static final String formatString ( Integer i, String format )
{	List<Integer> v = new ArrayList<>(1);
	v.add ( i );
	return formatString ( v, format );
}

/**
Format a long as a string.
@return Formatted string.
@param l A long to format.
@param format Format to use.
*/
public static final String formatString ( long l, String format )
{	List<Long> v = new ArrayList<>(1);
	v.add ( new Long(l) );
	return formatString ( v, format );
}

/**
Format an object as a string.
@return Formatted string.
@param o An object to format.
@param format Format to use.
*/
public static final String formatString ( Object o, String format )
{	List<Object> v = new ArrayList<Object>( 1 );
	v.add ( o );
	return formatString ( v, format );
}

/**
Format a string for output to a CSV file.  The following actions are taken:
<ol>
<li> If the string contains a comma or "alwaysQuote" is true, the string is surrounded by quotes.</li>
<li> If the string contains double quotes, each double quote is replaced with two double quotes, as per Excel conventions.
</ol>
@param s string to process
@param treatAsString if true, always return a result enclosed in double quotes, regardless of contents.
@return the string formatted for inclusion as an item in an Excel CSV file.
*/
public static String formatStringForCsv ( String s, boolean alwaysQuote )
{
	StringBuilder b = new StringBuilder();
    if ( alwaysQuote || (s.indexOf(",") >= 0) ) {
        b.append ( "\"" );
    }
    int length = s.length();
    char c;
    for ( int i = 0; i < length; i++ ) {
        c = s.charAt(i);
        if ( c == '"' ) {
            // Detected a quote so output double quotes.
            b.append ( "\"\"" );
        }
        else {
            // Just append the character
            b.append ( c );
        }
    }
    if ( alwaysQuote || (s.indexOf(",") >= 0) ) {
        b.append ( "\"" );
    }
    return b.toString();
}

/**
Return an array of valid format specifiers for the formatString() method, in
the format "%X - Description" where X is the format specifier.  The specifiers correspond to the C sprintf
formatting routine.
@return an array of format specifiers.
@param includeDescription If false, only the %X specifiers are returned.  if
True, the description is also returned.
@param forOutput if true, return specifiers for formatting; if false only include formatters for parsing
*/
public static String[] getStringFormatSpecifiers(boolean includeDescription, boolean forOutput )
{   String [] formats = new String[12];
    formats[0] = "%% - literal percent";
    formats[1] = "%c - single character";
    formats[2] = "%d - integer";
    formats[3] = "%4d - integer, 4-digit width";
    formats[4] = "%-d - integer, left justified";
    formats[5] = "%-4d - integer, 4-digit width, left justified";
    formats[6] = "%f - floating point";
    formats[7] = "%8.2f - floating point, 8 digits wide, 2 decimls";
    formats[8] = "%-8.2f - floating point, 8 digits wide, 2 decimls, left justified";
    formats[9] = "%-f - floating point, left justified";
    formats[10] = "%s - string";
    formats[11] = "%20.20s - string padded to width";
    return formats;
}

/**
Return a token in a string or null if no token.  This method calls
breakStringList() and returns the requested token or null if out of range.
@param string The string to break.
@param delim A String containing characters to treat as delimiters.
@param flag Bitmask indicating how to break the string.  Specify
DELIM_SKIP_BLANKS to skip blank fields and
DELIM_ALLOW_STRINGS to allow quoted strings (which may contain delimiters).
@param token Token to return (starting with 0).
@return the requested token or null.
*/
public static String getToken ( String string, String delim, int flag, int token )
{	if ( token < 0 ) {
		return null;
	}
	List<String> v = breakStringList ( string, delim, flag );
	if ( v == null ) {
		return null;
	}
	if ( v.size() < (token + 1) ) {
		return null;
	}
	return v.get(token);
}

/**
Process lists of strings to handle include and exclude.
The returned list is populated first considering the "includeFirst" parameter and the other list is removed.
The returned list is typically an overall list of strings to include.
This could be used, for example, to indicate which columns in a table to include in processing.
@param initialList An initial list to check for inclusion,
if null then includeList will be used and should not contain regular expressions.
@param includeList A list of strings to include, can be null.
@param excludeList A list of strings to exclude, can be null.
@param includeFirst If true, the initial list is checked against the "includeList".
If false, the list is first populated with "excludeList".
The second list is then removed from the first list
@param ignoreCase if true, the comparison of strings ignores case.
@param checkRegex if true, compare strings by allowing Java regular expressions (String.matches()).
In this case, if ignoreCase=true, strings are converted to uppercase before evaluating.
Original input with globbing style wildcard "*" must have been converted to Java style wildcard ".*".
*/
public static List<String> includeExcludeStrings ( List<String> initialList,
	List<String> includeList, List<String> excludeList,
	boolean includeFirst,
	boolean ignoreCase,
	boolean checkRegex ) {
    List<String> returnList = new ArrayList<>();
    List<String> listToRemove = null;
    List<String> listToInclude = null;
    // Initialize list.
    if ( includeFirst ) {
    	listToInclude = includeList;
    	listToRemove = excludeList;
    }
    else {
    	listToInclude = excludeList;
    	listToRemove = includeList;
    }
    // Populate the return list
    // - only add strings that are in the "listToInclude"
    if ( initialList == null ) {
        // If the initial list is null, use the include list
    	initialList = new ArrayList<>();
    	if ( includeList != null ) {
    		initialList.addAll(includeList);
    	}
    }
	// Add to the return list from the include list
    for ( String s1 : initialList ) {
	    // Search the include list
	    // - if a match, add to the list
	    for ( String s2 : listToInclude ) {
		    if ( checkRegex ) {
			    // Checking regular expression
			    if ( ignoreCase ) {
				    // Can't use simple string comparison, so convert to upper-case.
				    if ( s1.toUpperCase().matches(s2.toUpperCase()) ) {
					    returnList.add(s1);
				    }
			    }
			    else {
				    if ( s1.matches(s2) ) {
					    returnList.add(s1);
				    }
			    }
		    }
		    else {
			    // Not checking regular expression so can just do string comparison.
			    if ( ignoreCase ) {
				    if ( s1.equalsIgnoreCase(s2) ) {
					    returnList.add(s1);
				    }
			    }
			    else {
				    if ( s1.equals(s2) ) {
					    returnList.add(s1);
				    }
			    }
		    }
	    }
    }
    // Loop through the initial list
    // - must use indices because list size is changed during looping
    int s1Size = returnList.size();
    String s1;
    for ( int is1 = 0; is1 < s1Size; is1++ ) {
    	s1 = returnList.get(is1);
    	// Search the exclude list
    	// - if a match, remove from the list
    	for ( String s2 : listToRemove ) {
    		if ( checkRegex ) {
    			// Checking regular expression
    			if ( ignoreCase ) {
    				// Can't use simple string comparison, so convert to upper-case.
    				if ( s1.toUpperCase().matches(s2.toUpperCase()) ) {
    					returnList.remove(s1);
    					--is1;
    					--s1Size;
    				}
    			}
    			else {
    				if ( s1.matches(s2) ) {
    					returnList.remove(s1);
    					--is1;
    					--s1Size;
    				}
    			}
    		}
    		else {
    			// Not checking regular expression so can just do string comparison.
    			if ( ignoreCase ) {
    				if ( s1.equalsIgnoreCase(s2) ) {
    					returnList.remove(s1);
    					--is1;
    					--s1Size;
    				}
    			}
    			else {
    				if ( s1.equals(s2) ) {
    					returnList.remove(s1);
    					--is1;
    					--s1Size;
    				}
    			}
    		}
    	}
    }
    return returnList;
}

/**
Evaluate a list of strings and return a new list that matches strings to include.
The returned list includes only strings in the included list.
This could be used, for example, to indicate which columns in a table to include in processing.
@param initialList An initial list of strings to evaluate, none of which are regex.
@param includeList A list of strings to include, which can include regex.
@param ignoreCase if true, the comparison of strings ignores case.
@param checkRegex if true, compare strings by allowing Java regular expressions (String.matches()).
In this case, if ignoreCase=true, strings are converted to uppercase before evaluating.
Original input with globbing style wildcard "*" must have been converted to Java style wildcard ".*".
*/
public static List<String> includeStrings ( List<String> initialList, List<String> includeList,
	boolean ignoreCase, boolean checkRegex ) {
    List<String> returnList = new ArrayList<>();
    // Loop through the include list
    // - only add strings that are in the second list
    for ( String s1 : initialList ) {
    	// List through the second list
    	// - if a match, remove from the list
    	for ( String s2 : includeList ) {
    		if ( checkRegex ) {
    			// Checking regular expression
    			if ( ignoreCase ) {
    				// Can't use simple string comparison, so convert to upper-case.
    				if ( s1.toUpperCase().matches(s2.toUpperCase()) ) {
    					returnList.add(s1);
    				}
    			}
    			else {
    				if ( s1.matches(s2) ) {
    					returnList.add(s1);
    				}
    			}
    		}
    		else {
    			// Not checking regular expression so can just do string comparison.
    			if ( ignoreCase ) {
    				if ( s1.equalsIgnoreCase(s2) ) {
    					returnList.add(s1);
    				}
    			}
    			else {
    				if ( s1.equals(s2) ) {
    					returnList.add(s1);
    				}
    			}
    		}
    	}
    }
    return returnList;
}

// TODO SAM 2009-06-01 Evaluate whether to deprecate, etc given that it should not be ignore case
// based on the name of the method.
/**
Return index of string in string list.  If string is not in string list,
-1 is returned.  <b>A case-insensitive compare is used.</b>
@return Index of string in stringlist (or -1).
@param stringlist List of strings to search.
@param searchString String to return index of.
*/
public static int indexOf ( List<String> stringlist, String searchString )
{	if ( stringlist == null || searchString == null ) {
		return -1;
	}
	int num_strings = stringlist.size();
	String currentString;
	for ( int i=0; i<num_strings; i++ ) {
		currentString = stringlist.get(i);
		if (currentString == null) {
			// skip
		}
		else if ( currentString.equalsIgnoreCase ( searchString )) {
			return i;	
		}
	}
	return -1;
}

/**
Return index of string in string list.  If string is not in string list,
-1 is returned.  A case-insensitive compare is used.
@return Index of string in stringlist (or -1).
@param stringlist List of strings to search.
@param searchString String to return index of.
*/
public static int indexOfIgnoreCase ( List<String> stringlist, String searchString )
{	if ( stringlist == null || searchString == null ) {
		return -1;
    }

	int num_strings = stringlist.size();
	String currentString;
	for ( int i=0; i<num_strings; i++ ) {
		currentString = stringlist.get(i);
		if (currentString == null) {
			// skip
		}
		else if ( currentString.equalsIgnoreCase ( searchString )) {
			return i;	
		}
	}
	return -1;
}

/**
Determine whether a string exists in another string, ignoring case.
@param full Full string to check.
@param substring Substring to find in "full".
@param fromIndex The index where the search should begin.
@return position of substring or -1 if not found.
*/
public static int indexOfIgnoreCase ( String full, String substring, int fromIndex )
{	// Convert both strings to uppercase and then do the comparison.
	String full_up = full.toUpperCase();
	String substring_up = substring.toUpperCase();
	int pos = full_up.indexOf ( substring_up, fromIndex );
	return pos;
}

/**
Return index of substring in string list.  If string is not in string list,
-1 is returned.  A case-insensitive compare is used.
@return Index of string in stringlist (or -1).
@param stringlist List of strings to search.
@param searchString String to return index of.
*/
public static int indexOfSubstringIgnoreCase ( List<String> stringlist, String searchString )
{	if ( stringlist == null || searchString == null ) {
		return -1;
    }

	int num_strings = stringlist.size();
	String currentString;
	for ( int i=0; i<num_strings; i++ ) {
		currentString = stringlist.get(i);
		if (currentString == null) {
			// skip
		}
		else if ( currentString.toUpperCase().indexOf ( searchString.toUpperCase()) >= 0 ) {
			return i;	
		}
	}
	return -1;
}


/**
Determine whether a string is an ASCII string.
@return true if the string is an ASCII string.
@param s String to check.
*/
public static boolean isASCII( String s )
{	int sLength = s.length();
	char [] c = new char[sLength];

	// Get character array
	try {
		s.getChars(0,sLength,c,0);
	} catch(StringIndexOutOfBoundsException SIOOBe) {
		return false;
	}

	// Loop through character array checking to make sure it is ASCII
	for(int i=0;i<sLength;i++) {
		if((!Character.isLetterOrDigit(c[i]) && !Character.isWhitespace(c[i]) &&
			c[i] != '.' && c[i] != '-' && c[i] != '_') ||
			(charToHex(c[i])).compareTo("007F") > 0)
		{
			return false;
		}
	}

	// Return true if it makes it to here
	return true;
}

/**
Determine whether a string can be converted to a boolean.
@return true if the string can be converted to a boolean ("true" or "false"), false otherwise.
@param s String to convert.
*/
public static boolean isBoolean( String s )
{	if ( s == null ) {
        return false;
    }
	if ( s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false") ) {
		return true;
	}
	return false;
}

/**
Determine whether a string is a double precision value.
@return true if the string can be converted to a double.
@param s String to convert.
*/
public static boolean isDouble( String s )
{	if ( s == null ) {
        return false;
    }
    try {
        new Double( s.trim() ).doubleValue();
		return true;
	}
	catch( NumberFormatException e ){
		return false;
	}
}

/**
Determine whether a string can be converted to an integer.
@return true if the string can be converted to a integer.
@param s String to convert.
*/
public static boolean isInteger( String s )
{	if ( s == null ) {
        return false;
    }
    try {
        Integer.parseInt( s.trim() );
		return true;
	}
	catch( NumberFormatException e ){
		return false;
	}
}

/**
Determine whether a string can be converted to a long.
@return true if the string can be converted to a long.
@param s String to convert.
*/
public static boolean isLong(String s)
{   if ( s == null ) {
        return false;
    }
	try {
		new Long(s).longValue();
		return true;
	}
	catch (NumberFormatException e) {
		return false;
	}
}	

/**
Wrap text by breaking a string into lines that are less than or equal to a desired length.
@return the text as a new string delimited by the line break characters.
@param string String to wrap.
@param maxlength Maximum length of the string.
@param linebreak Character(s) to insert at the end of a line (e.g., "\n").
If not specified, "\n" is used.
*/
public static String lineWrap ( String string, int maxlength, String linebreak )
{	if ( (linebreak == null) || linebreak.equals("") ) {
		linebreak = "\n";
	}
	if ( string == null ) {
		return linebreak;
	}
	if ( string.length() <= maxlength ) {
		return string + linebreak;
	}
	// For now just do breakStringList() using white space.  However, need
	// to consider commas, etc.  One idea would be to loop through the
	// string and start at the next maxlength point.  Then go back to find
	// a delimiter.  If none is found, insert one somewhere or move forward.
	//
	// Also need to consider Tom's code.
	List<String> v = breakStringList ( string, " \t\n", 0 );
	int size = 0; 
	if ( v != null ) {
		size = v.size();
	}
	StringBuilder main_buffer = new StringBuilder();
	StringBuilder sub_buffer = new StringBuilder();
	String token = null;
	for ( int i = 0; i < size; i++ ) {
		token = v.get(i);
		if ((sub_buffer.length() + 1 + token.length()) > maxlength ){
			// Add the sub_buffer to the buffer...
			main_buffer.append ( sub_buffer.toString() + linebreak);
			sub_buffer.setLength(0);
			sub_buffer.append ( token );
		}
		else {
			// Add the token to the sub_buffer...
			sub_buffer.append( " " + token );
		}
	}
	if ( sub_buffer.length() > 0 ) {
		main_buffer.append ( sub_buffer.toString() + linebreak);
	}
	return main_buffer.toString();
}

/**
Convert a string containing literal representations of characters (e.g., "\t") to
the internal equivalent (e.g., tab character).  This should be used with care, for example
to convert a visual delimiter string into the internal equivalent.  The string combinations
that are recognized are: "\t" (tab) and "\s" (space).
@param s string to convert
@return the converted string, or null if the input string is null
*/
public static String literalToInternal ( String s )
{
    if ( s != null ) {
        s = s.replace("\\t", "\t");
        s = s.replace("\\s", " ");
    }
    return s;
}

/**
Determine the maximum size of the String in a list.
@param v list of objects to check the size.  The toString() method is called
to get a String representation of the object for the check.
@return the maximum size or -1 if it cannot be determined.
*/
public static int maxSize ( List<String> v )
{	int size = 0;
	int maxsize = -1;
	int len = 0;
	if ( v != null ) {
		len = v.size();
	}
	Object o;
	for ( int i = 0; i < len; i++ ) {
		o = v.get(i);
		if ( o == null ) {
			continue;
		}
		size = o.toString().length();
		if ( size > maxsize ) {
			maxsize = size;
		}
	}
	return maxsize;
}

/**
Determine if strings match, while ignoring uppercase/lowercase.  The input
strings are converted to uppercase before the comparison is made.
@return true if the string matches the regular expression, ignoring case.
@param s String to check.
@param regex Regular expression used as input to String.matches().
*/
public static boolean matchesIgnoreCase ( String s, String regex )
{	return s.toUpperCase().matches ( regex.toUpperCase() );
}

/**
** Notes:	(1)	This routine compares a candidate string with a string
**			that contains regular expression wildcard characters and
**			returns 1 if the candidate string matches.  The
**			following wild cards are currently recognized:
**
**				.	Match one character.
**				*	Match zero or more characters.
**				[...]	Match any one the characters enclosed
**					in the brackets.  This can be a list
**					of characters or a character range
**					separated by a -.
**		(2)	This routine is designed to be called by a higher level
**			routine to check actual filenames versus wildcard patterns.
**		(3)	The following combination is known not to work:
**
**				xxx*.[abc]
**
**			and will be fixed as time allows.
</pre>
@param ignore_case if true, case will be ignored when comparing strings.  If
false, strings will be compared literally.
@param candidate_string String to evaluate.
@param regexp_string Regular expression string to match.
@deprecated Use the standard String.matches() method or StringUtil.matchesIgnoreCase().
*/
public static boolean matchesRegExp ( boolean ignore_case, String candidate_string, String regexp_string )
{	String okchars = "", routine = "StringUtil.mtchesRegExp";
	int	dl = 50, nokchars = 0;
	boolean	asterisk = false, jumptotest = false;

	if ( candidate_string == null ) {
		return false;
	}
	if ( regexp_string == null ) {
		return false;
	}
	int candidate_len = candidate_string.length();
	int regexp_len = regexp_string.length();

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Comparing \"" + candidate_string + "\" to \"" + regexp_string + "\"" );
	}

	// Put in this quick check because the code does not seem to be
	// working but need to get something delivered for regular expressions that end in *...

	if ( regexp_string.endsWith("*") && (StringUtil.patternCount(regexp_string,"*") == 1) ) {
		// The regular expression is xxx* so do a quick check...
		int endpos = regexp_string.indexOf("*");
		if ( endpos == 0 ) {
			return true;
		}
		if ( candidate_string.length() < endpos ) {
			// Candidate string is not long enough to compare
			// needs to be as long as the regular expression without the *)...
			return false;
		}
		if ( ignore_case ) {
			if ( regexp_string.substring(0,endpos).equalsIgnoreCase(
				candidate_string.substring(0,endpos)) ) {
				return true;
			}
		}
		else {
		    if ( regexp_string.substring(0,endpos).equals(candidate_string.substring(0,endpos)) ) {
				return true;
			}
		}
	}

	// ican = position in candiate_string
	// ireg = position in regexp_string
	// ccan = character in candidate_string
	// creg = character in regexp_string
	int	ican = 0, ireg = 0;
	char ccan, creg;
	while ( true ) {
		// Start new segment in the regular expression...
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Start new segment section" );
		}
		if ( !jumptotest ) {
			asterisk = false;
			for ( ireg = 0; ireg < regexp_len; ireg++ ) {
				creg = regexp_string.charAt(ireg);
				if ( creg != '*' ) {
					break;
				}
				// Else equals '*'..
				asterisk = true;
			}
		}

		// Now test for a match...

		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,"Start test section" );
		}

		jumptotest = false;
		// i is position in regexp_string, j in candidate_string.
		while ( true ) {
			creg = regexp_string.charAt(ireg);
			for ( ican = 0; (ireg < regexp_len) && (creg != '*'); ireg++, ican++ ) {
				creg = regexp_string.charAt(ireg);
				if ( ican >= candidate_len ) {
					// No match...
					return false;
				}
				ccan = candidate_string.charAt(ican);
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"regexp_string[" + ireg + "]=" + creg + "candidate_string[" + ican + "]=" + ccan );
				}
				if ( creg != ccan ) {
					if ( creg == '.' ) {
						// Single character match...
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, routine, "Character . - go to next character" );
						}
						continue;
					}
					else if ( creg == '[' ) {
						// Start of character range.
						// First need to get OK characters...
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, routine, "[ - check range character" );
						}
						++ireg;
						while ( true ) {
							if ( ireg >=regexp_len){
								return false;
							}
							creg = regexp_string.charAt(ireg);
							if ( creg != ']' ) {
								break;
							}
							else if (creg == '-' ) {
								// Need to find the next character and then go until that matches...
								++ireg;
								if ( ireg >= regexp_len ) {
									return false;
								}
								creg =
								regexp_string.charAt(ireg);
								if ( (nokchars > 0) && (creg < okchars.charAt(nokchars - 1)) ) {
									return
									false;
								}
								if ( Message.isDebugOn ) {
									Message.printDebug (
									dl, routine, "Using range " + okchars.charAt(nokchars - 1) + " to " + creg );
								}
								while ( true ) {
									okchars += okchars.charAt(nokchars - 1) + 1;
									++nokchars;
									if ( Message.isDebugOn ) {
										Message.printDebug (
										dl, routine, "Added " + okchars.charAt(nokchars - 1) + " from [-] list" );
									}
									if ( okchars.charAt(nokchars - 1) == creg ) {
										// Last character in range...
										break;
									}
								}
							}
							else {
							    // Just add the character...
								okchars += creg;
								++nokchars;
								if ( Message.isDebugOn ) {
									Message.printDebug (
									dl, routine, "Added " + okchars.charAt(nokchars - 1) + " from [abc] list" );
								}
								++ireg;
							}
						}
						// Now check the character...
						if ( okchars.indexOf(ccan) >= 0 ) {
							// Matches OK...
							continue;
						}
						else {
						    // No match...
							return false;
						}
					}
					else if ( !asterisk ) {
						// ?
						if ( Message.isDebugOn ) {
							Message.printDebug ( dl, routine, "Not asterisk." );
						}
						return false;
					}
					// increment candidate...
					++ican;
					// Reevaluate the loop again...
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Jumping to test" );
					}
					jumptotest = true;
					break;
				}
				else {
				    if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Chars are equal.  Increment..." );
					}
				}
			}
			if ( jumptotest || (ireg >= regexp_len) || (creg == '*') ) {
				break;
			}
		}

		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Outside for loop" );
		}

		if ( !jumptotest ) {
			if ( creg == '*' ) {
				//if ( Message.isDebugOn ) {
				//	Message.printDebug ( dl, routine, "Have an * - increment by " + i + " and restart segment" );
				//}
				// Don't need?
				//pt_candidate	+= j;
				//pt_regexp	+= i;
				continue;
			}

			if ( ican >= candidate_len ) {
				// End of string...
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "End of string." );
				}
				return true;
			}
			else if ( (ireg > 0) && (regexp_string.charAt(ireg - 1) == '*') ) {
				// Rest of string is wildcard...
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Rest of string *." );
				}
				return true;
			}
			else if ( !asterisk ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Not asterisk." );
				}
				return false;
			}
			// Don't need?
			//++pt_candidate;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Jumping to test" );
			}
			jumptotest = true;
		}
	}
}

/**
Check to see if a String matches a regular expression, considering case explicitly.
@param candidate_string String to evaluate.
@param regexp_string Regular expression string to match.
@return true if the candidate string matches the regular expression.
@deprecated Use the standard String.matches() method or StringUtil.matchesIgnoreCase().
*/
public static boolean matchesRegExp ( String candidate_string, String regexp_string )
{	return matchesRegExp ( false, candidate_string, regexp_string );
}

/**
 * Parse a dictionary string of format:
 *     key1: value1, key2: value2
 * If value contains special characters (: and ,), it can be surrounded by single quotes to escape, for example:
 *     key1: 'value1', key2: 'value2'
 * @param dictString the dictionary string
 * @return a LinkedHashMap with the dictionary, used to maintain the order of the dictionary.
 * Any quoted values will have quotes removed.
 */
public static HashMap<String,String> parseDictionary ( String dictString ) {
    HashMap<String,String> dict = new LinkedHashMap<String,String>();
    // Use Message debug level 1 to see processing, remove when logic is confirmed
    // Because the value may be surrounded by double quotes, have to parse
    if ( (dictString != null) && (dictString.length() > 0) && (dictString.indexOf(":") > 0) ) {
    	if ( dictString.indexOf("'") < 0) {
    		// Legacy code that works when no surrounding quotes
    		// First break map pairs by comma
            List<String>pairs = StringUtil.breakStringList(dictString, ",", 0 );
            // Now break pairs and put in hashtable
            for ( String pair : pairs ) {
                String [] parts = pair.split(":");
                dict.put(parts[0].trim(), parts[1].trim() );
            }
    	}
    	else {
    		// Have surrounding quotes so break out token by token
    		// - this code may replace the above but the above worked so keep until tested out
    		int pos = -1;
    		int posLast = dictString.length() - 1;
    		boolean inKey = false;
    		boolean inValue = false;
    		boolean quoted = false;
    		char c;
    		StringBuilder key = new StringBuilder(), value = new StringBuilder(); // Define so use length to check
    		while ( pos < posLast ) {
    			++pos; // Increment character
    			c = dictString.charAt(pos);
    			if ( Message.isDebugOn ) {
    				Message.printDebug(1,"","pos=" + pos + " c=" + c + " inKey=" + inKey + " inValue=" + inValue +
    					" quoted=" + quoted + " keyLength=" + key.length() + " key=" + key + " value=" + value);
    			}
    			// Loop through the dictionary string
    			if ( !inKey && !inValue && (key.length() == 0) ) {
    				if ( Message.isDebugOn ) {
    					Message.printDebug(1,"","Searching for key");
    				}
    				// Searching for the next key
    				if ( Character.isWhitespace(c) || c == ',' ) {
    					// Can skip characters
    					continue;
    				}
    				else {
    					// Found a key, may be surrounded by single quotes
    					if ( Message.isDebugOn ) {
    						Message.printDebug(1,"","Found key");
    					}
   						inKey = true;
   						inValue = false;
   						key = new StringBuilder();
    					if ( c == '\'' ) {
    						quoted = true;
    					}
    					else {
    						// Other character, append
    						key.append(c);
    					}
    				}
    			}
    			else if ( inKey ) {
    				// In a key
   					if ( Message.isDebugOn ) {
   						Message.printDebug(1,"","In key");
   					}
    				if ( (c == '\'') && quoted )  {
    					// Found ending quote
    					inKey = false;
    					quoted = false;
    				}
    				else if ( !quoted && (Character.isWhitespace(c) || (c == ':')) ) {
    					// Not quoted, found ending delimiter
    					inKey = false;
    				}
    				else {
    					// Add the character to the key
    					key.append(c);
    				}
    			}
    			else if ( !inKey && !inValue && (key.length() > 0) )  {
    				// Found the key but have not yet started the value.
   					if ( Message.isDebugOn ) {
   						Message.printDebug(1,"","Searching for value");
   					}
    				if ( Character.isWhitespace(c) ) {
    					// Ignore the character
    					continue;
    				}
    				else if ( c == ':' ) {
    					// Value is starting
    					value = new StringBuilder();
    				}
    				else if ( c == '\'' ) {
    					// Value is quoted
    					quoted = true;
    					inValue = true;
    				}
    				else {
    					// Any other character
    					quoted = false;
    					inValue = true;
    					value.append(c);
    				}
    			}
    			else if ( inValue ) {
    				// In a value
    				if ( (c == '\'') && quoted )  {
    					// Found ending quote
    					inValue = false;
    					quoted = false;
    					dict.put(key.toString().trim(), value.toString().trim());
    					key.setLength(0); // Zero out so can start again
    					inKey = false;
    					inValue = false;
    					quoted = false;
    				}
    				else if ( !quoted && ((c == ',') || (pos == posLast)) ) {
    					// Not quoted, found ending delimiter
    					inValue = false;
    					dict.put(key.toString().trim(), value.toString().trim());
    					key.setLength(0); // Zero out so can start again
    					inKey = false;
    					inValue = false;
    					quoted = false;
    				}
    				else {
    					// Character in the value
    					value.append(c);
    				}
    			}
    		}
    	}
    }
	return dict;
}

/**
Parse a string like "1, 2, 3" or "1,2,3" or "1-3,4,6-10" into an array containing the numbers.
Single values result in a range where the start and end value are the same.
@param seq string to parse
@param delim delimiter characters
@param parseFlag see breakStringList() flag
@param offset number to add to each value, useful when converting from user-specified values to
internal zero-index values - specify zero if no offset is required
@return an array of integers parsed from the string.
*/
public static int [][] parseIntegerRangeSequence ( String seq, String delim, int parseFlag, int offset )
{
    if ( seq == null ) {
        return new int[0][0];
    }
    List<String> tokens = breakStringList ( seq, delim, parseFlag );
    int size = 0;
    if ( tokens != null ) {
        size = tokens.size();
    }
    if ( size == 0 ) {
        return new int[0][0];
    }
    else {
        int[][] ranges = new int[size][2];
        for ( int i = 0; i < size; i++ ) {
            String token = tokens.get(i);
            if ( token.indexOf("-") > 0 ) {
                // Range.  Split out the start and end of the range
                List<String> tokens2 = breakStringList ( token, "-", 0 );
                ranges[i][0] = Integer.parseInt(tokens2.get(0).trim()) + offset;
                ranges[i][1] = Integer.parseInt(tokens2.get(1).trim()) + offset;
            }
            else {
                // Single number
                ranges[i][0] = Integer.parseInt(token.trim()) + offset;
                ranges[i][1] = ranges[i][0];
            }
        }
        return ranges;
    }
}

/**
Parse a string like "1, 2, 3" or "1,2,3" into an array containing the numbers.
@param seq string to parse
@param delim delimiter characters
@param parseFlag see breakStringList() flag
@return an array of integers parsed from the string.
*/
public static int [] parseIntegerSequenceArray ( String seq, String delim, int parseFlag )
{
    if ( seq == null ) {
        return new int[0];
    }
    List<String> tokens = breakStringList ( seq, delim, parseFlag );
    int size = 0;
    if ( tokens != null ) {
        size = tokens.size();
    }
    if ( size == 0 ) {
        return new int[0];
    }
    else {
        int iseq[] = new int[size];
        for ( int i = 0; i < size; i++ ) {
            iseq[i] = Integer.parseInt(tokens.get(i).trim() );
        }
        return iseq;
    }
}

/**
Parse a string like "1:3" or "1:" or "1:-2" into an array containing the positional numbers, where the
parts are start, stop, and optionally step.
Single values result in a range where the start and end value are the same.
Note that this does NOT impose zero-offset indexing (like Python) - the range is 1+ to nVals, which
is more conducive to applications with users who are not programmers.
@param seq string to parse
@param delim delimiter character(s)
@param parseFlag see breakStringList() flag
@param count the number of values in the list corresponding to the slice, needed if the slice uses
ending notation like -2.
@return an array of integers parsed from the string.
*/
public static int [] parseIntegerSlice ( String seq, String delim, int parseFlag, int count )
{
    if ( seq == null ) {
        return new int[0];
    }
    List<String> tokens = breakStringList ( seq, delim, parseFlag );
    if ( seq.endsWith(delim) ) {
        // breakStringList won't return a token at the end
        tokens.add("");
    }
    int size = 0;
    if ( tokens != null ) {
        size = tokens.size();
    }
    if ( size == 0 ) {
        return new int[0];
    }
    else if ( size == 1 ) {
        // Single value
        int [] vals = new int[1];
        vals[0] = Integer.parseInt(tokens.get(0));
        return vals;
    }
    else {
        // Start value...
        int start = 1; // Default
        String token = tokens.get(0);
        if ( !token.equals("") ) {
            start = Integer.parseInt(token);
        }
        // End value...
        int end = count; // Default
        token = tokens.get(1);
        if ( token.equals("") ) {
            // End has not been specified so loop to the count
            end = count;
        }
        else {
            // End has been specified...
            end = Integer.parseInt(token);
            if ( end < 0 ) {
                // Negative number so relative to the count
                end = end + count;
            }
        }
        // Determine the step
        int step = 1;
        if ( size == 3 ) {
            // Have a step
            step = Integer.parseInt(tokens.get(2));
        }
        // Determine the number of values...
        // FIXME SAM 2010-12-17 Need more checks on invalid integers to avoid negative nVals
        //Message.printStatus(2,"", "Seq=\""+seq+"\" delim=\""+delim+"\" flag=" + parseFlag + " count="+count +
        //      " start=" + start + " end=" + end + " step=" + step );
        int nVals = (end - start)/step + 1;
        // Now iterate and generate the sequence
        int[] vals = new int[nVals];
        int i = 0;
        for ( int ival = start; ival <= end; ival += step ) {
            vals[i++] = ival;
        }
        return vals;
    }
}

/**
Count the number of unique (non-overlapping) instances of a pattern in a string.
@param s String to search.
@param pattern Pattern to search for.  Currently this can only be a one-character string.
@return The count of the unique instances.
*/
public static int patternCount ( String s, String pattern )
{	int count = 0;
	if ( (s == null) || (pattern == null) || (pattern.length() < 1) ) {
		return count;
	}
	int size = s.length ();
	char c = pattern.charAt(0);
	for ( int i = 0; i < size; i++ ) {
		if ( s.charAt(i) == c ) {
			++count;
		}
	}
	return count;
}

/**
Return "s" if the count is > 1 or an empty string if 1.  This is used to form strings that may or may not refer
to a plural.
@param count the number of objects being evaluted to determine if plural.
@return "s" if count is > 1, and "" otherwise.
*/
public static String pluralS ( int count )
{
    if ( count > 1 ) {
        return "s";
    }
    else {
        return "";
    }
}

/**
@return String up to but not including the delimiter character.
@param string0 String to read from.
@param delim Delimiter character to read to.
*/
public static String readToDelim ( String string0, char delim )
{	int i = 0;
	char c;
	StringBuilder string = new StringBuilder ();

	if ( string0 == null ) {
		return string.toString();
	}
	do {
	    c = string0.charAt(i);
		if ( c == delim ) {
			return string.toString();
		}
		else {
		    string.append ( c );
		}
		i++;
	} while ( c != '\0' );
	return string.toString();
}

/**
Remove a character from a string.
@return String that has the character removed.
@param s String to remove character from.
@param r String to remove.
*/
public static String remove ( String s, String r )
{	if ( (s == null) || (r == null) ) {
		return s;
	}
	StringBuilder buffer = new StringBuilder();
	int size = s.length();
	int r_length = r.length();
	for ( int i = 0; i < size; i++ ) {
		if ( s.indexOf(r,i) == i ) {
			// Skip next few characters...
			i += (r_length - 1);
		}
		else {
		    buffer.append ( s.charAt(i) );
		}
	}
	return buffer.toString();	
}

/**
Remove matching strings from a list.
@param strings list of strings to process
@param regex Java regular expression as per String.match() - if null then null strings will be matched
@param ignoreCase if true then the strings and regular expression will be compared as uppercase
*/
public static void removeMatching ( List<String> strings, String regex, boolean ignoreCase )
{
    if ( strings == null ) {
        return;
    }
    String s;
    String regexUpper = regex.toUpperCase();
    boolean matches = false;
    for ( int i = 0; i < strings.size(); i++ ) {
        matches = false;
        s = strings.get(i);
        if ( s == null ) {
            // Special case for removing nulls
            if ( regex == null ) {
                matches = true;
            }
        }
        else {
            if ( ignoreCase ) {
                matches = s.toUpperCase().matches(regexUpper);
            }
            else {
                matches = s.matches(regex);
            }
            if ( matches ) {
                strings.remove(i);
                --i;
            }
        }
    }
}

/**
Remove matching strings from a list.
@return the number of strings removed
@param strings main list of strings to process
@param strings2 list of strings to remove from main list
@param ignoreCase if true then the strings will be compared as uppercase
*/
public static int removeMatching ( List<String> strings, List<String> strings2, boolean ignoreCase )
{
    if ( (strings == null) || (strings2 == null) ) {
        return 0;
    }
    int size = strings.size();
    String s;
    boolean match;
    int matchCount = 0;
    // Iterate backwards so removing does not cause an issue
    for ( int i = (size - 1); i >= 0 ; i-- ) {
        s = strings.get(i);
        if ( s == null ) {
            continue;
        }
        match = false;
        for ( String s2 : strings2 ) {
            if ( s2 == null ) {
                continue;
            }
            if ( ignoreCase ) {
                if ( s.equalsIgnoreCase(s2) ) {
                    match = true;
                    break;
                }
            }
            else {
                if ( s.equals(s2) ) {
                    match = true;
                    break;
                }
            }
        }
        if ( match ) {
            // Remove from list
            strings.remove(i);
            --i;
        }
    }
    return matchCount;
}

/**
Remove the duplicates from a list of String.  The input list is modified so
make a copy before calling this method if necessary.
@param strings list of String to evaluate.
@param ignore_case If true, case is ignored in making string comparisons.
@param sorted If true, the input list is assumed to be sorted - this
increases processing speed.  TRUE IS CURRENTLY THE ONLY VALUE THAT IS SUPPORTED.
@return the list with duplicate values rememoved.
*/
public static List<String> removeDuplicates ( List<String> strings, boolean ignore_case, boolean sorted )
{	if ( sorted ) {
		// Loop through and compare each string with the previous string
		// in the list, removing the current string if a duplicate.
		int size = 0;
		if ( strings != null ) {
			size = strings.size();
		}
		String string, string0 = null;
		if ( size > 0 ) {
			string0 = strings.get(0);
		}
		for ( int i = 1; i < size; i++ ) {
			string = strings.get(i);
			if ( ignore_case ) {
				if ( string.equalsIgnoreCase(string0) ) {
					strings.remove(i);
					--i;
					--size;
				}
			}
			else if ( string.equals(string0) ) {
				strings.remove(i);
				--i;
				--size;
			}
			string0 = string;
		}
	}
	return strings;
}

/**
Remove the newline character(s) from a string.
The newline pattern for UNIX or PC machines is recognized, as appropriate.
@return String that has the newline character removed.
@param string String to remove newline read from.
*/
public static String removeNewline ( String string )
{	char c, c2 = '\n';
	int	k;

	if ( string == null ) {
		return string;
	}
	int string_length = string.length ();
	if ( string_length == 0 ) {
		return string;
	}
	for ( int i = 0; i < string_length; i++ ) {
		c = string.charAt ( i );
		if ( (c == '\n') || (c == '\r') ) {
			// Regardless of platform a newline is always initiated by a \n character
			// See if the character after newline(s) is the end of the string...
			for ( k = (i + 1); k < string_length; k++ ) {
				c2 = string.charAt ( k );
				if ( (c2 != '\n') && (c2 != '\r') ) {
					break;
				}
			}
			if ( (c2 == '\n') || (c2 == '\r') ) {
				// Nothing after the newline(s).  Return the string up to that point.  substring will
				// return up to i - 1!
				String newstring = string.substring(0, i);
				return newstring;
			}
			else {
				// Something after the newline(s)...
			 	//*pt = ' ';
				Message.printWarning ( 3, "StringUtil.removeNewline", "embedded newlines not handled yet" );
				// FIXME SAM 2009-01-19 Need to use a StringBuilder or something to better handle
				// embedded newlines.
				/*
				try {
					throw new Exception(
						"embedded newlines not"
						+ "handled yet");
				}
				catch (Exception e) {
					Message.printWarning(2, "", e);
				}
				*/
			}
		}
	}
	// If we get to here there were no newlines so just return...
	return string;
}

/**
The newline pattern for UNIX or PC machines is recognized, as appropriate.
@return String that has the newline character removed.
@param string String to remove newline read from.
*/
public static String removeNewline ( StringBuffer string )
{
	return removeNewline ( string.toString() );
}

/**
Reorder the string list given the order array (for example created by the sortStringList() method).
@param strings the string list to sort
@param sortOrder an array indicating the order that the strings should be in (e.g., if sortOrder[0] = 15, then
string [15] should be used in position 0.
@param createNewList if true, create and return a new list; if false, reorder the provided list in place
*/
public static List<String> reorderStringList ( List<String> strings, int [] sortOrder, boolean createNewList )
{
    ArrayList<String> strings2 = new ArrayList<String>(strings.size());
    for ( int i = 0; i < strings.size(); i++ ) {
        strings2.add(strings.get(sortOrder[i]));
    }
    if ( createNewList ) {
        return strings2;
    }
    else {
        for ( int i = 0; i < strings.size(); i++ ) {
            strings.set(i, strings2.get(i));
        }
        return strings;
    }
}

/**
Replaces every instance of a given substring in one string with
another substring.  The replacement is not recursive.  This method can be used
instead of the newer String.replace*() methods when using older versions of Java.
@param strOrig the String in which the string replacement should occur.
@param s1 the String to be replaced.
@param s2 the String to replace s1.
@return str with every instance of s1 replaced with s2.
*/
public static String replaceString ( String strOrig, String s1, String s2 ) {
	if (strOrig == null) {
		return strOrig;
	}
	if (strOrig.length() == 0) {
		return strOrig;
	}	

	String str = new String(strOrig);
	int i = str.indexOf(s1);
	int s1_len = s1.length();	// length of string to replace
	int s2_len = s2.length();	// Length of string replace with
	int len = str.length();
	String before = null;
	String after = null;
	int start = 0;
	while ( i >= 0 ) {
		// If in here, then we need to do a replacement...
		// String before the match...
		before = str.substring(0, i);
		if ( i == (len - 1) ) {
			// At the end of the string...
			str = before + s2;
			i = -1;
		}
		else {	
			after = str.substring(i + s1_len);	
			str = before + s2 + after;
			start = before.length() + s2_len;
			i = str.indexOf(s1, start);
		}
	}
	return str;
}

/**
Given a string representation of a floating point number, round to the
desired precision.  Currently this operates on a string (and not a double)
because the method is called from the formatString() method that operates on strings.
@return String representation of the rounded floating point number.
@param string String containing a floating point number.
@param precision Number of digits after the decimal point to round the number.
*/
public static String round ( String string, int precision )
{	String new_string;

	// First break the string into its integer and remainder parts...
	int dot_pos = string.indexOf ( '.' );
	if ( dot_pos < 0 ) {
		// No decimal.
		return string;
	}
	// If we get to here there is a decimal.  Figure out the size of the integer and the remainder...
	int integer_length = dot_pos;
	int remainder_length = string.length() - integer_length - 1;
	if ( remainder_length == precision ) {
		// Then our precision matches the remainder length and we can return the original string...
		return string;
	}
	else if ( remainder_length < precision ) {
		// If the remainder length is less than the precision, then we
		// can just add zeros on the end of the original string until we get to the precision length...
	}
	// If we get to here we need to do the more complicated roundoff 
	// stuff.  First check if the precision is zero.  If so, round off the main number and return...
	if ( precision == 0 ) {
		long ltemp = Math.round ( new Double(string).doubleValue() );
		return ( new Long(ltemp).toString() );
	}
	// If we get to here, we have more than a zero precision and need to
	// jump through some hoops.  First, create a new string that has the remainder...
	StringBuilder remainder_string = new StringBuilder ( string.substring(dot_pos + 1) );
	// Next insert a decimal point after the precision digits.
	remainder_string.insert(precision,'.');
	// Now convert the string to a Double...
	Double dtemp = new Double ( remainder_string.toString() );
	// Now round...
	long ltemp = Math.round ( dtemp.doubleValue() );
	// Now convert back to a string...
	String rounded_remainder = new String ( new Long(ltemp).toString() );
 	String integer_string = string.substring(0,integer_length);
	if ( rounded_remainder.length() < precision ) {
		// The number we were working with had leading zeros and we
		// lost that during the round.  Insert zeros again...
		StringBuilder buf = new StringBuilder ( rounded_remainder );
		int number_to_add = precision - rounded_remainder.length();
		for ( int i = 0; i < number_to_add; i++ ) {
			buf.insert(0,'0');
		}
		new_string = integer_string + "." + buf.toString();
		return new_string;
	}
	else if ( rounded_remainder.length() > precision ) {
		// We have, during rounding, had to carry over into the next
		// larger ten's spot (for example, 99.6 has been rounded to
		// 100.  Therefore, we need to use all but the first digit of
		// the rounded remainder and we need to increment our original number (or decrement if negative!).
		char first_char = string.charAt(0);
		long new_long = new Long(integer_string).longValue();
		if ( first_char == '-' ) {
			// Negative...
			--new_long;
		}
		else {
			// Positive...
			++new_long;
		}
		new_string = new_long + "." + rounded_remainder.substring(1);
		return new_string;
	}
	// Now put together the string again...
	new_string = integer_string + "." + ltemp;

/*
	if ( Message.isDebugOn ) {
		Message.printDebug ( 20, routine, "Original: " + string +
		" new: " + new_string );
	}
*/
	return new_string;
}

// showControl - do a verbose output of a string and show control characters
//
// Notes:	(1)	This is mainly used for debugging Java bugs and
//			understanding the language better.
/**
This is mainly used for Java debugging and testing.
@return A list of strings, each of which is the expanded character for a character in the original string.
@param string String to print control characters for.
*/
public static List<String> showControl ( String string )
{	List<String> v = new ArrayList<String>();

	int length = string.length();
	char c;
	String control;
	for ( int i = 0; i < length; i++ ) {
		c = string.charAt(i);
		if ( Character.isISOControl(c) ) {
			// Control character...
			if ( c == '\r' ) {
				control = "CR";
			}
			else if ( c == '\n' ) {
				control = "NL";
			}
			else {
				control = "Ctrl-unknown(" + c + ")";
			}
			v.add ( "Letter [" + i + "]: " + control );
		}
		else if ( Character.isLetterOrDigit(c) ) {
			// Print it...
			v.add ( "Letter [" + i + "]: " + c );
		}
		else {
			// Don't handle...
			v.add ( "Letter [" + i + "]: unknown(" + c +")");
		}
	}
	return v;
}

/**
Sort a list of strings into ascending order, considering case.
@return The sorted list (a new list is returned).
@param list The original list of String.
*/
public static List<String> sortStringList ( List<String> list )
{	return sortStringList ( list, SORT_ASCENDING, null, false, false );
}

/**
Sort a list of strings.
@return The sorted list (a new list is always returned, even if empty).
@param list The original list of String.
@param order Order to sort (SORT_ASCENDING or SORT_DESCENDING).
@param sort_order Original locations of data after sort (array needs to be
allocated before calling routine).  For example, first sort String data and then
sort associated data by using new_other_data[i] = old_other_data[sort_order[i]];
Can be null if sflag is false.
@param sflag Indicates whether "sort_order" is to be filled.
@param ignore_case If true, then case is ignored when comparing the strings.
*/
public static List<String> sortStringList ( List<String> list, int order, int sort_order[], boolean sflag,
	boolean ignore_case )
{	int	i, ismallest;
	int[] itmp=null;
	String routine="StringUtil.sortStringList", smallest="";

	if ( (list == null) || (list.size() == 0) ){
		Message.printWarning ( 50, routine, "NULL string list" );
		// Always return a new list
		return new Vector<String>();
	}
	int size = list.size();

	List<String> list_tosort = list;
	if ( ignore_case ) {
		// Create a new list that is all upper case...
		list_tosort = new Vector<String>( size );
		String string = null;
		for ( int j = 0; j < size; j++ ) {
			string = list.get(j);
			if ( string == null ) {
				list_tosort.add ( string );
			}
			else {
				list_tosort.add ( string.toUpperCase() );
			}
		}
	}
	List<String> newlist = new Vector<String>(size);

	// Allocate memory for the temporary int array used to keep track of the sort order...

	itmp = new int [size];

	for ( i = 0; i < size; i++ ) {
		itmp[i] = 0; // indicates not in new list yet
	}

	// OK, now do the sort.  Just do a buble sort and go through the entire
	// list twice.  Note that "issmallest" is used even if finding the largest for descending sort.

	int count = 0;
	while ( true ) {
		ismallest = -1;
		for ( i = 0; i < size; i++ ) {
			if ( itmp[i] != 0 ) {
				// Already in the new list...
				continue;
			}
			// Save the "smallest" string (null is considered smallest).  If this is the first
			// string encountered this iteration, initialize with the first string...
			// TODO SAM 2013-09-15 How to handle nulls?
			if( (ismallest == -1) || 
				((order == SORT_ASCENDING) && (list_tosort.get(i).compareTo(smallest) < 0) ) ||
				((order == SORT_DESCENDING) && (list_tosort.get(i).compareTo(smallest) > 0)) ) {
				smallest = list_tosort.get(i);
				ismallest = i;
			}
		}
		if ( ismallest == -1 ) {
			// We have exhausted the search so break out...
			break;
		}
		// Put in the original item (which will have the original case)...
		newlist.add( list.get(ismallest) );
		if ( sflag ) {
			sort_order[count++] = ismallest;
		}

		itmp[ismallest] = 1;
	}
	return newlist;
}

/**
Checks to see if one String starts with another, ignoring case.
@param s the String to check if it begins with the other
@param pattern the String that is being checked if it is the start of the other string.
@return true if the second String is the starting String in the first.
*/
public static boolean startsWithIgnoreCase ( String s, String pattern )
{	if ( (s == null) || (pattern == null) ) {
		return false;
	}
	int plen = pattern.length();
	int slen = s.length();
	if ( (plen == 0) || (slen < plen) ) {
		return false;
	}
	String sub = s.substring(0, plen);
	return sub.regionMatches(true,0,pattern,0,plen);
}

/**
Checks to see if two strings (of which either or both may be null) are equal.
@param s1 the first String to check.
@param s2 the second String to check.
@return true if the Strings are equal (null == null), false if not.
*/
public static boolean stringsAreEqual(String s1, String s2) {
	if (s1 == null && s2 == null) {
		return true;
	}
	if (s1 == null || s2 == null) {
		return false;
	}
	if (s1.trim().equals(s2.trim())) {
		return true;
	}
	return false;
}

/**
Convert a list of strings to an array of strings.
@return An array containing the strings.
@param v list of strings to convert.
*/
public static String[] toArray ( List<String> v )
{	if ( v == null ) {
		return null;
	}
	int vector_size = v.size();
	String [] array = new String [vector_size];
	for ( int i = 0; i < vector_size; i++ ) {
		array[i] = v.get(i);
	}
	return array;
}

/**
Return the count of the tokens in a string or null if no token.  This method
calls breakStringList() and returns the resulting count.
@param string The string to break.
@param delim A String containing characters to treat as delimiters.
@param flag Bitmask indicating how to break the string.  Specify
DELIM_SKIP_BLANKS to skip blank fields and
DELIM_ALLOW_STRINGS to allow quoted strings (which may contain delimiters).
@return the first token or null.
*/
public static int tokenCount ( String string, String delim, int flag )
{	List<String> v = breakStringList ( string, delim, flag );
	if ( v == null ) {
		return 0;
	}
	int size = v.size();
	v = null;
	return size;
}

/**
Convert an array of strings to a List of strings.
@return A List containing the strings.
@param array Array of strings to convert.
*/
public static List<String> toList ( String [] array )
{
	if ( array == null ) {
		return null;
	}
	int array_size = array.length;
	List<String> v = new Vector<String>( array_size, 50 );
	for ( int i = 0; i < array_size; i++ ) {
		v.add ( array[i] );
	}
	return v;
}

/**
Convert an enumeration of strings to a list of strings.
@return A list containing the strings.
@param e Enumeration of strings to convert.
*/
public static List<String> toList ( Enumeration<String> e )
{
	if ( e == null ) {
		return null;
	}
	List<String> v = new Vector<String>( 50 );
	while ( e.hasMoreElements() ) {
		v.add ( e.nextElement() );
	}
	return v;
}

/**
Convert a list of strings into one long string that is delimited by the
given string (usually the system line separator).  Null strings are treated
as empty strings.  This is useful for converting lists to something that a TextArea can display.
@param delimiter delimiter to include between each string, or null to not use a delimiter.
@return the combined string, or null if the original list is null.
*/
public static String toString ( List<String> strings, String delimiter )
{
	if ( strings == null ) {
		return null;
	}
	StringBuilder buffer = new StringBuilder ();
	int size = strings.size();
	for ( int i = 0; i < size; i++ ) {
		if ( (i > 0) && (delimiter != null) ) {
			buffer.append(delimiter);
		}
		buffer.append ( strings.get(i) );
	}
	return buffer.toString();
}

/**
Remove characters from string.
@return A string that has been unpadded (whitespace removed from front, back and/or middle).
@param string String to unpad.
@param white0 Whitespace characters to remove.
@param flag Bitmask indicating how to unpad.  Can be
PAD_FRONT, PAD_BACK, PAD_MIDDLE, PAD_FRONT_BACK, or PAD_FRONT_MIDDLE_BACK.
*/
public static String unpad ( String string, String white0, int flag )
{	int length_string, length_white;
	String default_white = " \t\n\r", white;
	StringBuilder buffer;

	// Check for NULL prointers...

	if ( string == null ) {
		return string;
	}

	// Set default whitespace characters if not specified...

	if ( white0 == null ) {
		white = default_white;
		length_white = white.length ();
	}
	else {
		length_white = white0.length();
		if ( length_white == 0 ) {
			white = default_white;
			length_white = white.length ();
		}
		else {
			white = white0;
			length_white = white.length ();
		}
	}

	length_string = string.length ();
	if ( length_string == 0 ) {
		return string;
	}

	int istring;
	char cstring = '\0';

	// Unpad the whole string...

	if ( (flag == StringUtil.PAD_FRONT_MIDDLE_BACK) && (length_string > 0) ) {
		buffer = new StringBuilder ();
		for ( istring = 0; istring < length_string; istring++ ) {
			cstring = string.charAt ( istring );
			if ( white.indexOf(cstring) != -1 ) {
				// Don't transfer the character...
				continue;
			}
			buffer.append ( cstring );
		}
		return buffer.toString();
	}

	buffer = new StringBuilder ( string );

	// Do the back first so that we do not shift the string yet...

	if ( ((flag & StringUtil.PAD_BACK) != 0) && (length_string > 0) ) {
		// Remove whitespace from back...
		istring = length_string - 1;
		if ( istring >= 0 ) {
			cstring = string.charAt ( istring );
			while (	(istring >= 0) && (white.indexOf(cstring) != -1) ) {
				// Shorten by one character as we backtrack...
				--length_string;
				if ( length_string < 0 ) {
					length_string = 0;
				}
				buffer.setLength ( length_string );
				--istring;
				if ( istring >= 0 ) {
					cstring = string.charAt ( istring );
				}
			}
		}
		//Message.printDebug ( dl, routine, "Result after \"%s\" off back: \"%s\".", white, string );
	}

	// Now do the front...

	int skip_count = 0;
	if ( ((flag & StringUtil.PAD_FRONT) != 0) && (length_string > 0) ) {
		// Remove whitespace from front...
		istring = 0;
		cstring = string.charAt ( istring );
		while ( (istring < length_string) && (white.indexOf(cstring) != -1) ) {
			// Skipping leading whitespace...
			++skip_count;
			++istring;
			if ( istring < length_string ) {
				cstring = string.charAt ( istring );
			}
		}
		if ( skip_count > 0 ) {
			// We need to shift the string...
			return ( buffer.toString().substring(skip_count) );
		}
		//buffer.append ( string.substring(istring) );
		//strcpy ( string, pt );
		//Message.printDebug ( dl, routine, "Result after \"%s\" off front: \"%s\".", white, string );
	}

	// Else, return the string from the front and back operations...
	return buffer.toString();
}

/**
This is essentially equivalent to String.trim().
@return A string that has had spaces removed from the front and back.
@param string The string to unpad.
*/
public static String unpad ( String string )
{	return( unpad( string, " ", PAD_FRONT_BACK ) );
}

/**
This is the same as the String version, but allows a StringBuffer as input.
*/
public static String unpad ( StringBuffer string, String white0, int flag )
{	return unpad ( string.toString(), white0, flag );
}

/**
Wraps text to fit on a line of a certain length.  Text is wrapped at newlines,
commas, periods, exclamation marks, question marks, open and close parentheses,
open and close braces, open and close curly brackets, colons, semicolons,
spaces, tabs, backslashes and forward slashes.
@param s the text to wrap to fit on a certain-length line.
@param lineLength the maximum length of a line of text.  Must be at least 2.
@return a wrapped that will fit on lines of the given length.
*/
public static String wrap(String s, int lineLength) {
	List<String> v = StringUtil.breakStringList(s, "\n", 0);
	StringBuilder sb = new StringBuilder("");
	
	for (int i = 0; i < v.size(); i++) {
		sb.append(wrapHelper(v.get(i), lineLength));
	}
	return sb.toString();
}

/**
Wraps text to fit on a line of a certain length.  Text is wrapped at newlines,
commas, periods, exclamation marks, question marks, open and close parentheses,
open and close braces, open and close curly brackets, colons, semicolons,
spaces, tabs, backslashes and forward slashes.
@param s the text to wrap to fit on a certain-length line.
@param lineLength the maximum length of a line of text.  Must be at least 2.
@return a wrapped that will fit on lines of the given length.
*/
public static String wrapHelper(String s, int lineLength) {
	if (lineLength < 2) {
		return "";
	}

	// the most-recently-located index of a point in the text at which a wrap can occur
	int next = -1;
	// the previously-located index of a point in the text at which a wrap could occur
	int last = -1;
	String trim = null;
	StringBuilder sb = new StringBuilder();

	// first check for the trivial case -- a String that's shorter than the maximum allowed line length
	if (s.length() <= lineLength) {
		sb.append(s + "\n");
		return sb.toString();
	}
	
	while (true) {
		last = next;

		// find the next point from which a line wrap can possibly occur.
		next = wrapFindFirstWrappableIndex(s, next + 1);

		// if the next point for a valid wordwrap is beyond the maximum allowable line length ...
		if (next > lineLength) {
			// ... and no previous word wrap point was found ...
			if (last == -1) {
				// ... split the text up the length of the line.
				// Put in a hyphen, and then carry over the 
				// rest of the text after the hyphen to
				// be checked for wordwraps in the next
				// iteration.
				trim = s.substring(0, lineLength - 1);
				trim += "-";
				sb.append(trim + "\n");
				s = s.substring(lineLength - 1);
				s = s.trim();
				last = -1;
				next = -1;
			}
			// ... and a previous wrap point was found.
			else {	
				// ... split the text at the point of the 
				// previous word wrap point and then let the 
				// rest of the string be carried over to be 
				// checked in the next iteration.
				trim = s.substring(0, last);
				sb.append(trim + "\n");
				s = s.substring(last);
				s = s.trim();
				last = -1;
				next = -1;
			}
		}
		// if the next wrap point if exactly on the barrier between
		// maximum line length and invalid line length ...
		else if (next == lineLength) {
			// ... a perfect fit was found, so take all the text 
			// up the next wrap point and put it on one line,
			// then carry over the rest of the text to be checked
			// in a later iteration.
			trim = s.substring(0, next);
			sb.append(trim + "\n");
			s = s.substring(next);
			s = s.trim();
			last = -1;
			next = -1;
		}	
		// if no valid wrap points can be found in the rest of the
		// text, but a valid wrap point was found just prior ...
		else if (next == -1 && last > -1) {
			// ... means this is possibly the last line.  See
			// if what is left will fit on one line ...
			if (s.length() <= lineLength) {
				sb.append(s + "\n");
				return sb.toString();
			}			
				
			// ... but if not, take the text up to the last 
			// wrap point and put it on a line and then take 
			// the rest of the text and prepare it to be handled 
			// in the next iteration.
			
			trim = s.substring(0, last);
			sb.append(trim + "\n");
			s = s.substring(last);
			s = s.trim();
			last = -1;
			next = -1;
		}
		// if no valid wrap points can be found through the end
		// of the text, and none were found previously ...
		else if (next == -1 && last == -1) {
			// ... then the end of the text is getting close.

			// if the text is still longer than the maximum 
			// allowable line length ...
			if (s.length() > lineLength) {
				// ... then loop through and separate it
				// into hyphenatable chunks and put each 
				// chunk on a line.  
				while (true) {
					trim = s.substring(0, lineLength - 1);
					trim += "-";
					sb.append(trim + "\n");
					s = s.substring(lineLength - 1);
					s = s.trim();
					if (s.length() <= lineLength) {
						sb.append(s + "\n");
						return sb.toString();
					}
				}
			}
			// if the remaining text can all fit on one line,
			// put it on one line and return it.
			else {
				sb.append(s + "\n");
				return sb.toString();
			}		
		}
	}
}

/**
A helper function used by wrap() to locate a the point at which a line of text can be wrapped.
@param s the text to check.
@param from the point from which to check the text.
@return the location of the next immediate wrap point, or -1 if none can be found.
*/
private static int wrapFindFirstWrappableIndex(String s, int from) {
	// There are two batches of characters to be checked and each batch must be handled differently.  

	// In the first case are characters that denote that the line can be wrapped immediately AFTERWARDS
	int index1 = wrapFindFirstWrappableIndexHelper(s, ".", from, -1);
	index1 = wrapFindFirstWrappableIndexHelper(s, ",", from, index1);
	index1 = wrapFindFirstWrappableIndexHelper(s, "!", from, index1);
	index1 = wrapFindFirstWrappableIndexHelper(s, "?", from, index1);
	index1 = wrapFindFirstWrappableIndexHelper(s, ";", from, index1);
	index1 = wrapFindFirstWrappableIndexHelper(s, ":", from, index1);
	index1 = wrapFindFirstWrappableIndexHelper(s, ")", from, index1);
	index1 = wrapFindFirstWrappableIndexHelper(s, "}", from, index1);
	index1 = wrapFindFirstWrappableIndexHelper(s, "]", from, index1);
	index1 = wrapFindFirstWrappableIndexHelper(s, "/", from, index1);
	index1 = wrapFindFirstWrappableIndexHelper(s, "\\", from, index1);

	// In the second case are characters that denote that the line must be wrapped BEFORE.
	int index2 = wrapFindFirstWrappableIndexHelper(s, "(", from, -1);
	index2 = wrapFindFirstWrappableIndexHelper(s, "{", from, index2);
	index2 = wrapFindFirstWrappableIndexHelper(s, "[", from, index2);
	index2 = wrapFindFirstWrappableIndexHelper(s, " ", from, index2);
	index2 = wrapFindFirstWrappableIndexHelper(s, "\t", from, index2);

	/*
	So given a line like this:
		The solution (X) is easy!  Okay.
	
	If the line needs to be wrapped at the open parentheses, the wrap
	is performed before, so that the wrap would look like:
		The solution
		(X) is easy!  Okay.
	and not:
		The solution (
		X) is easy!  Okay.
	
	Likewise, if the wrap must be performed at the exclamation point,
	the wrap should look like:
		The solution (X) is easy!
		Okay.
	and not:
		The solution (X) is easy
		!  Okay.
	*/

	int index = -1;

	// if no valid wrap point could be found from the second batch ...
	if (index2 == -1) {
		// .. but the first batch had a valid point ...
		if (index1 > -1) {
			// ... increment the wrap position, in accord with
			// how the first batch wrap positions are set
			index = index1 + 1;
		}
		else {
			// (redundant, just to show what will actually be
			// returned from the method below)
			index = -1;
		}
	}
	// ... if a valid wrap point was found in the second batch ...
	else {
		// ... and no valid wrap point was found in the first ...
		if (index1 == -1) {
			// .. the wrap point is the second batch's point.
			index = index2;
		}
		// ... AND the first batch had a valid wrap point ...
		else {
			// ... choose whichever is lesser.  The two values
			// will NEVER be the same.

			if (index2 < index1) { 
				index = index2;
			}
			else {
				index = index1 + 1;
			}
		}
	}
		
	return index;
}

/**
Helper method for wrapFindFirstWrappableIndex to locate the first index of
a character in a line from a specified point and compare it to a 
previously-determined index of another character in the line.
@param s the text to check.
@param ch the character to find the index of.
@param from the point from which to search for the index.
@param index the location found for a wrappable point in the same line of text
from the same from point, but for a different character.
@return the earliest wrap point based on the wrap point that is found and the
previously-found wrap point (index), or -1 if none can be found.
*/
private static int wrapFindFirstWrappableIndexHelper(String s, String ch, int from, int index) {
	// Find the first position of the specified character from the specified start point.
	int i = s.indexOf(ch, from);
	
	// If no position was found, and no position had been found previously ...
	if (i == -1 && index == -1) {
		return -1;
	}
	
	// If a position was found this time, but none had been found previously ...
	if (i > -1 && index == -1) {
		return i;
	}
	// If no position was found this time, but one had been found previously ...
	else if (i == -1 && index > -1) {	
		return index;
	}
	// If a position was found this time AND one had been found previously ...
	else {
		// return whichever is smaller.
		if (index < i) {
			return index;
		}
		else {
			return i;
		}
	}
}

}
