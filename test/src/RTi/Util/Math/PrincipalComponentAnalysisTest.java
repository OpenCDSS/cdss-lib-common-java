/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RTi.Util.Math;

import junit.framework.TestCase;

/**
 *
 * @author cen
 */
public class PrincipalComponentAnalysisTest extends TestCase {
    
    public PrincipalComponentAnalysisTest(String testName) {
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

    /**
     * Test of pccalc method, of class PrincipalComponentAnalysis.
     */
    public void testPrincipalComponentAnalysis() throws Exception {

        double[] yArray = new double [11];
        double[][] xMatrix = new double[11][7];

        yArray[0] = 15.95;
        yArray[1] = 16.78;
        yArray[2] = 26.39;
        yArray[3] = 19.07;
        yArray[4] = 19.10;
        yArray[5] = 13.10;
        yArray[6] = 17.05;
        yArray[7] = 20.23;
        yArray[8] = 37.02;
        yArray[9] = 19.24;
        yArray[10] = 15.04;

        xMatrix[0][0] = 6.11; xMatrix[0][1] = 9.44; xMatrix[0][2] = 18.43; xMatrix[0][3] = 9.02; xMatrix[0][4] = 13.15; xMatrix[0][5] = 32.87; xMatrix[0][6] = 5.32;
        xMatrix[1][0] = 5.83; xMatrix[1][1] = 4.59; xMatrix[1][2] = 10.55; xMatrix[1][3] = 4.61; xMatrix[1][4] = 13.82; xMatrix[1][5] = 13.75; xMatrix[1][6] = 2.72;
        xMatrix[2][0] = 2.18; xMatrix[2][1] = 10.28; xMatrix[2][2] = 30.37; xMatrix[2][3] = 15.39; xMatrix[2][4] = 25.91; xMatrix[2][5] = 23.74; xMatrix[2][6] = 4.09;
        xMatrix[3][0] = 10.65; xMatrix[3][1] = 8.53; xMatrix[3][2] = 18.67; xMatrix[3][3] = 6.06; xMatrix[3][4] = 25.16; xMatrix[3][5] = 26.65; xMatrix[3][6] = 4.47;
        xMatrix[4][0] = 10.90; xMatrix[4][1] = 7.25; xMatrix[4][2] = 21.46; xMatrix[4][3] = 11.26; xMatrix[4][4] = 21.18; xMatrix[4][5] = 18.82; xMatrix[4][6] = 3.83;
        xMatrix[5][0] = 8.20; xMatrix[5][1] = 5.30; xMatrix[5][2] = 13.41; xMatrix[5][3] = 5.67; xMatrix[5][4] = 16.10; xMatrix[5][5] = 15.47; xMatrix[5][6] = 2.81;
        xMatrix[6][0] = 12.84; xMatrix[6][1] = 6.58; xMatrix[6][2] = 15.59; xMatrix[6][3] = 7.24; xMatrix[6][4] = 19.96; xMatrix[6][5] = 19.02; xMatrix[6][6] = 2.91;
        xMatrix[7][0] = 17.52; xMatrix[7][1] = 6.14; xMatrix[7][2] = 23.15; xMatrix[7][3] = 10.24; xMatrix[7][4] = 29.29; xMatrix[7][5] = 26.10; xMatrix[7][6] = 3.90;
        xMatrix[8][0] = 29.41; xMatrix[8][1] = 11.77; xMatrix[8][2] = 35.51; xMatrix[8][3] = 13.27; xMatrix[8][4] = 48.35; xMatrix[8][5] = 41.93; xMatrix[8][6] = 8.46;
        xMatrix[9][0] = 13.62; xMatrix[9][1] = 7.87; xMatrix[9][2] = 23.55; xMatrix[9][3] = 12.44; xMatrix[9][4] = 24.21; xMatrix[9][5] = 23.15; xMatrix[9][6] = 5.12;
        xMatrix[10][0] = 8.54; xMatrix[10][1] = 4.72; xMatrix[10][2] = 13.66; xMatrix[10][3] = 7.01; xMatrix[10][4] = 14.29; xMatrix[10][5] = 13.78; xMatrix[10][6] = 2.76;
        
        PrincipalComponentAnalysis pca = MathUtil.performPrincipalComponentAnalysis(yArray, xMatrix,
            -999, -999, 20 );

        // need to add check for expected file and actual results.
    }

   

}
