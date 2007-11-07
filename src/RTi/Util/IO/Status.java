/*****************************************************************************
	Status.java		2007-03-22
******************************************************************************  
Revisions
2007-03-21	Ian Schneider, RTi		Initial version.
2007-03-27	Kurt Tometich, RTi		Added some javadoc.
*****************************************************************************/
package RTi.Util.IO;

/**
Status encapsulates a message and a level.  Can be used to log
various levels of errors or warnings.  It usually reflects the Status of
a data check or data validation rule, but can be used for other situations
that require some type of Status.
**/
public final class Status {
    
    public static final int ERROR = 0;
    public static final int WARNING = 1;
    public static final int OK = 2;
    
    // A Status object with status of OK
    public static final Status OKAY = new Status("OK",OK);
    
    // Message to add to status is it fails
    private final String message;
    // Level to log the failure as
    private final int level;
    
    /**
     Initializes the Status object with its message and
     level.
     @param message The message to add if this test fails.
     @param level The level to log the failure as.
     */
    private Status(String message,int level) {
        this.message = message;
        this.level = level;
    }
    
    /**
     Returns the current level.
     @return The level of error logging.
     */
    public int getLevel() {
        return level;
    }
    
    /**
     Returns a new Status object with the given message and level.
     This should be used when there is a failure to return a status
     object that details the failure.
     @param message Message to add to the Status object (i.e. "Object
     must not be null").
     param level The level to the log the message as.
     @return Status object.
     */
    public static Status status(String message,int level) {
        return new Status(message,level);
    }
    
    /**
     Helper method that logs and error with the given message
     and returns the Status object.
     @param message The message to log for this error.
     @return Status object.
     */
    public static Status error(String message) {
        return status(message,ERROR);
    }
    
    /**
     Helper method that logs a warning with the given message
     and returns the Status object.
     @param message The message to log for this warning.
     @return Status object.
     */
    public static Status warning(String message) {
        return status(message,WARNING);
    }
    
    /**
     Overrides the toString method in Object and returns
     the message for this Status object.
     @return The message for this Status object.
     */
    public String toString() {
        return message;
    }
    
}
