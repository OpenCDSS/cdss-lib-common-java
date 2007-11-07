// ----------------------------------------------------------------------------
// PackageSuite - a suite builder class that can be used to generate tests
//	for a single package.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2006-06-04	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.Util.Test;

import junit.framework.Test;

/**
This class is a generic Suite builder class that can be used to automatically
generate a TestSuite for a single package.  It allows RTi to avoid the need to 
create and maintain a separate Suite class in every package.
*/
public class PackageSuite {

/**
Create the suite for the package.  The package name should have been set 
as a Java property named "PACKAGE_NAME".
@return the test suite to be run.
@throws Exception if PACKAGE_NAME is not set.
*/
public static Test suite() 
throws Exception {
	String packageName = System.getProperty("PACKAGE_NAME");

	if (packageName == null) {
		throw new Exception(
			"System property 'PACKAGE_NAME' must be set to the "
			+ "name of the package for which to execute unit "
			+ "tests.");
	}
	
	return TestUtil.suite(packageName);
}

}
