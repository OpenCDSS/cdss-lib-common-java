// TSUtil_SortTimeSeries - sort time series.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

package RTi.TS;

import java.util.ArrayList;
import java.util.List;

import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Sort time series.
*/
public class TSUtil_SortTimeSeries <T extends TS>
{

/**
Time series to sort.
*/
private List<T> tslist = null;

/**
How to get TSID to sort ("TSID" or "AliasTSID").
*/
private String tsidFormat = null;

/**
Time series property to sort.
*/
private String property = null;

/**
Time series property format for sorting.
*/
private String propertyFormat = null;

/**
Time series sort order, -1 for descending and 1 for ascending.
*/
private int sortOrder = 1;

/**
Constructor.
@param tslist Time series to process.
@param tsidFormat how to get TSID, either TSID or AliasTSID.
@param property time series property to sort
@param propertyFormat time series property format for sorting, using C-style %s, etc.
@param sortOrder sort order, -1 descending or 1 ascending.
*/
public TSUtil_SortTimeSeries ( List<T> tslist, String tsidFormat, String property, String propertyFormat, int sortOrder ) {
    this.tslist = tslist;
    this.tsidFormat = tsidFormat;
    this.property = property;
    this.propertyFormat = propertyFormat;
    this.sortOrder = sortOrder;
}

/**
Sort the time series as per the constructor parameters.
*/
public List<T> sortTimeSeries ( )
throws Exception {
    List<T> tslist = this.tslist;

    if ( (tslist == null) || (tslist.size() == 0) ) {
        return tslist;
    }
    int order = StringUtil.SORT_ASCENDING;
    if ( this.sortOrder < 0 ) {
        order = StringUtil.SORT_DESCENDING;
    }
    // Sort precedence is:
    // 1. Property.
    // 2. Formatted properties.
    // 3. Alias and/or TSID.
    int size = tslist.size();
    if ( (this.property != null) && !this.property.isEmpty() ) {
        // Sort using a specific time series property:
        // - first determine whether the type of property is consistent
        // - if so, sort using the native format so number sort OK
        // - if not, convert to strings to sort
        // - nulls are treated as small values
        T ts = null;
        int intCount = 0;
        int doubleCount = 0;
        int stringCount = 0;
        int unknownCount = 0;
        int nullCount = 0;
        Object propVal;
        for ( int i = 0; i < size; i++ ) {
            ts = tslist.get(i);
            if ( ts == null ) {
                ++nullCount;
            }
            else {
                propVal = ts.getProperty(this.property);
                if ( propVal == null ) {
                    ++nullCount;
                }
                else if ( (propVal instanceof Float) || (propVal instanceof Double) ) {
                    ++doubleCount;
                }
                else if ( (propVal instanceof Integer) || (propVal instanceof Long) ) {
                    ++intCount;
                }
                else if ( propVal instanceof String ) {
                    ++stringCount;
                }
                else {
                    ++unknownCount;
                }
            }
        }
        if ( stringCount < 0 ) {
        	// TODO use so compiler does not complain about unused variable.
        }
        if ( unknownCount < 0 ) {
        	// TODO use so compiler does not complain about unused variable.
        }
        if ( (doubleCount + nullCount) == size ) {
            // Sorting floating point numbers.
            double [] doubles = new double[size];
            // Sort doubles.
            Object o;
            for ( int i = 0; i < size; i++ ) {
                ts = tslist.get(i);
                if ( ts == null ) {
                    // Set to smallest double value.
                    doubles[i] = Double.MIN_VALUE;
                }
                else {
                    o = ts.getProperty(this.property);
                    if ( o == null ) {
                        doubles[i] = Double.MIN_VALUE;
                    }
                    else {
                        // Check on counts should have determined all Doubles or nulls so cast should be safe.
                        doubles[i] = (Double)o;
                    }
                }
            }
            int [] sortOrderArray = new int[size];
            // Get the sorted order.
            boolean useSortArray = true;
            MathUtil.sort ( doubles, MathUtil.SORT_QUICK, order, sortOrderArray, useSortArray );
            List<T> tslistSorted = new ArrayList<>( size );
            for ( int i = 0; i < size; i++ ) {
                tslistSorted.add( tslist.get ( sortOrderArray[i] ) );
            }
            return tslistSorted;
        }
        else if ( (intCount + nullCount) == size ) {
            // Sorting integer numbers.
            long [] integers = new long[size];
            // Sort integers.
            for ( int i = 0; i < size; i++ ) {
                ts = tslist.get(i);
                if ( ts == null ) {
                    // Set to smallest integer value.
                    integers[i] = Integer.MIN_VALUE;
                }
                else {
                    Object o = ts.getProperty(this.property);
                    if ( o == null ) {
                        integers[i] = Integer.MIN_VALUE;
                    }
                    else {
                        // Check on counts should have determined all Integers or nulls so cast should be safe.
                        integers[i] = (Integer)o;
                    }
                }
            }
            int [] sortOrderArray = new int[size];
            // Get the sorted order.
            boolean useSortArray = true;
            MathUtil.sort ( integers, MathUtil.SORT_QUICK, order, sortOrderArray, useSortArray );
            List<T> tslistSorted = new ArrayList<>( size );
            for ( int i = 0; i < size; i++ ) {
                tslistSorted.add( tslist.get ( sortOrderArray[i] ) );
            }
            return tslistSorted;
        }
        else {
            // Sorting strings.
            List<String> strings = new ArrayList<>(size);
            // Sort by formatting a property string.
            for ( int i = 0; i < size; i++ ) {
                ts = tslist.get(i);
                if ( ts == null ) {
                    strings.add ( "" );
                }
                else {
                    Object o = ts.getProperty(this.property);
                    if ( o == null ) {
                        strings.add ( "" );
                    }
                    else {
                        // TODO SAM 2014-05-12 This may have problems with floating point numbers not formatting nicely
                    	// (e.g., scientific notation).
                        strings.add ( "" + o );
                    }
                }
            }
            int [] sortOrderArray = new int[size];
            // Get the sorted order.
            boolean useSortArray = true;
            boolean ignoreCase = true;
            StringUtil.sortStringList ( strings, order, sortOrderArray, useSortArray, ignoreCase);
            List<T> tslistSorted = new ArrayList<>( size );
            for ( int i = 0; i < size; i++ ) {
                tslistSorted.add( tslist.get ( sortOrderArray[i] ) );
            }
            return tslistSorted;
        }
    }
    else if ( (this.propertyFormat != null) && !this.propertyFormat.isEmpty() ) {
        List<String> strings = new ArrayList<>(size);
        // Sort by formatting a property string.
        T ts = null;
        for ( int i = 0; i < size; i++ ) {
            ts = tslist.get(i);
            if ( ts == null ) {
                strings.add ( "" );
            }
            else {
                strings.add ( ts.formatLegend(this.propertyFormat) );
            }
        }
        int [] sortOrderArray = new int[size];
        // Get the sorted order.
        boolean useSortArray = true;
        boolean ignoreCase = true;
        StringUtil.sortStringList ( strings, order, sortOrderArray, useSortArray, ignoreCase );
        // Now sort the time series.
        List<T> tslistSorted = new ArrayList<>( size );
        for ( int i = 0; i < size; i++ ) {
            tslistSorted.add( tslist.get ( sortOrderArray[i] ) );
        }
        return tslistSorted;
    }
    else {
        // Default is to sort by the Alias and/or TSID.
        boolean tryAliasFirst = false;
        if ( (this.tsidFormat != null) && this.tsidFormat.equalsIgnoreCase("AliasTSID") ) {
            tryAliasFirst = true;
        }
        TSIdent tsid;
        List<String> strings = new ArrayList<>(size);
        T ts = null;
        for ( int i = 0; i < size; i++ ) {
            ts = tslist.get(i);
            if ( tryAliasFirst ) {
                // Use the alias if non-null and non-blank.
                String alias = ts.getAlias();
                if ( (alias != null) && !alias.isEmpty() ) {
                    strings.add(alias);
                    continue;
                }
            }
            if ( ts == null ) {
                strings.add ( "" );
                continue;
            }
            tsid = ts.getIdentifier();
            if ( tsid == null ) {
                strings.add ( "" );
                continue;
            }
            // Use the full identifier.
            strings.add ( tsid.toString( true ) );
        }
        int [] sortOrderArray = new int[size];
        // Get the sorted order.
        boolean useSortArray = true;
        boolean ignoreCase = true;
        StringUtil.sortStringList ( strings, order, sortOrderArray, useSortArray, ignoreCase );
        // Now sort the time series.
        List<T> tslistSorted = new ArrayList<>( size );
        for ( int i = 0; i < size; i++ ) {
            tslistSorted.add( tslist.get ( sortOrderArray[i] ) );
        }
        return tslistSorted;
    }
}

}