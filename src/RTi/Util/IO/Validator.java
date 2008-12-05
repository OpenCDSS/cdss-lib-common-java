/*****************************************************************************
Validators.java - 2007-03-26
******************************************************************************
Revisions
2007-03-21	Ian Schneider, RTi		Initial Version.
*****************************************************************************/
package RTi.Util.IO;
/**
 * A Validator determines whether some state is valid or not by returning a Status object.
 */
public interface Validator {
  
    Status validate(Object value);
    
}