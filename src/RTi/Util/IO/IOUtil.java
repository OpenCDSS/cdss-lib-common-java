// IOUtil - this class provides static functions for file input/output

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

import java.applet.Applet;
import java.applet.AppletContext;

import java.awt.Desktop;
import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.lang.Math;
import java.lang.String;
import java.lang.StringBuffer;
import java.lang.System;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeUtil;

/**
This class provides static functions for file input/output and also provides
global functionality that may be useful in any program.
The class provides useful functionality in addition to the Java System, IO, and security classes.
A PropListManager is used to manage a global, unnamed PropList in conjunction with other PropLists.
To make the best use of this class, initialize from the main() or init() functions, as follows:
<p>

<pre>
// Called if an applet...
public static final String PROGNAME = "myprog";
public static final String PROGVER = "1.2 (12 Mar 1998)";

public void init ()
{	IOUtil.setApplet ( this );
    IOUtil.setProgramData ( PROGNAME, PROGVER, null );
}

// Called if stand-alone...
public static void main ( String argv )
{	// The default is not an applet.
    IOUtil.setProgramData ( PROGNAME, PROGVER, argv );
}
</pre>
*/
public abstract class IOUtil {

// Global data.

// TODO SAM 2009-05-06 Evaluate whether needed.
/**
Flags use to indicate the vendor
*/
public final static int SUN = 1;
public final static int MICROSOFT = 2;
public final static int UNKNOWN = 3;

/**
String to use to indicate a file header revision line.
@see #getFileHeader
*/
protected static final String HEADER_REVISION_STRING = "HeaderRevision";

/**
String used to indicate comments in files (unless otherwise indicated).
*/
protected static final String UNIVERSAL_COMMENT_STRING = "#";

/**
Command-line arguments, guaranteed to be non-null but may be empty.
*/
private static String _argv[] = new String[0];

/**
Applet, null if not an applet.
This is typically not used because applets hav been phased out due to security risk.
*/
private static Applet _applet = null;

/**
Applet context.  Call setAppletContext() from init() of an application that uses this class.
This is typically not used because applets hav been phased out due to security risk.
*/
private static AppletContext _applet_context = null;

/**
Document base for the applet.
This is typically not used because applets hav been phased out due to security risk.
*/
private static URL _document_base = null;

/**
Program command file.
TODO SAM (2009-05-06) Evaluate phasing out since command file is managed with processor, not program.
*/
private static String _command_file ="";

/**
Program command list.
TODO SAM (2009-05-06) Evaluate phasing out since command file is managed with processor, not program.
*/
private static List<String> _command_list = null;

/**
Host (computer) running the program.
*/
private static String _host = "";

/**
Program name, as it should appear in title bars, Help About, etc.
*/
private static String _progname = "";

/**
Program version, typically a semantic version like "1.2.3" or "1.2.3.beta".
*/
private static String _progver = "";

/**
Program user.
*/
private static String _user = "";

/**
Indicates whether a test run (not used much anymore) - can be used for experimental features that are buried
in the code base.
*/
private static boolean _testing = false;

/**
Program working directory, which is virtual and used to create absolute paths to files.
This is needed because the application cannot change the current working directory due to security checks.
*/
private static String _working_dir = "";

/**
Indicates whether global data are initialized.
*/
private static boolean _initialized = false;

/**
Indicate whether the program is running as an applet.
*/
private static boolean _is_applet = false;

/**
Indicates whether the program is running in batch (non-interactive) or interactive GUI/shell.
*/
private static boolean _is_batch = false;

/**
A property list manager that can be used globally in the application.
*/
private static PropListManager _prop_list_manager = null;

/**
TODO SAM 2009-05-06 Seems to be redundant with _is_applet.
*/
private static boolean __runningApplet = false;

/**
Home directory for the application, typically the installation location (e.g., C:\Program Files\Company\AppName).
*/
private static String __homeDir = null;

/**
 * List of classpath items (jar files and folders with wildcards).
 * This is used, for example, to allow code to run a separate Java program using the application's startup environment.
 */
private static List<String> applicationPluginClasspathList = new ArrayList<>();

/**
Add a PropList to the list managed by the IOUtil PropListManager.
@param proplist PropList to add to the list managed by the PropListManager.
@param replace_if_match If the name of the PropList matches one that is already
in the list, replace it (true), or add the new list additionally (false).
*/
public static void addPropList ( PropList proplist, boolean replace_if_match ) {
	if ( !_initialized ) {
		initialize();
	}
	_prop_list_manager.addList ( proplist, replace_if_match );
}

/**
Adjust an existing path.  This can be used, for example to navigate up an absolute path by a relative change.
The resulting path is returned.  Rules for adjustment are as follows:
<ol>
<li>	If the adjustment is an absolute path, the returned path is the same as the adjustment.
	</li>
<li>	If the adjustment is a relative path (e.g., "..", "../something",
	"something", "something/something2"), the initial path is adjusted by
	removing redundant path information if possible.
	</li>
<li>	If the adjustment is a relative path that cannot be applied, an exception is thrown.
	</li>
<li>	The returned path will not have the file separator unless the path is the root directory.
	</li>
</ol>
No check for path existence is made.
@return the original path adjusted by the adjustment, with no path separator at the end.
@param initialPath Original path to adjust.
@param adjustment Adjustment to the path to apply (e.g., "..", or a file/folder name).
@exception Exception if the path cannot be adjusted.
*/
public static String adjustPath ( String initialPath, String adjustment )
throws Exception {
	File a = new File ( adjustment );
	// If the adjustment is blank, return the initial path.
	// Do not trim the adjustment because it is possible that a filename has only spaces.
	if ( (adjustment == null) || (adjustment.length() == 0) ) {
		return initialPath;
	}
	if ( a.isAbsolute() ) {
		// Adjustment is absolute so make the adjustment.
		return adjustment;
	}
	// The adjustment is relative.  First make sure the initial path ends in a file separator.
	StringBuffer buffer = new StringBuffer ( initialPath );
	char filesep = File.separator.charAt(0);
	if ( initialPath.charAt(initialPath.length() - 1) != filesep ) {
		buffer.append ( filesep );
	}
	// Loop through the adjustment.  For every ".." that is encountered, remove one directory from "buffer".
	int length = adjustment.length();
	String upOne = "..";
	for ( int i = 0; i < length; i++ ) {
		if ( adjustment.indexOf(upOne,i) == i ) {
			// The next part of the string has "..".  Move up one level in the initial string.
		    // The buffer will have a separator at the end so need to skip over it at the start.
			for ( int j = buffer.length() - 2; j >= 0; j-- ) {
				if ( buffer.charAt(j) == filesep ) {
					// Found the previous separator.
					buffer.setLength(j + 1);
					break;
				}
			}
			// Increment in the adjustment.
			i += 2;	// Loop increment will go past the separator.
		}
		else if ( adjustment.indexOf("..",i) == i ) {
			// Need to go up one directory.
		}
		else if ( adjustment.charAt(i) == '.' ) {
			// If the next character is a separator (or at the end of the string),
			// ignore this part of the path (since it references the current directory.
			if ( i == (length - 1) ) {
				// Done processing.
				break;
			}
			else if ( adjustment.charAt(i + 1) == filesep ) {
				// Skip.
				++i;
				continue;
			}
			else {
			    // A normal "." for a file extension so add it.
				buffer.append ( '.' );
			}
		}
		else {
		    // Add the characters to the adjusted path.
			buffer.append ( adjustment.charAt(i) );
		}
	}
	// Remove the trailing separator, but only if not the root directory.
	if ( (buffer.charAt(buffer.length() - 1) == filesep) && !buffer.toString().equals("" + filesep) ) {
		// Remove the trailing file separator.
		buffer.setLength(buffer.length() - 1);
	}
	return buffer.toString();
}

/**
Tries to manually load a class into memory in order to see if it is available.
Normally, class-loading is done by the Java Virtual Machine when a class is first used in code,
but this method can be used to load a class at any time,
and more importantly, to check whether a class is available to the virtual machine.<p>
Classes may be unavailable because of a difference in program versions,
or because they were intentionally left out of a jar file in order to limit the functionality of an application.<p>
The virtual machine will look through the entire class path when it tries to load the given class.
@param className the fully-qualified class name (including package) of the class to try loading.  Examples:<p>
- RTi.Util.GUI.JWorksheet<p>
- java.util.Vector<p>
- DWR.DMI.HydroBaseDMI.HydroBase_StructureView
@return true if the class could be loaded, false if not.
*/
public static boolean classCanBeLoaded(String className) {
	try {
		Class.forName(className);
	}
	catch (ClassNotFoundException cnfe) {
		return false;
	}
	return true;
}

/**
 * Copy a directory.
 * Based on:  https://www.baeldung.com/java-copy-directory
 * @param sourceDirectory source directory to be copied
 * @param destinationDirectory destination directory to create
 * @param problems a list of problems copying, which allows copying attempts to occur without first error throwing an exception
 */
public static void copyDirectory ( String sourceDirectory, String destinationDirectory, List<String> problems )
throws IOException {
    Files.walk(Paths.get(sourceDirectory)).forEach(source -> {
        Path destination = Paths.get(
        	destinationDirectory,
        	source.toString().substring(sourceDirectory.length()));
        try {
        	// Copy each file (or create an empty folder).
            Files.copy(source, destination);
        }
        catch (IOException e) {
        	String message = "Error copying folder \"" + source + "\" to \"" + destination + "\".";
        	Message.printWarning(3,"IOUtil.copyDirectory", message );
        	problems.add(message);
        }
    });
}

// TODO smalers 2021-08-26 replace with Java NIO Files class call.
/**
Copies a file from one file to another.
@param source the source file to copy.
@param dest the destination file to copy the first file to.
@throws IOException if there is an error copying the file.
*/
public static void copyFile(File source, File dest)
throws IOException {
	FileInputStream fis = null;
	FileOutputStream fos = null;
	try {
		fis = new FileInputStream(source);
		fos = new FileOutputStream(dest);
		byte[] buf = new byte[1024];
		int i = 0;
		while((i = fis.read(buf)) != -1) {
			fos.write(buf, 0, i);
		}
	}
	finally {
		if ( fis != null ) {
			fis.close();
		}
		if ( fos != null ) {
			fos.close();
		}
	}
}

/**
Copies the specified String to the system clipboard.
@param string the String to copy to the clipboard.
*/
public static void copyToClipboard(String string) {
	StringSelection stsel = new StringSelection(string);
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	clipboard.setContents(stsel, stsel);
}

/**
 * Delete a directory.
 * From:  https://www.baeldung.com/java-delete-directory
 * @param directory directory to be deleted
 * @return true if delete was successful, false if not
 */
public static boolean deleteDirectory ( File directory ) {
	if ( directory.exists() ) {
	    File[] allContents = directory.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
	    return directory.delete();
	}
	return false;
}

/**
Enforce a file extension using a list of accepted extensions.
For example, if a file chooser is used but the file extension is not added by the chooser.
For example, if the file name is "file" and the extension is "zzz",
then the returned value will be "file.zzz".
If the file is "file.zzz", the returned value will be the same (no change).
There is currently no sophistication to handle input file names with multiple
extensions that are different from the requested extension.
@param filename The file name on which to enforce the extension.
@param extensions The file extension to enforce, without the leading ".".
All extensions are checked (case sensitive).  The first matched is used.
If none match, the first extension in the list is applied.
@return the filename with enforced extension.
*/
public static String enforceFileExtension ( String filename, List<String> extensions ) {
	for ( String extension : extensions ) {
		if ( filename.endsWith("." + extension) ) {
			// Found a match so use filename as is.
			return filename;
		}
	}
	// If here the extension did not match so use the first extension.
	return filename + "." + extensions.get(0);
}


/**
Enforce a file extension.  For example, if a file chooser is used but the file
extension is not added by the chooser.  For example, if the file name is
"file" and the extension is "zzz", then the returned value will be "file.zzz".
If the file is "file.zzz", the returned value will be the same (no change).
Used the overloaded method to handle input file names with multiple extensions.
@param filename The file name on which to enforce the extension.
@param extension The file extension to enforce, without the leading ".".
@return the filename with enforced extension.
*/
public static String enforceFileExtension ( String filename, String extension ) {
	if ( StringUtil.endsWithIgnoreCase(filename, "." + extension) ) {
		// Found a match so use filename as is.
		return filename;
	}
	else {
	    return filename + "." + extension;
	}
}

/**
Expand a configuration string property value using environment and Java runtime environment variables.
If the string is prefixed with "Env:" then the string will be replaced with the environment variable of the matching name.
If the string is prefixed with "SysProp:" then the string will be replaced with
the JRE runtime system property of the same name.
Comparisons are case-sensitive and if a match is not found the original string will be returned.
@param propName name of the property, used for messaging
@param propValue the string property value to expand
@return expanded property value
*/
public static String expandPropertyForEnvironment ( String propName, String propValue ) {
    if ( propValue == null ) {
        return null;
    }
    int pos = StringUtil.indexOfIgnoreCase(propValue,"Env:",0);
    if ( (pos == 0) && (propValue.length() > 4) ) {
        String env = System.getenv(propValue.substring(4));
        if ( env != null ) {
            return env;
        }
        else {
            return propValue;
        }
    }
    pos = StringUtil.indexOfIgnoreCase(propValue,"SysProp:",0);
    if ( (pos == 0) && (propValue.length() > 8) ) {
        String sys = System.getProperty(propValue.substring(8));
        if ( sys != null ) {
            return sys;
        }
        else {
            return propValue;
        }
    }
    if ( propValue.equalsIgnoreCase("Prompt") ) {
        // Prompt for the value.
        System.out.print("Enter value for \"" + propName + "\": " );
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            propValue = in.readLine().trim();
        }
        catch ( IOException e ) {
            propValue = "";
        }
    }
    // No special case so return the original value.
    return propValue;
}

/**
Determine if a file/directory exists.
@return true if the file/directory exists, false if not.
@param filename String path to the file/directory to check.
*/
public static boolean fileExists ( String filename ) {
	if ( filename == null ) {
		return false;
	}
	File file = new File(filename);
	boolean exists = file.exists();
	file = null;
	return exists;
}

/**
Determine if a file/directory is readable.
@return true if the file/directory is readable, false if not.
@param filename String path to the file/directory to check.
*/
public static boolean fileReadable ( String filename ) {
	if ( filename == null ) {
		return false;
	}
	InputStream st = null;
	try {
	    st = IOUtil.getInputStream( filename);
	}
	catch ( Exception e ) {
		st = null;
		return false;
	}
	if ( st != null ) {
		try {
		    st.close();
		}
		catch ( Exception e ) {
		}
		st = null;
		return true;
	}
	return false;
	// This only works with files.  The above works with URLs.
	//File file = new File(filename);
	//boolean canread = file.canRead();
	//file = null;
	//return canread;
}

/**
 * Read a file into a StringBuilder, including retaining the existing newlines.
 * This ensures that if the text is modified and written,
 * the file will have the same line endings as the original.
 * @param filename Name of file to read.
 * @return StringBuilder containing the characters in the file.
 */
public static StringBuilder fileToStringBuilder ( String filename )
throws FileNotFoundException, IOException {
	StringBuilder sb = new StringBuilder();

	BufferedReader reader = null;
	try {
		reader = new BufferedReader(new FileReader(filename) );
		int bufferLen = 512;
		char [] array = new char[bufferLen];
		int nchars;
		while ((nchars = reader.read(array,0,bufferLen)) > -1 ) {
	  		sb.append(array,0,nchars);
	   	}
	}
	finally {
		if ( reader != null ) {
			reader.close();
		}
	}
	return sb;
}

/**
Read in a file and store it in a string list (list of String).
@param filename	File to read and convert to string list.
@return a list of String read from the file, with newlines removed.
@exception IOException if there is an error.
*/
public static List<String> fileToStringList ( String filename ) throws IOException {
	return fileToStringList ( filename, -1 );
}

/**
Read in a file and store it in a string list (list of String).
@param filename	File to read and convert to string list.
@param maxLines the maximum number of lines to read (negative to ignore)
@return a list of String read from the file, with newlines removed.
@exception IOException if there is an error.
*/
public static List<String> fileToStringList ( String filename, int maxLines )
throws IOException {
	List<String> list = null;
	String message, routine = IOUtil.class.getSimpleName() + ".fileToStringList", tempstr;

	if ( filename == null ) {
		message = "Filename is NULL";
		Message.printWarning ( 10, routine, message );
		throw new IOException ( message );
	}
	if ( filename.length() == 0 ) {
		message = "Filename is empty";
		Message.printWarning ( 10, routine, message );
		throw new IOException ( message );
	}

	// Open the file.

	if ( Message.isDebugOn ) {
		Message.printDebug ( 30, routine, "Breaking file \"" + filename + "\" into string list" );
	}
	BufferedReader fp = null;
	try {
	    fp = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream( filename) ));
	}
	catch ( Exception e ) {
		message = "Unable to read file \"" + filename + "\" (" + e + ").";
		Message.printWarning ( 3, routine, message );
		throw new IOException ( message );
	}

	try {
		list = new ArrayList<>(50);
		int lineCount = 0;
		while ( true ) {
			tempstr = fp.readLine();
			++lineCount;
			if ( (maxLines > 0) && (lineCount > maxLines) ) {
				break;
			}
			if ( tempstr == null ) {
				break;
			}
			tempstr = StringUtil.removeNewline ( tempstr );
			list.add (tempstr );
		}
	}
	finally {
		fp.close ();
	}
	return list;
}

/**
Determine if a file is writeable.  False is returned if the file does not exist.
@return true if the file is writeable, false if not.  The file must exist.
@param filename String path to the file to check.
*/
public static boolean fileWriteable ( String filename ) {
	if ( filename == null ) {
		return false;
	}
	File file = new File(filename);
	boolean canwrite = file.canWrite();
	file = null;
	return canwrite;
}

/**
 * Find the first executable program in the PATH.
 * @param program name of program to find in the PATH, will be exactly matched
 * so adjust for the operating system before calling.
 * On Windows, extensions "exe", "bat", and "cmd" are also tested.
 * Full or relative paths are not handled.
 * @return the File corresponding to the found program, or null if the program is not found in the PATH.
 */
public static File findProgramInPath ( String program ) {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = IOUtil.class.getSimpleName() + ".findProgamInPath";
	}
	String path = System.getenv("PATH");
	Message.printStatus(2, "", "PATH=" + path );
	if ( path == null ) {
		return null;
	}
	// Split the path based on the operating system.
	String [] parts = path.split(File.pathSeparator);
	for ( int i = 0; i < parts.length; i++ ) {
		File f = new File(parts[i] + File.separator + program);
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, "Checking file \"" + f.getAbsolutePath() + "\"" );
		}
		if ( f.exists() && f.canExecute() ) {
			return f;
		}
		if ( !IOUtil.isUNIXMachine() ) {
			// Also check common extensions on Windows until a match is found.
			String [] extensions = { "exe", "bat", "cmd" };
			for ( String extension : extensions ) {
				f = new File(parts[i] + File.separator + program + "." + extension );
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "Checking file \"" + f.getAbsolutePath() + "\"" );
				}
				if ( f.exists() && f.canExecute() ) {
					return f;
				}
			}
		}
	}
	return null;
}

/**
Format a header for a file, useful to understand the file creation.  The header looks like the following.
Trailing spaces are avoided because TSTool trims lines when reading command files and differences
from the source file will be indicated as a modified file.
This is mainly an issue for regression testing and other cases where TSTool is used to create a command file
that is run in a later step.
<p>
<pre>
# File generated by...
# program:   TSTool 14.0.1 (2021-10-15)
# user:      sam
# date:      Mon Jun 26 14:49:18 MDT 1995
# host:      white
# directory: /crdss/dmiutils/demandts/data
# command:   ../src/demandts -d1 -w1,10 -demands -istatemod
#            /crdss/statemod/data/white/white.ddh -icu
#            /crdss/statemod/data/white/white.ddc -sstatemod
#            /crdss/statemod/data/white/white.dds -eff12
</pre>
<p>
@param commentLinePrefix The string to use for the start of comment lines (e.g., "#").
Use blank if the prefix character will be added by calling code.
@param maxWidth The maximum length of a line of output (if whitespace is embedded in the header information,
lines will be broken appropriately to fit within the specified length.
@param isXML Indicates whether the comments are being formatted for an XML file.
XML files must be handled specifically because some characters that may be printed
to the header may not be handled by the XML parser.
The opening and closing XML tags must be added before and after calling this method.
@return the list of formatted header strings, guaranteed to be non-null
*/
public static List<String> formatCreatorHeader ( String commentLinePrefix, int maxWidth, boolean isXml ) {
    int commentLen, i, leftBorder = 12, len;

    if ( !_initialized ) {
        // Need to initialize the class static data.
        initialize ();
    }

    String now = TimeUtil.getSystemTimeString ( "" );

    // Make sure that a valid comment string is used.

    if ( commentLinePrefix == null ) {
        commentLinePrefix = "";
    }
    String commentLinePrefix2 = "";
    if ( !commentLinePrefix.equals("") ) {
        // Add a space to the end of the prefix:
    	// - avoids comments smashed against the line prefix
    	// - this helps with readability
    	// - however; be careful that strings have no trailing spaces,
    	//   which will be trimmed by TSTool and may show as a modified command file
        commentLinePrefix2 = commentLinePrefix + " ";
    }
    commentLen = commentLinePrefix2.length();

    // Format the comment string for the command line printout.

    StringBuffer commentSpace0 = new StringBuffer ( commentLinePrefix2 );
    for ( i = 0; i < leftBorder; i++ ) {
        commentSpace0.append(" ");
    }
    String commentSpace = commentSpace0.toString();
    commentSpace0 = null;

    List<String> comments = new ArrayList<>();
    comments.add(commentLinePrefix2 + "File generated by...");
    if ( _progver.isEmpty() ) {
      comments.add(commentLinePrefix2 + "program:      " + _progname);
    }
    else {
      comments.add(commentLinePrefix2 + "program:      " + _progname + " " + _progver);
    }
    comments.add(commentLinePrefix2 + "user:         " + _user);
    comments.add(commentLinePrefix2 + "date:         " + now);
    comments.add(commentLinePrefix2 + "host:         " + _host);
    comments.add(commentLinePrefix2 + "directory:    " + _working_dir);
    comments.add(commentLinePrefix2 + "command line: " + _progname.trim());
    int column0 = commentLen + leftBorder + _progname.length() + 1;
    int column  = column0;  // Column position, starting at 1.
    StringBuffer b = new StringBuffer(commentLinePrefix2);
    if ( _argv != null ) {
        for ( i = 0; i < _argv.length; i++ ) {
            len = _argv[i].length();
            // Need 1 to account for blank between arguments.
            if ( (column + 1 + len) > maxWidth ) {
                // Put the argument on a new line.
                comments.add ( b.toString() );
                b.setLength(0);
                b.append(commentLinePrefix2);
                b.append ( commentSpace + _argv[i] );
                column = column0 + len;
            }
            else {
                // Put the argument on the same line.
                b.append ( " " + _argv[i] );
                column += (len + 1);
            }
        }
    }
    comments.add(b.toString().trim());
    if ( _command_list != null ) {
        // Print the command list contents.
        if ( isXml ) {
            comments.add ( commentLinePrefix );
        }
        else {
            comments.add ( commentLinePrefix2 + "-----------------------------------------------------------------------" );
        }
        if ( fileReadable(_command_file) ) {
            comments.add ( commentLinePrefix2 + "Last command file: \"" + _command_file + "\"" );
        }
        comments.add ( commentLinePrefix );
        comments.add ( commentLinePrefix2 + "Commands used to generate output:" );
        comments.add ( commentLinePrefix );
        int size = _command_list.size();
        for ( i = 0; i < size; i++ ) {
            comments.add ( commentLinePrefix2 + _command_list.get(i) );
        }
    }
    else if ( fileReadable(_command_file) ) {
        // Print the command file contents.
        if ( isXml ) {
            comments.add ( commentLinePrefix );
        }
        else {
            comments.add ( commentLinePrefix2 + "-----------------------------------------------------------------------" );
        }
        comments.add ( commentLinePrefix2 + "Command file \"" + _command_file + "\":" );
        comments.add ( commentLinePrefix );
        boolean error = false;
        BufferedReader cfp = null;
        FileReader file = null;
        try {
            file = new FileReader ( _command_file );
            cfp = new BufferedReader ( file );
        }
        catch ( Exception e ) {
            error = true;
        }
        if ( !error ) {
            String string;
            while ( true ) {
                try {
                    string = cfp.readLine ();
                    if ( string == null ) {
                        break;
                    }
                }
                catch ( Exception e ) {
                    // End of file.
                    break;
                }
                comments.add ( commentLinePrefix2 + " " + string.trim() );
            }
        }
    }
    return comments;
}

/**
Get the Applet.
@return The Applet instance set with setApplet().
@see #setApplet
*/
public static Applet getApplet ( ) {
	return _applet;
}

/**
Get the AppletContext.
@return The AppletContext instance set with setAppletContext.
@see #setAppletContext
*/
public static AppletContext getAppletContext ( ) {
	return _applet_context;
}

/**
Returns the application home directory.  This is the directory from which log files,
configuration files, documentation etc, can be located.
Normally it is the installation home (e.g., C:\Program Files\RTi\TSTool-Version).
@return the application home directory.
*/
public static String getApplicationHomeDir() {
	return __homeDir;
}

/**
 * Get the list of application plugin classpath folders.
 * @return the list of application plugin classpath folders.
 */
public static List<String> getApplicationPluginClasspath () {
	return IOUtil.applicationPluginClasspathList;
}

/**
Get the document base.
@return The DocumentBase instance set when setApplet() is called.
@see #setApplet
*/
public static URL getDocumentBase ( ) {
	return _document_base;
}

/**
Return the drive letter for a path.
@return the drive letter for a path (e.g., "C:") or return an empty string if
no drive is found it the start of the path.
@deprecated come back to this to resolve UNC issues.  TODO JTS - 2006-02-16
*/
@Deprecated
public static String getDrive ( String path ) {
	if ( isUNIXMachine() ) {
		return "";
	}
	// Assume windows.
	if ( (path.length() >= 2) &&
		(((path.charAt(0) >= 'a') && (path.charAt(0) <= 'z')) ||
		((path.charAt(0) >= 'A') && (path.charAt(0) <= 'Z'))) &&
		(path.charAt(1) == ':') ) {
		return path.substring(0,2);
	}
	else {
	    return "";
	}
}

/**
Determine the file extension.
@return the part of a file name after the last "." character, or null if no ".".
*/
public static String getFileExtension ( String file ) {
	List<String> v = StringUtil.breakStringList ( file, ".", 0 );
	if ( (v == null) || (v.size() == 0) ) {
		return null;
	}
	return v.get(v.size() - 1);
}

// NEED TO CLEAN UP JAVADOC AND OPTIMIZE FOR GC.
/* ----------------------------------------------------------------------------
** HMGetFileHeader -	get the header for a file, assuming that the header is
**			indicated by comment or other special characters
** ----------------------------------------------------------------------------
** Copyright:	See the COPYRIGHT file.
** ----------------------------------------------------------------------------
** Notes:	(1)	This routine opens and closes the file and removes the
**			newlines from the strings.
**		(2)	"comments" contains a list of strings that, if at the
**			beginning of a line, indicate that the line is a
**			comment.  For example { "#", "REM", "$" }.  The
**			"ignore_comments" string list indicates comments that
**			do match "comments" but are comments that are to be
**			ignored.  For example { "#>", "#ignore" }.
**		(3)	It is assumed that a special comment can be saved that
**			has the format:
**
**				#HeaderRevision 1
**
**			where the number indicates the revision on the file
**			header.  The smallest such number is returned in
**			"header_first" and the largest in "header_last".
** ----------------------------------------------------------------------------
** History:
**
** 14 Feb 96	Steven A. Malers, RTi	Created routine.
** 04 Mar 96	SAM, RTi		Change so that the comment strings are
**					a string list and add the ignore list.
**					Also add the header revisions.
** 05 Sep 96	SAM, RTi		Split out of HMUtil.c file.
** 07 Oct 96	SAM, RTi		Add <string.h> to prototype functions.
** ----------------------------------------------------------------------------
** Variable	I/O	Description
**
** comments	I	Strings at start of line that indicate a header.
** dl		L	Debug level for this routine.
** filename	I	Name of file to pull lines from.
** flags	I	Flags.  Currently unused but may be used in the future
**			to ignore some of the special processing.
** fp		L	Pointer to open file.
** header_first	O	First header revision encountered in comments (smallest
**			number).
** header_last	O	Last header revision encountered in comments (largest
**			number).
** header_revision L	Header revision read from file.
** i		L	Loop counter for strings.
** ierror	O	Error code.
** ignore_comments I	Strings at start of line that indicte a header but which
**			should be ignored.
** iscomment	L	Is the current line a comment?
** isignore	L	Is the current line a comment that is to be ignored?
** len		L	Length of string.
** list		O	String list for header lines.
** nlines	O	Number of lines in header.
** revlen	L	Length of the header revision string.
** revpt	L	Pointer to a start of potential header revision comment.
** routine	L	Name of this routine.
** string	L	Line read from file.
** ----------------------------------------------------------------------------
*/

/**
@deprecated Use version that operates on lists.
*/
@Deprecated
public static FileHeader getFileHeader ( String filename, String[] commentIndicators,
		String[] ignoredCommentIndicators, int flags ) {
	List<String> commentIndicatorList = null;
	if ( commentIndicators != null ) {
		commentIndicatorList = StringUtil.toList(commentIndicators);
	}
	List<String> ignoredCommentIndicatorList = null;
	if ( ignoredCommentIndicators != null ) {
		ignoredCommentIndicatorList = StringUtil.toList(ignoredCommentIndicators);
	}
	return getFileHeader ( filename, commentIndicatorList, ignoredCommentIndicatorList, flags );
}

/**
@return The FileHeader associated with a file.  This information is used by processFileHeaders
when tracking revisions to files.  Comment strings must start at the beginning of the line.
@param fileName The name of the file to process.
@param commentIndicators A list of strings indicating valid comment indicators.  For example: {"#", "*"}.
@param ignoredCommentIndicators A list of strings indicating valid comments which
should be ignored and which take precedence over "comments".  For example, "#>"
might be used for comments that are written to a file each time it is revised
but those comments are to be ignored each time the header is read.
@param flags Currently unused.
@see #processFileHeaders
*/
public static FileHeader getFileHeader ( String fileName, List<String> commentIndicators,
	List<String> ignoredCommentIndicators, int flags ) {
	String	routine = IOUtil.class.getSimpleName() + ".getFileHeader", string;
	int	dl = 30, header_first = -1, header_last = -1, header_revision, i, len, revlen;
	boolean	iscomment, isignore;

	// Need to handle error.

	revlen = HEADER_REVISION_STRING.length();
	if ( fileName == null ) {
		Message.printWarning ( 10, routine, "NULL file name pointer" );
		return null;
	}
	if ( fileName.length() == 0 ) {
		Message.printWarning ( 10, routine, "Empty file name" );
		return null;
	}
	if ( !fileReadable(fileName) ) {
		Message.printWarning ( 10, routine, "File \"" + fileName + "\" is not readable" );
		return null;
	}
	if ( commentIndicators == null ) {
		Message.printWarning ( 10, routine, "Empty comment strings list" );
		return null;
	}

	// Open the file.

	BufferedReader fp = null;
	try {
		fp = new BufferedReader ( new FileReader(fileName) );
	}
	catch ( Exception e ) {
		Message.printWarning ( 10, routine, "Error opening file \"" + fileName + "\" for reading." );
		return null;
	}

	// Now read lines until we get to the end of the file or hit a non-header line (OK to skip "ignore_comments").

	FileHeader header = new FileHeader ();

	int length_comments = commentIndicators.size();
	int linecount = 0;
	while ( true ) {
		++linecount;
		try {
			string = fp.readLine ();
			if ( string == null ) {
				break;
			}
		}
		catch ( Exception e ) {
			// End of file.
			break;
		}
		// First, find out if the line is a comment.
		// It is if the first part of the string exactly matches any of the comment strings.
		iscomment = false;
		boolean revision_start = false;
		int comment_length = 0;
		for ( i = 0; i < length_comments; i++ ) {
			// Find the length of the comment string.
			comment_length = ((String)commentIndicators.get(i)).length();
			if ( comment_length < 1 ) {
				continue;
			}
			// Allow characters too so do a regionMatches.
			revision_start = string.regionMatches(true,0,(String)commentIndicators.get(i),0,
				((String)commentIndicators.get(i)).length() );
			if ( revision_start ) {
				// Found a match.
				iscomment	= true;
				if ( Message.isDebugOn ) {
					Message.printDebug ( 50, routine, "Found comment at line " + linecount );
				}
				break;
			}
		}
		// If we do not have a comment, then there is no need to continue in this loop because
		// we are out of the comments section in the file.
		if ( !iscomment ) {
			break;
		}
		// Find out if this is a header revision comment, and, if so, compare to the current values saved.
		String revision_string;
		if ( (comment_length + revlen) <= string.length() ) {
			// There might be a header string.
			revision_string = string.substring(comment_length, (comment_length + revlen));
		}
		else {
			revision_string = string.substring(comment_length);
		}
		if ( revision_string.equals(HEADER_REVISION_STRING) ) {
			// This is a header revision line so read the revision number from the string.
			revision_string = string.substring(	comment_length + revlen + 1);
			header_revision = StringUtil.atoi ( revision_string );
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Found header revision " + header_revision + "from \"" + string + "\"" );
			}
			header_first = Math.min(header_first, header_revision);
			header_last = Math.max(header_last, header_revision);
		}
		// Now determine whether this is a comment that can be ignored.
		// If so, we just do not add it to the list.
		isignore = false;
		if ( ignoredCommentIndicators != null ) {
			for ( i = 0; i < ignoredCommentIndicators.size(); i++ ) {
				// Find the length of the comment string.
				len = ((String)ignoredCommentIndicators.get(i)).length();
				String ignore_substring;
				if ( len <= string.length() ) {
					ignore_substring = string.substring(0,len);
				}
				else {
					ignore_substring = string.substring(0);
				}
				if(ignore_substring.equals((String)ignoredCommentIndicators.get(i))){
					// Found a match.
					isignore = true;
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Ignoring: \"" + string + "\"" );
					}
					break;
				}
			}
		}
		// If the comment is to be ignored, read another line.
		if ( isignore ) {
			// Don't want to read any further.  First ignored comment indicates the entire header has been read.
			break;
		}
		// If we have gotten to here, add the line to the list.
		string = StringUtil.removeNewline ( string );
		header.addElement ( string );
		//FIXME SAM 2008-12-11 need to trap error.
		//if ( list == (char **)NULL ) {
		//	HMPrintWarning ( 10, routine, "Error adding to string list." );
		//}
	}

	try {
		fp.close ();
	}
	catch ( Exception e ) {
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "\"" + fileName + "\":  " + header.size() + " lines in header" );
	}
	header.setHeaderFirst ( header_first );
	header.setHeaderLast ( header_last );
	return header;
}

/**
 * Get a list of files and/or folders.
 * This is the most general method.
 * @param startingFolder starting folder to list
 * @param listRecursive if true, list sub-folder contents recursively
 * @param listFiles if true, include files in output
 * @param listFolders if true, include folders in output
 * @param includePatterns if specified, include only the filenames that match any of the specified patterns
 * (leading path is not checked) - use java regular expression patterns (e.g., ".*" instead of "*")
 * @param excludePatterns if specified, exclude filenames that match any of the specified patterns
 * (after includePatterns is evaluated) - use java regular expression patterns (e.g., ".*" instead of "*")
 */
public static List<File> getFiles ( File startingFolder, boolean listRecursive, boolean listFiles, boolean listFolders,
		List<String> includePatterns, List<String> excludePatterns ) throws IOException {
	String routine = IOUtil.class.getSimpleName() + ".getFiles";
	if ( Message.isDebugOn ) {
		Message.printStatus(2, routine, "Listing startFolder=\"" + startingFolder
			+ "\" listRecursive=" + listRecursive
			+ " listFiles=" + listFiles
			+ " listFolders=" + listFolders );
	}
	List<File> matchingFiles = new ArrayList<>();
	if ( startingFolder == null ) {
		// Could not find a starting folder, should not happen.
		return matchingFiles;
	}
	String startingFolderString = startingFolder.getAbsolutePath();
	// Get the starting folder length, used to strip the starting folder below to check for subfolders.
	int startingFolderLen0 = startingFolderString.length();
	if ( !startingFolderString.endsWith("/") && !startingFolderString.endsWith("\\") ) {
		// Have to add to skip over the / immediately after the starting folder in results.
		// TODO smalers 2023-03-28 is this fragile because of how the path is changed during processing?
		startingFolderLen0 +=1;
	}
	// Needed because inner block below needs final.
	final int startingFolderLen = startingFolderLen0;

	// The pathMatcher is for the entire absolute path.
	//PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);

	// Use the walkFileTree() method:
	// - see: https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/essential/io/examples/Find.java
	// - the starting folder limits the paths that are evaluated but each path is still the full absolute path
	Path startingFolderPath = Paths.get(startingFolder.getAbsolutePath());
	Files.walkFileTree(startingFolderPath, new SimpleFileVisitor<Path>() {

		// The methods below handle directories (folder) separate from files.

		/**
		 * Handle each directory that is visited, necessary to include directories.
		 */
		@Override
		public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs) throws IOException {
			File file = dirPath.toFile();

			if ( Message.isDebugOn ) {
				Message.printStatus(2, routine, "  Checking folder path=\"" + dirPath + "\"");
			}

			// Default is to add all files and will constrain based on calling parameters.
			boolean okToAdd = true;

			if ( startingFolderString.equals(file.getAbsolutePath()) ) {
				// Do not include the folder itself.
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "  Skipping because starting folder is the same as directory.");
				}
				okToAdd = false;
			}
			else if ( file.isDirectory() && !listFolders ) {
				// Do not include if folders are not included.
				okToAdd = false;
			}
			else if ( !listRecursive ) {
				String pathEnd = file.getAbsolutePath().substring(startingFolderLen);
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "  Checking pathEnd=\"" + pathEnd);
				}
				if ( (pathEnd.indexOf("/") > 0) || (pathEnd.indexOf("\\") > 0) ) {
					// Don't want recursive listing:
					// - check whether the path after the starting folder contains / or \
					//   and if yes, then a sub-folder is being listed
					// - TODO smalers 2023-03-28 there is probably a way to break out of the walk
					okToAdd = false;
				}
			}

			// Check to see if the folder only (not leading path) matches the pattern filters.
			if ( okToAdd ) {
				String fileName = file.getName();
				if ( (includePatterns != null) && (includePatterns.size() > 0) ) {
					// Only include if the path matches one of the filters.
					boolean matchedInclude = false;
					for ( String includePattern : includePatterns ) {
						if ( fileName.matches(includePattern) ) {
							matchedInclude = true;
						}
					}
					if ( !matchedInclude ) {
						okToAdd = false;
					}
				}
				if ( okToAdd ) {
					if ( (excludePatterns != null) && (excludePatterns.size() > 0) ) {
						// Check the excludes.
						for ( String excludePattern : excludePatterns ) {
							if ( fileName.matches(excludePattern) ) {
								okToAdd = false;
								break;
							}
						}
					}
				}
			}

			// If was not filtered out, add to the list.
			if ( okToAdd ) {
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "  Adding \"" + file + "\"");
				}
				matchingFiles.add(file);
			}
			else {
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "  Skipping \"" + file + "\"");
				}
			}

			return FileVisitResult.CONTINUE;
		}

		/**
		 * Handle each file that is visited.
		 * This does not handle folders.
		 */
		@Override
		public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
			File file = filePath.toFile();

			if ( Message.isDebugOn ) {
				Message.printStatus(2, routine, "  Checking file path=\"" + filePath + "\"");
			}

			// Default is to add all files and will constrain based on calling parameters.
			boolean okToAdd = true;
			/* TODO smalers 2023-03-28 Evaluate whether to use a PathMatcher instead of the following code:
			 * - a PathMatcher might perform better
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Checking path \"" + path + "\" using pattern \"" + pattern + "\"");
			}
			if ( pathMatcher.matches(path) ) {
				if ( Message.isDebugOn ) {
					Message.printDebug(1, routine, "Matched file \"" + path + "\" using pattern \"" + pattern + "\"");
				}
				files.add(path.toFile());
			}
			*/

			if ( file.isFile() && !listFiles ) {
				// Do not include if files are not included.
				okToAdd = false;
			}
			else if ( !listRecursive ) {
				String pathEnd = file.getAbsolutePath().substring(startingFolderLen);
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "  Checking pathEnd=\"" + pathEnd);
				}
				if ( (pathEnd.indexOf("/") > 0) || (pathEnd.indexOf("\\") > 0) ) {
					// Don't want recursive listing:
					// - check whether the path after the starting folder contains / or \
					//   and if yes, then a sub-folder is being listed
					// - TODO smalers 2023-03-28 there is probably a way to break out of the walk
					okToAdd = false;
				}
			}

			// Check to see if the file only (not leading path) matches the pattern filters.
			if ( okToAdd ) {
				String fileName = file.getName();
				if ( (includePatterns != null) && (includePatterns.size() > 0) ) {
					// Only include if the path matches one of the filters.
					boolean matchedInclude = false;
					for ( String includePattern : includePatterns ) {
						if ( fileName.matches(includePattern) ) {
							matchedInclude = true;
						}
					}
					if ( !matchedInclude ) {
						okToAdd = false;
					}
				}
				if ( okToAdd ) {
					if ( (excludePatterns != null) && (excludePatterns.size() > 0) ) {
						// Check the excludes.
						for ( String excludePattern : excludePatterns ) {
							if ( fileName.matches(excludePattern) ) {
								okToAdd = false;
								break;
							}
						}
					}
				}
			}

			// If was not filtered out, add to the list.
			if ( okToAdd ) {
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "  Adding \"" + file + "\"");
				}
				matchingFiles.add(file);
			}
			else {
				if ( Message.isDebugOn ) {
					Message.printStatus(2, routine, "  Skipping \"" + file + "\"");
				}
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc ) throws IOException {
			return FileVisitResult.CONTINUE;
		}

	});
	return matchingFiles;
}

/**
Get a list of files from a path list.
This does not check the file systems.  It just manipulates the paths.
@return a list of paths to a file given a prefix path and a file name.
The files do not need to exist.  Return null if there is a problem with input.
@param paths Paths to prefix the file with.
@param file Name of file to append to paths.
*/
public static List<String> getFilesFromPathList ( List<String> paths, String file ) {
	String fullfile, routine = IOUtil.class.getSimpleName() + ".getFilesFromPathList";
	List<String> newlist = null;
	int	i, npaths;

	// Check for NULL list, and file.

	if ( paths == null ) {
		Message.printWarning ( 10, routine, "NULL path list" );
		return null;
	}
	if ( file == null ) {
		Message.printWarning ( 10, routine, "NULL file name" );
		return newlist;
	}

	npaths = paths.size();
	newlist = new ArrayList<>(10);
	String dirsep = System.getProperty ( "file.separator");
	for ( i = 0; i < npaths; i++ ) {
		// Add each string to the list.
		fullfile = paths.get(i) + dirsep + file;
		newlist.add ( fullfile );
	}
	return newlist;
}

/**
Return a list of files matching a pattern.
The listing is recursive.
@param pattern file pattern to match relative to the starting folder, as "glob:..." string used with java.nio package,
should be an absolute path with only Linux folder separator (/)
@return a list of matching File, guaranteed to exist but may be an empty list
*/
public static List<File> getFilesMatchingPattern(String pattern) throws IOException {
	String routine = IOUtil.class.getSimpleName() + ".getFilesMatchingPattern";
	if ( Message.isDebugOn ) {
		Message.printDebug(1, routine, "Getting matching files for: " + pattern);
	}
	List<File> files = new ArrayList<>();

	String startingFolder = IOUtil.getPathWithNoGlob(pattern);
	if ( startingFolder == null ) {
		// Could not find a starting folder, should not happen.
		return files;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug(1, routine, "Starting folder is: " + startingFolder);
	}
	// Reset pattern to the remainder of the original pattern.

	// The pathMatcher is for the entire absolute path.
	PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);

	// The starting folder limits the paths that are evaluated but each path is still the full absolute path.
	Path startingFolderPath = Paths.get(startingFolder);
	Files.walkFileTree(startingFolderPath, new SimpleFileVisitor<Path>() {

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Checking path \"" + path + "\" using pattern \"" + pattern + "\"");
			}
			if ( pathMatcher.matches(path) ) {
				if ( Message.isDebugOn ) {
					Message.printDebug(1, routine, "Matched file \"" + path + "\" using pattern \"" + pattern + "\"");
				}
				files.add(path.toFile());
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc ) throws IOException {
			return FileVisitResult.CONTINUE;
		}

	});
	return files;
}

/**
Return a list of files matching a pattern.
Currently the folder must exist (no wildcard) and the file part of the path can contain wildcards using
globbing notation (e.g., *.txt).
@param folder folder to search for files
@param extension to match or use * to match all files
@param caseIndependent if true check extension case-independent (default is case-dependent)
TODO SAM 2012-07-22 This method should be replaced with java.nio.file.PathMatcher when updated to Java 1.7.
*/
public static List<File> getFilesMatchingPattern(String folder, String extension, boolean caseIndependent) {
    File f = new File(folder);
    // Get the list of all files in the folder (do this because want to do case-independent).
    SimpleFileFilter filter = new SimpleFileFilter("*", "all");
    File[] files = f.listFiles(filter);
    List<File> matchedFiles = new ArrayList<>();
    if ( (files == null) || (files.length == 0) ) {
        return matchedFiles;
    }
    else {
        for ( File f2 : files ) {
            if ( (extension == null) || extension.isEmpty() || extension.equals("*") ) {
            	matchedFiles.add(f2);
            }
            else {
            	if ( caseIndependent ) {
            		if ( IOUtil.getFileExtension(f2.getName()).equalsIgnoreCase(extension) ) {
            			matchedFiles.add(f2);
            		}
            	}
            	else {
            		if ( IOUtil.getFileExtension(f2.getName()).equals(extension) ) {
            			matchedFiles.add(f2);
            		}
            	}
            }
        }
        return matchedFiles;
    }
}

/**
 * Determine the overall attributes for files in folder.
 * The size of all files is summed.
 * The modification time is the latest modification time.
 * @param folder the starting folder for the file search
 * @param listRecursive whether to list files recursively in sub-folders
 * @param includePatterns if specified, include only the filenames that match the pattern (leading path is not checked) - use glob-style wildcards
 * @param excludePatterns if specified, exclude filenames that match the pattern (after includePatterns is evaluated) - use glob-style wildcards
 * @return the size in bytes
 * @throws IOException if the folder does not exist
 */
public static BasicFolderAttributes getFolderAttributes ( File folder, boolean listRecursive, List<String> includePatterns, List<String> excludePatterns )
throws IOException {
	String routine = IOUtil.class.getSimpleName() + ".getFolderAttributes";
	// Properties for files in the folder.
	FileTime filesMinCreationTime = null;
	FileTime filesMaxCreationTime = null;
	FileTime filesMinModifiedTime = null;
	FileTime filesMaxModifiedTime = null;
	long size = 0;
	try {
		boolean listFiles = true;
		boolean listFolders = true;
		List<File> files = getFiles ( folder, listRecursive, listFiles, listFolders, includePatterns, excludePatterns );
		//Message.printStatus(2, routine, "Have " + files.size() + " files in folder \"" + folder.getAbsolutePath() + "\".");
		// Loop through the files and evaluate each.
		for ( File file : files ) {
			Path path = Paths.get(file.getAbsolutePath());
			BasicFileAttributes fileAttrib = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			FileTime creationTime = fileAttrib.creationTime();
			FileTime lastModifiedTime = fileAttrib.lastModifiedTime();
			if ( (filesMinCreationTime == null) || (creationTime.compareTo(filesMinCreationTime) < 0) ) {
				filesMinCreationTime = creationTime;
			}
			if ( (filesMaxCreationTime == null) || (creationTime.compareTo(filesMaxCreationTime) > 0) ) {
				filesMaxCreationTime = creationTime;
			}
			if ( (filesMinModifiedTime == null) || (lastModifiedTime.compareTo(filesMinModifiedTime) < 0) ) {
				filesMinModifiedTime = lastModifiedTime;
			}
			if ( (filesMaxModifiedTime == null) || (lastModifiedTime.compareTo(filesMaxModifiedTime) > 0) ) {
				filesMaxModifiedTime = lastModifiedTime;
			}
			size += file.length();
		}
	}
	catch ( Exception e ) {
		Message.printWarning(3, routine, "Error getting folder size.");
		Message.printWarning(3, routine, e );
		size = 0;
	}

	// Get the attributes for the folder.
	Path path = Paths.get(folder.getAbsolutePath());
	BasicFileAttributes fileAttrib = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

	// Create a file attributes object based on the operating system.
	BasicFolderAttributes folderAttrib = new BasicFolderAttributes (
		folder, fileAttrib.creationTime(), fileAttrib.lastModifiedTime(), size,
		filesMinCreationTime, filesMaxCreationTime, filesMinModifiedTime, filesMaxModifiedTime );

	return folderAttrib;
}

/**
Open an input stream given a URL or regular file name.
@return An InputStream given a URL or file name.
If the string starts with "http:", "ftp:", or "file:", a URL is created and the associated stream is returned.
Otherwise, a file is opened and the associated stream is returned.
@param url_string
@exception IOException if the input stream cannot be initialized.
*/
public static InputStream getInputStream ( String url_string )
throws IOException {
	String routine = IOUtil.class.getSimpleName() + ".getInputStream";
    URL url;
    FileInputStream fileStream;
    String noIndex = "Cannot open file at " + url_string + ".";

	// Make sure that the string is not empty.

	if ( url_string == null ) {
		throw new IOException ( "URL is null." );
	}
	if ( url_string.length() < 1 ) {
		throw new IOException ( "URL is empty." );
	}

	if ( url_string.regionMatches( true, 0, "http:", 0, 5) ||
		url_string.regionMatches( true, 0, "https:", 0, 6) ||
		url_string.regionMatches( true, 0, "file:", 0, 5) ||
		url_string.regionMatches( true, 0, "ftp:", 0, 4) ) {
		try {
			url = new URL ( url_string );
			return ( url.openStream() );
		}
		catch ( Exception Error ) {
			Message.printWarning (10, routine, noIndex );
			throw new IOException(noIndex);
		}
	}
	else {
	    try {
	        fileStream = new FileInputStream(url_string);
			return ( fileStream );
		}
		catch (Exception e) {
			Message.printWarning ( 10, routine, noIndex );
			throw new IOException(noIndex);
		}
	}
}

/**
Returns the contents of the manifests in all the Jar files in the classpath.
The contents are returned are a formatted list of Strings.  The name of
each Jar file is printed, followed by a list of the lines in its manifest.
If there are further Jar files, a space is added and the pattern is repeated.<p>
The order of the Jar files in the list is the same as the order the Jar files appear in the CLASSPATH.<p>
The order of the manifest data is not necessarily the same as how they appear in the manifest file.
The Java classes provided for accessing Manifest data do not return the data in any given order.
For this reason, the manifest data are sorted alphabetically in the list.
@return the contents of the manifests of the Jar files in a Vector of Strings.
*/
public static List<String> getJarFilesManifests() {
	// Get the Classpath and split it into a String array.
	// The order of the elements in the array is the same as the order in which things are included in the classpath.
	String[] jars = System.getProperty("java.class.path").split(System.getProperty("path.separator"));

	Attributes a = null;
	int j = -1;
	int size = -1;
	JarFile jar = null;
	Manifest mf = null;
	Object[] o = null;
	Set<Object> set = null;
	String tab = "    ";
	List<String> sort = null;
	List<String> v = new ArrayList<>();

	v.add ("---------------------------------------------------------------------------");
	v.add ("Manifest values for each jar file in the class path are listed below:");
	v.add ("- manifest values are sorted");
	v.add ("---------------------------------------------------------------------------");
	v.add ("");

	for (int i = 0; i < jars.length; i++) {
		if (!StringUtil.endsWithIgnoreCase(jars[i], ".jar")) {
			// Directories, etc, can be specified in a class path but only process the jar files in the class path.
			continue;
		}

		v.add(jars[i]);

		try {
			// Create a JarFile instance:
			// - will find the jar file in the class path?
			// - throws IOException and SecurityException, both of which extend from Exception
			jar = new JarFile(jars[i]);
			mf = jar.getManifest();
			a = mf.getMainAttributes();
			set = a.keySet();
			o = set.toArray();
			sort = new ArrayList<>();
			for (j = 0; j < o.length; j++) {
				sort.add(tab + ((Attributes.Name)(o[j])) + " = " + a.getValue((Attributes.Name)(o[j])));
			}

			// The order in which the data in the manifest file are returned is not guaranteed to be in the same
			// order as they are in the manifest file.
			// Therefore, the data are sorted to present a consistent return pattern.
			Collections.sort(sort);
			size = sort.size();
			for (j = 0; j < size; j++) {
				v.add(sort.get(j));
			}
		}
		catch (Exception e) {
			String routine = IOUtil.class.getSimpleName() + ".getJarFilesManifests";
			Message.printWarning(2, routine, "An error occurred while reading the manifest for: '" + jars[i] + "'.");
			Message.printWarning(3, routine, e);
			v.add(tab + "An error occurred while reading the manifest.");
		}
		finally {
			try {
				if ( jar != null ) {
					jar.close();
				}
			}
			catch ( IOException e ) {
				// Should not happen.
			}
		}

		v.add("");
	}

	return v;
}

/**
 * Return the Java major version (e.g., 7, 8, 11).
 *@return the Java major version (e.g., 7, 8, 11), or -1 if unknown.
 */
public static int getJavaMajorVersion () {
	String javaVersion = System.getProperty("java.vm.specification.version");
	if ( javaVersion.startsWith("1.") ) {
		// Early versions like 1.7.
		javaVersion = javaVersion.substring(2).trim();
		return Integer.parseInt(javaVersion);
	}
	else {
		// Version is just the major version such as "11" for Java 11:
		// - check for a period just in case
		if ( javaVersion.contains(".") ) {
			int pos = javaVersion.indexOf(".");
			return Integer.parseInt(javaVersion.substring(0,pos));
		}
		else {
			return Integer.parseInt(javaVersion);
		}
	}
}

/**
Return the Java Runtime Environment architecture bits.
@return the JRE bits, 32 or 64
*/
public static int getJreArchBits () {
	String arch = System.getProperty("sun.arch.data.model");
	if ( arch == null ) {
		return -1;
	}
	int bits = Integer.parseInt(arch);
	return bits;
}

/**
Return the operating system architecture bits.  This is only enabled on Windows.
@return the architecture bits, 32 or 64.
*/
public static int getOSArchBits () {
	if ( !isUNIXMachine() ) {
	    String arch = System.getenv("PROCESSOR_ARCHITECTURE");
	    String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

	    int realArch = 32;
	    if ( ((arch != null) && arch.endsWith("64")) || ((wow64Arch != null) && wow64Arch.endsWith("64")) ||
	    	System.getProperty("os.arch").contains("64") && !System.getProperty("os.arch").equals("IA64N") ) {
	        // IA64N, despite its name, is not actually 64 bit.
	        // See http://h30499.www3.hp.com/t5/System-Administration/Java-SDK-What-are-IA64N-and-IA64W/td-p/4863858
	    	realArch = 64;
	    }
	    return realArch;
	}
	return 32;
}

/**
Return a path considering the working directory set by setProgramWorkingDir().  The following rules are used:
<ul>
<li>	If the path is null or empty, return the path.</li>
<li>	If the path is an absolute path (starts with / or \ or has : as the
	second character; or starts with http:, ftp:, file:), it is returned as is.</li>
<li>	If the path is a relative path and the working directory is ".", the path is returned.</li>
<li>	If the path is a relative path and the working directory is not ".",
	the path is appended to the current working directory (separated with
	/ or \ as appropriate) and returned.</li>
</ul>
@param path Path to use.
@return a path considering the working directory.
*/
public static String getPathUsingWorkingDir ( String path ) {
    String routine = IOUtil.class.getSimpleName() + ".getPathUsingWorkingDir";
    if ( (path == null) || (path.length() == 0) ) {
		return path;
	}
	// Check for URL.
	if ( path.startsWith("http:") || path.startsWith("ftp:") || path.startsWith("file:") ) {
		return path;
	}
	// Check for absolute path.
	if ( isUNIXMachine() ) {
		if ( path.charAt(0) == '/' ) {
			return path;
		}
		if ( _working_dir.equals("") || _working_dir.equals(".") ) {
			return path;
		}
		else {
			String fullPath = path;
			try {
			    fullPath = (new File(_working_dir + "/" + path).getCanonicalPath().toString());
			}
			catch (IOException e) {
			    Message.printWarning(3, routine, e);
			    // FIXME SAM 2009-05-05 Evaluate whether to do the following - used for startup issues before logging?
				e.printStackTrace();
			}
			return fullPath;
			//return ( _working_dir + "/" + path );
		}
	}
	else {
		if (path.startsWith("\\\\")) {
			// UNC path.
			return path;
		}
		if ( (path.charAt(0) == '\\') || ((path.length() >= 2) && (path.charAt(1) == ':')) ) {
			return path;
		}
		if ( _working_dir.equals("") || _working_dir.equals(".") ) {
			return path;
		}
		else {
			String fullPath = path;
   			try {
   				fullPath = (new File(_working_dir + "\\" + path).getCanonicalPath().toString());
    		} catch (IOException e) {
    		    Message.printWarning(3, routine, e);
    			e.printStackTrace();
    		}
			return fullPath;
			//return ( _working_dir + "\\" + path );
		}
	}
}

/**
 * Get the leading part of a path that does not contain glob characters (*, {, ?, or [).
 * This is used to determine the starting folder for the getFilesMatchingPattern function.
 * The backslash is not handled because because it may be in a Windows path.
 * Strings are used as parameter and return value because glob characters are allowed and
 * may not be handled in a normal path.
 * @param path a full (absolute) path, starting with / on Linux and \ on Windows,
 * although both are handled
 * @return the leading path that DOES NOT contain glob characters, ending in the folder separator,
 * or null if no glob characters
 */
public static String getPathWithNoGlob ( String path ) {
	// Remove leading "glob:".
	path = path.replace("glob:", "");
	int pos = Integer.MAX_VALUE;
	String searchChars = "*{?[";
	for ( int i = 0; i < searchChars.length(); i++ ) {
		int pos0 = path.indexOf(searchChars.charAt(i));
		if ( pos0 >= 0 ) {
			if ( pos0 < pos ) {
				pos = pos0;
			}
		}
	}
	// If here either found the first occurrence of a glob character or none.
	if ( pos == Integer.MAX_VALUE ) {
		// Did not find any glob characters.
		return null;
	}
	// Search backward from the glob character to find / or \.
	char c;
	for ( int i = pos; i >= 0; i-- ) {
		c = path.charAt(i);
		if ( (c == '/') || (c == '\\') ) {
			// Found a folder separator.
			return path.substring(0,i);
		}
	}
	// Should not get here so return null.
	return null;
}

/**
 * Get the process ID for the current Java Virtual Machine.
 * This is useful when communicating the information in a system.
 */
public static int getProcessId () {
	String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
	String pid = processName.split("@")[0];
	Integer ipid = Integer.parseInt(pid);
	return ipid.intValue();
}

/**
Return the program arguments.
@return The program arguments set with setProgramArguments.
@see #setProgramArguments
*/
public static String[] getProgramArguments () {
	if ( !_initialized ) {
		initialize ();
	}
	return _argv;
}

/**
Return the program command file.
@return The command file used with the program, as set by setProgramCommandFile.
@see #setProgramCommandFile
*/
public static String getProgramCommandFile () {
	if ( !_initialized ) {
		initialize ();
	}
	return _command_file;
}

/**
Return the program command list.  Typically either a command file or list is used and the list takes precedence.
@return The command list used with the program, as set by setProgramCommandList.
@see #setProgramCommandList
*/
public static List<String> getProgramCommandList () {
	if ( !_initialized ) {
		initialize ();
	}
	return _command_list;
}

/**
Return name of host machine.
@return The host that is running the program, as set by setProgramHost.
@see #setProgramHost
*/
public static String getProgramHost () {
	if ( !_initialized ) {
		initialize ();
	}
	return _host;
}

/**
Return the program name.
@return The program name as set by setProgramName.
@see #setProgramName
*/
public static String getProgramName () {
	if ( !_initialized ) {
		initialize ();
	}
	return _progname;
}

/**
Return the program user.
@return The program user as set by setProgramUser.
@see #setProgramUser
*/
public static String getProgramUser () {
	if ( !_initialized ) {
		initialize ();
	}
	return _user;
}

/**
Return the program version.
@return The program version as set by setProgramVersion.
@see #setProgramVersion
*/
public static String getProgramVersion () {
	if ( !_initialized ) {
		initialize ();
	}
	return _progver;
}

/**
Return the working directory.
@return The program working directory as set by setProgramWorkingDir.
@see #setProgramWorkingDir
*/
public static String getProgramWorkingDir() {
	if ( !_initialized ) {
		initialize ();
	}
	return _working_dir;
}

/**
Return global PropList property.
@return The property in the global property list manager corresponding to the given key.
<b>This routine is being reworked to be consistent with the Prop* classes.</b>
@param key String key to look up a property.
@see Prop
@see PropList
@see PropListManager
*/
public static Prop getProp ( String key ) {
	if ( !_initialized ) {
		initialize ();
	}
	return _prop_list_manager.getProp ( key );
}

/**
Return property value as an Object.
@return The value of a property in the global property list manager corresponding to the given key.
@param key String key to look up a property.
@see Prop
@see PropList
@see PropListManager
*/
public static Object getPropContents ( String key ) {
	if ( !_initialized ) {
		initialize ();
	}
	Prop prop = getProp ( key );
	if ( key == null ) {
		return null;
	}
	return prop.getContents();
}

/**
Return global PropListManager.
@return the instance of the global property list manager.
@see Prop
@see PropList
@see PropListManager
*/
public static PropListManager getPropListManager () {
	if ( !_initialized ) {
		initialize ();
	}
	return _prop_list_manager;
}

/**
Return property value as a String.
@return The value of a property in the global property list manager corresponding to the given key.
@param key String key to look up a property.
@see Prop
@see PropList
@see PropListManager
*/
public static String getPropValue ( String key ) {
	if ( !_initialized ) {
		initialize ();
	}
	try {
	    Prop prop = getProp ( key );
		if ( prop == null ) {
			return null;
		}
		return prop.getValue ();
	}
	catch (Exception e ) {
		// Probably a security exception.
		return null;
	}
}

/**
Returns a list of strings containing information about the system on which
the Java application is currently running.
See also getJarFileManifests() method for details for each jar file.
@return a list of Strings.
*/
public static List<String> getSystemProperties() {
	String tab = "    ";

	List<String> systemProperties = new ArrayList<>();

	systemProperties.add("System Properties Defined for Application: ");
	systemProperties.add(tab + " Program Name: " + _progname + " " + _progver);
	systemProperties.add(tab + " User Name: " + _user);
	String now = TimeUtil.getSystemTimeString("");
	systemProperties.add(tab + " Date: " + now);
	systemProperties.add(tab + " Host: " + _host);
	systemProperties.add(tab + " Working Directory: " + _working_dir);
	String command = tab + " Command: " + _progname + " ";

	int totalLength = command.length();

	int length = 0;

	if (_argv != null) {
		for (int i = 0; i < _argv.length; i++) {
			length = _argv[i].length() + 1;

			if (totalLength + length >= 80) {
				// Full command line would be too big for the line,
				// so add the current line and put the next argument on what will be the next line.
				systemProperties.add(command);
				command = tab + tab + _argv[i];
			}
			else {
				command += _argv[i] + " ";
			}
			totalLength = command.length();
		}
	}
	systemProperties.add(command);
	systemProperties.add("");

	systemProperties.add("Operating System Information:");
	systemProperties.add(tab + "Name: " + System.getProperty("os.name"));
	systemProperties.add(tab + "Version: " + System.getProperty("os.version"));
	systemProperties.add(tab + "System Architecture: " + System.getProperty("os.arch"));
	systemProperties.add("");

	systemProperties.add("Environment Variables:");
    Map<String,String> env = System.getenv();
    Set<String> envVars = env.keySet();
    List<String> envVarList = new ArrayList<>(envVars);
    envVarList = StringUtil.sortStringList(envVarList, StringUtil.SORT_ASCENDING, null, false, true);
    for ( String name : envVarList ) {
    	systemProperties.add(tab + " " + name + " = \"" + System.getenv(name) + "\"");
    }
    systemProperties.add("");

	systemProperties.add("Java Virtual Machine Memory Information:");
	systemProperties.add(tab + "JVM PID: " + IOUtil.getProcessId() );
	Runtime r = Runtime.getRuntime();
	systemProperties.add(tab + "Maximum memory (see Java -Xmx): " + r.maxMemory() + " bytes, " + r.maxMemory()/1024 + " kb, " + r.maxMemory()/1048576 + " mb" );
	systemProperties.add(tab + "Total memory (will be increased to maximum as needed): " + r.totalMemory() + " bytes, " + r.totalMemory()/1024 + " kb, " + r.totalMemory()/1048576 + " mb");
	long used = r.totalMemory() - r.freeMemory();
	systemProperties.add(tab + "Used memory: " + used + " bytes, " + used/1024 + " kb, " + used/1048576 + " mb");
	systemProperties.add(tab + "Free memory: " + r.freeMemory() + " bytes, " + r.freeMemory()/1024 + " kb, " + r.freeMemory()/1048576 + " mb");
	systemProperties.add("");

    systemProperties.add("Java Virtual Machine Properties (System.getProperties()): ");
    Properties properties = System.getProperties();
    Set<String> names = properties.stringPropertyNames();
    List<String> nameList = new ArrayList<>(names);
    Collections.sort ( nameList );
    for ( String name : nameList ) {
    	if ( name.equals("line.separator") ) {
    		// Special case because printing actual character will be invisible.
    		String nl = System.getProperty(name);
    		nl = nl.replace("\r", "\\r");
    		nl = nl.replace("\n", "\\n");
    		systemProperties.add(tab + " " + name + " = \"" + nl + "\"");
    	}
    	else {
    		systemProperties.add(tab + " " + name + " = \"" + System.getProperty(name) + "\"");
    	}
    }
    systemProperties.add("");

	systemProperties.add("Java Information:");
	systemProperties.add(tab + "Vendor: " + System.getProperty("java.vendor"));
	systemProperties.add(tab + "Version: " + System.getProperty("java.version"));
	systemProperties.add(tab + "Home: " + System.getProperty("java.home"));

	String sep = System.getProperty("path.separator");

	String[] jars = System.getProperty("java.class.path").split(sep);

	if (jars.length == 0) {
		return systemProperties;
	}

	String cp = tab + "Classpath: " + jars[0];
	totalLength = cp.length();

	for (int i = 1; i < jars.length; i++) {
		length = jars[i].length();

		if (totalLength + length >= 80) {
			systemProperties.add(cp + sep);
			cp = tab + tab + jars[i];
		}
		else {
			cp += sep + jars[i];
		}
		totalLength = cp.length();
	}
	systemProperties.add(cp);

	systemProperties.add("");

	return systemProperties;
}

/**
Download a file given a URI and optionally save to a local file or StringBuffer.
If the file is not saved, the error code return value will indicate whether it exists (200) or not (e.g., 400).
Redirects should be followed by default.
Any exception is absorbed and the return code indicates the error.
@param uri the URI for the file to retrieve.
@param outputFile output file to save the content.  If null or empty, don't save.
@param outputString output string to save the content.  If null, don't save.
@param connectTimeout connect timeout in ms
@param readTimeout read timeout in ms, 0 or negative to not set
@return HTTP exit code from retrieving the content, 0 or negative to not set
*/
public static int getUriContent ( String uri, String outputFile, StringBuilder outputString, int connectTimeout, int readTimeout ) {
    FileOutputStream fos = null;
    HttpURLConnection urlConnection = null;
    InputStream is = null;
    int code = -1; // HTTP code.
	try {
        // Some sites need cookie manager.
        // See: http://stackoverflow.com/questions/11022934/getting-java-net-protocolexception-server-redirected-too-many-times-error
        CookieHandler.setDefault(new CookieManager(null,CookiePolicy.ACCEPT_ALL));
        // Open the input stream.
        URL url = new URL(uri);
        urlConnection = (HttpURLConnection)url.openConnection();
        if ( connectTimeout > 0 ) {
        	urlConnection.setConnectTimeout(connectTimeout);
        }
        if ( readTimeout > 0 ) {
        	urlConnection.setReadTimeout(connectTimeout);
        }
        is = urlConnection.getInputStream();
        BufferedInputStream isr = new BufferedInputStream(is);
        // Open the output file.
        boolean doOutputFile = false;
        if ( (outputFile != null) && !outputFile.isEmpty() ) {
            fos = new FileOutputStream( outputFile );
            doOutputFile = true;
        }
        boolean doOutputString = false;
        if ( outputString != null ) {
        	doOutputString = true;
        }
        // Output the characters to the local file.
        int numCharsRead;
        int arraySize = 8192; // 8K optimal.
        byte[] byteArray = new byte[arraySize];
        //int bytesRead = 0;
        while ((numCharsRead = isr.read(byteArray, 0, arraySize)) != -1) {
        	if ( doOutputFile ) {
        		fos.write(byteArray, 0, numCharsRead);
        	}
            if ( doOutputString ) {
            	// Also set the content in memory.
            	if ( numCharsRead == byteArray.length ) {
            		outputString.append(new String(byteArray));
            	}
            	else {
            		byte [] byteArray2 = new byte[numCharsRead];
            		System.arraycopy(byteArray, 0, byteArray2, 0, numCharsRead);
            		outputString.append(new String(byteArray2));
            	}
            }
            //bytesRead += numCharsRead;
        }
	}
	catch ( Exception e ) {
		// Log it.
		String routine = IOUtil.class.getSimpleName() + ".getUriContent";
		Message.printWarning(3, routine, "Error getting the content for: ", uri );
		Message.printWarning(3, routine, e );
	}
    finally {
        // Close the streams and connection.
        if ( is != null ) {
        	try {
        		is.close();
        	}
        	catch ( IOException e ) {
        	}
        }
        if ( urlConnection != null ) {
        	urlConnection.disconnect();
        	try {
        		code = urlConnection.getResponseCode();
        	}
        	catch ( IOException e ) {
        		// Should not happen?
        		code = -999;
        	}
        }
    }
	return code;
}

/**
Determines JVM through which the application/applet is currently running
@return the vendor as SUN, MICROSOFT, etc...
*/
public static int getVendor() {
	String s = System.getProperty( "java.vendor.url" );
	if ( s.equalsIgnoreCase( "http://www.sun.com" ) ) {
		return SUN;
	}
	else if ( s.equalsIgnoreCase( "http://www.microsoft.com" ) ) {
		return MICROSOFT;
	}
	else {
	    return UNKNOWN;
	}
}

/**
Initialize the global data.
The setApplet() method should be called first in an applet to allow some of the if statements below to be executed properly.
*/
private static void initialize () {
	String routine = IOUtil.class.getSimpleName() + ".initialize";
	int dl = 1;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Initializing IOUtil..." );
	}
	try {
	    // Put this in just in case we have security problems.
		if ( _is_applet ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "An applet!");
			}
			_command_file = "";
			_command_list = null;
			// TODO (JTS - 2005-06-06) should do some testing to see what the effects are
			// of doing a host set like in the non-applet code below.  Possibilities I foresee:
			// 1) applets lack the permission to get the hostname
			// 2) applets return the name of the computer the user is physically working on.
			// 3) applets return the name of the server on which the applet code actually resides.
			// I have no way of knowing right now which one would
			// be the case, and moreover, no time to test this.
			_host = "web server/client/URL unknown";
			_progname = "program name unknown";
			_progver = "version unknown";
			_user = "user unknown (applet)";
			_working_dir = "dir unknown (applet)";
			__homeDir = "dir unknown (applet)";
		}
		else {
		    // A stand-alone application.
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Not an applet!" );
			}
			_command_file = "";
			_command_list = null;
			_host = InetAddress.getLocalHost().getHostName();
			_progname = "program name unknown";
			_progver = "version unknown";
			_user = System.getProperty("user.name");
			_working_dir = System.getProperty ("user.dir");
			__homeDir = System.getProperty ("user.dir");
		}
	} catch ( Exception e ) {
		// Don't do anything.  Just print a warning.
		Message.printWarning ( 3, routine, "Caught an exception initializing IOUtil (" + e + ").  Continuing." );
	}

	// Initialize the applet context.

	_applet_context = null;

	// Initialize the property list manager to contain an unnamed list.

	_prop_list_manager = new PropListManager ();
	_prop_list_manager.addList ( new PropList("", PropList.FORMAT_PROPERTIES), true );

	// Set the flag to know that the class has been initialized.

	_initialized = true;
}

/**
Return true if the program is an applet (must have set with setApplet).
@see #setApplet
*/
public static boolean isApplet () {
	return _is_applet;
}

/**
Set whether the program is an Applet (often called by other routines).
DO NOT CALL INITIALIZE BEFORE SETTING.
THIS FUNCTION IS EXPECTED TO BE CALLED FIRST THING IN THE init() FUNCTION OF AN APPLET CLASS.
THEN, initialize() WILL KNOW TO TREAT AS AN APPLET!
initialize() is called automatically from this method.
@param is_applet true or false, indicatign whether an Applet.
@deprecated Use setApplet()
@see #setApplet
*/
@Deprecated
public static void isApplet ( boolean is_applet ) {
	int dl = 1;

	_is_applet = is_applet;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, "IOUtil.isApplet", "set _is_applet to " + _is_applet );
	}
	// Force the reinitialization.  Problems may have occurred when IO
	// first loaded if it did not know that we are running an Applet so reinitialize now.
	initialize ();
}

/**
Return true if the program is a batch program.
@return true if the program is a batch program.
*/
public static boolean isBatch () {
	return _is_batch;
}

/**
Set whether the program is running in batch mode (the default if not set is false).
@param is_batch Indicates whether the program is batch mode.
*/
public static void isBatch ( boolean is_batch ) {
	_is_batch = is_batch;
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "IOUtil.isBatch", "Batch mode is " + _is_batch );
	}
}

/**
Determine whether the machine is big or little endian.
This method should be used when dealing with binary files written using native operating system applications
(e.g., native C, C++, and FORTRAN compilers).
The Java Virtual Machine is big endian so any binary files written with Java are transparently big endian.
If little endian files need to be read, use the EndianDataInputStream and other classes in this package.
Currently the determination is made by looking at the operating system.  The following are assumed:
<pre>
Linux	          LittleEndian
All other UNIX    BigEndian
All others        LittleEndian
</pre>
@return true if the machine is big endian.
*/
public static boolean isBigEndianMachine () {
	String name = System.getProperty ( "os.name" );
	if ( name.equalsIgnoreCase("Linux") ) {
		return false;
	}
	else if ( isUNIXMachine() ) {
		return true;
	}
	else {
	    return false;
	}
}

/**
Determine whether a path is an absolute path.
The standard Java File.isAbsolute() only returns true on Windows if the leading
path contains a drive like C:\xxxx or if the path begins with two backslashes.
A single backslash will return false.  This method will return true for a single
backslash at the front of a string on Windows.
*/
public static boolean isAbsolute ( String path ) {
	if ( !isUNIXMachine() && path.startsWith("\\") ) {
		// UNC will match this, as well as normal paths.
		return true;
	}
	// Use the standard method.
	File f = new File ( path );
	return f.isAbsolute();
}

/**
Checks to see if the given port is open.  Open ports can be used.
Ports that are not open are already in use by some other process.
@param port the port number to check.
@return whether the port is open or not.
*/
public static boolean isPortOpen(int port) {
	try {
		(new ServerSocket(port)).close();
		(new ServerSocket(port)).close();
		return true;
	}
	catch (Exception e) {
		return false;
	}
}

/**
Returns whether the program is running as an applet (must be set with setRunningApplet).
*/
public static boolean isRunningApplet() {
	return __runningApplet;
}

/**
Determine if a UNIX machine.  The following seem to be standard,
although there are variations depending on hardware and OS version:
<pre>
Operating System      os.arch    os.name          os.version
Mac OS X              x86_64     "Mac OS X"       10.6.4
Windows 95            x86        "Windows 95"     4.0
Windows 98            x86        "Windows 95"     4.1
NT                    x86        "Windows NT"     4.0
Windows 2000          x86        "Windows NT"     5.0
Linux                 i386       "Linux"
HP-UX                 PA-RISC
</pre>
@return true if a UNIX platform, including os.name of Linux, false if not (presumably Windows).
*/
public static boolean isUNIXMachine () {
	String arch = System.getProperty ( "os.arch" ).toUpperCase();
	String name = System.getProperty ( "os.name" ).toUpperCase();
	if ( arch.equals("UNIX") || arch.equals("PA-RISC") ||
		name.equals("LINUX") || name.startsWith("MAC OS X") ) {
		return true;
	}
	else {
	    return false;
	}
}

/**
Count the number of lines in a file.
@param file	File to read.
@return the number of lines in the file.
@exception IOException if there is an error.
*/
public static int lineCount ( File file)
throws IOException {
	String message, routine = IOUtil.class.getSimpleName() + ".lineCount", tempstr;

	if ( file == null ) {
		message = "Filename is null.";
		//Message.printWarning ( 10, routine, message );
		throw new IOException ( message );
	}
	if ( file.getAbsoluteFile().length() == 0 ) {
		message = "Filename is empty.";
		//Message.printWarning ( 10, routine, message );
		throw new IOException ( message );
	}

	// Open the file.

	BufferedReader fp = null;
	try {
	    fp = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream( file.getAbsolutePath()) ));
	}
	catch ( Exception e ) {
		message = "Unable to read file \"" + file.getAbsolutePath() + "\" (" + e + ").";
		Message.printWarning ( 3, routine, message );
		throw new IOException ( message );
	}

	int count = 0;
	while ( true ) {
		tempstr = fp.readLine();
		if ( tempstr == null ) {
			break;
		}
		++count;
	}
	fp.close ();
	return count;
}

/**
Count the number of pattern matches in a file.
@param file	File to read and convert to string list.
@param pattern string with Java regular expression
@param boolean countLines if true, return the number of lines with matches, if false (currently not supported),
return the number of pattern matches (more than one match per line is allowed)
@return the number of pattern matches in the file (can be more than one match on a line).
@exception IOException if there is an error.
*/
public static int matchCount ( File file, String pattern, boolean countLines )
throws IOException {
	String message, tempstr;

	if ( file == null ) {
		message = "Filename is null.";
		//Message.printWarning ( 10, routine, message );
		throw new IOException ( message );
	}
	if ( file.getAbsoluteFile().length() == 0 ) {
		message = "Filename is empty.";
		//Message.printWarning ( 10, routine, message );
		throw new IOException ( message );
	}

	// Open the file.

	BufferedReader fp = null;
	try {
	    fp = new BufferedReader ( new InputStreamReader(IOUtil.getInputStream( file.getAbsolutePath()) ));
	}
	catch ( Exception e ) {
		String routine = IOUtil.class.getSimpleName() + ".matchCount";
		message = "Unable to read file \"" + file.getAbsolutePath() + "\" (" + e + ").";
		Message.printWarning ( 3, routine, message );
		throw new IOException ( message );
	}

	int count = 0;
	while ( true ) {
		tempstr = fp.readLine();
		if ( tempstr == null ) {
			break;
		}

		if ( countLines ) {
			if ( tempstr.matches(pattern) ) {
				++count;
			}
		}
		else {
		}
	}
	fp.close ();
	return count;
}

/**
Sets an array and all its elements to null (for garbage collection).
Care should be taken to ensure that this method is NOT used with static data,
as the finalize method of any instance of an Object will clear the static data for all instances of the Object.
@param array the array to null.  Can already be null.
*/
public static void nullArray(Object[] array) {
	if (array == null) {
		return;
	}
	int size = array.length;
	for (int i = 0; i < size; i++) {
		array[i] = null;
	}
}

/**
Open the resource identified by the URL using the appropriate application for the operating system and user environment.
On Windows, determine the default application using the file extension (e.g., "html" will result in a web browser).
On UNIX/Linux, a web browser is always used.
@param url URL to open.
@deprecated use the java.awt.Desktop class
*/
@Deprecated
public static void openURL(String url) {
    try {
        Desktop desktop = Desktop.getDesktop();
        desktop.browse ( new URI(url) );
    }
    catch ( Exception e ) {
    	Message.printWarning(2, "IOUtil.openURL", "Could not open application to view to URL \"" + url +
    	    "\" (" + e + ").");
    }
}

/**
Print a standard header to a file.  See the overloaded method for more information.
It is assumed that the file is not an XML file.
@param ofp PrintWriter that is being written to.
@param comment0 The String to use for comments.
@param maxwidth The maximum length of a line of output (if whitespace is embedded in the header information,
lines will be broken appropriately to fit within the specified length.
@param flag Currently unused.
@return 0 if successful, 1 if not.
*/
public static int printCreatorHeader ( PrintWriter ofp, String comment0, int maxwidth, int flag ) {
	return printCreatorHeader ( ofp, comment0, maxwidth, flag, null );
}

/**
Print a header to a file.  The header looks like the following:
<p>
<pre>
# File generated by
# program:   demandts 2.7 (25 Jun 1995)
# user:      sam
# date:      Mon Jun 26 14:49:18 MDT 1995
# host:      white
# directory: /crdss/dmiutils/demandts/data
# command:   ../src/demandts -d1 -w1,10 -demands -istatemod
#            /crdss/statemod/data/white/white.ddh -icu
#            /crdss/statemod/data/white/white.ddc -sstatemod
#            /crdss/statemod/data/white/white.dds -eff12
</pre>
<p>
@param ofp PrintWriter that is being written to.
@param commentLinePrefix The string to use for the start of comment lines (e.g., "#").
@param maxwidth The maximum length of a line of output (if whitespace is embedded in the header information,
lines will be broken appropriately to fit within the specified length.
@param flag Currently unused.
@param props Properties used to format the header.
Currently the only property that is recognized is "IsXML", which can be "true" or "false".
XML files must be handled specifically because some characters that may be printed
to the header may not be handled by the XML parser.
The opening and closing XML tags must be added before and after calling this method.
@return 0 if successful, 1 if not.
*/
public static int printCreatorHeader ( PrintWriter ofp, String commentLinePrefix, int maxwidth, int flag, PropList props ) {
	boolean isXml = false;
	// Figure out properties.
	if ( props != null ) {
		String prop_value = props.getValue ( "IsXML" );
		if ( (prop_value != null) && prop_value.equalsIgnoreCase("true") ) {
			isXml = true;
			// If XML, do not print multiple dashes together in the comments below.
		}
	}

	if ( ofp == null ) {
		String routine = IOUtil.class.getSimpleName() + ".printCreatorHeader";
		Message.printWarning ( 2, routine, "Output file pointer is NULL" );
		return 1;
	}

	if ( !_initialized ) {
		initialize ();
	}

	// Get the formatted header comments.

	List<String> comments = formatCreatorHeader ( commentLinePrefix, maxwidth, isXml );

	for ( String c: comments ) {
	    ofp.println(c);
	}
    ofp.flush ();
	return 0;
}

/**
Print a list of strings to a file.  The file is created, opened, and closed.
@param file name of file to write.
@param strings list of strings to write.
*/
public static void printStringList ( String file, List<String> strings )
throws IOException {
	PrintWriter	ofp;

	// Open the file.

	try {
	    ofp = new PrintWriter ( new FileOutputStream(file) );
	}
	catch ( Exception e ) {
		String routine = IOUtil.class.getSimpleName() + ".printStringList";
		String message = "Unable to open output file \"" + file + "\"";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
	try {
	    printStringList ( ofp, strings );
	}
	finally {
      	// Flush and close the file.
    	ofp.flush();
    	ofp.close();
	}
}

/**
Print a list of strings to an opened file.
@param ofp PrintWrite to write to.
@param strings list of strings to write.
*/
public static void printStringList ( PrintWriter ofp, List<String> strings )
throws IOException {
	if ( strings == null ) {
		return;
	}
	int size = strings.size();
	for ( int i = 0; i < size; i++ ) {
		ofp.println ( strings.get(i) );
	}
}

/**
@deprecated Use the overloaded version that uses List parameters.
@param oldFile
@param newFile
@param newComments
@param commentIndicators
@param ignoredCommentIndicators
@param flags
@return PrintWriter to allow additional writing to the file.
*/
@Deprecated
public static PrintWriter processFileHeaders ( String oldFile, String newFile,
		String [] newComments, String [] commentIndicators, String [] ignoredCommentIndicators, int flags ) {
	List<String> newCommentList = null;
	List<String> commentIndicatorList = null;
	List<String> ignoreCommentIndicatorList = null;
	if ( newComments != null ) {
		newCommentList = StringUtil.toList(newComments);
	}
	if ( commentIndicators != null ) {
		commentIndicatorList = StringUtil.toList(commentIndicators);
	}
	if ( ignoredCommentIndicators != null ) {
		ignoreCommentIndicatorList = StringUtil.toList(ignoredCommentIndicators);
	}
	return processFileHeaders ( oldFile, newFile,
		newCommentList, commentIndicatorList, ignoreCommentIndicatorList, flags );
}

/**
This method should be used to process the header of a file that is going through
revisions over time.  It can be used short of full revision control on the file.
The old file header will be copied to the new file using special comments (assume # is comment):
<p>

<pre>
#HeaderRevision 1
</pre>
<p>

Where the number indicates the revision for the header.  The initial header will be number 0.
@return PrintWriter for the file (it will be opened and processed so that the
new file header consists of the old header with new comments at the top).
The file can then be written to.  Return null if the new file cannot be opened.
@param oldFile An existing file whose header is to be updated.
@param newFile The name of the new file that is to contain the updated header
(and will be pointed to by the returned PrintWriter (it can be the same as
"oldfile").  If the name of the file ends in XML then the file is assumed to
be an XML file and the header is wrapped in <!-- --> (this may change to actual XML tags in the future).
@param newComments list of strings to be added as comments in the new revision (often null).
@param commentIndicators list of strings that indicate comment lines that should be retained in the next revision.
@param ignoredCommentIndicators list of strings that indicate comment lines that
can be ignored in the next revision (e.g., lines that describe the file format that only need to appear once).
@param flags Currently unused.
*/
public static PrintWriter processFileHeaders ( String oldFile, String newFile, List<String> newComments,
		List<String> commentIndicators, List<String> ignoredCommentIndicators, int flags ) {
	String comment;
	String routine = IOUtil.class.getSimpleName() + ".processFileHeaders";
	FileHeader oldheader;
	PrintWriter	ofp = null;
	int dl = 50, i, header_last = -1, header_revision, wl = 20;
	boolean is_xml = false;

	// Get the old file header.

	if ( oldFile == null ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "NULL old file - no old header" );
		}
		oldheader = null;
	}
	else if ( oldFile.length() == 0 ) {
		Message.printWarning ( dl, routine, "Empty old file - no old header" );
		oldheader = null;
	}
	else {
	    // Try to get the header.
		oldheader = getFileHeader (	oldFile, commentIndicators, ignoredCommentIndicators, 0 );
		if ( oldheader != null ) {
			header_last = oldheader.getHeaderLast();
		}
	}

	// Open the new output file.

	try {
	    ofp = new PrintWriter ( new FileOutputStream(newFile) );
		if ( StringUtil.endsWithIgnoreCase(newFile,".xml") ) {
			is_xml = true;
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		Message.printWarning ( wl, routine, "Unable to open output file \"" + newFile + "\"" );
		return null;
	}

	// Print the new file header.  If a comment string is not specified, use the default.

	if ( (commentIndicators == null) || (commentIndicators.size() == 0) ) {
		comment = UNIVERSAL_COMMENT_STRING;
	}
	else {
	    comment = (String)commentIndicators.get(0);
	}
	header_revision = header_last + 1;
	if ( is_xml ) {
		ofp.println ( "<?xml version=\"1.0\" encoding=\"utf-8\"?>" );
		ofp.println ( "<!--" );
	}
	ofp.println ( comment + HEADER_REVISION_STRING + " " + header_revision);
	ofp.println ( comment );

	// Now print the standard header.

	PropList props = new PropList ( "header" );
	if ( is_xml ) {
		props.set ( "IsXML=true" );
	}
	printCreatorHeader ( ofp, comment, 80, 0, props );

	// Now print essential comments for this revision.  These strings do not have the comment prefix.

	if ( newComments != null ) {
		if ( newComments.size() > 0 ) {
			if ( is_xml ) {
				ofp.println ( comment );
			}
			else {
			    ofp.println ( comment + "----" );
			}
			for ( i = 0; i < newComments.size(); i++ ) {
				ofp.println ( comment + " " + newComments.get(i) );
			}
		}
	}

	if ( is_xml ) {
		ofp.println ( comment );
	}
	else {
	    ofp.println ( comment +	"------------------------------------------------" );
	}

	// Now print the old header.  It already has the comment character.

	if ( oldheader != null ) {
		if ( oldheader.size() > 0 ) {
			for ( i = 0; i < oldheader.size(); i++ ) {
				ofp.println ( oldheader.elementAt(i) );
			}
		}
	}

	if ( is_xml ) {
		ofp.println ( "-->" );
	}
	return ofp;
}

/**
Read a response given a URL string.  If the response code is >= 400 the result is read from the error stream.
Otherwise, the response is read from the input stream.
See the UrlReader class as a more robust option.
@return the string read from a URL.
@param urlString the URL to read from.
@deprecated use the UrlReader class.
*/
@Deprecated
public static String readFromURL ( String urlString )
throws MalformedURLException, IOException {
    URL url = new URL ( urlString );
    // Open the input stream.
    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
    InputStream in = null;
    if ( urlConnection.getResponseCode() >= 400 ) {
        in = urlConnection.getErrorStream();
    }
    else {
        in = urlConnection.getInputStream();
    }
    InputStreamReader inp = new InputStreamReader(in);
    BufferedReader reader = new BufferedReader(inp);
    char[] buffer = new char[8192];
    int len1 = 0;
    StringBuffer b = new StringBuffer();
    while ( (len1 = reader.read(buffer)) != -1 ) {
        b.append(buffer,0,len1);
    }
    in.close();
    urlConnection.disconnect();
    return b.toString();
}

/**
Replaces old file extension with new one.
If the file has no extension then it adds the extension specified.
@param file File name to change extension on.
@param extension New file extension.
@return file_new New file name with replaced extension.
@throws IOException
*/
public static String replaceFileExtension( String file, String extension )
throws IOException {
	// First make sure the file is an absolute value this makes it easier to check and replace.
	File tmp = new File(file);
	if ( !tmp.isAbsolute() ) {
		file = tmp.getCanonicalPath().toString();
	}
	tmp = null;

	// Add a period to the beginning of the extension if one doesn't exist already.
	if ( !(extension.startsWith(".")) ) {
		extension = "." + extension;
	}
	List<String> v = StringUtil.breakStringList ( file, ".", 0 );
	if ( (v == null) || (v.size() == 0) ) {
		// Didn't have an extension so add one and return it.
		if ( file != null && file.length() > 0 ) {
			return file += extension;
		}
	}
	String file_new = "";
	for( int i = 0; i < v.size() - 1; i++ ) {
		file_new += v.get( i );
	}
	// Add the new extension.
	file_new += extension;
	return file_new;
}

/**
Set the applet for a program.
This is generally called from the init() method of an application.
This method then saves the AppletContext and DocumentBase for later use.
After calling with a non-null Applet, isApplet() will return true.
@param applet The Applet for the application.
@see #isApplet
@see #getApplet
@see #getAppletContext
@see #getDocumentBase
*/
public static void setApplet ( Applet applet ) {
	if ( applet != null ) {
		_applet = applet;
		_applet_context = applet.getAppletContext();
		_document_base = applet.getDocumentBase();
		_is_applet = true;
	}
	// Do this after setting the applet so that the initialization can check.
	if ( !_initialized ) {
		initialize ();
	}
}

/**
Set the AppletContext.  This is generally only called from low-level code (normally just need to call setApplet()).
@param applet_context The AppletContext for the current applet.
@see #setApplet
@see #getAppletContext
*/
public static void setAppletContext ( AppletContext applet_context ) {
	_applet_context = applet_context;
}

/**
Sets the application home directory.
This is a base directory that should only be set once during an application run.
It is the base from which log files, system files, etc, can be located.
For instance, for CDSS TSTool the application home is set to C:\CDSS\TSTool-Version.
Other directories under this include "system" and "logs".
@param homeDir the home directory to set.
*/
public static void setApplicationHomeDir(String homeDir) {
	if (!_initialized) {
		initialize();
	}

	if (homeDir != null) {
		homeDir = homeDir.trim();
		// Remove the trailing directory separator.
		if (homeDir.endsWith(File.separator)) {
			homeDir = homeDir.substring(0, (homeDir.length() - 1));
		}

		// For windows-based machines:
		if (File.separator.equals("\\")) {
			// On DOS.
			if (homeDir.startsWith("\\\\")) {
				// UNC path -- leave as is.
			}
			else if (homeDir.charAt(1) != ':') {
				// homeDir does not start with a drive letter.
				// Get the drive letter of the current working dir and use it instead.
				// Since working dir is initialized to the java working dir when IOUtil is first used,
				// _working_dir will always have a drive letter for windows machines.
				char drive = _working_dir.charAt(0);
				homeDir = drive + ":" + homeDir;
			}
		}
		__homeDir = homeDir;
	}
}

/**
 * Set the list of application plugin classpath folders.
 * @param applicationClasspathList a list of classpath jar files and folders with "/*" that
 * are dynamically determined from plugins.
 */
public static void setApplicationPluginClasspath ( List<String> applicationPluginClasspathList ) {
	IOUtil.applicationPluginClasspathList = applicationPluginClasspathList;
}

/**
Set the program arguments.
This is generally only called from low-level code (normally just need to call setProgramData()).  A copy is saved.
@param argv Program arguments.
@see #setProgramData
*/
public static void setProgramArguments ( String argv[] ) {
	if ( !_initialized ) {
		initialize ();
	}

	if ( argv == null ) {
		// No arguments - initialize to avoid null pointer exceptions.
		_argv = new String[0];
		return;
	}

	// Create a copy of the command-line arguments.
	int length = argv.length;
	if ( length > 0 ) {
		_argv = new String[length];
		for ( int i = 0; i < length; i++ ) {
			_argv[i] = argv[i];
		}
	}
}

/**
Set the program command file.  This is generally only called from low-level
command parsing code (e.g. RTi.HydroSolutions.DSSApp.parseCommands()).
@param command_file Command file to use with the program.
@see #getProgramCommandFile
@deprecated utilize command processors to get comments suitable for files
*/
@Deprecated
public static void setProgramCommandFile ( String command_file ) {
	if ( !_initialized ) {
		initialize ();
	}
	if ( command_file != null ) {
		_command_file = command_file;
	}
}

/**
Set the program main data, which can be used later for GUI labels, etc.
This is generally called from the main() or init() function of an application (or from application base classes).
@param progname The program name.
@param progver The program version, used in Help About and other information,
can be a semantic version, version with date, etc., but version should always be the first string.
@param argv The program command-line arguments (ignored if an Applet).
@see #getProgramName
@see #getProgramVersion
@see #getProgramArguments
*/
public static void setProgramData ( String progname, String progver, String argv[] ) {
	if ( !_initialized) {
		initialize ();
	}
	setProgramName ( progname );
	setProgramVersion ( progver );
	setProgramArguments ( argv );
}

/**
Set the host name on which the program is running.
@param host The host (machine) name.
@see #getProgramHost
*/
public static void setProgramHost ( String host ) {
	if ( !_initialized ) {
		initialize ();
	}
	if ( host != null ) {
		_host = host;
	}
}

/**
Set the program name.
@param progname The program name.
@see #getProgramName
*/
public static void setProgramName ( String progname ) {
	if ( !_initialized ) {
		initialize ();
	}
	if ( progname != null ) {
		_progname = progname;
	}
}

/**
Set the program user.  This is usually called from within IO by checking system properties.
@param user The user name.
@see #getProgramUser
*/
public static void setProgramUser ( String user ) {
	if ( !_initialized ) {
		initialize ();
	}
	if ( user != null ) {
		_user = user;
	}
}

/**
Set the program version, used in Help About dialogs and checking for version requirements.
@param progver The program version.
@see #getProgramVersion
*/
public static void setProgramVersion ( String progver ) {
	if ( !_initialized ) {
		initialize ();
	}
	if ( progver != null ) {
		_progver = progver;
	}
}

/**
Set the program working directory.  It does not cause a directory change.
This method may be called, for example, when a GUI program applies an artificial directory change.
Java does not allow a change in the working directory but by setting here the application is
indicating that relative paths should be relative to this directory.
The value of the working directory should be an absolute path if from a GUI to ensure
that the correct absolute path to files can be determined.
The default working directory is the directory in which the application started.
This is often reset soon by an application to indicate a "home" directory where work occurs.
@param working_dir The program working directory.  The trailing directory delimiter will be removed if specified.
Currently, working_dir must be an absolute path (e.g., as taken from a file chooser).
If not, the given directory is prepended with the previous drive letter if a Windows machine.
In the future, a relative path (e.g., "..\xxxx") may be allowed, in which case,
the previous working directory will be adjusted.
@see #getProgramWorkingDir
*/
public static void setProgramWorkingDir ( String working_dir ) {
	if ( !_initialized ) {
		initialize ();
	}
	if ( working_dir != null ) {
		working_dir = working_dir.trim();
		if ( working_dir.endsWith(File.separator) ) {
			working_dir = working_dir.substring(0,(working_dir.length() - 1) );
		}

		// For windows-based machines:
		if (File.separator.equals("\\")) {
			// On DOS:
			if (working_dir.startsWith("\\\\")) {
				// UNC drive -- leave as is.
			}
			else if (working_dir.charAt(1) != ':') {
				// working_dir does not start with a drive letter.
				// Get the drive letter of the current working dir and use it instead.
				// Since working dir is initialized to the java working dir when IOUtil is first used,
				// _working_dir will always have a drive letter for windows machines.
				char drive = _working_dir.charAt(0);
				working_dir = drive + ":" + working_dir;
			}
		}
		_working_dir = working_dir;
	}
}

/**
Set a property in the global PropListManager.  This sets the value in the un-named PropList.
*/
public static void setProp ( String key, Object prop ) {
	if ( !_initialized ) {
		initialize ();
	}
	// Set in the first list.
	_prop_list_manager.setValue ( "", key, prop );
}

/**
Sets whether the program is running as an applet.
*/
public static void setRunningApplet(boolean applet) {
	if (!_initialized) {
		initialize();
	}
	__runningApplet = applet;
}

/**
Determine a unique temporary file name, using the system clock.
On UNIX, temporary files are created in /tmp.  On PCs, temporary files are created in C:/TEMP.
The file may theoretically be grabbed by another application but this is unlikely.
If using Java 1.2x, can use the File.createTempFile() method instead.
@return Full path to an unused temporary file.
*/
public static String tempFileName() {
    return tempFileName ( null, null );
}

/**
Determine a unique temporary file name, using the system clock milliseconds.
The system property java.io.tmpdir is used to determine the folder.
The file may theoretically be grabbed by another
application but this is unlikely since the filename is based on the current time
@param prefix prefix to filename, in addition to temporary pattern, or null for no prefix.
@param extension extension to filename (without leading .), or null for no extension.
@return Full path to an unused temporary file.
*/
public static String tempFileName( String prefix, String extension ) {
	// Get the prefix.
	String dir = null;
	if ( prefix == null ) {
	    prefix = "";
	}
	if ( extension == null ) {
	    extension = "";
	}
	else if ( !extension.startsWith(".") ) {
	    extension = "." + extension;
	}
    String [] tmpdirs = null;
	if ( isUNIXMachine() ) {
	    tmpdirs = new String[3];
	    tmpdirs[0] = System.getProperty("java.io.tmpdir");
	    tmpdirs[1] = "/tmp";
	    tmpdirs[2] = "/var/tmp";
	}
	else {
	    tmpdirs = new String[3];
	    tmpdirs[0] = System.getProperty("java.io.tmpdir");
	    tmpdirs[1] = "C:\\tmp";
	    tmpdirs[2] = "C:\\temp";
	}
	for ( int i = 0; i < tmpdirs.length; i++ ) {
        String dir2 = tmpdirs[i];
	    File f = new File(dir2);
	    if ( f.exists() && f.isDirectory() ) {
	        // Found a folder that will work.
	        dir = dir2;
	        break;
	    }
	}
	if ( dir == null ) {
	    // Should hopefully never happen.
	    throw new RuntimeException ( "Cannot determine temporary file location." );
	}
	// Use the date as a seed and make sure the file does not exist.
	String filename = null;
	while ( true ) {
		Date d = new Date();
		if ( dir.endsWith(File.separator) ) {
		    filename = dir + prefix + d.getTime() + extension;
		}
		else {
		    filename = dir + File.separator + prefix + d.getTime() + extension;
		}
		if ( !fileExists(filename) ) {
			break;
		}
	}
	File finalName = new File(filename);
	try {
	    // Do this to unmangle Windows paths.
	    return finalName.getCanonicalPath();
	}
	catch ( IOException e ) {
	    // Just go with mangled name.
	    return filename;
	}
}

/**
Set whether the application is being run in test mode.
The testing() method can be called to check the value.
An appropriate way to use this functionality is to check for a -test command line argument.
If present, call IOUtil.testing(true).  Later, check the value with IOUtil.testing().
This is useful for adding GUI features or expanded debugging only for certain parts of the code that are being tested.
@param is_testing true if the application is being run in test mode (default initial value is false).
@return the value of the testing flag, after being set.
*/
public static boolean testing ( boolean is_testing ) {
	_testing = is_testing;
	return _testing;
}

/**
Determine whether the application is being run in test mode.  See overloaded method for more information.
@return the value of the testing flag, after being set.
*/
public static boolean testing () {
	return _testing;
}

/**
Convert a path and an absolute directory to an absolute path.
@param dir Directory to prepend to path.
@param path Path to append to dir to create an absolute path.
If absolute, it will be returned.  If relative, it will be appended to dir.
If the path includes "..", the directory will be truncated before appending the non-".." part of the path.
*/
public static String toAbsolutePath ( String dir, String path ) {
	File f = new File ( path );
	if ( f.isAbsolute() ) {
		return path;
	}
	// Loop through the "path".  For each occurrence of "..", knock a directory off the end of the "dir".

	// Always trim any trailing directory separators off the directory paths.
	while (dir.length() > 1 && dir.endsWith(File.separator)) {
		dir = dir.substring(0, dir.length() - 1);
	}
	while (path.length() > 1 && path.endsWith(File.separator)) {
		path = path.substring(0, path.length() - 1);
	}

	int path_length = path.length();
	String sep = File.separator;
	int pos;
	for ( int i = 0; i < path_length; i++ ) {
		if ( path.startsWith("./") || path.startsWith(".\\") ) {
			// No need for this in the result.
			// Adjust the path and evaluate again.
			path = path.substring(2);
			i = -1;
			path_length -= 2;
		}
		if ( path.startsWith("../") || path.startsWith("..\\") ) {
			// Remove a directory from each path.
			pos = dir.lastIndexOf(sep);
			if ( pos >= 0 ) {
				// This will remove the separator.
				dir = dir.substring(0,pos);
			}
			// Adjust the path and evaluate again.
			path = path.substring(3);
			i = -1;
			path_length -= 3;
		}
		else if (path.equals("..")) {
			// Remove a directory from each path.
			pos = dir.lastIndexOf(sep);
			if (pos >= 0) {
				dir = dir.substring(0, pos);
			}
			// Adjust the path and evaluate again.
			path = path.substring(2);
			i = -1;
			path_length -= 2;
		}
	}

	return dir + File.separator + path;
}

/**
 * Convert a file path to a portable path.
 * In particular, this is used to convert Windows paths that can cause problems with backslashes
 * (C:\a\b\c.xxx) to a path with forward slashes that are recognized by Python, R, etc. (c:/a/b/c.xxx).
 * @param path to be converted to portable path.  If already portable (no backslashes detected), return as is.
 * @return portable path, where backslashes are converted to forward slashes.
 * If the input string is null, null will be returned.
 */
public static String toPortablePath ( String path ) {
	if ( path == null ) {
		return null;
	}
	else {
		// Replace \ with /.
		return path.replace('\\', '/');
	}
}

/**
 * Convert a file path to a POSIX path.
 * This is used to convert Windows paths that can cause problems with drive letters and backslashes (C:\a\b\c.xxx) to
 * a path with forward slashes that are recognized by Python, R, etc. (/c/a/b/c.xxx).
 * If the drive is not included then the leading slash is omitted.
 * @param path to be converted to POSIX path.  If already POSIX (no backslashes detected), return as is.
 * @return POSIX path, where backslashes are converted to forward slashes and Windows drive is converted to first folder.
 * If the input string is null, null will be returned.
 */
public static String toPosixPath ( String path ) {
	if ( path == null ) {
		return null;
	}
	else {
		if ( Character.isAlphabetic(path.charAt(0)) && (path.charAt(1) == ':') ) {
			// Convert C:... to /C/ by removing colon (next step will deal with backslashes).
			path = "/" + path.replace(":", "");
		}
		// Replace \ with /.
		return path.replace('\\', '/');
	}
}

/**
Convert a path "path" and an absolute directory "dir" to a relative path.
If "dir" is at the start of "path" it is removed.
If it is not present, an exception is thrown.
For example, a "dir" of \a\b\c\d\e and a "path" of \a\b\c\x\y will result in ..\..\x\y.<p>
The strings passed in to this method should not end with a file separator
(either "\" or "/", depending on the system).  If they have a file separator,
the separator will be trimmed off the end.
<br>
There are four conditions for which to check:<ul>
<li>The directories are exactly the same ("\a\b\c" and "\a\b\c")</li>
<li>The second directory is farther down the same branch that the first directory is on ("\a\b\c\d\e" and "\a\b\c").<li>
<li>The second directory requires a backtracking up the branch on which the first directory is on ("\a\b\c\d" and "\a\b\c\e" or "\a\b\c\d" and "\g")</li>
<li>For DOS: the directories are on different drives.</li>
<br>
This method will do error checking to make sure the directories passed in to it are not null or empty,
but apart from that does no error-checking to validate proper directory naming structure.
This method will fail with improper directory names (e.g., "C:\\c:\\\\\\\\test\\\\").
@param rootDir the root directory from which to build a relative directory.
@param relDir the directory for which to create the relative directory path from the rootDir.
@return the relative path created from the two directory structures.
This path will NOT have a trailing directory separator (\ or /).
If both the rootDir and relDir are the same, for instance, the value "." will be returned.
Plus the directory separator, this becomes ".\" or "./".
@exception Exception if the conversion cannot occur.
Most likely will occur in DOS when the two directories are on different drives.
Will also be thrown if null or empty strings are passed in as directories.
*/
public static String toRelativePath (String rootDir, String relDir)
throws Exception {
	// Do some simple error checking.
	if (rootDir == null || rootDir.trim().equals("")) {
		throw new Exception ("Bad rootDir (" + rootDir + ") passed in to IOUtil.toRelativePath()");
	}
	if (relDir == null || relDir.trim().equals("")) {
		throw new Exception ("Bad relDir (" + relDir + ") passed in to IOUtil.toRelativePath()");
	}

	String sep = File.separator;

	boolean unix = true;

	if (sep.equals("\\")) {
		unix = false;
		// This is running on DOS.
		// Check to see if the drive letters are the same for each directory -- if they aren't,
		// the second directory can't be converted to a relative directory.
		char drive1 = rootDir.toLowerCase().charAt(0);
		char drive2 = relDir.toLowerCase().charAt(0);

		if (drive1 != drive2) {
			throw new Exception ( "Cannot adjust \"" + relDir +
				" to relative using directory \"" + rootDir + "\"");
		}
	}

	// Always trim any trailing directory separators off the directory paths.
	while (rootDir.length() > 1 && rootDir.endsWith(File.separator)) {
		rootDir = rootDir.substring(0, rootDir.length() - 1);
	}
	while (relDir.length() > 1 && relDir.endsWith(File.separator)) {
		relDir = relDir.substring(0, relDir.length() - 1);
	}

	// Check to see if the two paths are the same.
	if ((unix && rootDir.equals(relDir)) || (!unix && rootDir.equalsIgnoreCase(relDir))) {
		return ".";
	}

	// Check to see if the relDir dir is farther up the same branch that the rootDir is on.

	if ((unix && relDir.startsWith(rootDir)) || (!unix && StringUtil.startsWithIgnoreCase(relDir, rootDir))){

		// At this point, it is known that relDir is longer than rootDir.
		String c = "" + relDir.charAt(rootDir.length());

		if (c.equals(File.separator)) {
			String higher = relDir.substring(rootDir.length());
			if (higher.startsWith(sep)) {
				higher = higher.substring(1);
			}
			return higher;
		}
	}

	// If none of the above were triggered, then the second directory
	// is higher up the first directory's directory branch.

	// Get the final directory separator from the first directory,
	// and then start working backwards in the string to find where the
	// second directory and the first directory share directory information.
	int start = rootDir.lastIndexOf(sep);
	int x = 0;
	for (int i = start; i >= 0; i--) {
		String s = String.valueOf(rootDir.charAt(i));

		if (!s.equals(sep)) {
			// Do nothing this iteration.
		}
		else if ((unix && relDir.regionMatches(false, 0, rootDir + sep, 0, i + 1))
			|| (!unix && relDir.regionMatches(true,0,rootDir + sep, 0, i+1))){
			// A common "header" in the directory name has been found.
			// Count the number of separators in each directory to determine how much separation lies between the two.
			int dir1seps = StringUtil.patternCount(rootDir.substring(0, i), sep);
			int dir2seps = StringUtil.patternCount(rootDir, sep);
			x = i + 1;
			if (x > relDir.length()) {
				x = relDir.length();
			}
			String uncommon = relDir.substring(x, relDir.length());
			int steps = dir2seps - dir1seps;
			if (steps == 1) {
				if (uncommon.trim().equals("")) {
					return "..";
				}
				else {
					return ".." + sep + uncommon;
				}
			}
			else {
				if (uncommon.trim().equals("")) {
					uncommon = "..";
				}
				else {
					uncommon = ".." + sep + uncommon;
				}
				for (int j = 1; j < steps; j++) {
					uncommon = ".." + sep + uncommon;
				}
			}
			return uncommon;
		}
	}

	return relDir;
}

/**
Verify that a path is appropriate for the operating system.
This is a simple method that does the following:
<ol>
<li>    If on UNIX/LINUX, replace all "\" characters with "/".  WARNING - as implemented,
        this will convert UNC paths to forward slashes.</li>
<li>    If on Windows, do nothing.  Java automatically handles "/" in paths.</li>
</ol>
@return A path to the file that uses separators appropriate for the operating system.
*/
public static String verifyPathForOS ( String path ) {
    return verifyPathForOS ( path, false );
}

/**
Verify that a path is appropriate for the operating system.
This is a simple method that does the following:
<ol>
<li>    If on UNIX/LINUX, replace all "\" characters with "/".  WARNING - as implemented,
        this will convert UNC paths to forward slashes.</li>
<li>    If on Windows, do nothing (unless force=true).  Java automatically handles "/" in paths.</li>
</ol>
@param force always do the conversion (on Windows this will always convert // to \ - this should probably be
the default behavior but make it an option since this has not always been the behavior of this method (see overload).
Force should be used, for example, when using URL paths that require forward slashes.
@return A path to the file that uses separators appropriate for the operating system.
*/
public static String verifyPathForOS ( String path, boolean force ) {
    if ( path == null ) {
        return path;
    }
    if ( isUNIXMachine() ) {
        return ( path.replace ( '\\', '/' ) );
    }
    else {
        if ( force ) {
            // Even on windows force it although it does not seem to be necessary in most cases.
            return ( path.replace ( '/', '\\' ) );
        }
        else {
            // Just return... paths on Windows can have / or \ and still work.
            return path;
        }
    }
}

/**
Write a file.
@param filename Name of file to write.
@param contents Contents to write to file.  It is assumed that the contents
contains line break characters.
*/
public static void writeFile ( String filename, String contents )
throws IOException {
	BufferedWriter fp = null;
	try {
	    fp = new BufferedWriter ( new FileWriter( filename ));
		fp.write ( contents );
		fp.close ();
	}
	catch ( Exception e ) {
		String routine = IOUtil.class.getSimpleName() + ".writeFile";
		String message = "Unable to open file \"" + filename + "\"";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
}

}