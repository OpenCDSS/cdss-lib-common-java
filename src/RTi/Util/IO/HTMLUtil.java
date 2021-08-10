// HTMLUtil - this class convert a text in an HTML text format with symbolic code (&xxxx;)

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

package RTi.Util.IO;

//package com.jcorporate.expresso.core.misc;

import java.util.Arrays;

import RTi.Util.Message.Message;

//import RTi.Util.Message.Message;

/*
 * HtmlUtil.java
 *
 * Copyright 1999, 2002, 2002 Yves Henri AMAIZO.
 *                            amy_amaizo@compuserve.com
 */

/**
 * This class convert a text in an HTML text format with symbolic code (&xxxx;),
 * it also convert a given HTML text format which contain symbolic code to text.
 *
 * @version        $Revision: 3 $  $Date: 2006-03-01 03:17:08 -0800 (Wed, 01 Mar 2006) $
 * @author        Yves Henri AMAIZO
 */
public class HTMLUtil {

    /**
     * Method text2html: Convert a text to an HTML format.
     * Special characters may be encoded with &xxxx; literal.
     * Characters that do not require encoding, such as space, are passed through.
     * If necessary, search and replace after this encoding is processed.
     *
     * @param text The original text string
     * @param includeWrapper if true, include &lt;html&gt; wrapper tags around the HTML.
     * If false, convert the string encoding to HTML to deal with special characters, but do not wrap the string with the tags.
     * @return The converted HTML text including symbolic codes string
     */
    public static String text2html(String text, boolean includeWrapper) {
    	boolean debug = false; // Set to true to troubleshoot.
        if (text == null) {
            return text;
        }
        
        StringBuilder t = new StringBuilder(text.length());
 
        if ( includeWrapper ) {
            t.append("<html>");
        }
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Check for non ISO8859-1 characters.
            int pos = (int)c;
            if ( debug ) {
            	if ( c == '\n' ) {
            		Message.printStatus(2, "", "Detected NL" );
            	}
            	else if ( c == '\r' ) {
            		Message.printStatus(2, "", "Detected CR" );
            	}
            	Message.printStatus(2, "", "Lookup table position for '" + c + "' is " + pos );
            }
            if ( pos < symbolicCode.length ) {
                // Character is within the lookup table.
                String sc = symbolicCode[pos];
                if ( debug ) {
                	 Message.printStatus(2, "", "Translated character '" + c + "' is '" + sc + "'" );
                }
                if ("".equals(sc)) {
                    // Character does not need to be converted so just append the original character.
                    t = t.append(c);
                }
                else {
                    // Character was converted to encoded representation.
                    t = t.append(sc);
                }
            }
            else {
                // Not in the lookup table so just append.
                t = t.append(c);
            }
        }
        if ( includeWrapper ) {
            t.append("</html>");
        }
        return t.toString();
    }

    /**
     * Method html2text: Convert an HTML text format to a normal text format.
     *
     * @param text:     The original HTML text string
     * @return          The converted text without symbolic codes string
     */
    public static String html2text(String text) {
        if (text == null) {
            return text;
        }
        StringBuilder t = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&') {
                String code = String.valueOf(c);
                do {
                    if (++i >= text.length()) {
                        break;
                    }
                    if (text.charAt(i) == '&') {
                        i--;
                        break;
                    }
                    code += text.charAt(i);
                } while (text.charAt(i) != ';');
                int index = Arrays.binarySearch(sortedSymbolicCode, new NumericSymbolicCode(code, 0));
                // Does the extracting code correspond to something ?
                if (index >= 0) {
                    t = t.append((char) sortedSymbolicCode[index].getNumericCode());
                }
                else {
                    t = t.append(code);
                }
            }
            else {
                t = t.append(c);
            }
        }
        return t.toString();
    }

    /**
     * Array of symbolic code order by numeric code ! <br>
     * The symbolic codes and their position correspond to the ISO 8859-1 set
     * of char. The empty definitions mean that there is no symbolic codes for
     * that character or this symbolic code is not used.
     */
    private static final String[] symbolicCode = {
        // 0+
        "", "", "", "", "", "", "", "", "", "",
        // 10+
        // 10 - Linux newline
        // 13 - carriage return - only convert the newline (10) character
        "<br>", "", "", "", "", "", "", "", "", "",
        // 20+
        "", "", "", "", "",
        "&#25;", // yen sign
        "", "", "", "",
        // 30+
        "", "", "", "",
        "&quot;", // quotation mark
        "", "", "", "&amp;", "",
        // 40+
        "", "", "", "", "", "", "", "", "", "",
        // 50+
        "", "", "", "", "", "", "", "", "", "",
        // 60+
        "&lt;", "", "&gt;", "",
        "&#64;", // commercial at
        "", "", "", "", "",
        // 70+
        "", "", "", "", "", "", "", "", "", "",
        // 80+
        "", "", "", "", "", "", "", "", "", "",
        // 90+
        "", "", "", "", "", "",
        "&#96;", // grave accent
        "", "", "",
        // 100+
        "", "", "", "", "", "", "", "", "", "",
        // 110+
        "", "", "", "", "", "", "", "", "", "",
        // 120+
        "", "", "", "", "", "", "", "", "&#128;", "",
        // 130+
        "", "", "", "", "", "", "", "", "", "",
        // 140+
        "", "", "", "", "", "&#145;",
        "&#146;", // other apostrophe
        "&#147;", "&#148;", "",
        // 150+
        "", "", "", "", "", "", "", "", "", "",
        // 160+
        "&nbsp;", // non breaking space (should be &nbsp;)
        "&iexcl;", // inverted exclamation sign
        "&cent;", // cent sign
        "&pound;", // pound sterling sign
        "&curren;", // general currency sign
        "&yen;", // yen sign
        "&brvbar;", // broken vertical bar
        "&sect;", // section sign (legal)
        "&uml;", // umlaut (dieresis)
        "&copy;", // copyright
        // 170+
        "&ordf;", // feminine ordinal
        "&laquo;", // guillemot left
        "&not;", // not sign
        "&shy;", // soft hyphen
        "&reg;", // registered trademark
        "&macr;", // macron accent
        "&deg;", // degree sign
        "&plusmn;", // plus or minus
        "&sup2;", // raised to square(superscript two)
        "&sup3;", // superscript three
        // 180+
        "&acute;", // acute accent
        "&micro;", // micron sign
        "&para", // paragraph sign, Pi
        "&middot;", // middle dot
        "&cedil;", // cedilla mark
        "&supl;", // raised to one(superscript one)
        "&ordm;", // masculine ordinal
        "&raquo;", // guillemot right
        "&frac14;", // one-forth fraction
        "&frac12;", // half fraction
        // 190+
        "&frac34;", // three-fourth fraction
        "&iquest;", // inverted question mark
        "&Agrave;", // A with grave accent
        "&Aacute;", // A with acute accent
        "&Acirc;", // A with circumflex accent
        "&Atilde;", // A with tilde accent
        "&Auml;", // A with angstrom
        "&Aring;", // A with umlaut mark
        "&AElig;", // AE dipthong (ligature)
        "&Ccedil;", // C with cedilla mark
        // 200+
        "&Egrave;", // E with grave accent
        "&Eacute;", // E with acute accent
        "&Ecirc;", // E with circumflex accent
        "&Euml;", // E with umlaut mark
        "&Igrave;", // I with grave accent
        "&Iacute;", // I with acute accent
        "&Icirc;", // I with circumflex accent
        "&Iuml;", // I with umlaut mark
        "&ETH;", // Icelandic Capital Eth
        "&Ntilde;", // N with tilde accent
        // 210+
        "&Ograve;", // O with grave accent
        "&Oacute;", // O with acute accent
        "&Ocirc;", // O with circumflex accent
        "&Otilde;", // O with tilde accent
        "&Ouml;", // O with umlaut mark
        "&times;", // multiply sign
        "&Oslash;", // O slash
        "&Ugrave;", // U with grave accent
        "&Uacute;", // U with acute accent
        "&Ucirc;", // U with circumflex accent
        // 220+
        "&Uuml;", // U with umlaut mark
        "&Yacute;", // Y with acute accent
        "&THORN;", // Icelandic Capital Thorn
        "&szlig;", // small sharp s(sz ligature)
        "&agrave;", // a with grave accent
        "&aacute;", // a with acute accent
        "&acirc;", // a with circumflex accent
        "&atilde;", // a with tilde accent
        "&auml;", // a with angstrom
        "&aring;", // a with umlaut mark
        // 230+
        "&aelig;", // ae dipthong (ligature)
        "&ccedil;", // c with cedilla mark
        "&egrave;", // e with grave accent
        "&eacute;", // e with acute accent
        "&ecirc;", // e with circumflex accent
        "&euml;", // e with umlaut mark
        "&igrave;", // i with grave accent
        "&iacute;", // i with acute accent
        "&icirc;", // i with circumflex accent
        "&iuml;", // i with umlaut mark
        // 240+
        "&eth;", // Icelandic small eth
        "&ntilde;", // n with tilde accent
        "&ograve", // o with grave accent
        "&oacute;", // o with acute accent
        "&ocirc;", // o with circumflex accent
        "&otilde", // o with tilde accent
        "&ouml;", // o with umlaut mark
        "&divide;", // divide sign
        "&oslash;", // o slash
        "&ugrave;", // u with grave accent
        // 250+
        "&uacute;", // u with acute accent
        "&ucirc;", // u with circumflex accent
        "&uuml;", // u with umlaut mark
        "&yacute;", // y with acute accent
        "&thorn;", // Icelandic small thorn
        "&yuml;", // y with umlaut mark
    };

    /**
     * Array of symbolic code order symbolic code !<br>
     * This array is the reciprocal from the 'symbolicCode' array.
     */
    private static NumericSymbolicCode[] sortedSymbolicCode =
            new NumericSymbolicCode[symbolicCode.length];

    /**
     * This class is the structure used for the 'sortedSymbolicCode' array.
     * Each symbolic code string (sorted by alphabetical order) have its numerical
     * corresponding code.<br>
     * This class also implements the 'Comparable' interface to ease the sorting
     * process in the initialisation bloc.
     */
    final private static class NumericSymbolicCode implements Comparable<NumericSymbolicCode> {

        public NumericSymbolicCode(String symbolicCode, int numericCode) {
            this.symbolicCode = symbolicCode;
            this.numericCode = numericCode;
        }

        //public String getSymbolicCode() {
        //    return symbolicCode;
        //}

        public int getNumericCode() {
            return numericCode;
        }

        public int compareTo(NumericSymbolicCode nsc) {
            return symbolicCode.compareTo(nsc.symbolicCode);
        }

        private String symbolicCode;
        private int numericCode;
    }

    /**
     * Initialization and sorting of the 'sortedSymbolicCode'
     */
    static {
        for (int i = 0; i < symbolicCode.length; i++) {
            sortedSymbolicCode[i] = new NumericSymbolicCode(symbolicCode[i], i);
        }
        Arrays.sort(sortedSymbolicCode);
    }
}
