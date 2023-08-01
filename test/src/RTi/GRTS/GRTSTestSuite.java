package RTi.GRTS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.Test.TestCollector;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class GRTSTestSuite extends TestCase {

	private static List<String> testList;
	
	public GRTSTestSuite(String testname) {
		super(testname);
	}

	public GRTSTestSuite() {	
	}	
	
	public static Test suite() throws ClassNotFoundException {
		testList = new ArrayList<String>();
	    TestSuite suite = new TestSuite();
	    TestCollector tests = new TestCollector();
	    File path = new File("..\\test\\unit\\src");
	    tests.visitAllFiles(path);
	    testList = tests.getTestList();
	    
	    for(int i = 0; i < testList.size(); i++) {
	    	//String testName = (testList.get(i).toString());
	    	//String test = tests.formatFileName(testName);
	    	/* TODO smalers 2023-05-19 need to fix
	    	suite.addTestSuite(Class.forName(test));
	    	*/
	    }
		
	    return suite;
	}
	
}