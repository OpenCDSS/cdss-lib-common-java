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
Class __missingObjectClass = null;

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
public MissingObjectEvent ( String missingObjectID, Class missingObjectClass, String missingObjectClassName, Object resource )
{
    __id = missingObjectID;
    __resource = resource;
    __missingObjectClass = missingObjectClass;
    __missingObjectClassName = missingObjectClassName;
}

public Class getMissingObjectClass ()
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
