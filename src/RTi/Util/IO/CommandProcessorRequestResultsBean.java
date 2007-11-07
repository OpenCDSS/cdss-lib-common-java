package RTi.Util.IO;

/**
This interface provides accessor methods to retrieve the results of
a CommandsProcessor processRequest() call.  The returned information is
expected to include a PropList of name/data pairs, and information suitable
for users if an error occurs.
*/
public interface CommandProcessorRequestResultsBean {

/**
Return the results data.
@return the results PropList.  The name of the value can be used to
look up the results sub-component and the contents of the property
provide the data.  Results may be unavailable (null prop or contents).
However, the PropList should always be non-null.
*/
public PropList getResultsPropList ();

// TODO SAM 2007-02-10 Evaluate whether eveloper and user info is needed
// TODO SAM 2007-02-10 Evaluate whether HTML should be allowed.
/**
Return the warning associated with a request, if it failed or had to
resolve an issue while returning information.
@return a warning message that explains why the request failed.
The message can include embedded newline characters.
The message can contain newlines.
*/
public String getWarningText();

/**
Return a suggested fix to resolve the warning.
@return a message that offers a suggestion to fix the warning.
*/
public String getWarningRecommendationText();

}