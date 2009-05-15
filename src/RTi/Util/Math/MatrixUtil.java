/*
 * This class contains tools to handle matrix manipulation.
 * Many of the routines have been modified from a C version, adding
 * better checks for valid input data, easier use (size input matrices and vectors
 * are calculated rather than input), and better methods for handling results
 * (returned from the method rather than part of the parameter list).
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
 * @return determinant of n by n matrix
 */
public static double Inverse ( double[][] a )
throws Exception
{
    double[] x = new double[0];
    return Inverse ( MatrixUtil.MatrixInverseComputations.INVERSE_ONLY, a, x);
}

/**
 *
 * @param indic Indicates desired computations:  one of MatrixInverseComputations
 *      (INVERSE_ONLY, INVERSE_AND_EQUATION_SOLUTIONS, EQUATION_SOLUTIONS_ONLY)
 * @param a;                     // input matrix
 * @param x;                      // vector of equation solutions
 * @return determinant of n by n+1 matrix
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

/**
 * Post-multiply a matrix by a vector.
 * @param x1 input matrix
 * @param x2 vector
 * @return prod product of multiplication
 * @throws java.security.InvalidParameterException
 */
public static double[] Multiply ( double[][] x1, double[] x2  )
throws InvalidParameterException
{
    int nX1Rows = x1.length;
    if ( nX1Rows == 0) {
        throw new InvalidParameterException("Zero-sized input matrix.");
    }

    int nX1Cols = x1[0].length;

    if ( nX1Cols != x2.length ){
        throw new InvalidParameterException(
                "Number of columns in matrix (" + nX1Cols +
                ") doesn't match number of rows in vector ("+ x2.length + ")." );
    }

    double[] prod = new double[nX1Rows];

    for (int i = 0; i < nX1Rows; i++) {
         prod[i] = 0.;
         for (int j = 0; j < nX1Cols; j++)
            prod[i] += x1[i][j] * x2[j];
    }
    return prod;
}

/**
 * Multiply two matrices.
 * @param x1 first input matrix
 * @param x2 second input matrix
 * @return prod product of multiplication
 * @throws java.security.InvalidParameterException
 */
public static double[][] Multiply ( double[][] x1, double[][] x2  )
throws InvalidParameterException
{
    int nX1Rows = x1.length;
    if ( nX1Rows == 0) {
        throw new InvalidParameterException("Zero-sized input matrix.");
    }

    int nX1Cols = x1[0].length;

    if ( nX1Cols != x2.length ){
        throw new InvalidParameterException(
                "Number of columns in first matrix (" + nX1Cols +
                ") doesn't match number of rows in second matrix ("+ x2.length + ")." );
    }
    
    int nX2Cols = x2[0].length;

    double[][] prod = new double[nX1Rows][nX2Cols];

    for (int i = 0; i < nX1Rows; i++) {
      for (int j = 0; j < nX2Cols; j++) {
         prod[i][j] = 0.;
         for (int k = 0; k < nX1Cols; k++)
            prod[i][j] += x1[i][k] * x2[k][j];
      }
    }
    return prod;
}

/**
 * Transpose a matrix.
 * @param x matrix to transpose
 * @return xt resulting matrix
 * @throws java.security.InvalidParameterException
 */
public static double[][] Transpose ( double[][] x )
throws InvalidParameterException
{
    int nrows = x.length;
    if ( nrows == 0 ) {
        throw new InvalidParameterException("Zero-sized matrix.");
    }

    int ncols = x[0].length;

    // It looks odd to see [ncols][nrows], but this is transpose which is
    // exactly what we want.
    double[][] xt = new double[ncols][nrows];

    for (int i = 0; i < nrows; i++) {
       for (int j = 0; j < ncols; j++)
          xt[j][i] = x[i][j];
    }
    return xt;
}

/**
 * Indexes an array arrin[0..n-1], i.e., outputs the array indx[0..n-1]
 *    such that arrin[indx[j]] is in ascending order for j = 0, 1, ... , n-1.
 *    The input quantities n and arrin are not changed.
 *
 * @param arrin input array
 * @param indx index vector
 * @param n number of elements to sort
 */
public static void sortArray(double[] arrin, int[] indx, int n)
{
   int i, indxt, ir, j, l;
   double q;

   /* Initialize the index vector with consecutive integers */

   for (j = 0; j < n; j++)
      indx[j] = j;

   /* From here on, we just have Heapsort, but with indirect indexing
      through indx in all references to arrin */

   l = n / 2 + 1;
   ir = n;
   while (true) {
      if (l > 1)
         q = arrin[(indxt = indx[--l-1])];
      else {
         q = arrin[(indxt = indx[ir-1])];
         indx[ir-1] = indx[0];
         if (--ir == 1) {
            indx[0] = indxt;
            return;
         }
      }
      i = l;
      j = l * 2;
      while (j <= ir) {
         if (j < ir && arrin[indx[j-1]] < arrin[indx[j]])
            j++;
         if (q < arrin[indx[j-1]]) {
            indx[i-1] = indx[j-1];
            j += (i = j);
         }
         else
            j = ir + 1;
      }
      indx[i-1] = indxt;
   }
}

}
