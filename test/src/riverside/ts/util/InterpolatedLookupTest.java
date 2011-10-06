package riverside.ts.util;

import java.util.HashMap;
import junit.framework.TestCase;
import riverside.ts.util.InterpolatedLookup.InterpolationMode;

/**
 *
 * @author rla
 */
public class InterpolatedLookupTest extends TestCase {
    
    public InterpolatedLookupTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private void populateBasis(InterpolatedLookup lookup) {
        HashMap list = new HashMap();
        list.put(1.0, 1.0);
        list.put(2.0, 1.5);
        list.put(3.0, 2.0);
        lookup.setBasis(list);
    }

    /**
     * Test of isEmpty method, of class InterpolatedLookup.
     */
    public void testIsEmpty() {
        System.out.println("isEmpty");
        InterpolatedLookup instance = new InterpolatedLookup();
        boolean expResult = true;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
        
        populateBasis(instance);
        expResult = false;
        assertEquals(expResult, instance.isEmpty());
    }

    /**
     * Test of setInterpolationMode method, of class InterpolatedLookup.
     */
    public void testSetInterpolationMode() {
        System.out.println("setInterpolationMode");
        InterpolatedLookup instance = new InterpolatedLookup();
        instance.setInterpolationMode(InterpolatedLookup.InterpolationMode.LINEAR);
        populateBasis(instance);
        double key = 1.2;
        double interpedLinearValue = instance.lookupValue(key);
        if (interpedLinearValue <= 0 || interpedLinearValue > 2) {
            fail("The LINEAR interpolation returned a value out of range. " + interpedLinearValue);
        }
        instance.setInterpolationMode(InterpolatedLookup.InterpolationMode.LOGARITHMIC);
        double interpedLogValue = instance.lookupValue(key);
        
        if (interpedLinearValue == interpedLogValue) {
            fail("The LINEAR interpolated value unexpectedly matched the LOGARITHMIC interpolated value.");
        }
    }

    /**
     * Test of setBasis method, of class InterpolatedLookup.
     */
    public void testSetBasis() {
        System.out.println("setBasis");
        HashMap list = null;
        InterpolatedLookup instance = new InterpolatedLookup();
        try {
            instance.setBasis(list);
            fail("Setting a null list failed to throw an exception. ");
        }
        catch (Exception e) {
            // good
        }
        try {
            populateBasis(instance);
        }
        catch (Exception e) {
            fail("Setting the basis threw an unexpected exception. "+e.getMessage());
        }
    }

    /**
     * Test of lookupValue method, of class InterpolatedLookup.
     */
    public void testLookupValue() {
        System.out.println("lookupValue");
        double key = 1.0;
        InterpolatedLookup instance = new InterpolatedLookup();
        populateBasis(instance);
        double expResult = 1.0;
        double result = instance.lookupValue(key);
        assertEquals(expResult, result, 1.0);
        
        expResult = 1.5;
        key = 2.0;
        result = instance.lookupValue(key);
        assertEquals(expResult, result, 1.5);
        
        // try an interpolated value
        key = 1.75;
        expResult = 1.75;
        result = instance.lookupValue(key);
        assertEquals(expResult, result, 1.75);
    }

    /**
     * Test of lookupKey method, of class InterpolatedLookup.
     */
    public void testLookupKey() {
        System.out.println("lookupKey");
        double value = 1.0;
        InterpolatedLookup instance = new InterpolatedLookup();
        populateBasis(instance);
        
        double expResult = 1.0;
        double result = instance.lookupKey(value);
        assertEquals(expResult, result, 1.0);
        
        expResult = 3.0;
        result = instance.lookupKey(2.0);
        assertEquals(expResult, result, 3.0);
        
        // try an interpolated value
        expResult = 2.5;
        result = instance.lookupKey(1.75);
        assertEquals(expResult, result, 2.5);
        
    }
}
