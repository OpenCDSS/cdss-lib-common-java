// ----------------------------------------------------------------------------
// JarResources.java - class for reading resources out of a .jar file
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// Original class file taken from:
// http://www.javaworld.com/javaworld/javatips/jw-javatip49.html
// by John D. Mitchell and Arthur Choi
// Reworked and edited on 15/07/02 by J. Thomas Sapienza, RTi
// ----------------------------------------------------------------------------
// History:
// 2002-07-15	J. Thomas Sapienza, RTi	Initial RTi version
// 2002-07-22	JTS, RTi		Updated, Javadoc'd
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.IO;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import RTi.Util.Message.Message;

/**
The JarResources class that can read and manipulate resources
in jar files.  It can do this one of two ways.  <p>
<ol>
<li>Read resources from the jar file upon demand</b><br>
To do this, declare the JarResources class as usual and then use the 
method <b>getResourceFromJar(...)</b> when a resource is needed.</li>
<li>Read all resources in the jar file into a hash table 
(this providesquicker access)</b><br>
Working with files like this, the JarResources class is declared as usual,
but the function <b>buildResourceHashtable()</b> must be called next.  
This sets up all the resources in the hash table.  To retrieve resources
from the hashtable, use the method <b>getResourceFromHashtable(...)</b></li>
This example Is a test driver. 
Given a jar file and a resource name, it trys to
extract the resource and then tells us whether it could or not.

<strong>Example</strong>
If there was have a JAR file which jarred up a bunch of gif image
files. Now, by using JarResources, a user could extract, create, and display
those images on-the-fly.
<pre>
    ...
    JarResources JR = new JarResources("GifBundle.jar");
    Image image = Toolkit.createImage(JR.getResource("logo.gif");
    Image logo = Toolkit.getDefaultToolkit().createImage(
                  JR.getResources("logo.gif")
                  );
    ...
</pre>

<p>
<b>Here is an example main() that uses the JarResource code:<p></b>
<pre>
public static void main(String[] args) throws IOException {
if (args.length != 2) {
	System.err.println(
	"usage: java JarResources <jar file name> <resource name>"
	);
	System.exit(1);
}
JarResources jr=new JarResources(args[0]);
jr.buildResourceHashtable();
byte[] buff=jr.getResourceFromHashtable(args[1]);
if (buff == null) {
	System.out.println("Could not find " + args[1] + ".");
} 
else {
	System.out.println("Found "+
		args[1]+ " (length=" + buff.length + ").");
}
} 
*/
public final class JarResources {

/**
Hashtable for holding the contents of the jar file
*/
private Hashtable __jarResources_Hashtable = new Hashtable();

/**
Hashtable for holding the sizes of the files in the jar file
*/
private Hashtable __sizes_Hashtable = new Hashtable();  

/**
The name of the jar file being worked with
*/
private String __jarFileName;
	
/**
Creates a JarResources, extracting all resources from a Jar
into an internal hashtable, keyed by resource names.<p>

The jarFileName that is passed into this method should be the valid name
and path of a jar file name.  The path can be relative, or absolute. <p>

For instance, consider the following directory structure:<br>
<tt>
c:\tmp<br>
c:\tmp\test<br>
c:\tmp\test\lib<br>
c:\tmp\test\lib\images.jar<br>
<br></tt>
If a user is in the c:\tmp\test directory running a program that requires a
jar file, and the JarResources class is being initialized to read from the
images.jar file, the path to the jar file can be given as:<br>
<tt>
lib\images.jar <br>
c:\tmp\test\lib\images.jar<br>
</tt><p>
If the user was in the same directory as the jar file, the name of the jarfile
would be enough.
@param jarFileName a jar or zip file
*/
public JarResources(String jarFileName) {
	__jarFileName = jarFileName;
	Message.printStatus ( 1, "", "SAMX JarResources" );
	init();
}

/**
Reads all the resources in the jar file into a hashtable (for quicker 
access)
*/
public void buildResourceHashtable() {
	try {
		// extract resources and put them into the hashtable.
		ZipInputStream zis = new ZipInputStream(
			new BufferedInputStream(
			new FileInputStream(__jarFileName)));
		ZipEntry ze = null;

		while ((ze = zis.getNextEntry()) != null) {
			if (ze.isDirectory()) {
				continue;
			}
			
			if (Message.isDebugOn) {
				Message.printDebug(1, 
					"JarResources.buildResourceHashtable",
					"ze.getName() = " + ze.getName() 
					+ ", getSize() = " + ze.getSize());
			}
	
			int size = (int) ze.getSize();
			// -1 means unknown size. 
			if (size == -1) {
				size = ((Integer) __sizes_Hashtable.get(
					ze.getName())).intValue();
			}
			
			byte[] b = new byte[(int) size];
			int rb = 0;
			int chunk = 0;
			while (((int) size - rb) > 0) {
				chunk = zis.read(b, rb, (int) size - rb);
				if (chunk == -1) {
					break;
				}
				rb += chunk;
			}
			
			// add to internal resource hashtable
			__jarResources_Hashtable.put(ze.getName(), b);
			if (Message.isDebugOn) {
				Message.printDebug(1,
					"JarResources.buildResourceHashtable",
					ze.getName() + "  rb=" + rb 
					+ ", size = " + size 
					+ ", csize = " 
					+ ze.getCompressedSize());
			}
		}
		zis.close();
	} 
	catch (Exception e) {
		Message.printWarning(2, "buildResourceHashtable",
			"An error occured while building the resource "
			+ "hashtable.");
		Message.printWarning(2, "buildResourceHashtable", e);
	}
}

// TODO SAM 2007-04-09 Evaluate whether needed or should be public
/**
Dumps a zip entry into a string for debugging purposes.  The string 
is of the form:<p>
<b>[d|f] [stored|deflated] [name] [size (/ deflated_size)]</b><br>
where:<br>
d: the file is a directory<br>
f: the file is a file<br>
stored: the file was not compressed in the jar<br>
deflated: the file was compressed in the jar<br>
name: the name of the file or directory in the jar<br>
size: the size of the file
deflated_size: if the file is deflated, the compressed size of the file<br>
@param ze a ZipEntry
@return the string describing the entry in the jar file
*/
/*
private String dumpZipEntry(ZipEntry ze) {
	StringBuffer sb = new StringBuffer();
	if (ze.isDirectory()) {
		sb.append("d "); 
	} 
	else {
		sb.append("f "); 
	}
	
	if (ze.getMethod() == ZipEntry.STORED) {
		sb.append("stored   "); 
	} 
	else {
		sb.append("deflated ");
	}
	
	sb.append(ze.getName());
	sb.append("\t");
	sb.append("" + ze.getSize());
	
	if (ze.getMethod() == ZipEntry.DEFLATED) {
		sb.append("/" + ze.getCompressedSize());
	}
	return sb.toString();
}
*/

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__jarResources_Hashtable = null;
	__sizes_Hashtable = null;
	__jarFileName = null;
	super.finalize();
}

/**
Returns a list of all the resources in the jar file in hashtable form.
@return a hashtable of all the resources and their file sizes.  The name
of the resource is the key, and the size of the resource is the value
*/
public Hashtable getResourcesHashtable() {
	return(__sizes_Hashtable);
}

/**
Returns a String Vector of the names of all the resources in the jar file 
@return a String Vector of the names of all the resources in the jar file
*/
public List getResourcesList() {
	Enumeration e = __sizes_Hashtable.keys();
	List v = new Vector(__sizes_Hashtable.size());
	while (e.hasMoreElements()) {
		String key = (String)e.nextElement();
		v.add(key);
	}

	return v;					
}

/**
Extracts a jar resource as a byte array.  The array can be used in 
methods that take byte array parameters.  For instance, if an image 
is read from a jar file, it can be used in ImageIcon(byte[] imageData);<p>
@param name a resource name.
@return a resource as a byte array
*/
public byte[] getResourceFromHashtable(String name) {
	return (byte[])__jarResources_Hashtable.get(name);
}

/**
Returns a resource from the jar file by reading the resource directly
from the jar file.  This is less efficient that using the hashtable if there
are many resources being retrieved.
@param resourceName the name of the resource to return
@return a resource in a byte array
*/
public byte[] getResourceFromJar(String resourceName) {
	try {
		// extract resources and put them into the hashtable.
		ZipInputStream zis = new ZipInputStream(
			new BufferedInputStream(
			new FileInputStream(__jarFileName)));
		ZipEntry ze = null;
		
		while ((ze = zis.getNextEntry()) != null) {
			if (ze.isDirectory()) {
				continue;
			}
			
			int size=(int)ze.getSize();
			// -1 means unknown size. 
			if (size == -1) {			
				size = ((Integer) __sizes_Hashtable.get(
					ze.getName())).intValue();
			}
	
			if (ze.getName().equalsIgnoreCase(resourceName)) {
				byte[] b = new byte[(int) size];
				int rb = 0;
				int chunk = 0;
				
			chunk = zis.read(b, rb, size);
				rb += chunk;
				while ((size - rb) > 0) {
					chunk = zis.read(b, rb, (size - rb));
	
					if (chunk == -1) {
						break;
					}
					
					rb += chunk;
				}
				return b;
			}
		}
		
		zis.close();
		return null;
	}
	catch (Exception e) {
		Message.printWarning(2, "getResourceFromJar",
			"An error occured while getting the resource from "
			+ "the jar.");
		Message.printWarning(2, "getResourceFromJar", e);
	}
	return null;
}

/**
Returns the size of the specified resource in the jar file in bytes.
@param resourceName the name of the resource to return the size of
@return an int of the size of the specified jar file resource
*/
public int getSize(String resourceName) {
	return ((Integer) __sizes_Hashtable.get(resourceName)).intValue();
}   	

/**
Reads the jar file and creates a hashtable of all the resources in the
hashtable and their filesizes.
*/
private void init() {
	try {
		// extracts just sizes only. 
		ZipFile zf = new ZipFile(__jarFileName);
		Enumeration e = zf.entries();
		while (e.hasMoreElements()) {
			ZipEntry ze = (ZipEntry) e.nextElement();
			if (!ze.isDirectory()) {
				__sizes_Hashtable.put(
					  ze.getName(),
					  new Integer((int) ze.getSize()));
			}
		}
	}
	catch (Exception e) {
		Message.printWarning(2, "init", "An error occured while "
			+ "initializing the JarResources.");
		Message.printWarning(2, "init", e);
	}	 
}

} // End of JarResources class.
