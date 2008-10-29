// ----------------------------------------------------------------------------
// IOUtil - input/output functions
// ----------------------------------------------------------------------------
// Notes:	(1)	This class contains public static RTi input/output
//			utility functions.  They are essentially equivalent to
//			the HMData C library routines, except for overloading
//			and use of Java classes.
//		(2)	This class is not meant to be initialized as an intance
//			of an object.  It manages its own data.  Use this class
//			to store useful program information.
// ----------------------------------------------------------------------------
// History:
//
// ? Jan 1998	Steven A. Malers, RTi	Work subsequent to this date has
//					involved pulling in legacy code from
//					RTi's libraries.
// 28 Jan 1998	SAM, RTi		Update to include the getInputStream()
//					function which is an intelligent way
//					to get an input stream from a file name
//					or URL.
// 14 Mar 1998	SAM, RTi		Add javadoc.
// 02 Apr 1998  DLG, RTi		Added getFileSeparator().
// 07 May 1998  DLG, RTi		Added getVendor().
// 03 Aug 1998	SAM, RTi		Add printStringList to match legacy
//					code.
// 16 Nov 1998	SAM, RTi		Add getFilesFromPathList,
//					fileToStringList.
// 30 Nov 1998	SAM, RTi		Add writeFile.
// 02 Feb 1999	SAM, RTi		Update processFileHeaders - previously
//					checked for comment anywhere in line.
//					Now checks beginning of line.
// 11 Mar 1999	SAM, RTi		Change so initialization messages are
//					not printed to standard output.
// 24 May 1999	CEN, RTi		Added check to processFileHeaders
//					to make sure the oldheader returned 
//					from getFileHeader is not null
// 27 May 1999	SAM, RTi		Change so fileToStringList can handle
//					a URL (for applets).  Change to catch
//					Exception rather than IOException in
//					many cases.
// 07 Jan 2001	SAM, RTi		Copy IO to this IOUtil class.  Deprecate
//					all the IO methods and point to this
//					class.  The IOUtil name is consistent
//					with other classes and the C++ versios.
//					Clean up javadoc and optimize memory
//					use (set unused to null).  Change some
//					data and methods to protected, public
//					to allow IO to call.  Change methods
//					from int return to void where a return
//					is not needed.  Other minor code
//					cleanup.  Minor change to
//					setProgramWorkingDir() documentation.
// 28 Mar 2001	SAM, RTi		Add fileExists() and isBatch().  The
//					latter can be use by programs that can
//					run in batch and GUI mode.
// 10 May 2001	SAM, RTi		Fully enable the ProcessManagerList
//					code so that IOUtil can be used to
//					broker lookups and serve as a "bulletin
//					board".
// 2001-11-08	SAM, RTi		Synchronize with UNIX.
//					Change isReadable() to handle URLs also.
// 2001-11-20	SAM, RTi		Fix printCreatorHeader() so that command
//					line arguments correctly print within
//					the maximum line width.
// 2002-01-14	SAM, RTi		Add tempFileName() to get a temporary
//					file name.
// 2002-01-20	SAM, RTi		Add testing() methods to support test
//					code.
// 2002-03-29	SAM, RTi		Add toAbsolutePath() and
//					toRelativePath().
// 2002-04-16	SAM, RTi		Add setProgramCommandList().  This can
//					be used with GUI-based programs where
//					the commands are shown on-screen.
// 2002-05-14	SAM, RTi		Add support for Linux in
//					isUNIXMachine().
// 2002-06-07	SAM, RTi		Deprecate getProgramWorkingDirectory()
//					in favor of getProgramWorkingDir() to be
//					consistent.
// 2002-06-13	SAM, RTi		Add getFileExtension().
// 2002-10-28	SAM, RTi		Add isBigEndianMachine().
// 2002-11-05	SAM, RTi		Add appendToPath().  Update
//					toRelativePath() to handle cases where
//					the leading part of the path can be
//					discarded and replaced with a series of
//					"..\..".  Update toAbsolutePath()
//					similarly.
// 2003-02-20	SAM, RTi		Fix bug in toRelativePath() that was
//					causing a wrong number of ".." to be
//					used.
// 2003-04-17	J. Thomas Sapienza, RTi	Reworked toRelativePath() entirely.
// 2003-04-29	SAM, RTi		Deprecate getFileSeparator() - just use
//					File.separator.
// 2003-06-05	SAM, RTi		Handle XML support to
//					processFileHeaders() and clean up the
//					code some to remove old comments.
// 2003-08-24	SAM, RTi		Increase warning level from 2 to 10 in
//					getInputStream().
// 2003-09-08	SAM, RTi		* Add isAbsolute() because the standard
//					  File.isAbsolute() is not as handy as
//					  needed.
//					* Add getDrive() to return the drive for
//					  a path.
// 2003-09-17	JTS, RTi		* Fixed small bugs (very rare that they
//					  would occur) in getAbsolutePath() and
//					  getRelativePath().
// 2003-11-04	SAM, RTi		* Add enforceFileExtension() to make
//					  sure that a file has the desired
//					  extension.
// 2003-11-05	JTS, RTi		toRelativePath() was having issues
//					with case-sensitivity and paths.  
//					Corrected the problems that were 
//					observed.
// 2003-12-04	JTS, RTi		Added copyToClipboard().
// 2004-02-03	JTS, RTi		Corrected a bug in toRelativePath().
// 2004-03-15	SAM, RTi		Fixed bug in processFileHeaders() -
//					now allow null comment string.
// 2004-05-05	JTS, RTi		Changed setProgramWorkingDir() to 
//					put the proper drive letter on DOS 
//					working directories that do not have 
//					drive letters.
// 2004-07-07	SAM, RTi		Rework printCreatorHeader() to handle
//					XML - previous work by JTS (not in
//					history) used boolean to indicate XML
//					but changed to PropList to allow more
//					flexibility.
// 2004-07-21	SAM, RTi		Update adjustPath() to return the
//					original path if no adjustment is made.
// 2004-08-04	JTS, RTi		Added release().
// 2004-09-30	JTS, RTi		Added copyFile().
// 2005-05-23	JTS, RTi		Corrected bug in toRelativePath() that
//					was causing paths with similar but
//					different roots to convert to relative 
//					paths improperly.
// 2005-06-06	JTS, RTi		For standalone programs (ie, those that
//					are not applets) the host name is now
//					properly set to be the name of the 
//					computer on which the program is 
//					running.
// 2005-06-08	JTS, RTi		Added setApplicationHomeDir().
// 2005-11-16	JTS, RTi		Added getJarFilesManifests().
// 2005-11-17	JTS, RTi		Added getSystemProperties().
// 2006-02-16	JTS, RTi		Numerous changes to better support UNC
//					paths.
// 2007-01-02   KAT, RTi	Fixed the getPathUsingWorkingDir() method
//							to handle relative paths correctly.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
//EndHeader

package RTi.Util.IO;

import java.applet.Applet;
import java.applet.AppletContext;

import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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

import java.lang.reflect.Method;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.Vector;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeUtil;


// Declare as abstract to prevent from declaring...

/**
This class provides static functions for file input/output and also provides
global functionality that may be useful in any program.  The class provides
useful functionality in addition to the Java System, IO, and security classes.
A PropListManager is used to manage a global, un-named PropList in conjunction
with other PropLists.
To make the best use of this class, initialize from the main()
or init() functions, as follows:
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

// Global data...

/**
Flags use to indicate the vendor
*/
public final static int		SUN		= 1,
				MICROSOFT	= 2,
				UNKNOWN		= 3;

/**
String to use to indicate a file header revision line.
@see #getFileHeader
*/
protected static final String HEADER_REVISION_STRING = "HeaderRevision";
/**
String used to indicate comments in files (unless otherwise indicated).
*/
protected static final String UNIVERSAL_COMMENT_STRING = "#";

private static String _argv[] = null;	// Command-line arguments.
private static Applet _applet = null;	// Applet.
private static AppletContext _applet_context = null;
					// Applet context.  Call
					// setAppletContext() from init() of
					// an application that uses this class.
private static URL _document_base = null;
					// Document base for the applet.

private static String _command_file ="";// Program command file
private static Vector _command_list=null;// Program command list
private static String _host = "";	// Host running the program.
private static String _progname = "";	// Program name.
private static String _progver = "";	// Program version.
private static String _user = "";	// Program user.
private static boolean _testing = false;// Indicates whether a test run.

private static String _working_dir = "";// Program working directory.

private static boolean _initialized = false;
					// Indicates whether global data are
					// initialized.
private static boolean _is_applet = false;
					// Use to indicate if we are running as
					// an applet.
private static boolean _is_batch = false;
					// Use to indicate if program is batch
					// (non-interactive) or interactive
					// GUI/shell
private static PropListManager _prop_list_manager = null;
					// A property list manager that can be
					// used by any application.
private static boolean __runningApplet = false;
private static boolean __release = false;
private static String __homeDir = null;

/**
Add a PropList to the list managed by the IOUtil PropListManager.
@param proplist PropList to add to the list managed by the PropListManager.
@param replace_if_match If the name of the PropList matches one that is already
in the list, replace it (true), or add the new list additionally (false).
*/
public static void addPropList ( PropList proplist, boolean replace_if_match )
{	if ( !_initialized ) {
		initialize();
	}
	_prop_list_manager.addList ( proplist, replace_if_match );
}

/**
Adjust an existing path.  This can be used, for example to navigate up an
absolute path by a relative change.  The resulting path is returned.  Rules
for adjustment are as follows:
<ol>
<li>	If the adjustment is an absolute path, the returned path is the same
	as the adjustment.
	</li>
<li>	If the adjustment is a relative path (e.g., "..", "../something",
	"something", "something/something2"), the initial path is adjusted by
	removing redundant path information if possible.
	</li>
<li>	If the adjustment is a relative path that cannot be applied, an
	exception is thrown.
	</li>
<li>	The returned path will not have the file separator unless the path is
	the root directory.
	</li>
</ol>
No check for path existence is made.
@return the original path adjusted by the adjustment, with no path separator
at the end.
@param initial_path Original path to adjust.
@param adjustment Adjustment to the path to apply (e.g., "..").
@exception Exception if the path cannot be adjusted.
*/
public static String adjustPath ( String initial_path, String adjustment )
throws Exception
{	File a = new File ( adjustment );
	// If the adjustment is blank, return the initial path.  Do not trim
	// the adjustment because it is possible that a filename has only
	// spaces.
	if ( (adjustment == null) || (adjustment.length() == 0) ) {
		return initial_path;
	}
	if ( a.isAbsolute() ) {
		// Adjustment is absolute so make the adjustment...
		return adjustment;
	}
	// The adjustment is relative.  First make sure the initial path ends
	// in a file separator...
	StringBuffer buffer = new StringBuffer ( initial_path );
	char filesep = File.separator.charAt(0);
	if ( initial_path.charAt(initial_path.length() - 1) != filesep ) {
		buffer.append ( filesep );
	}
	// Loop through the adjustment.  For every ".." that is encountered,
	// remove one directory from "buffer"...
	int length = adjustment.length();
	String up_one = "..";
	for ( int i = 0; i < length; i++ ) {
		if ( adjustment.indexOf(up_one,i) == i ) {
			// The next part of the string has "..".  Move up one
			// level in the initial string.  The buffer will have a
			// separator at the end so need to skip over it at the
			// start...
			for ( int j = buffer.length() - 2; j >= 0; j-- ) {
				if ( buffer.charAt(j) == filesep ) {
					// Found the previous separator...
					buffer.setLength(j + 1);
					break;
				}
			}
			// Increment in the adjustment...
			i += 2;	// Loop increment will go past the separator
		}
		else if ( adjustment.indexOf("..",i) == i ) {
			// Need to go up one directory
		}
		else if ( adjustment.charAt(i) == '.' ) {
			// If the next character is a separator (or at the end
			// of the string), ignore this part of the path (since
			// it references the current directory...
			if ( i == (length - 1) ) {
				// Done processing...
				break;
			}
			else if ( adjustment.charAt(i + 1) == filesep ) {
				// Skip...
				++i;
				continue;
			}
			else {	// A normal "." for a file extension so add
				// it...
				buffer.append ( '.' );
			}
		}
		else {	// Add the characters to the adjusted path...
			buffer.append ( adjustment.charAt(i) );
		}
	}
	// Remove the trailing separator, but only if not the root directory..
	if (	(buffer.charAt(buffer.length() - 1) == filesep) &&
		!buffer.equals("" + filesep) ) {
		// Remove the trailing file separator...
		buffer.setLength(buffer.length() - 1);
	}
	return buffer.toString();
}

/**
Tries to manually load a class into memory in order to see if it is available.  
Normally, class-loading is done by the Java Virtual Machine when a class is 
first used in code, but this method can be used to load a class at any time, 
and more importantly, to check whether a class is available to the virtual
machine.<p>
Classes may be unavailable because of a difference in program versions, or 
because they were intentionally left out of a jar file in order to limit
the functionality of an application.<p>
The virtual machine will look through the entire class path when it tries to
load the given class.
@param className the fully-qualified class name (including package) of the
class to try loading.  Examples:<p>
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
Copies a file from one file to another.
@param source the source file to copy.
@param dest the destination file to copy the first file to.
@throws IOException if there is an error copying the file.
*/
public static void copyFile(File source, File dest) 
throws IOException {
/*
	FileChannel in = new FileInputStream(source).getChannel();
	FileChannel out = new FileOutputStream(dest).getChannel();

	long size = in.size();
	MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
	out.write(buf);

	in.close();
	out.close();
*/
	// JTS (2004-10-11)
	// the above is supposed to be faster, but MT was getting some 
	// bizarre errors that seem related to the use of Channels.  Will
	// try the following, but I believe it's going to be slowed.
	FileInputStream fis  = new FileInputStream(source);
	FileOutputStream fos = new FileOutputStream(dest);
	byte[] buf = new byte[1024];
	int i = 0;
	while((i = fis.read(buf)) != -1) {
		fos.write(buf, 0, i);
	}
	fis.close();
	fos.close();
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
Enforce a file extension.  For example, if a file chooser is used but the file
extension is not added by the chooser.  For example, if the file name is
"file" and the extension is "zzz", then the returned value will be "file.zzz".
If the file is "file.zzz", the returned value will be the same (no change).
There is currently no sophistication to handle input file names with multiple
extensions that are different from the requested extension.
@param filename The file name on which to enforce the extension.
@param extension The file extension to enforce, without the leading ".".
*/
public static String enforceFileExtension ( String filename, String extension )
{	if ( StringUtil.endsWithIgnoreCase(filename, "." + extension) ) {
		return filename;
	}
	else {	return filename + "." + extension;
	}
}

/**
Determine if a file/directory exists.
@return true if the file/directory exists, false if not.
@param filename String path to the file/directory to check.
*/
public static boolean fileExists ( String filename )
{	if ( filename == null ) {
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
public static boolean fileReadable ( String filename )
{	if ( filename == null ) {
		return false;
	}
	InputStream st = null;
	try {	st = IOUtil.getInputStream( filename);
	}
	catch ( Exception e ) {
		st = null;
		return false;
	}
	if ( st != null ) {
		try {	st.close();
		}
		catch ( Exception e ) {
		}
		st = null;
		return true;
	}
	return false;
	// This only works with files.  The above works with URLs...
	//File file = new File(filename);
	//boolean canread = file.canRead();
	//file = null;
	//return canread;
}

/**
Read in a file and store it in a string list (Vector of String).
@param filename	File to read and convert to string list.
@return the file as a string list.
@exception IOException if there is an error.
*/
public static Vector fileToStringList (	String filename )
throws IOException
{	Vector	list = null;
	String	message, routine = "IOUtil.fileToStringList", tempstr;
	
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

	// Open the file...

	if ( Message.isDebugOn ) {
		Message.printDebug ( 30, routine,
		"Breaking file \"" + filename + "\" into string list" );
	}
	BufferedReader fp = null;
	try {	fp = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream( filename) ));
	}
	catch ( Exception e ) {
		message = "Unable to read file \"" + filename + "\"";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}

	list = new Vector ( 50, 50 );
	while ( true ) {
		tempstr = fp.readLine();
		if ( tempstr == null ) {
			break;
		}
		tempstr = StringUtil.removeNewline ( tempstr );
		list.addElement (tempstr );
	}
	fp.close ();
	fp = null;
	tempstr = null;
	message = null;
	routine = null;
	return list;
}

/**
Determine if a file is writeable.  False is returned if the file does not
exist.
@return true if the file is writeable, false if not.  The file must exist.
@param filename String path to the file to check.
*/
public static boolean fileWriteable ( String filename )
{	if ( filename == null ) {
		return false;
	}
	File file = new File(filename);
	boolean canwrite = file.canWrite();
	file = null;
	return canwrite;
}

/**
Get the Applet.
@return The Applet instance set with setApplet().
@see #setApplet
*/
public static Applet getApplet ( )
{	return _applet;
}

/**
Get the AppletContext.
@return The AppletContext instance set with setAppletContext.
@see #setAppletContext
*/
public static AppletContext getAppletContext ( )
{	return _applet_context;
}

/**
Returns the application home dir.  This is the directory from which log files,
config files, etc, can be located.
@return the application home dir.
*/
public static String getApplicationHomeDir() {
	return __homeDir;
}

/**
Get the document base.
@return The DocumentBase instance set when setApplet() is called.
@see #setApplet
*/
public static URL getDocumentBase ( )
{	return _document_base;
}

/**
Return the drive letter for a path.
@return the drive letter for a path (e.g., "C:") or return an empty string if
no drive is found it the start of the path.
@deprecated come back to this to resolve UNC issues.  REVISIT JTS - 2006-02-16
*/
public static String getDrive ( String path )
{	if ( isUNIXMachine() ) {
		return "";
	}
	// Assume windows...
	if (	(path.length() >= 2) &&
		(((path.charAt(0) >= 'a') && (path.charAt(0) <= 'z')) ||
		((path.charAt(0) >= 'A') && (path.charAt(0) <= 'Z'))) && 
		(path.charAt(1) == ':') ) {
		return path.substring(0,2);
	}
	else {	return "";
	}
}

/**
Determine the file extension.
@return the part of a file name after the last "." character, or null if no
".".
*/
public static String getFileExtension ( String file )
{	Vector v = StringUtil.breakStringList ( file, ".", 0 );
	if ( (v == null) || (v.size() == 0) ) {
		return null;
	}
	return (String)v.elementAt(v.size() - 1);
}

// NEED TO CLEAN UP JAVADOC AND OPTIMIZE FOR GC...
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
@return The FileHeader associated with a file.  This information is used by
processFileHeaders when tracking revisions to files.  Comment strings must
start at the beginning of the line.
@param filename The name of the file to process.
@param comments An array of strings indicating valid comments.  For example:
{"#", "*"}.
@param ignore_comments An array of strings indicating valid comments which
should be ignored and which take precedence over "comments".  For example, "#>"
might be used for comments that are written to a file each time it is revised
but those comments are to be ignored each time the header is read.
@param flags Currently unused.
@see #processFileHeaders
*/
public static FileHeader getFileHeader (	String filename,
						String[] comments,
						String[] ignore_comments,
						int flags )
{	String	routine = "IOUtil.getFileHeader", string;
	int	dl = 30, header_first = -1, header_last = -1, header_revision,
		i, len, revlen;
	boolean	iscomment, isignore;

// Need to handle error.
	
	revlen		= HEADER_REVISION_STRING.length();
	if ( filename == null ) {
		Message.printWarning ( 10, routine, "NULL file name pointer" );
		return null;
	}
	if ( filename.length() == 0 ) {
		Message.printWarning ( 10, routine, "Empty file name" );
		return null;
	}
	if ( !fileReadable(filename) ) {
		Message.printWarning ( 10, routine,
		"File \"" + filename + "\" is not readable" );
		return null;
	}
	if ( comments == null ) {
		Message.printWarning ( 10, routine,
		"Empty comment strings list" );
		return null;
	}

	// Open the file...

	BufferedReader fp = null;
	try {	fp = new BufferedReader ( new FileReader(filename) );
	}
	catch ( Exception e ) {
		Message.printWarning ( 10, routine,
		"Error opening file \"" + filename + "\" for reading." );
		return null;
	}

	// Now read lines until we get to the end of the file or hit a
	// non-header line (OK to skip "ignore_comments")...
	
	FileHeader header = new FileHeader ();

	int length_comments = comments.length;
	int linecount = 0;
	while ( true ) {
		++linecount;
		try {	string = fp.readLine ();
			if ( string == null ) {
				break;
			}
		}
		catch ( Exception e ) {
			// End of file.
			break;
		}
		// First, find out if the line is a comment.  It is if the
		// first part of the string exactly matches any of the comment
		// strings.
		iscomment = false;
		boolean revision_start = false;
		int comment_length = 0;
		for ( i = 0; i < length_comments; i++ ) {
			// Find the length of the comment string...
			comment_length = comments[i].length();
			if ( comment_length < 1 ) {
				continue;
			}
			// Allow characters too so do a regionMatches...
			revision_start = string.regionMatches(true,0,
				comments[i],0,comments[i].length() );
			if ( revision_start ) {
				// Found a match...
				iscomment	= true;
				if ( Message.isDebugOn ) {
					Message.printDebug ( 50, routine,
					"Found comment at line " + linecount );
				}
				break;
			}
		}
		// If we do not have a comment, then there is no need to
		// continue in this loop because we are out of the comments
		// section in the file...
		if ( !iscomment ) {
			break;
		}
		// Find out if this is a header revision comment, and, if so,
		// compare to the current values saved...
		String revision_string;
		if ( (comment_length + revlen) <= string.length() ) {
			// There might be a header string
			revision_string = string.substring(comment_length,
					(comment_length + revlen));
		}
		else {	revision_string = string.substring(comment_length);
		}
		if (	revision_string.equals(HEADER_REVISION_STRING) ) {
			/*
			** This is a header revision line so read the revision
			** number from the string...
			*/
			revision_string = string.substring(
					comment_length + revlen + 1);
			header_revision = StringUtil.atoi ( revision_string );
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Found header revision " + header_revision +
				"from \"" + string + "\"" );
			}
			header_first = Math.min
				(header_first, header_revision);
			header_last = Math.max
				(header_last, header_revision);
		}
		// Now determine whether this is a comment that can be ignored.
		// If so, we just do not add it to the list...
		isignore = false;
		if ( ignore_comments != null ) {
			for ( i = 0; i < ignore_comments.length; i++ ) {
				// Find the length of the comment string...
				len = ignore_comments[i].length();
				String ignore_substring;
				if ( len <= string.length() ) {
					ignore_substring =
					string.substring(0,len);
				}
				else {	ignore_substring =
					string.substring(0);
				}
				if(ignore_substring.equals(ignore_comments[i])){
					// Found a match...
					isignore = true;
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine,
						"Ignoring: \"" + string +
						"\"" );
					}
					break;
				}
			}
		}
		// If the comment is to be ignored, read another line...
		if ( isignore ) {
			// continue;
			
			// don't want to read anyfurther.  First ignored
			// comment indicates the entire header has been read
			break;
		}
		// If we have gotten to here, add the line to the list...
		string = StringUtil.removeNewline ( string );
		header.addElement ( string );
		/*
		need to trap error
		if ( list == (char **)NULL ) {
			HMPrintWarning ( 10, routine,
			"Error adding to string list" );
		}
		*/
	}

	try {	fp.close ();
	}
	catch ( Exception e ) {
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "\"" + filename + "\":  " +
		header.size() + " lines in header" );
	}
	header.setHeaderFirst ( header_first );
	header.setHeaderLast ( header_last );
	return header;
}

/**
Get a list of files from a pathlist.
@return a list of paths to a file given a prefix path and a file name.
The files do not need to exist.  Return null if there is a problem with input.
@param paths Paths to prefix the file with.
@param file Name of file to append to paths.
*/
public static Vector getFilesFromPathList ( Vector paths, String file )
{	String	fullfile, routine = "IOUtil.getFilesFromPathList";
	Vector	newlist = null;
	int	i, npaths;

	// Check for NULL list, and file...

	if ( paths == null ) {
		Message.printWarning ( 10, routine, "NULL path list" );
		return null;
	}
	if ( file == null ) {
		Message.printWarning ( 10, routine, "NULL file name" );
		return newlist;
	}
	//if ( !*file ) {
	//	HMPrintWarning ( 10, routine, "Empty file name" );
	//	return newlist;
	//}

	npaths = paths.size();
	newlist = new Vector ( 10, 5 );
	String dirsep = System.getProperty ( "file.separator");
	for ( i = 0; i < npaths; i++ ) {
		// Add each string to the list...
		fullfile = (String)paths.elementAt(i) + dirsep + file;
		newlist.addElement ( fullfile );
	}
	return newlist;
}

/**
Return the appropriate file separator.
@return The appropriate file separator ( e.g., "/" or "\" ), depending on OS
and Applet.
@deprecated Use File.separator.
*/
public static String getFileSeparator()
{	if( IOUtil.isApplet() ) {
		return "/";
	}
	else {	return System.getProperty( "file.separator" );		
	}
}

/**
Open an input stream given a URL or regular file name.
@return An InputStream given a URL or file name.  If the string starts with
"http:", "ftp:", or "file:", a URL is created and the associated stream is
returned.  Otherwise, a file is opened and the associated stream is returned.
@param url_string
@exception IOException if the input stream cannot be initialized.
*/
public static InputStream getInputStream ( String url_string )
throws IOException
{	String	routine="IOUtil.getInputStream";
        URL url;
        FileInputStream fileStream;
        String noIndex = "Cannot open file at " + url_string + ".";

	// Make sure that the string is not empty...

	if ( url_string == null ) {
		throw new IOException ( "URL is null" );
	}
	if ( url_string.length() < 1 ) {
		throw new IOException ( "URL is empty" );
	}
        
	if (	url_string.regionMatches( true, 0, "http:", 0, 5) ||
		url_string.regionMatches( true, 0, "file:", 0, 5) ||
		url_string.regionMatches( true, 0, "ftp:", 0, 4) ) {
		try {	url = new URL ( url_string );
			return ( url.openStream() );
		}
		catch ( Exception Error ) {
			Message.printWarning (10, routine, noIndex );
			throw new IOException(noIndex);
		}
	}
	else {	try {	fileStream = new FileInputStream(url_string);
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
The contents are returned are a formatted Vector of Strings.  The name of
each Jar file is printed, followed by a list of the lines in its manifest.
If there are further Jar files, a space is added and the pattern is repeated.<p>
The order of the Jar files in the list is the same as the order the Jar files
appear in the CLASSPATH.<p>
The order of the manifest data is not necessarily the same as how they appear 
in the manifest file.  The Java classes provided for accessing Manifest data
do not return the data in any given order.  For this reason, the manifest data
are sorted alphabetically in the list.
@return the contents of the manifests of the Jar files in a Vector of Strings.
*/
public static Vector getJarFilesManifests() {
	String routine = "IOUtil.getJarFilesManifests";

	// Get the Classpath and split it into a String array.  The order
	// of the elements in the array is the same as the order in which
	// things are included in the classpath.
	String[] jars = System.getProperty("java.class.path").split(
		System.getProperty("path.separator"));
	
	Attributes a = null;
	int j = -1;
	int size = -1;
	JarFile jar = null;
	Manifest mf = null;
	Object[] o = null;
	Set set = null;
	String tab = "    ";
	Vector sort = null;
	Vector v = new Vector();
	
	for (int i = 0; i < jars.length; i++) {
		if (!StringUtil.endsWithIgnoreCase(jars[i], ".jar")) {
			// directories, etc, can be specified in a class path
			// but avoid those for just the jar files in the 
			// class path.
			continue;
		}
		
		v.add(jars[i]);	
		
		try {
			jar = new JarFile(jars[i]);
			mf = jar.getManifest();
			a = mf.getMainAttributes();
			set = a.keySet();
			o = set.toArray();
			sort = new Vector();
			for (j = 0; j < o.length; j++) {
				sort.add(tab + ((Attributes.Name)(o[j])) + " = "
					+ a.getValue((Attributes.Name)(o[j])));
			}

			// the order in which the data in the manifest file 
			// are returned is not guaranteed to be in the same
			// order as they are in the manifest file.  Thus, the
			// data are sorted to present a consistent return 
			// pattern.
			Collections.sort(sort);
			size = sort.size();
			for (j = 0; j < size; j++) {
				v.add(sort.elementAt(j));
			}			
		}
		catch (Exception e) {
			Message.printWarning(2, routine,
				"An error occurred while reading the manifest "
				+ "for: '" + jars[i] + "'.");
			Message.printWarning(3, routine, e);
			v.add(tab + "An error occurred while reading the "
				+ "manifest.");
		}

		v.add("");
	}

	return v;
}

/**
Return a path considering the working directory set by
setProgramWorkingDir().  The following rules are used:
@param path Path to use.
<ul>
<li>	If the path is null or empty, return the path.</li>
<li>	If the path is an absolute path (starts with / or \ or has : as the
	second character; or starts with http:, ftp:, file:), it is returned as
	is.</li>
<li>	If the path is a relative path and the working directory is ".", the
	path is returned.</li>
<li>	If the path is a relative path and the working directory is not ".",
	the path is appended to the current working directory (separated with
	/ or \ as appropriate) and returned.</li>
</ul>
@return a path considering the working directory.
*/
public static String getPathUsingWorkingDir ( String path )
{	if ( (path == null) || (path.length() == 0) ) {
		return path;
	}
	// Check for URL...
	if (	path.startsWith("http:") ||
		path.startsWith("ftp:") ||
		path.startsWith("file:") ) {
		return path;
	}
	// Check for absolute path...
	if ( isUNIXMachine() ) {
		if ( path.charAt(0) == '/' ) {
			return path;
		}
		if ( _working_dir.equals("") || _working_dir.equals(".") ) {
			return path;
		}
		else {
			String fullPath = path;
			try { fullPath = (new File(_working_dir + "/" 
					 + path).getCanonicalPath().toString());
				} 
			catch (IOException e) {
					e.printStackTrace();
				}		
			return fullPath;
			//return ( _working_dir + "/" + path );
		}
	}
	else {	
		if (path.startsWith("\\\\")) {
			// UNC path
			return path;
		}
		if (	(path.charAt(0) == '\\') ||
			((path.length() >= 2) && (path.charAt(1) == ':')) ) {
			return path;
		} 
		if ( _working_dir.equals("") || _working_dir.equals(".") ) {
			return path;
		}
		else {	
			String fullPath = path;
			try { fullPath = (new File(_working_dir + "\\" 
					 + path).getCanonicalPath().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}	
			return fullPath;
			//return ( _working_dir + "\\" + path );
		}
	}
}

/**
Return the program arguments.
@return The program arguments set with setProgramArguments.
@see #setProgramArguments
*/
public static String[] getProgramArguments ()
{	if ( !_initialized ) {
		initialize ();
	}
	return _argv;
}

/**
Return the program command file.
@return The command file used with the program, as set by setProgramCommandFile.
@see #setProgramCommandFile
*/
public static String getProgramCommandFile ()
{	if ( !_initialized ) {
		initialize ();
	}
	return _command_file;
}

/**
Return the program command list.  Typically either a command file or list is
used and the list takes precedence.
@return The command list used with the program, as set by setProgramCommandList.
@see #setProgramCommandList
*/
public static Vector getProgramCommandList ()
{	if ( !_initialized ) {
		initialize ();
	}
	return _command_list;
}

/**
Return name of host machine.
@return The host that is running the program, as set by setProgramHost.
@see #setProgramHost
*/
public static String getProgramHost ()
{	if ( !_initialized ) {
		initialize ();
	}
	return _host;
}

/**
Return the program name.
@return The program name as set by setProgramName.
@see #setProgramName
*/
public static String getProgramName ()
{	if ( !_initialized ) {
		initialize ();
	}
	return _progname;
}

/**
Return the program user.
@return The program user as set by setProgramUser.
@see #setProgramUser
*/
public static String getProgramUser ()
{	if ( !_initialized ) {
		initialize ();
	}
	return _user;
}

/**
Return the program version.
@return The program version as set by setProgramVersion.
@see #setProgramVersion
*/
public static String getProgramVersion ()
{	if ( !_initialized ) {
		initialize ();
	}
	return _progver;
}

/**
Return the working directory, as set by setProgramWorkingDir().  The software
may not actually be running in the directory.
@return The program working directory as set by setProgramWorkingDir.
@see #setProgramWorkingDir
@deprecated use getProgramWorkingDir
*/
public static String getProgramWorkingDirectory ()
{	return getProgramWorkingDir();
}

/**
Return the working directory.
@return The program working directory as set by setProgramWorkingDir.
@see #setProgramWorkingDir
*/
public static String getProgramWorkingDir()
{	if ( !_initialized ) {
		initialize ();
	}
	return _working_dir;
}

/**
Return global PropList property.
@return The property in the global property list manager corresponding to the
given key.
<b>This routine is being reworked to be consistent with the Prop* classes.</b>
@param key String key to look up a property.
@see Prop
@see PropList
@see PropListManager
*/
public static Prop getProp ( String key )
{	if ( !_initialized ) {
		initialize ();
	}
	return _prop_list_manager.getProp ( key );
}

/**
Return property value as an Object.
@return The value of a property in the global property list manager
corresponding to the given key.
@param key String key to look up a property.
@see Prop
@see PropList
@see PropListManager
*/
public static Object getPropContents ( String key )
{	if ( !_initialized ) {
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
public static PropListManager getPropListManager ()
{	if ( !_initialized ) {
		initialize ();
	}
	return _prop_list_manager;
}

/**
Return property value as a String.
@return The value of a property in the global property list manager
corresponding to the given key.
@param key String key to look up a property.
@see Prop
@see PropList
@see PropListManager
*/
public static String getPropValue ( String key )
{	if ( !_initialized ) {
		initialize ();
	}
	try {	Prop prop = getProp ( key );
		if ( prop == null ) {
			return null;
		}
		return prop.getValue ();
	}
	catch (Exception e ) {
		// Probably a security exception...
		return null;
	}
}

/**
Returns a Vector of Strings containing information about the system on which
the Java application is currently running.
@return a Vector of Strings.
*/
public static Vector getSystemProperties() {
	String tab = "    ";
	
	Vector v = new Vector();

	v.add("System Properties Generated for: ");
	v.add(tab + " Program Name: " + _progname + " " + _progver);
	v.add(tab + " User Name: " + _user);
	String now = TimeUtil.getSystemTimeString("");
	v.add(tab + " Date: " + now);
	v.add(tab + " Host: " + _host);
	v.add(tab + " Working Directory: " + _working_dir);
	String command = tab + " Command: " + _progname + " ";

	int totalLength = command.length();

	int length = 0;

	if (_argv != null) {
		for (int i = 0; i < _argv.length; i++) {
			length = _argv[i].length() + 1;
		
			if (totalLength + length >= 80) {
				// it would be too big for the line, so 
				// add the current line and put the next
				// argument on what will be the next line
				v.add(command);
				command = tab + tab + _argv[i];
			}
			else {
				command += _argv[i] + " ";
			}
			totalLength = command.length();
		}
	}
	v.add(command);
	v.add("");
	
	v.add("Operating System Information");
	v.add(tab + "Name: " + System.getProperty("os.name"));
	v.add(tab + "Version: " + System.getProperty("os.version"));
	v.add(tab + "System Architecture: " + System.getProperty("os.arch"));
	v.add("");
	v.add("Java Information");
	v.add(tab + "Vendor: " + System.getProperty("java.vendor"));
	v.add(tab + "Version: " + System.getProperty("java.version"));
	v.add(tab + "Home: " + System.getProperty("java.home"));

	String sep = System.getProperty("path.separator");

	String[] jars = System.getProperty("java.class.path").split(sep);

	if (jars.length == 0) {
		return v;
	}

	String cp = tab + "Classpath: " + jars[0];
	totalLength = cp.length();

	for (int i = 1; i < jars.length; i++) {
		length = jars[i].length();

		if (totalLength + length >= 80) {
			v.add(cp + sep);
			cp = tab + tab + jars[i];
		}
		else {
			cp += sep + jars[i];
		}
		totalLength = cp.length();
	}
	v.add(cp);
		
	v.add("");
	return v;
}

/**
Determines JVM through which the application/applet is currently running
@return the vendor as SUN, MICROSOFT, etc...
*/
public static int getVendor()
{	String s = System.getProperty( "java.vendor.url" );
	if ( s.equalsIgnoreCase( "http://www.sun.com" ) ) {
		return SUN;
	}
	else if ( s.equalsIgnoreCase( "http://www.microsoft.com" ) ) {
		return MICROSOFT;
	}
	else {	return UNKNOWN;
	}
}

/**
Initialize the global data.  setApplet() should be called first in an applet
to allow some of the if statements below to be executed properly.
*/
private static void initialize ()
{	String routine = "IOUtil.initialize";
	int dl = 1;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Initializing IOUtil..." );
	}
	try {	// Put this in just in case we have security problems...
		if ( _is_applet ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "An applet!");
			}
			_command_file = "";
			_command_list = null;
			// REVISIT (JTS - 2005-06-06)
			// should do some testing to see what the effects are
			// of doing a host set like in the non-applet code 
			// below.  Possibilities I foresee:
			// 1) applets lack the permission to get the hostname
			// 2) applets return the name of the computer the user
			//    is physically working on.
			// 3) applets return the name of the server on which
			//    the applet code actually resides.
			// I have no way of knowing right now which one would
			// be the case, and moreover, no time to test this.
			_host = "web server/client/URL unknown";
			_progname = "program name unknown";
			_progver = "version unknown";
			_user = "user unknown (applet)";
			_working_dir = "dir unknown (applet)";
			__homeDir = "dir unknown (applet)";
		}
		else {	// A stand-alone application...
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Not an applet!" );
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
		// Don't do anything.  Just print a warning...
		Message.printWarning ( 2, routine,
		"Caught an exception initializing IOUtil.  Continuing." );
	}

	// Initialize the applet context...

	_applet_context = null;

	// Initialize the property list manager to contain an unnamed list...

	_prop_list_manager = new PropListManager ();
	_prop_list_manager.addList (
		new PropList("", PropList.FORMAT_PROPERTIES), true );

	// Set the flag to know that the class has been initialized...

	_initialized = true;
}

/**
Return true if the program is an applet (must have set with setApplet).
@see #setApplet
*/
public static boolean isApplet ()
{	return _is_applet;
}

/**
Set whether the program is an Applet (often called by other routines).
DO NOT CALL INITIALIZE BEFORE SETTING.  THIS
FUNCTION IS EXPECTED TO BE CALLED FIRST THING IN THE init() FUNCTION OF
AN APPLET CLASS.  THEN, initialize() WILL KNOW TO TREAT AS AN APPLET!
initialize() is called automatically from this method.
@param is_applet true or false, indicatign whether an Applet.
@deprecated Use setApplet()
@see #setApplet
*/
public static void isApplet ( boolean is_applet )
{	int dl = 1;

	_is_applet = is_applet;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, "IOUtil.isApplet",
		"set _is_applet to " + _is_applet );
	}
	// Force the reinitialization.  Problems may have occurred when IO
	// first loaded if it did not know that we are running an Applet so
	// reinitialize now.
	initialize ();
}

/**
Return true if the program is a batch program.
@return true if the program is a batch program.
*/
public static boolean isBatch ()
{	return _is_batch;
}

/**
Set whether the program is running in batch mode (the default if not set is
false). 
@param is_batch Indicates whether the program is batch mode.
*/
public static void isBatch ( boolean is_batch )
{	_is_batch = is_batch;
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "IOUtil.isBatch",
		"Batch mode is " + _is_batch );
	}
}

/**
Determine whether the machine is big or little endian.  This method should be
used when dealing with binary files written using native operating system
applications (e.g., native C, C++, and FORTRAN compilers).  The Java Virtual
Machine is big endian so any binary files written with Java are transparently
big endian.  If little endian files need to be read, use the
EndianDataInputStream and other classes in this package.
Currently the determinatoin is made by looking at the operating system.  The
following are assumed:
<pre>
Linux	          LittleEndian
All other UNIX    BigEndian
All others        LittleEndian
</pre>
@return true if the machine is big endian.
*/ 
public static boolean isBigEndianMachine ()
{	String name = System.getProperty ( "os.name" );
	if ( name.equalsIgnoreCase("Linux") ) {
		return false;
	}
	else if ( isUNIXMachine() ) {
		return true;
	}
	else {	return false;
	}
}

/**
Determine whether a path is an absolute path.
The standard Java File.isAbsolute() only returns true on Windows if the leading
path contains a drive like C:\xxxx or if the path begins with two backslashes.
A single backslash will return false.  This method will return true for a single
backslash at the front of a string on Windows.
*/
public static boolean isAbsolute ( String path )
{	if ( !isUNIXMachine() && path.startsWith("\\") ) {
		// UNC will match this, as well as normal paths
		return true;
	}
	// Use the standard method...
	File f = new File ( path );
	return f.isAbsolute();
}

/**
Checks to see if the given port is open.  Open ports can be used.  Ports that
are not open are already in use by some other process.
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
Returns whether the program is running as an applet (must be set with 
setRunningApplet).
*/
public static boolean isRunningApplet() {
	return __runningApplet;
}

/**
Determine if a UNIX machine.  The following seem to be standard:
<pre>
Operating System      os.arch    os.name          os.version
Windows 95            x86        "Windows 95"     4.0
Windows 98            x86        "Windows 95"     4.1
NT                    x86        "Windows NT"     4.0
Windows 2000          x86        "Windows NT"     5.0
Linux                 i386       "Linux"
HP-UX                 PA-RISC
</pre>
@return true if a UNIX platform, including os.name of Linux, false if not (presumably
Windows).
*/
public static boolean isUNIXMachine ()
{	String arch = System.getProperty ( "os.arch" );
	String name = System.getProperty ( "os.name" );
	if (	arch.equalsIgnoreCase("UNIX") ||
		arch.equalsIgnoreCase("PA-RISC") ||
		name.equalsIgnoreCase("Linux") ) {
		return true;
	}
	else {	return false;
	}
}

/**
Sets an array and all its elements to null (for garbage collection).  Care
should be taken to ensure that this method is NOT used with static data, as
the finalize method of any instance of an Object will clear the static data
for all instances of the Object.
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
Finds the default browser for Mac, Windows or Unix and then
launches the URL given.
@param url URL to open the default browser to.
 */
public static void openURL(String url) 
{
    String osName = System.getProperty("os.name");
    if ( url == null ) {
    	return;
    }
    // Find the default browser for the OS
    try {
       if (osName.startsWith("Mac OS")) {
          Class fileMgr = Class.forName("com.apple.eio.FileManager");
          Method openURL = fileMgr.getDeclaredMethod("openURL",
             new Class[] {String.class});
          openURL.invoke(null, new Object[] {url});
       }
       else if (osName.startsWith("Windows")) {
          Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
       }
       else { //assume Unix or Linux
          String[] browsers = {
             "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
          String browser = null;
          for (int count = 0; count < browsers.length && browser == null;
          count++) {
             if (Runtime.getRuntime().exec(
                   new String[] { "which", browsers[count] }).waitFor() == 0)
                browser = browsers[count];
          }
          // If browser is not found then print warning and throw exception
          if (browser == null) {
             Message.printWarning(2, "IOUtil.openURL", 
            	 "Could not find web browser" );
          }
          else {
             Runtime.getRuntime().exec( new String[] { browser, url } );
          }
       }
    }
    catch ( Exception e ) {
    	Message.printWarning(2, "IOUtil.openURL", "Could not open default" +
    		" browser to URL: " + url );
       }
}

/**
Print a standard header to a file.  See the overloaded method for more
information.  It is assumed that the file is not an XML file.
@param ofp PrintWriter that is being written to.
@param comment0 The String to use for comments.
@param maxwidth The maximum length of a line of output (if whitespace is
embedded in the header information, lines will be broken appropriately to fit
within the specified length.
@param flag Currently unused.
@return 0 if successful, 1 if not.
*/
public static int printCreatorHeader ( PrintWriter ofp, String comment0, int maxwidth, int flag )
{	return printCreatorHeader ( ofp, comment0, maxwidth, flag, null );
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
@param comment0 The String to use for comments.
@param maxwidth The maximum length of a line of output (if whitespace is
embedded in the header information, lines will be broken appropriately to fit
within the specified length.
@param flag Currently unused.
@param props Properties used to format the header.  Currently the only
property that is recognized is "IsXML", which can be "true" or "false".  XML
files must be handled specifically because some characters that may be printed
to the header may not be handled by the XML parser.  The opening and closing
XML tags must be added before and after calling this method.
@return 0 if successful, 1 if not.
*/
public static int printCreatorHeader (	PrintWriter ofp, String comment0,
					int maxwidth, int flag, PropList props )
{	String	comment, routine = "IOUtil.printCreatorHeader";
	int	commentlen, i, left_border = 12, len;
	boolean is_xml = false;
	// Figure out properties...
	if ( props != null ) {
		String prop_value = props.getValue ( "IsXML" );
		if ( (prop_value != null) && prop_value.equalsIgnoreCase("true") ) {
			is_xml = true;
			// If XML, do not print multiple dashes together in the comments below.
		}
	}

	if ( ofp == null ) {
		Message.printWarning ( 2, routine, "Output file pointer is NULL" );
		return 1;
	}

	if ( !_initialized ) {
		initialize ();
	}

	String now = TimeUtil.getSystemTimeString ( "" );

	// Make sure that a valid comment string is used...

	if ( comment0 == null ) {
		commentlen = 0;
		comment = "";
	}
	else {
	    comment = comment0;
		commentlen = comment.length();
	}

	// Format the comment string for the command line printout...

	StringBuffer comment_space0 = new StringBuffer ( comment );
	for ( i = 0; i < left_border; i++ ) {
		comment_space0.append(" ");
	}
	String comment_space = comment_space0.toString();
	comment_space0 = null;

	ofp.println ( comment + " File generated by..." );
	ofp.println ( comment + " program:      " + _progname + " " + _progver );
	ofp.println ( comment + " user:         " + _user );
	ofp.println ( comment + " date:         " + now );
	ofp.println ( comment + " host:         " + _host );
	ofp.println ( comment + " directory:    " + _working_dir );
	ofp.print (   comment + " command line: " + _progname );
	int column0	= commentlen + left_border + _progname.length() + 1;
	int column	= column0;	// Column position, starting at 1
	if ( _argv != null ) {
		for ( i = 0; i < _argv.length; i++ ) {
			len = _argv[i].length();
			// Need 1 to account for blank between arguments...
			if ( (column + 1 + len) > maxwidth ) {
				// Put the argument on a new line...
				ofp.println ();
				ofp.print ( comment_space + _argv[i] );
				column = column0 + len;
			}
			else {
			    // Put the argument on the same line...
				ofp.print ( " " + _argv[i] );
				column += (len + 1);
			}
		}
	}
	ofp.println ();
	if ( _command_list != null ) {
		// Print the command list contents...
		if ( is_xml ) {
			ofp.println ( comment );
		}
		else {
		    ofp.println ( comment +
			"-----------------------------------------------------------------------" );
		}
		if ( fileReadable(_command_file) ) {
			ofp.println ( comment + " Last command file: \"" +
			_command_file + "\"" );
		}
		ofp.println ( comment );
		ofp.println ( comment + " Commands used to generate output:" );
		ofp.println ( comment );
		int size = _command_list.size();
		for ( i = 0; i < size; i++ ) {
			ofp.println ( comment + " " + (String)_command_list.elementAt(i) );
		}
	}
	else if ( fileReadable(_command_file) ) {
		// Print the command file contents...
		if ( is_xml ) {
			ofp.println ( comment );
		}
		else {	ofp.println ( comment +
			"-----------------------------------------------------------------------" );
		}
		ofp.println ( comment + " Command file \"" + _command_file + "\":" );
		ofp.println ( comment );
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
				ofp.println ( comment + " " + string );
			}
		}
		try {
		    cfp.close ();
		}
		catch ( Exception e ) {
		}
		ofp.flush ();
	}
	return 0;
}

/**
Print a Vector of strings to a file.  The file is created, opened, and closed.
*/
public static void printStringList ( String file, Vector strings )
throws IOException
{	String		message, routine = "IOUtil.printStringList";
	PrintWriter	ofp;

	// Open the file...

	try {	ofp = new PrintWriter ( 
		new FileOutputStream(file) );
	}
	catch ( Exception e ) {
		message = "Unable to open output file \"" + file + "\"";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
	printStringList ( ofp, strings );

	// Flush and close the file...

	ofp.flush();
	ofp.close();
	ofp = null;
	message = null;
	routine = null;
}

/**
Print a Vector of strings to an opened file.
@param ofp PrintWrite to write to.
@param strings Vector of strings to write.
*/
public static void printStringList ( PrintWriter ofp, Vector strings )
throws IOException
{	if ( strings == null ) {
		return;
	}
	int size = strings.size();
	for ( int i = 0; i < size; i++ ) {
		ofp.println ( (String)strings.elementAt(i) );
	}
}

/**
This method should be used to process the header of a file that is going through
revisions over time.  It can be used short of full revision control on the file.
The old file header will be copied to the new file using special comments
(assume # is comment):
<p>

<pre>
#HeaderRevision 1
</pre>
<p>

Where the number indicates the revision for the header.
The initial header will be number 0.
@return PrintWriter for the file (it will be opened and processed so that the
new file header consists of the old header with new comments at the top).  The
file can then be written to.  Return null if the new file cannot be opened.
@param oldfile An existing file whose header is to be updated.
@param newfile The name of the new file that is to contain the updated header
(and will be pointed to by the returned PrintWriter (it can be the same as
"oldfile").  If the name of the file ends in XML then the file is assumed to
be an XML file and the header is wrapped in <!-- --> (this may change to actual
XML tags in the future).
@param newcomments Array of strings to be added as comments in the new
revision (often null).
@param comment_strings Array of strings that indicate comment lines that should
be retained in the next revision.
@param ignore_comment_strings Array of strings that indicate comment lines that
can be ignored in the next revision (e.g., lines that describe the file format
that only need to appear once).
@param flags Currently unused.
*/
public static PrintWriter processFileHeaders (	String oldfile, String newfile,
						String[] newcomments,
						String[] comment_strings,
						String[] ignore_comment_strings,
						int flags )
{	String comment;
	String routine = "IOUtil.processFileHeaders";
	FileHeader oldheader;
	PrintWriter	ofp = null;
	int dl = 50, i, header_last = -1, header_revision, wl = 20;
	boolean is_xml = false;

	// Get the old file header...

	if ( oldfile == null ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "NULL old file - no old header" );
		}
		oldheader = null;
	}
	else if ( oldfile.length() == 0 ) {
		Message.printWarning ( dl, routine, "Empty old file - no old header" );
		oldheader = null;
	}
	else {
	    // Try to get the header...
		oldheader = getFileHeader (	oldfile, comment_strings, ignore_comment_strings, 0 );
		if ( oldheader != null ) {
			header_last = oldheader.getHeaderLast();
		}
	}

	// Open the new output file...

	try {
	    ofp = new PrintWriter ( new FileOutputStream(newfile) );
		if ( StringUtil.endsWithIgnoreCase(newfile,".xml") ) {
			is_xml = true;
		}
	}
	catch ( Exception e ) {
		e.printStackTrace();
		Message.printWarning ( wl, routine, "Unable to open output file \"" + newfile + "\"" );
		return null;
	}

	// Print the new file header.  If a comment string is not specified, use the default...

	if ( (comment_strings == null) || (comment_strings.length == 0) ) {
		comment = UNIVERSAL_COMMENT_STRING;
	}
	else {
	    comment = comment_strings[0];
	}
	header_revision = header_last + 1;
	if ( is_xml ) {
		ofp.println ( "<?xml version=\"1.0\" encoding=\"utf-8\"?>" );
		ofp.println ( "<!--" );
	}
	ofp.println ( comment + HEADER_REVISION_STRING + " " + header_revision);
	ofp.println ( comment );

	// Now print the standard header...

	PropList props = new PropList ( "header" );
	if ( is_xml ) {
		props.set ( "IsXML=true" );
	}
	printCreatorHeader ( ofp, comment, 80, 0, props );

	// Now print essential comments for this revision.  These strings do
	// not have the comment prefix...

	if ( newcomments != null ) {
		if ( newcomments.length > 0 ) {
			if ( is_xml ) {
				ofp.println ( comment );
			}
			else {
			    ofp.println ( comment + "----" );
			}
			for ( i = 0; i < newcomments.length; i++ ) {
				ofp.println ( comment + " " + newcomments[i] );
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
Checks whether the application is running in release mode or not.
@deprecated Avoid using this method - deal with release issues as part of the build testing.
@return true if in release mode, false if not.
*/
public static boolean release() {
	return __release;
}

/**
Replaces old file extension with new one.  If the file has no
extension then it adds the extension specified.
@param file File name to change extension on.
@param extension New file extension.
@return file_new New file name with replaced extension.
 * @throws IOException 
 */
public static String replaceFileExtension( String file, String extension ) 
throws IOException
{
	// first make sure the file is an absolute value
	// this makes it easier to check and replace
	File tmp = new File(file);
	if ( !tmp.isAbsolute() ) {
		file = tmp.getCanonicalPath().toString();
	}
	tmp = null;
	
	// add a period to the beginning of the extension
	// if one doesn't exist already
	if ( !(extension.startsWith(".")) ) {
		extension = "." + extension;
	}
	Vector v = StringUtil.breakStringList ( file, ".", 0 );
	if ( (v == null) || (v.size() == 0) ) {
		// didn't have an extension so add one and return it
		if ( file != null && file.length() > 0 ) {
			return file += extension;
		}
	}
	String file_new = "";
	for( int i = 0; i < v.size() - 1; i++ ) {
		file_new += ( String )v.elementAt( i );
	}
	// add the new extension
	file_new += extension;
	return file_new;	
}

/**
Set the applet for a program.  This is generally called from the init() method
of an application.  This method then saves the AppletContext and DocumentBase
for later use.  After calling with a non-null Applet, isApplet() will return
true.
@param applet The Applet for the application.
@see #isApplet
@see #getApplet
@see #getAppletContext
@see #getDocumentBase
*/
public static void setApplet ( Applet applet )
{	if ( applet != null ) {
		_applet = applet;
		_applet_context = applet.getAppletContext();
		_document_base = applet.getDocumentBase();
		_is_applet = true;
	}
	// Do this after setting the applet so that the initialization can
	// check...
	if ( !_initialized ) {
		initialize ();
	}
}

/**
Set the AppletContext.  This is generally only called from low-level code
(normally just need to call setApplet()).
@param applet_context The AppletContext for the current applet.
@see #setApplet
@see #getAppletContext
*/
public static void setAppletContext ( AppletContext applet_context )
{	_applet_context = applet_context;
}

/**
Sets the application home directory.  This is a base directory that should 
only be set once during an application run.  It is the base from which log
files, system files, etc, can be located.  For instance, for CDSS applications
the application home is set to (eg) j:\cdss\.  Other directories under this
include "system" and "logs".
@param homeDir the home directory to set.
*/
public static void setApplicationHomeDir(String homeDir) {
	if (!_initialized) {
		initialize();
	}

	if (homeDir != null) {
		homeDir = homeDir.trim();
		if (homeDir.endsWith(File.separator)) {
			homeDir = homeDir.substring(0, (homeDir.length() - 1));
		}

		// for windows-based machines:
		if (File.separator.equals("\\")) {
			// on dos
			if (homeDir.startsWith("\\\\")) {
				// UNC path -- leave as is
			}
			else if (homeDir.charAt(1) != ':') {
				// homeDir does not start with a drive
				// letter.  Get the drive letter of the current
				// working dir and use it instead.  Since 
				// working dir is initialized to the java 
				// working dir when IOUtil is first used,
				// _working_dir will always have a drive letter
				// for windows machines.
				char drive = _working_dir.charAt(0);
				homeDir = drive + ":" + homeDir;
			}
		}
		__homeDir = homeDir;
	}
}

/**
Set the program arguments.  This is generally only called from low-level code
(normally just need to call setProgramData()).  A copy is saved.
@param argv Program arguments.
@see #setProgramData
*/
public static void setProgramArguments ( String argv[] )
{	if ( !_initialized ) {
		initialize ();
	}

	if ( argv == null ) {
		// No arguments...
		_argv = null;
		return;
	}

	// Now we create a copy of the command-line arguments
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
public static void setProgramCommandFile ( String command_file )
{	if ( !_initialized ) {
		initialize ();
	}
	if ( command_file != null ) {
		_command_file = command_file;
	}
}

/**
Set the program command list.  This is generally only called from graphical
user interfaces, immediately before processing commands.
@param command_list Command list to use with the program (can be null).
@see #getProgramCommandList
@deprecated utilize command processors to get comments suitable for files
*/
public static void setProgramCommandList ( Vector command_list )
{	if ( !_initialized ) {
		initialize ();
	}
	_command_list = command_list;
}

/**
Set the program main data, which can be used later for GUI labels, etc.  This
is generally called from the main() or init() function of an application (or
from application base classes).
@param progname The program name.
@param progver The program version.
@param argv The program command-line arguments (ignored if an Applet).
@see #getProgramName
@see #getProgramVersion
@see #getProgramArguments
*/
public static void setProgramData ( String progname, String progver,
	String argv[] )
{	if ( !_initialized) {
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
public static void setProgramHost ( String host )
{	if ( !_initialized ) {
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
public static void setProgramName ( String progname )
{	if ( !_initialized ) {
		initialize ();
	}
	if ( progname != null ) {
		_progname = progname;
	}
}

/**
Set the program user.  This is usually called from within IO by checking system
properties.
@param user The user name.
@see #getProgramUser
*/
public static void setProgramUser ( String user )
{	if ( !_initialized ) {
		initialize ();
	}
	if ( user != null ) {
		_user = user;
	}
}

/**
Set the program version.
@param progver The program version.
@see #getProgramVersion
*/
public static void setProgramVersion ( String progver )
{	if ( !_initialized ) {
		initialize ();
	}
	if ( progver != null ) {
		_progver = progver;
	}
}

/**
Set the program working directory.  It does not cause a directory change.
This method may be called, for example, when a GUI program applies an artificial
directory change.  Java does not allow a change in the working directory but
by setting here the application is indicating that relative paths should be
relative to this directory.  The value of the working directory should be an
absolute path if from a GUI to ensure that the correct absolute path to files
can be determined.  The default working directory is the directory in which the
application started.  This is often reset soon by an application to indicate a
"home" directory where work occurs.
@param working_dir The program working directory.  The trailing directory
delimiter will be removed if specified.  Currently, working_dir must be an
absolute path (e.g., as taken from a file chooser).  If not, the given directory
is prepended with the previous drive letter if a Windows machine.  In the
future, a relative path (e.g., "..\xxxx") may be allowed, in which case, the
previous working directory will be adjusted.
@see #getProgramWorkingDir
*/
public static void setProgramWorkingDir ( String working_dir )
{	if ( !_initialized ) {
		initialize ();
	}
	if ( working_dir != null ) {
		working_dir = working_dir.trim();
		if ( working_dir.endsWith(File.separator) ) {
			working_dir = working_dir.substring(
					0,(working_dir.length() - 1) );
		}

		// for windows-based machines:
		if (File.separator.equals("\\")) {
			// on dos
			if (working_dir.startsWith("\\\\")) {
				// UNC drive -- leave as is
			}
			else if (working_dir.charAt(1) != ':') {
				// working_dir does not start with a drive
				// letter.  Get the drive letter of the current
				// working dir and use it instead.  Since 
				// working dir is initialized to the java 
				// working dir when IOUtil is first used,
				// _working_dir will always have a drive letter
				// for windows machines.
				char drive = _working_dir.charAt(0);
				working_dir = drive + ":" + working_dir;
			}
		}
		_working_dir = working_dir;
	}
}

/**
Set a property in the global PropListManager.  This sets the value in the
un-named PropList.
*/
public static void setProp ( String key, Object prop )
{	if ( !_initialized ) {
		initialize ();
	}
	// Set in the first list...
	_prop_list_manager.setValue ( "", key, prop );
}

/**
Sets whether the application is running in release mode.
@param release if true, the application is in release mode.
@deprecated Avoid using this method - deal with release issues as part of the build testing.
*/
public static void setRelease(boolean release) {	
	if (!_initialized) {
		initialize();
	}
	__release = release;
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
Determine a unique temporary file name.  On UNIX, temporary files are created
in /tmp.  On PCs, temporary files are created in C:/TEMP.  If using Java 1.2x,
use the File.createTempFile() method instead.
@return Full path to an unused temporary file.
*/
public static String tempFileName()
{	// Get the prefix...
	String prefix = null;
	if ( isUNIXMachine() ) {
		prefix = "/tmp/";
	}
	else {	prefix = "C:\\temp\\";
	}
	Date d = null;
	// Use the date as a seed and make sure the file does not exist...
	String filename = null;
	while ( true ) {
		d = new Date();
		filename = prefix + d.getTime();
		if ( !fileExists(filename) ) {
			d = null;
			break;
		}
	}
	prefix = null;
	return filename;
}

/**
Set whether the application is being run in test mode.  The testing()
method can be called to check the value.  An appropriate way to use this
functionality is to check for a -test command line argument.  If present, call
IOUtil.testing(true).  Later, check the value with IOUtil.testing().  This is
useful for adding GUI features or expanded debugging only for certain parts of
the code that are being tested.
@param is_testing true if the application is being run in test mode (default
initial value is false).
@return the value of the testing flag, after being set.
*/
public static boolean testing ( boolean is_testing )
{	_testing = is_testing;
	return _testing;
}

/**
Determine whether the application is being run in test mode.  See overloaded
method for more information.
@return the value of the testing flag, after being set.
*/
public static boolean testing ()
{	return _testing;
}

/**
Convert a path and an absolute directory to an absolute path.
@param dir Directory to prepend to path.
@param path Path to append to dir to create an absolute path.  If absolute, it
will be returned.  If relative, it will be appended to dir.  If the path
includes "..", the directory will be truncated before appending the non-".."
part of the path.
*/
public static String toAbsolutePath ( String dir, String path )
{	File f = new File ( path );
	if ( f.isAbsolute() ) {
		return path;
	}
	// Loop through the "path".  For each occurance of "..", knock a
	// directory off the end of the "dir"...

	// always trim any trailing directory separators off the directory
	// paths
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
			// No need for this in the result...
			// Adjust the path and evaluate again...
			path = path.substring(2);
			i = -1;
			path_length -= 2;
		}
		if ( path.startsWith("../") || path.startsWith("..\\") ) {
			// Remove a directory from each path...
			pos = dir.lastIndexOf(sep);
			if ( pos >= 0 ) {
				// This will remove the separator...
				dir = dir.substring(0,pos);
			}
			// Adjust the path and evaluate again...
			path = path.substring(3);
			i = -1;
			path_length -= 3;
		}
		else if (path.equals("..")) {
			// remove a directory from each path
			pos = dir.lastIndexOf(sep);
			if (pos >= 0) {
				dir = dir.substring(0, pos);
			}
			// adjust the path and evaluate again
			path = path.substring(2);
			i = -1;
			path_length -= 2;
		}
	}

	return dir + File.separator + path;
}

/**
Convert a path "path" and an absolute directory "dir" to a relative path.
If "dir" is at the start of "path" it is removed.  If it is not present, an
exception is thrown.  For example, a "dir" of \a\b\c\d\e and a "path" of
\a\b\c\x\y will result in ..\..\x\y.<p>
The strings passed in to this method should not end with a file separator 
(either "\" or "/", depending on the system).  If they have a file separator,
the separator will be trimmed off the end.
<br>
There are four conditions for which to check:<ul>
<li>The directories are exactly the same ("\a\b\c" and "\a\b\c")</li>
<li>The second directory is farther down the same branch that the first
directory is on ("\a\b\c\d\e" and "\a\b\c").<li>
<li>The second directory requires a backtracking up the branch on which the
first directory is on ("\a\b\c\d" and "\a\b\c\e" or "\a\b\c\d" and "\g")</li>
<li>For DOS: the directories are on different drives.</li>
<br>
This method will do error checking to make sure the directories passed in
to it are not null or empty, but apart from that does no error-checking to 
validate proper directory naming structure.  This method will fail with
improper directory names (e.g., "C:\\c:\\\\\\\\test\\\\").
@param rootDir the root directory from which to build a relative directory.
@param relDir the directory for which to create the relative directory path
from the rootDir.
@return the relative path created from the two directory structures.  This
path will NOT have a trailing directory separator (\ or /).  If both the
rootDir and relDir are the same, for instance, the value "." will be returned.
Plus the directory separator, this becomes ".\" or "./".
@exception Exception if the conversion cannot occur.  Most likely will occur
in DOS when the two directories are on different drives.  Will also be thrown
if null or empty strings are passed in as directories.
*/
public static String toRelativePath (String rootDir, String relDir)
throws Exception {
	// do some simple error checking
	if (rootDir == null || rootDir.trim().equals("")) {
		throw new Exception ("Bad rootDir (" + rootDir + ") passed "
			+ "in to IOUtil.toRelativePath()");
	}
	if (relDir == null || relDir.trim().equals("")) {
		throw new Exception ("Bad relDir (" + relDir + ") passed "
			+ "in to IOUtil.toRelativePath()");
	}

	String sep = File.separator;
	
	boolean unix = true;

	if (sep.equals("\\")) {
		unix = false;
		// this is running on DOS.  Check to see if the drive letters
		// are the same for each directory -- if they aren't, the
		// second directory can't be converted to a relative directory.
		char drive1 = rootDir.toLowerCase().charAt(0);
		char drive2 = relDir.toLowerCase().charAt(0);

		if (drive1 != drive2) {
			throw new Exception ( "Cannot adjust \"" + relDir +
				" to relative using directory \"" 
				+ rootDir + "\"");
		}
	}
		
	// always trim any trailing directory separators off the directory
	// paths
	while (rootDir.length() > 1 && rootDir.endsWith(File.separator)) {
		rootDir = rootDir.substring(0, rootDir.length() - 1);
	}
	while (relDir.length() > 1 && relDir.endsWith(File.separator)) {
		relDir = relDir.substring(0, relDir.length() - 1);
	}

	// Check to see if the two paths are the same
	if ((unix && rootDir.equals(relDir)) 
		|| (!unix && rootDir.equalsIgnoreCase(relDir))) {
		return ".";
	}

	// check to see if the relDir dir is farther up the same branch that
	// the rootDir is on.
	
	if ((unix && relDir.startsWith(rootDir))
		|| (!unix && StringUtil.startsWithIgnoreCase(relDir, rootDir))){

		// at this point, it is known that relDir is longer than
		// rootDir
		String c = "" + relDir.charAt(rootDir.length());

		if (c.equals(File.separator)) {	
			String higher = relDir.substring(rootDir.length());
			if (higher.startsWith(sep)) {
				higher = higher.substring(1);
			}
			return higher;
		}
	}

	// if none of the above were triggered, then the second directory
	// is higher up the first directory's directory branch

	// get the final directory separator from the first directory, and
	// then start working backwards in the string to find where the
	// second directory and the first directory share directory 
	// information
	int start = rootDir.lastIndexOf(sep);	
	int x = 0;
	for (int i = start; i >= 0; i--) {
		String s = String.valueOf(rootDir.charAt(i));

		if (!s.equals(sep)) {
			// do nothing this iteration
		}
		else if ((unix 
		    && relDir.regionMatches(false, 0, rootDir + sep, 0, i + 1))
			|| (!unix 
			&& relDir.regionMatches(true,0,rootDir + sep, 0, i+1))){
			// a common "header" in the directory name has been
			// found.  Count the number of separators in each
			// directory to determine how much separation lies
			// between the two
			int dir1seps = StringUtil.patternCount(
				rootDir.substring(0, i), sep);
			int dir2seps = StringUtil.patternCount(
				rootDir, sep);
			x = i + 1;
			if (x > relDir.length()) {
				x = relDir.length();
			}
			String uncommon = relDir.substring(x,
				relDir.length());
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
public static String verifyPathForOS ( String path )
{   if ( path == null ) {
        return path;
    }
    if ( isUNIXMachine() ) {
        return ( path.replace ( '\\', '/' ) );
    }
    else {
        return path;
    }
}

/**
Write a file.
@param filename Name of file to write.
@param contents Contents to write to file.  It is assumed that the contents
contains line break characters.
*/
public static void writeFile ( String filename, String contents )
throws IOException
{	String message, routine = "IOUtil.writeFile";

	BufferedWriter fp = null;
	try {	fp = new BufferedWriter ( new FileWriter( filename ));
		fp.write ( contents );
		fp.close ();
	}
	catch ( Exception e ) {
		message = "Unable to open file \"" + filename + "\"";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
}

} // End class IOUtil
