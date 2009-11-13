package RTi.Util.Time;

import junit.framework.TestCase;

/**
 * Test cases for the TimeUtil class.
 */
public class TestTimeUtil extends TestCase {
    
    /**
     * Test for calendar year type in first month of year.
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
     * Test for calendar year type in second month of year.
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
     * Test for water year type in first month of year.
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
     * Test for water year type in third month of year.
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
     * Test for water year type in fourth month of year (first calendar month).
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
     * Test for water year type in 12 month of year.
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
    * Test for calendar year type.
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
    * Test for water year type, where the calendar month is at the start of the water year.
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
    * Test for water year type, where the calendar month is 12.
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
    * Test for water year type, where the calendar month is 1.
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
    * Test for water year type, where the calendar month is 9.
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
    * Test for null data - should generate an exception.
    */
   public void testMonthOfYear_Exception() {
       try {
           TimeUtil.monthOfYear( null, null );
           fail ( "Exception on null data - behavior expected." );
       }
       catch ( Exception e ) {
           // Should fail here, which means that the test passed
       }
   }

}