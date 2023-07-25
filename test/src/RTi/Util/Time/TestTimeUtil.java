package RTi.Util.Time;

import RTi.Util.Message.Message;
import junit.framework.TestCase;

/**
 * Test cases for the TimeUtil class methods:
 * - all methods below are alphabetized by method name
 */
public class TestTimeUtil extends TestCase {
    
    /**
     * Test 'dayOfYear' method for calendar year type in first month of year.
     */
    public void testDayOfYear_Calendar1() {
        int days = 0;
        DateTime date = null;
        try {
            date = DateTime.parse("2000-01-15");
            days = TimeUtil.dayOfYear(date,YearType.CALENDAR);
        }
        catch ( Exception e ) {
            fail ( e.toString() );
        }
        assertEquals ( days, 15 );
    }
    
    /**
     * Test 'dayOfYear' method for calendar year type in second month of year.
     */
    public void testDayOfYear_Calendar2() {
        int days = 0;
        DateTime date = null;
        try {
            date = DateTime.parse("2000-02-15");
            days = TimeUtil.dayOfYear(date,YearType.CALENDAR);
        }
        catch ( Exception e ) {
            fail ( e.toString() );
        }
        assertEquals ( days, 46 );
    }
    
    /**
     * Test 'dayOfYear' method for water year type in first month of year.
     */
    public void testDayOfYear_Water1() {
        int days = 0;
        DateTime date = null;
        try {
            date = DateTime.parse("2000-10-15");
            days = TimeUtil.dayOfYear(date,YearType.WATER);
        }
        catch ( Exception e ) {
            fail ( e.toString() );
        }
        assertEquals ( days, 15 );
    }
    
    /**
     * Test 'dayOfYear' method for water year type in third month of year.
     */
    public void testDayOfYear_Water3() {
        int days = 0;
        DateTime date = null;
        try {
            date = DateTime.parse("2000-12-15");
            days = TimeUtil.dayOfYear(date,YearType.WATER);
        }
        catch ( Exception e ) {
            fail ( e.toString() );
        }
        // 31 + 30 + 15
        assertEquals ( days, 76 );
   }
    
    /**
     * Test 'dayOfYear' method for water year type in fourth month of year (first calendar month).
     */
    public void testDayOfYear_Water4() {
        int days = 0;
        DateTime date = null;
        try {
            date = DateTime.parse("2001-01-15");
            days = TimeUtil.dayOfYear(date,YearType.WATER);
        }
        catch ( Exception e ) {
            fail ( e.toString() );
        }
        // 31 + 30 + 31 + 15
        assertEquals ( days, 107 );
    }
    
    /**
     * Test 'dayOfYear' method for water year type in 12 month of year.
     */
    public void testDayOfYear_Water12() {
        int days = 0;
        DateTime date = null;
        try {
            date = DateTime.parse("2001-09-15");
            days = TimeUtil.dayOfYear(date,YearType.WATER);
        }
        catch ( Exception e ) {
            fail ( e.toString() );
        }
        // 31 + 30 + 31 (Oct-Dec) + 31 + 28 + 31 + 30 + 31 + 60 + 61 + 61 + 15 (Jan - Sep 15)
        assertEquals ( days, 350 );
    }
    
   /**
    * Test 'monthOfYear' method for calendar year type.
    */
   public void testMonthOfYear_Calendar11() {
       int months = 0;
       DateTime date = null;
       try {
           date = DateTime.parse("2000-11");
           months = TimeUtil.monthOfYear(date,YearType.CALENDAR);
       }
       catch ( Exception e ) {
           fail ( e.toString() );
       }
       assertEquals ( months, 11 );
   }
   
   /**
    * Test 'monthOfYear' method for water year type, where the calendar month is at the start of the water year.
    */
   public void testMonthOfYear_WaterYear10() {
       int months = 0;
       DateTime date = null;
       try {
           date = DateTime.parse("2000-10");
           months = TimeUtil.monthOfYear(date,YearType.WATER);
       }
       catch ( Exception e ) {
           fail ( e.toString() );
       }
       assertEquals ( months, 1 );
   }
   
   /**
    * Test 'monthOfYear' for water year type, where the calendar month is 12.
    */
   public void testMonthOfYear_WaterYear12() {
       int months = 0;
       DateTime date = null;
       try {
           date = DateTime.parse("2000-12");
           months = TimeUtil.monthOfYear(date,YearType.WATER);
       }
       catch ( Exception e ) {
           fail ( e.toString() );
       }
       assertEquals ( months, 3 );
   }
   
   /**
    * Test 'monthOfYear' method for water year type, where the calendar month is 1.
    */
   public void testMonthOfYear_WaterYear11() {
       int months = 0;
       DateTime date = null;
       try {
           date = DateTime.parse("2001-01");
           months = TimeUtil.monthOfYear(date,YearType.WATER);
       }
       catch ( Exception e ) {
           fail ( e.toString() );
       }
       assertEquals ( months, 4 );
   }
   
   /**
    * Test 'monthOfYear' method for water year type, where the calendar month is 9.
    */
   public void testMonthOfYear_WaterYear9() {
       int months = 0;
       DateTime date = null;
       try {
           date = DateTime.parse("2001-09");
           months = TimeUtil.monthOfYear(date,YearType.WATER);
       }
       catch ( Exception e ) {
           fail ( e.toString() );
       }
       assertEquals ( months, 12 );
   }
   
   /**
    * Test 'monthOfYear' method for null data - should generate an exception.
    */
   public void testMonthOfYear_Exception() {
       try {
           TimeUtil.monthOfYear( null, null );
           fail ( "Exception on null data - behavior expected." );
       }
       catch ( Exception e ) {
           // Should fail here, which means that the test passed.
       }
   }

   /**
    * Test 'shiftTimeZone' for setting to blank'.
    */
   public void testShiftTimeZone_Blank() {
	   DateTime dt = null;
	   String routine = getClass().getSimpleName() + ".testShiftTimeZone_Blank";
       try {
    	   dt = DateTime.parse("2023-01-01 12:00:00 GMT");
    	   System.out.println( routine + ": DateTime before shift: " + dt);
    	   dt.shiftTimeZone("");
    	   //Message.printStatus(2, routine, "DateTime after shift: " + dt);
    	   System.out.println( routine + ": DateTime after shift: " + dt);
       }
       catch ( Exception e ) {
           fail ( e.toString() );
       }
       assertEquals ( dt.toString(), "2023-01-01 12:00:00" );
   }

   /**
    * Test 'shiftTimeZone' for 'GMT' to 'America/Denver'.
    */
   public void testShiftTimeZone_GMT_To_Denver() {
	   DateTime dt = null;
	   String routine = getClass().getSimpleName() + ".testShiftTimeZone_GMT_To_Denver";
       try {
    	   dt = DateTime.parse("2023-01-01 12:00:00 GMT");
    	   System.out.println( routine + ": DateTime before shift: " + dt);
    	   dt.shiftTimeZone("America/Denver");
    	   //Message.printStatus(2, routine, "DateTime after shift: " + dt);
    	   System.out.println( routine + ": DateTime after shift: " + dt);
       }
       catch ( Exception e ) {
           fail ( e.toString() );
       }
       assertEquals ( dt.toString(), "2023-01-01 05:00:00 America/Denver" );
   }
}