//---------------------------------------------------------------------------
// SimpleFileFilter 
//---------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//---------------------------------------------------------------------------
// History:
//
// 03 Oct 2002	J. Thomas Sapienza, RTi	Initial version.
// 10 Oct 2002	JTS, RTi		Javadoc'd
// 24 Oct 2002	JTS, RTi		Corrected error that left extensions
//					null when adding them as individual
//					Strings.
// 12 Nov 2002	JTS, RTi		Moved into RTi.Util.GUI;  Revised code
//					according to SAM's comments.  Mostly
//					reformatting and comment changes, but 
//					also:
//					* descriptions are now required when
//					  a filter is made.
//					* filter-matching is done with case
//					  sensitivity now, for UNIX systems.
//					* removed the setDescription method and
//					  inlined it.
//					* removed and inlined the addExtension
//					  method
// 2003-08-25	JTS, RTi		Class now implements the 
//					java.io.FileFilter interface, so that 
//					it can be used for multiple file 
//					filtering purposes.
// 2003-08-27	JTS, RTi		Added getFilters().
// 2003-09-23	JTS, RTi		Added getShortDescription().
// 2004-05-04	JTS, RTi		Added the NA member variable.
// 2005-04-26	JTS, RTi		Added finalize().
// 2006-02-01	JTS, RTi		Added __allFiles boolean flag support 
//					for single-extension file filters.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//---------------------------------------------------------------------------

package RTi.Util.GUI;

import java.io.File;

import java.util.List;
import java.util.Vector;

import RTi.Util.IO.IOUtil;

// These classes are also used, but are not imported because they both must
// be referred to with the full path name in order to avoid conflicts.
//import java.io.FileFilter;
//import javax.swing.filechooser.FileFilter;

/**
<p>
SimpleFileFilter is an easy way to implement File Filters in JFileChoosers.
A SimpleFileFilter contains a _single_ filter for use in a dialog, and
file extensions that should be matched against should NOT be added to 
the filter with a period before the characters (ie, add "gif", not ".gif").<p>

For example, if building a graphics application, the designer would still need 
to create a separate filter object for .gifs, .jpegs, .bmps, etc.  When 
extensions are passed in to the Filter constructors, they can be in the form
(using JPegs as the example): .jpeg or jpeg -- the period automatically will be added.
</p>
<p>
The description used in the various constructors should be fairly concise.  
For .jpegs, the description "jpeg image files" would suffice.
</p>
<p>
The following is an example in which a JFileChooser is set up with two SimpleFileFilters:<p>
<blockquote><pre>
JFileChooser fc = new JFileChooser();
fc.setDialogTitle("Choose data file to open");

SimpleFileFilter cff = new SimpleFileFilter("txt", "Comma-delimited Files");

List<String> v = new Vector(2);
v.addElement("dat");
v.addElement("data");
SimpleFileFilter dff = new SimpleFileFilter(v, "Application Data Files");

fc.addChoosableFileFilter(cff);
fc.addChoosableFileFilter(dff);
fc.setFileFilter(dff);
</pre></blockquote>
<p>
The code above will create a JFileChooser for opening data files of two 
specified kinds.  In the file dialog, the user can choose between opening:
"Comma-delimited Files (.txt)" or
"Application Data Files (.dat, .data)"
The default file filter that will be selected when the JFileChooser appears
on-screen will be dff, or "Application Data Files (.dat, .data)"
</p>
*/
public class SimpleFileFilter 
extends javax.swing.filechooser.FileFilter 
implements java.io.FileFilter {

/**
"N/A" -- a String that can be used when specifying a filter extension that
forces no extension to appear.
*/
public final static String NA = "N/A";

private boolean __allFiles = false;

/**
Whether the description of the extensions should be used.
*/
private boolean __describeExtensions = true;

/** 
A description of the filter.
*/
private String __description = null;

/**
A long description of the filter, containing the list of extensions to be
filtered and the description of the list.
*/
// TODO SAM 2007-05-09 Evaluate whether needed
//private String __fullDescription = null;

/**
A list of all the filtered extensions in the filter.
*/
private List<String> __filters = null;

/**
Creates a SimpleFileFilter that will filter for the given extension with 
the given description.  If the extension is specified as "N/A" -- use the
public NA member variable -- no extension will be displayed.
@param extension a String extension (without the preceding '.' ) to
be filtered.  If the extension is specified as "N/A" -- use the
public NA member variable -- no extension will be displayed.
@param description a couple words describing the filter.
*/
public SimpleFileFilter(String extension, String description) {
	initialize();
	
	if (extension == null) {
		return;
	}

	__filters.add(extension);
	if (extension.equals(NA)) {
		__describeExtensions = false;
	}

	if (extension.equals("*")) {
		__allFiles = true;
	}

	__description = description;
	//__fullDescription = getDescription();
}

/**
Creates a SimpleFileFilter that will filter for each extension in the 
list of Strings, using the given description.
@param filters a list of Strings, each of which will be an extension to
be filtered for.  For example, "jpg, jpeg" or "htm, html".
@param description a couple words describing the filter.
*/
public SimpleFileFilter(List<String> filters, String description) {
	initialize();
	for (String filter: filters ) {
		__filters.add(filter);
	}
	__description = description;
	//__fullDescription = getDescription();
}

/**
Checks a File to see if it its extension matches one of the extensions in the filter.
@param f the File to check the extension of.
@return true if the File has a matching extension, false if not.
*/
public boolean accept(File f) {
	if (f != null) {
		// the next line makes it so that directories always match
		// the file filter.  If directories were not set to match,
		// then a file dialog would not have the capability to browse
		// into other directories, as they would not show up in the dialog.
		if (f.isDirectory()) {
			return true;
		}
	
		if (__allFiles) {
			return true;
		}
		
		String extension = getExtension(f);
		if (extension != null) {
			int size = __filters.size();
			for (int i = 0; i < size; i++) {
				if (IOUtil.isUNIXMachine()) {
					if (extension.equals( __filters.get(i))) {
					      return true;
					}
				}
				else {
					if (extension.equalsIgnoreCase( __filters.get(i)) ) {
					      return true;
					}
				}
			}
		}
	}
	return false;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__description = null;
	__filters = null;
	super.finalize();
}

/**
Returns the full description for the filter.  The description would consist
of the short description followed by a list of all the extensions.  
Example:
"Jpeg Image Files (.jpg, .jpeg)"
@return the whole description of the filter.
*/
public String getDescription() {
	String fullDescription = "";
	
	if (__describeExtensions == true) {
		if (__description==null) {
		 	fullDescription = "(";
		} else {
			fullDescription = __description + " (";
		}

		int size = __filters.size();
	
		if (size == 1 && __allFiles) {
			fullDescription += "All Files)";
		}
		else {
			for (int i = 0; i < size; i++) {
				if (i > 0) {
					fullDescription += ", ";
				}
				fullDescription += "." + (String) __filters.get(i);
			}
			fullDescription += ")";
		}
	} 
	else {
		fullDescription = __description;
	}
	return fullDescription;
}

/**
Pulls off the extension from the given file and returns it.
@param f the File off of which to get the extension.
@return the String extension if the file exists, or null if the file doesn't exist.
*/
public String getExtension(File f) {
	if (f != null) {
		String filename = f.getName();
		return IOUtil.getFileExtension(filename);
	}
	return null;
}

/**
Returns the list of extensions for which this file filter filters.
@return the list of extensions for which this file filter filters.
*/
public List<String> getFilters() {
	return __filters;
}

/**
Returns the short description, always without any data about the filtered extensions.
@return the short description, always without any data about the filtered extensions.
*/
public String getShortDescription() {
	return __description;
}

/**
Initialize values.
*/
private void initialize() {
	__filters = new Vector();
}

/**
Sets whether to display the list of extensions in the filter description
shown in the dialog.  If true, the dialog will display a phrase similar to
"Example Files (.ex, .exf)".  Otherwise, only "Example Files" would be shown.
The default behavior is to show all the extensions.
@param describeExtensions if true, the extensions will be shown in the 
list of filters in the dialog, otherwise, they won't be.
*/
public void showExtensionListInDescription(boolean describeExtensions) {
	__describeExtensions = describeExtensions;
	//__fullDescription = getDescription();
}

/**
Returns a string description of this file filter.
@return a string description of this file filter.
*/
public String toString() {
	return getDescription();
}

}