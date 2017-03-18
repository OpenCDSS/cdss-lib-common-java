package RTi.TS;


import RTi.Util.Test.TestCollector;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import java.util.List;
import java.io.File;

public class TSTestSuite extends TestCase {

	private static List<String> testList;
	
	public TSTestSuite(String testname)
	{
		super(testname);
	}

	public TSTestSuite()
	{	
	}	
	
	public static Test suite()
	{
	    TestSuite suite = new TestSuite();
	    TestCollector tests = new TestCollector();
	    File path = new File("test/src");
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

