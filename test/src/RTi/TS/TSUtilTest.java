package RTi.TS;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import RTi.Util.Time.DateTime;

public class TSUtilTest extends TestCase {

	private String tsid;
	private HourTS ts1, ts2;

	public TSUtilTest(String testname)
	{
		super(testname);
	}

	public TSUtilTest()
	{
		
	}

	protected void setUp() throws Exception
	{
		// Create a 6-hour time series...
		tsid = "TestLoc.TestSource.TestType.6Hour";
		ts1 = (HourTS)TSUtil.newTimeSeries ( tsid, true );
		DateTime dt1 = DateTime.parse ( "2006-01-01 06:00:00 Z", 
		DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ);
		DateTime dt2 = DateTime.parse ( "2006-01-02 06:00:00 Z", 
		DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ );
		ts1.setDate1 ( dt1 );	// Precision will be truncated to hour for TS
		ts1.setDate2 ( dt2 );	// Precision will be truncated to hour for TS
		ts1.allocateDataSpace();
	
		// Fabricate some time series data...
		double value = 1.0;
		for ( DateTime dt = new DateTime(dt1); dt.lessThanOrEqualTo(dt2);
		     dt.addHour(6) ) 
		{
			ts1.setDataValue ( dt, value );
			value = value + 1.0;
		}

	}
	
	/*******************************************************************
	Expect that date1 and date2 will be shifted
	Each DateTime-value pair will also be appropriate  
	The DateTime in Z will equal the new shifted time plus/minus
	the offset for that zone  The following peforms that check
	The time zone must also be in the DateTime string  This is
	also checked by gettting the current zone on the shifted date
	and comparing that with the expected time zone abbreviation
	
	If you request a value with ts2.getDataValue (), you expect
	it to be in the shifted time zone and align with the original data. 

	REVISIT: KAT	2006-10-03
	might want to add a data file in which a parameterized testing 
	approach can be used.  The file might contain a start and end time as
	well as a time zone to shift to, the time offset for that zone and
	a data value
	********************************************************************/
	public void testShiftTimeSeries() throws Exception
	{
		System.out.println("Testing shiftTimeSeries from Z to CST ...");
		int timeShift = 6;
		String zoneExpected = "CST";
		ts2 = (HourTS)TSUtil.shiftTimeZone ( ts1, "CST", null, true );

		DateTime dt2 = ts1.getDate2();
		DateTime dt, dtshifted;
		for ( dt = new DateTime( ts1.getDate1() ),
			dtshifted = new DateTime( ts2.getDate1() );
			dt.lessThanOrEqualTo( dt2 );
			dt.addHour( 6 ), dtshifted.addHour( 6 ) ) 
		{
			//Check time shift
			// set back to Z time, based on hour offset to check dates
			dtshifted.addHour(timeShift);

			// truncate the timezone to compare dates
			String formatshifted = 
				dtshifted.toString( DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS );	
			String formatdt = 
				dt.toString( DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS );
			assertEquals( formatshifted, formatdt );

			// get current time zone and check if it is the expected zone
			String zone = dtshifted.getTimeZoneAbbreviation();
			zone.trim();
			assertEquals( zoneExpected, zone );

			// set shifted DateTime back to the zone offset DateTime
			dtshifted.addHour(- (timeShift) );

			//Check the dataValues
			assertEquals( ts1.getDataValue(dt),  
					ts2.getDataValue(dtshifted), 0.0 );
			
		}

	}
	
	
	// Quick Unit test suite
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTest((new TSUtilTest("testShiftTimeSeries")));
		return suite;
	}
	
}

