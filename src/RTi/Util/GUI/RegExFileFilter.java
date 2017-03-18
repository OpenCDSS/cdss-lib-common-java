//---------------------------------------------------------------------------
// RegExFileFilter - A simple way to implement JFileChooser file filters
// that match files based on Regular Expressions.
//---------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//---------------------------------------------------------------------------
// History:
//
// 2002-11-13	J. Thomas Sapienza, RTi	Initial version from SimpleFileFilter.
// 2002-11-25	JTS, RTi		Revised based on comments from SAM.
//					Improved documentation, cleaned up code,
//					removed Initialize method.
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//---------------------------------------------------------------------------

package RTi.Util.GUI;

import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

/**
RegExFileFilter is a simple JFileChooser file filter that
matches files based on Regular Expressions.
 
A RegExFileFilter contains a <b>single</b> filter for use in a dialog.  This 
single RegExFileFilter can match more than one regular expression.<p>
 
The description used in the various constructors should be concise.  <p>

The following is an example in which a JFileChooser is set up with two
RegExFileFilters:<p>
<pre>
JFileChooser fc = new JFileChooser();
fc.setDialogTitle("Choose data file to open");

RegExFileFilter cff = new RegExFileFilter("^A.*", 
"Filenames starting with 'A'");

Vector v = new Vector(2);
v.addElement(".*in.*");
v.addElement(".*out.*");
RegExFileFilter dff = new RegExFileFilter(v,
	"Filenames containing 'in' or 'out'");

fc.addChoosableFileFilter(cff);
fc.addChoosableFileFilter(dff);
fc.setFileFilter(dff);
</pre><p>

The code above will create a JFileChooser for opening data files of two 
specified kinds.  In the file dialog, the user can choose between opening:<br>
"Filenames starting with 'A'" or<br>
"Filenames containing 'in' or 'out'"<p>
The default file filter that will be selected when the JFileChooser appears
on-screen will be dff, or "Filenames containing 'in' or 'out'"<p>

The above are very simple examples, and are not meant to explain anything
about regular expressions.  To learn more about regular expressions, search
the web or see the Javadocs for java.lang.String.matches().<p>

Note that the developer can choose to have the description for a filter 
show the regular expression that is used to match with (ie, the chooser 
could instead show:<br>
"Filenames starting with 'A' (^A.*)"<br>
but this could confuse users.
*/
public class RegExFileFilter 
extends FileFilter {

/**
Whether the regular expressions that files are matched with should be 
shown in the description.  
*/
private boolean __displayRegExs = false;

/** 
A description of the filter.
*/
private String __description = null;

/**
A long description of the filter.  This is different from the short 
description in that if the regular expressions with which files are 
matched are to be shown in the JFileChooser, this description will 
contain both the short description and the regular expressions.
See showRegExListInDescription() for more information.
*/
//TODO SAM 2007-05-09 Evaluate if needed.
//private String __fullDescription = null;

/**
A list of all the regular expressions in the filter.
*/
private List<String> __filters = null;

/**
Creates a RegExFileFilter that will filter for the given regular expression,
and which has the given description.
@param regex a regular expression to filter files for
@param description a concise description of the filter (e.g., "Shockwave Media Files").
*/
public RegExFileFilter(String regex, String description) {
	__filters = new Vector<String>();
	
	if (regex == null) {
		return;
	}

	__filters.add(regex);

	__description = description;
	//__fullDescription = getDescription();
}

/**
Creates a RegExFileFilter that will filter for each regular expression in the
list of Strings, and will use the given description.
@param filters a list of Strings, each of which is a regular 
expression to be used as a filter.
@param description a concise description of the filter. (e.g., "Shockwave Media Files").
*/
public RegExFileFilter(List<String> filters, String description) {
	__filters = new Vector<String>();

	for (int i = 0; i < filters.size(); i++) {
		__filters.add(filters.get(i));
	}
	__description = description;
	//__fullDescription = getDescription();
}

/**
Checks a File to see if it is matched by one of the regular expressions
in the filter.
@param f the File to check
@return true if the File matches one of the regular expressions
*/
public boolean accept(File f) {
	if(f != null) {
		// the next line makes it so that directories always match
		// the file filter.  If directories were not set to match,
		// then a file dialog would not have the capability to browse
		// into other directories, as they would not show up in the
		// dialog.
		if(f.isDirectory()) {
			return true;
		}
		String filename = f.getName();
		for (int i = 0; i < __filters.size(); i++) {
			if (filename.matches((String)__filters.get(i))) {
				return true;
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
Returns the full description for the filter.  The description will consist
of the short description, and that could possibly be followed by a list 
of all the regular expressions, depending on how 
showRegExListInDescription() was called.  If showRegExListInDescription()
hadn't been called yet, the tailing list of all the regular expressions
will not be shown.
Example:
"Filenames with 'in' or 'out followed by a 4-digit number'" or
"Filenames with 'in' or 'out' (.*in.*, .*out.*\D\D\D\D.*)"
Note that '.' are not literals.  This is explained in the documentation for
regular expressions.  To do a literal '.', use '\.'.
@return the whole description of the filter.
*/
public String getDescription() {
	String fullDescription = "";
	
	if(__displayRegExs == true) {
		if (__description==null) {
		 	fullDescription = "(";
		} else {
			fullDescription = 
				__description + " (";
		}

		int size = __filters.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				fullDescription += ", ";
			}
			fullDescription += (String) __filters.get(i);
		}
		fullDescription += ")";
	} else {
		fullDescription = __description;
	}
	return fullDescription;
}

/**
Sets whether to display the list of regular expressions in the filter 
description shown in the dialog.  If true, the dialog will display a 
phrase similar to
"Example Files (.*\.ex, .*\..exf)".  Otherwise, only "Example Files" 
would be shown.
The default behavior is to not show any of the regular expressions.
@param displayRegExs if true, the regular expressions will be shown in the 
list of filters in the dialog, otherwise, they won't be.
*/
public void showRegExListInDescription(boolean displayRegExs) {
	__displayRegExs = displayRegExs;
	//__fullDescription = getDescription();
}

}
