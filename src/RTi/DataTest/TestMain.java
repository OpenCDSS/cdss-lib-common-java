// ----------------------------------------------------------------------------
// 
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.TS.DayTS;

import RTi.Util.Message.Message;

import RTi.Util.Time.DateTime;

public class TestMain {

public TestMain(String[] args) {
	try {
		////////////////////////////////////////////////////////////////
		// Set up the time series with the sample data
		DayTS ts = new DayTS();
		DateTime dt1 = DateTime.parse("2005-01-01");
		DateTime dt2 = DateTime.parse("2005-12-31");
		ts.setDate1(dt1);
		ts.setDate2(dt2);
		ts.allocateDataSpace();
		DateTime dt = DateTime.parse("2005-01-01");
		double value = 0;
		double inc = 1;
		for (int i = 0; i < 365; i++) {
			if (value >= 100) {
				inc *= -1;
				inc--;
			}
			else if (value <= 0) {
				inc *= -1;
				inc++;
			}
			ts.setDataValue(dt, value);
			value += inc;
//			System.out.println("Val: " + value);
			dt.addDay(1);
		}
	
		////////////////////////////////////////////////////////////////
		// Set up the dummy TSSupplier
		TestTSSupplier supplier = new TestTSSupplier();
		TestTSSupplier.setTS(ts);			

//		args[0] = "GetTSValue([09304115.USGS.Streamflow.Month~HydroBase]) - GetTSValue([GUNREDCO.DWR.Streamflow.Month~HydroBase]) < 5000 && GetTSValue([09304115.USGS.Streamflow.Month~HydroBase]) - GetTSValue([GUNREDCO.DWR.Streamflow.Month~HydroBase]) > 0";
		args[0] = "GetTSValue([*]) - GetTSValue([*.*.*.*]) < 5000";

		System.out.println(">" + args[0] + "<");
	
		DataTest test = DataTestFactory.buildDataTest(args[0],supplier);
/*	
		Vector v = test.expressionTreeToVector();
		for (int i = 0; i < v.size(); i++) {
			System.out.println("--\n\n" + v.elementAt(i));
		}

		if (1 == 1) {
			System.exit(1);
		}
*/		
		test.setTestData();
		if (!test.checkTimeSeriesForSameIntervals()) {
			throw new Exception("Not all the time series in "
				+ "the DataTest have the same interval base "
				+ "and multiplier.");
		}

//		DataTest test2 = new DataTest(test);

		////////////////////////////////////////////////////////////////
		// Run the test over a year.
		dt = DateTime.parse("2005-01-01");
		for (int i = 0; i < 350; i++) {
			if (test.isInActiveSeason(dt)) {
				test.run(dt);
				System.out.println("" 
					+ test.getDataTestExpression()
					.toString(dt));
				System.out.println("?: " 
					+ test.getDataTestExpression()
					.evaluate(dt));
			}
			dt.addDay(1);
		}
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}

public static void main(String[] args) {
	Message.setDebugLevel(Message.TERM_OUTPUT, 1);
	Message.setWarningLevel(Message.TERM_OUTPUT, 1);
	Message.setDebugLevel(Message.LOG_OUTPUT, 10);
	Message.setWarningLevel(Message.LOG_OUTPUT, 10);

	String logFile = "src\\RTi\\DataTest\\log.log";
	String routine = "doLogFile";

	java.io.PrintWriter ofp;
	try {   
		ofp = Message.openLogFile(logFile);
		Message.setOutputFile(Message.LOG_OUTPUT, ofp);
		Message.printStatus(1, routine, "Using logfile: '" + logFile 
			+ "'");
	}
	catch (Exception e) {
		Message.printWarning(2, routine, 
		"Unable to open log file \"" + logFile + "\"");
	}

	new TestMain(args);
}

}
