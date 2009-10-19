package RTi.TS;

import java.util.List;
import java.util.Vector;

import RTi.Util.Time.TimeInterval;

// TODO SAM 2009-10-14 Migrate computational code from TSAnalyst to here
/**
Compute a YearTS that has a statistic for each year in the period.
*/
public class TSUtil_NewStatisticYearTS
{

/**
Return a list of statistic choices for the requested interval and scale.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.  Parameters can be used to limit the
choices (these features will be phased in over time as statistics are added).
@param interval TimeInterval.DAY, etc., indicating the interval of data for the
statistic (e.g., average value for the year).  Pass TimeInterval.UNKNOWN to get all choices.
@param timescale MeasTimeScale.ACCM, etc., indicating whether the statistic is
expected on accumulated, mean, instantaneous data.  Pass null to get all choices.  CURRENTLY NOT USED.
*/
public static List<TSStatisticType> getStatisticChoicesForInterval ( int interval, String timescale )
{   List<TSStatisticType> statistics = new Vector();
    if ( (interval >= TimeInterval.MONTH) || (interval == TimeInterval.UNKNOWN) ) {
        statistics.add ( TSStatisticType.COUNT_GE );
        statistics.add ( TSStatisticType.COUNT_GT );
        statistics.add ( TSStatisticType.COUNT_LE );
        statistics.add ( TSStatisticType.COUNT_LT );
        statistics.add ( TSStatisticType.DAY_OF_FIRST_GE );
        statistics.add ( TSStatisticType.DAY_OF_FIRST_GT );
        statistics.add ( TSStatisticType.DAY_OF_FIRST_LE );
        statistics.add ( TSStatisticType.DAY_OF_FIRST_LT );
        statistics.add ( TSStatisticType.DAY_OF_LAST_GE );
        statistics.add ( TSStatisticType.DAY_OF_LAST_GT );
        statistics.add ( TSStatisticType.DAY_OF_LAST_LE );
        statistics.add ( TSStatisticType.DAY_OF_LAST_LT );
        statistics.add ( TSStatisticType.DAY_OF_MAX );
        statistics.add ( TSStatisticType.DAY_OF_MIN );
        statistics.add ( TSStatisticType.MAX );
        // TODO SAM 2009-10-14 Need to support median
        //statistics.add ( TSStatisticType.MEDIAN );
        statistics.add ( TSStatisticType.MEAN );
        statistics.add ( TSStatisticType.MIN );
        statistics.add ( TSStatisticType.TOTAL );
    }
    return statistics;
}

/**
Return a list of statistic choices for the requested interval and scale.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.  Parameters can be used to limit the
choices (these features will be phased in over time as statistics are added).
@param interval TimeInterval.DAY, etc., indicating the interval of data for the
statistic (e.g., average value for the year).  Pass TimeInterval.UNKNOWN to get all choices.
@param timescale MeasTimeScale.ACCM, etc., indicating whether the statistic is
expected on accumulated, mean, instantaneous data.  Pass null to get all choices.  CURRENTLY NOT USED.
*/
public static List<String> getStatisticChoicesForIntervalAsStrings ( int interval, String timescale )
{
    List<TSStatisticType> choices = getStatisticChoicesForInterval( interval, timescale);
    List<String> stringChoices = new Vector();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
}

}