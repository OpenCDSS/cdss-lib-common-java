package RTi.Util.IO;

/**
 * Simple count of warnings, intended to be used with utility code that may generate warnings.
 * Could use a MutableInteger class if that were available.
 * @author sam
 *
 */
public class WarningCount extends Object
{
    
/**
The number of warnings that are detected.
*/
private int __warningCount = 0;
    
/**
Constructor.
*/
public WarningCount ()
{   super();
}

/**
Return the warning count.
*/
public int getCount()
{
    return __warningCount;
}

/**
Increment the warning count by 1.
*/
public int incrementCount ()
{
    ++__warningCount;
    return __warningCount;
}

/**
Increment the warning count.
@param count the increment to the previous count
@return the count after incrementing
*/
public int incrementCount ( int count )
{
    __warningCount += count;
    return __warningCount;
}

}