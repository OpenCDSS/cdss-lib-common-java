package RTi.Util.IO;

import java.util.HashMap;
import java.util.Map;

import javax.print.PrintService;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;

import RTi.Util.String.StringUtil;

// TODO SAM 2011-06-24 Evaluate using a singleton, etc., but don't want a bunch of static code if it can be avoided.

/**
Facilitate looking up paper sizes.
Borrowed from:  http://www.jpedal.org/gplSrc/org/jpedal/examples/simpleviewer/paper/PaperSizes.java.html
*/
public class PaperSizeLookup
{

/**
Map to convert between MediaSizeName and more readable names.
*/
private Map<String,String> paperNames = new HashMap<String,String>();

/**
Constructor.
*/
protected PaperSizeLookup ()
{
    populateDisplayNameMap();
}

/**
Get the common page size name for the page size.
@param the mediaName (media.toString() to look up (e.g., "na-letter")
@return the common name that can be used for displays (e.g., "North American Letter")
*/
public String lookupDisplayName ( String mediaName )
{
    return paperNames.get(mediaName);
}

/**
Lookup the Media integer from the media name (Media.toString()).
@param mediaName name of the media (e.g., "na-letter").
@return the Media that corresponds to the name, or null if not matched.
*/
public Media lookupMediaFromName ( PrintService printService, String mediaName )
{   Media [] supportedMediaArray =
        (Media [])printService.getSupportedAttributeValues(Media.class, null, null);
    if ( supportedMediaArray != null ) {
        for ( int i = 0; i < supportedMediaArray.length; i++ ) {
            if ( supportedMediaArray[i].toString().equalsIgnoreCase(mediaName) ) {
                return supportedMediaArray[i];
            }
        }
    }
    return null;
}

/**
Populate the list of paper sizes corresponding to the paperNames list.
These are protected (?why?) in the MediaSize so need to have this list.
*/
public MediaSizeName lookupMediaSizeNameFromString ( String mediaSizeName )
{
    MediaSizeName [] mediaSizeNameArray = {
        MediaSizeName.ISO_A4,
        MediaSizeName.NA_LETTER,
        MediaSizeName.ISO_A0,
        MediaSizeName.ISO_A1,
        MediaSizeName.ISO_A2,
        MediaSizeName.ISO_A3,
        MediaSizeName.ISO_A5,
        MediaSizeName.ISO_A6,
        MediaSizeName.ISO_A7,
        MediaSizeName.ISO_A8,
        MediaSizeName.ISO_A9,
        MediaSizeName.ISO_A10,
        MediaSizeName.ISO_B0,
        MediaSizeName.ISO_B1,
        MediaSizeName.ISO_B2,
        MediaSizeName.ISO_B3,
        MediaSizeName.ISO_B4,
        MediaSizeName.ISO_B5,
        MediaSizeName.ISO_B6,
        MediaSizeName.ISO_B7,
        MediaSizeName.ISO_B8,
        MediaSizeName.ISO_B9,
        MediaSizeName.ISO_B10,
        MediaSizeName.JIS_B0,
        MediaSizeName.JIS_B1,
        MediaSizeName.JIS_B2,
        MediaSizeName.JIS_B3,
        MediaSizeName.JIS_B4,
        MediaSizeName.JIS_B5,
        MediaSizeName.JIS_B6,
        MediaSizeName.JIS_B7,
        MediaSizeName.JIS_B8,
        MediaSizeName.JIS_B9,
        MediaSizeName.JIS_B10,
        MediaSizeName.ISO_C0,
        MediaSizeName.ISO_C1,
        MediaSizeName.ISO_C2,
        MediaSizeName.ISO_C3,
        MediaSizeName.ISO_C4,
        MediaSizeName.ISO_C5,
        MediaSizeName.ISO_C6,
        MediaSizeName.NA_LEGAL,
        MediaSizeName.EXECUTIVE,
        MediaSizeName.LEDGER,
        MediaSizeName.TABLOID,
        MediaSizeName.INVOICE,
        MediaSizeName.FOLIO,
        MediaSizeName.QUARTO,
        MediaSizeName.JAPANESE_POSTCARD,
        MediaSizeName.JAPANESE_DOUBLE_POSTCARD,
        MediaSizeName.A,
        MediaSizeName.B,
        MediaSizeName.C,
        MediaSizeName.D,
        MediaSizeName.E,
        MediaSizeName.ISO_DESIGNATED_LONG,
        MediaSizeName.ITALY_ENVELOPE,
        MediaSizeName.MONARCH_ENVELOPE,
        MediaSizeName.PERSONAL_ENVELOPE,
        MediaSizeName.NA_NUMBER_9_ENVELOPE,
        MediaSizeName.NA_NUMBER_10_ENVELOPE,
        MediaSizeName.NA_NUMBER_11_ENVELOPE,
        MediaSizeName.NA_NUMBER_12_ENVELOPE,
        MediaSizeName.NA_NUMBER_14_ENVELOPE,
        MediaSizeName.NA_6X9_ENVELOPE,
        MediaSizeName.NA_7X9_ENVELOPE,
        MediaSizeName.NA_9X11_ENVELOPE,
        MediaSizeName.NA_9X12_ENVELOPE,
        MediaSizeName.NA_10X13_ENVELOPE,
        MediaSizeName.NA_10X14_ENVELOPE,
        MediaSizeName.NA_10X15_ENVELOPE,
        MediaSizeName.NA_5X7,
        MediaSizeName.NA_8X10};
    
    for ( int i = 0; i < mediaSizeNameArray.length; i++ ) {
        if (mediaSizeNameArray[i].toString().equalsIgnoreCase(mediaSizeName)) {
            return mediaSizeNameArray[i];
        }
    }
    return null;
}

// FIXME SAM 2011-06-26 Need to enable this somehow
/**
Lookup a MediaSizeName instance from the media size name (Media.toString(), when Media is a MediaSizeName).
@param mediaSizeName name of the media size (e.g., "na-letter").
@return the MediaSizeName that corresponds to the name, or null if not matched.
public MediaSizeName lookupMediaSizeNameFromName ( String mediaSizeName )
{   // Why are the string table and EnumSyntax array protected?  How can a lookup be done?
    //String [] stringTable = MediaSizeName.getStringTable();
    return null;
}
*/

/**
Fill the name map from standardized (Media.toString() to more usable names.
*/
private void populateDisplayNameMap() {
    paperNames.put("iso-a0", "A0");
    paperNames.put("iso-a1", "A1");
    paperNames.put("iso-a2", "A2");
    paperNames.put("iso-a3", "A3");
    paperNames.put("iso-a4", "A4");
    paperNames.put("iso-a5", "A5");
    paperNames.put("iso-a6", "A6");
    paperNames.put("iso-a7", "A7");
    paperNames.put("iso-a8", "A8");
    paperNames.put("iso-a9", "A9");
    paperNames.put("iso-a10", "A10");
    paperNames.put("iso-b0", "B0");
    paperNames.put("iso-b1", "B1");
    paperNames.put("iso-b2", "B2");
    paperNames.put("iso-b3", "B3");
    paperNames.put("iso-b4", "B4");
    paperNames.put("iso-b5", "B5");
    paperNames.put("iso-b6", "B6");
    paperNames.put("iso-b7", "B7");
    paperNames.put("iso-b8", "B8");
    paperNames.put("iso-b9", "B9");
    paperNames.put("iso-b10", "B10");
    paperNames.put("na-letter", "North American Letter");
    paperNames.put("na-legal", "North American Legal");
    paperNames.put("na-8x10", "North American 8x10 inch");
    paperNames.put("na-5x7", "North American 5x7 inch");
    paperNames.put("executive", "Executive");
    paperNames.put("folio", "Folio");
    paperNames.put("invoice", "Invoice");
    paperNames.put("tabloid", "Tabloid");
    paperNames.put("ledger", "Ledger");
    paperNames.put("quarto", "Quarto");
    paperNames.put("iso-c0", "C0");
    paperNames.put("iso-c1", "C1");
    paperNames.put("iso-c2", "C2");
    paperNames.put("iso-c3", "C3");
    paperNames.put("iso-c4", "C4");
    paperNames.put("iso-c5", "C5");
    paperNames.put("iso-c6", "C6");
    paperNames.put("iso-designated-long", "ISO Designated Long size");
    paperNames.put("na-10x13-envelope", "North American 10x13 inch");
    paperNames.put("na-9x12-envelope", "North American 9x12 inch");
    paperNames.put("na-number-10-envelope", "North American number 10 business envelope");
    paperNames.put("na-7x9-envelope", "North American 7x9 inch envelope");
    paperNames.put("na-9x11-envelope", "North American 9x11 inch envelope");
    paperNames.put("na-10x14-envelope", "North American 10x14 inch envelope");
    paperNames.put("na-number-9-envelope", "North American number 9 business envelope");
    paperNames.put("na-6x9-envelope", "North American 6x9 inch envelope");
    paperNames.put("na-10x15-envelope", "North American 10x15 inch envelope");
    paperNames.put("monarch-envelope", "Monarch envelope");
    paperNames.put("jis-b0", "Japanese B0");
    paperNames.put("jis-b1", "Japanese B1");
    paperNames.put("jis-b2", "Japanese B2");
    paperNames.put("jis-b3", "Japanese B3");
    paperNames.put("jis-b4", "Japanese B4");
    paperNames.put("jis-b5", "Japanese B5");
    paperNames.put("jis-b6", "Japanese B6");
    paperNames.put("jis-b7", "Japanese B7");
    paperNames.put("jis-b8", "Japanese B8");
    paperNames.put("jis-b9", "Japanese B9");
    paperNames.put("jis-b10", "Japanese B10");
    paperNames.put("a", "Engineering ANSI A");
    paperNames.put("b", "Engineering ANSI B");
    paperNames.put("c", "Engineering ANSI C");
    paperNames.put("d", "Engineering ANSI D");
    paperNames.put("e", "Engineering ANSI E");
    paperNames.put("arch-a", "Architectural A");
    paperNames.put("arch-b", "Architectural B");
    paperNames.put("arch-c", "Architectural C");
    paperNames.put("arch-d", "Architectural D");
    paperNames.put("arch-e", "Architectural E");
    paperNames.put("japanese-postcard", "Japanese Postcard");
    paperNames.put("oufuko-postcard", "Oufuko Postcard");
    paperNames.put("italian-envelope", "Italian Envelope");
    paperNames.put("personal-envelope", "Personal Envelope");
    paperNames.put("na-number-11-envelope", "North American Number 11 Envelope");
    paperNames.put("na-number-12-envelope", "North American Number 12 Envelope");
    paperNames.put("na-number-14-envelope", "North American Number 14 Envelope");
    
    // Loop through the map and additionally populate the names with the page size.
    // For example this would result in a display name "North American Letter - 8.5x11 in"
    // TODO SAM 2011-06-25 Need to finish this - don't have time to figure out all the lookups!
    
    MediaSizeName mediaSizeName;
    float pageWidth;
    float pageHeight;
    for ( Map.Entry<String,String> entry: paperNames.entrySet() ) {
        String key = entry.getKey();
        String value = entry.getValue();
        // Determine the MediaSizeName instance for the string name
        mediaSizeName = lookupMediaSizeNameFromString(key);
        MediaSize mediaSize = MediaSize.getMediaSizeForName(mediaSizeName);
        //Message.printStatus(2, routine, "paper size for \"" + paperSize + "\" is " + mediaSize );
        if ( mediaSize != null ) {
            pageWidth = mediaSize.getX(MediaSize.INCH);
            pageHeight = mediaSize.getY(MediaSize.INCH);
            // Update the value with the dimension (even if already included in the name)
            value = value + " (" + StringUtil.formatString(pageWidth,"%.2f") + "x" +
                StringUtil.formatString(pageHeight,"%.2f") + " in)";
            paperNames.put(key, value);
        }
    }
}

}