// TestCase -- this class extends the basic JUnit TestCase class and adds functionality

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

// ----------------------------------------------------------------------------
// TestCase -- this class extends the basic JUnit TestCase class and adds
//	additional functionality.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2005-09-19	J. Thomas Sapienza, RTi	Initial version.
// 2005-09-20	JTS, RTi		Overrode the int, boolean and String
//					versions of assertEquals() in order to
//					add hooks for logging to a file.
// 2005-09-21	JTS, RTi		Further revision.
// 2006-06-05	JTS, RTi		Renamed to HydroBaseTestCase.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

// TODO SAM 2007-05-09 Why is this related to HydroBase?

package RTi.Util.Test;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import RTi.Util.Time.DateTime;

/**
This class contains methods for use in running JUnit tests.  Primarily,
this class contains <code>assertEquals()</code> methods not provided by the
base JUnit code.<p>
Because this class is primarily for use within specific tests in a suite of
JUnit tests, it needs to be able to report to the test runner that a test 
has failed.  It does this by use of the <code>internalFail()</code> 
method.
So it should be noted that even though this class 
extends <code>TestCase</code>, it is not intended to ever be run as a test.
Instead, it can be used as a base class for other TestCase classes.<p>
<b>Notes on Use:</b><p>
<ul>
<li>The first test added to any TestSuite must be initializeTestSuite()</li>
<li>If information should be logged to a log file, the TestSuite being run 
should have the <code>test_openLogFile</code> test added before any actual
test.  This isn't 
an actual test, but will ensure that the logging information is set up properly.
</li>
<li>Before adding the <code>test_openLogFile</code> test to a Suite, call the
static methods <code>setLogFileDirectory()</code> and 
<code>setLogFileBaseName()</code> to specify how the log file should be named.
</li>
<li>The first line of every TestCase should be 
<code>initializeTestCase(...)</code>.
</li>
<li>The last line of every TestCase should be 
<code>checkTestStatus()</code>.
</li>
*/
public class HydroBaseTestCase
extends junit.framework.TestCase {

/**
Specifies whether to timestamp the log filename.
*/
private static boolean __useTimestamp = true;

/**
The output levels for writing to Message.  In order, they correspond to:
Debug/Term, Debug/Log, Status/Term, Status/Log, Warning/Term, Warning/Log.
*/
private static int[] __debugLevels = { 0, 0, 0, 2, 0, 3 };

/**
Used to keep track of the number of errors that occurred in running a single
test case.  Static so that it can be accessed in <code>internalFail()</code>.
*/
private static int __errorCount = 0;

/**
Used to count the number of failed tests in a test suite.
*/
private static int __failCount = 0;

/**
Used to count the number of successful tests run in a test suite.
*/
private static int __passCount = 0;

/**
Used to count the total number of errors occuring.
*/
private static int __totalErrorCount = 0;

/**
The directory in which the log file will be placed.
*/
private static String __logDir = null;

/**
The base name of the log file.
*/
private static String __logName = null;

/**
The name of the current test being run.
*/
private String __name = null;

/**
Constructor.
@param name the name of the test being run.
*/
public HydroBaseTestCase(String name) {
	super(name);
}

/**
Checks whether two primitive <code>boolean</code> variables are equal.
If not, the test will fail via <code>internalFail()</code>.
@param d1 the first boolean to check.
@param d2 the second boolean to check.
*/
public static void assertEquals(boolean b1, boolean b2) {
	Message.printStatus(4, "", "1: " + b1 + "  2: " + b2);
	if (b1 != b2) {
		internalFail("Values " + b1 + " and " + b2 
			+ " are not the same.");
	}
	else {
		Message.printStatus(3, "", "    PASS");
	}
}

/**
Checks whether two primitive <code>double</code> variables are equal.
If not, the test will fail via <code>internalFail()</code>.
@param d1 the first double to check.
@param d2 the second double to check.
*/
public static void assertEquals(double d1, double d2) {
	Message.printStatus(4, "", "1: " + d1 + "  2: " + d2);
	if (d1 != d2) {
		internalFail("Values " + d1 + " and " + d2 
			+ " are not the same.");
	}
	else {
		Message.printStatus(3, "", "    PASS");
	}
}

/**
Checks whether two primitive <code>float</code> variables are equal.
If not, the test will fail via <code>internalFail()</code>.
@param f1 the first float to check.
@param f2 the second float to check.
*/
public static void assertEquals(float f1, float f2) {
	Message.printStatus(4, "", "1: " + f1 + "  2: " + f2);
	if (f1 != f2) {
		internalFail("Values " + f1 + " and " + f2 
			+ " are not the same.");
	}
	else {
		Message.printStatus(3, "", "    PASS");
	}
}

/**
Checks whether two <code>int</code> variables are equal.
If not, the test will fail via <code>internalFail</code>.
@param i1 the first int to check
@param i2 the second int to check.
*/
public static void assertEquals(int i1, int i2) {
	Message.printStatus(4, "", "1: " + i1 + "  2: " + i2);
	if (i1 != i2) {
		internalFail("int 1 is " + i1 + " and int 2 is " + i2);
	}
	else {
		Message.printStatus(3, "", "    PASS");
	}
}

/**
Checks whether two <code>String</code> variables are equal.
If not, the test will fail via <code>internalFail()</code>.
@param s1 the first String to check.
@param s2 the second String to check.
*/
public static void assertEquals(String s1, String s2) {
	Message.printStatus(4, "", "1: '" + s1 + "'   2: '" + s2 + "'");

	if (s1 == null && s2 == null) {
		Message.printStatus(3, "", "    PASS");
		return;
	}
	else if (s1 == null) {
		internalFail("String 1 is null and String 2 is '" + s2 + "'");
	}
	else if (s2 == null) {
		internalFail("String 1 is '" + s1 + "' and String 2 is null");
	}
	else if (!s1.equals(s2)) {
		internalFail("String 1 is '" + s1 + "' and String 2 is '" 
			+ s2 + "'");
	}
	else {
		Message.printStatus(3, "", "    PASS");
	}
}

/**
Checks to see whether the data objects in two Vectors of data contain the 
same values in the same order.
<b>Important Notes:</b><p>
<ul>
<li>The object comparison will be made via a call to <code>equals()</code>.
<li>The Vectors can contain null objects.</li>
<li>The Objects in the same position in each Vector can be of different types
(although this will be reported as an error).</li>
</ul>
@param v1 the first Vector to check.
@param v2 the second Vector to check.
@throws Exception if there are any errors reading through the Vector data.
*/
public static void assertEquals(List<Object> v1, List<Object> v2) 
throws Exception {
	if (v1 != null && v2 != null) {
		Message.printStatus(4, "", "  Comparing vectors of " + v1.size()
			+ " and " + v2.size() + " elements.");
	}

	if (v1 == null || v2 == null) {
		if (v1 == null && v2 == null) {
			// null Vectors are basically the same
			return;
		}
		else if (v1 == null) {
			internalFail(
				"Vector 1 is null and Vector 2 is not null.");
			return;
		}
		else {
			internalFail(
				"Vector 1 is not null and Vector 2 is null.");
			return;
		}
	}

	int size1 = v1.size();
	int size2 = v2.size();

	if (size1 != size2) {
		internalFail(
			"Vector 1 contains " + size1 + " elements and Vector 2 "
			+ "contains " + size2 + " elements.");
		return;
	}

	Object o1 = null;
	Object o2 = null;
	for (int i = 0; i < size1; i++) {
		o1 = v1.get(i);
		o2 = v2.get(i);

		Message.printStatus(4, "", "O1: '" + o1 + "'   O2: '" + o2
			+ "'");

		if (o1 == null && o2 == null) {
			continue;
		}
		else if (o1 == null) {
			internalFail(
				"At Vector position " + i + ": The Object in "
				+ "Vector 1 is null.");
			return;
		}
		else if (o2 == null) {
			internalFail(
				"At Vector position " + i + ": The Object in "
				+ "Vector 2 is null.");
			return;
		}		

		Class<?> c1 = o1.getClass();
		Class<?> c2 = o2.getClass();
	
		if (c1 != c2) {
			internalFail(
				"At Vector position " + i + ": The Object in "
				+ "Vector 1 is of class " + c1 + " while the "
				+ "Object in Vector 2 is of class " + c2);
			return;
		}

		if (!o1.equals(o2)) {
			internalFail("The objects (" + o1 + ") and (" + o2 
				+ ") are not equal.");
			return;
		}
	}

	Message.printStatus(3, "", "    PASS");
}

/**
Checks to see whether the TestCase was run successfully.
*/
public void checkTestStatus() {
Message.printStatus(1, "", "EC: " + __errorCount);
	if (__errorCount == 1) {
		Message.printWarning(2, "", 
			"FAIL: '" + __name + "'.  1 error.");
		__failCount++;
		fail("FAIL: '" + __name + "'.  1 error.");
	}
	else if (__errorCount > 1) {
		Message.printWarning(2, "", 
			"FAIL: '" + __name + "'.  " + __errorCount +" errors.");
		__failCount++;
		fail("FAIL: '" + __name + "'.  " + __errorCount + " errors.");
	}
	else {
		Message.printStatus(2, "", "PASS: '" + __name + "'.");
		__passCount++;
	}
}

/**
Returns the number of failed tests.
@return the number of failed tests.
*/
public static int getFailCount() {
	return __failCount;
}

/**
Returns the number of passed tests.
@return the number of passed tests.
*/
public static int getPassCount() {
	return __passCount;
}

/**
Initializes settings in this class for when a new TestCase is being run and
prints header information for the test to the log.
@param name the name of the test case being run.
*/
public void initializeTestCase(String name) {
	__name = name;
	__errorCount = 0;
	
	Message.printStatus(2, "", "");
	Message.printStatus(2, "", "Running tests for " + name);
	Message.printStatus(2, "", "-----------------------------------");	
}

/**
Initializes settings in this class for when a new TestSuite is being run.
*/
public static HydroBaseTestCase initializeTestSuite() {
	return new HydroBaseTestCase("test_initializeTestSuite");
}

/**
A fail method specific to this class that does some special error handling and
log reporting of the error that occurred.  This method is not named 
<code>fail()</code> because access to the superclass <code>fail()</code> is
needed, and since the superclass method is static it cannot be called via:
<code>super.fail()</code>.
@param message the message to report as the reason a test failed.
*/
public static void internalFail(String message) {
//	Message.printWarning(2, "", "FAIL: " + message);
	try {
		// this exception is thrown so that the error shows up 
		// in the log file
		throw new Exception("FAIL: " + message);
	}
	catch (Exception e) {
		Message.printWarning(3, "", e);
	}

	__errorCount++;
	__totalErrorCount++;
}

/**
Returns a TestCase that opens a log file for writing.  This TestCase should be
the first one added to a TestSuite.  Prior to calling this method, the log
static log file information should be set up with calls to 
<code>setLogFileDirectory</code> and <code>setLogFileBaseName</code>.
@return a TestCase that opens a lof file for writing.
*/
public static HydroBaseTestCase openLogFile() {
	return new HydroBaseTestCase("test_openLogFile");
}

/**
Sets the base name of the log file.  To this will be added a time stamp and 
the extension ".log".
@param baseName the base name of the log file.
*/
public static void setLogFileBaseName(String baseName) {
	__logName = baseName;
}

/**
Sets the name of the directory in which the log file will be opened.
@param directory the directory in which the log file will be opened.
*/
public static void setLogFileDirectory(String directory) {
	__logDir = directory;
}

/**
Specifies whether to timestamp the log filename.  Default is true.
@param useTimestamp if true, the log file name will have a timestamp.  
*/
public static void setLogFileTimeStamp(boolean useTimestamp) {
	__useTimestamp = useTimestamp;
}

/**
Sets output levels for the Message class.
@param debugTerm the ouput level for writing DEBUG messages to the terminal.
Default is 0.
@param debugLog the ouput level for writing DEBUG messages to the log.
Default is 0.
@param statusTerm the ouput level for writing STATUS messages to the terminal.
Default is 0.
@param statusLog the ouput level for writing STATUS messages to the log.
Default is 2.
@param warningTerm the ouput level for writing WARNING messages to the terminal.
Default is 0.
@param warningLog the ouput level for writing WARNING messages to the log.
Default is 3.
*/
public static void setOutputLevels(int debugTerm, int debugLog, int statusTerm,
int statusLog, int warningTerm, int warningLog) {
	__debugLevels[0] = debugTerm;
	__debugLevels[1] = debugLog;
	__debugLevels[2] = statusTerm;
	__debugLevels[3] = statusLog;
	__debugLevels[4] = warningTerm;
	__debugLevels[5] = warningLog;
}

public void test_initializeTestSuite() {
	__passCount = 0;
	__failCount = 0;
	__totalErrorCount = 0;
}

/**
Opens the log file for writing.
*/
public void test_openLogFile() {
	Message.setDebugLevel(Message.TERM_OUTPUT, __debugLevels[0]);
	Message.setDebugLevel(Message.LOG_OUTPUT, __debugLevels[1]);
	Message.setStatusLevel(Message.TERM_OUTPUT, __debugLevels[2]);
	Message.setStatusLevel(Message.LOG_OUTPUT, __debugLevels[3]);
	Message.setWarningLevel(Message.TERM_OUTPUT, __debugLevels[4]);
	Message.setWarningLevel(Message.LOG_OUTPUT, __debugLevels[5]);

	Message.setOutputFile(Message.TERM_OUTPUT, (PrintWriter)null);
	PrintWriter ofp;
	String logFile = null;
	try {   
		DateTime dt = new DateTime(DateTime.DATE_CURRENT);
		if (__logDir == null) {
			__logDir = ".";
		}
		if (__logName == null) {
			__logName = "log";
		}

		String filename = __logName;
		
		if (__useTimestamp) {
			filename += "_" + dt.getYear() + "-"
				+ StringUtil.formatString(dt.getMonth(), "%02d")
				+ "-"
				+ StringUtil.formatString(dt.getDay(), "%02d")
				+ "_" 
				+ StringUtil.formatString(dt.getHour(), "%02d")
				+ "" 
				+ StringUtil.formatString(dt.getMinute(), 
					"%02d");
		}

		filename += ".log";

		ofp = Message.openLogFile(__logDir + File.separator 
			+ filename);
		Message.setOutputFile(Message.LOG_OUTPUT, ofp);
	}
	catch (Exception e) {
		Message.printWarning(2, "", 
			"Unable to open log file \"" + logFile + "\"");
	}
}

public static HydroBaseTestCase testSummary() {
	return new HydroBaseTestCase("test_testSummary");
}

public void test_testSummary() {
	Message.printStatus(2, "", "");
	Message.printStatus(2, "", "SUMMARY");
	Message.printStatus(2, "", "---------------------------------");
	
	int testCount = __failCount + __passCount;
	String plural = "s";
	if (testCount == 1) {
		plural = "";
	}
	Message.printStatus(2, "", "" + testCount + " test" + plural + " run.");

	plural = "s";
	if (__failCount == 1) { 
		plural = "";
	}
	String plural2 = "s";
	if (__totalErrorCount == 1) {
		plural2 = "";
	}
	Message.printStatus(2, "", "" + __failCount + " test" + plural 
		+ " failed (" + __totalErrorCount + " error" + plural2 + ")");

	plural = "s";
	if (__passCount == 1) { 
		plural = "";
	}		
	Message.printStatus(2, "", "" + __passCount + " test" + plural 
		+ " passed.");
}

}
