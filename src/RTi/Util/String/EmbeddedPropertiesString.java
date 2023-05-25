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

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This class parses a string containing embedded Property=Value properties.
 * A HashMap of properties and the string without the properties is output.
 */
public class EmbeddedPropertiesString {

	/**
	 * Original string, which may or may not include properties.
	 */
	private String stringOrig = "";

	/**
	 * String without properties.
	 */
	private String stringNoProperties = "";
	
	/**
	 * String that indicates that properties will follow, for example "//".
	 * The indicator is evaluated for each line if line breaks are used.
	 * NOT CURRENTLY IMPLEMENTED.
	 */
	private String propertyIndicator = null;

	/**
	 * Map of properties parsed from the string, guaranteed to be non-null but may be empty.
	 * No properties may be present, in which case the map is empty.
	 */
	private HashMap<String,String> propertyMap = new LinkedHashMap<>();

	/**
	 * Constructor.  Properties can occur anywhere except inside quoted strings.
	 * @param s string that may or may not contain Property=Value strings.
	 */
	public EmbeddedPropertiesString ( String s ) {
		// Call the overloaded version.
		this ( s, null );
	}

	/**
	 * Constructor.
	 * @param s string that may or may not contain Property=Value strings.
	 * @param propertyIndicator string that indicates that properties will follow (e.g., "//"),
	 * if null properties can occur anywhere in the string
	 */
	public EmbeddedPropertiesString ( String s, String propertyIndicator ) {
		this.stringOrig = s;
		this.propertyIndicator = propertyIndicator;
		parse();
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
	 * @param propertyName the name of the property to look up
	 * @return the property value, or null if not defined.
	 */
	public String getPropertyValue ( String propertyName ) {
		return this.propertyMap.get(propertyName);
	}

	/**
	 * Return the properties map.
	 * @return the properties map, guaranteed to not be null.
	 */
	public HashMap<String,String> getPropertyMap() {
		return this.propertyMap;
	}

	/**
	 * Return the string after properties have been removed.
	 * NOT CURRENTLY IMPLEMENTED.
	 * @return the string after properties have been removed
	 */
	public String getStringNoProperties () {
		return this.stringNoProperties;
	}

	/**
	 * Parse the original string.
	 * Properties must use the syntax Property=Value with no spaces around the equal sign or in the property name or value.
	 * Properties are assumed to be at the end of the description using the following syntax:
	 *
	 *   Some description. Property=Value
	 *   Some description. Property1=Value1
	 *   Some description. Property1=Value1 Property2=Value2
	 *   Some description. // Property1=Value1 Property2=Value2
	 *   Some multi-line description.\n
	 *       Property1=Value1\n
	 *       Property2=Value2
	 *
	 * Remove properties with syntax Property=Value (set in 'this.stringNoProperties')
	 * and keep the original text without the properties (in 'this.stringOrig').
	 * If property values contain spaces, enclose in double quotes.
	 */
	private void parse () {
		if ( (this.stringOrig == null) || (this.stringOrig.trim().isEmpty()) ) {
			// Nothing to parse.
			this.stringNoProperties = "";
			this.propertyMap = new LinkedHashMap<>();
			return;
		}

		// Parse the original string and remove the properties.
		// Set the descriptionText to full description by default, but may be reset below.
		this.stringNoProperties = this.stringOrig;

		// Initialize the property map to an empty list, simplifies use elsewhere.
		this.propertyMap = new LinkedHashMap<>();

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
		StringBuilder stringBuilder = new StringBuilder();
		String line; // Single line from description.
		for ( int iline = 0; iline < lines.length; iline++ ) {
			// Single line in description to parse:
			// - remove carriage returns if still present
			line = lines[iline].replace("\r","");
			
			// Reposition the start of he string if 'propertyIndicator' is used.
			if ( (this.propertyIndicator != null) && !this.propertyIndicator.isEmpty() ) {
				int pos2 = line.indexOf(this.propertyIndicator) ;
				if ( pos2 >= 0 ) {
					// Index in line to start looking for properties.
					int pos3 = pos2 + this.propertyIndicator.length();
					if ( pos3 >= line.length() ) {
						// Property indicator is at the end of the line (nothing follows).
						line = "";
					}
					else {
						// There are characters after the indicator to parse.
						line = line.substring(pos3);
					}
				}
			}
			
			// Trim leading and ending whitespace from the line.
			line = line.trim();
			if ( line.length() == 0 ) {
				// No reason to check the line.
				continue;
			}

			// Loop through the line character by character:
			// - this assumes that quoted strings are well-behaved (matching)
			// - could use a regular expression or tokenizer, but can be complex and confusing
			
			boolean inQuotedString = false;
			boolean inWord = false;
			boolean checkProperty = false;
			StringBuilder word = new StringBuilder(); // To store a word delimited by spaces or = (could be the property name or value).
			String propertyName = null; // Property name.
			StringBuilder quotedString = new StringBuilder(); // Quoted string (could be a property value if after equal sign).
			char c;
			for ( int ichar = 0; ichar < line.length(); ichar++ ) {
				c = line.charAt(ichar);
				// Whether to check setting a property:
				// - if set to true below, the character could indicate the end of a property value
				checkProperty = false;
				if ( c == '"' ) {
					if ( inQuotedString ) {
						// End of quote.
						inQuotedString = false;
						// Indicate to check setting a property below.
						checkProperty = true;
					}
					else {
						// Start of quote.
						inQuotedString = true;
						quotedString.setLength(0);
						// Go to the next character.
						continue;
					}
				}
				else if ( (c == ' ') || (c == '\t') ) {
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
						// Not in a word. Just ignore the space.
						continue;
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
					}
				}
				else {
					// Every other character.
					if ( inQuotedString ) {
						// Add the character to the quoted string.
						quotedString.append(c);
					}
					else if ( inWord ) {
						// Add the character to the quoted string.
						word.append(c);
						if ( ichar == (line.length() - 1) ) {
							// End of the line so need to check the property.
							checkProperty = true;
						}
					}
					else {
						// Not in a word so start a new word.
						inWord = true;
						word.setLength(0);
						word.append(c);
					}
				}
				
				if ( checkProperty ) {
					// Check whether a property can be set:
					// - will be the case if have a word, followed by an equal sign, followed by a word or quoted string
					if ( propertyName != null ) {
						if ( quotedString.length() > 0 ) {
							// Have a property value that is a quoted string.
							this.propertyMap.put(propertyName, quotedString.toString());
						}
						else if ( word.length() > 0 ) {
							// Have a property value that is a word.
							this.propertyMap.put(propertyName, word.toString());
						}
						// Since set a property, reset to start looking for another property on the same line.
						inWord = false;
						word.setLength(0);
						inQuotedString = false;
						quotedString.setLength(0);;
						propertyName = null;
					}
				}
			}
		}
		// Reset the description text to the description without properties:
		// - trim to remove any trailing newlines and whitespace
		//this.stringNoProperties = descriptionBuilder.toString().trim();
	}

}