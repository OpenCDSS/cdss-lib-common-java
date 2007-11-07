// ----------------------------------------------------------------------------
// History:
//
// 23 May 2000	Steven A. Malers, RTi	See copyright for source.  Add to GR
//					library and test with some GR devices
//					(e.g., GeoView).  Modified the code
//					as follows:
//					* Convert import * to explicit imports.
//					* Use RTi messages instead of errors to
//					  standard error.
//					* Remove derivation from Frame - this
//					  appears to have been used only to
//					  enable use of the Media tracker.
// 2002-11-05	J. Thomas Sapienza, RTi	Reformatted.  Got rid of 4-space tabs,
//					made variable private and public 
//					depending on use.  Eliminated unused
//					variables, cleaned up code, commented
//					where possible, organized methods 
//					alphabetically, and split out into
//					separate classes.
// ----------------------------------------------------------------------------
// COPYRIGHT:
//
// Version 1.0a
// Copyright (C) 1998, James R. Weeks and BioElectroMech.
// Visit BioElectroMech at www.obrador.com.  Email James@obrador.com.

// See license.txt for details about the allowed used of this software.
// This software is based in part on the work of the Independent JPEG Group.
// See IJGreadme.txt for details about the Independent JPEG Group's license.

// This encoder is inspired by the Java Jpeg encoder by Florian Raemy,
// studwww.eurecom.fr/~raemy.
// It borrows a great deal of code and structure from the Independent
// Jpeg Group's Jpeg 6a library, Copyright Thomas G. Lane.
// See license.txt for details.
// ----------------------------------------------------------------------------

package RTi.GR;

// This class incorporates quality scaling as implemented in the JPEG-6a
// library.

/**
 * A Java implementation of the Discreet Cosine Transform
 */
class JpegEncoder_DCT
{
/**
 * Divisors for chrominance
 */
public double divisorsChrominance[] = new double[N*N];
/**
 * Divisors for luminance
 */
public double divisorsLuminance[] = new double[N*N];

/**
 * DCT Block Size - default 8
 */
public static int N = 8;
/**
 * Image quality (0-100) - default 80 (good image / good compression)
 */
public static int QUALITY = 80;
/**
 * Quantitization Matrix for luminace.
 */
public int quantumLuminance[]     = new int[N*N];
/**
 * Quantitization Matrix for chrominance.
 */
public int quantumChrominance[]     = new int[N*N];

/**
 * ??
 */
public Object divisors[] = new Object[2];
/**
 * ??
 */
public Object quantum[] = new Object[2];

/**
* Constructs a new DCT object. Initializes the cosine transform matrix
* these are used when computing the DCT and it's inverse. This also
* initializes the run length counters and the ZigZag sequence. Note that
* the image quality can be worse than 25 however the image will be
* extemely pixelated, usually to a block size of N.
*
* @param quality the quality of the image (0 worst - 100 best)
*/
public JpegEncoder_DCT(int quality)
{
	initMatrix(quality);
}

/**
 * This method performs a DCT on a block of image data using the AAN
 * method as implemented in the IJG Jpeg-6a library.
 * 
 * @param input the input data on which to have the DCT performed.
 */
public double[][] forwardDCT(float input[][])
{
	double output[][] = new double[N][N];
	double tmp00, tmp01, tmp02, tmp03, tmp04, tmp05, tmp06, tmp07;
	double tmp08, tmp09, tmp10, tmp11;
	double z1, z2, z3, z4, z5, z6, z7;
	int i;
	int j;
	
	// Subtracts 128 from the input values
	for (i = 0; i < 8; i++) {
		for(j = 0; j < 8; j++) {
			output[i][j] = ((double)input[i][j] - (double)128.0);
			// input[i][j] -= 128;
	
		}
	}
	
	for (i = 0; i < 8; i++) {
		tmp00 = output[i][0] + output[i][7];
		tmp07 = output[i][0] - output[i][7];
		tmp01 = output[i][1] + output[i][6];
		tmp06 = output[i][1] - output[i][6];
		tmp02 = output[i][2] + output[i][5];
		tmp05 = output[i][2] - output[i][5];
		tmp03 = output[i][3] + output[i][4];
		tmp04 = output[i][3] - output[i][4];
		
		tmp08 = tmp00 + tmp03;
		tmp11 = tmp00 - tmp03;
		tmp09 = tmp01 + tmp02;
		tmp10 = tmp01 - tmp02;
	
		output[i][0] = tmp08 + tmp09;
		output[i][4] = tmp08 - tmp09;
	
		z1 = (tmp10 + tmp11) * (double) 0.707106781;
		output[i][2] = tmp11 + z1;
		output[i][6] = tmp11 - z1;
	
		tmp08 = tmp04 + tmp05;
		tmp09 = tmp05 + tmp06;
		tmp10 = tmp06 + tmp07;
	
		z5 = (tmp08 - tmp10) * (double) 0.382683433;
		z2 = ((double) 0.541196100) * tmp08 + z5;
		z4 = ((double) 1.306562965) * tmp10 + z5;
		z3 = tmp09 * ((double) 0.707106781);
	
		z6 = tmp07 + z3;
		z7 = tmp07 - z3;
	
		output[i][5] = z7 + z2;
		output[i][3] = z7 - z2;
		output[i][1] = z6 + z4;
		output[i][7] = z6 - z4;
	}
	
	for (i = 0; i < 8; i++) {
		tmp00 = output[0][i] + output[7][i];
		tmp07 = output[0][i] - output[7][i];
		tmp01 = output[1][i] + output[6][i];
		tmp06 = output[1][i] - output[6][i];
		tmp02 = output[2][i] + output[5][i];
		tmp05 = output[2][i] - output[5][i];
		tmp03 = output[3][i] + output[4][i];
		tmp04 = output[3][i] - output[4][i];
		
		tmp08 = tmp00 + tmp03;
		tmp11 = tmp00 - tmp03;
		tmp09 = tmp01 + tmp02;
		tmp10 = tmp01 - tmp02;
		
		output[0][i] = tmp08 + tmp09;
		output[4][i] = tmp08 - tmp09;
		
		z1 = (tmp10 + tmp11) * (double) 0.707106781;
		output[2][i] = tmp11 + z1;
		output[6][i] = tmp11 - z1;
		
		tmp08 = tmp04 + tmp05;
		tmp09 = tmp05 + tmp06;
		tmp10 = tmp06 + tmp07;
		
		z5 = (tmp08 - tmp10) * (double) 0.382683433;
		z2 = ((double) 0.541196100) * tmp08 + z5;
		z4 = ((double) 1.306562965) * tmp10 + z5;
		z3 = tmp09 * ((double) 0.707106781);
		
		z6 = tmp07 + z3;
		z7 = tmp07 - z3;
		
		output[5][i] = z7 + z2;
		output[3][i] = z7 - z2;
		output[1][i] = z6 + z4;
		output[7][i] = z6 - z4;
	}

	return output;
}

/**
 * This method preforms forward DCT on a block of image data using
 * the literal method specified for a 2-D Discrete Cosine Transform.
 * It is included as a curiosity and can give you an idea of the
 * difference in the compression result (the resulting image quality)
 * by comparing its output to the output of the AAN method below.
 * It is ridiculously inefficient.
 */
// For now the final output is unusable.  The associated quantization step
// needs some tweaking.  If you get this part working, please let me know.
public double[][] forwardDCTExtreme(float input[][])
{
	double output[][] = new double[N][N];
	int u, v, x, y;
	
	for (v = 0; v < 8; v++) {
		for (u = 0; u < 8; u++) {
			for (x = 0; x < 8; x++) {
				for (y = 0; y < 8; y++) {
					output[v][u] += 
					((double)input[x][y])*Math.cos((
					(double)(2*x + 1)*(double)u*Math.PI)/
					(double)16)*Math.cos(((double)(2*y + 1)
					*(double)v*Math.PI)/(double)16);
				}			
			}
			output[v][u] *= (double)(0.25)*((u == 0) ? 
				((double)1.0/Math.sqrt(2)) : 
				(double) 1.0)*((v == 0) ? 
					((double)1.0/Math.sqrt(2)) : 
					(double) 1.0);
		}
	}
	return output;
}

/**
 * This method sets up the quantization matrix for luminance and
 * chrominance using the quality parameter.
 */
private void initMatrix(int initQuality)
{
	double[] AANscaleFactor = { 
		1.0, 
		1.387039845, 
		1.306562965, 
		1.175875602,
		1.0, 
		0.785694958, 
		0.541196100, 
		0.275899379
	};
	int i;
	int j;
	int index;
	int quality;
	int temp;

	// converting quality setting to that specified in 
	// the jpeg_quality_scaling method in the IJG Jpeg-6a C libraries
	quality = initQuality;
	if (quality <= 0) {
		quality = 1;
	}
	if (quality > 100) {
		quality = 100;
	}
	if (quality < 50) {
		quality = 5000 / quality;
	}
	else {
		quality = 200 - quality * 2;
	}

	// Creating the luminance matrix
	quantumLuminance[0]=16;
	quantumLuminance[1]=11;
	quantumLuminance[2]=10;
	quantumLuminance[3]=16;
	quantumLuminance[4]=24;
	quantumLuminance[5]=40;
	quantumLuminance[6]=51;
	quantumLuminance[7]=61;
	quantumLuminance[8]=12;
	quantumLuminance[9]=12;
	quantumLuminance[10]=14;
	quantumLuminance[11]=19;
	quantumLuminance[12]=26;
	quantumLuminance[13]=58;
	quantumLuminance[14]=60;
	quantumLuminance[15]=55;
	quantumLuminance[16]=14;
	quantumLuminance[17]=13;
	quantumLuminance[18]=16;
	quantumLuminance[19]=24;
	quantumLuminance[20]=40;
	quantumLuminance[21]=57;
	quantumLuminance[22]=69;
	quantumLuminance[23]=56;
	quantumLuminance[24]=14;
	quantumLuminance[25]=17;
	quantumLuminance[26]=22;
	quantumLuminance[27]=29;
	quantumLuminance[28]=51;
	quantumLuminance[29]=87;
	quantumLuminance[30]=80;
	quantumLuminance[31]=62;
	quantumLuminance[32]=18;
	quantumLuminance[33]=22;
	quantumLuminance[34]=37;
	quantumLuminance[35]=56;
	quantumLuminance[36]=68;
	quantumLuminance[37]=109;
	quantumLuminance[38]=103;
	quantumLuminance[39]=77;
	quantumLuminance[40]=24;
	quantumLuminance[41]=35;
	quantumLuminance[42]=55;
	quantumLuminance[43]=64;
	quantumLuminance[44]=81;
	quantumLuminance[45]=104;
	quantumLuminance[46]=113;
	quantumLuminance[47]=92;
	quantumLuminance[48]=49;
	quantumLuminance[49]=64;
	quantumLuminance[50]=78;
	quantumLuminance[51]=87;
	quantumLuminance[52]=103;
	quantumLuminance[53]=121;
	quantumLuminance[54]=120;
	quantumLuminance[55]=101;
	quantumLuminance[56]=72;
	quantumLuminance[57]=92;
	quantumLuminance[58]=95;
	quantumLuminance[59]=98;
	quantumLuminance[60]=112;
	quantumLuminance[61]=100;
	quantumLuminance[62]=103;
	quantumLuminance[63]=99;

	for (j = 0; j < 64; j++) {	
		temp = (quantumLuminance[j] * quality + 50) / 100;
		if ( temp <= 0) {
			temp = 1;
		}
		if (temp > 255) {
			temp = 255;
		}
		quantumLuminance[j] = temp;
	}
	
	index = 0;
	for (i = 0; i < 8; i++) {
		for (j = 0; j < 8; j++) {
		// The divisors for the LL&M method (the slow integer method 
		// used in jpeg 6a library).  This method is currently 
		// (04/04/98) incompletely implemented.
		//divisorsLuminance[index] = 
		//	((double) quantumLuminance[index]) << 3;
		// The divisors for the AAN method (the float method used in 
		// jpeg 6a library.
			divisorsLuminance[index] = 
				(double) ((double)1.0/((double) 
				quantumLuminance[index] *
				AANscaleFactor[i] * AANscaleFactor[j] * 
				(double) 8.0));
			index++;
		}
	}


	// Creating the chrominance matrix
	quantumChrominance[0]=17;
	quantumChrominance[1]=18;
	quantumChrominance[2]=24;
	quantumChrominance[3]=47;
	quantumChrominance[4]=99;
	quantumChrominance[5]=99;
	quantumChrominance[6]=99;
	quantumChrominance[7]=99;
	quantumChrominance[8]=18;
	quantumChrominance[9]=21;
	quantumChrominance[10]=26;
	quantumChrominance[11]=66;
	quantumChrominance[12]=99;
	quantumChrominance[13]=99;
	quantumChrominance[14]=99;
	quantumChrominance[15]=99;
	quantumChrominance[16]=24;
	quantumChrominance[17]=26;
	quantumChrominance[18]=56;
	quantumChrominance[19]=99;
	quantumChrominance[20]=99;
	quantumChrominance[21]=99;
	quantumChrominance[22]=99;
	quantumChrominance[23]=99;
	quantumChrominance[24]=47;
	quantumChrominance[25]=66;
	quantumChrominance[26]=99;
	quantumChrominance[27]=99;
	quantumChrominance[28]=99;
	quantumChrominance[29]=99;
	quantumChrominance[30]=99;
	quantumChrominance[31]=99;
	quantumChrominance[32]=99;
	quantumChrominance[33]=99;
	quantumChrominance[34]=99;
	quantumChrominance[35]=99;
	quantumChrominance[36]=99;
	quantumChrominance[37]=99;
	quantumChrominance[38]=99;
	quantumChrominance[39]=99;
	quantumChrominance[40]=99;
	quantumChrominance[41]=99;
	quantumChrominance[42]=99;
	quantumChrominance[43]=99;
	quantumChrominance[44]=99;
	quantumChrominance[45]=99;
	quantumChrominance[46]=99;
	quantumChrominance[47]=99;
	quantumChrominance[48]=99;
	quantumChrominance[49]=99;
	quantumChrominance[50]=99;
	quantumChrominance[51]=99;
	quantumChrominance[52]=99;
	quantumChrominance[53]=99;
	quantumChrominance[54]=99;
	quantumChrominance[55]=99;
	quantumChrominance[56]=99;
	quantumChrominance[57]=99;
	quantumChrominance[58]=99;
	quantumChrominance[59]=99;
	quantumChrominance[60]=99;
	quantumChrominance[61]=99;
	quantumChrominance[62]=99;
	quantumChrominance[63]=99;

	for (j = 0; j < 64; j++) {
		temp = (quantumChrominance[j] * quality + 50) / 100;
		if ( temp <= 0) {
			temp = 1;
		}
		if (temp >= 255) {
			temp = 255;	
		}
		quantumChrominance[j] = temp;
	}
	
	index = 0;
	for (i = 0; i < 8; i++) {
		for (j = 0; j < 8; j++) {
		// The divisors for the LL&M method (the slow integer method 
		// used in jpeg 6a library).  This method is currently 
		// (04/04/98) incompletely implemented.
			// divisorsChrominance[index] = 
			//	((double) quantumChrominance[index]) << 3;
		// The divisors for the AAN method (the float method used 
		// in jpeg 6a library.
			divisorsChrominance[index] = 
				(double) ((double)1.0/((double) 
				quantumChrominance[index] * AANscaleFactor[i] *
				AANscaleFactor[j] * (double)8.0));
			index++;
		}
	}

	// quantum and divisors are objects used to hold the 
	// appropriate matrices
	quantum[0] = quantumLuminance;
	divisors[0] = divisorsLuminance;
	quantum[1] = quantumChrominance;
	divisors[1] = divisorsChrominance;
}


/**
 * This method quantitizes data and rounds it to the nearest integer.
 *
 * @param inputData the data to be quantized
 * @param code ??
 */
public int[] quantizeBlock(double inputData[][], int code)
{
	int i;
	int index;
	int j;
	int outputData[] = new int[N*N];
	
	index = 0;
	for (i = 0; i < 8; i++) {
		for (j = 0; j < 8; j++) {
		// The second line results in significantly better compression.
			outputData[index] = (int)(Math.round(inputData[i][j] * 
				(((double[]) (divisors[code]))[index])));
			// outputData[index] = (int)(((inputData[i][j] * 
			//	(((double[]) (divisors[code]))[index])) 
			//	+ 16384.5) -16384);
			index++;
		}
	}

	return outputData;
}

/**
 * This is the method for quantizing a block DCT'ed with forwardDCTExtreme
 * This method quantitizes data and rounds it to the nearest integer.
 *
 * @param inputData the data to be quantized
 * @param code ??
 */
public int[] quantizeBlockExtreme(double inputData[][], int code)
{
	int i;
	int index;
	int j;
	int outputData[] = new int[N*N];
	
	index = 0;
	for (i = 0; i < 8; i++) {
		for (j = 0; j < 8; j++) {
			outputData[index] = (int)(Math.round(inputData[i][j] 
				/ (double)(((int[]) (quantum[code]))[index])));
			index++;
		}
	}

	return outputData;
}
}

