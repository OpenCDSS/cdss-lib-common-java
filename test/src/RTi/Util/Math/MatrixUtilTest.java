/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RTi.Util.Math;

import RTi.Util.Math.MatrixUtil.MatrixInverseComputations;
import java.security.InvalidParameterException;
import junit.framework.TestCase;

/**
 *
 * @author cen
 */
public class MatrixUtilTest extends TestCase {
    
    public MatrixUtilTest(String testName) {
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

    public void testInverse() {
        int size = 2;
        double matrix[][] = new double[size][size];
        double vector[] = new double[size+1];
        matrix[0][0]= 2;
        matrix[0][1]= 3;
        matrix[1][0]= 4;
        matrix[1][1]= 5;

        double deter = 0;
        try {
            deter = MatrixUtil.inverse(MatrixUtil.MatrixInverseComputations.INVERSE_ONLY, matrix, vector);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("results: " + deter );
        // should add a print routine to MatrixUtil?
        for ( int i=0; i<size; i++ ) {
            for ( int j=0; j<size; j++ ) {
                System.out.print(" " + matrix[i][j]);
            }
            System.out.println("");
        }

        assertEquals(-2, deter, 0.001);
        assertEquals(-2.5, matrix[0][0], 0.001);
        assertEquals(1.5, matrix[0][1], 0.001);
        assertEquals(2, matrix[1][0], 0.001);
        assertEquals(-1, matrix[1][1], 0.001);


        double matrix2[][] = new double[size][size+1];
        matrix2[0][0]= 2;
        matrix2[0][1]= 3;
        matrix2[0][2]= 6;
        matrix2[1][0]= 4;
        matrix2[1][1]= 5;
        matrix2[1][2]= 7;
        try {
            deter = MatrixUtil.inverse(MatrixUtil.MatrixInverseComputations.INVERSE_AND_EQUATION_SOLUTIONS, matrix2, vector);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("results: " + deter );
        // should add a print routine to MatrixUtil?
        System.out.println("matrix:");
        for ( int i=0; i<size; i++ ) {
            for ( int j=0; j<size+1; j++ ) {
                System.out.print(" " + matrix2[i][j]);
            }
            System.out.println("");
        }

        System.out.println("vector:");
        for ( int j=0; j<size+1; j++ ) {
                System.out.println(vector[j]);
        }

        /*
        try {
            deter = MatrixUtil.inverse(MatrixUtil.MatrixInverseComputations.INVERSE_AND_EQUATION_SOLUTIONS, matrix, vector);
            fail ();
        } catch (InvalidParameterException ex) {
            assertSame("Matrix size not properly reported.", 
                    "Equation solutions can only be calculated for n by n+1 matrices.", ex.getMessage());
        } catch (Exception ex) {

        }
        try {
            deter = MatrixUtil.inverse(MatrixUtil.MatrixInverseComputations.INVERSE_ONLY, matrix2, vector);
            fail();
        } catch (InvalidParameterException ex) {
            assertSame("Matrix size not properly reported.",
                    "Only n by n matrices can be inversed.", ex.getMessage());
        } catch (Exception ex) {

        }
        */
    }


    public void testTranspose ()
    {
        int nrows = 4;
        int ncols = 5;
        double[][] a = new double[nrows][ncols];

        for ( int i=0; i<nrows; i++ ) {
            for ( int j=0; j<ncols; j++ ) {
                a[i][j] = (i*nrows) + j;
            }
        }

        double[][] b = MatrixUtil.transpose(a);

        System.out.println("Original Matrix:");
        for ( int i=0; i<nrows; i++ ) {
            for ( int j=0; j<ncols; j++ ) {
                System.out.print("  " + a[i][j]);
            }
            System.out.println("");
        }
        System.out.println("Resulting Matrix:");
        for ( int i=0; i<ncols; i++ ) {
            for ( int j=0; j<nrows; j++ ) {
                System.out.print("  " + b[i][j]);
            }
            System.out.println("");
        }

        // randomly chosen elements to compare
        assertEquals ( 0, b[0][0], 0.01);
        assertEquals ( 7, b[3][1], 0.01);
        assertEquals ( 9, b[1][2], 0.01);

    }

    public void testMultiply ()
    {
        int nARows = 3;
        int nACols = 4;
        int nBRows = nACols;
        int nBCols = 2;
        double[][] a = new double[nARows][nACols];
        double[][] b = new double[nBRows][nBCols];

        // fill a
        for ( int i=0; i<nARows; i++ ) {
            for ( int j=0; j<nACols; j++ ) {
                a[i][j] = (i*nARows) + j;
            }
        }

        System.out.println("Original Matrix (A):");
        for ( int i=0; i<nARows; i++ ) {
            for ( int j=0; j<nACols; j++ ) {
                System.out.print("  " + a[i][j]);
            }
            System.out.println("");
        }

        // fill b
        for ( int i=0; i<nBRows; i++ ) {
            for ( int j=0; j<nBCols; j++ ) {
                b[i][j] = (i*nBRows) + j;
            }
        }

        System.out.println("Original Matrix (B):");
        for ( int i=0; i<nBRows; i++ ) {
            for ( int j=0; j<nBCols; j++ ) {
                System.out.print("  " + b[i][j]);
            }
            System.out.println("");
        }

        double[][] product = MatrixUtil.multiply(a, b);

        // print results
        System.out.println("Product:");
        for ( int i=0; i<nARows; i++ ) {
            for ( int j=0; j<nBCols; j++ ) {
                System.out.print("  " + product[i][j]);
            }
            System.out.println("");
        }

        assertEquals ( 56, product[0][0], 0.01);
        assertEquals ( 62, product[0][1], 0.01);
        assertEquals ( 128, product[1][0], 0.01);
        assertEquals ( 146, product[1][1], 0.01);
        assertEquals ( 200, product[2][0], 0.01);
        assertEquals ( 230, product[2][1], 0.01);
    }

    public void testMultiplyVector ()
    {
        int nARows = 3;
        int nACols = 4;
        int nBRows = nACols;
        double[][] a = new double[nARows][nACols];
        double[] b = new double[nBRows];

        // fill a
        for ( int i=0; i<nARows; i++ ) {
            for ( int j=0; j<nACols; j++ ) {
                a[i][j] = (i*nARows) + j;
            }
        }

        System.out.println("Original Matrix:");
        for ( int i=0; i<nARows; i++ ) {
            for ( int j=0; j<nACols; j++ ) {
                System.out.print("  " + a[i][j]);
            }
            System.out.println("");
        }

        // fill b
        for ( int i=0; i<nBRows; i++ ) {
                b[i] = i*nBRows;
        }

        System.out.println("Original Vector:");
        for ( int i=0; i<nBRows; i++ ) {
                System.out.println( b[i]);
        }

        double[] product = MatrixUtil.multiply(a, b);

        // print results
        System.out.println("Product:");
        for ( int i=0; i<nARows; i++ ) {
                System.out.println("  " + product[i]);
        }

        assertEquals ( 56, product[0], 0.01);
        assertEquals ( 128, product[1], 0.01);
        assertEquals ( 200, product[2], 0.01);
    }

    public void testSortArray ()
    {
        int n = 5;
        double[] x = new double[] {7, 8.2, 2.1, 4, 0};
        int[] indx = new int [n];

        MatrixUtil.sortArray(x, indx, n);
        System.out.println("Array: ");
        for ( int i=0; i<n; i++) {
            System.out.print(" " + x[i]);
        }
        System.out.println("");
        System.out.println("Array index: ");
        for ( int i=0; i<n; i++) {
            System.out.print(" " + indx[i]);
        }
        System.out.println("");
        System.out.println("Sorted array: ");
        for ( int i=0; i<n; i++) {
            System.out.print(" " + x[indx[i]]);
        }
        System.out.println("");
    }

    public void testCat (){
        int x = 1;
        double y = 2;
        double[] xt = new double[] {10, 11};
        doit ( x, y, xt );
        System.out.println("x = " + x + ", y = " + y + ", xt = " + xt[0] + " " + xt[1]);
    }
    private void doit ( int x, double y, double[] xt ) {
        x = 3;
        y = 4;
        xt[0] = 84;
        xt[1] = 72;
    }

}
