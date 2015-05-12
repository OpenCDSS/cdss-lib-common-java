package RTi.Util.Table;

import java.util.LinkedHashMap;
import java.util.Map;

import RTi.Util.Message.Message;
import RTi.Util.String.StringDictionary;

/**
This class provides a filter to determine whether table rows that are being processed match filter criteria.
*/
public class DataTableFilter
{

/**
Table to be filtered.
*/
private DataTable table = null;

/**
Table column numbers for include filter columns.
*/
private int [] columnIncludeFiltersNumbers = new int[0];

/**
Glob (*) patterns to match include filter columns.
*/
private String [] columnIncludeFiltersGlobs = null;

/**
Table column numbers for exclude filter columns.
*/
private int [] columnExcludeFiltersNumbers = new int[0];

/**
Glob (*) patterns to match exclude filter columns.
*/
private String [] columnExcludeFiltersGlobs = null;

/**
Constructor for StringDictionaries.
*/
public DataTableFilter ( DataTable table, StringDictionary columnIncludeFilters, StringDictionary columnExcludeFilters )
throws InvalidTableColumnException
{	this.table = table;
    // Get include filter columns and glob-style regular expressions
    if ( columnIncludeFilters != null ) {
        LinkedHashMap<String, String> map = columnIncludeFilters.getLinkedHashMap();
        this.columnIncludeFiltersNumbers = new int[map.size()];
        this.columnIncludeFiltersGlobs = new String[map.size()];
        int ikey = -1;
        String key = null;
        for ( Map.Entry<String,String> entry : map.entrySet() ) {
            ++ikey;
            this.columnIncludeFiltersNumbers[ikey] = -1;
            try {
                key = entry.getKey();
                this.columnIncludeFiltersNumbers[ikey] = table.getFieldIndex(key);
                this.columnIncludeFiltersGlobs[ikey] = map.get(key);
                // Turn default globbing notation into internal Java regex notation
                this.columnIncludeFiltersGlobs[ikey] = this.columnIncludeFiltersGlobs[ikey].replace("*", ".*").toUpperCase();
            }
            catch ( Exception e ) {
                throw new InvalidTableColumnException ( "ColumnIncludeFilters column \"" + key + "\" not found in table \"" + table.getTableID() + "\"");
            }
        }
    }
    // Get exclude filter columns and glob-style regular expressions
    if ( columnExcludeFilters != null ) {
        LinkedHashMap<String, String> map = columnExcludeFilters.getLinkedHashMap();
        this.columnExcludeFiltersNumbers = new int[map.size()];
        this.columnExcludeFiltersGlobs = new String[map.size()];
        int ikey = -1;
        String key = null;
        for ( Map.Entry<String,String> entry : map.entrySet() ) {
            ++ikey;
            this.columnExcludeFiltersNumbers[ikey] = -1;
            try {
                key = entry.getKey();
                this.columnExcludeFiltersNumbers[ikey] = table.getFieldIndex(key);
                this.columnExcludeFiltersGlobs[ikey] = map.get(key);
                // Turn default globbing notation into internal Java regex notation
                this.columnExcludeFiltersGlobs[ikey] = this.columnExcludeFiltersGlobs[ikey].replace("*", ".*").toUpperCase();
                Message.printStatus(2,"","Exclude filter column \"" + key + "\" [" +
                	this.columnExcludeFiltersNumbers[ikey] + "] glob \"" + this.columnExcludeFiltersGlobs[ikey] + "\"" );
            }
            catch ( Exception e ) {
            	throw new InvalidTableColumnException ( "ColumnExcludeFilters column \"" + key + "\" not found in table \"" + table.getTableID() + "\"");
            }
        }
    }
}

/**
Determine whether a row should be included in processing because it matches the include and exclude filters.
@param irow row index (0+) to check for inclusion
@param throwExceptions if true, throw exceptions when table data cannot be checked for some reason
*/
public boolean includeRow ( int irow, boolean throwExceptions )
{
	DataTable table = this.table;
	
	boolean filterMatches = true; // Default is match
	Object o;
	String s;
    if ( this.columnIncludeFiltersNumbers.length > 0 ) {
        // Filters can be done on any columns so loop through to see if row matches
        for ( int icol = 0; icol < this.columnIncludeFiltersNumbers.length; icol++ ) {
            if ( this.columnIncludeFiltersNumbers[icol] < 0 ) {
                filterMatches = false;
                break;
            }
            try {
                o = table.getFieldValue(irow, this.columnIncludeFiltersNumbers[icol]);
                if ( o == null ) {
                    filterMatches = false;
                    break; // Don't include nulls when checking values
                }
                s = ("" + o).toUpperCase();
                if ( !s.matches(this.columnIncludeFiltersGlobs[icol]) ) {
                    // A filter did not match so don't copy the record
                    filterMatches = false;
                    break;
                }
            }
            catch ( Exception e ) {
            	if ( throwExceptions ) {
            		throw new RuntimeException("Error getting table data for [" + irow + "][" +
            			this.columnIncludeFiltersNumbers[icol] + "] (" + e + ")." );
            	}
            }
        }
        if ( !filterMatches ) {
            // Skip the record.
            return false;
        }
    }
    if ( this.columnExcludeFiltersNumbers.length > 0 ) {
        int matchesCount = 0;
        // Filters can be done on any columns so loop through to see if row matches
        for ( int icol = 0; icol < this.columnExcludeFiltersNumbers.length; icol++ ) {
            if ( this.columnExcludeFiltersNumbers[icol] < 0 ) {
                // Can't do filter so don't try
                break;
            }
            try {
                o = table.getFieldValue(irow, this.columnExcludeFiltersNumbers[icol]);
                //Message.printStatus(2,"","Got cell object " + o );
                if ( o == null ) {
                	if ( this.columnExcludeFiltersGlobs[icol].isEmpty() ) {
                		// Trying to match blank cells
                		++matchesCount;
                	}
                	else { // Don't include nulls when checking values
                		break;
                	}
                }
                s = ("" + o).toUpperCase();
                //Message.printStatus(2,"","Comparing table value \"" + s + "\" with exclude filter \"" + columnExcludeFiltersGlobs[icol] + "\"");
                if ( s.matches(this.columnExcludeFiltersGlobs[icol]) ) {
                    // A filter matched so don't copy the record
                	//Message.printStatus(2,"","Exclude filter matches");
                    ++matchesCount;
                }
            }
            catch ( Exception e ) {
            	if ( throwExceptions ) {
            		throw new RuntimeException("Error getting table data for [" + irow + "][" +
            			this.columnExcludeFiltersNumbers[icol] + "] (" + e + ")." );
            	}
            }
        }
        //Message.printStatus(2,"","matchesCount=" + matchesCount + " excludeFiltersLength=" +  columnExcludeFiltersNumbers.length );
        if ( matchesCount == this.columnExcludeFiltersNumbers.length ) {
            // Skip the record since all exclude filters were matched
        	//Message.printStatus(2,"","Skipping since all exclude filters matched");
            return false;
        }
    }
    return filterMatches;
}

}