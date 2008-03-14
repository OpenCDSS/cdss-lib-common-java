package RTi.Util.IO;

/**
Filter output from the process that is being run by the ProcessManager.
This is useful when an external process produces a large amount of output
that would normally slow down the 
*/
public interface ProcessManagerOutputFilter
{
	/**
	 * Filter the program output that is processed by the ProcessManager, for
	 * example to limit the amount of output that is being displayed by the
	 * ProcessManagerJDialog.
	 * @param line Line to evaluate.
	 * @return the String to output, or null if the line should not be output.
	 */
	public String filterOutput ( String line );
}
