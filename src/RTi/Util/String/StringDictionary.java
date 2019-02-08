// StringDictionary - dictionary string that handles the format "Key1:value1, Key2:value2, ..."

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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
Dictionary string that handles the format "Key1:value1, Key2:value2, ..."
*/
public class StringDictionary
{

/**
LinkedHashMap for the dictionary, which maintains the original insert order.
*/
private LinkedHashMap<String,String> dict = new LinkedHashMap<String,String>();

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
*/
public StringDictionary ( String s, String keyValueSep, String itemSep )
{
    if ( (s != null) && (s.length() > 0) && (s.indexOf(keyValueSep) > 0) ) {
        // First break map pairs
        List<String>pairs = StringUtil.breakStringList(s, itemSep, 0 );
        // Now break pairs and put in LinkedHashMap
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
public String get ( String key )
{
    return this.dict.get(key);
}

/**
Get the dictionary as a TreeMap.
*/
public LinkedHashMap<String,String> getLinkedHashMap()
{
    return this.dict;
}

/**
Return the string representation of the dictionary in form "key1:value1,key2:value2".
*/
public String toString()
{
    Set set = this.dict.entrySet();
    Iterator i = set.iterator();
    StringBuilder b = new StringBuilder();
    Map.Entry item;
    while ( i.hasNext() ) {
        item = (Map.Entry)i.next();
        if ( b.length() > 0 ) {
            b.append(itemSep);
        }
        b.append(item.getKey() + keyValueSep + item.getValue());
    }
    return b.toString();
}

}
