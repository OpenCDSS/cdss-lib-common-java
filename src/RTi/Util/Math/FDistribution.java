// FDistribution - this class provides features related to the F distribution

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
This class provides features related to the F distribution.
*/
public class FDistribution
{

/**
Determine the cumulative F distribution value.
@param v1 Degrees of freedom.  Currently only support 2.
@param v2 Degrees of freedom.  Can be any value.
@param probability Probability associated with right-side of distribution (e.g.,
1 = 99% cumulative, 5 = 95% cumulative.  Currently only support 1 and 5.
@exception Exception if the input parameters are not supported.
*/
public static double getCumulativeFDistribution ( int v1, int v2, int probability )
throws Exception
{	if ( v1 != 2 ) {
		throw new Exception ( "v1 (" + v1 + ") must be 2" );
	}
	if ( (probability != 5) && (probability != 1) ) {
		throw new Exception ( "probability (" + probability + ") must be 1 or 5" );
	}

	int col = 2;
	if ( probability == 1 ) {
		col = 1;
	}

	// For now don't carry around this data as static so the footprint of the code is smaller.

	//			v2    F_.01	F_.05
	double [][] F_v2 = {
	            { 1.0, 5000.0,	200.0 },
				{ 2.0, 99.0,	19.0 },
				{ 3.0, 30.82,	9.55 },
				{ 4.0, 18.00,	6.94 },
				{ 5.0, 13.27,	5.79 },
				{ 6.0, 10.92,	5.14 },
				{ 7.0, 9.55,	4.74 },
				{ 8.0, 8.65,	4.46 },
				{ 9.0, 8.02,	4.26 },
				{ 10.0, 7.56,	4.10 },
				{ 11.0, 7.21,	3.98 },
				{ 12.0, 6.93,	3.89 },
				{ 13.0, 6.70,	3.81 },
				{ 14.0, 6.51,	3.74 },
				{ 15.0, 6.36,	3.68 },
				{ 17.0, 6.11,	3.59 },
				{ 20.0, 5.85,	3.49 },
				{ 25.0, 5.57,	3.39 },
				{ 30.0, 5.39,	3.32 },
				{ 40.0, 5.18,	3.23 },
				{ 50.0, 5.03,	3.18 },
				{ 75.0, 4.90,	3.12 },
				{ 100.0, 4.82,	3.09 },
				{ 200.0, 4.71,	3.04 },
				{ 500.0, 4.65,	3.01 },
				{ 1000.0, 4.63,	3.00 },
				{ 1.0e10, 4.61,	3.00 } };

	int size = 27;
	double dv2 = (double)v2;
	if ( dv2 < F_v2[0][0] ) {
		// Probably precision problem.  Use the first value
		return F_v2[0][col];
	}
	else if ( dv2 > F_v2[size - 1][0] ) {
		// Probably precision problem.  Use the maximum value...
		return F_v2[size - 1][col];
	}
	// Else find bounding values and interpolate...
	int i_lower = 0, i_upper = 1;
	for ( int i = (size - 1); i >= 0; i-- ) {
		if ( dv2 >= F_v2[i][0] ) {
			i_lower = i;
			i_upper = i + 1;
			break;
		}
	}
	return MathUtil.interpolate ( dv2,
			F_v2[i_lower][0], F_v2[i_upper][0],
			F_v2[i_lower][col], F_v2[i_upper][col] );
}

}
