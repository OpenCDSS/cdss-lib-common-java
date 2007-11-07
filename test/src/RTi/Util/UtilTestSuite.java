package RTi.Util;
import java.io.File;
import java.util.ArrayList;

import RTi.Util.Test.TestCollector;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class UtilTestSuite extends TestCase {

	private static ArrayList testList;
	
	public UtilTestSuite(String testname)
	{
		super(testname);
	}

	public UtilTestSuite()
	{	
	}	
	
	public static Test suite()
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
	    	try 
	    	{
				suite.addTestSuite(Class.forName(test));
	    	} 
	    	catch (ClassNotFoundException e) 
	    	{
				e.printStackTrace();
			}
	    }
		
	    return suite;
	}
	
	
}

