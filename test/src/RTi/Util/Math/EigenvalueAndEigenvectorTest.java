/*
 * 
 */

package RTi.Util.Math;

import junit.framework.TestCase;

/**
 *
 * @author cen
 */
public class EigenvalueAndEigenvectorTest extends TestCase {
    
    public EigenvalueAndEigenvectorTest(String testName) {
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

    public void testNonStaticJacobi () {
        int n=4;
        double[][] a = new double[n][n];
        a[0][0] = 4; a[0][1] = -30; a[0][2] = 60; a[0][3] = -35;
        a[1][0] = -30; a[1][1] = 300; a[1][2] = -675; a[1][3] = 420;
        a[2][0] = 60; a[2][1] = -675; a[2][2] = 1620; a[2][3] = -1050;
        a[3][0] = -35; a[3][1] = 420; a[3][2] = -1050; a[3][3] = 700;

        EigenvalueAndEigenvector ee = new EigenvalueAndEigenvector( a );
        ee.jacobi();
        if ( ee.getStatus() != EigenvalueAndEigenvector.Status.CONVERGED )
            fail();
        double eigenvalues[] = ee.getEigenvalues();
        assertEquals( 2585.25, eigenvalues[0], .1 );
        assertEquals( 37.10, eigenvalues[1], .1 );
        assertEquals( 1.48, eigenvalues[2], .1 );
        assertEquals( 0.17, eigenvalues[3], .1 );

        double eigenvectors[][] = ee.getEigenvectors();
        assertEquals( 0.029, eigenvectors[0][0], .01 );
        assertEquals( -0.329, eigenvectors[1][0], .01 );
        assertEquals( 0.791, eigenvectors[2][0], .01 );
        assertEquals( -0.515, eigenvectors[3][0], .01 );

    }

    public void testJacobi() {
        int n=4;
        double[][] a = new double[n][n];
        double[] eigenvalues = new double[n];
        double[][] eigenvectors = new double[n][n];

        a[0][0] = 4; a[0][1] = -30; a[0][2] = 60; a[0][3] = -35;
        a[1][0] = -30; a[1][1] = 300; a[1][2] = -675; a[1][3] = 420;
        a[2][0] = 60; a[2][1] = -675; a[2][2] = 1620; a[2][3] = -1050;
        a[3][0] = -35; a[3][1] = 420; a[3][2] = -1050; a[3][3] = 700;

        EigenvalueAndEigenvector.jacobi(a, n, eigenvalues, eigenvectors);

        for ( int i=0; i<n; i++ ) {
            System.out.println("eigenvalue: " + eigenvalues[i]);
            System.out.println("eigenvectors:");
            for ( int j=0; j<n; j++ ) {
                    System.out.println("     " + eigenvectors[j][i]);
            }
            System.out.println("");
        }

        assertEquals( 2585.25, eigenvalues[0], .1 );
        assertEquals( 37.10, eigenvalues[1], .1 );
        assertEquals( 1.48, eigenvalues[2], .1 );
        assertEquals( 0.17, eigenvalues[3], .1 );

        assertEquals( 0.029, eigenvectors[0][0], .01 );
        assertEquals( -0.329, eigenvectors[1][0], .01 );
        assertEquals( 0.791, eigenvectors[2][0], .01 );
        assertEquals( -0.515, eigenvectors[3][0], .01 );
    }

}
