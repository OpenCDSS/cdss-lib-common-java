// ----------------------------------------------------------------------------
// TestUtil - Utility methods for setting up and running unit tests.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2006-06-04	J. Thomas Sapienza, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.Test;

import java.io.File;

import java.lang.reflect.Method;

import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.String.StringUtil;

import RTi.Util.Table.DataTable;

/**
Utility class for test code.<p>

The following is a description of the process by which an Ant file starts up
a testing process, and the order in which constructors and methods are called:
<p>
<ul>
<li>	
The Ant target:<p>
<code>
<target name="test"
	depends="compile, compile_test"
	description="Runs the unit tests.">
	<junit 
		fork="yes">
		<classpath refid="classpath"/>
		<sysproperty 
			key="PACKAGE_NAME" 
			value="${package}"/>
		<formatter 
			type="plain"/>
		<test 
			name="RTi.Util.Test.PackageSuite"
			todir="${test_results_dir}"
			outfile="UnitTestOutput-${TODAY}"/>
	</junit>
</target>
</code>
<p>
starts up the console TestRunner with the RTi.Util.Test.PackageSuite class.  
PackageSuite is a class that allows all the tests in a given package, or 
alternately all the package tests found in a given file (e.g., 
test\data\TestsToRun.txt), to be run at once.<p>

The Ant task sets a system property named "PACKAGE_NAME" to the value of the
current package.  PackageSuite uses this value to determine the classes it 
should collect to be executed by the TestRunner.<p>

When a TestRunner starts up and is passed a class for which to run test methods,
the TestRunner automatically calls the class's "public static Test suite()" 
method.  This method creates the suite of test methods that will be executed 
by the TestRunner.  The test methods are collected into a 
junit.framework.TestSuite object.<p>

PackageSuite's suite() method will create a TestSuite composed of all the 
test methods in the test classes of the package.
</li>

<li>
PackageSuite calls utility code in RTi.Util.Test.TestUtil that does the
work of locating the test classes in the package specified by the 
"PACKAGE_NAME" property.  TestUtil not only locates the classes that will
need to be executed by the TestRunner, but it also adds them to the TestSuite
that will be returned from PackageSuite.suite().  It locates classes by 
doing a search of "\build\test" (by default) for all classes ending in
"Test.class".
</li>

<li>
When TestUtil is adding test classes to the TestSuite, it does so by 
dynamically instantiating the classes using Class.forName().newInstance();
(For more information on Class.forName(), see 
<a href="http://mindprod.com/jgloss/classforname.html">
http://mindprod.com/jgloss/classforname.html</a>).  
TestUtil then uses reflection to determine whether the instantiated class
has a "suite()" method.  If the instantiated class does have
a "suite()" method, that method is called and the tests returned from the method
are added to the TestSuite.  This TestSuite is the object returned from 
PackageSuite.suite(), and is the suite of tests run by the TestRunner.
</li>

<p><b>Note:</b><p>

The following code is all in place to allow the running of individual test case
classes within a SINGLE package.  Expanding it to allow running tests across
multiple packages (provided all the packages' class files could be found under
the current working directory) would require the following, I think:<p>
<pre>
1) Create a MultiPackageSuite class (for clarity) which takes a system property
   that can be one of those things:
   - an incomplete package (such as RTi.Util)
   - a set of comma-separate packages (such as RTi.Util.GUI,RTi.Util.String)

2) Add code to TestUtil.  First, a method like "multiPackageSuite(Vector)". 
   The Vector is a parameter of Strings to match packages against.  This method
   should search all the directories under the current working directory (which
   must be in the classpath) or an alternate directory (provided it is in the 
   class path) and checks all the .class files it finds.  It first checks to see
   if the class has a suite() method.  If not, the class is skipped.  Next,
   it checks to see if the class is in a matching package.  If so, the class
   is added to the TestSuite to run.
   
3) The assumption would be that when running multi-package tests, a developer
   cannot choose to run specific test classes or test methods.  All tests in 
   the selected packages are run at once.  If a multi-package test fails, 
   the developer will need to peg down to the specific class that is causing 
   errors and do development and testing within it. 
</pre><p>
Overall, the above would be a bit of work, and probably only for the sake of
RTiCore.  I'm honestly not 100% sure it's worth the time. 
*/
public class TestUtil {

/*
REVISIT (JTS - 2006-06-07)
RTi Messaging
------------------------------------------------------------

The code below uses the JUnit logging mechanism, and for that reason, output
is printed with System.out.println() and exceptions with printStackTrace().

However, we will want to enable the ability to log messages from classes into
a regular RTi log file.  I have several ideas on this front, depending on 
how we want to do this logging:

1) Log to a single RTi log file for the duration of the entire test run.

To do this would be relatively easy.  Since PackageSuite.suite() will be the
entry point for collecting all the tests for a single package, it could be 
extended to something like (pseudocode for now):

public static void suite() {
	String packageName = System.getProperty("PACKAGE_NAME");

	if (packageName == null) {
		throw new Exception(
			"System property 'PACKAGE_NAME' must be set to the "
			+ "name of the package for which to execute unit "
			+ "tests.");
	}

	//----------------------
	Message.openLogFile(packageName + ".log");
	
	return TestUtil.suite(packageName);
}

--------------------------------------------------------------------------------

2) If we want to do a single RTi log file per test class, it would be a 
bit more complicated, but it could be done.  We would rely on the automatic
setUp() methods and the use of a utility class, like so:

(the following would be contained within a test class)

/--
Whether the log file for this class has been opened.
--/
private static boolean __logFileOpened = false;

/--
The name of the log file to be written for this test class.
-/
private Static String __logName = "StringUtilTest.log";

/--
Called before a test is run.
-/
public void setUp() {
	if (!__logFileOpened) {
		Message.openLogFile(__logName);
		__logFileOpened = true;
	}
}

--------------------------------------------------------------------------------

Note that in both the above examples, there is no way of knowing when to close
the log file.   While it's possible to determine the first time that setUp()
is called, it's not possible to determine the last time in a class that
tearDown() will be called.  Likewise, when PackageSuite.suite() is called,
that is the entry point into the testing, but there's no way to put hooks
into when the testing is done. 

A few things we could do about this:
- in the finalize() method of the Message class, check for an open log file
  and if one is found, close it cleanyl.
- when a new log file is opened in Message, close the previous one.  
  In fact, this might be done already.

--------------------------------------------------------------------------------

Finally, we will need some way to get the logging information into the
log file opening (ie, to determine the levels at which debug, status, and
warning are opened).  The most-easily configurable way, I feel, would
be to pass this information into the testing framework via system properties
(like how "PACKAGE_NAME" is used in PackageSuite).  

This would require a change to the "test" target in the build files to something
like this (note the 3 new <sysproperty> tags):

<target name="test"
	depends="compile, compile_test"
	description="Runs the unit tests.">

	<junit 
		fork="yes">
		<classpath refid="classpath"/>
		<sysproperty
			key = "PACKAGE_NAME"
			value="${package}"/>
		<sysproperty
			key = "DEBUG_LEVEL"
			value="${debug_level}/>
		<sysproperty
			key = "STATUS_LEVEL"
			value="${status_level}/>
		<sysproperty
			key = "WARNING_LEVEL"
			value="${warning_level}/>
		<formatter 
			type="plain"/>
		<test 
			name="RTi.Util.Test.PackageSuite"
			todir="${test_results_dir}"
			outfile="UnitTestOutput-${TODAY}"/>
	</junit>
</target>


Then, in the top of the build.xml file, the levels for debug, status, and
warning could be set like this:

<property name="debug_level"	value="0"/>
<property name="status_level"	value="2"/>
<property name="warning_level"	value="1"/>

Changing the values would require no recompilation, which is the benefit of
doing it in this (slightly more complicated) way.
*/

/**
The String that, if found in the file of the tests to be executed, will 
indicate to the TestSuite builder that all the tests in the package should
be run.
*/
private static String __ALL_TESTS = "ALL_TESTS";

/**
The base directory in which suite() will search to find class files that 
contain "suite()" methods.
*/
private static String __BUILD_DIRECTORY_NAME 
	= "build" + File.separator + "test" + File.separator;
	
/**
The file that contains the names of test classes that should be executed. 
By default, it is "TestsToRun_[user name].txt".
*/
private static String __TESTS_TO_RUN_FILE_NAME 
	= "TestsToRun_" + IOUtil.getProgramUser() + ".txt";

/**
The directory in which the file containing the name of test classes to be
executed is found.  By default, it is "test\data\".
*/
private static String __TESTS_TO_RUN_FILE_DIRECTORY_NAME 
	= "test" + File.separator + "data" + File.separator;

/**
Returns the base directory in which suite() will search to find class files. 
By default, this is "build\test\".
@return the base directory in which suite() will search to find class files.
*/
public static String getBuildDirectoryName() {
	return __BUILD_DIRECTORY_NAME;
}

/**
Returns the file that contains the names of test classes that should be
executed.  By default, it is "TestsToRun_[user name].txt".
@return the file that contains the names of test classes that should be
executed.
*/
public static String getTestsToRunFileName() {
	return __TESTS_TO_RUN_FILE_NAME;
}

/**
Returns the directory in which the tests-to-run file is contained.  By default,
it is "test\data\".
@return the directory in which the tests-to-run file is contained.
*/
public static String getTestsToRunFileDirectoryName() {
	return __TESTS_TO_RUN_FILE_DIRECTORY_NAME;
}

/**
Reads the list of tests that should be added to a package's TestSuite from the
file that defines the tests to run (see getTestsToRunFile() and 
setTestsToRunFileName())
See the memo at 
I:\DEVELOP\doc\ProceduresAndGuidelines\SoftwareDirectoryStandard\ 
for more information on the format of the file.
@return the Vector of tests to add to the TestSuite, or null if all tests in the
package should be added to the suite.  Any error will result in null being
returned.
*/
public static Vector readTestList() {
	// Try to read the TestsToRun file. 
	File testFile = new File(__TESTS_TO_RUN_FILE_DIRECTORY_NAME 
		+ __TESTS_TO_RUN_FILE_NAME);
			
	if (!testFile.exists()) {
		// If the file doesn't exist, return null so that the
		// TestSuite builder will add all tests in the package
		// to the suite.
		System.out.println("TestUtil.readTestList(): Tests to run file "
			+ "(" + __TESTS_TO_RUN_FILE_DIRECTORY_NAME 
			+ __TESTS_TO_RUN_FILE_NAME + ") does not exist.");
		return null;
	}

	// Read the file into a DataTable.  This is the easiest way
	// to process the file quickly.  

	PropList props = new PropList("");
	props.add("Delimiter=\t");
	props.add("CommentLineIndicator=#");
	props.add("TrimStrings=true");

	try {
		// Read the file into the table, then iterate through
		// the records that were read and all them to a
		// Vector.  The DataTable code does all the work of
		// trimming fields and skipping comment lines.
		
		DataTable table = DataTable.parseFile(testFile.toString(), 
			props);
			
		int num = table.getNumberOfRecords();
		Vector v = new Vector();
		String s = null;
		
		// Create the Vector names of test classes to be added to
		// the TestSuite by reading them from the tests-to-run file.  
		// This Vector can contain duplicate values if classes are
		// listed more than once in the file, but that will result
		// in the classes' tests being run more than once.
		
		for (int i = 0; i < num; i++) {
			s = (String)table.getFieldValue(i, 0);

			// If __ALL_TESTS is found, ignore everything
			// else in the file and return null, causing
			// all tests in the package to be added to the
			// test suite.
			
			if (s.equalsIgnoreCase(__ALL_TESTS)) {
				System.out.println("TestUtil.readTestList(): "
					+ "'" + __ALL_TESTS + "' found in "
					+ __TESTS_TO_RUN_FILE_DIRECTORY_NAME 
					+ __TESTS_TO_RUN_FILE_NAME + "; "
					+ "all test classes will be added "
					+ "to TestSuite.");
				return null;
			}

			v.add(table.getFieldValue(i, 0));
		}

		// Sort the list of test classes into alphabetical order.  
		// This will not affect the order in which the specific tests
		// are run, but will ensure that the classes are always run 
		// in an alphabetical order, for clarity.
		java.util.Collections.sort(v);
		return v;
	}
	catch (Exception e) {
		// This is probably an error caused when trying to 
		// create the DataTable from the file.
		System.out.println("TestUtil.readTestList(): "
			+ "An error occurred while trying to read "
			+ __TESTS_TO_RUN_FILE_DIRECTORY_NAME 
			+ __TESTS_TO_RUN_FILE_NAME + " into a DataTable.  All "
			+ "package test classes will be added to TestSuite.");
		e.printStackTrace();
		return null;
	}
}

/**
Sets the base directory in which suite() will search to find class files. 
By default, this is "build\test\".  
@param buildDirectory the base directory in which suite() will search to find 
class files.
*/
public static void setBuildDirectoryName(String buildDirectoryName) {
	if (!buildDirectoryName.trim().endsWith(File.separator)) {
		__BUILD_DIRECTORY_NAME = buildDirectoryName.trim() 
			+ File.separator;
	}
	else {
		__BUILD_DIRECTORY_NAME = buildDirectoryName.trim();
	}
}

/**
Returns the file that contains the names of test classes that should be
executed.  By default, it is "TestsToRun_[user name].txt".
@param testsToRunFileName the file that contains the names of test classes that 
should be executed.
*/
public static void setTestsToRunFileName(String testsToRunFileName) {
	__TESTS_TO_RUN_FILE_NAME = testsToRunFileName.trim();
}

/**
Returns the directory in which the tests-to-run file is contained.  By default,
it is "test\data\".
@param testsToRunDirectoryName the directory in which the tests-to-run file 
is contained.
*/
public static void setTestsToRunFileDirectoryName(
String testsToRunDirectoryName) {
	if (!testsToRunDirectoryName.trim().endsWith(File.separator)) {
		__TESTS_TO_RUN_FILE_DIRECTORY_NAME 
			= testsToRunDirectoryName.trim() + File.separator;
	}
	else {
		__TESTS_TO_RUN_FILE_DIRECTORY_NAME 
			= testsToRunDirectoryName.trim();
	}
}

/**
Creates a suite of tests for the given package.  Checks for the presence of
the tests-to-run file (by default, "test\data\TestsToRun_[user name].txt", 
and if the file is found, the list of tests to be run will be taken from 
this file.  If the file is not found, TestUtil will search the classes
directory for all classes that have filenames ending with "Test.class" and 
that have a "suite()" method and will add their test methods to the TestSuite.
@param packageName the name of the package for which to create a suite of 
tests.  This must be a full package name (e.g., "RTi.Util.String"), not a 
sub-set of a package (e.g., "RTi.Util").
@return a TestSuite to be executed by a TestRunner.
*/
public static Test suite(String packageName) {
	TestSuite suite = new TestSuite();

	// Read the list of tests to execute from the TestsToRun file.
	Vector tests = readTestList();

	Class c = null;
	Method m = null;
	Object o = null;
			
	// null will be returned from readTestList() if there were any errors
	// reading TestsToRun_[].txt, or if __ALL_TESTS was found in the file.
	// Either way, if the tests Vector comes back as a null Vector the 
	// TestSuite will be populated with all the test class files found from
	// searching through the classes directories.
	
	if (tests != null) {
		// Only add the test classes in the TestsToRun file to the Test Suite.
		for (int i = 0; i < tests.size(); i++) {
			try {
				// Instantiate the class
				o = Class.forName(packageName + "." + tests.elementAt(i)).newInstance();
				c = o.getClass();

				// Try to get the suite() method from the class.  If there is no suite()
				// method, a NoSuchMethodException will be thrown.
				m = c.getMethod("suite", (Class[])null);

				// Execute the suite() method on the instantiated class.
				suite.addTest((Test)(m.invoke(o, (Object[])null)));
			}
			catch (NoSuchMethodException e) {
				// The given class did not have a suite() method.  The class will simply be skipped.
				System.out.println("TestUtil.suite(): Class '" + packageName + "."
					+ tests.elementAt(i) + "' does not contain a suite() method.");
			}						
			catch (Exception e) {
				// An error occurred creating this test and adding it to the suite.  Skip it and go
				// to the next one.
				System.out.println("TestUtil.suite(): Class '" + packageName + "."
					+ tests.elementAt(i) + "' could not be instantiated.");
				e.printStackTrace();
			}			
		}
		return suite;
	}
	else {
		// Add tests for all the test classes in the build/test 
		// directory.  Only those classes that end with "Test.class"
		// and have a "suite()" method will be added.

		// Turn the package into a directory (e.g., "RTi.Util.GUI" becomes "RTi\Util\GUI").
		String packageDir = StringUtil.replaceString(packageName, ".", File.separator);		
		try {
				
			// Get a list of all the class files in that directory.
			File dir = new File(__BUILD_DIRECTORY_NAME + packageDir);
	    	String[] children = dir.list();
			
			Vector v = new Vector();
			if (children == null) {
				// Either the directory does not exist or is not a directory.  Neither should occur.
				throw new Exception(dir.toString() + " is not a valid directory.");
			} 
			else {
				int index = 0;
				for (int i = 0; i < children.length; i++) {
					// Find the classes in the directory that end with "Test.class".
					String filename = children[i];
					if (filename.endsWith("Test.class")) {
						// Trim ".class" from the filename.
						index = filename.indexOf(".");
						v.add(filename.substring(0, index));
					}
				}
			}
		
			// Iterate through all the "XXXTest.class" files found in the directory.
			int size = v.size();

			for (int i = 0; i < size; i++) {
				try {
					// Instantiate the class
					o = Class.forName(packageName + "." + v.elementAt(i)).newInstance();
					c = o.getClass();

					// Try to get the suite() method from the class.  If there is no suite()
					// method, a NoSuchMethodException will be thrown.
					m = c.getMethod("suite", (Class[])null);

					// Execute the suite() method on the instantiated class.
					suite.addTest( (Test)(m.invoke(o, (Object[])null)));
				}
				catch (NoSuchMethodException e) {
					// The given class did not have a suite() method.  The class will simply be skipped.
					System.out.println("TestUtil.suite(): Class '" + packageName + "."
						+ v.elementAt(i) + "' does not contain a suite() method.");
				}				
				catch (Exception e) {
					// An error occurred creating this test and adding it to the suite.  Skip it 
					// and go to the next one.
					System.out.println("TestUtil.suite(): Class '" + packageName + "."
						+ v.elementAt(i) + "' could not be instantiated.");
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			// The directory containing the class files could not be found or was specified incorrectly.
			System.out.println("TestUtil.suite(): The directory containing the class files "
				+ "(" + __BUILD_DIRECTORY_NAME + packageDir + ") could not be found or was specified incorrectly.");
			e.printStackTrace();
		}
	}
	
	return suite;
}

}

/*
REVISIT (JTS - 2006-06-06)

A few other things that could be added (without much exertion):

- The ability to specify individual test methods in the test file.
  File format could be 2 columns, with the second column optional.  
  First column is the list of TestClasses to execute, second column are the
  methods within classes.  If no methods are listed, all methods are added 
  (the suite() method is called).
  
  Example file might be:

  	IOUtilTest
	PropListTest	getValue
	PropListTest	setProp
	Command
  
  This would run tests in 3 classes, and would only run two test methods within
  PropListTest.

- Given the way things are structured, might be able to assume that the only
  classes that make it to build/test/package/package/package/ are classes 
  with tests in them.  So that way, above, don't have to check the class names
  for *Test.class.
*/
  
