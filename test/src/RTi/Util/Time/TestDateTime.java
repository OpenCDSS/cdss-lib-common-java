package RTi.Util.Time;

import junit.framework.TestCase;

/**
 * Test cases for the DateTime class.
 */
public class TestDateTime extends TestCase {
    
   /**
    * Test parsing MM/DD/YYYY HH:MM with month, day, hour, and minute that are unpadded.
    */
   public void testParseFormat_FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm_Unpadded() {
       String dtStringUnpadded = "1/2/2008 3:4"; // String to parse, which is unpadded
       String dtStringPadded = "01/02/2008 03:04";  // String for comparison after formatting
       DateTime dt = null;
       try {
           dt = DateTime.parse( dtStringUnpadded, DateTime.FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm );
       }
       catch ( Exception e ) {
           fail ( e.toString() );
       }
       assertEquals ( dt.toString(DateTime.FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm), dtStringPadded );
   }
   
   /**
    * Test parsing MM/DD/YYYY HH:MM with month, day, hour, and minute that are padded.
    */
   public void testParseFormat_FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm() {
       String dtStringPadded = "01/02/2008 03:04";  // String for comparison after formatting
       DateTime dt = null;
       try {
           dt = DateTime.parse( dtStringPadded, DateTime.FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm );
       }
       catch ( Exception e ) {
           fail ( e.toString() );
       }
       assertEquals ( dt.toString(DateTime.FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm), dtStringPadded );
   }
   
   /**
    * Test parsing MM/DD/YYYY HH:MM bad data - should generate an exception.
    */
   public void testParseFormat_FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm_BadData() {
       String dtStringPadded = "011/022/2008 03:04";  // String for comparison after formatting
       try {
           DateTime.parse( dtStringPadded, DateTime.FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm );
           fail ( "Exception on bad data \"" + dtStringPadded + "\" not caught" );
       }
       catch ( Exception e ) {
           // Should fail here, which means that the test passed
       }
   }

}
