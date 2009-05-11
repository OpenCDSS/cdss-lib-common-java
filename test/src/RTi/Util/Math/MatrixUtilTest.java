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
            deter = MatrixUtil.Inverse(MatrixUtil.MatrixInverseComputations.INVERSE_ONLY, matrix, vector);
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
            deter = MatrixUtil.Inverse(MatrixUtil.MatrixInverseComputations.INVERSE_AND_EQUATION_SOLUTIONS, matrix2, vector);
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

        
        try {
            deter = MatrixUtil.Inverse(MatrixUtil.MatrixInverseComputations.INVERSE_AND_EQUATION_SOLUTIONS, matrix, vector);
            fail ();
        } catch (InvalidParameterException ex) {
            assertSame("Matrix size not properly reported.", 
                    "Equation solutions can only be calculated for n by n+1 matrices.", ex.getMessage());
        } catch (Exception ex) {

        }
        try {
            deter = MatrixUtil.Inverse(MatrixUtil.MatrixInverseComputations.INVERSE_ONLY, matrix2, vector);
            fail();
        } catch (InvalidParameterException ex) {
            assertSame("Matrix size not properly reported.",
                    "Only n by n matrices can be inversed.", ex.getMessage());
        } catch (Exception ex) {

        }
        
    }

}
