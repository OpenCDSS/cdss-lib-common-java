2013-02-22 Steve Malers The statement below does not seem to be accurate.
    Only xbean.jar seems to be in the Java build path while libXMLJava.jar,
    jsr173_1.0_api.jar, and xbean.jar are included in the product.properties
    and are distributed.
    
    However, a search of the TSTool code shows that no code is using the
    classes in RTi.Util.XML.  The packages may have been added to support NDFD
    web services, but was never used.  XML packages are included in the Java SDK
    now so there is less need for third-party code.  However, Apache POI is being
    used in TSTool.

    Remove xbean.jar from the build path because it seems to be interfering
    with new Apache POI package to read from Excel (xmlbeans-2.3.0).
    
    If removing from build path works, then remove the old files from the
    distribution and move to the new xmlbeans jar, including the RTi.Util.XML
    code (perhaps used in NDFD work that was never made operational?).
    
    Keep this README file for now in case there is a need to revert.
    
2008-03-01?
    These jar files are used by the RTi.Util.XML package code.  Currently it appears
    that only libXMLJava.jar is needed but the others are kept around because they
    seem to have been needed previously.  Perhaps they were used when attempting
    to enable the NDFD time series reader.

