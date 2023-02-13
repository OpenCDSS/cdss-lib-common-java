// StringMultiDictionary - dictionary string that handles the format "Key1:value1, Key2:value2, ...", allowing duplicate keys

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2022 Colorado Department of Natural Resources

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
import java.util.List;

import RTi.Util.Message.Message;

/**
Dictionary that handles parsing a string with format "Key1:value1, Key2:value2, ..."
The keys and values can be quoted with single quotes to surround special characters (spaces, commas, :, etc.).
An array can also be specified for value if surrounded with [ ].
This dictionary handles duplicate keys.
Currently it only parses the string and is not a full implementation of a Map
*/
public class MultiKeyStringDictionary
{

/**
List for the keys, which matches the list for the values.
*/
private List<String> keyList = new ArrayList<>();

/**
List for the values, which matches the list for the keys.
*/
private List<String> valueList = new ArrayList<>();

/**
The key/value separator.
*/
private final String keyValueSep = ":";

/**
The item separator.
*/
private final String itemSep = ",";

/**
Construct the parser by specifying the input string, the key/value separator, and the dictionary item separator.
@param s string dictionary to parse of format:  key1:value1,key2:value2,...
If the value contains special characters, it can be surrounded by single quotes.
The quotes will be removed in the parsed value (and will be added by toString if necessary).
@param keyValueSep separator character between key and value, default is ':'.
@param itemSep separator character between items, default is ','.
*/
public MultiKeyStringDictionary ( String dictString, String keyValueSep, String itemSep ) {
    if ( (dictString != null) && (dictString.length() > 0) && (dictString.indexOf(keyValueSep) > 0) ) {
	    // Have an existing dictionary string so parse and use to populate the dialog.
	    String [] dictParts = null;
	    dictString = dictString.trim();
	    if ( dictString == null ) {
	    	// Nothing to parse.
	    	return;
	    }
	    if ( dictString.indexOf(",") < 0 ) {
	    	// Only one part to parse.
	        dictParts = new String[1];
	        dictParts[0] = dictString;
	    }
	    else {
	    	// Have more than one HTTP header separated by commas.
	    	// TODO smalers 2022-07-04 need to handle comma in dictionary key and value.
	        dictParts = dictString.split(",");
	    }
	    if ( dictParts != null ) {
    	    this.keyList = new ArrayList<>();
    	    this.valueList = new ArrayList<>();
    	    for ( String dictPart : dictParts ) {
    	    	Message.printStatus(2, "", "Parsing dictionary part: \"" + dictPart + "\"");
    	        // Split the part by:
    	        // It is possible that the dictionary entry value contains a protected ':' so have to split manually.
    	        // For example, this is used with the following syntax, with 'key' on the left and 'value on the right:
    	    	//   key:value - simple case
    	    	//   key:'${TS:property}' - to retrieve time series properties (quotes required due to semicolon in property)
    	        //   '${TS:property}':value - to set properties (quotes required due to semicolon in property)
    	    	//   key:'http://some/address' - colon in the value must be protected with single quotes
    	    	//   key:'https://some/address' - colon in the value must be protected with single quotes
   	        	// { - put this here to match the following comment so editors match brackets correctly
    	    	String key = null;
    	    	String value = "";
    	    	// Position for the separating colon.
    	    	int sepPos = 0;
    	    	// The following works for properties.
    	        // Get the key.
    	        int quoteColonPos = dictPart.indexOf("':");
    	        if ( quoteColonPos > 0 ) {
    	        	// The key is like 'key':value or 'key':'value'.
    	        	key = dictPart.substring(0,quoteColonPos).trim().replace("'","");
    	        	sepPos = quoteColonPos + 1;
    	        }
    	        else {
    	        	// No quote around key.
    	        	int colonPos = dictPart.indexOf(":");
    	        	key = dictPart.substring(0,colonPos).trim();
    	        	sepPos = colonPos;
    	        }
    	        // Get the value.
    	        int colonQuotePos = dictPart.indexOf(":'", sepPos);
    	        if ( colonQuotePos > 0 ) {
    	        	// Value is like 'key':'value' or key:'value'.
    	        	if ( (colonQuotePos + 2) >= dictPart.length() ) {
    	        		// Empty value.
    	        		value = "";
    	        	}
    	        	else {
    	        		value = dictPart.substring((colonQuotePos + 2)).trim().replace("'","");
    	        	}
    	        }
    	        else {
    	        	// No quote around value.
    	        	if ( (sepPos + 1) >= dictPart.length() ) {
    	        		// Empty value.
    	        		value = "";
    	        	}
    	        	else {
    	        		value = dictPart.substring((sepPos + 1)).trim();
    	        	}
    	        }
    	        /*
    	         * TODO smalers 2022-07-04 remove unused code when tested out.
    	        int colonPos = dictParts[i].indexOf("}:");
    	        if ( colonPos > 0 ) {
    	            // Have a ${property}:value
    	        	// { - put this here to match the following comment so editors match brackets correctly.
    	            ++colonPos; // Increment one position since }: is 2 characters.
    	        }
    	        else {
    	            // No colon in the key.
    	            colonPos = dictParts[i].indexOf(":");
    	        }
    	        if ( colonPos >= 0 ) {
  	                this.keyList.add(dictParts[i].substring(0,colonPos).trim().replace("'", ""));
  	                if ( colonPos == (dictParts[i].length() - 1) ) {
  	                    // Colon is at the end of the string.
                        this.valueList.add("");
  	                }
  	                else {
    	                this.valueList.add(dictParts[i].substring(colonPos + 1).trim().replace("'", ""));
    	            }
    	        }
    	        else {
    	            this.keyList.add(dictParts[i].trim().replace("'", ""));
    	            this.valueList.add("");
    	        }
    	        */
    	        // Set the parts int the lists.
   	            this.keyList.add(key);
   	            this.valueList.add(value);
    	    }
	    }
    }
}

/**
Get the key at a position.
@param index list position (0+).
*/
public String getKey(int index) {
    return this.keyList.get(index);
}

/**
Get the value at a position.
@param index list position (0+).
*/
public String getValue(int index) {
    return this.valueList.get(index);
}

/**
Get the key list.
*/
public List<String> getKeyList() {
    return this.keyList;
}

/**
Get the value list.
*/
public List<String> getValueList() {
    return this.valueList;
}

/**
 * Return the size of the dictionary.
 */
public int size () {
	return this.keyList.size();
}

/**
Return the string representation of the dictionary in form "key1:value1,key2:value2".
*/
public String toString() {
    StringBuilder b = new StringBuilder();
    for ( int i = 0; i < this.keyList.size(); i++ ) {
        if ( b.length() > 0 ) {
            b.append(itemSep);
        }
    	String key = keyList.get(i);
        String value = this.valueList.get(i);
        String chars = ":,\"";
        if ( StringUtil.containsAny(key, chars, false) ) {
        	// Surround the key with single quotes.
        	b.append("'" + key + "'");
        }
        else {
        	// Return the key as is.
        	b.append(key);
        }
        b.append(keyValueSep);
        if ( StringUtil.containsAny(value, chars, false) ) {
        	// Surround the value with single quotes.
        	b.append("'" + this.valueList.get(i) + "'");
        }
        else {
        	// Return the value as is.
        	b.append(value);
        }
    }
    return b.toString();
}

}