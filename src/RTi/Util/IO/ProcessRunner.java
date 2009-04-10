package RTi.Util.IO;

import java.util.List;

/**
This interface indicates that a class runs one or more processes and allows the process instances to be retrieved.
One use is to allow a program to kill processes if they are misbehaving.
*/
public interface ProcessRunner {

/**
Return the list of processes being managed by an object.
@return the list of processes being managed by an object.
*/
public List<Process> getProcessList ();

}