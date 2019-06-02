// MultiValueHash - class to handle multi-value hash

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

package RTi.Collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a hashmap containing multiple values for a key.
 * <p>
 * For instance, Bob may have two phone numbers, 2143 & 1824.
 * <br>
 * The key would be Bob & the values 2143 & 1834
 * 
 * @see Map
 */
 public class MultiValueHash
{
  private Map map= new HashMap();
  
  /**
   * Associates the specified value with the specified key in this map.
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   */
  public void put(Object key, Object value)
  {
    List l =(List) map.get(key);
     if (l==null)
      map.put(key, l=new ArrayList());
    l.add(value);
  }
  
/**
 * Returns the value to which this map maps the specified key.
 * <p>
 * A return value of null is returned if the map contains no mapping for
 * this key; it's also possible tat the map explicitely maps the key to
 * null.
 * 
 * @param key key whose associated value is to be returned.
 * @return the value to which this map maps the specified key, or null
 *  if the map contains no mapping for this key.
 */
  public Object get(Object key)
  {
    List l = (List) map.get(key);
    return l;
  }
  
/**
 * Returns a set view of the keys contained in this map.
 * <p>
 * The set is backed by the map, so changes to the map are reflected in 
 * the set, and vice-versa. 
 * 
 * @return a set view of the keys contained in this map
 */
  public Set keySet()
  {
    return map.keySet();    
  }
  
    public static void main(String[] args)
  {
    MultiValueHash mvh = new MultiValueHash();
    mvh.put("Bob","2143");
    mvh.put("Bob","1834");

    mvh.put("Sally","1234");
    mvh.put("Sally","5678");
    mvh.put("Sally","1010");

    Set set = mvh.keySet();
    System.out.println("# keys: " + set.size());
    String key;
    
    Iterator it = set.iterator();
    while (it.hasNext())
           {
             key =  (String)it.next();
             List l = (List) mvh.get(key);
             
              System.out.println(" " +key + ": " + l);
           }
    
    List l = (List) mvh.get("Bob");
    
   if ( l != null)
        System.out.println(l.size() + ": " + l);
  }
  
}