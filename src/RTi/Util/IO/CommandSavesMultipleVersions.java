package RTi.Util.IO;

/**
Implementation of this interface indicates that a command can save multiple versions.  This is used
in transitionary code, for example to handle the older TSTool "TS Alias = " notation,
which is being phased out in favor of simple parameter=value notation.
*/
public interface CommandSavesMultipleVersions
{

/**
Return the string representation of the command, considering the major software version.
@param parameters the command parameters
@param majorVersion the major version of the application/processor (e.g., version 10 for TSTool
no longer uses "TS Alias = " notation.
*/
public String toString ( PropList parameters, int majorVersion );

}