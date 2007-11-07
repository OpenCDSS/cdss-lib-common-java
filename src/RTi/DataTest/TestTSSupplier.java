package RTi.DataTest;

import java.util.Vector;

import RTi.TS.TSSupplier;

import RTi.TS.TS;
import RTi.TS.TSIdent;

import RTi.Util.Time.DateTime;

public class TestTSSupplier 
implements TSSupplier {

private static TS __ts = null;

public static void setTS(TS ts) {
	__ts = ts;
}

public String getTSSupplierName() {
	return "TestTSSupplier";
}

public TS readTimeSeries(String tsident_string, DateTime date1, DateTime date2,
String req_units, boolean read_data)
throws Exception {
	return __ts;
}

public TS readTimeSeries(TS req_ts, String fname, DateTime date1, 
DateTime date2, String req_units, boolean read_data)
throws Exception {
	return __ts;
}

public Vector readTimeSeriesList(String fname, DateTime date1, DateTime date2,
String req_units, boolean read_data)
throws Exception {
	return null;
}

public Vector readTimeSeriesList(TSIdent tsident, String fname,
DateTime date1, DateTime date2, String req_units, boolean read_data)
throws Exception {
	return null;
}

}
