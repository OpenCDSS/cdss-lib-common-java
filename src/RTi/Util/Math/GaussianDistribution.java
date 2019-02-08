// GaussianDistribution - Gaussian distribution

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.Util.Math;

/**
Gaussian distribution.
C
    C        GAUSSIAN PROBABILITY FUNCTIONS   W.KIRBY  JUNE 71
    C        GAUSEX=VALUE EXCEEDED WITH PROB EXPROB
    C        GAUSAB=VALUE (NOT EXCEEDED) WITH PROBCUMPROB
    C        GAUSCF=CUMULATIVE PROBABILITY FUNCTION
    C        GAUSDY=DENSITY FUNCTION
    C
    C        SUBPGMS USED -- NONE
    C
    C        GAUSCF MODIFIED 740906 WK -- REPLACED ERF FCN REF BY RATIONAL
    C             APPRX N
    C        ALSO REMOVED DOUBLE PRECISION FROM GAUSEX AND GAUSAB.
    C        76-05-04 WK -- TRAP UNDERFLOWS IN EXP IN GUASCF AND DY.
    C
*/
public class GaussianDistribution
{

private static double XLIM = 18.3;
    
/**
 * 
 */
public static double ex ( double p )
{
    double C0 = 2.51551700;
    double C1 = .8028530000;
    double C2 = .0103280000;

    double D1 = 1.432788000;
    double D2 = .1892690000;
    double D3 = .0013080000;

    if ( p >= 1.0 ) {
        return -10.;
    }

    else if ( p <= 0.) {
        return 10.;
    }
    double pr = p;
    if ( p > .5 ) {
        pr = 1.00 - pr;
    }
    double t = Math.sqrt(-2.00*Math.log(pr));
    double GAUSEX = t - (C0 + t*(C1 + t*C2))/(1. + t*(D1+t*(D2+t*D3)));
    if ( p > .5 ) {
        GAUSEX = -GAUSEX;
    }
    return GAUSEX;
}

/**
 *
 */
public static double ab ( double cumprb )
{
    double p = 1. - cumprb;
    return ex ( p );
}
              
/**
 * 
 */
public static double cf ( double XX )
{
    double ax = Math.abs(XX);
    double GAUSCF = 1.;
    if ( ax <= XLIM ) {
          double t = 1.0/(1.0 + .2316419*ax);
          double d = 0.3989423*Math.exp(-XX*XX*.5);
          GAUSCF = 1.-d*t*((((1.330274*t - 1.821256)*t + 1.781478)*t - 0.3565638)*t + 0.3193815);
    }
    if ( XX < 0.0 ) {
        GAUSCF = 1. - GAUSCF;
    }
    return GAUSCF;
}
    
/**
 * 
 */
public static double dy ( double XX )
{
    if ( Math.abs(XX) > XLIM ) {
        return 0.0;
    }
    return .3989423*Math.exp(-.500*XX*XX);
}

}
