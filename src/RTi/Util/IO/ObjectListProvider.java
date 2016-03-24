package RTi.Util.IO;

import java.util.List;

/**
This interface defines behavior to return a list of objects of a certain class type.
This is useful, for example, if some class maintains a list of objects and those
objects need to be listed in some other code, for example in a UI choice.
*/
public interface ObjectListProvider
{

    /**
     * Return a List of objects of the requested class type.
     * @return a List of objects of the requested class type, or null if no objects of
     * the requested type are available.
     * @param c Class to return.
     */
    public List<? extends Object> getObjectList ( Class c );
}
