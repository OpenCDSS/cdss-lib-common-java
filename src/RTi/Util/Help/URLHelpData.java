// URLHelpData - a single data item for on-line help class

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

//------------------------------------------------------------------------------
// URLHelpData - a single data item for on-line help class
//------------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 28 Jan 1998	Steven A. Malers,	Created initial version.
//		RTi
// 2001-11-14	SAM, RTi		Clean up javadoc.  Add finalize().
//					Set variables to null when no longer
//					used.
//------------------------------------------------------------------------------

package RTi.Util.Help;

import java.lang.String;

/**
This class stores one help index item, for use by URLHelp.  The item consists
of a key, a topic description, and a URL.  The
URLHelp.readIndex() function describes the index file format for help items.
@see URLHelp
*/
public class URLHelpData
{

/**
Key used to look up help index information.
*/
protected String _key;

/**
String to display in a help index (descriptive phrase).
*/
protected String _topic;

/**
URL corresponding to the key and topic.
*/
protected String _URL;

/**
Construct and set data to empty strings.
*/
public URLHelpData ()
{	initialize();
}

/**
Construct using the data members.
@param key The key for the help index item.
@param topic The topic for the help index item.
@param URL The URL for the help index item.
*/
public URLHelpData ( String key, String topic, String URL )
{	initialize();
	setKey ( key );
	setTopic ( topic );
	setURL ( URL );
}

/**
Clean up for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	_key = null;
	_topic = null;
	_URL = null;
	super.finalize();
}

/**
Return the key for the help index item.
@return The key for the help index item.
*/
public String getKey ()
{	return _key;
}

/**
Return the topic for the help index item.
@return The topic for the help index item.
*/
public String getTopic ()
{	return _topic;
}

/**
Return the URL for the help index item.
@return The URL for the help index item.
*/
public String getURL ()
{	return _URL;
}

/**
Initialize the data.
*/
private void initialize ()
{	_key = "";
	_topic = "";
	_URL = "";
}

/**
Set the string key used to look up help.
@param key The key to use for the help index item.
*/
public void setKey ( String key )
{	if ( key != null ) {
		_key = key;
	}
}

/**
Set the topic used for the help data item.
@param topic The topic to use for the help index item.
*/
public void setTopic ( String topic )
{	if ( topic != null ) {
		_topic = topic;
	}
}

/**
Set the URL to use for the help data item.
@param URL The URL to use for the help index item.
*/
public void setURL ( String URL )
{	if ( URL != null ) {
		_URL = URL;
	}
}

/**
Convert to a string representation.
@return The string representation of the help index item.
*/
public String toString ()
{	return "key:\"" + _key + "\" URL:\"" + _URL +
		"\" topic:\"" + _topic + "\"";
}

} // End URLHelpData
