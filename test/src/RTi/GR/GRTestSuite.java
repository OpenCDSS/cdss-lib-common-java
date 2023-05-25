package RTi.GR;
import RTi.Util.Test.TestCollector;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class GRTestSuite extends TestCase {

	private static List<String> testList;
	
	public GRTestSuite(String testname) {
		super(testname);
	}

	public GRTestSuite() {	
	}	
	
	public static Test suite() {
		testList = new ArrayList<String>();
	    TestSuite suite = new TestSuite();
	    TestCollector tests = new TestCollector();
	    File path = new File("..\\test\\unit\\src");
	    tests.visitAllFiles(path);
	    testList = tests.getTestList();
	    
	    for(int i = 0; i < testList.size(); i++) {
	    	String testName = (testList.get(i).toString());
	    	String test = tests.formatFileName(testName);
	    	/* TODO smalers 2023-05-19 need to fix.
	    	try {
				suite.addTestSuite(Class.forName(test));
	    	} 
	    	catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			*/
	    }
		
	    return suite;
	}
}