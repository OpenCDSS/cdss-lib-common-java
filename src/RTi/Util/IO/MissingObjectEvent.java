// MissingObjectEvent - this event describes missing object errors, for example when an object is requested but cannot be read.

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

package RTi.Util.IO;

/**
 * This event describes missing object errors, for example when an object is requested but cannot
 * be read.
 * @author sam
 *
 */
public class MissingObjectEvent extends CommandProcessorEvent
{
    
/**
 * Identifier of object that was missing.
 */
String __id = "";

/**
 * Class of object that was missing.
 */
Class<?> __missingObjectClass = null;

/**
 * Human readable name for class of object that was missing.
 */
String __missingObjectClassName = "";

/**
 * Object that was the source/domain of the problem.
 */
Object __resource = null;

/**
 * Construct an instance indicating that an object could not be read/created.
 * @param missingObjectID unique identifier for the object.
 * @param resource the object that is the "domain" of the problem.
 * @param missingObjectClass the class that for the missing object.
 * @param missingObjectClassName the human-readable name of the class (e.g., "Time Series" as
 * opposed to "RTi.TS.TS".
 */
public MissingObjectEvent ( String missingObjectID, Class<?> missingObjectClass, String missingObjectClassName, Object resource )
{
    __id = missingObjectID;
    __resource = resource;
    __missingObjectClass = missingObjectClass;
    __missingObjectClassName = missingObjectClassName;
}

public Class<?> getMissingObjectClass ()
{
    return __missingObjectClass;
}

public String getMissingObjectClassName ()
{
    return __missingObjectClassName;
}

public String getMissingObjectID ()
{
    return __id;
}

public Object getResource ()
{
    return __resource;
}

}
