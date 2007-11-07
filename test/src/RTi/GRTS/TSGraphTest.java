//	2006-10-04	KAT, RTi	Added this test class to be able to test
//					a new method getNearestDateTimeLessThanOrEqualTo
//					Added the test method which creates time series
//					of several hour types and checks the validity
//					of the DateTime returned.


package RTi.GRTS;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import RTi.TS.*;
import RTi.GRTS.TSGraph;
import RTi.Util.Time.DateTime;

public class TSGraphTest extends TestCase {

	private String tsid;
	private HourTS ts3h, ts6h, ts12h, ts24h;

	public TSGraphTest(String testname)
	{
		super(testname);
	}

	public TSGraphTest()
	{
		
	}

	protected void setUp() throws Exception
	{
		double value = 1.0;
		DateTime dt1 = DateTime.parse ( "2006-01-01 07:00:00", 
				DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS);
		DateTime dt2 = DateTime.parse ( "2006-01-08 07:00:00", 
				DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS );

		// Create a 3-hour time series...
		tsid = "TestLoc.TestSource.TestType.3Hour";
		ts3h = (HourTS)TSUtil.newTimeSeries ( tsid, true );
		ts3h.setDate1 ( dt1 );	// Precision will be truncated to hour for TS
		ts3h.setDate2 ( dt2 );	// Precision will be truncated to hour for TS
		ts3h.allocateDataSpace();
	
		// Create a 6-hour time series...
		tsid = "TestLoc.TestSource.TestType.6Hour";
		ts6h= (HourTS)TSUtil.newTimeSeries ( tsid, true );
		ts6h.setDate1 ( dt1 );	// Precision will be truncated to hour for TS
		ts6h.setDate2 ( dt2 );	// Precision will be truncated to hour for TS
		ts6h.allocateDataSpace();
	
		// Create a 12-hour time series...
		tsid = "TestLoc.TestSource.TestType.12Hour";
		ts12h= (HourTS)TSUtil.newTimeSeries ( tsid, true );
		ts12h.setDate1 ( dt1 );	// Precision will be truncated to hour for TS
		ts12h.setDate2 ( dt2 );	// Precision will be truncated to hour for TS
		ts12h.allocateDataSpace();

		// Create a 24-hour time series...
		tsid = "TestLoc.TestSource.TestType.24Hour";
		ts24h= (HourTS)TSUtil.newTimeSeries ( tsid, true );
		ts24h.setDate1 ( dt1 );	// Precision will be truncated to hour for TS
		ts24h.setDate2 ( dt2 );	// Precision will be truncated to hour for TS
		ts24h.allocateDataSpace();

	}
	
	/*******************************************************************
	This method takes a time series object and a DateTime to be graphed
	and tests for a correctly aligned DateTime returned  This was added 
	because some data comes in on an offset from the interval and needs
	to start at the correct nearest even interval from the offset.  	
	********************************************************************/
	public void testgetNearestDateTimeLessThanOrEqualTo() throws Exception
	{
		DateTime start_date = new DateTime();
		start_date = DateTime.parse ( "2006-01-03 05", 
				DateTime.FORMAT_YYYY_MM_DD_HH);
		DateTime dt_result = new DateTime();
	
		System.out.println("Testing getNearestDateTimeLessThanOrEqualTo ..");
		
		System.out.println("Candidate DateTime: " + start_date.toString());
		// 3 hour time series
		TS ts = (TS)ts3h;
		dt_result = TSGraph.getNearestDateTimeLessThanOrEqualTo(start_date, ts);
		//System.out.println(dt_result.toString());
		assertEquals("2006-01-03 04", dt_result.toString());	

		// 6 hour time series
		ts = (TS)ts6h;
		dt_result = TSGraph.getNearestDateTimeLessThanOrEqualTo(start_date, ts);
		//System.out.println(dt_result.toString());
		assertEquals("2006-01-03 01", dt_result.toString());	

		// 12 hour time series
		ts = (TS)ts12h;
		dt_result = TSGraph.getNearestDateTimeLessThanOrEqualTo(start_date, ts);
		//System.out.println(dt_result.toString());
		assertEquals("2006-01-03 07", dt_result.toString());	

		// 24 hour time series
		ts = (TS)ts24h;
		dt_result = TSGraph.getNearestDateTimeLessThanOrEqualTo(start_date, ts);
		//System.out.println(dt_result.toString());
		assertEquals("2006-01-03 07", dt_result.toString());	

	}
	
	
	// Quick Unit test suite
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTest((new TSGraphTest("testgetNearestDateTimeLessThanOrEqualTo")));
		return suite;
	}
	
}

