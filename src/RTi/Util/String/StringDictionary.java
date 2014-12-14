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