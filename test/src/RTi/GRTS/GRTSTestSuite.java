package RTi.GRTS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import RTi.Util.Test.TestCollector;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class GRTSTestSuite extends TestCase {

	private static ArrayList testList;
	
	public GRTSTestSuite(String testname)
	{
		super(testname);
	}

	public GRTSTestSuite()
	{	
	}	
	
	public static Test suite() throws ClassNotFoundException
	{
		testList = new ArrayList();
	    TestSuite suite = new TestSuite();
	    TestCollector tests = new TestCollector();
	    File path = new File("..\\test\\unit\\src");
	    tests.visitAllFiles(path);
	    testList = tests.getTestList();
	    
	    for(int i = 0; i < testList.size(); i++)
	    {
	    	String testName = (testList.get(i).toString());
	    	String test = tests.formatFileName(testName);
	    	suite.addTestSuite(Class.forName(test));
	    }
		
	    return suite;
	}
	
	
}

