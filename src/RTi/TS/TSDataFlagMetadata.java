package RTi.TS;

/**
Metadata about flags used with a time series.
Instances of this class can be added to a time series via addDataFlagMetaData() method.
This information is useful for output reports and displays, to explain the meaning of data flags.
The class is immutable.
*/
public class TSDataFlagMetadata
{
    
/**
Data flag.  Although this is a string, flags are generally one character.
*/
private String __dataFlag = "";

/**
Description for the data flag.
*/
private String __description = "";

/**
Constructor.
@param dataFlag data flag (generally one character).
@param description description of the data flag.
*/
public TSDataFlagMetadata ( String dataFlag, String description )
{
    setDataFlag ( dataFlag );
    setDescription ( description );
}

/**
Return the data flag.
@return the data flag
*/
public String getDataFlag ()
{
    return __dataFlag;
}

/**
Return the data flag description.
@return the data flag description
*/
public String getDescription ()
{
    return __description;
}

/**
Set the data flag.
@param dataFlag the data flag
*/
private void setDataFlag ( String dataFlag )
{
    __dataFlag = dataFlag;
}

/**
Set the description for the data flag.
@param description the data flag description
*/
private void setDescription ( String description )
{
    __description = description;
}

}