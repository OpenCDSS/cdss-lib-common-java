// ----------------------------------------------------------------------------
// PropList - use to hold a list of properties
// ----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// Sep 1997?	Steven A. Malers,	Initial version.
//		Riverside Technology,
//		inc.
// 02 Feb 1998	SAM, RTi		Get all of the Prop* classes working
//					together.
// 24 Feb 1998	SAM, RTi		Add the javadoc comments.
// 02 May 1998	SAM, RTi		Add the getValid function.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 17 May 1999	SAM, RTi		Add setUsingObject to avoid overload
//					conflict.
// 06 Nov 2000	CEN, RTi		Added read/writePersistent 
//  					implementation similar to C++ implem.
//					Included adding clear
// 14 Jan 2001	SAM, RTi		Overload set to take a Prop.
// 13 Feb 2001	SAM, RTi		Change readPersistent() and associated
//					methods return void as per C++.  Fix
//					bug where readPersistent() was not
//					handling whitespace correctly.  Add
//					javadoc to readPersistent().  Add
//					getValue(key,inst), getProp(key,inst),
//					findProp(key,inst) to store multiple
//					instances of properties with the same
//					key.
// 27 Apr 2001	SAM, RTi		Change all debug levels to 100.
// 10 May 2001	SAM, RTi		Testing to get working with embedded
//					variables like ${...}.  This involves
//					passing the persistent format to the
//					Prop.getValue() method so that it can
//					decide whether to expand the contents.
//					Move routine names into messages
//					themselves to limit overhead.  Set
//					unused variables to null to optimize
//					memory management.  Change initial
//					list size from 100 to 20.
// 14 May 2001	SAM, RTi		Change so that when parsing properties
//					the = is the only delimiter so that
//					quotes around arguments with spaces
//					are not needed.
// 2001-11-08	SAM, RTi		Synchronize with UNIX.  Changes from
//					2001-05-14... Add a boolean flag
//					_literal_quotes to keep the quotes in
//					the PropList.  This is useful where
//					commands are saved in PropLists.
//					Change so when reading a persistent
//					file, the file can be a regular file or
//					a URL.
// 2002-01-20	SAM, RTi		Fix one case where equals() was being
//					used instead of equalsIgnoreCase() when
//					finding property names.
// 2002-02-03	SAM, RTi		Add setHowSet() and getHowSet() to track
//					how a property is set.  Remove the
//					*_CONFIG static parameters because
//					similar values are found in Prop.  The
//					values were never used.  Change set
//					methods to be void instead of having a
//					return type.  The return value is never
//					used.  Fix bug where setValue() was
//					replacing the Prop in the PropList with
//					the given object (rather than the value
//					in the Prop) - not sure if this code
//					was ever getting called!  Change
//					readPersistent() and writePersistent()
//					to throw an IOException if there is an
//					error.  Add getPropsMatchingRegExp(),
//					which is used by TSProduct to help write
//					properties.
// 2002-07-01	SAM, RTi		Add elementAt() to get a property at
//					a position.
// 2002-12-24	SAM, RTi		Support /* */ comments in Java PropList.
// 2003-03-27	SAM, RTi		Fix bugs in setContents() and setValue()
//					methods where when a match was found
//					the code did not return, resulting in
//					a new duplicate property also being
//					appended.
// 2003-10-27	J. Thomas Sapienza, RTi	Added a very basic copy constructor.
//					In the future should implement
//					clone() and a copy constructor that
//					can handle Props that have data objects.
// 2003-11-11	JTS, RTi		* Added getPropCount().
//					* Added the methods with the replace
//					  parameter.
//					* Added unSetAll().
// 2004-02-03	SAM, RTi		* Add parse().
// 2004-07-15	JTS, RTi		Added sortList().
// 2004-11-29	JTS, RTi		Added getList().
// 2005-04-29	SAM, RTi		* Overload the parse method to take a
//					  "how set" value.
// 2005-06-09	JTS, RTi		Warnings in readPersistent() are now 
//					printed at level 2.
// 2005-12-06	JTS, RTi		Added validatePropNames().
// 2007-03-02	SAM, RTi		Update setUsingObject() to allow null.
// ---------------------------------------------------------------------------- 
// EndHeader

package RTi.Util.IO;

import java.lang.Object;
import java.lang.String;
import java.util.List;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class manages a list of Prop instances, effectively creating a properties
list that can be used to store properties for an object, etc.  This class
contains a list of Prop instances and methods to interface with the data (set
properties, look up properties, etc.).  Property lists are typically used to
store and pass variable length, variable content data, as opposed to fixed
parameters.  Often, only a PropList needs to be used (and Prop, PropListManager)
can be avoided.  Note that the standard Java Hashtable can also be used for
properties but does not have some of the features of PropList.
<p>

Often, a PropList will contain only simple string properties.  However, it is
possible to store any Object in a PropList, keyed by a name.  Internally, each
property has a String key, a String value, an Object contents, and an integer
flag indicating how the property was set (from file, by user, etc).  For simple
strings, the value and contents are the same.  For other Objects, the contents
evaluates to toString(); however, applications will often use the contents
directly by casting after retrieving from the PropList.
<p>

An additional feature of PropList is the use of variables in strings.  If a
PropList is created using a persistent format of FORMAT_NWSRFS or
FORMAT_MAKEFILE, variables are encoded using $(varname).  If FORMAT_PROPERTIES,
the notation is ${varname}.  For example, two properties may be defined as:
<p>
<pre>
prop1 = "Hello World"
title = "My name is ${prop1}
</pre>

The default persistent format does not support this behavior, but using the
formats described above automatically supports this behavior.  Additionally,
the IOUtil property methods (setProp(), getProp*()) are used to check for
properties.  Therefore, you can use IOUtil to define properties one place in an
application and use the properties in a different part of the application.
<p>

Each of the special formats will also make an external call to a program to fill
in information if the following syntax is used:
<p>
<pre>
prop1 = "Current time: `date`"
</pre>

The back-quotes cause a system call using ProcessManager and the output is
placed in the resulting variable.
<p>
If the properties are edited at run-time (e.g., by a graphical user interface),
it is common to use the "how set" flag to control how the properties file is
written.  For example:
<pre>
PropList props = new PropList ( "" );
props.setPersistentName ( "somefile" );
// The following uses a "how set" value of Prop.SET_FROM_PERSISTENT.
props.readPersistent ( "somefile" );
// Next, the application may check the file properties and assign some internal
// defaults to have a full set of properties...
props.setHowSet ( Prop.SET_AS_RUNTIME_DEFAULT );
// When a user interface is displayed...
props.setHowSet ( Prop.SET_AT_RUNTIME_BY_USER );
// ...User interaction...
props.setHowSet ( Prop.SET_UNKNOWN );
// Then there is usually custom code to write a specific PropList to a file.
// Only properties that were originally read or have been modified by the user
// may be written (internal defaults often make the property list verbose, but
// may be still desirable).
</pre>
@see Prop
@see PropListManager
@see ProcessManager
*/
public class PropList
{

/**
Indicates that the configuration file format is unknown.
*/
public static final int	FORMAT_UNKNOWN = 0;

/**
Indicates that the configuration file format is that used by Makefiles.
*/
public static final int	FORMAT_MAKEFILE	= 1;

/**
Indicates that the configuration file format is that used by NWSRFS
configuration files (apps_defaults).  A : is used instead of the = for assignment.
*/
public static final int	FORMAT_NWSRFS = 2;

/**
Indicates that configuration information is being stored in a custom database.
*/
public static final int	FORMAT_CUSTOM_DB = 3;

/**
Indicates that configuration information is being stored in standard RTi properties file.
*/
public static final int	FORMAT_PROPERTIES = 4;

/**
Name of this PropList.
*/
private String __listName;
/**
List of Prop.
*/
private List<Prop> __list;
/**
File to save in.
*/
private String __persistentName;
/**
Format of file to read.
*/
private int	__persistentFormat;
/**
Last line read from the property file.
*/
private int	__lastLineNumberRead;
/**
Indicates if quotes should be treated literally when setting Prop values.
*/
private boolean	__literalQuotes = true;
/**
The "how set" value to use when properties are being set.
*/
private int	__howSet = Prop.SET_UNKNOWN;

/**
Copy constructor for a PropList.  Currently only works with PropLists that store String key/value pairs.<p>
TODO (JTS - 2003-10-27) Later, add in support for cloning of props that have data objects in the value section.
@param props the PropList to duplicate.
*/
public PropList(PropList props) {
	// in the order of the data members above ...
	
	setPropListName(new String(props.getPropListName()));

	__list = new Vector(100, 10);
	// duplicate all the props
	int size = props.size();
	Prop prop = null;
	for (int i = 0; i < size; i++) {
		prop = props.propAt(i);
		set(new Prop(prop.getKey(), prop.getValue()), false);
	}

	setPersistentName(new String(props.getPersistentName()));
	setPersistentFormat(props.getPersistentFormat());
	// __lastLineNumberRead is ignored (that var probably doesn't need to be private)
	setLiteralQuotes(props.getLiteralQuotes());
	setHowSet(props.getHowSet());
}

/**
Construct given the name of the list (the list name should be unique if
multiple lists are being used in a PropListManager.  The persistent format defaults to FORMAT_UNKNOWN.
@param listName The name of the property list.
@see PropListManager
*/
public PropList ( String listName )
{	initialize ( listName, "", FORMAT_UNKNOWN );
}

/**
Construct using a list name and a configuration file format type.
@param listName The name of the property list.
@param persistentFormat The format of the list when written to a configuration file (see FORMAT_*).
*/
public PropList ( String listName, int persistentFormat )
{	initialize ( listName, "", persistentFormat );
}

/**
Construct using a list name, a configuration file name,
and a configuration file format type.  The file is not actually read (call readPersistent() to do so).
@param listName The name of the property list.
@param persistentName The name of the configuration file.
@param persistentFormat The format of the list when written to a configuration file (see FORMAT_*).
*/
public PropList ( String listName, String persistentName, int persistentFormat )
{	initialize ( listName, persistentName, persistentFormat );
}

/**
Add a property by parsing out a property string like "X=Y".
@param prop_string A property string.
*/
public void add ( String prop_string )
{	// Just call the set routine...
	set ( prop_string );
}

/**
Append a property to the list using a string key.
@param key String key for the property.
@param contents Contents for the property.
*/
private void append ( String key, Object contents, boolean isLiteral )
{	Prop prop = new Prop ( key, contents, contents.toString(), __howSet );
    prop.setIsLiteral ( isLiteral );
	if ( Message.isDebugOn ) {
		Message.printDebug ( 100, "PropList.append", "Setting property \"" + key + "\" to: \"" + contents.toString() + "\"" );
	}
	__list.add ( prop );
}

/**
Append a property to the list using a string key, the contents, and value.
@param key String key for the property.
@param contents Contents for the property.
@param value String value for the property.
*/
private void append ( String key, Object contents, String value )
{	Prop prop = new Prop ( key, contents, value, __howSet );
	if ( Message.isDebugOn ) {
		Message.printDebug ( 100, "PropList.append", "Setting property \"" + key + "\" to: \"" + value + "\"" );
	}
	__list.add ( prop );
}

/**
Remove all items from the PropList.
*/
public void clear()
{	__list.clear();
}

/**
Return the Prop instance at the requested position.  Use size() to determine the size of the PropList.
@param pos the position of the property to return (0+).
@return the Prop at the specified position.
*/
public Prop elementAt(int pos)
{	return __list.get(pos);
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	__listName = null;
	__list = null;
	__persistentName = null;
	super.finalize();
}

/**
Find a property in the list.
@return The index position of the property corresponding to the string key, or -1 if not found.
@param key The string key used to look up the property.
*/
public int findProp ( String key )
{	int	size = __list.size();
	Prop prop_i;
	String propKey;
	for ( int i = 0; i < size; i++ ) {
		prop_i = __list.get(i);
		propKey = (String)prop_i.getKey();
		if ( key.equalsIgnoreCase(propKey) ) {
			// Have a match.  Return the position...
			if ( Message.isDebugOn ) {
				Message.printDebug ( 100, "PropList.findProp", "Found property \"" + key + "\" at index " + i);
			}
			return i;
		}
	}
	return -1;
}

/**
Find a property in the list.
@return The index position of the property corresponding to the string key, or -1 if not found.
@param key The string key used to look up the property.
@param inst Instance number of property (0+). If inst is one, then
the second property with that name is returned, etc.
*/
public int findProp ( String key, int inst )
{	int size = size();
	for ( int i = 0; i < size; i++ ) {
		if ( propAt(i).getKey().equalsIgnoreCase( key ) ) {
			if ( (inst--) != 0 ) {
				 continue;
			}
			return i;
		}
	}
	return -1;
}

/**
Find a property in the list.
@return The index position of the property corresponding to the integer key, or -1 if not found.
@param intKey The integer key used to look up the property.
*/
public int findProp ( int intKey )
{	int	prop_intKey, size = __list.size();
	Prop prop_i;
	for ( int i = 0; i < size; i++ ) {
		prop_i = __list.get(i);
		prop_intKey = prop_i.getIntKey();
		if ( intKey == prop_intKey ) {
			// Have a match.  Return the position...
			return i;
		}
	}
	return -1;
}

/**
Return property contents.
@return The contents of the property as an Object, or null if not found.
@param key The string key used to look up the property.
*/
public Object getContents ( String key )
{	int	pos = findProp ( key );
	if ( pos >= 0 ) {
		// We have a match.  Get the contents...
		return (__list.get(pos)).getContents();
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 100, "PropList.getContents", "Did not find property \"" + key + "\"" );
	}
	return null;
}

/**
Return the "how set" value that is in effect when setting properties.
@return the "how set" value that is in effect when setting properties.
*/
public int getHowSet ()
{	return __howSet;
}

/**
Returns the list of Props.
@return the list of Props.
*/
public List<Prop> getList() {
	return __list;
}

/**
Indicate whether quotes in the contents should be handled literally.
@return true if quotes are handled literally, false if they should be discarded
when contents are converted to the string value.
*/
public boolean getLiteralQuotes ()
{	return __literalQuotes;
}

/**
Return the name of the property list.
@return The name of the property list.
*/
public String getPropListName ( )
{	return __listName;
}

/**
Returns a count of the number of the properties in the list that have the specified key value.
@param key the key for which to find matching properties.  Can not be null.
@return the number of properties in the list with the same key.
*/
public int getPropCount ( String key )
{	int count = 0;
	int size = size();
	for ( int i = 0; i < size; i++ ) {
		if ( propAt(i).getKey().equalsIgnoreCase( key ) ) {
			count++;
		}
	}
	return count;
}

// TODO SAM 2005-04-29 StringUtil.matchesRegExp() has been deprecated.
// Need to figure out how to update the following code.

/**
Return a list of Prop that have a key that matches a regular expression.
This is useful when writing a PropList to a file in a well-defined order.
@param regExp Regular expression recognized by StringUtil.matchesRegExp().
@return a list of Prop, or null if no matching properties are found.
*/
public List<Prop> getPropsMatchingRegExp ( String regExp )
{	if ( (__list == null) || (regExp == null) ) {
		return null;
	}
	int size = __list.size();
	List<Prop> props = new Vector ();
	Prop prop;
	for ( int i = 0; i < size; i++ ) {
		prop = __list.get(i);
		// Do a case-independent comparison...
		if ( StringUtil.matchesRegExp(true, prop.getKey(), regExp)) {
			props.add ( prop );
		}
	}
	if ( props.size() == 0 ) {
		props = null;
	}
	return props;
}

/**
Return the name of the property list file.
@return The name of the property list file.
*/
public String getPersistentName ( )
{	return __persistentName;
}

/**
Return the format of the property list file.
@return The format of the property list file.
*/
public int getPersistentFormat ( )
{	return __persistentFormat;
}

/**
Search the list using the string key.
@return The property corresponding to the string key, or null if not found.
@param key The string key used to look up the property.
*/
public Prop getProp ( String key )
{	int	pos = findProp ( key );
	if ( pos >= 0 ) {
		Prop prop = __list.get(pos);
		prop.refresh(this);
		return prop;
	}
	return null;
}

/**
Return the property corresponding to the string key or null if not found.
@return The property corresponding to the string key, or <CODE>null</CODE> if not found.
@param key The string key used to look up the property.
@param inst Instance number of property (0+). If inst is one, then
the second property with that name is returned, etc.
*/
public Prop getProp ( String key, int inst )
{	int index = findProp ( key, inst );
	if( index == -1 ) {
		return null;
	}
	return propAt ( index );
}

/**
Search the list using the integer key.  This should not be confused with
elementAt(), which returns the property at a position in the list.
@return The property corresponding to the string key.
@param intKey The integer key used to look up the property.
*/
public Prop getProp ( int intKey )
{	int pos = findProp ( intKey );
	if ( pos >= 0 ) {
		return __list.get(pos);
	}
	return null;
}

/**
Return a valid non-null PropList.
@return A valid PropList.  If the PropList that is passed in is null, a new
PropList will be created given the supplied name.  This is useful for routines
that need a valid PropList for local processing (rather than checking for a
null list that may have been passed as input).
@param props PropList to check.  Will be returned if not null.
@param newName Name of new PropList if "props" is null.
*/
public static PropList getValidPropList ( PropList props, String newName )
{	if ( props != null ) {
		// List is not null, so return...
		return props;
	}
	else {
	    // List is null, so create a new and return...
		if ( newName == null ) {
			return new PropList ( "PropList.getValidPropList" );
		}
		return new PropList ( newName );
	}
}

/**
The string value of the property corresponding to the string key, or null if not found.
@return The string value of the property corresponding to the string key.
@param key The string key used to look up the property.
*/
public String getValue ( String key )
{	int pos = findProp ( key );
	if ( pos >= 0 ) {
		// We have a match.  Get the value...
		String value = (__list.get(pos)).getValue(this);
		if ( Message.isDebugOn ) {
			Message.printDebug(100,"PropList.getValue", "Found value of \"" + key + "\" to be \"" + value + "\"" );
		}
		return value;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 100, "PropList.getValue", "Did not find property \"" + key + "\"" );
	}
	return null;
}

/**
Return the string value of the property given the instance number (allows
storage of duplicate keys), or null if not found.
@return The string value of the property corresponding to the string key, or <CODE>null</CODE> if not found.
@param key The string key used to look up the property.
@param inst Instance number of property (0+). If inst is one, then
the second property with that name is returned, etc.
*/
public String getValue ( String key, int inst )
{	Prop p = getProp ( key, inst );
	if ( p == null ) {
		return null;
	}
	String value = p.getValue ( this );
	p = null;
	return value;
}

/**
Return the string value of the property corresponding to the integer key, or null if not a valid integer key.
@return The string value of the property corresponding to the integer key.
@param intKey The integer key used to look up the property.
*/
public String getValue ( int intKey )
{	int	pos = findProp ( intKey );
	if ( pos >= 0 ) {
		// Have a match.  Get the value...
		return (__list.get(pos)).getValue( this );
	}
	return null;
}

/**
Initialize the object.
@param listName Name for the PropList.
@param persistentName Persistent name for the PropList (used only when reading from a file).
@param persistentFormat Format for properties file.
*/
private void initialize ( String listName, String persistentName, int persistentFormat )
{	if ( listName == null ) {
		__listName = "";
	}
	else {
	    __listName = listName;
	}
	if ( persistentName == null ) {
		__persistentName = "";
	}
	else {
	    __persistentName = persistentName;
	}
	__persistentFormat = persistentFormat;
	__list = new Vector (20,10);	// A vector of Prop
	__lastLineNumberRead = 0;
}

/**
Create a PropList by parsing a string of the form prop="val",prop="val".
The "how set" value for each property is set to Prop.SET_UNKNOWN.
@param string String to parse.
@param listName the name to assign to the new PropList.
@param delim the delimiter to use when parsing the proplist ("," in the above
example).  Quoted strings are assumed to be allowed.
@return a PropList with the expanded properties.  A non-null value is
guaranteed; however, the list may contain zero items.
*/
public static PropList parse ( String string, String listName, String delim )
{	return parse ( Prop.SET_UNKNOWN, string, listName, delim );
}

/**
Create a PropList by parsing a string of the form: prop="val",prop="val".  There
may be spaces embedded between tokens.
@param howSet Indicate the "how set" value that should be set for all
properties that are parsed (see Prop.SET_*).
@param string String to parse.
@param listName the name to assign to the new PropList.
@param delim the delimiter to use when parsing the PropList ("," in the above
example).  Quoted strings are assumed to be allowed.
@return a PropList with the expanded properties.  A non-null value is
guaranteed; however, the list may contain zero items.
@see Prop
*/
public static PropList parse ( int howSet, String string, String listName, String delim )
{	PropList props = new PropList ( listName );
	props.setHowSet ( howSet );
	if ( string == null ) {
		return props;
	}
	// Allowing quoted strings is necessary because a comma or = could be in a string
	List<String> tokens = StringUtil.breakStringList ( string, delim, StringUtil.DELIM_ALLOW_STRINGS );
						
	int size = 0;
	if ( tokens != null ) {
		size = tokens.size();
	}
	for ( int i = 0; i < size; i++ ) {
	    //Message.printStatus ( 2, "PropList.parse", "Parsing parameter string \"" + (String)tokens.elementAt(i));
		// The above call to breakStringList() may have stripped quotes that would protected "=" in the
	    // properties.  Therefore just find the first "=" and take the left and right sides.
	    String token = (String)tokens.get(i);
	    int pos = token.indexOf('=');
	    if ( pos > 0 ) {
	        // Don't want property names to have spaces
		    String prop = token.substring(0,pos).trim();
		    // TODO SAM 2008-11-18 Evaluate how to handle whitespace
		    String value = "";
		    if ( token.length() > (pos + 1) ) {
		        // Right side is NOT empty
		        value = token.substring((pos + 1),token.length());
		    }
		    //Message.printStatus ( 2, "PropList.parse", "Setting property \"" + prop + "\"=\"" + value + "\"" );
			props.set ( prop, value );
		}
	}
	return props;
}

/**
Return the prop at a position (zero-index), or null if the index is out of range.
@return The property for the specified index position (referenced to zero).
Return null if the index is invalid.
@param i The index position used to look up the property.
*/
public Prop propAt ( int i )
{	if ( (i < 0) || (i > (__list.size() - 1)) ) {
		return null;
	}
	return __list.get ( i );
}

/**
Read a property list from a persistent source, appending new properties to current list.
The properties will be appended to the current list and non-property literals like comments will be ignored.
@exception IOException if there is an error reading the file.
*/
public void readPersistent ()
throws IOException
{	readPersistent ( true );
}

/**
Read a property list from a persistent source, appending new properties to current list.
Non-property literals like comments will be ignored.
@param append If true, properties from the file will be appended to the current list.
@exception IOException if there is an error reading the file.
*/
public void readPersistent ( boolean append )
throws IOException
{
    readPersistent ( true, false );
}

/**
Read a property list from a persistent source.  The "how_set" flag for each
property is set to Prop.SET_FROM_PERSISTENT.  The file can have the format:
<pre>
# COMMENT
# Simple setting:
variable = value

\/\*
Multi-line
comment - start and end of comments must be in first characters of line.
\*\/

# Use section headings:
[MyProps]
prop1 = value

# For the above retrieve with MyProps.prop1

# Lines can have continuation if \ is at the end:
variable = xxxx \
yyy

# Properties with whitespace can be enclosed in " " for clarity (or not):
variable = "string with spaces"

# Variables ${var} will be expanded at query time to compare to
# IOUtil.getPropValue() and also other defined properties:
variable = ${var}

# Text defined inside 'hard quotes' will not be expanded and will be literal:
variable = 'hello ${world}'
variable = 'hello `date`'

# Text defined inside "soft quotes" will be expanded:
variable = "hello ${world}"
variable = "hello `date`"

# Duplicate variables will be read in.  However, to lookup, use getPropValue()
</pre>
@param append Append to current property list (true) or clear out current list (false).
@param includeLiterals if true, comments and other non-property lines will be included as literals
in the property list using key "Literal1", "Literal2", etc.  This is useful if reading a property file,
updating its values, and then writing out again, trying to retain the original comments.
@exception IOException if there is an error reading the file.
*/
public void readPersistent ( boolean append, boolean includeLiterals )
throws IOException
{	String routine = "PropList.readPersistent";

	String line; 
	String prefix = "";
	int idx;
	boolean continuation = false;
	String lineSave = null;
	String name, value;
	List<String> v = null;
	boolean inComment = false;
	int literalCount = 0;

	if ( ! append ) {
		clear();
	}

	BufferedReader in = null;
	try {
	    in = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream(__persistentName) ) );
	} catch ( Exception e ) {
		String message = "Unable to open \"" + __persistentName + "\" to read properties.";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}

	int howSetPrev = __howSet;
	__howSet = Prop.SET_FROM_PERSISTENT;
	try {
        __lastLineNumberRead = 0;	
        int length = 0;
        while ((line = in.readLine()) != null ) {
        	line = line.trim();
        	++__lastLineNumberRead;
        	if ( continuation ) {
        		// Take this line and add it to the previous.  Add a space to separate tokens.
        	    // Should not normally be a comment.
        		line = lineSave + " " + line;
        	}
        	// Handle line continuation with \ at end...
        	if ( line.endsWith("\\")) {
        		continuation = true;
        		// Add a space at the end because when continuing lines the next line likely has separation tokens.
        		lineSave = line.substring(0,line.length()-1);
        		continue;
        	}
        	continuation = false;
        	lineSave = null;
        	if ( (line.length() > 0) && (line.charAt(0) == '#') ) {
        	    // Comment line
                if ( includeLiterals ) {
                    ++literalCount;
                    append ( "Literal" + literalCount, line, true );
                }
        	    continue;
        	}
        	if ( (idx = line.indexOf( '#' )) != -1 ) {
        	    // Remove # comments from the ends of lines
        		line = line.substring( 0, idx );
        	}
        	if ( inComment && line.startsWith("*/") ) {
        		inComment = false;
        		// For now the end of comment must be at the start of the line so ignore the rest of the line...
        		if ( includeLiterals ) {
        		    ++literalCount;
        		    append ( "Literal" + literalCount, line, true );
        		}
        		continue;
        	}
        	if ( !inComment && line.startsWith("/*") ) {
        		inComment = true;
        		// For now the end of comment must be at the start of the line so ignore the rest of the line...
                if ( includeLiterals ) {
                    ++literalCount;
                    append ( "Literal" + literalCount, line, true );
                }
        		continue;
        	}
        	if ( inComment ) {
        		// Did not detect an end to the comment above so skip the line...
                if ( includeLiterals ) {
                    ++literalCount;
                    append ( "Literal" + literalCount, line, true );
                }
        		continue;
        	}
        	if ( line.length() == 0 ) {
                if ( includeLiterals ) {
                    ++literalCount;
                    append ( "Literal" + literalCount, line, true );
                }
        		continue;
        	}
        	if ( line.charAt( 0 ) == '[') {
        	    // Block indicator - contents of [] will be prepended to property names
        		if ( line.indexOf(']') == -1 ) {
        			Message.printWarning ( 2, routine, "Missing ] on line " + __lastLineNumberRead + " of " +
        			     __persistentName );
        			continue;
        		}
        		prefix = line.substring ( 1, line.indexOf(']')) + ".";
                if ( includeLiterals ) {
                    ++literalCount;
                    append ( "Literal" + literalCount, line, true );
                }
        		continue;
        	}
        
        	int pos = line.indexOf('=');
        	if ( pos < 0 ) {
        		Message.printWarning ( 2, routine, "Missing equal sign on line " +
        		__lastLineNumberRead + " of " + __persistentName + " (" + line + ")" );
                if ( includeLiterals ) {
                    ++literalCount;
                    append ( "Literal" + literalCount, line, true );
                }
        		continue;
        	}
        	v = new Vector(2);
        	v.add ( line.substring(0,pos) );
        	v.add ( line.substring(pos + 1) );
        
        	if ( v.size() == 2 ) {
        		name = prefix + (v.get(0)).trim();
        		value = (v.get(1)).trim();
        		length = value.length();
        		if ( (length > 1) && ((value.charAt(0) == '"') || (value.charAt(0) == '\'') &&
        			(value.charAt(0) == value.charAt(length-1))) ) {
        			// Get rid of bounding quotes because they are not needed...
        			value = value.substring(1,(length - 1));
        		}
        		// Now set in the PropList...
        		if ( name.length() > 0 ) {
        			append ( name, value, false );
        		}
        	}
        	else {
        	    Message.printWarning ( 2, routine, "Missing or too many equal signs on line " + 
        		__lastLineNumberRead + " of " + __persistentName + " (" + line + ")");
        	}
        }
        
        in.close();
	} catch ( Exception e ) {
		String message = "Exception caught while reading line " + __lastLineNumberRead + " of " +  __persistentName + ".";
		Message.printWarning ( 3, routine, message );
		throw new IOException ( message );
	}
    finally {
        if ( in != null ) {
            in.close();
        }
    }
	// Clean up...
	__howSet = howSetPrev;
}

/**
Set the property given a string like "prop=propcontents" where "propcontents"
can be a string containing wild-cards.  This feature is used with configuration files.
If the property key exists, reset the property to the new information.
@see PropListManager
@param propString A property string like prop=contents.
*/
public void set ( String propString )
{	Prop prop = PropListManager.parsePropString(this,propString);
	if ( prop == null ) {
		return;
	}
	set ( prop.getKey(), (String)prop.getContents(), prop.getValue(this) );
}

/**
Set the property given a string key and string value. 
If the property key exists, reset the property to the new information.
@param key The property string key.
@param value The string value of the property (will also be used for the contents).
*/
public void set ( String key, String value )
{	set(key, value, true);
}

/**
Set the property given a string key and string value.  If the key already exists
it will be replaced if the replace parameter is true.  Otherwise, a duplicate property will be added to the list.
@param key The property string key.
@param value The string value of the property (will also be used for the contents.
@param replace if true, if the key already exists in the PropList, its value
will be replaced.  If false, a duplicate key will be added.
*/
public void set ( String key, String value, boolean replace )
{	int index = findProp ( key );
	
	if ( index < 0 || !replace) {
		// Not currently in the list so add it...
		append ( key, (Object)value, value );
	}
	else {
	    // Already in the list so change it...
		Prop prop = __list.get ( index );
		prop.setKey ( key );
		prop.setContents ( value );
		prop.setValue ( value );
		prop.setHowSet ( __howSet );
	}
}

/**
Set the property given a string key and contents. 
If the property key exists, reset the property to the new information.
@param key The property string key.
@param contents The contents of the property.
@param value The string value of the property
*/
public void set ( String key, String contents, String value )
{	set ( key, contents, value, true);
}

/**
Set the property given a string key and contents. If the key already exists
it will be replaced if the replace parameter is true.  Otherwise, a duplicate property will be added to the list.
@param key The property string key.
@param contents The contents of the property.
@param value The string value of the property
@param replace if true, if the key already exists in the PropList, its value
will be replaced.  If false, a duplicate key will be added.
*/
public void set ( String key, String contents, String value, boolean replace )
{	// Find if this is already a property in this list...

	int index = findProp ( key );
	if ( index < 0 || !replace ) {
		// Not currently in the list so add it...
		append ( key, contents, value );
	}
	else {
	    // Already in the list so change it...
		Prop prop = __list.get ( index );
		prop.setKey ( key );
		prop.setContents ( contents );
		prop.setValue ( value );
		prop.setHowSet ( __howSet );
	}
}

/**
Set the property given a Prop.  If the property key exists, reset the property to the new information.
@param prop The contents of the property.
*/
public void set ( Prop prop )
{	set ( prop, true);
}

/**
Set the property given a Prop. If the key already exists
it will be replaced if the replace parameter is true.  Otherwise, a duplicate property will be added to the list.
If the property key exists, reset the property to the new information.
@param prop The contents of the property.
*/
public void set ( Prop prop, boolean replace )
{	// Find if this is already a property in this list...
	if ( prop == null ) {
		return;
	}
	int index = findProp ( prop.getKey() );
	if ( index < 0 || !replace ) {
		// Not currently in the list so add it...
		append ( prop.getKey(), prop.getContents(), prop.getValue(this) );
	}
	else {
	    // Already in the list so change it...
		prop.setHowSet ( __howSet );
		__list.set ( index, prop );
	}
}

/**
Set the "how set" flag, which indicates how properties are being set.
@param how_set For properties that are being set, indicates how they are being set (see Prop.SET_*).
*/
public void setHowSet ( int how_set ) {
	__howSet = how_set;
}

/**
Set the flag for how to handle quotes in the PropList when expanding the
contents.  The default is to keep the literal quotes.  This parameter is
checked by the PropListManager.resolveContentsValue() method.
@param literal_quotes If set to true, quotes in the contents will be passed
literally to the value.  If set to false, the quotes will be discarded when
contents are converted to a string value.
*/
public void setLiteralQuotes ( boolean literal_quotes )
{	__literalQuotes = literal_quotes;
}

/**
Set the property list name.
@param list_name The property list name.
*/
public int setPropListName ( String list_name )
{	if ( list_name != null ) {
		__listName = list_name;
	}
	return 0;
}

/**
Set the configuration file name.
@param persistent_name The configuration file name.
*/
public void setPersistentName ( String persistent_name )
{	if ( persistent_name != null ) {
		__persistentName = persistent_name;
	}
}

/**
Set the configuration file format.
@param persistent_format The configuration file format.
*/
public void setPersistentFormat ( int persistent_format )
{	__persistentFormat = persistent_format;
}

/**
Set the property given a string key and contents.  If the contents do not have
a clean string value, use set ( new Prop(String,Object,String) ) instead.
If the property key exists, reset the property to the new information.
@param key The property string key.
@param contents The contents of the property as an Object.  The value is
determined by calling the object's toString() method.  If contents are null, then
the String value is also set to null.
*/
public void setUsingObject ( String key, Object contents )
{	// Ignore null keys...

	if ( key == null ) {
		return;
	}

	// Find if this is already a property in this list...

	int index = findProp ( key );
	String value = null;
	if ( contents != null ) {
		contents.toString();
	}
	if ( index < 0 ) {
		// Not currently in the list so add it...
		append ( key, contents, value );
	}
	else {
	    // Already in the list so change it...
		Prop prop = __list.get ( index );
		prop.setKey ( key );
		prop.setContents ( contents );
		prop.setValue ( value );
		prop.setHowSet ( __howSet );
	}
	value = null;
}

/**
Set the value of a property after finding in the list (warning:  if the
key is not found, a new property will be added).
<b>Warning:  If using the PropList in a general way, set values using the
set() method (setValue is a used internally and for advanced code - additional
checks are being added to make setValue more universal).</b>
@param key String key for property.
@param value String value for property.
*/
public void setValue ( String key, String value )
{	int pos = findProp(key);
	if ( pos >= 0 ) {
		// Have a match.  Reset the value in the corresponding Prop...
		Prop prop = __list.get(pos);
		prop.setValue(value);
		return;
	}
	// If we get to here we did not find a match and need to add a new item to the list...
	append ( key, value, false );
}

/**
Return the size of the property list.
@return The size of the property list.
*/
public int size ()
{	if ( __list == null ) {
		return 0;
	}
	else {
	    return __list.size();
	}
}

/**
Does a simple sort of the items in the PropList, using java.util.Collections().
*/
public void sortList()
{
	java.util.Collections.sort(__list);
}

/**
Return a string representation of the PropList (verbose).
@return A string representation of the PropList (verbose).
*/
public String toString ()
{	return toString(",");
}

/**
Return a string representation of the PropList (verbose).
The properties are formatted as follows:  prop="value",prop="value"
@return A string representation of the PropList (verbose).
@param delim Delimiter to use between properties (comma in the above example).
*/
public String toString ( String delim )
{	StringBuffer b = new StringBuffer();
	int size = __list.size();
	Prop prop;
	for ( int i = 0; i < size; i++ ) {
		prop = __list.get(i);
		if ( i > 0 ) {
			b.append ( delim );
		}
		if ( prop == null ) {
			b.append ( "null" );
		}
		else {
			b.append ( prop.getKey() + "=\"" + prop.getValue() + "\"" );
		}
	}
	return b.toString();
}

/**
Unset a value (remove from the list).
Remove the property from the property list.
@param pos index (0+) for property to remove.
*/
public void unset ( int pos )
{   if ( pos >= 0 ) {
        __list.remove(pos);
    }
}

/**
Unset a value (remove from the list).
Remove the property from the property list.
@param key String key for property to remove.
*/
public void unSet ( String key )
{	int pos = findProp ( key );
	if ( pos >= 0 ) {
		__list.remove(pos);
	}
}

/**
Unsets all properties with matching keys from the list.
@param key String key for the properties to remove.
*/
public void unSetAll ( String key )
{	int count = getPropCount(key);
	for (int i = 0; i < count; i++) {
		unSet(key);
	}
}

/*
Checks all the property names in the PropList to make sure only valid and 
deprecated ones are in the list, and returns a list with warning messages
about deprecated and invalid properties.  Invalid properties ARE NOT removed.
See the overloaded method for more information.
*/
public List<String> validatePropNames(List<String> validProps, List<String> deprecatedProps,
    List<String> deprecatedNotes, String target ) 
throws Exception
{
    return validatePropNames( validProps, deprecatedProps, deprecatedNotes, target, false );
}

/**
Checks all the property names in the PropList to make sure only valid and 
deprecated ones are in the list, and returns a list with warning messages
about deprecated and invalid properties.
@param validProps a list of property names that are possible valid values.
Not all the values in this list need to be present in the PropList, but all
the property names in the PropList must be in this list to be considered valid.
<p>If any properties are found in the PropList that are not valid and not 
deprecated, the returned list will include a line in the format:<p>
<pre>	[name] is not a valid [target].</pre><p>
Where [name] is the name of the property and [target] is the target (see the
<b>target</b> parameter for more information on this).<p>
If this parameter is null it will be considered the same as a zero-size
list.  In both cases, all properties in the PropList will be flagged as invalid properties.
@param deprecatedProps an optional list of the names of properties that are
deprecated (i.e., in a transitional state between property names).  If any 
property names in this list are found in the PropList, the returned list
will contain a line in the format: <p>
<pre>	[name] is no longer recognized as a valid [target].  [note]</pre><p>
Where [name] is the name of the property, [target] is the target (see the 
<b>target</b> parameter in this method), and [note] is an optional note 
explaining the deprecation (see the next parameter).<p>
This list can be null if there are no deprecated properties.<p>
<b>Note:</b> The property names in this list must not include any of the
values in the deprecatedProps list, or else the property names will be 
considered valid -- and <b>not</b> checked for whether they are deprecated or not.
@param deprecatedNotes an optional list that accompanies the deprecatedProps
list and offers further information about the deprecation.<p>
If deprecatedProps is included, this list is optional.  However, if
this list is non-null, the deprecatedProps list must be non-null as well.<p>
The elements in this list are related to the elements in the deprecatedProps
list on a 1:1 basis.  That is, the data at element N in each list are 
the name of a deprecated property and a note explaining the deprecation.<p>
The note will be added to the deprecation warning lines as shown above in the
documentation for deprecatedProps.  For best formatting, the first character
of the note should be capitalized and it should include a period at the end of the note.
@param target an option String describing in more detail the kind of property
stored in the PropList.  The default value is "property."  As shown in the 
documentation above, target is used to offer further information about an invalid or deprecated prop.<p>
For example, if a PropList stores worksheet properties, this value could be set
to "JWorksheet property" so that the error messages would be:<p>
<pre>	PropertyName is no longer recognized as a valid JWorksheet property.
	PropertyName is not a valid JWorksheet property.</pre><p>
@param removeInvalid indicates whether invalid properties should be removed from the list.
@return null or a list containing Strings, each of which is a warning about 
an invalid or deprecated property in the PropList.  The order of the returned
list is that invalid properties are returned first, and deprecated properties
returned second.  If null is returned, no invalid or deprecated properties were found in the PropList.
@throws Exception if an error occurs.  Specifically, if deprecatedProps and
deprecatedNotes are non-null and the size of the lists is different, an 
Exception will be thrown warning of the error.
*/
public List<String> validatePropNames(List<String> validProps, List<String> deprecatedProps,
List<String> deprecatedNotes, String target, boolean removeInvalid ) 
throws Exception {
	// Get the sizes of the lists that will be iterated through, handling null lists gracefully.

	int validPropsSize = 0;
	if (validProps != null) {
		validPropsSize = validProps.size();
	}

	int deprecatedPropsSize = 0;
	if (deprecatedProps != null) {
		deprecatedPropsSize = deprecatedProps.size();
	}

	// The size of the deprecatedNotes list is computed in order to check
	// that its size is the same as the deprecatedProps size.

	boolean hasNotes = false;
	int deprecatedNotesSize = 0;
	if (deprecatedNotes != null) {
		deprecatedNotesSize = deprecatedNotes.size();
		hasNotes = true;
	}

	if (hasNotes && deprecatedPropsSize != deprecatedNotesSize) {
		throw new Exception("The number of deprecatedProps (" + deprecatedPropsSize + ") is not the same as the "
			+ "number of deprecatedNotes (" + deprecatedNotesSize + ")");
	}

	// Default the target value.

	if (target == null) {
		target = "property";
	}

	boolean valid = false;
	Prop p = null;
	String key = null;
	String msg = null;
	String val = null;
	List<String> warnings = new Vector();
	
	// Iterate through all the properties in the PropList and check for
	// whether they are valid, invalid, or deprecated.

	List<String> invalids = new Vector();
	List<String> deprecateds = new Vector();
	
	String removeInvalidString = "";
	if ( removeInvalid ) {
	    removeInvalidString = "  Removing invalid property.";
	}
	
	for (int i = 0; i < size(); i++) { // Check size dynamically in case props are removed below
		p = propAt(i);
		key = p.getKey();

		valid = false;

		// First make sure that the property is in the valid property name list.
		// Properties will only be checked for whether they are deprecated if they are not valid.

		for (int j = 0; j < validPropsSize; j++) {
			val = validProps.get(j);
			if (val.equalsIgnoreCase(key)) {
				valid = true;
				break;
			}
		}

		if (!valid) {
			// Only check to see if the property is in the deprecated list if it was not already found in 
			// the valid properties list.

			for (int j = 0; j < deprecatedPropsSize; j++) {
				val = deprecatedProps.get(j);
				if (val.equalsIgnoreCase(key)) {
					msg = "\"" + key + "\" is no longer recognized as a valid " + target + "." + removeInvalidString;
					if (hasNotes) {
						msg += "  " + deprecatedNotes.get(j);
					}
					deprecateds.add(msg);

					// Flag valid as true because otherwise this Property will also be reported
					// as an invalid property below, and that is not technically true.  Nor
					// is it technically true that the property is valid, strictly-speaking,
					// but this avoids double error messages for the same property.

					valid = true;
					break;
				}
			}
		}

		// Add the error message for invalid properties.  

		if (!valid) {
			invalids.add( "\"" + key + "\" is not a valid " + target + "." + removeInvalidString );
		}
		
		if ( !valid && removeInvalid ) {
		    this.unset(i);
		}
	}

	for (int i = 0; i < invalids.size(); i++) {
		warnings.add(invalids.get(i));
	}

	for (int i = 0; i < deprecateds.size(); i++) {
		warnings.add(deprecateds.get(i));
	}

	if (warnings.size() == 0) {
		return null;
	}
	else {
		return warnings;
	}
}

/**
Write the property list to a persistent medium (e.g., configuration file).
The original persistent name is used for the filename.
If the properties contain literals, then blocks will be determined from the literals and properties
with starting values that match the literal will be stripped of the leading literal.  For example,
App.Property = X will be written:
<pre>
[Block]
Property = X
</pre>
@exception IOException if there is an error writing the file.
*/
public void writePersistent ()
throws IOException
{	PrintWriter out;
	try {
	    out = new PrintWriter(new FileOutputStream (__persistentName ));
	} catch ( Exception e ) {
		String message = "Unable to open \"" + __persistentName + "\" to write PropList.";
		Message.printWarning ( 3, "PropList.writePersistent", message );
		throw new IOException ( message );
	}

	Prop p = null;
	String key = null, value = null;
	boolean gotSpace;
	char c;
	int size = size();
	int length = 0;
	String blockName = null;
	for ( int i=0; i<size; i++ ) {
		p = propAt(i);
		value = p.getValue(this);
		if ( p.getIsLiteral() ) {
		    // TODO SAM 2008-11-21 Decide if there needs to be a parameter to control whether these
		    // are written.  Presumably if they are read from a file they will typically be rewritten to the file.
		    // A literal string.  Just print it.
		    out.println( value.toString() );
		    // See if inside a block
		    if ( value.startsWith("[") ) {
		        // Have a [Block] in the properties so keep track of it
		        value = value.trim();
		        blockName = value.trim().substring(1,value.length() - 1);
		    }
		}
		else {
		    // A normal property.  Format it as Property = Value
    		key = p.getKey();
    		if ( (blockName != null) && key.startsWith(blockName + ".") ) {
    		    // A block name has been detected.  Strip from the key so that the block name is not redundant.
    		    key = key.substring(blockName.length() + 1); // +1 is for "."
    		}
    		// Do any special character encoding
    		gotSpace = false;
    		length = value.length();
    		for ( int j=0; j<length; j++ ) {
    			c = value.charAt(j);		
    			if ( c == ' ' ) {
    				gotSpace = true;
    			}
    		}
    
    		if ( gotSpace ) {
    			out.println( key + " = \"" + value.toString() + "\"" );
    		}
    		else {
    		    out.println( key + " = " + value.toString());
    		}
		}
	}
	out.close();
}

}