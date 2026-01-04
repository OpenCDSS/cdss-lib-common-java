// EmbeddedPropertiesString - class to parse a string with embedded properties

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 2023 Colorado Department of Natural Resources

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import RTi.Util.Message.Message;

/**
 * This class parses a string containing embedded Property=Value properties.
 * A map of properties and the string without the properties is output.
 */
public class EmbeddedPropertiesString {

	/**
	 * The delimiter used with DOUBLE_SLASH format.
	 */
	private static final String DOUBLE_SLASH_DELIMITER = "//";
	
	/**
	 * Format for embedded properties (currently only DOUBLE_SLASH is supported.
	 */
	private EmbeddedPropertyFormatType embeddedPropertyFormatType = EmbeddedPropertyFormatType.DOUBLE_SLASH;

	/**
	 * Original string, which may or may not include properties and embedded newlines for a multiple-line string.
	 */
	private String stringOrig = "";
	
	/**
	 * Number of line in the string, based on parts after splitting using the newline character.
	 */
	private int lineCount = -1;

	/**
	 * String without properties.
	 */
	private String stringWithoutProperties = "";
	
	/**
	 * String that delimits properties from the main part of the string, for example "//".
	 * If a delimiter is specified, then the string can be evaluated to check that properties are ONLY after the delimiter.
	 */
	private String propertyDelimiter = null;

	/**
	 * Map of properties parsed from the string after the delimiter, guaranteed to be non-null but may be empty.
	 * No properties may be present, in which case the map is empty.
	 * Use a LinkedHashMap so that the original order of the properties is retained.
	 */
	private HashMap<String,String> propertiesAfterDelimiterMap = new LinkedHashMap<>();

	/**
	 * Map of properties parsed from the string before the delimiter, guaranteed to be non-null but may be empty.
	 * No properties may be present, in which case the map is empty.
	 * Use a LinkedHashMap so that the original order of the properties is retained.
	 */
	private HashMap<String,String> propertiesBeforeDelimiterMap = new LinkedHashMap<>();

	/**
	 * Constructor.  Properties can occur anywhere except inside quoted strings.
	 * The format type defaults to DOUBLE_SLASH.
	 * @param s string that may or may not contain Property=Value strings.
	 */
	public EmbeddedPropertiesString ( String s ) {
		// Call the overloaded version.
		this ( EmbeddedPropertyFormatType.DOUBLE_SLASH, s );
	}

	/**
	 * Constructor.
	 * @param embeddedPropertyFormatType the format type for embedded properties
	 * @param s string that may or may not contain Property=Value strings.
	 */
	public EmbeddedPropertiesString ( EmbeddedPropertyFormatType embeddedPropertyFormatType, String s ) {
		// Initialize the parsed data.
		this.embeddedPropertyFormatType = embeddedPropertyFormatType;
		this.stringOrig = s;
		this.propertiesBeforeDelimiterMap = new LinkedHashMap<>();
		this.propertiesAfterDelimiterMap = new LinkedHashMap<>();
		this.stringWithoutProperties = "";

		if ( embeddedPropertyFormatType == EmbeddedPropertyFormatType.DOUBLE_SLASH ) {
			this.propertyDelimiter = DOUBLE_SLASH_DELIMITER;
		}
		else if ( embeddedPropertyFormatType == EmbeddedPropertyFormatType.PROPERTY_VALUE ) {
			this.propertyDelimiter = null;
		}
		else {
			throw new RuntimeException ( "Unsupported format type: " + embeddedPropertyFormatType );
		}
		
		// Parse the original string into properties.
		parse();
	}
	
	/**
	 * Check the string and generate problems if any of the following exist:
	 * <ul>
	 * <li> Property=Value syntax exists but is not after the delimiter.
	 * </ul>
	 * @param problems non-null list to add problems (if null a new list will be created and returned)
	 * @return the list of problems, may be empty and is guarantee to be non-null
	 */
	public List<String> check ( List<String> problems ) {
		if ( problems == null ) {
			problems = new ArrayList<>();
		}
		/*
		if ( propertiesBeforeDelimiterMap.size() > 0 ) {
			problems.add(DEFAULT_DELIMITER)
		}
		*/
		return problems;
	}

	/**
	 * Return the embe3ded property format type.
	 * @return the embedded property format type
	 */
	public EmbeddedPropertyFormatType getEmbeddedPropertyFormatType () {
		return this.embeddedPropertyFormatType;
	}

	/**
	 * Return the line count from the original string (number of parts after splitting with newline character).
	 * @return the line count
	 */
	public int getLineCount () {
		return this.lineCount;
	}

	/**
	 * Return the original string.
	 * @return the original string
	 */
	public String getStringOrig () {
		return this.stringOrig;
	}
	
	/**
	 * Return the property value, or null if not defined.
	 * Only properties defined after the delimiter are returned,
	 * which is the case for properly-formatted embedded properties.
	 * @param propertyName the name of the property to look up
	 * @return the property value, or null if not defined.
	 */
	public String getPropertyValue ( String propertyName ) {
		// Only check the properties after the delimiter.
		boolean includeBefore = false;
		boolean includeAfter = true;
		return getPropertyValue ( propertyName, includeBefore, includeAfter );
	}

	/**
	 * Return the property value, or null if not defined.
	 * Properties after the delimiter are checked first and if not found, properties before the delimiter are checked,
	 * but the checks are controlled by the method parameters.
	 * @param propertyName the name of the property to look up
	 * @param includePropertiesBeforeDelimiter if then check properties before the delimiter
	 * @param includePropertiesAftereDelimiter if then check properties after the delimiter
	 * @return the property value, or null if not defined.
	 */
	public String getPropertyValue ( String propertyName, boolean includePropertiesBeforeDelimiter, boolean includePropertiesAfterDelimiter ) {
		String propertyValue = null;
		if ( includePropertiesAfterDelimiter ) {
			propertyValue = this.propertiesAfterDelimiterMap.get(propertyName);
		}
		if ( (propertyValue == null) && includePropertiesBeforeDelimiter ) {
			propertyValue = this.propertiesBeforeDelimiterMap.get(propertyName);
		}
		return propertyValue;
	}

	/**
	 * Return the properties map for properties after the delimiter.
	 * @return the properties map, guaranteed to not be null.
	 */
	public HashMap<String,String> getPropertiesAfterDelimiterMap() {
		return this.propertiesAfterDelimiterMap;
	}

	/**
	 * Return the properties map for properties before the delimiter.
	 * @return the properties map, guaranteed to not be null.
	 */
	public HashMap<String,String> getPropertiesBeforeDelimiterMap() {
		return this.propertiesBeforeDelimiterMap;
	}

	/**
	 * Return the string after properties have been removed.
	 * @return the string after properties have been removed
	 * @deprecated use getStringWithoutProperties()
	 */
	public String getStringNoProperties () {
		return getStringWithoutProperties();
	}

	/**
	 * Return the string after properties have been removed.
	 * NOT CURRENTLY IMPLEMENTED.
	 * @return the string after properties have been removed
	 */
	public String getStringWithoutProperties () {
		return this.stringWithoutProperties;
	}

	/**
	 * Parse the original string.
	 * Properties must use the syntax Property=Value with no spaces around the equal sign or in the property name or value.
	 * Properties are assumed to be at the end of the description using the following syntax:
	 *
	 * <pre>
	 * If no delimiter string:
	 * 
	 *   Some description. Property1=Value1
	 *   Some description. Property1=Value1 Property2=Value2
	 *   Some description. Property1=Value1 Property2="Value2 with spaces and double quotes"
	 *   Some description. Property1=Value1 Property2='Value2 with spaces and single quotes'
	 *   Some multi-line description.\n
	 *       Property1=Value1\n
	 *       Property2=Value2
	 *   
	 * If delimiter string:
	 * 
	 *   Some description. // Property1=Value1 Property2=Value2
	 *   Some description. // Property1=Value1 Property2="Value2 with spaces and double quotes"
	 *   Some description. // Property1=Value1 Property2='Value2 with spaces and single quotes'
	 *   Some description. //
	 *      Property1=Value1
	 *      Property2=Value2
	 * </pre>
	 *
	 * Remove properties with syntax Property=Value (set in 'this.stringNoProperties')
	 * and keep the original text without the properties (in 'this.stringOrig').
	 * If property values contain spaces, enclose in double quotes.
	 */
	private void parse () {
		String routine = this.getClass().getSimpleName() + ".parse";

		// Set the following to true for development.
		boolean debug = false;
		
		if ( debug ) {
			Message.printStatus(2, routine, "Parsing: \"" + this.stringOrig + "\"");
		}

		if ( (this.stringOrig == null) || (this.stringOrig.trim().isEmpty()) ) {
			// Nothing to parse.
			return;
		}

		// Parse the original string and remove the properties.
		// Set the descriptionText to full description by default, but may be reset below.
		this.stringWithoutProperties = this.stringOrig;

		// Determine if there are properties in the description using syntax:
		//
		//  Property1=Value1
		//  Property2=Value2
		//
		// or:
		//
		//  Property1=Value1 Property2=Value2 Property3=Value3
		//
		// Spaces are not allowed around equal sign.
		// Properties CAN occur on one line with rest of description, or be on separate lines.

		// First split the original string by newlines,
		// which will retain carriage returns in front of newlines if they are present (trim them below).
		// Use a StringBuilder to create the string without extra properties.
		String [] lines = this.stringOrig.split("\n");
		this.lineCount = lines.length;
		for ( int iline = 0; iline < lineCount; iline++ ) {
			// Single line in description to parse:
			// - remove carriage returns if still present
			String line = lines[iline].replace("\r","");
			// Line before the delimiter (whole line if no delimiter).
			String lineBeforeDelim = "";
			// Line after the delimiter.
			String lineAfterDelim = "";
			
			if ( debug ) {
				Message.printStatus(2, routine, "  Parsing line[" + iline + "]: \"" + line + "\"");
			}
			
			// Reposition the start of the string if 'propertyIndicator' is used.
			if ( (this.propertyDelimiter != null) && !this.propertyDelimiter.isEmpty() ) {
				// Position to start looking for the delimiter:
				// - for example the delimited is "//" for DOUBLE_SLASH format.
				int posStart = 0;
				while ( true ) {
					int pos2 = line.indexOf(this.propertyDelimiter, posStart);
					if ( pos2 < 0 ) {
						// No properties delimiter so nothing to process.
						if ( debug ) {
							Message.printStatus(2, routine, "    Did not find property delimiter.");
						}
						// Line before the delimiter is the entire line.
						lineBeforeDelim = line;
						lineAfterDelim = "";
						break;
					}
					else if ( (pos2 > 0) && (line.charAt(pos2 - 1) == ':') ) {
						// Probably is a URL like "https://...":
						// - don't consider it to be the properties delimiter
						// - advance and keep searching
						if ( debug ) {
							Message.printStatus(2, routine, "    Found delimiter but looks like a URI so skipping." );
						}
						posStart = pos2 + this.propertyDelimiter.length();
					}
					else {
						// The pos2 index matches the delimiter:
						// - can determine the strings before and after the delimiter to parse for properties
						int pos3 = pos2 + this.propertyDelimiter.length();
						if ( pos3 >= line.length() ) {
							// Property indicator is at the end of the line (nothing follows).
							lineAfterDelim = "";
							// Most of the line.
							lineBeforeDelim = line.substring(0,pos2);
							if ( debug ) {
								Message.printStatus(2, routine, "    Delimiter is at the end of the line." );
							}
						}
						else {
							// There are characters after the indicator to parse.
							lineAfterDelim = line.substring(pos3);
							// Line prior to the delimiter.
							lineBeforeDelim = line.substring(0,pos2);
							if ( debug ) {
								Message.printStatus(2, routine, "    Delimiter is NOT at the end of the line." );
							}
						}
						// Break out of searching for the delimiter.
						break;
					}
				}
			}
			else {
				// No delimiter so treat the entire line as "before delimiter".
				lineBeforeDelim = line;
				lineAfterDelim = "";
			}
			
			if ( (iline == 0) && (this.embeddedPropertyFormatType == EmbeddedPropertyFormatType.DOUBLE_SLASH) ) {
				// Set the string without the trailing properties.
				this.stringWithoutProperties = lineBeforeDelim.trim();
			}
			
			if ( debug ) {
				Message.printStatus(2, routine, "    lineBeforeDelim=\"" + lineBeforeDelim + "\"" );
				Message.printStatus(2, routine, "    lineAfterDelim=\"" + lineAfterDelim + "\"" );
			}

			// Parse the properties before the delimiter:
			// - the map must not be null and will be updated
			if ( ! lineBeforeDelim.isEmpty() ) {
				parseString ( debug, this.propertiesBeforeDelimiterMap, lineBeforeDelim );
			}
			
			// Parse the properties after the delimiter.
			// - the map must not be null and will be updated
			if ( ! lineAfterDelim.isEmpty() ) {
				parseString ( debug, this.propertiesAfterDelimiterMap, lineAfterDelim );
			}
		} // End looping over lines in a string.

		// Reset the description text to the description without properties:
		// - trim to remove any trailing newlines and whitespace
		//this.stringNoProperties = descriptionBuilder.toString().trim();
	}
	
	/**
	 * Parse a string to determine properties.
	 * The string should be parsed out of the source based on the EmbeddedStringFormatType (this.formatType).
	 * @param debug whether debug messages should be printed
	 * @param propertiesMap the map to be updated with found properties
	 * @param string the string to parse, which will either be a line before the delimiter (full line if no delimiter)
	 * or the string after the delimiter
	 */
	private void parseString ( boolean debug, Map<String,String> propertiesMap, String string ) {
		String routine = this.getClass().getSimpleName() + ".parseString";

		if ( debug ) {
			Message.printStatus(2, routine, "Parsing  \"" + string + "\".");
		}

		// Loop through the line character by character:
		// - this assumes that quoted strings are well-behaved (matching)
		// - could use a regular expression or tokenizer, but can be complex and confusing
		
		boolean inQuotedString = false;
		char quoteChar = '\0';
		boolean inWord = false;
		boolean checkProperty = false;
		StringBuilder word = new StringBuilder(); // To store a word delimited by spaces or = (could be the property name or value).
		String propertyName = null; // Property name.
		StringBuilder quotedString = new StringBuilder(); // Quoted string (could be a property value if after equal sign).
		char c;
		// Count of properties that are set.
		int propertyCount = 0;
		
		// Don't use "continue" in the following loop so that debug messages will be complete.
		for ( int ichar = 0; ichar < string.length(); ichar++ ) {
			c = string.charAt(ichar);
			if ( debug ) {
				Message.printStatus(2, routine, "Processing character: '" + c + "'");
			}
			// Whether to check setting a property:
			// - if set to true below, the character could indicate the end of a property value
			checkProperty = false;
			if ( c == '"' ) {
				// Handle quoted string using double quotes.
				if ( inQuotedString && (quoteChar == c) ) {
					// End of quote.
					inQuotedString = false;
					// Indicate to check setting a property below.
					checkProperty = true;
					if ( debug ) {
						Message.printStatus(2, routine, "End of double-quote string.");
					}
				}
				else {
					// Start of quote.
					inQuotedString = true;
					quoteChar = c;
					quotedString.setLength(0);
					if ( debug ) {
						Message.printStatus(2, routine, "Start of double-quote string.");
					}
				}
			}
			else if ( c == '\'' ) {
				// Handle quoted string using single quotes.
				if ( inQuotedString && (quoteChar == c) ) {
					// End of quote.
					inQuotedString = false;
					// Indicate to check setting a property below.
					checkProperty = true;
					if ( debug ) {
						Message.printStatus(2, routine, "End of single-quote string.");
					}
				}
				else {
					// Start of quote.
					inQuotedString = true;
					quoteChar = c;
					quotedString.setLength(0);
					if ( debug ) {
						Message.printStatus(2, routine, "Start of single-quote string.");
					}
				}
			}
			else if ( (c == ' ') || (c == '\t') || (c == ',') ) {
				// Typically between property definitions or within a quoted string.
				if ( inQuotedString ) {
					// Add the character to the quoted string.
					quotedString.append(c);
				}
				else if ( inWord ) {
					// End of a word.
					inWord = false;
					// Indicate to check setting a property below.
					checkProperty = true;
				}
				else {
					// Not in a word or quoted string. Just ignore the space until another word or quoted string is encountered.
				}
			}
			else if ( c == '=' ) {
				if ( inQuotedString ) {
					// Add the character to the quoted string.
					quotedString.append(c);
				}
				else if ( word.length() > 0 ) {
					// The previous word before the equal is the property name.
					propertyName = word.toString();
					word.setLength(0);
					inWord = false;
					if ( debug ) {
						Message.printStatus(2, routine, "Found =, using word for property name: \"" + propertyName + "\".");
					}
				}
			}
			else {
				// Every other character.
				if ( inQuotedString ) {
					// Add the character to the quoted string.
					quotedString.append(c);
				}
				else if ( inWord ) {
					// Add the character to the word.
					word.append(c);
					if ( ichar == (string.length() - 1) ) {
						// End of the string so need to check the property.
						checkProperty = true;
					}
				}
				else {
					// Not in a word so start a new word.
					inWord = true;
					word.setLength(0);
					word.append(c);
					if ( ichar == (string.length() - 1) ) {
						// End of the string so need to check the property.
						checkProperty = true;
					}
				}
			}
			
			// If here:
			// - a single character has been processed above
			// - quoted string or word may have been appended to
			// - if checkProperty was set to true, then the code below will set a property and reseet to look for another property
			
			if ( debug ) {
				Message.printStatus ( 2, routine, "checkProperty=" + checkProperty
					+ " propertyName=\"" + propertyName
					+ "\" inWord=" + inWord + " word=\"" + word
					+ "\" inQuotedString=" + inQuotedString + " quotedString=\"" + quotedString + "\"" );
			}

			if ( checkProperty ) {
				// Check whether a property can be set:
				// - will be the case if have a word, followed by an equal sign, followed by a word or quoted string
				if ( (propertyName != null) && !propertyName.isEmpty() ) {
					// Make sure that the property name is not the leading part of a URL or URL query parameter:
					// - may need to include other checks for invalid property names
					if ( propertyName.contains("//") || propertyName.contains("?") || propertyName.contains("&") ) {
						// Looks like part of a URL so skip:
						// - trailing query parts should also be detected because of ? or & in the name
						if ( debug ) {
							Message.printStatus ( 2, routine, "NOT setting property from URL fragment with name: " + propertyName );
						}
					}
					else if ( quotedString.length() > 0 ) {
						// Have a property value that is a quoted string.
						propertiesMap.put(propertyName, quotedString.toString());
						if ( debug ) {
							Message.printStatus ( 2, routine, "Set property from quoted string: " + propertyName + "=\"" + quotedString + "\"" );
						}
						++propertyCount;
					}
					else if ( word.length() > 0 ) {
						// Have a non-quoted property value that is a word.
						propertiesMap.put(propertyName, word.toString());
						if ( debug ) {
							Message.printStatus ( 2, routine, "Set property from word: " + propertyName + "=\"" + word + "\"" );
						}
						++propertyCount;
					}
					// Since a property was handled, reset to start looking for another property on the same line.
					inWord = false;
					word.setLength(0);
					inQuotedString = false;
					quotedString.setLength(0);
					propertyName = null;
				}
			}
		} // End processing string characters.

		if ( debug ) {
			Message.printStatus(2,routine,"Parsed " + propertyCount + " encoded properties.");
		}
	}

	/**
	 * Format the string as:
	 * <ul>
	 * <li> With delimiter: "getStringBeforeDelimiter() // Property1=Value1 Property2=Value2</li>
	 * <li> Without delimiter: "getStringOrig()"
	 * </ul>
	 * If no properties exist after the delimiter, do not output the delimiter.
	 * The output is reconstructed from parsed values and may not exactly match the original string.
	 */
	public String toString () {
		return "";
	}

}