/*
 *
 */

package RTi.Util.Math;

import RTi.Util.Message.Message;
import java.security.InvalidParameterException;

/**
 *
 * @author cen
 */
public class MatrixUtil {

public enum MatrixInverseComputations { 
    INVERSE_ONLY, INVERSE_AND_EQUATION_SOLUTIONS, EQUATION_SOLUTIONS_ONLY }

/*    David Garen  5/89
 *
 *    Calculate matrix inverse in place by Gauss-Jordan method with
 *    maximum pivot strategy.
 *
 *    Converted from FORTRAN program in:  Brice Carnahan, H.A. Luther, and
 *    James O. Wilkes, Applied Numerical Methods, John Wiley, 1969,
 *    pp. 290-291.
 *
 *    Return 0 for singular matrix (no solution), or
 *           value of determinant for normal completion.
 *
 *    When indic is negative, function computes the inverse of the n by n
 *    matrix a in place.  When indic is zero, function computes the n
 *    solutions x[0] ... x[n-1] corresponding to the set of linear equations
 *    with augmented matrix of coefficients in the n by n+1 array a and in
 *    addition computes the inverse of the coefficient matrix in place as
 *    above.  If indic is positive, the set of linear equations is solved, but
 *    the inverse is not computed in place.  The Gauss-Jordan complete
 *    elimination method is employed with the maximum pivot strategy.  Row and
 *    column subscripts of successive pivot elements are saved in order in the
 *    irow and jcol arrays, respectively.  k is the pivot counter, pivot is
 *    the algebraic value of the pivot element, max is the number of columns
 *    in a, and deter is the determinant of the coefficient matrix.  The
 *    solutions are computed in the (n+1)th column of a and then unscrambled
 *    and put in proper order in x[0] ... x[n-1] using the pivot subscript
 *    information available in the irow and jcol arrays.  The sign of the
 *    determinant is adjusted, if necessary, by determining if an odd or even
 *    number of pairwise interchanges is required to put the elements of the
 *    jord array in ascending sequence, where jord[irow[i]] = jcol[i].  If the
 *    inverse is required, it is unscrambled in place using y[0] ... y[n-1] as
 *    temporary storage.  The value of the determinant is returned as the
 *    value of the function.  Should the potential pivot of largest magnitude
 *    be smaller in magnitude than eps, the matrix is considered to be
 *    singular, and zero is returned as the value of the function.
 */

/**
 *
 * @param a matrix to invert
 * @return determinate of n by n matrix
 */
public static double Inverse ( double[][] a )
throws Exception {
    double[] x = new double[0];
    return Inverse ( MatrixUtil.MatrixInverseComputations.INVERSE_ONLY, a, x);
}

/**
 *
 * @param indic Indicates desired computations:  one of MatrixInverseComputations
 *      (INVERSE_ONLY, INVERSE_AND_EQUATION_SOLUTIONS, EQUATION_SOLUTIONS_ONLY)
 * @param a;                     // input matrix
 * @param x;                      // vector of equation solutions
 */
public static double Inverse( MatrixInverseComputations indic, double[][] a, double[] x )
        throws Exception
{
   String routine = "MatrixUtil.Inverse";

   double aijck;                // a[i][jcolk] temporary storage
   double deter = 1.;           // determinant
   double eps = 1.0e-10;        // minimum value for pivot element
   int iscan, jscan;            // for loop indexes
   int ip1;                     // i plus 1
   int[] irow;                  // vector of pivot subscript information
   int irowi;                   // irow[i] temporary storage
   int irowj;                   // irow[j] temporary storage
   int irowk;                   // irow[k] temporary storage
   int intch = 0;               // counter for determinant sign change
   int[] jcol;                  // vector of pivot subscript information
   int jcoli;                   // jcol[i] temporary storage
   int jcolj;                   // jcol[j] temporary storage
   int jcolk;                   // jcol[k] temporary storage
   int[] jord;                  // order vector for solution values
   int jtemp;                   // temporary storage for j index
   int max;                     // number of columns in matrix a
   int nm1;                     // n minus 1
   double pivot;                // pivot element
   int test;                    // while loop test variable
   double[] y;                  // temporary storage vector

   // number of rows and columns in matrix a
   int n = a.length;
   max = indic==MatrixInverseComputations.INVERSE_ONLY ? n : n+1;

   // Perform a few checks on input
   if ( n == 0 ) {
       Message.printWarning(3, routine, "Matrix is empty.");
       return 0;
   }
   if ( indic == MatrixInverseComputations.INVERSE_ONLY ) {
       if ( n != a[0].length ) {
           Message.printWarning(1, routine, "Only n by n matrices can be inversed.");
           throw new InvalidParameterException("Only n by n matrices can be inversed.");
       }
   } else {
       if ( n != a[0].length - 1 ) {
           Message.printWarning(1, routine, "Equation solutions can only be calculated for n by n+1 matrices.");
           throw new InvalidParameterException("Equation solutions can only be calculated for n by n+1 matrices.");
       }
   }

   // Allocate work vector space
   y = new double[n];
   irow = new int[n];
   jcol = new int[n];
   jord = new int[n];

   // Begin elimination procedure

   for (int k = 0; k < n; k++) {

      // Search for the pivot element

      pivot = 0.;
      for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++) {

            // Scan irow and jcol arrays for invalid pivot subscripts

            if (k > 0) {
               test = 1;
               iscan = 0;
               while (test == 1 && iscan < k)
                  if (i == irow[iscan++])
                     test = 0;
               if (test == 0)
                  continue;

               jscan = 0;
               while (test == 1 && jscan < k)
                  if (j == jcol[jscan++])
                     test = 0;
               if (test == 0)
                  continue;
            }

            // Save maximum array element as pivot element

            if (Math.abs(a[i][j]) > Math.abs(pivot)) {
               pivot = a[i][j];
               irow[k] = i;
               jcol[k] = j;
            }
         }
      }

      // Ensure that selected pivot is larger than eps
      if (Math.abs(pivot) <= eps) {
         return(0.);
      }

      // Update the determinant value

      irowk = irow[k];
      jcolk = jcol[k];
      deter *= pivot;

      // Normalize pivot row elements

      for (int j = 0; j < max; j++)
         a[irowk][j] /= pivot;

      // Carry out elimination and develop inverse

      a[irowk][jcolk] = 1. / pivot;
      for (int i = 0; i < n; i++) {
         aijck = a[i][jcolk];
         if (i != irowk) {
            a[i][jcolk] = -aijck / pivot;
            for (int j = 0; j < max; j++) {
               if (j != jcolk)
                  a[i][j] -= aijck * a[irowk][j];
            }
         }
      }
   }

   // Order solution values (if any) and create jord array

   for (int i = 0; i < n; i++) {
      irowi = irow[i];
      jcoli = jcol[i];
      jord[irowi] = jcoli;
      if (indic == MatrixInverseComputations.INVERSE_AND_EQUATION_SOLUTIONS ||
          indic == MatrixInverseComputations.EQUATION_SOLUTIONS_ONLY )
         x[jcoli] = a[irowi][n];
   }

   // Adjust sign of determinant

   nm1 = n - 1;
   for (int i = 0; i < nm1; i++) {
      ip1 = i + 1;
      for (int j = ip1; j < n; j++) {
         if (jord[j] < jord[i]) {
            jtemp = jord[j];
            jord[j] = jord[i];
            jord[i] = jtemp;
            intch++;
         }
      }
   }
   if ((intch / 2 * 2) != intch)
      deter = -deter;

   // If indic is positive, return with results
   if (indic== MatrixInverseComputations.EQUATION_SOLUTIONS_ONLY) {
      return(deter);
   }

   // If indic is negative or zero, unscramble the inverse ...

   // First by rows ...
   for (int j = 0; j < n; j++) {
      for (int i = 0; i < n; i++) {
         irowi = irow[i];
         jcoli = jcol[i];
         y[jcoli] = a[irowi][j];
      }
      for (int i = 0; i < n; i++)
         a[i][j] = y[i];
   }

   // Then by columns ...

   for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
         irowj = irow[j];
         jcolj = jcol[j];
         y[irowj] = a[i][jcolj];
      }
      for (int j = 0; j < n; j++)
         a[i][j] = y[j];
   }

   // Return for indic negative or zero
   return(deter);
}

}
