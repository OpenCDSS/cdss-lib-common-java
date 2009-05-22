/**
 *    This program computes multiple regression equations for all combinations
 *    of input variables selected.  If principal components regression is
 *    selected, only those components passing a t-test at a user-specified
 *    level of confidence will be retained.
 */

package RTi.Util.Math;

import java.io.PrintStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cen
 */
public class PrincipalComponentAnalysis {
    
double[] _dependentArray;
double[][] _independentMatrix;
double _independentMissingValue;
double _dependentMissingValue;

// for now, let's use these as defaults, but some may eventually be user driven
int _maxCombinations;
double _tcrit = 1.2;


double[]b;              // regression coefficients (elements in column 0 are intercepts)
double[][] bcomb;       // regression coefficients for combinations
double[] bs;            // regression coeffs. for standardized data
double[][] comp;        // computed flows from full regression for combinations
double[] corr;          // vector of signs of correlations of independent var. with dependent vars.
int[] cuse;             // vector indicating variables used for current combination
double[] err;           // vector of forecast errors
double[][] error;       // matrix of forecast errors for combs.
double[][] evct;        // eigenvector matrix
int lastc;              // last principal component to be considered
int[] ncomp;            // number of components retained for combs.
int[][] obs;            // matrix indicating observations used for stored combinations
int nobs;               // number of observations in input file
int[] nobsc;            // vector of number of observations used in each combination stored
int[] ouse;             // vector indicating observations used for current combination
int nx;                 // number of x variables used
double[] r;             // correlation coefficients for combinations
double[][] rp;          // correlation coefficients for prin. comp.
double[] se;            // standard deviations for combinations
//double[] t;             // vector of t statistics for PCAreg. coeffs.
double[][] x;           // independent variable data matrix containing only data included in computations
double[] xm;            // vector of means of x data
double[] xs;            // vector of standard deviations of x data
double[][] xstd;        // standardized x data
double[] y;             // dependent variable data vector containing only data included in computations
double[] yest;          // y-estimates from PCAreg_coef function

public PrincipalComponentAnalysis ( double[] dependentArray, double[][] independentMatrix,
        double independentMissingValue, double dependentMissingValue, int maxCombinations )
{
    initialize ( dependentArray, independentMatrix, independentMissingValue, dependentMissingValue, maxCombinations );
    try {
        analyze();
    } catch (Exception ex) {
        Logger.getLogger(PrincipalComponentAnalysis.class.getName()).log(Level.SEVERE, null, ex);
    }
}

   

private void analyze( ) throws Exception {
    /* Allocate array space for principal components calculations
     * Most of these will be moved to their appropriate subroutine.
     * Just defining here to get started.
     */

    int nn;
    int icomb = -1;
    int[][] csave = new int[_maxCombinations][nx+1];    // matrix of flags indicating variables used in stored combinations
    for (int i = 0; i < _maxCombinations; i++) {
       for (int j = 0; j <= nx; j++) {
          csave[i][j] = 0;
       }
    }

    int[] iuse = new int[nx+1];                     // vector indicating to use that station -
    for (int j = 1; j <= nx; j++) {                 // really should be taken out since we are always using all stations
       iuse[j] = 1;
    }

    int[] indx = new int[_maxCombinations];         // index vector for sorting combinations


    /* Calculate correlations of independent variables with dependent
     * variable -- signs of correlations will be used in PCAregress() for
     * screening equations
     */

    for (int j = 0; j < nx; j++) {
         int n = 0;
         double sumy, sumy2, sumx, sumx2, sumxy;
         sumy = sumy2 = sumx = sumx2 = sumxy = 0.;
         for (int i = 0; i < nobs; i++)
            if (_dependentArray[i] != _dependentMissingValue &&
                _independentMatrix[i][j] != _independentMissingValue) {
               n++;
               sumy += _dependentArray[i];
               sumy2 += _dependentArray[i] * _dependentArray[i];
               sumx += _independentMatrix[i][j];
               sumx2 += _independentMatrix[i][j] * _independentMatrix[i][j];
               sumxy += _independentMatrix[i][j] * _dependentArray[i];
            }
         double dum1 = n * sumxy - sumx * sumy;
         double dum2 = n * sumy2 - sumy * sumy;
         double dum3 = n * sumx2 - sumx * sumx;
         corr[j+1] = sign (dum1 / Math.sqrt(dum2 * dum3));
    }

    // Perform regression combinations

     for (int i = 0; i < nobs; i++) {
      y[i] = _dependentArray[i];
      x[i][0] = 1.;
   }
   for (int n = 1; n <= nx; n++) {

      /* Form n-variable models and perform regressions */

      if (n == 1) {      /* 1-variable models */

         for (int j = 1; j <= nx; j++)
            if (iuse[j] == 1) {
               cuse[j] = 1;
               icomb = PCAreg(icomb, n );
               cuse[j] = 0;
            }
      }
      else {            /* All but 1-variable models */

         nn = Math.min((icomb+1), _maxCombinations);

         /* Form variable combinations from those already stored */

         for (int i = 0; i < nn; i++) {

            /* Skip stored combinations that have fewer than n-1 variables */

            int cadd = 0;
            for (int j = 1; j <= nx; j++)
               cadd += csave[i][j];
            /* Debug
            fprintf(fpout, "\n\nCheck for combinations having fewer than n-1 variables:\ni = %d   cadd = %d   n = %d   nn = %d",
                    i, cadd, n, nn);
               End debug */
            if (cadd < (n-1))
               continue;

            /* Set cuse flags for first n-1 variables */

            for (int j = 1; j <= nx; j++) {
               cuse[j] = 0;
            }/*  this is NOT the same as the loop below ... why?
            for (int j = 1; j < n; j++ ) {
               if (csave[i][j] == 1) {
                  cuse[j] = 1;
               }
            }*/
            int w = 1;
            int a =  0;
            while (w < n)
               if (csave[i][++a] == 1) {
                  cuse[a] = 1;
                  w++;
               }

            /* Cycle through all variables for n-th variable */

            for (int j = 1; j <= nx; j++) {

               /* If j-th variable is already in equation, skip it --
                  otherwise set it as the n-th variable in equation */

               if (iuse[j] != 1 || cuse[j] == 1)
                  continue;
               cuse[j] = 1;
               if (i == 0) {

                  /* For first stored combination, go ahead and perform
                     regression */
                  /* Debug */
                   System.out.println("cuse before PCAreg: ");
                  for ( int q=1; q<=nx; q++ )
                        System.out.print("  " + cuse[q]);
                   System.out.println("");
                  /* End debug */
                  icomb = PCAreg(icomb, n);
                  cuse[j] = 0;
               }
               else {

                  /* Check previous stored combinations for duplicates */
                  int m;
                  for ( m = 0; m < i; m++) {
                     cadd = 0;
                     for (int l = 1; l <= nx; l++)
                        cadd += ((csave[m][l] + cuse[l]) / 2);
                    /* Debug
                    fprintf(fpout, "\n\nCheck for dups:  i = %d   m = %d   cadd = %d,
                            i, m, cadd);
                       End debug */
                     if (cadd >= (n-1)) {
                        cuse[j] = 0;
                        break;
                     }
                  }

                  /* No duplicate found; do regression */

                  if (m == i) {
                    /* Debug
                    fprintf(fpout, "\n\nNo duplicate found ... performing regression ...");
                       End debug */
                      /* Debug */
                   System.out.println("cuse before PCAreg: ");
                  for ( int q=1; q<=nx; q++ )
                        System.out.print("  " + cuse[q]);
                   System.out.println("");
                  /* End debug */
                     icomb = PCAreg(icomb, n );
                     cuse[j] = 0;
                  }
               }
            }
         }
      }

      /* Stopping criterion -- if none of the n-variable models was
         kept in the top MCOMB combinations, quit */

      nn = Math.min((icomb+1), _maxCombinations);
      int m = 0;
      for (int i = 0; i < nn; i++) {
         m = 0;
         for (int j = 1; j <= nx; j++) {
            if (iuse[j] == 1 && bcomb[i][j] != _independentMissingValue)
               m++;
         }
         if (m == n)
            break;
      }
      if (m < n) {
         break;
      }

      /* Set csave flags indicating which variables are used for each
         stored combination */

      for (int i = 0; i < nn; i++) {
         for (int j = 1; j <= nx; j++) {
             csave[i][j] = (iuse[j] == 1 && bcomb[i][j] != _independentMissingValue) ? 1 : 0;
         }
      }
        /* Debug */
        System.out.printf("\n\ncsave flags after %d-variable eqns. have been done:\n", n);
        for (int i = 0; i < nn; i++) {
           System.out.printf( "\n");
           for (int j = 1; j <= nx; j++)
              System.out.printf( "%d  ", csave[i][j]);
        }
        System.out.printf( "\n\n\n");
           /* End debug */
   }
   int ncomb = icomb + 1;
   if (ncomb == 0) {
       // @todo This shouldn't really throw an exception, just end the program...
      throw new Exception ("No valid equations found.  Program terminated ...");
   }

   /* Sort top MCOMB equations based on standard error */

   if (ncomb == 1) {
      indx[0] = 0;
   }
   else {
      int n = Math.min(ncomb, _maxCombinations);
        /* Debug
        fprintf(fpout, "\n\nUnsorted regression results:\n");
        for (i = 0; i < n; i++) {
           fprintf(fpout, "\n%4d%6.3f%10.3f%10.3f", i, r[i], se[i],
                   bcomb[i][0]);
           for (j = PCAny; j < nv; j++)
              if (iuse[j] == 1)
                 fprintf(fpout, "%10.3f", bcomb[i][j]);
        }
           End debug */
      MatrixUtil.sortArray(se, indx, n);
        /* Debug */
        System.out.println("Sorted regression results:");
        for (int i = 0; i < n; i++) {
            System.out.printf("\n%4d%6.3f%10.3f%10.3f",
                    indx[i], r[indx[i]], se[indx[i]], bcomb[indx[i]][0]);
           for (int j = 1; j <= nx; j++)
              if (iuse[j] == 1)
                 System.out.printf( "%10.3f", bcomb[indx[i]][j]);
        }
           /* End debug */


   }

   printOutput(System.out, ncomb, iuse, indx );

    System.out.println("PROGRAM FINISHED.");
}

private void initialize(double[] dependentArray, double[][] independentMatrix,
        double independentMissingValue, double dependentMissingValue, int maxCombinations ) {
    _dependentArray = dependentArray;
    _independentMatrix = independentMatrix;
    _independentMissingValue = independentMissingValue;
    _dependentMissingValue = dependentMissingValue;
    _maxCombinations = maxCombinations;

    nx = _independentMatrix[0].length;      // number of x variables used
    lastc = nx;
    nobs = _independentMatrix.length;      // number of observations in input
    bs = new double[nx+1];         // regression coeffs. for standardized data

    evct = null;   // eigenvector matrix
    ncomp = new int[_maxCombinations]; // number of components retained for combinations
    for (int i = 0; i < _maxCombinations; i++)
       ncomp[i] = 0;
    rp = new double[nx][nx];     // correlation coefficients for prin. comp.
    xm = new double[nx+1];         // vector of means of x data
    xs = new double[nx+1];         // vector of standard deviations of x data
    xstd = new double[nobs][nx+1];       // standardized x data


    // error series output option flag
    comp = new double[_maxCombinations][nobs];  // computed flows from full regression for combinations
    error = new double[_maxCombinations][nobs]; // matrix of forecast errors for combs.
    obs = new int[_maxCombinations][nobs];      // matrix indicating observations used for stored combinations

    b = new double[nx+1];                       // regression coefficients (elements in column 0 are intercepts)
    bcomb = new double[_maxCombinations][nx+1]; // regression coefficients for combinations

    for (int i = 0; i < _maxCombinations; i++) {
       for (int j = 0; j <= nx; j++) {
          bcomb[i][j] = _independentMissingValue;
       }
    }

    corr = new double[nx+1];     // vector of signs of correlations of independent var. with dependent vars.
    cuse = new int[nx+1];        // vector indicating variables used for current combination
    for (int j = 0; j < (nx+1); j++) {
       corr[j] = 0.;
       cuse[j] = 0;
    }

    err = new double[nobs];                // vector of forecast errors
    nobsc = new int[_maxCombinations];     // vector of number of observations used in each combination stored
    ouse = new int[nobs];                  // vector indicating observations used for current combination
    for (int i = 0; i < nobs; i++)
       ouse[i] = 1;
    r = new double[_maxCombinations];      // correlation coefficients for combinations
    se = new double[_maxCombinations];     // standard deviations for combinations
    x = new double[nobs][nx+1];            //
    y = new double[nobs];
    yest = new double[nobs];               // y-estimates from PCAreg_coef function
}

/**
 *    Set flags, load data arrays, and call regress function
 *    for a given combination of variables.
 *
 * @param int icomb combination counter
 * @param int k number of variables included in equation
 * @return updated icomb
 */
private int PCAreg(int icomb, int k ) throws Exception
{
   icomb++;

   /* Set flags for observations to be used */
   int n = nobs;
   int m = 0;

   for (int i = 0; i < nobs; i++) {
      if (_dependentArray[i] == _dependentMissingValue) {
         ouse[i] = 0;
         n--;
         continue;
      }
      for ( m = 1; m <= nx; m++) {
         if ( cuse[m] == 1 && _independentMatrix[i][m-1] == _independentMissingValue ) {
            ouse[i] = 0;
            n--;
            break;
         }
      }
   }

   // If fewer than six observations, throw out that combination
   if (n < 6) {
      for (int i = 0; i < nobs; i++)
         ouse[i] = 1;
      icomb--;
      return icomb;
   }

   m = -1;
   for (int i = 0; i < nobs; i++) {
      if (ouse[i] == 1) {
         y[++m] = _dependentArray[i];
         n = 0; 
         for (int l = 1; l <= nx; l++)
            if (cuse[l] == 1)
               x[m][++n] = _independentMatrix[i][l-1];
      }
   }

    /* Debug */
    System.out.printf( "\n\nPCAreg:  y and x data for combination %d:\n", icomb);
    for (int i = 0; i <= m; i++) {
       System.out.printf( "\n%10.3f", y[i]);
       for (int l = 0; l <= k; l++)
          System.out.printf( "%10.3f", x[i][l]);
    }
    /* End debug */


   // Perform regression
  icomb = PCAregress(icomb, m+1, k );

  // Reset ouse flags for next combination
   for (int i = 0; i < nobs; i++)
      ouse[i] = 1;

   return icomb;

}

/*
 *    Compute regression coefficients for an input matrix of observations for
 *    independent variables (x) and an input vector of observations for a
 *    dependent variable (y).
 *
 *    Return 0 for normal completion or
 *           1 if matrix inversion failed
 * @param x matrix of independent variable obs.
 * @param n number of observations
 * @param nvar number of variables
 * @param rANDse [0] = correlation coefficient, [1] = standard error
 * @param t vector of t statistics for PCAreg. coeffs.
 * @param y dependent variable data vector containing only data included in computations
 * @param yest y-estimates
 * @param err vector of forecast errors
 */
int PCAreg_coef( int n, int nvar, double[] rANDse, double[] t )
{
   double dum1, dum2, dum3;      /* dummy variables for corr. coeff. calc. */
   double mse;                   /* mean square error */
   double sse = 0.;              /* sum of squared errors */
   double sumf = 0.;             /* sum of forecast data */
   double sumf2 = 0;             /* sum of squared forecast values */
   double sumo = 0.;             /* sum of observed data */
   double sumof = 0.;            /* sum of observed times forecast */
   double sumo2 = 0.;            /* sum of squared observed values*/

   /* Add one to nvar to account for intercept */

   nvar++;
    /* Debug
    fprintf(fpout, "\n\nPCAreg_coef:  y and x input data:\n");
    for (i = 0; i < n; i++) {
       fprintf(fpout, "\n%8.2f", y[i]);
       for (j = 0; j < nvar; j++)
          fprintf(fpout, "%8.2f", x[i][j]);
    }
       End debug */

   // Transpose the x matrix (x')

    /* Debug
    fprintf(fpout, "\n\nPCAreg_coef:  transposing matrix ...\n");
       End debug */
   double [][] xt = MatrixUtil.transpose(x, n, nvar );

   // Compute x'x

    /* Debug
    fprintf(fpout, "\n\nPCAreg_coef:  multiplying matrix ...\n");
       End debug */
   double[][] work1 = MatrixUtil.multiply(xt, x, nvar, n, nvar );
   try {
        /* Invert x'x */
        /* Debug
        fprintf(fpout, "\n\nPCAreg_coef:  inverting matrix ...\n");
        End debug */
        if (MatrixUtil.inverse(work1, nvar) == 0.) {
            return 1;
        }
    } catch (Exception ex) {
        return 1;
    }

   /* Save variances of regression coefficients -- diagonal elements of
      (x'x) inverse */

   for (int i = 0; i < nvar; i++)
      t[i] = work1[i][i];

   // Multiply matrix inverse by x'

    /* Debug
    fprintf(fpout, "\n\nPCAreg_coef:  multiplying matrix ...\n");
       End debug */
   double[][] work2 = MatrixUtil.multiply(work1, xt, nvar, nvar, n);

   // Multiply resultant matrix by y vector to get regression coefficients

    /* Debug
    fprintf(fpout, "\n\nPCAreg_coef:  calculating regression coefficients ...\n");
       End debug */
   double[] btemp = MatrixUtil.multiply(work2, y, nvar, n );
   for ( int i=0; i<btemp.length; i++ ) {
       b[i] = btemp[i];
   }
    /* Debug
    fprintf(fpout, "\n\nRegression coefficients:\n");
    for (i = 0; i < nvar; i++)
       fprintf(fpout, "%8.3f\n", b[i]);
       End debug */

   // Generate equation estimates of y (yest)

    /* Debug
    fprintf(fpout, "\n\nPCAreg_coef:  calculating y estimates ...\n");
       End debug */
   yest = MatrixUtil.multiply(x, b, n, nvar );


   // Compute correlation coefficient and standard error

    /* Debug
    fprintf(fpout, "\n\nPCAreg_coef:  calculating r and se ...\n");
       End debug */

  for (int i = 0; i < n; i++) {
     sumo += y[i];
     sumf += yest[i];
     sumof += y[i] * yest[i];
     sumo2 += y[i] * y[i];
     sumf2 += yest[i] * yest[i];
     err[i] = yest[i] - y[i];
     sse += err[i] * err[i];
  }

   dum1 = n * sumof - sumo * sumf;
   dum2 = n * sumo2 - sumo * sumo;
   dum3 = n * sumf2 - sumf * sumf;
   rANDse[0] = dum1 / Math.sqrt(dum2 * dum3);
   mse = sse / (n - nvar);
   rANDse[1] = Math.sqrt(mse);

   // Compute t statistics for regression coefficients

    /* Debug
    fprintf(fpout, "\n\nPCAreg_coef:  calculating t statistics ...\n");
       End debug */

   for (int i = 0; i < nvar; i++) {
      t[i] *= mse;
      t[i] = b[i] / Math.sqrt(t[i]);
   }

   return(0);
}


/*
 *    Perform regression calculations.
 *
 * @param icomb combination counter
 * @param nobsuse number of obs. used in current comb.
 * @param nvar number of variables
 */
private int PCAregress(int icomb, int nobsuse, int nvar  )
throws Exception
{
   int imax;                     // maximum value for i index
   int ise;                      // index for standard error maximum
   int nc = 0;                       // number of components retained
   int j;
   double[] rANDse = new double[2];           // correlation coefficient and standard error
   double rsave = 0;                              // temporary storage for correlation coeff.
   double semax;                              // maximum standard error stored
   double sesave = 0;                             // temporary storage for standard error
   double[] t = new double[nx+1];          // t statistics
   double[] bsave = new double[nx+1];         // temporary storage for regression coeffs. in
   double[] esave = new double[nobsuse];      // temporary storage for error series
   double[] cmpsave = new double[nobsuse];      // temporary storage for computed flows

   // Principal components regression

   if ( nvar > 1) {

      // Compute principal components

      if (pccalc(nobsuse, nvar ) != 0) {
            throw new Exception ("Error in eigenvalue/eigenvector computation.  Program terminated ... Combination no. " + icomb );
      }

      // Compute regression coefficients

      imax = Math.min(nvar, lastc);
      nc = 0;
      for (int i = 1; i <= imax; i++) {

         // Only perform regression if there are 4 remaining degrees of freedom

         if ((nobsuse - i - 1) < 4) {
            /* Debug
            fprintf(fpout, "\n\nPCAregress: Fewer than 4 remaining d.f. for comb. %d", *icomb);
               End debug */
            break;
         }
         if (PCAreg_coef(nobsuse, i, rANDse, t) != 0) {
            throw new Exception ("Matrix inversion cannot be completed.  Program terminated ...");
         }

         /* Test for significance of last included component. If significant,
            convert regression coefficients from principal components into
            original variables and save results */

        /* Debug
        fprintf(fpout, "\n\nPCAregress: t-values for combination %d are:", *icomb);
        for (j = 1; j <= i; j++)
           fprintf(fpout, "%8.3f", t[j]);
        fprintf(fpout, "\n           Variables:");
        for (j = PCAny; j < nv; j++)
           if (cuse[j] == 1)
              fprintf(fpout, "  %s", v[j]);
           End debug */
         if (Math.abs(t[i]) >= _tcrit) {
            pcconv(i, nvar );

            /* Test for coefficients opposite in sign to the sign of their
               correlation with the dependent variable.  If opposite, do
               not use the last included component. */

            int k = 0;
            for ( j = 1; j <= nx; j++) {
               if (cuse[j] == 1 && sign(b[++k]) != corr[j])
                  break;
            }

            if (j >= (nx+1)) {
               nc = i;

               // Save regression results

               for (j = 0; j <= nvar; j++)
                  bsave[j] = b[j];
                  rsave = rANDse[0];
                  sesave = rANDse[1];
                  for (j = 0; j < nobsuse; j++) {
                     esave[j] = err[j];
                     cmpsave[j] = yest[j];
                  }
            }
        /* Debug
        else {
           fprintf(fpout, "\n\nRegression coefficient of inappropriate sign -- component no. %d", i);
           fprintf(fpout, "\nVariables and coefficients:\n");
           for (j = PCAny; j < nv; j++)
              if (cuse[j] == 1)
                 fprintf(fpout, "       %s", v[j]);
           fprintf(fpout, "\n");
           for (j = 1; j <= nvar; j++)
              fprintf(fpout, "%10.3f", b[j]);
        }
           End debug */
         }
         else
            break;
      }

      // If no components are significant, throw out that combination.

      if (nc == 0) {
         (icomb)--;
         return icomb;
      }
    /* Debug
    else {
       fprintf(fpout, "\n\nNumber of components retained = %d\nVariables:\n", nc);
       for (j = PCAny; j < nv; j++)
          if (cuse[j] == 1)
             fprintf(fpout, "       %s", v[j]);
    }
       End debug */
   }

   // Standard regression */
   else if (PCAreg_coef(nobsuse, nvar, rANDse, t) != 0) {
      throw new Exception ("Matrix inversion cannot be completed.  Program terminated ...");
   }
   else {

      /* If any variables have non-significant t-statistic, throw out
         that combination; otherwise, save regression results */

        /* Debug
        fprintf(fpout, "\n\nPCAregress: t-values for combination %d are:  ", *icomb);
        for (j = 1; j <= nvar; j++)
           fprintf(fpout, "%8.3f", t[j]);
           End debug */
      for ( j = 1; j <= nvar; j++)
         if (Math.abs(t[j]) < _tcrit) {
            icomb--;
            return icomb;
         }
      for (j = 0; j <= nvar; j++)
         bsave[j] = b[j];
      rsave = rANDse[0];
      sesave = rANDse[1];
     for (j = 0; j < nobsuse; j++) {
        esave[j] = err[j];
        cmpsave[j] = yest[j];
     }
   }
    /* Debug */
    System.out.printf( "\n\nPCAregress results for combination %d:", icomb);
    if (nvar > 1)
       System.out.printf("\n   (number of prin. comp. retained = %d)", nc);
    System.out.printf("\n   (number of observations used = %d)", nobsuse);
    System.out.printf("\nrsave: %6.3f sesave: %10.3f", rsave, sesave);
    System.out.printf("\nintercept?: %6.3f", bsave[0]);
    System.out.printf("\nnvar: %d\n", nvar);
    for (int q = 1; q <= nx; q++)
         if (cuse[q] == 1)
             System.out.printf( "\ncuse(%d)", q);
    for (int k = 1; k <= nvar; k++) {
            System.out.printf( "\n(%d) %10.3f", k, bsave[k]);
    }
       /* End debug */

   if (icomb < _maxCombinations) {

      // Save results for first MCOMB combinations */

      bcomb[icomb][0] = bsave[0];
      int k = 0;
      for (j = 1; j <= nx; j++)
         if (cuse[j] == 1)
            bcomb[icomb][j] = bsave[++k];
      r[icomb] = rsave;
      se[icomb] = sesave;
      nobsc[icomb] = nobsuse;
      ncomp[icomb] = nvar > 1 ? nc : 0;
     k = -1;
     for (j = 0; j < nobs; j++) {
        if (ouse[j] == 1) {
           k++;
           error[icomb][j] = esave[k];
           comp[icomb][j] = cmpsave[k];
           obs[icomb][j] = 1;
        }
        else
           obs[icomb][j] = 0;
     }
   }
   else {

      /* After first MCOMB combinations, save results if current combination
         is superior to all those stored */

      // Find regular standard error
      semax = -1.;
      ise = -1;
      for (int i = 0; i < _maxCombinations; i++) {
            if (se[i] > semax) {
               semax = se[i];
               ise = i;
            }
      }
        /* Debug
        fprintf(fpout, "\n\nsemax for combination %d is %f", *icomb, semax);
           End debug */

      /*
         Search for maximum stored regular standard error and compare
         with current combination's standard error.  If the latter is
         larger than the former, throw out that combination.  If it is
         smaller, replace the stored results for the maximum with the
         results for the current combination. */

      if (sesave < semax) {
            // save results if combination is superior to stored ones

            /* Debug
            fprintf(fpout, "\n\nCombination %d replacing stored value no. %d", *icomb, ise);
               End debug */
            bcomb[ise][0] = bsave[0];
            int k = 0;
            for (j = 1; j <= nx; j++) {
                  bcomb[ise][j] = cuse[j] == 1 ? bsave[++k] : _independentMissingValue;
            }
            r[ise] = rsave;
            se[ise] = sesave;
            nobsc[ise] = nobsuse;
            ncomp[ise] = nvar > 1 ? nc : 0;
           k = -1;
           for (j = 0; j < nobs; j++) {
              if (ouse[j] == 1) {
                 k++;
                 error[ise][j] = esave[k];
                 comp[ise][j] = cmpsave[k];
                 obs[ise][j] = 1;
              }
              else
                 obs[ise][j] = 0;
           }
      }
   }
   return icomb;
}


/*
 *    Compute principal components for input to multiple regression.
 *
 *    Return 0 for normal completion or
 *           1 if no convergence in eigenvalue/eigenvector calculation
 *
 * @param nobs number of observations
 * @param nx number of variables
 */
int pccalc(int nobs, int nx )
{
   double dum1, dum2, dum3;      // results of intermediate calculations
   int ip1;                      // i plus 1
   int jp1;                      // j plus 1
   double suma;                  // sum of first data elements
   double sumb;                  // sum of second data elements
   double sumab;                 // sum of products of first and second data elements
   double suma2;                 // sum of squares of first data elements
   double sumb2;                 // sum of squares of second data elements
   double denom;

    /* Debug
    fprintf(fpout, "\n\npccalc:  input x data:\n");
    for (i = 0; i < nobs; i++) {
       fprintf(fpout, "\n");
       for (j = 0; j <= nx; j++)
          fprintf(fpout, "%9.2f", x[i][j]);
    }
   End debug */

   // Compute means, standard deviations, and correlation matrix

   for (int i = 0; i < nx; i++)
      rp[i][i] = 1.;
   for (int i = 0; i < nx; i++) {
      ip1 = i + 1;
      suma = 0.;
      suma2 = 0.;
      for (int k = 0; k < nobs; k++) {
         suma += x[k][ip1];
         suma2 += x[k][ip1] * x[k][ip1];
      }

      // Means and standard deviations

      xm[ip1] = suma / nobs;
      xs[ip1] = Math.sqrt((suma2 - (suma * suma / nobs)) / (nobs - 1));

      // Correlation matrix

      for (int j = i + 1; j < nx; j++) {
         jp1 = j + 1;
         sumb = 0.;
         sumab = 0.;
         sumb2 = 0;
         for (int k = 0; k < nobs; k++) {
            sumb += x[k][jp1];
            sumab += x[k][ip1] * x[k][jp1];
            sumb2 += x[k][jp1] * x[k][jp1];
         }
         dum1 = nobs * sumab - suma * sumb;
         dum2 = nobs * suma2 - suma * suma;
         dum3 = nobs * sumb2 - sumb * sumb;
         denom =  Math.sqrt(dum2 * dum3);
         if ( denom == 0 ) {
             System.out.println("Zero in denominator!!");
             return (1);
         }
         rp[i][j] = dum1 / denom;
         // rp[i][j] = denom == 0 ? 0 : dum1 / Math.sqrt(dum2 * dum3);
         rp[j][i] = rp[i][j];
      }
   }
    /* Debug
    fprintf(fpout, "\n\npccalc:  means and sd's of x data:\n");
    for (i = 1; i <= nx; i++)
       fprintf(fpout, "%10.3f%10.3f\n", xm[i], xs[i]);
       End debug */

   // Compute eigenvalues and eigenvectors
   EigenvalueAndEigenvector ee = new EigenvalueAndEigenvector( rp );
   ee.jacobi(nx);
   if ( ee.getStatus() != EigenvalueAndEigenvector.Status.CONVERGED )
        return(1);
   evct = ee.getEigenvectors();

   // Standardize input data

    /* Debug
    fprintf(fpout, "\n\npccalc:  calculating standardized data:\n");
       End debug */
   for (int j = 1; j <= nx; j++) {
      for (int i = 0; i < nobs; i++) {
        /* Debug
                fprintf(fpout, "x[%d][%d] = %7.2f  xm[%d] = %8.3f  xs[%d] = %8.3f",
                        i, j, x[i][j], j, xm[j], j, xs[j]);
           End debug */
         xstd[i][j] = (x[i][j] - xm[j]) / xs[j];
        /* Debug
                 fprintf(fpout, "  xstd[%d][%d] = %7.3f  adr = %p  adr **xstd = %p\n",
                         i, j, xstd[i][j], &xstd[i][j], xstd);
           End debug */
      }
   }
    /* Debug
    fprintf(fpout, "\n\npccalc:  standardized x data:\n");
    for (i = 0; i < nobs; i++) {
       fprintf(fpout, "\n");
       for (j = 1; j <= nx; j++)
          fprintf(fpout, "%8.3f", xstd[i][j]);
    }
    fprintf(fpout, "\n\npccalc:  addresses of standardized x data:\n");
    for (i = 0; i < nobs; i++) {
       fprintf(fpout, "\n%p", xstd);
       for (j = 1; j <= nx; j++)
          fprintf(fpout, " %p", &xstd[i][j]);
    }
       End debug */

   // Compute new variates (principal components) */

   for (int i = 0; i < nobs; i++) {
      for (int j = 0; j < lastc; j++) {
         jp1 = j + 1;
         x[i][jp1] = 0.;
         for (int k = 0; k < nx; k++)
            x[i][jp1] += evct[k][j] * xstd[i][k+1];
      }
   }
   return(0);
}

/*
 *    Convert principal components regression coefficients into coefficients
 *    in terms of original variables.
 *
 * @param ncomp number of principal components used
 * @param nx number of variables
 */
void pcconv(int ncomp, int nx )
{
   double sum = 0.;              // summation variable
   int i, j;                     // array indexes
   int ip1;                      // i plus 1

   /* Convert regression coefficients for components into
      coefficients for standardized original input variables */

   for (i = 0; i < nx; i++) {
      ip1 = i + 1;
      bs[ip1] = 0.;
      for (j = 0; j < ncomp; j++)
         bs[ip1] += evct[i][j] * b[j+1];
   }
   bs[0] = b[0];

   /* Convert regression coefficients for standardized original input variables
      into coefficients for unstandardized original input variables */

   for (i = 1; i <= nx; i++)
      b[i] = bs[i] / xs[i];
   for (i = 1; i <= nx; i++)
      sum += b[i] * xm[i];
   b[0] = bs[0] - sum;
}


private void printOutput ( PrintStream fpout, int ncomb, int[] iuse, int[] indx )
{
    // Write out results for top NCOMBP equations

   int nlines = (nx - 1) / 12 + 1;
   int nn = Math.min(ncomb, _maxCombinations);
   int ix;

   // Header information

   Date date = new Date();
   fpout.printf("\n%s     %s\n%s%3.1f",
           "REGRESSION COMBINATION PROGRAM", date,
           "Critical value of t-statistic = ", _tcrit);
   fpout.printf("\n%s%d",
              "Maximum number of principal components retained = ", lastc);
   fpout.printf("\nNumber of combinations evaluated = %d", ncomb);

   // Equation summary

   fpout.printf("\n\n\n\n\nEQUATION SUMMARY:\n\n\nRANK   VARIABLES");
   if (nx >= 6)
      for (int i = 6; i <= nx; i++)
         fpout.printf("  ");

   fpout.printf("   STANDARD   NO.\n");
   fpout.printf("                ");
   if (nx >= 6)
      for (int i = 6; i <= nx; i++)
         fpout.printf("  ");
   fpout.printf("      ERROR  OBS.");
   for (int i = 0; i < 3; i++) {
      fpout.printf("\n      ");

      if (i == 0) {
          for ( int j=1; j <= nx; j++ ) {
              if (iuse[j] == 1)
                  fpout.printf(" X");
          }

         if (nx < 5) {
            for (int j = 5; j > nx; j--)
               fpout.printf("  ");
         }
         fpout.printf("             USED");
      } else if ( i==2 ) {
          for ( int j=1; j <= nx; j++ ) {
              if (iuse[j] == 1)
                  fpout.printf(" %d", j);
          }
      }
   }
   fpout.printf("\n");
   for (int k = 0; k < nn; k++) {
         ix = indx[k];
      fpout.printf("\n%4d  ", k+1);
      for (int j = 1; j <= nx; j++) {
         if (iuse[j] == 1) {
            if (bcomb[ix][j] != _independentMissingValue)
               fpout.printf(" X");
            else
               fpout.printf("  ");
         }
      }
      if (nx < 5) {
         for (int j = 5; j > nx; j--)
            fpout.printf("  ");
      }
      fpout.printf("%11.3f%6d", se[ix], nobsc[ix]);
   }

   // List of ranked equations

   fpout.printf("\n\n\n\n\nRANKED REGRESSION EQUATIONS:");
   for (int k = 0; k < nn; k++) {
      ix = indx[k];
      int jend = 0;
      for (int i = 1; i <= nlines; i++) {
         int jbegin = jend + 1;
         if (i == nlines)
            jend = nx;
         if (i == 1)
            fpout.printf("\n\n\nRANK   INTERCEPT");
         else
            fpout.printf("\n\n                ");
         int n = 0;
         for (int j = jbegin; j <= nx; j++)
            if (iuse[j] == 1 ) {
               fpout.printf("      x %s", j);
               n++;
               if (n == 12) {
                  jend = j;
                  break;
               }
            }
         if (i == 1)
            fpout.printf("\n%4d%12.3f", k+1, bcomb[ix][0]);
         else
            fpout.printf("\n                ");
         for (int j = jbegin; j <= jend; j++) {
            if (iuse[j] == 1) {
               if (bcomb[ix][j] != _independentMissingValue)
                  fpout.printf("%9.3f", bcomb[ix][j]);
               else
                  fpout.printf("        0");
            }
         }
      }
      fpout.printf("\n\n%s%d",
              "       Number of observations used = ", nobsc[ix]);
      fpout.printf("\n%s%d",
                 "       Number of principal components used = ", ncomp[ix]);
      fpout.printf("\n\n%s%18.3f\n%s%31.3f",
              "       CORRELATION COEFFICIENT (R) = ", r[ix],
              "       STANDARD ERROR = ", se[ix]);


      // Observed, computed, and error series
         fpout.printf("\n       YEAR   OBSERVED   COMPUTED      ERROR\n");
         for (int i = 0; i < nobs; i++)
            if (obs[ix][i] == 1) {
               fpout.printf("\n%11d%11.2f%11.2f%11.2f", i,
                          y[i], comp[ix][i], error[ix][i]);
            }
   }
   fpout.printf("\n");

}

private int sign ( double i )
{
    return ( i >= 0 ? 1 : -1 );

}

}
