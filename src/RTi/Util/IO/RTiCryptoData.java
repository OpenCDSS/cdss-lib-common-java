// RTiCryptoData - class that holds information about the different cipher packages, passwords and seeds used.

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

// ----------------------------------------------------------------------------
// RTiCryptoData.java - Class that holds information about the different
//			cipher packages, passwords and seeds used.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-01-09	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.Util.IO;

/**
The RTiCryptoData class stores the encryption and decryption data 
(passwords and seeds) for a given RTi cipher (as determined by a 
two-character prefix on encrypted data).
*/
public class RTiCryptoData {

/**
Returns a Blowfish CBCIV code for a given prefix.  CBCIV codes are used
to seed the Blowfish encryption/decryption cipher.
@param prefix the prefix for which to return the CBCIV code.
@return a CBCIV string for the prefix.
*/
public static long getBlowfishCBCIV(String prefix) {
	if (prefix.equals("00")) {
		return 0xadeadcafeBeefcabL;
	}
	return 0x0000000000000000L;
}

/**
Returns a Blowfish password for the given prefix.  
@param prefix the prefix for which to return the password.
@return a password for the given prefix.
*/
public static byte[] getBlowfishPassword(String prefix) {
	String pw = "";
	if (prefix.equals("00")) {
		pw = "default RTi first version password";
	}

	int len = pw.length();
	byte[] bpw = new byte[len];

	for (int i = 0; i < len; i++) {
		bpw[i] = (byte)pw.charAt(i);
	}
	
	return bpw;
}

/**
Returns the type of Cipher package being used based on a two-character
prefix.
@param prefix the two-character prefix to specify the package.
@return the cipher package that is associated with the two-character prefix,
or -1 if the package was unknown.  
*/
public static int lookupCipherPackage(String prefix) {
	if (prefix.equals("00")) {
		return Cipher.BLOWFISH;
	} 
	return -1;
}

}
