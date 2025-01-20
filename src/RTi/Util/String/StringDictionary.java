// StringDictionary - dictionary string that handles the format "Key1:value1, Key2:value2, ..."

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
Dictionary that handles parsing a string with format "Key1:value1, Key2:value2, ..."
The values can be quoted with single quotes to surround special characters (spaces, commas, :).
An array can also be specified for value if surrounded with [ ].
This dictionary only handles unique keys.
If duplicate keys need to be handled, for example to parse, use the StringMultiDictionary class.
*/
public class StringDictionary
{

/**
LinkedHashMap for the dictionary, which maintains the original insert order.
*/
private LinkedHashMap<String,String> dict = new LinkedHashMap<>();

/**
The key/value separator.
*/
private String keyValueSep = ":";

/**
The item separator.
*/
private String itemSep = ",";

/**
Construct the parser by specifying the input string, the key/value separator, and the dictionary item separator.
The resulting dictionary is guaranteed to be non-null but may be empty.
@param s string dictionary to parse.
@param keyValueSep separator character between key and value, default is ':'.
@param itemSep separator character between items, default is ','.
*/
public StringDictionary ( String s, String keyValueSep, String itemSep ) {
    if ( (s != null) && (s.length() > 0) && (s.indexOf(keyValueSep) > 0) ) {
        // First break map pairs:
    	// - TODO smalers 2019-09-29 this needs logic to handle quoted and bracketed values
        List<String> pairs = StringUtil.breakStringList(s, itemSep, 0 );
        // Now break pairs and put in LinkedHashMap.
        for ( String pair : pairs ) {
            String [] parts = pair.split(keyValueSep);
            if ( parts.length == 1 ) {
                this.dict.put(parts[0].trim(), "" );
            }
            else if ( parts.length > 1 ){
                this.dict.put(parts[0].trim(), parts[1].trim() );
            }
        }
    }
}

/**
Get a value from the dictionary.
@param key string key to look up.
*/
public String get ( String key ) {
    return this.dict.get(key);
}

/**
Return the dictionary as a LinkedHashMap.
@return the dictionary as a LinkedHashMap
*/
public LinkedHashMap<String,String> getLinkedHashMap() {
    return this.dict;
}

/**
 * Return the number of entries in the dictionary.
 * @return the number of entries in the dictionary
 */
public int size() {
	return this.dict.size();
}

/**
Return the string representation of the dictionary in form "key1:value1,key2:value2".
@return the string representation of the dictionary
*/
public String toString() {
    Iterator<Map.Entry<String,String>> i = this.dict.entrySet().iterator();
    StringBuilder b = new StringBuilder();
    Map.Entry<String,String> item;
    while ( i.hasNext() ) {
        item = (Map.Entry<String,String>)i.next();
        if ( b.length() > 0 ) {
            b.append(itemSep);
        }
        b.append(item.getKey() + keyValueSep + item.getValue());
    }
    return b.toString();
}

}