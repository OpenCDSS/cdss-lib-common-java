package RTi.TS;

import java.util.List;
import java.util.Vector;

import RTi.Util.Time.TimeInterval;

// TODO SAM 2009-10-14 Migrate computational code from TSAnalyst to here
/**
Compute a YearTS that has a statistic for each year in the period.
*/
public class TSUtil_NewStatisticTimeSeries
{

/**
Get the list of statistics that can be performed.
*/
public static List<TSStatisticType> getStatisticChoices()
{
    // TODO SAM 2009-10-14 Need to enable more statistics
    List<TSStatisticType> choices = new Vector();
    //choices.add ( TSStatisticType.MAX );
    choices.add ( TSStatisticType.MEAN );
    //choices.add ( TSStatisticType.MEDIAN );
    //choices.add ( TSStatisticType.MIN );
    //choices.add ( TSStatisticType.SKEW );
    //choices.add ( TSStatisticType.STD_DEV );
    //choices.add ( TSStatisticType.VARIANCE );
    return choices;
}

/**
Get the list of statistics that can be performed.
@return the statistic display names as strings.
*/
public static List<String> getStatisticChoicesAsStrings()
{
    List<TSStatisticType> choices = getStatisticChoices();
    List<String> stringChoices = new Vector();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
}

}