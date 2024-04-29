// Prop - use to hold an object's properties

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

package RTi.Util.IO;

/**
This class provides a way to generically store property information and can be used similar to Java properties,
environment variables, etc.
The main difference is that it allows the contents of a property to be an Object,
which allows flexibility to use the property for anything in Java.
<p>
A property essentially consists of a string key and an associated object.
The functions that deal with property "contents" return the literal object.
The functions that deal with property "value" return a string representation of the value.
In many cases, the Object will actually be a String and therefore the contents and value will be the same
(there is currently no constructor to take contents only - if it is added,
then the value will be set to contents.toString() at construction).
<p>
A property can also hold a literal string.
This will be the case when a configuration file is read and a literal comment or blank line is to be retained,
to allow outputting the properties with very close to the original formatting.
In this case, the isLiteral() call will return true.
@see PropList
@see PropListManager
*/
public class Prop
implements Comparable<Prop>
{

/**
Indicates that it is unknown how the property was set (this is the default).
*/
public static final int SET_UNKNOWN = 0;

/**
Indicates that the property was set from a file or database.
In this case, when a PropList is saved, the property should typically be saved.
*/
public static final int SET_FROM_PERSISTENT = 1;

/**
Indicates that the property was set at run-time as a default value.
In this case, when a PropList is saved, the property often may be ignored because it
will be set to the same default value the next time.
*/
public static final int SET_AS_RUNTIME_DEFAULT = 2;

/**
Indicates that the property was set by the user at run-time.
In this case, when a PropList is saved,
the property should likely be saved because the user has specified a value different from internal defaults.
*/
public static final int SET_AT_RUNTIME_BY_USER = 3;

/**
Indicates that the property was automatically set for the user at run-time.
In this case, when a PropList is saved,
the property should likely be saved because the user the property is considered important in defining something.
However, for all practical purposes, it is a run-time default and, in and of itself,
should not force the user to save.
*/
public static final int SET_AT_RUNTIME_FOR_USER = 4;

/**
Indicates that the property was set behind the scenes in a way that should be invisible to the user.
Users cannot edit hidden properties, will never see hidden properties,
and should never be able to save hidden properties to a persistent source.
*/
public static final int SET_HIDDEN = 5;

/**
Indicates whether property is read from a persistent source, set internally as a run-time default,
or is set at runtime by the user.
*/
private int	__howSet;

/**
Integer key for faster lookups.
*/
private int	__intKey;

/**
Indicate whether the property is a literal string. By default the property is a normal property.
*/
private boolean __isLiteral = false;

/**
String to look up property.
*/
private String __key;

/**
Contents of property (anything derived from Object).
This may be a string or another object.  If a string, it contains the value before expanding wildcards, etc.
*/
private Object __contents;

/**
Value of the object as a string.  In most cases, the object will be a string.
The value is the fully-expanded string (wildcards and other variables are expanded).
If not a string, this may contain the toString() representation.
 */
private String __value;

/**
Construct a property having no key and no object (not very useful!).
*/
public Prop () {
	initialize ( SET_UNKNOWN, 0, "", null, null );
}

/**
Construct using a string key and a string.
@param key String to use as key to look up property.
@param contents The contents of the property (in this case the same as the value.
*/
public Prop ( String key, String contents ) {
	// Contents and value are the same.
	initialize ( SET_UNKNOWN, 0, key, contents, contents );
}

/**
Construct using a string key, and both contents and string value.
@param key String to use as key to look up property.
@param contents The contents of the property, which may be different than the string value
@param value The value of the property as a string.
*/
public Prop ( String key, Object contents, String value ) {
	// Contents and string may be different.
	initialize ( SET_UNKNOWN, 0, key, contents, value );
}

/**
Construct using a string key, and both contents and string value.
@param key String to use as key to look up property.
@param contents The contents of the property, which may be different than the string value
@param value The value of the property as a string.
@param howSet Indicates how the property is being set.
*/
public Prop ( String key, Object contents, String value, int howSet ) {
	// Contents and string may be different.
	initialize ( howSet, 0, key, contents, value );
}

/**
Construct using a string key, an integer key, and string contents.
@param key String to use as key to look up property.
@param intkey Integer to use to look up the property (integer keys can be used in place of strings for lookups).
@param contents The contents of the property (in this case the same as the
*/
public Prop ( String key, int intkey, String contents ) {
	initialize ( SET_UNKNOWN, intkey, key, contents, contents );
}

/**
Construct using a string key, an integer key, and both contents and value.
@param key String to use as key to look up property.
@param intKey Integer to use to look up the property (integer keys can be used in place of strings for lookups).
@param contents The contents of the property.
@param value The string value of the property.
*/
public Prop ( String key, int intKey, Object contents, String value ) {
	initialize ( SET_UNKNOWN, intKey, key, contents, value );
}

/**
Construct using a string key, an integer key, and both contents and value.
@param key String to use as key to look up property.
@param intKey Integer to use to look up the property (integer keys can be used in place of strings for lookups).
@param contents The contents of the property.
@param value The string value of the property.
@param howSet Indicates how the property is being set.
*/
public Prop ( String key, int intKey, Object contents, String value,int howSet) {
	initialize ( howSet, intKey, key, contents, value );
}

/**
Construct using a string key, an integer key, string contents, and specify modifier flags.
@param key String to use as key to look up property.
@param intKey Integer to use to look up the property (integer keys can be used in place of strings for lookups).
@param contents The contents of the property (in this case the same as the value.
@param howSet Indicates how the property is being set (see SET_*).
*/
public Prop ( String key, int intKey, String contents, int howSet ) {
	initialize ( howSet, intKey, key, contents, contents );
}

/**
Used to compare this Prop to another Prop in order to sort them.  Inherited from Comparable interface.
@param o the Prop to compare against.
@return 0 if the Props' keys and values are the same,
-1 if this Prop sorts earlier than the other Prop,
or 1 if this Prop sorts higher than the other Prop.
*/
public int compareTo(Prop p) {
	// First compare the property name (key).
	int result = __key.compareTo(p.getKey());
	if (result != 0) {
		return result;
	}

	// If the keys are the same, compare the values.
	result = __value.compareTo(p.getValue());

	return result;
}

/**
Return the contents (Object) for the property.
@return The contents (Object) for the property (note: the original is returned, not a copy).
*/
public Object getContents () {
	return __contents;
}

/**
Return the way that the property was set (see SET_*).
@return the way that the property was set.
*/
public int getHowSet () {
	return __howSet;
}

/**
Return the integer key for the property.
@return The integer key for the property.
*/
public int getIntKey () {
	return __intKey;
}

/**
Return whether the property is a literal string.
If true the string contents can be output as is to represent the persistent format of the data, without the key.
For example, the property might be Literal1 = "# Some comment in the file.".
@return true if the property is a literal string, false if not.
*/
public boolean getIsLiteral () {
    return __isLiteral;
}

/**
Return the string key for the property.
@return The string key for the property.
*/
public String getKey () {
	return __key;
}

/**
Return the string value for the property.
@return The string value for the property.
*/
public String getValue () {
	return __value;
}

/**
Return the string value for the property expanding the contents if necessary.
@param props PropList to search.
@return The string value for the property.
*/
public String getValue ( PropList props ) {
	// This will expand contents if necessary.
	refresh ( props );
	return __value;
}

/**
Initialize member data.
@param howSet how the property was set
@param intKey integer key for the property
@param stringKey String key for the property
@param contents Object contents for the property
@param value String equivalent contents for the property (typically contents.toString())
*/
private void initialize ( int howSet, int intKey, String key, Object contents, String value ) {
	__howSet = howSet;
	__intKey = intKey;
	if ( key == null ) {
		__key = "";
	}
	else {
	    __key = key;
	}
	__contents = contents;
	if ( value == null ) {
		__value = "";
	}
	else {
	    __value = value;
	}
}

/**
Refresh the contents by resetting the value by expanding the contents.
@param props PropList to search.
@return The string value for the property.
*/
public void refresh ( PropList props ) {
	int persistent_format = props.getPersistentFormat();
 	if ( (persistent_format == PropList.FORMAT_MAKEFILE) ||
		(persistent_format == PropList.FORMAT_NWSRFS) ||
		(persistent_format == PropList.FORMAT_PROPERTIES) ) {
		// Try to expand the contents...
		if ( __contents instanceof String ) {
			__value = PropListManager.resolveContentsValue(props,(String)__contents);
		}
	}
}

/**
Set the contents for a property.
@param contents The contents of a property as an Object.
*/
public void setContents ( Object contents ) {
	// Use a reference here (do we need a copy?).

	if ( contents != null ) {
		__contents = contents;
	}
}

/**
Set how the property is being set (see SET_*).
Set how the property is being set.
*/
public void setHowSet ( int how_set ) {
	__howSet = how_set;
}

/**
Set the integer key for the property.  This is usually maintained by PropList.
@param intkey Integer key for the property.
@see PropList
*/
public void setIntKey ( int intkey ) {
	__intKey = intkey;
}

/**
Indicate whether the property is a literal string.
@param isLiteral true if the property is a literal string, false if a normal property.
*/
public void setIsLiteral ( boolean isLiteral ) {
    __isLiteral = isLiteral;
}

/**
Set the string key for the property.
@param key String key for the property.
*/
public void setKey ( String key ) {
	if ( key != null ) {
		__key = key;
	}
}

/**
Set the string value for the property.
@param value The string value for the property.
*/
public void setValue ( String value ) {
	if ( value != null ) {
		__value = value;
	}
}

/**
Return a string representation of the property (a verbose output).
@return a string representation of the property.
*/
public String toString () {
	if ( getIsLiteral() ) {
        return __value;
    }
    else {
        return "Key=\"" + __key + "\" (" + __intKey + "), value = \"" + __value + "\" _how_set= " + __howSet;
    }
}

}