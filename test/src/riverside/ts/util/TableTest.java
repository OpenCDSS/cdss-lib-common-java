
package riverside.ts.util;

import junit.framework.TestCase;
import java.util.Arrays;
import riverside.ts.util.Table;

/**
 *
 * @author iws
 */
public class TableTest extends TestCase {

    public TableTest(String testName) {
        super(testName);
    }

    public void testGetNRows() {
        Table t = new Table();
        assertEquals(0,t.getNRows());
        t.allocateDataSpace(10);
        assertEquals(10,t.getNRows());
    }

    public void testSet() {
        Table t = new Table(1);
        t.set(0,123,456);
        assertEquals(123d,t.lookup(0,0));
        assertEquals(456d,t.lookup(0,1));
    }

    public void testGetMin() {
        Table t = Table.create(
            100,200,
            200,100
            );
        assertEquals(100d,t.getMin(0));
        assertEquals(100d,t.getMin(1));
    }

    public void testGetMax() {
        Table t = Table.create(
            100,200,
            200,100
            );
        assertEquals(200d,t.getMax(0));
        assertEquals(200d,t.getMax(1));
    }

    public void testLookupNoInterpolation() {
        Table t = Table.create(
            1,2,
            3,4,
            5,6
            );
        assertEquals(2d,t.lookup(1,1,false,null));
        assertEquals(4d,t.lookup(3,1,false,null));
        assertEquals(6d,t.lookup(5,1,false,null));
    }
    public void testLookupLinearInterpolation() {
        Table t = Table.create(
            1,2,
            3,4,
            5,6
            );
        assertEquals(2d,t.lookup(1,1,false,Table.InterpolationMode.LINEAR));
        assertEquals(3d,t.lookup(2,1,false,Table.InterpolationMode.LINEAR));
        assertEquals(5d,t.lookup(4,1,false,Table.InterpolationMode.LINEAR));
    }
    public void testLookupLogInterpolation() {
        Table t = Table.create(
            1,2,
            3,4,
            5,6
            );
        assertEquals(2d,t.lookup(1,1,false,Table.InterpolationMode.LOGARITHMIC));
        assertEquals(2.378414230005442d,t.lookup(1.5,1,false,Table.InterpolationMode.LOGARITHMIC));
        assertEquals(2.82842712474619,t.lookup(2,1,false,Table.InterpolationMode.LOGARITHMIC));
        assertEquals(3.363585661014858d,t.lookup(2.5,1,false,Table.InterpolationMode.LOGARITHMIC));
        assertEquals(4d,t.lookup(3,1,false,Table.InterpolationMode.LOGARITHMIC));
    }

    public void testSort() {
        Table t = Table.create(
            2,6,
            3,2,
            1,4
            );
        double[] a123 = new double[] {1,2,3};
        double[] a312 = new double[] {3,1,2};
        double[] a462 = new double[] {4,6,2};
        double[] a246 = new double[] {2,4,6};
        t.sort(0);
        assertTrue(Arrays.equals(a123,t.getColumn(0)));
        assertTrue(Arrays.equals(a462,t.getColumn(1)));
        t.sort(1);
        assertTrue(Arrays.equals(a246,t.getColumn(1)));
        assertTrue(Arrays.equals(a312,t.getColumn(0)));
    }

    public void testGetSmallerIndex() {
        Table t = Table.create(
            1,1,
            2,2,
            3,3
            );
        assertEquals(0,t.getSmallerIndex(0,0));
        assertEquals(0,t.getSmallerIndex(1.5,0));
        assertEquals(2,t.getSmallerIndex(3.5,0));
    }

    public void testGetLargerIndex() {
        Table t = Table.create(
            1,1,
            2,2,
            3,3
            );
        assertEquals(0,t.getLargerIndex(0,0));
        assertEquals(1,t.getLargerIndex(1.5,0));
        assertEquals(2,t.getLargerIndex(3.5,0));
    }

}
