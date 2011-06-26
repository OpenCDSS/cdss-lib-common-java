package RTi.Util.IO;

import java.util.HashMap;
import java.util.Map;

import javax.print.PrintService;
import javax.print.attribute.standard.Media;

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
Map<String,String> paperNames = new HashMap();
    
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
    
    for ( Map.Entry<String,String> entry: paperNames.entrySet() ) {
        String key = entry.getKey();
        String value = entry.getValue();
        // Determine the MediaSizeName instance for the string name
        // Update the value with the dimension (even if already included in the name)
    }
}

}